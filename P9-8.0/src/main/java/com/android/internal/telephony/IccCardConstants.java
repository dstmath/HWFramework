package com.android.internal.telephony;

public class IccCardConstants {
    public static final String INTENT_KEY_ICC_STATE = "ss";
    public static final String INTENT_KEY_LOCKED_REASON = "reason";
    public static final String INTENT_VALUE_ABSENT_ON_PERM_DISABLED = "PERM_DISABLED";
    public static final String INTENT_VALUE_ICC_ABSENT = "ABSENT";
    public static final String INTENT_VALUE_ICC_CARD_IO_ERROR = "CARD_IO_ERROR";
    public static final String INTENT_VALUE_ICC_CARD_RESTRICTED = "CARD_RESTRICTED";
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
        UNKNOWN,
        ABSENT,
        PIN_REQUIRED,
        PUK_REQUIRED,
        NETWORK_LOCKED,
        READY,
        NOT_READY,
        PERM_DISABLED,
        CARD_IO_ERROR,
        CARD_RESTRICTED,
        DEACTIVED;

        public boolean isPinLocked() {
            return this == PIN_REQUIRED || this == PUK_REQUIRED;
        }

        public boolean iccCardExist() {
            if (this == PIN_REQUIRED || this == PUK_REQUIRED || this == NETWORK_LOCKED || this == READY || this == PERM_DISABLED || this == CARD_IO_ERROR || this == CARD_RESTRICTED) {
                return true;
            }
            return false;
        }

        public static State intToState(int state) throws IllegalArgumentException {
            switch (state) {
                case 0:
                    return UNKNOWN;
                case 1:
                    return ABSENT;
                case 2:
                    return PIN_REQUIRED;
                case 3:
                    return PUK_REQUIRED;
                case 4:
                    return NETWORK_LOCKED;
                case 5:
                    return READY;
                case 6:
                    return NOT_READY;
                case 7:
                    return PERM_DISABLED;
                case 8:
                    return CARD_IO_ERROR;
                case 9:
                    return CARD_RESTRICTED;
                default:
                    throw new IllegalArgumentException();
            }
        }
    }
}
