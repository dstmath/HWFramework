package android.icu.text;

import android.icu.impl.ICUResourceBundle;
import android.icu.util.Calendar;
import android.icu.util.ChineseCalendar;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Category;
import java.util.Locale;

@Deprecated
public class ChineseDateFormatSymbols extends DateFormatSymbols {
    static final long serialVersionUID = 6827816119783952890L;
    String[] isLeapMonth;

    @Deprecated
    public ChineseDateFormatSymbols() {
        this(ULocale.getDefault(Category.FORMAT));
    }

    @Deprecated
    public ChineseDateFormatSymbols(Locale locale) {
        super(ChineseCalendar.class, ULocale.forLocale(locale));
    }

    @Deprecated
    public ChineseDateFormatSymbols(ULocale locale) {
        super(ChineseCalendar.class, locale);
    }

    @Deprecated
    public ChineseDateFormatSymbols(Calendar cal, Locale locale) {
        super(cal.getClass(), locale);
    }

    @Deprecated
    public ChineseDateFormatSymbols(Calendar cal, ULocale locale) {
        super(cal.getClass(), locale);
    }

    @Deprecated
    public String getLeapMonth(int leap) {
        return this.isLeapMonth[leap];
    }

    @Deprecated
    protected void initializeData(ULocale loc, ICUResourceBundle b, String calendarType) {
        super.initializeData(loc, b, calendarType);
        initializeIsLeapMonth();
    }

    void initializeData(DateFormatSymbols dfs) {
        super.initializeData(dfs);
        if (dfs instanceof ChineseDateFormatSymbols) {
            this.isLeapMonth = ((ChineseDateFormatSymbols) dfs).isLeapMonth;
        } else {
            initializeIsLeapMonth();
        }
    }

    private void initializeIsLeapMonth() {
        this.isLeapMonth = new String[2];
        this.isLeapMonth[0] = "";
        this.isLeapMonth[1] = this.leapMonthPatterns != null ? this.leapMonthPatterns[0].replace("{0}", "") : "";
    }
}
