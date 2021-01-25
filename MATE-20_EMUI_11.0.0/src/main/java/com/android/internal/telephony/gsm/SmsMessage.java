package com.android.internal.telephony.gsm;

import android.common.HwFrameworkFactory;
import android.content.res.Resources;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.SystemProperties;
import android.telephony.PhoneNumberUtils;
import android.telephony.PreciseDisconnectCause;
import android.telephony.Rlog;
import android.text.TextUtils;
import android.text.format.Time;
import com.android.internal.R;
import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.telephony.EncodeException;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.Sms7BitEncodingTranslator;
import com.android.internal.telephony.SmsAddress;
import com.android.internal.telephony.SmsConstants;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.uicc.IccUtils;
import com.huawei.internal.telephony.gsm.SmsMessageEx;
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
    private boolean mReplyPathPresent = false;
    private int mStatus;
    private int mVoiceMailCount = 0;
    private SmsConstants.MessageClass messageClass;

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
        if (validityPeriod < 5 || validityPeriod > VALIDITY_PERIOD_MAX) {
            Rlog.e(LOG_TAG, "Invalid Validity Period" + validityPeriod);
            return -1;
        } else if (validityPeriod <= 720) {
            return (validityPeriod / 5) - 1;
        } else {
            if (validityPeriod <= 1440) {
                return ((validityPeriod - 720) / 30) + 143;
            }
            if (validityPeriod <= 43200) {
                return (validityPeriod / MetricsProto.MetricsEvent.ACTION_HUSH_GESTURE) + 166;
            }
            if (validityPeriod <= VALIDITY_PERIOD_MAX) {
                return (validityPeriod / 10080) + 192;
            }
            return -1;
        }
    }

    public static SubmitPdu getSubmitPdu(String scAddress, String destinationAddress, String message, boolean statusReportRequested, byte[] header) {
        return getSubmitPdu(scAddress, destinationAddress, message, statusReportRequested, header, 0, 0, 0);
    }

    public static SubmitPdu getSubmitPdu(String scAddress, String destinationAddress, String message, boolean statusReportRequested, byte[] header, int encoding, int languageTable, int languageShiftTable) {
        return getSubmitPdu(scAddress, destinationAddress, message, statusReportRequested, header, encoding, languageTable, languageShiftTable, -1);
    }

    public static SubmitPdu getSubmitPdu(String scAddress, String destinationAddress, String message, boolean statusReportRequested, byte[] header, int encoding, int languageTable, int languageShiftTable, int validityPeriod) {
        SubmitPdu submitPdu;
        byte[] header2;
        int languageShiftTable2;
        int languageTable2;
        int encoding2;
        byte mtiByte;
        byte[] userData;
        if (message == null) {
            submitPdu = null;
        } else if (destinationAddress == null) {
            submitPdu = null;
        } else {
            if (encoding == 0) {
                GsmAlphabet.TextEncodingDetails ted = calculateLength(message, false);
                encoding2 = ted.codeUnitSize;
                languageTable2 = ted.languageTable;
                languageShiftTable2 = ted.languageShiftTable;
                if (encoding2 != 1 || (languageTable2 == 0 && languageShiftTable2 == 0)) {
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
                mtiByte = (byte) (i | 1 | 16);
            } else {
                if (header2 == null) {
                    i = 0;
                }
                mtiByte = (byte) (i | 1);
            }
            ByteArrayOutputStream bo = getSubmitPduHead(scAddress, destinationAddress, mtiByte, statusReportRequested, ret);
            if (bo == null) {
                return ret;
            }
            if (encoding2 == 1) {
                try {
                    userData = GsmAlphabet.stringToGsm7BitPackedWithHeader(message, header2, languageTable2, languageShiftTable2);
                } catch (EncodeException e) {
                    if (e.getError() == 1) {
                        Rlog.e(LOG_TAG, "Exceed size limitation EncodeException", e);
                        return null;
                    }
                    try {
                        userData = encodeUCS2(message, header2);
                        encoding2 = 3;
                    } catch (EncodeException ex1) {
                        Rlog.e(LOG_TAG, "Exceed size limitation EncodeException", ex1);
                        return null;
                    } catch (UnsupportedEncodingException uex) {
                        Rlog.e(LOG_TAG, "Implausible UnsupportedEncodingException ", uex);
                        return null;
                    }
                }
            } else {
                try {
                    userData = encodeUCS2(message, header2);
                } catch (UnsupportedEncodingException uex2) {
                    Rlog.e(LOG_TAG, "Implausible UnsupportedEncodingException ", uex2);
                    return null;
                }
            }
            if (encoding2 == 1) {
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
            Rlog.d(LOG_TAG, "pdu size" + ret.encodedMessage.length);
            return ret;
        }
        Rlog.e(LOG_TAG, "message or destinationAddress null");
        return submitPdu;
    }

    private static byte[] encodeUCS2(String message, byte[] header) throws UnsupportedEncodingException, EncodeException {
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
        if (userData.length <= 255) {
            byte[] ret = new byte[(userData.length + 1)];
            ret[0] = (byte) (255 & userData.length);
            System.arraycopy(userData, 0, ret, 1, userData.length);
            return ret;
        }
        throw new EncodeException("Payload cannot exceed 255 bytes", 1);
    }

    public static SubmitPdu getSubmitPdu(String scAddress, String destinationAddress, String message, boolean statusReportRequested) {
        return getSubmitPdu(scAddress, destinationAddress, message, statusReportRequested, (byte[]) null);
    }

    public static SubmitPdu getSubmitPdu(String scAddress, String destinationAddress, String message, boolean statusReportRequested, int validityPeriod) {
        return getSubmitPdu(scAddress, destinationAddress, message, statusReportRequested, null, 0, 0, 0, validityPeriod);
    }

    public static SubmitPdu getSubmitPdu(String scAddress, String destinationAddress, int destinationPort, byte[] data, boolean statusReportRequested) {
        int i;
        SmsHeader.PortAddrs portAddrs = new SmsHeader.PortAddrs();
        portAddrs.destPort = destinationPort;
        portAddrs.origPort = 0;
        portAddrs.areEightBits = false;
        SmsHeader smsHeader = new SmsHeader();
        smsHeader.portAddrs = portAddrs;
        if ((destinationAddress.equals("1065840409") || destinationAddress.equals("10654040")) && destinationPort == 16998) {
            portAddrs.origPort = 16998;
        }
        boolean isCTAutoRegMessage = SmsMessageBase.CT_SMS_SEND_CENTER.equals(destinationAddress);
        byte[] smsHeaderData = SmsHeader.toByteArray(smsHeader);
        byte mtiByte = 1;
        int smsHeaderDataLengthIncludeHead = isCTAutoRegMessage ? 0 : smsHeaderData.length + 1;
        Rlog.d(LOG_TAG, "data.length = " + data.length + "isCTAutoRegMessage " + isCTAutoRegMessage);
        if (data.length + smsHeaderDataLengthIncludeHead > 140) {
            Rlog.e(LOG_TAG, "SMS data message may only contain " + (140 - smsHeaderDataLengthIncludeHead) + " bytes");
            return null;
        }
        if (!isCTAutoRegMessage) {
            mtiByte = 65;
        }
        SubmitPdu ret = new SubmitPdu();
        ByteArrayOutputStream bo = getSubmitPduHead(scAddress, destinationAddress, mtiByte, statusReportRequested, ret);
        if (bo == null) {
            return ret;
        }
        bo.write(4);
        Rlog.d(LOG_TAG, "put the length is " + (data.length + smsHeaderDataLengthIncludeHead));
        bo.write(data.length + smsHeaderDataLengthIncludeHead);
        if (!isCTAutoRegMessage) {
            Rlog.d(LOG_TAG, "It is not reg message, do!!!");
            bo.write(smsHeaderData.length);
            i = 0;
            bo.write(smsHeaderData, 0, smsHeaderData.length);
        } else {
            i = 0;
            Rlog.d(LOG_TAG, "It is reg message, do nothing.");
        }
        bo.write(data, i, data.length);
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

    public static class PduParser {
        int mCur = 0;
        byte[] mPdu;
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

        /* access modifiers changed from: package-private */
        public int getByte() {
            byte[] bArr = this.mPdu;
            int i = this.mCur;
            this.mCur = i + 1;
            return bArr[i] & 255;
        }

        /* access modifiers changed from: package-private */
        public GsmSmsAddress getAddress() {
            byte[] bArr = this.mPdu;
            int i = this.mCur;
            int lengthBytes = (((bArr[i] & 255) + 1) / 2) + 2;
            try {
                GsmSmsAddress ret = new GsmSmsAddress(bArr, i, lengthBytes);
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
            Time time = new Time(Time.TIMEZONE_UTC);
            time.year = year >= 90 ? year + PreciseDisconnectCause.ECBM_NOT_SUPPORTED : year + 2000;
            time.month = month - 1;
            time.monthDay = day;
            time.hour = hour;
            time.minute = minute;
            time.second = second;
            return time.toMillis(true) - ((long) (((timezoneOffset2 * 15) * 60) * 1000));
        }

        /* access modifiers changed from: package-private */
        public int constructUserData(boolean hasUserDataHeader, boolean dataInSeptets) {
            int offset;
            int bufferLen;
            int offset2 = this.mCur;
            byte[] bArr = this.mPdu;
            int offset3 = offset2 + 1;
            int userDataLength = bArr[offset2] & 255;
            int headerSeptets = 0;
            int userDataHeaderLength = 0;
            if (hasUserDataHeader) {
                int offset4 = offset3 + 1;
                userDataHeaderLength = bArr[offset3] & 255;
                byte[] udh = new byte[userDataHeaderLength];
                System.arraycopy(bArr, offset4, udh, 0, userDataHeaderLength);
                this.mUserDataHeader = SmsHeader.fromByteArray(udh);
                offset = offset4 + userDataHeaderLength;
                int headerBits = (userDataHeaderLength + 1) * 8;
                headerSeptets = (headerBits / 7) + (headerBits % 7 > 0 ? 1 : 0);
                this.mUserDataSeptetPadding = (headerSeptets * 7) - headerBits;
            } else {
                offset = offset3;
            }
            if (dataInSeptets) {
                bufferLen = this.mPdu.length - offset;
            } else {
                bufferLen = userDataLength - (hasUserDataHeader ? userDataHeaderLength + 1 : 0);
                if (bufferLen < 0) {
                    bufferLen = 0;
                }
            }
            this.mUserData = new byte[bufferLen];
            byte[] bArr2 = this.mPdu;
            byte[] bArr3 = this.mUserData;
            System.arraycopy(bArr2, offset, bArr3, 0, bArr3.length);
            this.mCur = offset;
            if (!dataInSeptets) {
                return this.mUserData.length;
            }
            int count = userDataLength - headerSeptets;
            if (count < 0) {
                return 0;
            }
            return count;
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

        public GsmSmsAddress getAddressHw() {
            return getAddress();
        }

        public int getByteHw() {
            return getByte();
        }

        public byte[] getPduHw() {
            return this.mPdu;
        }

        public int getCurHw() {
            return this.mCur;
        }

        public void setCurHw(int cur) {
            this.mCur = cur;
        }
    }

    public static GsmAlphabet.TextEncodingDetails calculateLength(CharSequence msgBody, boolean use7bitOnly) {
        CharSequence newMsgBody = null;
        if (Resources.getSystem().getBoolean(R.bool.config_sms_force_7bit_encoding)) {
            newMsgBody = Sms7BitEncodingTranslator.translate(msgBody, false);
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

    @Override // com.android.internal.telephony.SmsMessageBase
    public int getProtocolIdentifier() {
        return this.mProtocolIdentifier;
    }

    /* access modifiers changed from: package-private */
    public int getDataCodingScheme() {
        return this.mDataCodingScheme;
    }

    @Override // com.android.internal.telephony.SmsMessageBase
    public boolean isReplace() {
        int i = this.mProtocolIdentifier;
        return (i & 192) == 64 && (i & 63) > 0 && (i & 63) < 8;
    }

    @Override // com.android.internal.telephony.SmsMessageBase
    public boolean isCphsMwiMessage() {
        if (this.mOriginatingAddress == null) {
            return false;
        }
        if (((GsmSmsAddress) this.mOriginatingAddress).isCphsVoiceMessageClear() || ((GsmSmsAddress) this.mOriginatingAddress).isCphsVoiceMessageSet()) {
            return true;
        }
        return false;
    }

    @Override // com.android.internal.telephony.SmsMessageBase
    public boolean isMWIClearMessage() {
        if (this.mIsMwi && !this.mMwiSense) {
            return true;
        }
        if (this.mOriginatingAddress == null || !((GsmSmsAddress) this.mOriginatingAddress).isCphsVoiceMessageClear()) {
            return false;
        }
        return true;
    }

    @Override // com.android.internal.telephony.SmsMessageBase
    public boolean isMWISetMessage() {
        if (this.mIsMwi && this.mMwiSense) {
            return true;
        }
        if (this.mOriginatingAddress == null || !((GsmSmsAddress) this.mOriginatingAddress).isCphsVoiceMessageSet()) {
            return false;
        }
        return true;
    }

    @Override // com.android.internal.telephony.SmsMessageBase
    public boolean isMwiDontStore() {
        if (this.mIsMwi && this.mMwiDontStore) {
            return true;
        }
        if (!isCphsMwiMessage() || !WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER.equals(getMessageBody())) {
            return false;
        }
        return true;
    }

    @Override // com.android.internal.telephony.SmsMessageBase
    public int getStatus() {
        return this.mStatus;
    }

    @Override // com.android.internal.telephony.SmsMessageBase
    public boolean isStatusReportMessage() {
        return this.mIsStatusReportMessage;
    }

    @Override // com.android.internal.telephony.SmsMessageBase
    public boolean isReplyPathPresent() {
        return this.mReplyPathPresent;
    }

    private void parsePdu(byte[] pdu) {
        this.mPdu = pdu;
        PduParser p = new PduParser(pdu);
        SmsMessageEx.PduParserEx pEx = new SmsMessageEx.PduParserEx();
        pEx.setPduParser(p);
        this.blacklistFlag = HwFrameworkFactory.getHwBaseInnerSmsManager().checkSmsBlacklistFlag(pEx);
        this.mScAddress = p.getSCAddress();
        String str = this.mScAddress;
        int firstByte = p.getByte();
        this.mMti = firstByte & 3;
        SmsMessageEx smsMessageEx = new SmsMessageEx();
        smsMessageEx.setSmsMessage(this);
        if (!HwFrameworkFactory.getHwBaseInnerSmsManager().parseGsmSmsSubmit(smsMessageEx, this.mMti, pEx, firstByte)) {
            int i = this.mMti;
            if (i != 0) {
                if (i == 1) {
                    parseSmsSubmit(p, firstByte);
                    return;
                } else if (i == 2) {
                    parseSmsStatusReport(p, firstByte);
                    return;
                } else if (i != 3) {
                    throw new RuntimeException("Unsupported message type");
                }
            }
            parseSmsDeliver(p, firstByte);
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
        boolean hasUserDataHeader = true;
        this.mReplyPathPresent = (firstByte & 128) == 128;
        this.mOriginatingAddress = p.getAddress();
        SmsAddress smsAddress = this.mOriginatingAddress;
        this.mProtocolIdentifier = p.getByte();
        this.mDataCodingScheme = p.getByte();
        this.mScTimeMillis = p.getSCTimestampMillis();
        if ((firstByte & 64) != 64) {
            hasUserDataHeader = false;
        }
        parseUserData(p, hasUserDataHeader);
    }

    private void parseSmsSubmit(PduParser p, int firstByte) {
        int validityPeriodLength;
        boolean hasUserDataHeader = true;
        this.mReplyPathPresent = (firstByte & 128) == 128;
        this.mMessageRef = p.getByte();
        this.mRecipientAddress = p.getAddress();
        SmsAddress smsAddress = this.mRecipientAddress;
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
        if ((firstByte & 64) != 64) {
            hasUserDataHeader = false;
        }
        parseUserData(p, hasUserDataHeader);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0054, code lost:
        if (r6 != 3) goto L_0x015b;
     */
    private void parseUserData(PduParser p, boolean hasUserDataHeader) {
        int i;
        boolean hasMessageClass = false;
        int encodingType = 0;
        int i2 = this.mDataCodingScheme;
        int i3 = 208;
        if ((i2 & 128) == 0) {
            boolean userDataCompressed = (i2 & 32) != 0;
            hasMessageClass = (this.mDataCodingScheme & 16) != 0;
            if (userDataCompressed) {
                Rlog.w(LOG_TAG, "4 - Unsupported SMS data coding scheme (compression) " + (this.mDataCodingScheme & 255));
            } else {
                int i4 = (this.mDataCodingScheme >> 2) & 3;
                if (i4 != 0) {
                    if (i4 != 1) {
                        if (i4 == 2) {
                            encodingType = 3;
                        }
                    } else if (Resources.getSystem().getBoolean(R.bool.config_sms_decode_gsm_8bit_data)) {
                        encodingType = 2;
                    }
                    Rlog.w(LOG_TAG, "1 - Unsupported SMS data coding scheme " + (this.mDataCodingScheme & 255));
                    encodingType = 2;
                } else {
                    encodingType = 1;
                }
            }
        } else if ((i2 & 240) == 240) {
            hasMessageClass = true;
            encodingType = (i2 & 4) == 0 ? 1 : 2;
        } else if ((i2 & 240) == 192 || (i2 & 240) == 208 || (i2 & 240) == 224) {
            if ((this.mDataCodingScheme & 240) == 224) {
                encodingType = 3;
            } else {
                encodingType = 1;
            }
            boolean active = (this.mDataCodingScheme & 8) == 8;
            int i5 = this.mDataCodingScheme;
            if ((i5 & 3) == 0) {
                this.mIsMwi = true;
                this.mMwiSense = active;
                this.mMwiDontStore = (i5 & 240) == 192;
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
        } else if ((i2 & 192) != 128) {
            Rlog.w(LOG_TAG, "3 - Unsupported SMS data coding scheme " + (this.mDataCodingScheme & 255));
        } else if (i2 == 132) {
            encodingType = 4;
        } else {
            Rlog.w(LOG_TAG, "5 - Unsupported SMS data coding scheme " + (this.mDataCodingScheme & 255));
        }
        int count = p.constructUserData(hasUserDataHeader, encodingType == 1);
        this.mUserData = p.getUserData();
        this.mUserDataHeader = p.getUserDataHeader();
        if (!hasUserDataHeader || this.mUserDataHeader.specialSmsMsgList.size() == 0) {
            i = 0;
        } else {
            Iterator<SmsHeader.SpecialSmsMsg> it = this.mUserDataHeader.specialSmsMsgList.iterator();
            while (it.hasNext()) {
                SmsHeader.SpecialSmsMsg msg = it.next();
                int msgInd = msg.msgIndType & 255;
                if (msgInd == 0 || msgInd == 128) {
                    this.mIsMwi = true;
                    if (msgInd == 128) {
                        this.mMwiDontStore = false;
                    } else if (!this.mMwiDontStore) {
                        int i6 = this.mDataCodingScheme;
                        if (!(((i6 & 240) == i3 || (i6 & 240) == 224) && (this.mDataCodingScheme & 3) == 0)) {
                            this.mMwiDontStore = true;
                        }
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
                i3 = 208;
            }
            i = 0;
        }
        if (encodingType == 0) {
            this.mMessageBody = null;
        } else if (encodingType == 1) {
            this.mMessageBody = p.getUserDataGSM7Bit(count, hasUserDataHeader ? this.mUserDataHeader.languageTable : i, hasUserDataHeader ? this.mUserDataHeader.languageShiftTable : i);
        } else if (encodingType != 2) {
            if (encodingType == 3) {
                this.mMessageBody = p.getUserDataUCS2(count);
            } else if (encodingType == 4) {
                this.mMessageBody = p.getUserDataKSC5601(count);
            }
        } else if (Resources.getSystem().getBoolean(R.bool.config_sms_decode_gsm_8bit_data)) {
            this.mMessageBody = p.getUserDataGSM8bit(count);
        } else {
            this.mMessageBody = null;
        }
        if (this.mMessageBody != null) {
            parseMessageBody();
        }
        if (!hasMessageClass) {
            this.messageClass = SmsConstants.MessageClass.UNKNOWN;
            return;
        }
        int i7 = this.mDataCodingScheme & 3;
        if (i7 == 0) {
            this.messageClass = SmsConstants.MessageClass.CLASS_0;
        } else if (i7 == 1) {
            this.messageClass = SmsConstants.MessageClass.CLASS_1;
        } else if (i7 == 2) {
            this.messageClass = SmsConstants.MessageClass.CLASS_2;
        } else if (i7 == 3) {
            this.messageClass = SmsConstants.MessageClass.CLASS_3;
        }
    }

    @Override // com.android.internal.telephony.SmsMessageBase
    public SmsConstants.MessageClass getMessageClass() {
        return this.messageClass;
    }

    /* access modifiers changed from: package-private */
    public boolean isUsimDataDownload() {
        int i;
        return this.messageClass == SmsConstants.MessageClass.CLASS_2 && ((i = this.mProtocolIdentifier) == 127 || i == 124);
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
        int i = smsValidityPeriod;
        if (i >= 0 && i <= 255) {
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

    public static byte[] encodeUCS2Hw(String message, byte[] header) throws UnsupportedEncodingException, EncodeException {
        return encodeUCS2(message, header);
    }
}
