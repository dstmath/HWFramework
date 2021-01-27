package ohos.global.icu.impl.duration;

import java.util.Locale;
import java.util.TimeZone;

class BasicDurationFormatterFactory implements DurationFormatterFactory {
    private PeriodBuilder builder;
    private BasicDurationFormatter f;
    private DateFormatter fallback;
    private long fallbackLimit;
    private PeriodFormatter formatter;
    private String localeName = Locale.getDefault().toString();
    private BasicPeriodFormatterService ps;
    private TimeZone timeZone = TimeZone.getDefault();

    BasicDurationFormatterFactory(BasicPeriodFormatterService basicPeriodFormatterService) {
        this.ps = basicPeriodFormatterService;
    }

    @Override // ohos.global.icu.impl.duration.DurationFormatterFactory
    public DurationFormatterFactory setPeriodFormatter(PeriodFormatter periodFormatter) {
        if (periodFormatter != this.formatter) {
            this.formatter = periodFormatter;
            reset();
        }
        return this;
    }

    @Override // ohos.global.icu.impl.duration.DurationFormatterFactory
    public DurationFormatterFactory setPeriodBuilder(PeriodBuilder periodBuilder) {
        if (periodBuilder != this.builder) {
            this.builder = periodBuilder;
            reset();
        }
        return this;
    }

    @Override // ohos.global.icu.impl.duration.DurationFormatterFactory
    public DurationFormatterFactory setFallback(DateFormatter dateFormatter) {
        boolean z = true;
        if (dateFormatter != null ? dateFormatter.equals(this.fallback) : this.fallback == null) {
            z = false;
        }
        if (z) {
            this.fallback = dateFormatter;
            reset();
        }
        return this;
    }

    @Override // ohos.global.icu.impl.duration.DurationFormatterFactory
    public DurationFormatterFactory setFallbackLimit(long j) {
        if (j < 0) {
            j = 0;
        }
        if (j != this.fallbackLimit) {
            this.fallbackLimit = j;
            reset();
        }
        return this;
    }

    @Override // ohos.global.icu.impl.duration.DurationFormatterFactory
    public DurationFormatterFactory setLocale(String str) {
        if (!str.equals(this.localeName)) {
            this.localeName = str;
            PeriodBuilder periodBuilder = this.builder;
            if (periodBuilder != null) {
                this.builder = periodBuilder.withLocale(str);
            }
            PeriodFormatter periodFormatter = this.formatter;
            if (periodFormatter != null) {
                this.formatter = periodFormatter.withLocale(str);
            }
            reset();
        }
        return this;
    }

    @Override // ohos.global.icu.impl.duration.DurationFormatterFactory
    public DurationFormatterFactory setTimeZone(TimeZone timeZone2) {
        if (!timeZone2.equals(this.timeZone)) {
            this.timeZone = timeZone2;
            PeriodBuilder periodBuilder = this.builder;
            if (periodBuilder != null) {
                this.builder = periodBuilder.withTimeZone(timeZone2);
            }
            reset();
        }
        return this;
    }

    @Override // ohos.global.icu.impl.duration.DurationFormatterFactory
    public DurationFormatter getFormatter() {
        if (this.f == null) {
            DateFormatter dateFormatter = this.fallback;
            if (dateFormatter != null) {
                this.fallback = dateFormatter.withLocale(this.localeName).withTimeZone(this.timeZone);
            }
            this.formatter = getPeriodFormatter();
            this.builder = getPeriodBuilder();
            this.f = createFormatter();
        }
        return this.f;
    }

    public PeriodFormatter getPeriodFormatter() {
        if (this.formatter == null) {
            this.formatter = this.ps.newPeriodFormatterFactory().setLocale(this.localeName).getFormatter();
        }
        return this.formatter;
    }

    public PeriodBuilder getPeriodBuilder() {
        if (this.builder == null) {
            this.builder = this.ps.newPeriodBuilderFactory().setLocale(this.localeName).setTimeZone(this.timeZone).getSingleUnitBuilder();
        }
        return this.builder;
    }

    public DateFormatter getFallback() {
        return this.fallback;
    }

    public long getFallbackLimit() {
        if (this.fallback == null) {
            return 0;
        }
        return this.fallbackLimit;
    }

    public String getLocaleName() {
        return this.localeName;
    }

    public TimeZone getTimeZone() {
        return this.timeZone;
    }

    /* access modifiers changed from: protected */
    public BasicDurationFormatter createFormatter() {
        return new BasicDurationFormatter(this.formatter, this.builder, this.fallback, this.fallbackLimit, this.localeName, this.timeZone);
    }

    /* access modifiers changed from: protected */
    public void reset() {
        this.f = null;
    }
}
