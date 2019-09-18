package java.time;

import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;

/* renamed from: java.time.-$$Lambda$102LK-VjqD_Dw4HKR2kUw-BMsRk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$102LKVjqD_Dw4HKR2kUwBMsRk implements TemporalQuery {
    public static final /* synthetic */ $$Lambda$102LKVjqD_Dw4HKR2kUwBMsRk INSTANCE = new $$Lambda$102LKVjqD_Dw4HKR2kUwBMsRk();

    private /* synthetic */ $$Lambda$102LKVjqD_Dw4HKR2kUwBMsRk() {
    }

    public final Object queryFrom(TemporalAccessor temporalAccessor) {
        return YearMonth.from(temporalAccessor);
    }
}
