package com.android.internal.telephony.vsim;

import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.HwTelephonyManagerInner;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConfig;

public class HwVSimUtilsInner {
    static final int ACTIVE_MODEM_MODE_DUAL = 1;
    static final int ACTIVE_MODEM_MODE_SINGLE = 0;
    public static final int DISABLE = 0;
    public static final int DOMAIN_CS_ONLY = 0;
    public static final int ENABLE = 1;
    public static final boolean IS_CMCC_4GSWITCH_DISABLE = HwFullNetworkConfig.IS_CMCC_4GSWITCH_DISABLE;
    public static final boolean IS_DSDSPOWER_SUPPORT = SystemProperties.getBoolean("ro.config.hw_dsdspowerup", false);
    private static final String LOG_TAG = "VSimUtilsInner";
    private static final int LTE_SERVICE_OFF = 0;
    private static final int LTE_SERVICE_ON = 1;
    public static final int NETWORK_MODE_CDMA_GSM = 59;
    private static final String PROPERTY_VSIM_DSDS_VERSION = "ro.radio.vsim_dsds_version";
    public static final int SIM = 1;
    public static final int STATE_ON = 1;
    static final int UE_OPERATION_MODE_DATA_CENTRIC = 1;
    static final int UE_OPERATION_MODE_VOICE_CENTRIC = 0;
    public static final int VSIM = 11;
    private static final int VSIM_DSDS_VERSION_DEFAULT = 0;
    private static final int VSIM_DSDS_VERSION_ONE = 1;
    private static final int VSIM_DSDS_VERSION_PROP = SystemProperties.getInt(PROPERTY_VSIM_DSDS_VERSION, 0);
    private static final int VSIM_MODEM_COUNT = SystemProperties.getInt("ro.radio.vsim_modem_count", 3);
    private static final int VSIM_MODEM_COUNT_DEFAULT = 3;
    private static final int VSIM_MODEM_COUNT_DUAL = 2;
    private static final int VSIM_MODEM_COUNT_FAKE_TRIPPLE = 3;
    private static final int VSIM_MODEM_COUNT_REAL_TRIPPLE = 4;
    public static final int VSIM_NETWORK_MODE_UNKNOWN = -1;
    public static final int VSIM_SIM_STATE_INVALID = -1;
    public static final int VSIM_SUB_INVALID = -1;

    private static void slogd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    private static void sloge(String s) {
        HwVSimLog.VSimLogE(LOG_TAG, s);
    }

    public static int getInsertedCardCount(int[] cardTypes) {
        if (cardTypes == null) {
            sloge("getInsertedCardCount, cardTypes is null !");
            return -1;
        } else if (cardTypes.length == 0) {
            slogd("getInsertedCardCount, cardTypes.length == 0 !");
            return -1;
        } else {
            int insertedCardCount = 0;
            for (int i : cardTypes) {
                if (i != 0) {
                    insertedCardCount++;
                }
            }
            return insertedCardCount;
        }
    }

    public static boolean[] getCardState(int[] cardTypes) {
        if (cardTypes == null) {
            sloge("getCardState, cardTypes is null !");
            return null;
        } else if (cardTypes.length == 0) {
            slogd("getCardState, cardTypes.length == 0 !");
            return null;
        } else {
            boolean[] isCardPresent = new boolean[cardTypes.length];
            for (int i = 0; i < cardTypes.length; i++) {
                isCardPresent[i] = cardTypes[i] != 0;
            }
            return isCardPresent;
        }
    }

    public static CommandsInterface getCiBySub(int subId, CommandsInterface vsimCi, CommandsInterface[] cis) {
        if (subId < 0 || subId > cis.length) {
            return null;
        }
        if (subId == cis.length) {
            return vsimCi;
        }
        return cis[subId];
    }

    static Phone getPhoneBySub(int subId, Phone vsimPhone, Phone[] phones) {
        if (subId < 0 || subId > 2) {
            return null;
        }
        if (subId == 2) {
            return vsimPhone;
        }
        return phones[subId];
    }

    public static Integer getCiIndex(Message msg) {
        if (msg == null) {
            return 0;
        }
        if (msg.obj != null && (msg.obj instanceof Integer)) {
            return (Integer) msg.obj;
        }
        if (msg.obj == null || !(msg.obj instanceof AsyncResult)) {
            return 0;
        }
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar.userObj == null || !(ar.userObj instanceof Integer)) {
            return 0;
        }
        return (Integer) ar.userObj;
    }

    public static boolean isRequestNotSupport(Throwable ex) {
        if (!(ex instanceof CommandException)) {
            return false;
        }
        CommandException.Error err = CommandException.Error.GENERIC_FAILURE;
        try {
            err = ((CommandException) ex).getCommandError();
        } catch (ClassCastException e) {
        }
        if (err == CommandException.Error.REQUEST_NOT_SUPPORTED) {
            return true;
        }
        return false;
    }

    public static boolean isChinaTelecom() {
        return HuaweiTelephonyConfigs.isChinaTelecom();
    }

    public static boolean isFullNetworkSupported() {
        return SystemProperties.getBoolean(HwFullNetworkConfig.PROPERTY_FULL_NETWORK_SUPPORT, false);
    }

    public static int getAnotherSlotId(int subId) {
        return subId == 0 ? 1 : 0;
    }

    public static int getSinIndexBySlotId(int subId) {
        return subId == 2 ? 11 : 1;
    }

    public static boolean isValidSlotId(int subId) {
        return subId >= 0 && subId < HwVSimModemAdapter.PHONE_COUNT;
    }

    public static boolean isPlatformTwoModems() {
        return VSIM_MODEM_COUNT == 2;
    }

    public static boolean isPlatformRealTripple() {
        return VSIM_MODEM_COUNT == 4;
    }

    public static boolean isVSimDsdsVersionOne() {
        return VSIM_DSDS_VERSION_PROP == 1;
    }

    public static boolean isPlatformTwoModemsActual() {
        return isPlatformTwoModems() || (isPlatformRealTripple() && isVSimDsdsVersionOne());
    }

    public static boolean isDualImsSupported() {
        return HwTelephonyManagerInner.getDefault().isDualImsSupported();
    }

    public static boolean isPlatformNeedWaitNvMatchUnsol() {
        return HwTelephonyManagerInner.getDefault().isDualImsSupported();
    }

    public static int[] createSimSlotsTable(int m0, int m1, int m2) {
        int[] slots = new int[HwVSimModemAdapter.MAX_SUB_COUNT];
        slots[0] = m0;
        slots[1] = m1;
        slots[2] = m2;
        return slots;
    }

    public static boolean isRadioAvailable(int subId) {
        boolean z = true;
        if (!HwVSimController.isInstantiated()) {
            return true;
        }
        if (!HwVSimUtils.isPlatformTwoModems()) {
            CommandsInterface ci = HwVSimController.getInstance().getCiBySub(subId);
            if (ci == null || !ci.isRadioAvailable()) {
                z = false;
            }
            return z;
        } else if (HwVSimController.getInstance().isVSimEnabled()) {
            if (HwVSimController.getInstance().getSimSlotTableLastSlotId() == subId) {
                z = false;
            }
            return z;
        } else {
            if (2 == subId) {
                z = false;
            }
            return z;
        }
    }

    public static int getNetworkTypeInModem1ForCmcc(int defaultNetworkType) {
        if (!IS_CMCC_4GSWITCH_DISABLE) {
            return defaultNetworkType;
        }
        if (isDualImsSupported()) {
            return 9;
        }
        return 3;
    }

    public static boolean isDualImsSwitchOpened() {
        return 1 == SystemProperties.getInt("persist.radio.dualltecap", 0);
    }
}
