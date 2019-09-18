package com.android.server.devicepolicy.plugins;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.UserHandle;
import com.android.server.devicepolicy.DevicePolicyPlugin;
import com.android.server.devicepolicy.HwAdminCache;
import com.android.server.devicepolicy.HwLog;
import com.android.server.devicepolicy.PolicyStruct;
import java.util.ArrayList;

public class HwSystemManagerPlugin extends DevicePolicyPlugin {
    private static final String POLICY_ENTERPRISE_WHITE_LIST = "enterprise-whitelist-hwsystemmanager";
    private static final String SYSTEM_MANAGER_PERMISSION = "com.huawei.systemmanager.permission.ACCESS_INTERFACE";
    public static final String TAG = HwSystemManagerPlugin.class.getSimpleName();
    private static final String UPDATE_DATA_ENTERPRISE_LIST_ADD = "com.huawei.systemmanager.traffic.enterprisewhitelist.add";
    private static final String UPDATE_DATA_ENTERPRISE_LIST_REMOVE = "com.huawei.systemmanager.traffic.enterprisewhitelist.remove";
    private static final String UPDATE_DATA_SAVER_MODE = "com.huawei.systemmanager.netassistant.traffic.datasaver.update";
    private static final String UPDATE_DATA_SAVER_MODE_REMOVED = "com.huawei.systemmanager.netassistant.traffic.datasaver.update.remove";
    private static final String UPDATE_NOTIFICATION_SUPERWHITELIST = "com.huawei.notificationmanager.superwhitelist";
    private static final String UPDATE_NOTIFICATION_SUPERWHITELIST_REMOVE = "com.huawei.notificationmanager.superwhitelist.remove";
    private Bundle policyBundle = null;

    public HwSystemManagerPlugin(Context context) {
        super(context);
    }

    public String getPluginName() {
        return getClass().getSimpleName();
    }

    public PolicyStruct getPolicyStruct() {
        HwLog.i(TAG, "getPolicyStruct");
        PolicyStruct struct = new PolicyStruct(this);
        PolicyStruct.PolicyType listType = PolicyStruct.PolicyType.LIST;
        PolicyStruct.PolicyType stateType = PolicyStruct.PolicyType.STATE;
        PolicyStruct.PolicyType enterproseListType = PolicyStruct.PolicyType.LIST;
        struct.addStruct("super-whitelist-hwsystemmanager/super-whitelist-hwsystemmanager-item", listType, new String[]{"value"});
        struct.addStruct("data-saver-mode", stateType, new String[]{"value"});
        struct.addStruct("enterprise-whitelist-hwsystemmanager/enterprise-whitelist-hwsystemmanager-item", enterproseListType, new String[]{"value"});
        return struct;
    }

    public boolean checkCallingPermission(ComponentName who, String policyName) {
        HwLog.i(TAG, "checkCallingPermission");
        this.mContext.enforceCallingOrSelfPermission(SYSTEM_MANAGER_PERMISSION, "it does not have SYSTEM_MANAGER_PERMISSION permisson");
        return true;
    }

    public boolean onSetPolicy(ComponentName who, String policyName, Bundle policyData, boolean effective) {
        this.policyBundle = policyData;
        return true;
    }

    public void onSetPolicyCompleted(ComponentName who, String policyName, boolean changed) {
        if (policyName != null && policyName.equals("data-saver-mode") && changed) {
            this.mContext.sendBroadcast(new Intent(UPDATE_DATA_SAVER_MODE), SYSTEM_MANAGER_PERMISSION);
            HwLog.i(TAG, "Broadcast TO HwSystemManager Data Saver");
        } else if (policyName != null && policyName.equals(HwAdminCache.SUPER_WHITE_LIST_APP)) {
            Intent intent = new Intent(UPDATE_NOTIFICATION_SUPERWHITELIST);
            if (this.policyBundle != null) {
                intent.putExtras(this.policyBundle);
            }
            this.mContext.sendBroadcastAsUser(intent, UserHandle.getUserHandleForUid(Binder.getCallingUid()), SYSTEM_MANAGER_PERMISSION);
            HwLog.i(TAG, "Broadcast TO HwSystemManager");
        } else if (policyName != null && policyName.equals(POLICY_ENTERPRISE_WHITE_LIST) && changed) {
            this.mContext.sendBroadcast(new Intent(UPDATE_DATA_ENTERPRISE_LIST_ADD), SYSTEM_MANAGER_PERMISSION);
            HwLog.i(TAG, "Broadcast TO HwSystemManager");
        }
    }

    public boolean onRemovePolicy(ComponentName who, String policyName, Bundle policyData, boolean globalPolicyChanged) {
        if (policyName != null && policyName.equals("data-saver-mode")) {
            this.mContext.sendBroadcast(new Intent(UPDATE_DATA_SAVER_MODE_REMOVED), SYSTEM_MANAGER_PERMISSION);
            HwLog.i(TAG, "onRemove-Broadcast TO HwSystemManager Data Saver");
        } else if (policyName != null && policyName.equals(HwAdminCache.SUPER_WHITE_LIST_APP)) {
            Intent intent = new Intent(UPDATE_NOTIFICATION_SUPERWHITELIST_REMOVE);
            intent.putExtras(policyData);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.getUserHandleForUid(Binder.getCallingUid()), SYSTEM_MANAGER_PERMISSION);
            HwLog.i(TAG, "onRemove-Broadcast TO HwSystemManager");
        } else if (policyName != null && policyName.equals(POLICY_ENTERPRISE_WHITE_LIST)) {
            this.mContext.sendBroadcast(new Intent(UPDATE_DATA_ENTERPRISE_LIST_REMOVE), SYSTEM_MANAGER_PERMISSION);
            HwLog.i(TAG, "Broadcast TO HwSystemManager");
        }
        return true;
    }

    public boolean onActiveAdminRemoved(ComponentName who, ArrayList<PolicyStruct.PolicyItem> arrayList) {
        return true;
    }
}
