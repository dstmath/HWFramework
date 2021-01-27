package com.huawei.server.fingerprint;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.Log;
import android.view.WindowManager;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.biometric.FingerprintSupportEx;
import com.huawei.android.os.HwPowerManager;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.provider.SettingsEx;
import com.huawei.android.util.HwNotchSizeUtil;
import com.huawei.hwpartfingerprintopt.BuildConfig;
import com.huawei.utils.HwPartResourceUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class FingerprintViewUtils {
    private static final int AOD_SOLUTION_SUPPORT = SystemPropertiesEx.getInt("ro.config.support_aod", 0);
    private static final int AOD_SOLUTION_SUPPORT_FLAG = 2;
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
    private static final int INITIAL_BRIGHTNESS = -1;
    private static final int INVALID_VALUE = -1;
    private static final boolean IS_SUPPORT_APP_SOLUTION;
    private static final int MARGIN_SIZE = 2;
    private static final int MOVE_NUM = 16;
    private static final int NOTCH_SIZE = 2;
    private static final int NOTCH_STATUS_DEFAULT = 0;
    private static final int NOTCH_STATUS_HIDE = 1;
    private static final String PKGNAME_OF_KEYGUARD = "com.android.systemui";
    public static final int RATE_TO_NORMAL = 0;
    public static final int RATE_TO_SIXTY = 2;
    private static final String TAG = "FingerprintViewUtils";

    static {
        boolean z = false;
        if (AOD_SOLUTION_SUPPORT == 2) {
            z = true;
        }
        IS_SUPPORT_APP_SOLUTION = z;
    }

    private FingerprintViewUtils() {
    }

    private static float getDpiScale() {
        int lcdDpi = SystemPropertiesEx.getInt("ro.sf.real_lcd_density", SystemPropertiesEx.getInt("ro.sf.lcd_density", 640));
        int dpi = SystemPropertiesEx.getInt("persist.sys.dpi", lcdDpi);
        int realdpi = SystemPropertiesEx.getInt("persist.sys.realdpi", dpi);
        float scale = (((float) lcdDpi) * 1.0f) / ((float) dpi);
        Log.i(TAG, "getDPIScale: lcdDpi: " + lcdDpi + " dpi: " + dpi + " realdpi: " + realdpi + " scale: " + scale);
        return scale;
    }

    private static float getPxScale() {
        int lcdDpi = SystemPropertiesEx.getInt("ro.sf.real_lcd_density", SystemPropertiesEx.getInt("ro.sf.lcd_density", 640));
        int dpi = SystemPropertiesEx.getInt("persist.sys.dpi", lcdDpi);
        int realdpi = SystemPropertiesEx.getInt("persist.sys.realdpi", dpi);
        float scale = (((float) realdpi) * 1.0f) / ((float) dpi);
        Log.i(TAG, "getPxScale: lcdDpi: " + lcdDpi + " dpi: " + dpi + " realdpi: " + realdpi + " scale: " + scale);
        return scale;
    }

    private static int getNotchState(Context context) {
        if (context != null) {
            return SettingsEx.Secure.getIntForUser(context.getContentResolver(), DISPLAY_NOTCH_STATUS, 0, -2);
        }
        Log.e(TAG, "getNotchState context is null");
        return 0;
    }

    public static int getFingerprintDefaultLogoRadius(Context context) {
        if (context == null) {
            Log.e(TAG, "getFingerprintDefaultLogoRadius context is null");
            return DEFAULT_RADIUS;
        }
        int logoRadius = new BigDecimal((int) ((((float) context.getResources().getDimensionPixelSize(HwPartResourceUtils.getResourceId("finger_print_view_width"))) * getDpiScale()) + FINGER_BUTTON_SIZE_TRANS_PARAMETER)).divide(new BigDecimal((double) (2.0f * getPxScale())), 2, RoundingMode.HALF_UP).intValue();
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

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x007f: APUT  
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
            leftmargin = (int) ((((float) context.getResources().getDimensionPixelSize(HwPartResourceUtils.getResourceId("navigationbar_height"))) * dpiScale) + FINGER_BUTTON_SIZE_TRANS_PARAMETER);
            rightmargin = ((int) (((((float) fingerprintCenterX) * scale) - ((float) (width / 2))) + FINGER_BUTTON_SIZE_TRANS_PARAMETER)) - notchHeight;
        } else if (currentRotation == 1) {
            BigDecimal scaleDegreeBig = new BigDecimal((double) getPxScale());
            BigDecimal notchHeightDegreeBig = new BigDecimal(notchHeight);
            leftmargin = (int) (((((float) fingerprintCenterX) * scale) - ((float) (width / 2))) + FINGER_BUTTON_SIZE_TRANS_PARAMETER);
            rightmargin = ((int) ((((float) context.getResources().getDimensionPixelSize(HwPartResourceUtils.getResourceId("navigationbar_height"))) * dpiScale) + FINGER_BUTTON_SIZE_TRANS_PARAMETER)) + notchHeightDegreeBig.multiply(scaleDegreeBig).intValue();
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
            FingerprintSupportEx.setLayoutParamsPrivateFlags(fingerprintOnlyLayoutParams, FingerprintSupportEx.getPrivateFlagShowForAllUsers());
            fingerprintOnlyLayoutParams.format = -3;
            fingerprintOnlyLayoutParams.screenOrientation = 14;
            fingerprintOnlyLayoutParams.setTitle(FINGRPRINT_IMAGE_TITLE_NAME);
            fingerprintOnlyLayoutParams.gravity = 8388659;
            fingerprintOnlyLayoutParams.type = windowType;
            fingerprintOnlyLayoutParams.x = fingerprintMargin[0];
            fingerprintOnlyLayoutParams.width = (int) ((((float) context.getResources().getDimensionPixelSize(HwPartResourceUtils.getResourceId("finger_print_view_for_alipay_width"))) * getDpiScale()) + FINGER_BUTTON_SIZE_TRANS_PARAMETER);
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
            String[] positionGs = SystemPropertiesEx.get("persist.sys.fingerprint.hardwarePosition", "-1,-1,-1,-1").split(",");
            try {
                if (positionGs.length < 4) {
                    return pxPositions;
                }
                pxPositions[0] = Integer.parseInt(positionGs[0]);
                pxPositions[1] = Integer.parseInt(positionGs[1]);
                pxPositions[2] = Integer.parseInt(positionGs[2]);
                pxPositions[3] = Integer.parseInt(positionGs[3]);
                Log.d(TAG, "getHardwarePosition from SystemPropertiesEx: " + pxPositions[0]);
                return pxPositions;
            } catch (NumberFormatException e) {
                Log.e(TAG, "getFingerprintHardwarePosition NumberFormatException");
            }
        } else {
            int[] parsedPositions = {-1, -1, -1, -1};
            parsedPositions[0] = result[0] >> MOVE_NUM;
            parsedPositions[1] = result[0] & FLAG_FINGERPRINT_POSITION_MASK;
            parsedPositions[2] = result[1] >> MOVE_NUM;
            parsedPositions[3] = result[1] & FLAG_FINGERPRINT_POSITION_MASK;
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
            List<ActivityManager.RunningAppProcessInfo> procsList = ActivityManagerEx.getRunningAppProcesses();
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
            List<ActivityManager.RunningAppProcessInfo> procsList = ActivityManagerEx.getRunningAppProcesses();
            if (procsList != null) {
                if (!procsList.isEmpty()) {
                    for (ActivityManager.RunningAppProcessInfo proc : procsList) {
                        if (proc.importance == 100) {
                            Log.d(TAG, "foreground processName = " + proc.processName);
                            return proc.processName;
                        }
                    }
                    return BuildConfig.FLAVOR;
                }
            }
            Log.i(TAG, "getForegroundActivity RunningAppProcessInfo is null");
            return BuildConfig.FLAVOR;
        } catch (RemoteException e) {
            Log.w(TAG, "am.getRunningAppProcesses() failed");
        }
    }

    public static boolean isSupportAppAodSolution() {
        return IS_SUPPORT_APP_SOLUTION;
    }

    public static boolean isScreenOff(PowerManager powerManager) {
        if (powerManager == null) {
            return false;
        }
        if (!powerManager.isInteractive()) {
            Log.i(TAG, "screen is off");
            return true;
        } else if (getBrightness() != 0) {
            return false;
        } else {
            Log.i(TAG, "brightness is not set");
            return true;
        }
    }

    public static int getBrightness() {
        int currentBrightness;
        Bundle data = new Bundle();
        if (HwPowerManager.getHwBrightnessData("CurrentBrightness", data) != 0) {
            currentBrightness = -1;
            Log.w(TAG, "get currentBrightness failed!");
        } else {
            currentBrightness = data.getInt("Brightness");
        }
        Log.i(TAG, "currentBrightness=" + currentBrightness);
        return currentBrightness;
    }

    public static boolean isAppAodMode(PowerManager powerManager) {
        if (!IS_SUPPORT_APP_SOLUTION || !isScreenOff(powerManager)) {
            return false;
        }
        return true;
    }

    public static boolean isApModeAodEnable(Context context) {
        if (context == null || !IS_SUPPORT_APP_SOLUTION) {
            return false;
        }
        ContentResolver resolver = context.getContentResolver();
        int currentUser = ActivityManagerEx.getCurrentUser();
        boolean isAodEnable = SettingsEx.Secure.getIntForUser(resolver, "aod_switch", 0, currentUser) == 1;
        boolean isFpEnable = SettingsEx.Secure.getIntForUser(resolver, "fp_keyguard_enable", 0, currentUser) == 1;
        if (isAodEnable || isFpEnable) {
            return true;
        }
        return false;
    }

    public static boolean isFingerprintEnabledInAppMode(Context context) {
        if (context == null || !IS_SUPPORT_APP_SOLUTION || SettingsEx.Secure.getIntForUser(context.getContentResolver(), "fp_keyguard_enable", 0, ActivityManagerEx.getCurrentUser()) != 1) {
            return false;
        }
        return true;
    }

    public static boolean isSupportSurfaceAnimation(List<String> animFileNames) {
        if (!isSupportAppAodSolution()) {
            return false;
        }
        if (animFileNames != null && !animFileNames.isEmpty()) {
            return true;
        }
        Log.i(TAG, "animFileNames empty");
        return false;
    }
}
