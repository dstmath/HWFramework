package android.icu.impl.data;

import android.icu.util.EasterHoliday;
import android.icu.util.Holiday;
import android.icu.util.SimpleHoliday;
import java.util.ListResourceBundle;

public class HolidayBundle_en_GB extends ListResourceBundle {
    private static final Object[][] fContents;
    private static final Holiday[] fHolidays = new Holiday[]{SimpleHoliday.NEW_YEARS_DAY, SimpleHoliday.MAY_DAY, new SimpleHoliday(4, 31, -2, "Spring Holiday"), new SimpleHoliday(7, 31, -2, "Summer Bank Holiday"), SimpleHoliday.CHRISTMAS, SimpleHoliday.BOXING_DAY, new SimpleHoliday(11, 31, -2, "Christmas Holiday"), EasterHoliday.GOOD_FRIDAY, EasterHoliday.EASTER_SUNDAY, EasterHoliday.EASTER_MONDAY};

    static {
        r0 = new Object[2][];
        r0[0] = new Object[]{"holidays", fHolidays};
        r0[1] = new Object[]{"Labor Day", "Labour Day"};
        fContents = r0;
    }

    public synchronized Object[][] getContents() {
        return fContents;
    }
}
