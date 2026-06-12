# AI Caller

An Android dialer/caller app (Kotlin + Jetpack Compose) with built-in AI features powered by the
Google Gemini API, with on-device fallbacks so the app keeps working offline.

## Features

- **Dialer & call log** - standard dial pad, call placement, and a synced call history view.
- **AI call screening & spam detection** - a `CallScreeningService` analyzes incoming numbers using
  local heuristics plus an optional AI risk assessment, then silences or blocks high-risk calls.
  Users can report numbers as spam from any call.
- **Live transcription & call summaries** - an on-device `SpeechRecognizer` transcribes calls in
  real time via a foreground service; after the call, Gemini generates a summary, action items,
  and sentiment score.
- **Smart contacts & insights** - call summaries roll up into per-contact AI insights: suggested
  tags, relationship type, running sentiment average, and last-call summary.
- **Voice assistant & smart auto-reply** - a voice command screen interprets natural-language
  commands ("Call mom", "Block this number", "Read my last call summary", "Search for the dentist")
  and missed calls can trigger an AI-drafted SMS auto-reply.

## Project structure

```
app/src/main/java/com/aicaller/app/
├── ai/                # AiClient interface, Gemini client, local heuristic fallback
├── data/
│   ├── local/         # Room database, entities, DAOs
│   └── repository/    # Contact, Call, Spam and AutoReply repositories
├── di/                 # Hilt modules (database, networking, AI bindings)
├── service/            # CallScreeningService, phone-state receivers, transcription service
├── worker/             # WorkManager job that generates AI call summaries
├── ui/                 # Compose screens (dialer, recents, contacts, assistant, settings, spam)
└── util/               # Encrypted prefs, phone number helpers
```

## Setup

1. Open the project in Android Studio (Kotlin 1.9.24, AGP 8.4.1, JDK 17).
2. Build & run on a device or emulator (minSdk 26).
3. Grant the requested runtime permissions (phone, contacts, microphone, call log, SMS).
4. In **Settings**:
   - Optionally add a **Google Gemini API key** (from [Google AI Studio](https://aistudio.google.com/app/apikey))
     to enable Gemini-powered summaries, spam analysis, auto-replies, and voice commands. Without a
     key, the app uses on-device heuristics for every feature.
   - Tap **Set as call screening app** so the AI spam-detection service is active.
   - Toggle individual features (spam screening, auto-block, transcription, smart auto-reply).

## Notes

- All AI calls go through `AiClient` (`ai/AiClient.kt`). The Gemini-backed implementation
  (`GeminiAiClient`) automatically falls back to `LocalHeuristicAiClient` if no API key is set
  or a request fails, so the app is fully usable offline.
- The Gemini API key is stored using `EncryptedSharedPreferences`.
- Call recordings/transcription require the user's consent and may be subject to local call
  recording laws - review applicable regulations before enabling in production.
