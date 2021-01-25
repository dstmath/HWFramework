package com.android.server.display.color;

import android.animation.ValueAnimator;
import android.content.Context;
import java.io.PrintWriter;

/* access modifiers changed from: package-private */
public abstract class TintController {
    private ValueAnimator mAnimator;
    private Boolean mIsActivated;

    public abstract int getLevel();

    public abstract float[] getMatrix();

    public abstract boolean isAvailable(Context context);

    public abstract void setMatrix(int i);

    public abstract void setUp(Context context, boolean z);

    TintController() {
    }

    public ValueAnimator getAnimator() {
        return this.mAnimator;
    }

    public void setAnimator(ValueAnimator animator) {
        this.mAnimator = animator;
    }

    public void cancelAnimator() {
        ValueAnimator valueAnimator = this.mAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
    }

    public void endAnimator() {
        ValueAnimator valueAnimator = this.mAnimator;
        if (valueAnimator != null) {
            valueAnimator.end();
            this.mAnimator = null;
        }
    }

    public void setActivated(Boolean isActivated) {
        this.mIsActivated = isActivated;
    }

    public boolean isActivated() {
        Boolean bool = this.mIsActivated;
        return bool != null && bool.booleanValue();
    }

    public boolean isActivatedStateNotSet() {
        return this.mIsActivated == null;
    }

    public void dump(PrintWriter pw) {
    }
}
