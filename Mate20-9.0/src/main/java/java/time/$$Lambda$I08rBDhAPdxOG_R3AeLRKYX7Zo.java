package java.time;

import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;

/* renamed from: java.time.-$$Lambda$I08rBDhAPdxOG_R3AeLRKYX7Z-o  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$I08rBDhAPdxOG_R3AeLRKYX7Zo implements TemporalQuery {
    public static final /* synthetic */ $$Lambda$I08rBDhAPdxOG_R3AeLRKYX7Zo INSTANCE = new $$Lambda$I08rBDhAPdxOG_R3AeLRKYX7Zo();

    private /* synthetic */ $$Lambda$I08rBDhAPdxOG_R3AeLRKYX7Zo() {
    }

    public final Object queryFrom(TemporalAccessor temporalAccessor) {
        return OffsetTime.from(temporalAccessor);
    }
}
