package android.util;

import android.content.Context;
import android.hardware.SensorEvent;
import android.os.PowerManager;
import android.os.SystemProperties;
import com.huawei.android.os.HwPowerManager;

public class EsdDetection {
    public static final boolean DEBUG = false;
    private static int DETECT_MAX_TIME = 10000;
    private static final boolean ESD_ENABLE = SystemProperties.getBoolean("ro.product.esdenable", false);
    public static final int ESD_ENTER = 1;
    public static final int ESD_EXIT = 0;
    public static final int ESD_UNKNOW = -1;
    static final String TAG = "EsdDetection";
    private static EsdDetection mEsdDetection;
    private static Object mLock = new Object();
    private long detectStartTime = 0;
    private int esdCurrentStatus = -1;
    private int esdLastStatus = -1;
    private boolean esdNeedInitStatus = true;
    private Context mContext;
    private PowerManager mPowerManager;

    public static native int esdDetection_native(float f, float f2, float f3);

    public static native void init_native();

    public static native void unInit_native();

    static {
        try {
            System.loadLibrary("EsdDetection_jni");
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "LoadLibrary occurs error " + e.toString());
        }
    }

    private EsdDetection(Context context) {
        this.mContext = context;
        this.mPowerManager = (PowerManager) context.getSystemService("power");
    }

    public static EsdDetection getInstance(Context context) {
        EsdDetection esdDetection;
        synchronized (mLock) {
            if (mEsdDetection == null) {
                mEsdDetection = new EsdDetection(context);
            }
            esdDetection = mEsdDetection;
        }
        return esdDetection;
    }

    public static boolean isEsdEnabled() {
        return ESD_ENABLE;
    }

    public boolean esdDetection(SensorEvent event) {
        if (this.esdNeedInitStatus) {
            init_native();
            this.esdNeedInitStatus = false;
            this.detectStartTime = System.currentTimeMillis();
        }
        this.esdCurrentStatus = esdDetectionInternal(event);
        if (this.esdCurrentStatus != this.esdLastStatus) {
            Slog.i(TAG, "esdDetection esdCurrentStatus = " + this.esdCurrentStatus + ", esdLastStatus = " + this.esdLastStatus);
        }
        if (this.esdCurrentStatus == 1) {
            this.esdLastStatus = this.esdCurrentStatus;
            turnOffScreen();
            return true;
        } else if (this.esdCurrentStatus == 0 && this.esdLastStatus == 1) {
            unInitStatus();
            turnOnScreen();
            Slog.i(TAG, "detection finish.");
            return false;
        } else if (System.currentTimeMillis() - this.detectStartTime >= ((long) DETECT_MAX_TIME)) {
            Slog.i(TAG, "detection exceeded maximum time");
            unInitStatus();
            turnOnScreen();
            return false;
        } else {
            this.esdLastStatus = this.esdCurrentStatus;
            return true;
        }
    }

    public void unInitStatus() {
        unInit_native();
        this.esdNeedInitStatus = true;
        this.esdLastStatus = -1;
        this.esdCurrentStatus = -1;
        this.detectStartTime = 0;
    }

    public int getEsdCurrentStatus() {
        return this.esdCurrentStatus;
    }

    private int esdDetectionInternal(SensorEvent event) {
        if (event == null) {
            Slog.e(TAG, "event == null");
            return -1;
        } else if (event.values == null) {
            Slog.e(TAG, "event.values == null");
            return -1;
        } else if (event.values.length >= 3) {
            return esdDetection_native(event.values[0], event.values[1], event.values[2]);
        } else {
            Slog.e(TAG, "event.values.length < 3");
            return -1;
        }
    }

    private void turnOffScreen() {
        if (!HwPowerManager.isSystemSuspending() && !this.esdNeedInitStatus) {
            HwPowerManager.suspendSystem(true, true);
        }
    }

    private void turnOnScreen() {
        if (!this.mPowerManager.isScreenOn()) {
            HwPowerManager.suspendSystem(false, false);
            Log.i(TAG, "screen is realy off ,just set suspendSystem false , do not refresh background");
            return;
        }
        if (HwPowerManager.isSystemSuspending()) {
            HwPowerManager.suspendSystem(false, true);
        }
    }
}
