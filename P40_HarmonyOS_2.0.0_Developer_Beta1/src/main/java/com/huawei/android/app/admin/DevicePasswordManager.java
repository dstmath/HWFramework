package com.huawei.android.app.admin;

import android.content.ComponentName;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import huawei.android.app.admin.HwDevicePolicyManagerEx;

public class DevicePasswordManager {
    public static final String KEY_KEYGUARD_DISABLED = "keyguard_disabled";
    public static final String KEY_KEYGUARD_TYPE = "keyguard_type";
    public static final String KEY_QUICK_TOOLS = "keyguard_quick_tools_disabled";
    public static final String POLICY_KEYGUARD_DISABLED = "policy_keyguard_disabled";
    public static final String POLICY_QUICK_TOOLS_DISABLED = "policy_keyguard_quick_tools_disabled";
    private static final String PWD_CHANGE_EXTEND_TIME = "pwd-password-change-extendtime";
    private static final String PWD_NUM_SEQUENCE_MAX_LENGTH = "pwd-num-sequence-max-length";
    private static final String PWD_REPETITION_MAX_LENGTH = "pwd-repetition-max-length";
    private static final int SLIDING_KEYGUARD_TYPE_VALUE = 0;
    private static final String TAG = "DevicePasswordManager";
    private final HwDevicePolicyManagerEx mDpm = new HwDevicePolicyManagerEx();

    public boolean setPasswordNumSequenceMaxLength(ComponentName admin, int length) {
        if (length < 0) {
            return false;
        }
        String maxLength = String.valueOf(length);
        Bundle bundle = new Bundle();
        bundle.putString("value", maxLength);
        return this.mDpm.setPolicy(admin, PWD_NUM_SEQUENCE_MAX_LENGTH, bundle);
    }

    public int getPasswordNumSequenceMaxLength(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, PWD_NUM_SEQUENCE_MAX_LENGTH);
        if (bundle == null) {
            return 0;
        }
        String value = bundle.getString("value");
        if (TextUtils.isEmpty(value)) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            Log.w(TAG, "getPasswordNumSequenceMaxLength NumberFormatException!");
            return 0;
        }
    }

    public boolean setPasswordRepeatMaxLength(ComponentName admin, int length) {
        if (length < 0) {
            return false;
        }
        String maxLength = String.valueOf(length);
        Bundle bundle = new Bundle();
        bundle.putString("value", maxLength);
        return this.mDpm.setPolicy(admin, PWD_REPETITION_MAX_LENGTH, bundle);
    }

    public int getPasswordRepeatMaxLength(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, PWD_REPETITION_MAX_LENGTH);
        if (bundle == null) {
            return 0;
        }
        String value = bundle.getString("value");
        if (TextUtils.isEmpty(value)) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            Log.w(TAG, "getPasswordRepeatMaxLength NumberFormatException!");
            return 0;
        }
    }

    public boolean setPasswordChangeExtendTime(ComponentName admin, long time) {
        if (time < 0) {
            return false;
        }
        String extandTime = String.valueOf(time);
        Bundle bundle = new Bundle();
        bundle.putString("value", extandTime);
        return this.mDpm.setPolicy(admin, PWD_CHANGE_EXTEND_TIME, bundle);
    }

    public long getPasswordChangeExtendTime(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, PWD_CHANGE_EXTEND_TIME);
        if (bundle == null) {
            return 0;
        }
        String value = bundle.getString("value");
        if (TextUtils.isEmpty(value)) {
            return 0;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            Log.w(TAG, "getPasswordChangeExtendTime NumberFormatException!");
            return 0;
        }
    }

    public boolean setKeyguardDisabled(ComponentName admin, int keyguardType, boolean isDisabled) {
        if (keyguardType != 0) {
            return false;
        }
        Bundle bundle = new Bundle();
        bundle.putString(KEY_KEYGUARD_TYPE, String.valueOf(keyguardType));
        bundle.putString(KEY_KEYGUARD_DISABLED, Boolean.toString(isDisabled));
        return this.mDpm.setPolicy(admin, POLICY_KEYGUARD_DISABLED, bundle);
    }

    public boolean isKeyguardDisabled(ComponentName admin, int keyguardType) {
        Bundle bundle;
        if (keyguardType == 0 && (bundle = this.mDpm.getPolicy(admin, POLICY_KEYGUARD_DISABLED)) != null && bundle.getString(KEY_KEYGUARD_TYPE, "").equals(String.valueOf(keyguardType))) {
            return Boolean.valueOf(bundle.getString(KEY_KEYGUARD_DISABLED, Boolean.FALSE.toString())).booleanValue();
        }
        return false;
    }

    public boolean setQuickToolsDisabled(ComponentName admin, boolean isDisabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_QUICK_TOOLS, isDisabled);
        return this.mDpm.setPolicy(admin, POLICY_QUICK_TOOLS_DISABLED, bundle);
    }

    public boolean isQuickToolsDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, POLICY_QUICK_TOOLS_DISABLED);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean(KEY_QUICK_TOOLS, false);
    }
}
