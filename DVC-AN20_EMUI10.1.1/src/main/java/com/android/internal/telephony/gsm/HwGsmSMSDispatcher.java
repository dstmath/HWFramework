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
        /* class com.android.internal.telephony.gsm.HwGsmSMSDispatcher.AnonymousClass1 */

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
        Rlog.i(TAG, "sendSms: tracker is:" + tracker);
        int ss = this.mPhone.getServiceState().getState();
        if (tracker != null && !isIms() && ss != 0) {
            tracker.onFailed(this.mContext, getNotInServiceError(ss), 0);
        } else if (tracker == null || !HwTelephonyFactory.getHwInnerSmsManager().isLimitNumOfSmsEnabled(true) || !HwTelephonyFactory.getHwInnerSmsManager().isExceedSMSLimit(this.mContext, true) || PHONE_PACKAGE.equals(tracker.mAppInfo.packageName)) {
            this.mTrackerList.add(tracker);
            Rlog.d(TAG, "sendSms: mTrackerList = " + this.mTrackerList.size());
            if (this.mTrackerList.size() == 1 && sendSmsImmediately(tracker)) {
                removeMessages(EVENT_SMS_SENDING_TIMEOUT);
                sendMessageDelayed(obtainMessage(EVENT_SMS_SENDING_TIMEOUT, tracker), (long) SMS_SENDING_TIMOUEOUT);
            }
        } else {
            tracker.onFailed(this.mContext, 5, 0);
            Handler handler = this.mToastHanlder;
            if (handler != null) {
                handler.sendEmptyMessage(1);
            }
        }
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i == 2) {
            Rlog.i(TAG, "handleSendComplete Remove EVENT_SMS_SENDING_TIMEOUT");
            removeMessages(EVENT_SMS_SENDING_TIMEOUT);
            HwGsmSMSDispatcher.super.handleMessage(msg);
        } else if (i != EVENT_SMS_SENDING_TIMEOUT) {
            if (SMSDispatcherUtils.getEventSendRetry() == msg.what) {
                Rlog.i(TAG, "SMS send retry..");
                sendMessageDelayed(obtainMessage(EVENT_SMS_SENDING_TIMEOUT, (SMSDispatcher.SmsTracker) msg.obj), (long) SMS_SENDING_TIMOUEOUT);
            }
            HwGsmSMSDispatcher.super.handleMessage(msg);
        } else {
            SMSDispatcher.SmsTracker tracker = (SMSDispatcher.SmsTracker) msg.obj;
            Rlog.i(TAG, "EVENT_SMS_SENDING_TIMEOUT, failed tracker is: " + tracker + "blocked size is: " + this.mTrackerList.size());
            if (this.mTrackerList.size() > 0 && this.mTrackerList.remove(tracker)) {
                Rlog.e(TAG, "EVENT_SMS_SENDING_TIMEOUT, failed tracker is: " + tracker + "blocked size is: " + this.mTrackerList.size());
                removeMessages(EVENT_SMS_SENDING_TIMEOUT);
                if (this.mTrackerList.size() > 0 && sendSmsImmediately(this.mTrackerList.get(0))) {
                    sendMessageDelayed(obtainMessage(EVENT_SMS_SENDING_TIMEOUT, this.mTrackerList.get(0)), (long) SMS_SENDING_TIMOUEOUT);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean checkCustIgnoreShortCodeTips() {
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
        for (String str : delPromtHplmns.split(",")) {
            if (hplmn.equals(str)) {
                return true;
            }
        }
        return false;
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
        if (isIms() || ss == 0 || currentPhoneType != 2 || tracker == null) {
            return false;
        }
        Rlog.i(TAG, "sendSms fail: is not Ims and not in Service, so clear tracker list and retransmission sms with CdmaDispatcher");
        tracker.onFailed(this.mContext, getNotInServiceError(ss), 0);
        this.mTrackerList.clear();
        return true;
    }

    /* access modifiers changed from: protected */
    public void sendSmsSendingTimeOutMessageDelayed(SMSDispatcher.SmsTracker tracker) {
        Rlog.i(TAG, "handleSendComplete: tracker is:" + tracker);
        if (!isCdmaAndNoIms(tracker)) {
            if (HwTelephonyFactory.getHwInnerSmsManager().isLimitNumOfSmsEnabled(true) && HwTelephonyFactory.getHwInnerSmsManager().isExceedSMSLimit(this.mContext, true) && !PHONE_PACKAGE.equals(tracker.mAppInfo.packageName)) {
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
            } else if (this.mTrackerList.remove(tracker) && this.mTrackerList.size() > 0 && sendSmsImmediately(this.mTrackerList.get(0))) {
                sendMessageDelayed(obtainMessage(EVENT_SMS_SENDING_TIMEOUT, this.mTrackerList.get(0)), (long) SMS_SENDING_TIMOUEOUT);
            }
        }
    }
}
