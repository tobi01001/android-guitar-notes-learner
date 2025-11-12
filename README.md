# Android Guitar Notes Learner

A Kotlin + Jetpack Compose app to help learn and memorize the notes on the guitar neck.

Goals
- Kotlin, Jetpack Compose, single-activity app.
- Minimum API level 34 (Android 14+) — no backward compatibility required initially.
- Material 3 Compose theming.
- Practice sessions that randomly ask for notes from a selected string/fret/scale/mode and check played audio input.
- Built-in guitar tuner (standard tuning to start).
- Real-time pitch detection with configurable sensitivity and auto-adjust capabilities.

Quick start (local)
1. Open project in Android Studio (use an Android Studio version that supports the Android Gradle Plugin used in the build files).
2. Build and run on a device (microphone required for audio features).

## Features

### Audio Detection & Sensitivity
The app includes real-time pitch detection for guitar notes with advanced audio processing capabilities:

- **Manual Sensitivity Control**: Adjustable microphone sensitivity (0.5x to 2.0x) to accommodate different guitars, pickups, and environments
- **Auto-Adjust Sensitivity**: Automatically adapts the sensitivity multiplier based on incoming signal levels
  - Analyzes RMS (Root Mean Square) level over a rolling window (~1 second)
  - Dynamically adjusts gain to maintain optimal signal levels for pitch detection (range: 0.5x to 2.0x)
  - Uses rolling average for smooth transitions without abrupt jumps
  - Works in conjunction with manual sensitivity: `finalSensitivity = baseSensitivity × autoAdjustFactor`
- **Audio Source Selection**: Choose between Auto, Unprocessed, Voice Recognition, or Microphone input sources
- **Real-time Audio Level Visualization**: See your input levels in the settings screen

These features ensure accurate pitch detection across various devices and playing conditions.
## Audio Processing

### High-Pass Filter

The app automatically applies a 60 Hz high-pass filter to all incoming audio. This removes:
- Low-frequency handling noise (bumps, taps on the device)
- Environmental rumble (traffic, wind, HVAC systems)
- DC offset and subsonic content

The filter does not affect guitar notes (lowest note E2 is at 82 Hz) and significantly improves pitch detection accuracy by preventing false triggers from non-musical low-frequency noise.

**Technical Details:**
- Filter type: One-pole IIR high-pass
- Cutoff frequency: 60 Hz (configurable)
- Implementation: See `HighPassFilter.kt` and `AUDIO_DETECTION_ANALYSIS.md` Section 7.2.3

**Tuning the Filter:**
If you need to adjust the cutoff frequency for different instruments or environments, modify the `cutoffFrequency` parameter in `AudioRecorder.kt`:
```kotlin
val highPassFilter = HighPassFilter(sampleRate = SAMPLE_RATE, cutoffFrequency = 60.0)
```
- Lower (e.g., 50 Hz): More conservative filtering, allows more low-frequency content
- Higher (e.g., 70-80 Hz): More aggressive filtering for noisy environments (may affect lowest E string)

## Development

### Testing
Run unit tests with:
```bash
./gradlew test
```

### Code Formatting
This project uses [ktlint](https://github.com/pinterest/ktlint) for Kotlin code formatting.

Check code formatting:
```bash
./gradlew ktlintCheck
```

Auto-fix formatting issues:
```bash
./gradlew ktlintFormat
```

### Linting
Run Android Lint checks:
```bash
./gradlew lint
```

### CI/CD
The project includes a GitHub Actions workflow that automatically:
- Builds debug and release APKs
- Runs Android Lint checks
- Runs ktlint code formatting checks
- Runs all unit tests

All checks run on push to main and on pull requests.

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