package com.huawei.internal.telephony;

import com.android.internal.telephony.SmsAddress;

public abstract class SmsAddressEx {
    protected SmsAddress mSmsAddress;

    public void setSmsAddress(String address) {
        SmsAddress smsAddress = this.mSmsAddress;
        if (smsAddress != null) {
            smsAddress.address = address;
        }
    }

    public SmsAddress getSmsAddress() {
        SmsAddress smsAddress = this.mSmsAddress;
        if (smsAddress != null) {
            return smsAddress;
        }
        return null;
    }
}
