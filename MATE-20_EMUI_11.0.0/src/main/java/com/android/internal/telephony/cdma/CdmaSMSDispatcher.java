package com.android.internal.telephony.cdma;

import android.annotation.UnsupportedAppUsage;
import android.os.Message;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.SMSDispatcher;
import com.android.internal.telephony.SmsDispatchersController;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.util.SMSDispatcherUtil;
import com.huawei.internal.telephony.SMSDispatcherEx;

public final class CdmaSMSDispatcher extends SMSDispatcher {
    private static final String TAG = "CdmaSMSDispatcher";
    private static final boolean VDBG = true;

    public CdmaSMSDispatcher(Phone phone, SmsDispatchersController smsDispatchersController) {
        super(phone, smsDispatchersController);
        Rlog.d(TAG, "CdmaSMSDispatcher created");
    }

    @Override // com.android.internal.telephony.SMSDispatcher
    @UnsupportedAppUsage
    public String getFormat() {
        return "3gpp2";
    }

    public void sendStatusReportMessage(SmsMessage sms) {
        Rlog.d(TAG, "sending EVENT_HANDLE_STATUS_REPORT message");
        sendMessage(obtainMessage(10, sms));
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.SMSDispatcher
    public void handleStatusReport(Object o) {
        if (o instanceof SmsMessage) {
            Rlog.d(TAG, "calling handleCdmaStatusReport()");
            handleCdmaStatusReport((SmsMessage) o);
            return;
        }
        Rlog.e(TAG, "handleStatusReport() called for object type " + o.getClass().getName());
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.SMSDispatcher
    public boolean shouldBlockSmsForEcbm() {
        return this.mPhone.isInEcm() && isCdmaMo() && !isIms();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.SMSDispatcher
    public SmsMessageBase.SubmitPduBase getSubmitPdu(String scAddr, String destAddr, String message, boolean statusReportRequested, SmsHeader smsHeader, int priority, int validityPeriod) {
        return SMSDispatcherUtil.getSubmitPduCdma(scAddr, destAddr, message, statusReportRequested, smsHeader, priority);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.SMSDispatcher
    public SmsMessageBase.SubmitPduBase getSubmitPdu(String scAddr, String destAddr, int destPort, byte[] message, boolean statusReportRequested) {
        return SMSDispatcherUtil.getSubmitPduCdma(scAddr, destAddr, destPort, message, statusReportRequested);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.SMSDispatcher
    public GsmAlphabet.TextEncodingDetails calculateLength(CharSequence messageBody, boolean use7bitOnly) {
        return SMSDispatcherUtil.calculateLengthCdma(messageBody, use7bitOnly);
    }

    @UnsupportedAppUsage
    private void handleCdmaStatusReport(SmsMessage sms) {
        int count = this.deliveryPendingList.size();
        for (int i = 0; i < count; i++) {
            SMSDispatcher.SmsTracker tracker = (SMSDispatcher.SmsTracker) this.deliveryPendingList.get(i);
            if (tracker.mMessageRef == sms.mMessageRef) {
                if (((Boolean) this.mSmsDispatchersController.handleSmsStatusReport(tracker, getFormat(), sms.getPdu()).second).booleanValue()) {
                    this.deliveryPendingList.remove(i);
                    return;
                } else {
                    return;
                }
            }
        }
    }

    @Override // com.android.internal.telephony.SMSDispatcher
    public void sendSms(SMSDispatcher.SmsTracker tracker) {
        SMSDispatcherEx.SmsTrackerEx smsTrackerEx = getSmsTrackerEx(tracker);
        if (this.mHwCdmaSmsDispatcher.isNeedToSendSms(smsTrackerEx)) {
            Rlog.i(TAG, "sendSms:  isIms()=" + isIms() + " mRetryCount=" + tracker.mRetryCount + " mImsRetry=" + tracker.mImsRetry + " mMessageRef=" + tracker.mMessageRef + " mUsesImsServiceForIms=" + tracker.mUsesImsServiceForIms + " SS=" + this.mPhone.getServiceState().getState());
            int ss = this.mPhone.getServiceState().getState();
            boolean imsSmsDisabled = false;
            if (isIms() || ss == 0) {
                Message reply = obtainMessage(2, tracker);
                byte[] pdu = (byte[]) tracker.getData().get("pdu");
                int currentDataNetwork = this.mPhone.getServiceState().getDataNetworkType();
                if ((currentDataNetwork == 14 || (ServiceState.isLte(currentDataNetwork) && !this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed())) && this.mPhone.getServiceState().getVoiceNetworkType() == 7 && ((GsmCdmaPhone) this.mPhone).mCT.mState != PhoneConstants.State.IDLE) {
                    imsSmsDisabled = true;
                }
                if ((tracker.mImsRetry == 0 && !isIms()) || imsSmsDisabled || tracker.mUsesImsServiceForIms) {
                    this.mCi.sendCdmaSms(pdu, reply);
                } else if (this.mHwCdmaSmsDispatcher.isCdmaIms(smsTrackerEx)) {
                    this.mHwCdmaSmsDispatcher.handleSendSmsAfter(smsTrackerEx);
                    return;
                } else {
                    this.mCi.sendImsCdmaSms(pdu, tracker.mImsRetry, tracker.mMessageRef, reply);
                    tracker.mImsRetry++;
                }
                this.mHwCdmaSmsDispatcher.handleSendSmsAfter(smsTrackerEx);
                return;
            }
            tracker.onFailed(this.mContext, getNotInServiceError(ss), 0);
            this.mHwCdmaSmsDispatcher.handleSendSmsAfter(smsTrackerEx);
        }
    }
}
