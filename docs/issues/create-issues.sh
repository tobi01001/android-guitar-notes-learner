#!/bin/bash
# Script to create all design consistency GitHub issues
# Prerequisites: 
#   - GitHub CLI (gh) installed
#   - Authenticated with: gh auth login

set -e

REPO="tobi01001/android-guitar-notes-learner"

echo "Creating design consistency issues for $REPO"
echo "=============================================="
echo ""

# Function to extract title from markdown frontmatter
get_title() {
    grep "^title:" "$1" | sed 's/title: //'
}

# Function to extract labels from markdown frontmatter
get_labels() {
    grep "^labels:" "$1" | sed 's/labels: //'
}

# Main tracking issue
echo "Creating main tracking issue..."
MAIN_ISSUE=$(gh issue create \
  --repo "$REPO" \
  --title "$(get_title design_consistency_feature.md)" \
  --label "$(get_labels design_consistency_feature.md)" \
  --body-file design_consistency_feature.md)
echo "✓ Created: $MAIN_ISSUE"
MAIN_ISSUE_NUM=$(echo "$MAIN_ISSUE" | grep -oP '\d+$')
echo ""

# Priority 1 issues
echo "Creating Priority 1 issues..."
gh issue create \
  --repo "$REPO" \
  --title "$(get_title design_practice_config_screen.md)" \
  --label "$(get_labels design_practice_config_screen.md),priority-1" \
  --body-file design_practice_config_screen.md \
  && echo "✓ Created: Practice Config Screen issue"

gh issue create \
  --repo "$REPO" \
  --title "$(get_title design_settings_screen.md)" \
  --label "$(get_labels design_settings_screen.md),priority-1" \
  --body-file design_settings_screen.md \
  && echo "✓ Created: Settings Screen issue"
echo ""

# Priority 2 issues
echo "Creating Priority 2 issues..."
gh issue create \
  --repo "$REPO" \
  --title "$(get_title design_tuner_screen.md)" \
  --label "$(get_labels design_tuner_screen.md),priority-2" \
  --body-file design_tuner_screen.md \
  && echo "✓ Created: Tuner Screen issue"

gh issue create \
  --repo "$REPO" \
  --title "$(get_title design_notes_played_screen.md)" \
  --label "$(get_labels design_notes_played_screen.md),priority-2" \
  --body-file design_notes_played_screen.md \
  && echo "✓ Created: Notes Played Screen issue"

gh issue create \
  --repo "$REPO" \
  --title "$(get_title design_practice_session_screen.md)" \
  --label "$(get_labels design_practice_session_screen.md),priority-2" \
  --body-file design_practice_session_screen.md \
  && echo "✓ Created: Practice Session Screen issue"
echo ""

# Priority 3 issue
echo "Creating Priority 3 issue..."
gh issue create \
  --repo "$REPO" \
  --title "$(get_title design_permission_screen.md)" \
  --label "$(get_labels design_permission_screen.md),priority-3" \
  --body-file design_permission_screen.md \
  && echo "✓ Created: Permission Screen issue"
echo ""

echo "=============================================="
echo "All issues created successfully!"
echo ""
echo "Main tracking issue: #$MAIN_ISSUE_NUM"
echo ""
echo "Next steps:"
echo "1. Link sub-issues to main tracking issue #$MAIN_ISSUE_NUM"
echo "2. Assign issues to @copilot or team members"
echo "3. Follow implementation order by priority (1 → 2 → 3)"
