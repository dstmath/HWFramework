package com.android.internal.telephony.cdma;

import android.os.Handler;
import android.os.Message;
import android.telephony.Rlog;
import android.widget.Toast;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SMSDispatcher;
import com.android.internal.telephony.SMSDispatcherUtils;
import com.android.internal.telephony.SmsDispatchersController;
import java.util.ArrayList;
import java.util.List;

public class HwCdmaSMSDispatcher extends CdmaSMSDispatcher {
    protected static final int EVENT_SMS_SENDING_TIMEOUT = 1000;
    private static final int POP_TOAST = 1;
    private static final int RESULT_ERROR_LIMIT_EXCEEDED = 5;
    protected static final int SMS_SENDING_TIMOUEOUT = (!HuaweiTelephonyConfigs.isHisiPlatform() ? 60000 : 210000);
    private static final String TAG = "HwCdmaSMSDispatcher";
    private static final int TRACKER_FAIL_ERRORCODE = 0;
    private Handler mToastHanlder = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                Toast.makeText(HwCdmaSMSDispatcher.this.mContext, 33685944, 1).show();
            }
        }
    };
    protected List<SMSDispatcher.SmsTracker> mTrackerList = new ArrayList();

    public HwCdmaSMSDispatcher(Phone phone, SmsDispatchersController smsDispatchersController) {
        super(phone, smsDispatchersController);
        Rlog.d(TAG, "HwCdmaSMSDispatcher created");
    }

    public void sendSms(SMSDispatcher.SmsTracker tracker) {
        Rlog.d(TAG, "sendSms: tracker is:" + tracker);
        int ss = this.mPhone.getServiceState().getState();
        if (tracker != null && !isIms() && ss != 0) {
            tracker.onFailed(this.mContext, getNotInServiceError(ss), 0);
        } else if (!HwTelephonyFactory.getHwInnerSmsManager().isLimitNumOfSmsEnabled(true) || !HwTelephonyFactory.getHwInnerSmsManager().isExceedSMSLimit(this.mContext, true)) {
            this.mTrackerList.add(tracker);
            Rlog.d(TAG, "sendSms: mTrackerList = " + this.mTrackerList.size());
            if (1 == this.mTrackerList.size() && sendSmsImmediately(tracker)) {
                removeMessages(1000);
                sendMessageDelayed(obtainMessage(1000, tracker), (long) SMS_SENDING_TIMOUEOUT);
            }
        } else {
            tracker.onFailed(this.mContext, 5, 0);
            if (this.mToastHanlder != null) {
                this.mToastHanlder.sendEmptyMessage(1);
            }
        }
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i == 2) {
            Rlog.d(TAG, "handleSendComplete Remove EVENT_SMS_SENDING_TIMEOUT");
            removeMessages(1000);
            HwCdmaSMSDispatcher.super.handleMessage(msg);
        } else if (i != 1000) {
            if (SMSDispatcherUtils.getEventSendRetry() == msg.what) {
                Rlog.d(TAG, "SMS send retry..");
                sendMessageDelayed(obtainMessage(1000, (SMSDispatcher.SmsTracker) msg.obj), (long) SMS_SENDING_TIMOUEOUT);
            }
            HwCdmaSMSDispatcher.super.handleMessage(msg);
        } else {
            SMSDispatcher.SmsTracker tracker = (SMSDispatcher.SmsTracker) msg.obj;
            Rlog.d(TAG, "EVENT_SMS_SENDING_TIMEOUT, failed tracker is: " + tracker + "blocked size is: " + this.mTrackerList.size());
            if (this.mTrackerList.size() > 0 && this.mTrackerList.remove(tracker)) {
                Rlog.e(TAG, "EVENT_SMS_SENDING_TIMEOUT, failed tracker is: " + tracker + "blocked size is: " + this.mTrackerList.size());
                removeMessages(1000);
                if (this.mTrackerList.size() > 0 && sendSmsImmediately(this.mTrackerList.get(0))) {
                    sendMessageDelayed(obtainMessage(1000, this.mTrackerList.get(0)), (long) SMS_SENDING_TIMOUEOUT);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean sendSmsImmediately(SMSDispatcher.SmsTracker tracker) {
        if (tracker == null || isCdmaIms(tracker)) {
            return false;
        }
        HwCdmaSMSDispatcher.super.sendSms(tracker);
        HwTelephonyFactory.getHwInnerSmsManager().triggerSendSmsOverLoadCheck(this);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isViaAndCdma() {
        Rlog.d(TAG, "isViaAndCdma: isVia = " + HwModemCapability.isCapabilitySupport(14));
        return HwModemCapability.isCapabilitySupport(14);
    }

    private boolean isCdmaIms(SMSDispatcher.SmsTracker tracker) {
        int currentPhoneType = this.mPhone.getPhoneType();
        if (tracker == null || !isIms() || currentPhoneType != 2) {
            return false;
        }
        Rlog.d(TAG, "sendSms retry fail: upgrade to Ims, so clear tracker list and retransmission sms with GsmDispatcher");
        tracker.onFailed(this.mContext, getNotInServiceError(this.mPhone.getServiceState().getState()), 0);
        this.mTrackerList.clear();
        return true;
    }

    /* access modifiers changed from: protected */
    public void sendSmsSendingTimeOutMessageDelayed(SMSDispatcher.SmsTracker tracker) {
        Rlog.d(TAG, "handleSendComplete: tracker is:" + tracker);
        if (!isCdmaIms(tracker)) {
            if (!HwTelephonyFactory.getHwInnerSmsManager().isLimitNumOfSmsEnabled(true) || !HwTelephonyFactory.getHwInnerSmsManager().isExceedSMSLimit(this.mContext, true)) {
                if (this.mTrackerList.remove(tracker) && this.mTrackerList.size() > 0 && sendSmsImmediately(this.mTrackerList.get(0))) {
                    sendMessageDelayed(obtainMessage(1000, this.mTrackerList.get(0)), (long) SMS_SENDING_TIMOUEOUT);
                }
                return;
            }
            this.mTrackerList.remove(tracker);
            if (this.mTrackerList.size() > 0 && this.mToastHanlder != null) {
                this.mTrackerList.get(0).onFailed(this.mContext, 5, 0);
                this.mToastHanlder.sendEmptyMessage(1);
            }
            int trackerSize = this.mTrackerList.size();
            for (int i = 0; i < trackerSize; i++) {
                this.mTrackerList.get(i).onFailed(this.mContext, 5, 0);
            }
            this.mTrackerList.clear();
        }
    }
}
