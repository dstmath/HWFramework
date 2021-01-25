package com.android.server.devicepolicy.plugins;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.android.server.devicepolicy.DevicePolicyPlugin;
import com.android.server.devicepolicy.HwLog;
import com.android.server.devicepolicy.PolicyStruct;
import java.util.ArrayList;

public class DeviceLocationPlugin extends DevicePolicyPlugin {
    private static final String ACTION_DEVICE_POLICY_CHANGED = "com.huawei.devicepolicy.action.POLICY_CHANGED";
    private static final String DISALLOW_PASSIVE_LOCATION = "passive_location_disallow_item";
    private static final String LOCATION_CONTROL_PERMSSION = "com.huawei.permission.sec.MDM_LOCATION";
    public static final String TAG = DeviceLocationPlugin.class.getSimpleName();

    public DeviceLocationPlugin(Context context) {
        super(context);
    }

    public String getPluginName() {
        return getClass().getSimpleName();
    }

    public PolicyStruct getPolicyStruct() {
        HwLog.i(TAG, "getPolicyStruct");
        PolicyStruct struct = new PolicyStruct(this);
        struct.addStruct(DISALLOW_PASSIVE_LOCATION, PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        return struct;
    }

    public boolean checkCallingPermission(ComponentName who, String policyName) {
        this.mContext.enforceCallingOrSelfPermission(LOCATION_CONTROL_PERMSSION, "need permission com.huawei.permission.sec.MDM_LOCATION");
        return true;
    }

    public boolean onSetPolicy(ComponentName who, String policyName, Bundle policyData, boolean isChanged) {
        Log.i(TAG, "onSetPolicy");
        if (policyName != null) {
            return true;
        }
        Log.e(TAG, "params null.");
        return false;
    }

    public boolean onRemovePolicy(ComponentName who, String policyName, Bundle policyData, boolean isChanged) {
        Log.i(TAG, "onRemovePolicy");
        if (policyName != null) {
            return true;
        }
        Log.e(TAG, "policyName null");
        return false;
    }

    public boolean onActiveAdminRemoved(ComponentName who, ArrayList<PolicyStruct.PolicyItem> removedPolicies) {
        Log.i(TAG, "onActiveAdminRemoved");
        if (removedPolicies == null || removedPolicies.size() <= 0) {
            return true;
        }
        int size = removedPolicies.size();
        for (int i = 0; i < size; i++) {
            String policyName = removedPolicies.get(i).getPolicyName();
            if (DISALLOW_PASSIVE_LOCATION.equals(policyName)) {
                sendLocationAllowSet(policyName);
            }
        }
        return true;
    }

    private void sendLocationAllowSet(String policyName) {
        Intent intent = new Intent();
        intent.setAction("com.huawei.devicepolicy.action.POLICY_CHANGED");
        intent.putExtra("policyName", policyName);
        intent.setPackage("android");
        intent.addFlags(1073741824);
        this.mContext.sendBroadcast(intent);
    }
}
