package com.android.server.wm;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.Surface;
import android.view.SurfaceControl;

class StrictModeFlash {
    private static final String TAG = "WindowManager";
    private boolean mDrawNeeded;
    private int mLastDH;
    private int mLastDW;
    private final Surface mSurface = new Surface();
    private final SurfaceControl mSurfaceControl;
    private final int mThickness = 20;

    public StrictModeFlash(DisplayContent dc) {
        SurfaceControl ctrl = null;
        try {
            ctrl = dc.makeOverlay().setName("StrictModeFlash").setSize(1, 1).setFormat(-3).build();
            ctrl.setLayer(1010000);
            ctrl.setPosition(0.0f, 0.0f);
            ctrl.show();
            this.mSurface.copyFrom(ctrl);
        } catch (Surface.OutOfResourcesException e) {
        }
        this.mSurfaceControl = ctrl;
        this.mDrawNeeded = true;
    }

    private void drawIfNeeded() {
        if (this.mDrawNeeded) {
            this.mDrawNeeded = false;
            int dw = this.mLastDW;
            int dh = this.mLastDH;
            Canvas c = null;
            try {
                c = this.mSurface.lockCanvas(new Rect(0, 0, dw, dh));
            } catch (Surface.OutOfResourcesException | IllegalArgumentException e) {
            }
            if (c != null) {
                c.save();
                c.clipRect(new Rect(0, 0, dw, 20));
                c.drawColor(-65536);
                c.restore();
                c.save();
                c.clipRect(new Rect(0, 0, 20, dh));
                c.drawColor(-65536);
                c.restore();
                c.save();
                c.clipRect(new Rect(dw - 20, 0, dw, dh));
                c.drawColor(-65536);
                c.restore();
                c.save();
                c.clipRect(new Rect(0, dh - 20, dw, dh));
                c.drawColor(-65536);
                c.restore();
                this.mSurface.unlockCanvasAndPost(c);
            }
        }
    }

    public void setVisibility(boolean on) {
        if (this.mSurfaceControl != null) {
            drawIfNeeded();
            if (on) {
                this.mSurfaceControl.show();
            } else {
                this.mSurfaceControl.hide();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void positionSurface(int dw, int dh) {
        if (this.mLastDW != dw || this.mLastDH != dh) {
            this.mLastDW = dw;
            this.mLastDH = dh;
            this.mSurfaceControl.setSize(dw, dh);
            this.mDrawNeeded = true;
        }
    }
}
