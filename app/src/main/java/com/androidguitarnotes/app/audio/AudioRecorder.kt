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
     * Starts recording and returns a flow of audio data chunks.
     *
     * Note: RECORD_AUDIO permission must be granted before calling this method.
     * Permission handling is managed by the calling ViewModel/UI layer.
     *
     * @return Flow of ShortArray containing audio samples
     * @throws SecurityException if RECORD_AUDIO permission is not granted
     * @throws IllegalStateException if AudioRecord initialization fails
     */
    @SuppressLint("MissingPermission")
    fun startRecording(): Flow<ShortArray> =
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
                        emit(audioData)
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
