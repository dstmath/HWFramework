package com.android.internal.telephony.vsim;

import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.HwTelephonyManagerInner;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwAllInOneController;
import com.android.internal.telephony.Phone;

public class HwVSimUtilsInner {
    public static final int ACTIVE_MODEM_MODE_DUAL = 1;
    public static final int ACTIVE_MODEM_MODE_SINGLE = 0;
    public static final int DISABLE = 0;
    public static final int DOMAIN_CS_ONLY = 0;
    public static final int DOMAIN_CS_PS = 2;
    public static final int DOMAIN_PS_ONLY = 1;
    public static final int ENABLE = 1;
    public static final boolean IS_CMCC_4GSWITCH_DISABLE = HwAllInOneController.IS_CMCC_4GSWITCH_DISABLE;
    public static final boolean IS_DSDSPOWER_SUPPORT = SystemProperties.getBoolean("ro.config.hw_dsdspowerup", false);
    private static final String LOG_TAG = "VSimUtilsInner";
    public static final int NETWORK_MODE_CDMA_GSM = 59;
    private static final String PROPERTY_VSIM_DSDS_VERSION = "ro.radio.vsim_dsds_version";
    private static final String PROPERTY_VSIM_SUPPORT_GSM = "persist.radio.vsim_support_gsm";
    public static final int SIM = 1;
    public static final int STATE_EA = 1;
    public static final int STATE_EB = 2;
    public static final int STATE_OFF = 0;
    public static final int STATE_ON = 1;
    public static final int UE_OPERATION_MODE_DATA_CENTRIC = 1;
    public static final int UE_OPERATION_MODE_VOICE_CENTRIC = 0;
    public static final int VSIM = 11;
    private static final int VSIM_DSDS_VERSION_DEFAULT = 0;
    private static final int VSIM_DSDS_VERSION_ONE = 1;
    private static final int VSIM_DSDS_VERSION_PROP = SystemProperties.getInt(PROPERTY_VSIM_DSDS_VERSION, 0);
    private static final int VSIM_MODEM_COUNT = SystemProperties.getInt("ro.radio.vsim_modem_count", 3);
    private static final int VSIM_MODEM_COUNT_DEFAULT = 3;
    private static final int VSIM_MODEM_COUNT_DUAL = 2;
    private static final int VSIM_MODEM_COUNT_ERROR = -1;
    private static final int VSIM_MODEM_COUNT_FAKE_TRIPPLE = 3;
    private static final int VSIM_MODEM_COUNT_NOT_SUPPORT = 0;
    private static final int VSIM_MODEM_COUNT_REAL_TRIPPLE = 4;
    private static final int VSIM_MODEM_COUNT_SINGLE = 1;
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
                if (cardTypes[i] == 0) {
                    isCardPresent[i] = false;
                } else {
                    isCardPresent[i] = true;
                }
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

    public static Phone getPhoneBySub(int subId, Phone vsimPhone, Phone[] phones) {
        if (subId < 0 || subId > 2) {
            return null;
        }
        if (subId == 2) {
            return vsimPhone;
        }
        return phones[subId];
    }

    public static Integer getCiIndex(Message msg) {
        Integer index = Integer.valueOf(0);
        if (msg == null) {
            return index;
        }
        if (msg.obj != null && (msg.obj instanceof Integer)) {
            return msg.obj;
        }
        if (msg.obj == null || !(msg.obj instanceof AsyncResult)) {
            return index;
        }
        AsyncResult ar = msg.obj;
        if (ar.userObj == null || !(ar.userObj instanceof Integer)) {
            return index;
        }
        return ar.userObj;
    }

    public static boolean isRequestNotSupport(Throwable ex) {
        if (!(ex instanceof CommandException)) {
            return false;
        }
        Error err = Error.GENERIC_FAILURE;
        try {
            err = ((CommandException) ex).getCommandError();
        } catch (ClassCastException e) {
        }
        if (err == Error.REQUEST_NOT_SUPPORTED) {
            return true;
        }
        return false;
    }

    public static boolean isChinaTelecom() {
        return HuaweiTelephonyConfigs.isChinaTelecom();
    }

    public static boolean isFullNetworkSupported() {
        return SystemProperties.getBoolean("ro.config.full_network_support", false);
    }

    public static boolean isVsimSupportGSM() {
        return Boolean.valueOf(SystemProperties.getBoolean(PROPERTY_VSIM_SUPPORT_GSM, true)).booleanValue();
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

    public static boolean isDualImsSupported() {
        return HwTelephonyManagerInner.getDefault().isDualImsSupported();
    }

    public static boolean isPlatformNeedWaitNvMatchUnsol() {
        return HwTelephonyManagerInner.getDefault().isDualImsSupported();
    }
}
