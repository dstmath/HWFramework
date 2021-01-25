package com.android.internal.telephony.gsm;

import android.telephony.PhoneNumberUtils;
import android.text.format.Time;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.EncodeExceptionEx;
import com.huawei.internal.telephony.GsmAlphabetEx;
import com.huawei.internal.telephony.gsm.SmsMessageEx;
import com.huawei.internal.util.BitwiseOutputStreamEx;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public abstract class HwSmsMessage {
    static final String LOG_TAG = "HwSmsMessage";
    public static final int SMS_TOA_UNKNOWN = 128;

    public static SmsMessageEx.SubmitPduEx getDeliverPdu(String scAddress, String scTimeStamp, String origAddress, String message, byte[] UDH) {
        if (origAddress == null || message == null) {
            RlogEx.e("SmsMessage", "enter getDeliverPdu().");
            return null;
        }
        SmsMessageEx.SubmitPduEx ret = new SmsMessageEx.SubmitPduEx();
        ByteArrayOutputStream bo = new ByteArrayOutputStream(176);
        if (scAddress == null) {
            ret.setEncodedScAddress((byte[]) null);
        } else {
            ret.setEncodedScAddress(PhoneNumberUtils.networkPortionToCalledPartyBCDWithLength(scAddress));
        }
        bo.write((UDH != null ? 68 : 0) | 0);
        byte[] daBytes = PhoneNumberUtils.networkPortionToCalledPartyBCD(origAddress);
        if (daBytes == null) {
            bo.write(0);
            bo.write(SMS_TOA_UNKNOWN);
        } else {
            int i = 1;
            int length = (daBytes.length - 1) * 2;
            if ((daBytes[daBytes.length - 1] & 240) != 240) {
                i = 0;
            }
            bo.write(length - i);
            bo.write(daBytes, 0, daBytes.length);
        }
        bo.write(0);
        try {
            byte[] userData = GsmAlphabetEx.stringToGsm7BitPackedWithHeader(message, UDH);
            if ((userData[0] & 255) > 160) {
                RlogEx.e("SmsMessage", "Message too long");
                try {
                    bo.close();
                } catch (IOException e) {
                    RlogEx.e(LOG_TAG, "bo close error.");
                }
                return null;
            }
            bo.write(0);
            try {
                byte[] timeStampArray = encodeSCTimeStamp(scTimeStamp);
                bo.write(timeStampArray, 0, timeStampArray.length);
            } catch (BitwiseOutputStreamEx.AccessExceptionEx e2) {
                bo.write(0);
                bo.write(0);
                bo.write(0);
                bo.write(0);
                bo.write(0);
                bo.write(0);
                bo.write(0);
                RlogEx.e(LOG_TAG, "encode SC timestamp failed.");
            }
            bo.write(userData, 0, userData.length);
            try {
                bo.close();
            } catch (IOException e3) {
                RlogEx.e(LOG_TAG, "bo close error.");
            }
            ret.setEncodedMessage(bo.toByteArray());
            return ret;
        } catch (EncodeExceptionEx e4) {
            try {
                byte[] userData2 = SmsMessageEx.encodeUCS2Hw(message, UDH);
                if (userData2.length > 141) {
                    try {
                        bo.close();
                    } catch (IOException e5) {
                        RlogEx.e(LOG_TAG, "bo close error.");
                    }
                    return null;
                }
                bo.write(8);
                try {
                    byte[] timeStampArray2 = encodeSCTimeStamp(scTimeStamp);
                    bo.write(timeStampArray2, 0, timeStampArray2.length);
                } catch (BitwiseOutputStreamEx.AccessExceptionEx e6) {
                    bo.write(0);
                    bo.write(0);
                    bo.write(0);
                    bo.write(0);
                    bo.write(0);
                    bo.write(0);
                    bo.write(0);
                    RlogEx.e(LOG_TAG, "encode SC timestamp failed.");
                }
                bo.write(userData2, 0, userData2.length);
                bo.close();
            } catch (EncodeExceptionEx | UnsupportedEncodingException e7) {
                RlogEx.e(LOG_TAG, "Implausible UnsupportedEncodingException");
                try {
                    bo.close();
                } catch (IOException e8) {
                    RlogEx.e(LOG_TAG, "bo close error.");
                }
                return null;
            }
        } catch (Throwable e9) {
            try {
                bo.close();
            } catch (IOException e10) {
                RlogEx.e(LOG_TAG, "bo close error.");
            }
            throw e9;
        }
    }

    private static byte[] encodeSCTimeStamp(String mcTimeStamp) throws BitwiseOutputStreamEx.AccessExceptionEx {
        BitwiseOutputStreamEx outStream = new BitwiseOutputStreamEx(7);
        if (mcTimeStamp == null || mcTimeStamp.length() == 0) {
            RlogEx.e(LOG_TAG, "bad parm in encodeSCTimeStamp().");
            return outStream.toByteArray();
        }
        Time Date = new Time("UTC");
        try {
            Date.set(Long.parseLong(mcTimeStamp));
            Date.year %= 100;
            outStream.write(4, (byte) (Date.year % 10));
            outStream.write(4, (byte) (Date.year / 10));
            Date.month++;
            outStream.write(4, (byte) (Date.month % 10));
            outStream.write(4, (byte) (Date.month / 10));
            outStream.write(4, (byte) (Date.monthDay % 10));
            outStream.write(4, (byte) (Date.monthDay / 10));
            outStream.write(4, (byte) (Date.hour % 10));
            outStream.write(4, (byte) (Date.hour / 10));
            outStream.write(4, (byte) (Date.minute % 10));
            outStream.write(4, (byte) (Date.minute / 10));
            outStream.write(4, (byte) (Date.second % 10));
            outStream.write(4, (byte) (Date.second / 10));
            outStream.write(8, 0);
            return outStream.toByteArray();
        } catch (NumberFormatException e) {
            RlogEx.e(LOG_TAG, "encodeSCTimeStamp NumberFormatException.");
            return outStream.toByteArray();
        }
    }

    public static String getUserDataGSM8Bit(SmsMessageEx.PduParserEx p, int septetCount) {
        if (p == null) {
            return null;
        }
        try {
            String ret = GsmAlphabetEx.gsm8BitUnpackedToString(p.getPduHw(), p.getCurHw(), septetCount);
            p.setCurHw(p.getCurHw() + septetCount);
            return ret;
        } catch (Exception e) {
            return null;
        }
    }
}
