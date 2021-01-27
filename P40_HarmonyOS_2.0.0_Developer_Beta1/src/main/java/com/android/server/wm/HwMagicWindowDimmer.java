package com.android.server.wm;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Flog;
import android.view.Surface;
import android.view.SurfaceControl;

public class HwMagicWindowDimmer extends Dimmer {
    private static final float RADIUS_OFFSET = 11.0f;
    private Surface mSurface;
    private SurfaceControl mSurfaceControl;

    public HwMagicWindowDimmer(WindowState host) {
        super(host.getParent());
    }

    /* access modifiers changed from: package-private */
    public boolean updateDims(SurfaceControl.Transaction transaction, Rect bounds) {
        if (this.mDimState == null) {
            return false;
        }
        if (!this.mDimState.mDimming) {
            Surface surface = this.mSurface;
            if (surface != null) {
                surface.destroy();
                this.mSurface = null;
            }
        } else {
            Canvas canvas = null;
            try {
                if (this.mSurfaceControl != null) {
                    this.mSurfaceControl.setBufferSize(bounds.width(), bounds.height());
                }
                canvas = this.mSurface.lockCanvas(bounds);
            } catch (IllegalArgumentException e) {
                Flog.i(310, "HwDimmer lockCanvas Exception: " + e);
            } catch (Surface.OutOfResourcesException e2) {
                Flog.i(310, "HwDimmer lockCanvas Exception: " + e2);
            }
            if (canvas == null) {
                return false;
            }
            Paint paint = new Paint(5);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(-16777216);
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            canvas.drawRoundRect(new RectF(bounds), RADIUS_OFFSET, RADIUS_OFFSET, paint);
            this.mSurface.unlockCanvasAndPost(canvas);
        }
        return HwMagicWindowDimmer.super.updateDims(transaction, bounds);
    }

    /* access modifiers changed from: package-private */
    public SurfaceControl makeDimLayer() {
        SurfaceControl.Builder bufferSize = this.mHost.makeChildSurface((WindowContainer) null).setParent(this.mHost.getSurfaceControl()).setFormat(-3).setBufferSize(1, 1);
        this.mSurfaceControl = bufferSize.setName("Dim Layer for - " + this.mHost.getName()).build();
        this.mSurface = new Surface();
        this.mSurface.copyFrom(this.mSurfaceControl);
        return this.mSurfaceControl;
    }

    /* access modifiers changed from: package-private */
    public void destroyDimmer(SurfaceControl.Transaction transaction) {
        if (this.mDimState != null) {
            Surface surface = this.mSurface;
            if (surface != null) {
                surface.destroy();
                this.mSurface = null;
            }
            if (this.mDimState.mDimLayer.isValid()) {
                transaction.remove(this.mDimState.mDimLayer);
            }
            this.mDimState.mSurfaceAnimator = null;
            this.mDimState = null;
        }
    }
}
