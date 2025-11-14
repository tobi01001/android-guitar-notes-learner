# Practice Session Implementation

This document describes the implementation of the random note generator and practice session flow for the Guitar Notes Learner app.

## Overview

The practice session feature allows users to practice identifying and playing guitar notes based on their configured preferences. Notes are randomly selected from the user's chosen strings, frets, and note modes.

## Architecture

### Components

#### 1. Data Models

**PracticeNote.kt**
- Represents a single note on the guitar fretboard
- Properties:
  - `stringNumber` (1-6): Guitar string number
  - `fret` (0-24): Fret position
  - `noteName`: Musical note name (e.g., "E", "F#", "A")

**PracticeSessionState.kt**
- Sealed class representing different session states
- States:
  - `Ready`: Initial state before session starts
  - `Active`: Session in progress with current note and progress
  - `Paused`: Session paused with state preserved
  - `Completed`: Session finished with final statistics

#### 2. Business Logic

**RandomNoteGenerator.kt**
- Generates random notes based on `PracticeConfig`
- Features:
  - Respects user-selected strings and fret range
  - Supports three note modes:
    - `SEMITONES`: All chromatic notes including sharps
    - `WHOLE_NOTES`: Currently returns all chromatic notes (future: filter to natural notes)
    - `SCALE`: Currently returns all chromatic notes (future: support specific scales)
  - Uses standard tuning (E A D G B E)

**PracticeSessionViewModel.kt**
- Manages practice session state using MVVM pattern
- Key responsibilities:
  - Session lifecycle management (start, pause, resume, end)
  - Timer management for time-based sessions
  - Progress tracking for count-based sessions
  - Random note generation coordination
- Uses Kotlin coroutines for timer
- Exposes state via `StateFlow`

#### 3. User Interface

**PracticeSessionScreen.kt**
- Jetpack Compose UI with Material Design 3
- Four screen states:
  1. **Ready Screen**: Instructions and start button
  2. **Active Screen**: Current note display, progress, and controls
  3. **Paused Screen**: Pause information and resume option
  4. **Completed Screen**: Session statistics and finish button

### Screen Flow

```
┌─────────────┐
│   Config    │
│   Screen    │
└──────┬──────┘
       │ Start Practice
       ▼
┌─────────────┐
│    Ready    │
│   Screen    │
└──────┬──────┘
       │ Start
       ▼
┌─────────────┐      Pause      ┌─────────────┐
│   Active    │◄───────────────►│   Paused    │
│   Screen    │      Resume     │   Screen    │
└──────┬──────┘                 └──────┬──────┘
       │ Complete/End                  │ End
       ├───────────────────────────────┘
       ▼
┌─────────────┐
│  Completed  │
│   Screen    │
└──────┬──────┘
       │ Finish
       ▼
    (Back to Config)
```

## Features

### Session Types

**Time-Based Sessions**
- User sets duration in minutes (e.g., 5 minutes)
- Session completes when time expires
- Progress bar shows time remaining
- User can end session early

**Count-Based Sessions**
- User sets number of notes to practice (e.g., 20 notes)
- Session completes after playing all notes
- Progress bar shows notes completed
- User can end session early

### Note Generation

Notes are randomly selected based on:
- **Selected Strings**: User chooses which strings to practice (1-6)
- **Fret Range**: User sets minimum and maximum fret (0-24)
- **Note Mode**: Determines which notes can appear
  - Semitones: All 12 chromatic notes
  - Whole Notes: Currently same as semitones (future: natural notes only)
  - Scale: Currently same as semitones (future: notes from selected scale)

### Session Controls

- **Next Note**: Advances to a new random note, increments counter
- **Pause/Resume**: Pauses timer, preserves session state
- **End Session**: Completes session early, shows statistics

### Progress Tracking

The UI displays:
- **Notes Completed**: Count of notes practiced
- **Total Notes**: Target count (count-based sessions only)
- **Elapsed Time**: Time spent in session
- **Total Time**: Target duration (time-based sessions only)
- **Progress Bars**: Visual indicators for both notes and time

## UI Components

### Active Session Display

```
┌─────────────────────────────────┐
│  ▓▓▓▓▓▓░░░░░░  50%             │  Progress bars
│  5 / 10 notes                   │
│  2:30 / 5:00                    │
├─────────────────────────────────┤
│                                 │
│      Play this note:            │  Instructions
│                                 │
│  ┌───────────────────────────┐ │
│  │                           │ │
│  │          A                │ │  Large note display
│  │                           │ │
│  │    String 3, Fret 2       │ │  Position info
│  │                           │ │
│  └───────────────────────────┘ │
│                                 │
├─────────────────────────────────┤
│  ┌─────────────────────────┐   │
│  │     Next Note           │   │  Primary action
│  └─────────────────────────┘   │
│  ┌──────────┐  ┌──────────┐   │
│  │  Pause   │  │   End    │   │  Secondary actions
│  └──────────┘  └──────────┘   │
└─────────────────────────────────┘
```

## Code Example

### Starting a Practice Session

```kotlin
// In MainActivity navigation
val navController = rememberNavController()
var practiceConfig by remember { mutableStateOf<PracticeConfig?>(null) }

NavHost(navController, startDestination = "home") {
    // ... config screen sets practiceConfig
    
    composable("practiceSession") {
        val config = practiceConfig
        if (config != null) {
            PracticeSessionScreen(
                config = config,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
```

### Observing Session State

```kotlin
@Composable
fun PracticeSessionScreen(
    config: PracticeConfig,
    viewModel: PracticeSessionViewModel = remember { PracticeSessionViewModel(config) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    when (val currentState = state) {
        is PracticeSessionState.Active -> {
            // Display current note
            Text(currentState.currentNote.noteName)
            
            // Handle next note
            Button(onClick = { viewModel.nextNote() }) {
                Text("Next Note")
            }
        }
        // ... handle other states
    }
}
```

## Testing

### Unit Tests

The implementation includes comprehensive unit tests:

**RandomNoteGeneratorTest.kt** (7 tests)
- Note generation within string selection
- Note generation within fret range
- Different note modes
- Deterministic behavior with single options
- Variety across multiple generations

**PracticeConfigTest.kt** (6 tests)
- Default configuration values
- Configuration customization
- Copy functionality

**PracticeSessionStateTest.kt** (9 tests)
- State creation and properties
- State transitions
- Progress tracking
- Time-based and count-based sessions

### Manual Testing Checklist

- [ ] Start a time-based practice session
- [ ] Start a count-based practice session
- [ ] Verify random notes change on "Next Note"
- [ ] Verify notes respect string selection
- [ ] Verify notes respect fret range
- [ ] Pause and resume a session
- [ ] End a session early
- [ ] Complete a time-based session
- [ ] Complete a count-based session
- [ ] Verify progress bars update correctly
- [ ] Verify timer counts up correctly
- [ ] Verify all strings are displayed (no hardcoded text)

## Strings Resource

All UI strings are externalized to `strings.xml` for internationalization:

```xml
<!-- Practice Session -->
<string name="practice_session_title">Practice Session</string>
<string name="ready_to_practice">Ready to Practice!</string>
<string name="practice_instructions">Play each note as it appears...</string>
<string name="play_this_note">Play this note:</string>
<string name="string_and_fret">String %1$d, Fret %2$d</string>
<string name="next_note">Next Note</string>
<string name="pause">Pause</string>
<string name="resume">Resume</string>
<string name="end_session">End Session</string>
<string name="session_paused">Session Paused</string>
<string name="session_complete">Session Complete!</string>
<string name="notes_played">Notes Played</string>
<string name="total_time">Total Time</string>
<string name="finish">Finish</string>
<string name="notes_progress">%1$d / %2$d notes</string>
<string name="notes_completed_count">%1$d notes completed</string>
<string name="time_progress">%1$s / %2$s</string>
<string name="elapsed_time">Time: %1$s</string>
```

## Future Enhancements

### Short Term
1. **Audio Feedback**: Play note sound when displayed
2. **Note Recognition**: Use microphone to verify played note
3. **Visual Fretboard**: Show note position on interactive fretboard
4. **History**: Track practice session history

### Medium Term
1. **Scale Selection**: Proper implementation of scale mode with key selection
2. **Whole Notes Filtering**: Filter out sharps/flats in whole notes mode
3. **Statistics**: Detailed performance analytics
4. **Achievements**: Gamification with badges and milestones

### Long Term
1. **Progressive Difficulty**: Adaptive note selection based on user performance
2. **Custom Tunings**: Support alternative tunings beyond standard
3. **Chord Practice**: Extend to chord recognition
4. **Multiplayer**: Compete with other users

## Technical Details

### Dependencies

```kotlin
// Already included in project
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")
implementation("androidx.compose.material3:material3:1.1.0")
implementation("androidx.navigation:navigation-compose:2.7.0")
```

### Performance Considerations

- **Timer Updates**: Updates every 1 second (not every frame)
- **State Management**: Uses StateFlow for efficient recomposition
- **Random Generation**: O(1) complexity for note generation
- **Memory**: Minimal state stored, no large data structures

### Known Limitations

1. **Build Environment**: Cannot test build due to network restrictions
2. **Note Modes**: WHOLE_NOTES and SCALE currently behave like SEMITONES
3. **No Audio**: Visual display only, no sound playback
4. **No Verification**: User must self-verify correct note playing
5. **Standard Tuning Only**: No support for alternate tunings

## Contributing

When extending this feature:
1. Maintain MVVM architecture
2. Keep UI in Compose (no XML)
3. Externalize all strings to `strings.xml`
4. Add unit tests for business logic
5. Follow Material Design 3 guidelines
6. Use coroutines for async operations
7. Document new features in this file

## License

See LICENSE file in project root.
