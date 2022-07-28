package com.iha.olmega_mobilesoftware_v2.Questionnaire.Questionnaire

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.iha.olmega_mobilesoftware_v2.R
import java.util.*

/**
 * Created by ulrikkowalk on 28.02.17.
 *
 * Question is the class that carries all information needed to fill a question view, i.e.,
 * type of answer, answer options, filter ids, etc
 * BUT: It does not generate the view, only provides contents
 *
 */
class Question(private val mQuestionBlueprint: String?, context: Context?) : AppCompatActivity() {
    private val LOG: String? = "Question"
    var questionText: String? = null
    var typeAnswer: String? = null
    var numAnswers = 0
    var questionId = 0
    var isHidden = false
    var isForced = false
    private val ListOfNonTypicalAnswerTypes = Arrays.asList("text", "date")
    private val mListOfAnswerIds: ArrayList<Int?>? = ArrayList()
    private var mAnswers: MutableList<Answer> = mutableListOf()
    var filterId: ArrayList<Int?>?
    private val mContext: Context?
    private fun extractQuestionId(): Int {
        // Obtain Question Id from Questionnaire
        return mQuestionBlueprint!!.split("id=\"").toTypedArray()[1].split("\"").toTypedArray()[0].replace("_", "").toInt()
    }

    private fun extractQuestionText(): String? {
        // Obtain Question Text from Questionnaire
        return try {
            mQuestionBlueprint!!.split("<label>|</label>").toTypedArray()[1].split("<text>|</text>").toTypedArray()[1]
        } catch (e: Exception) {
            ""
        }
    }

    private fun extractQuestionTextFinish(): String? {
        // Obtain Question Text from Questionnaire
        return mQuestionBlueprint!!.split("\\r?\\n").toTypedArray()[1].split("<text>|</text>").toTypedArray()[1]
    }

    private fun extractFilterId(): ArrayList<Int?>? {
        val listOfFilterIds = ArrayList<Int?>()
        if (mQuestionBlueprint!!.split("filter=\"").toTypedArray().size > 1) {
            val arrayTmp: Array<String?> = mQuestionBlueprint.split("filter=\"").toTypedArray()[1].split("\"").toTypedArray()[0].replace("\\s+".toRegex(), "").split(",").toTypedArray()
            for (iId in arrayTmp.indices) {

                // Negative factor represents EXCLUSION filter
                var nFactor = 1
                if (arrayTmp[iId]!!.startsWith("!")) {
                    nFactor = -1
                }
                listOfFilterIds.add(
                        arrayTmp[iId]!!.replace("_", "").replace("!", "").toInt() * nFactor)
            }
        }
        return listOfFilterIds
    }

    private fun extractIsForced(): Boolean {
        return mQuestionBlueprint!!.contains("forceAnswer=\"true\"")
    }

    private fun extractNumAnswers(): Int {
        return if (nonTypicalAnswer(typeAnswer)) {
            1
        } else {
            // Obtain Number of Answers
            mAnswers.size
        }
    }

    private fun extractTypeAnswers(): String? {
        // Obtain Answer Type (e.g. Radio, Button, Slider,...)
        return mQuestionBlueprint!!.split("type=\"").toTypedArray()[1].split("\"").toTypedArray()[0]
    }

    private fun extractAnswerList(): MutableList<Answer>? {

        // List of Answers
        val listAnswers: MutableList<Answer> = ArrayList()
        val stringArray: Array<String?> = mQuestionBlueprint!!.split("<option|<default").toTypedArray()
        for (iA in 1 until stringArray.size) {
            var answerString = ""
            var answerId = -1
            var answerGroup = -1
            var sGroupTmp: String
            var isDefault = false
            var isExclusive = false
            if (stringArray[iA]!!.contains("option")) {
                isDefault = false
                if (stringArray[iA]!!.contains("id=") && stringArray[iA]!!.split("id=\"|\"").toTypedArray().size > 1) {
                    answerId = stringArray[iA]!!.split("id=\"|\"").toTypedArray()[1].replace("_", "").toInt()
                }
                if (stringArray[iA]!!.contains("group=") && stringArray[iA]!!.split("group=\"|\"").toTypedArray().size > 1) {
                    sGroupTmp = stringArray[iA]!!.split("group=\"").toTypedArray()[1].split("\"").toTypedArray()[0]
                    answerGroup = sGroupTmp.toInt()
                }
                if (stringArray[iA]!!.split("<text>|</text>").toTypedArray().size > 1) {
                    answerString = stringArray[iA]!!.split("<text>|</text>").toTypedArray()[1]
                }
                if (stringArray[iA]!!.contains("condition=\"exclusive\"")) {
                    isExclusive = true
                }
                listAnswers.add(Answer(
                        answerString,
                        answerId,
                        answerGroup,
                        isDefault,
                        isExclusive
                ))
            }
            if (stringArray[iA]!!.contains("default")) {
                isDefault = true
                if (stringArray[iA]!!.contains("id=") && stringArray[iA]!!.split("id=\"|\"").toTypedArray().size > 1) {
                    answerId = stringArray[iA]!!.split("id=\"|\"").toTypedArray()[1].replace("_", "").toInt()
                }
                if (stringArray[iA]!!.contains("group=") && stringArray[iA]!!.split("group=\"|\"").toTypedArray().size > 1) {
                    sGroupTmp = stringArray[iA]!!.split("group=\"").toTypedArray()[1].split("\"").toTypedArray()[0]
                    answerGroup = sGroupTmp.toInt()
                }
                if (stringArray[iA]!!.split("<text>|</text>").toTypedArray().size > 1) {
                    answerString = stringArray[iA]!!.split("<text>|</text>").toTypedArray()[1]
                }
                if (stringArray[iA]!!.contains("condition=\"exclusive\"")) {
                    isExclusive = true
                }
                listAnswers.add(Answer(
                        answerString,
                        answerId,
                        answerGroup,
                        isDefault,
                        isExclusive
                ))
            }
        }
        return listAnswers
    }

    private fun extractHidden(): Boolean {
        return mQuestionBlueprint!!.contains("hidden=\"true\"")
    }

    // String Array carrying introductory Line with Id, Type, Filter
    val isFinish: Boolean
        get() {
            // String Array carrying introductory Line with Id, Type, Filter
            val introductoryLine: Array<String?> = mQuestionBlueprint!!.split("\"").toTypedArray()
            return introductoryLine.size == 1
        }

    private fun nonTypicalAnswer(sTypeAnswer: String?): Boolean {
        return ListOfNonTypicalAnswerTypes.contains(sTypeAnswer)
    }

    val answers: List<Answer?>?
        get() = mAnswers
    val answerIds: ArrayList<Int?>?
        get() {
            if (numAnswers > 0) {
                for (iAnswer in 0 until numAnswers) {
                    mListOfAnswerIds!!.add(mAnswers[iAnswer].Id)
                }
            }
            return mListOfAnswerIds
        }

    // Public Constructor
    init {
        filterId = ArrayList()
        mContext = context
        if (isFinish) {
            questionId = 999999999
            questionText = extractQuestionTextFinish()
            typeAnswer = "finish"
            numAnswers = 1
            isHidden = false
            isForced = false
            mAnswers = ArrayList()
            mAnswers.add(Answer(mContext!!.resources.getString(R.string.buttonTextFinish), -1, 99999))
        } else {
            // Obtain Question Id
            questionId = extractQuestionId()
            // Obtain Question Text
            questionText = extractQuestionText()

            // Obtain Filter Id
            filterId = extractFilterId()
            // Obtain Answer Type (e.g. Radio, Button, Slider,...)
            typeAnswer = extractTypeAnswers()
            // Obtain whether answer is forced (no answer - no forward swipe)
            isForced = extractIsForced()

            // Create List of Answers
            mAnswers = extractAnswerList()!!
            // In case of real text input no answer text is given
            if (mAnswers.size == 0) {
                mAnswers = mutableListOf<Answer>()
                mAnswers.add(Answer("", 33333, -1, false, false))
            }

            // Obtain Number of Answers
            numAnswers = extractNumAnswers()
            // Determine whether Element is hidden
            isHidden = extractHidden()
        }
    }
}