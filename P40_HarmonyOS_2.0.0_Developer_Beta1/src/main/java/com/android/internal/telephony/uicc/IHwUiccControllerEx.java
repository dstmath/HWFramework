package com.android.internal.telephony.uicc;

import android.os.Handler;
import android.os.Message;

public interface IHwUiccControllerEx {
    default void processRadioPowerDownIfNoCard() {
    }

    default void registerForFdnStatusChange(Handler h, int what, Object obj) {
    }

    default void unregisterForFdnStatusChange(Handler h) {
    }

    default void notifyFdnStatusChange() {
    }

    default void getUiccCardStatus(Message result, int slotId) {
    }
}
