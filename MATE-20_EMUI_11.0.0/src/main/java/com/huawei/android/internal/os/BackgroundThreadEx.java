package com.huawei.android.internal.os;

import android.os.Handler;
import com.android.internal.os.BackgroundThread;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class BackgroundThreadEx {
    public static Handler getHandler() {
        return BackgroundThread.getHandler();
    }
}
