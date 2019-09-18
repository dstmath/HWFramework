package com.huawei.android.telephony;

import android.telephony.SmsInterceptionManager;

public class SmsInterceptionManagerEx {
    private static final SmsInterceptionManagerEx sInstance = new SmsInterceptionManagerEx();

    public static synchronized SmsInterceptionManagerEx getInstance() {
        SmsInterceptionManagerEx smsInterceptionManagerEx;
        synchronized (SmsInterceptionManagerEx.class) {
            smsInterceptionManagerEx = sInstance;
        }
        return smsInterceptionManagerEx;
    }

    public void registerListener(SmsInterceptionListenerEx listener, int priority) {
        SmsInterceptionManager.getInstance().registerListener(listener, priority);
    }

    public void unregisterListener(int priority) {
        SmsInterceptionManager.getInstance().unregisterListener(priority);
    }
}
