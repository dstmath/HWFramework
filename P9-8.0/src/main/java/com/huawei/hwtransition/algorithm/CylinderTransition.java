package com.huawei.hwtransition.algorithm;

import android.animation.TimeInterpolator;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.FloatMath;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import com.huawei.hwtransition.AlgorithmUtil;
import com.huawei.hwtransition.algorithm.BaseTransition.TransformationInfo;

public class CylinderTransition extends BaseTransition {
    public static final float CAMERA_DISTANCE_FACTOR = 2.5f;
    public static final float DEGREE_TO_RADIAN = 0.017453292f;
    public static final float RADIUS_SCALE = 0.9f;
    private float mAngle;
    private TimeInterpolator mInterpolator;
    boolean mIsCylinderform;
    private float mRadius;

    public CylinderTransition() {
        this.mAngle = -1.0f;
        this.mInterpolator = new DecelerateInterpolator();
        this.mRadius = -1.0f;
        this.mIsCylinderform = false;
        this.mAnimationType = "3D";
        setBreakTimes(4);
        if (this.mTransformationInfo.mCamera == null) {
            this.mTransformationInfo.mCamera = new Camera();
            this.mTransformationInfo.matrix3D = new Matrix();
        }
    }

    public void setBreakTimes(int i) {
        this.mBreakTimes = i;
        this.mAngle = 360.0f / ((float) (this.mBreakTimes * 2));
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

    public int getBreakOrder(int j, float scrollProgress) {
        return scrollProgress > 0.0f ? j : (this.mBreakTimes - j) - 1;
    }

    public boolean transform(int part, boolean isOverScrollFirst, boolean isOverScrollLast, float scrollProgress, View child) {
        Rect rect = new Rect();
        AlgorithmUtil.getTransformRect(child, rect);
        float percent = 1.0f;
        float cz = ((float) (-child.getMeasuredHeight())) * 2.5f;
        if (this.mSetCameraZ) {
            cz = this.mCameraZ;
        }
        if (cz != -8.0f) {
            this.mTransformationInfo.mCamera.setLocation(0.0f, 0.0f, (child.getResources().getDisplayMetrics().density * cz) / ((float) child.getResources().getDisplayMetrics().densityDpi));
        }
        if (!(this.mLayout_type != 0 || isOverScrollFirst || isOverScrollLast)) {
            this.mTransformationInfo.mTranslationX = ((float) rect.width()) * scrollProgress;
        }
        this.mRadius = (((float) rect.width()) * 0.9f) / 2.0f;
        float clipCenter = ((float) rect.left) + (((float) (rect.width() / this.mBreakTimes)) * (((float) part) + 0.5f));
        float angle = ((this.mAngle * (((float) part) + 0.5f)) - 0.049804688f) - (180.0f * scrollProgress);
        float tz = this.mRadius * (1.0f - FloatMath.cos(0.017453292f * angle));
        float tx = (((this.mRadius * FloatMath.sin(0.017453292f * angle)) + ((float) rect.left)) + (((float) rect.width()) / 2.0f)) - clipCenter;
        float alpha = 2.0f - (tz / this.mRadius);
        if (alpha > 1.0f) {
            alpha = 1.0f;
        } else if (alpha < 0.4f) {
            alpha = 0.4f;
        }
        if (Math.abs(scrollProgress) > 0.9f) {
            alpha = ((1.0f - Math.abs(scrollProgress)) * alpha) * 10.0f;
        }
        this.mTransformationInfo.mBounds.set(rect.left + ((rect.width() / this.mBreakTimes) * part), rect.top, rect.left + ((rect.width() / this.mBreakTimes) * (part + 1)), rect.bottom);
        this.mTransformationInfo.mBoundsDirty = true;
        float aProgress = Math.abs(scrollProgress);
        if (aProgress >= 0.05f) {
            if (alpha == 1.0f) {
                this.mIsCylinderform = true;
            }
            this.mTransformationInfo.mAlpha = alpha;
            this.mTransformationInfo.mAlphaDirty = true;
        } else if (!this.mIsScrolling) {
            percent = this.mInterpolator.getInterpolation(aProgress / 0.05f);
            this.mIsCylinderform = false;
        } else if (!this.mIsCylinderform) {
            percent = this.mInterpolator.getInterpolation(aProgress / 0.05f);
        }
        this.mTransformationInfo.mPivotX = clipCenter;
        this.mTransformationInfo.mPivotY = (float) (rect.top + (rect.height() / 2));
        this.mTransformationInfo.mRotationY = angle * percent;
        TransformationInfo transformationInfo = this.mTransformationInfo;
        transformationInfo.mTranslationX += tx * percent;
        this.mTransformationInfo.mTranslationZ = tz * percent;
        this.mTransformationInfo.mMatrixDirty = true;
        return true;
    }
}
