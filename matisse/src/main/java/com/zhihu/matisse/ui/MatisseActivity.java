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
package com.zhihu.matisse.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import com.zhihu.matisse.R;
import com.zhihu.matisse.base.BaseDBActivity;
import com.zhihu.matisse.databinding.ActivityMatisseBinding;
import com.zhihu.matisse.internal.entity.Album;
import com.zhihu.matisse.internal.entity.Item;
import com.zhihu.matisse.internal.entity.SelectionSpec;
import com.zhihu.matisse.internal.model.AlbumCollection;
import com.zhihu.matisse.internal.model.SelectedItemCollection;
import com.zhihu.matisse.internal.ui.AlbumPreviewActivity;
import com.zhihu.matisse.internal.ui.BasePreviewActivity;
import com.zhihu.matisse.internal.ui.MediaSelectionFragment;
import com.zhihu.matisse.internal.ui.SelectedPreviewActivity;
import com.zhihu.matisse.internal.ui.adapter.AlbumMediaAdapter;
import com.zhihu.matisse.internal.ui.adapter.AlbumsAdapter;
import com.zhihu.matisse.internal.ui.widget.AlbumsSpinner;
import com.zhihu.matisse.internal.ui.widget.IncapableDialog;
import com.zhihu.matisse.internal.utils.MediaStoreCompat;
import com.zhihu.matisse.internal.utils.PathUtils;
import com.zhihu.matisse.internal.utils.PhotoMetadataUtils;
import com.zhihu.matisse.internal.utils.SingleMediaScanner;
import java.util.ArrayList;

/**
 * Main Activity to display albums and media content (images/videos) in each album
 * and also support media selecting operations.
 */
public class MatisseActivity extends BaseDBActivity<ActivityMatisseBinding>
  implements AlbumCollection.AlbumCallbacks, AdapterView.OnItemSelectedListener, MediaSelectionFragment.SelectionProvider, AlbumMediaAdapter.CheckStateListener,
  AlbumMediaAdapter.OnMediaClickListener, AlbumMediaAdapter.OnPhotoCapture {

  public static final String EXTRA_RESULT_SELECTION = "extra_result_selection";
  public static final String EXTRA_RESULT_SELECTION_PATH = "extra_result_selection_path";
  public static final String EXTRA_RESULT_ORIGINAL_ENABLE = "extra_result_original_enable";

  private static final int REQUEST_CODE_PREVIEW = 23;
  private static final int REQUEST_CODE_CAPTURE = 24;
  public static final String CHECK_STATE = "checkState";
  private final AlbumCollection mAlbumCollection = new AlbumCollection();
  private MediaStoreCompat mMediaStoreCompat;
  private final SelectedItemCollection mSelectedCollection = new SelectedItemCollection(this);
  private SelectionSpec mSpec;

  private AlbumsSpinner mAlbumsSpinner;
  private AlbumsAdapter mAlbumsAdapter;

  private boolean mOriginalEnable;

  @Override protected void init(@Nullable Bundle savedInstanceState) {
    super.init(savedInstanceState);
    mSpec = SelectionSpec.getInstance();
    setTheme(mSpec.themeId);
  }

  @Override protected void initAfter(@Nullable Bundle savedInstanceState) {
    super.init(savedInstanceState);
    if (!mSpec.hasInited) {
      setResult(RESULT_CANCELED);
      finish();
      return;
    }
    if (mSpec.needOrientationRestriction()) {
      setRequestedOrientation(mSpec.orientation);
    }

    if (mSpec.capture) {
      mMediaStoreCompat = new MediaStoreCompat(this);
      if (mSpec.captureStrategy == null) {
        throw new RuntimeException("Don't forget to set CaptureStrategy.");
      }
      mMediaStoreCompat.setCaptureStrategy(mSpec.captureStrategy);
    }

    mSelectedCollection.onCreate(savedInstanceState);

    if (savedInstanceState != null) {
      mOriginalEnable = savedInstanceState.getBoolean(CHECK_STATE);
    }
    updateBottomToolbar();

    mAlbumCollection.onCreate(this, this);
    mAlbumCollection.onRestoreInstanceState(savedInstanceState);
    mAlbumCollection.loadAlbums();
  }

  @Override protected void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    mSelectedCollection.onSaveInstanceState(outState);
    mAlbumCollection.onSaveInstanceState(outState);
    outState.putBoolean("checkState", mOriginalEnable);
  }

  @Override protected void initView() {
    try {
      binding.toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_24);
      int color;
      try (TypedArray ta = getTheme().obtainStyledAttributes(new int[] { R.attr.album_element_color })) {
        color = ta.getColor(0, 0);
      }
      binding.toolbar.getNavigationIcon().setColorFilter(color, PorterDuff.Mode.SRC_IN);
      binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    } catch (Exception e) {
      e.printStackTrace();
    }

    mAlbumsAdapter = new AlbumsAdapter(this, null, false);

    mAlbumsSpinner = new AlbumsSpinner(this);
    mAlbumsSpinner.setOnItemSelectedListener(this);
    mAlbumsSpinner.setSelectedTextView(binding.selectedAlbum);
    mAlbumsSpinner.setPopupAnchorView(binding.toolbar);
    mAlbumsSpinner.setAdapter(mAlbumsAdapter);

    binding.buttonPreview.setOnClickListener(v -> preview());
    binding.buttonApply.setOnClickListener(v -> apply());

    binding.originalLayout.setOnClickListener(v -> originData());
  }

  private void preview() {
    Intent intent = new Intent(this, SelectedPreviewActivity.class);
    intent.putExtra(BasePreviewActivity.EXTRA_DEFAULT_BUNDLE, mSelectedCollection.getDataWithBundle());
    intent.putExtra(BasePreviewActivity.EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable);
    startActivityForResult(intent, REQUEST_CODE_PREVIEW);
  }

  private void apply() {
    Intent result = new Intent();
    ArrayList<Uri> selectedUris = (ArrayList<Uri>) mSelectedCollection.asListOfUri();
    result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION, selectedUris);
    ArrayList<String> selectedPaths = (ArrayList<String>) mSelectedCollection.asListOfString();
    result.putStringArrayListExtra(EXTRA_RESULT_SELECTION_PATH, selectedPaths);
    result.putExtra(EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable);
    setResult(RESULT_OK, result);
    finish();
  }

  private void originData() {
    int count = countOverMaxSize();
    if (count > 0) {
      IncapableDialog incapableDialog = IncapableDialog.newInstance("", getString(R.string.error_over_original_count, count, mSpec.originalMaxSize));
      incapableDialog.show(getSupportFragmentManager(), IncapableDialog.class.getName());
      return;
    }

    mOriginalEnable = !mOriginalEnable;
    binding.original.setChecked(mOriginalEnable);

    if (mSpec.onCheckedListener != null) {
      mSpec.onCheckedListener.onCheck(mOriginalEnable);
    }
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    mAlbumCollection.onDestroy();
    mSpec.onCheckedListener = null;
    mSpec.onSelectedListener = null;
  }

  @Override public void onBackPressed() {
    setResult(Activity.RESULT_CANCELED);
    super.onBackPressed();
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode != RESULT_OK) return;

    if (requestCode == REQUEST_CODE_PREVIEW) {
      Bundle resultBundle = data.getBundleExtra(BasePreviewActivity.EXTRA_RESULT_BUNDLE);
      ArrayList<Item> selected = resultBundle.getParcelableArrayList(SelectedItemCollection.STATE_SELECTION);
      mOriginalEnable = data.getBooleanExtra(BasePreviewActivity.EXTRA_RESULT_ORIGINAL_ENABLE, false);
      int collectionType = resultBundle.getInt(SelectedItemCollection.STATE_COLLECTION_TYPE, SelectedItemCollection.COLLECTION_UNDEFINED);
      if (data.getBooleanExtra(BasePreviewActivity.EXTRA_RESULT_APPLY, false)) {
        Intent result = new Intent();
        ArrayList<Uri> selectedUris = new ArrayList<>();
        ArrayList<String> selectedPaths = new ArrayList<>();
        if (selected != null) {
          for (Item item : selected) {
            selectedUris.add(item.getContentUri());
            selectedPaths.add(PathUtils.getPath(this, item.getContentUri()));
          }
        }
        result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION, selectedUris);
        result.putStringArrayListExtra(EXTRA_RESULT_SELECTION_PATH, selectedPaths);
        result.putExtra(EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable);
        setResult(RESULT_OK, result);
        finish();
      } else {
        mSelectedCollection.overwrite(selected, collectionType);
        Fragment mediaSelectionFragment = getSupportFragmentManager().findFragmentByTag(MediaSelectionFragment.class.getSimpleName());
        if (mediaSelectionFragment instanceof MediaSelectionFragment) {
          ((MediaSelectionFragment) mediaSelectionFragment).refreshMediaGrid();
        }
        updateBottomToolbar();
      }
    } else if (requestCode == REQUEST_CODE_CAPTURE) {
      // Just pass the data back to previous calling Activity.
      Uri contentUri = mMediaStoreCompat.getCurrentPhotoUri();
      String path = mMediaStoreCompat.getCurrentPhotoPath();
      ArrayList<Uri> selected = new ArrayList<>();
      selected.add(contentUri);
      ArrayList<String> selectedPath = new ArrayList<>();
      selectedPath.add(path);
      Intent result = new Intent();
      result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION, selected);
      result.putStringArrayListExtra(EXTRA_RESULT_SELECTION_PATH, selectedPath);
      setResult(RESULT_OK, result);
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        MatisseActivity.this.revokeUriPermission(contentUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
      }

      new SingleMediaScanner(this.getApplicationContext(), path, new SingleMediaScanner.ScanListener() {
        @Override public void onScanFinish() {
          Log.i("SingleMediaScanner", "scan finish!");
        }
      });
      finish();
    }
  }

  private void updateBottomToolbar() {
    int selectedCount = mSelectedCollection.count();
    if (selectedCount == 0) {
      binding.buttonPreview.setEnabled(false);
      binding.buttonApply.setEnabled(false);
      binding.buttonApply.setText(getString(R.string.button_apply_default));
    } else if (selectedCount == 1 && mSpec.singleSelectionModeEnabled()) {
      binding.buttonPreview.setEnabled(true);
      binding.buttonApply.setText(R.string.button_apply_default);
      binding.buttonApply.setEnabled(true);
    } else {
      binding.buttonPreview.setEnabled(true);
      binding.buttonApply.setEnabled(true);
      binding.buttonApply.setText(getString(R.string.button_apply, selectedCount));
    }

    if (mSpec.originalable) {
      binding.originalLayout.setVisibility(View.VISIBLE);
      updateOriginalState();
    } else {
      binding.originalLayout.setVisibility(View.INVISIBLE);
    }
  }

  private void updateOriginalState() {
    binding.original.setChecked(mOriginalEnable);
    if (countOverMaxSize() > 0) {

      if (mOriginalEnable) {
        IncapableDialog incapableDialog = IncapableDialog.newInstance("", getString(R.string.error_over_original_size, mSpec.originalMaxSize));
        incapableDialog.show(getSupportFragmentManager(), IncapableDialog.class.getName());

        binding.original.setChecked(false);
        mOriginalEnable = false;
      }
    }
  }

  private int countOverMaxSize() {
    int count = 0;
    int selectedCount = mSelectedCollection.count();
    for (int i = 0; i < selectedCount; i++) {
      Item item = mSelectedCollection.asList().get(i);

      if (item.isImage()) {
        float size = PhotoMetadataUtils.getSizeInMB(item.size);
        if (size > mSpec.originalMaxSize) {
          count++;
        }
      }
    }
    return count;
  }

  @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    mAlbumCollection.setStateCurrentSelection(position);
    mAlbumsAdapter.getCursor().moveToPosition(position);
    Album album = Album.valueOf(mAlbumsAdapter.getCursor());
    if (album.isAll() && SelectionSpec.getInstance().capture) {
      album.addCaptureCount();
    }
    onAlbumSelected(album);
  }

  @Override public void onNothingSelected(AdapterView<?> parent) {

  }

  @Override public void onAlbumLoad(final Cursor cursor) {
    mAlbumsAdapter.swapCursor(cursor);
    // select default album.
    Handler handler = new Handler(Looper.getMainLooper());
    handler.post(() -> {
      cursor.moveToPosition(mAlbumCollection.getCurrentSelection());
      mAlbumsSpinner.setSelection(MatisseActivity.this, mAlbumCollection.getCurrentSelection());
      Album album = Album.valueOf(cursor);
      if (album.isAll() && SelectionSpec.getInstance().capture) {
        album.addCaptureCount();
      }
      onAlbumSelected(album);
    });
  }

  @Override public void onAlbumReset() {
    mAlbumsAdapter.swapCursor(null);
  }

  private void onAlbumSelected(Album album) {
    if (album.isAll() && album.isEmpty()) {
      binding.container.setVisibility(View.GONE);
      binding.emptyView.setVisibility(View.VISIBLE);
    } else {
      binding.container.setVisibility(View.VISIBLE);
      binding.emptyView.setVisibility(View.GONE);
      Fragment fragment = MediaSelectionFragment.newInstance(album);
      getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment, MediaSelectionFragment.class.getSimpleName()).commitAllowingStateLoss();
    }
  }

  @Override public void onUpdate() {
    // notify bottom toolbar that check state changed.
    updateBottomToolbar();

    if (mSpec.onSelectedListener != null) {
      mSpec.onSelectedListener.onSelected(mSelectedCollection.asListOfUri(), mSelectedCollection.asListOfString());
    }
  }

  @Override public void onMediaClick(Album album, Item item, int adapterPosition) {
    Intent intent = new Intent(this, AlbumPreviewActivity.class);
    intent.putExtra(AlbumPreviewActivity.EXTRA_ALBUM, album);
    intent.putExtra(AlbumPreviewActivity.EXTRA_ITEM, item);
    intent.putExtra(BasePreviewActivity.EXTRA_DEFAULT_BUNDLE, mSelectedCollection.getDataWithBundle());
    intent.putExtra(BasePreviewActivity.EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable);
    startActivityForResult(intent, REQUEST_CODE_PREVIEW);
  }

  @Override public SelectedItemCollection provideSelectedItemCollection() {
    return mSelectedCollection;
  }

  @Override public void capture() {
    if (mMediaStoreCompat != null) {
      mMediaStoreCompat.dispatchCaptureIntent(this, REQUEST_CODE_CAPTURE);
    }
  }

  @Override protected int setLayoutResourceID() {
    return R.layout.activity_matisse;
  }
}
