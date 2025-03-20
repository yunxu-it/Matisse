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
package com.zhihu.matisse.internal.loader

import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.database.MergeCursor
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.provider.MediaStore
import android.provider.MediaStore.Files.FileColumns
import android.provider.MediaStore.Images.Media
import android.provider.MediaStore.MediaColumns
import android.provider.MediaStore.VOLUME_EXTERNAL
import androidx.loader.content.CursorLoader
import com.zhihu.matisse.internal.entity.Album
import com.zhihu.matisse.internal.entity.Item
import com.zhihu.matisse.internal.entity.SelectionSpec
import com.zhihu.matisse.internal.utils.MediaStoreCompat

/**
 * Load images and videos into a single cursor.
 */
class AlbumMediaLoader private constructor(context: Context, selection: String, selectionArgs: Array<String>, private val mEnableCapture: Boolean) :
  CursorLoader(context, QUERY_URI, PROJECTION, selection, selectionArgs, ORDER_BY) {

  override fun loadInBackground(): Cursor? {
    val result = super.loadInBackground()
    if (!mEnableCapture || !MediaStoreCompat.hasCameraFeature(context)) {
      return result
    }
    val dummy = MatrixCursor(PROJECTION)
    dummy.addRow(arrayOf<Any>(Item.ITEM_ID_CAPTURE, Item.ITEM_DISPLAY_NAME_CAPTURE, "", 0, 0))
    return MergeCursor(arrayOf(dummy, result))
  }

  override fun onContentChanged() { // FIXME a dirty way to fix loading multiple times
  }

  companion object {
    private val QUERY_URI: Uri = if (VERSION.SDK_INT >= VERSION_CODES.Q) {
      MediaStore.Files.getContentUri(VOLUME_EXTERNAL)
    } else {
      MediaStore.Files.getContentUri("external")
    };

    private val PROJECTION = arrayOf(FileColumns._ID, MediaColumns.DISPLAY_NAME, MediaColumns.MIME_TYPE, MediaColumns.SIZE, MediaColumns.DURATION)

    // === params for album ALL && showSingleMediaType: false ===
    private const val SELECTION_ALL = "(${FileColumns.MEDIA_TYPE}=? OR ${FileColumns.MEDIA_TYPE}=?) AND ${MediaColumns.SIZE}>0"

    private val SELECTION_ALL_ARGS = arrayOf(FileColumns.MEDIA_TYPE_IMAGE.toString(), FileColumns.MEDIA_TYPE_VIDEO.toString())

    // ===========================================================
    // === params for album ALL && showSingleMediaType: true ===
    private const val SELECTION_ALL_FOR_SINGLE_MEDIA_TYPE = "${FileColumns.MEDIA_TYPE}=? AND ${MediaColumns.SIZE}>0"

    private fun getSelectionArgsForSingleMediaType(mediaType: Int): Array<String> {
      return arrayOf(mediaType.toString())
    }

    // =========================================================
    // === params for ordinary album && showSingleMediaType: false ===
    private const val SELECTION_ALBUM = "(${FileColumns.MEDIA_TYPE}=? OR ${FileColumns.MEDIA_TYPE}=?) AND bucket_id=? AND ${MediaColumns.SIZE}>0"

    private fun getSelectionAlbumArgs(albumId: String): Array<String> {
      return arrayOf(FileColumns.MEDIA_TYPE_IMAGE.toString(), FileColumns.MEDIA_TYPE_VIDEO.toString(), albumId)
    }

    // ===============================================================
    // === params for ordinary album && showSingleMediaType: true ===
    private const val SELECTION_ALBUM_FOR_SINGLE_MEDIA_TYPE = "${FileColumns.MEDIA_TYPE}=? AND bucket_id=? AND ${MediaColumns.SIZE}>0"

    private fun getSelectionAlbumArgsForSingleMediaType(mediaType: Int, albumId: String): Array<String> {
      return arrayOf(mediaType.toString(), albumId)
    }

    // ===============================================================
    // === params for album ALL && showSingleMediaType: true && MineType=="image/gif"
    private const val SELECTION_ALL_FOR_GIF = "${FileColumns.MEDIA_TYPE}=? AND ${MediaColumns.MIME_TYPE}=? AND ${MediaColumns.SIZE}>0"

    private fun getSelectionArgsForGifType(mediaType: Int): Array<String> {
      return arrayOf(mediaType.toString(), "image/gif")
    }

    // ===============================================================
    // === params for ordinary album && showSingleMediaType: true  && MineType=="image/gif" ===
    private const val SELECTION_ALBUM_FOR_GIF = "${FileColumns.MEDIA_TYPE}=? AND bucket_id=? AND ${MediaColumns.MIME_TYPE}=? AND ${MediaColumns.SIZE}>0"

    private fun getSelectionAlbumArgsForGifType(mediaType: Int, albumId: String): Array<String> {
      return arrayOf(mediaType.toString(), albumId, "image/gif")
    }

    // ===============================================================
    private const val ORDER_BY = Media.DATE_TAKEN + " DESC"

    fun newInstance(context: Context, album: Album, capture: Boolean): CursorLoader {
      val selection: String
      val selectionArgs: Array<String>
      val enableCapture: Boolean

      if (album.isAll) {
        if (SelectionSpec.getInstance().onlyShowGif()) {
          selection = SELECTION_ALL_FOR_GIF
          selectionArgs = getSelectionArgsForGifType(
            FileColumns.MEDIA_TYPE_IMAGE
          )
        } else if (SelectionSpec.getInstance().onlyShowImages()) {
          selection = SELECTION_ALL_FOR_SINGLE_MEDIA_TYPE
          selectionArgs = getSelectionArgsForSingleMediaType(
            FileColumns.MEDIA_TYPE_IMAGE
          )
        } else if (SelectionSpec.getInstance().onlyShowVideos()) {
          selection = SELECTION_ALL_FOR_SINGLE_MEDIA_TYPE
          selectionArgs = getSelectionArgsForSingleMediaType(
            FileColumns.MEDIA_TYPE_VIDEO
          )
        } else {
          selection = SELECTION_ALL
          selectionArgs = SELECTION_ALL_ARGS
        }
        enableCapture = capture
      } else {
        if (SelectionSpec.getInstance().onlyShowGif()) {
          selection = SELECTION_ALBUM_FOR_GIF
          selectionArgs = getSelectionAlbumArgsForGifType(
            FileColumns.MEDIA_TYPE_IMAGE, album.id
          )
        } else if (SelectionSpec.getInstance().onlyShowImages()) {
          selection = SELECTION_ALBUM_FOR_SINGLE_MEDIA_TYPE
          selectionArgs = getSelectionAlbumArgsForSingleMediaType(
            FileColumns.MEDIA_TYPE_IMAGE, album.id
          )
        } else if (SelectionSpec.getInstance().onlyShowVideos()) {
          selection = SELECTION_ALBUM_FOR_SINGLE_MEDIA_TYPE
          selectionArgs = getSelectionAlbumArgsForSingleMediaType(
            FileColumns.MEDIA_TYPE_VIDEO, album.id
          )
        } else {
          selection = SELECTION_ALBUM
          selectionArgs = getSelectionAlbumArgs(album.id)
        }
        enableCapture = false
      }
      return AlbumMediaLoader(context, selection, selectionArgs, enableCapture)
    }
  }
}
