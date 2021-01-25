package org.junit.runner.manipulation;

import java.util.Comparator;
import org.junit.runner.Description;

public class Sorter implements Comparator<Description> {
    public static final Sorter NULL = new Sorter(new Comparator<Description>() {
        /* class org.junit.runner.manipulation.Sorter.AnonymousClass1 */

        public int compare(Description o1, Description o2) {
            return 0;
        }
    });
    private final Comparator<Description> comparator;

    public Sorter(Comparator<Description> comparator2) {
        this.comparator = comparator2;
    }

    public void apply(Object object) {
        if (object instanceof Sortable) {
            ((Sortable) object).sort(this);
        }
    }

    public int compare(Description o1, Description o2) {
        return this.comparator.compare(o1, o2);
    }
}
