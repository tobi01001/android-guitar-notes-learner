package com.androidguitarnotes.app.audio

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Manages audio recording, pitch detection, and note recognition.
 */
class AudioManager {
    private val audioRecorder = AudioRecorder()
    private val pitchDetector = PitchDetector()
    private val noteRecognizer = NoteRecognizer()

    /**
     * Represents the result of audio analysis.
     */
    sealed class AudioAnalysisResult {
        data class NoteDetected(
            val noteName: String,
            val frequency: Double,
            val cents: Double,
            val audioLevel: Float,
            val octave: Int,
            val noteNameWithOctave: String,
        ) : AudioAnalysisResult()

        data class NoNoteDetected(
            val audioLevel: Float,
        ) : AudioAnalysisResult()
    }

    /**
     * Starts listening for audio and analyzing pitch.
     *
     * @param sensitivityMultiplier Base multiplier for audio sensitivity (0.5 to 2.0, default 1.0)
     * @param audioSource Audio source to use, or null to auto-select
     * @param autoAdjustEnabled Whether to enable auto-adjust sensitivity feature
     * @return Flow of AudioAnalysisResult
     */
    fun startListening(
        sensitivityMultiplier: Float = 1.0f,
        audioSource: Int? = null,
        autoAdjustEnabled: Boolean = false,
    ): Flow<AudioAnalysisResult> =
        audioRecorder
            .startRecording(sensitivityMultiplier, audioSource, autoAdjustEnabled)
            .map { audioDataWithLevel ->
                val frequency = pitchDetector.detectPitch(audioDataWithLevel.audioData)

                if (frequency != null) {
                    val recognizedNote = noteRecognizer.recognizeNote(frequency)
                    AudioAnalysisResult.NoteDetected(
                        noteName = recognizedNote.noteName,
                        frequency = recognizedNote.frequency,
                        cents = recognizedNote.cents,
                        audioLevel = audioDataWithLevel.level,
                        octave = recognizedNote.octave,
                        noteNameWithOctave = recognizedNote.noteNameWithOctave,
                    )
                } else {
                    AudioAnalysisResult.NoNoteDetected(
                        audioLevel = audioDataWithLevel.level,
                    )
                }
            }

    /**
     * Stops listening for audio.
     */
    fun stopListening() {
        audioRecorder.stopRecording()
    }

    /**
     * Checks if currently listening.
     */
    fun isListening(): Boolean = audioRecorder.isRecording()
}
