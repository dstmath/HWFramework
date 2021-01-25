package com.android.server.devicepolicy.plugins;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.android.server.devicepolicy.DevicePolicyPlugin;
import com.android.server.devicepolicy.HwLog;
import com.android.server.devicepolicy.PolicyStruct;
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
        struct.addStruct(POLICY_DISABLE_VIDEO, PolicyStruct.PolicyType.STATE, new String[]{"value"});
        return struct;
    }

    public boolean checkCallingPermission(ComponentName admin, String policyName) {
        HwLog.i(TAG, "checkCallingPermission");
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_CAMERA", "does not have PERMISSION_MDM_CAMERA permission!");
        return true;
    }

    public boolean onSetPolicy(ComponentName admin, String policyName, Bundle policyData, boolean isChanged) {
        return true;
    }

    public void onSetPolicyCompleted(ComponentName admin, String policyName, boolean isChanged) {
        HwLog.i(TAG, "onSetPolicyCompleted");
        if (isChanged && POLICY_DISABLE_VIDEO.equals(policyName)) {
            this.mContext.sendBroadcast(createDisableVideoPolicyIntent());
        }
    }

    public boolean onRemovePolicy(ComponentName admin, String policyName, Bundle policyData, boolean isChanged) {
        return true;
    }

    public void onRemovePolicyCompleted(ComponentName admin, String policyName, boolean isChanged) {
        String str = TAG;
        HwLog.i(str, "onRemovePolicyCompleted, policyName: " + policyName + ", changed: " + isChanged);
        if (isChanged && POLICY_DISABLE_VIDEO.equals(policyName)) {
            this.mContext.sendBroadcast(createDisableVideoPolicyIntent());
        }
    }

    public boolean onActiveAdminRemoved(ComponentName admin, ArrayList<PolicyStruct.PolicyItem> arrayList) {
        HwLog.i(TAG, "onActiveAdminRemoved");
        return true;
    }

    private Intent createDisableVideoPolicyIntent() {
        Intent intent = new Intent("com.huawei.devicepolicy.action.POLICY_CHANGED");
        intent.setPackage(PACKAGE_NAME_CAEMRA);
        intent.putExtra(POLICY_NAME, POLICY_DISABLE_VIDEO);
        return intent;
    }
}
