package com.iha.olmega_mobilesoftware_v2

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.*
import android.util.Log
import android.view.*
import android.view.View.OnTouchListener
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.iha.olmega_mobilesoftware_v2.ControlService.LocalBinder
import com.iha.olmega_mobilesoftware_v2.Core.FileIO
import com.iha.olmega_mobilesoftware_v2.Core.LogIHAB
import com.iha.olmega_mobilesoftware_v2.Questionnaire.QuestionnaireActivity
import com.iha.olmega_mobilesoftware_v2.SystemStatus.SystemStatusListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private val TAG = this.javaClass.simpleName
    private val blockedKeys = listOf(KeyEvent.KEYCODE_VOLUME_DOWN,KeyEvent.KEYCODE_VOLUME_UP)
    private var controlService: ControlService? = null
    private var mIsBound = false
    private var mDevicePolicyManager: DevicePolicyManager? = null
    private var mAdminComponentName: ComponentName? = null
    private val neccessaryPermissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)
    private var neccessaryPermissionsIdx = 0
    private var isLocked = false
    private var questionnaireMotivation = QuestionnaireMotivation.Manual
    lateinit var vibrator: Vibrator
    private var automaticQuestTimer = Long.MIN_VALUE
    private var wifiActivated = false
    private var AppClosed = true

    private val mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            controlService = (service as LocalBinder).service
            controlService!!.startForeground()
            //if (!controlService.systemStatus.Preferences().isAdmin())
            //    checkKioskMode();
            checkIsDeviceOwner()
            checkWifi()
            controlService!!.systemStatus.setSystemStatusListener(object : SystemStatusListener() {
                override fun setAcitivyStates(acitivyStates: AcitivyStates?) {
                    if (acitivyStates!!.isCharging && controlService!!.systemStatus.Preferences().usbCutsConnection()) {
                        if (QuestionnaireActivity.thisAppCompatActivity != null) QuestionnaireActivity.thisAppCompatActivity!!.finish()
                    }
                    findViewById<View>(R.id.Layout_CalibrationValues).visibility = (if (acitivyStates.showCalibrationValuesError) 0 else 1) * 8
                    findViewById<View>(R.id.charging).visibility = (if (acitivyStates.isCharging) 0 else 1) * 8
                    val InfoTextView = findViewById<View>(R.id.InfoTextView) as TextView
                    InfoTextView.text = acitivyStates.InfoText
                    InfoTextView.isEnabled = acitivyStates.questionaireEnabled
                    if (controlService!!.systemStatus.Preferences().isAdmin) findViewById<View>(R.id.logo).setBackgroundResource(R.color.BatteryGreen) else if (controlService!!.systemStatus.Preferences().configHasErrors) findViewById<View>(R.id.logo).setBackgroundResource(R.color.design_default_color_error) else findViewById<View>(R.id.logo).setBackgroundResource(R.color.lighterGray)
                    val battery_bottom = findViewById<View>(R.id.battery_bottom)
                    when (acitivyStates.BatteryState) {
                        BatteryStates.Normal -> battery_bottom.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.BatteryGreen))
                        BatteryStates.Warning -> battery_bottom.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.BatteryYellow))
                        BatteryStates.Critical -> battery_bottom.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.JadeRed))
                        else -> {}
                    }
                    // Batterie Level
                    val battery_bottomParams = battery_bottom.layoutParams
                    battery_bottomParams.height = (findViewById<View>(R.id.BatterieView).height * (acitivyStates.batteryLevel / 100)).toInt()
                    battery_bottom.layoutParams = battery_bottomParams
                    val battery_top = findViewById<View>(R.id.battery_top)
                    val battery_topParams = battery_top.layoutParams
                    battery_topParams.height = (findViewById<View>(R.id.BatterieView).height * (1 - acitivyStates.batteryLevel / 100)).toInt()
                    battery_top.layoutParams = battery_topParams
                    if (acitivyStates.profileState == States.Connected) findViewById<View>(R.id.Action_Record).backgroundTintList = ColorStateList.valueOf(ResourcesCompat.getColor(resources, R.color.PhantomDarkBlue, null)) else findViewById<View>(R.id.Action_Record).backgroundTintList = ColorStateList.valueOf(ResourcesCompat.getColor(resources, R.color.JadeGray, null))
                }

                override fun updateAutomaticQuestionnaireTimer(Message: String?, TimeRemaining: Long) {
                    val NextQuestTextView = findViewById<TextView>(R.id.nextQuestTextView)
                    if (TimeRemaining > 0) NextQuestTextView.text = Message else NextQuestTextView.text = ""
                    if (TimeRemaining > Long.MIN_VALUE && TimeRemaining <= 0) questionnaireMotivation = QuestionnaireMotivation.Auto else questionnaireMotivation = QuestionnaireMotivation.Manual
                }
            })
        }

        override fun onServiceDisconnected(className: ComponentName) {
            controlService!!.systemStatus.Refresh()
            controlService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        /*
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .penaltyDeath()
                .build());
         */
        super.onCreate(savedInstanceState)
        appContext = applicationContext
        Thread.setDefaultUncaughtExceptionHandler(OLMEGAUncaughtExceptionHandler(this, MainActivity::class.java))
        setContentView(R.layout.activity_main)
        //MainActivity.this.doBindService();
        findViewById<View>(R.id.logo).setOnClickListener(View.OnClickListener { showPreferences(
            controlService!!.systemStatus.Preferences().isAdmin || controlService!!.systemStatus.Preferences().configHasErrors) })
        findViewById<View>(R.id.Action_Logo).setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                createHelpScreen()
            }
        })
        findViewById<View>(R.id.logo).setOnTouchListener(object : OnTouchListener {
            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> timerLongClick.start()
                    MotionEvent.ACTION_UP -> timerLongClick.cancel()
                }
                return false
            }

            // Timer enabling long click for user access to preferences menu
            private val durationLongClick = (5 * 1000).toLong()
            private val timerLongClick: CountDownTimer = object : CountDownTimer(durationLongClick, 200) {
                override fun onTick(l: Long) {}
                override fun onFinish() {
                    showPreferences(!controlService!!.systemStatus.Preferences().isAdmin)
                }
            }
        })
        findViewById<View>(R.id.InfoTextView).setOnTouchListener { view: View?, motionEvent: MotionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) startQuestionnaire()
            true
        }
        val dateTimeHandler = Handler((Looper.myLooper())!!)
        dateTimeHandler.postDelayed(object : Runnable {
            @Synchronized
            override fun run() {
                val dateTimeTextView = findViewById<TextView>(R.id.DateTimeTextView)
                val currentDate = Date(System.currentTimeMillis())
                val dateformat = SimpleDateFormat("dd.MM.yyyy HH:mm")
                dateTimeTextView.text = dateformat.format(currentDate.time)
                if (wifiActivated) {
                    if (findViewById<View>(R.id.Action_Wifi).visibility == View.VISIBLE) findViewById<View>(R.id.Action_Wifi).visibility = View.INVISIBLE else findViewById<View>(R.id.Action_Wifi).visibility = View.VISIBLE
                }
                if ((isLocked == false) && (questionnaireMotivation == QuestionnaireMotivation.Auto) && (controlService!!.systemStatus.curentActivity == ActiviyRequestCode.MainActivity)) {
                    if (automaticQuestTimer <= 0) automaticQuestTimer = (30 * 60).toLong()
                    if (automaticQuestTimer >= 29 * 60) {
                        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                        LogIHAB.log("Vibration: 500")
                    }
                    val tempView = findViewById<TextView>(R.id.InfoTextView)
                    if (tempView.currentTextColor == ContextCompat.getColor(applicationContext, R.color.colorPrimary)) setInfoTextView(true) else setInfoTextView(false)
                    automaticQuestTimer = automaticQuestTimer - 1
                } else setInfoTextView(false)
                dateTimeHandler.postDelayed(this, 1000)
            }
        }, 0)
    }

    override fun onStart() {
        super.onStart()
        if (AppClosed == true) {
            LogIHAB.log("AppStarted")
            LogIHAB.log("Version: " + BuildConfig.VERSION_NAME)
            AppClosed = false
        }
        checkPermission()
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        // create necessary files
        if (!Preferences.UdaterSettings.exists()) {
            try {
                val inputStream = resources.openRawResource(R.raw.udatersettings)
                val buffer = ByteArray(inputStream.available())
                inputStream.read(buffer)
                val outStream: OutputStream = FileOutputStream(Preferences.UdaterSettings)
                outStream.write(buffer)
            } catch (e: IOException) {
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if ((controlService != null) && (controlService!!.systemStatus.curentActivity == ActiviyRequestCode.MainActivity)) {
            LogIHAB.log("   AppClosed")
            AppClosed = true
        }
    }

    private fun checkPermission() {
        if (neccessaryPermissionsIdx < neccessaryPermissions.size) {
            if (ContextCompat.checkSelfPermission(this@MainActivity, neccessaryPermissions[neccessaryPermissionsIdx]) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(neccessaryPermissions[neccessaryPermissionsIdx]), 1)
            } else {
                neccessaryPermissionsIdx++
                checkPermission()
            }
        } else {
            val updaterSettings = File(FileIO.folderPath + File.separator + "UdaterSettings.xml")
            if (!updaterSettings.isFile) {
                try {
                    val inputStream = resources.openRawResource(R.raw.udatersettings)
                    val buffer = ByteArray(inputStream.available())
                    inputStream.read(buffer)
                    val outStream: OutputStream = FileOutputStream(updaterSettings)
                    outStream.write(buffer)
                } catch (e: IOException) {
                }
            }
            if (!SystemStatus.AFExConfigFolder.exists()) SystemStatus.AFExConfigFolder.mkdirs()
            if (SystemStatus.AFExConfigFolder.listFiles() == null || SystemStatus.AFExConfigFolder.listFiles().size == 0) {
                try {
                    val fileListIn = intArrayOf(R.raw.example_mic_in_speaker_out, R.raw.example_rfcomm_in_audio_out, R.raw.example_standalone, R.raw.rfcomm)
                    val fileListOut = arrayOf("example_mic_in_speaker_out.xml", "example_rfcomm_in_audio_out.xml", "standalone.xml", "rfcomm.xml")
                    for (idx in fileListIn.indices) {
                        val file = File(SystemStatus.AFExConfigFolder.absolutePath + File.separator + fileListOut[idx])
                        val inputStream = resources.openRawResource(fileListIn[idx])
                        val buffer = ByteArray(inputStream.available())
                        inputStream.read(buffer)
                        val outStream: OutputStream = FileOutputStream(file)
                        outStream.write(buffer)
                    }
                } catch (e: IOException) {
                }
            }
            FileIO().scanQuestOptions()
            doBindService()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkPermission()
                } else {
                    Toast.makeText(this, "All Permissions must be granted", Toast.LENGTH_LONG).show()
                    finish()
                }
                return
            }
        }
    }

    private fun setDefaultCosuPolicies(active: Boolean) {
        // set user restrictions
        setUserRestriction(UserManager.DISALLOW_ADD_USER, active)
        setUserRestriction(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA, active)
        setUserRestriction(UserManager.DISALLOW_ADJUST_VOLUME, false)
        setUserRestriction(UserManager.DISALLOW_CREATE_WINDOWS, active)
        Log.i(TAG, "KIOSK MODE: $active")
    }

    private fun setUserRestriction(restriction: String, disallow: Boolean) {
        if (disallow) {
            mDevicePolicyManager!!.addUserRestriction((mAdminComponentName)!!, restriction)
        } else {
            mDevicePolicyManager!!.clearUserRestriction((mAdminComponentName)!!, restriction)
        }
    }

    private fun startQuestionnaire() {
        val InfoTextView = findViewById<TextView>(R.id.InfoTextView)
        if ((controlService != null) && InfoTextView.isEnabled && (isLocked == false)) {
            isLocked = true
            controlService!!.systemStatus.setActiveActivity(ActiviyRequestCode.QuestionnaireActivity)
            automaticQuestTimer = Long.MIN_VALUE
            setInfoTextView(false)
            InfoTextView.setText(R.string.pleaseWait)
            val QuestionaireIntent = Intent(controlService, QuestionnaireActivity::class.java)
            QuestionaireIntent.putExtra("forceAnswer", controlService!!.systemStatus.Preferences().forceAnswer())
            QuestionaireIntent.putExtra("isAdmin", controlService!!.systemStatus.Preferences().isAdmin)
            QuestionaireIntent.putExtra("clientID", controlService!!.systemStatus.Preferences().clientID())
            QuestionaireIntent.putExtra("selectedQuest", controlService!!.systemStatus.Preferences().selectedQuest())
            QuestionaireIntent.putExtra("motivation", questionnaireMotivation.toString())
            questionnaireMotivation = QuestionnaireMotivation.Manual
            startActivityForResult(QuestionaireIntent, ActiviyRequestCode.QuestionnaireActivity.ordinal)
        } else if (controlService == null) LogIHAB.log("startQuestionnaire()")
    }

    private fun doBindService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, ControlService::class.java))
        } else {
            startService(Intent(this, ControlService::class.java))
        }
    }

    @Synchronized
    override fun onResume() {
        super.onResume()
        isLocked = true
        bindService(Intent(this@MainActivity, ControlService::class.java), mServiceConnection, BIND_AUTO_CREATE)
        mIsBound = true
        checkWifi()
        val lockUntilResumeComplete = Handler(Looper.getMainLooper())
        lockUntilResumeComplete.postDelayed(object : Runnable {
            @Synchronized
            override fun run() {
                if (controlService != null) {
                    controlService!!.systemStatus.setActiveActivity(ActiviyRequestCode.MainActivity)
                    isLocked = false
                } else lockUntilResumeComplete.postDelayed(this, 10)
            }
        }, 10)
    }

    private fun checkWifi() {
        val wifiManager = getSystemService(WIFI_SERVICE) as WifiManager
        findViewById<View>(R.id.Action_Wifi).visibility = View.INVISIBLE
        wifiActivated = false
        if (wifiManager.isWifiEnabled && controlService != null) {
            wifiActivated = true
            if (controlService!!.systemStatus.Preferences().isAdmin) {
                findViewById<View>(R.id.Action_Wifi).setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View) {
                        Toast.makeText(this@MainActivity, "Warning: Wifi should be disabled for optimal data transmission!", Toast.LENGTH_LONG).show()
                    }
                })
            } else wifiManager.isWifiEnabled = false
        }
    }

    override fun onPause() {
        super.onPause()
        //if (controlService != null)
        //    controlService.startForeground();
        doUnbindService()
    }

    private fun doUnbindService() {
        if (mIsBound) {
            unbindService(mServiceConnection)
            mIsBound = false
        }
    }

    private fun showPreferences(show: Boolean) {
        if (show && !isLocked) {
            isLocked = true
            controlService!!.systemStatus.setActiveActivity(ActiviyRequestCode.PreferencesActivity)
            val InfoTextView = findViewById<TextView>(R.id.InfoTextView)
            setInfoTextView(false)
            InfoTextView.setText(R.string.pleaseWait)
            val intent = Intent(controlService, PreferencesActivity::class.java)
            intent.putExtra("isDeviceOwner", controlService!!.systemStatus.Preferences().isDeviceOwner)
            startActivityForResult(intent, ActiviyRequestCode.PreferencesActivity.ordinal)
        }
    }

    private fun createHelpScreen() {
        if (isLocked == false) {
            isLocked = true
            controlService!!.systemStatus.setActiveActivity(ActiviyRequestCode.HelpActiviy)
            val InfoTextView = findViewById<TextView>(R.id.InfoTextView)
            setInfoTextView(false)
            InfoTextView.setText(R.string.pleaseWait)
            startActivity(Intent(this, Help::class.java))
        }
    }

    // KIOSK MODE
    // adb shell dpm set-device-owner com.iha.olmega_mobilesoftware_v2/.AdminReceiver .
    private fun checkIsDeviceOwner() {
        val deviceAdmin = ComponentName(this@MainActivity, AdminReceiver::class.java)
        mDevicePolicyManager = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        try {
            mDevicePolicyManager!!.setLockTaskPackages(deviceAdmin, arrayOf(packageName))
            mAdminComponentName = deviceAdmin
            mDevicePolicyManager = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
            controlService!!.systemStatus.Preferences().isDeviceOwner = true
        } catch (e: Exception) {
            controlService!!.systemStatus.Preferences().isDeviceOwner = false
        }
        if (controlService!!.systemStatus.Preferences().isDeviceOwner) {
            try {
                setDefaultCosuPolicies(!controlService!!.systemStatus.Preferences().isAdmin)
                setKioskMode(!controlService!!.systemStatus.Preferences().isAdmin)
                controlService!!.systemStatus.Preferences().isInKioskMode = !controlService!!.systemStatus.Preferences().isAdmin
            } catch (e: Exception) {
                controlService!!.systemStatus.Preferences().isInKioskMode = false
            }
        }
    }

    private fun setKioskMode(enabled: Boolean) {
        if (enabled) {
            if (mDevicePolicyManager!!.isLockTaskPermitted(this.packageName)) {
                startLockTask()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.setDecorFitsSystemWindows(false)
                    if (window.insetsController != null) {
                        window.insetsController!!.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                        window.insetsController!!.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    }
                } else {
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                }
            } else {
                stopLockTask()
                Toast.makeText(this, "Kiosk not permitted", Toast.LENGTH_LONG).show()
            }
        } else {
            stopLockTask()
        }
    }

    // This disables the Volume Buttons
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        Log.e(TAG, "EVENT: " + event.keyCode)
        if (blockedKeys.contains(event.keyCode) && !controlService!!.systemStatus.Preferences().isAdmin) {
            return true
        } else if ((event.keyCode == KeyEvent.KEYCODE_POWER)) { // && !controlService.systemStatus.Preferences().isAdmin() && !controlService.systemStatus.Preferences().isInKioskMode) {
            Log.e(TAG, "POWER BUTTON WAS PRESSED")
            return super.dispatchKeyEvent(event)
        } else {
            return super.dispatchKeyEvent(event)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        // Little hack since the Power button seems to be inaccessible at this point
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus && (controlService != null) && !controlService!!.systemStatus.Preferences().isAdmin) {
            val closeDialog = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
            sendBroadcast(closeDialog)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            setKioskMode(true)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (controlService != null) {
            if (requestCode == ActiviyRequestCode.DEVICE_ADMIN.ordinal) {
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this@MainActivity, "You have enabled the Admin Device features", Toast.LENGTH_SHORT).show()
                    checkIsDeviceOwner()
                    //if (!controlService.systemStatus.Preferences().isAdmin())
                    //    setKioskMode();
                } else {
                    Toast.makeText(this@MainActivity, "Problem to enable the Admin Device features", Toast.LENGTH_SHORT).show()
                }
            } else if (requestCode == ActiviyRequestCode.QuestionnaireActivity.ordinal) { // && resultCode == Activity.RESULT_OK) {
                questionnaireMotivation = QuestionnaireMotivation.Manual
                controlService!!.systemStatus.ResetAutomaticQuestionaireTimer()
            } else if (requestCode == ActiviyRequestCode.PreferencesActivity.ordinal && resultCode == RESULT_OK) {
                if (data!!.getBooleanExtra("disableDeviceAdmin", false)) {
                    try {
                        mDevicePolicyManager = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
                        mDevicePolicyManager!!.clearDeviceOwnerApp(packageName)
                        Toast.makeText(this, "Removing DeviceAdmin successful!", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Toast.makeText(this, "Removing DeviceAdmin not successful!", Toast.LENGTH_LONG).show()
                    }
                }
                if (data.getBooleanExtra("disableDeviceAdmin", false)) {
                    try {
                        mDevicePolicyManager = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
                        mDevicePolicyManager!!.clearDeviceOwnerApp(packageName)
                        Toast.makeText(this, "Removing DeviceAdmin successful!", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Toast.makeText(this, "Removing DeviceAdmin not successful!", Toast.LENGTH_LONG).show()
                    }
                }
                if (data.getBooleanExtra("enableDeviceAdmin", false)) {
                    val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, ComponentName(this, AdminReceiver::class.java))
                    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Device Admin for Kiosk Mode")
                    startActivityForResult(intent, ActiviyRequestCode.DEVICE_ADMIN.ordinal)
                }
                if (data.getBooleanExtra("killAppAndService", false)) {
                    if (controlService!!.systemStatus.Preferences().isInKioskMode) stopLockTask()
                    stopService(Intent(this, ControlService::class.java))
                    controlService!!.stopForeground(true)
                    doUnbindService()
                    //Toast.makeText(MainActivity.this, "App and Service killed!", Toast.LENGTH_LONG).show();
                    finish()
                    System.exit(1)
                }
                if (data.getStringExtra("installNewApp") != null) {
                    stopService(Intent(this, ControlService::class.java))
                    finish()
                    val intent: Intent
                    val apk = File(data.getStringExtra("installNewApp"))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        val apkURI = FileProvider.getUriForFile(applicationContext, packageName + ".provider", apk)
                        intent = Intent(Intent.ACTION_VIEW)
                        intent.data = apkURI
                        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    } else {
                        val apkUri = Uri.fromFile(apk)
                        intent = Intent(Intent.ACTION_VIEW)
                        intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(intent)
                }
            }
        } else LogIHAB.log("controlService = NULL (onActivityResult(...), requestCode: $requestCode)")
    }

    private fun setInfoTextView(highlight: Boolean) {
        val tempView = findViewById<TextView>(R.id.InfoTextView)
        if (highlight) {
            tempView.setBackgroundResource(R.color.JadeRed)
            tempView.setTextColor(ContextCompat.getColor(applicationContext, R.color.BackgroundColor))
        } else {
            tempView.setBackgroundColor(Color.TRANSPARENT)
            tempView.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
        }
        tempView.invalidate()
    }

    override fun onBackPressed() {
        if (controlService!!.systemStatus.Preferences().isAdmin || !controlService!!.systemStatus.Preferences().isInKioskMode) super.onBackPressed()
    }

   companion object {
        var appContext: Context? = null
            private set
    }
}