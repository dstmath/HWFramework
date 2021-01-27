package huawei.android.app.admin;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SyncAdapterType;
import android.hdm.HwDeviceManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.app.admin.DeviceApplicationManager;
import com.huawei.android.app.admin.DeviceControlManager;
import com.huawei.android.app.admin.DeviceRestrictionManager;
import com.huawei.android.app.admin.DeviceSettingsManager;
import com.huawei.android.app.admin.DeviceWifiPolicyManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class HwDeviceManagerImpl implements HwDeviceManager.IHwDeviceManager {
    private static final String BUNDLE_VALUE_NAME = "value";
    private static final long CHECK_USER_OPERATION_TIMEOUT = 300000;
    private static final String DISABLE_NAVIGATIONBAR_POLICY = "disable-navigationbar";
    private static final String DISABLE_NOTIFICATION_POLICY = "disable-notification";
    private static final int DISABLE_OPS_TIMEOUT = 100;
    private static final boolean IS_TV = "tv".equals(SystemProperties.get("ro.build.characteristics", "default"));
    private static final String POLICY_NETWORK_BLACK_DOMAIN_LIST = "network-black-domain-list";
    private static final String POLICY_NETWORK_BLACK_IP_LIST = "network-black-ip-list";
    private static final String POLICY_NETWORK_TRUST_APP_LIST = "network-trust-app-list";
    private static final String POLICY_NETWORK_WHITE_DOMAIN_LIST = "network-white-domain-list";
    private static final String POLICY_NETWORK_WHITE_IP_LIST = "network-white-ip-list";
    private static final String SETTINGS_FALLBACK_ACTIVITY_NAME = "com.android.settings.FallbackHome";
    private static final String SLEEP_TIME_INTERVAL_AFTER_POWER_ON = "sleep-time-interval-after-power-on";
    private static final String TAG = "HwDevicePolicyManagerImpl";
    private static final String UNAVAILABLE_SSID_LIST = "unavailable-ssid-list";
    HwDevicePolicyManagerEx mHwDpm;

    public HwDeviceManagerImpl() {
        this.mHwDpm = null;
        this.mHwDpm = new HwDevicePolicyManagerEx();
    }

    public boolean disallowOp(int type) {
        long startTime = SystemClock.uptimeMillis();
        boolean isDisallowed = IS_TV;
        if (type == 0) {
            isDisallowed = isWifiDisabled();
        } else if (type == 1) {
            isDisallowed = isVoiceDisabled();
        } else if (type == 2) {
            isDisallowed = isInstallSourceDisabled();
        } else if (type == 6) {
            isDisallowed = isAdbOrSdCardInstallRestricted();
        } else if (type == 8) {
            isDisallowed = isBluetoothDisabled();
        } else if (type == 20) {
            isDisallowed = isScreenCaptureDisabled();
        } else if (type == 23) {
            isDisallowed = isClipboardDisabled();
        } else if (type == 33) {
            isDisallowed = isSendNotificationDisabled();
        } else if (type == 54) {
            isDisallowed = isMultiWindowDisabled();
        } else if (type == 66) {
            isDisallowed = isMediaControlDisabled();
        } else if (type == 30) {
            isDisallowed = isMicrophoneDisabled();
        } else if (type == 31) {
            isDisallowed = isHeadphoneDisabled();
        } else if (type == 44) {
            isDisallowed = isAndroidAnimationDisabled();
        } else if (type == 45) {
            isDisallowed = isWifiP2pDisabled();
        } else if (type == 60) {
            isDisallowed = isFileShareDisabled();
        } else if (type == 61) {
            isDisallowed = isSleepByPowerButtonDisabled();
        } else if (type == 71) {
            isDisallowed = isStatusBarDisabled();
        } else if (type != 72) {
            switch (type) {
                case 10:
                    isDisallowed = isSafeModeDisabled();
                    break;
                case 11:
                    isDisallowed = isAdbDisabled();
                    break;
                case 12:
                    isDisallowed = isUsbOtgDisabled();
                    break;
                case 13:
                    isDisallowed = isGpsDisabled();
                    break;
                case 14:
                    isDisallowed = isHomeButtonDisabled();
                    break;
                case 15:
                    isDisallowed = isTaskButtonDisabled();
                    break;
                case 16:
                    isDisallowed = isBackButtonDisabled();
                    break;
                case 17:
                    isDisallowed = isChangeLauncherDisable();
                    break;
                default:
                    switch (type) {
                        case 35:
                            isDisallowed = isChangeWallpaperDisabled();
                            break;
                        case 36:
                            isDisallowed = isScreenOffDisabled();
                            break;
                        case 37:
                            isDisallowed = isPowerDisabled();
                            break;
                        case 38:
                            isDisallowed = isVolumeAdjustDisabled();
                            break;
                        case 39:
                            isDisallowed = isLocationServiceDisabled();
                            break;
                        case 40:
                            isDisallowed = isLocationModeDisabled();
                            break;
                        case 41:
                            isDisallowed = isNetworkLocationDisabled();
                            break;
                        case 42:
                            isDisallowed = isRoamingSyncDisabled();
                            break;
                        default:
                            switch (type) {
                                case 48:
                                    isDisallowed = isInFraredDisabled();
                                    break;
                                case 49:
                                    isDisallowed = isShutdownMenuDisabled();
                                    break;
                                case 50:
                                    isDisallowed = isFingerprintAuthenticationDisabled();
                                    break;
                                case 51:
                                    isDisallowed = isForceEnableBluetooth();
                                    break;
                                case 52:
                                    isDisallowed = isForceEnableWifi();
                                    break;
                                default:
                                    switch (type) {
                                        case DISABLE_OPS_TIMEOUT /* 100 */:
                                            isDisallowed = isWritingSdCardDisabled();
                                            break;
                                        case 101:
                                            isDisallowed = isPassiveProviderDisabled();
                                            break;
                                        case 102:
                                            isDisallowed = isNotificationDisabled();
                                            break;
                                        case 103:
                                            try {
                                                isDisallowed = isNavigationBarDisabled();
                                                break;
                                            } catch (Exception e) {
                                                Log.e(TAG, "Disallow operation " + type + ", an exception occured");
                                                break;
                                            }
                                    }
                            }
                    }
            }
        } else {
            isDisallowed = isVoiceAssistantButtonDisabled();
        }
        if (SystemClock.uptimeMillis() - startTime > 100) {
            Log.w(TAG, "disallowOp timeout. type:" + type);
        }
        return isDisallowed;
    }

    public boolean disallowOp(int type, String param) {
        long startTime = SystemClock.uptimeMillis();
        boolean isDisallowed = IS_TV;
        if (type == 3) {
            isDisallowed = isPersistentApp(param);
        } else if (type == 4) {
            isDisallowed = isDisallowedRunningApp(param);
        } else if (type == 5) {
            isDisallowed = isDisallowedUninstallPackage(param);
        } else if (type == 7) {
            isDisallowed = !isAllowedInstallPackage(param);
        } else if (type == 53) {
            isDisallowed = isAccessibilityServicesWhiteList(param);
        } else if (type == 70) {
            isDisallowed = isWifiUnavailableTipsDisabled(param);
        } else if (type == 18) {
            isDisallowed = isDisabledDeactivateMdmPackage(param);
        } else if (type != 19) {
            switch (type) {
                case 24:
                    isDisallowed = isVisibleGoogleAccountSync(param);
                    break;
                case 25:
                    isDisallowed = isGoogleAccountAutoSyncDisabled(param);
                    break;
                case 26:
                    try {
                        isDisallowed = isIgnoredFrequentRelaunchApp(param);
                        break;
                    } catch (Exception e) {
                        Log.e(TAG, "Disallow operation " + type + ", an exception occured");
                        break;
                    }
            }
        } else {
            isDisallowed = isDisallowedInstallPackage(param);
        }
        if (SystemClock.uptimeMillis() - startTime > 100) {
            Log.w(TAG, "disallowOp timeout. type:" + type + ", param:" + param);
        }
        return isDisallowed;
    }

    public boolean disallowOp(int type, Intent intent) {
        long startTime = SystemClock.uptimeMillis();
        boolean isDisallowed = IS_TV;
        if (type == 21) {
            isDisallowed = isApplicationDisabled(intent);
        } else if (type == 27) {
            try {
                isDisallowed = !isIntentFromAllowedInstallSource(intent);
            } catch (Exception e) {
                Log.e(TAG, "Disallow operation " + type + ", an exception occured");
            }
        }
        if (SystemClock.uptimeMillis() - startTime > 100) {
            Log.w(TAG, "disallowOp timeout. type:" + type);
        }
        return isDisallowed;
    }

    public List<String> getList(int type) {
        if (type == 9) {
            return getNetworkAccessWhitelist();
        }
        if (type == 22) {
            return getSuperWhiteListApp();
        }
        if (type == 74) {
            return getNetworkAccessList(POLICY_NETWORK_TRUST_APP_LIST);
        }
        if (type == 55) {
            return getTaskLockAppList();
        }
        if (type == 56) {
            return getRuntimePermissionFixAppList();
        }
        switch (type) {
            case 62:
                return getNetworkAccessList(POLICY_NETWORK_WHITE_IP_LIST);
            case 63:
                return getNetworkAccessList(POLICY_NETWORK_BLACK_IP_LIST);
            case DeviceWifiPolicyManager.SSID_LENGTH_LIMIT /* 64 */:
                return getNetworkAccessList(POLICY_NETWORK_WHITE_DOMAIN_LIST);
            case 65:
                try {
                    return getNetworkAccessList(POLICY_NETWORK_BLACK_DOMAIN_LIST);
                } catch (Exception e) {
                    Log.e(TAG, "Get list " + type + ", an exception occured");
                    return null;
                }
            default:
                return null;
        }
    }

    public String getString(int type) {
        if (type == 34) {
            return getSingleApp();
        }
        if (type != 73) {
            return null;
        }
        return getSleepTimeIntervalAfterPowerOn();
    }

    private boolean isWifiDisabled() {
        return this.mHwDpm.isHwFrameworkAdminAllowed(ConstantValue.RESULT_DISABLED_WIFI);
    }

    private boolean isNetworkLocationDisabled() {
        Bundle data = this.mHwDpm.getCachedPolicyForFwk(null, "settings_policy_forbidden_network_location", null);
        if (data != null) {
            return data.getBoolean("value", IS_TV);
        }
        Log.w(TAG, "HwDevicePolicyManagerEx :: isNetworkLocationDisabled() get null policy data.");
        return IS_TV;
    }

    private boolean isBluetoothDisabled() {
        return this.mHwDpm.isHwFrameworkAdminAllowed(ConstantValue.RESULT_DISABLED_BLUETOOTH);
    }

    private boolean isVoiceDisabled() {
        return this.mHwDpm.isHwFrameworkAdminAllowed(ConstantValue.RESULT_DISABLED_VOICE);
    }

    private boolean isInstallSourceDisabled() {
        return this.mHwDpm.isHwFrameworkAdminAllowed(ConstantValue.RESULT_DISABLED_INSTALL_SOURCE);
    }

    private boolean isSafeModeDisabled() {
        return this.mHwDpm.isHwFrameworkAdminAllowed(ConstantValue.RESULT_DISABLED_SAFEMODE);
    }

    private boolean isAdbDisabled() {
        return this.mHwDpm.isHwFrameworkAdminAllowed(ConstantValue.RESULT_DISABLED_ADB);
    }

    private boolean isUsbOtgDisabled() {
        return this.mHwDpm.isHwFrameworkAdminAllowed(ConstantValue.RESULT_DISABLED_USB_OTG);
    }

    private boolean isGpsDisabled() {
        return this.mHwDpm.isHwFrameworkAdminAllowed(ConstantValue.RESULT_DISABLED_GPS);
    }

    private boolean isChangeLauncherDisable() {
        return this.mHwDpm.isHwFrameworkAdminAllowed(ConstantValue.RESULT_DISABLED_CHANGE_LAUNCHER);
    }

    private boolean isHomeButtonDisabled() {
        return this.mHwDpm.isHwFrameworkAdminAllowed(ConstantValue.RESULT_DISABLED_HOME);
    }

    private boolean isTaskButtonDisabled() {
        return this.mHwDpm.isHwFrameworkAdminAllowed(ConstantValue.RESULT_DISABLED_TASK);
    }

    private boolean isBackButtonDisabled() {
        return this.mHwDpm.isHwFrameworkAdminAllowed(ConstantValue.RESULT_DISABLED_BACK);
    }

    private boolean isClipboardDisabled() {
        return this.mHwDpm.isHwFrameworkAdminAllowed(ConstantValue.RESULT_DISABLED_CLIPBOARD);
    }

    private boolean isWifiP2pDisabled() {
        return this.mHwDpm.isHwFrameworkAdminAllowed(ConstantValue.TRANSACTION_SET_WIFI_P2P_DISABLED);
    }

    private boolean isInFraredDisabled() {
        return this.mHwDpm.isHwFrameworkAdminAllowed(ConstantValue.TRANSACTION_SET_INFRARED_DISABLED);
    }

    private boolean isGoogleAccount(String accountType) {
        return "com.google".equals(accountType);
    }

    private List<String> getSuperWhiteListApp() {
        try {
            Bundle bundle = this.mHwDpm.getCachedPolicyForFwk(null, "super-whitelist-hwsystemmanager", null);
            if (bundle != null) {
                return bundle.getStringArrayList("value");
            }
            return null;
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            Log.e(TAG, "getSuperWhiteListApp get list Exception");
            return null;
        } catch (Exception e2) {
            Log.e(TAG, "getSuperWhiteListApp catch Exception");
            return null;
        }
    }

    private boolean isVisibleGoogleAccountSync(String authority) {
        if (TextUtils.isEmpty(authority)) {
            return IS_TV;
        }
        SyncAdapterType[] syncs = ContentResolver.getSyncAdapterTypes();
        for (SyncAdapterType adapter : syncs) {
            if (authority.equals(adapter.authority) && isGoogleAccount(adapter.accountType) && adapter.isUserVisible()) {
                return true;
            }
        }
        return IS_TV;
    }

    private boolean isGoogleAccountAutoSyncDisabled(String accountType) {
        if (!isGoogleAccount(accountType) || !this.mHwDpm.isHwFrameworkAdminAllowed(ConstantValue.RESULT_DISABLED_GOOGLE_ACCOUNT_AUTO_SYNC)) {
            return IS_TV;
        }
        return true;
    }

    private boolean isIgnoredFrequentRelaunchApp(String pkgName) {
        List<String> packagelists;
        if (!TextUtils.isEmpty(pkgName) && (packagelists = this.mHwDpm.getHwFrameworkAdminList(ConstantValue.RESULT_IGNORE_FREQUENT_RELAUNCH_APP_LIST)) != null && packagelists.contains(pkgName)) {
            return true;
        }
        return IS_TV;
    }

    private boolean isPersistentApp(String pkgName) {
        if (pkgName == null) {
            return IS_TV;
        }
        List<String> packagelists = null;
        try {
            packagelists = this.mHwDpm.getHwFrameworkAdminList(ConstantValue.RESULT_PERSISTENT_APP_LIST);
        } catch (Exception e) {
            Log.e(TAG, "isPersistentApp catch Exception");
        }
        if (packagelists == null || !packagelists.contains(pkgName)) {
            return IS_TV;
        }
        return true;
    }

    private boolean isDisallowedRunningApp(String pkgName) {
        if (pkgName == null) {
            return IS_TV;
        }
        List<String> packagelists = null;
        try {
            packagelists = this.mHwDpm.getHwFrameworkAdminList(ConstantValue.RESULT_DISALLOWED_RUNNING_APP_LIST);
        } catch (Exception e) {
            Log.e(TAG, "isDisallowedRunningApp catch Exception");
        }
        if (packagelists == null || !packagelists.contains(pkgName)) {
            return IS_TV;
        }
        return true;
    }

    private boolean isIntentFromAllowedInstallSource(Intent intent) {
        List<String> appMarketPkgNames;
        if (intent == null || !isInstall(intent) || !this.mHwDpm.isHwFrameworkAdminAllowed(ConstantValue.RESULT_DISABLED_INSTALL_SOURCE) || (appMarketPkgNames = this.mHwDpm.getHwFrameworkAdminList(ConstantValue.RESULT_INSTALL_SOURCE_WHITELIST)) == null || appMarketPkgNames.isEmpty()) {
            return true;
        }
        return appMarketPkgNames.contains(intent.getStringExtra("caller_package"));
    }

    private boolean isAdbOrSdCardInstallRestricted() {
        boolean isHwDisableinstall = this.mHwDpm.isHwFrameworkAdminAllowed(ConstantValue.RESULT_DISABLED_INSTALL_SOURCE);
        int callingUid = Binder.getCallingUid();
        if (!isHwDisableinstall) {
            return IS_TV;
        }
        if (callingUid != 2000 && callingUid != 0) {
            return IS_TV;
        }
        Log.d(TAG, "checkInstallPackageDisabled true ");
        return true;
    }

    private boolean isAllowedInstallPackage(String pkgName) {
        if (pkgName == null) {
            return true;
        }
        List<String> packagelists = null;
        try {
            packagelists = this.mHwDpm.getHwFrameworkAdminList(ConstantValue.RESULT_INSTALL_PACKAGE_WHITELIST);
        } catch (Exception e) {
            Log.e(TAG, "isAllowedInstallPackage catch Exception");
        }
        if (packagelists == null || packagelists.isEmpty() || packagelists.contains(pkgName)) {
            return true;
        }
        return IS_TV;
    }

    private boolean isDisallowedUninstallPackage(String pkgName) {
        if (pkgName == null) {
            return IS_TV;
        }
        List<String> packagelists = null;
        try {
            packagelists = this.mHwDpm.getHwFrameworkAdminList(ConstantValue.RESULT_DISALLOWED_UNINSTALL_PACKAGE_LIST);
        } catch (Exception e) {
            Log.e(TAG, "isDisallowedUninstallPackage catch Exception");
        }
        if (packagelists == null || packagelists.isEmpty()) {
            return IS_TV;
        }
        return packagelists.contains(pkgName);
    }

    private boolean isDisabledDeactivateMdmPackage(String pkgName) {
        if (pkgName == null) {
            return IS_TV;
        }
        List<String> packagelists = null;
        try {
            packagelists = this.mHwDpm.getHwFrameworkAdminList(ConstantValue.RESULT_DISABLED_DEACTIVATE_MDM_PACKAGE_LIST);
        } catch (Exception e) {
            Log.e(TAG, "isDisabledDeactivateMdmPackage catch Exception");
        }
        if (packagelists == null || packagelists.isEmpty()) {
            return IS_TV;
        }
        return packagelists.contains(pkgName);
    }

    private boolean isInstall(Intent intent) {
        ComponentName componentName;
        if (intent == null) {
            return IS_TV;
        }
        String action = intent.getAction();
        String type = intent.getType();
        if ("android.intent.action.SEND".equals(action) && "application/vnd.android.package-archive".equals(type)) {
            return IS_TV;
        }
        if ("android.intent.action.INSTALL_PACKAGE".equals(action) || "application/vnd.android.package-archive".equals(type)) {
            return true;
        }
        if (!"android.intent.action.VIEW".equals(action) || (componentName = intent.getComponent()) == null || !"com.android.packageinstaller".equals(componentName.getPackageName())) {
            return IS_TV;
        }
        return true;
    }

    private List<String> getNetworkAccessWhitelist() {
        try {
            return this.mHwDpm.getHwFrameworkAdminList(ConstantValue.RESULT_NETWORK_ACCESS_WHITELIST);
        } catch (Exception e) {
            Log.e(TAG, "getNetworkAccessWhitelist catch Exception");
            return null;
        }
    }

    private List<String> getNetworkAccessList(String policyName) {
        try {
            Bundle bundle = this.mHwDpm.getCachedPolicyForFwk(null, policyName, null);
            if (bundle != null) {
                ArrayList<String> policyList = bundle.getStringArrayList("value");
                if (policyList != null) {
                    if (!policyList.isEmpty()) {
                        return policyList;
                    }
                }
                return Collections.emptyList();
            }
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            Log.e(TAG, "getDomainNameNetworkList catch Exception");
        }
        return Collections.emptyList();
    }

    private boolean isDisallowedInstallPackage(String pkgName) {
        if (pkgName == null) {
            return IS_TV;
        }
        List<String> packagelists = null;
        try {
            packagelists = this.mHwDpm.getHwFrameworkAdminList(ConstantValue.RESULT_INSTALL_PACKAGE_BLACKLIST);
        } catch (Exception e) {
            Log.e(TAG, "isDisallowedInstallPackage catch Exception");
        }
        if (packagelists == null || !packagelists.contains(pkgName)) {
            return IS_TV;
        }
        return true;
    }

    private boolean isApplicationDisabled(Intent intent) {
        ComponentName componentName;
        String pkgName;
        if (intent == null || (componentName = intent.getComponent()) == null || (pkgName = componentName.getPackageName()) == null) {
            return IS_TV;
        }
        String settingPackageName = IS_TV ? "com.huawei.homevision.settings" : "com.android.settings";
        try {
            List<String> packagelists = this.mHwDpm.getHwFrameworkAdminList(ConstantValue.RESULT_DISABLED_APPLICATION_LIST);
            if (packagelists != null) {
                if (!packagelists.isEmpty()) {
                    if (packagelists.contains(pkgName)) {
                        if (!settingPackageName.equals(pkgName) || !componentName.toString().contains(SETTINGS_FALLBACK_ACTIVITY_NAME)) {
                            return true;
                        }
                        return IS_TV;
                    }
                    return IS_TV;
                }
            }
            return IS_TV;
        } catch (Exception e) {
            Log.e(TAG, "isApplicationDisabled catch Exception");
        }
    }

    private boolean isScreenCaptureDisabled() {
        return this.mHwDpm.isHwFrameworkAdminAllowed(ConstantValue.RESULT_DISABLED_CAPTURE_SCREEN);
    }

    private boolean isWritingSdCardDisabled() {
        Bundle bundle = this.mHwDpm.getCachedPolicyForFwk(null, "disable-sdwriting", null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return IS_TV;
    }

    private boolean isNotificationDisabled() {
        Bundle bundle = this.mHwDpm.getCachedPolicyForFwk(null, DISABLE_NOTIFICATION_POLICY, null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return IS_TV;
    }

    private boolean isMicrophoneDisabled() {
        Bundle bundle = this.mHwDpm.getCachedPolicyForFwk(null, "disable-microphone", null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return IS_TV;
    }

    private boolean isHeadphoneDisabled() {
        Bundle bundle = this.mHwDpm.getCachedPolicyForFwk(null, "disable-headphone", null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return IS_TV;
    }

    private boolean isNavigationBarDisabled() {
        Bundle bundle = this.mHwDpm.getCachedPolicyForFwk(null, DISABLE_NAVIGATIONBAR_POLICY, null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return IS_TV;
    }

    private boolean isSendNotificationDisabled() {
        Bundle bundle = this.mHwDpm.getCachedPolicyForFwk(null, DeviceRestrictionManager.DISABLE_SEND_NOTIFICATION, null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return IS_TV;
    }

    private String getSingleApp() {
        Bundle bundle = this.mHwDpm.getCachedPolicyForFwk(null, DeviceApplicationManager.POLICY_SINGLE_APP, null);
        if (bundle != null) {
            return bundle.getString("value");
        }
        return null;
    }

    private boolean isChangeWallpaperDisabled() {
        Bundle bundle = this.mHwDpm.getCachedPolicyForFwk(null, DeviceRestrictionManager.DISABLE_CHANGE_WALLPAPER, null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return IS_TV;
    }

    private boolean isScreenOffDisabled() {
        Bundle bundle = this.mHwDpm.getCachedPolicyForFwk(null, "settings_policy_forbidden_screen_off", null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return IS_TV;
    }

    private boolean isFingerprintAuthenticationDisabled() {
        Bundle bundle = this.mHwDpm.getCachedPolicyForFwk(null, DeviceRestrictionManager.DISABLE_FINGERPRINT_AUTHENTICATION, null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return IS_TV;
    }

    private boolean isPowerDisabled() {
        Bundle bundle = this.mHwDpm.getCachedPolicyForFwk(null, DeviceRestrictionManager.DISABLE_POWER_SHUTDOWN, null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return IS_TV;
    }

    private boolean isShutdownMenuDisabled() {
        Bundle bundle = this.mHwDpm.getCachedPolicyForFwk(null, DeviceRestrictionManager.DISABLE_SHUTDOWNMENU, null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return IS_TV;
    }

    private boolean isVolumeAdjustDisabled() {
        Bundle bundle = this.mHwDpm.getCachedPolicyForFwk(null, DeviceRestrictionManager.DISABLE_VOLUME, null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return IS_TV;
    }

    private boolean isLocationServiceDisabled() {
        Bundle bundle = this.mHwDpm.getCachedPolicyForFwk(null, "settings_policy_forbidden_location_service", null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return IS_TV;
    }

    private boolean isLocationModeDisabled() {
        Bundle bundle = this.mHwDpm.getCachedPolicyForFwk(null, "settings_policy_forbidden_location_mode", null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return IS_TV;
    }

    private boolean isRoamingSyncDisabled() {
        Bundle bundle = this.mHwDpm.getCachedPolicyForFwk(null, "disable-sync", null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return IS_TV;
    }

    private boolean isPassiveProviderDisabled() {
        Bundle bundle = this.mHwDpm.getCachedPolicyForFwk(null, "passive_location_disallow_item", null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        Log.i(TAG, "isPassiveProviderDisabled getCachedPolicyForFwk get null data");
        return IS_TV;
    }

    private boolean isForceEnableBluetooth() {
        Bundle bundle = new HwDevicePolicyManagerEx().getPolicy(null, DeviceRestrictionManager.FORCE_ENABLE_BT);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return IS_TV;
    }

    private boolean isForceEnableWifi() {
        Bundle bundle = new HwDevicePolicyManagerEx().getPolicy(null, DeviceRestrictionManager.FORCE_ENABLE_WIFI);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return IS_TV;
    }

    private boolean isAccessibilityServicesWhiteList(String pkg) {
        if (TextUtils.isEmpty(pkg)) {
            return IS_TV;
        }
        return isAccessibilityServicesMdmWhiteList(pkg);
    }

    private boolean isAccessibilityServicesMdmWhiteList(String pkg) {
        ArrayList<String> servicesLists = null;
        try {
            Bundle bundle = this.mHwDpm.getCachedPolicyForFwk(null, "accessibility_services_white_list", null);
            if (bundle != null) {
                servicesLists = bundle.getStringArrayList("value");
            }
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            Log.e(TAG, "isAccessibilityServicesMDMWhiteList get list Exception");
        } catch (Exception e2) {
            Log.e(TAG, "isAccessibilityServicesMDMWhiteList catch Exception");
        }
        if (servicesLists == null || servicesLists.isEmpty()) {
            Log.w(TAG, "isAccessibilityServicesWhiteList servicesList is null or empty.");
            return IS_TV;
        }
        Iterator<ComponentName> it = transformStringToComponentName(servicesLists).iterator();
        while (it.hasNext()) {
            ComponentName cpName = it.next();
            if (cpName != null && cpName.getPackageName().equals(pkg)) {
                return true;
            }
        }
        return IS_TV;
    }

    private ArrayList<ComponentName> transformStringToComponentName(ArrayList<String> lists) {
        ArrayList<ComponentName> componentNames = new ArrayList<>();
        Iterator<String> it = lists.iterator();
        while (it.hasNext()) {
            componentNames.add(ComponentName.unflattenFromString(it.next()));
        }
        return componentNames;
    }

    private boolean isFileShareDisabled() {
        Bundle bundle = this.mHwDpm.getCachedPolicyForFwk(null, DeviceRestrictionManager.POLICY_FILE_SHARE, null);
        if (bundle == null) {
            return IS_TV;
        }
        return bundle.getBoolean("value");
    }

    private boolean isAndroidAnimationDisabled() {
        Bundle bundle = this.mHwDpm.getCachedPolicyForFwk(null, DeviceSettingsManager.DISABLED_ANDROID_ANIMATION, null);
        return bundle == null ? IS_TV : bundle.getBoolean("value");
    }

    private boolean isMultiWindowDisabled() {
        Bundle bundle = this.mHwDpm.getCachedPolicyForFwk(null, "disable-multi-window", null);
        return bundle == null ? IS_TV : bundle.getBoolean("value");
    }

    private boolean isSleepByPowerButtonDisabled() {
        Bundle bundle = this.mHwDpm.getCachedPolicyForFwk(null, "disable-screen-turn-off", null);
        if (bundle == null) {
            return IS_TV;
        }
        return bundle.getBoolean("value");
    }

    private boolean isWifiUnavailableTipsDisabled(String ssid) {
        Bundle bundle;
        ArrayList<String> lists;
        if (TextUtils.isEmpty(ssid) || ssid.length() <= 2 || (bundle = this.mHwDpm.getCachedPolicyForFwk(null, UNAVAILABLE_SSID_LIST, null)) == null || (lists = bundle.getStringArrayList("value")) == null) {
            return IS_TV;
        }
        return lists.contains(ssid.substring(1, ssid.length() - 1));
    }

    private boolean isStatusBarDisabled() {
        Bundle bundle = this.mHwDpm.getCachedPolicyForFwk(null, DeviceRestrictionManager.DISABLE_STATUS_BAR, null);
        return bundle == null ? IS_TV : bundle.getBoolean("value");
    }

    private List<String> getTaskLockAppList() {
        Bundle bundle = this.mHwDpm.getCachedPolicyForFwk(null, DeviceApplicationManager.POLICY_TASK_LOCK_APP_LIST, null);
        if (bundle != null) {
            return bundle.getStringArrayList("value");
        }
        return Collections.emptyList();
    }

    private boolean isMediaControlDisabled() {
        Bundle bundle = this.mHwDpm.getCachedPolicyForFwk(null, DeviceControlManager.POLICY_SET_MEDIA_CONTROL_DISABLED, null);
        return bundle == null ? IS_TV : bundle.getBoolean("value");
    }

    private boolean isVoiceAssistantButtonDisabled() {
        Bundle bundle = this.mHwDpm.getCachedPolicyForFwk(null, DeviceRestrictionManager.DISABLE_VOICE_ASSISTANT_BUTTON, null);
        return bundle == null ? IS_TV : bundle.getBoolean("value");
    }

    private String getSleepTimeIntervalAfterPowerOn() {
        Bundle bundle = this.mHwDpm.getCachedPolicyForFwk(null, "sleep-time-interval-after-power-on", null);
        if (bundle != null) {
            return bundle.getString("value");
        }
        return String.valueOf((long) CHECK_USER_OPERATION_TIMEOUT);
    }

    private List<String> getRuntimePermissionFixAppList() {
        try {
            Bundle bundle = this.mHwDpm.getCachedPolicyForFwk(null, DeviceApplicationManager.FIX_APP_RUNTIME_PERMISSION_LIST, null);
            if (bundle != null) {
                return bundle.getStringArrayList("value");
            }
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            Log.e(TAG, "getRuntimePermissionFixAppList exception.");
        }
        return Collections.emptyList();
    }
}
