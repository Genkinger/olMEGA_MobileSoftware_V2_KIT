package com.iha.olmega_mobilesoftware_v2.Questionnaire.Core

import android.content.Context
import android.provider.Settings.Secure
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.iha.olmega_mobilesoftware_v2.Questionnaire.Questionnaire.Question
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by ulrikkowalk on 14.03.17.
 */
class MetaData(private val mContext: Context?, private val KEY_HEAD: String?, private val KEY_FOOT: String?, private val KEY_SURVEY_URI: String?, private val KEY_MOTIVATION: String?,
               private val KEY_VERSION: String?) : AppCompatActivity() {
    private var deviceId: String? = null
    private var startDate: String? = null
    private var startDateUTC: String? = null
    private var endDate: String? = null
    private var endDateUTC: String? = null
    private val KEY_TAG_CLOSE: String?
    private val KEY_VALUE_OPEN: String?
    private val KEY_VALUE_CLOSE: String?
    private val KEY_RECORD_OPEN: String?
    private val KEY_RECORD_CLOSE: String?
    var data: String? = null
        private set
    private var questId: String? = null
    var fileName: String? = null
        private set
    private val KEY_NEW_LINE: String?
    private val KEY_SHORT_CLOSE: String?
    private val DATE_FORMAT: SimpleDateFormat?
    private val DATE_FORMAT_FILENAME: SimpleDateFormat?
    private var mTimeQuery = 0
    private var mTimeQueryUTC = 0
    private val mQuestionList: ArrayList<Question?>?
    private var mEvaluationList: EvaluationList? = null
    fun initialise(): Boolean {
        // Obtain Device Id
        deviceId = generateDeviceId()
        // Obtain current Time Stamp at the Beginning of Questionnaire
        startDate = generateTimeNow()
        // Obtain current UTC Time Stamp at the Beginning of Questionnaire
        startDateUTC = generateTimeNowUTC()
        questId = generateQuestId()
        fileName = generateFileName()
        return true
    }

    fun finalise(evaluationList: EvaluationList?): Boolean {
        mEvaluationList = evaluationList
        // Obtain current Time Stamp at the End of Questionnaire
        endDate = generateTimeNow()
        // Obtain current UTC Time Stamp at the End of Questionnaire
        endDateUTC = generateTimeNowUTC()
        collectData()
        return true
    }

    // List of questions according to questionnaire - needed to account for unanswered questions
    fun addQuestion(question: Question?) {
        mQuestionList!!.add(question)
    }

    private fun collectData() {
        var questionId = -255

        /* Information about Questionnaire */data = KEY_HEAD
        data += KEY_NEW_LINE
        data += KEY_MOTIVATION
        data += KEY_NEW_LINE
        data += KEY_RECORD_OPEN
        data += " uri=\""
        data += KEY_SURVEY_URI!!.substring(0, KEY_SURVEY_URI.length - 4) // loose ".xml"
        data += "/"
        data += fileName
        data += "\""
        data += KEY_NEW_LINE
        data += " survey_uri=\""
        data += KEY_SURVEY_URI
        data += "\""
        data += KEY_TAG_CLOSE
        data += KEY_NEW_LINE

        /* Device ID */data += KEY_VALUE_OPEN
        data += "device_id=\""
        data += deviceId
        data += "\""
        data += KEY_SHORT_CLOSE
        data += KEY_NEW_LINE

        /* Start Date */data += KEY_VALUE_OPEN
        data += "start_date=\""
        data += startDate
        data += "\""
        data += KEY_SHORT_CLOSE
        data += KEY_NEW_LINE

        /* Start Date UTC */data += KEY_VALUE_OPEN
        data += "start_date_UTC=\""
        data += startDateUTC
        data += "\""
        data += KEY_SHORT_CLOSE
        data += KEY_NEW_LINE

        /* App version */data += KEY_VALUE_OPEN
        data += "app_version=\""
        data += KEY_VERSION
        data += "\""
        data += KEY_SHORT_CLOSE
        data += KEY_NEW_LINE

        /* Questionnaire Results */for (iQuestion in mQuestionList!!.indices) {
            questionId = mQuestionList[iQuestion]!!.questionId
            if (questionId != 99999) {
                data += KEY_VALUE_OPEN
                data += "question_id=\""
                data += questionId
                data += "\""
                var ANSWER_DATA: String? = ""
                when (mEvaluationList!!.getAnswerTypeFromQuestionId(questionId)) {
                    "none" -> ANSWER_DATA += "/>"
                    "text" -> {
                        ANSWER_DATA += " option_ids=\""
                        ANSWER_DATA += mEvaluationList!!.getTextFromQuestionId(questionId)
                        ANSWER_DATA += "\"/>"
                    }
                    "id" -> {
                        val listOfIds = mEvaluationList!!.getCheckedAnswerIdsFromQuestionId(questionId)
                        Log.e(LOG, "id: " + questionId + " num: " + listOfIds!!.size)
                        ANSWER_DATA += " option_ids=\""
                        ANSWER_DATA += listOfIds[0]
                        if (listOfIds.size > 1) {
                            var iId = 1
                            while (iId < listOfIds.size) {
                                ANSWER_DATA += ";"
                                ANSWER_DATA += listOfIds[iId]
                                iId++
                            }
                        }
                        ANSWER_DATA += "\"/>"
                    }
                    "value" -> {
                        ANSWER_DATA += " option_ids=\""
                        ANSWER_DATA += mEvaluationList!!.getValueFromQuestionId(questionId)
                        ANSWER_DATA += "\"/>"
                    }
                    else -> Log.e(LOG, "Unknown element found during evaluation: " +
                            mEvaluationList!!.getAnswerTypeFromQuestionId(questionId))
                }
                data += ANSWER_DATA
                data += KEY_NEW_LINE
            }
        }

        /* End Date */data += KEY_VALUE_OPEN
        data += "end_date=\""
        data += endDate
        data += "\""
        data += KEY_SHORT_CLOSE
        data += KEY_NEW_LINE

        /* End Date UTC */data += KEY_VALUE_OPEN
        data += "end_date_UTC=\""
        data += endDateUTC
        data += "\""
        data += KEY_SHORT_CLOSE
        data += KEY_NEW_LINE
        data += KEY_RECORD_CLOSE
        data += KEY_NEW_LINE
        data += KEY_FOOT
    }

    private fun generateFileName(): String? {
        return deviceId + "_" + generateTimeNowFilename() + ".xml"
    }

    private fun generateQuestId(): String? {
        return deviceId + "_" + startDateUTC
    }

    private fun generateDeviceId(): String? {
        return Secure.getString(mContext!!.contentResolver, Secure.ANDROID_ID)
    }

    private fun generateTimeNowFilename(): String? {
        val dateTime = Calendar.getInstance(TimeZone.getTimeZone("GMT+1"))
        return DATE_FORMAT_FILENAME!!.format(dateTime.time)
    }

    private fun generateTimeNow(): String? {
        val dateTime = Calendar.getInstance(TimeZone.getTimeZone("GMT+1"))
        return DATE_FORMAT!!.format(dateTime.time)
    }

    private fun generateTimeNowUTC(): String? {
        val dateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        return DATE_FORMAT!!.format(dateTime.time)
    }

    private val timeNow: String?
        private get() = if (mTimeQuery == 0) {
            mTimeQuery++
            startDate
        } else {
            endDate
        }
    private val timeNowUTC: String?
        private get() = if (mTimeQueryUTC == 0) {
            mTimeQueryUTC++
            startDateUTC
        } else {
            endDateUTC
        }

    private fun getTextFromId(id: Int): String? {
        try {
            return mEvaluationList!!.getTextFromQuestionId(id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    companion object {
        private val LOG: String? = "MetaData"
    }

    init {
        mQuestionList = ArrayList()
        DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT)
        DATE_FORMAT_FILENAME = SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.ROOT)
        KEY_RECORD_OPEN = "<record"
        KEY_RECORD_CLOSE = "</record>"
        KEY_TAG_CLOSE = ">"
        KEY_VALUE_OPEN = "<value "
        KEY_VALUE_CLOSE = "</value>"
        KEY_NEW_LINE = "\n"
        KEY_SHORT_CLOSE = "/>"
    }
}