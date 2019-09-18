package android.icu.impl.duration;

import android.icu.impl.duration.BasicPeriodBuilderFactory;

/* compiled from: BasicPeriodBuilderFactory */
class OneOrTwoUnitBuilder extends PeriodBuilderImpl {
    OneOrTwoUnitBuilder(BasicPeriodBuilderFactory.Settings settings) {
        super(settings);
    }

    public static OneOrTwoUnitBuilder get(BasicPeriodBuilderFactory.Settings settings) {
        if (settings == null) {
            return null;
        }
        return new OneOrTwoUnitBuilder(settings);
    }

    /* access modifiers changed from: protected */
    public PeriodBuilder withSettings(BasicPeriodBuilderFactory.Settings settingsToUse) {
        return get(settingsToUse);
    }

    /* access modifiers changed from: protected */
    public Period handleCreate(long duration, long referenceDate, boolean inPast) {
        Period period = null;
        short uset = this.settings.effectiveSet();
        for (int i = 0; i < TimeUnit.units.length; i++) {
            if (((1 << i) & uset) != 0) {
                TimeUnit unit = TimeUnit.units[i];
                long unitDuration = approximateDurationOf(unit);
                if (duration >= unitDuration || period != null) {
                    double count = ((double) duration) / ((double) unitDuration);
                    if (period == null) {
                        if (count >= 2.0d) {
                            return Period.at((float) count, unit);
                        }
                        period = Period.at(1.0f, unit).inPast(inPast);
                        duration -= unitDuration;
                    } else if (count >= 1.0d) {
                        return period.and((float) count, unit);
                    } else {
                        return period;
                    }
                }
            }
        }
        return period;
    }
}
