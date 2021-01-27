package com.android.internal.telephony.gsm;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;
import com.android.internal.telephony.DefaultHwGsmSMSDispatcherEx;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwInnerSmsManagerImpl;
import com.android.internal.telephony.ISMSDispatcherInner;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.SMSDispatcherEx;
import com.huawei.internal.telephony.uicc.IccRecordsEx;
import java.util.ArrayList;
import java.util.List;

public class HwGsmSMSDispatcherEx extends DefaultHwGsmSMSDispatcherEx {
    private static final int EVENT_SMS_SENDING_TIMEOUT = 1000;
    private static final String PHONE_PACKAGE = "com.android.phone";
    private static final int POP_TOAST = 1;
    private static final int RESULT_ERROR_LIMIT_EXCEEDED = 5;
    private static final int SMS_SENDING_TIMOUEOUT = (!HuaweiTelephonyConfigs.isHisiPlatform() ? 60000 : 210000);
    private static final String TAG = "HwGsmSMSDispatcherEx";
    private static final int TRACKER_FAIL_ERRORCODE = 0;
    private Context mContext;
    private Handler mHandler;
    private PhoneExt mPhone;
    private ISMSDispatcherInner mSmsDisPatcher;
    private Handler mToastHanlder = new Handler() {
        /* class com.android.internal.telephony.gsm.HwGsmSMSDispatcherEx.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                Toast.makeText(HwGsmSMSDispatcherEx.this.mContext, 33685944, 1).show();
            }
        }
    };
    private List<SMSDispatcherEx.SmsTrackerEx> mTrackerList = new ArrayList();

    public HwGsmSMSDispatcherEx(Context context, ISMSDispatcherInner smsDisPatcherInner, PhoneExt phonext) {
        this.mContext = context;
        this.mSmsDisPatcher = smsDisPatcherInner;
        this.mHandler = this.mSmsDisPatcher.getSmsDispatcherEx().getInstance();
        this.mPhone = phonext;
        RlogEx.d(TAG, "HwGsmSMSDispatcherEx created");
    }

    public boolean isNeedToSendSms(SMSDispatcherEx.SmsTrackerEx tracker) {
        if (tracker == null) {
            return false;
        }
        if (tracker.isMultiSms()) {
            return true;
        }
        int ss = this.mPhone.getServiceState().getState();
        if (!this.mSmsDisPatcher.isIms() && ss != 0) {
            tracker.onFailed(this.mContext, SMSDispatcherEx.getNotInServiceError(ss), 0);
            return false;
        } else if (!HwInnerSmsManagerImpl.getDefault().isLimitNumOfSmsEnabled(true) || !HwInnerSmsManagerImpl.getDefault().isExceedSMSLimit(this.mContext, true) || PHONE_PACKAGE.equals(tracker.getAppPackageName())) {
            this.mTrackerList.add(tracker);
            RlogEx.d(TAG, "sendSms: mTrackerList = " + this.mTrackerList.size());
            if (this.mTrackerList.size() != 1) {
                return false;
            }
            return !isCdmaAndNoIms(tracker);
        } else {
            tracker.onFailed(this.mContext, 5, 0);
            Handler handler = this.mToastHanlder;
            if (handler != null) {
                handler.sendEmptyMessage(1);
            }
            return false;
        }
    }

    public void handleSendSmsAfter(SMSDispatcherEx.SmsTrackerEx tracker) {
        if (tracker != null && !tracker.isMultiSms()) {
            HwInnerSmsManagerImpl.getDefault().triggerSendSmsOverLoadCheck(this.mSmsDisPatcher.getSmsDispatcherEx());
            this.mHandler.removeMessages(EVENT_SMS_SENDING_TIMEOUT);
            Handler handler = this.mHandler;
            handler.sendMessageDelayed(handler.obtainMessage(EVENT_SMS_SENDING_TIMEOUT, tracker), (long) SMS_SENDING_TIMOUEOUT);
        }
    }

    public void handleSmsSendingTimeout(Message msg) {
        if (msg.obj instanceof SMSDispatcherEx.SmsTrackerEx) {
            SMSDispatcherEx.SmsTrackerEx tracker = (SMSDispatcherEx.SmsTrackerEx) msg.obj;
            RlogEx.i(TAG, "EVENT_SMS_SENDING_TIMEOUT, failed tracker is: " + tracker + "blocked size is: " + this.mTrackerList.size());
            if (this.mTrackerList.size() > 0 && this.mTrackerList.remove(tracker)) {
                RlogEx.e(TAG, "EVENT_SMS_SENDING_TIMEOUT, failed tracker is: " + tracker + "blocked size is: " + this.mTrackerList.size());
                this.mHandler.removeMessages(EVENT_SMS_SENDING_TIMEOUT);
                if (this.mTrackerList.size() > 0 && sendSmsImmediately(this.mTrackerList.get(0))) {
                    Handler handler = this.mHandler;
                    handler.sendMessageDelayed(handler.obtainMessage(EVENT_SMS_SENDING_TIMEOUT, this.mTrackerList.get(0)), (long) SMS_SENDING_TIMOUEOUT);
                }
            }
        }
    }

    public void handleSendSmsReTry(SMSDispatcherEx.SmsTrackerEx tracker) {
        RlogEx.i(TAG, "SMS send retry..");
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(EVENT_SMS_SENDING_TIMEOUT, tracker), (long) SMS_SENDING_TIMOUEOUT);
    }

    public void removeTimeoutEvent() {
        this.mHandler.removeMessages(EVENT_SMS_SENDING_TIMEOUT);
    }

    public boolean checkCustIgnoreShortCodeTips() {
        String delPromtHplmns = Settings.System.getString(this.mContext.getContentResolver(), "hw_del_prompt_hplmn");
        if (TextUtils.isEmpty(delPromtHplmns)) {
            RlogEx.w(TAG, "hplmn not match");
            return false;
        }
        IccRecordsEx iccRecordsEx = this.mPhone.getIccRecords();
        String hplmn = iccRecordsEx != null ? iccRecordsEx.getOperatorNumeric() : null;
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

    public boolean sendSmsImmediately(SMSDispatcherEx.SmsTrackerEx tracker) {
        if (tracker == null || isCdmaAndNoIms(tracker)) {
            return false;
        }
        tracker.setMultiSms(true);
        this.mSmsDisPatcher.sendSms(tracker);
        tracker.setMultiSms(false);
        HwInnerSmsManagerImpl.getDefault().triggerSendSmsOverLoadCheck(this.mSmsDisPatcher.getSmsDispatcherEx());
        return true;
    }

    private boolean isCdmaAndNoIms(SMSDispatcherEx.SmsTrackerEx tracker) {
        int ss = this.mPhone.getServiceState().getState();
        int currentPhoneType = this.mPhone.getPhoneType();
        if (this.mSmsDisPatcher.isIms() || ss == 0 || currentPhoneType != 2 || tracker == null) {
            return false;
        }
        RlogEx.i(TAG, "sendSms fail: is not Ims and not in Service, so clear tracker list and retransmission sms with CdmaDispatcher");
        tracker.onFailed(this.mContext, SMSDispatcherEx.getNotInServiceError(ss), 0);
        this.mTrackerList.clear();
        return true;
    }

    public void sendSmsSendingTimeOutMessageDelayed(SMSDispatcherEx.SmsTrackerEx tracker) {
        RlogEx.i(TAG, "handleSendComplete: tracker is:" + tracker);
        if (tracker == null || isCdmaAndNoIms(tracker)) {
            return;
        }
        if (!HwInnerSmsManagerImpl.getDefault().isLimitNumOfSmsEnabled(true) || !HwInnerSmsManagerImpl.getDefault().isExceedSMSLimit(this.mContext, true) || PHONE_PACKAGE.equals(tracker.getAppPackageName())) {
            RlogEx.i(TAG, "handleSendComplete: mTrackerList.size() is:" + this.mTrackerList.size());
            if (this.mTrackerList.remove(tracker) && this.mTrackerList.size() > 0 && sendSmsImmediately(this.mTrackerList.get(0))) {
                Handler handler = this.mHandler;
                handler.sendMessageDelayed(handler.obtainMessage(EVENT_SMS_SENDING_TIMEOUT, this.mTrackerList.get(0)), (long) SMS_SENDING_TIMOUEOUT);
                return;
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
