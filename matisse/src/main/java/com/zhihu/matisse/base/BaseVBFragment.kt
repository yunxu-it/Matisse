/*
 * Copyright 2025 lixiaolong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zhihu.matisse.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseVBFragment<VB : ViewBinding> : Fragment() {
  private var _binding: VB? = null
  protected val binding get() = _binding!!

  protected abstract fun inflateBinding(
    inflater: LayoutInflater,
    container: ViewGroup?
  ): VB

  private var viewModels: HashMap<String, AbsViewModel> = hashMapOf()

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View {
    _binding = inflateBinding(inflater, container)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initView()
    initData()
  }

  protected abstract fun initView()

  protected open fun initData() {}

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

}
