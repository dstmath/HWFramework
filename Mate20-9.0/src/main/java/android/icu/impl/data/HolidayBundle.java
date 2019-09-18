package android.icu.impl.data;

import java.util.ListResourceBundle;

public class HolidayBundle extends ListResourceBundle {
    private static final Object[][] fContents = {new Object[]{"", ""}};

    public synchronized Object[][] getContents() {
        return fContents;
    }
}
