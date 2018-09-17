package com.huawei.hwtransition.algorithm;

import android.animation.TimeInterpolator;
import android.view.View;
import com.huawei.hwtransition.AlgorithmUtil;
import com.huawei.hwtransition.interpolator.ZInterpolator;

public class EditDepthTransition extends BaseTransition {
    private static final float SCALE_INTERPOLATOR_FACTOR = 0.5f;
    public static final float TRANSITION_SCALE_FACTOR = 0.844f;
    private TimeInterpolator mScaleInterpolator;

    public EditDepthTransition() {
        this.mAnimationType = "2D";
        this.mScaleInterpolator = new ZInterpolator(SCALE_INTERPOLATOR_FACTOR);
    }

    public boolean transform(int part, boolean isOverScrollFirst, boolean isOverScrollLast, float scrollProgress, View child) {
        float interpolatedProgress = this.mScaleInterpolator.getInterpolation(Math.abs(scrollProgress));
        float screenPivotX = ((float) child.getWidth()) / 2.0f;
        this.mTransformationInfo.mPivotX = AlgorithmUtil.transformPivotX(child, screenPivotX);
        float screenPivotY = ((float) child.getHeight()) / 2.0f;
        this.mTransformationInfo.mPivotY = AlgorithmUtil.transformPivotY(child, screenPivotY);
        this.mTransformationInfo.mScaleX = (1.0f - interpolatedProgress) + (0.844f * interpolatedProgress);
        this.mTransformationInfo.mScaleY = this.mTransformationInfo.mScaleX;
        this.mTransformationInfo.mMatrixDirty = true;
        if (this.mLayout_type == 0) {
            float progress = scrollProgress;
            if (isOverScrollFirst) {
                if (!this.mIsEdge) {
                    progress = scrollProgress + 1.0f;
                }
                this.mTransformationInfo.mTranslationX = ((-progress) * ((float) child.getMeasuredWidth())) * child.getScaleX();
            } else if (isOverScrollLast) {
                if (!this.mIsEdge) {
                    progress = scrollProgress - 1.0f;
                }
                this.mTransformationInfo.mTranslationX = ((-progress) * ((float) child.getMeasuredWidth())) * child.getScaleX();
            }
        } else {
            this.mTransformationInfo.mTranslationX = ((((float) child.getMeasuredWidth()) * scrollProgress) * child.getScaleX()) * -1.0f;
        }
        return true;
    }
}
