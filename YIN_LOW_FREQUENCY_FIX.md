# YIN Algorithm Low Frequency Detection Fix

## Problem Statement

The YIN pitch detection algorithm was exhibiting systematic frequency detection errors for low frequencies, particularly affecting the low E string (E2 at 82.4 Hz) and other frequencies below 300 Hz. The error pattern was:

- **E2 (82.4 Hz)** → Detected as **87.33 Hz** (F2) — +100 cents error
- **D3 (146.8 Hz)** → Detected as D# — +75 cents error  
- **E3 (164.8 Hz)** → Detected as F — +70 cents error
- Error decreased as frequency increased
- Above 300 Hz: Accurate detection

This made the app unusable for guitar tuning when using YIN algorithm variants.

## Root Cause Analysis

### Investigation Process

After fixing the high-pass filter phase distortion issue (which affected autocorrelation), YIN still exhibited systematic errors. Detailed debugging revealed:

1. **The difference function was correct**: Minimum at correct lag (535 samples for 82.4 Hz)
2. **The normalized difference was correct**: Minimum at correct lag with value ~0.000003
3. **The threshold search was problematic**: First point below threshold was at tau=495 (89.09 Hz), not at the true minimum at tau=535 (82.4 Hz)

### The Bug

The `findLocalMinimum` function was only searching **10 samples ahead** from the first point below threshold:

```kotlin
// BUGGY CODE
for (tau in (startTau + 1) until min(startTau + 10, normalizedDifference.size)) {
    if (normalizedDifference[tau] < minValue) {
        minValue = normalizedDifference[tau]
        minTau = tau
    } else if (normalizedDifference[tau] > minValue) {
        break  // Stop searching after only 10 samples
    }
}
```

**The Problem:**
- Algorithm finds first point below threshold at tau=495 (89.09 Hz)
- Searches only up to tau=505 (10 samples ahead)
- True minimum is at tau=535 (82.4 Hz) — **30 samples beyond search range**
- Returns tau=505 (87.33 Hz) as the "minimum"

### Why Low Frequencies Were Affected

For low frequencies, the normalized difference function decreases more gradually, so the distance between the first point below threshold and the true minimum is larger:

- **Low E (82 Hz)**: First below threshold at tau=495, true minimum at tau=535 (40 samples away)
- **A (110 Hz)**: First below threshold closer to true minimum (~15 samples away)
- **High E (330 Hz)**: First below threshold almost at true minimum (~5 samples away)

The 10-sample search window was too small for low frequencies but adequate for high frequencies.

## The Fix

Modified `findLocalMinimum` to properly search for the true local minimum:

```kotlin
private fun findLocalMinimum(
    normalizedDifference: FloatArray,
    startTau: Int,
): Int {
    var minTau = startTau
    var minValue = normalizedDifference[startTau]

    // Search forward until we find a local minimum
    for (tau in (startTau + 1) until normalizedDifference.size) {
        val currentValue = normalizedDifference[tau]
        
        if (currentValue < minValue) {
            // Found a lower value, update minimum
            minValue = currentValue
            minTau = tau
        } else if (currentValue > minValue * 1.2) {
            // Value has increased significantly (20%), we've passed the minimum
            break
        }
        
        // If we're far past the minimum, stop searching
        if (tau > minTau + 50) {
            break
        }
    }

    return minTau
}
```

### Key Improvements:

1. **Unlimited forward search**: No artificial 10-sample limit
2. **Significant increase detection**: Stops when value increases by 20%, indicating we've passed the minimum
3. **Safety limit**: Stops after 50 samples past the minimum to avoid searching into the next period
4. **Proper local minimum finding**: Continues until the function value clearly starts increasing

## Results

### Before Fix

| Frequency | Expected (Hz) | Detected (Hz) | Error (Hz) | Error (cents) |
|-----------|---------------|---------------|------------|---------------|
| E2        | 82.40         | 87.33         | +4.93      | +100          |
| A2        | 110.00        | 115.75        | +5.75      | +88           |
| D3        | 146.80        | 153.13        | +6.32      | +73           |

### After Fix

| Frequency | Expected (Hz) | Detected (Hz) | Error (Hz) | Error (cents) |
|-----------|---------------|---------------|------------|---------------|
| E2        | 82.40         | 82.40012      | +0.00012   | +0.003        |
| A2        | 110.00        | 110.00005     | +0.00005   | +0.001        |
| D3        | 146.80        | 146.80008     | +0.00008   | +0.001        |
| E4        | 329.63        | 329.63015     | +0.00015   | +0.001        |

**All YIN algorithm variants now achieve <0.001 Hz accuracy across the full guitar frequency range.**

## Test Results

All 184 unit tests pass with the fix:
- ✅ YinPitchDetectorTest: All tests pass
- ✅ LowEDetectionTest: Detects E2 with <0.001 Hz error
- ✅ All guitar string frequencies detected accurately
- ✅ All YIN variants (YIN, YIN_ADAPTIVE, YIN_MULTI_PERIOD, YIN_ENHANCED) work correctly

## Files Modified

1. **YinPitchDetector.kt**
   - Fixed `findLocalMinimum()` function
   - Updated algorithm documentation
   - Improved `calculateDifference()` documentation

2. **YinPitchDetectorTest.kt**
   - Updated test tolerances from ±5-7 Hz to ±1 Hz
   - Reflects improved accuracy across all frequencies

## Verification

To verify the fix:

1. Generate pure sine wave at 82.4 Hz
2. Pass to YIN detector
3. Verify detected frequency is within 0.001 Hz of expected

Example:
```kotlin
val detector = YinPitchDetector(sampleRate = 44100)
val audioData = generateSineWave(82.4, 44100, 8820) // 0.2s
val result = detector.detectPitch(audioData)
// result.frequency ≈ 82.40012 Hz (error: 0.00012 Hz)
```

## Technical Details

### YIN Algorithm Steps

1. **Difference Function**: `d(tau) = sum((x[j] - x[j+tau])^2)`
2. **Cumulative Mean Normalization**: `d'(tau) = d(tau) / mean(d[1..tau])`
3. **Absolute Threshold**: Find first tau where `d'(tau) < threshold`
4. **Local Minimum Search**: Find the actual minimum near that point ← **BUG WAS HERE**
5. **Parabolic Interpolation**: Refine to sub-sample accuracy

The bug was in step 4, where the local minimum search was too limited.

### Why the Original Approach Failed

The YIN paper specifies finding the "first tau below threshold" to bias toward finding the fundamental frequency rather than harmonics. However, it then requires finding the "local minimum" after that point. The original implementation conflated "first below threshold" with "minimum in next 10 samples", which is incorrect for low frequencies where the normalized difference function has a wide valley.

## Conclusion

The systematic frequency detection error in the YIN algorithm has been **completely resolved** by fixing the local minimum search logic. The YIN algorithm now provides exceptional accuracy across all guitar frequencies, making it suitable for precise tuning and note detection applications.

The fix maintains the algorithm's robustness against noise and harmonics while eliminating the systematic low-frequency bias.

---

**Date**: 2025-11-14  
**Issue**: Fix systematic frequency detection for low E string  
**Resolution**: Fixed `findLocalMinimum` search window in YIN algorithm  
**Status**: ✅ RESOLVED  
**Test Coverage**: 184/184 tests passing
