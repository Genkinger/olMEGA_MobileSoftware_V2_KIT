package com.iha.olmega_mobilesoftware_v2.Questionnaire.Questionnaire

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by ul1021 on 17.05.2017.
 */
class AnswerTypeDate(private val mContext: Context?, private val mQuestionnaire: Questionnaire?, private val mQuestionId: Int) : AppCompatActivity() {
    private val DATE_FORMAT: SimpleDateFormat?
    private val LOG_STRING: String? = "AnswerTypeDate"
    private var mString: String? = ""
    fun addAnswer(sAnswer: String?): Boolean {
        when (sAnswer) {
            "\$utcnow" -> mString = generateTimeNowUTC()
            "\$now" -> mString = generateTimeNow()
        }
        mQuestionnaire!!.addTextToEvaluationLst(mQuestionId, mString)
        return true
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
        DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT)
    }
}