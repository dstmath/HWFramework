package com.huawei.android.telephony;

public class HwSmsInterceptionListenerEx {
    private SmsInterceptionListenerEx mSmsInterceptionListenerEx;

    public HwSmsInterceptionListenerEx(HwSmsInterceptionCallBack callBack) {
        this.mSmsInterceptionListenerEx = null;
        this.mSmsInterceptionListenerEx = new SmsInterceptionListenerEx();
        this.mSmsInterceptionListenerEx.setSmsInterceptionCallBack(callBack);
    }

    public void registerListener(int priority) {
        SmsInterceptionManagerEx.getInstance().registerListener(this.mSmsInterceptionListenerEx, priority);
    }

    public void unregisterListener(int priority) {
        SmsInterceptionManagerEx.getInstance().unregisterListener(priority);
    }

    public SmsInterceptionListenerEx getSmsInterceptionListenerEx() {
        return this.mSmsInterceptionListenerEx;
    }
}
