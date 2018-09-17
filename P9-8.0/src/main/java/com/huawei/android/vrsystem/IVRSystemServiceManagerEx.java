package com.huawei.android.vrsystem;

import android.content.Context;
import android.vrsystem.IVRSystemServiceManager;

public class IVRSystemServiceManagerEx {
    private IVRSystemServiceManager mVRSM;

    public static IVRSystemServiceManagerEx create(Context context) {
        IVRSystemServiceManager service = (IVRSystemServiceManager) context.getSystemService("vr_system");
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
}
