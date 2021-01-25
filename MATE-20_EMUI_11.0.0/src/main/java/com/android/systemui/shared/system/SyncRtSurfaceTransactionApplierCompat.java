package com.android.systemui.shared.system;

import android.graphics.HardwareRenderer;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.view.Surface;
import android.view.View;
import android.view.ViewRootImpl;
import java.util.function.Consumer;

public class SyncRtSurfaceTransactionApplierCompat {
    private static final int MSG_UPDATE_SEQUENCE_NUMBER = 0;
    private Runnable mAfterApplyCallback;
    private final Handler mApplyHandler;
    private int mPendingSequenceNumber = 0;
    private int mSequenceNumber = 0;
    private final Surface mTargetSurface;
    private final ViewRootImpl mTargetViewRootImpl;

    public SyncRtSurfaceTransactionApplierCompat(View targetView) {
        Surface surface = null;
        this.mTargetViewRootImpl = targetView != null ? targetView.getViewRootImpl() : null;
        ViewRootImpl viewRootImpl = this.mTargetViewRootImpl;
        this.mTargetSurface = viewRootImpl != null ? viewRootImpl.mSurface : surface;
        this.mApplyHandler = new Handler(new Handler.Callback() {
            /* class com.android.systemui.shared.system.SyncRtSurfaceTransactionApplierCompat.AnonymousClass1 */

            @Override // android.os.Handler.Callback
            public boolean handleMessage(Message msg) {
                if (msg.what != 0) {
                    return false;
                }
                SyncRtSurfaceTransactionApplierCompat.this.onApplyMessage(msg.arg1);
                return true;
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onApplyMessage(int seqNo) {
        this.mSequenceNumber = seqNo;
        if (this.mSequenceNumber == this.mPendingSequenceNumber && this.mAfterApplyCallback != null) {
            Runnable r = this.mAfterApplyCallback;
            this.mAfterApplyCallback = null;
            r.run();
        }
    }

    public void scheduleApply(final SurfaceParams... params) {
        ViewRootImpl viewRootImpl = this.mTargetViewRootImpl;
        if (viewRootImpl != null && viewRootImpl.getView() != null) {
            this.mPendingSequenceNumber++;
            final int toApplySeqNo = this.mPendingSequenceNumber;
            this.mTargetViewRootImpl.registerRtFrameCallback(new HardwareRenderer.FrameDrawingCallback() {
                /* class com.android.systemui.shared.system.SyncRtSurfaceTransactionApplierCompat.AnonymousClass2 */

                public void onFrameDraw(long frame) {
                    if (SyncRtSurfaceTransactionApplierCompat.this.mTargetSurface == null || !SyncRtSurfaceTransactionApplierCompat.this.mTargetSurface.isValid()) {
                        Message.obtain(SyncRtSurfaceTransactionApplierCompat.this.mApplyHandler, 0, toApplySeqNo, 0).sendToTarget();
                        return;
                    }
                    TransactionCompat t = new TransactionCompat();
                    for (int i = params.length - 1; i >= 0; i--) {
                        SurfaceParams surfaceParams = params[i];
                        t.deferTransactionUntil(surfaceParams.surface, SyncRtSurfaceTransactionApplierCompat.this.mTargetSurface, frame);
                        SyncRtSurfaceTransactionApplierCompat.applyParams(t, surfaceParams);
                    }
                    t.setEarlyWakeup();
                    t.apply();
                    Message.obtain(SyncRtSurfaceTransactionApplierCompat.this.mApplyHandler, 0, toApplySeqNo, 0).sendToTarget();
                }
            });
            this.mTargetViewRootImpl.getView().invalidate();
        }
    }

    public void addAfterApplyCallback(final Runnable afterApplyCallback) {
        if (this.mSequenceNumber == this.mPendingSequenceNumber) {
            afterApplyCallback.run();
        } else if (this.mAfterApplyCallback == null) {
            this.mAfterApplyCallback = afterApplyCallback;
        } else {
            final Runnable oldCallback = this.mAfterApplyCallback;
            this.mAfterApplyCallback = new Runnable() {
                /* class com.android.systemui.shared.system.SyncRtSurfaceTransactionApplierCompat.AnonymousClass3 */

                @Override // java.lang.Runnable
                public void run() {
                    afterApplyCallback.run();
                    oldCallback.run();
                }
            };
        }
    }

    public static void applyParams(TransactionCompat t, SurfaceParams params) {
        t.setMatrix(params.surface, params.matrix);
        t.setWindowCrop(params.surface, params.windowCrop);
        t.setAlpha(params.surface, params.alpha);
        t.setLayer(params.surface, params.layer);
        t.setCornerRadius(params.surface, params.cornerRadius);
        t.show(params.surface);
    }

    public static void create(final View targetView, final Consumer<SyncRtSurfaceTransactionApplierCompat> callback) {
        if (targetView == null) {
            callback.accept(null);
        } else if (targetView.getViewRootImpl() != null) {
            callback.accept(new SyncRtSurfaceTransactionApplierCompat(targetView));
        } else {
            targetView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                /* class com.android.systemui.shared.system.SyncRtSurfaceTransactionApplierCompat.AnonymousClass4 */

                @Override // android.view.View.OnAttachStateChangeListener
                public void onViewAttachedToWindow(View v) {
                    targetView.removeOnAttachStateChangeListener(this);
                    callback.accept(new SyncRtSurfaceTransactionApplierCompat(targetView));
                }

                @Override // android.view.View.OnAttachStateChangeListener
                public void onViewDetachedFromWindow(View v) {
                }
            });
        }
    }

    public static class SurfaceParams {
        public final float alpha;
        final float cornerRadius;
        public final int layer;
        public final Matrix matrix;
        public final SurfaceControlCompat surface;
        public final Rect windowCrop;

        public SurfaceParams(SurfaceControlCompat surface2, float alpha2, Matrix matrix2, Rect windowCrop2, int layer2, float cornerRadius2) {
            this.surface = surface2;
            this.alpha = alpha2;
            this.matrix = new Matrix(matrix2);
            this.windowCrop = new Rect(windowCrop2);
            this.layer = layer2;
            this.cornerRadius = cornerRadius2;
        }
    }
}
