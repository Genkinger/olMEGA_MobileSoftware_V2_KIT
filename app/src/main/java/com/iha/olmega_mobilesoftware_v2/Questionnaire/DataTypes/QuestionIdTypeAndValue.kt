package com.iha.olmega_mobilesoftware_v2.Questionnaire.DataTypes

import android.util.Log

/**
 * Created by ulrikkowalk on 09.05.17.
 */
class QuestionIdTypeAndValue(val questionId: Int, val answerType: String?, val value: String?) {
    private val LOG_STRING: String? = "QuestionIdTypeAndValue"
    private val isDebug = false

    init {
        if (isDebug) {
            Log.i(LOG_STRING, "QId: " + questionId + ", Type: " +
                    answerType + ", Value: " + value)
        }
    }
}