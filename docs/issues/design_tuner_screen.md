---
title: [FEATURE] Apply Dark Guitar Background to Tuner Screen
labels: enhancement, ui/ux, design, cody-agent, priority-2
assignees: ''
---

## Parent Issue
This is a sub-issue of the main design consistency feature.
**Parent**: Apply Design Consistency Across All Screens

## Screen Overview
**File**: `app/src/main/java/com/androidguitarnotes/app/tuner/TunerScreen.kt`

The Tuner Screen provides real-time guitar tuning assistance with:
- Large note name display
- Visual tuning indicator (dial/gauge)
- Frequency display
- Cent deviation display (-50 to +50)
- Start/Stop listening controls
- Real-time audio processing and pitch detection

## Current State
- Uses standard Material3 `Scaffold` with `TopAppBar`
- System background color
- Large text displays for note information
- Visual tuning indicator with animations
- Start/Stop button with state management
- Real-time updates during audio listening

## Desired State
- Dark guitar fretboard background with overlay
- High-contrast display elements for at-a-glance tuning
- Semi-transparent but clearly visible indicators
- Enhanced color coding for tuning status (sharp/flat/in-tune)
- Maintains all tuning functionality and accuracy
- Smooth animations on dark background

## Implementation Tasks

- [ ] Wrap screen content in Box with layered structure
- [ ] Add guitar background image layer
- [ ] Add semi-transparent overlay (0.6f alpha)
- [ ] Update Scaffold to use transparent container color
- [ ] Update TopAppBar styling with white text
- [ ] Update note name display (large, white, high contrast)
- [ ] Update frequency display styling
- [ ] Update cent deviation display
- [ ] Style tuning indicator/gauge with enhanced visibility
- [ ] Update color coding (in-tune: green, flat: red/orange, sharp: blue)
- [ ] Style Start/Stop listening button
- [ ] Ensure animations work smoothly on dark background
- [ ] Update status text ("Listening...", "Tap to start")
- [ ] Test real-time updates with new styling
- [ ] Verify high visibility in various lighting conditions
- [ ] Test permission flow integration
- [ ] Verify accessibility standards

## Design Guidelines Reference
Follow the specifications in `/docs/development/APP_DESIGN_GUIDELINES.md`, particularly:
- Section 2: Background Treatment
- Section 3: Color System (tuning status colors)
- Section 4: Typography (large display text)
- Section 8: Animations and Effects
- Section 10: Screen-Specific Guidelines (Active Session Screens)

## Key Components to Update

### Note Display
- Extra large text size (48-64sp)
- Bold weight
- White with subtle shadow
- High contrast for quick recognition

### Tuning Indicator
- Visual gauge/dial with clear markers
- Color-coded zones:
  - Center (±5 cents): Green with higher intensity
  - Slight deviation (±5-15 cents): Yellow/Orange
  - Significant deviation (>±15 cents): Red/Blue
- Smooth animation between states
- Semi-transparent background for depth

### Frequency Display
- Medium text size (20-24sp)
- White text
- Format: "440.00 Hz"
- Subtle background card

### Cent Deviation Display
- Medium-large text (24-32sp)
- Color-coded by deviation
- Format: "+12" or "-8"
- Clear positive/negative indication

### Start/Stop Button
- Large, prominent button
- Accent color with 0.6f alpha
- White text
- Clear state indication (Start Listening / Stop Listening)
- 16dp rounded corners
- Elevated appearance

## Color Coding for Tuning Status

```kotlin
// Tuning status colors
val inTuneColor = Color(0xFF4CAF50) // Green
val slightlyFlatColor = Color(0xFFFF9800) // Orange
val slightlySharpColor = Color(0xFF2196F3) // Blue
val veryFlatColor = Color(0xFFF44336) // Red
val verySharpColor = Color(0xFF3F51B5) // Indigo

// Apply with appropriate alpha for visibility on dark background
val statusColor = when {
    abs(cents) < 5 -> inTuneColor
    cents < -15 -> veryFlatColor
    cents < 0 -> slightlyFlatColor
    cents < 15 -> slightlySharpColor
    else -> verySharpColor
}
```

## Layout Structure

The tuner should maintain a clear visual hierarchy:

1. **Top**: TopAppBar with back button
2. **Center Focus**: Large note name and tuning indicator
3. **Supporting Info**: Frequency and cent deviation
4. **Bottom**: Start/Stop button

Use generous spacing to prevent visual clutter and enable quick glances during tuning.

## Animation Considerations

- Smooth color transitions for tuning status (300-500ms)
- Animated indicator movement matching pitch changes
- Pulsing effect when perfectly in tune (optional enhancement)
- Ensure animations don't cause performance issues
- Test animation smoothness on lower-end devices

## Acceptance Criteria

- [ ] Background image visible across entire screen
- [ ] Semi-transparent overlay applied (0.6f alpha)
- [ ] Note name is highly visible and readable
- [ ] Frequency displays correctly in Hz
- [ ] Cent deviation displays with correct sign (+/-)
- [ ] Tuning indicator animates smoothly
- [ ] Color coding works for all tuning states
- [ ] In-tune state is clearly distinguishable
- [ ] Start Listening button works
- [ ] Stop Listening button works
- [ ] Real-time updates work correctly
- [ ] Audio permission flow works
- [ ] Back navigation works
- [ ] Screen stays on while listening (KeepScreenOn)
- [ ] High visibility maintained in bright and dim lighting
- [ ] Text contrast meets WCAG AA standards
- [ ] Touch targets are minimum 48x48dp
- [ ] No performance degradation during real-time processing
- [ ] Tested on multiple screen sizes
- [ ] Visual consistency with Home screen

## Testing Checklist

### Functional Testing
- [ ] Tap Start Listening (should begin audio processing)
- [ ] Play guitar note and verify detection
- [ ] Verify note name updates correctly
- [ ] Verify frequency displays accurately
- [ ] Verify cent deviation shows correct value
- [ ] Tune string sharp and verify color changes
- [ ] Tune string flat and verify color changes
- [ ] Achieve in-tune status and verify visual feedback
- [ ] Tap Stop Listening (should stop processing)
- [ ] Navigate back to home
- [ ] Test permission request flow
- [ ] Verify screen stays on while listening

### Visual Testing
- [ ] Background displays correctly
- [ ] Overlay is consistent
- [ ] Note display is highly visible
- [ ] Tuning indicator is clear and intuitive
- [ ] Color transitions are smooth
- [ ] All text is readable
- [ ] Button states are clear
- [ ] No visual flickering during updates
- [ ] Layout works on small screens
- [ ] Layout works on large screens

### Performance Testing
- [ ] Real-time updates are smooth (no lag)
- [ ] Animations don't impact pitch detection
- [ ] No memory leaks during extended use
- [ ] Battery usage is reasonable
- [ ] Works on lower-end devices

### Accessibility Testing
- [ ] Contrast check on all displays
- [ ] Touch target size verification
- [ ] Test with TalkBack (note announcements)
- [ ] Test with large text
- [ ] Color-blind friendly status indicators

## Notes

This is a **Priority 2** screen because:
- Real-time audio processing requires careful testing
- High visibility requirements for active use
- More complex animations and state changes
- Critical for user experience during tuning

Special attention needed for:
- Ensuring dark background doesn't reduce visibility
- Maintaining smooth real-time updates
- Clear tuning status at a glance
- Performance during continuous audio processing

Consider:
- Users often tune in various lighting conditions
- Quick glances are common during tuning
- Color coding should be intuitive
- Large, clear text is essential

## Implementation Assignment
- [ ] Assign to Cody agent (recommended)
- [ ] I'll implement this manually
