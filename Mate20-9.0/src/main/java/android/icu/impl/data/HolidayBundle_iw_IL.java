package android.icu.impl.data;

import android.icu.util.HebrewHoliday;
import android.icu.util.Holiday;
import java.util.ListResourceBundle;

public class HolidayBundle_iw_IL extends ListResourceBundle {
    private static final Object[][] fContents = {new Object[]{"holidays", fHolidays}};
    private static final Holiday[] fHolidays = {HebrewHoliday.ROSH_HASHANAH, HebrewHoliday.YOM_KIPPUR, HebrewHoliday.HANUKKAH, HebrewHoliday.PURIM, HebrewHoliday.PASSOVER, HebrewHoliday.SHAVUOT, HebrewHoliday.SELIHOT};

    public synchronized Object[][] getContents() {
        return fContents;
    }
}
