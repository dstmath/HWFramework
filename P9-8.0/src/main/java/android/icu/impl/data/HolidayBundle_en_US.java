package android.icu.impl.data;

import android.icu.util.EasterHoliday;
import android.icu.util.Holiday;
import android.icu.util.SimpleHoliday;
import java.util.ListResourceBundle;

public class HolidayBundle_en_US extends ListResourceBundle {
    private static final Object[][] fContents;
    private static final Holiday[] fHolidays;

    static {
        Holiday[] holidayArr = new Holiday[18];
        holidayArr[0] = SimpleHoliday.NEW_YEARS_DAY;
        holidayArr[1] = new SimpleHoliday(0, 15, 2, "Martin Luther King Day", 1986);
        holidayArr[2] = new SimpleHoliday(1, 15, 2, "Presidents' Day", 1976);
        holidayArr[3] = new SimpleHoliday(1, 22, "Washington's Birthday", 1776, 1975);
        holidayArr[4] = EasterHoliday.GOOD_FRIDAY;
        holidayArr[5] = EasterHoliday.EASTER_SUNDAY;
        holidayArr[6] = new SimpleHoliday(4, 8, 1, "Mother's Day", 1914);
        holidayArr[7] = new SimpleHoliday(4, 31, -2, "Memorial Day", 1971);
        holidayArr[8] = new SimpleHoliday(4, 30, "Memorial Day", 1868, 1970);
        holidayArr[9] = new SimpleHoliday(5, 15, 1, "Father's Day", 1956);
        holidayArr[10] = new SimpleHoliday(6, 4, "Independence Day", 1776);
        holidayArr[11] = new SimpleHoliday(8, 1, 2, "Labor Day", 1894);
        holidayArr[12] = new SimpleHoliday(10, 2, 3, "Election Day");
        holidayArr[13] = new SimpleHoliday(9, 8, 2, "Columbus Day", 1971);
        holidayArr[14] = new SimpleHoliday(9, 31, "Halloween");
        holidayArr[15] = new SimpleHoliday(10, 11, "Veterans' Day", 1918);
        holidayArr[16] = new SimpleHoliday(10, 22, 5, "Thanksgiving", 1863);
        holidayArr[17] = SimpleHoliday.CHRISTMAS;
        fHolidays = holidayArr;
        Object[][] objArr = new Object[1][];
        objArr[0] = new Object[]{"holidays", fHolidays};
        fContents = objArr;
    }

    public synchronized Object[][] getContents() {
        return fContents;
    }
}
