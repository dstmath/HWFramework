package com.android.internal.telephony.gsm;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import com.android.internal.telephony.CallFailCause;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.cat.ComprehensionTlvTag;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.telephony.uicc.IccIoResult;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.UsimServiceTable;

public class UsimDataDownloadHandler extends Handler {
    private static final int BER_SMS_PP_DOWNLOAD_TAG = 209;
    private static final int DEV_ID_NETWORK = 131;
    private static final int DEV_ID_UICC = 129;
    private static final int EVENT_SEND_ENVELOPE_RESPONSE = 2;
    private static final int EVENT_START_DATA_DOWNLOAD = 1;
    private static final int EVENT_WRITE_SMS_COMPLETE = 3;
    private static final String TAG = "UsimDataDownloadHandler";
    private final CommandsInterface mCi;
    private final int mPhoneId;

    public UsimDataDownloadHandler(CommandsInterface commandsInterface, int phoneId) {
        this.mCi = commandsInterface;
        this.mPhoneId = phoneId;
    }

    /* access modifiers changed from: package-private */
    public int handleUsimDataDownload(UsimServiceTable ust, SmsMessage smsMessage) {
        if (ust == null || !ust.isAvailable(UsimServiceTable.UsimService.DATA_DL_VIA_SMS_PP)) {
            Rlog.d(TAG, "DATA_DL_VIA_SMS_PP service not available, storing message to UICC.");
            this.mCi.writeSmsToSim(3, IccUtils.bytesToHexString(PhoneNumberUtils.networkPortionToCalledPartyBCDWithLength(smsMessage.getServiceCenterAddress())), IccUtils.bytesToHexString(smsMessage.getPdu()), obtainMessage(3));
            addUsimDataDownloadToMetrics(false);
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
        int dcs = smsMessage.getDataCodingScheme();
        int pid = smsMessage.getProtocolIdentifier();
        byte[] pdu = smsMessage.getPdu();
        int scAddressLength = pdu[0] & 255;
        int tpduIndex = scAddressLength + 1;
        int tpduLength = pdu.length - tpduIndex;
        int bodyLength = getEnvelopeBodyLength(scAddressLength, tpduLength);
        byte[] envelope = new byte[(bodyLength + 1 + (bodyLength > 127 ? 2 : 1))];
        int index = 0 + 1;
        envelope[0] = -47;
        if (bodyLength > 127) {
            envelope[index] = -127;
            index++;
        }
        int index2 = index + 1;
        envelope[index] = (byte) bodyLength;
        int index3 = index2 + 1;
        envelope[index2] = (byte) (ComprehensionTlvTag.DEVICE_IDENTITIES.value() | 128);
        int index4 = index3 + 1;
        envelope[index3] = 2;
        int index5 = index4 + 1;
        envelope[index4] = -125;
        int index6 = index5 + 1;
        envelope[index5] = -127;
        if (scAddressLength != 0) {
            int index7 = index6 + 1;
            envelope[index6] = (byte) ComprehensionTlvTag.ADDRESS.value();
            int index8 = index7 + 1;
            envelope[index7] = (byte) scAddressLength;
            System.arraycopy(pdu, 1, envelope, index8, scAddressLength);
            index6 = index8 + scAddressLength;
        }
        int index9 = index6 + 1;
        envelope[index6] = (byte) (ComprehensionTlvTag.SMS_TPDU.value() | 128);
        if (tpduLength > 127) {
            envelope[index9] = -127;
            index9++;
        }
        int index10 = index9 + 1;
        envelope[index9] = (byte) tpduLength;
        System.arraycopy(pdu, tpduIndex, envelope, index10, tpduLength);
        if (index10 + tpduLength != envelope.length) {
            Rlog.e(TAG, "startDataDownload() calculated incorrect envelope length, aborting.");
            acknowledgeSmsWithError(255);
            addUsimDataDownloadToMetrics(false);
            return;
        }
        this.mCi.sendEnvelopeWithStatus(IccUtils.bytesToHexString(envelope), obtainMessage(2, new int[]{dcs, pid}));
        addUsimDataDownloadToMetrics(true);
    }

    private static int getEnvelopeBodyLength(int scAddressLength, int tpduLength) {
        int length = tpduLength + 5 + (tpduLength > 127 ? 2 : 1);
        if (scAddressLength != 0) {
            return length + 2 + scAddressLength;
        }
        return length;
    }

    private void sendSmsAckForEnvelopeResponse(IccIoResult response, int dcs, int pid) {
        boolean success;
        byte[] smsAckPdu;
        int index;
        int index2;
        int sw1 = response.sw1;
        int sw2 = response.sw2;
        if ((sw1 == 144 && sw2 == 0) || sw1 == 145) {
            Rlog.d(TAG, "USIM data download succeeded: " + response.toString());
            success = true;
        } else if (sw1 == 147 && sw2 == 0) {
            Rlog.e(TAG, "USIM data download failed: Toolkit busy");
            acknowledgeSmsWithError(CommandsInterface.GSM_SMS_FAIL_CAUSE_USIM_APP_TOOLKIT_BUSY);
            return;
        } else if (sw1 == 98 || sw1 == 99) {
            Rlog.e(TAG, "USIM data download failed: " + response.toString());
            success = false;
        } else {
            Rlog.e(TAG, "Unexpected SW1/SW2 response from UICC: " + response.toString());
            success = false;
        }
        byte[] responseBytes = response.payload;
        if (responseBytes != null && responseBytes.length != 0) {
            if (success) {
                smsAckPdu = new byte[(responseBytes.length + 5)];
                int index3 = 0 + 1;
                smsAckPdu[0] = 0;
                index = index3 + 1;
                smsAckPdu[index3] = 7;
            } else {
                smsAckPdu = new byte[(responseBytes.length + 6)];
                int index4 = 0 + 1;
                smsAckPdu[0] = 0;
                int index5 = index4 + 1;
                smsAckPdu[index4] = -43;
                smsAckPdu[index5] = 7;
                index = index5 + 1;
            }
            int index6 = index + 1;
            smsAckPdu[index] = (byte) pid;
            int index7 = index6 + 1;
            smsAckPdu[index6] = (byte) dcs;
            if (is7bitDcs(dcs)) {
                index2 = index7 + 1;
                smsAckPdu[index7] = (byte) ((responseBytes.length * 8) / 7);
            } else {
                index2 = index7 + 1;
                smsAckPdu[index7] = (byte) responseBytes.length;
            }
            System.arraycopy(responseBytes, 0, smsAckPdu, index2, responseBytes.length);
            this.mCi.acknowledgeIncomingGsmSmsWithPdu(success, IccUtils.bytesToHexString(smsAckPdu), null);
        } else if (success) {
            this.mCi.acknowledgeLastIncomingGsmSms(true, 0, null);
        } else {
            acknowledgeSmsWithError(CommandsInterface.GSM_SMS_FAIL_CAUSE_USIM_DATA_DOWNLOAD_ERROR);
        }
    }

    private void acknowledgeSmsWithError(int cause) {
        this.mCi.acknowledgeLastIncomingGsmSms(false, cause, null);
    }

    private static boolean is7bitDcs(int dcs) {
        return (dcs & 140) == 0 || (dcs & CallFailCause.DIAL_MODIFIED_TO_USSD) == 240;
    }

    private void addUsimDataDownloadToMetrics(boolean result) {
        TelephonyMetrics.getInstance().writeIncomingSMSPP(this.mPhoneId, "3gpp", result);
    }

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i == 1) {
            handleDataDownload((SmsMessage) msg.obj);
        } else if (i == 2) {
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar.exception != null) {
                Rlog.e(TAG, "UICC Send Envelope failure, exception: " + ar.exception);
                acknowledgeSmsWithError(CommandsInterface.GSM_SMS_FAIL_CAUSE_USIM_DATA_DOWNLOAD_ERROR);
                return;
            }
            int[] dcsPid = (int[]) ar.userObj;
            sendSmsAckForEnvelopeResponse((IccIoResult) ar.result, dcsPid[0], dcsPid[1]);
        } else if (i != 3) {
            Rlog.e(TAG, "Ignoring unexpected message, what=" + msg.what);
        } else {
            AsyncResult ar2 = (AsyncResult) msg.obj;
            if (ar2.exception == null) {
                Rlog.d(TAG, "Successfully wrote SMS-PP message to UICC");
                this.mCi.acknowledgeLastIncomingGsmSms(true, 0, null);
                return;
            }
            Rlog.d(TAG, "Failed to write SMS-PP message to UICC", ar2.exception);
            this.mCi.acknowledgeLastIncomingGsmSms(false, 255, null);
        }
    }
}
