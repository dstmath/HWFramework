package com.android.internal.telephony.gsm;

import android.common.HwFrameworkFactory;
import android.content.res.Resources;
import android.os.SystemProperties;
import android.telephony.PhoneNumberUtils;
import android.telephony.PreciseDisconnectCause;
import android.telephony.Rlog;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.LogException;
import com.android.internal.R;
import com.android.internal.telephony.EncodeException;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.GsmAlphabet.TextEncodingDetails;
import com.android.internal.telephony.Sms7BitEncodingTranslator;
import com.android.internal.telephony.SmsAddress;
import com.android.internal.telephony.SmsConstants.MessageClass;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.SmsHeader.PortAddrs;
import com.android.internal.telephony.SmsHeader.SpecialSmsMsg;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.SmsMessageBase.SubmitPduBase;
import com.android.internal.telephony.uicc.IccUtils;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;

public class SmsMessage extends SmsMessageBase {
    static final String LOG_TAG = "SmsMessage";
    private static final int MAX_SMS_TPVP = 255;
    private static final int MIN_SMS_TPVP = 0;
    private static final boolean VDBG = false;
    private static boolean hasSmsVp;
    private static final boolean isAddTPVP = SystemProperties.getBoolean("ro.config.hw_SmsAddTP-VP", false);
    private static final boolean isAllowedCsFw = SystemProperties.getBoolean("ro.config.hw_bastet_csfw", false);
    private static int smsValidityPeriod = SystemProperties.getInt("ro.config.sms_vp", -1);
    private int mDataCodingScheme;
    private boolean mIsStatusReportMessage = false;
    private int mMti;
    private int mProtocolIdentifier;
    private GsmSmsAddress mRecipientAddress;
    private boolean mReplyPathPresent = false;
    private int mStatus;
    private int mVoiceMailCount = 0;
    private MessageClass messageClass;

    public static class PduParser {
        public int mCur = 0;
        public byte[] mPdu;
        byte[] mUserData;
        SmsHeader mUserDataHeader;
        int mUserDataSeptetPadding = 0;

        PduParser(byte[] pdu) {
            this.mPdu = pdu;
        }

        String getSCAddress() {
            String ret;
            int len = getByte();
            if (len == 0) {
                ret = null;
            } else {
                try {
                    ret = PhoneNumberUtils.calledPartyBCDToString(this.mPdu, this.mCur, len);
                } catch (RuntimeException tr) {
                    Rlog.d(SmsMessage.LOG_TAG, "invalid SC address: ", tr);
                    ret = null;
                }
            }
            this.mCur += len;
            return ret;
        }

        public int getByte() {
            byte[] bArr = this.mPdu;
            int i = this.mCur;
            this.mCur = i + 1;
            return bArr[i] & 255;
        }

        public GsmSmsAddress getAddress() {
            int lengthBytes = (((this.mPdu[this.mCur] & 255) + 1) / 2) + 2;
            try {
                GsmSmsAddress ret = new GsmSmsAddress(this.mPdu, this.mCur, lengthBytes);
                this.mCur += lengthBytes;
                return ret;
            } catch (ParseException e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        long getSCTimestampMillis() {
            byte[] bArr = this.mPdu;
            int i = this.mCur;
            this.mCur = i + 1;
            int year = IccUtils.gsmBcdByteToInt(bArr[i]);
            bArr = this.mPdu;
            i = this.mCur;
            this.mCur = i + 1;
            int month = IccUtils.gsmBcdByteToInt(bArr[i]);
            bArr = this.mPdu;
            i = this.mCur;
            this.mCur = i + 1;
            int day = IccUtils.gsmBcdByteToInt(bArr[i]);
            bArr = this.mPdu;
            i = this.mCur;
            this.mCur = i + 1;
            int hour = IccUtils.gsmBcdByteToInt(bArr[i]);
            bArr = this.mPdu;
            i = this.mCur;
            this.mCur = i + 1;
            int minute = IccUtils.gsmBcdByteToInt(bArr[i]);
            bArr = this.mPdu;
            i = this.mCur;
            this.mCur = i + 1;
            int second = IccUtils.gsmBcdByteToInt(bArr[i]);
            bArr = this.mPdu;
            i = this.mCur;
            this.mCur = i + 1;
            byte tzByte = bArr[i];
            int timezoneOffset = IccUtils.gsmBcdByteToInt((byte) (tzByte & -9));
            if ((tzByte & 8) != 0) {
                timezoneOffset = -timezoneOffset;
            }
            Time time = new Time(Time.TIMEZONE_UTC);
            time.year = year >= 90 ? year + PreciseDisconnectCause.ECBM_NOT_SUPPORTED : year + 2000;
            time.month = month - 1;
            time.monthDay = day;
            time.hour = hour;
            time.minute = minute;
            time.second = second;
            return time.toMillis(true) - ((long) (((timezoneOffset * 15) * 60) * 1000));
        }

        int constructUserData(boolean hasUserDataHeader, boolean dataInSeptets) {
            int i;
            int bufferLen;
            int i2 = 0;
            int offset = this.mCur;
            int offset2 = offset + 1;
            int userDataLength = this.mPdu[offset] & 255;
            int headerSeptets = 0;
            int userDataHeaderLength = 0;
            if (hasUserDataHeader) {
                offset = offset2 + 1;
                userDataHeaderLength = this.mPdu[offset2] & 255;
                byte[] udh = new byte[userDataHeaderLength];
                System.arraycopy(this.mPdu, offset, udh, 0, userDataHeaderLength);
                this.mUserDataHeader = SmsHeader.fromByteArray(udh);
                offset += userDataHeaderLength;
                int headerBits = (userDataHeaderLength + 1) * 8;
                headerSeptets = headerBits / 7;
                if (headerBits % 7 > 0) {
                    i = 1;
                } else {
                    i = 0;
                }
                headerSeptets += i;
                this.mUserDataSeptetPadding = (headerSeptets * 7) - headerBits;
            } else {
                offset = offset2;
            }
            if (dataInSeptets) {
                bufferLen = this.mPdu.length - offset;
            } else {
                if (hasUserDataHeader) {
                    i = userDataHeaderLength + 1;
                } else {
                    i = 0;
                }
                bufferLen = userDataLength - i;
                if (bufferLen < 0) {
                    bufferLen = 0;
                }
            }
            this.mUserData = new byte[bufferLen];
            System.arraycopy(this.mPdu, offset, this.mUserData, 0, this.mUserData.length);
            this.mCur = offset;
            if (!dataInSeptets) {
                return this.mUserData.length;
            }
            int count = userDataLength - headerSeptets;
            if (count >= 0) {
                i2 = count;
            }
            return i2;
        }

        byte[] getUserData() {
            return this.mUserData;
        }

        SmsHeader getUserDataHeader() {
            return this.mUserDataHeader;
        }

        String getUserDataGSM7Bit(int septetCount, int languageTable, int languageShiftTable) {
            String ret = GsmAlphabet.gsm7BitPackedToString(this.mPdu, this.mCur, septetCount, this.mUserDataSeptetPadding, languageTable, languageShiftTable);
            this.mCur += (septetCount * 7) / 8;
            return ret;
        }

        String getUserDataGSM8bit(int byteCount) {
            String ret = GsmAlphabet.gsm8BitUnpackedToString(this.mPdu, this.mCur, byteCount);
            this.mCur += byteCount;
            return ret;
        }

        String getUserDataUCS2(int byteCount) {
            String ret;
            try {
                ret = new String(this.mPdu, this.mCur, byteCount, "utf-16");
            } catch (UnsupportedEncodingException ex) {
                ret = LogException.NO_VALUE;
                Rlog.e(SmsMessage.LOG_TAG, "implausible UnsupportedEncodingException", ex);
            }
            this.mCur += byteCount;
            return ret;
        }

        String getUserDataKSC5601(int byteCount) {
            String ret;
            try {
                ret = new String(this.mPdu, this.mCur, byteCount, "KSC5601");
            } catch (UnsupportedEncodingException ex) {
                ret = LogException.NO_VALUE;
                Rlog.e(SmsMessage.LOG_TAG, "implausible UnsupportedEncodingException", ex);
            }
            this.mCur += byteCount;
            return ret;
        }

        boolean moreDataPresent() {
            return this.mPdu.length > this.mCur;
        }
    }

    public static class SubmitPdu extends SubmitPduBase {
    }

    static {
        boolean z = false;
        if (smsValidityPeriod >= 0 && smsValidityPeriod <= 255) {
            z = true;
        }
        hasSmsVp = z;
    }

    public static SmsMessage createFromPdu(byte[] pdu) {
        try {
            SmsMessage msg = new SmsMessage();
            msg.parsePdu(pdu);
            return msg;
        } catch (RuntimeException ex) {
            Rlog.e(LOG_TAG, "SMS PDU parsing failed: ", ex);
            return null;
        } catch (OutOfMemoryError e) {
            Rlog.e(LOG_TAG, "SMS PDU parsing failed with out of memory: ", e);
            return null;
        }
    }

    public boolean isTypeZero() {
        return this.mProtocolIdentifier == 64;
    }

    public static SmsMessage newFromCMT(byte[] pdu) {
        try {
            SmsMessage msg = new SmsMessage();
            msg.parsePdu(pdu);
            return msg;
        } catch (RuntimeException ex) {
            Rlog.e(LOG_TAG, "SMS PDU parsing failed: ", ex);
            return null;
        }
    }

    public static SmsMessage newFromCDS(byte[] pdu) {
        try {
            SmsMessage msg = new SmsMessage();
            msg.parsePdu(pdu);
            return msg;
        } catch (RuntimeException ex) {
            Rlog.e(LOG_TAG, "CDS SMS PDU parsing failed: ", ex);
            return null;
        }
    }

    public static SmsMessage createFromEfRecord(int index, byte[] data) {
        try {
            SmsMessage msg = new SmsMessage();
            msg.mIndexOnIcc = index;
            if ((data[0] & 1) == 0) {
                Rlog.w(LOG_TAG, "SMS parsing failed: Trying to parse a free record");
                return null;
            }
            msg.mStatusOnIcc = data[0] & 7;
            int size = data.length - 1;
            byte[] pdu = new byte[size];
            System.arraycopy(data, 1, pdu, 0, size);
            msg.parsePdu(pdu);
            return msg;
        } catch (RuntimeException ex) {
            Rlog.e(LOG_TAG, "SMS PDU parsing failed: ", ex);
            return null;
        }
    }

    public static int getTPLayerLengthForPDU(String pdu) {
        return ((pdu.length() / 2) - Integer.parseInt(pdu.substring(0, 2), 16)) - 1;
    }

    public static SubmitPdu getSubmitPdu(String scAddress, String destinationAddress, String message, boolean statusReportRequested, byte[] header) {
        return getSubmitPdu(scAddress, destinationAddress, message, statusReportRequested, header, 0, 0, 0);
    }

    public static SubmitPdu getSubmitPdu(String scAddress, String destinationAddress, String message, boolean statusReportRequested, byte[] header, int encoding, int languageTable, int languageShiftTable) {
        if (message == null || destinationAddress == null) {
            return null;
        }
        byte mtiByte;
        byte[] userData;
        if (encoding == 0) {
            TextEncodingDetails ted = calculateLength(message, false);
            encoding = ted.codeUnitSize;
            languageTable = ted.languageTable;
            languageShiftTable = ted.languageShiftTable;
            if (encoding == 1 && !(languageTable == 0 && languageShiftTable == 0)) {
                SmsHeader smsHeader;
                if (header != null) {
                    smsHeader = SmsHeader.fromByteArray(header);
                    if (!(smsHeader.languageTable == languageTable && smsHeader.languageShiftTable == languageShiftTable)) {
                        Rlog.w(LOG_TAG, "Updating language table in SMS header: " + smsHeader.languageTable + " -> " + languageTable + ", " + smsHeader.languageShiftTable + " -> " + languageShiftTable);
                        smsHeader.languageTable = languageTable;
                        smsHeader.languageShiftTable = languageShiftTable;
                        header = SmsHeader.toByteArray(smsHeader);
                    }
                } else {
                    smsHeader = new SmsHeader();
                    smsHeader.languageTable = languageTable;
                    smsHeader.languageShiftTable = languageShiftTable;
                    header = SmsHeader.toByteArray(smsHeader);
                }
            }
        }
        SubmitPdu ret = new SubmitPdu();
        if (isAddTPVP || hasSmsVp) {
            mtiByte = (byte) (((header != null ? 64 : 0) | 1) | 16);
        } else {
            mtiByte = (byte) ((header != null ? 64 : 0) | 1);
        }
        ByteArrayOutputStream bo = getSubmitPduHead(scAddress, destinationAddress, mtiByte, statusReportRequested, ret);
        if (encoding == 1) {
            try {
                userData = GsmAlphabet.stringToGsm7BitPackedWithHeader(message, header, languageTable, languageShiftTable);
            } catch (EncodeException e) {
                try {
                    userData = encodeUCS2(message, header);
                    encoding = 3;
                } catch (UnsupportedEncodingException uex) {
                    Rlog.e(LOG_TAG, "Implausible UnsupportedEncodingException ", uex);
                    return null;
                }
            }
        }
        try {
            userData = encodeUCS2(message, header);
        } catch (UnsupportedEncodingException uex2) {
            Rlog.e(LOG_TAG, "Implausible UnsupportedEncodingException ", uex2);
            return null;
        }
        if (encoding == 1) {
            if ((userData[0] & 255) > 160) {
                Rlog.e(LOG_TAG, "Message too long (" + (userData[0] & 255) + " septets)");
                return null;
            }
            bo.write(0);
        } else if ((userData[0] & 255) > 140) {
            Rlog.e(LOG_TAG, "Message too long (" + (userData[0] & 255) + " bytes)");
            return null;
        } else {
            bo.write(8);
        }
        if (hasSmsVp) {
            bo.write(smsValidityPeriod);
        } else if (isAddTPVP) {
            bo.write(255);
        }
        bo.write(userData, 0, userData.length);
        ret.encodedMessage = bo.toByteArray();
        return ret;
    }

    public static byte[] encodeUCS2(String message, byte[] header) throws UnsupportedEncodingException {
        byte[] userData;
        byte[] textPart = message.getBytes("utf-16be");
        if (header != null) {
            userData = new byte[((header.length + textPart.length) + 1)];
            userData[0] = (byte) header.length;
            System.arraycopy(header, 0, userData, 1, header.length);
            System.arraycopy(textPart, 0, userData, header.length + 1, textPart.length);
        } else {
            userData = textPart;
        }
        byte[] ret = new byte[(userData.length + 1)];
        ret[0] = (byte) (userData.length & 255);
        System.arraycopy(userData, 0, ret, 1, userData.length);
        return ret;
    }

    public static SubmitPdu getSubmitPdu(String scAddress, String destinationAddress, String message, boolean statusReportRequested) {
        return getSubmitPdu(scAddress, destinationAddress, message, statusReportRequested, null);
    }

    public static SubmitPdu getSubmitPdu(String scAddress, String destinationAddress, int destinationPort, byte[] data, boolean statusReportRequested) {
        PortAddrs portAddrs = new PortAddrs();
        portAddrs.destPort = destinationPort;
        portAddrs.origPort = 0;
        portAddrs.areEightBits = false;
        SmsHeader smsHeader = new SmsHeader();
        smsHeader.portAddrs = portAddrs;
        if ((destinationAddress.equals("1065840409") || destinationAddress.equals("10654040")) && destinationPort == 16998) {
            portAddrs.origPort = 16998;
        }
        byte[] smsHeaderData = SmsHeader.toByteArray(smsHeader);
        if ((data.length + smsHeaderData.length) + 1 > 140) {
            Rlog.e(LOG_TAG, "SMS data message may only contain " + ((140 - smsHeaderData.length) - 1) + " bytes");
            return null;
        }
        SubmitPdu ret = new SubmitPdu();
        ByteArrayOutputStream bo = getSubmitPduHead(scAddress, destinationAddress, (byte) 65, statusReportRequested, ret);
        bo.write(4);
        bo.write((data.length + smsHeaderData.length) + 1);
        bo.write(smsHeaderData.length);
        bo.write(smsHeaderData, 0, smsHeaderData.length);
        bo.write(data, 0, data.length);
        ret.encodedMessage = bo.toByteArray();
        return ret;
    }

    private static ByteArrayOutputStream getSubmitPduHead(String scAddress, String destinationAddress, byte mtiByte, boolean statusReportRequested, SubmitPdu ret) {
        int i;
        ByteArrayOutputStream bo = new ByteArrayOutputStream(180);
        if (scAddress == null) {
            ret.encodedScAddress = null;
        } else {
            ret.encodedScAddress = PhoneNumberUtils.networkPortionToCalledPartyBCDWithLength(scAddress);
        }
        if (statusReportRequested) {
            mtiByte = (byte) (mtiByte | 32);
        }
        bo.write(mtiByte);
        bo.write(0);
        byte[] daBytes = PhoneNumberUtils.networkPortionToCalledPartyBCD(destinationAddress);
        int length = (daBytes.length - 1) * 2;
        if ((daBytes[daBytes.length - 1] & 240) == 240) {
            i = 1;
        } else {
            i = 0;
        }
        bo.write(length - i);
        bo.write(daBytes, 0, daBytes.length);
        bo.write(0);
        return bo;
    }

    public static TextEncodingDetails calculateLength(CharSequence msgBody, boolean use7bitOnly) {
        CharSequence newMsgBody = null;
        if (Resources.getSystem().getBoolean(R.bool.config_sms_force_7bit_encoding)) {
            newMsgBody = Sms7BitEncodingTranslator.translate(msgBody);
        }
        if (TextUtils.isEmpty(newMsgBody)) {
            newMsgBody = msgBody;
        }
        TextEncodingDetails ted = GsmAlphabet.countGsmSeptets(newMsgBody, use7bitOnly);
        if (ted == null) {
            return SmsMessageBase.calcUnicodeEncodingDetails(newMsgBody);
        }
        return ted;
    }

    public int getProtocolIdentifier() {
        return this.mProtocolIdentifier;
    }

    int getDataCodingScheme() {
        return this.mDataCodingScheme;
    }

    public boolean isReplace() {
        if ((this.mProtocolIdentifier & 192) != 64 || (this.mProtocolIdentifier & 63) <= 0 || (this.mProtocolIdentifier & 63) >= 8) {
            return false;
        }
        return true;
    }

    public boolean isCphsMwiMessage() {
        if (this.mOriginatingAddress == null) {
            return false;
        }
        boolean z;
        if (((GsmSmsAddress) this.mOriginatingAddress).isCphsVoiceMessageClear()) {
            z = true;
        } else {
            z = ((GsmSmsAddress) this.mOriginatingAddress).isCphsVoiceMessageSet();
        }
        return z;
    }

    public boolean isMWIClearMessage() {
        if (this.mIsMwi && (this.mMwiSense ^ 1) != 0) {
            return true;
        }
        boolean isCphsVoiceMessageClear;
        if (this.mOriginatingAddress != null) {
            isCphsVoiceMessageClear = ((GsmSmsAddress) this.mOriginatingAddress).isCphsVoiceMessageClear();
        } else {
            isCphsVoiceMessageClear = false;
        }
        return isCphsVoiceMessageClear;
    }

    public boolean isMWISetMessage() {
        if (this.mIsMwi && this.mMwiSense) {
            return true;
        }
        boolean isCphsVoiceMessageSet;
        if (this.mOriginatingAddress != null) {
            isCphsVoiceMessageSet = ((GsmSmsAddress) this.mOriginatingAddress).isCphsVoiceMessageSet();
        } else {
            isCphsVoiceMessageSet = false;
        }
        return isCphsVoiceMessageSet;
    }

    public boolean isMwiDontStore() {
        if (this.mIsMwi && this.mMwiDontStore) {
            return true;
        }
        if (isCphsMwiMessage() && " ".equals(getMessageBody())) {
            return true;
        }
        return false;
    }

    public int getStatus() {
        return this.mStatus;
    }

    public boolean isStatusReportMessage() {
        return this.mIsStatusReportMessage;
    }

    public boolean isReplyPathPresent() {
        return this.mReplyPathPresent;
    }

    private void parsePdu(byte[] pdu) {
        this.mPdu = pdu;
        PduParser p = new PduParser(pdu);
        if (isAllowedCsFw) {
            if (255 == (p.mPdu[p.mCur] & 255)) {
                p.mCur++;
                this.blacklistFlag = true;
                Rlog.d(LOG_TAG, "parsePdu blacklistFlag: " + this.blacklistFlag + "p.mCur: " + p.mCur);
            } else {
                this.blacklistFlag = false;
            }
        }
        this.mScAddress = p.getSCAddress();
        String str = this.mScAddress;
        int firstByte = p.getByte();
        this.mMti = firstByte & 3;
        if (!HwFrameworkFactory.getHwBaseInnerSmsManager().parseGsmSmsSubmit(this, this.mMti, p, firstByte)) {
            switch (this.mMti) {
                case 0:
                case 3:
                    parseSmsDeliver(p, firstByte);
                    break;
                case 1:
                    parseSmsSubmit(p, firstByte);
                    break;
                case 2:
                    parseSmsStatusReport(p, firstByte);
                    break;
                default:
                    throw new RuntimeException("Unsupported message type");
            }
        }
    }

    private void parseSmsStatusReport(PduParser p, int firstByte) {
        this.mIsStatusReportMessage = true;
        this.mMessageRef = p.getByte();
        this.mRecipientAddress = p.getAddress();
        this.mScTimeMillis = p.getSCTimestampMillis();
        p.getSCTimestampMillis();
        this.mStatus = p.getByte();
        if (p.moreDataPresent()) {
            int extraParams = p.getByte();
            int moreExtraParams = extraParams;
            while ((moreExtraParams & 128) != 0) {
                moreExtraParams = p.getByte();
            }
            if ((extraParams & 120) == 0) {
                if ((extraParams & 1) != 0) {
                    this.mProtocolIdentifier = p.getByte();
                }
                if ((extraParams & 2) != 0) {
                    this.mDataCodingScheme = p.getByte();
                }
                if ((extraParams & 4) != 0) {
                    parseUserData(p, (firstByte & 64) == 64);
                }
            }
        }
    }

    private void parseSmsDeliver(PduParser p, int firstByte) {
        this.mReplyPathPresent = (firstByte & 128) == 128;
        this.mOriginatingAddress = p.getAddress();
        SmsAddress smsAddress = this.mOriginatingAddress;
        this.mProtocolIdentifier = p.getByte();
        this.mDataCodingScheme = p.getByte();
        this.mScTimeMillis = p.getSCTimestampMillis();
        parseUserData(p, (firstByte & 64) == 64);
    }

    private void parseSmsSubmit(PduParser p, int firstByte) {
        int validityPeriodLength;
        boolean z = false;
        if ((firstByte & 128) == 128) {
            z = true;
        }
        this.mReplyPathPresent = z;
        this.mMessageRef = p.getByte();
        this.mRecipientAddress = p.getAddress();
        GsmSmsAddress gsmSmsAddress = this.mRecipientAddress;
        this.mProtocolIdentifier = p.getByte();
        this.mDataCodingScheme = p.getByte();
        int validityPeriodFormat = (firstByte >> 3) & 3;
        if (validityPeriodFormat == 0) {
            validityPeriodLength = 0;
        } else if (2 == validityPeriodFormat) {
            validityPeriodLength = 1;
        } else {
            validityPeriodLength = 7;
        }
        while (true) {
            int validityPeriodLength2 = validityPeriodLength;
            validityPeriodLength = validityPeriodLength2 - 1;
            if (validityPeriodLength2 <= 0) {
                break;
            }
            p.getByte();
        }
        parseUserData(p, (firstByte & 64) == 64);
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void parseUserData(PduParser p, boolean hasUserDataHeader) {
        boolean hasMessageClass = false;
        int encodingType = 0;
        if ((this.mDataCodingScheme & 128) == 0) {
            boolean userDataCompressed = (this.mDataCodingScheme & 32) != 0;
            hasMessageClass = (this.mDataCodingScheme & 16) != 0;
            if (!userDataCompressed) {
                switch ((this.mDataCodingScheme >> 2) & 3) {
                    case 0:
                        encodingType = 1;
                        break;
                    case 1:
                        if (Resources.getSystem().getBoolean(R.bool.config_sms_decode_gsm_8bit_data)) {
                            encodingType = 2;
                            break;
                        }
                    case 2:
                        encodingType = 3;
                        break;
                    case 3:
                        Rlog.w(LOG_TAG, "1 - Unsupported SMS data coding scheme " + (this.mDataCodingScheme & 255));
                        encodingType = 2;
                        break;
                }
            }
            Rlog.w(LOG_TAG, "4 - Unsupported SMS data coding scheme (compression) " + (this.mDataCodingScheme & 255));
        } else if ((this.mDataCodingScheme & 240) == 240) {
            hasMessageClass = true;
            encodingType = (this.mDataCodingScheme & 4) == 0 ? 1 : 2;
        } else if ((this.mDataCodingScheme & 240) == 192 || (this.mDataCodingScheme & 240) == 208 || (this.mDataCodingScheme & 240) == 224) {
            if ((this.mDataCodingScheme & 240) == 224) {
                encodingType = 3;
            } else {
                encodingType = 1;
            }
            boolean active = (this.mDataCodingScheme & 8) == 8;
            if ((this.mDataCodingScheme & 3) == 0) {
                this.mIsMwi = true;
                this.mMwiSense = active;
                this.mMwiDontStore = (this.mDataCodingScheme & 240) == 192;
                if (active) {
                    this.mVoiceMailCount = -1;
                } else {
                    this.mVoiceMailCount = 0;
                }
                Rlog.w(LOG_TAG, "MWI in DCS for Vmail. DCS = " + (this.mDataCodingScheme & 255) + " Dont store = " + this.mMwiDontStore + " vmail count = " + this.mVoiceMailCount);
            } else {
                this.mIsMwi = false;
                Rlog.w(LOG_TAG, "MWI in DCS for fax/email/other: " + (this.mDataCodingScheme & 255));
            }
        } else if ((this.mDataCodingScheme & 192) != 128) {
            Rlog.w(LOG_TAG, "3 - Unsupported SMS data coding scheme " + (this.mDataCodingScheme & 255));
        } else if (this.mDataCodingScheme == 132) {
            encodingType = 4;
        } else {
            Rlog.w(LOG_TAG, "5 - Unsupported SMS data coding scheme " + (this.mDataCodingScheme & 255));
        }
        int count = p.constructUserData(hasUserDataHeader, encodingType == 1);
        this.mUserData = p.getUserData();
        this.mUserDataHeader = p.getUserDataHeader();
        if (hasUserDataHeader && this.mUserDataHeader.specialSmsMsgList.size() != 0) {
            for (SpecialSmsMsg msg : this.mUserDataHeader.specialSmsMsgList) {
                int msgInd = msg.msgIndType & 255;
                if (msgInd == 0 || msgInd == 128) {
                    this.mIsMwi = true;
                    if (msgInd == 128) {
                        this.mMwiDontStore = false;
                    } else if (!(this.mMwiDontStore || (((this.mDataCodingScheme & 240) == 208 || (this.mDataCodingScheme & 240) == 224) && (this.mDataCodingScheme & 3) == 0))) {
                        this.mMwiDontStore = true;
                    }
                    this.mVoiceMailCount = msg.msgCount & 255;
                    if (this.mVoiceMailCount > 0) {
                        this.mMwiSense = true;
                    } else {
                        this.mMwiSense = false;
                    }
                    Rlog.w(LOG_TAG, "MWI in TP-UDH for Vmail. Msg Ind = " + msgInd + " Dont store = " + this.mMwiDontStore + " Vmail count = " + this.mVoiceMailCount);
                } else {
                    Rlog.w(LOG_TAG, "TP_UDH fax/email/extended msg/multisubscriber profile. Msg Ind = " + msgInd);
                }
            }
        }
        switch (encodingType) {
            case 0:
                this.mMessageBody = null;
                break;
            case 1:
                this.mMessageBody = p.getUserDataGSM7Bit(count, hasUserDataHeader ? this.mUserDataHeader.languageTable : 0, hasUserDataHeader ? this.mUserDataHeader.languageShiftTable : 0);
                break;
            case 2:
                if (!Resources.getSystem().getBoolean(R.bool.config_sms_decode_gsm_8bit_data)) {
                    this.mMessageBody = null;
                    break;
                } else {
                    this.mMessageBody = p.getUserDataGSM8bit(count);
                    break;
                }
            case 3:
                this.mMessageBody = p.getUserDataUCS2(count);
                break;
            case 4:
                this.mMessageBody = p.getUserDataKSC5601(count);
                break;
        }
        if (this.mMessageBody != null) {
            parseMessageBody();
        }
        if (hasMessageClass) {
            switch (this.mDataCodingScheme & 3) {
                case 0:
                    this.messageClass = MessageClass.CLASS_0;
                    return;
                case 1:
                    this.messageClass = MessageClass.CLASS_1;
                    return;
                case 2:
                    this.messageClass = MessageClass.CLASS_2;
                    return;
                case 3:
                    this.messageClass = MessageClass.CLASS_3;
                    return;
                default:
                    return;
            }
        }
        this.messageClass = MessageClass.UNKNOWN;
    }

    public MessageClass getMessageClass() {
        return this.messageClass;
    }

    boolean isUsimDataDownload() {
        if (this.messageClass == MessageClass.CLASS_2) {
            return this.mProtocolIdentifier == 127 || this.mProtocolIdentifier == 124;
        } else {
            return false;
        }
    }

    public int getNumOfVoicemails() {
        if (!this.mIsMwi && isCphsMwiMessage()) {
            if (this.mOriginatingAddress == null || !((GsmSmsAddress) this.mOriginatingAddress).isCphsVoiceMessageSet()) {
                this.mVoiceMailCount = 0;
            } else {
                this.mVoiceMailCount = 255;
            }
            Rlog.v(LOG_TAG, "CPHS voice mail message");
        }
        return this.mVoiceMailCount;
    }

    public void setProtocolIdentifierHw(int value) {
        this.mProtocolIdentifier = value;
    }

    public void setDataCodingSchemeHw(int value) {
        this.mDataCodingScheme = value;
    }

    public void parseUserDataHw(PduParser p, boolean hasUserDataHeader) {
        parseUserData(p, hasUserDataHeader);
    }
}
