package com.iha.olmega_mobilesoftware_v2.AFEx.AcousticFeatureExtraction

import android.util.Log
import com.iha.olmega_mobilesoftware_v2.AFEx.AcousticFeatureExtraction.StageRFCOMM
import com.iha.olmega_mobilesoftware_v2.AFEx.Tools.AudioFileIO
import java.io.DataOutputStream
import java.io.IOException
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.experimental.and

/**
 * Write raw audio to disk
 */
class StageAudioWrite(parameter: HashMap<*, *>?) : Stage(parameter!!) {
    var io: AudioFileIO? = null
    var stream: DataOutputStream? = null
    private var calibValues: FloatArray? = floatArrayOf(1f, 1f)
    var CalibValuesLoaded = false
    var timeFormat = DateTimeFormatter.ofPattern("uuuuMMdd_HHmmssSSS")
            .withLocale(Locale.getDefault())
            .withZone(ZoneId.systemDefault())

    override fun start() {
        io = AudioFileIO("cache_" + timeFormat!!.format(Stage.Companion.startTime))
        stream = io!!.openDataOutStream(
                Stage.Companion.samplingrate,
                Stage.Companion.channels,
                16,
                true)
        super.start()
    }

    override fun rebuffer() {

        // we do not want rebuffering in a writer stage, just get the data and and pass it on.
        var abort = false
        Log.d(LOG, "----------> $id: Start processing")
        while (!Thread.currentThread().isInterrupted and !abort) {
            val data = receive()
            if (data != null) {
                process(data)
            } else {
                abort = true
            }
        }
        io!!.closeDataOutStream()
        Log.d(LOG, "$id: Stopped consuming")
    }

    override fun process(buffer: Array<FloatArray>?) {
        val dataOut = ByteArray(buffer!!.size * buffer[0].size * 2)
        var tmp: Short
        if (!CalibValuesLoaded) {
            var tempStage: Stage? = this
            while (tempStage != null) {
                tempStage = tempStage.inStage
                if (tempStage != null && tempStage.javaClass == StageRFCOMM::class.java && !java.lang.Float.isNaN((tempStage as StageRFCOMM?)!!.calibValues!![0]) && !java.lang.Float.isNaN((tempStage as StageRFCOMM?)!!.calibValues!![1])) {
                    calibValues = (tempStage as StageRFCOMM?)!!.calibValues!!.clone()
                    break
                }
            }
            CalibValuesLoaded = true
        }
        for (i in 0 until buffer[0].size) {
            tmp = (buffer[0][i] * Short.MAX_VALUE.toFloat() / calibValues!![0]).toInt().toShort()
            dataOut[i * 4] = (tmp and 0xff).toByte()
            dataOut[i * 4 + 1] = (tmp.toInt() shr 8 and 0xff).toByte()
            tmp = (buffer[1][i] * Short.MAX_VALUE.toFloat() / calibValues!![1]).toInt().toShort()
            dataOut[i * 4 + 2] = (tmp and 0xff).toByte()
            dataOut[i * 4 + 3] = (tmp.toInt() shr 8 and 0xff).toByte()
        }
        try {
            stream!!.write(dataOut)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {
        val LOG: String? = "StageConsumer"
    }
}