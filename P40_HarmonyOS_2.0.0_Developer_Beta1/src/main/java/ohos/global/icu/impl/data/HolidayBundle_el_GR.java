package ohos.global.icu.impl.data;

import java.util.ListResourceBundle;
import ohos.global.icu.util.EasterHoliday;
import ohos.global.icu.util.Holiday;
import ohos.global.icu.util.SimpleHoliday;

public class HolidayBundle_el_GR extends ListResourceBundle {
    private static final Object[][] fContents = {new Object[]{"holidays", fHolidays}};
    private static final Holiday[] fHolidays = {SimpleHoliday.NEW_YEARS_DAY, SimpleHoliday.EPIPHANY, new SimpleHoliday(2, 25, 0, "Independence Day"), SimpleHoliday.MAY_DAY, SimpleHoliday.ASSUMPTION, new SimpleHoliday(9, 28, 0, "Ochi Day"), SimpleHoliday.CHRISTMAS, SimpleHoliday.BOXING_DAY, new EasterHoliday(-2, true, "Good Friday"), new EasterHoliday(0, true, "Easter Sunday"), new EasterHoliday(1, true, "Easter Monday"), new EasterHoliday(50, true, "Whit Monday")};

    @Override // java.util.ListResourceBundle
    public synchronized Object[][] getContents() {
        return fContents;
    }
}
