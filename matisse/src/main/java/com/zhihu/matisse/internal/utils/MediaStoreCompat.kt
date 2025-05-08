/*
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zhihu.matisse.internal.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.core.os.EnvironmentCompat
import androidx.fragment.app.Fragment
import com.zhihu.matisse.internal.entity.CaptureStrategy
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MediaStoreCompat {
  private val mContext: WeakReference<Activity>
  private val mFragment: WeakReference<Fragment>?
  private var mCaptureStrategy: CaptureStrategy? = null

  var currentPhotoUri: Uri? = null
    private set
  var currentPhotoPath: String? = null
    private set

  constructor(activity: Activity) {
    mContext = WeakReference(activity)
    mFragment = null
  }

  constructor(activity: Activity, fragment: Fragment) {
    mContext = WeakReference(activity)
    mFragment = WeakReference(fragment)
  }

  fun setCaptureStrategy(strategy: CaptureStrategy?) {
    mCaptureStrategy = strategy
  }

  fun dispatchCapturePhotoIntent(context: Context, requestCode: Int) {
    val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    if (captureIntent.resolveActivity(context.packageManager) != null) {
      var photoFile: File? = null
      try {
        photoFile = createImageFile()
      } catch (e: IOException) {
        e.printStackTrace()
      }

      if (photoFile != null) {
        currentPhotoPath = photoFile.absolutePath
        currentPhotoUri = FileProvider.getUriForFile(mContext.get()!!, mCaptureStrategy!!.authority, photoFile)
        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri)
        captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        if (mFragment != null) {
          mFragment.get()!!.startActivityForResult(captureIntent, requestCode)
        } else {
          mContext.get()!!.startActivityForResult(captureIntent, requestCode)
        }
      }
    }
  }

  fun dispatchCaptureVideoIntent(context: Context, requestCode: Int) {
    val captureIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
    if (captureIntent.resolveActivity(context.packageManager) != null) {
      var photoFile: File? = null
      try {
        photoFile = createVideoFile()
      } catch (e: IOException) {
        e.printStackTrace()
      }

      if (photoFile != null) {
        currentPhotoPath = photoFile.absolutePath
        currentPhotoUri = FileProvider.getUriForFile(mContext.get()!!, mCaptureStrategy!!.authority, photoFile)
        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri)
        captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        if (mFragment != null) {
          mFragment.get()!!.startActivityForResult(captureIntent, requestCode)
        } else {
          mContext.get()!!.startActivityForResult(captureIntent, requestCode)
        }
      }
    }
  }

  @Throws(IOException::class)
  private fun createImageFile(): File? {
    // Create an image file name
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = String.format("JPEG_%s.jpg", timeStamp)
    var storageDir: File?
    if (mCaptureStrategy!!.isPublic) {
      storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
      if (!storageDir.exists()) storageDir.mkdirs()
    } else {
      storageDir = mContext.get()!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    }
    if (mCaptureStrategy!!.directory != null) {
      storageDir = File(storageDir, mCaptureStrategy!!.directory)
      if (!storageDir.exists()) storageDir.mkdirs()
    }

    // Avoid joining path components manually
    val tempFile = File(storageDir, imageFileName)

    // Handle the situation that user's external storage is not ready
    if (Environment.MEDIA_MOUNTED != EnvironmentCompat.getStorageState(tempFile)) {
      return null
    }

    return tempFile
  }

  @Throws(IOException::class)
  private fun createVideoFile(): File? {
    // Create an image file name
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = String.format("JPEG_%s.mp4", timeStamp)
    var storageDir: File?
    if (mCaptureStrategy!!.isPublic) {
      storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
      if (!storageDir.exists()) storageDir.mkdirs()
    } else {
      storageDir = mContext.get()!!.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
    }
    if (mCaptureStrategy!!.directory != null) {
      storageDir = File(storageDir, mCaptureStrategy!!.directory)
      if (!storageDir.exists()) {
        storageDir.mkdirs()
      }
    }

    // Avoid joining path components manually
    val tempFile = File(storageDir, imageFileName)

    // Handle the situation that user's external storage is not ready
    if (Environment.MEDIA_MOUNTED != EnvironmentCompat.getStorageState(tempFile)) {
      return null
    }

    return tempFile
  }

  companion object {
    /**
     * Checks whether the device has a camera feature or not.
     *
     * @param context a context to check for camera feature.
     * @return true if the device has a camera feature. false otherwise.
     */
    fun hasCameraFeature(context: Context): Boolean {
      val pm = context.applicationContext.packageManager
      return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)
    }
  }
}
