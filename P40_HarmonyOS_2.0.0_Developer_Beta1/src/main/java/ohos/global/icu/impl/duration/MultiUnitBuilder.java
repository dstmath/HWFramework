package ohos.global.icu.impl.duration;

import ohos.global.icu.impl.duration.BasicPeriodBuilderFactory;

/* compiled from: BasicPeriodBuilderFactory */
class MultiUnitBuilder extends PeriodBuilderImpl {
    private int nPeriods;

    MultiUnitBuilder(int i, BasicPeriodBuilderFactory.Settings settings) {
        super(settings);
        this.nPeriods = i;
    }

    public static MultiUnitBuilder get(int i, BasicPeriodBuilderFactory.Settings settings) {
        if (i <= 0 || settings == null) {
            return null;
        }
        return new MultiUnitBuilder(i, settings);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.duration.PeriodBuilderImpl
    public PeriodBuilder withSettings(BasicPeriodBuilderFactory.Settings settings) {
        return get(this.nPeriods, settings);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.duration.PeriodBuilderImpl
    public Period handleCreate(long j, long j2, boolean z) {
        short effectiveSet = this.settings.effectiveSet();
        int i = 0;
        Period period = null;
        long j3 = j;
        for (int i2 = 0; i2 < TimeUnit.units.length; i2++) {
            if (((1 << i2) & effectiveSet) != 0) {
                TimeUnit timeUnit = TimeUnit.units[i2];
                if (i == this.nPeriods) {
                    break;
                }
                long approximateDurationOf = approximateDurationOf(timeUnit);
                if (j3 >= approximateDurationOf || i > 0) {
                    i++;
                    double d = (double) approximateDurationOf;
                    double d2 = ((double) j3) / d;
                    if (i < this.nPeriods) {
                        d2 = Math.floor(d2);
                        j3 -= (long) (d * d2);
                    }
                    if (period == null) {
                        period = Period.at((float) d2, timeUnit).inPast(z);
                    } else {
                        period = period.and((float) d2, timeUnit);
                    }
                }
            }
        }
        return period;
    }
}
