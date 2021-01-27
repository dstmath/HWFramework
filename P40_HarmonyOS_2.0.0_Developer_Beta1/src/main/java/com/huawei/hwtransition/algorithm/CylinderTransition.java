package com.huawei.hwtransition.algorithm;

import android.animation.TimeInterpolator;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import com.huawei.hwtransition.AlgorithmUtil;

public class CylinderTransition extends BaseTransition {
    private static final float ALPHA_FACTOR_TO_SCROLL_PROGRESS = 10.0f;
    private static final float ALPHA_THRESHOLD = 0.4f;
    public static final float CAMERA_DISTANCE_FACTOR = 2.5f;
    public static final float DEGREE_TO_RADIAN = 0.017453292f;
    private static final float PROGRESS_THRESHOLD = 0.05f;
    public static final float RADIUS_SCALE = 0.9f;
    private static final float SCROLL_PROGRESS_THRESHOLD = 0.9f;
    private float mAngle = -1.0f;
    private TimeInterpolator mInterpolator = new DecelerateInterpolator();
    boolean mIsCylinderform = false;
    private float mRadius = -1.0f;

    public CylinderTransition() {
        this.mAnimationType = "3D";
        setBreakTimes(4);
        if (this.mTransformationInfo.mCamera == null) {
            this.mTransformationInfo.mCamera = new Camera();
            this.mTransformationInfo.matrix3D = new Matrix();
        }
    }

    public final void setBreakTimes(int breakTimes) {
        this.mBreakTimes = breakTimes;
        this.mAngle = 360.0f / (((float) this.mBreakTimes) * 2.0f);
    }

    @Override // com.huawei.hwtransition.algorithm.BaseTransition
    public int getDrawingOrder(int childCount, int indexValue, int part, float scrollProgress) {
        if (scrollProgress == 0.0f || Math.abs(scrollProgress) == 1.0f) {
            return indexValue;
        }
        if (scrollProgress > 0.0f) {
            if (scrollProgress > 0.5f) {
                return indexValue;
            }
            int index = indexValue + 1;
            return index >= childCount ? indexValue : index;
        } else if (scrollProgress > -0.5f) {
            return indexValue;
        } else {
            int index2 = indexValue - 1;
            return index2 < 0 ? indexValue : index2;
        }
    }

    @Override // com.huawei.hwtransition.algorithm.BaseTransition
    public int getBreakOrder(int indexValue, float scrollProgress) {
        return scrollProgress > 0.0f ? indexValue : (this.mBreakTimes - indexValue) - 1;
    }

    @Override // com.huawei.hwtransition.algorithm.BaseTransition
    public boolean transform(int part, boolean isOverScrollFirst, boolean isOverScrollLast, float scrollProgress, View child) {
        if (child == null) {
            return false;
        }
        Rect rect = new Rect();
        AlgorithmUtil.getTransformRect(child, rect);
        float cameraTempZ = ((float) (-child.getMeasuredHeight())) * 2.5f;
        if (this.mIsSetCameraZ) {
            cameraTempZ = this.mCameraZ;
        }
        if (!(cameraTempZ == -8.0f || child.getResources().getDisplayMetrics().densityDpi == 0)) {
            this.mTransformationInfo.mCamera.setLocation(0.0f, 0.0f, (child.getResources().getDisplayMetrics().density * cameraTempZ) / ((float) child.getResources().getDisplayMetrics().densityDpi));
        }
        if (this.mLayoutType == 0 && !isOverScrollFirst && !isOverScrollLast) {
            this.mTransformationInfo.mTranslationX = ((float) rect.width()) * scrollProgress;
        }
        this.mRadius = (0.9f * ((float) rect.width())) / 2.0f;
        float angle = (-90.0f + (this.mAngle * (((float) part) + 0.5f))) - (180.0f * scrollProgress);
        float tz = this.mRadius * (1.0f - ((float) Math.cos((double) (0.017453292f * angle))));
        float alpha = 2.0f - (tz / this.mRadius);
        if (alpha > 1.0f) {
            alpha = 1.0f;
        } else if (alpha < ALPHA_THRESHOLD) {
            alpha = ALPHA_THRESHOLD;
        }
        if (Math.abs(scrollProgress) > 0.9f) {
            alpha = (1.0f - Math.abs(scrollProgress)) * alpha * ALPHA_FACTOR_TO_SCROLL_PROGRESS;
        }
        this.mTransformationInfo.mBounds.set(rect.left + ((rect.width() / this.mBreakTimes) * part), rect.top, rect.left + ((rect.width() / this.mBreakTimes) * (part + 1)), rect.bottom);
        this.mTransformationInfo.mIsBoundsDirty = true;
        float progress = Math.abs(scrollProgress);
        float percent = 1.0f;
        if (progress >= PROGRESS_THRESHOLD) {
            if (alpha == 1.0f) {
                this.mIsCylinderform = true;
            }
            this.mTransformationInfo.mAlpha = alpha;
            this.mTransformationInfo.mIsAlphaDirty = true;
        } else if (!this.mIsScrolling) {
            percent = this.mInterpolator.getInterpolation(progress / PROGRESS_THRESHOLD);
            this.mIsCylinderform = false;
        } else if (!this.mIsCylinderform) {
            percent = this.mInterpolator.getInterpolation(progress / PROGRESS_THRESHOLD);
        }
        float clipCenter = ((float) rect.left) + (((float) (rect.width() / this.mBreakTimes)) * (((float) part) + 0.5f));
        this.mTransformationInfo.mPivotX = clipCenter;
        this.mTransformationInfo.mPivotY = ((float) rect.top) + (((float) rect.height()) / 2.0f);
        this.mTransformationInfo.mRotationY = angle * percent;
        float tx = (((this.mRadius * ((float) Math.sin((double) (0.017453292f * angle)))) + ((float) rect.left)) + (((float) rect.width()) / 2.0f)) - clipCenter;
        this.mTransformationInfo.mTranslationX += tx * percent;
        this.mTransformationInfo.mTranslationZ = tz * percent;
        this.mTransformationInfo.mIsMatrixDirty = true;
        return true;
    }
}
