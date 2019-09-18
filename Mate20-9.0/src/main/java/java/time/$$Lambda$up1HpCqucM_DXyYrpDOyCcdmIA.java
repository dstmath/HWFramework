package java.time;

import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;

/* renamed from: java.time.-$$Lambda$up1HpCqucM_DXyY-rpDOyCcdmIA  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$up1HpCqucM_DXyYrpDOyCcdmIA implements TemporalQuery {
    public static final /* synthetic */ $$Lambda$up1HpCqucM_DXyYrpDOyCcdmIA INSTANCE = new $$Lambda$up1HpCqucM_DXyYrpDOyCcdmIA();

    private /* synthetic */ $$Lambda$up1HpCqucM_DXyYrpDOyCcdmIA() {
    }

    public final Object queryFrom(TemporalAccessor temporalAccessor) {
        return ZonedDateTime.from(temporalAccessor);
    }
}
