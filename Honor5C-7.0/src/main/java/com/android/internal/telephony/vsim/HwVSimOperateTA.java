package com.android.internal.telephony.vsim;

import android.telephony.Rlog;

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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.vsim.HwVSimOperateTA.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.vsim.HwVSimOperateTA.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.vsim.HwVSimOperateTA.<clinit>():void");
    }

    public native int operTA(int i, int i2, int i3, int i4, String str, String str2, String str3, int i5, int i6);

    private int writeToTA(String imsi, int cardType, int apnType, String Challenge, boolean isForHash, String taPath, int vsimLoc, int modemID) {
        switch (operTA(CMD_WIRTE_MODEM_METHOD3, OP_WRITECARD, cardType, apnType, Challenge, imsi, taPath, vsimLoc, modemID)) {
            case OPERTA__RESULT_TA_SUCCESS /*0*/:
                return OPERTA__RESULT_TA_SUCCESS;
            case OP_WRITECARD /*1*/:
                return OP_WRITECARD;
            case OP_SETAPN /*2*/:
                return 6;
            case OP_CLEARAPN /*3*/:
                return 8;
            default:
                return OP_WRITECARD;
        }
    }

    private int writeApnToTA(String imsi, int cardType, int apnType, String Challenge, boolean isForHash, String taPath) {
        switch (operTA(CMD_WIRTE_MODEM_METHOD3, OP_SETAPN, cardType, apnType, Challenge, imsi, taPath, OPERTA__RESULT_TA_SUCCESS, OPERTA__RESULT_TA_SUCCESS)) {
            case OPERTA__RESULT_TA_SUCCESS /*0*/:
                return OPERTA__RESULT_TA_SUCCESS;
            case OP_WRITECARD /*1*/:
                return OP_WRITECARD;
            case OP_SETAPN /*2*/:
                return 6;
            default:
                return OP_WRITECARD;
        }
    }

    private int clearToTA() {
        switch (operTA(CMD_WIRTE_MODEM_METHOD3, OP_CLEARAPN, OPERTA__RESULT_TA_SUCCESS, OPERTA__RESULT_TA_SUCCESS, "000", null, null, OPERTA__RESULT_TA_SUCCESS, OPERTA__RESULT_TA_SUCCESS)) {
            case OPERTA__RESULT_TA_SUCCESS /*0*/:
                return OPERTA__RESULT_TA_SUCCESS;
            case OP_WRITECARD /*1*/:
                return OP_WRITECARD;
            default:
                return OP_WRITECARD;
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
        int result = OP_WRITECARD;
        switch (operation) {
            case OP_WRITECARD /*1*/:
                result = writeToTA(imsi, cardType, apnType, Challenge, isForHash, taPath, vsimLoc, modemID);
                break;
            case OP_SETAPN /*2*/:
                result = writeApnToTA(imsi, cardType, apnType, Challenge, isForHash, taPath);
                break;
            case OP_CLEARAPN /*3*/:
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
