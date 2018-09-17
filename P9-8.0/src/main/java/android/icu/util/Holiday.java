package android.icu.util;

import android.icu.util.ULocale.Category;
import java.util.Date;
import java.util.Locale;
import java.util.MissingResourceException;

public abstract class Holiday implements DateRule {
    private static Holiday[] noHolidays = new Holiday[0];
    private String name;
    private DateRule rule;

    public static Holiday[] getHolidays() {
        return getHolidays(ULocale.getDefault(Category.FORMAT));
    }

    public static Holiday[] getHolidays(Locale locale) {
        return getHolidays(ULocale.forLocale(locale));
    }

    public static Holiday[] getHolidays(ULocale locale) {
        Holiday[] result = noHolidays;
        try {
            return (Holiday[]) UResourceBundle.getBundleInstance("android.icu.impl.data.HolidayBundle", locale).getObject("holidays");
        } catch (MissingResourceException e) {
            return result;
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

    protected Holiday(String name, DateRule rule) {
        this.name = name;
        this.rule = rule;
    }

    public String getDisplayName() {
        return getDisplayName(ULocale.getDefault(Category.DISPLAY));
    }

    public String getDisplayName(Locale locale) {
        return getDisplayName(ULocale.forLocale(locale));
    }

    public String getDisplayName(ULocale locale) {
        String dispName = this.name;
        try {
            return UResourceBundle.getBundleInstance("android.icu.impl.data.HolidayBundle", locale).getString(this.name);
        } catch (MissingResourceException e) {
            return dispName;
        }
    }

    public DateRule getRule() {
        return this.rule;
    }

    public void setRule(DateRule rule) {
        this.rule = rule;
    }
}
