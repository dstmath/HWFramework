package ohos.global.icu.impl.data;

import java.util.ListResourceBundle;
import ohos.global.icu.util.EasterHoliday;
import ohos.global.icu.util.Holiday;
import ohos.global.icu.util.SimpleHoliday;

public class HolidayBundle_fr_CA extends ListResourceBundle {
    private static final Object[][] fContents = {new Object[]{"holidays", fHolidays}};
    private static final Holiday[] fHolidays = {new SimpleHoliday(0, 1, 0, "New Year's Day"), new SimpleHoliday(4, 19, 0, "Victoria Day"), new SimpleHoliday(5, 24, 0, "National Day"), new SimpleHoliday(6, 1, 0, "Canada Day"), new SimpleHoliday(7, 1, 2, "Civic Holiday"), new SimpleHoliday(8, 1, 2, "Labour Day"), new SimpleHoliday(9, 8, 2, "Thanksgiving"), new SimpleHoliday(10, 11, 0, "Remembrance Day"), SimpleHoliday.CHRISTMAS, SimpleHoliday.BOXING_DAY, SimpleHoliday.NEW_YEARS_EVE, EasterHoliday.GOOD_FRIDAY, EasterHoliday.EASTER_SUNDAY, EasterHoliday.EASTER_MONDAY};

    @Override // java.util.ListResourceBundle
    public synchronized Object[][] getContents() {
        return fContents;
    }
}
