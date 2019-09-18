package android.icu.impl.duration;

import android.icu.impl.duration.BasicPeriodBuilderFactory;

/* compiled from: BasicPeriodBuilderFactory */
class SingleUnitBuilder extends PeriodBuilderImpl {
    SingleUnitBuilder(BasicPeriodBuilderFactory.Settings settings) {
        super(settings);
    }

    public static SingleUnitBuilder get(BasicPeriodBuilderFactory.Settings settings) {
        if (settings == null) {
            return null;
        }
        return new SingleUnitBuilder(settings);
    }

    /* access modifiers changed from: protected */
    public PeriodBuilder withSettings(BasicPeriodBuilderFactory.Settings settingsToUse) {
        return get(settingsToUse);
    }

    /* access modifiers changed from: protected */
    public Period handleCreate(long duration, long referenceDate, boolean inPast) {
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
