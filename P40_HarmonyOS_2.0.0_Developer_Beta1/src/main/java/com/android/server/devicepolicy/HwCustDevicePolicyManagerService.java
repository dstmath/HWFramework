package com.android.server.devicepolicy;

import android.content.ComponentName;
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

    public void monitorFactoryReset(String component, String reason) {
    }

    public void clearWipeDataFactoryLowlevel(Context context, String reason, boolean wipeEuicc) {
    }

    public void setDeviceOwnerEx(Context context, ComponentName admin) {
    }

    public void setProfileOwnerEx(Context context, ComponentName admin) {
    }
}
