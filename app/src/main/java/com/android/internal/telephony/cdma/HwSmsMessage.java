package com.android.internal.telephony.cdma;

import android.os.SystemProperties;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.telephony.HwSubscriptionManager;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.SmsMessageBaseUtils;
import com.android.internal.telephony.cdma.SmsMessage.SubmitPdu;
import com.android.internal.telephony.cdma.sms.BearerData;
import com.android.internal.telephony.cdma.sms.BearerData.TimeStamp;
import com.android.internal.telephony.cdma.sms.CdmaSmsAddress;
import com.android.internal.telephony.cdma.sms.SmsEnvelope;
import com.android.internal.telephony.cdma.sms.UserData;
import com.android.internal.telephony.vsim.HwVSimEventReport;
import com.android.internal.telephony.vsim.HwVSimSlotSwitchController;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;
import com.android.internal.util.BitwiseInputStream;
import com.android.internal.util.BitwiseInputStream.AccessException;
import com.android.internal.util.HexDump;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class HwSmsMessage {
    private static final byte BEARER_DATA = (byte) 8;
    private static final byte BEARER_REPLY_OPTION = (byte) 6;
    private static final byte CAUSE_CODES = (byte) 7;
    private static final byte[] DELIVERY_ACK_SUCCESS = null;
    private static final String DELIVERY_ACK_SUCCESS_EN = "succ";
    private static final byte DESTINATION_ADDRESS = (byte) 4;
    private static final byte DESTINATION_SUB_ADDRESS = (byte) 5;
    private static final String LOGGABLE_TAG = "CDMA:SMS";
    static final String LOG_TAG = "SmsMessage";
    private static final byte ORIGINATING_ADDRESS = (byte) 2;
    private static final byte ORIGINATING_SUB_ADDRESS = (byte) 3;
    private static boolean PLUS_TRANFER_IN_AP = false;
    private static final byte SERVICE_CATEGORY = (byte) 1;
    private static final byte TELESERVICE_IDENTIFIER = (byte) 0;
    private static final boolean VDBG = false;
    private static SmsMessageUtils smsMessageUtils;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.cdma.HwSmsMessage.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.cdma.HwSmsMessage.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cdma.HwSmsMessage.<clinit>():void");
    }

    public static SubmitPdu getSubmitDeliverPdu(boolean isSubmitPdu, String mcTimeStamp, String destAddr, String message, SmsHeader smsHeader) {
        if (message == null || destAddr == null) {
            Log.e("CDMA/SmsMessage", "empty message or destAddr");
            return null;
        }
        UserData uData = new UserData();
        uData.payloadStr = message;
        uData.userDataHeader = smsHeader;
        return privateGetSubmitDeliverPdu(isSubmitPdu, destAddr, mcTimeStamp, uData);
    }

    private static SubmitPdu privateGetSubmitDeliverPdu(boolean isSubmitPdu, String origAddrStr, String mcTimestamp, UserData userData) {
        CdmaSmsAddress destAddr;
        if (PLUS_TRANFER_IN_AP) {
            destAddr = SmsMessage.parseAddrForSMSMO(origAddrStr);
            if (destAddr == null) {
                Rlog.e(LOG_TAG, "privateGetSubmitDeliverPdu, CdmaSmsAddress parse error.");
                return null;
            }
        }
        destAddr = CdmaSmsAddress.parse(PhoneNumberUtils.cdmaCheckAndProcessPlusCodeForSms(origAddrStr));
        if (destAddr == null) {
            return null;
        }
        BearerData bearerData = new BearerData();
        bearerData.messageId = SmsMessage.getNextMessageId();
        bearerData.userAckReq = false;
        bearerData.readAckReq = false;
        bearerData.reportReq = false;
        bearerData.deliveryAckReq = false;
        if (isSubmitPdu) {
            bearerData.messageType = 2;
            bearerData.priorityIndicatorSet = true;
            bearerData.priority = 0;
        } else {
            bearerData.messageType = 1;
            TimeStamp msgTimeStamp = new TimeStamp();
            msgTimeStamp.set(Long.parseLong(mcTimestamp));
            bearerData.msgCenterTimeStamp = msgTimeStamp;
        }
        bearerData.userData = userData;
        byte[] encodedBearerData = BearerData.encode(bearerData);
        if (Rlog.isLoggable(LOGGABLE_TAG, 2)) {
            Rlog.d(LOG_TAG, "MO (encoded) BearerData = " + bearerData);
            if (encodedBearerData != null) {
                Rlog.d(LOG_TAG, "MO raw BearerData = '" + HexDump.toHexString(encodedBearerData) + "'");
            }
        }
        if (encodedBearerData == null) {
            return null;
        }
        int digitNum = 0;
        try {
            int digitCount;
            ByteArrayOutputStream baos = new ByteArrayOutputStream(363);
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeInt(4098);
            dos.write(0);
            dos.writeInt(0);
            dos.writeInt(destAddr.digitMode);
            dos.writeInt(destAddr.numberMode);
            dos.writeInt(destAddr.ton);
            dos.writeInt(destAddr.numberPlan);
            dos.write(destAddr.numberOfDigits);
            dos.write(destAddr.origBytes, 0, destAddr.origBytes.length);
            if (destAddr.origBytes.length < 36) {
                digitNum = 36 - destAddr.origBytes.length;
            } else {
                Rlog.e(LOG_TAG, "destAddr too long");
            }
            for (digitCount = 0; digitCount < digitNum; digitCount++) {
                dos.write(HwSubscriptionManager.SUB_INIT_STATE);
            }
            dos.writeInt(0);
            dos.write(0);
            dos.write(0);
            for (digitCount = 0; digitCount < 36; digitCount++) {
                dos.write(HwSubscriptionManager.SUB_INIT_STATE);
            }
            dos.writeInt(encodedBearerData.length);
            dos.write(encodedBearerData, 0, encodedBearerData.length);
            if (encodedBearerData.length < 255) {
                digitNum = 255 - encodedBearerData.length;
            } else {
                digitNum = 0;
                Rlog.e(LOG_TAG, "encodedBearerData too long");
            }
            for (digitCount = 0; digitCount < digitNum; digitCount++) {
                dos.write(HwSubscriptionManager.SUB_INIT_STATE);
            }
            dos.close();
            SubmitPdu pdu = new SubmitPdu();
            pdu.encodedMessage = baos.toByteArray();
            for (byte content : pdu.encodedMessage) {
                Rlog.e(LOG_TAG, "pdu is" + content);
            }
            pdu.encodedScAddress = null;
            return pdu;
        } catch (IOException ex) {
            Rlog.e(LOG_TAG, "creating SubmitPdu failed: " + ex);
            return null;
        }
    }

    public static void parseRUIMPdu(SmsMessage msg, byte[] pdu) {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(pdu));
        SmsEnvelope env = new SmsEnvelope();
        CdmaSmsAddress addr = new CdmaSmsAddress();
        boolean readSmsEnvOK = false;
        byte[] address = new byte[0];
        byte[] subAddress = new byte[0];
        byte[] causeCodes = new byte[0];
        addr.origBytes = new byte[0];
        try {
            env.messageType = dis.readByte();
            while (!readSmsEnvOK) {
                int message_len = dis.readByte() & HwSubscriptionManager.SUB_INIT_STATE;
                switch (dis.readByte()) {
                    case HwVSimUtilsInner.UE_OPERATION_MODE_VOICE_CENTRIC /*0*/:
                        env.teleService = dis.readShort();
                        break;
                    case HwVSimUtilsInner.UE_OPERATION_MODE_DATA_CENTRIC /*1*/:
                        env.serviceCategory = dis.readShort();
                        break;
                    case HwVSimUtilsInner.STATE_EB /*2*/:
                    case HwVSimEventReport.VSIM_PROCESS_TYPE_ED /*4*/:
                        address = new byte[message_len];
                        if (dis.read(address, 0, message_len) == message_len) {
                            break;
                        }
                        Log.e(LOG_TAG, "DataInputStream read error");
                        break;
                    case HwVSimSlotSwitchController.CARD_TYPE_DUAL_MODE /*3*/:
                    case HwVSimEventReport.VSIM_CAUSE_TYPE_SWITCH_SLOT /*5*/:
                        subAddress = new byte[message_len];
                        if (dis.read(subAddress, 0, message_len) == message_len) {
                            break;
                        }
                        Log.e(LOG_TAG, "DataInputStream read error");
                        break;
                    case HwVSimEventReport.VSIM_CAUSE_TYPE_SET_TEE_DATA /*6*/:
                        env.bearerReply = dis.readByte();
                        break;
                    case HwVSimEventReport.VSIM_CAUSE_TYPE_CARD_POWER_ON /*7*/:
                        causeCodes = new byte[message_len];
                        if (dis.read(causeCodes, 0, message_len) == message_len) {
                            break;
                        }
                        Log.e(LOG_TAG, "DataInputStream read error");
                        break;
                    case HwVSimEventReport.VSIM_CAUSE_TYPE_ACTIVE_MODEM_MODE /*8*/:
                        env.bearerData = new byte[message_len];
                        if (dis.read(env.bearerData, 0, message_len) != message_len) {
                            Log.e(LOG_TAG, "DataInputStream read error");
                        }
                        readSmsEnvOK = true;
                        break;
                    default:
                        break;
                }
            }
            dis.close();
        } catch (Exception ex) {
            Log.e(LOG_TAG, "createFromPdu: conversion from byte array to object failed: " + ex);
        }
        parseAddress(address, addr);
        if (PLUS_TRANFER_IN_AP) {
            String number = HwCustPlusAndIddNddConvertUtils.replaceIddNddWithPlusForSms(new String(addr.origBytes, Charset.defaultCharset()));
            if (addr.ton == 1 && number != null && number.length() > 0 && number.charAt(0) != '+') {
                number = "+" + number;
                Rlog.d(LOG_TAG, "HwSmsMessage->parseRUIMPdu ton == SmsAddress.TON_INTERNATIONAL");
            }
            if (number != null) {
                addr.origBytes = number.getBytes(Charset.defaultCharset());
            }
            addr.numberOfDigits = addr.origBytes.length;
        }
        parseCauseCodes(causeCodes, env);
        SmsMessageBaseUtils.setOriginatingAddress(msg, addr);
        env.origAddress = addr;
        smsMessageUtils.setEnvelope(msg, env);
        SmsMessageBaseUtils.setPdu(msg, pdu);
        msg.parseSms();
        if (Log.isLoggable(LOGGABLE_TAG, 2)) {
            HexDump.toHexString(subAddress);
        }
    }

    protected static void parseAddress(byte[] address, CdmaSmsAddress addr) {
        if (address != null) {
            try {
                BitwiseInputStream inStream = new BitwiseInputStream(address);
                if (inStream.available() > 0) {
                    int digitsSize;
                    int val;
                    addr.digitMode = inStream.read(1);
                    addr.numberMode = inStream.read(1);
                    int readCount = 2;
                    if (1 == addr.digitMode) {
                        digitsSize = 8;
                        addr.ton = inStream.read(3);
                        readCount = 5;
                        if (addr.numberMode == 0) {
                            addr.numberPlan = inStream.read(4);
                            readCount = 5 + 4;
                        }
                    } else {
                        digitsSize = 4;
                    }
                    addr.numberOfDigits = inStream.read(8);
                    readCount += 8;
                    int numOfDigits = addr.numberOfDigits;
                    int length = numOfDigits;
                    int i = 0;
                    addr.origBytes = new byte[numOfDigits];
                    while (!false) {
                        if (numOfDigits <= 0) {
                            break;
                        }
                        if (4 == digitsSize) {
                            val = inStream.read(4);
                        } else {
                            val = inStream.read(8);
                        }
                        addr.origBytes[i] = (byte) val;
                        numOfDigits--;
                        i++;
                    }
                    if (addr.digitMode == 0) {
                        StringBuffer strBuf = new StringBuffer(length);
                        for (int j = 0; j < length; j++) {
                            val = addr.origBytes[j] & 15;
                            if (val >= 1 && val <= 9) {
                                strBuf.append(Integer.toString(val, 10));
                            } else if (10 == val) {
                                strBuf.append('0');
                            } else if (11 == val) {
                                strBuf.append('*');
                            } else if (12 == val) {
                                strBuf.append('#');
                            }
                            addr.origBytes[j] = (byte) strBuf.charAt(j);
                        }
                    }
                }
            } catch (AccessException ex) {
                Log.e(LOG_TAG, "parseAddress decode failed: " + ex);
            }
        }
    }

    protected static void parseCauseCodes(byte[] causeCodes, SmsEnvelope env) {
        if (causeCodes != null) {
            try {
                BitwiseInputStream inStream = new BitwiseInputStream(causeCodes);
                if (inStream.available() > 0) {
                    env.replySeqNo = (byte) inStream.read(6);
                    env.errorClass = (byte) inStream.read(2);
                    if (env.errorClass != null) {
                        env.causeCode = (byte) inStream.read(8);
                    }
                }
            } catch (AccessException ex) {
                Log.e(LOG_TAG, "parseCauseCodes decode failed: " + ex);
            } catch (Exception ex2) {
                Log.e(LOG_TAG, "parseCauseCodes: conversion from byte array to object failed: " + ex2);
            }
        }
    }

    public static void doubleSmsStatusCheck(SmsMessage msg) {
        Log.d(LOG_TAG, "DELIVERY_ACK message old status = " + smsMessageUtils.getStatus(msg));
        if ("TELECOM".equals(SystemProperties.get("ro.config.operators")) && smsMessageUtils.getStatus(msg) != 0) {
            Log.d(LOG_TAG, "network is China Telecom,and status = " + smsMessageUtils.getStatus(msg));
            String userDataStr = "";
            String successStr = "";
            try {
                byte[] userData = SmsMessageBaseUtils.getUserData(msg);
                if (userData != null) {
                    userDataStr = new String(userData, "utf-16be");
                }
                successStr = new String(DELIVERY_ACK_SUCCESS, "utf-16be");
            } catch (Exception e) {
                Log.e(LOG_TAG, "not support charset utf-16be");
            }
            Log.d(LOG_TAG, "userDataStr = " + userDataStr + ";successStr = " + successStr);
            Log.d(LOG_TAG, "mMessageBody");
            if (userDataStr.contains(successStr) || (SmsMessageBaseUtils.getMessageBody(msg) != null && SmsMessageBaseUtils.getMessageBody(msg).toLowerCase().contains(DELIVERY_ACK_SUCCESS_EN))) {
                Log.d(LOG_TAG, "mUserData contains DELIVERY_ACK_SUCCESS, so should set status = 0");
                smsMessageUtils.setStatus(msg, 0);
            }
        } else if (msg.getStatus() != 0) {
            int status = msg.getStatus() >> 16;
            int errClass = status >> 8;
            int errCause = status & HwSubscriptionManager.SUB_INIT_STATE;
            Log.d(LOG_TAG, "CDMA sms status = " + status + ", errClass = " + errClass + ", " + " errCause = " + errCause);
            if (errClass == 0 && 3 != errCause) {
                smsMessageUtils.setStatus(msg, 0);
            }
        }
        Log.d(LOG_TAG, "DELIVERY_ACK message new status = " + smsMessageUtils.getStatus(msg));
    }

    public static int getCdmaSub() {
        if (HwTelephonyManagerInner.getDefault().isFullNetworkSupported()) {
            int i = 0;
            while (i < TelephonyManager.getDefault().getPhoneCount()) {
                int cdmaSubId = SubscriptionManager.getSubId(i)[0];
                if (2 == TelephonyManager.getDefault().getCurrentPhoneType(cdmaSubId) && 5 == TelephonyManager.getDefault().getSimState(i)) {
                    Rlog.d(LOG_TAG, "getCdmaSubId find cdma phone subId = " + cdmaSubId);
                    return cdmaSubId;
                }
                i++;
            }
        }
        Rlog.d(LOG_TAG, "use default cdma phone sub 0");
        return 0;
    }
}
