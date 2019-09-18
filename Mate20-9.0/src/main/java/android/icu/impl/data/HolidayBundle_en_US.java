package android.icu.impl.data;

import android.icu.util.EasterHoliday;
import android.icu.util.Holiday;
import android.icu.util.SimpleHoliday;
import java.util.ListResourceBundle;

public class HolidayBundle_en_US extends ListResourceBundle {
    private static final Object[][] fContents = {new Object[]{"holidays", fHolidays}};
    private static final Holiday[] fHolidays;

    static {
        SimpleHoliday simpleHoliday = new SimpleHoliday(0, 15, 2, "Martin Luther King Day", 1986);
        SimpleHoliday simpleHoliday2 = new SimpleHoliday(1, 15, 2, "Presidents' Day", 1976);
        SimpleHoliday simpleHoliday3 = new SimpleHoliday(1, 22, "Washington's Birthday", 1776, 1975);
        SimpleHoliday simpleHoliday4 = new SimpleHoliday(4, 8, 1, "Mother's Day", 1914);
        SimpleHoliday simpleHoliday5 = new SimpleHoliday(4, 31, -2, "Memorial Day", 1971);
        SimpleHoliday simpleHoliday6 = new SimpleHoliday(4, 30, "Memorial Day", 1868, 1970);
        SimpleHoliday simpleHoliday7 = new SimpleHoliday(5, 15, 1, "Father's Day", 1956);
        SimpleHoliday simpleHoliday8 = new SimpleHoliday(8, 1, 2, "Labor Day", 1894);
        SimpleHoliday simpleHoliday9 = new SimpleHoliday(9, 8, 2, "Columbus Day", 1971);
        SimpleHoliday simpleHoliday10 = new SimpleHoliday(10, 22, 5, "Thanksgiving", 1863);
        fHolidays = new Holiday[]{SimpleHoliday.NEW_YEARS_DAY, simpleHoliday, simpleHoliday2, simpleHoliday3, EasterHoliday.GOOD_FRIDAY, EasterHoliday.EASTER_SUNDAY, simpleHoliday4, simpleHoliday5, simpleHoliday6, simpleHoliday7, new SimpleHoliday(6, 4, "Independence Day", 1776), simpleHoliday8, new SimpleHoliday(10, 2, 3, "Election Day"), simpleHoliday9, new SimpleHoliday(9, 31, "Halloween"), new SimpleHoliday(10, 11, "Veterans' Day", 1918), simpleHoliday10, SimpleHoliday.CHRISTMAS};
    }

    public synchronized Object[][] getContents() {
        return fContents;
    }
}
