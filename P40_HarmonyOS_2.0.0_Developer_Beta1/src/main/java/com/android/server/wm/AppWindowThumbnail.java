package com.android.server.wm;

import android.graphics.GraphicBuffer;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Binder;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.animation.Animation;
import com.android.server.wm.SurfaceAnimator;

public class AppWindowThumbnail implements SurfaceAnimator.Animatable {
    private static final int MATRIX_TO_ARRAY_LEN = 9;
    private static final String TAG = "WindowManager";
    private final AppWindowToken mAppToken;
    private final int mHeight;
    private SurfaceControl mInnerThumbnailLeash;
    private final boolean mRelative;
    private final SurfaceAnimator mSurfaceAnimator;
    private SurfaceControl mSurfaceControl;
    private final int mWidth;

    AppWindowThumbnail(SurfaceControl.Transaction t, AppWindowToken appToken, GraphicBuffer thumbnailHeader) {
        this(t, appToken, thumbnailHeader, false);
    }

    AppWindowThumbnail(SurfaceControl.Transaction t, AppWindowToken appToken, GraphicBuffer thumbnailHeader, boolean relative) {
        this(t, appToken, thumbnailHeader, relative, new Surface(), null);
    }

    AppWindowThumbnail(SurfaceControl.Transaction t, AppWindowToken appToken, GraphicBuffer thumbnailHeader, boolean relative, Surface drawSurface, SurfaceAnimator animator) {
        this.mAppToken = appToken;
        this.mRelative = relative;
        if (animator != null) {
            this.mSurfaceAnimator = animator;
        } else {
            this.mSurfaceAnimator = new SurfaceAnimator(this, new Runnable() {
                /* class com.android.server.wm.$$Lambda$AppWindowThumbnail$hHTeq2FR5SSE1YyVM6KwuzeLLo */

                @Override // java.lang.Runnable
                public final void run() {
                    AppWindowThumbnail.m2lambda$hHTeq2FR5SSE1YyVM6KwuzeLLo(AppWindowThumbnail.this);
                }
            }, appToken.mWmService);
        }
        this.mWidth = thumbnailHeader.getWidth();
        this.mHeight = thumbnailHeader.getHeight();
        WindowState window = appToken.findMainWindow();
        SurfaceControl.Builder makeSurface = appToken.makeSurface();
        this.mSurfaceControl = makeSurface.setName("thumbnail anim: " + appToken.toString()).setBufferSize(this.mWidth, this.mHeight).setFormat(-3).setMetadata(2, appToken.windowType).setMetadata(1, window != null ? window.mOwnerUid : Binder.getCallingUid()).build();
        drawSurface.copyFrom(this.mSurfaceControl);
        drawSurface.attachAndQueueBuffer(thumbnailHeader);
        drawSurface.release();
        t.show(this.mSurfaceControl);
        t.setLayer(this.mSurfaceControl, Integer.MAX_VALUE);
        if (relative) {
            t.reparent(this.mSurfaceControl, appToken.getSurfaceControl());
        }
    }

    public void createInnerThumbnailLeash(Rect startRect, SurfaceControl.Transaction t) {
        if (startRect == null) {
            Slog.w(TAG, "createInnerThumbnailLeash failed, startRect is null");
            return;
        }
        SurfaceControl.Builder parent = makeAnimationLeash().setParent(getAnimationLeashParent());
        this.mInnerThumbnailLeash = parent.setName(getSurfaceControl() + " -interim-thumbnail-leash").build();
        Matrix matrix = new Matrix();
        matrix.setTranslate((float) startRect.left, (float) startRect.top);
        matrix.preScale((((float) startRect.width()) * 1.0f) / ((float) this.mWidth), (((float) startRect.height()) * 1.0f) / ((float) this.mHeight));
        Slog.i(TAG, "matrix = " + matrix + ", mInnerThumbnailLeash = " + this.mInnerThumbnailLeash);
        t.setMatrix(this.mInnerThumbnailLeash, matrix, new float[MATRIX_TO_ARRAY_LEN]);
        t.reparent(this.mSurfaceControl, this.mInnerThumbnailLeash);
        t.setLayer(this.mInnerThumbnailLeash, Integer.MAX_VALUE);
        t.show(this.mInnerThumbnailLeash);
    }

    public void startAnimation(SurfaceControl.Transaction t, Animation anim) {
        startAnimation(t, anim, (Point) null);
    }

    public void startAnimation(SurfaceControl.Transaction t, Animation anim, Point position) {
        anim.restrictDuration(10000);
        anim.scaleCurrentDuration(this.mAppToken.mWmService.getTransitionAnimationScaleLocked());
        this.mSurfaceAnimator.startAnimation(t, new LocalAnimationAdapter(new WindowAnimationSpec(anim, position, this.mAppToken.getDisplayContent().mAppTransition.canSkipFirstFrame(), this.mAppToken.getDisplayContent().getWindowCornerRadius()), this.mAppToken.mWmService.mSurfaceAnimationRunner), false);
    }

    public void startAnimation(SurfaceControl.Transaction t, AnimationAdapter anim, boolean hidden) {
        this.mSurfaceAnimator.startAnimation(t, anim, hidden);
    }

    /* access modifiers changed from: public */
    private void onAnimationFinished() {
    }

    public void setShowing(SurfaceControl.Transaction pendingTransaction, boolean show) {
        if (show) {
            pendingTransaction.show(this.mSurfaceControl);
        } else {
            pendingTransaction.hide(this.mSurfaceControl);
        }
    }

    public void destroy() {
        this.mSurfaceAnimator.cancelAnimation();
        getPendingTransaction().remove(this.mSurfaceControl);
        this.mSurfaceControl = null;
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        proto.write(1120986464257L, this.mWidth);
        proto.write(1120986464258L, this.mHeight);
        if (this.mSurfaceAnimator.isAnimating()) {
            this.mSurfaceAnimator.writeToProto(proto, 1146756268035L);
        }
        proto.end(token);
    }

    @Override // com.android.server.wm.SurfaceAnimator.Animatable
    public SurfaceControl.Transaction getPendingTransaction() {
        return this.mAppToken.getPendingTransaction();
    }

    @Override // com.android.server.wm.SurfaceAnimator.Animatable
    public void commitPendingTransaction() {
        this.mAppToken.commitPendingTransaction();
    }

    @Override // com.android.server.wm.SurfaceAnimator.Animatable
    public void onAnimationLeashCreated(SurfaceControl.Transaction t, SurfaceControl leash) {
        t.setLayer(leash, Integer.MAX_VALUE);
        SurfaceControl surfaceControl = this.mInnerThumbnailLeash;
        if (surfaceControl != null) {
            t.remove(surfaceControl);
            this.mInnerThumbnailLeash = null;
        }
        if (this.mRelative) {
            t.reparent(leash, this.mAppToken.getSurfaceControl());
        }
    }

    @Override // com.android.server.wm.SurfaceAnimator.Animatable
    public void onAnimationLeashLost(SurfaceControl.Transaction t) {
        t.hide(this.mSurfaceControl);
    }

    @Override // com.android.server.wm.SurfaceAnimator.Animatable
    public SurfaceControl.Builder makeAnimationLeash() {
        return this.mAppToken.makeSurface();
    }

    @Override // com.android.server.wm.SurfaceAnimator.Animatable
    public SurfaceControl getSurfaceControl() {
        return this.mSurfaceControl;
    }

    @Override // com.android.server.wm.SurfaceAnimator.Animatable
    public SurfaceControl getAnimationLeashParent() {
        return this.mAppToken.getAppAnimationLayer();
    }

    @Override // com.android.server.wm.SurfaceAnimator.Animatable
    public SurfaceControl getParentSurfaceControl() {
        return this.mAppToken.getParentSurfaceControl();
    }

    @Override // com.android.server.wm.SurfaceAnimator.Animatable
    public int getSurfaceWidth() {
        return this.mWidth;
    }

    @Override // com.android.server.wm.SurfaceAnimator.Animatable
    public int getSurfaceHeight() {
        return this.mHeight;
    }
}
