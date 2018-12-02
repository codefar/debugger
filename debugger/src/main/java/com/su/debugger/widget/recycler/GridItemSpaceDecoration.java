package com.su.debugger.widget.recycler;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by su on 16-8-2.
 */
public class GridItemSpaceDecoration extends RecyclerView.ItemDecoration {
    private int mSpacing;
    private int mSpanCount;

    public GridItemSpaceDecoration(int spanCount, int itemOffset) {
        mSpacing = itemOffset / 2;
        mSpanCount = spanCount;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        outRect.set(mSpacing, mSpacing, mSpacing, mSpacing);
        int position = parent.getChildLayoutPosition(view);
        int count = parent.getAdapter().getItemCount();
        if (isLastLine(position, count)) {
            outRect.bottom = 0;
        }
    }

    private boolean isFirstColumn(int position) {
        return position % mSpanCount == 0;
    }

    private boolean isLastColumn(int position) {
        return (position + 1) % mSpanCount == 0;
    }

    private boolean isLast(int position, int count) {
        return (position + 1) == count;
    }

    private boolean isFirstLine(int position) {
        return position < mSpanCount;
    }

    private boolean isLastLine(int position, int count) {
        if (count % mSpanCount != 0) {
            return position >= (count / mSpanCount * mSpanCount);
        } else {
            return position >= ((count / mSpanCount - 1) * mSpanCount);
        }
    }
}
