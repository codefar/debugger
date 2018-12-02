package com.su.debugger.widget.recycler;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class DividerDecoration extends RecyclerView.ItemDecoration {

    private RecyclerView mRecyclerView;
    private Drawable mDivider;
    private int mDividerHeight;

    public DividerDecoration(RecyclerView recyclerView) {
        this.mRecyclerView = recyclerView;
    }

    @Override
    public void onDrawOver(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if (mDivider == null) {
            return;
        }
        final int childCount = parent.getChildCount();
        final int width = parent.getWidth();
        for (int childViewIndex = 0; childViewIndex < childCount; childViewIndex++) {
            final View view = parent.getChildAt(childViewIndex);
            int top = (int) ViewCompat.getY(view) + view.getHeight();
            mDivider.setBounds(0, top, width, top + mDividerHeight);
            mDivider.draw(canvas);
        }
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        outRect.bottom = mDividerHeight;
    }

    public void setDivider(Drawable divider) {
        if (divider != null) {
            mDividerHeight = divider.getIntrinsicHeight();
        } else {
            mDividerHeight = 0;
        }
        mDivider = divider;
        mRecyclerView.invalidateItemDecorations();
    }

    public void setDividerHeight(int dividerHeight) {
        mDividerHeight = dividerHeight;
        mRecyclerView.invalidateItemDecorations();
    }
}
