package com.androidguitarnotes.app.audio

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Fast Fourier Transform (FFT) implementation using Cooley-Tukey radix-2 algorithm.
 *
 * This optimized FFT implementation reduces computational complexity from O(n²) to O(n log n),
 * which is critical for real-time audio processing. For n=4096:
 * - DFT: ~16 million operations
 * - FFT: ~49,152 operations (327x faster)
 *
 * ## Implementation Details
 * - Uses in-place Cooley-Tukey radix-2 decimation-in-time algorithm
 * - Requires input size to be a power of 2
 * - Processes complex numbers in interleaved format [real0, imag0, real1, imag1, ...]
 * - Bit-reversal permutation for correct ordering
 *
 * ## Usage
 * ```kotlin
 * val fft = FFT(4096)
 * val complexData = FloatArray(4096 * 2) // real and imaginary parts
 * // Fill real parts, imaginary parts start as 0
 * fft.transform(complexData)
 * val magnitudes = fft.getMagnitudes(complexData)
 * ```
 *
 * @param n FFT size (must be a power of 2)
 * @throws IllegalArgumentException if n is not a power of 2
 */
class FFT(
    private val n: Int,
) {
    init {
        require(
            n > 0 && (n and (n - 1)) == 0,
        ) {
            "FFT size must be a power of 2, got $n"
        }
    }

    // Pre-computed twiddle factors for efficiency
    private val cosTable = FloatArray(n / 2)
    private val sinTable = FloatArray(n / 2)

    init {
        // Pre-compute twiddle factors: e^(-2πi*k/n) for k = 0 to n/2-1
        for (k in 0 until n / 2) {
            val angle = -2.0 * PI * k / n
            cosTable[k] = cos(angle).toFloat()
            sinTable[k] = sin(angle).toFloat()
        }
    }

    /**
     * Performs in-place FFT on complex data.
     *
     * The input array must contain interleaved real and imaginary parts:
     * [real0, imag0, real1, imag1, real2, imag2, ...]
     *
     * After transformation, the array contains frequency-domain complex numbers
     * in the same interleaved format.
     *
     * @param data Complex data array of size 2*n (interleaved real/imaginary)
     * @throws IllegalArgumentException if data size is not 2*n
     */
    fun transform(data: FloatArray) {
        require(data.size == 2 * n) {
            "Data array size must be ${2 * n}, got ${data.size}"
        }

        // Step 1: Bit-reversal permutation
        var j = 0
        for (i in 0 until n - 1) {
            if (i < j) {
                // Swap complex numbers at positions i and j
                var temp = data[2 * i]
                data[2 * i] = data[2 * j]
                data[2 * j] = temp

                temp = data[2 * i + 1]
                data[2 * i + 1] = data[2 * j + 1]
                data[2 * j + 1] = temp
            }

            // Bit-reversal counter
            var k = n / 2
            while (k <= j) {
                j -= k
                k /= 2
            }
            j += k
        }

        // Step 2: Cooley-Tukey decimation-in-time FFT
        var size = 2
        while (size <= n) {
            val halfSize = size / 2
            val tableStep = n / size

            for (i in 0 until n step size) {
                var k = 0
                for (m in i until i + halfSize) {
                    val l = m + halfSize

                    // Get twiddle factor from pre-computed table
                    val twiddleReal = cosTable[k * tableStep]
                    val twiddleImag = sinTable[k * tableStep]

                    // Complex multiplication: (data[l] * twiddle)
                    val tempReal = data[2 * l] * twiddleReal - data[2 * l + 1] * twiddleImag
                    val tempImag = data[2 * l] * twiddleImag + data[2 * l + 1] * twiddleReal

                    // Butterfly operation
                    data[2 * l] = data[2 * m] - tempReal
                    data[2 * l + 1] = data[2 * m + 1] - tempImag
                    data[2 * m] = data[2 * m] + tempReal
                    data[2 * m + 1] = data[2 * m + 1] + tempImag

                    k++
                }
            }
            size *= 2
        }
    }

    /**
     * Computes magnitude spectrum from complex FFT result.
     *
     * @param data Complex FFT result in interleaved format
     * @return Magnitude array of size n/2 (only positive frequencies up to Nyquist)
     */
    fun getMagnitudes(data: FloatArray): FloatArray {
        require(data.size == 2 * n) {
            "Data array size must be ${2 * n}, got ${data.size}"
        }

        val magnitudes = FloatArray(n / 2)
        for (i in magnitudes.indices) {
            val real = data[2 * i]
            val imag = data[2 * i + 1]
            magnitudes[i] = sqrt(real * real + imag * imag)
        }
        return magnitudes
    }

    /**
     * Convenience method to compute magnitude spectrum from real-valued input.
     *
     * @param realData Real-valued time-domain samples
     * @return Magnitude spectrum of size n/2
     * @throws IllegalArgumentException if realData size doesn't match FFT size
     */
    fun computeMagnitudeSpectrum(realData: FloatArray): FloatArray {
        require(realData.size == n) {
            "Real data array size must be $n, got ${realData.size}"
        }

        // Create complex data array (interleaved real/imaginary)
        val complexData = FloatArray(2 * n)
        for (i in realData.indices) {
            complexData[2 * i] = realData[i]
            complexData[2 * i + 1] = 0f // Imaginary part is 0
        }

        // Perform FFT
        transform(complexData)

        // Extract magnitudes
        return getMagnitudes(complexData)
    }
}
