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
    private static boolean sIsFirstCheck = true;
    private static boolean sIsSupportApsSdr = false;
    public static long slastSetPropertyTimeStamp = 0;
    public float mRatio = 2.0f;

    static {
        boolean z = true;
        if ((SystemProperties.getInt("sys.aps.support", 0) & 278528) == 0) {
            z = false;
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
        if (sIsFirstCheck) {
            if (2048 == (SystemProperties.getInt("sys.aps.support", 0) & 2048)) {
                sIsSupportApsSdr = true;
            } else {
                ApsCommon.logI(TAG, "SDR: control: Dcr module is not supported");
            }
            sIsFirstCheck = false;
        }
        return sIsSupportApsSdr;
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

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0049  */
    private static void setPropertyForKeyCode(int keyCode) {
        boolean needSetPropertyForKeyCode;
        long currentTime = SystemClock.uptimeMillis();
        int i = 1;
        boolean isGame = !SystemProperties.get("sys.aps.gameProcessName", "").isEmpty();
        boolean isBrowser = false;
        if (!isGame) {
            isBrowser = !SystemProperties.get("sys.aps.browserProcessName", "").isEmpty();
            if (!isBrowser) {
                return;
            }
        }
        if (slastSetPropertyTimeStamp != 0) {
            if (currentTime - slastSetPropertyTimeStamp <= ((long) (isGame ? 5000 : 2500))) {
                needSetPropertyForKeyCode = false;
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
                    StringBuilder sb = new StringBuilder();
                    sb.append("SDR: Controller, setPropertyForKeyCode 2.5/5s, isGame:");
                    sb.append(isGame ? 1 : 0);
                    sb.append(", isBrowser:");
                    if (!isBrowser) {
                        i = 0;
                    }
                    sb.append(i);
                    sb.append(", keycode: ");
                    sb.append(keyCode);
                    sb.append(", msg: ");
                    sb.append(msg);
                    ApsCommon.logD(TAG, sb.toString());
                }
            }
        }
        needSetPropertyForKeyCode = true;
        if (needSetPropertyForKeyCode) {
        }
    }
}
