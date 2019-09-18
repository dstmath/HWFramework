package com.huawei.android.vrsystem;

import android.content.Context;
import android.vrsystem.IVRListener;
import android.vrsystem.IVRSystemServiceManager;

public class IVRSystemServiceManagerEx {
    public static final String VR_MANAGER = "vr_system";
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
}
