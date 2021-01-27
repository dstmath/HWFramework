package com.huawei.android.biometric;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.SurfaceSession;

public class FingerprintSurfaceEx {
    public static final int HIDDEN = 4;
    private static final String TAG = "FingerprintSurfaceEx";
    private Surface mSurface;
    private SurfaceControl mSurfaceControl;
    private SurfaceSession mSurfaceSession;

    public FingerprintSurfaceEx() {
        this.mSurfaceSession = new SurfaceSession();
    }

    public FingerprintSurfaceEx(SurfaceControl surfaceControl) {
        if (surfaceControl == null) {
            this.mSurfaceSession = new SurfaceSession();
        } else {
            this.mSurfaceControl = surfaceControl;
        }
    }

    public void createSurfaceControl(String title, int wide, int heigh, int flag, int format) {
        try {
            if (this.mSurfaceSession != null) {
                this.mSurfaceControl = new SurfaceControl.Builder(this.mSurfaceSession).setName(title).setBufferSize(wide, heigh).setFlags(flag).setParent(null).setFormat(format).build();
            }
        } catch (Surface.OutOfResourcesException e) {
            Log.e(TAG, "createSurfaceControl OutOfResourcesException");
        }
    }

    public void setSurfaceControlPosition(float xaxis, float yaxis) {
        SurfaceControl surfaceControl = this.mSurfaceControl;
        if (surfaceControl != null) {
            surfaceControl.setPosition(xaxis, yaxis);
        }
    }

    public void createSurface() {
        this.mSurface = new Surface();
        SurfaceControl surfaceControl = this.mSurfaceControl;
        if (surfaceControl != null) {
            this.mSurface.copyFrom(surfaceControl);
        }
    }

    public static void openTransaction() {
        SurfaceControl.openTransaction();
    }

    public static void closeTransaction() {
        SurfaceControl.closeTransaction();
    }

    public void show() {
        SurfaceControl surfaceControl = this.mSurfaceControl;
        if (surfaceControl != null) {
            surfaceControl.show();
        }
    }

    public void hide() {
        SurfaceControl surfaceControl = this.mSurfaceControl;
        if (surfaceControl != null) {
            surfaceControl.hide();
        }
    }

    public void remove() {
        SurfaceControl surfaceControl = this.mSurfaceControl;
        if (surfaceControl != null) {
            surfaceControl.remove();
        }
    }

    public void setAlpha(float alpha) {
        SurfaceControl surfaceControl = this.mSurfaceControl;
        if (surfaceControl != null) {
            surfaceControl.setAlpha(alpha);
        }
    }

    public void setLayer(int layer) {
        SurfaceControl surfaceControl = this.mSurfaceControl;
        if (surfaceControl != null) {
            surfaceControl.setLayer(layer);
        }
    }

    public Canvas lockCanvas(Rect inOutDirty) {
        Surface surface = this.mSurface;
        if (surface == null) {
            return null;
        }
        try {
            return surface.lockCanvas(null);
        } catch (Surface.OutOfResourcesException e) {
            Log.e(TAG, "lockCanvas OutOfResourcesException");
            return null;
        }
    }

    public void unlockCanvasAndPost(Canvas canvas) {
        Surface surface = this.mSurface;
        if (surface != null) {
            surface.unlockCanvasAndPost(canvas);
        }
    }

    public void setColorTransform(float[] matrix, float[] translation) {
        SurfaceControl surfaceControl = this.mSurfaceControl;
        if (surfaceControl != null) {
            surfaceControl.setColorTransform(matrix, translation);
        }
    }
}
