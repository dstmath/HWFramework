package ohos.global.icu.impl.duration;

public interface PeriodFormatter {
    String format(Period period);

    PeriodFormatter withLocale(String str);
}
