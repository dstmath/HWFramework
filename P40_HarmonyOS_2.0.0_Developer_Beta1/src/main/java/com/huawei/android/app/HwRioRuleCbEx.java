package com.huawei.android.app;

import android.app.HwRioClientInfo;
import android.app.IHwRioRuleCb;

public class HwRioRuleCbEx {
    private static final String TAG = HwRioRuleCbEx.class.getSimpleName();
    private IHwRioRuleCb mService = new IHwRioRuleCb.Stub() {
        /* class com.huawei.android.app.HwRioRuleCbEx.AnonymousClass1 */

        public String getRemoteRioRule(HwRioClientInfo info) {
            return HwRioRuleCbEx.this.getRemoteRioRule(new HwRioClientInfoEx(info));
        }
    };

    public String getRemoteRioRule(HwRioClientInfoEx info) {
        return null;
    }

    public IHwRioRuleCb getInnerListener() {
        return this.mService;
    }
}
