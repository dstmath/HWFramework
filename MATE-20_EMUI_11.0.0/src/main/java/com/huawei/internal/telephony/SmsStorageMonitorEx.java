package com.huawei.internal.telephony;

import com.android.internal.telephony.SmsStorageMonitor;

public class SmsStorageMonitorEx {
    private SmsStorageMonitor mSmsStorageMonitor;

    public void setSmsStorageMonitor(SmsStorageMonitor smsStorageMonitor) {
        this.mSmsStorageMonitor = smsStorageMonitor;
    }

    public boolean isStorageAvailable() {
        SmsStorageMonitor smsStorageMonitor = this.mSmsStorageMonitor;
        if (smsStorageMonitor != null) {
            return smsStorageMonitor.isStorageAvailable();
        }
        return false;
    }
}
