package com.android.server.wm;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.view.Display;
import android.view.Surface;
import android.view.Surface.OutOfResourcesException;
import android.view.SurfaceControl;
import android.view.SurfaceSession;

class StrictModeFlash {
    private static final String TAG = "WindowManager";
    private boolean mDrawNeeded;
    private int mLastDH;
    private int mLastDW;
    private final Surface mSurface = new Surface();
    private final SurfaceControl mSurfaceControl;
    private final int mThickness = 20;

    public StrictModeFlash(Display display, SurfaceSession session) {
        SurfaceControl ctrl;
        try {
            ctrl = new SurfaceControl(session, "StrictModeFlash", 1, 1, -3, 4);
            try {
                ctrl.setLayerStack(display.getLayerStack());
                ctrl.setLayer(1010000);
                ctrl.setPosition(0.0f, 0.0f);
                ctrl.show();
                this.mSurface.copyFrom(ctrl);
            } catch (OutOfResourcesException e) {
            }
        } catch (OutOfResourcesException e2) {
            ctrl = null;
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
            } catch (IllegalArgumentException e) {
            } catch (OutOfResourcesException e2) {
            }
            if (c != null) {
                c.clipRect(new Rect(0, 0, dw, 20), Op.REPLACE);
                c.drawColor(-65536);
                c.clipRect(new Rect(0, 0, 20, dh), Op.REPLACE);
                c.drawColor(-65536);
                c.clipRect(new Rect(dw - 20, 0, dw, dh), Op.REPLACE);
                c.drawColor(-65536);
                c.clipRect(new Rect(0, dh - 20, dw, dh), Op.REPLACE);
                c.drawColor(-65536);
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

    void positionSurface(int dw, int dh) {
        if (this.mLastDW != dw || this.mLastDH != dh) {
            this.mLastDW = dw;
            this.mLastDH = dh;
            this.mSurfaceControl.setSize(dw, dh);
            this.mDrawNeeded = true;
        }
    }
}
