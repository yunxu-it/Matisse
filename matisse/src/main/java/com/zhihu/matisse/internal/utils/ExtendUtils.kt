package com.zhihu.matisse.internal.utils

import android.content.res.TypedArray

inline fun <T> TypedArray.autoClose(block: (TypedArray) -> T) {
  try {
    block(this)
  } finally {
    recycle()
  }
}