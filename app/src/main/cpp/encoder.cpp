#include "encoder.h"

extern "C" {

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

    JNIEXPORT int JNICALL Java_gq_icctv_icctv_StreamingEncoder_nativeEncode(JNIEnv *env, jobject, void *pixels) {
        LOGI(TAG, "Encoding");

        LOGI(TAG, "Encoded");

        return 1;
    }


    JNIEXPORT void JNICALL Java_gq_icctv_icctv_StreamingEncoder_nativeRelease(JNIEnv *env, jobject) {
        if (self == NULL) { return; }

        LOGI("NativeEncoder", "Releasing");
        sws_freeContext(self->sws);
        avcodec_close(self->context);
        av_free(self->context);
        av_free(self->frame);
        free(self->frame_buffer);
        free(self);
        LOGI("NativeEncoder", "Released");
    }
}
