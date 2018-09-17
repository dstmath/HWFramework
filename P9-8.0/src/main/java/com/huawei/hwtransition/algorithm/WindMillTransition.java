package com.huawei.hwtransition.algorithm;

import android.util.Log;
import android.view.View;
import com.huawei.hwtransition.AlgorithmUtil;

public class WindMillTransition extends BaseTransition {
    private static final float COORDINATE_Y_FACTOR = 2.0f;
    private static int pageAngle = 45;

    public WindMillTransition() {
        this.mAnimationType = "2D";
    }

    public boolean transform(int part, boolean isOverScrollFirst, boolean isOverScrollLast, float scrollProgress, View child) {
        int cw = child.getWidth();
        int ch = child.getHeight();
        this.mTransformationInfo.mRotation = (-scrollProgress) * ((float) pageAngle);
        float pivotX = ((float) cw) / COORDINATE_Y_FACTOR;
        this.mTransformationInfo.mPivotX = AlgorithmUtil.transformPivotX(child, pivotX);
        if (ch <= cw) {
            ch = cw;
        }
        float pivotY = ((float) ch) * COORDINATE_Y_FACTOR;
        this.mTransformationInfo.mPivotY = AlgorithmUtil.transformPivotY(child, pivotY);
        if (!(this.mLayout_type != 0 || isOverScrollFirst || isOverScrollLast)) {
            this.mTransformationInfo.mTranslationX = (((float) cw) * scrollProgress) * child.getScaleX();
        }
        this.mTransformationInfo.mMatrixDirty = true;
        return true;
    }

    public static void setPageAngle(int wPageAngle) {
        pageAngle = wPageAngle;
        Log.d(BaseTransition.TAG, "windmill transition page angle is " + pageAngle);
    }
}
