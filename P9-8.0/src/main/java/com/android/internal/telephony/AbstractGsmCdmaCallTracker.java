package com.android.internal.telephony;

import android.os.Handler;

public abstract class AbstractGsmCdmaCallTracker extends CallTracker {
    CdmaCallTrackerReference mCdmaReference = HwTelephonyFactory.getHwPhoneManager().createHwCdmaCallTrackerReference(this);
    GsmCallTrackerReference mGsmReference = HwTelephonyFactory.getHwPhoneManager().createHwGsmCallTrackerReference(this);
    private GsmCdmaPhone mPhone;

    public interface CdmaCallTrackerReference {
        void dispose();

        boolean notifyRegistrantsDelayed();

        void registerForLineControlInfo(Handler handler, int i, Object obj);

        void setConnEncryptCallByNumber(String str, boolean z);

        void switchVoiceCallBackgroundState(int i);

        void unregisterForLineControlInfo(Handler handler);
    }

    public interface GsmCallTrackerReference {
        boolean notifyRegistrantsDelayed();

        void switchVoiceCallBackgroundState(int i);
    }

    protected AbstractGsmCdmaCallTracker(GsmCdmaPhone phone) {
        this.mPhone = phone;
    }

    public void registerForLineControlInfo(Handler h, int what, Object obj) {
        this.mCdmaReference.registerForLineControlInfo(h, what, obj);
    }

    public void unregisterForLineControlInfo(Handler h) {
        this.mCdmaReference.unregisterForLineControlInfo(h);
    }

    public void dispose() {
        this.mCdmaReference.dispose();
    }

    public boolean notifyRegistrantsDelayed() {
        if (this.mPhone.isPhoneTypeGsm()) {
            return this.mGsmReference.notifyRegistrantsDelayed();
        }
        return this.mCdmaReference.notifyRegistrantsDelayed();
    }

    public void switchVoiceCallBackgroundState(int state) {
        if (this.mPhone.isPhoneTypeGsm()) {
            this.mGsmReference.switchVoiceCallBackgroundState(state);
        } else {
            this.mCdmaReference.switchVoiceCallBackgroundState(state);
        }
    }

    public void setConnEncryptCallByNumber(String number, boolean val) {
        this.mCdmaReference.setConnEncryptCallByNumber(number, val);
    }
}
