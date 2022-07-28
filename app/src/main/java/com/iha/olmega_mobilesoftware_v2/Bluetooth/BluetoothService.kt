/*
 * Copyright (C) 2009 The Android Open Source Project
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
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

@SuppressLint("NewApi")
class BluetoothService(context: Context?, handler: Handler?) {
    // Member fields
    private val mAdapter: BluetoothAdapter?
    private val mHandler: Handler?
    private var mSecureAcceptThread: AcceptThread? = null
    private var mConnectThread: ConnectThread? = null
    private var mConnectedThread: ConnectedThread? = null
    private var mState: Int
    private var isAndroid = BluetoothState.DEVICE_ANDROID
    var BluetoothDevice_MAC: String? = ""// Give the new state to the Handler so the UI Activity can update

    // Set the current state of the chat connection
    // state : An integer defining the current connection state
    // Return the current connection state.
    @get:Synchronized
    @set:Synchronized
    var state: Int
        get() = mState
        private set(state) {
            Log.d(TAG, "setState() $mState -> $state")
            mState = state

            // Give the new state to the Handler so the UI Activity can update
            mHandler!!.obtainMessage(BluetoothState.MESSAGE_STATE_CHANGE, state, -1).sendToTarget()
        }

    // Start the chat service. Specifically start AcceptThread to begin a
    // session in listening (server) mode. Called by the Activity onResume()
    @Synchronized
    fun start(isAndroid: Boolean) {
        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }
        state = BluetoothState.STATE_LISTEN

        // Start the thread to listen on a BluetoothServerSocket
        if (mSecureAcceptThread == null) {
            mSecureAcceptThread = AcceptThread(isAndroid)
            mSecureAcceptThread!!.start()
            this@BluetoothService.isAndroid = isAndroid
        }
    }

    // Start the ConnectThread to initiate a connection to a remote device
    // device : The BluetoothDevice to connect
    // secure : Socket Security type - Secure (true) , Insecure (false)
    @Synchronized
    fun connect(device: BluetoothDevice?) {
        BluetoothDevice_MAC = device!!.address
        // Cancel any thread attempting to make a connection
        if (mState == BluetoothState.STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread!!.cancel()
                mConnectThread = null
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }

        // Start the thread to connect with the given device
        mConnectThread = ConnectThread(device)
        mConnectThread!!.start()
        state = BluetoothState.STATE_CONNECTING
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    @Synchronized
    fun connected(socket: BluetoothSocket?, device: BluetoothDevice?, socketType: String?) {
        BluetoothDevice_MAC = device!!.address
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread!!.cancel()
            mSecureAcceptThread = null
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = ConnectedThread(socket, socketType)
        mConnectedThread!!.start()

        // Send the name of the connected device back to the UI Activity
        val msg = mHandler!!.obtainMessage(BluetoothState.MESSAGE_DEVICE_NAME)
        val bundle = Bundle()
        bundle.putString(BluetoothState.DEVICE_NAME, device.name)
        bundle.putString(BluetoothState.DEVICE_ADDRESS, device.address)
        msg.data = bundle
        mHandler.sendMessage(msg)
        state = BluetoothState.STATE_CONNECTED
    }

    // Stop all threads
    @Synchronized
    fun stop() {
        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread!!.cancel()
            mSecureAcceptThread!!.kill()
            mSecureAcceptThread = null
        }
        state = BluetoothState.STATE_NONE
    }

    // Write to the ConnectedThread in an unsynchronized manner
    // out : The bytes to write
    fun write(out: ByteArray?) {
        // Create temporary object
        var r: ConnectedThread?
        // Synchronize a copy of the ConnectedThread
        synchronized(this) {
            if (mState != BluetoothState.STATE_CONNECTED) return
            r = mConnectedThread
        }
        // Perform the write unsynchronized
        r!!.write(out)
    }

    // Indicate that the connection attempt failed and notify the UI Activity
    private fun connectionFailed() {
        // Start the service over to restart listening mode
        start(isAndroid)
    }

    // Indicate that the connection was lost and notify the UI Activity
    fun connectionLost() {
        // Start the service over to restart listening mode
        start(isAndroid)
    }

    // This thread runs while listening for incoming connections. It behaves
    // like a server-side client. It runs until a connection is accepted
    // (or until cancelled)
    private inner class AcceptThread(isAndroid: Boolean) : Thread() {
        // The local server socket
        private var mmServerSocket: BluetoothServerSocket?
        private val mSocketType: String? = null
        var isRunning = true
        override fun run() {
            name = "AcceptThread$mSocketType"
            var socket: BluetoothSocket? = null

            // Listen to the server socket if we're not connected
            while (mState != BluetoothState.STATE_CONNECTED && isRunning) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    if (mmServerSocket != null) socket = mmServerSocket!!.accept()
                } catch (e: Exception) {
                    break
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized(this@BluetoothService) {
                        when (mState) {
                            BluetoothState.STATE_LISTEN, BluetoothState.STATE_CONNECTING ->                                 // Situation normal. Start the connected thread.
                                connected(socket, socket.remoteDevice,
                                        mSocketType)
                            BluetoothState.STATE_NONE, BluetoothState.STATE_CONNECTED ->                                 // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close()
                                } catch (e: Exception) {
                                }
                        }
                    }
                }
            }
        }

        fun cancel() {
            try {
                mmServerSocket!!.close()
                mmServerSocket = null
            } catch (e: Exception) {
            }
        }

        fun kill() {
            isRunning = false
        }

        init {
            name = "BT AcceptThread"
            var tmp: BluetoothServerSocket? = null

            // Create a new listening server socket
            try {
                tmp = if (isAndroid) mAdapter!!.listenUsingRfcommWithServiceRecord(NAME_SECURE, UUID_ANDROID_DEVICE) else mAdapter!!.listenUsingRfcommWithServiceRecord(NAME_SECURE, UUID_OTHER_DEVICE)
            } catch (e: Exception) {
            }
            mmServerSocket = tmp
        }
    }

    // This thread runs while attempting to make an outgoing connection
    // with a device. It runs straight through
    // the connection either succeeds or fails
    private inner class ConnectThread(device: BluetoothDevice?) : Thread() {
        private val mmSocket: BluetoothSocket?
        private val mmDevice: BluetoothDevice?
        private val mSocketType: String? = null
        override fun run() {
            // Always cancel discovery because it will slow down a connection
            mAdapter!!.cancelDiscovery()
            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket!!.connect()
            } catch (e: Exception) {
                // Close the socket
                try {
                    mmSocket!!.close()
                } catch (e2: Exception) {
                }
                //try again
                try {
                    mmSocket!!.connect()
                } catch (e1: Exception) {
                    e1.printStackTrace()
                    connectionFailed()
                }
            }
            // Reset the ConnectThread because we're done
            synchronized(this@BluetoothService) { mConnectThread = null }
            // Start the connected thread
            connected(mmSocket, mmDevice, mSocketType)
        }

        fun cancel() {
            try {
                mmSocket!!.close()
            } catch (e: Exception) {
            }
        }

        init {
            name = "BT ConnectThread"
            mmDevice = device
            BluetoothDevice_MAC = device!!.address
            var tmp: BluetoothSocket? = null

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = if (isAndroid) device.createRfcommSocketToServiceRecord(UUID_ANDROID_DEVICE) else device.createRfcommSocketToServiceRecord(UUID_OTHER_DEVICE)
            } catch (e: Exception) {
            }
            mmSocket = tmp
        }
    }

    // This thread runs during a connection with a remote device.
    // It handles all incoming and outgoing transmissions.
    private inner class ConnectedThread(socket: BluetoothSocket?, socketType: String?) : Thread() {
        private val mmSocket: BluetoothSocket?
        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?
        override fun run() {
            val buffer = ByteArray(1024)

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    if (mmInStream != null) {
                        while (mmInStream.available() >= buffer.size) {
                            mmInStream.read(buffer, 0, buffer.size)
                            mHandler!!.obtainMessage(BluetoothState.MESSAGE_READ, buffer.size, -1, buffer.clone()).sendToTarget()
                        }
                        sleep(25)
                    } else {
                        connectionLost()
                        // Start the service over to restart listening mode
                        this@BluetoothService.start(isAndroid)
                        break
                    }
                } catch (e: IOException) {
                    connectionLost()
                    // Start the service over to restart listening mode
                    this@BluetoothService.start(isAndroid)
                    break
                } catch (e: Exception) {
                    connectionLost()
                    // Start the service over to restart listening mode
                    this@BluetoothService.start(isAndroid)
                    break
                }
            }
        }

        // Write to the connected OutStream.
        // @param buffer  The bytes to write
        fun write(buffer: ByteArray?) {
            try { /*
                byte[] buffer2 = new byte[buffer.length + 2];
                for(int i = 0 ; i < buffer.length ; i++)
                    buffer2[i] = buffer[i];
                buffer2[buffer2.length - 2] = 0x0A;
                buffer2[buffer2.length - 1] = 0x0D;*/
                mmOutStream!!.write(buffer)
                // Share the sent message back to the UI Activity
                mHandler!!.obtainMessage(BluetoothState.MESSAGE_WRITE, -1, -1, buffer).sendToTarget()
            } catch (e: IOException) {
            }
        }

        fun cancel() {
            try {
                mmSocket!!.close()
            } catch (e: Exception) {
            }
        }

        init {
            name = "BT ConnectedThread"
            mmSocket = socket
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket!!.inputStream
                tmpOut = socket.outputStream
            } catch (e: Exception) {
            }
            mmInStream = tmpIn
            mmOutStream = tmpOut
        }
    }

    companion object {
        // Debugging
        private val TAG: String? = "Bluetooth Service"

        // Name for the SDP record when creating server socket
        private val NAME_SECURE: String? = "Bluetooth Secure"

        // Unique UUID for this application
        private val UUID_ANDROID_DEVICE = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66")
        private val UUID_OTHER_DEVICE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    // Constructor. Prepares a new BluetoothChat session
    // context : The UI Activity Context
    // handler : A Handler to send messages back to the UI Activity
    init {
        mAdapter = BluetoothAdapter.getDefaultAdapter()
        mState = BluetoothState.STATE_NONE
        mHandler = handler
    }
}