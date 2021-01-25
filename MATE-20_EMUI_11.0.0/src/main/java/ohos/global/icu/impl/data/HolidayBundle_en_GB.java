package ohos.global.icu.impl.data;

import java.util.ListResourceBundle;
import ohos.global.icu.util.EasterHoliday;
import ohos.global.icu.util.Holiday;
import ohos.global.icu.util.SimpleHoliday;

public class HolidayBundle_en_GB extends ListResourceBundle {
    private static final Object[][] fContents = {new Object[]{"holidays", fHolidays}, new Object[]{"Labor Day", "Labour Day"}};
    private static final Holiday[] fHolidays = {SimpleHoliday.NEW_YEARS_DAY, SimpleHoliday.MAY_DAY, new SimpleHoliday(4, 31, -2, "Spring Holiday"), new SimpleHoliday(7, 31, -2, "Summer Bank Holiday"), SimpleHoliday.CHRISTMAS, SimpleHoliday.BOXING_DAY, new SimpleHoliday(11, 31, -2, "Christmas Holiday"), EasterHoliday.GOOD_FRIDAY, EasterHoliday.EASTER_SUNDAY, EasterHoliday.EASTER_MONDAY};

    @Override // java.util.ListResourceBundle
    public synchronized Object[][] getContents() {
        return fContents;
    }
}
