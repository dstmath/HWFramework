package com.android.server.wm;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.Display;
import android.view.Surface;
import android.view.Surface.OutOfResourcesException;
import android.view.SurfaceControl;
import android.view.SurfaceSession;

class EmulatorDisplayOverlay {
    private static final String TAG = "WindowManager";
    private boolean mDrawNeeded;
    private int mLastDH;
    private int mLastDW;
    private Drawable mOverlay;
    private int mRotation;
    private Point mScreenSize = new Point();
    private final Surface mSurface = new Surface();
    private final SurfaceControl mSurfaceControl;
    private boolean mVisible;

    public EmulatorDisplayOverlay(Context context, Display display, SurfaceSession session, int zOrder) {
        SurfaceControl ctrl;
        display.getSize(this.mScreenSize);
        try {
            SurfaceSession surfaceSession = session;
            ctrl = new SurfaceControl(surfaceSession, "EmulatorDisplayOverlay", this.mScreenSize.x, this.mScreenSize.y, -3, 4);
            try {
                ctrl.setLayerStack(display.getLayerStack());
                ctrl.setLayer(zOrder);
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
        this.mOverlay = context.getDrawable(17302196);
    }

    private void drawIfNeeded() {
        if (this.mDrawNeeded && (this.mVisible ^ 1) == 0) {
            this.mDrawNeeded = false;
            Canvas c = null;
            try {
                c = this.mSurface.lockCanvas(new Rect(0, 0, this.mScreenSize.x, this.mScreenSize.y));
            } catch (IllegalArgumentException e) {
            } catch (OutOfResourcesException e2) {
            }
            if (c != null) {
                c.drawColor(0, Mode.SRC);
                this.mSurfaceControl.setPosition(0.0f, 0.0f);
                int size = Math.max(this.mScreenSize.x, this.mScreenSize.y);
                this.mOverlay.setBounds(0, 0, size, size);
                this.mOverlay.draw(c);
                this.mSurface.unlockCanvasAndPost(c);
            }
        }
    }

    public void setVisibility(boolean on) {
        if (this.mSurfaceControl != null) {
            this.mVisible = on;
            drawIfNeeded();
            if (on) {
                this.mSurfaceControl.show();
            } else {
                this.mSurfaceControl.hide();
            }
        }
    }

    void positionSurface(int dw, int dh, int rotation) {
        if (this.mLastDW != dw || this.mLastDH != dh || this.mRotation != rotation) {
            this.mLastDW = dw;
            this.mLastDH = dh;
            this.mDrawNeeded = true;
            this.mRotation = rotation;
            drawIfNeeded();
        }
    }
}
