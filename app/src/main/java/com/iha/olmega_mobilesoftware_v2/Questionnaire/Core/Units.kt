package com.iha.olmega_mobilesoftware_v2.Questionnaire.Core

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatActivity
import com.iha.olmega_mobilesoftware_v2.R

/**
 * Created by ulrikkowalk on 01.03.17.
 */
class Units(private val mContext: Context?) : AppCompatActivity() {
    private val mResources: Resources = mContext!!.resources
    private val mMetrics: DisplayMetrics = mResources.displayMetrics

    fun getUsableSliderHeight(isImmersive: Boolean): Int {
        return if (isImmersive) {
            screenHeight - mContext!!.resources.getDimension(R.dimen.toolBarHeightWithPadding).toInt() - mContext.resources.getDimension(R.dimen.progressBarHeight).toInt() - mContext.resources.getDimension(R.dimen.questionTextHeight).toInt()
        } else {
            screenHeight -
                    statusBarHeight - mContext!!.resources.getDimension(R.dimen.toolBarHeightWithPadding).toInt() - mContext.resources.getDimension(R.dimen.progressBarHeight).toInt() - mContext.resources.getDimension(R.dimen.questionTextHeight).toInt() - mContext.resources.getDimension(R.dimen.preferencesButtonHeight).toInt() - mContext.resources.getDimension(R.dimen.actionBarHeight).toInt()
        }
    }

    val statusBarHeight: Int
        get() = (24 * mMetrics.density).toInt()

    fun convertDpToPixels(dp: Float): Int {
        return (dp * (mMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }

    fun convertPixelsToDp(px: Float): Int {
        return (px / (mMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }

    fun convertPixelsToSp(px: Int): Int {
        return (px / mMetrics.scaledDensity).toInt()
    }

    fun convertSpToPixels(sp: Int): Int {
        return (sp * mMetrics.scaledDensity).toInt()
    }

    companion object {
        private val LOG: String = "Units"
        var screenHeight: Int = 0
        var screenWidth: Int  = 0
    }

    init {
        screenWidth = mMetrics.widthPixels
        screenHeight = mMetrics.heightPixels
    }
}