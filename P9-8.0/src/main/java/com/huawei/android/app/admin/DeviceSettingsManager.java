package com.huawei.android.app.admin;

import android.content.ComponentName;
import android.os.Bundle;
import android.util.Log;
import huawei.android.app.admin.HwDevicePolicyManagerEx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class DeviceSettingsManager {
    public static final String CONFIG_NORMAL_VALUE = "NORMAL";
    public static final String CONFIG_NOT_SETUP_VALUE = "not_set_status";
    private static final String DISABLE_NAVIGATIONBAR = "disable-navigationbar";
    private static final String DISABLE_NOTIFICATION = "disable-notification";
    public static final String EMPTY_STRING = "";
    public static final int FONT_MDM_POLICY_REMOVED = -1;
    public static final int FONT_SIZE_EXTRA_HUGE = 5;
    private static final float FONT_SIZE_EXTRA_HUGE_VALUE = 1.30001f;
    public static final int FONT_SIZE_HUGE = 4;
    private static final float FONT_SIZE_HUGE_VALUE = 1.3f;
    public static final int FONT_SIZE_LARGE = 3;
    private static final float FONT_SIZE_LARGE_VALUE = 1.15f;
    public static final int FONT_SIZE_NORMAL = 2;
    private static final float FONT_SIZE_NORMAL_VALUE = 1.0f;
    public static final int FONT_SIZE_SMALL = 1;
    private static final float FONT_SIZE_SMALL_VALUE = 0.85f;
    private static final HashMap<Integer, Float> FONT_SIZE_TYPES = new HashMap();
    public static final String MDM_POLICY_NAME = "mdm_policy_name";
    public static final String MDM_POLICY_TYPE = "mdm_policy_type";
    public static final String MDM_SETTINGS_PERMISSION = "com.huawei.permission.sec.PERMISSION_MDM_DEVICE_SETTINGS_MANAGER";
    public static final String POLICY_ACCESSIBILITY_SERVICES_WHITE_LIST = "accessibility_services_white_list";
    public static final boolean POLICY_ACTIVE = true;
    public static final String POLICY_FORBIDDEN_ADD_USER = "settings_policy_forbidden_add_user";
    public static final String POLICY_FORBIDDEN_BLUETOOTH_NET_SHARE = "settings_policy_forbidden_bluetooth_net_share";
    public static final String POLICY_FORBIDDEN_DEVELOPMENT_OPTION = "settings_policy_forbidden_development_option";
    public static final String POLICY_FORBIDDEN_ECHO_PWD = "settings_policy_forbidden_echo_pwd";
    public static final String POLICY_FORBIDDEN_EDIT_APN = "settings_policy_forbidden_edit_apn";
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
    public static final boolean POLICY_INACTIVE = false;
    public static final String POLICY_LOCK_PASSWORD_ALLOWED = "settings_policy_lock_password_allowed";
    public static final String POLICY_MODIFY_FONT_SIZE_CONFIG = "settings_policy_modify_font_size_config";
    public static final String POLICY_MODIFY_WIFI_SLEEP_MODE_CONFIG = "settings_policy_modify_wifi_sleep_mode_config";
    public static final String POLICY_SEARCH_INDEX_DISABLED = "search_index_disabled";
    public static final String RECEIVER_ACTION_POLICY_ITEM_REMOVED = "com.android.settings.mdm.receiver.action.MDMPolicyItemRemoved";
    public static final String RECEIVER_ACTION_POLICY_MODIFIED = "com.android.settings.mdm.receiver.action.MDMPolicyModified";
    public static final String RECEIVER_ACTION_POLICY_TOTAL_REMOVED = "com.android.settings.mdm.receiver.action.MDMPolicyTotalRemoved";
    public static final String RECEIVER_ACTION_SETTINGS_ITEM_MODIFIED = "com.android.settings.mdm.receiver.action.MDMSettingsItemModified";
    public static final String SETTINGS_APK_NAME = "com.android.settings";
    public static final String SETTINGS_MDM_RECEIVER = "com.android.settings.mdm.HwMDMPolicyMonitorReceiver";
    public static final String STATE_VALUE = "value";
    private static final String TAG = DeviceSettingsManager.class.getSimpleName();
    public static final int TYPE_CFG = 2;
    public static final int TYPE_COMPOUND = 3;
    public static final int TYPE_INVALID = -1;
    public static final int TYPE_LIST = 1;
    public static final int TYPE_STATE = 0;
    public static final String WIFI_STANDBY_MODE_ALWAYS = "always";
    private static final HashMap<Integer, String> WIFI_STANDBY_MODE_MAP = new HashMap();
    public static final String WIFI_STANDBY_MODE_NEVER = "never";
    public static final String WIFI_STANDBY_MODE_NORMAL = "normal";
    public static final int WIFI_STANDBY_MODE_NORMAL_INT_VALUE = -1;
    public static final String WIFI_STANDBY_MODE_ON_CHARGE_ONLY = "on-charge-only";
    private HwDevicePolicyManagerEx mDpm = new HwDevicePolicyManagerEx();

    static {
        WIFI_STANDBY_MODE_MAP.put(Integer.valueOf(2), WIFI_STANDBY_MODE_ALWAYS);
        WIFI_STANDBY_MODE_MAP.put(Integer.valueOf(1), WIFI_STANDBY_MODE_ON_CHARGE_ONLY);
        WIFI_STANDBY_MODE_MAP.put(Integer.valueOf(0), WIFI_STANDBY_MODE_NEVER);
        WIFI_STANDBY_MODE_MAP.put(Integer.valueOf(-1), WIFI_STANDBY_MODE_NORMAL);
        FONT_SIZE_TYPES.put(Integer.valueOf(1), Float.valueOf(FONT_SIZE_SMALL_VALUE));
        FONT_SIZE_TYPES.put(Integer.valueOf(2), Float.valueOf(FONT_SIZE_NORMAL_VALUE));
        FONT_SIZE_TYPES.put(Integer.valueOf(3), Float.valueOf(FONT_SIZE_LARGE_VALUE));
        FONT_SIZE_TYPES.put(Integer.valueOf(4), Float.valueOf(FONT_SIZE_HUGE_VALUE));
        FONT_SIZE_TYPES.put(Integer.valueOf(5), Float.valueOf(FONT_SIZE_EXTRA_HUGE_VALUE));
    }

    public boolean setNetworkLocationDisabled(ComponentName admin, boolean disable) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_NETWORK_LOCATION, disable);
    }

    private boolean setStateTypePolicy(ComponentName admin, String policyName, boolean activePolicy) {
        return this.mDpm.setPolicy(admin, policyName, buildStateTypeData(activePolicy));
    }

    private Bundle buildStateTypeData(boolean disable) {
        Bundle value = new Bundle();
        value.putInt(MDM_POLICY_TYPE, 0);
        value.putBoolean("value", disable);
        return value;
    }

    public boolean isNetworkLocationDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_NETWORK_LOCATION);
    }

    private boolean getStateTypePolicyActiveStatus(ComponentName admin, String policyName) {
        Log.i(TAG, "getStateTypePolicyActiveStatus() raw policyName: " + policyName);
        return isStatePolicyActive(getStateTypeDataFromBundle(this.mDpm.getPolicy(admin, policyName)));
    }

    private boolean getStateTypeDataFromBundle(Bundle policyValue) {
        return getStateTypeDataFromBundle(policyValue, false);
    }

    private boolean getStateTypeDataFromBundle(Bundle policyValue, boolean defaultValue) {
        if (policyValue != null) {
            return policyValue.getBoolean("value", defaultValue);
        }
        Log.w(TAG, "getStateTypeDataFromBundle() get null data. will return default value: " + defaultValue);
        return defaultValue;
    }

    private boolean isStatePolicyActive(boolean state) {
        return state;
    }

    public boolean setUnknownSourceAppInstallDisabled(ComponentName admin, boolean disable) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_UNKNOWN_APP_INSTALL, disable);
    }

    public boolean isUnknownSourceAppInstallDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_UNKNOWN_APP_INSTALL);
    }

    public boolean setAllTetheringDisabled(ComponentName admin, boolean disable) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_NETWORK_SHARE, disable);
    }

    public boolean isAllTetheringDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_NETWORK_SHARE);
    }

    public boolean setWIFIeditDisabled(ComponentName admin, boolean disable) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_EDIT_WIFI, disable);
    }

    public boolean isWIFIeditDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_EDIT_WIFI);
    }

    public boolean setRestoreFactoryDisabled(ComponentName admin, boolean disable) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_RESTORE_DEVICE, disable);
    }

    public boolean isRestoreFactoryDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_RESTORE_DEVICE);
    }

    public boolean setWIFIStandbyMode(ComponentName admin, int config) {
        if (!isValidWIFIStandbyMode(config)) {
            return false;
        }
        String policyFormatValue = getWIFIStandbyModePolicyValue(config);
        if (WIFI_STANDBY_MODE_NORMAL.equals(policyFormatValue)) {
            return removeConfigTypePolicy(admin, POLICY_MODIFY_WIFI_SLEEP_MODE_CONFIG);
        }
        return setConfigTypePolicy(admin, POLICY_MODIFY_WIFI_SLEEP_MODE_CONFIG, policyFormatValue);
    }

    private boolean removeConfigTypePolicy(ComponentName admin, String policyName) {
        return this.mDpm.removePolicy(admin, policyName, null);
    }

    private boolean setConfigTypePolicy(ComponentName admin, String policyName, String config) {
        return this.mDpm.setPolicy(admin, policyName, buildConfigTypeData(config));
    }

    private Bundle buildConfigTypeData(String config) {
        Bundle value = new Bundle();
        value.putInt(MDM_POLICY_TYPE, 2);
        value.putString("value", config);
        return value;
    }

    private static boolean isValidWIFIStandbyMode(int config) {
        return WIFI_STANDBY_MODE_MAP.keySet().contains(Integer.valueOf(config));
    }

    private static String getWIFIStandbyModePolicyValue(int policyStatus) {
        if (isValidWIFIStandbyMode(policyStatus)) {
            return (String) WIFI_STANDBY_MODE_MAP.get(Integer.valueOf(policyStatus));
        }
        return WIFI_STANDBY_MODE_NORMAL;
    }

    public int getWIFIStandbyMode(ComponentName admin) {
        return getWIFIStandbyModeSystemValue(getConfigTypePolicyStatus(admin, POLICY_MODIFY_WIFI_SLEEP_MODE_CONFIG));
    }

    private String getConfigTypePolicyStatus(ComponentName admin, String policyName) {
        Log.i(TAG, "getConfigTypePolicyStatus() admin:" + admin + " policyName: " + policyName);
        return getConfigTypeDataFromBundle(this.mDpm.getPolicy(admin, policyName), CONFIG_NOT_SETUP_VALUE);
    }

    private static int getWIFIStandbyModeSystemValue(String policyStatus) {
        for (Entry<Integer, String> entry : WIFI_STANDBY_MODE_MAP.entrySet()) {
            if (((String) entry.getValue()).equals(policyStatus)) {
                return ((Integer) entry.getKey()).intValue();
            }
        }
        return -1;
    }

    private String getConfigTypeDataFromBundle(Bundle policyValue, String defaultValue) {
        if (policyValue == null) {
            return defaultValue;
        }
        return policyValue.getString("value", defaultValue);
    }

    public boolean setBluetoothTetheringDisabled(ComponentName admin, boolean disable) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_BLUETOOTH_NET_SHARE, disable);
    }

    public boolean isBluetoothTetheringDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_BLUETOOTH_NET_SHARE);
    }

    public boolean setAddUserDisabled(ComponentName admin, boolean disable) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_ADD_USER, disable);
    }

    public boolean isAddUserDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_ADD_USER);
    }

    public boolean setEchoPasswordDisabled(ComponentName admin, boolean disable) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_ECHO_PWD, disable);
    }

    public boolean isEchoPasswordDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_ECHO_PWD);
    }

    public boolean setForceStopSystemSignatureAppDisabled(ComponentName admin, boolean disable) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_STOP_SYSTEM_SIGN_APP, disable);
    }

    public boolean isForceStopSystemSignatureAppDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_STOP_SYSTEM_SIGN_APP);
    }

    public boolean setUSBTetheringDisabled(ComponentName admin, boolean disable) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_USB_NET_SHARE, disable);
    }

    public boolean isUSBTetheringDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_USB_NET_SHARE);
    }

    public boolean setTimeAndDateSetDisabled(ComponentName admin, boolean disable) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_MODIFY_TIME_TIMEZONE, disable);
    }

    public boolean isTimeAndDateSetDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_MODIFY_TIME_TIMEZONE);
    }

    public boolean setGoogleBackupRestoreDisabled(ComponentName admin, boolean disable) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_GOOGLE_BACKUP, disable);
    }

    public boolean isGoogleBackupRestoreDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_GOOGLE_BACKUP);
    }

    public boolean setScreenOffDisabled(ComponentName admin, boolean disable) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_SCREEN_OFF, disable);
    }

    public boolean isScreenOffDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_SCREEN_OFF);
    }

    public boolean setFontSize(ComponentName admin, int size) {
        if (-1 == size) {
            return removeConfigTypePolicy(admin, POLICY_MODIFY_FONT_SIZE_CONFIG);
        }
        if (!isValidFontSizeValue(size)) {
            return false;
        }
        return setConfigTypePolicy(admin, POLICY_MODIFY_FONT_SIZE_CONFIG, String.valueOf(((Float) FONT_SIZE_TYPES.get(Integer.valueOf(size))).floatValue()));
    }

    private boolean isValidFontSizeValue(int fontSize) {
        return FONT_SIZE_TYPES.keySet().contains(Integer.valueOf(fontSize));
    }

    public boolean setUnlockByFingerprintDisabled(ComponentName admin, boolean disable) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_UNLOCK_BY_FINGERPRINT, disable);
    }

    public boolean isUnlockByFingerprintDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_UNLOCK_BY_FINGERPRINT);
    }

    public boolean setMockLocationDisable(ComponentName admin, boolean disable) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_MOCK_LOCATION, disable);
    }

    public boolean isMockLocationDisable(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_MOCK_LOCATION);
    }

    public boolean setRestrictBackgroundProcessDisabled(ComponentName admin, boolean disable) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_RESTRICT_BACKGROUND_PROCESS, disable);
    }

    public boolean isRestrictBackgroundProcessDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_RESTRICT_BACKGROUND_PROCESS);
    }

    public boolean setDevelopmentOptionDisabled(ComponentName admin, boolean disable) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_DEVELOPMENT_OPTION, disable);
    }

    public boolean isDevelopmentOptionDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_DEVELOPMENT_OPTION);
    }

    public boolean setLocationServiceDisabled(ComponentName admin, boolean disable) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_LOCATION_SERVICE, disable);
    }

    public boolean isLocationServiceDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_LOCATION_SERVICE);
    }

    public boolean setLocationModeDisabled(ComponentName admin, boolean disable) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_LOCATION_MODE, disable);
    }

    public boolean isLocationModeDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_LOCATION_MODE);
    }

    public boolean setImmediatelyDestroyActivitiesDisabled(ComponentName admin, boolean disable) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_IMMEDIATEL_DESTROY_ACTIVITIES, disable);
    }

    public boolean isImmediatelyDestroyActivitiesDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_IMMEDIATEL_DESTROY_ACTIVITIES);
    }

    public boolean setHuaweiBeamDisabled(ComponentName admin, boolean disable) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_HUAWEI_BEAM, disable);
    }

    public boolean isHuaweiBeamDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_HUAWEI_BEAM);
    }

    public boolean setLockPasswordAllowed(ComponentName admin, boolean disable) {
        return setStateTypePolicy(admin, POLICY_LOCK_PASSWORD_ALLOWED, disable);
    }

    public boolean isLockPasswordAllowed(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_LOCK_PASSWORD_ALLOWED);
    }

    private ArrayList<String> getListTypePolicyStatus(ComponentName admin, String policyName) {
        return getListTypeDataFromBundle(this.mDpm.getPolicy(admin, policyName));
    }

    private ArrayList<String> getListTypeDataFromBundle(Bundle policyValue) {
        if (policyValue != null) {
            return policyValue.getStringArrayList("value");
        }
        return null;
    }

    private boolean removeListTypePolicy(ComponentName admin, String policyName, ArrayList<String> list) {
        return this.mDpm.removePolicy(admin, policyName, buildListTypeData(list));
    }

    private boolean setListTypePolicy(ComponentName admin, String policyName, ArrayList<String> list) {
        return this.mDpm.setPolicy(admin, policyName, buildListTypeData(list));
    }

    private Bundle buildListTypeData(ArrayList<String> list) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("list is null or empty");
        }
        Bundle bundle = new Bundle();
        bundle.putInt(MDM_POLICY_TYPE, 1);
        bundle.putStringArrayList("value", list);
        return bundle;
    }

    public boolean setAllowAccessibilityServicesList(ComponentName admin, ArrayList<String> pkgList) {
        return setListTypePolicy(admin, POLICY_ACCESSIBILITY_SERVICES_WHITE_LIST, pkgList);
    }

    public ArrayList<String> getAllowAccessibilityServicesList(ComponentName admin) {
        return getListTypePolicyStatus(admin, POLICY_ACCESSIBILITY_SERVICES_WHITE_LIST);
    }

    public boolean removeAllowAccessibilityServicesList(ComponentName admin, ArrayList<String> pkgList) {
        return removeListTypePolicy(admin, POLICY_ACCESSIBILITY_SERVICES_WHITE_LIST, pkgList);
    }

    public boolean setNotificationDisabled(ComponentName who, boolean disable) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", disable);
        Log.w(TAG, "set Notification Disabled: " + disable + ",by ComponentName is: " + (who == null ? "null" : who.flattenToString()));
        return this.mDpm.setPolicy(who, DISABLE_NOTIFICATION, bundle);
    }

    public boolean isNotificationDisabled(ComponentName who) {
        Bundle bundle = this.mDpm.getPolicy(who, DISABLE_NOTIFICATION);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean setNavigationBarDisabled(ComponentName who, boolean disable) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", disable);
        Log.w(TAG, "set Navigation Bar Disabled: " + disable + ",by ComponentName is: " + (who == null ? "null" : who.flattenToString()));
        return this.mDpm.setPolicy(who, DISABLE_NAVIGATIONBAR, bundle);
    }

    public boolean isNavigationBarDisabled(ComponentName who) {
        Bundle bundle = this.mDpm.getPolicy(who, DISABLE_NAVIGATIONBAR);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean setSearchIndexDisabled(ComponentName admin, boolean disabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", disabled);
        return this.mDpm.setPolicy(admin, POLICY_SEARCH_INDEX_DISABLED, bundle);
    }

    public boolean isSearchIndexDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, POLICY_SEARCH_INDEX_DISABLED);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("value");
    }
}
