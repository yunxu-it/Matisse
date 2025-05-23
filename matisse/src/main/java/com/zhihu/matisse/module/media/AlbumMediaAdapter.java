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
package com.zhihu.matisse.module.media;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.zhihu.matisse.R;
import com.zhihu.matisse.internal.entity.Album;
import com.zhihu.matisse.internal.entity.IncapableCause;
import com.zhihu.matisse.internal.entity.Item;
import com.zhihu.matisse.internal.entity.SelectionSpec;
import com.zhihu.matisse.internal.model.SelectedItemCollection;
import com.zhihu.matisse.internal.ui.adapter.RecyclerViewCursorAdapter;
import com.zhihu.matisse.internal.ui.widget.CheckView;
import com.zhihu.matisse.internal.ui.widget.MediaGrid;

public class AlbumMediaAdapter extends RecyclerViewCursorAdapter<RecyclerView.ViewHolder> implements MediaGrid.OnMediaGridClickListener {

  private static final int VIEW_TYPE_CAPTURE_PHOTO = 0x01;
  private static final int VIEW_TYPE_CAPTURE_VIDEO = 0x03;
  private static final int VIEW_TYPE_MEDIA = 0x02;
  private final SelectedItemCollection mSelectedCollection;
  private final Drawable mPlaceholder;
  private SelectionSpec mSelectionSpec;
  private CheckStateListener mCheckStateListener;
  private OnMediaClickListener mOnMediaClickListener;
  private RecyclerView mRecyclerView;
  private int mImageResize;

  public AlbumMediaAdapter(Context context, SelectedItemCollection selectedCollection, RecyclerView recyclerView) {
    super(null);
    mSelectionSpec = SelectionSpec.getInstance();
    mSelectedCollection = selectedCollection;

    TypedArray ta = context.getTheme().obtainStyledAttributes(new int[] { R.attr.item_placeholder });
    mPlaceholder = ta.getDrawable(0);
    ta.recycle();

    mRecyclerView = recyclerView;
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    if (viewType == VIEW_TYPE_CAPTURE_PHOTO) {
      View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_capture_item, parent, false);
      CaptureViewHolder holder = new CaptureViewHolder(v);
      holder.itemView.setOnClickListener(v1 -> {
        if (v1.getContext() instanceof OnPhotoCapture) {
          ((OnPhotoCapture) v1.getContext()).capturePhoto();
        }
      });
      return holder;
    } else if (viewType == VIEW_TYPE_CAPTURE_VIDEO) {
      View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_capture_item, parent, false);
      CaptureViewHolder holder = new CaptureViewHolder(v);
      holder.itemView.setOnClickListener(v1 -> {
        if (v1.getContext() instanceof OnPhotoCapture) {
          ((OnPhotoCapture) v1.getContext()).captureVideo();
        }
      });
      return holder;
    } else if (viewType == VIEW_TYPE_MEDIA) {
      View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_grid_item, parent, false);
      return new MediaViewHolder(v);
    }
    return null;
  }

  @Override protected void onBindViewHolder(final RecyclerView.ViewHolder holder, Cursor cursor) {
    if (holder instanceof CaptureViewHolder captureViewHolder) {
      Drawable[] drawables = captureViewHolder.mHint.getCompoundDrawables();
      TypedArray ta = holder.itemView.getContext().getTheme().obtainStyledAttributes(new int[] { R.attr.capture_textColor });
      int color = ta.getColor(0, 0);
      ta.recycle();

      for (int i = 0; i < drawables.length; i++) {
        Drawable drawable = drawables[i];
        if (drawable != null) {
          final Drawable.ConstantState state = drawable.getConstantState();
          if (state == null) {
            continue;
          }

          Drawable newDrawable = state.newDrawable().mutate();
          newDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
          newDrawable.setBounds(drawable.getBounds());
          drawables[i] = newDrawable;
        }
      }
      captureViewHolder.mHint.setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
    } else if (holder instanceof MediaViewHolder mediaViewHolder) {
      final Item item = Item.valueOf(cursor);
      Log.i("AlbumMediaAdapter", "onBindViewHolder-114: " + item.toString());
      mediaViewHolder.mMediaGrid.preBindMedia(
        new MediaGrid.PreBindInfo(getImageResize(mediaViewHolder.mMediaGrid.getContext()), mPlaceholder, mSelectionSpec.countable, holder));
      mediaViewHolder.mMediaGrid.bindMedia(item);
      mediaViewHolder.mMediaGrid.setOnMediaGridClickListener(this);
      setCheckStatus(item, mediaViewHolder.mMediaGrid);
    }
  }

  private void setCheckStatus(Item item, MediaGrid mediaGrid) {
    if (mSelectionSpec.countable) {
      int checkedNum = mSelectedCollection.checkedNumOf(item);
      if (checkedNum > 0) {
        mediaGrid.setCheckEnabled(true);
        mediaGrid.setCheckedNum(checkedNum);
      } else {
        if (mSelectedCollection.maxSelectableReached()) {
          mediaGrid.setCheckEnabled(false);
          mediaGrid.setCheckedNum(CheckView.UNCHECKED);
        } else {
          mediaGrid.setCheckEnabled(true);
          mediaGrid.setCheckedNum(checkedNum);
        }
      }
    } else {
      boolean selected = mSelectedCollection.isSelected(item);
      if (selected) {
        mediaGrid.setCheckEnabled(true);
        mediaGrid.setChecked(true);
      } else {
        if (mSelectedCollection.maxSelectableReached()) {
          mediaGrid.setCheckEnabled(false);
          mediaGrid.setChecked(false);
        } else {
          mediaGrid.setCheckEnabled(true);
          mediaGrid.setChecked(false);
        }
      }
    }
  }

  @Override public void onThumbnailClicked(ImageView thumbnail, Item item, RecyclerView.ViewHolder holder) {
    if (mSelectionSpec.showPreview) {
      if (mOnMediaClickListener != null) {
        mOnMediaClickListener.onMediaClick(null, item, holder.getAdapterPosition());
      }
    } else {
      updateSelectedItem(item, holder);
    }
  }

  @Override public void onCheckViewClicked(CheckView checkView, Item item, RecyclerView.ViewHolder holder) {
    updateSelectedItem(item, holder);
  }

  private void updateSelectedItem(Item item, RecyclerView.ViewHolder holder) {
    if (mSelectionSpec.countable) {
      int checkedNum = mSelectedCollection.checkedNumOf(item);
      if (checkedNum == CheckView.UNCHECKED) {
        if (assertAddSelection(holder.itemView.getContext(), item)) {
          mSelectedCollection.add(item);
          notifyCheckStateChanged();
        }
      } else {
        mSelectedCollection.remove(item);
        notifyCheckStateChanged();
      }
    } else {
      if (mSelectedCollection.isSelected(item)) {
        mSelectedCollection.remove(item);
        notifyCheckStateChanged();
      } else {
        if (assertAddSelection(holder.itemView.getContext(), item)) {
          mSelectedCollection.add(item);
          notifyCheckStateChanged();
        }
      }
    }
  }

  private void notifyCheckStateChanged() {
    notifyDataSetChanged();
    if (mCheckStateListener != null) {
      mCheckStateListener.onUpdate();
    }
  }

  @Override public int getItemViewType(int position, Cursor cursor) {
    if (Item.valueOf(cursor).isCapturePhoto()) {
      return VIEW_TYPE_CAPTURE_PHOTO;
    } else if (Item.valueOf(cursor).isCaptureVideo()) {
      return VIEW_TYPE_CAPTURE_VIDEO;
    } else {
      return VIEW_TYPE_MEDIA;
    }
  }

  private boolean assertAddSelection(Context context, Item item) {
    IncapableCause cause = mSelectedCollection.isAcceptable(item);
    IncapableCause.handleCause(context, cause);
    return cause == null;
  }

  public void registerCheckStateListener(CheckStateListener listener) {
    mCheckStateListener = listener;
  }

  public void unregisterCheckStateListener() {
    mCheckStateListener = null;
  }

  public void registerOnMediaClickListener(OnMediaClickListener listener) {
    mOnMediaClickListener = listener;
  }

  public void unregisterOnMediaClickListener() {
    mOnMediaClickListener = null;
  }

  public void refreshSelection() {
    GridLayoutManager layoutManager = (GridLayoutManager) mRecyclerView.getLayoutManager();
    int first = layoutManager.findFirstVisibleItemPosition();
    int last = layoutManager.findLastVisibleItemPosition();
    if (first == -1 || last == -1) {
      return;
    }
    Cursor cursor = getCursor();
    for (int i = first; i <= last; i++) {
      RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(first);
      if (holder instanceof MediaViewHolder) {
        if (cursor.moveToPosition(i)) {
          setCheckStatus(Item.valueOf(cursor), ((MediaViewHolder) holder).mMediaGrid);
        }
      }
    }
  }

  private int getImageResize(Context context) {
    if (mImageResize == 0) {
      RecyclerView.LayoutManager lm = mRecyclerView.getLayoutManager();
      int spanCount = ((GridLayoutManager) lm).getSpanCount();
      int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
      int availableWidth = screenWidth - context.getResources().getDimensionPixelSize(R.dimen.media_grid_spacing) * (spanCount - 1);
      mImageResize = availableWidth / spanCount;
      mImageResize = (int) (mImageResize * mSelectionSpec.thumbnailScale);
    }
    return mImageResize;
  }

  public interface CheckStateListener {
    void onUpdate();
  }

  public interface OnMediaClickListener {
    void onMediaClick(Album album, Item item, int adapterPosition);
  }

  public interface OnPhotoCapture {
    void capturePhoto();

    void captureVideo();
  }

  private static class MediaViewHolder extends RecyclerView.ViewHolder {

    private final MediaGrid mMediaGrid;

    MediaViewHolder(View itemView) {
      super(itemView);
      mMediaGrid = (MediaGrid) itemView;
    }
  }

  private static class CaptureViewHolder extends RecyclerView.ViewHolder {

    private final TextView mHint;

    CaptureViewHolder(View itemView) {
      super(itemView);

      mHint = (TextView) itemView.findViewById(R.id.hint);
    }
  }
}
