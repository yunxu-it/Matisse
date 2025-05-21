
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
package com.zhihu.matisse.module.media

import android.database.Cursor
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zhihu.matisse.base.AbsViewModel
import com.zhihu.matisse.internal.entity.Album
import kotlinx.coroutines.launch

class MediaViewModel(private val repository: MediaRepository) : AbsViewModel() {
  private val _mediaList = MutableLiveData<Cursor>()
  val mediaList: LiveData<Cursor> get() = _mediaList

  /**
   * @param needCaptureItem 预览列表不需要显示拍照和录像按钮
   */
  fun loadAlbums(album: Album, needCaptureItem: Boolean = false) {
    viewModelScope.launch {
      _mediaList.value = repository.loadAlbumMediaList(album, needCaptureItem)
    }
  }
}

class MediaViewModelFactory(private val repository: MediaRepository) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(MediaViewModel::class.java)) {
      return MediaViewModel(repository) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}