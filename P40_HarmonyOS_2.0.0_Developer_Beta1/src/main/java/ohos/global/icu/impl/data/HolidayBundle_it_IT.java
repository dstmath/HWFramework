package ohos.global.icu.impl.data;

import java.util.ListResourceBundle;
import ohos.global.icu.util.EasterHoliday;
import ohos.global.icu.util.Holiday;
import ohos.global.icu.util.SimpleHoliday;

public class HolidayBundle_it_IT extends ListResourceBundle {
    private static final Object[][] fContents = {new Object[]{"holidays", fHolidays}};
    private static final Holiday[] fHolidays = {SimpleHoliday.NEW_YEARS_DAY, SimpleHoliday.EPIPHANY, new SimpleHoliday(3, 1, 0, "Liberation Day"), new SimpleHoliday(4, 1, 0, "Labor Day"), SimpleHoliday.ASSUMPTION, SimpleHoliday.ALL_SAINTS_DAY, SimpleHoliday.IMMACULATE_CONCEPTION, SimpleHoliday.CHRISTMAS, new SimpleHoliday(11, 26, 0, "St. Stephens Day"), SimpleHoliday.NEW_YEARS_EVE, EasterHoliday.EASTER_SUNDAY, EasterHoliday.EASTER_MONDAY};

    @Override // java.util.ListResourceBundle
    public synchronized Object[][] getContents() {
        return fContents;
    }
}
