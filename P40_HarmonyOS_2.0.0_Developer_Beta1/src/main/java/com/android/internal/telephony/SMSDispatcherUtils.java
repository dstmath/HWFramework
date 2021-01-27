package com.android.internal.telephony;

import com.huawei.internal.telephony.SMSDispatcherEx;

public class SMSDispatcherUtils {
    public static int getMaxSendRetries() {
        return SMSDispatcherEx.getMaxSendRetriesHw();
    }

    public static int getEventSendRetry() {
        return SMSDispatcherEx.getEventSendRetryHw();
    }
}
