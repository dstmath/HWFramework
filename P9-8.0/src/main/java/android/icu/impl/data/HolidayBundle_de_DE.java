package android.icu.impl.data;

import android.icu.util.EasterHoliday;
import android.icu.util.Holiday;
import android.icu.util.SimpleHoliday;
import java.util.ListResourceBundle;

public class HolidayBundle_de_DE extends ListResourceBundle {
    private static final Object[][] fContents;
    private static final Holiday[] fHolidays = new Holiday[]{SimpleHoliday.NEW_YEARS_DAY, SimpleHoliday.MAY_DAY, new SimpleHoliday(5, 15, 4, "Memorial Day"), new SimpleHoliday(9, 3, 0, "Unity Day"), SimpleHoliday.ALL_SAINTS_DAY, new SimpleHoliday(10, 18, 0, "Day of Prayer and Repentance"), SimpleHoliday.CHRISTMAS, SimpleHoliday.BOXING_DAY, EasterHoliday.GOOD_FRIDAY, EasterHoliday.EASTER_SUNDAY, EasterHoliday.EASTER_MONDAY, EasterHoliday.ASCENSION, EasterHoliday.WHIT_SUNDAY, EasterHoliday.WHIT_MONDAY};

    static {
        Object[][] objArr = new Object[1][];
        objArr[0] = new Object[]{"holidays", fHolidays};
        fContents = objArr;
    }

    public synchronized Object[][] getContents() {
        return fContents;
    }
}
