package java.time.chrono;

import java.io.Serializable;
import java.util.Comparator;

/* renamed from: java.time.chrono.-$$Lambda$AbstractChronology$j22w8kHhJoqCd56hhLQK1G0VLFw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AbstractChronology$j22w8kHhJoqCd56hhLQK1G0VLFw implements Comparator, Serializable {
    public static final /* synthetic */ $$Lambda$AbstractChronology$j22w8kHhJoqCd56hhLQK1G0VLFw INSTANCE = new $$Lambda$AbstractChronology$j22w8kHhJoqCd56hhLQK1G0VLFw();

    private /* synthetic */ $$Lambda$AbstractChronology$j22w8kHhJoqCd56hhLQK1G0VLFw() {
    }

    public final int compare(Object obj, Object obj2) {
        return Long.compare(((ChronoLocalDate) obj).toEpochDay(), ((ChronoLocalDate) obj2).toEpochDay());
    }
}
