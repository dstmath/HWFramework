package com.huawei.android.telecom;

import android.telecom.Call;

public class CallEx {
    public static boolean isActiveSub(Call call) {
        return call.mIsActiveSub;
    }
}
