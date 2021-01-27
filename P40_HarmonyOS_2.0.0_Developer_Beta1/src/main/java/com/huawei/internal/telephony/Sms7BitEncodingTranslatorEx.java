package com.huawei.internal.telephony;

import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.Sms7BitEncodingTranslator;

public class Sms7BitEncodingTranslatorEx {
    public static String translate(CharSequence message) {
        return Sms7BitEncodingTranslator.translate(message, useCdmaFormatForMoSms());
    }

    private static boolean useCdmaFormatForMoSms() {
        if (!SmsManager.getDefault().isImsSmsSupported()) {
            return TelephonyManager.getDefault().getCurrentPhoneType() == 2;
        }
        return "3gpp2".equals(SmsManager.getDefault().getImsSmsFormat());
    }
}
