package com.zhihu.matisse.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

abstract class BaseActivity : AppCompatActivity() {

  private var viewModels: HashMap<String, AbsViewModel> = hashMapOf()

  fun <T : AbsViewModel> getViewModel(clazz: Class<T>): T {
    val key = clazz.name
    val model: AbsViewModel = viewModels[key] ?: ViewModelProvider(this)[clazz]
    lifecycle.addObserver(model)
    viewModels[key] = model
    return model as T
  }

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

  override fun onDestroy() {
    super.onDestroy()
    for (viewModel in viewModels) {
      lifecycle.removeObserver(viewModel.value)
    }
    viewModels.clear()
  }

}