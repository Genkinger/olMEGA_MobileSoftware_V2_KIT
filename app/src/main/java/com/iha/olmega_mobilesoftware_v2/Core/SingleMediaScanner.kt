package com.iha.olmega_mobilesoftware_v2.Core

import android.content.Context
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.MediaScannerConnectionClient
import android.net.Uri
import java.io.File

class SingleMediaScanner(context: Context?, private val mFile: File?) : MediaScannerConnectionClient {
    private val mMs: MediaScannerConnection?
    override fun onMediaScannerConnected() {
        mMs!!.scanFile(mFile!!.absolutePath, null)
    }

    override fun onScanCompleted(path: String?, uri: Uri?) {
        mMs!!.disconnect()
    }

    init {
        mMs = MediaScannerConnection(context, this)
        mMs.connect()
    }
}