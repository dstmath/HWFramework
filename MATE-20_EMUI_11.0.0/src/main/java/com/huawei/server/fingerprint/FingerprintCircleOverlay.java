package com.huawei.server.fingerprint;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import huawei.com.android.server.fingerprint.FingerViewController;

public class FingerprintCircleOverlay {
    private static final int BLUR_MASK_PARAMETER = 20;
    private static final float DEFUALT_RADIUS = 95.0f;
    private static final float DEFUALT_SCALE = 1.0f;
    private static final int DIA_RADIUS_RATIO = 2;
    private static final int LENGTH_RADIUS_RATIO = 4;
    private static final String TAG = "FingerprintCircleOverlay";
    private static final String TITLE = "fingerprint_circle_layer";
    private boolean isVisible;
    private float mAlpha;
    private int mCenterX;
    private int mCenterY;
    private int mColor;
    private Context mContext;
    private int mHeight;
    private boolean mIsCreate;
    private int mLayer;
    private float mRadius = DEFUALT_RADIUS;
    private float mScale = 1.0f;
    private Surface mSurface;
    private SurfaceControl mSurfaceControl;
    private SurfaceSession mSurfaceSession;
    private int mWidth;

    public FingerprintCircleOverlay(Context context) {
        this.mContext = context;
    }

    public void create(int centerX, int centerY, float scale) {
        this.mCenterX = centerX;
        this.mCenterY = centerY;
        this.mScale = scale;
        this.mIsCreate = createSurface();
        drawIfNeeded();
    }

    public void show() {
        if (this.mSurfaceControl != null && !this.isVisible) {
            Log.i(TAG, "fingerprintCircleOverlay show");
            this.mSurfaceControl.show();
            this.isVisible = true;
            FingerViewController.getInstance(this.mContext).notifyCaptureImage();
        }
    }

    public void hide() {
        if (this.mSurfaceControl != null && this.isVisible) {
            Log.i(TAG, "fingerprintCircleOverlay hide");
            this.mSurfaceControl.hide();
            this.isVisible = false;
            FingerViewController.getInstance(this.mContext).notifyDismissBlueSpot();
        }
    }

    public void destroy() {
        SurfaceControl surfaceControl = this.mSurfaceControl;
        if (surfaceControl != null) {
            surfaceControl.remove();
            Log.i(TAG, "fingerprintCircleOverlay destroy");
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

    public void setColor(int color) {
        this.mColor = color;
    }

    public void setRadius(int radius) {
        this.mRadius = (float) radius;
    }

    public void setAlpha(float alpha) {
        this.mAlpha = alpha;
        SurfaceControl surfaceControl = this.mSurfaceControl;
        if (surfaceControl != null) {
            surfaceControl.setAlpha(alpha);
        }
    }

    public void setScale(float scale) {
        this.mScale = scale;
        Log.d(TAG, "mScale = " + this.mScale);
    }

    public void setCenterPoints(int centerX, int centerY) {
        this.mCenterX = centerX;
        this.mCenterY = centerY;
        Log.d(TAG, "mCenterX = " + this.mCenterX + ",mCenterY=" + this.mCenterY);
    }

    public void setLayer(int layer) {
        this.mLayer = layer;
        SurfaceControl surfaceControl = this.mSurfaceControl;
        if (surfaceControl != null) {
            surfaceControl.setLayer(layer);
        }
    }

    private boolean createSurface() {
        Log.i(TAG, "fingerprintCircleOverlay createSurface mScale =" + this.mScale + ", mRadius = " + this.mRadius);
        if (this.mSurfaceSession == null) {
            this.mSurfaceSession = new SurfaceSession();
        }
        if (this.mScale == 0.0f) {
            Log.i(TAG, "scale INVALID, use defalut");
            this.mScale = 1.0f;
        }
        if (this.mRadius == 0.0f) {
            Log.i(TAG, "mRadius INVALID, use defalut");
            this.mRadius = DEFUALT_RADIUS;
        }
        SurfaceControl.openTransaction();
        try {
            if (this.mSurfaceControl == null) {
                this.mSurfaceControl = new SurfaceControl.Builder(this.mSurfaceSession).setName(TITLE).setBufferSize((int) (this.mRadius * this.mScale * 4.0f), (int) (this.mRadius * this.mScale * 4.0f)).setFlags(4).setParent(null).setFormat(-3).build();
                this.mSurfaceControl.setPosition((((float) this.mCenterX) - (this.mRadius * 2.0f)) * this.mScale, (((float) this.mCenterY) - (this.mRadius * 2.0f)) * this.mScale);
                FingerprintController.getInstance().setSurfaceControlByDisplaySettings(this.mSurfaceControl);
            }
            this.mSurface = new Surface();
            this.mSurface.copyFrom(this.mSurfaceControl);
            SurfaceControl.closeTransaction();
            return true;
        } catch (Surface.OutOfResourcesException e) {
            Log.e(TAG, "Unable to createCircleSurface1.");
            SurfaceControl.closeTransaction();
            return false;
        } catch (IllegalArgumentException e2) {
            Log.e(TAG, "mRadius = " + this.mRadius + "mScale = " + this.mScale);
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
        Log.i(TAG, "fingerprintCircleOverlay drawIfNeeded");
        Canvas canvas = null;
        try {
            canvas = this.mSurface.lockCanvas(null);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "IllegalArgumentException cannot get Canvas");
        } catch (Surface.OutOfResourcesException e2) {
            Log.e(TAG, "OutOfResourcesException cannot get Canvas");
        }
        if (canvas != null) {
            canvas.drawColor(0);
            Paint circlePaint = new Paint(1);
            circlePaint.setDither(true);
            circlePaint.setStyle(Paint.Style.FILL);
            circlePaint.setColor(this.mColor);
            circlePaint.setMaskFilter(new BlurMaskFilter(20.0f, BlurMaskFilter.Blur.SOLID));
            float f = this.mRadius;
            float f2 = this.mScale;
            canvas.drawCircle(f * 2.0f * f2, 2.0f * f * f2, f * f2, circlePaint);
            this.mSurface.unlockCanvasAndPost(canvas);
        }
    }
}
