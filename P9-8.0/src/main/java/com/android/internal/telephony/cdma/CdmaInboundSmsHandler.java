package com.android.internal.telephony.cdma;

import android.content.Context;
import android.content.res.Resources;
import android.hardware.radio.V1_0.DataCallFailCause;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.SmsCbMessage;
import com.android.internal.telephony.CellBroadcastHandler;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.InboundSmsHandler;
import com.android.internal.telephony.InboundSmsTracker;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.SmsConstants.MessageClass;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.SmsStorageMonitor;
import com.android.internal.telephony.TelephonyComponentFactory;
import com.android.internal.telephony.WspTypeDecoder;
import com.android.internal.telephony.cdma.sms.BearerData;
import com.android.internal.util.HexDump;
import java.util.Arrays;

public class CdmaInboundSmsHandler extends InboundSmsHandler {
    private final boolean mCheckForDuplicatePortsInOmadmWapPush = Resources.getSystem().getBoolean(17956936);
    private byte[] mLastAcknowledgedSmsFingerprint;
    private byte[] mLastDispatchedSmsFingerprint;
    private final CdmaServiceCategoryProgramHandler mServiceCategoryProgramHandler;
    private final CdmaSMSDispatcher mSmsDispatcher;

    private CdmaInboundSmsHandler(Context context, SmsStorageMonitor storageMonitor, Phone phone, CdmaSMSDispatcher smsDispatcher) {
        super("CdmaInboundSmsHandler", context, storageMonitor, phone, CellBroadcastHandler.makeCellBroadcastHandler(context, phone));
        this.mSmsDispatcher = smsDispatcher;
        this.mServiceCategoryProgramHandler = CdmaServiceCategoryProgramHandler.makeScpHandler(context, phone.mCi);
        phone.mCi.setOnNewCdmaSms(getHandler(), 1, null);
    }

    protected void onQuitting() {
        this.mPhone.mCi.unSetOnNewCdmaSms(getHandler());
        this.mCellBroadcastHandler.dispose();
        log("unregistered for 3GPP2 SMS");
        super.onQuitting();
    }

    public static CdmaInboundSmsHandler makeInboundSmsHandler(Context context, SmsStorageMonitor storageMonitor, Phone phone, CdmaSMSDispatcher smsDispatcher) {
        CdmaInboundSmsHandler handler = new CdmaInboundSmsHandler(context, storageMonitor, phone, smsDispatcher);
        handler.start();
        return handler;
    }

    protected boolean is3gpp2() {
        return true;
    }

    protected int dispatchMessageRadioSpecific(SmsMessageBase smsb) {
        SmsMessage sms = (SmsMessage) smsb;
        if (1 == sms.getMessageType()) {
            log("Broadcast type message");
            SmsCbMessage cbMessage = sms.parseBroadcastSms();
            if (cbMessage != null) {
                this.mCellBroadcastHandler.dispatchSmsMessage(cbMessage);
            } else {
                loge("error trying to parse broadcast SMS");
            }
            return 1;
        }
        this.mLastDispatchedSmsFingerprint = sms.getIncomingSmsFingerprint();
        if (this.mLastAcknowledgedSmsFingerprint == null || !Arrays.equals(this.mLastDispatchedSmsFingerprint, this.mLastAcknowledgedSmsFingerprint)) {
            sms.parseSms();
            int teleService = sms.getTeleService();
            log("teleService: 0x" + Integer.toHexString(teleService));
            switch (teleService) {
                case DataCallFailCause.OEM_DCFAILCAUSE_2 /*4098*/:
                case DataCallFailCause.OEM_DCFAILCAUSE_5 /*4101*/:
                    if (sms.isStatusReportMessage()) {
                        this.mSmsDispatcher.sendStatusReportMessage(sms);
                        return 1;
                    }
                    break;
                case DataCallFailCause.OEM_DCFAILCAUSE_3 /*4099*/:
                case InboundSmsTracker.DEST_PORT_FLAG_3GPP2 /*262144*/:
                    handleVoicemailTeleservice(sms);
                    return 1;
                case DataCallFailCause.OEM_DCFAILCAUSE_4 /*4100*/:
                case 65002:
                    break;
                case DataCallFailCause.OEM_DCFAILCAUSE_6 /*4102*/:
                    this.mServiceCategoryProgramHandler.dispatchSmsMessage(sms);
                    return 1;
                case 65005:
                    if ((92 == SystemProperties.getInt("ro.config.hw_opta", 0) || ServiceStateTracker.CS_NOTIFICATION == SystemProperties.getInt("ro.config.hw_opta", 0)) && 156 == SystemProperties.getInt("ro.config.hw_optb", 0)) {
                        log("CT's AutoRegSms notification!");
                        this.mSmsDispatcher.dispatchCTAutoRegSmsPdus(sms);
                        return 1;
                    }
                default:
                    loge("unsupported teleservice 0x" + Integer.toHexString(teleService));
                    return 4;
            }
            if (!this.mStorageMonitor.isStorageAvailable() && sms.getMessageClass() != MessageClass.CLASS_0) {
                log("No storage available, return.");
                return 3;
            } else if (DataCallFailCause.OEM_DCFAILCAUSE_4 == teleService) {
                return processCdmaWapPdu(sms.getUserData(), sms.mMessageRef, sms.getOriginatingAddress(), sms.getDisplayOriginatingAddress(), sms.getTimestampMillis());
            } else if (65002 != teleService || !HwTelephonyFactory.getHwInnerSmsManager().currentSubIsChinaTelecomSim(this.mPhone.getPhoneId())) {
                return dispatchNormalMessage(smsb);
            } else {
                log("CT's MMS notification");
                BearerData mCTBearerData = BearerData.decode(sms.getUserData());
                if (mCTBearerData == null) {
                    log("Decode user data failed");
                    return 1;
                }
                return processCdmaWapPdu(mCTBearerData.userData.payload, 31, sms.getOriginatingAddress(), sms.getDisplayOriginatingAddress(), sms.getTimestampMillis());
            }
        }
        log("Receives network duplicate SMS by fingerprint, return.");
        return 1;
    }

    protected void acknowledgeLastIncomingSms(boolean success, int result, Message response) {
        int causeCode = resultToCause(result);
        this.mPhone.mCi.acknowledgeLastIncomingCdmaSms(success, causeCode, response);
        if (causeCode == 0) {
            this.mLastAcknowledgedSmsFingerprint = this.mLastDispatchedSmsFingerprint;
        }
        this.mLastDispatchedSmsFingerprint = null;
    }

    protected void onUpdatePhoneObject(Phone phone) {
        super.onUpdatePhoneObject(phone);
        this.mCellBroadcastHandler.updatePhoneObject(phone);
    }

    private static int resultToCause(int rc) {
        switch (rc) {
            case -1:
            case 1:
                return 0;
            case 3:
                return 35;
            case 4:
                return 4;
            default:
                return 39;
        }
    }

    private void handleVoicemailTeleservice(SmsMessage sms) {
        int voicemailCount = sms.getNumOfVoicemails();
        log("Voicemail count=" + voicemailCount);
        if (voicemailCount < 0) {
            voicemailCount = -1;
        } else if (voicemailCount > 99) {
            voicemailCount = 99;
        }
        this.mPhone.setVoiceMessageCount(voicemailCount);
    }

    private int processCdmaWapPdu(byte[] pdu, int referenceNumber, String address, String dispAddr, long timestamp) {
        int msgType = pdu[0] & 255;
        if (msgType != 0) {
            log("Received a WAP SMS which is not WDP. Discard.");
            return 1;
        }
        int index = 1 + 1;
        int totalSegments = pdu[1] & 255;
        int index2 = index + 1;
        int segment = pdu[index] & 255;
        if (segment >= totalSegments) {
            loge("WDP bad segment #" + segment + " expecting 0-" + (totalSegments - 1));
            return 1;
        }
        int sourcePort = 0;
        int destinationPort = 0;
        if (segment == 0) {
            index = index2 + 1;
            index2 = index + 1;
            sourcePort = ((pdu[index2] & 255) << 8) | (pdu[index] & 255);
            index = index2 + 1;
            index2 = index + 1;
            destinationPort = ((pdu[index2] & 255) << 8) | (pdu[index] & 255);
            if (this.mCheckForDuplicatePortsInOmadmWapPush && checkDuplicatePortOmadmWapPush(pdu, index2)) {
                index2 += 4;
            }
        }
        log("Received WAP PDU. Type = " + msgType + ", originator = " + address + ", src-port = " + sourcePort + ", dst-port = " + destinationPort + ", ID = " + referenceNumber + ", segment# = " + segment + '/' + totalSegments);
        byte[] userData = new byte[(pdu.length - index2)];
        System.arraycopy(pdu, index2, userData, 0, pdu.length - index2);
        return addTrackerToRawTableAndSendMessage(TelephonyComponentFactory.getInstance().makeInboundSmsTracker(userData, timestamp, destinationPort, true, address, dispAddr, referenceNumber, segment, totalSegments, true, HexDump.toHexString(userData)), false);
    }

    private static boolean checkDuplicatePortOmadmWapPush(byte[] origPdu, int index) {
        index += 4;
        byte[] omaPdu = new byte[(origPdu.length - index)];
        System.arraycopy(origPdu, index, omaPdu, 0, omaPdu.length);
        WspTypeDecoder pduDecoder = HwTelephonyFactory.getHwInnerSmsManager().createHwWspTypeDecoder(omaPdu);
        if (!pduDecoder.decodeUintvarInteger(2) || !pduDecoder.decodeContentType(pduDecoder.getDecodedDataLength() + 2)) {
            return false;
        }
        return WspTypeDecoder.CONTENT_TYPE_B_PUSH_SYNCML_NOTI.equals(pduDecoder.getValueString());
    }
}
