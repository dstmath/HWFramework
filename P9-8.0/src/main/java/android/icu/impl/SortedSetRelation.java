package android.icu.impl;

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

    public static <T extends Comparable<? super T>> boolean hasRelation(SortedSet<T> a, int allow, SortedSet<T> b) {
        if (allow < 0 || allow > 7) {
            throw new IllegalArgumentException("Relation " + allow + " out of range");
        }
        boolean anb = (allow & 4) != 0;
        boolean ab = (allow & 2) != 0;
        boolean bna = (allow & 1) != 0;
        switch (allow) {
            case 2:
                if (a.size() != b.size()) {
                    return false;
                }
                break;
            case 3:
                if (a.size() > b.size()) {
                    return false;
                }
                break;
            case 6:
                if (a.size() < b.size()) {
                    return false;
                }
                break;
        }
        if (a.size() == 0) {
            if (b.size() == 0) {
                return true;
            }
            return bna;
        } else if (b.size() == 0) {
            return anb;
        } else {
            Iterator<? extends T> ait = a.iterator();
            Iterator<? extends T> bit = b.iterator();
            T aa = ait.next();
            T bb = bit.next();
            while (true) {
                int comp = ((Comparable) aa).compareTo(bb);
                if (comp == 0) {
                    if (!ab) {
                        return false;
                    }
                    if (ait.hasNext()) {
                        if (!bit.hasNext()) {
                            return anb;
                        }
                        aa = ait.next();
                        bb = bit.next();
                    } else if (bit.hasNext()) {
                        return bna;
                    } else {
                        return true;
                    }
                } else if (comp < 0) {
                    if (!anb) {
                        return false;
                    }
                    if (!ait.hasNext()) {
                        return bna;
                    }
                    aa = ait.next();
                } else if (!bna) {
                    return false;
                } else {
                    if (!bit.hasNext()) {
                        return anb;
                    }
                    bb = bit.next();
                }
            }
        }
    }

    public static <T extends Comparable<? super T>> SortedSet<? extends T> doOperation(SortedSet<T> a, int relation, SortedSet<T> b) {
        TreeSet<? extends T> temp;
        switch (relation) {
            case 0:
                a.clear();
                return a;
            case 1:
                temp = new TreeSet(b);
                temp.removeAll(a);
                a.clear();
                a.addAll(temp);
                return a;
            case 2:
                a.retainAll(b);
                return a;
            case 3:
                a.clear();
                a.addAll(b);
                return a;
            case 4:
                a.removeAll(b);
                return a;
            case 5:
                temp = new TreeSet(b);
                temp.removeAll(a);
                a.removeAll(b);
                a.addAll(temp);
                return a;
            case 6:
                return a;
            case 7:
                a.addAll(b);
                return a;
            default:
                throw new IllegalArgumentException("Relation " + relation + " out of range");
        }
    }
}
