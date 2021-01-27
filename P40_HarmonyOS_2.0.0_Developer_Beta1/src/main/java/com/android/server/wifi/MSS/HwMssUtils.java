package com.android.server.wifi.MSS;

import android.content.Context;
import android.os.SystemProperties;
import android.util.wifi.HwHiLog;
import android.widget.Toast;
import com.android.server.wifi.MSS.HwMssArbitrager;
import java.util.Locale;

public class HwMssUtils {
    public static final int ETH_ALEN = 6;
    public static final String HISI_CHIP_1102A = "hi1102a";
    public static final String HISI_CHIP_1103 = "hi1103";
    public static final String HISI_CHIP_1105 = "hi1105";
    public static final int HISI_EVENT_MSS_BLACKLIST = 100;
    public static final int HISI_M2S_RSP_TIMEOUT = 300000;
    public static final int HISI_S2M_RSP_TIMEOUT = 30000;
    private static final boolean HWDBG = true;
    private static boolean ISALLOWSWITCH = HWDBG;
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
    private static final String TAG = "HwMssUtils";
    private static String chipName = "unknown";

    private static void initMssDebug() {
        if (MSSDBG == 2) {
            synchronized (HwMssUtils.class) {
                MSSDBG = SystemProperties.getInt("runtime.hwmss.debug", 0);
            }
        }
    }

    /* renamed from: com.android.server.wifi.MSS.HwMssUtils$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$server$wifi$MSS$HwMssArbitrager$MssState = new int[HwMssArbitrager.MssState.values().length];

        static {
            try {
                $SwitchMap$com$android$server$wifi$MSS$HwMssArbitrager$MssState[HwMssArbitrager.MssState.MSSMIMO.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$server$wifi$MSS$HwMssArbitrager$MssState[HwMssArbitrager.MssState.MSSSISO.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$server$wifi$MSS$HwMssArbitrager$MssState[HwMssArbitrager.MssState.ABSMIMO.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$server$wifi$MSS$HwMssArbitrager$MssState[HwMssArbitrager.MssState.ABSMRC.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$android$server$wifi$MSS$HwMssArbitrager$MssState[HwMssArbitrager.MssState.ABSSWITCHING.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    public static void switchToast(Context cxt, HwMssArbitrager.MssState state) {
        String value = "switch to state: UNKNOWN";
        int i = AnonymousClass1.$SwitchMap$com$android$server$wifi$MSS$HwMssArbitrager$MssState[state.ordinal()];
        if (i == 1) {
            value = "switch to state: MSSMIMO";
        } else if (i == 2) {
            value = "switch to state: MSSSISO";
        } else if (i == 3) {
            value = "switch to state: ABSMIMO";
        } else if (i == 4) {
            value = "switch to state: ABSMRC";
        } else if (i == 5) {
            value = "switch to state: ABSSWITCHING";
        }
        toast(cxt, value);
    }

    public static void toast(Context cxt, String value) {
        initMssDebug();
        if (cxt != null && value != null && MSSDBG == 1) {
            Toast.makeText(cxt, value, 1).show();
        }
    }

    public static String parseMacBytes(byte[] bytes) {
        if (bytes == null || bytes.length != 6) {
            return "";
        }
        return String.format(Locale.ENGLISH, "%02x:%02x:%02x:%02x:%02x:%02x", Byte.valueOf(bytes[0]), Byte.valueOf(bytes[1]), Byte.valueOf(bytes[2]), Byte.valueOf(bytes[3]), Byte.valueOf(bytes[4]), Byte.valueOf(bytes[5]));
    }

    public static String parseMaskMacBytes(byte[] bytes) {
        if (bytes == null || bytes.length != 6) {
            return "";
        }
        return String.format(Locale.ENGLISH, "%02x:%02x:%02x:xx:xx:%02x", Byte.valueOf(bytes[0]), Byte.valueOf(bytes[1]), Byte.valueOf(bytes[2]), Byte.valueOf(bytes[5]));
    }

    public static boolean is1103() {
        if ("unknown".equals(chipName)) {
            chipName = SystemProperties.get("ro.connectivity.sub_chiptype", "unknown");
        }
        return HISI_CHIP_1103.equals(chipName);
    }

    public static boolean is1105() {
        if ("unknown".equals(chipName)) {
            chipName = SystemProperties.get("ro.connectivity.sub_chiptype", "unknown");
        }
        return HISI_CHIP_1105.equals(chipName);
    }

    public static boolean is1102A() {
        if ("unknown".equals(chipName)) {
            chipName = SystemProperties.get("ro.connectivity.sub_chiptype", "unknown");
        }
        return HISI_CHIP_1102A.equals(chipName);
    }

    public static void log(int level, boolean isFmtStrPrivate, String msg, Object... args) {
        log(level, isFmtStrPrivate, TAG, msg, args);
    }

    public static void log(int level, boolean isFmtStrPrivate, String tag, String msg, Object... args) {
        initMssDebug();
        if (level == 0) {
            HwHiLog.e(tag, isFmtStrPrivate, msg, args);
        } else if (level == 1) {
            HwHiLog.d(tag, isFmtStrPrivate, msg, args);
        } else if (MSSDBG == 1) {
            HwHiLog.d(tag, isFmtStrPrivate, msg, args);
        }
    }

    public static void logD(String tag, boolean isFmtStrPrivate, String msg, Object... args) {
        log(1, isFmtStrPrivate, tag, msg, args);
    }

    public static void logE(String tag, boolean isFmtStrPrivate, String msg, Object... args) {
        log(0, isFmtStrPrivate, tag, msg, args);
    }

    public static void logV(String tag, boolean isFmtStrPrivate, String msg, Object... args) {
        log(3, isFmtStrPrivate, tag, msg, args);
    }

    public static void logD(boolean isFmtStrPrivate, String msg, Object... args) {
        log(1, isFmtStrPrivate, msg, args);
    }

    public static void logE(boolean isFmtStrPrivate, String msg, Object... args) {
        log(0, isFmtStrPrivate, msg, args);
    }

    public static void logV(boolean isFmtStrPrivate, String msg, Object... args) {
        log(3, isFmtStrPrivate, msg, args);
    }

    public static synchronized void setAllowSwitch(boolean value) {
        synchronized (HwMssUtils.class) {
            ISALLOWSWITCH = value;
        }
    }

    public static boolean isAllowSwitch() {
        return ISALLOWSWITCH;
    }
}
