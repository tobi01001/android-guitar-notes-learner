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
class AudioManager(
    private var algorithm: PitchDetectionAlgorithm = PitchDetectionAlgorithm.YIN,
) {
    private val audioRecorder = AudioRecorder()
    private var pitchDetector = PitchDetector(algorithm = algorithm)
    private val noteRecognizer = NoteRecognizer()
    private var confirmationFilter: PitchConfirmationFilter? = null

    /**
     * Updates the pitch detection algorithm.
     */
    fun setAlgorithm(newAlgorithm: PitchDetectionAlgorithm) {
        algorithm = newAlgorithm
        pitchDetector = PitchDetector(algorithm = algorithm)
    }

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
     * Starts listening for audio and analyzing pitch with canonical DetectedNote model.
     *
     * @param sensitivityMultiplier Base multiplier for audio sensitivity (0.5 to 2.0, default 1.0)
     * @param audioSource Audio source to use, or null to auto-select
     * @param noiseGateThreshold RMS threshold below which signal is gated (default 0.01f)
     * @param autoAdjustEnabled Whether to enable auto-adjust sensitivity feature
     * @param multiFrameConfirmationEnabled Whether to require consecutive frame confirmation (ENH-001)
     * @return Flow of DetectedNote (always populated, never null)
     */
    fun startListeningWithDetectedNote(
        sensitivityMultiplier: Float = 1.0f,
        audioSource: Int? = null,
        noiseGateThreshold: Float = 0.01f,
        autoAdjustEnabled: Boolean = false,
        multiFrameConfirmationEnabled: Boolean = false,
    ): Flow<DetectedNote> =
        audioRecorder
            .startRecording(sensitivityMultiplier, audioSource, autoAdjustEnabled, noiseGateThreshold)
            .map { audioDataWithLevel ->
                // Initialize or reset confirmation filter when recording starts
                if (multiFrameConfirmationEnabled && confirmationFilter == null) {
                    confirmationFilter = PitchConfirmationFilter(requiredConsecutiveFrames = 2)
                } else if (!multiFrameConfirmationEnabled && confirmationFilter != null) {
                    confirmationFilter = null
                }

                // Process the audio frame to get raw detection
                val rawDetection =
                    if (audioDataWithLevel.isGated) {
                        // If signal is gated (below threshold), return default DetectedNote
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

                // Apply multi-frame confirmation if enabled
                if (confirmationFilter != null) {
                    val confirmed =
                        confirmationFilter?.confirm(rawDetection) { detection ->
                            if (detection.isDetected) {
                                Pair(detection.noteName, detection.octave)
                            } else {
                                null
                            }
                        }
                    // Return confirmed detection or undetected result with current audio level
                    confirmed
                        ?: DetectedNote(
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
                } else {
                    // No confirmation - return raw detection
                    rawDetection
                }
            }

    /**
     * Stops listening for audio.
     */
    fun stopListening() {
        audioRecorder.stopRecording()
        confirmationFilter?.reset()
    }

    /**
     * Checks if currently listening.
     */
    fun isListening(): Boolean = audioRecorder.isRecording()
}
