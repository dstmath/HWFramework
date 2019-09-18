package java.time;

import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;

/* renamed from: java.time.-$$Lambda$PTL8WkLA4o-1z4zIUBjrvwi808w  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PTL8WkLA4o1z4zIUBjrvwi808w implements TemporalQuery {
    public static final /* synthetic */ $$Lambda$PTL8WkLA4o1z4zIUBjrvwi808w INSTANCE = new $$Lambda$PTL8WkLA4o1z4zIUBjrvwi808w();

    private /* synthetic */ $$Lambda$PTL8WkLA4o1z4zIUBjrvwi808w() {
    }

    public final Object queryFrom(TemporalAccessor temporalAccessor) {
        return Instant.from(temporalAccessor);
    }
}
