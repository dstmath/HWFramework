package com.android.server.wm;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.Slog;
import android.view.Display;
import android.view.Surface;
import android.view.Surface.OutOfResourcesException;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import com.android.server.usb.UsbAudioDevice;

class CircularDisplayMask {
    private static final String TAG = "WindowManager";
    private boolean mDimensionsUnequal = false;
    private boolean mDrawNeeded;
    private int mLastDH;
    private int mLastDW;
    private int mMaskThickness;
    private Paint mPaint;
    private int mRotation;
    private int mScreenOffset = 0;
    private Point mScreenSize = new Point();
    private final Surface mSurface = new Surface();
    private final SurfaceControl mSurfaceControl;
    private boolean mVisible;

    public CircularDisplayMask(Display display, SurfaceSession session, int zOrder, int screenOffset, int maskThickness) {
        SurfaceControl ctrl;
        display.getSize(this.mScreenSize);
        if (this.mScreenSize.x != this.mScreenSize.y + screenOffset) {
            Slog.w(TAG, "Screen dimensions of displayId = " + display.getDisplayId() + "are not equal, circularMask will not be drawn.");
            this.mDimensionsUnequal = true;
        }
        try {
            SurfaceSession surfaceSession = session;
            ctrl = new SurfaceControl(surfaceSession, "CircularDisplayMask", this.mScreenSize.x, this.mScreenSize.y, -3, 4);
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
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
        this.mScreenOffset = screenOffset;
        this.mMaskThickness = maskThickness;
    }

    private void drawIfNeeded() {
        if (this.mDrawNeeded && (this.mVisible ^ 1) == 0 && !this.mDimensionsUnequal) {
            this.mDrawNeeded = false;
            Canvas c = null;
            try {
                c = this.mSurface.lockCanvas(new Rect(0, 0, this.mScreenSize.x, this.mScreenSize.y));
            } catch (IllegalArgumentException e) {
            } catch (OutOfResourcesException e2) {
            }
            if (c != null) {
                switch (this.mRotation) {
                    case 0:
                    case 1:
                        this.mSurfaceControl.setPosition(0.0f, 0.0f);
                        break;
                    case 2:
                        this.mSurfaceControl.setPosition(0.0f, (float) (-this.mScreenOffset));
                        break;
                    case 3:
                        this.mSurfaceControl.setPosition((float) (-this.mScreenOffset), 0.0f);
                        break;
                }
                int circleRadius = this.mScreenSize.x / 2;
                c.drawColor(UsbAudioDevice.kAudioDeviceMetaMask);
                c.drawCircle((float) circleRadius, (float) circleRadius, (float) (circleRadius - this.mMaskThickness), this.mPaint);
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
