package ohos.global.icu.impl.duration;

import java.util.TimeZone;
import ohos.global.icu.impl.duration.BasicPeriodBuilderFactory;

/* access modifiers changed from: package-private */
/* compiled from: BasicPeriodBuilderFactory */
public abstract class PeriodBuilderImpl implements PeriodBuilder {
    protected BasicPeriodBuilderFactory.Settings settings;

    /* access modifiers changed from: protected */
    public abstract Period handleCreate(long j, long j2, boolean z);

    /* access modifiers changed from: protected */
    public abstract PeriodBuilder withSettings(BasicPeriodBuilderFactory.Settings settings2);

    @Override // ohos.global.icu.impl.duration.PeriodBuilder
    public PeriodBuilder withTimeZone(TimeZone timeZone) {
        return this;
    }

    @Override // ohos.global.icu.impl.duration.PeriodBuilder
    public Period create(long j) {
        return createWithReferenceDate(j, System.currentTimeMillis());
    }

    public long approximateDurationOf(TimeUnit timeUnit) {
        return BasicPeriodBuilderFactory.approximateDurationOf(timeUnit);
    }

    @Override // ohos.global.icu.impl.duration.PeriodBuilder
    public Period createWithReferenceDate(long j, long j2) {
        boolean z = j < 0;
        if (z) {
            j = -j;
        }
        Period createLimited = this.settings.createLimited(j, z);
        if (createLimited != null) {
            return createLimited;
        }
        Period handleCreate = handleCreate(j, j2, z);
        return handleCreate == null ? Period.lessThan(1.0f, this.settings.effectiveMinUnit()).inPast(z) : handleCreate;
    }

    @Override // ohos.global.icu.impl.duration.PeriodBuilder
    public PeriodBuilder withLocale(String str) {
        BasicPeriodBuilderFactory.Settings locale = this.settings.setLocale(str);
        return locale != this.settings ? withSettings(locale) : this;
    }

    protected PeriodBuilderImpl(BasicPeriodBuilderFactory.Settings settings2) {
        this.settings = settings2;
    }
}
