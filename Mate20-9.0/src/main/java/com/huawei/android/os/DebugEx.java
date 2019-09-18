package com.huawei.android.os;

import android.os.Debug;

public class DebugEx {
    public static long countInstancesOfClass(Class cls) {
        return Debug.countInstancesOfClass(cls);
    }
}
