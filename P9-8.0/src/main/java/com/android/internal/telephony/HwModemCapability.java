package com.android.internal.telephony;

import android.os.SystemProperties;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.LogException;

public class HwModemCapability {
    private static final int A_VALUE = 10;
    private static final int BCD_LEN = 4;
    private static String MODEM_CAP = SystemProperties.get("persist.radio.modem.cap", LogException.NO_VALUE);
    public static final int MODEM_CAP_BIP_SUPPORT = 1;
    public static final int MODEM_CAP_CDMA_USE_VIA_HISI = 14;
    public static final int MODEM_CAP_DSDA_SPEECH_CODEC_ADJUST = 11;
    public static final int MODEM_CAP_DSDS_MANUAL_PS_ATTACH = 10;
    public static final int MODEM_CAP_DUAL_PS_ATTACHED = 0;
    public static final int MODEM_CAP_FULL_PREFMODE = 3;
    public static final int MODEM_CAP_GET_ICCID_AT = 19;
    public static final int MODEM_CAP_GET_IMSI_GSM = 18;
    public static final int MODEM_CAP_GET_MODEM_CAPABILITY = 9;
    public static final int MODEM_CAP_LONG_SMS_DELAY_RELEASE = 17;
    public static final int MODEM_CAP_MANUAL_SEL_NETWORK_AUTO = 4;
    public static final int MODEM_CAP_MAX = 360;
    public static final int MODEM_CAP_NOUPDATE_LAC_AND_CID = 12;
    public static final int MODEM_CAP_NV_FUCTION_RPC = 13;
    public static final int MODEM_CAP_ONS_MATCH_PNN = 5;
    public static final int MODEM_CAP_PLUS_TRANSFER_SUPPORT = 2;
    public static final int MODEM_CAP_RETTACH_API_SUPPORT = 7;
    public static final int MODEM_CAP_RIL_RECOVERY_ENDCALL = 8;
    public static final int MODEM_CAP_RPT_DEREGISTER_STATE_DELAYED = 6;
    public static final int MODEM_CAP_SUPPORT_DIFF_ID = 15;
    public static final int MODEM_CAP_SUPPORT_DUAL_VOLTE = 21;
    public static final int MODEM_CAP_SUPPORT_IMEI_BIND_SLOT = 26;
    public static final int MODEM_CAP_SUPPORT_SWITCH_SOCKET = 16;
    private static final String TAG = "HwModemCapability";

    public static boolean isCapabilitySupport(int capability) {
        boolean z = true;
        int bcdIndex = capability / 4;
        int bcdOffset = capability % 4;
        if (capability < 0 || capability >= 360) {
            return false;
        }
        if (TextUtils.isEmpty(MODEM_CAP)) {
            MODEM_CAP = SystemProperties.get("persist.radio.modem.cap", LogException.NO_VALUE);
        }
        try {
            int bcdValue = convertChar2Int(MODEM_CAP.charAt(bcdIndex));
            if (bcdValue != -1) {
                if (((1 << (3 - bcdOffset)) & bcdValue) <= 0) {
                    z = false;
                }
                return z;
            }
        } catch (IndexOutOfBoundsException ex) {
            Log.e(TAG, "isCapabilitySupport " + ex);
        }
        return false;
    }

    private static int convertChar2Int(char origChar) {
        if (origChar >= '0' && origChar <= '9') {
            return origChar - 48;
        }
        if (origChar >= DateFormat.AM_PM && origChar <= 'f') {
            return (origChar - 97) + 10;
        }
        if (origChar < DateFormat.CAPITAL_AM_PM || origChar > 'F') {
            return -1;
        }
        return (origChar - 65) + 10;
    }
}
