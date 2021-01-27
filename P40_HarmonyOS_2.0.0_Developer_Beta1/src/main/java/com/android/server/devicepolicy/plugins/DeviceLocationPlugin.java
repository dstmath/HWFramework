package com.android.server.devicepolicy.plugins;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.android.server.devicepolicy.DevicePolicyPlugin;
import com.android.server.devicepolicy.HwLog;
import com.android.server.devicepolicy.PolicyStruct;
import java.util.ArrayList;
import java.util.Iterator;

public class DeviceLocationPlugin extends DevicePolicyPlugin {
    private static final String ACTION_DEVICE_POLICY_CHANGED = "com.huawei.devicepolicy.action.POLICY_CHANGED";
    private static final String DISALLOW_PASSIVE_LOCATION = "passive_location_disallow_item";
    private static final String LOCATION_CONTROL_PERMSSION = "com.huawei.permission.sec.MDM_LOCATION";
    private static final String TAG = DeviceLocationPlugin.class.getSimpleName();

    public DeviceLocationPlugin(Context context) {
        super(context);
    }

    public String getPluginName() {
        return getClass().getSimpleName();
    }

    public PolicyStruct getPolicyStruct() {
        HwLog.i(TAG, "getPolicyStruct");
        PolicyStruct struct = new PolicyStruct(this);
        struct.addStruct(DISALLOW_PASSIVE_LOCATION, PolicyStruct.PolicyType.STATE, new String[]{DeviceSettingsPlugin.STATE_VALUE});
        return struct;
    }

    public boolean checkCallingPermission(ComponentName who, String policyName) {
        this.mContext.enforceCallingOrSelfPermission(LOCATION_CONTROL_PERMSSION, "need permission com.huawei.permission.sec.MDM_LOCATION");
        return true;
    }

    public boolean onSetPolicy(ComponentName who, String policyName, Bundle policyData, boolean isChanged) {
        HwLog.i(TAG, "onSetPolicy");
        if (policyName != null) {
            return true;
        }
        HwLog.e(TAG, "params null.");
        return false;
    }

    public boolean onRemovePolicy(ComponentName who, String policyName, Bundle policyData, boolean isChanged) {
        HwLog.i(TAG, "onRemovePolicy");
        if (policyName != null) {
            return true;
        }
        HwLog.e(TAG, "policyName null");
        return false;
    }

    public boolean onActiveAdminRemoved(ComponentName who, ArrayList<PolicyStruct.PolicyItem> removedPolicies) {
        HwLog.i(TAG, "onActiveAdminRemoved");
        if (removedPolicies == null) {
            return true;
        }
        Iterator<PolicyStruct.PolicyItem> it = removedPolicies.iterator();
        while (it.hasNext()) {
            String policyName = it.next().getPolicyName();
            if (DISALLOW_PASSIVE_LOCATION.equals(policyName)) {
                sendLocationAllowSet(policyName);
            }
        }
        return true;
    }

    private void sendLocationAllowSet(String policyName) {
        Intent intent = new Intent();
        intent.setAction(ACTION_DEVICE_POLICY_CHANGED);
        intent.putExtra("policyName", policyName);
        intent.setPackage("android");
        intent.addFlags(1073741824);
        this.mContext.sendBroadcast(intent);
    }
}
