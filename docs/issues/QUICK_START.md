# Quick Start: Creating Design Consistency Issues

## TL;DR

Run this command to create all 7 GitHub issues at once:

```bash
cd docs/issues
./create-issues.sh
```

## What You Need

1. **GitHub CLI** installed: https://cli.github.com/
2. **Authenticated** with your GitHub account: `gh auth login`
3. **Write access** to the repository

## What Will Happen

The script will create 7 GitHub issues:

1. **Main Tracking Issue** - `[FEATURE] Apply Dark Guitar Background Design to All Screens`
2. **Priority 1 Issues** (2):
   - Practice Configuration Screen
   - Settings Screen
3. **Priority 2 Issues** (3):
   - Tuner Screen
   - Notes Played Screen
   - Practice Session Screen
4. **Priority 3 Issue** (1):
   - Permission Screen

All issues will have proper labels: `enhancement`, `ui/ux`, `design`, `cody-agent`, and priority labels.

## After Creating Issues

1. **Note the issue numbers** returned by the script
2. **Assign issues** to @copilot, Cody agent, or team members
3. **Link sub-issues** to the main tracking issue
4. **Follow implementation order**: Priority 1 → Priority 2 → Priority 3

## Alternative: Manual Creation

If you prefer to create issues one-by-one via GitHub web interface:

1. Go to https://github.com/tobi01001/android-guitar-notes-learner/issues/new
2. Select "Feature Request" template
3. Copy content from one of the markdown files in this directory
4. Update title and labels from the file's frontmatter
5. Create and assign the issue

## Need Help?

- Full instructions: See `README.md` in this directory
- Design guidelines: `/docs/development/APP_DESIGN_GUIDELINES.md`
- Implementation guide: `/docs/development/DESIGN_CONSISTENCY_IMPLEMENTATION_GUIDE.md`

## Why These Are Needed

These issues implement consistent dark guitar background design across all app screens, matching the beautiful home screen aesthetic. The design guidelines are already documented - now we just need to apply them!
