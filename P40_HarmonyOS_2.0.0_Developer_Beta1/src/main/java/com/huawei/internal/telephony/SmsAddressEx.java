package com.huawei.internal.telephony;

import com.android.internal.telephony.SmsAddress;

public abstract class SmsAddressEx {
    public static final int TON_INTERNATIONAL = 1;
    protected SmsAddress mSmsAddress;

    public SmsAddress getSmsAddress() {
        SmsAddress smsAddress = this.mSmsAddress;
        if (smsAddress != null) {
            return smsAddress;
        }
        return null;
    }

    public void setSmsAddress(String address) {
        SmsAddress smsAddress = this.mSmsAddress;
        if (smsAddress != null) {
            smsAddress.address = address;
        }
    }
}
