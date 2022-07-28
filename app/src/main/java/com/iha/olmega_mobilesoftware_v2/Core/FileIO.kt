package com.iha.olmega_mobilesoftware_v2.Core

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import com.iha.olmega_mobilesoftware_v2.BuildConfig
import com.iha.olmega_mobilesoftware_v2.MainActivity
import java.io.*
import java.util.*

/**
 * Created by ulrikkowalk on 23.03.17.
 */
class FileIO {
    private val isVerbose = false
    private var mContext: Context? = null

    fun setupFirstUse(context: Context?): Boolean {
        mContext = context
        return null != scanQuestOptions()
    }

    fun checkConfigFile(): Boolean {
        // If for whatever reason rules.ini exists, preferences are shown
        return scanConfigMode()
    }

    // Check whether preferences unlock file is present in main directory
    private fun scanConfigMode(): Boolean {
        val fileConfig = File(folderPath + File.separator + FILE_CONFIG)

        //new SingleMediaScanner(mContext, fileConfig);
        return fileConfig.exists()
    }

    private fun deleteConfigFile(): Boolean {
        val fileConfig = File(folderPath + File.separator + FOLDER_DATA +
                File.separator + FILE_CONFIG)
        return fileConfig.delete()
    }

    fun obtainCalibration(): FloatArray {

        // Obtain working Directory
        val dir = File(folderPath + "/" + FOLDER_CALIB)
        // Address Basis File in working Directory
        val file = File(dir, "calib.txt")
        val calib = floatArrayOf(0.0f, 0.0f)
        try {
            val sc = Scanner(file)
            // we just need to use \\Z as delimiter
            sc.useDelimiter(" ")
            calib[0] = java.lang.Float.valueOf(sc.next())
            calib[1] = java.lang.Float.valueOf(sc.next())
            for (i in 0..1) {
                Log.e(LOG, "CALIB: " + calib[i])
            }
        } catch (e: Exception) {
        }
        return calib
    }

    fun scanForQuestionnaire(questName: String): Boolean {

        // Scan quest folder (not the nicest way)
        try {
            Runtime.getRuntime().exec("am broadcast -a android.intent.action.MEDIA_MOUNTED -d file:///sdcard/IHAB/quest")
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // Obtain working Directory
        val dir = File(folderPath + File.separator + FOLDER_QUEST)
        if (!dir.exists()) dir.mkdirs()
        val files = dir.listFiles { _, name -> name.lowercase(Locale.getDefault()).endsWith(FORMAT_QUESTIONNAIRE) }
        if (files != null) {
            for (iFile in files.indices) {
                if (files[iFile]!!.name == questName) {
                    //Log.e(LOG, "Quest file found: " + files[iFile].getName());
                    return true
                }
            }
        }
        return false
    }

    // Offline version reads XML Basis Sheet from Raw Folder
    fun readRawTextFile(ctx: Context?, resId: Int): String? {
        val inputStream = ctx!!.resources.openRawResource(resId)
        val inputReader = InputStreamReader(inputStream)
        val buffReader = BufferedReader(inputReader)
        var line: String?
        val text = StringBuilder()
        var isComment = false
        try {
            while (buffReader.readLine().also { line = it } != null) {
                if (line!!.trim { it <= ' ' }.startsWith("/*")) {
                    isComment = true
                }
                if (!line!!.trim { it <= ' ' }.isEmpty() && !line!!.trim { it <= ' ' }.startsWith("//") && !isComment) {
                    text.append(line)
                    text.append('\n')
                } else {
                    if (isVerbose) {
                        Log.i(LOG, "Dropping line: " + line!!.trim { it <= ' ' })
                    }
                }
                if (!line!!.trim { it <= ' ' }.startsWith("//") && line!!.split(" //").toTypedArray().size > 1) {
                    text.append(line!!.split(" //").toTypedArray()[0].trim { it <= ' ' })
                    if (isVerbose) {
                        Log.i(LOG, "Dropping part: " + line!!.split(" //").toTypedArray()[1].trim { it <= ' ' })
                    }
                }
                if (line!!.trim { it <= ' ' }.endsWith("*/")) {
                    isComment = false
                }
            }
        } catch (e: IOException) {
            return null
        }
        return text.toString()
    }

    // Scan "quest" directory for present questionnaires
    fun scanQuestOptions(): Array<String>? {

        // Scan quest folder (not the nicest way)
        try {
            Runtime.getRuntime().exec("am broadcast -a android.intent.action.MEDIA_MOUNTED -d file:///sdcard/IHAB/quest")
        } catch (e: IOException) {
            e.printStackTrace()
        }

        //TODO: Validate files
        // Obtain working Directory
        val dir = File(folderPath + File.separator + FOLDER_QUEST)
        // Temporary file targeted by MediaScanner
        val tmp = File(folderPath + File.separator + FOLDER_QUEST + File.separator + FILE_TEMP)
        if (!dir.exists()) {
            dir.mkdirs()
            try {
                tmp.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            //new SingleMediaScanner(mContext, tmp);
            //File fileLog = new File(getFolderPath() + File.separator + ControlService.FILENAME_LOG);
            //new SingleMediaScanner(mContext, fileLog);
        }

        // Scan for files of type XML
        val files = dir.listFiles { _, name -> name.lowercase(Locale.getDefault()).endsWith(FORMAT_QUESTIONNAIRE) }
        return files?.map{ it.name }?.toTypedArray()
    }

    // Online with dynamic filename
    fun readRawTextFile(fileName: String): String? {
        return try {
            // Obtain working Directory
            val dir = File("$folderPath/$FOLDER_QUEST")
            // Address Basis File in working Directory
            val file = File(dir, fileName)
            val inputStream = FileInputStream(file)
            val inputReader = InputStreamReader(inputStream)
            val buffReader = BufferedReader(inputReader)
            var line: String?
            val text = StringBuilder()
            var isComment = false
            try {
                while (buffReader.readLine().also { line = it } != null) {
                    if (line!!.trim { it <= ' ' }.startsWith("/*")) {
                        isComment = true
                    }
                    if (!line!!.trim { it <= ' ' }.isEmpty() && !line!!.trim { it <= ' ' }.startsWith("//") && !isComment) {
                        text.append(line)
                        text.append('\n')
                    } else {
                        if (isVerbose) {
                            Log.i(LOG, "Dropping line: " + line!!.trim { it <= ' ' })
                        }
                    }
                    if (!line!!.trim { it <= ' ' }.startsWith("//") && line!!.split(" //").toTypedArray().size > 1) {
                        text.append(line!!.split(" //").toTypedArray()[0].trim { it <= ' ' })
                        if (isVerbose) {
                            Log.i(LOG, "Dropping part: " + line!!.split(" //").toTypedArray()[1].trim { it <= ' ' })
                        }
                    }
                    if (line!!.trim { it <= ' ' }.endsWith("*/")) {
                        isComment = false
                    }
                }
            } catch (e: IOException) {
                return null
            }
            inputStream.close()
            text.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Online with predefined filename
    fun readRawTextFile(): String? {
        return try {
            // Obtain working Directory
            val dir = File("$folderPath/$FOLDER_QUEST")
            // Address Basis File in working Directory
            val file = File(dir, FILE_NAME)
            val inputStream = FileInputStream(file)
            val inputReader = InputStreamReader(inputStream)
            val buffReader = BufferedReader(inputReader)
            var line: String?
            val text = StringBuilder()
            var isComment = false
            try {
                while (buffReader.readLine().also { line = it } != null) {
                    if (line!!.trim { it <= ' ' }.startsWith("/*")) {
                        isComment = true
                    }
                    if (!line!!.trim { it <= ' ' }.isEmpty() && !line!!.trim { it <= ' ' }.startsWith("//") && !isComment) {
                        text.append(line)
                        text.append('\n')
                    } else {
                        if (isVerbose) {
                            Log.i(LOG, "Dropping line: " + line!!.trim { it <= ' ' })
                        }
                    }
                    if (!line!!.trim { it <= ' ' }.startsWith("//") && line!!.split(" //").toTypedArray().size > 1) {
                        text.append(line!!.split(" //").toTypedArray()[0].trim { it <= ' ' })
                        if (isVerbose) {
                            Log.i(LOG, "Dropping part: " + line!!.split(" //").toTypedArray()[1].trim { it <= ' ' })
                        }
                    }
                    if (line!!.trim { it <= ' ' }.endsWith("*/")) {
                        isComment = false
                    }
                }
            } catch (e: IOException) {
                return null
            }
            inputStream.close()
            text.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveDataToFile(context: Context?, filename: String, data: String): File? {

        //MediaScannerConnection mMs = new MediaScannerConnection(context, this);
        //mMs.connect();

        // Obtain working Directory
        val dir = File("$folderPath/$FOLDER_DATA/")
        // Address Basis File in working Directory
        val file = File(dir, filename)
        Log.e(LOG, "" + dir)

        // Make sure the path directory exists.
        if (!dir.exists()) {
            dir.mkdirs()
            if (BuildConfig.DEBUG) {
                Log.i(LOG, "Directory created: $dir")
            }
        }
        if (BuildConfig.DEBUG) {
            Log.e(LOG, "writing to File: " + file.absolutePath)
        }
        try {
            if (!file.exists()) {
                file.createNewFile()
            }
            val fOut = FileOutputStream(file)
            val myOutWriter = OutputStreamWriter(fOut)
            myOutWriter.append(data)
            myOutWriter.close()
            fOut.flush()
            fOut.close()
            SingleMediaScanner(context, file)
            if (BuildConfig.DEBUG) {
                Log.i(LOG, "Data successfully written.")
            }
            return file
        } catch (e: IOException) {
            if (BuildConfig.DEBUG) {
                Log.e("Exception", "File write failed: $e")
            }
        }
        return null
    }

    fun saveDataToFileOffline(context: Context, filename: String, data: String): Boolean {

        // Obtain working Directory
        val dir = File("C:/Users/ul1021/Desktop/data")
        // Address Basis File in working Directory
        val file = File(dir, filename)
        Log.i(LOG, file.absolutePath)

        // Make sure the path directory exists.
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                if (BuildConfig.DEBUG) {
                    Log.i(LOG, "Directory created: $dir")
                }
                if (BuildConfig.DEBUG) {
                    Log.i(LOG, "writing to File: " + file.absolutePath)
                }
                try {
                    if (!file.exists()) {
                        file.createNewFile()
                    }
                    val fOut = FileOutputStream(file)
                    val myOutWriter = OutputStreamWriter(fOut)
                    myOutWriter.append(data)
                    myOutWriter.close()
                    fOut.flush()
                    fOut.close()
                    SingleMediaScanner(context, file)
                    if (BuildConfig.DEBUG) {
                        Log.i(LOG, "Data successfully written.")
                    }
                    return true
                } catch (e: IOException) {
                    if (BuildConfig.DEBUG) {
                        Log.e("Exception", "File write failed: $e")
                    }
                }
            } else {
                if (BuildConfig.DEBUG) {
                    Log.e(LOG, "Unable to create directory. Shutting down.")
                }
            }
        }
        return false
    }

    companion object {
        val FOLDER_MAIN: String = "olMEGA"
        private val FOLDER_DATA: String = "data"
        private val FOLDER_QUEST: String = "quest"
        private val FOLDER_CALIB: String = "calibration"
        private val FILE_NAME: String = "questionnairecheckboxgroup.xml"
        private val LOG: String = "FileIO"

        // File the system looks for in order to show preferences, needs to be in main directory
        private val FILE_CONFIG: String = "config"
        private val FORMAT_QUESTIONNAIRE: String = ".xml"
        private val FILE_TEMP: String = "copy_questionnaire_here"

        // Create / Find main Folder
        val folderPath: String
            get() {
                val baseDirectory: File = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) File(
                    Objects.requireNonNull(
                        MainActivity.appContext
                    )!!.getExternalFilesDir(null).toString() + File.separator + ".") else File(Environment.getExternalStorageDirectory().toString() + File.separator + FOLDER_MAIN)
                if (!baseDirectory.exists()) {
                    baseDirectory.mkdirs()
                }
                return baseDirectory.absolutePath
            }
    }
}