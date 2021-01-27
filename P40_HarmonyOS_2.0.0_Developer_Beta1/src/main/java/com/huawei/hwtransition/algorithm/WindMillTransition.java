package com.huawei.hwtransition.algorithm;

import android.util.Log;
import android.view.View;
import com.huawei.hwtransition.AlgorithmUtil;

public class WindMillTransition extends BaseTransition {
    private static final float COORDINATE_Y_FACTOR = 2.0f;
    private static final int DEFAULT_PAGE_ANGLE = 45;
    private static int pageAngle = DEFAULT_PAGE_ANGLE;

    public WindMillTransition() {
        this.mAnimationType = "2D";
    }

    @Override // com.huawei.hwtransition.algorithm.BaseTransition
    public boolean transform(int part, boolean isOverScrollFirst, boolean isOverScrollLast, float scrollProgress, View child) {
        if (child == null) {
            return false;
        }
        int childWidth = child.getWidth();
        int childHeight = child.getHeight();
        this.mTransformationInfo.mRotation = (-scrollProgress) * ((float) pageAngle);
        this.mTransformationInfo.mPivotX = AlgorithmUtil.transformPivotX(child, ((float) childWidth) / 2.0f);
        this.mTransformationInfo.mPivotY = AlgorithmUtil.transformPivotY(child, (childHeight > childWidth ? (float) childHeight : (float) childWidth) * 2.0f);
        if (this.mLayoutType == 0 && !isOverScrollFirst && !isOverScrollLast) {
            this.mTransformationInfo.mTranslationX = ((float) childWidth) * scrollProgress * child.getScaleX();
        }
        this.mTransformationInfo.mIsMatrixDirty = true;
        return true;
    }

    public static void setPageAngle(int pageAngleValue) {
        pageAngle = pageAngleValue;
        Log.d(BaseTransition.TAG, "windmill transition page angle is " + pageAngle);
    }
}
