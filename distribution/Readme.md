# Put pre-compiled libraries here

This directory should hold the native dependencies, such as FFmpeg.

Artifacts are built on [Travis CI](https://travis-ci.org/icctv/ffmpeg-android/builds), and pushed to [GitHub Releases](https://github.com/icctv/ffmpeg-android/releases).

Download and unzip them, or run this script:

  $ bash ./get_native_dependencies.sh

Make sure your files are placed in the right folders, like in the tree below:

```
  /
  ├── app
  ├── build.gradle
  ├── ci
  │   ├── install.sh
  │   └── sign.sh
  └── distribution
      ├── Readme.md   <-- this file
      └── ffmpeg
          ├── armeabi-v7a
          │   ├── include
          │   └   └── libavcodec [...]
          ├── armeabi-v7a-neon
          │   ├── include
          │   └   └── libavcodec [...]
          └── x86
              ├── include
              └   └── libavcodec [...]
```
