package com.zhihu.matisse.listener

/**
 * when original is enabled , callback immediately when user check or uncheck original.
 */
interface OnCheckedListener {
  fun onCheck(isChecked: Boolean)
}
