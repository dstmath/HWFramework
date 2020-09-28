package huawei.android.app.admin;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SyncAdapterType;
import android.hdm.HwDeviceManager;
import android.os.Binder;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HwDeviceManagerImpl implements HwDeviceManager.IHwDeviceManager {
    private static final String DISABLE_NAVIGATIONBAR_POLICY = "disable-navigationbar";
    private static final String DISABLE_NOTIFICATION_POLICY = "disable-notification";
    private static final String TAG = "HwDevicePolicyManagerImpl";
    private static final String UNAVAILABLE_SSID_LIST = "unavailable-ssid-list";
    HwDevicePolicyManagerEx mHwDPM;

    public HwDeviceManagerImpl() {
        this.mHwDPM = null;
        this.mHwDPM = new HwDevicePolicyManagerEx();
    }

    public boolean isWifiDisabled() {
        return this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_disable_wifi);
    }

    public boolean isNetworkLocationDisabled() {
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
        return "com.google".equals(accountType);
    }

    public List<String> getSuperWhiteListApp() {
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

    public boolean isVisibleGoogleAccountSync(String authority) {
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

    public boolean isGoogleAccountAutoSyncDisabled(String accountType) {
        return isGoogleAccount(accountType) && this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_disable_googleAccount_autosync);
    }

    public boolean isIgnoredFrequentRelaunchApp(String pkgName) {
        List<String> packagelist;
        if (!TextUtils.isEmpty(pkgName) && (packagelist = this.mHwDPM.getHwFrameworkAdminList(ConstantValue.result_ignore_frequent_relaunch_app_list)) != null && packagelist.contains(pkgName)) {
            return true;
        }
        return false;
    }

    public boolean isPersistentApp(String pkgName) {
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

    public boolean isDisallowedRunningApp(String pkgName) {
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

    public boolean isIntentFromAllowedInstallSource(Intent intent) {
        List<String> appMarketPkgNames;
        if (intent == null || !isInstall(intent) || !this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_disable_installsource) || (appMarketPkgNames = this.mHwDPM.getHwFrameworkAdminList(ConstantValue.result_installsource_whitelist)) == null || appMarketPkgNames.isEmpty() || appMarketPkgNames.contains(intent.getStringExtra("caller_package"))) {
            return true;
        }
        return false;
    }

    public boolean isAdbOrSDCardInstallRestricted() {
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

    public boolean isAllowedInstallPackage(String pkgName) {
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

    public boolean isDisallowedUninstallPackage(String pkgName) {
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

    public boolean isDisabledDeactivateMdmPackage(String pkgName) {
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

    public List<String> getNetworkAccessWhitelist() {
        try {
            return this.mHwDPM.getHwFrameworkAdminList(ConstantValue.result_networkAccess_whitelist);
        } catch (Exception e) {
            Log.e(TAG, "getNetworkAccessWhitelist catch Exception");
            return null;
        }
    }

    public List<String> getNetworkAccessList(String policyName) {
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

    public boolean isDisallowedInstallPackage(String pkgName) {
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

    public boolean isApplicationDisabled(Intent intent) {
        String pkgName = null;
        if (intent.getComponent() != null) {
            pkgName = intent.getComponent().getPackageName();
        }
        if (pkgName == null) {
            return false;
        }
        try {
            List<String> packagelist = this.mHwDPM.getHwFrameworkAdminList(ConstantValue.result_disable_application_list);
            if (!(packagelist == null || packagelist.size() == 0 || !packagelist.contains(pkgName))) {
                if (!"com.android.settings".equals(pkgName) || intent.getComponent() == null || !intent.getComponent().toString().contains("com.android.settings.FallbackHome")) {
                    return true;
                }
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "isApplicationDisabled catch Exception");
        }
        return false;
    }

    public boolean isScreenCaptureDisabled() {
        return this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_disable_capture_screen);
    }

    public boolean isWritingSDCardDisabled() {
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

    public boolean isNavigationBarDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, DISABLE_NAVIGATIONBAR_POLICY, null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean isSendNotificationDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "disable-send-notification", null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public String getSingleApp() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "policy-single-app", null);
        if (bundle != null) {
            return bundle.getString("value");
        }
        return null;
    }

    public boolean isChangeWallpaperDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "disable-change-wallpaper", null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean isScreenOffDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "settings_policy_forbidden_screen_off", null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean isFingerprintAuthenticationDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "disable-fingerprint-authentication", null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean isPowerDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "disable-power-shutdown", null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean isShutdownMenuDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "disable-shutdownmenu", null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean isVolumeAdjustDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "disable-volume", null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean isLocationServiceDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "settings_policy_forbidden_location_service", null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean isLocationModeDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "settings_policy_forbidden_location_mode", null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean isRoamingSyncDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "disable-sync", null);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean isPassiveProviderDisabled() {
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

    public boolean isAccessibilityServicesWhiteList(String pkg) {
        if (!TextUtils.isEmpty(pkg) && isAccessibilityServicesMDMWhiteList(pkg)) {
            return true;
        }
        return false;
    }

    private boolean isAccessibilityServicesMDMWhiteList(String pkg) {
        ArrayList<String> servicesList = null;
        try {
            Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "accessibility_services_white_list", null);
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

    public boolean isFileShareDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "policy-file-share-disabled", null);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("value");
    }

    public boolean isAndroidAnimationDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "disabled-android-animation", null);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("value");
    }

    public boolean isMultiWindowDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "disable-multi-window", null);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("value");
    }

    public boolean isSleepByPowerButtonDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "disable-screen-turn-off", null);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("value");
    }

    public boolean isWifiUnavailableTipsDisabled(String ssid) {
        Bundle bundle;
        ArrayList<String> list;
        if (TextUtils.isEmpty(ssid) || ssid.length() <= 2 || (bundle = this.mHwDPM.getCachedPolicyForFwk(null, UNAVAILABLE_SSID_LIST, null)) == null || (list = bundle.getStringArrayList("value")) == null) {
            return false;
        }
        return list.contains(ssid.substring(1, ssid.length() - 1));
    }

    public boolean isStatusBarDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "disable_status_bar", null);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("value");
    }

    public List<String> getTaskLockAppList() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "task-lock-app-list", null);
        if (bundle != null) {
            return bundle.getStringArrayList("value");
        }
        return Collections.emptyList();
    }

    public boolean isMediaControlDisabled() {
        Bundle bundle = this.mHwDPM.getCachedPolicyForFwk(null, "device_control_set_media_control_disabled", null);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("value");
    }
}
