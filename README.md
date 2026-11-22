# FreeRF Global Call

Android Bluetooth voice-call prototype (Kotlin).

## What this build does
- Nearby, paired Android-to-Android voice over Bluetooth Classic RFCOMM.
- One phone waits for an incoming connection; the other selects a paired device and connects.
- Microphone and speaker audio are streamed in both directions.

## Important limits
This is **not** a thousand-mile radio phone. Ordinary Android apps cannot make two stock phones communicate over thousands of miles without a network path or specialized, legally authorized radio infrastructure. Bluetooth range is nearby only and varies by environment.

This is an early prototype, not a production calling app. Encryption, echo cancellation, robust reconnection, foreground-call service, and broad device testing are not included. Test only with trusted paired devices.

## Build
Open in Android Studio, allow Gradle sync, then build the debug APK.

For Termux, install/configure JDK 17, Android SDK platform 35 and build-tools, set ANDROID_HOME, and install Gradle. Then run:
```bash
bash build-termux.sh
```
APK output: `app/build/outputs/apk/debug/app-debug.apk`

## Use
1. Install on two Android phones.
2. Pair them in Android Bluetooth Settings.
3. On phone A, tap **Wait for incoming call**.
4. On phone B, tap **Call paired phone** and select phone A.
5. Grant microphone and Bluetooth permissions.
