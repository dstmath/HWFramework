package com.android.server.devicepolicy.plugins;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import com.android.server.devicepolicy.DevicePolicyPlugin;
import com.android.server.devicepolicy.HwLog;
import com.android.server.devicepolicy.PolicyStruct;
import com.android.server.devicepolicy.PolicyStruct.PolicyItem;
import com.android.server.devicepolicy.PolicyStruct.PolicyType;
import java.util.ArrayList;

public class DeviceNetworkPlugin extends DevicePolicyPlugin {
    private static final String ACTION_NETWORK_BLACK_LIST_CHANGED = "com.huawei.devicepolicy.action.POLICY_CHANGED";
    private static final Uri HISTORY_URI = Uri.parse("content://com.android.browser/history");
    private static final String ITEM_NETWORK_BLACK_LIST = "network-black-list/network-black-list-item";
    private static final int MAX_NUM = 1000;
    private static final String POLICY_NETWORK_BLACK_LIST = "network-black-list";
    public static final String TAG = DeviceNetworkPlugin.class.getSimpleName();
    private static final String URL_COLUMN_NAME = "url";

    public DeviceNetworkPlugin(Context context) {
        super(context);
    }

    public String getPluginName() {
        return getClass().getSimpleName();
    }

    public PolicyStruct getPolicyStruct() {
        PolicyStruct struct = new PolicyStruct(this);
        struct.addStruct(POLICY_NETWORK_BLACK_LIST, PolicyType.LIST, new String[0]);
        struct.addStruct(ITEM_NETWORK_BLACK_LIST, PolicyType.LIST, new String[]{"value"});
        return struct;
    }

    public boolean onInit(PolicyStruct policyStruct) {
        HwLog.i(TAG, "onInit");
        if (policyStruct != null) {
            return true;
        }
        HwLog.d(TAG, "policyStruct of DeviceNetworkPlugin is null");
        return false;
    }

    public boolean checkCallingPermission(ComponentName who, String policyName) {
        if (policyName.equals(POLICY_NETWORK_BLACK_LIST)) {
            HwLog.i(TAG, "check the calling Permission");
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_NETWORK_MANAGER", "does not have network_manager MDM permission!");
            return true;
        }
        HwLog.e(TAG, "unknown policy name: " + policyName);
        return false;
    }

    public boolean onSetPolicy(ComponentName who, String policyName, Bundle policyData, boolean changed) {
        if (this.mPolicyStruct == null || policyData == null) {
            HwLog.i(TAG, "policy struct of the black list of network is null");
            return false;
        } else if (policyName.equals(POLICY_NETWORK_BLACK_LIST)) {
            HwLog.i(TAG, "onSetPolicy and policyName: " + policyName + " changed:" + changed);
            ArrayList<String> data = policyData.getStringArrayList("value");
            if (data == null || data.size() == 0) {
                return false;
            }
            if (!changed) {
                return true;
            }
            ArrayList<String> policies = this.mPolicyStruct.getPolicyItem(POLICY_NETWORK_BLACK_LIST).combineAllAttributes().getStringArrayList("value");
            if (policies == null || !data.removeAll(policies) || policies.size() + data.size() <= 1000) {
                return true;
            }
            throw new IllegalArgumentException("Black list beyond maximum number");
        } else {
            HwLog.e(TAG, "unknown policy name: " + policyName);
            return false;
        }
    }

    public void onSetPolicyCompleted(ComponentName who, String policyName, boolean changed) {
        if (changed) {
            if (policyName.equals(POLICY_NETWORK_BLACK_LIST)) {
                HwLog.i(TAG, "send broadcast when on set policy completed.");
                sendBroadcast("com.huawei.devicepolicy.action.POLICY_CHANGED");
            } else {
                HwLog.e(TAG, "unknown policy name: " + policyName);
            }
        }
    }

    public boolean onRemovePolicy(ComponentName who, String policyName, Bundle policyData, boolean changed) {
        return true;
    }

    public void onRemovePolicyCompleted(ComponentName who, String policyName, boolean changed) {
        if (changed) {
            if (policyName.equals(POLICY_NETWORK_BLACK_LIST)) {
                HwLog.i(TAG, "send broadcast when on remove policy completed.");
                sendBroadcast("com.huawei.devicepolicy.action.POLICY_CHANGED");
            } else {
                HwLog.e(TAG, "unknown policy name: " + policyName);
            }
        }
    }

    public boolean onActiveAdminRemoved(ComponentName who, ArrayList<PolicyItem> arrayList) {
        return true;
    }

    public void onActiveAdminRemovedCompleted(ComponentName who, ArrayList<PolicyItem> arrayList) {
        HwLog.i(TAG, "the active admin has been Removed");
        sendBroadcast("com.huawei.devicepolicy.action.POLICY_CHANGED");
    }

    public ArrayList<String> queryBrowsingHistory() {
        ArrayList<String> historyList = new ArrayList();
        Cursor cursor = this.mContext.getContentResolver().query(HISTORY_URI, new String[]{URL_COLUMN_NAME}, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                historyList.add(cursor.getString(cursor.getColumnIndex(URL_COLUMN_NAME)));
            }
            cursor.close();
        } else {
            HwLog.d(TAG, "query browser histroy cursor is null ");
        }
        return historyList;
    }

    private void sendBroadcast(String action) {
        Intent intent = new Intent(action);
        intent.setPackage("com.android.browser");
        this.mContext.sendBroadcast(intent);
    }
}
