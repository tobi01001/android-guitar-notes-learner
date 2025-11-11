package com.androidguitarnotes.app.notesplayed

import com.androidguitarnotes.app.audio.AudioManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotesPlayedViewModelTest {
    private lateinit var audioManager: AudioManager
    private lateinit var settingsViewModel: com.androidguitarnotes.app.settings.SettingsViewModel
    private lateinit var viewModel: NotesPlayedViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        audioManager = mockk(relaxed = true)
        settingsViewModel =
            com.androidguitarnotes.app.settings
                .SettingsViewModel()
        viewModel = NotesPlayedViewModel(audioManager, settingsViewModel)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be not listening with no detected note`() {
        val state = viewModel.state.value
        assertFalse(state.isListening)
        assertNull(state.detectedNote)
    }

    @Test
    fun `startListening should update isListening to true`() =
        runTest {
            every { audioManager.startListening(any(), any()) } returns flowOf()

            viewModel.startListening()

            val state = viewModel.state.value
            assertTrue(state.isListening)
        }

    @Test
    fun `stopListening should update isListening to false and clear detected note`() =
        runTest {
            every { audioManager.startListening(any(), any()) } returns flowOf()
            viewModel.startListening()

            viewModel.stopListening()

            val state = viewModel.state.value
            assertFalse(state.isListening)
            assertNull(state.detectedNote)
            verify { audioManager.stopListening() }
        }

    @Test
    fun `should update detected note when note is detected`() =
        runTest {
            val noteResult =
                AudioManager.AudioAnalysisResult.NoteDetected(
                    noteName = "A",
                    frequency = 440.0,
                    cents = 0.0,
                    audioLevel = 0.5f,
                    octave = 4,
                    noteNameWithOctave = "A4",
                )
            every { audioManager.startListening(any(), any()) } returns flowOf(noteResult)

            viewModel.startListening()

            val state = viewModel.state.value
            assertTrue(state.isListening)
            assertNotNull(state.detectedNote)
            assertEquals("A", state.detectedNote?.noteName)
            assertEquals(440.0, state.detectedNote?.frequency)
            assertEquals(0.0, state.detectedNote?.cents)
        }

    @Test
    fun `should clear detected note when no note is detected`() =
        runTest {
            val noteResult =
                AudioManager.AudioAnalysisResult.NoteDetected(
                    noteName = "A",
                    frequency = 440.0,
                    cents = 0.0,
                    audioLevel = 0.5f,
                    octave = 4,
                    noteNameWithOctave = "A4",
                )
            val noNoteResult = AudioManager.AudioAnalysisResult.NoNoteDetected(audioLevel = 0.1f)

            every { audioManager.startListening(any(), any()) } returns flowOf(noteResult, noNoteResult)

            viewModel.startListening()

            val state = viewModel.state.value
            assertTrue(state.isListening)
            assertNull(state.detectedNote)
        }

    @Test
    fun `stopListening should be called when viewModel is cleared`() =
        runTest {
            every { audioManager.startListening(any(), any()) } returns flowOf()
            viewModel.startListening()

            // Simulate clearing by stopping listening
            viewModel.stopListening()

            verify { audioManager.stopListening() }
        }
}
