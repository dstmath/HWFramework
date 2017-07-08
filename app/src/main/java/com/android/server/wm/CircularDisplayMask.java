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
import com.android.server.wm.WindowManagerService.H;

class CircularDisplayMask {
    private static final String TAG = null;
    private boolean mDimensionsUnequal;
    private boolean mDrawNeeded;
    private int mLastDH;
    private int mLastDW;
    private int mMaskThickness;
    private Paint mPaint;
    private int mRotation;
    private int mScreenOffset;
    private Point mScreenSize;
    private final Surface mSurface;
    private final SurfaceControl mSurfaceControl;
    private boolean mVisible;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wm.CircularDisplayMask.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wm.CircularDisplayMask.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.CircularDisplayMask.<clinit>():void");
    }

    public CircularDisplayMask(Display display, SurfaceSession session, int zOrder, int screenOffset, int maskThickness) {
        SurfaceControl ctrl;
        this.mScreenOffset = 0;
        this.mSurface = new Surface();
        this.mDimensionsUnequal = false;
        this.mScreenSize = new Point();
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
        if (this.mDrawNeeded && this.mVisible && !this.mDimensionsUnequal) {
            this.mDrawNeeded = false;
            Canvas c = null;
            try {
                c = this.mSurface.lockCanvas(new Rect(0, 0, this.mScreenSize.x, this.mScreenSize.y));
            } catch (IllegalArgumentException e) {
            } catch (OutOfResourcesException e2) {
            }
            if (c != null) {
                switch (this.mRotation) {
                    case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                    case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                        this.mSurfaceControl.setPosition(0.0f, 0.0f);
                        break;
                    case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                        this.mSurfaceControl.setPosition(0.0f, (float) (-this.mScreenOffset));
                        break;
                    case H.REPORT_LOSING_FOCUS /*3*/:
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
