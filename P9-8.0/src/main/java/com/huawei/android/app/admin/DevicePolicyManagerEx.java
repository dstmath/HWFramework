package com.huawei.android.app.admin;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import huawei.android.app.admin.HwDevicePolicyManagerEx;

public class DevicePolicyManagerEx {
    public static final int NOT_SUPPORT_SD_CRYPT = -1;
    public static final int SD_CRYPT_STATE_DECRYPTED = 1;
    public static final int SD_CRYPT_STATE_DECRYPTING = 4;
    public static final int SD_CRYPT_STATE_ENCRYPTED = 2;
    public static final int SD_CRYPT_STATE_ENCRYPTING = 3;
    public static final int SD_CRYPT_STATE_INVALID = 0;
    public static final int SD_CRYPT_STATE_MISMATCH = 5;
    public static final int SD_CRYPT_STATE_WAIT_UNLOCK = 6;
    private static final HwDevicePolicyManagerEx mDpm = new HwDevicePolicyManagerEx();

    public static int getSDCardEncryptionStatus() {
        return mDpm.getSDCardEncryptionStatus();
    }

    public static void setSDCardDecryptionDisabled(ComponentName who, boolean disabled) {
        mDpm.setSDCardDecryptionDisabled(who, disabled);
    }

    public static boolean isSDCardDecryptionDisabled(ComponentName who) {
        return mDpm.isSDCardDecryptionDisabled(who);
    }

    public static boolean hasHwPolicy() {
        return mDpm.hasHwPolicy();
    }

    public static String getActionDpmStateChanged() {
        return "android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED";
    }

    public static boolean packageHasActiveAdmins(DevicePolicyManager dm, String packageName, int userId) {
        return dm.packageHasActiveAdmins(packageName, userId);
    }
}
