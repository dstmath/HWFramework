package com.huawei.android.os;

import com.android.internal.os.ZygoteInit;

public class ZygoteInitEx {
    public static boolean isMygote() {
        return ZygoteInit.sIsMygote;
    }
}
