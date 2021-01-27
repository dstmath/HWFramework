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
import com.android.server.devicepolicy.BdReportUtils;
import com.android.server.devicepolicy.DevicePolicyPlugin;
import com.android.server.devicepolicy.HwDevicePolicyManagerServiceUtil;
import com.android.server.devicepolicy.HwLog;
import com.android.server.devicepolicy.PolicyStruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.regex.Pattern;

public class DevicePackageManagerPlugin extends DevicePolicyPlugin {
    private static final String ALLOW_NOTIFY_DB_KEY = "allow_notification_white_apps";
    private static final String ATTR_VALUE = "value";
    private static final String BROADCAST_PKG_NAME = "com.huawei.systemmanager";
    private static final String BROADCAST_SYSTEM_PKG_NAME = "android";
    private static final String NOTIFICATION_INTENT_ACTION = "com.huawei.devicepolicy.action.POLICY_CHANGED";
    private static final String NOTIFY_APP_WHITE = "notification-app-bt-white-list";
    private static final String NOTIFY_APP_WHITE_ITEM = "notification-app-bt-white-list/notification-app-bt-white-list-item";
    private static final String POLICY_NAME = "policy_name";
    private static final String POLICY_UPDATE_NFN_WHITE_LIST_DATA = "com.huawei.systemmanager.update_notification_white_list_data";
    private static final String TAG = DevicePackageManagerPlugin.class.getSimpleName();
    private static final String UPDATE_SYS_APP_INSTALL_LIST = "update-sys-app-install-list";
    private static final String UPDATE_SYS_APP_UNDETACHABLE_INSTALL_LIST = "update-sys-app-undetachable-install-list";

    public DevicePackageManagerPlugin(Context context) {
        super(context);
    }

    public String getPluginName() {
        return getClass().getSimpleName();
    }

    public PolicyStruct getPolicyStruct() {
        HwLog.i(TAG, "getPolicyStruct");
        PolicyStruct struct = new PolicyStruct(this);
        struct.addStruct(UPDATE_SYS_APP_INSTALL_LIST, PolicyStruct.PolicyType.CONFIGURATION, new String[]{"value"});
        struct.addStruct(UPDATE_SYS_APP_UNDETACHABLE_INSTALL_LIST, PolicyStruct.PolicyType.CONFIGURATION, new String[]{"value"});
        struct.addStruct(NOTIFY_APP_WHITE_ITEM, PolicyStruct.PolicyType.LIST, new String[]{"value"});
        return struct;
    }

    public boolean checkCallingPermission(ComponentName who, String policyName) {
        HwLog.i(TAG, "checkCallingPermission");
        if (UPDATE_SYS_APP_INSTALL_LIST.equals(policyName)) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_INSTALL_SYS_APP", "does not have INSTALL_SYS_APP MDM permission!");
            return true;
        } else if (UPDATE_SYS_APP_UNDETACHABLE_INSTALL_LIST.equals(policyName)) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_INSTALL_SYS_APP", "does not have INSTALL_SYS_APP MDM permission!");
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_INSTALL_UNDETACHABLE_APP", "does not have INSTALL_UNDETACHABLE_SYS_APP MDM permission!");
            return true;
        } else {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "does not have app_management MDM permission!");
            return true;
        }
    }

    public boolean onSetPolicy(ComponentName who, String policyName, Bundle policyData, boolean isChanged) {
        HwLog.i(TAG, "onSetPolicy");
        if (!isEffective(who, policyName, policyData)) {
            return false;
        }
        char c = 65535;
        int hashCode = policyName.hashCode();
        if (hashCode != -1003065536) {
            if (hashCode != -79527545) {
                if (hashCode == 496906511 && policyName.equals(NOTIFY_APP_WHITE)) {
                    c = 0;
                }
            } else if (policyName.equals(UPDATE_SYS_APP_UNDETACHABLE_INSTALL_LIST)) {
                c = 2;
            }
        } else if (policyName.equals(UPDATE_SYS_APP_INSTALL_LIST)) {
            c = 1;
        }
        if (c == 0) {
            return setNotificationDataToDb(policyData);
        }
        if (c != 1 && c != 2) {
            return true;
        }
        BdReportUtils.reportSetPolicyPkgData(who, this.mContext, policyData);
        return setSysAppDataToSystem(policyData, policyName);
    }

    public boolean onRemovePolicy(ComponentName who, String policyName, Bundle policyData, boolean isEffective) {
        HwLog.i(TAG, "onRemovePolicy");
        if (!isEffective(who, policyName, policyData)) {
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
        if (!isEffective(who, policyName, policyData)) {
            return false;
        }
        char c = 65535;
        if (policyName.hashCode() == 496906511 && policyName.equals(NOTIFY_APP_WHITE)) {
            c = 0;
        }
        if (c != 0) {
            return true;
        }
        getNotificationData(policyData);
        return true;
    }

    public void onSetPolicyCompleted(ComponentName who, String policyName, boolean isChanged) {
        HwLog.i(TAG, "onSetPolicyCompleted");
        if (policyName != null && policyName.equals(NOTIFY_APP_WHITE) && isChanged) {
            sendBroadCastForHwSystemManager();
        }
    }

    public boolean onActiveAdminRemoved(ComponentName who, ArrayList<PolicyStruct.PolicyItem> removedPolicies) {
        HwLog.i(TAG, "onActiveAdminRemoved");
        if (removedPolicies == null) {
            return true;
        }
        Iterator<PolicyStruct.PolicyItem> it = removedPolicies.iterator();
        while (it.hasNext()) {
            PolicyStruct.PolicyItem item = it.next();
            if (item != null && item.getPolicyName().equals(NOTIFY_APP_WHITE)) {
                cleanAllowNotificationDb();
                sendBroadCastForHwSystemManager();
            }
        }
        return true;
    }

    private void cleanAllowNotificationDb() {
        Settings.Global.putStringForUser(this.mContext.getContentResolver(), ALLOW_NOTIFY_DB_KEY, DeviceSettingsPlugin.EMPTY_STRING, 0);
    }

    public boolean isEffective(ComponentName who, String policyName, Bundle policyData) {
        if (policyData == null) {
            return true;
        }
        if (NOTIFY_APP_WHITE.equals(policyName)) {
            ArrayList<String> packageNames = null;
            try {
                packageNames = policyData.getStringArrayList("value");
            } catch (ArrayIndexOutOfBoundsException e) {
                HwLog.e(TAG, "NOTIFY_APP_WHITE exception.");
            }
            if (!HwDevicePolicyManagerServiceUtil.isValidatePackageNames(packageNames)) {
                throw new IllegalArgumentException("packageName:" + packageNames + " is invalid.");
            }
        }
        if (UPDATE_SYS_APP_INSTALL_LIST.equals(policyName) || UPDATE_SYS_APP_UNDETACHABLE_INSTALL_LIST.equals(policyName)) {
            String value = policyData.getString("value");
            if (!TextUtils.isEmpty(value) && !isValidatePolicyValue(value)) {
                throw new IllegalArgumentException("policy value:" + value + " is invalid.");
            }
        }
        return true;
    }

    private boolean removeNotificationDataFromDb(Bundle bundle) {
        if (bundle == null) {
            HwLog.i(TAG, "removeNotificationDataFromDb bundle is null");
            return false;
        }
        ArrayList<String> listDatas = null;
        try {
            listDatas = bundle.getStringArrayList("value");
        } catch (ArrayIndexOutOfBoundsException e) {
            HwLog.e(TAG, "removeNotificationDataFromDb exception.");
        }
        if (listDatas == null || listDatas.isEmpty()) {
            return false;
        }
        long identityToken = Binder.clearCallingIdentity();
        try {
            ArrayList<String> notifnlistDatas = getNotificationDataFromDb();
            Iterator<String> it = listDatas.iterator();
            while (it.hasNext()) {
                String notifyData = it.next();
                if (notifnlistDatas.contains(notifyData)) {
                    notifnlistDatas.remove(notifyData);
                }
            }
            return Settings.Global.putStringForUser(this.mContext.getContentResolver(), ALLOW_NOTIFY_DB_KEY, formatNotificationData(notifnlistDatas, DeviceSettingsPlugin.EMPTY_STRING), 0);
        } finally {
            Binder.restoreCallingIdentity(identityToken);
        }
    }

    private void getNotificationData(Bundle policyData) {
        if (policyData == null) {
            policyData = new Bundle();
        }
        policyData.putStringArrayList("value", getNotificationDataFromDb());
    }

    private ArrayList<String> getNotificationDataFromDb() {
        ArrayList<String> lists = new ArrayList<>();
        String notificationWhiteApps = Settings.Global.getStringForUser(this.mContext.getContentResolver(), ALLOW_NOTIFY_DB_KEY, 0);
        if (!TextUtils.isEmpty(notificationWhiteApps)) {
            String[] notificationWhiteAppsArray = notificationWhiteApps.split(";");
            if (notificationWhiteAppsArray.length > 0) {
                Collections.addAll(lists, notificationWhiteAppsArray);
            }
        }
        return lists;
    }

    private boolean setNotificationDataToDb(Bundle bundle) {
        if (bundle == null) {
            HwLog.i(TAG, "setNotificationDataToDb bundle is null");
            return false;
        }
        ArrayList<String> lists = null;
        try {
            lists = bundle.getStringArrayList("value");
        } catch (ArrayIndexOutOfBoundsException e) {
            HwLog.e(TAG, "removeNotificationDataFromDb exception.");
        }
        if (lists == null || lists.isEmpty()) {
            return false;
        }
        long identityToken = Binder.clearCallingIdentity();
        try {
            return Settings.Global.putStringForUser(this.mContext.getContentResolver(), ALLOW_NOTIFY_DB_KEY, formatNotificationData(lists, Settings.Global.getStringForUser(this.mContext.getContentResolver(), ALLOW_NOTIFY_DB_KEY, 0)), 0);
        } finally {
            Binder.restoreCallingIdentity(identityToken);
        }
    }

    private String formatNotificationData(ArrayList<String> list, String notificationDatas) {
        StringBuffer buffer = TextUtils.isEmpty(notificationDatas) ? new StringBuffer() : new StringBuffer(notificationDatas);
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            buffer.append(it.next() + ";");
        }
        return buffer.toString();
    }

    private void sendBroadCastForHwSystemManager() {
        HwLog.i(TAG, "sendBroadCastForHwSystemManager");
        long callingId = Binder.clearCallingIdentity();
        try {
            Intent intent = new Intent(NOTIFICATION_INTENT_ACTION);
            intent.setPackage(BROADCAST_PKG_NAME);
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
            Intent intent = new Intent(NOTIFICATION_INTENT_ACTION);
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
