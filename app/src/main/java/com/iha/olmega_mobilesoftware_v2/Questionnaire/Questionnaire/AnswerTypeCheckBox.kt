package com.iha.olmega_mobilesoftware_v2.Questionnaire.Questionnaire

/**
 * Created by ulrikkowalk on 17.02.17.
 */
/*
class AnswerTypeCheckBox(context: Context?, questionnaire: Questionnaire?, qParent: AnswerLayout?, nQuestionId: Int) : AnswerType(context, questionnaire, qParent, nQuestionId) {
    //public final AnswerLayout mParent;
    //private final Context mContext;
    //private final int mQuestionId;
    override val mListOfAnswers: MutableList<StringIntegerAndInteger> = mutableListOf()
    private val mListOfDefaults: MutableList<Int?>?

    //private final Questionnaire mQuestionnaire;
    var answerParams: LinearLayout.LayoutParams? = null
    private var mExclusiveId = -1
    fun addAnswer(nAnswerId: Int, sAnswer: String?, nGroup: Int, isDefault: Boolean, isExclusive: Boolean) {
        mListOfAnswers.add(StringIntegerAndInteger(sAnswer!!, nAnswerId, nGroup))
        if (isDefault) {
            mListOfDefaults!!.add(mListOfAnswers!!.size - 1)
        }
        if (isExclusive) {
            mExclusiveId = nAnswerId
        }
    }

    override fun buildView() {
        for (iAnswer in mListOfAnswers!!.indices) {
            val currentId = mListOfAnswers[iAnswer].id
            val currentString = mListOfAnswers[iAnswer].text
            val checkBox = CheckBox(mContext)
            checkBox.id = currentId
            checkBox.text = currentString
            checkBox.textSize = mContext!!.resources.getDimension(R.dimen.textSizeAnswer)
            checkBox.isChecked = false
            checkBox.gravity = Gravity.CENTER_VERTICAL
            //checkBox.setGravity(Gravity.START);
            checkBox.setPadding(24, 24, 24, 24)
            /*checkBox.setPadding(
                    (int) mContext.getResources().getDimension(R.dimen.answerTypeCheckBoxPadding_Left),
                    (int) mContext.getResources().getDimension(R.dimen.answerTypeCheckBoxPadding_Top),
                    (int) mContext.getResources().getDimension(R.dimen.answerTypeCheckBoxPadding_Right),
                    (int) mContext.getResources().getDimension(R.dimen.answerTypeCheckBoxPadding_Bottom)
            );*/checkBox.setTextColor(ContextCompat.getColor(mContext, R.color.TextColor))
            checkBox.setBackgroundColor(ContextCompat.getColor(mContext, R.color.BackgroundColor))
            val states = arrayOf<IntArray?>(intArrayOf(android.R.attr.state_checked), intArrayOf())
            val colors = intArrayOf(ContextCompat.getColor(mContext, R.color.JadeRed),
                    ContextCompat.getColor(mContext, R.color.JadeRed))
            CompoundButtonCompat.setButtonTintList(checkBox, ColorStateList(states, colors))

            // Parameters of Answer Button
            answerParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
            checkBox.minHeight = mContext.resources.getDimension(R.dimen.textSizeAnswer).toInt()
            if (mListOfAnswers[iAnswer].id == 66666) {
                checkBox.isEnabled = false
                checkBox.visibility = View.INVISIBLE
            }
            mParent!!.layoutAnswer!!.addView(checkBox, answerParams)
        }
    }

    override fun addClickListener() {
        for (iAnswer in mListOfAnswers!!.indices) {
            val group: Int = mListOfAnswers[iAnswer].group
            val currentId = mListOfAnswers[iAnswer].id
            val checkBox = mParent!!.layoutAnswer!!.findViewById<View?>(currentId) as CheckBox
            if (mListOfDefaults!!.contains(currentId)) {
                checkBox.isChecked = true
                mQuestionnaire!!.addIdToEvaluationList(mQuestionId, currentId)
            }
            checkBox?.setOnClickListener {
                if (checkBox.isChecked) {
                    if (group != -1) {
                        unCheckGroup(group)
                    }
                    if (currentId == mExclusiveId) {
                        unCheckEverythingElse()
                    } else {
                        unCheckExclusive()
                    }
                    checkBox.isChecked = true
                    mQuestionnaire!!.addIdToEvaluationList(mQuestionId, currentId)
                } else {
                    mQuestionnaire!!.removeIdFromEvaluationList(currentId)
                }
                mQuestionnaire.checkVisibility()
            }
        }
    }

    private fun unCheckGroup(nGroup: Int): Boolean {
        for (iAnswer in mListOfAnswers!!.indices) {
            if (mListOfAnswers[iAnswer].group == nGroup) {
                val currentId = mListOfAnswers[iAnswer].id
                val checkBox = mParent!!.layoutAnswer!!.findViewById<View?>(currentId) as CheckBox
                checkBox.isChecked = false
                mQuestionnaire!!.removeIdFromEvaluationList(currentId)
            }
        }
        return true
    }

    private fun unCheckExclusive() {
        val checkBox = mParent!!.layoutAnswer!!.findViewById<View?>(mExclusiveId) as CheckBox
        try {
            checkBox.isChecked = false
            mQuestionnaire!!.removeIdFromEvaluationList(mExclusiveId)
        } catch (e: Exception) {
        }
    }

    private fun unCheckEverythingElse() {
        for (iAnswer in mListOfAnswers!!.indices) {
            if (mListOfAnswers[iAnswer].id != mExclusiveId) {
                val currentId = mListOfAnswers[iAnswer].id
                val checkBox = mParent!!.layoutAnswer!!.findViewById<View?>(currentId) as CheckBox
                checkBox.isChecked = false
                mQuestionnaire!!.removeIdFromEvaluationList(currentId)
            }
        }
    }

    companion object {
        private val LOG: String? = "AnswerTypeCheckbox"
    }

    init {

        /*mContext = context;
        mParent = qParent;
        mQuestionId = nQuestionId;
        mQuestionnaire = questionnaire;*/
        mListOfAnswers = ArrayList<StringIntegerAndInteger?>()
        mListOfDefaults = ArrayList()
    }
}
 */