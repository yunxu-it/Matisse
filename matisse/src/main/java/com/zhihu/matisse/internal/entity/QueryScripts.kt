package com.zhihu.matisse.internal.entity

import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.provider.MediaStore.Files
import android.provider.MediaStore.Files.FileColumns
import android.provider.MediaStore.Images.Media
import android.provider.MediaStore.MediaColumns
import android.provider.MediaStore.Video
import com.zhihu.matisse.MimeType

object QueryScripts {
  fun beforeAndroidTen(): Boolean {
    return VERSION.SDK_INT < VERSION_CODES.Q
  }

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

    return ContentUris.withAppendedId(contentUri, id)
  }

  const val COLUMN_BUCKET_ID: String = "bucket_id"

  const val COLUMN_BUCKET_DISPLAY_NAME: String = "bucket_display_name"
  const val COLUMN_URI: String = "uri"
  const val COLUMN_COUNT: String = "count"

  val QUERY_URI: Uri = Files.getContentUri("external")

  val COLUMNS: Array<String> = arrayOf(
    FileColumns._ID, COLUMN_BUCKET_ID, COLUMN_BUCKET_DISPLAY_NAME, MediaColumns.MIME_TYPE, COLUMN_URI, COLUMN_COUNT
  )

  private val PROJECTION_ALBUM: Array<String> = arrayOf(
    FileColumns._ID, COLUMN_BUCKET_ID, COLUMN_BUCKET_DISPLAY_NAME, MediaColumns.MIME_TYPE, "COUNT(*) AS $COLUMN_COUNT"
  )

  private val PROJECTION_ALBUM_29: Array<String> = arrayOf(
    FileColumns._ID, COLUMN_BUCKET_ID, COLUMN_BUCKET_DISPLAY_NAME, MediaColumns.MIME_TYPE
  )

  fun projectionAlbum(): Array<String> = if (beforeAndroidTen()) PROJECTION_ALBUM else PROJECTION_ALBUM_29

  // === params for showSingleMediaType: false ===

  // 所有相册 所有媒体
  private const val SELECTION: String = "(${FileColumns.MEDIA_TYPE}=? OR ${FileColumns.MEDIA_TYPE}=?) AND ${MediaColumns.SIZE}>0) GROUP BY (bucket_id"

  private const val SELECTION_29: String = "(${FileColumns.MEDIA_TYPE}=? OR ${FileColumns.MEDIA_TYPE}=?) AND ${MediaColumns.SIZE}>0"

  private val ARGS_VIDEO_AND_IMAGE: Array<String> = arrayOf(FileColumns.MEDIA_TYPE_IMAGE.toString(), FileColumns.MEDIA_TYPE_VIDEO.toString())

  fun selectionAllForAllAlbum(): String {
    return if (beforeAndroidTen()) SELECTION else SELECTION_29
  }

  fun argsForImageAndVideo(): Array<String> {
    return ARGS_VIDEO_AND_IMAGE
  }

  // 单个相册 所有媒体
  const val SELECTION_ALL_BY_ALBUM = "(${FileColumns.MEDIA_TYPE}=? OR ${FileColumns.MEDIA_TYPE}=?) AND bucket_id=? AND ${MediaColumns.SIZE}>0"

  fun selectionAllByAlbumArgs(albumId: String): Array<String> {
    return arrayOf(FileColumns.MEDIA_TYPE_IMAGE.toString(), FileColumns.MEDIA_TYPE_VIDEO.toString(), albumId)
  }

  // =============================================
  // === params for showSingleMediaType: true ===

  private const val SELECTION_FOR_SINGLE_MEDIA_TYPE_OLD: String = "${FileColumns.MEDIA_TYPE}=? AND ${MediaColumns.SIZE}>0) GROUP BY (bucket_id"

  private const val SELECTION_FOR_SINGLE_MEDIA_TYPE_29: String = "${FileColumns.MEDIA_TYPE}=? AND ${MediaColumns.SIZE}>0"

  fun selectionArgsForSingleMediaType(mediaType: Int): Array<String> {
    return arrayOf(mediaType.toString())
  }

  fun selectionForSingleMediaType(): String {
    return if (beforeAndroidTen()) SELECTION_FOR_SINGLE_MEDIA_TYPE_OLD else SELECTION_FOR_SINGLE_MEDIA_TYPE_29
  }

  fun selectionMediaForSingleMediaType(): String {
    return SELECTION_FOR_SINGLE_MEDIA_TYPE_29
  }

  const val SELECTION_ALBUM_FOR_SINGLE_MEDIA_TYPE = "${FileColumns.MEDIA_TYPE}=? AND bucket_id=? AND ${MediaColumns.SIZE}>0"

  fun getSelectionAlbumArgsForSingleMediaType(mediaType: Int, albumId: String): Array<String> {
    return arrayOf(mediaType.toString(), albumId)
  }

  // =============================================
  // === params for showSingleMediaType: true ===

  private const val SELECTION_FOR_SINGLE_MEDIA_GIF_TYPE: String =
    "${FileColumns.MEDIA_TYPE}=? AND ${MediaColumns.SIZE}>0 AND ${MediaColumns.MIME_TYPE}=?) GROUP BY (bucket_id"

  private const val SELECTION_FOR_SINGLE_MEDIA_GIF_TYPE_29: String = "${FileColumns.MEDIA_TYPE}=? AND ${MediaColumns.SIZE}>0 AND ${MediaColumns.MIME_TYPE}=?"

  fun selectionForSingleMediaGifType(): String {
    return if (beforeAndroidTen()) SELECTION_FOR_SINGLE_MEDIA_GIF_TYPE else SELECTION_FOR_SINGLE_MEDIA_GIF_TYPE_29
  }

  fun selectionArgsForSingleMediaGifType(): Array<String> {
    return arrayOf(FileColumns.MEDIA_TYPE_IMAGE.toString(), "image/gif")
  }

  // media
  const val SELECTION_ALBUM_FOR_GIF = "${FileColumns.MEDIA_TYPE}=? AND bucket_id=? AND ${MediaColumns.MIME_TYPE}=? AND ${MediaColumns.SIZE}>0"

  fun selectionAlbumArgsForGifType(albumId: String): Array<String> {
    return arrayOf(FileColumns.MEDIA_TYPE_IMAGE.toString(), albumId, "image/gif")
  }

  fun selectionMediaForSingleMediaGifType(): String {
    return SELECTION_FOR_SINGLE_MEDIA_GIF_TYPE_29
  }

  const val ORDER_BY = "${Media.DATE_TAKEN} DESC"

  // ------------------------------------- media --------------------------------------------------
  val PROJECTION_MEDIA = arrayOf(FileColumns._ID, MediaColumns.DISPLAY_NAME, MediaColumns.MIME_TYPE, MediaColumns.SIZE, MediaColumns.DURATION)

  fun selectionMediaAll(): String {
    return SELECTION_29
  }

}