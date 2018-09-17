package com.huawei.hwtransition.algorithm;

import android.view.View;

public class TranlationTransition extends BaseTransition {
    public TranlationTransition() {
        this.mAnimationType = "2D";
    }

    public boolean transform(int part, boolean isOverScrollFirst, boolean isOverScrollLast, float scrollProgress, View child) {
        if (this.mLayout_type != 0) {
            this.mTransformationInfo.mMatrixDirty = true;
            this.mTransformationInfo.mTranslationX = ((((float) child.getWidth()) * scrollProgress) * child.getScaleX()) * -1.0f;
        } else if (isOverScrollFirst || isOverScrollLast) {
            this.mTransformationInfo.mMatrixDirty = true;
            this.mTransformationInfo.mTranslationX = ((((float) child.getWidth()) * scrollProgress) * child.getScaleX()) * -1.0f;
        }
        return true;
    }
}
