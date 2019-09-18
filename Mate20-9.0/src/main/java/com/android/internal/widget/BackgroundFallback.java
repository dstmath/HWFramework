package com.android.internal.widget;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;

public class BackgroundFallback {
    private Drawable mBackgroundFallback;

    public void setDrawable(Drawable d) {
        this.mBackgroundFallback = d;
    }

    public boolean hasFallback() {
        return this.mBackgroundFallback != null;
    }

    public void draw(ViewGroup boundsView, ViewGroup root, Canvas c, View content, View coveringView1, View coveringView2) {
        int i;
        Canvas canvas = c;
        View view = coveringView1;
        View view2 = coveringView2;
        if (hasFallback()) {
            int width = boundsView.getWidth();
            int height = boundsView.getHeight();
            int rootOffsetX = root.getLeft();
            int rootOffsetY = root.getTop();
            int right = 0;
            int bottom = 0;
            int childCount = root.getChildCount();
            int top = height;
            int left = width;
            int i2 = 0;
            while (i2 < childCount) {
                View child = root.getChildAt(i2);
                int childCount2 = childCount;
                Drawable childBg = child.getBackground();
                if (child != content) {
                    if (child.getVisibility() == 0) {
                        if (!isOpaque(childBg)) {
                        }
                    }
                    i2++;
                    childCount = childCount2;
                } else if (childBg == null && (child instanceof ViewGroup) && ((ViewGroup) child).getChildCount() == 0) {
                    i2++;
                    childCount = childCount2;
                }
                left = Math.min(left, child.getLeft() + rootOffsetX);
                top = Math.min(top, child.getTop() + rootOffsetY);
                right = Math.max(right, child.getRight() + rootOffsetX);
                bottom = Math.max(bottom, child.getBottom() + rootOffsetY);
                i2++;
                childCount = childCount2;
            }
            int bottom2 = bottom;
            int left2 = left;
            boolean eachBarCoversTopInY = true;
            int i3 = 0;
            while (i3 < 2) {
                View v = i3 == 0 ? view : view2;
                if (v == null || v.getVisibility() != 0 || v.getAlpha() != 1.0f || !isOpaque(v.getBackground())) {
                    eachBarCoversTopInY = false;
                } else {
                    if (v.getTop() <= 0 && v.getBottom() >= height && v.getLeft() <= 0 && v.getRight() >= left2) {
                        left2 = 0;
                    }
                    if (v.getTop() <= 0 && v.getBottom() >= height && v.getLeft() <= right && v.getRight() >= width) {
                        right = width;
                    }
                    if (v.getTop() <= 0 && v.getBottom() >= top && v.getLeft() <= 0 && v.getRight() >= width) {
                        top = 0;
                    }
                    if (v.getTop() <= bottom2 && v.getBottom() >= height && v.getLeft() <= 0 && v.getRight() >= width) {
                        bottom2 = height;
                    }
                    eachBarCoversTopInY &= v.getTop() <= 0 && v.getBottom() >= top;
                }
                i3++;
            }
            if (eachBarCoversTopInY && (viewsCoverEntireWidth(view, view2, width) || viewsCoverEntireWidth(view2, view, width))) {
                top = 0;
            }
            if (left2 < right && top < bottom2) {
                if (top > 0) {
                    i = 0;
                    this.mBackgroundFallback.setBounds(0, 0, width, top);
                    this.mBackgroundFallback.draw(canvas);
                } else {
                    i = 0;
                }
                if (left2 > 0) {
                    this.mBackgroundFallback.setBounds(i, top, left2, height);
                    this.mBackgroundFallback.draw(canvas);
                }
                if (right < width) {
                    this.mBackgroundFallback.setBounds(right, top, width, height);
                    this.mBackgroundFallback.draw(canvas);
                }
                if (bottom2 < height) {
                    this.mBackgroundFallback.setBounds(left2, bottom2, right, height);
                    this.mBackgroundFallback.draw(canvas);
                }
            }
        }
    }

    private boolean isOpaque(Drawable childBg) {
        return childBg != null && childBg.getOpacity() == -1;
    }

    private boolean viewsCoverEntireWidth(View view1, View view2, int width) {
        return view1.getLeft() <= 0 && view1.getRight() >= view2.getLeft() && view2.getRight() >= width;
    }
}
