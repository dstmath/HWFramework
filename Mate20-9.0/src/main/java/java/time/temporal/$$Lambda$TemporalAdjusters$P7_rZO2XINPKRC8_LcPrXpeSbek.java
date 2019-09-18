package java.time.temporal;

/* renamed from: java.time.temporal.-$$Lambda$TemporalAdjusters$P7_rZO2XINPKRC8_LcPrXpeSbek  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TemporalAdjusters$P7_rZO2XINPKRC8_LcPrXpeSbek implements TemporalAdjuster {
    public static final /* synthetic */ $$Lambda$TemporalAdjusters$P7_rZO2XINPKRC8_LcPrXpeSbek INSTANCE = new $$Lambda$TemporalAdjusters$P7_rZO2XINPKRC8_LcPrXpeSbek();

    private /* synthetic */ $$Lambda$TemporalAdjusters$P7_rZO2XINPKRC8_LcPrXpeSbek() {
    }

    public final Temporal adjustInto(Temporal temporal) {
        return temporal.with(ChronoField.DAY_OF_MONTH, 1).plus(1, ChronoUnit.MONTHS);
    }
}
