package com.huawei.internal.telephony;

import com.android.internal.telephony.IccCardConstants;

public class IccCardConstantsEx {
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

    public enum StateEx {
        UNKNOWN(IccCardConstants.State.UNKNOWN),
        ABSENT(IccCardConstants.State.ABSENT),
        PIN_REQUIRED(IccCardConstants.State.PIN_REQUIRED),
        PUK_REQUIRED(IccCardConstants.State.PUK_REQUIRED),
        NETWORK_LOCKED(IccCardConstants.State.NETWORK_LOCKED),
        READY(IccCardConstants.State.READY),
        NOT_READY(IccCardConstants.State.NOT_READY),
        PERM_DISABLED(IccCardConstants.State.PERM_DISABLED),
        CARD_IO_ERROR(IccCardConstants.State.CARD_IO_ERROR),
        CARD_RESTRICTED(IccCardConstants.State.CARD_RESTRICTED),
        LOADED(IccCardConstants.State.LOADED),
        DEACTIVED(IccCardConstants.State.DEACTIVED);
        
        private final IccCardConstants.State value;

        private StateEx(IccCardConstants.State value2) {
            this.value = value2;
        }

        public boolean isPinLocked() {
            return this == PIN_REQUIRED || this == PUK_REQUIRED;
        }

        public boolean iccCardExist() {
            return this == PIN_REQUIRED || this == PUK_REQUIRED || this == NETWORK_LOCKED || this == READY || this == NOT_READY || this == PERM_DISABLED || this == CARD_IO_ERROR || this == CARD_RESTRICTED || this == LOADED;
        }

        public IccCardConstants.State getValue() {
            return this.value;
        }

        public static StateEx getStateExByState(IccCardConstants.State state) {
            switch (AnonymousClass1.$SwitchMap$com$android$internal$telephony$IccCardConstants$State[state.ordinal()]) {
                case 1:
                    return UNKNOWN;
                case 2:
                    return ABSENT;
                case 3:
                    return PIN_REQUIRED;
                case 4:
                    return PUK_REQUIRED;
                case 5:
                    return NETWORK_LOCKED;
                case 6:
                    return READY;
                case 7:
                    return NOT_READY;
                case 8:
                    return PERM_DISABLED;
                case 9:
                    return CARD_IO_ERROR;
                case 10:
                    return CARD_RESTRICTED;
                case 11:
                    return LOADED;
                case 12:
                    return DEACTIVED;
                default:
                    return null;
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.huawei.internal.telephony.IccCardConstantsEx$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$IccCardConstants$State = new int[IccCardConstants.State.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.UNKNOWN.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.ABSENT.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.PIN_REQUIRED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.PUK_REQUIRED.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.NETWORK_LOCKED.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.READY.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.NOT_READY.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.PERM_DISABLED.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.CARD_IO_ERROR.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.CARD_RESTRICTED.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.LOADED.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.DEACTIVED.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
        }
    }
}
