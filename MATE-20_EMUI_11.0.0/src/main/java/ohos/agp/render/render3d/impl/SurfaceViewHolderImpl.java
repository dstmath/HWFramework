package ohos.agp.render.render3d.impl;

import java.util.function.Consumer;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.components.Component;
import ohos.agp.components.surfaceprovider.SurfaceProvider;
import ohos.agp.graphics.SurfaceOps;
import ohos.agp.render.render3d.BuildConfig;
import ohos.agp.render.render3d.TargetBuffer;
import ohos.agp.render.render3d.ViewHolder;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

final class SurfaceViewHolderImpl implements ViewHolder {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "core: SurfaceViewHolderImpl");
    private ViewHolder.SurfaceListener mListener;
    private final SurfaceTargetBufferImpl mSurfaceTargetBuffer = new SurfaceTargetBufferImpl();
    private SurfaceProvider mView;

    SurfaceViewHolderImpl(SurfaceProvider surfaceProvider, AgpContextImpl agpContextImpl) {
        this.mView = surfaceProvider;
        surfaceProvider.getSurfaceOps().ifPresent(new Consumer(agpContextImpl) {
            /* class ohos.agp.render.render3d.impl.$$Lambda$SurfaceViewHolderImpl$XKqEQ94Rk7dOOHp3NeDa5QlefW0 */
            private final /* synthetic */ AgpContextImpl f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                SurfaceViewHolderImpl.this.lambda$new$0$SurfaceViewHolderImpl(this.f$1, (SurfaceOps) obj);
            }
        });
        surfaceProvider.setZOrderOnTop(true);
    }

    public /* synthetic */ void lambda$new$0$SurfaceViewHolderImpl(final AgpContextImpl agpContextImpl, SurfaceOps surfaceOps) {
        surfaceOps.addCallback(new SurfaceOps.Callback() {
            /* class ohos.agp.render.render3d.impl.SurfaceViewHolderImpl.AnonymousClass1 */

            @Override // ohos.agp.graphics.SurfaceOps.Callback
            public void surfaceCreated(SurfaceOps surfaceOps) {
                if (BuildConfig.DEBUG) {
                    HiLog.debug(SurfaceViewHolderImpl.LABEL, "surfaceCreated()", new Object[0]);
                }
                SurfaceViewHolderImpl.this.mSurfaceTargetBuffer.init(agpContextImpl, surfaceOps.getSurface());
                if (SurfaceViewHolderImpl.this.mListener != null) {
                    SurfaceViewHolderImpl.this.mListener.onSurfaceAvailable();
                }
            }

            @Override // ohos.agp.graphics.SurfaceOps.Callback
            public void surfaceChanged(SurfaceOps surfaceOps, int i, int i2, int i3) {
                if (BuildConfig.DEBUG) {
                    HiLog.debug(SurfaceViewHolderImpl.LABEL, "surfaceChanged(%{public}d x %{public}d)", new Object[]{Integer.valueOf(i2), Integer.valueOf(i3)});
                }
                SurfaceViewHolderImpl.this.mSurfaceTargetBuffer.updateSize(i2, i3);
                if (SurfaceViewHolderImpl.this.mListener != null) {
                    SurfaceViewHolderImpl.this.mListener.onSurfaceSizeUpdated(i2, i3);
                }
            }

            @Override // ohos.agp.graphics.SurfaceOps.Callback
            public void surfaceDestroyed(SurfaceOps surfaceOps) {
                if (BuildConfig.DEBUG) {
                    HiLog.debug(SurfaceViewHolderImpl.LABEL, "surfaceDestroyed()", new Object[0]);
                }
                SurfaceViewHolderImpl.this.mSurfaceTargetBuffer.release();
                if (SurfaceViewHolderImpl.this.mListener != null) {
                    SurfaceViewHolderImpl.this.mListener.onSurfaceDestroyed();
                }
            }
        });
    }

    @Override // ohos.agp.render.render3d.ViewHolder
    public Component getView() {
        return this.mView;
    }

    @Override // ohos.agp.render.render3d.ViewHolder
    public void release() {
        this.mSurfaceTargetBuffer.release();
        ViewHolder.SurfaceListener surfaceListener = this.mListener;
        if (surfaceListener != null) {
            surfaceListener.onSurfaceDestroyed();
        }
        this.mListener = null;
        this.mView = null;
    }

    @Override // ohos.agp.render.render3d.ViewHolder
    public TargetBuffer getTargetBuffer() {
        return this.mSurfaceTargetBuffer;
    }

    @Override // ohos.agp.render.render3d.ViewHolder
    public void setOpaque(boolean z) {
        if (z) {
            this.mView.getSurfaceOps().ifPresent($$Lambda$SurfaceViewHolderImpl$larPCZvgfb2r4KvBUKqPCeOriDo.INSTANCE);
        } else {
            this.mView.getSurfaceOps().ifPresent($$Lambda$SurfaceViewHolderImpl$SITS8zqnx61fPvLhhr0j9sbDDMY.INSTANCE);
        }
    }

    @Override // ohos.agp.render.render3d.ViewHolder
    public void setSurfaceListener(ViewHolder.SurfaceListener surfaceListener) {
        this.mListener = surfaceListener;
    }
}
