package com.android.internal.telephony.cdma;

import android.content.Context;
import android.content.res.Resources;
import android.os.Message;
import android.telephony.SmsCbMessage;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.CellBroadcastHandler;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.InboundSmsHandler;
import com.android.internal.telephony.InboundSmsTracker;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SmsConstants;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.SmsStorageMonitor;
import com.android.internal.telephony.TelephonyComponentFactory;
import com.android.internal.telephony.WspTypeDecoder;
import com.android.internal.telephony.cdma.sms.BearerData;
import com.android.internal.util.HexDump;
import com.huawei.internal.telephony.CdmaSMSDispatcherEx;
import com.huawei.internal.telephony.cdma.SmsMessageEx;
import java.util.Arrays;

public class CdmaInboundSmsHandler extends InboundSmsHandler {
    private static final String CT_SMS_SEND_CENTER = "10659401";
    private final boolean mCheckForDuplicatePortsInOmadmWapPush = Resources.getSystem().getBoolean(17891427);
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
    @Override // com.android.internal.telephony.InboundSmsHandler
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
    @Override // com.android.internal.telephony.InboundSmsHandler
    public boolean is3gpp2() {
        return true;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00e0  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00f3  */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x010b  */
    @Override // com.android.internal.telephony.InboundSmsHandler
    public int dispatchMessageRadioSpecific(SmsMessageBase smsb) {
        SmsMessage sms = (SmsMessage) smsb;
        if (1 == sms.getMessageType()) {
            log("Broadcast type message");
            SmsCbMessage cbMessage = sms.parseBroadcastSms(TelephonyManager.from(this.mContext).getNetworkOperatorForPhone(this.mPhone.getPhoneId()));
            if (cbMessage != null) {
                this.mCellBroadcastHandler.dispatchSmsMessage(cbMessage);
            } else {
                loge("error trying to parse broadcast SMS");
            }
            return 1;
        }
        this.mLastDispatchedSmsFingerprint = sms.getIncomingSmsFingerprint();
        byte[] bArr = this.mLastAcknowledgedSmsFingerprint;
        if (bArr == null || !Arrays.equals(this.mLastDispatchedSmsFingerprint, bArr)) {
            sms.parseSms();
            int teleService = sms.getTeleService();
            log("teleService: 0x" + Integer.toHexString(teleService));
            if (!CT_SMS_SEND_CENTER.equals(sms.getOriginatingAddress()) || teleService == 65005) {
                if (teleService != 262144) {
                    switch (teleService) {
                        case 4098:
                        case 4101:
                            if (sms.isStatusReportMessage()) {
                                this.mSmsDispatcher.sendStatusReportMessage(sms);
                                return 1;
                            }
                            if (this.mStorageMonitor.isStorageAvailable() && sms.getMessageClass() != SmsConstants.MessageClass.CLASS_0) {
                                log("No storage available, return.");
                                return 3;
                            } else if (4100 == teleService) {
                                return processCdmaWapPdu(sms.getUserData(), sms.mMessageRef, sms.getOriginatingAddress(), sms.getDisplayOriginatingAddress(), sms.getTimestampMillis());
                            } else {
                                if (65002 != teleService || !HwTelephonyFactory.getHwInnerSmsManager().currentSubIsChinaTelecomSim(this.mPhone.getPhoneId())) {
                                    return dispatchNormalMessage(smsb);
                                }
                                log("CT's MMS notification");
                                BearerData mCTBearerData = BearerData.decode(sms.getUserData());
                                if (mCTBearerData != null) {
                                    return processCdmaWapPdu(mCTBearerData.userData.payload, 31, sms.getOriginatingAddress(), sms.getDisplayOriginatingAddress(), sms.getTimestampMillis());
                                }
                                log("Decode user data failed");
                                return 1;
                            }
                        case 4099:
                            break;
                        case 4100:
                            if (this.mStorageMonitor.isStorageAvailable()) {
                                break;
                            }
                            if (4100 == teleService) {
                            }
                            break;
                        case 4102:
                            this.mServiceCategoryProgramHandler.dispatchSmsMessage(sms);
                            return 1;
                        default:
                            CdmaSMSDispatcherEx cdmaSMSDispatcherEx = new CdmaSMSDispatcherEx();
                            cdmaSMSDispatcherEx.setCdmaSmsDispatcher(this.mSmsDispatcher);
                            SmsMessageEx smsMessageEx = new SmsMessageEx();
                            smsMessageEx.setSmsMessage(sms);
                            int doResult = HwTelephonyFactory.getHwInnerSmsManager().handleExtendTeleService(teleService, cdmaSMSDispatcherEx, smsMessageEx);
                            if (doResult != 0) {
                                if (doResult == 1) {
                                    return 1;
                                }
                                loge("unsupported teleservice 0x" + Integer.toHexString(teleService));
                                return 4;
                            }
                            if (this.mStorageMonitor.isStorageAvailable()) {
                            }
                            if (4100 == teleService) {
                            }
                            break;
                    }
                }
                handleVoicemailTeleservice(sms);
                return 1;
            }
            log("not auto CT message but send form CT center.");
            return 4;
        }
        log("Receives network duplicate SMS by fingerprint, return.");
        return 1;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.InboundSmsHandler
    public void acknowledgeLastIncomingSms(boolean success, int result, Message response) {
        int causeCode = resultToCause(result);
        this.mPhone.mCi.acknowledgeLastIncomingCdmaSms(success, causeCode, response);
        if (causeCode == 0) {
            this.mLastAcknowledgedSmsFingerprint = this.mLastDispatchedSmsFingerprint;
        }
        this.mLastDispatchedSmsFingerprint = null;
    }

    private static int resultToCause(int rc) {
        if (rc == -1 || rc == 1) {
            return 0;
        }
        if (rc != 3) {
            return rc != 4 ? 39 : 4;
        }
        return 35;
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
        addVoicemailSmsToMetrics();
    }

    /* JADX INFO: Multiple debug info for r7v11 int: [D('destinationPort' int), D('index' int)] */
    private int processCdmaWapPdu(byte[] pdu, int referenceNumber, String address, String dispAddr, long timestamp) {
        int destinationPort;
        int index;
        int index2 = 0 + 1;
        int msgType = pdu[0] & 255;
        if (msgType != 0) {
            log("Received a WAP SMS which is not WDP. Discard.");
            return 1;
        }
        int index3 = index2 + 1;
        int totalSegments = pdu[index2] & 255;
        int index4 = index3 + 1;
        int segment = pdu[index3] & 255;
        if (segment >= totalSegments) {
            StringBuilder sb = new StringBuilder();
            sb.append("WDP bad segment #");
            sb.append(segment);
            sb.append(" expecting 0-");
            sb.append(totalSegments - 1);
            loge(sb.toString());
            return 1;
        }
        int sourcePort = 0;
        if (segment == 0) {
            int index5 = index4 + 1;
            int sourcePort2 = (pdu[index4] & 255) << 8;
            int index6 = index5 + 1;
            sourcePort = sourcePort2 | (pdu[index5] & 255);
            int index7 = index6 + 1;
            int index8 = index7 + 1;
            int destinationPort2 = ((pdu[index6] & 255) << 8) | (pdu[index7] & 255);
            if (!this.mCheckForDuplicatePortsInOmadmWapPush || !checkDuplicatePortOmadmWapPush(pdu, index8)) {
                destinationPort = destinationPort2;
                index = index8;
            } else {
                destinationPort = destinationPort2;
                index = index8 + 4;
            }
        } else {
            index = index4;
            destinationPort = 0;
        }
        log("Received WAP PDU. Type = " + msgType + ", originator = " + address + ", src-port = " + sourcePort + ", dst-port = " + destinationPort + ", ID = " + referenceNumber + ", segment# = " + segment + '/' + totalSegments);
        byte[] userData = new byte[(pdu.length - index)];
        System.arraycopy(pdu, index, userData, 0, pdu.length - index);
        return addTrackerToRawTableAndSendMessage(TelephonyComponentFactory.getInstance().inject(InboundSmsTracker.class.getName()).makeInboundSmsTracker(userData, timestamp, destinationPort, true, address, dispAddr, referenceNumber, segment, totalSegments, true, HexDump.toHexString(userData), false), false);
    }

    private static boolean checkDuplicatePortOmadmWapPush(byte[] origPdu, int index) {
        int index2 = index + 4;
        byte[] omaPdu = new byte[(origPdu.length - index2)];
        System.arraycopy(origPdu, index2, omaPdu, 0, omaPdu.length);
        WspTypeDecoder pduDecoder = new WspTypeDecoder(omaPdu);
        if (pduDecoder.decodeUintvarInteger(2) && pduDecoder.decodeContentType(2 + pduDecoder.getDecodedDataLength())) {
            return WspTypeDecoder.CONTENT_TYPE_B_PUSH_SYNCML_NOTI.equals(pduDecoder.getValueString());
        }
        return false;
    }

    private void addVoicemailSmsToMetrics() {
        this.mMetrics.writeIncomingVoiceMailSms(this.mPhone.getPhoneId(), "3gpp2");
    }
}
