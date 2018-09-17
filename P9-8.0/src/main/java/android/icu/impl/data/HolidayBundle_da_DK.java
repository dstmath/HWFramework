package android.icu.impl.data;

import android.icu.util.EasterHoliday;
import android.icu.util.Holiday;
import android.icu.util.SimpleHoliday;
import java.util.ListResourceBundle;

public class HolidayBundle_da_DK extends ListResourceBundle {
    private static final Object[][] fContents;
    private static final Holiday[] fHolidays = new Holiday[]{SimpleHoliday.NEW_YEARS_DAY, new SimpleHoliday(3, 30, -6, "General Prayer Day"), new SimpleHoliday(5, 5, "Constitution Day"), SimpleHoliday.CHRISTMAS_EVE, SimpleHoliday.CHRISTMAS, SimpleHoliday.BOXING_DAY, SimpleHoliday.NEW_YEARS_EVE, EasterHoliday.MAUNDY_THURSDAY, EasterHoliday.GOOD_FRIDAY, EasterHoliday.EASTER_SUNDAY, EasterHoliday.EASTER_MONDAY, EasterHoliday.ASCENSION, EasterHoliday.WHIT_MONDAY};

    static {
        Object[][] objArr = new Object[1][];
        objArr[0] = new Object[]{"holidays", fHolidays};
        fContents = objArr;
    }

    public synchronized Object[][] getContents() {
        return fContents;
    }
}
