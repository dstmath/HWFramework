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
    boolean mAlphaMode;
    protected String mAnimationType;
    int mBreakTimes;
    float mCameraZ;
    boolean mIsEdge;
    boolean mIsScrolling;
    int mLayout_type;
    int mOrientation;
    int mPageSpacing;
    boolean mSetCameraZ;
    protected TransformationInfo mTransformationInfo;
    public boolean mUseBg;

    public static class TransformationInfo {
        public float mAlpha;
        public boolean mAlphaDirty;
        public boolean mBackgroundDirty;
        public final Rect mBounds;
        public boolean mBoundsDirty;
        public Camera mCamera;
        public final Matrix mMatrix;
        public float[] mMatrix3D;
        public boolean mMatrixDirty;
        public float mPivotX;
        public float mPivotY;
        public float mPivotZ;
        public float mRotation;
        public float mRotationX;
        public float mRotationY;
        public float mScaleX;
        public float mScaleY;
        public float mScaleZ;
        public float mTranslationX;
        public float mTranslationY;
        public float mTranslationZ;
        public Matrix matrix3D;

        public TransformationInfo() {
            this.mMatrixDirty = false;
            this.mAlphaDirty = false;
            this.mBoundsDirty = false;
            this.mBackgroundDirty = false;
            this.mAlpha = 1.0f;
            this.mBounds = new Rect();
            this.mMatrix = new Matrix();
            this.mMatrix3D = new float[16];
            this.mCamera = null;
            this.matrix3D = null;
            this.mRotationY = 0.0f;
            this.mRotationX = 0.0f;
            this.mRotation = 0.0f;
            this.mTranslationX = 0.0f;
            this.mTranslationY = 0.0f;
            this.mTranslationZ = 0.0f;
            this.mScaleX = 1.0f;
            this.mScaleY = 1.0f;
            this.mScaleZ = 1.0f;
            this.mPivotX = 0.0f;
            this.mPivotY = 0.0f;
            this.mPivotZ = 0.0f;
        }

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

    public BaseTransition() {
        this.mLayout_type = ORIENTATION_HORIZENTAL;
        this.mCameraZ = -8.0f;
        this.mSetCameraZ = false;
        this.mUseBg = false;
        this.mAlphaMode = true;
        this.mOrientation = ORIENTATION_HORIZENTAL;
        this.mPageSpacing = ORIENTATION_HORIZENTAL;
        this.mIsEdge = false;
        this.mTransformationInfo = new TransformationInfo();
        this.mBreakTimes = ORIENTATION_VERTICAL;
        this.mIsScrolling = false;
        this.mAnimationType = null;
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
            android.opengl.Matrix.setIdentityM(info.mMatrix3D, ORIENTATION_HORIZENTAL);
            android.opengl.Matrix.translateM(info.mMatrix3D, ORIENTATION_HORIZENTAL, info.mPivotX + info.mTranslationX, info.mPivotY + info.mTranslationY, info.mPivotZ + info.mTranslationZ);
            if (info.mRotation != 0.0f) {
                android.opengl.Matrix.rotateM(info.mMatrix3D, ORIENTATION_HORIZENTAL, info.mRotation, 0.0f, 0.0f, 1.0f);
            } else if (info.mRotationX != 0.0f) {
                android.opengl.Matrix.rotateM(info.mMatrix3D, ORIENTATION_HORIZENTAL, info.mRotationX, 1.0f, 0.0f, 0.0f);
            } else if (info.mRotationY != 0.0f) {
                android.opengl.Matrix.rotateM(info.mMatrix3D, ORIENTATION_HORIZENTAL, info.mRotationY, 0.0f, 1.0f, 0.0f);
            }
            android.opengl.Matrix.translateM(info.mMatrix3D, ORIENTATION_HORIZENTAL, -info.mPivotX, -info.mPivotY, -info.mPivotZ);
            android.opengl.Matrix.scaleM(info.mMatrix3D, ORIENTATION_HORIZENTAL, info.mScaleX, info.mScaleY, info.mScaleZ);
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
