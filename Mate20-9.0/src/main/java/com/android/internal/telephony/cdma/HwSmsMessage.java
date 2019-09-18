package com.android.internal.telephony.cdma;

import android.os.SystemProperties;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.SmsMessageBaseUtils;
import com.android.internal.telephony.cdma.SmsMessage;
import com.android.internal.telephony.cdma.sms.BearerData;
import com.android.internal.telephony.cdma.sms.CdmaSmsAddress;
import com.android.internal.telephony.cdma.sms.SmsEnvelope;
import com.android.internal.telephony.cdma.sms.UserData;
import com.android.internal.util.BitwiseInputStream;
import com.android.internal.util.HexDump;
import com.huawei.utils.reflect.EasyInvokeFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class HwSmsMessage {
    private static final byte BEARER_DATA = 8;
    private static final byte BEARER_REPLY_OPTION = 6;
    private static final byte CAUSE_CODES = 7;
    private static final byte[] DELIVERY_ACK_SUCCESS = {98, 16, 82, -97};
    private static final String DELIVERY_ACK_SUCCESS_EN = "succ";
    private static final byte DESTINATION_ADDRESS = 4;
    private static final byte DESTINATION_SUB_ADDRESS = 5;
    private static final String LOGGABLE_TAG = "CDMA:SMS";
    static final String LOG_TAG = "SmsMessage";
    private static final byte ORIGINATING_ADDRESS = 2;
    private static final byte ORIGINATING_SUB_ADDRESS = 3;
    private static boolean PLUS_TRANFER_IN_AP = (!HwModemCapability.isCapabilitySupport(2));
    private static final byte SERVICE_CATEGORY = 1;
    private static final byte TELESERVICE_IDENTIFIER = 0;
    private static final boolean VDBG = false;
    private static SmsMessageUtils smsMessageUtils = ((SmsMessageUtils) EasyInvokeFactory.getInvokeUtils(SmsMessageUtils.class));

    public static SmsMessage.SubmitPdu getSubmitDeliverPdu(boolean isSubmitPdu, String mcTimeStamp, String destAddr, String message, SmsHeader smsHeader) {
        if (message == null || destAddr == null) {
            Log.e("CDMA/SmsMessage", "empty message or destAddr");
            return null;
        }
        UserData uData = new UserData();
        uData.payloadStr = message;
        uData.userDataHeader = smsHeader;
        return privateGetSubmitDeliverPdu(isSubmitPdu, destAddr, mcTimeStamp, uData);
    }

    private static SmsMessage.SubmitPdu privateGetSubmitDeliverPdu(boolean isSubmitPdu, String origAddrStr, String mcTimestamp, UserData userData) {
        CdmaSmsAddress destAddr;
        if (PLUS_TRANFER_IN_AP) {
            destAddr = SmsMessage.parseAddrForSMSMO(origAddrStr);
            if (destAddr == null) {
                Rlog.e(LOG_TAG, "privateGetSubmitDeliverPdu, CdmaSmsAddress parse error.");
                return null;
            }
        } else {
            destAddr = CdmaSmsAddress.parse(PhoneNumberUtils.cdmaCheckAndProcessPlusCodeForSms(origAddrStr));
        }
        CdmaSmsAddress destAddr2 = destAddr;
        if (destAddr2 == null) {
            return null;
        }
        BearerData bearerData = new BearerData();
        bearerData.messageId = SmsMessage.getNextMessageId();
        int i = 0;
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
            BearerData.TimeStamp msgTimeStamp = new BearerData.TimeStamp();
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
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(363);
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeInt(4098);
            dos.write(0);
            dos.writeInt(0);
            dos.writeInt(destAddr2.digitMode);
            dos.writeInt(destAddr2.numberMode);
            dos.writeInt(destAddr2.ton);
            dos.writeInt(destAddr2.numberPlan);
            dos.write(destAddr2.numberOfDigits);
            dos.write(destAddr2.origBytes, 0, destAddr2.origBytes.length);
            dos.writeInt(0);
            dos.write(0);
            dos.write(0);
            dos.writeInt(encodedBearerData.length);
            dos.write(encodedBearerData, 0, encodedBearerData.length);
            dos.close();
            SmsMessage.SubmitPdu pdu = new SmsMessage.SubmitPdu();
            pdu.encodedMessage = baos.toByteArray();
            byte[] bArr = pdu.encodedMessage;
            int length = bArr.length;
            while (i < length) {
                byte content = bArr[i];
                CdmaSmsAddress destAddr3 = destAddr2;
                try {
                    CdmaSmsAddress destAddr4 = new StringBuilder();
                    BearerData bearerData2 = bearerData;
                    try {
                        destAddr4.append("pdu is");
                        destAddr4.append(content);
                        Rlog.e(LOG_TAG, destAddr4.toString());
                        i++;
                        destAddr2 = destAddr3;
                        bearerData = bearerData2;
                    } catch (IOException e) {
                        ex = e;
                        Rlog.e(LOG_TAG, "creating SubmitPdu failed: " + ex);
                        return null;
                    }
                } catch (IOException e2) {
                    ex = e2;
                    BearerData bearerData3 = bearerData;
                    Rlog.e(LOG_TAG, "creating SubmitPdu failed: " + ex);
                    return null;
                }
            }
            BearerData bearerData4 = bearerData;
            pdu.encodedScAddress = null;
            return pdu;
        } catch (IOException e3) {
            ex = e3;
            CdmaSmsAddress cdmaSmsAddress = destAddr2;
            BearerData bearerData5 = bearerData;
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
                byte message_paramID = dis.readByte();
                int message_len = 255 & dis.readByte();
                switch (message_paramID) {
                    case 0:
                        env.teleService = dis.readShort();
                        break;
                    case 1:
                        env.serviceCategory = dis.readShort();
                        break;
                    case 2:
                    case 4:
                        address = new byte[message_len];
                        if (dis.read(address, 0, message_len) == message_len) {
                            break;
                        } else {
                            Log.e(LOG_TAG, "DataInputStream read error");
                            break;
                        }
                    case 3:
                    case 5:
                        subAddress = new byte[message_len];
                        if (dis.read(subAddress, 0, message_len) == message_len) {
                            break;
                        } else {
                            Log.e(LOG_TAG, "DataInputStream read error");
                            break;
                        }
                    case 6:
                        env.bearerReply = dis.readByte();
                        break;
                    case 7:
                        causeCodes = new byte[message_len];
                        if (dis.read(causeCodes, 0, message_len) == message_len) {
                            break;
                        } else {
                            Log.e(LOG_TAG, "DataInputStream read error");
                            break;
                        }
                    case 8:
                        env.bearerData = new byte[message_len];
                        if (dis.read(env.bearerData, 0, message_len) != message_len) {
                            Log.e(LOG_TAG, "DataInputStream read error");
                        }
                        readSmsEnvOK = true;
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
        int digitsSize;
        int val;
        if (address != null) {
            try {
                BitwiseInputStream inStream = new BitwiseInputStream(address);
                if (inStream.available() > 0) {
                    addr.digitMode = inStream.read(1);
                    addr.numberMode = inStream.read(1);
                    int readCount = 2;
                    if (1 == addr.digitMode) {
                        digitsSize = 8;
                        addr.ton = inStream.read(3);
                        readCount = 2 + 3;
                        if (addr.numberMode == 0) {
                            addr.numberPlan = inStream.read(4);
                            readCount += 4;
                        }
                    } else {
                        digitsSize = 4;
                    }
                    addr.numberOfDigits = inStream.read(8);
                    int readCount2 = readCount + 8;
                    int numOfDigits = addr.numberOfDigits;
                    int length = numOfDigits;
                    int i = 0;
                    addr.origBytes = new byte[numOfDigits];
                    while (true) {
                        if (0 != 0) {
                            break;
                        } else if (numOfDigits <= 0) {
                            break;
                        } else {
                            if (4 == digitsSize) {
                                val = inStream.read(4);
                            } else {
                                val = inStream.read(8);
                            }
                            addr.origBytes[i] = (byte) val;
                            numOfDigits--;
                            i++;
                        }
                    }
                    if (addr.digitMode == 0) {
                        convertionDTMFDigits(addr, length);
                    }
                }
            } catch (BitwiseInputStream.AccessException ex) {
                Log.e(LOG_TAG, "parseAddress decode failed: " + ex);
            }
        }
    }

    protected static void convertionDTMFDigits(CdmaSmsAddress addr, int length) {
        StringBuffer strBuf = new StringBuffer(length);
        for (int j = 0; j < length; j++) {
            int val = 15 & addr.origBytes[j];
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

    protected static void parseCauseCodes(byte[] causeCodes, SmsEnvelope env) {
        if (causeCodes != null) {
            try {
                BitwiseInputStream inStream = new BitwiseInputStream(causeCodes);
                if (inStream.available() > 0) {
                    env.replySeqNo = (byte) inStream.read(6);
                    env.errorClass = (byte) inStream.read(2);
                    if (env.errorClass != 0) {
                        env.causeCode = (byte) inStream.read(8);
                    }
                }
            } catch (BitwiseInputStream.AccessException ex) {
                Log.e(LOG_TAG, "parseCauseCodes decode failed: " + ex);
            } catch (Exception ex2) {
                Log.e(LOG_TAG, "parseCauseCodes: conversion from byte array to object failed: " + ex2);
            }
        }
    }

    public static void doubleSmsStatusCheck(SmsMessage msg) {
        if (msg != null) {
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
                int errCause = status & 255;
                Log.d(LOG_TAG, "CDMA sms status = " + status + ", errClass = " + errClass + ",  errCause = " + errCause);
                if (errClass == 0 && 3 != errCause) {
                    smsMessageUtils.setStatus(msg, 0);
                }
            }
            Log.d(LOG_TAG, "DELIVERY_ACK message new status = " + smsMessageUtils.getStatus(msg));
        }
    }

    public static int getCdmaSub() {
        if (HwTelephonyManagerInner.getDefault().isFullNetworkSupported()) {
            int phoneCount = TelephonyManager.getDefault().getPhoneCount();
            for (int i = 0; i < phoneCount; i++) {
                int cdmaSubId = SubscriptionManager.getSubId(i)[0];
                if (2 == TelephonyManager.getDefault().getCurrentPhoneType(cdmaSubId) && 5 == TelephonyManager.getDefault().getSimState(i)) {
                    Rlog.d(LOG_TAG, "getCdmaSubId find cdma phone subId = " + cdmaSubId);
                    return cdmaSubId;
                }
            }
        }
        Rlog.d(LOG_TAG, "use default cdma phone sub 0");
        return 0;
    }
}
