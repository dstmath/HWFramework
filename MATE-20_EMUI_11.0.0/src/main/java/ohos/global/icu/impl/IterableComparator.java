package ohos.global.icu.impl;

import java.util.Comparator;
import java.util.Iterator;

public class IterableComparator<T> implements Comparator<Iterable<T>> {
    private static final IterableComparator NOCOMPARATOR = new IterableComparator();
    private final Comparator<T> comparator;
    private final int shorterFirst;

    @Override // java.util.Comparator
    public /* bridge */ /* synthetic */ int compare(Object obj, Object obj2) {
        return compare((Iterable) ((Iterable) obj), (Iterable) ((Iterable) obj2));
    }

    public IterableComparator() {
        this(null, true);
    }

    public IterableComparator(Comparator<T> comparator2) {
        this(comparator2, true);
    }

    public IterableComparator(Comparator<T> comparator2, boolean z) {
        this.comparator = comparator2;
        this.shorterFirst = z ? 1 : -1;
    }

    public int compare(Iterable<T> iterable, Iterable<T> iterable2) {
        int i;
        if (iterable == null) {
            if (iterable2 == null) {
                return 0;
            }
            return -this.shorterFirst;
        } else if (iterable2 == null) {
            return this.shorterFirst;
        } else {
            Iterator<T> it = iterable2.iterator();
            for (T t : iterable) {
                if (!it.hasNext()) {
                    return this.shorterFirst;
                }
                T next = it.next();
                Comparator<T> comparator2 = this.comparator;
                if (comparator2 != null) {
                    i = comparator2.compare(t, next);
                    continue;
                } else {
                    i = t.compareTo(next);
                    continue;
                }
                if (i != 0) {
                    return i;
                }
            }
            if (it.hasNext()) {
                return -this.shorterFirst;
            }
            return 0;
        }
    }

    public static <T> int compareIterables(Iterable<T> iterable, Iterable<T> iterable2) {
        return NOCOMPARATOR.compare((Iterable) iterable, (Iterable) iterable2);
    }
}
