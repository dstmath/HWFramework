package ohos.global.icu.text;

import java.util.Locale;
import ohos.global.icu.impl.ICUResourceBundle;
import ohos.global.icu.util.Calendar;
import ohos.global.icu.util.ChineseCalendar;
import ohos.global.icu.util.ULocale;

@Deprecated
public class ChineseDateFormatSymbols extends DateFormatSymbols {
    static final long serialVersionUID = 6827816119783952890L;
    String[] isLeapMonth;

    @Deprecated
    public ChineseDateFormatSymbols() {
        this(ULocale.getDefault(ULocale.Category.FORMAT));
    }

    @Deprecated
    public ChineseDateFormatSymbols(Locale locale) {
        super(ChineseCalendar.class, ULocale.forLocale(locale));
    }

    @Deprecated
    public ChineseDateFormatSymbols(ULocale uLocale) {
        super(ChineseCalendar.class, uLocale);
    }

    @Deprecated
    public ChineseDateFormatSymbols(Calendar calendar, Locale locale) {
        super((Class<? extends Calendar>) calendar.getClass(), locale);
    }

    @Deprecated
    public ChineseDateFormatSymbols(Calendar calendar, ULocale uLocale) {
        super((Class<? extends Calendar>) calendar.getClass(), uLocale);
    }

    @Deprecated
    public String getLeapMonth(int i) {
        return this.isLeapMonth[i];
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.text.DateFormatSymbols
    @Deprecated
    public void initializeData(ULocale uLocale, ICUResourceBundle iCUResourceBundle, String str) {
        super.initializeData(uLocale, iCUResourceBundle, str);
        initializeIsLeapMonth();
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.global.icu.text.DateFormatSymbols
    public void initializeData(DateFormatSymbols dateFormatSymbols) {
        super.initializeData(dateFormatSymbols);
        if (dateFormatSymbols instanceof ChineseDateFormatSymbols) {
            this.isLeapMonth = ((ChineseDateFormatSymbols) dateFormatSymbols).isLeapMonth;
        } else {
            initializeIsLeapMonth();
        }
    }

    private void initializeIsLeapMonth() {
        this.isLeapMonth = new String[2];
        String[] strArr = this.isLeapMonth;
        String str = "";
        strArr[0] = str;
        if (this.leapMonthPatterns != null) {
            str = this.leapMonthPatterns[0].replace("{0}", str);
        }
        strArr[1] = str;
    }
}
