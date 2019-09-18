package com.huawei.android.os;

import android.os.Trace;

public final class TraceEx {
    public static void traceBegin(long traceTag, String methodName) {
        Trace.traceBegin(traceTag, methodName);
    }

    public static void traceEnd(long traceTag) {
        Trace.traceEnd(traceTag);
    }

    public static final long getTraceTagView() {
        return 8;
    }
}
