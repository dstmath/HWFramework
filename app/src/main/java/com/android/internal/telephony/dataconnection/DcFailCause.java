package com.android.internal.telephony.dataconnection;

import android.content.res.Resources;
import android.os.SystemProperties;
import android.telephony.Rlog;
import java.util.HashMap;

public enum DcFailCause {
    ;
    
    private static final HashMap<Integer, DcFailCause> sErrorCodeToFailCauseMap = null;
    private final int mErrorCode;
    private final boolean mRestartRadioOnRegularDeactivation;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.dataconnection.DcFailCause.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.dataconnection.DcFailCause.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DcFailCause.<clinit>():void");
    }

    private DcFailCause(int errorCode) {
        this.mRestartRadioOnRegularDeactivation = Resources.getSystem().getBoolean(17957017);
        this.mErrorCode = errorCode;
    }

    public int getErrorCode() {
        return this.mErrorCode;
    }

    public boolean isRestartRadioFail() {
        return this == REGULAR_DEACTIVATION ? this.mRestartRadioOnRegularDeactivation : false;
    }

    public boolean isPermanentFail() {
        boolean z = true;
        if (isMatchedDsFail()) {
            return false;
        }
        if (!(this == OPERATOR_BARRED || this == MISSING_UNKNOWN_APN || this == UNKNOWN_PDP_ADDRESS_TYPE || this == USER_AUTHENTICATION || this == ACTIVATION_REJECT_GGSN || this == SERVICE_OPTION_NOT_SUPPORTED || this == SERVICE_OPTION_NOT_SUBSCRIBED || this == NSAPI_IN_USE || this == ONLY_IPV4_ALLOWED || this == ONLY_IPV6_ALLOWED || this == PROTOCOL_ERRORS || this == RADIO_POWER_OFF || this == TETHERED_CALL_ACTIVE || this == RADIO_NOT_AVAILABLE || this == UNACCEPTABLE_NETWORK_PARAMETER || this == SIGNAL_LOST)) {
            z = false;
        }
        return z;
    }

    public boolean isEventLoggable() {
        if (this == OPERATOR_BARRED || this == INSUFFICIENT_RESOURCES || this == UNKNOWN_PDP_ADDRESS_TYPE || this == USER_AUTHENTICATION || this == ACTIVATION_REJECT_GGSN || this == ACTIVATION_REJECT_UNSPECIFIED || this == SERVICE_OPTION_NOT_SUBSCRIBED || this == SERVICE_OPTION_NOT_SUPPORTED || this == SERVICE_OPTION_OUT_OF_ORDER || this == NSAPI_IN_USE || this == ONLY_IPV4_ALLOWED || this == ONLY_IPV6_ALLOWED || this == PROTOCOL_ERRORS || this == SIGNAL_LOST || this == RADIO_POWER_OFF || this == TETHERED_CALL_ACTIVE || this == UNACCEPTABLE_NETWORK_PARAMETER) {
            return true;
        }
        return false;
    }

    public static DcFailCause fromInt(int errorCode) {
        DcFailCause fc = (DcFailCause) sErrorCodeToFailCauseMap.get(Integer.valueOf(errorCode));
        if (fc == null) {
            return UNKNOWN;
        }
        return fc;
    }

    private boolean isMatchedDsFail() {
        boolean isMatched = false;
        try {
            String cntelfailcau = SystemProperties.get("ro.hwpp_ds_fail", "");
            Rlog.d("DcFailCause", "isMatchedDsFail cntelfailcau: " + cntelfailcau);
            for (String fcau : cntelfailcau.split(",")) {
                if (Integer.toString(this.mErrorCode).equals(fcau)) {
                    Rlog.d("DcFailCause", "ErrorCode has been matched: " + this.mErrorCode);
                    isMatched = true;
                }
            }
        } catch (Exception ex) {
            Rlog.e("DcFailCause", "Exception isMatchedDsFail get ds fail cause, ", ex);
        }
        return isMatched;
    }
}
