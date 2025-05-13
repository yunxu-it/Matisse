package com.zhihu.matisse.internal.utils

import android.app.Application
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat

object ResourceUtil {
  var context: Application? = null

  fun init(mApplication: Application) {
    context = mApplication
  }

  fun getString(@StringRes resId: Int): String {
    checkInit()
    return context!!.getString(resId)
  }

  fun getString(@StringRes resId: Int, vararg formatArgs: Any?): String {
    checkInit()
    return context!!.getString(resId, *formatArgs)
  }

  fun getStringArray(resId: Int): Array<out String> {
    checkInit()
    return context!!.resources.getStringArray(resId)
  }

  fun getColor(@ColorRes resId: Int): Int {
    checkInit()
    return ContextCompat.getColor(context!!, resId)
  }

  fun getDrawable(@DrawableRes resId: Int): Drawable? {
    checkInit()
    return ContextCompat.getDrawable(context!!, resId)
  }

  fun getColorStateList(@ColorRes resId: Int): ColorStateList? {
    checkInit()
    return ContextCompat.getColorStateList(context!!, resId)
  }

  private fun checkInit() {
    if (context == null) {
      throw RuntimeException("context need init!")
    }
  }
}