package android.icu.impl.duration;

/* compiled from: BasicPeriodBuilderFactory */
class OneOrTwoUnitBuilder extends PeriodBuilderImpl {
    OneOrTwoUnitBuilder(Settings settings) {
        super(settings);
    }

    public static OneOrTwoUnitBuilder get(Settings settings) {
        if (settings == null) {
            return null;
        }
        return new OneOrTwoUnitBuilder(settings);
    }

    protected PeriodBuilder withSettings(Settings settingsToUse) {
        return get(settingsToUse);
    }

    protected Period handleCreate(long duration, long referenceDate, boolean inPast) {
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
