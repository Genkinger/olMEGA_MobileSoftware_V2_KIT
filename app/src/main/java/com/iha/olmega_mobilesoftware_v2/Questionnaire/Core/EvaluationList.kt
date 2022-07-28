package com.iha.olmega_mobilesoftware_v2.Questionnaire.Core

import android.util.Log
import com.iha.olmega_mobilesoftware_v2.Questionnaire.DataTypes.QuestionIdTypeAndValue
import java.util.*

/**
 * Created by ulrikkowalk on 09.05.17.
 */
class EvaluationList : ArrayList<QuestionIdTypeAndValue?>() {
    private val LOG_STRING: String? = "EvaluationList"
    private val mEvaluationList: MutableList<QuestionIdTypeAndValue?>?
    private val isVerbose = false

    // For answer Ids
    fun add(nQuestionId: Int, nAnswerId: Int): Boolean {
        mEvaluationList!!.add(QuestionIdTypeAndValue(
                nQuestionId, "id", Integer.toString(nAnswerId)))
        if (isVerbose) {
            Log.i(LOG_STRING, "Entry added: $nAnswerId")
        }
        return true
    }

    // For answer texts
    fun add(nQuestionId: Int, sText: String?): Boolean {
        mEvaluationList!!.add(QuestionIdTypeAndValue(
                nQuestionId, "text", sText))
        return true
    }

    // For floating point values
    fun add(nQuestionId: Int, nValue: Float): Boolean {
        mEvaluationList!!.add(QuestionIdTypeAndValue(
                nQuestionId, "value", java.lang.Float.toString(nValue)))
        return true
    }

    fun add(nQuestionId: Int, listOfIds: List<Int?>?): Boolean {
        for (iId in listOfIds!!.indices) {
            mEvaluationList!!.add(QuestionIdTypeAndValue(nQuestionId,
                    "id", listOfIds[iId].toString()))
        }
        return true
    }

    //Remove all answers with given Ids in input list
    fun removeAllAnswerIds(listOfIds: ArrayList<Int?>?): Boolean {
        var nRemoved = 0
        for (iId in listOfIds!!.indices) {
            val currentId = listOfIds[iId]!!
            for (iAnswer in mEvaluationList!!.indices.reversed()) {
                if (mEvaluationList[iAnswer]!!.answerType == "id" &&
                        mEvaluationList[iAnswer]!!.value ==
                        Integer.toString(currentId)) {
                    mEvaluationList.removeAt(iAnswer)
                    nRemoved++
                }
            }
        }
        if (isVerbose) {
            Log.i(LOG_STRING, "Entries removed: $nRemoved")
        }
        return true
    }

    //Remove all answers from question with given QuestionId
    fun removeQuestionId(QuestionId: Int): Boolean {
        var nRemoved = 0
        for (iAnswer in mEvaluationList!!.indices.reversed()) {
            if (mEvaluationList[iAnswer]!!.questionId == QuestionId) {
                mEvaluationList.removeAt(iAnswer)
                nRemoved++
            }
        }
        if (isVerbose) {
            Log.i(LOG_STRING, "Entries removed: $nRemoved")
        }
        return true
    }

    //Remove all answers of given type
    fun removeAllOfType(sType: String?): Boolean {
        var nRemoved = 0
        for (iAnswer in mEvaluationList!!.indices.reversed()) {
            if (mEvaluationList[iAnswer]!!.answerType == sType) {
                mEvaluationList.removeAt(iAnswer)
                nRemoved++
            }
        }
        if (isVerbose) {
            Log.i(LOG_STRING, "Entries removed of Type $sType:$nRemoved")
        }
        return true
    }

    //Remove given AnswerId
    fun removeAnswerId(Id: Int): Boolean {
        var nRemoved = 0
        for (iAnswer in mEvaluationList!!.indices.reversed()) {
            if (mEvaluationList[iAnswer]!!.answerType == "id" &&
                    mEvaluationList[iAnswer]!!.value == Integer.toString(Id)) {
                mEvaluationList.removeAt(iAnswer)
                nRemoved++
            }
        }
        if (isVerbose) {
            Log.i(LOG_STRING, "Entries removed: $nRemoved")
        }
        return true
    }

    //Check whether List contains question Id
    fun containsQuestionId(id: Int): Boolean {
        for (iItem in mEvaluationList!!.indices) {
            if (mEvaluationList[iItem]!!.questionId == id) {
                return true
            }
        }
        return false
    }

    //Check whether List contains answer Id
    fun containsAnswerId(id: Int): Boolean {
        for (iItem in mEvaluationList!!.indices) {
            if (mEvaluationList[iItem]!!.answerType == "id" &&
                    mEvaluationList[iItem]!!.value!!.toInt() == id) {
                return true
            }
        }
        return false
    }

    // Check whether List contains member of answer Id List
    fun containsAnswerId(listOfIds: ArrayList<Int?>?): Boolean {
        for (iId in listOfIds!!.indices) {
            for (iItem in mEvaluationList!!.indices) {
                if (mEvaluationList[iItem]!!.answerType == "id" &&
                        mEvaluationList[iItem]!!.value!!.toInt() ==
                        listOfIds[iId]) {
                    return true
                }
            }
        }
        return false
    }

    // Check whether List contains member of answer Id List
    fun containsAtLeastOneAnswerId(listOfIds: ArrayList<Int?>?): Boolean {
        for (iId in listOfIds!!.indices) {
            for (iItem in mEvaluationList!!.indices) {
                if (mEvaluationList[iItem]!!.answerType == "id" &&
                        mEvaluationList[iItem]!!.value!!.toInt() ==
                        listOfIds[iId]) {
                    return true
                }
            }
        }
        return false
    }

    // Check whether List contains member of answer Id List
    fun containsAllAnswerIds(listOfIds: ArrayList<Int?>?): Boolean {
        val resultArray = IntArray(listOfIds!!.size)
        Arrays.fill(resultArray, 0)
        for (iId in listOfIds.indices) {
            for (iItem in mEvaluationList!!.indices) {
                if (mEvaluationList[iItem]!!.answerType == "id" &&
                        mEvaluationList[iItem]!!.value!!.toInt() ==
                        listOfIds[iId]) {
                    resultArray[iId] = 1
                }
            }
        }
        var resultValue = 1
        for (iRes in listOfIds.indices) {
            resultValue *= resultArray[iRes]
        }
        return resultValue != 0
    }

    fun getTextFromQuestionId(id: Int): String? {
        for (iItem in mEvaluationList!!.indices) {
            if (mEvaluationList[iItem]!!.answerType == "text" &&
                    mEvaluationList[iItem]!!.questionId == id) {
                return mEvaluationList[iItem]!!.value
            }
        }
        return "none"
    }

    fun getAnswerTypeFromQuestionId(id: Int): String? {
        for (iItem in mEvaluationList!!.indices) {
            if (mEvaluationList[iItem]!!.questionId == id) {
                return mEvaluationList[iItem]!!.answerType
            }
        }
        return "none"
    }

    fun getCheckedAnswerIdsFromQuestionId(id: Int): ArrayList<String?>? {
        val listOfAnswerIds = ArrayList<String?>()
        for (iItem in mEvaluationList!!.indices) {
            if (mEvaluationList[iItem]!!.questionId == id &&
                    mEvaluationList[iItem]!!.answerType === "id") {
                listOfAnswerIds.add(mEvaluationList[iItem]!!.value)
            }
        }
        return listOfAnswerIds
    }

    fun getCheckedAnswerValuesFromQuestionId(id: Int): ArrayList<String?>? {
        val listOfAnswerValues = ArrayList<String?>()
        for (iItem in mEvaluationList!!.indices) {
            if (mEvaluationList[iItem]!!.questionId == id &&
                    mEvaluationList[iItem]!!.answerType === "value") {
                listOfAnswerValues.add(mEvaluationList[iItem]!!.value)
            }
        }
        return listOfAnswerValues
    }

    fun getValueFromQuestionId(id: Int): String? {
        for (iItem in mEvaluationList!!.indices) {
            if (mEvaluationList[iItem]!!.answerType == "value" &&
                    mEvaluationList[iItem]!!.questionId == id) {
                return mEvaluationList[iItem]!!.value
            }
        }
        return "none"
    }

    @JvmName("size1")
    fun size(): Int {
        return mEvaluationList!!.size
    }

    override fun get(item: Int): QuestionIdTypeAndValue? {
        return mEvaluationList!![item]
    }

    init {
        mEvaluationList = ArrayList()
    }
}