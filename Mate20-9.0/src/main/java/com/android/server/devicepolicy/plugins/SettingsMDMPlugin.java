package com.android.server.devicepolicy.plugins;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.text.TextUtils;
import com.android.server.devicepolicy.DevicePolicyPlugin;
import com.android.server.devicepolicy.HwLog;
import com.android.server.devicepolicy.PolicyStruct;
import huawei.android.app.admin.HwDevicePolicyManagerEx;
import java.util.ArrayList;
import java.util.Iterator;

public class SettingsMDMPlugin extends DevicePolicyPlugin {
    public static final String ADM_APK_NAME = "adm_apk_name";
    public static final String ADM_CLASS_NAME = "adm_class_name";
    public static final String CONFIG_NORMAL_VALUE = "NORMAL";
    public static final String CONFIG_NOT_SETUP_VALUE = "not_set_status";
    private static final String[] CONFIG_POLICY_ARRAY = {POLICY_MODIFY_WIFI_SLEEP_MODE_CONFIG, POLICY_MODIFY_FONT_SIZE_CONFIG};
    public static final String DISABLED_ANDROID_ANIMATION = "disabled-android-animation";
    public static final String EMPTY_STRING = "";
    private static final ArrayList<String> LISTENER_APK_LIST = new ArrayList<>();
    private static final String[] LIST_POLICY_ARRAY = {POLICY_ACCESSIBILITY_SERVICES_WHITE_LIST};
    public static final String MDM_POLICY_NAME = "mdm_policy_name";
    public static final String MDM_POLICY_TYPE = "mdm_policy_type";
    public static final String MDM_SETTINGS_PERMISSION = "com.huawei.permission.sec.MDM_SETTINGS_RESTRICTION";
    public static final String POLICY_ACCESSIBILITY_SERVICES_WHITE_LIST = "accessibility_services_white_list";
    public static final String POLICY_ACCESSIBILITY_SERVICES_WHITE_LIST_ITEM = "accessibility_services_white_list/accessibility_services_item";
    public static final boolean POLICY_ACTIVE = true;
    public static final String POLICY_APPLICATION_LOCK = "policy_application_lock_disabled";
    public static final String POLICY_FORBIDDEN_ADD_USER = "settings_policy_forbidden_add_user";
    public static final String POLICY_FORBIDDEN_BLUETOOTH_NET_SHARE = "settings_policy_forbidden_bluetooth_net_share";
    public static final String POLICY_FORBIDDEN_DEVELOPMENT_OPTION = "settings_policy_forbidden_development_option";
    public static final String POLICY_FORBIDDEN_ECHO_PWD = "settings_policy_forbidden_echo_pwd";
    public static final String POLICY_FORBIDDEN_EDIT_WIFI = "settings_policy_forbidden_edit_wifi";
    public static final String POLICY_FORBIDDEN_GOOGLE_BACKUP = "settings_policy_forbidden_google_backup";
    public static final String POLICY_FORBIDDEN_HUAWEI_BEAM = "settings_policy_forbidden_huawei_beam";
    public static final String POLICY_FORBIDDEN_IMMEDIATEL_DESTROY_ACTIVITIES = "settings_policy_forbidden_immediatel_destroy";
    public static final String POLICY_FORBIDDEN_LOCATION_MODE = "settings_policy_forbidden_location_mode";
    public static final String POLICY_FORBIDDEN_LOCATION_SERVICE = "settings_policy_forbidden_location_service";
    public static final String POLICY_FORBIDDEN_MOCK_LOCATION = "settings_policy_forbidden_mock_location";
    public static final String POLICY_FORBIDDEN_MODIFY_TIME_TIMEZONE = "settings_policy_forbidden_modify_time_timezone";
    public static final String POLICY_FORBIDDEN_NETWORK_LOCATION = "settings_policy_forbidden_network_location";
    public static final String POLICY_FORBIDDEN_NETWORK_SHARE = "settings_policy_forbidden_network_share";
    public static final String POLICY_FORBIDDEN_RESTORE_DEVICE = "settings_policy_forbidden_restore_device";
    public static final String POLICY_FORBIDDEN_RESTRICT_BACKGROUND_PROCESS = "settings_policy_forbidden_restrict_background_process";
    public static final String POLICY_FORBIDDEN_SCREEN_OFF = "settings_policy_forbidden_screen_off";
    public static final String POLICY_FORBIDDEN_STOP_SYSTEM_SIGN_APP = "settings_policy_forbidden_stop_system_sign_app";
    public static final String POLICY_FORBIDDEN_UNKNOWN_APP_INSTALL = "settings_policy_forbidden_unknown_app_install";
    public static final String POLICY_FORBIDDEN_UNLOCK_BY_FINGERPRINT = "settings_policy_forbidden_unlock_by_fingerprint";
    public static final String POLICY_FORBIDDEN_USB_NET_SHARE = "settings_policy_forbidden_usb_net_share";
    public static final String POLICY_FORCE_ENCRYPT_SDCARD = "force-encrypt-sdcard";
    public static final boolean POLICY_INACTIVE = false;
    public static final String POLICY_LOCK_PASSWORD_ALLOWED = "settings_policy_lock_password_allowed";
    public static final String POLICY_MODIFY_FONT_SIZE_CONFIG = "settings_policy_modify_font_size_config";
    public static final String POLICY_MODIFY_WIFI_SLEEP_MODE_CONFIG = "settings_policy_modify_wifi_sleep_mode_config";
    public static final String POLICY_PARENT_CONTROL = "policy_parent_control_disabled";
    public static final String POLICY_PHONE_FIND = "policy_phone_find_disabled";
    public static final String POLICY_SEARCH_INDEX_DISABLED = "search_index_disabled";
    public static final String POLICY_SIM_LOCK = "policy_sim_lock_disabled";
    public static final String POLICY_VALUE = "config_value";
    public static final String RECEIVER_ACTION_POLICY_ITEM_REMOVED = "com.android.settings.mdm.receiver.action.MDMPolicyItemRemoved";
    public static final String RECEIVER_ACTION_POLICY_MODIFIED = "com.android.settings.mdm.receiver.action.MDMPolicyModified";
    public static final String RECEIVER_ACTION_POLICY_TOTAL_REMOVED = "com.android.settings.mdm.receiver.action.MDMPolicyTotalRemoved";
    public static final String RECEIVER_ACTION_SETTINGS_ITEM_MODIFIED = "com.android.settings.mdm.receiver.action.MDMSettingsItemModified";
    private static final String RESTORE_PERSISTENT_CONFIG_PROPERTY = "persist.sys.disable_reset";
    public static final String SETTINGS_APK_NAME = "com.android.settings";
    public static final String SETTINGS_MDM_RECEIVER = "com.android.settings.mdm.HwMDMPolicyMonitorReceiver";
    private static final String[] STATE_POLICY_ARRAY = {POLICY_FORBIDDEN_NETWORK_LOCATION, POLICY_FORBIDDEN_UNKNOWN_APP_INSTALL, POLICY_FORBIDDEN_NETWORK_SHARE, POLICY_FORBIDDEN_EDIT_WIFI, POLICY_FORBIDDEN_RESTORE_DEVICE, POLICY_FORBIDDEN_BLUETOOTH_NET_SHARE, POLICY_FORBIDDEN_ADD_USER, POLICY_FORBIDDEN_STOP_SYSTEM_SIGN_APP, POLICY_FORBIDDEN_USB_NET_SHARE, POLICY_FORBIDDEN_MODIFY_TIME_TIMEZONE, POLICY_FORBIDDEN_GOOGLE_BACKUP, POLICY_FORBIDDEN_ECHO_PWD, POLICY_FORBIDDEN_UNLOCK_BY_FINGERPRINT, POLICY_FORBIDDEN_MOCK_LOCATION, POLICY_FORBIDDEN_RESTRICT_BACKGROUND_PROCESS, POLICY_FORBIDDEN_HUAWEI_BEAM, POLICY_FORBIDDEN_SCREEN_OFF, POLICY_FORBIDDEN_DEVELOPMENT_OPTION, POLICY_FORBIDDEN_IMMEDIATEL_DESTROY_ACTIVITIES, POLICY_FORBIDDEN_LOCATION_SERVICE, POLICY_FORBIDDEN_LOCATION_MODE, POLICY_LOCK_PASSWORD_ALLOWED, POLICY_SEARCH_INDEX_DISABLED, POLICY_PHONE_FIND, POLICY_PARENT_CONTROL, POLICY_SIM_LOCK, POLICY_APPLICATION_LOCK, DISABLED_ANDROID_ANIMATION, POLICY_FORCE_ENCRYPT_SDCARD};
    public static final String STATE_VALUE = "value";
    private static final String TAG = SettingsMDMPlugin.class.getSimpleName();
    public static final int TYPE_CFG = 2;
    public static final int TYPE_COMPOUND = 3;
    public static final int TYPE_INVALID = -1;
    public static final int TYPE_LIST = 1;
    public static final int TYPE_STATE = 0;

    static {
        LISTENER_APK_LIST.add("com.android.settings");
    }

    public SettingsMDMPlugin(Context context) {
        super(context);
    }

    public boolean onInit(PolicyStruct policyStruct) {
        HwLog.i(TAG, "onInit() begin.");
        if (policyStruct == null) {
            return false;
        }
        return true;
    }

    public boolean checkCallingPermission(ComponentName who, String policyName) {
        this.mContext.enforceCallingOrSelfPermission(MDM_SETTINGS_PERMISSION, "Do you declare use permission 'com.huawei.permission.sec.MDM_SETTINGS_RESTRICTION' in your manifest?");
        return true;
    }

    public boolean onSetPolicy(ComponentName who, String policyName, Bundle policyData, boolean globalPolicyChanged) {
        String str = TAG;
        HwLog.i(str, "onSetPolicy() begin. policyName: " + policyName + ", globalPolicyChanged: " + globalPolicyChanged);
        boolean retVal = checkPolicyValue(policyName, policyData);
        if (POLICY_FORBIDDEN_RESTORE_DEVICE.equals(policyName) && retVal) {
            SystemProperties.set(RESTORE_PERSISTENT_CONFIG_PROPERTY, policyData.getBoolean("value") ? "true" : "false");
            String str2 = TAG;
            HwLog.i(str2, "property of disable factory reset is set to " + SystemProperties.get(RESTORE_PERSISTENT_CONFIG_PROPERTY, ""));
        }
        if (POLICY_FORCE_ENCRYPT_SDCARD.equals(policyName) && retVal) {
            boolean enabled = policyData.getBoolean("value");
            String str3 = TAG;
            HwLog.i(str3, "force encrypt sdcard is set to " + enabled);
        }
        notifyPolicyChangeEvent(who, RECEIVER_ACTION_POLICY_MODIFIED, policyName, policyData);
        return retVal;
    }

    public boolean onRemovePolicy(ComponentName who, String policyName, Bundle policyData, boolean globalPolicyChanged) {
        String str = TAG;
        HwLog.i(str, "onRemovePolicy() begin. policyName: " + policyName + ", globalPolicyChanged: " + globalPolicyChanged);
        notifyPolicyChangeEvent(who, RECEIVER_ACTION_POLICY_ITEM_REMOVED, policyName, policyData);
        return true;
    }

    public boolean onGetPolicy(ComponentName who, String policyName, Bundle policyData) {
        String str = TAG;
        HwLog.i(str, "onGetPolicy() policyName: " + policyName);
        return true;
    }

    public boolean onActiveAdminRemoved(ComponentName who, ArrayList<PolicyStruct.PolicyItem> removedPolicies) {
        notifyPolicyChangeEvent(who, RECEIVER_ACTION_POLICY_TOTAL_REMOVED);
        boolean active = false;
        if (removedPolicies != null) {
            Iterator<PolicyStruct.PolicyItem> it = removedPolicies.iterator();
            while (it.hasNext()) {
                String policyName = it.next().getPolicyName();
                if (POLICY_FORBIDDEN_RESTORE_DEVICE.equals(policyName)) {
                    Bundle bundle = new HwDevicePolicyManagerEx().getPolicy(null, policyName);
                    if (bundle != null) {
                        active = bundle.getBoolean("value");
                    }
                    String str = TAG;
                    HwLog.i(str, "active = " + active);
                    if (!active) {
                        SystemProperties.set(RESTORE_PERSISTENT_CONFIG_PROPERTY, "false");
                    }
                }
            }
        }
        return true;
    }

    public String getPluginName() {
        return getClass().getSimpleName();
    }

    public PolicyStruct getPolicyStruct() {
        PolicyStruct struct = new PolicyStruct(this);
        addStateTypeStruct(struct);
        addConfigTypeStruct(struct);
        addListTypeStruct(struct);
        return struct;
    }

    private boolean notifyPolicyChangeEvent(ComponentName who, String action) {
        int apkListSize = LISTENER_APK_LIST.size();
        for (int i = 0; i < apkListSize; i++) {
            sendFixComponentBroadcast(who, action, LISTENER_APK_LIST.get(i));
        }
        return true;
    }

    private boolean notifyPolicyChangeEvent(ComponentName who, String action, String policyName, Bundle policy) {
        int apkListSize = LISTENER_APK_LIST.size();
        for (int i = 0; i < apkListSize; i++) {
            sendFixComponentBroadcast(who, action, LISTENER_APK_LIST.get(i), policyName, policy);
        }
        return true;
    }

    private boolean sendFixComponentBroadcast(ComponentName who, String action, String apkName) {
        return sendFixComponentBroadcast(who, action, apkName, null, null);
    }

    private boolean sendFixComponentBroadcast(ComponentName who, String action, String apkName, String policyName, Bundle policyData) {
        String str = TAG;
        HwLog.i(str, "sendFixComponentBroadcast() send broadcast action: " + action + ", package: " + apkName + ", policyName: " + policyName);
        Intent it = new Intent(action);
        it.setPackage(apkName);
        Bundle data = new Bundle();
        if (policyName != null) {
            data.putString(MDM_POLICY_NAME, policyName);
        }
        if (who != null) {
            String admName = who.getPackageName();
            data.putString(ADM_APK_NAME, TextUtils.isEmpty(admName) ? "" : admName);
            String admClass = who.getClassName();
            data.putString(ADM_CLASS_NAME, TextUtils.isEmpty(admClass) ? "" : admClass);
        }
        if (policyData != null) {
            int policyType = policyData.getInt(MDM_POLICY_TYPE, -1);
            if (policyType == 0) {
                boolean value = policyData.getBoolean("value", false);
                data.putInt(MDM_POLICY_TYPE, 0);
                data.putBoolean("value", value);
            } else if (2 == policyType) {
                String config = policyData.getString("value", CONFIG_NORMAL_VALUE);
                if (CONFIG_NORMAL_VALUE.equals(config)) {
                    HwLog.w(TAG, "sendFixComponentBroadcast() set CONFIGURE type policy, and not give valid value.");
                }
                data.putInt(MDM_POLICY_TYPE, 2);
                data.putString("value", config);
            } else if (1 == policyType) {
                ArrayList list = policyData.getStringArrayList("value");
                data.putInt(MDM_POLICY_TYPE, 1);
                data.putStringArrayList("value", list);
            } else {
                data.putInt(MDM_POLICY_TYPE, 3);
                data.putBundle(POLICY_VALUE, policyData);
            }
        }
        it.putExtras(data);
        this.mContext.sendBroadcast(it);
        return true;
    }

    private void addStateTypeStruct(PolicyStruct struct) {
        for (String policyName : STATE_POLICY_ARRAY) {
            struct.addStruct(policyName, PolicyStruct.PolicyType.STATE, new String[]{"value"});
        }
    }

    private void addConfigTypeStruct(PolicyStruct struct) {
        for (String policyName : CONFIG_POLICY_ARRAY) {
            struct.addStruct(policyName, PolicyStruct.PolicyType.CONFIGURATION, new String[]{"value"});
        }
    }

    private void addListTypeStruct(PolicyStruct struct) {
        struct.addStruct(POLICY_ACCESSIBILITY_SERVICES_WHITE_LIST_ITEM, PolicyStruct.PolicyType.LIST, new String[]{"value"});
    }

    private int getPolicyType(String policyName) {
        int i = 0;
        for (String policy : STATE_POLICY_ARRAY) {
            if (policy.equals(policyName)) {
                return 0;
            }
        }
        for (String policy2 : CONFIG_POLICY_ARRAY) {
            if (policy2.equals(policyName)) {
                return 2;
            }
        }
        while (true) {
            int i2 = i;
            if (i2 >= LIST_POLICY_ARRAY.length) {
                return -1;
            }
            if (LIST_POLICY_ARRAY[i2].equals(policyName)) {
                return 1;
            }
            i = i2 + 1;
        }
    }

    private boolean checkPolicyValue(String policyName, Bundle policy) {
        if (-1 == getPolicyType(policyName) || policy == null) {
            String str = TAG;
            HwLog.e(str, "checkPolicyValue() get unknown policy: " + policyName + "OR get null policy.");
            return false;
        }
        boolean retVal = true;
        int policyType = policy.getInt(MDM_POLICY_TYPE, -1);
        if (2 == policyType && CONFIG_NORMAL_VALUE.equals(policy.getString("value", CONFIG_NORMAL_VALUE))) {
            HwLog.w(TAG, "checkPolicyValue() set CONFIGURE type policy, and not give valid value.");
            retVal = false;
        }
        String config = TAG;
        HwLog.i(config, "checkPolicyValue() get policy type: " + policyType + ", retVal: " + retVal);
        return retVal;
    }
}
