package com.android.internal.telephony.util;

import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.gsm.SmsMessage;

public final class SMSDispatcherUtil {
    private SMSDispatcherUtil() {
    }

    public static SmsMessageBase.SubmitPduBase getSubmitPdu(boolean isCdma, String scAddr, String destAddr, String message, boolean statusReportRequested, SmsHeader smsHeader) {
        if (isCdma) {
            return getSubmitPduCdma(scAddr, destAddr, message, statusReportRequested, smsHeader);
        }
        return getSubmitPduGsm(scAddr, destAddr, message, statusReportRequested);
    }

    public static SmsMessageBase.SubmitPduBase getSubmitPdu(boolean isCdma, String scAddr, String destAddr, String message, boolean statusReportRequested, SmsHeader smsHeader, int priority, int validityPeriod) {
        if (isCdma) {
            return getSubmitPduCdma(scAddr, destAddr, message, statusReportRequested, smsHeader, priority);
        }
        return getSubmitPduGsm(scAddr, destAddr, message, statusReportRequested, validityPeriod);
    }

    public static SmsMessageBase.SubmitPduBase getSubmitPduGsm(String scAddr, String destAddr, String message, boolean statusReportRequested) {
        return SmsMessage.getSubmitPdu(scAddr, destAddr, message, statusReportRequested);
    }

    public static SmsMessageBase.SubmitPduBase getSubmitPduGsm(String scAddr, String destAddr, String message, boolean statusReportRequested, int validityPeriod) {
        return SmsMessage.getSubmitPdu(scAddr, destAddr, message, statusReportRequested, validityPeriod);
    }

    public static SmsMessageBase.SubmitPduBase getSubmitPduCdma(String scAddr, String destAddr, String message, boolean statusReportRequested, SmsHeader smsHeader) {
        return com.android.internal.telephony.cdma.SmsMessage.getSubmitPdu(scAddr, destAddr, message, statusReportRequested, smsHeader);
    }

    public static SmsMessageBase.SubmitPduBase getSubmitPduCdma(String scAddr, String destAddr, String message, boolean statusReportRequested, SmsHeader smsHeader, int priority) {
        return com.android.internal.telephony.cdma.SmsMessage.getSubmitPdu(scAddr, destAddr, message, statusReportRequested, smsHeader, priority);
    }

    public static SmsMessageBase.SubmitPduBase getSubmitPdu(boolean isCdma, String scAddr, String destAddr, int destPort, byte[] message, boolean statusReportRequested) {
        if (isCdma) {
            return getSubmitPduCdma(scAddr, destAddr, destPort, message, statusReportRequested);
        }
        return getSubmitPduGsm(scAddr, destAddr, destPort, message, statusReportRequested);
    }

    public static SmsMessageBase.SubmitPduBase getSubmitPduCdma(String scAddr, String destAddr, int destPort, byte[] message, boolean statusReportRequested) {
        return com.android.internal.telephony.cdma.SmsMessage.getSubmitPdu(scAddr, destAddr, destPort, message, statusReportRequested);
    }

    public static SmsMessageBase.SubmitPduBase getSubmitPduGsm(String scAddr, String destAddr, int destPort, byte[] message, boolean statusReportRequested) {
        return SmsMessage.getSubmitPdu(scAddr, destAddr, destPort, message, statusReportRequested);
    }

    public static GsmAlphabet.TextEncodingDetails calculateLength(boolean isCdma, CharSequence messageBody, boolean use7bitOnly) {
        if (isCdma) {
            return calculateLengthCdma(messageBody, use7bitOnly);
        }
        return calculateLengthGsm(messageBody, use7bitOnly);
    }

    public static GsmAlphabet.TextEncodingDetails calculateLengthGsm(CharSequence messageBody, boolean use7bitOnly) {
        return SmsMessage.calculateLength(messageBody, use7bitOnly);
    }

    public static GsmAlphabet.TextEncodingDetails calculateLengthCdma(CharSequence messageBody, boolean use7bitOnly) {
        return com.android.internal.telephony.cdma.SmsMessage.calculateLength(messageBody, use7bitOnly, false);
    }
}
