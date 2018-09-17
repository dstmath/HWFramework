package com.huawei.internal.telephony;

import android.os.Handler;
import android.os.Message;
import com.android.internal.telephony.IccCard;
import com.huawei.android.util.NoExtAPIException;

public class IccCardEx {
    public static final String INTENT_VALUE_ICC_CARD_IO_ERROR = "CARD_IO_ERROR";
    public static final String INTENT_VALUE_ICC_REQUIRE_LOCK = "REQUIRE_LOCKED";
    public static final String INTENT_VALUE_ICC_SIM_REFRESH = "SIM_REFRESH";
    public static final String INTENT_VALUE_ICC_UNKNOWN = "UNKNOWN";
    public static final String INTENT_VALUE_LOCKED_CORPORATE = "SIM CORPORATE";
    public static final String INTENT_VALUE_LOCKED_CORPORATE_PUK = "SIM LOCK CORPORATE BLOCK";
    public static final String INTENT_VALUE_LOCKED_NETWORK_PUK = "SIM LOCK BLOCK";
    public static final String INTENT_VALUE_LOCKED_NETWORK_SUBSET = "SIM NETWORK SUBSET";
    public static final String INTENT_VALUE_LOCKED_NETWORK_SUBSET_PUK = "SIM LOCK NETWORK SUBSET BLOCK";
    public static final String INTENT_VALUE_LOCKED_PERSO = "PERSO";
    public static final String INTENT_VALUE_LOCKED_RUIM_CORPORATE = "RUIM CORPORATE";
    public static final String INTENT_VALUE_LOCKED_RUIM_HRPD = "RUIM HRPD";
    public static final String INTENT_VALUE_LOCKED_RUIM_NETWORK1 = "RUIM NETWORK1";
    public static final String INTENT_VALUE_LOCKED_RUIM_NETWORK2 = "RUIM NETWORK2";
    public static final String INTENT_VALUE_LOCKED_RUIM_RUIM = "RUIM RUIM";
    public static final String INTENT_VALUE_LOCKED_RUIM_SERVICE_PROVIDER = "RUIM SERVICE PROVIDER";
    public static final String INTENT_VALUE_LOCKED_SERVICE_PROVIDER = "SIM SERVICE PROVIDER";
    public static final String INTENT_VALUE_LOCKED_SERVICE_PROVIDER_PUK = "SIM LOCK SERVICE PROVIDERBLOCK";
    public static final String INTENT_VALUE_LOCKED_SIM = "SIM SIM";

    public enum State {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.internal.telephony.IccCardEx.State.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.internal.telephony.IccCardEx.State.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.huawei.internal.telephony.IccCardEx.State.<clinit>():void");
        }

        public static boolean isPukLocked(com.android.internal.telephony.IccCardConstants.State obj) {
            throw new NoExtAPIException("method not supported.");
        }
    }

    public IccCardEx() {
    }

    public static final boolean getIccFdnAvailable(IccCard obj) {
        return obj.getIccFdnAvailable();
    }

    public static final int getIccPin1RetryCount(IccCard obj) {
        throw new NoExtAPIException("method not supported.");
    }

    public static final int getIccPin2RetryCount(IccCard obj) {
        throw new NoExtAPIException("method not supported.");
    }

    public static final boolean getIccPin2Blocked(IccCard obj) {
        throw new NoExtAPIException("method not supported.");
    }

    public static final boolean getIccPuk2Blocked(IccCard obj) {
        throw new NoExtAPIException("method not supported.");
    }

    public static void registerForPersoLocked(IccCard obj, Handler h, int what, Object object) {
        throw new NoExtAPIException("method not supported.");
    }

    public static void registerForNetworkLocked(IccCard obj, Handler h, int what, Object object) {
        obj.registerForNetworkLocked(h, what, object);
    }

    public static void supplyDepersonalization(IccCard obj, String pin, int type, Message onComplete) {
        obj.supplyDepersonalization(pin, type, onComplete);
    }

    public static void supplyNetworkDepersonalization(IccCard obj, String pin, int type, Message onComplete) {
        throw new NoExtAPIException("method not supported.");
    }

    public static void supplyNetworkDepersonalization(IccCard obj, String pin, Message onComplete) {
        throw new NoExtAPIException("method not supported.");
    }

    public static void broadcastIccStateChangedIntent(IccCard obj, String value, String reason) {
        throw new NoExtAPIException("method not supported.");
    }

    public static State getIccCardExState(com.android.internal.telephony.IccCardConstants.State state) {
        if ("PERSO_LOCKED".equals(state.toString())) {
            return State.PERSO_LOCKED;
        }
        if (INTENT_VALUE_ICC_CARD_IO_ERROR.equals(state.toString())) {
            return State.CARD_IO_ERROR;
        }
        if ("DEACTIVED".equals(state.toString())) {
            return State.DEACTIVED;
        }
        if ("SIM_NETWORK_SUBSET_LOCKED".equals(state.toString())) {
            return State.SIM_NETWORK_SUBSET_LOCKED;
        }
        if ("SIM_CORPORATE_LOCKED".equals(state.toString())) {
            return State.SIM_CORPORATE_LOCKED;
        }
        if ("SIM_SERVICE_PROVIDER_LOCKED".equals(state.toString())) {
            return State.SIM_SERVICE_PROVIDER_LOCKED;
        }
        if ("SIM_SIM_LOCKED".equals(state.toString())) {
            return State.SIM_SIM_LOCKED;
        }
        if ("RUIM_NETWORK1_LOCKED".equals(state.toString())) {
            return State.RUIM_NETWORK1_LOCKED;
        }
        if ("RUIM_NETWORK2_LOCKED".equals(state.toString())) {
            return State.RUIM_NETWORK1_LOCKED;
        }
        if ("RUIM_HRPD_LOCKED".equals(state.toString())) {
            return State.RUIM_HRPD_LOCKED;
        }
        if ("RUIM_CORPORATE_LOCKED".equals(state.toString())) {
            return State.RUIM_HRPD_LOCKED;
        }
        if ("RUIM_SERVICE_PROVIDER_LOCKED".equals(state.toString())) {
            return State.RUIM_CORPORATE_LOCKED;
        }
        if ("RUIM_RUIM_LOCKED".equals(state.toString())) {
            return State.RUIM_RUIM_LOCKED;
        }
        if ("SIM_NETWORK_LOCKED_PUK".equals(state.toString())) {
            return State.RUIM_RUIM_LOCKED;
        }
        if ("SIM_NETWORK_SUBSET_LOCKED_PUK".equals(state.toString())) {
            return State.SIM_NETWORK_SUBSET_LOCKED_PUK;
        }
        if ("SIM_CORPORATE_LOCKED_PUK".equals(state.toString())) {
            return State.SIM_NETWORK_SUBSET_LOCKED_PUK;
        }
        if ("SIM_SERVICE_PROVIDER_LOCKED_PUK".equals(state.toString())) {
            return State.SIM_SERVICE_PROVIDER_LOCKED_PUK;
        }
        if ("NETWORK_LOCKED".equals(state.toString())) {
            return State.NETWORK_LOCKED;
        }
        if (INTENT_VALUE_ICC_UNKNOWN.equals(state.toString())) {
            return State.UNKNOWN;
        }
        if ("ABSENT".equals(state.toString())) {
            return State.ABSENT;
        }
        if ("PIN_REQUIRED".equals(state.toString())) {
            return State.PIN_REQUIRED;
        }
        if ("PUK_REQUIRED".equals(state.toString())) {
            return State.PUK_REQUIRED;
        }
        if ("NETWORK_LOCKED".equals(state.toString())) {
            return State.NETWORK_LOCKED;
        }
        if ("READY".equals(state.toString())) {
            return State.READY;
        }
        if ("NOT_READY".equals(state.toString())) {
            return State.NOT_READY;
        }
        if ("PERM_DISABLED".equals(state.toString())) {
            return State.PERM_DISABLED;
        }
        return State.UNKNOWN;
    }
}
