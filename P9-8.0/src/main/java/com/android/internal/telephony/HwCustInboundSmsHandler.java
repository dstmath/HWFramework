package com.android.internal.telephony;

import android.content.Context;
import android.os.AsyncResult;
import android.telephony.SmsMessage;
import java.io.ByteArrayOutputStream;

public class HwCustInboundSmsHandler {
    public boolean dispatchMessageByDestPort(int destPort, SmsMessageBase sms, Context mContext) {
        return false;
    }

    public boolean isIQIEnable() {
        return false;
    }

    public void createIQClient(Context mContext) {
    }

    public boolean isIQISms(SmsMessage sms) {
        return false;
    }

    public boolean isIQIWapPush(ByteArrayOutputStream output) {
        return false;
    }

    public boolean isNotNotifyWappushEnabled(AsyncResult ar) {
        return false;
    }
}
