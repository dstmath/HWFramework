package com.huawei.internal.telephony;

import com.android.internal.telephony.CallManager;
import com.huawei.annotation.HwSystemApi;

public class CallManagerExt {
    public static int getState() {
        return CallManager.getInstance().getState().ordinal();
    }

    @HwSystemApi
    public static void onSwitchToOtherActiveSub(PhoneExt phone) {
        if (phone != null) {
            CallManager.getInstance().onSwitchToOtherActiveSub(phone.getPhone());
        }
    }
}
