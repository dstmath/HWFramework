package android.icu.impl.data;

import java.util.ListResourceBundle;

public class HolidayBundle_da extends ListResourceBundle {
    private static final Object[][] fContents;

    static {
        r0 = new Object[12][];
        r0[0] = new Object[]{"Armistice Day", "våbenhvile"};
        r0[1] = new Object[]{"Ascension", "himmelfart"};
        r0[2] = new Object[]{"Boxing Day", "anden juledag"};
        r0[3] = new Object[]{"Christmas Eve", "juleaften"};
        r0[4] = new Object[]{"Easter", "påske"};
        r0[5] = new Object[]{"Epiphany", "helligtrekongersdag"};
        r0[6] = new Object[]{"Good Friday", "langfredag"};
        r0[7] = new Object[]{"Halloween", "allehelgensaften"};
        r0[8] = new Object[]{"Maundy Thursday", "skærtorsdag"};
        r0[9] = new Object[]{"Palm Sunday", "palmesøndag"};
        r0[10] = new Object[]{"Pentecost", "pinse"};
        r0[11] = new Object[]{"Shrove Tuesday", "hvidetirsdag"};
        fContents = r0;
    }

    public synchronized Object[][] getContents() {
        return fContents;
    }
}
