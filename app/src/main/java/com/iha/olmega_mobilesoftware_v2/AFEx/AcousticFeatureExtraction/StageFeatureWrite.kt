package com.iha.olmega_mobilesoftware_v2.AFEx.AcousticFeatureExtraction

import android.util.Log
import com.iha.olmega_mobilesoftware_v2.AFEx.Tools.AudioFileIO
import com.iha.olmega_mobilesoftware_v2.AFEx.Tools.NetworkIO
import com.iha.olmega_mobilesoftware_v2.Core.SingleMediaScanner
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Write feature data to disk
 *
 * - Per default the writer assumes that incoming data represents one frame.
 * If the array is a multidimensional, the data is concatenated.
 * TODO: Check if allocating for each small buffer is problematic of if it is reasonable to cache
 * small buffers (e.g. RMS).
 * Alternatively, all features could be required to concatenate before sending, so the number of
 * frames can be calculated from the incoming array size.
 *
 * - Length of a feature file is determined by time, corresponding to e.g. 60 seconds of audio data.
 *
 * - Timestamp calculation is based on time set in Stage and relative block sizes. Implement
 * sanity check to compare calculated to actual time? Take into account the delay of the
 * processing queue?
 */
class StageFeatureWrite(parameter: HashMap<*, *>?) : Stage(parameter!!) {
    private var featureFile: File? = null
    private var featureRAF: RandomAccessFile? = null
    private var startTime: Instant? = null
    private var currentTime: Instant? = null
    private var timestamp: String? = null
    private val feature: String?
    private var isUdp = 0
    private var nFeatures = 0
    private var blockCount = 0
    private var bufferSize = 0
    var mySamplingRate // nedded because subsampling
            : Int
    private var hopDuration = 0
    private var relTimestamp = intArrayOf(0)
    private var inStage_hopSizeOut = 0
    private var inStage_blockSizeOut = 0
    private val featFileSize = 60f // size of feature files in seconds.
    private var HeaderPos_calibValues: Long = -1
    var timeFormat = DateTimeFormatter.ofPattern("uuuuMMdd_HHmmssSSS")
            .withLocale(Locale.getDefault())
            .withZone(ZoneId.systemDefault())
    var timeFormatUdp = DateTimeFormatter.ofPattern("HHmmssSSS")
            .withLocale(Locale.getDefault())
            .withZone(ZoneId.systemDefault())

    override fun start() {
        inStage_hopSizeOut = inStage!!.hopSizeOut
        inStage_blockSizeOut = inStage!!.blockSizeOut
        //startTime = Stage.startTime;
        //currentTime = startTime;
        relTimestamp = intArrayOf(0, 0)
        //openFeatureFile();
        super.start()
    }

    fun startWithoutThread() {
        inStage_hopSizeOut = inStage!!.hopSizeOut
        inStage_blockSizeOut = inStage!!.blockSizeOut
        relTimestamp = intArrayOf(0, 0)
        //openFeatureFile();
    }

    override fun cleanup() {
        closeFeatureFile()
        super.cleanup()
    }

    override fun rebuffer() {
        // we do not want rebuffering in a writer stage, just get the data and and pass it on.
        var abort = false
        Log.d(Stage.Companion.LOG, "----------> $id: Start processing")
        while (!Thread.currentThread().isInterrupted and !abort) {
            if (hasInQueue()) {
                val data = receive()
                if (data != null) {
                    process(data)
                } else {
                    abort = true
                }
            }
        }
        closeFeatureFile()
        Log.d(Stage.Companion.LOG, "$id: Stopped consuming")
    }

    public override fun process(buffer: Array<FloatArray>?) {
        appendFeature(buffer)
    }

    private fun openFeatureFile() {
        val directory: File = File(AudioFileIO.Companion.FEATURE_FOLDER)
        if (!directory.exists()) {
            directory.mkdir()
        }
        if (featureRAF != null) {
            closeFeatureFile()
        }
        if (startTime == null) {
            startTime = Stage.Companion.startTime
            currentTime = startTime
        }
        // add length of last feature file to current time
        currentTime = currentTime!!.plusMillis((relTimestamp[0].toFloat() / mySamplingRate.toFloat() * 1000.0).toLong())
        timestamp = timeFormat!!.format(currentTime)
        try {
            featureFile = File(directory.toString() + "/" + feature + "_" + timestamp + EXTENSION)
            //featureFile = new File(directory + "/" + feature + "_" + timeFormat.format(Instant.now()) + EXTENSION);
            featureRAF = RandomAccessFile(featureFile, "rw")

            // write header
            featureRAF!!.writeInt(4) // Feature File Version
            featureRAF!!.writeInt(0) // block count, written on close
            featureRAF!!.writeInt(0) // feature dimensions, written on close
            featureRAF!!.writeInt(inStage_blockSizeOut) // [samples]
            featureRAF!!.writeInt(inStage_hopSizeOut) // [samples]
            featureRAF!!.writeInt(mySamplingRate)
            featureRAF!!.writeBytes(timestamp!!.substring(2)) // YYMMDD_HHMMssSSS, 16 bytes (absolute timestamp)
            featureRAF!!.writeBytes(timeFormat!!.format(Instant.now()).substring(2)) // YYMMDD_HHMMssSSS, 16 bytes (absolute timestamp)
            HeaderPos_calibValues = featureRAF!!.filePointer
            featureRAF!!.writeFloat(0.0.toFloat()) // calibration value 1, written on close
            featureRAF!!.writeFloat(0.0.toFloat()) // calibration value 2, written on close
            featureRAF!!.writeBytes(String.format("%1$16s", "")) // Android ID
            featureRAF!!.writeBytes(String.format("%1$17s", "")) // Bluetooth Transmitter MAC
            blockCount = 0
            hopDuration = inStage_hopSizeOut
            relTimestamp[0] = relTimestamp[0] % (featFileSize * mySamplingRate).toInt()
            relTimestamp[1] = relTimestamp[0] + (inStage_blockSizeOut - 1)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    protected fun appendFeature(data: Array<FloatArray>?) {
        //System.out.println("timestamp: " + relTimestamp[1] + " | size: " + featFileSize);
        // start a new feature file?
        if (featureRAF == null || relTimestamp[0] >= featFileSize * mySamplingRate) {
            // Update timestamp based on samples processed. This only considers block- and hopsize
            // of the previous stage. If another stage uses different hopping, averaging or any
            // other mechanism to obscure samples vs. time, this has to be tracked elsewhere!
            openFeatureFile()
        }
        // calculate buffer size from actual data to take care of jagged arrays (e.g. PSDs):
        // (samples in data + 2 timestamps) * 4 bytes
        if (bufferSize == 0) {
            nFeatures = 2 // timestamps
            for (aData in data!!) {
                //Log.d(LOG, "LENGTH: " + aData.length);
                nFeatures += aData.size
            }
            bufferSize = nFeatures * 4 // 4 bytes to a float
        }
        val bbuffer = ByteBuffer.allocate(bufferSize)
        val fbuffer = bbuffer.asFloatBuffer()
        fbuffer.put(floatArrayOf(relTimestamp[0].toFloat() / mySamplingRate, relTimestamp[1].toFloat() / mySamplingRate))

        // send UDP packets. Only passes the first array!
        if (isUdp == 1 && data!![0][0] == 1f) {
            NetworkIO.sendUdpPacket(timeFormatUdp!!.format(currentTime!!.plusMillis((relTimestamp[0].toFloat() / mySamplingRate * 1000.0).toLong())))
        }
        for (aData in data!!) {
            fbuffer.put(aData)
        }

        // round to 4 decimals -> milliseconds * 10^-1.
        relTimestamp[0] = relTimestamp[0] + hopDuration
        relTimestamp[1] = relTimestamp[1] + hopDuration
        try {
            if (featureRAF != null) {
                featureRAF!!.channel.write(bbuffer)
                blockCount++
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Synchronized
    private fun closeFeatureFile() {
        try {
            var calibValuesInDB = floatArrayOf(0f, 0f)
            var HardwareIDs = arrayOf<String?>(String.format("%1$16s", ""), String.format("%1$16s", ""))
            var tempStage: Stage? = this
            while (tempStage != null) {
                tempStage = tempStage.inStage
                if (tempStage != null && tempStage.javaClass == StageRFCOMM::class.java && !java.lang.Float.isNaN((tempStage as StageRFCOMM?)!!.calibValuesInDB!![0]) && !java.lang.Float.isNaN((tempStage as StageRFCOMM?)!!.calibValuesInDB!![1])) {
                    calibValuesInDB = (tempStage as StageRFCOMM?)!!.calibValuesInDB!!.clone()
                    HardwareIDs = (tempStage as StageRFCOMM?)!!.HardwareIDs!!.clone()
                    break
                }
            }
            if (featureRAF != null) {
                featureRAF!!.seek(4)
                featureRAF!!.writeInt(blockCount) // block count for this feature file
                featureRAF!!.writeInt(nFeatures) // features + timestamps per block
                featureRAF!!.seek(HeaderPos_calibValues)
                featureRAF!!.writeFloat(calibValuesInDB[0])
                featureRAF!!.writeFloat(calibValuesInDB[1])
                featureRAF!!.writeBytes(String.format("%1$16s", HardwareIDs[0]).substring(0, 16))
                featureRAF!!.writeBytes(String.format("%1$17s", HardwareIDs[1]).substring(0, 17))
                featureRAF!!.close()
                if (blockCount == 0 && nFeatures == 0) featureFile!!.delete()
                featureRAF = null
            }
            SingleMediaScanner(context, featureFile)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {
        private val EXTENSION: String? = ".feat"
    }

    init {
        feature = parameter!!["prefix"] as String?
        if (parameter["udp"] == null) isUdp = 0 else isUdp = parameter["udp"].toString().toInt()
        mySamplingRate = Stage.Companion.samplingrate
    }
}