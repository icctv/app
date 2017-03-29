#include "encoder.h"

extern "C" {
    int64_t getTimeNsec() {
        struct timespec now;
        clock_gettime(CLOCK_MONOTONIC, &now);
        return (int64_t) now.tv_sec*1000000000LL + now.tv_nsec;
    }

    char TAG[] = "NativeEncoder";
    encoder_t *self;

    JNIEXPORT jstring JNICALL Java_gq_icctv_icctv_StreamingEncoder_getConfiguration(JNIEnv *env, jobject) {
        char info[10000] = {0};
        sprintf(info, "%s\n", avcodec_configuration());
        return env->NewStringUTF(info);
    }

    JNIEXPORT void JNICALL Java_gq_icctv_icctv_StreamingEncoder_nativeInitialize(JNIEnv *env, jobject, int in_width, int in_height, int out_width, int out_height, int bitrate) {
        LOGI(TAG, "Initializing");

        self = (encoder_t *) malloc(sizeof(encoder_t));
        memset(self, 0, sizeof(encoder_t));

        self->in_width = in_width;
        self->in_height = in_height;
        self->out_width = out_width;
        self->out_height = out_height;

        avcodec_register_all();

        self->codec = avcodec_find_encoder(AV_CODEC_ID_MPEG1VIDEO);

        self->context = avcodec_alloc_context3(self->codec);
        self->context->dct_algo = FF_DCT_FASTINT;
        self->context->bit_rate = bitrate;
        self->context->width = out_width;
        self->context->height = out_height;
        self->context->time_base.num = 1;
        self->context->time_base.den = 30;
        self->context->gop_size = 30;
        self->context->max_b_frames = 0;
        self->context->pix_fmt = AV_PIX_FMT_YUV420P;

        avcodec_open2(self->context, self->codec, NULL);

        self->frame = av_frame_alloc();
        self->frame->format = AV_PIX_FMT_YUV420P;
        self->frame->width  = out_width;
        self->frame->height = out_height;
        self->frame->pts = 0;

        // The magic constant 32 is the line size alignment which allows SIMD
        int frame_size = av_image_get_buffer_size(AV_PIX_FMT_YUV420P, out_width, out_height, 32);
        self->frame_buffer = malloc(frame_size);
        avpicture_fill((AVPicture*) self->frame, (uint8_t*) self->frame_buffer, AV_PIX_FMT_YUV420P, out_width, out_height);

        self->sws = sws_getContext(
            in_width, in_height, AV_PIX_FMT_NV21,
            out_width, out_height, AV_PIX_FMT_YUV420P,
            SWS_FAST_BILINEAR, 0, 0, 0
        );

        if (!self->sws) {
            LOGE(TAG, "Could not create scale context");
        }

        LOGI(TAG, "Initialized");
    }

    JNIEXPORT int JNICALL Java_gq_icctv_icctv_StreamingEncoder_nativeEncode(JNIEnv *env, jobject, jbyteArray pixelsBuffer) {
        int64_t time_start = getTimeNsec();
        int duration = 1;
        int fps = 0;


        int length = env->GetArrayLength(pixelsBuffer);

        uint8_t *pixels = (uint8_t *) env->GetByteArrayElements(pixelsBuffer, NULL);

        // The length of a stride is probably the width of the frame
        int stride = self->in_width;

        // Source pixels are in NV21 format: 2 planes, Y and VU
        // [Y0 Y1 Y2 Y4 ...       ] <- Y plane (luma)
        // [                      ]
        // [                      ]
        // [V0 U0 V1 U1 V2 U2 ... ] <- VU plane (chroma)
        uint8_t *in_data[2] = { pixels, pixels + stride * self->in_height };

        // In NV21, each of the two planes have the same stride (=width)
        int in_linesize[2] = { stride, stride };

        // LOGI(TAG, "Scaling in_linesize=%d, in_height=%d, in_width=%d, frame_linesize=%d",
        //     in_linesize[0],
        //     self->in_height,
        //     self->in_width,
        //     self->frame->linesize);

        // LOGI(TAG, "Scaling in_data[0]=%u, in_data[1]=%u", (unsigned int) pixels[0], (unsigned int) pixels[1]);

        // Perform pixel format conversion from NV21 to YV12 (YUV420P)
        // Scaling is fast (~1ms), don't worry about bottleneck here
        sws_scale(self->sws,
                  in_data,
                  in_linesize,
                  0,
                  self->in_height,
                  self->frame->data,
                  self->frame->linesize);


        self->frame->pts++;
        av_init_packet(&self->packet);
        int success = 0;
        avcodec_encode_video2(self->context, &self->packet, self->frame, &success);
        if(success) {
            // memcpy(encoded_data, self->packet.data, self->packet.size);
        } else {
            LOGE(TAG, "Failed to encode frame");
        }

        av_free_packet(&self->packet);

        // Free the array without copying back changes ("abort")
        env->ReleaseByteArrayElements(pixelsBuffer, (jbyte *) pixels, JNI_ABORT);
        env->DeleteLocalRef(pixelsBuffer);

        duration = (int)((getTimeNsec() - time_start) / 1000000);
        fps = (int)(1 / (duration / 1000.0));

        LOGI(TAG, "took %d ms (%d fps)", duration, fps);

        return success;
    }


    JNIEXPORT void JNICALL Java_gq_icctv_icctv_StreamingEncoder_nativeRelease(JNIEnv *env, jobject) {
        if (self == NULL) { return; }

        LOGI(TAG, "Releasing");
        sws_freeContext(self->sws);
        avcodec_close(self->context);
        av_free(self->context);
        av_free(self->frame);
        free(self->frame_buffer);
        free(self);
        LOGI(TAG, "Released");
    }
}
