package com.huawei.internal.telephony.msim;

import android.content.Context;
import com.android.internal.telephony.Phone;
import com.huawei.android.util.NoExtAPIException;

public class MSimPhoneFactoryEx {
    public static Phone getPhone(int subscription) {
        throw new NoExtAPIException("method not supported.");
    }

    public static int getVoiceSubscription() {
        throw new NoExtAPIException("method not supported.");
    }

    public static void makeMultiSimDefaultPhones(Context context) {
        throw new NoExtAPIException("method not supported.");
    }

    public static int getDefaultSubscription() {
        throw new NoExtAPIException("method not supported.");
    }

    public static int getDataSubscription() {
        throw new NoExtAPIException("method not supported.");
    }

    public static int getSMSSubscription() {
        throw new NoExtAPIException("method not supported.");
    }

    public static boolean isPromptEnabled() {
        throw new NoExtAPIException("method not supported.");
    }

    public static Phone getDefaultPhone() {
        throw new NoExtAPIException("method not supported.");
    }
}
