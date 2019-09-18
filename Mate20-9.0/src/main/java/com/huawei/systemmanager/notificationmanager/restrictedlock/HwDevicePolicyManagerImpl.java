package com.huawei.systemmanager.notificationmanager.restrictedlock;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import java.util.List;

public class HwDevicePolicyManagerImpl implements IDevicePolicyManager {
    private static volatile HwDevicePolicyManagerImpl mInstance = null;

    public static synchronized IDevicePolicyManager getInstance() {
        HwDevicePolicyManagerImpl hwDevicePolicyManagerImpl;
        synchronized (HwDevicePolicyManagerImpl.class) {
            if (mInstance == null) {
                mInstance = new HwDevicePolicyManagerImpl();
            }
            hwDevicePolicyManagerImpl = mInstance;
        }
        return hwDevicePolicyManagerImpl;
    }

    public List<ComponentName> getActiveAdminsAsUser(Context context, int userid) {
        DevicePolicyManager mDevicePolicyManager = (DevicePolicyManager) context.getSystemService("device_policy");
        if (mDevicePolicyManager != null) {
            return mDevicePolicyManager.getActiveAdminsAsUser(userid);
        }
        return null;
    }
}
