package ohos.global.icu.impl;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import ohos.global.icu.lang.CharSequences;
import ohos.global.icu.util.ICUException;

public class StringRange {
    public static final Comparator<int[]> COMPARE_INT_ARRAYS = new Comparator<int[]>() {
        /* class ohos.global.icu.impl.StringRange.AnonymousClass1 */

        public int compare(int[] iArr, int[] iArr2) {
            int min = Math.min(iArr.length, iArr2.length);
            for (int i = 0; i < min; i++) {
                int i2 = iArr[i] - iArr2[i];
                if (i2 != 0) {
                    return i2;
                }
            }
            return iArr.length - iArr2.length;
        }
    };
    private static final boolean DEBUG = false;

    public interface Adder {
        void add(String str, String str2);
    }

    public static void compact(Set<String> set, Adder adder, boolean z, boolean z2) {
        int codePointAt;
        if (!z2) {
            String str = null;
            int i = 0;
            int i2 = 0;
            String str2 = null;
            String str3 = null;
            for (String str4 : set) {
                if (str2 != null) {
                    if (str4.regionMatches(0, str2, 0, i) && (codePointAt = str4.codePointAt(i)) == i2 + 1 && str4.length() == Character.charCount(codePointAt) + i) {
                        str3 = str4;
                        i2 = codePointAt;
                    } else {
                        if (str3 == null) {
                            str3 = null;
                        } else if (z) {
                            str3 = str3.substring(i, str3.length());
                        }
                        adder.add(str2, str3);
                    }
                }
                i2 = str4.codePointBefore(str4.length());
                i = str4.length() - Character.charCount(i2);
                str3 = null;
                str2 = str4;
            }
            if (str3 != null) {
                if (!z) {
                    str = str3;
                } else {
                    str = str3.substring(i, str3.length());
                }
            }
            adder.add(str2, str);
            return;
        }
        Relation of = Relation.of(new TreeMap(), TreeSet.class);
        for (String str5 : set) {
            Ranges ranges = new Ranges(str5);
            of.put(ranges.size(), ranges);
        }
        for (Map.Entry entry : of.keyValuesSet()) {
            Iterator<Ranges> it = compact(((Integer) entry.getKey()).intValue(), (Set) entry.getValue()).iterator();
            while (it.hasNext()) {
                Ranges next = it.next();
                adder.add(next.start(), next.end(z));
            }
        }
    }

    public static void compact(Set<String> set, Adder adder, boolean z) {
        compact(set, adder, z, false);
    }

    private static LinkedList<Ranges> compact(int i, Set<Ranges> set) {
        LinkedList<Ranges> linkedList = new LinkedList<>(set);
        for (int i2 = i - 1; i2 >= 0; i2--) {
            Ranges ranges = null;
            Iterator<Ranges> it = linkedList.iterator();
            while (it.hasNext()) {
                Ranges next = it.next();
                if (ranges != null && ranges.merge(i2, next)) {
                    it.remove();
                } else {
                    ranges = next;
                }
            }
        }
        return linkedList;
    }

    /* access modifiers changed from: package-private */
    public static final class Range implements Comparable<Range> {
        int max;
        int min;

        public Range(int i, int i2) {
            this.min = i;
            this.max = i2;
        }

        @Override // java.lang.Object
        public boolean equals(Object obj) {
            return this == obj || (obj != null && (obj instanceof Range) && compareTo((Range) obj) == 0);
        }

        public int compareTo(Range range) {
            int i = this.min - range.min;
            if (i != 0) {
                return i;
            }
            return this.max - range.max;
        }

        @Override // java.lang.Object
        public int hashCode() {
            return (this.min * 37) + this.max;
        }

        @Override // java.lang.Object
        public String toString() {
            StringBuilder appendCodePoint = new StringBuilder().appendCodePoint(this.min);
            if (this.min == this.max) {
                return appendCodePoint.toString();
            }
            appendCodePoint.append('~');
            return appendCodePoint.appendCodePoint(this.max).toString();
        }
    }

    /* access modifiers changed from: package-private */
    public static final class Ranges implements Comparable<Ranges> {
        private final Range[] ranges;

        public Ranges(String str) {
            int[] codePoints = CharSequences.codePoints(str);
            this.ranges = new Range[codePoints.length];
            for (int i = 0; i < codePoints.length; i++) {
                this.ranges[i] = new Range(codePoints[i], codePoints[i]);
            }
        }

        public boolean merge(int i, Ranges ranges2) {
            for (int length = this.ranges.length - 1; length >= 0; length--) {
                if (length == i) {
                    if (this.ranges[length].max != ranges2.ranges[length].min - 1) {
                        return false;
                    }
                } else if (!this.ranges[length].equals(ranges2.ranges[length])) {
                    return false;
                }
            }
            this.ranges[i].max = ranges2.ranges[i].max;
            return true;
        }

        public String start() {
            StringBuilder sb = new StringBuilder();
            int i = 0;
            while (true) {
                Range[] rangeArr = this.ranges;
                if (i >= rangeArr.length) {
                    return sb.toString();
                }
                sb.appendCodePoint(rangeArr[i].min);
                i++;
            }
        }

        public String end(boolean z) {
            int firstDifference = firstDifference();
            if (firstDifference == this.ranges.length) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            if (!z) {
                firstDifference = 0;
            }
            while (true) {
                Range[] rangeArr = this.ranges;
                if (firstDifference >= rangeArr.length) {
                    return sb.toString();
                }
                sb.appendCodePoint(rangeArr[firstDifference].max);
                firstDifference++;
            }
        }

        public int firstDifference() {
            int i = 0;
            while (true) {
                Range[] rangeArr = this.ranges;
                if (i >= rangeArr.length) {
                    return rangeArr.length;
                }
                if (rangeArr[i].min != this.ranges[i].max) {
                    return i;
                }
                i++;
            }
        }

        public Integer size() {
            return Integer.valueOf(this.ranges.length);
        }

        public int compareTo(Ranges ranges2) {
            int length = this.ranges.length - ranges2.ranges.length;
            if (length != 0) {
                return length;
            }
            int i = 0;
            while (true) {
                Range[] rangeArr = this.ranges;
                if (i >= rangeArr.length) {
                    return 0;
                }
                int compareTo = rangeArr[i].compareTo(ranges2.ranges[i]);
                if (compareTo != 0) {
                    return compareTo;
                }
                i++;
            }
        }

        @Override // java.lang.Object
        public String toString() {
            String start = start();
            String end = end(false);
            if (end == null) {
                return start;
            }
            return start + "~" + end;
        }
    }

    public static Collection<String> expand(String str, String str2, boolean z, Collection<String> collection) {
        if (str == null || str2 == null) {
            throw new ICUException("Range must have 2 valid strings");
        }
        int[] codePoints = CharSequences.codePoints(str);
        int[] codePoints2 = CharSequences.codePoints(str2);
        int length = codePoints.length - codePoints2.length;
        if (z && length != 0) {
            throw new ICUException("Range must have equal-length strings");
        } else if (length < 0) {
            throw new ICUException("Range must have start-length ≥ end-length");
        } else if (codePoints2.length != 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < length; i++) {
                sb.appendCodePoint(codePoints[i]);
            }
            add(0, length, codePoints, codePoints2, sb, collection);
            return collection;
        } else {
            throw new ICUException("Range must have end-length > 0");
        }
    }

    private static void add(int i, int i2, int[] iArr, int[] iArr2, StringBuilder sb, Collection<String> collection) {
        int i3 = iArr[i + i2];
        int i4 = iArr2[i];
        if (i3 <= i4) {
            boolean z = i == iArr2.length - 1;
            int length = sb.length();
            for (int i5 = i3; i5 <= i4; i5++) {
                sb.appendCodePoint(i5);
                if (z) {
                    collection.add(sb.toString());
                } else {
                    add(i + 1, i2, iArr, iArr2, sb, collection);
                }
                sb.setLength(length);
            }
            return;
        }
        throw new ICUException("Range must have xᵢ ≤ yᵢ for each index i");
    }
}
