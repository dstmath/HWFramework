package com.huawei.android.os;

import huawei.android.os.HwProtectAreaManager;

public class ProtectAreaEx {
    public static int readProtectArea(String optItem, int readBufLen, String[] readBuf, int[] errorNum) {
        return HwProtectAreaManager.getInstance().readProtectArea(optItem, readBufLen, readBuf, errorNum);
    }

    public static int writeProtectArea(String optItem, int writeLen, String writeValue, int[] errorNum) {
        return HwProtectAreaManager.getInstance().writeProtectArea(optItem, writeLen, writeValue, errorNum);
    }
}
