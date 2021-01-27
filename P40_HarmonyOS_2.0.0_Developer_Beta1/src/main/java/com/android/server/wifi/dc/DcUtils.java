package com.android.server.wifi.dc;

import android.net.MacAddress;
import android.os.Message;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import com.android.internal.util.State;
import com.android.server.wifi.MSS.HwMssUtils;
import java.util.Locale;

public class DcUtils {
    public static final int HILINK_ACTION_DC_CONNECT = 2;
    public static final int HILINK_ACTION_DC_DISCONNECT = 3;
    public static final int HILINK_ACTION_DC_GET_CONFIG = 1;
    public static final String INTERFACE_2G = "2.4G";
    public static final String INTERFACE_5G = "5G";
    public static final String INTERFACE_5G_GAME = "5G-2";
    private static final boolean IS_DC_ENABLE = SystemProperties.getBoolean("ro.config.hw_wifi_dc_enable", true);
    public static final int MSG_APP_DC_CONNECT = 35;
    public static final int MSG_DC_CONNECT = 25;
    public static final int MSG_DC_CONNECT_FAIL = 26;
    public static final int MSG_DC_DISCONNECT = 27;
    public static final int MSG_DC_DISCONNECT_FAIL = 28;
    public static final int MSG_DC_P2P_CONNECTED = 32;
    public static final int MSG_DC_P2P_DELETE_COMPLETE = 36;
    public static final int MSG_FIND_DC_MANAGER = 19;
    public static final int MSG_FIND_DC_MANAGER_FAIL = 21;
    public static final int MSG_FIND_DC_MANAGER_SUCC = 20;
    public static final int MSG_GAME_START = 6;
    public static final int MSG_GAME_STOP = 7;
    public static final int MSG_GET_DC_CONFIG = 22;
    public static final int MSG_GET_DC_CONFIG_FAIL = 23;
    public static final int MSG_GET_DC_CONFIG_SUCC = 24;
    public static final int MSG_HILINK_RESET = 18;
    public static final int MSG_MAGICLINK_CONNECT_FAIL = 10;
    public static final int MSG_MAGICLINK_CONNECT_FAIL_P2PCONFLICT = 11;
    public static final int MSG_MAGICLINK_DISCONNECT_FAIL = 13;
    public static final int MSG_MAGICLINK_DISCONNECT_SUCC = 12;
    private static final String[] MSG_NAME_STRINGS = {"MSG_WIFI_CONNECTED", "MSG_WIFI_DISCONNECTED", "MSG_WIFI_ENABLED", "MSG_WIFI_DISABLED", "MSG_P2P_CONNECTED", "MSG_P2P_DISCONNECTED", "MSG_GAME_START", "MSG_GAME_STOP", "MSG_SELECT_DC_NETWORK", "", "MSG_MAGICLINK_CONNECT_FAIL", "MSG_MAGICLINK_CONNECT_FAIL_P2PCONFLICT", "MSG_MAGICLINK_DISCONNECT_SUCC", "MSG_MAGICLINK_DISCONNECT_FAIL", "MSG_ROUTER_HIP2P_START_SUCC", "MSG_ROUTER_HIP2P_START_FAIL", "MSG_ROUTER_HIP2P_STOP_SUCC", "MSG_ROUTER_HIP2P_STOP_FAIL", "MSG_HILINK_RESET", "MSG_FIND_DC_MANAGER", "MSG_FIND_DC_MANAGER_SUCC", "MSG_FIND_DC_MANAGER_FAIL", "MSG_GET_DC_CONFIG", "MSG_GET_DC_CONFIG_FAIL", "MSG_GET_DC_CONFIG_SUCC", "MSG_DC_CONNECT", "MSG_DC_CONNECT_FAIL", "MSG_DC_DISCONNECT", "MSG_DC_DISCONNECT_FAIL", "MSG_WIFI_ROAMING_COMPLETED", "MSG_SCAN_RESULTS", "MSG_NO_NETWORK_BUILD_DC", "MSG_DC_P2P_CONNECTED", "MSG_WIFI_SIGNAL_GOOD", "MSG_WIFI_SIGNAL_BAD", "MSG_APP_DC_CONNECT", "MSG_DC_P2P_DELETE_COMPLETE"};
    public static final int MSG_NO_NETWORK_BUILD_DC = 31;
    public static final int MSG_P2P_CONNECTED = 4;
    public static final int MSG_P2P_DISCONNECTED = 5;
    public static final int MSG_ROUTER_HIP2P_START_FAIL = 15;
    public static final int MSG_ROUTER_HIP2P_START_SUCC = 14;
    public static final int MSG_ROUTER_HIP2P_STOP_FAIL = 17;
    public static final int MSG_ROUTER_HIP2P_STOP_SUCC = 16;
    public static final int MSG_SCAN_RESULTS = 30;
    public static final int MSG_SELECT_DC_NETWORK = 8;
    public static final int MSG_WIFI_CONNECTED = 0;
    public static final int MSG_WIFI_DISABLED = 3;
    public static final int MSG_WIFI_DISCONNECTED = 1;
    public static final int MSG_WIFI_ENABLED = 2;
    public static final int MSG_WIFI_ROAMING_COMPLETED = 29;
    public static final int MSG_WIFI_SIGNAL_BAD = 34;
    public static final int MSG_WIFI_SIGNAL_GOOD = 33;
    public static final int RADIX_HEX = 16;
    private static final String TAG = "DcUtils";

    private DcUtils() {
    }

    public static String getStateAndMessageString(State state, Message message) {
        if (message.what >= 0) {
            int i = message.what;
            String[] strArr = MSG_NAME_STRINGS;
            if (i < strArr.length) {
                return strArr[message.what];
            }
        }
        return "what:" + Integer.toString(message.what);
    }

    public static boolean isDcSupported() {
        boolean isSupport = IS_DC_ENABLE && (HwMssUtils.is1103() || HwMssUtils.is1105() || isMtkSupport());
        HwHiLog.d(TAG, false, "isDCSupported: %{public}s", new Object[]{Boolean.valueOf(isSupport)});
        return isSupport;
    }

    public static String wifiAddr2p2pAddr(String wifiAddr) {
        if (TextUtils.isEmpty(wifiAddr)) {
            HwHiLog.e(TAG, false, "wifiAddr is empty", new Object[0]);
            return "";
        }
        try {
            byte[] macByteAddr = MacAddress.byteAddrFromStringAddr(wifiAddr);
            macByteAddr[0] = (byte) (macByteAddr[0] | 2);
            macByteAddr[4] = (byte) (macByteAddr[4] ^ 128);
            return MacAddress.stringAddrFromByteAddr(macByteAddr).toUpperCase(Locale.ROOT);
        } catch (IllegalArgumentException e) {
            HwHiLog.e(TAG, false, "byteAddrFromStringAddr wifiAddr exception happens", new Object[0]);
            return "";
        }
    }

    private static boolean isMtkSupport() {
        String platform = SystemProperties.get("ro.hardware", "unknown");
        return "mt6873".equals(platform) || "mt6889".equals(platform);
    }
}
