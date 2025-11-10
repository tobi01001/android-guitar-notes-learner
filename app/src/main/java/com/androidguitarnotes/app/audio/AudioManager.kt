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
            val cents: Double
        ) : AudioAnalysisResult()
        
        data object NoNoteDetected : AudioAnalysisResult()
    }
    
    /**
     * Starts listening for audio and analyzing pitch.
     * 
     * @return Flow of AudioAnalysisResult
     */
    fun startListening(): Flow<AudioAnalysisResult> {
        return audioRecorder.startRecording()
            .map { audioData ->
                val frequency = pitchDetector.detectPitch(audioData)
                
                if (frequency != null) {
                    val recognizedNote = noteRecognizer.recognizeNote(frequency)
                    AudioAnalysisResult.NoteDetected(
                        noteName = recognizedNote.noteName,
                        frequency = recognizedNote.frequency,
                        cents = recognizedNote.cents
                    )
                } else {
                    AudioAnalysisResult.NoNoteDetected
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
    fun isListening(): Boolean {
        return audioRecorder.isRecording()
    }
}
