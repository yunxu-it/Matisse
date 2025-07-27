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
package com.zhihu.matisse.base

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type

abstract class BaseActivity : AppCompatActivity() {
  private var immersiveInitPaddingTop: Int = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    init(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)
    ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v: View, insets: WindowInsetsCompat ->
      val navigationBars = insets.getInsets(Type.systemBars())
      v.setPadding(0, 0, 0, navigationBars.bottom)
      insets
    }
    initContentView()
    initAfter(savedInstanceState)
    initView()
    initData()

    immersiveView()?.let {
      immersiveInitPaddingTop = it.layoutParams.height
      ViewCompat.setOnApplyWindowInsetsListener(it) { v: View, insets: WindowInsetsCompat ->
        val insetsInsets = insets.getInsets(Type.statusBars())
        v.setPadding(v.paddingLeft, immersiveInitPaddingTop + insetsInsets.top, v.paddingRight, v.paddingBottom)
        insets
      }
    }
  }

  protected open fun immersiveView(): View? {
    return null
  }

  protected open fun init(savedInstanceState: Bundle?) {}

  protected open fun initAfter(savedInstanceState: Bundle?) {}

  protected open fun initContentView() {
    setContentView(setLayoutResourceID())
  }

  protected fun setLayoutResourceID(): Int {
    return 0
  }

  protected abstract fun initView()

  protected open fun initData() {}

}