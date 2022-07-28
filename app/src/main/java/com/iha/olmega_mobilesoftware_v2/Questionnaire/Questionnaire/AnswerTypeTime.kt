package com.iha.olmega_mobilesoftware_v2.Questionnaire.Questionnaire

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.iha.olmega_mobilesoftware_v2.Core.LogIHAB
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by ulrikkowalk on 17.02.17.
 */
class AnswerTypeTime(private val mContext: Context?, private val mQuestionnaire: Questionnaire?, private val mQuestionId: Int) : AppCompatActivity() {
    private val DATE_FORMAT: SimpleDateFormat?
    private val LOG: String? = "AnswerTypeTime"
    fun addAnswer(nAnswerId: Int, sAnswer: String?) {
        Log.e(LOG, "TIME ADDED")
        try {
            val first = DATE_FORMAT!!.parse(sAnswer!!.subSequence(0, 5).toString())
            val last = DATE_FORMAT.parse(sAnswer.subSequence(6, 11).toString())
            val test = DATE_FORMAT.parse(generateTimeNow())
            if (test!!.compareTo(first) > 0 && test.compareTo(last) <= 0) {
                mQuestionnaire!!.addIdToEvaluationList(mQuestionId, nAnswerId)
                LogIHAB.log("Time-based decision made in favour of id: $nAnswerId")
            }
        } catch (ex: Exception) {
            LogIHAB.log("Exception caught during AnswerTypeTime: $sAnswer")
        }
    }

    private fun generateTimeNow(): String? {
        val dateTime = Calendar.getInstance(TimeZone.getTimeZone("GMT+1"))
        return DATE_FORMAT!!.format(dateTime.time)
    }

    private fun generateTimeNowUTC(): String? {
        val dateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        return DATE_FORMAT!!.format(dateTime.time)
    }

    init {
        DATE_FORMAT = SimpleDateFormat("HH:mm", Locale.ROOT)
        Log.e(LOG, "TIME INIT")
    }
}