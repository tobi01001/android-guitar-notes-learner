package com.androidguitarnotes.app.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing practice session state and logic.
 */
class PracticeSessionViewModel(private val config: PracticeConfig) : ViewModel() {
    
    private val noteGenerator = RandomNoteGenerator(config)
    
    private val _state = MutableStateFlow<PracticeSessionState>(PracticeSessionState.Ready)
    val state: StateFlow<PracticeSessionState> = _state.asStateFlow()
    
    private var timerJob: Job? = null
    private var startTimeMillis: Long = 0
    private var pausedTimeMillis: Long = 0
    
    /**
     * Starts a new practice session.
     */
    fun startSession() {
        val firstNote = noteGenerator.generateNote()
        startTimeMillis = System.currentTimeMillis()
        pausedTimeMillis = 0
        
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
            totalTimeSeconds = totalTimeSeconds
        )
        
        startTimer()
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
            // Adjust start time to account for paused duration
            val pausedDuration = System.currentTimeMillis() - pausedTimeMillis
            startTimeMillis += pausedDuration
            
            _state.value = PracticeSessionState.Active(
                currentNote = currentState.currentNote,
                notesCompleted = currentState.notesCompleted,
                totalNotes = currentState.totalNotes,
                elapsedTimeSeconds = currentState.elapsedTimeSeconds,
                totalTimeSeconds = currentState.totalTimeSeconds
            )
            
            startTimer()
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
                    val elapsedSeconds = (System.currentTimeMillis() - startTimeMillis) / 1000
                    
                    // Check if time-based session is complete
                    if (config.durationType == DurationType.TIME && 
                        elapsedSeconds >= config.durationMinutes * 60L) {
                        completeSession(currentState.notesCompleted)
                        break
                    }
                    
                    _state.value = currentState.copy(elapsedTimeSeconds = elapsedSeconds)
                }
            }
        }
    }
    
    /**
     * Completes the practice session.
     */
    private fun completeSession(notesCompleted: Int) {
        timerJob?.cancel()
        val totalTime = (System.currentTimeMillis() - startTimeMillis) / 1000
        _state.value = PracticeSessionState.Completed(
            notesCompleted = notesCompleted,
            totalTimeSeconds = totalTime
        )
    }
    
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
