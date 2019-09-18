package com.huawei.secure.android.common.detect;

import com.huawei.secure.android.common.util.LogsUtil;

public final class SD {
    public static native boolean idj();

    public static native boolean iej();

    public static native boolean irpj();

    public static native boolean irtj();

    static {
        try {
            System.loadLibrary("aegissec");
        } catch (UnsatisfiedLinkError e) {
            LogsUtil.e("SD", "load aegis.so UnsatisfiedLinkError", true);
        }
    }

    private SD() {
    }
}
