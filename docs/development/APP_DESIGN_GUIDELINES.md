# App Design Guidelines

## Overview
This document defines the visual design principles and guidelines for the Android Guitar Notes Learner app. All screens, features, and extensions should follow these guidelines to maintain a consistent user experience.

## Design Philosophy
The app embraces a **dark, immersive guitar-themed aesthetic** that creates an intimate, focused environment for learning and practicing guitar. The design should feel like you're in a dimly-lit music studio with your guitar.

## Core Design Principles

### 1. Visual Identity
- **Primary Theme**: Dark guitar background with warm, subtle gradients
- **Color Palette**: Deep blacks (#0D0D0D to #262626), warm overlays, and vibrant accent colors
- **Imagery**: Guitar fretboard imagery as the foundation of visual identity
- **Atmosphere**: Professional, focused, intimate, and musically-inspired

### 2. Background Treatment

#### Primary Background
- Use the guitar fretboard image (`background.jpg`) as the base layer
- Apply `ContentScale.Crop` to ensure full coverage
- Credit: Photo by Peter Jarkuliš (https://www.pexels.com/@peter-jarkulis-87581/)
- Source: https://www.pexels.com/photo/black-acoustic-guitar-287202/

#### Overlay Strategy
- Apply a semi-transparent black overlay (alpha 0.6f) to improve readability
- Use vignette effects to darken edges and focus attention
- Maintain consistent overlay transparency across screens

#### Alternative Background (when image is not suitable)
- Use `guitar_background.xml` gradient drawable
- Two-layer gradient system:
  - Base: Linear gradient from #262626 → #1A1A1A → #0D0D0D (90° angle)
  - Overlay: Radial gradient vignette effect with center transparency

### 3. Color System

#### Accent Colors
Use the `NoteColors` utility class for consistent, accessible colors:
- Practice: Warm, inviting tones
- Tuner: Cool, precise tones
- Notes Played: Vibrant, attention-grabbing tones
- Settings: Neutral, professional tones

#### Text Colors
- Primary text: White (`Color.White`)
- Secondary text: White with reduced alpha (0.7f - 0.85f)
- Disabled text: White with alpha 0.38f

#### Button Colors
- Container: Accent color with 0.6f alpha for semi-transparency
- Content: White for maximum contrast
- Elevation: 4dp default, 8dp pressed

### 4. Typography

#### Hierarchy
- **Screen Titles**: `MaterialTheme.typography.headlineMedium`
  - Font weight: Bold
  - Color: White
  - Text shadow: 4dp blur for depth
- **Section Headers**: 18-20sp, FontWeight.Bold
- **Body Text**: 16sp, FontWeight.Normal
- **Button Labels**: 16sp, FontWeight.Bold
- **Captions**: 14sp, FontWeight.Normal

#### Text Effects
- Apply subtle shadows or blur to titles over busy backgrounds
- Ensure minimum contrast ratio of 4.5:1 (WCAG AA standard)
- Use `TextAlign.Center` for primary headings

### 5. Component Styling

#### Buttons
```kotlin
// Navigation/Primary Buttons
ButtonDefaults.buttonColors(
    containerColor = accentColor.copy(alpha = 0.6f),
    contentColor = Color.White,
)
shape = RoundedCornerShape(16.dp)
elevation = ButtonDefaults.buttonElevation(
    defaultElevation = 4.dp,
    pressedElevation = 8.dp,
)
```

#### Cards and Surfaces
- Background: Dark with slight transparency (#1A1A1A with alpha)
- Corner radius: 16dp for primary surfaces, 12dp for secondary
- Elevation: 2-4dp for subtle depth
- Border: Optional 1dp white border with low alpha (0.12f) for definition

#### Icons
- Size: 48dp for primary actions, 24dp for navigation/toolbar
- Color: White or accent color based on context
- Ensure sufficient touch target size (minimum 48x48dp)

### 6. Layout Principles

#### Spacing System
Use consistent spacing multiples of 4dp:
- Tiny: 4dp
- Small: 8dp
- Medium: 16dp
- Large: 24dp
- Extra Large: 32dp, 48dp

#### Screen Structure
```
Box (Full screen)
├── Background Image/Gradient Layer
├── Semi-transparent Overlay (0.6f alpha)
└── Content Layer
    ├── TopAppBar (if applicable)
    │   ├── Navigation icon (back button)
    │   └── Title
    ├── Main Content (scrollable if needed)
    │   └── Content with consistent padding (16-24dp)
    └── Bottom Actions/Bar (if applicable)
```

#### Content Padding
- Horizontal padding: 24dp for main content, 16dp for compact layouts
- Vertical spacing: 16-24dp between major sections
- Top padding: 32dp after title for visual breathing room

### 7. Navigation Patterns

#### Top App Bar
```kotlin
TopAppBar(
    title = { Text(stringResource(R.string.screen_title)) },
    navigationIcon = {
        IconButton(onClick = onBack) {
            Text("←", fontSize = 24.sp) // Consistent back arrow
        }
    },
)
```

#### Bottom Action Bar
- Fixed at bottom with 16dp padding
- Use Row with `Arrangement.spacedBy(8.dp)` for multiple buttons
- Primary action on the right, secondary on the left
- Use `Modifier.weight(1f)` for equal button widths

### 8. Animations and Effects

#### Transitions
- Use `FastOutSlowInEasing` for natural motion
- Duration: 300-500ms for most transitions
- Avoid abrupt changes; fade overlays when appropriate

#### Visual Feedback
- Button press: Elevation change (4dp → 8dp)
- Loading states: Linear progress indicators with accent colors
- Success/Error: Color changes with appropriate durations (500ms)

#### Effects
- Blur: Use sparingly (1-2dp) for title shadows over backgrounds
- Shadow: 4dp for elevated components, 8dp for emphasis
- Vignette: Radial gradients for focus direction

### 9. Accessibility

#### Color Contrast
- Maintain 4.5:1 contrast ratio minimum (WCAG AA)
- Use `NoteColors.getAccessibleButtonColorFor()` for consistent accessibility
- Test with dark mode and light mode if supported

#### Touch Targets
- Minimum 48x48dp for all interactive elements
- Provide adequate spacing between touch targets (8dp minimum)

#### Content Descriptions
- Provide meaningful `contentDescription` for all icons
- Use `null` only for purely decorative images

#### Text Sizing
- Support system font scaling
- Test layouts with large text sizes enabled

### 10. Screen-Specific Guidelines

#### Home Screen
- Full-screen guitar background with overlay
- 2x2 grid of navigation buttons centered
- Title at top with shadow effect
- Generous padding and spacing for visual balance

#### Configuration/Settings Screens
- Dark background with guitar theme
- Scrollable content in Column layout
- Group related settings with subtle dividers or spacing
- Bottom fixed action bar for primary actions

#### Active Session Screens
- Dark background maintained
- Keep screen on during active use
- High-contrast, large elements for at-a-glance viewing
- Minimal distractions, focused on primary task

#### Modal Dialogs
- Dark surface color matching app theme
- Rounded corners (16dp)
- Clear title and action buttons
- Dismissible background overlay

## Implementation Checklist

When implementing a new screen or feature, ensure:
- [ ] Dark guitar background or gradient is applied
- [ ] Semi-transparent overlay (0.6f alpha) is present
- [ ] Typography follows size and weight guidelines
- [ ] Colors use `NoteColors` utility for consistency
- [ ] Buttons have proper transparency (0.6f), rounded corners (16dp), and elevation
- [ ] Spacing uses the 4dp-based system
- [ ] Navigation follows standard TopAppBar pattern
- [ ] Touch targets are minimum 48x48dp
- [ ] Text contrast meets WCAG AA standards
- [ ] Animations use appropriate easing and duration
- [ ] Content descriptions provided for accessibility

## Future Considerations

### Potential Enhancements
- Dark mode / Light mode toggle (currently optimized for dark)
- Customizable background images
- Theme color customization
- Advanced accessibility options (high contrast mode)
- Animated background elements (subtle particle effects, string vibrations)

### Extension Guidelines
When adding new features:
1. Review these guidelines first
2. Maintain visual consistency with existing screens
3. Reuse existing UI components where possible
4. Test on multiple screen sizes and densities
5. Verify accessibility compliance
6. Document any new patterns or components

## Resources

### Key Files
- `MainActivity.kt` - HomeScreen reference implementation
- `app/src/main/res/drawable/background.jpg` - Primary background image
- `app/src/main/res/drawable/guitar_background.xml` - Alternative gradient background
- `app/src/main/java/com/androidguitarnotes/app/ui/NoteColors.kt` - Color utility

### Compose Components
- Material3 components library
- Standard Jetpack Compose layouts
- Custom composables in `ui/` package

### Design Tools
- Color contrast checker: https://webaim.org/resources/contrastchecker/
- Material Design 3 guidelines: https://m3.material.io/
- Android accessibility guidelines: https://developer.android.com/guide/topics/ui/accessibility

## Revision History
- v1.0 (2024) - Initial design guidelines based on Home screen implementation
