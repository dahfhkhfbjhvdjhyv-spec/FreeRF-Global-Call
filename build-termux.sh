#!/data/data/com.termux/files/usr/bin/bash
set -e
if [ -z "${ANDROID_HOME:-}" ]; then
  echo "Set ANDROID_HOME and install Android SDK platform 35 and build-tools."
  exit 1
fi
gradle assembleDebug
echo "APK: app/build/outputs/apk/debug/app-debug.apk"
