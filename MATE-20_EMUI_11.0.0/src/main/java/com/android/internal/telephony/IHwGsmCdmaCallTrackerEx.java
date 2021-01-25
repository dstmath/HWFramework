package com.android.internal.telephony;

import android.os.Handler;

public interface IHwGsmCdmaCallTrackerEx {
    default boolean notifyRegistrantsDelayed() {
        return false;
    }

    default void switchVoiceCallBackgroundState(int state) {
    }

    default void registerForLineControlInfo(Handler h, int what, Object obj) {
    }

    default void unregisterForLineControlInfo(Handler h) {
    }

    default void dispose() {
    }

    default void setConnEncryptCallByNumber(String number, boolean val) {
    }
}
