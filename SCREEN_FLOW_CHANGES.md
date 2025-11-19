# Practice Session Screen Flow Changes

## Previous Flow

```
┌──────────────┐
│  Home Screen │
└──────┬───────┘
       │ Click "Practice"
       ▼
┌──────────────────────┐
│  Config Screen       │
│  - String selection  │
│  - Fret range       │
│  - Note mode        │
│  - Duration         │
│  - Progression mode │
│                      │
│  [Start Practice]   │
│  [Back]             │
└──────┬───────────────┘
       │ Click "Start Practice"
       ▼
┌──────────────────────┐
│  Ready Screen        │
│  "Ready to Practice!"│
│                      │
│  [Start]            │
│  [Back]             │
└──────┬───────────────┘
       │ Click "Start"
       ▼
┌──────────────────────┐
│  Active Session      │
│  - Current note      │
│  - Progress bars     │
│  - Fretboard view    │
│                      │
│  [Next Note]        │
│  [Pause] [End]      │
└──────┬───────────────┘
       │ Complete
       ▼
┌──────────────────────┐
│  Completed Screen    │
│  - Notes played      │
│  - Total time        │
│                      │
│  [Finish]           │
└──────┬───────────────┘
       │ Click "Finish"
       ▼
   (Back to Config)
```

### Pain Points in Previous Flow:
1. Always starts at Config Screen, even if settings are already configured
2. No way to see current settings on Ready Screen
3. No quick way to modify settings from Ready Screen
4. After completion, only option is "Finish" - requires full navigation to restart
5. No quick "Repeat" option for repeated practice sessions

---

## New Flow

```
┌──────────────┐
│  Home Screen │
└──────┬───────┘
       │ Click "Practice"
       ▼
┌──────────────────────────────────┐
│  Ready Screen                     │
│  "Ready to Practice!"             │
│                                   │
│  ┌─────────────────────────────┐ │
│  │ Practice Settings            │ │
│  │ • Strings: 1, 2, 3, 4, 5, 6 │ │
│  │ • Fret Range: 0–12          │ │
│  │ • Note Mode: Whole Notes     │ │
│  │ • Duration: 5 minutes        │ │
│  │ • Progression: Manual        │ │
│  └─────────────────────────────┘ │
│                                   │
│       [Start Practice]            │ ← Main action
│                                   │
│    [Back]      [Config]          │ ← Secondary actions
└──────┬────────────────┬───────────┘
       │                │
       │                └─────────────┐
       │ Click "Start"                │ Click "Config"
       ▼                              ▼
┌──────────────────────┐    ┌──────────────────────┐
│  Active Session      │    │  Config Screen       │
│  - Current note      │    │  - String selection  │
│  - Progress bars     │    │  - Fret range       │
│  - Fretboard view    │    │  - Note mode        │
│  - Audio feedback    │    │  - Duration         │
│                      │    │  - Progression mode │
│  [Next Note]        │    │                      │
│  [Pause] [End]      │    │  [Start Practice]   │
└──────┬───────────────┘    │  [Back]             │
       │ Complete            └──────────────────────┘
       ▼
┌──────────────────────────────────┐
│  Completed Screen                 │
│  - Notes played: 42               │
│  - Total time: 5:23               │
│                                   │
│       [Repeat]                   │ ← New! Quick restart
│                                   │
│    [Config]    [Back]            │ ← New! More options
└──────┬───────────┬────────────────┘
       │           │
       │ "Repeat"  │ "Back"
       │           └──────────────────┐
       │                              ▼
       │                         (Home Screen)
       │
       └─────────────────────────────┐
                                     │
                                     ▼
                             Back to Active Session
                             (with same settings)
```

### Benefits of New Flow:
1. ✅ Direct access to practice session from home
2. ✅ Settings summary visible on Ready Screen
3. ✅ Easy access to Config via button if changes needed
4. ✅ Quick "Repeat" option after completion
5. ✅ Config accessible from Completed Screen
6. ✅ Reduced friction for repeated practice sessions
7. ✅ Settings persisted automatically via DataStore

---

## Screen Mockups

### Ready Screen (New Design)

```
┌─────────────────────────────────────┐
│  ← Practice Session                 │
├─────────────────────────────────────┤
│                                     │
│         Ready to Practice!          │
│                                     │
│   Play each note as it appears.    │
│   Take your time and focus on      │
│          accuracy.                  │
│                                     │
│   ┌───────────────────────────┐   │
│   │ Practice Settings          │   │
│   │                            │   │
│   │ Strings: 1, 2, 3, 4, 5, 6 │   │
│   │ Fret Range: 0–12          │   │
│   │ Note Mode: Whole Notes     │   │
│   │ Duration: 5 minutes        │   │
│   │ Progression: Manual        │   │
│   └───────────────────────────┘   │
│                                     │
│   ┌───────────────────────────┐   │
│   │    Start Practice          │   │
│   └───────────────────────────┘   │
│                                     │
│   ┌─────────────┐ ┌─────────────┐ │
│   │    Back     │ │   Config    │ │
│   └─────────────┘ └─────────────┘ │
│                                     │
└─────────────────────────────────────┘
```

### Completed Screen (New Design)

```
┌─────────────────────────────────────┐
│  ← Practice Session                 │
├─────────────────────────────────────┤
│                                     │
│        Session Complete!            │
│                                     │
│   ┌───────────────────────────┐   │
│   │                            │   │
│   │     Notes Played           │   │
│   │         42                 │   │
│   │                            │   │
│   │     Total Time             │   │
│   │        5:23                │   │
│   │                            │   │
│   └───────────────────────────┘   │
│                                     │
│   ┌───────────────────────────┐   │
│   │       Repeat               │   │
│   └───────────────────────────┘   │
│                                     │
│   ┌─────────────┐ ┌─────────────┐ │
│   │   Config    │ │    Back     │ │
│   └─────────────┘ └─────────────┘ │
│                                     │
└─────────────────────────────────────┘
```

---

## Implementation Notes

### Navigation Changes
- Home → `practiceSession` (was: Home → `practice`)
- Config still accessible via button
- PracticeSessionScreen loads config from repository automatically

### State Management
- Config loaded from PracticeSettingsRepository via PracticeConfigViewModel
- Config persisted using DataStore (existing mechanism)
- ViewModel recreated when config changes (using key parameter)

### Button Layout Pattern
- Primary action: Full-width button (70%)
- Secondary actions: Side-by-side buttons in Row (50% each)
- Consistent across Ready and Completed screens
