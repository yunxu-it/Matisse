package com.zhihu.matisse.module.album

import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.database.MergeCursor
import android.net.Uri
import android.provider.MediaStore
import android.provider.MediaStore.Files.FileColumns
import android.provider.MediaStore.MediaColumns
import com.zhihu.matisse.internal.entity.Album
import com.zhihu.matisse.internal.entity.SelectionSpec
import com.zhihu.matisse.internal.utils.Platform.beforeAndroidTen
import com.zhihu.matisse.module.album.AlbumCollection.Companion.COLUMNS
import com.zhihu.matisse.module.album.AlbumCollection.Companion.COLUMN_BUCKET_DISPLAY_NAME
import com.zhihu.matisse.module.album.AlbumCollection.Companion.COLUMN_BUCKET_ID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class AlbumRepository(private val context: Context) {
  companion object {
    fun newInstance(context: Context): AlbumRepository {
      return AlbumRepository(context.applicationContext)
    }
  }

  private var coroutineContext: CoroutineContext = Dispatchers.IO

  suspend fun loadAlbums(): Cursor? = withContext(coroutineContext) {
    val selectionSpec = SelectionSpec.getInstance()
    val (selection, selectionArgs) = buildSelection(selectionSpec)

    val projection = if (beforeAndroidTen()) AlbumCollection.PROJECTION else AlbumCollection.PROJECTION_29
    val uri = MediaStore.Files.getContentUri("external")

    val cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, AlbumCollection.BUCKET_ORDER_BY) ?: return@withContext null
    return@withContext processCursor(cursor, beforeAndroidTen())
  }

  private fun buildSelection(selectionSpec: SelectionSpec): Pair<String, Array<String>> {
    return when {
      selectionSpec.onlyShowGif() -> Pair(
        if (beforeAndroidTen()) AlbumCollection.SELECTION_FOR_SINGLE_MEDIA_GIF_TYPE else AlbumCollection.SELECTION_FOR_SINGLE_MEDIA_GIF_TYPE_29,
        AlbumCollection.getSelectionArgsForSingleMediaGifType(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)
      )

      selectionSpec.onlyShowImages() -> Pair(
        if (beforeAndroidTen()) AlbumCollection.SELECTION_FOR_SINGLE_MEDIA_TYPE else AlbumCollection.SELECTION_FOR_SINGLE_MEDIA_TYPE_29,
        AlbumCollection.getSelectionArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)
      )

      selectionSpec.onlyShowVideos() -> Pair(
        if (beforeAndroidTen()) AlbumCollection.SELECTION_FOR_SINGLE_MEDIA_TYPE else AlbumCollection.SELECTION_FOR_SINGLE_MEDIA_TYPE_29,
        AlbumCollection.getSelectionArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)
      )

      else -> Pair(
        if (beforeAndroidTen()) AlbumCollection.SELECTION else AlbumCollection.SELECTION_29, AlbumCollection.SELECTION_ARGS
      )
    }
  }

  private fun processCursor(cursor: Cursor, isBeforeQ: Boolean): Cursor {
    return if (isBeforeQ) processPreQ(cursor) else processPostQ(cursor)
  }

  private fun processPreQ(albums: Cursor): Cursor { // ... 原 loadInBackground 中针对 Android Q 之前的处理逻辑 ...
    val allAlbum = MatrixCursor(COLUMNS)
    var totalCount = 0
    var allAlbumCoverUri: Uri? = null
    val otherAlbums = MatrixCursor(COLUMNS)
    while (albums.moveToNext()) {
      val fileId: Long = albums.getLong(albums.getColumnIndexOrThrow(FileColumns._ID))
      val bucketId: Long = albums.getLong(albums.getColumnIndexOrThrow(COLUMN_BUCKET_ID))
      val bucketDisplayName: String = albums.getString(albums.getColumnIndexOrThrow(COLUMN_BUCKET_DISPLAY_NAME))
      val mimeType: String = albums.getString(albums.getColumnIndexOrThrow(MediaColumns.MIME_TYPE))
      val uri: Uri = AlbumCollection.getUri(albums)
      val count: Int = albums.getInt(albums.getColumnIndexOrThrow(AlbumCollection.COLUMN_COUNT))

      otherAlbums.addRow(
        arrayOf(
          fileId.toString(), bucketId.toString(), bucketDisplayName, mimeType, uri.toString(), count.toString()
        )
      )
      totalCount += count
    }
    if (albums.moveToFirst()) {
      allAlbumCoverUri = AlbumCollection.getUri(albums)
    }

    allAlbum.addRow(
      arrayOf<String?>(
        Album.ALBUM_ID_ALL, Album.ALBUM_ID_ALL, Album.ALBUM_NAME_ALL, null, allAlbumCoverUri?.toString(), totalCount.toString()
      )
    )

    return MergeCursor(arrayOf<Cursor>(allAlbum, otherAlbums));
  }

  private fun processPostQ(albums: Cursor): Cursor { // ... 原 loadInBackground 中针对 Android Q 及之后的分组逻辑 ...
    val allAlbum = MatrixCursor(COLUMNS)

    var totalCount = 0
    var allAlbumCoverUri: Uri? = null

    // Pseudo GROUP BY
    val countMap: MutableMap<Long, Long> = HashMap()
    while (albums.moveToNext()) {
      val bucketId: Long = albums.getLong(albums.getColumnIndexOrThrow(COLUMN_BUCKET_ID))

      var count = countMap[bucketId]
      if (count == null) {
        count = 1L
      } else {
        count++
      }
      countMap[bucketId] = count
    }

    val otherAlbums = MatrixCursor(COLUMNS)
    if (albums.moveToFirst()) {
      allAlbumCoverUri = AlbumCollection.getUri(albums)

      val done: MutableSet<Long> = HashSet()

      do {
        val bucketId: Long = albums.getLong(albums.getColumnIndexOrThrow(COLUMN_BUCKET_ID))

        if (done.contains(bucketId)) {
          continue
        }

        val fileId: Long = albums.getLong(albums.getColumnIndexOrThrow(FileColumns._ID)) ?: 0L
        val bucketDisplayName: String = albums.getString(albums.getColumnIndexOrThrow(COLUMN_BUCKET_DISPLAY_NAME)) ?: ""
        val mimeType: String = albums.getString(albums.getColumnIndexOrThrow(MediaColumns.MIME_TYPE))
        val uri: Uri = AlbumCollection.getUri(albums)
        val count = countMap[bucketId]!!

        otherAlbums.addRow(arrayOf(fileId.toString(), bucketId.toString(), bucketDisplayName, mimeType, uri.toString(), count.toString()))
        done.add(bucketId)

        totalCount += count.toInt()
      } while (albums.moveToNext())
    }

    allAlbum.addRow(
      arrayOf<String?>(
        Album.ALBUM_ID_ALL, Album.ALBUM_ID_ALL, Album.ALBUM_NAME_ALL, null, allAlbumCoverUri?.toString(), totalCount.toString()
      )
    )

    return MergeCursor(arrayOf<Cursor>(allAlbum, otherAlbums))
  }

}