package com.android.internal.telephony.cdma;

import android.content.res.Resources;
import android.os.Parcel;
import android.os.SystemProperties;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.SmsCbLocation;
import android.telephony.SmsCbMessage;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaSmsCbProgramData;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.GsmAlphabet.TextEncodingDetails;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.Sms7BitEncodingTranslator;
import com.android.internal.telephony.SmsConstants.MessageClass;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.SmsHeader.PortAddrs;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.SmsMessageBase.SubmitPduBase;
import com.android.internal.telephony.cdma.sms.BearerData;
import com.android.internal.telephony.cdma.sms.CdmaSmsAddress;
import com.android.internal.telephony.cdma.sms.CdmaSmsSubaddress;
import com.android.internal.telephony.cdma.sms.SmsEnvelope;
import com.android.internal.telephony.cdma.sms.UserData;
import com.android.internal.telephony.imsphone.CallFailCause;
import com.android.internal.util.BitwiseInputStream;
import com.android.internal.util.HexDump;
import com.google.android.mms.pdu.CharacterSets;
import com.google.android.mms.pdu.PduHeaders;
import com.google.android.mms.pdu.PduPersister;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class SmsMessage extends SmsMessageBase {
    private static final byte BEARER_DATA = (byte) 8;
    private static final byte BEARER_REPLY_OPTION = (byte) 6;
    private static final byte CAUSE_CODES = (byte) 7;
    private static final String CT_SMS_SEND_CENTER = "10659401";
    private static final byte DESTINATION_ADDRESS = (byte) 4;
    private static final byte DESTINATION_SUB_ADDRESS = (byte) 5;
    private static final String LOGGABLE_TAG = "CDMA:SMS";
    static final String LOG_TAG = "SmsMessage";
    private static final byte ORIGINATING_ADDRESS = (byte) 2;
    private static final byte ORIGINATING_SUB_ADDRESS = (byte) 3;
    private static boolean PLUS_TRANFER_IN_AP = false;
    private static final int RETURN_ACK = 1;
    private static final int RETURN_NO_ACK = 0;
    private static final byte SERVICE_CATEGORY = (byte) 1;
    private static final byte TELESERVICE_IDENTIFIER = (byte) 0;
    private static final boolean VDBG = false;
    private BearerData mBearerData;
    private SmsEnvelope mEnvelope;
    private int status;

    public static class SubmitPdu extends SubmitPduBase {
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.cdma.SmsMessage.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.cdma.SmsMessage.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cdma.SmsMessage.<clinit>():void");
    }

    public static SmsMessage createFromPdu(byte[] pdu) {
        SmsMessage msg = new SmsMessage();
        try {
            msg.parsePdu(pdu);
            return msg;
        } catch (RuntimeException ex) {
            Rlog.e(LOG_TAG, "SMS PDU parsing failed: ", ex);
            return null;
        } catch (OutOfMemoryError e) {
            Log.e(LOG_TAG, "SMS PDU parsing failed with out of memory: ", e);
            return null;
        }
    }

    public static SmsMessage newFromParcel(Parcel p) {
        int index;
        SmsMessage msg = new SmsMessage();
        SmsEnvelope env = new SmsEnvelope();
        CdmaSmsAddress addr = new CdmaSmsAddress();
        CdmaSmsSubaddress subaddr = new CdmaSmsSubaddress();
        env.teleService = p.readInt();
        if (p.readByte() != null) {
            env.messageType = RETURN_ACK;
        } else if (env.teleService == 0) {
            env.messageType = 2;
        } else {
            env.messageType = RETURN_NO_ACK;
        }
        env.serviceCategory = p.readInt();
        int addressDigitMode = p.readInt();
        addr.digitMode = (byte) (addressDigitMode & PduHeaders.STORE_STATUS_ERROR_END);
        addr.numberMode = (byte) (p.readInt() & PduHeaders.STORE_STATUS_ERROR_END);
        addr.ton = p.readInt();
        addr.numberPlan = (byte) (p.readInt() & PduHeaders.STORE_STATUS_ERROR_END);
        byte count = p.readByte();
        addr.numberOfDigits = count;
        byte[] data = new byte[count];
        for (byte index2 = (byte) 0; index2 < count; index2 += RETURN_ACK) {
            data[index2] = p.readByte();
            if (addressDigitMode == 0) {
                data[index2] = msg.convertDtmfToAscii(data[index2]);
            }
        }
        addr.origBytes = data;
        if (PLUS_TRANFER_IN_AP) {
            String number = HwCustPlusAndIddNddConvertUtils.replaceIddNddWithPlusForSms(new String(addr.origBytes, Charset.defaultCharset()));
            if (addr.ton == RETURN_ACK && number != null && number.length() > 0 && number.charAt(RETURN_NO_ACK) != '+') {
                Rlog.d(LOG_TAG, "newFromParcel ton == SmsAddress.TON_INTERNATIONAL");
                number = HwCustPlusAndIddNddConvertUtils.PLUS_PREFIX + number;
            }
            if (number != null) {
                addr.origBytes = number.getBytes(Charset.defaultCharset());
            } else {
                addr.origBytes = data;
            }
            addr.numberOfDigits = addr.origBytes.length;
        }
        subaddr.type = p.readInt();
        subaddr.odd = p.readByte();
        int count2 = p.readByte();
        if (count2 < 0) {
            count2 = RETURN_NO_ACK;
        }
        data = new byte[count2];
        for (index = RETURN_NO_ACK; index < count2; index += RETURN_ACK) {
            data[index] = p.readByte();
        }
        subaddr.origBytes = data;
        int countInt = p.readInt();
        if (countInt < 0) {
            countInt = RETURN_NO_ACK;
        }
        data = new byte[countInt];
        for (index = RETURN_NO_ACK; index < countInt; index += RETURN_ACK) {
            data[index] = p.readByte();
        }
        env.bearerData = data;
        env.origAddress = addr;
        env.origSubaddress = subaddr;
        msg.mOriginatingAddress = addr;
        msg.mEnvelope = env;
        msg.createPdu();
        return msg;
    }

    public static SmsMessage createFromEfRecord(int index, byte[] data) {
        try {
            SmsMessage msg = new SmsMessage();
            msg.mIndexOnIcc = index;
            if ((data[RETURN_NO_ACK] & RETURN_ACK) == 0) {
                Rlog.w(LOG_TAG, "SMS parsing failed: Trying to parse a free record");
                return null;
            }
            msg.mStatusOnIcc = data[RETURN_NO_ACK] & 7;
            int size = data[RETURN_ACK] & PduHeaders.STORE_STATUS_ERROR_END;
            byte[] pdu = new byte[size];
            System.arraycopy(data, 2, pdu, RETURN_NO_ACK, size);
            HwTelephonyFactory.getHwInnerSmsManager().parseRUIMPdu(msg, pdu);
            return msg;
        } catch (RuntimeException ex) {
            Rlog.e(LOG_TAG, "SMS PDU parsing failed: ", ex);
            return null;
        }
    }

    public static int getTPLayerLengthForPDU(String pdu) {
        Rlog.w(LOG_TAG, "getTPLayerLengthForPDU: is not supported in CDMA mode.");
        return RETURN_NO_ACK;
    }

    public static SubmitPdu getSubmitPdu(String scAddr, String destAddr, String message, boolean statusReportRequested, SmsHeader smsHeader) {
        if (message == null || destAddr == null) {
            return null;
        }
        UserData uData = new UserData();
        uData.payloadStr = message;
        uData.userDataHeader = smsHeader;
        return privateGetSubmitPdu(destAddr, statusReportRequested, uData);
    }

    public static SubmitPdu getSubmitPdu(String scAddr, String destAddr, int destPort, byte[] data, boolean statusReportRequested) {
        PortAddrs portAddrs = new PortAddrs();
        portAddrs.destPort = destPort;
        portAddrs.origPort = RETURN_NO_ACK;
        portAddrs.areEightBits = false;
        SmsHeader smsHeader = new SmsHeader();
        smsHeader.portAddrs = portAddrs;
        UserData uData = new UserData();
        if (CT_SMS_SEND_CENTER.equals(destAddr)) {
            uData.userDataHeader = null;
        } else {
            uData.userDataHeader = smsHeader;
        }
        uData.msgEncoding = RETURN_NO_ACK;
        uData.msgEncodingSet = true;
        uData.payload = data;
        return privateGetSubmitPdu(destAddr, statusReportRequested, uData);
    }

    public static SubmitPdu getSubmitPdu(String destAddr, UserData userData, boolean statusReportRequested) {
        return privateGetSubmitPdu(destAddr, statusReportRequested, userData);
    }

    public int getProtocolIdentifier() {
        Rlog.w(LOG_TAG, "getProtocolIdentifier: is not supported in CDMA mode.");
        return RETURN_NO_ACK;
    }

    public boolean isReplace() {
        Rlog.w(LOG_TAG, "isReplace: is not supported in CDMA mode.");
        return false;
    }

    public boolean isCphsMwiMessage() {
        Rlog.w(LOG_TAG, "isCphsMwiMessage: is not supported in CDMA mode.");
        return false;
    }

    public boolean isMWIClearMessage() {
        return this.mBearerData != null && this.mBearerData.numberOfMessages == 0;
    }

    public boolean isMWISetMessage() {
        return this.mBearerData != null && this.mBearerData.numberOfMessages > 0;
    }

    public boolean isMwiDontStore() {
        if (this.mBearerData == null || this.mBearerData.numberOfMessages <= 0 || this.mBearerData.userData != null) {
            return false;
        }
        return true;
    }

    public int getStatus() {
        return this.status << 16;
    }

    public boolean isStatusReportMessage() {
        return this.mBearerData.messageType == 4;
    }

    public boolean isReplyPathPresent() {
        Rlog.w(LOG_TAG, "isReplyPathPresent: is not supported in CDMA mode.");
        return false;
    }

    public static TextEncodingDetails calculateLength(CharSequence messageBody, boolean use7bitOnly, boolean isEntireMsg) {
        CharSequence newMsgBody = null;
        if (Resources.getSystem().getBoolean(17957019)) {
            newMsgBody = Sms7BitEncodingTranslator.translate(messageBody);
        }
        if (TextUtils.isEmpty(newMsgBody)) {
            newMsgBody = messageBody;
        }
        return BearerData.calcTextEncodingDetails(newMsgBody, use7bitOnly, isEntireMsg);
    }

    public static TextEncodingDetails calculateLength(CharSequence messageBody, boolean use7bitOnly) {
        return calculateLength(messageBody, use7bitOnly, false);
    }

    public int getTeleService() {
        return this.mEnvelope.teleService;
    }

    public int getMessageType() {
        if (this.mEnvelope.serviceCategory != 0) {
            return RETURN_ACK;
        }
        return RETURN_NO_ACK;
    }

    private void parsePdu(byte[] pdu) {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(pdu));
        SmsEnvelope env = new SmsEnvelope();
        CdmaSmsAddress addr = new CdmaSmsAddress();
        try {
            env.messageType = dis.readInt();
            env.teleService = dis.readInt();
            env.serviceCategory = dis.readInt();
            addr.digitMode = dis.readByte();
            addr.numberMode = dis.readByte();
            addr.ton = dis.readByte();
            addr.numberPlan = dis.readByte();
            int length = dis.readUnsignedByte();
            addr.numberOfDigits = length;
            if (length > pdu.length) {
                throw new RuntimeException("createFromPdu: Invalid pdu, addr.numberOfDigits " + length + " > pdu len " + pdu.length);
            }
            addr.origBytes = new byte[length];
            dis.read(addr.origBytes, RETURN_NO_ACK, length);
            env.bearerReply = dis.readInt();
            env.replySeqNo = dis.readByte();
            env.errorClass = dis.readByte();
            env.causeCode = dis.readByte();
            int bearerDataLength = dis.readInt();
            if (bearerDataLength > pdu.length) {
                throw new RuntimeException("createFromPdu: Invalid pdu, bearerDataLength " + bearerDataLength + " > pdu len " + pdu.length);
            }
            env.bearerData = new byte[bearerDataLength];
            dis.read(env.bearerData, RETURN_NO_ACK, bearerDataLength);
            dis.close();
            this.mOriginatingAddress = addr;
            env.origAddress = addr;
            this.mEnvelope = env;
            this.mPdu = pdu;
            parseSms();
        } catch (IOException ex) {
            throw new RuntimeException("createFromPdu: conversion from byte array to object failed: " + ex, ex);
        } catch (Exception ex2) {
            Rlog.e(LOG_TAG, "createFromPdu: conversion from byte array to object failed: " + ex2);
        }
    }

    private void parsePduFromEfRecord(byte[] pdu) {
        ByteArrayInputStream bais = new ByteArrayInputStream(pdu);
        DataInputStream dis = new DataInputStream(bais);
        SmsEnvelope env = new SmsEnvelope();
        CdmaSmsAddress addr = new CdmaSmsAddress();
        CdmaSmsSubaddress subAddr = new CdmaSmsSubaddress();
        try {
            env.messageType = dis.readByte();
            while (dis.available() > 0) {
                int parameterId = dis.readByte();
                int parameterLen = dis.readUnsignedByte();
                byte[] parameterData = new byte[parameterLen];
                int index;
                switch (parameterId) {
                    case RETURN_NO_ACK /*0*/:
                        env.teleService = dis.readUnsignedShort();
                        Rlog.i(LOG_TAG, "teleservice = " + env.teleService);
                        break;
                    case RETURN_ACK /*1*/:
                        env.serviceCategory = dis.readUnsignedShort();
                        break;
                    case PduPersister.PROC_STATUS_PERMANENTLY_FAILURE /*2*/:
                    case CharacterSets.ISO_8859_1 /*4*/:
                        dis.read(parameterData, RETURN_NO_ACK, parameterLen);
                        BitwiseInputStream addrBis = new BitwiseInputStream(parameterData);
                        addr.digitMode = addrBis.read(RETURN_ACK);
                        addr.numberMode = addrBis.read(RETURN_ACK);
                        int numberType = RETURN_NO_ACK;
                        int i = addr.digitMode;
                        if (r0 == RETURN_ACK) {
                            numberType = addrBis.read(3);
                            addr.ton = numberType;
                            if (addr.numberMode == 0) {
                                addr.numberPlan = addrBis.read(4);
                            }
                        }
                        addr.numberOfDigits = addrBis.read(8);
                        byte[] data = new byte[addr.numberOfDigits];
                        if (addr.digitMode == 0) {
                            index = RETURN_NO_ACK;
                            while (true) {
                                i = addr.numberOfDigits;
                                if (index < r0) {
                                    data[index] = convertDtmfToAscii((byte) (addrBis.read(4) & 15));
                                    index += RETURN_ACK;
                                }
                            }
                        } else {
                            i = addr.digitMode;
                            if (r0 != RETURN_ACK) {
                                Rlog.e(LOG_TAG, "Incorrect Digit mode");
                            } else if (addr.numberMode == 0) {
                                index = RETURN_NO_ACK;
                                while (true) {
                                    i = addr.numberOfDigits;
                                    if (index < r0) {
                                        data[index] = (byte) (addrBis.read(8) & PduHeaders.STORE_STATUS_ERROR_END);
                                        index += RETURN_ACK;
                                    }
                                }
                            } else {
                                i = addr.numberMode;
                                if (r0 != RETURN_ACK) {
                                    Rlog.e(LOG_TAG, "Originating Addr is of incorrect type");
                                } else if (numberType == 2) {
                                    Rlog.e(LOG_TAG, "TODO: Originating Addr is email id");
                                } else {
                                    Rlog.e(LOG_TAG, "TODO: Originating Addr is data network address");
                                }
                            }
                        }
                        addr.origBytes = data;
                        Rlog.i(LOG_TAG, "Originating Addr=" + addr.toString());
                        break;
                    case PduPersister.PROC_STATUS_COMPLETED /*3*/:
                    case CharacterSets.ISO_8859_2 /*5*/:
                        dis.read(parameterData, RETURN_NO_ACK, parameterLen);
                        BitwiseInputStream bitwiseInputStream = new BitwiseInputStream(parameterData);
                        subAddr.type = bitwiseInputStream.read(3);
                        subAddr.odd = bitwiseInputStream.readByteArray(RETURN_ACK)[RETURN_NO_ACK];
                        int subAddrLen = bitwiseInputStream.read(8);
                        byte[] subdata = new byte[subAddrLen];
                        for (index = RETURN_NO_ACK; index < subAddrLen; index += RETURN_ACK) {
                            subdata[index] = convertDtmfToAscii((byte) (bitwiseInputStream.read(4) & PduHeaders.STORE_STATUS_ERROR_END));
                        }
                        subAddr.origBytes = subdata;
                        break;
                    case CharacterSets.ISO_8859_3 /*6*/:
                        dis.read(parameterData, RETURN_NO_ACK, parameterLen);
                        env.bearerReply = new BitwiseInputStream(parameterData).read(6);
                        break;
                    case CharacterSets.ISO_8859_4 /*7*/:
                        dis.read(parameterData, RETURN_NO_ACK, parameterLen);
                        BitwiseInputStream ccBis = new BitwiseInputStream(parameterData);
                        env.replySeqNo = ccBis.readByteArray(6)[RETURN_NO_ACK];
                        env.errorClass = ccBis.readByteArray(2)[RETURN_NO_ACK];
                        if (env.errorClass == null) {
                            break;
                        }
                        env.causeCode = ccBis.readByteArray(8)[RETURN_NO_ACK];
                        break;
                    case CharacterSets.ISO_8859_5 /*8*/:
                        dis.read(parameterData, RETURN_NO_ACK, parameterLen);
                        env.bearerData = parameterData;
                        break;
                    default:
                        throw new Exception("unsupported parameterId (" + parameterId + ")");
                }
            }
            bais.close();
            dis.close();
        } catch (Exception ex) {
            Rlog.e(LOG_TAG, "parsePduFromEfRecord: conversion from pdu to SmsMessage failed" + ex);
        }
        this.mOriginatingAddress = addr;
        env.origAddress = addr;
        env.origSubaddress = subAddr;
        this.mEnvelope = env;
        this.mPdu = pdu;
        parseSms();
    }

    public void parseSms() {
        if (this.mEnvelope.teleService == SmsEnvelope.TELESERVICE_MWI) {
            this.mBearerData = new BearerData();
            if (this.mEnvelope.bearerData != null) {
                this.mBearerData.numberOfMessages = this.mEnvelope.bearerData[RETURN_NO_ACK] & PduHeaders.STORE_STATUS_ERROR_END;
            }
            return;
        }
        this.mBearerData = BearerData.decode(this.mEnvelope.bearerData);
        if (Rlog.isLoggable(LOGGABLE_TAG, 2)) {
            Rlog.d(LOG_TAG, "MT raw BearerData = '" + HexDump.toHexString(this.mEnvelope.bearerData) + "'");
            Rlog.d(LOG_TAG, "MT (decoded) BearerData = " + this.mBearerData);
        }
        this.mMessageRef = this.mBearerData.messageId;
        if (this.mBearerData.userData != null) {
            this.mUserData = this.mBearerData.userData.payload;
            this.mUserDataHeader = this.mBearerData.userData.userDataHeader;
            this.mMessageBody = this.mBearerData.userData.payloadStr;
        }
        if (!(this.mOriginatingAddress == null || this.mOriginatingAddress.origBytes == null)) {
            this.mOriginatingAddress.address = new String(this.mOriginatingAddress.origBytes);
            if (this.mOriginatingAddress.ton == RETURN_ACK && this.mOriginatingAddress.address.charAt(RETURN_NO_ACK) != '+') {
                this.mOriginatingAddress.address = HwCustPlusAndIddNddConvertUtils.PLUS_PREFIX + this.mOriginatingAddress.address;
            }
        }
        if (this.mBearerData.msgCenterTimeStamp != null) {
            this.mScTimeMillis = this.mBearerData.msgCenterTimeStamp.toMillis(true);
        }
        if (this.mBearerData.messageType == 4) {
            if (this.mBearerData.messageStatusSet) {
                this.status = this.mBearerData.errorClass << 8;
                this.status |= this.mBearerData.messageStatus;
                HwTelephonyFactory.getHwInnerSmsManager().doubleSmsStatusCheck(this);
            } else {
                Rlog.d(LOG_TAG, "DELIVERY_ACK message without msgStatus (" + (this.mUserData == null ? "also missing" : "does have") + " userData).");
                this.status = RETURN_NO_ACK;
            }
        } else if (!(this.mBearerData.messageType == RETURN_ACK || this.mBearerData.messageType == 2)) {
            throw new RuntimeException("Unsupported message type: " + this.mBearerData.messageType);
        }
        if (this.mMessageBody != null) {
            parseMessageBody();
        } else if (this.mUserData != null) {
        }
    }

    public SmsCbMessage parseBroadcastSms() {
        BearerData bData = BearerData.decode(this.mEnvelope.bearerData, this.mEnvelope.serviceCategory);
        if (bData == null) {
            Rlog.w(LOG_TAG, "BearerData.decode() returned null");
            return null;
        }
        if (Rlog.isLoggable(LOGGABLE_TAG, 2)) {
            Rlog.d(LOG_TAG, "MT raw BearerData = " + HexDump.toHexString(this.mEnvelope.bearerData));
        }
        return new SmsCbMessage(2, RETURN_ACK, bData.messageId, new SmsCbLocation(TelephonyManager.getDefault().getNetworkOperator()), this.mEnvelope.serviceCategory, bData.getLanguage(), bData.userData.payloadStr, bData.priority, null, bData.cmasWarningInfo);
    }

    public MessageClass getMessageClass() {
        if (this.mBearerData.displayMode == 0) {
            return MessageClass.CLASS_0;
        }
        return MessageClass.UNKNOWN;
    }

    public static synchronized int getNextMessageId() {
        int msgId;
        synchronized (SmsMessage.class) {
            msgId = SystemProperties.getInt("persist.radio.cdma.msgid", RETURN_ACK);
            String nextMsgId = Integer.toString((msgId % CallFailCause.ERROR_UNSPECIFIED) + RETURN_ACK);
            try {
                SystemProperties.set("persist.radio.cdma.msgid", nextMsgId);
                if (Rlog.isLoggable(LOGGABLE_TAG, 2)) {
                    Rlog.d(LOG_TAG, "next persist.radio.cdma.msgid = " + nextMsgId);
                    Rlog.d(LOG_TAG, "readback gets " + SystemProperties.get("persist.radio.cdma.msgid"));
                }
            } catch (RuntimeException ex) {
                Rlog.e(LOG_TAG, "set nextMessage ID failed: " + ex);
            }
        }
        return msgId;
    }

    public static CdmaSmsAddress parseAddrForSMSMO(String destAddrStr) {
        String destAddress = processPlusCodeForSMSMO(destAddrStr);
        if (destAddress == null || destAddress.isEmpty()) {
            Rlog.e(LOG_TAG, "got null or empty address after processPlusCodeForSMSMO()!");
            return null;
        }
        CdmaSmsAddress destAddr = CdmaSmsAddress.parse(destAddress);
        if (destAddr != null) {
            return destAddr;
        }
        Rlog.e(LOG_TAG, "privateGetSubmitPdu, CdmaSmsAddress parse error.");
        return null;
    }

    public static String processPlusCodeForSMSMO(String destAddrStr) {
        if (destAddrStr == null || destAddrStr.isEmpty()) {
            Rlog.e(LOG_TAG, "destAddrStr is null or empty, just return!");
            return destAddrStr;
        } else if (destAddrStr.startsWith(HwCustPlusAndIddNddConvertUtils.PLUS_PREFIX)) {
            return HwCustPlusAndIddNddConvertUtils.replacePlusCodeWithIddNddForSms(destAddrStr);
        } else {
            Rlog.e(LOG_TAG, "there is no + code, no need to convert, just return!");
            return destAddrStr;
        }
    }

    private static SubmitPdu privateGetSubmitPdu(String destAddrStr, boolean statusReportRequested, UserData userData) {
        CdmaSmsAddress destAddr;
        if (PLUS_TRANFER_IN_AP) {
            destAddr = parseAddrForSMSMO(destAddrStr);
            if (destAddr == null) {
                Rlog.e(LOG_TAG, "privateGetSubmitPdu, CdmaSmsAddress parse error.");
                return null;
            }
        }
        destAddr = CdmaSmsAddress.parse(PhoneNumberUtils.cdmaCheckAndProcessPlusCodeForSms(destAddrStr));
        if (destAddr == null) {
            return null;
        }
        BearerData bearerData = new BearerData();
        bearerData.messageType = 2;
        bearerData.messageId = getNextMessageId();
        if (SystemProperties.getBoolean("ro.sms.roam_sms_no_dreport", false) && statusReportRequested) {
            int cdmaSubId = HwTelephonyFactory.getHwInnerSmsManager().getCdmaSub();
            boolean isRoaming = TelephonyManager.getDefault().isNetworkRoaming(cdmaSubId);
            Rlog.d(LOG_TAG, "cdmaSubId " + cdmaSubId + ", isRoaming " + isRoaming);
            if (isRoaming && userData != null) {
                statusReportRequested = false;
            }
        }
        bearerData.deliveryAckReq = statusReportRequested;
        bearerData.userAckReq = false;
        bearerData.readAckReq = false;
        bearerData.reportReq = false;
        bearerData.userData = userData;
        byte[] encodedBearerData = BearerData.encode(bearerData);
        if (Rlog.isLoggable(LOGGABLE_TAG, 2)) {
            Rlog.d(LOG_TAG, "MO (encoded) BearerData = " + bearerData);
            Rlog.d(LOG_TAG, "MO raw BearerData = '" + HexDump.toHexString(encodedBearerData) + "'");
        }
        if (encodedBearerData == null) {
            return null;
        }
        int teleservice = bearerData.hasUserDataHeader ? SmsEnvelope.TELESERVICE_WEMT : SmsEnvelope.TELESERVICE_WMT;
        SmsEnvelope envelope = new SmsEnvelope();
        envelope.messageType = RETURN_NO_ACK;
        envelope.teleService = teleservice;
        envelope.destAddress = destAddr;
        envelope.bearerReply = RETURN_ACK;
        envelope.bearerData = encodedBearerData;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(100);
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeInt(envelope.teleService);
            dos.writeInt(RETURN_NO_ACK);
            dos.writeInt(RETURN_NO_ACK);
            dos.write(destAddr.digitMode);
            dos.write(destAddr.numberMode);
            dos.write(destAddr.ton);
            dos.write(destAddr.numberPlan);
            dos.write(destAddr.numberOfDigits);
            dos.write(destAddr.origBytes, RETURN_NO_ACK, destAddr.origBytes.length);
            dos.write(RETURN_NO_ACK);
            dos.write(RETURN_NO_ACK);
            dos.write(RETURN_NO_ACK);
            dos.write(encodedBearerData.length);
            dos.write(encodedBearerData, RETURN_NO_ACK, encodedBearerData.length);
            dos.close();
            SubmitPdu pdu = new SubmitPdu();
            pdu.encodedMessage = baos.toByteArray();
            pdu.encodedScAddress = null;
            return pdu;
        } catch (IOException ex) {
            Rlog.e(LOG_TAG, "creating SubmitPdu failed: " + ex);
            return null;
        }
    }

    private void createPdu() {
        SmsEnvelope env = this.mEnvelope;
        CdmaSmsAddress addr = env.origAddress;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(100);
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(baos));
        try {
            dos.writeInt(env.messageType);
            dos.writeInt(env.teleService);
            dos.writeInt(env.serviceCategory);
            dos.writeByte(addr.digitMode);
            dos.writeByte(addr.numberMode);
            dos.writeByte(addr.ton);
            dos.writeByte(addr.numberPlan);
            dos.writeByte(addr.numberOfDigits);
            dos.write(addr.origBytes, RETURN_NO_ACK, addr.origBytes.length);
            dos.writeInt(env.bearerReply);
            dos.writeByte(env.replySeqNo);
            dos.writeByte(env.errorClass);
            dos.writeByte(env.causeCode);
            dos.writeInt(env.bearerData.length);
            dos.write(env.bearerData, RETURN_NO_ACK, env.bearerData.length);
            dos.close();
            this.mPdu = baos.toByteArray();
        } catch (IOException ex) {
            Rlog.e(LOG_TAG, "createPdu: conversion from object to byte array failed: " + ex);
        }
    }

    private byte convertDtmfToAscii(byte dtmfDigit) {
        switch (dtmfDigit) {
            case RETURN_NO_ACK /*0*/:
                return (byte) 68;
            case RETURN_ACK /*1*/:
                return (byte) 49;
            case PduPersister.PROC_STATUS_PERMANENTLY_FAILURE /*2*/:
                return (byte) 50;
            case PduPersister.PROC_STATUS_COMPLETED /*3*/:
                return (byte) 51;
            case CharacterSets.ISO_8859_1 /*4*/:
                return (byte) 52;
            case CharacterSets.ISO_8859_2 /*5*/:
                return (byte) 53;
            case CharacterSets.ISO_8859_3 /*6*/:
                return (byte) 54;
            case CharacterSets.ISO_8859_4 /*7*/:
                return (byte) 55;
            case CharacterSets.ISO_8859_5 /*8*/:
                return (byte) 56;
            case CharacterSets.ISO_8859_6 /*9*/:
                return (byte) 57;
            case CharacterSets.ISO_8859_7 /*10*/:
                return (byte) 48;
            case CharacterSets.ISO_8859_8 /*11*/:
                return (byte) 42;
            case CharacterSets.ISO_8859_9 /*12*/:
                return (byte) 35;
            case UserData.ASCII_CR_INDEX /*13*/:
                return (byte) 65;
            case SmsHeader.ELT_ID_LARGE_ANIMATION /*14*/:
                return (byte) 66;
            case SignalToneUtil.IS95_CONST_IR_SIG_ISDN_OFF /*15*/:
                return (byte) 67;
            default:
                return (byte) 32;
        }
    }

    public int getNumOfVoicemails() {
        return this.mBearerData.numberOfMessages;
    }

    public byte[] getIncomingSmsFingerprint() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(this.mEnvelope.serviceCategory);
        output.write(this.mEnvelope.teleService);
        output.write(this.mEnvelope.origAddress.origBytes, RETURN_NO_ACK, this.mEnvelope.origAddress.origBytes.length);
        output.write(this.mEnvelope.bearerData, RETURN_NO_ACK, this.mEnvelope.bearerData.length);
        output.write(this.mEnvelope.origSubaddress.origBytes, RETURN_NO_ACK, this.mEnvelope.origSubaddress.origBytes.length);
        return output.toByteArray();
    }

    public ArrayList<CdmaSmsCbProgramData> getSmsCbProgramData() {
        return this.mBearerData.serviceCategoryProgramData;
    }
}
