package com.android.server.wm;

import android.graphics.GraphicBuffer;
import android.graphics.Point;
import android.os.Binder;
import android.util.proto.ProtoOutputStream;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.animation.Animation;
import com.android.server.job.controllers.JobStatus;
import com.android.server.os.HwBootFail;
import com.android.server.wm.SurfaceAnimator;

class AppWindowThumbnail implements SurfaceAnimator.Animatable {
    private static final String TAG = "WindowManager";
    private final AppWindowToken mAppToken;
    private final int mHeight;
    private final SurfaceAnimator mSurfaceAnimator;
    private final SurfaceControl mSurfaceControl;
    private final int mWidth;

    AppWindowThumbnail(SurfaceControl.Transaction t, AppWindowToken appToken, GraphicBuffer thumbnailHeader) {
        this.mAppToken = appToken;
        this.mSurfaceAnimator = new SurfaceAnimator(this, new Runnable() {
            public final void run() {
                AppWindowThumbnail.this.onAnimationFinished();
            }
        }, appToken.mService);
        this.mWidth = thumbnailHeader.getWidth();
        this.mHeight = thumbnailHeader.getHeight();
        WindowState window = appToken.findMainWindow();
        SurfaceControl.Builder makeSurface = appToken.makeSurface();
        this.mSurfaceControl = makeSurface.setName("thumbnail anim: " + appToken.toString()).setSize(this.mWidth, this.mHeight).setFormat(-3).setMetadata(appToken.windowType, window != null ? window.mOwnerUid : Binder.getCallingUid()).build();
        Surface drawSurface = new Surface();
        drawSurface.copyFrom(this.mSurfaceControl);
        drawSurface.attachAndQueueBuffer(thumbnailHeader);
        drawSurface.release();
        t.show(this.mSurfaceControl);
        t.setLayer(this.mSurfaceControl, HwBootFail.STAGE_BOOT_SUCCESS);
    }

    /* access modifiers changed from: package-private */
    public void startAnimation(SurfaceControl.Transaction t, Animation anim) {
        startAnimation(t, anim, null);
    }

    /* access modifiers changed from: package-private */
    public void startAnimation(SurfaceControl.Transaction t, Animation anim, Point position) {
        anim.restrictDuration(JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
        anim.scaleCurrentDuration(this.mAppToken.mService.getTransitionAnimationScaleLocked());
        this.mSurfaceAnimator.startAnimation(t, new LocalAnimationAdapter(new WindowAnimationSpec(anim, position, this.mAppToken.mService.mAppTransition.canSkipFirstFrame()), this.mAppToken.mService.mSurfaceAnimationRunner), false);
    }

    /* access modifiers changed from: private */
    public void onAnimationFinished() {
    }

    /* access modifiers changed from: package-private */
    public void setShowing(SurfaceControl.Transaction pendingTransaction, boolean show) {
        if (show) {
            pendingTransaction.show(this.mSurfaceControl);
        } else {
            pendingTransaction.hide(this.mSurfaceControl);
        }
    }

    /* access modifiers changed from: package-private */
    public void destroy() {
        this.mSurfaceAnimator.cancelAnimation();
        this.mSurfaceControl.destroy();
    }

    /* access modifiers changed from: package-private */
    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        proto.write(1120986464257L, this.mWidth);
        proto.write(1120986464258L, this.mHeight);
        this.mSurfaceAnimator.writeToProto(proto, 1146756268035L);
        proto.end(token);
    }

    public SurfaceControl.Transaction getPendingTransaction() {
        return this.mAppToken.getPendingTransaction();
    }

    public void commitPendingTransaction() {
        this.mAppToken.commitPendingTransaction();
    }

    public void onAnimationLeashCreated(SurfaceControl.Transaction t, SurfaceControl leash) {
        t.setLayer(leash, HwBootFail.STAGE_BOOT_SUCCESS);
    }

    public void onAnimationLeashDestroyed(SurfaceControl.Transaction t) {
        t.hide(this.mSurfaceControl);
    }

    public SurfaceControl.Builder makeAnimationLeash() {
        return this.mAppToken.makeSurface();
    }

    public SurfaceControl getSurfaceControl() {
        return this.mSurfaceControl;
    }

    public SurfaceControl getAnimationLeashParent() {
        return this.mAppToken.getAppAnimationLayer();
    }

    public SurfaceControl getParentSurfaceControl() {
        return this.mAppToken.getParentSurfaceControl();
    }

    public int getSurfaceWidth() {
        return this.mWidth;
    }

    public int getSurfaceHeight() {
        return this.mHeight;
    }
}
