package com.zhihu.matisse.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    init(savedInstanceState)
    initContentView()
    initAfter(savedInstanceState)
    initView()
    initData()
  }

  protected open fun init(savedInstanceState: Bundle?) {}

  protected open fun initAfter(savedInstanceState: Bundle?) {}

  protected open fun initContentView() {
    setContentView(setLayoutResourceID())
  }

  protected abstract fun setLayoutResourceID(): Int

  protected abstract fun initView()

  protected open fun initData() {}

}