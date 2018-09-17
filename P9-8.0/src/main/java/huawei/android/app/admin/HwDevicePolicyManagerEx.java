package huawei.android.app.admin;

import android.content.ComponentName;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.UserHandle;
import java.util.List;
import java.util.Map;

public class HwDevicePolicyManagerEx {
    private static final String TAG = "HwDevicePolicyManagerEx";
    private TransactionSponsor mSponsor = new TransactionSponsor();

    public void setWifiDisabled(ComponentName who, boolean disabled) {
        this.mSponsor.transactTo_setFunctionDisabled(ConstantValue.transaction_setWifiDisabled, "setWifiDisabled", who, disabled, UserHandle.myUserId());
    }

    public boolean isWifiDisabled(ComponentName who) {
        return this.mSponsor.transactTo_isFunctionDisabled(ConstantValue.transaction_isWifiDisabled, "isWifiDisabled", who, 0);
    }

    public void setBluetoothDisabled(ComponentName who, boolean disabled) {
        this.mSponsor.transactTo_setFunctionDisabled(1024, "setBluetoothDisabled", who, disabled, UserHandle.myUserId());
    }

    public boolean isBluetoothDisabled(ComponentName who) {
        return this.mSponsor.transactTo_isFunctionDisabled(ConstantValue.transaction_isBluetoothDisabled, "isBluetoothDisabled", who, 0);
    }

    public void setWifiApDisabled(ComponentName admin, boolean disabled) {
        this.mSponsor.transactTo_setFunctionDisabled(ConstantValue.transaction_setWifiApDisabled, "setWifiApDisabled", admin, disabled, UserHandle.myUserId());
    }

    public boolean isWifiApDisabled(ComponentName admin) {
        return this.mSponsor.transactTo_isFunctionDisabled(ConstantValue.transaction_isWifiApDisabled, "isWifiApDisabled", admin, 0);
    }

    public void setBootLoaderDisabled(ComponentName admin, boolean disabled) {
        this.mSponsor.transactTo_setFunctionDisabled(ConstantValue.transaction_setBootLoaderDisabled, "setBootLoaderDisabled", admin, disabled, UserHandle.myUserId());
    }

    public boolean isBootLoaderDisabled(ComponentName admin) {
        return this.mSponsor.transactTo_isFunctionDisabled(ConstantValue.transaction_isBootLoaderDisabled, "isBootLoaderDisabled", admin, 0);
    }

    public void setUSBDataDisabled(ComponentName admin, boolean disabled) {
        this.mSponsor.transactTo_setFunctionDisabled(ConstantValue.transaction_setUSBDataDisabled, "setUSBDataDisabled", admin, disabled, UserHandle.myUserId());
    }

    public boolean isUSBDataDisabled(ComponentName admin) {
        return this.mSponsor.transactTo_isFunctionDisabled(ConstantValue.transaction_isUSBDataDisabled, "isUSBDataDisabled", admin, 0);
    }

    public void setExternalStorageDisabled(ComponentName admin, boolean disabled) {
        this.mSponsor.transactTo_setFunctionDisabled(ConstantValue.transaction_setExternalStorageDisabled, "setExternalStorageDisabled", admin, disabled, UserHandle.myUserId());
    }

    public boolean isExternalStorageDisabled(ComponentName admin) {
        return this.mSponsor.transactTo_isFunctionDisabled(ConstantValue.transaction_isExternalStorageDisabled, "isExternalStorageDisabled", admin, 0);
    }

    public void setNFCDisabled(ComponentName admin, boolean disabled) {
        this.mSponsor.transactTo_setFunctionDisabled(ConstantValue.transaction_setNFCDisabled, "setNFCDisabled", admin, disabled, UserHandle.myUserId());
    }

    public boolean isNFCDisabled(ComponentName admin) {
        return this.mSponsor.transactTo_isFunctionDisabled(ConstantValue.transaction_isNFCDisabled, "isNFCDisabled", admin, 0);
    }

    public void setDataConnectivityDisabled(ComponentName admin, boolean disabled) {
        this.mSponsor.transactTo_setFunctionDisabled(ConstantValue.transaction_setDataConnectivityDisabled, "setDataConnectivityDisabled", admin, disabled, UserHandle.myUserId());
    }

    public boolean isDataConnectivityDisabled(ComponentName admin) {
        return this.mSponsor.transactTo_isFunctionDisabled(ConstantValue.transaction_isDataConnectivityDisabled, "isDataConnectivityDisabled", admin, 0);
    }

    public void setVoiceDisabled(ComponentName admin, boolean disabled) {
        this.mSponsor.transactTo_setFunctionDisabled(ConstantValue.transaction_setVoiceDisabled, "setSMSDisabled", admin, disabled, UserHandle.myUserId());
    }

    public boolean isVoiceDisabled(ComponentName admin) {
        return this.mSponsor.transactTo_isFunctionDisabled(ConstantValue.transaction_isVoiceDisabled, "isVoiceDisabled", admin, 0);
    }

    public void setSMSDisabled(ComponentName admin, boolean disabled) {
        this.mSponsor.transactTo_setFunctionDisabled(ConstantValue.transaction_setSMSDisabled, "setSMSDisabled", admin, disabled, UserHandle.myUserId());
    }

    public boolean isSMSDisabled(ComponentName admin) {
        return this.mSponsor.transactTo_isFunctionDisabled(ConstantValue.transaction_isSMSDisabled, "isSMSDisabled", admin, 0);
    }

    public void hangupCalling(ComponentName admin) {
        this.mSponsor.transactTo_execCommand(ConstantValue.transaction_hangupCalling, "hangupCalling", admin, UserHandle.myUserId());
    }

    public void installPackage(ComponentName admin, String packagePath) {
        this.mSponsor.transactTo_execCommand((int) ConstantValue.transaction_installPackage, "installPackage", admin, packagePath, UserHandle.myUserId());
    }

    public void uninstallPackage(ComponentName admin, String packageName, boolean keepData) {
        this.mSponsor.transactTo_uninstallPackage(ConstantValue.transaction_uninstallPackage, "uninstallPackage", admin, packageName, keepData, UserHandle.myUserId());
    }

    public void clearPackageData(ComponentName admin, String packageName) {
        this.mSponsor.transactTo_execCommand((int) ConstantValue.transaction_clearPackageData, "clearPackageData", admin, packageName, UserHandle.myUserId());
    }

    public void enableInstallPackage(ComponentName admin) {
        this.mSponsor.transactTo_execCommand(ConstantValue.transaction_enableInstallPackage, "enableInstallPackage", admin, UserHandle.myUserId());
    }

    public void disableInstallSource(ComponentName admin, List<String> whitelist) {
        this.mSponsor.transactTo_execCommand((int) ConstantValue.transaction_disableInstallSource, "disableInstallPackage", admin, (List) whitelist, UserHandle.myUserId());
    }

    public boolean isInstallSourceDisabled(ComponentName admin) {
        return this.mSponsor.transactTo_isFunctionDisabled(ConstantValue.transaction_isInstallSourceDisabled, "isInstallSourceDisabled", admin, 0);
    }

    public List<String> getInstallPackageSourceWhiteList(ComponentName admin) {
        return this.mSponsor.transactTo_getListFunction(ConstantValue.transaction_getInstallPackageSourceWhiteList, "getInstallPackageSourceWhiteList", admin, UserHandle.myUserId());
    }

    public void addPersistentApp(ComponentName admin, List<String> packageNames) {
        this.mSponsor.transactTo_execCommand((int) ConstantValue.transaction_addPersistentApp, "addPersistentApp", admin, (List) packageNames, UserHandle.myUserId());
    }

    public void removePersistentApp(ComponentName admin, List<String> packageNames) {
        this.mSponsor.transactTo_execCommand((int) ConstantValue.transaction_removePersistentApp, "removePersistentApp", admin, (List) packageNames, UserHandle.myUserId());
    }

    public List<String> getPersistentApp(ComponentName admin) {
        return this.mSponsor.transactTo_getListFunction(ConstantValue.transaction_getPersistentApp, "getPersistentApp", admin, 0);
    }

    public void addDisallowedRunningApp(ComponentName admin, List<String> packageNames) {
        this.mSponsor.transactTo_execCommand((int) ConstantValue.transaction_addDisallowedRunningApp, "addDisallowedRunningApp", admin, (List) packageNames, UserHandle.myUserId());
    }

    public void removeDisallowedRunningApp(ComponentName admin, List<String> packageNames) {
        this.mSponsor.transactTo_execCommand((int) ConstantValue.transaction_removeDisallowedRunningApp, "removeDisallowedRunningApp", admin, (List) packageNames, UserHandle.myUserId());
    }

    public List<String> getDisallowedRunningApp(ComponentName admin) {
        return this.mSponsor.transactTo_getListFunction(ConstantValue.transaction_getDisallowedRunningApp, "getDisallowedRunningApp", admin, 0);
    }

    public void addInstallPackageWhiteList(ComponentName admin, List<String> packageNames) {
        this.mSponsor.transactTo_execCommand((int) ConstantValue.transaction_addInstallPackageWhiteList, "add_install_packages", admin, (List) packageNames, UserHandle.myUserId());
    }

    public void removeInstallPackageWhiteList(ComponentName admin, List<String> packageNames) {
        this.mSponsor.transactTo_execCommand((int) ConstantValue.transaction_removeInstallPackageWhiteList, "remove_install_packages", admin, (List) packageNames, UserHandle.myUserId());
    }

    public List<String> getInstallPackageWhiteList(ComponentName admin) {
        return this.mSponsor.transactTo_getListFunction(ConstantValue.transaction_getInstallPackageWhiteList, "get_install_packagewhitelist", admin, 0);
    }

    public void addDisallowedUninstallPackages(ComponentName admin, List<String> packageNames) {
        this.mSponsor.transactTo_execCommand((int) ConstantValue.transaction_addDisallowedUninstallPackages, "add_notuninstall_packages", admin, (List) packageNames, UserHandle.myUserId());
    }

    public void removeDisallowedUninstallPackages(ComponentName admin, List<String> packageNames) {
        this.mSponsor.transactTo_execCommand((int) ConstantValue.transaction_removeDisallowedUninstallPackages, "remove_notuninstall_packages", admin, (List) packageNames, UserHandle.myUserId());
    }

    public List<String> getDisallowedUninstallPackageList(ComponentName admin) {
        return this.mSponsor.transactTo_getListFunction(ConstantValue.transaction_getDisallowedUninstallPackageList, "get_install_packagewhitelist", admin, 0);
    }

    public void addDisabledDeactivateMdmPackages(ComponentName admin, List<String> packageNames) {
        this.mSponsor.transactTo_execCommand((int) ConstantValue.transaction_addDisabledDeactivateMdmPackages, "add_disabled_deactive_Mdm_packages", admin, (List) packageNames, UserHandle.myUserId());
    }

    public void removeDisabledDeactivateMdmPackages(ComponentName admin, List<String> packageNames) {
        this.mSponsor.transactTo_execCommand((int) ConstantValue.transaction_removeDisabledDeactivateMdmPackages, "remove_disabled_deactive_Mdm_packages", admin, (List) packageNames, UserHandle.myUserId());
    }

    public List<String> getDisabledDeactivateMdmPackageList(ComponentName admin) {
        return this.mSponsor.transactTo_getListFunction(ConstantValue.transaction_getDisabledDeactivateMdmPackageList, "get_disabled_deactive_Mdm_packages", admin, 0);
    }

    public void killApplicationProcess(ComponentName admin, String packageName) {
        this.mSponsor.transactTo_execCommand((int) ConstantValue.transaction_killApplicationProcess, "killApplicationProcess", admin, packageName, UserHandle.myUserId());
    }

    public void shutdownDevice(ComponentName admin) {
        this.mSponsor.transactTo_execCommand(ConstantValue.transaction_shutdownDevice, "shutdownDevice", admin, UserHandle.myUserId());
    }

    public void rebootDevice(ComponentName admin) {
        this.mSponsor.transactTo_execCommand(ConstantValue.transaction_rebootDevice, "rebootDevice", admin, UserHandle.myUserId());
    }

    public void setStatusBarExpandPanelDisabled(ComponentName who, boolean disabled) {
        this.mSponsor.transactTo_setFunctionDisabled(ConstantValue.transaction_setStatusBarExpandPanelDisabled, "setStatusBarExpandPanelDisabled", who, disabled, UserHandle.myUserId());
    }

    public boolean isStatusBarExpandPanelDisabled(ComponentName who) {
        return this.mSponsor.transactTo_isFunctionDisabled(ConstantValue.transaction_isStatusBarExpandPanelDisabled, "isStatusBarExpandPanelDisabled", who, 0);
    }

    public void configExchangeMailProvider(ComponentName admin, Bundle para) {
        this.mSponsor.transactTo_configExchangeMail(ConstantValue.transaction_configExchangeMail, "configExchangeMailProvider", admin, para, UserHandle.myUserId());
    }

    public Bundle getMailProviderForDomain(ComponentName admin, String domain) {
        return this.mSponsor.transactTo_getMailProviderForDomain(ConstantValue.transaction_getMailProviderForDomain, "getMailProviderForDomain", admin, domain, 0);
    }

    public boolean isRooted(ComponentName admin) {
        return this.mSponsor.transactTo_isFunctionDisabled(ConstantValue.transaction_isRooted, "getRootState", admin, 0);
    }

    boolean isHwFrameworkAdminAllowed(int code) {
        return this.mSponsor.transactTo_isFunctionDisabled(code, "getHwAdminCachedValue", null, 0);
    }

    List<String> getHwFrameworkAdminList(int code) {
        return this.mSponsor.transactTo_getListFunction(code, "getHwAdminCachedList", null, 0);
    }

    public void setSafeModeDisabled(ComponentName who, boolean disabled) {
        this.mSponsor.transactTo_setFunctionDisabled(ConstantValue.transaction_setSafeModeDisabled, "setSafeModeDisabled", who, disabled, UserHandle.myUserId());
    }

    public boolean isSafeModeDisabled(ComponentName who) {
        return this.mSponsor.transactTo_isFunctionDisabled(ConstantValue.transaction_isSafeModeDisabled, "isSafeModeDisabled", who, 0);
    }

    public void setAdbDisabled(ComponentName who, boolean disabled) {
        this.mSponsor.transactTo_setFunctionDisabled(ConstantValue.transaction_setAdbDisabled, "setAdbDisabled", who, disabled, UserHandle.myUserId());
    }

    public boolean isAdbDisabled(ComponentName who) {
        return this.mSponsor.transactTo_isFunctionDisabled(ConstantValue.transaction_isAdbDisabled, "isAdbDisabled", who, 0);
    }

    public void setUSBOtgDisabled(ComponentName who, boolean disabled) {
        this.mSponsor.transactTo_setFunctionDisabled(ConstantValue.transaction_setUSBOtgDisabled, "setUSBOtgDisabled", who, disabled, UserHandle.myUserId());
    }

    public boolean isUSBOtgDisabled(ComponentName who) {
        return this.mSponsor.transactTo_isFunctionDisabled(ConstantValue.transaction_isUSBOtgDisabled, "isUSBOtgDisabled", who, 0);
    }

    public void setGPSDisabled(ComponentName who, boolean disabled) {
        this.mSponsor.transactTo_setFunctionDisabled(ConstantValue.transaction_setGPSDisabled, "setGPSDisabled", who, disabled, UserHandle.myUserId());
    }

    public boolean isGPSDisabled(ComponentName who) {
        return this.mSponsor.transactTo_isFunctionDisabled(ConstantValue.transaction_isGPSDisabled, "isGPSDisabled", who, 0);
    }

    public void turnOnGPS(ComponentName who, boolean on) {
        this.mSponsor.transactTo_setFunctionDisabled(ConstantValue.transaction_turnOnGPS, "turnOnGPS", who, on, UserHandle.myUserId());
    }

    public boolean isGPSTurnOn(ComponentName who) {
        return this.mSponsor.transactTo_isFunctionDisabled(ConstantValue.transaction_isGPSTurnOn, "isGPSTurnOn", who, 0);
    }

    public void setTaskButtonDisabled(ComponentName who, boolean disabled) {
        this.mSponsor.transactTo_setFunctionDisabled(ConstantValue.transaction_setTaskButtonDisabled, "setTaskButtonDisabled", who, disabled, UserHandle.myUserId());
    }

    public boolean isTaskButtonDisabled(ComponentName who) {
        return this.mSponsor.transactTo_isFunctionDisabled(ConstantValue.transaction_isTaskButtonDisabled, "isTaskButtonDisabled", who, 0);
    }

    public void setHomeButtonDisabled(ComponentName who, boolean disabled) {
        this.mSponsor.transactTo_setFunctionDisabled(ConstantValue.transaction_setHomeButtonDisabled, "setHomeButtonDisabled", who, disabled, UserHandle.myUserId());
    }

    public boolean isHomeButtonDisabled(ComponentName who) {
        return this.mSponsor.transactTo_isFunctionDisabled(ConstantValue.transaction_isHomeButtonDisabled, "isHomeButtonDisabled", who, 0);
    }

    public void setBackButtonDisabled(ComponentName who, boolean disabled) {
        this.mSponsor.transactTo_setFunctionDisabled(ConstantValue.transaction_setBackButtonDisabled, "setBackButtonDisabled", who, disabled, UserHandle.myUserId());
    }

    public boolean isBackButtonDisabled(ComponentName who) {
        return this.mSponsor.transactTo_isFunctionDisabled(ConstantValue.transaction_isBackButtonDisabled, "isBackButtonDisabled", who, 0);
    }

    public void setSysTime(ComponentName admin, long millis) {
        this.mSponsor.transactTo_execCommand((int) ConstantValue.transaction_setSysTime, "setSysTime", admin, String.valueOf(millis), UserHandle.myUserId());
    }

    public void setDefaultLauncher(ComponentName admin, String packageName, String className) {
        this.mSponsor.transactTo_setDefaultLauncher(ConstantValue.transaction_setDefaultLauncher, "setDefaultLauncher", admin, packageName, className, 0);
    }

    public void clearDefaultLauncher(ComponentName admin) {
        this.mSponsor.transactTo_execCommand(ConstantValue.transaction_clearDefaultLauncher, "clearDefaultLauncher", admin, 0);
    }

    public Bitmap captureScreen(ComponentName admin) {
        return this.mSponsor.transactTo_captureScreen(ConstantValue.transaction_captureScreen, "captureScreen", admin, 0);
    }

    public void setCustomSettingsMenu(ComponentName admin, List<String> menusToDelete) {
        this.mSponsor.transactTo_execCommand((int) ConstantValue.transaction_setCustomSettingsMenu, "setCustomSettingsMenu", admin, (List) menusToDelete, UserHandle.myUserId());
    }

    public void addApn(ComponentName admin, Map<String, String> apninfo) {
        this.mSponsor.transactTo_execCommand((int) ConstantValue.transaction_addApn, "addApn", admin, (Map) apninfo, 0);
    }

    public void deleteApn(ComponentName admin, String id) {
        this.mSponsor.transactTo_execCommand((int) ConstantValue.transaction_deleteApn, "deleteApn", admin, id, 0);
    }

    public void updateApn(ComponentName admin, Map<String, String> apninfo, String id) {
        this.mSponsor.transactTo_execCommand(ConstantValue.transaction_updateApn, "updateApn", admin, apninfo, id, 0);
    }

    public Map<String, String> getApnInfo(ComponentName admin, String id) {
        return this.mSponsor.transactTo_getApnInfo(ConstantValue.transaction_getApnInfo, "getApnInfo", admin, id, 0);
    }

    public List<String> queryApn(ComponentName admin, Map<String, String> apninfo) {
        return this.mSponsor.transactTo_queryApn(ConstantValue.transaction_queryApn, "queryApn", admin, apninfo, 0);
    }

    public void setPreferApn(ComponentName admin, String id) {
        this.mSponsor.transactTo_execCommand((int) ConstantValue.transaction_setPreferApn, "setPreferApn", admin, id, 0);
    }

    public void addNetworkAccessWhitelist(ComponentName admin, List<String> addrList) {
        this.mSponsor.transactTo_execCommand((int) ConstantValue.transaction_addNetworkAccessWhitelist, "addNetworkAccessWhitelist", admin, (List) addrList, 0);
    }

    public void removeNetworkAccessWhitelist(ComponentName admin, List<String> addrList) {
        this.mSponsor.transactTo_execCommand((int) ConstantValue.transaction_removeNetworkAccessWhitelist, "removeNetworkAccessWhitelist", admin, (List) addrList, 0);
    }

    public List<String> getNetworkAccessWhitelist(ComponentName admin) {
        return this.mSponsor.transactTo_getListFunction(ConstantValue.transaction_getNetworkAccessWhitelist, "getNetworkAccessWhitelist", admin, 0);
    }

    public int getSDCardEncryptionStatus() {
        return this.mSponsor.transactTo_getSDCardEncryptionStatus(ConstantValue.transaction_getSDCardEncryptionStatus, "getSDCardEncryptionStatus", 0);
    }

    public void setSDCardDecryptionDisabled(ComponentName who, boolean disabled) {
        this.mSponsor.transactTo_setFunctionDisabled(ConstantValue.transaction_setSDCardDecryptionDisabled, "setSDCardDecryptionDisabled", who, disabled, UserHandle.myUserId());
    }

    public boolean isSDCardDecryptionDisabled(ComponentName who) {
        return this.mSponsor.transactTo_isFunctionDisabled(ConstantValue.transaction_isSDCardDecryptionDisabled, "isSDCardDecryptionDisabled", who, UserHandle.myUserId());
    }

    public boolean setPolicy(ComponentName who, String policyName, Bundle policyData) {
        return this.mSponsor.transactTo_setPolicy(ConstantValue.transaction_setPolicy, policyName, "setPolicy", who, UserHandle.myUserId(), policyData, 0);
    }

    public Bundle getPolicy(ComponentName who, String policyName) {
        return this.mSponsor.transactTo_getPolicy(ConstantValue.transaction_getPolicy, policyName, null, "getPolicy", who, UserHandle.myUserId(), 0);
    }

    public boolean removePolicy(ComponentName who, String policyName, Bundle policyData) {
        return this.mSponsor.transactTo_setPolicy(ConstantValue.transaction_removePolicy, policyName, "removePolicy", who, UserHandle.myUserId(), policyData, 0);
    }

    public boolean setCustomPolicy(ComponentName who, String policyName, Bundle policyData) {
        return this.mSponsor.transactTo_setPolicy(ConstantValue.transaction_setPolicy, policyName, "setPolicy", who, UserHandle.myUserId(), policyData, 1);
    }

    public Bundle getCustomPolicy(ComponentName who, String policyName, Bundle keyWords) {
        return this.mSponsor.transactTo_getPolicy(ConstantValue.transaction_getPolicy, policyName, keyWords, "getPolicy", who, UserHandle.myUserId(), 1);
    }

    public boolean removeCustomPolicy(ComponentName who, String policyName, Bundle policyData) {
        return this.mSponsor.transactTo_setPolicy(ConstantValue.transaction_removePolicy, policyName, "removePolicy", who, UserHandle.myUserId(), policyData, 1);
    }

    public Bundle getCachedPolicyForFwk(ComponentName who, String policyName, Bundle keyWords) {
        return this.mSponsor.transactTo_getPolicy(ConstantValue.transaction_getPolicy, policyName, keyWords, "getPolicy", who, UserHandle.myUserId(), 2);
    }

    public boolean hasHwPolicy() {
        return this.mSponsor.transactTo_hasHwPolicy(ConstantValue.transaction_hasHwPolicy, "hasHwPolicy", UserHandle.myUserId());
    }

    public void setSilentActiveAdmin(ComponentName admin) {
        this.mSponsor.transactTo_execCommand(ConstantValue.transaction_setSilentActiveAdmin, "setSilentActiveAdmin", admin, UserHandle.myUserId());
    }

    public void setAccountDisabled(ComponentName who, String accountType, boolean disabled) {
        this.mSponsor.transactTo_setAccountDisabled(ConstantValue.transaction_setAccountDisabled, "setAccountDisabled", who, accountType, disabled, UserHandle.myUserId());
    }

    public boolean isAccountDisabled(ComponentName who, String accountType) {
        return this.mSponsor.transactTo_isAccountDisabled(ConstantValue.transaction_isAccountDisabled, "isAccountDisabled", who, accountType, UserHandle.myUserId());
    }

    public boolean formatSDCard(ComponentName who, String diskId) {
        return this.mSponsor.transactTo_formatSDCard(ConstantValue.transaction_formatSDCard, "formatSDCard", who, diskId, UserHandle.myUserId());
    }

    public boolean installCertificateWithType(ComponentName who, int type, byte[] certBuffer, String name, String password, int keystore, boolean requestAccess) {
        return this.mSponsor.transactTo_installCertificateWithType(ConstantValue.transaction_installCertificateWithType, "installCertificateWithType", who, type, certBuffer, name, password, keystore, requestAccess, UserHandle.myUserId());
    }

    public void setDeviceOwnerApp(ComponentName admin, String ownerName) {
        this.mSponsor.transactTo_execCommand((int) ConstantValue.transaction_setDeviceOwnerApp, "setDeviceOwnerApp", admin, ownerName, UserHandle.myUserId());
    }

    public void clearDeviceOwnerApp() {
        this.mSponsor.transactTo_execCommand(ConstantValue.transaction_clearDeviceOwnerApp, "clearDeviceOwnerApp", null, UserHandle.myUserId());
    }

    public void turnOnMobiledata(ComponentName who, boolean on) {
        this.mSponsor.transactTo_setFunctionDisabled(ConstantValue.transaction_turnOnMobiledata, "turnOnMobiledata", who, on, 0);
    }

    public boolean setCarrierLockScreenPassword(ComponentName who, String password, String phoneNumber) {
        return this.mSponsor.transactTo_setCarrierLockScreenPassword(ConstantValue.transaction_setCarrierLockScreenPassword, "setCarrierLockScreenPassword", who, password, phoneNumber, UserHandle.myUserId());
    }

    public boolean clearCarrierLockScreenPassword(ComponentName who, String password) {
        return this.mSponsor.transactTo_clearCarrierLockScreenPassword(ConstantValue.transaction_clearCarrierLockScreenPassword, "clearExtendLockScreenPassword", who, password, UserHandle.myUserId());
    }
}
