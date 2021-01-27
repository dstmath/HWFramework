package com.android.server.wifipro;

import android.app.ActivityManager;
import android.common.HwFrameworkFactory;
import android.content.ComponentName;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpResults;
import android.net.LinkAddress;
import android.net.NetworkInfo;
import android.net.NetworkUtils;
import android.net.StaticIpConfiguration;
import android.net.booster.IHwCommBoosterServiceManager;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.CellLocation;
import android.telephony.HwTelephonyManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.location.HwLogRecordManager;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.aod.AodThemeConst;
import com.huawei.netassistant.service.INetAssistantService;
import huawei.cust.HwCfgFilePolicy;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WifiProCommonUtils implements IHwWifiProCommonUtilsEx {
    private static final int ACTION_CONTROL_HIDATA_MPLINK = 0;
    private static final String ADD_SPLITER_LEN = "(.{2})";
    private static final Object BACKGROUND_LOCK = new Object();
    private static final String BCM_CHIP_4345 = "4345";
    private static final String BCM_CHIP_4359 = "4359";
    public static final String BROWSER_LAUNCHED_BY_WIFI_PORTAL = "wifi_portal";
    public static final String BROWSER_LAUNCH_FROM = "launch_from";
    private static final int BSSID_COMPARE_LENGTH = 11;
    private static final int BSSID_LENGTH_COMMON = 17;
    private static final int BSSID_LENGTH_HAVE_SPLITTER = 17;
    private static final int BSSID_LENGTH_NO_SPLITTER = 12;
    private static final int BSSID_LENGTH_SPECIAL = 12;
    private static final int BSSID_OFFSET_BEGIN = 3;
    private static final String BSSID_SPLITTER = "$1:";
    private static final int BSSID_START_INDEX = 0;
    private static final String CFG_FILE_NAME = "/WifiProNetworkConfig.json";
    private static final String CFG_VER_DIR = "emcom/noncell";
    private static final String CHIPSET_TYPE_PROP = "ro.connectivity.sub_chiptype";
    public static final String COUNTRY_CODE_CN = "460";
    private static final String DEFAULT_CHIP_TYPE = "none";
    private static final int DEFAULT_INTELLIGENT_SWITCH_VALUE = 0;
    public static final int DNS_REACHALBE = 0;
    public static final int DNS_UNREACHALBE = -1;
    private static final int DUAL_CARD_STATE_ON = 1;
    private static final int ENABLE_NEW_SWITCH_ALGORITHM = 1;
    public static final int ENTERPRISE_HOTSPOT_THRESHOLD = 4;
    private static final int EXTRACT_HAVE_SPLITTER_LENGTH = 6;
    private static final int EXTRACT_NO_SPLITTER_LENGTH = 4;
    private static final int GET_APP_PACKAGE_WHITE_LIST = 1001;
    private static final String HISI_CHIP_1102A = "1102a";
    private static final String HISI_CHIP_1103 = "1103";
    private static final String HISI_CHIP_1105 = "1105";
    public static final int HISTORY_ITEM_INTERNET = 1;
    public static final int HISTORY_ITEM_NO_INTERNET = 0;
    public static final int HISTORY_ITEM_PORTAL = 2;
    private static final int HISTORY_ITEM_UNCHECKED = -1;
    public static final int HISTORY_TYPE_EMPTY = 103;
    public static final int HISTORY_TYPE_HAS_INTERNET_EVER = 104;
    public static final int HISTORY_TYPE_INTERNET = 100;
    public static final int HISTORY_TYPE_PORTAL = 102;
    public static final int HTPP_FOREVER_HTTP_REDIRECTED = 301;
    public static final int HTTP_MAX_REDIRECT = 399;
    public static final int HTTP_REACHALBE_GOOLE = 204;
    public static final int HTTP_REACHALBE_HOME = 200;
    public static final int HTTP_REDIRECTED = 302;
    public static final int HTTP_SEE_OTHERS = 303;
    public static final int HTTP_TEMP_REDIRECT = 307;
    public static final int HTTP_UNREACHALBE = 599;
    public static final String HUAWEI_SETTINGS = (IS_TV ? WIFI_SETTINGS_TV : WIFI_SETTINGS_PHONE);
    public static final String HUAWEI_SETTINGS_WLAN = (IS_TV ? WIFI_SETTINGS_ACTIVITY_TV : WIFI_SETTINGS_ACTIVITY_PHONE);
    private static final String HW_WIFI_SELF_CURING = "net.wifi.selfcuring";
    public static final int ID_PORTAL_AUTH_EXPIRATION_INFO = 909009072;
    public static final int ID_UPDATE_AUTO_OPEN_WIFI_FAILED_INFO = 909002064;
    public static final int ID_UPDATE_DUAL_BAND_WIFI_INFO = 909009065;
    public static final int ID_UPDATE_PORTAL_DETECT_STAT_INFO = 909002060;
    public static final int ID_UPDATE_PORTAL_LOAD_PAGE_FAILED = 909002062;
    public static final int ID_UPDATE_PORTAL_POPUP_BROWSER_FAILED = 909002061;
    public static final int ID_UPDATE_PORTAL_POPUP_OTHER_BROWSER_STAT_INFO = -1;
    public static final int ID_WIFIPRO_DISABLE_INFO = 909002066;
    public static final int ID_WIFI_NET_QUALITY_INFO = 909009015;
    public static final int ID_WIFI_USER_CLOSE_WIFI_STAT_INFO = 909002058;
    public static final int ID_WIFI_USER_CONNECT_OTHER_WIFI_STAT_INFO = 909002059;
    private static final int INDEX_CELL_ID = 0;
    private static final int INDEX_DOMAINS = 1;
    private static final int INDEX_FLAG = 4;
    private static final int INDEX_GATEWAY = 6;
    private static final int INDEX_IP_ADDRESS = 2;
    private static final int INDEX_NUM_EIGHT = 8;
    private static final int INDEX_NUM_FIVE = 5;
    private static final int INDEX_NUM_FOUR = 4;
    private static final int INDEX_NUM_SEVEN = 7;
    private static final int INDEX_NUM_SIX = 6;
    private static final int INDEX_NUM_THREE = 3;
    private static final int INDEX_NUM_TWO = 2;
    private static final int INDEX_PREFLENGTH = 3;
    private static final int INDEX_SCOPE = 5;
    private static final String INTELLIGENCE_CARD_SETTING_DB = "intelligence_card_switch";
    private static final String INTERNET_HISTORY_INIT = "-1/-1/-1/-1/-1/-1/-1/-1/-1/-1";
    private static final int INVALID = -1;
    private static final int INVALID_CELL_ID = -1;
    private static final int INVALID_SIGNAL_STRENGTH = 99;
    private static final int IPV4_ADDRESS_LEN = 4;
    public static final boolean IS_TV = ("tv".equals(SystemProperties.get("ro.build.characteristics", AppActConstant.VALUE_DEFAULT)) || "mobiletv".equals(SystemProperties.get("ro.build.characteristics", AppActConstant.VALUE_DEFAULT)));
    public static final String KEY_MGMT_OWE = "OWE";
    public static final String KEY_MGMT_QUALCOMM_WAPI_CERT = "QUALCOMM_WAPI_CERT";
    public static final String KEY_MGMT_QUALCOMM_WAPI_PSK = "QUALCOMM_WAPI_PSK";
    public static final String KEY_MGMT_SAE = "SAE";
    public static final String KEY_MGMT_SUITE_B = "SUITE_B_192";
    public static final String KEY_MGMT_WAPI_CERT = "WAPI_CERT";
    public static final String KEY_MGMT_WAPI_PSK = "WAPI_PSK";
    public static final String KEY_PORTAL_CONFIG_KEY = "portal_config_key";
    public static final String KEY_PORTAL_DETECT_STAT_INFO = "portal_detect_stat_info";
    public static final String KEY_PORTAL_FIRST_DETECT = "portal_first_detect";
    public static final String KEY_PORTAL_HTTP_RESP_CODE = "portal_http_resp_code";
    public static final String KEY_PORTAL_REDIRECTED_URL = "portal_redirected_url";
    public static final String KEY_PROP_LOCALE = "ro.product.locale.region";
    private static final String KEY_PROP_PLATFORM = "ro.board.platform";
    public static final String KEY_WIFIPRO_MANUAL_CONNECT = "wifipro_manual_connect_ap";
    private static final String KEY_WIFI_PRO_PROPERTY = "ro.config.hw_wifipro_feature";
    public static final String KEY_WIFI_PRO_SWITCH = "smart_network_switching";
    private static final String KEY_WIFI_SECURE = "wifi_cloud_security_check";
    private static final int MAX_PHONE_ID = 2;
    private static final int MIN_SUB_ID = 0;
    public static final String[] NON_OPEN_PORTALS = {"\"0000docomo\"WPA_PSK"};
    private static final String PACKAGE_NAME = "PackageName";
    private static final String PLATFORM_980 = "kirin980";
    public static final int PORTAL_CONNECTED_AND_UNLOGIN = 1;
    public static final int PORTAL_DISCONNECTED_OR_LOGIN = 0;
    public static final String PORTAL_NETWORK_FLAG = "HW_WIFI_PORTAL_FLAG";
    public static final int PORTAL_USER_SELECT_FROM_NOTIFICATION = 2;
    public static final long RECHECK_DELAYED_MS = 3600000;
    public static final float RECOVERY_PERCENTAGE = 0.8f;
    public static final int RESP_CODE_ABNORMAL_SERVER = 604;
    public static final int RESP_CODE_CONN_RESET = 606;
    public static final int RESP_CODE_GATEWAY = 602;
    public static final int RESP_CODE_INVALID_URL = 603;
    public static final int RESP_CODE_REDIRECTED_HOST_CHANGED = 605;
    public static final int RESP_CODE_TIMEOUT = 600;
    public static final int RESP_CODE_UNSTABLE = 601;
    private static final int RIGHT_IP_ARRAY_LENGTH = 4;
    public static final int SCE_STATE_IDLE = 0;
    public static final int SCE_STATE_REASSOC = 101;
    public static final int SCE_STATE_RECONNECT = 103;
    public static final int SCE_STATE_RESET = 102;
    private static final int SET_DEFAULT_DATA_SUB_ID = 1002;
    public static final int SLOT_0 = 0;
    public static final int SLOT_1 = 1;
    public static final int SLOT_2 = 2;
    private static final int SSID_MAX_LENGTH = 32;
    private static final int SUCCESS = 0;
    private static final String TAG = "WifiProCommonUtils";
    private static final int TELEPHONY_INVALID_SIGNAL_VALUE = Integer.MAX_VALUE;
    public static final String VALID_PKGNAME_WIFIPRO = "com.huawei.hwwifiproservice";
    public static final int VERSION_COMMERCIAL_USER = 1;
    public static final int VERSION_DOMESTIC_BETA_USER = 3;
    private static final int WIFIPRO_ENABLE_ROBOT_ALGORITHM = 1024;
    private static final int WIFIPRO_FEATURE_NOT_CONFIG = 65535;
    private static final int WIFIPRO_TO_CELL_STRONG_SIGNAL_CTL = 3072;
    public static final int WIFI_2G_BAND_SCORE = 20;
    public static final int WIFI_5G_BAND_SCORE = 50;
    private static final String WIFI_BACKGROUND_CONN_TAG = "wifipro_recommending_access_points";
    public static final int WIFI_CATEGORY_WIFI5_SCORE = 1;
    public static final int WIFI_CATEGORY_WIFI6 = 2;
    public static final int WIFI_CATEGORY_WIFI6_160M_SCORE = 3;
    public static final int WIFI_CATEGORY_WIFI6_NONE_160M_SCORE = 2;
    public static final int WIFI_CATEGORY_WIFI6_PLUS = 3;
    public static final int WIFI_CATEGORY_WIFI6_PLUS_160M_SCORE = 5;
    public static final int WIFI_CATEGORY_WIFI6_PLUS_NONE_160M_SCORE = 4;
    public static final int WIFI_CATEGORY_WIFI6_PRO = 3;
    public static final int WIFI_DEFAULT_SCORE = -1;
    public static final int WIFI_LEVEL_FOUR = 4;
    public static final int WIFI_LEVEL_FOUR_SCORE = 80;
    public static final int WIFI_LEVEL_THREE = 3;
    public static final int WIFI_LEVEL_THREE_SCORE = 60;
    public static final int WIFI_LEVEL_TWO = 2;
    public static final int WIFI_LEVEL_TWO_SCORE = 20;
    public static final int WIFI_PRO_SOFT_RECONNECT = 104;
    private static final int WIFI_PRO_SWIFCH_OFF = 0;
    private static final String WIFI_PRO_SWITCH = "WifiProSwich";
    public static final String WIFI_SETTINGS_ACTIVITY_PHONE = "com.android.settings.Settings$WifiSettingsActivity";
    public static final String WIFI_SETTINGS_ACTIVITY_TV = "com.huawei.homevision.settings.network.wifi.WifiActivity";
    public static final String WIFI_SETTINGS_PHONE = "com.android.settings";
    public static final String WIFI_SETTINGS_TV = "com.huawei.homevision.settings";
    private static final int ZERO_SIGNAL_STRENGTH = 0;
    private static WifiProCommonUtils sWifiProCommonUtils = null;

    public static synchronized WifiProCommonUtils getDefault() {
        WifiProCommonUtils wifiProCommonUtils;
        synchronized (WifiProCommonUtils.class) {
            if (sWifiProCommonUtils == null) {
                sWifiProCommonUtils = new WifiProCommonUtils();
            }
            wifiProCommonUtils = sWifiProCommonUtils;
        }
        return wifiProCommonUtils;
    }

    public boolean hwIsWifiProSwitchOn(Context context) {
        return isWifiProSwitchOn(context);
    }

    public boolean hwIsAllowWifiConfigRecovery(String internetHistory) {
        return allowWifiConfigRecovery(internetHistory);
    }

    public static boolean isWifiProSwitchOn(Context context) {
        return context != null && Settings.System.getInt(context.getContentResolver(), "smart_network_switching", 0) == 1;
    }

    public static boolean isWifiProPropertyEnabled(Context context) {
        return context != null && AppActConstant.VALUE_TRUE.equalsIgnoreCase(Settings.Global.getString(context.getContentResolver(), "hw_wifipro_enable"));
    }

    public static boolean isWifiProLitePropertyEnabled(Context context) {
        return context != null && "lite".equalsIgnoreCase(Settings.Global.getString(context.getContentResolver(), "hw_wifipro_enable"));
    }

    public static WifiConfiguration getCurrentWifiConfig(WifiManager wifiManager) {
        if (wifiManager == null) {
            return null;
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        List<WifiConfiguration> configNetworks = wifiManager.getConfiguredNetworks();
        if (!(configNetworks == null || wifiInfo == null || !SupplicantState.isConnecting(wifiInfo.getSupplicantState()))) {
            for (int i = 0; i < configNetworks.size(); i++) {
                WifiConfiguration config = configNetworks.get(i);
                if (config.networkId == wifiInfo.getNetworkId() && config.networkId != -1) {
                    return config;
                }
            }
        }
        return null;
    }

    public static String getCurrentSsid(WifiManager wifiManager) {
        WifiInfo wifiInfo;
        if (wifiManager == null || (wifiInfo = wifiManager.getConnectionInfo()) == null || !SupplicantState.isConnecting(wifiInfo.getSupplicantState())) {
            return null;
        }
        return wifiInfo.getSSID();
    }

    public static String getCurrentBssid(WifiManager wifiManager) {
        WifiInfo wifiInfo;
        if (wifiManager == null || (wifiInfo = wifiManager.getConnectionInfo()) == null || !SupplicantState.isConnecting(wifiInfo.getSupplicantState())) {
            return null;
        }
        return wifiInfo.getBSSID();
    }

    public static int getCurrentRssi(WifiManager wifiManager) {
        WifiInfo wifiInfo;
        if (wifiManager == null || (wifiInfo = wifiManager.getConnectionInfo()) == null || !SupplicantState.isConnecting(wifiInfo.getSupplicantState())) {
            return -127;
        }
        return wifiInfo.getRssi();
    }

    public static int getBssidCounter(WifiConfiguration config, List<ScanResult> scanResults) {
        if (config == null || scanResults == null) {
            return 0;
        }
        String currentSsid = config.SSID;
        String configKey = config.configKey();
        if (TextUtils.isEmpty(currentSsid) || TextUtils.isEmpty(configKey)) {
            return 0;
        }
        int counter = 0;
        for (ScanResult nextResult : scanResults) {
            if (nextResult != null) {
                String scanSsid = "\"" + nextResult.SSID + "\"";
                String capabilities = nextResult.capabilities;
                if (currentSsid.equals(scanSsid) && isSameEncryptType(capabilities, configKey)) {
                    counter++;
                }
            }
        }
        return counter;
    }

    public static boolean isWifiConnected(WifiManager wifiManager) {
        WifiInfo wifiInfo;
        if (wifiManager == null || (wifiInfo = wifiManager.getConnectionInfo()) == null || wifiInfo.getSupplicantState() != SupplicantState.COMPLETED) {
            return false;
        }
        return true;
    }

    public static boolean isWifi5gConnected(WifiManager wifiManager) {
        WifiInfo wifiInfo;
        if (wifiManager == null || (wifiInfo = wifiManager.getConnectionInfo()) == null || !wifiInfo.is5GHz()) {
            return false;
        }
        return true;
    }

    public static boolean isWifiConnectedOrConnecting(WifiManager wifiManager) {
        WifiInfo wifiInfo;
        if (wifiManager == null || (wifiInfo = wifiManager.getConnectionInfo()) == null) {
            return false;
        }
        return SupplicantState.isConnecting(wifiInfo.getSupplicantState());
    }

    public static boolean isWifiConnectedActive(Context context) {
        NetworkInfo ni;
        if (context == null || (ni = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo()) == null || !ni.isConnected() || ni.getType() != 1) {
            return false;
        }
        return true;
    }

    public static boolean isWpaOrWpa2(WifiConfiguration config) {
        if (config == null) {
            return false;
        }
        int authType = config.allowedKeyManagement.cardinality() > 1 ? -1 : config.getAuthType();
        if (authType == 1 || authType == 4 || authType == 6 || authType == 11) {
            return true;
        }
        return false;
    }

    public static boolean isSae(WifiConfiguration config) {
        if (config == null) {
            return false;
        }
        if ((config.allowedKeyManagement.cardinality() > 1 ? -1 : config.getAuthType()) == 8) {
            return true;
        }
        return false;
    }

    public static boolean isWapi(WifiConfiguration config) {
        if (config == null) {
            return false;
        }
        switch (config.allowedKeyManagement.cardinality() > 1 ? -1 : config.getAuthType()) {
            case 16:
            case 17:
            case 18:
            case 19:
                return true;
            default:
                return false;
        }
    }

    public static boolean isEncrypted(WifiConfiguration config) {
        if (config == null) {
            return false;
        }
        if (isWpaOrWpa2(config) || isWapi(config) || isSae(config)) {
            return true;
        }
        return false;
    }

    public static boolean isEncryptedAuthType(int authType) {
        if (!(authType == 1 || authType == 4 || authType == 6 || authType == 8 || authType == 11)) {
            switch (authType) {
                case 16:
                case 17:
                case 18:
                case 19:
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    public static boolean isQueryActivityMatched(Context context, String activityName) {
        List<ActivityManager.RunningTaskInfo> runningTaskInfos;
        ComponentName cn;
        if (context == null || activityName == null || (runningTaskInfos = ((ActivityManager) context.getSystemService("activity")).getRunningTasks(1)) == null || runningTaskInfos.isEmpty() || (cn = runningTaskInfos.get(0).topActivity) == null || cn.getClassName() == null || !cn.getClassName().startsWith(activityName)) {
            return false;
        }
        return true;
    }

    public static boolean isManualConnecting(Context context) {
        if (context == null || Settings.System.getInt(context.getContentResolver(), KEY_WIFIPRO_MANUAL_CONNECT, 0) != 1) {
            return false;
        }
        return true;
    }

    public static boolean isInMonitorList(String input, String[] list) {
        if (!(input == null || list == null)) {
            for (String str : list) {
                if (input.equals(str)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isMobileDataOff(Context context) {
        if (context == null || Settings.Global.getInt(context.getContentResolver(), "mobile_data", 1) != 0) {
            return false;
        }
        return true;
    }

    public static boolean isMobileDataInactive(Context context) {
        NetworkInfo ni;
        if (context == null || (ni = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo()) == null || !ni.isConnected() || ni.getType() != 0) {
            return true;
        }
        return false;
    }

    public static boolean isCalling(Context context) {
        if (context == null) {
            return false;
        }
        int callState = ((TelephonyManager) context.getSystemService("phone")).getCallState();
        if (callState == 2 || callState == 1) {
            return true;
        }
        return false;
    }

    public static boolean isNoSimCard(Context context) {
        if (context == null) {
            return false;
        }
        TelephonyManager tm = (TelephonyManager) context.getSystemService("phone");
        if (tm.getSimState() == 1 || tm.getSimState() == 0) {
            return true;
        }
        return false;
    }

    public static boolean useOperatorOverSea() {
        String operator = TelephonyManager.getDefault().getNetworkOperator();
        if (operator == null || operator.length() <= 0 || operator.startsWith(COUNTRY_CODE_CN)) {
            return false;
        }
        return true;
    }

    public static boolean isOversea() {
        if (Locale.CHINA.getCountry().equalsIgnoreCase(SystemProperties.get(KEY_PROP_LOCALE))) {
            return false;
        }
        return true;
    }

    public static boolean allowRecheckForNoInternet(WifiConfiguration config, ScanResult scanResult, Context context) {
        boolean isAllowed = false;
        synchronized (BACKGROUND_LOCK) {
            if (config != null) {
                if (config.noInternetAccess && !allowWifiConfigRecovery(config.internetHistory) && config.internetRecoveryStatus == 3 && !isQueryActivityMatched(context, HUAWEI_SETTINGS_WLAN) && scanResult != null && ((scanResult.is24GHz() && scanResult.level >= -75) || (scanResult.is5GHz() && scanResult.level >= -72))) {
                    isAllowed = true;
                }
            }
        }
        return isAllowed;
    }

    public static void setBackgroundConnTag(Context context, boolean isBackground) {
        synchronized (BACKGROUND_LOCK) {
            if (context != null) {
                Settings.Secure.putInt(context.getContentResolver(), WIFI_BACKGROUND_CONN_TAG, isBackground ? 1 : 0);
            }
        }
    }

    public static String getProductLocale() {
        return SystemProperties.get(KEY_PROP_LOCALE, "");
    }

    public static boolean isWeakSingnalFastSwitchAllowed() {
        return !PLATFORM_980.equals(SystemProperties.get(KEY_PROP_PLATFORM, ""));
    }

    public static void setWifiSelfCureStatus(int state) {
        try {
            SystemProperties.set(HW_WIFI_SELF_CURING, String.valueOf(state));
        } catch (IllegalArgumentException e) {
            HwHiLog.e(TAG, false, "SystemProperties set RuntimeException.", new Object[0]);
        }
    }

    public static boolean isWifiSelfCuring() {
        return !String.valueOf(0).equals(SystemProperties.get(HW_WIFI_SELF_CURING, String.valueOf(0)));
    }

    public static int getSelfCuringState() {
        try {
            return Integer.parseInt(SystemProperties.get(HW_WIFI_SELF_CURING, String.valueOf(0)));
        } catch (NumberFormatException e) {
            HwHiLog.e(TAG, false, "getSelfCuringState failed", new Object[0]);
            return 0;
        }
    }

    public static boolean isWifiSecDetectOn(Context context) {
        if (context == null || Settings.Global.getInt(context.getContentResolver(), KEY_WIFI_SECURE, 0) != 1) {
            return false;
        }
        return true;
    }

    private static boolean isMatchedRequestByType(int itemValue, int numTarget, int numChecked, int[] items) {
        boolean isMatched = false;
        if (numChecked >= 1 && items.length > 0 && items[0] == itemValue) {
            isMatched = true;
        }
        if (!isMatched && numChecked == 2 && items.length > 1 && (items[0] == itemValue || items[1] == itemValue)) {
            isMatched = true;
        }
        if (isMatched || numChecked < 3 || ((float) numTarget) / ((float) numChecked) < 0.8f) {
            return isMatched;
        }
        return true;
    }

    public static boolean matchedRequestByHistory(String internetHistory, int type) {
        if (internetHistory == null || internetHistory.lastIndexOf(AodThemeConst.SPLASH) == -1) {
            return false;
        }
        String[] temp = internetHistory.split(AodThemeConst.SPLASH);
        int[] items = new int[temp.length];
        int numChecked = 0;
        int numHasInet = 0;
        int numPortal = 0;
        int numTarget = 0;
        for (int i = 0; i < temp.length; i++) {
            try {
                items[i] = Integer.parseInt(temp[i]);
                if (items[i] != -1) {
                    numChecked++;
                }
                if (items[i] == 1) {
                    numHasInet++;
                } else if (items[i] == 2) {
                    numPortal++;
                } else {
                    HwHiLog.e(TAG, false, "not expected", new Object[0]);
                }
            } catch (NumberFormatException e) {
                HwHiLog.e(TAG, false, "parse matchedRequestByHistory failed", new Object[0]);
            }
        }
        int itemValue = -1;
        if (type == 100) {
            itemValue = 1;
            numTarget = numHasInet;
        } else if (type == 102) {
            if (numPortal >= 1) {
                return true;
            }
            return false;
        } else if (type == 103) {
            if (numChecked == 0) {
                return true;
            }
            return false;
        } else if (type != 104) {
            HwHiLog.d(TAG, false, "no match type.", new Object[0]);
        } else if (numHasInet >= 1) {
            return true;
        } else {
            return false;
        }
        return isMatchedRequestByType(itemValue, numTarget, numChecked, items);
    }

    public static boolean isOpenType(WifiConfiguration config) {
        if (config == null || config.allowedKeyManagement.cardinality() > 1) {
            return false;
        }
        return config.getAuthType() == 0 || config.getAuthType() == 9;
    }

    public static boolean isOpenAndPortal(WifiConfiguration config) {
        return isOpenType(config) && config.portalNetwork;
    }

    public static boolean isOpenAndMaybePortal(WifiConfiguration config) {
        return isOpenType(config) && !config.noInternetAccess && matchedRequestByHistory(config.internetHistory, 103);
    }

    public static String parseHostByUrlLocation(String requestUrl) {
        int end;
        if (requestUrl != null) {
            int start = 0;
            if (requestUrl.startsWith("http://")) {
                start = 7;
            } else if (requestUrl.startsWith("https://")) {
                start = 8;
            }
            int end2 = requestUrl.indexOf(AodThemeConst.SPLASH, start);
            if (end2 == -1 || AodThemeConst.SPLASH.length() + end2 > requestUrl.length()) {
                end = requestUrl.indexOf("?", start);
            } else {
                int tmpEnd = requestUrl.substring(start, end2).indexOf("?", 0);
                end = tmpEnd != -1 ? tmpEnd + start : end2;
            }
            if (end != -1 && 0 <= end && AodThemeConst.SPLASH.length() + end <= requestUrl.length()) {
                return requestUrl.substring(0, end);
            }
        }
        return requestUrl;
    }

    public static boolean invalidUrlLocation(String location) {
        if (location == null) {
            return true;
        }
        if (location.startsWith("http://") || location.startsWith("https://")) {
            return false;
        }
        return true;
    }

    public static String dhcpResults2String(DhcpResults dhcpResults, int cellId) {
        if (dhcpResults == null || dhcpResults.ipAddress == null || dhcpResults.ipAddress.getAddress() == null || dhcpResults.dnsServers == null) {
            return null;
        }
        StringBuilder lastDhcpResults = new StringBuilder();
        lastDhcpResults.append(String.valueOf(cellId) + HwLogRecordManager.VERTICAL_SEPARATE);
        StringBuilder sb = new StringBuilder();
        String str = "";
        sb.append(dhcpResults.domains == null ? str : dhcpResults.domains);
        sb.append(HwLogRecordManager.VERTICAL_SEPARATE);
        lastDhcpResults.append(sb.toString());
        lastDhcpResults.append(dhcpResults.ipAddress.getAddress().getHostAddress() + HwLogRecordManager.VERTICAL_SEPARATE);
        lastDhcpResults.append(dhcpResults.ipAddress.getPrefixLength() + HwLogRecordManager.VERTICAL_SEPARATE);
        lastDhcpResults.append(dhcpResults.ipAddress.getFlags() + HwLogRecordManager.VERTICAL_SEPARATE);
        lastDhcpResults.append(dhcpResults.ipAddress.getScope() + HwLogRecordManager.VERTICAL_SEPARATE);
        StringBuilder sb2 = new StringBuilder();
        if (dhcpResults.gateway != null) {
            str = dhcpResults.gateway.getHostAddress();
        }
        sb2.append(str);
        sb2.append(HwLogRecordManager.VERTICAL_SEPARATE);
        lastDhcpResults.append(sb2.toString());
        Iterator it = dhcpResults.dnsServers.iterator();
        while (it.hasNext()) {
            lastDhcpResults.append(((InetAddress) it.next()).getHostAddress() + HwLogRecordManager.VERTICAL_SEPARATE);
        }
        return lastDhcpResults.toString();
    }

    private static InetAddress ipStrToInetAddress(String ipAddress) {
        byte[] ipAddrAarrys = new byte[4];
        String[] ipStrAarrys = ipAddress.split("\\.");
        if (ipStrAarrys.length != 4) {
            HwHiLog.e(TAG, false, "invalid IPv4 address length", new Object[0]);
            return null;
        }
        for (int i = 0; i < 4; i++) {
            try {
                ipAddrAarrys[i] = (byte) Integer.parseInt(ipStrAarrys[i]);
            } catch (NumberFormatException | UnknownHostException e) {
                HwHiLog.e(TAG, false, "Exception happens", new Object[0]);
                return null;
            }
        }
        return InetAddress.getByAddress(ipAddrAarrys);
    }

    public static StaticIpConfiguration dhcpResults2StaticIpConfig(String lastDhcpResults) {
        if (lastDhcpResults == null || lastDhcpResults.length() <= 0) {
            return null;
        }
        StaticIpConfiguration staticIpConfig = new StaticIpConfiguration();
        String[] dhcpResults = lastDhcpResults.split(HwLogRecordManager.VERTICAL_ESC_SEPARATE);
        InetAddress ipAddr = null;
        int prefLength = -1;
        int flag = -1;
        int scope = -1;
        for (int i = 0; i < dhcpResults.length; i++) {
            try {
                if (i != 0) {
                    if (i == 1) {
                        staticIpConfig.domains = dhcpResults[i];
                    } else if (i == 2) {
                        ipAddr = ipStrToInetAddress(dhcpResults[i]);
                    } else if (i == 3) {
                        prefLength = Integer.parseInt(dhcpResults[i]);
                    } else if (i == 4) {
                        flag = Integer.parseInt(dhcpResults[i]);
                    } else if (i == 5) {
                        scope = Integer.parseInt(dhcpResults[i]);
                    } else if (i == 6) {
                        staticIpConfig.gateway = ipStrToInetAddress(dhcpResults[i]);
                    } else {
                        InetAddress dnsServer = ipStrToInetAddress(dhcpResults[i]);
                        if (dnsServer != null) {
                            staticIpConfig.dnsServers.add(dnsServer);
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                HwHiLog.w(TAG, false, "Exception happens", new Object[0]);
            }
        }
        if (!(ipAddr == null || prefLength == -1 || staticIpConfig.gateway == null || staticIpConfig.dnsServers.size() <= 0)) {
            staticIpConfig.ipAddress = new LinkAddress(ipAddr, prefLength, flag, scope);
            return staticIpConfig;
        }
        return null;
    }

    public static String dhcpResults2Gateway(String lastDhcpResults) {
        if (lastDhcpResults == null || lastDhcpResults.length() <= 0) {
            return null;
        }
        try {
            String[] dhcpResults = lastDhcpResults.split(HwLogRecordManager.VERTICAL_ESC_SEPARATE);
            if (dhcpResults.length >= 7) {
                return InetAddress.getByName(dhcpResults[6]).toString();
            }
            return null;
        } catch (UnknownHostException e) {
            HwHiLog.e(TAG, false, "dhcpResults2Gateway UnknownHostException failed", new Object[0]);
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x006e A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x00c6  */
    public static boolean isAllowWifiSwitch(List<ScanResult> scanResults, List<WifiConfiguration> configNetworks, String currBssid, String currSsid, String currConfigKey, int rssiRequired) {
        boolean isSameConfigKey;
        if (scanResults != null) {
            if (scanResults.size() != 0) {
                if (configNetworks != null) {
                    if (configNetworks.size() != 0) {
                        for (ScanResult nextResult : scanResults) {
                            String scanSsid = "\"" + nextResult.SSID + "\"";
                            String scanResultEncrypt = nextResult.capabilities;
                            boolean isSameBssid = currBssid != null && currBssid.equals(nextResult.BSSID);
                            if (currSsid != null && currSsid.equals(scanSsid)) {
                                if (isSameEncryptType(scanResultEncrypt, currConfigKey)) {
                                    isSameConfigKey = true;
                                    if (isSameBssid) {
                                        if (!isSameConfigKey && nextResult.level >= rssiRequired) {
                                            for (WifiConfiguration nextConfig : configNetworks) {
                                                int disableReason = nextConfig.getNetworkSelectionStatus().getNetworkSelectionDisableReason();
                                                if ((!nextConfig.noInternetAccess || allowWifiConfigRecovery(nextConfig.internetHistory)) && disableReason <= 0 && !isOpenAndPortal(nextConfig) && !isOpenAndMaybePortal(nextConfig) && nextConfig.SSID != null && nextConfig.SSID.equals(scanSsid) && isSameEncryptType(scanResultEncrypt, nextConfig.configKey())) {
                                                    return true;
                                                }
                                            }
                                            continue;
                                        }
                                    }
                                }
                            }
                            isSameConfigKey = false;
                            if (isSameBssid) {
                            }
                        }
                        return false;
                    }
                }
                return false;
            }
        }
        return false;
    }

    public static boolean unreachableRespCodeByAndroid(int code) {
        return code != 204 && !isRedirectedRespCodeByGoogle(code);
    }

    public static boolean unreachableRespCode(int code) {
        return code == 599 || code == 600;
    }

    public static boolean httpUnreachableOrAbnormal(int code) {
        return code >= 599;
    }

    public static boolean isRedirectedRespCode(int respCode) {
        return respCode == 301 || respCode == 302 || respCode == 303 || respCode == 307;
    }

    public static boolean isRedirectedRespCodeByGoogle(int respCode) {
        if (respCode == 204 || respCode < 200 || respCode > 399) {
            return false;
        }
        return true;
    }

    public static boolean httpReachableOrRedirected(int code) {
        return code >= 200 && code <= 399;
    }

    public static boolean httpReachableHome(int code) {
        return code == 200;
    }

    public static int getReachableCode(boolean isGoogleServer) {
        if (isGoogleServer) {
            return HTTP_REACHALBE_GOOLE;
        }
        return 200;
    }

    public static boolean isEncryptionWep(String encryption) {
        return encryption.contains("WEP");
    }

    public static boolean isEncryptionPsk(String encryption) {
        return encryption.contains("PSK");
    }

    public static boolean isEncryptionEap(String encryption) {
        return encryption.contains("EAP");
    }

    public static boolean isEncryptionOwe(String encryption) {
        if (encryption == null) {
            return false;
        }
        return encryption.contains(KEY_MGMT_OWE);
    }

    public static boolean isEncryptionSae(String encryption) {
        if (encryption == null) {
            return false;
        }
        return encryption.contains(KEY_MGMT_SAE);
    }

    public static boolean isEncryptionWapi(String encryption) {
        if (encryption == null) {
            return false;
        }
        if (encryption.contains(KEY_MGMT_WAPI_PSK) || encryption.contains(KEY_MGMT_WAPI_CERT) || encryption.contains(KEY_MGMT_QUALCOMM_WAPI_PSK) || encryption.contains(KEY_MGMT_QUALCOMM_WAPI_CERT)) {
            return true;
        }
        return false;
    }

    public static boolean isNormalOpenNetwork(String encryption) {
        if (encryption != null && !isEncryptionWep(encryption) && !isEncryptionPsk(encryption) && !isEncryptionEap(encryption) && !isEncryptionOwe(encryption) && !isEncryptionSae(encryption) && !isEncryptionWapi(encryption)) {
            return true;
        }
        return false;
    }

    public static boolean isOpenNetwork(String encryption) {
        if (encryption == null) {
            return false;
        }
        if (isNormalOpenNetwork(encryption) || isEncryptionOwe(encryption)) {
            return true;
        }
        return false;
    }

    public static boolean isSameEncryptType(String encryptTypeA, String encryptTypeB) {
        if (encryptTypeA == null || encryptTypeB == null) {
            return false;
        }
        if (isEncryptionWep(encryptTypeA) && isEncryptionWep(encryptTypeB)) {
            return true;
        }
        if (isEncryptionSae(encryptTypeA) && isEncryptionSae(encryptTypeB)) {
            return true;
        }
        if (isEncryptionPsk(encryptTypeA) && isEncryptionPsk(encryptTypeB)) {
            return true;
        }
        if (isEncryptionEap(encryptTypeA) && isEncryptionEap(encryptTypeB)) {
            return true;
        }
        if (isEncryptionWapi(encryptTypeA) && isEncryptionWapi(encryptTypeB)) {
            return true;
        }
        if (isEncryptionOwe(encryptTypeA) && isEncryptionOwe(encryptTypeB)) {
            return true;
        }
        if (!isNormalOpenNetwork(encryptTypeA) || !isNormalOpenNetwork(encryptTypeB)) {
            return false;
        }
        return true;
    }

    public static boolean isLandscapeMode(Context context) {
        if (context == null || context.getResources() == null || context.getResources().getConfiguration() == null || context.getResources().getConfiguration().orientation != 2) {
            return false;
        }
        return true;
    }

    public static int getForegroundAppUid(Context context) {
        List<ActivityManager.RunningAppProcessInfo> lr;
        if (context == null || (lr = ((ActivityManager) context.getSystemService("activity")).getRunningAppProcesses()) == null) {
            return -1;
        }
        for (ActivityManager.RunningAppProcessInfo ra : lr) {
            if (ra.importance == 100) {
                return ra.uid;
            }
        }
        return -1;
    }

    public static String getPackageName(Context context, int uid) {
        if (uid == -1 || context == null) {
            return "total";
        }
        String name = context.getPackageManager().getNameForUid(uid);
        if (!TextUtils.isEmpty(name)) {
            return name;
        }
        return "unknown:" + uid;
    }

    public static String insertWifiConfigHistory(String strInternetHistory, int status) {
        if (strInternetHistory == null || strInternetHistory.lastIndexOf(AodThemeConst.SPLASH) == -1) {
            return INTERNET_HISTORY_INIT;
        }
        String newInternetHistory = String.valueOf(status) + AodThemeConst.SPLASH + strInternetHistory.substring(0, strInternetHistory.lastIndexOf(AodThemeConst.SPLASH));
        HwHiLog.d(TAG, false, "insert, newInternetHistory = %{public}s", new Object[]{newInternetHistory});
        return newInternetHistory;
    }

    public static String updateWifiConfigHistory(String strInternetHistory, int status) {
        if (strInternetHistory == null || strInternetHistory.lastIndexOf(AodThemeConst.SPLASH) == -1) {
            return INTERNET_HISTORY_INIT;
        }
        String newInternetHistory = String.valueOf(status) + AodThemeConst.SPLASH + strInternetHistory.substring(strInternetHistory.indexOf(AodThemeConst.SPLASH) + 1);
        HwHiLog.d(TAG, false, "update newInternetHistory = %{public}s", new Object[]{newInternetHistory});
        return newInternetHistory;
    }

    private static boolean isRecoveryByNumCheck(int numChecked, int[] items, int numHasInet) {
        boolean isAllowRecovery = false;
        if (numChecked >= 2) {
            if (items[0] != 1 && items[1] != 1) {
                return false;
            }
            isAllowRecovery = true;
            int i = 1;
            while (true) {
                if (i >= numChecked) {
                    break;
                } else if (items[i] != 1) {
                    isAllowRecovery = false;
                    break;
                } else {
                    i++;
                }
            }
        }
        if (!isAllowRecovery && numChecked >= 3 && items[1] == 1 && items[2] == 1) {
            isAllowRecovery = true;
        }
        if (isAllowRecovery || numChecked < 3 || ((float) numHasInet) / ((float) numChecked) < 0.8f) {
            return isAllowRecovery;
        }
        return true;
    }

    public static boolean allowWifiConfigRecovery(String internetHistory) {
        if (internetHistory == null || internetHistory.lastIndexOf(AodThemeConst.SPLASH) == -1) {
            HwHiLog.w(TAG, false, "allowWifiConfigRecovery, arg is invalid, internetHistory = null", new Object[0]);
            return false;
        }
        String[] temp = internetHistory.split(AodThemeConst.SPLASH);
        int[] items = new int[temp.length];
        int numChecked = 0;
        int numHasInet = 0;
        for (int i = 0; i < temp.length; i++) {
            try {
                items[i] = Integer.parseInt(temp[i]);
                if (items[i] != -1) {
                    numChecked++;
                }
                if (items[i] == 1) {
                    numHasInet++;
                }
            } catch (NumberFormatException e) {
                HwHiLog.w(TAG, false, "Broken network history: parse internetHistory failed.", new Object[0]);
            }
        }
        return isRecoveryByNumCheck(numChecked, items, numHasInet);
    }

    public static boolean isNetworkReachableByIcmp(String ipAddress, int timeout) {
        try {
            return Inet4Address.getByName(ipAddress).isReachable(timeout);
        } catch (IOException e) {
            HwHiLog.w(TAG, false, "Exception, Network is not Reachable by ICMP!", new Object[0]);
            return false;
        }
    }

    public static boolean isWifiOnly(Context context) {
        ConnectivityManager cm;
        if (context == null || (cm = (ConnectivityManager) context.getSystemService("connectivity")) == null || cm.isNetworkSupported(0)) {
            return false;
        }
        return true;
    }

    public static int getCurrenSignalLevel(WifiInfo wifiInfo) {
        if (wifiInfo != null) {
            return HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(wifiInfo.getFrequency(), wifiInfo.getRssi());
        }
        return 0;
    }

    public static int getSignalLevel(int frequency, int rssi) {
        return HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(frequency, rssi);
    }

    public static boolean isSsidSupportWiFi6(ScanResult scanResult) {
        if (scanResult == null) {
            return false;
        }
        if (scanResult.supportedWifiCategory == 2 || scanResult.supportedWifiCategory == 3) {
            return true;
        }
        return false;
    }

    public static int getCurrentCellId() {
        int phoneId;
        CellLocation cellLocation;
        int subId = HwTelephonyManager.getDefault().getPreferredDataSubscription();
        if (subId < 0 || (phoneId = SubscriptionManager.getPhoneId(subId)) < 0 || phoneId >= 2 || (cellLocation = HwTelephonyManager.getDefault().getCellLocation(phoneId)) == null) {
            return -1;
        }
        if (cellLocation instanceof CdmaCellLocation) {
            CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) cellLocation;
            int cellId = cdmaCellLocation.getBaseStationId();
            if (cellId < 0) {
                return cdmaCellLocation.getCid();
            }
            return cellId;
        } else if (cellLocation instanceof GsmCellLocation) {
            return ((GsmCellLocation) cellLocation).getCid();
        } else {
            return -1;
        }
    }

    public static boolean getAirplaneModeOn(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), "airplane_mode_on", 0) == 1;
    }

    public static String safeDisplayBssid(String sourceBssid) {
        if (sourceBssid == null) {
            return "null";
        }
        int len = sourceBssid.length();
        if (len == 12) {
            return sourceBssid.substring(0, 4) + "****" + sourceBssid.substring(len - 4, len);
        } else if (len != 17) {
            return "******";
        } else {
            return sourceBssid.substring(0, 6) + "**:**" + sourceBssid.substring(len - 6, len);
        }
    }

    public static String safeDisplayIpAddress(String ipAddress) {
        if (ipAddress == null) {
            return "null";
        }
        try {
            byte[] ipAddressArray = NetworkUtils.numericToInetAddress(ipAddress).getAddress();
            if (ipAddressArray.length == 4) {
                StringBuilder strBuilder = new StringBuilder();
                for (int index = 0; index < 3; index++) {
                    strBuilder.append((int) ipAddressArray[index]);
                    strBuilder.append(".");
                }
                strBuilder.append("***");
                return strBuilder.toString();
            }
        } catch (IllegalArgumentException e) {
            HwHiLog.e(TAG, false, "Not a numeric address", new Object[0]);
        }
        return "null";
    }

    public static boolean isWifiSwitchRobotAlgorithmEnabled() {
        if (SystemProperties.getInt(KEY_WIFI_PRO_PROPERTY, 65535) == 65535 || (SystemProperties.getInt(KEY_WIFI_PRO_PROPERTY, 65535) & 1024) == 1024) {
            return true;
        }
        return false;
    }

    public static boolean isWifi2CellInStrongSiganalEnabled() {
        int wifiProFeatureCtl = SystemProperties.getInt(KEY_WIFI_PRO_PROPERTY, 65535);
        if (wifiProFeatureCtl == 65535 || (wifiProFeatureCtl & WIFIPRO_TO_CELL_STRONG_SIGNAL_CTL) == WIFIPRO_TO_CELL_STRONG_SIGNAL_CTL) {
            return true;
        }
        return false;
    }

    public static boolean isValidSlotId(int slotId) {
        return slotId >= 0 && slotId < TelephonyManager.getDefault().getSimCount();
    }

    public static boolean isValidSubId(int subId) {
        if (subId >= 0) {
            return true;
        }
        return false;
    }

    public static int getMasterCardSlotId() {
        if (HwTelephonyManager.getDefault() == null) {
            HwHiLog.e(TAG, false, "getMasterCardSlotId fail", new Object[0]);
            return 0;
        }
        int defaultSlotId = HwTelephonyManager.getDefault().getDefault4GSlotId();
        if (isValidSlotId(defaultSlotId)) {
            return defaultSlotId;
        }
        HwHiLog.e(TAG, false, "invalid param, defaultSlotId=%{public}d", new Object[]{Integer.valueOf(defaultSlotId)});
        return 0;
    }

    public static int getMasterCardSubId() {
        int slotId = getMasterCardSlotId();
        int subId = convertSlotIdToSubId(slotId);
        HwHiLog.d(TAG, false, "getMasterCardSubId slotId=%{public}d, subId=%{public}d", new Object[]{Integer.valueOf(slotId), Integer.valueOf(subId)});
        return subId;
    }

    public static int getSlaveCardSlotId() {
        int slaveSlotId = 1;
        if (HwTelephonyManager.getDefault() == null) {
            HwHiLog.d(TAG, false, "getSlaveCardSlotId fail,HwTelephonyManager.getDefault() == null", new Object[0]);
            return 1;
        }
        int default4gSlotId = HwTelephonyManager.getDefault().getDefault4GSlotId();
        if (!isValidSlotId(default4gSlotId)) {
            HwHiLog.d(TAG, false, "getSlaveCardSlotId failed, default4gSlotId:%{public}d", new Object[]{Integer.valueOf(default4gSlotId)});
            return 1;
        }
        if (default4gSlotId != 0) {
            slaveSlotId = 0;
        }
        return slaveSlotId;
    }

    public static int getSlaveCardSubId() {
        int slotId = getSlaveCardSlotId();
        int subId = convertSlotIdToSubId(slotId);
        HwHiLog.d(TAG, false, "getSlaveCardSubId slotId=%{public}d, subId=%{public}d", new Object[]{Integer.valueOf(slotId), Integer.valueOf(subId)});
        return subId;
    }

    public static int getDefaultDataSubId() {
        int subId = SubscriptionManager.getDefaultDataSubscriptionId();
        HwHiLog.d(TAG, false, "getDefaultDataSubId subId=%{public}d", new Object[]{Integer.valueOf(subId)});
        return subId;
    }

    public static int convertSlotIdToSubId(int slotId) {
        if (!isValidSlotId(slotId)) {
            HwHiLog.e(TAG, false, "convertSlotIdToSubId, Invalid slotId=%{public}d", new Object[]{Integer.valueOf(slotId)});
            return -1;
        } else if (slotId == 2) {
            HwHiLog.e(TAG, false, "convertSlotIdToSubId, vsim slotId", new Object[0]);
            return -1;
        } else {
            int[] subIds = SubscriptionManagerEx.getSubId(slotId);
            if (subIds == null || subIds.length <= 0) {
                return -1;
            }
            return subIds[0];
        }
    }

    public static boolean isSignalValid(int signalVal) {
        if (signalVal == 99 || signalVal == 0 || signalVal == TELEPHONY_INVALID_SIGNAL_VALUE) {
            return false;
        }
        return true;
    }

    public static boolean isCustomerApplyTheTrafficPackage(TelephonyManager telephonyManager) {
        String imsi = getImsi(telephonyManager);
        if (TextUtils.isEmpty(imsi)) {
            HwHiLog.e(TAG, false, "isCustomerApplyTheTrafficPackage invalid imsi", new Object[0]);
            return false;
        }
        INetAssistantService netAssistantService = getNetAssistantService();
        if (netAssistantService != null) {
            try {
                if (netAssistantService.isUnlimitedDataSet(imsi)) {
                    HwHiLog.d(TAG, false, "user set UnlimitedDataSet", new Object[0]);
                    return true;
                }
            } catch (RemoteException e) {
                HwHiLog.e(TAG, false, "Exception no 1 happened in isCustomerApplyTheTrafficPackage", new Object[0]);
            } catch (SecurityException e2) {
                HwHiLog.e(TAG, false, "Exception no 2 happened in isCustomerApplyTheTrafficPackage", new Object[0]);
            }
        }
        return false;
    }

    public static boolean isDualCardStateOn(Context context) {
        int state = Settings.Global.getInt(context.getContentResolver(), INTELLIGENCE_CARD_SETTING_DB, 0);
        HwHiLog.d(TAG, false, "get smart card state=%{public}d", new Object[]{Integer.valueOf(state)});
        if (state == 1) {
            return true;
        }
        return false;
    }

    public static boolean isAdvancedChipUser() {
        String chipset = SystemProperties.get(CHIPSET_TYPE_PROP, DEFAULT_CHIP_TYPE);
        if (chipset == null || (!chipset.contains(BCM_CHIP_4345) && !chipset.contains(BCM_CHIP_4359) && !chipset.contains(HISI_CHIP_1103) && !chipset.contains(HISI_CHIP_1105))) {
            return false;
        }
        HwHiLog.d(TAG, false, "chipset type is advanced", new Object[0]);
        return true;
    }

    public static boolean isHiSiAdvancedChipUser() {
        String chipset = SystemProperties.get(CHIPSET_TYPE_PROP, DEFAULT_CHIP_TYPE);
        if (chipset == null || (!chipset.contains(HISI_CHIP_1103) && !chipset.contains(HISI_CHIP_1105))) {
            return false;
        }
        HwHiLog.d(TAG, false, "chipset type is hisi advanced", new Object[0]);
        return true;
    }

    public static boolean isHi1105Chip() {
        String chipset = SystemProperties.get(CHIPSET_TYPE_PROP, DEFAULT_CHIP_TYPE);
        if (chipset == null || !chipset.contains(HISI_CHIP_1105)) {
            return false;
        }
        HwHiLog.d(TAG, false, "chipset type 1105", new Object[0]);
        return true;
    }

    private static String getImsi(TelephonyManager telephonyManager) {
        int subId = getMasterCardSubId();
        if (telephonyManager != null && isValidSubId(subId)) {
            return telephonyManager.getSubscriberId(subId);
        }
        HwHiLog.e(TAG, false, "isExceedTrafficUsedThreshold mTelephonyManager == null or unValidSubId", new Object[0]);
        return "";
    }

    private static INetAssistantService getNetAssistantService() {
        IBinder binder = ServiceManager.getService("com.huawei.netassistant.service.netassistantservice");
        if (binder != null) {
            return INetAssistantService.Stub.asInterface(binder);
        }
        return null;
    }

    public static boolean isExceedTrafficUsedThreshold(TelephonyManager telephonyManager, float trafficUsedRateThreshold) {
        String imsi = getImsi(telephonyManager);
        if (TextUtils.isEmpty(imsi)) {
            HwHiLog.e(TAG, false, "isExceedTrafficUsedThreshold invalid imsi", new Object[0]);
            return false;
        }
        INetAssistantService netAssistantService = getNetAssistantService();
        if (netAssistantService != null) {
            try {
                long monthLimitBytes = netAssistantService.getMonthlyTotalBytes(imsi);
                if (monthLimitBytes == -1) {
                    HwHiLog.e(TAG, false, "the user has not subscribed to any traffic package", new Object[0]);
                    return false;
                }
                long monthUsedBytes = netAssistantService.getMonthMobileTotalBytes(imsi);
                if (monthLimitBytes > 0 && ((float) monthUsedBytes) / ((float) monthLimitBytes) >= trafficUsedRateThreshold) {
                    return true;
                }
            } catch (RemoteException e) {
                HwHiLog.e(TAG, false, "Exception no 1 happened in isExceedTrafficUsedThreshold", new Object[0]);
            } catch (SecurityException e2) {
                HwHiLog.e(TAG, false, "Exception no 2 happened in isExceedTrafficUsedThreshold", new Object[0]);
            }
        }
        return false;
    }

    public static HashMap<Integer, String> getAppInWhitelist() {
        HashMap<Integer, String> appWhitelist = new HashMap<>();
        IHwCommBoosterServiceManager hwCommBoosterServiceManager = HwFrameworkFactory.getHwCommBoosterServiceManager();
        if (hwCommBoosterServiceManager == null) {
            HwHiLog.e(TAG, false, "HwCommBoosterServiceManager is null", new Object[0]);
            return appWhitelist;
        }
        Bundle data = hwCommBoosterServiceManager.getBoosterPara(VALID_PKGNAME_WIFIPRO, 1001, new Bundle());
        if (data == null) {
            HwHiLog.e(TAG, false, "isInWhitelist data is null", new Object[0]);
            return appWhitelist;
        }
        try {
            if (data.getSerializable("TopAppHashMap") instanceof HashMap) {
                appWhitelist = (HashMap) data.getSerializable("TopAppHashMap");
            }
            if (appWhitelist == null) {
                HwHiLog.d(TAG, false, "appWhitelist is null", new Object[0]);
                return new HashMap<>();
            }
            HwHiLog.d(TAG, false, "isInWhitelist " + appWhitelist.size(), new Object[0]);
            return appWhitelist;
        } catch (ClassCastException e) {
            HwHiLog.e(TAG, false, "It is not desired hashmap.", new Object[0]);
        }
    }

    public static boolean setDefaultDataSub(int subId) {
        IHwCommBoosterServiceManager hwCommBoosterServiceManager = HwFrameworkFactory.getHwCommBoosterServiceManager();
        if (hwCommBoosterServiceManager == null) {
            HwHiLog.e(TAG, false, "HwCommBoosterServiceManager is null", new Object[0]);
            return false;
        }
        Bundle data = new Bundle();
        data.putInt("subId", subId);
        hwCommBoosterServiceManager.getBoosterPara(VALID_PKGNAME_WIFIPRO, 1002, data);
        return true;
    }

    public static int calculateScore(ScanResult scanResult) {
        int levelScore;
        int categoryScore;
        if (scanResult == null) {
            HwHiLog.d(TAG, false, "scanResult is null", new Object[0]);
            return -1;
        }
        int signalLevel = getSignalLevel(scanResult.frequency, scanResult.level);
        if (signalLevel == 2) {
            levelScore = 20;
        } else if (signalLevel == 3) {
            levelScore = 60;
        } else if (signalLevel != 4) {
            return -1;
        } else {
            levelScore = 80;
        }
        int bandScore = ScanResult.is5GHz(scanResult.frequency) ? 50 : 20;
        int i = scanResult.supportedWifiCategory;
        if (i != 2) {
            if (i != 3) {
                categoryScore = 1;
            } else if (scanResult.channelWidth == 3) {
                categoryScore = 5;
            } else {
                categoryScore = 4;
            }
        } else if (scanResult.channelWidth == 3) {
            categoryScore = 3;
        } else {
            categoryScore = 2;
        }
        return levelScore + bandScore + categoryScore;
    }

    public static boolean isCellNetworkClass2gOr3g(TelephonyManager telephonyManager) {
        if (telephonyManager == null) {
            HwHiLog.d(TAG, false, "TelephonyManager is null", new Object[0]);
            return false;
        }
        int type = telephonyManager.getNetworkType();
        if (TelephonyManager.getNetworkClass(type) != 1 && TelephonyManager.getNetworkClass(type) != 2) {
            return false;
        }
        HwHiLog.d(TAG, false, "Network is 2g or 3g", new Object[0]);
        return true;
    }

    public static boolean isNetworkType3G(TelephonyManager telephonyManager, int subId) {
        if (isValidSubId(subId) && TelephonyManager.getNetworkClass(telephonyManager.getNetworkType(subId)) == 2) {
            return true;
        }
        return false;
    }

    public static boolean isNetworkType2gOrUnknow(TelephonyManager telephonyManager, int subId) {
        if (!isValidSubId(subId)) {
            return false;
        }
        int type = telephonyManager.getNetworkType(subId);
        if (TelephonyManager.getNetworkClass(type) == 1 || TelephonyManager.getNetworkClass(type) == 0) {
            return true;
        }
        return false;
    }

    public static boolean isDomesticBetaUser() {
        return SystemProperties.getInt("ro.logsystem.usertype", 1) == 3;
    }

    public static boolean isDualBandAp(String currentBssid, String targetBssid) {
        if (currentBssid == null || targetBssid == null || currentBssid.equals(targetBssid)) {
            return false;
        }
        String bssid24gAp = currentBssid.toLowerCase(Locale.ROOT);
        String bssid5gAp = targetBssid.toLowerCase(Locale.ROOT);
        if ((bssid24gAp.length() == 17 || bssid24gAp.length() == 12) && (bssid5gAp.length() == 17 || bssid5gAp.length() == 12)) {
            if (bssid24gAp.length() == 12) {
                bssid24gAp = bssid24gAp.replaceAll(ADD_SPLITER_LEN, BSSID_SPLITTER).substring(0, 17);
            }
            if (bssid5gAp.length() == 12) {
                bssid5gAp = bssid5gAp.replaceAll(ADD_SPLITER_LEN, BSSID_SPLITTER).substring(0, 17);
            }
            return bssid24gAp.regionMatches(3, bssid5gAp, 3, 11);
        }
        HwHiLog.d(TAG, false, "isDualBandAP illegal bssid format", new Object[0]);
        return false;
    }

    private static void closeFileStream(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                HwHiLog.e(TAG, false, "close file IOException", new Object[0]);
            }
        }
    }

    private static String getJsonContent() {
        String res = "";
        String[] cfgFileInfo = HwCfgFilePolicy.getDownloadCfgFile(CFG_VER_DIR, "emcom/noncell/WifiProNetworkConfig.json");
        if (cfgFileInfo == null || cfgFileInfo.length == 0) {
            HwHiLog.e(TAG, false, "WifiPro Info config file is empty", new Object[0]);
            return res;
        }
        File targetFile = new File(cfgFileInfo[0]);
        if (!targetFile.isFile()) {
            HwHiLog.e(TAG, false, "WifiPro Info targetFile is not exist!", new Object[0]);
            return res;
        }
        InputStreamReader streamReader = null;
        BufferedReader bufferedReader = null;
        StringBuffer buffer = new StringBuffer();
        try {
            streamReader = new InputStreamReader(new FileInputStream(targetFile));
            bufferedReader = new BufferedReader(streamReader);
            while (true) {
                int code = bufferedReader.read();
                if (code == -1) {
                    break;
                }
                buffer.append((char) code);
            }
            res = buffer.toString();
        } catch (IOException e) {
            HwHiLog.e(TAG, false, "getJsonContent failed for IOException", new Object[0]);
        } catch (Throwable th) {
            closeFileStream(null);
            closeFileStream(null);
            throw th;
        }
        closeFileStream(bufferedReader);
        closeFileStream(streamReader);
        return res;
    }

    public static synchronized ArrayList<String> getWifiProBlacklist(String blacklistName) {
        synchronized (WifiProCommonUtils.class) {
            ArrayList<String> wifiProBlacklist = new ArrayList<>();
            String content = getJsonContent();
            if (TextUtils.isEmpty(content)) {
                HwHiLog.e(TAG, false, "WifiPro Apk WifiProConfigJson is empty", new Object[0]);
                return wifiProBlacklist;
            }
            try {
                JSONArray apkInfoJsonArray = new JSONObject(content).getJSONArray(blacklistName);
                int length = apkInfoJsonArray.length();
                for (int i = 0; i < length; i++) {
                    wifiProBlacklist.add(apkInfoJsonArray.getJSONObject(i).getString("PackageName"));
                }
            } catch (JSONException e) {
                HwHiLog.e(TAG, false, "wifipro get all rule failed for JSONException", new Object[0]);
            }
            return wifiProBlacklist;
        }
    }

    public static synchronized boolean getWifiProSwitchInfo() {
        synchronized (WifiProCommonUtils.class) {
            String content = getJsonContent();
            if (TextUtils.isEmpty(content)) {
                HwHiLog.e(TAG, false, "WifiPro Apk Info WifiProConfigJson is empty", new Object[0]);
                return true;
            }
            try {
                return new JSONObject(content).getInt(WIFI_PRO_SWITCH) != 0;
            } catch (JSONException e) {
                HwHiLog.e(TAG, false, "wifipro get switch info failed for JSONException", new Object[0]);
                return true;
            }
        }
    }
}
