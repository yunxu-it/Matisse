package com.zhihu.matisse.module.media

import android.database.Cursor
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zhihu.matisse.base.AbsViewModel
import com.zhihu.matisse.internal.entity.Album
import kotlinx.coroutines.launch

class MediaViewModel(private val repository: MediaRepository) : AbsViewModel() {
  private val _mediaList = MutableLiveData<Cursor>()
  val mediaList: LiveData<Cursor> get() = _mediaList

  fun loadAlbums(album: Album) {
    viewModelScope.launch {
      _mediaList.value = repository.loadAlbumMediaList(album)
    }
  }
}