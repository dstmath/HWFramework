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

    @Override // com.huawei.hwtransition.algorithm.BaseTransition
    public int getDrawingOrder(int childCount, int i, int part, float scrollProgress) {
        if (scrollProgress == 0.0f || Math.abs(scrollProgress) == 1.0f) {
            return i;
        }
        if (scrollProgress > 0.0f) {
            if (scrollProgress > 0.5f) {
                return i;
            }
            int index = i + 1;
            return index >= childCount ? i : index;
        } else if (scrollProgress > -0.5f) {
            return i;
        } else {
            int index2 = i - 1;
            return index2 < 0 ? i : index2;
        }
    }

    @Override // com.huawei.hwtransition.algorithm.BaseTransition
    public boolean transform(int part, boolean isOverScrollFirst, boolean isOverScrollLast, float scrollProgress, View child) {
        float cz = ((float) (-child.getMeasuredHeight())) * CAMERA_DISTANCE_FACTOR;
        if (this.mIsSetCameraZ) {
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
            this.mTransformationInfo.mRotationY = scrollProgress * 105.0f * -1.0f;
            this.mTransformationInfo.mPivotY = AlgorithmUtil.transformPivotY(child, ((float) child.getMeasuredHeight()) / 2.0f);
        } else {
            this.mTransformationInfo.mRotationX = scrollProgress * 105.0f;
            this.mTransformationInfo.mPivotX = AlgorithmUtil.transformPivotX(child, ((float) child.getMeasuredWidth()) / 2.0f);
        }
        this.mTransformationInfo.mAlpha = 1.0f - Math.abs(scrollProgress);
        this.mTransformationInfo.mIsAlphaDirty = true;
        if (this.mLayoutType == 0) {
            if (isOverScrollFirst || isOverScrollLast) {
                this.mTransformationInfo.mTranslationX = ((float) child.getMeasuredWidth()) * scrollProgress * child.getScaleX() * -1.0f;
            } else {
                this.mTransformationInfo.mTranslationX = 0.0f;
            }
        } else if (this.mOrientation == 0) {
            this.mTransformationInfo.mTranslationX = ((float) child.getMeasuredWidth()) * scrollProgress * child.getScaleX() * -1.0f;
        } else {
            this.mTransformationInfo.mTranslationY = ((float) child.getMeasuredHeight()) * scrollProgress * child.getScaleY() * -1.0f;
        }
        this.mTransformationInfo.mIsMatrixDirty = true;
        return true;
    }
}
