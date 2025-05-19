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
package com.zhihu.matisse

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.IntDef
import androidx.annotation.StyleRes
import com.zhihu.matisse.engine.ImageEngine
import com.zhihu.matisse.filter.Filter
import com.zhihu.matisse.internal.entity.CaptureStrategy
import com.zhihu.matisse.internal.entity.SelectionSpec
import com.zhihu.matisse.listener.OnCheckedListener
import com.zhihu.matisse.listener.OnSelectedListener
import com.zhihu.matisse.module.album.MatisseActivity
import kotlin.annotation.AnnotationRetention.SOURCE

/**
 * Fluent API for building media select specification.
 */
/**
 * 构造一个新的规范构建器在上下文中。
 *
 * @param matisse   请求者上下文包装器。
 * @param mimeTypes 要选择的MIME类型集合。
 */
class SelectionCreator internal constructor(private val matisse: Matisse, mimeTypes: Set<MimeType>, mediaTypeExclusive: Boolean) {
  private val mSelectionSpec: SelectionSpec = SelectionSpec.getCleanInstance()

  @IntDef(
    value = [ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, ActivityInfo.SCREEN_ORIENTATION_USER, ActivityInfo.SCREEN_ORIENTATION_BEHIND, ActivityInfo.SCREEN_ORIENTATION_SENSOR, ActivityInfo.SCREEN_ORIENTATION_NOSENSOR, ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE, ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT, ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE, ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT, ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR, ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE, ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT, ActivityInfo.SCREEN_ORIENTATION_FULL_USER, ActivityInfo.SCREEN_ORIENTATION_LOCKED]
  )
  @Retention(SOURCE)
  internal annotation class ScreenOrientation

  init {
    mSelectionSpec.mimeTypeSet = mimeTypes
    mSelectionSpec.mediaTypeExclusive = mediaTypeExclusive
    mSelectionSpec.orientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
  }

  /**
   * 是否仅显示一种媒体类型，如果选择的媒体仅为图片或视频时。
   * @param showSingleMediaType 是否仅显示一种媒体类型，即图片或视频。
   * @return [SelectionCreator] 用于流畅API调用。
   * @see SelectionSpec.onlyShowImages
   * @see SelectionSpec.onlyShowVideos
   */
  fun showSingleMediaType(showSingleMediaType: Boolean): SelectionCreator {
    mSelectionSpec.showSingleMediaType = showSingleMediaType
    return this
  }

  /**
   * 用于媒体选择Activity的主题。
   *
   *
   * 有两个内置主题：
   * 1. com.zhihu.matisse.R.style.Matisse_Zhihu;
   * 2. com.zhihu.matisse.R.style.Matisse_Dracula
   * 你可以从上述主题或其他主题派生自定义主题。
   *
   * @param themeId 主题资源ID。默认值为 com.zhihu.matisse.R.style.Matisse_Zhihu。
   * @return [SelectionCreator] 用于流畅的API。
   */
  fun theme(@StyleRes themeId: Int): SelectionCreator {
    mSelectionSpec.themeId = themeId
    return this
  }

  /**
   * 当用户选择媒体时，显示一个自动递增的数字或一个勾选标记。
   *
   * @param countable 为true时显示从1开始的自动递增数字，为false时显示勾选标记。默认值为false。
   * @return [SelectionCreator] 用于流式API。
   */
  fun countable(countable: Boolean): SelectionCreator {
    mSelectionSpec.countable = countable
    return this
  }

  /**
   *
   * 设置最大可选数量。
   *
   * @param maxSelectable 最大可选数量。默认值为1。
   * @return 返回 [SelectionCreator] 以支持流畅式API。
   */
  fun maxSelectable(maxSelectable: Int): SelectionCreator {
    require(maxSelectable >= 1) { "maxSelectable must be greater than or equal to one" }
    check(!(mSelectionSpec.maxImageSelectable > 0 || mSelectionSpec.maxVideoSelectable > 0)) { "already set maxImageSelectable and maxVideoSelectable" }
    mSelectionSpec.maxSelectable = maxSelectable
    return this
  }

  /**
   * 仅在 [SelectionSpec.mediaTypeExclusive] 设置为 true 且您希望为图像和视频媒体类型设置不同的最大可选文件数量时有用。
   * @param maxImageSelectable 图像的最大可选数量。
   *
   * @param maxVideoSelectable 视频的最大可选数量。
   * @return [SelectionCreator] 用于流畅API。
   */
  fun maxSelectablePerMediaType(maxImageSelectable: Int, maxVideoSelectable: Int): SelectionCreator {
    require(!(maxImageSelectable < 1 || maxVideoSelectable < 1)) { ("max selectable must be greater than or equal to one") }
    mSelectionSpec.maxSelectable = -1
    mSelectionSpec.maxImageSelectable = maxImageSelectable
    mSelectionSpec.maxVideoSelectable = maxVideoSelectable
    return this
  }

  /**
   * Add filter to filter each selecting item.
   *
   * @param filter [Filter]
   * @return [SelectionCreator] for fluent API.
   */
  fun addFilter(filter: Filter): SelectionCreator {
    (mSelectionSpec.filters?.toMutableList() ?: mutableListOf()).let {
      it.add(filter)
      mSelectionSpec.filters = it
    }
    return this
  }

  /**
   * 确定媒体网格视图中是否启用了照片拍摄功能。
   *
   * 如果这个值为 true，照片拍摄入口将仅在“所有媒体”的页面上显示。
   *
   * @param enable 是否启用捕获。默认值为 false；
   * @return [SelectionCreator] for fluent API.
   */
  fun capture(enable: Boolean): SelectionCreator {
    mSelectionSpec.capture = enable
    return this
  }

  /**
   * 展示原始照片并提供选项。让用户在选择后决定是否使用原始照片。
   *
   * @param enable 是否启用原始照片
   * @return [SelectionCreator]用于流畅API。
   */
  fun originalEnable(enable: Boolean): SelectionCreator {
    mSelectionSpec.originalable = enable
    return this
  }

  /**
   * 在预览模式下，当用户点击图片时，决定是否隐藏顶部和底部工具栏。
   * @param enable 是否隐藏顶部和底部工具栏
   * @return [SelectionCreator]用于流畅API。
   */
  fun autoHideToolbarOnSingleTap(enable: Boolean): SelectionCreator {
    mSelectionSpec.autoHideToolbar = enable
    return this
  }

  /**
   * 最大原始大小，单位为 MB。只有在 {link@originalEnable} 设置为 true 时才有用。
   * @param size 最大原始尺寸。默认值为 Integer.MAX_VALUE。
   * @return [SelectionCreator]用于流畅API。
   */
  fun maxOriginalSize(size: Int): SelectionCreator {
    mSelectionSpec.originalMaxSize = size
    return this
  }

  /**
   * Capture strategy provided for the location to save photos including internal and external
   * storage and also a authority for [androidx.core.content.FileProvider].
   *
   * @param captureStrategy [CaptureStrategy], needed only when capturing is enabled.
   * @return [SelectionCreator] for fluent API.
   */
  fun captureStrategy(captureStrategy: CaptureStrategy?): SelectionCreator {
    mSelectionSpec.captureStrategy = captureStrategy
    return this
  }

  /**
   * Set the desired orientation of this activity.
   *
   * @param orientation An orientation constant as used in [ScreenOrientation].
   * Default value is [android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT].
   * @return [SelectionCreator] for fluent API.
   * @see Activity.setRequestedOrientation
   */
  fun restrictOrientation(@ScreenOrientation orientation: Int): SelectionCreator {
    mSelectionSpec.orientation = orientation
    return this
  }

  /**
   * Set a fixed span count for the media grid. Same for different screen orientations.
   *
   *
   * This will be ignored when [.gridExpectedSize] is set.
   *
   * @param spanCount Requested span count.
   * @return [SelectionCreator] for fluent API.
   */
  fun spanCount(spanCount: Int): SelectionCreator {
    require(spanCount >= 1) { "spanCount cannot be less than 1" }
    mSelectionSpec.spanCount = spanCount
    return this
  }

  /**
   * Set expected size for media grid to adapt to different screen sizes. This won't necessarily
   * be applied cause the media grid should fill the view container. The measured media grid's
   * size will be as close to this value as possible.
   *
   * @param size Expected media grid size in pixel.
   * @return [SelectionCreator] for fluent API.
   */
  fun gridExpectedSize(size: Int): SelectionCreator {
    mSelectionSpec.gridExpectedSize = size
    return this
  }

  /**
   * Photo thumbnail's scale compared to the View's size. It should be a float value in (0.0,
   * 1.0].
   *
   * @param scale Thumbnail's scale in (0.0, 1.0]. Default value is 0.5.
   * @return [SelectionCreator] for fluent API.
   */
  fun thumbnailScale(scale: Float): SelectionCreator {
    require(!(scale <= 0f || scale > 1f)) { "Thumbnail scale must be between (0.0, 1.0]" }
    mSelectionSpec.thumbnailScale = scale
    return this
  }

  /**
   * Provide an image engine.
   *
   *
   * There are two built-in image engines:
   * 1. [com.zhihu.matisse.engine.impl.GlideEngine]
   * 2. [com.zhihu.matisse.engine.impl.PicassoEngine]
   * And you can implement your own image engine.
   *
   * @param imageEngine [ImageEngine]
   * @return [SelectionCreator] for fluent API.
   */
  fun imageEngine(imageEngine: ImageEngine): SelectionCreator {
    mSelectionSpec.imageEngine = imageEngine
    return this
  }

  /**
   * Set listener for callback immediately when user select or unselect something.
   *
   *
   * It's a redundant API with [Matisse.obtainResult],
   * we only suggest you to use this API when you need to do something immediately.
   *
   * @param listener [OnSelectedListener]
   * @return [SelectionCreator] for fluent API.
   */
  fun setOnSelectedListener(listener: OnSelectedListener?): SelectionCreator {
    mSelectionSpec.onSelectedListener = listener
    return this
  }

  /**
   * Set listener for callback immediately when user check or uncheck original.
   *
   * @param listener [OnSelectedListener]
   * @return [SelectionCreator] for fluent API.
   */
  fun setOnCheckedListener(listener: OnCheckedListener?): SelectionCreator {
    mSelectionSpec.onCheckedListener = listener
    return this
  }

  /**
   * Start to select media and wait for result.
   *
   * @param launcher [ActivityResultLauncher] for starting [MatisseActivity] for result.
   */
  fun forResult(launcher: ActivityResultLauncher<Intent>) {
    val activity = matisse.activity ?: return
    val intent = Intent(activity, MatisseActivity::class.java)
    launcher.launch(intent)
  }

  @Deprecated("use forResult instead")
  fun forResultCode(requestCode: Int) {
    val activity = matisse.activity ?: return
    val intent = Intent(activity, MatisseActivity::class.java)
    val fragment = matisse.fragment
    if (fragment != null) {
      fragment.startActivityForResult(intent, requestCode)
    } else {
      activity.startActivityForResult(intent, requestCode)
    }
  }

  fun showPreview(showPreview: Boolean): SelectionCreator {
    mSelectionSpec.showPreview = showPreview
    return this
  }
}
