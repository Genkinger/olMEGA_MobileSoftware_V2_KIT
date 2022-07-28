package com.iha.olmega_mobilesoftware_v2.Questionnaire

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.iha.olmega_mobilesoftware_v2.Core.FileIO
import com.iha.olmega_mobilesoftware_v2.Core.XMLReader
import com.iha.olmega_mobilesoftware_v2.Questionnaire.Questionnaire.QuestionnairePagerAdapter
import com.iha.olmega_mobilesoftware_v2.R

class QuestionnaireActivity : AppCompatActivity() {
    private val TAG = this.javaClass.simpleName
    var mViewPager: ViewPager? = null
    private var mAdapter: QuestionnairePagerAdapter? = null
    private var forceAnswer = false
    private var isAdmin = false
    var clientID: String? = null
    var selectedQuest: String? = null
    private var falseSwipes = 0

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(null)
        this.setResult(RESULT_OK, Intent())
        thisAppCompatActivity = this
        setContentView(R.layout.activity_main_questionaire)
        forceAnswer = intent.extras!!.getBoolean("forceAnswer")
        isAdmin = intent.extras!!.getBoolean("isAdmin")
        clientID = intent.extras!!.getString("clientID")
        selectedQuest = intent.extras!!.getString("selectedQuest")
        mViewPager = null
        mViewPager = findViewById(R.id.viewpager)
        mAdapter = QuestionnairePagerAdapter(this, mViewPager, !isAdmin)
        mViewPager!!.adapter = mAdapter
        mViewPager!!.addOnPageChangeListener(myOnPageChangeListener!!)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            if (window.insetsController != null) {
                window.insetsController!!.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                window.insetsController!!.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
        findViewById<View?>(R.id.logo2).setOnClickListener {
            if (isAdmin) {
                val returnIntent = Intent()
                this@QuestionnaireActivity.setResult(RESULT_OK, returnIntent)
                finish()
            }
        }
        if (isAdmin) findViewById<View?>(R.id.logo2).setBackgroundResource(R.color.BatteryGreen) else findViewById<View?>(R.id.logo2).setBackgroundResource(R.color.lighterGray)
        startQuestionnaire(intent.extras!!.getString("motivation"))
    }

    private val myOnPageChangeListener: OnPageChangeListener? = object : OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {}
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
        override fun onPageSelected(position: Int) {
            // In case of forced answers, no forward swiping is allowed on unanswered questions
            if (!mAdapter!!.hasQuestionBeenAnswered && mAdapter!!.hasQuestionForcedAnswer) {
                mAdapter!!.setQuestionnaireProgressBar(position - 1)
                mAdapter!!.setArrows(position - 1)
                mViewPager!!.setCurrentItem(position - 1, true)
                if (bRecordSwipes) {
                    falseSwipes += 1
                }
                Log.e(TAG, "False Swipes: $falseSwipes")
                if (bRecordSwipes && falseSwipes > 2) {
                    messageFalseSwipes()
                }
            } else {
                mAdapter!!.setQuestionnaireProgressBar(position)
                mAdapter!!.setArrows(position)
                mViewPager!!.setCurrentItem(position, true)
            }
        }
    }

    private fun messageFalseSwipes() {
        bRecordSwipes = false
        falseSwipes = 0
        AlertDialog.Builder(this, R.style.SwipeDialogTheme)
                .setTitle(R.string.app_name)
                .setMessage(R.string.swipeMessage)
                .setPositiveButton(R.string.swipeOkay) { dialog, which -> bRecordSwipes = true }
                .setCancelable(false)
                .show()
    }

    // Starts a new questionnaire, motivation can be {"auto", "manual"}
    private fun startQuestionnaire(motivation: String?) {
        var motivation = motivation
        val mFileIO = FileIO()
        var mXmlReader: XMLReader? = null
        try {
            mXmlReader = XMLReader(this, selectedQuest!!)
        } catch (e: Exception) {
            Toast.makeText(this, "No valid Questionnaire selected!", Toast.LENGTH_LONG).show()
            finish()
        }
        if (mXmlReader != null && mFileIO.setupFirstUse(this)) {
            val questionList = mXmlReader.questionList
            val head = mXmlReader.head
            val foot = mXmlReader.foot
            val surveyUri = mXmlReader.surveyURI
            motivation = "<motivation motivation =\"$motivation\"/>"
            mAdapter!!.createQuestionnaire(questionList, head, foot, surveyUri, motivation, clientID)
        }
    }

    fun incrementPage() {
        mViewPager!!.setCurrentItem(mViewPager!!.currentItem + 1, true)
    }

    fun hideSystemUI(isImmersive: Boolean) {
        if (isImmersive) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE)
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBackPressed() {}

    companion object {
        var thisAppCompatActivity: AppCompatActivity? = null
        private var bRecordSwipes = true
    }
}