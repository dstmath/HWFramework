package com.huawei.android.view;

import android.graphics.Rect;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.os.storage.StorageManagerExt;

public class HwExtDisplaySizeUtil implements IHwExtDisplaySizeUtil {
    private static final String NOTCH_PROP = SystemProperties.get("ro.config.hw_notch_size", StorageManagerExt.INVALID_KEY_DESC);
    private static final int SIDE_MODE = SystemProperties.getInt("hw_mc.ring.side_touch_mode", 0);
    private static final String SIDE_PROP = SystemProperties.get("ro.config.hw_curved_side_disp", StorageManagerExt.INVALID_KEY_DESC);
    public static final int SIDE_TOUCH_NONE = 0;
    public static final int SIDE_TOUCH_WITHOUT_SOLID = 1;
    public static final int SIDE_TOUCH_WITH_SOLID = 2;
    private static final String TAG = "HwExtDisplaySizeUtil";
    private static HwExtDisplaySizeUtil sInstance;
    private static int[] sNotchParams;
    private static int[] sSideParams;

    static {
        try {
            if (hasNotchInScreenInner()) {
                String[] params = NOTCH_PROP.split(",");
                int length = params.length;
                if (length < 4) {
                    Log.e(TAG, "hw_notch_size conifg error");
                    sNotchParams = null;
                } else {
                    sNotchParams = new int[length];
                    for (int i = 0; i < length; i++) {
                        sNotchParams[i] = Integer.parseInt(params[i]);
                    }
                }
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "hw_curved_side_disp conifg NumberFormatException");
            sNotchParams = null;
        } catch (Exception e2) {
            Log.e(TAG, "hw_curved_side_disp conifg Exception");
            sNotchParams = null;
        }
        try {
            if (hasSideInScreenInner()) {
                String[] params2 = SIDE_PROP.split(",");
                int length2 = params2.length;
                if (length2 < 4) {
                    Log.e(TAG, "hw_curved_side_disp conifg error");
                    sSideParams = null;
                    return;
                }
                sSideParams = new int[length2];
                for (int i2 = 0; i2 < length2; i2++) {
                    sSideParams[i2] = Integer.parseInt(params2[i2]);
                }
            }
        } catch (NumberFormatException e3) {
            Log.e(TAG, "hw_notch_size conifg NumberFormatException");
            sSideParams = null;
        } catch (Exception e4) {
            Log.e(TAG, "hw_notch_size conifg Exception");
            sSideParams = null;
        }
    }

    private HwExtDisplaySizeUtil() {
    }

    public static synchronized HwExtDisplaySizeUtil getInstance() {
        HwExtDisplaySizeUtil hwExtDisplaySizeUtil;
        synchronized (HwExtDisplaySizeUtil.class) {
            if (sInstance == null) {
                sInstance = new HwExtDisplaySizeUtil();
            }
            hwExtDisplaySizeUtil = sInstance;
        }
        return hwExtDisplaySizeUtil;
    }

    public boolean hasSideInScreen() {
        return hasSideInScreenInner();
    }

    private static boolean hasSideInScreenInner() {
        return !TextUtils.isEmpty(SIDE_PROP);
    }

    public boolean hasNotchInScreen() {
        return hasNotchInScreenInner();
    }

    private static boolean hasNotchInScreenInner() {
        return !TextUtils.isEmpty(NOTCH_PROP);
    }

    public int getSideTouchMode() {
        if (!hasSideInScreenInner()) {
            return 0;
        }
        int i = SIDE_MODE;
        if (i != 1 && i == 2) {
            return 2;
        }
        return 1;
    }

    public Rect getDisplaySafeInsets() {
        Rect side = getDisplaySideSafeInsets();
        Rect cutout = getDisplayCutoutSafeInsets();
        return new Rect(side.left > cutout.left ? side.left : cutout.left, side.top > cutout.top ? side.top : cutout.top, side.right > cutout.right ? side.right : cutout.right, side.bottom > cutout.bottom ? side.bottom : cutout.bottom);
    }

    public Rect getDisplaySideSafeInsets() {
        int[] iArr = sSideParams;
        if (iArr == null) {
            return new Rect();
        }
        return new Rect(iArr[0], 0, iArr[2], 0);
    }

    private Rect getDisplayCutoutSafeInsets() {
        int[] iArr = sNotchParams;
        if (iArr == null) {
            return new Rect();
        }
        return new Rect(0, iArr[1], 0, 0);
    }
}
