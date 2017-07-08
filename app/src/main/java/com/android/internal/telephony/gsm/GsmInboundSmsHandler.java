package com.android.internal.telephony.gsm;

import android.content.Context;
import android.os.Message;
import com.android.internal.telephony.InboundSmsHandler;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SmsConstants.MessageClass;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.SmsStorageMonitor;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.google.android.mms.pdu.PduHeaders;
import com.google.android.mms.pdu.PduPersister;

public class GsmInboundSmsHandler extends InboundSmsHandler {
    private final UsimDataDownloadHandler mDataDownloadHandler;

    private GsmInboundSmsHandler(Context context, SmsStorageMonitor storageMonitor, Phone phone) {
        super("GsmInboundSmsHandler", context, storageMonitor, phone, GsmCellBroadcastHandler.makeGsmCellBroadcastHandler(context, phone));
        phone.mCi.setOnNewGsmSms(getHandler(), 1, null);
        this.mDataDownloadHandler = new UsimDataDownloadHandler(phone.mCi);
    }

    protected void onQuitting() {
        this.mPhone.mCi.unSetOnNewGsmSms(getHandler());
        this.mCellBroadcastHandler.dispose();
        log("unregistered for 3GPP SMS");
        super.onQuitting();
    }

    public static GsmInboundSmsHandler makeInboundSmsHandler(Context context, SmsStorageMonitor storageMonitor, Phone phone) {
        GsmInboundSmsHandler handler = new GsmInboundSmsHandler(context, storageMonitor, phone);
        handler.start();
        return handler;
    }

    protected boolean is3gpp2() {
        return false;
    }

    protected int dispatchMessageRadioSpecific(SmsMessageBase smsb) {
        boolean z = false;
        SmsMessage sms = (SmsMessage) smsb;
        if (sms.isTypeZero()) {
            log("Received short message type 0, Don't display or store it. Send Ack");
            return 1;
        } else if (sms.isUsimDataDownload()) {
            return this.mDataDownloadHandler.handleUsimDataDownload(this.mPhone.getUsimServiceTable(), sms);
        } else {
            boolean handled = false;
            StringBuilder append;
            if (sms.isMWISetMessage()) {
                updateMessageWaitingIndicator(sms.getNumOfVoicemails());
                handled = sms.isMwiDontStore();
                append = new StringBuilder().append("Received voice mail indicator set SMS shouldStore=");
                if (!handled) {
                    z = true;
                }
                log(append.append(z).toString());
            } else if (sms.isMWIClearMessage()) {
                updateMessageWaitingIndicator(0);
                handled = sms.isMwiDontStore();
                append = new StringBuilder().append("Received voice mail indicator clear SMS shouldStore=");
                if (!handled) {
                    z = true;
                }
                log(append.append(z).toString());
            }
            if (handled) {
                return 1;
            }
            if (this.mStorageMonitor.isStorageAvailable() || sms.getMessageClass() == MessageClass.CLASS_0) {
                return dispatchNormalMessage(smsb);
            }
            return 3;
        }
    }

    private void updateMessageWaitingIndicator(int voicemailCount) {
        if (voicemailCount < 0) {
            voicemailCount = -1;
        } else if (voicemailCount > PduHeaders.STORE_STATUS_ERROR_END) {
            voicemailCount = PduHeaders.STORE_STATUS_ERROR_END;
        }
        this.mPhone.setVoiceMessageCount(voicemailCount);
        IccRecords records = UiccController.getInstance().getIccRecords(this.mPhone.getPhoneId(), 1);
        if (records != null) {
            log("updateMessageWaitingIndicator: updating SIM Records");
            records.setVoiceMessageWaiting(1, voicemailCount);
            return;
        }
        log("updateMessageWaitingIndicator: SIM Records not found");
    }

    protected void acknowledgeLastIncomingSms(boolean success, int result, Message response) {
        this.mPhone.mCi.acknowledgeLastIncomingGsmSms(success, resultToCause(result), response);
    }

    protected void onUpdatePhoneObject(Phone phone) {
        super.onUpdatePhoneObject(phone);
        log("onUpdatePhoneObject: dispose of old CellBroadcastHandler and make a new one");
        this.mCellBroadcastHandler.dispose();
        this.mCellBroadcastHandler = GsmCellBroadcastHandler.makeGsmCellBroadcastHandler(this.mContext, phone);
    }

    private static int resultToCause(int rc) {
        switch (rc) {
            case UiccCardApplication.AUTH_CONTEXT_UNDEFINED /*-1*/:
            case PduPersister.PROC_STATUS_TRANSIENT_FAILURE /*1*/:
                return 0;
            case PduPersister.PROC_STATUS_COMPLETED /*3*/:
                return BerTlv.BER_MENU_SELECTION_TAG;
            default:
                return PduHeaders.STORE_STATUS_ERROR_END;
        }
    }
}
