package com.zhihu.matisse.base

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel

abstract class AbsViewModel : ViewModel(), IViewModel, LifecycleObserver {

}