package ohos.global.icu.impl.data;

import java.util.ListResourceBundle;
import ohos.global.icu.util.EasterHoliday;
import ohos.global.icu.util.Holiday;
import ohos.global.icu.util.SimpleHoliday;

public class HolidayBundle_de_AT extends ListResourceBundle {
    private static final Object[][] fContents = {new Object[]{"holidays", fHolidays}, new Object[]{"Christmas", "Christtag"}, new Object[]{"New Year's Day", "Neujahrstag"}};
    private static final Holiday[] fHolidays = {SimpleHoliday.NEW_YEARS_DAY, SimpleHoliday.EPIPHANY, EasterHoliday.GOOD_FRIDAY, EasterHoliday.EASTER_SUNDAY, EasterHoliday.EASTER_MONDAY, EasterHoliday.ASCENSION, EasterHoliday.WHIT_SUNDAY, EasterHoliday.WHIT_MONDAY, EasterHoliday.CORPUS_CHRISTI, SimpleHoliday.ASSUMPTION, SimpleHoliday.ALL_SAINTS_DAY, SimpleHoliday.IMMACULATE_CONCEPTION, SimpleHoliday.CHRISTMAS, SimpleHoliday.ST_STEPHENS_DAY, new SimpleHoliday(4, 1, 0, "National Holiday"), new SimpleHoliday(9, 31, -2, "National Holiday")};

    @Override // java.util.ListResourceBundle
    public synchronized Object[][] getContents() {
        return fContents;
    }
}
