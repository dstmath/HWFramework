package com.android.server.multiwin.animation;

import android.graphics.Matrix;
import android.graphics.Point;
import android.util.Log;
import android.view.View;
import android.view.animation.ScaleAnimation;
import com.android.server.multiwin.animation.interpolator.FastOutSlowInInterpolator;

public class SplitScaleAnimation {
    private static final int MATRIX_VALUES_NUM = 9;
    private static final float SPLIT_DEFAULT_SCALE_FACTOR = 1.0f;
    private static final long SPLIT_SCALE_DURATION = 200;
    private static final float SPLIT_SCALE_FACTOR = 0.95f;
    private static final String TAG = "SplitScaleAnimation";
    private ScaleAnimation mScaleAnimation;
    private View mScaleTarget;
    private int mSplitMode = 0;

    public SplitScaleAnimation(View scaleTarget, int splitMode) {
        this.mScaleTarget = scaleTarget;
        this.mSplitMode = splitMode;
    }

    public void playScaleDownAnmation() {
        playScaleAnimation(SPLIT_SCALE_FACTOR);
    }

    public void playScaleUpAnimation() {
        playScaleAnimation(1.0f);
    }

    private void playScaleAnimation(float toScale) {
        float fromScaleX;
        float fromScaleY;
        if (this.mScaleTarget != null) {
            ScaleAnimation scaleAnimation = this.mScaleAnimation;
            if (scaleAnimation != null && scaleAnimation.hasStarted()) {
                this.mScaleAnimation.cancel();
            }
            Matrix animationMatrix = this.mScaleTarget.getAnimationMatrix();
            if (animationMatrix != null) {
                float[] values = new float[9];
                animationMatrix.getValues(values);
                fromScaleX = values[0];
                fromScaleY = values[4];
            } else {
                fromScaleX = 1.0f;
                fromScaleY = 1.0f;
            }
            Point pivots = getScalePivots();
            float pivotX = (float) pivots.x;
            float pivotY = (float) pivots.y;
            Log.d(TAG, "start split scale animation mSplitMode = " + this.mSplitMode + ", mScaleTarget.getWidth() = " + this.mScaleTarget.getWidth() + ", mScaleTarget.getHeight() = " + this.mScaleTarget.getHeight());
            this.mScaleAnimation = new ScaleAnimation(fromScaleX, toScale, fromScaleY, toScale, 0, pivotX, 0, pivotY);
            this.mScaleAnimation.setDuration(200);
            this.mScaleAnimation.setInterpolator(new FastOutSlowInInterpolator());
            this.mScaleAnimation.setFillEnabled(true);
            this.mScaleAnimation.setFillBefore(true);
            this.mScaleAnimation.setFillAfter(true);
            this.mScaleTarget.startAnimation(this.mScaleAnimation);
        }
    }

    private Point getScalePivots() {
        float pivotX = this.mScaleTarget.getPivotX();
        float pivotY = this.mScaleTarget.getPivotY();
        int i = this.mSplitMode;
        if (i == 1) {
            pivotX = (float) this.mScaleTarget.getWidth();
        } else if (i == 2) {
            pivotX = 0.0f;
        } else if (i == 3) {
            pivotY = (float) this.mScaleTarget.getHeight();
        } else if (i == 4) {
            pivotY = 0.0f;
        }
        return new Point((int) pivotX, (int) pivotY);
    }
}
