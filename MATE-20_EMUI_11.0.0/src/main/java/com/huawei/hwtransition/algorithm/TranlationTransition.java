package com.huawei.hwtransition.algorithm;

import android.view.View;

public class TranlationTransition extends BaseTransition {
    public TranlationTransition() {
        this.mAnimationType = "2D";
    }

    @Override // com.huawei.hwtransition.algorithm.BaseTransition
    public boolean transform(int part, boolean isOverScrollFirst, boolean isOverScrollLast, float scrollProgress, View child) {
        if (this.mLayoutType != 0) {
            this.mTransformationInfo.mIsMatrixDirty = true;
            this.mTransformationInfo.mTranslationX = ((float) child.getWidth()) * scrollProgress * child.getScaleX() * -1.0f;
        } else if (isOverScrollFirst || isOverScrollLast) {
            this.mTransformationInfo.mIsMatrixDirty = true;
            this.mTransformationInfo.mTranslationX = ((float) child.getWidth()) * scrollProgress * child.getScaleX() * -1.0f;
        }
        return true;
    }
}
