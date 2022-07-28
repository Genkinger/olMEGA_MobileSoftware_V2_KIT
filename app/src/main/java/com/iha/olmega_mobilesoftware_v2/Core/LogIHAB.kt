package com.iha.olmega_mobilesoftware_v2.Core

import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by ul1021 on 03.08.2018.
 */
object LogIHAB {
    private val mFileName: String = "log.txt"
    private val LOG: String = "LogIHAB"
    private var lastMessage: String = ""

    // Write input string to log file
    fun log(string: String) {
        if (string != lastMessage) {
            lastMessage = string
            val file: File = File(FileIO.Companion.folderPath + File.separator + mFileName)
            var fw: FileWriter? = null
            val formattedString = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date()) + " " + string
            try {
                fw = FileWriter(file, true)
                fw.append(formattedString)
                fw.append(System.getProperty("line.separator"))
            } catch (e: IOException) {
                Log.e(LOG, "Error writing file.")
            } finally {
                if (fw != null) try {
                    fw.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}