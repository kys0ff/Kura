# Kura 蔵

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
![Platform: Android 7.0+](https://img.shields.io/badge/Platform-Android%207.0%2B-blue.svg)

**Kura** is a lightweight, open-source app locker designed for speed and compatibility. It allows you to protect any app on your device using your system's biometric authentication (Fingerprint/Face).

## Why these permissions?
To provide a seamless locking experience, Kura requires two high-level permissions:

1. **Accessibility Service:** Used to detect in real-time when a "locked" app is being opened. This is the most battery-efficient way to monitor app launches on Android 7+.
2. **Display Over Other Apps:** Allows Kura to show the biometric authentication screen immediately over the target app, preventing unauthorized access to the app's content.

## Features
* **Broad Compatibility:** Supports devices running Android 7.0 (API 24) and above.
* **Biometric Security:** Leverages the official Android Biometric hardware.
* **Privacy Centric:** No internet access. No data collection. Just security.
* **Instant Lock:** High-performance detection ensures the lock screen appears before the app content is visible.

## Installation & Setup
1. Download the latest APK from the [Releases](#) section.
2. **Enable Accessibility:** Go to Settings > Accessibility > Kura and toggle "On."
3. **Allow Overlay:** When prompted, grant "Display over other apps" permission.
4. Select the apps you wish to protect and you're good to go!

## License
Licensed under the **MIT License**. See `LICENSE` for details.
