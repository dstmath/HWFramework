package com.huawei.server.fingerprint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.biometric.FingerprintSurfaceEx;
import com.huawei.android.os.SystemPropertiesEx;

public class FingerprintCircleOverlay {
    private static final int BLUR_MASK_PARAMETER = 20;
    private static final float DEFUALT_RADIUS = 95.0f;
    private static final float DEFUALT_SCALE = 1.0f;
    private static final int DIA_RADIUS_RATIO = 2;
    private static final boolean IS_SUPPORT_AP = "2".equals(SystemPropertiesEx.get("ro.config.support_aod", (String) null));
    private static final int LENGTH_RADIUS_RATIO = 4;
    private static final String TAG = "FingerprintCircleOverlay";
    private static final String TITLE = "fingerprint_circle_layer";
    private boolean isVisible;
    private float mAlpha;
    private int mCenterX;
    private int mCenterY;
    private int mColor;
    private Context mContext;
    private FingerprintSurfaceEx mFingerprintSurfaceEx;
    private int mHeight;
    private boolean mIsCreate;
    private int mLayer;
    private PowerManager mPowerManager;
    private float mRadius = DEFUALT_RADIUS;
    private float mScale = DEFUALT_SCALE;
    private int mWidth;

    public FingerprintCircleOverlay(Context context, PowerManager powerManager) {
        this.mContext = context;
        this.mPowerManager = powerManager;
    }

    public void create(int centerX, int centerY, float scale) {
        this.mCenterX = centerX;
        this.mCenterY = centerY;
        this.mScale = scale;
        if (!IS_SUPPORT_AP) {
            this.mIsCreate = createSurface();
            drawIfNeeded();
            return;
        }
        this.mIsCreate = createSurfaceFullscreen();
    }

    public void show() {
        if (this.mFingerprintSurfaceEx != null && !this.isVisible) {
            Log.i(TAG, "fingerprintCircleOverlay show");
            if (IS_SUPPORT_AP) {
                drawFpCircleWithTransparentBottomArea(FingerprintAnimByThemeModel.getAnimationHeight(this.mContext));
            }
            this.mFingerprintSurfaceEx.show();
            this.isVisible = true;
            FingerViewController.getInstance(this.mContext).notifyCaptureImage();
        }
    }

    public void hide() {
        if (this.mFingerprintSurfaceEx != null && this.isVisible) {
            Log.i(TAG, "fingerprintCircleOverlay hide");
            this.mFingerprintSurfaceEx.hide();
            this.isVisible = false;
            FingerViewController.getInstance(this.mContext).notifyDismissBlueSpot();
        }
    }

    public void destroy() {
        FingerprintSurfaceEx fingerprintSurfaceEx = this.mFingerprintSurfaceEx;
        if (fingerprintSurfaceEx != null) {
            fingerprintSurfaceEx.remove();
            Log.i(TAG, "fingerprintCircleOverlay destroy");
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

    public void setColor(int color) {
        this.mColor = color;
    }

    public void setRadius(int radius) {
        this.mRadius = (float) radius;
    }

    public void setAlpha(float alpha) {
        this.mAlpha = alpha;
        FingerprintSurfaceEx fingerprintSurfaceEx = this.mFingerprintSurfaceEx;
        if (fingerprintSurfaceEx != null) {
            fingerprintSurfaceEx.setAlpha(alpha);
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
        FingerprintSurfaceEx fingerprintSurfaceEx = this.mFingerprintSurfaceEx;
        if (fingerprintSurfaceEx != null) {
            fingerprintSurfaceEx.setLayer(layer);
        }
    }

    private boolean createSurface() {
        Log.i(TAG, "fingerprintCircleOverlay createSurface mScale =" + this.mScale + ", mRadius = " + this.mRadius);
        if (this.mFingerprintSurfaceEx == null) {
            this.mFingerprintSurfaceEx = new FingerprintSurfaceEx();
        }
        if (this.mScale == 0.0f) {
            Log.i(TAG, "scale INVALID, use defalut");
            this.mScale = DEFUALT_SCALE;
        }
        if (this.mRadius == 0.0f) {
            Log.i(TAG, "mRadius INVALID, use defalut");
            this.mRadius = DEFUALT_RADIUS;
        }
        FingerprintSurfaceEx.openTransaction();
        try {
            this.mFingerprintSurfaceEx.createSurfaceControl(TITLE, (int) (this.mRadius * this.mScale * 4.0f), (int) (this.mRadius * this.mScale * 4.0f), 4, -3);
            this.mFingerprintSurfaceEx.setSurfaceControlPosition((((float) this.mCenterX) - (this.mRadius * 2.0f)) * this.mScale, (((float) this.mCenterY) - (this.mRadius * 2.0f)) * this.mScale);
            FingerprintController.getInstance().setSurfaceControlByDisplaySettings(this.mFingerprintSurfaceEx);
            this.mFingerprintSurfaceEx.createSurface();
            FingerprintSurfaceEx.closeTransaction();
            return true;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "mRadius = " + this.mRadius + "mScale = " + this.mScale);
            FingerprintSurfaceEx.closeTransaction();
            return false;
        } catch (Throwable th) {
            FingerprintSurfaceEx.closeTransaction();
            throw th;
        }
    }

    private boolean createSurfaceFullscreen() {
        Log.i(TAG, "fingerprintCircleOverlay createSurface mScale =" + this.mScale + ", mRadius = " + this.mRadius);
        if (this.mFingerprintSurfaceEx == null) {
            this.mFingerprintSurfaceEx = new FingerprintSurfaceEx();
        }
        if (this.mScale == 0.0f) {
            Log.i(TAG, "scale INVALID, use defalut");
            this.mScale = DEFUALT_SCALE;
        }
        if (this.mRadius == 0.0f) {
            Log.i(TAG, "mRadius INVALID, use defalut");
            this.mRadius = DEFUALT_RADIUS;
        }
        FingerprintSurfaceEx.openTransaction();
        try {
            Point screenSize = getScreenSize();
            this.mFingerprintSurfaceEx.createSurfaceControl(TITLE, screenSize.x, screenSize.y, 4, -3);
            this.mFingerprintSurfaceEx.setSurfaceControlPosition(0.0f, 0.0f);
            FingerprintController.getInstance().setSurfaceControlByDisplaySettings(this.mFingerprintSurfaceEx);
            this.mFingerprintSurfaceEx.createSurface();
            FingerprintSurfaceEx.closeTransaction();
            return true;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "mRadius = " + this.mRadius + "mScale = " + this.mScale);
            FingerprintSurfaceEx.closeTransaction();
            return false;
        } catch (Throwable th) {
            FingerprintSurfaceEx.closeTransaction();
            throw th;
        }
    }

    private Point getScreenSize() {
        String defaultScreenSize = SystemPropertiesEx.get("ro.config.default_screensize");
        int screenWidth = 0;
        int screenHight = 0;
        if (!TextUtils.isEmpty(defaultScreenSize)) {
            String[] array = defaultScreenSize.split(",");
            if (array.length == 2) {
                try {
                    screenWidth = Integer.parseInt(array[0]);
                    screenHight = Integer.parseInt(array[1]);
                } catch (NumberFormatException e) {
                    Log.i(TAG, "Invalid defaultScreenSize prop:" + defaultScreenSize, e);
                }
            } else {
                Log.i(TAG, "Invalid defaultScreenSize prop:" + defaultScreenSize);
            }
        } else {
            screenWidth = Settings.Global.getInt(this.mContext.getContentResolver(), "aps_init_height", -1);
            screenHight = Settings.Global.getInt(this.mContext.getContentResolver(), "aps_init_width", -1);
        }
        return new Point(screenWidth, screenHight);
    }

    private void drawIfNeeded() {
        if (this.mFingerprintSurfaceEx == null) {
            Log.e(TAG, "mSurface not created");
            return;
        }
        Log.i(TAG, "fingerprintCircleOverlay drawIfNeeded");
        Canvas canvas = null;
        try {
            canvas = this.mFingerprintSurfaceEx.lockCanvas((Rect) null);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "IllegalArgumentException cannot get Canvas");
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
            this.mFingerprintSurfaceEx.unlockCanvasAndPost(canvas);
        }
    }

    public void makeBackgroundTransparent() {
        if (FingerprintViewUtils.isAppAodMode(this.mPowerManager)) {
            drawFpCircleWithTransparentBottomArea(0);
        } else {
            Log.e(TAG, "Background is already transparent");
        }
    }

    private void drawFpCircleWithTransparentBottomArea(int transparentHeight) {
        if (this.mFingerprintSurfaceEx == null) {
            Log.e(TAG, "mSurface not created");
            return;
        }
        Log.i(TAG, "fingerprintCircleOverlay drawIfNeeded");
        Canvas canvas = null;
        try {
            canvas = this.mFingerprintSurfaceEx.lockCanvas((Rect) null);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "IllegalArgumentException cannot get Canvas");
        }
        if (canvas != null) {
            Point screenSize = getScreenSize();
            Paint clearPaint = new Paint();
            clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            canvas.drawRect(0.0f, 0.0f, (float) screenSize.x, (float) screenSize.y, clearPaint);
            if (FingerprintViewUtils.isAppAodMode(this.mPowerManager)) {
                Log.i(TAG, "Draw in ap mode");
                drawFpCircleWithTransparentBottom(canvas, screenSize, transparentHeight);
            } else {
                Log.i(TAG, "Draw in normal mode");
                drawFpCircleWithTransparentBackgound(canvas, screenSize);
            }
            this.mFingerprintSurfaceEx.unlockCanvasAndPost(canvas);
        }
    }

    private void drawFpCircleWithTransparentBottom(Canvas destCanvas, Point screenSize, int transparentHeight) {
        int animationHeight = transparentHeight;
        Bitmap mask = Bitmap.createBitmap(screenSize.x, screenSize.y, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mask);
        Paint paint = new Paint(1);
        paint.setColor(-16777216);
        canvas.drawRect(0.0f, 0.0f, (float) screenSize.x, (float) screenSize.y, paint);
        int layer = destCanvas.saveLayer(0.0f, 0.0f, (float) screenSize.x, (float) screenSize.y, paint);
        destCanvas.drawBitmap(mask, 0.0f, 0.0f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        Path animationArea = new Path();
        if (animationHeight == 0) {
            Log.e(TAG, "Get animation height failed");
            animationHeight = screenSize.y;
        }
        animationArea.addRect(0.0f, (float) (screenSize.y - animationHeight), (float) screenSize.x, (float) screenSize.y, Path.Direction.CW);
        destCanvas.drawPath(animationArea, paint);
        paint.setXfermode(null);
        destCanvas.restoreToCount(layer);
        drawFpCircle(destCanvas, screenSize);
    }

    private void drawFpCircleWithTransparentBackgound(Canvas canvas, Point screenSize) {
        canvas.drawColor(0);
        drawFpCircle(canvas, screenSize);
    }

    private void drawFpCircle(Canvas canvas, Point screenSize) {
        Paint circlePaint = new Paint(1);
        circlePaint.setDither(true);
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setColor(this.mColor);
        circlePaint.setMaskFilter(new BlurMaskFilter(20.0f, BlurMaskFilter.Blur.SOLID));
        canvas.drawCircle((float) this.mCenterX, (float) this.mCenterY, this.mRadius * this.mScale, circlePaint);
        Log.i(TAG, "aod_fp_center:" + this.mCenterX + "," + this.mCenterY);
    }
}
