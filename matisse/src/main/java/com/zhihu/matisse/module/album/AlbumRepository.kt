package com.zhihu.matisse.module.album

import android.app.Application
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.zhihu.matisse.internal.entity.Album
import com.zhihu.matisse.internal.entity.SelectionSpec
import com.zhihu.matisse.internal.utils.Platform
import com.zhihu.matisse.internal.utils.Platform.beforeAndroidTen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class AlbumRepository @Inject constructor(private val context: Application, private val coroutineContext: CoroutineContext = Dispatchers.IO) {
  suspend fun loadAlbums(): List<Album> = withContext(coroutineContext) {
    val selectionSpec = SelectionSpec.getInstance()
    val (selection, selectionArgs) = buildSelection(selectionSpec)

    val projection = if (beforeAndroidTen()) AlbumScript.PROJECTION else AlbumScript.PROJECTION_29
    val uri = MediaStore.Files.getContentUri("external")

    val cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, AlbumScript.BUCKET_ORDER_BY) ?: return@withContext emptyList()
    return@withContext processCursor(cursor, beforeAndroidTen())
  }

  private fun buildSelection(selectionSpec: SelectionSpec): Pair<String, Array<String>> {
    return when {
      selectionSpec.onlyShowGif() -> Pair(
        if (beforeAndroidTen()) AlbumScript.SELECTION_FOR_SINGLE_MEDIA_GIF_TYPE else AlbumScript.SELECTION_FOR_SINGLE_MEDIA_GIF_TYPE_29,
        AlbumScript.getSelectionArgsForSingleMediaGifType(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)
      )

      selectionSpec.onlyShowImages() -> Pair(
        if (beforeAndroidTen()) AlbumScript.SELECTION_FOR_SINGLE_MEDIA_TYPE else AlbumScript.SELECTION_FOR_SINGLE_MEDIA_TYPE_29,
        AlbumScript.getSelectionArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)
      )

      selectionSpec.onlyShowVideos() -> Pair(
        if (beforeAndroidTen()) AlbumScript.SELECTION_FOR_SINGLE_MEDIA_TYPE else AlbumScript.SELECTION_FOR_SINGLE_MEDIA_TYPE_29,
        AlbumScript.getSelectionArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)
      )

      else -> Pair(
        if (beforeAndroidTen()) AlbumScript.SELECTION else AlbumScript.SELECTION_29, AlbumScript.SELECTION_ARGS
      )
    }
  }

  private fun processCursor(cursor: Cursor, isBeforeQ: Boolean): List<Album> {
    return if (isBeforeQ) processPreQ(cursor) else processPostQ(cursor)
  }

  private fun processPreQ(cursor: Cursor): List<Album> { // ... 原 loadInBackground 中针对 Android Q 之前的处理逻辑 ...
  }

  private fun processPostQ(cursor: Cursor): List<Album> { // ... 原 loadInBackground 中针对 Android Q 及之后的分组逻辑 ...
  }

}