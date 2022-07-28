/* 2x up- and downsampling
 *
 * Based on the corresponding C++ implementation by Marco Ruhland, 2009
 *
 */
package com.iha.olmega_mobilesoftware_v2.AFEx.Processing.Preprocessing

class CResampling {
    private var m_xpos1_ds = 0
    private var m_xpos2_ds = 0
    private var m_xpos_us = 0
    private val m_bcoeff_ds = FloatArray(32)
    private val m_xvec1_ds = FloatArray(64)
    private val m_xvec2_ds = FloatArray(33)
    private val m_bcoeff_us = FloatArray(32)
    private val m_xvec_us = FloatArray(64)

    init {
        m_bcoeff_us[0] = -0.000007589279555f // coefficients from ParksMcLellan-Design for anti-aliasing-filter
        m_bcoeff_us[1] = 0.000014322198629f // MATLAB: h=firpm(128,[0 0.45 0.55 1],[1 1 0 0]);
        m_bcoeff_us[2] = -0.000027380287300f // only one half of the filter coeffs, and all the zeroes left away
        m_bcoeff_us[3] = 0.000047790630650f // --> therefore only 32 coeffs stored here instead of 128
        m_bcoeff_us[4] = -0.000078268005383f // the 0th coefficient is 0.5f and is hard-coded in
        m_bcoeff_us[5] = 0.000122159126424f // the function Upsample2f.
        m_bcoeff_us[6] = -0.000183504414029f
        m_bcoeff_us[7] = 0.000267102568994f
        m_bcoeff_us[8] = -0.000378580777739f
        m_bcoeff_us[9] = 0.000524455894125f
        m_bcoeff_us[10] = -0.000712188588467f
        m_bcoeff_us[11] = 0.000950263467159f
        m_bcoeff_us[12] = -0.001248287205107f
        m_bcoeff_us[13] = 0.001617101218669f
        m_bcoeff_us[14] = -0.002068984059695f
        m_bcoeff_us[15] = 0.002617944057400f
        m_bcoeff_us[16] = -0.003280117638575f
        m_bcoeff_us[17] = 0.004074486286745f
        m_bcoeff_us[18] = -0.005023854567343f
        m_bcoeff_us[19] = 0.006156485422566f
        m_bcoeff_us[20] = -0.007508592696345f
        m_bcoeff_us[21] = 0.009128402960935f
        m_bcoeff_us[22] = -0.011082852122455f
        m_bcoeff_us[23] = 0.013469121084517f
        m_bcoeff_us[24] = -0.016435487666445f
        m_bcoeff_us[25] = 0.020221490523925f
        m_bcoeff_us[26] = -0.025241966087592f
        m_bcoeff_us[27] = 0.032283134342770f
        m_bcoeff_us[28] = -0.043034753000682f
        m_bcoeff_us[29] = 0.061899269714142f
        m_bcoeff_us[30] = -0.105037081083480f
        m_bcoeff_us[31] = 0.317953038191487f
        for (kk in 0..31) m_bcoeff_ds[kk] = m_bcoeff_us[kk] * 2f // pre-multiply coeffs for downsampling-filter by 2,
        // since multiplying by 2 is needed in 2x-downsampling to maintain
        // the signal magnitude.
        reset()
    }

    fun reset() {
        for (kk in 0..63) m_xvec1_ds[kk] = 0f
        for (kk in 0..32) m_xvec2_ds[kk] = 0f
        m_xpos1_ds = 0
        m_xpos2_ds = 0
        for (kk in 0..63) m_xvec_us[kk] = 0f
        m_xpos_us = 0
    }

    fun Downsample2f(data: FloatArray, numoutsamples: Int) {
        var mm = 0
        var outval: Float
        val buffer = FloatArray(numoutsamples)
        for (kk in 0 until numoutsamples) {
            m_xvec1_ds[m_xpos1_ds++] = data[mm++] // downsampling
            m_xvec2_ds[m_xpos2_ds++] = data[mm++]
            m_xpos1_ds = m_xpos1_ds and 63
            m_xpos2_ds %= 33
            outval = m_xvec2_ds[m_xpos2_ds]
            for (jj in 0..31)  // anti-alias-filtering
                outval += (m_xvec1_ds[m_xpos1_ds + jj and 63] + m_xvec1_ds[63 + m_xpos1_ds - jj and 63]) * m_bcoeff_ds[jj]
            buffer[kk] = outval / 2
        }
        System.arraycopy(buffer, 0, data, 0, buffer.size)
    }

    fun Downsample2fnoLP(data: FloatArray, numoutsamples: Int) {
        for (kk in 0 until numoutsamples)  // simple downsampling by factor 2 without anti-alias filtering
            data[kk] = data[kk * 2] * 2f // scaling by factor 2 is necessary, to maintain the signal magnitude
    }

    fun Upsample2f(data: FloatArray, numinsamples: Int) {
        var mm = 0
        var outval1: Float
        var outval2: Float
        val buffer = FloatArray(numinsamples * 2)
        for (kk in 0 until numinsamples) {
            m_xvec_us[m_xpos_us++] = data[kk]
            m_xpos_us = m_xpos_us and 63
            outval1 = m_xvec_us[31 + m_xpos_us and 63] * 0.5f // middle coefficient of anti-alias filter (0.5f)
            outval2 = 0f
            for (jj in 0..31)  // rest of anti-alias filtering
                outval2 += (m_xvec_us[m_xpos_us + jj and 63] + m_xvec_us[63 + m_xpos_us - jj and 63]) * m_bcoeff_us[jj]
            buffer[mm++] = outval1 // upsampling
            buffer[mm++] = outval2
        }
        System.arraycopy(buffer, 0, data, 0, buffer.size)
    }


}