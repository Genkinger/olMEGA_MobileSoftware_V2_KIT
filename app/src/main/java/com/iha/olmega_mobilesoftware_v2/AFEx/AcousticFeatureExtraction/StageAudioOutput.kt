package com.iha.olmega_mobilesoftware_v2.AFEx.AcousticFeatureExtraction

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.util.Log
import com.iha.olmega_mobilesoftware_v2.AFEx.AcousticFeatureExtraction.StageRFCOMM
import kotlin.experimental.and

/**
 * Audio Output
 */
class StageAudioOutput(parameter: HashMap<*, *>?) : Stage(parameter!!) {
    private var audioTrack: AudioTrack? = null
    private val buffersize: Int
    private var calibValues: FloatArray? = floatArrayOf(1f, 1f)
    var CalibValuesLoaded = false


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
        audioTrack!!.write(dataOut, 0, dataOut.size, AudioTrack.WRITE_NON_BLOCKING)
    }

    override fun stop() {
        audioTrack!!.stop()
    }

    companion object {
        val LOG: String? = "StageProducer"
    }

    init {
        Log.d(LOG, "Setting up audioOutput")
        buffersize = AudioRecord.getMinBufferSize(Stage.Companion.samplingrate,
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
        ) * 20
        audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                Stage.Companion.samplingrate,
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                buffersize,
                AudioTrack.MODE_STREAM)
        audioTrack!!.play()
    }
}