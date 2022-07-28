package com.iha.olmega_mobilesoftware_v2.AFEx.Processing.Preprocessing

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/*
 * High-Pass Filter
 *
 * Based on Robert Bristow-Johnson's Audio-EQ Cookbook:
 * http://www.musicdsp.org/files/Audio-EQ-Cookbook.txt
 */
class FilterHP(fs: Int, f0: Int) {
    private val b0: Float
    private val b1: Float
    private val b2: Float
    private val a1: Float
    private val a2: Float
    private var x0 = 0f
    private var x1 = 0f
    private var x2 = 0f
    private var y0 = 0f
    private var y1 = 0f
    private var y2 = 0f

    init {
        val w0 = (2.0f * PI * f0 / fs).toFloat()
        val w0sin = sin(w0.toDouble()).toFloat()
        val w0cos = cos(w0.toDouble()).toFloat()
        val q = sin(PI / 4.0f).toFloat()
        val alpha = w0sin / (2.0f * q)
        val a0 = 1 + alpha
        b0 = (1 + w0cos) / 2 / a0
        b1 = -(1 + w0cos) / a0
        b2 = (1 + w0cos) / 2 / a0
        a1 = -2 * w0cos / a0
        a2 = (1 - alpha) / a0
        reset()
    }

    fun reset() {
        x0 = 0f
        x1 = 0f
        x2 = 0f
        y0 = 0f
        y1 = 0f
        y2 = 0f
    }

    fun filter(data: FloatArray) {
        val nSamples = data.size
        for (kk in 0 until nSamples) {
            x2 = x1
            x1 = x0
            x0 = data[kk]
            y0 = b0 * x0 + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2
            y2 = y1
            y1 = y0
            data[kk] = y0
        }
    }


}