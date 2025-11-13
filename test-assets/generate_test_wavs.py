#!/usr/bin/env python3
"""
Generate test WAV files for audio testing purposes.

This script generates 16 WAV files, each representing a different volume level.
Each file contains a continuous frequency sweep from a starting frequency
through a specified number of chromatic notes, with seamless transitions.

Requirements:
- numpy
- scipy

Usage:
    python3 generate_test_wavs.py
"""

import numpy as np
from scipy.io import wavfile
import os


def generate_frequency_sweep(sample_rate=44100, start_freq=40.0, interval=1, num_notes=16):
    """
    Generate a continuous frequency sweep from min to max frequency.
    
    Args:
        sample_rate: Sample rate in Hz (default: 44100)
        start_freq: Starting frequency in Hz (default: 40.0)
        interval: Chromatic note difference per step (default: 1 = semitone)
        num_notes: Number of chromatic notes to cover (default: 16)
    
    Returns:
        numpy array containing the frequency sweep
    """
    # Calculate min and max frequencies based on interval
    # Each step is interval semitones, so frequency multiplier is 2^(interval/12)
    min_freq = start_freq
    max_freq = start_freq * (2 ** ((num_notes - 1) * interval / 12))
    
    # Calculate total time: approximately 1 second per chromatic note at the interval
    # Total chromatic notes covered = (num_notes - 1) * interval
    total_chromatic_notes = (num_notes - 1) * interval
    total_time = total_chromatic_notes * 1.0  # 1 second per chromatic note
    
    # Generate time array for the entire sweep
    total_samples = int(sample_rate * total_time)
    t = np.linspace(0, total_time, total_samples, endpoint=False)
    
    # Generate seamless frequency sweep from min to max
    # Use logarithmic frequency sweep for equal chromatic spacing perception
    # Instantaneous frequency: f(t) = min_freq * (max_freq/min_freq)^(t/total_time)
    # Phase: integral of 2*pi*f(t) dt
    frequency_ratio = max_freq / min_freq
    instantaneous_freq = min_freq * (frequency_ratio ** (t / total_time))
    
    # Calculate phase by integrating the instantaneous frequency
    # Phase(t) = 2*pi * integral(f(t) dt) = 2*pi * min_freq * total_time / ln(frequency_ratio) * (frequency_ratio^(t/total_time) - 1)
    phase = 2 * np.pi * min_freq * total_time / np.log(frequency_ratio) * (frequency_ratio ** (t / total_time) - 1)
    
    # Generate sine wave with varying frequency
    audio_data = np.sin(phase)
    
    return audio_data


def generate_wav_files(output_dir=".", num_files=16, sample_rate=44100, start_freq=40.0, interval=1, num_notes=16):
    """
    Generate WAV files with different volume levels.
    
    Args:
        output_dir: Directory to save WAV files (default: current directory)
        num_files: Number of files to generate (default: 16)
        sample_rate: Sample rate in Hz (default: 44100)
        start_freq: Starting frequency in Hz (default: 40.0)
        interval: Chromatic note difference per step (default: 1 = semitone)
        num_notes: Number of chromatic notes to cover (default: 16)
    """
    # Create output directory if it doesn't exist
    os.makedirs(output_dir, exist_ok=True)
    
    # Calculate frequency range
    min_freq = start_freq
    max_freq = start_freq * (2 ** ((num_notes - 1) * interval / 12))
    total_time = (num_notes - 1) * interval * 1.0
    
    # Generate the base frequency sweep (normalized to -1.0 to 1.0)
    base_sweep = generate_frequency_sweep(sample_rate=sample_rate, start_freq=start_freq, 
                                          interval=interval, num_notes=num_notes)
    
    # Generate amplitude levels from very low to very high
    # Using logarithmic scale for more perceptually uniform volume steps
    # Amplitude range: 0.01 (1%) to 1.0 (100%)
    min_amplitude = 0.01
    max_amplitude = 1.0
    
    # Calculate logarithmic spacing
    log_min = np.log10(min_amplitude)
    log_max = np.log10(max_amplitude)
    amplitudes = np.logspace(log_min, log_max, num_files)
    
    print(f"Generating {num_files} WAV files...")
    print(f"Sample rate: {sample_rate} Hz")
    print(f"Frequency range: {min_freq:.2f} Hz to {max_freq:.2f} Hz")
    print(f"Interval: {interval} semitone(s) per step")
    print(f"Chromatic notes covered: {(num_notes - 1) * interval}")
    print(f"Total duration per file: ~{total_time:.1f} seconds")
    print()
    
    for i, amplitude in enumerate(amplitudes, start=1):
        # Apply amplitude to base sweep
        audio_data = base_sweep * amplitude
        
        # Convert to 16-bit integer format
        # Scale to int16 range (-32768 to 32767)
        audio_data_int16 = (audio_data * 32767).astype(np.int16)
        
        # Generate filename
        filename = os.path.join(output_dir, f"test_volume_{i:02d}.wav")
        
        # Write WAV file
        wavfile.write(filename, sample_rate, audio_data_int16)
        
        print(f"Generated: {filename} (amplitude: {amplitude:.4f}, {amplitude*100:.2f}%)")
    
    print()
    print(f"Successfully generated {num_files} WAV files in '{output_dir}'")


def main():
    """Main entry point for the script."""
    # Generate files in the current directory
    generate_wav_files(output_dir=".", num_files=16, sample_rate=44100)


if __name__ == "__main__":
    main()
