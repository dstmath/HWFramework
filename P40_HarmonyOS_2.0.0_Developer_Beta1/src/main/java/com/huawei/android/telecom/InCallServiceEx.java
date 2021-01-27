package com.huawei.android.telecom;

import android.telecom.InCallService;

public class InCallServiceEx {
    public static void switchToOtherActiveSub(InCallService inCallService, String subId, boolean retainLch) {
        if (inCallService != null && inCallService.getPhone() != null) {
            inCallService.getPhone().switchToOtherActiveSub(subId, retainLch);
        }
    }

    public static void setBluetoothAudioRoute(InCallService inCallService, String address) {
        if (inCallService != null) {
            inCallService.setBluetoothAudioRoute(address);
        }
    }
}
