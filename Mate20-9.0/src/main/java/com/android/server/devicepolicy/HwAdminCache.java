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
    private Bundle disabledAndroidAnimation = null;
    private ArrayList<String> disabledApplicationlist = null;
    private List<String> disabledDeactiveMdmPackagesList = null;
    private List<String> disallowedRunningAppList = null;
    private List<String> disallowedUninstallPackageList = null;
    private Bundle exampleValue = null;
    private Bundle forceEnableBT = null;
    private Bundle forceEnableWifi = null;
    private Bundle forceEnablefileShare = null;
    private List<String> ignoreFrequentRelaunchAppList = null;
    private ArrayList<String> installPackageBlacklist = null;
    private List<String> installPackageWhitelist = null;
    private List<String> installSourceWhitelist = null;
    private Bundle mDisableLocationMode = null;
    private Bundle mDisableLocationService = null;
    private Bundle mDisableNotificationBundle = null;
    private Bundle mDisableSDCardWritingBundle = null;
    private Bundle mDisabledApplicationLock = null;
    private Bundle mDisabledParentControl = null;
    private Bundle mDisabledPhoneFind = null;
    private Bundle mDisabledSimLock = null;
    private Bundle mDisablenNavigationBarBundle = null;
    private Object mLock = new Object();
    private Bundle mSettingsPolicyForceEncryptSdcard;
    private Bundle mSettingsPolicyNetworkLocationStatus;
    private List<String> networkAccessWhitelist = null;
    private List<String> persistentAppList = null;
    private Bundle singleApp = null;
    private Bundle superWhiteListApp = null;

    public void syncHwAdminCache(int type, boolean value) {
        synchronized (this.mLock) {
            if (type == 8) {
                this.disableBluetooth = value;
            } else if (type != 19) {
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
                    default:
                        switch (type) {
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
                        }
                }
            } else {
                this.disableDecryptSDCard = value;
            }
        }
    }

    public void syncHwAdminCache(int type, List<String> list) {
        synchronized (this.mLock) {
            if (type == 9) {
                this.networkAccessWhitelist = list;
            } else if (type != 18) {
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
                }
            } else {
                this.disabledDeactiveMdmPackagesList = list;
            }
        }
    }

    public void syncHwAdminCache(String policyName, Bundle bundle) {
        synchronized (this.mLock) {
            char c = 65535;
            switch (policyName.hashCode()) {
                case -2032892315:
                    if (policyName.equals(DISABLE_PASSIVE_PROVIDER_POLICY)) {
                        c = 26;
                        break;
                    }
                    break;
                case -1809352329:
                    if (policyName.equals(DISABLE_GOOGLE_PLAY_STORE_POLICY)) {
                        c = 6;
                        break;
                    }
                    break;
                case -1586399269:
                    if (policyName.equals(SettingsMDMPlugin.POLICY_SIM_LOCK)) {
                        c = '%';
                        break;
                    }
                    break;
                case -1462770845:
                    if (policyName.equals(DISABLE_APPLICATIONS_LIST_POLICY)) {
                        c = 7;
                        break;
                    }
                    break;
                case -1418767509:
                    if (policyName.equals("disable-send-notification")) {
                        c = 16;
                        break;
                    }
                    break;
                case -1333051633:
                    if (policyName.equals(DISABLE_SYSTEM_BROWSER_POLICY)) {
                        c = 4;
                        break;
                    }
                    break;
                case -1032082848:
                    if (policyName.equals(DISABLE_SYNC)) {
                        c = 25;
                        break;
                    }
                    break;
                case -1002053434:
                    if (policyName.equals(DISABLE_SDWRITING_POLICY)) {
                        c = 8;
                        break;
                    }
                    break;
                case -851772941:
                    if (policyName.equals(SettingsMDMPlugin.POLICY_FORBIDDEN_SCREEN_OFF)) {
                        c = 19;
                        break;
                    }
                    break;
                case -694001423:
                    if (policyName.equals("disable-clipboard")) {
                        c = 10;
                        break;
                    }
                    break;
                case -595558097:
                    if (policyName.equals(DISABLE_MICROPHONE)) {
                        c = 13;
                        break;
                    }
                    break;
                case -414055785:
                    if (policyName.equals("policy-single-app")) {
                        c = 17;
                        break;
                    }
                    break;
                case -336519577:
                    if (policyName.equals(DISABLE_WIFIP2P_POLICY)) {
                        c = 27;
                        break;
                    }
                    break;
                case -304109734:
                    if (policyName.equals(DISABLE_NAVIGATIONBAR_POLICY)) {
                        c = '!';
                        break;
                    }
                    break;
                case -144764070:
                    if (policyName.equals(INSTALL_APKS_BLACK_LIST_POLICY)) {
                        c = 2;
                        break;
                    }
                    break;
                case 43563530:
                    if (policyName.equals(DISABLE_INFRARED_POLICY)) {
                        c = 28;
                        break;
                    }
                    break;
                case 114516600:
                    if (policyName.equals("xxxxx")) {
                        c = 0;
                        break;
                    }
                    break;
                case 153563136:
                    if (policyName.equals("policy-file-share-disabled")) {
                        c = '\"';
                        break;
                    }
                    break;
                case 382441887:
                    if (policyName.equals("disable-volume")) {
                        c = 22;
                        break;
                    }
                    break;
                case 458488698:
                    if (policyName.equals("disable-shutdownmenu")) {
                        c = 21;
                        break;
                    }
                    break;
                case 476421226:
                    if (policyName.equals(DISABLE_SCREEN_CAPTURE_POLICY)) {
                        c = 3;
                        break;
                    }
                    break;
                case 520557972:
                    if (policyName.equals(SettingsMDMPlugin.POLICY_APPLICATION_LOCK)) {
                        c = '&';
                        break;
                    }
                    break;
                case 539407267:
                    if (policyName.equals("disable-power-shutdown")) {
                        c = 20;
                        break;
                    }
                    break;
                case 591717814:
                    if (policyName.equals(SettingsMDMPlugin.POLICY_FORBIDDEN_LOCATION_MODE)) {
                        c = 24;
                        break;
                    }
                    break;
                case 594183088:
                    if (policyName.equals(DISABLE_NOTIFICATION_POLICY)) {
                        c = 9;
                        break;
                    }
                    break;
                case 702979817:
                    if (policyName.equals(DISABLE_HEADPHONE)) {
                        c = 15;
                        break;
                    }
                    break;
                case 731752599:
                    if (policyName.equals(SUPER_WHITE_LIST_APP)) {
                        c = 14;
                        break;
                    }
                    break;
                case 731920490:
                    if (policyName.equals("disable-change-wallpaper")) {
                        c = 18;
                        break;
                    }
                    break;
                case 853982814:
                    if (policyName.equals("ignore-frequent-relaunch-app")) {
                        c = 12;
                        break;
                    }
                    break;
                case 1044365373:
                    if (policyName.equals(SettingsMDMPlugin.POLICY_FORBIDDEN_NETWORK_LOCATION)) {
                        c = 1;
                        break;
                    }
                    break;
                case 1187313158:
                    if (policyName.equals(SettingsMDMPlugin.POLICY_PARENT_CONTROL)) {
                        c = '$';
                        break;
                    }
                    break;
                case 1389850009:
                    if (policyName.equals("disable-google-account-autosync")) {
                        c = 11;
                        break;
                    }
                    break;
                case 1463869800:
                    if (policyName.equals(DISABLE_SETTINGS_POLICY)) {
                        c = 5;
                        break;
                    }
                    break;
                case 1502491755:
                    if (policyName.equals(SettingsMDMPlugin.POLICY_FORCE_ENCRYPT_SDCARD)) {
                        c = '(';
                        break;
                    }
                    break;
                case 1695181060:
                    if (policyName.equals(SettingsMDMPlugin.POLICY_ACCESSIBILITY_SERVICES_WHITE_LIST)) {
                        c = ' ';
                        break;
                    }
                    break;
                case 1785346365:
                    if (policyName.equals("force-enable-wifi")) {
                        c = 31;
                        break;
                    }
                    break;
                case 1946452102:
                    if (policyName.equals("disable-fingerprint-authentication")) {
                        c = 29;
                        break;
                    }
                    break;
                case 1947338901:
                    if (policyName.equals(SettingsMDMPlugin.DISABLED_ANDROID_ANIMATION)) {
                        c = '\'';
                        break;
                    }
                    break;
                case 1981742202:
                    if (policyName.equals("force-enable-BT")) {
                        c = 30;
                        break;
                    }
                    break;
                case 2074833572:
                    if (policyName.equals(SettingsMDMPlugin.POLICY_PHONE_FIND)) {
                        c = '#';
                        break;
                    }
                    break;
                case 2076917186:
                    if (policyName.equals(SettingsMDMPlugin.POLICY_FORBIDDEN_LOCATION_SERVICE)) {
                        c = 23;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    this.exampleValue = bundle;
                    break;
                case 1:
                    this.mSettingsPolicyNetworkLocationStatus = bundle;
                    break;
                case 2:
                    this.installPackageBlacklist = bundle.getStringArrayList("value");
                    break;
                case 3:
                    this.disableScreenCapture = bundle.getBoolean("value");
                    break;
                case 4:
                    this.disableSystemBrowser = bundle.getBoolean("value");
                    break;
                case 5:
                    this.disableSettings = bundle.getBoolean("value");
                    break;
                case 6:
                    this.disableGooglePlayStore = bundle.getBoolean("value");
                    break;
                case 7:
                    this.disabledApplicationlist = bundle.getStringArrayList("value");
                    break;
                case 8:
                    this.mDisableSDCardWritingBundle = bundle;
                    break;
                case 9:
                    this.mDisableNotificationBundle = bundle;
                    break;
                case 10:
                    this.disableClipboard = bundle.getBoolean("value", false);
                    break;
                case 11:
                    this.disableGoogleAccountAutosync = bundle.getBoolean("value", false);
                    break;
                case 12:
                    this.ignoreFrequentRelaunchAppList = bundle.getStringArrayList("value");
                    break;
                case 13:
                    this.disableMicrophone = bundle;
                    break;
                case 14:
                    this.superWhiteListApp = bundle;
                    break;
                case 15:
                    this.disableHeadphone = bundle;
                    break;
                case 16:
                    this.disableSendNotification = bundle;
                    break;
                case 17:
                    this.singleApp = bundle;
                    break;
                case 18:
                    this.disableChangeWallpaper = bundle;
                    break;
                case 19:
                    this.disableScreenOff = bundle;
                    break;
                case 20:
                    this.disablePowerShutdown = bundle;
                    break;
                case 21:
                    this.disableShutdownMenu = bundle;
                    break;
                case 22:
                    this.disableVolume = bundle;
                    break;
                case 23:
                    this.mDisableLocationService = bundle;
                    break;
                case 24:
                    this.mDisableLocationMode = bundle;
                    break;
                case 25:
                    this.disableSync = bundle;
                    break;
                case 26:
                    this.disablePassiveProvider = bundle;
                    break;
                case 27:
                    this.disableWifiP2P = bundle.getBoolean("wifi_p2p_policy_item_value");
                    break;
                case 28:
                    this.disableInfrared = bundle.getBoolean("infrared_item_policy_value");
                    break;
                case 29:
                    this.disableFingerprintAuthentication = bundle;
                    break;
                case 30:
                    this.forceEnableBT = bundle;
                    break;
                case 31:
                    this.forceEnableWifi = bundle;
                    break;
                case ' ':
                    this.allowAccessibilityServices = bundle;
                    break;
                case '!':
                    this.mDisablenNavigationBarBundle = bundle;
                    break;
                case '\"':
                    this.forceEnablefileShare = bundle;
                    break;
                case '#':
                    this.mDisabledPhoneFind = bundle;
                    break;
                case '$':
                    this.mDisabledParentControl = bundle;
                    break;
                case '%':
                    this.mDisabledSimLock = bundle;
                    break;
                case '&':
                    this.mDisabledApplicationLock = bundle;
                    break;
                case '\'':
                    this.disabledAndroidAnimation = bundle;
                    break;
                case '(':
                    this.mSettingsPolicyForceEncryptSdcard = bundle;
                    break;
            }
        }
    }

    public Bundle getCachedBundle(String policyName) {
        Bundle result = null;
        synchronized (this.mLock) {
            char c = 65535;
            switch (policyName.hashCode()) {
                case -2032892315:
                    if (policyName.equals(DISABLE_PASSIVE_PROVIDER_POLICY)) {
                        c = 17;
                        break;
                    }
                    break;
                case -1586399269:
                    if (policyName.equals(SettingsMDMPlugin.POLICY_SIM_LOCK)) {
                        c = 26;
                        break;
                    }
                    break;
                case -1418767509:
                    if (policyName.equals("disable-send-notification")) {
                        c = 7;
                        break;
                    }
                    break;
                case -1032082848:
                    if (policyName.equals(DISABLE_SYNC)) {
                        c = 16;
                        break;
                    }
                    break;
                case -1002053434:
                    if (policyName.equals(DISABLE_SDWRITING_POLICY)) {
                        c = 3;
                        break;
                    }
                    break;
                case -851772941:
                    if (policyName.equals(SettingsMDMPlugin.POLICY_FORBIDDEN_SCREEN_OFF)) {
                        c = 10;
                        break;
                    }
                    break;
                case -595558097:
                    if (policyName.equals(DISABLE_MICROPHONE)) {
                        c = 2;
                        break;
                    }
                    break;
                case -414055785:
                    if (policyName.equals("policy-single-app")) {
                        c = 8;
                        break;
                    }
                    break;
                case -304109734:
                    if (policyName.equals(DISABLE_NAVIGATIONBAR_POLICY)) {
                        c = 22;
                        break;
                    }
                    break;
                case 3694080:
                    if (policyName.equals("xxxx")) {
                        c = 1;
                        break;
                    }
                    break;
                case 153563136:
                    if (policyName.equals("policy-file-share-disabled")) {
                        c = 23;
                        break;
                    }
                    break;
                case 382441887:
                    if (policyName.equals("disable-volume")) {
                        c = 13;
                        break;
                    }
                    break;
                case 458488698:
                    if (policyName.equals("disable-shutdownmenu")) {
                        c = 12;
                        break;
                    }
                    break;
                case 520557972:
                    if (policyName.equals(SettingsMDMPlugin.POLICY_APPLICATION_LOCK)) {
                        c = 27;
                        break;
                    }
                    break;
                case 539407267:
                    if (policyName.equals("disable-power-shutdown")) {
                        c = 11;
                        break;
                    }
                    break;
                case 591717814:
                    if (policyName.equals(SettingsMDMPlugin.POLICY_FORBIDDEN_LOCATION_MODE)) {
                        c = 15;
                        break;
                    }
                    break;
                case 594183088:
                    if (policyName.equals(DISABLE_NOTIFICATION_POLICY)) {
                        c = 4;
                        break;
                    }
                    break;
                case 702979817:
                    if (policyName.equals(DISABLE_HEADPHONE)) {
                        c = 6;
                        break;
                    }
                    break;
                case 731752599:
                    if (policyName.equals(SUPER_WHITE_LIST_APP)) {
                        c = 5;
                        break;
                    }
                    break;
                case 731920490:
                    if (policyName.equals("disable-change-wallpaper")) {
                        c = 9;
                        break;
                    }
                    break;
                case 1044365373:
                    if (policyName.equals(SettingsMDMPlugin.POLICY_FORBIDDEN_NETWORK_LOCATION)) {
                        c = 0;
                        break;
                    }
                    break;
                case 1187313158:
                    if (policyName.equals(SettingsMDMPlugin.POLICY_PARENT_CONTROL)) {
                        c = 25;
                        break;
                    }
                    break;
                case 1502491755:
                    if (policyName.equals(SettingsMDMPlugin.POLICY_FORCE_ENCRYPT_SDCARD)) {
                        c = 29;
                        break;
                    }
                    break;
                case 1695181060:
                    if (policyName.equals(SettingsMDMPlugin.POLICY_ACCESSIBILITY_SERVICES_WHITE_LIST)) {
                        c = 21;
                        break;
                    }
                    break;
                case 1785346365:
                    if (policyName.equals("force-enable-wifi")) {
                        c = 20;
                        break;
                    }
                    break;
                case 1946452102:
                    if (policyName.equals("disable-fingerprint-authentication")) {
                        c = 18;
                        break;
                    }
                    break;
                case 1947338901:
                    if (policyName.equals(SettingsMDMPlugin.DISABLED_ANDROID_ANIMATION)) {
                        c = 28;
                        break;
                    }
                    break;
                case 1981742202:
                    if (policyName.equals("force-enable-BT")) {
                        c = 19;
                        break;
                    }
                    break;
                case 2074833572:
                    if (policyName.equals(SettingsMDMPlugin.POLICY_PHONE_FIND)) {
                        c = 24;
                        break;
                    }
                    break;
                case 2076917186:
                    if (policyName.equals(SettingsMDMPlugin.POLICY_FORBIDDEN_LOCATION_SERVICE)) {
                        c = 14;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    result = this.mSettingsPolicyNetworkLocationStatus;
                    break;
                case 1:
                    result = this.exampleValue;
                    break;
                case 2:
                    result = this.disableMicrophone;
                    break;
                case 3:
                    result = this.mDisableSDCardWritingBundle;
                    break;
                case 4:
                    result = this.mDisableNotificationBundle;
                    break;
                case 5:
                    result = this.superWhiteListApp;
                    break;
                case 6:
                    result = this.disableHeadphone;
                    break;
                case 7:
                    result = this.disableSendNotification;
                    break;
                case 8:
                    result = this.singleApp;
                    break;
                case 9:
                    result = this.disableChangeWallpaper;
                    break;
                case 10:
                    result = this.disableScreenOff;
                    break;
                case 11:
                    result = this.disablePowerShutdown;
                    break;
                case 12:
                    result = this.disableShutdownMenu;
                    break;
                case 13:
                    result = this.disableVolume;
                    break;
                case 14:
                    result = this.mDisableLocationService;
                    break;
                case 15:
                    result = this.mDisableLocationMode;
                    break;
                case 16:
                    result = this.disableSync;
                    break;
                case 17:
                    result = this.disablePassiveProvider;
                    break;
                case 18:
                    result = this.disableFingerprintAuthentication;
                    break;
                case 19:
                    result = this.forceEnableBT;
                    break;
                case 20:
                    result = this.forceEnableWifi;
                    break;
                case 21:
                    result = this.allowAccessibilityServices;
                    break;
                case 22:
                    result = this.mDisablenNavigationBarBundle;
                    break;
                case 23:
                    result = this.forceEnablefileShare;
                    break;
                case 24:
                    result = this.mDisabledPhoneFind;
                    break;
                case 25:
                    result = this.mDisabledParentControl;
                    break;
                case 26:
                    result = this.mDisabledSimLock;
                    break;
                case 27:
                    result = this.mDisabledApplicationLock;
                    break;
                case 28:
                    result = this.disabledAndroidAnimation;
                    break;
                case 29:
                    result = this.mSettingsPolicyForceEncryptSdcard;
                    break;
            }
        }
        return result;
    }

    public boolean getCachedValue(int type) {
        boolean result = false;
        synchronized (this.mLock) {
            if (type == 8) {
                result = this.disableBluetooth;
            } else if (type == 19) {
                result = this.disableDecryptSDCard;
            } else if (type == 29) {
                result = this.disableWifiP2P;
            } else if (type != 32) {
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
                    default:
                        switch (type) {
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
                            default:
                                switch (type) {
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
                                }
                        }
                }
            } else {
                result = this.disableInfrared;
            }
        }
        return result;
    }

    public List<String> getCachedList(int type) {
        List<String> result = null;
        synchronized (this.mLock) {
            if (type == 9) {
                result = this.networkAccessWhitelist;
            } else if (type == 18) {
                result = this.disabledDeactiveMdmPackagesList;
            } else if (type != 20) {
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
                    default:
                        switch (type) {
                            case 27:
                                result = this.ignoreFrequentRelaunchAppList;
                                break;
                            case 28:
                                result = this.disabledApplicationlist;
                                break;
                        }
                }
            } else {
                result = this.installPackageBlacklist;
            }
        }
        return result;
    }
}
