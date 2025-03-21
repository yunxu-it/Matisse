/*
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zhihu.matisse.internal.entity

import android.content.Context
import android.widget.Toast
import androidx.annotation.IntDef
import androidx.fragment.app.FragmentActivity
import com.zhihu.matisse.internal.ui.widget.IncapableDialog
import kotlin.annotation.AnnotationRetention.SOURCE

class IncapableCause {
  @Retention(SOURCE)
  @IntDef(value = [TOAST, DIALOG, NONE])
  annotation class Form

  private var mForm = TOAST
  private var mTitle: String? = null
  private val mMessage: String

  constructor(message: String) {
    mMessage = message
  }

  constructor(title: String?, message: String) {
    mTitle = title
    mMessage = message
  }

  constructor(@Form form: Int, message: String) {
    mForm = form
    mMessage = message
  }

  constructor(@Form form: Int, title: String?, message: String) {
    mForm = form
    mTitle = title
    mMessage = message
  }

  companion object {
    const val TOAST: Int = 0x00
    const val DIALOG: Int = 0x01
    const val NONE: Int = 0x02

    @JvmStatic
    fun handleCause(context: Context, cause: IncapableCause?) {
      if (cause == null) return

      when (cause.mForm) {
        NONE -> {}
        DIALOG -> {
          val incapableDialog = IncapableDialog.newInstance(cause.mTitle, cause.mMessage)
          incapableDialog.show(
            (context as FragmentActivity).supportFragmentManager,
            IncapableDialog::class.java.name
          )
        }

        TOAST -> Toast.makeText(context, cause.mMessage, Toast.LENGTH_SHORT).show()
        else -> Toast.makeText(context, cause.mMessage, Toast.LENGTH_SHORT).show()
      }
    }
  }
}
