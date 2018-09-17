package huawei.android.app.admin;

import android.content.Intent;
import android.hdm.HwDeviceManager.IHwDeviceManager;
import android.os.Binder;
import android.util.Log;
import huawei.android.provider.HwSettings.System;
import java.util.List;

public class HwDeviceManagerImpl implements IHwDeviceManager {
    private static final String TAG = "HwDevicePolicyManagerImpl";
    HwDevicePolicyManagerEx mHwDPM;

    public HwDeviceManagerImpl() {
        this.mHwDPM = null;
        this.mHwDPM = new HwDevicePolicyManagerEx();
    }

    public boolean isWifiDisabled() {
        return this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_disable_wifi);
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isIntentFromAllowedInstallSource(Intent intent) {
        if (intent == null || !isInstall(intent) || !this.mHwDPM.isHwFrameworkAdminAllowed(ConstantValue.result_disable_installsource)) {
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

    /* JADX WARNING: inconsistent code. */
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

    /* JADX WARNING: inconsistent code. */
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
            list = this.mHwDPM.getHwFrameworkAdminList(ConstantValue.result_networkAccess_whitelist);
        } catch (Exception e) {
            Log.e(TAG, "getNetworkAccessWhitelist error : " + e.getMessage());
        }
        return list;
    }
}
