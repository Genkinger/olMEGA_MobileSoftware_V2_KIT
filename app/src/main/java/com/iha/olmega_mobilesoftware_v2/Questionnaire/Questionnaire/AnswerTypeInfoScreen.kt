package com.iha.olmega_mobilesoftware_v2.Questionnaire.Questionnaire

import android.content.Context
import android.graphics.text.LineBreaker
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.iha.olmega_mobilesoftware_v2.R

/**
 * Created by ulrikkowalk on 17.02.17.
 */
class AnswerTypeInfoScreen(private val mContext: Context?, private val mQuestionnaire: Questionnaire?, private val parent: AnswerLayout?) : AppCompatActivity() {
    private val mAnswerButton: Button? = null
    private val answerParams: LinearLayout.LayoutParams?
    private val mInfoText: TextView?
    private val LOG: String? = "AnswerTypeInfoScreen"
    fun addAnswer(sAnswer: String?): Boolean {
        val tmpArray: Array<String?> = sAnswer!!.split("<br/>").toTypedArray()
        var tmp: String? = ""
        for (iLine in 0 until tmpArray.size - 1) {
            tmp += tmpArray[iLine]
            tmp += System.getProperty("line.separator")
        }
        tmp += tmpArray[tmpArray.size - 1]
        mInfoText!!.text = tmp
        mInfoText.setTextColor(mContext!!.resources.getColor(R.color.TextColor))
        mInfoText.justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
        mInfoText.textSize = mContext.resources.getDimension(R.dimen.textSizeAnswer)
        parent!!.layoutAnswer!!.addView(mInfoText)
        return true
    }

    fun addClickListener() {}

    init {
        mInfoText = TextView(mContext)


        // Parameters of Answer Button
        answerParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        answerParams.setMargins(mContext!!.resources.getDimension(R.dimen.answerFinishMargin_Left).toInt(), mContext.resources.getDimension(R.dimen.answerFinishMargin_Top).toInt(), mContext.resources.getDimension(R.dimen.answerFinishMargin_Right).toInt(), mContext.resources.getDimension(R.dimen.answerFinishMargin_Bottom).toInt())
    }
}