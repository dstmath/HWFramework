package com.android.internal.telephony;

import android.os.Handler;
import com.android.internal.telephony.SMSDispatcher.SmsTracker;

public abstract class AbstractSMSDispatcher extends Handler {
    protected void sendSmsOrigin(SmsTracker tracker) {
    }

    protected boolean sendSmsImmediately(SmsTracker tracker) {
        return true;
    }

    protected boolean isViaAndCdma() {
        return false;
    }

    protected void sendSmsSendingTimeOutMessageDelayed(SmsTracker tracker) {
    }
}
