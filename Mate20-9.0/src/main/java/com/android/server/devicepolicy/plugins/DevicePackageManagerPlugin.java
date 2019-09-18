package com.android.server.devicepolicy.plugins;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import com.android.server.devicepolicy.DevicePolicyPlugin;
import com.android.server.devicepolicy.HwDevicePolicyManagerServiceUtil;
import com.android.server.devicepolicy.HwLog;
import com.android.server.devicepolicy.PolicyStruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;

public class DevicePackageManagerPlugin extends DevicePolicyPlugin {
    private static final String ALLOW_NOTIFY_DB_KEY = "allow_notification_white_apps";
    private static final String ATTR_VALUE = "value";
    private static final String BROADCAST_PKG_NAME = "com.huawei.systemmanager";
    private static final String BROADCAST_SYSTEM_PKG_NAME = "android";
    private static final String NOTIFY_APP_WHITE = "notification-app-bt-white-list";
    private static final String POLICY_CHANGED_INTENT_ACTION = "com.huawei.devicepolicy.action.POLICY_CHANGED";
    private static final String POLICY_NAME = "policy_name";
    private static final String POLICY_UPDATE_NFN_WHITE_LIST_DATA = "com.huawei.systemmanager.update_notification_white_list_data";
    public static final String TAG = DevicePackageManagerPlugin.class.getSimpleName();

    public DevicePackageManagerPlugin(Context context) {
        super(context);
    }

    public String getPluginName() {
        return getClass().getSimpleName();
    }

    public PolicyStruct getPolicyStruct() {
        HwLog.i(TAG, "getPolicyStruct");
        PolicyStruct struct = new PolicyStruct(this);
        struct.addStruct(NOTIFY_APP_WHITE, PolicyStruct.PolicyType.LIST, new String[0]);
        struct.addStruct("update-sys-app-install-list", PolicyStruct.PolicyType.CONFIGURATION, new String[]{"value"});
        struct.addStruct("update-sys-app-undetachable-install-list", PolicyStruct.PolicyType.CONFIGURATION, new String[]{"value"});
        return struct;
    }

    public boolean checkCallingPermission(ComponentName who, String policyName) {
        HwLog.i(TAG, "checkCallingPermission");
        if ("update-sys-app-install-list".equals(policyName)) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_INSTALL_SYS_APP", "does not have INSTALL_SYS_APP MDM permission!");
        } else if ("update-sys-app-undetachable-install-list".equals(policyName)) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_INSTALL_SYS_APP", "does not have INSTALL_SYS_APP MDM permission!");
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_INSTALL_UNDETACHABLE_APP", "does not have INSTALL_UNDETACHABLE_SYS_APP MDM permission!");
        } else {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "does not have app_management MDM permission!");
        }
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x002c, code lost:
        if (r7.equals(NOTIFY_APP_WHITE) == false) goto L_0x0045;
     */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0049 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x004a  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x004f  */
    public boolean onSetPolicy(ComponentName who, String policyName, Bundle policyData, boolean effective) {
        HwLog.i(TAG, "onSetPolicy");
        char c = 0;
        if (!isNotEffective(who, policyName, policyData, effective)) {
            int hashCode = policyName.hashCode();
            if (hashCode == -1003065536) {
                if (policyName.equals("update-sys-app-install-list")) {
                    c = 1;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                        case 2:
                            break;
                    }
                }
            } else if (hashCode != -79527545) {
                if (hashCode == 496906511) {
                }
            } else if (policyName.equals("update-sys-app-undetachable-install-list")) {
                c = 2;
                switch (c) {
                    case 0:
                        return setNotificationDataToDb(policyData);
                    case 1:
                    case 2:
                        return setSysAppDataToSystem(policyData, policyName);
                    default:
                        return true;
                }
            }
            c = 65535;
            switch (c) {
                case 0:
                    break;
                case 1:
                case 2:
                    break;
            }
        } else {
            return false;
        }
    }

    public boolean onRemovePolicy(ComponentName who, String policyName, Bundle policyData, boolean effective) {
        HwLog.i(TAG, "onRemovePolicy");
        if (isNotEffective(who, policyName, policyData, effective)) {
            return false;
        }
        char c = 65535;
        if (policyName.hashCode() == 496906511 && policyName.equals(NOTIFY_APP_WHITE)) {
            c = 0;
        }
        if (c != 0) {
            return true;
        }
        boolean result = removeNotificationDataFromDb(policyData);
        if (result) {
            sendBroadCastForHwSystemManager();
        }
        return result;
    }

    public boolean onGetPolicy(ComponentName who, String policyName, Bundle policyData) {
        HwLog.i(TAG, "onGetPolicy");
        if (isNotEffective(who, policyName, policyData, true)) {
            return false;
        }
        char c = 65535;
        if (policyName.hashCode() == 496906511 && policyName.equals(NOTIFY_APP_WHITE)) {
            c = 0;
        }
        if (c != 0) {
            return true;
        }
        return getNotificationData(policyData);
    }

    public void onSetPolicyCompleted(ComponentName who, String policyName, boolean changed) {
        HwLog.i(TAG, "onSetPolicyCompleted");
        if (policyName != null && policyName.equals(NOTIFY_APP_WHITE) && changed) {
            sendBroadCastForHwSystemManager();
        }
    }

    public boolean onActiveAdminRemoved(ComponentName who, ArrayList<PolicyStruct.PolicyItem> removedPolicies) {
        HwLog.i(TAG, "onActiveAdminRemoved");
        if (removedPolicies != null && removedPolicies.size() > 0) {
            int k = removedPolicies.size();
            for (int j = 0; j < k; j++) {
                if (removedPolicies.get(j).getPolicyName().equals(NOTIFY_APP_WHITE)) {
                    cleanAllowNotificationDb();
                    sendBroadCastForHwSystemManager();
                }
            }
        }
        return true;
    }

    private void cleanAllowNotificationDb() {
        Settings.Global.putStringForUser(this.mContext.getContentResolver(), ALLOW_NOTIFY_DB_KEY, "", 0);
    }

    public boolean isNotEffective(ComponentName who, String policyName, Bundle policyData, boolean effective) {
        if (policyData != null && NOTIFY_APP_WHITE.equals(policyName)) {
            ArrayList<String> packageNames = policyData.getStringArrayList("value");
            if (!HwDevicePolicyManagerServiceUtil.isValidatePackageNames(packageNames)) {
                throw new IllegalArgumentException("packageName:" + packageNames + " is invalid.");
            }
        }
        if (policyData != null && ("update-sys-app-install-list".equals(policyName) || "update-sys-app-undetachable-install-list".equals(policyName))) {
            String value = policyData.getString("value");
            if (!TextUtils.isEmpty(value) && !isValidatePolicyValue(value)) {
                throw new IllegalArgumentException("policy value:" + value + " is invalid.");
            }
        }
        return false;
    }

    private boolean removeNotificationDataFromDb(Bundle bundle) {
        ArrayList<String> listData = bundle.getStringArrayList("value");
        if (listData == null || listData.isEmpty()) {
            return false;
        }
        long identityToken = Binder.clearCallingIdentity();
        boolean result = false;
        try {
            ArrayList<String> notifnlistData = getNotificationDataFromDb();
            if (notifnlistData != null) {
                StringBuffer buffer = new StringBuffer();
                int size = listData.size();
                for (int i = 0; i < size; i++) {
                    String notifyData = listData.get(i);
                    if (notifnlistData.contains(notifyData)) {
                        notifnlistData.remove(notifyData);
                    } else {
                        buffer.append(notifyData + CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                    }
                }
                result = Settings.Global.putStringForUser(this.mContext.getContentResolver(), ALLOW_NOTIFY_DB_KEY, formatNotificationData(notifnlistData, buffer.toString()), 0);
            }
            return result;
        } finally {
            Binder.restoreCallingIdentity(identityToken);
        }
    }

    private boolean getNotificationData(Bundle policyData) {
        if (policyData == null) {
            policyData = new Bundle();
        }
        policyData.putStringArrayList("value", getNotificationDataFromDb());
        return true;
    }

    private ArrayList<String> getNotificationDataFromDb() {
        String notificationWhiteApps = Settings.Global.getStringForUser(this.mContext.getContentResolver(), ALLOW_NOTIFY_DB_KEY, 0);
        if (TextUtils.isEmpty(notificationWhiteApps)) {
            return null;
        }
        ArrayList<String> list = new ArrayList<>();
        String[] notificationWhiteAppsArray = notificationWhiteApps.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
        if (notificationWhiteAppsArray.length > 0) {
            Collections.addAll(list, notificationWhiteAppsArray);
        }
        return list;
    }

    private boolean setNotificationDataToDb(Bundle bundle) {
        ArrayList<String> list = bundle.getStringArrayList("value");
        if (list == null || list.isEmpty()) {
            return false;
        }
        long identityToken = Binder.clearCallingIdentity();
        try {
            return Settings.Global.putStringForUser(this.mContext.getContentResolver(), ALLOW_NOTIFY_DB_KEY, formatNotificationData(list, Settings.Global.getStringForUser(this.mContext.getContentResolver(), ALLOW_NOTIFY_DB_KEY, 0)), 0);
        } finally {
            Binder.restoreCallingIdentity(identityToken);
        }
    }

    private String formatNotificationData(ArrayList<String> list, String notificationDatas) {
        StringBuffer buffer = TextUtils.isEmpty(notificationDatas) ? new StringBuffer() : new StringBuffer(notificationDatas);
        int size = list.size();
        for (int i = 0; i < size; i++) {
            buffer.append(list.get(i) + CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
        }
        return buffer.toString();
    }

    private void sendBroadCastForHwSystemManager() {
        HwLog.i(TAG, "sendBroadCastForHwSystemManager");
        long callingId = Binder.clearCallingIdentity();
        try {
            Intent intent = new Intent("com.huawei.devicepolicy.action.POLICY_CHANGED");
            intent.setPackage("com.huawei.systemmanager");
            intent.putExtra(POLICY_NAME, POLICY_UPDATE_NFN_WHITE_LIST_DATA);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.of(ActivityManager.getCurrentUser()));
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    /* JADX INFO: finally extract failed */
    private boolean setSysAppDataToSystem(Bundle bundle, String policyName) {
        long callingId = Binder.clearCallingIdentity();
        try {
            Intent intent = new Intent("com.huawei.devicepolicy.action.POLICY_CHANGED");
            intent.setPackage(BROADCAST_SYSTEM_PKG_NAME);
            intent.putExtras(bundle);
            intent.putExtra(POLICY_NAME, policyName);
            this.mContext.sendBroadcast(intent, "android.permission.MANAGE_PROFILE_AND_DEVICE_OWNERS");
            Binder.restoreCallingIdentity(callingId);
            return true;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
            throw th;
        }
    }

    private boolean isValidatePolicyValue(String value) {
        if (!TextUtils.isEmpty(value) && Pattern.matches("^[0-9a-zA-Z][0-9a-zA-Z_.:;]*$", value)) {
            return true;
        }
        return false;
    }
}
