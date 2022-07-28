package com.iha.olmega_mobilesoftware_v2.AFEx.AcousticFeatureExtraction

/**
 * Feature extraction: Zero crossing rate of signal and derivative of the signal
 */
class StageProcZCR(parameter: HashMap<*, *>?) : Stage(parameter!!) {
    override fun process(buffer: Array<FloatArray>?) {
        val dataOut = Array<FloatArray>(buffer!!.size) { FloatArray(2) }
        for (i in buffer.indices) {
            dataOut[i][0] = zcr(buffer[i]).toFloat()
            dataOut[i][1] = zcr(diff(buffer[i])).toFloat()
        }
        send(dataOut)
    }

    protected fun zcr(`in`: FloatArray?): Int {
        var count = 0
        val data_sign = FloatArray(`in`!!.size)
        data_sign[0] = Math.signum(`in`[0])
        for (kk in 1 until `in`.size) {
            data_sign[kk] = Math.signum(`in`[kk])
            if (data_sign[kk] - data_sign[kk - 1] != 0f) count++
        }
        return count
    }

    protected fun diff(data: FloatArray?): FloatArray? {
        val delta = FloatArray(data!!.size - 1)
        for (kk in 0 until data.size - 1) delta[kk] = data[kk + 1] - data[kk]
        return delta
    }

    companion object {
        val LOG: String? = "StageProcRMS"
    }
}