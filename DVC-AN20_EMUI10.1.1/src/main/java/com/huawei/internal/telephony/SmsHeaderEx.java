package com.huawei.internal.telephony;

import com.android.internal.telephony.SmsHeader;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class SmsHeaderEx {
    private SmsHeader mSmsHeader;

    public SmsHeaderEx() {
    }

    private SmsHeaderEx(SmsHeader smsHeader) {
        this.mSmsHeader = smsHeader;
    }

    public static SmsHeaderEx fromByteArray(byte[] data) {
        SmsHeader smsHeader = null;
        if (data != null) {
            smsHeader = SmsHeader.fromByteArray(data);
        }
        return new SmsHeaderEx(smsHeader);
    }

    public void setSmsHeader(SmsHeader smsHeader) {
        this.mSmsHeader = smsHeader;
    }

    public SmsHeader getSmsHeader() {
        return this.mSmsHeader;
    }
}
