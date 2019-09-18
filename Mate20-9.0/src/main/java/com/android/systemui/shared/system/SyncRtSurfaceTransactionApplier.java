package com.android.systemui.shared.system;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.ThreadedRenderer;
import android.view.View;
import android.view.ViewRootImpl;
import com.android.systemui.shared.system.SyncRtSurfaceTransactionApplier;

public class SyncRtSurfaceTransactionApplier {
    private final Surface mTargetSurface;
    private final ViewRootImpl mTargetViewRootImpl;
    private final float[] mTmpFloat9 = new float[9];

    public static class SurfaceParams {
        final float alpha;
        final int layer;
        final Matrix matrix;
        final SurfaceControl surface;
        final Rect windowCrop;

        public SurfaceParams(SurfaceControlCompat surface2, float alpha2, Matrix matrix2, Rect windowCrop2, int layer2) {
            this.surface = surface2.mSurfaceControl;
            this.alpha = alpha2;
            this.matrix = new Matrix(matrix2);
            this.windowCrop = new Rect(windowCrop2);
            this.layer = layer2;
        }
    }

    public SyncRtSurfaceTransactionApplier(View targetView) {
        Surface surface = null;
        this.mTargetViewRootImpl = targetView != null ? targetView.getViewRootImpl() : null;
        this.mTargetSurface = this.mTargetViewRootImpl != null ? this.mTargetViewRootImpl.mSurface : surface;
    }

    public void scheduleApply(SurfaceParams... params) {
        if (this.mTargetViewRootImpl != null) {
            this.mTargetViewRootImpl.registerRtFrameCallback(new ThreadedRenderer.FrameDrawingCallback(params) {
                private final /* synthetic */ SyncRtSurfaceTransactionApplier.SurfaceParams[] f$1;

                {
                    this.f$1 = r2;
                }

                public final void onFrameDraw(long j) {
                    SyncRtSurfaceTransactionApplier.lambda$scheduleApply$0(SyncRtSurfaceTransactionApplier.this, this.f$1, j);
                }
            });
            this.mTargetViewRootImpl.getView().invalidate();
        }
    }

    public static /* synthetic */ void lambda$scheduleApply$0(SyncRtSurfaceTransactionApplier syncRtSurfaceTransactionApplier, SurfaceParams[] params, long frame) {
        if (syncRtSurfaceTransactionApplier.mTargetSurface != null && syncRtSurfaceTransactionApplier.mTargetSurface.isValid()) {
            SurfaceControl.Transaction t = new SurfaceControl.Transaction();
            for (int i = params.length - 1; i >= 0; i--) {
                SurfaceParams surfaceParams = params[i];
                t.deferTransactionUntilSurface(surfaceParams.surface, syncRtSurfaceTransactionApplier.mTargetSurface, frame);
                applyParams(t, surfaceParams, syncRtSurfaceTransactionApplier.mTmpFloat9);
            }
            t.setEarlyWakeup();
            t.apply();
        }
    }

    public static void applyParams(TransactionCompat t, SurfaceParams params) {
        applyParams(t.mTransaction, params, t.mTmpValues);
    }

    private static void applyParams(SurfaceControl.Transaction t, SurfaceParams params, float[] tmpFloat9) {
        t.setMatrix(params.surface, params.matrix, tmpFloat9);
        t.setWindowCrop(params.surface, params.windowCrop);
        t.setAlpha(params.surface, params.alpha);
        t.setLayer(params.surface, params.layer);
        t.show(params.surface);
    }
}
