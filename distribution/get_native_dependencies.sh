#!/bin/bash

set -e

echo "Downloading native pre-built dependencies for iCCTV"

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $DIR

wget https://github.com/icctv/ffmpeg-android/releases/download/1.1/build.zip
unzip build.zip

mv build ffmpeg
rm build.zip
rm -rf build/

cd -
