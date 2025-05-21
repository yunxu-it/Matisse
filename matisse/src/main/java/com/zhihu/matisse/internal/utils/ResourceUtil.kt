
/*
 * Copyright 2025 lixiaolong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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