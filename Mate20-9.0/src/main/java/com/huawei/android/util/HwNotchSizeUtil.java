package com.huawei.android.util;

import android.app.ActivityThread;
import android.app.Application;
import android.aps.IApsManager;
import android.common.HwFrameworkFactory;
import android.graphics.Point;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
import com.huawei.android.smcs.SmartTrimProcessEvent;

public class HwNotchSizeUtil {
    private static int[] NOTCH_PARAMS;
    private static String TAG = "HwNotchSizeUtil";
    private static final String mNotchProp = SystemProperties.get("ro.config.hw_notch_size", "");
    private static int sDefaultWidth = 0;

    static {
        try {
            if (hasNotchInScreen()) {
                String[] params = mNotchProp.split(SmartTrimProcessEvent.ST_EVENT_STRING_TOKEN);
                int length = params.length;
                if (length < 4) {
                    Log.e(TAG, "hw_notch_size conifg error");
                    NOTCH_PARAMS = null;
                    return;
                }
                NOTCH_PARAMS = new int[length];
                for (int i = 0; i < length; i++) {
                    NOTCH_PARAMS[i] = Integer.parseInt(params[i]);
                }
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "hw_notch_size conifg NumberFormatException");
            NOTCH_PARAMS = null;
        } catch (Exception e2) {
            String str = TAG;
            Log.e(str, "hw_notch_size conifg Exception " + e2.toString());
            NOTCH_PARAMS = null;
        }
    }

    public static boolean hasNotchInScreen() {
        return !TextUtils.isEmpty(mNotchProp);
    }

    public static int[] getNotchSize() {
        if (NOTCH_PARAMS == null) {
            return new int[]{0, 0};
        }
        return new int[]{calculateSize(NOTCH_PARAMS[0]), calculateSize(NOTCH_PARAMS[1])};
    }

    public static int getNotchOffset() {
        if (NOTCH_PARAMS == null) {
            return 0;
        }
        return calculateSize(NOTCH_PARAMS[2]);
    }

    public static int getNotchCorner() {
        if (NOTCH_PARAMS == null) {
            return 0;
        }
        return calculateSize(NOTCH_PARAMS[3]);
    }

    private static int calculateSize(int size) {
        int defaultWidth = getDefaultWidth();
        int rogWidth = SystemProperties.getInt("persist.sys.rog.width", 0);
        int rogHeight = SystemProperties.getInt("persist.sys.rog.height", 0);
        if (defaultWidth == 0 || rogWidth == 0 || rogHeight == 0) {
            int resolutionSize = getResolutionSize(size);
            String str = TAG;
            Log.w(str, "calculateSize error mRogWidth = " + rogWidth + "mRogWHeigth = " + rogHeight + ", mDefaultWidth = " + defaultWidth + ",size = " + size + ",resolutionSize =" + resolutionSize);
            return resolutionSize;
        }
        int min = rogWidth < rogHeight ? rogWidth : rogHeight;
        if (defaultWidth != min) {
            size = (int) (((((float) (size * min)) * 1.0f) / ((float) defaultWidth)) + 0.5f);
        }
        int resolutionSize2 = getResolutionSize(size);
        String str2 = TAG;
        Log.d(str2, "mRogWidth = " + rogWidth + "mRogWHeigth = " + rogHeight + ", mDefaultWidth = " + defaultWidth + ",size = " + size + ",resolutionSize =" + resolutionSize2);
        return resolutionSize2;
    }

    private static int getResolutionSize(int size) {
        Application application = ActivityThread.currentApplication();
        if (application == null) {
            return size;
        }
        String packagename = application.getPackageName();
        float resolutionRatio = -1.0f;
        try {
            IApsManager Apsmanager = HwFrameworkFactory.getApsManager();
            if (Apsmanager != null) {
                resolutionRatio = Apsmanager.getResolution(packagename);
            }
            if (0.0f < resolutionRatio && resolutionRatio < 1.0f) {
                size = (int) ((((float) size) * resolutionRatio) + 0.5f);
                String str = TAG;
                Log.d(str, "getResolutionSize resolutionRatio " + resolutionRatio);
            }
            return size;
        } catch (Exception e) {
            String str2 = TAG;
            Log.e(str2, "getResolutionSize exception " + e.toString());
            return size;
        }
    }

    private static synchronized int getDefaultWidth() {
        int i;
        synchronized (HwNotchSizeUtil.class) {
            if (sDefaultWidth == 0) {
                IWindowManager iwm = WindowManagerGlobal.getWindowManagerService();
                if (iwm != null) {
                    Point point = new Point();
                    try {
                        iwm.getInitialDisplaySize(0, point);
                        sDefaultWidth = point.x < point.y ? point.x : point.y;
                    } catch (RemoteException e) {
                        Log.e(TAG, "RemoteException while calculate device size", e);
                        sDefaultWidth = 0;
                    }
                }
            }
            i = sDefaultWidth;
        }
        return i;
    }
}
