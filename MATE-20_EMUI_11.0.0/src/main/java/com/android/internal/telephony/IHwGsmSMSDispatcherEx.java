package com.android.internal.telephony;

import android.os.Message;
import com.huawei.internal.telephony.SMSDispatcherEx;

public interface IHwGsmSMSDispatcherEx {
    default boolean isNeedToSendSms(SMSDispatcherEx.SmsTrackerEx tracker) {
        return true;
    }

    default void handleSendSmsAfter(SMSDispatcherEx.SmsTrackerEx tracker) {
    }

    default boolean checkCustIgnoreShortCodeTips() {
        return false;
    }

    default boolean sendSmsImmediately(SMSDispatcherEx.SmsTrackerEx tracker) {
        return true;
    }

    default void sendSmsSendingTimeOutMessageDelayed(SMSDispatcherEx.SmsTrackerEx tracker) {
    }

    default void handleSmsSendingTimeout(Message msg) {
    }

    default void handleSendSmsReTry(SMSDispatcherEx.SmsTrackerEx trackerEx) {
    }

    default void removeTimeoutEvent() {
    }
}
