package com.iha.olmega_mobilesoftware_v2

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.CompoundButtonCompat

class Help : AppCompatActivity() {
    private val TAG = this.javaClass.simpleName
    lateinit var inflater: LayoutInflater
    lateinit var rb1: RadioButton
    lateinit var rb2: RadioButton
    lateinit var cb1: CheckBox
    lateinit var cb2: CheckBox
    lateinit var cb3: CheckBox
    lateinit var tv1: TextView
    lateinit var tv2: TextView
    lateinit var tv3: TextView
    lateinit var bt1: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inflater = LayoutInflater.from(applicationContext)
        setContentView(generateView())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            if (window.insetsController != null) {
                window.insetsController!!.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                window.insetsController!!.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }

    fun generateView(): LinearLayout {
        val view = inflater.inflate(R.layout.layout_help, null) as LinearLayout
        rb1 = view.findViewById<View>(R.id.radioButton) as RadioButton
        rb2 = view.findViewById<View>(R.id.radioButton2) as RadioButton
        cb1 = view.findViewById<View>(R.id.checkBox) as CheckBox
        cb2 = view.findViewById<View>(R.id.checkBox2) as CheckBox
        cb3 = view.findViewById<View>(R.id.checkBox3) as CheckBox
        tv1 = view.findViewById<View>(R.id.textView) as TextView
        tv2 = view.findViewById<View>(R.id.textView2) as TextView
        tv3 = view.findViewById<View>(R.id.textView3) as TextView
        bt1 = view.findViewById<View>(R.id.button) as Button
        tv2.setText(R.string.help_hint)
        setRadioPrefs(rb1)
        setRadioPrefs(rb2)
        setCheckPrefs(cb1)
        setCheckPrefs(cb2)
        setCheckPrefs(cb3)
        setHeadLinePrefs(tv1)
        setTextPrefs(tv2)
        setTextPrefs(tv3)
        setButtonPrefs(bt1)
        bt1.setOnClickListener { finish() }
        return view
    }

    private fun setRadioPrefs(radiobutton: RadioButton) {
        radiobutton.textSize = applicationContext.resources.getDimension(R.dimen.textSizeAnswerHelp)
        radiobutton.gravity = Gravity.CENTER_VERTICAL
        radiobutton.setTextColor(ContextCompat.getColor(applicationContext, R.color.TextColor))
        radiobutton.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.BackgroundColor))
        val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
        val colors = intArrayOf(ContextCompat.getColor(applicationContext, R.color.JadeRed),
                ContextCompat.getColor(applicationContext, R.color.JadeRed))
        CompoundButtonCompat.setButtonTintList(radiobutton, ColorStateList(states, colors))
        radiobutton.minHeight = applicationContext.resources.getDimension(R.dimen.radioMinHeight).toInt()
        radiobutton.setPadding(24, 24, 24, 24)
    }

    private fun setCheckPrefs(checkBox: CheckBox) {
        checkBox.textSize = applicationContext.resources.getDimension(R.dimen.textSizeAnswerHelp)
        checkBox.isChecked = false
        checkBox.gravity = Gravity.CENTER_VERTICAL
        checkBox.setPadding(24, 24, 24, 24)
        checkBox.setTextColor(ContextCompat.getColor(applicationContext, R.color.TextColor))
        checkBox.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.BackgroundColor))
        val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
        val colors = intArrayOf(ContextCompat.getColor(applicationContext, R.color.JadeRed),
                ContextCompat.getColor(applicationContext, R.color.JadeRed))
        CompoundButtonCompat.setButtonTintList(checkBox, ColorStateList(states, colors))
    }

    private fun setHeadLinePrefs(textView: TextView) {
        textView.setTextColor(ContextCompat.getColor(applicationContext, R.color.TextColor))
        textView.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.lighterGray))
        textView.textSize = applicationContext.resources.getDimension(R.dimen.textSizeQuestion)
    }

    private fun setTextPrefs(textView: TextView) {
        textView.setTextColor(ContextCompat.getColor(applicationContext, R.color.TextColor))
        textView.textSize = applicationContext.resources.getDimension(R.dimen.textSizeAnswerHelp)
    }

    private fun setTextHTML(textView: TextView) {
        val string = textView.text.toString()
        textView.text = Html.fromHtml(string)
    }

    private fun setButtonPrefs(button: Button) {
        button.scaleX = 1.2f
        button.scaleY = 1.2f
        button.setTextColor(ContextCompat.getColor(applicationContext, R.color.TextColor))
        button.background = ContextCompat.getDrawable(applicationContext, R.drawable.button)
    }
}