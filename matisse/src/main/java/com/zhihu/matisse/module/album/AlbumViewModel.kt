package com.zhihu.matisse.module.album

import android.database.Cursor
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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