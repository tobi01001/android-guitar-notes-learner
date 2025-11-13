package com.androidguitarnotes.app.audio

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Manages audio recording, pitch detection, and note recognition.
 * 
 * ## Canonical Note Detection Model
 * 
 * All audio analysis results are modeled using a consistent DetectedNote data structure.
 * This provides:
 * - Predictable, non-null result for every frame
 * - Unified handling of detected/undetected states
 * - Consistent confidence and audio level information
 * - No need to check multiple result types downstream
 */
class AudioManager {
    private val audioRecorder = AudioRecorder()
    private val pitchDetector = PitchDetector()
    private val noteRecognizer = NoteRecognizer()

    /**
     * Canonical data model for note detection results.
     * 
     * This structure is always populated, even when no note is detected.
     * Downstream code can rely on a consistent interface without null checks.
     * 
     * @param isDetected True if a clear note was detected, false otherwise
     * @param noteName Note name (e.g., "A", "C#") or "?" if not detected
     * @param frequency Detected frequency in Hz, or null if not detected
     * @param cents Deviation from perfect pitch in cents, or 0.0 if not detected
     * @param confidence Detection confidence (0.0 to 1.0), or 0.0 if not detected
     * @param audioLevel RMS audio level (0.0 to 1.0)
     * @param octave Octave number, or -1 if not detected
     * @param noteNameWithOctave Note name with octave (e.g., "A4") or "?" if not detected
     * @param isGated True if signal was gated (below noise threshold)
     */
    data class DetectedNote(
        val isDetected: Boolean,
        val noteName: String,
        val frequency: Double?,
        val cents: Double,
        val confidence: Float,
        val audioLevel: Float,
        val octave: Int,
        val noteNameWithOctave: String,
        val isGated: Boolean,
    )

    /**
     * Represents the result of audio analysis.
     * 
     * @deprecated Use DetectedNote directly. This sealed class is maintained for backward compatibility.
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

        data class Gated(
            val audioLevel: Float,
        ) : AudioAnalysisResult()
    }

    /**
     * Starts listening for audio and analyzing pitch with canonical DetectedNote model.
     *
     * @param sensitivityMultiplier Base multiplier for audio sensitivity (0.5 to 2.0, default 1.0)
     * @param audioSource Audio source to use, or null to auto-select
     * @param noiseGateThreshold RMS threshold below which signal is gated (default 0.01f)
     * @param autoAdjustEnabled Whether to enable auto-adjust sensitivity feature
     * @return Flow of DetectedNote (always populated, never null)
     */
    fun startListeningWithDetectedNote(
        sensitivityMultiplier: Float = 1.0f,
        audioSource: Int? = null,
        noiseGateThreshold: Float = 0.01f,
        autoAdjustEnabled: Boolean = false,
    ): Flow<DetectedNote> =
        audioRecorder
            .startRecording(sensitivityMultiplier, audioSource, autoAdjustEnabled, noiseGateThreshold)
            .map { audioDataWithLevel ->
                // If signal is gated (below threshold), return default DetectedNote
                if (audioDataWithLevel.isGated) {
                    DetectedNote(
                        isDetected = false,
                        noteName = "?",
                        frequency = null,
                        cents = 0.0,
                        confidence = 0f,
                        audioLevel = audioDataWithLevel.level,
                        octave = -1,
                        noteNameWithOctave = "?",
                        isGated = true,
                    )
                } else {
                    val pitchResult = pitchDetector.detectPitchWithConfidence(audioDataWithLevel.audioData)

                    if (pitchResult != null) {
                        val recognizedNote = noteRecognizer.recognizeNote(pitchResult.frequency)
                        DetectedNote(
                            isDetected = true,
                            noteName = recognizedNote.noteName,
                            frequency = recognizedNote.frequency,
                            cents = recognizedNote.cents,
                            confidence = pitchResult.confidence,
                            audioLevel = audioDataWithLevel.level,
                            octave = recognizedNote.octave,
                            noteNameWithOctave = recognizedNote.noteNameWithOctave,
                            isGated = false,
                        )
                    } else {
                        DetectedNote(
                            isDetected = false,
                            noteName = "?",
                            frequency = null,
                            cents = 0.0,
                            confidence = 0f,
                            audioLevel = audioDataWithLevel.level,
                            octave = -1,
                            noteNameWithOctave = "?",
                            isGated = false,
                        )
                    }
                }
            }

    /**
     * Starts listening for audio and analyzing pitch.
     * 
     * @deprecated Use startListeningWithDetectedNote() for better consistency. This method is maintained for backward compatibility.
     *
     * @param sensitivityMultiplier Base multiplier for audio sensitivity (0.5 to 2.0, default 1.0)
     * @param audioSource Audio source to use, or null to auto-select
     * @param noiseGateThreshold RMS threshold below which signal is gated (default 0.01f)
     * @param autoAdjustEnabled Whether to enable auto-adjust sensitivity feature
     * @return Flow of AudioAnalysisResult
     */
    fun startListening(
        sensitivityMultiplier: Float = 1.0f,
        audioSource: Int? = null,
        noiseGateThreshold: Float = 0.01f,
        autoAdjustEnabled: Boolean = false,
    ): Flow<AudioAnalysisResult> =
        audioRecorder
            .startRecording(sensitivityMultiplier, audioSource, autoAdjustEnabled, noiseGateThreshold)
            .map { audioDataWithLevel ->
                // If signal is gated (below threshold), skip pitch detection
                if (audioDataWithLevel.isGated) {
                    AudioAnalysisResult.Gated(
                        audioLevel = audioDataWithLevel.level,
                    )
                } else {
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
