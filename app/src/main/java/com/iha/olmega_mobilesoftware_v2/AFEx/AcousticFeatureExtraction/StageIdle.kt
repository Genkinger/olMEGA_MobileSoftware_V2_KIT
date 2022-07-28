package com.iha.olmega_mobilesoftware_v2.AFEx.AcousticFeatureExtractionimport

import com.iha.olmega_mobilesoftware_v2.AFEx.AcousticFeatureExtraction.Stage

class StageIdle(parameter: HashMap<*, *>?) : Stage(parameter!!) {
    init {
        hasInput = false
    }
    override fun process(buffer: Array<FloatArray>?) {}
}