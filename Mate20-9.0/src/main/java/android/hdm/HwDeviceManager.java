package android.hdm;

import android.common.HwFrameworkFactory;
import android.content.Intent;
import android.util.Log;
import java.util.List;

public class HwDeviceManager {
    public static final int DISABLED_ADB = 11;
    public static final int DISABLED_BACK = 16;
    public static final int DISABLED_BLUETOOTH = 8;
    public static final int DISABLED_CLIPBOARD = 23;
    public static final int DISABLED_GOOGLE_ACCOUNT_AUTOSYNC = 25;
    public static final int DISABLED_GPS = 13;
    public static final int DISABLED_HEADPHONE = 31;
    public static final int DISABLED_HOME = 14;
    public static final int DISABLED_INSTALL_SOURCE = 2;
    public static final int DISABLED_LOCATION_MODE = 40;
    public static final int DISABLED_LOCATION_SERVICE = 39;
    public static final int DISABLED_MICROPHONE = 30;
    public static final int DISABLED_NETWORK_LOCATION = 41;
    public static final int DISABLED_SAFEMODE = 10;
    public static final int DISABLED_TASK = 15;
    public static final int DISABLED_USBOTG = 12;
    public static final int DISABLED_VOICE = 1;
    public static final int DISABLED_WIFI = 0;
    public static final int DISABLE_CHANGE_LAUNCHER = 17;
    public static final int DISABLE_CHANGE_WALLPAPER = 35;
    public static final int DISABLE_ROAMING_AUTOSYNC = 42;
    public static final int DISABLE_WRITING_SDCARD = 100;
    public static final int GET_SINGLE_APP = 34;
    public static final int IS_ACCESSSIBILITY_SERVICES_WHITELIST = 53;
    public static final int IS_ADBORSDCARD_INSTALL_RESTRICTED = 6;
    public static final int IS_ALLOWED_INSTALL_PACKAGE = 7;
    public static final int IS_DISABLED_DEACTIVATE_MDM_PACKAGE = 18;
    public static final int IS_DISABLED_FINGERPRINT_AUTHENTICATION = 50;
    public static final int IS_DISABLED_NAVIGATIONBAR = 103;
    public static final int IS_DISABLED_NOTIFICATION = 102;
    public static final int IS_DISABLED_PASSIVE_PROVIDER = 101;
    public static final int IS_DISABLE_ANDROID_ANIMATION = 44;
    public static final int IS_DISABLE_APPLICATION = 21;
    public static final int IS_DISABLE_FILE_SHARE = 60;
    public static final int IS_DISABLE_INFRARED = 48;
    public static final int IS_DISABLE_POWER_SHUTDOWN = 37;
    public static final int IS_DISABLE_SCREEN_CAPTURE = 20;
    public static final int IS_DISABLE_SCREEN_OFF = 36;
    public static final int IS_DISABLE_SEND_NOTIFICATION = 33;
    public static final int IS_DISABLE_SHUTDOWNMENU = 49;
    public static final int IS_DISABLE_VOLUME = 38;
    public static final int IS_DISABLE_WIFIP2P = 45;
    public static final int IS_DISALLOWED_INSTALL_PACKAGE = 19;
    public static final int IS_DISALLOWED_RUNNINGAPP = 4;
    public static final int IS_DISALLOWED_UNINSTALL_PACKAGE = 5;
    public static final int IS_FORCE_ENABLE_BT = 51;
    public static final int IS_FORCE_ENABLE_WIFI = 52;
    public static final int IS_IGNORED_FREQUENT_RELAUNCH_APP = 26;
    public static final int IS_PERSISTENT_APP = 3;
    public static final int IS_VISIBLE_GOOGLE_ACCOUNT_SYNC_ADAPTER = 24;
    public static final int NETWORK_ACCESS_WHITELIST = 9;
    public static final int SUPERWHITELIST_APP = 22;
    private static final String TAG = "HwDeviceManager";
    private static IHwDeviceManager sInstance = null;

    public interface IHwDeviceManager {
        List<String> getNetworkAccessWhitelist();

        String getSingleApp();

        List<String> getSuperWhiteListApp();

        boolean isAccessibilityServicesWhiteList(String str);

        boolean isAdbDisabled();

        boolean isAdbOrSDCardInstallRestricted();

        boolean isAllowedInstallPackage(String str);

        boolean isAndroidAnimationDisabled();

        boolean isApplicationDisabled(Intent intent);

        boolean isBackButtonDisabled();

        boolean isBluetoothDisabled();

        boolean isChangeLauncherDisable();

        boolean isChangeWallpaperDisabled();

        boolean isClipboardDisabled();

        boolean isDisabledDeactivateMdmPackage(String str);

        boolean isDisallowedInstallPackage(String str);

        boolean isDisallowedRunningApp(String str);

        boolean isDisallowedUninstallPackage(String str);

        boolean isFileShareDisabled();

        boolean isFingerprintAuthenticationDisabled();

        boolean isForceEnableBT();

        boolean isForceEnableWifi();

        boolean isGPSDisabled();

        boolean isGoogleAccountAutoSyncDisabled(String str);

        boolean isHeadphoneDisabled();

        boolean isHomeButtonDisabled();

        boolean isIgnoredFrequentRelaunchApp(String str);

        boolean isInFraredDisabled();

        boolean isInstallSourceDisabled();

        boolean isIntentFromAllowedInstallSource(Intent intent);

        boolean isLocationModeDisabled();

        boolean isLocationServiceDisabled();

        boolean isMicrophoneDisabled();

        boolean isNavigationBarDisabled();

        boolean isNetworkLocationDisabled();

        boolean isNotificationDisabled();

        boolean isPassiveProviderDisabled();

        boolean isPersistentApp(String str);

        boolean isPowerDisabled();

        boolean isRoamingSyncDisabled();

        boolean isSafeModeDisabled();

        boolean isScreenCaptureDisabled();

        boolean isScreenOffDisabled();

        boolean isSendNotificationDisabled();

        boolean isShutdownMenuDisabled();

        boolean isTaskButtonDisabled();

        boolean isUSBOtgDisabled();

        boolean isVisibleGoogleAccountSync(String str);

        boolean isVoiceDisabled();

        boolean isVolumeAdjustDisabled();

        boolean isWifiDisabled();

        boolean isWifiP2PDisabled();

        boolean isWritingSDCardDisabled();
    }

    private static IHwDeviceManager getImplObject() {
        if (sInstance == null) {
            sInstance = HwFrameworkFactory.getHuaweiDevicePolicyManager();
        }
        return sInstance;
    }

    public static boolean disallowOp(int type) {
        IHwDeviceManager instance = getImplObject();
        boolean bDisabled = false;
        if (instance == null) {
            Log.w(TAG, "Can not get the instance of IHwDeviceManager object.");
            return false;
        }
        switch (type) {
            case 0:
                bDisabled = instance.isWifiDisabled();
                break;
            case 1:
                bDisabled = instance.isVoiceDisabled();
                break;
            case 2:
                bDisabled = instance.isInstallSourceDisabled();
                break;
            default:
                switch (type) {
                    case 10:
                        bDisabled = instance.isSafeModeDisabled();
                        break;
                    case 11:
                        bDisabled = instance.isAdbDisabled();
                        break;
                    case 12:
                        bDisabled = instance.isUSBOtgDisabled();
                        break;
                    case 13:
                        bDisabled = instance.isGPSDisabled();
                        break;
                    case 14:
                        bDisabled = instance.isHomeButtonDisabled();
                        break;
                    case 15:
                        bDisabled = instance.isTaskButtonDisabled();
                        break;
                    case 16:
                        bDisabled = instance.isBackButtonDisabled();
                        break;
                    case 17:
                        bDisabled = instance.isChangeLauncherDisable();
                        break;
                    default:
                        switch (type) {
                            case 30:
                                bDisabled = instance.isMicrophoneDisabled();
                                break;
                            case 31:
                                bDisabled = instance.isHeadphoneDisabled();
                                break;
                            default:
                                switch (type) {
                                    case 35:
                                        bDisabled = instance.isChangeWallpaperDisabled();
                                        break;
                                    case 36:
                                        bDisabled = instance.isScreenOffDisabled();
                                        break;
                                    case 37:
                                        bDisabled = instance.isPowerDisabled();
                                        break;
                                    case 38:
                                        bDisabled = instance.isVolumeAdjustDisabled();
                                        break;
                                    case 39:
                                        bDisabled = instance.isLocationServiceDisabled();
                                        break;
                                    case 40:
                                        bDisabled = instance.isLocationModeDisabled();
                                        break;
                                    case 41:
                                        bDisabled = instance.isNetworkLocationDisabled();
                                        break;
                                    case 42:
                                        bDisabled = instance.isRoamingSyncDisabled();
                                        break;
                                    default:
                                        switch (type) {
                                            case 44:
                                                bDisabled = instance.isAndroidAnimationDisabled();
                                                break;
                                            case 45:
                                                bDisabled = instance.isWifiP2PDisabled();
                                                break;
                                            default:
                                                switch (type) {
                                                    case 48:
                                                        bDisabled = instance.isInFraredDisabled();
                                                        break;
                                                    case 49:
                                                        bDisabled = instance.isShutdownMenuDisabled();
                                                        break;
                                                    case 50:
                                                        bDisabled = instance.isFingerprintAuthenticationDisabled();
                                                        break;
                                                    case 51:
                                                        bDisabled = instance.isForceEnableBT();
                                                        break;
                                                    case 52:
                                                        bDisabled = instance.isForceEnableWifi();
                                                        break;
                                                    default:
                                                        switch (type) {
                                                            case 100:
                                                                bDisabled = instance.isWritingSDCardDisabled();
                                                                break;
                                                            case 101:
                                                                bDisabled = instance.isPassiveProviderDisabled();
                                                                break;
                                                            case 102:
                                                                bDisabled = instance.isNotificationDisabled();
                                                                break;
                                                            case 103:
                                                                bDisabled = instance.isNavigationBarDisabled();
                                                                break;
                                                            default:
                                                                switch (type) {
                                                                    case 6:
                                                                        bDisabled = instance.isAdbOrSDCardInstallRestricted();
                                                                        break;
                                                                    case 8:
                                                                        bDisabled = instance.isBluetoothDisabled();
                                                                        break;
                                                                    case 23:
                                                                        bDisabled = instance.isClipboardDisabled();
                                                                        break;
                                                                    case 33:
                                                                        bDisabled = instance.isSendNotificationDisabled();
                                                                        break;
                                                                    case 60:
                                                                        try {
                                                                            bDisabled = instance.isFileShareDisabled();
                                                                            break;
                                                                        } catch (Exception e) {
                                                                            Log.e(TAG, "Disallow operation " + type + " exception: " + e.getMessage());
                                                                            break;
                                                                        }
                                                                }
                                                        }
                                                }
                                        }
                                }
                        }
                }
        }
        return bDisabled;
    }

    public static boolean disallowOp(int type, String param) {
        IHwDeviceManager instance = getImplObject();
        boolean bDisabled = false;
        if (instance == null) {
            Log.w(TAG, "Can not get the instance of IHwDeviceManager object.");
            return false;
        }
        if (type == 7) {
            bDisabled = !instance.isAllowedInstallPackage(param);
        } else if (type != 53) {
            switch (type) {
                case 3:
                    bDisabled = instance.isPersistentApp(param);
                    break;
                case 4:
                    bDisabled = instance.isDisallowedRunningApp(param);
                    break;
                case 5:
                    bDisabled = instance.isDisallowedUninstallPackage(param);
                    break;
                default:
                    switch (type) {
                        case 18:
                            bDisabled = instance.isDisabledDeactivateMdmPackage(param);
                            break;
                        case 19:
                            bDisabled = instance.isDisallowedInstallPackage(param);
                            break;
                        default:
                            switch (type) {
                                case 24:
                                    bDisabled = instance.isVisibleGoogleAccountSync(param);
                                    break;
                                case 25:
                                    bDisabled = instance.isGoogleAccountAutoSyncDisabled(param);
                                    break;
                                case 26:
                                    try {
                                        bDisabled = instance.isIgnoredFrequentRelaunchApp(param);
                                        break;
                                    } catch (Exception e) {
                                        Log.e(TAG, "Disallow operation " + type + " exception: " + e.getMessage());
                                        break;
                                    }
                            }
                    }
            }
        } else {
            bDisabled = instance.isAccessibilityServicesWhiteList(param);
        }
        return bDisabled;
    }

    public static boolean disallowOp(Intent installSource) {
        IHwDeviceManager instance = getImplObject();
        boolean bDisabled = false;
        if (instance == null) {
            Log.w(TAG, "Can not get the instance of IHwDeviceManager object.");
            return false;
        }
        try {
            bDisabled = !instance.isIntentFromAllowedInstallSource(installSource);
        } catch (Exception e) {
            Log.e(TAG, "Disallow operation " + installSource.getAction() + " exception: " + e.getMessage());
        }
        return bDisabled;
    }

    public static List<String> getList(int type) {
        IHwDeviceManager instance = getImplObject();
        List<String> list = null;
        if (instance == null) {
            Log.w(TAG, "Can not get the instance of IHwDeviceManager object.");
            return null;
        }
        if (type == 9) {
            list = instance.getNetworkAccessWhitelist();
        } else if (type == 22) {
            try {
                list = instance.getSuperWhiteListApp();
            } catch (Exception e) {
                Log.e(TAG, "Get list " + type + " exception: " + e.getMessage());
            }
        }
        return list;
    }

    public static String getString(int type) {
        IHwDeviceManager instance = getImplObject();
        if (instance == null) {
            Log.w(TAG, "Can not get the instance of IHwDeviceManager object.");
            return null;
        }
        String result = null;
        if (type == 34) {
            result = instance.getSingleApp();
        }
        return result;
    }

    public static boolean mdmDisallowOp(int type, Intent intent) {
        IHwDeviceManager instance = getImplObject();
        boolean bDisabled = false;
        if (instance == null) {
            Log.w(TAG, "Can not get the instance of IHwDeviceManager object.");
            return false;
        }
        switch (type) {
            case 20:
                bDisabled = instance.isScreenCaptureDisabled();
                break;
            case 21:
                try {
                    bDisabled = instance.isApplicationDisabled(intent);
                    break;
                } catch (Exception e) {
                    Log.e(TAG, "Disallow operation " + type + " exception: " + e.getMessage());
                    break;
                }
        }
        return bDisabled;
    }
}
