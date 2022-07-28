package com.iha.olmega_mobilesoftware_v2.Questionnaire.Questionnaire

/**
 * Created by ulrikkowalk on 06.03.17.
 */
internal class QuestionInfo {
    private val LOG_STRING: String? = "QuestionInfo"
    val id: Int
    private val mFilterId: ArrayList<Int?>?
    val isHidden: Boolean
    val question: Question?
    val answerIds: List<Int?>?
    var isActive: Boolean
        private set
    private val mPositionInPager = 0
    var isForced: Boolean
        private set

    constructor(question: Question?, id: Int, filterId: ArrayList<Int?>?,
                hidden: Boolean,
                answerIds: List<Int?>?, isForced: Boolean) {
        this.question = question
        this.id = id
        mFilterId = filterId
        isActive = true
        isHidden = hidden
        this.answerIds = answerIds
        this.isForced = isForced
    }

    constructor(question: Question?) {
        this.question = question
        id = question!!.questionId
        mFilterId = question.filterId
        isActive = true
        isHidden = question.isHidden
        answerIds = question.answerIds
        isForced = question.isForced
    }

    // Function returns all positive Filter IDs which represent the MUST EXIST cases
    val filterIdPositive: ArrayList<Int?>?
        get() {
            // Function returns all positive Filter IDs which represent the MUST EXIST cases
            val listOfPositiveIds = ArrayList<Int?>()
            for (iElement in mFilterId!!.indices) {
                if (mFilterId[iElement]!! >= 0) {
                    listOfPositiveIds.add(mFilterId[iElement])
                }
            }
            return listOfPositiveIds
        }

    // Function returns all negative IDs (only absolute values), which represent the MUST NOT
    // EXIST case.
    val filterIdNegative: ArrayList<Int?>?
        get() {
            // Function returns all negative IDs (only absolute values), which represent the MUST NOT
            // EXIST case.
            val listOfNegativeIds = ArrayList<Int?>()
            for (iElement in mFilterId!!.indices) {
                if (mFilterId[iElement]!! < 0) {
                    listOfNegativeIds.add(-1 * mFilterId[iElement]!!)
                }
            }
            return listOfNegativeIds
        }

    fun setInactive() {
        isActive = false
    }

    fun setActive() {
        isActive = true
    }
}