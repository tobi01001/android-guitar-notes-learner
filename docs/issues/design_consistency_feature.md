---
title: [FEATURE] Apply Dark Guitar Background Design to All Screens
labels: enhancement, ui/ux, design, cody-agent
assignees: ''
---

## Feature Overview
This feature request tracks the effort to apply the dark guitar background design principles from the Home screen to all other screens in the app, ensuring a consistent and immersive user experience throughout the application.

## Problem Statement
Currently, only the Home screen features the distinctive dark guitar background with semi-transparent overlays that creates an immersive, guitar-focused atmosphere. Other screens (Practice Config, Practice Session, Tuner, Notes Played, and Settings) use standard Material3 Scaffold layouts without the guitar theme, creating a disjointed user experience.

**Affected Screens:**
- Practice Configuration Screen (`PracticeConfigScreen.kt`)
- Practice Session Screen (`PracticeSessionScreen.kt`)
- Tuner Screen (`TunerScreen.kt`)
- Notes Played Screen (`NotesPlayedScreen.kt`)
- Settings Screen (`SettingsScreen.kt`)
- Permission Screen (`PermissionScreen.kt`)

## Proposed Solution
Apply the design principles documented in `/docs/development/APP_DESIGN_GUIDELINES.md` to all screens. Each screen should maintain its functionality while adopting:

1. **Background Layer**: Dark guitar fretboard image or gradient background
2. **Overlay**: Semi-transparent black overlay (0.6f alpha) for content readability
3. **Color Scheme**: Consistent use of `NoteColors` utility for accents
4. **Typography**: White text with appropriate shadows/effects for readability
5. **Component Styling**: Semi-transparent buttons (0.6f alpha), 16dp rounded corners, proper elevation
6. **Spacing**: Consistent 4dp-based spacing system

The transformation should be done screen-by-screen to ensure quality and minimize risk.

## Design Guidelines Reference
All implementation work should follow the comprehensive design guidelines documented in:
**`/docs/development/APP_DESIGN_GUIDELINES.md`**

This document provides:
- Complete design philosophy and principles
- Background treatment specifications
- Color system and accessibility standards
- Typography hierarchy
- Component styling patterns
- Layout principles and spacing system
- Navigation patterns
- Implementation checklist

## Sub-Issues and Task Breakdown
This feature should be implemented through the following sub-issues, each focusing on a specific screen:

### Priority 1: Configuration and Settings Screens
These screens have simpler layouts and are good candidates for initial implementation:
- [ ] **Issue #TBD**: Apply design to Practice Configuration Screen
  - Scrollable content with dark background
  - Bottom action bar for Start/Back buttons
  - String selection, fret range, and mode selection UI

- [ ] **Issue #TBD**: Apply design to Settings Screen
  - Dark background with sections for audio, tuning, and algorithm settings
  - Toggle switches and sliders with enhanced visibility
  - Dialog components (Audio Source, Algorithm selection)

### Priority 2: Active Session Screens
These screens require careful attention to maintain usability during active use:
- [ ] **Issue #TBD**: Apply design to Tuner Screen
  - Large tuning display with enhanced contrast
  - Visual tuning indicator with guitar-themed colors
  - Maintain high visibility for frequency and cent displays

- [ ] **Issue #TBD**: Apply design to Notes Played Screen
  - Real-time note detection display
  - Fretboard visualization overlay
  - Start/Stop listening controls with enhanced styling

- [ ] **Issue #TBD**: Apply design to Practice Session Screen
  - Current note display with high contrast
  - Progress indicators and statistics
  - Fretboard visualization integration
  - Session complete dialog styling

### Priority 3: Supporting Screens
- [ ] **Issue #TBD**: Apply design to Permission Screen
  - Permission rationale dialog with consistent styling
  - Clear call-to-action buttons

## Implementation Guidelines

### For Each Screen:
1. **Wrap content in Box with layered structure:**
   ```kotlin
   Box(modifier = Modifier.fillMaxSize()) {
       // Layer 1: Background image or gradient
       Image(painter = painterResource(R.drawable.background), ...)
       
       // Layer 2: Semi-transparent overlay
       Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)))
       
       // Layer 3: Existing screen content with adjustments
       Scaffold(containerColor = Color.Transparent) { ... }
   }
   ```

2. **Update component colors:**
   - Use `NoteColors.getAccessibleButtonColorFor()` for accent colors
   - Apply 0.6f alpha to button containers
   - Ensure white text with proper contrast

3. **Adjust typography:**
   - Title text with shadow effects
   - White text colors throughout
   - Maintain size hierarchy

4. **Maintain functionality:**
   - All existing features must continue to work
   - No behavioral changes, only visual updates
   - Test all interactions and states

5. **Accessibility:**
   - Verify text contrast ratios (minimum 4.5:1)
   - Ensure touch targets remain 48x48dp
   - Test with TalkBack enabled

## Technical Considerations

### Resource Management
- Background image is already present at `app/src/main/res/drawable/background.jpg`
- Fallback gradient available at `app/src/main/res/drawable/guitar_background.xml`
- Both resources are properly credited and licensed

### Performance
- Background image should be loaded efficiently with proper content scale
- Consider memory usage on lower-end devices
- Test smooth scrolling on content-heavy screens

### Testing Strategy
- Visual regression testing for each modified screen
- Accessibility testing (contrast, touch targets, screen readers)
- Cross-device testing (various screen sizes and densities)
- Dark mode compatibility (if/when implemented)

### Code Quality
- Follow existing Kotlin and Compose conventions
- Reuse composables where possible
- Create shared UI components for common patterns
- Document any new reusable components

## Use Case and Benefits

### User Experience Benefits
1. **Visual Consistency**: Users experience a cohesive design throughout the app
2. **Immersion**: The guitar-themed aesthetic creates a focused learning environment
3. **Professional Feel**: Polished, consistent design enhances perceived quality
4. **Brand Identity**: Distinctive visual style sets the app apart

### Developer Benefits
1. **Clear Guidelines**: Documented patterns make future development easier
2. **Reusable Components**: Shared styling components reduce code duplication
3. **Maintainability**: Consistent structure improves long-term maintenance
4. **Onboarding**: New contributors can quickly understand design patterns

## Acceptance Criteria

For the overall feature to be considered complete:
- [ ] All screens listed above implement the dark guitar background design
- [ ] Design guidelines document is complete and accurate
- [ ] All sub-issues are resolved and closed
- [ ] Visual consistency is maintained across all screens
- [ ] No functional regressions introduced
- [ ] Accessibility standards maintained (WCAG AA compliance)
- [ ] Code follows established patterns and conventions
- [ ] All changes are tested on multiple devices and screen sizes
- [ ] Documentation is updated to reflect new design patterns

## Alternatives Considered

### Alternative 1: Gradual Theme Toggle
Allow users to toggle between "classic" (current) and "immersive" (new) themes.
- **Pros**: Provides user choice, reduces risk
- **Cons**: Increases complexity, splits design effort, harder to maintain
- **Decision**: Not chosen for initial implementation; can be considered later

### Alternative 2: Background Image Per Screen
Use different guitar-related images for each screen type.
- **Pros**: Visual variety, context-specific imagery
- **Cons**: Increased resource size, harder to maintain consistency
- **Decision**: Stick with single background for consistency; can enhance later

### Alternative 3: Simplified Gradient Only
Use only the gradient background without the photo.
- **Pros**: Smaller app size, faster rendering
- **Cons**: Less visually distinctive, reduced immersion
- **Decision**: Keep photo as primary option with gradient as fallback

## Additional Context

### Related Documentation
- Design Guidelines: `/docs/development/APP_DESIGN_GUIDELINES.md`
- Home Screen Reference: `app/src/main/java/com/androidguitarnotes/app/MainActivity.kt`
- NoteColors Utility: `app/src/main/java/com/androidguitarnotes/app/ui/NoteColors.kt`

### Background Image Attribution
- Photo by Peter Jarkuliš (https://www.pexels.com/@peter-jarkulis-87581/)
- Source: https://www.pexels.com/photo/black-acoustic-guitar-287202/
- License: Free to use (Pexels License)

### Screenshots
_Screenshots showing current state vs. desired state should be added as work progresses_

## Implementation Assignment

**Recommended Agent**: `cody` (Modern Android & Audio Specialist)

The Cody agent is well-suited for this task as it specializes in:
- Jetpack Compose UI implementation
- MVVM architecture (maintained in updates)
- Kotlin best practices
- Android design patterns

Each sub-issue should be assigned to the Cody agent for implementation, ensuring consistent quality and adherence to Android best practices.

## Would you like to implement this feature?
- [ ] Yes, I'd like to work on this (please assign to Cody agent)
- [ ] No, just suggesting

## Additional Notes
Any other context, ideas, or concerns about this feature
