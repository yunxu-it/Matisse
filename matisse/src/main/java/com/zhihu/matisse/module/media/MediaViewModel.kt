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