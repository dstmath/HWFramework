package com.huawei.android.os;

import android.os.Debug;
import com.huawei.annotation.HwSystemApi;

public class DebugEx {
    public static long countInstancesOfClass(Class cls) {
        return Debug.countInstancesOfClass(cls);
    }

    @HwSystemApi
    public static String getCaller() {
        return Debug.getCaller();
    }

    @HwSystemApi
    public static String getCallers(int depth) {
        return Debug.getCallers(depth);
    }
}
