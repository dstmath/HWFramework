package com.huawei.server.fingerprint;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import com.huawei.android.biometric.FingerprintSupportEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.view.WindowManagerEx;
import com.huawei.server.UiThreadEx;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class FingerprintCalibrarionView {
    private static final String APS_INIT_HEIGHT = "aps_init_height";
    private static final int CALIBRARION_CAPTURE_ERROR_CODE = 4;
    private static final int CALIBRARION_REMOVE_ERROR_CODE = 200;
    private static final int CALIBRARION_REMOVE_FLAG = 4;
    private static final long CALIBRARION_REMOVE_HIGHLIGHT_TIME = 5000;
    private static final int CAPTURE_BRIGHTNESS = 248;
    private static final int DEFAULT_INIT_HEIGHT = 2880;
    private static final int INITIAL_BRIGHTNESS = -1;
    private static final int ROUNDING = 2;
    private static final String TAG = "FingerprintCalibrarionView";
    private static FingerprintCalibrarionView sInstance;
    private final Runnable mAddHighLightViewRunnableCali = new Runnable() {
        /* class com.huawei.server.fingerprint.FingerprintCalibrarionView.AnonymousClass1 */

        @Override // java.lang.Runnable
        public void run() {
            FingerprintCalibrarionView.this.showCaliView();
        }
    };
    private int mAlphaValue;
    private HighLightMaskViewCali mCaliView;
    private WindowManager.LayoutParams mCaliViewParams;
    private Context mContext;
    private int mDefaultDisplayHeight;
    private int mFingerprintCenterX;
    private int mFingerprintCenterY;
    private int mFlag;
    private boolean mIsViewAdded;
    private final Runnable mRemoveHighLightViewRunnableCali = new Runnable() {
        /* class com.huawei.server.fingerprint.FingerprintCalibrarionView.AnonymousClass2 */

        @Override // java.lang.Runnable
        public void run() {
            FingerprintCalibrarionView.this.hideCaliView();
        }
    };
    private WindowManager mWindowManager;

    private FingerprintCalibrarionView(Context context) {
        this.mContext = context;
        Object object = context.getSystemService("window");
        if (object instanceof WindowManager) {
            this.mWindowManager = (WindowManager) object;
        }
    }

    public static synchronized FingerprintCalibrarionView getInstance(Context context) {
        FingerprintCalibrarionView fingerprintCalibrarionView;
        synchronized (FingerprintCalibrarionView.class) {
            if (sInstance == null) {
                sInstance = new FingerprintCalibrarionView(context);
            }
            fingerprintCalibrarionView = sInstance;
        }
        return fingerprintCalibrarionView;
    }

    public void showHighlightviewCali(int value) {
        Log.d(TAG, "showHighlightviewCali>>> value = " + value);
        if (value == 4) {
            this.mFlag = 0;
            this.mIsViewAdded = false;
            setLightLevel(CAPTURE_BRIGHTNESS, 0);
        } else if (value == CALIBRARION_REMOVE_ERROR_CODE) {
            this.mFlag++;
            if (this.mFlag == 4) {
                Log.d(TAG, "remove highlight view");
                UiThreadEx.getHandler().postDelayed(this.mRemoveHighLightViewRunnableCali, CALIBRARION_REMOVE_HIGHLIGHT_TIME);
            }
        } else {
            Log.d(TAG, "showHighlightviewCali Flag = " + this.mFlag);
        }
        this.mAlphaValue = value;
        UiThreadEx.getHandler().post(this.mAddHighLightViewRunnableCali);
    }

    public void setCenterPoints(int pointx, int pointy) {
        this.mFingerprintCenterX = pointx;
        this.mFingerprintCenterY = pointy;
    }

    private void setLightLevel(int level, int lightLevelTime) {
        FingerprintSupportEx.getInstance().setBrightnessNoLimit(level, lightLevelTime);
        Log.d(TAG, "setLightLevel :" + level + " time:" + lightLevelTime);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showCaliView() {
        Log.d(TAG, "showCaliView>>> mAlphaValue = " + this.mAlphaValue + " mIsViewAdded = " + this.mIsViewAdded);
        if (!this.mIsViewAdded) {
            this.mCaliView = new HighLightMaskViewCali(this.mContext);
            this.mCaliView.setCenter(this.mFingerprintCenterX, this.mFingerprintCenterY);
            int defaultDisplayHeight = Settings.Global.getInt(this.mContext.getContentResolver(), APS_INIT_HEIGHT, DEFAULT_INIT_HEIGHT);
            this.mCaliView.setScale(new BigDecimal(SystemPropertiesEx.getInt("persist.sys.rog.height", defaultDisplayHeight)).divide(new BigDecimal(defaultDisplayHeight), 2, RoundingMode.HALF_UP).floatValue());
            this.mCaliView.setCircleColor(Color.argb((float) this.mAlphaValue, 0.0f, 0.0f, 0.0f));
            this.mCaliViewParams = new WindowManager.LayoutParams(-1, -1);
            this.mCaliViewParams.type = FingerprintSupportEx.getTypeSecureSystemOverlay();
            WindowManager.LayoutParams layoutParams = this.mCaliViewParams;
            layoutParams.flags = 1304;
            FingerprintSupportEx.setLayoutParamsPrivateFlags(layoutParams, WindowManagerEx.LayoutParamsEx.getPrivateFlagShowForAllUsers());
            WindowManager.LayoutParams layoutParams2 = this.mCaliViewParams;
            layoutParams2.format = -3;
            WindowManager windowManager = this.mWindowManager;
            if (windowManager != null) {
                windowManager.addView(this.mCaliView, layoutParams2);
                this.mIsViewAdded = true;
                return;
            }
            return;
        }
        this.mCaliView.setCircleColor(Color.argb((float) this.mAlphaValue, 0.0f, 0.0f, 0.0f));
        this.mCaliView.invalidate();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void hideCaliView() {
        Log.d(TAG, "hideCaliView>>>");
        this.mIsViewAdded = false;
        this.mCaliView.setVisibility(4);
        this.mCaliView.invalidate();
        setLightLevel(-1, 0);
    }

    /* access modifiers changed from: private */
    public static class HighLightMaskViewCali extends View {
        private static final int DEFAULT_CIRCLE_RADIUS = 180;
        private int mCenterX;
        private int mCenterY;
        private int mOverlayColor;
        private final Paint mPaint = new Paint(1);
        private int mRadius;
        private float mScale;

        HighLightMaskViewCali(Context context) {
            super(context);
            this.mPaint.setDither(true);
            this.mPaint.setStyle(Paint.Style.FILL);
            this.mRadius = DEFAULT_CIRCLE_RADIUS;
        }

        public void setCenter(int centerX, int centerY) {
            this.mCenterX = centerX;
            this.mCenterY = centerY;
        }

        public void setScale(float scale) {
            this.mScale = scale;
        }

        public void setCircleColor(int color) {
            this.mPaint.setColor(color);
        }

        @Override // android.view.View
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawColor(this.mOverlayColor);
            float f = this.mScale;
            canvas.drawCircle(((float) this.mCenterX) * f, ((float) this.mCenterY) * f, ((float) this.mRadius) * f, this.mPaint);
            this.mPaint.setXfermode(null);
        }

        public void setOverlayColor(int overlayColor) {
            this.mOverlayColor = overlayColor;
        }
    }
}
