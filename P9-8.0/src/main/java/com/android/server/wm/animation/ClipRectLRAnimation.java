package com.android.server.wm.animation;

import android.graphics.Rect;
import android.view.animation.ClipRectAnimation;
import android.view.animation.Transformation;

public class ClipRectLRAnimation extends ClipRectAnimation {
    public ClipRectLRAnimation(int fromL, int fromR, int toL, int toR) {
        super(fromL, 0, fromR, 0, toL, 0, toR, 0);
    }

    protected void applyTransformation(float it, Transformation tr) {
        Rect oldClipRect = tr.getClipRect();
        tr.setClipRect(this.mFromRect.left + ((int) (((float) (this.mToRect.left - this.mFromRect.left)) * it)), oldClipRect.top, this.mFromRect.right + ((int) (((float) (this.mToRect.right - this.mFromRect.right)) * it)), oldClipRect.bottom);
    }
}
