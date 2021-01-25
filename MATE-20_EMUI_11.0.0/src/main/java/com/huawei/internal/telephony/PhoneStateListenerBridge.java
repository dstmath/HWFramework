package com.huawei.internal.telephony;

import android.os.Looper;
import android.telephony.PhoneStateListener;
import android.telephony.PreciseCallState;
import huawei.telephony.PreciseCallStateExt;

public class PhoneStateListenerBridge extends PhoneStateListener {
    private PhoneStateListenerExt mPhoneStateListenerExt;

    /* access modifiers changed from: package-private */
    public void setPhoneStateListenerExt(PhoneStateListenerExt phoneStateListenerExt) {
        this.mPhoneStateListenerExt = phoneStateListenerExt;
    }

    public PhoneStateListenerBridge() {
    }

    public PhoneStateListenerBridge(int subscription) {
        super(Integer.valueOf(subscription));
    }

    public PhoneStateListenerBridge(int subId, Looper looper) {
        super(Integer.valueOf(subId), looper);
    }

    public void onPreciseCallStateChanged(PreciseCallState callState) {
        if (this.mPhoneStateListenerExt != null) {
            this.mPhoneStateListenerExt.onPreciseCallStateChangedHw(PreciseCallStateExt.getPreciseCallStateExt(callState));
        }
    }

    @Override // android.telephony.PhoneStateListener
    public void onCallStateChanged(int state, String incomingNumber) {
        PhoneStateListenerExt phoneStateListenerExt = this.mPhoneStateListenerExt;
        if (phoneStateListenerExt != null) {
            phoneStateListenerExt.onCallStateChanged(state, incomingNumber);
        }
    }

    public void onRadioPowerStateChanged(int state) {
        PhoneStateListenerExt phoneStateListenerExt = this.mPhoneStateListenerExt;
        if (phoneStateListenerExt != null) {
            phoneStateListenerExt.onRadioPowerStateChanged(state);
        }
    }
}
