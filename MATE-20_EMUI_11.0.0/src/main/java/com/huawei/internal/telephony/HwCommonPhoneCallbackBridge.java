package com.huawei.internal.telephony;

import android.os.Bundle;
import com.huawei.internal.telephony.IHwCommonPhoneCallback;

public class HwCommonPhoneCallbackBridge extends IHwCommonPhoneCallback.Stub {
    private HwCommonPhoneCallback mCallback;

    public HwCommonPhoneCallbackBridge(HwCommonPhoneCallback callback) {
        this.mCallback = callback;
    }

    public void onCallback1(int param) {
        this.mCallback.onCallback1(param);
    }

    public void onCallback2(int param1, int param2) {
        this.mCallback.onCallback2(param1, param2);
    }

    public void onCallback3(int param1, int param2, Bundle param3) {
        this.mCallback.onCallback3(param1, param2, param3);
    }
}
