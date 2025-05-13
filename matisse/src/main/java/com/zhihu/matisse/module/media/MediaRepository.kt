package com.zhihu.matisse.module.media

import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.database.MergeCursor
import android.provider.MediaStore.Files.FileColumns
import com.zhihu.matisse.internal.entity.Album
import com.zhihu.matisse.internal.entity.Item
import com.zhihu.matisse.internal.entity.SelectionSpec
import com.zhihu.matisse.internal.utils.MediaStoreCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class MediaRepository(private val context: Context) {
  data class Param(
    val selection: String,
    val selectionArgs: Array<String>,
    val enableCapturePhoto: Boolean,
    val enableCaptureVideo: Boolean
  ) {
    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (javaClass != other?.javaClass) return false
      other as Param
      if (enableCapturePhoto != other.enableCapturePhoto) return false
      if (enableCaptureVideo != other.enableCaptureVideo) return false
      if (selection != other.selection) return false
      if (!selectionArgs.contentEquals(other.selectionArgs)) return false

      return true
    }

    override fun hashCode(): Int {
      var result = enableCapturePhoto.hashCode()
      result = 31 * result + enableCaptureVideo.hashCode()
      result = 31 * result + selection.hashCode()
      result = 31 * result + selectionArgs.contentHashCode()
      return result
    }
  }

  companion object {
    fun newInstance(context: Context): MediaRepository {
      return MediaRepository(context.applicationContext)
    }
  }

  private var coroutineContext: CoroutineContext = Dispatchers.IO

  suspend fun loadAlbumMediaList(album: Album, needCaptureItem: Boolean = false): Cursor? = withContext(coroutineContext) {
    val selectionSpec = SelectionSpec.getInstance()
    val (selection, selectionArgs, enableCapturePhoto, enableCaptureVideo) = buildSelection(selectionSpec, album)

    val projection = AlbumMediaCollection.PROJECTION
    val uri = AlbumMediaCollection.QUERY_URI

    val cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, AlbumMediaCollection.ORDER_BY) ?: return@withContext null
    return@withContext processCursor(cursor, needCaptureItem && enableCapturePhoto, needCaptureItem && enableCaptureVideo)
  }

  private fun buildSelection(selectionSpec: SelectionSpec, album: Album): Param {
    val selection: String
    val selectionArgs: Array<String>
    var enableCapturePhoto = false
    var enableCaptureVideo = false

    if (album.isAll) {
      if (selectionSpec.onlyShowGif()) {
        selection = AlbumMediaCollection.SELECTION_ALL_FOR_GIF
        selectionArgs = AlbumMediaCollection.getSelectionArgsForGifType(
          FileColumns.MEDIA_TYPE_IMAGE
        )
      } else if (selectionSpec.onlyShowImages()) {
        selection = AlbumMediaCollection.SELECTION_ALL_FOR_SINGLE_MEDIA_TYPE
        selectionArgs = AlbumMediaCollection.getSelectionArgsForSingleMediaType(
          FileColumns.MEDIA_TYPE_IMAGE
        )
      } else if (selectionSpec.onlyShowVideos()) {
        selection = AlbumMediaCollection.SELECTION_ALL_FOR_SINGLE_MEDIA_TYPE
        selectionArgs = AlbumMediaCollection.getSelectionArgsForSingleMediaType(
          FileColumns.MEDIA_TYPE_VIDEO
        )
      } else {
        selection = AlbumMediaCollection.SELECTION_ALL
        selectionArgs = AlbumMediaCollection.SELECTION_ALL_ARGS
      }
      enableCapturePhoto = selectionSpec.showImages()
      enableCaptureVideo = selectionSpec.showVideos()
    } else {
      if (selectionSpec.onlyShowGif()) {
        selection = AlbumMediaCollection.SELECTION_ALBUM_FOR_GIF
        selectionArgs = AlbumMediaCollection.getSelectionAlbumArgsForGifType(
          FileColumns.MEDIA_TYPE_IMAGE, album.id
        )
      } else if (selectionSpec.onlyShowImages()) {
        selection = AlbumMediaCollection.SELECTION_ALBUM_FOR_SINGLE_MEDIA_TYPE
        selectionArgs = AlbumMediaCollection.getSelectionAlbumArgsForSingleMediaType(
          FileColumns.MEDIA_TYPE_IMAGE, album.id
        )
      } else if (selectionSpec.onlyShowVideos()) {
        selection = AlbumMediaCollection.SELECTION_ALBUM_FOR_SINGLE_MEDIA_TYPE
        selectionArgs = AlbumMediaCollection.getSelectionAlbumArgsForSingleMediaType(
          FileColumns.MEDIA_TYPE_VIDEO, album.id
        )
      } else {
        selection = AlbumMediaCollection.SELECTION_ALBUM
        selectionArgs = AlbumMediaCollection.getSelectionAlbumArgs(album.id)
      }
    }
    return Param(selection, selectionArgs, enableCapturePhoto, enableCaptureVideo)
  }

  private fun processCursor(cursor: Cursor, enableCapturePhoto: Boolean, enableCaptureVideo: Boolean): Cursor {
    if (MediaStoreCompat.hasCameraFeature(context)) {
      if (enableCapturePhoto || enableCaptureVideo) {
        val dummy = MatrixCursor(AlbumMediaCollection.PROJECTION)
        if (enableCapturePhoto) {
          dummy.addRow(arrayOf<Any>(Item.ITEM_ID_CAPTURE_PHOTO, Item.ITEM_DISPLAY_NAME_CAPTURE, "", 0, 0))
        }
        if (enableCaptureVideo) {
          dummy.addRow(arrayOf<Any>(Item.ITEM_ID_CAPTURE_VIDEO, Item.ITEM_DISPLAY_NAME_CAPTURE, "", 0, 0))
        }
        return MergeCursor(arrayOf(dummy, cursor))
      }
    }
    return cursor
  }

}