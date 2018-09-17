package com.android.internal.telephony.gsm;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.cat.ComprehensionTlvTag;
import com.android.internal.telephony.uicc.IccIoResult;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.UsimServiceTable;
import com.android.internal.telephony.uicc.UsimServiceTable.UsimService;

public class UsimDataDownloadHandler extends Handler {
    private static final int BER_SMS_PP_DOWNLOAD_TAG = 209;
    private static final int DEV_ID_NETWORK = 131;
    private static final int DEV_ID_UICC = 129;
    private static final int EVENT_SEND_ENVELOPE_RESPONSE = 2;
    private static final int EVENT_START_DATA_DOWNLOAD = 1;
    private static final int EVENT_WRITE_SMS_COMPLETE = 3;
    private static final String TAG = "UsimDataDownloadHandler";
    private final CommandsInterface mCi;

    public UsimDataDownloadHandler(CommandsInterface commandsInterface) {
        this.mCi = commandsInterface;
    }

    int handleUsimDataDownload(UsimServiceTable ust, SmsMessage smsMessage) {
        if (ust == null || !ust.isAvailable(UsimService.DATA_DL_VIA_SMS_PP)) {
            Rlog.d(TAG, "DATA_DL_VIA_SMS_PP service not available, storing message to UICC.");
            this.mCi.writeSmsToSim(3, IccUtils.bytesToHexString(PhoneNumberUtils.networkPortionToCalledPartyBCDWithLength(smsMessage.getServiceCenterAddress())), IccUtils.bytesToHexString(smsMessage.getPdu()), obtainMessage(3));
            return -1;
        }
        Rlog.d(TAG, "Received SMS-PP data download, sending to UICC.");
        return startDataDownload(smsMessage);
    }

    public int startDataDownload(SmsMessage smsMessage) {
        if (sendMessage(obtainMessage(1, smsMessage))) {
            return -1;
        }
        Rlog.e(TAG, "startDataDownload failed to send message to start data download.");
        return 2;
    }

    private void handleDataDownload(SmsMessage smsMessage) {
        int index;
        int dcs = smsMessage.getDataCodingScheme();
        int pid = smsMessage.getProtocolIdentifier();
        byte[] pdu = smsMessage.getPdu();
        int scAddressLength = pdu[0] & 255;
        int tpduIndex = scAddressLength + 1;
        int tpduLength = pdu.length - tpduIndex;
        int bodyLength = getEnvelopeBodyLength(scAddressLength, tpduLength);
        byte[] envelope = new byte[((bodyLength + 1) + (bodyLength > 127 ? 2 : 1))];
        int i = 1;
        envelope[0] = (byte) -47;
        if (bodyLength > 127) {
            index = 1 + 1;
            envelope[1] = (byte) -127;
            i = index;
        }
        index = i + 1;
        envelope[i] = (byte) bodyLength;
        i = index + 1;
        envelope[index] = (byte) (ComprehensionTlvTag.DEVICE_IDENTITIES.value() | 128);
        index = i + 1;
        envelope[i] = (byte) 2;
        i = index + 1;
        envelope[index] = (byte) -125;
        index = i + 1;
        envelope[i] = (byte) -127;
        if (scAddressLength != 0) {
            i = index + 1;
            envelope[index] = (byte) ComprehensionTlvTag.ADDRESS.value();
            index = i + 1;
            envelope[i] = (byte) scAddressLength;
            System.arraycopy(pdu, 1, envelope, index, scAddressLength);
            i = index + scAddressLength;
        } else {
            i = index;
        }
        index = i + 1;
        envelope[i] = (byte) (ComprehensionTlvTag.SMS_TPDU.value() | 128);
        if (tpduLength > 127) {
            i = index + 1;
            envelope[index] = (byte) -127;
        } else {
            i = index;
        }
        index = i + 1;
        envelope[i] = (byte) tpduLength;
        System.arraycopy(pdu, tpduIndex, envelope, index, tpduLength);
        if (index + tpduLength != envelope.length) {
            Rlog.e(TAG, "startDataDownload() calculated incorrect envelope length, aborting.");
            acknowledgeSmsWithError(255);
            return;
        }
        this.mCi.sendEnvelopeWithStatus(IccUtils.bytesToHexString(envelope), obtainMessage(2, new int[]{dcs, pid}));
    }

    private static int getEnvelopeBodyLength(int scAddressLength, int tpduLength) {
        int length = (tpduLength + 5) + (tpduLength > 127 ? 2 : 1);
        if (scAddressLength != 0) {
            return (length + 2) + scAddressLength;
        }
        return length;
    }

    private void sendSmsAckForEnvelopeResponse(IccIoResult response, int dcs, int pid) {
        boolean success;
        int sw1 = response.sw1;
        int sw2 = response.sw2;
        if ((sw1 == 144 && sw2 == 0) || sw1 == 145) {
            Rlog.d(TAG, "USIM data download succeeded: " + response.toString());
            success = true;
        } else if (sw1 == 147 && sw2 == 0) {
            Rlog.e(TAG, "USIM data download failed: Toolkit busy");
            acknowledgeSmsWithError(212);
            return;
        } else if (sw1 == 98 || sw1 == 99) {
            Rlog.e(TAG, "USIM data download failed: " + response.toString());
            success = false;
        } else {
            Rlog.e(TAG, "Unexpected SW1/SW2 response from UICC: " + response.toString());
            success = false;
        }
        byte[] responseBytes = response.payload;
        if (responseBytes == null || responseBytes.length == 0) {
            if (success) {
                this.mCi.acknowledgeLastIncomingGsmSms(true, 0, null);
            } else {
                acknowledgeSmsWithError(213);
            }
            return;
        }
        byte[] smsAckPdu;
        int index;
        int i;
        if (success) {
            smsAckPdu = new byte[(responseBytes.length + 5)];
            smsAckPdu[0] = (byte) 0;
            index = 1 + 1;
            smsAckPdu[1] = (byte) 7;
            i = index;
        } else {
            smsAckPdu = new byte[(responseBytes.length + 6)];
            smsAckPdu[0] = (byte) 0;
            index = 1 + 1;
            smsAckPdu[1] = (byte) -43;
            i = index + 1;
            smsAckPdu[index] = (byte) 7;
        }
        index = i + 1;
        smsAckPdu[i] = (byte) pid;
        i = index + 1;
        smsAckPdu[index] = (byte) dcs;
        if (is7bitDcs(dcs)) {
            index = i + 1;
            smsAckPdu[i] = (byte) ((responseBytes.length * 8) / 7);
            i = index;
        } else {
            index = i + 1;
            smsAckPdu[i] = (byte) responseBytes.length;
            i = index;
        }
        System.arraycopy(responseBytes, 0, smsAckPdu, i, responseBytes.length);
        this.mCi.acknowledgeIncomingGsmSmsWithPdu(success, IccUtils.bytesToHexString(smsAckPdu), null);
    }

    private void acknowledgeSmsWithError(int cause) {
        this.mCi.acknowledgeLastIncomingGsmSms(false, cause, null);
    }

    private static boolean is7bitDcs(int dcs) {
        return (dcs & 140) == 0 || (dcs & 244) == 240;
    }

    public void handleMessage(Message msg) {
        AsyncResult ar;
        switch (msg.what) {
            case 1:
                handleDataDownload((SmsMessage) msg.obj);
                break;
            case 2:
                ar = msg.obj;
                if (ar.exception == null) {
                    int[] dcsPid = ar.userObj;
                    sendSmsAckForEnvelopeResponse((IccIoResult) ar.result, dcsPid[0], dcsPid[1]);
                    break;
                }
                Rlog.e(TAG, "UICC Send Envelope failure, exception: " + ar.exception);
                acknowledgeSmsWithError(213);
                return;
            case 3:
                ar = (AsyncResult) msg.obj;
                if (ar.exception != null) {
                    Rlog.d(TAG, "Failed to write SMS-PP message to UICC", ar.exception);
                    this.mCi.acknowledgeLastIncomingGsmSms(false, 255, null);
                    break;
                }
                Rlog.d(TAG, "Successfully wrote SMS-PP message to UICC");
                this.mCi.acknowledgeLastIncomingGsmSms(true, 0, null);
                break;
            default:
                Rlog.e(TAG, "Ignoring unexpected message, what=" + msg.what);
                break;
        }
    }
}
