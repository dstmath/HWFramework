package ohos.global.icu.impl.duration;

import ohos.global.icu.impl.duration.BasicPeriodBuilderFactory;

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
    @Override // ohos.global.icu.impl.duration.PeriodBuilderImpl
    public PeriodBuilder withSettings(BasicPeriodBuilderFactory.Settings settings) {
        return get(settings);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.duration.PeriodBuilderImpl
    public Period handleCreate(long j, long j2, boolean z) {
        short effectiveSet = this.settings.effectiveSet();
        for (int i = 0; i < TimeUnit.units.length; i++) {
            if (((1 << i) & effectiveSet) != 0) {
                TimeUnit timeUnit = TimeUnit.units[i];
                long approximateDurationOf = approximateDurationOf(timeUnit);
                if (j >= approximateDurationOf) {
                    return Period.at((float) (((double) j) / ((double) approximateDurationOf)), timeUnit).inPast(z);
                }
            }
        }
        return null;
    }
}
