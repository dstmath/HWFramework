package android.icu.impl.duration;

import java.util.Date;
import java.util.TimeZone;

class BasicDurationFormatter implements DurationFormatter {
    private PeriodBuilder builder;
    private DateFormatter fallback;
    private long fallbackLimit;
    private PeriodFormatter formatter;
    private String localeName;
    private TimeZone timeZone;

    public BasicDurationFormatter(PeriodFormatter formatter, PeriodBuilder builder, DateFormatter fallback, long fallbackLimit) {
        this.formatter = formatter;
        this.builder = builder;
        this.fallback = fallback;
        if (fallbackLimit < 0) {
            fallbackLimit = 0;
        }
        this.fallbackLimit = fallbackLimit;
    }

    protected BasicDurationFormatter(PeriodFormatter formatter, PeriodBuilder builder, DateFormatter fallback, long fallbackLimit, String localeName, TimeZone timeZone) {
        this.formatter = formatter;
        this.builder = builder;
        this.fallback = fallback;
        this.fallbackLimit = fallbackLimit;
        this.localeName = localeName;
        this.timeZone = timeZone;
    }

    public String formatDurationFromNowTo(Date targetDate) {
        long now = System.currentTimeMillis();
        return formatDurationFrom(targetDate.getTime() - now, now);
    }

    public String formatDurationFromNow(long duration) {
        return formatDurationFrom(duration, System.currentTimeMillis());
    }

    public String formatDurationFrom(long duration, long referenceDate) {
        String s = doFallback(duration, referenceDate);
        if (s == null) {
            return doFormat(doBuild(duration, referenceDate));
        }
        return s;
    }

    public DurationFormatter withLocale(String locName) {
        if (locName.equals(this.localeName)) {
            return this;
        }
        DateFormatter newFallback;
        PeriodFormatter newFormatter = this.formatter.withLocale(locName);
        PeriodBuilder newBuilder = this.builder.withLocale(locName);
        if (this.fallback == null) {
            newFallback = null;
        } else {
            newFallback = this.fallback.withLocale(locName);
        }
        return new BasicDurationFormatter(newFormatter, newBuilder, newFallback, this.fallbackLimit, locName, this.timeZone);
    }

    public DurationFormatter withTimeZone(TimeZone tz) {
        if (tz.equals(this.timeZone)) {
            return this;
        }
        DateFormatter newFallback;
        PeriodBuilder newBuilder = this.builder.withTimeZone(tz);
        if (this.fallback == null) {
            newFallback = null;
        } else {
            newFallback = this.fallback.withTimeZone(tz);
        }
        return new BasicDurationFormatter(this.formatter, newBuilder, newFallback, this.fallbackLimit, this.localeName, tz);
    }

    protected String doFallback(long duration, long referenceDate) {
        if (this.fallback == null || this.fallbackLimit <= 0 || Math.abs(duration) < this.fallbackLimit) {
            return null;
        }
        return this.fallback.format(referenceDate + duration);
    }

    protected Period doBuild(long duration, long referenceDate) {
        return this.builder.createWithReferenceDate(duration, referenceDate);
    }

    protected String doFormat(Period period) {
        if (period.isSet()) {
            return this.formatter.format(period);
        }
        throw new IllegalArgumentException("period is not set");
    }
}
