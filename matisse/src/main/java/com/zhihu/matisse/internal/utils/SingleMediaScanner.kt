package com.zhihu.matisse.internal.utils

import android.content.Context
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.MediaScannerConnectionClient
import android.net.Uri

/**
 * @author 工藤
 * @email gougou@16fan.com
 * create at 2018年10月23日12:17:59
 * description:媒体扫描
 */
class SingleMediaScanner(context: Context, private val mPath: String, private val mListener: ScanListener?) : MediaScannerConnectionClient {
  private val mMsc = MediaScannerConnection(context, this)

  interface ScanListener {
    /**
     * scan finish
     */
    fun onScanFinish()
  }

  init {
    mMsc.connect()
  }

  override fun onMediaScannerConnected() {
    mMsc.scanFile(mPath, null)
  }

  override fun onScanCompleted(mPath: String, mUri: Uri) {
    mMsc.disconnect()
    mListener?.onScanFinish()
  }
}
