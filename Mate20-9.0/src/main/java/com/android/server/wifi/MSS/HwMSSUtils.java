package com.android.server.wifi.MSS;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Log;
import android.widget.Toast;
import com.android.server.wifi.MSS.HwMSSArbitrager;
import java.util.Locale;

public class HwMSSUtils {
    public static final int ETH_ALEN = 6;
    public static final String HISI_CHIP_1103 = "hi1103";
    public static final int HISI_EVENT_MSS_BLACKLIST = 100;
    public static final int HISI_M2S_RSP_TIMEOUT = 300000;
    public static final int HISI_S2M_RSP_TIMEOUT = 30000;
    private static final boolean HWDBG = true;
    private static boolean ISALLOWSWITCH = true;
    public static final boolean ISBETA = false;
    public static final int LOG_LEVEL_DEBUG = 1;
    public static final int LOG_LEVEL_ERROR = 0;
    public static final int LOG_LEVEL_VERBOSE = 3;
    public static final int MAX_MSS_FAIL_CNT = 10;
    public static final int MSG_MSS_STATE_REPORT = 3;
    public static final int MSG_MSS_SWITCH_MIMO_REQ = 2;
    public static final int MSG_MSS_SWITCH_RSP_TIMEOUT = 4;
    public static final int MSG_MSS_SWITCH_SISO_REQ = 1;
    public static final int MSG_MSS_SYNC_STATE_REQ = 5;
    public static final int MSG_SCREEN_OFF = 22;
    public static final int MSG_SCREEN_ON = 21;
    public static final int MSG_SUPPLICANT_COMPLETE = 14;
    public static final int MSG_WIFI_CONNECTED = 10;
    public static final int MSG_WIFI_DISABLED = 13;
    public static final int MSG_WIFI_DISCONNECTED = 11;
    public static final int MSG_WIFI_ENABLED = 12;
    private static int MSSDBG = 2;
    private static final int MSS_DEBUG_ALLOW = 1;
    private static final int MSS_DEBUG_DENY = 0;
    private static final int MSS_DEBUG_UNINITIAL = 2;
    public static final int MSS_MIMO_TO_SISO = 1;
    public static final int MSS_SISO_TO_MIMO = 2;
    public static final int MSS_SYNC_AFT_CONNECTED = 5000;
    public static final String PERFORMANCEAPP = "com.example.wptp.testapp";
    private static String[] PERFORMANCE_PRODUCTS = {"ELE-", "VOG-"};
    public static final int RADIO_ACTION_ACTIVE = 1;
    public static final int RADIO_ACTION_DEACTIVE = 0;
    public static final int RADIO_TYPE_AIRPLANE_OFF = 4;
    public static final int RADIO_TYPE_AIRPLANE_ON = 3;
    public static final int RADIO_TYPE_APP_SWTICH = 0;
    public static final int RADIO_TYPE_CARD_ABSENT = 2;
    public static final int RADIO_TYPE_CARD_PLUGIN = 1;
    private static final String TAG = "HwMSSUtils";
    private static final String UNKNOWN = "unknown";
    private static String chipName = UNKNOWN;
    private static String productName = UNKNOWN;

    private static void initMSSDebug() {
        if (MSSDBG == 2) {
            synchronized (HwMSSUtils.class) {
                MSSDBG = SystemProperties.getInt("runtime.hwmss.debug", 0);
            }
        }
    }

    public static void switchToast(Context cxt, HwMSSArbitrager.MSSState state) {
        String value = "switch to state: UNKNOWN";
        switch (state) {
            case MSSMIMO:
                value = "switch to state: MSSMIMO";
                break;
            case MSSSISO:
                value = "switch to state: MSSSISO";
                break;
            case ABSMIMO:
                value = "switch to state: ABSMIMO";
                break;
            case ABSMRC:
                value = "switch to state: ABSMRC";
                break;
            case ABSSWITCHING:
                value = "switch to state: ABSSWITCHING";
                break;
        }
        toast(cxt, value);
    }

    public static void toast(Context cxt, String value) {
        initMSSDebug();
        if (cxt != null && value != null && MSSDBG == 1) {
            Toast.makeText(cxt, value, 1).show();
        }
    }

    public static String parseMacBytes(byte[] bytes) {
        if (bytes == null || bytes.length != 6) {
            return "";
        }
        return String.format(Locale.US, "%02x:%02x:%02x:%02x:%02x:%02x", new Object[]{Byte.valueOf(bytes[0]), Byte.valueOf(bytes[1]), Byte.valueOf(bytes[2]), Byte.valueOf(bytes[3]), Byte.valueOf(bytes[4]), Byte.valueOf(bytes[5])});
    }

    public static String parseMaskMacBytes(byte[] bytes) {
        if (bytes == null || bytes.length != 6) {
            return "";
        }
        return String.format(Locale.US, "%02x:%02x:%02x:xx:xx:%02x", new Object[]{Byte.valueOf(bytes[0]), Byte.valueOf(bytes[1]), Byte.valueOf(bytes[2]), Byte.valueOf(bytes[5])});
    }

    public static boolean isPerformanceProduct() {
        if (UNKNOWN.equals(productName)) {
            productName = SystemProperties.get("ro.product.name", UNKNOWN);
        }
        for (String product : PERFORMANCE_PRODUCTS) {
            if (productName != null && productName.startsWith(product)) {
                return true;
            }
        }
        return false;
    }

    public static boolean is1103() {
        if (UNKNOWN.equals(chipName)) {
            chipName = SystemProperties.get("ro.connectivity.sub_chiptype", UNKNOWN);
        }
        return HISI_CHIP_1103.equals(chipName);
    }

    public static void log(int level, String msg) {
        log(level, TAG, msg);
    }

    public static void log(int level, String tag, String msg) {
        initMSSDebug();
        if (level == 0) {
            Log.e(tag, msg);
        } else if (level == 1) {
            Log.d(tag, msg);
        } else if (MSSDBG == 1) {
            Log.d(tag, msg);
        }
    }

    public static void logd(String tag, String msg) {
        log(1, tag, msg);
    }

    public static void loge(String tag, String msg) {
        log(0, tag, msg);
    }

    public static void logv(String tag, String msg) {
        log(3, tag, msg);
    }

    public static void logd(String msg) {
        log(1, msg);
    }

    public static void loge(String msg) {
        log(0, msg);
    }

    public static void logv(String msg) {
        log(3, msg);
    }

    public static synchronized void setAllowSwitch(boolean value) {
        synchronized (HwMSSUtils.class) {
            ISALLOWSWITCH = value;
        }
    }

    public static boolean isAllowSwitch() {
        return ISALLOWSWITCH;
    }
}
