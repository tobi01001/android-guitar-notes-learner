# Issue Templates

This directory contains GitHub issue templates for the Android Guitar Notes Learner project.

## Available Templates

### General Templates
- **bug_report.md** - Report bugs and issues
- **feature_request.md** - Suggest general features

### Design Consistency Templates
A comprehensive set of templates for implementing consistent dark guitar background design across all app screens.

#### Main Tracking Issue
- **design_consistency_feature.yml** - Master feature request tracking the overall design consistency effort
  - Use this to create the main tracking issue
  - References all sub-issues
  - Provides implementation strategy and guidelines

#### Screen-Specific Sub-Issues

**Priority 1 - Simple Layouts** (implement first to establish patterns):
- **design_practice_config_screen.yml** - Practice Configuration Screen
- **design_settings_screen.yml** - Settings Screen

**Priority 2 - Active Sessions** (require careful attention to real-time processing):
- **design_tuner_screen.yml** - Tuner Screen
- **design_notes_played_screen.yml** - Notes Played Screen
- **design_practice_session_screen.yml** - Practice Session Screen

**Priority 3 - Supporting Screens**:
- **design_permission_screen.yml** - Permission Rationale Screen

## How to Use

1. **Navigate to Issues** in the GitHub repository
2. **Click "New Issue"**
3. **Select the appropriate template**
4. **Fill in any additional details**
5. **Assign** to the appropriate person or agent (design issues → Cody agent)
6. **Link related issues** (sub-issues should reference the main tracking issue)

## Design Consistency Feature

The design consistency templates work together as a coordinated effort:

1. **Start** by creating an issue from `design_consistency_feature.yml`
2. **Create sub-issues** for each screen using the screen-specific templates
3. **Reference** the main tracking issue in each sub-issue
4. **Follow** the implementation order by priority
5. **Refer** to `/docs/development/APP_DESIGN_GUIDELINES.md` for design specifications

Each template includes:
- Clear problem statement and goals
- Detailed implementation tasks
- Code examples and patterns
- Design guideline references
- Acceptance criteria
- Comprehensive testing checklists

## Documentation

For more information:
- **Design Guidelines**: `/docs/development/APP_DESIGN_GUIDELINES.md`
- **Implementation Guide**: `/docs/development/DESIGN_CONSISTENCY_IMPLEMENTATION_GUIDE.md`

## Labels

Design consistency issues use these labels:
- `enhancement` - Feature enhancement
- `ui/ux` - User interface/experience
- `design` - Design-related
- `cody-agent` - Recommended for Cody agent
- `priority-1`, `priority-2`, `priority-3` - Implementation priority

## Questions?

If you have questions about:
- **Using templates**: Review this README
- **Design decisions**: See APP_DESIGN_GUIDELINES.md
- **Implementation**: Check the relevant template or DESIGN_CONSISTENCY_IMPLEMENTATION_GUIDE.md
