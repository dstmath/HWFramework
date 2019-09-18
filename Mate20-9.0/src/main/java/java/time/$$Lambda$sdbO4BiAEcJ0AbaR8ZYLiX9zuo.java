package java.time;

import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;

/* renamed from: java.time.-$$Lambda$sdbO4BiAEcJ0Ab-aR8ZYLiX9zuo  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$sdbO4BiAEcJ0AbaR8ZYLiX9zuo implements TemporalQuery {
    public static final /* synthetic */ $$Lambda$sdbO4BiAEcJ0AbaR8ZYLiX9zuo INSTANCE = new $$Lambda$sdbO4BiAEcJ0AbaR8ZYLiX9zuo();

    private /* synthetic */ $$Lambda$sdbO4BiAEcJ0AbaR8ZYLiX9zuo() {
    }

    public final Object queryFrom(TemporalAccessor temporalAccessor) {
        return OffsetDateTime.from(temporalAccessor);
    }
}
