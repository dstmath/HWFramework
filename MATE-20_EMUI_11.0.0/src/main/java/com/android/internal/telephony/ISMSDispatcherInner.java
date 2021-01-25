package com.android.internal.telephony;

import com.huawei.internal.telephony.SMSDispatcherEx;

public interface ISMSDispatcherInner {
    SMSDispatcherEx getSmsDispatcherEx();

    boolean isIms();

    void sendSms(SMSDispatcherEx.SmsTrackerEx smsTrackerEx);
}
