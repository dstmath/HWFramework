package com.android.internal.telephony.test;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import com.android.internal.telephony.DriverCall;
import com.android.internal.telephony.RadioNVItems;
import com.android.internal.telephony.gsm.SmsCbConstants;
import com.android.internal.telephony.imsphone.CallFailCause;
import java.util.ArrayList;
import java.util.List;

class SimulatedGsmCallState extends Handler {
    static final int CONNECTING_PAUSE_MSEC = 500;
    static final int EVENT_PROGRESS_CALL_STATE = 1;
    static final int MAX_CALLS = 7;
    private boolean mAutoProgressConnecting;
    CallInfo[] mCalls;
    private boolean mNextDialFailImmediately;

    public SimulatedGsmCallState(Looper looper) {
        super(looper);
        this.mCalls = new CallInfo[MAX_CALLS];
        this.mAutoProgressConnecting = true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleMessage(Message msg) {
        synchronized (this) {
            switch (msg.what) {
                case EVENT_PROGRESS_CALL_STATE /*1*/:
                    progressConnectingCallState();
                    break;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean triggerRing(String number) {
        synchronized (this) {
            int empty = -1;
            boolean isCallWaiting = false;
            int i = 0;
            while (true) {
                if (i >= this.mCalls.length) {
                    break;
                }
                CallInfo call = this.mCalls[i];
                if (call == null && empty < 0) {
                    empty = i;
                } else if (call == null || !(call.mState == State.INCOMING || call.mState == State.WAITING)) {
                    if (call != null) {
                        isCallWaiting = true;
                    }
                }
                i += EVENT_PROGRESS_CALL_STATE;
            }
            Rlog.w("ModelInterpreter", "triggerRing failed; phone already ringing");
            return false;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void progressConnectingCallState() {
        synchronized (this) {
            CallInfo call;
            int i = 0;
            while (true) {
                if (i >= this.mCalls.length) {
                    break;
                }
                call = this.mCalls[i];
                if (call != null && call.mState == State.DIALING) {
                    break;
                }
                if (call != null) {
                    if (call.mState == State.ALERTING) {
                        break;
                    }
                }
                i += EVENT_PROGRESS_CALL_STATE;
            }
            call.mState = State.ALERTING;
            if (this.mAutoProgressConnecting) {
                sendMessageDelayed(obtainMessage(EVENT_PROGRESS_CALL_STATE, call), 500);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void progressConnectingToActive() {
        synchronized (this) {
            CallInfo call;
            int i = 0;
            while (true) {
                if (i >= this.mCalls.length) {
                    break;
                }
                call = this.mCalls[i];
                if (call == null || !(call.mState == State.DIALING || call.mState == State.ALERTING)) {
                    i += EVENT_PROGRESS_CALL_STATE;
                }
            }
            call.mState = State.ACTIVE;
        }
    }

    public void setAutoProgressConnectingCall(boolean b) {
        this.mAutoProgressConnecting = b;
    }

    public void setNextDialFailImmediately(boolean b) {
        this.mNextDialFailImmediately = b;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean triggerHangupForeground() {
        boolean found;
        synchronized (this) {
            found = false;
            int i = 0;
            while (true) {
                if (i >= this.mCalls.length) {
                    break;
                }
                CallInfo call = this.mCalls[i];
                if (call != null && (call.mState == State.INCOMING || call.mState == State.WAITING)) {
                    this.mCalls[i] = null;
                    found = true;
                }
                i += EVENT_PROGRESS_CALL_STATE;
            }
            for (i = 0; i < this.mCalls.length; i += EVENT_PROGRESS_CALL_STATE) {
                call = this.mCalls[i];
                if (call != null) {
                    if (!(call.mState == State.DIALING || call.mState == State.ACTIVE)) {
                        if (call.mState == State.ALERTING) {
                        }
                    }
                    this.mCalls[i] = null;
                    found = true;
                }
            }
        }
        return found;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean triggerHangupBackground() {
        boolean found;
        synchronized (this) {
            found = false;
            int i = 0;
            while (true) {
                if (i < this.mCalls.length) {
                    CallInfo call = this.mCalls[i];
                    if (call != null && call.mState == State.HOLDING) {
                        this.mCalls[i] = null;
                        found = true;
                    }
                    i += EVENT_PROGRESS_CALL_STATE;
                }
            }
        }
        return found;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean triggerHangupAll() {
        boolean found;
        synchronized (this) {
            found = false;
            int i = 0;
            while (true) {
                if (i < this.mCalls.length) {
                    CallInfo call = this.mCalls[i];
                    if (this.mCalls[i] != null) {
                        found = true;
                    }
                    this.mCalls[i] = null;
                    i += EVENT_PROGRESS_CALL_STATE;
                }
            }
        }
        return found;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onAnswer() {
        synchronized (this) {
            int i = 0;
            while (true) {
                if (i < this.mCalls.length) {
                    CallInfo call = this.mCalls[i];
                    if (call == null || !(call.mState == State.INCOMING || call.mState == State.WAITING)) {
                        i += EVENT_PROGRESS_CALL_STATE;
                    }
                } else {
                    return false;
                }
            }
            boolean switchActiveAndHeldOrWaiting = switchActiveAndHeldOrWaiting();
            return switchActiveAndHeldOrWaiting;
        }
    }

    public boolean onHangup() {
        boolean found = false;
        for (int i = 0; i < this.mCalls.length; i += EVENT_PROGRESS_CALL_STATE) {
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
        if (c1 != '\u0000') {
            callIndex = c1 - 49;
            if (callIndex < 0 || callIndex >= this.mCalls.length) {
                return false;
            }
        }
        switch (c0) {
            case '0':
                ret = releaseHeldOrUDUB();
                break;
            case CallFailCause.QOS_NOT_AVAIL /*49*/:
                if (c1 > '\u0000') {
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
            case SmsCbConstants.MESSAGE_ID_GSMA_ALLOCATED_CHANNEL_50 /*50*/:
                if (c1 > '\u0000') {
                    ret = separateCall(callIndex);
                    break;
                }
                ret = switchActiveAndHeldOrWaiting();
                break;
            case RadioNVItems.RIL_NV_CDMA_PRL_VERSION /*51*/:
                ret = conference();
                break;
            case RadioNVItems.RIL_NV_CDMA_BC10 /*52*/:
                ret = explicitCallTransfer();
                break;
            case RadioNVItems.RIL_NV_CDMA_BC14 /*53*/:
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
        boolean found = false;
        for (i = 0; i < this.mCalls.length; i += EVENT_PROGRESS_CALL_STATE) {
            CallInfo c = this.mCalls[i];
            if (c != null && c.isRinging()) {
                found = true;
                this.mCalls[i] = null;
                break;
            }
        }
        if (!found) {
            for (i = 0; i < this.mCalls.length; i += EVENT_PROGRESS_CALL_STATE) {
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
        boolean foundHeld = false;
        boolean foundActive = false;
        for (i = 0; i < this.mCalls.length; i += EVENT_PROGRESS_CALL_STATE) {
            CallInfo c = this.mCalls[i];
            if (c != null && c.mState == State.ACTIVE) {
                this.mCalls[i] = null;
                foundActive = true;
            }
        }
        if (!foundActive) {
            for (i = 0; i < this.mCalls.length; i += EVENT_PROGRESS_CALL_STATE) {
                c = this.mCalls[i];
                if (c != null && (c.mState == State.DIALING || c.mState == State.ALERTING)) {
                    this.mCalls[i] = null;
                }
            }
        }
        for (i = 0; i < this.mCalls.length; i += EVENT_PROGRESS_CALL_STATE) {
            c = this.mCalls[i];
            if (c != null && c.mState == State.HOLDING) {
                c.mState = State.ACTIVE;
                foundHeld = true;
            }
        }
        if (foundHeld) {
            return true;
        }
        i = 0;
        while (i < this.mCalls.length) {
            c = this.mCalls[i];
            if (c == null || !c.isRinging()) {
                i += EVENT_PROGRESS_CALL_STATE;
            } else {
                c.mState = State.ACTIVE;
                return true;
            }
        }
        return true;
    }

    public boolean switchActiveAndHeldOrWaiting() {
        int i;
        boolean hasHeld = false;
        for (i = 0; i < this.mCalls.length; i += EVENT_PROGRESS_CALL_STATE) {
            CallInfo c = this.mCalls[i];
            if (c != null && c.mState == State.HOLDING) {
                hasHeld = true;
                break;
            }
        }
        for (i = 0; i < this.mCalls.length; i += EVENT_PROGRESS_CALL_STATE) {
            c = this.mCalls[i];
            if (c != null) {
                if (c.mState == State.ACTIVE) {
                    c.mState = State.HOLDING;
                } else if (c.mState == State.HOLDING) {
                    c.mState = State.ACTIVE;
                } else if (!hasHeld && c.isRinging()) {
                    c.mState = State.ACTIVE;
                }
            }
        }
        return true;
    }

    public boolean separateCall(int index) {
        try {
            CallInfo c = this.mCalls[index];
            if (c == null || c.isConnecting() || countActiveLines() != EVENT_PROGRESS_CALL_STATE) {
                return false;
            }
            c.mState = State.ACTIVE;
            c.mIsMpty = false;
            for (int i = 0; i < this.mCalls.length; i += EVENT_PROGRESS_CALL_STATE) {
                int countHeld = 0;
                int lastHeld = 0;
                if (i != index) {
                    CallInfo cb = this.mCalls[i];
                    if (cb != null && cb.mState == State.ACTIVE) {
                        cb.mState = State.HOLDING;
                        countHeld = EVENT_PROGRESS_CALL_STATE;
                        lastHeld = i;
                    }
                }
                if (countHeld == EVENT_PROGRESS_CALL_STATE) {
                    this.mCalls[lastHeld].mIsMpty = false;
                }
            }
            return true;
        } catch (InvalidStateEx e) {
            return false;
        }
    }

    public boolean conference() {
        int i;
        int countCalls = 0;
        for (i = 0; i < this.mCalls.length; i += EVENT_PROGRESS_CALL_STATE) {
            CallInfo c = this.mCalls[i];
            if (c != null) {
                countCalls += EVENT_PROGRESS_CALL_STATE;
                if (c.isConnecting()) {
                    return false;
                }
            }
        }
        for (i = 0; i < this.mCalls.length; i += EVENT_PROGRESS_CALL_STATE) {
            c = this.mCalls[i];
            if (c != null) {
                c.mState = State.ACTIVE;
                if (countCalls > 0) {
                    c.mIsMpty = true;
                }
            }
        }
        return true;
    }

    public boolean explicitCallTransfer() {
        int countCalls = 0;
        for (int i = 0; i < this.mCalls.length; i += EVENT_PROGRESS_CALL_STATE) {
            CallInfo c = this.mCalls[i];
            if (c != null) {
                countCalls += EVENT_PROGRESS_CALL_STATE;
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
                if (countActiveLines() > EVENT_PROGRESS_CALL_STATE) {
                    Rlog.d("GSM", "SC< dial fail (invalid call state)");
                    return false;
                }
                int i = 0;
                while (i < this.mCalls.length) {
                    if (freeSlot < 0 && this.mCalls[i] == null) {
                        freeSlot = i;
                    }
                    if (this.mCalls[i] == null || this.mCalls[i].isActiveOrHeld()) {
                        if (this.mCalls[i] != null && this.mCalls[i].mState == State.ACTIVE) {
                            this.mCalls[i].mState = State.HOLDING;
                        }
                        i += EVENT_PROGRESS_CALL_STATE;
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
                    sendMessageDelayed(obtainMessage(EVENT_PROGRESS_CALL_STATE, this.mCalls[freeSlot]), 500);
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
        for (int i = 0; i < this.mCalls.length; i += EVENT_PROGRESS_CALL_STATE) {
            CallInfo c = this.mCalls[i];
            if (c != null) {
                ret.add(c.toDriverCall(i + EVENT_PROGRESS_CALL_STATE));
            }
        }
        Rlog.d("GSM", "SC< getDriverCalls " + ret);
        return ret;
    }

    public List<String> getClccLines() {
        ArrayList<String> ret = new ArrayList(this.mCalls.length);
        for (int i = 0; i < this.mCalls.length; i += EVENT_PROGRESS_CALL_STATE) {
            CallInfo c = this.mCalls[i];
            if (c != null) {
                ret.add(c.toCLCCLine(i + EVENT_PROGRESS_CALL_STATE));
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
        for (int i = 0; i < this.mCalls.length; i += EVENT_PROGRESS_CALL_STATE) {
            CallInfo call = this.mCalls[i];
            if (call != null) {
                int i2;
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
                    i2 = EVENT_PROGRESS_CALL_STATE;
                } else {
                    i2 = 0;
                }
                hasHeld |= i2;
                if (call.mState == State.ACTIVE) {
                    i2 = EVENT_PROGRESS_CALL_STATE;
                } else {
                    i2 = 0;
                }
                hasActive |= i2;
                hasConnecting |= call.isConnecting();
                hasRinging |= call.isRinging();
            }
        }
        int ret = 0;
        if (hasHeld != 0) {
            ret = EVENT_PROGRESS_CALL_STATE;
        }
        if (hasActive != 0) {
            ret += EVENT_PROGRESS_CALL_STATE;
        }
        if (hasConnecting != 0) {
            ret += EVENT_PROGRESS_CALL_STATE;
        }
        if (hasRinging != 0) {
            return ret + EVENT_PROGRESS_CALL_STATE;
        }
        return ret;
    }
}
