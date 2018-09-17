package com.android.internal.telephony;

public class SMSDispatcherUtils {
    public static int getMaxSendRetries() {
        return SMSDispatcher.getMaxSendRetriesHw();
    }

    public static int getEventSendRetry() {
        return SMSDispatcher.getEventSendRetryHw();
    }
}
