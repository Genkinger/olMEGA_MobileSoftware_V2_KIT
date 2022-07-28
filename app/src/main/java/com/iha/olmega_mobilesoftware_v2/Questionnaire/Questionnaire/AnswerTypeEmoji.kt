package com.iha.olmega_mobilesoftware_v2.Questionnaire.Questionnaire

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.iha.olmega_mobilesoftware_v2.Questionnaire.Core.Units
import com.iha.olmega_mobilesoftware_v2.Questionnaire.DataTypes.StringAndInteger
import com.iha.olmega_mobilesoftware_v2.R

/**
 * Created by ulrikkowalk on 17.02.17.
 */
class AnswerTypeEmoji(context: Context?, questionnaire: Questionnaire?,
                      qParent: AnswerLayout?, questionId: Int, immersive: Boolean) : AnswerType(context, questionnaire, qParent, questionId) {
    //private final Context mContext;
    //private final AnswerLayout mParent;
    //private final List<StringAndInteger> mListOfAnswers;
    private val mListOfIds: MutableList<Int?>?
    private val drawables: IntArray = IntArray(5)
    private val drawables_pressed: IntArray = IntArray(5)

    //private final Questionnaire mQuestionnaire;
    //private final int mQuestionId;
    private val LOG_STRING: String? = "AnswerTypeEmoji"
    private var mDefault = -1
    private val mViewPagerHeight = 0

    init {
        isImmersive = immersive
        //mContext = context;
        //mParent = qParent;
        //mQuestionnaire = questionnaire;
        //mListOfAnswers = new ArrayList<>();
        mListOfIds = ArrayList()
        //mQuestionId = questionId;
        drawables[0] = R.drawable.em1of5
        drawables[1] = R.drawable.em2of5
        drawables[2] = R.drawable.em3of5
        drawables[3] = R.drawable.em4of5
        drawables[4] = R.drawable.em5of5
        drawables_pressed[0] = R.drawable.em1of5_active
        drawables_pressed[1] = R.drawable.em2of5_active
        drawables_pressed[2] = R.drawable.em3of5_active
        drawables_pressed[3] = R.drawable.em4of5_active
        drawables_pressed[4] = R.drawable.em5of5_active
    }

    fun addAnswer(nId: Int, sAnswer: String, isDefault: Boolean): Boolean {
        mListOfAnswers.add(StringAndInteger(sAnswer, nId))
        mListOfIds!!.add(nId)
        if (isDefault) {
            mDefault = mListOfAnswers.size - 1
        }
        return true
    }

    override fun buildView() {
        val usableHeight = Units(mContext).getUsableSliderHeight(isImmersive)
        val numEmojis = mListOfAnswers.size
        // Make size of emojis adaptive
        val emojiSize = (usableHeight / (1.2f * numEmojis)).toInt()
        for (iAnswer in mListOfAnswers.indices) {
            val answerButton = Button(mContext)
            answerButton.layoutParams = LinearLayout.LayoutParams(
                    emojiSize,
                    emojiSize,
                    1.0f)
            val sAnswer = mListOfAnswers[iAnswer].text
            when (sAnswer) {
                "emoji_happy2" -> {
                    answerButton.background = ContextCompat.getDrawable(mContext!!, drawables[0])
                    answerButton.tag = 0
                }
                "emoji_happy1" -> {
                    answerButton.background = ContextCompat.getDrawable(mContext!!, drawables[1])
                    answerButton.tag = 1
                }
                "emoji_neutral" -> {
                    answerButton.background = ContextCompat.getDrawable(mContext!!, drawables[2])
                    answerButton.tag = 2
                }
                "emoji_sad1" -> {
                    answerButton.background = ContextCompat.getDrawable(mContext!!, drawables[3])
                    answerButton.tag = 3
                }
                "emoji_sad2" -> {
                    answerButton.background = ContextCompat.getDrawable(mContext!!, drawables[4])
                    answerButton.tag = 4
                }
                else -> {}
            }
            if (iAnswer == mDefault) {
                setChecked(true, answerButton)
                mQuestionnaire!!.removeQuestionIdFromEvaluationList(mQuestionId)
                mQuestionnaire.addIdToEvaluationList(mQuestionId, mListOfAnswers[iAnswer].id)
            } else {
                setChecked(false, answerButton)
            }
            answerButton.id = mListOfAnswers[iAnswer].id
            mParent!!.layoutAnswer!!.addView(answerButton)

            // Placeholder View because padding has no effect
            val placeHolder = View(mContext)
            placeHolder.setBackgroundColor(ContextCompat.getColor(
                    mContext!!, R.color.BackgroundColor))
            placeHolder.layoutParams = LinearLayout.LayoutParams((0.16f * emojiSize).toInt(), (0.16f * emojiSize).toInt(),
                    1.0f
            )

            // The lowest placeholder is unnecessary
            if (iAnswer < mListOfAnswers.size - 1) {
                mParent.layoutAnswer!!.addView(placeHolder)
            }
        }
    }

    override fun addClickListener() {
        for (iAnswer in mListOfAnswers.indices) {
            val button = mParent!!.layoutAnswer!!.findViewById<View?>(
                    mListOfAnswers[iAnswer].id) as Button
            button.setOnClickListener {
                for (iButton in mListOfAnswers.indices) {
                    val button = mParent.layoutAnswer!!.findViewById<View?>(
                            mListOfAnswers[iButton].id) as Button
                    if (iButton == iAnswer) {
                        setChecked(true, button)
                    } else {
                        setChecked(false, button)
                    }
                }
                mQuestionnaire!!.removeQuestionIdFromEvaluationList(mQuestionId)
                mQuestionnaire.addIdToEvaluationList(mQuestionId, mListOfAnswers[iAnswer].id)
                mQuestionnaire.checkVisibility()
            }
        }
    }

    private fun setChecked(isChecked: Boolean, answerButton: Button?) {
        if (isChecked) {
            answerButton!!.background = ContextCompat.getDrawable(mContext!!,
                    drawables_pressed[answerButton.tag as Int])
        } else {
            answerButton!!.background = ContextCompat.getDrawable(mContext!!,
                    drawables[answerButton.tag as Int])
        }
    }
}