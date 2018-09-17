package com.android.internal.telephony.gsm;

import android.os.Handler;
import android.os.Message;
import android.telephony.Rlog;
import android.widget.Toast;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.ImsSMSDispatcher;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SMSDispatcher.SmsTracker;
import com.android.internal.telephony.SMSDispatcherUtils;
import com.android.internal.telephony.SmsStorageMonitor;
import com.android.internal.telephony.SmsUsageMonitor;
import java.util.ArrayList;
import java.util.List;

public class HwGsmSMSDispatcher extends GsmSMSDispatcher {
    protected static final int EVENT_SMS_SENDING_TIMEOUT = 1000;
    private static final String PHONE_PACKAGE = "com.android.phone";
    private static final int POP_TOAST = 1;
    private static final int RESULT_ERROR_LIMIT_EXCEEDED = 5;
    protected static final int SMS_SENDING_TIMOUEOUT = 60000;
    private static final String TAG = "HwGsmSMSDispatcher";
    private static final int TRACKER_FAIL_ERRORCODE = 0;
    private Handler mToastHanlder = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Toast.makeText(HwGsmSMSDispatcher.this.mContext, 33685944, 1).show();
                    return;
                default:
                    return;
            }
        }
    };
    protected List<SmsTracker> mTrackerList = new ArrayList();

    public HwGsmSMSDispatcher(Phone phone, SmsStorageMonitor storageMonitor, SmsUsageMonitor usageMonitor, ImsSMSDispatcher imsSMSDispatcher, GsmInboundSmsHandler gsmInboundSmsHandler) {
        super(phone, storageMonitor, usageMonitor, imsSMSDispatcher, gsmInboundSmsHandler);
    }

    protected void sendSms(SmsTracker tracker) {
        Rlog.d(TAG, "sendSms: tracker is:" + tracker);
        int ss = this.mPhone.getServiceState().getState();
        if (tracker != null && (isIms() ^ 1) != 0 && ss != 0) {
            tracker.onFailed(this.mContext, getNotInServiceError(ss), 0);
        } else if (HwTelephonyFactory.getHwInnerSmsManager().isLimitNumOfSmsEnabled(true) && HwTelephonyFactory.getHwInnerSmsManager().isExceedSMSLimit(this.mContext, true) && (PHONE_PACKAGE.equals(tracker.mAppInfo.packageName) ^ 1) != 0) {
            tracker.onFailed(this.mContext, 5, 0);
            if (this.mToastHanlder != null) {
                this.mToastHanlder.sendEmptyMessage(1);
            }
        } else {
            this.mTrackerList.add(tracker);
            Rlog.d(TAG, "sendSms: mTrackerList = " + this.mTrackerList.size());
            if (1 == this.mTrackerList.size() && sendSmsImmediately(tracker)) {
                removeMessages(1000);
                sendMessageDelayed(obtainMessage(1000, tracker), 60000);
            }
        }
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 2:
                Rlog.d(TAG, "handleSendComplete Remove EVENT_SMS_SENDING_TIMEOUT");
                removeMessages(1000);
                super.handleMessage(msg);
                return;
            case 1000:
                SmsTracker tracker = msg.obj;
                Rlog.d(TAG, "EVENT_SMS_SENDING_TIMEOUT, failed tracker is: " + tracker + "blocked size is: " + this.mTrackerList.size());
                if (this.mTrackerList.size() > 0 && this.mTrackerList.remove(tracker)) {
                    Rlog.e(TAG, "EVENT_SMS_SENDING_TIMEOUT, failed tracker is: " + tracker + "blocked size is: " + this.mTrackerList.size());
                    removeMessages(1000);
                    if (this.mTrackerList.size() > 0 && sendSmsImmediately((SmsTracker) this.mTrackerList.get(0))) {
                        sendMessageDelayed(obtainMessage(1000, this.mTrackerList.get(0)), 60000);
                        return;
                    }
                    return;
                }
                return;
            default:
                if (SMSDispatcherUtils.getEventSendRetry() == msg.what) {
                    Rlog.d(TAG, "SMS send retry..");
                    sendMessageDelayed(obtainMessage(1000, (SmsTracker) msg.obj), 60000);
                }
                super.handleMessage(msg);
                return;
        }
    }

    protected boolean sendSmsImmediately(SmsTracker tracker) {
        if (tracker == null || isCdmaAndNoIms(tracker)) {
            return false;
        }
        super.sendSms(tracker);
        triggerSendSmsOverLoadCheck();
        return true;
    }

    private boolean isCdmaAndNoIms(SmsTracker tracker) {
        int ss = this.mPhone.getServiceState().getState();
        int currentPhoneType = this.mPhone.getPhoneType();
        if (tracker == null || (isIms() ^ 1) == 0 || ss == 0 || currentPhoneType != 2) {
            return false;
        }
        Rlog.d(TAG, "sendSms fail: is not Ims and not in Service, so clear tracker list and retransmission sms with CdmaDispatcher");
        tracker.onFailed(this.mContext, getNotInServiceError(ss), 0);
        this.mTrackerList.clear();
        return true;
    }

    protected void sendSmsSendingTimeOutMessageDelayed(SmsTracker tracker) {
        Rlog.d(TAG, "handleSendComplete: tracker is:" + tracker);
        if (!isCdmaAndNoIms(tracker)) {
            if (HwTelephonyFactory.getHwInnerSmsManager().isLimitNumOfSmsEnabled(true) && HwTelephonyFactory.getHwInnerSmsManager().isExceedSMSLimit(this.mContext, true) && (PHONE_PACKAGE.equals(tracker.mAppInfo.packageName) ^ 1) != 0) {
                this.mTrackerList.remove(tracker);
                if (this.mTrackerList.size() > 0 && this.mToastHanlder != null) {
                    ((SmsTracker) this.mTrackerList.get(0)).onFailed(this.mContext, 5, 0);
                    this.mToastHanlder.sendEmptyMessage(1);
                }
                int trackerListSize = this.mTrackerList.size();
                for (int i = 0; i < trackerListSize; i++) {
                    ((SmsTracker) this.mTrackerList.get(i)).onFailed(this.mContext, 5, 0);
                }
                this.mTrackerList.clear();
                return;
            }
            if (this.mTrackerList.remove(tracker) && this.mTrackerList.size() > 0 && sendSmsImmediately((SmsTracker) this.mTrackerList.get(0))) {
                sendMessageDelayed(obtainMessage(1000, this.mTrackerList.get(0)), 60000);
            }
        }
    }
}
