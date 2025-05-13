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
package com.zhihu.matisse.module.preview

import android.os.Bundle
import com.zhihu.matisse.internal.entity.Item
import com.zhihu.matisse.internal.entity.SelectionSpec.Companion.getInstance
import com.zhihu.matisse.internal.model.SelectedItemCollection

class PreviewSelectedActivity : BasePreviewActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (!getInstance().hasInited) {
      setResult(RESULT_CANCELED)
      finish()
      return
    }

    val defaultBundle = intent.getBundleExtra(EXTRA_DEFAULT_BUNDLE)
    val selected: List<Item>? = defaultBundle?.getParcelableArrayList(SelectedItemCollection.EXTRA_STATE_SELECTION)
    if (selected.isNullOrEmpty()) {
      return
    }

    mAdapter.addAll(selected)
    mAdapter.notifyDataSetChanged()

    if (mSpec.countable) {
      mCheckView.setCheckedNum(1)
    } else {
      mCheckView.setChecked(true)
    }

    mPreviousPos = 0
    updateSize(selected[0])
  }
}
