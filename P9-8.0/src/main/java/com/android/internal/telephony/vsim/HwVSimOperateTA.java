package com.android.internal.telephony.vsim;

import android.telephony.Rlog;
import android.util.Log;

public class HwVSimOperateTA {
    private static final int CMD_WIRTE_MODEM_METHOD3 = 43;
    private static final int CMD_WIRTE_MODEM_SHAREDATA = 16;
    private static final int CMD_WIRTE_MODEM_SHAREDATA2 = 39;
    private static boolean HWDBG = false;
    private static boolean HWFLOW = false;
    private static final String LOG_TAG = "VSimOperateTA";
    public static final int OPERTA__RESULT_CHECKCARD_ERROR = 3;
    public static final int OPERTA__RESULT_TA_ERROR = 1;
    public static final int OPERTA__RESULT_TA_SUCCESS = 0;
    public static final int OPERTA__RESULT_WRONG_CHALLENGE = 2;
    private static final int OPERTYPE_CLEARAPN = 3;
    private static final int OPERTYPE_SETAPN = 2;
    private static final int OPERTYPE_WRITECARD = 1;
    public static final int OP_CLEARAPN = 3;
    public static final int OP_SETAPN = 2;
    public static final int OP_WRITECARD = 1;
    private static HwVSimOperateTA sInstance = new HwVSimOperateTA();

    public native int operTA(int i, int i2, int i3, int i4, String str, String str2, String str3, int i5, int i6);

    static {
        boolean z;
        boolean z2 = true;
        if (Log.HWLog) {
            z = true;
        } else if (Log.HWModuleLog) {
            z = Log.isLoggable(LOG_TAG, 3);
        } else {
            z = false;
        }
        HWDBG = z;
        if (!Log.HWINFO) {
            if (Log.HWModuleLog) {
                z2 = Log.isLoggable(LOG_TAG, 4);
            } else {
                z2 = false;
            }
        }
        HWFLOW = z2;
        System.loadLibrary("operta");
    }

    private HwVSimOperateTA() {
    }

    public static HwVSimOperateTA getDefault() {
        return sInstance;
    }

    private int writeToTA(String imsi, int cardType, int apnType, String Challenge, boolean isForHash, String taPath, int vsimLoc, int modemID) {
        switch (operTA(43, 1, cardType, apnType, Challenge, imsi, taPath, vsimLoc, modemID)) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 6;
            case 3:
                return 8;
            default:
                return 1;
        }
    }

    private int writeApnToTA(String imsi, int cardType, int apnType, String Challenge, boolean isForHash, String taPath) {
        switch (operTA(43, 2, cardType, apnType, Challenge, imsi, taPath, 0, 0)) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 6;
            default:
                return 1;
        }
    }

    private int clearToTA() {
        switch (operTA(43, 3, 0, 0, "000", null, null, 0, 0)) {
            case 0:
                return 0;
            case 1:
                return 1;
            default:
                return 1;
        }
    }

    private void logd(String s) {
        Rlog.d(LOG_TAG, s);
    }

    private void logi(String s) {
        Rlog.i(LOG_TAG, s);
    }

    public int operateTA(int operation, String imsi, int cardType, int apnType, String Challenge, boolean isForHash, String taPath, int vsimLoc, int modemID) {
        if (HWFLOW) {
            logi("operateTA: operation = " + operation + ", vsimLoc =" + vsimLoc + ", modemId =" + modemID + ", isForHash = " + isForHash);
        }
        int result = 1;
        switch (operation) {
            case 1:
                result = writeToTA(imsi, cardType, apnType, Challenge, isForHash, taPath, vsimLoc, modemID);
                break;
            case 2:
                result = writeApnToTA(imsi, cardType, apnType, Challenge, isForHash, taPath);
                break;
            case 3:
                result = clearToTA();
                break;
            default:
                if (HWDBG) {
                    logd("operateTA do nothing");
                    break;
                }
                break;
        }
        if (HWFLOW) {
            logi("operateTA: result = " + result);
        }
        return result;
    }
}
