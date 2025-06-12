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
package com.zhihu.matisse.module.preview

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.zhihu.matisse.R
import com.zhihu.matisse.base.BaseVBFragment
import com.zhihu.matisse.databinding.FragmentPreviewItemBinding
import com.zhihu.matisse.internal.entity.Item
import com.zhihu.matisse.internal.entity.SelectionSpec.Companion.getInstance
import com.zhihu.matisse.internal.utils.PhotoMetadataUtils
import com.zhihu.matisse.listener.OnFragmentInteractionListener
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.DisplayType.FIT_TO_SCREEN

class PreviewItemFragment : BaseVBFragment<FragmentPreviewItemBinding>() {
  private var mListener: OnFragmentInteractionListener? = null

  override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentPreviewItemBinding {
    return FragmentPreviewItemBinding.inflate(inflater, container, false)
  }

  override fun initView() {
  }

  override fun initData() {
    super.initData()
    val item = requireArguments().getParcelable<Item>(ARGS_ITEM) ?: return
    if (item.isVideo) {
      binding.videoPlayButton.visibility = View.VISIBLE
      binding.videoPlayButton.setOnClickListener {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(item.contentUri(), "video/*")
        try {
          startActivity(intent)
        } catch (e: ActivityNotFoundException) {
          Toast.makeText(context, R.string.error_no_video_activity, Toast.LENGTH_SHORT).show()
        }
      }
    } else {
      binding.videoPlayButton.visibility = View.GONE
    }

    binding.imageView.displayType = FIT_TO_SCREEN
    binding.imageView.setSingleTapListener {
      if (mListener != null) {
        mListener!!.onClick()
      }
    }

    val size = PhotoMetadataUtils.getBitmapSize(item.contentUri(), activity)
    if (item.isGif) {
      getInstance().imageEngine.loadGifImage(requireContext(), size.x, size.y, binding.imageView, item.contentUri())
    } else {
      getInstance().imageEngine.loadImage(requireContext(), size.x, size.y, binding.imageView, item.contentUri())
    }
  }

  fun resetView() {
    binding.imageView.resetMatrix()
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context is OnFragmentInteractionListener) {
      mListener = context
    } else {
      throw RuntimeException("$context must implement OnFragmentInteractionListener")
    }
  }

  override fun onDetach() {
    super.onDetach()
    mListener = null
  }

  companion object {
    private const val ARGS_ITEM = "args_item"

    @JvmStatic
    fun newInstance(item: Item?): PreviewItemFragment {
      val fragment = PreviewItemFragment()
      val bundle = Bundle()
      bundle.putParcelable(ARGS_ITEM, item)
      fragment.arguments = bundle
      return fragment
    }
  }
}
