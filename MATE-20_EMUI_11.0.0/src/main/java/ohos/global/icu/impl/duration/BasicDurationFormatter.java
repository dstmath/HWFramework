package ohos.global.icu.impl.duration;

import java.util.Date;
import java.util.TimeZone;

class BasicDurationFormatter implements DurationFormatter {
    private PeriodBuilder builder;
    private DateFormatter fallback;
    private long fallbackLimit;
    private PeriodFormatter formatter;
    private String localeName;
    private TimeZone timeZone;

    public BasicDurationFormatter(PeriodFormatter periodFormatter, PeriodBuilder periodBuilder, DateFormatter dateFormatter, long j) {
        this.formatter = periodFormatter;
        this.builder = periodBuilder;
        this.fallback = dateFormatter;
        this.fallbackLimit = j >= 0 ? j : 0;
    }

    protected BasicDurationFormatter(PeriodFormatter periodFormatter, PeriodBuilder periodBuilder, DateFormatter dateFormatter, long j, String str, TimeZone timeZone2) {
        this.formatter = periodFormatter;
        this.builder = periodBuilder;
        this.fallback = dateFormatter;
        this.fallbackLimit = j;
        this.localeName = str;
        this.timeZone = timeZone2;
    }

    @Override // ohos.global.icu.impl.duration.DurationFormatter
    public String formatDurationFromNowTo(Date date) {
        long currentTimeMillis = System.currentTimeMillis();
        return formatDurationFrom(date.getTime() - currentTimeMillis, currentTimeMillis);
    }

    @Override // ohos.global.icu.impl.duration.DurationFormatter
    public String formatDurationFromNow(long j) {
        return formatDurationFrom(j, System.currentTimeMillis());
    }

    @Override // ohos.global.icu.impl.duration.DurationFormatter
    public String formatDurationFrom(long j, long j2) {
        String doFallback = doFallback(j, j2);
        return doFallback == null ? doFormat(doBuild(j, j2)) : doFallback;
    }

    @Override // ohos.global.icu.impl.duration.DurationFormatter
    public DurationFormatter withLocale(String str) {
        DateFormatter dateFormatter;
        if (str.equals(this.localeName)) {
            return this;
        }
        PeriodFormatter withLocale = this.formatter.withLocale(str);
        PeriodBuilder withLocale2 = this.builder.withLocale(str);
        DateFormatter dateFormatter2 = this.fallback;
        if (dateFormatter2 == null) {
            dateFormatter = null;
        } else {
            dateFormatter = dateFormatter2.withLocale(str);
        }
        return new BasicDurationFormatter(withLocale, withLocale2, dateFormatter, this.fallbackLimit, str, this.timeZone);
    }

    @Override // ohos.global.icu.impl.duration.DurationFormatter
    public DurationFormatter withTimeZone(TimeZone timeZone2) {
        DateFormatter dateFormatter;
        if (timeZone2.equals(this.timeZone)) {
            return this;
        }
        PeriodBuilder withTimeZone = this.builder.withTimeZone(timeZone2);
        DateFormatter dateFormatter2 = this.fallback;
        if (dateFormatter2 == null) {
            dateFormatter = null;
        } else {
            dateFormatter = dateFormatter2.withTimeZone(timeZone2);
        }
        return new BasicDurationFormatter(this.formatter, withTimeZone, dateFormatter, this.fallbackLimit, this.localeName, timeZone2);
    }

    /* access modifiers changed from: protected */
    public String doFallback(long j, long j2) {
        if (this.fallback == null || this.fallbackLimit <= 0 || Math.abs(j) < this.fallbackLimit) {
            return null;
        }
        return this.fallback.format(j2 + j);
    }

    /* access modifiers changed from: protected */
    public Period doBuild(long j, long j2) {
        return this.builder.createWithReferenceDate(j, j2);
    }

    /* access modifiers changed from: protected */
    public String doFormat(Period period) {
        if (period.isSet()) {
            return this.formatter.format(period);
        }
        throw new IllegalArgumentException("period is not set");
    }
}
