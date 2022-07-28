package com.iha.olmega_mobilesoftware_v2.Questionnaire.Questionnaire

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.iha.olmega_mobilesoftware_v2.Questionnaire.Core.ListOfViews
import com.iha.olmega_mobilesoftware_v2.Questionnaire.QuestionnaireActivity
import com.iha.olmega_mobilesoftware_v2.R

/**
 * Created by ulrikkowalk on 28.02.17.
 */
class QuestionnairePagerAdapter(var mQuestionnaireActivity: QuestionnaireActivity?, val mViewPager: ViewPager?, private val isImmersive: Boolean) : PagerAdapter() {
    private val TAG = this.javaClass.simpleName
    private var mNUM_PAGES = 0
    private var mHead: String? = null
    private var mFoot: String? = null
    private var mSurveyURI: String? = null
    private val mVersion: String? = null
    private var mQuestionnaire: Questionnaire? = null
    private var clientID: String? = null
    private var mMotivation: String? = ""
    private var mQuestionList: ArrayList<String>? = null
    var mListOfViews: ListOfViews? = null

    // Initialise questionnaire based on new input parameters
    fun createQuestionnaire(questionList: ArrayList<String>?, head: String?, foot: String?, surveyUri: String?, motivation: String?, clientID: String?) {
        this.clientID = clientID
        mQuestionList = questionList
        mHead = head
        mFoot = foot
        mSurveyURI = surveyUri
        mMotivation = motivation
        // Instantiates a Questionnaire Object based on Contents of raw XML File
        mQuestionnaire = Questionnaire(mQuestionnaireActivity, mHead, mFoot, mSurveyURI, mMotivation, mVersion, this, this.clientID)
        mQuestionnaire!!.setUp(questionList)
        mNUM_PAGES = mQuestionnaire!!.numPages
        mViewPager!!.offscreenPageLimit = 1
        mListOfViews = ListOfViews()
        createQuestionnaireLayout()
        setControlsQuestionnaire()
        // Creates and destroys views based on filter id settings
        // First, all pages are created, then unsuitable pages are erased from the list.
        mQuestionnaire!!.checkVisibility()
        notifyDataSetChanged()
        mViewPager.currentItem = 0
        setArrows(0)
        setQuestionnaireProgressBar()
        UI_STATE = UI_STATE_QUEST
    }

    // Initialise questionnaire based on last input parameters (only used in case of reversion)
    fun createQuestionnaire() {

        // Instantiates a Questionnaire Object based on Contents of raw XML File
        mQuestionnaire = Questionnaire(mQuestionnaireActivity, mHead, mFoot, mSurveyURI,
                mMotivation, mVersion, this, clientID)
        mQuestionnaire!!.setUp(mQuestionList)
        mNUM_PAGES = mQuestionnaire!!.numPages
        mViewPager!!.offscreenPageLimit = 1
        mListOfViews = ListOfViews()
        createQuestionnaireLayout()
        setControlsQuestionnaire()
        // Creates and destroys views based on filter id settings
        mQuestionnaire!!.checkVisibility()
        notifyDataSetChanged()
        mViewPager.currentItem = 0
        setArrows(0)
        setQuestionnaireProgressBar()
        UI_STATE = UI_STATE_QUEST
    }

    // Set the horizontal Indicator at the Top to follow Page Position
    fun setQuestionnaireProgressBar(position: Int) {
        val progress = mQuestionnaireActivity!!.findViewById<View?>(R.id.progress)
        val regress = mQuestionnaireActivity!!.findViewById<View?>(R.id.regress)
        val nAccuracy = 100
        val nProgress = (position + 1).toFloat() / mViewPager!!.adapter!!.count * nAccuracy
        val nRegress = nAccuracy - nProgress
        val progParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                nRegress
        )
        val regParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                nProgress
        )
        progress!!.layoutParams = progParams
        regress!!.layoutParams = regParams
    }

    // Set the horizontal Indicator at the Top to follow Page Position
    fun setQuestionnaireProgressBar() {
        val nAccuracy = 100
        val progress = mQuestionnaireActivity!!.findViewById<View?>(R.id.progress)
        val regress = mQuestionnaireActivity!!.findViewById<View?>(R.id.regress)
        val nProgress = (mViewPager!!.currentItem + 1).toFloat() /
                mViewPager.adapter!!.count * nAccuracy
        val nRegress = nAccuracy - nProgress
        val progParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                nRegress
        )
        val regParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                nProgress
        )
        progress!!.layoutParams = progParams
        regress!!.layoutParams = regParams
    }

    // Adjust visibility of navigation symbols to given state
    fun setArrows(position: Int) {
        if (position == 0) {
            mQuestionnaireActivity!!.findViewById<View?>(R.id.Action_Back).visibility = View.INVISIBLE
        } else if (mQuestionnaireActivity!!.findViewById<View?>(R.id.Action_Back).visibility == View.INVISIBLE) {
            mQuestionnaireActivity!!.findViewById<View?>(R.id.Action_Back).visibility = View.VISIBLE
        }
        if (position == mViewPager!!.adapter!!.count - 1) {
            mQuestionnaireActivity!!.findViewById<View?>(R.id.Action_Forward).visibility = View.INVISIBLE
        } else if (mQuestionnaireActivity!!.findViewById<View?>(R.id.Action_Forward).visibility == View.INVISIBLE) {
            mQuestionnaireActivity!!.findViewById<View?>(R.id.Action_Forward).visibility = View.VISIBLE
        }
    }

    // Add new page to display
    fun addView(view: View?, isForced: Boolean,
                listOfAnswerIds: ArrayList<Int?>?, listOfFilterIds: ArrayList<Int?>?) {
        mListOfViews!!.add(QuestionView(view, view!!.id, isForced,
                listOfAnswerIds, listOfFilterIds))
    }

    // Sets up visible control elements for questionnaire i.e. navigation symbols
    private fun setControlsQuestionnaire() {
        mQuestionnaireActivity!!.findViewById<View?>(R.id.Action_Forward).visibility = View.VISIBLE
        mQuestionnaireActivity!!.findViewById<View?>(R.id.Action_Back).visibility = View.VISIBLE
        mQuestionnaireActivity!!.findViewById<View?>(R.id.Action_Revert).visibility = View.VISIBLE
        mQuestionnaireActivity!!.findViewById<View?>(R.id.progress).setBackgroundColor(ContextCompat.getColor(mQuestionnaireActivity!!, R.color.JadeRed))
        mQuestionnaireActivity!!.findViewById<View?>(R.id.regress).setBackgroundColor(ContextCompat.getColor(mQuestionnaireActivity!!, R.color.JadeGray))
    }

    // Inserts contents into questionnaire and appoints recycler
    private fun createQuestionnaireLayout() {
        // Generate a view for each page/question and collect them in ArrayList
        for (iQuestion in 0 until mNUM_PAGES) {
            // Extracts Question Details from Questionnaire and creates Question
            val question = mQuestionnaire!!.createQuestion(iQuestion)
            // Inflates Question Layout based on Question Details
            val layout = mQuestionnaire!!.generateView(question, isImmersive)
            mListOfViews!!.add(QuestionView(layout, layout!!.id, question!!.isForced,
                    question.answerIds, question.filterId))
        }
    }

    val hasQuestionBeenAnswered: Boolean
        get() = if (mViewPager!!.currentItem > 0) {
            mQuestionnaire!!.getQuestionHasBeenAnswered(mListOfViews!![mViewPager.currentItem - 1]!!.id)
        } else {
            true
        }
    val hasQuestionForcedAnswer: Boolean
        get() = if (mViewPager!!.currentItem > 0) {
            mListOfViews!![mViewPager.currentItem - 1]!!.isForced
        } else {
            true
        }

    /**
     * Array Adapter Methods
     */
    // Removes specific view from list and updates viewpager
    fun removeView(id: Int) {
        val nCurrentItem = mViewPager!!.currentItem
        mViewPager.adapter = null
        mListOfViews!!.removeFromId(id)
        mViewPager.adapter = this
        mViewPager.currentItem = nCurrentItem
    }

    // Takes view out of viewpager and includes it in displayable collection
    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val view = mListOfViews!![position]!!.view!!
        collection.addView(view)
        return view
    }

    // Removes view from displayable collection
    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View?)
    }

    // Returns number of pages in viewpager
    override fun getCount(): Int {
        mNUM_PAGES = if (mListOfViews != null && mListOfViews!!.size != 0) {
            mListOfViews!!.size
        } else {
            0
        }
        return mNUM_PAGES
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    // Returns position of object in displayed list
    override fun getItemPosition(item: Any): Int {
        /*
        val index = mListOfViews!!.indexOf(item)
        return if (index == -1) POSITION_NONE else index

         */
        return 0
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return ""
    }

    fun moveForward() {
        if (mViewPager!!.currentItem < mViewPager.adapter!!.count - 1) {
            mViewPager.currentItem = mViewPager.currentItem + 1
        }
    }

    companion object {
        private const val UI_STATE_MENU = 1
        private const val UI_STATE_HELP = 2
        private const val UI_STATE_QUEST = 3
        private var UI_STATE = 0
    }

    init {
        // Set controls and listeners
        mQuestionnaireActivity!!.findViewById<View?>(R.id.Action_Back).setOnClickListener {
            if (mViewPager!!.currentItem != 0) {
                mViewPager.currentItem = mViewPager.currentItem - 1
            }
        }
        mQuestionnaireActivity!!.findViewById<View?>(R.id.Action_Forward).setOnClickListener {
            if (mViewPager!!.currentItem < mViewPager.adapter!!.count - 1) {
                mViewPager.currentItem = mViewPager.currentItem + 1
            }
        }
        mQuestionnaireActivity!!.findViewById<View?>(R.id.Action_Revert).setOnClickListener {
            Toast.makeText(mQuestionnaireActivity, R.string.infoTextRevert, Toast.LENGTH_SHORT).show()
            createQuestionnaire()
        }
    }
}