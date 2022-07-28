package com.iha.olmega_mobilesoftware_v2.AFEx.AcousticFeatureExtraction

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import java.time.Instant

/**
 * Capture audio using Android's AudioRecorder
 */
class StageAudioCapture(parameter: HashMap<*, *>?) : Stage(parameter!!) {
    private val audioRecord: AudioRecord?
    private val buffersize: Int
    private val blocksize_ms: Int
    private val frames: Int
    private var stopRecording = false

    override fun process(temp: Array<FloatArray>?) {
        var samplesRead: Int
        var i = 0
        val buffer = ShortArray(buffersize / 2)
        var dataOut = Array<FloatArray>(Stage.Companion.channels) { FloatArray(frames) }
        audioRecord!!.startRecording()
        Log.d(LOG, "Started producing")

        //Stage.startTime = Instant.now();
        while (!stopRecording and !Thread.currentThread().isInterrupted) {

            // read audio data
            samplesRead = audioRecord.read(buffer, 0, buffer.size)
            for (k in 0 until samplesRead / 2) {

                // split channels
                dataOut[0][i] = buffer[k * 2].toFloat() / Short.MAX_VALUE
                dataOut[1][i] = buffer[k * 2 + 1].toFloat() / Short.MAX_VALUE
                i++
                if (i >= frames) {
                    if (Stage.Companion.startTime == null) Stage.Companion.startTime = Instant.now().minusMillis((frames.toFloat() / Stage.Companion.samplingrate.toFloat() * 1000.0).toLong())
                    // send data to queue, reset dataOut & counter
                    send(dataOut)
                    dataOut = Array(Stage.Companion.channels) { FloatArray(frames) }
                    i = 0
                }
            }
        }
        Log.d(LOG, "Stopped producing")
        audioRecord.stop()
        stopRecording = false
    }

    override fun stop() {
        stopRecording = true
    }

    companion object {
        val LOG: String? = "StageProducer"
    }

    init {
        Log.d(LOG, "Setting up audioCapture")
        hasInput = false
        blocksize_ms = 25
        frames = blocksize_ms * Stage.Companion.samplingrate / 100
        buffersize = AudioRecord.getMinBufferSize(Stage.Companion.samplingrate,
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
        ) * 4
        Log.d(LOG, "Buffersize: $buffersize")
        audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                Stage.Companion.samplingrate,
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                buffersize
        )
    }
}