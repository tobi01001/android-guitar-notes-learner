# Fix for Low E String Frequency Detection Issue

## Problem Statement

When playing E2 (82.4 Hz), the app was detecting 87.33 Hz and reporting F instead of E (one semitone higher). This systematic error was present across all pitch detection algorithms (YIN+FFT, YIN enhanced, YIN Multi-Period, YIN adaptive, YIN, Autocorrelation), indicating a systematic error in the audio processing implementation rather than an algorithm-specific issue.

## Root Cause Analysis

### Investigation Process

1. **Frequency Analysis**
   - Expected: 82.4 Hz (E2)
   - Detected: 87.33 Hz (F)
   - Ratio: 87.33 / 82.4 = 1.0598 ≈ 2^(1/12) (exactly one semitone)
   - This suggested a systematic bias rather than random error

2. **High-Pass Filter Impact**
   - The audio pipeline applies a one-pole IIR high-pass filter to remove low-frequency rumble
   - Original cutoff: 60 Hz
   - E2 frequency: 82.41 Hz (only 1.37x the cutoff)

3. **Phase Distortion at 60 Hz Cutoff**
   - At 82.41 Hz with 60 Hz cutoff:
     - Magnitude: 0.8131 (-1.80 dB attenuation)
     - **Phase shift: 35.94 degrees (0.627 radians)**
     - Group delay: 40.44 samples
   
4. **Impact on Pitch Detection**
   - The phase shift distorts the waveform shape
   - Autocorrelation and YIN algorithms rely on finding periodic patterns
   - Phase distortion causes the correlation peak to appear at a shifted location
   - This results in detecting a shorter period (higher frequency)

### Technical Details

The one-pole IIR high-pass filter transfer function:
```
H(z) = (1 - z^-1) / (1 - α·z^-1)
```

Where α is calculated from the cutoff frequency:
```
α = RC / (RC + dt)
RC = 1 / (2π·fc)
dt = 1 / sampleRate
```

At frequencies close to the cutoff, the filter introduces significant phase shift, which distorts the waveform in a way that affects period detection.

## Solution

**Lower the high-pass filter cutoff from 60 Hz to 40 Hz.**

### Comparison: 60 Hz vs 40 Hz Cutoff

| Parameter | 60 Hz Cutoff | 40 Hz Cutoff | Improvement |
|-----------|--------------|--------------|-------------|
| Attenuation at E2 | -1.80 dB | -0.89 dB | 50% less attenuation |
| Phase shift at E2 | 35.94° | 25.83° | 28% less phase distortion |
| Frequency ratio | 1.37x | 2.06x | 50% more separation |

### Benefits of 40 Hz Cutoff

1. **Accurate E2 Detection**
   - Minimal phase distortion: only 26° vs 36°
   - Minimal amplitude loss: only -0.9 dB vs -1.8 dB
   - E2 is now 2.06x the cutoff (vs 1.37x)

2. **Still Removes Rumble**
   - Effectively attenuates frequencies below 40 Hz
   - Handles noise, bumps, environmental rumble
   - DC offset and subsonic content removed

3. **Safe for All Guitar Strings**
   - Lowest note (E2) at 82 Hz is well above 40 Hz
   - All other strings unaffected
   - High E (329 Hz) essentially unaffected

## Implementation

### Files Changed

1. **AudioRecorder.kt**
   - Changed cutoff from 60.0 to 40.0
   - Updated documentation

2. **HighPassFilter.kt**
   - Changed default parameter from 60.0 to 40.0
   - Updated class documentation

3. **HighPassFilterTest.kt**
   - Updated test expectations for 40 Hz cutoff
   - Added test comparing 40 Hz vs 60 Hz impact on E2

4. **LowEDetectionTest.kt** (new)
   - Comprehensive test for E2 detection across all algorithms

### Code Changes

```kotlin
// Before
val highPassFilter = HighPassFilter(sampleRate = SAMPLE_RATE, cutoffFrequency = 60.0)

// After
val highPassFilter = HighPassFilter(sampleRate = SAMPLE_RATE, cutoffFrequency = 40.0)
```

## Testing

### Unit Tests

All existing tests pass with the new 40 Hz cutoff:
- ✅ AudioRecorder tests
- ✅ HighPassFilter tests
- ✅ PitchDetector tests (all algorithms)
- ✅ YinPitchDetector tests
- ✅ NoteRecognizer tests

### New Tests Added

1. **High-pass filter comparison test**
   - Verifies 40 Hz has less attenuation than 60 Hz at E2
   - Confirms 40 Hz cutoff has <15% attenuation

2. **Low E detection test**
   - Tests E2 (82.4 Hz) detection across all algorithms
   - Verifies accuracy within acceptable tolerance

### Expected Improvements

With the 40 Hz cutoff, E2 detection should now be:
- Within ±5 Hz accuracy (vs previous ±7 Hz systematic error)
- Consistent across all pitch detection algorithms
- No longer systematically detecting F when E is played

## Verification Steps

To verify the fix resolves the issue:

1. **Record E2 (82.4 Hz) tone**
   - Use tone generator or actual guitar low E string
   - Play for 2-3 seconds

2. **Check detected frequency**
   - Should detect ~82 Hz (±5 Hz)
   - Should recognize as "E2" not "F2"
   - Should be consistent across algorithm selections

3. **Test all guitar strings**
   - Low E (82 Hz) - should detect E
   - A (110 Hz) - should detect A
   - D (147 Hz) - should detect D
   - G (196 Hz) - should detect G
   - B (247 Hz) - should detect B
   - High E (330 Hz) - should detect E

## References

- Issue: "Fix systematic frequency detection for low E string"
- One-pole IIR high-pass filter theory
- Phase response of first-order filters
- YIN pitch detection algorithm (De Cheveigné & Kawahara, 2002)

## Related Documents

- `AUDIO_DETECTION_ANALYSIS.md` - Overall audio processing analysis
- `ISSUE_68_ANALYSIS.md` - Previous audio reception improvements
- `ISSUE_84_IMPLEMENTATION.md` - YIN algorithm enhancements
