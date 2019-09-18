package com.android.internal.widget.helper;

import android.graphics.Canvas;
import android.view.View;
import com.android.internal.widget.RecyclerView;

class ItemTouchUIUtilImpl implements ItemTouchUIUtil {
    ItemTouchUIUtilImpl() {
    }

    public void onDraw(Canvas c, RecyclerView recyclerView, View view, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (isCurrentlyActive && view.getTag(16909025) == null) {
            Object originalElevation = Float.valueOf(view.getElevation());
            view.setElevation(1.0f + findMaxElevation(recyclerView, view));
            view.setTag(16909025, originalElevation);
        }
        view.setTranslationX(dX);
        view.setTranslationY(dY);
    }

    private float findMaxElevation(RecyclerView recyclerView, View itemView) {
        int childCount = recyclerView.getChildCount();
        float max = 0.0f;
        for (int i = 0; i < childCount; i++) {
            View child = recyclerView.getChildAt(i);
            if (child != itemView) {
                float elevation = child.getElevation();
                if (elevation > max) {
                    max = elevation;
                }
            }
        }
        return max;
    }

    public void clearView(View view) {
        Object tag = view.getTag(16909025);
        if (tag != null && (tag instanceof Float)) {
            view.setElevation(((Float) tag).floatValue());
        }
        view.setTag(16909025, null);
        view.setTranslationX(0.0f);
        view.setTranslationY(0.0f);
    }

    public void onSelected(View view) {
    }

    public void onDrawOver(Canvas c, RecyclerView recyclerView, View view, float dX, float dY, int actionState, boolean isCurrentlyActive) {
    }
}
