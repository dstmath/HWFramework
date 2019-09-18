package com.android.internal.telephony.cdma;

import android.content.Context;
import android.content.res.Resources;
import android.os.Message;
import android.telephony.SmsCbMessage;
import com.android.internal.telephony.CellBroadcastHandler;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.InboundSmsHandler;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.SmsStorageMonitor;
import com.android.internal.telephony.TelephonyComponentFactory;
import com.android.internal.telephony.WspTypeDecoder;
import com.android.internal.util.HexDump;
import java.util.Arrays;

public class CdmaInboundSmsHandler extends InboundSmsHandler {
    private final boolean mCheckForDuplicatePortsInOmadmWapPush = Resources.getSystem().getBoolean(17956944);
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

    /* access modifiers changed from: protected */
    public void onQuitting() {
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

    /* access modifiers changed from: protected */
    public boolean is3gpp2() {
        return true;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00a9, code lost:
        if (r12.mStorageMonitor.isStorageAvailable() != false) goto L_0x00ba;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00b1, code lost:
        if (r0.getMessageClass() == com.android.internal.telephony.SmsConstants.MessageClass.CLASS_0) goto L_0x00ba;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00b3, code lost:
        log("No storage available, return.");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00b9, code lost:
        return 3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00bc, code lost:
        if (4100 != r3) goto L_0x00d6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00d5, code lost:
        return processCdmaWapPdu(r0.getUserData(), r0.mMessageRef, r0.getOriginatingAddress(), r0.getDisplayOriginatingAddress(), r0.getTimestampMillis());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00d9, code lost:
        if (65002 != r3) goto L_0x0118;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00e9, code lost:
        if (com.android.internal.telephony.HwTelephonyFactory.getHwInnerSmsManager().currentSubIsChinaTelecomSim(r12.mPhone.getPhoneId()) == false) goto L_0x0118;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00eb, code lost:
        log("CT's MMS notification");
        r4 = com.android.internal.telephony.cdma.sms.BearerData.decode(r0.getUserData());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00f8, code lost:
        if (r4 != null) goto L_0x0100;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00fa, code lost:
        log("Decode user data failed");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00ff, code lost:
        return 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0117, code lost:
        return processCdmaWapPdu(r4.userData.payload, 31, r0.getOriginatingAddress(), r0.getDisplayOriginatingAddress(), r0.getTimestampMillis());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x011c, code lost:
        return dispatchNormalMessage(r13);
     */
    public int dispatchMessageRadioSpecific(SmsMessageBase smsb) {
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
            if (teleService != 262144) {
                switch (teleService) {
                    case 4098:
                    case 4101:
                        if (sms.isStatusReportMessage()) {
                            this.mSmsDispatcher.sendStatusReportMessage(sms);
                            return 1;
                        }
                        break;
                    case 4099:
                        break;
                    case 4100:
                        break;
                    case 4102:
                        this.mServiceCategoryProgramHandler.dispatchSmsMessage(sms);
                        return 1;
                    default:
                        int doResult = HwTelephonyFactory.getHwInnerSmsManager().handleExtendTeleService(teleService, this.mSmsDispatcher, sms);
                        if (doResult != 0) {
                            if (doResult == 1) {
                                return 1;
                            }
                            loge("unsupported teleservice 0x" + Integer.toHexString(teleService));
                            return 4;
                        }
                        break;
                }
            }
            handleVoicemailTeleservice(sms);
            return 1;
        }
        log("Receives network duplicate SMS by fingerprint, return.");
        return 1;
    }

    /* access modifiers changed from: protected */
    public void acknowledgeLastIncomingSms(boolean success, int result, Message response) {
        int causeCode = resultToCause(result);
        this.mPhone.mCi.acknowledgeLastIncomingCdmaSms(success, causeCode, response);
        if (causeCode == 0) {
            this.mLastAcknowledgedSmsFingerprint = this.mLastDispatchedSmsFingerprint;
        }
        this.mLastDispatchedSmsFingerprint = null;
    }

    /* access modifiers changed from: protected */
    public void onUpdatePhoneObject(Phone phone) {
        super.onUpdatePhoneObject(phone);
        this.mCellBroadcastHandler.updatePhoneObject(phone);
    }

    private static int resultToCause(int rc) {
        if (rc == -1 || rc == 1) {
            return 0;
        }
        switch (rc) {
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
        int index;
        int destinationPort;
        byte[] bArr = pdu;
        int index2 = 0 + 1;
        int msgType = bArr[0] & 255;
        if (msgType != 0) {
            log("Received a WAP SMS which is not WDP. Discard.");
            return 1;
        }
        int index3 = index2 + 1;
        int index4 = bArr[index2] & 255;
        int index5 = index3 + 1;
        int segment = bArr[index3] & 255;
        if (segment >= index4) {
            loge("WDP bad segment #" + segment + " expecting 0-" + (index4 - 1));
            return 1;
        }
        int sourcePort = 0;
        if (segment == 0) {
            int index6 = index5 + 1;
            int sourcePort2 = (bArr[index5] & 255) << 8;
            int index7 = index6 + 1;
            sourcePort = sourcePort2 | (bArr[index6] & 255);
            int index8 = index7 + 1;
            int destinationPort2 = index8 + 1;
            destinationPort = (255 & bArr[index8]) | ((bArr[index7] & 255) << 8);
            index = (this.mCheckForDuplicatePortsInOmadmWapPush == 0 || !checkDuplicatePortOmadmWapPush(bArr, destinationPort2)) ? destinationPort2 : destinationPort2 + 4;
        } else {
            index = index5;
            destinationPort = 0;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Received WAP PDU. Type = ");
        sb.append(msgType);
        sb.append(", originator = ");
        String str = address;
        sb.append(str);
        sb.append(", src-port = ");
        sb.append(sourcePort);
        sb.append(", dst-port = ");
        sb.append(destinationPort);
        sb.append(", ID = ");
        sb.append(referenceNumber);
        sb.append(", segment# = ");
        sb.append(segment);
        sb.append('/');
        sb.append(index4);
        log(sb.toString());
        byte[] userData = new byte[(bArr.length - index)];
        System.arraycopy(bArr, index, userData, 0, bArr.length - index);
        byte[] bArr2 = userData;
        int i = index;
        return addTrackerToRawTableAndSendMessage(TelephonyComponentFactory.getInstance().makeInboundSmsTracker(userData, timestamp, destinationPort, true, str, dispAddr, referenceNumber, segment, index4, true, HexDump.toHexString(userData)), false);
    }

    private static boolean checkDuplicatePortOmadmWapPush(byte[] origPdu, int index) {
        int index2 = index + 4;
        byte[] omaPdu = new byte[(origPdu.length - index2)];
        System.arraycopy(origPdu, index2, omaPdu, 0, omaPdu.length);
        WspTypeDecoder pduDecoder = HwTelephonyFactory.getHwInnerSmsManager().createHwWspTypeDecoder(omaPdu);
        if (pduDecoder.decodeUintvarInteger(2) && pduDecoder.decodeContentType(2 + pduDecoder.getDecodedDataLength())) {
            return WspTypeDecoder.CONTENT_TYPE_B_PUSH_SYNCML_NOTI.equals(pduDecoder.getValueString());
        }
        return false;
    }
}
