package com.android.internal.telephony.separated.metrics;

import java.util.function.IntFunction;

/* renamed from: com.android.internal.telephony.separated.metrics.-$$Lambda$TelephonyMetrics$F9I8P5zeR9ipk0DcjfoG0kuwxMM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TelephonyMetrics$F9I8P5zeR9ipk0DcjfoG0kuwxMM implements IntFunction {
    public static final /* synthetic */ $$Lambda$TelephonyMetrics$F9I8P5zeR9ipk0DcjfoG0kuwxMM INSTANCE = new $$Lambda$TelephonyMetrics$F9I8P5zeR9ipk0DcjfoG0kuwxMM();

    private /* synthetic */ $$Lambda$TelephonyMetrics$F9I8P5zeR9ipk0DcjfoG0kuwxMM() {
    }

    @Override // java.util.function.IntFunction
    public final Object apply(int i) {
        return TelephonyMetrics.lambda$writeCarrierIdMatchingEvent$2(i);
    }
}
