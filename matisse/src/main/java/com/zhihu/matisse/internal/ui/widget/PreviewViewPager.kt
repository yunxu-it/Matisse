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
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zhihu.matisse.internal.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.viewpager.widget.ViewPager
import it.sephiroth.android.library.imagezoom.ImageViewTouch

class PreviewViewPager(context: Context, attrs: AttributeSet?) : ViewPager(context, attrs) {
  override fun canScroll(v: View, checkV: Boolean, dx: Int, x: Int, y: Int): Boolean {
    if (v is ImageViewTouch) {
      return v.canScroll(dx) || super.canScroll(v, checkV, dx, x, y)
    }
    return super.canScroll(v, checkV, dx, x, y)
  }
}
