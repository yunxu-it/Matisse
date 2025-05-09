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

import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.provider.MediaStore.Files
import android.provider.MediaStore.Files.FileColumns
import android.provider.MediaStore.Images.Media
import android.provider.MediaStore.MediaColumns
import android.provider.MediaStore.Video
import com.zhihu.matisse.MimeType
import kotlinx.parcelize.Parcelize

@Parcelize
class Item(
  val id: Long,
  val mimeType: String,
  @JvmField
  val size: Long,
  @JvmField
  var duration: Long
) : Parcelable {

  @JvmField
  val contentUri: Uri = if (isImage) {
    Media.EXTERNAL_CONTENT_URI
  } else if (isVideo) {
    Video.Media.EXTERNAL_CONTENT_URI
  } else {
    Files.getContentUri("external")
  }.let {
    ContentUris.withAppendedId(it, id)
  }

  val isCapturePhoto: Boolean
    get() = id == ITEM_ID_CAPTURE_PHOTO

  val isCaptureVideo: Boolean
    get() = id == ITEM_ID_CAPTURE_VIDEO

  val isImage: Boolean
    get() = MimeType.isImage(mimeType)

  val isGif: Boolean
    get() = MimeType.isGif(mimeType)

  val isVideo: Boolean
    get() = MimeType.isVideo(mimeType)

  override fun equals(other: Any?): Boolean {
    if (other !is Item) {
      return false
    }

    return id == other.id && (mimeType != null && mimeType == other.mimeType || (mimeType == null && other.mimeType == null)) && (contentUri != null && contentUri == other.contentUri || (contentUri == null && other.contentUri == null)) && size == other.size && duration == other.duration
  }

  override fun hashCode(): Int {
    var result = 1
    result = 31 * result + id.hashCode()
    if (mimeType != null) {
      result = 31 * result + mimeType.hashCode()
    }
    result = 31 * result + contentUri.hashCode()
    result = 31 * result + size.hashCode()
    result = 31 * result + duration.hashCode()
    return result
  }

  companion object {
    const val ITEM_ID_CAPTURE_PHOTO: Long = -1
    const val ITEM_ID_CAPTURE_VIDEO: Long = -2
    const val ITEM_DISPLAY_NAME_CAPTURE: String = "Capture"

    @JvmStatic
    fun valueOf(cursor: Cursor): Item {
      return Item(
        cursor.getLong(cursor.getColumnIndexOrThrow(FileColumns._ID)),
        cursor.getString(cursor.getColumnIndexOrThrow(MediaColumns.MIME_TYPE)),
        cursor.getLong(cursor.getColumnIndexOrThrow(MediaColumns.SIZE)),
        cursor.getLong(cursor.getColumnIndexOrThrow("duration"))
      )
    }
  }
}
