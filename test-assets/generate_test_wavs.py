#!/usr/bin/env python3
"""
Generate test WAV files for audio testing purposes.

This script generates 16 WAV files, each representing a different volume level.
Each file contains a sweep through 16 chromatic notes (40 Hz to ~95 Hz),
with each note lasting approximately 1 second.

Requirements:
- numpy
- scipy

Usage:
    python3 generate_test_wavs.py
"""

import numpy as np
from scipy.io import wavfile
import os


def generate_frequency_sweep(sample_rate=44100, note_duration=1.0, num_notes=16):
    """
    Generate a frequency sweep through chromatic notes.
    
    Args:
        sample_rate: Sample rate in Hz (default: 44100)
        note_duration: Duration of each note in seconds (default: 1.0)
        num_notes: Number of chromatic notes to generate (default: 16)
    
    Returns:
        numpy array containing the frequency sweep
    """
    # Starting frequency (40 Hz)
    start_freq = 40.0
    
    # Calculate frequencies for each chromatic note
    # Each semitone is 2^(1/12) times the previous frequency
    frequencies = [start_freq * (2 ** (i / 12)) for i in range(num_notes)]
    
    # Generate samples for each note
    samples_per_note = int(sample_rate * note_duration)
    t = np.linspace(0, note_duration, samples_per_note, endpoint=False)
    
    # Concatenate all notes
    audio_data = []
    for freq in frequencies:
        # Generate sine wave for this note
        note_samples = np.sin(2 * np.pi * freq * t)
        audio_data.append(note_samples)
    
    # Concatenate all notes into a single array
    return np.concatenate(audio_data)


def generate_wav_files(output_dir=".", num_files=16, sample_rate=44100):
    """
    Generate WAV files with different volume levels.
    
    Args:
        output_dir: Directory to save WAV files (default: current directory)
        num_files: Number of files to generate (default: 16)
        sample_rate: Sample rate in Hz (default: 44100)
    """
    # Create output directory if it doesn't exist
    os.makedirs(output_dir, exist_ok=True)
    
    # Generate the base frequency sweep (normalized to -1.0 to 1.0)
    base_sweep = generate_frequency_sweep(sample_rate=sample_rate)
    
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
    print(f"Frequency range: 40.00 Hz to {40.0 * (2 ** (15 / 12)):.2f} Hz")
    print(f"Notes per file: 16 (each ~1 second)")
    print(f"Total duration per file: ~16 seconds")
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
