package com.zhihu.matisse.sample

import android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.core.content.ContextCompat.checkSelfPermission

object MediaType {
  const val IMAGE = 0
  const val VIDEO = 1
  const val ALL = 2
}

enum class StorageAccess {
  Full, Partial, Denied
}

object DynamicPermission {

  fun getImagePermissions(): Array<String> {
    return getMediaPermissions(MediaType.IMAGE)
  }

  fun getVideoPermissions(): Array<String> {
    return getMediaPermissions(MediaType.VIDEO)
  }

  fun getMediaPermissions(type: Int = MediaType.ALL): Array<String> {
    val list: MutableList<String> = ArrayList()
    if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) { // 33 图片视频选择单独访问权限
      if (VERSION.SDK_INT >= VERSION_CODES.UPSIDE_DOWN_CAKE) { // 34 部分访问权限
        list.add(READ_MEDIA_VISUAL_USER_SELECTED)
      }
      when (type) {
        MediaType.IMAGE -> {
          list.add(READ_MEDIA_IMAGES)
        }

        MediaType.VIDEO -> {
          list.add(READ_MEDIA_VIDEO)
        }

        else -> {
          list.add(READ_MEDIA_IMAGES)
          list.add(READ_MEDIA_VIDEO)
        }
      }
    } else {
      if (VERSION.SDK_INT >= VERSION_CODES.R) { // 30 分区访问，需要添加申请权限
        list.add(MANAGE_EXTERNAL_STORAGE)
      }
      list.add(READ_EXTERNAL_STORAGE) // 29及以下访问权限
    }
    return list.toTypedArray()
  }

  fun getStorageAccess(context: Context): StorageAccess {
    return if (checkSelfPermission(context, READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED || checkSelfPermission(
        context,
        READ_MEDIA_VIDEO
      ) == PackageManager.PERMISSION_GRANTED
    ) { // Full access on Android 13+
      StorageAccess.Full
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && checkSelfPermission(
        context,
        READ_MEDIA_VISUAL_USER_SELECTED
      ) == PackageManager.PERMISSION_GRANTED
    ) { // Partial access on Android 13+
      StorageAccess.Partial
    } else if (checkSelfPermission(context, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) { // Full access up to Android 12
      StorageAccess.Full
    } else { // Access denied
      StorageAccess.Denied
    }
  }
}

