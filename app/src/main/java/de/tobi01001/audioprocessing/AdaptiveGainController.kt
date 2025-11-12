package de.tobi01001.audioprocessing

/**
 * AdaptiveGainController - simple adaptive gain (auto-adjust sensitivity) controller.
 * - targetRms: desired RMS level
 * - minGain / maxGain: bounds for returned gain
 * - smoothing: 0.0..1.0 (closer to 1.0 = more smoothing)
 */
class AdaptiveGainController(
    private val targetRms: Float = 0.1f,
    private val minGain: Float = 0.5f,
    private val maxGain: Float = 2.0f,
    private val smoothing: Float = 0.9f
) {
    private var currentGain: Float = 1.0f

    /**
     * Update and return the new gain based on measured RMS.
     * Uses a proportional factor and an exponential smoothing to avoid abrupt jumps.
     */
    fun updateGain(actualRms: Float): Float {
        val safeRms = if (actualRms <= 1e-6f) 1e-6f else actualRms
        val desired = targetRms / safeRms
        // limit single-step change to avoid instability
        val stepLimit = 1.1f
        val unclamped = desired.coerceIn(1f / stepLimit, stepLimit) * currentGain
        // clamp to absolute bounds
        val bounded = unclamped.coerceIn(minGain, maxGain)
        // exponential smoothing (new = alpha*old + (1-alpha)*bounded)
        currentGain = smoothing * currentGain + (1.0f - smoothing) * bounded
        return currentGain
    }

    fun reset() { currentGain = 1.0f }
    fun getGain(): Float = currentGain
}