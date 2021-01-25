package com.android.server.wifi.hwcoex;

import android.util.Log;
import android.util.wifi.HwHiLog;
import com.huawei.android.telephony.SubscriptionManagerEx;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HiCoexUtils {
    public static final int BOOL_FALSE_TO_INT = 0;
    public static final int BOOL_TRUE_TO_INT = 1;
    public static final int CELL_FREQ_NR41_DEFAULT = 2500;
    private static final int CELL_FREQ_NR41_END = 2700;
    private static final int CELL_FREQ_NR41_START = 2400;
    private static final int CHAN_INITIAL_CAPACITY = 13;
    private static final String[] CMCC_PLMNS = {"46000", "46002", "46007", "81808"};
    private static final int[][] DEPRECATED_CHANNEL_TABLE = {new int[]{41, 6, 11, 36, 40}, new int[]{77, 36, 40, 0, 0}, new int[]{78, 36, 40, 0, 0}, new int[]{79, 36, 40, 0, 0}};
    private static final String[] FOREGROUND_SCAN_PACKAGES = {"com.android.settings", "com.huawei.lbs"};
    private static final int INVALID_SLOT_ID = -1;
    private static final boolean IS_DEBUG_VERBOSE = false;
    public static final int LOG_LEVEL_DEBUG = 1;
    public static final int LOG_LEVEL_ERROR = 0;
    public static final int LOG_LEVEL_VERBOSE = 3;
    public static final int MAX_SLOT_ID = 2;
    public static final int MSG_CALL_STATE_IDLE = 22;
    public static final int MSG_CALL_STATE_OFFHOOK = 23;
    public static final int MSG_CALL_STATE_RINGING = 24;
    public static final int MSG_CELL_STATE_CHANGED = 8;
    public static final int MSG_INITIALIZE = 1;
    public static final int MSG_LOWLATENCY_CHANGED = 21;
    public static final int MSG_NETWORK_CHANGED = 19;
    public static final int MSG_P2P_CONNECTED = 16;
    public static final int MSG_SOFTAP_DISABLED = 6;
    public static final int MSG_SOFTAP_ENABLED = 5;
    public static final int MSG_WIFI_CONNECTED = 4;
    public static final int MSG_WIFI_CONNECTING = 12;
    public static final int MSG_WIFI_CONNECT_TIMEOUT = 13;
    public static final int MSG_WIFI_DISABLED = 3;
    public static final int MSG_WIFI_DISCONNECTED = 7;
    public static final int MSG_WIFI_DISPLAY_CONNECTED = 18;
    public static final int MSG_WIFI_ENABLED = 2;
    public static final int MSG_WIFI_RPT_ENABLED = 17;
    public static final int MSG_WIFI_SCANNING = 10;
    public static final int MSG_WIFI_SCAN_TIMEOUT = 11;
    public static final int NETWORK_CELL = 801;
    public static final int NETWORK_UNKNOWN = 802;
    public static final int NETWORK_WIFI = 800;
    private static final int[][] NRFREQ_BAND_TABLE = {new int[]{2110000, 2170000, 1}, new int[]{1930000, 1990000, 2}, new int[]{1805000, 1880000, 3}, new int[]{869000, 894000, 5}, new int[]{2620000, 2690000, 7}, new int[]{925000, 960000, 8}, new int[]{729000, 746000, 12}, new int[]{791000, 821000, 20}, new int[]{1930000, 1995000, 25}, new int[]{758000, 803000, 28}, new int[]{2010000, 2025000, 34}, new int[]{2570000, 2620000, 38}, new int[]{1880000, 1920000, 39}, new int[]{2300000, 2400000, 40}, new int[]{2496000, 2690000, 41}, new int[]{1432000, 1517000, 50}, new int[]{1427000, 1432000, 51}, new int[]{2110000, 2200000, 66}, new int[]{1995000, 2020000, 70}, new int[]{617000, 652000, 71}, new int[]{1475000, 1518000, 74}, new int[]{1432000, 1517000, 75}, new int[]{1427000, 1432000, 76}, new int[]{3300000, 4200000, 77}, new int[]{3300000, 3800000, 78}, new int[]{4400000, 5000000, 79}, new int[]{26500000, 29500000, 257}, new int[]{24250000, 27500000, 258}, new int[]{37000000, 40000000, 260}, new int[]{27500000, 28350000, 261}};
    private static final int NR_ARFCN_HIGH_MAX = 3279165;
    private static final int NR_ARFCN_LOW_MAX = 599999;
    private static final int NR_ARFCN_MIDDLE_MAX = 2016666;
    private static final int NR_ARFCN_OFFSET_HIGH = 2016667;
    private static final int NR_ARFCN_OFFSET_LOW = 0;
    private static final int NR_ARFCN_OFFSET_MIDDLE = 600000;
    public static final int NR_BAND_NO_DEFAULT = 41;
    public static final int NR_BAND_NO_INVALID = -1;
    private static final int NR_DELTA_FREQ_GLOBAL_HIGH = 60;
    private static final int NR_DELTA_FREQ_GLOBAL_LOW = 5;
    private static final int NR_DELTA_FREQ_GLOBAL_MIDDLE = 15;
    private static final int NR_FREQ_OFFSET_HIGH = 24250080;
    private static final int NR_FREQ_OFFSET_LOW = 0;
    private static final int NR_FREQ_OFFSET_MIDDLE = 3000000;
    private static final int[] RECOMMEND_CHANNEL_LIST = {1, 2, 3};
    private static final String TAG = "HiCoexUtils";
    public static final int TIMEOUT_CONNECT = 10000;
    public static final int TIMEOUT_SCAN = 3000;
    public static final int TYPE_COEX_CELL = 0;
    public static final int TYPE_COEX_WIFI = 1;
    private static boolean isDebugEnable = false;

    private HiCoexUtils() {
    }

    public static List<Integer> getRecommendWiFiChannel(int freq) {
        if (freq < CELL_FREQ_NR41_START || freq > CELL_FREQ_NR41_END) {
            return null;
        }
        List<Integer> channels = new ArrayList<>(13);
        for (int channel : RECOMMEND_CHANNEL_LIST) {
            channels.add(new Integer(channel));
        }
        return channels;
    }

    public static List<Integer> getDeprecatedWiFiChannel(int bandNo) {
        int idx = 0;
        while (true) {
            int[][] iArr = DEPRECATED_CHANNEL_TABLE;
            if (idx >= iArr.length) {
                return Collections.emptyList();
            }
            if (bandNo != iArr[idx][0]) {
                idx++;
            } else {
                List<Integer> channels = new ArrayList<>(13);
                int subIdx = 1;
                while (true) {
                    int[][] iArr2 = DEPRECATED_CHANNEL_TABLE;
                    if (subIdx >= iArr2[idx].length) {
                        return channels;
                    }
                    if (iArr2[idx][subIdx] > 0) {
                        channels.add(new Integer(iArr2[idx][subIdx]));
                    }
                    subIdx++;
                }
            }
        }
    }

    public static boolean isCmccOperator(String operatorNumeric) {
        for (String plmn : CMCC_PLMNS) {
            if (plmn.equals(operatorNumeric)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isForegroundScanInList(String packageName) {
        for (String limitPkgName : FOREGROUND_SCAN_PACKAGES) {
            if (limitPkgName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSubIdValid(int subId) {
        if (subId >= 0) {
            return true;
        }
        return false;
    }

    public static int getSlotId(int subId) {
        if (!isSubIdValid(subId)) {
            return -1;
        }
        return SubscriptionManagerEx.getSlotIndex(subId);
    }

    public static boolean isActiveSubId(int subId) {
        if (isSubIdValid(subId) && isSlotIdValid(getSlotId(subId))) {
            return true;
        }
        return false;
    }

    public static boolean isSlotIdValid(int slotId) {
        return slotId >= 0 && slotId < 2;
    }

    public static int booleanToInt(boolean value) {
        return value ? 1 : 0;
    }

    public static void log(int level, String msg) {
        log(level, TAG, msg);
    }

    public static void log(int level, String tag, String msg) {
        if (level == 0) {
            HwHiLog.e(tag, false, msg, new Object[0]);
        } else if (level != 1) {
            boolean z = isDebugEnable;
        } else if (isDebugEnable) {
            HwHiLog.d(tag, false, msg, new Object[0]);
        }
    }

    public static void logD(String tag, String msg) {
        log(1, tag, msg);
    }

    public static void logE(String tag, String msg) {
        log(0, tag, msg);
    }

    public static void logV(String tag, String msg) {
        log(3, tag, msg);
    }

    public static void logD(String msg) {
        log(1, msg);
    }

    public static void logE(String msg) {
        log(0, msg);
    }

    public static void logV(String msg) {
        log(3, msg);
    }

    public static boolean isDebugEnable() {
        return isDebugEnable;
    }

    public static void initialDebugEnable() {
        isDebugEnable = Log.HWLog || Log.HWINFO;
    }

    public static int calculateNrBandByNrArfcn(int nrArfcn) {
        int arfcnOffset;
        int deltaFreqGlobal;
        int freqOffset;
        if (nrArfcn >= 0 && nrArfcn <= NR_ARFCN_LOW_MAX) {
            freqOffset = 0;
            deltaFreqGlobal = 5;
            arfcnOffset = 0;
        } else if (nrArfcn > NR_ARFCN_LOW_MAX && nrArfcn <= NR_ARFCN_MIDDLE_MAX) {
            freqOffset = NR_FREQ_OFFSET_MIDDLE;
            deltaFreqGlobal = 15;
            arfcnOffset = NR_ARFCN_OFFSET_MIDDLE;
        } else if (nrArfcn <= NR_ARFCN_MIDDLE_MAX || nrArfcn > NR_ARFCN_HIGH_MAX) {
            return -1;
        } else {
            freqOffset = NR_FREQ_OFFSET_HIGH;
            deltaFreqGlobal = NR_DELTA_FREQ_GLOBAL_HIGH;
            arfcnOffset = NR_ARFCN_OFFSET_HIGH;
        }
        int freq = ((nrArfcn - arfcnOffset) * deltaFreqGlobal) + freqOffset;
        int idx = 0;
        while (true) {
            int[][] iArr = NRFREQ_BAND_TABLE;
            if (idx >= iArr.length) {
                return -1;
            }
            if (freq >= iArr[idx][0] && freq <= iArr[idx][1]) {
                return iArr[idx][2];
            }
            idx++;
        }
    }
}
