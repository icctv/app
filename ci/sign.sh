#!/bin/bash
echo "Signing..."

set -e

if [ -z ${TRAVIS_BUILD_DIR+x} ]; then
  echo "TRAVIS_BUILD_DIR is not set";
  exit 1;
fi

if [ -z ${KEYSTORE_FILE+x} ]; then
  echo "KEYSTORE_FILE is not set";
  exit 1;
fi

if [ -z ${KEYSTORE_PASSWORD+x} ]; then
  echo "KEYSTORE_PASSWORD is not set";
  exit 1;
fi

if [ -z ${KEY_PASSWORD+x} ]; then
  echo "KEY_PASSWORD is not set";
  exit 1;
fi

if [ -z ${KEY_PASSWORD+x} ]; then
  echo "KEY_PASSWORD is not set";
  exit 1;
fi

if [ -z ${APK_NAME+x} ]; then
  echo "APK_NAME is not set";
  exit 1;
fi

cp $TRAVIS_BUILD_DIR/$KEYSTORE_FILE $HOME
cd app/build/outputs/apk/
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore $HOME/$KEYSTORE_FILE -storepass $KEYSTORE_PASSWORD -keypass $KEY_PASSWORD app-release-unsigned.apk $KEY_ALIAS

echo "Verifying signature..."
jarsigner -verify app-release-unsigned.apk
"${ANDROID_HOME}/build-tools/$ANDROID_BUILD_TOOLS/zipalign -v 4 app-release-unsigned.apk $APK_NAME"
