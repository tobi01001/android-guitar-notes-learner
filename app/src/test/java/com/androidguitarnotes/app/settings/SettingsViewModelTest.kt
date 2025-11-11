package com.androidguitarnotes.app.settings

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SettingsViewModel.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createMockRepository(): SettingsRepository {
        val repository = mockk<SettingsRepository>(relaxed = true)
        coEvery { repository.audioSource } returns flowOf(AudioSource.AUTO)
        return repository
    }

    @Test
    fun `default audio feedback is enabled`() =
        runTest {
            val viewModel = SettingsViewModel(createMockRepository())
            testDispatcher.scheduler.advanceUntilIdle()

            val audioFeedbackEnabled = viewModel.audioFeedbackEnabled.first()

            assertTrue("Audio feedback should be enabled by default", audioFeedbackEnabled)
        }

    @Test
    fun `default tuning is Standard`() =
        runTest {
            val viewModel = SettingsViewModel(createMockRepository())
            testDispatcher.scheduler.advanceUntilIdle()

            val defaultTuning = viewModel.defaultTuning.first()

            assertEquals("Default tuning should be Standard", "Standard", defaultTuning)
        }

    @Test
    fun `toggleAudioFeedback updates state`() =
        runTest {
            val viewModel = SettingsViewModel(createMockRepository())
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.toggleAudioFeedback(false)
            val audioFeedbackEnabled = viewModel.audioFeedbackEnabled.first()

            assertFalse("Audio feedback should be disabled", audioFeedbackEnabled)
        }

    @Test
    fun `toggleAudioFeedback can be toggled multiple times`() =
        runTest {
            val viewModel = SettingsViewModel(createMockRepository())
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.toggleAudioFeedback(false)
            viewModel.toggleAudioFeedback(true)
            val audioFeedbackEnabled = viewModel.audioFeedbackEnabled.first()

            assertTrue("Audio feedback should be enabled", audioFeedbackEnabled)
        }

    @Test
    fun `setDefaultTuning updates state`() =
        runTest {
            val viewModel = SettingsViewModel(createMockRepository())
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.setDefaultTuning("Drop D")
            val defaultTuning = viewModel.defaultTuning.first()

            assertEquals("Default tuning should be Drop D", "Drop D", defaultTuning)
        }

    @Test
    fun `setDefaultTuning can be changed multiple times`() =
        runTest {
            val viewModel = SettingsViewModel(createMockRepository())
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.setDefaultTuning("Drop D")
            viewModel.setDefaultTuning("Open G")
            val defaultTuning = viewModel.defaultTuning.first()

            assertEquals("Default tuning should be Open G", "Open G", defaultTuning)
        }

    @Test
    fun `default microphone sensitivity is 1_0`() =
        runTest {
            val viewModel = SettingsViewModel(createMockRepository())
            testDispatcher.scheduler.advanceUntilIdle()

            val sensitivity = viewModel.microphoneSensitivity.first()

            assertEquals("Microphone sensitivity should be 1.0 by default", 1.0f, sensitivity, 0.001f)
        }

    @Test
    fun `setMicrophoneSensitivity updates state`() =
        runTest {
            val viewModel = SettingsViewModel(createMockRepository())
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.setMicrophoneSensitivity(1.5f)
            val sensitivity = viewModel.microphoneSensitivity.first()

            assertEquals("Microphone sensitivity should be 1.5", 1.5f, sensitivity, 0.001f)
        }

    @Test
    fun `setMicrophoneSensitivity clamps values below 0_5`() =
        runTest {
            val viewModel = SettingsViewModel(createMockRepository())
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.setMicrophoneSensitivity(0.2f)
            val sensitivity = viewModel.microphoneSensitivity.first()

            assertEquals("Microphone sensitivity should be clamped to 0.5", 0.5f, sensitivity, 0.001f)
        }

    @Test
    fun `setMicrophoneSensitivity clamps values above 2_0`() =
        runTest {
            val viewModel = SettingsViewModel(createMockRepository())
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.setMicrophoneSensitivity(3.0f)
            val sensitivity = viewModel.microphoneSensitivity.first()

            assertEquals("Microphone sensitivity should be clamped to 2.0", 2.0f, sensitivity, 0.001f)
        }

    @Test
    fun `default auto-adjust sensitivity is false`() =
        runTest {
            val viewModel = SettingsViewModel(createMockRepository())
            testDispatcher.scheduler.advanceUntilIdle()

            val autoAdjust = viewModel.autoAdjustSensitivity.first()

            assertFalse("Auto-adjust sensitivity should be disabled by default", autoAdjust)
        }

    @Test
    fun `toggleAutoAdjustSensitivity updates state`() =
        runTest {
            val viewModel = SettingsViewModel(createMockRepository())
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.toggleAutoAdjustSensitivity(true)
            val autoAdjust = viewModel.autoAdjustSensitivity.first()

            assertTrue("Auto-adjust sensitivity should be enabled", autoAdjust)
        }

    @Test
    fun `toggleAutoAdjustSensitivity can be toggled multiple times`() =
        runTest {
            val viewModel = SettingsViewModel(createMockRepository())
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.toggleAutoAdjustSensitivity(true)
            viewModel.toggleAutoAdjustSensitivity(false)
            val autoAdjust = viewModel.autoAdjustSensitivity.first()

            assertFalse("Auto-adjust sensitivity should be disabled", autoAdjust)
        }

    @Test
    fun `default audio source is AUTO`() =
        runTest {
            val viewModel = SettingsViewModel(createMockRepository())
            testDispatcher.scheduler.advanceUntilIdle()

            val audioSource = viewModel.audioSource.first()

            assertEquals("Audio source should be AUTO by default", AudioSource.AUTO, audioSource)
        }

    @Test
    fun `setAudioSource updates state`() =
        runTest {
            val viewModel = SettingsViewModel(createMockRepository())
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.setAudioSource(AudioSource.UNPROCESSED)
            val audioSource = viewModel.audioSource.first()

            assertEquals("Audio source should be UNPROCESSED", AudioSource.UNPROCESSED, audioSource)
        }

    @Test
    fun `setAudioSource can be changed multiple times`() =
        runTest {
            val viewModel = SettingsViewModel(createMockRepository())
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.setAudioSource(AudioSource.UNPROCESSED)
            viewModel.setAudioSource(AudioSource.MIC)
            val audioSource = viewModel.audioSource.first()

            assertEquals("Audio source should be MIC", AudioSource.MIC, audioSource)
        }
}
