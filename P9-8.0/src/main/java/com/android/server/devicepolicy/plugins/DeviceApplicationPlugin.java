package com.android.server.devicepolicy.plugins;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import com.android.server.devicepolicy.DevicePolicyPlugin;
import com.android.server.devicepolicy.HwDevicePolicyManagerServiceUtil;
import com.android.server.devicepolicy.HwLog;
import com.android.server.devicepolicy.PolicyStruct;
import com.android.server.devicepolicy.PolicyStruct.PolicyItem;
import com.android.server.devicepolicy.PolicyStruct.PolicyType;
import java.util.ArrayList;

public class DeviceApplicationPlugin extends DevicePolicyPlugin {
    private static final String INSTALL_APKS_BLACK_LIST = "install-packages-black-list";
    private static final String INSTALL_APKS_BLACK_LIST_ITEM = "install-packages-black-list/install-packages-black-list-item";
    public static final String TAG = DeviceApplicationPlugin.class.getSimpleName();

    public DeviceApplicationPlugin(Context context) {
        super(context);
    }

    public boolean onInit(final PolicyStruct struct) {
        HwLog.i(TAG, "onInit");
        if (struct != null && struct.containsPolicyName("policy-single-app")) {
            IntentFilter filter = new IntentFilter("android.intent.action.BOOT_COMPLETED");
            filter.setPriority(1000);
            this.mContext.registerReceiver(new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    DeviceApplicationPlugin.this.mContext.unregisterReceiver(this);
                    String packageName = struct.getPolicyItem("policy-single-app").getAttrValue("value");
                    if (packageName != null && (packageName.isEmpty() ^ 1) != 0) {
                        Intent launchIntent = DeviceApplicationPlugin.this.mContext.getPackageManager().getLaunchIntentForPackage(packageName);
                        if (launchIntent != null) {
                            DeviceApplicationPlugin.this.mContext.startActivity(launchIntent);
                        }
                    }
                }
            }, filter);
        }
        return true;
    }

    public String getPluginName() {
        return getClass().getSimpleName();
    }

    public PolicyStruct getPolicyStruct() {
        HwLog.i(TAG, "getPolicyStruct");
        PolicyStruct struct = new PolicyStruct(this);
        struct.addStruct(INSTALL_APKS_BLACK_LIST_ITEM, PolicyType.LIST, new String[]{"value"});
        struct.addStruct("ignore-frequent-relaunch-app", PolicyType.LIST, new String[0]);
        struct.addStruct("ignore-frequent-relaunch-app/ignore-frequent-relaunch-app-item", PolicyType.LIST, new String[]{"value"});
        struct.addStruct("policy-single-app", PolicyType.CONFIGURATION, new String[]{"value"});
        return struct;
    }

    public boolean checkCallingPermission(ComponentName who, String policyName) {
        HwLog.i(TAG, "checkCallingPermission");
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "does not have app_management MDM permission!");
        return true;
    }

    public boolean onSetPolicy(ComponentName who, String policyName, Bundle policyData, boolean effective) {
        HwLog.i(TAG, "onSetPolicy");
        if (!policyName.equals("policy-single-app")) {
            return !isNotEffective(who, policyName, policyData, effective);
        } else {
            HwLog.i(TAG, "onSetPolicy POLICY_SINGLE_APP");
            Intent launchIntent = this.mContext.getPackageManager().getLaunchIntentForPackage(policyData.getString("value"));
            if (launchIntent == null) {
                return false;
            }
            this.mContext.startActivity(launchIntent);
            return true;
        }
    }

    public boolean onRemovePolicy(ComponentName who, String policyName, Bundle policyData, boolean effective) {
        HwLog.i(TAG, "onRemovePolicy");
        if (isNotEffective(who, policyName, policyData, effective)) {
            return false;
        }
        return true;
    }

    public boolean onGetPolicy(ComponentName who, String policyName, Bundle policyData) {
        HwLog.i(TAG, "onGetPolicy");
        if (isNotEffective(who, policyName, policyData, true)) {
            return false;
        }
        return true;
    }

    public boolean onActiveAdminRemoved(ComponentName who, ArrayList<PolicyItem> arrayList) {
        HwLog.i(TAG, "onActiveAdminRemoved");
        return true;
    }

    public boolean isNotEffective(ComponentName who, String policyName, Bundle policyData, boolean effective) {
        if (policyData != null && ("install-packages-black-list".equals(policyName) || "ignore-frequent-relaunch-app".equals(policyName))) {
            ArrayList<String> packageNames = policyData.getStringArrayList("value");
            if (!HwDevicePolicyManagerServiceUtil.isValidatePackageNames(packageNames)) {
                throw new IllegalArgumentException("packageName:" + packageNames + " is invalid.");
            }
        }
        return false;
    }
}
