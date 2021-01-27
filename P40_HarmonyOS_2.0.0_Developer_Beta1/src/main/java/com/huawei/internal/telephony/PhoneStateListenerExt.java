package com.huawei.internal.telephony;

import android.os.Looper;
import android.telephony.PhoneStateListener;
import com.huawei.annotation.HwSystemApi;
import huawei.telephony.PreciseCallStateExt;

@HwSystemApi
public class PhoneStateListenerExt {
    public static final int LISTEN_PRECISE_CALL_STATE = 2048;
    public static final int LISTEN_RADIO_POWER_STATE_CHANGED = 8388608;
    private static final String LOG_TAG = "PhoneStateListenerEx";
    private PhoneStateListenerBridge mPhoneStateListener;
    protected int mSubscription;

    public PhoneStateListenerExt() {
        this.mPhoneStateListener = new PhoneStateListenerBridge();
        this.mPhoneStateListener.setPhoneStateListenerExt(this);
    }

    public PhoneStateListenerExt(int subscription) {
        this.mPhoneStateListener = new PhoneStateListenerBridge(subscription);
        this.mSubscription = subscription;
        this.mPhoneStateListener.setPhoneStateListenerExt(this);
    }

    public PhoneStateListenerExt(int subId, Looper looper) {
        this.mPhoneStateListener = new PhoneStateListenerBridge(subId, looper);
        this.mSubscription = subId;
        this.mPhoneStateListener.setPhoneStateListenerExt(this);
    }

    public void onPreciseCallStateChangedHw(PreciseCallStateExt callState) {
    }

    public PhoneStateListener getPhoneStateListener() {
        return this.mPhoneStateListener;
    }

    public void onCallStateChanged(int state, String incomingNumber) {
    }

    public void onRadioPowerStateChanged(int state) {
    }
}
