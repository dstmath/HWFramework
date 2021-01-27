package com.huawei.android.app.admin;

import android.content.ComponentName;
import android.os.Bundle;
import android.util.Log;
import huawei.android.app.admin.HwDevicePolicyManagerEx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DeviceSettingsManager {
    private static final String CONFIG_NORMAL_VALUE = "NORMAL";
    private static final String CONFIG_NOT_SETUP_VALUE = "not_set_status";
    public static final String DISABLED_ANDROID_ANIMATION = "disabled-android-animation";
    private static final String DISABLE_NAVIGATIONBAR = "disable-navigationbar";
    private static final String DISABLE_NOTIFICATION = "disable-notification";
    private static final String EMPTY_STRING = "";
    private static final int FONT_MDM_POLICY_REMOVED = -1;
    private static final int FONT_SIZE_EXTRA_HUGE = 5;
    private static final float FONT_SIZE_EXTRA_HUGE_VALUE = 1.45f;
    private static final int FONT_SIZE_HUGE = 4;
    private static final float FONT_SIZE_HUGE_VALUE = 1.3f;
    private static final int FONT_SIZE_LARGE = 3;
    private static final float FONT_SIZE_LARGE_VALUE = 1.15f;
    private static final int FONT_SIZE_NORMAL = 2;
    private static final float FONT_SIZE_NORMAL_VALUE = 1.0f;
    private static final int FONT_SIZE_SMALL = 1;
    private static final float FONT_SIZE_SMALL_VALUE = 0.85f;
    private static final Map<Integer, Float> FONT_SIZE_TYPES = new HashMap();
    private static final String MDM_POLICY_NAME = "mdm_policy_name";
    private static final String MDM_POLICY_TYPE = "mdm_policy_type";
    private static final String MDM_SETTINGS_PERMISSION = "com.huawei.permission.sec.PERMISSION_MDM_DEVICE_SETTINGS_MANAGER";
    private static final String POLICY_ACCESSIBILITY_SERVICES_WHITE_LIST = "accessibility_services_white_list";
    public static final String POLICY_APPLICATION_LOCK = "policy_application_lock_disabled";
    private static final String POLICY_FORBIDDEN_ADD_USER = "settings_policy_forbidden_add_user";
    private static final String POLICY_FORBIDDEN_BLUETOOTH_NET_SHARE = "settings_policy_forbidden_bluetooth_net_share";
    private static final String POLICY_FORBIDDEN_DEVELOPMENT_OPTION = "settings_policy_forbidden_development_option";
    private static final String POLICY_FORBIDDEN_ECHO_PWD = "settings_policy_forbidden_echo_pwd";
    private static final String POLICY_FORBIDDEN_EDIT_APN = "settings_policy_forbidden_edit_apn";
    private static final String POLICY_FORBIDDEN_EDIT_WIFI = "settings_policy_forbidden_edit_wifi";
    private static final String POLICY_FORBIDDEN_GOOGLE_BACKUP = "settings_policy_forbidden_google_backup";
    private static final String POLICY_FORBIDDEN_HUAWEI_BEAM = "settings_policy_forbidden_huawei_beam";
    private static final String POLICY_FORBIDDEN_IMMEDIATEL_DESTROY_ACTIVITIES = "settings_policy_forbidden_immediatel_destroy";
    private static final String POLICY_FORBIDDEN_LOCATION_MODE = "settings_policy_forbidden_location_mode";
    private static final String POLICY_FORBIDDEN_LOCATION_SERVICE = "settings_policy_forbidden_location_service";
    private static final String POLICY_FORBIDDEN_MOCK_LOCATION = "settings_policy_forbidden_mock_location";
    private static final String POLICY_FORBIDDEN_MODIFY_TIME_TIMEZONE = "settings_policy_forbidden_modify_time_timezone";
    private static final String POLICY_FORBIDDEN_NETWORK_LOCATION = "settings_policy_forbidden_network_location";
    private static final String POLICY_FORBIDDEN_NETWORK_SHARE = "settings_policy_forbidden_network_share";
    private static final String POLICY_FORBIDDEN_RESTORE_DEVICE = "settings_policy_forbidden_restore_device";
    private static final String POLICY_FORBIDDEN_RESTRICT_BACKGROUND_PROCESS = "settings_policy_forbidden_restrict_background_process";
    private static final String POLICY_FORBIDDEN_SCREEN_OFF = "settings_policy_forbidden_screen_off";
    private static final String POLICY_FORBIDDEN_STOP_SYSTEM_SIGN_APP = "settings_policy_forbidden_stop_system_sign_app";
    private static final String POLICY_FORBIDDEN_UNKNOWN_APP_INSTALL = "settings_policy_forbidden_unknown_app_install";
    private static final String POLICY_FORBIDDEN_UNLOCK_BY_FINGERPRINT = "settings_policy_forbidden_unlock_by_fingerprint";
    private static final String POLICY_FORBIDDEN_USB_NET_SHARE = "settings_policy_forbidden_usb_net_share";
    private static final String POLICY_FORCE_ENCRYPT_SDCARD = "force-encrypt-sdcard";
    private static final String POLICY_LOCK_PASSWORD_ALLOWED = "settings_policy_lock_password_allowed";
    private static final String POLICY_MODIFY_FONT_SIZE_CONFIG = "settings_policy_modify_font_size_config";
    private static final String POLICY_MODIFY_WIFI_SLEEP_MODE_CONFIG = "settings_policy_modify_wifi_sleep_mode_config";
    public static final String POLICY_PARENT_CONTROL = "policy_parent_control_disabled";
    public static final String POLICY_PHONE_FIND = "policy_phone_find_disabled";
    private static final String POLICY_SEARCH_INDEX_DISABLED = "search_index_disabled";
    public static final String POLICY_SIM_LOCK = "policy_sim_lock_disabled";
    private static final String RECEIVER_ACTION_POLICY_ITEM_REMOVED = "com.android.settings.mdm.receiver.action.MDMPolicyItemRemoved";
    private static final String RECEIVER_ACTION_POLICY_MODIFIED = "com.android.settings.mdm.receiver.action.MDMPolicyModified";
    private static final String RECEIVER_ACTION_POLICY_TOTAL_REMOVED = "com.android.settings.mdm.receiver.action.MDMPolicyTotalRemoved";
    private static final String RECEIVER_ACTION_SETTINGS_ITEM_MODIFIED = "com.android.settings.mdm.receiver.action.MDMSettingsItemModified";
    private static final String SETTINGS_APK_NAME = "com.android.settings";
    private static final String SETTINGS_MDM_RECEIVER = "com.android.settings.mdm.HwMDMPolicyMonitorReceiver";
    private static final String STATE_VALUE = "value";
    private static final String TAG = DeviceSettingsManager.class.getSimpleName();
    private static final int TYPE_CFG = 2;
    private static final int TYPE_COMPOUND = 3;
    private static final int TYPE_INVALID = -1;
    private static final int TYPE_LIST = 1;
    private static final int TYPE_STATE = 0;
    private static final String WIFI_STANDBY_MODE_ALWAYS = "always";
    private static final Map<Integer, String> WIFI_STANDBY_MODE_MAP = new HashMap();
    private static final String WIFI_STANDBY_MODE_NEVER = "never";
    private static final String WIFI_STANDBY_MODE_NORMAL = "normal";
    private static final int WIFI_STANDBY_MODE_NORMAL_INT_VALUE = -1;
    private static final String WIFI_STANDBY_MODE_ON_CHARGE_ONLY = "on-charge-only";
    private HwDevicePolicyManagerEx mDpm = new HwDevicePolicyManagerEx();

    static {
        FONT_SIZE_TYPES.put(1, Float.valueOf((float) FONT_SIZE_SMALL_VALUE));
        FONT_SIZE_TYPES.put(2, Float.valueOf((float) FONT_SIZE_NORMAL_VALUE));
        FONT_SIZE_TYPES.put(3, Float.valueOf((float) FONT_SIZE_LARGE_VALUE));
        FONT_SIZE_TYPES.put(4, Float.valueOf((float) FONT_SIZE_HUGE_VALUE));
        FONT_SIZE_TYPES.put(5, Float.valueOf((float) FONT_SIZE_EXTRA_HUGE_VALUE));
        WIFI_STANDBY_MODE_MAP.put(2, WIFI_STANDBY_MODE_ALWAYS);
        WIFI_STANDBY_MODE_MAP.put(1, WIFI_STANDBY_MODE_ON_CHARGE_ONLY);
        WIFI_STANDBY_MODE_MAP.put(0, WIFI_STANDBY_MODE_NEVER);
        WIFI_STANDBY_MODE_MAP.put(-1, "normal");
    }

    public boolean setNetworkLocationDisabled(ComponentName admin, boolean isDisabled) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_NETWORK_LOCATION, isDisabled);
    }

    private boolean setStateTypePolicy(ComponentName admin, String policyName, boolean isActivePolicy) {
        return this.mDpm.setPolicy(admin, policyName, buildStateTypeData(isActivePolicy));
    }

    private Bundle buildStateTypeData(boolean isDisabled) {
        Bundle value = new Bundle();
        value.putInt(MDM_POLICY_TYPE, 0);
        value.putBoolean("value", isDisabled);
        return value;
    }

    public boolean isNetworkLocationDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_NETWORK_LOCATION);
    }

    private boolean getStateTypePolicyActiveStatus(ComponentName admin, String policyName) {
        String str = TAG;
        Log.i(str, "getStateTypePolicyActiveStatus() raw policyName: " + policyName);
        return isStatePolicyActive(getStateTypeDataFromBundle(this.mDpm.getPolicy(admin, policyName)));
    }

    private boolean getStateTypeDataFromBundle(Bundle policyValue) {
        return getStateTypeDataFromBundle(policyValue, false);
    }

    private boolean getStateTypeDataFromBundle(Bundle policyValue, boolean isDefaultActive) {
        if (policyValue != null) {
            return policyValue.getBoolean("value", isDefaultActive);
        }
        String str = TAG;
        Log.w(str, "getStateTypeDataFromBundle() get null data. will return default value: " + isDefaultActive);
        return isDefaultActive;
    }

    private boolean isStatePolicyActive(boolean isActive) {
        return isActive;
    }

    public boolean setUnknownSourceAppInstallDisabled(ComponentName admin, boolean isDisabled) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_UNKNOWN_APP_INSTALL, isDisabled);
    }

    public boolean isUnknownSourceAppInstallDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_UNKNOWN_APP_INSTALL);
    }

    public boolean setAllTetheringDisabled(ComponentName admin, boolean isDisabled) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_NETWORK_SHARE, isDisabled);
    }

    public boolean isAllTetheringDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_NETWORK_SHARE);
    }

    public boolean setWIFIeditDisabled(ComponentName admin, boolean isDisabled) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_EDIT_WIFI, isDisabled);
    }

    public boolean isWIFIeditDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_EDIT_WIFI);
    }

    public boolean setRestoreFactoryDisabled(ComponentName admin, boolean isDisabled) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_RESTORE_DEVICE, isDisabled);
    }

    public boolean isRestoreFactoryDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_RESTORE_DEVICE);
    }

    public boolean setWIFIStandbyMode(ComponentName admin, int config) {
        if (!isValidWifiStandbyMode(config)) {
            return false;
        }
        String policyFormatValue = getWifiStandbyModePolicyValue(config);
        if ("normal".equals(policyFormatValue)) {
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

    private static boolean isValidWifiStandbyMode(int config) {
        return WIFI_STANDBY_MODE_MAP.keySet().contains(Integer.valueOf(config));
    }

    private static String getWifiStandbyModePolicyValue(int policyStatus) {
        if (!isValidWifiStandbyMode(policyStatus)) {
            return "normal";
        }
        return WIFI_STANDBY_MODE_MAP.get(Integer.valueOf(policyStatus));
    }

    public int getWIFIStandbyMode(ComponentName admin) {
        return getWifiStandbyModeSystemValue(getConfigTypePolicyStatus(admin, POLICY_MODIFY_WIFI_SLEEP_MODE_CONFIG));
    }

    private String getConfigTypePolicyStatus(ComponentName admin, String policyName) {
        String str = TAG;
        Log.i(str, "getConfigTypePolicyStatus() admin:" + admin + " policyName: " + policyName);
        return getConfigTypeDataFromBundle(this.mDpm.getPolicy(admin, policyName), CONFIG_NOT_SETUP_VALUE);
    }

    private static int getWifiStandbyModeSystemValue(String policyStatus) {
        for (Map.Entry<Integer, String> entry : WIFI_STANDBY_MODE_MAP.entrySet()) {
            if (entry.getValue().equals(policyStatus)) {
                return entry.getKey().intValue();
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

    public boolean setBluetoothTetheringDisabled(ComponentName admin, boolean isDisabled) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_BLUETOOTH_NET_SHARE, isDisabled);
    }

    public boolean isBluetoothTetheringDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_BLUETOOTH_NET_SHARE);
    }

    public boolean setAddUserDisabled(ComponentName admin, boolean isDisabled) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_ADD_USER, isDisabled);
    }

    public boolean isAddUserDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_ADD_USER);
    }

    public boolean setEchoPasswordDisabled(ComponentName admin, boolean isDisabled) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_ECHO_PWD, isDisabled);
    }

    public boolean isEchoPasswordDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_ECHO_PWD);
    }

    public boolean setForceStopSystemSignatureAppDisabled(ComponentName admin, boolean isDisabled) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_STOP_SYSTEM_SIGN_APP, isDisabled);
    }

    public boolean isForceStopSystemSignatureAppDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_STOP_SYSTEM_SIGN_APP);
    }

    public boolean setUSBTetheringDisabled(ComponentName admin, boolean isDisabled) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_USB_NET_SHARE, isDisabled);
    }

    public boolean isUSBTetheringDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_USB_NET_SHARE);
    }

    public boolean setTimeAndDateSetDisabled(ComponentName admin, boolean isDisabled) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_MODIFY_TIME_TIMEZONE, isDisabled);
    }

    public boolean isTimeAndDateSetDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_MODIFY_TIME_TIMEZONE);
    }

    public boolean setGoogleBackupRestoreDisabled(ComponentName admin, boolean isDisabled) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_GOOGLE_BACKUP, isDisabled);
    }

    public boolean isGoogleBackupRestoreDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_GOOGLE_BACKUP);
    }

    public boolean setScreenOffDisabled(ComponentName admin, boolean isDisabled) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_SCREEN_OFF, isDisabled);
    }

    public boolean isScreenOffDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_SCREEN_OFF);
    }

    public boolean setFontSize(ComponentName admin, int size) {
        if (size == -1) {
            return removeConfigTypePolicy(admin, POLICY_MODIFY_FONT_SIZE_CONFIG);
        }
        if (!isValidFontSizeValue(size)) {
            return false;
        }
        return setConfigTypePolicy(admin, POLICY_MODIFY_FONT_SIZE_CONFIG, String.valueOf(FONT_SIZE_TYPES.get(Integer.valueOf(size)).floatValue()));
    }

    private boolean isValidFontSizeValue(int fontSize) {
        return FONT_SIZE_TYPES.keySet().contains(Integer.valueOf(fontSize));
    }

    public boolean setUnlockByFingerprintDisabled(ComponentName admin, boolean isDisabled) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_UNLOCK_BY_FINGERPRINT, isDisabled);
    }

    public boolean isUnlockByFingerprintDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_UNLOCK_BY_FINGERPRINT);
    }

    public boolean setMockLocationDisable(ComponentName admin, boolean isDisabled) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_MOCK_LOCATION, isDisabled);
    }

    public boolean isMockLocationDisable(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_MOCK_LOCATION);
    }

    public boolean setRestrictBackgroundProcessDisabled(ComponentName admin, boolean isDisabled) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_RESTRICT_BACKGROUND_PROCESS, isDisabled);
    }

    public boolean isRestrictBackgroundProcessDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_RESTRICT_BACKGROUND_PROCESS);
    }

    public boolean setDevelopmentOptionDisabled(ComponentName admin, boolean isDisabled) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_DEVELOPMENT_OPTION, isDisabled);
    }

    public boolean isDevelopmentOptionDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_DEVELOPMENT_OPTION);
    }

    public boolean setLocationServiceDisabled(ComponentName admin, boolean isDisabled) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_LOCATION_SERVICE, isDisabled);
    }

    public boolean isLocationServiceDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_LOCATION_SERVICE);
    }

    public boolean setLocationModeDisabled(ComponentName admin, boolean isDisabled) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_LOCATION_MODE, isDisabled);
    }

    public boolean isLocationModeDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_LOCATION_MODE);
    }

    public boolean setImmediatelyDestroyActivitiesDisabled(ComponentName admin, boolean isDisabled) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_IMMEDIATEL_DESTROY_ACTIVITIES, isDisabled);
    }

    public boolean isImmediatelyDestroyActivitiesDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_IMMEDIATEL_DESTROY_ACTIVITIES);
    }

    public boolean setHuaweiBeamDisabled(ComponentName admin, boolean isDisabled) {
        return setStateTypePolicy(admin, POLICY_FORBIDDEN_HUAWEI_BEAM, isDisabled);
    }

    public boolean isHuaweiBeamDisabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORBIDDEN_HUAWEI_BEAM);
    }

    public boolean setLockPasswordAllowed(ComponentName admin, boolean isDisabled) {
        return setStateTypePolicy(admin, POLICY_LOCK_PASSWORD_ALLOWED, isDisabled);
    }

    public boolean isLockPasswordAllowed(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_LOCK_PASSWORD_ALLOWED);
    }

    private ArrayList<String> getListTypePolicyStatus(ComponentName admin, String policyName) {
        return getListTypeDataFromBundle(this.mDpm.getPolicy(admin, policyName));
    }

    private ArrayList<String> getListTypeDataFromBundle(Bundle policyValue) {
        if (policyValue == null) {
            return null;
        }
        try {
            return policyValue.getStringArrayList("value");
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "getListTypeDataFromBundle exception.");
            return null;
        }
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

    public boolean setNotificationDisabled(ComponentName admin, boolean isDisabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isDisabled);
        String str = TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("set Notification Disabled: ");
        sb.append(isDisabled);
        sb.append(",by ComponentName is: ");
        sb.append(admin == null ? "null" : admin.flattenToString());
        Log.w(str, sb.toString());
        return this.mDpm.setPolicy(admin, DISABLE_NOTIFICATION, bundle);
    }

    public boolean isNotificationDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_NOTIFICATION);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean setNavigationBarDisabled(ComponentName admin, boolean isDisabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isDisabled);
        String str = TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("set Navigation Bar Disabled: ");
        sb.append(isDisabled);
        sb.append(",by ComponentName is: ");
        sb.append(admin == null ? "null" : admin.flattenToString());
        Log.w(str, sb.toString());
        return this.mDpm.setPolicy(admin, DISABLE_NAVIGATIONBAR, bundle);
    }

    public boolean isNavigationBarDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_NAVIGATIONBAR);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean setSearchIndexDisabled(ComponentName admin, boolean isDisabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isDisabled);
        return this.mDpm.setPolicy(admin, POLICY_SEARCH_INDEX_DISABLED, bundle);
    }

    public boolean isSearchIndexDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, POLICY_SEARCH_INDEX_DISABLED);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("value");
    }

    public boolean isPhoneFindDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, POLICY_PHONE_FIND);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("value");
    }

    public boolean setPhoneFindDisabled(ComponentName admin, boolean isDisabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isDisabled);
        return this.mDpm.setPolicy(admin, POLICY_PHONE_FIND, bundle);
    }

    public boolean isParentControlDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, POLICY_PARENT_CONTROL);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("value");
    }

    public boolean setParentControlDisabled(ComponentName admin, boolean isDisabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isDisabled);
        return this.mDpm.setPolicy(admin, POLICY_PARENT_CONTROL, bundle);
    }

    public boolean isSIMLockDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, POLICY_SIM_LOCK);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("value");
    }

    public boolean setSIMLockDisabled(ComponentName admin, boolean isDisabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isDisabled);
        return this.mDpm.setPolicy(admin, POLICY_SIM_LOCK, bundle);
    }

    public boolean isApplicationLockDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, POLICY_APPLICATION_LOCK);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("value");
    }

    public boolean setApplicationLockDisabled(ComponentName admin, boolean isDisabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isDisabled);
        return this.mDpm.setPolicy(admin, POLICY_APPLICATION_LOCK, bundle);
    }

    public boolean setAndroidAnimationDisabled(ComponentName admin, boolean isDisabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isDisabled);
        return this.mDpm.setPolicy(admin, DISABLED_ANDROID_ANIMATION, bundle);
    }

    public boolean isAndroidAnimationDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLED_ANDROID_ANIMATION);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean setForceEncryptSdcardEnabled(ComponentName admin, boolean isEnabled) {
        return setStateTypePolicy(admin, POLICY_FORCE_ENCRYPT_SDCARD, isEnabled);
    }

    public boolean isForceEncryptSdcardEnabled(ComponentName admin) {
        return getStateTypePolicyActiveStatus(admin, POLICY_FORCE_ENCRYPT_SDCARD);
    }
}
