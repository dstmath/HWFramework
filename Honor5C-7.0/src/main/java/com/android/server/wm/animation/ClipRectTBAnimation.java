package com.android.server.wm.animation;

import android.graphics.Rect;
import android.view.animation.ClipRectAnimation;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;

public class ClipRectTBAnimation extends ClipRectAnimation {
    private final int mFromTranslateY;
    private float mNormalizedTime;
    private final int mToTranslateY;
    private final Interpolator mTranslateInterpolator;

    public ClipRectTBAnimation(int fromT, int fromB, int toT, int toB, int fromTranslateY, int toTranslateY, Interpolator translateInterpolator) {
        super(0, fromT, 0, fromB, 0, toT, 0, toB);
        this.mFromTranslateY = fromTranslateY;
        this.mToTranslateY = toTranslateY;
        this.mTranslateInterpolator = translateInterpolator;
    }

    public boolean getTransformation(long currentTime, Transformation outTransformation) {
        long startOffset = getStartOffset();
        long duration = getDuration();
        float normalizedTime = duration != 0 ? ((float) (currentTime - (getStartTime() + startOffset))) / ((float) duration) : currentTime < getStartTime() ? 0.0f : 1.0f;
        this.mNormalizedTime = normalizedTime;
        return super.getTransformation(currentTime, outTransformation);
    }

    protected void applyTransformation(float it, Transformation tr) {
        int translation = (int) (((float) this.mFromTranslateY) + (((float) (this.mToTranslateY - this.mFromTranslateY)) * this.mTranslateInterpolator.getInterpolation(this.mNormalizedTime)));
        Rect oldClipRect = tr.getClipRect();
        tr.setClipRect(oldClipRect.left, (this.mFromRect.top - translation) + ((int) (((float) (this.mToRect.top - this.mFromRect.top)) * it)), oldClipRect.right, (this.mFromRect.bottom - translation) + ((int) (((float) (this.mToRect.bottom - this.mFromRect.bottom)) * it)));
    }
}
