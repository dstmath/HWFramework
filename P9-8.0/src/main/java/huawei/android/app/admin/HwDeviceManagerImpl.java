package huawei.android.app.admin;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SyncAdapterType;
import android.hdm.HwDeviceManager.IHwDeviceManager;
import android.os.Binder;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import huawei.android.provider.HwSettings.System;
import java.util.ArrayList;
import java.util.List;

public class HwDeviceManagerImpl implements IHwDeviceManager {
    private static final String DISABLE_NAVIGATIONBAR_POLICY = "disable-navigationbar";
    private static final String DISABLE_NOTIFICATION_POLICY = "disable-notification";
    private static final String TAG = "HwDevicePolicyManagerImpl";
    HwDevicePolicyManagerEx mHwDPM;

    public HwDeviceManagerImpl() {
        this.mHwDPM = null;
        this.mHwDPM = new HwDevicePolicyManagerEx();
    }

    public boolean isWifiDisabled() {
        return this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_disable_wifi);
    }

    public boolean isNetworkLocationDisabled() {
        String POLICY_FORBIDDEN_NETWORK_LOCATION = "settings_policy_forbidden_network_location";
        String STATE_VALUE = "value";
        Bundle data = this.mHwDPM.getCachedPolicyForFwk(null, "settings_policy_forbidden_network_location", null);
        if (data != null) {
            return data.getBoolean("value", false);
        }
        Log.w(TAG, "HwDevicePolicyManagerEx :: isNetworkLocationDisabled() get null policy data.");
        return false;
    }

    public boolean isBluetoothDisabled() {
        return this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_disable_bluetooth);
    }

    public boolean isVoiceDisabled() {
        return this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_disable_voice);
    }

    public boolean isInstallSourceDisabled() {
        return this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_disable_installsource);
    }

    public boolean isSafeModeDisabled() {
        return this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_disable_safemode);
    }

    public boolean isAdbDisabled() {
        return this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_disable_adb);
    }

    public boolean isUSBOtgDisabled() {
        return this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_disable_usbotg);
    }

    public boolean isGPSDisabled() {
        return this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_disable_gps);
    }

    public boolean isChangeLauncherDisable() {
        return this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_diable_change_launcher);
    }

    public boolean isHomeButtonDisabled() {
        return this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_disable_home);
    }

    public boolean isTaskButtonDisabled() {
        return this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_disable_task);
    }

    public boolean isBackButtonDisabled() {
        return this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_disable_back);
    }

    public boolean isClipboardDisabled() {
        return this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_disable_clipboard);
    }

    public boolean isWifiP2PDisabled() {
        return this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.transaction_setWifiP2PDisabled);
    }

    public boolean isInFraredDisabled() {
        return this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.transaction_setInfraredDisabled);
    }

    private boolean isGoogleAccount(String accountType) {
        String GOOGLE_ACCOUNT_TYPE = "com.google";
        return "com.google".equals(accountType);
    }

    public boolean isSuperWhiteListApp(String pkgName) {
        List packagelist = null;
        try {
            packagelist = this.mHwDPM.getCachedPolicyForFwk(null, ConstantValue.SUPERWHITELISTAPP, null).getStringArrayList("value");
        } catch (Exception e) {
            Log.e(TAG, "getInstallPackageWhiteList error : " + e.getMessage());
        }
        if (packagelist == null || !packagelist.contains(pkgName)) {
            return false;
        }
        return true;
    }

    public boolean isVisibleGoogleAccountSync(String authority) {
        if (TextUtils.isEmpty(authority)) {
            return false;
        }
        for (SyncAdapterType adapter : ContentResolver.getSyncAdapterTypes()) {
            if (authority.equals(adapter.authority) && isGoogleAccount(adapter.accountType) && adapter.isUserVisible()) {
                return true;
            }
        }
        return false;
    }

    public boolean isGoogleAccountAutoSyncDisabled(String accountType) {
        return isGoogleAccount(accountType) ? this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_disable_googleAccount_autosync) : false;
    }

    public boolean isIgnoredFrequentRelaunchApp(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            return false;
        }
        List<String> packagelist = this.mHwDPM.getHwFrameworkAdminList(ConstantValue.result_ignore_frequent_relaunch_app_list);
        if (packagelist == null || !packagelist.contains(pkgName)) {
            return false;
        }
        return true;
    }

    public boolean isPersistentApp(String pkgName) {
        if (pkgName == null) {
            return false;
        }
        List packagelist = null;
        try {
            packagelist = this.mHwDPM.getHwFrameworkAdminList(ConstantValue.result_persistentapp_list);
        } catch (Exception e) {
            Log.e(TAG, "getInstallPackageWhiteList error : " + e.getMessage());
        }
        if (packagelist == null || !packagelist.contains(pkgName)) {
            return false;
        }
        return true;
    }

    public boolean isDisallowedRunningApp(String pkgName) {
        if (pkgName == null) {
            return false;
        }
        List packagelist = null;
        try {
            packagelist = this.mHwDPM.getHwFrameworkAdminList(ConstantValue.result_disallowedrunning_app_list);
        } catch (Exception e) {
            Log.e(TAG, "getInstallPackageWhiteList error : " + e.getMessage());
        }
        if (packagelist == null || !packagelist.contains(pkgName)) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: Missing block: B:4:0x000b, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isIntentFromAllowedInstallSource(Intent intent) {
        if (intent == null || (isInstall(intent) ^ 1) != 0 || !this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_disable_installsource)) {
            return true;
        }
        List<String> appMarketPkgNames = this.mHwDPM.getHwFrameworkAdminList(ConstantValue.result_installsource_whitelist);
        if (appMarketPkgNames == null || appMarketPkgNames.isEmpty()) {
            return true;
        }
        String CALLER_PACKAGE = "caller_package";
        if (appMarketPkgNames.contains(intent.getStringExtra("caller_package"))) {
            return true;
        }
        return false;
    }

    public boolean isAdbOrSDCardInstallRestricted() {
        boolean isHwDisableinstall = this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_disable_installsource);
        int callingUid = Binder.getCallingUid();
        if (!isHwDisableinstall || (callingUid != System.AUTO_HIDE_NAVIGATIONBAR_TIMEOUT_DEFAULT && callingUid != 0)) {
            return false;
        }
        Log.d(TAG, "checkInstallPackageDisabled true ");
        return true;
    }

    public boolean isAllowedInstallPackage(String pkgName) {
        if (pkgName == null) {
            return true;
        }
        List packagelist = null;
        try {
            packagelist = this.mHwDPM.getHwFrameworkAdminList(ConstantValue.result_installpackage_whitelist);
        } catch (Exception e) {
            Log.e(TAG, "getInstallPackageWhiteList error : " + e.getMessage());
        }
        if (packagelist == null || packagelist.isEmpty() || packagelist.contains(pkgName)) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Missing block: B:9:0x0015, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isDisallowedUninstallPackage(String pkgName) {
        if (pkgName == null) {
            return false;
        }
        List<String> packagelist = null;
        try {
            packagelist = this.mHwDPM.getHwFrameworkAdminList(ConstantValue.result_disalloweduninstall_package_list);
        } catch (Exception e) {
            Log.e(TAG, "getDisallowedUninstallPackageList error : " + e.getMessage());
        }
        if (packagelist == null || packagelist.isEmpty() || !packagelist.contains(pkgName)) {
            return false;
        }
        Log.i(TAG, pkgName + " is in the notuninstallpackage_blacklist ");
        return true;
    }

    /* JADX WARNING: Missing block: B:9:0x0015, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isDisabledDeactivateMdmPackage(String pkgName) {
        if (pkgName == null) {
            return false;
        }
        List<String> packagelist = null;
        try {
            packagelist = this.mHwDPM.getHwFrameworkAdminList(ConstantValue.result_disabled_deactivate_Mdm_package_list);
        } catch (Exception e) {
            Log.e(TAG, "getDisabledDeactivateMdmPackageList error : " + e.getMessage());
        }
        if (packagelist == null || packagelist.isEmpty() || !packagelist.contains(pkgName)) {
            return false;
        }
        Log.i(TAG, pkgName + " is in the disabled deactivate Mdm package list ");
        return true;
    }

    private boolean isInstall(Intent intent) {
        String action = intent.getAction();
        String type = intent.getType();
        boolean story = false;
        if ("android.intent.action.INSTALL_PACKAGE".equals(action)) {
            story = true;
        }
        if ("application/vnd.android.package-archive".equals(type)) {
            return true;
        }
        return story;
    }

    public List<String> getNetworkAccessWhitelist() {
        List<String> list = null;
        try {
            return this.mHwDPM.getHwFrameworkAdminList(ConstantValue.result_networkAccess_whitelist);
        } catch (Exception e) {
            Log.e(TAG, "getNetworkAccessWhitelist error : " + e.getMessage());
            return list;
        }
    }

    public boolean isDisallowedInstallPackage(String pkgName) {
        if (pkgName == null) {
            return false;
        }
        List packagelist = null;
        try {
            packagelist = this.mHwDPM.getHwFrameworkAdminList(ConstantValue.result_installpackage_blacklist);
        } catch (Exception e) {
            Log.e(TAG, "getInstallPackageBlackList error : " + e.getMessage());
        }
        if (packagelist == null || !packagelist.contains(pkgName)) {
            return false;
        }
        return true;
    }

    public boolean isApplicationDisabled(Intent intent) {
        Object pkgName = null;
        if (intent.getComponent() != null) {
            pkgName = intent.getComponent().getPackageName();
        }
        if (pkgName == null) {
            return false;
        }
        String settingPackageName = "com.android.settings";
        String SETTINGS_FALLBACK_ACTIVITY_NAME = "com.android.settings.FallbackHome";
        try {
            List<String> packagelist = this.mHwDPM.getHwFrameworkAdminList(ConstantValue.result_disable_application_list);
            if (!(packagelist == null || packagelist.size() == 0 || !packagelist.contains(pkgName))) {
                if (settingPackageName.equals(pkgName) && intent.getComponent() != null && intent.getComponent().toString().contains(SETTINGS_FALLBACK_ACTIVITY_NAME)) {
                    return false;
                }
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "getIsDisableApplication error : " + e.getMessage());
        }
        return false;
    }

    public boolean isScreenCaptureDisabled() {
        return this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_disable_capture_screen);
    }

    public boolean isWritingSDCardDisabled() {
        String DISABLE_SDWRITING_POLICY = "disable-sdwriting";
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "disable-sdwriting", null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean isNotificationDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, DISABLE_NOTIFICATION_POLICY, null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean isMicrophoneDisabled() {
        String MICROPHONE_DISABLE = "disable-microphone";
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "disable-microphone", null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean isHeadphoneDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "disable-headphone", null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean isSendNotificationDisabled() {
        String DISABLE_SEND_NOTIFICATION = "disable-send-notification";
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "disable-send-notification", null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public String getSingleApp() {
        String POLICY_SINGLE_APP = "policy-single-app";
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "policy-single-app", null);
        if (bundle != null) {
            return bundle.getString("value");
        }
        return null;
    }

    public boolean isChangeWallpaperDisabled() {
        String DISABLE_CHANGE_WALLPAPER = "disable-change-wallpaper";
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "disable-change-wallpaper", null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean isScreenOffDisabled() {
        String POLICY_FORBIDDEN_SCREEN_OFF = "settings_policy_forbidden_screen_off";
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "settings_policy_forbidden_screen_off", null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean isFingerprintAuthenticationDisabled() {
        String DISABLE_FINGERPRINT_AUTHENTICATION = "disable-fingerprint-authentication";
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "disable-fingerprint-authentication", null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean isPowerDisabled() {
        String DISABLE_POWER_SHUTDOWN = "disable-power-shutdown";
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "disable-power-shutdown", null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean isShutdownMenuDisabled() {
        String DISABLE_SHUTDOWNMENU = "disable-shutdownmenu";
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "disable-shutdownmenu", null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean isVolumeAdjustDisabled() {
        String DISABLE_VOLUME = "disable-volume";
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "disable-volume", null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean isLocationServiceDisabled() {
        String LOCATION_SERVICES = "settings_policy_forbidden_location_service";
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "settings_policy_forbidden_location_service", null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean isLocationModeDisabled() {
        String LOCATION_MODEL = "settings_policy_forbidden_location_mode";
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "settings_policy_forbidden_location_mode", null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean isRoamingSyncDisabled() {
        String DISABLE_SYNC = "disable-sync";
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "disable-sync", null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean isPassiveProviderDisabled() {
        String DISALLOW_PASSIVE_LOCATION = "passive_location_disallow_item";
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "passive_location_disallow_item", null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        Log.i(TAG, "isPassiveProviderDisabled getCachedPolicyForFwk get null data");
        return false;
    }

    public boolean isForceEnableBT() {
        Bundle bundle = new HwDevicePolicyManagerEx().getPolicy(null, "force-enable-BT");
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean isForceEnableWifi() {
        Bundle bundle = new HwDevicePolicyManagerEx().getPolicy(null, "force-enable-wifi");
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean isNavigationBarDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, DISABLE_NAVIGATIONBAR_POLICY, null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean isAccessibilityServicesWhiteList(String pkg) {
        if (!TextUtils.isEmpty(pkg) && isAccessibilityServicesMDMWhiteList(pkg)) {
            return true;
        }
        return false;
    }

    private boolean isAccessibilityServicesMDMWhiteList(String pkg) {
        String POLICY_ACCESSIBILITY_SERVICES_WHITE_LIST = "accessibility_services_white_list";
        String STATE_VALUE = "value";
        ArrayList<String> servicesList = null;
        try {
            servicesList = this.mHwDPM.getCachedPolicyForFwk(null, "accessibility_services_white_list", null).getStringArrayList("value");
        } catch (Exception e) {
            Log.e(TAG, "isAccessibilityServicesWhiteList getAllowaccessibility services error : " + e.getMessage());
        }
        if (servicesList == null || servicesList.isEmpty()) {
            Log.w(TAG, "isAccessibilityServicesWhiteList servicesList is null or empty.");
            return false;
        }
        ArrayList<ComponentName> componeNameWhiteList = transformStringToComponentName(servicesList);
        int size = componeNameWhiteList.size();
        for (int i = 0; i < size; i++) {
            if (((ComponentName) componeNameWhiteList.get(i)).getPackageName().equals(pkg)) {
                Log.w(TAG, "isAccessibilityServicesWhiteList pkg=" + pkg + " in white list block!");
                return true;
            }
        }
        return false;
    }

    private ArrayList<ComponentName> transformStringToComponentName(ArrayList<String> list) {
        ArrayList<ComponentName> componentNames = new ArrayList();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            componentNames.add(ComponentName.unflattenFromString((String) list.get(i)));
        }
        return componentNames;
    }
}
