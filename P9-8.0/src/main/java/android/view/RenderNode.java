package android.view;

import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.AnimatedVectorDrawable.VectorDrawableAnimatorRT;
import libcore.util.NativeAllocationRegistry;

public class RenderNode {
    final long mNativeRenderNode;
    private final View mOwningView;

    private static class NoImagePreloadHolder {
        public static final NativeAllocationRegistry sRegistry = new NativeAllocationRegistry(RenderNode.class.getClassLoader(), RenderNode.nGetNativeFinalizer(), 1024);

        private NoImagePreloadHolder() {
        }
    }

    private static native void nAddAnimator(long j, long j2);

    private static native long nCreate(String str);

    private static native void nEndAllAnimators(long j);

    private static native float nGetAlpha(long j);

    private static native float nGetCameraDistance(long j);

    private static native boolean nGetClipToOutline(long j);

    private static native int nGetDebugSize(long j);

    private static native float nGetElevation(long j);

    private static native void nGetInverseTransformMatrix(long j, long j2);

    private static native long nGetNativeFinalizer();

    private static native float nGetPivotX(long j);

    private static native float nGetPivotY(long j);

    private static native float nGetRotation(long j);

    private static native float nGetRotationX(long j);

    private static native float nGetRotationY(long j);

    private static native float nGetScaleX(long j);

    private static native float nGetScaleY(long j);

    private static native void nGetTransformMatrix(long j, long j2);

    private static native float nGetTranslationX(long j);

    private static native float nGetTranslationY(long j);

    private static native float nGetTranslationZ(long j);

    private static native boolean nHasIdentityMatrix(long j);

    private static native boolean nHasOverlappingRendering(long j);

    private static native boolean nHasShadow(long j);

    private static native boolean nIsPivotExplicitlySet(long j);

    private static native boolean nIsValid(long j);

    private static native boolean nOffsetLeftAndRight(long j, int i);

    private static native boolean nOffsetTopAndBottom(long j, int i);

    private static native void nOutput(long j);

    private static native void nRequestPositionUpdates(long j, SurfaceView surfaceView);

    private static native boolean nSetAlpha(long j, float f);

    private static native boolean nSetAnimationMatrix(long j, long j2);

    private static native boolean nSetBottom(long j, int i);

    private static native boolean nSetCameraDistance(long j, float f);

    private static native boolean nSetClipBounds(long j, int i, int i2, int i3, int i4);

    private static native boolean nSetClipBoundsEmpty(long j);

    private static native boolean nSetClipToBounds(long j, boolean z);

    private static native boolean nSetClipToOutline(long j, boolean z);

    private static native void nSetDisplayList(long j, long j2);

    private static native boolean nSetElevation(long j, float f);

    private static native boolean nSetHasOverlappingRendering(long j, boolean z);

    private static native boolean nSetLayerPaint(long j, long j2);

    private static native boolean nSetLayerType(long j, int i);

    private static native boolean nSetLeft(long j, int i);

    private static native boolean nSetLeftTopRightBottom(long j, int i, int i2, int i3, int i4);

    private static native boolean nSetOutlineConvexPath(long j, long j2, float f);

    private static native boolean nSetOutlineEmpty(long j);

    private static native boolean nSetOutlineNone(long j);

    private static native boolean nSetOutlineRoundRect(long j, int i, int i2, int i3, int i4, float f, float f2);

    private static native boolean nSetPivotX(long j, float f);

    private static native boolean nSetPivotY(long j, float f);

    private static native boolean nSetProjectBackwards(long j, boolean z);

    private static native boolean nSetProjectionReceiver(long j, boolean z);

    private static native boolean nSetRevealClip(long j, boolean z, float f, float f2, float f3);

    private static native boolean nSetRight(long j, int i);

    private static native boolean nSetRotation(long j, float f);

    private static native boolean nSetRotationX(long j, float f);

    private static native boolean nSetRotationY(long j, float f);

    private static native boolean nSetScaleX(long j, float f);

    private static native boolean nSetScaleY(long j, float f);

    private static native boolean nSetStaticMatrix(long j, long j2);

    private static native boolean nSetTop(long j, int i);

    private static native boolean nSetTranslationX(long j, float f);

    private static native boolean nSetTranslationY(long j, float f);

    private static native boolean nSetTranslationZ(long j, float f);

    private RenderNode(String name, View owningView) {
        this.mNativeRenderNode = nCreate(name);
        NoImagePreloadHolder.sRegistry.registerNativeAllocation(this, this.mNativeRenderNode);
        this.mOwningView = owningView;
    }

    private RenderNode(long nativePtr) {
        this.mNativeRenderNode = nativePtr;
        NoImagePreloadHolder.sRegistry.registerNativeAllocation(this, this.mNativeRenderNode);
        this.mOwningView = null;
    }

    public void destroy() {
    }

    public static RenderNode create(String name, View owningView) {
        return new RenderNode(name, owningView);
    }

    public static RenderNode adopt(long nativePtr) {
        return new RenderNode(nativePtr);
    }

    public void requestPositionUpdates(SurfaceView view) {
        nRequestPositionUpdates(this.mNativeRenderNode, view);
    }

    public DisplayListCanvas start(int width, int height) {
        return DisplayListCanvas.obtain(this, width, height);
    }

    public void end(DisplayListCanvas canvas) {
        nSetDisplayList(this.mNativeRenderNode, canvas.finishRecording());
        canvas.recycle();
    }

    public void discardDisplayList() {
        nSetDisplayList(this.mNativeRenderNode, 0);
    }

    public boolean isValid() {
        return nIsValid(this.mNativeRenderNode);
    }

    long getNativeDisplayList() {
        if (isValid()) {
            return this.mNativeRenderNode;
        }
        throw new IllegalStateException("The display list is not valid.");
    }

    public boolean hasIdentityMatrix() {
        return nHasIdentityMatrix(this.mNativeRenderNode);
    }

    public void getMatrix(Matrix outMatrix) {
        nGetTransformMatrix(this.mNativeRenderNode, outMatrix.native_instance);
    }

    public void getInverseMatrix(Matrix outMatrix) {
        nGetInverseTransformMatrix(this.mNativeRenderNode, outMatrix.native_instance);
    }

    public boolean setLayerType(int layerType) {
        return nSetLayerType(this.mNativeRenderNode, layerType);
    }

    public boolean setLayerPaint(Paint paint) {
        return nSetLayerPaint(this.mNativeRenderNode, paint != null ? paint.getNativeInstance() : 0);
    }

    public boolean setClipBounds(Rect rect) {
        if (rect == null) {
            return nSetClipBoundsEmpty(this.mNativeRenderNode);
        }
        return nSetClipBounds(this.mNativeRenderNode, rect.left, rect.top, rect.right, rect.bottom);
    }

    public boolean setClipToBounds(boolean clipToBounds) {
        return nSetClipToBounds(this.mNativeRenderNode, clipToBounds);
    }

    public boolean setProjectBackwards(boolean shouldProject) {
        return nSetProjectBackwards(this.mNativeRenderNode, shouldProject);
    }

    public boolean setProjectionReceiver(boolean shouldRecieve) {
        return nSetProjectionReceiver(this.mNativeRenderNode, shouldRecieve);
    }

    public boolean setOutline(Outline outline) {
        if (outline == null) {
            return nSetOutlineNone(this.mNativeRenderNode);
        }
        switch (outline.mMode) {
            case 0:
                return nSetOutlineEmpty(this.mNativeRenderNode);
            case 1:
                return nSetOutlineRoundRect(this.mNativeRenderNode, outline.mRect.left, outline.mRect.top, outline.mRect.right, outline.mRect.bottom, outline.mRadius, outline.mAlpha);
            case 2:
                return nSetOutlineConvexPath(this.mNativeRenderNode, outline.mPath.mNativePath, outline.mAlpha);
            default:
                throw new IllegalArgumentException("Unrecognized outline?");
        }
    }

    public boolean hasShadow() {
        return nHasShadow(this.mNativeRenderNode);
    }

    public boolean setClipToOutline(boolean clipToOutline) {
        return nSetClipToOutline(this.mNativeRenderNode, clipToOutline);
    }

    public boolean getClipToOutline() {
        return nGetClipToOutline(this.mNativeRenderNode);
    }

    public boolean setRevealClip(boolean shouldClip, float x, float y, float radius) {
        return nSetRevealClip(this.mNativeRenderNode, shouldClip, x, y, radius);
    }

    public boolean setStaticMatrix(Matrix matrix) {
        return nSetStaticMatrix(this.mNativeRenderNode, matrix.native_instance);
    }

    public boolean setAnimationMatrix(Matrix matrix) {
        return nSetAnimationMatrix(this.mNativeRenderNode, matrix != null ? matrix.native_instance : 0);
    }

    public boolean setAlpha(float alpha) {
        return nSetAlpha(this.mNativeRenderNode, alpha);
    }

    public float getAlpha() {
        return nGetAlpha(this.mNativeRenderNode);
    }

    public boolean setHasOverlappingRendering(boolean hasOverlappingRendering) {
        return nSetHasOverlappingRendering(this.mNativeRenderNode, hasOverlappingRendering);
    }

    public boolean hasOverlappingRendering() {
        return nHasOverlappingRendering(this.mNativeRenderNode);
    }

    public boolean setElevation(float lift) {
        return nSetElevation(this.mNativeRenderNode, lift);
    }

    public float getElevation() {
        return nGetElevation(this.mNativeRenderNode);
    }

    public boolean setTranslationX(float translationX) {
        return nSetTranslationX(this.mNativeRenderNode, translationX);
    }

    public float getTranslationX() {
        return nGetTranslationX(this.mNativeRenderNode);
    }

    public boolean setTranslationY(float translationY) {
        return nSetTranslationY(this.mNativeRenderNode, translationY);
    }

    public float getTranslationY() {
        return nGetTranslationY(this.mNativeRenderNode);
    }

    public boolean setTranslationZ(float translationZ) {
        return nSetTranslationZ(this.mNativeRenderNode, translationZ);
    }

    public float getTranslationZ() {
        return nGetTranslationZ(this.mNativeRenderNode);
    }

    public boolean setRotation(float rotation) {
        return nSetRotation(this.mNativeRenderNode, rotation);
    }

    public float getRotation() {
        return nGetRotation(this.mNativeRenderNode);
    }

    public boolean setRotationX(float rotationX) {
        return nSetRotationX(this.mNativeRenderNode, rotationX);
    }

    public float getRotationX() {
        return nGetRotationX(this.mNativeRenderNode);
    }

    public boolean setRotationY(float rotationY) {
        return nSetRotationY(this.mNativeRenderNode, rotationY);
    }

    public float getRotationY() {
        return nGetRotationY(this.mNativeRenderNode);
    }

    public boolean setScaleX(float scaleX) {
        return nSetScaleX(this.mNativeRenderNode, scaleX);
    }

    public float getScaleX() {
        return nGetScaleX(this.mNativeRenderNode);
    }

    public boolean setScaleY(float scaleY) {
        return nSetScaleY(this.mNativeRenderNode, scaleY);
    }

    public float getScaleY() {
        return nGetScaleY(this.mNativeRenderNode);
    }

    public boolean setPivotX(float pivotX) {
        return nSetPivotX(this.mNativeRenderNode, pivotX);
    }

    public float getPivotX() {
        return nGetPivotX(this.mNativeRenderNode);
    }

    public boolean setPivotY(float pivotY) {
        return nSetPivotY(this.mNativeRenderNode, pivotY);
    }

    public float getPivotY() {
        return nGetPivotY(this.mNativeRenderNode);
    }

    public boolean isPivotExplicitlySet() {
        return nIsPivotExplicitlySet(this.mNativeRenderNode);
    }

    public boolean setCameraDistance(float distance) {
        return nSetCameraDistance(this.mNativeRenderNode, distance);
    }

    public float getCameraDistance() {
        return nGetCameraDistance(this.mNativeRenderNode);
    }

    public boolean setLeft(int left) {
        return nSetLeft(this.mNativeRenderNode, left);
    }

    public boolean setTop(int top) {
        return nSetTop(this.mNativeRenderNode, top);
    }

    public boolean setRight(int right) {
        return nSetRight(this.mNativeRenderNode, right);
    }

    public boolean setBottom(int bottom) {
        return nSetBottom(this.mNativeRenderNode, bottom);
    }

    public boolean setLeftTopRightBottom(int left, int top, int right, int bottom) {
        return nSetLeftTopRightBottom(this.mNativeRenderNode, left, top, right, bottom);
    }

    public boolean offsetLeftAndRight(int offset) {
        return nOffsetLeftAndRight(this.mNativeRenderNode, offset);
    }

    public boolean offsetTopAndBottom(int offset) {
        return nOffsetTopAndBottom(this.mNativeRenderNode, offset);
    }

    public void output() {
        nOutput(this.mNativeRenderNode);
    }

    public int getDebugSize() {
        return nGetDebugSize(this.mNativeRenderNode);
    }

    public void addAnimator(RenderNodeAnimator animator) {
        if (this.mOwningView == null || this.mOwningView.mAttachInfo == null) {
            throw new IllegalStateException("Cannot start this animator on a detached view!");
        }
        nAddAnimator(this.mNativeRenderNode, animator.getNativeAnimator());
        this.mOwningView.mAttachInfo.mViewRootImpl.registerAnimatingRenderNode(this);
    }

    public boolean isAttached() {
        return (this.mOwningView == null || this.mOwningView.mAttachInfo == null) ? false : true;
    }

    public void registerVectorDrawableAnimator(VectorDrawableAnimatorRT animatorSet) {
        if (this.mOwningView == null || this.mOwningView.mAttachInfo == null) {
            throw new IllegalStateException("Cannot start this animator on a detached view!");
        }
        this.mOwningView.mAttachInfo.mViewRootImpl.registerVectorDrawableAnimator(animatorSet);
    }

    public void endAllAnimators() {
        nEndAllAnimators(this.mNativeRenderNode);
    }
}
