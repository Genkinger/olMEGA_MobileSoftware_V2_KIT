package com.iha.olmega_mobilesoftware_v2.AFEx.AcousticFeatureExtraction

import android.util.Log

/**
 * Test Data Producer
 */
class StageTestData(parameters: HashMap<*, *>?) : Stage(parameters!!) {
    private val channels: Int
    private val frames: Int
    private var stopProducing = false
    override fun process(temp: Array<FloatArray>?) {
        var samplesRead: Int
        val idx = 0
        val buffer = ShortArray(frames * channels)
        var dataOut = Array<FloatArray>(channels) { FloatArray(frames) }
        for (i in 0 until frames * 2) {
            buffer[i] = 0
        }
        Log.d(LOG, "Started producing")
        while (!stopProducing and !Thread.currentThread().isInterrupted) {

            // create data
            for (i in 0 until frames * 2) {
                buffer[i] = (buffer[i] + 1).toShort()
            }
            for (k in 0 until frames) {
                // split channels
                dataOut[0][idx] = buffer[k * 2].toFloat()
                dataOut[1][idx] = buffer[k * 2 + 1].toFloat()
            }
            send(dataOut)
            dataOut = Array(channels) { FloatArray(frames) }
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        Log.d(LOG, "Stopped producing")
    }

    fun setStopProducing() {
        stopProducing = true
    }

    companion object {
        val LOG: String? = "StageProducer"
    }

    init {
        hasInput = false
        channels = 2
        frames = 10
    }
}