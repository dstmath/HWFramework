package com.huawei.internal.telephony;

import com.android.internal.telephony.Sms7BitEncodingTranslator;

public class Sms7BitEncodingTranslatorEx {
    public static String translate(CharSequence message) {
        return Sms7BitEncodingTranslator.translate(message);
    }
}
