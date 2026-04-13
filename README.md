# 🔒 Kura

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
![Platform: Android](https://img.shields.io/badge/Platform-Android_7.0_--_15.0-blue.svg)
![Language: Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF.svg)
![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen.svg)

**Kura** is a lightweight, privacy-focused security layer for Android, designed to protect your apps with biometric and device-level authentication without the corporate bloat.

---

> [!IMPORTANT]
> ### 🛑 THE ANDROID 16 PROTEST
> **Kura intentionally does NOT support Android 16 (API 36) or later.**
> We refuse to comply with Google's mandatory "Verified Developer" registries and their systematic destruction of independent sideloading. This app is built for users who own their devices, not for corporations that want to rent them back to you. We stand for a truly open Android.

---

## 🔧 Building from Source

### Prerequisites
- **Android Studio** (Ladybug or newer)
- **JDK 17** (Required for latest Gradle/Compose)
- **Android SDK** (API 24 to 35)

### Build Steps
```bash
# Clone the repository
git clone https://github.com/kys0ff/Kura.git
cd Kura

# Build the project
./gradlew build

# Generate Release APK
./gradlew assembleRelease
```
*The signed APK will be available at: `app/build/outputs/apk/release/`*

---

## 🛠️ Development & Tech Stack

- **Language:** 100% Kotlin
- **UI Framework:** Jetpack Compose (Material 3)
- **Architecture:** MVVM (Model-View-ViewModel)
- **Navigation:** Voyager
- **Dependency Injection:** Koin
- **Serialization:** KotlinX Serialization
- **Biometric API:** AndroidX Biometric for secure, hardware-backed authentication

---

## ⚠️ Privacy & Security

**Your privacy is sacred. Kura is built on a zero-trust model toward third-party servers.**

- ✅ **No Analytics:** We don't care how you use the app.
- ✅ **No Tracking:** No telemetry or "crash reporting" to Google/Firebase.
- ✅ **100% Offline:** No data ever leaves your device.
- ✅ **Transparent:** Source code is fully auditable.

---

## 🎯 Roadmap

- [x] Material Design 3 & Dynamic Color
- [x] Badge-based app filtering
- [x] Lock animation toggles
- [ ] Decoy app support
- [ ] Multi-user profile support
- [ ] **STAYING ON ANDROID 15** (Permanent)

---

## 📱 Compatibility

- **Minimum:** Android 7.0 (API 24)
- **Maximum:** Android 15 (API 35)
- **Android 16+:** **Incompatible.** We will not support Google's restricted registry policies.

---

## 📝 License

This project is licensed under the **MIT License**.

```text
MIT License

Copyright (c) 2026 kys0ff

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software...
```

---

## 🤝 Support

- 🐛 **Bugs:** [GitHub Issues](https://github.com/kys0ff/Kura/issues)
- 💬 **Ideas:** [GitHub Discussions](https://github.com/kys0ff/Kura/discussions)
- 👨‍💻 **Author:** [kys0ff](https://github.com/kys0ff)

**Made with ❤️ for Android Security and Freedom.**
