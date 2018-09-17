package com.huawei.internal.telephony;

import com.android.internal.telephony.PhoneFactory;

public class PhoneFactoryEx {
    public static PhoneEx getPhone(int phoneId) {
        return new PhoneEx(PhoneFactory.getPhone(phoneId));
    }
}
