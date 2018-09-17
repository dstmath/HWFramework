package android.icu.impl.data;

import java.util.ListResourceBundle;

public class HolidayBundle_it extends ListResourceBundle {
    private static final Object[][] fContents;

    static {
        Object[][] objArr = new Object[17][];
        objArr[0] = new Object[]{"All Saints' Day", "Ognissanti"};
        objArr[1] = new Object[]{"Armistice Day", "armistizio"};
        objArr[2] = new Object[]{"Ascension", "ascensione"};
        objArr[3] = new Object[]{"Ash Wednesday", "mercoledì delle ceneri"};
        objArr[4] = new Object[]{"Boxing Day", "Santo Stefano"};
        objArr[5] = new Object[]{"Christmas", "natale"};
        objArr[6] = new Object[]{"Easter Sunday", "pasqua"};
        objArr[7] = new Object[]{"Epiphany", "Epifania"};
        objArr[8] = new Object[]{"Good Friday", "venerdì santo"};
        objArr[9] = new Object[]{"Halloween", "vigilia di Ognissanti"};
        objArr[10] = new Object[]{"Maundy Thursday", "giovedì santo"};
        objArr[11] = new Object[]{"New Year's Day", "anno nuovo"};
        objArr[12] = new Object[]{"Palm Sunday", "domenica delle palme"};
        objArr[13] = new Object[]{"Pentecost", "di Pentecoste"};
        objArr[14] = new Object[]{"Shrove Tuesday", "martedi grasso"};
        objArr[15] = new Object[]{"St. Stephen's Day", "Santo Stefano"};
        objArr[16] = new Object[]{"Thanksgiving", "Giorno del Ringraziamento"};
        fContents = objArr;
    }

    public synchronized Object[][] getContents() {
        return fContents;
    }
}
