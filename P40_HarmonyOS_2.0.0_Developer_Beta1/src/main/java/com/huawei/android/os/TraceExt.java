package com.huawei.android.os;

import android.os.Trace;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class TraceExt {
    public static void traceCounter(long traceTag, String counterName, int counterValue) {
        Trace.traceCounter(traceTag, counterName, counterValue);
    }
}
