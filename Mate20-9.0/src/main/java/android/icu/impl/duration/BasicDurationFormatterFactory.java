package android.icu.impl.duration;

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

    BasicDurationFormatterFactory(BasicPeriodFormatterService ps2) {
        this.ps = ps2;
    }

    public DurationFormatterFactory setPeriodFormatter(PeriodFormatter formatter2) {
        if (formatter2 != this.formatter) {
            this.formatter = formatter2;
            reset();
        }
        return this;
    }

    public DurationFormatterFactory setPeriodBuilder(PeriodBuilder builder2) {
        if (builder2 != this.builder) {
            this.builder = builder2;
            reset();
        }
        return this;
    }

    public DurationFormatterFactory setFallback(DateFormatter fallback2) {
        boolean doReset = false;
        if (fallback2 != null ? !fallback2.equals(this.fallback) : this.fallback != null) {
            doReset = true;
        }
        if (doReset) {
            this.fallback = fallback2;
            reset();
        }
        return this;
    }

    public DurationFormatterFactory setFallbackLimit(long fallbackLimit2) {
        if (fallbackLimit2 < 0) {
            fallbackLimit2 = 0;
        }
        if (fallbackLimit2 != this.fallbackLimit) {
            this.fallbackLimit = fallbackLimit2;
            reset();
        }
        return this;
    }

    public DurationFormatterFactory setLocale(String localeName2) {
        if (!localeName2.equals(this.localeName)) {
            this.localeName = localeName2;
            if (this.builder != null) {
                this.builder = this.builder.withLocale(localeName2);
            }
            if (this.formatter != null) {
                this.formatter = this.formatter.withLocale(localeName2);
            }
            reset();
        }
        return this;
    }

    public DurationFormatterFactory setTimeZone(TimeZone timeZone2) {
        if (!timeZone2.equals(this.timeZone)) {
            this.timeZone = timeZone2;
            if (this.builder != null) {
                this.builder = this.builder.withTimeZone(timeZone2);
            }
            reset();
        }
        return this;
    }

    public DurationFormatter getFormatter() {
        if (this.f == null) {
            if (this.fallback != null) {
                this.fallback = this.fallback.withLocale(this.localeName).withTimeZone(this.timeZone);
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
        BasicDurationFormatter basicDurationFormatter = new BasicDurationFormatter(this.formatter, this.builder, this.fallback, this.fallbackLimit, this.localeName, this.timeZone);
        return basicDurationFormatter;
    }

    /* access modifiers changed from: protected */
    public void reset() {
        this.f = null;
    }
}
