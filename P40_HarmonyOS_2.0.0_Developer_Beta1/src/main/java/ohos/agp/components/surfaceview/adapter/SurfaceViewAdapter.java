package ohos.agp.components.surfaceview.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import java.util.ArrayList;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.graphics.Surface;
import ohos.agp.graphics.SurfaceOps;
import ohos.agp.render.Canvas;
import ohos.agp.utils.Rect;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class SurfaceViewAdapter {
    private static final Object LOCK = new Object();
    private static final HiLogLabel LOG_TAG = new HiLogLabel(3, (int) LogDomain.END, "SurfaceViewAdapter");
    private boolean mAOSPCallbackAdded = false;
    private Context mAOSPContext;
    private Surface mSurface;
    private final SurfaceCallback mSurfaceCallback = new SurfaceCallback();
    private final SurfaceOps mSurfaceOps = new SurfaceOps() {
        /* class ohos.agp.components.surfaceview.adapter.SurfaceViewAdapter.AnonymousClass1 */

        @Override // ohos.agp.graphics.SurfaceOps
        public void unlockCanvasAndPost(Canvas canvas) {
        }

        @Override // ohos.agp.graphics.SurfaceOps
        public void addCallback(SurfaceOps.Callback callback) {
            synchronized (SurfaceViewAdapter.LOCK) {
                if (!SurfaceViewAdapter.this.mSurfaceOpsCallbacks.contains(callback)) {
                    SurfaceViewAdapter.this.mSurfaceOpsCallbacks.add(callback);
                }
            }
            SurfaceViewAdapter.this.addAOSPCallback();
        }

        @Override // ohos.agp.graphics.SurfaceOps
        public void removeCallback(SurfaceOps.Callback callback) {
            synchronized (SurfaceViewAdapter.LOCK) {
                SurfaceViewAdapter.this.mSurfaceOpsCallbacks.remove(callback);
            }
        }

        @Override // ohos.agp.graphics.SurfaceOps
        public void setFormat(int i) {
            SurfaceViewAdapter.this.mSurfaceView.getHolder().setFormat(i);
        }

        @Override // ohos.agp.graphics.SurfaceOps
        public Canvas lockCanvas() {
            return new Canvas();
        }

        @Override // ohos.agp.graphics.SurfaceOps
        public Surface getSurface() {
            return SurfaceViewAdapter.this.mSurface;
        }

        @Override // ohos.agp.graphics.SurfaceOps
        public void setFixedSize(int i, int i2) {
            SurfaceViewAdapter.this.mSurfaceView.getHolder().setFixedSize(i, i2);
        }

        @Override // ohos.agp.graphics.SurfaceOps
        public void setKeepScreenOn(boolean z) {
            SurfaceViewAdapter.this.mSurfaceView.getHolder().setKeepScreenOn(z);
        }

        @Override // ohos.agp.graphics.SurfaceOps
        public Rect getSurfaceDimension() {
            android.graphics.Rect surfaceFrame = SurfaceViewAdapter.this.mSurfaceView.getHolder().getSurfaceFrame();
            return new Rect(0, 0, surfaceFrame.right, surfaceFrame.bottom);
        }
    };
    private final ArrayList<SurfaceOps.Callback> mSurfaceOpsCallbacks = new ArrayList<>();
    private SurfaceView mSurfaceView;

    public SurfaceViewAdapter(ohos.app.Context context) {
        Object hostContext = context.getHostContext();
        if (hostContext instanceof Context) {
            this.mAOSPContext = (Context) hostContext;
            this.mSurfaceView = new SurfaceView(this.mAOSPContext);
            pinToZTop(false);
            return;
        }
        HiLog.error(LOG_TAG, "SurfaceViewAdapter abilityShell is null", new Object[0]);
    }

    public SurfaceViewAdapter(Context context) {
        this.mAOSPContext = context;
        this.mSurfaceView = new SurfaceView(this.mAOSPContext);
        pinToZTop(false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addAOSPCallback() {
        if (!this.mAOSPCallbackAdded) {
            this.mSurfaceView.getHolder().addCallback(this.mSurfaceCallback);
            this.mAOSPCallbackAdded = true;
        }
    }

    public void pinToZTop(boolean z) {
        this.mSurfaceView.setZOrderOnTop(z);
    }

    public void setWidth(int i) {
        ViewGroup.LayoutParams layoutParams = this.mSurfaceView.getLayoutParams();
        if (layoutParams != null) {
            layoutParams.width = i;
        } else {
            layoutParams = new ViewGroup.LayoutParams(i, -1);
        }
        this.mSurfaceView.setLayoutParams(layoutParams);
    }

    public void setHeight(int i) {
        ViewGroup.LayoutParams layoutParams = this.mSurfaceView.getLayoutParams();
        if (layoutParams != null) {
            layoutParams.height = i;
        } else {
            layoutParams = new ViewGroup.LayoutParams(-1, i);
        }
        this.mSurfaceView.setLayoutParams(layoutParams);
    }

    public void setPosition(float f, float f2) {
        this.mSurfaceView.setX(f);
        this.mSurfaceView.setY(f2);
    }

    public void addToWindow() {
        Context context = this.mAOSPContext;
        if (context == null) {
            HiLog.error(LOG_TAG, "SurfaceViewAdapter mAOSPContext is null", new Object[0]);
        } else if (context instanceof Activity) {
            View findViewById = ((Activity) context).getWindow().getDecorView().findViewById(16908290);
            if (findViewById instanceof FrameLayout) {
                ((FrameLayout) findViewById).addView(this.mSurfaceView);
            }
        }
    }

    public void removeFromWindow() {
        Context context = this.mAOSPContext;
        if (context == null) {
            HiLog.error(LOG_TAG, "SurfaceViewAdapter mAOSPContext is null", new Object[0]);
        } else if (context instanceof Activity) {
            View findViewById = ((Activity) context).getWindow().getDecorView().findViewById(16908290);
            if (findViewById instanceof FrameLayout) {
                ((FrameLayout) findViewById).removeView(this.mSurfaceView);
            }
        }
    }

    public SurfaceOps getSurfaceOps() {
        return this.mSurfaceOps;
    }

    /* access modifiers changed from: private */
    public class SurfaceCallback implements SurfaceHolder.Callback {
        private SurfaceCallback() {
        }

        @Override // android.view.SurfaceHolder.Callback
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            SurfaceViewAdapter.this.mSurface = new Surface();
            SurfaceUtils.setSurfaceImpl(SurfaceViewAdapter.this.mSurface, surfaceHolder.getSurface());
            for (int i = 0; i < SurfaceViewAdapter.this.mSurfaceOpsCallbacks.size(); i++) {
                ((SurfaceOps.Callback) SurfaceViewAdapter.this.mSurfaceOpsCallbacks.get(i)).surfaceCreated(SurfaceViewAdapter.this.mSurfaceOps);
            }
        }

        @Override // android.view.SurfaceHolder.Callback
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
            for (int i4 = 0; i4 < SurfaceViewAdapter.this.mSurfaceOpsCallbacks.size(); i4++) {
                ((SurfaceOps.Callback) SurfaceViewAdapter.this.mSurfaceOpsCallbacks.get(i4)).surfaceChanged(SurfaceViewAdapter.this.mSurfaceOps, i, i2, i3);
            }
        }

        @Override // android.view.SurfaceHolder.Callback
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            for (int i = 0; i < SurfaceViewAdapter.this.mSurfaceOpsCallbacks.size(); i++) {
                ((SurfaceOps.Callback) SurfaceViewAdapter.this.mSurfaceOpsCallbacks.get(i)).surfaceDestroyed(SurfaceViewAdapter.this.mSurfaceOps);
            }
        }
    }
}
