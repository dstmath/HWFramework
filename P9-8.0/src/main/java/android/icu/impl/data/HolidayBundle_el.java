package android.icu.impl.data;

import java.util.ListResourceBundle;

public class HolidayBundle_el extends ListResourceBundle {
    private static final Object[][] fContents;

    static {
        r0 = new Object[11][];
        r0[0] = new Object[]{"Assumption", "15 Αύγούστου"};
        r0[1] = new Object[]{"Boxing Day", "Δεύτερη μέρα τών Χριστουγέννων"};
        r0[2] = new Object[]{"Christmas", "Χριστούγεννα"};
        r0[3] = new Object[]{"Clean Monday", "Καθαρή Δευτέρα"};
        r0[4] = new Object[]{"Easter Monday", "Δεύτερη μέρα τού Πάσχα"};
        r0[5] = new Object[]{"Epiphany", "Έπιφάνεια"};
        r0[6] = new Object[]{"Good Friday", "Μεγάλη Παρασκευή"};
        r0[7] = new Object[]{"May Day", "Πρωτομαγιά"};
        r0[8] = new Object[]{"New Year's Day", "Πρωτοχρονιά"};
        r0[9] = new Object[]{"Ochi Day", "28 Όκτωβρίου"};
        r0[10] = new Object[]{"Whit Monday", "Δεύτερη μέρα τού Πεντηκοστή"};
        fContents = r0;
    }

    public synchronized Object[][] getContents() {
        return fContents;
    }
}
