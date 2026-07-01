# Changelog — vlc-android-reborn

This file tracks changes made in this fork, separate from upstream VideoLAN history.
For upstream changes, see [NEWS](NEWS) and the upstream repository
(`https://code.videolan.org/videolan/vlc-android`).

## Conventions

- Fork-specific work is done on the `ai-optimization` branch and merged into `main`.
- `main` mirrors a known-good state; do not commit experiments directly to it.
- Upstream base of the initial snapshot: tag `upstream-base-20260615`
  (approx. upstream commit `6ef0bfdcb`, 2026-06-15).

## Unreleased

### Phase 1 — Code modernization (safe deprecation cleanup)
First, low-risk pass over deprecated APIs (build stays green):
- Centralized screen-size helpers (`getScreenWidth`/`getScreenHeight`) on AndroidX
  `WindowMetricsCalculator` instead of the deprecated `defaultDisplay.getMetrics`;
  `FrameRateManager` now uses `activity.display` on API 30+ with a fallback.
- Replaced deprecated `Locale(String)` / `Locale(lang, country)` constructors with a
  lenient `buildLocale()` helper (`Locale.Builder` + legacy fallback for malformed ids).
- Wrapped `overridePendingTransition` in `overrideOpen/CloseTransitionCompat` extensions
  that use `Activity.overrideActivityTransition` on API 34+ (behaviour unchanged below 34).
- Replaced `android.app.ProgressDialog` in MediaUtils with an AppCompat AlertDialog
  + indeterminate ProgressBar (completes the safe cluster).

### Phase 1 — Medium migration (Activity Result API, safe sites only)
Migrated isolated, non-permission `startActivityForResult`/`onActivityResult` pairs to
`registerForActivityResult` (no behaviour change; build green):
- PreferencesActivity: settings search result.
- PreferencesAudio: soundfont file picker.
- PreferencesAdvanced: restore-settings file picker.
Deferred (need on-device validation): permission/OTG/SAF flows, restart-propagating
results, and the MenuProvider migration (entangled with the fragment inheritance hierarchy).

### Phase 1 — Medium migration (lifecycle-aware flow collection)
Modernized the custom `Flow<T>.launchWhenStarted` helper (Kextensions) from the deprecated
`LifecycleCoroutineScope.launchWhenStarted` to `LifecycleOwner.repeatOnLifecycle(STARTED)`,
and updated its 9 call sites to pass the owning `this@Fragment`/`this@Activity` (preserves the
original lifecycle binding). Verified on an emulator (Android 14): app launches and all bottom-nav
tabs — Video/Audio/Browse/Playlists/More — navigate without crashes.
Still deferred: the 27 direct one-shot `lifecycleScope.launchWhenStarted { … }` calls (suspend
bodies; benign deprecation, need per-site judgement) and MenuProvider.


### Project setup
- Initialized fork repository, published to `anntr1k3/vlc-android-reborn` (private).
- Added `upstream` remote pointing to VideoLAN GitLab (push disabled).
- Added README fork notice and this changelog.

### Phase 0 — Build stabilization
The snapshot inherited from the previous session did not compile. Fixed so that
`./gradlew :application:app:assembleDebug` produces a working APK (uses prebuilt
libVLC/medialibrary Maven AARs; native engine not required):
- Restored Gradle wrapper (9.3.1); guarded native module includes by directory
  presence so the app layer builds without the C/C++ engine.
- Bumped vlc3 `minSdk` 17 → 21 (material 1.12.0 requires ≥19; legacy floor dropped).
- Added `androidx.room:room-ktx` for coroutine/suspend DAO support on Room 2.6.x.
- Fixed `onRequestPermissionsResult` signature (`Array<out String>`) broken by the
  previous androidx version bump (MainActivity, BaseActivity, OnboardingActivity).
- Fixed liveplotgraph R/BuildConfig imports after its de-modularization.
- Removed dead remote-access references left by the previous session
  (`ic_remote_access_big`, `REMOTE_ACCESS_ONBOARDING`, `preferences_remote_access`).

### Removed: moviepedia (movie/TV metadata scraper)
Fully removed the moviepedia feature (the previous report claimed this was done,
but it was only partially de-modularized and left non-compiling):
- Deleted moviepedia source tree, layouts, app delegates and Room database.
- Removed the "Find metadata" context action and `MOVIEPEDIA_*` constants.
- Removed the metadata-enriched Android TV surface: home "recently played/added"
  rows, scraping browse/details/search, and all dedicated TV presenters/activities.
  Android TV still works but without scraped metadata (titles/posters/cast).
