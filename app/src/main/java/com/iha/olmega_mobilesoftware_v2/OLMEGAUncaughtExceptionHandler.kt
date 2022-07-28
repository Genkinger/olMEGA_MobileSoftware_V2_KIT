package com.iha.olmega_mobilesoftware_v2

import android.content.Context
import android.content.Intent
import android.os.Process
import android.util.Log
import com.iha.olmega_mobilesoftware_v2.Core.LogIHAB
import java.io.PrintWriter
import java.io.StringWriter

class OLMEGAUncaughtExceptionHandler(private val myContext: Context, private val myActivityClass: Class<*>) : Thread.UncaughtExceptionHandler {
    private val TAG = this.javaClass.simpleName
    override fun uncaughtException(thread: Thread, exception: Throwable) {
        val sw = StringWriter()
        exception.printStackTrace(PrintWriter(sw))
        Log.e(TAG, sw.toString())
        LogIHAB.log("<begin stacktrace>\n$sw\n<end stacktrace>")
        val intent = Intent(myContext, myActivityClass)
        //you can use this String to know what caused the exception and in which Activity
        intent.putExtra("uncaughtException", "Exception is: $sw")
        intent.putExtra("stacktrace", sw.toString())
        myContext.startActivity(intent)
        //for restarting the Activity
        Process.killProcess(Process.myPid())
        System.exit(0)
    }
}