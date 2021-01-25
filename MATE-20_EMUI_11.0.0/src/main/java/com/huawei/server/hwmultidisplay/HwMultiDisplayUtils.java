package com.huawei.server.hwmultidisplay;

import android.app.ActivityManager;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.RemoteException;
import android.pc.IHwPCManager;
import android.util.HwPCUtils;
import android.util.Log;
import android.view.Display;
import com.huawei.android.inputmethod.HwInputMethodManager;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.view.DisplayEx;
import java.util.Iterator;
import java.util.List;

public class HwMultiDisplayUtils extends DefaultHwMultiDisplayUtils {
    private static final Object LOCK = new Object();
    private static final boolean SINK_WINDOWS_CAST_ENABLED = "tablet".equals(SystemPropertiesEx.get("ro.build.characteristics", "default"));
    private static final String TAG = "HwMultiDisplayUtils";
    private static final String WINDOWS_CAST_DISPLAY_NAME = "HiSightPCDisplay";
    private static final boolean WINDOWS_CAST_ENABLED = SystemPropertiesEx.getBoolean("ro.config.hw_emui_cast_mode", false);
    private static boolean mSupportOverlay = SystemPropertiesEx.getBoolean("hw_pc_support_windows_cast_overlay", false);
    private static HwMultiDisplayUtils sInstance = null;
    private static boolean sIsInBasicMode = false;
    private static boolean sIsSinkInWindowsCastMode = false;
    private static boolean sIsSinkKeyboardExist = true;
    private static boolean sIsWindowsCastMode = false;

    private HwMultiDisplayUtils() {
    }

    public static HwMultiDisplayUtils getInstance() {
        HwMultiDisplayUtils hwMultiDisplayUtils;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new HwMultiDisplayUtils();
            }
            hwMultiDisplayUtils = sInstance;
        }
        return hwMultiDisplayUtils;
    }

    public boolean isInWindowsCastMode() {
        return sIsWindowsCastMode && WINDOWS_CAST_ENABLED;
    }

    public static void setIsWindowsCastMode(boolean mode) {
        sIsWindowsCastMode = mode;
    }

    public boolean isInSinkWindowsCastMode() {
        return sIsSinkInWindowsCastMode && SINK_WINDOWS_CAST_ENABLED;
    }

    public static void setIsInSinkWindowsCastMode(boolean isInCastMode) {
        sIsSinkInWindowsCastMode = isInCastMode;
    }

    public static boolean isSinkHasKeyboard() {
        return sIsSinkKeyboardExist;
    }

    public static void setIsSinkHasKeyboard(boolean isKeyboardExist) {
        if (sIsSinkKeyboardExist != isKeyboardExist) {
            sIsSinkKeyboardExist = isKeyboardExist;
            HwInputMethodManager.restartInputMethodForMultiDisplay();
        }
    }

    public static String getWindowsCastDisplayName() {
        return WINDOWS_CAST_DISPLAY_NAME;
    }

    public static boolean isConnectedToWindows(Context context, int displayId) {
        if (!WINDOWS_CAST_ENABLED) {
            return false;
        }
        if (context == null || displayId == 0) {
            Log.e(TAG, "isConnectedToWindows context may be null.");
            return false;
        }
        Display display = ((DisplayManager) context.getSystemService("display")).getDisplay(displayId);
        if (display == null) {
            Log.e(TAG, "isConnectedToWindows error, display is null.");
            return false;
        }
        int type = DisplayEx.getType(display);
        if (WINDOWS_CAST_DISPLAY_NAME.equals(display.getName()) || ((type == 5 || type == 4) && mSupportOverlay)) {
            return true;
        }
        return false;
    }

    public boolean isScreenOnForHwMultiDisplay() {
        if (!HwPCUtils.isInWindowsCastMode() && !HwPCUtils.isDisallowLockScreenForHwMultiDisplay()) {
            return true;
        }
        try {
            IHwPCManager pcMgr = HwPCUtils.getHwPCManager();
            if (pcMgr == null || pcMgr.isScreenPowerOn()) {
                return true;
            }
            return false;
        } catch (RemoteException e) {
            HwPCUtils.log(TAG, "isScreenOnForHwMultiDisplay RemoteException");
            return true;
        }
    }

    public void lightScreenOnForHwMultiDisplay() {
        if (HwPCUtils.isInWindowsCastMode() || HwPCUtils.isDisallowLockScreenForHwMultiDisplay()) {
            try {
                IHwPCManager pcMgr = HwPCUtils.getHwPCManager();
                if (pcMgr != null && !pcMgr.isScreenPowerOn()) {
                    pcMgr.setScreenPower(true);
                }
            } catch (RemoteException e) {
                HwPCUtils.log(TAG, "lightScreenOnForHwMultiDisplay RemoteException");
            }
        }
    }

    public static String getPackageNameByPid(Context context, int pid) {
        ActivityManager activityManager;
        List<ActivityManager.RunningAppProcessInfo> appProcesses;
        if (pid <= 0 || (activityManager = (ActivityManager) context.getSystemService("activity")) == null || (appProcesses = activityManager.getRunningAppProcesses()) == null) {
            return null;
        }
        String packageName = null;
        Iterator<ActivityManager.RunningAppProcessInfo> it = appProcesses.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            ActivityManager.RunningAppProcessInfo appProcess = it.next();
            if (appProcess.pid == pid) {
                packageName = appProcess.processName;
                break;
            }
        }
        int indexProcessFlag = -1;
        if (packageName != null) {
            indexProcessFlag = packageName.indexOf(58);
        }
        return indexProcessFlag > 0 ? packageName.substring(0, indexProcessFlag) : packageName;
    }

    public static void setIsInBasicMode(boolean isInBasicMode) {
        sIsInBasicMode = isInBasicMode;
    }

    public boolean isInBasicMode() {
        return sIsInBasicMode && SINK_WINDOWS_CAST_ENABLED;
    }
}
