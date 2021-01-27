package ohos.global.icu.impl.duration;

import ohos.global.icu.impl.duration.BasicPeriodBuilderFactory;

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
    @Override // ohos.global.icu.impl.duration.PeriodBuilderImpl
    public PeriodBuilder withSettings(BasicPeriodBuilderFactory.Settings settings) {
        return get(settings);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.duration.PeriodBuilderImpl
    public Period handleCreate(long j, long j2, boolean z) {
        short effectiveSet = this.settings.effectiveSet();
        Period period = null;
        for (int i = 0; i < TimeUnit.units.length; i++) {
            if (((1 << i) & effectiveSet) != 0) {
                TimeUnit timeUnit = TimeUnit.units[i];
                long approximateDurationOf = approximateDurationOf(timeUnit);
                if (j >= approximateDurationOf || period != null) {
                    double d = ((double) j) / ((double) approximateDurationOf);
                    if (period != null) {
                        return d >= 1.0d ? period.and((float) d, timeUnit) : period;
                    }
                    if (d >= 2.0d) {
                        return Period.at((float) d, timeUnit);
                    }
                    period = Period.at(1.0f, timeUnit).inPast(z);
                    j -= approximateDurationOf;
                }
            }
        }
        return period;
    }
}
