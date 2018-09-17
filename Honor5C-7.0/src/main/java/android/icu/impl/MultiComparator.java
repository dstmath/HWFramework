package android.icu.impl;

import java.util.Comparator;

public class MultiComparator<T> implements Comparator<T> {
    private Comparator<T>[] comparators;

    public MultiComparator(Comparator<T>... comparators) {
        this.comparators = comparators;
    }

    public int compare(T arg0, T arg1) {
        int i = 0;
        while (i < this.comparators.length) {
            int result = this.comparators[i].compare(arg0, arg1);
            if (result == 0) {
                i++;
            } else if (result > 0) {
                return i + 1;
            } else {
                return -(i + 1);
            }
        }
        return 0;
    }
}
