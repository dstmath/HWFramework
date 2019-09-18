package java.time;

import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;

/* renamed from: java.time.-$$Lambda$sL_1zXqh7GXCv2G9X40ozp_OBMA  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$sL_1zXqh7GXCv2G9X40ozp_OBMA implements TemporalQuery {
    public static final /* synthetic */ $$Lambda$sL_1zXqh7GXCv2G9X40ozp_OBMA INSTANCE = new $$Lambda$sL_1zXqh7GXCv2G9X40ozp_OBMA();

    private /* synthetic */ $$Lambda$sL_1zXqh7GXCv2G9X40ozp_OBMA() {
    }

    public final Object queryFrom(TemporalAccessor temporalAccessor) {
        return MonthDay.from(temporalAccessor);
    }
}
