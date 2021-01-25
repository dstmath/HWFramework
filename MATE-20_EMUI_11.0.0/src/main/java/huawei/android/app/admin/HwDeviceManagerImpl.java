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
    private static final String POLICY_NETWORK_WHITE_DOMAIN_LIST = "network-white-domain-list";
    private static final String POLICY_NETWORK_WHITE_IP_LIST = "network-white-ip-list";
    private static final String SLEEP_TIME_INTERVAL_AFTER_POWER_ON = "sleep-time-interval-after-power-on";
    private static final String TAG = "HwDevicePolicyManagerImpl";
    private static final String UNAVAILABLE_SSID_LIST = "unavailable-ssid-list";
    HwDevicePolicyManagerEx mHwDPM;

    public HwDeviceManagerImpl() {
        this.mHwDPM = null;
        this.mHwDPM = new HwDevicePolicyManagerEx();
    }

    public boolean disallowOp(int type) {
        long startTime = SystemClock.uptimeMillis();
        boolean isDisallowed = false;
        if (type == 0) {
            isDisallowed = isWifiDisabled();
        } else if (type == 1) {
            isDisallowed = isVoiceDisabled();
        } else if (type == 2) {
            isDisallowed = isInstallSourceDisabled();
        } else if (type == 6) {
            isDisallowed = isAdbOrSDCardInstallRestricted();
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
            isDisallowed = isWifiP2PDisabled();
        } else if (type == 60) {
            isDisallowed = isFileShareDisabled();
        } else if (type == 61) {
            isDisallowed = isSleepByPowerButtonDisabled();
        } else if (type == 71) {
            isDisallowed = isStatusBarDisabled();
        } else if (type != 72) {
            switch (type) {
                case HwDeviceAdminInfo.USES_POLICY_SET_MDM_BLUETOOTH /* 10 */:
                    isDisallowed = isSafeModeDisabled();
                    break;
                case HwDeviceAdminInfo.USES_POLICY_SET_MDM_APN /* 11 */:
                    isDisallowed = isAdbDisabled();
                    break;
                case HwDeviceAdminInfo.USES_POLICY_SET_MDM_LOCATION /* 12 */:
                    isDisallowed = isUSBOtgDisabled();
                    break;
                case HwDeviceAdminInfo.USES_POLICY_SET_MDM_NETWORK_MANAGER /* 13 */:
                    isDisallowed = isGPSDisabled();
                    break;
                case HwDeviceAdminInfo.USES_POLICY_SET_MDM_PHONE_MANAGER /* 14 */:
                    isDisallowed = isHomeButtonDisabled();
                    break;
                case HwDeviceAdminInfo.USES_POLICY_SET_SDK_LAUNCHER /* 15 */:
                    isDisallowed = isTaskButtonDisabled();
                    break;
                case HwDeviceAdminInfo.USES_POLICY_SET_MDM_CAPTURE_SCREEN /* 16 */:
                    isDisallowed = isBackButtonDisabled();
                    break;
                case HwDeviceAdminInfo.USES_POLICY_SET_MDM_VPN /* 17 */:
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
                                    isDisallowed = isForceEnableBT();
                                    break;
                                case 52:
                                    isDisallowed = isForceEnableWifi();
                                    break;
                                default:
                                    switch (type) {
                                        case DISABLE_OPS_TIMEOUT /* 100 */:
                                            isDisallowed = isWritingSDCardDisabled();
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
        boolean isDisallowed = false;
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
                case HwDeviceAdminInfo.USES_POLICY_SET_MDM_KEYGUARD /* 24 */:
                    isDisallowed = isVisibleGoogleAccountSync(param);
                    break;
                case HwDeviceAdminInfo.USES_POLICY_SET_MDM /* 25 */:
                    isDisallowed = isGoogleAccountAutoSyncDisabled(param);
                    break;
                case HwDeviceAdminInfo.USES_POLICY_SET_MDM_CAMERA /* 26 */:
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
        boolean isDisallowed = false;
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
        return this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_disable_wifi);
    }

    private boolean isNetworkLocationDisabled() {
        Bundle data = this.mHwDPM.getCachedPolicyForFwk(null, DeviceSettingsManager.POLICY_FORBIDDEN_NETWORK_LOCATION, null);
        if (data != null) {
            return data.getBoolean("value", false);
        }
        Log.w(TAG, "HwDevicePolicyManagerEx :: isNetworkLocationDisabled() get null policy data.");
        return false;
    }

    private boolean isBluetoothDisabled() {
        return this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_disable_bluetooth);
    }

    private boolean isVoiceDisabled() {
        return this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_disable_voice);
    }

    private boolean isInstallSourceDisabled() {
        return this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_disable_installsource);
    }

    private boolean isSafeModeDisabled() {
        return this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_disable_safemode);
    }

    private boolean isAdbDisabled() {
        return this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_disable_adb);
    }

    private boolean isUSBOtgDisabled() {
        return this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_disable_usbotg);
    }

    private boolean isGPSDisabled() {
        return this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_disable_gps);
    }

    private boolean isChangeLauncherDisable() {
        return this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_diable_change_launcher);
    }

    private boolean isHomeButtonDisabled() {
        return this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_disable_home);
    }

    private boolean isTaskButtonDisabled() {
        return this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_disable_task);
    }

    private boolean isBackButtonDisabled() {
        return this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_disable_back);
    }

    private boolean isClipboardDisabled() {
        return this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_disable_clipboard);
    }

    private boolean isWifiP2PDisabled() {
        return this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.transaction_setWifiP2PDisabled);
    }

    private boolean isInFraredDisabled() {
        return this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.transaction_setInfraredDisabled);
    }

    private boolean isGoogleAccount(String accountType) {
        return "com.google".equals(accountType);
    }

    private List<String> getSuperWhiteListApp() {
        try {
            Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, ConstantValue.SUPERWHITELISTAPP, null);
            if (bundle != null) {
                return bundle.getStringArrayList("value");
            }
            return null;
        } catch (Exception e) {
            Log.e(TAG, "getSuperWhiteListApp catch Exception");
            return null;
        }
    }

    private boolean isVisibleGoogleAccountSync(String authority) {
        if (TextUtils.isEmpty(authority)) {
            return false;
        }
        SyncAdapterType[] syncs = ContentResolver.getSyncAdapterTypes();
        for (SyncAdapterType adapter : syncs) {
            if (authority.equals(adapter.authority) && isGoogleAccount(adapter.accountType) && adapter.isUserVisible()) {
                return true;
            }
        }
        return false;
    }

    private boolean isGoogleAccountAutoSyncDisabled(String accountType) {
        return isGoogleAccount(accountType) && this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_disable_googleAccount_autosync);
    }

    private boolean isIgnoredFrequentRelaunchApp(String pkgName) {
        List<String> packagelist;
        if (!TextUtils.isEmpty(pkgName) && (packagelist = this.mHwDPM.getHwFrameworkAdminList(ConstantValue.result_ignore_frequent_relaunch_app_list)) != null && packagelist.contains(pkgName)) {
            return true;
        }
        return false;
    }

    private boolean isPersistentApp(String pkgName) {
        if (pkgName == null) {
            return false;
        }
        List<String> packagelist = null;
        try {
            packagelist = this.mHwDPM.getHwFrameworkAdminList(ConstantValue.result_persistentapp_list);
        } catch (Exception e) {
            Log.e(TAG, "isPersistentApp catch Exception");
        }
        if (packagelist == null || !packagelist.contains(pkgName)) {
            return false;
        }
        return true;
    }

    private boolean isDisallowedRunningApp(String pkgName) {
        if (pkgName == null) {
            return false;
        }
        List<String> packagelist = null;
        try {
            packagelist = this.mHwDPM.getHwFrameworkAdminList(ConstantValue.result_disallowedrunning_app_list);
        } catch (Exception e) {
            Log.e(TAG, "isDisallowedRunningApp catch Exception");
        }
        if (packagelist == null || !packagelist.contains(pkgName)) {
            return false;
        }
        return true;
    }

    private boolean isIntentFromAllowedInstallSource(Intent intent) {
        List<String> appMarketPkgNames;
        if (intent == null || !isInstall(intent) || !this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_disable_installsource) || (appMarketPkgNames = this.mHwDPM.getHwFrameworkAdminList(ConstantValue.result_installsource_whitelist)) == null || appMarketPkgNames.isEmpty() || appMarketPkgNames.contains(intent.getStringExtra("caller_package"))) {
            return true;
        }
        return false;
    }

    private boolean isAdbOrSDCardInstallRestricted() {
        boolean isHwDisableinstall = this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_disable_installsource);
        int callingUid = Binder.getCallingUid();
        if (!isHwDisableinstall) {
            return false;
        }
        if (callingUid != 2000 && callingUid != 0) {
            return false;
        }
        Log.d(TAG, "checkInstallPackageDisabled true ");
        return true;
    }

    private boolean isAllowedInstallPackage(String pkgName) {
        if (pkgName == null) {
            return true;
        }
        List<String> packagelist = null;
        try {
            packagelist = this.mHwDPM.getHwFrameworkAdminList(ConstantValue.result_installpackage_whitelist);
        } catch (Exception e) {
            Log.e(TAG, "isAllowedInstallPackage catch Exception");
        }
        if (packagelist == null || packagelist.isEmpty() || packagelist.contains(pkgName)) {
            return true;
        }
        return false;
    }

    private boolean isDisallowedUninstallPackage(String pkgName) {
        if (pkgName == null) {
            return false;
        }
        List<String> packagelist = null;
        try {
            packagelist = this.mHwDPM.getHwFrameworkAdminList(ConstantValue.result_disalloweduninstall_package_list);
        } catch (Exception e) {
            Log.e(TAG, "isDisallowedUninstallPackage catch Exception");
        }
        if (packagelist == null || packagelist.isEmpty() || !packagelist.contains(pkgName)) {
            return false;
        }
        return true;
    }

    private boolean isDisabledDeactivateMdmPackage(String pkgName) {
        if (pkgName == null) {
            return false;
        }
        List<String> packagelist = null;
        try {
            packagelist = this.mHwDPM.getHwFrameworkAdminList(ConstantValue.result_disabled_deactivate_Mdm_package_list);
        } catch (Exception e) {
            Log.e(TAG, "isDisabledDeactivateMdmPackage catch Exception");
        }
        if (packagelist == null || packagelist.isEmpty() || !packagelist.contains(pkgName)) {
            return false;
        }
        return true;
    }

    private boolean isInstall(Intent intent) {
        ComponentName componentName;
        if (intent == null) {
            return false;
        }
        String action = intent.getAction();
        String type = intent.getType();
        if ("android.intent.action.SEND".equals(action) && "application/vnd.android.package-archive".equals(type)) {
            return false;
        }
        if ("android.intent.action.INSTALL_PACKAGE".equals(action) || "application/vnd.android.package-archive".equals(type)) {
            return true;
        }
        if (!"android.intent.action.VIEW".equals(action) || (componentName = intent.getComponent()) == null || !"com.android.packageinstaller".equals(componentName.getPackageName())) {
            return false;
        }
        return true;
    }

    private List<String> getNetworkAccessWhitelist() {
        try {
            return this.mHwDPM.getHwFrameworkAdminList(ConstantValue.result_networkAccess_whitelist);
        } catch (Exception e) {
            Log.e(TAG, "getNetworkAccessWhitelist catch Exception");
            return null;
        }
    }

    private List<String> getNetworkAccessList(String policyName) {
        try {
            Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, policyName, null);
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
            return false;
        }
        List<String> packagelist = null;
        try {
            packagelist = this.mHwDPM.getHwFrameworkAdminList(ConstantValue.result_installpackage_blacklist);
        } catch (Exception e) {
            Log.e(TAG, "isDisallowedInstallPackage catch Exception");
        }
        if (packagelist == null || !packagelist.contains(pkgName)) {
            return false;
        }
        return true;
    }

    private boolean isApplicationDisabled(Intent intent) {
        String pkgName = null;
        if (intent.getComponent() != null) {
            pkgName = intent.getComponent().getPackageName();
        }
        if (pkgName == null) {
            return false;
        }
        String settingPackageName = IS_TV ? "com.huawei.homevision.settings" : DeviceSettingsManager.SETTINGS_APK_NAME;
        try {
            List<String> packagelist = this.mHwDPM.getHwFrameworkAdminList(ConstantValue.result_disable_application_list);
            if (!(packagelist == null || packagelist.size() == 0 || !packagelist.contains(pkgName))) {
                if (!settingPackageName.equals(pkgName) || intent.getComponent() == null || !intent.getComponent().toString().contains("com.android.settings.FallbackHome")) {
                    return true;
                }
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "isApplicationDisabled catch Exception");
        }
        return false;
    }

    private boolean isScreenCaptureDisabled() {
        return this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_disable_capture_screen);
    }

    private boolean isWritingSDCardDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "disable-sdwriting", null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    private boolean isNotificationDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, DISABLE_NOTIFICATION_POLICY, null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    private boolean isMicrophoneDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "disable-microphone", null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    private boolean isHeadphoneDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "disable-headphone", null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    private boolean isNavigationBarDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, DISABLE_NAVIGATIONBAR_POLICY, null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    private boolean isSendNotificationDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, DeviceRestrictionManager.DISABLE_SEND_NOTIFICATION, null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    private String getSingleApp() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, DeviceApplicationManager.POLICY_SINGLE_APP, null);
        if (bundle != null) {
            return bundle.getString("value");
        }
        return null;
    }

    private boolean isChangeWallpaperDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, DeviceRestrictionManager.DISABLE_CHANGE_WALLPAPER, null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    private boolean isScreenOffDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, DeviceSettingsManager.POLICY_FORBIDDEN_SCREEN_OFF, null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    private boolean isFingerprintAuthenticationDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, DeviceRestrictionManager.DISABLE_FINGERPRINT_AUTHENTICATION, null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    private boolean isPowerDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, DeviceRestrictionManager.DISABLE_POWER_SHUTDOWN, null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    private boolean isShutdownMenuDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, DeviceRestrictionManager.DISABLE_SHUTDOWNMENU, null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    private boolean isVolumeAdjustDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, DeviceRestrictionManager.DISABLE_VOLUME, null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    private boolean isLocationServiceDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, DeviceSettingsManager.POLICY_FORBIDDEN_LOCATION_SERVICE, null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    private boolean isLocationModeDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, DeviceSettingsManager.POLICY_FORBIDDEN_LOCATION_MODE, null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    private boolean isRoamingSyncDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "disable-sync", null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    private boolean isPassiveProviderDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "passive_location_disallow_item", null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        Log.i(TAG, "isPassiveProviderDisabled getCachedPolicyForFwk get null data");
        return false;
    }

    private boolean isForceEnableBT() {
        Bundle bundle = new HwDevicePolicyManagerEx().getPolicy(null, DeviceRestrictionManager.FORCE_ENABLE_BT);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    private boolean isForceEnableWifi() {
        Bundle bundle = new HwDevicePolicyManagerEx().getPolicy(null, DeviceRestrictionManager.FORCE_ENABLE_WIFI);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    private boolean isAccessibilityServicesWhiteList(String pkg) {
        if (!TextUtils.isEmpty(pkg) && isAccessibilityServicesMDMWhiteList(pkg)) {
            return true;
        }
        return false;
    }

    private boolean isAccessibilityServicesMDMWhiteList(String pkg) {
        ArrayList<String> servicesList = null;
        try {
            Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, DeviceSettingsManager.POLICY_ACCESSIBILITY_SERVICES_WHITE_LIST, null);
            if (bundle != null) {
                servicesList = bundle.getStringArrayList("value");
            }
        } catch (Exception e) {
            Log.e(TAG, "isAccessibilityServicesMDMWhiteList catch Exception");
        }
        if (servicesList == null || servicesList.isEmpty()) {
            Log.w(TAG, "isAccessibilityServicesWhiteList servicesList is null or empty.");
            return false;
        }
        ArrayList<ComponentName> componeNameWhiteList = transformStringToComponentName(servicesList);
        int size = componeNameWhiteList.size();
        for (int i = 0; i < size; i++) {
            if (componeNameWhiteList.get(i).getPackageName().equals(pkg)) {
                return true;
            }
        }
        return false;
    }

    private ArrayList<ComponentName> transformStringToComponentName(ArrayList<String> list) {
        ArrayList<ComponentName> componentNames = new ArrayList<>();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            componentNames.add(ComponentName.unflattenFromString(list.get(i)));
        }
        return componentNames;
    }

    private boolean isFileShareDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, DeviceRestrictionManager.POLICY_FILE_SHARE, null);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("value");
    }

    private boolean isAndroidAnimationDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, DeviceSettingsManager.DISABLED_ANDROID_ANIMATION, null);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("value");
    }

    private boolean isMultiWindowDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "disable-multi-window", null);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("value");
    }

    private boolean isSleepByPowerButtonDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "disable-screen-turn-off", null);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("value");
    }

    private boolean isWifiUnavailableTipsDisabled(String ssid) {
        Bundle bundle;
        ArrayList<String> list;
        if (TextUtils.isEmpty(ssid) || ssid.length() <= 2 || (bundle = this.mHwDPM.getCachedPolicyForFwk(null, UNAVAILABLE_SSID_LIST, null)) == null || (list = bundle.getStringArrayList("value")) == null) {
            return false;
        }
        return list.contains(ssid.substring(1, ssid.length() - 1));
    }

    private boolean isStatusBarDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, DeviceRestrictionManager.DISABLE_STATUS_BAR, null);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("value");
    }

    private List<String> getTaskLockAppList() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, DeviceApplicationManager.POLICY_TASK_LOCK_APP_LIST, null);
        if (bundle != null) {
            return bundle.getStringArrayList("value");
        }
        return Collections.emptyList();
    }

    private boolean isMediaControlDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, DeviceControlManager.POLICY_SET_MEDIA_CONTROL_DISABLED, null);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("value");
    }

    private boolean isVoiceAssistantButtonDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, DeviceRestrictionManager.DISABLE_VOICE_ASSISTANT_BUTTON, null);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("value");
    }

    private String getSleepTimeIntervalAfterPowerOn() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "sleep-time-interval-after-power-on", null);
        if (bundle != null) {
            return bundle.getString("value");
        }
        return String.valueOf((long) CHECK_USER_OPERATION_TIMEOUT);
    }

    private List<String> getRuntimePermissionFixAppList() {
        try {
            Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, DeviceApplicationManager.FIX_APP_RUNTIME_PERMISSION_LIST, null);
            if (bundle != null) {
                return bundle.getStringArrayList("value");
            }
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            Log.e(TAG, "getRuntimePermissionFixAppList exception.");
        }
        return Collections.emptyList();
    }
}
