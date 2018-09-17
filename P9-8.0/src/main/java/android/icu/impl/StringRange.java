package android.icu.impl;

import android.icu.lang.CharSequences;
import android.icu.util.ICUException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class StringRange {
    public static final Comparator<int[]> COMPARE_INT_ARRAYS = new Comparator<int[]>() {
        public int compare(int[] o1, int[] o2) {
            int minIndex = Math.min(o1.length, o2.length);
            for (int i = 0; i < minIndex; i++) {
                int diff = o1[i] - o2[i];
                if (diff != 0) {
                    return diff;
                }
            }
            return o1.length - o2.length;
        }
    };
    private static final boolean DEBUG = false;

    public interface Adder {
        void add(String str, String str2);
    }

    static final class Range implements Comparable<Range> {
        int max;
        int min;

        public Range(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public boolean equals(Object obj) {
            if (this != obj) {
                return obj != null && (obj instanceof Range) && compareTo((Range) obj) == 0;
            } else {
                return true;
            }
        }

        public int compareTo(Range that) {
            int diff = this.min - that.min;
            if (diff != 0) {
                return diff;
            }
            return this.max - that.max;
        }

        public int hashCode() {
            return (this.min * 37) + this.max;
        }

        public String toString() {
            StringBuilder result = new StringBuilder().appendCodePoint(this.min);
            return this.min == this.max ? result.toString() : result.append('~').appendCodePoint(this.max).toString();
        }
    }

    static final class Ranges implements Comparable<Ranges> {
        private final Range[] ranges;

        public Ranges(String s) {
            int[] array = CharSequences.codePoints(s);
            this.ranges = new Range[array.length];
            for (int i = 0; i < array.length; i++) {
                this.ranges[i] = new Range(array[i], array[i]);
            }
        }

        public boolean merge(int pivot, Ranges other) {
            for (int i = this.ranges.length - 1; i >= 0; i--) {
                if (i == pivot) {
                    if (this.ranges[i].max != other.ranges[i].min - 1) {
                        return false;
                    }
                } else if (!this.ranges[i].equals(other.ranges[i])) {
                    return false;
                }
            }
            this.ranges[pivot].max = other.ranges[pivot].max;
            return true;
        }

        public String start() {
            StringBuilder result = new StringBuilder();
            for (Range range : this.ranges) {
                result.appendCodePoint(range.min);
            }
            return result.toString();
        }

        public String end(boolean mostCompact) {
            int firstDiff = firstDifference();
            if (firstDiff == this.ranges.length) {
                return null;
            }
            StringBuilder result = new StringBuilder();
            int i = mostCompact ? firstDiff : 0;
            while (i < this.ranges.length) {
                result.appendCodePoint(this.ranges[i].max);
                i++;
            }
            return result.toString();
        }

        public int firstDifference() {
            for (int i = 0; i < this.ranges.length; i++) {
                if (this.ranges[i].min != this.ranges[i].max) {
                    return i;
                }
            }
            return this.ranges.length;
        }

        public Integer size() {
            return Integer.valueOf(this.ranges.length);
        }

        public int compareTo(Ranges other) {
            int diff = this.ranges.length - other.ranges.length;
            if (diff != 0) {
                return diff;
            }
            for (int i = 0; i < this.ranges.length; i++) {
                diff = this.ranges[i].compareTo(other.ranges[i]);
                if (diff != 0) {
                    return diff;
                }
            }
            return 0;
        }

        public String toString() {
            String start = start();
            String end = end(false);
            return end == null ? start : start + "~" + end;
        }
    }

    public static void compact(Set<String> source, Adder adder, boolean shorterPairs, boolean moreCompact) {
        if (moreCompact) {
            Relation<Integer, Ranges> lengthToArrays = Relation.of(new TreeMap(), TreeSet.class);
            for (String s : source) {
                Ranges item = new Ranges(s);
                lengthToArrays.put(item.size(), item);
            }
            for (Entry<Integer, Set<Ranges>> entry : lengthToArrays.keyValuesSet()) {
                for (Ranges ranges : compact(((Integer) entry.getKey()).intValue(), (Set) entry.getValue())) {
                    adder.add(ranges.start(), ranges.end(shorterPairs));
                }
            }
            return;
        }
        String start = null;
        String end = null;
        int lastCp = 0;
        int prefixLen = 0;
        for (String s2 : source) {
            if (start != null) {
                if (s2.regionMatches(0, start, 0, prefixLen)) {
                    int currentCp = s2.codePointAt(prefixLen);
                    if (currentCp == lastCp + 1 && s2.length() == Character.charCount(currentCp) + prefixLen) {
                        end = s2;
                        lastCp = currentCp;
                    }
                }
                if (end == null) {
                    end = null;
                } else if (shorterPairs) {
                    end = end.substring(prefixLen, end.length());
                }
                adder.add(start, end);
            }
            start = s2;
            end = null;
            lastCp = s2.codePointBefore(s2.length());
            prefixLen = s2.length() - Character.charCount(lastCp);
        }
        if (end == null) {
            end = null;
        } else if (shorterPairs) {
            end = end.substring(prefixLen, end.length());
        }
        adder.add(start, end);
    }

    public static void compact(Set<String> source, Adder adder, boolean shorterPairs) {
        compact(source, adder, shorterPairs, false);
    }

    private static LinkedList<Ranges> compact(int size, Set<Ranges> inputRanges) {
        LinkedList<Ranges> ranges = new LinkedList(inputRanges);
        for (int i = size - 1; i >= 0; i--) {
            Ranges last = null;
            Iterator<Ranges> it = ranges.iterator();
            while (it.hasNext()) {
                Ranges item = (Ranges) it.next();
                if (last == null) {
                    last = item;
                } else if (last.merge(i, item)) {
                    it.remove();
                } else {
                    last = item;
                }
            }
        }
        return ranges;
    }

    public static Collection<String> expand(String start, String end, boolean requireSameLength, Collection<String> output) {
        if (start == null || end == null) {
            throw new ICUException("Range must have 2 valid strings");
        }
        int[] startCps = CharSequences.codePoints(start);
        int[] endCps = CharSequences.codePoints(end);
        int startOffset = startCps.length - endCps.length;
        if (requireSameLength && startOffset != 0) {
            throw new ICUException("Range must have equal-length strings");
        } else if (startOffset < 0) {
            throw new ICUException("Range must have start-length ≥ end-length");
        } else if (endCps.length == 0) {
            throw new ICUException("Range must have end-length > 0");
        } else {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < startOffset; i++) {
                builder.appendCodePoint(startCps[i]);
            }
            add(0, startOffset, startCps, endCps, builder, output);
            return output;
        }
    }

    private static void add(int endIndex, int startOffset, int[] starts, int[] ends, StringBuilder builder, Collection<String> output) {
        int start = starts[endIndex + startOffset];
        int end = ends[endIndex];
        if (start > end) {
            throw new ICUException("Range must have xᵢ ≤ yᵢ for each index i");
        }
        boolean last = endIndex == ends.length + -1;
        int startLen = builder.length();
        for (int i = start; i <= end; i++) {
            builder.appendCodePoint(i);
            if (last) {
                output.add(builder.toString());
            } else {
                add(endIndex + 1, startOffset, starts, ends, builder, output);
            }
            builder.setLength(startLen);
        }
    }
}
