package com.huawei.internal.telephony;

import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.Phone;

public class CallManagerCustEx {
    public static final Phone getPhoneInCall(CallManager obj) {
        if (obj == null) {
            return null;
        }
        return obj.getPhoneInCall();
    }
}
