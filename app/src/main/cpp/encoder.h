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

    AVPacket packet;
    SwsContext *sws;
    AVFormatContext *muxer;
    AVStream *stream;
} encoder_t;

typedef enum {
    frame_type_video = 0xFA010000,
    frame_type_audio = 0xFB010000
} frame_type_t;

typedef struct {
    frame_type_t type;
    int size;
    char data[0];
} frame_t;

int64_t getTimeNsec();
void measurePerformance(int duration);

#endif
