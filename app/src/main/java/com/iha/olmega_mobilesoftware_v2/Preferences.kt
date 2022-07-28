package com.iha.olmega_mobilesoftware_v2


import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.iha.olmega_mobilesoftware_v2.Core.FileIO
import java.io.File

class Preferences(mcontext: Context?) {
    var sharedPreferences: SharedPreferences
    var isInKioskMode = false
    var isDeviceOwner = false
    var configHasErrors = false
    val isAdmin: Boolean
        get() = sharedPreferences.getBoolean("isAdmin", false)

    fun usbCutsConnection(): Boolean {
        return sharedPreferences.getBoolean("usbCutsConnection", true)
    }

    fun autoStartActivity(): Boolean {
        return sharedPreferences.getBoolean("autoStartActivity", true)
    }

    fun forceAnswer(): Boolean {
        return sharedPreferences.getBoolean("forceAnswer", true)
    }

    val isKioskModeNecessary: Boolean
        get() = sharedPreferences.getBoolean("isKioskModeNecessary", true)

    //public boolean forceAnswerDialog() {return sharedPreferences.getBoolean("forceAnswerDialog", true);}
    //public boolean useQuestionnaireTimer() {return sharedPreferences.getBoolean("useQuestionnaireTimer", true);}
    //public boolean unsetDeviceAdmin() {return sharedPreferences.getBoolean("unsetDeviceAdmin", false);}
    //public boolean killAppAndService() {return sharedPreferences.getBoolean("killAppAndService", false);}
    //public String installNewApp() {return sharedPreferences.getString("installNewApp", "");}
    fun showQuestionnaireTimer(): Boolean {
        return sharedPreferences.getBoolean("showQuestionnaireTimer", true)
    }

    fun useQuestionnaire(): Boolean {
        return sharedPreferences.getBoolean("useQuestionnaire", false)
    }

    fun clientID(): String? {
        return sharedPreferences.getString("clientID", "0000")
    }

    fun selectedQuest(): String? {
        return sharedPreferences.getString("selectedQuest", "")
    }

    fun inputProfile(): String? {
        return sharedPreferences.getString("inputProfile", "")
    }

    fun onDestroy() {}

    companion object {
        //public void clearUnsetDeviceAdmin() {sharedPreferences.edit().putBoolean("unsetDeviceAdmin", false).commit();}
        var UdaterSettings = File(FileIO.folderPath + File.separator + "UdaterSettings.xml")
    }

    //private Context mContext = null;
    init {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mcontext)
        //sharedPreferences.edit().putBoolean("killAppAndService", false).commit();
        //sharedPreferences.edit().putString("installNewApp", "").commit();
        //mContext = mcontext;
    }
}