package com.android.internal.telephony;

import com.huawei.internal.telephony.SmsAddressEx;
import com.huawei.internal.telephony.SmsMessageBaseEx;

public class SmsMessageBaseUtils {
    public static String getMessageBody(SmsMessageBaseEx smb) {
        if (smb != null) {
            return smb.getMessageBody();
        }
        return null;
    }

    public static void setOriginatingAddress(SmsMessageBaseEx smb, SmsAddressEx smsOriginatingAddress) {
        if (smb != null) {
            smb.setOriginatingAddress(smsOriginatingAddress);
        }
    }

    public static void setPdu(SmsMessageBaseEx smb, byte[] pdu) {
        if (smb != null) {
            smb.setPdu(pdu);
        }
    }

    public static byte[] getUserData(SmsMessageBaseEx smb) {
        if (smb != null) {
            return smb.getUserData();
        }
        return null;
    }
}
