#ifndef APP_ENCODER_H
#define APP_ENCODER_H

#include <jni.h>
#include <string>
#include <iostream>
#include "log.h"

extern "C" {
    #include <libavcodec/avcodec.h>
    #include <libavformat/avformat.h>
    #include <libavutil/avutil.h>
    #include <libavutil/imgutils.h>
    #include <libswscale/swscale.h>
}

typedef struct {
    AVCodec *codec;
    AVCodecContext *context;
    AVFrame *frame;
    void *frame_buffer;

    int in_width, in_height;
    int out_width, out_height;
    char ingestUrl[2000];

    AVPacket packet;
    SwsContext *sws;
    AVFormatContext *muxer;
    AVStream *stream;
    uint8_t *output_buffer;
    AVIOContext *output;
} encoder_t;

int64_t getTimeNsec();
void measurePerformance(int duration);

#endif
