package com.zhihu.matisse.base

import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

abstract class BaseDBActivity<DB : ViewDataBinding> : BaseActivity() {

  protected lateinit var binding: DB

  override fun initContentView() {
    binding = DataBindingUtil.setContentView(this, setLayoutResourceID())
  }
}
