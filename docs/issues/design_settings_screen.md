---
title: [FEATURE] Apply Dark Guitar Background to Settings Screen
labels: enhancement, ui/ux, design, cody-agent, priority-1
assignees: ''
---

## Parent Issue
This is a sub-issue of the main design consistency feature.
**Parent**: Apply Design Consistency Across All Screens

## Screen Overview
**File**: `app/src/main/java/com/androidguitarnotes/app/settings/SettingsScreen.kt`

The Settings Screen provides configuration options for:
- Audio feedback toggle
- Default tuning selection
- Microphone sensitivity adjustment
- Auto-adjust sensitivity toggle
- Audio source selection
- Noise gate threshold
- Pitch detection algorithm selection

## Current State
- Uses standard Material3 `Scaffold` with `TopAppBar`
- System background color
- Standard switches, sliders, and radio buttons
- Dialog components for audio source and algorithm selection
- Scrollable content layout

## Desired State
- Dark guitar fretboard background with overlay
- Semi-transparent settings cards/sections
- White text throughout with proper contrast
- Enhanced switches and sliders on dark background
- Dialog components styled consistently
- Maintains all existing functionality

## Implementation Tasks

- [ ] Wrap screen content in Box with layered structure
- [ ] Add guitar background image layer
- [ ] Add semi-transparent overlay (0.6f alpha)
- [ ] Update Scaffold to use transparent container color
- [ ] Update TopAppBar styling with white text
- [ ] Update section headers to white with appropriate styling
- [ ] Update Switch colors for on/off states
- [ ] Update Slider track and thumb colors
- [ ] Update RadioButton colors
- [ ] Update Divider colors to be visible on dark background
- [ ] Style AudioSource selection dialog
- [ ] Style Algorithm selection dialog
- [ ] Style Permission rationale dialog
- [ ] Update all descriptive text colors to white
- [ ] Add text shadows where needed
- [ ] Test scrolling with background
- [ ] Test all settings interactions
- [ ] Test dialog opening and closing
- [ ] Verify accessibility standards

## Design Guidelines Reference
Follow the specifications in `/docs/development/APP_DESIGN_GUIDELINES.md`, particularly:
- Section 2: Background Treatment
- Section 3: Color System (especially for switches and sliders)
- Section 4: Typography
- Section 5: Component Styling
- Section 10: Screen-Specific Guidelines (Modal Dialogs)

## Key Components to Update

### Switch Components
```kotlin
Switch(
    checked = enabled,
    onCheckedChange = { ... },
    colors = SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = NoteColors.getAccessibleButtonColorFor("Settings"),
        uncheckedThumbColor = Color.White.copy(alpha = 0.7f),
        uncheckedTrackColor = Color.White.copy(alpha = 0.3f),
    )
)
```

### Slider Components
```kotlin
Slider(
    value = sensitivity,
    onValueChange = { ... },
    colors = SliderDefaults.colors(
        thumbColor = Color.White,
        activeTrackColor = NoteColors.getAccessibleButtonColorFor("Settings"),
        inactiveTrackColor = Color.White.copy(alpha = 0.3f),
    )
)
```

### Section Layout
- Use Cards with dark semi-transparent backgrounds
- Group related settings with spacing
- Clear section headers in white

### Dialog Components
- Dark surface color matching app theme
- White text for options
- Accent-colored selected item
- Rounded corners (16dp)

## Specific Settings to Style

### Audio Feedback Setting
- Switch with label
- Conditional permission request handling

### Microphone Sensitivity
- Slider with current value display
- Min/Max labels
- Auto-adjust toggle below

### Audio Source Selection
- Clickable row that opens dialog
- Current selection displayed
- Dialog with radio buttons for options

### Noise Gate Threshold
- Slider with dB value display
- Clear labels for "Off" and threshold values

### Pitch Detection Algorithm
- Clickable row that opens dialog
- Current algorithm displayed
- Dialog with descriptions for each algorithm

### Default Tuning
- Display current tuning
- Future: may have selection dialog

## Dialog Styling Pattern

```kotlin
AlertDialog(
    onDismissRequest = { ... },
    containerColor = Color(0xFF1A1A1A).copy(alpha = 0.95f),
    title = {
        Text(
            text = "Dialog Title",
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall
        )
    },
    text = {
        // Radio options or content
        Column {
            options.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { ... }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selected,
                        onClick = { ... },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = accentColor,
                            unselectedColor = Color.White.copy(alpha = 0.6f)
                        )
                    )
                    Text(text = option, color = Color.White)
                }
            }
        }
    },
    confirmButton = {
        TextButton(onClick = { ... }) {
            Text("OK", color = accentColor)
        }
    },
    shape = RoundedCornerShape(16.dp)
)
```

## Acceptance Criteria

- [ ] Background image visible across entire screen
- [ ] Semi-transparent overlay applied (0.6f alpha)
- [ ] All text is white and readable
- [ ] All switches work and show correct state
- [ ] All sliders work and show correct values
- [ ] Audio feedback toggle works with permission handling
- [ ] Microphone sensitivity adjustment works
- [ ] Auto-adjust sensitivity toggle works
- [ ] Audio source dialog opens, displays options, allows selection
- [ ] Algorithm selection dialog opens, displays options, allows selection
- [ ] Default tuning displays correctly
- [ ] Noise gate threshold adjustment works
- [ ] Back navigation works
- [ ] Scrolling is smooth with background
- [ ] Settings persist correctly
- [ ] Text contrast meets WCAG AA standards
- [ ] Touch targets are minimum 48x48dp
- [ ] No performance degradation
- [ ] Tested on multiple screen sizes
- [ ] Visual consistency with Home screen

## Testing Checklist

### Functional Testing
- [ ] Toggle audio feedback on/off
- [ ] Adjust microphone sensitivity slider
- [ ] Toggle auto-adjust sensitivity
- [ ] Open audio source dialog and select option
- [ ] Open algorithm dialog and select option
- [ ] Adjust noise gate threshold
- [ ] Verify all settings persist after navigation
- [ ] Test permission flow when enabling audio feedback
- [ ] Navigate back to home
- [ ] Scroll through all settings

### Visual Testing
- [ ] Background displays correctly
- [ ] Overlay is consistent
- [ ] All text is readable
- [ ] Switch states are visually clear (on/off)
- [ ] Slider positions are clear
- [ ] Dialogs display correctly
- [ ] Selected items in dialogs are highlighted
- [ ] No visual glitches on scroll
- [ ] Dividers are visible but subtle

### Accessibility Testing
- [ ] Contrast check on all text and controls
- [ ] Touch target size verification
- [ ] Test with TalkBack
- [ ] Test with large text
- [ ] Verify switch labels are clear
- [ ] Verify slider purpose is clear

## Notes

This is a **Priority 1** screen because:
- Form-based layout similar to Practice Config
- No real-time audio processing
- Clear information hierarchy
- Important for establishing dialog styling patterns

Special attention needed for:
- Switch and slider styling on dark backgrounds
- Dialog component consistency
- Permission request flow integration

## Implementation Assignment
- [ ] Assign to Cody agent (recommended)
- [ ] I'll implement this manually
