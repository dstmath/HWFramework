package com.huawei.server.fingerprint;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.PowerManager;
import android.util.Log;
import com.huawei.android.biometric.FingerprintSurfaceEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.hwpartfingerprintopt.BuildConfig;

public class FingerprintMaskOverlay {
    private static final int DEFAULT_BRIGHTNESS = 100;
    private static final int DEFAULT_SCREEN_SIZE_STRING = 2;
    private static final String ELLE_TITLE = "screenon_fingerprint_alpha_layer";
    private static final float MAX_ALPHA = 1.0f;
    private static final float MIN_ALPHA = 0.004f;
    private static final String TAG = "FingerprintMaskOverlay";
    private static final String VOGUE_TITLE = "fingerprint_alpha_layer";
    private boolean isVisible;
    private float mAlpha;
    private FingerprintSurfaceEx mFingerprintSurfaceEx;
    private int mHeight;
    private boolean mIsCreate;
    private int mLayer;
    private PowerManager mPowerManager;
    private String mTitle;
    private int mWidth;

    public FingerprintMaskOverlay(PowerManager powerManager) {
        this.mPowerManager = powerManager;
    }

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
        FingerprintSurfaceEx fingerprintSurfaceEx = this.mFingerprintSurfaceEx;
        if (fingerprintSurfaceEx != null && !this.isVisible) {
            fingerprintSurfaceEx.setAlpha(alpha);
            this.mFingerprintSurfaceEx.show();
            this.isVisible = true;
        }
    }

    public void show() {
        if (this.mFingerprintSurfaceEx != null && !this.isVisible) {
            Log.i(TAG, "FingerprintMaskOverlay show");
            this.mFingerprintSurfaceEx.show();
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
        if (FingerprintViewUtils.isAppAodMode(this.mPowerManager) && FingerprintViewUtils.getBrightness() < 100) {
            this.mAlpha = MAX_ALPHA;
            Log.i(TAG, "FingerprintMaskOverlay max setAlpha");
        }
        FingerprintSurfaceEx fingerprintSurfaceEx = this.mFingerprintSurfaceEx;
        if (fingerprintSurfaceEx != null) {
            fingerprintSurfaceEx.setAlpha(this.mAlpha);
        }
    }

    public void setLayer(int layer) {
        this.mLayer = layer;
        FingerprintSurfaceEx fingerprintSurfaceEx = this.mFingerprintSurfaceEx;
        if (fingerprintSurfaceEx != null) {
            fingerprintSurfaceEx.setLayer(layer);
        }
    }

    public void hide() {
        if (this.mFingerprintSurfaceEx != null && this.isVisible) {
            Log.i(TAG, "FingerprintMaskOverlay hide");
            this.mFingerprintSurfaceEx.hide();
            this.isVisible = false;
        }
    }

    public void destroy() {
        if (this.mFingerprintSurfaceEx != null) {
            Log.i(TAG, "destroy mFingerprintMaskOverlay");
            this.mFingerprintSurfaceEx.remove();
            this.mFingerprintSurfaceEx = null;
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
        if (this.mFingerprintSurfaceEx == null) {
            this.mFingerprintSurfaceEx = new FingerprintSurfaceEx();
        }
        if ((this.mWidth == 0 || this.mHeight == 0) && !getDefaultScreenSize()) {
            Log.i(TAG, "cannot get size of screen, return");
            return false;
        }
        FingerprintSurfaceEx.openTransaction();
        try {
            this.mFingerprintSurfaceEx.createSurfaceControl(this.mTitle, this.mWidth, this.mHeight, 4, -3);
            this.mFingerprintSurfaceEx.setSurfaceControlPosition(0.0f, 0.0f);
            FingerprintController.getInstance().setSurfaceControlByDisplaySettings(this.mFingerprintSurfaceEx);
            this.mFingerprintSurfaceEx.createSurface();
            FingerprintSurfaceEx.closeTransaction();
            return true;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Unable to create mSurfaceControl");
            FingerprintSurfaceEx.closeTransaction();
            return false;
        } catch (Throwable th) {
            FingerprintSurfaceEx.closeTransaction();
            throw th;
        }
    }

    private void drawIfNeeded() {
        if (this.mFingerprintSurfaceEx == null) {
            Log.e(TAG, "mSurface not created");
            return;
        }
        Log.i(TAG, "FingerprintMaskOverlay drawIfNeeded");
        Canvas canvas = null;
        try {
            canvas = this.mFingerprintSurfaceEx.lockCanvas((Rect) null);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "IllegalArgumentException cannot get Canvas");
        }
        if (canvas != null) {
            canvas.drawColor(-16777216);
            this.mFingerprintSurfaceEx.unlockCanvasAndPost(canvas);
        }
    }

    private boolean getDefaultScreenSize() {
        String defaultScreenSize = SystemPropertiesEx.get("ro.config.default_screensize");
        if (defaultScreenSize != null && !BuildConfig.FLAVOR.equals(defaultScreenSize)) {
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
