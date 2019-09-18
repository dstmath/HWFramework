package android.icu.util;

import android.icu.util.ULocale;
import java.util.Date;
import java.util.Locale;
import java.util.MissingResourceException;

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

    public static Holiday[] getHolidays(ULocale locale) {
        try {
            return (Holiday[]) UResourceBundle.getBundleInstance("android.icu.impl.data.HolidayBundle", locale).getObject("holidays");
        } catch (MissingResourceException e) {
            return noHolidays;
        }
    }

    public Date firstAfter(Date start) {
        return this.rule.firstAfter(start);
    }

    public Date firstBetween(Date start, Date end) {
        return this.rule.firstBetween(start, end);
    }

    public boolean isOn(Date date) {
        return this.rule.isOn(date);
    }

    public boolean isBetween(Date start, Date end) {
        return this.rule.isBetween(start, end);
    }

    protected Holiday(String name2, DateRule rule2) {
        this.name = name2;
        this.rule = rule2;
    }

    public String getDisplayName() {
        return getDisplayName(ULocale.getDefault(ULocale.Category.DISPLAY));
    }

    public String getDisplayName(Locale locale) {
        return getDisplayName(ULocale.forLocale(locale));
    }

    public String getDisplayName(ULocale locale) {
        try {
            return UResourceBundle.getBundleInstance("android.icu.impl.data.HolidayBundle", locale).getString(this.name);
        } catch (MissingResourceException e) {
            return this.name;
        }
    }

    public DateRule getRule() {
        return this.rule;
    }

    public void setRule(DateRule rule2) {
        this.rule = rule2;
    }
}
