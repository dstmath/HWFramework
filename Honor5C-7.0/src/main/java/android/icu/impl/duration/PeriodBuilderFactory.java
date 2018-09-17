package android.icu.impl.duration;

import java.util.TimeZone;

public interface PeriodBuilderFactory {
    PeriodBuilder getFixedUnitBuilder(TimeUnit timeUnit);

    PeriodBuilder getMultiUnitBuilder(int i);

    PeriodBuilder getOneOrTwoUnitBuilder();

    PeriodBuilder getSingleUnitBuilder();

    PeriodBuilderFactory setAllowMilliseconds(boolean z);

    PeriodBuilderFactory setAllowZero(boolean z);

    PeriodBuilderFactory setAvailableUnitRange(TimeUnit timeUnit, TimeUnit timeUnit2);

    PeriodBuilderFactory setLocale(String str);

    PeriodBuilderFactory setMaxLimit(float f);

    PeriodBuilderFactory setMinLimit(float f);

    PeriodBuilderFactory setTimeZone(TimeZone timeZone);

    PeriodBuilderFactory setUnitIsAvailable(TimeUnit timeUnit, boolean z);

    PeriodBuilderFactory setWeeksAloneOnly(boolean z);
}
