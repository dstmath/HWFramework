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

    public void draw(ViewGroup root, Canvas c, View content) {
        if (hasFallback()) {
            int width = root.getWidth();
            int height = root.getHeight();
            int left = width;
            int top = height;
            int right = 0;
            int bottom = 0;
            int childCount = root.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = root.getChildAt(i);
                Drawable childBg = child.getBackground();
                if (child == content) {
                    if (childBg == null && (child instanceof ViewGroup) && ((ViewGroup) child).getChildCount() == 0) {
                    }
                    left = Math.min(left, child.getLeft());
                    top = Math.min(top, child.getTop());
                    right = Math.max(right, child.getRight());
                    bottom = Math.max(bottom, child.getBottom());
                } else if (child.getVisibility() == 0) {
                    if (childBg != null) {
                        if (childBg.getOpacity() != -1) {
                        }
                        left = Math.min(left, child.getLeft());
                        top = Math.min(top, child.getTop());
                        right = Math.max(right, child.getRight());
                        bottom = Math.max(bottom, child.getBottom());
                    }
                }
            }
            if (left < right && top < bottom) {
                if (top > 0) {
                    this.mBackgroundFallback.setBounds(0, 0, width, top);
                    this.mBackgroundFallback.draw(c);
                }
                if (left > 0) {
                    this.mBackgroundFallback.setBounds(0, top, left, height);
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
}
