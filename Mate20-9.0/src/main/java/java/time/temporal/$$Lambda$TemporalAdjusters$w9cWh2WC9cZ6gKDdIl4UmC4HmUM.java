package java.time.temporal;

/* renamed from: java.time.temporal.-$$Lambda$TemporalAdjusters$w9cWh2WC9cZ6gKDdIl4UmC4HmUM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TemporalAdjusters$w9cWh2WC9cZ6gKDdIl4UmC4HmUM implements TemporalAdjuster {
    public static final /* synthetic */ $$Lambda$TemporalAdjusters$w9cWh2WC9cZ6gKDdIl4UmC4HmUM INSTANCE = new $$Lambda$TemporalAdjusters$w9cWh2WC9cZ6gKDdIl4UmC4HmUM();

    private /* synthetic */ $$Lambda$TemporalAdjusters$w9cWh2WC9cZ6gKDdIl4UmC4HmUM() {
    }

    public final Temporal adjustInto(Temporal temporal) {
        return temporal.with(ChronoField.DAY_OF_YEAR, 1);
    }
}
