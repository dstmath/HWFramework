package com.huawei.android.view;

import android.graphics.Rect;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.os.storage.StorageManagerExt;

public class HwExtDisplaySizeUtil implements IHwExtDisplaySizeUtil {
    private static int[] NOTCH_PARAMS = null;
    private static int[] SIDE_PARAMS = null;
    public static final int SIDE_TOUCH_NONE = 0;
    public static final int SIDE_TOUCH_WITHOUT_SOLID = 1;
    public static final int SIDE_TOUCH_WITH_SOLID = 2;
    private static final String TAG = "HwExtDisplaySizeUtil";
    private static HwExtDisplaySizeUtil mInstance;
    private static final String mNotchProp = SystemProperties.get("ro.config.hw_notch_size", StorageManagerExt.INVALID_KEY_DESC);
    private static final int mSideMode = SystemProperties.getInt("hw_mc.ring.side_touch_mode", 0);
    private static final String mSideProp = SystemProperties.get("ro.config.hw_curved_side_disp", StorageManagerExt.INVALID_KEY_DESC);

    static {
        try {
            if (hasNotchInScreenInner()) {
                String[] params = mNotchProp.split(",");
                int length = params.length;
                if (length < 4) {
                    Log.e(TAG, "hw_notch_size conifg error");
                    NOTCH_PARAMS = null;
                } else {
                    NOTCH_PARAMS = new int[length];
                    for (int i = 0; i < length; i++) {
                        NOTCH_PARAMS[i] = Integer.parseInt(params[i]);
                    }
                }
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "hw_curved_side_disp conifg NumberFormatException");
            NOTCH_PARAMS = null;
        } catch (Exception e2) {
            Log.e(TAG, "hw_curved_side_disp conifg Exception");
            NOTCH_PARAMS = null;
        }
        try {
            if (hasSideInScreenInner()) {
                String[] params2 = mSideProp.split(",");
                int length2 = params2.length;
                if (length2 < 4) {
                    Log.e(TAG, "hw_curved_side_disp conifg error");
                    SIDE_PARAMS = null;
                    return;
                }
                SIDE_PARAMS = new int[length2];
                for (int i2 = 0; i2 < length2; i2++) {
                    SIDE_PARAMS[i2] = Integer.parseInt(params2[i2]);
                }
            }
        } catch (NumberFormatException e3) {
            Log.e(TAG, "hw_notch_size conifg NumberFormatException");
            SIDE_PARAMS = null;
        } catch (Exception e4) {
            Log.e(TAG, "hw_notch_size conifg Exception");
            SIDE_PARAMS = null;
        }
    }

    public static synchronized HwExtDisplaySizeUtil getInstance() {
        HwExtDisplaySizeUtil hwExtDisplaySizeUtil;
        synchronized (HwExtDisplaySizeUtil.class) {
            if (mInstance == null) {
                mInstance = new HwExtDisplaySizeUtil();
            }
            hwExtDisplaySizeUtil = mInstance;
        }
        return hwExtDisplaySizeUtil;
    }

    private HwExtDisplaySizeUtil() {
    }

    public boolean hasSideInScreen() {
        return hasSideInScreenInner();
    }

    private static boolean hasSideInScreenInner() {
        return !TextUtils.isEmpty(mSideProp);
    }

    public boolean hasNotchInScreen() {
        return hasNotchInScreenInner();
    }

    private static boolean hasNotchInScreenInner() {
        return !TextUtils.isEmpty(mNotchProp);
    }

    public int getSideTouchMode() {
        if (!hasSideInScreenInner()) {
            return 0;
        }
        int i = mSideMode;
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
        int[] iArr = SIDE_PARAMS;
        if (iArr == null) {
            return new Rect();
        }
        return new Rect(iArr[0], 0, iArr[2], 0);
    }

    private Rect getDisplayCutoutSafeInsets() {
        int[] iArr = NOTCH_PARAMS;
        if (iArr == null) {
            return new Rect();
        }
        return new Rect(0, iArr[1], 0, 0);
    }
}
