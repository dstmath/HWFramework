package android.icu.impl.duration;

import android.icu.impl.duration.BasicPeriodBuilderFactory;

/* compiled from: BasicPeriodBuilderFactory */
class FixedUnitBuilder extends PeriodBuilderImpl {
    private TimeUnit unit;

    public static FixedUnitBuilder get(TimeUnit unit2, BasicPeriodBuilderFactory.Settings settingsToUse) {
        if (settingsToUse == null || (settingsToUse.effectiveSet() & (1 << unit2.ordinal)) == 0) {
            return null;
        }
        return new FixedUnitBuilder(unit2, settingsToUse);
    }

    FixedUnitBuilder(TimeUnit unit2, BasicPeriodBuilderFactory.Settings settings) {
        super(settings);
        this.unit = unit2;
    }

    /* access modifiers changed from: protected */
    public PeriodBuilder withSettings(BasicPeriodBuilderFactory.Settings settingsToUse) {
        return get(this.unit, settingsToUse);
    }

    /* access modifiers changed from: protected */
    public Period handleCreate(long duration, long referenceDate, boolean inPast) {
        if (this.unit == null) {
            return null;
        }
        return Period.at((float) (((double) duration) / ((double) approximateDurationOf(this.unit))), this.unit).inPast(inPast);
    }
}
