package com.android.internal.telephony.sip;

import android.os.SystemClock;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.UUSInfo;

/* access modifiers changed from: package-private */
public abstract class SipConnectionBase extends Connection {
    private static final boolean DBG = true;
    private static final String LOG_TAG = "SipConnBase";
    private static final boolean VDBG = false;
    private long mConnectTime;
    private long mConnectTimeReal;
    private long mCreateTime;
    private long mDisconnectTime;
    private long mDuration = -1;
    private long mHoldingStartTime;

    /* access modifiers changed from: protected */
    public abstract Phone getPhone();

    SipConnectionBase(String dialString) {
        super(3);
        log("SipConnectionBase: ctor dialString=" + SipPhone.hidePii(dialString));
        this.mPostDialString = PhoneNumberUtils.extractPostDialPortion(dialString);
        this.mCreateTime = System.currentTimeMillis();
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.internal.telephony.sip.SipConnectionBase$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$Call$State = new int[Call.State.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$Call$State[Call.State.ACTIVE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$Call$State[Call.State.DISCONNECTED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$Call$State[Call.State.HOLDING.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setState(Call.State state) {
        log("setState: state=" + state);
        int i = AnonymousClass1.$SwitchMap$com$android$internal$telephony$Call$State[state.ordinal()];
        if (i != 1) {
            if (i == 2) {
                this.mDuration = getDurationMillis();
                this.mDisconnectTime = System.currentTimeMillis();
            } else if (i == 3) {
                this.mHoldingStartTime = SystemClock.elapsedRealtime();
            }
        } else if (this.mConnectTime == 0) {
            this.mConnectTimeReal = SystemClock.elapsedRealtime();
            this.mConnectTime = System.currentTimeMillis();
        }
    }

    @Override // com.android.internal.telephony.Connection
    public long getCreateTime() {
        return this.mCreateTime;
    }

    @Override // com.android.internal.telephony.Connection
    public long getConnectTime() {
        return this.mConnectTime;
    }

    @Override // com.android.internal.telephony.Connection
    public long getDisconnectTime() {
        return this.mDisconnectTime;
    }

    @Override // com.android.internal.telephony.Connection
    public long getDurationMillis() {
        if (this.mConnectTimeReal == 0) {
            return 0;
        }
        if (this.mDuration < 0) {
            return SystemClock.elapsedRealtime() - this.mConnectTimeReal;
        }
        return this.mDuration;
    }

    @Override // com.android.internal.telephony.Connection
    public long getHoldDurationMillis() {
        if (getState() != Call.State.HOLDING) {
            return 0;
        }
        return SystemClock.elapsedRealtime() - this.mHoldingStartTime;
    }

    /* access modifiers changed from: package-private */
    public void setDisconnectCause(int cause) {
        log("setDisconnectCause: prev=" + this.mCause + " new=" + cause);
        this.mCause = cause;
    }

    @Override // com.android.internal.telephony.Connection
    public String getVendorDisconnectCause() {
        return null;
    }

    @Override // com.android.internal.telephony.Connection
    public void proceedAfterWaitChar() {
        log("proceedAfterWaitChar: ignore");
    }

    @Override // com.android.internal.telephony.Connection
    public void proceedAfterWildChar(String str) {
        log("proceedAfterWildChar: ignore");
    }

    @Override // com.android.internal.telephony.Connection
    public void cancelPostDial() {
        log("cancelPostDial: ignore");
    }

    private void log(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    @Override // com.android.internal.telephony.Connection
    public int getNumberPresentation() {
        return 1;
    }

    @Override // com.android.internal.telephony.Connection
    public UUSInfo getUUSInfo() {
        return null;
    }

    @Override // com.android.internal.telephony.Connection
    public int getPreciseDisconnectCause() {
        return 0;
    }

    @Override // com.android.internal.telephony.Connection
    public long getHoldingStartTime() {
        return this.mHoldingStartTime;
    }

    @Override // com.android.internal.telephony.Connection
    public long getConnectTimeReal() {
        return this.mConnectTimeReal;
    }

    @Override // com.android.internal.telephony.Connection
    public Connection getOrigConnection() {
        return null;
    }

    @Override // com.android.internal.telephony.Connection
    public boolean isMultiparty() {
        return false;
    }
}
