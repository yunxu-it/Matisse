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
package com.zhihu.matisse.sample

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.MimeType.GIF
import com.zhihu.matisse.engine.impl.GlideEngine
import com.zhihu.matisse.engine.impl.PicassoEngine
import com.zhihu.matisse.filter.Filter
import com.zhihu.matisse.internal.entity.CaptureStrategy
import com.zhihu.matisse.listener.OnCheckedListener
import com.zhihu.matisse.listener.OnSelectedListener
import com.zhihu.matisse.sample.SampleActivity.UriAdapter.UriViewHolder

class SampleActivity : AppCompatActivity() {
  private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
      val data: Intent? = result.data
      mAdapter!!.setData(Matisse.obtainResult(data), Matisse.obtainPathResult(data))
      Log.e("OnActivityResult ", Matisse.obtainOriginalState(data).toString())
    } else {
      // Handle the case where the result is not OK
    }
  }

  private val requestPermissionLauncher = registerForActivityResult(RequestMultiplePermissions()) { permissions: Map<String, Boolean> ->
    val storageAccess = DynamicPermission.getStorageAccess(this)
    if (storageAccess != StorageAccess.Denied) {
      startAction()
    }
  }

  private var type: Int = 0

  private var mAdapter: UriAdapter? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    findViewById<View>(R.id.zhihu).setOnClickListener { v -> onClick(0) }
    findViewById<View>(R.id.dracula).setOnClickListener { v -> onClick(1) }
    findViewById<View>(R.id.only_gif).setOnClickListener { v -> onClick(2) }

    val recyclerView = findViewById<View>(R.id.recyclerview) as RecyclerView
    recyclerView.layoutManager = LinearLayoutManager(this)
    recyclerView.adapter = UriAdapter().also { mAdapter = it }
  }

  fun onClick(type: Int) {
    this.type = type
    requestPermissionLauncher.launch(DynamicPermission.getMediaPermissions())
  }

  // </editor-fold>
  private fun startAction() {
    when (this.type) {
      0 -> {
        Matisse.from(this@SampleActivity)
          .choose(MimeType.ofImage(), false)
          .countable(true)
          .capture(true)
          .captureStrategy(CaptureStrategy(true, "com.zhihu.matisse.sample.fileprovider", "test"))
          .maxSelectable(9)
          .addFilter(GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
          .gridExpectedSize(resources.getDimensionPixelSize(R.dimen.grid_expected_size))
          .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
          .thumbnailScale(0.85f)
          .imageEngine(GlideEngine())
          .setOnSelectedListener(object : OnSelectedListener {
            override fun onSelected(uriList: List<Uri?>, pathList: List<String?>) {
              Log.e(
                "onSelected",
                "onSelected: pathList=$pathList"
              )
            }
          })
          .showSingleMediaType(true)
          .originalEnable(true)
          .maxOriginalSize(10)
          .autoHideToolbarOnSingleTap(true)
          .setOnCheckedListener(object : OnCheckedListener {
            override fun onCheck(isChecked: Boolean) {
              Log.e("isChecked", "onCheck: isChecked=$isChecked")
            }
          })
          .forResult(resultLauncher)
      }

      1 -> {
        Matisse.from(this@SampleActivity)
          .choose(MimeType.ofImage())
          .countable(false)
          .capture(true)
          .captureStrategy(CaptureStrategy(true, "com.zhihu.matisse.sample.fileprovider", "test"))
          .addFilter(GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
          .maxSelectable(9)
          .originalEnable(true)
          .maxOriginalSize(10)
          .imageEngine(PicassoEngine())
          .forResult(resultLauncher)
      }

      2 -> {
        Matisse.from(this@SampleActivity)
          .choose(MimeType.of(GIF), false)
          .countable(true)
          .maxSelectable(9)
          .addFilter(GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
          .gridExpectedSize(resources.getDimensionPixelSize(R.dimen.grid_expected_size))
          .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
          .thumbnailScale(0.85f)
          .imageEngine(GlideEngine())
          .showSingleMediaType(true)
          .originalEnable(true)
          .maxOriginalSize(10)
          .autoHideToolbarOnSingleTap(true)
          .forResult(resultLauncher)
      }
    }
    mAdapter!!.setData(null, null)
  }

  private class UriAdapter : Adapter<UriViewHolder>() {
    private var mUris: List<Uri>? = null
    private var mPaths: List<String>? = null

    fun setData(uris: List<Uri>?, paths: List<String>?) {
      mUris = uris
      mPaths = paths
      notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UriViewHolder {
      return UriViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.uri_item, parent, false))
    }

    override fun onBindViewHolder(holder: UriViewHolder, position: Int) {
      holder.mUri.text = mUris!![position].toString()
      holder.mPath.text = mPaths!![position]

      holder.mUri.alpha = if (position % 2 == 0) 1.0f else 0.54f
      holder.mPath.alpha = if (position % 2 == 0) 1.0f else 0.54f
    }

    override fun getItemCount(): Int {
      return if (mUris == null) 0 else mUris!!.size
    }

    class UriViewHolder(contentView: View) : ViewHolder(contentView) {
      val mUri: TextView = contentView.findViewById<View>(R.id.uri) as TextView
      val mPath: TextView = contentView.findViewById<View>(R.id.path) as TextView
    }
  }

}
