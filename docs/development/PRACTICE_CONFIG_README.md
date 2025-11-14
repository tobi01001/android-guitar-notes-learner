# Practice Session Configuration UI

This implementation provides a complete UI for configuring practice sessions in the Guitar Notes Learner app.

## Features

### String Selection
- Users can select which guitar strings to practice on (1-6)
- Multiple selection using Material 3 FilterChips
- At least one string must be selected at all times
- Visual feedback shows selected/unselected state

### Fret Range
- Configure the fret range for practice (0-24)
- Two text input fields: "From Fret" and "To Fret"
- Validates that From <= To
- Number keyboard for easy input

### Note Mode
- Three options via radio buttons:
  1. **Scale**: Practice within a specific scale
  2. **Whole Notes**: Practice all whole notes (no sharps/flats)
  3. **Semitones**: Practice all notes including sharps/flats

### Duration Selection
- Toggle between two modes:
  1. **Time-based**: Set practice duration in minutes
  2. **Note Count**: Set number of notes to practice
- Conditional input field appears based on selection
- Number keyboard for easy input

## Architecture

### MVVM Pattern
- **Model**: `PracticeConfig` data class with enums for modes
- **View**: `PracticeConfigScreen` composable with sub-components
- **ViewModel**: `PracticeConfigViewModel` managing state with StateFlow

### Unidirectional Data Flow
```
User Action → ViewModel Method → StateFlow Update → UI Recomposition
```

### State Management
- Uses Kotlin StateFlow for reactive state updates
- ViewModel exposes read-only `StateFlow<PracticeConfig>`
- UI collects state with `collectAsStateWithLifecycle()`

## File Structure

```
app/src/main/java/com/androidguitarnotes/app/
├── MainActivity.kt                      # Updated to integrate PracticeConfigScreen
└── practice/
    ├── PracticeConfig.kt               # Data models and enums
    ├── PracticeConfigViewModel.kt      # State management
    └── PracticeConfigScreen.kt         # UI components

app/src/main/res/values/
└── strings.xml                          # All UI strings (i18n ready)
```

## Usage

The screen is integrated into the navigation graph in `MainActivity`:

```kotlin
composable("practice") { 
    PracticeConfigScreen(
        onBack = { navController.popBackStack() },
        onStartPractice = { config ->
            // Use config to start practice session
            // config contains: selectedStrings, fretFrom, fretTo, noteMode, etc.
        }
    )
}
```

## Validation

The ViewModel includes validation logic:
- At least one string must be selected
- Fret range must be valid (0 <= from <= to <= 24)
- Duration must be positive based on type

The "Start Practice" button is only enabled when configuration is valid.

## Testing Notes

To build and test this implementation:

1. Ensure you have Android Studio installed
2. Open the project in Android Studio
3. Sync Gradle dependencies
4. Build and run on an emulator or device (API 34+)

The build requires:
- Gradle 8.0+ (wrapper configured for 8.2)
- Android Gradle Plugin 8.0.2
- Kotlin 1.8.20
- Target SDK 34, Min SDK 34

## Screenshots

The UI includes:
- Material 3 design with proper spacing
- Scrollable layout for smaller screens
- Real-time validation feedback
- Clear visual hierarchy
- Accessible components

## Future Enhancements

Potential improvements:
- Add scale selection UI when "Scale" mode is chosen
- Persist configuration across app restarts
- Add preset configurations (beginner, intermediate, advanced)
- Visual representation of selected strings on a fretboard
- Practice history tracking
