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

    public Drawable getDrawable() {
        return this.mBackgroundFallback;
    }

    public boolean hasFallback() {
        return this.mBackgroundFallback != null;
    }

    public void draw(ViewGroup boundsView, ViewGroup root, Canvas c, View content, View coveringView1, View coveringView2) {
        int i;
        if (hasFallback()) {
            int width = boundsView.getWidth();
            int height = boundsView.getHeight();
            int rootOffsetX = root.getLeft();
            int rootOffsetY = root.getTop();
            int left = width;
            int top = height;
            int right = 0;
            int bottom = 0;
            int i2 = 0;
            for (int childCount = root.getChildCount(); i2 < childCount; childCount = childCount) {
                View child = root.getChildAt(i2);
                Drawable childBg = child.getBackground();
                if (child != content) {
                    if (child.getVisibility() == 0) {
                        if (!isOpaque(childBg)) {
                        }
                    }
                    i2++;
                } else if (childBg == null && (child instanceof ViewGroup) && ((ViewGroup) child).getChildCount() == 0) {
                    i2++;
                }
                left = Math.min(left, child.getLeft() + rootOffsetX);
                top = Math.min(top, child.getTop() + rootOffsetY);
                right = Math.max(right, child.getRight() + rootOffsetX);
                bottom = Math.max(bottom, child.getBottom() + rootOffsetY);
                i2++;
            }
            boolean eachBarCoversTopInY = true;
            int i3 = 0;
            while (i3 < 2) {
                View v = i3 == 0 ? coveringView1 : coveringView2;
                if (v == null || v.getVisibility() != 0 || v.getAlpha() != 1.0f || !isOpaque(v.getBackground())) {
                    eachBarCoversTopInY = false;
                } else {
                    if (v.getTop() <= 0 && v.getBottom() >= height && v.getLeft() <= 0 && v.getRight() >= left) {
                        left = 0;
                    }
                    if (v.getTop() <= 0 && v.getBottom() >= height && v.getLeft() <= right && v.getRight() >= width) {
                        right = width;
                    }
                    if (v.getTop() <= 0 && v.getBottom() >= top && v.getLeft() <= 0 && v.getRight() >= width) {
                        top = 0;
                    }
                    if (v.getTop() <= bottom && v.getBottom() >= height && v.getLeft() <= 0 && v.getRight() >= width) {
                        bottom = height;
                    }
                    eachBarCoversTopInY &= v.getTop() <= 0 && v.getBottom() >= top;
                }
                i3++;
            }
            if (eachBarCoversTopInY && (viewsCoverEntireWidth(coveringView1, coveringView2, width) || viewsCoverEntireWidth(coveringView2, coveringView1, width))) {
                top = 0;
            }
            if (left < right && top < bottom) {
                if (top > 0) {
                    i = 0;
                    this.mBackgroundFallback.setBounds(0, 0, width, top);
                    this.mBackgroundFallback.draw(c);
                } else {
                    i = 0;
                }
                if (left > 0) {
                    this.mBackgroundFallback.setBounds(i, top, left, height);
                    this.mBackgroundFallback.draw(c);
                }
                if (right < width) {
                    this.mBackgroundFallback.setBounds(right, top, width, height);
                    this.mBackgroundFallback.draw(c);
                }
                if (bottom < height) {
                    this.mBackgroundFallback.setBounds(left, bottom, right, height);
                    this.mBackgroundFallback.draw(c);
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
