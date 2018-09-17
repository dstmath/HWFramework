package android.icu.impl.duration;

/* compiled from: BasicPeriodBuilderFactory */
class SingleUnitBuilder extends PeriodBuilderImpl {
    SingleUnitBuilder(Settings settings) {
        super(settings);
    }

    public static SingleUnitBuilder get(Settings settings) {
        if (settings == null) {
            return null;
        }
        return new SingleUnitBuilder(settings);
    }

    protected PeriodBuilder withSettings(Settings settingsToUse) {
        return get(settingsToUse);
    }

    protected Period handleCreate(long duration, long referenceDate, boolean inPast) {
        short uset = this.settings.effectiveSet();
        for (int i = 0; i < TimeUnit.units.length; i++) {
            if (((1 << i) & uset) != 0) {
                TimeUnit unit = TimeUnit.units[i];
                long unitDuration = approximateDurationOf(unit);
                if (duration >= unitDuration) {
                    return Period.at((float) (((double) duration) / ((double) unitDuration)), unit).inPast(inPast);
                }
            }
        }
        return null;
    }
}
