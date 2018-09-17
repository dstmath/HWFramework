package com.android.internal.telephony;

import android.os.SystemProperties;
import android.text.TextUtils;
import android.text.format.DateFormat;

public class HwModemCapability {
    private static final int A_VALUE = 10;
    private static final int BCD_LEN = 4;
    private static String MODEM_CAP = null;
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
    public static final int MODEM_CAP_SUPPORT_SWITCH_SOCKET = 16;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwModemCapability.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwModemCapability.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwModemCapability.<clinit>():void");
    }

    public static boolean isCapabilitySupport(int capability) {
        boolean z = true;
        int bcdIndex = capability / MODEM_CAP_MANUAL_SEL_NETWORK_AUTO;
        int bcdOffset = capability % MODEM_CAP_MANUAL_SEL_NETWORK_AUTO;
        if (capability < 0 || capability >= MODEM_CAP_MAX) {
            return false;
        }
        if (TextUtils.isEmpty(MODEM_CAP)) {
            MODEM_CAP = SystemProperties.get("persist.radio.modem.cap", "");
        }
        try {
            int bcdValue = convertChar2Int(MODEM_CAP.charAt(bcdIndex));
            if (bcdValue != -1) {
                if (((MODEM_CAP_BIP_SUPPORT << (3 - bcdOffset)) & bcdValue) <= 0) {
                    z = false;
                }
                return z;
            }
        } catch (IndexOutOfBoundsException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private static int convertChar2Int(char origChar) {
        if (origChar >= '0' && origChar <= '9') {
            return origChar - 48;
        }
        if (origChar >= DateFormat.AM_PM && origChar <= 'f') {
            return (origChar - 97) + MODEM_CAP_DSDS_MANUAL_PS_ATTACH;
        }
        if (origChar < DateFormat.CAPITAL_AM_PM || origChar > 'F') {
            return -1;
        }
        return (origChar - 65) + MODEM_CAP_DSDS_MANUAL_PS_ATTACH;
    }
}
