package android.icu.impl;

import java.util.Comparator;
import java.util.Iterator;

public class IterableComparator<T> implements Comparator<Iterable<T>> {
    private static final IterableComparator NOCOMPARATOR = new IterableComparator();
    private final Comparator<T> comparator;
    private final int shorterFirst;

    public IterableComparator() {
        this(null, true);
    }

    public IterableComparator(Comparator<T> comparator) {
        this(comparator, true);
    }

    public IterableComparator(Comparator<T> comparator, boolean shorterFirst) {
        this.comparator = comparator;
        this.shorterFirst = shorterFirst ? 1 : -1;
    }

    public int compare(Iterable<T> a, Iterable<T> b) {
        int i = 0;
        if (a == null) {
            if (b != null) {
                i = -this.shorterFirst;
            }
            return i;
        } else if (b == null) {
            return this.shorterFirst;
        } else {
            Iterator<T> bi = b.iterator();
            for (T aItem : a) {
                if (!bi.hasNext()) {
                    return this.shorterFirst;
                }
                int result;
                T bItem = bi.next();
                if (this.comparator != null) {
                    result = this.comparator.compare(aItem, bItem);
                    continue;
                } else {
                    result = ((Comparable) aItem).compareTo(bItem);
                    continue;
                }
                if (result != 0) {
                    return result;
                }
            }
            if (bi.hasNext()) {
                i = -this.shorterFirst;
            }
            return i;
        }
    }

    public static <T> int compareIterables(Iterable<T> a, Iterable<T> b) {
        return NOCOMPARATOR.compare((Iterable) a, (Iterable) b);
    }
}
