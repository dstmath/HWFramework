package android.icu.impl.data;

import android.icu.util.Holiday;
import android.icu.util.SimpleHoliday;
import java.util.ListResourceBundle;

public class HolidayBundle_ja_JP extends ListResourceBundle {
    private static final Object[][] fContents = {new Object[]{"holidays", fHolidays}};
    private static final Holiday[] fHolidays = {new SimpleHoliday(1, 11, 0, "National Foundation Day")};

    public synchronized Object[][] getContents() {
        return fContents;
    }
}
