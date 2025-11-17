# Design Consistency Feature Issues

This directory contains pre-written issue descriptions for implementing consistent dark guitar background design across all app screens.

> **⚠️ ACTION REQUIRED**: These are issue descriptions that need to be **created as actual GitHub issues**. GitHub Copilot cannot create issues directly, so you (the repository owner) need to create them using one of the methods below.

## How to Create GitHub Issues

These markdown files contain complete issue descriptions that **you (the repository owner) need to create** as GitHub issues. GitHub Copilot created these descriptions but cannot create the actual issues due to permission limitations.

Choose one of the methods below:

### Method 1: Automated Script (Easiest - Recommended)

Run the provided script to create all issues at once:

```bash
cd docs/issues
./create-issues.sh
```

**Prerequisites:**
- GitHub CLI (`gh`) installed: https://cli.github.com/
- Authenticated with your GitHub account: `gh auth login`

This will create all 7 issues with proper labels and return the issue numbers. **You must run this** as the repository owner or someone with write access.

### Method 2: GitHub Web Interface

1. **Navigate to the repository on GitHub**: https://github.com/tobi01001/android-guitar-notes-learner
2. **Go to Issues** → Click "New Issue"
3. **Select "Feature Request" template**
4. **Copy and paste** the content from the corresponding markdown file in this directory
5. **Update the title** from the frontmatter (e.g., `[FEATURE] Apply Dark Guitar Background Design to All Screens`)
6. **Add labels** as specified in the frontmatter (e.g., `enhancement`, `ui/ux`, `design`, `cody-agent`)
7. **Assign** to the appropriate person or agent:
   - For design consistency issues → assign to `@copilot` or Cody agent
   - Can also be assigned to any team member
8. **Create the issue**

### Method 2: GitHub CLI (For Batch Creation)

If you have the GitHub CLI (`gh`) installed:

```bash
# Example for creating the main tracking issue
gh issue create \
  --title "[FEATURE] Apply Dark Guitar Background Design to All Screens" \
  --label "enhancement,ui/ux,design,cody-agent" \
  --body-file docs/issues/design_consistency_feature.md

# Repeat for each sub-issue
gh issue create \
  --title "[FEATURE] Apply Dark Guitar Background to Practice Config Screen" \
  --label "enhancement,ui/ux,design,cody-agent,priority-1" \
  --body-file docs/issues/design_practice_config_screen.md
```

### Method 3: GitHub CLI Manual Creation

Create issues individually with custom options:

```bash
# Example: Create main tracking issue
gh issue create \
  --repo tobi01001/android-guitar-notes-learner \
  --title "[FEATURE] Apply Dark Guitar Background Design to All Screens" \
  --label "enhancement,ui/ux,design,cody-agent" \
  --assignee "@copilot" \
  --body-file design_consistency_feature.md

# Example: Create a sub-issue
gh issue create \
  --repo tobi01001/android-guitar-notes-learner \
  --title "[FEATURE] Apply Dark Guitar Background to Practice Config Screen" \
  --label "enhancement,ui/ux,design,cody-agent,priority-1" \
  --assignee "@copilot" \
  --body-file design_practice_config_screen.md
```

## Issue Files

### Main Tracking Issue
- **design_consistency_feature.md** - Coordinates the entire design consistency effort
  - Links all sub-issues
  - Provides implementation strategy and guidelines

### Priority 1: Configuration and Settings Screens
Simple layouts - implement first to establish patterns:
- **design_practice_config_screen.md** - Practice Configuration Screen
- **design_settings_screen.md** - Settings Screen

### Priority 2: Active Session Screens
Real-time processing - require careful testing:
- **design_tuner_screen.md** - Tuner Screen
- **design_notes_played_screen.md** - Notes Played Screen
- **design_practice_session_screen.md** - Practice Session Screen

### Priority 3: Supporting Screens
- **design_permission_screen.md** - Permission Rationale Dialog

## Assigning Issues

Once created, issues can be assigned to:
- **@copilot** or **Cody agent** - For automated implementation
- **Team members** - For manual implementation
- **External contributors** - If open for community contributions

The issues are labeled with `cody-agent` to indicate they're suitable for AI agent implementation.

## Linking Sub-Issues

After creating all issues:
1. Note the issue numbers for each sub-issue
2. Edit the main tracking issue (design_consistency_feature.md)
3. Update the sub-issue references from `#TBD` to actual issue numbers
4. This creates a clear hierarchy and tracking structure

## Documentation References

Each issue references:
- **Design Guidelines**: `/docs/development/APP_DESIGN_GUIDELINES.md`
- **Implementation Guide**: `/docs/development/DESIGN_CONSISTENCY_IMPLEMENTATION_GUIDE.md`

## Questions?

If you have questions about:
- **Creating issues**: See methods above
- **Design decisions**: Review APP_DESIGN_GUIDELINES.md
- **Implementation**: Check DESIGN_CONSISTENCY_IMPLEMENTATION_GUIDE.md
- **Assignment**: Issues can be assigned to anyone including @copilot
