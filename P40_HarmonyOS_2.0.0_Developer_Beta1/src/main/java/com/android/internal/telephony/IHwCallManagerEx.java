package com.android.internal.telephony;

import android.os.Handler;
import com.huawei.internal.telephony.PhoneExt;

public interface IHwCallManagerEx {
    default int getActiveSubscription() {
        return -1;
    }

    default void setActiveSubscription(int subscription) {
    }

    default void registerForPhoneStatesHw(PhoneExt phone) {
    }

    default void unregisterForPhoneStatesHw(PhoneExt phone) {
    }

    default void onSwitchToOtherActiveSub(PhoneExt phone) {
    }

    default void registerForSubscriptionChange(Handler h, int what, Object obj) {
    }

    default void unregisterForSubscriptionChange(Handler h) {
    }

    default void resultForKMCRemoteCmd(PhoneExt phone, int cmd, int reqData) {
    }

    default void setConnEncryptCallByNumber(PhoneExt phone, String number, boolean val) {
    }

    default void cmdForEncryptedCall(PhoneExt phone, int cmd, byte[] reqData) {
    }

    default void registerForEncryptedCall(Handler h, int what, Object obj) {
    }

    default void unregisterForEncryptedCall(Handler h) {
    }
}
