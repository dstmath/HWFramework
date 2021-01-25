package ohos.global.icu.impl.duration;

import java.util.Collection;
import ohos.global.icu.impl.duration.impl.PeriodFormatterDataService;
import ohos.global.icu.impl.duration.impl.ResourceBasedPeriodFormatterDataService;

public class BasicPeriodFormatterService implements PeriodFormatterService {
    private static BasicPeriodFormatterService instance;
    private PeriodFormatterDataService ds;

    public static BasicPeriodFormatterService getInstance() {
        if (instance == null) {
            instance = new BasicPeriodFormatterService(ResourceBasedPeriodFormatterDataService.getInstance());
        }
        return instance;
    }

    public BasicPeriodFormatterService(PeriodFormatterDataService periodFormatterDataService) {
        this.ds = periodFormatterDataService;
    }

    @Override // ohos.global.icu.impl.duration.PeriodFormatterService
    public DurationFormatterFactory newDurationFormatterFactory() {
        return new BasicDurationFormatterFactory(this);
    }

    @Override // ohos.global.icu.impl.duration.PeriodFormatterService
    public PeriodFormatterFactory newPeriodFormatterFactory() {
        return new BasicPeriodFormatterFactory(this.ds);
    }

    @Override // ohos.global.icu.impl.duration.PeriodFormatterService
    public PeriodBuilderFactory newPeriodBuilderFactory() {
        return new BasicPeriodBuilderFactory(this.ds);
    }

    @Override // ohos.global.icu.impl.duration.PeriodFormatterService
    public Collection<String> getAvailableLocaleNames() {
        return this.ds.getAvailableLocales();
    }
}
