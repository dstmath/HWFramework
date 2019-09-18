package com.android.server.devicepolicy;

import android.content.Context;

public class HwCustDevicePolicyManagerService {
    public void wipeStorageData(Context context) {
    }

    public boolean wipeDataAndReset(Context context) {
        return false;
    }

    public boolean isAttEraseDataOn(Context context) {
        return false;
    }

    public void isStartEraseAllDataForAtt(Context context, int failedAttemps) {
    }

    public boolean shouldActiveDeviceAdmins(String policyPath) {
        return false;
    }

    public void activeDeviceAdmins(String policyPath) {
    }

    public boolean eraseStorageForEAS(Context context) {
        return false;
    }

    public boolean isNewPwdSimpleCheck(String password, Context context) {
        return false;
    }

    public boolean isForbiddenSimplePwdFeatureEnable() {
        return false;
    }

    public boolean isCertNotificationAllowed(String applicationLable) {
        return true;
    }
}
