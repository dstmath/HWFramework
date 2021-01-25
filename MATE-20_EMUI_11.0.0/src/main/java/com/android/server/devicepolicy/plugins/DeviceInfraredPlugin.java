package com.android.server.devicepolicy.plugins;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import com.android.server.devicepolicy.DevicePolicyPlugin;
import com.android.server.devicepolicy.HwLog;
import com.android.server.devicepolicy.PolicyStruct;
import java.util.ArrayList;

public class DeviceInfraredPlugin extends DevicePolicyPlugin {
    private static final String INFRARED_CONTROL_PERMSSION = "com.huawei.permission.sec.MDM_INFRARED";
    private static final String INFRARED_ISALLOW_FORBID_KEY = "infrared_item_policy_value";
    private static final String INFRARED_ISALLOW_ITEM_POLICY_NAME = "infrared_item_policy_name";
    public static final String TAG = DeviceInfraredPlugin.class.getSimpleName();

    public DeviceInfraredPlugin(Context context) {
        super(context);
    }

    public String getPluginName() {
        return getClass().getSimpleName();
    }

    public PolicyStruct getPolicyStruct() {
        PolicyStruct struct = new PolicyStruct(this);
        struct.addStruct(INFRARED_ISALLOW_ITEM_POLICY_NAME, PolicyStruct.PolicyType.STATE, new String[]{INFRARED_ISALLOW_FORBID_KEY});
        return struct;
    }

    public boolean onInit(PolicyStruct policyStruct) {
        if (policyStruct == null) {
            return false;
        }
        return true;
    }

    public boolean checkCallingPermission(ComponentName who, String policyName) {
        this.mContext.enforceCallingOrSelfPermission(INFRARED_CONTROL_PERMSSION, "need permission com.huawei.permission.sec.MDM_INFRARD");
        return true;
    }

    public boolean onSetPolicy(ComponentName who, String policyName, Bundle policyData, boolean isChanged) {
        if (policyName != null) {
            return true;
        }
        HwLog.e(TAG, "onSetPolicy params null");
        return false;
    }

    public boolean onRemovePolicy(ComponentName who, String policyName, Bundle policyData, boolean isChanged) {
        if (policyName != null) {
            return true;
        }
        HwLog.e(TAG, "onRemovePolicy params null.");
        return false;
    }

    public boolean onActiveAdminRemoved(ComponentName who, ArrayList<PolicyStruct.PolicyItem> arrayList) {
        return true;
    }
}
