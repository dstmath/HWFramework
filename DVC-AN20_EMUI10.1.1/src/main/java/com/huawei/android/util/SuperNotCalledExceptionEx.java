package com.huawei.android.util;

import android.util.SuperNotCalledException;

public class SuperNotCalledExceptionEx {
    public static boolean isSuperNotCalledException(Class cls) {
        return cls.equals(SuperNotCalledException.class);
    }
}
