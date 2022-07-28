package com.iha.olmega_mobilesoftware_v2.Core

class RingBuffer(BufferSize: Int) {
    private val Data: ByteArray = ByteArray(BufferSize)
    private var idx: Int = 0

    fun addByte(data: Byte) {
        idx = (idx + 1) % Data.size
        Data[idx] = data
    }

    fun setByte(data: Byte, currIdx: Int) {
        var currIdx = currIdx
        currIdx = (idx + currIdx) % Data.size
        if (currIdx < 0) currIdx += Data.size
        Data[currIdx] = data
    }

    fun getByte(currIdx: Int): Byte {
        var currIdx = currIdx
        currIdx = (idx + currIdx) % Data.size
        if (currIdx < 0) currIdx += Data.size
        return Data[currIdx]
    }

    fun getShort(currIdx: Int): Short {
        return (getByte(currIdx + 1).toInt() and 0xFF shl 8 or (getByte(currIdx).toInt() and 0xFF)).toShort()
    }

    fun setShort(Value: Short, currIdx: Int) {
        setByte((Value.toInt() shr 8 and 0xFF).toByte(), currIdx + 1)
        setByte((Value.toInt() and 0xFF).toByte(), currIdx)
    }

    fun data(startIdx: Int, length: Int): ByteArray {
        var startIdx = startIdx
        val returnArray = ByteArray(length)
        startIdx = (idx + startIdx) % Data.size
        if (startIdx < 0) startIdx += Data.size
        val tmpLen = Math.min(length, Data.size - startIdx)
        System.arraycopy(Data, startIdx, returnArray, 0, tmpLen)
        if (tmpLen != length) System.arraycopy(Data, 0, returnArray, tmpLen, length - tmpLen)
        return returnArray
    }

}