cmake_minimum_required(VERSION 3.4.1)

add_library(encoder
            SHARED
            src/main/cpp/encoder.cpp)

include_directories(src/main/cpp/include/)