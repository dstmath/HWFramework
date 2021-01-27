package android.view;

import android.graphics.HardwareRenderer;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.view.SurfaceControl;
import android.view.SyncRtSurfaceTransactionApplier;
import android.view.View;
import com.android.internal.annotations.VisibleForTesting;
import java.util.function.Consumer;

public class SyncRtSurfaceTransactionApplier {
    private final Surface mTargetSurface;
    private final ViewRootImpl mTargetViewRootImpl;
    private final float[] mTmpFloat9 = new float[9];

    public SyncRtSurfaceTransactionApplier(View targetView) {
        Surface surface = null;
        this.mTargetViewRootImpl = targetView != null ? targetView.getViewRootImpl() : null;
        ViewRootImpl viewRootImpl = this.mTargetViewRootImpl;
        this.mTargetSurface = viewRootImpl != null ? viewRootImpl.mSurface : surface;
    }

    public void scheduleApply(SurfaceParams... params) {
        ViewRootImpl viewRootImpl = this.mTargetViewRootImpl;
        if (viewRootImpl != null) {
            viewRootImpl.registerRtFrameCallback(new HardwareRenderer.FrameDrawingCallback(params) {
                /* class android.view.$$Lambda$SyncRtSurfaceTransactionApplier$ttntIVYYZl7t890CcQHVoB3U1nQ */
                private final /* synthetic */ SyncRtSurfaceTransactionApplier.SurfaceParams[] f$1;

                {
                    this.f$1 = r2;
                }

                @Override // android.graphics.HardwareRenderer.FrameDrawingCallback
                public final void onFrameDraw(long j) {
                    SyncRtSurfaceTransactionApplier.this.lambda$scheduleApply$0$SyncRtSurfaceTransactionApplier(this.f$1, j);
                }
            });
            this.mTargetViewRootImpl.getView().invalidate();
        }
    }

    public /* synthetic */ void lambda$scheduleApply$0$SyncRtSurfaceTransactionApplier(SurfaceParams[] params, long frame) {
        Surface surface = this.mTargetSurface;
        if (surface != null && surface.isValid()) {
            SurfaceControl.Transaction t = new SurfaceControl.Transaction();
            for (int i = params.length - 1; i >= 0; i--) {
                SurfaceParams surfaceParams = params[i];
                t.deferTransactionUntilSurface(surfaceParams.surface, this.mTargetSurface, frame);
                applyParams(t, surfaceParams, this.mTmpFloat9);
            }
            t.setEarlyWakeup();
            t.apply();
        }
    }

    public static void applyParams(SurfaceControl.Transaction t, SurfaceParams params, float[] tmpFloat9) {
        t.setMatrix(params.surface, params.matrix, tmpFloat9);
        t.setWindowCrop(params.surface, params.windowCrop);
        t.setAlpha(params.surface, params.alpha);
        t.setLayer(params.surface, params.layer);
        t.setCornerRadius(params.surface, params.cornerRadius);
        if (params.visible) {
            t.show(params.surface);
        } else {
            t.hide(params.surface);
        }
    }

    public static void create(final View targetView, final Consumer<SyncRtSurfaceTransactionApplier> callback) {
        if (targetView == null) {
            callback.accept(null);
        } else if (targetView.getViewRootImpl() != null) {
            callback.accept(new SyncRtSurfaceTransactionApplier(targetView));
        } else {
            targetView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                /* class android.view.SyncRtSurfaceTransactionApplier.AnonymousClass1 */

                @Override // android.view.View.OnAttachStateChangeListener
                public void onViewAttachedToWindow(View v) {
                    View.this.removeOnAttachStateChangeListener(this);
                    callback.accept(new SyncRtSurfaceTransactionApplier(View.this));
                }

                @Override // android.view.View.OnAttachStateChangeListener
                public void onViewDetachedFromWindow(View v) {
                }
            });
        }
    }

    public static class SurfaceParams {
        @VisibleForTesting
        public final float alpha;
        @VisibleForTesting
        final float cornerRadius;
        @VisibleForTesting
        public final int layer;
        @VisibleForTesting
        public final Matrix matrix;
        @VisibleForTesting
        public final SurfaceControl surface;
        public final boolean visible;
        @VisibleForTesting
        public final Rect windowCrop;

        public SurfaceParams(SurfaceControl surface2, float alpha2, Matrix matrix2, Rect windowCrop2, int layer2, float cornerRadius2, boolean visible2) {
            this.surface = surface2;
            this.alpha = alpha2;
            this.matrix = new Matrix(matrix2);
            this.windowCrop = new Rect(windowCrop2);
            this.layer = layer2;
            this.cornerRadius = cornerRadius2;
            this.visible = visible2;
        }
    }
}
