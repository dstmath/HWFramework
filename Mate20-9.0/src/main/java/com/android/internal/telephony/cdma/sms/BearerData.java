package com.android.internal.telephony.cdma.sms;

import android.common.HwFrameworkFactory;
import android.content.res.Resources;
import android.telephony.Rlog;
import android.telephony.SmsCbCmasInfo;
import android.telephony.cdma.CdmaSmsCbProgramData;
import android.telephony.cdma.CdmaSmsCbProgramResults;
import android.text.format.Time;
import com.android.internal.midi.MidiConstants;
import com.android.internal.telephony.EncodeException;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.gsm.SmsMessage;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.util.BitwiseInputStream;
import com.android.internal.util.BitwiseOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TimeZone;

public final class BearerData {
    public static final int ALERT_DEFAULT = 0;
    public static final int ALERT_HIGH_PRIO = 3;
    public static final int ALERT_LOW_PRIO = 1;
    public static final int ALERT_MEDIUM_PRIO = 2;
    public static final int DISPLAY_MODE_DEFAULT = 1;
    public static final int DISPLAY_MODE_IMMEDIATE = 0;
    public static final int DISPLAY_MODE_USER = 2;
    public static final int ERROR_NONE = 0;
    public static final int ERROR_PERMANENT = 3;
    public static final int ERROR_TEMPORARY = 2;
    public static final int ERROR_UNDEFINED = 255;
    public static final int LANGUAGE_CHINESE = 6;
    public static final int LANGUAGE_ENGLISH = 1;
    public static final int LANGUAGE_FRENCH = 2;
    public static final int LANGUAGE_HEBREW = 7;
    public static final int LANGUAGE_JAPANESE = 4;
    public static final int LANGUAGE_KOREAN = 5;
    public static final int LANGUAGE_SPANISH = 3;
    public static final int LANGUAGE_UNKNOWN = 0;
    private static final String LOG_TAG = "BearerData";
    public static final int MESSAGE_TYPE_CANCELLATION = 3;
    public static final int MESSAGE_TYPE_DELIVER = 1;
    public static final int MESSAGE_TYPE_DELIVERY_ACK = 4;
    public static final int MESSAGE_TYPE_DELIVER_REPORT = 7;
    public static final int MESSAGE_TYPE_READ_ACK = 6;
    public static final int MESSAGE_TYPE_SUBMIT = 2;
    public static final int MESSAGE_TYPE_SUBMIT_REPORT = 8;
    public static final int MESSAGE_TYPE_USER_ACK = 5;
    public static final int PRIORITY_EMERGENCY = 3;
    public static final int PRIORITY_INTERACTIVE = 1;
    public static final int PRIORITY_NORMAL = 0;
    public static final int PRIORITY_URGENT = 2;
    public static final int PRIVACY_CONFIDENTIAL = 2;
    public static final int PRIVACY_NOT_RESTRICTED = 0;
    public static final int PRIVACY_RESTRICTED = 1;
    public static final int PRIVACY_SECRET = 3;
    public static final int RELATIVE_TIME_DAYS_LIMIT = 196;
    public static final int RELATIVE_TIME_HOURS_LIMIT = 167;
    public static final int RELATIVE_TIME_INDEFINITE = 245;
    public static final int RELATIVE_TIME_MINS_LIMIT = 143;
    public static final int RELATIVE_TIME_MOBILE_INACTIVE = 247;
    public static final int RELATIVE_TIME_NOW = 246;
    public static final int RELATIVE_TIME_RESERVED = 248;
    public static final int RELATIVE_TIME_WEEKS_LIMIT = 244;
    public static final int STATUS_ACCEPTED = 0;
    public static final int STATUS_BLOCKED_DESTINATION = 7;
    public static final int STATUS_CANCELLED = 3;
    public static final int STATUS_CANCEL_FAILED = 6;
    public static final int STATUS_DELIVERED = 2;
    public static final int STATUS_DEPOSITED_TO_INTERNET = 1;
    public static final int STATUS_DUPLICATE_MESSAGE = 9;
    public static final int STATUS_INVALID_DESTINATION = 10;
    public static final int STATUS_MESSAGE_EXPIRED = 13;
    public static final int STATUS_NETWORK_CONGESTION = 4;
    public static final int STATUS_NETWORK_ERROR = 5;
    public static final int STATUS_TEXT_TOO_LONG = 8;
    public static final int STATUS_UNDEFINED = 255;
    public static final int STATUS_UNKNOWN_ERROR = 31;
    private static final byte SUBPARAM_ALERT_ON_MESSAGE_DELIVERY = 12;
    private static final byte SUBPARAM_CALLBACK_NUMBER = 14;
    private static final byte SUBPARAM_DEFERRED_DELIVERY_TIME_ABSOLUTE = 6;
    private static final byte SUBPARAM_DEFERRED_DELIVERY_TIME_RELATIVE = 7;
    private static final byte SUBPARAM_ID_LAST_DEFINED = 23;
    private static final byte SUBPARAM_LANGUAGE_INDICATOR = 13;
    private static final byte SUBPARAM_MESSAGE_CENTER_TIME_STAMP = 3;
    private static final byte SUBPARAM_MESSAGE_DEPOSIT_INDEX = 17;
    private static final byte SUBPARAM_MESSAGE_DISPLAY_MODE = 15;
    private static final byte SUBPARAM_MESSAGE_IDENTIFIER = 0;
    private static final byte SUBPARAM_MESSAGE_STATUS = 20;
    private static final byte SUBPARAM_NUMBER_OF_MESSAGES = 11;
    private static final byte SUBPARAM_PRIORITY_INDICATOR = 8;
    private static final byte SUBPARAM_PRIVACY_INDICATOR = 9;
    private static final byte SUBPARAM_REPLY_OPTION = 10;
    private static final byte SUBPARAM_SERVICE_CATEGORY_PROGRAM_DATA = 18;
    private static final byte SUBPARAM_SERVICE_CATEGORY_PROGRAM_RESULTS = 19;
    private static final byte SUBPARAM_USER_DATA = 1;
    private static final byte SUBPARAM_USER_RESPONSE_CODE = 2;
    private static final byte SUBPARAM_VALIDITY_PERIOD_ABSOLUTE = 4;
    private static final byte SUBPARAM_VALIDITY_PERIOD_RELATIVE = 5;
    public int alert = 0;
    public boolean alertIndicatorSet = false;
    public CdmaSmsAddress callbackNumber;
    public SmsCbCmasInfo cmasWarningInfo;
    public TimeStamp deferredDeliveryTimeAbsolute;
    public int deferredDeliveryTimeRelative;
    public boolean deferredDeliveryTimeRelativeSet;
    public boolean deliveryAckReq;
    public int depositIndex;
    public int displayMode = 1;
    public boolean displayModeSet = false;
    public int errorClass = 255;
    public boolean hasUserDataHeader;
    public int language = 0;
    public boolean languageIndicatorSet = false;
    public int messageId;
    public int messageStatus = 255;
    public boolean messageStatusSet = false;
    public int messageType;
    public TimeStamp msgCenterTimeStamp;
    public int numberOfMessages;
    public int priority = 0;
    public boolean priorityIndicatorSet = false;
    public int privacy = 0;
    public boolean privacyIndicatorSet = false;
    public boolean readAckReq;
    public boolean reportReq;
    public ArrayList<CdmaSmsCbProgramData> serviceCategoryProgramData;
    public ArrayList<CdmaSmsCbProgramResults> serviceCategoryProgramResults;
    public boolean userAckReq;
    public UserData userData;
    public int userResponseCode;
    public boolean userResponseCodeSet = false;
    public TimeStamp validityPeriodAbsolute;
    public int validityPeriodRelative;
    public boolean validityPeriodRelativeSet;

    public static class CodingException extends Exception {
        public CodingException(String s) {
            super(s);
        }
    }

    private static class Gsm7bitCodingResult {
        byte[] data;
        int septets;

        private Gsm7bitCodingResult() {
        }
    }

    public static class TimeStamp extends Time {
        public TimeStamp() {
            super(TimeZone.getDefault().getID());
        }

        public static TimeStamp fromByteArray(byte[] data) {
            TimeStamp ts = new TimeStamp();
            int year = IccUtils.cdmaBcdByteToInt(data[0]);
            if (year > 99 || year < 0) {
                return null;
            }
            ts.year = year >= 96 ? year + 1900 : year + 2000;
            int month = IccUtils.cdmaBcdByteToInt(data[1]);
            if (month < 1 || month > 12) {
                return null;
            }
            ts.month = month - 1;
            int day = IccUtils.cdmaBcdByteToInt(data[2]);
            if (day < 1 || day > 31) {
                return null;
            }
            ts.monthDay = day;
            int hour = IccUtils.cdmaBcdByteToInt(data[3]);
            if (hour < 0 || hour > 23) {
                return null;
            }
            ts.hour = hour;
            int minute = IccUtils.cdmaBcdByteToInt(data[4]);
            if (minute < 0 || minute > 59) {
                return null;
            }
            ts.minute = minute;
            int second = IccUtils.cdmaBcdByteToInt(data[5]);
            if (second < 0 || second > 59) {
                return null;
            }
            ts.second = second;
            return ts;
        }

        public String toString() {
            return "TimeStamp " + ("{ year=" + this.year) + (", month=" + this.month) + (", day=" + this.monthDay) + (", hour=" + this.hour) + (", minute=" + this.minute) + (", second=" + this.second) + " }";
        }
    }

    public String getLanguage() {
        return getLanguageCodeForValue(this.language);
    }

    private static String getLanguageCodeForValue(int languageValue) {
        switch (languageValue) {
            case 1:
                return "en";
            case 2:
                return "fr";
            case 3:
                return "es";
            case 4:
                return "ja";
            case 5:
                return "ko";
            case 6:
                return "zh";
            case 7:
                return "he";
            default:
                return null;
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BearerData ");
        builder.append("{ messageType=" + this.messageType);
        builder.append(", messageId=" + this.messageId);
        StringBuilder sb = new StringBuilder();
        sb.append(", priority=");
        sb.append(this.priorityIndicatorSet ? Integer.valueOf(this.priority) : "unset");
        builder.append(sb.toString());
        StringBuilder sb2 = new StringBuilder();
        sb2.append(", privacy=");
        sb2.append(this.privacyIndicatorSet ? Integer.valueOf(this.privacy) : "unset");
        builder.append(sb2.toString());
        StringBuilder sb3 = new StringBuilder();
        sb3.append(", alert=");
        sb3.append(this.alertIndicatorSet ? Integer.valueOf(this.alert) : "unset");
        builder.append(sb3.toString());
        StringBuilder sb4 = new StringBuilder();
        sb4.append(", displayMode=");
        sb4.append(this.displayModeSet ? Integer.valueOf(this.displayMode) : "unset");
        builder.append(sb4.toString());
        StringBuilder sb5 = new StringBuilder();
        sb5.append(", language=");
        sb5.append(this.languageIndicatorSet ? Integer.valueOf(this.language) : "unset");
        builder.append(sb5.toString());
        StringBuilder sb6 = new StringBuilder();
        sb6.append(", errorClass=");
        sb6.append(this.messageStatusSet ? Integer.valueOf(this.errorClass) : "unset");
        builder.append(sb6.toString());
        StringBuilder sb7 = new StringBuilder();
        sb7.append(", msgStatus=");
        sb7.append(this.messageStatusSet ? Integer.valueOf(this.messageStatus) : "unset");
        builder.append(sb7.toString());
        StringBuilder sb8 = new StringBuilder();
        sb8.append(", msgCenterTimeStamp=");
        sb8.append(this.msgCenterTimeStamp != null ? this.msgCenterTimeStamp : "unset");
        builder.append(sb8.toString());
        StringBuilder sb9 = new StringBuilder();
        sb9.append(", validityPeriodAbsolute=");
        sb9.append(this.validityPeriodAbsolute != null ? this.validityPeriodAbsolute : "unset");
        builder.append(sb9.toString());
        StringBuilder sb10 = new StringBuilder();
        sb10.append(", validityPeriodRelative=");
        sb10.append(this.validityPeriodRelativeSet ? Integer.valueOf(this.validityPeriodRelative) : "unset");
        builder.append(sb10.toString());
        StringBuilder sb11 = new StringBuilder();
        sb11.append(", deferredDeliveryTimeAbsolute=");
        sb11.append(this.deferredDeliveryTimeAbsolute != null ? this.deferredDeliveryTimeAbsolute : "unset");
        builder.append(sb11.toString());
        StringBuilder sb12 = new StringBuilder();
        sb12.append(", deferredDeliveryTimeRelative=");
        sb12.append(this.deferredDeliveryTimeRelativeSet ? Integer.valueOf(this.deferredDeliveryTimeRelative) : "unset");
        builder.append(sb12.toString());
        builder.append(", userAckReq=" + this.userAckReq);
        builder.append(", deliveryAckReq=" + this.deliveryAckReq);
        builder.append(", readAckReq=" + this.readAckReq);
        builder.append(", reportReq=" + this.reportReq);
        builder.append(", numberOfMessages=" + this.numberOfMessages);
        builder.append(", callbackNumber=" + Rlog.pii(LOG_TAG, this.callbackNumber));
        builder.append(", depositIndex=" + this.depositIndex);
        builder.append(", hasUserDataHeader=" + this.hasUserDataHeader);
        builder.append(", userData=" + this.userData);
        builder.append(" }");
        return builder.toString();
    }

    private static void encodeMessageId(BearerData bData, BitwiseOutputStream outStream) throws BitwiseOutputStream.AccessException {
        outStream.write(8, 3);
        outStream.write(4, bData.messageType);
        outStream.write(8, bData.messageId >> 8);
        outStream.write(8, bData.messageId);
        outStream.write(1, bData.hasUserDataHeader ? 1 : 0);
        outStream.skip(3);
    }

    private static int countAsciiSeptets(CharSequence msg, boolean force) {
        int msgLen = msg.length();
        if (force) {
            return msgLen;
        }
        for (int i = 0; i < msgLen; i++) {
            if (UserData.charToAscii.get(msg.charAt(i), -1) == -1) {
                return -1;
            }
        }
        return msgLen;
    }

    public static GsmAlphabet.TextEncodingDetails calcTextEncodingDetails(CharSequence msg, boolean force7BitEncoding, boolean isEntireMsg) {
        GsmAlphabet.TextEncodingDetails ted;
        int septets = countAsciiSeptets(msg, force7BitEncoding);
        if (septets == -1 || septets > 160) {
            ted = SmsMessage.calculateLength(msg, force7BitEncoding);
            if (ted.msgCount == 1 && ted.codeUnitSize == 1 && isEntireMsg) {
                return SmsMessageBase.calcUnicodeEncodingDetails(msg);
            }
        } else {
            ted = new GsmAlphabet.TextEncodingDetails();
            ted.msgCount = 1;
            ted.codeUnitCount = septets;
            ted.codeUnitsRemaining = 160 - septets;
            ted.codeUnitSize = 1;
        }
        return ted;
    }

    public static byte[] encode7bitAscii(String msg, int septetOffset, boolean force) throws CodingException {
        try {
            BitwiseOutputStream outStream = new BitwiseOutputStream(msg.length() + septetOffset);
            int msgLen = msg.length();
            for (int i = 0; i < septetOffset; i++) {
                outStream.write(7, 0);
            }
            for (int i2 = 0; i2 < msgLen; i2++) {
                int charCode = UserData.charToAscii.get(msg.charAt(i2), -1);
                if (charCode != -1) {
                    outStream.write(7, charCode);
                } else if (force) {
                    outStream.write(7, 32);
                } else {
                    throw new CodingException("cannot ASCII encode (" + msg.charAt(i2) + ")");
                }
            }
            return outStream.toByteArray();
        } catch (BitwiseOutputStream.AccessException ex) {
            throw new CodingException("7bit ASCII encode failed: " + ex);
        }
    }

    private static byte[] encodeUtf16(String msg) throws CodingException {
        try {
            return msg.getBytes("utf-16be");
        } catch (UnsupportedEncodingException ex) {
            throw new CodingException("UTF-16 encode failed: " + ex);
        }
    }

    private static Gsm7bitCodingResult encode7bitGsm(String msg, int septetOffset, boolean force) throws CodingException {
        try {
            byte[] fullData = GsmAlphabet.stringToGsm7BitPacked(msg, septetOffset, !force, 0, 0);
            Gsm7bitCodingResult result = new Gsm7bitCodingResult();
            result.data = new byte[(fullData.length - 1)];
            System.arraycopy(fullData, 1, result.data, 0, fullData.length - 1);
            result.septets = fullData[0] & MidiConstants.STATUS_RESET;
            return result;
        } catch (EncodeException ex) {
            throw new CodingException("7bit GSM encode failed: " + ex);
        }
    }

    private static void encode7bitEms(UserData uData, byte[] udhData, boolean force) throws CodingException {
        Gsm7bitCodingResult gcr = encode7bitGsm(uData.payloadStr, (((udhData.length + 1) * 8) + 6) / 7, force);
        uData.msgEncoding = 9;
        uData.msgEncodingSet = true;
        uData.numFields = gcr.septets;
        uData.payload = gcr.data;
        uData.payload[0] = (byte) udhData.length;
        System.arraycopy(udhData, 0, uData.payload, 1, udhData.length);
    }

    private static void encode16bitEms(UserData uData, byte[] udhData) throws CodingException {
        byte[] payload = encodeUtf16(uData.payloadStr);
        int udhBytes = udhData.length + 1;
        uData.msgEncoding = 4;
        uData.msgEncodingSet = true;
        uData.numFields = ((udhBytes + 1) / 2) + (payload.length / 2);
        uData.payload = new byte[(uData.numFields * 2)];
        uData.payload[0] = (byte) udhData.length;
        System.arraycopy(udhData, 0, uData.payload, 1, udhData.length);
        System.arraycopy(payload, 0, uData.payload, udhBytes, payload.length);
    }

    private static void encodeEmsUserDataPayload(UserData uData) throws CodingException {
        byte[] headerData = SmsHeader.toByteArray(uData.userDataHeader);
        if (headerData == null) {
            Rlog.e(LOG_TAG, "user data with null headerData");
            return;
        }
        if (!uData.msgEncodingSet) {
            try {
                if (!HwFrameworkFactory.getHwBaseInnerSmsManager().encode7bitMultiSms(uData, headerData, false)) {
                    throw new CodingException("7bit multi sms encoding failed");
                }
            } catch (CodingException e) {
                encode16bitEms(uData, headerData);
            }
        } else if (uData.msgEncoding == 9) {
            encode7bitEms(uData, headerData, true);
        } else if (uData.msgEncoding == 2) {
            HwFrameworkFactory.getHwBaseInnerSmsManager().encode7bitMultiSms(uData, headerData, true);
        } else if (uData.msgEncoding == 4) {
            encode16bitEms(uData, headerData);
        } else {
            throw new CodingException("unsupported EMS user data encoding (" + uData.msgEncoding + ")");
        }
    }

    private static byte[] encodeShiftJis(String msg) throws CodingException {
        try {
            return msg.getBytes("Shift_JIS");
        } catch (UnsupportedEncodingException ex) {
            throw new CodingException("Shift-JIS encode failed: " + ex);
        }
    }

    private static void encodeUserDataPayload(UserData uData) throws CodingException {
        if (uData.payloadStr == null && uData.msgEncoding != 0) {
            Rlog.e(LOG_TAG, "user data with null payloadStr");
            uData.payloadStr = "";
        }
        if (uData.userDataHeader != null) {
            encodeEmsUserDataPayload(uData);
            return;
        }
        if (!uData.msgEncodingSet) {
            try {
                uData.payload = encode7bitAscii(uData.payloadStr, 0, false);
                uData.msgEncoding = 2;
            } catch (CodingException e) {
                uData.payload = encodeUtf16(uData.payloadStr);
                uData.msgEncoding = 4;
            }
            uData.numFields = uData.payloadStr.length();
            uData.msgEncodingSet = true;
        } else if (uData.msgEncoding != 0) {
            if (uData.payloadStr == null) {
                Rlog.e(LOG_TAG, "non-octet user data with null payloadStr");
                uData.payloadStr = "";
            }
            if (uData.msgEncoding == 9) {
                Gsm7bitCodingResult gcr = encode7bitGsm(uData.payloadStr, 0, true);
                uData.payload = gcr.data;
                uData.numFields = gcr.septets;
            } else if (uData.msgEncoding == 2) {
                uData.payload = encode7bitAscii(uData.payloadStr, 0, true);
                uData.numFields = uData.payloadStr.length();
            } else if (uData.msgEncoding == 4) {
                uData.payload = encodeUtf16(uData.payloadStr);
                uData.numFields = uData.payloadStr.length();
            } else if (uData.msgEncoding == 5) {
                uData.payload = encodeShiftJis(uData.payloadStr);
                uData.numFields = uData.payload.length;
            } else {
                throw new CodingException("unsupported user data encoding (" + uData.msgEncoding + ")");
            }
        } else if (uData.payload == null) {
            Rlog.e(LOG_TAG, "user data with octet encoding but null payload");
            uData.payload = new byte[0];
            uData.numFields = 0;
        } else {
            uData.numFields = uData.payload.length;
        }
    }

    private static void encodeUserData(BearerData bData, BitwiseOutputStream outStream) throws BitwiseOutputStream.AccessException, CodingException {
        encodeUserDataPayload(bData.userData);
        bData.hasUserDataHeader = bData.userData.userDataHeader != null;
        if (bData.userData.payload.length <= 140) {
            int dataBits = (bData.userData.payload.length * 8) - bData.userData.paddingBits;
            int paramBits = dataBits + 13;
            if (bData.userData.msgEncoding == 1 || bData.userData.msgEncoding == 10) {
                paramBits += 8;
            }
            int paramBytes = (paramBits / 8) + (paramBits % 8 > 0 ? 1 : 0);
            int paddingBits = (paramBytes * 8) - paramBits;
            outStream.write(8, paramBytes);
            outStream.write(5, bData.userData.msgEncoding);
            if (bData.userData.msgEncoding == 1 || bData.userData.msgEncoding == 10) {
                outStream.write(8, bData.userData.msgType);
            }
            outStream.write(8, bData.userData.numFields);
            outStream.writeByteArray(dataBits, bData.userData.payload);
            if (paddingBits > 0) {
                outStream.write(paddingBits, 0);
                return;
            }
            return;
        }
        throw new CodingException("encoded user data too large (" + bData.userData.payload.length + " > " + 140 + " bytes)");
    }

    private static void encodeReplyOption(BearerData bData, BitwiseOutputStream outStream) throws BitwiseOutputStream.AccessException {
        outStream.write(8, 1);
        outStream.write(1, bData.userAckReq ? 1 : 0);
        outStream.write(1, bData.deliveryAckReq ? 1 : 0);
        outStream.write(1, bData.readAckReq ? 1 : 0);
        outStream.write(1, bData.reportReq ? 1 : 0);
        outStream.write(4, 0);
    }

    private static byte[] encodeDtmfSmsAddress(String address) {
        int val;
        int digits = address.length();
        int dataBits = digits * 4;
        byte[] rawData = new byte[((dataBits / 8) + (dataBits % 8 > 0 ? 1 : 0))];
        for (int i = 0; i < digits; i++) {
            char c = address.charAt(i);
            if (c >= '1' && c <= '9') {
                val = c - '0';
            } else if (c == '0') {
                val = 10;
            } else if (c == '*') {
                val = 11;
            } else if (c != '#') {
                return null;
            } else {
                val = 12;
            }
            int i2 = i / 2;
            rawData[i2] = (byte) (rawData[i2] | (val << (4 - ((i % 2) * 4))));
        }
        return rawData;
    }

    private static void encodeCdmaSmsAddress(CdmaSmsAddress addr) throws CodingException {
        if (addr.digitMode == 1) {
            try {
                addr.origBytes = addr.address.getBytes("US-ASCII");
            } catch (UnsupportedEncodingException e) {
                throw new CodingException("invalid SMS address, cannot convert to ASCII");
            }
        } else {
            addr.origBytes = encodeDtmfSmsAddress(addr.address);
        }
    }

    private static void encodeCallbackNumber(BearerData bData, BitwiseOutputStream outStream) throws BitwiseOutputStream.AccessException, CodingException {
        int dataBits;
        CdmaSmsAddress addr = bData.callbackNumber;
        encodeCdmaSmsAddress(addr);
        int paramBits = 9;
        if (addr.digitMode == 1) {
            paramBits = 9 + 7;
            dataBits = addr.numberOfDigits * 8;
        } else {
            dataBits = addr.numberOfDigits * 4;
        }
        int paramBits2 = paramBits + dataBits;
        int paramBytes = (paramBits2 / 8) + (paramBits2 % 8 > 0 ? 1 : 0);
        int paddingBits = (paramBytes * 8) - paramBits2;
        outStream.write(8, paramBytes);
        outStream.write(1, addr.digitMode);
        if (addr.digitMode == 1) {
            outStream.write(3, addr.ton);
            outStream.write(4, addr.numberPlan);
        }
        outStream.write(8, addr.numberOfDigits);
        outStream.writeByteArray(dataBits, addr.origBytes);
        if (paddingBits > 0) {
            outStream.write(paddingBits, 0);
        }
    }

    private static void encodeMsgStatus(BearerData bData, BitwiseOutputStream outStream) throws BitwiseOutputStream.AccessException {
        outStream.write(8, 1);
        outStream.write(2, bData.errorClass);
        outStream.write(6, bData.messageStatus);
    }

    private static void encodeMsgCount(BearerData bData, BitwiseOutputStream outStream) throws BitwiseOutputStream.AccessException {
        outStream.write(8, 1);
        outStream.write(8, bData.numberOfMessages);
    }

    private static void encodeValidityPeriodRel(BearerData bData, BitwiseOutputStream outStream) throws BitwiseOutputStream.AccessException {
        outStream.write(8, 1);
        outStream.write(8, bData.validityPeriodRelative);
    }

    private static void encodePrivacyIndicator(BearerData bData, BitwiseOutputStream outStream) throws BitwiseOutputStream.AccessException {
        outStream.write(8, 1);
        outStream.write(2, bData.privacy);
        outStream.skip(6);
    }

    private static void encodeLanguageIndicator(BearerData bData, BitwiseOutputStream outStream) throws BitwiseOutputStream.AccessException {
        outStream.write(8, 1);
        outStream.write(8, bData.language);
    }

    private static void encodeDisplayMode(BearerData bData, BitwiseOutputStream outStream) throws BitwiseOutputStream.AccessException {
        outStream.write(8, 1);
        outStream.write(2, bData.displayMode);
        outStream.skip(6);
    }

    private static void encodePriorityIndicator(BearerData bData, BitwiseOutputStream outStream) throws BitwiseOutputStream.AccessException {
        outStream.write(8, 1);
        outStream.write(2, bData.priority);
        outStream.skip(6);
    }

    private static void encodeMsgDeliveryAlert(BearerData bData, BitwiseOutputStream outStream) throws BitwiseOutputStream.AccessException {
        outStream.write(8, 1);
        outStream.write(2, bData.alert);
        outStream.skip(6);
    }

    private static void encodeScpResults(BearerData bData, BitwiseOutputStream outStream) throws BitwiseOutputStream.AccessException {
        ArrayList<CdmaSmsCbProgramResults> results = bData.serviceCategoryProgramResults;
        outStream.write(8, results.size() * 4);
        Iterator<CdmaSmsCbProgramResults> it = results.iterator();
        while (it.hasNext()) {
            CdmaSmsCbProgramResults result = it.next();
            int category = result.getCategory();
            outStream.write(8, category >> 8);
            outStream.write(8, category);
            outStream.write(8, result.getLanguage());
            outStream.write(4, result.getCategoryResult());
            outStream.skip(4);
        }
    }

    public static byte[] encode(BearerData bData) {
        bData.hasUserDataHeader = (bData.userData == null || bData.userData.userDataHeader == null) ? false : true;
        try {
            BitwiseOutputStream outStream = new BitwiseOutputStream(200);
            outStream.write(8, 0);
            encodeMessageId(bData, outStream);
            if (bData.userData != null) {
                outStream.write(8, 1);
                encodeUserData(bData, outStream);
            }
            if (bData.callbackNumber != null) {
                outStream.write(8, 14);
                encodeCallbackNumber(bData, outStream);
            }
            if (bData.userAckReq || bData.deliveryAckReq || bData.readAckReq || bData.reportReq) {
                outStream.write(8, 10);
                encodeReplyOption(bData, outStream);
            }
            if (bData.numberOfMessages != 0) {
                outStream.write(8, 11);
                encodeMsgCount(bData, outStream);
            }
            if (bData.validityPeriodRelativeSet) {
                outStream.write(8, 5);
                encodeValidityPeriodRel(bData, outStream);
            }
            if (bData.privacyIndicatorSet) {
                outStream.write(8, 9);
                encodePrivacyIndicator(bData, outStream);
            }
            if (bData.languageIndicatorSet) {
                outStream.write(8, 13);
                encodeLanguageIndicator(bData, outStream);
            }
            if (bData.displayModeSet) {
                outStream.write(8, 15);
                encodeDisplayMode(bData, outStream);
            }
            if (bData.priorityIndicatorSet) {
                outStream.write(8, 8);
                encodePriorityIndicator(bData, outStream);
            }
            if (bData.alertIndicatorSet) {
                outStream.write(8, 12);
                encodeMsgDeliveryAlert(bData, outStream);
            }
            if (bData.messageStatusSet) {
                outStream.write(8, 20);
                encodeMsgStatus(bData, outStream);
            }
            if (bData.serviceCategoryProgramResults != null) {
                outStream.write(8, 19);
                encodeScpResults(bData, outStream);
            }
            HwFrameworkFactory.getHwBaseInnerSmsManager().encodeMsgCenterTimeStampCheck(bData, outStream);
            return outStream.toByteArray();
        } catch (BitwiseOutputStream.AccessException ex) {
            Rlog.e(LOG_TAG, "BearerData encode failed: " + ex);
            return null;
        } catch (CodingException ex2) {
            Rlog.e(LOG_TAG, "BearerData encode failed: " + ex2);
            return null;
        }
    }

    private static boolean decodeMessageId(BearerData bData, BitwiseInputStream inStream) throws BitwiseInputStream.AccessException {
        boolean decodeSuccess = false;
        int paramBits = inStream.read(8) * 8;
        if (paramBits >= 24) {
            paramBits -= 24;
            decodeSuccess = true;
            bData.messageType = inStream.read(4);
            bData.messageId = inStream.read(8) << 8;
            bData.messageId = inStream.read(8) | bData.messageId;
            boolean z = true;
            if (inStream.read(1) != 1) {
                z = false;
            }
            bData.hasUserDataHeader = z;
            inStream.skip(3);
        }
        if (!decodeSuccess || paramBits > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("MESSAGE_IDENTIFIER decode ");
            sb.append(decodeSuccess ? "succeeded" : "failed");
            sb.append(" (extra bits = ");
            sb.append(paramBits);
            sb.append(")");
            Rlog.d(LOG_TAG, sb.toString());
        }
        inStream.skip(paramBits);
        return decodeSuccess;
    }

    private static boolean decodeReserved(BearerData bData, BitwiseInputStream inStream, int subparamId) throws BitwiseInputStream.AccessException, CodingException {
        boolean decodeSuccess = false;
        int subparamLen = inStream.read(8);
        int paramBits = subparamLen * 8;
        if (paramBits <= inStream.available()) {
            decodeSuccess = true;
            inStream.skip(paramBits);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("RESERVED bearer data subparameter ");
        sb.append(subparamId);
        sb.append(" decode ");
        sb.append(decodeSuccess ? "succeeded" : "failed");
        sb.append(" (param bits = ");
        sb.append(paramBits);
        sb.append(")");
        Rlog.d(LOG_TAG, sb.toString());
        if (decodeSuccess) {
            return decodeSuccess;
        }
        throw new CodingException("RESERVED bearer data subparameter " + subparamId + " had invalid SUBPARAM_LEN " + subparamLen);
    }

    private static boolean decodeUserData(BearerData bData, BitwiseInputStream inStream) throws BitwiseInputStream.AccessException {
        int paramBits = inStream.read(8) * 8;
        bData.userData = new UserData();
        bData.userData.msgEncoding = inStream.read(5);
        bData.userData.msgEncodingSet = true;
        bData.userData.msgType = 0;
        int consumedBits = 5;
        if (bData.userData.msgEncoding == 1 || bData.userData.msgEncoding == 10) {
            bData.userData.msgType = inStream.read(8);
            consumedBits = 5 + 8;
        }
        bData.userData.numFields = inStream.read(8);
        bData.userData.payload = inStream.readByteArray(paramBits - (consumedBits + 8));
        return true;
    }

    private static String decodeUtf8(byte[] data, int offset, int numFields) throws CodingException {
        return decodeCharset(data, offset, numFields, 1, "UTF-8");
    }

    private static String decodeUtf16(byte[] data, int offset, int numFields) throws CodingException {
        return decodeCharset(data, offset, numFields - ((offset + (offset % 2)) / 2), 2, "utf-16be");
    }

    private static String decodeCharset(byte[] data, int offset, int numFields, int width, String charset) throws CodingException {
        if (numFields < 0 || (numFields * width) + offset > data.length) {
            int maxNumFields = ((data.length - offset) - (offset % width)) / width;
            if (maxNumFields >= 0) {
                Rlog.e(LOG_TAG, charset + " decode error: offset = " + offset + " numFields = " + numFields + " data.length = " + data.length + " maxNumFields = " + maxNumFields);
                numFields = maxNumFields;
            } else {
                throw new CodingException(charset + " decode failed: offset out of range");
            }
        }
        try {
            return new String(data, offset, numFields * width, charset);
        } catch (UnsupportedEncodingException ex) {
            throw new CodingException(charset + " decode failed: " + ex);
        }
    }

    private static String decode7bitAscii(byte[] data, int offset, int numFields) throws CodingException {
        int offsetBits = offset * 8;
        try {
            int offsetSeptets = (offsetBits + 6) / 7;
            int numFields2 = numFields - offsetSeptets;
            int paddingBits = (offsetSeptets * 7) - offsetBits;
            StringBuffer strBuf = new StringBuffer(numFields2);
            BitwiseInputStream inStream = new BitwiseInputStream(data);
            int wantedBits = (offsetSeptets * 7) + (numFields2 * 7);
            if (inStream.available() >= wantedBits) {
                inStream.skip(offsetBits + paddingBits);
                for (int i = 0; i < numFields2; i++) {
                    int charCode = inStream.read(7);
                    if (charCode >= 32 && charCode <= UserData.ASCII_MAP_MAX_INDEX) {
                        strBuf.append(UserData.ASCII_MAP[charCode - 32]);
                    } else if (charCode == 10) {
                        strBuf.append(10);
                    } else if (charCode == 13) {
                        strBuf.append(13);
                    } else {
                        strBuf.append(' ');
                    }
                }
                return strBuf.toString();
            }
            throw new CodingException("insufficient data (wanted " + wantedBits + " bits, but only have " + inStream.available() + ")");
        } catch (BitwiseInputStream.AccessException ex) {
            throw new CodingException("7bit ASCII decode failed: " + ex);
        }
    }

    private static String decode7bitGsm(byte[] data, int offset, int numFields) throws CodingException {
        int offsetBits = offset * 8;
        int offsetSeptets = (offsetBits + 6) / 7;
        String result = GsmAlphabet.gsm7BitPackedToString(data, offset, numFields - offsetSeptets, (offsetSeptets * 7) - offsetBits, 0, 0);
        if (result != null) {
            return result;
        }
        throw new CodingException("7bit GSM decoding failed");
    }

    private static String decodeLatin(byte[] data, int offset, int numFields) throws CodingException {
        return decodeCharset(data, offset, numFields, 1, "ISO-8859-1");
    }

    private static String decodeShiftJis(byte[] data, int offset, int numFields) throws CodingException {
        return decodeCharset(data, offset, numFields, 1, "Shift_JIS");
    }

    private static String decodeGsmDcs(byte[] data, int offset, int numFields, int msgType) throws CodingException {
        if ((msgType & 192) == 0) {
            switch ((msgType >> 2) & 3) {
                case 0:
                    return decode7bitGsm(data, offset, numFields);
                case 1:
                    return decodeUtf8(data, offset, numFields);
                case 2:
                    return decodeUtf16(data, offset, numFields);
                default:
                    throw new CodingException("unsupported user msgType encoding (" + msgType + ")");
            }
        } else {
            throw new CodingException("unsupported coding group (" + msgType + ")");
        }
    }

    private static void decodeUserDataPayload(UserData userData2, boolean hasUserDataHeader2) throws CodingException {
        int offset = 0;
        if (hasUserDataHeader2) {
            int udhLen = userData2.payload[0] & MidiConstants.STATUS_RESET;
            offset = 0 + udhLen + 1;
            byte[] headerData = new byte[udhLen];
            System.arraycopy(userData2.payload, 1, headerData, 0, udhLen);
            userData2.userDataHeader = SmsHeader.fromByteArray(headerData);
        }
        switch (userData2.msgEncoding) {
            case 0:
                boolean decodingtypeUTF8 = Resources.getSystem().getBoolean(17957029);
                byte[] payload = new byte[userData2.numFields];
                System.arraycopy(userData2.payload, 0, payload, 0, userData2.numFields < userData2.payload.length ? userData2.numFields : userData2.payload.length);
                userData2.payload = payload;
                if (!decodingtypeUTF8) {
                    userData2.payloadStr = decodeLatin(userData2.payload, offset, userData2.numFields);
                    return;
                } else {
                    userData2.payloadStr = decodeUtf8(userData2.payload, offset, userData2.numFields);
                    return;
                }
            case 2:
            case 3:
                userData2.payloadStr = decode7bitAscii(userData2.payload, offset, userData2.numFields);
                return;
            case 4:
                userData2.payloadStr = decodeUtf16(userData2.payload, offset, userData2.numFields);
                return;
            case 5:
                userData2.payloadStr = decodeShiftJis(userData2.payload, offset, userData2.numFields);
                return;
            case 8:
                userData2.payloadStr = decodeLatin(userData2.payload, offset, userData2.numFields);
                return;
            case 9:
                userData2.payloadStr = decode7bitGsm(userData2.payload, offset, userData2.numFields);
                return;
            case 10:
                userData2.payloadStr = decodeGsmDcs(userData2.payload, offset, userData2.numFields, userData2.msgType);
                return;
            default:
                throw new CodingException("unsupported user data encoding (" + userData2.msgEncoding + ")");
        }
    }

    private static void decodeIs91VoicemailStatus(BearerData bData) throws BitwiseInputStream.AccessException, CodingException {
        BitwiseInputStream inStream = new BitwiseInputStream(bData.userData.payload);
        int dataLen = inStream.available() / 6;
        int numFields = bData.userData.numFields;
        if (dataLen > 14 || dataLen < 3 || dataLen < numFields) {
            throw new CodingException("IS-91 voicemail status decoding failed");
        }
        try {
            StringBuffer strbuf = new StringBuffer(dataLen);
            while (inStream.available() >= 6) {
                strbuf.append(UserData.ASCII_MAP[inStream.read(6)]);
            }
            String data = strbuf.toString();
            bData.numberOfMessages = Integer.parseInt(data.substring(0, 2));
            char prioCode = data.charAt(2);
            if (prioCode == ' ') {
                bData.priority = 0;
            } else if (prioCode == '!') {
                bData.priority = 2;
            } else {
                throw new CodingException("IS-91 voicemail status decoding failed: illegal priority setting (" + prioCode + ")");
            }
            bData.priorityIndicatorSet = true;
            bData.userData.payloadStr = data.substring(3, numFields - 3);
        } catch (NumberFormatException ex) {
            throw new CodingException("IS-91 voicemail status decoding failed: " + ex);
        } catch (IndexOutOfBoundsException ex2) {
            throw new CodingException("IS-91 voicemail status decoding failed: " + ex2);
        }
    }

    private static void decodeIs91ShortMessage(BearerData bData) throws BitwiseInputStream.AccessException, CodingException {
        BitwiseInputStream inStream = new BitwiseInputStream(bData.userData.payload);
        int dataLen = inStream.available() / 6;
        int numFields = bData.userData.numFields;
        if (numFields > 14 || dataLen < numFields) {
            throw new CodingException("IS-91 short message decoding failed");
        }
        StringBuffer strbuf = new StringBuffer(dataLen);
        for (int i = 0; i < numFields; i++) {
            strbuf.append(UserData.ASCII_MAP[inStream.read(6)]);
        }
        bData.userData.payloadStr = strbuf.toString();
    }

    private static void decodeIs91Cli(BearerData bData) throws CodingException {
        int dataLen = new BitwiseInputStream(bData.userData.payload).available() / 4;
        int numFields = bData.userData.numFields;
        if (dataLen > 14 || dataLen < 3 || dataLen < numFields) {
            throw new CodingException("IS-91 voicemail status decoding failed");
        }
        CdmaSmsAddress addr = new CdmaSmsAddress();
        addr.digitMode = 0;
        addr.origBytes = bData.userData.payload;
        addr.numberOfDigits = (byte) numFields;
        decodeSmsAddress(addr);
        bData.callbackNumber = addr;
    }

    private static void decodeIs91(BearerData bData) throws BitwiseInputStream.AccessException, CodingException {
        switch (bData.userData.msgType) {
            case 130:
                decodeIs91VoicemailStatus(bData);
                return;
            case 131:
            case 133:
                decodeIs91ShortMessage(bData);
                return;
            case 132:
                decodeIs91Cli(bData);
                return;
            default:
                throw new CodingException("unsupported IS-91 message type (" + bData.userData.msgType + ")");
        }
    }

    private static boolean decodeReplyOption(BearerData bData, BitwiseInputStream inStream) throws BitwiseInputStream.AccessException {
        boolean decodeSuccess = false;
        int paramBits = inStream.read(8) * 8;
        if (paramBits >= 8) {
            paramBits -= 8;
            decodeSuccess = true;
            boolean z = true;
            bData.userAckReq = inStream.read(1) == 1;
            bData.deliveryAckReq = inStream.read(1) == 1;
            bData.readAckReq = inStream.read(1) == 1;
            if (inStream.read(1) != 1) {
                z = false;
            }
            bData.reportReq = z;
            inStream.skip(4);
        }
        if (!decodeSuccess || paramBits > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("REPLY_OPTION decode ");
            sb.append(decodeSuccess ? "succeeded" : "failed");
            sb.append(" (extra bits = ");
            sb.append(paramBits);
            sb.append(")");
            Rlog.d(LOG_TAG, sb.toString());
        }
        inStream.skip(paramBits);
        return decodeSuccess;
    }

    private static boolean decodeMsgCount(BearerData bData, BitwiseInputStream inStream) throws BitwiseInputStream.AccessException {
        boolean decodeSuccess = false;
        int paramBits = inStream.read(8) * 8;
        if (paramBits >= 8) {
            paramBits -= 8;
            decodeSuccess = true;
            bData.numberOfMessages = IccUtils.cdmaBcdByteToInt((byte) inStream.read(8));
        }
        if (!decodeSuccess || paramBits > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("NUMBER_OF_MESSAGES decode ");
            sb.append(decodeSuccess ? "succeeded" : "failed");
            sb.append(" (extra bits = ");
            sb.append(paramBits);
            sb.append(")");
            Rlog.d(LOG_TAG, sb.toString());
        }
        inStream.skip(paramBits);
        return decodeSuccess;
    }

    private static boolean decodeDepositIndex(BearerData bData, BitwiseInputStream inStream) throws BitwiseInputStream.AccessException {
        boolean decodeSuccess = false;
        int paramBits = inStream.read(8) * 8;
        if (paramBits >= 16) {
            paramBits -= 16;
            decodeSuccess = true;
            bData.depositIndex = inStream.read(8) | (inStream.read(8) << 8);
        }
        if (!decodeSuccess || paramBits > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("MESSAGE_DEPOSIT_INDEX decode ");
            sb.append(decodeSuccess ? "succeeded" : "failed");
            sb.append(" (extra bits = ");
            sb.append(paramBits);
            sb.append(")");
            Rlog.d(LOG_TAG, sb.toString());
        }
        inStream.skip(paramBits);
        return decodeSuccess;
    }

    private static String decodeDtmfSmsAddress(byte[] rawData, int numFields) throws CodingException {
        StringBuffer strBuf = new StringBuffer(numFields);
        for (int i = 0; i < numFields; i++) {
            int val = 15 & (rawData[i / 2] >>> (4 - ((i % 2) * 4)));
            if (val >= 1 && val <= 9) {
                strBuf.append(Integer.toString(val, 10));
            } else if (val == 10) {
                strBuf.append('0');
            } else if (val == 11) {
                strBuf.append('*');
            } else if (val == 12) {
                strBuf.append('#');
            } else {
                throw new CodingException("invalid SMS address DTMF code (" + val + ")");
            }
        }
        return strBuf.toString();
    }

    private static void decodeSmsAddress(CdmaSmsAddress addr) throws CodingException {
        if (addr.digitMode == 1) {
            try {
                addr.address = new String(addr.origBytes, 0, addr.origBytes.length, "US-ASCII");
            } catch (UnsupportedEncodingException e) {
                throw new CodingException("invalid SMS address ASCII code");
            }
        } else {
            addr.address = decodeDtmfSmsAddress(addr.origBytes, addr.numberOfDigits);
        }
    }

    private static boolean decodeCallbackNumber(BearerData bData, BitwiseInputStream inStream) throws BitwiseInputStream.AccessException, CodingException {
        int paramBits = inStream.read(8) * 8;
        if (paramBits < 8) {
            inStream.skip(paramBits);
            return false;
        }
        CdmaSmsAddress addr = new CdmaSmsAddress();
        addr.digitMode = inStream.read(1);
        byte fieldBits = SUBPARAM_VALIDITY_PERIOD_ABSOLUTE;
        byte consumedBits = SUBPARAM_USER_DATA;
        if (addr.digitMode == 1) {
            addr.ton = inStream.read(3);
            addr.numberPlan = inStream.read(4);
            fieldBits = SUBPARAM_PRIORITY_INDICATOR;
            consumedBits = (byte) (1 + 7);
        }
        addr.numberOfDigits = inStream.read(8);
        int remainingBits = paramBits - ((byte) (consumedBits + SUBPARAM_PRIORITY_INDICATOR));
        int dataBits = addr.numberOfDigits * fieldBits;
        int paddingBits = remainingBits - dataBits;
        if (remainingBits >= dataBits) {
            addr.origBytes = inStream.readByteArray(dataBits);
            inStream.skip(paddingBits);
            decodeSmsAddress(addr);
            bData.callbackNumber = addr;
            return true;
        }
        throw new CodingException("CALLBACK_NUMBER subparam encoding size error (remainingBits + " + remainingBits + ", dataBits + " + dataBits + ", paddingBits + " + paddingBits + ")");
    }

    private static boolean decodeMsgStatus(BearerData bData, BitwiseInputStream inStream) throws BitwiseInputStream.AccessException {
        boolean decodeSuccess = false;
        int paramBits = inStream.read(8) * 8;
        if (paramBits >= 8) {
            paramBits -= 8;
            decodeSuccess = true;
            bData.errorClass = inStream.read(2);
            bData.messageStatus = inStream.read(6);
        }
        if (!decodeSuccess || paramBits > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("MESSAGE_STATUS decode ");
            sb.append(decodeSuccess ? "succeeded" : "failed");
            sb.append(" (extra bits = ");
            sb.append(paramBits);
            sb.append(")");
            Rlog.d(LOG_TAG, sb.toString());
        }
        inStream.skip(paramBits);
        bData.messageStatusSet = decodeSuccess;
        return decodeSuccess;
    }

    private static boolean decodeMsgCenterTimeStamp(BearerData bData, BitwiseInputStream inStream) throws BitwiseInputStream.AccessException {
        boolean decodeSuccess = false;
        int paramBits = inStream.read(8) * 8;
        if (paramBits >= 48) {
            paramBits -= 48;
            decodeSuccess = true;
            bData.msgCenterTimeStamp = TimeStamp.fromByteArray(inStream.readByteArray(48));
        }
        if (!decodeSuccess || paramBits > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("MESSAGE_CENTER_TIME_STAMP decode ");
            sb.append(decodeSuccess ? "succeeded" : "failed");
            sb.append(" (extra bits = ");
            sb.append(paramBits);
            sb.append(")");
            Rlog.d(LOG_TAG, sb.toString());
        }
        inStream.skip(paramBits);
        return decodeSuccess;
    }

    private static boolean decodeValidityAbs(BearerData bData, BitwiseInputStream inStream) throws BitwiseInputStream.AccessException {
        boolean decodeSuccess = false;
        int paramBits = inStream.read(8) * 8;
        if (paramBits >= 48) {
            paramBits -= 48;
            decodeSuccess = true;
            bData.validityPeriodAbsolute = TimeStamp.fromByteArray(inStream.readByteArray(48));
        }
        if (!decodeSuccess || paramBits > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("VALIDITY_PERIOD_ABSOLUTE decode ");
            sb.append(decodeSuccess ? "succeeded" : "failed");
            sb.append(" (extra bits = ");
            sb.append(paramBits);
            sb.append(")");
            Rlog.d(LOG_TAG, sb.toString());
        }
        inStream.skip(paramBits);
        return decodeSuccess;
    }

    private static boolean decodeDeferredDeliveryAbs(BearerData bData, BitwiseInputStream inStream) throws BitwiseInputStream.AccessException {
        boolean decodeSuccess = false;
        int paramBits = inStream.read(8) * 8;
        if (paramBits >= 48) {
            paramBits -= 48;
            decodeSuccess = true;
            bData.deferredDeliveryTimeAbsolute = TimeStamp.fromByteArray(inStream.readByteArray(48));
        }
        if (!decodeSuccess || paramBits > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("DEFERRED_DELIVERY_TIME_ABSOLUTE decode ");
            sb.append(decodeSuccess ? "succeeded" : "failed");
            sb.append(" (extra bits = ");
            sb.append(paramBits);
            sb.append(")");
            Rlog.d(LOG_TAG, sb.toString());
        }
        inStream.skip(paramBits);
        return decodeSuccess;
    }

    private static boolean decodeValidityRel(BearerData bData, BitwiseInputStream inStream) throws BitwiseInputStream.AccessException {
        boolean decodeSuccess = false;
        int paramBits = inStream.read(8) * 8;
        if (paramBits >= 8) {
            paramBits -= 8;
            decodeSuccess = true;
            bData.deferredDeliveryTimeRelative = inStream.read(8);
        }
        if (!decodeSuccess || paramBits > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("VALIDITY_PERIOD_RELATIVE decode ");
            sb.append(decodeSuccess ? "succeeded" : "failed");
            sb.append(" (extra bits = ");
            sb.append(paramBits);
            sb.append(")");
            Rlog.d(LOG_TAG, sb.toString());
        }
        inStream.skip(paramBits);
        bData.deferredDeliveryTimeRelativeSet = decodeSuccess;
        return decodeSuccess;
    }

    private static boolean decodeDeferredDeliveryRel(BearerData bData, BitwiseInputStream inStream) throws BitwiseInputStream.AccessException {
        boolean decodeSuccess = false;
        int paramBits = inStream.read(8) * 8;
        if (paramBits >= 8) {
            paramBits -= 8;
            decodeSuccess = true;
            bData.validityPeriodRelative = inStream.read(8);
        }
        if (!decodeSuccess || paramBits > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("DEFERRED_DELIVERY_TIME_RELATIVE decode ");
            sb.append(decodeSuccess ? "succeeded" : "failed");
            sb.append(" (extra bits = ");
            sb.append(paramBits);
            sb.append(")");
            Rlog.d(LOG_TAG, sb.toString());
        }
        inStream.skip(paramBits);
        bData.validityPeriodRelativeSet = decodeSuccess;
        return decodeSuccess;
    }

    private static boolean decodePrivacyIndicator(BearerData bData, BitwiseInputStream inStream) throws BitwiseInputStream.AccessException {
        boolean decodeSuccess = false;
        int paramBits = inStream.read(8) * 8;
        if (paramBits >= 8) {
            paramBits -= 8;
            decodeSuccess = true;
            bData.privacy = inStream.read(2);
            inStream.skip(6);
        }
        if (!decodeSuccess || paramBits > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("PRIVACY_INDICATOR decode ");
            sb.append(decodeSuccess ? "succeeded" : "failed");
            sb.append(" (extra bits = ");
            sb.append(paramBits);
            sb.append(")");
            Rlog.d(LOG_TAG, sb.toString());
        }
        inStream.skip(paramBits);
        bData.privacyIndicatorSet = decodeSuccess;
        return decodeSuccess;
    }

    private static boolean decodeLanguageIndicator(BearerData bData, BitwiseInputStream inStream) throws BitwiseInputStream.AccessException {
        boolean decodeSuccess = false;
        int paramBits = inStream.read(8) * 8;
        if (paramBits >= 8) {
            paramBits -= 8;
            decodeSuccess = true;
            bData.language = inStream.read(8);
        }
        if (!decodeSuccess || paramBits > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("LANGUAGE_INDICATOR decode ");
            sb.append(decodeSuccess ? "succeeded" : "failed");
            sb.append(" (extra bits = ");
            sb.append(paramBits);
            sb.append(")");
            Rlog.d(LOG_TAG, sb.toString());
        }
        inStream.skip(paramBits);
        bData.languageIndicatorSet = decodeSuccess;
        return decodeSuccess;
    }

    private static boolean decodeDisplayMode(BearerData bData, BitwiseInputStream inStream) throws BitwiseInputStream.AccessException {
        boolean decodeSuccess = false;
        int paramBits = inStream.read(8) * 8;
        if (paramBits >= 8) {
            paramBits -= 8;
            decodeSuccess = true;
            bData.displayMode = inStream.read(2);
            inStream.skip(6);
        }
        if (!decodeSuccess || paramBits > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("DISPLAY_MODE decode ");
            sb.append(decodeSuccess ? "succeeded" : "failed");
            sb.append(" (extra bits = ");
            sb.append(paramBits);
            sb.append(")");
            Rlog.d(LOG_TAG, sb.toString());
        }
        inStream.skip(paramBits);
        bData.displayModeSet = decodeSuccess;
        return decodeSuccess;
    }

    private static boolean decodePriorityIndicator(BearerData bData, BitwiseInputStream inStream) throws BitwiseInputStream.AccessException {
        boolean decodeSuccess = false;
        int paramBits = inStream.read(8) * 8;
        if (paramBits >= 8) {
            paramBits -= 8;
            decodeSuccess = true;
            bData.priority = inStream.read(2);
            inStream.skip(6);
        }
        if (!decodeSuccess || paramBits > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("PRIORITY_INDICATOR decode ");
            sb.append(decodeSuccess ? "succeeded" : "failed");
            sb.append(" (extra bits = ");
            sb.append(paramBits);
            sb.append(")");
            Rlog.d(LOG_TAG, sb.toString());
        }
        inStream.skip(paramBits);
        bData.priorityIndicatorSet = decodeSuccess;
        return decodeSuccess;
    }

    private static boolean decodeMsgDeliveryAlert(BearerData bData, BitwiseInputStream inStream) throws BitwiseInputStream.AccessException {
        boolean decodeSuccess = false;
        int paramBits = inStream.read(8) * 8;
        if (paramBits >= 8) {
            paramBits -= 8;
            decodeSuccess = true;
            bData.alert = inStream.read(2);
            inStream.skip(6);
        }
        if (!decodeSuccess || paramBits > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("ALERT_ON_MESSAGE_DELIVERY decode ");
            sb.append(decodeSuccess ? "succeeded" : "failed");
            sb.append(" (extra bits = ");
            sb.append(paramBits);
            sb.append(")");
            Rlog.d(LOG_TAG, sb.toString());
        }
        inStream.skip(paramBits);
        bData.alertIndicatorSet = decodeSuccess;
        return decodeSuccess;
    }

    private static boolean decodeUserResponseCode(BearerData bData, BitwiseInputStream inStream) throws BitwiseInputStream.AccessException {
        boolean decodeSuccess = false;
        int paramBits = inStream.read(8) * 8;
        if (paramBits >= 8) {
            paramBits -= 8;
            decodeSuccess = true;
            bData.userResponseCode = inStream.read(8);
        }
        if (!decodeSuccess || paramBits > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("USER_RESPONSE_CODE decode ");
            sb.append(decodeSuccess ? "succeeded" : "failed");
            sb.append(" (extra bits = ");
            sb.append(paramBits);
            sb.append(")");
            Rlog.d(LOG_TAG, sb.toString());
        }
        inStream.skip(paramBits);
        bData.userResponseCodeSet = decodeSuccess;
        return decodeSuccess;
    }

    private static boolean decodeServiceCategoryProgramData(BearerData bData, BitwiseInputStream inStream) throws BitwiseInputStream.AccessException, CodingException {
        BitwiseInputStream bitwiseInputStream = inStream;
        if (inStream.available() >= 13) {
            int i = 8;
            int msgEncoding = bitwiseInputStream.read(5);
            int paramBits = (bitwiseInputStream.read(8) * 8) - 5;
            if (inStream.available() >= paramBits) {
                ArrayList<CdmaSmsCbProgramData> programDataList = new ArrayList<>();
                int paramBits2 = paramBits;
                boolean decodeSuccess = false;
                while (paramBits2 >= 48) {
                    int operation = bitwiseInputStream.read(4);
                    int category = (bitwiseInputStream.read(i) << i) | bitwiseInputStream.read(i);
                    int language2 = bitwiseInputStream.read(i);
                    int maxMessages = bitwiseInputStream.read(i);
                    int alertOption = bitwiseInputStream.read(4);
                    int numFields = bitwiseInputStream.read(i);
                    int paramBits3 = paramBits2 - 48;
                    int textBits = getBitsForNumFields(msgEncoding, numFields);
                    if (paramBits3 >= textBits) {
                        UserData userData2 = new UserData();
                        userData2.msgEncoding = msgEncoding;
                        userData2.msgEncodingSet = true;
                        userData2.numFields = numFields;
                        userData2.payload = bitwiseInputStream.readByteArray(textBits);
                        paramBits2 = paramBits3 - textBits;
                        decodeUserDataPayload(userData2, false);
                        UserData userData3 = userData2;
                        int i2 = textBits;
                        int i3 = numFields;
                        CdmaSmsCbProgramData programData = new CdmaSmsCbProgramData(operation, category, language2, maxMessages, alertOption, userData2.payloadStr);
                        programDataList.add(programData);
                        decodeSuccess = true;
                        i = 8;
                    } else {
                        int i4 = numFields;
                        throw new CodingException("category name is " + textBits + " bits in length, but there are only " + paramBits3 + " bits available");
                    }
                }
                if (!decodeSuccess || paramBits2 > 0) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("SERVICE_CATEGORY_PROGRAM_DATA decode ");
                    sb.append(decodeSuccess ? "succeeded" : "failed");
                    sb.append(" (extra bits = ");
                    sb.append(paramBits2);
                    sb.append(')');
                    Rlog.d(LOG_TAG, sb.toString());
                }
                bitwiseInputStream.skip(paramBits2);
                bData.serviceCategoryProgramData = programDataList;
                return decodeSuccess;
            }
            BearerData bearerData = bData;
            throw new CodingException("SERVICE_CATEGORY_PROGRAM_DATA decode failed: only " + inStream.available() + " bits available (" + paramBits + " bits expected)");
        }
        BearerData bearerData2 = bData;
        throw new CodingException("SERVICE_CATEGORY_PROGRAM_DATA decode failed: only " + inStream.available() + " bits available");
    }

    private static int serviceCategoryToCmasMessageClass(int serviceCategory) {
        switch (serviceCategory) {
            case 4096:
                return 0;
            case 4097:
                return 1;
            case 4098:
                return 2;
            case 4099:
                return 3;
            case 4100:
                return 4;
            default:
                return -1;
        }
    }

    private static int getBitsForNumFields(int msgEncoding, int numFields) throws CodingException {
        if (msgEncoding != 0) {
            switch (msgEncoding) {
                case 2:
                case 3:
                case 9:
                    return numFields * 7;
                case 4:
                    return numFields * 16;
                case 5:
                case 6:
                case 7:
                case 8:
                    break;
                default:
                    throw new CodingException("unsupported message encoding (" + msgEncoding + ')');
            }
        }
        return numFields * 8;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0098, code lost:
        r9 = ((r5 * 8) - 5) / 7;
     */
    private static void decodeCmasUserData(BearerData bData, int serviceCategory) throws BitwiseInputStream.AccessException, CodingException {
        int numFields;
        BearerData bearerData = bData;
        BitwiseInputStream inStream = new BitwiseInputStream(bearerData.userData.payload);
        if (inStream.available() >= 8) {
            if (inStream.read(8) == 0) {
                int messageClass = serviceCategoryToCmasMessageClass(serviceCategory);
                int category = -1;
                int responseType = -1;
                int severity = -1;
                int urgency = -1;
                int certainty = -1;
                while (inStream.available() >= 16) {
                    int recordType = inStream.read(8);
                    int recordLen = inStream.read(8);
                    switch (recordType) {
                        case 0:
                            UserData alertUserData = new UserData();
                            alertUserData.msgEncoding = inStream.read(5);
                            alertUserData.msgEncodingSet = true;
                            alertUserData.msgType = 0;
                            int i = alertUserData.msgEncoding;
                            if (i != 0) {
                                switch (i) {
                                    case 2:
                                    case 3:
                                        break;
                                    case 4:
                                        numFields = (recordLen - 1) / 2;
                                        break;
                                    default:
                                        switch (i) {
                                            case 8:
                                                break;
                                            case 9:
                                                break;
                                            default:
                                                numFields = 0;
                                                break;
                                        }
                                }
                            }
                            numFields = recordLen - 1;
                            alertUserData.numFields = numFields;
                            alertUserData.payload = inStream.readByteArray((recordLen * 8) - 5);
                            decodeUserDataPayload(alertUserData, false);
                            bearerData.userData = alertUserData;
                            break;
                        case 1:
                            category = inStream.read(8);
                            responseType = inStream.read(8);
                            severity = inStream.read(4);
                            urgency = inStream.read(4);
                            int certainty2 = inStream.read(4);
                            inStream.skip((recordLen * 8) - 28);
                            certainty = certainty2;
                            break;
                        default:
                            Rlog.w(LOG_TAG, "skipping unsupported CMAS record type " + recordType);
                            inStream.skip(recordLen * 8);
                            break;
                    }
                }
                SmsCbCmasInfo smsCbCmasInfo = new SmsCbCmasInfo(messageClass, category, responseType, severity, urgency, certainty);
                bearerData.cmasWarningInfo = smsCbCmasInfo;
                return;
            }
            throw new CodingException("unsupported CMAE_protocol_version " + protocolVersion);
        }
        throw new CodingException("emergency CB with no CMAE_protocol_version");
    }

    public static BearerData decode(byte[] smsData) {
        return decode(smsData, 0);
    }

    private static boolean isCmasAlertCategory(int category) {
        return category >= 4096 && category <= 4351;
    }

    public static BearerData decode(byte[] smsData, int serviceCategory) {
        boolean decodeSuccess;
        try {
            BitwiseInputStream inStream = new BitwiseInputStream(smsData);
            BearerData bData = new BearerData();
            int foundSubparamMask = 0;
            while (inStream.available() > 0) {
                int subparamId = inStream.read(8);
                int subparamIdBit = 1 << subparamId;
                if ((foundSubparamMask & subparamIdBit) != 0 && subparamId >= 0) {
                    if (subparamId <= 23) {
                        throw new CodingException("illegal duplicate subparameter (" + subparamId + ")");
                    }
                }
                switch (subparamId) {
                    case 0:
                        decodeSuccess = decodeMessageId(bData, inStream);
                        break;
                    case 1:
                        decodeSuccess = decodeUserData(bData, inStream);
                        break;
                    case 2:
                        decodeSuccess = decodeUserResponseCode(bData, inStream);
                        break;
                    case 3:
                        decodeSuccess = decodeMsgCenterTimeStamp(bData, inStream);
                        break;
                    case 4:
                        decodeSuccess = decodeValidityAbs(bData, inStream);
                        break;
                    case 5:
                        decodeSuccess = decodeValidityRel(bData, inStream);
                        break;
                    case 6:
                        decodeSuccess = decodeDeferredDeliveryAbs(bData, inStream);
                        break;
                    case 7:
                        decodeSuccess = decodeDeferredDeliveryRel(bData, inStream);
                        break;
                    case 8:
                        decodeSuccess = decodePriorityIndicator(bData, inStream);
                        break;
                    case 9:
                        decodeSuccess = decodePrivacyIndicator(bData, inStream);
                        break;
                    case 10:
                        decodeSuccess = decodeReplyOption(bData, inStream);
                        break;
                    case 11:
                        decodeSuccess = decodeMsgCount(bData, inStream);
                        break;
                    case 12:
                        decodeSuccess = decodeMsgDeliveryAlert(bData, inStream);
                        break;
                    case 13:
                        decodeSuccess = decodeLanguageIndicator(bData, inStream);
                        break;
                    case 14:
                        decodeSuccess = decodeCallbackNumber(bData, inStream);
                        break;
                    case 15:
                        decodeSuccess = decodeDisplayMode(bData, inStream);
                        break;
                    case 17:
                        decodeSuccess = decodeDepositIndex(bData, inStream);
                        break;
                    case 18:
                        decodeSuccess = decodeServiceCategoryProgramData(bData, inStream);
                        break;
                    case 20:
                        decodeSuccess = decodeMsgStatus(bData, inStream);
                        break;
                    default:
                        decodeSuccess = decodeReserved(bData, inStream, subparamId);
                        break;
                }
                if (decodeSuccess && subparamId >= 0 && subparamId <= 23) {
                    foundSubparamMask |= subparamIdBit;
                }
            }
            if ((foundSubparamMask & 1) != 0) {
                if (bData.userData != null) {
                    if (isCmasAlertCategory(serviceCategory)) {
                        decodeCmasUserData(bData, serviceCategory);
                    } else if (bData.userData.msgEncoding == 1) {
                        if (((foundSubparamMask ^ 1) ^ 2) != 0) {
                            Rlog.e(LOG_TAG, "IS-91 must occur without extra subparams (" + foundSubparamMask + ")");
                        }
                        decodeIs91(bData);
                    } else {
                        decodeUserDataPayload(bData.userData, bData.hasUserDataHeader);
                    }
                }
                return bData;
            }
            throw new CodingException("missing MESSAGE_IDENTIFIER subparam");
        } catch (BitwiseInputStream.AccessException ex) {
            Rlog.e(LOG_TAG, "BearerData decode failed: " + ex);
            return null;
        } catch (CodingException ex2) {
            Rlog.e(LOG_TAG, "BearerData decode failed: " + ex2);
            return null;
        }
    }
}
