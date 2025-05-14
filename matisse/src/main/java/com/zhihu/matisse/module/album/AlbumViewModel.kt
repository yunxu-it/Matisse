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