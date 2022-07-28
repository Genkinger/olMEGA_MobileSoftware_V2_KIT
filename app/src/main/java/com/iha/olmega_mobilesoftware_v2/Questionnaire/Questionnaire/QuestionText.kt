package com.iha.olmega_mobilesoftware_v2.Questionnaire.Questionnaire

import android.content.Context
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.iha.olmega_mobilesoftware_v2.Questionnaire.Core.Units
import com.iha.olmega_mobilesoftware_v2.Questionnaire.QuestionnaireActivity
import com.iha.olmega_mobilesoftware_v2.R

/**
 * Created by ulrikkowalk on 17.02.17.
 */
class QuestionText(private val mContext: QuestionnaireActivity?, nQuestionId: Int, sQuestion: String?, val parent: LinearLayout?) : AppCompatActivity() {
    val questionTextView: TextView?
    val questionLayoutParams: LinearLayout.LayoutParams?
    val mUnits: Units?
    val mText: String?
    fun addQuestion(): Boolean {
        parent!!.addView(
                questionTextView, questionLayoutParams)
        return true
    }

    val questionHeight: Int
        get() = spToPx(mContext!!.resources.getDimension(R.dimen.textSizeQuestion), mContext) * approximateLineCount(mText)

    fun approximateLineCount(sText: String?): Int {
        return Math.ceil(sText!!.length / 24.0).toInt()
    }

    companion object {
        fun spToPx(sp: Float, context: Context?): Int {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context!!.resources.displayMetrics).toInt()
        }
    }

    init {
        mUnits = Units(mContext)
        mText = sQuestion
        questionTextView = TextView(mContext)
        questionTextView.setId(nQuestionId)
        questionTextView.setTextColor(ContextCompat.getColor(mContext!!, R.color.TextColor))
        questionTextView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.lighterGray))
        questionTextView.setTextSize(mContext.resources.getDimension(R.dimen.textSizeQuestion))
        questionTextView.setText(sQuestion)
        questionTextView.setPadding(mUnits.convertDpToPixels(16f),
                mUnits.convertDpToPixels(8f),
                mUnits.convertDpToPixels(16f),
                mUnits.convertDpToPixels(16f))
        questionLayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        questionTextView.setMinHeight(mContext.resources.getDimension(R.dimen.textSizeQuestion).toInt())
    }
}