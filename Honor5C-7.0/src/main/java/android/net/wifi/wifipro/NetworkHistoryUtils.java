package android.net.wifi.wifipro;

import android.net.wifi.WifiConfiguration;
import android.util.Log;
import java.io.DataOutputStream;
import java.io.IOException;

public class NetworkHistoryUtils {
    public static final int HISTORY_ITEM_INTERNET = 1;
    public static final int HISTORY_ITEM_NO_INTERNET = 0;
    public static final int HISTORY_ITEM_PORTAL = 2;
    public static final int HISTORY_ITEM_UNCHECKED = -1;
    public static final String INTERNET_HISTORY_INIT = "-1/-1/-1/-1/-1/-1/-1/-1/-1/-1";
    public static final String INTERNET_HISTORY_KEY = "INTERNET_HISTORY";
    public static final String LAST_HAS_INTERNET_TS_KEY = "LAST_HAS_INTERNET_TS";
    public static final String PORTAL_NETWORK_KEY = "PORTAL_NETWORK";
    public static final float RECOVERY_PERCENTAGE = 0.5f;
    public static final String TAG = "NetworkHistoryUtils";
    public static final String WIFI_PRO_TEMP_CREATE = "WIFI_PRO_TEMP_CREATE";

    public static void writeNetworkHistory(WifiConfiguration config, DataOutputStream out, String separator, String nl) throws IOException {
        out.writeUTF(INTERNET_HISTORY_KEY + separator + config.internetHistory + nl);
        out.writeUTF(PORTAL_NETWORK_KEY + separator + Boolean.toString(config.portalNetwork) + nl);
        out.writeUTF(WIFI_PRO_TEMP_CREATE + separator + Boolean.toString(config.isTempCreated) + nl);
        out.writeUTF(LAST_HAS_INTERNET_TS_KEY + separator + Long.toString(config.lastHasInternetTimestamp) + nl);
    }

    public static void readNetworkHistory(WifiConfiguration config, String key, String value) {
        boolean z;
        if (key.startsWith(INTERNET_HISTORY_KEY)) {
            config.internetHistory = value;
        } else if (key.startsWith(PORTAL_NETWORK_KEY)) {
            config.portalNetwork = Boolean.parseBoolean(value);
        } else if (key.startsWith(WIFI_PRO_TEMP_CREATE)) {
            config.isTempCreated = Boolean.parseBoolean(value);
        } else if (key.startsWith(LAST_HAS_INTERNET_TS_KEY)) {
            config.lastHasInternetTimestamp = Long.parseLong(value);
        }
        if (config.portalNetwork) {
            z = false;
        } else {
            z = config.hasNoInternetAccess();
        }
        config.noInternetAccess = z;
    }

    public static String insertWifiConfigHistory(String internetHistory, int status) {
        String newInternetHistory = INTERNET_HISTORY_INIT;
        if (internetHistory == null || internetHistory.lastIndexOf("/") == HISTORY_ITEM_UNCHECKED) {
            LOGW("insertWifiConfigHistory, inputed arg is invalid, internetHistory = null");
            return newInternetHistory;
        }
        LOGD("insertWifiConfigHistory, internetHistory = " + internetHistory + ", status = " + status);
        newInternetHistory = String.valueOf(status) + "/" + internetHistory.substring(HISTORY_ITEM_NO_INTERNET, internetHistory.lastIndexOf("/"));
        LOGD("insertWifiConfigHistory, newInternetHistory = " + newInternetHistory);
        return newInternetHistory;
    }

    public static String updateWifiConfigHistory(String internetHistory, int status) {
        String newInternetHistory = INTERNET_HISTORY_INIT;
        if (internetHistory == null || internetHistory.lastIndexOf("/") == HISTORY_ITEM_UNCHECKED) {
            LOGW("updateWifiConfigHistory, inputed arg is invalid, internetHistory = null");
            return newInternetHistory;
        }
        LOGD("updateWifiConfigHistory, internetHistory = " + internetHistory + ", status = " + status);
        newInternetHistory = String.valueOf(status) + "/" + internetHistory.substring(internetHistory.indexOf("/") + HISTORY_ITEM_INTERNET);
        LOGD("updateWifiConfigHistory, newInternetHistory = " + newInternetHistory);
        return newInternetHistory;
    }

    public static boolean allowWifiConfigRecovery(String internetHistory) {
        boolean allowRecovery = false;
        if (internetHistory == null || internetHistory.lastIndexOf("/") == HISTORY_ITEM_UNCHECKED) {
            LOGW("allowWifiConfigRecovery, inputed arg is invalid, internetHistory = null");
            return false;
        }
        String[] temp = internetHistory.split("/");
        int[] items = new int[temp.length];
        int numChecked = HISTORY_ITEM_NO_INTERNET;
        int numNoInet = HISTORY_ITEM_NO_INTERNET;
        int numHasInet = HISTORY_ITEM_NO_INTERNET;
        int i = HISTORY_ITEM_NO_INTERNET;
        while (i < temp.length) {
            items[i] = Integer.parseInt(temp[i]);
            if (items[i] != HISTORY_ITEM_UNCHECKED) {
                numChecked += HISTORY_ITEM_INTERNET;
            }
            if (items[i] == 0) {
                numNoInet += HISTORY_ITEM_INTERNET;
            } else if (items[i] == HISTORY_ITEM_INTERNET || items[i] == HISTORY_ITEM_PORTAL) {
                numHasInet += HISTORY_ITEM_INTERNET;
            }
            i += HISTORY_ITEM_INTERNET;
        }
        LOGD("allowWifiConfigRecovery, numChecked = " + numChecked + ", numNoInet = " + numNoInet + ", numHasInet = " + numHasInet);
        if (numChecked >= HISTORY_ITEM_PORTAL) {
            allowRecovery = true;
            for (i = HISTORY_ITEM_INTERNET; i < numChecked; i += HISTORY_ITEM_INTERNET) {
                if (items[i] != HISTORY_ITEM_INTERNET) {
                    allowRecovery = false;
                    break;
                }
            }
        }
        if (!allowRecovery && numChecked >= 3 && items[HISTORY_ITEM_INTERNET] == HISTORY_ITEM_INTERNET && items[HISTORY_ITEM_PORTAL] == HISTORY_ITEM_INTERNET) {
            allowRecovery = true;
        }
        if (!allowRecovery && numChecked >= 3) {
            float percentage = ((float) numHasInet) / ((float) numChecked);
            LOGD("allowWifiConfigRecovery, percentage = " + percentage + ", RECOVERY_PERCENTAGE = " + RECOVERY_PERCENTAGE);
            if (percentage >= RECOVERY_PERCENTAGE) {
                allowRecovery = true;
            }
        }
        LOGD("allowWifiConfigRecovery, internetHistory = " + internetHistory + ", allowRecovery = " + allowRecovery);
        return allowRecovery;
    }

    private static void LOGD(String msg) {
        Log.d(TAG, msg);
    }

    private static void LOGW(String msg) {
        Log.w(TAG, msg);
    }
}
