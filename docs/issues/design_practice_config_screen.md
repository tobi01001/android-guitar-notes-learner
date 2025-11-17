---
title: [FEATURE] Apply Dark Guitar Background to Practice Config Screen
labels: enhancement, ui/ux, design, cody-agent, priority-1
assignees: ''
---

## Parent Issue
This is a sub-issue of the main design consistency feature.
**Parent**: Apply Design Consistency Across All Screens

## Screen Overview
**File**: `app/src/main/java/com/androidguitarnotes/app/practice/PracticeConfigScreen.kt`

The Practice Configuration Screen allows users to configure their practice session by selecting:
- Guitar strings to practice (E, A, D, G, B, e)
- Fret range (from/to)
- Note mode (Chromatic, Scale, Interval)
- Scale selection (when in Scale mode)
- Root note selection (when in Scale/Interval mode)

## Current State
- Uses standard Material3 `Scaffold` with `TopAppBar`
- White/system background
- Standard Material button colors
- Bottom action bar with Back/Start buttons
- Scrollable content in Column layout

## Desired State
- Dark guitar fretboard background with overlay
- Semi-transparent buttons and surfaces
- White text throughout with proper shadows
- Maintains all existing functionality
- Enhanced visual consistency with Home screen

## Implementation Tasks

- [ ] Wrap screen content in Box with layered structure
- [ ] Add guitar background image layer
- [ ] Add semi-transparent overlay (0.6f alpha)
- [ ] Update Scaffold to use transparent container color
- [ ] Update TopAppBar styling with white text
- [ ] Update button colors to use NoteColors with 0.6f alpha
- [ ] Update card/surface backgrounds to dark with transparency
- [ ] Update all text colors to white
- [ ] Add text shadows where needed for readability
- [ ] Update section dividers/separators to be visible on dark background
- [ ] Update TextField/Input colors (if any)
- [ ] Test scrolling with background
- [ ] Test all interactive elements (string toggles, sliders, dropdowns)
- [ ] Verify accessibility (contrast ratios, touch targets)
- [ ] Test on multiple screen sizes

## Design Guidelines Reference
Follow the specifications in `/docs/development/APP_DESIGN_GUIDELINES.md`, particularly:
- Section 2: Background Treatment
- Section 3: Color System
- Section 4: Typography
- Section 5: Component Styling
- Section 6: Layout Principles
- Section 9: Accessibility

## Implementation Pattern

```kotlin
@Composable
fun PracticeConfigScreen(...) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Background layer
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        
        // Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
        )
        
        // Existing content with transparent Scaffold
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { /* Update colors */ },
            bottomBar = { /* Update colors */ }
        ) { padding ->
            // Existing content with color updates
        }
    }
}
```

## Key Components to Update

### String Selection Section
- Checkbox or toggle button colors
- Section title styling
- Individual string button styling

### Fret Range Section
- Slider track and thumb colors
- TextField backgrounds (if used)
- Range labels and values

### Note Mode Section
- Radio button or segmented control colors
- Mode option card backgrounds

### Scale Selection (conditional)
- Dropdown or list styling
- Selected item highlighting

### Bottom Action Bar
- Back button: OutlinedButton with white border/text
- Start button: Filled button with accent color (0.6f alpha)

## Acceptance Criteria

- [ ] Background image visible across entire screen
- [ ] Semi-transparent overlay applied (0.6f alpha)
- [ ] All text is white and readable
- [ ] Buttons use NoteColors with 0.6f alpha
- [ ] All interactive elements remain fully functional
- [ ] String selection works correctly
- [ ] Fret range adjustment works correctly
- [ ] Note mode selection works correctly
- [ ] Scale selection (when visible) works correctly
- [ ] Start practice navigation works
- [ ] Back navigation works
- [ ] Scrolling is smooth with background
- [ ] Text contrast meets WCAG AA standards (4.5:1 minimum)
- [ ] Touch targets are minimum 48x48dp
- [ ] No performance degradation
- [ ] Tested on small, medium, and large screens
- [ ] Visual consistency with Home screen achieved

## Testing Checklist

### Functional Testing
- [ ] Toggle all string selections on/off
- [ ] Adjust fret range using sliders/inputs
- [ ] Switch between note modes (Chromatic, Scale, Interval)
- [ ] Select different scales when in Scale mode
- [ ] Select different root notes
- [ ] Click Start Practice (should navigate to session)
- [ ] Click Back (should return to home)
- [ ] Scroll through content (should be smooth)

### Visual Testing
- [ ] Background displays correctly
- [ ] Overlay is consistent
- [ ] All text is readable
- [ ] Buttons are visually distinct
- [ ] Selected states are clear
- [ ] Disabled states are appropriate
- [ ] No visual glitches on scroll

### Accessibility Testing
- [ ] Use contrast checker on all text
- [ ] Verify touch target sizes
- [ ] Test with TalkBack enabled
- [ ] Test with large text size
- [ ] Test with display scaling

## Notes

This is a **Priority 1** screen because:
- Relatively simple layout (scrollable form)
- No complex animations or real-time updates
- Good starter screen for establishing patterns
- Users configure practice before starting session

Lessons learned from this implementation should inform subsequent screens.

## Implementation Assignment
- [ ] Assign to Cody agent (recommended)
- [ ] I'll implement this manually
