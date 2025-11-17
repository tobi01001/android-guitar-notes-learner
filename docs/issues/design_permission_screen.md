---
title: [FEATURE] Apply Dark Guitar Background to Permission Screen
labels: enhancement, ui/ux, design, cody-agent, priority-3
assignees: ''
---

## Parent Issue
This is a sub-issue of the main design consistency feature.
**Parent**: Apply Design Consistency Across All Screens

## Screen Overview
**File**: `app/src/main/java/com/androidguitarnotes/app/permissions/PermissionScreen.kt`

The Permission Screen displays a rationale dialog when audio recording permission is needed:
- Clear explanation of why permission is needed
- Friendly, educational tone
- Request permission button
- Dismiss/cancel option
- Displayed as a dialog or full-screen modal

## Current State
- Standard AlertDialog or modal presentation
- System dialog styling
- Title, body text, and action buttons
- May appear over any screen

## Desired State
- Dark-themed dialog matching app aesthetic
- Clear, friendly explanation text in white
- Styled action buttons consistent with app
- Semi-transparent background overlay
- Rounded corners and proper elevation
- Icon to illustrate microphone permission

## Implementation Tasks

- [ ] Update dialog container color to dark theme
- [ ] Update title text color to white
- [ ] Update body text color to white
- [ ] Add optional microphone icon at top
- [ ] Style "Allow Permission" button with accent color
- [ ] Style "Cancel" / "Dismiss" button
- [ ] Ensure rounded corners (16dp)
- [ ] Add semi-transparent scrim/background
- [ ] Update text styles for hierarchy
- [ ] Test dialog appearance over all screens
- [ ] Test permission flow after allowing
- [ ] Test dismissal flow
- [ ] Verify accessibility standards

## Design Guidelines Reference
Follow the specifications in `/docs/development/APP_DESIGN_GUIDELINES.md`, particularly:
- Section 3: Color System (button colors)
- Section 4: Typography (dialog text)
- Section 5: Component Styling (buttons)
- Section 10: Screen-Specific Guidelines (Modal Dialogs)

## Implementation Pattern

```kotlin
@Composable
fun PermissionRationaleScreen(
    onRequestPermission: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A).copy(alpha = 0.95f),
        shape = RoundedCornerShape(16.dp),
        icon = {
            Icon(
                imageVector = Icons.Filled.Mic,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = stringResource(R.string.permission_title),
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = stringResource(R.string.permission_rationale),
                color = Color.White.copy(alpha = 0.9f),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NoteColors.getAccessibleButtonColorFor("Permission")
                        .copy(alpha = 0.6f),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.permission_allow))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.White.copy(alpha = 0.7f)
                )
            ) {
                Text(stringResource(R.string.permission_cancel))
            }
        }
    )
}
```

## Key Elements to Style

### Dialog Container
- Background: Dark (#1A1A1A) with high alpha (0.95f)
- Rounded corners: 16dp
- Proper elevation/shadow for depth

### Icon (Optional Enhancement)
- Microphone icon at top
- White color
- 48x48dp size
- Provides visual context

### Title
- "Microphone Permission Needed" or similar
- White text
- HeadlineSmall or Medium style
- Bold weight

### Body Text
- Clear explanation of why permission is needed
- White text with slight alpha (0.9f)
- BodyMedium style
- Multiple paragraphs if needed
- Friendly, educational tone

### Buttons
- **Primary (Allow)**: 
  - Filled button
  - Accent color with 0.6f alpha
  - White text
  - Rounded corners (12dp)
- **Secondary (Cancel)**:
  - Text button
  - White text with 0.7f alpha
  - No background

## Rationale Text Content

The permission rationale should explain:
1. **What**: App needs microphone access
2. **Why**: To detect guitar notes for tuning and practice
3. **When**: Only when actively listening (user control)
4. **Security**: Audio is processed locally, not recorded or transmitted

Example:
> "This app needs access to your microphone to hear and detect the notes you play on your guitar.
> 
> Your audio is processed in real-time on your device and is never recorded, stored, or transmitted.
> 
> You can start and stop listening at any time."

## Acceptance Criteria

- [ ] Dialog uses dark theme colors
- [ ] Background is dark and semi-transparent
- [ ] Title text is white and clear
- [ ] Body text is white and readable
- [ ] Explanation is clear and friendly
- [ ] Icon displays correctly (if added)
- [ ] Allow button is prominent and styled
- [ ] Cancel button is clear but secondary
- [ ] Rounded corners applied (16dp)
- [ ] Dialog appears centered on screen
- [ ] Scrim/overlay behind dialog is visible
- [ ] Allow button triggers permission request
- [ ] Cancel button dismisses dialog
- [ ] Dialog can be dismissed by tapping outside (optional)
- [ ] Text contrast meets WCAG AA standards
- [ ] Touch targets are minimum 48x48dp
- [ ] Works on all screen sizes
- [ ] Visual consistency with other app dialogs

## Testing Checklist

### Functional Testing
- [ ] Trigger permission request from Tuner screen
- [ ] Verify dialog appears
- [ ] Tap Allow button
- [ ] Verify system permission dialog appears
- [ ] Grant permission and verify feature works
- [ ] Trigger permission request from Notes Played screen
- [ ] Tap Cancel button
- [ ] Verify dialog dismisses
- [ ] Verify feature doesn't start without permission
- [ ] Test on first app launch
- [ ] Test after permission was previously denied

### Visual Testing
- [ ] Dialog is centered on screen
- [ ] Background scrim is visible
- [ ] All text is readable
- [ ] Icon displays correctly (if added)
- [ ] Buttons are clearly distinguished
- [ ] No text overflow or clipping
- [ ] Works on small screens
- [ ] Works on large screens
- [ ] Works in landscape orientation

### Accessibility Testing
- [ ] Contrast check on all text
- [ ] Button touch targets verified
- [ ] Test with TalkBack
- [ ] Screen reader announces dialog purpose
- [ ] Buttons are clearly labeled
- [ ] Test with large text size

## Notes

This is a **Priority 3** screen because:
- Simpler component (single dialog)
- Less frequently seen (only on first permission request)
- Can be styled after main screens
- Important for consistency but not core functionality

Special attention needed for:
- Clear, friendly explanation that doesn't scare users
- Professional appearance builds trust
- Consistency with other dialogs (settings, session complete)
- Ensuring users understand what they're granting

Consider:
- First impression of app's permissions handling
- User trust is important for allowing microphone access
- Clear explanation reduces permission denials
- Consistent styling reinforces professionalism

## Implementation Assignment
- [ ] Assign to Cody agent (recommended)
- [ ] I'll implement this manually
