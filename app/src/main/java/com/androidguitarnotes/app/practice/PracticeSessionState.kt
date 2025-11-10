package com.androidguitarnotes.app.practice

/**
 * Represents the current state of a practice session.
 */
sealed class PracticeSessionState {
    /**
     * Session is ready to start.
     */
    data object Ready : PracticeSessionState()
    
    /**
     * Session is actively running.
     */
    data class Active(
        val currentNote: PracticeNote,
        val notesCompleted: Int,
        val totalNotes: Int?,  // null for time-based sessions
        val elapsedTimeSeconds: Long,
        val totalTimeSeconds: Long?  // null for count-based sessions
    ) : PracticeSessionState()
    
    /**
     * Session is paused.
     */
    data class Paused(
        val currentNote: PracticeNote,
        val notesCompleted: Int,
        val totalNotes: Int?,
        val elapsedTimeSeconds: Long,
        val totalTimeSeconds: Long?
    ) : PracticeSessionState()
    
    /**
     * Session has completed.
     */
    data class Completed(
        val notesCompleted: Int,
        val totalTimeSeconds: Long
    ) : PracticeSessionState()
}
