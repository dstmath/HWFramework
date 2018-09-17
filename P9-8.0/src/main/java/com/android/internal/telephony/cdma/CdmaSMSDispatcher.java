package com.android.internal.telephony.cdma;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Intent;
import android.net.Uri;
import android.os.Message;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import com.android.internal.telephony.GsmAlphabet.TextEncodingDetails;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.ImsSMSDispatcher;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants.State;
import com.android.internal.telephony.SMSDispatcher;
import com.android.internal.telephony.SMSDispatcher.SmsTracker;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.SmsMessageBase.SubmitPduBase;
import com.android.internal.telephony.SmsStorageMonitor;
import com.android.internal.telephony.SmsUsageMonitor;
import com.android.internal.telephony.cdma.SmsMessage.SubmitPdu;
import com.android.internal.telephony.cdma.sms.UserData;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class CdmaSMSDispatcher extends SMSDispatcher {
    private static final String TAG = "CdmaSMSDispatcher";
    private static final boolean VDBG = true;

    public CdmaSMSDispatcher(Phone phone, SmsUsageMonitor usageMonitor, ImsSMSDispatcher imsSMSDispatcher) {
        super(phone, usageMonitor, imsSMSDispatcher);
        Rlog.d(TAG, "CdmaSMSDispatcher created");
    }

    public CdmaSMSDispatcher(Phone phone, SmsStorageMonitor storageMonitor, SmsUsageMonitor usageMonitor, ImsSMSDispatcher imsSMSDispatcher) {
        this(phone, usageMonitor, imsSMSDispatcher);
    }

    public String getFormat() {
        return "3gpp2";
    }

    public void sendStatusReportMessage(SmsMessage sms) {
        Rlog.d(TAG, "sending EVENT_HANDLE_STATUS_REPORT message");
        sendMessage(obtainMessage(10, sms));
    }

    protected void handleStatusReport(Object o) {
        if (o instanceof SmsMessage) {
            Rlog.d(TAG, "calling handleCdmaStatusReport()");
            handleCdmaStatusReport((SmsMessage) o);
            return;
        }
        Rlog.e(TAG, "handleStatusReport() called for object type " + o.getClass().getName());
    }

    private void handleCdmaStatusReport(SmsMessage sms) {
        Rlog.d(TAG, "handleCdmaStatusReport, sms.mMessageRef: " + sms.mMessageRef);
        int count = this.deliveryPendingList.size();
        for (int i = 0; i < count; i++) {
            SmsTracker tracker = (SmsTracker) this.deliveryPendingList.get(i);
            Rlog.d(TAG, "tracker.mMessageRef: " + tracker.mMessageRef);
            if (tracker.mMessageRef == sms.mMessageRef) {
                this.deliveryPendingList.remove(i);
                tracker.updateSentMessageStatus(this.mContext, 0);
                PendingIntent intent = tracker.mDeliveryIntent;
                Intent fillIn = new Intent();
                fillIn.putExtra("pdu", sms.getPdu());
                fillIn.putExtra("format", getFormat());
                try {
                    intent.send(this.mContext, -1, fillIn);
                    return;
                } catch (CanceledException e) {
                    return;
                }
            }
        }
    }

    public void sendData(String destAddr, String scAddr, int destPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        SubmitPdu pdu = SmsMessage.getSubmitPdu(scAddr, destAddr, destPort, data, deliveryIntent != null);
        if (pdu != null) {
            SmsTracker tracker = getSmsTracker(getSmsTrackerMap(destAddr, scAddr, destPort, data, pdu), sentIntent, deliveryIntent, getFormat(), null, false, null, false, true);
            String carrierPackage = getCarrierAppPackageName();
            if (carrierPackage != null) {
                Rlog.d(TAG, "Found carrier package.");
                SmsSender dataSmsSender = new DataSmsSender(tracker);
                dataSmsSender.sendSmsByCarrierApp(carrierPackage, new SmsSenderCallback(dataSmsSender));
                return;
            }
            Rlog.v(TAG, "No carrier package.");
            sendSubmitPdu(tracker);
            return;
        }
        Rlog.e(TAG, "CdmaSMSDispatcher.sendData(): getSubmitPdu() returned null");
        if (sentIntent != null) {
            try {
                sentIntent.send(1);
            } catch (CanceledException e) {
                Rlog.e(TAG, "Intent has been canceled!");
            }
        }
    }

    public void sendText(String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, Uri messageUri, String callingPkg, boolean persistMessage) {
        SubmitPduBase pdu = SmsMessage.getSubmitPdu(scAddr, destAddr, text, deliveryIntent != null, null);
        if (pdu != null) {
            SmsTracker tracker = getSmsTracker(getSmsTrackerMap(destAddr, scAddr, text, pdu), sentIntent, deliveryIntent, getFormat(), messageUri, false, text, true, persistMessage);
            String carrierPackage = getCarrierAppPackageName();
            if (carrierPackage != null) {
                Rlog.d(TAG, "Found carrier package.");
                SmsSender textSmsSender = new TextSmsSender(tracker);
                textSmsSender.sendSmsByCarrierApp(carrierPackage, new SmsSenderCallback(textSmsSender));
                return;
            }
            Rlog.v(TAG, "No carrier package.");
            sendSubmitPdu(tracker);
            return;
        }
        Rlog.e(TAG, "CdmaSMSDispatcher.sendText(): getSubmitPdu() returned null");
        if (sentIntent != null) {
            try {
                sentIntent.send(1);
            } catch (CanceledException e) {
                Rlog.e(TAG, "Intent has been canceled!");
            }
        }
    }

    protected void injectSmsPdu(byte[] pdu, String format, PendingIntent receivedIntent) {
        throw new IllegalStateException("This method must be called only on ImsSMSDispatcher");
    }

    protected TextEncodingDetails calculateLength(CharSequence messageBody, boolean use7bitOnly) {
        return SmsMessage.calculateLength(messageBody, use7bitOnly, false);
    }

    protected SmsTracker getNewSubmitPduTracker(String destinationAddress, String scAddress, String message, SmsHeader smsHeader, int encoding, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean lastPart, AtomicInteger unsentPartCount, AtomicBoolean anyPartFailed, Uri messageUri, String fullMessageText) {
        UserData uData = new UserData();
        uData.payloadStr = message;
        uData.userDataHeader = smsHeader;
        if (encoding == 1) {
            uData.msgEncoding = 2;
        } else {
            uData.msgEncoding = 4;
        }
        uData.msgEncodingSet = true;
        if (deliveryIntent == null) {
            lastPart = false;
        }
        return getSmsTracker(getSmsTrackerMap(destinationAddress, scAddress, message, SmsMessage.getSubmitPdu(destinationAddress, uData, lastPart)), sentIntent, deliveryIntent, getFormat(), unsentPartCount, anyPartFailed, messageUri, smsHeader, false, fullMessageText, true, true);
    }

    protected void sendSubmitPdu(SmsTracker tracker) {
        if (this.mPhone.isInEcm()) {
            Rlog.d(TAG, "Block SMS in Emergency Callback mode");
            tracker.onFailed(this.mContext, 4, 0);
            return;
        }
        sendRawPdu(tracker);
    }

    public void sendSms(SmsTracker tracker) {
        Rlog.d(TAG, "sendSms:  isIms()=" + isIms() + " mRetryCount=" + tracker.mRetryCount + " mImsRetry=" + tracker.mImsRetry + " mMessageRef=" + tracker.mMessageRef + " SS=" + this.mPhone.getServiceState().getState());
        sendSmsByPstn(tracker);
    }

    protected void sendSmsByPstn(SmsTracker tracker) {
        int ss = this.mPhone.getServiceState().getState();
        if (isIms() || ss == 0) {
            Message reply = obtainMessage(2, tracker);
            byte[] pdu = (byte[]) tracker.getData().get("pdu");
            int currentDataNetwork = this.mPhone.getServiceState().getDataNetworkType();
            boolean imsSmsDisabled = ((currentDataNetwork == 14 || (ServiceState.isLte(currentDataNetwork) && (this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed() ^ 1) != 0)) && this.mPhone.getServiceState().getVoiceNetworkType() == 7) ? ((GsmCdmaPhone) this.mPhone).mCT.mState != State.IDLE : false;
            if ((tracker.mImsRetry != 0 || (isIms() ^ 1) == 0) && !imsSmsDisabled) {
                this.mCi.sendImsCdmaSms(pdu, tracker.mImsRetry, tracker.mMessageRef, reply);
                tracker.mImsRetry++;
            } else {
                this.mCi.sendCdmaSms(pdu, reply);
            }
            return;
        }
        tracker.onFailed(this.mContext, SMSDispatcher.getNotInServiceError(ss), 0);
    }

    public void dispatchCTAutoRegSmsPdus(SmsMessageBase smsb) {
        byte[][] pdus = new byte[][]{((SmsMessage) smsb).getUserData()};
        Intent intent = new Intent("android.provider.Telephony.CT_AUTO_REG_RECV_CONFIRM_ACK");
        intent.putExtra("pdus", pdus);
        intent.putExtra("CdmaSubscription", getSubId());
        intent.addFlags(134217728);
        this.mContext.sendOrderedBroadcast(intent, "android.permission.RECEIVE_SMS", null, this, -1, null, null);
        Rlog.d(TAG, "dispatchCTAutoRegSmsPdus end. Broadcast send to apk!");
    }
}
