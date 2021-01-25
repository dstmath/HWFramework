package com.huawei.internal.telephony;

import com.android.internal.telephony.PhoneNotifier;

public class PhoneNotifierEx {
    private PhoneNotifier mPhoneNotifier;

    public PhoneNotifier getPhoneNotifier() {
        return this.mPhoneNotifier;
    }

    public void setPhoneNotifier(PhoneNotifier phoneNotifier) {
        this.mPhoneNotifier = phoneNotifier;
    }
}
