package com.huawei.internal.telephony.msim;

import android.os.Handler;
import com.huawei.android.util.NoExtAPIException;

public class CardSubscriptionManagerEx extends Handler {
    public static SubscriptionDataEx getCardSubscriptions(int cardIndex) {
        throw new NoExtAPIException("method not supported.");
    }

    public static void registerForSimStateChanged(Handler h, int what, Object obj) {
        throw new NoExtAPIException("method not supported.");
    }

    public static void unRegisterForSimStateChanged(Handler h) {
        throw new NoExtAPIException("method not supported.");
    }
}
