package com.huawei.server.fingerprint;

import android.graphics.Canvas;
import android.os.SystemProperties;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.SurfaceSession;

public class FingerprintMaskOverlay {
    private static final int DEFAULT_SCREEN_SIZE_STRING = 2;
    private static final String ELLE_TITLE = "screenon_fingerprint_alpha_layer";
    private static final float MIN_ALPHA = 0.004f;
    private static final String TAG = "FingerprintMaskOverlay";
    private static final String VOGUE_TITLE = "fingerprint_alpha_layer";
    private boolean isVisible;
    private float mAlpha;
    private int mHeight;
    private boolean mIsCreate;
    private int mLayer;
    private Surface mSurface;
    private SurfaceControl mSurfaceControl;
    private SurfaceSession mSurfaceSession;
    private String mTitle;
    private int mWidth;

    public void create(int width, int height, int type) {
        Log.i(TAG, "create width = " + width + " ,height = " + height + " ,type = " + type);
        this.mWidth = width;
        this.mHeight = height;
        this.mTitle = type == 0 ? VOGUE_TITLE : ELLE_TITLE;
        this.mIsCreate = createSurface();
        drawIfNeeded();
    }

    public void show(float alpha) {
        this.mAlpha = alpha;
        SurfaceControl surfaceControl = this.mSurfaceControl;
        if (surfaceControl != null && !this.isVisible) {
            surfaceControl.setAlpha(alpha);
            this.mSurfaceControl.show();
            this.isVisible = true;
        }
    }

    public void show() {
        if (this.mSurfaceControl != null && !this.isVisible) {
            Log.i(TAG, "FingerprintMaskOverlay show");
            this.mSurfaceControl.show();
            this.isVisible = true;
        }
    }

    public void setAlpha(float alpha) {
        Log.i(TAG, "FingerprintMaskOverlay alpha =" + alpha);
        this.mAlpha = alpha;
        if (this.mAlpha == 0.0f) {
            this.mAlpha = MIN_ALPHA;
            Log.i(TAG, "FingerprintMaskOverlay change alpha to min alpha");
        }
        SurfaceControl surfaceControl = this.mSurfaceControl;
        if (surfaceControl != null) {
            surfaceControl.setAlpha(this.mAlpha);
        }
    }

    public void setLayer(int layer) {
        this.mLayer = layer;
        SurfaceControl surfaceControl = this.mSurfaceControl;
        if (surfaceControl != null) {
            surfaceControl.setLayer(layer);
        }
    }

    public void hide() {
        if (this.mSurfaceControl != null && this.isVisible) {
            Log.i(TAG, "FingerprintMaskOverlay hide");
            this.mSurfaceControl.hide();
            this.isVisible = false;
        }
    }

    public void destroy() {
        if (this.mSurfaceControl != null) {
            Log.i(TAG, "destroy mFingerprintMaskOverlay");
            this.mSurfaceControl.remove();
            this.mSurfaceControl = null;
            this.isVisible = false;
            this.mIsCreate = false;
        }
    }

    public boolean isVisible() {
        return this.isVisible;
    }

    public boolean isCreate() {
        return this.mIsCreate;
    }

    private boolean createSurface() {
        Log.i(TAG, "FingerprintMaskOverlay createSurface mTitle = " + this.mTitle);
        if (this.mSurfaceSession == null) {
            this.mSurfaceSession = new SurfaceSession();
        }
        if ((this.mWidth == 0 || this.mHeight == 0) && !getDefaultScreenSize()) {
            Log.i(TAG, "cannot get size of screen, return");
            return false;
        }
        SurfaceControl.openTransaction();
        try {
            if (this.mSurfaceControl == null) {
                this.mSurfaceControl = new SurfaceControl.Builder(this.mSurfaceSession).setName(this.mTitle).setBufferSize(this.mWidth, this.mHeight).setFlags(4).setParent(null).setFormat(-3).build();
                this.mSurfaceControl.setPosition(0.0f, 0.0f);
                FingerprintController.getInstance().setSurfaceControlByDisplaySettings(this.mSurfaceControl);
            }
            this.mSurface = new Surface();
            this.mSurface.copyFrom(this.mSurfaceControl);
            SurfaceControl.closeTransaction();
            return true;
        } catch (Surface.OutOfResourcesException e) {
            Log.e(TAG, "Unable to create mSurfaceControl");
            SurfaceControl.closeTransaction();
            return false;
        } catch (IllegalArgumentException e2) {
            Log.e(TAG, "mWidth = " + this.mWidth + "mHeight = " + this.mHeight);
            SurfaceControl.closeTransaction();
            return false;
        } catch (Throwable th) {
            SurfaceControl.closeTransaction();
            throw th;
        }
    }

    private void drawIfNeeded() {
        if (this.mSurface == null) {
            Log.e(TAG, "mSurface not created");
            return;
        }
        Log.i(TAG, "FingerprintMaskOverlay drawIfNeeded");
        Canvas canvas = null;
        try {
            canvas = this.mSurface.lockCanvas(null);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "IllegalArgumentException cannot get Canvas");
        } catch (Surface.OutOfResourcesException e2) {
            Log.e(TAG, "OutOfResourcesException cannot get Canvas");
        }
        if (canvas != null) {
            canvas.drawColor(-16777216);
            this.mSurface.unlockCanvasAndPost(canvas);
        }
    }

    private boolean getDefaultScreenSize() {
        String defaultScreenSize = SystemProperties.get("ro.config.default_screensize");
        if (defaultScreenSize != null && !"".equals(defaultScreenSize)) {
            String[] arrays = defaultScreenSize.split(",");
            if (arrays.length == 2) {
                try {
                    this.mWidth = Integer.parseInt(arrays[0]);
                    this.mHeight = Integer.parseInt(arrays[1]);
                    Log.i(TAG, "defaultScreenSizePoint get from prop : mInitDisplayWidth=" + this.mWidth + ",mInitDisplayHeight=" + this.mHeight);
                    return true;
                } catch (NumberFormatException e) {
                    Log.i(TAG, "defaultScreenSizePoint: NumberFormatException");
                    return false;
                }
            } else {
                Log.i(TAG, "defaultScreenSizePoint the defaultScreenSize prop is error,defaultScreenSize=" + defaultScreenSize);
            }
        }
        return false;
    }
}
