package com.android.internal.telephony.vsim;

import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemProperties;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.Phone;

public class HwVSimUtilsInner {
    public static final int ACTIVE_MODEM_MODE_DUAL = 1;
    public static final int ACTIVE_MODEM_MODE_SINGLE = 0;
    public static final int DISABLE = 0;
    public static final int DOMAIN_CS_ONLY = 0;
    public static final int DOMAIN_CS_PS = 2;
    public static final int DOMAIN_PS_ONLY = 1;
    public static final int ENABLE = 1;
    public static final boolean IS_DSDSPOWER_SUPPORT = false;
    private static final String LOG_TAG = "VSimUtilsInner";
    private static final String PROPERTY_VSIM_SUPPORT_GSM = "persist.radio.vsim_support_gsm";
    public static final int SIM = 1;
    public static final int STATE_EA = 1;
    public static final int STATE_EB = 2;
    public static final int STATE_OFF = 0;
    public static final int STATE_ON = 1;
    public static final int UE_OPERATION_MODE_DATA_CENTRIC = 1;
    public static final int UE_OPERATION_MODE_VOICE_CENTRIC = 0;
    public static final int VSIM = 11;
    private static final int VSIM_MODEM_COUNT = 0;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.vsim.HwVSimUtilsInner.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.vsim.HwVSimUtilsInner.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.vsim.HwVSimUtilsInner.<clinit>():void");
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
        if (subId < 0 || subId > VSIM_MODEM_COUNT_DUAL) {
            return null;
        }
        if (subId == VSIM_MODEM_COUNT_DUAL) {
            return vsimPhone;
        }
        return phones[subId];
    }

    public static Integer getCiIndex(Message msg) {
        Integer index = Integer.valueOf(VSIM_MODEM_COUNT_NOT_SUPPORT);
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
            return IS_DSDSPOWER_SUPPORT;
        }
        Error err = Error.GENERIC_FAILURE;
        try {
            err = ((CommandException) ex).getCommandError();
        } catch (ClassCastException e) {
        }
        if (err == Error.REQUEST_NOT_SUPPORTED) {
            return true;
        }
        return IS_DSDSPOWER_SUPPORT;
    }

    public static boolean isChinaTelecom() {
        return HuaweiTelephonyConfigs.isChinaTelecom();
    }

    public static boolean isFullNetworkSupported() {
        return SystemProperties.getBoolean("ro.config.full_network_support", IS_DSDSPOWER_SUPPORT);
    }

    public static boolean isVsimSupportGSM() {
        return Boolean.valueOf(SystemProperties.getBoolean(PROPERTY_VSIM_SUPPORT_GSM, true)).booleanValue();
    }

    public static boolean isPlatformTwoModems() {
        return VSIM_MODEM_COUNT == VSIM_MODEM_COUNT_DUAL ? true : IS_DSDSPOWER_SUPPORT;
    }

    public static boolean isPlatformRealTripple() {
        return VSIM_MODEM_COUNT == VSIM_MODEM_COUNT_REAL_TRIPPLE ? true : IS_DSDSPOWER_SUPPORT;
    }
}
