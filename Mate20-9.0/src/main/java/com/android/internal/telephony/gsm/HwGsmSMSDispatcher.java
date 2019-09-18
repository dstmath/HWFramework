package com.android.internal.telephony.gsm;

import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.Rlog;
import android.text.TextUtils;
import android.widget.Toast;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SMSDispatcher;
import com.android.internal.telephony.SMSDispatcherUtils;
import com.android.internal.telephony.SmsDispatchersController;
import com.android.internal.telephony.uicc.IccRecords;
import java.util.ArrayList;
import java.util.List;

public class HwGsmSMSDispatcher extends GsmSMSDispatcher {
    protected static final int EVENT_SMS_SENDING_TIMEOUT = 1000;
    private static final String PHONE_PACKAGE = "com.android.phone";
    private static final int POP_TOAST = 1;
    private static final int RESULT_ERROR_LIMIT_EXCEEDED = 5;
    protected static final int SMS_SENDING_TIMOUEOUT = (!HuaweiTelephonyConfigs.isHisiPlatform() ? 60000 : 210000);
    private static final String TAG = "HwGsmSMSDispatcher";
    private static final int TRACKER_FAIL_ERRORCODE = 0;
    private Handler mToastHanlder = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                Toast.makeText(HwGsmSMSDispatcher.this.mContext, 33685944, 1).show();
            }
        }
    };
    protected List<SMSDispatcher.SmsTracker> mTrackerList = new ArrayList();

    public HwGsmSMSDispatcher(Phone phone, SmsDispatchersController smsDispatchersController, GsmInboundSmsHandler gsmInboundSmsHandler) {
        super(phone, smsDispatchersController, gsmInboundSmsHandler);
        Rlog.d(TAG, "HwGsmSMSDispatcher created");
    }

    /* access modifiers changed from: protected */
    public void sendSms(SMSDispatcher.SmsTracker tracker) {
        Rlog.d(TAG, "sendSms: tracker is:" + tracker);
        int ss = this.mPhone.getServiceState().getState();
        if (tracker != null && !isIms() && ss != 0) {
            tracker.onFailed(this.mContext, getNotInServiceError(ss), 0);
        } else if (!HwTelephonyFactory.getHwInnerSmsManager().isLimitNumOfSmsEnabled(true) || !HwTelephonyFactory.getHwInnerSmsManager().isExceedSMSLimit(this.mContext, true) || PHONE_PACKAGE.equals(tracker.mAppInfo.packageName)) {
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
            HwGsmSMSDispatcher.super.handleMessage(msg);
        } else if (i != 1000) {
            if (SMSDispatcherUtils.getEventSendRetry() == msg.what) {
                Rlog.d(TAG, "SMS send retry..");
                sendMessageDelayed(obtainMessage(1000, (SMSDispatcher.SmsTracker) msg.obj), (long) SMS_SENDING_TIMOUEOUT);
            }
            HwGsmSMSDispatcher.super.handleMessage(msg);
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
    public boolean checkCustIgnoreShortCodeTips() {
        boolean isNeedDelPrompt = false;
        String delPromtHplmns = Settings.System.getString(this.mContext.getContentResolver(), "hw_del_prompt_hplmn");
        if (TextUtils.isEmpty(delPromtHplmns)) {
            Rlog.w(TAG, "hplmn not match");
            return false;
        }
        IccRecords r = this.mPhone.getIccRecords();
        String hplmn = r != null ? r.getOperatorNumeric() : null;
        if (TextUtils.isEmpty(hplmn)) {
            return false;
        }
        String[] homeplmns = delPromtHplmns.split(",");
        int i = 0;
        int s = homeplmns.length;
        while (true) {
            if (i >= s) {
                break;
            } else if (hplmn.equals(homeplmns[i])) {
                isNeedDelPrompt = true;
                break;
            } else {
                i++;
            }
        }
        return isNeedDelPrompt;
    }

    /* access modifiers changed from: protected */
    public boolean sendSmsImmediately(SMSDispatcher.SmsTracker tracker) {
        if (tracker == null || isCdmaAndNoIms(tracker)) {
            return false;
        }
        HwGsmSMSDispatcher.super.sendSms(tracker);
        HwTelephonyFactory.getHwInnerSmsManager().triggerSendSmsOverLoadCheck(this);
        return true;
    }

    private boolean isCdmaAndNoIms(SMSDispatcher.SmsTracker tracker) {
        int ss = this.mPhone.getServiceState().getState();
        int currentPhoneType = this.mPhone.getPhoneType();
        if (tracker == null || isIms() || ss == 0 || currentPhoneType != 2) {
            return false;
        }
        Rlog.d(TAG, "sendSms fail: is not Ims and not in Service, so clear tracker list and retransmission sms with CdmaDispatcher");
        tracker.onFailed(this.mContext, getNotInServiceError(ss), 0);
        this.mTrackerList.clear();
        return true;
    }

    /* access modifiers changed from: protected */
    public void sendSmsSendingTimeOutMessageDelayed(SMSDispatcher.SmsTracker tracker) {
        Rlog.d(TAG, "handleSendComplete: tracker is:" + tracker);
        if (!isCdmaAndNoIms(tracker)) {
            if (!HwTelephonyFactory.getHwInnerSmsManager().isLimitNumOfSmsEnabled(true) || !HwTelephonyFactory.getHwInnerSmsManager().isExceedSMSLimit(this.mContext, true) || PHONE_PACKAGE.equals(tracker.mAppInfo.packageName)) {
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
            int trackerListSize = this.mTrackerList.size();
            for (int i = 0; i < trackerListSize; i++) {
                this.mTrackerList.get(i).onFailed(this.mContext, 5, 0);
            }
            this.mTrackerList.clear();
        }
    }
}
