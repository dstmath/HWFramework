package com.huawei.android.telephony;

import android.os.Bundle;
import android.telephony.SmsInterceptionListener;

public class SmsInterceptionListenerEx extends SmsInterceptionListener {
    private HwSmsInterceptionCallBack mSmsInterceptionCallBack = null;

    public int handleSmsDeliverAction(Bundle smsInfo) {
        if (this.mSmsInterceptionCallBack != null) {
            return this.mSmsInterceptionCallBack.handleSmsDeliverAction(smsInfo);
        }
        return 0;
    }

    public int handleWapPushDeliverAction(Bundle wapPushInfo) {
        if (this.mSmsInterceptionCallBack != null) {
            return this.mSmsInterceptionCallBack.handleWapPushDeliverAction(wapPushInfo);
        }
        return 0;
    }

    public boolean sendNumberBlockedRecord(Bundle smsInfo) {
        if (this.mSmsInterceptionCallBack != null) {
            return this.mSmsInterceptionCallBack.sendNumberBlockedRecord(smsInfo);
        }
        return false;
    }

    public void setSmsInterceptionCallBack(HwSmsInterceptionCallBack callBack) {
        this.mSmsInterceptionCallBack = callBack;
    }
}
