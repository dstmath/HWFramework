package com.android.internal.telephony.cdma.sms;

import com.huawei.internal.telephony.cdma.sms.BearerDataEx;
import com.huawei.internal.telephony.cdma.sms.UserDataEx;
import com.huawei.internal.util.BitwiseOutputStreamEx;

public class HwBearerData {
    private static final String LOG_TAG = "HwBearerData";

    public static boolean encode7bitMultiSms(UserDataEx uData, byte[] udhData, boolean force) {
        if (uData == null || udhData == null) {
            return false;
        }
        int udhSeptets = (((udhData.length + 1) * 8) + 6) / 7;
        uData.setMsgEncoding(2);
        uData.setMsgEncodingSet(true);
        try {
            uData.setPayload(BearerDataEx.encode7bitAsciiHw(uData.getPayloadStr(), udhSeptets, force));
            if (uData.getPayloadStr() != null) {
                uData.setNumFields(uData.getPayloadStr().length() + udhSeptets);
            }
            uData.setPayloadForFirstByte((byte) udhData.length);
            System.arraycopy(udhData, 0, uData.getPayload(), 1, udhData.length);
            return true;
        } catch (BearerDataEx.CodingExceptionEx e) {
            return false;
        }
    }

    public static void encodeMsgCenterTimeStampCheck(BearerDataEx bData, BitwiseOutputStreamEx outStream) throws BitwiseOutputStreamEx.AccessExceptionEx {
        if (bData != null && bData.isMsgCenterTimeStampExist() && outStream != null) {
            outStream.write(8, BearerDataEx.getSubparamMessageCenterTimeStamp());
            encodeMsgCenterTimeStamp(bData, outStream);
        }
    }

    private static void encodeMsgCenterTimeStamp(BearerDataEx bData, BitwiseOutputStreamEx outStream) throws BitwiseOutputStreamEx.AccessExceptionEx {
        outStream.write(8, 6);
        bData.setMsgCenterTimeStampYear(bData.getMsgCenterTimeStampYear() % 100);
        outStream.write(4, (byte) (bData.getMsgCenterTimeStampYear() / 10));
        outStream.write(4, (byte) (bData.getMsgCenterTimeStampYear() % 10));
        bData.setMsgCenterTimeStampMonth(bData.getMsgCenterTimeStampMonth() + 1);
        outStream.write(4, (byte) (bData.getMsgCenterTimeStampMonth() / 10));
        outStream.write(4, (byte) (bData.getMsgCenterTimeStampMonth() % 10));
        outStream.write(4, (byte) (bData.getMsgCenterTimeStampMonthDay() / 10));
        outStream.write(4, (byte) (bData.getMsgCenterTimeStampMonthDay() % 10));
        outStream.write(4, (byte) (bData.getMsgCenterTimeStampHour() / 10));
        outStream.write(4, (byte) (bData.getMsgCenterTimeStampHour() % 10));
        outStream.write(4, (byte) (bData.getMsgCenterTimeStampMinute() / 10));
        outStream.write(4, (byte) (bData.getMsgCenterTimeStampMinute() % 10));
        outStream.write(4, (byte) (bData.getMsgCenterTimeStampSecond() / 10));
        outStream.write(4, (byte) (bData.getMsgCenterTimeStampSecond() % 10));
    }
}
