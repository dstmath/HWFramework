package android.hdm;

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
    public static final int FIX_APP_RUNTIME_PERMISSION_LIST = 56;
    public static final int GET_SINGLE_APP = 34;
    public static final int IS_ACCESSSIBILITY_SERVICES_WHITELIST = 53;
    public static final int IS_ADBORSDCARD_INSTALL_RESTRICTED = 6;
    public static final int IS_ALLOWED_INSTALL_PACKAGE = 7;
    public static final int IS_DISABLED_DEACTIVATE_MDM_PACKAGE = 18;
    public static final int IS_DISABLED_FINGERPRINT_AUTHENTICATION = 50;
    public static final int IS_DISABLED_INSTALL_SOURCE = 27;
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
    public static final int IS_DISABLE_VOICE_ASSIST_BUTTON = 72;
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
    public static final int SLEEP_TIME_INTERVAL_AFTER_POWER_ON = 73;
    public static final int SUPERWHITELIST_APP = 22;
    private static final String TAG = "HwDeviceManager";
    public static final int TASK_LOCK_APP_LIST = 55;
    private static IHwDeviceManager sInstance = null;

    public interface IHwDeviceManager {
        boolean disallowOp(int i);

        boolean disallowOp(int i, Intent intent);

        boolean disallowOp(int i, String str);

        List<String> getList(int i);

        String getString(int i);
    }

    private static IHwDeviceManager getImplObject() {
        if (sInstance == null) {
            sInstance = HwDeviceMangerFactory.loadFactory().getHuaweiDevicePolicyManager();
        }
        return sInstance;
    }

    public static boolean disallowOp(int type) {
        IHwDeviceManager instance = getImplObject();
        if (instance != null) {
            return instance.disallowOp(type);
        }
        Log.w(TAG, "Can not get the instance of IHwDeviceManager object.");
        return false;
    }

    public static boolean disallowOp(int type, String param) {
        IHwDeviceManager instance = getImplObject();
        if (instance != null) {
            return instance.disallowOp(type, param);
        }
        Log.w(TAG, "Can not get the instance of IHwDeviceManager object.");
        return false;
    }

    public static boolean disallowOp(Intent installSource) {
        IHwDeviceManager instance = getImplObject();
        if (instance != null) {
            return instance.disallowOp(27, installSource);
        }
        Log.w(TAG, "Can not get the instance of IHwDeviceManager object.");
        return false;
    }

    public static List<String> getList(int type) {
        IHwDeviceManager instance = getImplObject();
        if (instance != null) {
            return instance.getList(type);
        }
        Log.w(TAG, "Can not get the instance of IHwDeviceManager object.");
        return null;
    }

    public static String getString(int type) {
        IHwDeviceManager instance = getImplObject();
        if (instance != null) {
            return instance.getString(type);
        }
        Log.w(TAG, "Can not get the instance of IHwDeviceManager object.");
        return null;
    }

    public static boolean mdmDisallowOp(int type, Intent intent) {
        IHwDeviceManager instance = getImplObject();
        if (instance == null) {
            Log.w(TAG, "Can not get the instance of IHwDeviceManager object.");
            return false;
        } else if (intent == null) {
            return instance.disallowOp(type);
        } else {
            return instance.disallowOp(type, intent);
        }
    }
}
