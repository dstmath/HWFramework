package ohos.global.icu.impl.data;

import java.util.ListResourceBundle;
import ohos.global.icu.util.Holiday;
import ohos.global.icu.util.SimpleHoliday;

public class HolidayBundle_en_CA extends ListResourceBundle {
    private static final Object[][] fContents = {new Object[]{"holidays", fHolidays}, new Object[]{"Labor Day", "Labour Day"}};
    private static final Holiday[] fHolidays = {SimpleHoliday.NEW_YEARS_DAY, new SimpleHoliday(4, 19, 0, "Victoria Day"), new SimpleHoliday(6, 1, 0, "Canada Day"), new SimpleHoliday(7, 1, 2, "Civic Holiday"), new SimpleHoliday(8, 1, 2, "Labor Day"), new SimpleHoliday(9, 8, 2, "Thanksgiving"), new SimpleHoliday(10, 11, 0, "Remembrance Day"), SimpleHoliday.CHRISTMAS, SimpleHoliday.BOXING_DAY, SimpleHoliday.NEW_YEARS_EVE};

    @Override // java.util.ListResourceBundle
    public synchronized Object[][] getContents() {
        return fContents;
    }
}
