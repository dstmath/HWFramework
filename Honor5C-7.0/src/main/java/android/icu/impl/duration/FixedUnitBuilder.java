package android.icu.impl.duration;

/* compiled from: BasicPeriodBuilderFactory */
class FixedUnitBuilder extends PeriodBuilderImpl {
    private TimeUnit unit;

    public static FixedUnitBuilder get(TimeUnit unit, Settings settingsToUse) {
        if (settingsToUse == null || (settingsToUse.effectiveSet() & (1 << unit.ordinal)) == 0) {
            return null;
        }
        return new FixedUnitBuilder(unit, settingsToUse);
    }

    FixedUnitBuilder(TimeUnit unit, Settings settings) {
        super(settings);
        this.unit = unit;
    }

    protected PeriodBuilder withSettings(Settings settingsToUse) {
        return get(this.unit, settingsToUse);
    }

    protected Period handleCreate(long duration, long referenceDate, boolean inPast) {
        if (this.unit == null) {
            return null;
        }
        return Period.at((float) (((double) duration) / ((double) approximateDurationOf(this.unit))), this.unit).inPast(inPast);
    }
}
