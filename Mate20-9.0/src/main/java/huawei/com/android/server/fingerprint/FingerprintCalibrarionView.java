package huawei.com.android.server.fingerprint;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.IPowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import com.android.server.UiThread;
import com.android.server.gesture.GestureNavConst;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.huawei.android.os.SystemPropertiesEx;

public class FingerprintCalibrarionView {
    private static final String APS_INIT_HEIGHT = "aps_init_height";
    private static final int CAPTURE_BRIGHTNESS = 248;
    private static final int DEFAULT_INIT_HEIGHT = 2880;
    private static final int INITIAL_BRIGHTNESS = -1;
    private static final String TAG = "FingerprintCalibrarionView";
    private static FingerprintCalibrarionView sInstance;
    private final Runnable mAddHighLightViewRunnableCali = new Runnable() {
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
        public void run() {
            FingerprintCalibrarionView.this.hideCaliView();
        }
    };
    private WindowManager mWindowManager;
    private IPowerManager pm;

    private static class HighLightMaskViewCali extends View {
        private static final int DEFAULT_CIRCLE_RADIUS = 180;
        private int mCenterX;
        private int mCenterY;
        private int mOverlayColor;
        private final Paint mPaint = new Paint(1);
        private int mRadius;
        private float mScale;

        public HighLightMaskViewCali(Context context) {
            super(context);
            this.mPaint.setDither(true);
            this.mPaint.setStyle(Paint.Style.FILL);
            this.mRadius = 180;
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

        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawColor(this.mOverlayColor);
            canvas.drawCircle(((float) this.mCenterX) * this.mScale, ((float) this.mCenterY) * this.mScale, ((float) this.mRadius) * this.mScale, this.mPaint);
            this.mPaint.setXfermode(null);
        }

        public void setOverlayColor(int overlayColor) {
            this.mOverlayColor = overlayColor;
        }
    }

    private FingerprintCalibrarionView(Context context) {
        this.mContext = context;
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.pm = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
    }

    public static FingerprintCalibrarionView getInstance(Context context) {
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
        } else if (value == 200) {
            this.mFlag++;
            if (this.mFlag == 4) {
                Log.d(TAG, "remove highlight view");
                UiThread.getHandler().postDelayed(this.mRemoveHighLightViewRunnableCali, 5000);
            }
        }
        this.mAlphaValue = value;
        UiThread.getHandler().post(this.mAddHighLightViewRunnableCali);
    }

    public void setCenterPoints(int x, int y) {
        this.mFingerprintCenterX = x;
        this.mFingerprintCenterY = y;
    }

    private void setLightLevel(int level, int lightLevelTime) {
        try {
            this.pm.setBrightnessNoLimit(level, lightLevelTime);
            Log.d(TAG, "setLightLevel :" + level + " time:" + lightLevelTime);
        } catch (RemoteException e) {
            Log.e(TAG, "setFingerprintviewHighlight catch RemoteException ");
        }
    }

    /* access modifiers changed from: private */
    public void showCaliView() {
        Log.d(TAG, "showCaliView>>> mAlphaValue = " + this.mAlphaValue + " mIsViewAdded = " + this.mIsViewAdded);
        if (!this.mIsViewAdded) {
            this.mCaliView = new HighLightMaskViewCali(this.mContext);
            this.mCaliView.setCenter(this.mFingerprintCenterX, this.mFingerprintCenterY);
            int defaultDisplayHeight = Settings.Global.getInt(this.mContext.getContentResolver(), APS_INIT_HEIGHT, DEFAULT_INIT_HEIGHT);
            this.mCaliView.setScale(((float) SystemPropertiesEx.getInt("persist.sys.rog.height", defaultDisplayHeight)) / ((float) defaultDisplayHeight));
            this.mCaliView.setCircleColor(Color.argb((float) this.mAlphaValue, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO));
            this.mCaliViewParams = new WindowManager.LayoutParams(-1, -1);
            this.mCaliViewParams.type = HwArbitrationDEFS.MSG_MPLINK_STOP_COEX_SUCC;
            this.mCaliViewParams.flags = 1304;
            this.mCaliViewParams.privateFlags |= 16;
            this.mCaliViewParams.format = -3;
            this.mWindowManager.addView(this.mCaliView, this.mCaliViewParams);
            this.mIsViewAdded = true;
            return;
        }
        this.mCaliView.setCircleColor(Color.argb((float) this.mAlphaValue, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO));
        this.mCaliView.invalidate();
    }

    /* access modifiers changed from: private */
    public void hideCaliView() {
        Log.d(TAG, "hideCaliView>>>");
        this.mIsViewAdded = false;
        this.mCaliView.setVisibility(4);
        this.mCaliView.invalidate();
        setLightLevel(-1, 0);
    }
}
