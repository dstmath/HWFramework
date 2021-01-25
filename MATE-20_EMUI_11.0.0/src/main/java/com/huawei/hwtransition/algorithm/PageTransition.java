package com.huawei.hwtransition.algorithm;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.view.View;
import com.huawei.hwtransition.AlgorithmUtil;

public class PageTransition extends BaseTransition {
    private static final float CAMARA_DISTANCE_FACTOR = -6.7f;
    private static final int PAGE_LEFT = 0;
    private static final int PAGE_RIGHT = 1;

    public PageTransition() {
        this.mAnimationType = "3D";
        this.mIsUseBg = true;
        this.mIsAlphaMode = false;
        this.mBreakTimes = 2;
        this.mTransformationInfo.mIsBackgroundDirty = true;
        if (this.mTransformationInfo.mCamera == null) {
            this.mTransformationInfo.mCamera = new Camera();
            this.mTransformationInfo.matrix3D = new Matrix();
        }
    }

    @Override // com.huawei.hwtransition.algorithm.BaseTransition
    public int getDrawingOrder(int childCount, int i, int part, float scrollProgress) {
        return part == 0 ? i : (childCount - i) - 1;
    }

    @Override // com.huawei.hwtransition.algorithm.BaseTransition
    public boolean transform(int part, boolean isOverScrollFirst, boolean isOverScrollLast, float scrollProgress, View child) {
        if (this.mLayoutType == 0 && !isOverScrollFirst && !isOverScrollLast) {
            this.mTransformationInfo.mTranslationX = ((((float) child.getMeasuredWidth()) * child.getScaleX()) + ((float) this.mPageSpacing)) * scrollProgress;
            this.mTransformationInfo.mIsMatrixDirty = true;
        }
        Rect rect = new Rect();
        AlgorithmUtil.getTransformRect(child, rect);
        float camaraDis = ((((float) rect.width()) * CAMARA_DISTANCE_FACTOR) * child.getResources().getDisplayMetrics().density) / ((float) child.getResources().getDisplayMetrics().densityDpi);
        if (this.mOrientation == 0) {
            this.mTransformationInfo.mBounds.set(rect.left + ((rect.width() / 2) * part), rect.top, rect.left + ((rect.width() / 2) * (part + 1)), rect.bottom);
        } else {
            this.mTransformationInfo.mBounds.set(0, (rect.height() / 2) * part, rect.width(), (rect.height() / 2) * (part + 1));
        }
        this.mTransformationInfo.mIsBoundsDirty = true;
        if (part == 1) {
            if (scrollProgress < 0.0f) {
                if (scrollProgress < -0.5f) {
                    this.mTransformationInfo.mAlpha = (1.0f + scrollProgress) * 2.0f;
                    this.mTransformationInfo.mIsAlphaDirty = true;
                }
            } else if (scrollProgress >= 0.5f) {
                return false;
            } else {
                this.mTransformationInfo.mCamera.setLocation(0.0f, 0.0f, camaraDis);
                if (this.mOrientation == 0) {
                    this.mTransformationInfo.mRotationY = (-scrollProgress) * 180.0f;
                } else {
                    this.mTransformationInfo.mRotationX = 180.0f * scrollProgress;
                }
                float pivotX = ((float) child.getMeasuredWidth()) / 2.0f;
                this.mTransformationInfo.mPivotX = AlgorithmUtil.transformPivotX(child, pivotX);
                float pivotY = ((float) child.getMeasuredHeight()) / 2.0f;
                this.mTransformationInfo.mPivotY = AlgorithmUtil.transformPivotY(child, pivotY);
                this.mTransformationInfo.mIsMatrixDirty = true;
            }
        } else if (scrollProgress < 0.0f) {
            if (scrollProgress <= -0.5f) {
                return false;
            }
            this.mTransformationInfo.mCamera.setLocation(0.0f, 0.0f, camaraDis);
            if (this.mOrientation == 0) {
                this.mTransformationInfo.mRotationY = (-scrollProgress) * 180.0f;
            } else {
                this.mTransformationInfo.mRotationX = 180.0f * scrollProgress;
            }
            float pivotX2 = ((float) child.getMeasuredWidth()) / 2.0f;
            this.mTransformationInfo.mPivotX = AlgorithmUtil.transformPivotX(child, pivotX2);
            float pivotY2 = ((float) child.getMeasuredHeight()) / 2.0f;
            this.mTransformationInfo.mPivotY = AlgorithmUtil.transformPivotY(child, pivotY2);
            this.mTransformationInfo.mIsMatrixDirty = true;
        } else if (scrollProgress > 0.5f) {
            this.mTransformationInfo.mAlpha = (1.0f - scrollProgress) * 2.0f;
            this.mTransformationInfo.mIsAlphaDirty = true;
        }
        return true;
    }
}
