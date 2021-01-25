package ohos.global.icu.impl.data;

import java.util.ListResourceBundle;
import ohos.global.icu.util.Holiday;
import ohos.global.icu.util.SimpleHoliday;

public class HolidayBundle_ja_JP extends ListResourceBundle {
    private static final Object[][] fContents = {new Object[]{"holidays", fHolidays}};
    private static final Holiday[] fHolidays = {new SimpleHoliday(1, 11, 0, "National Foundation Day")};

    @Override // java.util.ListResourceBundle
    public synchronized Object[][] getContents() {
        return fContents;
    }
}
