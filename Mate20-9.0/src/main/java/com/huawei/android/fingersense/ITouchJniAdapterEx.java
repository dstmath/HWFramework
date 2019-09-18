package com.huawei.android.fingersense;

import com.huawei.itouch.HwITouchJniAdapter;

public class ITouchJniAdapterEx {
    public static void registerJniListener() {
        HwITouchJniAdapter.getInstance().registerJniListener();
    }

    public static int getAppType() {
        return HwITouchJniAdapter.getInstance().getAppType();
    }
}
