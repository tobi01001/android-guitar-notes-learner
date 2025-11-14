# Android Guitar Notes Learner

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Android CI](https://github.com/tobi01001/android-guitar-notes-learner/actions/workflows/ci.yml/badge.svg)](https://github.com/tobi01001/android-guitar-notes-learner/actions/workflows/ci.yml)

A Kotlin + Jetpack Compose app to help learn and memorize the notes on the guitar neck.

## Goals
- Kotlin, Jetpack Compose, single-activity app.
- Minimum API level 34 (Android 14+) — no backward compatibility required initially.
- Material 3 Compose theming.
- Practice sessions that randomly ask for notes from a selected string/fret/scale/mode and check played audio input.
- Built-in guitar tuner (standard tuning to start).
- Real-time pitch detection with configurable sensitivity and auto-adjust capabilities.

## Quick Start

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

## Documentation

### For Contributors
- **[CONTRIBUTING.md](CONTRIBUTING.md)** - How to contribute to this project
- **[CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md)** - Community guidelines and standards

### Technical Documentation
- **[Audio Detection Analysis](docs/technical/AUDIO_DETECTION_ANALYSIS.md)** - Comprehensive analysis of audio processing and pitch detection
- **[Audio Library Research](docs/technical/AUDIO_LIBRARY_RESEARCH.md)** - Research on audio processing libraries and techniques

### Development Documentation
- **[Future Enhancements](docs/development/FUTURE_ENHANCEMENTS.md)** - Planned features and improvements
  - [ENH-001: Multi-Frame Pitch Confirmation](docs/development/ENHANCEMENT_001_MULTI_FRAME_CONFIRMATION.md)
  - [ENH-002: Harmonic Consistency Checks](docs/development/ENHANCEMENT_002_HARMONIC_CONSISTENCY.md)
- **[Practice Configuration](docs/development/PRACTICE_CONFIG_README.md)** - Practice session configuration details
- **[Practice Session](docs/development/PRACTICE_SESSION_README.md)** - Practice session implementation details

## Project Information

- **Package name**: `com.androidguitarnotes.app`
- **Build system**: Gradle Kotlin DSL (build.gradle.kts)
- **License**: MIT
- **Min SDK**: 34 (Android 14+)
- **Target SDK**: 34

## Contributing

We welcome contributions! Please read our [Contributing Guidelines](CONTRIBUTING.md) and [Code of Conduct](CODE_OF_CONDUCT.md) before submitting pull requests.