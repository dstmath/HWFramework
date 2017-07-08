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
    private static final String TAG = null;
    private boolean mDrawNeeded;
    private int mLastDH;
    private int mLastDW;
    private final Surface mSurface;
    private final SurfaceControl mSurfaceControl;
    private final int mThickness;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wm.StrictModeFlash.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wm.StrictModeFlash.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.StrictModeFlash.<clinit>():void");
    }

    public StrictModeFlash(Display display, SurfaceSession session) {
        SurfaceControl ctrl;
        this.mSurface = new Surface();
        this.mThickness = 20;
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
