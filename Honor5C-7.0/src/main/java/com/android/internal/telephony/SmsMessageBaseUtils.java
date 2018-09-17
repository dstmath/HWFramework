package com.android.internal.telephony;

public class SmsMessageBaseUtils {
    public static String getMessageBody(SmsMessageBase smb) {
        if (smb != null) {
            return smb.mMessageBody;
        }
        return null;
    }

    public static void setOriginatingAddress(SmsMessageBase smb, SmsAddress smsOriginatingAddress) {
        if (smb != null) {
            smb.mOriginatingAddress = smsOriginatingAddress;
        }
    }

    public static void setPdu(SmsMessageBase smb, byte[] pdu) {
        if (smb != null) {
            smb.mPdu = pdu;
        }
    }

    public static byte[] getUserData(SmsMessageBase smb) {
        if (smb != null) {
            return smb.mUserData;
        }
        return null;
    }
}
