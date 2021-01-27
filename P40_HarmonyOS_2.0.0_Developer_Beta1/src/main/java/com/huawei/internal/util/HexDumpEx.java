package com.huawei.internal.util;

import com.android.internal.util.HexDump;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class HexDumpEx {
    public static String toHexString(byte[] array) {
        return HexDump.toHexString(array);
    }
}
