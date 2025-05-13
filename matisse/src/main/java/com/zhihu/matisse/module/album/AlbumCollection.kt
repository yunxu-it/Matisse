package com.zhihu.matisse.module.album

import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore.Files
import android.provider.MediaStore.Files.FileColumns
import android.provider.MediaStore.Images.Media
import android.provider.MediaStore.MediaColumns
import android.provider.MediaStore.Video
import com.zhihu.matisse.MimeType

class AlbumCollection {
  private val STATE_CURRENT_SELECTION = "state_current_selection"

  var currentSelection: Int = 0
    private set

  fun onRestoreInstanceState(savedInstanceState: Bundle?) {
    if (savedInstanceState == null) {
      return
    }

    currentSelection = savedInstanceState.getInt(STATE_CURRENT_SELECTION)
  }

  fun onSaveInstanceState(outState: Bundle) {
    outState.putInt(STATE_CURRENT_SELECTION, currentSelection)
  }

  fun setStateCurrentSelection(currentSelection: Int) {
    this.currentSelection = currentSelection
  }

  companion object {
    const val COLUMN_BUCKET_ID: String = "bucket_id"

    const val COLUMN_BUCKET_DISPLAY_NAME: String = "bucket_display_name"
    const val COLUMN_URI: String = "uri"
    const val COLUMN_COUNT: String = "count"

    val QUERY_URI: Uri = Files.getContentUri("external")

    val COLUMNS: Array<String> = arrayOf(
      FileColumns._ID, COLUMN_BUCKET_ID, COLUMN_BUCKET_DISPLAY_NAME, MediaColumns.MIME_TYPE, COLUMN_URI, COLUMN_COUNT
    )

    val PROJECTION: Array<String> = arrayOf(
      FileColumns._ID, COLUMN_BUCKET_ID, COLUMN_BUCKET_DISPLAY_NAME, MediaColumns.MIME_TYPE, "COUNT(*) AS $COLUMN_COUNT"
    )

    val PROJECTION_29: Array<String> = arrayOf(
      FileColumns._ID, COLUMN_BUCKET_ID, COLUMN_BUCKET_DISPLAY_NAME, MediaColumns.MIME_TYPE
    )

    // === params for showSingleMediaType: false ===

    val SELECTION: String = "(${FileColumns.MEDIA_TYPE}=? OR ${FileColumns.MEDIA_TYPE}=?) AND ${MediaColumns.SIZE}>0) GROUP BY (bucket_id"

    val SELECTION_29: String = ("(" + FileColumns.MEDIA_TYPE + "=?" + " OR " + FileColumns.MEDIA_TYPE + "=?)" + " AND " + MediaColumns.SIZE + ">0")

    val SELECTION_ARGS: Array<String> = arrayOf(
      FileColumns.MEDIA_TYPE_IMAGE.toString(), FileColumns.MEDIA_TYPE_VIDEO.toString(),
    )

    // =============================================
    // === params for showSingleMediaType: true ===

    val SELECTION_FOR_SINGLE_MEDIA_TYPE: String = FileColumns.MEDIA_TYPE + "=?" + " AND " + MediaColumns.SIZE + ">0" + ") GROUP BY (bucket_id"

    val SELECTION_FOR_SINGLE_MEDIA_TYPE_29: String = FileColumns.MEDIA_TYPE + "=?" + " AND " + MediaColumns.SIZE + ">0"

    fun getSelectionArgsForSingleMediaType(mediaType: Int): Array<String> {
      return arrayOf(mediaType.toString())
    }

    // =============================================
    // === params for showSingleMediaType: true ===

    const val SELECTION_FOR_SINGLE_MEDIA_GIF_TYPE: String =
      (FileColumns.MEDIA_TYPE + "=?" + " AND " + MediaColumns.SIZE + ">0" + " AND " + MediaColumns.MIME_TYPE + "=?" + ") GROUP BY (bucket_id")

    const val SELECTION_FOR_SINGLE_MEDIA_GIF_TYPE_29: String =
      FileColumns.MEDIA_TYPE + "=?" + " AND " + MediaColumns.SIZE + ">0" + " AND " + MediaColumns.MIME_TYPE + "=?"

    fun getSelectionArgsForSingleMediaGifType(mediaType: Int): Array<String> {
      return arrayOf(mediaType.toString(), "image/gif")
    }

    // =============================================

    const val BUCKET_ORDER_BY: String = "datetaken DESC"

    fun getUri(cursor: Cursor): Uri {
      val id = cursor.getLong(cursor.getColumnIndexOrThrow(FileColumns._ID))
      val mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaColumns.MIME_TYPE))

      val contentUri = if (MimeType.isImage(mimeType)) {
        Media.EXTERNAL_CONTENT_URI
      } else if (MimeType.isVideo(mimeType)) {
        Video.Media.EXTERNAL_CONTENT_URI
      } else {
        Files.getContentUri("external")
      }

      val uri = ContentUris.withAppendedId(contentUri, id)
      return uri
    }
  }
}