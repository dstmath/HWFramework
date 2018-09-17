package com.android.server.devicepolicy.plugins;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.android.server.devicepolicy.DevicePolicyPlugin;
import com.android.server.devicepolicy.HwLog;
import com.android.server.devicepolicy.PolicyStruct;
import com.android.server.devicepolicy.PolicyStruct.PolicyItem;
import com.android.server.devicepolicy.PolicyStruct.PolicyType;
import java.util.ArrayList;

public class DeviceCameraPlugin extends DevicePolicyPlugin {
    private static final String ACTION_POLICY_CHANGED = "com.huawei.devicepolicy.action.POLICY_CHANGED";
    private static final String PACKAGE_NAME_CAEMRA = "com.huawei.camera";
    private static final String POLICY_DISABLE_VIDEO = "disable-video";
    private static final String POLICY_NAME = "policy_name";
    private static final String STATE_VALUE = "value";
    private static final String TAG = DeviceCameraPlugin.class.getSimpleName();

    public DeviceCameraPlugin(Context context) {
        super(context);
    }

    public String getPluginName() {
        return getClass().getSimpleName();
    }

    public PolicyStruct getPolicyStruct() {
        PolicyStruct struct = new PolicyStruct(this);
        PolicyType stateType = PolicyType.STATE;
        struct.addStruct(POLICY_DISABLE_VIDEO, stateType, new String[]{"value"});
        return struct;
    }

    public boolean checkCallingPermission(ComponentName admin, String policyName) {
        HwLog.i(TAG, "checkCallingPermission");
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_CAMERA", "does not have PERMISSION_MDM_CAMERA permission!");
        return true;
    }

    public boolean onSetPolicy(ComponentName admin, String policyName, Bundle policyData, boolean changed) {
        return true;
    }

    public void onSetPolicyCompleted(ComponentName admin, String policyName, boolean changed) {
        HwLog.i(TAG, "onSetPolicyCompleted");
        if (changed && POLICY_DISABLE_VIDEO.equals(policyName)) {
            this.mContext.sendBroadcast(createDisableVideoPolicyIntent());
        }
    }

    public boolean onRemovePolicy(ComponentName admin, String policyName, Bundle policyData, boolean changed) {
        return true;
    }

    public void onRemovePolicyCompleted(ComponentName admin, String policyName, boolean changed) {
        HwLog.i(TAG, "onRemovePolicyCompleted, policyName: " + policyName + ", changed: " + changed);
        if (changed && POLICY_DISABLE_VIDEO.equals(policyName)) {
            this.mContext.sendBroadcast(createDisableVideoPolicyIntent());
        }
    }

    public boolean onActiveAdminRemoved(ComponentName admin, ArrayList<PolicyItem> arrayList) {
        HwLog.i(TAG, "onActiveAdminRemoved");
        return true;
    }

    private Intent createDisableVideoPolicyIntent() {
        Intent intent = new Intent("com.huawei.devicepolicy.action.POLICY_CHANGED");
        intent.setPackage("com.huawei.camera");
        intent.putExtra(POLICY_NAME, POLICY_DISABLE_VIDEO);
        return intent;
    }
}
