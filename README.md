# ZCode Pocket

An Android client for observing and controlling ZCode sessions through a ZCode ACP hub. Includes chats, projects, agents, machine status, streaming responses, tool cards, and permission prompts. [T1: `app/src/main/java/app/zcode/pocket/`]

## Build

Open this directory in Android Studio and configure an Android SDK. The project uses Java 17, Android SDK 36, Build Tools 36.0.0, Kotlin 2.2.0, and Gradle 8.13. Minimum Android API: 26. [T1: `app/build.gradle.kts`, `build.gradle.kts`, `gradle/wrapper/gradle-wrapper.properties`]

On Windows:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
```

On macOS or Linux:

```sh
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

The debug APK is written to `.out/app/outputs/apk/debug/app-debug.apk`. [T1: `build.gradle.kts` build-directory setting]

## Connect

Run a separately configured [ZCode ACP hub](https://github.com/william0wang/zcode-acp). In the app's Settings, enter your machine's hub URL and remote bearer token, then use Test. Replace the initial URL with your own machine's address. Keep credentials and signing keys out of Git. [T1: `app/src/main/java/app/zcode/pocket/ui/SettingsScreen.kt`, `app/src/main/java/app/zcode/pocket/data/Prefs.kt`]

## Repository scope

This repository contains the Android project extracted from the ZCode Playbook's `playbook/toolkit/zcode-pocket/android` unit. The desktop launcher, hub, and design twin are separate components. The app source is published unchanged; caches, generated APKs, and local SDK settings are excluded. [T1: publication source snapshot, 2026-10-12]
