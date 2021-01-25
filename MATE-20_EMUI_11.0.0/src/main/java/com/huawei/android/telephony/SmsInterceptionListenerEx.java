package com.huawei.android.telephony;

import android.os.Bundle;
import android.telephony.SmsInterceptionListener;

public class SmsInterceptionListenerEx extends SmsInterceptionListener {
    private HwSmsInterceptionCallBack mSmsInterceptionCallBack = null;

    public int handleSmsDeliverAction(Bundle smsInfo) {
        HwSmsInterceptionCallBack hwSmsInterceptionCallBack = this.mSmsInterceptionCallBack;
        if (hwSmsInterceptionCallBack != null) {
            return hwSmsInterceptionCallBack.handleSmsDeliverAction(smsInfo);
        }
        return 0;
    }

    public int handleWapPushDeliverAction(Bundle wapPushInfo) {
        HwSmsInterceptionCallBack hwSmsInterceptionCallBack = this.mSmsInterceptionCallBack;
        if (hwSmsInterceptionCallBack != null) {
            return hwSmsInterceptionCallBack.handleWapPushDeliverAction(wapPushInfo);
        }
        return 0;
    }

    public boolean sendNumberBlockedRecord(Bundle smsInfo) {
        HwSmsInterceptionCallBack hwSmsInterceptionCallBack = this.mSmsInterceptionCallBack;
        if (hwSmsInterceptionCallBack != null) {
            return hwSmsInterceptionCallBack.sendNumberBlockedRecord(smsInfo);
        }
        return false;
    }

    public void setSmsInterceptionCallBack(HwSmsInterceptionCallBack callBack) {
        this.mSmsInterceptionCallBack = callBack;
    }
}
