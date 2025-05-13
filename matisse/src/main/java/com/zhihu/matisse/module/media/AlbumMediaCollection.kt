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
package com.zhihu.matisse.module.media

import android.net.Uri
import android.provider.MediaStore
import android.provider.MediaStore.Files.FileColumns
import android.provider.MediaStore.Images.Media
import android.provider.MediaStore.MediaColumns

/**
 * Load images and videos into a single cursor.
 */
class AlbumMediaCollection {

  companion object {
    val QUERY_URI: Uri = MediaStore.Files.getContentUri("external")

    val PROJECTION = arrayOf(FileColumns._ID, MediaColumns.DISPLAY_NAME, MediaColumns.MIME_TYPE, MediaColumns.SIZE, MediaColumns.DURATION)

    // === params for album ALL && showSingleMediaType: false ===
    const val SELECTION_ALL = "(${FileColumns.MEDIA_TYPE}=? OR ${FileColumns.MEDIA_TYPE}=?) AND ${MediaColumns.SIZE}>0"

    val SELECTION_ALL_ARGS = arrayOf(FileColumns.MEDIA_TYPE_IMAGE.toString(), FileColumns.MEDIA_TYPE_VIDEO.toString())

    // ===========================================================
    // === params for album ALL && showSingleMediaType: true ===
    const val SELECTION_ALL_FOR_SINGLE_MEDIA_TYPE = "${FileColumns.MEDIA_TYPE}=? AND ${MediaColumns.SIZE}>0"

    fun getSelectionArgsForSingleMediaType(mediaType: Int): Array<String> {
      return arrayOf(mediaType.toString())
    }

    // =========================================================
    // === params for ordinary album && showSingleMediaType: false ===
    const val SELECTION_ALBUM = "(${FileColumns.MEDIA_TYPE}=? OR ${FileColumns.MEDIA_TYPE}=?) AND bucket_id=? AND ${MediaColumns.SIZE}>0"

    fun getSelectionAlbumArgs(albumId: String): Array<String> {
      return arrayOf(FileColumns.MEDIA_TYPE_IMAGE.toString(), FileColumns.MEDIA_TYPE_VIDEO.toString(), albumId)
    }

    // ===============================================================
    // === params for ordinary album && showSingleMediaType: true ===
    const val SELECTION_ALBUM_FOR_SINGLE_MEDIA_TYPE = "${FileColumns.MEDIA_TYPE}=? AND bucket_id=? AND ${MediaColumns.SIZE}>0"

    fun getSelectionAlbumArgsForSingleMediaType(mediaType: Int, albumId: String): Array<String> {
      return arrayOf(mediaType.toString(), albumId)
    }

    // ===============================================================
    // === params for album ALL && showSingleMediaType: true && MineType=="image/gif"
    const val SELECTION_ALL_FOR_GIF = "${FileColumns.MEDIA_TYPE}=? AND ${MediaColumns.MIME_TYPE}=? AND ${MediaColumns.SIZE}>0"

    fun getSelectionArgsForGifType(mediaType: Int): Array<String> {
      return arrayOf(mediaType.toString(), "image/gif")
    }

    // ===============================================================
    // === params for ordinary album && showSingleMediaType: true  && MineType=="image/gif" ===
    const val SELECTION_ALBUM_FOR_GIF = "${FileColumns.MEDIA_TYPE}=? AND bucket_id=? AND ${MediaColumns.MIME_TYPE}=? AND ${MediaColumns.SIZE}>0"

    fun getSelectionAlbumArgsForGifType(mediaType: Int, albumId: String): Array<String> {
      return arrayOf(mediaType.toString(), albumId, "image/gif")
    }

    // ===============================================================
    const val ORDER_BY = Media.DATE_TAKEN + " DESC"

  }
}
