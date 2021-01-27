package com.android.internal.telephony;

import com.android.internal.telephony.PhoneConstants;

public class PhoneConstantConversions {
    public static int convertCallState(PhoneConstants.State state) {
        int i = AnonymousClass1.$SwitchMap$com$android$internal$telephony$PhoneConstants$State[state.ordinal()];
        if (i == 1) {
            return 1;
        }
        if (i != 2) {
            return 0;
        }
        return 2;
    }

    public static PhoneConstants.State convertCallState(int state) {
        if (state == 1) {
            return PhoneConstants.State.RINGING;
        }
        if (state != 2) {
            return PhoneConstants.State.IDLE;
        }
        return PhoneConstants.State.OFFHOOK;
    }

    /* renamed from: com.android.internal.telephony.PhoneConstantConversions$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$PhoneConstants$DataState = new int[PhoneConstants.DataState.values().length];
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$PhoneConstants$State = new int[PhoneConstants.State.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$PhoneConstants$DataState[PhoneConstants.DataState.CONNECTING.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$PhoneConstants$DataState[PhoneConstants.DataState.CONNECTED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$PhoneConstants$DataState[PhoneConstants.DataState.SUSPENDED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$PhoneConstants$State[PhoneConstants.State.RINGING.ordinal()] = 1;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$PhoneConstants$State[PhoneConstants.State.OFFHOOK.ordinal()] = 2;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    public static int convertDataState(PhoneConstants.DataState state) {
        int i = AnonymousClass1.$SwitchMap$com$android$internal$telephony$PhoneConstants$DataState[state.ordinal()];
        if (i == 1) {
            return 1;
        }
        if (i == 2) {
            return 2;
        }
        if (i != 3) {
            return 0;
        }
        return 3;
    }

    public static PhoneConstants.DataState convertDataState(int state) {
        if (state == 1) {
            return PhoneConstants.DataState.CONNECTING;
        }
        if (state == 2) {
            return PhoneConstants.DataState.CONNECTED;
        }
        if (state != 3) {
            return PhoneConstants.DataState.DISCONNECTED;
        }
        return PhoneConstants.DataState.SUSPENDED;
    }
}
