package ohos.global.icu.impl.data;

import java.util.ListResourceBundle;
import ohos.global.icu.util.EasterHoliday;
import ohos.global.icu.util.Holiday;
import ohos.global.icu.util.SimpleHoliday;

public class HolidayBundle_da_DK extends ListResourceBundle {
    private static final Object[][] fContents = {new Object[]{"holidays", fHolidays}};
    private static final Holiday[] fHolidays = {SimpleHoliday.NEW_YEARS_DAY, new SimpleHoliday(3, 30, -6, "General Prayer Day"), new SimpleHoliday(5, 5, "Constitution Day"), SimpleHoliday.CHRISTMAS_EVE, SimpleHoliday.CHRISTMAS, SimpleHoliday.BOXING_DAY, SimpleHoliday.NEW_YEARS_EVE, EasterHoliday.MAUNDY_THURSDAY, EasterHoliday.GOOD_FRIDAY, EasterHoliday.EASTER_SUNDAY, EasterHoliday.EASTER_MONDAY, EasterHoliday.ASCENSION, EasterHoliday.WHIT_MONDAY};

    @Override // java.util.ListResourceBundle
    public synchronized Object[][] getContents() {
        return fContents;
    }
}
