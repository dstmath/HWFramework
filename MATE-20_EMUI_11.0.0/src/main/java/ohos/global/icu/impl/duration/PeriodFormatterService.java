package ohos.global.icu.impl.duration;

import java.util.Collection;

public interface PeriodFormatterService {
    Collection<String> getAvailableLocaleNames();

    DurationFormatterFactory newDurationFormatterFactory();

    PeriodBuilderFactory newPeriodBuilderFactory();

    PeriodFormatterFactory newPeriodFormatterFactory();
}
