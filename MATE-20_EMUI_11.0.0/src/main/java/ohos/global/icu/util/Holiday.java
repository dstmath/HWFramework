package ohos.global.icu.util;

import java.util.Date;
import java.util.Locale;
import java.util.MissingResourceException;
import ohos.global.icu.util.ULocale;

public abstract class Holiday implements DateRule {
    private static Holiday[] noHolidays = new Holiday[0];
    private String name;
    private DateRule rule;

    public static Holiday[] getHolidays() {
        return getHolidays(ULocale.getDefault(ULocale.Category.FORMAT));
    }

    public static Holiday[] getHolidays(Locale locale) {
        return getHolidays(ULocale.forLocale(locale));
    }

    public static Holiday[] getHolidays(ULocale uLocale) {
        try {
            return (Holiday[]) UResourceBundle.getBundleInstance("ohos.global.icu.impl.data.HolidayBundle", uLocale).getObject("holidays");
        } catch (MissingResourceException unused) {
            return noHolidays;
        }
    }

    @Override // ohos.global.icu.util.DateRule
    public Date firstAfter(Date date) {
        return this.rule.firstAfter(date);
    }

    @Override // ohos.global.icu.util.DateRule
    public Date firstBetween(Date date, Date date2) {
        return this.rule.firstBetween(date, date2);
    }

    @Override // ohos.global.icu.util.DateRule
    public boolean isOn(Date date) {
        return this.rule.isOn(date);
    }

    @Override // ohos.global.icu.util.DateRule
    public boolean isBetween(Date date, Date date2) {
        return this.rule.isBetween(date, date2);
    }

    protected Holiday(String str, DateRule dateRule) {
        this.name = str;
        this.rule = dateRule;
    }

    public String getDisplayName() {
        return getDisplayName(ULocale.getDefault(ULocale.Category.DISPLAY));
    }

    public String getDisplayName(Locale locale) {
        return getDisplayName(ULocale.forLocale(locale));
    }

    public String getDisplayName(ULocale uLocale) {
        String str = this.name;
        try {
            return UResourceBundle.getBundleInstance("ohos.global.icu.impl.data.HolidayBundle", uLocale).getString(this.name);
        } catch (MissingResourceException unused) {
            return str;
        }
    }

    public DateRule getRule() {
        return this.rule;
    }

    public void setRule(DateRule dateRule) {
        this.rule = dateRule;
    }
}
