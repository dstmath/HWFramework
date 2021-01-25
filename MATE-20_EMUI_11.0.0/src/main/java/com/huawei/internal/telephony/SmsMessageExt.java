package com.huawei.internal.telephony;

import android.telephony.SmsMessage;
import com.huawei.internal.telephony.SmsMessageBaseEx;

public class SmsMessageExt {
    private SmsMessage mSmsMessage;

    public SmsMessageExt(SmsMessage smsMessage) {
        this.mSmsMessage = smsMessage;
    }

    public static SmsMessage.SubmitPdu getSubmitPdu(SmsMessageBaseEx.SubmitPduBaseEx spbEx) {
        if (spbEx != null) {
            return new SmsMessage.SubmitPdu(spbEx.getSubmitPduBase());
        }
        return null;
    }

    public boolean isSmsBlackFlag() {
        SmsMessage smsMessage = this.mSmsMessage;
        if (smsMessage == null || smsMessage.mWrappedSmsMessage == null) {
            return false;
        }
        return this.mSmsMessage.mWrappedSmsMessage.isBlacklistFlag();
    }
}
