package com.android.internal.telephony.cdma;

import android.telephony.HwTelephonyManagerInner;
import android.util.Log;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.SmsMessageBaseUtils;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.PhoneNumberUtilsEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.hwparttelephony.BuildConfig;
import com.huawei.internal.telephony.SmsHeaderEx;
import com.huawei.internal.telephony.cdma.SmsMessageEx;
import com.huawei.internal.telephony.cdma.sms.BearerDataEx;
import com.huawei.internal.telephony.cdma.sms.CdmaSmsAddressEx;
import com.huawei.internal.telephony.cdma.sms.SmsEnvelopeEx;
import com.huawei.internal.telephony.cdma.sms.UserDataEx;
import com.huawei.internal.util.BitwiseInputStreamEx;
import com.huawei.internal.util.HexDumpEx;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

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

    public static SmsMessageEx.SubmitPduEx getSubmitDeliverPdu(boolean isSubmitPdu, String mcTimeStamp, String destAddr, String message, SmsHeaderEx smsHeader) {
        if (message == null || destAddr == null) {
            Log.e("CDMA/SmsMessage", "empty message or destAddr");
            return null;
        }
        UserDataEx uData = new UserDataEx();
        uData.setPayloadStr(message);
        uData.setUserDataHeader(smsHeader);
        return privateGetSubmitDeliverPdu(isSubmitPdu, destAddr, mcTimeStamp, uData);
    }

    /* JADX WARNING: Removed duplicated region for block: B:49:0x0175 A[SYNTHETIC, Splitter:B:49:0x0175] */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x017d A[Catch:{ IOException -> 0x0179 }] */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x018c A[SYNTHETIC, Splitter:B:59:0x018c] */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0194 A[Catch:{ IOException -> 0x0190 }] */
    /* JADX WARNING: Removed duplicated region for block: B:69:? A[RETURN, SYNTHETIC] */
    private static SmsMessageEx.SubmitPduEx privateGetSubmitDeliverPdu(boolean isSubmitPdu, String origAddrStr, String mcTimestamp, UserDataEx userData) {
        CdmaSmsAddressEx destAddr;
        CdmaSmsAddressEx destAddr2;
        if (PLUS_TRANFER_IN_AP) {
            CdmaSmsAddressEx destAddr3 = SmsMessageEx.parseAddrForSMSMO(origAddrStr);
            if (destAddr3 == null) {
                RlogEx.e(LOG_TAG, "privateGetSubmitDeliverPdu, CdmaSmsAddress parse error.");
                return null;
            }
            destAddr = destAddr3;
        } else {
            destAddr = CdmaSmsAddressEx.parse(PhoneNumberUtilsEx.cdmaCheckAndProcessPlusCodeForSms(origAddrStr));
        }
        if (destAddr == null) {
            return null;
        }
        BearerDataEx bearerData = new BearerDataEx();
        bearerData.setMessageId(SmsMessageEx.getNextMessageId());
        bearerData.setUserAckReq(false);
        bearerData.setReadAckReq(false);
        bearerData.setReportReq(false);
        bearerData.setDeliveryAckReq(false);
        if (isSubmitPdu) {
            bearerData.setMessageType(2);
            bearerData.setPriorityIndicatorSet(true);
            bearerData.setPriority(0);
        } else {
            bearerData.setMessageType(1);
            BearerDataEx.TimeStampEx msgTimeStamp = new BearerDataEx.TimeStampEx();
            try {
                msgTimeStamp.set(Long.parseLong(mcTimestamp));
            } catch (NumberFormatException e) {
                RlogEx.e(LOG_TAG, "parse mcTimestamp error");
                msgTimeStamp.set(0);
            }
            bearerData.setMsgCenterTimeStamp(msgTimeStamp);
        }
        bearerData.setUserData(userData);
        byte[] encodedBearerData = BearerDataEx.encode(bearerData);
        if (RlogEx.isLoggable(LOGGABLE_TAG, 2)) {
            RlogEx.i(LOG_TAG, "MO (encoded) BearerData = " + bearerData);
            if (encodedBearerData != null) {
                RlogEx.i(LOG_TAG, "MO raw BearerData = '" + HexDumpEx.toHexString(encodedBearerData) + "'");
            }
        }
        if (encodedBearerData == null) {
            return null;
        }
        ByteArrayOutputStream baos = null;
        DataOutputStream dos = null;
        try {
            baos = new ByteArrayOutputStream(363);
            dos = new DataOutputStream(baos);
            dos.writeInt(4098);
            dos.write(0);
            dos.writeInt(0);
            dos.writeInt(destAddr.getDigitMode());
            dos.writeInt(destAddr.getNumberMode());
            dos.writeInt(destAddr.getTon());
            dos.writeInt(destAddr.getNumberPlan());
            dos.write(destAddr.getNumberOfDigits());
            dos.write(destAddr.getOrigBytes(), 0, destAddr.getOrigBytes().length);
            dos.writeInt(0);
            dos.write(0);
            dos.write(0);
            dos.writeInt(encodedBearerData.length);
            dos.write(encodedBearerData, 0, encodedBearerData.length);
            dos.close();
            SmsMessageEx.SubmitPduEx pdu = new SmsMessageEx.SubmitPduEx();
            pdu.setEncodedMessage(baos.toByteArray());
            byte[] encodedMessage = pdu.getEncodedMessage();
            int length = encodedMessage.length;
            int i = 0;
            while (i < length) {
                byte content = encodedMessage[i];
                StringBuilder sb = new StringBuilder();
                try {
                    sb.append("pdu is");
                    sb.append((int) content);
                    RlogEx.e(LOG_TAG, sb.toString());
                    i++;
                    encodedMessage = encodedMessage;
                    destAddr = destAddr;
                } catch (IOException e2) {
                    try {
                        RlogEx.e(LOG_TAG, "creating SubmitPdu failed: ");
                        if (baos != null) {
                            try {
                                baos.close();
                            } catch (IOException e3) {
                                RlogEx.e(LOG_TAG, "close stream error.");
                                return null;
                            }
                        }
                        if (dos != null) {
                            return null;
                        }
                        dos.close();
                        return null;
                    } catch (Throwable th) {
                        destAddr2 = th;
                        if (baos != null) {
                            try {
                                baos.close();
                            } catch (IOException e4) {
                                RlogEx.e(LOG_TAG, "close stream error.");
                                throw destAddr2;
                            }
                        }
                        if (dos != null) {
                            dos.close();
                        }
                        throw destAddr2;
                    }
                }
            }
            pdu.setEncodedScAddress((byte[]) null);
            try {
                baos.close();
                dos.close();
            } catch (IOException e5) {
                RlogEx.e(LOG_TAG, "close stream error.");
            }
            return pdu;
        } catch (IOException e6) {
            RlogEx.e(LOG_TAG, "creating SubmitPdu failed: ");
            if (baos != null) {
            }
            if (dos != null) {
            }
        } catch (Throwable th2) {
            destAddr2 = th2;
            if (baos != null) {
            }
            if (dos != null) {
            }
            throw destAddr2;
        }
    }

    public static void parseRUIMPdu(SmsMessageEx msg, byte[] pdu) {
        if (msg != null && pdu != null) {
            ByteArrayInputStream bais = new ByteArrayInputStream(pdu);
            DataInputStream dis = new DataInputStream(bais);
            SmsEnvelopeEx env = new SmsEnvelopeEx();
            CdmaSmsAddressEx addr = new CdmaSmsAddressEx();
            boolean readSmsEnvOK = false;
            byte[] address = new byte[0];
            byte[] subAddress = new byte[0];
            byte[] causeCodes = new byte[0];
            addr.setOrigBytes(new byte[0]);
            try {
                env.setMessageType(dis.readByte());
                while (!readSmsEnvOK) {
                    byte message_paramID = dis.readByte();
                    int message_len = dis.readByte() & 255;
                    switch (message_paramID) {
                        case 0:
                            env.setTeleService(dis.readShort());
                            break;
                        case 1:
                            env.setServiceCategory(dis.readShort());
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
                            env.setBearerReply(dis.readByte());
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
                            env.setBearerData(new byte[message_len]);
                            if (dis.read(env.getBearerData(), 0, message_len) != message_len) {
                                Log.e(LOG_TAG, "DataInputStream read error");
                            }
                            readSmsEnvOK = true;
                            break;
                    }
                }
                dis.close();
                try {
                    bais.close();
                    dis.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "close stream error.");
                }
            } catch (Exception e2) {
                Log.e(LOG_TAG, "createFromPdu: conversion from byte array to object failed");
                bais.close();
                dis.close();
            } catch (Throwable e3) {
                try {
                    bais.close();
                    dis.close();
                } catch (IOException e4) {
                    Log.e(LOG_TAG, "close stream error.");
                }
                throw e3;
            }
            parseAddress(address, addr);
            if (PLUS_TRANFER_IN_AP) {
                String number = HwCustPlusAndIddNddConvertUtils.replaceIddNddWithPlusForSms(new String(addr.getOrigBytes(), Charset.defaultCharset()));
                if (addr.getTon() == 1 && number != null && number.length() > 0 && number.charAt(0) != '+') {
                    number = "+" + number;
                    RlogEx.i(LOG_TAG, "HwSmsMessage->parseRUIMPdu ton == SmsAddress.TON_INTERNATIONAL");
                }
                if (number != null) {
                    addr.setOrigBytes(number.getBytes(Charset.defaultCharset()));
                }
                addr.setNumberOfDigits(addr.getOrigBytes().length);
            }
            parseCauseCodes(causeCodes, env);
            SmsMessageBaseUtils.setOriginatingAddress(msg, addr);
            env.setOrigAddress(addr);
            msg.setSmsEnvelope(env);
            SmsMessageBaseUtils.setPdu(msg, pdu);
            msg.parseSms();
            if (Log.isLoggable(LOGGABLE_TAG, 2)) {
                HexDumpEx.toHexString(subAddress);
            }
        }
    }

    protected static void parseAddress(byte[] address, CdmaSmsAddressEx addr) {
        int digitsSize;
        int val;
        if (address != null && addr != null) {
            try {
                BitwiseInputStreamEx inStream = new BitwiseInputStreamEx(address);
                if (inStream.available() > 0) {
                    addr.setDigitMode(inStream.read(1));
                    addr.setNumberMode(inStream.read(1));
                    int readCount = 2;
                    if (1 == addr.getDigitMode()) {
                        digitsSize = 8;
                        addr.setTon(inStream.read(3));
                        readCount = 2 + 3;
                        if (addr.getNumberMode() == 0) {
                            addr.setNumberPlan(inStream.read(4));
                            readCount += 4;
                        }
                    } else {
                        digitsSize = 4;
                    }
                    addr.setNumberOfDigits(inStream.read(8));
                    int readCount2 = readCount + 8;
                    int numOfDigits = addr.getNumberOfDigits();
                    int i = 0;
                    addr.setOrigBytes(new byte[numOfDigits]);
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
                            addr.setOrigBytes((byte) val, i);
                            numOfDigits--;
                            i++;
                        }
                    }
                    if (addr.getDigitMode() == 0) {
                        convertionDTMFDigits(addr, numOfDigits);
                    }
                }
            } catch (BitwiseInputStreamEx.AccessExceptionEx ex) {
                Log.e(LOG_TAG, "parseAddress decode failed: " + ex);
            }
        }
    }

    protected static void convertionDTMFDigits(CdmaSmsAddressEx addr, int length) {
        if (addr != null) {
            StringBuffer strBuf = new StringBuffer(length);
            for (int j = 0; j < length; j++) {
                int val = addr.getOrigBytes()[j] & 15;
                if (val >= 1 && val <= 9) {
                    strBuf.append(Integer.toString(val, 10));
                } else if (10 == val) {
                    strBuf.append('0');
                } else if (11 == val) {
                    strBuf.append('*');
                } else if (12 == val) {
                    strBuf.append('#');
                }
                addr.setOrigBytes((byte) strBuf.charAt(j), j);
            }
        }
    }

    protected static void parseCauseCodes(byte[] causeCodes, SmsEnvelopeEx env) {
        if (causeCodes != null && env != null) {
            try {
                BitwiseInputStreamEx inStream = new BitwiseInputStreamEx(causeCodes);
                if (inStream.available() > 0) {
                    env.setReplySeqNo((byte) inStream.read(6));
                    env.setErrorClass((byte) inStream.read(2));
                    if (env.getErrorClass() != 0) {
                        env.setCauseCode((byte) inStream.read(8));
                    }
                }
            } catch (BitwiseInputStreamEx.AccessExceptionEx e) {
                Log.e(LOG_TAG, "parseCauseCodes decode failed.");
            } catch (Exception e2) {
                Log.e(LOG_TAG, "parseCauseCodes: conversion from byte array to object failed");
            }
        }
    }

    public static void doubleSmsStatusCheck(SmsMessageEx msg) {
        if (msg != null) {
            Log.d(LOG_TAG, "DELIVERY_ACK message old status = " + msg.getStatus());
            if ("TELECOM".equals(SystemPropertiesEx.get("ro.config.operators")) && msg.getStatus() != 0) {
                Log.d(LOG_TAG, "network is China Telecom,and status = " + msg.getStatus());
                String userDataStr = BuildConfig.FLAVOR;
                String successStr = BuildConfig.FLAVOR;
                try {
                    byte[] userData = SmsMessageBaseUtils.getUserData(msg);
                    if (userData != null) {
                        userDataStr = new String(userData, StandardCharsets.UTF_16BE);
                    }
                    successStr = new String(DELIVERY_ACK_SUCCESS, StandardCharsets.UTF_16BE);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "not support charset utf-16be");
                }
                Log.d(LOG_TAG, "userDataStr = " + userDataStr + ";successStr = " + successStr);
                Log.d(LOG_TAG, "mMessageBody");
                if (userDataStr.contains(successStr) || (SmsMessageBaseUtils.getMessageBody(msg) != null && SmsMessageBaseUtils.getMessageBody(msg).toLowerCase().contains(DELIVERY_ACK_SUCCESS_EN))) {
                    Log.d(LOG_TAG, "mUserData contains DELIVERY_ACK_SUCCESS, so should set status = 0");
                    msg.setStatus(0);
                }
            } else if (msg.getStatus() != 0) {
                int status = msg.getStatus() >> 16;
                int errClass = status >> 8;
                int errCause = status & 255;
                Log.d(LOG_TAG, "CDMA sms status = " + status + ", errClass = " + errClass + ",  errCause = " + errCause);
                if (errClass == 0 && 3 != errCause) {
                    msg.setStatus(0);
                }
            }
            Log.d(LOG_TAG, "DELIVERY_ACK message new status = " + msg.getStatus());
        }
    }

    public static int getCdmaSub() {
        if (HwTelephonyManagerInner.getDefault().isFullNetworkSupported()) {
            int phoneCount = TelephonyManagerEx.getDefault().getPhoneCount();
            for (int i = 0; i < phoneCount; i++) {
                if (SubscriptionManagerEx.getSubId(i) != null) {
                    int cdmaSubId = SubscriptionManagerEx.getSubId(i)[0];
                    if (TelephonyManagerEx.getCurrentPhoneType(cdmaSubId) == 2 && TelephonyManagerEx.getDefault().getSimState(i) == 5) {
                        RlogEx.i(LOG_TAG, "getCdmaSubId find cdma phone subId = " + cdmaSubId);
                        return cdmaSubId;
                    }
                }
            }
        }
        RlogEx.i(LOG_TAG, "use default cdma phone sub 0");
        return 0;
    }
}
