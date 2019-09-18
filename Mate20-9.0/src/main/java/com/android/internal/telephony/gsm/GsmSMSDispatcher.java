package com.android.internal.telephony.gsm;

import android.os.AsyncResult;
import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SMSDispatcher;
import com.android.internal.telephony.SmsDispatchersController;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.util.SMSDispatcherUtil;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public abstract class GsmSMSDispatcher extends SMSDispatcher {
    private static final int EVENT_NEW_SMS_STATUS_REPORT = 100;
    private static final String TAG = "GsmSMSDispatcher";
    private GsmInboundSmsHandler mGsmInboundSmsHandler;
    private AtomicReference<IccRecords> mIccRecords = new AtomicReference<>();
    private AtomicReference<UiccCardApplication> mUiccApplication = new AtomicReference<>();
    protected UiccController mUiccController = null;

    public GsmSMSDispatcher(Phone phone, SmsDispatchersController smsDispatchersController, GsmInboundSmsHandler gsmInboundSmsHandler) {
        super(phone, smsDispatchersController);
        this.mCi.setOnSmsStatus(this, 100, null);
        this.mGsmInboundSmsHandler = gsmInboundSmsHandler;
        this.mUiccController = UiccController.getInstance();
        this.mUiccController.registerForIccChanged(this, 15, null);
        Rlog.d(TAG, "GsmSMSDispatcher created");
    }

    public void dispose() {
        super.dispose();
        this.mCi.unSetOnSmsStatus(this);
        this.mUiccController.unregisterForIccChanged(this);
    }

    /* access modifiers changed from: protected */
    public String getFormat() {
        return "3gpp";
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i != 100) {
            switch (i) {
                case 14:
                    this.mGsmInboundSmsHandler.sendMessage(1, msg.obj);
                    return;
                case 15:
                    onUpdateIccAvailability();
                    return;
                default:
                    super.handleMessage(msg);
                    return;
            }
        } else {
            handleStatusReport((AsyncResult) msg.obj);
        }
    }

    /* access modifiers changed from: protected */
    public boolean shouldBlockSmsForEcbm() {
        return false;
    }

    /* access modifiers changed from: protected */
    public SmsMessageBase.SubmitPduBase getSubmitPdu(String scAddr, String destAddr, String message, boolean statusReportRequested, SmsHeader smsHeader, int priority, int validityPeriod) {
        return SMSDispatcherUtil.getSubmitPduGsm(scAddr, destAddr, message, statusReportRequested, validityPeriod);
    }

    /* access modifiers changed from: protected */
    public SmsMessageBase.SubmitPduBase getSubmitPdu(String scAddr, String destAddr, int destPort, byte[] message, boolean statusReportRequested) {
        return SMSDispatcherUtil.getSubmitPduGsm(scAddr, destAddr, destPort, message, statusReportRequested);
    }

    /* access modifiers changed from: protected */
    public GsmAlphabet.TextEncodingDetails calculateLength(CharSequence messageBody, boolean use7bitOnly) {
        return SMSDispatcherUtil.calculateLengthGsm(messageBody, use7bitOnly);
    }

    private void handleStatusReport(AsyncResult ar) {
        byte[] pdu = (byte[]) ar.result;
        SmsMessage sms = SmsMessage.newFromCDS(pdu);
        if (sms != null) {
            int messageRef = sms.mMessageRef;
            int i = 0;
            int count = this.deliveryPendingList.size();
            while (true) {
                if (i >= count) {
                    break;
                }
                SMSDispatcher.SmsTracker tracker = (SMSDispatcher.SmsTracker) this.deliveryPendingList.get(i);
                if (tracker.mMessageRef != messageRef) {
                    i++;
                } else if (((Boolean) this.mSmsDispatchersController.handleSmsStatusReport(tracker, getFormat(), pdu).second).booleanValue()) {
                    this.deliveryPendingList.remove(i);
                }
            }
        }
        this.mCi.acknowledgeLastIncomingGsmSms(true, 1, null);
    }

    /* access modifiers changed from: protected */
    public void sendSms(SMSDispatcher.SmsTracker tracker) {
        HashMap<String, Object> map = tracker.getData();
        byte[] pdu = (byte[]) map.get("pdu");
        if (tracker.mRetryCount > 0) {
            Rlog.d(TAG, "sendSms:  mRetryCount=" + tracker.mRetryCount + " mMessageRef=" + tracker.mMessageRef + " SS=" + this.mPhone.getServiceState().getState());
            if ((pdu[0] & 1) == 1) {
                pdu[0] = (byte) (pdu[0] | 4);
                pdu[1] = (byte) tracker.mMessageRef;
            }
        }
        Rlog.d(TAG, "sendSms:  isIms()=" + isIms() + " mRetryCount=" + tracker.mRetryCount + " mImsRetry=" + tracker.mImsRetry + " mMessageRef=" + tracker.mMessageRef + " mUsesImsServiceForIms=" + tracker.mUsesImsServiceForIms + " SS=" + this.mPhone.getServiceState().getState());
        int ss = this.mPhone.getServiceState().getState();
        if (isIms() || ss == 0) {
            byte[] smsc = (byte[]) map.get("smsc");
            Message reply = obtainMessage(2, tracker);
            if ((tracker.mImsRetry != 0 || isIms()) && !tracker.mUsesImsServiceForIms) {
                this.mCi.sendImsGsmSms(IccUtils.bytesToHexString(smsc), IccUtils.bytesToHexString(pdu), tracker.mImsRetry, tracker.mMessageRef, reply);
                tracker.mImsRetry++;
            } else if (tracker.mRetryCount != 0 || !tracker.mExpectMore) {
                this.mCi.sendSMS(IccUtils.bytesToHexString(smsc), IccUtils.bytesToHexString(pdu), reply);
            } else {
                this.mCi.sendSMSExpectMore(IccUtils.bytesToHexString(smsc), IccUtils.bytesToHexString(pdu), reply);
            }
            return;
        }
        tracker.onFailed(this.mContext, getNotInServiceError(ss), 0);
    }

    /* access modifiers changed from: protected */
    public UiccCardApplication getUiccCardApplication() {
        Rlog.d(TAG, "GsmSMSDispatcher: subId = " + this.mPhone.getSubId() + " slotId = " + this.mPhone.getPhoneId());
        return this.mUiccController.getUiccCardApplication(this.mPhone.getPhoneId(), 1);
    }

    private void onUpdateIccAvailability() {
        if (this.mUiccController != null) {
            UiccCardApplication newUiccApplication = getUiccCardApplication();
            UiccCardApplication app = this.mUiccApplication.get();
            if (app != newUiccApplication) {
                if (app != null) {
                    Rlog.d(TAG, "Removing stale icc objects.");
                    if (this.mIccRecords.get() != null) {
                        this.mIccRecords.get().unregisterForNewSms(this);
                    }
                    this.mIccRecords.set(null);
                    this.mUiccApplication.set(null);
                }
                if (newUiccApplication != null) {
                    Rlog.d(TAG, "New Uicc application found");
                    this.mUiccApplication.set(newUiccApplication);
                    this.mIccRecords.set(newUiccApplication.getIccRecords());
                    if (this.mIccRecords.get() != null) {
                        this.mIccRecords.get().registerForNewSms(this, 14, null);
                    }
                }
            }
        }
    }
}
