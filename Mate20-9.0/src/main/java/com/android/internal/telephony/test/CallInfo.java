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

    /* access modifiers changed from: package-private */
    public String toCLCCLine(int index) {
        StringBuilder sb = new StringBuilder();
        sb.append("+CLCC: ");
        sb.append(index);
        sb.append(",");
        sb.append(this.mIsMT ? ProxyController.MODEM_1 : ProxyController.MODEM_0);
        sb.append(",");
        sb.append(this.mState.value());
        sb.append(",0,");
        sb.append(this.mIsMpty ? ProxyController.MODEM_1 : ProxyController.MODEM_0);
        sb.append(",\"");
        sb.append(this.mNumber);
        sb.append("\",");
        sb.append(this.mTOA);
        return sb.toString();
    }

    /* access modifiers changed from: package-private */
    public DriverCall toDriverCall(int index) {
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

    /* access modifiers changed from: package-private */
    public boolean isActiveOrHeld() {
        return this.mState == State.ACTIVE || this.mState == State.HOLDING;
    }

    /* access modifiers changed from: package-private */
    public boolean isConnecting() {
        return this.mState == State.DIALING || this.mState == State.ALERTING;
    }

    /* access modifiers changed from: package-private */
    public boolean isRinging() {
        return this.mState == State.INCOMING || this.mState == State.WAITING;
    }
}
