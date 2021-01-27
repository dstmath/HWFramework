package ohos.global.icu.impl;

import ohos.global.icu.text.TimeZoneNames;
import ohos.global.icu.util.ULocale;

public class TimeZoneNamesFactoryImpl extends TimeZoneNames.Factory {
    public TimeZoneNames getTimeZoneNames(ULocale uLocale) {
        return new TimeZoneNamesImpl(uLocale);
    }
}
