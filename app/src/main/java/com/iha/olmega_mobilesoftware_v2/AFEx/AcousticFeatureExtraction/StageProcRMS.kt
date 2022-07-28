package com.iha.olmega_mobilesoftware_v2.AFEx.AcousticFeatureExtraction

/**
 * Feature extraction: RMS
 */
class StageProcRMS(parameter: HashMap<*, *>?) : Stage(parameter!!) {
    override fun process(buffer: Array<FloatArray>?) {
        val dataOut = Array<FloatArray>(buffer!!.size) { FloatArray(1) }
        for (i in buffer.indices) {
            dataOut[i][0] = rms(buffer[i])
        }
        send(dataOut)
    }

    protected fun rms(data: FloatArray?): Float {
        var temp = 0f
        for (sample in data!!) {
            temp += sample * sample
        }
        temp /= data.size.toFloat()
        return Math.sqrt(temp.toDouble()).toFloat()
    }

    companion object {
        val LOG: String? = "StageProcRMS"
    }
}