package com.iha.olmega_mobilesoftware_v2.Questionnaire.Questionnaire

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.MotionEventCompat
import com.iha.olmega_mobilesoftware_v2.Questionnaire.Core.Units
import com.iha.olmega_mobilesoftware_v2.Questionnaire.DataTypes.StringAndInteger
import com.iha.olmega_mobilesoftware_v2.R

/**
 * Created by ulrikkowalk on 04.04.17.
 */
class AnswerTypeSliderFree(context: Context?, questionnaire: Questionnaire?,
                           qParent: AnswerLayout?, nQuestionId: Int, private val mQuestion: QuestionText?, immersive: Boolean) : AnswerType(context, questionnaire, qParent, nQuestionId) {
    private val mHorizontalContainer: LinearLayout?
    private val mAnswerListContainer: LinearLayout?
    private val mResizeView: View?
    private val mRemainView: View?
    private val width: Int
    private val mUsableHeight: Int
    private var mDefaultAnswer = -1
    private var nTextViewHeight = 0

    // These serve to normalise pixel/value for now
    private val mMagicNumber1 = 140
    private val mMagicNumber2 = 151
    override fun buildView() {
        Log.e(AnswerType.Companion.LOG, "NUMBER: " + mListOfAnswers.size)
        for (iTmp in mListOfAnswers.indices) {
            Log.e(AnswerType.Companion.LOG, "ans: " + mListOfAnswers[iTmp])
        }


        // Iterate over all options and create a TextView for each one
        for (iAnswer in mListOfAnswers.indices) {
            val textMark = TextView(mContext)
            val textParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1.0f)
            textParams.setMargins(mContext!!.resources.getDimension(R.dimen.SliderTextBottomMargin_Left).toInt(), mContext.resources.getDimension(R.dimen.SliderTextBottomMargin_Top).toInt(), mContext.resources.getDimension(R.dimen.SliderTextBottomMargin_Right).toInt(), mContext.resources.getDimension(R.dimen.SliderTextBottomMargin_Bottom).toInt())
            textMark.setPadding(mContext.resources.getDimension(R.dimen.SliderTextPadding_Left).toInt(), mContext.resources.getDimension(R.dimen.SliderTextPadding_Top).toInt(), mContext.resources.getDimension(R.dimen.SliderTextPadding_Right).toInt(), mContext.resources.getDimension(R.dimen.SliderTextPadding_Bottom).toInt())
            textMark.text = mListOfAnswers[iAnswer].text
            textMark.id = mListOfAnswers[iAnswer].id
            textMark.setTextColor(ContextCompat.getColor(mContext, R.color.TextColor))

            // Adaptive size and lightness of font of in-between tick marks
            if (mListOfAnswers.size > 6) {
                var textSize = mContext.resources.getDimension(R.dimen.textSizeAnswer).toInt() * 7 / mListOfAnswers.size
                if (iAnswer % 2 == 1) {
                    textSize -= 2
                    textMark.setTextColor(ContextCompat.getColor(mContext, R.color.TextColor_Light))
                }
                textMark.textSize = textSize.toFloat()
            } else {
                textMark.textSize = mContext.resources.getDimension(R.dimen.textSizeAnswer)
            }
            textMark.gravity = Gravity.CENTER_VERTICAL
            textMark.layoutParams = textParams
            textMark.setBackgroundColor(ContextCompat.getColor(mContext, R.color.BackgroundColor))
            mAnswerListContainer!!.addView(textMark)
        }
        mParent!!.layoutAnswer!!.addView(mHorizontalContainer)
    }

    fun addAnswer(nAnswerId: Int, sAnswer: String?, isDefault: Boolean) {
        mListOfAnswers.add(StringAndInteger(sAnswer, nAnswerId))
        // index of default answer if present
        if (isDefault) {
            // If default present, this element is the one
            mDefaultAnswer = mListOfAnswers.size - 1
            // Handles default id if existent
            setProgressItem(mDefaultAnswer)
        }
    }

    override fun addClickListener() {
        val tvTemp = mAnswerListContainer!!.findViewById<View?>(mListOfAnswers[0].id) as TextView
        tvTemp.post {
            nTextViewHeight = tvTemp.height
            // Handles default id if existent
            if (mDefaultAnswer == -1) {
                setProgressItem((mListOfAnswers.size - 1) / 2)
                mQuestionnaire!!.addValueToEvaluationList(mQuestionId, fractionFromProgress)
            } else {
                setProgressItem(mDefaultAnswer)
                mQuestionnaire!!.addValueToEvaluationList(mQuestionId, fractionFromProgress)
            }
        }

        // Enables clicking on option directly
        for (iAnswer in mListOfAnswers.indices) {
            val currentId = mListOfAnswers[iAnswer].id
            val tv = mAnswerListContainer.findViewById<View?>(currentId) as TextView
            tv.setOnClickListener {
                setProgressItem(iAnswer)
                mQuestionnaire!!.removeQuestionIdFromEvaluationList(mQuestionId)
                mQuestionnaire.addValueToEvaluationList(mQuestionId, fractionFromProgress)
                Log.e(AnswerType.Companion.LOG, "Percent: $fractionFromProgress")
            }
        }

        // Enables dragging of slider
        mResizeView!!.setOnTouchListener(OnTouchListener { v, event ->
            val action = MotionEventCompat.getActionMasked(event)
            when (action) {
                MotionEvent.ACTION_DOWN -> return@OnTouchListener true
                MotionEvent.ACTION_MOVE -> return@OnTouchListener rescaleSliderOnline(event)
                MotionEvent.ACTION_UP -> return@OnTouchListener rescaleSliderFinal(event)
                MotionEvent.ACTION_CANCEL -> return@OnTouchListener true
                MotionEvent.ACTION_OUTSIDE -> {
                    Log.d("Motion", "Movement occurred outside bounds " +
                            "of current screen element")
                    return@OnTouchListener true
                }
                else -> {}
            }
            true
        })

        // Enables clicking in area above slider (remainView) to adjust
        mRemainView!!.setOnTouchListener(OnTouchListener { v, event ->
            val action = MotionEventCompat.getActionMasked(event)
            when (action) {
                MotionEvent.ACTION_DOWN -> return@OnTouchListener true
                MotionEvent.ACTION_MOVE -> return@OnTouchListener rescaleSliderOnline(event)
                MotionEvent.ACTION_UP -> return@OnTouchListener rescaleSliderFinal(event)
                MotionEvent.ACTION_CANCEL -> return@OnTouchListener true
                MotionEvent.ACTION_OUTSIDE -> {
                    Log.d("Motion", "Movement occurred outside bounds " +
                            "of current screen element")
                    return@OnTouchListener true
                }
                else -> {}
            }
            true
        })
    }

    // Set progress  bar according to user input
    private fun rescaleSliderFinal(motionEvent: MotionEvent?): Boolean {
        val nValueSelected = clipValuesToRange(motionEvent!!.rawY).toInt()
        try {
            setProgressPixels(nValueSelected)
            mQuestionnaire!!.removeQuestionIdFromEvaluationList(mQuestionId)
            mQuestionnaire.addValueToEvaluationList(mQuestionId, fractionFromProgress)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }

    // Set progress  bar according to user input
    private fun rescaleSliderOnline(motionEvent: MotionEvent?): Boolean {
        val nValueSelected = clipValuesToRange(motionEvent!!.rawY).toInt()
        try {
            setProgressPixels(nValueSelected) // +200 to fit on bigger screen
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }

    // Ensure values inside slider boundaries
    private fun clipValuesToRange(inVal: Float): Float {
        var inVal = inVal
        val nPad = mContext!!.resources.getDimension(R.dimen.answerLayoutPadding_Bottom).toInt()
        if (inVal < Units.screenHeight - mUsableHeight - nPad) {
            inVal = (Units.screenHeight - mUsableHeight - nPad).toFloat()
        } else if (inVal > Units.screenHeight - nPad - mMinProgress) {
            inVal = (Units.screenHeight - nPad - mMinProgress).toFloat()
        }
        return inVal
    }

    // Set progress/slider according to number of selected item (counting from 0)
    private fun setProgressItem(numItem: Int): Int {
        val nHeightView = (mUsableHeight - mMagicNumber1) / mListOfAnswers.size
        val nPixProgress = ((2 * (mListOfAnswers.size - numItem) - 1) /
                2.0f * nHeightView).toInt()
        mResizeView!!.layoutParams.height = nPixProgress
        mResizeView.layoutParams = mResizeView.layoutParams
        return nPixProgress
    }

    // Set progress/slider according to user input measured in pixels
    private fun setProgressPixels(nPixels: Int): Boolean {
        mResizeView!!.layoutParams.height = Units.screenHeight - mContext!!.resources.getDimension(R.dimen.toolBarHeightWithPadding).toInt() - mContext.resources.getDimension(R.dimen.answerLayoutPadding_Bottom).toInt() -
                nPixels
        mResizeView.layoutParams = mResizeView.layoutParams
        return true
    }

    // Returns a floating point number between 0.0 and 1.0 according to progress
    private val fractionFromProgress: Float
        private get() = (mResizeView!!.layoutParams.height - mMinProgress).toFloat() /
                (mUsableHeight - mMagicNumber2 - mMinProgress)

    companion object {
        var LOG_STRING: String? = "AnswerTypeSliderFree"
        private const val mMinProgress = 10
    }

    init {

        //TODO: Resolve magic numbers
        isImmersive = immersive

        // Slider Layout is predefined in XML
        val inflater = LayoutInflater.from(context)
        width = Units.screenWidth
        mParent!!.scrollContent!!.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
        )
        mUsableHeight = Units(mContext).getUsableSliderHeight(isImmersive)
        Log.e(AnswerType.Companion.LOG, "USABLEHEIGHT:$mUsableHeight")
        /**
         *
         * |           mHorizontalContainer          |
         * | mSliderContainer | mAnswerListContainer |
         *
         */

        // mHorizontalContainer is parent to both slider and answer option containers
        mHorizontalContainer = inflater!!.inflate(
                R.layout.answer_type_slider, mParent.scrollContent, false) as LinearLayout
        mHorizontalContainer.orientation = LinearLayout.HORIZONTAL
        mHorizontalContainer.layoutParams = LinearLayout.LayoutParams(
                width,
                mUsableHeight - mMagicNumber2,
                1f
        )
        mHorizontalContainer.setBackgroundColor(
                ContextCompat.getColor(mContext!!, R.color.BackgroundColor))

        // mSliderContainer is host to slider on the left
        val mSliderContainer = mHorizontalContainer.findViewById<View?>(
                R.id.SliderContainer) as RelativeLayout
        mSliderContainer.setBackgroundColor(ContextCompat.getColor(
                mContext, R.color.BackgroundColor))

        // mAnswerListContainer is host to vertical array of answer options
        mAnswerListContainer = mHorizontalContainer.findViewById<View?>(R.id.AnswerTextContainer) as LinearLayout
        mAnswerListContainer.orientation = LinearLayout.VERTICAL
        mAnswerListContainer.setBackgroundColor(ContextCompat.getColor(
                mContext, R.color.BackgroundColor))
        mAnswerListContainer.layoutParams = LinearLayout.LayoutParams(
                width - 100,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
        )
        mResizeView = mHorizontalContainer.findViewById(R.id.ResizeView)
        mRemainView = mHorizontalContainer.findViewById(R.id.RemainView)
        mResizeView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.JadeRed))
        mRemainView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.BackgroundColor))
    }
}