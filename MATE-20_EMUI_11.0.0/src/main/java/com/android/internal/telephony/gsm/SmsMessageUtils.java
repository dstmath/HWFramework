package com.android.internal.telephony.gsm;

import com.huawei.internal.telephony.gsm.SmsMessageEx;

public class SmsMessageUtils {
    public void setProtocolIdentifier(SmsMessageEx smsMessage, int value) {
        if (smsMessage != null) {
            smsMessage.setProtocolIdentifierHw(value);
        }
    }

    public void setDataCodingScheme(SmsMessageEx smsMessage, int value) {
        if (smsMessage != null) {
            smsMessage.setDataCodingSchemeHw(value);
        }
    }

    public void parseUserData(SmsMessageEx smsMessage, SmsMessageEx.PduParserEx p, boolean hasUserDataHeader) {
        if (smsMessage != null) {
            smsMessage.parseUserDataHw(p, hasUserDataHeader);
        }
    }
}
