package com.iha.olmega_mobilesoftware_v2

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.iha.olmega_mobilesoftware_v2.Core.LogIHAB

class ControlService : Service() {
    private val TAG = this.javaClass.simpleName
    private val mBinder: IBinder = LocalBinder()
    lateinit var systemStatus: SystemStatus
        private set

    fun Status(): SystemStatus {
        return systemStatus
    }

    private val mTaskHandler = Handler()
    private val mActivityCheckTime = 5000
    private val mDisplayReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action != null) {
                when (action) {
                    "android.intent.action.SCREEN_ON" -> LogIHAB.log("Display: on")
                    "android.intent.action.SCREEN_OFF" -> LogIHAB.log("Display: off")
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        systemStatus = SystemStatus(this)
        //Log.d(TAG, "Service onCreate");
        mTaskHandler.post(mActivityCheckRunnable)

        // Register receiver for display activity
        val displayFilter = IntentFilter()
        displayFilter.addAction(Intent.ACTION_SCREEN_ON)
        displayFilter.addAction(Intent.ACTION_SCREEN_OFF)
        registerReceiver(mDisplayReceiver, displayFilter)
        startForeground()
    }

    fun startForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val NOTIFICATION_CHANNEL_ID = getString(R.string.app_name)
            val channelName = getString(R.string.app_name)
            val chan = NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE)
            chan.lightColor = Color.BLUE
            chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val manager = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            manager.createNotificationChannel(chan)
            val intent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), 0)
            val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            val notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.drawable.logo)
                    .setTicker(getString(R.string.app_name))
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.app_name))
                    .setContentIntent(intent)
                    .build()
            startForeground(1, notification)
        } else startForeground(1, Notification())
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand")
        return START_STICKY
    }

    override fun onDestroy() {
        //Log.d(TAG, "Service destroyed");
        systemStatus.onDestroy()
        unregisterReceiver(mDisplayReceiver)
        super.onDestroy()
    }

    inner class LocalBinder : Binder() {
        val service: ControlService
            get() = this@ControlService
    }

    private val mActivityCheckRunnable: Runnable = object : Runnable {
        override fun run() {
            startMainActivity(false)
            mTaskHandler.postDelayed(this, mActivityCheckTime.toLong())
        }
    }

    fun startMainActivity(forceStartActivity: Boolean): Boolean {
        var isActivityRunning = false
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val runningTaskInfo = manager.getRunningTasks(1)
        for (iActivity in runningTaskInfo.indices) {
            val componentInfo = runningTaskInfo[iActivity].topActivity
            if (componentInfo!!.packageName == packageName) {
                isActivityRunning = true
            }
        }
        if (!isActivityRunning && (forceStartActivity || systemStatus.Preferences().autoStartActivity() && systemStatus.Preferences().isInKioskMode && !systemStatus.Preferences().isAdmin)) {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        return isActivityRunning
    }
}