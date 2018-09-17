package com.huawei.internal.telephony;

import com.android.internal.telephony.MmiCode;
import com.android.internal.telephony.Phone;

public class MmiCodeEx {
    public static Phone getPhone(MmiCode obj) {
        return obj.getPhone();
    }
}
