package android.icu.impl.duration;

/* compiled from: BasicPeriodBuilderFactory */
class MultiUnitBuilder extends PeriodBuilderImpl {
    private int nPeriods;

    MultiUnitBuilder(int nPeriods, Settings settings) {
        super(settings);
        this.nPeriods = nPeriods;
    }

    public static MultiUnitBuilder get(int nPeriods, Settings settings) {
        if (nPeriods <= 0 || settings == null) {
            return null;
        }
        return new MultiUnitBuilder(nPeriods, settings);
    }

    protected PeriodBuilder withSettings(Settings settingsToUse) {
        return get(this.nPeriods, settingsToUse);
    }

    protected Period handleCreate(long duration, long referenceDate, boolean inPast) {
        Period period = null;
        int n = 0;
        short uset = this.settings.effectiveSet();
        for (int i = 0; i < TimeUnit.units.length; i++) {
            if (((1 << i) & uset) != 0) {
                TimeUnit unit = TimeUnit.units[i];
                if (n == this.nPeriods) {
                    break;
                }
                long unitDuration = approximateDurationOf(unit);
                if (duration >= unitDuration || n > 0) {
                    n++;
                    double count = ((double) duration) / ((double) unitDuration);
                    if (n < this.nPeriods) {
                        count = Math.floor(count);
                        duration -= (long) (((double) unitDuration) * count);
                    }
                    if (period == null) {
                        period = Period.at((float) count, unit).inPast(inPast);
                    } else {
                        period = period.and((float) count, unit);
                    }
                }
            }
        }
        return period;
    }
}
