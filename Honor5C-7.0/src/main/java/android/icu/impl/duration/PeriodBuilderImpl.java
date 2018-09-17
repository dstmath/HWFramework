package android.icu.impl.duration;

import java.util.TimeZone;

/* compiled from: BasicPeriodBuilderFactory */
abstract class PeriodBuilderImpl implements PeriodBuilder {
    protected Settings settings;

    protected abstract Period handleCreate(long j, long j2, boolean z);

    protected abstract PeriodBuilder withSettings(Settings settings);

    public Period create(long duration) {
        return createWithReferenceDate(duration, System.currentTimeMillis());
    }

    public long approximateDurationOf(TimeUnit unit) {
        return BasicPeriodBuilderFactory.approximateDurationOf(unit);
    }

    public Period createWithReferenceDate(long duration, long referenceDate) {
        boolean inPast = duration < 0;
        if (inPast) {
            duration = -duration;
        }
        Period ts = this.settings.createLimited(duration, inPast);
        if (ts != null) {
            return ts;
        }
        ts = handleCreate(duration, referenceDate, inPast);
        if (ts == null) {
            return Period.lessThan(1.0f, this.settings.effectiveMinUnit()).inPast(inPast);
        }
        return ts;
    }

    public PeriodBuilder withTimeZone(TimeZone timeZone) {
        return this;
    }

    public PeriodBuilder withLocale(String localeName) {
        Settings newSettings = this.settings.setLocale(localeName);
        if (newSettings != this.settings) {
            return withSettings(newSettings);
        }
        return this;
    }

    protected PeriodBuilderImpl(Settings settings) {
        this.settings = settings;
    }
}
