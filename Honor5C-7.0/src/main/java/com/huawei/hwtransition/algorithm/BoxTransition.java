package com.huawei.hwtransition.algorithm;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.View;
import com.huawei.hwtransition.AlgorithmUtil;

public class BoxTransition extends BaseTransition {
    private static final float CAMERA_DISTANCE_FACTOR = 2.67f;
    private static final int PAGE_ANGLE = 105;

    public BoxTransition() {
        this.mAnimationType = "3D";
        if (this.mTransformationInfo.mCamera == null) {
            this.mTransformationInfo.mCamera = new Camera();
            this.mTransformationInfo.matrix3D = new Matrix();
        }
    }

    public int getDrawingOrder(int childCount, int i, int part, float scrollProgress) {
        if (scrollProgress == 0.0f || Math.abs(scrollProgress) == 1.0f) {
            return i;
        }
        int index;
        if (scrollProgress > 0.0f) {
            if (((double) scrollProgress) <= 0.5d) {
                index = i + 1;
                if (index >= childCount) {
                    index = i;
                }
            } else {
                index = i;
            }
        } else if (((double) scrollProgress) > -0.5d) {
            index = i;
        } else {
            index = i - 1;
            if (index < 0) {
                index = i;
            }
        }
        return index;
    }

    public boolean transform(int part, boolean isOverScrollFirst, boolean isOverScrollLast, float scrollProgress, View child) {
        float cz = ((float) (-child.getMeasuredHeight())) * CAMERA_DISTANCE_FACTOR;
        if (this.mSetCameraZ) {
            cz = this.mCameraZ;
        }
        if (cz != -8.0f) {
            this.mTransformationInfo.mCamera.setLocation(0.0f, 0.0f, (child.getResources().getDisplayMetrics().density * cz) / ((float) child.getResources().getDisplayMetrics().densityDpi));
        }
        if (scrollProgress < 0.0f) {
            if (this.mOrientation == 0) {
                this.mTransformationInfo.mPivotX = AlgorithmUtil.transformPivotX(child, 0.0f);
            } else {
                this.mTransformationInfo.mPivotY = AlgorithmUtil.transformPivotY(child, 0.0f);
            }
        } else if (this.mOrientation == 0) {
            this.mTransformationInfo.mPivotX = AlgorithmUtil.transformPivotX(child, (float) child.getMeasuredWidth());
        } else {
            this.mTransformationInfo.mPivotY = AlgorithmUtil.transformPivotY(child, (float) child.getMeasuredHeight());
        }
        if (this.mOrientation == 0) {
            this.mTransformationInfo.mRotationY = (105.0f * scrollProgress) * -1.0f;
            this.mTransformationInfo.mPivotY = AlgorithmUtil.transformPivotY(child, ((float) child.getMeasuredHeight()) / 2.0f);
        } else {
            this.mTransformationInfo.mRotationX = 105.0f * scrollProgress;
            this.mTransformationInfo.mPivotX = AlgorithmUtil.transformPivotX(child, ((float) child.getMeasuredWidth()) / 2.0f);
        }
        this.mTransformationInfo.mAlpha = 1.0f - Math.abs(scrollProgress);
        this.mTransformationInfo.mAlphaDirty = true;
        if (this.mLayout_type == 0) {
            if (isOverScrollFirst || isOverScrollLast) {
                this.mTransformationInfo.mTranslationX = ((((float) child.getMeasuredWidth()) * scrollProgress) * child.getScaleX()) * -1.0f;
            } else {
                this.mTransformationInfo.mTranslationX = 0.0f;
            }
        } else if (this.mOrientation == 0) {
            this.mTransformationInfo.mTranslationX = ((((float) child.getMeasuredWidth()) * scrollProgress) * child.getScaleX()) * -1.0f;
        } else {
            this.mTransformationInfo.mTranslationY = ((((float) child.getMeasuredHeight()) * scrollProgress) * child.getScaleY()) * -1.0f;
        }
        this.mTransformationInfo.mMatrixDirty = true;
        return true;
    }
}
