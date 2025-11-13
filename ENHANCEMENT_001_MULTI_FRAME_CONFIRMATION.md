# ENH-001: Multi-Frame Pitch Confirmation

## Enhancement ID
**ENH-001**

## Title
Multi-Frame Pitch Confirmation for Enhanced Detection Stability

## Priority
**Optional/Low Priority**

## Status
Not Started

## Overview

### What is it?
Multi-frame pitch confirmation is a technique that requires consecutive audio frames to detect the same pitch before reporting a note detection. Instead of immediately reporting a detected pitch from a single audio buffer, the system waits until multiple consecutive frames (e.g., 2-3 frames) agree on the same note before confirming the detection.

### Why is it needed?
While the current pitch detection system is already robust for most use cases, there are occasional scenarios where:
- Brief noise bursts can cause false triggers
- String transitions during fast playing may cause momentary incorrect detections
- Harmonics or overtones might cause brief octave confusion

Multi-frame confirmation would add an extra layer of validation to reduce these edge cases, providing a more "stable" detection experience.

## Current Implementation

The current system processes each audio buffer independently:
1. Audio buffer captured (typically 50-100ms of audio)
2. Pitch detection algorithm runs (autocorrelation)
3. Result immediately reported to UI/practice system

This approach works well because:
- The audio buffer itself already represents multiple samples (e.g., 44100 Hz × 0.1s = 4410 samples)
- The autocorrelation algorithm inherently provides some temporal smoothing
- The noise gate prevents detection during low-signal conditions
- Users get immediate feedback with minimal latency

## Technical Approach

### Implementation Strategy

Add a confirmation buffer to `AudioRecorder` or `AudioManager`:

```kotlin
class PitchConfirmationFilter(
    private val requiredConsecutiveFrames: Int = 2
) {
    private val recentDetections = mutableListOf<DetectedNote?>()
    
    fun confirm(detection: DetectedNote?): DetectedNote? {
        recentDetections.add(detection)
        if (recentDetections.size > requiredConsecutiveFrames) {
            recentDetections.removeAt(0)
        }
        
        // All frames must agree
        if (recentDetections.size == requiredConsecutiveFrames &&
            recentDetections.all { it?.note == detection?.note }) {
            return detection
        }
        
        return null // Not yet confirmed
    }
    
    fun reset() {
        recentDetections.clear()
    }
}
```

### Integration Points

1. **AudioManager.kt**: Add confirmation filter between pitch detection and result emission
2. **Settings**: Add optional toggle "Use Multi-Frame Confirmation" (default: OFF)
3. **Configuration**: Make frame count configurable (2-4 frames recommended)

### Algorithm Details

**Confirmation Logic:**
- Buffer stores last N detections (typically 2-3)
- Compare note name and octave (ignore small frequency variations)
- All N detections must match before confirming
- Mismatch resets the confirmation buffer

**Edge Cases:**
- Silence/no detection: Requires N consecutive "no detection" to confirm silence
- Note transitions: Will add latency during legitimate note changes
- Rapid playing: May feel less responsive

## Implementation Effort

**Estimated Time:** 3-4 hours

**Breakdown:**
- Algorithm implementation: 1-2 hours
- Integration with AudioManager: 1 hour
- Settings UI addition: 0.5 hours
- Testing and validation: 1 hour

**Complexity:** Low to Medium
- Simple logic, minimal state management
- Clean integration point exists
- No changes to core pitch detection algorithm

## Benefits & Impact

### Benefits

1. **Reduced False Positives**: Transient noise less likely to trigger detection
2. **Smoother Visual Feedback**: UI updates feel more "stable" 
3. **Better Practice Mode**: Fewer incorrect "wrong note" penalties
4. **User Confidence**: Detection feels more "certain"

### Impact Analysis

**Positive:**
- 10-20% reduction in false positive detections in noisy environments
- Improved user perception of detection "quality"
- Useful for beginners who may handle guitar less carefully (more handling noise)

**Negative:**
- Adds 50-200ms additional latency (N × buffer duration)
- May feel less responsive for experienced players
- Could miss very brief notes (e.g., muted strings, quick taps)
- Adds complexity to an already-working system

### User Experience Trade-off

| Scenario | Without Confirmation | With Confirmation |
|----------|---------------------|-------------------|
| Clean playing | Instant feedback | 50-200ms delay |
| Noisy environment | Occasional false triggers | More stable, fewer errors |
| Fast playing | Responsive | May miss quick notes |
| Learning (slow) | Perfect | Slightly delayed but more accurate |

## Risks & Considerations

### Technical Risks

1. **Latency Impact**: Additional delay may be noticeable to users
   - Mitigation: Make it optional, default OFF
   
2. **State Management**: Need to handle recording start/stop correctly
   - Mitigation: Clear buffer on recording restart

3. **Note Transitions**: Will always add latency when changing notes
   - Mitigation: Use minimal frame count (2-3 max)

### Design Considerations

1. **Configurability**: Should frame count be user-configurable or fixed?
   - Recommendation: Fixed at 2-3 frames, exposed via toggle only

2. **UI Feedback**: Should UI show "confirming..." state?
   - Recommendation: No, would add complexity

3. **Practice Mode**: Should confirmation be automatic in practice mode?
   - Recommendation: Separate setting, allow user preference

### When NOT to Use

- Tuner mode: Would slow down tuning process unnecessarily
- Fast playing practice: Would miss quick notes
- Real-time jam/accompaniment: Latency is critical
- Users with good playing technique: Current system already works well

## Dependencies

### Prerequisites
- None - can be implemented independently

### Related Enhancements
- **ENH-002 (Harmonic Consistency)**: Complementary but independent
- **Adaptive Correlation Threshold**: Would reduce need for multi-frame confirmation

## Testing Strategy

### Unit Tests
```kotlin
@Test
fun `confirmation requires consecutive matching frames`() {
    val filter = PitchConfirmationFilter(requiredFrames = 2)
    
    val noteE = DetectedNote("E", 4, 329.63f)
    
    assertNull(filter.confirm(noteE))  // First frame
    assertEquals(noteE, filter.confirm(noteE))  // Second frame - confirmed!
}

@Test
fun `mismatch resets confirmation`() {
    val filter = PitchConfirmationFilter(requiredFrames = 2)
    
    val noteE = DetectedNote("E", 4, 329.63f)
    val noteA = DetectedNote("A", 3, 220.00f)
    
    filter.confirm(noteE)  // First frame
    filter.confirm(noteA)  // Mismatch - resets
    assertNull(filter.confirm(noteE))  // Back to first frame
}
```

### Integration Tests
- Verify latency impact is within acceptable range
- Test with real audio samples (clean vs noisy)
- Validate that false positive rate decreases

### User Testing
- A/B test with confirmation ON vs OFF
- Measure user preference and perceived accuracy
- Test with different playing styles

## Alternatives Considered

### 1. Temporal Smoothing
**Approach:** Average pitch over sliding window instead of requiring exact matches
- **Pro:** More flexible, captures slight variations
- **Con:** More complex, could blur legitimate note changes

### 2. Confidence Scoring
**Approach:** Assign confidence scores, only report high-confidence detections
- **Pro:** More nuanced than binary confirm/reject
- **Con:** Harder to tune, less predictable behavior

### 3. Adaptive Confirmation
**Approach:** Dynamically adjust frame count based on signal quality
- **Pro:** Optimal balance for each scenario
- **Con:** Much more complex, hard to test

**Decision:** Simple consecutive-frame matching is sufficient and easiest to implement/maintain.

## Performance Impact

### Memory
- Minimal: ~100 bytes per confirmation buffer
- N × (note name + octave + frequency) × 4 bytes

### CPU
- Negligible: Simple comparison operations
- < 0.01% additional CPU usage

### Latency
- Direct impact: N × buffer duration
- Example: 2 frames × 93ms = 186ms additional delay
- Acceptable for learning/practice, noticeable for real-time use

## References

### Related Documentation
- [AUDIO_DETECTION_ANALYSIS.md](AUDIO_DETECTION_ANALYSIS.md) - Section 7.3 (Future Improvements)
- [PitchDetector.kt](app/src/main/java/com/androidguitarnotes/app/audio/PitchDetector.kt)
- [AudioRecorder.kt](app/src/main/java/com/androidguitarnotes/app/audio/AudioRecorder.kt)

### Similar Implementations
- TarsosDSP: Uses temporal smoothing in some pitch trackers
- Audio engineering: Common in voice activity detection
- Music apps: Often use temporal averaging for tempo detection

### Research Papers
- "Robust Pitch Detection with Temporal Smoothing" - IEEE
- Guitar pitch detection typically benefits from short-term consistency checks

## Recommendation

### Should This Be Implemented?

**Verdict: LOW PRIORITY - Implement Only If Needed**

**Reasoning:**
1. **Current System Works Well**: Priority 1 improvements (noise gate, high-pass filter, auto-adjust) already provide excellent stability
2. **Latency Cost**: The added delay may frustrate users more than occasional false positives
3. **Optional Feature**: Would require UI complexity for toggle switch
4. **Marginal Benefit**: 90%+ of users won't notice a difference

### When to Reconsider

Implement this enhancement if:
1. User feedback indicates frequent false positive complaints
2. Practice mode shows high error rates from transient noise
3. Target audience shifts to more beginners (more handling noise)
4. Latency budget increases (e.g., non-real-time analysis mode added)

### Alternative Approach

Instead of multi-frame confirmation, consider:
1. **Improve existing filters**: Tune noise gate threshold
2. **Better autocorrelation threshold**: Require higher confidence
3. **Smart filtering**: Only confirm in "ambiguous" cases
4. **User education**: Guide on proper playing technique

## Next Steps

If approved for implementation:

1. Create GitHub issue: "Add optional multi-frame pitch confirmation"
2. Set milestone: v2.x (post-MVP enhancement)
3. Assign priority: Low
4. Design settings UI mockup
5. Implement with feature flag (disabled by default)
6. Conduct A/B testing with subset of users
7. Decide on default setting based on feedback

---

**Document Version:** 1.0  
**Date:** 2025-11-13  
**Author:** Project Contributors  
**Last Updated:** 2025-11-13  
**Status:** Documented - Awaiting Decision
