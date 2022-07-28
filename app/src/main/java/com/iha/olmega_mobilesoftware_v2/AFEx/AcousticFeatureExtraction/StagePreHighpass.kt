package com.iha.olmega_mobilesoftware_v2.AFEx.AcousticFeatureExtraction

import com.iha.olmega_mobilesoftware_v2.AFEx.Processing.Preprocessing.FilterHP

/**
 * Audio preprocessing
 */
class StagePreHighpass(parameter: HashMap<*, *>?) : Stage(parameter!!) {
    var cutoff_hz: Int
    var filterHP: Array<FilterHP?>?
    override fun process(buffer: Array<FloatArray>?) {
        val dataOut = Array<FloatArray>(buffer!!.size) { FloatArray(buffer[0].size) }
        for (i in 0..1) {
            System.arraycopy(buffer[i], 0, dataOut[i], 0, buffer[i].size)
            filterHP!![i]!!.filter(dataOut[i])
        }
        send(dataOut)
    }

    companion object {
        val LOG: String? = "StagePreHighpass"
    }

    init {
        cutoff_hz = parameter!!["cutoff_hz"].toString().toInt()
        filterHP = arrayOfNulls<FilterHP?>(Stage.Companion.channels)
        for (i in 0 until Stage.Companion.channels) {
            filterHP!![i] = FilterHP(Stage.Companion.samplingrate, cutoff_hz)
        }
    }
}