package com.huawei.hwtransition.algorithm;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.View;
import com.huawei.hwtransition.AlgorithmUtil;

public class GoRotateTransition extends BaseTransition {
    private static final float CAMERA_DISTANCE_FACTOR = 2.67f;
    private static final int PAGE_ANGLE = 180;

    public GoRotateTransition() {
        this.mAnimationType = "3D";
        if (this.mTransformationInfo.mCamera == null) {
            this.mTransformationInfo.mCamera = new Camera();
            this.mTransformationInfo.matrix3D = new Matrix();
        }
    }

    public boolean transform(int part, boolean isOverScrollFirst, boolean isOverScrollLast, float scrollProgress, View child) {
        if (Math.abs(scrollProgress) > 0.5f) {
            return false;
        }
        int cw = child.getWidth();
        int ch = child.getHeight();
        this.mTransformationInfo.mCamera.setLocation(0.0f, 0.0f, ((((float) (-child.getMeasuredHeight())) * CAMERA_DISTANCE_FACTOR) * child.getResources().getDisplayMetrics().density) / ((float) child.getResources().getDisplayMetrics().densityDpi));
        if (scrollProgress < 0.0f) {
            this.mTransformationInfo.mPivotX = AlgorithmUtil.transformPivotX(child, 0.0f);
        } else {
            float leftScreenPivotX = (float) cw;
            this.mTransformationInfo.mPivotX = AlgorithmUtil.transformPivotX(child, leftScreenPivotX);
        }
        float screenPivotY = ((float) ch) / 2.0f;
        this.mTransformationInfo.mPivotY = AlgorithmUtil.transformPivotY(child, screenPivotY);
        this.mTransformationInfo.mRotationY = (180.0f * scrollProgress) * -1.0f;
        if (this.mLayout_type != 0) {
            this.mTransformationInfo.mTranslationX = ((((float) child.getMeasuredWidth()) * scrollProgress) * child.getScaleX()) * -1.0f;
        } else if (isOverScrollFirst || isOverScrollLast) {
            this.mTransformationInfo.mTranslationX = ((-scrollProgress) * ((float) child.getMeasuredWidth())) * child.getScaleX();
        }
        this.mTransformationInfo.mMatrixDirty = true;
        return true;
    }
}
