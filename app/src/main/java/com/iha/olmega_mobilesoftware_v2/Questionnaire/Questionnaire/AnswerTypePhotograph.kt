package com.iha.olmega_mobilesoftware_v2.Questionnaire.Questionnaire

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.iha.olmega_mobilesoftware_v2.Questionnaire.Core.Units
import com.iha.olmega_mobilesoftware_v2.R

/**
 * Created by ul1021 on 21.07.2017.
 */
class AnswerTypePhotograph(private val mContext: Context?, private val mParent: AnswerLayout?) : AppCompatActivity() {
    private val LOG_STRING: String? = "AnswerTypePhotograph"
    private var mString: String? = ""
    private val mButton1: Button?
    private val mButton2: Button?
    private val mPreview: ImageView?
    private val mPreviewParams: LinearLayout.LayoutParams? = null
    private val mAnswerParams: LinearLayout.LayoutParams? = null
    private val mContainer: LinearLayout?
    private val mUnits: Units?
    private var mId = 0
    private val mUsableHeight: Int
    var pm: PackageManager?

    fun addAnswer(sAnswer: String?, id: Int) {
        mString = sAnswer
        mId = id

        //mButton1.setText(mString);
        //mButton1.setId(mId);
    }

    fun buildView() {
        mParent!!.layoutAnswer!!.addView(mContainer)
    }

    fun addClickListener() {
        mButton1!!.setOnClickListener {
            Log.e(LOG_STRING, "Click1")
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(pm!!) != null) {
                Log.e(LOG_STRING, "tpi: " + takePictureIntent.resolveActivity(pm!!))
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }


/*
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
*/
        }
        mButton2!!.setOnClickListener { Log.e(LOG_STRING, "Click2") }
    } /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mPreview.setImageBitmap(imageBitmap);
        }
    }
*/

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
    }

    //TODO:  check camera availability up front
    init {
        mUnits = Units(mContext)
        isImmersive = false
        mUsableHeight = mUnits.getUsableSliderHeight(isImmersive)
        // Slider Layout is predefined in XML
        val inflater = LayoutInflater.from(mContext)
        val width: Int = Units.Companion.screenWidth
        mContainer = inflater!!.inflate(
                R.layout.answer_type_photograph, mParent!!.scrollContent, false) as LinearLayout
        mPreview = mContainer.findViewById<View?>(R.id.photographyPreview) as ImageView
        mButton1 = mContainer.findViewById<View?>(R.id.photographyButton1) as Button
        mButton2 = mContainer.findViewById<View?>(R.id.photographyButton2) as Button
        mPreview.layoutParams.height = mUsableHeight - 500
        mButton1.text = "yes"
        mButton1.textSize = mContext!!.resources.getDimension(R.dimen.textSizeAnswer)
        mButton1.setBackgroundResource(R.drawable.button)
        /*mButton1.setGravity(Gravity.CENTER_HORIZONTAL);
        mButton1.setTextColor(ContextCompat.getColor(mContext, R.color.TextColor));
        mButton1.setBackgroundColor(ContextCompat.getColor(mContext, R.color.BackgroundColor));
        mButton1.setAllCaps(false);
        mButton1.setTypeface(null, Typeface.NORMAL);
        */mButton2.text = "no"
        mButton2.textSize = mContext.resources.getDimension(R.dimen.textSizeAnswer)
        mButton2.setBackgroundResource(R.drawable.button)
        /*mButton2.setGravity(Gravity.CENTER_HORIZONTAL);
        mButton2.setTextColor(ContextCompat.getColor(mContext, R.color.TextColor));
        mButton2.setBackgroundColor(ContextCompat.getColor(mContext, R.color.BackgroundColor));
        mButton2.setAllCaps(false);
        mButton2.setTypeface(null, Typeface.NORMAL);
        */pm = mContext.packageManager
    }
}