package com.zhihu.matisse.module.album

import android.net.Uri
import android.provider.MediaStore.Files
import android.provider.MediaStore.Files.FileColumns
import android.provider.MediaStore.MediaColumns

object AlbumScript {
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

  val SELECTION_FOR_SINGLE_MEDIA_GIF_TYPE: String =
    (FileColumns.MEDIA_TYPE + "=?" + " AND " + MediaColumns.SIZE + ">0" + " AND " + MediaColumns.MIME_TYPE + "=?" + ") GROUP BY (bucket_id")

  val SELECTION_FOR_SINGLE_MEDIA_GIF_TYPE_29: String =
    FileColumns.MEDIA_TYPE + "=?" + " AND " + MediaColumns.SIZE + ">0" + " AND " + MediaColumns.MIME_TYPE + "=?"

  fun getSelectionArgsForSingleMediaGifType(mediaType: Int): Array<String> {
    return arrayOf(mediaType.toString(), "image/gif")
  }

  // =============================================

  const val BUCKET_ORDER_BY: String = "datetaken DESC"
}