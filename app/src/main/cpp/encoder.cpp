#include "encoder.h"

extern "C" {
    JNIEXPORT jstring JNICALL Java_gq_icctv_icctv_StreamingEncoder_getString(JNIEnv *env, jobject) {
        std::string info = "Unicorn";
        return env->NewStringUTF(info.c_str());
    }
}
