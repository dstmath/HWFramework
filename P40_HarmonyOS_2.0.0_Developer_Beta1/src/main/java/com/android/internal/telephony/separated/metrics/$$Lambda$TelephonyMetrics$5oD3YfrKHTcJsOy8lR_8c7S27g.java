package com.android.internal.telephony.separated.metrics;

import java.util.function.IntFunction;

/* renamed from: com.android.internal.telephony.separated.metrics.-$$Lambda$TelephonyMetrics$5oD3YfrKHTcJsOy8lR_8-c7S27g  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TelephonyMetrics$5oD3YfrKHTcJsOy8lR_8c7S27g implements IntFunction {
    public static final /* synthetic */ $$Lambda$TelephonyMetrics$5oD3YfrKHTcJsOy8lR_8c7S27g INSTANCE = new $$Lambda$TelephonyMetrics$5oD3YfrKHTcJsOy8lR_8c7S27g();

    private /* synthetic */ $$Lambda$TelephonyMetrics$5oD3YfrKHTcJsOy8lR_8c7S27g() {
    }

    @Override // java.util.function.IntFunction
    public final Object apply(int i) {
        return TelephonyMetrics.lambda$convertEmergencyNumberToEmergencyNumberInfo$1(i);
    }
}
