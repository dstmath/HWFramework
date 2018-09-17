package com.android.server.devicepolicy;

import android.content.ComponentName;
import android.graphics.Bitmap;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

interface IHwDevicePolicyManager {
    void addApn(ComponentName componentName, Map<String, String> map, int i);

    void addDisabledDeactivateMdmPackages(ComponentName componentName, List<String> list, int i);

    void addDisallowedRunningApp(ComponentName componentName, List<String> list, int i);

    void addDisallowedUninstallPackages(ComponentName componentName, List<String> list, int i);

    void addInstallPackageWhiteList(ComponentName componentName, List<String> list, int i);

    void addNetworkAccessWhitelist(ComponentName componentName, List<String> list, int i);

    void addPersistentApp(ComponentName componentName, List<String> list, int i);

    void bdReport(int i, String str);

    Bitmap captureScreen(ComponentName componentName, int i);

    boolean clearCarrierLockScreenPassword(ComponentName componentName, String str, int i);

    void clearDefaultLauncher(ComponentName componentName, int i);

    void clearDeviceOwnerApp(int i);

    void clearPackageData(ComponentName componentName, String str, int i);

    void configExchangeMailProvider(ComponentName componentName, Bundle bundle, int i);

    int configVpnProfile(ComponentName componentName, Bundle bundle, int i);

    void deleteApn(ComponentName componentName, String str, int i);

    void disableInstallSource(ComponentName componentName, List<String> list, int i);

    void enableInstallPackage(ComponentName componentName, int i);

    boolean formatSDCard(ComponentName componentName, String str, int i);

    Map<String, String> getApnInfo(ComponentName componentName, String str, int i);

    List<String> getDisabledDeactivateMdmPackageList(ComponentName componentName, int i);

    List<String> getDisallowedRunningApp(ComponentName componentName, int i);

    List<String> getDisallowedUninstallPackageList(ComponentName componentName, int i);

    Bundle getHwAdminCachedBundle(String str);

    List<String> getHwAdminCachedList(int i);

    boolean getHwAdminCachedValue(int i);

    List<String> getInstallPackageSourceWhiteList(ComponentName componentName, int i);

    List<String> getInstallPackageWhiteList(ComponentName componentName, int i);

    Bundle getMailProviderForDomain(ComponentName componentName, String str, int i);

    List<String> getNetworkAccessWhitelist(ComponentName componentName, int i);

    List<String> getPersistentApp(ComponentName componentName, int i);

    Bundle getPolicy(ComponentName componentName, String str, int i);

    int getSDCardEncryptionStatus();

    Bundle getVpnList(ComponentName componentName, Bundle bundle, int i);

    Bundle getVpnProfile(ComponentName componentName, Bundle bundle, int i);

    void hangupCalling(ComponentName componentName, int i);

    boolean hasHwPolicy(int i);

    boolean installCertificateWithType(ComponentName componentName, int i, byte[] bArr, String str, String str2, int i2, boolean z, int i3);

    void installPackage(ComponentName componentName, String str, int i);

    boolean isAccountDisabled(ComponentName componentName, String str, int i);

    boolean isAdbDisabled(ComponentName componentName, int i);

    boolean isBackButtonDisabled(ComponentName componentName, int i);

    boolean isBluetoothDisabled(ComponentName componentName, int i);

    boolean isBootLoaderDisabled(ComponentName componentName, int i);

    boolean isDataConnectivityDisabled(ComponentName componentName, int i);

    boolean isExternalStorageDisabled(ComponentName componentName, int i);

    boolean isGPSDisabled(ComponentName componentName, int i);

    boolean isGPSTurnOn(ComponentName componentName, int i);

    boolean isHomeButtonDisabled(ComponentName componentName, int i);

    boolean isInstallSourceDisabled(ComponentName componentName, int i);

    boolean isNFCDisabled(ComponentName componentName, int i);

    boolean isRooted(ComponentName componentName, int i);

    boolean isSDCardDecryptionDisabled(ComponentName componentName, int i);

    boolean isSMSDisabled(ComponentName componentName, int i);

    boolean isSafeModeDisabled(ComponentName componentName, int i);

    boolean isStatusBarExpandPanelDisabled(ComponentName componentName, int i);

    boolean isTaskButtonDisabled(ComponentName componentName, int i);

    boolean isUSBDataDisabled(ComponentName componentName, int i);

    boolean isUSBOtgDisabled(ComponentName componentName, int i);

    boolean isVoiceDisabled(ComponentName componentName, int i);

    boolean isWifiApDisabled(ComponentName componentName, int i);

    boolean isWifiDisabled(ComponentName componentName, int i);

    void killApplicationProcess(ComponentName componentName, String str, int i);

    List<String> queryApn(ComponentName componentName, Map<String, String> map, int i);

    ArrayList<String> queryBrowsingHistory(ComponentName componentName, int i);

    void removeDisabledDeactivateMdmPackages(ComponentName componentName, List<String> list, int i);

    void removeDisallowedRunningApp(ComponentName componentName, List<String> list, int i);

    void removeDisallowedUninstallPackages(ComponentName componentName, List<String> list, int i);

    void removeInstallPackageWhiteList(ComponentName componentName, List<String> list, int i);

    void removeNetworkAccessWhitelist(ComponentName componentName, List<String> list, int i);

    void removePersistentApp(ComponentName componentName, List<String> list, int i);

    int removePolicy(ComponentName componentName, String str, Bundle bundle, int i);

    int removeVpnProfile(ComponentName componentName, Bundle bundle, int i);

    void setAccountDisabled(ComponentName componentName, String str, boolean z, int i);

    void setAdbDisabled(ComponentName componentName, boolean z, int i);

    void setBackButtonDisabled(ComponentName componentName, boolean z, int i);

    void setBluetoothDisabled(ComponentName componentName, boolean z, int i);

    void setBootLoaderDisabled(ComponentName componentName, boolean z, int i);

    boolean setCarrierLockScreenPassword(ComponentName componentName, String str, String str2, int i);

    void setCustomSettingsMenu(ComponentName componentName, List<String> list, int i);

    void setDataConnectivityDisabled(ComponentName componentName, boolean z, int i);

    void setDefaultLauncher(ComponentName componentName, String str, String str2, int i);

    void setDeviceOwnerApp(ComponentName componentName, String str, int i);

    void setExternalStorageDisabled(ComponentName componentName, boolean z, int i);

    void setGPSDisabled(ComponentName componentName, boolean z, int i);

    void setHomeButtonDisabled(ComponentName componentName, boolean z, int i);

    void setNFCDisabled(ComponentName componentName, boolean z, int i);

    int setPolicy(ComponentName componentName, String str, Bundle bundle, int i);

    void setPreferApn(ComponentName componentName, String str, int i);

    void setSDCardDecryptionDisabled(ComponentName componentName, boolean z, int i);

    void setSMSDisabled(ComponentName componentName, boolean z, int i);

    void setSafeModeDisabled(ComponentName componentName, boolean z, int i);

    void setSilentActiveAdmin(ComponentName componentName, int i);

    void setStatusBarExpandPanelDisabled(ComponentName componentName, boolean z, int i);

    void setSysTime(ComponentName componentName, long j, int i);

    int setSystemLanguage(ComponentName componentName, Bundle bundle, int i);

    void setTaskButtonDisabled(ComponentName componentName, boolean z, int i);

    void setUSBDataDisabled(ComponentName componentName, boolean z, int i);

    void setUSBOtgDisabled(ComponentName componentName, boolean z, int i);

    void setVoiceDisabled(ComponentName componentName, boolean z, int i);

    void setWifiApDisabled(ComponentName componentName, boolean z, int i);

    void setWifiDisabled(ComponentName componentName, boolean z, int i);

    void shutdownOrRebootDevice(int i, ComponentName componentName, int i2);

    void turnOnGPS(ComponentName componentName, boolean z, int i);

    void turnOnMobiledata(ComponentName componentName, boolean z, int i);

    void uninstallPackage(ComponentName componentName, String str, boolean z, int i);

    void updateApn(ComponentName componentName, Map<String, String> map, String str, int i);
}
