package com.androidguitarnotes.app.tuner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidguitarnotes.app.audio.AudioManager
import com.androidguitarnotes.app.settings.SettingsViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.log2

/**
 * ViewModel for the guitar tuner screen.
 */
class TunerViewModel(
    private val audioManager: AudioManager,
    private val settingsViewModel: SettingsViewModel,
) : ViewModel() {
    private val _state = MutableStateFlow(TunerState())
    val state: StateFlow<TunerState> = _state.asStateFlow()

    private var listeningJob: Job? = null

    /**
     * Selects a guitar string to tune.
     */
    fun selectString(guitarString: GuitarString) {
        _state.value = _state.value.copy(selectedString = guitarString)
    }

    /**
     * Starts listening for audio input.
     */
    fun startListening() {
        if (_state.value.isListening) return

        _state.value = _state.value.copy(isListening = true)

        listeningJob?.cancel()
        listeningJob =
            viewModelScope.launch {
                val audioSource = settingsViewModel.audioSource.value
                val audioSourceValue = if (audioSource.value == -1) null else audioSource.value
                val sensitivity = settingsViewModel.microphoneSensitivity.value
                val autoAdjust = settingsViewModel.autoAdjustSensitivity.value
                val noiseGateThreshold = settingsViewModel.noiseGateThreshold.value

                audioManager.startListening(
                    sensitivityMultiplier = sensitivity,
                    audioSource = audioSourceValue,
                    autoAdjustEnabled = autoAdjust,
                    noiseGateThreshold = noiseGateThreshold,
                ).collect { result ->
                    when (result) {
                        is AudioManager.AudioAnalysisResult.NoteDetected -> {
                            val selectedString = _state.value.selectedString
                            val cents =
                                calculateCentsDeviation(
                                    result.frequency,
                                    selectedString.frequency,
                                )

                            _state.value =
                                _state.value.copy(
                                    tuningStatus =
                                        TuningStatus.Detecting(
                                            detectedFrequency = result.frequency,
                                            cents = cents,
                                        ),
                                )
                        }
                        is AudioManager.AudioAnalysisResult.NoNoteDetected -> {
                            _state.value =
                                _state.value.copy(
                                    tuningStatus = TuningStatus.NotDetected,
                                )
                        }
                        is AudioManager.AudioAnalysisResult.Gated -> {
                            _state.value =
                                _state.value.copy(
                                    tuningStatus = TuningStatus.NotDetected,
                                )
                        }
                    }
                }
            }
    }

    /**
     * Stops listening for audio input.
     */
    fun stopListening() {
        listeningJob?.cancel()
        listeningJob = null
        audioManager.stopListening()
        _state.value =
            _state.value.copy(
                isListening = false,
                tuningStatus = TuningStatus.NotDetected,
            )
    }

    /**
     * Calculates the cents deviation between detected and target frequency.
     */
    private fun calculateCentsDeviation(
        detectedFreq: Double,
        targetFreq: Double,
    ): Double = 1200.0 * log2(detectedFreq / targetFreq)

    /**
     * Checks if the current tuning status is in tune.
     */
    fun isInTune(): Boolean {
        val status = _state.value.tuningStatus
        return status is TuningStatus.Detecting && abs(status.cents) <= TunerConstants.IN_TUNE_THRESHOLD_CENTS
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}
