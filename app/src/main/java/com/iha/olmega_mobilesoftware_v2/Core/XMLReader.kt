package com.iha.olmega_mobilesoftware_v2.Core

import android.content.Context
import android.util.Log
import java.util.*
import java.util.concurrent.ThreadLocalRandom

/**
 * Central class for XML sheet info extraction tasks
 */
class XMLReader(private val mContext: Context, fileName: String) {
    private val mFileIO: FileIO?
    val head: String?
    val foot: String?
    val surveyURI: String?
    private val KEY_NEW_LINE: String?
    private var mTimerMean = 0
    private var mTimerDeviation = 0
    private var mTimerInterval = 0

    // List containing all questions (including attached information)
    var questionList: ArrayList<String>?
    private val nDefaultTimerMean = 30
    private val nDefaultTimerDeviation = 5
    private val nSecondsInMinute = 60
    private var mDateList: ArrayList<String>? = null
    private var mTimerLayout: String? = ""
    private fun extractHead(rawInput: String?): String? {
        var head: String? = ""
        val tempHead: Array<String?> = rawInput!!.split("<|>").toTypedArray()
        head += "<"
        head += tempHead[1]
        head += ">"
        head += KEY_NEW_LINE
        head += "<"
        head += tempHead[3]
        head += ">"
        return head
    }

    /*private String extractSurveyURI(String rawInput) {
        return rawInput.split("<survey uri=\"")[1].split("\">")[0];
    }*/
    private fun extractSurveyURI(inString: String?): String? {
        return inString
    }

    private fun extractFoot(rawInput: String?): String? {
        val rawInputLines: Array<String?> = rawInput!!.split("\n").toTypedArray()
        return rawInputLines[rawInputLines.size - 1]
    }

    val newTimerInterval: Int
        get() {
            if (mTimerLayout.equals("single", ignoreCase = true)) {
                mTimerInterval = ThreadLocalRandom.current().nextInt(
                        mTimerMean - mTimerDeviation,
                        mTimerMean + mTimerDeviation + 1)
            } else if (mTimerLayout.equals("multi", ignoreCase = true)) {
                val tmp_date = Date()
                val tmp_dateTime = tmp_date.hours * 60 * 60 + tmp_date.minutes * 60 +
                        tmp_date.seconds
                var tmp_result: Int
                var nextTimer = 0
                for (iDate in mDateList!!.indices) {
                    val tmp_hours = mDateList!![iDate].split(":").toTypedArray()[0].toInt()
                    val tmp_minutes = mDateList!![iDate].split(":").toTypedArray()[1].toInt()
                    val tmp_time = tmp_hours * 60 * 60 + tmp_minutes * 60
                    tmp_result = if (tmp_time > tmp_dateTime) +1 else if (tmp_time < tmp_dateTime) -1 else 0
                    if (tmp_result == 1) {
                        nextTimer = iDate
                        break
                    }
                }
                mTimerInterval = mDateList!![nextTimer].split(":").toTypedArray()[0].toInt() * 60 * 60 +
                        mDateList!![nextTimer].split(":").toTypedArray()[1].toInt() * 60 -
                        tmp_dateTime
                if (mTimerInterval <= 0) {
                    mTimerInterval += +24 * 60 * 60 + 60 * 60
                }
            }
            return mTimerInterval
        }
    val questionnaireHasTimer: Boolean
        get() = mTimerMean != 0

    private fun thinOutList(mQuestionList: ArrayList<String>?): ArrayList<String>? {
        // Removes irrelevant data from question sheet
        var iItem = mQuestionList!!.size - 1
        while (iItem >= 0) {
            mQuestionList.removeAt(iItem)
            iItem = iItem - 2
        }
        return mQuestionList
    }

    private fun stringArrayToListString(stringArray: Array<String>?): ArrayList<String>? {
        // Turns an array of Strings into a List of Strings
        val listString = ArrayList<String>()
        Collections.addAll(listString, *stringArray!!)
        return listString
    }

    companion object {
        private val LOG: String = "XMLReader"
    }

    init {
        mFileIO = FileIO()
        questionList = ArrayList()
        val rawInput = mFileIO.readRawTextFile(fileName)
        KEY_NEW_LINE = "\n"
        val timerTemp: Array<String?> = rawInput!!.split("<timer|</timer>").toTypedArray()

        // timerTemp.length == 0 means no timer information can be found
        if (timerTemp.size > 1) {
            if (timerTemp[1]!!.split("mean").toTypedArray().size > 1) {
                try {
                    mTimerMean = timerTemp[1]!!.split("\"").toTypedArray()[1].toInt()
                } catch (e: Exception) {
                    mTimerMean = nDefaultTimerMean * nSecondsInMinute
                    Log.e(LOG, "Invalid entry. Timer mean set to $mTimerMean seconds.")
                }
                mTimerLayout = "single"
            }
            if (timerTemp[1]!!.split("deviation").toTypedArray().size > 1) {
                try {
                    mTimerDeviation = timerTemp[1]!!.split("\"").toTypedArray()[3].toInt()
                } catch (e: Exception) {
                    mTimerDeviation = nDefaultTimerDeviation * nSecondsInMinute
                    Log.e(LOG, "Invalid entry. Timer mean set to 300 seconds.")
                }
                mTimerLayout = "single"
            }
            if (timerTemp[1]!!.split("date").toTypedArray().size > 1) {
                try {
                    mDateList = ArrayList()
                    val tmp_entries: Array<String?> = timerTemp[1]!!.split("\"").toTypedArray()[1].split(";").toTypedArray()
                    // Sort list
                    Arrays.sort(tmp_entries, 1, tmp_entries.size)
                    if (tmp_entries.size > 1) {
                        for (iDate in tmp_entries.indices) {
                            if (tmp_entries[iDate]!!.split(":").toTypedArray()[0].toInt() > 23 ||
                                    tmp_entries[iDate]!!.split(":").toTypedArray()[1].toInt() > 59) {
                                Log.e(LOG, "Invalid entry: " + tmp_entries[iDate])
                            } else {
                                Log.e(LOG, "Entry added: " + tmp_entries[iDate])
                                mDateList!!.add(tmp_entries[iDate]!!)
                            }
                        }
                        mTimerMean = -255
                    } else {
                        mTimerMean = 0
                        mTimerDeviation = 0
                    }
                } catch (e: Exception) {
                    Log.e(LOG, "Invalid date specified.")
                }
                mTimerLayout = "multi"
            }
        } else {
            mTimerMean = 0
            mTimerDeviation = 0
        }

        // Split basis data into question segments
        val questionnaire: Array<String> = rawInput.split("<question|</question>|<finish>|</finish>").toTypedArray()
        head = extractHead(rawInput)
        foot = extractFoot(rawInput)
        surveyURI = extractSurveyURI(fileName)
        questionList = stringArrayToListString(questionnaire)
        questionList = thinOutList(questionList)
    }
}