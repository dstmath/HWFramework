package java.time.temporal;

/* renamed from: java.time.temporal.-$$Lambda$TemporalAdjusters$iS4EYkMA1JewgnCHCuVtjW33u14  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TemporalAdjusters$iS4EYkMA1JewgnCHCuVtjW33u14 implements TemporalAdjuster {
    public static final /* synthetic */ $$Lambda$TemporalAdjusters$iS4EYkMA1JewgnCHCuVtjW33u14 INSTANCE = new $$Lambda$TemporalAdjusters$iS4EYkMA1JewgnCHCuVtjW33u14();

    private /* synthetic */ $$Lambda$TemporalAdjusters$iS4EYkMA1JewgnCHCuVtjW33u14() {
    }

    public final Temporal adjustInto(Temporal temporal) {
        return temporal.with(ChronoField.DAY_OF_YEAR, temporal.range(ChronoField.DAY_OF_YEAR).getMaximum());
    }
}
