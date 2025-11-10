package com.androidguitarnotes.app.practice

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidguitarnotes.app.audio.AudioManager
import com.androidguitarnotes.app.permissions.PermissionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing practice session state and logic.
 */
class PracticeSessionViewModel(
    private val config: PracticeConfig,
    private val context: Context,
    private val audioManager: AudioManager = AudioManager(),
    private val permissionManager: PermissionManager = PermissionManager(context)
) : ViewModel() {
    
    private val noteGenerator = RandomNoteGenerator(config)
    
    private val _state = MutableStateFlow<PracticeSessionState>(PracticeSessionState.Ready)
    val state: StateFlow<PracticeSessionState> = _state.asStateFlow()
    
    private val _audioPermissionRequired = MutableStateFlow(false)
    val audioPermissionRequired: StateFlow<Boolean> = _audioPermissionRequired.asStateFlow()
    
    private val _showPermissionRationale = MutableStateFlow(false)
    val showPermissionRationale: StateFlow<Boolean> = _showPermissionRationale.asStateFlow()
    
    private var timerJob: Job? = null
    private var audioListeningJob: Job? = null
    private var startTimeMillis: Long = 0
    private var pausedTimeMillis: Long = 0
    private var totalPausedDuration: Long = 0
    
    /**
     * Checks and requests audio permission if needed.
     */
    fun checkAndRequestAudioPermission() {
        if (permissionManager.isRecordAudioPermissionGranted()) {
            // Permission already granted, start audio listening
            if (_state.value is PracticeSessionState.Active) {
                startAudioListening()
            }
        } else {
            // Show rationale first
            _showPermissionRationale.value = true
        }
    }
    
    /**
     * Called when user agrees to grant permission from rationale screen.
     */
    fun onPermissionRationaleDismissed() {
        _showPermissionRationale.value = false
    }
    
    /**
     * Requests audio permission after showing rationale.
     */
    fun requestAudioPermission() {
        _showPermissionRationale.value = false
        _audioPermissionRequired.value = true
    }
    
    /**
     * Called when audio permission is granted.
     */
    fun onAudioPermissionGranted() {
        _audioPermissionRequired.value = false
        // Start audio listening if session is active
        if (_state.value is PracticeSessionState.Active) {
            startAudioListening()
        }
    }
    
    /**
     * Called when audio permission is denied.
     */
    fun onAudioPermissionDenied() {
        _audioPermissionRequired.value = false
        // Continue session without audio feedback
    }
    
    /**
     * Starts a new practice session.
     */
    fun startSession() {
        val firstNote = noteGenerator.generateNote()
        startTimeMillis = System.currentTimeMillis()
        pausedTimeMillis = 0
        totalPausedDuration = 0
        
        val totalNotes = if (config.durationType == DurationType.COUNT) {
            config.noteCount
        } else {
            null
        }
        
        val totalTimeSeconds = if (config.durationType == DurationType.TIME) {
            config.durationMinutes * 60L
        } else {
            null
        }
        
        _state.value = PracticeSessionState.Active(
            currentNote = firstNote,
            notesCompleted = 0,
            totalNotes = totalNotes,
            elapsedTimeSeconds = 0,
            totalTimeSeconds = totalTimeSeconds,
            noteFeedback = PracticeSessionState.NoteFeedback.None
        )
        
        startTimer()
        // Audio listening will be started after permission is granted
    }
    
    /**
     * Starts listening for audio input and analyzing notes.
     * Only starts if permission is granted.
     */
    private fun startAudioListening() {
        // Double-check permission before starting
        if (!permissionManager.isRecordAudioPermissionGranted()) {
            android.util.Log.w("PracticeSessionViewModel", "Cannot start audio - permission not granted")
            return
        }
        
        audioListeningJob?.cancel()
        audioListeningJob = viewModelScope.launch {
            try {
                audioManager.startListening().collect { result ->
                    val currentState = _state.value
                    if (currentState is PracticeSessionState.Active) {
                        when (result) {
                            is AudioManager.AudioAnalysisResult.NoteDetected -> {
                                val expectedNote = currentState.currentNote.noteName
                                val isCorrect = result.noteName == expectedNote
                                
                                val feedback = if (isCorrect) {
                                    PracticeSessionState.NoteFeedback.Correct
                                } else {
                                    PracticeSessionState.NoteFeedback.Detected(
                                        result.noteName,
                                        result.cents
                                    )
                                }
                                
                                _state.value = currentState.copy(noteFeedback = feedback)
                            }
                            is AudioManager.AudioAnalysisResult.NoNoteDetected -> {
                                _state.value = currentState.copy(
                                    noteFeedback = PracticeSessionState.NoteFeedback.None
                                )
                            }
                        }
                    }
                }
            } catch (e: SecurityException) {
                // Permission was revoked during recording
                android.util.Log.e("PracticeSessionViewModel", "Permission revoked during recording", e)
                stopAudioListening()
            } catch (e: Exception) {
                // Log audio errors for debugging
                android.util.Log.e("PracticeSessionViewModel", "Audio listening error", e)
                // Continue without audio feedback
            }
        }
    }
    
    /**
     * Stops listening for audio input.
     */
    private fun stopAudioListening() {
        audioListeningJob?.cancel()
        audioManager.stopListening()
    }
    
    /**
     * Moves to the next note in the practice session.
     */
    fun nextNote() {
        val currentState = _state.value
        if (currentState is PracticeSessionState.Active) {
            val newNotesCompleted = currentState.notesCompleted + 1
            
            // Check if session should complete (count-based)
            if (config.durationType == DurationType.COUNT && 
                newNotesCompleted >= config.noteCount) {
                completeSession(newNotesCompleted)
                return
            }
            
            val nextNote = noteGenerator.generateNote()
            _state.value = currentState.copy(
                currentNote = nextNote,
                notesCompleted = newNotesCompleted
            )
        }
    }
    
    /**
     * Pauses the practice session.
     */
    fun pauseSession() {
        val currentState = _state.value
        if (currentState is PracticeSessionState.Active) {
            pausedTimeMillis = System.currentTimeMillis()
            timerJob?.cancel()
            stopAudioListening()
            
            _state.value = PracticeSessionState.Paused(
                currentNote = currentState.currentNote,
                notesCompleted = currentState.notesCompleted,
                totalNotes = currentState.totalNotes,
                elapsedTimeSeconds = currentState.elapsedTimeSeconds,
                totalTimeSeconds = currentState.totalTimeSeconds
            )
        }
    }
    
    /**
     * Resumes a paused practice session.
     */
    fun resumeSession() {
        val currentState = _state.value
        if (currentState is PracticeSessionState.Paused) {
            // Accumulate paused duration for multiple pause/resume cycles
            val pausedDuration = System.currentTimeMillis() - pausedTimeMillis
            totalPausedDuration += pausedDuration
            
            _state.value = PracticeSessionState.Active(
                currentNote = currentState.currentNote,
                notesCompleted = currentState.notesCompleted,
                totalNotes = currentState.totalNotes,
                elapsedTimeSeconds = currentState.elapsedTimeSeconds,
                totalTimeSeconds = currentState.totalTimeSeconds,
                noteFeedback = PracticeSessionState.NoteFeedback.None
            )
            
            startTimer()
            // Only start audio if permission is granted
            if (permissionManager.isRecordAudioPermissionGranted()) {
                startAudioListening()
            }
        }
    }
    
    /**
     * Ends the practice session early.
     */
    fun endSession() {
        val currentState = _state.value
        when (currentState) {
            is PracticeSessionState.Active -> {
                completeSession(currentState.notesCompleted)
            }
            is PracticeSessionState.Paused -> {
                completeSession(currentState.notesCompleted)
            }
            else -> { /* Do nothing */ }
        }
    }
    
    /**
     * Starts the timer for time-based sessions or elapsed time tracking.
     */
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000) // Update every second
                
                val currentState = _state.value
                if (currentState is PracticeSessionState.Active) {
                    val elapsedSeconds = (System.currentTimeMillis() - startTimeMillis - totalPausedDuration) / 1000
                    
                    // Check if time-based session is complete
                    if (config.durationType == DurationType.TIME && 
                        elapsedSeconds >= config.durationMinutes * 60L) {
                        completeSession(currentState.notesCompleted)
                        break
                    }
                    
                    _state.value = currentState.copy(elapsedTimeSeconds = elapsedSeconds)
                } else {
                    // Exit loop if state is no longer Active
                    break
                }
            }
        }
    }
    
    /**
     * Completes the practice session.
     */
    private fun completeSession(notesCompleted: Int) {
        timerJob?.cancel()
        stopAudioListening()
        val totalTime = (System.currentTimeMillis() - startTimeMillis - totalPausedDuration) / 1000
        _state.value = PracticeSessionState.Completed(
            notesCompleted = notesCompleted,
            totalTimeSeconds = totalTime
        )
    }
    
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        stopAudioListening()
    }
}
