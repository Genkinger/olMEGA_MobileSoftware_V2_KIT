package com.iha.olmega_mobilesoftware_v2.AFEx.AcousticFeatureExtraction

import android.util.Log

class StageProcOnsetDetection(parameter: HashMap<*, *>?) : Stage(parameter!!) {
    private val T: Double
    private val bandSplit_State_1: DoubleArray?
    private val bandSplit_State_2: DoubleArray?
    private val bandSplit_Frequency = 800.0
    private val bandSplit_Q = 1.0f / Math.sqrt(2.0)
    private val bandSplit_Radians: Double
    private val bandSplit_wa: Double
    private val bandSplit_G: Double
    private val bandSplit_R: Double
    private val bandSplit_MulState1: Double
    private val bandSplit_MulIn: Double

    // [lp, bp, hp][L, R]
    private val energyRatio_Tau_Fast_ms: FloatArray?
    private val energyRatio_Tau_Slow_ms: FloatArray?
    private val energyRatio_State_Fast: Array<FloatArray?>?
    private val energyRatio_State_Slow: Array<FloatArray?>?
    private val energyRatio_Alpha_Fast: FloatArray?
    private val energyRatio_Alpha_Slow: FloatArray?
    private val energyRatio_Alpha_Fast_MinusOne: FloatArray?
    private val energyRatio_Alpha_Slow_MinusOne: FloatArray?

    // [LP, BP, HP, WB]
    private val detectOnsets_ThreshBase: FloatArray?
    private var detectOnsets_ThreshRaise: FloatArray?
    private val detectOnsets_Param1: FloatArray?
    private val detectOnsets_Decay: FloatArray?
    private var rms_rec = 0f
    private val alpha: Float
    override fun cleanup() {
        Log.d(LOG, "Stopped " + LOG)
        super.cleanup()
    }

    override fun process(buffer: Array<FloatArray>?) {
        val block_left = FloatArray(blockSize)
        val block_right = FloatArray(blockSize)
        for (iSample in 0 until blockSize) {
            block_left[iSample] = buffer!![0][iSample]
            block_right[iSample] = buffer[1][iSample]
        }
        val data = onsetDetection(block_left, block_right)
        val dataOut = Array<FloatArray>(1) { FloatArray(1) }
        dataOut[0][0] = data
        send(dataOut)
    }

    protected fun onsetDetection(block_left: FloatArray?, block_right: FloatArray?): Float {
        // left
        val bands_left = bandSplit(block_left, 0)
        // right
        val bands_right = bandSplit(block_right, 1)
        return detectOnsets(bands_left, bands_right, block_left, block_right)
    }

    protected fun detectOnsets(bands_left: Array<FloatArray?>?, bands_right: Array<FloatArray?>?, block_left: FloatArray?, block_right: FloatArray?): Float {
        var flags = Array<FloatArray?>(4) { FloatArray(2) }
        var flag = 0.0f
        var onsetFound = false
        var threshold: FloatArray? = floatArrayOf(0.0f, 0.0f, 0.0f, 0.0f)
        val rms = 0.5f * (rms(block_left) + rms(block_right))
        rms_rec = alpha * rms + (1.0f - alpha) * rms_rec
        val energy_lp_left = energyRatio(getChannel(bands_left, 0), 0, 0)
        val energy_bp_left = energyRatio(getChannel(bands_left, 1), 1, 0)
        val energy_hp_left = energyRatio(getChannel(bands_left, 2), 2, 0)
        val energy_wb_left = energyRatio(block_left, 3, 0)
        val energy_lp_right = energyRatio(getChannel(bands_right, 0), 0, 1)
        val energy_bp_right = energyRatio(getChannel(bands_right, 1), 1, 1)
        val energy_hp_right = energyRatio(getChannel(bands_right, 2), 2, 1)
        val energy_wb_right = energyRatio(block_right, 3, 1)
        for (iSample in 0 until blockSize) {
            threshold = addElementwise(detectOnsets_ThreshBase, detectOnsets_ThreshRaise)
            flags = Array(4) { FloatArray(2) }
            if (!onsetFound) {
                if (energy_lp_left!![iSample] > threshold!![0]) {
                    flags[0]!![0] = 1.0f
                }
                if (energy_bp_left!![iSample] > threshold[1]) {
                    flags[1]!![0] = 1.0f
                }
                if (energy_hp_left!![iSample] > threshold[2]) {
                    flags[2]!![0] = 1.0f
                }
                if (energy_wb_left!![iSample] > threshold[3]) {
                    flags[3]!![0] = 1.0f
                }
                if (energy_lp_right!![iSample] > threshold[0]) {
                    flags[0]!![1] = 1.0f
                }
                if (energy_bp_right!![iSample] > threshold[1]) {
                    flags[1]!![1] = 1.0f
                }
                if (energy_hp_right!![iSample] > threshold[2]) {
                    flags[2]!![1] = 1.0f
                }
                if (energy_wb_right!![iSample] > threshold[3]) {
                    flags[3]!![1] = 1.0f
                }

                // If more than one band has registered a peak then return 1, else 0
                if (flags[0]!![0] + flags[1]!![0] + flags[2]!![0] + flags[3]!![0] +
                        flags[0]!![1] + flags[1]!![1] + flags[2]!![1] + flags[3]!![1] > 1.0f) {
                    flag = 1.0f
                    onsetFound = true
                    detectOnsets_ThreshRaise = multiplyElementwise(
                            detectOnsets_Param1, threshold)
                }
            }
            multiplyWithArray(detectOnsets_ThreshRaise, detectOnsets_Decay)
        }
        return flag
    }

    protected fun rms(signal: FloatArray?): Float {
        var out = 0f
        for (iSample in signal!!.indices) {
            out += signal[iSample] * signal[iSample]
        }
        out /= signal.size.toFloat()
        return Math.sqrt(out.toDouble()).toFloat()
    }

    protected fun getChannel(`in`: Array<FloatArray?>?, chan: Int): FloatArray? {
        val out = FloatArray(`in`!!.size)
        for (iSample in `in`.indices) {
            out[iSample] = `in`[iSample]!![chan]
        }
        return out
    }

    protected fun multiplyWithArray(a: FloatArray?, b: FloatArray?) {
        for (iCol in a!!.indices) {
            a[iCol] *= b!![iCol]
        }
    }

    protected fun multiplyElementwise(a: FloatArray?, b: FloatArray?): FloatArray? {
        val out = FloatArray(a!!.size)
        for (iCol in a.indices) {
            out[iCol] = a[iCol] * b!![iCol]
        }
        return out
    }

    protected fun addElementwise(a: FloatArray?, b: FloatArray?): FloatArray? {
        val out = FloatArray(a!!.size)
        for (iCol in a.indices) {
            out[iCol] = a[iCol] + b!![iCol]
        }
        return out
    }

    protected fun bandSplit(signal: FloatArray?, chan: Int): Array<FloatArray?>? {
        /**
         * Perform a bandsplit via state variable filter
         *
         * input:  float[blocklen] signal
         * int chan [0, 1] left/right for correct filter states
         * output: float[blocklen][3] output (blocklen x [lp_L, bp_L, hp_L])
         *
         */
        var ylp: Float
        var ybp: Float
        var yhp: Float
        var tmp_1: Float
        var tmp_2: Float
        val output = Array<FloatArray?>(blockSize) { FloatArray(3) }
        for (iSample in 0 until blockSize) {
            yhp = ((signal!![iSample] - bandSplit_MulState1 * bandSplit_State_1!![chan] -
                    bandSplit_State_2!![chan]) * bandSplit_MulIn).toFloat()
            tmp_1 = (yhp * bandSplit_G).toFloat()
            ybp = (bandSplit_State_1[chan] + tmp_1).toFloat()
            bandSplit_State_1[chan] = (ybp + tmp_1).toDouble()
            tmp_2 = (ybp * bandSplit_G).toFloat()
            ylp = (bandSplit_State_2[chan] + tmp_2).toFloat()
            bandSplit_State_2[chan] = (ylp + tmp_2).toDouble()
            output[iSample]!![0] = ylp
            output[iSample]!![1] = ybp
            output[iSample]!![2] = yhp
        }
        return output
    }

    protected fun energyRatio(signal: FloatArray?, band: Int, chan: Int): FloatArray? {
        /**
         * Energy Ratio of signal
         *
         * Signal input format is float[blocklen] (single channel)
         *
         * Signal output format is float[blocklen] (single channel)
         *
         * band, chan: needed to target specific filter states (lp, bp, hp, wb)
         *
         */
        val signal_square = multiplyElementwise(signal, signal)
        val signal_filtered_Fast = filter(signal_square, energyRatio_Alpha_Fast, energyRatio_Alpha_Fast_MinusOne, band, chan, 0)
        val signal_filtered_Slow = filter(signal_square, energyRatio_Alpha_Slow, energyRatio_Alpha_Slow_MinusOne, band, chan, 1)
        val ratio = FloatArray(blockSize)
        for (iSample in 0 until blockSize) {
            ratio[iSample] = signal_filtered_Fast!![iSample] / signal_filtered_Slow!![iSample]
        }
        return ratio
    }

    protected fun filter(signal: FloatArray?, coeff_b: FloatArray?, coeff_a: FloatArray?, band: Int, chan: Int, fast_slow: Int): FloatArray? {
        /**
         * single channel IIR filter
         *
         * Input format of signal is float[blocklen]
         *
         * coeff_b is b[0]
         * coeff_a is a[1]; a[0] = 1
         *
         * band: [lp, bp, hp]
         * chan: [1, 2] (L, R)
         * fast_slow = [0, 1] either fast or slow
         *
         * (layout to account for state memory)
         *
         */
        val output = FloatArray(signal!!.size)
        var tmp: Float
        if (fast_slow == 0) {
            for (iSample in signal.indices) {
                tmp = coeff_b!![band] * signal[iSample] - coeff_a!![band] * energyRatio_State_Fast!![band]!![chan]
                output[iSample] = tmp
                energyRatio_State_Fast[band]!![chan] = tmp
            }
        } else {
            for (iSample in signal.indices) {
                tmp = coeff_b!![band] * signal[iSample] - coeff_a!![band] * energyRatio_State_Slow!![band]!![chan]
                output[iSample] = tmp
                energyRatio_State_Slow[band]!![chan] = tmp
            }
        }
        return output
    }

    companion object {
        val LOG: String? = "StageProcOnsetDetection"
    }

    init {
        T = (1.0f / Stage.Companion.samplingrate).toDouble()
        alpha = 0.1f / Stage.Companion.samplingrate
        bandSplit_State_1 = DoubleArray(2)
        bandSplit_State_2 = DoubleArray(2)
        bandSplit_Radians = bandSplit_Frequency * 2 * Math.PI
        bandSplit_wa = 2.0f / T * Math.tan(bandSplit_Radians * T / 2.0f)
        bandSplit_G = bandSplit_wa * T / 2.0f
        bandSplit_R = 0.5f / bandSplit_Q
        bandSplit_MulState1 = 2.0f * bandSplit_R + bandSplit_G
        bandSplit_MulIn = 1.0f / (1.0f + 2.0f * bandSplit_R * bandSplit_G + bandSplit_G * bandSplit_G)

        // [lp, bp, hp, wb][L, R]
        energyRatio_Tau_Fast_ms = floatArrayOf(4.0f, 2.0f, 2.0f, 2.0f) //1.0f, 1.0f, 1.0f, 1.0f
        energyRatio_Tau_Slow_ms = floatArrayOf(160.0f, 20.0f, 10.0f, 5.0f) //80.0f, 40.0f, 20.0f, 5.0f
        energyRatio_State_Fast = Array(4) { FloatArray(2) }
        energyRatio_State_Slow = Array(4) { FloatArray(2) }
        energyRatio_Alpha_Fast = floatArrayOf((1.0f - Math.exp(-1.0f / (energyRatio_Tau_Fast_ms[0] * 0.001 * Stage.Companion.samplingrate))).toFloat(), (1.0f - Math.exp(-1.0f / (energyRatio_Tau_Fast_ms[1] * 0.001 * Stage.Companion.samplingrate))).toFloat(), (1.0f - Math.exp(-1.0f / (energyRatio_Tau_Fast_ms[2] * 0.001 * Stage.Companion.samplingrate))).toFloat(), (1.0f - Math.exp(-1.0f / (energyRatio_Tau_Fast_ms[3] * 0.001 * Stage.Companion.samplingrate))).toFloat())
        energyRatio_Alpha_Slow = floatArrayOf((1.0f - Math.exp(-1.0f / (energyRatio_Tau_Slow_ms[0] * 0.001 * Stage.Companion.samplingrate))).toFloat(), (1.0f - Math.exp(-1.0f / (energyRatio_Tau_Slow_ms[1] * 0.001 * Stage.Companion.samplingrate))).toFloat(), (1.0f - Math.exp(-1.0f / (energyRatio_Tau_Slow_ms[2] * 0.001 * Stage.Companion.samplingrate))).toFloat(), (1.0f - Math.exp(-1.0f / (energyRatio_Tau_Slow_ms[2] * 0.001 * Stage.Companion.samplingrate))).toFloat())
        energyRatio_Alpha_Fast_MinusOne = floatArrayOf((-Math.exp(-1.0f / (energyRatio_Tau_Fast_ms[0] * 0.001 * Stage.Companion.samplingrate))).toFloat(), (-Math.exp(-1.0f / (energyRatio_Tau_Fast_ms[1] * 0.001 * Stage.Companion.samplingrate))).toFloat(), (-Math.exp(-1.0f / (energyRatio_Tau_Fast_ms[2] * 0.001 * Stage.Companion.samplingrate))).toFloat(), (-Math.exp(-1.0f / (energyRatio_Tau_Fast_ms[3] * 0.001 * Stage.Companion.samplingrate))).toFloat())
        energyRatio_Alpha_Slow_MinusOne = floatArrayOf((-Math.exp(-1.0f / (energyRatio_Tau_Slow_ms[0] * 0.001 * Stage.Companion.samplingrate))).toFloat(), (-Math.exp(-1.0f / (energyRatio_Tau_Slow_ms[1] * 0.001 * Stage.Companion.samplingrate))).toFloat(), (-Math.exp(-1.0f / (energyRatio_Tau_Slow_ms[2] * 0.001 * Stage.Companion.samplingrate))).toFloat(), (-Math.exp(-1.0f / (energyRatio_Tau_Slow_ms[2] * 0.001 * Stage.Companion.samplingrate))).toFloat())
        // [LP, BP, HP, WB]
        detectOnsets_ThreshBase = floatArrayOf(8.0f, 4.0f, 4.0f, 16.0f) //8.0f, 8.0f, 8.0f, 8.0f
        detectOnsets_ThreshRaise = floatArrayOf(0.0f, 0.0f, 0.0f, 0.0f) //0.0f, 0.0f, 0.0f, 0.0f
        detectOnsets_Param1 = floatArrayOf(8.0f, 4.0f, 6.0f, 32.0f) //4.0f, 4.0f, 4.0f, 4.0f
        detectOnsets_Decay = floatArrayOf(Math.pow(2000.0, (-1.0f / Stage.Companion.samplingrate).toDouble()).toFloat(), Math.pow(8000.0, (-1.0f / Stage.Companion.samplingrate).toDouble()).toFloat(), Math.pow(100.0, (-1.0f / Stage.Companion.samplingrate).toDouble()).toFloat(), Math.pow(100.0, (-1.0f / Stage.Companion.samplingrate).toDouble()).toFloat()) // 8192, 8192, 8192, 8192
    }
}