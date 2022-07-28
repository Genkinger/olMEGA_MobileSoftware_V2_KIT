package com.iha.olmega_mobilesoftware_v2.AFEx.AcousticFeatureExtraction

/**
 * Feature extraction: Maximal value in block
 */
class StageProcMaxValue(parameter: HashMap<*, *>?) : Stage(parameter!!) {
    override fun process(buffer: Array<FloatArray>?) {
        val dataOut = Array<FloatArray>(buffer!!.size) { FloatArray(1) }
        for (i in buffer.indices) {
            dataOut[i][0] = maxValue(buffer[i])
        }
        send(dataOut)
    }

    protected fun maxValue(data: FloatArray?): Float {
        var max = 0f
        var value: Float
        for (aData in data!!) {
            value = Math.abs(aData)
            if (value > max) max = value
        }
        return max
    }

    companion object {
        val LOG: String? = "StageProcMaxValue"
    }
}