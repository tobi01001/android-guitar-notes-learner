# Android Guitar Notes Learner

A Kotlin + Jetpack Compose app to help learn and memorize the notes on the guitar neck.

Goals
- Kotlin, Jetpack Compose, single-activity app.
- Minimum API level 34 (Android 14+) — no backward compatibility required initially.
- Material 3 Compose theming.
- Practice sessions that randomly ask for notes from a selected string/fret/scale/mode and check played audio input.
- Built-in guitar tuner (standard tuning to start).

Quick start (local)
1. Open project in Android Studio (use an Android Studio version that supports the Android Gradle Plugin used in the build files).
2. Build and run on a device (microphone required for audio features).

Planned initial features (M1)
- App skeleton with Compose navigation (Home / Practice / Settings).
- Practice configuration UI (string selection, fret range, mode, duration).
- Random note generator for practice sessions.
- Permission handling for RECORD_AUDIO; placeholder for tuner / pitch detection.

Repository details
- Package name: com.androidguitarnotes.app
- Build system: Gradle Kotlin DSL (build.gradle.kts)
- License: MIT

Next steps
- I will create the private repository tobi01001/android-guitar-notes-learner and push this skeleton directly to main once you accept the GitHub authorization prompt.
- After pushing, I will create issues for the CI task and break the practice/tuner work into issues.