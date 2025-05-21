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
package com.zhihu.matisse.module.preview

import android.database.Cursor
import android.os.Bundle
import android.util.Log
import com.zhihu.matisse.internal.entity.Album
import com.zhihu.matisse.internal.entity.Item
import com.zhihu.matisse.internal.entity.Item.Companion.valueOf
import com.zhihu.matisse.internal.entity.SelectionSpec.Companion.getInstance
import com.zhihu.matisse.module.media.MediaRepository.Companion.newInstance
import com.zhihu.matisse.module.media.MediaViewModel

class PreviewAlbumActivity : BasePreviewActivity() {
  private val mediaViewModel: MediaViewModel by lazy { MediaViewModel(newInstance(this)) }

  private var item: Item? = null

  private var mIsAlreadySetPosition = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (!getInstance().hasInited) {
      setResult(RESULT_CANCELED)
      finish()
      return
    }

    initData()
  }

  private fun initData() {
    val album = intent.getParcelableExtra<Album>(EXTRA_ALBUM)
    if (album != null) {
      mediaViewModel.loadAlbums(album)
      mediaViewModel.mediaList.observe(this) { cursor: Cursor? ->
        if (cursor == null) return@observe
        onAlbumMediaLoad(cursor)
      }
    }
    item = intent.getParcelableExtra<Item>(EXTRA_ITEM)
    if (item != null) {
      if (mSpec.countable) {
        mCheckView.setCheckedNum(mSelectedCollection.checkedNumOf(item))
      } else {
        mCheckView.setChecked(mSelectedCollection.isSelected(item))
      }
      updateSize(item)
    }
  }

  private fun onAlbumMediaLoad(cursor: Cursor) {
    val items: MutableList<Item> = ArrayList()
    while (cursor.moveToNext()) {
      items.add(valueOf(cursor))
    }
    cursor.close()

    if (items.isEmpty()) {
      return
    }

    mAdapter.addAll(items)
    mAdapter.notifyDataSetChanged()

    if (!mIsAlreadySetPosition) {
      //onAlbumMediaLoad is called many times..
      mIsAlreadySetPosition = true
      if (item != null) {
        val selectedIndex = items.indexOf(item)
        mPager.setCurrentItem(selectedIndex, false)
        mPreviousPos = selectedIndex
      }
    }
  }

  companion object {
    const val EXTRA_ALBUM: String = "extra_album"
    const val EXTRA_ITEM: String = "extra_item"
  }
}
