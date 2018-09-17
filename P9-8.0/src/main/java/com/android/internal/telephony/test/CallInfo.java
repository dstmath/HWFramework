package com.android.internal.telephony.test;

import com.android.internal.telephony.ATParseEx;
import com.android.internal.telephony.DriverCall;
import com.android.internal.telephony.ProxyController;

/* compiled from: SimulatedGsmCallState */
class CallInfo {
    boolean mIsMT;
    boolean mIsMpty;
    String mNumber;
    State mState;
    int mTOA;

    /* compiled from: SimulatedGsmCallState */
    enum State {
        ACTIVE(0),
        HOLDING(1),
        DIALING(2),
        ALERTING(3),
        INCOMING(4),
        WAITING(5);
        
        private final int mValue;

        private State(int value) {
            this.mValue = value;
        }

        public int value() {
            return this.mValue;
        }
    }

    CallInfo(boolean isMT, State state, boolean isMpty, String number) {
        this.mIsMT = isMT;
        this.mState = state;
        this.mIsMpty = isMpty;
        this.mNumber = number;
        if (number.length() <= 0 || number.charAt(0) != '+') {
            this.mTOA = 129;
        } else {
            this.mTOA = 145;
        }
    }

    static CallInfo createOutgoingCall(String number) {
        return new CallInfo(false, State.DIALING, false, number);
    }

    static CallInfo createIncomingCall(String number) {
        return new CallInfo(true, State.INCOMING, false, number);
    }

    String toCLCCLine(int index) {
        return "+CLCC: " + index + "," + (this.mIsMT ? ProxyController.MODEM_1 : ProxyController.MODEM_0) + "," + this.mState.value() + ",0," + (this.mIsMpty ? ProxyController.MODEM_1 : ProxyController.MODEM_0) + ",\"" + this.mNumber + "\"," + this.mTOA;
    }

    DriverCall toDriverCall(int index) {
        DriverCall ret = new DriverCall();
        ret.index = index;
        ret.isMT = this.mIsMT;
        try {
            ret.state = DriverCall.stateFromCLCC(this.mState.value());
            ret.isMpty = this.mIsMpty;
            ret.number = this.mNumber;
            ret.TOA = this.mTOA;
            ret.isVoice = true;
            ret.als = 0;
            return ret;
        } catch (ATParseEx ex) {
            throw new RuntimeException("should never happen", ex);
        }
    }

    boolean isActiveOrHeld() {
        return this.mState == State.ACTIVE || this.mState == State.HOLDING;
    }

    boolean isConnecting() {
        return this.mState == State.DIALING || this.mState == State.ALERTING;
    }

    boolean isRinging() {
        return this.mState == State.INCOMING || this.mState == State.WAITING;
    }
}
