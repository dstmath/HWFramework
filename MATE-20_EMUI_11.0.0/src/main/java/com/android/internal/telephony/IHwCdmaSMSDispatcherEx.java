package com.android.internal.telephony;

import android.os.Message;
import com.huawei.internal.telephony.SMSDispatcherEx;

public interface IHwCdmaSMSDispatcherEx {
    default boolean isNeedToSendSms(SMSDispatcherEx.SmsTrackerEx tracker) {
        return true;
    }

    default void handleSendSmsAfter(SMSDispatcherEx.SmsTrackerEx tracker) {
    }

    default boolean isCdmaIms(SMSDispatcherEx.SmsTrackerEx tracker) {
        return false;
    }

    default boolean sendSmsImmediately(SMSDispatcherEx.SmsTrackerEx tracker) {
        return false;
    }

    default boolean isViaAndCdma() {
        return false;
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
