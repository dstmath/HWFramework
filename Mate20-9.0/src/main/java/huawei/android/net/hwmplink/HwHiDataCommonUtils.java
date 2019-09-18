package huawei.android.net.hwmplink;

import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemProperties;
import android.util.Log;

public class HwHiDataCommonUtils {
    public static final int APP_WIFI_NO_SLEEP = 0;
    public static final int APP_WIFI_SLEEP_UNKNOWN = -1;
    public static final String BASE_TAG = "HiData_";
    private static final String BroadcastKey = "com.android.server.hidata.arbitration.HwArbitrationStateMachine";
    private static final String BroadcastNetworkKey = "MPLinkSuccessNetWorkKey";
    private static final String BroadcastUIDKey = "MPLinkSuccessUIDKey";
    public static final int CELL_NETWORK = 0;
    public static final boolean HIDATA_DEBUG = SystemProperties.getBoolean("ro.config.mplink_log", false);
    public static final int NONE_NETWORK = -1;
    private static final String TAG = ("HiData_" + HwHiDataCommonUtils.class.getSimpleName());
    public static final int WHITE_LIST_TYPE_WIFI_SLEEP = 7;
    public static final int WIFI_NETWORK = 1;

    public static void logD(String tag, String log) {
        Log.d(tag, log);
    }

    public static void logI(String tag, String log) {
        if (HIDATA_DEBUG) {
            Log.i(tag, log);
        }
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
}
