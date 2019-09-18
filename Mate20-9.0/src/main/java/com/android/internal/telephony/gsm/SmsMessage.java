package com.android.internal.telephony.gsm;

import android.common.HwFrameworkFactory;
import android.content.res.Resources;
import android.os.SystemProperties;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.text.TextUtils;
import android.text.format.Time;
import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.midi.MidiConstants;
import com.android.internal.telephony.EncodeException;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.Sms7BitEncodingTranslator;
import com.android.internal.telephony.SmsAddress;
import com.android.internal.telephony.SmsConstants;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.uicc.IccUtils;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Iterator;

public class SmsMessage extends SmsMessageBase {
    private static final int INVALID_VALIDITY_PERIOD = -1;
    static final String LOG_TAG = "SmsMessage";
    private static final int MAX_SMS_TPVP = 255;
    private static final int MIN_SMS_TPVP = 0;
    private static final int VALIDITY_PERIOD_FORMAT_ABSOLUTE = 3;
    private static final int VALIDITY_PERIOD_FORMAT_ENHANCED = 1;
    private static final int VALIDITY_PERIOD_FORMAT_NONE = 0;
    private static final int VALIDITY_PERIOD_FORMAT_RELATIVE = 2;
    private static final int VALIDITY_PERIOD_MAX = 635040;
    private static final int VALIDITY_PERIOD_MIN = 5;
    private static final boolean VDBG = false;
    private static boolean hasSmsVp;
    private static final boolean isAddTPVP = SystemProperties.getBoolean("ro.config.hw_SmsAddTP-VP", false);
    private static int smsValidityPeriod = SystemProperties.getInt("ro.config.sms_vp", -1);
    private int mDataCodingScheme;
    private boolean mIsStatusReportMessage = false;
    private int mMti;
    private int mProtocolIdentifier;
    private GsmSmsAddress mRecipientAddress;
    private boolean mReplyPathPresent = false;
    private int mStatus;
    private int mVoiceMailCount = 0;
    private SmsConstants.MessageClass messageClass;

    public static class PduParser {
        public int mCur = 0;
        public byte[] mPdu;
        byte[] mUserData;
        SmsHeader mUserDataHeader;
        int mUserDataSeptetPadding = 0;

        PduParser(byte[] pdu) {
            this.mPdu = pdu;
        }

        /* access modifiers changed from: package-private */
        public String getSCAddress() {
            String ret;
            int len = getByte();
            if (len == 0) {
                ret = null;
            } else {
                try {
                    ret = PhoneNumberUtils.calledPartyBCDToString(this.mPdu, this.mCur, len, 2);
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
            return bArr[i] & MidiConstants.STATUS_RESET;
        }

        public GsmSmsAddress getAddress() {
            int lengthBytes = 2 + (((this.mPdu[this.mCur] & 255) + 1) / 2);
            try {
                GsmSmsAddress ret = new GsmSmsAddress(this.mPdu, this.mCur, lengthBytes);
                this.mCur += lengthBytes;
                return ret;
            } catch (ParseException e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        /* access modifiers changed from: package-private */
        public long getSCTimestampMillis() {
            byte[] bArr = this.mPdu;
            int i = this.mCur;
            this.mCur = i + 1;
            int year = IccUtils.gsmBcdByteToInt(bArr[i]);
            byte[] bArr2 = this.mPdu;
            int i2 = this.mCur;
            this.mCur = i2 + 1;
            int month = IccUtils.gsmBcdByteToInt(bArr2[i2]);
            byte[] bArr3 = this.mPdu;
            int i3 = this.mCur;
            this.mCur = i3 + 1;
            int day = IccUtils.gsmBcdByteToInt(bArr3[i3]);
            byte[] bArr4 = this.mPdu;
            int i4 = this.mCur;
            this.mCur = i4 + 1;
            int hour = IccUtils.gsmBcdByteToInt(bArr4[i4]);
            byte[] bArr5 = this.mPdu;
            int i5 = this.mCur;
            this.mCur = i5 + 1;
            int minute = IccUtils.gsmBcdByteToInt(bArr5[i5]);
            byte[] bArr6 = this.mPdu;
            int i6 = this.mCur;
            this.mCur = i6 + 1;
            int second = IccUtils.gsmBcdByteToInt(bArr6[i6]);
            byte[] bArr7 = this.mPdu;
            int i7 = this.mCur;
            this.mCur = i7 + 1;
            byte tzByte = bArr7[i7];
            int timezoneOffset = IccUtils.gsmBcdByteToInt((byte) (tzByte & -9));
            int timezoneOffset2 = (tzByte & 8) == 0 ? timezoneOffset : -timezoneOffset;
            Time time = new Time("UTC");
            time.year = year >= 90 ? year + 1900 : year + 2000;
            time.month = month - 1;
            time.monthDay = day;
            time.hour = hour;
            time.minute = minute;
            time.second = second;
            return time.toMillis(true) - ((long) (((timezoneOffset2 * 15) * 60) * 1000));
        }

        /* access modifiers changed from: package-private */
        public int constructUserData(boolean hasUserDataHeader, boolean dataInSeptets) {
            int bufferLen;
            int offset = this.mCur;
            int offset2 = offset + 1;
            int userDataLength = this.mPdu[offset] & 255;
            int headerSeptets = 0;
            int userDataHeaderLength = 0;
            int i = 0;
            if (hasUserDataHeader) {
                int offset3 = offset2 + 1;
                userDataHeaderLength = this.mPdu[offset2] & MidiConstants.STATUS_RESET;
                byte[] udh = new byte[userDataHeaderLength];
                System.arraycopy(this.mPdu, offset3, udh, 0, userDataHeaderLength);
                this.mUserDataHeader = SmsHeader.fromByteArray(udh);
                int offset4 = offset3 + userDataHeaderLength;
                int headerBits = (userDataHeaderLength + 1) * 8;
                headerSeptets = (headerBits / 7) + (headerBits % 7 > 0 ? 1 : 0);
                this.mUserDataSeptetPadding = (headerSeptets * 7) - headerBits;
                offset2 = offset4;
            }
            if (dataInSeptets) {
                bufferLen = this.mPdu.length - offset2;
            } else {
                bufferLen = userDataLength - (hasUserDataHeader ? userDataHeaderLength + 1 : 0);
                if (bufferLen < 0) {
                    bufferLen = 0;
                }
            }
            this.mUserData = new byte[bufferLen];
            System.arraycopy(this.mPdu, offset2, this.mUserData, 0, this.mUserData.length);
            this.mCur = offset2;
            if (!dataInSeptets) {
                return this.mUserData.length;
            }
            int count = userDataLength - headerSeptets;
            if (count >= 0) {
                i = count;
            }
            return i;
        }

        /* access modifiers changed from: package-private */
        public byte[] getUserData() {
            return this.mUserData;
        }

        /* access modifiers changed from: package-private */
        public SmsHeader getUserDataHeader() {
            return this.mUserDataHeader;
        }

        /* access modifiers changed from: package-private */
        public String getUserDataGSM7Bit(int septetCount, int languageTable, int languageShiftTable) {
            String ret = GsmAlphabet.gsm7BitPackedToString(this.mPdu, this.mCur, septetCount, this.mUserDataSeptetPadding, languageTable, languageShiftTable);
            this.mCur += (septetCount * 7) / 8;
            return ret;
        }

        /* access modifiers changed from: package-private */
        public String getUserDataGSM8bit(int byteCount) {
            String ret = GsmAlphabet.gsm8BitUnpackedToString(this.mPdu, this.mCur, byteCount);
            this.mCur += byteCount;
            return ret;
        }

        /* access modifiers changed from: package-private */
        public String getUserDataUCS2(int byteCount) {
            String ret;
            try {
                ret = new String(this.mPdu, this.mCur, byteCount, "utf-16");
            } catch (UnsupportedEncodingException ex) {
                Rlog.e(SmsMessage.LOG_TAG, "implausible UnsupportedEncodingException", ex);
                ret = "";
            }
            this.mCur += byteCount;
            return ret;
        }

        /* access modifiers changed from: package-private */
        public String getUserDataKSC5601(int byteCount) {
            String ret;
            try {
                ret = new String(this.mPdu, this.mCur, byteCount, "KSC5601");
            } catch (UnsupportedEncodingException ex) {
                Rlog.e(SmsMessage.LOG_TAG, "implausible UnsupportedEncodingException", ex);
                ret = "";
            }
            this.mCur += byteCount;
            return ret;
        }

        /* access modifiers changed from: package-private */
        public boolean moreDataPresent() {
            return this.mPdu.length > this.mCur;
        }
    }

    public static class SubmitPdu extends SmsMessageBase.SubmitPduBase {
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

    public static int getRelativeValidityPeriod(int validityPeriod) {
        int relValidityPeriod = -1;
        if (validityPeriod < 5 || validityPeriod > VALIDITY_PERIOD_MAX) {
            Rlog.e(LOG_TAG, "Invalid Validity Period" + validityPeriod);
            return -1;
        }
        if (validityPeriod <= 720) {
            relValidityPeriod = (validityPeriod / 5) - 1;
        } else if (validityPeriod <= 1440) {
            relValidityPeriod = ((validityPeriod - 720) / 30) + 143;
        } else if (validityPeriod <= 43200) {
            relValidityPeriod = (validityPeriod / MetricsProto.MetricsEvent.ACTION_HUSH_GESTURE) + 166;
        } else if (validityPeriod <= VALIDITY_PERIOD_MAX) {
            relValidityPeriod = (validityPeriod / 10080) + 192;
        }
        return relValidityPeriod;
    }

    public static SubmitPdu getSubmitPdu(String scAddress, String destinationAddress, String message, boolean statusReportRequested, byte[] header) {
        return getSubmitPdu(scAddress, destinationAddress, message, statusReportRequested, header, 0, 0, 0);
    }

    public static SubmitPdu getSubmitPdu(String scAddress, String destinationAddress, String message, boolean statusReportRequested, byte[] header, int encoding, int languageTable, int languageShiftTable) {
        return getSubmitPdu(scAddress, destinationAddress, message, statusReportRequested, header, encoding, languageTable, languageShiftTable, -1);
    }

    public static SubmitPdu getSubmitPdu(String scAddress, String destinationAddress, String message, boolean statusReportRequested, byte[] header, int encoding, int languageTable, int languageShiftTable, int validityPeriod) {
        byte[] header2;
        int languageShiftTable2;
        int languageTable2;
        int encoding2;
        byte mtiByte;
        byte[] userData;
        String str = destinationAddress;
        String str2 = message;
        if (str2 == null || str == null) {
            String str3 = scAddress;
            boolean z = statusReportRequested;
            Rlog.e(LOG_TAG, "message or destinationAddress null");
            return null;
        }
        if (encoding == 0) {
            GsmAlphabet.TextEncodingDetails ted = calculateLength(str2, false);
            int encoding3 = ted.codeUnitSize;
            languageTable2 = ted.languageTable;
            languageShiftTable2 = ted.languageShiftTable;
            if (encoding3 != 1 || (languageTable2 == 0 && languageShiftTable2 == 0)) {
                header2 = header;
            } else if (header != null) {
                SmsHeader smsHeader = SmsHeader.fromByteArray(header);
                if (smsHeader.languageTable == languageTable2 && smsHeader.languageShiftTable == languageShiftTable2) {
                    header2 = header;
                } else {
                    Rlog.w(LOG_TAG, "Updating language table in SMS header: " + smsHeader.languageTable + " -> " + languageTable2 + ", " + smsHeader.languageShiftTable + " -> " + languageShiftTable2);
                    smsHeader.languageTable = languageTable2;
                    smsHeader.languageShiftTable = languageShiftTable2;
                    header2 = SmsHeader.toByteArray(smsHeader);
                }
            } else {
                SmsHeader smsHeader2 = new SmsHeader();
                smsHeader2.languageTable = languageTable2;
                smsHeader2.languageShiftTable = languageShiftTable2;
                header2 = SmsHeader.toByteArray(smsHeader2);
            }
            encoding2 = encoding3;
        } else {
            header2 = header;
            encoding2 = encoding;
            languageTable2 = languageTable;
            languageShiftTable2 = languageShiftTable;
        }
        SubmitPdu ret = new SubmitPdu();
        int i = 64;
        if (isAddTPVP || hasSmsVp) {
            if (header2 == null) {
                i = 0;
            }
            mtiByte = (byte) (1 | i | 16);
        } else {
            if (header2 == null) {
                i = 0;
            }
            mtiByte = (byte) (1 | i);
        }
        ByteArrayOutputStream bo = getSubmitPduHead(scAddress, str, mtiByte, statusReportRequested, ret);
        if (bo == null) {
            return ret;
        }
        if (encoding2 == 1) {
            try {
                userData = GsmAlphabet.stringToGsm7BitPackedWithHeader(str2, header2, languageTable2, languageShiftTable2);
            } catch (EncodeException e) {
                EncodeException encodeException = e;
                try {
                    userData = encodeUCS2(str2, header2);
                    encoding2 = 3;
                } catch (UnsupportedEncodingException uex) {
                    UnsupportedEncodingException unsupportedEncodingException = uex;
                    Rlog.e(LOG_TAG, "Implausible UnsupportedEncodingException ", uex);
                    return null;
                }
            }
        } else {
            try {
                userData = encodeUCS2(str2, header2);
            } catch (UnsupportedEncodingException uex2) {
                UnsupportedEncodingException unsupportedEncodingException2 = uex2;
                Rlog.e(LOG_TAG, "Implausible UnsupportedEncodingException ", uex2);
                return null;
            }
        }
        if (encoding2 == 1) {
            if ((userData[0] & MidiConstants.STATUS_RESET) > 160) {
                Rlog.e(LOG_TAG, "Message too long (" + (userData[0] & MidiConstants.STATUS_RESET) + " septets)");
                return null;
            }
            bo.write(0);
        } else if ((userData[0] & MidiConstants.STATUS_RESET) > 140) {
            Rlog.e(LOG_TAG, "Message too long (" + (userData[0] & MidiConstants.STATUS_RESET) + " bytes)");
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
        Rlog.d(LOG_TAG, "pdu size" + ret.encodedMessage.length);
        return ret;
    }

    public static byte[] encodeUCS2(String message, byte[] header) throws UnsupportedEncodingException {
        byte[] userData;
        byte[] textPart = message.getBytes("utf-16be");
        if (header != null) {
            userData = new byte[(header.length + textPart.length + 1)];
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
        return getSubmitPdu(scAddress, destinationAddress, message, statusReportRequested, (byte[]) null);
    }

    public static SubmitPdu getSubmitPdu(String scAddress, String destinationAddress, String message, boolean statusReportRequested, int validityPeriod) {
        return getSubmitPdu(scAddress, destinationAddress, message, statusReportRequested, null, 0, 0, 0, validityPeriod);
    }

    public static SubmitPdu getSubmitPdu(String scAddress, String destinationAddress, int destinationPort, byte[] data, boolean statusReportRequested) {
        SmsHeader.PortAddrs portAddrs = new SmsHeader.PortAddrs();
        portAddrs.destPort = destinationPort;
        portAddrs.origPort = 0;
        portAddrs.areEightBits = false;
        SmsHeader smsHeader = new SmsHeader();
        smsHeader.portAddrs = portAddrs;
        if ((destinationAddress.equals("1065840409") || destinationAddress.equals("10654040")) && destinationPort == 16998) {
            portAddrs.origPort = 16998;
        }
        byte[] smsHeaderData = SmsHeader.toByteArray(smsHeader);
        if (data.length + smsHeaderData.length + 1 > 140) {
            Rlog.e(LOG_TAG, "SMS data message may only contain " + ((140 - smsHeaderData.length) - 1) + " bytes");
            return null;
        }
        SubmitPdu ret = new SubmitPdu();
        ByteArrayOutputStream bo = getSubmitPduHead(scAddress, destinationAddress, (byte) 65, statusReportRequested, ret);
        if (bo == null) {
            return ret;
        }
        bo.write(4);
        bo.write(data.length + smsHeaderData.length + 1);
        bo.write(smsHeaderData.length);
        bo.write(smsHeaderData, 0, smsHeaderData.length);
        bo.write(data, 0, data.length);
        ret.encodedMessage = bo.toByteArray();
        return ret;
    }

    private static ByteArrayOutputStream getSubmitPduHead(String scAddress, String destinationAddress, byte mtiByte, boolean statusReportRequested, SubmitPdu ret) {
        ByteArrayOutputStream bo = new ByteArrayOutputStream(180);
        if (scAddress == null) {
            Rlog.e(LOG_TAG, "scAddress is null");
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
        if (daBytes == null) {
            return null;
        }
        int i = 1;
        int length = (daBytes.length - 1) * 2;
        if ((daBytes[daBytes.length - 1] & 240) != 240) {
            i = 0;
        }
        bo.write(length - i);
        bo.write(daBytes, 0, daBytes.length);
        bo.write(0);
        return bo;
    }

    public static GsmAlphabet.TextEncodingDetails calculateLength(CharSequence msgBody, boolean use7bitOnly) {
        CharSequence newMsgBody = null;
        if (Resources.getSystem().getBoolean(17957028)) {
            newMsgBody = Sms7BitEncodingTranslator.translate(msgBody);
        }
        if (TextUtils.isEmpty(newMsgBody)) {
            newMsgBody = msgBody;
        }
        GsmAlphabet.TextEncodingDetails ted = GsmAlphabet.countGsmSeptets(newMsgBody, use7bitOnly);
        if (ted == null) {
            return SmsMessageBase.calcUnicodeEncodingDetails(newMsgBody);
        }
        return ted;
    }

    public int getProtocolIdentifier() {
        return this.mProtocolIdentifier;
    }

    /* access modifiers changed from: package-private */
    public int getDataCodingScheme() {
        return this.mDataCodingScheme;
    }

    public boolean isReplace() {
        return (this.mProtocolIdentifier & 192) == 64 && (this.mProtocolIdentifier & 63) > 0 && (this.mProtocolIdentifier & 63) < 8;
    }

    public boolean isCphsMwiMessage() {
        boolean z = false;
        if (this.mOriginatingAddress == null) {
            return false;
        }
        if (((GsmSmsAddress) this.mOriginatingAddress).isCphsVoiceMessageClear() || ((GsmSmsAddress) this.mOriginatingAddress).isCphsVoiceMessageSet()) {
            z = true;
        }
        return z;
    }

    public boolean isMWIClearMessage() {
        boolean z = true;
        if (this.mIsMwi && !this.mMwiSense) {
            return true;
        }
        if (this.mOriginatingAddress == null || !((GsmSmsAddress) this.mOriginatingAddress).isCphsVoiceMessageClear()) {
            z = false;
        }
        return z;
    }

    public boolean isMWISetMessage() {
        boolean z = true;
        if (this.mIsMwi && this.mMwiSense) {
            return true;
        }
        if (this.mOriginatingAddress == null || !((GsmSmsAddress) this.mOriginatingAddress).isCphsVoiceMessageSet()) {
            z = false;
        }
        return z;
    }

    public boolean isMwiDontStore() {
        if (this.mIsMwi && this.mMwiDontStore) {
            return true;
        }
        if (!isCphsMwiMessage() || !" ".equals(getMessageBody())) {
            return false;
        }
        return true;
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
        this.blacklistFlag = HwFrameworkFactory.getHwBaseInnerSmsManager().checkSmsBlacklistFlag(p);
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
        boolean hasUserDataHeader = true;
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
                    if ((firstByte & 64) != 64) {
                        hasUserDataHeader = false;
                    }
                    parseUserData(p, hasUserDataHeader);
                }
            }
        }
    }

    private void parseSmsDeliver(PduParser p, int firstByte) {
        boolean hasUserDataHeader = false;
        this.mReplyPathPresent = (firstByte & 128) == 128;
        this.mOriginatingAddress = p.getAddress();
        SmsAddress smsAddress = this.mOriginatingAddress;
        this.mProtocolIdentifier = p.getByte();
        this.mDataCodingScheme = p.getByte();
        this.mScTimeMillis = p.getSCTimestampMillis();
        if ((firstByte & 64) == 64) {
            hasUserDataHeader = true;
        }
        parseUserData(p, hasUserDataHeader);
    }

    private void parseSmsSubmit(PduParser p, int firstByte) {
        int validityPeriodLength;
        boolean hasUserDataHeader = false;
        this.mReplyPathPresent = (firstByte & 128) == 128;
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
            int validityPeriodLength2 = validityPeriodLength - 1;
            if (validityPeriodLength <= 0) {
                break;
            }
            p.getByte();
            validityPeriodLength = validityPeriodLength2;
        }
        if ((firstByte & 64) == 64) {
            hasUserDataHeader = true;
        }
        parseUserData(p, hasUserDataHeader);
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x01e1, code lost:
        if ((r0.mDataCodingScheme & com.android.internal.logging.nano.MetricsProto.MetricsEvent.FINGERPRINT_ENROLLING) == 224) goto L_0x01e6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x01ea, code lost:
        if ((r0.mDataCodingScheme & 3) != 0) goto L_0x01ec;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x01ec, code lost:
        r0.mMwiDontStore = true;
     */
    private void parseUserData(PduParser p, boolean hasUserDataHeader) {
        int i;
        char c;
        PduParser pduParser = p;
        boolean z = hasUserDataHeader;
        boolean hasMessageClass = false;
        int encodingType = 0;
        int i2 = 128;
        int i3 = 0;
        if ((this.mDataCodingScheme & 128) == 0) {
            boolean userDataCompressed = (this.mDataCodingScheme & 32) != 0;
            hasMessageClass = (this.mDataCodingScheme & 16) != 0;
            if (!userDataCompressed) {
                switch ((this.mDataCodingScheme >> 2) & 3) {
                    case 0:
                        encodingType = 1;
                        break;
                    case 1:
                        if (Resources.getSystem().getBoolean(17957027)) {
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
            } else {
                Rlog.w(LOG_TAG, "4 - Unsupported SMS data coding scheme (compression) " + (this.mDataCodingScheme & 255));
            }
        } else if ((this.mDataCodingScheme & MetricsProto.MetricsEvent.FINGERPRINT_ENROLLING) == 240) {
            hasMessageClass = true;
            encodingType = (this.mDataCodingScheme & 4) == 0 ? 1 : 2;
        } else if ((this.mDataCodingScheme & MetricsProto.MetricsEvent.FINGERPRINT_ENROLLING) == 192 || (this.mDataCodingScheme & MetricsProto.MetricsEvent.FINGERPRINT_ENROLLING) == 208 || (this.mDataCodingScheme & MetricsProto.MetricsEvent.FINGERPRINT_ENROLLING) == 224) {
            if ((this.mDataCodingScheme & MetricsProto.MetricsEvent.FINGERPRINT_ENROLLING) == 224) {
                encodingType = 3;
            } else {
                encodingType = 1;
            }
            boolean active = (this.mDataCodingScheme & 8) == 8;
            if ((this.mDataCodingScheme & 3) == 0) {
                this.mIsMwi = true;
                this.mMwiSense = active;
                this.mMwiDontStore = (this.mDataCodingScheme & MetricsProto.MetricsEvent.FINGERPRINT_ENROLLING) == 192;
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
        int count = pduParser.constructUserData(z, encodingType == 1);
        this.mUserData = p.getUserData();
        this.mUserDataHeader = p.getUserDataHeader();
        if (z && this.mUserDataHeader.specialSmsMsgList.size() != 0) {
            Iterator<SmsHeader.SpecialSmsMsg> it = this.mUserDataHeader.specialSmsMsgList.iterator();
            while (it.hasNext()) {
                SmsHeader.SpecialSmsMsg msg = it.next();
                int msgInd = msg.msgIndType & 255;
                if (msgInd == 0 || msgInd == i2) {
                    this.mIsMwi = true;
                    if (msgInd == i2) {
                        this.mMwiDontStore = false;
                    } else if (!this.mMwiDontStore) {
                        c = 208;
                        if ((this.mDataCodingScheme & MetricsProto.MetricsEvent.FINGERPRINT_ENROLLING) != 208) {
                        }
                    }
                    c = 208;
                    this.mVoiceMailCount = msg.msgCount & 255;
                    if (this.mVoiceMailCount > 0) {
                        this.mMwiSense = true;
                    } else {
                        this.mMwiSense = false;
                    }
                    Rlog.w(LOG_TAG, "MWI in TP-UDH for Vmail. Msg Ind = " + msgInd + " Dont store = " + this.mMwiDontStore + " Vmail count = " + this.mVoiceMailCount);
                } else {
                    Rlog.w(LOG_TAG, "TP_UDH fax/email/extended msg/multisubscriber profile. Msg Ind = " + msgInd);
                    c = 208;
                }
                char c2 = c;
                i2 = 128;
            }
        }
        switch (encodingType) {
            case 0:
                this.mMessageBody = null;
                break;
            case 1:
                if (z) {
                    i = this.mUserDataHeader.languageTable;
                } else {
                    i = 0;
                }
                if (z) {
                    i3 = this.mUserDataHeader.languageShiftTable;
                }
                this.mMessageBody = pduParser.getUserDataGSM7Bit(count, i, i3);
                break;
            case 2:
                if (!Resources.getSystem().getBoolean(17957027)) {
                    this.mMessageBody = null;
                    break;
                } else {
                    this.mMessageBody = pduParser.getUserDataGSM8bit(count);
                    break;
                }
            case 3:
                this.mMessageBody = pduParser.getUserDataUCS2(count);
                break;
            case 4:
                this.mMessageBody = pduParser.getUserDataKSC5601(count);
                break;
        }
        if (this.mMessageBody != null) {
            parseMessageBody();
        }
        if (!hasMessageClass) {
            this.messageClass = SmsConstants.MessageClass.UNKNOWN;
            return;
        }
        switch (this.mDataCodingScheme & 3) {
            case 0:
                this.messageClass = SmsConstants.MessageClass.CLASS_0;
                return;
            case 1:
                this.messageClass = SmsConstants.MessageClass.CLASS_1;
                return;
            case 2:
                this.messageClass = SmsConstants.MessageClass.CLASS_2;
                return;
            case 3:
                this.messageClass = SmsConstants.MessageClass.CLASS_3;
                return;
            default:
                return;
        }
    }

    public SmsConstants.MessageClass getMessageClass() {
        return this.messageClass;
    }

    /* access modifiers changed from: package-private */
    public boolean isUsimDataDownload() {
        return this.messageClass == SmsConstants.MessageClass.CLASS_2 && (this.mProtocolIdentifier == 127 || this.mProtocolIdentifier == 124);
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

    static {
        boolean z = false;
        if (smsValidityPeriod >= 0 && smsValidityPeriod <= 255) {
            z = true;
        }
        hasSmsVp = z;
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
