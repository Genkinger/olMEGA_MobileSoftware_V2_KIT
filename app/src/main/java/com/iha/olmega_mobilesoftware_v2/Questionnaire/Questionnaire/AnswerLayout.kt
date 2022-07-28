package com.iha.olmega_mobilesoftware_v2.Questionnaire.Questionnaire

import android.content.Context
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.iha.olmega_mobilesoftware_v2.R

/**
 * Created by ulrikkowalk on 17.02.17.
 */
class AnswerLayout(private val mContext: Context?) : AppCompatActivity() {
    val layoutAnswer: LinearLayout?
    val scrollContent: ScrollView?

    init {
        // Main Layout has to be incorporated in ScrollView for Overflow Handling
        scrollContent = ScrollView(mContext)
        scrollContent.setBackgroundColor(ContextCompat.getColor(mContext!!, R.color.BackgroundColor))
        scrollContent.setLayoutParams(LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT))
        scrollContent.setId(0)

        // Main Layout - Right now Framework carrying ONE Question
        layoutAnswer = LinearLayout(mContext)
        layoutAnswer.setPadding(mContext.resources.getDimension(R.dimen.answerLayoutPadding_Left).toInt(), mContext.resources.getDimension(R.dimen.answerLayoutPadding_Top).toInt(), mContext.resources.getDimension(R.dimen.answerLayoutPadding_Right).toInt(), mContext.resources.getDimension(R.dimen.answerLayoutPadding_Bottom).toInt())
        layoutAnswer.setOrientation(LinearLayout.VERTICAL)
        layoutAnswer.setBackgroundColor(ContextCompat.getColor(mContext, R.color.BackgroundColor))
        layoutAnswer.setGravity(Gravity.CENTER_HORIZONTAL)
        layoutAnswer.setLayoutParams(LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT))

        // Linear Layout is Child to ScrollView (must always be)
        scrollContent.addView(layoutAnswer)
    }
}