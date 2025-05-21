
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
package com.zhihu.matisse.module.album

import android.database.Cursor
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zhihu.matisse.base.AbsViewModel
import kotlinx.coroutines.launch

class AlbumViewModel(private val repository: AlbumRepository) : AbsViewModel() {
  private val _albums = MutableLiveData<Cursor>()
  val albums: LiveData<Cursor> get() = _albums

  fun loadAlbums() {
    viewModelScope.launch {
      _albums.value = repository.loadAlbums()
    }
  }
}

class AlbumViewModelFactory(private val repository: AlbumRepository) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(AlbumViewModel::class.java)) {
      return AlbumViewModel(repository) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}