package com.huawei.android.os;

import android.os.Trace;
import com.huawei.annotation.HwSystemApi;

public final class TraceEx {
    @HwSystemApi
    public static final long TRACE_TAG_CAMERA = 1024;

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
