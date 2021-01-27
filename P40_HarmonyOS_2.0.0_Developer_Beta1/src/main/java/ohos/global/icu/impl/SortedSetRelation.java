package ohos.global.icu.impl;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

public class SortedSetRelation {
    public static final int A = 6;
    public static final int ADDALL = 7;
    public static final int ANY = 7;
    public static final int A_AND_B = 2;
    public static final int A_NOT_B = 4;
    public static final int B = 3;
    public static final int B_NOT_A = 1;
    public static final int B_REMOVEALL = 1;
    public static final int COMPLEMENTALL = 5;
    public static final int CONTAINS = 6;
    public static final int DISJOINT = 5;
    public static final int EQUALS = 2;
    public static final int ISCONTAINED = 3;
    public static final int NONE = 0;
    public static final int NO_A = 1;
    public static final int NO_B = 4;
    public static final int REMOVEALL = 4;
    public static final int RETAINALL = 2;

    public static <T extends Comparable<? super T>> boolean hasRelation(SortedSet<T> sortedSet, int i, SortedSet<T> sortedSet2) {
        if (i < 0 || i > 7) {
            throw new IllegalArgumentException("Relation " + i + " out of range");
        }
        boolean z = (i & 4) != 0;
        boolean z2 = (i & 2) != 0;
        boolean z3 = (i & 1) != 0;
        if (i != 2) {
            if (i != 3) {
                if (i == 6 && sortedSet.size() < sortedSet2.size()) {
                    return false;
                }
            } else if (sortedSet.size() > sortedSet2.size()) {
                return false;
            }
        } else if (sortedSet.size() != sortedSet2.size()) {
            return false;
        }
        if (sortedSet.size() == 0) {
            if (sortedSet2.size() == 0) {
                return true;
            }
            return z3;
        } else if (sortedSet2.size() == 0) {
            return z;
        } else {
            Iterator<T> it = sortedSet.iterator();
            Iterator<T> it2 = sortedSet2.iterator();
            T next = it.next();
            T next2 = it2.next();
            while (true) {
                int compareTo = next.compareTo(next2);
                if (compareTo == 0) {
                    if (!z2) {
                        return false;
                    }
                    if (!it.hasNext()) {
                        if (!it2.hasNext()) {
                            return true;
                        }
                        return z3;
                    } else if (!it2.hasNext()) {
                        return z;
                    } else {
                        next = it.next();
                        next2 = it2.next();
                    }
                } else if (compareTo < 0) {
                    if (!z) {
                        return false;
                    }
                    if (!it.hasNext()) {
                        return z3;
                    }
                    next = it.next();
                } else if (!z3) {
                    return false;
                } else {
                    if (!it2.hasNext()) {
                        return z;
                    }
                    next2 = it2.next();
                }
            }
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public static <T extends Comparable<? super T>> SortedSet<? extends T> doOperation(SortedSet<T> sortedSet, int i, SortedSet<T> sortedSet2) {
        switch (i) {
            case 0:
                sortedSet.clear();
                return sortedSet;
            case 1:
                TreeSet treeSet = new TreeSet((SortedSet) sortedSet2);
                treeSet.removeAll(sortedSet);
                sortedSet.clear();
                sortedSet.addAll(treeSet);
                return sortedSet;
            case 2:
                sortedSet.retainAll(sortedSet2);
                return sortedSet;
            case 3:
                sortedSet.clear();
                sortedSet.addAll(sortedSet2);
                return sortedSet;
            case 4:
                sortedSet.removeAll(sortedSet2);
                return sortedSet;
            case 5:
                TreeSet treeSet2 = new TreeSet((SortedSet) sortedSet2);
                treeSet2.removeAll(sortedSet);
                sortedSet.removeAll(sortedSet2);
                sortedSet.addAll(treeSet2);
                return sortedSet;
            case 6:
                break;
            case 7:
                sortedSet.addAll(sortedSet2);
                break;
            default:
                throw new IllegalArgumentException("Relation " + i + " out of range");
        }
        return sortedSet;
    }
}
