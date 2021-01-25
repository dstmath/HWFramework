package ohos.global.icu.impl.duration;

import ohos.global.icu.impl.duration.BasicPeriodBuilderFactory;

/* compiled from: BasicPeriodBuilderFactory */
class FixedUnitBuilder extends PeriodBuilderImpl {
    private TimeUnit unit;

    public static FixedUnitBuilder get(TimeUnit timeUnit, BasicPeriodBuilderFactory.Settings settings) {
        if (settings == null || (settings.effectiveSet() & (1 << timeUnit.ordinal)) == 0) {
            return null;
        }
        return new FixedUnitBuilder(timeUnit, settings);
    }

    FixedUnitBuilder(TimeUnit timeUnit, BasicPeriodBuilderFactory.Settings settings) {
        super(settings);
        this.unit = timeUnit;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.duration.PeriodBuilderImpl
    public PeriodBuilder withSettings(BasicPeriodBuilderFactory.Settings settings) {
        return get(this.unit, settings);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.duration.PeriodBuilderImpl
    public Period handleCreate(long j, long j2, boolean z) {
        TimeUnit timeUnit = this.unit;
        if (timeUnit == null) {
            return null;
        }
        return Period.at((float) (((double) j) / ((double) approximateDurationOf(timeUnit))), this.unit).inPast(z);
    }
}
