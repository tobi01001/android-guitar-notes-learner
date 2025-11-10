package com.androidguitarnotes.app.settings

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for SettingsViewModel.
 */
class SettingsViewModelTest {
    @Test
    fun `default audio feedback is enabled`() = runTest {
        val viewModel = SettingsViewModel()

        val audioFeedbackEnabled = viewModel.audioFeedbackEnabled.first()

        assertTrue("Audio feedback should be enabled by default", audioFeedbackEnabled)
    }

    @Test
    fun `default tuning is Standard`() = runTest {
        val viewModel = SettingsViewModel()

        val defaultTuning = viewModel.defaultTuning.first()

        assertEquals("Default tuning should be Standard", "Standard", defaultTuning)
    }

    @Test
    fun `toggleAudioFeedback updates state`() = runTest {
        val viewModel = SettingsViewModel()

        viewModel.toggleAudioFeedback(false)
        val audioFeedbackEnabled = viewModel.audioFeedbackEnabled.first()

        assertFalse("Audio feedback should be disabled", audioFeedbackEnabled)
    }

    @Test
    fun `toggleAudioFeedback can be toggled multiple times`() = runTest {
        val viewModel = SettingsViewModel()

        viewModel.toggleAudioFeedback(false)
        viewModel.toggleAudioFeedback(true)
        val audioFeedbackEnabled = viewModel.audioFeedbackEnabled.first()

        assertTrue("Audio feedback should be enabled", audioFeedbackEnabled)
    }

    @Test
    fun `setDefaultTuning updates state`() = runTest {
        val viewModel = SettingsViewModel()

        viewModel.setDefaultTuning("Drop D")
        val defaultTuning = viewModel.defaultTuning.first()

        assertEquals("Default tuning should be Drop D", "Drop D", defaultTuning)
    }

    @Test
    fun `setDefaultTuning can be changed multiple times`() = runTest {
        val viewModel = SettingsViewModel()

        viewModel.setDefaultTuning("Drop D")
        viewModel.setDefaultTuning("Open G")
        val defaultTuning = viewModel.defaultTuning.first()

        assertEquals("Default tuning should be Open G", "Open G", defaultTuning)
    }
}
