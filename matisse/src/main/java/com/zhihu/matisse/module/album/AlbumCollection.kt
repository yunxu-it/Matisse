package com.zhihu.matisse.module.album

import android.os.Bundle

class AlbumCollection {

  private val STATE_CURRENT_SELECTION = "state_current_selection"

  var currentSelection: Int = 0
    private set

  fun onRestoreInstanceState(savedInstanceState: Bundle?) {
    if (savedInstanceState == null) {
      return
    }

    currentSelection = savedInstanceState.getInt(STATE_CURRENT_SELECTION)
  }

  fun onSaveInstanceState(outState: Bundle) {
    outState.putInt(STATE_CURRENT_SELECTION, currentSelection)
  }

  fun setStateCurrentSelection(currentSelection: Int) {
    this.currentSelection = currentSelection
  }
}