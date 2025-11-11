package com.androidguitarnotes.app.practice

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for PracticeConfigViewModel.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PracticeConfigViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createMockRepository(): PracticeSettingsRepository {
        val repository = mockk<PracticeSettingsRepository>(relaxed = true)
        coEvery { repository.practiceConfig } returns flowOf(PracticeConfig())
        return repository
    }

    @Test
    fun `initial config is valid`() {
        val viewModel = PracticeConfigViewModel(createMockRepository())
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue("Initial config should be valid", viewModel.isConfigValid())
    }

    @Test
    fun `setProgressionMode updates config`() {
        val viewModel = PracticeConfigViewModel(createMockRepository())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setProgressionMode(ProgressionMode.AUDIO_VERIFICATION)
        val config = viewModel.config.value

        assertEquals(ProgressionMode.AUDIO_VERIFICATION, config.progressionMode)
    }

    @Test
    fun `setAutoIntervalSeconds updates config`() {
        val viewModel = PracticeConfigViewModel(createMockRepository())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setAutoIntervalSeconds(5.5f)
        val config = viewModel.config.value

        assertEquals(5.5f, config.autoIntervalSeconds, 0.001f)
    }

    @Test
    fun `config is invalid with auto interval less than 0_5 seconds`() {
        val viewModel = PracticeConfigViewModel(createMockRepository())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setProgressionMode(ProgressionMode.AUTO_INTERVAL)
        viewModel.setAutoIntervalSeconds(0.3f)

        assertFalse(
            "Config should be invalid with auto interval < 0.5s",
            viewModel.isConfigValid(),
        )
    }

    @Test
    fun `config is invalid with auto interval greater than 10 seconds`() {
        val viewModel = PracticeConfigViewModel(createMockRepository())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setProgressionMode(ProgressionMode.AUTO_INTERVAL)
        viewModel.setAutoIntervalSeconds(11.0f)

        assertFalse(
            "Config should be invalid with auto interval > 10s",
            viewModel.isConfigValid(),
        )
    }

    @Test
    fun `config is valid with auto interval at minimum boundary`() {
        val viewModel = PracticeConfigViewModel(createMockRepository())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setProgressionMode(ProgressionMode.AUTO_INTERVAL)
        viewModel.setAutoIntervalSeconds(0.5f)

        assertTrue(
            "Config should be valid with auto interval = 0.5s",
            viewModel.isConfigValid(),
        )
    }

    @Test
    fun `config is valid with auto interval at maximum boundary`() {
        val viewModel = PracticeConfigViewModel(createMockRepository())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setProgressionMode(ProgressionMode.AUTO_INTERVAL)
        viewModel.setAutoIntervalSeconds(10.0f)

        assertTrue(
            "Config should be valid with auto interval = 10.0s",
            viewModel.isConfigValid(),
        )
    }

    @Test
    fun `config is valid with manual mode regardless of auto interval`() {
        val viewModel = PracticeConfigViewModel(createMockRepository())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setProgressionMode(ProgressionMode.MANUAL)
        viewModel.setAutoIntervalSeconds(15.0f) // Invalid interval

        assertTrue(
            "Config should be valid in MANUAL mode regardless of interval",
            viewModel.isConfigValid(),
        )
    }

    @Test
    fun `config is valid with audio verification mode regardless of auto interval`() {
        val viewModel = PracticeConfigViewModel(createMockRepository())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setProgressionMode(ProgressionMode.AUDIO_VERIFICATION)
        viewModel.setAutoIntervalSeconds(0.1f) // Invalid interval

        assertTrue(
            "Config should be valid in AUDIO_VERIFICATION mode regardless of interval",
            viewModel.isConfigValid(),
        )
    }
}
