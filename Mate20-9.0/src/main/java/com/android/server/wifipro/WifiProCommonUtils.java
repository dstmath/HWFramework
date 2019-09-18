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
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.CellLocation;
import android.telephony.HwTelephonyManager;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.location.HwLogRecordManager;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;

public class WifiProCommonUtils implements IHwWifiProCommonUtilsEx {
    public static final String BROWSER_LAUNCHED_BY_WIFI_PORTAL = "wifi_portal";
    public static final String BROWSER_LAUNCH_FROM = "launch_from";
    public static final String COUNTRY_CODE_CN = "460";
    public static final int DNS_REACHALBE = 0;
    public static final int DNS_UNREACHALBE = -1;
    public static final int ENTERPRISE_HOTSPOT_THRESHOLD = 4;
    public static final int HISTORY_ITEM_INTERNET = 1;
    public static final int HISTORY_ITEM_NO_INTERNET = 0;
    public static final int HISTORY_ITEM_PORTAL = 2;
    public static final int HISTORY_ITEM_UNCHECKED = -1;
    public static final int HISTORY_TYPE_EMPTY = 103;
    public static final int HISTORY_TYPE_HAS_INTERNET_EVER = 104;
    public static final int HISTORY_TYPE_INTERNET = 100;
    public static final int HISTORY_TYPE_PORTAL = 102;
    public static final int HTTP_REACHALBE_GOOLE = 204;
    public static final int HTTP_REACHALBE_HOME = 200;
    public static final int HTTP_REDIRECTED = 302;
    public static final int HTTP_UNREACHALBE = 599;
    public static final String HUAWEI_SETTINGS = "com.android.settings";
    public static final String HUAWEI_SETTINGS_WLAN = "com.android.settings.Settings$WifiSettingsActivity";
    private static final String HW_WIFI_SELF_CURING = "net.wifi.selfcuring";
    public static final int ID_PORTAL_AUTH_EXPIRATION_INFO = 909009072;
    public static final int ID_UPDATE_AUTO_OPEN_WIFI_FAILED_INFO = 909002064;
    public static final int ID_UPDATE_DUAL_BAND_FAILED_INFO = 909002063;
    public static final int ID_UPDATE_DUAL_BAND_WIFI_INFO = 909009065;
    public static final int ID_UPDATE_PORTAL_DETECT_STAT_INFO = 909002060;
    public static final int ID_UPDATE_PORTAL_LOAD_PAGE_FAILED = 909002062;
    public static final int ID_UPDATE_PORTAL_POPUP_BROWSER_FAILED = 909002061;
    public static final int ID_UPDATE_PORTAL_POPUP_OTHER_BROWSER_STAT_INFO = -1;
    public static final int ID_WIFIPRO_DISABLE_INFO = 909002066;
    public static final int ID_WIFI_SWITCH_MOBILE_STAT_INFO = 909002057;
    public static final int ID_WIFI_USER_CLOSE_WIFI_STAT_INFO = 909002058;
    public static final int ID_WIFI_USER_CONNECT_OTHER_WIFI_STAT_INFO = 909002059;
    public static final String INTERNET_HISTORY_INIT = "-1/-1/-1/-1/-1/-1/-1/-1/-1/-1";
    public static final String KEY_PORTAL_CONFIG_KEY = "portal_config_key";
    public static final String KEY_PORTAL_DETECT_STAT_INFO = "portal_detect_stat_info";
    public static final String KEY_PORTAL_FIRST_DETECT = "portal_first_detect";
    public static final String KEY_PORTAL_HTTP_RESP_CODE = "portal_http_resp_code";
    public static final String KEY_PORTAL_REDIRECTED_URL = "portal_redirected_url";
    public static final String KEY_PROP_LOCALE = "ro.product.locale.region";
    public static final String KEY_WIFIPRO_MANUAL_CONNECT = "wifipro_manual_connect_ap";
    public static final String KEY_WIFI_PRO_SWITCH = "smart_network_switching";
    private static final String KEY_WIFI_SECURE = "wifi_cloud_security_check";
    public static final String[] NON_OPEN_PORTALS = {"\"0000docomo\"WPA_PSK"};
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
    public static final int SCE_STATE_IDLE = 0;
    public static final int SCE_STATE_REASSOC = 101;
    public static final int SCE_STATE_RECONNECT = 103;
    public static final int SCE_STATE_RESET = 102;
    private static final String WIFI_BACKGROUND_CONN_TAG = "wifipro_recommending_access_points";
    public static final int WIFI_PRO_SOFT_RECONNECT = 104;
    private static final Object mBackgroundLock = new Object();
    private static WifiProCommonUtils mWifiProCommonUtils = null;

    public boolean hwIsWifiProSwitchOn(Context context) {
        return isWifiProSwitchOn(context);
    }

    public boolean hwIsAllowWifiConfigRecovery(String internetHistory) {
        return allowWifiConfigRecovery(internetHistory);
    }

    public static synchronized WifiProCommonUtils getDefault() {
        WifiProCommonUtils wifiProCommonUtils;
        synchronized (WifiProCommonUtils.class) {
            if (mWifiProCommonUtils == null) {
                mWifiProCommonUtils = new WifiProCommonUtils();
            }
            wifiProCommonUtils = mWifiProCommonUtils;
        }
        return wifiProCommonUtils;
    }

    public static boolean isWifiProSwitchOn(Context context) {
        return context != null && Settings.System.getInt(context.getContentResolver(), "smart_network_switching", 0) == 1;
    }

    public static boolean isWifiProPropertyEnabled(Context context) {
        return context != null && "true".equalsIgnoreCase(Settings.Global.getString(context.getContentResolver(), "hw_wifipro_enable"));
    }

    public static boolean isWifiProLitePropertyEnabled(Context context) {
        return context != null && "lite".equalsIgnoreCase(Settings.Global.getString(context.getContentResolver(), "hw_wifipro_enable"));
    }

    public static WifiConfiguration getCurrentWifiConfig(WifiManager wifiManager) {
        if (wifiManager != null) {
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
        }
        return null;
    }

    public static String getCurrentSsid(WifiManager wifiManager) {
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null && SupplicantState.isConnecting(wifiInfo.getSupplicantState())) {
                return wifiInfo.getSSID();
            }
        }
        return null;
    }

    public static String getCurrentBssid(WifiManager wifiManager) {
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null && SupplicantState.isConnecting(wifiInfo.getSupplicantState())) {
                return wifiInfo.getBSSID();
            }
        }
        return null;
    }

    public static int getCurrentRssi(WifiManager wifiManager) {
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null && SupplicantState.isConnecting(wifiInfo.getSupplicantState())) {
                return wifiInfo.getRssi();
            }
        }
        return -127;
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
        for (int i = 0; i < scanResults.size(); i++) {
            String scanSsid = "\"" + scanResults.get(i).SSID + "\"";
            String capabilities = scanResults.get(i).capabilities;
            if (currentSsid.equals(scanSsid) && isSameEncryptType(capabilities, configKey)) {
                counter++;
            }
        }
        return counter;
    }

    public static boolean isWifiConnected(WifiManager wifiManager) {
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null && wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                return true;
            }
        }
        return false;
    }

    public static boolean isWifi5GConnected(WifiManager wifiManager) {
        boolean z = false;
        if (wifiManager == null) {
            return false;
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null && wifiInfo.is5GHz()) {
            z = true;
        }
        return z;
    }

    public static boolean isWifiConnectedOrConnecting(WifiManager wifiManager) {
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                return SupplicantState.isConnecting(wifiInfo.getSupplicantState());
            }
        }
        return false;
    }

    public static boolean isWifiConnectedActive(Context context) {
        if (context != null) {
            NetworkInfo ni = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
            if (ni != null && ni.isConnected() && ni.getType() == 1) {
                return true;
            }
        }
        return false;
    }

    public static boolean isWpaOrWpa2(WifiConfiguration config) {
        boolean z = false;
        if (config == null) {
            return false;
        }
        int authType = config.allowedKeyManagement.cardinality() > 1 ? -1 : config.getAuthType();
        if (authType == 1 || authType == 4) {
            z = true;
        }
        return z;
    }

    public static boolean isQueryActivityMatched(Context context, String activityName) {
        if (!(context == null || activityName == null)) {
            List<ActivityManager.RunningTaskInfo> runningTaskInfos = ((ActivityManager) context.getSystemService("activity")).getRunningTasks(1);
            if (runningTaskInfos != null && !runningTaskInfos.isEmpty()) {
                ComponentName cn = runningTaskInfos.get(0).topActivity;
                if (cn == null || cn.getClassName() == null || !cn.getClassName().startsWith(activityName)) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    public static boolean isManualConnecting(Context context) {
        boolean z = false;
        if (context == null) {
            return false;
        }
        if (Settings.System.getInt(context.getContentResolver(), KEY_WIFIPRO_MANUAL_CONNECT, 0) == 1) {
            z = true;
        }
        return z;
    }

    public static boolean isInMonitorList(String input, String[] list) {
        if (!(input == null || list == null)) {
            for (String equals : list) {
                if (input.equals(equals)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isMobileDataOff(Context context) {
        boolean z = false;
        if (context == null) {
            return false;
        }
        if (Settings.Global.getInt(context.getContentResolver(), "mobile_data", 1) == 0) {
            z = true;
        }
        return z;
    }

    public static boolean isMobileDataInactive(Context context) {
        if (context != null) {
            NetworkInfo ni = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
            if (ni != null && ni.isConnected() && ni.getType() == 0) {
                return false;
            }
        }
        return true;
    }

    public static boolean isCalling(Context context) {
        if (context != null) {
            int callState = ((TelephonyManager) context.getSystemService("phone")).getCallState();
            if (2 == callState || 1 == callState) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNoSIMCard(Context context) {
        boolean z = false;
        if (context == null) {
            return false;
        }
        if (((TelephonyManager) context.getSystemService("phone")).getSimState() == 1) {
            z = true;
        }
        return z;
    }

    public static boolean useOperatorOverSea() {
        String operator = TelephonyManager.getDefault().getNetworkOperator();
        if (operator == null || operator.length() <= 0 || operator.startsWith(COUNTRY_CODE_CN)) {
            return false;
        }
        return true;
    }

    public static boolean allowRecheckForNoInternet(WifiConfiguration config, ScanResult scanResult, Context context) {
        boolean allowed = false;
        synchronized (mBackgroundLock) {
            if (config != null) {
                try {
                    if (config.noInternetAccess && !allowWifiConfigRecovery(config.internetHistory) && config.internetRecoveryStatus == 3 && !isQueryActivityMatched(context, HUAWEI_SETTINGS_WLAN) && scanResult != null && ((scanResult.is24GHz() && scanResult.level >= -75) || (scanResult.is5GHz() && scanResult.level >= -72))) {
                        allowed = true;
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
        }
        return allowed;
    }

    public static void setBackgroundConnTag(Context context, boolean background) {
        synchronized (mBackgroundLock) {
            if (context != null) {
                try {
                    Settings.Secure.putInt(context.getContentResolver(), WIFI_BACKGROUND_CONN_TAG, background);
                } catch (Throwable th) {
                    throw th;
                }
            }
        }
    }

    public static String getProductLocale() {
        return SystemProperties.get(KEY_PROP_LOCALE, "");
    }

    public static void setWifiSelfCureStatus(int state) {
        try {
            SystemProperties.set(HW_WIFI_SELF_CURING, String.valueOf(state));
        } catch (RuntimeException e) {
            Log.e("WifiProCommonUtils", "SystemProperties set RuntimeException.");
        }
    }

    public static boolean isWifiSelfCuring() {
        return !String.valueOf(0).equals(SystemProperties.get(HW_WIFI_SELF_CURING, String.valueOf(0)));
    }

    public static int getSelfCuringState() {
        return Integer.parseInt(SystemProperties.get(HW_WIFI_SELF_CURING, String.valueOf(0)));
    }

    public static boolean isWifiSecDetectOn(Context context) {
        boolean z = false;
        if (context == null) {
            return false;
        }
        if (Settings.Global.getInt(context.getContentResolver(), KEY_WIFI_SECURE, 0) == 1) {
            z = true;
        }
        return z;
    }

    public static boolean matchedRequestByHistory(String internetHistory, int type) {
        boolean z;
        String str = internetHistory;
        int i = type;
        boolean matched = false;
        if (str == null || str.lastIndexOf("/") == -1) {
            return false;
        }
        String[] temp = str.split("/");
        int[] items = new int[temp.length];
        int numHasInet = 0;
        int numPortal = 0;
        int numTarget = 0;
        int numNoInet = 0;
        int numChecked = 0;
        int numChecked2 = 0;
        while (true) {
            int i2 = numChecked2;
            z = true;
            if (i2 >= temp.length) {
                break;
            }
            try {
                items[i2] = Integer.parseInt(temp[i2]);
                if (items[i2] != -1) {
                    numChecked++;
                }
                if (items[i2] == 0) {
                    numNoInet++;
                } else if (items[i2] == 1) {
                    numHasInet++;
                } else if (items[i2] == 2) {
                    numPortal++;
                }
                numChecked2 = i2 + 1;
            } catch (NumberFormatException e) {
                Log.e("WifiProCommonUtils", "matchedRequestByHistory broken network history: parse internetHistory failed");
            }
        }
        int itemValue = -1;
        if (i == 100) {
            itemValue = 1;
            numTarget = numHasInet;
        } else if (i == 102) {
            if (numPortal < 1) {
                z = false;
            }
            return z;
        } else if (i == 103) {
            if (numChecked != 0) {
                z = false;
            }
            return z;
        } else if (i == 104) {
            if (numHasInet < 1) {
                z = false;
            }
            return z;
        }
        if (numChecked >= 1 && items[0] == itemValue) {
            matched = true;
        }
        if (!matched && numChecked == 2 && (items[0] == itemValue || items[1] == itemValue)) {
            matched = true;
        }
        if (!matched && numChecked >= 3 && ((float) numTarget) / ((float) numChecked) >= 0.8f) {
            matched = true;
        }
        return matched;
    }

    public static boolean isOpenType(WifiConfiguration config) {
        boolean z = false;
        if (config == null || config.allowedKeyManagement.cardinality() > 1) {
            return false;
        }
        if (config.getAuthType() == 0) {
            z = true;
        }
        return z;
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
            int end2 = requestUrl.indexOf("/", start);
            if (end2 == -1 || "/".length() + end2 > requestUrl.length()) {
                end = requestUrl.indexOf("?", start);
            } else {
                int tmpEnd = requestUrl.substring(start, end2).indexOf("?", 0);
                end = tmpEnd != -1 ? tmpEnd + start : end2;
            }
            if (end != -1 && 0 <= end && "/".length() + end <= requestUrl.length()) {
                return requestUrl.substring(0, end);
            }
        }
        return requestUrl;
    }

    public static boolean invalidUrlLocation(String location) {
        if (location == null || (!location.startsWith("http://") && !location.startsWith("https://"))) {
            return true;
        }
        return false;
    }

    public static String dhcpResults2String(DhcpResults dhcpResults, int cellid) {
        if (dhcpResults == null || dhcpResults.ipAddress == null || dhcpResults.ipAddress.getAddress() == null || dhcpResults.dnsServers == null) {
            return null;
        }
        StringBuilder lastDhcpResults = new StringBuilder();
        lastDhcpResults.append(String.valueOf(cellid) + "|");
        StringBuilder sb = new StringBuilder();
        sb.append(dhcpResults.domains == null ? "" : dhcpResults.domains);
        sb.append("|");
        lastDhcpResults.append(sb.toString());
        lastDhcpResults.append(buildPrintableIpAddress(dhcpResults.ipAddress.getAddress().getHostAddress()));
        lastDhcpResults.append("|");
        lastDhcpResults.append(dhcpResults.ipAddress.getPrefixLength() + "|");
        lastDhcpResults.append(dhcpResults.ipAddress.getFlags() + "|");
        lastDhcpResults.append(dhcpResults.ipAddress.getScope() + "|");
        StringBuilder sb2 = new StringBuilder();
        sb2.append(dhcpResults.gateway != null ? dhcpResults.gateway.getHostAddress() : "");
        sb2.append("|");
        lastDhcpResults.append(sb2.toString());
        Iterator it = dhcpResults.dnsServers.iterator();
        while (it.hasNext()) {
            lastDhcpResults.append(((InetAddress) it.next()).getHostAddress() + "|");
        }
        return lastDhcpResults.toString();
    }

    public static StaticIpConfiguration dhcpResults2StaticIpConfig(String lastDhcpResults) {
        if (lastDhcpResults != null && lastDhcpResults.length() > 0) {
            StaticIpConfiguration staticIpConfig = new StaticIpConfiguration();
            String[] dhcpResults = lastDhcpResults.split(HwLogRecordManager.VERTICAL_ESC_SEPARATE);
            InetAddress ipAddr = null;
            int prefLength = -1;
            int flag = -1;
            int scope = -1;
            int i = 0;
            while (i < dhcpResults.length) {
                try {
                    if (i != 0) {
                        if (i == 1) {
                            staticIpConfig.domains = dhcpResults[i];
                        } else if (i == 2) {
                            ipAddr = InetAddress.getByName(dhcpResults[i]);
                        } else if (i == 3) {
                            prefLength = Integer.parseInt(dhcpResults[i]);
                        } else if (i == 4) {
                            flag = Integer.parseInt(dhcpResults[i]);
                        } else if (i == 5) {
                            scope = Integer.parseInt(dhcpResults[i]);
                        } else if (i == 6) {
                            staticIpConfig.gateway = InetAddress.getByName(dhcpResults[i]);
                        } else {
                            staticIpConfig.dnsServers.add(InetAddress.getByName(dhcpResults[i]));
                        }
                    }
                    i++;
                } catch (IllegalArgumentException | UnknownHostException e) {
                }
            }
            if (!(ipAddr == null || prefLength == -1 || staticIpConfig.gateway == null || staticIpConfig.dnsServers.size() <= 0)) {
                staticIpConfig.ipAddress = new LinkAddress(ipAddr, prefLength, flag, scope);
                return staticIpConfig;
            }
        }
        return null;
    }

    public static String dhcpResults2Gateway(String lastDhcpResults) {
        if (lastDhcpResults != null && lastDhcpResults.length() > 0) {
            try {
                String[] dhcpResults = lastDhcpResults.split(HwLogRecordManager.VERTICAL_ESC_SEPARATE);
                if (dhcpResults.length >= 7) {
                    return InetAddress.getByName(dhcpResults[6]).toString();
                }
            } catch (UnknownHostException e) {
                Log.e("WifiProCommonUtils", "dhcpResults2Gateway UnknownHostException failed");
            }
        }
        return null;
    }

    public static boolean isAllowWifiSwitch(List<ScanResult> scanResults, List<WifiConfiguration> configNetworks, String currBssid, String currSsid, String currConfigKey, int rssiRequired) {
        boolean sameConfigKey;
        List<ScanResult> list = scanResults;
        List<WifiConfiguration> list2 = configNetworks;
        String str = currBssid;
        String str2 = currSsid;
        if (list == null || scanResults.size() == 0) {
            String str3 = currConfigKey;
            int i = rssiRequired;
            return false;
        } else if (list2 == null || configNetworks.size() == 0) {
            String str4 = currConfigKey;
            int i2 = rssiRequired;
            return false;
        } else {
            for (int i3 = 0; i3 < scanResults.size(); i3++) {
                ScanResult nextResult = list.get(i3);
                String scanSsid = "\"" + nextResult.SSID + "\"";
                String scanResultEncrypt = nextResult.capabilities;
                boolean sameBssid = str != null && str.equals(nextResult.BSSID);
                if (str2 == null || !str2.equals(scanSsid)) {
                    String str5 = currConfigKey;
                } else if (isSameEncryptType(scanResultEncrypt, currConfigKey)) {
                    sameConfigKey = true;
                    if (!sameBssid || sameConfigKey) {
                        int i4 = rssiRequired;
                    } else if (nextResult.level < rssiRequired) {
                        continue;
                    } else {
                        for (int j = 0; j < configNetworks.size(); j++) {
                            WifiConfiguration nextConfig = list2.get(j);
                            int disableReason = nextConfig.getNetworkSelectionStatus().getNetworkSelectionDisableReason();
                            if ((!nextConfig.noInternetAccess || allowWifiConfigRecovery(nextConfig.internetHistory)) && disableReason <= 0 && !isOpenAndPortal(nextConfig) && !isOpenAndMaybePortal(nextConfig) && nextConfig.SSID != null && nextConfig.SSID.equals(scanSsid) && isSameEncryptType(scanResultEncrypt, nextConfig.configKey())) {
                                return true;
                            }
                        }
                        continue;
                    }
                }
                sameConfigKey = false;
                if (!sameBssid) {
                }
                int i42 = rssiRequired;
            }
            String str6 = currConfigKey;
            int i5 = rssiRequired;
            return false;
        }
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

    public static int getReachableCode(boolean googleServer) {
        return googleServer ? 204 : 200;
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

    public static boolean isOpenNetwork(String encryption) {
        if (isEncryptionWep(encryption) || isEncryptionPsk(encryption) || isEncryptionEap(encryption)) {
            return false;
        }
        return true;
    }

    public static boolean isSameEncryptType(String encryption1, String encryption2) {
        if (encryption1 == null || encryption2 == null) {
            return false;
        }
        if ((!isEncryptionWep(encryption1) || !isEncryptionWep(encryption2)) && ((!isEncryptionPsk(encryption1) || !isEncryptionPsk(encryption2)) && ((!isEncryptionEap(encryption1) || !isEncryptionEap(encryption2)) && (!isOpenNetwork(encryption1) || !isOpenNetwork(encryption2))))) {
            return false;
        }
        return true;
    }

    public static boolean isLandscapeMode(Context context) {
        boolean z = false;
        if (context == null || context.getResources() == null || context.getResources().getConfiguration() == null) {
            return false;
        }
        if (context.getResources().getConfiguration().orientation == 2) {
            z = true;
        }
        return z;
    }

    public static int getForegroundAppUid(Context context) {
        if (context == null) {
            return -1;
        }
        List<ActivityManager.RunningAppProcessInfo> lr = ((ActivityManager) context.getSystemService("activity")).getRunningAppProcesses();
        if (lr == null) {
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
        if (TextUtils.isEmpty(name)) {
            name = "unknown:" + uid;
        }
        return name;
    }

    public static String insertWifiConfigHistory(String internetHistory, int status) {
        String newInternetHistory;
        if (internetHistory == null || internetHistory.lastIndexOf("/") == -1) {
            return INTERNET_HISTORY_INIT;
        }
        String internetHistory2 = internetHistory.substring(0, internetHistory.lastIndexOf("/"));
        Log.d("WifiProCommonUtils", "insertWifiConfigHistory, newInternetHistory = " + newInternetHistory);
        return newInternetHistory;
    }

    public static String updateWifiConfigHistory(String internetHistory, int status) {
        String newInternetHistory;
        if (internetHistory == null || internetHistory.lastIndexOf("/") == -1) {
            return INTERNET_HISTORY_INIT;
        }
        String internetHistory2 = internetHistory.substring(internetHistory.indexOf("/") + 1);
        Log.d("WifiProCommonUtils", "updateWifiConfigHistory, newInternetHistory = " + newInternetHistory);
        return newInternetHistory;
    }

    public static boolean allowWifiConfigRecovery(String internetHistory) {
        boolean allowRecovery = false;
        if (internetHistory == null || internetHistory.lastIndexOf("/") == -1) {
            Log.w("WifiProCommonUtils", "allowWifiConfigRecovery, inputed arg is invalid, internetHistory = null");
            return false;
        }
        String[] temp = internetHistory.split("/");
        int[] items = new int[temp.length];
        int numHasInet = 0;
        int numNoInet = 0;
        int numChecked = 0;
        int i = 0;
        while (i < temp.length) {
            try {
                items[i] = Integer.parseInt(temp[i]);
                if (items[i] != -1) {
                    numChecked++;
                }
                if (items[i] == 0) {
                    numNoInet++;
                } else if (items[i] == 1) {
                    numHasInet++;
                }
                i++;
            } catch (NumberFormatException e) {
                Log.w("WifiProCommonUtils", "Broken network history: parse internetHistory failed.");
            }
        }
        if (numChecked >= 2) {
            if (items[0] != 1 && items[1] != 1) {
                return false;
            }
            allowRecovery = true;
            int i2 = 1;
            while (true) {
                if (i2 >= numChecked) {
                    break;
                } else if (items[i2] != 1) {
                    allowRecovery = false;
                    break;
                } else {
                    i2++;
                }
            }
        }
        if (!allowRecovery && numChecked >= 3 && items[1] == 1 && items[2] == 1) {
            allowRecovery = true;
        }
        if (!allowRecovery && numChecked >= 3 && ((float) numHasInet) / ((float) numChecked) >= 0.8f) {
            allowRecovery = true;
        }
        return allowRecovery;
    }

    public static boolean isNetworkReachableByICMP(String ipAddress, int timeout) {
        try {
            return Inet4Address.getByName(ipAddress).isReachableByICMP(timeout);
        } catch (IOException e) {
            Log.d("WifiProCommonUtils", "IOException, isNetworkReachableByICMP failed!");
            return false;
        }
    }

    public static boolean isWifiOnly(Context context) {
        boolean z = false;
        if (context == null) {
            return false;
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
        if (cm != null && !cm.isNetworkSupported(0)) {
            z = true;
        }
        return z;
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

    public static int getCurrentCellId() {
        int cellid = -1;
        int phoneId = HwTelephonyManager.getDefault().getPreferredDataSubscription();
        if (phoneId < 0 || phoneId >= 2) {
            return -1;
        }
        CellLocation cellLocation = HwTelephonyManager.getDefault().getCellLocation(phoneId);
        if (cellLocation != null) {
            if (cellLocation instanceof CdmaCellLocation) {
                CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) cellLocation;
                cellid = cdmaCellLocation.getBaseStationId();
                if (cellid < 0) {
                    cellid = cdmaCellLocation.getCid();
                }
            } else if (cellLocation instanceof GsmCellLocation) {
                cellid = ((GsmCellLocation) cellLocation).getCid();
            }
        }
        return cellid;
    }

    public static boolean getAirplaneModeOn(Context mContext) {
        return Settings.Global.getInt(mContext.getContentResolver(), "airplane_mode_on", 0) == 1;
    }

    public static String buildPrintableIpAddress(String originIpAddr) {
        if (originIpAddr == null) {
            return null;
        }
        byte[] ipAddrArray = NetworkUtils.numericToInetAddress(originIpAddr).getAddress();
        if (ipAddrArray.length != 4) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int index = 0; index < 3; index++) {
            sb.append(ipAddrArray[index]);
            sb.append(".");
        }
        sb.append("***");
        return sb.toString();
    }
}
