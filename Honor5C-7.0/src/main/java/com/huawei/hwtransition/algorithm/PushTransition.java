package com.huawei.hwtransition.algorithm;

import android.view.View;
import com.huawei.hwtransition.AlgorithmUtil;

public class PushTransition extends BaseTransition {
    public PushTransition() {
        this.mAnimationType = "2D";
    }

    public boolean transform(int part, boolean isOverScrollFirst, boolean isOverScrollLast, float scrollProgress, View child) {
        float pivotX;
        float scale;
        int cw = child.getWidth();
        if (scrollProgress < 0.0f) {
            pivotX = (float) cw;
            scale = scrollProgress + 1.0f;
        } else {
            pivotX = 0.0f;
            scale = 1.0f - scrollProgress;
        }
        this.mTransformationInfo.mScaleX = scale;
        this.mTransformationInfo.mPivotX = AlgorithmUtil.transformPivotX(child, pivotX);
        if (!(this.mLayout_type != 0 || isOverScrollFirst || isOverScrollLast)) {
            this.mTransformationInfo.mTranslationX = (((float) cw) * scrollProgress) * child.getScaleX();
        }
        this.mTransformationInfo.mMatrixDirty = true;
        return true;
    }
}
