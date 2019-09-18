package java.time;

import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;

/* renamed from: java.time.-$$Lambda$2Dm_gBEmfWAFyI8wDj_HTrgAgUc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$2Dm_gBEmfWAFyI8wDj_HTrgAgUc implements TemporalQuery {
    public static final /* synthetic */ $$Lambda$2Dm_gBEmfWAFyI8wDj_HTrgAgUc INSTANCE = new $$Lambda$2Dm_gBEmfWAFyI8wDj_HTrgAgUc();

    private /* synthetic */ $$Lambda$2Dm_gBEmfWAFyI8wDj_HTrgAgUc() {
    }

    public final Object queryFrom(TemporalAccessor temporalAccessor) {
        return LocalTime.from(temporalAccessor);
    }
}
