package com.huawei.hwtransition.algorithm;

import android.view.View;
import com.huawei.hwtransition.AlgorithmUtil;

public class PushTransition extends BaseTransition {
    public PushTransition() {
        this.mAnimationType = "2D";
    }

    @Override // com.huawei.hwtransition.algorithm.BaseTransition
    public boolean transform(int part, boolean isOverScrollFirst, boolean isOverScrollLast, float scrollProgress, View child) {
        float pivotX;
        float scale;
        if (child == null) {
            return false;
        }
        int childWidth = child.getWidth();
        if (scrollProgress < 0.0f) {
            pivotX = (float) childWidth;
            scale = scrollProgress + 1.0f;
        } else {
            pivotX = 0.0f;
            scale = 1.0f - scrollProgress;
        }
        this.mTransformationInfo.mScaleX = scale;
        this.mTransformationInfo.mPivotX = AlgorithmUtil.transformPivotX(child, pivotX);
        if (this.mLayoutType == 0 && !isOverScrollFirst && !isOverScrollLast) {
            this.mTransformationInfo.mTranslationX = ((float) childWidth) * scrollProgress * child.getScaleX();
        }
        this.mTransformationInfo.mIsMatrixDirty = true;
        return true;
    }
}
