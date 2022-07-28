package com.iha.olmega_mobilesoftware_v2


import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Handler
import android.os.Looper
import com.iha.olmega_mobilesoftware_v2.AFEx.AcousticFeatureExtraction.StageManager
import com.iha.olmega_mobilesoftware_v2.Core.FileIO
import com.iha.olmega_mobilesoftware_v2.Core.LogIHAB
import com.iha.olmega_mobilesoftware_v2.Core.XMLReader
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

class SystemStatus(private val mContext: ControlService) {
    private val TAG = this.javaClass.simpleName
    private var mReceiver: BroadcastReceiver? = null
    private val batteryStates: IntArray = mContext.resources.getIntArray(R.array.batteryStates)
    private var stageManager: StageManager? = null
    private var stageMangerState = StageManagerStates.Undefined
    private var mySystemStatusListener: SystemStatusListener? = null
    private var raiseAutomaticQuestionaire_TimerEventAt = Long.MIN_VALUE
    private val mStageStateReceiver: BroadcastReceiver
    private val preferences = Preferences(mContext.applicationContext)
    private var BatteryManagerStatus = -1
    private var lockUntilStageManagerIsRunning = false
    private val taskHandler = Handler(Looper.myLooper()!!)
    var curentActivity = ActiviyRequestCode.MainActivity
        private set
    private var lastBatteryLevel = 0f
    private var lastChargingState = false
    private var AutomaticQuestionaireIsTriggered = false
    private var refreshHandlerIsActive = false
    private val acitivyStates = AcitivyStates()
    private val stageMangerConfigFile: File
        private get() = File(AFExConfigFolder.toString() + File.separator + Preferences().inputProfile())

    private val AutomaticQuestionnaireRunnable: Runnable = object : Runnable {
        @Synchronized
        override fun run() {
            updateAutomaticQuestionnaireTimer()
            taskHandler.postDelayed(this, 1000)
        }
    }

    init {
        if (mReceiver != null) mContext.unregisterReceiver(mReceiver)
        mReceiver = BatteryBroadcastReceiver()
        mContext.registerReceiver(mReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        mStageStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == "StageState" && stageManager != null && stageManager!!.isRunning && curentActivity != ActiviyRequestCode.PreferencesActivity && acitivyStates.profileState != States.values()[intent.getIntExtra("currentState", States.Connected.ordinal)]) {
                    acitivyStates.profileState = States.values()[intent.getIntExtra("currentState", States.Connected.ordinal)]
                    Refresh()
                } else if (intent.action == "CalibrationValuesError" && curentActivity != ActiviyRequestCode.PreferencesActivity) {
                    acitivyStates.showCalibrationValuesError = intent.getBooleanExtra("Value", false)
                    Refresh()
                }
            }
        }
        taskHandler.postDelayed(AutomaticQuestionnaireRunnable, 1000)
        val filter = IntentFilter("StageState")
        filter.priority = 999
        mContext.registerReceiver(mStageStateReceiver, filter)
        val filter2 = IntentFilter("CalibrationValuesError")
        filter2.priority = 999
        mContext.registerReceiver(mStageStateReceiver, filter2)
        val bm = mContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        acitivyStates.batteryLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).toFloat()
    }

    fun Preferences(): Preferences {
        return preferences
    }

    fun setSystemStatusListener(listener: SystemStatusListener?) {
        mySystemStatusListener = listener
        Refresh()
    }

    @Synchronized
    fun Refresh() {
        if (lockUntilStageManagerIsRunning == false) {
            acitivyStates.InfoText = mContext.resources.getString(R.string.pleaseWait)
            acitivyStates.NextQuestText = ""
            acitivyStates.questionaireEnabled = false
            preferences.configHasErrors = false
            acitivyStates.isCharging = BatteryManagerStatus == BatteryManager.BATTERY_STATUS_CHARGING || BatteryManagerStatus == BatteryManager.BATTERY_STATUS_FULL
            if (lastChargingState != acitivyStates.isCharging) {
                if (acitivyStates.isCharging) LogIHAB.log("StateCharging")
                lastChargingState = acitivyStates.isCharging
            }
            acitivyStates.BatteryState = if (acitivyStates.batteryLevel <= batteryStates[1]) BatteryStates.Critical else if (acitivyStates.batteryLevel >= batteryStates[1] && acitivyStates.batteryLevel <= batteryStates[0]) BatteryStates.Warning else BatteryStates.Normal
            // Charging State
            if (curentActivity != ActiviyRequestCode.MainActivity) raiseAutomaticQuestionaire_TimerEventAt = Long.MIN_VALUE
            if (!preferences.isAdmin && !Preferences().isInKioskMode && Preferences().isKioskModeNecessary || acitivyStates.isCharging && Preferences().usbCutsConnection() || curentActivity == ActiviyRequestCode.PreferencesActivity) {
                if (curentActivity != ActiviyRequestCode.PreferencesActivity && !Preferences().isInKioskMode && Preferences().isKioskModeNecessary) {
                    acitivyStates.InfoText = mContext.resources.getString(R.string.UnableToStartKioskMode)
                    preferences.configHasErrors = true
                }
                if (acitivyStates.isCharging && Preferences().usbCutsConnection()) acitivyStates.InfoText = mContext.resources.getString(R.string.infoCharging)
                stageMangerState = StageManagerStates.Undefined
                acitivyStates.profileState = States.Undefined
                acitivyStates.InputProfile = ""
                raiseAutomaticQuestionaire_TimerEventAt = Long.MIN_VALUE
                if (stageManager != null && stageManager!!.isRunning) {
                    val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    if (mBluetoothAdapter.isEnabled) mBluetoothAdapter.disable()
                    acitivyStates.showCalibrationValuesError = false
                    stageManager!!.stop()
                }
            } else if (!FileIO().scanForQuestionnaire(preferences.selectedQuest()!!) && preferences.useQuestionnaire()) {
                raiseAutomaticQuestionaire_TimerEventAt = Long.MIN_VALUE
                acitivyStates.InfoText = mContext.resources.getString(R.string.noQuestionnaires)
                preferences.configHasErrors = true
            } else if (acitivyStates.profileState == States.RequestDisconnection || stageManager == null || !stageManager!!.isRunning || acitivyStates.InputProfile != preferences.inputProfile() && curentActivity != ActiviyRequestCode.PreferencesActivity) {
                acitivyStates.InputProfile = ""
                acitivyStates.profileState = States.Undefined
                if (stageMangerConfigFile.exists() && stageMangerConfigFile.isFile) {
                    try {
                        acitivyStates.InputProfile = preferences.inputProfile()!!
                        acitivyStates.profileState = States.Connected
                        stageMangerState = StageManagerStates.Running
                        startStageManager()
                    } catch (e: Exception) {
                        val sw = StringWriter()
                        e.printStackTrace(PrintWriter(sw))
                        LogIHAB.log(sw.toString())
                        stageMangerState = StageManagerStates.ConfigFileNotValid
                        e.printStackTrace()
                    }
                } else stageMangerState = StageManagerStates.NoConfigSelected
            }
            when (stageMangerState) {
                StageManagerStates.Undefined -> {}
                StageManagerStates.ConfigFileNotValid -> {
                    acitivyStates.profileState = States.Undefined
                    acitivyStates.InfoText = "Stage Manager Config-File '" + stageMangerConfigFile.absoluteFile + "' not valid!"
                    preferences.configHasErrors = true
                }
                StageManagerStates.NoConfigSelected -> {
                    acitivyStates.profileState = States.Undefined
                    acitivyStates.InfoText = "No Input Profile Config selected!"
                    preferences.configHasErrors = true
                }
                StageManagerStates.Running -> {}
            }
            if (curentActivity == ActiviyRequestCode.MainActivity && stageManager != null && stageManager!!.isRunning && acitivyStates.profileState != States.Undefined) {
                when (acitivyStates.profileState) {
                    States.Init -> acitivyStates.InfoText = "Initializing"
                    States.Connecting -> {
                        LogIHAB.log("StateConnecting")
                        acitivyStates.InfoText = mContext.resources.getString(R.string.infoConnecting)
                    }
                    States.Connected -> {
                        LogIHAB.log("StateRunning")
                        acitivyStates.InfoText = mContext.resources.getString(R.string.infoConnected)
                        if (preferences.useQuestionnaire()) {
                            acitivyStates.questionaireEnabled = true
                            acitivyStates.InfoText = mContext.resources.getString(R.string.menuText)
                            if (raiseAutomaticQuestionaire_TimerEventAt == Long.MIN_VALUE) { // Preferences().useQuestionnaireTimer() &&
                                val mXmlReader = XMLReader(mContext, preferences.selectedQuest()!!)
                                if (mXmlReader.questionnaireHasTimer) raiseAutomaticQuestionaire_TimerEventAt = System.currentTimeMillis() + mXmlReader.newTimerInterval * 1000
                                //else
                                //    raiseAutomaticQuestionaire_TimerEventAt = Long.MAX_VALUE;
                            }
                        }
                    }
                    else -> {}
                }
            }
            // Battery State
            if (acitivyStates.BatteryState == BatteryStates.Critical) acitivyStates.InfoText += """
     
     
     ${mContext.resources.getString(R.string.batteryCritical)}
     """.trimIndent() else if (acitivyStates.BatteryState == BatteryStates.Warning) acitivyStates.InfoText += """
     
     
     ${mContext.resources.getString(R.string.batteryWarning)}
     """.trimIndent()
            acitivyStates.isAutomaticQuestionaireActive = raiseAutomaticQuestionaire_TimerEventAt != Long.MAX_VALUE && raiseAutomaticQuestionaire_TimerEventAt != Long.MIN_VALUE && acitivyStates.profileState == States.Connected && curentActivity == ActiviyRequestCode.MainActivity
            if (mySystemStatusListener != null) mySystemStatusListener!!.setAcitivyStates(acitivyStates)
            lockUntilStageManagerIsRunning = false
            updateAutomaticQuestionnaireTimer()
        } else if (refreshHandlerIsActive == false) {
            refreshHandlerIsActive = true
            val refreshHandler = Handler(Looper.getMainLooper())
            refreshHandler.postDelayed(object : Runnable {
                @Synchronized
                override fun run() {
                    if (lockUntilStageManagerIsRunning) refreshHandler.postDelayed(this, 100) else {
                        refreshHandlerIsActive = false
                        Refresh()
                    }
                }
            }, 100)
        }
    }

    fun ResetAutomaticQuestionaireTimer() {
        raiseAutomaticQuestionaire_TimerEventAt = Long.MIN_VALUE
        acitivyStates.isAutomaticQuestionaireActive = false
        Refresh()
    }

    private fun startStageManager() {
        acitivyStates.showCalibrationValuesError = false
        lockUntilStageManagerIsRunning = true
        if (stageManager != null && stageManager!!.isRunning) stageManager!!.stop()
        stageManager = StageManager(mContext, stageMangerConfigFile)
        try {
            stageManager!!.start()
        } catch (e: Exception) {
            if (stageManager != null && stageManager!!.isRunning) stageManager!!.stop()
            throw e
        }
    }

    fun setActiveActivity(activity: ActiviyRequestCode) {
        if (activity == ActiviyRequestCode.QuestionnaireActivity) LogIHAB.log("StateQuest")
        curentActivity = activity
        Refresh()
    }

    private fun updateAutomaticQuestionnaireTimer() {
        if (mySystemStatusListener != null) {
            if (acitivyStates.isAutomaticQuestionaireActive && raiseAutomaticQuestionaire_TimerEventAt != Long.MIN_VALUE && raiseAutomaticQuestionaire_TimerEventAt != Long.MAX_VALUE) {
                val mCountDownString = mContext.resources.getString(R.string.timeRemaining)
                val mTempTextCountDownRemaining = mCountDownString.split("%").toTypedArray()
                val remaining = (raiseAutomaticQuestionaire_TimerEventAt - System.currentTimeMillis()) / 1000
                val hoursRemaining = remaining / 60 / 60
                val minutesRemaining = (remaining - hoursRemaining * 60 * 60) / 60
                val secondsRemaining = remaining - hoursRemaining * 60 * 60 - minutesRemaining * 60
                if (preferences.showQuestionnaireTimer()) {
                    acitivyStates.NextQuestText = String.format("%s%02d%s%02d%s%02d%s",
                            mTempTextCountDownRemaining[0], hoursRemaining,
                            mTempTextCountDownRemaining[1], minutesRemaining,
                            mTempTextCountDownRemaining[2], secondsRemaining,
                            mTempTextCountDownRemaining[3])
                } else acitivyStates.NextQuestText = ""
                mySystemStatusListener!!.updateAutomaticQuestionnaireTimer(acitivyStates.NextQuestText, remaining)
                if (raiseAutomaticQuestionaire_TimerEventAt - System.currentTimeMillis() < 10 * 1000 && raiseAutomaticQuestionaire_TimerEventAt - System.currentTimeMillis() > 5 * 1000) mContext.startMainActivity(true) else if (raiseAutomaticQuestionaire_TimerEventAt - System.currentTimeMillis() <= 0) {
                    if (AutomaticQuestionaireIsTriggered == false) LogIHAB.log("StateProposing")
                    AutomaticQuestionaireIsTriggered = true
                } else AutomaticQuestionaireIsTriggered = false
            } else mySystemStatusListener!!.updateAutomaticQuestionnaireTimer("", Long.MIN_VALUE)
        }
    }

    /*
    private void startAutomaticQuestionnaireTimer(long timer) {
        if (AutomaticQuestionnaireTimer != null) {
            AutomaticQuestionnaireTimer.cancel();
            AutomaticQuestionnaireTimer = null;
        }
        Looper.prepare();
        AutomaticQuestionnaireTimer = new CountDownTimer(timer, 1000) {
            public void onTick(long millisUntilFinished) {
                Looper.loop();
                Log.d("TIMER", "TEST");
                Refresh();
                if (raiseAutomaticQuestionaire_TimerEventAt - System.currentTimeMillis() < 10 * 1000 && raiseAutomaticQuestionaire_TimerEventAt - System.currentTimeMillis() > 5 * 1000)
                    mContext.startMainActivity(true);
            }

            public void onFinish() {
                if (mySystemStatusListener != null)
                    mySystemStatusListener.startAutomaticQuestionnaire();
                raiseAutomaticQuestionaire_TimerEventAt = -1;
            }
        }.start();
    }
*/
    fun onDestroy() {
        if (mReceiver != null) mContext.unregisterReceiver(mReceiver)
        mContext.unregisterReceiver(mStageStateReceiver)
        taskHandler.removeCallbacks(AutomaticQuestionnaireRunnable)
        if (stageManager != null && stageManager!!.isRunning) stageManager!!.stop()
        Preferences().onDestroy()
    }

    private inner class BatteryBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            BatteryManagerStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            acitivyStates.batteryLevel = intent.getIntExtra("level", 0).toFloat()
            if (lastBatteryLevel != acitivyStates.batteryLevel) {
                LogIHAB.log("battery level: " + acitivyStates.batteryLevel / 100)
                lastBatteryLevel = acitivyStates.batteryLevel
            }
            Refresh()
        }
    }

    abstract class SystemStatusListener {
        open fun setAcitivyStates(acitivyStates: AcitivyStates?) {}
        open fun updateAutomaticQuestionnaireTimer(Message: String?, TimeRemaining: Long) {}
    }

    companion object {
        var AFExConfigFolder = File(FileIO.folderPath + File.separator + "AFEx")
    }

}