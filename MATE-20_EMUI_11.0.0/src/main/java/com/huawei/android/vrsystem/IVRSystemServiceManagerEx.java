package com.huawei.android.vrsystem;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.vrsystem.IVRListener;
import android.vrsystem.IVRSystemServiceManager;
import com.huawei.android.hardware.display.HwDisplayManager;

public class IVRSystemServiceManagerEx {
    private static final int DEFAULT_BATTERY = 0;
    private static final String EMPTY_STRING = "";
    private static final int MAX_BRIGHTNESS = 9;
    private static final int MIN_BRIGHTNESS = 0;
    private static final int PARAMS_LEN = 3;
    private static final String TAG = "IVRSystemServiceManagerEx";
    public static final String VR_MANAGER = "vr_system";
    private static final String VR_VIRTUAL_SCREEN_NAME = "com.huawei.vrvirtualscreen";
    private IVRSystemServiceManager mManager;

    public static IVRSystemServiceManagerEx create(Context context) {
        if (context != null) {
            return new IVRSystemServiceManagerEx((IVRSystemServiceManager) context.getSystemService(VR_MANAGER));
        }
        Log.e(TAG, "context is null in IVRSystemServiceManagerEx create.");
        return new IVRSystemServiceManagerEx(null);
    }

    private IVRSystemServiceManagerEx(IVRSystemServiceManager service) {
        this.mManager = service;
    }

    public boolean isVRMode() {
        IVRSystemServiceManager iVRSystemServiceManager = this.mManager;
        if (iVRSystemServiceManager != null) {
            return iVRSystemServiceManager.isVRMode();
        }
        Log.e(TAG, "mManager is null in isVRMode.");
        return false;
    }

    public boolean isVirtualScreenMode() {
        IVRSystemServiceManager iVRSystemServiceManager = this.mManager;
        if (iVRSystemServiceManager != null) {
            return iVRSystemServiceManager.isVirtualScreenMode();
        }
        Log.e(TAG, "mManager is null in isVirtualScreenMode.");
        return false;
    }

    public boolean isVRApplication(Context context, String packageName) {
        if (this.mManager != null && context != null && !TextUtils.isEmpty(packageName)) {
            return this.mManager.isVRApplication(context, packageName);
        }
        Log.e(TAG, "params is invalid in isVRApplication.");
        return false;
    }

    public String getContactName(Context context, String num) {
        if (this.mManager != null && context != null && !TextUtils.isEmpty(num)) {
            return this.mManager.getContactName(context, num);
        }
        Log.e(TAG, "params is invalid in getContactName.");
        return "";
    }

    public void registerVRListener(Context context, IVRListener listener) {
        IVRSystemServiceManager iVRSystemServiceManager = this.mManager;
        if (iVRSystemServiceManager == null || listener == null) {
            Log.e(TAG, "params is invalid in registerVRListener.");
        } else {
            iVRSystemServiceManager.registerVRListener(context, listener);
        }
    }

    public void registerExpandListener(Context context, IVRListener listener) {
        IVRSystemServiceManager iVRSystemServiceManager = this.mManager;
        if (iVRSystemServiceManager == null || listener == null) {
            Log.e(TAG, "params is invalid in registerExpandListener.");
        } else {
            iVRSystemServiceManager.registerExpandListener(context, listener);
        }
    }

    public int getHelmetBattery(Context context) {
        IVRSystemServiceManager iVRSystemServiceManager = this.mManager;
        if (iVRSystemServiceManager != null && context != null) {
            return iVRSystemServiceManager.getHelmetBattery(context);
        }
        Log.e(TAG, "params is invalid in getHelmetBattery.");
        return 0;
    }

    public int getHelmetBrightness(Context context) {
        IVRSystemServiceManager iVRSystemServiceManager = this.mManager;
        if (iVRSystemServiceManager != null && context != null) {
            return iVRSystemServiceManager.getHelmetBrightness(context);
        }
        Log.e(TAG, "params is invalid in getHelmetBrightness.");
        return 0;
    }

    public void setHelmetBrightness(Context context, int brightness) {
        IVRSystemServiceManager iVRSystemServiceManager = this.mManager;
        if (iVRSystemServiceManager == null || context == null) {
            Log.e(TAG, "params is invalid in getHelmetBrightness.");
        } else if (brightness < 0 || brightness > 9) {
            Log.e(TAG, "brightness to set is invalid in setHelmetBrightness.");
        } else {
            iVRSystemServiceManager.setHelmetBrightness(context, brightness);
        }
    }

    public static boolean createVrDisplay(String displayName, int[] displayParams, Context context) {
        if (displayName == null || displayParams == null) {
            Log.e(TAG, "params is invalid in createVrDisplay.");
            return false;
        } else if (checkPermissionForMultiDisplay(context) && displayParams.length == 3) {
            return HwDisplayManager.createVrDisplay(displayName, displayParams);
        } else {
            Log.e(TAG, "params or permission is invalid in createVrDisplay.");
            return false;
        }
    }

    public static boolean destroyVrDisplay(String displayName, Context context) {
        if (displayName != null && checkPermissionForMultiDisplay(context)) {
            return HwDisplayManager.destroyVrDisplay(displayName);
        }
        Log.e(TAG, "params or permission is invalid in destroyVrDisplay.");
        return false;
    }

    public static boolean destroyAllVrDisplay(Context context) {
        if (checkPermissionForMultiDisplay(context)) {
            return HwDisplayManager.destroyAllVrDisplay();
        }
        Log.e(TAG, "permission is invalid in destroyAllVrDisplay.");
        return false;
    }

    private static boolean checkPermissionForMultiDisplay(Context context) {
        if (context == null) {
            Log.e(TAG, "context is invalid in checkPermissionForMultiDisplay.");
            return false;
        }
        String packageName = context.getPackageName();
        if (packageName != null) {
            return packageName.equals(VR_VIRTUAL_SCREEN_NAME);
        }
        Log.e(TAG, "packageName is null.");
        return false;
    }
}
