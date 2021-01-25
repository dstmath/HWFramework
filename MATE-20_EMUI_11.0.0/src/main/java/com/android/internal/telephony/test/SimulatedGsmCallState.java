package com.android.internal.telephony.test;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import com.android.internal.telephony.DriverCall;
import com.android.internal.telephony.test.CallInfo;
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

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        synchronized (this) {
            if (msg.what == 1) {
                progressConnectingCallState();
            }
        }
    }

    public boolean triggerRing(String number) {
        synchronized (this) {
            int empty = -1;
            boolean isCallWaiting = false;
            for (int i = 0; i < this.mCalls.length; i++) {
                CallInfo call = this.mCalls[i];
                if (call == null && empty < 0) {
                    empty = i;
                } else if (call != null && (call.mState == CallInfo.State.INCOMING || call.mState == CallInfo.State.WAITING)) {
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
                this.mCalls[empty].mState = CallInfo.State.WAITING;
            }
            return true;
        }
    }

    public void progressConnectingCallState() {
        synchronized (this) {
            int i = 0;
            while (true) {
                if (i >= this.mCalls.length) {
                    break;
                }
                CallInfo call = this.mCalls[i];
                if (call == null || call.mState != CallInfo.State.DIALING) {
                    if (call != null && call.mState == CallInfo.State.ALERTING) {
                        call.mState = CallInfo.State.ACTIVE;
                        break;
                    }
                    i++;
                } else {
                    call.mState = CallInfo.State.ALERTING;
                    if (this.mAutoProgressConnecting) {
                        sendMessageDelayed(obtainMessage(1, call), 500);
                    }
                }
            }
        }
    }

    public void progressConnectingToActive() {
        CallInfo call;
        synchronized (this) {
            int i = 0;
            while (true) {
                if (i >= this.mCalls.length) {
                    break;
                }
                call = this.mCalls[i];
                if (call == null || !(call.mState == CallInfo.State.DIALING || call.mState == CallInfo.State.ALERTING)) {
                    i++;
                }
            }
            call.mState = CallInfo.State.ACTIVE;
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
            found = false;
            for (int i = 0; i < this.mCalls.length; i++) {
                CallInfo call = this.mCalls[i];
                if (call != null && (call.mState == CallInfo.State.INCOMING || call.mState == CallInfo.State.WAITING)) {
                    this.mCalls[i] = null;
                    found = true;
                }
            }
            for (int i2 = 0; i2 < this.mCalls.length; i2++) {
                CallInfo call2 = this.mCalls[i2];
                if (call2 != null && (call2.mState == CallInfo.State.DIALING || call2.mState == CallInfo.State.ACTIVE || call2.mState == CallInfo.State.ALERTING)) {
                    this.mCalls[i2] = null;
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
                if (call != null && call.mState == CallInfo.State.HOLDING) {
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
                CallInfo callInfo = this.mCalls[i];
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
            for (int i = 0; i < this.mCalls.length; i++) {
                CallInfo call = this.mCalls[i];
                if (call != null && (call.mState == CallInfo.State.INCOMING || call.mState == CallInfo.State.WAITING)) {
                    return switchActiveAndHeldOrWaiting();
                }
            }
            return false;
        }
    }

    public boolean onHangup() {
        boolean found = false;
        int i = 0;
        while (true) {
            CallInfo[] callInfoArr = this.mCalls;
            if (i >= callInfoArr.length) {
                return found;
            }
            CallInfo call = callInfoArr[i];
            if (!(call == null || call.mState == CallInfo.State.WAITING)) {
                this.mCalls[i] = null;
                found = true;
            }
            i++;
        }
    }

    public boolean onChld(char c0, char c1) {
        int callIndex = 0;
        if (c1 != 0 && (c1 - '1' < 0 || callIndex >= this.mCalls.length)) {
            return false;
        }
        switch (c0) {
            case '0':
                return releaseHeldOrUDUB();
            case '1':
                if (c1 <= 0) {
                    return releaseActiveAcceptHeldOrWaiting();
                }
                CallInfo[] callInfoArr = this.mCalls;
                if (callInfoArr[callIndex] == null) {
                    return false;
                }
                callInfoArr[callIndex] = null;
                return true;
            case '2':
                if (c1 <= 0) {
                    return switchActiveAndHeldOrWaiting();
                }
                return separateCall(callIndex);
            case '3':
                return conference();
            case '4':
                return explicitCallTransfer();
            case '5':
                return false;
            default:
                return false;
        }
    }

    public boolean releaseHeldOrUDUB() {
        boolean found = false;
        int i = 0;
        while (true) {
            CallInfo[] callInfoArr = this.mCalls;
            if (i < callInfoArr.length) {
                CallInfo c = callInfoArr[i];
                if (c != null && c.isRinging()) {
                    found = true;
                    this.mCalls[i] = null;
                    break;
                }
                i++;
            } else {
                break;
            }
        }
        if (found) {
            return true;
        }
        int i2 = 0;
        while (true) {
            CallInfo[] callInfoArr2 = this.mCalls;
            if (i2 >= callInfoArr2.length) {
                return true;
            }
            CallInfo c2 = callInfoArr2[i2];
            if (c2 != null && c2.mState == CallInfo.State.HOLDING) {
                this.mCalls[i2] = null;
            }
            i2++;
        }
    }

    public boolean releaseActiveAcceptHeldOrWaiting() {
        boolean foundHeld = false;
        boolean foundActive = false;
        int i = 0;
        while (true) {
            CallInfo[] callInfoArr = this.mCalls;
            if (i >= callInfoArr.length) {
                break;
            }
            CallInfo c = callInfoArr[i];
            if (c != null && c.mState == CallInfo.State.ACTIVE) {
                this.mCalls[i] = null;
                foundActive = true;
            }
            i++;
        }
        if (!foundActive) {
            int i2 = 0;
            while (true) {
                CallInfo[] callInfoArr2 = this.mCalls;
                if (i2 >= callInfoArr2.length) {
                    break;
                }
                CallInfo c2 = callInfoArr2[i2];
                if (c2 != null && (c2.mState == CallInfo.State.DIALING || c2.mState == CallInfo.State.ALERTING)) {
                    this.mCalls[i2] = null;
                }
                i2++;
            }
        }
        int i3 = 0;
        while (true) {
            CallInfo[] callInfoArr3 = this.mCalls;
            if (i3 >= callInfoArr3.length) {
                break;
            }
            CallInfo c3 = callInfoArr3[i3];
            if (c3 != null && c3.mState == CallInfo.State.HOLDING) {
                c3.mState = CallInfo.State.ACTIVE;
                foundHeld = true;
            }
            i3++;
        }
        if (foundHeld) {
            return true;
        }
        int i4 = 0;
        while (true) {
            CallInfo[] callInfoArr4 = this.mCalls;
            if (i4 >= callInfoArr4.length) {
                return true;
            }
            CallInfo c4 = callInfoArr4[i4];
            if (c4 == null || !c4.isRinging()) {
                i4++;
            } else {
                c4.mState = CallInfo.State.ACTIVE;
                return true;
            }
        }
    }

    public boolean switchActiveAndHeldOrWaiting() {
        boolean hasHeld = false;
        int i = 0;
        while (true) {
            CallInfo[] callInfoArr = this.mCalls;
            if (i < callInfoArr.length) {
                CallInfo c = callInfoArr[i];
                if (c != null && c.mState == CallInfo.State.HOLDING) {
                    hasHeld = true;
                    break;
                }
                i++;
            } else {
                break;
            }
        }
        int i2 = 0;
        while (true) {
            CallInfo[] callInfoArr2 = this.mCalls;
            if (i2 >= callInfoArr2.length) {
                return true;
            }
            CallInfo c2 = callInfoArr2[i2];
            if (c2 != null) {
                if (c2.mState == CallInfo.State.ACTIVE) {
                    c2.mState = CallInfo.State.HOLDING;
                } else if (c2.mState == CallInfo.State.HOLDING) {
                    c2.mState = CallInfo.State.ACTIVE;
                } else if (!hasHeld && c2.isRinging()) {
                    c2.mState = CallInfo.State.ACTIVE;
                }
            }
            i2++;
        }
    }

    public boolean separateCall(int index) {
        CallInfo cb;
        try {
            CallInfo c = this.mCalls[index];
            if (c != null && !c.isConnecting()) {
                if (countActiveLines() == 1) {
                    c.mState = CallInfo.State.ACTIVE;
                    c.mIsMpty = false;
                    for (int i = 0; i < this.mCalls.length; i++) {
                        int countHeld = 0;
                        int lastHeld = 0;
                        if (!(i == index || (cb = this.mCalls[i]) == null || cb.mState != CallInfo.State.ACTIVE)) {
                            cb.mState = CallInfo.State.HOLDING;
                            countHeld = 0 + 1;
                            lastHeld = i;
                        }
                        if (countHeld == 1) {
                            this.mCalls[lastHeld].mIsMpty = false;
                        }
                    }
                    return true;
                }
            }
            return false;
        } catch (InvalidStateEx e) {
            return false;
        }
    }

    public boolean conference() {
        int countCalls = 0;
        int i = 0;
        while (true) {
            CallInfo[] callInfoArr = this.mCalls;
            if (i < callInfoArr.length) {
                CallInfo c = callInfoArr[i];
                if (c != null) {
                    countCalls++;
                    if (c.isConnecting()) {
                        return false;
                    }
                }
                i++;
            } else {
                int i2 = 0;
                while (true) {
                    CallInfo[] callInfoArr2 = this.mCalls;
                    if (i2 >= callInfoArr2.length) {
                        return true;
                    }
                    CallInfo c2 = callInfoArr2[i2];
                    if (c2 != null) {
                        c2.mState = CallInfo.State.ACTIVE;
                        if (countCalls > 0) {
                            c2.mIsMpty = true;
                        }
                    }
                    i2++;
                }
            }
        }
    }

    public boolean explicitCallTransfer() {
        int countCalls = 0;
        int i = 0;
        while (true) {
            CallInfo[] callInfoArr = this.mCalls;
            if (i >= callInfoArr.length) {
                return triggerHangupAll();
            }
            CallInfo c = callInfoArr[i];
            if (c != null) {
                countCalls++;
                if (c.isConnecting()) {
                    return false;
                }
            }
            i++;
        }
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
        } else if (!phNum.startsWith("*99") || !phNum.endsWith("#")) {
            try {
                if (countActiveLines() > 1) {
                    Rlog.d("GSM", "SC< dial fail (invalid call state)");
                    return false;
                }
                int i = 0;
                while (true) {
                    CallInfo[] callInfoArr = this.mCalls;
                    if (i < callInfoArr.length) {
                        if (freeSlot < 0 && callInfoArr[i] == null) {
                            freeSlot = i;
                        }
                        CallInfo[] callInfoArr2 = this.mCalls;
                        if (callInfoArr2[i] == null || callInfoArr2[i].isActiveOrHeld()) {
                            CallInfo[] callInfoArr3 = this.mCalls;
                            if (callInfoArr3[i] != null && callInfoArr3[i].mState == CallInfo.State.ACTIVE) {
                                this.mCalls[i].mState = CallInfo.State.HOLDING;
                            }
                            i++;
                        } else {
                            Rlog.d("GSM", "SC< dial fail (invalid call state)");
                            return false;
                        }
                    } else if (freeSlot < 0) {
                        Rlog.d("GSM", "SC< dial fail (invalid call state)");
                        return false;
                    } else {
                        callInfoArr[freeSlot] = CallInfo.createOutgoingCall(phNum);
                        if (this.mAutoProgressConnecting) {
                            sendMessageDelayed(obtainMessage(1, this.mCalls[freeSlot]), 500);
                        }
                        Rlog.d("GSM", "SC< dial (slot = " + freeSlot + ")");
                        return true;
                    }
                }
            } catch (InvalidStateEx e) {
                Rlog.d("GSM", "SC< dial fail (invalid call state)");
                return false;
            }
        } else {
            Rlog.d("GSM", "SC< dial ignored (gprs)");
            return true;
        }
    }

    public List<DriverCall> getDriverCalls() {
        ArrayList<DriverCall> ret = new ArrayList<>(this.mCalls.length);
        int i = 0;
        while (true) {
            CallInfo[] callInfoArr = this.mCalls;
            if (i < callInfoArr.length) {
                CallInfo c = callInfoArr[i];
                if (c != null) {
                    ret.add(c.toDriverCall(i + 1));
                }
                i++;
            } else {
                Rlog.d("GSM", "SC< getDriverCalls " + ret);
                return ret;
            }
        }
    }

    public List<String> getClccLines() {
        ArrayList<String> ret = new ArrayList<>(this.mCalls.length);
        int i = 0;
        while (true) {
            CallInfo[] callInfoArr = this.mCalls;
            if (i >= callInfoArr.length) {
                return ret;
            }
            CallInfo c = callInfoArr[i];
            if (c != null) {
                ret.add(c.toCLCCLine(i + 1));
            }
            i++;
        }
    }

    private int countActiveLines() throws InvalidStateEx {
        boolean hasMpty = false;
        boolean hasHeld = false;
        boolean hasActive = false;
        boolean hasConnecting = false;
        boolean hasRinging = false;
        boolean mptyIsHeld = false;
        int i = 0;
        while (true) {
            CallInfo[] callInfoArr = this.mCalls;
            if (i < callInfoArr.length) {
                CallInfo call = callInfoArr[i];
                if (call != null) {
                    boolean z = false;
                    if (!hasMpty && call.mIsMpty) {
                        mptyIsHeld = call.mState == CallInfo.State.HOLDING;
                    } else if (call.mIsMpty && mptyIsHeld && call.mState == CallInfo.State.ACTIVE) {
                        Rlog.e("ModelInterpreter", "Invalid state");
                        throw new InvalidStateEx();
                    } else if (!call.mIsMpty && hasMpty && mptyIsHeld && call.mState == CallInfo.State.HOLDING) {
                        Rlog.e("ModelInterpreter", "Invalid state");
                        throw new InvalidStateEx();
                    }
                    hasMpty |= call.mIsMpty;
                    hasHeld |= call.mState == CallInfo.State.HOLDING;
                    if (call.mState == CallInfo.State.ACTIVE) {
                        z = true;
                    }
                    hasActive |= z;
                    hasConnecting |= call.isConnecting();
                    hasRinging |= call.isRinging();
                }
                i++;
            } else {
                int ret = 0;
                if (hasHeld) {
                    ret = 0 + 1;
                }
                if (hasActive) {
                    ret++;
                }
                if (hasConnecting) {
                    ret++;
                }
                if (hasRinging) {
                    return ret + 1;
                }
                return ret;
            }
        }
    }
}
