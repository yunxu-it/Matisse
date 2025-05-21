
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