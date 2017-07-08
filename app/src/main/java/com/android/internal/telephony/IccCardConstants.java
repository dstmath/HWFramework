package com.android.internal.telephony;

import com.huawei.pgmng.plug.PGSdk;
import huawei.cust.HwCfgFilePolicy;

public class IccCardConstants {
    public static final String INTENT_KEY_ICC_STATE = "ss";
    public static final String INTENT_KEY_LOCKED_REASON = "reason";
    public static final String INTENT_VALUE_ABSENT_ON_PERM_DISABLED = "PERM_DISABLED";
    public static final String INTENT_VALUE_ICC_ABSENT = "ABSENT";
    public static final String INTENT_VALUE_ICC_CARD_IO_ERROR = "CARD_IO_ERROR";
    public static final String INTENT_VALUE_ICC_IMSI = "IMSI";
    public static final String INTENT_VALUE_ICC_INTERNAL_LOCKED = "INTERNAL_LOCKED";
    public static final String INTENT_VALUE_ICC_LOADED = "LOADED";
    public static final String INTENT_VALUE_ICC_LOCKED = "LOCKED";
    public static final String INTENT_VALUE_ICC_NOT_READY = "NOT_READY";
    public static final String INTENT_VALUE_ICC_READY = "READY";
    public static final String INTENT_VALUE_ICC_UNKNOWN = "UNKNOWN";
    public static final String INTENT_VALUE_LOCKED_NETWORK = "NETWORK";
    public static final String INTENT_VALUE_LOCKED_ON_PIN = "PIN";
    public static final String INTENT_VALUE_LOCKED_ON_PUK = "PUK";

    public enum State {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.IccCardConstants.State.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.IccCardConstants.State.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IccCardConstants.State.<clinit>():void");
        }

        public boolean isPinLocked() {
            return this == PIN_REQUIRED || this == PUK_REQUIRED;
        }

        public boolean iccCardExist() {
            if (this == PIN_REQUIRED || this == PUK_REQUIRED || this == NETWORK_LOCKED || this == READY || this == PERM_DISABLED || this == CARD_IO_ERROR) {
                return true;
            }
            return false;
        }

        public static State intToState(int state) throws IllegalArgumentException {
            switch (state) {
                case HwCfgFilePolicy.GLOBAL /*0*/:
                    return UNKNOWN;
                case HwCfgFilePolicy.EMUI /*1*/:
                    return ABSENT;
                case HwCfgFilePolicy.PC /*2*/:
                    return PIN_REQUIRED;
                case HwCfgFilePolicy.BASE /*3*/:
                    return PUK_REQUIRED;
                case HwCfgFilePolicy.CUST /*4*/:
                    return NETWORK_LOCKED;
                case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                    return READY;
                case HwCfgFilePolicy.CLOUD_DPLMN /*6*/:
                    return NOT_READY;
                case HwCfgFilePolicy.CLOUD_APN /*7*/:
                    return PERM_DISABLED;
                case PGSdk.TYPE_VIDEO /*8*/:
                    return CARD_IO_ERROR;
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    public IccCardConstants() {
    }
}
