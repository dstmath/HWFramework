package com.android.internal.telephony.vsim;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.util.Log;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneNotifier;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.dataconnection.ApnSetting;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.google.android.mms.pdu.CharacterSets;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public class VSimUtilsInner {
    public static final String DEVICE_ID_PREF = "deviceId_pending_for_vsim";
    public static final String DEVICE_SVN_PREF = "deviceSvn_pending_for_vsim";
    public static final String ESN_PREF = "esn_pending_for_vsim";
    private static boolean HWFLOW = false;
    public static final String IMEI_PREF = "imei_pending_for_vsim";
    private static final int IS_M2_CS_ONLY = 4;
    private static final int IS_MMS_ON_M2 = 5;
    private static final int IS_RADIO_AVAILABLE = 7;
    private static final int IS_SUB_ON_M2 = 6;
    private static final int IS_VSIM_CAUSE_CARD_RELOAD = 8;
    private static final int IS_VSIM_ENABLED = 9;
    private static final int IS_VSIM_IN_PROCESS = 2;
    private static final int IS_VSIM_ON = 1;
    private static final int IS_VSIM_RECONNECTING = 3;
    private static final String LOG_TAG = "VSimUtilsInner";
    public static final String MEID_PREF = "meid_pending_for_vsim";
    private static final int MMS_START = 1;
    private static final int MMS_STOP = 2;
    private static final int NEED_BLOCK_PIN = 10;
    private static final int NEED_BLOCK_UNRESERVED_SUBID = 11;
    private static final int PHONE_COUNT = 2;
    private static final int VSIM_MODEM_COUNT = SystemProperties.getInt("ro.radio.vsim_modem_count", 3);
    private static final int VSIM_MODEM_COUNT_DEFAULT = 3;
    private static final int VSIM_MODEM_COUNT_DUAL = 2;
    private static final int VSIM_MODEM_COUNT_ERROR = -1;
    private static final int VSIM_MODEM_COUNT_FAKE_TRIPPLE = 3;
    private static final int VSIM_MODEM_COUNT_NOT_SUPPORT = 0;
    private static final int VSIM_MODEM_COUNT_REAL_TRIPPLE = 4;
    private static final int VSIM_MODEM_COUNT_SINGLE = 1;
    protected static final boolean sIsPlatformSupportVSim = SystemProperties.getBoolean("ro.radio.vsim_support", false);
    private static boolean[] sLegacyMarkTable = new boolean[2];

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(LOG_TAG, 4) : false : true;
        HWFLOW = isLoggable;
        for (int i = 0; i < 2; i++) {
            sLegacyMarkTable[i] = false;
        }
    }

    private VSimUtilsInner() {
    }

    public static boolean isPlatformTwoModems() {
        boolean z = false;
        if (!sIsPlatformSupportVSim) {
            return false;
        }
        if (VSIM_MODEM_COUNT == 2) {
            z = true;
        }
        return z;
    }

    public static boolean isPlatformRealTripple() {
        boolean z = false;
        if (!sIsPlatformSupportVSim) {
            return false;
        }
        if (VSIM_MODEM_COUNT == 4) {
            z = true;
        }
        return z;
    }

    public static boolean isVSimSub(int sub) {
        boolean z = false;
        if (!sIsPlatformSupportVSim) {
            return false;
        }
        if (sub == 2) {
            z = true;
        }
        return z;
    }

    public static boolean isVSimOn() {
        if (sIsPlatformSupportVSim) {
            return HwTelephonyFactory.getHwInnerVSimManager().isVSimInStatus(1, -1);
        }
        return false;
    }

    public static boolean isVSimInProcess() {
        if (sIsPlatformSupportVSim) {
            return HwTelephonyFactory.getHwInnerVSimManager().isVSimInStatus(2, -1);
        }
        return false;
    }

    public static boolean isVSimReconnecting() {
        if (sIsPlatformSupportVSim) {
            return HwTelephonyFactory.getHwInnerVSimManager().isVSimInStatus(3, -1);
        }
        return false;
    }

    public static void setLastRilFailCause(int cause) {
        if (sIsPlatformSupportVSim) {
            HwTelephonyFactory.getHwInnerVSimManager().setLastRilFailCause(cause);
        }
    }

    public static void registerForIccChanged(Handler h, int what, Object obj) {
        if (sIsPlatformSupportVSim) {
            HwTelephonyFactory.getHwInnerVSimManager().registerForIccChanged(h, what, obj);
        }
    }

    public static void unregisterForIccChanged(Handler h) {
        if (sIsPlatformSupportVSim) {
            HwTelephonyFactory.getHwInnerVSimManager().unregisterForIccChanged(h);
        }
    }

    public static boolean isVSimPhone(Phone phone) {
        if (sIsPlatformSupportVSim) {
            return HwTelephonyFactory.getHwInnerVSimManager().isVSimPhone(phone);
        }
        return false;
    }

    public static IccRecords fetchVSimIccRecords(int family) {
        return HwTelephonyFactory.getHwInnerVSimManager().fetchVSimIccRecords(family);
    }

    public static UiccCardApplication getVSimUiccCardApplication(int appFamily) {
        return HwTelephonyFactory.getHwInnerVSimManager().getVSimUiccCardApplication(appFamily);
    }

    private static ApnSetting makeVSimApnSetting() {
        return new ApnSetting(0, "00000", "vsim", "apn", "", "", "", "", "", "", "", -1, parseTypes("default,supl"), "IP", "IP", true, 0, 0, 0, false, 0, 0, 0, 0, "", "");
    }

    private static String[] parseTypes(String types) {
        if (types != null && !types.equals("")) {
            return types.split(",");
        }
        return new String[]{CharacterSets.MIMENAME_ANY_CHARSET};
    }

    public static ArrayList<ApnSetting> createVSimApnList() {
        ArrayList<ApnSetting> result = new ArrayList();
        result.add(makeVSimApnSetting());
        if (HWFLOW) {
            Rlog.i(LOG_TAG, "createVSimApnList: X result=" + result);
        }
        return result;
    }

    public static void checkMmsStart(int subId) {
        if (sIsPlatformSupportVSim) {
            HwTelephonyFactory.getHwInnerVSimManager().checkMmsForVSim(1, subId);
        }
    }

    public static void checkMmsStop(int subId) {
        if (sIsPlatformSupportVSim) {
            HwTelephonyFactory.getHwInnerVSimManager().checkMmsForVSim(2, subId);
        }
    }

    public static boolean isSubOnM2(int subId) {
        if (sIsPlatformSupportVSim) {
            return HwTelephonyFactory.getHwInnerVSimManager().isVSimInStatus(6, subId);
        }
        return false;
    }

    public static boolean isMmsOnM2() {
        if (sIsPlatformSupportVSim) {
            return HwTelephonyFactory.getHwInnerVSimManager().isVSimInStatus(5, -1);
        }
        return false;
    }

    public static boolean isM2CSOnly() {
        if (sIsPlatformSupportVSim) {
            return HwTelephonyFactory.getHwInnerVSimManager().isVSimInStatus(4, -1);
        }
        return false;
    }

    public static boolean isVSimFiltrateApn(int subId, int type) {
        if (subId != 2 || type == 0 || 3 == type || 4 == type || 5 == type) {
            return false;
        }
        return true;
    }

    public static void setMarkForCardReload(int subId, boolean value) {
        if (sIsPlatformSupportVSim) {
            if (isVSimCauseCardReload()) {
                setVSimLegacyReloadMark(subId, true);
            }
            HwTelephonyFactory.getHwInnerVSimManager().setMarkForCardReload(subId, value);
        }
    }

    public static boolean isRadioAvailable(int subId) {
        if (sIsPlatformSupportVSim) {
            return HwTelephonyFactory.getHwInnerVSimManager().isVSimInStatus(7, subId);
        }
        return true;
    }

    public static void makeVSimPhoneFactory(Context context, PhoneNotifier notifier, Phone[] pps, CommandsInterface[] cis) {
        if (sIsPlatformSupportVSim) {
            HwTelephonyFactory.getHwInnerVSimManager().makeVSimPhoneFactory(context, notifier, pps, cis);
        }
    }

    public static Phone getVSimPhone() {
        return HwTelephonyFactory.getHwInnerVSimManager().getVSimPhone();
    }

    public static void dumpVSimPhoneFactory(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (sIsPlatformSupportVSim) {
            HwTelephonyFactory.getHwInnerVSimManager().dumpVSimPhoneFactory(fd, pw, args);
        }
    }

    public static ServiceStateTracker makeVSimServiceStateTracker(Phone phone, CommandsInterface ci) {
        if (sIsPlatformSupportVSim) {
            return HwTelephonyFactory.getHwInnerVSimManager().makeVSimServiceStateTracker(phone, ci);
        }
        return null;
    }

    public static boolean isVSimCauseCardReload() {
        if (sIsPlatformSupportVSim) {
            return HwTelephonyFactory.getHwInnerVSimManager().isVSimInStatus(8, -1);
        }
        return false;
    }

    public static String getPendingDeviceInfoFromSP(String prefKey) {
        if (sIsPlatformSupportVSim) {
            return HwTelephonyFactory.getHwInnerVSimManager().getPendingDeviceInfoFromSP(prefKey);
        }
        return null;
    }

    public static boolean isVSimEnabled() {
        if (sIsPlatformSupportVSim) {
            return HwTelephonyFactory.getHwInnerVSimManager().isVSimInStatus(9, -1);
        }
        return false;
    }

    public static boolean needBlockPin(int subId) {
        if (sIsPlatformSupportVSim) {
            return HwTelephonyFactory.getHwInnerVSimManager().isVSimInStatus(10, subId);
        }
        return false;
    }

    public static boolean needBlockUnReservedForVsim(int subId) {
        if (sIsPlatformSupportVSim) {
            return HwTelephonyFactory.getHwInnerVSimManager().isVSimInStatus(11, subId);
        }
        return false;
    }

    private static boolean isIccStateChangedByVSimReload(String value, int subId) {
        if (!"ABSENT".equals(value) && !"READY".equals(value) && !"IMSI".equals(value) && !"LOADED".equals(value)) {
            return false;
        }
        if (isVSimEnabled() || isVSimCauseCardReload()) {
            if ("LOADED".equals(value)) {
                setVSimLegacyReloadMark(subId, false);
            }
            return true;
        } else if (!isVSimLegacyReloadMark(subId)) {
            return false;
        } else {
            setVSimLegacyReloadMark(subId, false);
            return true;
        }
    }

    private static boolean isVSimLegacyReloadMark(int subId) {
        if (subId < 0 || subId >= 2) {
            return false;
        }
        return sLegacyMarkTable[subId];
    }

    private static void setVSimLegacyReloadMark(int subId, boolean value) {
        if (subId >= 0 && subId < 2) {
            if (HWFLOW) {
                Rlog.i(LOG_TAG, "set sLegacyMarkTable[" + subId + "] = " + value);
            }
            sLegacyMarkTable[subId] = value;
        }
    }

    public static void putVSimExtraForIccStateChanged(Intent intent, int subId, String value) {
        if (sIsPlatformSupportVSim && isIccStateChangedByVSimReload(value, subId)) {
            String INTENT_KEY_VSIM = "vsim";
            String INTENT_VALUE_VSIM_RELOAD = "VSIM_RELOAD";
            if (HWFLOW) {
                Rlog.i(LOG_TAG, "vsim add extra param for ACTION_SIM_STATE_CHANGED as vsim reload");
            }
            intent.putExtra("vsim", "VSIM_RELOAD");
        }
    }

    public static int getTopPrioritySubscriptionId() {
        if (sIsPlatformSupportVSim) {
            return HwTelephonyFactory.getHwInnerVSimManager().getTopPrioritySubscriptionId();
        }
        return SubscriptionManager.getDefaultDataSubscriptionId();
    }
}
