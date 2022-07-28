package com.iha.olmega_mobilesoftware_v2


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
/**
 * Start activity on system startup
 *
 * http://stackoverflow.com/questions/6391902/how-to-start-an-application-on-startup
 */
class AutostartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, _intent: Intent) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val autostart = sharedPreferences.getBoolean("autoStartActivity", true)
        val isAdmin = sharedPreferences.getBoolean("isAdmin", false)
        if (_intent.action == Intent.ACTION_BOOT_COMPLETED && autostart && !isAdmin) {
            val intent = Intent(context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}