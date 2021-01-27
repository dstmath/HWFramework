package com.android.server.devicepolicy.plugins;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import com.android.server.devicepolicy.DevicePolicyPlugin;
import com.android.server.devicepolicy.HwLog;
import com.android.server.devicepolicy.PolicyStruct;
import java.util.ArrayList;
import java.util.Iterator;

public class PhoneManagerPlugin extends DevicePolicyPlugin {
    private static final String DATA_ROAMING_SIM2 = "data_roaming_sim2";
    private static final String INCOMING_DAY_LIMIT = "calls_limitation_incoming_day/mt_day_limit";
    private static final String INCOMING_MONTH_LIMIT = "calls_limitation_incoming_month/mt_month_limit";
    private static final String INCOMING_WEEK_LIMIT = "calls_limitation_incoming_week/mt_week_limit";
    private static final String NODE_VALUE = "value";
    private static final String OUTGOING_DAY_LIMIT = "calls_limitation_outgoing_day/mo_day_limit";
    private static final String OUTGOING_MONTH_LIMIT = "calls_limitation_outgoing_month/mo_month_limit";
    private static final String OUTGOING_WEEK_LIMIT = "calls_limitation_outgoing_week/mo_week_limit";
    private static final String PHONE_NUMBER_INCOMING_LIMIT = "incoming_limit";
    private static final String PHONE_NUMBER_INCOMING_LIST_ITEM = "phone-number-incoming-list/phone-number-list-item";
    private static final String PHONE_NUMBER_OUTGOING_LIMIT = "outgoing_limit";
    private static final String PHONE_NUMBER_OUTGOING_LIST_ITEM = "phone-number-outgoing-list/phone-number-list-item";
    private static final String PHONE_PERMISSION = "com.huawei.permission.sec.MDM_PHONE_MANAGER";
    private static final String TAG = PhoneManagerPlugin.class.getSimpleName();
    private static final String TELECOM_PACKAGE_NAME = "com.android.server.telecom";
    private static final String TELESERVICE_PACKAGE_NAME = "com.android.phone";
    private static final String UPDATE_PHONE_NUMBER_SHOWING_DISBALE = "com.huawei.contacts.action.PHONE_NUMBER_SHOWING_CHANGED";
    private Context mContext;
    private ContentResolver mResolver = this.mContext.getContentResolver();

    public PhoneManagerPlugin(Context context) {
        super(context);
        this.mContext = context;
    }

    public String getPluginName() {
        return getClass().getSimpleName();
    }

    public PolicyStruct getPolicyStruct() {
        PolicyStruct struct = new PolicyStruct(this);
        PolicyStruct.PolicyType stateType = PolicyStruct.PolicyType.STATE;
        struct.addStruct("disallow-data-roaming", stateType, new String[]{"value"});
        struct.addStruct("disallow-access-point-name", stateType, new String[]{"value"});
        struct.addStruct("disallow-phone-number-showing", stateType, new String[]{"value"});
        struct.addStruct("phone-number-incoming-is-black-list-mode", stateType, new String[]{"value"});
        struct.addStruct("phone-number-outgoing-is-black-list-mode", stateType, new String[]{"value"});
        struct.addStruct("apply-to-all-incoming-calls", stateType, new String[]{"value"});
        struct.addStruct("apply-to-all-outgoing-calls", stateType, new String[]{"value"});
        struct.addStruct("disallow-non-emergency-call", stateType, new String[]{"value"});
        struct.addStruct("disallow-roaming-call", stateType, new String[]{"value"});
        struct.addStruct("disallow-multi-calling", stateType, new String[]{"value"});
        PolicyStruct.PolicyType listType = PolicyStruct.PolicyType.LIST;
        struct.addStruct("phone-number-incoming-list", listType, new String[0]);
        struct.addStruct("phone-number-outgoing-list", listType, new String[0]);
        struct.addStruct(PHONE_NUMBER_INCOMING_LIST_ITEM, listType, new String[]{"value"});
        struct.addStruct(PHONE_NUMBER_OUTGOING_LIST_ITEM, listType, new String[]{"value"});
        PolicyStruct.PolicyType configType = PolicyStruct.PolicyType.CONFIGURATION;
        struct.addStruct("calls_limitation_outgoing_day", configType, new String[0]);
        struct.addStruct(OUTGOING_DAY_LIMIT, configType, new String[]{"outgoing_day_limit"});
        struct.addStruct("calls_limitation_outgoing_week", configType, new String[0]);
        struct.addStruct(OUTGOING_WEEK_LIMIT, configType, new String[]{"outgoing_week_limit"});
        struct.addStruct("calls_limitation_outgoing_month", configType, new String[0]);
        struct.addStruct(OUTGOING_MONTH_LIMIT, configType, new String[]{"outgoing_month_limit"});
        struct.addStruct("calls_limitation_incoming_day", configType, new String[0]);
        struct.addStruct(INCOMING_DAY_LIMIT, configType, new String[]{"incoming_day_limit"});
        struct.addStruct("calls_limitation_incoming_week", configType, new String[0]);
        struct.addStruct(INCOMING_WEEK_LIMIT, configType, new String[]{"incoming_week_limit"});
        struct.addStruct("calls_limitation_incoming_month", configType, new String[0]);
        struct.addStruct(INCOMING_MONTH_LIMIT, configType, new String[]{"incoming_month_limit"});
        struct.addStruct("call-status-notification-app", configType, new String[]{"value"});
        return struct;
    }

    public boolean checkCallingPermission(ComponentName who, String policyName) {
        Context context = this.mContext;
        if (context == null) {
            return false;
        }
        context.enforceCallingOrSelfPermission(PHONE_PERMISSION, "Permission not granted to access phone manager.");
        return true;
    }

    public boolean onSetPolicy(ComponentName who, String policyName, Bundle policyData, boolean isGlobalPolicyChanged) {
        String str = TAG;
        HwLog.i(str, "onSetPolicy: " + policyName);
        if (policyData == null) {
            return false;
        }
        if (!"disallow-data-roaming".equals(policyName)) {
            return true;
        }
        onSetDataRoamingPolicy(policyData);
        return true;
    }

    public void onSetPolicyCompleted(ComponentName who, String policyName, boolean isChanged) {
        if ("disallow-data-roaming".equals(policyName)) {
            sendConfigChangedBroadcast(TELESERVICE_PACKAGE_NAME, "disallow-data-roaming");
        }
        if ("disallow-access-point-name".equals(policyName)) {
            sendConfigChangedBroadcast(TELESERVICE_PACKAGE_NAME, "disallow-access-point-name");
        }
        if ("disallow-phone-number-showing".equals(policyName) && isChanged) {
            broadcastForNumberHideChanged();
        }
    }

    public boolean onRemovePolicy(ComponentName who, String policyName, Bundle policyData, boolean isGlobalPolicyChanged) {
        String str = TAG;
        HwLog.i(str, "onRemovePolicy: " + policyName);
        return true;
    }

    public void onRemovePolicyCompleted(ComponentName who, String policyName, boolean isChanged) {
        if (isCallsLimitPolicy(policyName)) {
            sendCallsLimitRemovedBroadcast(TELECOM_PACKAGE_NAME, policyName);
        }
    }

    private boolean isCallsLimitPolicy(String policyName) {
        for (String policy : new String[]{"calls_limitation_outgoing_day", "calls_limitation_outgoing_week", "calls_limitation_outgoing_month", "calls_limitation_incoming_day", "calls_limitation_incoming_week", "calls_limitation_incoming_month"}) {
            if (policy.equals(policyName)) {
                return true;
            }
        }
        return false;
    }

    private void sendCallsLimitRemovedBroadcast(String packageName, String policyName) {
        if (this.mContext != null) {
            long callingId = Binder.clearCallingIdentity();
            try {
                HwLog.i(TAG, "sendBroadcast sendCallsLimitRemovedBroadcast.");
                Intent intent = new Intent("com.huawei.devicepolicy.action.POLICY_CHANGED");
                intent.setPackage(TELECOM_PACKAGE_NAME);
                intent.putExtra("policy_name", policyName);
                intent.putExtra("calls_limit_removed", true);
                this.mContext.sendBroadcastAsUser(intent, UserHandle.of(ActivityManager.getCurrentUser()));
            } finally {
                Binder.restoreCallingIdentity(callingId);
            }
        }
    }

    public boolean onActiveAdminRemoved(ComponentName admin, ArrayList<PolicyStruct.PolicyItem> arrayList) {
        return true;
    }

    public void onActiveAdminRemovedCompleted(ComponentName admin, ArrayList<PolicyStruct.PolicyItem> removedPolicies) {
        if (this.mContext != null && removedPolicies != null) {
            boolean isSendToTelecom = false;
            boolean isSendToTeleService = false;
            Iterator<PolicyStruct.PolicyItem> it = removedPolicies.iterator();
            while (it.hasNext()) {
                PolicyStruct.PolicyItem pi = it.next();
                if (pi != null && pi.isGlobalPolicyChanged()) {
                    String policyName = pi.getPolicyName();
                    if (isCallsLimitPolicy(policyName)) {
                        isSendToTelecom = true;
                    }
                    if ("disallow-data-roaming".equals(policyName) || "disallow-access-point-name".equals(policyName)) {
                        isSendToTeleService = true;
                    }
                    if ("disallow-phone-number-showing".equals(policyName)) {
                        broadcastForNumberHideChanged();
                    }
                }
            }
            if (isSendToTelecom) {
                sendAdminRemovedBroadcast(TELECOM_PACKAGE_NAME);
            }
            if (isSendToTeleService) {
                sendAdminRemovedBroadcast(TELESERVICE_PACKAGE_NAME);
            }
        }
    }

    private void sendAdminRemovedBroadcast(String packageName) {
        long callingId = Binder.clearCallingIdentity();
        try {
            HwLog.i(TAG, "sendBroadcast onActiveAdminRemovedCompleted.");
            Intent intent = new Intent("com.huawei.devicepolicy.action.POLICY_CHANGED");
            intent.putExtra("admin_removed", true);
            intent.setPackage(packageName);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.of(ActivityManager.getCurrentUser()));
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    private void onSetDataRoamingPolicy(Bundle policyData) {
        if (policyData == null) {
            HwLog.i(TAG, "onSetDataRoamingPolicy policyData is null");
            return;
        }
        boolean isRestrictDataRoaming = policyData.getBoolean("value", false);
        long callingId = Binder.clearCallingIdentity();
        if (isRestrictDataRoaming) {
            try {
                disableDataRoaming();
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(callingId);
                throw th;
            }
        }
        Binder.restoreCallingIdentity(callingId);
    }

    private void sendConfigChangedBroadcast(String packageName, String type) {
        if (this.mContext != null) {
            long callingId = Binder.clearCallingIdentity();
            try {
                String str = TAG;
                HwLog.i(str, "sendBroadcast type= " + type);
                Intent intent = new Intent("com.huawei.devicepolicy.action.POLICY_CHANGED");
                intent.setPackage(packageName);
                intent.putExtra("policy_name", type);
                this.mContext.sendBroadcastAsUser(intent, UserHandle.of(ActivityManager.getCurrentUser()));
            } finally {
                Binder.restoreCallingIdentity(callingId);
            }
        }
    }

    private void broadcastForNumberHideChanged() {
        if (this.mContext != null) {
            long callingId = Binder.clearCallingIdentity();
            try {
                HwLog.i(TAG, "sendBroadcast for:disallow-phone-number-showing");
                this.mContext.sendBroadcastAsUser(new Intent(UPDATE_PHONE_NUMBER_SHOWING_DISBALE), UserHandle.of(ActivityManager.getCurrentUser()), PHONE_PERMISSION);
            } finally {
                Binder.restoreCallingIdentity(callingId);
            }
        }
    }

    private void disableDataRoaming() {
        Settings.Global.putInt(this.mResolver, "data_roaming", 0);
        if (TelephonyManager.getDefault().getSimCount() != 1) {
            Settings.Global.putInt(this.mResolver, DATA_ROAMING_SIM2, 0);
        }
    }
}
