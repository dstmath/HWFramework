package com.android.internal.telephony.gsm;

import android.content.res.Resources;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.text.TextUtils;
import android.text.format.Time;
import com.android.internal.telephony.AbstractPhoneBase;
import com.android.internal.telephony.EncodeException;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.GsmAlphabet.TextEncodingDetails;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.Sms7BitEncodingTranslator;
import com.android.internal.telephony.SmsConstants.MessageClass;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.SmsHeader.PortAddrs;
import com.android.internal.telephony.SmsHeader.SpecialSmsMsg;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.SmsMessageBase.SubmitPduBase;
import com.android.internal.telephony.imsphone.CallFailCause;
import com.android.internal.telephony.uicc.IccUtils;
import com.google.android.mms.pdu.CharacterSets;
import com.google.android.mms.pdu.PduHeaders;
import com.google.android.mms.pdu.PduPart;
import com.google.android.mms.pdu.PduPersister;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;

public class SmsMessage extends SmsMessageBase {
    static final String LOG_TAG = "SmsMessage";
    private static final boolean VDBG = false;
    private static final boolean isAddTPVP = false;
    private static final boolean isAllowedCsFw = false;
    private int mDataCodingScheme;
    private boolean mIsStatusReportMessage;
    private int mMti;
    private int mProtocolIdentifier;
    private GsmSmsAddress mRecipientAddress;
    private boolean mReplyPathPresent;
    private int mStatus;
    private int mVoiceMailCount;
    private MessageClass messageClass;

    public static class PduParser {
        public int mCur;
        public byte[] mPdu;
        byte[] mUserData;
        SmsHeader mUserDataHeader;
        int mUserDataSeptetPadding;

        PduParser(byte[] pdu) {
            this.mPdu = pdu;
            this.mCur = 0;
            this.mUserDataSeptetPadding = 0;
        }

        String getSCAddress() {
            String str;
            int len = getByte();
            if (len == 0) {
                str = null;
            } else {
                try {
                    str = PhoneNumberUtils.calledPartyBCDToString(this.mPdu, this.mCur, len);
                } catch (RuntimeException tr) {
                    Rlog.d(SmsMessage.LOG_TAG, "invalid SC address: ", tr);
                    str = null;
                }
            }
            this.mCur += len;
            return str;
        }

        public int getByte() {
            byte[] bArr = this.mPdu;
            int i = this.mCur;
            this.mCur = i + 1;
            return bArr[i] & PduHeaders.STORE_STATUS_ERROR_END;
        }

        public GsmSmsAddress getAddress() {
            int lengthBytes = (((this.mPdu[this.mCur] & PduHeaders.STORE_STATUS_ERROR_END) + 1) / 2) + 2;
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
            Time time = new Time("UTC");
            time.year = year >= 90 ? year + 1900 : year + ServiceStateTracker.NITZ_UPDATE_DIFF_DEFAULT;
            time.month = month - 1;
            time.monthDay = day;
            time.hour = hour;
            time.minute = minute;
            time.second = second;
            return time.toMillis(true) - ((long) (((timezoneOffset * 15) * 60) * CharacterSets.UCS2));
        }

        int constructUserData(boolean hasUserDataHeader, boolean dataInSeptets) {
            int i;
            int bufferLen;
            int i2 = 0;
            int offset = this.mCur;
            int offset2 = offset + 1;
            int userDataLength = this.mPdu[offset] & PduHeaders.STORE_STATUS_ERROR_END;
            int headerSeptets = 0;
            int userDataHeaderLength = 0;
            if (hasUserDataHeader) {
                offset = offset2 + 1;
                userDataHeaderLength = this.mPdu[offset2] & PduHeaders.STORE_STATUS_ERROR_END;
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
                ret = new String(this.mPdu, this.mCur, byteCount, CharacterSets.MIMENAME_UTF_16);
            } catch (UnsupportedEncodingException ex) {
                ret = "";
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
                ret = "";
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
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.gsm.SmsMessage.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.gsm.SmsMessage.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.gsm.SmsMessage.<clinit>():void");
    }

    public SmsMessage() {
        this.mReplyPathPresent = false;
        this.mIsStatusReportMessage = false;
        this.mVoiceMailCount = 0;
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

    public static SmsMessage newFromCMT(String[] lines) {
        try {
            SmsMessage msg = new SmsMessage();
            msg.parsePdu(IccUtils.hexStringToBytes(lines[1]));
            return msg;
        } catch (RuntimeException ex) {
            Rlog.e(LOG_TAG, "SMS PDU parsing failed: ", ex);
            return null;
        }
    }

    public static SmsMessage newFromCDS(String line) {
        try {
            SmsMessage msg = new SmsMessage();
            msg.parsePdu(IccUtils.hexStringToBytes(line));
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
        if (isAddTPVP) {
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
            if ((userData[0] & PduHeaders.STORE_STATUS_ERROR_END) > PduHeaders.PREVIOUSLY_SENT_BY) {
                Rlog.e(LOG_TAG, "Message too long (" + (userData[0] & PduHeaders.STORE_STATUS_ERROR_END) + " septets)");
                return null;
            }
            bo.write(0);
        } else if ((userData[0] & PduHeaders.STORE_STATUS_ERROR_END) > PduPart.P_DEP_COMMENT) {
            Rlog.e(LOG_TAG, "Message too long (" + (userData[0] & PduHeaders.STORE_STATUS_ERROR_END) + " bytes)");
            return null;
        } else {
            bo.write(8);
        }
        if (isAddTPVP) {
            bo.write(PduHeaders.STORE_STATUS_ERROR_END);
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
        ret[0] = (byte) (userData.length & PduHeaders.STORE_STATUS_ERROR_END);
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
        if ((data.length + smsHeaderData.length) + 1 > PduPart.P_DEP_COMMENT) {
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
        ByteArrayOutputStream bo = new ByteArrayOutputStream(PduHeaders.RECOMMENDED_RETRIEVAL_MODE);
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
        if ((daBytes[daBytes.length - 1] & CallFailCause.CALL_BARRED) == CallFailCause.CALL_BARRED) {
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
        if (Resources.getSystem().getBoolean(17957019)) {
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
        if ((this.mProtocolIdentifier & PduPart.P_CONTENT_ID) != 64 || (this.mProtocolIdentifier & 63) <= 0 || (this.mProtocolIdentifier & 63) >= 8) {
            return false;
        }
        return true;
    }

    public boolean isCphsMwiMessage() {
        if (((GsmSmsAddress) this.mOriginatingAddress).isCphsVoiceMessageClear()) {
            return true;
        }
        return ((GsmSmsAddress) this.mOriginatingAddress).isCphsVoiceMessageSet();
    }

    public boolean isMWIClearMessage() {
        if (this.mIsMwi && !this.mMwiSense) {
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
        int firstByte;
        this.mPdu = pdu;
        PduParser p = new PduParser(pdu);
        if (isAllowedCsFw) {
            if (PduHeaders.STORE_STATUS_ERROR_END == (p.mPdu[p.mCur] & PduHeaders.STORE_STATUS_ERROR_END)) {
                p.mCur++;
                this.blacklistFlag = true;
                Rlog.d(LOG_TAG, "parsePdu blacklistFlag: " + this.blacklistFlag + "p.mCur: " + p.mCur);
            } else {
                this.blacklistFlag = false;
            }
        }
        this.mScAddress = p.getSCAddress();
        if (this.mScAddress != null) {
            firstByte = p.getByte();
            this.mMti = firstByte & 3;
        } else {
            firstByte = p.getByte();
            this.mMti = firstByte & 3;
        }
        if (!HwTelephonyFactory.getHwInnerSmsManager().parseGsmSmsSubmit(this, this.mMti, p, firstByte)) {
            switch (this.mMti) {
                case PduPersister.LOAD_MODE_MMS_COMMON /*0*/:
                case PduPersister.PROC_STATUS_COMPLETED /*3*/:
                    parseSmsDeliver(p, firstByte);
                    break;
                case PduPersister.PROC_STATUS_TRANSIENT_FAILURE /*1*/:
                    parseSmsSubmit(p, firstByte);
                    break;
                case PduPersister.PROC_STATUS_PERMANENTLY_FAILURE /*2*/:
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
            while ((moreExtraParams & PduPart.P_Q) != 0) {
                moreExtraParams = p.getByte();
            }
            if ((extraParams & AbstractPhoneBase.BUFFER_SIZE) == 0) {
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
        boolean z;
        if ((firstByte & PduPart.P_Q) == PduPart.P_Q) {
            z = true;
        } else {
            z = false;
        }
        this.mReplyPathPresent = z;
        this.mOriginatingAddress = p.getAddress();
        if (this.mOriginatingAddress != null) {
            this.mProtocolIdentifier = p.getByte();
            this.mDataCodingScheme = p.getByte();
            this.mScTimeMillis = p.getSCTimestampMillis();
        } else {
            this.mProtocolIdentifier = p.getByte();
            this.mDataCodingScheme = p.getByte();
            this.mScTimeMillis = p.getSCTimestampMillis();
        }
        parseUserData(p, (firstByte & 64) == 64);
    }

    private void parseSmsSubmit(PduParser p, int firstByte) {
        boolean z;
        int validityPeriodFormat;
        int validityPeriodLength;
        boolean hasUserDataHeader;
        if ((firstByte & PduPart.P_Q) == PduPart.P_Q) {
            z = true;
        } else {
            z = false;
        }
        this.mReplyPathPresent = z;
        this.mMessageRef = p.getByte();
        this.mRecipientAddress = p.getAddress();
        if (this.mRecipientAddress != null) {
            this.mProtocolIdentifier = p.getByte();
            this.mDataCodingScheme = p.getByte();
            validityPeriodFormat = (firstByte >> 3) & 3;
        } else {
            this.mProtocolIdentifier = p.getByte();
            this.mDataCodingScheme = p.getByte();
            validityPeriodFormat = (firstByte >> 3) & 3;
        }
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
        } else {
            hasUserDataHeader = false;
        }
        parseUserData(p, hasUserDataHeader);
    }

    private void parseUserData(PduParser p, boolean hasUserDataHeader) {
        boolean hasMessageClass = false;
        int encodingType = 0;
        if ((this.mDataCodingScheme & PduPart.P_Q) == 0) {
            boolean userDataCompressed = (this.mDataCodingScheme & 32) != 0;
            hasMessageClass = (this.mDataCodingScheme & 16) != 0;
            if (!userDataCompressed) {
                switch ((this.mDataCodingScheme >> 2) & 3) {
                    case PduPersister.LOAD_MODE_MMS_COMMON /*0*/:
                        encodingType = 1;
                        break;
                    case PduPersister.PROC_STATUS_TRANSIENT_FAILURE /*1*/:
                        encodingType = 2;
                        break;
                    case PduPersister.PROC_STATUS_PERMANENTLY_FAILURE /*2*/:
                        encodingType = 3;
                        break;
                    case PduPersister.PROC_STATUS_COMPLETED /*3*/:
                        Rlog.w(LOG_TAG, "1 - Unsupported SMS data coding scheme " + (this.mDataCodingScheme & PduHeaders.STORE_STATUS_ERROR_END));
                        encodingType = 0;
                        break;
                    default:
                        break;
                }
            }
            Rlog.w(LOG_TAG, "4 - Unsupported SMS data coding scheme (compression) " + (this.mDataCodingScheme & PduHeaders.STORE_STATUS_ERROR_END));
        } else if ((this.mDataCodingScheme & CallFailCause.CALL_BARRED) == CallFailCause.CALL_BARRED) {
            hasMessageClass = true;
            encodingType = (this.mDataCodingScheme & 4) == 0 ? 1 : 2;
        } else if ((this.mDataCodingScheme & CallFailCause.CALL_BARRED) == PduPart.P_CONTENT_ID || (this.mDataCodingScheme & CallFailCause.CALL_BARRED) == BerTlv.BER_PROACTIVE_COMMAND_TAG || (this.mDataCodingScheme & CallFailCause.CALL_BARRED) == PduHeaders.STORE_STATUS_ERROR_PERMANENT_FAILURE) {
            if ((this.mDataCodingScheme & CallFailCause.CALL_BARRED) == PduHeaders.STORE_STATUS_ERROR_PERMANENT_FAILURE) {
                encodingType = 3;
            } else {
                encodingType = 1;
            }
            boolean active = (this.mDataCodingScheme & 8) == 8;
            if ((this.mDataCodingScheme & 3) == 0) {
                this.mIsMwi = true;
                this.mMwiSense = active;
                this.mMwiDontStore = (this.mDataCodingScheme & CallFailCause.CALL_BARRED) == PduPart.P_CONTENT_ID;
                if (active) {
                    this.mVoiceMailCount = -1;
                } else {
                    this.mVoiceMailCount = 0;
                }
                Rlog.w(LOG_TAG, "MWI in DCS for Vmail. DCS = " + (this.mDataCodingScheme & PduHeaders.STORE_STATUS_ERROR_END) + " Dont store = " + this.mMwiDontStore + " vmail count = " + this.mVoiceMailCount);
            } else {
                this.mIsMwi = false;
                Rlog.w(LOG_TAG, "MWI in DCS for fax/email/other: " + (this.mDataCodingScheme & PduHeaders.STORE_STATUS_ERROR_END));
            }
        } else if ((this.mDataCodingScheme & PduPart.P_CONTENT_ID) != PduPart.P_Q) {
            Rlog.w(LOG_TAG, "3 - Unsupported SMS data coding scheme " + (this.mDataCodingScheme & PduHeaders.STORE_STATUS_ERROR_END));
        } else if (this.mDataCodingScheme == PduHeaders.STATUS_UNRECOGNIZED) {
            encodingType = 4;
        } else {
            Rlog.w(LOG_TAG, "5 - Unsupported SMS data coding scheme " + (this.mDataCodingScheme & PduHeaders.STORE_STATUS_ERROR_END));
        }
        int count = p.constructUserData(hasUserDataHeader, encodingType == 1);
        this.mUserData = p.getUserData();
        this.mUserDataHeader = p.getUserDataHeader();
        if (hasUserDataHeader && this.mUserDataHeader.specialSmsMsgList.size() != 0) {
            for (SpecialSmsMsg msg : this.mUserDataHeader.specialSmsMsgList) {
                int msgInd = msg.msgIndType & PduHeaders.STORE_STATUS_ERROR_END;
                if (msgInd == 0 || msgInd == PduPart.P_Q) {
                    this.mIsMwi = true;
                    if (msgInd == PduPart.P_Q) {
                        this.mMwiDontStore = false;
                    } else if (!this.mMwiDontStore) {
                        if ((this.mDataCodingScheme & CallFailCause.CALL_BARRED) == BerTlv.BER_PROACTIVE_COMMAND_TAG || (this.mDataCodingScheme & CallFailCause.CALL_BARRED) == PduHeaders.STORE_STATUS_ERROR_PERMANENT_FAILURE) {
                            if ((this.mDataCodingScheme & 3) != 0) {
                            }
                        }
                        this.mMwiDontStore = true;
                    }
                    this.mVoiceMailCount = msg.msgCount & PduHeaders.STORE_STATUS_ERROR_END;
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
            case PduPersister.LOAD_MODE_MMS_COMMON /*0*/:
                this.mMessageBody = null;
                break;
            case PduPersister.PROC_STATUS_TRANSIENT_FAILURE /*1*/:
                this.mMessageBody = p.getUserDataGSM7Bit(count, hasUserDataHeader ? this.mUserDataHeader.languageTable : 0, hasUserDataHeader ? this.mUserDataHeader.languageShiftTable : 0);
                break;
            case PduPersister.PROC_STATUS_PERMANENTLY_FAILURE /*2*/:
                this.mMessageBody = HwTelephonyFactory.getHwInnerSmsManager().getUserDataGSM8Bit(p, count);
                break;
            case PduPersister.PROC_STATUS_COMPLETED /*3*/:
                this.mMessageBody = p.getUserDataUCS2(count);
                break;
            case CharacterSets.ISO_8859_1 /*4*/:
                this.mMessageBody = p.getUserDataKSC5601(count);
                break;
        }
        if (this.mMessageBody != null) {
            parseMessageBody();
        }
        if (hasMessageClass) {
            switch (this.mDataCodingScheme & 3) {
                case PduPersister.LOAD_MODE_MMS_COMMON /*0*/:
                    this.messageClass = MessageClass.CLASS_0;
                    return;
                case PduPersister.PROC_STATUS_TRANSIENT_FAILURE /*1*/:
                    this.messageClass = MessageClass.CLASS_1;
                    return;
                case PduPersister.PROC_STATUS_PERMANENTLY_FAILURE /*2*/:
                    this.messageClass = MessageClass.CLASS_2;
                    return;
                case PduPersister.PROC_STATUS_COMPLETED /*3*/:
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
            return this.mProtocolIdentifier == com.android.internal.telephony.CallFailCause.INTERWORKING_UNSPECIFIED || this.mProtocolIdentifier == 124;
        } else {
            return false;
        }
    }

    public int getNumOfVoicemails() {
        if (!this.mIsMwi && isCphsMwiMessage()) {
            if (this.mOriginatingAddress == null || !((GsmSmsAddress) this.mOriginatingAddress).isCphsVoiceMessageSet()) {
                this.mVoiceMailCount = 0;
            } else {
                this.mVoiceMailCount = PduHeaders.STORE_STATUS_ERROR_END;
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
