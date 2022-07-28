package com.iha.olmega_mobilesoftware_v2.Core

import android.util.Log
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class AudioFileIO {
    lateinit var filename: String
    var samplerate = 0
    var channels = 0
    var format = 0
    var isWave = false
    lateinit var file: File
    lateinit var stream: DataOutputStream

    // create main folder
    val folderPath: String?
        get() {
            val baseDirectory = File(CACHE_FOLDER)
            if (!baseDirectory.exists()) {
                baseDirectory.mkdir()
            }
            return baseDirectory.absolutePath
        }

    // build filename
    fun getFilename(wavHeader: Boolean): String {
        return StringBuilder()
                .append(folderPath)
                .append(File.separator)
                .append(String.format("%06d", chunkId))
                .append("_")
                .append(SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.US))
                .append(".")
                .append(getExtension(wavHeader))
                .toString()
    }

    // file extension depending on format
    fun getExtension(isWave: Boolean?): String {
        return if (isWave!!) CACHE_WAVE else CACHE_RAW
    }

    // open output stream w/o filename
    fun openDataOutStream(_samplerate: Int, _channels: Int, _format: Int, _isWave: Boolean): DataOutputStream? {
        samplerate = _samplerate
        channels = _channels
        format = _format
        isWave = _isWave
        filename = getFilename(isWave)
        file = File(filename)
        return openFileStream()
    }

    fun openFileStream(): DataOutputStream? {
        try {
            val os = FileOutputStream(file, false)
            stream = DataOutputStream(BufferedOutputStream(os))

            // Write zeros. This will be filled with a proper header on close.
            // Alternatively, FileChannel might be used.
            if (isWave) {
                val nBytes = 44 // length of the WAV (RIFF) header
                val zeros = ByteArray(nBytes)
                for (i in 0 until nBytes) {
                    zeros[i] = 0
                }
                stream.write(zeros)
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return stream
    }

    // close the output stream
    fun closeDataOutStream() {
        try {
            stream.flush()
            stream.close()
            if (isWave) {
                writeWavHeader()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // open input stream
    fun openInputStream(filepath: String): FileInputStream? {
        var inputStream: FileInputStream? = null
        try {
            inputStream = FileInputStream(filepath)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return inputStream
    }

    // close the input stream
    fun closeInStream(stream: BufferedInputStream?) {
        try {
            stream!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // write WAV (RIFF) header
    fun writeWavHeader() {
        val GROUP_ID = "RIFF".toByteArray()
        val RIFF_TYPE = "WAVE".toByteArray()
        val FORMAT_ID = "fmt ".toByteArray()
        val DATA_ID = "data".toByteArray()
        val FORMAT_TAG: Short = 1 // PCM
        val FMT_LENGTH = 16
        val bitsize: Short = 16 // TODO
        try {
            val raFile = RandomAccessFile(file, "rw")
            val fileLength = raFile.length().toInt() // [bytes]
            val chunkSize = fileLength - 8
            val dataSize = fileLength - 44
            val blockAlign = (channels * (bitsize / 8)).toShort()
            val bytesPerSec = samplerate * blockAlign
            Log.e(LOG, "Bytes/s: $bytesPerSec, BlockAlign: $blockAlign")


            // RIFF-Header
            raFile.write(GROUP_ID)
            raFile.writeInt(Integer.reverseBytes(chunkSize))
            raFile.write(RIFF_TYPE)

            // fmt
            raFile.write(FORMAT_ID)
            raFile.writeInt(Integer.reverseBytes(FMT_LENGTH))
            raFile.writeShort(java.lang.Short.reverseBytes(FORMAT_TAG).toInt())
            raFile.writeShort(java.lang.Short.reverseBytes(channels.toShort()).toInt())
            raFile.writeInt(Integer.reverseBytes(samplerate))
            raFile.writeInt(Integer.reverseBytes(bytesPerSec))
            raFile.writeShort(java.lang.Short.reverseBytes(blockAlign).toInt())
            raFile.writeShort(java.lang.Short.reverseBytes(bitsize).toInt())

            // data
            raFile.write(DATA_ID)
            raFile.writeInt(Integer.reverseBytes(dataSize))
            raFile.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {
        protected val LOG: String = "IOClass"
        val MAIN_FOLDER: String = FileIO.Companion.FOLDER_MAIN
        val CACHE_FOLDER: String = "$MAIN_FOLDER/cache"
        val FEATURE_FOLDER: String = "$MAIN_FOLDER/features"
        val CACHE_WAVE: String = "wav"
        val CACHE_RAW: String = "raw"
        private var chunkId = 1
        fun setChunkId(id: Int) {
            chunkId = id
        }

        // delete a file
        fun deleteFile(filename: String): Boolean {
            val success: Boolean
            val file = File(filename)
            if (file.exists()) {
                success = file.delete()
                if (!success) {
                    Log.d(LOG, "Failed to delete $filename")
                }
            } else {
                Log.d(LOG, "Failed to delete $filename: File does not exist")
                success = false
            }
            return success
        }
    }
}