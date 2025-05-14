package com.zhihu.matisse.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

abstract class BaseDBFragment<DB : ViewDataBinding> : Fragment() {

  protected lateinit var binding: DB
  private var viewModels: HashMap<String, AbsViewModel> = hashMapOf()

  abstract fun initLayoutId(): Int

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View {
    binding = DataBindingUtil.inflate(inflater, initLayoutId(), container, false)
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
  }

}
