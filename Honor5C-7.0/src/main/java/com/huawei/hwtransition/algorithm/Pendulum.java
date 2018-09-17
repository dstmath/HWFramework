package com.huawei.hwtransition.algorithm;

import android.animation.TimeInterpolator;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.View;
import com.huawei.hwtransition.interpolator.SimplePendulumDamperInterpolator;

public class Pendulum extends BaseTransition {
    private static final float CAMERA_DISTANCE_FACTOR = 100.0f;
    private static final float ROTATION_ANGLE = 14.0f;

    public Pendulum() {
        this.mAnimationType = "3D";
        if (this.mTransformationInfo.mCamera == null) {
            this.mTransformationInfo.mCamera = new Camera();
            this.mTransformationInfo.matrix3D = new Matrix();
        }
    }

    public boolean transform(int part, boolean isOverScrollFirst, boolean isOverScrollLast, float scrollProgress, View child) {
        this.mTransformationInfo.mCamera.setLocation(0.0f, 0.0f, ((((float) (-child.getMeasuredHeight())) * CAMERA_DISTANCE_FACTOR) * child.getResources().getDisplayMetrics().density) / ((float) child.getResources().getDisplayMetrics().densityDpi));
        this.mTransformationInfo.mPivotX = (float) child.getLeft();
        this.mTransformationInfo.mPivotY = (float) child.getTop();
        this.mTransformationInfo.mRotationX = ROTATION_ANGLE * scrollProgress;
        this.mTransformationInfo.mMatrixDirty = true;
        return true;
    }

    public TimeInterpolator getDefInterpolator() {
        return new SimplePendulumDamperInterpolator();
    }
}
