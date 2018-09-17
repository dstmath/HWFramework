package com.huawei.hwtransition.algorithm;

import android.animation.TimeInterpolator;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

public abstract class BaseTransition {
    public static final int LAYOUT_FRAME = 1;
    public static final int LAYOUT_LINEAR = 0;
    private static final float NONZERO_EPSILON = 0.001f;
    public static final int ORIENTATION_HORIZENTAL = 0;
    public static final int ORIENTATION_VERTICAL = 1;
    public static final String TAG = "BaseTransition";
    boolean mAlphaMode = true;
    protected String mAnimationType = null;
    int mBreakTimes = 1;
    float mCameraZ = -8.0f;
    boolean mIsEdge = false;
    boolean mIsScrolling = false;
    int mLayout_type = 0;
    int mOrientation = 0;
    int mPageSpacing = 0;
    boolean mSetCameraZ = false;
    protected TransformationInfo mTransformationInfo = new TransformationInfo();
    public boolean mUseBg = false;

    public static class TransformationInfo {
        public float mAlpha = 1.0f;
        public boolean mAlphaDirty = false;
        public boolean mBackgroundDirty = false;
        public final Rect mBounds = new Rect();
        public boolean mBoundsDirty = false;
        public Camera mCamera = null;
        public final Matrix mMatrix = new Matrix();
        public float[] mMatrix3D = new float[16];
        public boolean mMatrixDirty = false;
        public float mPivotX = 0.0f;
        public float mPivotY = 0.0f;
        public float mPivotZ = 0.0f;
        public float mRotation = 0.0f;
        public float mRotationX = 0.0f;
        public float mRotationY = 0.0f;
        public float mScaleX = 1.0f;
        public float mScaleY = 1.0f;
        public float mScaleZ = 1.0f;
        public float mTranslationX = 0.0f;
        public float mTranslationY = 0.0f;
        public float mTranslationZ = 0.0f;
        public Matrix matrix3D = null;

        private void clearDirty() {
            this.mAlphaDirty = false;
            this.mAlpha = 1.0f;
            this.mMatrixDirty = false;
            this.mBoundsDirty = false;
            clearMatrix();
        }

        private void clearMatrix() {
            this.mRotation = 0.0f;
            this.mRotationX = 0.0f;
            this.mRotationY = 0.0f;
            this.mScaleX = 1.0f;
            this.mScaleY = 1.0f;
            this.mScaleZ = 1.0f;
            this.mTranslationX = 0.0f;
            this.mTranslationY = 0.0f;
            this.mTranslationZ = 0.0f;
        }

        public void dump(String tag) {
            Log.d(tag, "R[" + this.mRotationX + ", " + this.mRotationY + ", " + this.mRotation + "]" + " RP[" + this.mPivotX + ", " + this.mPivotY + ", " + this.mPivotZ + "]" + " T[" + this.mTranslationX + ", " + this.mTranslationY + ", " + this.mTranslationZ + "]" + "S[" + this.mScaleX + ", " + this.mScaleY + ", " + this.mScaleZ + "]" + ", a = " + this.mAlpha);
        }

        public void dump() {
            dump(BaseTransition.TAG);
        }
    }

    public TransformationInfo getTransformation(int part, boolean isOverScrollFirst, boolean isOverScrollLast, float scrollProgress, boolean isEdge, View parent, View child, int pageSpacing) {
        this.mTransformationInfo.clearDirty();
        this.mPageSpacing = pageSpacing;
        this.mIsEdge = isEdge;
        if (!transform(part, isOverScrollFirst, isOverScrollLast, scrollProgress, child)) {
            return null;
        }
        updateMatrix();
        if (!this.mAlphaMode) {
            this.mTransformationInfo.mAlphaDirty = false;
        }
        return this.mTransformationInfo;
    }

    public TransformationInfo getTransformation3D(int idx, float scrollProgress, Rect targetRt) {
        this.mTransformationInfo.clearDirty();
        if (!transform(idx, scrollProgress, targetRt)) {
            return null;
        }
        updateMatrix3D();
        if (!this.mAlphaMode) {
            this.mTransformationInfo.mAlphaDirty = false;
        }
        return this.mTransformationInfo;
    }

    private static boolean nonzero(float value) {
        return value < -0.001f || value > NONZERO_EPSILON;
    }

    public void setLayoutType(int type) {
        this.mLayout_type = type;
    }

    public void setAlphaMode(boolean useAlpha) {
        this.mAlphaMode = useAlpha;
    }

    public void setOrientation(int orientation) {
        this.mOrientation = orientation;
    }

    public boolean isHorizental() {
        return this.mOrientation == 0;
    }

    private void updateMatrix() {
        TransformationInfo info = this.mTransformationInfo;
        if (info.mMatrixDirty) {
            info.mMatrix.reset();
            if (nonzero(info.mRotationX) || nonzero(info.mRotationY)) {
                info.mCamera.save();
                info.mMatrix.preScale(info.mScaleX, info.mScaleY, info.mPivotX, info.mPivotY);
                info.mCamera.translate(0.0f, 0.0f, info.mTranslationZ);
                info.mCamera.rotate(info.mRotationX, info.mRotationY, -info.mRotation);
                info.mCamera.getMatrix(info.matrix3D);
                info.matrix3D.preTranslate(-info.mPivotX, -info.mPivotY);
                info.matrix3D.postTranslate(info.mPivotX + info.mTranslationX, info.mPivotY + info.mTranslationY);
                info.mMatrix.postConcat(info.matrix3D);
                info.mCamera.restore();
                return;
            }
            info.mMatrix.setTranslate(info.mTranslationX, info.mTranslationY);
            info.mMatrix.preRotate(info.mRotation, info.mPivotX, info.mPivotY);
            info.mMatrix.preScale(info.mScaleX, info.mScaleY, info.mPivotX, info.mPivotY);
        }
    }

    private void updateMatrix3D() {
        TransformationInfo info = this.mTransformationInfo;
        if (info.mMatrixDirty) {
            android.opengl.Matrix.setIdentityM(info.mMatrix3D, 0);
            android.opengl.Matrix.translateM(info.mMatrix3D, 0, info.mPivotX + info.mTranslationX, info.mPivotY + info.mTranslationY, info.mPivotZ + info.mTranslationZ);
            if (info.mRotation != 0.0f) {
                android.opengl.Matrix.rotateM(info.mMatrix3D, 0, info.mRotation, 0.0f, 0.0f, 1.0f);
            } else if (info.mRotationX != 0.0f) {
                android.opengl.Matrix.rotateM(info.mMatrix3D, 0, info.mRotationX, 1.0f, 0.0f, 0.0f);
            } else if (info.mRotationY != 0.0f) {
                android.opengl.Matrix.rotateM(info.mMatrix3D, 0, info.mRotationY, 0.0f, 1.0f, 0.0f);
            }
            android.opengl.Matrix.translateM(info.mMatrix3D, 0, -info.mPivotX, -info.mPivotY, -info.mPivotZ);
            android.opengl.Matrix.scaleM(info.mMatrix3D, 0, info.mScaleX, info.mScaleY, info.mScaleZ);
        }
    }

    public int getBreakTimes() {
        return this.mBreakTimes;
    }

    public int getDrawingOrder(int childCount, int i, int part, float scrollProgress) {
        return i;
    }

    public int getBreakOrder(int j, float scrollProgress) {
        return j;
    }

    public boolean transform(int part, boolean isOverScrollFirst, boolean isOverScrollLast, float scrollProgress, View child) {
        return false;
    }

    public boolean transform(int idx, float scrollProgress, Rect targetRt) {
        return false;
    }

    public void setState(boolean isScrolling) {
        this.mIsScrolling = isScrolling;
    }

    public String getAnimationType() {
        return this.mAnimationType;
    }

    public void setCameraDistance(float z) {
        this.mCameraZ = z;
        this.mSetCameraZ = true;
    }

    public void reset() {
        this.mSetCameraZ = false;
    }

    public TimeInterpolator getDefInterpolator() {
        return null;
    }
}
