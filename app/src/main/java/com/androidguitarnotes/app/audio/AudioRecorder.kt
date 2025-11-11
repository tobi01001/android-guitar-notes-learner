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
 */
class AudioRecorder {
    companion object {
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val BUFFER_SIZE_MULTIPLIER = 2
    }

    @Volatile
    private var audioRecord: AudioRecord? = null
    private val bufferSize =
        AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
        ) * BUFFER_SIZE_MULTIPLIER

    /**
     * Represents audio data with its level.
     */
    data class AudioDataWithLevel(
        val audioData: ShortArray,
        val level: Float,
    )

    /**
     * Starts recording and returns a flow of audio data chunks with level information.
     *
     * Note: RECORD_AUDIO permission must be granted before calling this method.
     * Permission handling is managed by the calling ViewModel/UI layer.
     *
     * @param sensitivityMultiplier Multiplier for audio sensitivity (0.5 to 2.0, default 1.0)
     * @return Flow of AudioDataWithLevel containing audio samples and level
     * @throws SecurityException if RECORD_AUDIO permission is not granted
     * @throws IllegalStateException if AudioRecord initialization fails
     */
    @SuppressLint("MissingPermission")
    fun startRecording(sensitivityMultiplier: Float = 1.0f): Flow<AudioDataWithLevel> =
        flow {
            try {
                audioRecord =
                    AudioRecord(
                        MediaRecorder.AudioSource.MIC,
                        SAMPLE_RATE,
                        CHANNEL_CONFIG,
                        AUDIO_FORMAT,
                        bufferSize,
                    )

                if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                    throw IllegalStateException("AudioRecord initialization failed- check permissions")
                }

                audioRecord?.startRecording()

                val buffer = ShortArray(bufferSize / 2)

                while (coroutineContext.isActive) {
                    val readResult = audioRecord?.read(buffer, 0, buffer.size) ?: 0

                    if (readResult > 0) {
                        // Create a copy to avoid reusing the same buffer
                        val audioData = buffer.copyOf(readResult)

                        // Calculate audio level (RMS)
                        val level = calculateAudioLevel(audioData)

                        // Apply sensitivity multiplier
                        val adjustedData = applySensitivity(audioData, sensitivityMultiplier)

                        emit(AudioDataWithLevel(adjustedData, level))
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
     * Calculates the audio level (RMS) from audio samples.
     * Returns a value between 0.0 and 1.0.
     */
    private fun calculateAudioLevel(audioData: ShortArray): Float {
        if (audioData.isEmpty()) return 0f

        var sum = 0.0
        for (sample in audioData) {
            val normalized = sample.toFloat() / Short.MAX_VALUE
            sum += normalized * normalized
        }

        val rms = kotlin.math.sqrt(sum / audioData.size)
        return rms.toFloat().coerceIn(0f, 1f)
    }

    /**
     * Applies sensitivity multiplier to audio data.
     */
    private fun applySensitivity(
        audioData: ShortArray,
        multiplier: Float,
    ): ShortArray {
        if (multiplier == 1.0f) return audioData

        return ShortArray(audioData.size) { i ->
            val adjusted = (audioData[i] * multiplier).toInt()
            adjusted.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
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
