package com.android.server.devicepolicy;

import java.util.HashMap;

public class LegacyPolicy {
    private static final HashMap<Integer, String> POLICY_DATA = new HashMap<Integer, String>() {
        /* class com.android.server.devicepolicy.LegacyPolicy.AnonymousClass1 */

        {
            put(1004, "setWifiDisabled");
            put(1006, "setWifiApDisabled");
            put(1010, "setUSBDataDisabled");
            put(1012, "setExternalStorageDisabled");
            put(1014, "setNFCDisabled");
            put(1016, "setDataConnectivityDisabled");
            put(1018, "setVoiceDisabled");
            put(1020, "setSMSDisabled");
            put(1022, "setStatusBarExpandPanelDisabled");
            put(1024, "setBluetoothDisabled");
            put(1026, "setGPSDisabled");
            put(1028, "setAdbDisabled");
            put(1030, "setUSBOtgDisabled");
            put(1032, "setSafeModeDisabled");
            put(1034, "setTaskButtonDisabled");
            put(1036, "setHomeButtonDisabled");
            put(1038, "setBackButtonDisabled");
            put(1501, "shutdownDevice");
            put(1502, "rebootDevice");
            put(1504, "turnOnGPS");
            put(1506, "setDefaultLauncher");
            put(1507, "clearDefaultLauncher");
            put(1508, "setCustomSettingsMenu");
            put(1509, "captureScreen");
            put(1510, "setSysTime");
            put(1511, "setDeviceOwnerApp");
            put(1512, "clearDeviceOwnerApp");
            put(1513, "turnOnMobiledata");
            put(1514, "setDefaultDataCard");
            put(2001, "hangupCalling");
            put(2501, "installPackage");
            put(2502, "uninstallPackage");
            put(2503, "clearPackageData");
            put(2504, "enableInstallPackage");
            put(2505, "disableInstallSource");
            put(2508, "addInstallPackageWhiteList");
            put(2509, "removeInstallPackageWhiteList");
            put(2511, "addDisallowedUninstallPackages");
            put(2512, "removeDisallowedUninstallPackages");
            put(2514, "addDisabledDeactivateMdmPackages");
            put(2515, "removeDisabledDeactivateMdmPackages");
            put(3001, "addPersistentApp");
            put(3002, "removePersistentApp");
            put(3004, "addDisallowedRunningApp");
            put(3005, "removeDisallowedRunningApp");
            put(3007, "killApplicationProcess");
            put(3501, "configExchangeMail");
            put(3503, "resetNetworkSetting");
            put(5001, "addApn");
            put(5002, "deleteApn");
            put(5003, "updateApn");
            put(5006, "setPreferApn");
            put(5007, "addNetworkAccessWhitelist");
            put(5008, "removeNetworkAccessWhitelist");
            put(5011, "setSDCardDecryptionDisabled");
            put(5017, "formatSDCard");
            put(5018, "setAccountDisabled");
            put(5020, "installCertificateWithType");
            put(6001, "setSilentActiveAdmin");
            put(2517, "addInstallPackageTrustList");
            put(2518, "removeInstallPackageTrustList");
        }
    };

    private LegacyPolicy() {
    }

    public static String getPolicyName(int code) {
        return POLICY_DATA.get(Integer.valueOf(code));
    }
}
