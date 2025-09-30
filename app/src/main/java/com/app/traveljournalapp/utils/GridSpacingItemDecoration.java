package com.app.traveljournalapp.utils;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
    private final int spanCount, spacing, edge;
    public GridSpacingItemDecoration(int spanCount, int spacingDp, boolean includeEdge, Context ctx) {
        this.spanCount = spanCount;
        this.spacing = (int) (spacingDp * ctx.getResources().getDisplayMetrics().density);
        this.edge = includeEdge ? 1 : 0;
    }
    @Override public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int pos = parent.getChildAdapterPosition(view);
        int col = pos % spanCount;
        if (edge==1){
            outRect.left  = spacing - col * spacing / spanCount;
            outRect.right = (col + 1) * spacing / spanCount;
            if (pos < spanCount) outRect.top = spacing;
            outRect.bottom = spacing;
        } else {
            outRect.left  = col * spacing / spanCount;
            outRect.right = spacing - (col + 1) * spacing / spanCount;
            if (pos >= spanCount) outRect.top = spacing;
        }
    }
}

