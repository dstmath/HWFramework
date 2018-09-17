package com.huawei.internal.telephony;

import android.os.Handler;
import android.os.Message;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.IccCardConstantsEx;
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
        PERSO_LOCKED,
        CARD_IO_ERROR,
        DEACTIVED,
        SIM_NETWORK_SUBSET_LOCKED,
        SIM_CORPORATE_LOCKED,
        SIM_SERVICE_PROVIDER_LOCKED,
        SIM_SIM_LOCKED,
        RUIM_NETWORK1_LOCKED,
        RUIM_NETWORK2_LOCKED,
        RUIM_HRPD_LOCKED,
        RUIM_CORPORATE_LOCKED,
        RUIM_SERVICE_PROVIDER_LOCKED,
        RUIM_RUIM_LOCKED,
        SIM_NETWORK_LOCKED_PUK,
        SIM_NETWORK_SUBSET_LOCKED_PUK,
        SIM_CORPORATE_LOCKED_PUK,
        SIM_SERVICE_PROVIDER_LOCKED_PUK,
        UNKNOWN,
        ABSENT,
        PIN_REQUIRED,
        PUK_REQUIRED,
        NETWORK_LOCKED,
        READY,
        NOT_READY,
        PERM_DISABLED;

        public static boolean isPukLocked(com.android.internal.telephony.IccCardConstants.State obj) {
            throw new NoExtAPIException("method not supported.");
        }
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
        if (IccCardConstantsEx.INTENT_VALUE_ICC_ABSENT.equals(state.toString())) {
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
        if (IccCardConstantsEx.INTENT_VALUE_ICC_READY.equals(state.toString())) {
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
