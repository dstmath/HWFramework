package com.android.internal.telephony;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.HwTelephonyManagerInnerUtils;
import com.android.internal.telephony.fullnetwork.HwFullNetworkManager;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.internal.telephony.PhoneExt;
import java.util.HashMap;
import java.util.Map;

public class HwNetworkTypeUtils {
    private static final int DEFAULT_DUAL_NR_SWITCH_STATE = SystemPropertiesEx.getInt("hw_mc.radio.defdualnrcap", 0);
    private static final int INVALID_NETWORK_MODE = -1;
    public static final boolean IS_DUAL_IMS_SUPPORTED = HwModemCapability.isCapabilitySupport(21);
    public static final boolean IS_MODEM_FULL_PREFMODE_SUPPORTED = HwModemCapability.isCapabilitySupport(3);
    private static final String LOG_TAG = "HwNetworkTypeUtils";
    private static final int SWITCH_OFF = 0;
    private static final int SWITCH_ON = 1;
    private static int lteOffMappingMode;
    private static int lteOnMappingMode;
    private static int nrOffMappingMode;
    private static int nrOnMappingMode;
    private static HashMap<Integer, Integer> serviceOnOffMapping = new HashMap<>();

    static {
        lteOnMappingMode = -1;
        lteOffMappingMode = -1;
        nrOnMappingMode = -1;
        nrOffMappingMode = -1;
        serviceOnOffMapping.put(8, 4);
        serviceOnOffMapping.put(9, 3);
        serviceOnOffMapping.put(10, 7);
        serviceOnOffMapping.put(11, 7);
        serviceOnOffMapping.put(12, 2);
        serviceOnOffMapping.put(15, 13);
        serviceOnOffMapping.put(17, 16);
        serviceOnOffMapping.put(19, 14);
        serviceOnOffMapping.put(20, 18);
        serviceOnOffMapping.put(22, 21);
        serviceOnOffMapping.put(25, 24);
        serviceOnOffMapping.put(26, 1);
        serviceOnOffMapping.put(61, 52);
        serviceOnOffMapping.put(64, 8);
        serviceOnOffMapping.put(65, 9);
        serviceOnOffMapping.put(66, 10);
        serviceOnOffMapping.put(67, 11);
        serviceOnOffMapping.put(68, 12);
        serviceOnOffMapping.put(69, 22);
        int serviceOnMappingMode = SystemPropertiesEx.getInt("ro.telephony.default_network", -1);
        if (isNrServiceOn(serviceOnMappingMode)) {
            nrOnMappingMode = serviceOnMappingMode;
            nrOffMappingMode = serviceOnOffMapping.get(Integer.valueOf(nrOnMappingMode)).intValue();
            lteOnMappingMode = nrOffMappingMode;
        } else {
            lteOnMappingMode = serviceOnMappingMode;
        }
        if (serviceOnOffMapping.containsKey(Integer.valueOf(lteOnMappingMode))) {
            lteOffMappingMode = serviceOnOffMapping.get(Integer.valueOf(lteOnMappingMode)).intValue();
        }
        String[] lteOnOffMapings = SystemPropertiesEx.get("ro.hwpp.lteonoff_mapping", "0,0").split(",");
        if (lteOnOffMapings.length == 2) {
            try {
                if (Integer.parseInt(lteOnOffMapings[0]) != 0) {
                    lteOnMappingMode = Integer.parseInt(lteOnOffMapings[0]);
                    lteOffMappingMode = Integer.parseInt(lteOnOffMapings[1]);
                }
            } catch (NumberFormatException e) {
                loge("set value NumberFormatException,");
            }
        }
    }

    public static int getOnModeFromMapping(int curPrefMode) {
        int onKey = -1;
        for (Map.Entry<Integer, Integer> entry : serviceOnOffMapping.entrySet()) {
            if (curPrefMode == entry.getValue().intValue()) {
                onKey = entry.getKey().intValue();
                if (!(7 == curPrefMode && 11 == onKey)) {
                    break;
                }
            }
        }
        if (HwFullNetworkManager.getInstance().isCMCCDsdxDisable() || onKey != 26) {
            return onKey;
        }
        return 9;
    }

    public static int getOffModeFromMapping(int curPrefMode) {
        if (serviceOnOffMapping.containsKey(Integer.valueOf(curPrefMode))) {
            return serviceOnOffMapping.get(Integer.valueOf(curPrefMode)).intValue();
        }
        return -1;
    }

    public static boolean isLteServiceOn(int curPrefMode) {
        if (curPrefMode == 23 || curPrefMode == 63 || curPrefMode == 30 || curPrefMode == 31) {
            return true;
        }
        if (!serviceOnOffMapping.containsKey(Integer.valueOf(curPrefMode)) || isNrServiceOn(curPrefMode)) {
            return false;
        }
        return true;
    }

    public static boolean isDualImsSwitchOpened() {
        return SystemPropertiesEx.getInt("persist.radio.dualltecap", 0) == 1;
    }

    public static boolean isDualNrSwitchOpened(Context context) {
        if (context == null || context.getContentResolver() == null) {
            return false;
        }
        String result = Settings.Global.getString(context.getContentResolver(), "dual_sim_nr_enabler");
        if ("true".equalsIgnoreCase(result)) {
            return true;
        }
        if (!"false".equalsIgnoreCase(result) && DEFAULT_DUAL_NR_SWITCH_STATE == 1) {
            return true;
        }
        return false;
    }

    public static int getNetworkModeFromDB(Context context, int slotId) {
        int curPrefMode;
        ContentResolver contentResolver = context.getContentResolver();
        int curPrefMode2 = Settings.Global.getInt(contentResolver, "preferred_network_mode" + slotId, -1);
        if (curPrefMode2 != -1) {
            log("getNetworkModeFromDB: original Rule is valid, (slotId, mode)=(" + slotId + "," + curPrefMode2 + ").");
            return curPrefMode2;
        }
        log("getNetworkModeFromDB: original Rule is invalid, try to recover.");
        if (IS_MODEM_FULL_PREFMODE_SUPPORTED && TelephonyManagerEx.isMultiSimEnabled()) {
            curPrefMode = calcPreferNetworkModeForQcomAndMtk(context, slotId);
        } else if (!IS_DUAL_IMS_SUPPORTED || !TelephonyManagerEx.isMultiSimEnabled()) {
            curPrefMode = getNetworkModeForHisiSingleIms(context, -1);
            if (curPrefMode == -1) {
                curPrefMode = PhoneExt.getPreferredNetworkMode();
                log("getNetworkModeFromDB: Hisi Single Ims Rule is invalid, use default.");
            } else {
                log("getNetworkModeFromDB: Hisi Single Ims Rule is valid.");
            }
        } else {
            int mainSlotPreMode = getNetworkModeForHisiSingleIms(context, -1);
            if (slotId != HwTelephonyManagerInner.getDefault().getDefault4GSlotId()) {
                int i = 3;
                if (HwModemCapability.isCapabilitySupport(29)) {
                    if (isDualImsSwitchOpened()) {
                        i = 10;
                    }
                    curPrefMode = i;
                } else {
                    if (isDualImsSwitchOpened()) {
                        i = PhoneExt.getPreferredNetworkMode();
                    }
                    curPrefMode = i;
                }
                log("getNetworkModeFromDB: This is not the main slot, use default.");
            } else if (mainSlotPreMode == -1) {
                curPrefMode = PhoneExt.getPreferredNetworkMode();
                log("getNetworkModeFromDB: main slot, Hisi Single Ims Rule is invalid, use default.");
            } else {
                curPrefMode = mainSlotPreMode;
                log("getNetworkModeFromDB: main slot, and Hisi Single Ims Rule is valid.");
            }
        }
        log("getNetworkModeFromDB: To refresh db, using (slotId, mode)=(" + slotId + "," + curPrefMode + ").");
        saveNetworkModeToDB(context, slotId, curPrefMode);
        return curPrefMode;
    }

    private static int calcPreferNetworkModeForQcomAndMtk(Context context, int slotId) {
        int curPrefMode;
        if (slotId == HwTelephonyManagerInner.getDefault().getDefault4GSlotId()) {
            int curPrefMode2 = PhoneExt.getPreferredNetworkMode();
            log("calcPreferNetworkModeForQcomAndMtk: This is the main slot, use default.");
            return curPrefMode2;
        }
        if (!HwTelephonyManagerInnerUtils.getDefault().isDualNrSupported() || !isDualNrSwitchOpened(context)) {
            curPrefMode = isDualImsSwitchOpened() ? 22 : 21;
        } else {
            curPrefMode = 69;
        }
        log("calcPreferNetworkModeForQcomAndMtk: This is not the main slot, use default.");
        return curPrefMode;
    }

    public static void saveNetworkModeToDB(Context context, int slotId, int mode) {
        if (!SubscriptionManagerEx.isValidSlotIndex(slotId)) {
            log("saveNetworkModeToDB, slotId " + slotId + " is invalid, return.");
            return;
        }
        ContentResolver contentResolver = context.getContentResolver();
        Settings.Global.putInt(contentResolver, "preferred_network_mode" + slotId, mode);
        if (IS_MODEM_FULL_PREFMODE_SUPPORTED && TelephonyManagerEx.isMultiSimEnabled()) {
            TelephonyManagerEx.putIntAtIndex(context.getContentResolver(), "preferred_network_mode", slotId, mode);
        } else if (!IS_DUAL_IMS_SUPPORTED || !TelephonyManagerEx.isMultiSimEnabled()) {
            Settings.Global.putInt(context.getContentResolver(), "preferred_network_mode", mode);
        } else if (slotId == HwTelephonyManagerInner.getDefault().getDefault4GSlotId()) {
            Settings.Global.putInt(context.getContentResolver(), "preferred_network_mode", mode);
        }
        log("saveNetworkModeToDB, Succeed to save network mode. (subid,networkMode)=(" + slotId + "," + mode + ").");
    }

    private static int getNetworkModeBySubIdForQcom(Context context, int slotId, int defaultValue) {
        try {
            return TelephonyManagerEx.getIntAtIndex(context.getContentResolver(), "preferred_network_mode", slotId);
        } catch (Settings.SettingNotFoundException e) {
            loge("getNetworkModeBySubIdForQcom, Setting Not Found");
            return defaultValue;
        }
    }

    private static int getNetworkModeForHisiSingleIms(Context context, int defaultValue) {
        return Settings.Global.getInt(context.getContentResolver(), "preferred_network_mode", defaultValue);
    }

    private static void log(String msg) {
        RlogEx.i(LOG_TAG, msg);
    }

    private static void loge(String msg) {
        RlogEx.e(LOG_TAG, msg);
    }

    public static void exchangeDualCardNetworkModeDB(Context context) {
        int networkTypeForSlot1 = getNetworkModeFromDB(context, 0);
        int networkTypeForSlot2 = getNetworkModeFromDB(context, 1);
        log("exchangeDualCardNetworkModeDB: sub1 is " + networkTypeForSlot1 + ", sub2 is " + networkTypeForSlot2);
        if (-1 != networkTypeForSlot1 && -1 != networkTypeForSlot2) {
            if (HwTelephonyManagerInner.getDefault().getDefault4GSlotId() == 0) {
                if (isLteServiceOn(networkTypeForSlot1) && (HwFullNetworkManager.getInstance().isCMCCHybird() || !isDualImsSwitchOpened())) {
                    networkTypeForSlot1 = 3;
                    log("exchangeDualCardNetworkModeDB: sub2 is slave sim card, shouldn't have LTE ability.");
                }
            } else if (isLteServiceOn(networkTypeForSlot2) && (HwFullNetworkManager.getInstance().isCMCCHybird() || !isDualImsSwitchOpened())) {
                networkTypeForSlot2 = 3;
                log("exchangeDualCardNetworkModeDB: sub1 is slave sim card, shouldn't have LTE ability.");
            }
            saveNetworkModeToDB(context, 0, networkTypeForSlot2);
            saveNetworkModeToDB(context, 1, networkTypeForSlot1);
        }
    }

    public static int getLteOnMappingMode() {
        return lteOnMappingMode;
    }

    public static int getLteOffMappingMode() {
        return lteOffMappingMode;
    }

    public static int getNrOnMappingMode() {
        return nrOnMappingMode;
    }

    public static int getNrOffMappingMode() {
        return nrOffMappingMode;
    }

    public static boolean isNrServiceOn(int curPrefMode) {
        return judgeNrAbilityByHwNetworkType(curPrefMode) || judgeNrAbilityByNetworkType(curPrefMode);
    }

    private static boolean judgeNrAbilityByHwNetworkType(int curPrefMode) {
        return curPrefMode == 64 || curPrefMode == 65 || curPrefMode == 66 || curPrefMode == 67 || curPrefMode == 68 || curPrefMode == 69;
    }

    private static boolean judgeNrAbilityByNetworkType(int curPrefMode) {
        return curPrefMode == 23 || curPrefMode == 24 || curPrefMode == 26 || curPrefMode == 28 || curPrefMode == 29 || curPrefMode == 30 || curPrefMode == 31 || curPrefMode == 32 || curPrefMode == 25 || curPrefMode == 27 || curPrefMode == 33;
    }
}
