package com.iha.olmega_mobilesoftware_v2.AFEx.AcousticFeatureExtraction

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.provider.Settings.Secure
import android.util.Log
import com.iha.olmega_mobilesoftware_v2.Bluetooth.BluetoothSPP
import com.iha.olmega_mobilesoftware_v2.Bluetooth.BluetoothSPP.BluetoothConnectionListener
import com.iha.olmega_mobilesoftware_v2.Bluetooth.BluetoothState
import com.iha.olmega_mobilesoftware_v2.Core.LogIHAB
import com.iha.olmega_mobilesoftware_v2.Core.RingBuffer
import com.iha.olmega_mobilesoftware_v2.States
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.time.Instant
import kotlin.experimental.xor

internal enum class initState {
    UNINITIALIZED, WAITING_FOR_CALIBRATION_VALUES, WAITING_FOR_AUDIOTRANSMISSION, INITIALIZED
}

class StageRFCOMM(parameter: HashMap<*, *>?) : Stage(parameter!!) {
    var checksum: Byte = 0
    private var restartStages = false
    private var bt: BluetoothSPP? = null
    private val alivePingTimeout = 100
    private var ValidBlocksFeatureBufferIdx = 0
    private var BufferIdx = 0
    private val frames: Int
    private var lastBlockNumber = 0
    private var currBlockNumber = 0
    private var additionalBytesCount = 0
    private var lostBlockCount = 0
    private val AudioBufferSize = block_size * 4
    private val millisPerBlock = block_size * 1000 / 16000
    private var contiguousLostBlockCount = 0
    private var lastEmptyPackageTimer: Long = 0
    private var lastStreamTimer: Long = 0
    private var lastBluetoothPingTimer: Long = 0
    var calibValues: FloatArray? = floatArrayOf(Float.NaN, Float.NaN)
    var calibValuesInDB: FloatArray? = floatArrayOf(Float.NaN, Float.NaN)
    var HardwareIDs: Array<String?>? = arrayOf("", "")
    private val ringBuffer: RingBuffer?
    private var initializeState: initState? = null
    var emptyAudioBlock: ByteArray?
    var dataOut: Array<FloatArray>?
    var ValidBlocksFeature: Array<FloatArray?>?
    private var lastStateChange = System.currentTimeMillis()
    var ReconnectTrials = 0
    var myStageFeatureWrite: StageFeatureWrite? = null
    override fun start() {
        super.start()
        sendBroadcast(States.Init)
        initBluetooth()
    }

    var loop = true
    private fun initBluetooth() {
        LogIHAB.log("Bluetooth: Setting up StageRFCOMM")
        //Log.d(LOG, "Bluetooth: Setting up StageRFCOMM");
        if (bt == null) bt = BluetoothSPP(Stage.Companion.context)
        var delay = 2000
        if (!bt!!.isBluetoothEnabled) {
            LogIHAB.log("Bluetooth: Enable Bluetooth Adapter")
            bt!!.enable()
            delay = 5000
        }
        loop = true
        val showInitLoopHandler = Handler(Looper.getMainLooper())
        showInitLoopHandler.postDelayed(object : Runnable {
            @Synchronized
            override fun run() {
                if (loop) {
                    sendBroadcast(States.Init)
                    showInitLoopHandler.postDelayed(this, 100)
                } else sendBroadcast(States.Connecting)
            }
        }, 0)
        Handler(Looper.getMainLooper()).postDelayed({
            loop = false
            if (bt != null) {
                bt!!.cancelDiscovery()
                bt!!.setBluetoothConnectionListener(object : BluetoothConnectionListener {
                    override fun onDeviceConnected(name: String?, address: String?) {
                        setState(initState.UNINITIALIZED)
                        sendBroadcast(States.Connecting)
                        lastBlockNumber = 0
                        currBlockNumber = 0
                        additionalBytesCount = 0
                        lostBlockCount = 0
                        contiguousLostBlockCount = 0
                        checksum = 0
                        lastEmptyPackageTimer = System.currentTimeMillis()
                        lastStreamTimer = System.currentTimeMillis()
                        lastBluetoothPingTimer = System.currentTimeMillis()
                    }

                    override fun onDeviceDisconnected() {
                        setState(initState.UNINITIALIZED)
                        sendBroadcast(States.Connecting)
                        LogIHAB.log("Bluetooth: disconnected")
                    }

                    override fun onDeviceConnectionFailed() {}
                })
                bt!!.setBluetoothStateListener(object : BluetoothSPP.BluetoothStateListener {
                    override fun onServiceStateChanged(state: Int) {
                        if (state == BluetoothState.STATE_LISTEN || state == BluetoothState.STATE_NONE) {
                            setState(initState.UNINITIALIZED)
                            sendBroadcast(States.Connecting)
                        }
                    }
                })
                bt!!.setOnDataReceivedListener(object: BluetoothSPP.OnDataReceivedListener {
                    override fun onDataReceived(data: ByteArray?, message: String?) {
                        DataReceived(data)
                    }
                })
                bt!!.setupService()
                val TimeHandler = Handler(Looper.getMainLooper())
                TimeHandler.postDelayed(object : Runnable {
                    override fun run() {
                        if (initializeState == initState.WAITING_FOR_CALIBRATION_VALUES) {
                            if (System.currentTimeMillis() - lastStreamTimer > 1000 && bt != null) {
                                if (bt != null) bt!!.send("GC", false)
                                lastStreamTimer = System.currentTimeMillis()
                            }
                        }
                        if (System.currentTimeMillis() - lastStateChange > 60 * 1000 && initializeState == initState.UNINITIALIZED ||
                                System.currentTimeMillis() - lastStateChange > 10 * 1000 && initializeState == initState.WAITING_FOR_AUDIOTRANSMISSION ||
                                System.currentTimeMillis() - lastStateChange > 10 * 1000 && initializeState == initState.WAITING_FOR_CALIBRATION_VALUES) {
                            lastStateChange = System.currentTimeMillis()
                            setState(initState.UNINITIALIZED)
                            val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                            if (mBluetoothAdapter!!.isEnabled && bt != null) {
                                bt!!.stopService()
                                ReconnectTrials += 1
                                if (ReconnectTrials >= 5 || initializeState == initState.WAITING_FOR_CALIBRATION_VALUES || initializeState == initState.WAITING_FOR_AUDIOTRANSMISSION) {
                                    LogIHAB.log("Bluetooth: Disable Bluetooth Adapter")
                                    mBluetoothAdapter.disable()
                                    ReconnectTrials = 0
                                }
                                Handler(Looper.getMainLooper()).postDelayed({ initBluetooth() }, 1000)
                            }
                        } else if (initializeState == initState.INITIALIZED) {
                            if (System.currentTimeMillis() - lastEmptyPackageTimer > 200) {
                                for (count in 0 until 200 / millisPerBlock) {
                                    lostBlockCount++
                                    lastBlockNumber++
                                    writeData(emptyAudioBlock, true)
                                    Log.d(LOG, "emptyAudioBlock (in Loop)")
                                }
                                lastEmptyPackageTimer = System.currentTimeMillis()
                            }
                            if (System.currentTimeMillis() - lastBluetoothPingTimer > alivePingTimeout) {
                                if (bt != null) bt!!.send(" ", false)
                                lastBluetoothPingTimer = System.currentTimeMillis()
                            }
                            if (contiguousLostBlockCount > 10) {
                                sendBroadcast(States.RequestDisconnection)
                            }
                            if (System.currentTimeMillis() - lastStreamTimer >= 5 * 1000) // 5 seconds
                            {
                                LogIHAB.log("Bluetooth: Transmission Timeout")
                                setState(initState.UNINITIALIZED)
                                sendBroadcast(States.Connecting)
                                if (bt != null) {
                                    bt!!.bluetoothService!!.connectionLost()
                                    bt!!.bluetoothService!!.start(false)
                                }
                            }
                            for (consumer in consumerSet) {
                                if (consumer!!.thread == null || !consumer.thread!!.isAlive || consumer.thread!!.isInterrupted) {
                                    restartStages = true
                                }
                            }
                        }
                        if (bt != null) TimeHandler.postDelayed(this, 100)
                    }
                }, 100)
                bt!!.startService(BluetoothState.DEVICE_OTHER)
                if (bt!!.isBluetoothEnabled) sendBroadcast(States.Connecting)
            }
        }, delay.toLong())
    }

    private fun sendBroadcast(state: States?) {
        when (state) {
            States.Init -> LogIHAB.log("Bluetooth: initializing")
            States.Connecting -> LogIHAB.log("Bluetooth: connecting")
            States.Connected -> {
                LogIHAB.log("Bluetooth: connected")
                if (bt != null && bt!!.bluetoothService != null) {
                    HardwareIDs!![1] = bt!!.bluetoothService!!.BluetoothDevice_MAC
                    LogIHAB.log("Bluetooth: Device '" + bt!!.bluetoothService!!.BluetoothDevice_MAC + "'")
                }
            }
            else -> {}
        }
        val intent = Intent("StageState") //action: "msg"
        intent.setPackage(Stage.Companion.context!!.packageName)
        intent.putExtra("currentState", state!!.ordinal)
        Stage.Companion.context!!.sendBroadcast(intent)
    }

    override fun process(buffer: Array<FloatArray>?) {}
    override fun stop() {
        if (myStageFeatureWrite != null) myStageFeatureWrite!!.stop()
        if (bt != null) bt!!.stopService()
        bt = null
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBluetoothAdapter!!.isEnabled) {
            mBluetoothAdapter.disable()
        }
    }

    private fun setState(state: initState?) {
        if (initializeState != state) {
            lastStateChange = System.currentTimeMillis()
            initializeState = state
        }
    }

    @Synchronized
    private fun DataReceived(data: ByteArray?) {
        for (sample in data!!) {
            ringBuffer!!.addByte(sample)
            checksum = checksum xor ringBuffer.getByte(0)
            if (ringBuffer.getByte(-2) == 0x00.toByte() && ringBuffer.getByte(-3) == 0x80.toByte()) {
                when (initializeState) {
                    initState.UNINITIALIZED -> {
                        val protocollVersion: Int = ringBuffer.getByte(-4).toInt() and 0xFF shl 8 or (ringBuffer.getByte(-5).toInt() and 0xFF)
                        when (protocollVersion) {
                            1 -> {
                                calibValuesInDB!![0] = 0.0.toFloat()
                                calibValuesInDB!![1] = 0.0.toFloat()
                                calibValues!![0] = 1.0.toFloat()
                                calibValues!![1] = 1.0.toFloat()
                                additionalBytesCount = 12
                                setState(initState.WAITING_FOR_AUDIOTRANSMISSION)
                                sendBroadcast(States.Connecting)
                            }
                            2 -> {
                                calibValuesInDB!![0] = Float.NaN
                                calibValuesInDB!![1] = Float.NaN
                                calibValues!![0] = Float.NaN
                                calibValues!![1] = Float.NaN
                                additionalBytesCount = 12
                                if (bt != null) bt!!.send("GC", false)
                                setState(initState.WAITING_FOR_CALIBRATION_VALUES)
                                sendBroadcast(States.Connecting)
                            }
                        }
                    }
                    initState.WAITING_FOR_CALIBRATION_VALUES -> if (ringBuffer.getByte(-15) == 0xFF.toByte() && ringBuffer.getByte(-16) == 0x7F.toByte() && ringBuffer.getByte(-14) == 'C'.code.toByte() && (ringBuffer.getByte(-13) == 'L'.code.toByte() || ringBuffer.getByte(-13) == 'R'.code.toByte())) {
                        val values = ByteArray(8)
                        var ValuesChecksum = ringBuffer.getByte(-13)
                        var count = 0
                        while (count < 8) {
                            values[count] = ringBuffer.getByte(-12 + count)
                            ValuesChecksum = ValuesChecksum xor values[count]
                            count++
                        }
                        if (ValuesChecksum == ringBuffer.getByte(-4)) {
                            if (ringBuffer.getByte(-13) == 'L'.code.toByte()) calibValuesInDB!![0] = ByteBuffer.wrap(values).double.toFloat() else if (ringBuffer.getByte(-13) == 'R'.code.toByte()) calibValuesInDB!![1] = ByteBuffer.wrap(values).double.toFloat()
                            if (!java.lang.Float.isNaN(calibValuesInDB!![0]) && !java.lang.Float.isNaN(calibValuesInDB!![1])) {
                                calibValues!![0] = Math.pow(10.0, calibValuesInDB!![0] / 20.0).toFloat()
                                calibValues!![1] = Math.pow(10.0, calibValuesInDB!![1] / 20.0).toFloat()
                                if (calibValuesInDB!![0] <= 0 || calibValuesInDB!![1] <= 0) {
                                    val intent = Intent("CalibrationValuesError") //action: "msg"
                                    intent.setPackage(Stage.Companion.context!!.packageName)
                                    intent.putExtra("Value", true)
                                    Stage.Companion.context!!.sendBroadcast(intent)
                                }
                                setState(initState.WAITING_FOR_AUDIOTRANSMISSION)
                                sendBroadcast(States.Connecting)
                            }
                        }
                    }
                    initState.WAITING_FOR_AUDIOTRANSMISSION ->                         //Log.d(LOG, "ConnectedThread::RUN::WAITING_FOR_AUDIOTRANSMISSION");
                        if (ringBuffer.getByte(2 - (AudioBufferSize + additionalBytesCount)) == 0xFF.toByte() && ringBuffer.getByte(1 - (AudioBufferSize + additionalBytesCount)) == 0x7F.toByte()) {
                            if (ringBuffer.getByte(0).toInt() == checksum.toInt() xor ringBuffer.getByte(0).toInt()) {
                                currBlockNumber = ringBuffer.getByte(-6).toInt() and 0xFF shl 8 or (ringBuffer.getByte(-7).toInt() and 0xFF)
                                lastBlockNumber = currBlockNumber
                                setState(initState.INITIALIZED)
                                sendBroadcast(States.Connected)
                                restartStages = true
                            }
                            checksum = 0
                        }
                    initState.INITIALIZED -> if (ringBuffer.getByte(2 - (AudioBufferSize + additionalBytesCount)) == 0xFF.toByte() && ringBuffer.getByte(1 - (AudioBufferSize + additionalBytesCount)) == 0x7F.toByte()) {
                        if (ringBuffer.getByte(0).toInt() == checksum.toInt() xor ringBuffer.getByte(0).toInt()) {
                            if (restartStages) {
                                if (myStageFeatureWrite != null) myStageFeatureWrite!!.stop()
                                //Stage.startTime = Instant.now();
                                val parameters = HashMap<String?, String?>()
                                parameters["id"] = "9999999"
                                parameters["prefix"] = "VTB"
                                parameters["nfeatures"] = "1"
                                parameters["blocksize"] = "1"
                                parameters["hopsize"] = "1"
                                myStageFeatureWrite = StageFeatureWrite(parameters)
                                myStageFeatureWrite!!.mySamplingRate = Stage.Companion.samplingrate
                                hopSizeOut = block_size
                                blockSizeOut = block_size
                                myStageFeatureWrite!!.inStage = this
                                myStageFeatureWrite!!.startWithoutThread()
                                hopSizeOut = 400
                                blockSizeOut = 400
                                for (consumer in consumerSet) {
                                    consumer!!.start()
                                }
                                restartStages = false
                            }
                            currBlockNumber = ringBuffer.getByte(-6).toInt() and 0xFF shl 8 or (ringBuffer.getByte(-7).toInt() and 0xFF)
                            if (currBlockNumber < lastBlockNumber && lastBlockNumber - currBlockNumber > currBlockNumber + (65536 - lastBlockNumber)) currBlockNumber += 65536
                            if (lastBlockNumber < currBlockNumber) {
                                lostBlockCount += currBlockNumber - lastBlockNumber - 1
                                while (lastBlockNumber < currBlockNumber - 1) {
                                    //Log.d(LOG, "CurrentBlock: " + currBlockNumber + "\tLostBlocks: " + lostBlockCount);
                                    writeData(emptyAudioBlock, true)
                                    //Log.d(LOG, "emptyAudioBlock " + lastBlockNumber);
                                    lastBlockNumber++
                                }
                                lastBlockNumber = currBlockNumber % 65536
                                writeData(ringBuffer.data(3 - (AudioBufferSize + additionalBytesCount), AudioBufferSize), false)
                                lastStreamTimer = System.currentTimeMillis()
                                lastEmptyPackageTimer = System.currentTimeMillis()
                            }
                        }
                        checksum = 0
                    }
                    else -> {}
                }
            }
        }
    }

    @Synchronized
    private fun writeData(data: ByteArray?, isEmpty: Boolean) {
        if (!isEmpty) emptyAudioBlock = data!!.clone()
        if (myStageFeatureWrite != null) {
            ValidBlocksFeature!![0]!![ValidBlocksFeatureBufferIdx] = 1F
            if (isEmpty) {
                ValidBlocksFeature!![0]!![ValidBlocksFeatureBufferIdx] = 0F
                contiguousLostBlockCount++
            } else contiguousLostBlockCount = 0
        }
        ValidBlocksFeatureBufferIdx++
        val buffer = ShortArray(data!!.size / 2)
        ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()[buffer]
        for (k in 0 until buffer.size / 2) {
            dataOut!![0][BufferIdx] = buffer[k * 2].toFloat() * calibValues!![0] / Short.MAX_VALUE.toFloat()
            dataOut!![1][BufferIdx] = buffer[k * 2 + 1].toFloat() * calibValues!![1] / Short.MAX_VALUE.toFloat()
            BufferIdx++
            if (BufferIdx == frames) {
                if (Stage.Companion.startTime == null) Stage.Companion.startTime = Instant.now().minusMillis((frames.toFloat() / Stage.Companion.samplingrate.toFloat() * 1000.0).toLong())
                send(dataOut!!)
                dataOut = Array(Stage.Companion.channels) { FloatArray(frames) }
                val tempFloat = Array<FloatArray>(1) { FloatArray(1) }
                if (myStageFeatureWrite != null) {
                    for (i in 0 until ValidBlocksFeatureBufferIdx) {
                        tempFloat[0][0] = ValidBlocksFeature!![0]!![i]
                        myStageFeatureWrite!!.process(tempFloat.clone())
                    }
                    ValidBlocksFeature = Array(1) { FloatArray(ValidBlocksFeature!![0]!!.size) }
                    ValidBlocksFeatureBufferIdx = 0
                }
                BufferIdx = 0
            }
        }
    }

    companion object {
        val LOG: String? = "StageRFCOMM"
        private const val block_size = 64
    }

    init {
        sendBroadcast(States.Init)
        hasInput = false
        val blocksize_ms = 25
        frames = blocksize_ms * Stage.Companion.samplingrate / 100
        dataOut = Array(Stage.Companion.channels) { FloatArray(frames) }
        ValidBlocksFeature = Array(1) { FloatArray(Math.ceil((frames / block_size).toDouble()).toInt() + 1) }
        ringBuffer = RingBuffer(AudioBufferSize * 2)
        emptyAudioBlock = ByteArray(AudioBufferSize)
        HardwareIDs!![0] = Secure.getString(Stage.context!!.contentResolver, Secure.ANDROID_ID)
    }
}