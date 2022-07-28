package com.iha.olmega_mobilesoftware_v2.Questionnaire.Questionnaire

import android.content.Context
import android.provider.Settings.Secure
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.iha.olmega_mobilesoftware_v2.Questionnaire.QuestionnaireActivity
import com.iha.olmega_mobilesoftware_v2.R

/**
 * Created by ulrikkowalk on 17.02.17.
 */
class AnswerTypeText(context: Context?, questionnaire: Questionnaire?, qParent: AnswerLayout?,
                     nQuestionId: Int, isImmersive: Boolean) : AnswerType(context, questionnaire, qParent, nQuestionId) {
    private val LOG_STRING: String? = "AnswerTypeText"
    var mAnswerText: EditText? = null
    var answerParams: LinearLayout.LayoutParams? = null
    private var mButtonOkay: Button? = null
    private var isSystem = false
    fun addQuestion(sAnswer: String?) {
        when (sAnswer) {
            "\$device.id" -> {
                isSystem = true
                mQuestionnaire!!.addTextToEvaluationLst(mQuestionId, generateDeviceId())
            }
        }
    }

    override fun buildView() {
        if (!isSystem) {
            mAnswerText = EditText(mContext)
            mAnswerText!!.setRawInputType(InputType.TYPE_CLASS_TEXT)
            mAnswerText!!.textSize = mContext!!.resources.getDimension(R.dimen.textSizeAnswer)
            mAnswerText!!.gravity = Gravity.START
            mAnswerText!!.setTextColor(ContextCompat.getColor(mContext, R.color.TextColor))
            mAnswerText!!.setBackgroundColor(ContextCompat.getColor(mContext, R.color.BackgroundColor))
            mAnswerText!!.setHint(R.string.hintTextAnswer)
            mAnswerText!!.setHintTextColor(ContextCompat.getColor(mContext, R.color.JadeGray))

            // Parameters of Answer Button Layout
            answerParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT)
            answerParams!!.setMargins(mContext.resources.getDimension(R.dimen.answerTextMargin_Left).toInt(), mContext.resources.getDimension(R.dimen.answerTextMargin_Top).toInt(), mContext.resources.getDimension(R.dimen.answerTextMargin_Right).toInt(), mContext.resources.getDimension(R.dimen.answerTextMargin_Bottom).toInt())
            mButtonOkay = Button(mContext)
            mButtonOkay!!.setText(R.string.buttonTextOkay)
            mButtonOkay!!.scaleX = 1.5f
            mButtonOkay!!.scaleY = 1.5f
            mButtonOkay!!.setTextColor(ContextCompat.getColor(mContext, R.color.TextColor))
            mButtonOkay!!.background = ContextCompat.getDrawable(mContext, R.drawable.button)
            val buttonParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            )
            buttonParams.topMargin = 96
            buttonParams.bottomMargin = 48
            mAnswerText!!.isFocusableInTouchMode
            mParent!!.layoutAnswer!!.addView(mAnswerText, answerParams)
            mParent.layoutAnswer!!.addView(mButtonOkay, buttonParams)
        }
    }

    override fun addClickListener() {
        if (!isSystem) {
            mButtonOkay!!.setOnClickListener { // Check if no view has focus, then hide soft keyboard:
                val view: View? = mAnswerText
                if (view != null) {
                    val imm = mAnswerText!!.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(mAnswerText!!.windowToken, 0)
                    mAnswerText!!.isCursorVisible = false
                }
                val text = mAnswerText!!.text.toString()
                if (text.length != 0) {
                    mQuestionnaire!!.removeQuestionIdFromEvaluationList(mQuestionId)
                    mQuestionnaire.addTextToEvaluationLst(mQuestionId, text)
                } else {
                    Log.e(LOG_STRING, "No text was entered.")
                }
                (mContext as QuestionnaireActivity).hideSystemUI(isImmersive)
                mContext.incrementPage()
            }
        }
    }

    private fun generateDeviceId(): String? {
        return Secure.getString(mContext!!.contentResolver, Secure.ANDROID_ID)
    }

    init {
        this.isImmersive = isImmersive
    }
}