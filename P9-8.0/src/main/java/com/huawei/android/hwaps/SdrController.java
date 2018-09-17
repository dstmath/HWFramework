package com.huawei.android.hwaps;

import android.app.HwApsInterface;
import android.os.SystemClock;
import android.os.SystemProperties;

public class SdrController {
    public static final int KEYCODE_BACK = 4;
    public static final int KEYCODE_HOME = 3;
    public static final int KEYCODE_MENU = 82;
    public static final int KEYCODE_POWER = 26;
    private static final String TAG = "SdrController";
    private static boolean mIsModuleTurnOn;
    private static SdrController sInstance = null;
    public static long slastSetPropertyTimeStamp = 0;
    public float mRatio = 2.0f;

    static {
        boolean z = false;
        if ((SystemProperties.getInt("sys.aps.support", 0) & 278528) != 0) {
            z = true;
        }
        mIsModuleTurnOn = z;
    }

    public static synchronized SdrController getInstance() {
        SdrController sdrController;
        synchronized (SdrController.class) {
            if (sInstance == null) {
                sInstance = new SdrController();
            }
            sdrController = sInstance;
        }
        return sdrController;
    }

    public static boolean isSupportApsSdr() {
        if (2048 == (SystemProperties.getInt("sys.aps.support", 0) & 2048)) {
            return true;
        }
        ApsCommon.logI(TAG, "SDR: control: Dcr module is not supported");
        return false;
    }

    public void startSdr() {
        HwApsInterface.nativeStartSdr(this.mRatio);
        ApsCommon.logD(TAG, "SDR: control: start zoom");
    }

    public void stopSdr() {
        HwApsInterface.nativeStopSdr();
        ApsCommon.logD(TAG, "SDR: control: stop zoom");
    }

    public void stopSdrImmediately() {
        HwApsInterface.nativeStopSdrImmediately();
        ApsCommon.logD(TAG, "SDR: control: stop zoom immediately");
    }

    public void setSdrRatio(float ratio) {
        this.mRatio = ratio;
        HwApsInterface.nativeSetSdrRatio(ratio);
        ApsCommon.logD(TAG, "SDR: control: setSdrRatio  : " + this.mRatio);
    }

    public float getCurrentSdrRatio() {
        return HwApsInterface.nativeGetCurrentSdrRatio();
    }

    public boolean IsSdrCase() {
        boolean isSdrCase = HwApsInterface.nativeIsSdrCase();
        ApsCommon.logD(TAG, "SDR: control: check if sdr can be run. [result:" + isSdrCase + "]");
        return isSdrCase;
    }

    public static boolean StopSdrForSpecial(String info, int keyCode) {
        if (4 == keyCode && mIsModuleTurnOn) {
            setPropertyForKeyCode(keyCode);
        }
        return true;
    }

    private static void setPropertyForKeyCode(int keyCode) {
        boolean needSetPropertyForKeyCode;
        long currentTime = SystemClock.uptimeMillis();
        boolean isGame = SystemProperties.get("sys.aps.gameProcessName", "").isEmpty() ^ 1;
        boolean isBrowser = false;
        if (!isGame) {
            isBrowser = SystemProperties.get("sys.aps.browserProcessName", "").isEmpty() ^ 1;
            if (!isBrowser) {
                return;
            }
        }
        if (slastSetPropertyTimeStamp != 0) {
            needSetPropertyForKeyCode = currentTime - slastSetPropertyTimeStamp > ((long) (isGame ? 5000 : 2500));
        } else {
            needSetPropertyForKeyCode = true;
        }
        if (needSetPropertyForKeyCode) {
            final String msg = Long.toString(currentTime) + "|" + Integer.toString(keyCode);
            try {
                new Thread(new Runnable() {
                    public void run() {
                        SystemProperties.set("sys.aps.keycode", msg);
                    }
                }).start();
            } catch (Exception e) {
                ApsCommon.logD(TAG, "SDR: Controller, setPropertyForKeyCode failed to setproperties." + e);
            }
            slastSetPropertyTimeStamp = currentTime;
            ApsCommon.logD(TAG, "SDR: Controller, setPropertyForKeyCode 2.5/5s, isGame:" + (isGame ? 1 : 0) + ", isBrowser:" + (isBrowser ? 1 : 0) + ", keycode: " + keyCode + ", msg: " + msg);
        }
    }
}
