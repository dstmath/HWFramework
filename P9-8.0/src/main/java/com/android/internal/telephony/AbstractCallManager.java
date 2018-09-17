package com.android.internal.telephony;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;

public abstract class AbstractCallManager {
    CallManagerReference mReference = HwTelephonyFactory.getHwPhoneManager().createHwCallManagerReference(this);

    public interface CallManagerReference {
        void calllNotifierDisconnectNotify(AsyncResult asyncResult);

        void cmdForEncryptedCall(Phone phone, int i, byte[] bArr);

        void disconnectNotify(Message message);

        int getActiveSubscription();

        void inCallScreenDisconnectNotify(AsyncResult asyncResult);

        void onSwitchToOtherActiveSub(Phone phone);

        void registerForEncryptedCall(Handler handler, int i, Object obj);

        void registerForPhoneStates(Phone phone);

        void registerForSubscriptionChange(Handler handler, int i, Object obj);

        void resultForKMCRemoteCmd(Phone phone, int i, int i2);

        void setActiveSubscription(int i);

        void setCallNotifierDisconnectCallback(disconnectCallback disconnectcallback);

        void setConnEncryptCallByNumber(Phone phone, String str, boolean z);

        void setInCallScreenDisconnectCallback(disconnectCallback disconnectcallback);

        void unregisterForEncryptedCall(Handler handler);

        void unregisterForPhoneStates(Phone phone);

        void unregisterForSubscriptionChange(Handler handler);
    }

    public interface disconnectCallback {
        void disconnectNotify(AsyncResult asyncResult);
    }

    public void setInCallScreenDisconnectCallback(disconnectCallback callback) {
        this.mReference.setInCallScreenDisconnectCallback(callback);
    }

    public void setCallNotifierDisconnectCallback(disconnectCallback callback) {
        this.mReference.setCallNotifierDisconnectCallback(callback);
    }

    public void inCallScreenDisconnectNotify(AsyncResult r) {
        this.mReference.inCallScreenDisconnectNotify(r);
    }

    public void calllNotifierDisconnectNotify(AsyncResult r) {
        this.mReference.calllNotifierDisconnectNotify(r);
    }

    public void disconnectNotify(Message msg) {
        this.mReference.disconnectNotify(msg);
    }

    public void setActiveSubscription(int subscription) {
        this.mReference.setActiveSubscription(subscription);
    }

    public int getActiveSubscription() {
        return this.mReference.getActiveSubscription();
    }

    protected void registerForPhoneStates(Phone phone) {
        this.mReference.registerForPhoneStates(phone);
    }

    protected void unregisterForPhoneStates(Phone phone) {
        this.mReference.unregisterForPhoneStates(phone);
    }

    public void onSwitchToOtherActiveSub(Phone currentPhone) {
        this.mReference.onSwitchToOtherActiveSub(currentPhone);
    }

    public void registerForSubscriptionChange(Handler h, int what, Object obj) {
        this.mReference.registerForSubscriptionChange(h, what, obj);
    }

    public void unregisterForSubscriptionChange(Handler h) {
        this.mReference.unregisterForSubscriptionChange(h);
    }

    public void resultForKMCRemoteCmd(Phone phone, int cmd, int reqData) {
        this.mReference.resultForKMCRemoteCmd(phone, cmd, reqData);
    }

    public void setConnEncryptCallByNumber(Phone phone, String number, boolean val) {
        this.mReference.setConnEncryptCallByNumber(phone, number, val);
    }

    public void cmdForEncryptedCall(Phone phone, int cmd, byte[] reqData) {
        this.mReference.cmdForEncryptedCall(phone, cmd, reqData);
    }

    public void registerForEncryptedCall(Handler h, int what, Object obj) {
        this.mReference.registerForEncryptedCall(h, what, obj);
    }

    public void unregisterForEncryptedCall(Handler h) {
        this.mReference.unregisterForEncryptedCall(h);
    }
}
