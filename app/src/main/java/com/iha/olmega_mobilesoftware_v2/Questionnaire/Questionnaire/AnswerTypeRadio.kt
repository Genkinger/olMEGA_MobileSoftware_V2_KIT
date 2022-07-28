package com.iha.olmega_mobilesoftware_v2.Questionnaire.Questionnaire

import android.content.*
import android.content.res.ColorStateList
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.widget.CompoundButtonCompat
import com.iha.olmega_mobilesoftware_v2.Questionnaire.DataTypes.StringAndInteger
import com.iha.olmega_mobilesoftware_v2.R

/**
 * Created by ulrikkowalk on 17.02.17.
 */
class AnswerTypeRadio(context: Context?, questionnaire: Questionnaire?, qParent: AnswerLayout?, nQuestionId: Int) : AnswerType(context, questionnaire, qParent, nQuestionId) {
    private val mRadioGroup: RadioGroup?

    /*private final AnswerLayout mParent;
    private final Context mContext;
    private final Questionnaire mQuestionnaire;
    private final int mQuestionId;
    private final List<StringAndInteger> mListOfAnswers;*/
    private var mDefault = -1
    fun addAnswer(nAnswerId: Int, sAnswer: String?, isDefault: Boolean) {
        mListOfAnswers.add(StringAndInteger(sAnswer, nAnswerId))
        if (isDefault) {
            mDefault = mListOfAnswers.size - 1
        }
    }

    override fun buildView() {
        for (iAnswer in mListOfAnswers.indices) {
            val button = RadioButton(mContext)
            button.id = mListOfAnswers[iAnswer].id
            button.text = mListOfAnswers[iAnswer].text
            button.textSize = mContext!!.resources.getDimension(R.dimen.textSizeAnswer)
            button.isChecked = false
            button.gravity = Gravity.CENTER_VERTICAL
            button.setTextColor(ContextCompat.getColor(mContext, R.color.TextColor))
            button.setBackgroundColor(ContextCompat.getColor(mContext, R.color.BackgroundColor))
            val states = arrayOf<IntArray?>(intArrayOf(android.R.attr.state_checked), intArrayOf())
            val colors = intArrayOf(ContextCompat.getColor(mContext, R.color.JadeRed),
                    ContextCompat.getColor(mContext, R.color.JadeRed))
            CompoundButtonCompat.setButtonTintList(button, ColorStateList(states, colors))
            button.minHeight = mContext.resources.getDimension(R.dimen.radioMinHeight).toInt()
            button.setPadding(24, 24, 24, 24)
            if (iAnswer == mDefault) {
                button.isChecked = true
                mQuestionnaire!!.addIdToEvaluationList(mQuestionId, mListOfAnswers[mDefault].id)
            }

            // Parameters of Answer Button
            val answerParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
            if (mListOfAnswers[iAnswer].id == 66666) {
                button.isEnabled = false
                button.visibility = View.INVISIBLE
            }
            mRadioGroup!!.addView(button, answerParams)
        }
        mParent!!.layoutAnswer!!.addView(mRadioGroup)
    }

    override fun addClickListener() {
        mRadioGroup!!.setOnCheckedChangeListener { group, checkedId -> // In Case of Radio Buttons checking one means un-checking all other Elements
            // Therefore onClickListening must be handled on Group Level
            // listOfRadioIds contains all Ids of current Radio Group
            mQuestionnaire!!.removeQuestionIdFromEvaluationList(mQuestionId)
            mQuestionnaire.addIdToEvaluationList(mQuestionId, checkedId)
            mRadioGroup.check(checkedId)

            // Toggle Visibility of suited/unsuited frames
            mQuestionnaire.checkVisibility()
        }
    }

    companion object {
        private val LOG_STRING: String? = "AnswerTypeRadio"
    }

    init {

        //mContext = context;
        //mQuestionnaire = questionnaire;
        //mParent = parent;
        //mQuestionId = Id;

        //mListOfAnswers = new ArrayList<>();

        // Answer Buttons of type "radio" are grouped and handled together
        mRadioGroup = RadioGroup(mContext)
        mRadioGroup.setOrientation(RadioGroup.VERTICAL)
    }
}