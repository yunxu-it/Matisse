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
package com.zhihu.matisse.internal.entity

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Parcelable
import android.util.Log
import com.zhihu.matisse.R
import com.zhihu.matisse.internal.loader.AlbumLoader
import kotlinx.parcelize.Parcelize
import androidx.core.net.toUri

@Parcelize
class Album(var id: String, var coverUri: Uri, private var displayName: String, var count: Long) : Parcelable {

  fun addCaptureCount() {
    count++
  }

  fun getDisplayName(context: Context): String? {
    if (isAll) {
      return context.getString(R.string.album_name_all)
    }
    return displayName
  }

  val isAll: Boolean
    get() = ALBUM_ID_ALL == id

  val isEmpty: Boolean
    get() = count == 0L

  companion object {
    const val ALBUM_ID_ALL: String = (-1).toString()
    const val ALBUM_NAME_ALL: String = "All"

    /**
     * Constructs a new [Album] entity from the [Cursor].
     * This method is not responsible for managing cursor resource, such as close, iterate, and so on.
     */
    @JvmStatic
    fun valueOf(cursor: Cursor): Album {
      val albumUri = cursor.getString(cursor.getColumnIndexOrThrow(AlbumLoader.COLUMN_URI))
      val indexAlbumName = cursor.getColumnIndexOrThrow("bucket_display_name")
      Log.i("Album", "valueOf-63: " + indexAlbumName + " " + cursor.getString(indexAlbumName))
      return Album(
        cursor.getString(cursor.getColumnIndexOrThrow("bucket_id")),
        (albumUri ?: "").toUri(),
        cursor.getString(indexAlbumName) ?: "unknown",
        cursor.getLong(cursor.getColumnIndexOrThrow(AlbumLoader.COLUMN_COUNT))
      )
    }
  }
}