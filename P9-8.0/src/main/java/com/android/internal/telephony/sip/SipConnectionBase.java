package com.android.internal.telephony.sip;

import android.os.SystemClock;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import com.android.internal.telephony.Call.State;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.UUSInfo;

abstract class SipConnectionBase extends Connection {
    private static final /* synthetic */ int[] -com-android-internal-telephony-Call$StateSwitchesValues = null;
    private static final boolean DBG = true;
    private static final String LOG_TAG = "SipConnBase";
    private static final boolean VDBG = false;
    private long mConnectTime;
    private long mConnectTimeReal;
    private long mCreateTime;
    private long mDisconnectTime;
    private long mDuration = -1;
    private long mHoldingStartTime;

    private static /* synthetic */ int[] -getcom-android-internal-telephony-Call$StateSwitchesValues() {
        if (-com-android-internal-telephony-Call$StateSwitchesValues != null) {
            return -com-android-internal-telephony-Call$StateSwitchesValues;
        }
        int[] iArr = new int[State.values().length];
        try {
            iArr[State.ACTIVE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[State.ALERTING.ordinal()] = 4;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[State.DIALING.ordinal()] = 5;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[State.DISCONNECTED.ordinal()] = 2;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[State.DISCONNECTING.ordinal()] = 6;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[State.HOLDING.ordinal()] = 3;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[State.IDLE.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[State.INCOMING.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[State.WAITING.ordinal()] = 9;
        } catch (NoSuchFieldError e9) {
        }
        -com-android-internal-telephony-Call$StateSwitchesValues = iArr;
        return iArr;
    }

    protected abstract Phone getPhone();

    SipConnectionBase(String dialString) {
        super(3);
        log("SipConnectionBase: ctor dialString=" + SipPhone.hidePii(dialString));
        this.mPostDialString = PhoneNumberUtils.extractPostDialPortion(dialString);
        this.mCreateTime = System.currentTimeMillis();
    }

    protected void setState(State state) {
        log("setState: state=" + state);
        switch (-getcom-android-internal-telephony-Call$StateSwitchesValues()[state.ordinal()]) {
            case 1:
                if (this.mConnectTime == 0) {
                    this.mConnectTimeReal = SystemClock.elapsedRealtime();
                    this.mConnectTime = System.currentTimeMillis();
                    return;
                }
                return;
            case 2:
                this.mDuration = getDurationMillis();
                this.mDisconnectTime = System.currentTimeMillis();
                return;
            case 3:
                this.mHoldingStartTime = SystemClock.elapsedRealtime();
                return;
            default:
                return;
        }
    }

    public long getCreateTime() {
        return this.mCreateTime;
    }

    public long getConnectTime() {
        return this.mConnectTime;
    }

    public long getDisconnectTime() {
        return this.mDisconnectTime;
    }

    public long getDurationMillis() {
        if (this.mConnectTimeReal == 0) {
            return 0;
        }
        if (this.mDuration < 0) {
            return SystemClock.elapsedRealtime() - this.mConnectTimeReal;
        }
        return this.mDuration;
    }

    public long getHoldDurationMillis() {
        if (getState() != State.HOLDING) {
            return 0;
        }
        return SystemClock.elapsedRealtime() - this.mHoldingStartTime;
    }

    void setDisconnectCause(int cause) {
        log("setDisconnectCause: prev=" + this.mCause + " new=" + cause);
        this.mCause = cause;
    }

    public String getVendorDisconnectCause() {
        return null;
    }

    public void proceedAfterWaitChar() {
        log("proceedAfterWaitChar: ignore");
    }

    public void proceedAfterWildChar(String str) {
        log("proceedAfterWildChar: ignore");
    }

    public void cancelPostDial() {
        log("cancelPostDial: ignore");
    }

    private void log(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    public int getNumberPresentation() {
        return 1;
    }

    public UUSInfo getUUSInfo() {
        return null;
    }

    public int getPreciseDisconnectCause() {
        return 0;
    }

    public long getHoldingStartTime() {
        return this.mHoldingStartTime;
    }

    public long getConnectTimeReal() {
        return this.mConnectTimeReal;
    }

    public Connection getOrigConnection() {
        return null;
    }

    public boolean isMultiparty() {
        return false;
    }
}
