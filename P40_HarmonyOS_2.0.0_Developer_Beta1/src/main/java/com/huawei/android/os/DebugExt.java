package com.huawei.android.os;

import android.os.Debug;

public class DebugExt {
    public static long getPss(int pid, long[] outUssSwapPssRss, long[] outMemtrack) {
        return Debug.getPss(pid, outUssSwapPssRss, outMemtrack);
    }
}
