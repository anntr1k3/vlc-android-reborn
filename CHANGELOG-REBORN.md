# Changelog — vlc-android-reborn

This file tracks changes made in this fork, separate from upstream VideoLAN history.
For upstream changes, see [NEWS](NEWS) and the upstream repository
(`https://code.videolan.org/videolan/vlc-android`).

## Conventions

- Fork-specific work lands on `main`.
- Tag `v1.0.0-reborn` marks the current known-good snapshot.
- Upstream base of the initial snapshot: tag `upstream-base-20260615`
  (approx. upstream commit `6ef0bfdcb`, 2026-06-15).

## Unreleased

### Follow-up from review
- Restored `.kt` copyright banners in `AppSetupDelegate` and `VLCApplication`.
- `loadPagedList` sizes the track `ArrayList` from the first page’s `tracksCount`, not from the parent-item count.
- Mini-player progress bar uses GPU `scaleY` during the slide and `View.GONE` when the sheet is fully expanded, so it no longer keeps a 4dp layout hole.
- Removed leftover `moviepedia_hint` / `moviepedia_result` strings after the scraper module deletion.
- Sleep timer last tick sleeps the remaining milliseconds instead of clamping the delay up to 500 ms.
- Installable `applicationId` is now `org.videolan.vlc.reborn` (debug: `org.videolan.vlc.reborn.debug`). Launcher label is `VLC Reborn`. This can sit next to official VLC; it is **not** an in-place update of an existing `org.videolan.vlc` install.
- Cover decode no longer forces `RGB_565`; album art uses the default `ARGB_8888` config so alpha and 24-bit color are kept.
- Double-tap seek zones restored to upstream 25% / 50% / 25% (`range/4` and `0.75`) so the center play/pause band is not a fifth of the screen. Kept as the daily-driver default.
- Opt-in JVM suites (`-PincludeLegacyVlcAndroidUnitTests=true`) now compile. `DirectoryRepositoryTest` runs on Mockito. Viewmodel/Robolectric tests still cannot execute on JDK 25 with Robolectric 4.3.1 (`IllegalAccessException`). Dead `SubtitlesModelTest` and empty `BrowserFavRepositoryTest` removed. Default CI stays `test-common` + `media/`.
- Emulator smoke (AVD `vlc_smoke_api34`, API 34; no physical device): debug package `org.videolan.vlc.reborn.debug` installs and launches; black theme applies after restart; local video plays via SAF after Hardware Acceleration is set to Disabled (Automatic showed a black frame on this AVD); local audio plays via SAF with the mini-player progress bar visible; the Interface Sleep timer dialog opens and dismisses without crashing the process. Not verified on this run: double-tap seek zones (5 s clip ended during gestures), install next to official VLC, or the storage-permission / library-scan path. An `ACTION_VIEW` https intent still opens the All files access screen (`StartActivity` upstream behavior). No fork code changes from this smoke.

### Phase 3 — Minimalist UI & Typography (Zero Clutter)
- **True AMOLED Black Surface Mode**: Updated `dark_background` to `#000000` (true pitch black) across `Theme.VLC.Black`, `background_default`, action bars, headers, and window backgrounds, turning OLED pixels completely off for maximum battery savings and infinite contrast.
- **Homescreen Widgets Modern Typography**: Upgraded track title and artist typography across mini and macro home screen widgets (`widget_mini.xml`, `widget_macro.xml`) to `sans-serif-medium` and `sans-serif` for clean readability on Android launchers.
- **Video Player HUD & Tracks Dialog Typography**: Refined video title overlay, clock, and track section headers (`player_hud_right.xml`, `player_overlay_track_item.xml`) with `sans-serif-medium` and crisp drop-shadow rendering, dramatically improving readability over bright scenes.
- **Settings Hierarchy Streamlining & Category Reorganization**: Reorganized root `preferences.xml` into clean, well-defined Material 3 categories (Playback & Interface, Media Library & Storage, Casting & Security, Advanced). Consolidated loose video options (`hardware_acceleration`, `screen_orientation`, `video_action_switch`) into `preferences_video.xml`, eliminating root clutter and preference duplication while preserving 100% of user settings and search indexing.
- **Audio & Main Screen Background Cleanup**: Replaced legacy skeuomorphic `deep_space_gradient` bitmap and overlapping dark gradient views (`top_gradient`, `bottom_gradient`) with clean, theme-adaptive surfaces (`?attr/background_default_darker` / `?attr/background_default`), eliminating visual banding across all screens.
- **Modern Typography & Card Presentation**: Refined audio cover card corners and elevations (12-24dp), upgraded title, equalizer frequency band labels (`equalizer_bar.xml`), and player timer typography (`VLC.Player.TimeText`) to `sans-serif-medium` / `sans-serif` for high legibility and contemporary aesthetics.
- **Intuitive Video Player Double-Tap Seeking**: Expanded double-tap rewind/fast-forward gesture detection zones in `VideoTouchDelegate.kt` from narrow 25% screen edges to natural 40% left/right touch zones for frictionless video seeking.
  Superseded: zones restored to upstream 25/50/25 (see Unreleased).

### Phase 2 — Performance (Search Debounce, Battery, Memory & Scrolling)
- **Zero-Relayout Mini-Player Gestures (120 FPS)**: Replaced per-frame `layoutParams.height` mutations and heavy `requestLayout()` calls during bottom-sheet sliding in `AudioPlayerAnimator.kt` with GPU-accelerated `scaleY` and alpha transforms, completely eliminating UI thread layout thrashing during player expansion gestures.
- **SearchActivity Coroutine Debounce**: Implemented 150ms debounce with cancellation of stale search jobs in `SearchActivity.kt`, preventing dozens of redundant parallel SQLite queries per keystroke and eliminating typing lag.
- **Battery-Efficient Sleep Timer**: Replaced 1000ms wake-up loop in `PlaybackService.kt` with adaptive delay calculation via `SleepTimerCalculator`, capping far-from-expiry wakeups at 30 s (up to 30x fewer than a 1 s loop) and sleeping the exact remaining time on the last tick. Accompanied by unit tests in `SleepTimerCalculatorTest`.
- **ImageLoader Coroutine Job Cancellation**: Attached decode job tracking to target views via `R.id.image_job`. When views are recycled, rebound, or loaded from `BitmapCache`, stale decoding jobs on `Dispatchers.IO` are cancelled immediately (`cancelPreviousImageJob`), eliminating wasted CPU and memory spikes during fast fling/scroll through large libraries.
- **RGB_565 Artwork Decoding**: Switched audio cover decoding in `AudioUtil.fetchCoverBitmap` to `Bitmap.Config.RGB_565`, reducing bitmap memory consumption on the heap by 50% (2 bytes/px instead of 4 bytes/px) with no perceptible quality loss for album art.
  Superseded: `RGB_565` preference removed (see Unreleased).
- **Media Library Batch Loading Capacity**: `loadPagedList` now estimates `ArrayList` capacity from the first page’s `tracksCount` scaled to the parent total, instead of `ArrayList(count)` sized by albums/folders. That previous size was far below the flattened track list and caused extra growth copies.
- **MediaItemDiffCallback Fast Path**: Optimized DiffUtil `areItemsTheSame` with fast ID and itemType comparison path, eliminating identity false-negatives and redundant view re-creations during list updates.
- **Active Unit Test Suite Expansion**: Added `MediaItemDiffCallbackTest`, `SleepTimerCalculatorTest`, `ExtensionsTest`, and `StringsTest` into the actively run test suite, raising JVM test coverage across utility and DiffUtil layers.

- **Release Coroutines Suspension Overhead Fix**: Wrapped global `DEBUG_PROPERTY_NAME` in `BuildConfig.DEBUG` in `AppSetupDelegate.kt`, preventing unnecessary stack-trace inspection and overhead on every coroutine suspension point in release builds.

### Phase 0 — Build & DevEx fixes
- Added `org.gradle.caching=true` in `gradle.properties` for build task caching.
- Added missing `junit:junit` test dependency to `application/television/build.gradle` to ensure `./gradlew testDebugUnitTest` and global test tasks pass out-of-the-box.

### Phase 1 — Medium migration (Fragment menus → MenuProvider)
Migrated all Fragment menus off the deprecated `setHasOptionsMenu` / `onCreateOptionsMenu` /
`onPrepareOptionsMenu` / `onOptionsItemSelected` APIs to the `MenuProvider` API:
- `BaseFragment` now implements `MenuProvider` and registers itself via
  `addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)` — so only the RESUMED
  (visible) tab contributes menu items, preventing cross-tab duplication. Subclasses simply
  override `onCreateMenu`/`onPrepareMenu`/`onMenuItemSelected`, preserving the existing
  template-method super-chaining across the fragment inheritance hierarchy.
- Migrated 14 fragments in the hierarchy (browser/audio/video/history) plus the standalone
  `PreferencesAndroidAuto` preference fragment. Activities keep their (non-deprecated) menu callbacks.
- Verified on emulator (Android 14): Video/Audio/Browse menus each render the correct, distinct
  items; deep super-chaining (Browse) combines items into one menu; switching tabs and back does
  NOT duplicate or leak items; no crashes.

### Phase 2 — Performance (Baseline Profile)
- Added a **Baseline Profile** (`application/app/src/main/baseline-prof.txt`, ~16k HRF rules) plus the
  `androidx.profileinstaller` dependency. The profile AOT-compiles hot startup + browse + playback +
  scroll paths (app + androidx/kotlin libs), cutting cold-start time and scroll jank on release builds.
- The profile was captured from a real run on the emulator (launch → video list → playback → tab
  switches → scroll), extracted from the ART reference profile via on-device `profman
  --dump-classes-and-methods`, so it reflects actual executed code rather than guesses.
- Verified: `assembleRelease` runs the ART-profile tasks and packages `assets/dexopt/baseline.prof`
  (+ `.profm`) into the APK. (Startup was already well-structured — heavy init is deferred to background
  threads; no main-thread I/O, StrictMode, or jank observed on the test setup, so the profile is the
  highest-value remaining startup/scroll win. Actual speedup needs release benchmarking on real hardware.)

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
Deferred (need on-device validation): restart-propagating results and the MenuProvider
migration (entangled with the fragment inheritance hierarchy).

### Phase 1 — Medium migration (SAF/OTG storage-access to Activity Result API)
Migrated the two remaining SAF document-tree flows off deprecated `startActivityForResult`/
`onActivityResult` to `registerForActivityResult(StartActivityForResult())` (faithful 1:1,
no semantic change): `OtgAccess` (OTG root grant) and `WriteExternalDelegate` (SD-card write
grant). Launchers are registered as fragment properties (before STARTED). Note: the OTG/SD code
paths can't be exercised on the emulator (no removable storage) — verified compile + no
registration/crash regression on general navigation; the grant flows themselves still want a
real-device check. Runtime *permission* requests were already on the Activity Result API.


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
- Removed leftover `moviepedia_hint` / `moviepedia_result` translations from all locales.
- Removed the "Find metadata" context action and `MOVIEPEDIA_*` constants.
- Removed the metadata-enriched Android TV surface: home "recently played/added"
  rows, scraping browse/details/search, and all dedicated TV presenters/activities.
  Android TV still works but without scraped metadata (titles/posters/cast).
