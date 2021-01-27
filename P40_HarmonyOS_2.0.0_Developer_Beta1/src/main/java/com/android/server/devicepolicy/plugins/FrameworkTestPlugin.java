package com.android.server.devicepolicy.plugins;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import com.android.server.devicepolicy.DevicePolicyPlugin;
import com.android.server.devicepolicy.HwLog;
import com.android.server.devicepolicy.PolicyStruct;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;

public class FrameworkTestPlugin extends DevicePolicyPlugin {
    private static final int END_INDEX = 10;
    private static final String MDM_WIFI_PERMISSION = "com.huawei.permission.sec.MDM_WIFI";
    private static final String TAG = FrameworkTestPlugin.class.getSimpleName();
    private static final String TEST_CONFIGURATION_LIST = "test_configuration_list/test_configuration_list_item";
    private static final String TEST_MULTIUSERS = "test_policy_multi_users";

    public FrameworkTestPlugin(Context context) {
        super(context);
    }

    public String getPluginName() {
        return FrameworkTestPlugin.class.getSimpleName();
    }

    public PolicyStruct getPolicyStruct() {
        PolicyStruct struct = new PolicyStruct(this);
        struct.addStruct(TEST_CONFIGURATION_LIST, PolicyStruct.PolicyType.CONFIGLIST, new String[]{DeviceSettingsPlugin.STATE_VALUE});
        struct.addStruct(TEST_MULTIUSERS, PolicyStruct.PolicyType.STATE, true, new String[]{DeviceSettingsPlugin.STATE_VALUE});
        return struct;
    }

    public boolean onSetPolicy(ComponentName admin, String policyName, Bundle policyData, boolean isGlobalPolicyChanged) {
        String str = TAG;
        HwLog.i(str, "onSetPolicy PolicyName " + policyName + " globalPolicyChanged:" + isGlobalPolicyChanged);
        return true;
    }

    public boolean onRemovePolicy(ComponentName admin, String policyName, Bundle policyData, boolean isGlobalPolicyChanged) {
        return true;
    }

    public boolean onActiveAdminRemoved(ComponentName componentName, ArrayList<PolicyStruct.PolicyItem> arrayList) {
        return false;
    }

    public boolean onInit(PolicyStruct struct) {
        addGetter(TEST_CONFIGURATION_LIST, new DevicePolicyPlugin.IPolicyItemKeyGetter() {
            /* class com.android.server.devicepolicy.plugins.FrameworkTestPlugin.AnonymousClass1 */

            public String getKey(String str) {
                try {
                    return new JSONObject(str).getString("accountName");
                } catch (JSONException e) {
                    HwLog.e(FrameworkTestPlugin.TAG, "onInit JSONException happend");
                    return DeviceSettingsPlugin.EMPTY_STRING;
                }
            }
        });
        return true;
    }

    public boolean checkCallingPermission(ComponentName componentName, String str) {
        HwLog.d(TAG, "checkCallingPermission");
        this.mContext.enforceCallingOrSelfPermission(MDM_WIFI_PERMISSION, "need permission com.huawei.permission.sec.MDM_WIFI");
        return true;
    }
}
