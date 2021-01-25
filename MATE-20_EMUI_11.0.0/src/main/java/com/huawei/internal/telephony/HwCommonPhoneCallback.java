package com.huawei.internal.telephony;

import android.os.Bundle;

public class HwCommonPhoneCallback {
    private HwCommonPhoneCallbackBridge mHwCommonPhoneCallbackBridge = new HwCommonPhoneCallbackBridge(this);

    public Object getHwCommonPhoneCallback() {
        return this.mHwCommonPhoneCallbackBridge;
    }

    public void onCallback1(int param) {
    }

    public void onCallback2(int param1, int param2) {
    }

    public void onCallback3(int param1, int param2, Bundle param3) {
    }
}
