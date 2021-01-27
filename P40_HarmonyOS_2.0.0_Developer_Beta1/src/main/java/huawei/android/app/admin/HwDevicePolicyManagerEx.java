package huawei.android.app.admin;

import android.content.ComponentName;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.os.UserHandle;
import java.util.List;
import java.util.Map;

public class HwDevicePolicyManagerEx {
    private static final String TAG = "HwDevicePolicyManagerEx";
    private TransactionSponsor mSponsor = new TransactionSponsor();

    public void setWifiDisabled(ComponentName admin, boolean isDisabled) {
        this.mSponsor.transactToSetFunctionDisabled(ConstantValue.TRANSACTION_SET_WIFI_DISABLED, admin, isDisabled, UserHandle.myUserId());
    }

    public boolean isWifiDisabled(ComponentName admin) {
        return this.mSponsor.transactToIsFunctionDisabled(ConstantValue.TRANSACTION_IS_WIFI_DISABLED, admin, 0);
    }

    public void setBluetoothDisabled(ComponentName admin, boolean isDisabled) {
        this.mSponsor.transactToSetFunctionDisabled(ConstantValue.TRANSACTION_SET_BLUETOOTH_DISABLED, admin, isDisabled, UserHandle.myUserId());
    }

    public boolean isBluetoothDisabled(ComponentName admin) {
        return this.mSponsor.transactToIsFunctionDisabled(ConstantValue.TRANSACTION_IS_BLUETOOTH_DISABLED, admin, 0);
    }

    public void setWifiApDisabled(ComponentName admin, boolean isDisabled) {
        this.mSponsor.transactToSetFunctionDisabled(ConstantValue.TRANSACTION_SET_WIFI_AP_DISABLED, admin, isDisabled, UserHandle.myUserId());
    }

    public boolean isWifiApDisabled(ComponentName admin) {
        return this.mSponsor.transactToIsFunctionDisabled(ConstantValue.TRANSACTION_IS_WIFI_AP_DISABLED, admin, 0);
    }

    public void setBootLoaderDisabled(ComponentName admin, boolean isDisabled) {
        this.mSponsor.transactToSetFunctionDisabled(ConstantValue.TRANSACTION_SET_BOOTLOADER_DISABLED, admin, isDisabled, UserHandle.myUserId());
    }

    public boolean isBootLoaderDisabled(ComponentName admin) {
        return this.mSponsor.transactToIsFunctionDisabled(ConstantValue.TRANSACTION_IS_BOOTLOADER_DISABLED, admin, 0);
    }

    public void setUsbDataDisabled(ComponentName admin, boolean isDisabled) {
        this.mSponsor.transactToSetFunctionDisabled(ConstantValue.TRANSACTION_SET_USB_DATA_DISABLED, admin, isDisabled, UserHandle.myUserId());
    }

    public boolean isUsbDataDisabled(ComponentName admin) {
        return this.mSponsor.transactToIsFunctionDisabled(ConstantValue.TRANSACTION_IS_USB_DATA_DISABLED, admin, 0);
    }

    public void setExternalStorageDisabled(ComponentName admin, boolean isDisabled) {
        this.mSponsor.transactToSetFunctionDisabled(ConstantValue.TRANSACTION_SET_EXTERNAL_STORAGE_DISABLED, admin, isDisabled, UserHandle.myUserId());
    }

    public boolean isExternalStorageDisabled(ComponentName admin) {
        return this.mSponsor.transactToIsFunctionDisabled(ConstantValue.TRANSACTION_IS_EXTERNAL_STORAGE_DISABLED, admin, 0);
    }

    public void setNfcDisabled(ComponentName admin, boolean isDisabled) {
        this.mSponsor.transactToSetFunctionDisabled(ConstantValue.TRANSACTION_SET_NFC_DISABLED, admin, isDisabled, UserHandle.myUserId());
    }

    public boolean isNfcDisabled(ComponentName admin) {
        return this.mSponsor.transactToIsFunctionDisabled(ConstantValue.TRANSACTION_IS_NFC_DISABLED, admin, 0);
    }

    public void setDataConnectivityDisabled(ComponentName admin, boolean isDisabled) {
        this.mSponsor.transactToSetFunctionDisabled(ConstantValue.TRANSACTION_SET_DATA_CONNECTIVITY_DISABLED, admin, isDisabled, UserHandle.myUserId());
    }

    public boolean isDataConnectivityDisabled(ComponentName admin) {
        return this.mSponsor.transactToIsFunctionDisabled(ConstantValue.TRANSACTION_IS_DATA_CONNECTIVITY_DISABLED, admin, 0);
    }

    public void setVoiceDisabled(ComponentName admin, boolean isDisabled) {
        this.mSponsor.transactToSetFunctionDisabled(ConstantValue.TRANSACTION_SET_VOICE_DISABLED, admin, isDisabled, UserHandle.myUserId());
    }

    public boolean isVoiceDisabled(ComponentName admin) {
        return this.mSponsor.transactToIsFunctionDisabled(ConstantValue.TRANSACTION_IS_VOICE_DISABLED, admin, 0);
    }

    public void setSmsDisabled(ComponentName admin, boolean isDisabled) {
        this.mSponsor.transactToSetFunctionDisabled(ConstantValue.TRANSACTION_SET_SMS_DISABLED, admin, isDisabled, UserHandle.myUserId());
    }

    public boolean isSmsDisabled(ComponentName admin) {
        return this.mSponsor.transactToIsFunctionDisabled(ConstantValue.TRANSACTION_IS_SMS_DISABLED, admin, 0);
    }

    public void hangupCalling(ComponentName admin) {
        this.mSponsor.transactToExecCommand(ConstantValue.TRANSACTION_HANG_UP_CALLING, admin, UserHandle.myUserId());
    }

    public void installPackage(ComponentName admin, String packagePath) {
        this.mSponsor.transactToExecCommand(ConstantValue.TRANSACTION_INSTALL_PACKAGE, admin, packagePath, UserHandle.myUserId());
    }

    public void uninstallPackage(ComponentName admin, String packageName, boolean isKeepData) {
        this.mSponsor.transactToUninstallPackage(ConstantValue.TRANSACTION_UNINSTALL_PACKAGE, admin, packageName, isKeepData, UserHandle.myUserId());
    }

    public void clearPackageData(ComponentName admin, String packageName) {
        this.mSponsor.transactToExecCommand(ConstantValue.TRANSACTION_CLEAR_PACKAGE_DATA, admin, packageName, UserHandle.myUserId());
    }

    public void enableInstallPackage(ComponentName admin) {
        this.mSponsor.transactToExecCommand(ConstantValue.TRANSACTION_ENABLE_INSTALL_PACKAGE, admin, UserHandle.myUserId());
    }

    public void disableInstallSource(ComponentName admin, List<String> whitelist) {
        this.mSponsor.transactToExecCommand(ConstantValue.TRANSACTION_DISABLED_INSTALL_SOURCE, admin, whitelist, UserHandle.myUserId());
    }

    public boolean isInstallSourceDisabled(ComponentName admin) {
        return this.mSponsor.transactToIsFunctionDisabled(ConstantValue.TRANSACTION_IS_INSTALL_SOURCE_DISABLED, admin, 0);
    }

    public List<String> getInstallPackageSourceWhiteList(ComponentName admin) {
        return this.mSponsor.transactToGetListFunction(ConstantValue.TRANSACTION_GET_INSTALL_PACKAGE_SOURCE_WHITELIST, admin, UserHandle.myUserId());
    }

    public void addPersistentApp(ComponentName admin, List<String> packageNames) {
        this.mSponsor.transactToExecCommand(ConstantValue.TRANSACTION_ADD_PERSISTENT_APP, admin, packageNames, UserHandle.myUserId());
    }

    public void removePersistentApp(ComponentName admin, List<String> packageNames) {
        this.mSponsor.transactToExecCommand(ConstantValue.TRANSACTION_REMOVE_PERSISTENT_APP, admin, packageNames, UserHandle.myUserId());
    }

    public List<String> getPersistentApp(ComponentName admin) {
        return this.mSponsor.transactToGetListFunction(ConstantValue.TRANSACTION_GET_PERSISTENT_APP, admin, 0);
    }

    public void addDisallowedRunningApp(ComponentName admin, List<String> packageNames) {
        this.mSponsor.transactToExecCommand(ConstantValue.TRANSACTION_ADD_DISALLOWED_RUNNING_APP, admin, packageNames, UserHandle.myUserId());
    }

    public void removeDisallowedRunningApp(ComponentName admin, List<String> packageNames) {
        this.mSponsor.transactToExecCommand(ConstantValue.TRANSACTION_REMOVE_DISALLOWED_RUNNING_APP, admin, packageNames, UserHandle.myUserId());
    }

    public List<String> getDisallowedRunningApp(ComponentName admin) {
        return this.mSponsor.transactToGetListFunction(ConstantValue.TRANSACTION_GET_DISALLOWED_RUNNING_APP, admin, 0);
    }

    public void addInstallPackageWhiteList(ComponentName admin, List<String> packageNames) {
        this.mSponsor.transactToExecCommand(ConstantValue.TRANSACTION_ADD_INSTALL_PACKAGE_WHITELIST, admin, packageNames, UserHandle.myUserId());
    }

    public void addInstallPackageTrustList(ComponentName admin, List<String> packageNames) {
        this.mSponsor.transactToExecCommand(ConstantValue.TRANSACTION_ADD_INSTALL_PACKAGE_TRUST_LIST, admin, packageNames, UserHandle.myUserId());
    }

    public void removeInstallPackageWhiteList(ComponentName admin, List<String> packageNames) {
        this.mSponsor.transactToExecCommand(ConstantValue.TRANSACTION_REMOVE_INSTALL_PACKAGE_WHITELIST, admin, packageNames, UserHandle.myUserId());
    }

    public void removeInstallPackageTrustList(ComponentName admin, List<String> packageNames) {
        this.mSponsor.transactToExecCommand(ConstantValue.TRANSACTION_REMOVE_INSTALL_PACKAGE_TRUST_LIST, admin, packageNames, UserHandle.myUserId());
    }

    public List<String> getInstallPackageWhiteList(ComponentName admin) {
        return this.mSponsor.transactToGetListFunction(ConstantValue.TRANSACTION_GET_INSTALL_PACKAGE_WHITELIST, admin, 0);
    }

    public void addDisallowedUninstallPackages(ComponentName admin, List<String> packageNames) {
        this.mSponsor.transactToExecCommand(ConstantValue.TRANSACTION_ADD_DISALLOWED_UNINSTALL_PACKAGES, admin, packageNames, UserHandle.myUserId());
    }

    public void removeDisallowedUninstallPackages(ComponentName admin, List<String> packageNames) {
        this.mSponsor.transactToExecCommand(ConstantValue.TRANSACTION_REMOVE_DISALLOWED_UNINSTALL_PACKAGES, admin, packageNames, UserHandle.myUserId());
    }

    public List<String> getDisallowedUninstallPackageList(ComponentName admin) {
        return this.mSponsor.transactToGetListFunction(ConstantValue.TRANSACTION_GET_DISALLOWED_UNINSTALL_PACKAGE_LIST, admin, 0);
    }

    public void addDisabledDeactivateMdmPackages(ComponentName admin, List<String> packageNames) {
        this.mSponsor.transactToExecCommand(ConstantValue.TRANSACTION_ADD_DISABLED_DEACTIVATE_MDM_PACKAGES, admin, packageNames, UserHandle.myUserId());
    }

    public void removeDisabledDeactivateMdmPackages(ComponentName admin, List<String> packageNames) {
        this.mSponsor.transactToExecCommand(ConstantValue.TRANSACTION_REMOVE_DISABLED_DEACTIVATE_MDM_PACKAGES, admin, packageNames, UserHandle.myUserId());
    }

    public List<String> getDisabledDeactivateMdmPackageList(ComponentName admin) {
        return this.mSponsor.transactToGetListFunction(ConstantValue.TRANSACTION_GET_DISABLED_DEACTIVATE_MDM_PACKAGE_LIST, admin, 0);
    }

    public void killApplicationProcess(ComponentName admin, String packageName) {
        this.mSponsor.transactToExecCommand(ConstantValue.TRANSACTION_KILL_APPLICATION_PROCESS, admin, packageName, UserHandle.myUserId());
    }

    public void shutdownDevice(ComponentName admin) {
        this.mSponsor.transactToExecCommand(ConstantValue.TRANSACTION_SHUTDOWN_DEVICE, admin, UserHandle.myUserId());
    }

    public void rebootDevice(ComponentName admin) {
        this.mSponsor.transactToExecCommand(ConstantValue.TRANSACTION_REBOOT_DEVICE, admin, UserHandle.myUserId());
    }

    public void setStatusBarExpandPanelDisabled(ComponentName admin, boolean isDisabled) {
        this.mSponsor.transactToSetFunctionDisabled(ConstantValue.TRANSACTION_SET_STATUS_BAR_EXPANDPANEL_DISABLED, admin, isDisabled, UserHandle.myUserId());
    }

    public boolean isStatusBarExpandPanelDisabled(ComponentName admin) {
        return this.mSponsor.transactToIsFunctionDisabled(ConstantValue.TRANSACTION_IS_STATUS_BAR_EXPANDPANEL_DISABLED, admin, 0);
    }

    public void configExchangeMailProvider(ComponentName admin, Bundle para) {
        this.mSponsor.transactToConfigExchangeMail(ConstantValue.TRANSACTION_CONFIG_EXCHANGE_MAIL, admin, para, UserHandle.myUserId());
    }

    public Bundle getMailProviderForDomain(ComponentName admin, String domain) {
        return this.mSponsor.transactToGetMailProviderForDomain(ConstantValue.TRANSACTION_GET_MAIL_PROVIDER_FOR_DOMAIN, admin, domain, 0);
    }

    public boolean isRooted(ComponentName admin) {
        return this.mSponsor.transactToIsFunctionDisabled(ConstantValue.TRANSACTION_IS_ROOTED, admin, 0);
    }

    public boolean isHwFrameworkAdminAllowed(int code) {
        return this.mSponsor.transactToIsFunctionDisabled(code, null, 0);
    }

    public List<String> getHwFrameworkAdminList(int code) {
        return this.mSponsor.transactToGetListFunction(code, null, 0);
    }

    public void setSafeModeDisabled(ComponentName admin, boolean isDisabled) {
        this.mSponsor.transactToSetFunctionDisabled(ConstantValue.TRANSACTION_SET_SAFEMODE_DISABLED, admin, isDisabled, UserHandle.myUserId());
    }

    public boolean isSafeModeDisabled(ComponentName admin) {
        return this.mSponsor.transactToIsFunctionDisabled(ConstantValue.TRANSACTION_IS_SAFEMODE_DISABLED, admin, 0);
    }

    public void setAdbDisabled(ComponentName admin, boolean isDisabled) {
        this.mSponsor.transactToSetFunctionDisabled(ConstantValue.TRANSACTION_SET_ADB_DISABLED, admin, isDisabled, UserHandle.myUserId());
    }

    public boolean isAdbDisabled(ComponentName admin) {
        return this.mSponsor.transactToIsFunctionDisabled(ConstantValue.TRANSACTION_IS_ADB_DISABLED, admin, 0);
    }

    public void setUsbOtgDisabled(ComponentName admin, boolean isDisabled) {
        this.mSponsor.transactToSetFunctionDisabled(ConstantValue.TRANSACTION_SET_USB_OTG_DISABLED, admin, isDisabled, UserHandle.myUserId());
    }

    public boolean isUsbOtgDisabled(ComponentName admin) {
        return this.mSponsor.transactToIsFunctionDisabled(ConstantValue.TRANSACTION_IS_USB_OTG_DISABLED, admin, 0);
    }

    public void setGpsDisabled(ComponentName admin, boolean isDisabled) {
        this.mSponsor.transactToSetFunctionDisabled(ConstantValue.TRANSACTION_SET_GPS_DISABLED, admin, isDisabled, UserHandle.myUserId());
    }

    public boolean isGpsDisabled(ComponentName admin) {
        return this.mSponsor.transactToIsFunctionDisabled(ConstantValue.TRANSACTION_IS_GPS_DISABLED, admin, 0);
    }

    public void turnOnGps(ComponentName admin, boolean isOn) {
        this.mSponsor.transactToSetFunctionDisabled(ConstantValue.TRANSACTION_TURN_ON_GPS, admin, isOn, UserHandle.myUserId());
    }

    public boolean isGpsTurnOn(ComponentName admin) {
        return this.mSponsor.transactToIsFunctionDisabled(ConstantValue.TRANSACTION_IS_GPS_TURN_ON, admin, 0);
    }

    public void setTaskButtonDisabled(ComponentName admin, boolean isDisabled) {
        this.mSponsor.transactToSetFunctionDisabled(ConstantValue.TRANSACTION_SET_TASK_BUTTON_DISABLED, admin, isDisabled, UserHandle.myUserId());
    }

    public boolean isTaskButtonDisabled(ComponentName admin) {
        return this.mSponsor.transactToIsFunctionDisabled(ConstantValue.TRANSACTION_IS_TASK_BUTTON_DISABLED, admin, 0);
    }

    public void setHomeButtonDisabled(ComponentName admin, boolean isDisabled) {
        this.mSponsor.transactToSetFunctionDisabled(ConstantValue.TRANSACTION_SET_HOME_BUTTON_DISABLED, admin, isDisabled, UserHandle.myUserId());
    }

    public boolean isHomeButtonDisabled(ComponentName admin) {
        return this.mSponsor.transactToIsFunctionDisabled(ConstantValue.TRANSACTION_IS_HOME_BUTTON_DISABLED, admin, 0);
    }

    public void setBackButtonDisabled(ComponentName admin, boolean isDisabled) {
        this.mSponsor.transactToSetFunctionDisabled(ConstantValue.TRANSACTION_SET_BACK_BUTTON_DISABLED, admin, isDisabled, UserHandle.myUserId());
    }

    public boolean isBackButtonDisabled(ComponentName admin) {
        return this.mSponsor.transactToIsFunctionDisabled(ConstantValue.TRANSACTION_IS_BACK_BUTTON_DISABLED, admin, 0);
    }

    public void setSysTime(ComponentName admin, long millis) {
        this.mSponsor.transactToExecCommand(ConstantValue.TRANSACTION_SET_SYS_TIME, admin, String.valueOf(millis), UserHandle.myUserId());
    }

    public void setDefaultLauncher(ComponentName admin, String packageName, String className) {
        this.mSponsor.transactToSetDefaultLauncher(ConstantValue.TRANSACTION_SET_DEFAULT_LAUNCHER, admin, packageName, className, 0);
    }

    public void clearDefaultLauncher(ComponentName admin) {
        this.mSponsor.transactToExecCommand(ConstantValue.TRANSACTION_CLEAR_DEFAULT_LAUNCHER, admin, 0);
    }

    public Bitmap captureScreen(ComponentName admin) {
        return this.mSponsor.transactToCaptureScreen(ConstantValue.TRANSACTION_CAPTURE_SCREEN, admin, 0);
    }

    public void setCustomSettingsMenu(ComponentName admin, List<String> menusToDelete) {
        this.mSponsor.transactToExecCommand(ConstantValue.TRANSACTION_SET_CUSTOM_SETTINGS_MENU, admin, menusToDelete, UserHandle.myUserId());
    }

    public void addApn(ComponentName admin, Map<String, String> apninfo) {
        this.mSponsor.transactToExecCommand(ConstantValue.TRANSACTION_ADD_APN, admin, apninfo, 0);
    }

    public void deleteApn(ComponentName admin, String id) {
        this.mSponsor.transactToExecCommand(ConstantValue.TRANSACTION_DELETE_APN, admin, id, 0);
    }

    public void updateApn(ComponentName admin, Map<String, String> apninfo, String id) {
        this.mSponsor.transactToExecCommand(ConstantValue.TRANSACTION_UPDATE_APN, admin, apninfo, id, 0);
    }

    public Map<String, String> getApnInfo(ComponentName admin, String id) {
        return this.mSponsor.transactToGetApnInfo(ConstantValue.TRANSACTION_GET_APN_INFO, admin, id, 0);
    }

    public List<String> queryApn(ComponentName admin, Map<String, String> apninfo) {
        return this.mSponsor.transactToQueryApn(ConstantValue.TRANSACTION_QUERY_APN, admin, apninfo, 0);
    }

    public void setPreferApn(ComponentName admin, String id) {
        this.mSponsor.transactToExecCommand(ConstantValue.TRANSACTION_SET_PREFER_APN, admin, id, 0);
    }

    public void addNetworkAccessWhitelist(ComponentName admin, List<String> addrList) {
        this.mSponsor.transactToExecCommand(ConstantValue.TRANSACTION_ADD_NETWORK_ACCESS_WHITELIST, admin, addrList, 0);
    }

    public void removeNetworkAccessWhitelist(ComponentName admin, List<String> addrList) {
        this.mSponsor.transactToExecCommand(ConstantValue.TRANSACTION_REMOVE_NETWORK_ACCESS_WHITELIST, admin, addrList, 0);
    }

    public List<String> getNetworkAccessWhitelist(ComponentName admin) {
        return this.mSponsor.transactToGetListFunction(ConstantValue.TRANSACTION_GET_NETWORK_ACCESS_WHITELIST, admin, 0);
    }

    public int getSdCardEncryptionStatus() {
        return this.mSponsor.transactToGetSdCardEncryptionStatus(ConstantValue.TRANSACTION_GET_SDCARD_ENCRYPTION_STATUS, 0);
    }

    public void setSdCardDecryptionDisabled(ComponentName admin, boolean isDisabled) {
        this.mSponsor.transactToSetFunctionDisabled(ConstantValue.TRANSACTION_SET_SDCARD_DECRYPTION_DISABLED, admin, isDisabled, UserHandle.myUserId());
    }

    public boolean isSdCardDecryptionDisabled(ComponentName admin) {
        return this.mSponsor.transactToIsFunctionDisabled(ConstantValue.TRANSACTION_IS_SDCARD_DECRYPTION_DISABLED, admin, UserHandle.myUserId());
    }

    public boolean setPolicy(ComponentName admin, String policyName, Bundle policyData) {
        Bundle bundle = new Bundle();
        bundle.putInt("code", ConstantValue.TRANSACTION_SET_POLICY);
        bundle.putString("name", policyName);
        return this.mSponsor.transactToSetPolicy(bundle, admin, UserHandle.myUserId(), policyData, 0);
    }

    public Bundle getPolicy(ComponentName admin, String policyName) {
        Bundle bundle = new Bundle();
        bundle.putInt("code", ConstantValue.TRANSACTION_GET_POLICY);
        bundle.putString("name", policyName);
        return this.mSponsor.transactToGetPolicy(bundle, null, admin, UserHandle.myUserId(), 0);
    }

    public Bundle getPolicy(ComponentName admin, String policyName, int userId) {
        Bundle bundle = new Bundle();
        bundle.putInt("code", ConstantValue.TRANSACTION_GET_POLICY);
        bundle.putString("name", policyName);
        return this.mSponsor.transactToGetPolicy(bundle, null, admin, userId, 0);
    }

    public boolean removePolicy(ComponentName admin, String policyName, Bundle policyData) {
        Bundle bundle = new Bundle();
        bundle.putInt("code", ConstantValue.TRANSACTION_REMOVE_POLICY);
        bundle.putString("name", policyName);
        return this.mSponsor.transactToSetPolicy(bundle, admin, UserHandle.myUserId(), policyData, 0);
    }

    public boolean setCustomPolicy(ComponentName admin, String policyName, Bundle policyData) {
        Bundle bundle = new Bundle();
        bundle.putInt("code", ConstantValue.TRANSACTION_SET_POLICY);
        bundle.putString("name", policyName);
        return this.mSponsor.transactToSetPolicy(bundle, admin, UserHandle.myUserId(), policyData, 1);
    }

    public Bundle getCustomPolicy(ComponentName admin, String policyName, Bundle keyWords) {
        Bundle bundle = new Bundle();
        bundle.putInt("code", ConstantValue.TRANSACTION_GET_POLICY);
        bundle.putString("name", policyName);
        return this.mSponsor.transactToGetPolicy(bundle, keyWords, admin, UserHandle.myUserId(), 1);
    }

    public boolean removeCustomPolicy(ComponentName admin, String policyName, Bundle policyData) {
        Bundle bundle = new Bundle();
        bundle.putInt("code", ConstantValue.TRANSACTION_REMOVE_POLICY);
        bundle.putString("name", policyName);
        return this.mSponsor.transactToSetPolicy(bundle, admin, UserHandle.myUserId(), policyData, 1);
    }

    public Bundle getCachedPolicyForFwk(ComponentName admin, String policyName, Bundle keyWords) {
        Bundle bundle = new Bundle();
        bundle.putInt("code", ConstantValue.TRANSACTION_GET_POLICY);
        bundle.putString("name", policyName);
        return this.mSponsor.transactToGetPolicy(bundle, keyWords, admin, UserHandle.myUserId(), 2);
    }

    public boolean hasHwPolicy() {
        return this.mSponsor.transactToHasHwPolicy(ConstantValue.TRANSACTION_HAS_HW_POLICY, UserHandle.myUserId());
    }

    public void setSilentActiveAdmin(ComponentName admin) {
        this.mSponsor.transactToExecCommand(ConstantValue.TRANSACTION_SET_SILENT_ACTIVE_ADMIN, admin, UserHandle.myUserId());
    }

    public void setAccountDisabled(ComponentName admin, String accountType, boolean isDisabled) {
        this.mSponsor.transactToSetAccountDisabled(ConstantValue.TRANSACTION_SET_ACCOUNT_DISABLED, admin, accountType, isDisabled, UserHandle.myUserId());
    }

    public boolean isAccountDisabled(ComponentName admin, String accountType) {
        return this.mSponsor.transactToIsAccountDisabled(ConstantValue.TRANSACTION_IS_ACCOUNT_DISABLED, admin, accountType, UserHandle.myUserId());
    }

    public boolean formatSdCard(ComponentName admin, String diskId) {
        return this.mSponsor.transactToFormatSdCard(ConstantValue.TRANSACTION_FORMAT_SDCARD, admin, diskId, UserHandle.myUserId());
    }

    public boolean installCertificateWithType(ComponentName admin, Bundle bundle, byte[] certBuffer) {
        return this.mSponsor.transactToInstallCertificateWithType(ConstantValue.TRANSACTION_INSTALL_CERTIFICATE_WITH_TYPE, admin, bundle, certBuffer, UserHandle.myUserId());
    }

    public void setDeviceOwnerApp(ComponentName admin, String ownerName) {
        this.mSponsor.transactToExecCommand(ConstantValue.TRANSACTION_SET_DEVICE_OWNER_APP, admin, ownerName, UserHandle.myUserId());
    }

    public void clearDeviceOwnerApp() {
        this.mSponsor.transactToExecCommand(ConstantValue.TRANSACTION_CLEAR_DEVICE_OWNER_APP, null, UserHandle.myUserId());
    }

    public void turnOnMobiledata(ComponentName admin, boolean isOn) {
        this.mSponsor.transactToSetFunctionDisabled(ConstantValue.TRANSACTION_TURN_ON_MOBILE_DATA, admin, isOn, 0);
    }

    public void resetNetworkSetting(ComponentName admin) {
        this.mSponsor.transactToExecCommand(ConstantValue.TRANSACTION_RESET_NETWORK_SETTING, admin, UserHandle.myUserId());
    }

    public boolean setDefaultDataCard(ComponentName admin, int slotId, Message response) {
        return this.mSponsor.transactToSetDefaultDataCard(ConstantValue.TRANSACTION_SET_DEFAULT_DATA_CARD, admin, slotId, response, UserHandle.myUserId());
    }
}
