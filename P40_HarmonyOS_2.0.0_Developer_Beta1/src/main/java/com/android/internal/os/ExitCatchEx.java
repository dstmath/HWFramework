package com.android.internal.os;

import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class ExitCatchEx {
    public static final int EXIT_CATCH_FORAPP_FLAG = 8;

    public static boolean enable(int pid, int flags) {
        return ExitCatch.enable(pid, flags);
    }

    public static boolean disable(int pid) {
        return ExitCatch.disable(pid);
    }
}
