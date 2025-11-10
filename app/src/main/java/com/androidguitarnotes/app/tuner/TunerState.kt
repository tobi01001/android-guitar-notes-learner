package com.androidguitarnotes.app.tuner

/**
 * Represents the tuning status for a guitar string.
 */
sealed class TuningStatus {
    data object NotDetected : TuningStatus()
    data class Detecting(
        val detectedFrequency: Double,
        val cents: Double
    ) : TuningStatus()
}

/**
 * State for the tuner screen.
 */
data class TunerState(
    val selectedString: GuitarString = GuitarString.STANDARD_TUNING[0],
    val tuningStatus: TuningStatus = TuningStatus.NotDetected,
    val isListening: Boolean = false
)
