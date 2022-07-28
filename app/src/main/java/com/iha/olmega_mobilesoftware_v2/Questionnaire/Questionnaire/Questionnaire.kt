package com.iha.olmega_mobilesoftware_v2.Questionnaire.Questionnaire

import android.graphics.Color
import android.view.View
import android.widget.LinearLayout
import com.iha.olmega_mobilesoftware_v2.Core.FileIO
import com.iha.olmega_mobilesoftware_v2.Questionnaire.Core.EvaluationList
import com.iha.olmega_mobilesoftware_v2.Questionnaire.Core.MetaData
import com.iha.olmega_mobilesoftware_v2.Questionnaire.QuestionnaireActivity
import java.util.*

/**
 * Created by ulrikkowalk on 28.02.17.
 */
class Questionnaire(// Context of MainActivity()
        private val mQuestionnaireActivity: QuestionnaireActivity?, private val mHead: String?, private val mFoot: String?, private val mSurveyURI: String?,
        private val mMotivation: String?, version: String?, // Context of QuestionnairePageAdapter for visibility
        private val mContextQPA: QuestionnairePagerAdapter?,
        clientID: String?) {
    // Accumulator for ids, values and texts gathered from user input
    private val mEvaluationList: EvaluationList?

    // Basic information about all available questions
    private val mQuestionInfo: ArrayList<QuestionInfo?>?
    private val mFileIO: FileIO?

    // Flag: display forced empty vertical spaces
    private val acceptBlankSpaces = false

    // Number of pages in questionnaire (visible and hidden)
    var numPages = 0
        private set

    // ArrayList containing all questions (including all attached information)
    private var mQuestionList: ArrayList<String>?
    private var mMetaData: MetaData? = null
    private val mVersion: String?
    private var isImmersive = false
    private val clientID: String?
    fun setUp(questionList: ArrayList<String>?) {
        mMetaData = MetaData(mQuestionnaireActivity, mHead, mFoot,
                mSurveyURI, mMotivation, mVersion)
        mMetaData!!.initialise()
        mQuestionList = questionList
        numPages = mQuestionList!!.size
        putAllQuestionsInQuestionInfo()
    }

    // Generate a Layout for Question at desired Position based on String Blueprint
    fun createQuestion(position: Int): Question? {
        val sQuestionBlueprint = mQuestionList!![position]
        return Question(sQuestionBlueprint, mQuestionnaireActivity)
    }

    // Builds the Layout of each Stage Question
    fun generateView(question: Question?, immersive: Boolean): LinearLayout? {
        isImmersive = immersive

        // Are the answers to this specific Question grouped as Radio Button Group?
        var isRadio = false
        var isCheckBox = false
        var isSliderFix = false
        var isSliderFree = false
        var isEmoji = false
        var isText = false
        var isWebsite = false
        var isFinish = false
        var isPhotograph = false
        var isInfo = false
        var isInfoScreen = false
        var isTime = false
        val answerContainer = LinearLayout(mQuestionnaireActivity)
        answerContainer.id = question!!.questionId
        val linearContainerParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)
        answerContainer.orientation = LinearLayout.VERTICAL
        answerContainer.layoutParams = linearContainerParams
        answerContainer.setBackgroundColor(Color.WHITE)

        // TextView carrying the Question
        val questionText = QuestionText(mQuestionnaireActivity, question.questionId,
                question.questionText, answerContainer)
        questionText.addQuestion()

        // Creates a Canvas for the Answer Layout
        val answerLayout = AnswerLayout(mQuestionnaireActivity)
        answerContainer.addView(answerLayout.scrollContent)

        // Format of Answer e.g. "radio", "checkbox", ...
        val sType = question.typeAnswer
        val answerTypeRadio = AnswerTypeRadio(
                mQuestionnaireActivity, this, answerLayout, question.questionId)

        // In case of checkbox type
        /*
        val answerTypeCheckBox = AnswerTypeCheckBox(
                mQuestionnaireActivity, this, answerLayout, question.getQuestionId())
        */
        // In case of emoji type
        val answerTypeEmoji = AnswerTypeEmoji(
                mQuestionnaireActivity, this, answerLayout, question.questionId, isImmersive)

        // In case of sliderFix type
        val answerSliderFix = AnswerTypeSliderFix(
                mQuestionnaireActivity, this, answerLayout, question.questionId, questionText, isImmersive)

        // In case of sliderFree type
        val answerSliderFree = AnswerTypeSliderFree(
                mQuestionnaireActivity, this, answerLayout, question.questionId, questionText, isImmersive)
        val answerTypeText = AnswerTypeText(
                mQuestionnaireActivity, this, answerLayout, question.questionId, isImmersive)
        val answerTypeWebsite = AnswerTypeWebsite(
                mQuestionnaireActivity, this, answerLayout, question.questionId, isImmersive,
                clientID)
        val answerTypeFinish = AnswerTypeFinish(
                mQuestionnaireActivity, this, answerLayout)
        val answerTypePhotograph = AnswerTypePhotograph(
                mQuestionnaireActivity, answerLayout)
        val answerTypeInfo = AnswerTypeInfo(
                mQuestionnaireActivity, this, answerLayout)
        val answerTypeInfoScreen = AnswerTypeInfoScreen(
                mQuestionnaireActivity, this, answerLayout)
        val answerTypeTime = AnswerTypeTime(
                mQuestionnaireActivity, this, question.questionId)

        // Number of possible Answers
        val nNumAnswers = question.numAnswers
        // List carrying all Answers and Answer Ids
        val answerList = question.answers

        // Iteration over all possible Answers attributed to current question
        for (iAnswer in 0 until nNumAnswers) {

            // Obtain Answer specific Parameters
            val currentAnswer = answerList!![iAnswer]
            val sAnswer = currentAnswer!!.Text
            val nAnswerId = currentAnswer.Id
            val nAnswerGroup = currentAnswer.Group
            val isDefault = currentAnswer.isDefault
            val isExclusive = currentAnswer.isExclusive
            if (nAnswerId == 66666 && acceptBlankSpaces || nAnswerId != 66666) {
                when (sType) {
                    "radio" -> {
                        isRadio = true
                        answerTypeRadio.addAnswer(nAnswerId, sAnswer, isDefault)
                    }
                    "checkbox" -> {
                        /*
                        isCheckBox = true
                        answerTypeCheckBox.addAnswer(nAnswerId, sAnswer, nAnswerGroup,
                                isDefault, isExclusive)
                         */
                    }
                    "text" -> {
                        isText = true
                        if (nNumAnswers > 0) {
                            answerTypeText.addQuestion(sAnswer)
                        }
                    }
                    "finish" -> {
                        isFinish = true
                        answerTypeFinish.addAnswer()
                    }
                    "sliderFix" -> {
                        isSliderFix = true
                        answerSliderFix.addAnswer(nAnswerId, sAnswer, isDefault)
                    }
                    "sliderFree" -> {
                        isSliderFree = true
                        answerSliderFree.addAnswer(nAnswerId, sAnswer, isDefault)
                    }
                    "emoji" -> {
                        isEmoji = true
                        answerTypeEmoji.addAnswer(nAnswerId, sAnswer!!, isDefault)
                    }
                    "website" -> {
                        isWebsite = true
                        answerTypeWebsite.addAnswer(sAnswer)
                    }
                    "photograph" -> {
                        isPhotograph = true
                        answerTypePhotograph.addAnswer(sAnswer, nAnswerId)
                    }
                    "info" -> {
                        isInfo = true
                        answerTypeInfo.addAnswer()
                    }
                    "infoscreen" -> {
                        isInfoScreen = true
                        answerTypeInfoScreen.addAnswer(sAnswer)
                    }
                    "time" -> {
                        isTime = true
                        answerTypeTime.addAnswer(nAnswerId, sAnswer)
                    }
                    else -> {
                        isRadio = false
                    }
                }
            }
        }
        if (isText) {
            answerTypeText.buildView()
            answerTypeText.addClickListener()
        }
        if (isCheckBox) {
            /*
            answerTypeCheckBox.buildView()
            answerTypeCheckBox.addClickListener()

             */
        }
        if (isEmoji) {
            answerTypeEmoji.buildView()
            answerTypeEmoji.addClickListener()
        }
        if (isSliderFix) {
            answerSliderFix.buildView()
            answerSliderFix.addClickListener()
        }
        if (isSliderFree) {
            answerSliderFree.buildView()
            answerSliderFree.addClickListener()
        }
        if (isRadio) {
            answerTypeRadio.buildView()
            answerTypeRadio.addClickListener()
        }
        if (isWebsite) {
            answerTypeWebsite.buildView()
            answerTypeWebsite.addClickListener()
        }
        if (isFinish) {
            answerTypeFinish.addClickListener()
        }
        if (isPhotograph) {
            answerTypePhotograph.buildView()
            answerTypePhotograph.addClickListener()
        }
        if (isInfo) {
            answerTypeInfo.addClickListener()
        }
        if (isInfoScreen) {
            //answerTypeInfo.addClickListener();
        }
        if (isTime) {
            //answerTypeTime.buildView();
        }
        return answerContainer
    }

    fun addValueToEvaluationList(questionId: Int, value: Float): Boolean {
        mEvaluationList!!.add(questionId, value)
        return true
    }

    fun addTextToEvaluationLst(questionId: Int, text: String?): Boolean {
        mEvaluationList!!.add(questionId, text)
        return true
    }

    fun addIdToEvaluationList(questionId: Int, id: Int): Boolean {
        mEvaluationList!!.add(questionId, id)
        return true
    }

    fun removeIdFromEvaluationList(id: Int): Boolean {
        mEvaluationList!!.removeAnswerId(id)
        return true
    }

    fun removeQuestionIdFromEvaluationList(questionId: Int): Boolean {
        mEvaluationList!!.removeQuestionId(questionId)
        return true
    }

    fun finaliseEvaluation(): Boolean {
        mMetaData!!.finalise(mEvaluationList)
        mFileIO!!.saveDataToFile(mQuestionnaireActivity, mMetaData!!.fileName!!, mMetaData!!.data!!)
        returnToMenu()
        return true
    }

    fun getId(question: Question?): Int {
        return question!!.questionId
    }

    // Function checks all available pages on whether their filtering condition has been met and
    // toggles visibility by destroying or creating the views and adding them to the list of
    // views which is handled by QuestionnairePagerAdapter
    fun checkVisibility(): Boolean {
        var sid: String? = ""
        for (iQ in mEvaluationList!!.indices) {
            sid += mEvaluationList[iQ]!!.value
            sid += ", "
        }
        var wasChanged = true

        // Repeat until nothing changes anymore
        while (wasChanged) {
            wasChanged = false

            //Log.e(LOG, "Size of MQuestionInfo: " + mQuestionInfo.size());
            for (iPos in mQuestionInfo!!.indices) {
                val qI = mQuestionInfo[iPos]
                if (qI!!.isActive) {                                                                    // View is active but might be obsolete
                    if (qI.isHidden) {                                                                // View is declared "Hidden"
                        removeQuestion(iPos)
                        wasChanged = true
                        /*} else if (!mEvaluationList.containsAtLeastOneAnswerId(qI.getFilterIdPositive())    // Not even 1 positive Filter Id exists OR No positive filter Ids declared
                            && qI.getFilterIdPositive().size() > 0) {
                        removeQuestion(iPos);
                        wasChanged = true;*/
                    } else if (!mEvaluationList.containsAllAnswerIds(qI.filterIdPositive) // Not all positive Filter Id exist OR No positive filter Ids declared
                            && qI.filterIdPositive!!.size > 0) {
                        removeQuestion(iPos)
                        wasChanged = true
                    } else if (mEvaluationList.containsAtLeastOneAnswerId(qI.filterIdNegative)) {  // At least 1 negative filter Id exists
                        removeQuestion(iPos)
                        wasChanged = true
                    }
                } else {                                                                                // View is inactive but should possibly be active
                    if (!qI.isHidden //&& (mEvaluationList.containsAtLeastOneAnswerId(qI.getFilterIdPositive())    // View is not declared "Hidden"
                            //|| qI.getFilterIdPositive().size() == 0)                                    // && (At least 1 positive Filter Id exists OR No positive filter Ids declared)
                            && (mEvaluationList.containsAllAnswerIds(qI.filterIdPositive) // View is not declared "Hidden"
                                    || qI.filterIdPositive!!.size == 0) // && (All positive Filter Ids exist OR No positive filter Ids declared)
                            && (!mEvaluationList.containsAtLeastOneAnswerId(qI.filterIdNegative) // && (Not even 1 negative Filter Id exists OR No negative filter Ids declared)
                                    || qI.filterIdNegative!!.size == 0)) {
                        addQuestion(iPos)
                        wasChanged = true
                    }
                }
                if (qI.question!!.typeAnswer == "time") {
                    removeViewOnly(iPos)
                }
            }
        }

        // Sort the List of Views by Id
        Collections.sort(mContextQPA!!.mListOfViews)
        // Force a reload of the List of Views
        mContextQPA.notifyDataSetChanged()
        return true
    }

    private fun putAllQuestionsInQuestionInfo() {
        for (iQuestion in mQuestionList!!.indices) {
            val question = createQuestion(iQuestion)
            mQuestionInfo!!.add(QuestionInfo(question))
            mQuestionInfo[iQuestion]!!.setActive()

            // Question is added to MetaData so the class always holds a complete set of all questions
            // (empty questions are still included in output xml file)
            mMetaData!!.addQuestion(question)
        }
    }

    // Adds the question to the displayed list
    private fun addQuestion(iPos: Int): Boolean {
        mQuestionInfo!![iPos]!!.setActive()
        val question = createQuestion(iPos)
        val view: View? = generateView(question, isImmersive)

        // View is fetched from Storage List and added to Active List
        mContextQPA!!.addView(view,
                question!!.isForced,  // this is were the injection happens
                question.answerIds,
                question.filterId)
        mContextQPA.notifyDataSetChanged()
        mContextQPA.setQuestionnaireProgressBar()
        return true
    }

    // Removes the question from the displayed list and all given answer ids from memory
    fun removeQuestion(iPos: Int): Boolean {
        mQuestionInfo!![iPos]!!.setInactive()
        mEvaluationList!!.removeQuestionId(mQuestionInfo[iPos]!!.id)

        // Remove View from Active List
        mContextQPA!!.removeView(mQuestionInfo[iPos]!!.id)
        mContextQPA.notifyDataSetChanged()
        mContextQPA.setQuestionnaireProgressBar()
        return true
    }

    fun removeViewOnly(iPos: Int) {
        mContextQPA!!.removeView(mQuestionInfo!![iPos]!!.id)
        mContextQPA.notifyDataSetChanged()
        mContextQPA.setQuestionnaireProgressBar()
    }

    // Returns answers given by user for specific question
    fun getQuestionHasBeenAnswered(id: Int): Boolean {
        return mEvaluationList!!.containsQuestionId(id)
    }

    private fun returnToMenu() {
        mQuestionnaireActivity!!.finish()
    }

    fun moveForward() {
        mContextQPA!!.moveForward()
    }

    companion object {
        private val LOG: String? = "Questionnaire"
    }

    init {
        mEvaluationList = EvaluationList()
        mQuestionList = ArrayList()
        mFileIO = FileIO()
        mQuestionInfo = ArrayList()
        mVersion = version
        this.clientID = clientID
    }
}