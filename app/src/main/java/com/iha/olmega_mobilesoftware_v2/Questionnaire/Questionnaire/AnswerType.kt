package com.iha.olmega_mobilesoftware_v2.Questionnaire.Questionnaire

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.iha.olmega_mobilesoftware_v2.Questionnaire.DataTypes.StringAndInteger

/**
 * Created by ul1021 on 06.02.2018.
 */
abstract class AnswerType(open val mContext: Context?, val mQuestionnaire: Questionnaire?, val mParent: AnswerLayout?, val mQuestionId: Int) : AppCompatActivity() {
    open val mListOfAnswers: MutableList<StringAndInteger> = mutableListOf()
    abstract fun buildView()
    abstract fun addClickListener()

    companion object {
        val LOG: String = "AnswerType"
    }
}