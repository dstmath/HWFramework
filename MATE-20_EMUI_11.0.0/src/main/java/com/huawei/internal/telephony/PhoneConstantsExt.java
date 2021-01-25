package com.huawei.internal.telephony;

import com.android.internal.telephony.PhoneConstants;

public class PhoneConstantsExt {

    public enum DataStateEx {
        CONNECTED(PhoneConstants.DataState.CONNECTED),
        CONNECTING(PhoneConstants.DataState.CONNECTING),
        DISCONNECTED(PhoneConstants.DataState.DISCONNECTED),
        SUSPENDED(PhoneConstants.DataState.SUSPENDED);
        
        private final PhoneConstants.DataState value;

        private DataStateEx(PhoneConstants.DataState value2) {
            this.value = value2;
        }

        public static DataStateEx getCardStateExByCardState(PhoneConstants.DataState dataState) {
            if (dataState == null) {
                return null;
            }
            int i = AnonymousClass1.$SwitchMap$com$android$internal$telephony$PhoneConstants$DataState[dataState.ordinal()];
            if (i == 1) {
                return CONNECTED;
            }
            if (i == 2) {
                return CONNECTING;
            }
            if (i == 3) {
                return DISCONNECTED;
            }
            if (i != 4) {
                return null;
            }
            return SUSPENDED;
        }

        public PhoneConstants.DataState getValue() {
            return this.value;
        }
    }

    public enum StateEx {
        IDLE(PhoneConstants.State.IDLE),
        RINGING(PhoneConstants.State.RINGING),
        OFFHOOK(PhoneConstants.State.OFFHOOK);
        
        private final PhoneConstants.State value;

        private StateEx(PhoneConstants.State value2) {
            this.value = value2;
        }

        public static StateEx getStateExByState(PhoneConstants.State state) {
            if (state == null) {
                return null;
            }
            int i = AnonymousClass1.$SwitchMap$com$android$internal$telephony$PhoneConstants$State[state.ordinal()];
            if (i == 1) {
                return IDLE;
            }
            if (i == 2) {
                return RINGING;
            }
            if (i != 3) {
                return null;
            }
            return OFFHOOK;
        }

        public PhoneConstants.State getValue() {
            return this.value;
        }
    }

    /* renamed from: com.huawei.internal.telephony.PhoneConstantsExt$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$PhoneConstants$DataState = new int[PhoneConstants.DataState.values().length];
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$PhoneConstants$State = new int[PhoneConstants.State.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$PhoneConstants$State[PhoneConstants.State.IDLE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$PhoneConstants$State[PhoneConstants.State.RINGING.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$PhoneConstants$State[PhoneConstants.State.OFFHOOK.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$PhoneConstants$DataState[PhoneConstants.DataState.CONNECTED.ordinal()] = 1;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$PhoneConstants$DataState[PhoneConstants.DataState.CONNECTING.ordinal()] = 2;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$PhoneConstants$DataState[PhoneConstants.DataState.DISCONNECTED.ordinal()] = 3;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$PhoneConstants$DataState[PhoneConstants.DataState.SUSPENDED.ordinal()] = 4;
            } catch (NoSuchFieldError e7) {
            }
        }
    }
}
