package com.iha.olmega_mobilesoftware_v2.Questionnaire.Questionnaire

import android.view.View

/**
 * Created by ulrikkowalk on 09.03.17.
 */
class QuestionView(val view: View?, id: Int, isForced: Boolean,
                   listOfAnswerIds: ArrayList<Int?>?,
                   listOfFilterIds: ArrayList<Int?>?) : Comparable<QuestionView?> {
    private val mId: Int?
    val isForced: Boolean
    val listOfAnswerIds: ArrayList<Int?>?
    val listOfFilterIds: ArrayList<Int?>?
    override fun compareTo(questionView: QuestionView?): Int {
        return mId!!.compareTo(questionView!!.id)
    }

    val id: Int
        get() = mId!!

    // Function returns all positive Filter IDs which represent the MUST EXIST cases
    val filterIdPositive: ArrayList<Int?>?
        get() {
            // Function returns all positive Filter IDs which represent the MUST EXIST cases
            val listOfPositiveIds = ArrayList<Int?>()
            for (iElement in listOfFilterIds!!.indices) {
                if (listOfFilterIds[iElement]!! >= 0) {
                    listOfPositiveIds.add(listOfFilterIds[iElement])
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
            for (iElement in listOfFilterIds!!.indices) {
                if (listOfFilterIds[iElement]!! < 0) {
                    listOfNegativeIds.add(-1 * listOfFilterIds[iElement]!!)
                }
            }
            return listOfNegativeIds
        }

    init {
        mId = id
        this.isForced = isForced
        this.listOfAnswerIds = listOfAnswerIds
        this.listOfFilterIds = listOfFilterIds
    }
}