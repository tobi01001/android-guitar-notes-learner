package com.androidguitarnotes.app.audio

/**
 * Multi-frame pitch confirmation filter for enhanced detection stability.
 *
 * Requires multiple consecutive audio frames to detect the same pitch before confirming
 * the detection. This reduces false positives from transient noise or brief harmonics.
 *
 * ## Implementation Details (ENH-001)
 *
 * The filter maintains a buffer of recent detections and only confirms a note when:
 * - All N consecutive frames detect the same note name and octave
 * - Silence requires N consecutive "no detection" frames to confirm
 *
 * Benefits:
 * - Reduced false positives from transient noise
 * - Smoother visual feedback in UI
 * - Fewer incorrect penalties in practice mode
 *
 * Trade-offs:
 * - Adds N × buffer_duration latency (typically 100-200ms for 2-3 frames)
 * - May miss very brief notes
 * - Less responsive for fast playing
 *
 * @param requiredConsecutiveFrames Number of consecutive matching frames required (default: 2)
 */
class PitchConfirmationFilter(
    private val requiredConsecutiveFrames: Int = 2,
) {
    private val recentDetections = mutableListOf<DetectionKey?>()

    /**
     * Key for comparing detections (note name + octave only, ignoring frequency variations).
     */
    private data class DetectionKey(
        val noteName: String,
        val octave: Int,
    )

    /**
     * Confirms a detection after N consecutive matching frames.
     *
     * @param detection The detection result to confirm (can be null for no detection)
     * @return The original detection if confirmed, null otherwise
     */
    fun <T> confirm(
        detection: T?,
        noteExtractor: (T) -> Pair<String, Int>?,
    ): T? {
        // Extract key from detection (note name + octave)
        val key =
            detection?.let { det ->
                noteExtractor(det)?.let { (noteName, octave) ->
                    DetectionKey(noteName, octave)
                }
            }

        // Add to buffer
        recentDetections.add(key)

        // Keep only the most recent N detections
        if (recentDetections.size > requiredConsecutiveFrames) {
            recentDetections.removeAt(0)
        }

        // Check if all frames match
        if (recentDetections.size == requiredConsecutiveFrames &&
            recentDetections.all { it == key }
        ) {
            return detection // Confirmed!
        }

        return null // Not yet confirmed
    }

    /**
     * Resets the confirmation buffer.
     * Should be called when recording starts/stops or settings change.
     */
    fun reset() {
        recentDetections.clear()
    }

    /**
     * Returns the current buffer size.
     * Useful for testing and diagnostics.
     */
    fun getBufferSize(): Int = recentDetections.size
}
