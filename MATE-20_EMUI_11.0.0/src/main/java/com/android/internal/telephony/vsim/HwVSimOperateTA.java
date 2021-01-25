package com.android.internal.telephony.vsim;

import com.huawei.android.telephony.RlogEx;

public class HwVSimOperateTA {
    private static final int CMD_WIRTE_MODEM_METHOD3 = 43;
    private static final int CMD_WIRTE_MODEM_SHAREDATA = 16;
    private static final int CMD_WIRTE_MODEM_SHAREDATA2 = 39;
    private static final String LOG_TAG = "VSimOperateTA";
    public static final int OPERTA_RESULT_CHECKCARD_ERROR = 3;
    public static final int OPERTA_RESULT_ICC_CHANNEL_ERROR = 4;
    public static final int OPERTA_RESULT_ICC_TA_COMMON = 9;
    public static final int OPERTA_RESULT_ICC_TA_TIMEOUT = 6;
    public static final int OPERTA_RESULT_IS_STUB_ON_BATCH_WAFER = 5;
    public static final int OPERTA_RESULT_PCIE_NO_IPK = 8;
    public static final int OPERTA_RESULT_TA_ERROR = 1;
    public static final int OPERTA_RESULT_TA_SUCCESS = 0;
    public static final int OPERTA_RESULT_TEE_SERVICE_NOT_EXIST = 7;
    public static final int OPERTA_RESULT_WRONG_CHALLENGE = 2;
    public static final int OP_BATCHWAFER_CLOSE_SESSION = 100;
    public static final int OP_BATCHWAFER_READ_MODEM_RESULT = 4;
    public static final int OP_BATCHWAFER_SETAPN = 52;
    public static final int OP_BATCHWAFER_WRITECARD = 51;
    public static final int OP_CLEARAPN = 3;
    public static final int OP_SETAPN = 2;
    public static final int OP_WRITECARD = 1;
    private static HwVSimOperateTA sInstance = new HwVSimOperateTA();
    private HwVSimOperateTANative mHwVSimOperateTANative = new HwVSimOperateTANative();

    private HwVSimOperateTA() {
    }

    public static HwVSimOperateTA getDefault() {
        return sInstance;
    }

    private int writeToTa(String imsi, int cardType, int apnType, String challenge, String taPath, int vsimLoc, int modemId) {
        switch (this.mHwVSimOperateTANative.operTANative(43, 1, cardType, apnType, challenge, imsi, taPath, vsimLoc, modemId)) {
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

    private int writeToTaBatchWafer(int operType, String imsi, int cardType, int apnType, String challenge, String taPath, int vsimLoc, int modemId) {
        switch (this.mHwVSimOperateTANative.operTANative(43, operType, cardType, apnType, challenge, imsi, taPath, vsimLoc, modemId)) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 6;
            case 3:
            default:
                return 1;
            case 4:
                return 9;
            case 5:
                return 10;
            case 6:
                return 11;
            case 7:
                return 17;
            case 8:
                return 18;
            case 9:
                return 19;
        }
    }

    private int writeApnToTa(String imsi, int cardType, int apnType, String challenge, String taPath) {
        switch (this.mHwVSimOperateTANative.operTANative(43, 2, cardType, apnType, challenge, imsi, taPath, 0, 0)) {
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

    private int clearToTa() {
        switch (this.mHwVSimOperateTANative.operTANative(43, 3, 0, 0, "000", (String) null, (String) null, 0, 0)) {
            case 0:
                return 0;
            case 1:
                return 1;
            default:
                return 1;
        }
    }

    private void logd(String s) {
        RlogEx.d(LOG_TAG, s);
    }

    private void logi(String s) {
        RlogEx.i(LOG_TAG, s);
    }

    public int operateTA(int operation, String imsi, int cardType, int apnType, String challenge, boolean isForHash, String taPath, int vsimLoc, int modemId) {
        logi("operateTA: operation = " + operation + ", vsimLoc =" + vsimLoc + ", modemId =" + modemId + ", isForHash = " + isForHash);
        int result = 1;
        if (operation != 100) {
            switch (operation) {
                case 1:
                    result = writeToTa(imsi, cardType, apnType, challenge, taPath, vsimLoc, modemId);
                    break;
                case 2:
                    result = writeApnToTa(imsi, cardType, apnType, challenge, taPath);
                    break;
                case 3:
                    result = clearToTa();
                    break;
                default:
                    switch (operation) {
                        case 51:
                        case 52:
                            break;
                        default:
                            logd("operateTA do nothing");
                            break;
                    }
                case 4:
                    result = writeToTaBatchWafer(operation, imsi, cardType, apnType, challenge, taPath, vsimLoc, modemId);
                    break;
            }
            logi("operateTA: result = " + result);
            return result;
        }
        result = writeToTaBatchWafer(operation, imsi, cardType, apnType, challenge, taPath, vsimLoc, modemId);
        logi("operateTA: result = " + result);
        return result;
    }
}
