package com.huawei.android.app.admin;

import android.content.ComponentName;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import huawei.android.app.admin.HwDevicePolicyManagerEx;

public class DevicePasswordManager {
    private static final String PWD_CHANGE_EXTEND_TIME = "pwd-password-change-extendtime";
    private static final String PWD_NUM_SEQUENCE_MAX_LENGTH = "pwd-num-sequence-max-length";
    private static final String PWD_REPETITION_MAX_LENGTH = "pwd-repetition-max-length";
    private static final String TAG = "DevicePasswordManager";
    private final HwDevicePolicyManagerEx mDpm = new HwDevicePolicyManagerEx();

    public boolean setPasswordNumSequenceMaxLength(ComponentName who, int length) {
        if (length < 0) {
            return false;
        }
        String maxLength = String.valueOf(length);
        Bundle bundle = new Bundle();
        bundle.putString("value", maxLength);
        return this.mDpm.setPolicy(who, PWD_NUM_SEQUENCE_MAX_LENGTH, bundle);
    }

    public int getPasswordNumSequenceMaxLength(ComponentName who) {
        int length = 0;
        Bundle bundle = this.mDpm.getPolicy(who, PWD_NUM_SEQUENCE_MAX_LENGTH);
        if (bundle == null) {
            return length;
        }
        String value = bundle.getString("value");
        if (!TextUtils.isEmpty(value)) {
            try {
                length = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                Log.w(TAG, "getPasswordNumSequenceMaxLength NumberFormatException!");
            }
        }
        return length;
    }

    public boolean setPasswordRepeatMaxLength(ComponentName who, int length) {
        if (length < 0) {
            return false;
        }
        String maxLength = String.valueOf(length);
        Bundle bundle = new Bundle();
        bundle.putString("value", maxLength);
        return this.mDpm.setPolicy(who, PWD_REPETITION_MAX_LENGTH, bundle);
    }

    public int getPasswordRepeatMaxLength(ComponentName who) {
        int repeatLength = 0;
        Bundle bundle = this.mDpm.getPolicy(who, PWD_REPETITION_MAX_LENGTH);
        if (bundle == null) {
            return repeatLength;
        }
        String value = bundle.getString("value");
        if (!TextUtils.isEmpty(value)) {
            try {
                repeatLength = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                Log.w(TAG, "getPasswordRepeatMaxLength NumberFormatException!");
            }
        }
        return repeatLength;
    }

    public boolean setPasswordChangeExtendTime(ComponentName who, long time) {
        if (time < 0) {
            return false;
        }
        String extandTime = String.valueOf(time);
        Bundle bundle = new Bundle();
        bundle.putString("value", extandTime);
        return this.mDpm.setPolicy(who, PWD_CHANGE_EXTEND_TIME, bundle);
    }

    public long getPasswordChangeExtendTime(ComponentName who) {
        long extendTime = 0;
        Bundle bundle = this.mDpm.getPolicy(who, PWD_CHANGE_EXTEND_TIME);
        if (bundle == null) {
            return extendTime;
        }
        String value = bundle.getString("value");
        if (!TextUtils.isEmpty(value)) {
            try {
                extendTime = Long.parseLong(value);
            } catch (NumberFormatException e) {
                Log.w(TAG, "getPasswordChangeExtendTime NumberFormatException!");
            }
        }
        return extendTime;
    }
}
