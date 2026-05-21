# FocusFlow+

FocusFlow+ is an Android productivity and wellness assistant built with **Kotlin**, **Jetpack Compose**, **Room**, and the **Gemini API**. The app helps a user turn a vague task into a realistic focus session by considering three inputs:

- the task they want to complete,
- their current energy level,
- and the amount of time they have available.

Gemini generates a structured plan with focus time, break time, session structure, health tips, and a motivational message. The app then saves the session locally and guides the user through a timer, summary, and history flow.

## Why this project is interesting

Many productivity apps only provide a fixed Pomodoro timer. FocusFlow+ adds an AI layer that adapts the session to the user's context. A tired user gets a gentler plan, while a high-energy user can receive a longer deep-work block. This makes the application more personal, more flexible, and easier to present as an AI-assisted mobile experience.

## Core features

- Gemini-powered focus-session planning.
- Task, energy, and available-time inputs.
- Focus and break timer.
- Local session persistence with Room.
- Completion summary.
- History dashboard with total completed sessions, today's sessions, and total focus minutes.
- Clean Jetpack Compose UI with reusable UI components.
- API-key loading from root `local.properties`.

## App flow

```text
Planner screen
   -> Gemini recommendation
   -> Saved Room session
   -> Focus / break timer
   -> Completion summary
   -> Session history
```

## Architecture

```text
app/src/main/java/com/example/focusflowplus
├── data
│   ├── local        # Room database, DAO, entity
│   ├── remote       # Gemini API Retrofit models/service
│   └── repository   # AI and session repositories
├── domain/model     # App domain models
├── navigation       # Compose navigation graph
└── ui
    ├── components   # Shared Compose UI components
    ├── input        # Planner screen + ViewModel
    ├── session      # Timer screen + ViewModel
    ├── summary      # Completion summary
    ├── history      # Local history dashboard
    └── theme        # Material 3 theme
```

## Tech stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM-style ViewModels + repositories |
| Local database | Room |
| Network | Retrofit + OkHttp |
| AI provider | Gemini API |
| Build system | Gradle Kotlin DSL |

## Gemini setup

Create or edit this file in the root project folder:

```text
FocusFlowPlus-main/local.properties
```

Add your Gemini key:

```properties
sdk.dir=C\:\\Users\\BLADE\\AppData\\Local\\Android\\Sdk

GEMINI_API_KEY= X
GEMINI_MODEL=gemini-2.5-flash
```

If Android Studio already created `local.properties`, keep the existing `sdk.dir=...` line and add the two Gemini lines below it.

Do **not** put the key inside `app/local.properties`. It must be beside `settings.gradle.kts`.

## How to run

1. Open the project root folder in Android Studio: `FocusFlowPlus-main`.
2. Let Gradle sync.
3. Add your Gemini API key to root `local.properties`.
4. Run **File -> Sync Project with Gradle Files**.
5. Run **Build -> Clean Project**.
6. Run **Build -> Rebuild Project**.
7. Launch the app on an emulator or Android device.


## Important security note

This implementation is suitable for a school project, prototype, or local demo. For a real public application, do **not** call Gemini directly from Android, because APKs can be decompiled and API keys can be extracted.

A safer production architecture is:

```text
Android app -> your backend -> Gemini API
```

The backend should store the API key, enforce rate limits, validate requests, and protect user data.

## Current limitations

- The app stores sessions locally only; there is no cloud account or sync.
- The Gemini key is loaded into the Android build for prototype use.
- There is no notification when a timer phase ends.
- There is no feedback form after a completed session.
- History has basic statistics only.

## Suggested future improvements

- Add notifications or sound/vibration when focus and break phases end.
- Add a post-session feedback screen: mood, difficulty, productivity score.
- Use feedback history to personalize future Gemini prompts.
- Add weekly charts for focus minutes and completion consistency.
- Add user profiles and cloud sync.
- Move Gemini calls to a backend for production security.
- Add offline fallback plans when the Gemini API is unavailable.

## Troubleshooting Gemini

- Make sure `local.properties` is in the project root, not inside `app/`.
- Make sure the key line is exactly `GEMINI_API_KEY=...` with no quotes and no spaces around `=`.
- Rebuild the app after changing the key.
- Uninstall the old app from the emulator and run again if the old build is cached.
- If you shared your key anywhere, revoke it in Google AI Studio and create a new one.
