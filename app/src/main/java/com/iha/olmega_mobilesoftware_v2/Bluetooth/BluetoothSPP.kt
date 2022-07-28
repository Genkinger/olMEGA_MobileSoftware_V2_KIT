/*
 * Copyright (C) 2014 Akexorcist
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.iha.olmega_mobilesoftware_v2.Bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Toast

@SuppressLint("NewApi")
class BluetoothSPP(  // Context from activity which call this class
        private val mContext: Context?) {
    // Listener for Bluetooth Status & Connection
    private var mBluetoothStateListener: BluetoothStateListener? = null
    private var mDataReceivedListener: OnDataReceivedListener? = null
    private var mBluetoothConnectionListener: BluetoothConnectionListener? = null
    private var mAutoConnectionListener: AutoConnectionListener? = null

    // Local Bluetooth adapter
    var bluetoothAdapter: BluetoothAdapter? = null

    // Member object for the chat services
    var bluetoothService: BluetoothService? = null
        private set

    // Name and Address of the connected device
    var connectedDeviceName: String? = null
        private set
    var connectedDeviceAddress: String? = null
        private set
    var isAutoConnecting = false
        private set
    private var isAutoConnectionEnabled = false
    private var isConnected = false
    private var isConnecting = false
    private var isServiceRunning = false
    private var keyword: String? = ""
    private var isAndroid = BluetoothState.DEVICE_ANDROID
    private var bcl: BluetoothConnectionListener? = null
    private var c = 0

    interface BluetoothStateListener {
        fun onServiceStateChanged(state: Int)
    }

    interface OnDataReceivedListener {
        fun onDataReceived(data: ByteArray?, message: String?)
    }

    interface BluetoothConnectionListener {
        fun onDeviceConnected(name: String?, address: String?)
        fun onDeviceDisconnected()
        fun onDeviceConnectionFailed()
    }

    interface AutoConnectionListener {
        fun onAutoConnectionStarted()
        fun onNewConnection(name: String?, address: String?)
    }

    val isBluetoothAvailable: Boolean
        get() {
            try {
                if (bluetoothAdapter == null || bluetoothAdapter!!.address == null) return false
            } catch (e: NullPointerException) {
                return false
            }
            return true
        }
    val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter!!.isEnabled
    val isServiceAvailable: Boolean
        get() = bluetoothService != null

    fun startDiscovery(): Boolean {
        return bluetoothAdapter!!.startDiscovery()
    }

    val isDiscovery: Boolean
        get() = bluetoothAdapter!!.isDiscovering

    fun cancelDiscovery(): Boolean {
        return bluetoothAdapter!!.cancelDiscovery()
    }

    fun setupService() {
        bluetoothService = BluetoothService(mContext, mHandler)
    }

    val serviceState: Int
        get() = if (bluetoothService != null) bluetoothService!!.state else -1

    fun startService(isAndroid: Boolean) {
        if (bluetoothService != null) {
            if (bluetoothService!!.state == BluetoothState.STATE_NONE) {
                isServiceRunning = true
                bluetoothService!!.start(isAndroid)
                this@BluetoothSPP.isAndroid = isAndroid
            }
        }
    }

    fun stopService() {
        if (bluetoothService != null) {
            isServiceRunning = false
            bluetoothService!!.stop()
        }
        Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
            override fun run() {
                if (bluetoothService != null) {
                    isServiceRunning = false
                    bluetoothService!!.stop()
                }
            }
        }, 500)
    }

    fun setDeviceTarget(isAndroid: Boolean) {
        stopService()
        startService(isAndroid)
        this@BluetoothSPP.isAndroid = isAndroid
    }

    @SuppressLint("HandlerLeak")
    private val mHandler: Handler? = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                BluetoothState.MESSAGE_WRITE -> {}
                BluetoothState.MESSAGE_READ -> {
                    val readBuf = msg.obj as ByteArray
                    val readMessage = String(readBuf)
                    if (readBuf != null && readBuf.size > 0) {
                        if (mDataReceivedListener != null) mDataReceivedListener!!.onDataReceived(readBuf, readMessage)
                    }
                }
                BluetoothState.MESSAGE_DEVICE_NAME -> {
                    connectedDeviceName = msg.data.getString(BluetoothState.DEVICE_NAME)
                    connectedDeviceAddress = msg.data.getString(BluetoothState.DEVICE_ADDRESS)
                    if (mBluetoothConnectionListener != null) mBluetoothConnectionListener!!.onDeviceConnected(connectedDeviceName, connectedDeviceAddress)
                    isConnected = true
                }
                BluetoothState.MESSAGE_TOAST -> Toast.makeText(mContext, msg.data.getString(BluetoothState.TOAST), Toast.LENGTH_SHORT).show()
                BluetoothState.MESSAGE_STATE_CHANGE -> {
                    if (mBluetoothStateListener != null) mBluetoothStateListener!!.onServiceStateChanged(msg.arg1)
                    if (isConnected && msg.arg1 != BluetoothState.STATE_CONNECTED) {
                        if (mBluetoothConnectionListener != null) mBluetoothConnectionListener!!.onDeviceDisconnected()
                        if (isAutoConnectionEnabled) {
                            isAutoConnectionEnabled = false
                            autoConnect(keyword)
                        }
                        isConnected = false
                        connectedDeviceName = null
                        connectedDeviceAddress = null
                    }
                    if (!isConnecting && msg.arg1 == BluetoothState.STATE_CONNECTING) {
                        isConnecting = true
                    } else if (isConnecting) {
                        if (msg.arg1 != BluetoothState.STATE_CONNECTED) {
                            if (mBluetoothConnectionListener != null) mBluetoothConnectionListener!!.onDeviceConnectionFailed()
                        }
                        isConnecting = false
                    }
                }
            }
        }
    }

    fun stopAutoConnect() {
        isAutoConnectionEnabled = false
    }

    fun connect(data: Intent?) {
        val address = data!!.extras!!.getString(BluetoothState.EXTRA_DEVICE_ADDRESS)
        val device = bluetoothAdapter!!.getRemoteDevice(address)
        bluetoothService!!.connect(device)
    }

    fun connect(address: String?) {
        val device = bluetoothAdapter!!.getRemoteDevice(address)
        bluetoothService!!.connect(device)
    }

    fun disconnect() {
        if (bluetoothService != null) {
            isServiceRunning = false
            bluetoothService!!.stop()
            if (bluetoothService!!.state == BluetoothState.STATE_NONE) {
                isServiceRunning = true
                bluetoothService!!.start(isAndroid)
            }
        }
    }

    fun setBluetoothStateListener(listener: BluetoothStateListener) {
        mBluetoothStateListener = listener
    }

    fun setOnDataReceivedListener(listener: OnDataReceivedListener?) {
        mDataReceivedListener = listener
    }

    fun setBluetoothConnectionListener(listener: BluetoothConnectionListener?) {
        mBluetoothConnectionListener = listener
    }

    fun setAutoConnectionListener(listener: AutoConnectionListener?) {
        mAutoConnectionListener = listener
    }

    fun enable() {
        bluetoothAdapter!!.enable()
    }

    fun send(data: ByteArray?, CRLF: Boolean) {
        if (bluetoothService!!.state == BluetoothState.STATE_CONNECTED) {
            if (CRLF) {
                val data2 = ByteArray(data!!.size + 2)
                for (i in data.indices) data2[i] = data[i]
                data2[data2.size - 2] = 0x0A
                data2[data2.size - 1] = 0x0D
                bluetoothService!!.write(data2)
            } else {
                bluetoothService!!.write(data)
            }
        }
    }

    fun send(data: String?, CRLF: Boolean) {
        var data = data
        if (bluetoothService!!.state == BluetoothState.STATE_CONNECTED) {
            if (CRLF) data += "\r\n"
            bluetoothService!!.write(data!!.toByteArray())
        }
    }

    val pairedDeviceName: Array<String?>?
        get() {
            var c = 0
            val devices = bluetoothAdapter!!.bondedDevices
            val name_list = arrayOfNulls<String?>(devices!!.size)
            for (device in devices) {
                name_list[c] = device!!.name
                c++
            }
            return name_list
        }
    val pairedDeviceAddress: Array<String?>?
        get() {
            var c = 0
            val devices = bluetoothAdapter!!.bondedDevices
            val address_list = arrayOfNulls<String?>(devices!!.size)
            for (device in devices) {
                address_list[c] = device!!.address
                c++
            }
            return address_list
        }

    fun autoConnect(keywordName: String?) {
        if (!isAutoConnectionEnabled) {
            keyword = keywordName
            isAutoConnectionEnabled = true
            isAutoConnecting = true
            if (mAutoConnectionListener != null) mAutoConnectionListener!!.onAutoConnectionStarted()
            val arr_filter_address = ArrayList<String?>()
            val arr_filter_name = ArrayList<String?>()
            val arr_name = pairedDeviceName
            val arr_address = pairedDeviceAddress
            for (i in arr_name!!.indices) {
                if (arr_name[i]!!.contains(keywordName!!)) {
                    arr_filter_address.add(arr_address!![i])
                    arr_filter_name.add(arr_name[i])
                }
            }
            bcl = object : BluetoothConnectionListener {
                override fun onDeviceConnected(name: String?, address: String?) {
                    bcl = null
                    isAutoConnecting = false
                }

                override fun onDeviceDisconnected() {}
                override fun onDeviceConnectionFailed() {
                    Log.e("CHeck", "Failed")
                    if (isServiceRunning) {
                        if (isAutoConnectionEnabled) {
                            c++
                            if (c >= arr_filter_address.size) c = 0
                            connect(arr_filter_address[c])
                            Log.e("CHeck", "Connect")
                            if (mAutoConnectionListener != null) mAutoConnectionListener!!.onNewConnection(arr_filter_name[c], arr_filter_address[c])
                        } else {
                            bcl = null
                            isAutoConnecting = false
                        }
                    }
                }
            }
            setBluetoothConnectionListener(bcl)
            c = 0
            if (mAutoConnectionListener != null) mAutoConnectionListener!!.onNewConnection(arr_name[c], arr_address!![c])
            if (arr_filter_address.size > 0) connect(arr_filter_address[c]) else Toast.makeText(mContext, "Device name mismatch", Toast.LENGTH_SHORT).show()
        }
    }

    init {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }
}