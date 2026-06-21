# Nova Shell — Custom Android Launcher

A native Android launcher app built with Kotlin and Jetpack Compose. Nova Shell replaces your device home screen with a custom, deeply themeable experience — with an integrated AI assistant coming in later phases.

## Phase 1 — Minimal Working Launcher

This initial version:
- Registers as a HOME app (appears in the launcher chooser)
- Lists all installed apps alphabetically using the `LauncherApps` API
- Tapping an app launches it
- Pressing Home returns to Nova Shell
- Back button is consumed (standard launcher behavior)

## Building

1. Open the project in Android Studio (Hedgehog or newer recommended).
2. Let Gradle sync complete.
3. Connect a device or start an emulator (API 26+).
4. Run the `app` configuration.

## Setting as Default Launcher

### On the emulator / device:
1. Press the Home button after installing.
2. Android will show a picker — select **Nova Shell** and choose **Always**.

### If you already have a default launcher set:
1. Go to **Settings → Apps → Default Apps → Home app**.
2. Select **Nova Shell**.

### To revert:
1. Go to **Settings → Apps → Default Apps → Home app**.
2. Select your previous launcher (e.g., Pixel Launcher, One UI Home).

## Project Structure

```
app/src/main/java/com/novashell/launcher/
├── MainActivity.kt          — Entry point, edge-to-edge, Compose host
├── AppInfo.kt                — Data class for installed app metadata
├── AppsRepository.kt         — Queries installed apps via LauncherApps API
├── LauncherViewModel.kt      — MVVM ViewModel exposing app list as StateFlow
└── ui/
    ├── HomeScreen.kt         — Main composable: app list with tap-to-launch
    └── theme/
        └── Theme.kt          — Material 3 theme with dynamic color support
```

## Security Note

The Gemini API key (needed in later phases) must be stored in `local.properties` and referenced via `BuildConfig`. Never commit API keys to version control.

## Tech Stack

- Kotlin, Jetpack Compose (Material 3)
- MVVM with ViewModel + StateFlow
- LauncherApps API for correct app enumeration
- Room + DataStore (dependencies included, used in later phases)
- Min SDK 26 (Android 8.0), Target SDK 35
