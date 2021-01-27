package com.android.server.devicepolicy.plugins;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.text.TextUtils;
import com.android.server.devicepolicy.DevicePolicyPlugin;
import com.android.server.devicepolicy.HwLog;
import com.android.server.devicepolicy.PolicyStruct;
import huawei.android.app.admin.HwDevicePolicyManagerEx;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface;

public class DeviceSettingsPlugin extends DevicePolicyPlugin {
    public static final String ADM_APK_NAME = "adm_apk_name";
    public static final String ADM_CLASS_NAME = "adm_class_name";
    public static final String CONFIG_NORMAL_VALUE = "NORMAL";
    public static final String CONFIG_NOT_SETUP_VALUE = "not_set_status";
    private static final String[] CONFIG_POLICY_ARRAY = {POLICY_MODIFY_WIFI_SLEEP_MODE_CONFIG, POLICY_MODIFY_FONT_SIZE_CONFIG};
    public static final String DISABLED_ANDROID_ANIMATION = "disabled-android-animation";
    private static final String DISABLE_FACTORY_RESET = "true";
    public static final String EMPTY_STRING = "";
    private static final String ENABLE_FACTORY_RESET = "false";
    private static final int FACTORY_OEM_DATA_SIZE = 16;
    private static final int FACTORY_OEM_MAIN_ID = 204;
    private static final int FACTORY_OEM_SUB_ID = 32;
    private static final List<String> LISTENER_APK_LIST = new ArrayList();
    private static final String[] LIST_POLICY_ARRAY = {POLICY_ACCESSIBILITY_SERVICES_WHITE_LIST};
    public static final String MDM_POLICY_NAME = "mdm_policy_name";
    public static final String MDM_POLICY_TYPE = "mdm_policy_type";
    public static final String MDM_SETTINGS_PERMISSION = "com.huawei.permission.sec.MDM_SETTINGS_RESTRICTION";
    public static final String POLICY_ACCESSIBILITY_SERVICES_WHITE_LIST = "accessibility_services_white_list";
    public static final String POLICY_ACCESSIBILITY_SERVICES_WHITE_LIST_ITEM = "accessibility_services_white_list/accessibility_services_item";
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
    private static final String TAG = DeviceSettingsPlugin.class.getSimpleName();
    private static final String THREAD_NAME_OF_INIT_FACTORY_RESET = "MDM_Init_FactoryReset";
    public static final int TYPE_CFG = 2;
    public static final int TYPE_COMPOUND = 3;
    public static final int TYPE_INVALID = -1;
    public static final int TYPE_LIST = 1;
    public static final int TYPE_STATE = 0;

    static {
        LISTENER_APK_LIST.add(SETTINGS_APK_NAME);
    }

    public DeviceSettingsPlugin(Context context) {
        super(context);
    }

    private boolean setFactoryResetStatus(boolean isDisabled, boolean isSync) {
        if (!disableRestoreFactory(isDisabled)) {
            String str = TAG;
            HwLog.w(str, "set factory reset to " + isDisabled + " failed");
            return false;
        } else if (!isSync) {
            return true;
        } else {
            SystemProperties.set(RESTORE_PERSISTENT_CONFIG_PROPERTY, isDisabled ? DISABLE_FACTORY_RESET : ENABLE_FACTORY_RESET);
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initRestoreFactoryStatus() {
        String status = SystemProperties.get(RESTORE_PERSISTENT_CONFIG_PROPERTY, EMPTY_STRING);
        boolean isDisabled = isRestoreFactoryDisabled();
        String str = TAG;
        HwLog.i(str, "prop_factory_reset: " + status + " oem_factory_reset: " + isDisabled);
        if ((DISABLE_FACTORY_RESET.equals(status) && !isDisabled) || (ENABLE_FACTORY_RESET.equals(status) && isDisabled)) {
            String str2 = TAG;
            HwLog.i(str2, "init factory reset to " + status + " from prop");
            setFactoryResetStatus(isDisabled ^ true, false);
        }
        if (TextUtils.isEmpty(status)) {
            String statusFromXml = PluginUtils.readValueFromXml(POLICY_FORBIDDEN_RESTORE_DEVICE);
            String str3 = TAG;
            HwLog.i(str3, "xml_factory_reset: " + statusFromXml);
            if (TextUtils.isEmpty(statusFromXml) && isDisabled) {
                setFactoryResetStatus(false, true);
            }
            if ((DISABLE_FACTORY_RESET.equals(statusFromXml) && !isDisabled) || (ENABLE_FACTORY_RESET.equals(statusFromXml) && isDisabled)) {
                String str4 = TAG;
                HwLog.i(str4, "init factory reset to " + statusFromXml + " from xml");
                setFactoryResetStatus(isDisabled ^ true, true);
            }
        }
    }

    public boolean onInit(PolicyStruct policyStruct) {
        HwLog.i(TAG, "onInit() begin.");
        Thread thread = new Thread(new Runnable() {
            /* class com.android.server.devicepolicy.plugins.DeviceSettingsPlugin.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                try {
                    DeviceSettingsPlugin.this.initRestoreFactoryStatus();
                } catch (Exception e) {
                    HwLog.e(DeviceSettingsPlugin.TAG, "MDM_Init_FactoryReset exception!");
                }
            }
        }, THREAD_NAME_OF_INIT_FACTORY_RESET);
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            /* class com.android.server.devicepolicy.plugins.DeviceSettingsPlugin.AnonymousClass2 */

            @Override // java.lang.Thread.UncaughtExceptionHandler
            public void uncaughtException(Thread thread, Throwable throwable) {
                HwLog.e(DeviceSettingsPlugin.TAG, "MDM_Init_FactoryReset uncaught exception!");
            }
        });
        thread.start();
        if (policyStruct == null) {
            return false;
        }
        return true;
    }

    public boolean checkCallingPermission(ComponentName who, String policyName) {
        this.mContext.enforceCallingOrSelfPermission(MDM_SETTINGS_PERMISSION, "Do you declare use permission 'com.huawei.permission.sec.MDM_SETTINGS_RESTRICTION' in your manifest?");
        return true;
    }

    public boolean onSetPolicy(ComponentName who, String policyName, Bundle policyData, boolean isGlobalPolicyChanged) {
        if (policyData == null) {
            HwLog.i(TAG, "onSetPolicy policyData is null");
            return false;
        }
        String str = TAG;
        HwLog.i(str, "onSetPolicy() begin. policyName: " + policyName + ", globalPolicyChanged: " + isGlobalPolicyChanged);
        boolean retVal = checkPolicyValue(policyName, policyData);
        if (POLICY_FORCE_ENCRYPT_SDCARD.equals(policyName) && retVal) {
            boolean isEnabled = policyData.getBoolean(STATE_VALUE);
            String str2 = TAG;
            HwLog.i(str2, "force encrypt sdcard is set to " + isEnabled);
        }
        notifyPolicyChangeEvent(who, RECEIVER_ACTION_POLICY_MODIFIED, policyName, policyData);
        return retVal;
    }

    public void onSetPolicyCompleted(ComponentName admin, String policyName, boolean isChanged) {
        if (POLICY_FORBIDDEN_RESTORE_DEVICE.equals(policyName) && isChanged) {
            boolean isDisabled = false;
            Bundle bundle = new HwDevicePolicyManagerEx().getPolicy((ComponentName) null, policyName);
            if (bundle != null) {
                isDisabled = bundle.getBoolean(STATE_VALUE);
            }
            String str = TAG;
            HwLog.i(str, "onSetPolicyCompleted factory reset policy value = " + isDisabled);
            setFactoryResetStatus(isDisabled, true);
        }
    }

    private boolean disableRestoreFactory(boolean isDisabled) {
        String status = isDisabled ? DISABLE_FACTORY_RESET : ENABLE_FACTORY_RESET;
        String str = TAG;
        HwLog.i(str, "oem of disable factory reset origin is " + isRestoreFactoryDisabled());
        boolean isSuccess = true;
        try {
            IHwFactoryInterface hwFactoryInterface = IHwFactoryInterface.getService("hwfactoryinterface_hal", true);
            if (hwFactoryInterface == null) {
                HwLog.w(TAG, "write factory reset init failed");
                return false;
            }
            if (hwFactoryInterface.oeminfo_write_reused((int) FACTORY_OEM_MAIN_ID, (int) FACTORY_OEM_SUB_ID, status.length(), status) != 0) {
                HwLog.w(TAG, "write factory reset failed");
                isSuccess = false;
            }
            if (isSuccess) {
                String str2 = TAG;
                HwLog.i(str2, "oem of disable factory reset is set to " + isRestoreFactoryDisabled());
            }
            return isSuccess;
        } catch (RemoteException e) {
            HwLog.w(TAG, "write factory reset exception");
            isSuccess = false;
        } catch (NoSuchElementException e2) {
            HwLog.w(TAG, "write factory reset NoSuchElementException exception");
            isSuccess = false;
        }
    }

    private boolean isOeminfoDisabled(int ret, String oemInfoValue) {
        if (ret != 0 || oemInfoValue == null || !oemInfoValue.startsWith(DISABLE_FACTORY_RESET)) {
            return false;
        }
        HwLog.i(TAG, "oem of factory reset is true");
        return true;
    }

    private boolean isRestoreFactoryDisabled() {
        boolean isDisabled = false;
        try {
            IHwFactoryInterface hwFactoryInterface = IHwFactoryInterface.getService("hwfactoryinterface_hal", true);
            if (hwFactoryInterface == null) {
                HwLog.w(TAG, "read factory reset init failed!");
                return false;
            }
            final Map<String, Object> result = new HashMap<>();
            result.put("ret", -1);
            hwFactoryInterface.oeminfo_Read_reused((int) FACTORY_OEM_MAIN_ID, (int) FACTORY_OEM_SUB_ID, (int) FACTORY_OEM_DATA_SIZE, new IHwFactoryInterface.oeminfo_Read_reusedCallback() {
                /* class com.android.server.devicepolicy.plugins.DeviceSettingsPlugin.AnonymousClass3 */

                public void onValues(int ret, String out) {
                    result.put("ret", Integer.valueOf(ret));
                    result.put("out", out);
                }
            });
            Object retFlag = result.get("ret");
            Object output = result.get("out");
            if ((retFlag instanceof Integer) && (output instanceof String)) {
                isDisabled = isOeminfoDisabled(((Integer) retFlag).intValue(), (String) output);
            }
            return isDisabled;
        } catch (RemoteException e) {
            HwLog.w(TAG, "read factory reset exception");
        } catch (NoSuchElementException e2) {
            HwLog.w(TAG, "read factory reset NoSuchElementException exception");
        }
    }

    public boolean onRemovePolicy(ComponentName who, String policyName, Bundle policyData, boolean isGlobalPolicyChanged) {
        String str = TAG;
        HwLog.i(str, "onRemovePolicy() begin. policyName: " + policyName + ", globalPolicyChanged: " + isGlobalPolicyChanged);
        notifyPolicyChangeEvent(who, RECEIVER_ACTION_POLICY_ITEM_REMOVED, policyName, policyData);
        return true;
    }

    public boolean onActiveAdminRemoved(ComponentName who, ArrayList<PolicyStruct.PolicyItem> arrayList) {
        HwLog.i(TAG, "onActiveAdminRemoved");
        notifyPolicyChangeEvent(who, RECEIVER_ACTION_POLICY_TOTAL_REMOVED);
        return true;
    }

    public void onActiveAdminRemovedCompleted(ComponentName who, ArrayList<PolicyStruct.PolicyItem> removedPolicies) {
        if (removedPolicies != null) {
            boolean isDisabled = false;
            Iterator<PolicyStruct.PolicyItem> it = removedPolicies.iterator();
            while (it.hasNext()) {
                String policyName = it.next().getPolicyName();
                if (POLICY_FORBIDDEN_RESTORE_DEVICE.equals(policyName)) {
                    Bundle bundle = new HwDevicePolicyManagerEx().getPolicy((ComponentName) null, policyName);
                    if (bundle != null) {
                        isDisabled = bundle.getBoolean(STATE_VALUE);
                    }
                    String str = TAG;
                    HwLog.i(str, "onActiveAdminRemovedCompleted factory reset policy value = " + isDisabled);
                    setFactoryResetStatus(isDisabled, true);
                }
            }
        }
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
        for (String item : LISTENER_APK_LIST) {
            sendFixComponentBroadcast(who, action, item);
        }
        return true;
    }

    private boolean notifyPolicyChangeEvent(ComponentName who, String action, String policyName, Bundle policy) {
        for (String item : LISTENER_APK_LIST) {
            sendFixComponentBroadcast(who, action, item, policyName, policy);
        }
        return true;
    }

    private boolean sendFixComponentBroadcast(ComponentName who, String action, String apkName) {
        return sendFixComponentBroadcast(who, action, apkName, null, null);
    }

    private boolean sendFixComponentBroadcast(ComponentName who, String action, String apkName, String policyName, Bundle policyData) {
        String str = TAG;
        HwLog.i(str, "sendFixComponentBroadcast() send broadcast action: " + action + ", policyName: " + policyName);
        Intent it = new Intent(action);
        it.setPackage(apkName);
        Bundle data = new Bundle();
        if (policyName != null) {
            data.putString(MDM_POLICY_NAME, policyName);
        }
        if (who != null) {
            String admName = who.getPackageName();
            boolean isEmpty = TextUtils.isEmpty(admName);
            String admClass = EMPTY_STRING;
            data.putString(ADM_APK_NAME, isEmpty ? admClass : admName);
            String admClass2 = who.getClassName();
            if (!TextUtils.isEmpty(admClass2)) {
                admClass = admClass2;
            }
            data.putString(ADM_CLASS_NAME, admClass);
        }
        if (policyData != null) {
            int policyType = policyData.getInt(MDM_POLICY_TYPE, -1);
            if (policyType == 0) {
                boolean isActive = policyData.getBoolean(STATE_VALUE, false);
                data.putInt(MDM_POLICY_TYPE, 0);
                data.putBoolean(STATE_VALUE, isActive);
            } else if (policyType == 2) {
                String config = policyData.getString(STATE_VALUE, CONFIG_NORMAL_VALUE);
                if (CONFIG_NORMAL_VALUE.equals(config)) {
                    HwLog.w(TAG, "sendFixComponentBroadcast() set CONFIGURE type policy, and not give valid value.");
                }
                data.putInt(MDM_POLICY_TYPE, 2);
                data.putString(STATE_VALUE, config);
            } else if (policyType == 1) {
                ArrayList<String> lists = null;
                try {
                    lists = policyData.getStringArrayList(STATE_VALUE);
                } catch (ArrayIndexOutOfBoundsException e) {
                    HwLog.e(TAG, "disableApplicationList exception.");
                }
                data.putInt(MDM_POLICY_TYPE, 1);
                data.putStringArrayList(STATE_VALUE, lists);
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
            struct.addStruct(policyName, PolicyStruct.PolicyType.STATE, new String[]{STATE_VALUE});
        }
    }

    private void addConfigTypeStruct(PolicyStruct struct) {
        for (String policyName : CONFIG_POLICY_ARRAY) {
            struct.addStruct(policyName, PolicyStruct.PolicyType.CONFIGURATION, new String[]{STATE_VALUE});
        }
    }

    private void addListTypeStruct(PolicyStruct struct) {
        struct.addStruct(POLICY_ACCESSIBILITY_SERVICES_WHITE_LIST_ITEM, PolicyStruct.PolicyType.LIST, new String[]{STATE_VALUE});
    }

    private int getPolicyType(String policyName) {
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
        for (String policy3 : LIST_POLICY_ARRAY) {
            if (policy3.equals(policyName)) {
                return 1;
            }
        }
        return -1;
    }

    private boolean checkPolicyValue(String policyName, Bundle policy) {
        if (getPolicyType(policyName) == -1 || policy == null) {
            String str = TAG;
            HwLog.e(str, "checkPolicyValue() get unknown policy: " + policyName + "OR get null policy.");
            return false;
        }
        boolean isConfigType = true;
        int policyType = policy.getInt(MDM_POLICY_TYPE, -1);
        if (policyType == 2 && CONFIG_NORMAL_VALUE.equals(policy.getString(STATE_VALUE, CONFIG_NORMAL_VALUE))) {
            HwLog.w(TAG, "checkPolicyValue() set CONFIGURE type policy, and not give valid value.");
            isConfigType = false;
        }
        String str2 = TAG;
        HwLog.i(str2, "checkPolicyValue() get policy type: " + policyType + ", retVal: " + isConfigType);
        return isConfigType;
    }
}
