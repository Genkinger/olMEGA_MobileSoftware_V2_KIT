package com.iha.olmega_mobilesoftware_v2.Core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.util.Log
import com.iha.olmega_mobilesoftware_v2.MainActivity

class BootReceiver : BroadcastReceiver() {
    private val TAG = this.javaClass.simpleName
    override fun onReceive(context: Context?, _intent: Intent?) {
        Log.d(TAG, "onReceive")
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context!!.applicationContext)
        val autostart = sharedPreferences!!.getBoolean("autoStartActivity", true)
        val isAdmin = sharedPreferences.getBoolean("isAdmin", false)
        if (_intent!!.action == Intent.ACTION_BOOT_COMPLETED && autostart && !isAdmin) {
            Log.d(TAG, "onReceive: start Intent")
            val intent = Intent(context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}