package com.zhihu.matisse.module.album

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zhihu.matisse.base.AbsViewModel
import com.zhihu.matisse.internal.entity.Album
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class AlbumViewModel @Inject constructor(private val repository: AlbumRepository) : AbsViewModel() {
  private val _albums = MutableLiveData<List<Album>>()
  val albums: LiveData<List<Album>> get() = _albums

  fun loadAlbums() {
    viewModelScope.launch {
      _albums.value = repository.loadAlbums()
    }
  }

}