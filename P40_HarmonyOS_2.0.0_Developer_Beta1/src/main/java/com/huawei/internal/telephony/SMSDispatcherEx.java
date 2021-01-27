package com.huawei.internal.telephony;

import android.content.Context;
import android.os.Handler;
import android.util.ArraySet;
import com.android.internal.telephony.SMSDispatcher;

public class SMSDispatcherEx {
    protected SMSDispatcher mSmsDispatcher;

    public static int getNotInServiceError(int ss) {
        return SMSDispatcher.getNotInServiceError(ss);
    }

    public static int getMaxSendRetriesHw() {
        return SMSDispatcher.getMaxSendRetriesHw();
    }

    public static int getEventSendRetryHw() {
        return SMSDispatcher.getEventSendRetryHw();
    }

    public void setSmsDispatcher(SMSDispatcher smsDispatcher) {
        this.mSmsDispatcher = smsDispatcher;
    }

    public ArraySet<String> getPackageSendSmsCount() {
        SMSDispatcher sMSDispatcher = this.mSmsDispatcher;
        if (sMSDispatcher != null) {
            return sMSDispatcher.getPackageSendSmsCount();
        }
        return null;
    }

    public int getSubId() {
        SMSDispatcher sMSDispatcher = this.mSmsDispatcher;
        if (sMSDispatcher != null) {
            return sMSDispatcher.getSubIdHw();
        }
        return 0;
    }

    public void clearPackageSendSmsCount() {
        SMSDispatcher sMSDispatcher = this.mSmsDispatcher;
        if (sMSDispatcher != null) {
            sMSDispatcher.clearPackageSendSmsCount();
        }
    }

    public CharSequence getAppLabelHw(String appPackage, int userId) {
        SMSDispatcher sMSDispatcher = this.mSmsDispatcher;
        if (sMSDispatcher != null) {
            return sMSDispatcher.getAppLabelHw(appPackage, userId);
        }
        return null;
    }

    public Context getContext() {
        SMSDispatcher sMSDispatcher = this.mSmsDispatcher;
        if (sMSDispatcher != null) {
            return sMSDispatcher.getContext();
        }
        return null;
    }

    public Handler getInstance() {
        return this.mSmsDispatcher;
    }

    public static class SmsTrackerEx {
        private boolean mIsMultiSms;
        private SMSDispatcher.SmsTracker mSmsTracker;

        public SMSDispatcher.SmsTracker getSmsTracker() {
            return this.mSmsTracker;
        }

        public void setSmsTracker(SMSDispatcher.SmsTracker smsTracker) {
            this.mSmsTracker = smsTracker;
        }

        public void onFailed(Context context, int error, int errorCode) {
            SMSDispatcher.SmsTracker smsTracker = this.mSmsTracker;
            if (smsTracker != null) {
                smsTracker.onFailed(context, error, errorCode);
            }
        }

        public String getAppPackageName() {
            SMSDispatcher.SmsTracker smsTracker = this.mSmsTracker;
            if (smsTracker == null || smsTracker.mAppInfo == null) {
                return null;
            }
            return this.mSmsTracker.mAppInfo.packageName;
        }

        public boolean isMultiSms() {
            return this.mIsMultiSms;
        }

        public void setMultiSms(boolean isMultiSms) {
            this.mIsMultiSms = isMultiSms;
        }
    }
}
