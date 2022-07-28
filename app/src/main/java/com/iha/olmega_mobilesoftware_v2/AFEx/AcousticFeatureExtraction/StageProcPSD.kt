package com.iha.olmega_mobilesoftware_v2.AFEx.AcousticFeatureExtraction

import android.util.Log
import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D
import java.util.*

/**
 * Feature extraction: Auto- and cross correlation
 *
 * Data is smoothed so no information can be recovered when recreating a time signal from these
 * spectra. It contains a few magic numbers and makes certain assumptions about the input buffer
 * (25 ms, 50 % overlap) and sends spectra representing chunks of 125 ms.
 *
 * Use the following feature definition:
 * <stage feature="StageProcPSD" id="7" blocksize="400" hopsize="200" blockout="2000" hopout="2000"></stage>
 *
 */
class StageProcPSD(parameter: HashMap<*, *>?) : Stage(parameter!!) {
    var cpsd: CPSD?
    override fun cleanup() {
        Log.d(LOG, "Stopped " + LOG)
        super.cleanup()
    }

    override fun process(buffer: Array<FloatArray>?) {
        cpsd!!.calculate(buffer)
    }

    inner class CPSD internal constructor() {
        var alpha: Float
        var window: FloatArray?
        var Xx: FloatArray? = null
        var Xy: FloatArray? = null
        var data: Array<FloatArray?>? = null
        var dataConj: Array<FloatArray?>? = null
        var P: Array<FloatArray?>? = null
        var Ptemp: Array<FloatArray?>? = null
        var nfft: Int
        var samples: Int
        var block = 0
        var fft: FloatFFT_1D?

        fun calculate(input: Array<FloatArray>?) {
            data = Array(Stage.Companion.channels) { FloatArray(nfft * 2) }
            dataConj = arrayOfNulls<FloatArray?>(Stage.Companion.channels)
            Ptemp = Array(3) { FloatArray(nfft * 2) }
            P = Ptemp
            for (iChannel in data!!.indices) {

                // window
                for (i in 0 until samples) {
                    data!![iChannel]!![i] = input!![iChannel][i] * window!![i]
                }

                // FFT
                fft!!.realForwardFull(data!![iChannel])

                // complex conjugate
                dataConj!![iChannel] = Arrays.copyOf(data!![iChannel]!!, data!![iChannel]!!.size)
                var iSample = 3
                while (iSample < data!![iChannel]!!.size) {
                    dataConj!![iChannel]!![iSample] = -data!![iChannel]!![iSample]
                    iSample += 2
                }
            }

            // correlation
            var i = 0
            while (i < 2 * nfft - 1) {
                P!![0]!![i] = data!![0]!![i] * dataConj!![1]!![i] - data!![0]!![i + 1] * dataConj!![1]!![i + 1]
                P!![0]!![i + 1] = data!![0]!![i] * dataConj!![1]!![i + 1] + data!![0]!![i + 1] * dataConj!![1]!![i]
                P!![1]!![i] = data!![0]!![i] * dataConj!![0]!![i] - data!![0]!![i + 1] * dataConj!![0]!![i + 1]
                P!![1]!![i + 1] = data!![0]!![i] * dataConj!![0]!![i + 1] + data!![0]!![i + 1] * dataConj!![0]!![i]
                P!![2]!![i] = data!![1]!![i] * dataConj!![1]!![i] - data!![1]!![i + 1] * dataConj!![1]!![i + 1]
                P!![2]!![i + 1] = data!![1]!![i] * dataConj!![1]!![i + 1] + data!![1]!![i + 1] * dataConj!![1]!![i]
                i += 2
            }


            // recursive averaging & store data for next average
            for (k in 0..2) {
                for (i in 0 until nfft * 2) {
                    Ptemp!![k]!![i] = alpha * Ptemp!![k]!![i] + (1 - alpha) * P!![k]!![i]
                    P!![k]!![i] = Ptemp!![k]!![i]
                }
            }

            // count blocks
            block++

            // write a block every 125 ms, i.e. every 10th block (for blocksize of 25ms)
            if (block >= 10) {
                val dataOut = arrayOf<FloatArray>()
                dataOut[0] = FloatArray(nfft + 2) // complex spectrum (cross-correlation)
                dataOut[1] = FloatArray(nfft / 2 + 1) // real spectrum (auto-correlation)
                dataOut[2] = FloatArray(nfft / 2 + 1) // real spectrum (auto-correlation)

                // copy one-sided spectrum for cross-correlation and scale
                dataOut[0][0] = P!![0]!![0] / Stage.Companion.samplingrate // 0 Hz
                dataOut[0][nfft] = P!![0]!![nfft] / Stage.Companion.samplingrate // fs/2
                for (i in 2 until nfft) {
                    dataOut[0][i] = P!![0]!![i] / (2 * Stage.Companion.samplingrate)
                }

                // copy one sided spectrum for auto correlation, scale, omit imaginary parts
                for (k in 1..2) {
                    dataOut[k][0] = P!![k]!![0] / Stage.Companion.samplingrate
                    dataOut[k][nfft / 2] = P!![k]!![nfft] / Stage.Companion.samplingrate
                    for (i in 1 until nfft / 2) {
                        dataOut[k][i] = P!![k]!![2 * i] / (2 * Stage.Companion.samplingrate)
                    }
                }
                send(dataOut)
                block = 0
            }
        }

        private fun nextpow2(x: Int): Int {
            return 1 shl 32 - Integer.numberOfLeadingZeros(x - 1)
        }

        private fun hann(samples: Int, nfft: Int): FloatArray? {
            val window = FloatArray(samples)
            var norm = 0f

            // calculate window & normalisation constant
            for (i in 0 until samples) {
                window[i] = (0.5 - 0.5 * Math.cos(2 * Math.PI * i.toFloat() / samples)).toFloat()
                norm += window[i] * window[i]
            }
            norm *= samples.toFloat() / nfft

            // normalise
            for (i in 0 until samples) {
                window[i] /= norm
            }
            return window
        }

        init {
            nfft = nextpow2(blockSize)
            println("----------------> NFFT: $nfft")
            window = hann(blockSize, nfft)
            fft = FloatFFT_1D(nfft)
            alpha = Math.exp(-(blockSize - hopSize) / (Stage.Companion.samplingrate * 0.125)).toFloat()
            samples = blockSize
        }
    }

    companion object {
        val LOG: String? = "StageProcPSD"
    }

    init {
        cpsd = CPSD()
    }
}