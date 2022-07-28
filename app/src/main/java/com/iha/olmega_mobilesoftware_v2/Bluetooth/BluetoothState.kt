/*
 * Copyright 2014 Akexorcist
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.iha.olmega_mobilesoftware_v2.Bluetooth

object BluetoothState {
    // Constants that indicate the current connection state
    const val STATE_NONE = 0 // we're doing nothing
    const val STATE_LISTEN = 1 // now listening for incoming connections
    const val STATE_CONNECTING = 2 // now initiating an outgoing connection
    const val STATE_CONNECTED = 3 // now connected to a remote device
    const val STATE_NULL = -1 // now service is null

    // Message types sent from the BluetoothChatService Handler
    const val MESSAGE_STATE_CHANGE = 1
    const val MESSAGE_READ = 2
    const val MESSAGE_WRITE = 3
    const val MESSAGE_DEVICE_NAME = 4
    const val MESSAGE_TOAST = 5

    // Intent request codes
    const val REQUEST_CONNECT_DEVICE = 384
    const val REQUEST_ENABLE_BT = 385

    // Key names received from the BluetoothChatService Handler
    val DEVICE_NAME: String? = "device_name"
    val DEVICE_ADDRESS: String? = "device_address"
    val TOAST: String? = "toast"
    const val DEVICE_ANDROID = true
    const val DEVICE_OTHER = false

    // Return Intent extra
    var EXTRA_DEVICE_ADDRESS: String? = "device_address"
}