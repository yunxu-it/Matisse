package com.zhihu.matisse.internal.utils

import android.os.Build.VERSION
import android.os.Build.VERSION_CODES

object Platform {
  fun beforeAndroidTen(): Boolean {
    return VERSION.SDK_INT < VERSION_CODES.Q
  }
}
