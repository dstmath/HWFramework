package android.graphics;

import android.view.NativeVectorDrawableAnimator;
import android.view.RenderNodeAnimator;
import com.android.internal.util.ArrayUtils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import libcore.util.NativeAllocationRegistry;

public final class RenderNode {
    public static final int USAGE_BACKGROUND = 1;
    public static final int USAGE_UNKNOWN = 0;
    private final AnimationHost mAnimationHost;
    private CompositePositionUpdateListener mCompositePositionUpdateListener;
    private RecordingCanvas mCurrentRecordingCanvas;
    public final long mNativeRenderNode;

    public interface AnimationHost {
        boolean isAttached();

        void registerAnimatingRenderNode(RenderNode renderNode);

        void registerVectorDrawableAnimator(NativeVectorDrawableAnimator nativeVectorDrawableAnimator);
    }

    public interface PositionUpdateListener {
        void positionChanged(long j, int i, int i2, int i3, int i4);

        void positionLost(long j);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface UsageHint {
    }

    private static native void nAddAnimator(long j, long j2);

    private static native long nCreate(String str);

    private static native void nEndAllAnimators(long j);

    private static native boolean nGetAllowForceDark(long j);

    private static native float nGetAlpha(long j);

    private static native int nGetAmbientShadowColor(long j);

    private static native boolean nGetAnimationMatrix(long j, long j2);

    private static native int nGetBottom(long j);

    private static native float nGetCameraDistance(long j);

    private static native boolean nGetClipToBounds(long j);

    private static native boolean nGetClipToOutline(long j);

    private static native int nGetDebugSize(long j);

    private static native float nGetElevation(long j);

    private static native int nGetHeight(long j);

    private static native void nGetInverseTransformMatrix(long j, long j2);

    private static native int nGetLayerType(long j);

    private static native int nGetLeft(long j);

    /* access modifiers changed from: private */
    public static native long nGetNativeFinalizer();

    private static native float nGetPivotX(long j);

    private static native float nGetPivotY(long j);

    private static native int nGetRight(long j);

    private static native float nGetRotation(long j);

    private static native float nGetRotationX(long j);

    private static native float nGetRotationY(long j);

    private static native float nGetScaleX(long j);

    private static native float nGetScaleY(long j);

    private static native boolean nGetShadowClip(long j);

    private static native int nGetSpotShadowColor(long j);

    private static native int nGetTop(long j);

    private static native void nGetTransformMatrix(long j, long j2);

    private static native float nGetTranslationX(long j);

    private static native float nGetTranslationY(long j);

    private static native float nGetTranslationZ(long j);

    private static native long nGetUniqueId(long j);

    private static native int nGetWidth(long j);

    private static native boolean nHasIdentityMatrix(long j);

    private static native boolean nHasOverlappingRendering(long j);

    private static native boolean nHasShadow(long j);

    private static native boolean nIsPivotExplicitlySet(long j);

    private static native boolean nIsValid(long j);

    private static native boolean nOffsetLeftAndRight(long j, int i);

    private static native boolean nOffsetTopAndBottom(long j, int i);

    private static native void nOutput(long j);

    private static native void nRequestPositionUpdates(long j, PositionUpdateListener positionUpdateListener);

    private static native boolean nResetPivot(long j);

    private static native boolean nSetAllowForceDark(long j, boolean z);

    private static native boolean nSetAlpha(long j, float f);

    private static native boolean nSetAmbientShadowColor(long j, int i);

    private static native boolean nSetAnimationMatrix(long j, long j2);

    private static native boolean nSetBottom(long j, int i);

    private static native boolean nSetCameraDistance(long j, float f);

    private static native boolean nSetClipBounds(long j, int i, int i2, int i3, int i4);

    private static native boolean nSetClipBoundsEmpty(long j);

    private static native boolean nSetClipToBounds(long j, boolean z);

    private static native boolean nSetClipToOutline(long j, boolean z);

    private static native boolean nSetDirtyLeftTopRightBottom(long j, int i, int i2, int i3, int i4);

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

    private static native boolean nSetOutlineOutColor(long j, int i);

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

    private static native boolean nSetShadowClip(long j, boolean z);

    private static native boolean nSetShadowEffectParams(long j, float f, float f2, float f3, float f4);

    private static native boolean nSetSpotShadowColor(long j, int i);

    private static native boolean nSetStaticMatrix(long j, long j2);

    private static native boolean nSetTop(long j, int i);

    private static native boolean nSetTranslationX(long j, float f);

    private static native boolean nSetTranslationY(long j, float f);

    private static native boolean nSetTranslationZ(long j, float f);

    private static native void nSetUsageHint(long j, int i);

    private static class NoImagePreloadHolder {
        public static final NativeAllocationRegistry sRegistry = NativeAllocationRegistry.createMalloced(RenderNode.class.getClassLoader(), RenderNode.nGetNativeFinalizer());

        private NoImagePreloadHolder() {
        }
    }

    public RenderNode(String name) {
        this(name, null);
    }

    private RenderNode(String name, AnimationHost animationHost) {
        this.mNativeRenderNode = nCreate(name);
        NoImagePreloadHolder.sRegistry.registerNativeAllocation(this, this.mNativeRenderNode);
        this.mAnimationHost = animationHost;
    }

    private RenderNode(long nativePtr) {
        this.mNativeRenderNode = nativePtr;
        NoImagePreloadHolder.sRegistry.registerNativeAllocation(this, this.mNativeRenderNode);
        this.mAnimationHost = null;
    }

    public static RenderNode create(String name, AnimationHost animationHost) {
        return new RenderNode(name, animationHost);
    }

    public static RenderNode adopt(long nativePtr) {
        return new RenderNode(nativePtr);
    }

    /* access modifiers changed from: private */
    public static final class CompositePositionUpdateListener implements PositionUpdateListener {
        private static final PositionUpdateListener[] sEmpty = new PositionUpdateListener[0];
        private final PositionUpdateListener[] mListeners;

        CompositePositionUpdateListener(PositionUpdateListener... listeners) {
            this.mListeners = listeners != null ? listeners : sEmpty;
        }

        public CompositePositionUpdateListener with(PositionUpdateListener listener) {
            return new CompositePositionUpdateListener((PositionUpdateListener[]) ArrayUtils.appendElement(PositionUpdateListener.class, this.mListeners, listener));
        }

        public CompositePositionUpdateListener without(PositionUpdateListener listener) {
            return new CompositePositionUpdateListener((PositionUpdateListener[]) ArrayUtils.removeElement(PositionUpdateListener.class, this.mListeners, listener));
        }

        @Override // android.graphics.RenderNode.PositionUpdateListener
        public void positionChanged(long frameNumber, int left, int top, int right, int bottom) {
            for (PositionUpdateListener pul : this.mListeners) {
                pul.positionChanged(frameNumber, left, top, right, bottom);
            }
        }

        @Override // android.graphics.RenderNode.PositionUpdateListener
        public void positionLost(long frameNumber) {
            for (PositionUpdateListener pul : this.mListeners) {
                pul.positionLost(frameNumber);
            }
        }
    }

    public void addPositionUpdateListener(PositionUpdateListener listener) {
        CompositePositionUpdateListener comp;
        CompositePositionUpdateListener comp2 = this.mCompositePositionUpdateListener;
        if (comp2 == null) {
            comp = new CompositePositionUpdateListener(listener);
        } else {
            comp = comp2.with(listener);
        }
        this.mCompositePositionUpdateListener = comp;
        nRequestPositionUpdates(this.mNativeRenderNode, comp);
    }

    public void removePositionUpdateListener(PositionUpdateListener listener) {
        CompositePositionUpdateListener comp = this.mCompositePositionUpdateListener;
        if (comp != null) {
            CompositePositionUpdateListener comp2 = comp.without(listener);
            this.mCompositePositionUpdateListener = comp2;
            nRequestPositionUpdates(this.mNativeRenderNode, comp2);
        }
    }

    public RecordingCanvas beginRecording(int width, int height) {
        if (this.mCurrentRecordingCanvas == null) {
            this.mCurrentRecordingCanvas = RecordingCanvas.obtain(this, width, height);
            return this.mCurrentRecordingCanvas;
        }
        throw new IllegalStateException("Recording currently in progress - missing #endRecording() call?");
    }

    public RecordingCanvas beginRecording() {
        return beginRecording(nGetWidth(this.mNativeRenderNode), nGetHeight(this.mNativeRenderNode));
    }

    public void endRecording() {
        if (this.mCurrentRecordingCanvas != null) {
            RecordingCanvas canvas = this.mCurrentRecordingCanvas;
            this.mCurrentRecordingCanvas = null;
            nSetDisplayList(this.mNativeRenderNode, canvas.finishRecording());
            canvas.recycle();
            return;
        }
        throw new IllegalStateException("No recording in progress, forgot to call #beginRecording()?");
    }

    @Deprecated
    public RecordingCanvas start(int width, int height) {
        return beginRecording(width, height);
    }

    @Deprecated
    public void end(RecordingCanvas canvas) {
        if (canvas == this.mCurrentRecordingCanvas) {
            endRecording();
            return;
        }
        throw new IllegalArgumentException("Wrong canvas");
    }

    public void discardDisplayList() {
        nSetDisplayList(this.mNativeRenderNode, 0);
    }

    public boolean hasDisplayList() {
        return nIsValid(this.mNativeRenderNode);
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

    @Deprecated
    public boolean setLayerType(int layerType) {
        return nSetLayerType(this.mNativeRenderNode, layerType);
    }

    @Deprecated
    public boolean setLayerPaint(Paint paint) {
        return nSetLayerPaint(this.mNativeRenderNode, paint != null ? paint.getNativeInstance() : 0);
    }

    public boolean setUseCompositingLayer(boolean forceToLayer, Paint paint) {
        return nSetLayerType(this.mNativeRenderNode, forceToLayer ? 2 : 0) | nSetLayerPaint(this.mNativeRenderNode, paint != null ? paint.getNativeInstance() : 0);
    }

    public boolean getUseCompositingLayer() {
        return nGetLayerType(this.mNativeRenderNode) != 0;
    }

    public boolean setClipRect(Rect rect) {
        if (rect == null) {
            return nSetClipBoundsEmpty(this.mNativeRenderNode);
        }
        return nSetClipBounds(this.mNativeRenderNode, rect.left, rect.top, rect.right, rect.bottom);
    }

    public boolean setClipToBounds(boolean clipToBounds) {
        return nSetClipToBounds(this.mNativeRenderNode, clipToBounds);
    }

    public boolean getClipToBounds() {
        return nGetClipToBounds(this.mNativeRenderNode);
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
        int i = outline.mMode;
        if (i == 0) {
            return nSetOutlineEmpty(this.mNativeRenderNode);
        }
        if (i == 1) {
            nSetOutlineOutColor(this.mNativeRenderNode, outline.getOutColor());
            return nSetOutlineRoundRect(this.mNativeRenderNode, outline.mRect.left, outline.mRect.top, outline.mRect.right, outline.mRect.bottom, outline.mRadius, outline.mAlpha);
        } else if (i == 2) {
            return nSetOutlineConvexPath(this.mNativeRenderNode, outline.mPath.mNativePath, outline.mAlpha);
        } else {
            throw new IllegalArgumentException("Unrecognized outline?");
        }
    }

    public boolean hasShadow() {
        return nHasShadow(this.mNativeRenderNode);
    }

    public boolean setSpotShadowColor(int color) {
        return nSetSpotShadowColor(this.mNativeRenderNode, color);
    }

    public int getSpotShadowColor() {
        return nGetSpotShadowColor(this.mNativeRenderNode);
    }

    public boolean setAmbientShadowColor(int color) {
        return nSetAmbientShadowColor(this.mNativeRenderNode, color);
    }

    public int getAmbientShadowColor() {
        return nGetAmbientShadowColor(this.mNativeRenderNode);
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

    public Matrix getAnimationMatrix() {
        Matrix output = new Matrix();
        if (nGetAnimationMatrix(this.mNativeRenderNode, output.native_instance)) {
            return output;
        }
        return null;
    }

    public boolean setAlpha(float alpha) {
        return nSetAlpha(this.mNativeRenderNode, alpha);
    }

    public float getAlpha() {
        return nGetAlpha(this.mNativeRenderNode);
    }

    public boolean setShadowClip(boolean isShadowClip) {
        return nSetShadowClip(this.mNativeRenderNode, isShadowClip);
    }

    public boolean getShadowClip() {
        return nGetShadowClip(this.mNativeRenderNode);
    }

    public boolean setHasOverlappingRendering(boolean hasOverlappingRendering) {
        return nSetHasOverlappingRendering(this.mNativeRenderNode, hasOverlappingRendering);
    }

    public void setUsageHint(int usageHint) {
        nSetUsageHint(this.mNativeRenderNode, usageHint);
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

    public boolean setShadowEffectParams(float elevation, float ambientAlpha, float spotAlpha, float translationY) {
        return nSetShadowEffectParams(this.mNativeRenderNode, elevation, ambientAlpha, spotAlpha, translationY);
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

    public boolean setRotationZ(float rotation) {
        return nSetRotation(this.mNativeRenderNode, rotation);
    }

    public float getRotationZ() {
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

    public boolean resetPivot() {
        return nResetPivot(this.mNativeRenderNode);
    }

    public boolean setCameraDistance(float distance) {
        if (Float.isFinite(distance) && distance >= 0.0f) {
            return nSetCameraDistance(this.mNativeRenderNode, -distance);
        }
        throw new IllegalArgumentException("distance must be finite & positive, given=" + distance);
    }

    public float getCameraDistance() {
        return -nGetCameraDistance(this.mNativeRenderNode);
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

    public int getLeft() {
        return nGetLeft(this.mNativeRenderNode);
    }

    public int getTop() {
        return nGetTop(this.mNativeRenderNode);
    }

    public int getRight() {
        return nGetRight(this.mNativeRenderNode);
    }

    public int getBottom() {
        return nGetBottom(this.mNativeRenderNode);
    }

    public int getWidth() {
        return nGetWidth(this.mNativeRenderNode);
    }

    public int getHeight() {
        return nGetHeight(this.mNativeRenderNode);
    }

    public boolean setLeftTopRightBottom(int left, int top, int right, int bottom) {
        return nSetLeftTopRightBottom(this.mNativeRenderNode, left, top, right, bottom);
    }

    public boolean setDirtyLeftTopRightBottom(int dirtyLeft, int dirtyTop, int dirtyRight, int dirtyBottom) {
        return nSetDirtyLeftTopRightBottom(this.mNativeRenderNode, dirtyLeft, dirtyTop, dirtyRight, dirtyBottom);
    }

    public boolean setPosition(int left, int top, int right, int bottom) {
        return nSetLeftTopRightBottom(this.mNativeRenderNode, left, top, right, bottom);
    }

    public boolean setPosition(Rect position) {
        return nSetLeftTopRightBottom(this.mNativeRenderNode, position.left, position.top, position.right, position.bottom);
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

    public long computeApproximateMemoryUsage() {
        return (long) nGetDebugSize(this.mNativeRenderNode);
    }

    public boolean setForceDarkAllowed(boolean allow) {
        return nSetAllowForceDark(this.mNativeRenderNode, allow);
    }

    public boolean isForceDarkAllowed() {
        return nGetAllowForceDark(this.mNativeRenderNode);
    }

    public long getUniqueId() {
        return nGetUniqueId(this.mNativeRenderNode);
    }

    public void addAnimator(RenderNodeAnimator animator) {
        if (isAttached()) {
            nAddAnimator(this.mNativeRenderNode, animator.getNativeAnimator());
            this.mAnimationHost.registerAnimatingRenderNode(this);
            return;
        }
        throw new IllegalStateException("Cannot start this animator on a detached view!");
    }

    public boolean isAttached() {
        AnimationHost animationHost = this.mAnimationHost;
        return animationHost != null && animationHost.isAttached();
    }

    public void registerVectorDrawableAnimator(NativeVectorDrawableAnimator animatorSet) {
        if (isAttached()) {
            this.mAnimationHost.registerVectorDrawableAnimator(animatorSet);
            return;
        }
        throw new IllegalStateException("Cannot start this animator on a detached view!");
    }

    public void endAllAnimators() {
        nEndAllAnimators(this.mNativeRenderNode);
    }
}
