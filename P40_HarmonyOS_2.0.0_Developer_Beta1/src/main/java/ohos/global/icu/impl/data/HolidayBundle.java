package ohos.global.icu.impl.data;

import java.util.ListResourceBundle;

public class HolidayBundle extends ListResourceBundle {
    private static final Object[][] fContents = {new Object[]{"", ""}};

    @Override // java.util.ListResourceBundle
    public synchronized Object[][] getContents() {
        return fContents;
    }
}
