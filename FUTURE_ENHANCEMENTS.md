# Future Enhancements - Tracking and Planning

## Purpose

This document serves as a central index for tracking future feature ideas and enhancements for the Android Guitar Notes Learner project. Each enhancement is documented in detail with priority levels, implementation estimates, and technical specifications.

## Overview

As the project matures, we continuously identify opportunities for improvements that would enhance the user experience, improve detection accuracy, or add new capabilities. This document tracks these ideas to ensure they are properly evaluated and prioritized.

## Enhancement Index

### Audio Processing & Detection Enhancements

| ID | Title | Priority | Status | Document |
|---|---|---|---|---|
| ENH-001 | Multi-Frame Pitch Confirmation | Optional/Low | Not Started | [ENHANCEMENT_001_MULTI_FRAME_CONFIRMATION.md](ENHANCEMENT_001_MULTI_FRAME_CONFIRMATION.md) |
| ENH-002 | Harmonic Consistency Checks for Octave Disambiguation | Priority 2 | Not Started | [ENHANCEMENT_002_HARMONIC_CONSISTENCY.md](ENHANCEMENT_002_HARMONIC_CONSISTENCY.md) |

### Priority Definitions

- **Priority 1**: Critical improvements required for production quality (All completed ✅)
- **Priority 2**: Important enhancements that significantly improve user experience or accuracy
- **Priority 3**: Nice-to-have features for advanced use cases
- **Optional/Low**: Improvements that provide marginal benefits or are only useful in specific scenarios

## Current Implementation Status

### ✅ Completed (Priority 1)

All Priority 1 improvements have been successfully implemented:

1. ✅ Auto-adjust sensitivity (AGC) - Implemented in PR #58
2. ✅ High-pass filter (60 Hz) - Implemented in PR #58
3. ✅ Configurable noise gate - Implemented in PR #58

### 📋 Planned (Priority 2)

These enhancements are documented and ready for implementation consideration:

1. ❌ **Harmonic Consistency Checks** (ENH-002) - Better octave disambiguation
2. ❌ **Parabolic Interpolation** - Improved tuner accuracy (±0.1 Hz)
3. ❌ **Configurable Correlation Threshold** - User-adjustable detection sensitivity
4. ❌ **Dynamic Lag Range Optimization** - 15-25% performance improvement

### 🔮 Future (Priority 3 & Optional)

Long-term enhancements requiring significant effort:

1. ❌ **Multi-Frame Pitch Confirmation** (ENH-001) - Extra detection stability
2. ❌ **Hybrid Autocorrelation + FFT** - More robust detection
3. ❌ **Polyphonic Detection** - Chord recognition capability
4. ❌ **Machine Learning-based Pitch Detection** - State-of-the-art accuracy

## How to Use This Document

### Adding a New Enhancement

1. Create a new enhancement document using the template below
2. Assign the next available ID (ENH-XXX)
3. Add an entry to the Enhancement Index table
4. Link the document in the appropriate priority section

### Enhancement Document Template

Each enhancement should be documented in a separate markdown file with:

- **Overview**: What is the enhancement and why is it needed?
- **Priority & Rationale**: Why this priority level?
- **Technical Approach**: How would it be implemented?
- **Implementation Effort**: Estimated time and complexity
- **Benefits & Impact**: What improvements would users see?
- **Risks & Considerations**: What challenges or trade-offs exist?
- **Dependencies**: What needs to be in place first?
- **References**: Related issues, code, or documentation

### Promoting an Enhancement

When an enhancement is ready to be implemented:

1. Create a GitHub issue referencing the enhancement document
2. Update the Status column in the Enhancement Index
3. Assign the issue to a milestone
4. Link the enhancement document in the issue description

## Related Documentation

- [AUDIO_DETECTION_ANALYSIS.md](AUDIO_DETECTION_ANALYSIS.md) - Comprehensive audio system analysis
- [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md) - Summary of implemented features
- [ISSUE_68_REVIEW_README.md](ISSUE_68_REVIEW_README.md) - Review of audio improvements

## Contributing

Have an idea for a future enhancement? 

1. Review existing enhancements to avoid duplication
2. Consider the priority level based on impact vs effort
3. Create a detailed enhancement document following the template
4. Submit via pull request or create a GitHub issue for discussion

---

**Last Updated:** 2025-11-13  
**Maintainer:** Project Contributors  
**Status:** Active - continuously updated as new ideas emerge
