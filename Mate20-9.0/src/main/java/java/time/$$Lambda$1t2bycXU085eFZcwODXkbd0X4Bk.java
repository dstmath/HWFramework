package java.time;

import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;

/* renamed from: java.time.-$$Lambda$1t2bycXU085eFZcwODXkbd0X4Bk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$1t2bycXU085eFZcwODXkbd0X4Bk implements TemporalQuery {
    public static final /* synthetic */ $$Lambda$1t2bycXU085eFZcwODXkbd0X4Bk INSTANCE = new $$Lambda$1t2bycXU085eFZcwODXkbd0X4Bk();

    private /* synthetic */ $$Lambda$1t2bycXU085eFZcwODXkbd0X4Bk() {
    }

    public final Object queryFrom(TemporalAccessor temporalAccessor) {
        return Year.from(temporalAccessor);
    }
}
