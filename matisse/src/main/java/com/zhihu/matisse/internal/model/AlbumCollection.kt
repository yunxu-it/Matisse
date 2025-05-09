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
package com.zhihu.matisse.internal.model

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.loader.app.LoaderManager
import androidx.loader.app.LoaderManager.LoaderCallbacks
import androidx.loader.content.Loader
import com.zhihu.matisse.internal.loader.AlbumLoader
import java.lang.ref.WeakReference

class AlbumCollection : LoaderCallbacks<Cursor> {
  private var mContext: WeakReference<Context>? = null
  private var mLoaderManager: LoaderManager? = null
  private var mCallbacks: AlbumCallbacks? = null
  var currentSelection: Int = 0
    private set
  private var mLoadFinished = false

  override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
    val context = mContext?.get() ?: throw RuntimeException("context is null")

    mLoadFinished = false
    return AlbumLoader.newInstance(context)
  }

  override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
    if (!mLoadFinished) {
      mCallbacks?.let {
        it.onAlbumLoad(data)
        mLoadFinished = true
      }
    }
  }

  override fun onLoaderReset(loader: Loader<Cursor>) {
    mCallbacks?.onAlbumReset()
  }

  fun onCreate(activity: FragmentActivity, callbacks: AlbumCallbacks?) {
    mContext = WeakReference(activity)
    mLoaderManager = LoaderManager.getInstance(activity)
    mCallbacks = callbacks
  }

  fun onRestoreInstanceState(savedInstanceState: Bundle?) {
    if (savedInstanceState == null) {
      return
    }

    currentSelection = savedInstanceState.getInt(STATE_CURRENT_SELECTION)
  }

  fun onSaveInstanceState(outState: Bundle) {
    outState.putInt(STATE_CURRENT_SELECTION, currentSelection)
  }

  fun onDestroy() {
    mLoaderManager?.destroyLoader(LOADER_ID)
    mCallbacks = null
  }

  fun loadAlbums() {
    mLoaderManager?.initLoader(LOADER_ID, null, this)
  }

  fun setStateCurrentSelection(currentSelection: Int) {
    this.currentSelection = currentSelection
  }

  interface AlbumCallbacks {
    fun onAlbumLoad(cursor: Cursor?)

    fun onAlbumReset()
  }

  companion object {
    private const val LOADER_ID = 1
    private const val STATE_CURRENT_SELECTION = "state_current_selection"
  }
}
