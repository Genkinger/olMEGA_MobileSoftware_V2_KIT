package com.iha.olmega_mobilesoftware_v2.AFEx.AcousticFeatureExtraction

import android.content.Context
import com.iha.olmega_mobilesoftware_v2.AFEx.Tools.AudioFileIO
import java.io.File

/**
 * Sets up and starts stages, i.e. producers, conducers and consumers.
 */
class StageManager @JvmOverloads constructor(context: Context?, features: File? = null) {
    var mainStage: Stage?
    var isRunning = false
    fun start() {

        // Start time is set here, will get overwritten in 1st Stage, e.g. StageAudioCapture.
        //Stage.startTime = Instant.now();
        mainStage!!.start()
        isRunning = true
    }

    fun stop() {
        mainStage!!.stop()
        // Following stages will stop automagically when queue.poll() times out.
        isRunning = false
    }

    init {
        var features = features
        Stage.Companion.samplingrate = 16000
        Stage.Companion.channels = 2
        Stage.Companion.context = context
        // build processing tree
        if (features == null) features = File(AudioFileIO.Companion.MAIN_FOLDER + File.separator + AudioFileIO.Companion.STAGE_CONFIG)
        mainStage = StageFactory().parseConfig(features)
    }
}