#!/bin/bash

export ADB_INSTALL_TIMEOUT=5

echo "$ANDROID_HOME"
mkdir "$ANDROID_HOME/licenses" || true
echo -e "\n8933bad161af4178b1185d1a37fbf41ea5269c55" > "$ANDROID_HOME/licenses/android-sdk-license"
echo -e "\n84831b9409646a918e30573bab4c9c91346d8abd" > "$ANDROID_HOME/licenses/android-sdk-preview-license"

echo y | android update sdk -u -a -t tools
echo y | android update sdk -u -a -t platform-tools
echo y | android update sdk -u -a -t build-tools-$ANDROID_BUILD_TOOLS
echo y | android update sdk -u -a -t android-$ANDROID_API
echo y | android update sdk -u -a -t extra-google-m2repository
echo y | android update sdk -u -a -t extra-android-m2repository
