package com.android.server.devicepolicy;

import android.os.Bundle;
import com.android.server.devicepolicy.plugins.SettingsMDMPlugin;
import java.util.ArrayList;
import java.util.List;

public class HwAdminCache {
    public static final int DISABLED_DEACTIVE_MDM_PACKAGES = 18;
    public static final int DISABLE_ADB = 11;
    public static final int DISABLE_APPLICATIONS_LIST = 28;
    public static final String DISABLE_APPLICATIONS_LIST_POLICY = "disable-applications-list";
    public static final int DISABLE_BACK = 16;
    public static final int DISABLE_BLUETOOTH = 8;
    public static final int DISABLE_CHANGE_LAUNCHER = 17;
    public static final int DISABLE_CLIPBOARD = 25;
    public static final int DISABLE_DECRYPT_SDCARD = 19;
    public static final int DISABLE_GOOGLE_ACCOUNT_AUTOSYNC = 26;
    public static final int DISABLE_GOOGLE_PLAY_STORE = 24;
    public static final String DISABLE_GOOGLE_PLAY_STORE_POLICY = "disable-google-play-store";
    public static final int DISABLE_GPS = 13;
    public static final String DISABLE_HEADPHONE = "disable-headphone";
    public static final int DISABLE_HOME = 14;
    public static final int DISABLE_INFRARED = 32;
    public static final String DISABLE_INFRARED_POLICY = "infrared_item_policy_name";
    public static final int DISABLE_INSTALLSOURCE = 2;
    public static final String DISABLE_MICROPHONE = "disable-microphone";
    public static final String DISABLE_NAVIGATIONBAR_POLICY = "disable-navigationbar";
    public static final String DISABLE_NOTIFICATION_POLICY = "disable-notification";
    public static final String DISABLE_PASSIVE_PROVIDER_POLICY = "passive_location_disallow_item";
    public static final int DISABLE_SAFEMODE = 10;
    public static final int DISABLE_SCREEN_CAPTURE = 21;
    public static final String DISABLE_SCREEN_CAPTURE_POLICY = "disable-screen-capture";
    public static final String DISABLE_SDWRITING_POLICY = "disable-sdwriting";
    public static final int DISABLE_SETTINGS = 23;
    public static final String DISABLE_SETTINGS_POLICY = "disable-settings";
    public static final String DISABLE_SYNC = "disable-sync";
    public static final int DISABLE_SYSTEM_BROWSER = 22;
    public static final String DISABLE_SYSTEM_BROWSER_POLICY = "disable-system-browser";
    public static final int DISABLE_TASK = 15;
    public static final int DISABLE_USBOTG = 12;
    public static final int DISABLE_VOICE = 1;
    public static final int DISABLE_WIFI = 0;
    public static final int DISABLE_WIFIP2P = 29;
    public static final String DISABLE_WIFIP2P_POLICY = "wifi_p2p_item_policy_name";
    public static final int DISALLOWEDRUNNING_APP_LIST = 5;
    public static final int DISALLOWEDUNINSTALL_PACKAGE_LIST = 7;
    public static final int IGNORE_FREQUENT_RELAUNCH_APP_LIST = 27;
    public static final int INSTALLPACKAGE_WHITELIST = 6;
    public static final int INSTALLSOURCE_WHITELIST = 3;
    public static final int INSTALL_APKS_BLACK_LIST = 20;
    public static final String INSTALL_APKS_BLACK_LIST_POLICY = "install-packages-black-list";
    public static final int NETWORK_ACCESS_WHITELIST = 9;
    public static final int PERSISTENTAPP_LIST = 4;
    public static final String SUPER_WHITE_LIST_APP = "super-whitelist-hwsystemmanager";
    private Bundle allowAccessibilityServices = null;
    private boolean disableAdb = false;
    private boolean disableBack = false;
    private boolean disableBluetooth = false;
    private boolean disableChangeLauncher = false;
    private Bundle disableChangeWallpaper = null;
    private boolean disableClipboard = false;
    private boolean disableDecryptSDCard = false;
    private Bundle disableFingerprintAuthentication = null;
    private boolean disableGPS = false;
    private boolean disableGoogleAccountAutosync = false;
    private boolean disableGooglePlayStore = false;
    private Bundle disableHeadphone = null;
    private boolean disableHome = false;
    private boolean disableInfrared = false;
    private boolean disableInstallSource = false;
    private Bundle disableMicrophone = null;
    private Bundle disablePassiveProvider = null;
    private Bundle disablePowerShutdown = null;
    private boolean disableSafeMode = false;
    private boolean disableScreenCapture = false;
    private Bundle disableScreenOff = null;
    private Bundle disableSendNotification = null;
    private boolean disableSettings = false;
    private Bundle disableShutdownMenu = null;
    private Bundle disableSync = null;
    private boolean disableSystemBrowser = false;
    private boolean disableTask = false;
    private boolean disableUSBOtg = false;
    private boolean disableVoice = false;
    private Bundle disableVolume = null;
    private boolean disableWifi = false;
    private boolean disableWifiP2P = false;
    private ArrayList<String> disabledApplicationlist = null;
    private List<String> disabledDeactiveMdmPackagesList = null;
    private List<String> disallowedRunningAppList = null;
    private List<String> disallowedUninstallPackageList = null;
    private Bundle exampleValue = null;
    private Bundle forceEnableBT = null;
    private Bundle forceEnableWifi = null;
    private List<String> ignoreFrequentRelaunchAppList = null;
    private ArrayList<String> installPackageBlacklist = null;
    private List<String> installPackageWhitelist = null;
    private List<String> installSourceWhitelist = null;
    private Bundle mDisableLocationMode = null;
    private Bundle mDisableLocationService = null;
    private Bundle mDisableNotificationBundle = null;
    private Bundle mDisableSDCardWritingBundle = null;
    private Bundle mDisablenNavigationBarBundle = null;
    private Object mLock = new Object();
    private Bundle mSettingsPolicyNetworkLocationStatus;
    private List<String> networkAccessWhitelist = null;
    private List<String> persistentAppList = null;
    private Bundle singleApp = null;
    private Bundle superWhiteListApp = null;

    public void syncHwAdminCache(int type, boolean value) {
        synchronized (this.mLock) {
            switch (type) {
                case 0:
                    this.disableWifi = value;
                    break;
                case 1:
                    this.disableVoice = value;
                    break;
                case 2:
                    this.disableInstallSource = value;
                    break;
                case 8:
                    this.disableBluetooth = value;
                    break;
                case 10:
                    this.disableSafeMode = value;
                    break;
                case 11:
                    this.disableAdb = value;
                    break;
                case 12:
                    this.disableUSBOtg = value;
                    break;
                case 13:
                    this.disableGPS = value;
                    break;
                case 14:
                    this.disableHome = value;
                    break;
                case 15:
                    this.disableTask = value;
                    break;
                case 16:
                    this.disableBack = value;
                    break;
                case 17:
                    this.disableChangeLauncher = value;
                    break;
                case 19:
                    this.disableDecryptSDCard = value;
                    break;
            }
        }
    }

    public void syncHwAdminCache(int type, List<String> list) {
        synchronized (this.mLock) {
            switch (type) {
                case 3:
                    this.installSourceWhitelist = list;
                    break;
                case 4:
                    this.persistentAppList = list;
                    break;
                case 5:
                    this.disallowedRunningAppList = list;
                    break;
                case 6:
                    this.installPackageWhitelist = list;
                    break;
                case 7:
                    this.disallowedUninstallPackageList = list;
                    break;
                case 9:
                    this.networkAccessWhitelist = list;
                    break;
                case 18:
                    this.disabledDeactiveMdmPackagesList = list;
                    break;
            }
        }
    }

    public void syncHwAdminCache(String policyName, Bundle bundle) {
        synchronized (this.mLock) {
            if (policyName.equals("xxxxx")) {
                this.exampleValue = bundle;
            } else if (policyName.equals(SettingsMDMPlugin.POLICY_FORBIDDEN_NETWORK_LOCATION)) {
                this.mSettingsPolicyNetworkLocationStatus = bundle;
            } else if (policyName.equals(INSTALL_APKS_BLACK_LIST_POLICY)) {
                this.installPackageBlacklist = bundle.getStringArrayList("value");
            } else if (policyName.equals(DISABLE_SCREEN_CAPTURE_POLICY)) {
                this.disableScreenCapture = bundle.getBoolean("value");
            } else if (policyName.equals(DISABLE_SYSTEM_BROWSER_POLICY)) {
                this.disableSystemBrowser = bundle.getBoolean("value");
            } else if (policyName.equals(DISABLE_SETTINGS_POLICY)) {
                this.disableSettings = bundle.getBoolean("value");
            } else if (policyName.equals(DISABLE_GOOGLE_PLAY_STORE_POLICY)) {
                this.disableGooglePlayStore = bundle.getBoolean("value");
            } else if (policyName.equals(DISABLE_APPLICATIONS_LIST_POLICY)) {
                this.disabledApplicationlist = bundle.getStringArrayList("value");
            } else if (policyName.equals(DISABLE_SDWRITING_POLICY)) {
                this.mDisableSDCardWritingBundle = bundle;
            } else if (policyName.equals(DISABLE_NOTIFICATION_POLICY)) {
                this.mDisableNotificationBundle = bundle;
            } else if (policyName.equals("disable-clipboard")) {
                this.disableClipboard = bundle.getBoolean("value", false);
            } else if (policyName.equals("disable-google-account-autosync")) {
                this.disableGoogleAccountAutosync = bundle.getBoolean("value", false);
            } else if (policyName.equals("ignore-frequent-relaunch-app")) {
                this.ignoreFrequentRelaunchAppList = bundle.getStringArrayList("value");
            } else if (policyName.equals(DISABLE_MICROPHONE)) {
                this.disableMicrophone = bundle;
            } else if (policyName.equals(SUPER_WHITE_LIST_APP)) {
                this.superWhiteListApp = bundle;
            } else if (policyName.equals(DISABLE_HEADPHONE)) {
                this.disableHeadphone = bundle;
            } else if (policyName.equals("disable-send-notification")) {
                this.disableSendNotification = bundle;
            } else if (policyName.equals("policy-single-app")) {
                this.singleApp = bundle;
            } else if (policyName.equals("disable-change-wallpaper")) {
                this.disableChangeWallpaper = bundle;
            } else if (policyName.equals(SettingsMDMPlugin.POLICY_FORBIDDEN_SCREEN_OFF)) {
                this.disableScreenOff = bundle;
            } else if (policyName.equals("disable-power-shutdown")) {
                this.disablePowerShutdown = bundle;
            } else if (policyName.equals("disable-shutdownmenu")) {
                this.disableShutdownMenu = bundle;
            } else if (policyName.equals("disable-volume")) {
                this.disableVolume = bundle;
            } else if (policyName.equals(SettingsMDMPlugin.POLICY_FORBIDDEN_LOCATION_SERVICE)) {
                this.mDisableLocationService = bundle;
            } else if (policyName.equals(SettingsMDMPlugin.POLICY_FORBIDDEN_LOCATION_MODE)) {
                this.mDisableLocationMode = bundle;
            } else if (policyName.equals(DISABLE_SYNC)) {
                this.disableSync = bundle;
            } else if (policyName.equals(DISABLE_PASSIVE_PROVIDER_POLICY)) {
                this.disablePassiveProvider = bundle;
            } else if (policyName.equals(DISABLE_WIFIP2P_POLICY)) {
                this.disableWifiP2P = bundle.getBoolean("wifi_p2p_policy_item_value");
            } else if (policyName.equals(DISABLE_INFRARED_POLICY)) {
                this.disableInfrared = bundle.getBoolean("infrared_item_policy_value");
            } else if (policyName.equals("disable-fingerprint-authentication")) {
                this.disableFingerprintAuthentication = bundle;
            } else if (policyName.equals("force-enable-BT")) {
                this.forceEnableBT = bundle;
            } else if (policyName.equals("force-enable-wifi")) {
                this.forceEnableWifi = bundle;
            } else if (policyName.equals(DISABLE_NAVIGATIONBAR_POLICY)) {
                this.mDisablenNavigationBarBundle = bundle;
            } else if (policyName.equals(SettingsMDMPlugin.POLICY_ACCESSIBILITY_SERVICES_WHITE_LIST)) {
                this.allowAccessibilityServices = bundle;
            }
        }
    }

    public Bundle getCachedBundle(String policyName) {
        Bundle result = null;
        synchronized (this.mLock) {
            if (policyName.equals(SettingsMDMPlugin.POLICY_FORBIDDEN_NETWORK_LOCATION)) {
                result = this.mSettingsPolicyNetworkLocationStatus;
            } else if (policyName.equals("xxxx")) {
                result = this.exampleValue;
            } else if (policyName.equals(DISABLE_MICROPHONE)) {
                result = this.disableMicrophone;
            } else if (policyName.equals(DISABLE_SDWRITING_POLICY)) {
                result = this.mDisableSDCardWritingBundle;
            } else if (policyName.equals(DISABLE_NOTIFICATION_POLICY)) {
                result = this.mDisableNotificationBundle;
            } else if (policyName.equals(SUPER_WHITE_LIST_APP)) {
                result = this.superWhiteListApp;
            } else if (policyName.equals(DISABLE_HEADPHONE)) {
                result = this.disableHeadphone;
            } else if (policyName.equals("disable-send-notification")) {
                result = this.disableSendNotification;
            } else if (policyName.equals("policy-single-app")) {
                result = this.singleApp;
            } else if (policyName.equals("disable-change-wallpaper")) {
                result = this.disableChangeWallpaper;
            } else if (policyName.equals(SettingsMDMPlugin.POLICY_FORBIDDEN_SCREEN_OFF)) {
                result = this.disableScreenOff;
            } else if (policyName.equals("disable-power-shutdown")) {
                result = this.disablePowerShutdown;
            } else if (policyName.equals("disable-shutdownmenu")) {
                result = this.disableShutdownMenu;
            } else if (policyName.equals("disable-volume")) {
                result = this.disableVolume;
            } else if (policyName.equals(SettingsMDMPlugin.POLICY_FORBIDDEN_LOCATION_SERVICE)) {
                result = this.mDisableLocationService;
            } else if (policyName.equals(SettingsMDMPlugin.POLICY_FORBIDDEN_LOCATION_MODE)) {
                result = this.mDisableLocationMode;
            } else if (policyName.equals(DISABLE_SYNC)) {
                result = this.disableSync;
            } else if (policyName.equals(DISABLE_PASSIVE_PROVIDER_POLICY)) {
                result = this.disablePassiveProvider;
            } else if (policyName.equals("disable-fingerprint-authentication")) {
                result = this.disableFingerprintAuthentication;
            } else if (policyName.equals("force-enable-BT")) {
                result = this.forceEnableBT;
            } else if (policyName.equals("force-enable-wifi")) {
                result = this.forceEnableWifi;
            } else if (policyName.equals(DISABLE_NAVIGATIONBAR_POLICY)) {
                result = this.mDisablenNavigationBarBundle;
            } else if (policyName.equals(SettingsMDMPlugin.POLICY_ACCESSIBILITY_SERVICES_WHITE_LIST)) {
                result = this.allowAccessibilityServices;
            }
        }
        return result;
    }

    public boolean getCachedValue(int type) {
        boolean result = false;
        synchronized (this.mLock) {
            switch (type) {
                case 0:
                    result = this.disableWifi;
                    break;
                case 1:
                    result = this.disableVoice;
                    break;
                case 2:
                    result = this.disableInstallSource;
                    break;
                case 8:
                    result = this.disableBluetooth;
                    break;
                case 10:
                    result = this.disableSafeMode;
                    break;
                case 11:
                    result = this.disableAdb;
                    break;
                case 12:
                    result = this.disableUSBOtg;
                    break;
                case 13:
                    result = this.disableGPS;
                    break;
                case 14:
                    result = this.disableHome;
                    break;
                case 15:
                    result = this.disableTask;
                    break;
                case 16:
                    result = this.disableBack;
                    break;
                case 17:
                    result = this.disableChangeLauncher;
                    break;
                case 19:
                    result = this.disableDecryptSDCard;
                    break;
                case 21:
                    result = this.disableScreenCapture;
                    break;
                case 22:
                    result = this.disableSystemBrowser;
                    break;
                case 23:
                    result = this.disableSettings;
                    break;
                case 24:
                    result = this.disableGooglePlayStore;
                    break;
                case 25:
                    result = this.disableClipboard;
                    break;
                case 26:
                    result = this.disableGoogleAccountAutosync;
                    break;
                case 29:
                    result = this.disableWifiP2P;
                    break;
                case 32:
                    result = this.disableInfrared;
                    break;
            }
        }
        return result;
    }

    public List<String> getCachedList(int type) {
        List<String> result = null;
        synchronized (this.mLock) {
            switch (type) {
                case 3:
                    result = this.installSourceWhitelist;
                    break;
                case 4:
                    result = this.persistentAppList;
                    break;
                case 5:
                    result = this.disallowedRunningAppList;
                    break;
                case 6:
                    result = this.installPackageWhitelist;
                    break;
                case 7:
                    result = this.disallowedUninstallPackageList;
                    break;
                case 9:
                    result = this.networkAccessWhitelist;
                    break;
                case 18:
                    result = this.disabledDeactiveMdmPackagesList;
                    break;
                case 20:
                    result = this.installPackageBlacklist;
                    break;
                case 27:
                    result = this.ignoreFrequentRelaunchAppList;
                    break;
                case 28:
                    result = this.disabledApplicationlist;
                    break;
            }
        }
        return result;
    }
}
