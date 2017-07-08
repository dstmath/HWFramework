package android.icu.impl;

import android.icu.text.TimeZoneNames;
import android.icu.text.TimeZoneNames.Factory;
import android.icu.util.ULocale;

public class TimeZoneNamesFactoryImpl extends Factory {
    public TimeZoneNames getTimeZoneNames(ULocale locale) {
        return new TimeZoneNamesImpl(locale);
    }
}
