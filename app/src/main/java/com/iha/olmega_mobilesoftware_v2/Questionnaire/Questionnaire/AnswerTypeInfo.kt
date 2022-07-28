package com.iha.olmega_mobilesoftware_v2.Questionnaire.Questionnaire

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.iha.olmega_mobilesoftware_v2.R

/**
 * Created by ulrikkowalk on 17.02.17.
 */
class AnswerTypeInfo(private val mContext: Context?, private val mQuestionnaire: Questionnaire?, private val parent: AnswerLayout?) : AppCompatActivity() {
    private val mAnswerButton: Button?
    private val answerParams: LinearLayout.LayoutParams?
    fun addAnswer(): Boolean {
        parent!!.layoutAnswer!!.addView(mAnswerButton, answerParams)
        return true
    }

    fun addClickListener() {
        mAnswerButton!!.setOnClickListener { mQuestionnaire!!.finaliseEvaluation() }
    }

    init {
        mAnswerButton = Button(mContext)
        mAnswerButton.setText(R.string.buttonTextFinish)
        mAnswerButton.setTextSize(mContext!!.resources.getDimension(R.dimen.textSizeAnswer))
        mAnswerButton.setGravity(Gravity.CENTER_HORIZONTAL)
        mAnswerButton.setTextColor(ContextCompat.getColor(mContext, R.color.TextColor))
        mAnswerButton.setBackgroundColor(ContextCompat.getColor(mContext, R.color.BackgroundColor))
        mAnswerButton.setAllCaps(false)
        mAnswerButton.setTypeface(null, Typeface.NORMAL)
        mAnswerButton.setBackgroundResource(R.drawable.button)

        // Parameters of Answer Button
        answerParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        answerParams.setMargins(mContext.resources.getDimension(R.dimen.answerFinishMargin_Left).toInt(), mContext.resources.getDimension(R.dimen.answerFinishMargin_Top).toInt(), mContext.resources.getDimension(R.dimen.answerFinishMargin_Right).toInt(), mContext.resources.getDimension(R.dimen.answerFinishMargin_Bottom).toInt())
    }
}