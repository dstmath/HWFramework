package com.android.internal.telephony;

import android.content.ContentResolver;
import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConfig;
import com.android.internal.telephony.fullnetwork.HwFullNetworkManager;
import java.util.HashMap;
import java.util.Map;

public class HwNetworkTypeUtils {
    public static final int INVALID_NETWORK_MODE = -1;
    public static final boolean IS_DUAL_IMS_SUPPORTED = HwModemCapability.isCapabilitySupport(21);
    public static final boolean IS_MODEM_FULL_PREFMODE_SUPPORTED = HwModemCapability.isCapabilitySupport(3);
    private static final String LOG_TAG = "HwNetworkTypeUtils";
    public static final int LTE_SERVICE_OFF = 0;
    public static final int LTE_SERVICE_ON = 1;
    private static HashMap<Integer, Integer> LteOnOffMapping = new HashMap<>();
    private static int lteOffMappingMode;
    private static int lteOnMappingMode;

    static {
        lteOnMappingMode = -1;
        lteOffMappingMode = -1;
        LteOnOffMapping.put(8, 4);
        LteOnOffMapping.put(9, 3);
        LteOnOffMapping.put(10, 7);
        LteOnOffMapping.put(11, 7);
        LteOnOffMapping.put(12, 2);
        LteOnOffMapping.put(15, 13);
        LteOnOffMapping.put(17, 16);
        LteOnOffMapping.put(19, 14);
        LteOnOffMapping.put(20, 18);
        LteOnOffMapping.put(22, 21);
        LteOnOffMapping.put(25, 24);
        LteOnOffMapping.put(26, 1);
        LteOnOffMapping.put(61, 52);
        lteOnMappingMode = SystemProperties.getInt("ro.telephony.default_network", -1);
        if (LteOnOffMapping.containsKey(Integer.valueOf(lteOnMappingMode))) {
            lteOffMappingMode = LteOnOffMapping.get(Integer.valueOf(lteOnMappingMode)).intValue();
        } else {
            lteOnMappingMode = -1;
        }
        String[] lteOnOffMapings = SystemProperties.get("ro.hwpp.lteonoff_mapping", "0,0").split(",");
        if (lteOnOffMapings.length == 2 && Integer.parseInt(lteOnOffMapings[0]) != 0) {
            lteOnMappingMode = Integer.parseInt(lteOnOffMapings[0]);
            lteOffMappingMode = Integer.parseInt(lteOnOffMapings[1]);
        }
    }

    public static int getOnModeFromMapping(int curPrefMode) {
        int onKey = -1;
        for (Map.Entry<Integer, Integer> entry : LteOnOffMapping.entrySet()) {
            if (curPrefMode == entry.getValue().intValue()) {
                onKey = entry.getKey().intValue();
                if (7 != curPrefMode || 11 != onKey) {
                    break;
                }
            }
        }
        if (HwFullNetworkConfig.IS_CMCC_4GSWITCH_DISABLE || onKey != 26) {
            return onKey;
        }
        return 9;
    }

    public static int getOffModeFromMapping(int curPrefMode) {
        if (LteOnOffMapping.containsKey(Integer.valueOf(curPrefMode))) {
            return LteOnOffMapping.get(Integer.valueOf(curPrefMode)).intValue();
        }
        return -1;
    }

    public static boolean isLteServiceOn(int curPrefMode) {
        return LteOnOffMapping.containsKey(Integer.valueOf(curPrefMode)) || curPrefMode == 23 || curPrefMode == 30 || curPrefMode == 31 || curPrefMode == 63;
    }

    public static int getLteOnMappingMode() {
        return lteOnMappingMode;
    }

    public static int getLteOffMappingMode() {
        return lteOffMappingMode;
    }

    public static boolean isDualImsSwitchOpened() {
        return 1 == SystemProperties.getInt("persist.radio.dualltecap", 0);
    }

    public static int getNetworkModeFromDB(Context context, int subId) {
        int curPrefMode;
        ContentResolver contentResolver = context.getContentResolver();
        int curPrefMode2 = Settings.Global.getInt(contentResolver, "preferred_network_mode" + subId, -1);
        if (curPrefMode2 != -1) {
            log("getNetworkModeFromDB: Google Rule is valid, return. (subid,networkMode)=(" + subId + "," + curPrefMode2 + ").");
            return curPrefMode2;
        }
        log("getNetworkModeFromDB: Google Rule is invalid, try to recover.");
        if (IS_MODEM_FULL_PREFMODE_SUPPORTED && TelephonyManager.getDefault().isMultiSimEnabled()) {
            curPrefMode = getNetworkModeBySubIdForQcom(context, subId, -1);
            if (curPrefMode == -1) {
                curPrefMode = RILConstants.PREFERRED_NETWORK_MODE;
                log("getNetworkModeFromDB: Qcom Rule is invalid, use default.");
            } else {
                log("getNetworkModeFromDB: Qcom Rule is valid.");
            }
        } else if (!IS_DUAL_IMS_SUPPORTED || !TelephonyManager.getDefault().isMultiSimEnabled()) {
            curPrefMode = getNetworkModeForHisiSingleIms(context, -1);
            if (curPrefMode == -1) {
                curPrefMode = RILConstants.PREFERRED_NETWORK_MODE;
                log("getNetworkModeFromDB: Hisi Single Ims Rule is invalid, use default.");
            } else {
                log("getNetworkModeFromDB: Hisi Single Ims Rule is valid.");
            }
        } else {
            int mainSlotPreMode = getNetworkModeForHisiSingleIms(context, -1);
            if (subId != HwTelephonyManagerInner.getDefault().getDefault4GSlotId()) {
                curPrefMode = isDualImsSwitchOpened() ? RILConstants.PREFERRED_NETWORK_MODE : 3;
                log("getNetworkModeFromDB: This is not the main slot, use default.");
            } else if (mainSlotPreMode == -1) {
                curPrefMode = RILConstants.PREFERRED_NETWORK_MODE;
                log("getNetworkModeFromDB: This is the main slot, but Hisi Single Ims Rule is invalid, use default.");
            } else {
                curPrefMode = mainSlotPreMode;
                log("getNetworkModeFromDB: This is the main slot, and Hisi Single Ims Rule is valid.");
            }
        }
        log("getNetworkModeFromDB: To fresh Google Rule, using (subid,networkMode)=(" + subId + "," + curPrefMode + ").");
        saveNetworkModeToDB(context, subId, curPrefMode);
        return curPrefMode;
    }

    public static void saveNetworkModeToDB(Context context, int subId, int mode) {
        ContentResolver contentResolver = context.getContentResolver();
        Settings.Global.putInt(contentResolver, "preferred_network_mode" + subId, mode);
        if (IS_MODEM_FULL_PREFMODE_SUPPORTED && TelephonyManager.getDefault().isMultiSimEnabled()) {
            TelephonyManager.putIntAtIndex(context.getContentResolver(), "preferred_network_mode", subId, mode);
        } else if (!IS_DUAL_IMS_SUPPORTED || !TelephonyManager.getDefault().isMultiSimEnabled()) {
            Settings.Global.putInt(context.getContentResolver(), "preferred_network_mode", mode);
        } else if (subId == HwTelephonyManagerInner.getDefault().getDefault4GSlotId()) {
            Settings.Global.putInt(context.getContentResolver(), "preferred_network_mode", mode);
        }
        log("saveNetworkModeToDB, Succeed to save network mode. (subid,networkMode)=(" + subId + "," + mode + ").");
    }

    private static int getNetworkModeBySubIdForQcom(Context context, int subId, int defaultValue) {
        int curPrefMode = defaultValue;
        try {
            return TelephonyManager.getIntAtIndex(context.getContentResolver(), "preferred_network_mode", subId);
        } catch (Settings.SettingNotFoundException e) {
            loge("getNetworkModeBySubIdForQcom, Setting Not Found");
            return curPrefMode;
        }
    }

    private static int getNetworkModeForHisiSingleIms(Context context, int defaultValue) {
        return Settings.Global.getInt(context.getContentResolver(), "preferred_network_mode", defaultValue);
    }

    private static void log(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    private static void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }

    public static void exchangeDualCardNetworkModeDB(Context context) {
        int networkTypeForSub1 = getNetworkModeFromDB(context, 0);
        int networkTypeForSub2 = getNetworkModeFromDB(context, 1);
        log("exchangeDualCardNetworkModeDB: sub1 is " + networkTypeForSub1 + ", sub2 is " + networkTypeForSub2);
        if (-1 != networkTypeForSub1 && -1 != networkTypeForSub2) {
            if (HwTelephonyManagerInner.getDefault().getDefault4GSlotId() == 0) {
                if (isLteServiceOn(networkTypeForSub1) && (HwFullNetworkManager.getInstance().isCMCCHybird() || !isDualImsSwitchOpened())) {
                    networkTypeForSub1 = 3;
                    log("exchangeDualCardNetworkModeDB: sub2 is slave sim card, shouldn't have LTE ability.");
                }
            } else if (isLteServiceOn(networkTypeForSub2) && (HwFullNetworkManager.getInstance().isCMCCHybird() || !isDualImsSwitchOpened())) {
                networkTypeForSub2 = 3;
                log("exchangeDualCardNetworkModeDB: sub1 is slave sim card, shouldn't have LTE ability.");
            }
            saveNetworkModeToDB(context, 0, networkTypeForSub2);
            saveNetworkModeToDB(context, 1, networkTypeForSub1);
        }
    }

    public static boolean isNrServiceOn(int curPrefMode) {
        return curPrefMode == 64 || curPrefMode == 65 || curPrefMode == 66 || curPrefMode == 67 || curPrefMode == 68 || curPrefMode == 69;
    }
}
