package com.android.server.wifi.hwUtil;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.os.Build;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.wifi.HiCoexManager;
import com.android.server.wifi.HwWifiServiceFactory;
import com.android.server.wifi.WifiInjector;
import java.util.Locale;
import java.util.UUID;

public class WifiCommonUtils {
    public static final int CH64 = 64;
    public static final int CHANNEL_MAX_BAND_2GHZ = 13;
    private static final int CH_2G_MAX = 14;
    private static final int CH_2G_MIN = 1;
    private static final int CH_5G_BEGIN = 34;
    private static final int CH_5G_MAX = 165;
    public static final int CH_5G_MIN = 36;
    private static final int DEFAULT_CH = 0;
    private static final int DEVICE_NAME_DEFAULT_LENGTH = 64;
    public static final String DEVICE_WLAN = "WLAN";
    public static final String DEVICE_WLANP2P = "WLAN-P2P";
    public static final String EXTRA_THREE_STATE = "wifi_mode_state";
    private static final int FREQ_CH1 = 2412;
    public static final int FREQ_CH100 = 5500;
    private static final int FREQ_CH14 = 2484;
    public static final int FREQ_CH36 = 5180;
    public static final int FREQ_CH52 = 5260;
    public static final int FREQ_CH56 = 5280;
    public static final int FREQ_CH60 = 5300;
    public static final int FREQ_CH64 = 5320;
    private static final int FREQ_DIFF_PER_CH = 5;
    public static final String HW_SYSTEM_PERMISSION = "huawei.android.permission.HW_SIGNATURE_OR_SYSTEM";
    private static final int ID_BEGIN_INDEX = 0;
    private static final int ID_END_INDEX = 4;
    public static final boolean IS_ATT;
    public static final boolean IS_TV = ("tv".equals(SystemProperties.get("ro.build.characteristics", "default")) || "mobiletv".equals(SystemProperties.get("ro.build.characteristics", "default")));
    public static final boolean IS_VERIZON = ("389".equals(SystemProperties.get("ro.config.hw_opta", "0")) && "840".equals(SystemProperties.get("ro.config.hw_optb", "0")));
    private static final int MAX_5G_FREQ = 5825;
    private static final int MIN_5G_FREQ = 5170;
    public static final String PACKAGE_NAME_AIRSHARING = "com.huawei.android.airsharing";
    public static final String PACKAGE_NAME_ASSOCIATE_ASSISTANT = "com.huawei.associateassistant";
    public static final String PACKAGE_NAME_ASSOCIATE_SETTINGS = "com.android.settings";
    public static final String PACKAGE_NAME_FRAMEWORK = "android";
    public static final String PACKAGE_NAME_HIVIEW = "com.huawei.hiview";
    public static final String PACKAGE_NAME_INSTANTSHARE = "com.huawei.android.instantshare";
    public static final String PACKAGE_NAME_LBS = "com.huawei.lbs";
    public static final String PACKAGE_NAME_MIRACAST = "com.hisilicon.miracast";
    public static final String PACKAGE_NAME_NEARBY = "com.huawei.nearby";
    public static final String PACKAGE_NAME_PCASSISTANT = "com.huawei.pcassistant";
    public static final String PACKAGE_NAME_STEREOAUDIO = "com.huawei.waudio";
    public static final String PACKAGE_NAME_WIFI_SETTINGS_TV = "com.huawei.homevision.settings";
    public static final String SET_SSID_NAME = "set_hotspot_ssid_name";
    public static final String STATE_CONNECT_END = "CONNECT_END";
    public static final String STATE_CONNECT_START = "CONNECT_START";
    public static final String STATE_DISCONNECTED = "DISCONNECTED";
    private static final String TAG = "WifiCommonUtils";
    public static final String THREE_STATE_ACTION = "huawei.wifi.WIFI_MODE_STATE";
    public static final int THREE_STATE_MODE_DISABLED = 0;
    public static final int THREE_STATE_MODE_ENABLED = 1;
    public static final int THREE_STATE_MODE_INTERMEDIATE = 2;
    public static final int THREE_STATE_MODE_UNKNOWN = -1;
    private static final int UUID_BEGIN_INDEX = 24;
    private static final int UUID_END_INDEX = 28;
    public static final int WIFI_MODE_BIT_ACTION_FRWK = Integer.MIN_VALUE;
    public static final int WIFI_MODE_BIT_ALLOW_HWSHARE_CONN = 32;
    public static final int WIFI_MODE_BIT_DISABLE_P2P_CONN = 16;
    public static final int WIFI_MODE_BIT_ENABLE_WIFI = 8;
    public static final int WIFI_MODE_DISCONNECT_WIFI_MASK = 1;
    public static final int WIFI_MODE_INTERMEDIATE_STATE = 63;
    public static final int WIFI_MODE_REFUSE_AUTO_CONNECT_WIFI_MASK = 2;
    public static final int WIFI_MODE_RESET = 0;
    public static final int WIFI_MODE_SET_WIFI_STATUS_MASK = 7;
    public static final int WIFI_MODE_STOP_WIFI_SCAN_MASK = 4;
    public static final String WIFI_SETTINGS_ACTIVITY_PHONE = "com.android.settings.Settings$WifiSettingsActivity";
    public static final String WIFI_SETTINGS_ACTIVITY_TV = "com.huawei.homevision.settings.network.wifi.WifiActivity";
    private static String sDeviceInfo = "";
    private static String sStateInfo = "";

    static {
        boolean z = false;
        if ("07".equals(SystemProperties.get("ro.config.hw_opta", "0")) && "840".equals(SystemProperties.get("ro.config.hw_optb", "0"))) {
            z = true;
        }
        IS_ATT = z;
    }

    public static void notifyDeviceState(String device, String state, String extras) {
        HiCoexManager hiCoexManager;
        if (device == null || state == null) {
            Log.e(TAG, "parameter error");
            return;
        }
        if (DEVICE_WLAN.equals(device) && STATE_CONNECT_START.equals(state) && (hiCoexManager = HwWifiServiceFactory.getHiCoexManager()) != null) {
            hiCoexManager.notifyWifiConnecting(true);
        }
        if (sDeviceInfo.equals(device) && sStateInfo.equals(state)) {
            Log.i(TAG, "Do not need notify device state. Device = " + device + ", state = " + state);
        } else if (HwWifiServiceFactory.getHwTelphonyUtils().notifyDeviceState(device, state, "")) {
            sDeviceInfo = device;
            sStateInfo = state;
            Log.i(TAG, "Notify deviceState success. Device = " + device + ", state = " + state);
        } else {
            Log.e(TAG, "Notify deviceState failed. Device = " + device + ", state = " + state);
        }
    }

    public static int convertFrequencyToChannelNumber(int frequency) {
        if (frequency >= FREQ_CH1 && frequency <= FREQ_CH14) {
            return ((frequency - 2412) / 5) + 1;
        }
        if (frequency < MIN_5G_FREQ || frequency > MAX_5G_FREQ) {
            return 0;
        }
        return ((frequency - 5170) / 5) + 34;
    }

    public static int convertChannelToFrequency(int channel) {
        if (channel >= 1 && channel < 14) {
            return ((channel - 1) * 5) + FREQ_CH1;
        }
        if (channel == 14) {
            return FREQ_CH14;
        }
        if (channel < 36 || channel > CH_5G_MAX) {
            return -1;
        }
        return ((channel - 36) * 5) + FREQ_CH36;
    }

    public static boolean doesNotWifiConnectRejectByCust(WifiConfiguration.NetworkSelectionStatus status, String ssid, Context context) {
        if (context == null) {
            return false;
        }
        String custWifiCureBlackList = Settings.System.getString(context.getContentResolver(), "wifi_cure_black_list");
        String custAutoWifiPackageName = Settings.System.getString(context.getContentResolver(), "cust_auto_wifi_package_name");
        if (TextUtils.isEmpty(custWifiCureBlackList) || TextUtils.isEmpty(custAutoWifiPackageName) || TextUtils.isEmpty(ssid) || ssid.length() < 1 || status == null) {
            return false;
        }
        String compareSsid = ssid.substring(1, ssid.length() - 1);
        String disableName = status.getNetworkSelectionDisableName();
        if (!custWifiCureBlackList.contains(compareSsid) || status.isNetworkEnabled() || disableName == null || !disableName.equals(custAutoWifiPackageName)) {
            return false;
        }
        return true;
    }

    public static String getPersistedDeviceName(WifiInjector wifiInjector, Context context) {
        if (wifiInjector == null || context == null) {
            Log.e(TAG, "getPersistedDeviceName parameter error");
            return null;
        }
        String deviceName = getCustomDeviceName(wifiInjector.getFrameworkFacade().getStringSetting(context, "wifi_p2p_device_name"), context);
        if (deviceName != null) {
            return deviceName;
        }
        String deviceName2 = SystemProperties.get("ro.config.marketing_name");
        if (!TextUtils.isEmpty(deviceName2)) {
            return deviceName2;
        }
        String id = wifiInjector.getFrameworkFacade().getSecureStringSetting(context, "android_id");
        if (id == null || id.length() <= 4 || IS_ATT || IS_VERIZON) {
            return Build.MODEL;
        }
        return Build.MODEL + "_" + id.substring(0, 4);
    }

    public static String getSettingActivityName() {
        return IS_TV ? WIFI_SETTINGS_ACTIVITY_TV : WIFI_SETTINGS_ACTIVITY_PHONE;
    }

    private static String getCustomDeviceName(String deviceName, Context context) {
        String customDeviceName;
        if (context == null) {
            Log.e(TAG, "getCustomDeviceName parameter error");
            return null;
        } else if (!SystemProperties.getBoolean("ro.config.hw_wifi_bt_name", false) || !TextUtils.isEmpty(deviceName)) {
            return deviceName;
        } else {
            StringBuilder sb = new StringBuilder(64);
            String uuidStr = UUID.randomUUID().toString();
            String marketingName = SystemProperties.get("ro.config.marketing_name");
            if (!TextUtils.isEmpty(marketingName)) {
                sb.append(marketingName);
                sb.append("_");
                sb.append(uuidStr.substring(24, 28).toUpperCase(Locale.ROOT));
                customDeviceName = sb.toString();
            } else {
                sb.append("HUAWEI ");
                sb.append(Build.PRODUCT);
                sb.append("_");
                sb.append(uuidStr.substring(24, 28).toUpperCase(Locale.ROOT));
                customDeviceName = sb.toString();
            }
            Settings.Global.putString(context.getContentResolver(), "wifi_p2p_device_name", customDeviceName);
            return customDeviceName;
        }
    }
}
