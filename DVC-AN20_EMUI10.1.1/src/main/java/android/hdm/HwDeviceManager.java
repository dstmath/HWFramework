package android.hdm;

import android.common.HwFrameworkFactory;
import android.content.Intent;
import android.os.SystemClock;
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
    private static final int DISABLE_OPS_TIMEOUT = 100;
    public static final int DISABLE_ROAMING_AUTOSYNC = 42;
    public static final int DISABLE_WRITING_SDCARD = 100;
    public static final int GET_SINGLE_APP = 34;
    public static final int IS_ACCESSSIBILITY_SERVICES_WHITELIST = 53;
    public static final int IS_ADBORSDCARD_INSTALL_RESTRICTED = 6;
    public static final int IS_ALLOWED_INSTALL_PACKAGE = 7;
    public static final int IS_DISABLED_DEACTIVATE_MDM_PACKAGE = 18;
    public static final int IS_DISABLED_FINGERPRINT_AUTHENTICATION = 50;
    public static final int IS_DISABLED_MULTIWINDOW = 54;
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
    public static final int IS_DISABLE_SCREEN_TURN_OFF = 61;
    public static final int IS_DISABLE_SEND_NOTIFICATION = 33;
    public static final int IS_DISABLE_SHUTDOWNMENU = 49;
    public static final int IS_DISABLE_STATUS_BAR = 71;
    public static final int IS_DISABLE_VOLUME = 38;
    public static final int IS_DISABLE_WIFIP2P = 45;
    public static final int IS_DISABLE_WIFI_UNAVAILABLE_TIPS = 70;
    public static final int IS_DISALLOWED_INSTALL_PACKAGE = 19;
    public static final int IS_DISALLOWED_RUNNINGAPP = 4;
    public static final int IS_DISALLOWED_UNINSTALL_PACKAGE = 5;
    public static final int IS_FORCE_ENABLE_BT = 51;
    public static final int IS_FORCE_ENABLE_WIFI = 52;
    public static final int IS_IGNORED_FREQUENT_RELAUNCH_APP = 26;
    public static final int IS_MEDIA_CONTROL_DISABLED = 66;
    public static final int IS_PERSISTENT_APP = 3;
    public static final int IS_VISIBLE_GOOGLE_ACCOUNT_SYNC_ADAPTER = 24;
    public static final int NETWORK_ACCESS_WHITELIST = 9;
    public static final int NETWORK_DOMAINNAME_BLACKLIST = 65;
    public static final int NETWORK_DOMAINNAME_WHITELIST = 64;
    public static final int NETWORK_IP_BLACKLIST = 63;
    public static final int NETWORK_IP_WHITELIST = 62;
    private static final String POLICY_NETWORK_BLACK_DOMAIN_LIST = "network-black-domain-list";
    private static final String POLICY_NETWORK_BLACK_IP_LIST = "network-black-ip-list";
    private static final String POLICY_NETWORK_WHITE_DOMAIN_LIST = "network-white-domain-list";
    private static final String POLICY_NETWORK_WHITE_IP_LIST = "network-white-ip-list";
    public static final int SUPERWHITELIST_APP = 22;
    private static final String TAG = "HwDeviceManager";
    public static final int TASK_LOCK_APP_LIST = 55;
    private static IHwDeviceManager sInstance = null;

    public interface IHwDeviceManager {
        List<String> getNetworkAccessList(String str);

        List<String> getNetworkAccessWhitelist();

        String getSingleApp();

        List<String> getSuperWhiteListApp();

        List<String> getTaskLockAppList();

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

        boolean isMediaControlDisabled();

        boolean isMicrophoneDisabled();

        boolean isMultiWindowDisabled();

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

        boolean isSleepByPowerButtonDisabled();

        boolean isStatusBarDisabled();

        boolean isTaskButtonDisabled();

        boolean isUSBOtgDisabled();

        boolean isVisibleGoogleAccountSync(String str);

        boolean isVoiceDisabled();

        boolean isVolumeAdjustDisabled();

        boolean isWifiDisabled();

        boolean isWifiP2PDisabled();

        boolean isWifiUnavailableTipsDisabled(String str);

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
        if (instance == null) {
            Log.w(TAG, "Can not get the instance of IHwDeviceManager object.");
            return false;
        }
        long startTime = SystemClock.uptimeMillis();
        boolean bDisabled = false;
        if (type == 0) {
            bDisabled = instance.isWifiDisabled();
        } else if (type == 1) {
            bDisabled = instance.isVoiceDisabled();
        } else if (type == 2) {
            bDisabled = instance.isInstallSourceDisabled();
        } else if (type == 6) {
            bDisabled = instance.isAdbOrSDCardInstallRestricted();
        } else if (type == 8) {
            bDisabled = instance.isBluetoothDisabled();
        } else if (type == 23) {
            bDisabled = instance.isClipboardDisabled();
        } else if (type == 33) {
            bDisabled = instance.isSendNotificationDisabled();
        } else if (type == 54) {
            bDisabled = instance.isMultiWindowDisabled();
        } else if (type == 66) {
            bDisabled = instance.isMediaControlDisabled();
        } else if (type == 71) {
            bDisabled = instance.isStatusBarDisabled();
        } else if (type == 30) {
            bDisabled = instance.isMicrophoneDisabled();
        } else if (type == 31) {
            bDisabled = instance.isHeadphoneDisabled();
        } else if (type == 44) {
            bDisabled = instance.isAndroidAnimationDisabled();
        } else if (type == 45) {
            bDisabled = instance.isWifiP2PDisabled();
        } else if (type == 60) {
            bDisabled = instance.isFileShareDisabled();
        } else if (type != 61) {
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
                                            try {
                                                bDisabled = instance.isNavigationBarDisabled();
                                                break;
                                            } catch (Exception e) {
                                                Log.e(TAG, "Disallow operation " + type + " exception: " + e.getMessage());
                                                break;
                                            }
                                    }
                            }
                    }
            }
        } else {
            bDisabled = instance.isSleepByPowerButtonDisabled();
        }
        if (SystemClock.uptimeMillis() - startTime > 100) {
            Log.w(TAG, "disallowOp timeout. type:" + type);
        }
        return bDisabled;
    }

    public static boolean disallowOp(int type, String param) {
        IHwDeviceManager instance = getImplObject();
        if (instance == null) {
            Log.w(TAG, "Can not get the instance of IHwDeviceManager object.");
            return false;
        }
        long startTime = SystemClock.uptimeMillis();
        boolean bDisabled = false;
        if (type == 3) {
            bDisabled = instance.isPersistentApp(param);
        } else if (type == 4) {
            bDisabled = instance.isDisallowedRunningApp(param);
        } else if (type == 5) {
            bDisabled = instance.isDisallowedUninstallPackage(param);
        } else if (type == 7) {
            bDisabled = !instance.isAllowedInstallPackage(param);
        } else if (type == 53) {
            bDisabled = instance.isAccessibilityServicesWhiteList(param);
        } else if (type == 70) {
            bDisabled = instance.isWifiUnavailableTipsDisabled(param);
        } else if (type == 18) {
            bDisabled = instance.isDisabledDeactivateMdmPackage(param);
        } else if (type != 19) {
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
        } else {
            bDisabled = instance.isDisallowedInstallPackage(param);
        }
        if (SystemClock.uptimeMillis() - startTime > 100) {
            Log.w(TAG, "disallowOp timeout. type:" + type + ", param:" + param);
        }
        return bDisabled;
    }

    public static boolean disallowOp(Intent installSource) {
        IHwDeviceManager instance = getImplObject();
        if (instance == null) {
            Log.w(TAG, "Can not get the instance of IHwDeviceManager object.");
            return false;
        }
        long startTime = SystemClock.uptimeMillis();
        boolean bDisabled = false;
        try {
            bDisabled = !instance.isIntentFromAllowedInstallSource(installSource);
        } catch (Exception e) {
            Log.e(TAG, "Disallow operation " + installSource.getAction() + " exception: " + e.getMessage());
        }
        if (SystemClock.uptimeMillis() - startTime > 100) {
            Log.w(TAG, "disallowOp timeout");
        }
        return bDisabled;
    }

    public static List<String> getList(int type) {
        IHwDeviceManager instance = getImplObject();
        if (instance == null) {
            Log.w(TAG, "Can not get the instance of IHwDeviceManager object.");
            return null;
        } else if (type == 9) {
            return instance.getNetworkAccessWhitelist();
        } else {
            if (type == 22) {
                return instance.getSuperWhiteListApp();
            }
            if (type == 55) {
                return instance.getTaskLockAppList();
            }
            switch (type) {
                case 62:
                    return instance.getNetworkAccessList(POLICY_NETWORK_WHITE_IP_LIST);
                case 63:
                    return instance.getNetworkAccessList(POLICY_NETWORK_BLACK_IP_LIST);
                case 64:
                    return instance.getNetworkAccessList(POLICY_NETWORK_WHITE_DOMAIN_LIST);
                case 65:
                    try {
                        return instance.getNetworkAccessList(POLICY_NETWORK_BLACK_DOMAIN_LIST);
                    } catch (Exception e) {
                        Log.e(TAG, "Get list " + type + " exception: " + e.getMessage());
                        return null;
                    }
                default:
                    return null;
            }
        }
    }

    public static String getString(int type) {
        IHwDeviceManager instance = getImplObject();
        if (instance == null) {
            Log.w(TAG, "Can not get the instance of IHwDeviceManager object.");
            return null;
        } else if (type != 34) {
            return null;
        } else {
            return instance.getSingleApp();
        }
    }

    public static boolean mdmDisallowOp(int type, Intent intent) {
        IHwDeviceManager instance = getImplObject();
        if (instance == null) {
            Log.w(TAG, "Can not get the instance of IHwDeviceManager object.");
            return false;
        }
        long startTime = SystemClock.uptimeMillis();
        boolean bDisabled = false;
        if (type == 20) {
            bDisabled = instance.isScreenCaptureDisabled();
        } else if (type == 21) {
            try {
                bDisabled = instance.isApplicationDisabled(intent);
            } catch (Exception e) {
                Log.e(TAG, "Disallow operation " + type + " exception: " + e.getMessage());
            }
        }
        if (SystemClock.uptimeMillis() - startTime > 100) {
            Log.w(TAG, "mdmDisallowOp timeout. type:" + type);
        }
        return bDisabled;
    }
}
