package com.iha.olmega_mobilesoftware_v2.AFEx.Tools

import java.io.ByteArrayOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer

object NetworkIO {
    fun sendUdpPacket(timestamp: String) {
        val thread = Thread({
            try {
                // prepare data
                val out = ByteArrayOutputStream()
                out.write(timestamp.toByteArray())
                //out.write("_".getBytes());
                //out.write(data.getBytes());
                //out.write(floatToBytes(data));
                val buffer = out.toByteArray()

                //System.out.println("Data: " + buffer.length+ " | " + buffer.toString());
                //System.out.println("Localhost: " + InetAddress.getLocalHost());
                val ds = DatagramSocket()
                val dp = DatagramPacket(buffer, 0, buffer.size, InetAddress.getLocalHost(), 40007)
                ds.send(dp)
            } catch (e: Exception) {
                println("Send failed. $e")
            }
        }, "NetworkIO")
        thread.start()
    }

    fun floatToBytes(data: FloatArray): ByteArray {
        val buffer = ByteBuffer.allocate(data.size * 4)
        for (value in data) buffer.putFloat(value)
        return buffer.array()
    }
}