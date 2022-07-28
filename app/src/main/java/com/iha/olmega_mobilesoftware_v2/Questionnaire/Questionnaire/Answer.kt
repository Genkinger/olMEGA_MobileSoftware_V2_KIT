package com.iha.olmega_mobilesoftware_v2.Questionnaire.Questionnaire

import android.util.Log

/**
 * Created by ulrikkowalk on 28.02.17.
 */
class Answer {
    private val LOG_STRING: String? = "Answer"
    var Text: String?
    var Id: Int
    var Group: Int
    var isDefault = false
        private set
    private val isDebug = false
    var isExclusive = false
        private set

    constructor(sAnswer: String?, nAnswerId: Int, nGroup: Int) {
        Text = sAnswer
        Text = Text!!.replace("&lt;".toRegex(), "<")
        Text = Text!!.replace("&gt;".toRegex(), ">")
        Id = nAnswerId
        Group = nGroup
        if (isDebug) {
            Log.i(LOG_STRING, "Answer added - Text: $Text, Id: $Id, Group: $Group")
        }
    }

    constructor(sAnswer: String?, nAnswerId: Int, nGroup: Int, bDefault: Boolean, bExclusive: Boolean) {
        Text = sAnswer
        Text = Text!!.replace("&lt;".toRegex(), "<")
        Text = Text!!.replace("&gt;".toRegex(), ">")
        Id = nAnswerId
        Group = nGroup
        isDefault = bDefault
        isExclusive = bExclusive
        if (isDebug) {
            Log.i(LOG_STRING, "Answer added - Text: " + Text + ", Id: " +
                    Id + ", Group: " + nGroup + ", Default: " + isDefault +
                    ", Exclusive: " + isExclusive)
        }
    }
}