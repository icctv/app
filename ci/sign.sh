#!/bin/bash
echo "Signing..."

cp $TRAVIS_BUILD_DIR/.keystore $HOME
cd app/build/outputs/apk/
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore $HOME/keystore.jks -storepass $KEYSTORE_PASSWORD -keypass $KEY_PASSWORD app-release-unsigned.apk $KEY_ALIAS

echo "Verifying signature..."
jarsigner -verify app-release-unsigned.apk
"${ANDROID_HOME}/build-tools/21.1.2/zipalign -v 4 app-release-unsigned.apk yourapp.apk"
