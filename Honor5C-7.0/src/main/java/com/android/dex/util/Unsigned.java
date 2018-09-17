package com.android.dex.util;

import com.android.dex.DexFormat;

public final class Unsigned {
    private Unsigned() {
    }

    public static int compare(short ushortA, short ushortB) {
        if (ushortA == ushortB) {
            return 0;
        }
        return (ushortA & DexFormat.MAX_TYPE_IDX) < (ushortB & DexFormat.MAX_TYPE_IDX) ? -1 : 1;
    }

    public static int compare(int uintA, int uintB) {
        if (uintA == uintB) {
            return 0;
        }
        return (((long) uintA) & 4294967295L) < (((long) uintB) & 4294967295L) ? -1 : 1;
    }
}
