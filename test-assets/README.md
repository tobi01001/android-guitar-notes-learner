# Test Audio Assets

This directory contains tools and generated audio files for testing the Android Guitar Notes Learner app's audio detection and pitch recognition capabilities.

## Purpose

The test WAV files provide a controlled set of audio samples with:
- **Known frequencies**: 16 chromatic notes from 40 Hz to ~95 Hz
- **Varying volume levels**: 16 different amplitude levels from very quiet to very loud
- **Consistent duration**: Each note lasts approximately 1 second
- **Simple waveforms**: Pure sine waves for predictable testing

These files are useful for:
- Testing pitch detection accuracy across frequency ranges
- Validating volume sensitivity and auto-adjust features
- Debugging audio input processing
- Verifying microphone and audio source selection
- Benchmarking performance across different devices

## Files

- **generate_test_wavs.py**: Python script to generate test WAV files
- **test_volume_01.wav to test_volume_16.wav**: Generated test files (16 total)
  - Each file contains 16 one-second notes sweeping through chromatic frequencies
  - Volume increases progressively from file 01 (quietest) to file 16 (loudest)

## Requirements

To run the generation script, you need:
- Python 3.6 or higher
- `numpy` library
- `scipy` library

Install dependencies:
```bash
pip install numpy scipy
```

## Generating Test Files

Run the script from this directory:

```bash
cd test-assets
python3 generate_test_wavs.py
```

The script will generate 16 WAV files in the current directory:
- `test_volume_01.wav` (lowest volume, ~1% amplitude)
- `test_volume_02.wav` (~2% amplitude)
- ...
- `test_volume_16.wav` (highest volume, 100% amplitude)

Each file contains:
- 16 chromatic notes (40.00 Hz, 42.38 Hz, 44.90 Hz, ..., up to ~95.14 Hz)
- Each note lasts 1 second
- Total duration: ~16 seconds per file
- Sample rate: 44100 Hz
- Format: 16-bit PCM WAV

## Using Test Files with Android Devices

### Method 1: Transfer via ADB (Recommended for Emulator)

1. **Start your emulator or connect your device**
   ```bash
   # List connected devices
   adb devices
   ```

2. **Push files to device storage**
   ```bash
   # Push all test files to Music directory
   adb push test_volume_*.wav /sdcard/Music/
   
   # Or push to Downloads directory
   adb push test_volume_*.wav /sdcard/Download/
   ```

3. **Verify files were transferred**
   ```bash
   adb shell ls -l /sdcard/Music/test_volume_*.wav
   ```

4. **Play files using a media player app** on the device or use them as input for testing

### Method 2: Play Files and Record on Real Device

1. **Play the test files on your computer** using any audio player
   - Adjust system volume to desired level
   - Use speakers or headphones positioned near the device's microphone

2. **Open the Android Guitar Notes Learner app** on your device

3. **Start a practice session or tuner** to test audio detection

4. **Play the test files** and observe the app's response to different volume levels and frequencies

### Method 3: Direct Audio Playback (Testing)

For more controlled testing, you can:

1. **Push files to device** (as in Method 1)

2. **Use adb to play audio directly** (requires device with `stagefright` or similar):
   ```bash
   # Play a specific test file
   adb shell am start -a android.intent.action.VIEW -d file:///sdcard/Music/test_volume_01.wav -t audio/wav
   ```

### Method 4: Use as Input for Automated Testing

For automated testing scenarios:
1. Transfer files to the device
2. Use Android's MediaPlayer or ExoPlayer in test code to play files
3. Capture the app's response programmatically
4. Validate pitch detection results against expected frequencies

## Testing Recommendations

1. **Start with mid-range volume** (files 07-10) to establish baseline
2. **Test low volumes** (files 01-05) to verify sensitivity adjustments
3. **Test high volumes** (files 12-16) to check for saturation/clipping
4. **Compare frequencies** within each file to validate pitch detection across the range
5. **Test with different audio sources** (Microphone, Unprocessed, Voice Recognition)
6. **Enable auto-adjust sensitivity** and observe behavior with different volume levels

## Technical Details

### Frequency Generation
- Starting frequency: 40 Hz
- Semitone calculation: `f(n) = f0 × 2^(n/12)` where f0 = 40 Hz
- 16 semitones span approximately 2.5 octaves

### Amplitude Levels
- Logarithmic spacing for perceptually uniform volume steps
- Range: 0.01 (1%) to 1.0 (100%) of full scale
- Formula: `amplitude = 10^(log_min + i × (log_max - log_min) / (num_files - 1))`

### Audio Format
- Sample rate: 44100 Hz (CD quality)
- Bit depth: 16-bit PCM
- Channels: Mono
- No compression or effects applied

## Troubleshooting

**Issue**: Files not playing on device
- Ensure the file format is supported (WAV should work on most Android devices)
- Check file permissions after transfer
- Try moving files to a different directory (Music, Download, or Documents)

**Issue**: Script fails to generate files
- Verify numpy and scipy are installed: `python3 -c "import numpy, scipy; print('OK')"`
- Check Python version: `python3 --version` (should be 3.6+)
- Ensure write permissions in the current directory

**Issue**: ADB command not found
- Install Android SDK Platform Tools
- Add platform-tools to your PATH
- Verify with: `adb --version`

## Related Documentation

- Main README: `../README.md` (app overview and features)
- Audio Detection Analysis: `../AUDIO_DETECTION_ANALYSIS.md`
- Implementation Summary: `../IMPLEMENTATION_SUMMARY.md`

## License

These test assets are part of the Android Guitar Notes Learner project and are provided under the same MIT license as the main project.
