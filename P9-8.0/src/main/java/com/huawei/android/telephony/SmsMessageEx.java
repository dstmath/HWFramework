package com.huawei.android.telephony;

import android.telephony.SmsMessage;
import android.telephony.SmsMessage.SubmitPdu;
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

    public static SubmitPdu getSubmitPdu(String scAddress, String timeStamps, String destinationAddress, String message, byte[] UDH) {
        try {
            return HwTelephonyFactory.getHwInnerSmsManager().getSubmitPdu(scAddress, timeStamps, destinationAddress, message, UDH);
        } catch (Exception e) {
            Log.d("SmsMessageEx", "getSubmitPdu is error");
            return null;
        }
    }

    public static SubmitPdu getSubmitPdu(String scAddress, String timeStamps, String destinationAddress, String message, byte[] UDH, int subscription) {
        try {
            return HwTelephonyFactory.getHwInnerSmsManager().getSubmitPdu(scAddress, timeStamps, destinationAddress, message, UDH, subscription);
        } catch (Exception e) {
            Log.d("SmsMessageEx", "getSubmitPdu is error");
            return null;
        }
    }

    public static SubmitPdu getDeliverPdu(String scAddress, String scTimeStamp, String origAddress, String message, byte[] UDH) {
        return HwTelephonyFactory.getHwInnerSmsManager().getDeliverPdu(scAddress, scTimeStamp, origAddress, message, UDH);
    }

    public static SubmitPdu getDeliverPdu(String scAddress, String scTimeStamp, String origAddress, String message, byte[] UDH, int subscription) {
        return HwTelephonyFactory.getHwInnerSmsManager().getDeliverPdu(scAddress, scTimeStamp, origAddress, message, UDH, subscription);
    }

    public static SmsMessageEx createFromPdu(byte[] pdu) {
        return new SmsMessageEx(SmsMessage.createFromPdu(pdu));
    }

    public String getMessageBody() {
        return this.mSmsMessage == null ? null : this.mSmsMessage.getMessageBody();
    }

    public String getOriginatingAddress() {
        return this.mSmsMessage == null ? null : this.mSmsMessage.getOriginatingAddress();
    }

    public String getServiceCenterAddress() {
        return this.mSmsMessage == null ? null : this.mSmsMessage.getServiceCenterAddress();
    }

    public long getTimestampMillis() {
        return this.mSmsMessage == null ? 0 : this.mSmsMessage.getTimestampMillis();
    }

    public static boolean isWrappedSmsMessageValid(SmsMessage sms) {
        return sms.mWrappedSmsMessage != null;
    }

    public static boolean useCdmaFormatForMoSms() {
        return SmsMessage.useCdmaFormatForMoSms();
    }
}
