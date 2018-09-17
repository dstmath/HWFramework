package com.android.internal.telephony.test;

import android.hardware.radio.V1_0.RadioError;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import com.android.internal.telephony.DriverCall;
import java.util.ArrayList;
import java.util.List;

class SimulatedGsmCallState extends Handler {
    static final int CONNECTING_PAUSE_MSEC = 500;
    static final int EVENT_PROGRESS_CALL_STATE = 1;
    static final int MAX_CALLS = 7;
    private boolean mAutoProgressConnecting = true;
    CallInfo[] mCalls = new CallInfo[7];
    private boolean mNextDialFailImmediately;

    public SimulatedGsmCallState(Looper looper) {
        super(looper);
    }

    public void handleMessage(Message msg) {
        synchronized (this) {
            switch (msg.what) {
                case 1:
                    progressConnectingCallState();
                    break;
            }
        }
    }

    /* JADX WARNING: Missing block: B:32:0x0058, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean triggerRing(String number) {
        synchronized (this) {
            int empty = -1;
            boolean isCallWaiting = false;
            for (int i = 0; i < this.mCalls.length; i++) {
                CallInfo call = this.mCalls[i];
                if (call == null && empty < 0) {
                    empty = i;
                } else if (call != null && (call.mState == State.INCOMING || call.mState == State.WAITING)) {
                    Rlog.w("ModelInterpreter", "triggerRing failed; phone already ringing");
                    return false;
                } else if (call != null) {
                    isCallWaiting = true;
                }
            }
            if (empty < 0) {
                Rlog.w("ModelInterpreter", "triggerRing failed; all full");
                return false;
            }
            this.mCalls[empty] = CallInfo.createIncomingCall(PhoneNumberUtils.extractNetworkPortion(number));
            if (isCallWaiting) {
                this.mCalls[empty].mState = State.WAITING;
            }
        }
    }

    public void progressConnectingCallState() {
        synchronized (this) {
            int i = 0;
            while (i < this.mCalls.length) {
                CallInfo call = this.mCalls[i];
                if (call == null || call.mState != State.DIALING) {
                    if (call != null) {
                        if (call.mState == State.ALERTING) {
                            call.mState = State.ACTIVE;
                            break;
                        }
                    }
                    i++;
                } else {
                    call.mState = State.ALERTING;
                    if (this.mAutoProgressConnecting) {
                        sendMessageDelayed(obtainMessage(1, call), 500);
                    }
                }
            }
        }
    }

    public void progressConnectingToActive() {
        synchronized (this) {
            for (CallInfo call : this.mCalls) {
                if (call != null && (call.mState == State.DIALING || call.mState == State.ALERTING)) {
                    call.mState = State.ACTIVE;
                    break;
                }
            }
        }
    }

    public void setAutoProgressConnectingCall(boolean b) {
        this.mAutoProgressConnecting = b;
    }

    public void setNextDialFailImmediately(boolean b) {
        this.mNextDialFailImmediately = b;
    }

    public boolean triggerHangupForeground() {
        boolean found;
        synchronized (this) {
            int i;
            CallInfo call;
            found = false;
            for (i = 0; i < this.mCalls.length; i++) {
                call = this.mCalls[i];
                if (call != null && (call.mState == State.INCOMING || call.mState == State.WAITING)) {
                    this.mCalls[i] = null;
                    found = true;
                }
            }
            for (i = 0; i < this.mCalls.length; i++) {
                call = this.mCalls[i];
                if (call != null && (call.mState == State.DIALING || call.mState == State.ACTIVE || call.mState == State.ALERTING)) {
                    this.mCalls[i] = null;
                    found = true;
                }
            }
        }
        return found;
    }

    public boolean triggerHangupBackground() {
        boolean found;
        synchronized (this) {
            found = false;
            for (int i = 0; i < this.mCalls.length; i++) {
                CallInfo call = this.mCalls[i];
                if (call != null && call.mState == State.HOLDING) {
                    this.mCalls[i] = null;
                    found = true;
                }
            }
        }
        return found;
    }

    public boolean triggerHangupAll() {
        boolean found;
        synchronized (this) {
            found = false;
            for (int i = 0; i < this.mCalls.length; i++) {
                CallInfo call = this.mCalls[i];
                if (this.mCalls[i] != null) {
                    found = true;
                }
                this.mCalls[i] = null;
            }
        }
        return found;
    }

    public boolean onAnswer() {
        synchronized (this) {
            int i = 0;
            while (i < this.mCalls.length) {
                CallInfo call = this.mCalls[i];
                if (call == null || !(call.mState == State.INCOMING || call.mState == State.WAITING)) {
                    i++;
                } else {
                    boolean switchActiveAndHeldOrWaiting = switchActiveAndHeldOrWaiting();
                    return switchActiveAndHeldOrWaiting;
                }
            }
            return false;
        }
    }

    public boolean onHangup() {
        boolean found = false;
        for (int i = 0; i < this.mCalls.length; i++) {
            CallInfo call = this.mCalls[i];
            if (!(call == null || call.mState == State.WAITING)) {
                this.mCalls[i] = null;
                found = true;
            }
        }
        return found;
    }

    public boolean onChld(char c0, char c1) {
        boolean ret;
        int callIndex = 0;
        if (c1 != 0) {
            callIndex = c1 - 49;
            if (callIndex < 0 || callIndex >= this.mCalls.length) {
                return false;
            }
        }
        switch (c0) {
            case RadioError.NO_SMS_TO_ACK /*48*/:
                ret = releaseHeldOrUDUB();
                break;
            case '1':
                if (c1 > 0) {
                    if (this.mCalls[callIndex] != null) {
                        this.mCalls[callIndex] = null;
                        ret = true;
                        break;
                    }
                    ret = false;
                    break;
                }
                ret = releaseActiveAcceptHeldOrWaiting();
                break;
            case '2':
                if (c1 > 0) {
                    ret = separateCall(callIndex);
                    break;
                }
                ret = switchActiveAndHeldOrWaiting();
                break;
            case '3':
                ret = conference();
                break;
            case '4':
                ret = explicitCallTransfer();
                break;
            case '5':
                ret = false;
                break;
            default:
                ret = false;
                break;
        }
        return ret;
    }

    public boolean releaseHeldOrUDUB() {
        int i;
        CallInfo c;
        boolean found = false;
        for (i = 0; i < this.mCalls.length; i++) {
            c = this.mCalls[i];
            if (c != null && c.isRinging()) {
                found = true;
                this.mCalls[i] = null;
                break;
            }
        }
        if (!found) {
            for (i = 0; i < this.mCalls.length; i++) {
                c = this.mCalls[i];
                if (c != null && c.mState == State.HOLDING) {
                    this.mCalls[i] = null;
                }
            }
        }
        return true;
    }

    public boolean releaseActiveAcceptHeldOrWaiting() {
        int i;
        CallInfo c;
        boolean foundHeld = false;
        boolean foundActive = false;
        for (i = 0; i < this.mCalls.length; i++) {
            c = this.mCalls[i];
            if (c != null && c.mState == State.ACTIVE) {
                this.mCalls[i] = null;
                foundActive = true;
            }
        }
        if (!foundActive) {
            for (i = 0; i < this.mCalls.length; i++) {
                c = this.mCalls[i];
                if (c != null && (c.mState == State.DIALING || c.mState == State.ALERTING)) {
                    this.mCalls[i] = null;
                }
            }
        }
        for (CallInfo c2 : this.mCalls) {
            if (c2 != null && c2.mState == State.HOLDING) {
                c2.mState = State.ACTIVE;
                foundHeld = true;
            }
        }
        if (foundHeld) {
            return true;
        }
        i = 0;
        while (i < this.mCalls.length) {
            c2 = this.mCalls[i];
            if (c2 == null || !c2.isRinging()) {
                i++;
            } else {
                c2.mState = State.ACTIVE;
                return true;
            }
        }
        return true;
    }

    public boolean switchActiveAndHeldOrWaiting() {
        boolean hasHeld = false;
        for (CallInfo c : this.mCalls) {
            if (c != null && c.mState == State.HOLDING) {
                hasHeld = true;
                break;
            }
        }
        for (CallInfo c2 : this.mCalls) {
            if (c2 != null) {
                if (c2.mState == State.ACTIVE) {
                    c2.mState = State.HOLDING;
                } else if (c2.mState == State.HOLDING) {
                    c2.mState = State.ACTIVE;
                } else if (!hasHeld && c2.isRinging()) {
                    c2.mState = State.ACTIVE;
                }
            }
        }
        return true;
    }

    public boolean separateCall(int index) {
        try {
            CallInfo c = this.mCalls[index];
            if (c == null || c.isConnecting() || countActiveLines() != 1) {
                return false;
            }
            c.mState = State.ACTIVE;
            c.mIsMpty = false;
            for (int i = 0; i < this.mCalls.length; i++) {
                int countHeld = 0;
                int lastHeld = 0;
                if (i != index) {
                    CallInfo cb = this.mCalls[i];
                    if (cb != null && cb.mState == State.ACTIVE) {
                        cb.mState = State.HOLDING;
                        countHeld = 1;
                        lastHeld = i;
                    }
                }
                if (countHeld == 1) {
                    this.mCalls[lastHeld].mIsMpty = false;
                }
            }
            return true;
        } catch (InvalidStateEx e) {
            return false;
        }
    }

    public boolean conference() {
        int countCalls = 0;
        for (CallInfo c : this.mCalls) {
            if (c != null) {
                countCalls++;
                if (c.isConnecting()) {
                    return false;
                }
            }
        }
        for (CallInfo c2 : this.mCalls) {
            if (c2 != null) {
                c2.mState = State.ACTIVE;
                if (countCalls > 0) {
                    c2.mIsMpty = true;
                }
            }
        }
        return true;
    }

    public boolean explicitCallTransfer() {
        int countCalls = 0;
        for (CallInfo c : this.mCalls) {
            if (c != null) {
                countCalls++;
                if (c.isConnecting()) {
                    return false;
                }
            }
        }
        return triggerHangupAll();
    }

    public boolean onDial(String address) {
        int freeSlot = -1;
        Rlog.d("GSM", "SC> dial '" + address + "'");
        if (this.mNextDialFailImmediately) {
            this.mNextDialFailImmediately = false;
            Rlog.d("GSM", "SC< dial fail (per request)");
            return false;
        }
        String phNum = PhoneNumberUtils.extractNetworkPortion(address);
        if (phNum.length() == 0) {
            Rlog.d("GSM", "SC< dial fail (invalid ph num)");
            return false;
        } else if (phNum.startsWith("*99") && phNum.endsWith("#")) {
            Rlog.d("GSM", "SC< dial ignored (gprs)");
            return true;
        } else {
            try {
                if (countActiveLines() > 1) {
                    Rlog.d("GSM", "SC< dial fail (invalid call state)");
                    return false;
                }
                int i = 0;
                while (i < this.mCalls.length) {
                    if (freeSlot < 0 && this.mCalls[i] == null) {
                        freeSlot = i;
                    }
                    if (this.mCalls[i] == null || (this.mCalls[i].isActiveOrHeld() ^ 1) == 0) {
                        if (this.mCalls[i] != null && this.mCalls[i].mState == State.ACTIVE) {
                            this.mCalls[i].mState = State.HOLDING;
                        }
                        i++;
                    } else {
                        Rlog.d("GSM", "SC< dial fail (invalid call state)");
                        return false;
                    }
                }
                if (freeSlot < 0) {
                    Rlog.d("GSM", "SC< dial fail (invalid call state)");
                    return false;
                }
                this.mCalls[freeSlot] = CallInfo.createOutgoingCall(phNum);
                if (this.mAutoProgressConnecting) {
                    sendMessageDelayed(obtainMessage(1, this.mCalls[freeSlot]), 500);
                }
                Rlog.d("GSM", "SC< dial (slot = " + freeSlot + ")");
                return true;
            } catch (InvalidStateEx e) {
                Rlog.d("GSM", "SC< dial fail (invalid call state)");
                return false;
            }
        }
    }

    public List<DriverCall> getDriverCalls() {
        ArrayList<DriverCall> ret = new ArrayList(this.mCalls.length);
        for (int i = 0; i < this.mCalls.length; i++) {
            CallInfo c = this.mCalls[i];
            if (c != null) {
                ret.add(c.toDriverCall(i + 1));
            }
        }
        Rlog.d("GSM", "SC< getDriverCalls " + ret);
        return ret;
    }

    public List<String> getClccLines() {
        ArrayList<String> ret = new ArrayList(this.mCalls.length);
        for (int i = 0; i < this.mCalls.length; i++) {
            CallInfo c = this.mCalls[i];
            if (c != null) {
                ret.add(c.toCLCCLine(i + 1));
            }
        }
        return ret;
    }

    private int countActiveLines() throws InvalidStateEx {
        int hasMpty = 0;
        int hasHeld = 0;
        int hasActive = 0;
        int hasConnecting = 0;
        int hasRinging = 0;
        boolean mptyIsHeld = false;
        for (CallInfo call : this.mCalls) {
            if (call != null) {
                int i;
                if (hasMpty == 0 && call.mIsMpty) {
                    if (call.mState == State.HOLDING) {
                        mptyIsHeld = true;
                    } else {
                        mptyIsHeld = false;
                    }
                } else if (call.mIsMpty && mptyIsHeld && call.mState == State.ACTIVE) {
                    Rlog.e("ModelInterpreter", "Invalid state");
                    throw new InvalidStateEx();
                } else if (!call.mIsMpty && hasMpty != 0 && mptyIsHeld && call.mState == State.HOLDING) {
                    Rlog.e("ModelInterpreter", "Invalid state");
                    throw new InvalidStateEx();
                }
                hasMpty |= call.mIsMpty;
                if (call.mState == State.HOLDING) {
                    i = 1;
                } else {
                    i = 0;
                }
                hasHeld |= i;
                if (call.mState == State.ACTIVE) {
                    i = 1;
                } else {
                    i = 0;
                }
                hasActive |= i;
                hasConnecting |= call.isConnecting();
                hasRinging |= call.isRinging();
            }
        }
        int ret = 0;
        if (hasHeld != 0) {
            ret = 1;
        }
        if (hasActive != 0) {
            ret++;
        }
        if (hasConnecting != 0) {
            ret++;
        }
        if (hasRinging != 0) {
            return ret + 1;
        }
        return ret;
    }
}
