# Aura OS — Custom Android Launcher with AI Assistant

A native Android launcher with an integrated AI assistant (**Aura**) powered by Google Gemini. Built with Kotlin and Jetpack Compose.

## Features

### Launcher
- Registers as HOME app (replaces default launcher)
- Lists installed apps via `LauncherApps` API
- Edge-to-edge, Material You dynamic theming

### Aura AI Assistant
- **Text chat** — full conversational AI powered by Gemini 2.0 Flash
- **Voice input** — tap the mic to speak, with animated waveform visualizer
- **Wake word** — say "Hey Aura" to activate hands-free (toggle on/off)
- **App launching** — "Open Instagram", "Find calculator"
- **Web search** — "Search for weather today"
- **Device info** — "What's my battery level?", "How much storage do I have?"
- **Reminders** — "Remind me to call mom in 30 minutes"
- **System settings** — "Turn on flashlight", "Open WiFi settings"
- **Calls & SMS** — "Call Mom", "Text John I'll be late" (with confirmation)
- **Theme control** — "Switch to dark mode"
- **Long-press Home** — opens Aura (requires Accessibility Service)
- **Floating button** — draggable sparkle button, always accessible
- **Typing animation** — responses appear character by character
- **Action confirmations** — calls/SMS require explicit user confirmation before executing

## Setup

1. Open in Android Studio (Hedgehog+), let Gradle sync.
2. Create `local.properties` in the project root (if not present) and add:
   ```
   GEMINI_API_KEY=your_gemini_api_key_here
   ```
3. Build and run on a device/emulator (API 26+).
4. Set as default launcher: press Home → select Aura OS → Always.

## Security

- **API key** is read from `local.properties` via `BuildConfig`. Never committed to version control.
- **No backend, no login, no telemetry, no ads.** Only network call is to the Gemini API.
- **All data stored locally** (Room database + DataStore).
- **Calls/SMS always require confirmation** — never auto-executed.
- **Wake word is on-device only** — uses Android SpeechRecognizer, no cloud voice service.

## Architecture

```
com.auraos.launcher/
├── MainActivity.kt              — Entry point, permissions, wake word receiver
├── AuraApplication.kt           — Notification channels
├── AuraViewModel.kt             — AI assistant state management
├── LauncherViewModel.kt         — App list state
├── AppInfo.kt / AppsRepository.kt — Installed apps via LauncherApps
├── ai/
│   ├── GeminiApiClient.kt       — Gemini API HTTP calls
│   ├── IntentResponse.kt        — Structured intent data model
│   ├── ConversationMemory.kt    — Session context (10 turns)
│   └── ActionExecutor.kt        — Executes parsed intents locally
├── voice/
│   ├── VoiceRecognitionManager.kt — SpeechRecognizer wrapper
│   └── WakeWordService.kt       — Foreground service for "Hey Aura"
├── data/
│   ├── db/AuraDatabase.kt       — Room DB (reminders, conversations)
│   └── ReminderReceiver.kt      — AlarmManager broadcast handler
├── accessibility/
│   └── AuraAccessibilityService.kt — Long-press Home detection
└── ui/
    ├── HomeScreen.kt             — Main launcher + Aura overlay
    ├── theme/Theme.kt            — Material 3 + dynamic color
    └── assistant/
        ├── AuraPanel.kt          — Full chat UI (messages, input, header)
        ├── VoiceVisualizer.kt    — Animated orb + waveform bars
        └── FloatingAuraButton.kt — Draggable sparkle FAB
```

## Tech Stack

- Kotlin, Jetpack Compose (Material 3)
- MVVM — ViewModel + StateFlow
- Gemini 2.0 Flash API (structured JSON intents)
- Android SpeechRecognizer (voice + wake word)
- Room + DataStore (local persistence)
- OkHttp (API calls)
- Min SDK 26, Target SDK 35
