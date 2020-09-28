package com.huawei.android.vrsystem;

import android.content.Context;
import android.vrsystem.IVRListener;
import android.vrsystem.IVRSystemServiceManager;
import com.huawei.android.hardware.display.HwDisplayManager;

public class IVRSystemServiceManagerEx {
    private static final int PARAMS_LEN = 3;
    public static final String VR_MANAGER = "vr_system";
    private static final String VR_VIRTUAL_SCREEN_NAME = "com.huawei.vrvirtualscreen";
    private IVRSystemServiceManager mVRSM;

    public static IVRSystemServiceManagerEx create(Context context) {
        IVRSystemServiceManager service = (IVRSystemServiceManager) context.getSystemService(VR_MANAGER);
        if (service == null) {
            return null;
        }
        return new IVRSystemServiceManagerEx(service);
    }

    private IVRSystemServiceManagerEx(IVRSystemServiceManager service) {
        this.mVRSM = service;
    }

    public boolean isVRMode() {
        return this.mVRSM.isVRMode();
    }

    public boolean isVirtualScreenMode() {
        return this.mVRSM.isVirtualScreenMode();
    }

    public boolean isVRApplication(Context context, String packageName) {
        return this.mVRSM.isVRApplication(context, packageName);
    }

    public String getContactName(Context context, String num) {
        return this.mVRSM.getContactName(context, num);
    }

    public void registerVRListener(Context context, IVRListener vrlistener) {
        this.mVRSM.registerVRListener(context, vrlistener);
    }

    public void registerExpandListener(Context context, IVRListener vrlistener) {
        this.mVRSM.registerExpandListener(context, vrlistener);
    }

    public int getHelmetBattery(Context context) {
        return this.mVRSM.getHelmetBattery(context);
    }

    public int getHelmetBrightness(Context context) {
        return this.mVRSM.getHelmetBrightness(context);
    }

    public void setHelmetBrightness(Context context, int brightness) {
        this.mVRSM.setHelmetBrightness(context, brightness);
    }

    public static boolean createVrDisplay(String displayName, int[] displayParams, Context context) {
        if (displayName == null || displayParams == null || !checkPermissionForMultiDisplay(context) || displayParams.length != 3) {
            return false;
        }
        return HwDisplayManager.createVrDisplay(displayName, displayParams);
    }

    public static boolean destroyVrDisplay(String displayName, Context context) {
        if (displayName == null || !checkPermissionForMultiDisplay(context)) {
            return false;
        }
        return HwDisplayManager.destroyVrDisplay(displayName);
    }

    public static boolean destroyAllVrDisplay(Context context) {
        if (!checkPermissionForMultiDisplay(context)) {
            return false;
        }
        return HwDisplayManager.destroyAllVrDisplay();
    }

    private static boolean checkPermissionForMultiDisplay(Context context) {
        if (context == null) {
            return false;
        }
        return VR_VIRTUAL_SCREEN_NAME.equals(context.getPackageName());
    }
}
