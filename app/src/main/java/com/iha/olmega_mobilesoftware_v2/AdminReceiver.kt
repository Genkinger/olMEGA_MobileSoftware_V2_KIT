package com.iha.olmega_mobilesoftware_v2

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent

class AdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        //Toast.makeText(context, "Device admin enabled", Toast.LENGTH_SHORT).show();
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return "Warning"
    }

    override fun onDisabled(context: Context, intent: Intent) {
        //Toast.makeText(context, "Device admin disabled", Toast.LENGTH_SHORT).show();
    }

    override fun onLockTaskModeEntering(context: Context, intent: Intent, pkg: String) {
        //Toast.makeText(context, "Kiosk mode enabled", Toast.LENGTH_SHORT).show();
    }

    override fun onLockTaskModeExiting(context: Context, intent: Intent) {
        //Toast.makeText(context, "Kiosk mode disabled", Toast.LENGTH_SHORT).show();
    }
}