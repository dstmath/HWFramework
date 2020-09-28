package com.android.internal.telephony;

import android.os.Handler;
import android.util.ArraySet;
import android.widget.Toast;
import com.android.internal.telephony.SMSDispatcher;

public abstract class AbstractSMSDispatcher extends Handler {
    protected static final String CONTACTS_PACKAGE_NAME = "com.android.contacts";
    protected static final int EVENT_POP_TOAST = 17;
    protected static final int MAX_SEND_RETRIES_FOR_VIA = 3;
    protected static final String MESSAGE_STRING_NAME = "app_label";
    protected static final String OUTGOING_SMS_EXCEPTION_PATTERN = "outgoing_sms_exception_pattern";
    protected static final String OUTGOING_SMS_RESTRICTION_PATTERN = "outgoing_sms_restriction_pattern";
    protected static final String PHONE_PACKAGE = "com.android.phone";
    protected static final String RESOURCE_TYPE_STRING = "string";
    protected static final String SYSTEM_MANAGER_PROCESS_NAME = "com.huawei.systemmanager:service";
    protected static final String TELECOM_PACKAGE_NAME = "com.android.server.telecom";
    protected String mCallingPackage = PhoneConfigurationManager.SSSS;
    protected final ArraySet<String> mPackageSendSmsCount = new ArraySet<>();
    protected Toast mToast = null;

    /* access modifiers changed from: protected */
    public void sendSmsOrigin(SMSDispatcher.SmsTracker tracker) {
    }

    /* access modifiers changed from: protected */
    public boolean sendSmsImmediately(SMSDispatcher.SmsTracker tracker) {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean checkCustIgnoreShortCodeTips() {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isViaAndCdma() {
        return false;
    }

    /* access modifiers changed from: protected */
    public void sendSmsSendingTimeOutMessageDelayed(SMSDispatcher.SmsTracker tracker) {
    }
}
