package com.iha.olmega_mobilesoftware_v2.Questionnaire.Questionnaire

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.iha.olmega_mobilesoftware_v2.Questionnaire.Core.Units
import com.iha.olmega_mobilesoftware_v2.R

/**
 * Created by ulrikkowalk on 17.02.17.
 */
class AnswerTypeWebsite(private val context: Context?, questionnaire: Questionnaire?, qParent: AnswerLayout?,
                        nQuestionId: Int, isImmersive: Boolean, private val clientID: String?) : AnswerType(context, questionnaire, qParent, nQuestionId) {
    private var url: String? = null
    private var button: Button? = null
    private val inflater: LayoutInflater?
    fun addAnswer(url: String?) {
        if (url!!.contains("\$clientID$")) {
            this.url = url.replace("\$clientID$", clientID!!)
        } else {
            this.url = url
        }
    }

    override fun buildView() {
        if (isNetworkAvailable) {
            val webView = WebView(mContext!!)
            //webView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 70));
            webView.webViewClient = WebViewClient()
            webView.settings.javaScriptEnabled = true
            webView.settings.useWideViewPort = true
            webView.settings.loadWithOverviewMode = true
            webView.loadUrl(url!!)
            //webView.setInitialScale(70);
            Log.e(AnswerType.Companion.LOG, "URL: " + url)
            button = Button(mContext)
            button!!.setText(R.string.buttonTextOkay)
            button!!.setTextColor(ContextCompat.getColor(mContext, R.color.TextColor))
            button!!.background = ContextCompat.getDrawable(mContext, R.drawable.button)
            val buttonParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            )
            buttonParams.topMargin = 96
            buttonParams.bottomMargin = 48
            val webViewParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    Units.screenWidth,
                    Units.screenHeight - buttonParams.bottomMargin - buttonParams.topMargin - 500
            )
            mParent!!.layoutAnswer!!.setBackgroundColor(ContextCompat.getColor(mContext, R.color.WebGray))
            mParent.scrollContent!!.setBackgroundColor(ContextCompat.getColor(mContext, R.color.WebGray))
            mParent.layoutAnswer!!.addView(webView, webViewParams)
            mParent.layoutAnswer.addView(button, buttonParams)
        } else {
            val info = TextView(mContext)
            info.setText(R.string.noInternet)
            info.textSize = 20.0f
            info.textAlignment = View.TEXT_ALIGNMENT_CENTER
            val infoParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
            infoParams.topMargin = 150
            mParent!!.layoutAnswer!!.addView(info, infoParams)
            Log.e(AnswerType.Companion.LOG, "NO NETWORK AVAILABLE")
        }
    }

    override fun onBackPressed() {
        //super.onBackPressed();
    }

    override fun addClickListener() {
        if (isNetworkAvailable) {
            button!!.setOnClickListener {
                mQuestionnaire!!.moveForward()
                //((MainActivity) context).nextQuestion();
            }
        }
    }

    val isNetworkAvailable: Boolean
        get() {
            val connectivityManager = context!!.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            return connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo!!.isConnected
        }

    companion object {
        private val LOG_STRING: String? = "AnswerTypeWebsite"
    }

    init {
        inflater = LayoutInflater.from(context)
    }
}