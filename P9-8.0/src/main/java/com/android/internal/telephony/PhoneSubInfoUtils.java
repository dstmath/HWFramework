package com.android.internal.telephony;

import android.content.Context;

public class PhoneSubInfoUtils {
    public Phone getPhone(PhoneSubInfoController phoneSubInfo, int subId) {
        return phoneSubInfo.getPhoneHw(subId);
    }

    public Context getContext(PhoneSubInfoController phoneSubInfo) {
        return phoneSubInfo.getContextHw();
    }
}
