package com.huawei.android.telephony;

import android.os.Bundle;
import android.telephony.SmsInterceptionListener;

public class SmsInterceptionListenerEx extends SmsInterceptionListener {
    public int handleSmsDeliverAction(Bundle smsInfo) {
        return 0;
    }

    public int handleWapPushDeliverAction(Bundle wapPushInfo) {
        return 0;
    }

    public boolean sendNumberBlockedRecord(Bundle smsInfo) {
        return false;
    }
}
