package com.android.internal.telephony;

import com.huawei.internal.telephony.PhoneExt;

public class HwCustPhoneManager {
    public boolean isStringHuaweiIgnoreCode(PhoneExt phone, String dialString) {
        return false;
    }

    public boolean changeMMItoUSSD(PhoneExt phone, String poundString) {
        return false;
    }

    public boolean isSupportOrangeApn(PhoneExt phone) {
        return false;
    }

    public void addSpecialAPN(PhoneExt phone) {
    }
}
