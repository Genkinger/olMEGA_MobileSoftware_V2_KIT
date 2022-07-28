package com.iha.olmega_mobilesoftware_v2

import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.iha.olmega_mobilesoftware_v2.Bluetooth.BluetoothSPP
import com.iha.olmega_mobilesoftware_v2.Bluetooth.BluetoothSPP.BluetoothConnectionListener
import com.iha.olmega_mobilesoftware_v2.Bluetooth.BluetoothState

class LinkDeviceHelper : AppCompatActivity() {
    private val TAG = this.javaClass.simpleName
    lateinit var bt: BluetoothSPP
    private var pairedDevices = mutableListOf<BluetoothDevice>()
    private var linkHelperBluetoothState: LinkHelperBluetoothStates? = null
    private var BluetoothIsConnected = false
    private var BluetoothHasData = false
    private var maxConnectionTrialTimeout: Long = 0
    private var currentConnectionTryTimeout: Long = 0
    private var currentDeviceId = 0
    private val timeOut = 30

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_link_device_helper)
        findViewById<View>(R.id.button_Link).setOnClickListener { linkPossibleDevices() }
        findViewById<View>(R.id.buttonClose).setOnClickListener { finish() }
        initBluetooth()
    }

    private fun initBluetooth() {
        findViewById<View>(R.id.button_Link).isEnabled = false
        for (device in BluetoothAdapter.getDefaultAdapter().bondedDevices) {
            pairedDevices.add(device)
        }
        bt = BluetoothSPP(applicationContext)
        var delay = 2000
        if (!bt.isBluetoothEnabled) {
            bt.enable()
            delay = 5000
        }
        Handler(Looper.getMainLooper()).postDelayed({
            bt.cancelDiscovery()
            bt.setBluetoothConnectionListener(object : BluetoothConnectionListener {
                override fun onDeviceConnected(name: String?, address: String?) {
                    BluetoothIsConnected = true
                }

                override fun onDeviceDisconnected() {
                    BluetoothIsConnected = false
                }

                override fun onDeviceConnectionFailed() {
                    BluetoothIsConnected = false
                }
            })
            //bt.setBluetoothStateListener(state -> { });
            bt.setOnDataReceivedListener(object: BluetoothSPP.OnDataReceivedListener{
                override fun onDataReceived(data: ByteArray?, message: String?) {
                    BluetoothHasData = true
                }
            })
            findViewById<View>(R.id.button_Link).isEnabled = true
            (findViewById<View>(R.id.InfoText) as TextView).setText(R.string.LinkingText)
        }, delay.toLong())
    }

    public override fun onStart() {
        super.onStart()
    }

    private fun linkPossibleDevices() {
        (findViewById<View>(R.id.InfoText) as TextView).setText(R.string.pleaseWait)
        findViewById<View>(R.id.button_Link).isEnabled = false
        bt.setupService()
        bt.startService(BluetoothState.DEVICE_OTHER)
        currentDeviceId = 0
        linkHelperBluetoothState = LinkHelperBluetoothStates.Disconnected
        maxConnectionTrialTimeout = System.currentTimeMillis() + timeOut * 1000
        currentConnectionTryTimeout = System.currentTimeMillis() + 4 * 1000
        val connectionHandler = Handler()
        connectionHandler.postDelayed(object : Runnable {
            override fun run() {
                val newTimer: Long = 200
                when (linkHelperBluetoothState) {
                    LinkHelperBluetoothStates.Disconnected -> {
                        if (pairedDevices.size > 0) bt.connect(pairedDevices[currentDeviceId].address) else {
                            currentDeviceId = 0
                            pairedDevices = ArrayList()
                            for (device in BluetoothAdapter.getDefaultAdapter().bondedDevices) {
                                pairedDevices.add(device)
                            }
                        }
                        linkHelperBluetoothState = LinkHelperBluetoothStates.Connecting
                    }
                    LinkHelperBluetoothStates.Connecting -> if (System.currentTimeMillis() > currentConnectionTryTimeout) {
                        linkHelperBluetoothState = LinkHelperBluetoothStates.Disconnecting
                    } else if (BluetoothIsConnected) {
                        bt.send("STOREMAC", false)
                        bt.send("STOREMAC", false)
                        linkHelperBluetoothState = LinkHelperBluetoothStates.Disconnecting
                    }
                    LinkHelperBluetoothStates.Disconnecting -> {
                        if (BluetoothIsConnected) bt.send("STOREMAC", false)
                        //bt.disconnect();
                        linkHelperBluetoothState = LinkHelperBluetoothStates.Disconnected
                        currentDeviceId = (currentDeviceId + 1) % pairedDevices.size
                    }
                    else -> {}
                }
                if (System.currentTimeMillis() > maxConnectionTrialTimeout || pairedDevices.isEmpty()) {
                    //bt.getBluetoothService().stop();
                    //bt.disconnect();
                    bt.stopService()
                    val handler = Handler()
                    handler.postDelayed({
                        BluetoothHasData = false
                        BluetoothIsConnected = false
                        checkForStableConnection()
                    }, 5000)
                } else connectionHandler.postDelayed(this, newTimer)
            }
        }, 100)
    }

    private fun checkForStableConnection() {
        bt.setupService()
        bt.startService(BluetoothState.DEVICE_OTHER)
        findViewById<View>(R.id.button_Link).isEnabled = false
        maxConnectionTrialTimeout = System.currentTimeMillis() + timeOut * 1000
        val waitForConnectionHandler = Handler()
        waitForConnectionHandler.postDelayed(object : Runnable {
            override fun run() {
                bt.send("GC", false)
                if (BluetoothHasData) {
                    //bt.getBluetoothService().stop();
                    //bt.disconnect();
                    bt.stopService()
                    AlertDialog.Builder(this@LinkDeviceHelper, R.style.SwipeDialogTheme)
                            .setTitle(R.string.app_name)
                            .setMessage("Linking was successfull!")
                            .setPositiveButton(R.string.buttonTextOkay) { _,_ -> finish() }
                            .setCancelable(false)
                            .show()
                } else if (System.currentTimeMillis() > maxConnectionTrialTimeout) {
                    //bt.getBluetoothService().stop();
                    //bt.disconnect();
                    bt.stopService()
                    AlertDialog.Builder(this@LinkDeviceHelper, R.style.SwipeDialogTheme)
                            .setTitle(R.string.app_name)
                            .setMessage("Linking was not successfull! Please make sure the device has been paired and retry!")
                            .setPositiveButton(R.string.buttonTextOkay) { _, _ ->
                                (findViewById<View>(R.id.InfoText) as TextView).setText(R.string.LinkingText)
                                findViewById<View>(R.id.button_Link).isEnabled = true
                            }
                            .setCancelable(false)
                            .show()
                } else waitForConnectionHandler.postDelayed(this, 200)
            }
        }, 100)
    }

    override fun onDestroy() {
        super.onDestroy()
        bt.stopService()
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBluetoothAdapter.isEnabled) {
            mBluetoothAdapter.disable()
        }
    }
}