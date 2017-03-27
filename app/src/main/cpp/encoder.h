#ifndef APP_ENCODER_H
#define APP_ENCODER_H

#include <jni.h>
#include <string>
#include <iostream>
#include "log.h"

extern "C" {
    #include <libavcodec/avcodec.h>
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

    AVPacket packet;
    SwsContext *sws;
} encoder_t;

typedef struct {
    int size;
    char data[0];
} frame_t;


#endif
