package com.android.server.devicepolicy.plugins;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import com.android.server.devicepolicy.DevicePolicyPlugin;
import com.android.server.devicepolicy.HwLog;
import com.android.server.devicepolicy.PolicyStruct;
import com.android.server.devicepolicy.PolicyStruct.PolicyItem;
import com.android.server.devicepolicy.PolicyStruct.PolicyType;
import java.util.ArrayList;

public class DevicePasswordPlugin extends DevicePolicyPlugin {
    private static final String PWD_CHANGE_EXTEND_TIME = "pwd-password-change-extendtime";
    private static final String PWD_NUM_SEQUENCE_MAX_LENGTH = "pwd-num-sequence-max-length";
    private static final String PWD_REPETITION_MAX_LENGTH = "pwd-repetition-max-length";
    public static final String TAG = DevicePasswordPlugin.class.getSimpleName();

    public DevicePasswordPlugin(Context context) {
        super(context);
    }

    public String getPluginName() {
        return getClass().getSimpleName();
    }

    public PolicyStruct getPolicyStruct() {
        HwLog.i(TAG, "getPolicyStruct");
        PolicyStruct struct = new PolicyStruct(this);
        struct.addStruct(PWD_NUM_SEQUENCE_MAX_LENGTH, PolicyType.CONFIGURATION, new String[]{"value"});
        struct.addStruct(PWD_REPETITION_MAX_LENGTH, PolicyType.CONFIGURATION, new String[]{"value"});
        struct.addStruct(PWD_CHANGE_EXTEND_TIME, PolicyType.CONFIGURATION, new String[]{"value"});
        return struct;
    }

    public boolean onInit(PolicyStruct policyStruct) {
        HwLog.i(TAG, "onInit");
        if (policyStruct == null) {
            return false;
        }
        return true;
    }

    public boolean checkCallingPermission(ComponentName who, String policyName) {
        HwLog.i(TAG, "checkCallingPermission");
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_KEYGUARD", "does not have PERMISSION_MDM_KEYGUARD permission!");
        return true;
    }

    public boolean onSetPolicy(ComponentName who, String policyName, Bundle policyData, boolean changed) {
        HwLog.i(TAG, "onSetPolicy");
        return true;
    }

    public boolean onRemovePolicy(ComponentName who, String policyName, Bundle policyData, boolean changed) {
        HwLog.i(TAG, "onRemovePolicy");
        return true;
    }

    public boolean onGetPolicy(ComponentName who, String policyName, Bundle policyData) {
        HwLog.i(TAG, "onGetPolicy");
        return true;
    }

    public boolean onActiveAdminRemoved(ComponentName who, ArrayList<PolicyItem> arrayList) {
        HwLog.i(TAG, "onActiveAdminRemoved");
        return true;
    }
}
