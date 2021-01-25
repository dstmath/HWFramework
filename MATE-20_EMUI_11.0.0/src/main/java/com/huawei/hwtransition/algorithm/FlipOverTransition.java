package com.huawei.hwtransition.algorithm;

import android.animation.TimeInterpolator;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import com.huawei.hwtransition.AlgorithmUtil;
import com.huawei.hwtransition.interpolator.ZInterpolator;

public class FlipOverTransition extends BaseTransition {
    private static final float LEFTSCREEN_INTERPOLATOR_FACTOR = 1.5f;
    private static final int LEFT_PAGE_ANGLE = 80;
    private static final float OVERLAY_FACTOR = (1.0f - ((float) Math.cos(0.7853981633974483d)));
    private static final float RIGHTSCREEN_INTERPOLATOR_FACTOR = 1.5f;
    private static final int RIGHT_PAGE_ANGLE = 45;
    private static final float SCALE_INTERPOLATOR_FACTOR = 0.5f;
    private static final float TRANSITION_SCALE_FACTOR = 0.7f;
    private TimeInterpolator mLeftScreenAlphaInterpolator = new DecelerateInterpolator();
    private TimeInterpolator mLeftScreenInterpolator = new AccelerateInterpolator(1.5f);
    private TimeInterpolator mLeftScreenScaleInterpolator = new ZInterpolator(0.5f);
    private TimeInterpolator mRightScreenInterpolator = new DecelerateInterpolator(1.5f);

    public FlipOverTransition() {
        this.mAnimationType = "3D";
        if (this.mTransformationInfo.mCamera == null) {
            this.mTransformationInfo.mCamera = new Camera();
            this.mTransformationInfo.matrix3D = new Matrix();
        }
    }

    @Override // com.huawei.hwtransition.algorithm.BaseTransition
    public boolean transform(int part, boolean isOverScrollFirst, boolean isOverScrollLast, float scrollProgress, View child) {
        float pivotX;
        float pivotY;
        float angle;
        float translationX;
        float scale;
        float alpha;
        if (scrollProgress < OVERLAY_FACTOR) {
            float interpolatedProgress = this.mRightScreenInterpolator.getInterpolation(Math.abs(scrollProgress));
            angle = -1.0f * interpolatedProgress * 45.0f;
            pivotX = ((float) child.getMeasuredWidth()) / 2.0f;
            pivotY = ((float) child.getMeasuredHeight()) / 2.0f;
            translationX = (((-1.0f * interpolatedProgress) * ((float) child.getMeasuredWidth())) / 2.0f) * OVERLAY_FACTOR;
            alpha = 1.0f;
            scale = 1.0f;
        } else {
            pivotX = OVERLAY_FACTOR;
            pivotY = ((float) child.getMeasuredHeight()) / 2.0f;
            angle = this.mLeftScreenInterpolator.getInterpolation(Math.abs(scrollProgress)) * 80.0f;
            translationX = ((float) child.getMeasuredWidth()) * scrollProgress * child.getScaleX();
            float interpolatedProgress2 = this.mLeftScreenScaleInterpolator.getInterpolation(Math.abs(scrollProgress));
            scale = (1.0f - interpolatedProgress2) + (TRANSITION_SCALE_FACTOR * interpolatedProgress2);
            alpha = this.mLeftScreenAlphaInterpolator.getInterpolation(1.0f - Math.abs(scrollProgress));
        }
        this.mTransformationInfo.mPivotX = AlgorithmUtil.transformPivotX(child, pivotX);
        this.mTransformationInfo.mPivotY = AlgorithmUtil.transformPivotY(child, pivotY);
        this.mTransformationInfo.mRotationY = angle;
        this.mTransformationInfo.mScaleX = scale;
        this.mTransformationInfo.mScaleY = scale;
        this.mTransformationInfo.mIsMatrixDirty = true;
        this.mTransformationInfo.mAlpha = alpha;
        this.mTransformationInfo.mIsAlphaDirty = true;
        if (this.mLayoutType != 0) {
            translationX += ((float) child.getMeasuredWidth()) * scrollProgress * child.getScaleX() * -1.0f;
        } else if (isOverScrollFirst) {
            translationX = (-scrollProgress) * ((float) child.getMeasuredWidth()) * child.getScaleX();
        } else if (isOverScrollLast) {
            translationX = OVERLAY_FACTOR;
        }
        this.mTransformationInfo.mTranslationX = translationX;
        return true;
    }
}
