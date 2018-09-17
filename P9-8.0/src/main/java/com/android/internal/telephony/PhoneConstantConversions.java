package com.android.internal.telephony;

import com.android.internal.telephony.PhoneConstants.DataState;
import com.android.internal.telephony.PhoneConstants.State;

public class PhoneConstantConversions {
    private static final /* synthetic */ int[] -com-android-internal-telephony-PhoneConstants$DataStateSwitchesValues = null;
    private static final /* synthetic */ int[] -com-android-internal-telephony-PhoneConstants$StateSwitchesValues = null;

    private static /* synthetic */ int[] -getcom-android-internal-telephony-PhoneConstants$DataStateSwitchesValues() {
        if (-com-android-internal-telephony-PhoneConstants$DataStateSwitchesValues != null) {
            return -com-android-internal-telephony-PhoneConstants$DataStateSwitchesValues;
        }
        int[] iArr = new int[DataState.values().length];
        try {
            iArr[DataState.CONNECTED.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[DataState.CONNECTING.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[DataState.DISCONNECTED.ordinal()] = 6;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[DataState.SUSPENDED.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
        -com-android-internal-telephony-PhoneConstants$DataStateSwitchesValues = iArr;
        return iArr;
    }

    private static /* synthetic */ int[] -getcom-android-internal-telephony-PhoneConstants$StateSwitchesValues() {
        if (-com-android-internal-telephony-PhoneConstants$StateSwitchesValues != null) {
            return -com-android-internal-telephony-PhoneConstants$StateSwitchesValues;
        }
        int[] iArr = new int[State.values().length];
        try {
            iArr[State.IDLE.ordinal()] = 6;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[State.OFFHOOK.ordinal()] = 1;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[State.RINGING.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        -com-android-internal-telephony-PhoneConstants$StateSwitchesValues = iArr;
        return iArr;
    }

    public static int convertCallState(State state) {
        switch (-getcom-android-internal-telephony-PhoneConstants$StateSwitchesValues()[state.ordinal()]) {
            case 1:
                return 2;
            case 2:
                return 1;
            default:
                return 0;
        }
    }

    public static State convertCallState(int state) {
        switch (state) {
            case 1:
                return State.RINGING;
            case 2:
                return State.OFFHOOK;
            default:
                return State.IDLE;
        }
    }

    public static int convertDataState(DataState state) {
        switch (-getcom-android-internal-telephony-PhoneConstants$DataStateSwitchesValues()[state.ordinal()]) {
            case 1:
                return 2;
            case 2:
                return 1;
            case 3:
                return 3;
            default:
                return 0;
        }
    }

    public static DataState convertDataState(int state) {
        switch (state) {
            case 1:
                return DataState.CONNECTING;
            case 2:
                return DataState.CONNECTED;
            case 3:
                return DataState.SUSPENDED;
            default:
                return DataState.DISCONNECTED;
        }
    }
}
