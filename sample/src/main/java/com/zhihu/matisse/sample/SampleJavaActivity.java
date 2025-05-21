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
package com.zhihu.matisse.sample;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.engine.impl.PicassoEngine;
import com.zhihu.matisse.filter.Filter;
import com.zhihu.matisse.internal.entity.CaptureStrategy;
import com.zhihu.matisse.listener.OnCheckedListener;
import com.zhihu.matisse.listener.OnSelectedListener;

import java.util.List;

public class SampleJavaActivity extends AppCompatActivity {
  private int type;
  private UriAdapter mAdapter;

  private final ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
    if (result.getResultCode() == Activity.RESULT_OK) {
      Intent data = result.getData();
      mAdapter.setData(Matisse.obtainResult(data), Matisse.obtainPathResult(data));
      Log.e("OnActivityResult", String.valueOf(Matisse.obtainOriginalState(data)));
    }
  });

  private final ActivityResultLauncher<String[]> requestPermissionLauncher =
    registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
      StorageAccess storageAccess = DynamicPermission.INSTANCE.getStorageAccess(this);
      if (storageAccess != StorageAccess.Denied) {
        startAction();
      }
    });

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    TextView viewById = findViewById(R.id.zhihu);
    viewById.setText("JAVA");
    viewById.setOnClickListener(v -> onClick(0));
    findViewById(R.id.dracula).setOnClickListener(v -> onClick(1));
    findViewById(R.id.only_gif).setOnClickListener(v -> onClick(2));

    RecyclerView recyclerView = findViewById(R.id.recyclerview);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    mAdapter = new UriAdapter();
    recyclerView.setAdapter(mAdapter);
  }

  private void onClick(int type) {
    this.type = type;
    requestPermissionLauncher.launch(DynamicPermission.INSTANCE.getMediaPermissions(0));
  }

  private void startAction() {
    switch (type) {
      case 0:
        Matisse.from(this)
          .choose(MimeType.of(MimeType.PNG, MimeType.JPEG), false)
          .countable(true)
          .capture(true)
          .theme(R.style.AppTheme)
          .captureStrategy(new CaptureStrategy(true, "com.zhihu.matisse.sample.fileprovider", "test"))
          .maxSelectable(9)
          .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
          .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
          .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
          .thumbnailScale(0.85f)
          .imageEngine(new GlideEngine())
          .showSingleMediaType(true)
          .maxOriginalSize(10)
          .autoHideToolbarOnSingleTap(true)
          .setOnCheckedListener(new OnCheckedListener() {
            @Override public void onCheck(boolean isChecked) {
              Log.e("isChecked", "onCheck: isChecked=" + isChecked);
            }
          })
          .forResult(100);
        break;
      case 1:
        Matisse.from(this)
          .choose(MimeType.ofImage())
          .countable(false)
          .capture(true)
          .captureStrategy(new CaptureStrategy(true, "com.zhihu.matisse.sample.fileprovider", "test"))
          .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
          .maxSelectable(9)
          .originalEnable(true)
          .maxOriginalSize(10)
          .imageEngine(new PicassoEngine())
          .forResult(resultLauncher);
        break;
      case 2:
        Matisse.from(this)
          .choose(MimeType.of(MimeType.GIF), false)
          .countable(true)
          .maxSelectable(9)
          .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
          .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
          .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
          .thumbnailScale(0.85f)
          .imageEngine(new GlideEngine())
          .showSingleMediaType(true)
          .originalEnable(true)
          .maxOriginalSize(10)
          .autoHideToolbarOnSingleTap(true)
          .forResult(resultLauncher);
        break;
    }
    mAdapter.setData(null, null);
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    Log.i("SampleActivity", "onActivityReenter-190: " + requestCode);
  }

  static class UriAdapter extends RecyclerView.Adapter<UriAdapter.UriViewHolder> {

    private List<Uri> mUris;
    private List<String> mPaths;

    void setData(List<Uri> uris, List<String> paths) {
      mUris = uris;
      mPaths = paths;
      notifyDataSetChanged();
    }

    @NonNull @Override public UriViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.uri_item, parent, false);
      return new UriViewHolder(view);
    }

    @Override public void onBindViewHolder(@NonNull UriViewHolder holder, int position) {
      holder.mUri.setText(mUris.get(position).toString());
      holder.mPath.setText(mPaths.get(position));

      float alpha = position % 2 == 0 ? 1.0f : 0.54f;
      holder.mUri.setAlpha(alpha);
      holder.mPath.setAlpha(alpha);
    }

    @Override public int getItemCount() {
      return mUris != null ? mUris.size() : 0;
    }

    static class UriViewHolder extends RecyclerView.ViewHolder {
      TextView mUri;
      TextView mPath;

      UriViewHolder(View contentView) {
        super(contentView);
        mUri = contentView.findViewById(R.id.uri);
        mPath = contentView.findViewById(R.id.path);
      }
    }
  }
}
