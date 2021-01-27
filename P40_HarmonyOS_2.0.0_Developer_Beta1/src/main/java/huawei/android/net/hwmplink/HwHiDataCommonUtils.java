package huawei.android.net.hwmplink;

import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemProperties;
import android.util.wifi.HwHiLog;
import com.huawei.android.net.wifi.WifiInfoEx;

public class HwHiDataCommonUtils {
    public static final int APP_WIFI_NO_SLEEP = 0;
    public static final int APP_WIFI_SLEEP_UNKNOWN = -1;
    public static final String BASE_TAG = "HiData_";
    private static final String BROADCAST_KEY = "com.android.server.hidata.arbitration.HwArbitrationStateMachine";
    private static final String BROADCAST_NETWORK_KEY = "MPLinkSuccessNetWorkKey";
    private static final String BROADCAST_UID_KEY = "MPLinkSuccessUIDKey";
    public static final int CELL_NETWORK = 0;
    private static final int DEFAULT_VALUE = -1;
    public static final boolean HIDATA_DEBUG = SystemProperties.getBoolean("ro.config.mplink_log", false);
    public static final int NONE_NETWORK = -1;
    private static final String TAG = ("HiData_" + HwHiDataCommonUtils.class.getSimpleName());
    public static final int WHITE_LIST_TYPE_WIFI_SLEEP = 7;
    public static final int WIFI_NETWORK = 1;

    private HwHiDataCommonUtils() {
    }

    public static void logD(String tag, boolean isPrivateFmtStr, String fmt, Object... args) {
        HwHiLog.d(tag, isPrivateFmtStr, fmt, args);
    }

    public static void logI(String tag, boolean isPrivateFmtStr, String fmt, Object... args) {
        if (HIDATA_DEBUG) {
            HwHiLog.i(tag, isPrivateFmtStr, fmt, args);
        }
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
            return WifiInfoEx.INVALID_RSSI;
        }
        return wifiInfo.getRssi();
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

    public static boolean isWpaOrWpa2(WifiConfiguration config) {
        if (config == null) {
            return false;
        }
        int authType = config.allowedKeyManagement.cardinality() > 1 ? -1 : config.getAuthType();
        if (authType == 1 || authType == 4) {
            return true;
        }
        return false;
    }
}
