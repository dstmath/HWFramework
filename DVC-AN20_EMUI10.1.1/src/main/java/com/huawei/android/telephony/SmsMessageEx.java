package com.huawei.android.telephony;

import android.telephony.SmsMessage;
import android.util.Log;
import com.android.internal.telephony.HwTelephonyFactory;

public class SmsMessageEx {
    private SmsMessage mSmsMessage;

    private SmsMessageEx(SmsMessage smsMessage) {
        this.mSmsMessage = smsMessage;
    }

    public static void setSubId(SmsMessage obj, int subId) {
        obj.setSubId(subId);
    }

    public static int getSubId(SmsMessage obj) {
        return obj.getSubId();
    }

    public static int getMessageRefrenceNumber() {
        return HwTelephonyFactory.getHwInnerSmsManager().getMessageRefrenceNumber();
    }

    public static byte[] getUserDataHeaderForGsm(int seqNum, int maxNum, int MessageReferenceNum) {
        return HwTelephonyFactory.getHwInnerSmsManager().getUserDataHeaderForGsm(seqNum, maxNum, MessageReferenceNum);
    }

    public static SmsMessage.SubmitPdu getSubmitPdu(String scAddress, String timeStamps, String destinationAddress, String message, byte[] UDH) {
        try {
            return HwTelephonyFactory.getHwInnerSmsManager().getSubmitPdu(scAddress, timeStamps, destinationAddress, message, UDH);
        } catch (Exception e) {
            Log.d("SmsMessageEx", "getSubmitPdu is error");
            return null;
        }
    }

    public static SmsMessage.SubmitPdu getSubmitPdu(String scAddress, String timeStamps, String destinationAddress, String message, byte[] UDH, int subscription) {
        try {
            return HwTelephonyFactory.getHwInnerSmsManager().getSubmitPdu(scAddress, timeStamps, destinationAddress, message, UDH, subscription);
        } catch (Exception e) {
            Log.d("SmsMessageEx", "getSubmitPdu is error");
            return null;
        }
    }

    public static SmsMessage.SubmitPdu getDeliverPdu(String scAddress, String scTimeStamp, String origAddress, String message, byte[] UDH) {
        return HwTelephonyFactory.getHwInnerSmsManager().getDeliverPdu(scAddress, scTimeStamp, origAddress, message, UDH);
    }

    public static SmsMessage.SubmitPdu getDeliverPdu(String scAddress, String scTimeStamp, String origAddress, String message, byte[] UDH, int subscription) {
        return HwTelephonyFactory.getHwInnerSmsManager().getDeliverPdu(scAddress, scTimeStamp, origAddress, message, UDH, subscription);
    }

    public static SmsMessageEx createFromPdu(byte[] pdu) {
        return new SmsMessageEx(SmsMessage.createFromPdu(pdu));
    }

    public String getMessageBody() {
        SmsMessage smsMessage = this.mSmsMessage;
        if (smsMessage == null) {
            return null;
        }
        return smsMessage.getMessageBody();
    }

    public String getOriginatingAddress() {
        SmsMessage smsMessage = this.mSmsMessage;
        if (smsMessage == null) {
            return null;
        }
        return smsMessage.getOriginatingAddress();
    }

    public String getServiceCenterAddress() {
        SmsMessage smsMessage = this.mSmsMessage;
        if (smsMessage == null) {
            return null;
        }
        return smsMessage.getServiceCenterAddress();
    }

    public long getTimestampMillis() {
        SmsMessage smsMessage = this.mSmsMessage;
        if (smsMessage == null) {
            return 0;
        }
        return smsMessage.getTimestampMillis();
    }

    public static boolean isWrappedSmsMessageValid(SmsMessage sms) {
        return (sms == null || sms.mWrappedSmsMessage == null) ? false : true;
    }

    public static boolean useCdmaFormatForMoSms() {
        return SmsMessage.useCdmaFormatForMoSmsEx();
    }
}
