# External Audio Processing Libraries Research

## Executive Summary

This document provides comprehensive research and analysis of external audio processing libraries suitable for the Android Guitar Notes Learner app. The research evaluates libraries based on accuracy, usability, maintenance, license compatibility, and integration considerations for guitar note learning applications.

**Current Implementation**: The app currently uses a custom-built audio processing pipeline with:
- Autocorrelation-based pitch detection
- 44.1 kHz sample rate with PCM float format
- High-pass filter at 60 Hz
- Auto-adjust sensitivity and noise gate
- Real-time processing with Kotlin coroutines

**Research Scope**: Evaluated libraries for pitch detection, audio analysis, and DSP capabilities suitable for:
- Guitar note detection (60 Hz - 1500 Hz range)
- Real-time processing with low latency
- Android compatibility (API 34+)
- Kotlin/Java integration

---

## 1. TarsosDSP

### Overview
TarsosDSP is a well-established, pure Java audio processing library specifically designed for Android and desktop applications. It provides comprehensive DSP functionality with a focus on pitch detection.

### Key Features
- **Multiple Pitch Detection Algorithms**:
  - YIN algorithm (recommended for accuracy)
  - McLeod Pitch Method (MPM)
  - Dynamic Wavelet Algorithm
  - FFT_YIN (optimized for real-time performance)
- **Real-time Audio Input**: Direct microphone integration
- **Audio Effects**: Time stretching (WSOLA), pitch shifting, resampling
- **Additional Features**: Onset detection, Goertzel DTMF decoding, filtering
- **Android Native**: Pure Java implementation, no JNI required

### Integration
```gradle
implementation 'be.tarsos.dsp:core:2.5'
```

**Example Usage:**
```java
AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);
PitchDetectionHandler pdh = new PitchDetectionHandler() {
    @Override
    public void handlePitch(PitchDetectionResult result, AudioEvent e) {
        float pitchInHz = result.getPitch();
        // Use pitch data
    }
};
AudioProcessor p = new PitchProcessor(
    PitchEstimationAlgorithm.YIN, 
    22050, 
    1024, 
    pdh
);
dispatcher.addAudioProcessor(p);
new Thread(dispatcher, "Audio Dispatcher").start();
```

### Performance
- **Sample Rate**: Typically 22050 Hz (adjustable)
- **Buffer Size**: 1024-2048 samples recommended
- **Latency**: 50-100ms typical
- **CPU Usage**: Low to moderate (pure Java optimization)
- **Accuracy**: ±2-5 Hz with YIN, ±0.5 Hz with parabolic interpolation

### Pros
✅ **Pure Java**: No native code, simplifies Android integration  
✅ **Multiple Algorithms**: Flexibility to choose best algorithm for use case  
✅ **Well-documented**: Extensive examples and tutorials  
✅ **Active Community**: Regular updates, good support  
✅ **Battle-tested**: Used in production apps worldwide  
✅ **Real-time Ready**: Optimized for live audio processing  
✅ **Educational**: Clear code, good for learning DSP concepts  
✅ **Android-first Design**: Built specifically for Android constraints

### Cons
❌ **Java Performance**: Slower than native C++ implementations  
❌ **Memory**: Higher memory footprint than native solutions  
❌ **Limited to Monophonic**: Cannot detect multiple simultaneous notes  
❌ **Pitch Drops**: Known issue with sudden drops after note release (FFT_YIN)  
❌ **No GPU Acceleration**: CPU-only processing

### Use Cases for This App
- **Excellent fit** for guitar note detection
- YIN algorithm is more accurate than current autocorrelation
- Direct replacement for PitchDetector.kt with minimal code changes
- Can run alongside existing code for A/B testing

### License
**GNU Affero General Public License v3.0 (AGPL-3.0)**

⚠️ **License Concern**: AGPL-3.0 is copyleft and requires:
- Source code disclosure for network-served applications
- Apps using TarsosDSP must also be AGPL-3.0 or compatible
- **Not compatible** with MIT license used in this project
- Would require relicensing the entire app or obtaining commercial license

### Recommendation
**Rating: 4/5** - Excellent technical fit but license incompatibility is a significant concern.

**Alternative**: Implement YIN algorithm directly based on published paper (De Cheveigné & Kawahara, 2002) to avoid licensing issues.

### References
- GitHub: https://github.com/JorenSix/TarsosDSP
- Documentation: https://0110.be/posts/TarsosDSP_on_Android_-_Audio_Processing_in_Java_on_Android
- Release Downloads: https://0110.be/releases
- Flutter Plugin: https://pub.dev/packages/flutter_pitch_detection
- TarsosDSPKit (packaged AAR): https://github.com/koendv/TarsosDSPKit

---

## 2. Google Oboe

### Overview
Oboe is Google's open-source C++ audio library designed for high-performance, low-latency audio on Android. It's the official recommended solution for professional audio applications.

### Key Features
- **Ultra-low Latency**: Optimized for minimal audio latency
- **Smart API Selection**: Automatically chooses best native API (AAudio for API 27+, OpenSL ES for older versions)
- **PCM Offload Support**: Up to 30% battery savings for playback
- **Compressed Format Support**: MP3, AAC_LC playback
- **Spatial Audio**: Spatialization APIs support
- **Modern C++**: Clean, safe API design
- **Device Workarounds**: Mitigates Android audio bugs across devices

### Latest Version
**v1.10.0** (September 2024)
- MMAP policy querying
- Multiple output device queries
- Playback parameter control (speed/pitch shift)
- Enhanced OboeTester for CPU load testing

### Integration
Oboe is a C++ library requiring NDK integration:
```gradle
android {
    externalNativeBuild {
        cmake {
            path "CMakeLists.txt"
        }
    }
}
```

**CMakeLists.txt:**
```cmake
find_package(oboe REQUIRED CONFIG)
target_link_libraries(your-target oboe::oboe)
```

### Performance
- **Latency**: 10-20ms achievable on modern devices (lowest on Android)
- **Sample Rate**: Any supported by device (typically 44.1 or 48 kHz)
- **CPU Usage**: Very low (native C++ optimization)
- **Compatibility**: API 16+ (Android 4.1+), covers 99% of devices

### Pros
✅ **Lowest Latency**: Best-in-class audio latency for Android  
✅ **Google Official**: Official support and ongoing development  
✅ **Battery Efficient**: PCM offload and hardware optimizations  
✅ **Device Coverage**: Works across 99% of Android devices  
✅ **Production Ready**: Used in major apps (games, DAWs, instruments)  
✅ **Excellent Documentation**: Comprehensive guides and examples  
✅ **Active Development**: Regular updates and improvements  
✅ **Permissive License**: Apache 2.0, compatible with any project

### Cons
❌ **C++ Required**: Requires NDK knowledge and JNI bridging  
❌ **No DSP Built-in**: Only handles I/O, not pitch detection  
❌ **Integration Complexity**: More complex setup than pure Java solutions  
❌ **Build Time**: Increases build time due to native compilation  
❌ **Debugging**: Native debugging is more complex  
❌ **Not Kotlin-native**: Requires JNI wrapper code

### Use Cases for This App
- **Not directly applicable** - Oboe handles audio I/O, not pitch detection
- Could replace AudioRecorder.kt for lower latency audio capture
- Would still need pitch detection library/algorithm on top
- Best used in combination with another DSP library
- Overkill for current app requirements (custom AudioRecord implementation is sufficient)

### License
**Apache License 2.0**

✅ **License Compatible**: Fully compatible with MIT license and commercial use.

### Recommendation
**Rating: 3/5** - Excellent for audio I/O but doesn't solve pitch detection problem.

**Use Case**: Consider if app needs professional-grade low-latency audio or plans to add recording/playback features. Not recommended as standalone solution for current requirements.

### References
- GitHub: https://github.com/google/oboe
- Documentation: https://android.googlesource.com/platform/external/oboe/+/refs/heads/main/docs/FullGuide.md
- API Reference: https://google.github.io/oboe/
- Android Developers: https://developer.android.com/games/sdk/oboe
- Release Notes: https://github.com/google/oboe/releases

---

## 3. Essentia

### Overview
Essentia is a comprehensive C++ library for audio and music analysis developed by the Music Technology Group at Universitat Pompeu Fabra. It provides state-of-the-art algorithms for music information retrieval (MIR).

### Key Features
- **Extensive Algorithm Library**: 400+ algorithms for audio analysis
- **Pitch Detection**: PitchMelodia, PitchYin, PitchYinFFT
- **Music Analysis**: Tempo, key, chord detection, beat tracking
- **Spectral Analysis**: FFT, MFCC, spectral descriptors
- **Machine Learning**: Audio classification, similarity
- **Cross-platform**: Linux, macOS, Windows, Android, iOS, Web (WASM)

### Pitch Detection Algorithms
**PitchMelodia** (MELODIA method):
- Input: Audio signal vector
- Output: Pitch values (Hz) + confidence
- Configurable: Frame size, hop size, frequency range, harmonic weight
- Handles: Both monophonic and polyphonic signals

**PitchYin**:
- YIN algorithm implementation
- Optimized for monophonic signals
- Excellent for instrument tuning

### Integration (Android)
Requires NDK build process:
```bash
# Clone repository
git clone https://github.com/MTG/essentia.git
cd essentia

# Build for Android using provided script
./build_android.sh

# Integrate .so files into Android project via JNI
```

### Performance
- **Accuracy**: Research-grade, state-of-the-art algorithms
- **CPU Usage**: Moderate to high (comprehensive analysis)
- **Latency**: Depends on frame size and hop size
- **Memory**: Moderate (efficient C++ implementation)

### Pros
✅ **Comprehensive**: 400+ algorithms for audio/music analysis  
✅ **Research-grade**: Academically validated, published algorithms  
✅ **Professional**: Used by Spotify, BMAT, Freesound, major MIR projects  
✅ **Advanced Pitch Detection**: PitchMelodia handles polyphonic signals  
✅ **Well-documented**: Extensive API reference and examples  
✅ **Active Development**: Regular updates from MTG research group  
✅ **Cross-platform**: Runs on all major platforms including web  
✅ **Permissive License**: GNU Affero GPL v3 with commercial option

### Cons
❌ **Steep Learning Curve**: Complex library with many algorithms  
❌ **Native Code**: Requires JNI integration for Android  
❌ **Build Complexity**: Custom build process for Android  
❌ **Overkill**: Far more features than needed for basic pitch detection  
❌ **Size**: Large library increases APK size  
❌ **Performance**: More resource-intensive than specialized pitch libraries  
❌ **License**: AGPL-3.0 requires source disclosure (similar to TarsosDSP)

### Use Cases for This App
- **Overkill** for current requirements
- Excellent if planning to add:
  - Chord detection
  - Key/scale detection
  - Tempo/rhythm analysis
  - Music similarity features
- Better suited for comprehensive music analysis apps

### License
**GNU Affero General Public License v3.0 (AGPL-3.0)** or commercial license

⚠️ **License Concern**: Same AGPL-3.0 issues as TarsosDSP. Not compatible with MIT license without relicensing or commercial agreement.

### Recommendation
**Rating: 3/5** - Powerful but excessive for current needs. License incompatibility is a concern.

**Use Case**: Consider for future if adding advanced music analysis features. Otherwise, too complex for guitar note detection alone.

### References
- GitHub: https://github.com/MTG/essentia
- Documentation: https://essentia.upf.edu/contents.html
- PitchMelodia Algorithm: https://essentia.upf.edu/reference/std_PitchMelodia.html
- Applications: https://essentia.upf.edu/applications.html
- Essentia.js (Web/WASM): https://mtg.github.io/essentia.js/

---

## 4. CREPE + TensorFlow Lite

### Overview
CREPE (Convolutional REpresentation for Pitch Estimation) is a state-of-the-art deep learning model for monophonic pitch detection. It can be deployed on Android using TensorFlow Lite for on-device inference.

### Key Features
- **Deep Learning**: CNN-based pitch estimation from raw waveforms
- **State-of-the-art Accuracy**: Outperforms traditional methods (pYIN, SWIPE)
- **No Feature Engineering**: Works directly on audio waveforms
- **Configurable**: Model capacity (tiny to full) for accuracy vs. performance tradeoff
- **TensorFlow Ecosystem**: Integration with TFLite for mobile deployment

### SPICE Model Alternative
Google provides SPICE (another pitch detection model) via TensorFlow Hub, which is:
- Specifically designed for mobile deployment
- Pre-trained and optimized for TFLite
- Easier integration than CREPE conversion

### Integration
```gradle
implementation 'org.tensorflow:tensorflow-lite:2.14.0'
implementation 'org.tensorflow:tensorflow-lite-task-audio:0.4.4'
```

**Workflow:**
1. Convert CREPE/SPICE model to .tflite format
2. Preprocess audio (mono, 16kHz, normalization)
3. Run inference using TFLite Java API
4. Post-process results

**Audio Preprocessing (using JLibrosa or custom code):**
```java
// Convert to mono, resample to 16kHz, normalize amplitude
float[] audioBuffer = preprocessAudio(rawAudio);
// Run inference
float[][] results = tfliteInterpreter.run(audioBuffer);
float pitchHz = postprocessResults(results);
```

### Performance
- **Accuracy**: ±0.1 Hz (best-in-class for monophonic signals)
- **Latency**: 30-100ms depending on model size and device
- **CPU Usage**: Higher than traditional algorithms (neural network inference)
- **Memory**: Moderate (model size 500KB - 5MB depending on variant)
- **Battery Impact**: Higher than traditional DSP methods

### Pros
✅ **Highest Accuracy**: State-of-the-art pitch detection  
✅ **Robust to Noise**: Better noise handling than traditional algorithms  
✅ **No Manual Tuning**: No parameter tuning needed  
✅ **Ongoing Research**: Continuously improving with ML advances  
✅ **TensorFlow Ecosystem**: Good tooling and documentation  
✅ **Permissive License**: Apache 2.0 (CREPE) and compatible with commercial use  
✅ **Model Variety**: SPICE model readily available on TensorFlow Hub

### Cons
❌ **Inference Latency**: Higher than traditional DSP (30-100ms)  
❌ **Preprocessing Required**: Audio must be preprocessed (mono, 16kHz)  
❌ **APK Size**: Adds TFLite runtime + model (5-10 MB)  
❌ **CPU/Battery**: More resource-intensive than traditional methods  
❌ **Complexity**: More moving parts (model, preprocessing, inference)  
❌ **Not Kotlin-native**: Java/JNI integration required  
❌ **Limited to Monophonic**: Cannot detect chords (same as traditional)

### Use Cases for This App
- **Excellent for tuner feature**: Maximum accuracy for guitar tuning
- **Good for practice mode**: Reliable note detection in noisy environments
- **Future-proof**: Can be retrained or fine-tuned for specific guitars
- Higher latency acceptable for learning app (not real-time music performance)

### License
**Apache License 2.0** (CREPE) / **Apache License 2.0** (TensorFlow Lite)

✅ **License Compatible**: Fully compatible with MIT license.

### Recommendation
**Rating: 4.5/5** - Highest accuracy, modern approach, compatible license.

**Best for**: Apps requiring maximum pitch detection accuracy and willing to accept slightly higher latency and resource usage. Excellent choice for guitar tuner feature.

**Implementation Effort**: Medium to High (model conversion, preprocessing, TFLite integration)

### References
- CREPE GitHub: https://github.com/marl/crepe
- CREPE PyPI: https://pypi.org/project/crepe/
- SPICE Model (TensorFlow Hub): https://www.tensorflow.org/hub/tutorials/spice
- TensorFlow Lite Audio Classification: https://developers.google.com/codelabs/tflite-audio-classification-basic-android
- Audio Processing Guide: https://fritz.ai/audio-processing-in-android-with-tensorflow-lite-models/
- Task Library: https://ai.google.dev/edge/litert/libraries/task_library/audio_classifier

---

## 5. Superpowered SDK

### Overview
Superpowered is a commercial, professional-grade C++ audio SDK for iOS and Android. It provides comprehensive audio processing including pitch detection, time stretching, effects, and low-latency audio I/O.

### Key Features
- **Pitch Detection**: Professional-grade pitch tracking
- **Music Analysis**: BPM detection, key detection, spectral analysis
- **Audio Effects**: Time stretching, pitch shifting, reverb, EQ, compression
- **Ultra-low Latency**: Optimized for real-time audio processing
- **Cross-platform**: iOS, Android, desktop
- **Battery Optimized**: Efficient power consumption

### Performance
- **Latency**: Sub-10ms achievable
- **CPU Usage**: Extremely optimized (lower than alternatives)
- **Battery**: Optimized for mobile (30% lower power than OpenSL ES/vDSP)
- **Accuracy**: Professional-grade pitch detection

### Pros
✅ **Professional Grade**: Used by Spotify, Microsoft, top audio apps  
✅ **Ultra-low Latency**: Best-in-class performance  
✅ **Comprehensive**: All audio processing needs in one SDK  
✅ **Battle-tested**: Production-proven in major apps  
✅ **Excellent Support**: Priority support with commercial license  
✅ **Cross-platform**: Single codebase for iOS and Android

### Cons
❌ **Commercial License Required**: Significant annual cost  
❌ **Expensive**: $6,000/year per platform (Android)  
❌ **Not Open Source**: Proprietary, closed source  
❌ **Vendor Lock-in**: Dependency on third-party commercial vendor  
❌ **Overkill**: More features than needed for educational app  
❌ **Learning Curve**: Comprehensive SDK requires time to learn

### Licensing and Cost
- **Evaluation License**: Free for testing (max 1,000 installs, internal only)
- **White Label License (Production)**:
  - Android only: **$6,000/year**
  - Android + iOS: **$10,000/year**
  - Each additional platform: **$4,000/year**
- Per app licensing (each app requires separate license)
- No royalties or revenue sharing
- Fixed annual fee

### Use Cases for This App
- **Not recommended** for educational/open-source app
- Best for commercial apps with significant revenue
- Justified for professional audio apps (DAWs, synthesizers, pro tuners)
- Cost prohibitive for learning apps or indie projects

### License
**Commercial License** - Proprietary, closed source

❌ **Not suitable** for MIT-licensed open-source project

### Recommendation
**Rating: 2/5** - Excellent technology but cost-prohibitive for this project.

**Use Case**: Only consider if this becomes a commercial product with significant revenue. Not appropriate for educational/open-source app.

### References
- Website: https://superpowered.com/audio-library-sdk
- Pricing: https://superpowered.com/pricing
- Documentation: http://docs.superpowered.com/getting-started/licensing/
- Features Overview: https://superpowered.com/audio-overview

---

## 6. Open Source Guitar Tuner Projects

### Overview
Several open-source Android guitar tuner apps with battle-tested pitch detection implementations that can serve as reference implementations or direct code adaptation.

### 6.1 Guitar Tuner by eduardocorteslima

**Technology Stack**:
- Kotlin + Jetpack Compose
- Material Design 3
- TarsosDSP (YIN algorithm)
- Modern Android architecture

**Key Features**:
- Real-time pitch detection (±1 Hz accuracy in 60-500 Hz range)
- Noise filtering
- Multiple tuning modes
- Visual feedback
- Multi-language support

**License**: MIT

✅ **Directly usable** - Same license, modern Kotlin codebase

**GitHub**: https://github.com/eduardocorteslima/guitar-tuner

### 6.2 Tunify (JunkieTuner) by thestbar

**Technology Stack**:
- Java (planned Kotlin rewrite)
- YIN algorithm implementation
- Android native

**License**: MIT

✅ **Compatible** - Open source, educational resources provided

**GitHub**: https://github.com/thestbar/tunify

### 6.3 Tuner (F-Droid)

**Technology Stack**:
- Hybrid FFT + Autocorrelation
- Scientific-grade controls
- FFT window adjustments
- Pitch visualization
- Custom instruments/temperaments support

**License**: GNU GPL

⚠️ **License concern** - GPL requires source disclosure

**F-Droid**: https://f-droid.org/packages/de.moekadu.tuner/

### 6.4 Tunerly (F-Droid)

**Technology Stack**:
- Minimalistic design
- Support for guitar, bass, ukulele
- Multiple tunings
- Multilingual

**License**: GNU GPL

⚠️ **License concern** - GPL requires source disclosure

**F-Droid**: https://f-droid.org/packages/com.tunerly/

### Recommendation
**Rating: 4/5** - Excellent learning resources and potential code adaptation.

**Best Use**: Study these implementations, particularly eduardocorteslima's Guitar Tuner (MIT license, Kotlin, Compose) for direct code reference or adaptation.

**Action Items**:
1. Review eduardocorteslima's implementation for YIN integration patterns
2. Consider contributing to existing projects
3. Adapt algorithms (not full libraries) to avoid licensing issues

---

## 7. Algorithm Comparison: YIN vs Autocorrelation vs FFT

### Current Implementation: Autocorrelation
- **Type**: Time-domain periodicity detection
- **Accuracy**: ±2-5 Hz typical
- **Strengths**: Simple, fast, good fundamental detection
- **Weaknesses**: Octave errors, sensitive to noise

### YIN Algorithm
- **Type**: Enhanced autocorrelation with normalization
- **Accuracy**: ±1-2 Hz (±0.1 Hz with parabolic interpolation)
- **Strengths**: 
  - Fewer octave errors than autocorrelation
  - Better noise robustness
  - Industry standard for monophonic pitch detection
- **Weaknesses**:
  - Slightly more complex than basic autocorrelation
  - Still monophonic only
- **Recommendation**: **Best upgrade path from current implementation**

### FFT-based Methods
- **Type**: Frequency-domain analysis
- **Accuracy**: ±2-10 Hz (depends on window size)
- **Strengths**:
  - Fast (O(n log n))
  - Full spectrum information
  - Good for harmonic analysis
- **Weaknesses**:
  - Can mistake harmonics for fundamentals (especially guitars)
  - Sensitive to background noise
  - Resolution vs. latency tradeoff
- **Recommendation**: Combine with autocorrelation for hybrid approach

### Hybrid Approaches
- **Method**: FFT for initial estimate + autocorrelation/YIN for refinement
- **Accuracy**: ±0.5-1 Hz
- **Strengths**: Best of both worlds
- **Weaknesses**: More complex, higher CPU usage
- **Recommendation**: Ideal for professional tuner feature

### Performance Comparison Table

| Algorithm        | Accuracy | Latency | CPU Usage | Octave Errors | Noise Robustness |
|-----------------|----------|---------|-----------|---------------|------------------|
| Autocorrelation | ±2-5 Hz  | Low     | Low       | Moderate      | Moderate         |
| YIN             | ±1-2 Hz  | Low     | Low-Mod   | Low           | Good             |
| FFT             | ±2-10 Hz | Low     | Low       | High          | Poor             |
| Hybrid          | ±0.5-1 Hz| Moderate| Moderate  | Very Low      | Excellent        |
| CREPE (ML)      | ±0.1 Hz  | High    | High      | Very Low      | Excellent        |

---

## 8. Recommendations

### 8.1 Short-term (Immediate Improvement)

**Option A: Implement YIN Algorithm Directly**
- **Effort**: Medium (2-3 days)
- **Cost**: $0
- **License**: No issues (algorithm is public domain)
- **Benefits**: 
  - 2-5× accuracy improvement over current autocorrelation
  - No external dependencies
  - Maintains current Kotlin-native architecture
  - No license conflicts
- **Implementation**:
  1. Study YIN paper (De Cheveigné & Kawahara, 2002)
  2. Implement in Kotlin in PitchDetector.kt
  3. Add parabolic interpolation for sub-sample accuracy
  4. Keep existing autocorrelation as fallback

**Recommended Code Structure**:
```kotlin
class PitchDetector(private val sampleRate: Int = 44100) {
    fun detectPitch(audioData: FloatArray): Double? {
        // Try YIN first (more accurate)
        val yinResult = detectPitchYin(audioData)
        if (yinResult != null && isValidPitch(yinResult)) {
            return yinResult
        }
        
        // Fallback to autocorrelation
        return detectPitchAutocorrelation(audioData)
    }
    
    private fun detectPitchYin(audioData: FloatArray): Double? {
        // YIN implementation with cumulative mean normalized difference
        // Include parabolic interpolation for sub-sample accuracy
    }
    
    private fun detectPitchAutocorrelation(audioData: FloatArray): Double? {
        // Existing implementation
    }
}
```

**Priority**: ⭐⭐⭐⭐⭐ **HIGHEST RECOMMENDATION**

---

**Option B: Use Reference Implementation from eduardocorteslima**
- **Effort**: Low (1 day)
- **Cost**: $0
- **License**: MIT (fully compatible)
- **Benefits**:
  - Battle-tested YIN implementation
  - Modern Kotlin code
  - Already proven in production app
  - Easy integration
- **Approach**:
  1. Review their TarsosDSP integration
  2. Extract YIN algorithm understanding
  3. Implement similar pattern in our codebase
  4. Avoid TarsosDSP dependency (AGPL-3.0 license)

**Priority**: ⭐⭐⭐⭐ **HIGHLY RECOMMENDED**

---

### 8.2 Medium-term (Enhanced Features)

**Option C: Add TensorFlow Lite with SPICE Model**
- **Effort**: High (1-2 weeks)
- **Cost**: $0
- **License**: Apache 2.0 (compatible)
- **Benefits**:
  - State-of-the-art accuracy (±0.1 Hz)
  - Excellent noise robustness
  - Professional tuner quality
  - Future-proof (ML-based)
- **Drawbacks**:
  - Higher latency (30-100ms)
  - Increased APK size (5-10 MB)
  - Higher battery usage
  - More complex integration
- **Use Case**: Add as "Professional Mode" or dedicated tuner feature
- **Priority**: ⭐⭐⭐ **RECOMMENDED FOR TUNER FEATURE**

---

### 8.3 Long-term (Advanced Features)

**Option D: Hybrid Algorithm (FFT + YIN)**
- **Effort**: High (1-2 weeks)
- **Cost**: $0
- **Benefits**:
  - Maximum accuracy with traditional DSP
  - Fast initial detection (FFT) + precise refinement (YIN)
  - Professional-grade results
- **Implementation**:
  1. FFT for quick frequency estimation
  2. YIN for precise fundamental confirmation
  3. Confidence scoring to choose best result
- **Priority**: ⭐⭐ **NICE TO HAVE**

---

### 8.4 Not Recommended

**Option X: TarsosDSP Library**
- ❌ AGPL-3.0 license incompatible with MIT
- ❌ Would require relicensing entire app
- Alternative: Implement YIN directly (public domain algorithm)

**Option Y: Essentia Library**
- ❌ Overkill for current requirements
- ❌ AGPL-3.0 license incompatible
- ❌ High integration complexity
- Alternative: Use only if adding comprehensive music analysis

**Option Z: Superpowered SDK**
- ❌ $6,000+/year cost prohibitive
- ❌ Not suitable for educational/open-source app
- Alternative: Consider only for commercial pivot

**Option W: Google Oboe**
- ❌ Doesn't solve pitch detection problem
- ❌ Current AudioRecord implementation is sufficient
- Alternative: Consider only if need ultra-low latency I/O

---

## 9. Implementation Roadmap

### Phase 1: Immediate Accuracy Improvement (Week 1-2)
**Goal**: Improve pitch detection accuracy to ±1 Hz

1. **Implement YIN Algorithm** (3-4 days)
   - Study YIN paper and reference implementations
   - Implement in Kotlin as enhancement to PitchDetector.kt
   - Add parabolic interpolation
   - Write comprehensive unit tests
   - Compare with existing autocorrelation

2. **A/B Testing** (1-2 days)
   - Keep both algorithms in codebase
   - Add setting to switch between them
   - Collect accuracy metrics
   - Validate with real guitar input

3. **Optimization** (1-2 days)
   - Profile performance
   - Optimize hot paths
   - Ensure real-time performance maintained
   - Document parameter tuning

**Expected Outcome**: 2-5× accuracy improvement, maintained low latency

---

### Phase 2: Enhanced Tuner Feature (Week 3-5)
**Goal**: Add professional-grade tuner with ±0.1 Hz accuracy

1. **TensorFlow Lite Integration** (1 week)
   - Add TFLite dependencies
   - Convert/download SPICE model
   - Implement audio preprocessing
   - Create inference pipeline
   - Build confidence scoring

2. **Tuner UI** (3-4 days)
   - Create dedicated tuner screen
   - Add visual feedback (needle, cents display)
   - Implement string selection
   - Add alternative tunings support

3. **Mode Selection** (1-2 days)
   - "Fast Mode" (YIN - low latency)
   - "Accurate Mode" (CREPE/SPICE - high accuracy)
   - Auto-select based on device capability

**Expected Outcome**: Professional tuner feature competitive with commercial apps

---

### Phase 3: Advanced Features (Future)
**Goal**: Differentiation and advanced learning features

1. **Hybrid Pitch Detection** (1-2 weeks)
   - Implement FFT + YIN hybrid
   - Add confidence scoring
   - Optimize for guitar frequency range

2. **Chord Detection** (3-4 weeks)
   - Research polyphonic detection
   - Evaluate Essentia or custom FFT approach
   - Implement basic chord recognition
   - Add chord practice mode

3. **Audio Source Optimization** (1 week)
   - Experiment with Oboe for ultra-low latency
   - Compare with current AudioRecord
   - Implement if significant improvement

**Expected Outcome**: Advanced features, competitive advantage

---

## 10. Integration Guidelines

### 10.1 Architecture Considerations

**Current Architecture** (Preserve):
```
AudioManager (Kotlin)
├── AudioRecorder (Kotlin) - Audio capture + preprocessing
├── PitchDetector (Kotlin) - Pitch detection
└── NoteRecognizer (Kotlin) - Note conversion
```

**Enhanced Architecture** (Recommended):
```
AudioManager (Kotlin)
├── AudioRecorder (Kotlin) - Audio capture + preprocessing
├── PitchDetectionStrategy (Kotlin Interface)
│   ├── YinPitchDetector (Kotlin) - YIN algorithm [NEW]
│   ├── AutocorrelationPitchDetector (Kotlin) - Current implementation
│   ├── HybridPitchDetector (Kotlin) - FFT + YIN [FUTURE]
│   └── MLPitchDetector (Kotlin + TFLite) - CREPE/SPICE [FUTURE]
└── NoteRecognizer (Kotlin) - Note conversion
```

**Benefits**:
- Multiple algorithms available via strategy pattern
- Easy A/B testing
- Runtime algorithm selection
- Maintains Kotlin-native design
- No external library dependencies for core functionality

---

### 10.2 Kotlin Implementation Example

```kotlin
interface PitchDetectionStrategy {
    fun detectPitch(audioData: FloatArray): Double?
    val accuracy: Float  // Expected accuracy in Hz
    val latency: Int     // Expected latency in ms
}

class YinPitchDetector(
    private val sampleRate: Int = 44100,
    private val threshold: Float = 0.1f
) : PitchDetectionStrategy {
    
    override val accuracy = 1.0f  // ±1 Hz
    override val latency = 50     // ~50ms
    
    override fun detectPitch(audioData: FloatArray): Double? {
        if (audioData.isEmpty()) return null
        
        // Step 1: Calculate difference function
        val difference = calculateDifference(audioData)
        
        // Step 2: Cumulative mean normalized difference
        val normalizedDifference = cumulativeMeanNormalizedDifference(difference)
        
        // Step 3: Absolute threshold
        val tau = absoluteThreshold(normalizedDifference, threshold)
            ?: return null
        
        // Step 4: Parabolic interpolation
        val refinedTau = parabolicInterpolation(normalizedDifference, tau)
        
        // Step 5: Convert to frequency
        val frequency = sampleRate.toDouble() / refinedTau
        
        return if (frequency in 60.0..1500.0) frequency else null
    }
    
    private fun calculateDifference(audioData: FloatArray): FloatArray {
        // Implement difference function from YIN paper
        // ...
    }
    
    private fun cumulativeMeanNormalizedDifference(
        difference: FloatArray
    ): FloatArray {
        // Implement CMND from YIN paper
        // ...
    }
    
    private fun absoluteThreshold(
        normalized: FloatArray, 
        threshold: Float
    ): Int? {
        // Find first value below threshold
        // ...
    }
    
    private fun parabolicInterpolation(
        normalized: FloatArray,
        tau: Int
    ): Double {
        // Parabolic interpolation for sub-sample accuracy
        // ...
    }
}

// Usage in AudioManager
class AudioManager(context: Context) {
    private val pitchDetector: PitchDetectionStrategy = YinPitchDetector()
    
    // Can switch at runtime:
    fun setPitchDetectionMode(mode: PitchDetectionMode) {
        pitchDetector = when (mode) {
            PitchDetectionMode.FAST -> AutocorrelationPitchDetector()
            PitchDetectionMode.ACCURATE -> YinPitchDetector()
            PitchDetectionMode.PROFESSIONAL -> MLPitchDetector()
        }
    }
}
```

---

### 10.3 Testing Strategy

**Unit Tests**:
```kotlin
@Test
fun `YIN detector finds A440 within 1 Hz`() {
    val detector = YinPitchDetector(sampleRate = 44100)
    val audioData = generateSineWave(440.0, duration = 0.1, sampleRate = 44100)
    
    val detectedPitch = detector.detectPitch(audioData)
    
    assertNotNull(detectedPitch)
    assertEquals(440.0, detectedPitch!!, delta = 1.0) // Within ±1 Hz
}

@Test
fun `YIN detector handles low E string (82 Hz)`() {
    val detector = YinPitchDetector(sampleRate = 44100)
    val audioData = generateSineWave(82.41, duration = 0.1, sampleRate = 44100)
    
    val detectedPitch = detector.detectPitch(audioData)
    
    assertNotNull(detectedPitch)
    assertEquals(82.41, detectedPitch!!, delta = 1.0)
}

@Test
fun `YIN detector rejects noise`() {
    val detector = YinPitchDetector(sampleRate = 44100)
    val audioData = generateWhiteNoise(size = 4096)
    
    val detectedPitch = detector.detectPitch(audioData)
    
    assertNull(detectedPitch) // Should not detect pitch in pure noise
}
```

**Integration Tests**:
- Real guitar audio samples from different strings
- Various noise levels
- Different guitar types (acoustic, electric)
- Out-of-tune notes
- Damped/sustained notes

---

## 11. License Compatibility Matrix

| Library/Solution | License | Compatible with MIT | Requires Attribution | Source Disclosure Required | Commercial Use |
|-----------------|---------|---------------------|---------------------|---------------------------|----------------|
| YIN (Algorithm) | Public Domain | ✅ Yes | ❌ No | ❌ No | ✅ Yes |
| TensorFlow Lite | Apache 2.0 | ✅ Yes | ✅ Yes | ❌ No | ✅ Yes |
| CREPE (Model) | Apache 2.0 | ✅ Yes | ✅ Yes | ❌ No | ✅ Yes |
| Google Oboe | Apache 2.0 | ✅ Yes | ✅ Yes | ❌ No | ✅ Yes |
| TarsosDSP | AGPL-3.0 | ❌ No | ✅ Yes | ✅ Yes | ⚠️ With restrictions |
| Essentia | AGPL-3.0 | ❌ No | ✅ Yes | ✅ Yes | ⚠️ With restrictions |
| Superpowered | Commercial | ⚠️ With license | ❌ No | ❌ No | ⚠️ Annual fee |
| eduardocorteslima/guitar-tuner | MIT | ✅ Yes | ✅ Yes | ❌ No | ✅ Yes |

**Legend**:
- ✅ Yes: Fully compatible/allowed
- ❌ No: Not compatible/not required
- ⚠️ With restrictions: Special conditions apply

**Recommendation**: Prioritize MIT, Apache 2.0, or public domain solutions to maintain current project license and avoid legal complexities.

---

## 12. Cost-Benefit Analysis

### Option A: Implement YIN Directly
- **Development Cost**: 2-3 days ($0 if in-house)
- **Ongoing Cost**: $0
- **Accuracy Gain**: 2-5×
- **Complexity**: Low
- **Risk**: Low
- **ROI**: ⭐⭐⭐⭐⭐ **HIGHEST**

### Option B: TensorFlow Lite + SPICE
- **Development Cost**: 1-2 weeks ($0 if in-house)
- **Ongoing Cost**: $0
- **Accuracy Gain**: 10-50×
- **Complexity**: High
- **Risk**: Medium (latency, battery)
- **ROI**: ⭐⭐⭐⭐ **HIGH**

### Option C: TarsosDSP Library
- **Development Cost**: 1-2 days
- **Ongoing Cost**: $0 (but license change required)
- **Accuracy Gain**: 2-5×
- **Complexity**: Low
- **Risk**: High (license incompatibility)
- **ROI**: ⭐⭐ **LOW** (legal risk)

### Option D: Superpowered SDK
- **Development Cost**: 1-2 weeks
- **Ongoing Cost**: $6,000/year
- **Accuracy Gain**: 10-50×
- **Complexity**: High
- **Risk**: High (vendor lock-in, cost)
- **ROI**: ⭐ **VERY LOW** (cost prohibitive)

**Recommendation**: Option A for immediate improvement, Option B for enhanced tuner feature.

---

## 13. Conclusion

### Final Recommendations

**Immediate Action (Next Sprint)**:
1. **Implement YIN algorithm directly in Kotlin**
   - No external dependencies
   - No license conflicts
   - 2-5× accuracy improvement
   - Low complexity, low risk
   - Maintains current architecture

2. **Study eduardocorteslima's guitar-tuner repository**
   - MIT license, fully compatible
   - Modern Kotlin + Compose reference
   - Learn YIN integration patterns
   - Validate implementation approach

**Medium-term Enhancement (Future Sprint)**:
3. **Add TensorFlow Lite + SPICE for tuner feature**
   - State-of-the-art accuracy
   - Professional-grade tuning
   - Apache 2.0 license compatible
   - Positions app competitively

**Not Recommended**:
- ❌ TarsosDSP (license incompatibility)
- ❌ Essentia (overkill + license issues)
- ❌ Superpowered (cost prohibitive)
- ❌ Oboe alone (doesn't solve pitch detection)

### Success Metrics

After implementing YIN:
- Pitch detection accuracy: ±1-2 Hz (currently ±2-5 Hz)
- Maintained latency: < 100ms
- No new dependencies
- No license changes
- Backward compatible

After adding TFLite + SPICE (optional):
- Tuner accuracy: ±0.1 Hz
- Professional tuner feature
- Competitive with commercial tuner apps

### Next Steps

1. Create implementation task for YIN algorithm
2. Set up development branch
3. Implement YIN in PitchDetector.kt
4. Write comprehensive tests
5. Profile and optimize
6. Document tuning parameters
7. Update AUDIO_DETECTION_ANALYSIS.md

---

## 14. References

### Academic Papers
- De Cheveigné, A., & Kawahara, H. (2002). "YIN, a fundamental frequency estimator for speech and music." The Journal of the Acoustical Society of America, 111(4), 1917-1930.

### Documentation Links
- TarsosDSP: https://github.com/JorenSix/TarsosDSP
- Google Oboe: https://github.com/google/oboe
- Essentia: https://github.com/MTG/essentia
- CREPE: https://github.com/marl/crepe
- TensorFlow Lite: https://www.tensorflow.org/lite
- SPICE Model: https://www.tensorflow.org/hub/tutorials/spice

### Open Source Projects
- Guitar Tuner (eduardocorteslima): https://github.com/eduardocorteslima/guitar-tuner
- Tunify: https://github.com/thestbar/tunify
- F-Droid Tuner: https://f-droid.org/packages/de.moekadu.tuner/

### Additional Resources
- Pitch Detection Algorithms Comparison: https://pitchdetector.com/
- FFT vs Autocorrelation for Guitar Tuning: HackerNoon Guitar Tuner guide
- Android Audio Best Practices: developer.android.com

---

**Document Version**: 1.0  
**Date**: 2025-11-13  
**Author**: Research for Android Guitar Notes Learner  
**Status**: Complete - Ready for implementation planning
