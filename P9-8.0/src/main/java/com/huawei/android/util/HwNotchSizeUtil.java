package com.huawei.android.util;

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
            Log.e(TAG, "hw_notch_size conifg Exception " + e2.toString());
            NOTCH_PARAMS = null;
        }
    }

    public static boolean hasNotchInScreen() {
        return TextUtils.isEmpty(mNotchProp) ^ 1;
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
            Log.w(TAG, "calculateSize error mRogWidth = " + rogWidth + "mRogWHeigth = " + rogHeight + ", mDefaultWidth = " + defaultWidth);
            return size;
        }
        int min = rogWidth < rogHeight ? rogWidth : rogHeight;
        if (defaultWidth != min) {
            size = (size * min) / defaultWidth;
        }
        Log.d(TAG, "mRogWidth = " + rogWidth + "mRogWHeigth = " + rogHeight + ", mDefaultWidth = " + defaultWidth + "size = " + size);
        return size;
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
