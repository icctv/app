cmake_minimum_required(VERSION 3.4.1)

set (APP_PLATFORM -DAPP_PLATFORM=android-9)

# Where to find the prebuilt libraries?
set(distribution_DIR ${CMAKE_SOURCE_DIR}/../distribution)

# Import prebuilt ffmpeg libraries
add_library(ffmpeg-codec STATIC IMPORTED)
set_target_properties(ffmpeg-codec PROPERTIES IMPORTED_LOCATION ${distribution_DIR}/ffmpeg/${ANDROID_ABI}/lib/libavcodec.a)

add_library(ffmpeg-device STATIC IMPORTED)
set_target_properties(ffmpeg-device PROPERTIES IMPORTED_LOCATION ${distribution_DIR}/ffmpeg/${ANDROID_ABI}/lib/libavdevice.a)

add_library(ffmpeg-filter STATIC IMPORTED)
set_target_properties(ffmpeg-filter PROPERTIES IMPORTED_LOCATION ${distribution_DIR}/ffmpeg/${ANDROID_ABI}/lib/libavfilter.a)

add_library(ffmpeg-format STATIC IMPORTED)
set_target_properties(ffmpeg-format PROPERTIES IMPORTED_LOCATION ${distribution_DIR}/ffmpeg/${ANDROID_ABI}/lib/libavformat.a)

add_library(ffmpeg-util STATIC IMPORTED)
set_target_properties(ffmpeg-util PROPERTIES IMPORTED_LOCATION ${distribution_DIR}/ffmpeg/${ANDROID_ABI}/lib/libavutil.a)

add_library(ffmpeg-postproc STATIC IMPORTED)
set_target_properties(ffmpeg-postproc PROPERTIES IMPORTED_LOCATION ${distribution_DIR}/ffmpeg/${ANDROID_ABI}/lib/libpostproc.a)

add_library(ffmpeg-sample STATIC IMPORTED)
set_target_properties(ffmpeg-sample PROPERTIES IMPORTED_LOCATION ${distribution_DIR}/ffmpeg/${ANDROID_ABI}/lib/libswresample.a)

add_library(ffmpeg-scale STATIC IMPORTED)
set_target_properties(ffmpeg-scale PROPERTIES IMPORTED_LOCATION ${distribution_DIR}/ffmpeg/${ANDROID_ABI}/lib/libswscale.a)


# Set compiler flags
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")

# Fix 'shared library text segment is not shareable' linker error
set(CMAKE_SHARED_LINKER_FLAGS "-Wall -v -Wl,--no-warn-shared-textrel")


# Add custom encoder library
add_library(encoder
            SHARED
            src/main/cpp/encoder.cpp)


# Where to find the libraries that our custom encoder depends on?
target_include_directories(encoder PRIVATE
                           ${distribution_DIR}/ffmpeg/${ANDROID_ABI}/include)


# Add logging library
find_library(log-lib
             log)


# Now link everything together
target_link_libraries(encoder
                      ffmpeg-codec
                      ffmpeg-device
                      ffmpeg-filter
                      ffmpeg-format
                      ffmpeg-util
                      ffmpeg-postproc
                      ffmpeg-sample
                      ffmpeg-scale
                      z
                      ${log-lib})
