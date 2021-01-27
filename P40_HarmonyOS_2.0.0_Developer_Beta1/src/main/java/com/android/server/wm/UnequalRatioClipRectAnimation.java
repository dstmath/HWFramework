package com.android.server.wm;

import android.graphics.Rect;
import android.view.animation.ClipRectAnimation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

public class UnequalRatioClipRectAnimation extends ClipRectAnimation {
    private Interpolator mXInterpolator = new LinearInterpolator();
    private Interpolator mYInterpolator = new LinearInterpolator();

    public UnequalRatioClipRectAnimation(Rect fromClip, Rect toClip) {
        super(fromClip, toClip);
    }

    public void setXInterpolator(Interpolator interpolator) {
        this.mXInterpolator = interpolator;
    }

    public void setYInterpolator(Interpolator interpolator) {
        this.mYInterpolator = interpolator;
    }

    public void setInterpolator(Interpolator interpolator) {
        UnequalRatioClipRectAnimation.super.setInterpolator(new LinearInterpolator());
    }

    /* access modifiers changed from: protected */
    public void applyTransformation(float it, Transformation tr) {
        float itX = this.mXInterpolator.getInterpolation(it);
        float itY = this.mYInterpolator.getInterpolation(it);
        tr.setClipRect(this.mFromRect.left + ((int) (((float) (this.mToRect.left - this.mFromRect.left)) * itX)), this.mFromRect.top + ((int) (((float) (this.mToRect.top - this.mFromRect.top)) * itY)), this.mFromRect.right + ((int) (((float) (this.mToRect.right - this.mFromRect.right)) * itX)), this.mFromRect.bottom + ((int) (((float) (this.mToRect.bottom - this.mFromRect.bottom)) * itY)));
    }
}
