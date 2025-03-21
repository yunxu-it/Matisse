/*
 * Copyright (C) 2014 nohana, Inc.
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

import android.content.pm.ActivityInfo
import androidx.annotation.StyleRes
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.R
import com.zhihu.matisse.engine.ImageEngine
import com.zhihu.matisse.engine.impl.GlideEngine
import com.zhihu.matisse.filter.Filter
import com.zhihu.matisse.listener.OnCheckedListener
import com.zhihu.matisse.listener.OnSelectedListener

class SelectionSpec private constructor() {
  @JvmField
  var mimeTypeSet: Set<MimeType>? = null

  @JvmField
  var mediaTypeExclusive: Boolean = false

  /**
   * 是否仅显示一种媒体类型，如果选择的媒体仅为图片或视频时。
   */
  var showSingleMediaType: Boolean = false

  @JvmField
  @StyleRes
  var themeId: Int = 0

  @JvmField
  var orientation: Int = 0

  /**
   * 勾选圆圈显示计数
   */
  @JvmField
  var countable: Boolean = false

  @JvmField
  var maxSelectable: Int = 0

  @JvmField
  var maxImageSelectable: Int = 0

  @JvmField
  var maxVideoSelectable: Int = 0

  @JvmField
  var filters: List<Filter>? = null

  /**
   * 开启拍照功能
   */
  @JvmField
  var capture: Boolean = false

  @JvmField
  var captureStrategy: CaptureStrategy? = null

  @JvmField
  var spanCount: Int = 0

  @JvmField
  var gridExpectedSize: Int = 0

  @JvmField
  var thumbnailScale: Float = 0f

  @JvmField
  var imageEngine: ImageEngine = GlideEngine()

  @JvmField
  var hasInited: Boolean = false

  @JvmField
  var onSelectedListener: OnSelectedListener? = null

  @JvmField
  var originalable: Boolean = false

  @JvmField
  var autoHideToolbar: Boolean = false

  @JvmField
  var originalMaxSize: Int = 0

  @JvmField
  var onCheckedListener: OnCheckedListener? = null

  @JvmField
  var showPreview: Boolean = false

  private fun reset() {
    mimeTypeSet = null
    mediaTypeExclusive = true
    showSingleMediaType = false
    themeId = R.style.Matisse_Zhihu
    orientation = 0
    countable = false
    maxSelectable = 1
    maxImageSelectable = 0
    maxVideoSelectable = 0
    filters = null
    capture = false
    captureStrategy = null
    spanCount = 3
    gridExpectedSize = 0
    thumbnailScale = 0.5f
    imageEngine = GlideEngine()
    hasInited = true
    originalable = false
    autoHideToolbar = false
    originalMaxSize = Int.MAX_VALUE
    showPreview = true
  }

  fun singleSelectionModeEnabled(): Boolean {
    return !countable && (maxSelectable == 1 || (maxImageSelectable == 1 && maxVideoSelectable == 1))
  }

  fun needOrientationRestriction(): Boolean {
    return orientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
  }

  fun onlyShowImages(): Boolean {
    return showSingleMediaType && MimeType.ofImage().containsAll(mimeTypeSet ?: emptySet())
  }

  fun onlyShowVideos(): Boolean {
    return showSingleMediaType && MimeType.ofVideo().containsAll(mimeTypeSet ?: emptySet())
  }

  fun onlyShowGif(): Boolean {
    return showSingleMediaType && MimeType.ofGif() == mimeTypeSet
  }

  private object InstanceHolder {
    val instance: SelectionSpec = SelectionSpec()
  }

  companion object {
    @JvmStatic
    fun getInstance(): SelectionSpec {
      return InstanceHolder.instance
    }

    fun getCleanInstance(): SelectionSpec {
      val selectionSpec: SelectionSpec = InstanceHolder.instance
      selectionSpec.reset()
      return selectionSpec
    }
  }
}
