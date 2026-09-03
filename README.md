# 🚀 VLC for Android — Reborn

<p align="center">
  <b>Minimalist UI • Peak Performance • True AMOLED Black • 120 FPS Fluidity • Battery Efficiency</b>
</p>

<p align="center">
  <a href="README.ru.md">🇷🇺 Читать на русском</a> | <b>🇬🇧 English</b>
</p>

<p align="center">
  <a href="https://github.com/anntr1k3/vlc-android-reborn/releases"><img src="https://img.shields.io/github/v/release/anntr1k3/vlc-android-reborn?color=orange&label=Download%20APK&logo=android" alt="Download Release"></a>
  <a href="https://github.com/anntr1k3/vlc-android-reborn/actions"><img src="https://img.shields.io/badge/Build-Passing-brightgreen?logo=github-actions" alt="Build Status"></a>
  <a href="COPYING"><img src="https://img.shields.io/badge/License-GPLv3-blue.svg" alt="License"></a>
</p>

---

> **Note:** **VLC Android Reborn** is a modernized, high-performance community fork of [VLC for Android](https://code.videolan.org/videolan/vlc-android). It focuses on extreme UI minimalism, zero frame drops, maximum battery life, and modern Android ergonomics.
> 
> Detailed history of fork improvements: [CHANGELOG-REBORN.md](CHANGELOG-REBORN.md).

---

## 📥 Download & Install

Download the latest ready-to-install Android APK directly from our GitHub Releases:

👉 **[Download Latest VLC Android Reborn APK](https://github.com/anntr1k3/vlc-android-reborn/releases)**

The installable package is `org.videolan.vlc.reborn`, so it can be installed next to official VLC. It will not update an existing `org.videolan.vlc` install.

---

## ⚡ Key Improvements in Reborn

### 🖤 1. Ultra-Minimalist UI & True AMOLED Black
* **Pure Pitch Black (`#000000`) Surfaces**: Baseline dark theme turns off OLED/AMOLED pixels entirely, delivering infinite contrast, eliminating dark-room light bleed, and significantly saving battery.
* **Zero Visual Clutter**: Removed legacy skeuomorphic gradients and banding overlays (`deep_space_gradient`, `top_gradient`, `bottom_gradient`) in favor of clean, matte Material 3 surfaces.
* **Modern Typography**: Clear, legible `sans-serif-medium` typography across video player HUD, timer overlays, track titles, equalizer bands, and home screen widgets.

### 🚀 2. Peak Performance & 120 FPS Scrolling
* **Zero-Relayout Mini-Player Gestures**: Bottom-sheet sliding animations run on GPU transforms (`scaleY`/`alpha`) during the gesture, then hide the 4dp progress bar with `View.GONE` when fully expanded so the header is not pinned to empty space.
* **Stale Job Cancellation in Lists (`ImageLoader`)**: Fast flinging through large media libraries instantly cancels background decode jobs for off-screen items, preventing CPU spikes and memory bloat.
* **Instant Search with Debounce**: 150ms keystroke debounce prevents SQLite lock contention during search queries.
* **Preallocated Batch Loading**: Pre-allocates exact list capacities in `MedialibraryProvider`, avoiding garbage collection pauses during large queue initialization (10k+ tracks).

### 🔋 3. Battery Efficiency & Smart Engine
* **Adaptive Sleep Timer**: Replaced legacy 1000ms periodic CPU wakeups with an adaptive delay (up to 30 s while far from expiry, remaining time on the last tick).
* **Release Coroutine Optimization**: Stripped debug stack-trace overhead from coroutine suspension points in release builds.

### 👆 4. Ergonomics & Frictionless Control
* **Streamlined Settings**: Reorganized settings into 4 clean Material 3 categories (Playback & Interface, Media Library & Storage, Casting & Security, Advanced), removing duplicate and fragmented options.
* **Modern Home Screen Widgets**: Redesigned 4x1 and 4x2 widgets with clean typography and layout.

---

## 🛠️ Building from Source

### Prerequisites
* JDK 17
* Android SDK (API 34 / Build Tools 34.0.0)

### Clone & Build
```bash
# Clone the repository
git clone https://github.com/anntr1k3/vlc-android-reborn.git
cd vlc-android-reborn

# Run unit tests
./gradlew testDebugUnitTest

# Build installable Debug APK
./gradlew :application:app:assembleDebug
```

The compiled APK will be located at:
`application/app/build/outputs/apk/debug/VLC-Android-3.7.1-debug-all.apk`

---

## 📂 Project Structure

* `application/app` — Application entry point, launcher, and packaging.
* `application/vlc-android` — Main UI, fragments, viewmodels, video & audio player activities.
* `application/medialibrary` — SQLite database & media library integration layer.
* `application/resources` — Shared styles, themes, drawables, and strings.
* `application/tools` — Core utilities, coroutine dispatchers, and helpers.
* `application/television` — Android TV leanback interface.

---

## 📄 License

VLC for Android is licensed under [GPLv2 (or later)](COPYING) / GPLv3.  
VLC engine (*LibVLC*) for Android is licensed under [LGPLv2.1](libvlc/COPYING.LIB).
