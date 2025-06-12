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
package com.zhihu.matisse.module.media

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.zhihu.matisse.R
import com.zhihu.matisse.base.BaseVBFragment
import com.zhihu.matisse.databinding.FragmentMediaSelectionBinding
import com.zhihu.matisse.internal.entity.Album
import com.zhihu.matisse.internal.entity.Item
import com.zhihu.matisse.internal.entity.SelectionSpec.Companion.getInstance
import com.zhihu.matisse.internal.model.SelectedItemCollection
import com.zhihu.matisse.internal.ui.widget.MediaGridInset
import com.zhihu.matisse.internal.utils.UIUtils.spanCount
import com.zhihu.matisse.module.media.AlbumMediaAdapter.CheckStateListener
import com.zhihu.matisse.module.media.AlbumMediaAdapter.OnMediaClickListener
import com.zhihu.matisse.module.media.MediaRepository.Companion.newInstance

class AlbumMediaFragment : BaseVBFragment<FragmentMediaSelectionBinding>(), CheckStateListener, OnMediaClickListener {
  private val mediaViewModel: MediaViewModel by lazy {
    ViewModelProvider(
      this, MediaViewModelFactory(newInstance(requireContext()))
    )[MediaViewModel::class.java]
  }

  private lateinit var mAdapter: AlbumMediaAdapter

  private var mSelectionProvider: SelectionProvider? = null
  private var mCheckStateListener: CheckStateListener? = null
  private var parentMediaClickListener: OnMediaClickListener? = null

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context is SelectionProvider) {
      mSelectionProvider = context
    } else {
      throw IllegalStateException("Context must implement SelectionProvider.")
    }
    if (context is CheckStateListener) {
      mCheckStateListener = context
    }
    if (context is OnMediaClickListener) {
      parentMediaClickListener = context
    }
  }

  override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentMediaSelectionBinding {
    return FragmentMediaSelectionBinding.inflate(inflater, container, false)
  }

  override fun initView() {
    binding.recyclerview.setHasFixedSize(true)
    mAdapter = AlbumMediaAdapter(requireContext(), mSelectionProvider!!.provideSelectedItemCollection(), binding.recyclerview)
    mAdapter.registerCheckStateListener(this)
    mAdapter.registerOnMediaClickListener(this)

    val selectionSpec = getInstance()
    val spanCount: Int = if (selectionSpec.gridExpectedSize > 0) {
      spanCount(requireContext(), selectionSpec.gridExpectedSize)
    } else {
      selectionSpec.spanCount
    }

    binding.recyclerview.layoutManager = GridLayoutManager(requireContext(), spanCount)

    val spacing = resources.getDimensionPixelSize(R.dimen.media_grid_spacing)
    binding.recyclerview.addItemDecoration(MediaGridInset(spanCount, spacing, false))
    binding.recyclerview.adapter = mAdapter
  }

  override fun initData() {
    super.initData()
    val album = requireArguments().getParcelable<Album>(EXTRA_ALBUM)

    if (album != null) {
      mediaViewModel.loadAlbums(album, true)
      mediaViewModel.mediaList.observe(viewLifecycleOwner) { cursor: Cursor? ->
        mAdapter.swapCursor(cursor)
      }
    }
  }

  fun refreshMediaGrid() {
    mAdapter.notifyDataSetChanged()
  }

  fun refreshSelection() {
    mAdapter.refreshSelection()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    mAdapter.swapCursor(null)
  }

  override fun onUpdate() { // notify outer Activity that check state changed
    if (mCheckStateListener != null) {
      mCheckStateListener!!.onUpdate()
    }
  }

  override fun onMediaClick(album: Album?, item: Item, adapterPosition: Int) {
    val albumData = requireArguments().getParcelable<Album>(EXTRA_ALBUM)
    parentMediaClickListener?.onMediaClick(albumData, item, adapterPosition)
  }

  interface SelectionProvider {
    fun provideSelectedItemCollection(): SelectedItemCollection?
  }

  companion object {
    const val EXTRA_ALBUM: String = "extra_album"

    @JvmStatic
    fun newInstance(album: Album): AlbumMediaFragment {
      val fragment = AlbumMediaFragment()
      val args = Bundle()
      args.putParcelable(EXTRA_ALBUM, album)
      fragment.arguments = args
      return fragment
    }
  }
}
