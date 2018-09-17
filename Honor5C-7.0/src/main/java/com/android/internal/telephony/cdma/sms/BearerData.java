package com.android.internal.telephony.cdma.sms;

import android.content.res.Resources;
import android.telephony.Rlog;
import android.telephony.SmsCbCmasInfo;
import android.telephony.cdma.CdmaSmsCbProgramData;
import android.telephony.cdma.CdmaSmsCbProgramResults;
import android.text.format.Time;
import com.android.internal.telephony.EncodeException;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.GsmAlphabet.TextEncodingDetails;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.cdma.SignalToneUtil;
import com.android.internal.telephony.gsm.SmsCbConstants;
import com.android.internal.telephony.gsm.SmsMessage;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.util.BitwiseInputStream;
import com.android.internal.util.BitwiseOutputStream;
import com.android.internal.util.BitwiseOutputStream.AccessException;
import com.google.android.mms.pdu.CharacterSets;
import com.google.android.mms.pdu.PduHeaders;
import com.google.android.mms.pdu.PduPart;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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
    private static final byte SUBPARAM_ALERT_ON_MESSAGE_DELIVERY = (byte) 12;
    private static final byte SUBPARAM_CALLBACK_NUMBER = (byte) 14;
    private static final byte SUBPARAM_DEFERRED_DELIVERY_TIME_ABSOLUTE = (byte) 6;
    private static final byte SUBPARAM_DEFERRED_DELIVERY_TIME_RELATIVE = (byte) 7;
    private static final byte SUBPARAM_ID_LAST_DEFINED = (byte) 23;
    private static final byte SUBPARAM_LANGUAGE_INDICATOR = (byte) 13;
    private static final byte SUBPARAM_MESSAGE_CENTER_TIME_STAMP = (byte) 3;
    private static final byte SUBPARAM_MESSAGE_DEPOSIT_INDEX = (byte) 17;
    private static final byte SUBPARAM_MESSAGE_DISPLAY_MODE = (byte) 15;
    private static final byte SUBPARAM_MESSAGE_IDENTIFIER = (byte) 0;
    private static final byte SUBPARAM_MESSAGE_STATUS = (byte) 20;
    private static final byte SUBPARAM_NUMBER_OF_MESSAGES = (byte) 11;
    private static final byte SUBPARAM_PRIORITY_INDICATOR = (byte) 8;
    private static final byte SUBPARAM_PRIVACY_INDICATOR = (byte) 9;
    private static final byte SUBPARAM_REPLY_OPTION = (byte) 10;
    private static final byte SUBPARAM_SERVICE_CATEGORY_PROGRAM_DATA = (byte) 18;
    private static final byte SUBPARAM_SERVICE_CATEGORY_PROGRAM_RESULTS = (byte) 19;
    private static final byte SUBPARAM_USER_DATA = (byte) 1;
    private static final byte SUBPARAM_USER_RESPONSE_CODE = (byte) 2;
    private static final byte SUBPARAM_VALIDITY_PERIOD_ABSOLUTE = (byte) 4;
    private static final byte SUBPARAM_VALIDITY_PERIOD_RELATIVE = (byte) 5;
    public int alert;
    public boolean alertIndicatorSet;
    public CdmaSmsAddress callbackNumber;
    public SmsCbCmasInfo cmasWarningInfo;
    public TimeStamp deferredDeliveryTimeAbsolute;
    public int deferredDeliveryTimeRelative;
    public boolean deferredDeliveryTimeRelativeSet;
    public boolean deliveryAckReq;
    public int depositIndex;
    public int displayMode;
    public boolean displayModeSet;
    public int errorClass;
    public boolean hasUserDataHeader;
    public int language;
    public boolean languageIndicatorSet;
    public int messageId;
    public int messageStatus;
    public boolean messageStatusSet;
    public int messageType;
    public TimeStamp msgCenterTimeStamp;
    public int numberOfMessages;
    public int priority;
    public boolean priorityIndicatorSet;
    public int privacy;
    public boolean privacyIndicatorSet;
    public boolean readAckReq;
    public boolean reportReq;
    public ArrayList<CdmaSmsCbProgramData> serviceCategoryProgramData;
    public ArrayList<CdmaSmsCbProgramResults> serviceCategoryProgramResults;
    public boolean userAckReq;
    public UserData userData;
    public int userResponseCode;
    public boolean userResponseCodeSet;
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
            int year = IccUtils.cdmaBcdByteToInt(data[BearerData.STATUS_ACCEPTED]);
            if (year > 99 || year < 0) {
                return null;
            }
            ts.year = year >= 96 ? year + 1900 : year + ServiceStateTracker.NITZ_UPDATE_DIFF_DEFAULT;
            int month = IccUtils.cdmaBcdByteToInt(data[BearerData.STATUS_DEPOSITED_TO_INTERNET]);
            if (month < BearerData.STATUS_DEPOSITED_TO_INTERNET || month > 12) {
                return null;
            }
            ts.month = month - 1;
            int day = IccUtils.cdmaBcdByteToInt(data[BearerData.STATUS_DELIVERED]);
            if (day < BearerData.STATUS_DEPOSITED_TO_INTERNET || day > BearerData.STATUS_UNKNOWN_ERROR) {
                return null;
            }
            ts.monthDay = day;
            int hour = IccUtils.cdmaBcdByteToInt(data[BearerData.STATUS_CANCELLED]);
            if (hour < 0 || hour > 23) {
                return null;
            }
            ts.hour = hour;
            int minute = IccUtils.cdmaBcdByteToInt(data[BearerData.STATUS_NETWORK_CONGESTION]);
            if (minute < 0 || minute > 59) {
                return null;
            }
            ts.minute = minute;
            int second = IccUtils.cdmaBcdByteToInt(data[BearerData.STATUS_NETWORK_ERROR]);
            if (second < 0 || second > 59) {
                return null;
            }
            ts.second = second;
            return ts;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("TimeStamp ");
            builder.append("{ year=").append(this.year);
            builder.append(", month=").append(this.month);
            builder.append(", day=").append(this.monthDay);
            builder.append(", hour=").append(this.hour);
            builder.append(", minute=").append(this.minute);
            builder.append(", second=").append(this.second);
            builder.append(" }");
            return builder.toString();
        }
    }

    public BearerData() {
        this.priorityIndicatorSet = false;
        this.priority = STATUS_ACCEPTED;
        this.privacyIndicatorSet = false;
        this.privacy = STATUS_ACCEPTED;
        this.alertIndicatorSet = false;
        this.alert = STATUS_ACCEPTED;
        this.displayModeSet = false;
        this.displayMode = STATUS_DEPOSITED_TO_INTERNET;
        this.languageIndicatorSet = false;
        this.language = STATUS_ACCEPTED;
        this.messageStatusSet = false;
        this.errorClass = STATUS_UNDEFINED;
        this.messageStatus = STATUS_UNDEFINED;
        this.userResponseCodeSet = false;
    }

    public String getLanguage() {
        return getLanguageCodeForValue(this.language);
    }

    private static String getLanguageCodeForValue(int languageValue) {
        switch (languageValue) {
            case STATUS_DEPOSITED_TO_INTERNET /*1*/:
                return "en";
            case STATUS_DELIVERED /*2*/:
                return "fr";
            case STATUS_CANCELLED /*3*/:
                return "es";
            case STATUS_NETWORK_CONGESTION /*4*/:
                return "ja";
            case STATUS_NETWORK_ERROR /*5*/:
                return "ko";
            case STATUS_CANCEL_FAILED /*6*/:
                return "zh";
            case STATUS_BLOCKED_DESTINATION /*7*/:
                return "he";
            default:
                return null;
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BearerData ");
        builder.append("{ messageType=").append(this.messageType);
        builder.append(", messageId=").append(this.messageId);
        builder.append(", priority=").append(this.priorityIndicatorSet ? Integer.valueOf(this.priority) : "unset");
        builder.append(", privacy=").append(this.privacyIndicatorSet ? Integer.valueOf(this.privacy) : "unset");
        builder.append(", alert=").append(this.alertIndicatorSet ? Integer.valueOf(this.alert) : "unset");
        builder.append(", displayMode=").append(this.displayModeSet ? Integer.valueOf(this.displayMode) : "unset");
        builder.append(", language=").append(this.languageIndicatorSet ? Integer.valueOf(this.language) : "unset");
        builder.append(", errorClass=").append(this.messageStatusSet ? Integer.valueOf(this.errorClass) : "unset");
        builder.append(", msgStatus=").append(this.messageStatusSet ? Integer.valueOf(this.messageStatus) : "unset");
        builder.append(", msgCenterTimeStamp=").append(this.msgCenterTimeStamp != null ? this.msgCenterTimeStamp : "unset");
        builder.append(", validityPeriodAbsolute=").append(this.validityPeriodAbsolute != null ? this.validityPeriodAbsolute : "unset");
        builder.append(", validityPeriodRelative=").append(this.validityPeriodRelativeSet ? Integer.valueOf(this.validityPeriodRelative) : "unset");
        builder.append(", deferredDeliveryTimeAbsolute=").append(this.deferredDeliveryTimeAbsolute != null ? this.deferredDeliveryTimeAbsolute : "unset");
        builder.append(", deferredDeliveryTimeRelative=").append(this.deferredDeliveryTimeRelativeSet ? Integer.valueOf(this.deferredDeliveryTimeRelative) : "unset");
        builder.append(", userAckReq=").append(this.userAckReq);
        builder.append(", deliveryAckReq=").append(this.deliveryAckReq);
        builder.append(", readAckReq=").append(this.readAckReq);
        builder.append(", reportReq=").append(this.reportReq);
        builder.append(", numberOfMessages=").append(this.numberOfMessages);
        builder.append(", callbackNumber=").append(this.callbackNumber);
        builder.append(", depositIndex=").append(this.depositIndex);
        builder.append(", hasUserDataHeader=").append(this.hasUserDataHeader);
        builder.append(", userData=").append(this.userData);
        builder.append(" }");
        return builder.toString();
    }

    private static void encodeMessageId(BearerData bData, BitwiseOutputStream outStream) throws AccessException {
        outStream.write(STATUS_TEXT_TOO_LONG, STATUS_CANCELLED);
        outStream.write(STATUS_NETWORK_CONGESTION, bData.messageType);
        outStream.write(STATUS_TEXT_TOO_LONG, bData.messageId >> STATUS_TEXT_TOO_LONG);
        outStream.write(STATUS_TEXT_TOO_LONG, bData.messageId);
        outStream.write(STATUS_DEPOSITED_TO_INTERNET, bData.hasUserDataHeader ? STATUS_DEPOSITED_TO_INTERNET : STATUS_ACCEPTED);
        outStream.skip(STATUS_CANCELLED);
    }

    private static int countAsciiSeptets(CharSequence msg, boolean force) {
        int msgLen = msg.length();
        if (force) {
            return msgLen;
        }
        for (int i = STATUS_ACCEPTED; i < msgLen; i += STATUS_DEPOSITED_TO_INTERNET) {
            if (UserData.charToAscii.get(msg.charAt(i), -1) == -1) {
                return -1;
            }
        }
        return msgLen;
    }

    public static TextEncodingDetails calcTextEncodingDetails(CharSequence msg, boolean force7BitEncoding, boolean isEntireMsg) {
        TextEncodingDetails ted;
        int septets = countAsciiSeptets(msg, force7BitEncoding);
        if (septets == -1 || septets > PduHeaders.PREVIOUSLY_SENT_BY) {
            ted = SmsMessage.calculateLength(msg, force7BitEncoding);
            if (ted.msgCount == STATUS_DEPOSITED_TO_INTERNET && ted.codeUnitSize == STATUS_DEPOSITED_TO_INTERNET && isEntireMsg) {
                return SmsMessageBase.calcUnicodeEncodingDetails(msg);
            }
        }
        ted = new TextEncodingDetails();
        ted.msgCount = STATUS_DEPOSITED_TO_INTERNET;
        ted.codeUnitCount = septets;
        ted.codeUnitsRemaining = 160 - septets;
        ted.codeUnitSize = STATUS_DEPOSITED_TO_INTERNET;
        return ted;
    }

    public static byte[] encode7bitAscii(String msg, int septetOffset, boolean force) throws CodingException {
        try {
            int i;
            BitwiseOutputStream outStream = new BitwiseOutputStream(msg.length() + septetOffset);
            int msgLen = msg.length();
            for (i = STATUS_ACCEPTED; i < septetOffset; i += STATUS_DEPOSITED_TO_INTERNET) {
                outStream.write(STATUS_BLOCKED_DESTINATION, STATUS_ACCEPTED);
            }
            for (i = STATUS_ACCEPTED; i < msgLen; i += STATUS_DEPOSITED_TO_INTERNET) {
                int charCode = UserData.charToAscii.get(msg.charAt(i), -1);
                if (charCode != -1) {
                    outStream.write(STATUS_BLOCKED_DESTINATION, charCode);
                } else if (force) {
                    outStream.write(STATUS_BLOCKED_DESTINATION, 32);
                } else {
                    throw new CodingException("cannot ASCII encode (" + msg.charAt(i) + ")");
                }
            }
            return outStream.toByteArray();
        } catch (AccessException ex) {
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
        boolean z = false;
        if (!force) {
            z = true;
        }
        try {
            byte[] fullData = GsmAlphabet.stringToGsm7BitPacked(msg, septetOffset, z, STATUS_ACCEPTED, STATUS_ACCEPTED);
            Gsm7bitCodingResult result = new Gsm7bitCodingResult();
            result.data = new byte[(fullData.length - 1)];
            System.arraycopy(fullData, STATUS_DEPOSITED_TO_INTERNET, result.data, STATUS_ACCEPTED, fullData.length - 1);
            result.septets = fullData[STATUS_ACCEPTED] & STATUS_UNDEFINED;
            return result;
        } catch (EncodeException ex) {
            throw new CodingException("7bit GSM encode failed: " + ex);
        }
    }

    private static void encode7bitEms(UserData uData, byte[] udhData, boolean force) throws CodingException {
        Gsm7bitCodingResult gcr = encode7bitGsm(uData.payloadStr, (((udhData.length + STATUS_DEPOSITED_TO_INTERNET) * STATUS_TEXT_TOO_LONG) + STATUS_CANCEL_FAILED) / STATUS_BLOCKED_DESTINATION, force);
        uData.msgEncoding = STATUS_DUPLICATE_MESSAGE;
        uData.msgEncodingSet = true;
        uData.numFields = gcr.septets;
        uData.payload = gcr.data;
        uData.payload[STATUS_ACCEPTED] = (byte) udhData.length;
        System.arraycopy(udhData, STATUS_ACCEPTED, uData.payload, STATUS_DEPOSITED_TO_INTERNET, udhData.length);
    }

    private static void encode16bitEms(UserData uData, byte[] udhData) throws CodingException {
        byte[] payload = encodeUtf16(uData.payloadStr);
        int udhBytes = udhData.length + STATUS_DEPOSITED_TO_INTERNET;
        int udhCodeUnits = (udhBytes + STATUS_DEPOSITED_TO_INTERNET) / STATUS_DELIVERED;
        int payloadCodeUnits = payload.length / STATUS_DELIVERED;
        uData.msgEncoding = STATUS_NETWORK_CONGESTION;
        uData.msgEncodingSet = true;
        uData.numFields = udhCodeUnits + payloadCodeUnits;
        uData.payload = new byte[(uData.numFields * STATUS_DELIVERED)];
        uData.payload[STATUS_ACCEPTED] = (byte) udhData.length;
        System.arraycopy(udhData, STATUS_ACCEPTED, uData.payload, STATUS_DEPOSITED_TO_INTERNET, udhData.length);
        System.arraycopy(payload, STATUS_ACCEPTED, uData.payload, udhBytes, payload.length);
    }

    private static void encodeEmsUserDataPayload(UserData uData) throws CodingException {
        byte[] headerData = SmsHeader.toByteArray(uData.userDataHeader);
        if (headerData == null) {
            Rlog.e(LOG_TAG, "user data with null headerData");
            return;
        }
        if (!uData.msgEncodingSet) {
            try {
                if (!HwTelephonyFactory.getHwInnerSmsManager().encode7bitMultiSms(uData, headerData, false)) {
                    throw new CodingException("7bit multi sms encoding failed");
                }
            } catch (CodingException e) {
                encode16bitEms(uData, headerData);
            }
        } else if (uData.msgEncoding == STATUS_DUPLICATE_MESSAGE) {
            encode7bitEms(uData, headerData, true);
        } else if (uData.msgEncoding == STATUS_DELIVERED) {
            HwTelephonyFactory.getHwInnerSmsManager().encode7bitMultiSms(uData, headerData, true);
        } else if (uData.msgEncoding == STATUS_NETWORK_CONGESTION) {
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
                uData.payload = encode7bitAscii(uData.payloadStr, STATUS_ACCEPTED, false);
                uData.msgEncoding = STATUS_DELIVERED;
            } catch (CodingException e) {
                uData.payload = encodeUtf16(uData.payloadStr);
                uData.msgEncoding = STATUS_NETWORK_CONGESTION;
            }
            uData.numFields = uData.payloadStr.length();
            uData.msgEncodingSet = true;
        } else if (uData.msgEncoding != 0) {
            if (uData.payloadStr == null) {
                Rlog.e(LOG_TAG, "non-octet user data with null payloadStr");
                uData.payloadStr = "";
            }
            if (uData.msgEncoding == STATUS_DUPLICATE_MESSAGE) {
                Gsm7bitCodingResult gcr = encode7bitGsm(uData.payloadStr, STATUS_ACCEPTED, true);
                uData.payload = gcr.data;
                uData.numFields = gcr.septets;
            } else if (uData.msgEncoding == STATUS_DELIVERED) {
                uData.payload = encode7bitAscii(uData.payloadStr, STATUS_ACCEPTED, true);
                uData.numFields = uData.payloadStr.length();
            } else if (uData.msgEncoding == STATUS_NETWORK_CONGESTION) {
                uData.payload = encodeUtf16(uData.payloadStr);
                uData.numFields = uData.payloadStr.length();
            } else if (uData.msgEncoding == STATUS_NETWORK_ERROR) {
                uData.payload = encodeShiftJis(uData.payloadStr);
                uData.numFields = uData.payload.length;
            } else {
                throw new CodingException("unsupported user data encoding (" + uData.msgEncoding + ")");
            }
        } else if (uData.payload == null) {
            Rlog.e(LOG_TAG, "user data with octet encoding but null payload");
            uData.payload = new byte[STATUS_ACCEPTED];
            uData.numFields = STATUS_ACCEPTED;
        } else {
            uData.numFields = uData.payload.length;
        }
    }

    private static void encodeUserData(BearerData bData, BitwiseOutputStream outStream) throws AccessException, CodingException {
        boolean z;
        encodeUserDataPayload(bData.userData);
        if (bData.userData.userDataHeader != null) {
            z = true;
        } else {
            z = false;
        }
        bData.hasUserDataHeader = z;
        if (bData.userData.payload.length > PduPart.P_DEP_COMMENT) {
            throw new CodingException("encoded user data too large (" + bData.userData.payload.length + " > " + PduPart.P_DEP_COMMENT + " bytes)");
        }
        int i;
        int dataBits = (bData.userData.payload.length * STATUS_TEXT_TOO_LONG) - bData.userData.paddingBits;
        int paramBits = dataBits + STATUS_MESSAGE_EXPIRED;
        if (bData.userData.msgEncoding == STATUS_DEPOSITED_TO_INTERNET || bData.userData.msgEncoding == STATUS_INVALID_DESTINATION) {
            paramBits += STATUS_TEXT_TOO_LONG;
        }
        int i2 = paramBits / STATUS_TEXT_TOO_LONG;
        if (paramBits % STATUS_TEXT_TOO_LONG > 0) {
            i = STATUS_DEPOSITED_TO_INTERNET;
        } else {
            i = STATUS_ACCEPTED;
        }
        int paramBytes = i2 + i;
        int paddingBits = (paramBytes * STATUS_TEXT_TOO_LONG) - paramBits;
        outStream.write(STATUS_TEXT_TOO_LONG, paramBytes);
        outStream.write(STATUS_NETWORK_ERROR, bData.userData.msgEncoding);
        if (bData.userData.msgEncoding == STATUS_DEPOSITED_TO_INTERNET || bData.userData.msgEncoding == STATUS_INVALID_DESTINATION) {
            outStream.write(STATUS_TEXT_TOO_LONG, bData.userData.msgType);
        }
        outStream.write(STATUS_TEXT_TOO_LONG, bData.userData.numFields);
        outStream.writeByteArray(dataBits, bData.userData.payload);
        if (paddingBits > 0) {
            outStream.write(paddingBits, STATUS_ACCEPTED);
        }
    }

    private static void encodeReplyOption(BearerData bData, BitwiseOutputStream outStream) throws AccessException {
        int i;
        outStream.write(STATUS_TEXT_TOO_LONG, STATUS_DEPOSITED_TO_INTERNET);
        if (bData.userAckReq) {
            i = STATUS_DEPOSITED_TO_INTERNET;
        } else {
            i = STATUS_ACCEPTED;
        }
        outStream.write(STATUS_DEPOSITED_TO_INTERNET, i);
        if (bData.deliveryAckReq) {
            i = STATUS_DEPOSITED_TO_INTERNET;
        } else {
            i = STATUS_ACCEPTED;
        }
        outStream.write(STATUS_DEPOSITED_TO_INTERNET, i);
        if (bData.readAckReq) {
            i = STATUS_DEPOSITED_TO_INTERNET;
        } else {
            i = STATUS_ACCEPTED;
        }
        outStream.write(STATUS_DEPOSITED_TO_INTERNET, i);
        if (bData.reportReq) {
            i = STATUS_DEPOSITED_TO_INTERNET;
        } else {
            i = STATUS_ACCEPTED;
        }
        outStream.write(STATUS_DEPOSITED_TO_INTERNET, i);
        outStream.write(STATUS_NETWORK_CONGESTION, STATUS_ACCEPTED);
    }

    private static byte[] encodeDtmfSmsAddress(String address) {
        int i = STATUS_ACCEPTED;
        int digits = address.length();
        int dataBits = digits * STATUS_NETWORK_CONGESTION;
        int dataBytes = dataBits / STATUS_TEXT_TOO_LONG;
        if (dataBits % STATUS_TEXT_TOO_LONG > 0) {
            i = STATUS_DEPOSITED_TO_INTERNET;
        }
        byte[] rawData = new byte[(dataBytes + i)];
        for (int i2 = STATUS_ACCEPTED; i2 < digits; i2 += STATUS_DEPOSITED_TO_INTERNET) {
            int val;
            char c = address.charAt(i2);
            if (c >= '1' && c <= '9') {
                val = c - 48;
            } else if (c == '0') {
                val = STATUS_INVALID_DESTINATION;
            } else if (c == '*') {
                val = 11;
            } else if (c != '#') {
                return null;
            } else {
                val = 12;
            }
            i = i2 / STATUS_DELIVERED;
            rawData[i] = (byte) (rawData[i] | (val << (4 - ((i2 % STATUS_DELIVERED) * STATUS_NETWORK_CONGESTION))));
        }
        return rawData;
    }

    private static void encodeCdmaSmsAddress(CdmaSmsAddress addr) throws CodingException {
        if (addr.digitMode == STATUS_DEPOSITED_TO_INTERNET) {
            try {
                addr.origBytes = addr.address.getBytes("US-ASCII");
                return;
            } catch (UnsupportedEncodingException e) {
                throw new CodingException("invalid SMS address, cannot convert to ASCII");
            }
        }
        addr.origBytes = encodeDtmfSmsAddress(addr.address);
    }

    private static void encodeCallbackNumber(BearerData bData, BitwiseOutputStream outStream) throws AccessException, CodingException {
        int dataBits;
        int i;
        CdmaSmsAddress addr = bData.callbackNumber;
        encodeCdmaSmsAddress(addr);
        int paramBits = STATUS_DUPLICATE_MESSAGE;
        if (addr.digitMode == STATUS_DEPOSITED_TO_INTERNET) {
            paramBits = 16;
            dataBits = addr.numberOfDigits * STATUS_TEXT_TOO_LONG;
        } else {
            dataBits = addr.numberOfDigits * STATUS_NETWORK_CONGESTION;
        }
        paramBits += dataBits;
        int i2 = paramBits / STATUS_TEXT_TOO_LONG;
        if (paramBits % STATUS_TEXT_TOO_LONG > 0) {
            i = STATUS_DEPOSITED_TO_INTERNET;
        } else {
            i = STATUS_ACCEPTED;
        }
        int paramBytes = i2 + i;
        int paddingBits = (paramBytes * STATUS_TEXT_TOO_LONG) - paramBits;
        outStream.write(STATUS_TEXT_TOO_LONG, paramBytes);
        outStream.write(STATUS_DEPOSITED_TO_INTERNET, addr.digitMode);
        if (addr.digitMode == STATUS_DEPOSITED_TO_INTERNET) {
            outStream.write(STATUS_CANCELLED, addr.ton);
            outStream.write(STATUS_NETWORK_CONGESTION, addr.numberPlan);
        }
        outStream.write(STATUS_TEXT_TOO_LONG, addr.numberOfDigits);
        outStream.writeByteArray(dataBits, addr.origBytes);
        if (paddingBits > 0) {
            outStream.write(paddingBits, STATUS_ACCEPTED);
        }
    }

    private static void encodeMsgStatus(BearerData bData, BitwiseOutputStream outStream) throws AccessException {
        outStream.write(STATUS_TEXT_TOO_LONG, STATUS_DEPOSITED_TO_INTERNET);
        outStream.write(STATUS_DELIVERED, bData.errorClass);
        outStream.write(STATUS_CANCEL_FAILED, bData.messageStatus);
    }

    private static void encodeMsgCount(BearerData bData, BitwiseOutputStream outStream) throws AccessException {
        outStream.write(STATUS_TEXT_TOO_LONG, STATUS_DEPOSITED_TO_INTERNET);
        outStream.write(STATUS_TEXT_TOO_LONG, bData.numberOfMessages);
    }

    private static void encodeValidityPeriodRel(BearerData bData, BitwiseOutputStream outStream) throws AccessException {
        outStream.write(STATUS_TEXT_TOO_LONG, STATUS_DEPOSITED_TO_INTERNET);
        outStream.write(STATUS_TEXT_TOO_LONG, bData.validityPeriodRelative);
    }

    private static void encodePrivacyIndicator(BearerData bData, BitwiseOutputStream outStream) throws AccessException {
        outStream.write(STATUS_TEXT_TOO_LONG, STATUS_DEPOSITED_TO_INTERNET);
        outStream.write(STATUS_DELIVERED, bData.privacy);
        outStream.skip(STATUS_CANCEL_FAILED);
    }

    private static void encodeLanguageIndicator(BearerData bData, BitwiseOutputStream outStream) throws AccessException {
        outStream.write(STATUS_TEXT_TOO_LONG, STATUS_DEPOSITED_TO_INTERNET);
        outStream.write(STATUS_TEXT_TOO_LONG, bData.language);
    }

    private static void encodeDisplayMode(BearerData bData, BitwiseOutputStream outStream) throws AccessException {
        outStream.write(STATUS_TEXT_TOO_LONG, STATUS_DEPOSITED_TO_INTERNET);
        outStream.write(STATUS_DELIVERED, bData.displayMode);
        outStream.skip(STATUS_CANCEL_FAILED);
    }

    private static void encodePriorityIndicator(BearerData bData, BitwiseOutputStream outStream) throws AccessException {
        outStream.write(STATUS_TEXT_TOO_LONG, STATUS_DEPOSITED_TO_INTERNET);
        outStream.write(STATUS_DELIVERED, bData.priority);
        outStream.skip(STATUS_CANCEL_FAILED);
    }

    private static void encodeMsgDeliveryAlert(BearerData bData, BitwiseOutputStream outStream) throws AccessException {
        outStream.write(STATUS_TEXT_TOO_LONG, STATUS_DEPOSITED_TO_INTERNET);
        outStream.write(STATUS_DELIVERED, bData.alert);
        outStream.skip(STATUS_CANCEL_FAILED);
    }

    private static void encodeScpResults(BearerData bData, BitwiseOutputStream outStream) throws AccessException {
        ArrayList<CdmaSmsCbProgramResults> results = bData.serviceCategoryProgramResults;
        outStream.write(STATUS_TEXT_TOO_LONG, results.size() * STATUS_NETWORK_CONGESTION);
        for (CdmaSmsCbProgramResults result : results) {
            int category = result.getCategory();
            outStream.write(STATUS_TEXT_TOO_LONG, category >> STATUS_TEXT_TOO_LONG);
            outStream.write(STATUS_TEXT_TOO_LONG, category);
            outStream.write(STATUS_TEXT_TOO_LONG, result.getLanguage());
            outStream.write(STATUS_NETWORK_CONGESTION, result.getCategoryResult());
            outStream.skip(STATUS_NETWORK_CONGESTION);
        }
    }

    public static byte[] encode(BearerData bData) {
        boolean z = true;
        if (bData.userData == null) {
            z = false;
        } else if (bData.userData.userDataHeader == null) {
            z = false;
        }
        bData.hasUserDataHeader = z;
        try {
            BitwiseOutputStream outStream = new BitwiseOutputStream(PduPart.P_CONTENT_TRANSFER_ENCODING);
            outStream.write(STATUS_TEXT_TOO_LONG, STATUS_ACCEPTED);
            encodeMessageId(bData, outStream);
            if (bData.userData != null) {
                outStream.write(STATUS_TEXT_TOO_LONG, STATUS_DEPOSITED_TO_INTERNET);
                encodeUserData(bData, outStream);
            }
            if (bData.callbackNumber != null) {
                outStream.write(STATUS_TEXT_TOO_LONG, 14);
                encodeCallbackNumber(bData, outStream);
            }
            if (bData.userAckReq || bData.deliveryAckReq || bData.readAckReq || bData.reportReq) {
                outStream.write(STATUS_TEXT_TOO_LONG, STATUS_INVALID_DESTINATION);
                encodeReplyOption(bData, outStream);
            }
            if (bData.numberOfMessages != 0) {
                outStream.write(STATUS_TEXT_TOO_LONG, 11);
                encodeMsgCount(bData, outStream);
            }
            if (bData.validityPeriodRelativeSet) {
                outStream.write(STATUS_TEXT_TOO_LONG, STATUS_NETWORK_ERROR);
                encodeValidityPeriodRel(bData, outStream);
            }
            if (bData.privacyIndicatorSet) {
                outStream.write(STATUS_TEXT_TOO_LONG, STATUS_DUPLICATE_MESSAGE);
                encodePrivacyIndicator(bData, outStream);
            }
            if (bData.languageIndicatorSet) {
                outStream.write(STATUS_TEXT_TOO_LONG, STATUS_MESSAGE_EXPIRED);
                encodeLanguageIndicator(bData, outStream);
            }
            if (bData.displayModeSet) {
                outStream.write(STATUS_TEXT_TOO_LONG, 15);
                encodeDisplayMode(bData, outStream);
            }
            if (bData.priorityIndicatorSet) {
                outStream.write(STATUS_TEXT_TOO_LONG, STATUS_TEXT_TOO_LONG);
                encodePriorityIndicator(bData, outStream);
            }
            if (bData.alertIndicatorSet) {
                outStream.write(STATUS_TEXT_TOO_LONG, 12);
                encodeMsgDeliveryAlert(bData, outStream);
            }
            if (bData.messageStatusSet) {
                outStream.write(STATUS_TEXT_TOO_LONG, 20);
                encodeMsgStatus(bData, outStream);
            }
            if (bData.serviceCategoryProgramResults != null) {
                outStream.write(STATUS_TEXT_TOO_LONG, 19);
                encodeScpResults(bData, outStream);
            }
            HwTelephonyFactory.getHwInnerSmsManager().encodeMsgCenterTimeStampCheck(bData, outStream);
            return outStream.toByteArray();
        } catch (AccessException ex) {
            Rlog.e(LOG_TAG, "BearerData encode failed: " + ex);
            return null;
        } catch (CodingException ex2) {
            Rlog.e(LOG_TAG, "BearerData encode failed: " + ex2);
            return null;
        }
    }

    private static boolean decodeMessageId(BearerData bData, BitwiseInputStream inStream) throws BitwiseInputStream.AccessException {
        boolean z = true;
        boolean decodeSuccess = false;
        int paramBits = inStream.read(STATUS_TEXT_TOO_LONG) * STATUS_TEXT_TOO_LONG;
        if (paramBits >= 24) {
            paramBits -= 24;
            decodeSuccess = true;
            bData.messageType = inStream.read(STATUS_NETWORK_CONGESTION);
            bData.messageId = inStream.read(STATUS_TEXT_TOO_LONG) << STATUS_TEXT_TOO_LONG;
            bData.messageId |= inStream.read(STATUS_TEXT_TOO_LONG);
            if (inStream.read(STATUS_DEPOSITED_TO_INTERNET) != STATUS_DEPOSITED_TO_INTERNET) {
                z = false;
            }
            bData.hasUserDataHeader = z;
            inStream.skip(STATUS_CANCELLED);
        }
        if (!decodeSuccess || paramBits > 0) {
            Rlog.d(LOG_TAG, "MESSAGE_IDENTIFIER decode " + (decodeSuccess ? "succeeded" : "failed") + " (extra bits = " + paramBits + ")");
        }
        inStream.skip(paramBits);
        return decodeSuccess;
    }

    private static boolean decodeReserved(BearerData bData, BitwiseInputStream inStream, int subparamId) throws BitwiseInputStream.AccessException, CodingException {
        boolean decodeSuccess = false;
        int subparamLen = inStream.read(STATUS_TEXT_TOO_LONG);
        int paramBits = subparamLen * STATUS_TEXT_TOO_LONG;
        if (paramBits <= inStream.available()) {
            decodeSuccess = true;
            inStream.skip(paramBits);
        }
        Rlog.d(LOG_TAG, "RESERVED bearer data subparameter " + subparamId + " decode " + (decodeSuccess ? "succeeded" : "failed") + " (param bits = " + paramBits + ")");
        if (decodeSuccess) {
            return decodeSuccess;
        }
        throw new CodingException("RESERVED bearer data subparameter " + subparamId + " had invalid SUBPARAM_LEN " + subparamLen);
    }

    private static boolean decodeUserData(BearerData bData, BitwiseInputStream inStream) throws BitwiseInputStream.AccessException {
        int paramBits = inStream.read(STATUS_TEXT_TOO_LONG) * STATUS_TEXT_TOO_LONG;
        bData.userData = new UserData();
        bData.userData.msgEncoding = inStream.read(STATUS_NETWORK_ERROR);
        bData.userData.msgEncodingSet = true;
        bData.userData.msgType = STATUS_ACCEPTED;
        int consumedBits = STATUS_NETWORK_ERROR;
        if (bData.userData.msgEncoding == STATUS_DEPOSITED_TO_INTERNET || bData.userData.msgEncoding == STATUS_INVALID_DESTINATION) {
            bData.userData.msgType = inStream.read(STATUS_TEXT_TOO_LONG);
            consumedBits = STATUS_MESSAGE_EXPIRED;
        }
        bData.userData.numFields = inStream.read(STATUS_TEXT_TOO_LONG);
        int dataBits = paramBits - (consumedBits + STATUS_TEXT_TOO_LONG);
        bData.userData.payload = inStream.readByteArray(dataBits);
        return true;
    }

    private static String decodeUtf8(byte[] data, int offset, int numFields) throws CodingException {
        return decodeCharset(data, offset, numFields, STATUS_DEPOSITED_TO_INTERNET, "UTF-8");
    }

    private static String decodeUtf16(byte[] data, int offset, int numFields) throws CodingException {
        return decodeCharset(data, offset, numFields - ((offset + (offset % STATUS_DELIVERED)) / STATUS_DELIVERED), STATUS_DELIVERED, "utf-16be");
    }

    private static String decodeCharset(byte[] data, int offset, int numFields, int width, String charset) throws CodingException {
        if (numFields < 0 || (numFields * width) + offset > data.length) {
            int maxNumFields = ((data.length - offset) - (offset % width)) / width;
            if (maxNumFields < 0) {
                throw new CodingException(charset + " decode failed: offset out of range");
            }
            Rlog.e(LOG_TAG, charset + " decode error: offset = " + offset + " numFields = " + numFields + " data.length = " + data.length + " maxNumFields = " + maxNumFields);
            numFields = maxNumFields;
        }
        try {
            return new String(data, offset, numFields * width, charset);
        } catch (UnsupportedEncodingException ex) {
            throw new CodingException(charset + " decode failed: " + ex);
        }
    }

    private static String decode7bitAscii(byte[] data, int offset, int numFields) throws CodingException {
        int offsetBits = offset * STATUS_TEXT_TOO_LONG;
        try {
            int offsetSeptets = (offsetBits + STATUS_CANCEL_FAILED) / STATUS_BLOCKED_DESTINATION;
            numFields -= offsetSeptets;
            int paddingBits = (offsetSeptets * STATUS_BLOCKED_DESTINATION) - offsetBits;
            StringBuffer strBuf = new StringBuffer(numFields);
            BitwiseInputStream inStream = new BitwiseInputStream(data);
            int wantedBits = (offsetSeptets * STATUS_BLOCKED_DESTINATION) + (numFields * STATUS_BLOCKED_DESTINATION);
            if (inStream.available() < wantedBits) {
                throw new CodingException("insufficient data (wanted " + wantedBits + " bits, but only have " + inStream.available() + ")");
            }
            inStream.skip(offsetBits + paddingBits);
            for (int i = STATUS_ACCEPTED; i < numFields; i += STATUS_DEPOSITED_TO_INTERNET) {
                int charCode = inStream.read(STATUS_BLOCKED_DESTINATION);
                if (charCode >= 32 && charCode <= UserData.ASCII_MAP_MAX_INDEX) {
                    strBuf.append(UserData.ASCII_MAP[charCode - 32]);
                } else if (charCode == STATUS_INVALID_DESTINATION) {
                    strBuf.append('\n');
                } else if (charCode == STATUS_MESSAGE_EXPIRED) {
                    strBuf.append('\r');
                } else {
                    strBuf.append(' ');
                }
            }
            return strBuf.toString();
        } catch (BitwiseInputStream.AccessException ex) {
            throw new CodingException("7bit ASCII decode failed: " + ex);
        }
    }

    private static String decode7bitGsm(byte[] data, int offset, int numFields) throws CodingException {
        int offsetBits = offset * STATUS_TEXT_TOO_LONG;
        int offsetSeptets = (offsetBits + STATUS_CANCEL_FAILED) / STATUS_BLOCKED_DESTINATION;
        int i = numFields - offsetSeptets;
        String result = GsmAlphabet.gsm7BitPackedToString(data, offset, numFields, (offsetSeptets * STATUS_BLOCKED_DESTINATION) - offsetBits, STATUS_ACCEPTED, STATUS_ACCEPTED);
        if (result != null) {
            return result;
        }
        throw new CodingException("7bit GSM decoding failed");
    }

    private static String decodeLatin(byte[] data, int offset, int numFields) throws CodingException {
        return decodeCharset(data, offset, numFields, STATUS_DEPOSITED_TO_INTERNET, "ISO-8859-1");
    }

    private static String decodeShiftJis(byte[] data, int offset, int numFields) throws CodingException {
        return decodeCharset(data, offset, numFields, STATUS_DEPOSITED_TO_INTERNET, "Shift_JIS");
    }

    private static String decodeGsmDcs(byte[] data, int offset, int numFields, int msgType) throws CodingException {
        switch ((msgType >> STATUS_DELIVERED) & STATUS_CANCELLED) {
            case STATUS_ACCEPTED /*0*/:
                return decode7bitGsm(data, offset, numFields);
            case STATUS_DEPOSITED_TO_INTERNET /*1*/:
                return decodeUtf8(data, offset, numFields);
            case STATUS_DELIVERED /*2*/:
                return decodeUtf16(data, offset, numFields);
            default:
                throw new CodingException("unsupported user msgType encoding (" + msgType + ")");
        }
    }

    private static void decodeUserDataPayload(UserData userData, boolean hasUserDataHeader) throws CodingException {
        int offset = STATUS_ACCEPTED;
        if (hasUserDataHeader) {
            int udhLen = userData.payload[STATUS_ACCEPTED] & STATUS_UNDEFINED;
            offset = (udhLen + STATUS_DEPOSITED_TO_INTERNET) + STATUS_ACCEPTED;
            byte[] headerData = new byte[udhLen];
            System.arraycopy(userData.payload, STATUS_DEPOSITED_TO_INTERNET, headerData, STATUS_ACCEPTED, udhLen);
            userData.userDataHeader = SmsHeader.fromByteArray(headerData);
        }
        switch (userData.msgEncoding) {
            case STATUS_ACCEPTED /*0*/:
                boolean decodingtypeUTF8 = Resources.getSystem().getBoolean(17956966);
                byte[] payload = new byte[userData.numFields];
                System.arraycopy(userData.payload, STATUS_ACCEPTED, payload, STATUS_ACCEPTED, userData.numFields < userData.payload.length ? userData.numFields : userData.payload.length);
                userData.payload = payload;
                if (decodingtypeUTF8) {
                    userData.payloadStr = decodeUtf8(userData.payload, offset, userData.numFields);
                } else {
                    userData.payloadStr = decodeLatin(userData.payload, offset, userData.numFields);
                }
            case STATUS_DELIVERED /*2*/:
            case STATUS_CANCELLED /*3*/:
                userData.payloadStr = decode7bitAscii(userData.payload, offset, userData.numFields);
            case STATUS_NETWORK_CONGESTION /*4*/:
                userData.payloadStr = decodeUtf16(userData.payload, offset, userData.numFields);
            case STATUS_NETWORK_ERROR /*5*/:
                userData.payloadStr = decodeShiftJis(userData.payload, offset, userData.numFields);
            case STATUS_TEXT_TOO_LONG /*8*/:
                userData.payloadStr = decodeLatin(userData.payload, offset, userData.numFields);
            case STATUS_DUPLICATE_MESSAGE /*9*/:
                userData.payloadStr = decode7bitGsm(userData.payload, offset, userData.numFields);
            case STATUS_INVALID_DESTINATION /*10*/:
                userData.payloadStr = decodeGsmDcs(userData.payload, offset, userData.numFields, userData.msgType);
            default:
                throw new CodingException("unsupported user data encoding (" + userData.msgEncoding + ")");
        }
    }

    private static void decodeIs91VoicemailStatus(BearerData bData) throws BitwiseInputStream.AccessException, CodingException {
        BitwiseInputStream inStream = new BitwiseInputStream(bData.userData.payload);
        int dataLen = inStream.available() / STATUS_CANCEL_FAILED;
        int numFields = bData.userData.numFields;
        if (dataLen > 14 || dataLen < STATUS_CANCELLED || dataLen < numFields) {
            throw new CodingException("IS-91 voicemail status decoding failed");
        }
        try {
            StringBuffer strbuf = new StringBuffer(dataLen);
            while (inStream.available() >= STATUS_CANCEL_FAILED) {
                strbuf.append(UserData.ASCII_MAP[inStream.read(STATUS_CANCEL_FAILED)]);
            }
            String data = strbuf.toString();
            bData.numberOfMessages = Integer.parseInt(data.substring(STATUS_ACCEPTED, STATUS_DELIVERED));
            char prioCode = data.charAt(STATUS_DELIVERED);
            if (prioCode == ' ') {
                bData.priority = STATUS_ACCEPTED;
            } else if (prioCode == '!') {
                bData.priority = STATUS_DELIVERED;
            } else {
                throw new CodingException("IS-91 voicemail status decoding failed: illegal priority setting (" + prioCode + ")");
            }
            bData.priorityIndicatorSet = true;
            bData.userData.payloadStr = data.substring(STATUS_CANCELLED, numFields - 3);
        } catch (NumberFormatException ex) {
            throw new CodingException("IS-91 voicemail status decoding failed: " + ex);
        } catch (IndexOutOfBoundsException ex2) {
            throw new CodingException("IS-91 voicemail status decoding failed: " + ex2);
        }
    }

    private static void decodeIs91ShortMessage(BearerData bData) throws BitwiseInputStream.AccessException, CodingException {
        BitwiseInputStream inStream = new BitwiseInputStream(bData.userData.payload);
        int dataLen = inStream.available() / STATUS_CANCEL_FAILED;
        int numFields = bData.userData.numFields;
        if (numFields > 14 || dataLen < numFields) {
            throw new CodingException("IS-91 short message decoding failed");
        }
        StringBuffer strbuf = new StringBuffer(dataLen);
        for (int i = STATUS_ACCEPTED; i < numFields; i += STATUS_DEPOSITED_TO_INTERNET) {
            strbuf.append(UserData.ASCII_MAP[inStream.read(STATUS_CANCEL_FAILED)]);
        }
        bData.userData.payloadStr = strbuf.toString();
    }

    private static void decodeIs91Cli(BearerData bData) throws CodingException {
        int dataLen = new BitwiseInputStream(bData.userData.payload).available() / STATUS_NETWORK_CONGESTION;
        int numFields = bData.userData.numFields;
        if (dataLen > 14 || dataLen < STATUS_CANCELLED || dataLen < numFields) {
            throw new CodingException("IS-91 voicemail status decoding failed");
        }
        CdmaSmsAddress addr = new CdmaSmsAddress();
        addr.digitMode = STATUS_ACCEPTED;
        addr.origBytes = bData.userData.payload;
        addr.numberOfDigits = (byte) numFields;
        decodeSmsAddress(addr);
        bData.callbackNumber = addr;
    }

    private static void decodeIs91(BearerData bData) throws BitwiseInputStream.AccessException, CodingException {
        switch (bData.userData.msgType) {
            case PduPart.P_LEVEL /*130*/:
                decodeIs91VoicemailStatus(bData);
            case PduPart.P_TYPE /*131*/:
            case PduPart.P_DEP_NAME /*133*/:
                decodeIs91ShortMessage(bData);
            case PduHeaders.STATUS_UNRECOGNIZED /*132*/:
                decodeIs91Cli(bData);
            default:
                throw new CodingException("unsupported IS-91 message type (" + bData.userData.msgType + ")");
        }
    }

    private static boolean decodeReplyOption(BearerData bData, BitwiseInputStream inStream) throws BitwiseInputStream.AccessException {
        boolean z = true;
        boolean decodeSuccess = false;
        int paramBits = inStream.read(STATUS_TEXT_TOO_LONG) * STATUS_TEXT_TOO_LONG;
        if (paramBits >= STATUS_TEXT_TOO_LONG) {
            boolean z2;
            paramBits -= 8;
            decodeSuccess = true;
            bData.userAckReq = inStream.read(STATUS_DEPOSITED_TO_INTERNET) == STATUS_DEPOSITED_TO_INTERNET;
            if (inStream.read(STATUS_DEPOSITED_TO_INTERNET) == STATUS_DEPOSITED_TO_INTERNET) {
                z2 = true;
            } else {
                z2 = false;
            }
            bData.deliveryAckReq = z2;
            if (inStream.read(STATUS_DEPOSITED_TO_INTERNET) == STATUS_DEPOSITED_TO_INTERNET) {
                z2 = true;
            } else {
                z2 = false;
            }
            bData.readAckReq = z2;
            if (inStream.read(STATUS_DEPOSITED_TO_INTERNET) != STATUS_DEPOSITED_TO_INTERNET) {
                z = false;
            }
            bData.reportReq = z;
            inStream.skip(STATUS_NETWORK_CONGESTION);
        }
        if (!decodeSuccess || paramBits > 0) {
            Rlog.d(LOG_TAG, "REPLY_OPTION decode " + (decodeSuccess ? "succeeded" : "failed") + " (extra bits = " + paramBits + ")");
        }
        inStream.skip(paramBits);
        return decodeSuccess;
    }

    private static boolean decodeMsgCount(BearerData bData, BitwiseInputStream inStream) throws BitwiseInputStream.AccessException {
        boolean decodeSuccess = false;
        int paramBits = inStream.read(STATUS_TEXT_TOO_LONG) * STATUS_TEXT_TOO_LONG;
        if (paramBits >= STATUS_TEXT_TOO_LONG) {
            paramBits -= 8;
            decodeSuccess = true;
            bData.numberOfMessages = IccUtils.cdmaBcdByteToInt((byte) inStream.read(STATUS_TEXT_TOO_LONG));
        }
        if (!decodeSuccess || paramBits > 0) {
            Rlog.d(LOG_TAG, "NUMBER_OF_MESSAGES decode " + (decodeSuccess ? "succeeded" : "failed") + " (extra bits = " + paramBits + ")");
        }
        inStream.skip(paramBits);
        return decodeSuccess;
    }

    private static boolean decodeDepositIndex(BearerData bData, BitwiseInputStream inStream) throws BitwiseInputStream.AccessException {
        boolean decodeSuccess = false;
        int paramBits = inStream.read(STATUS_TEXT_TOO_LONG) * STATUS_TEXT_TOO_LONG;
        if (paramBits >= 16) {
            paramBits -= 16;
            decodeSuccess = true;
            bData.depositIndex = (inStream.read(STATUS_TEXT_TOO_LONG) << STATUS_TEXT_TOO_LONG) | inStream.read(STATUS_TEXT_TOO_LONG);
        }
        if (!decodeSuccess || paramBits > 0) {
            Rlog.d(LOG_TAG, "MESSAGE_DEPOSIT_INDEX decode " + (decodeSuccess ? "succeeded" : "failed") + " (extra bits = " + paramBits + ")");
        }
        inStream.skip(paramBits);
        return decodeSuccess;
    }

    private static String decodeDtmfSmsAddress(byte[] rawData, int numFields) throws CodingException {
        StringBuffer strBuf = new StringBuffer(numFields);
        for (int i = STATUS_ACCEPTED; i < numFields; i += STATUS_DEPOSITED_TO_INTERNET) {
            int val = (rawData[i / STATUS_DELIVERED] >>> (4 - ((i % STATUS_DELIVERED) * STATUS_NETWORK_CONGESTION))) & 15;
            if (val >= STATUS_DEPOSITED_TO_INTERNET && val <= STATUS_DUPLICATE_MESSAGE) {
                strBuf.append(Integer.toString(val, STATUS_INVALID_DESTINATION));
            } else if (val == STATUS_INVALID_DESTINATION) {
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
        if (addr.digitMode == STATUS_DEPOSITED_TO_INTERNET) {
            try {
                addr.address = new String(addr.origBytes, STATUS_ACCEPTED, addr.origBytes.length, "US-ASCII");
                return;
            } catch (UnsupportedEncodingException e) {
                throw new CodingException("invalid SMS address ASCII code");
            }
        }
        addr.address = decodeDtmfSmsAddress(addr.origBytes, addr.numberOfDigits);
    }

    private static boolean decodeCallbackNumber(BearerData bData, BitwiseInputStream inStream) throws BitwiseInputStream.AccessException, CodingException {
        int paramBits = inStream.read(STATUS_TEXT_TOO_LONG) * STATUS_TEXT_TOO_LONG;
        if (paramBits < STATUS_TEXT_TOO_LONG) {
            inStream.skip(paramBits);
            return false;
        }
        CdmaSmsAddress addr = new CdmaSmsAddress();
        addr.digitMode = inStream.read(STATUS_DEPOSITED_TO_INTERNET);
        byte fieldBits = SUBPARAM_VALIDITY_PERIOD_ABSOLUTE;
        int consumedBits = STATUS_DEPOSITED_TO_INTERNET;
        if (addr.digitMode == STATUS_DEPOSITED_TO_INTERNET) {
            addr.ton = inStream.read(STATUS_CANCELLED);
            addr.numberPlan = inStream.read(STATUS_NETWORK_CONGESTION);
            fieldBits = SUBPARAM_PRIORITY_INDICATOR;
            consumedBits = (byte) STATUS_TEXT_TOO_LONG;
        }
        addr.numberOfDigits = inStream.read(STATUS_TEXT_TOO_LONG);
        int remainingBits = paramBits - ((byte) (consumedBits + STATUS_TEXT_TOO_LONG));
        int dataBits = addr.numberOfDigits * fieldBits;
        int paddingBits = remainingBits - dataBits;
        if (remainingBits < dataBits) {
            throw new CodingException("CALLBACK_NUMBER subparam encoding size error (remainingBits + " + remainingBits + ", dataBits + " + dataBits + ", paddingBits + " + paddingBits + ")");
        }
        addr.origBytes = inStream.readByteArray(dataBits);
        inStream.skip(paddingBits);
        decodeSmsAddress(addr);
        bData.callbackNumber = addr;
        return true;
    }

    private static boolean decodeMsgStatus(BearerData bData, BitwiseInputStream inStream) throws BitwiseInputStream.AccessException {
        boolean decodeSuccess = false;
        int paramBits = inStream.read(STATUS_TEXT_TOO_LONG) * STATUS_TEXT_TOO_LONG;
        if (paramBits >= STATUS_TEXT_TOO_LONG) {
            paramBits -= 8;
            decodeSuccess = true;
            bData.errorClass = inStream.read(STATUS_DELIVERED);
            bData.messageStatus = inStream.read(STATUS_CANCEL_FAILED);
        }
        if (!decodeSuccess || paramBits > 0) {
            Rlog.d(LOG_TAG, "MESSAGE_STATUS decode " + (decodeSuccess ? "succeeded" : "failed") + " (extra bits = " + paramBits + ")");
        }
        inStream.skip(paramBits);
        bData.messageStatusSet = decodeSuccess;
        return decodeSuccess;
    }

    private static boolean decodeMsgCenterTimeStamp(BearerData bData, BitwiseInputStream inStream) throws BitwiseInputStream.AccessException {
        boolean decodeSuccess = false;
        int paramBits = inStream.read(STATUS_TEXT_TOO_LONG) * STATUS_TEXT_TOO_LONG;
        if (paramBits >= 48) {
            paramBits -= 48;
            decodeSuccess = true;
            bData.msgCenterTimeStamp = TimeStamp.fromByteArray(inStream.readByteArray(48));
        }
        if (!decodeSuccess || paramBits > 0) {
            Rlog.d(LOG_TAG, "MESSAGE_CENTER_TIME_STAMP decode " + (decodeSuccess ? "succeeded" : "failed") + " (extra bits = " + paramBits + ")");
        }
        inStream.skip(paramBits);
        return decodeSuccess;
    }

    private static boolean decodeValidityAbs(BearerData bData, BitwiseInputStream inStream) throws BitwiseInputStream.AccessException {
        boolean decodeSuccess = false;
        int paramBits = inStream.read(STATUS_TEXT_TOO_LONG) * STATUS_TEXT_TOO_LONG;
        if (paramBits >= 48) {
            paramBits -= 48;
            decodeSuccess = true;
            bData.validityPeriodAbsolute = TimeStamp.fromByteArray(inStream.readByteArray(48));
        }
        if (!decodeSuccess || paramBits > 0) {
            Rlog.d(LOG_TAG, "VALIDITY_PERIOD_ABSOLUTE decode " + (decodeSuccess ? "succeeded" : "failed") + " (extra bits = " + paramBits + ")");
        }
        inStream.skip(paramBits);
        return decodeSuccess;
    }

    private static boolean decodeDeferredDeliveryAbs(BearerData bData, BitwiseInputStream inStream) throws BitwiseInputStream.AccessException {
        boolean decodeSuccess = false;
        int paramBits = inStream.read(STATUS_TEXT_TOO_LONG) * STATUS_TEXT_TOO_LONG;
        if (paramBits >= 48) {
            paramBits -= 48;
            decodeSuccess = true;
            bData.deferredDeliveryTimeAbsolute = TimeStamp.fromByteArray(inStream.readByteArray(48));
        }
        if (!decodeSuccess || paramBits > 0) {
            Rlog.d(LOG_TAG, "DEFERRED_DELIVERY_TIME_ABSOLUTE decode " + (decodeSuccess ? "succeeded" : "failed") + " (extra bits = " + paramBits + ")");
        }
        inStream.skip(paramBits);
        return decodeSuccess;
    }

    private static boolean decodeValidityRel(BearerData bData, BitwiseInputStream inStream) throws BitwiseInputStream.AccessException {
        boolean decodeSuccess = false;
        int paramBits = inStream.read(STATUS_TEXT_TOO_LONG) * STATUS_TEXT_TOO_LONG;
        if (paramBits >= STATUS_TEXT_TOO_LONG) {
            paramBits -= 8;
            decodeSuccess = true;
            bData.deferredDeliveryTimeRelative = inStream.read(STATUS_TEXT_TOO_LONG);
        }
        if (!decodeSuccess || paramBits > 0) {
            Rlog.d(LOG_TAG, "VALIDITY_PERIOD_RELATIVE decode " + (decodeSuccess ? "succeeded" : "failed") + " (extra bits = " + paramBits + ")");
        }
        inStream.skip(paramBits);
        bData.deferredDeliveryTimeRelativeSet = decodeSuccess;
        return decodeSuccess;
    }

    private static boolean decodeDeferredDeliveryRel(BearerData bData, BitwiseInputStream inStream) throws BitwiseInputStream.AccessException {
        boolean decodeSuccess = false;
        int paramBits = inStream.read(STATUS_TEXT_TOO_LONG) * STATUS_TEXT_TOO_LONG;
        if (paramBits >= STATUS_TEXT_TOO_LONG) {
            paramBits -= 8;
            decodeSuccess = true;
            bData.validityPeriodRelative = inStream.read(STATUS_TEXT_TOO_LONG);
        }
        if (!decodeSuccess || paramBits > 0) {
            Rlog.d(LOG_TAG, "DEFERRED_DELIVERY_TIME_RELATIVE decode " + (decodeSuccess ? "succeeded" : "failed") + " (extra bits = " + paramBits + ")");
        }
        inStream.skip(paramBits);
        bData.validityPeriodRelativeSet = decodeSuccess;
        return decodeSuccess;
    }

    private static boolean decodePrivacyIndicator(BearerData bData, BitwiseInputStream inStream) throws BitwiseInputStream.AccessException {
        boolean decodeSuccess = false;
        int paramBits = inStream.read(STATUS_TEXT_TOO_LONG) * STATUS_TEXT_TOO_LONG;
        if (paramBits >= STATUS_TEXT_TOO_LONG) {
            paramBits -= 8;
            decodeSuccess = true;
            bData.privacy = inStream.read(STATUS_DELIVERED);
            inStream.skip(STATUS_CANCEL_FAILED);
        }
        if (!decodeSuccess || paramBits > 0) {
            Rlog.d(LOG_TAG, "PRIVACY_INDICATOR decode " + (decodeSuccess ? "succeeded" : "failed") + " (extra bits = " + paramBits + ")");
        }
        inStream.skip(paramBits);
        bData.privacyIndicatorSet = decodeSuccess;
        return decodeSuccess;
    }

    private static boolean decodeLanguageIndicator(BearerData bData, BitwiseInputStream inStream) throws BitwiseInputStream.AccessException {
        boolean decodeSuccess = false;
        int paramBits = inStream.read(STATUS_TEXT_TOO_LONG) * STATUS_TEXT_TOO_LONG;
        if (paramBits >= STATUS_TEXT_TOO_LONG) {
            paramBits -= 8;
            decodeSuccess = true;
            bData.language = inStream.read(STATUS_TEXT_TOO_LONG);
        }
        if (!decodeSuccess || paramBits > 0) {
            Rlog.d(LOG_TAG, "LANGUAGE_INDICATOR decode " + (decodeSuccess ? "succeeded" : "failed") + " (extra bits = " + paramBits + ")");
        }
        inStream.skip(paramBits);
        bData.languageIndicatorSet = decodeSuccess;
        return decodeSuccess;
    }

    private static boolean decodeDisplayMode(BearerData bData, BitwiseInputStream inStream) throws BitwiseInputStream.AccessException {
        boolean decodeSuccess = false;
        int paramBits = inStream.read(STATUS_TEXT_TOO_LONG) * STATUS_TEXT_TOO_LONG;
        if (paramBits >= STATUS_TEXT_TOO_LONG) {
            paramBits -= 8;
            decodeSuccess = true;
            bData.displayMode = inStream.read(STATUS_DELIVERED);
            inStream.skip(STATUS_CANCEL_FAILED);
        }
        if (!decodeSuccess || paramBits > 0) {
            Rlog.d(LOG_TAG, "DISPLAY_MODE decode " + (decodeSuccess ? "succeeded" : "failed") + " (extra bits = " + paramBits + ")");
        }
        inStream.skip(paramBits);
        bData.displayModeSet = decodeSuccess;
        return decodeSuccess;
    }

    private static boolean decodePriorityIndicator(BearerData bData, BitwiseInputStream inStream) throws BitwiseInputStream.AccessException {
        boolean decodeSuccess = false;
        int paramBits = inStream.read(STATUS_TEXT_TOO_LONG) * STATUS_TEXT_TOO_LONG;
        if (paramBits >= STATUS_TEXT_TOO_LONG) {
            paramBits -= 8;
            decodeSuccess = true;
            bData.priority = inStream.read(STATUS_DELIVERED);
            inStream.skip(STATUS_CANCEL_FAILED);
        }
        if (!decodeSuccess || paramBits > 0) {
            Rlog.d(LOG_TAG, "PRIORITY_INDICATOR decode " + (decodeSuccess ? "succeeded" : "failed") + " (extra bits = " + paramBits + ")");
        }
        inStream.skip(paramBits);
        bData.priorityIndicatorSet = decodeSuccess;
        return decodeSuccess;
    }

    private static boolean decodeMsgDeliveryAlert(BearerData bData, BitwiseInputStream inStream) throws BitwiseInputStream.AccessException {
        boolean decodeSuccess = false;
        int paramBits = inStream.read(STATUS_TEXT_TOO_LONG) * STATUS_TEXT_TOO_LONG;
        if (paramBits >= STATUS_TEXT_TOO_LONG) {
            paramBits -= 8;
            decodeSuccess = true;
            bData.alert = inStream.read(STATUS_DELIVERED);
            inStream.skip(STATUS_CANCEL_FAILED);
        }
        if (!decodeSuccess || paramBits > 0) {
            Rlog.d(LOG_TAG, "ALERT_ON_MESSAGE_DELIVERY decode " + (decodeSuccess ? "succeeded" : "failed") + " (extra bits = " + paramBits + ")");
        }
        inStream.skip(paramBits);
        bData.alertIndicatorSet = decodeSuccess;
        return decodeSuccess;
    }

    private static boolean decodeUserResponseCode(BearerData bData, BitwiseInputStream inStream) throws BitwiseInputStream.AccessException {
        boolean decodeSuccess = false;
        int paramBits = inStream.read(STATUS_TEXT_TOO_LONG) * STATUS_TEXT_TOO_LONG;
        if (paramBits >= STATUS_TEXT_TOO_LONG) {
            paramBits -= 8;
            decodeSuccess = true;
            bData.userResponseCode = inStream.read(STATUS_TEXT_TOO_LONG);
        }
        if (!decodeSuccess || paramBits > 0) {
            Rlog.d(LOG_TAG, "USER_RESPONSE_CODE decode " + (decodeSuccess ? "succeeded" : "failed") + " (extra bits = " + paramBits + ")");
        }
        inStream.skip(paramBits);
        bData.userResponseCodeSet = decodeSuccess;
        return decodeSuccess;
    }

    private static boolean decodeServiceCategoryProgramData(BearerData bData, BitwiseInputStream inStream) throws BitwiseInputStream.AccessException, CodingException {
        if (inStream.available() < STATUS_MESSAGE_EXPIRED) {
            throw new CodingException("SERVICE_CATEGORY_PROGRAM_DATA decode failed: only " + inStream.available() + " bits available");
        }
        int paramBits = inStream.read(STATUS_TEXT_TOO_LONG) * STATUS_TEXT_TOO_LONG;
        int msgEncoding = inStream.read(STATUS_NETWORK_ERROR);
        paramBits -= 5;
        if (inStream.available() < paramBits) {
            throw new CodingException("SERVICE_CATEGORY_PROGRAM_DATA decode failed: only " + inStream.available() + " bits available (" + paramBits + " bits expected)");
        }
        ArrayList<CdmaSmsCbProgramData> programDataList = new ArrayList();
        boolean decodeSuccess = false;
        while (paramBits >= 48) {
            int operation = inStream.read(STATUS_NETWORK_CONGESTION);
            int category = (inStream.read(STATUS_TEXT_TOO_LONG) << STATUS_TEXT_TOO_LONG) | inStream.read(STATUS_TEXT_TOO_LONG);
            int language = inStream.read(STATUS_TEXT_TOO_LONG);
            int maxMessages = inStream.read(STATUS_TEXT_TOO_LONG);
            int alertOption = inStream.read(STATUS_NETWORK_CONGESTION);
            int numFields = inStream.read(STATUS_TEXT_TOO_LONG);
            paramBits -= 48;
            int textBits = getBitsForNumFields(msgEncoding, numFields);
            if (paramBits < textBits) {
                throw new CodingException("category name is " + textBits + " bits in length," + " but there are only " + paramBits + " bits available");
            }
            UserData userData = new UserData();
            userData.msgEncoding = msgEncoding;
            userData.msgEncodingSet = true;
            userData.numFields = numFields;
            userData.payload = inStream.readByteArray(textBits);
            paramBits -= textBits;
            decodeUserDataPayload(userData, false);
            programDataList.add(new CdmaSmsCbProgramData(operation, category, language, maxMessages, alertOption, userData.payloadStr));
            decodeSuccess = true;
        }
        if (!decodeSuccess || paramBits > 0) {
            Rlog.d(LOG_TAG, "SERVICE_CATEGORY_PROGRAM_DATA decode " + (decodeSuccess ? "succeeded" : "failed") + " (extra bits = " + paramBits + ')');
        }
        inStream.skip(paramBits);
        bData.serviceCategoryProgramData = programDataList;
        return decodeSuccess;
    }

    private static int serviceCategoryToCmasMessageClass(int serviceCategory) {
        switch (serviceCategory) {
            case SmsCbConstants.SERIAL_NUMBER_ETWS_ACTIVATE_POPUP /*4096*/:
                return STATUS_ACCEPTED;
            case SmsEnvelope.SERVICE_CATEGORY_CMAS_EXTREME_THREAT /*4097*/:
                return STATUS_DEPOSITED_TO_INTERNET;
            case SmsEnvelope.TELESERVICE_WMT /*4098*/:
                return STATUS_DELIVERED;
            case SmsEnvelope.TELESERVICE_VMN /*4099*/:
                return STATUS_CANCELLED;
            case SmsEnvelope.TELESERVICE_WAP /*4100*/:
                return STATUS_NETWORK_CONGESTION;
            default:
                return -1;
        }
    }

    private static int getBitsForNumFields(int msgEncoding, int numFields) throws CodingException {
        switch (msgEncoding) {
            case STATUS_ACCEPTED /*0*/:
            case STATUS_NETWORK_ERROR /*5*/:
            case STATUS_CANCEL_FAILED /*6*/:
            case STATUS_BLOCKED_DESTINATION /*7*/:
            case STATUS_TEXT_TOO_LONG /*8*/:
                return numFields * STATUS_TEXT_TOO_LONG;
            case STATUS_DELIVERED /*2*/:
            case STATUS_CANCELLED /*3*/:
            case STATUS_DUPLICATE_MESSAGE /*9*/:
                return numFields * STATUS_BLOCKED_DESTINATION;
            case STATUS_NETWORK_CONGESTION /*4*/:
                return numFields * 16;
            default:
                throw new CodingException("unsupported message encoding (" + msgEncoding + ')');
        }
    }

    private static void decodeCmasUserData(BearerData bData, int serviceCategory) throws BitwiseInputStream.AccessException, CodingException {
        BitwiseInputStream inStream = new BitwiseInputStream(bData.userData.payload);
        if (inStream.available() < STATUS_TEXT_TOO_LONG) {
            throw new CodingException("emergency CB with no CMAE_protocol_version");
        }
        int protocolVersion = inStream.read(STATUS_TEXT_TOO_LONG);
        if (protocolVersion != 0) {
            throw new CodingException("unsupported CMAE_protocol_version " + protocolVersion);
        }
        int messageClass = serviceCategoryToCmasMessageClass(serviceCategory);
        int category = -1;
        int responseType = -1;
        int severity = -1;
        int urgency = -1;
        int certainty = -1;
        while (inStream.available() >= 16) {
            int recordType = inStream.read(STATUS_TEXT_TOO_LONG);
            int recordLen = inStream.read(STATUS_TEXT_TOO_LONG);
            switch (recordType) {
                case STATUS_ACCEPTED /*0*/:
                    int numFields;
                    UserData alertUserData = new UserData();
                    alertUserData.msgEncoding = inStream.read(STATUS_NETWORK_ERROR);
                    alertUserData.msgEncodingSet = true;
                    alertUserData.msgType = STATUS_ACCEPTED;
                    switch (alertUserData.msgEncoding) {
                        case STATUS_ACCEPTED /*0*/:
                        case STATUS_TEXT_TOO_LONG /*8*/:
                            numFields = recordLen - 1;
                            break;
                        case STATUS_DELIVERED /*2*/:
                        case STATUS_CANCELLED /*3*/:
                        case STATUS_DUPLICATE_MESSAGE /*9*/:
                            numFields = ((recordLen * STATUS_TEXT_TOO_LONG) - 5) / STATUS_BLOCKED_DESTINATION;
                            break;
                        case STATUS_NETWORK_CONGESTION /*4*/:
                            numFields = (recordLen - 1) / STATUS_DELIVERED;
                            break;
                        default:
                            numFields = STATUS_ACCEPTED;
                            break;
                    }
                    alertUserData.numFields = numFields;
                    alertUserData.payload = inStream.readByteArray((recordLen * STATUS_TEXT_TOO_LONG) - 5);
                    decodeUserDataPayload(alertUserData, false);
                    bData.userData = alertUserData;
                    break;
                case STATUS_DEPOSITED_TO_INTERNET /*1*/:
                    category = inStream.read(STATUS_TEXT_TOO_LONG);
                    responseType = inStream.read(STATUS_TEXT_TOO_LONG);
                    severity = inStream.read(STATUS_NETWORK_CONGESTION);
                    urgency = inStream.read(STATUS_NETWORK_CONGESTION);
                    certainty = inStream.read(STATUS_NETWORK_CONGESTION);
                    inStream.skip((recordLen * STATUS_TEXT_TOO_LONG) - 28);
                    break;
                default:
                    Rlog.w(LOG_TAG, "skipping unsupported CMAS record type " + recordType);
                    inStream.skip(recordLen * STATUS_TEXT_TOO_LONG);
                    break;
            }
        }
        bData.cmasWarningInfo = new SmsCbCmasInfo(messageClass, category, responseType, severity, urgency, certainty);
    }

    public static BearerData decode(byte[] smsData) {
        return decode(smsData, STATUS_ACCEPTED);
    }

    private static boolean isCmasAlertCategory(int category) {
        if (category < SmsCbConstants.SERIAL_NUMBER_ETWS_ACTIVATE_POPUP || category > SmsEnvelope.SERVICE_CATEGORY_CMAS_LAST_RESERVED_VALUE) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static BearerData decode(byte[] smsData, int serviceCategory) {
        try {
            BitwiseInputStream inStream = new BitwiseInputStream(smsData);
            BearerData bData = new BearerData();
            int foundSubparamMask = STATUS_ACCEPTED;
            while (inStream.available() > 0) {
                int subparamId = inStream.read(STATUS_TEXT_TOO_LONG);
                int subparamIdBit = STATUS_DEPOSITED_TO_INTERNET << subparamId;
                if ((foundSubparamMask & subparamIdBit) == 0 || subparamId < 0 || subparamId > 23) {
                    boolean decodeSuccess;
                    switch (subparamId) {
                        case STATUS_ACCEPTED /*0*/:
                            decodeSuccess = decodeMessageId(bData, inStream);
                            break;
                        case STATUS_DEPOSITED_TO_INTERNET /*1*/:
                            decodeSuccess = decodeUserData(bData, inStream);
                            break;
                        case STATUS_DELIVERED /*2*/:
                            decodeSuccess = decodeUserResponseCode(bData, inStream);
                            break;
                        case STATUS_CANCELLED /*3*/:
                            decodeSuccess = decodeMsgCenterTimeStamp(bData, inStream);
                            break;
                        case STATUS_NETWORK_CONGESTION /*4*/:
                            decodeSuccess = decodeValidityAbs(bData, inStream);
                            break;
                        case STATUS_NETWORK_ERROR /*5*/:
                            decodeSuccess = decodeValidityRel(bData, inStream);
                            break;
                        case STATUS_CANCEL_FAILED /*6*/:
                            decodeSuccess = decodeDeferredDeliveryAbs(bData, inStream);
                            break;
                        case STATUS_BLOCKED_DESTINATION /*7*/:
                            decodeSuccess = decodeDeferredDeliveryRel(bData, inStream);
                            break;
                        case STATUS_TEXT_TOO_LONG /*8*/:
                            decodeSuccess = decodePriorityIndicator(bData, inStream);
                            break;
                        case STATUS_DUPLICATE_MESSAGE /*9*/:
                            decodeSuccess = decodePrivacyIndicator(bData, inStream);
                            break;
                        case STATUS_INVALID_DESTINATION /*10*/:
                            decodeSuccess = decodeReplyOption(bData, inStream);
                            break;
                        case CharacterSets.ISO_8859_8 /*11*/:
                            decodeSuccess = decodeMsgCount(bData, inStream);
                            break;
                        case CharacterSets.ISO_8859_9 /*12*/:
                            decodeSuccess = decodeMsgDeliveryAlert(bData, inStream);
                            break;
                        case STATUS_MESSAGE_EXPIRED /*13*/:
                            decodeSuccess = decodeLanguageIndicator(bData, inStream);
                            break;
                        case SmsHeader.ELT_ID_LARGE_ANIMATION /*14*/:
                            decodeSuccess = decodeCallbackNumber(bData, inStream);
                            break;
                        case SignalToneUtil.IS95_CONST_IR_SIG_ISDN_OFF /*15*/:
                            decodeSuccess = decodeDisplayMode(bData, inStream);
                            break;
                        case PduHeaders.MMS_VERSION_1_1 /*17*/:
                            decodeSuccess = decodeDepositIndex(bData, inStream);
                            break;
                        case PduHeaders.MMS_VERSION_1_2 /*18*/:
                            decodeSuccess = decodeServiceCategoryProgramData(bData, inStream);
                            break;
                        case SmsHeader.ELT_ID_EXTENDED_OBJECT /*20*/:
                            decodeSuccess = decodeMsgStatus(bData, inStream);
                            break;
                        default:
                            decodeSuccess = decodeReserved(bData, inStream, subparamId);
                            break;
                    }
                    if (decodeSuccess && subparamId >= 0 && subparamId <= 23) {
                        foundSubparamMask |= subparamIdBit;
                    }
                } else {
                    throw new CodingException("illegal duplicate subparameter (" + subparamId + ")");
                }
            }
            if ((foundSubparamMask & STATUS_DEPOSITED_TO_INTERNET) == 0) {
                throw new CodingException("missing MESSAGE_IDENTIFIER subparam");
            }
            if (bData.userData != null) {
                if (isCmasAlertCategory(serviceCategory)) {
                    decodeCmasUserData(bData, serviceCategory);
                } else if (bData.userData.msgEncoding == STATUS_DEPOSITED_TO_INTERNET) {
                    if (((foundSubparamMask ^ STATUS_DEPOSITED_TO_INTERNET) ^ STATUS_DELIVERED) != 0) {
                        Rlog.e(LOG_TAG, "IS-91 must occur without extra subparams (" + foundSubparamMask + ")");
                    }
                    decodeIs91(bData);
                } else {
                    decodeUserDataPayload(bData.userData, bData.hasUserDataHeader);
                }
            }
            return bData;
        } catch (BitwiseInputStream.AccessException ex) {
            Rlog.e(LOG_TAG, "BearerData decode failed: " + ex);
        } catch (CodingException ex2) {
            Rlog.e(LOG_TAG, "BearerData decode failed: " + ex2);
        }
    }
}
