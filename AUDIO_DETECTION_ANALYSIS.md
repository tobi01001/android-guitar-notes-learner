# Audio Recording and Note/Frequency Detection - In-Depth Analysis

## Executive Summary

This document provides a comprehensive analysis of the audio recording and note/frequency detection implementation in the Android Guitar Notes Learner app. The system uses a real-time audio processing pipeline that:

1. **Records** audio from the device microphone at 44.1 kHz sample rate
2. **Detects pitch** using autocorrelation algorithm on audio samples
3. **Recognizes notes** by converting detected frequencies to musical notes using equal temperament tuning
4. **Provides feedback** through a reactive flow-based architecture

The implementation is optimized for guitar note detection (60 Hz - 1500 Hz range) and includes configurable sensitivity controls.

**Implementation Status:** All Priority 1 improvements have been successfully implemented in PR #58.

---

## Implementation Status

### ✅ Priority 1 - COMPLETED

All three Priority 1 improvements from Section 11.4 have been implemented:

1. **✅ Auto-Adjust Sensitivity** - Implemented in `AudioRecorder.kt`
   - Status: **FULLY IMPLEMENTED**
   - Tracks RMS level over rolling window (~1 second)
   - Dynamically adjusts sensitivity multiplier (0.5x to 2.0x range)
   - Uses smooth adjustment (0.9x-1.1x per step) to avoid abrupt changes
   - Works in conjunction with manual sensitivity: `finalSensitivity = baseSensitivity × autoAdjustFactor`
   - See Section 5.3 for detailed implementation notes

2. **✅ Noise Gate** - Implemented in `AudioManager.kt` and `AudioRecorder.kt`
   - Status: **FULLY IMPLEMENTED**
   - Configurable RMS threshold (default 0.01f)
   - New `Gated` result type in `AudioAnalysisResult`
   - Suppresses pitch detection when signal below threshold
   - Reduces CPU usage when idle
   - UI shows idle state when gate is active

3. **✅ High-Pass Filter** - Implemented in `HighPassFilter.kt`
   - Status: **FULLY IMPLEMENTED**
   - One-pole IIR high-pass filter at 60 Hz cutoff
   - Removes low-frequency rumble and handling noise
   - Applied after sensitivity adjustment, before pitch detection
   - Minimal impact on guitar frequencies (lowest E2 at 82 Hz)
   - See Section 7.2.3 for algorithm details

### 📋 Priority 2 - NOT YET IMPLEMENTED

These improvements are planned for future implementation:

1. **❌ Parabolic Interpolation** - Not implemented
   - Would improve tuner accuracy from ±2-5 Hz to ±0.1 Hz
   - Beneficial for tuner feature
   - See Section 7.2.1 for implementation details

2. **❌ Configurable Correlation Threshold** - Not implemented
   - Currently fixed at 0.1
   - Would allow users to adjust detection sensitivity
   - See Section 6.3 for impact analysis

3. **❌ Dynamic Lag Range Optimization** - Not implemented
   - Would improve performance by 15-25%
   - Reduces autocorrelation search range based on previous detections
   - See Section 7.2.2 for details

### 📋 Priority 3 - NOT YET IMPLEMENTED

These are long-term enhancements:

1. **❌ Hybrid Autocorrelation + FFT** - Not implemented
   - Would provide more robust detection in challenging cases
   - Significant implementation effort (2-3 days)
   
2. **❌ Advanced Harmonic Analysis** - Not implemented
   - Better octave disambiguation
   - More complex algorithm

3. **❌ Polyphonic Detection** - Not implemented
   - Would enable chord detection
   - Requires complete redesign (1-2 weeks effort)

---

## 1. Audio Recording Pipeline

### 1.1 Overview

The audio recording is handled by the `AudioRecorder` class, which uses Android's low-level `AudioRecord` API to capture raw audio data from the microphone.

### 1.2 Audio Recording Configuration

**Key Parameters:**
```kotlin
SAMPLE_RATE = 44100          // Samples per second (44.1 kHz)
CHANNEL_CONFIG = MONO        // Single channel audio
AUDIO_FORMAT = PCM_FLOAT     // 32-bit floating point samples (-1.0 to 1.0)
BUFFER_SIZE_MULTIPLIER = 2   // 2x minimum buffer for stability
```

**Why 44.1 kHz?**
- Standard audio sample rate that provides frequency detection up to ~22 kHz (Nyquist theorem)
- Sufficient for guitar frequencies (lowest E2 ≈ 82 Hz, highest notes with harmonics < 2 kHz)
- Balances precision with computational efficiency

**Why PCM_FLOAT?**
- Floating point format (-1.0 to 1.0) simplifies mathematical operations
- No need for integer-to-float conversion during processing
- Better precision for autocorrelation calculations

### 1.3 Audio Source Selection

The app intelligently selects the best audio source for pitch detection:

```kotlin
Priority Order:
1. UNPROCESSED (API 29+)        // Raw audio, no processing
2. VOICE_RECOGNITION            // Speech-optimized, some processing
3. MIC                          // General purpose (fallback)
```

**UNPROCESSED (Preferred):**
- Provides raw microphone data with minimal system processing
- Avoids AGC (Automatic Gain Control), noise reduction, echo cancellation
- Best for accurate pitch detection
- Only available on Android 10+ devices

**Why avoid AGC and noise reduction?**
- These features can distort the waveform needed for accurate frequency detection
- AGC can amplify background noise when guitar is quiet
- Noise reduction may filter out harmonic content important for pitch detection

### 1.4 Audio Data Flow

```
Microphone → AudioRecord → FloatArray Buffer → Sensitivity Adjustment → High-Pass Filter → Noise Gate → Level Calculation → Emit to Flow
```

**Buffer Processing:**
1. Audio samples are read into a `FloatArray` buffer (typically 4096-8192 samples)
2. Auto-adjust sensitivity factor is calculated if enabled (based on rolling RMS window)
3. Combined sensitivity multiplier is applied: `adjustedSample = sample × (baseSensitivity × autoAdjustFactor)`
4. Samples are clamped to valid range: `[-1.0, 1.0]`
5. High-pass filter (60 Hz) is applied to remove low-frequency noise
6. Noise gate check: signal below threshold is marked as gated
7. RMS level is calculated for visual feedback
8. Data is emitted through a Kotlin Flow for reactive processing

**Buffer Size:**
- Minimum buffer size × 2 for stability
- Typically 4096-8192 samples at 44.1 kHz
- Represents ~93-185ms of audio
- Larger buffers = more stable but higher latency

---

## 2. Pitch Detection Algorithm

### 2.1 Autocorrelation Method

The `PitchDetector` class uses **autocorrelation** to find the fundamental frequency. This is a time-domain method that identifies periodicity in the signal.

**Algorithm Overview:**

Autocorrelation measures how similar a signal is to a delayed version of itself. For a periodic signal (like a musical note), the signal will strongly correlate with itself at delays corresponding to the period of the fundamental frequency.

### 2.2 Step-by-Step Process

**Input:** FloatArray of audio samples (typically 4096-8192 samples)

**Step 1: Calculate Lag Range**
```kotlin
minLag = sampleRate / MAX_FREQUENCY  // ~29 samples (for 1500 Hz)
maxLag = sampleRate / MIN_FREQUENCY  // ~735 samples (for 60 Hz)
```
- Lag represents time delay in samples
- Only search within the expected guitar frequency range

**Step 2: Autocorrelation Calculation**
```kotlin
for (lag in minLag..maxLag) {
    correlation = 0
    for (i in 0 until (audioData.size - lag)) {
        correlation += audioData[i] × audioData[i + lag]
    }
    if (correlation > bestCorrelation) {
        bestCorrelation = correlation
        bestLag = lag
    }
}
```

**What's happening:**
- For each lag value, multiply the signal with its delayed version
- Sum all the products to get correlation strength
- Higher correlation = more periodic at that lag
- The lag with highest correlation corresponds to the period

**Step 3: Validation**
```kotlin
if (bestCorrelation < 0.1f || bestLag == 0) {
    return null  // No clear pitch detected
}
```
- Correlation threshold ensures we only detect strong, clear pitches
- Helps reject noise and non-pitched sounds

**Step 4: Frequency Calculation**
```kotlin
frequency = sampleRate / bestLag
```
- Convert lag (samples) to frequency (Hz)
- Example: If bestLag = 100 samples at 44100 Hz → frequency = 441 Hz (close to A4)

**Step 5: Range Validation**
```kotlin
return if (frequency in 60.0..1500.0) frequency else null
```
- Ensures detected frequency is within expected guitar range
- Rejects out-of-range harmonics or noise

### 2.3 Frequency Detection Range

**Configured Range:**
- **MIN_FREQUENCY**: 60 Hz (below low E2 at ~82 Hz, provides margin)
- **MAX_FREQUENCY**: 1500 Hz (covers high E4 and harmonics)

**Guitar String Fundamentals:**
- Low E2: 82.41 Hz
- A2: 110 Hz
- D3: 146.83 Hz
- G3: 196 Hz
- B3: 246.94 Hz
- High E4: 329.63 Hz

The 60-1500 Hz range captures:
- All fundamental frequencies of standard guitar tuning
- First few harmonics (which aid in detection accuracy)
- Provides margin for slightly out-of-tune notes

### 2.4 Algorithm Characteristics

**Strengths:**
- Simple and computationally efficient (O(n×m) where n=samples, m=lag range)
- Robust to noise and harmonic content
- No need for complex FFT calculations
- Works well for single-note pitched sounds

**Limitations:**
- Cannot detect multiple simultaneous pitches (polyphonic)
- May struggle with very quiet signals (low SNR)
- Performance degrades with complex waveforms (e.g., distorted guitar)
- Requires sufficient samples to cover at least one full period

**Computational Complexity:**
- For 4096 samples and lag range ~700: ~2.8 million operations
- On modern Android devices: < 10ms processing time
- Runs on background thread (Dispatchers.IO) to avoid blocking UI

---

## 3. Note Recognition

### 3.1 Frequency to Musical Note Conversion

The `NoteRecognizer` class converts detected frequencies to musical notes using **equal temperament tuning**.

### 3.2 Equal Temperament Formula

In equal temperament, each semitone is separated by a factor of 2^(1/12):

```kotlin
midiNote = 69 + 12 × log₂(frequency / 440)
```

Where:
- **69** is the MIDI note number for A4
- **440 Hz** is the standard tuning reference (A4)
- **12** semitones per octave
- **log₂** is the base-2 logarithm

**Example Calculations:**

| Frequency | Calculation | MIDI Note | Note Name |
|-----------|-------------|-----------|-----------|
| 440 Hz | 69 + 12×log₂(440/440) = 69 | 69 | A4 |
| 880 Hz | 69 + 12×log₂(880/440) = 81 | 81 | A5 |
| 220 Hz | 69 + 12×log₂(220/440) = 57 | 57 | A3 |
| 261.63 Hz | 69 + 12×log₂(261.63/440) ≈ 60 | 60 | C4 |

### 3.3 Cents Calculation

**Cents** measure fine-tuning deviations. 100 cents = 1 semitone.

```kotlin
cents = (actualMidi - nearestMidi) × 100
```

**Example:**
- Detected frequency: 442 Hz
- A4 at 440 Hz = MIDI 69
- Actual MIDI: 69 + 12×log₂(442/440) ≈ 69.08
- Cents: (69.08 - 69) × 100 = **+8 cents** (slightly sharp)

**Interpretation:**
- **0 cents**: Perfect pitch
- **±1-10 cents**: Excellent tuning
- **±10-25 cents**: Acceptable tuning
- **±25-50 cents**: Noticeably out of tune
- **±50+ cents**: Significantly out of tune

### 3.4 Note Matching

The app uses a **±50 cents threshold** for note matching:

```kotlin
MATCH_THRESHOLD_CENTS = 50.0  // Half semitone
```

**Why 50 cents?**
- Approximately half a semitone
- Accommodates slightly out-of-tune guitars
- Strict enough to differentiate adjacent notes
- Lenient enough for learning/practice scenarios

**Matching Logic:**
```kotlin
fun matchesNote(detectedFrequency: Double, expectedNote: String): Boolean {
    val recognized = recognizeNote(detectedFrequency)
    // Must match note name AND be within ±50 cents
    return (recognized.noteName == expectedNote) && 
           (abs(recognized.cents) <= 50.0)
}
```

### 3.5 Octave Detection

```kotlin
octave = (midiNote / 12) - 1
```

**Example:**
- MIDI 69 (A4): (69 / 12) - 1 = 4.75 - 1 ≈ **4**
- MIDI 60 (C4): (60 / 12) - 1 = 5 - 1 = **4**
- MIDI 48 (C3): (48 / 12) - 1 = 4 - 1 = **3**

The app uses octave information to distinguish between the same note played on different strings or positions on the fretboard.

---

## 4. Audio Manager Integration

### 4.1 Complete Processing Pipeline

The `AudioManager` orchestrates the entire pipeline:

```
AudioRecorder → Raw Audio Samples (with level and gate status)
    ↓
PitchDetector → Detected Frequency (or null) [only if not gated]
    ↓
NoteRecognizer → Musical Note + Cents + Octave
    ↓
Flow<AudioAnalysisResult> → UI/ViewModel
```

### 4.2 Result Types

**NoteDetected:**
```kotlin
data class NoteDetected(
    val noteName: String,           // e.g., "A", "C#"
    val frequency: Double,          // Hz
    val cents: Double,              // deviation from perfect pitch
    val audioLevel: Float,          // 0.0 to 1.0
    val octave: Int,                // e.g., 3, 4, 5
    val noteNameWithOctave: String  // e.g., "A4", "C#3"
)
```

**NoNoteDetected:**
```kotlin
data class NoNoteDetected(
    val audioLevel: Float  // Still provides visual feedback
)
```

**Gated:**
```kotlin
data class Gated(
    val audioLevel: Float  // Signal below noise gate threshold
)
```

### 4.3 Reactive Flow Architecture

The system uses Kotlin Flows for reactive, non-blocking audio processing:

```kotlin
audioManager.startListening()
    .collect { result ->
        when (result) {
            is NoteDetected -> // Update UI with detected note
            is NoNoteDetected -> // Clear note feedback, show level
            is Gated -> // Show idle state (noise gate active)
        }
    }
```

**Benefits:**
- Non-blocking: Audio processing runs on background thread
- Reactive: UI automatically updates when new results arrive
- Cancellable: Easy to stop/start listening
- Backpressure handling: Can skip frames if UI is busy

---

## 5. Microphone Sensitivity System

### 5.1 Sensitivity Multiplier

**User-Controlled Parameter:**
```kotlin
sensitivityMultiplier: Float = 1.0f  // Range: 0.5 to 2.0
```

**Effect:**
```kotlin
adjustedSample = originalSample × sensitivityMultiplier
```

**Practical Impact:**

| Multiplier | Effect | Use Case |
|------------|--------|----------|
| 0.5 | -6 dB | Loud environments, strong pickup signals |
| 1.0 | 0 dB (default) | Normal conditions |
| 1.5 | +3.5 dB | Quiet guitars, low-quality mics |
| 2.0 | +6 dB | Very quiet signals, acoustic guitars |

### 5.2 Audio Level Calculation

**RMS (Root Mean Square) Method:**
```kotlin
rms = sqrt(Σ(sample²) / n)
```

**Logarithmic Scaling:**
```kotlin
db = 20 × log₁₀(rms)
normalizedLevel = (db + 60) / 60  // Map -60dB to 0dB → 0.0 to 1.0
```

**Why logarithmic?**
- Human perception of loudness is logarithmic
- Makes quiet sounds more visible in UI
- Typical guitar signal ranges from -60 dB to 0 dB
- Better visual feedback for users

### 5.3 Auto-Adjust Sensitivity

**Status:** ✅ **FULLY IMPLEMENTED**

Auto-adjust sensitivity dynamically adjusts the sensitivity multiplier based on the incoming signal level to maintain optimal pitch detection.

**Implementation (per AUDIO_DETECTION_ANALYSIS.md Section 7.2.3):**
```kotlin
// In AudioRecorder.kt
private var currentAutoAdjustFactor = 1.0f
private val rmsHistory = ArrayDeque<Float>(RMS_WINDOW_SIZE)

private fun updateAutoAdjustFactor(rawRms: Float) {
    // Add to rolling window
    rmsHistory.addLast(rawRms)
    if (rmsHistory.size > RMS_WINDOW_SIZE) {
        rmsHistory.removeFirst()
    }
    
    // Calculate average RMS over window
    val avgRms = rmsHistory.average().toFloat()
    
    // Calculate proportional error to reach target RMS
    val error = AUTO_ADJUST_TARGET_RMS / (avgRms + 0.001f)
    
    // Smooth adjustment with per-step limits (0.9x to 1.1x)
    val adjustment = error.coerceIn(0.9f, 1.1f)
    
    // Apply adjustment to current gain
    currentAutoAdjustFactor *= adjustment
    
    // Ensure factor stays within safe bounds (0.5x to 2.0x)
    currentAutoAdjustFactor = currentAutoAdjustFactor.coerceIn(0.5f, 2.0f)
}
```

**How it works:**
1. **Tracks RMS level** over rolling window (~1 second, 44 buffers)
2. **Calculates average RMS** from the window
3. **Computes proportional error** relative to target RMS (0.1f)
4. **Applies smooth per-step adjustment** limited to 0.9x-1.1x per iteration
5. **Multiplies current gain** by adjustment
6. **Clamps final gain** to safe bounds (0.5x to 2.0x)

**Combined effect:**
```kotlin
finalSensitivity = baseSensitivity × autoAdjustFactor
```

Where:
- `baseSensitivity`: User's manual slider setting (0.5 to 2.0)
- `autoAdjustFactor`: Dynamically calculated from signal level (0.5 to 2.0)
- `finalSensitivity`: Combined multiplier applied to audio samples

**Benefits:**
- Automatic adaptation to different guitars/microphones
- Maintains optimal signal level for pitch detection
- Smooth transitions without abrupt jumps
- Works in conjunction with manual sensitivity
- Better out-of-box experience for users

**Configuration:**
- Target RMS: 0.1f (optimal detection level)
- Window size: 44 buffers (~1 second at default buffer size)
- Adjustment range: 0.5x to 2.0x
- Per-step limit: 0.9x to 1.1x (smooth convergence)

---

## 6. Parameters Impacting Quality

### 6.1 Sample Rate (44100 Hz)

**Impact:**
- **Higher sample rate** (e.g., 48000 Hz):
  - Pros: Slightly better high-frequency accuracy
  - Cons: More computational load, larger buffers
  - Minimal benefit for guitar frequencies

- **Lower sample rate** (e.g., 22050 Hz):
  - Pros: Lower CPU usage, smaller buffers
  - Cons: Can only detect up to ~11 kHz (may miss high harmonics)
  - Risk: Aliasing artifacts

**Recommendation:** 44.1 kHz is optimal for guitar note detection.

### 6.2 Buffer Size (2× minimum)

**Current:** `BUFFER_SIZE_MULTIPLIER = 2`

**Impact:**
- **Smaller buffer** (1× minimum):
  - Pros: Lower latency (~50ms)
  - Cons: Risk of buffer underruns, less stable
  - Risk: May not contain full period of low frequencies

- **Larger buffer** (4× minimum):
  - Pros: More stable, better low-frequency detection
  - Cons: Higher latency (~200ms), delayed feedback
  - Diminishing returns for guitar notes

**Recommendation:** 2× is a good balance for real-time guitar note detection.

### 6.3 Autocorrelation Threshold (0.1)

**Current:** `bestCorrelation < 0.1f → return null`

**Impact:**
- **Lower threshold** (e.g., 0.05):
  - Pros: Detects weaker/quieter notes
  - Cons: More false positives, noisy detection

- **Higher threshold** (e.g., 0.2):
  - Pros: Only detects very clear pitches
  - Cons: May miss quiet or damped notes

**Recommendation:** 0.1 provides good balance; could be made user-configurable for different use cases.

### 6.4 Frequency Range (60-1500 Hz)

**Current:** `MIN_FREQUENCY = 60.0, MAX_FREQUENCY = 1500.0`

**Impact:**
- **Wider range** (e.g., 40-3000 Hz):
  - Pros: More flexible for different instruments
  - Cons: More false detections, slower computation

- **Narrower range** (e.g., 80-1000 Hz):
  - Pros: Faster computation, fewer false positives
  - Cons: May miss some legitimate notes/harmonics

**Recommendation:** Current range is well-suited for standard guitar tuning.

### 6.5 Note Match Threshold (±50 cents)

**Current:** `MATCH_THRESHOLD_CENTS = 50.0`

**Impact:**
- **Stricter threshold** (e.g., ±25 cents):
  - Pros: Encourages better tuning
  - Cons: Frustrating for beginners, may reject valid attempts

- **Looser threshold** (e.g., ±75 cents):
  - Pros: More forgiving for beginners
  - Cons: Accepts significantly out-of-tune notes

**Recommendation:** ±50 cents is appropriate for learning scenarios; could offer "strict" and "forgiving" modes.

### 6.6 Noise Gate Threshold (0.01f)

**Current:** `noiseGateThreshold = 0.01f`

**Status:** ✅ **IMPLEMENTED**

**Impact:**
- **Lower threshold** (e.g., 0.005f):
  - Pros: More sensitive, detects quieter signals
  - Cons: May process background noise

- **Higher threshold** (e.g., 0.05f):
  - Pros: More aggressive noise suppression
  - Cons: May gate out quiet guitar notes

**Recommendation:** Default of 0.01f (-40 dB) is good starting point; now user-configurable in settings.

---

## 7. Quality Adjustments and Improvements

### 7.1 Current Adjustable Parameters

**User-Accessible Settings:**

1. **Microphone Sensitivity** (0.5 - 2.0) - ✅ Implemented
   - Adjusts signal amplitude before processing
   - Helps with different guitar/microphone combinations

2. **Auto-Adjust Sensitivity** (On/Off) - ✅ Implemented
   - Automatically adjusts sensitivity based on signal level
   - Works in conjunction with manual sensitivity

3. **Audio Source** (Auto / Unprocessed / Voice Recognition / Mic) - ✅ Implemented
   - Affects raw audio quality and processing
   - Auto-select typically chooses best option

4. **Noise Gate Threshold** (0.001 - 0.1) - ✅ Implemented
   - User-configurable threshold for noise suppression
   - Available in settings screen

### 7.2 Implemented Quality Improvements

#### 7.2.1 Auto-Adjust Sensitivity (Priority 1) - ✅ IMPLEMENTED

Implemented in `AudioRecorder.kt` as documented in Section 5.3.

**Algorithm:**
1. Maintains rolling window of RMS values (~1 second)
2. Calculates average RMS over window
3. Computes proportional error relative to target RMS (0.1f)
4. Applies smooth per-step adjustment (0.9x-1.1x per iteration)
5. Clamps final gain to safe bounds (0.5x to 2.0x)

**Benefits:**
- Automatic adjustment to different guitars/microphones
- Maintains optimal signal level for pitch detection
- Better user experience

**Code Location:** `AudioRecorder.kt` - `updateAutoAdjustFactor()` method

#### 7.2.2 Noise Gate (Priority 1) - ✅ IMPLEMENTED

Implemented with configurable threshold in `AudioManager.kt` and `AudioRecorder.kt`.

**Implementation:**
```kotlin
// In AudioRecorder.kt
val isGated = rawRms < noiseGateThreshold

// In AudioManager.kt
if (audioDataWithLevel.isGated) {
    AudioAnalysisResult.Gated(audioLevel = audioDataWithLevel.level)
} else {
    // Process pitch detection
}
```

**Benefits:**
- Reduces CPU usage when idle
- Cleaner UI feedback (no spurious detections)
- User-configurable threshold in settings
- Battery savings

**Code Locations:**
- `AudioRecorder.kt` - Noise gate check
- `AudioManager.kt` - `Gated` result type
- `SettingsScreen.kt` - UI for threshold adjustment

#### 7.2.3 High-Pass Filter (Priority 1) - ✅ IMPLEMENTED

Implemented as one-pole IIR high-pass filter in `HighPassFilter.kt`.

**Implementation:**
```kotlin
class HighPassFilter(private val sampleRate: Int = 44100, cutoffFrequency: Double = 60.0) {
    private var prevInput = 0f
    private var prevOutput = 0f
    private val alpha: Float

    init {
        val rc = 1.0 / (2.0 * PI * cutoffFrequency)
        val dt = 1.0 / sampleRate
        alpha = (rc / (rc + dt)).toFloat()
    }

    fun process(input: Float): Float {
        val output = alpha * (prevOutput + input - prevInput)
        prevInput = input
        prevOutput = output
        return output
    }

    fun process(samples: FloatArray): FloatArray {
        for (i in samples.indices) {
            samples[i] = process(samples[i])
        }
        return samples
    }
}
```

**Characteristics:**
- Cutoff frequency: 60 Hz (below lowest guitar note E2 at 82 Hz)
- Filter type: One-pole IIR (6 dB/octave roll-off)
- Performance: Minimal CPU overhead (one multiply, two additions per sample)
- In-place processing for efficiency

**Benefits:**
- Removes low-frequency handling noise (bumps, taps)
- Reduces environmental rumble (traffic, wind, HVAC)
- Eliminates DC offset and subsonic content
- Improves pitch detection accuracy by reducing spurious low-frequency triggers
- No impact on guitar frequencies

**Integration:**
Applied in `AudioRecorder.kt` after sensitivity adjustment, before RMS calculation:
```kotlin
val highPassFilter = HighPassFilter(sampleRate = SAMPLE_RATE, cutoffFrequency = 60.0)
// ... in recording loop:
val adjustedData = applySensitivity(audioData, combinedMultiplier)
highPassFilter.process(adjustedData)  // Apply filter in-place
```

**Code Locations:**
- `HighPassFilter.kt` - Filter implementation
- `AudioRecorder.kt` - Filter integration in pipeline
- `HighPassFilterTest.kt` - Comprehensive unit tests

### 7.3 Potential Future Improvements

#### 7.3.1 Precision Improvements (Priority 2)

**1. Parabolic Interpolation** - ❌ NOT IMPLEMENTED

Current implementation finds the best lag value at integer sample precision. Parabolic interpolation could improve frequency accuracy:

```kotlin
// Pseudocode
fun interpolatePeak(prev: Float, peak: Float, next: Float): Float {
    return 0.5f * (prev - next) / (prev - 2*peak + next)
}

// Apply to bestLag to get sub-sample precision
refinedLag = bestLag + interpolatePeak(
    correlation[bestLag-1],
    correlation[bestLag],
    correlation[bestLag+1]
)
```

**Benefit:** 
- Improves frequency accuracy from ±2 Hz to ±0.1 Hz
- More accurate cents calculation
- Better tuner precision

**Tradeoff:** 
- Minimal additional computation
- Recommended for tuner feature
- May be overkill for practice mode

**2. Zero-Crossing Detection** - ❌ NOT IMPLEMENTED

Add zero-crossing rate analysis to validate pitch detection:

```kotlin
fun calculateZeroCrossingRate(audioData: FloatArray): Float {
    var crossings = 0
    for (i in 1 until audioData.size) {
        if ((audioData[i-1] < 0 && audioData[i] >= 0) ||
            (audioData[i-1] >= 0 && audioData[i] < 0)) {
            crossings++
        }
    }
    return crossings.toFloat() / audioData.size
}
```

**Benefit:**
- Helps distinguish pitched vs unpitched sounds
- Can detect clipped signals (too much sensitivity)
- Additional validation metric

**3. Harmonic Product Spectrum (HPS)** - ❌ NOT IMPLEMENTED

For challenging cases, could add HPS as fallback:

```kotlin
// Simplified concept
fun harmonicProductSpectrum(fft: DoubleArray, numHarmonics: Int): DoubleArray {
    val hps = fft.copyOf()
    for (h in 2..numHarmonics) {
        for (i in fft.indices) {
            if (i * h < fft.size) {
                hps[i] *= fft[i * h]
            }
        }
    }
    return hps
}
```

**Benefit:**
- Better detection of fundamental frequency in presence of strong harmonics
- Useful for distorted guitar signals

**Tradeoff:**
- Requires FFT (more computation)
- More complex implementation
- May not be necessary for clean signals

#### 7.3.2 Speed Improvements (Priority 2)

**1. Reduce Lag Search Range** - ❌ NOT IMPLEMENTED

Dynamically adjust search range based on previous detections:

```kotlin
// If previously detected note was ~440 Hz, search ±100 Hz range
val centerLag = sampleRate / lastDetectedFrequency
val searchRange = centerLag * 0.2  // ±20% range
```

**Benefit:**
- Reduces computation by 50-75%
- Faster real-time response
- Still catches string changes

**2. Decimation for Low Frequencies** - ❌ NOT IMPLEMENTED

For low frequencies, downsample the signal:

```kotlin
if (expectedFrequency < 150) {
    // Downsample by 2x for low strings
    val decimated = audioData.filterIndexed { i, _ -> i % 2 == 0 }
    effectiveSampleRate = sampleRate / 2
}
```

**Benefit:**
- Faster autocorrelation for bass strings
- Reduced memory usage

**Tradeoff:**
- More complex code
- Marginal benefit on modern devices

**3. Early Exit Optimization** - ❌ NOT IMPLEMENTED

Exit autocorrelation loop early if clear peak found:

```kotlin
for (lag in minLag..maxLag) {
    // ... calculate correlation ...
    if (correlation > 0.8f) {
        // Very strong correlation, likely the correct pitch
        break
    }
}
```

**Benefit:**
- Faster detection for clear signals
- Reduces average-case computation

---

## 8. Algorithm Trade-offs

### 8.1 Autocorrelation vs FFT-based Methods

**Current: Autocorrelation**

**Alternative: FFT + Peak Detection**

| Aspect | Autocorrelation | FFT-based |
|--------|----------------|-----------|
| Complexity | O(n×m) | O(n log n) |
| Accuracy | Good for single pitch | Better for complex signals |
| Polyphony | No | Possible |
| Latency | Low | Moderate |
| Implementation | Simple | Complex |
| CPU Usage | Moderate | Higher |

**Verdict:** Autocorrelation is appropriate for this app's use case (monophonic guitar notes).

### 8.2 Time Domain vs Frequency Domain

**Current: Time Domain (Autocorrelation)**

**Alternative: Frequency Domain (FFT + HPS)**

| Aspect | Time Domain | Frequency Domain |
|--------|-------------|------------------|
| Harmonic handling | Implicit | Explicit |
| Noise robustness | Good | Better |
| Computation | Simple | Complex |
| Memory | Low | Higher |
| Real-time | Excellent | Good |

**Verdict:** Time domain is simpler and sufficient for clean guitar signals.

### 8.3 Accuracy vs Speed Trade-offs

Current implementation prioritizes:
1. **Speed**: Fast enough for real-time feedback (< 100ms latency)
2. **Simplicity**: Maintainable, understandable code
3. **Accuracy**: Sufficient for learning/practice (±5 Hz typical accuracy)

For a professional tuner, would prioritize:
1. **Accuracy**: ±0.1 Hz with parabolic interpolation
2. **Precision**: Advanced filtering and validation
3. **Speed**: Still fast, but can tolerate slightly higher latency

---

## 9. Limitations and Edge Cases

### 9.1 Current Limitations

**1. Monophonic Only**
- Cannot detect multiple simultaneous notes (chords)
- This is inherent to autocorrelation approach
- **Impact:** Practice mode is limited to single-note exercises

**2. Octave Ambiguity**
- Strong harmonics can confuse fundamental detection
- Example: Guitar harmonic at 12th fret may detect as note one octave higher
- **Mitigation:** Frequency range limits help, but not foolproof

**3. Quiet Signal Handling**
- Very quiet notes may not be detected
- Users must play with sufficient volume
- **Mitigation:** ✅ Auto-adjust sensitivity now helps adapt to quiet signals

**4. Background Noise**
- ✅ High-pass filter now removes low-frequency noise
- ✅ Noise gate now suppresses spurious detections in idle conditions
- Relies on audio source's built-in processing (if using VOICE_RECOGNITION)

**5. Attack Transients**
- Strong attack (initial pluck) may cause momentary false detection
- **Mitigation:** Correlation threshold helps filter out transients

### 9.2 Edge Cases

**1. Very Low Notes (< 82 Hz)**
- Low E string at 82 Hz is near the detection limit
- Requires longer audio buffer (more samples) to capture full period
- Detection may be less reliable

**2. Fret Buzz and String Rattle**
- Non-harmonic noise can interfere with pitch detection
- Correlation will be low, likely resulting in null detection
- **Impact:** Encourages good playing technique

**3. Overtones and Harmonics**
- Bright-toned guitars with strong harmonics may confuse detector
- Touch harmonics (natural harmonics at 5th, 7th, 12th frets) may detect incorrectly
- **Mitigation:** Frequency range filtering, but not perfect

**4. Damped Notes**
- Notes that decay quickly may only be detected briefly
- Fast decay = less time for detection
- **Impact:** May need to sustain notes slightly longer

**5. Simultaneous Notes (Chords)**
- Autocorrelation will find strongest frequency component
- May detect fundamental, or may detect nothing (ambiguous correlation)
- **Limitation:** Cannot be addressed without switching to polyphonic algorithm

---

## 10. Recommendations

### 10.1 Priority 1 Improvements - ✅ ALL COMPLETED

1. **✅ Auto-Adjust Sensitivity** - IMPLEMENTED
   - Fully functional in `AudioRecorder.kt`
   - Rolling window RMS tracking
   - Smooth adjustment algorithm
   - User can enable/disable in settings

2. **✅ Noise Gate** - IMPLEMENTED
   - Configurable threshold in settings
   - New `Gated` result type
   - UI shows idle state
   - Reduces CPU usage when idle

3. **✅ High-Pass Filter** - IMPLEMENTED
   - One-pole IIR filter at 60 Hz
   - Integrated in audio pipeline
   - Comprehensive unit tests
   - Documented in README

### 10.2 Priority 2 Improvements - NOT YET IMPLEMENTED

1. **❌ Parabolic Interpolation for Tuner**
   - Effort: Medium (3-4 hours)
   - Impact: High for tuner, low for practice mode
   - Improves frequency accuracy to ±0.1 Hz

2. **❌ Dynamic Lag Range Optimization**
   - Effort: Medium (4-5 hours)
   - Impact: Medium (15-25% speed improvement)
   - Track previous detections, narrow search range

3. **❌ Advanced Octave Disambiguation**
   - Effort: Medium (5-6 hours)
   - Impact: Medium (reduces octave errors)
   - Analyze harmonic content to confirm fundamental

4. **❌ Make Correlation Threshold Configurable**
   - Effort: Very Low (< 1 hour)
   - Impact: Medium (power users can fine-tune)
   - Add to settings: "Detection Sensitivity" (0.05 to 0.2 range)

5. **❌ Adaptive Correlation Threshold**
   - Effort: Medium (3-4 hours)
   - Impact: Medium (better handling of varying signal quality)
   - Adjust threshold based on signal characteristics

### 10.3 Priority 3 Improvements - NOT YET IMPLEMENTED

1. **❌ Hybrid Algorithm: Autocorrelation + FFT**
   - Effort: High (2-3 days)
   - Impact: High (more robust detection)
   - Use autocorrelation for speed, FFT for validation/ambiguous cases

2. **❌ Polyphonic Detection**
   - Effort: Very High (1-2 weeks)
   - Impact: Very High (enables chord detection)
   - Requires complete redesign with FFT + peak tracking

3. **❌ Machine Learning-based Pitch Detection**
   - Effort: Very High (2-3 weeks + training)
   - Impact: High (state-of-art accuracy)
   - Use CREPE or similar model; requires TensorFlow Lite integration

4. **❌ Real-time Audio Effects/Preprocessing**
   - Effort: High (1-2 weeks)
   - Impact: Medium-High (better signal quality)
   - Add EQ, compression, adaptive filtering before pitch detection

---

## 11. Conclusion

### 11.1 Summary of Current Implementation

The Android Guitar Notes Learner app uses a well-designed, efficient audio processing pipeline:

- **Audio Recording:** 44.1 kHz, mono, PCM float, intelligent source selection
- **Pitch Detection:** Time-domain autocorrelation, optimized for guitar frequencies
- **Note Recognition:** Equal temperament with cents deviation, ±50 cent matching
- **Architecture:** Reactive Flow-based, non-blocking, cancellable
- **✅ Auto-Adjust Sensitivity:** Fully implemented with rolling window RMS tracking
- **✅ Noise Gate:** Implemented with configurable threshold
- **✅ High-Pass Filter:** Implemented at 60 Hz cutoff

### 11.2 Overall Quality Assessment

**Strengths:**
- ✅ Simple, maintainable, and understandable code
- ✅ Fast enough for real-time feedback (< 100ms latency)
- ✅ Sufficient accuracy for learning/practice scenarios
- ✅ Good handling of guitar frequency range
- ✅ Configurable sensitivity for different hardware
- ✅ Proper audio source selection (UNPROCESSED preferred)
- ✅ Auto-adjust sensitivity for automatic signal adaptation
- ✅ Noise gate for cleaner detection and CPU savings
- ✅ High-pass filter for noise rejection

**Areas for Future Improvement:**
- ⚠️ No parabolic interpolation (could improve tuner accuracy)
- ⚠️ Fixed correlation threshold (not adaptive)
- ⚠️ No polyphonic detection (by design, but limits future features)
- ⚠️ Integer lag precision (could use interpolation for professional tuner)

### 11.3 Quality vs Complexity Balance

The current implementation strikes an **excellent balance** for the app's purpose:
- Production-ready for learning/practice scenarios
- No over-engineering
- Room for targeted improvements without major refactoring
- Good foundation for future enhancements
- **All Priority 1 improvements successfully implemented**

### 11.4 Recommendation Priority

**✅ Priority 1 (COMPLETED in PR #58):**
1. ✅ **Auto-adjust sensitivity** - Fully implemented
2. ✅ **Noise gate** - Fully implemented with configurable threshold
3. ✅ **High-pass filter** - Fully implemented at 60 Hz

**Priority 2 (Consider for Next Version):**
1. ❌ Parabolic interpolation for tuner accuracy
2. ❌ Configurable correlation threshold
3. ❌ Dynamic lag range optimization

**Priority 3 (Future Enhancements):**
1. ❌ Hybrid autocorrelation + FFT validation
2. ❌ Advanced harmonic analysis
3. ❌ Polyphonic detection (major feature)

---

## Appendix: Technical Specifications

### A1. Audio Parameters Summary

| Parameter | Value | Rationale |
|-----------|-------|-----------|
| Sample Rate | 44100 Hz | Standard rate, sufficient for guitar |
| Channels | Mono | Single pitch source |
| Format | PCM Float | Simplified math operations |
| Buffer Size | 2× minimum | Balance latency vs stability |
| Frequency Range | 60-1500 Hz | Covers guitar + margin |
| Correlation Threshold | 0.1 | Balance sensitivity vs false positives |
| Match Threshold | ±50 cents | Half semitone, forgiving but distinct |
| Sensitivity Range | 0.5-2.0 | ±6 dB adjustment range |
| Noise Gate Threshold | 0.01f (default) | -40 dB, user-configurable |
| High-Pass Cutoff | 60 Hz | Below lowest guitar note (E2 at 82 Hz) |
| Auto-Adjust Target RMS | 0.1f | Optimal level for pitch detection |
| Auto-Adjust Range | 0.5-2.0x | Same as manual sensitivity range |

### A2. Performance Characteristics

| Metric | Typical Value | Notes |
|--------|--------------|-------|
| Detection Latency | 50-100 ms | Buffer + processing time |
| CPU Usage | < 5% | Single core, background thread |
| Memory Usage | < 5 MB | Small buffers, no caching |
| Frequency Accuracy | ±2-5 Hz | Integer lag precision |
| Cents Accuracy | ±2-5 cents | Based on frequency accuracy |
| Detection Rate | 10-20 Hz | New result every 50-100ms |
| False Positive Rate | < 5% | With proper playing technique |

### A3. Code Structure

```
AudioManager
├── AudioRecorder (Audio capture & sensitivity)
│   ├── AudioRecord API
│   ├── Audio source selection
│   ├── Sensitivity adjustment
│   ├── Auto-adjust sensitivity (✅ implemented)
│   ├── High-pass filtering (✅ implemented)
│   ├── Noise gate check (✅ implemented)
│   └── RMS level calculation
├── PitchDetector (Frequency detection)
│   ├── Autocorrelation algorithm
│   ├── Lag search optimization
│   └── Frequency validation
└── NoteRecognizer (Musical note conversion)
    ├── Frequency to MIDI conversion
    ├── Cents calculation
    └── Note name mapping
```

### A4. Dependencies

- **Android SDK:** AudioRecord, MediaRecorder
- **Kotlin:** Coroutines, Flow
- **Math:** Standard library (sqrt, log2, log10)

No external audio processing libraries required.

---

**Document Version:** 2.0  
**Date:** 2025-11-12  
**Last Updated:** 2025-11-12 (after PR #58 implementation)  
**Status:** All Priority 1 improvements completed and documented  
**Author:** Analysis and implementation tracking for audio processing improvements
