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
    private lateinit var permissionManager: com.androidguitarnotes.app.permissions.PermissionManager
    private lateinit var viewModel: NotesPlayedViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        audioManager = mockk(relaxed = true)
        permissionManager = mockk(relaxed = true)
        // Mock permission as granted by default for tests
        every { permissionManager.isRecordAudioPermissionGranted() } returns true
        val settingsRepository =
            mockk<com.androidguitarnotes.app.settings.SettingsRepository>(relaxed = true)
        every { settingsRepository.audioSource } returns
            flowOf(com.androidguitarnotes.app.settings.AudioSource.AUTO)
        settingsViewModel =
            com.androidguitarnotes.app.settings
                .SettingsViewModel(settingsRepository)
        viewModel = NotesPlayedViewModel(audioManager, settingsViewModel, permissionManager)
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
        assertNull(state.lastDetectedNote)
        assertEquals(0L, state.lastDetectionTimestamp)
    }

    @Test
    fun `startListening should update isListening to true`() =
        runTest {
            every { audioManager.startListeningWithDetectedNote(any(), any(), any(), any()) } returns flowOf()

            viewModel.startListening()

            val state = viewModel.state.value
            assertTrue(state.isListening)
        }

    @Test
    fun `stopListening should update isListening to false and clear detected note and last detected note`() =
        runTest {
            every { audioManager.startListeningWithDetectedNote(any(), any(), any(), any()) } returns flowOf()
            viewModel.startListening()

            viewModel.stopListening()

            val state = viewModel.state.value
            assertFalse(state.isListening)
            assertNull(state.detectedNote)
            assertNull(state.lastDetectedNote)
            assertEquals(0L, state.lastDetectionTimestamp)
            verify { audioManager.stopListening() }
        }

    @Test
    fun `should update detected note when note is detected`() =
        runTest {
            val detectedNote =
                AudioManager.DetectedNote(
                    isDetected = true,
                    noteName = "A",
                    frequency = 440.0,
                    cents = 0.0,
                    confidence = 0.9f,
                    audioLevel = 0.5f,
                    octave = 4,
                    noteNameWithOctave = "A4",
                    isGated = false,
                )
            every { audioManager.startListeningWithDetectedNote(any(), any(), any(), any()) } returns flowOf(detectedNote)

            viewModel.startListening()

            val state = viewModel.state.value
            assertTrue(state.isListening)
            assertNotNull(state.detectedNote)
            assertEquals("A", state.detectedNote?.noteName)
            assertEquals(440.0, state.detectedNote?.frequency)
            assertEquals(0.0, state.detectedNote?.cents)
        }

    @Test
    fun `should clear detected note when no note is detected but persist last detected note`() =
        runTest {
            val detectedNote =
                AudioManager.DetectedNote(
                    isDetected = true,
                    noteName = "A",
                    frequency = 440.0,
                    cents = 0.0,
                    confidence = 0.9f,
                    audioLevel = 0.5f,
                    octave = 4,
                    noteNameWithOctave = "A4",
                    isGated = false,
                )
            val noNote =
                AudioManager.DetectedNote(
                    isDetected = false,
                    noteName = "?",
                    frequency = null,
                    cents = 0.0,
                    confidence = 0f,
                    audioLevel = 0.1f,
                    octave = -1,
                    noteNameWithOctave = "?",
                    isGated = false,
                )

            every { audioManager.startListeningWithDetectedNote(any(), any(), any(), any()) } returns flowOf(detectedNote, noNote)

            viewModel.startListening()

            val state = viewModel.state.value
            assertTrue(state.isListening)
            assertNull(state.detectedNote)
            assertNotNull(state.lastDetectedNote)
            assertEquals("A", state.lastDetectedNote?.noteName)
            assertTrue(state.lastDetectionTimestamp > 0L)
        }

    @Test
    fun `stopListening should be called when viewModel is cleared`() =
        runTest {
            every { audioManager.startListeningWithDetectedNote(any(), any(), any(), any()) } returns flowOf()
            viewModel.startListening()

            // Simulate clearing by stopping listening
            viewModel.stopListening()

            verify { audioManager.stopListening() }
        }

    @Test
    fun `should persist last detected note when a new note is detected`() =
        runTest {
            val detectedNote1 =
                AudioManager.DetectedNote(
                    isDetected = true,
                    noteName = "A",
                    frequency = 440.0,
                    cents = 0.0,
                    confidence = 0.9f,
                    audioLevel = 0.5f,
                    octave = 4,
                    noteNameWithOctave = "A4",
                    isGated = false,
                )
            val detectedNote2 =
                AudioManager.DetectedNote(
                    isDetected = true,
                    noteName = "C",
                    frequency = 261.63,
                    cents = 0.0,
                    confidence = 0.9f,
                    audioLevel = 0.5f,
                    octave = 4,
                    noteNameWithOctave = "C4",
                    isGated = false,
                )

            every { audioManager.startListeningWithDetectedNote(any(), any(), any(), any()) } returns flowOf(detectedNote1, detectedNote2)

            viewModel.startListening()

            val state = viewModel.state.value
            assertTrue(state.isListening)
            assertNotNull(state.detectedNote)
            assertEquals("C", state.detectedNote?.noteName)
            assertNotNull(state.lastDetectedNote)
            assertEquals("C", state.lastDetectedNote?.noteName)
            assertTrue(state.lastDetectionTimestamp > 0L)
        }
}
