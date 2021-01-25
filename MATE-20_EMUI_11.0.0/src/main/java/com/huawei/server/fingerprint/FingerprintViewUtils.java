package com.huawei.server.fingerprint;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;
import com.huawei.android.util.HwNotchSizeUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class FingerprintViewUtils {
    private static final int ARRAY_LENGTH = 4;
    private static final int DEFAULT_DENSITY = 640;
    private static final int DEFAULT_LCD_DPI = 640;
    private static final int DEFAULT_RADIUS = 87;
    private static final String DISPLAY_NOTCH_STATUS = "display_notch_status";
    private static final int DIVIDE_SCALE = 2;
    private static final float FINGER_BUTTON_SIZE_TRANS_PARAMETER = 0.5f;
    private static final String FINGRPRINT_IMAGE_TITLE_NAME = "hw_ud_fingerprint_image";
    private static final int FLAG_FINGERPRINT_POSITION_MASK = 65535;
    private static final int INDEX_FOUR = 3;
    private static final int INDEX_ONE = 0;
    private static final int INDEX_THREE = 2;
    private static final int INDEX_TWO = 1;
    private static final int INVALID_VALUE = -1;
    private static final int MARGIN_SIZE = 2;
    private static final int MOVE_NUM = 16;
    private static final int NOTCH_SIZE = 2;
    private static final int NOTCH_STATUS_DEFAULT = 0;
    private static final int NOTCH_STATUS_HIDE = 1;
    private static final String PKGNAME_OF_KEYGUARD = "com.android.systemui";
    public static final int RATE_TO_NORMAL = 0;
    public static final int RATE_TO_SIXTY = 2;
    private static final String TAG = "FingerprintViewUtils";

    private FingerprintViewUtils() {
    }

    private static float getDpiScale() {
        int lcdDpi = SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 640));
        int dpi = SystemProperties.getInt("persist.sys.dpi", lcdDpi);
        int realdpi = SystemProperties.getInt("persist.sys.realdpi", dpi);
        float scale = (((float) lcdDpi) * 1.0f) / ((float) dpi);
        Log.i(TAG, "getDPIScale: lcdDpi: " + lcdDpi + " dpi: " + dpi + " realdpi: " + realdpi + " scale: " + scale);
        return scale;
    }

    private static float getPxScale() {
        int lcdDpi = SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 640));
        int dpi = SystemProperties.getInt("persist.sys.dpi", lcdDpi);
        int realdpi = SystemProperties.getInt("persist.sys.realdpi", dpi);
        float scale = (((float) realdpi) * 1.0f) / ((float) dpi);
        Log.i(TAG, "getPxScale: lcdDpi: " + lcdDpi + " dpi: " + dpi + " realdpi: " + realdpi + " scale: " + scale);
        return scale;
    }

    private static int getNotchState(Context context) {
        if (context != null) {
            return Settings.Secure.getIntForUser(context.getContentResolver(), "display_notch_status", 0, -2);
        }
        Log.e(TAG, "getNotchState context is null");
        return 0;
    }

    public static int getFingerprintDefaultLogoRadius(Context context) {
        if (context == null) {
            Log.e(TAG, "getFingerprintDefaultLogoRadius context is null");
            return DEFAULT_RADIUS;
        }
        int logoRadius = new BigDecimal((int) ((((float) context.getResources().getDimensionPixelSize(34472519)) * getDpiScale()) + 0.5f)).divide(new BigDecimal((double) (2.0f * getPxScale())), 2, RoundingMode.HALF_UP).intValue();
        Log.i(TAG, "getFingerprintDefaultLogoRadius logoRadius=" + logoRadius);
        return logoRadius;
    }

    public static int getFingerprintNotchHeight(Context context) {
        if (context == null) {
            Log.e(TAG, "getFingerprintNotchHeight context is null");
            return 0;
        }
        int notchHeight = 0;
        if (getNotchState(context) == 1) {
            int[] notchSizes = HwNotchSizeUtil.getNotchSize();
            if (notchSizes.length == 2) {
                notchHeight = new BigDecimal(notchSizes[1]).divide(new BigDecimal((double) getPxScale()), 2, RoundingMode.HALF_UP).intValue();
            }
        }
        Log.i(TAG, "getFingerprintNotchHeight notchHeight=" + notchHeight);
        return notchHeight;
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0078: APUT  
      (r4v1 'layoutMargins' int[] A[D('layoutMargins' int[])])
      (0 ??[int, short, byte, char])
      (r5v1 'leftmargin' int A[D('leftmargin' int)])
     */
    public static int[] calculateFingerprintLayoutLeftMargin(int width, float scale, int currentRotation, Context context, int fingerprintCenterX) {
        float dpiScale = getDpiScale();
        int notchHeight = getFingerprintNotchHeight(context);
        int[] layoutMargins = new int[2];
        int leftmargin = 0;
        int rightmargin = 0;
        if (currentRotation == 3) {
            leftmargin = (int) ((((float) context.getResources().getDimensionPixelSize(34472740)) * dpiScale) + 0.5f);
            rightmargin = ((int) (((((float) fingerprintCenterX) * scale) - ((float) (width / 2))) + 0.5f)) - notchHeight;
        } else if (currentRotation == 1) {
            BigDecimal scaleDegreeBig = new BigDecimal((double) getPxScale());
            leftmargin = (int) (((((float) fingerprintCenterX) * scale) - ((float) (width / 2))) + 0.5f);
            rightmargin = ((int) ((((float) context.getResources().getDimensionPixelSize(34472740)) * dpiScale) + 0.5f)) + new BigDecimal(notchHeight).multiply(scaleDegreeBig).intValue();
        } else {
            Log.i(TAG, "calculateFingerprintLayoutLeftMargin currentRotation = " + currentRotation);
        }
        layoutMargins[0] = leftmargin;
        layoutMargins[1] = rightmargin;
        return layoutMargins;
    }

    public static void setFingerprintOnlyLayoutParams(Context context, WindowManager.LayoutParams fingerprintOnlyLayoutParams, int windowType, int[] fingerprintMargin) {
        if (context == null || fingerprintOnlyLayoutParams == null) {
            Log.w(TAG, "setFingerprintOnlyLayoutParams context or fingerprintOnlyLayoutParams is null");
        } else if (fingerprintMargin.length <= 0) {
            Log.w(TAG, "setFingerprintOnlyLayoutParams fingerprintMargin is null");
        } else {
            fingerprintOnlyLayoutParams.flags = 16777480;
            fingerprintOnlyLayoutParams.privateFlags |= 16;
            fingerprintOnlyLayoutParams.format = -3;
            fingerprintOnlyLayoutParams.screenOrientation = 14;
            fingerprintOnlyLayoutParams.setTitle(FINGRPRINT_IMAGE_TITLE_NAME);
            fingerprintOnlyLayoutParams.gravity = 8388659;
            fingerprintOnlyLayoutParams.type = windowType;
            fingerprintOnlyLayoutParams.x = fingerprintMargin[0];
            fingerprintOnlyLayoutParams.width = (int) ((((float) context.getResources().getDimensionPixelSize(34472517)) * getDpiScale()) + 0.5f);
        }
    }

    public static int[] covertToIntArray(String str) {
        if (str == null) {
            return new int[0];
        }
        String[] arrs = str.split(",");
        int[] nums = new int[arrs.length];
        for (int i = 0; i < nums.length; i++) {
            try {
                nums[i] = Integer.parseInt(arrs[i]);
            } catch (NumberFormatException e) {
                Log.e(TAG, "covertToIntArray NumberFormatException");
            }
        }
        return nums;
    }

    public static int[] getFingerprintHardwarePosition(int[] result) {
        if (result[0] == -1) {
            int[] pxPositions = {-1, -1, -1, -1};
            String[] positionGs = SystemProperties.get("persist.sys.fingerprint.hardwarePosition", "-1,-1,-1,-1").split(",");
            try {
                if (positionGs.length < 4) {
                    return pxPositions;
                }
                pxPositions[0] = Integer.parseInt(positionGs[0]);
                pxPositions[1] = Integer.parseInt(positionGs[1]);
                pxPositions[2] = Integer.parseInt(positionGs[2]);
                pxPositions[3] = Integer.parseInt(positionGs[3]);
                Log.d(TAG, "getHardwarePosition from SystemProperties: " + pxPositions[0]);
                return pxPositions;
            } catch (NumberFormatException e) {
                Log.e(TAG, "getFingerprintHardwarePosition NumberFormatException");
            }
        } else {
            int[] parsedPositions = {-1, -1, -1, -1};
            parsedPositions[0] = result[0] >> 16;
            parsedPositions[1] = result[0] & 65535;
            parsedPositions[2] = result[1] >> 16;
            parsedPositions[3] = result[1] & 65535;
            return parsedPositions;
        }
    }

    public static void setScreenRefreshRate(Context context, int state, String packageName, boolean isFingerInScreenSupported, Handler handler) {
        if (!isFingerInScreenSupported) {
            Log.i(TAG, "setScreenRefreshRate not support in screen fingerprint");
        } else if ("com.android.systemui".equals(packageName)) {
            Log.i(TAG, "not setScreenRefreshRate in framework");
        } else if (context == null || packageName == null) {
            Log.w(TAG, "setScreenRefreshRate context or packageName is null");
        } else {
            FingerprintController.getInstance().setScreenRefreshRate(state, packageName, handler);
        }
    }

    public static boolean isForegroundActivity(String packageName) {
        try {
            List<ActivityManager.RunningAppProcessInfo> procsList = ActivityManager.getService().getRunningAppProcesses();
            if (procsList != null) {
                if (!procsList.isEmpty()) {
                    for (ActivityManager.RunningAppProcessInfo proc : procsList) {
                        if (proc.processName.equals(packageName) && proc.importance == 100) {
                            return true;
                        }
                    }
                    return false;
                }
            }
            Log.i(TAG, "isForegroundActivity RunningAppProcessInfo is null");
            return false;
        } catch (RemoteException e) {
            Log.w(TAG, "am.getRunningAppProcesses() failed");
        }
    }

    public static String getForegroundActivity() {
        try {
            List<ActivityManager.RunningAppProcessInfo> procsList = ActivityManager.getService().getRunningAppProcesses();
            if (procsList != null) {
                if (!procsList.isEmpty()) {
                    for (ActivityManager.RunningAppProcessInfo proc : procsList) {
                        if (proc.importance == 100) {
                            Log.d(TAG, "foreground processName = " + proc.processName);
                            return proc.processName;
                        }
                    }
                    return "";
                }
            }
            Log.i(TAG, "getForegroundActivity RunningAppProcessInfo is null");
            return "";
        } catch (RemoteException e) {
            Log.w(TAG, "am.getRunningAppProcesses() failed");
        }
    }
}
