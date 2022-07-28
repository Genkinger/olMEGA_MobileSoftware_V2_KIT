package com.iha.olmega_mobilesoftware_v2.AFEx.Toolsimport

import java.text.SimpleDateFormat
import java.util.*

/**
 * Returns a timestamp as string
 */
object Timestamp {
    fun getTimestamp(type: Int): String {
        var timestamp: SimpleDateFormat? = null
        when (type) {
            1 -> timestamp = SimpleDateFormat("yyyyMMdd", Locale.US)
            2 -> timestamp = SimpleDateFormat("HHmmssSSS", Locale.US)
            3 -> timestamp = SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.US)
            4 -> timestamp = SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.SSS", Locale.US)
        }
        return timestamp!!.format(Date())
    }
}