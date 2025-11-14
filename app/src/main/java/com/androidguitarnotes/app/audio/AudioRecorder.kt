package com.androidguitarnotes.app.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

/**
 * Records audio from microphone and provides audio samples for pitch detection.
 *
 * ## Audio Processing Pipeline (Split Path Design)
 *
 * The audio processing pipeline uses split paths to avoid phase distortion in pitch detection:
 * 1. **Raw audio capture** from microphone (44.1 kHz, mono, PCM float)
 * 2. **Float conversion** - Samples already in float format from Android AudioRecord
 * 3. **Pre-processing RMS** - Calculate raw RMS for auto-adjust algorithm (only when auto-adjust is enabled)
 * 4. **Auto-adjust sensitivity** - Dynamically adjusts gain based on rolling RMS window (if enabled)
 * 5. **Sensitivity gain application** - Applies combined manual + auto-adjust gain multiplier
 *    - **CRITICAL:** No hard clamping applied during analysis chain
 *    - Hard clamping creates irreversible distortion and loss of harmonics
 *    - Clamping only appropriate before playback/output step (not in analysis)
 * 6. **Signal split:**
 *    - **Path A (Pitch Detection):** Unfiltered audio → pitch detection algorithms
 *    - **Path B (Level Display):** High-pass filtered audio → RMS calculation for visual feedback
 * 7. **Noise gate check** - Uses unfiltered RMS to check if signal passes threshold
 * 8. **Emit to flow** - Sends UNFILTERED audio for pitch detection
 *
 * ## Why Split Paths?
 *
 * IIR high-pass filters introduce **phase distortion** that causes systematic frequency errors
 * at low frequencies. Even with a 40 Hz cutoff (well below E2 at 82 Hz):
 * - E2 (82.4 Hz) is detected as 88.8 Hz (+6.4 Hz, +129 cents) → appears as F2 instead of E2
 * - D3 (146.8 Hz) is detected as 153.3 Hz (+6.5 Hz, +75 cents) → appears as D#3
 * - Error decreases as frequency increases
 * 
 * This affects ALL pitch detection algorithms (YIN, autocorrelation, FFT) because the phase
 * distortion shifts zero-crossings in time, making period measurement incorrect.
 *
 * **Solution:** Remove filter from pitch detection path, but keep it for RMS calculation
 * to provide clean visual level feedback without rumble fluctuations.
 *
 * ## Processing Order Rationale
 *
 * - **Auto-adjust before gain:** Uses raw RMS to determine appropriate gain adjustment
 * - **No filtering for pitch detection:** Preserves accurate phase relationships for period detection
 * - **No hard clamping:** Preserves harmonics and signal quality for pitch detection
 * - **Filtering only for level display:** Removes rumble from visual feedback without affecting accuracy
 * - **Gate uses unfiltered RMS:** True signal energy check, not influenced by filter
 *
 * ## High-Pass Filter (Level Display Only)
 *
 * A one-pole IIR high-pass filter with 40 Hz cutoff is applied ONLY to the level calculation path.
 * This provides clean visual feedback by removing:
 * - Low-frequency handling noise (bumps, taps)
 * - Environmental rumble (traffic, wind, HVAC)
 * - DC offset and subsonic content
 *
 * The filter is NOT applied to the pitch detection signal to avoid phase distortion errors.
 *
 * ## Microphone Sensitivity
 *
 * ### Manual Sensitivity Control
 * The `sensitivityMultiplier` parameter (range 0.5 to 2.0) directly multiplies the audio samples,
 * effectively increasing or decreasing the gain before pitch detection. This is controlled by the
 * user via the microphone sensitivity slider in settings.
 * - Values < 1.0: Reduce sensitivity (useful for loud environments or strong pickups)
 * - Values = 1.0: No adjustment (default)
 * - Values > 1.0: Increase sensitivity (useful for quiet guitars or low-quality microphones)
 *
 * ### Auto-Adjust Sensitivity
 * When enabled, auto-adjust sensitivity dynamically adjusts the sensitivity multiplier based on
 * the incoming audio signal level (RMS) to maintain optimal levels for pitch detection.
 * This works in conjunction with (not replace) the manual sensitivity slider, where:
 * - The manual slider sets a base multiplier
 * - Auto-adjust applies dynamic fine-tuning on top of the base multiplier
 * - The combined effect is: `finalSensitivity = baseSensitivity * autoAdjustFactor`
 *
 * **Implementation (per AUDIO_DETECTION_ANALYSIS.md Section 7.2.3):**
 * - Tracks RMS level over a rolling window (approximately 1 second)
 * - Calculates proportional error: `error = targetRMS / (actualRMS + epsilon)`
 * - Applies smooth per-step adjustment limited to 0.9x-1.1x per iteration
 * - Multiplies current gain by adjustment: `gain *= adjustment`
 * - Clamps final gain to safe bounds (0.5x to 2.0x)
 * - Weak signals (RMS < target): gradually increase gain toward 2.0x
 * - Strong signals (RMS > target): gradually reduce gain toward 0.5x
 *
 * ### Base Sensitivity and Microphone Input
 * The base sensitivity is determined by:
 * 1. Hardware microphone characteristics (gain, frequency response)
 * 2. Android audio source selection (UNPROCESSED, VOICE_RECOGNITION, MIC)
 *    - UNPROCESSED: Raw audio with minimal processing (best for pitch detection)
 *    - VOICE_RECOGNITION: Optimized for speech (may apply noise reduction)
 *    - MIC: General-purpose microphone input
 * 3. Device-specific audio processing (AGC, noise suppression, etc.)
 *
 * ### Sensitivity Issues (e.g., Fairphone 6)
 * If sensitivity seems too low on certain devices:
 * 1. Increase the manual sensitivity slider above 1.0
 * 2. Try different audio sources in settings (e.g., UNPROCESSED vs MIC)
 * 3. Ensure the device's microphone is not blocked or damaged
 * 4. Check if the device has aggressive AGC that may need to be disabled in system settings
 */
class AudioRecorder {
    companion object {
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_FLOAT
        private const val BUFFER_SIZE_MULTIPLIER = 2

        // Auto-adjust sensitivity constants (per AUDIO_DETECTION_ANALYSIS.md Section 7.2.3)
        private const val AUTO_ADJUST_MIN_FACTOR = 0.5f
        private const val AUTO_ADJUST_MAX_FACTOR = 2.0f
        private const val AUTO_ADJUST_TARGET_RMS = 0.1f // Target RMS level for optimal detection
        private const val RMS_WINDOW_SIZE = 44 // Number of buffers for RMS window (~1 second)

        /**
         * Selects the best audio source for pitch detection.
         * Prioritizes UNPROCESSED for highest precision (API 29+),
         * falls back to VOICE_RECOGNITION, then MIC as last resort.
         */
        private fun selectBestAudioSource(): Int {
            // Try UNPROCESSED first (API 29+, best for pitch detection)
            val unprocessed = MediaRecorder.AudioSource.UNPROCESSED
            if (isAudioSourceAvailable(unprocessed)) {
                Log.d("AudioRecorder", "Using UNPROCESSED audio source")
                return unprocessed
            }

            // Try VOICE_RECOGNITION as fallback
            val voiceRecognition = MediaRecorder.AudioSource.VOICE_RECOGNITION
            if (isAudioSourceAvailable(voiceRecognition)) {
                Log.d("AudioRecorder", "Using VOICE_RECOGNITION audio source")
                return voiceRecognition
            }

            // Use MIC as last resort
            Log.d("AudioRecorder", "Using MIC audio source")
            return MediaRecorder.AudioSource.MIC
        }

        /**
         * Checks if an audio source is available on this device.
         */
        @SuppressLint("MissingPermission")
        private fun isAudioSourceAvailable(audioSource: Int): Boolean {
            return try {
                val bufferSize =
                    AudioRecord.getMinBufferSize(
                        SAMPLE_RATE,
                        CHANNEL_CONFIG,
                        AUDIO_FORMAT,
                    )
                if (bufferSize <= 0) return false

                val audioRecord =
                    AudioRecord(
                        audioSource,
                        SAMPLE_RATE,
                        CHANNEL_CONFIG,
                        AUDIO_FORMAT,
                        bufferSize,
                    )
                val available = audioRecord.state == AudioRecord.STATE_INITIALIZED
                audioRecord.release()
                available
            } catch (e: Exception) {
                Log.w("AudioRecorder", "Audio source $audioSource not available", e)
                false
            }
        }
    }

    @Volatile
    private var audioRecord: AudioRecord? = null
    private val bufferSize =
        AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
        ) * BUFFER_SIZE_MULTIPLIER

    // Auto-adjust sensitivity state
    @Volatile
    private var currentAutoAdjustFactor = 1.0f
    private val rmsHistory = ArrayDeque<Float>(RMS_WINDOW_SIZE)

    /**
     * Represents audio data with its level and gate status.
     */
    data class AudioDataWithLevel(
        val audioData: FloatArray,
        val level: Float,
        val isGated: Boolean = false,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is AudioDataWithLevel) return false
            if (!audioData.contentEquals(other.audioData)) return false
            if (level != other.level) return false
            if (isGated != other.isGated) return false
            return true
        }

        override fun hashCode(): Int {
            var result = audioData.contentHashCode()
            result = 31 * result + level.hashCode()
            result = 31 * result + isGated.hashCode()
            return result
        }
    }

    /**
     * Starts recording and returns a flow of audio data chunks with level information.
     *
     * Note: RECORD_AUDIO permission must be granted before calling this method.
     * Permission handling is managed by the calling ViewModel/UI layer.
     *
     * @param sensitivityMultiplier Base multiplier for audio sensitivity (0.5 to 2.0, default 1.0)
     * @param preferredAudioSource Preferred audio source to use, or null to auto-select
     * @param autoAdjustEnabled Whether to enable auto-adjust sensitivity feature
     * @param noiseGateThreshold RMS threshold below which signal is gated (default 0.01f)
     * @return Flow of AudioDataWithLevel containing audio samples and level
     * @throws SecurityException if RECORD_AUDIO permission is not granted
     * @throws IllegalStateException if AudioRecord initialization fails
     * @throws IllegalArgumentException if sensitivityMultiplier is outside valid range
     */
    @SuppressLint("MissingPermission")
    fun startRecording(
        sensitivityMultiplier: Float = 1.0f,
        preferredAudioSource: Int? = null,
        autoAdjustEnabled: Boolean = false,
        noiseGateThreshold: Float = 0.01f,
    ): Flow<AudioDataWithLevel> =
        flow {
            require(sensitivityMultiplier in 0.5f..2.0f) {
                "Sensitivity multiplier must be between 0.5 and 2.0, got $sensitivityMultiplier"
            }

            try {
                // Reset auto-adjust state when starting new recording session
                currentAutoAdjustFactor = 1.0f
                rmsHistory.clear()

                val audioSource = preferredAudioSource ?: selectBestAudioSource()
                audioRecord =
                    AudioRecord(
                        audioSource,
                        SAMPLE_RATE,
                        CHANNEL_CONFIG,
                        AUDIO_FORMAT,
                        bufferSize,
                    )

                if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                    throw IllegalStateException("AudioRecord initialization failed- check permissions")
                }

                audioRecord?.startRecording()

                // Create high-pass filter to remove low-frequency rumble
                // Cutoff at 40 Hz (well below lowest guitar note E2 at ~82 Hz)
                // Lowered from 60 Hz to reduce phase distortion at E2 frequency
                val highPassFilter = HighPassFilter(sampleRate = SAMPLE_RATE, cutoffFrequency = 40.0)

                val buffer = FloatArray(bufferSize / 4)

                while (coroutineContext.isActive) {
                    val readResult = audioRecord?.read(buffer, 0, buffer.size, AudioRecord.READ_BLOCKING) ?: 0

                    if (readResult > 0) {
                        // Create a copy to avoid reusing the same buffer
                        val audioData = buffer.copyOf(readResult)

                        // Calculate combined sensitivity multiplier
                        val combinedMultiplier =
                            if (autoAdjustEnabled) {
                                // Calculate raw RMS before any adjustment for auto-adjust algorithm
                                val rawRms = calculateRawRms(audioData)
                                updateAutoAdjustFactor(rawRms)
                                sensitivityMultiplier * currentAutoAdjustFactor
                            } else {
                                sensitivityMultiplier
                            }
                        // Apply sensitivity multiplier to clean audio for pitch detection
                        val adjustedData = applySensitivity(audioData, combinedMultiplier)

                        // CRITICAL FIX: Do NOT apply high-pass filter to pitch detection signal
                        // The IIR high-pass filter introduces phase distortion that causes systematic
                        // frequency errors at low frequencies. For example, at 40 Hz cutoff:
                        // - E2 (82.4 Hz) appears as 88.8 Hz (+6.4 Hz, essentially F2)
                        // - D3 (146.8 Hz) appears as 153.3 Hz (+6.5 Hz, essentially D#3)
                        // This affects ALL pitch detection algorithms equally.
                        //
                        // Solution: Split signal paths:
                        // 1. Pitch detection: Use raw adjusted audio (no filtering)
                        // 2. Level display: Use filtered audio for clean RMS calculation
                        
                        // Create filtered copy for RMS/level calculation only
                        val filteredDataForLevel = adjustedData.copyOf()
                        highPassFilter.process(filteredDataForLevel)

                        // Calculate raw RMS for noise gate check (using unfiltered data)
                        val rawRms = calculateRawRms(adjustedData)

                        // Check if signal passes the noise gate
                        val isGated = rawRms < noiseGateThreshold

                        // Calculate audio level from filtered data (cleaner visual feedback)
                        val level = calculateAudioLevel(filteredDataForLevel)

                        // Emit unfiltered data for pitch detection
                        emit(AudioDataWithLevel(adjustedData, level, isGated))
                    } else if (readResult < 0) {
                        // Error reading audio data
                        Log.e("AudioRecorder", "Error reading audio: $readResult")
                        break
                    }
                }
            } catch (e: SecurityException) {
                Log.e("AudioRecorder", "SecurityException - missing RECORD_AUDIO permission", e)
                throw e
            } catch (e: Exception) {
                Log.e("AudioRecorder", "Error in audio recording", e)
                throw e
            } finally {
                stopRecording()
            }
        }.flowOn(Dispatchers.IO)

    /**
     * Calculates the raw RMS (Root Mean Square) value from audio samples.
     * Returns the unscaled RMS value.
     */
    private fun calculateRawRms(audioData: FloatArray): Float {
        if (audioData.isEmpty()) return 0f

        var sum = 0.0
        for (sample in audioData) {
            sum += sample * sample
        }
        return kotlin.math.sqrt(sum / audioData.size).toFloat()
    }

    /**
     * Calculates the audio level (RMS) from audio samples.
     * Returns a value between 0.0 and 1.0.
     *
     * Applies logarithmic scaling to improve visibility of low-level signals
     * and make the level meter more responsive to typical audio input ranges.
     */
    private fun calculateAudioLevel(audioData: FloatArray): Float {
        val rms = calculateRawRms(audioData)
        if (rms < 0.001f) return 0f // Below threshold

        // Apply logarithmic scaling for better visualization
        // This helps make low audio levels more visible
        // Using dB-like scaling: 20 * log10(rms) normalized to 0-1 range
        // Assuming typical guitar input ranges from -60dB to 0dB
        val db = 20f * kotlin.math.log10(rms.toDouble()).toFloat()
        // Map -60dB to 0.0 and 0dB to 1.0
        val normalizedLevel = ((db + 60f) / 60f).coerceIn(0f, 1f)

        return normalizedLevel
    }

    /**
     * Updates the auto-adjust sensitivity factor based on rolling window RMS.
     *
     * This implements the auto-adjust sensitivity algorithm as documented in
     * AUDIO_DETECTION_ANALYSIS.md Section 7.2.3:
     * 1. Maintains a rolling window of RMS values (approximately 1 second)
     * 2. Calculates average RMS over the window
     * 3. Computes proportional error relative to target RMS
     * 4. Applies smooth per-step adjustment (0.9x-1.1x per iteration)
     * 5. Clamps final gain to safe bounds (0.5x to 2.0x)
     *
     * @param rawRms Current raw RMS value from audio buffer
     */
    private fun updateAutoAdjustFactor(rawRms: Float) {
        // Add to rolling window
        rmsHistory.addLast(rawRms)
        if (rmsHistory.size > RMS_WINDOW_SIZE) {
            rmsHistory.removeFirst()
        }

        // Calculate average RMS over window
        val avgRms =
            if (rmsHistory.isNotEmpty()) {
                rmsHistory.average().toFloat()
            } else {
                rawRms
            }

        // Calculate proportional error to reach target RMS
        // Add small epsilon to avoid division by zero
        val error = AUTO_ADJUST_TARGET_RMS / (avgRms + 0.001f)

        // Smooth adjustment with per-step limits (0.9x to 1.1x)
        // This prevents abrupt jumps and provides gradual convergence
        val adjustment = error.coerceIn(0.9f, 1.1f)

        // Apply adjustment to current gain
        currentAutoAdjustFactor *= adjustment

        // Ensure factor stays within safe bounds
        currentAutoAdjustFactor =
            currentAutoAdjustFactor.coerceIn(
                AUTO_ADJUST_MIN_FACTOR,
                AUTO_ADJUST_MAX_FACTOR,
            )
    }

    /**
     * Applies sensitivity multiplier to audio data.
     *
     * Note: Per audio pipeline best practices, we do NOT hard-clamp samples during analysis.
     * Hard clamping creates irreversible distortion and loss of harmonics that hinders
     * autocorrelation and spectral analysis. Clamping should only occur before playback/output.
     */
    private fun applySensitivity(
        audioData: FloatArray,
        multiplier: Float,
    ): FloatArray {
        if (multiplier == 1.0f) return audioData

        return FloatArray(audioData.size) { i ->
            audioData[i] * multiplier
        }
    }

    /**
     * Stops recording and releases resources.
     */
    @Synchronized
    fun stopRecording() {
        audioRecord?.apply {
            if (state == AudioRecord.STATE_INITIALIZED) {
                stop()
            }
            release()
        }
        audioRecord = null
    }

    /**
     * Checks if recording is currently active.
     */
    fun isRecording(): Boolean = audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING
}
