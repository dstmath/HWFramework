package java.util;

final class DualPivotQuicksort {
    private static final int COUNTING_SORT_THRESHOLD_FOR_BYTE = 29;
    private static final int COUNTING_SORT_THRESHOLD_FOR_SHORT_OR_CHAR = 3200;
    private static final int INSERTION_SORT_THRESHOLD = 47;
    private static final int MAX_RUN_COUNT = 67;
    private static final int MAX_RUN_LENGTH = 33;
    private static final int NUM_BYTE_VALUES = 256;
    private static final int NUM_CHAR_VALUES = 65536;
    private static final int NUM_SHORT_VALUES = 65536;
    private static final int QUICKSORT_THRESHOLD = 286;

    private DualPivotQuicksort() {
    }

    static void sort(int[] a, int left, int right, int[] work, int workBase, int workLen) {
        if (right - left < QUICKSORT_THRESHOLD) {
            sort(a, left, right, true);
            return;
        }
        int lo;
        int hi;
        int[] b;
        int bo;
        int ao;
        int[] run = new int[68];
        int count = 0;
        run[0] = left;
        int k = left;
        while (k < right) {
            if (a[k] >= a[k + 1]) {
                if (a[k] <= a[k + 1]) {
                    int m = MAX_RUN_LENGTH;
                    while (true) {
                        k++;
                        if (k > right || a[k - 1] != a[k]) {
                            break;
                        }
                        m--;
                        if (m == 0) {
                            sort(a, left, right, true);
                            return;
                        }
                    }
                }
                do {
                    k++;
                    if (k > right) {
                        break;
                    }
                } while (a[k - 1] >= a[k]);
                lo = run[count] - 1;
                hi = k;
                while (true) {
                    lo++;
                    hi--;
                    if (lo >= hi) {
                        break;
                    }
                    int t = a[lo];
                    a[lo] = a[hi];
                    a[hi] = t;
                }
            } else {
                do {
                    k++;
                    if (k > right) {
                        break;
                    }
                } while (a[k - 1] <= a[k]);
            }
            count++;
            if (count == MAX_RUN_COUNT) {
                sort(a, left, right, true);
                return;
            }
            run[count] = k;
        }
        int right2 = right + 1;
        if (run[count] == right) {
            count++;
            run[count] = right2;
        } else if (count == 1) {
            return;
        }
        int odd = 0;
        int n = 1;
        while (true) {
            n <<= 1;
            if (n >= count) {
                break;
            }
            byte odd2 = (byte) (odd2 ^ 1);
        }
        int blen = right2 - left;
        if (work == null || workLen < blen || workBase + blen > work.length) {
            work = new int[blen];
            workBase = 0;
        }
        if (odd2 == 0) {
            System.arraycopy(a, left, work, workBase, blen);
            b = a;
            bo = 0;
            a = work;
            ao = workBase - left;
        } else {
            b = work;
            ao = 0;
            bo = workBase - left;
        }
        while (count > 1) {
            int i;
            int last = 0;
            for (k = 2; k <= count; k += 2) {
                hi = run[k];
                int mi = run[k - 1];
                i = run[k - 2];
                int q = mi;
                int p = i;
                while (i < hi) {
                    int p2;
                    int q2;
                    if (q >= hi || (p < mi && a[p + ao] <= a[q + ao])) {
                        p2 = p + 1;
                        b[i + bo] = a[p + ao];
                        q2 = q;
                    } else {
                        q2 = q + 1;
                        b[i + bo] = a[q + ao];
                        p2 = p;
                    }
                    i++;
                    q = q2;
                    p = p2;
                }
                last++;
                run[last] = hi;
            }
            if ((count & 1) != 0) {
                i = right2;
                lo = run[count - 1];
                while (true) {
                    i--;
                    if (i < lo) {
                        break;
                    }
                    b[i + bo] = a[i + ao];
                }
                last++;
                run[last] = right2;
            }
            int[] t2 = a;
            a = b;
            b = t2;
            int o = ao;
            ao = bo;
            bo = o;
            count = last;
        }
    }

    private static void sort(int[] a, int left, int right, boolean leftmost) {
        int length = (right - left) + 1;
        int k;
        if (length < INSERTION_SORT_THRESHOLD) {
            if (leftmost) {
                int i = left;
                int j = left;
                while (i < right) {
                    int ai = a[i + 1];
                    while (ai < a[j]) {
                        a[j + 1] = a[j];
                        int j2 = j - 1;
                        if (j == left) {
                            j = j2;
                            break;
                        }
                        j = j2;
                    }
                    a[j + 1] = ai;
                    i++;
                    j = i;
                }
            } else {
                while (left < right) {
                    left++;
                    if (a[left] < a[left - 1]) {
                        while (true) {
                            k = left;
                            left++;
                            if (left > right) {
                                break;
                            }
                            int a1 = a[k];
                            int a2 = a[left];
                            if (a1 < a2) {
                                a2 = a1;
                                a1 = a[left];
                            }
                            while (true) {
                                k--;
                                if (a1 >= a[k]) {
                                    break;
                                }
                                a[k + 2] = a[k];
                            }
                            k++;
                            a[k + 1] = a1;
                            while (true) {
                                k--;
                                if (a2 >= a[k]) {
                                    break;
                                }
                                a[k + 1] = a[k];
                            }
                            a[k + 1] = a2;
                            left++;
                        }
                        int last = a[right];
                        while (true) {
                            right--;
                            if (last >= a[right]) {
                                break;
                            }
                            a[right + 1] = a[right];
                        }
                        a[right + 1] = last;
                    }
                }
                return;
            }
            return;
        }
        int t;
        int seventh = ((length >> 3) + (length >> 6)) + 1;
        int e3 = (left + right) >>> 1;
        int e2 = e3 - seventh;
        int e1 = e2 - seventh;
        int e4 = e3 + seventh;
        int e5 = e4 + seventh;
        if (a[e2] < a[e1]) {
            t = a[e2];
            a[e2] = a[e1];
            a[e1] = t;
        }
        if (a[e3] < a[e2]) {
            t = a[e3];
            a[e3] = a[e2];
            a[e2] = t;
            if (t < a[e1]) {
                a[e2] = a[e1];
                a[e1] = t;
            }
        }
        if (a[e4] < a[e3]) {
            t = a[e4];
            a[e4] = a[e3];
            a[e3] = t;
            if (t < a[e2]) {
                a[e3] = a[e2];
                a[e2] = t;
                if (t < a[e1]) {
                    a[e2] = a[e1];
                    a[e1] = t;
                }
            }
        }
        if (a[e5] < a[e4]) {
            t = a[e5];
            a[e5] = a[e4];
            a[e4] = t;
            if (t < a[e3]) {
                a[e4] = a[e3];
                a[e3] = t;
                if (t < a[e2]) {
                    a[e3] = a[e2];
                    a[e2] = t;
                    if (t < a[e1]) {
                        a[e2] = a[e1];
                        a[e1] = t;
                    }
                }
            }
        }
        int less = left;
        int great = right;
        int ak;
        if (a[e1] == a[e2] || a[e2] == a[e3] || a[e3] == a[e4] || a[e4] == a[e5]) {
            int pivot = a[e3];
            for (k = left; k <= great; k++) {
                if (a[k] != pivot) {
                    ak = a[k];
                    if (ak < pivot) {
                        a[k] = a[less];
                        a[less] = ak;
                        less++;
                    } else {
                        while (a[great] > pivot) {
                            great--;
                        }
                        if (a[great] < pivot) {
                            a[k] = a[less];
                            a[less] = a[great];
                            less++;
                        } else {
                            a[k] = pivot;
                        }
                        a[great] = ak;
                        great--;
                    }
                }
            }
            sort(a, left, less - 1, leftmost);
            sort(a, great + 1, right, false);
        } else {
            int great2;
            int pivot1 = a[e2];
            int pivot2 = a[e4];
            a[e2] = a[left];
            a[e4] = a[right];
            do {
                less++;
            } while (a[less] < pivot1);
            do {
                great--;
            } while (a[great] > pivot2);
            k = less - 1;
            loop9:
            while (true) {
                k++;
                if (k > great) {
                    break;
                }
                ak = a[k];
                if (ak < pivot1) {
                    a[k] = a[less];
                    a[less] = ak;
                    less++;
                } else if (ak > pivot2) {
                    while (a[great] > pivot2) {
                        great2 = great - 1;
                        if (great == k) {
                            great = great2;
                            break loop9;
                        }
                        great = great2;
                    }
                    if (a[great] < pivot1) {
                        a[k] = a[less];
                        a[less] = a[great];
                        less++;
                    } else {
                        a[k] = a[great];
                    }
                    a[great] = ak;
                    great--;
                } else {
                    continue;
                }
            }
            a[left] = a[less - 1];
            a[less - 1] = pivot1;
            a[right] = a[great + 1];
            a[great + 1] = pivot2;
            sort(a, left, less - 2, leftmost);
            sort(a, great + 2, right, false);
            if (less < e1 && e5 < great) {
                while (a[less] == pivot1) {
                    less++;
                }
                while (a[great] == pivot2) {
                    great--;
                }
                k = less - 1;
                loop13:
                while (true) {
                    k++;
                    if (k > great) {
                        break;
                    }
                    ak = a[k];
                    if (ak == pivot1) {
                        a[k] = a[less];
                        a[less] = ak;
                        less++;
                    } else if (ak == pivot2) {
                        while (a[great] == pivot2) {
                            great2 = great - 1;
                            if (great == k) {
                                great = great2;
                                break loop13;
                            }
                            great = great2;
                        }
                        if (a[great] == pivot1) {
                            a[k] = a[less];
                            a[less] = pivot1;
                            less++;
                        } else {
                            a[k] = a[great];
                        }
                        a[great] = ak;
                        great--;
                    } else {
                        continue;
                    }
                }
            }
            sort(a, less, great, false);
        }
    }

    static void sort(long[] a, int left, int right, long[] work, int workBase, int workLen) {
        if (right - left < QUICKSORT_THRESHOLD) {
            sort(a, left, right, true);
            return;
        }
        int lo;
        int hi;
        long[] b;
        int bo;
        int ao;
        int[] run = new int[68];
        int count = 0;
        run[0] = left;
        int k = left;
        while (k < right) {
            if (a[k] >= a[k + 1]) {
                if (a[k] <= a[k + 1]) {
                    int m = MAX_RUN_LENGTH;
                    while (true) {
                        k++;
                        if (k > right || a[k - 1] != a[k]) {
                            break;
                        }
                        m--;
                        if (m == 0) {
                            sort(a, left, right, true);
                            return;
                        }
                    }
                }
                do {
                    k++;
                    if (k > right) {
                        break;
                    }
                } while (a[k - 1] >= a[k]);
                lo = run[count] - 1;
                hi = k;
                while (true) {
                    lo++;
                    hi--;
                    if (lo >= hi) {
                        break;
                    }
                    long t = a[lo];
                    a[lo] = a[hi];
                    a[hi] = t;
                }
            } else {
                do {
                    k++;
                    if (k > right) {
                        break;
                    }
                } while (a[k - 1] <= a[k]);
            }
            count++;
            if (count == MAX_RUN_COUNT) {
                sort(a, left, right, true);
                return;
            }
            run[count] = k;
        }
        int right2 = right + 1;
        if (run[count] == right) {
            count++;
            run[count] = right2;
        } else if (count == 1) {
            return;
        }
        int odd = 0;
        int n = 1;
        while (true) {
            n <<= 1;
            if (n >= count) {
                break;
            }
            byte odd2 = (byte) (odd2 ^ 1);
        }
        int blen = right2 - left;
        if (work == null || workLen < blen || workBase + blen > work.length) {
            work = new long[blen];
            workBase = 0;
        }
        if (odd2 == 0) {
            System.arraycopy(a, left, work, workBase, blen);
            b = a;
            bo = 0;
            a = work;
            ao = workBase - left;
        } else {
            b = work;
            ao = 0;
            bo = workBase - left;
        }
        while (count > 1) {
            int i;
            int last = 0;
            for (k = 2; k <= count; k += 2) {
                hi = run[k];
                int mi = run[k - 1];
                i = run[k - 2];
                int q = mi;
                int p = i;
                while (i < hi) {
                    int p2;
                    int q2;
                    if (q >= hi || (p < mi && a[p + ao] <= a[q + ao])) {
                        p2 = p + 1;
                        b[i + bo] = a[p + ao];
                        q2 = q;
                    } else {
                        q2 = q + 1;
                        b[i + bo] = a[q + ao];
                        p2 = p;
                    }
                    i++;
                    q = q2;
                    p = p2;
                }
                last++;
                run[last] = hi;
            }
            if ((count & 1) != 0) {
                i = right2;
                lo = run[count - 1];
                while (true) {
                    i--;
                    if (i < lo) {
                        break;
                    }
                    b[i + bo] = a[i + ao];
                }
                last++;
                run[last] = right2;
            }
            long[] t2 = a;
            a = b;
            b = t2;
            int o = ao;
            ao = bo;
            bo = o;
            count = last;
        }
    }

    private static void sort(long[] a, int left, int right, boolean leftmost) {
        int length = (right - left) + 1;
        int k;
        if (length < INSERTION_SORT_THRESHOLD) {
            if (leftmost) {
                int i = left;
                int j = left;
                while (i < right) {
                    long ai = a[i + 1];
                    while (ai < a[j]) {
                        a[j + 1] = a[j];
                        int j2 = j - 1;
                        if (j == left) {
                            j = j2;
                            break;
                        }
                        j = j2;
                    }
                    a[j + 1] = ai;
                    i++;
                    j = i;
                }
            } else {
                while (left < right) {
                    left++;
                    if (a[left] < a[left - 1]) {
                        while (true) {
                            k = left;
                            left++;
                            if (left > right) {
                                break;
                            }
                            long a1 = a[k];
                            long a2 = a[left];
                            if (a1 < a2) {
                                a2 = a1;
                                a1 = a[left];
                            }
                            while (true) {
                                k--;
                                if (a1 >= a[k]) {
                                    break;
                                }
                                a[k + 2] = a[k];
                            }
                            k++;
                            a[k + 1] = a1;
                            while (true) {
                                k--;
                                if (a2 >= a[k]) {
                                    break;
                                }
                                a[k + 1] = a[k];
                            }
                            a[k + 1] = a2;
                            left++;
                        }
                        long last = a[right];
                        while (true) {
                            right--;
                            if (last >= a[right]) {
                                break;
                            }
                            a[right + 1] = a[right];
                        }
                        a[right + 1] = last;
                    }
                }
                return;
            }
            return;
        }
        long t;
        int seventh = ((length >> 3) + (length >> 6)) + 1;
        int e3 = (left + right) >>> 1;
        int e2 = e3 - seventh;
        int e1 = e2 - seventh;
        int e4 = e3 + seventh;
        int e5 = e4 + seventh;
        if (a[e2] < a[e1]) {
            t = a[e2];
            a[e2] = a[e1];
            a[e1] = t;
        }
        if (a[e3] < a[e2]) {
            t = a[e3];
            a[e3] = a[e2];
            a[e2] = t;
            if (t < a[e1]) {
                a[e2] = a[e1];
                a[e1] = t;
            }
        }
        if (a[e4] < a[e3]) {
            t = a[e4];
            a[e4] = a[e3];
            a[e3] = t;
            if (t < a[e2]) {
                a[e3] = a[e2];
                a[e2] = t;
                if (t < a[e1]) {
                    a[e2] = a[e1];
                    a[e1] = t;
                }
            }
        }
        if (a[e5] < a[e4]) {
            t = a[e5];
            a[e5] = a[e4];
            a[e4] = t;
            if (t < a[e3]) {
                a[e4] = a[e3];
                a[e3] = t;
                if (t < a[e2]) {
                    a[e3] = a[e2];
                    a[e2] = t;
                    if (t < a[e1]) {
                        a[e2] = a[e1];
                        a[e1] = t;
                    }
                }
            }
        }
        int less = left;
        int great = right;
        long ak;
        if (a[e1] == a[e2] || a[e2] == a[e3] || a[e3] == a[e4] || a[e4] == a[e5]) {
            long pivot = a[e3];
            for (k = left; k <= great; k++) {
                if (a[k] != pivot) {
                    ak = a[k];
                    if (ak < pivot) {
                        a[k] = a[less];
                        a[less] = ak;
                        less++;
                    } else {
                        while (a[great] > pivot) {
                            great--;
                        }
                        if (a[great] < pivot) {
                            a[k] = a[less];
                            a[less] = a[great];
                            less++;
                        } else {
                            a[k] = pivot;
                        }
                        a[great] = ak;
                        great--;
                    }
                }
            }
            sort(a, left, less - 1, leftmost);
            sort(a, great + 1, right, false);
        } else {
            int great2;
            long pivot1 = a[e2];
            long pivot2 = a[e4];
            a[e2] = a[left];
            a[e4] = a[right];
            do {
                less++;
            } while (a[less] < pivot1);
            do {
                great--;
            } while (a[great] > pivot2);
            k = less - 1;
            loop9:
            while (true) {
                k++;
                if (k > great) {
                    break;
                }
                ak = a[k];
                if (ak < pivot1) {
                    a[k] = a[less];
                    a[less] = ak;
                    less++;
                } else if (ak > pivot2) {
                    while (a[great] > pivot2) {
                        great2 = great - 1;
                        if (great == k) {
                            great = great2;
                            break loop9;
                        }
                        great = great2;
                    }
                    if (a[great] < pivot1) {
                        a[k] = a[less];
                        a[less] = a[great];
                        less++;
                    } else {
                        a[k] = a[great];
                    }
                    a[great] = ak;
                    great--;
                } else {
                    continue;
                }
            }
            a[left] = a[less - 1];
            a[less - 1] = pivot1;
            a[right] = a[great + 1];
            a[great + 1] = pivot2;
            sort(a, left, less - 2, leftmost);
            sort(a, great + 2, right, false);
            if (less < e1 && e5 < great) {
                while (a[less] == pivot1) {
                    less++;
                }
                while (a[great] == pivot2) {
                    great--;
                }
                k = less - 1;
                loop13:
                while (true) {
                    k++;
                    if (k > great) {
                        break;
                    }
                    ak = a[k];
                    if (ak == pivot1) {
                        a[k] = a[less];
                        a[less] = ak;
                        less++;
                    } else if (ak == pivot2) {
                        while (a[great] == pivot2) {
                            great2 = great - 1;
                            if (great == k) {
                                great = great2;
                                break loop13;
                            }
                            great = great2;
                        }
                        if (a[great] == pivot1) {
                            a[k] = a[less];
                            a[less] = pivot1;
                            less++;
                        } else {
                            a[k] = a[great];
                        }
                        a[great] = ak;
                        great--;
                    } else {
                        continue;
                    }
                }
            }
            sort(a, less, great, false);
        }
    }

    static void sort(short[] a, int left, int right, short[] work, int workBase, int workLen) {
        if (right - left > COUNTING_SORT_THRESHOLD_FOR_SHORT_OR_CHAR) {
            int[] count = new int[65536];
            int i = left - 1;
            while (true) {
                i++;
                if (i > right) {
                    break;
                }
                int i2 = a[i] - -32768;
                count[i2] = count[i2] + 1;
            }
            i = 65536;
            int k = right + 1;
            while (k > left) {
                do {
                    i--;
                } while (count[i] == 0);
                short value = (short) (i - 32768);
                int s = count[i];
                while (true) {
                    k--;
                    a[k] = value;
                    s--;
                    if (s > 0) {
                    }
                }
            }
            return;
        }
        doSort(a, left, right, work, workBase, workLen);
    }

    private static void doSort(short[] a, int left, int right, short[] work, int workBase, int workLen) {
        if (right - left < QUICKSORT_THRESHOLD) {
            sort(a, left, right, true);
            return;
        }
        int lo;
        int hi;
        short[] b;
        int bo;
        int ao;
        int[] run = new int[68];
        int count = 0;
        run[0] = left;
        int k = left;
        while (k < right) {
            if (a[k] >= a[k + 1]) {
                if (a[k] <= a[k + 1]) {
                    int m = MAX_RUN_LENGTH;
                    while (true) {
                        k++;
                        if (k > right || a[k - 1] != a[k]) {
                            break;
                        }
                        m--;
                        if (m == 0) {
                            sort(a, left, right, true);
                            return;
                        }
                    }
                }
                do {
                    k++;
                    if (k > right) {
                        break;
                    }
                } while (a[k - 1] >= a[k]);
                lo = run[count] - 1;
                hi = k;
                while (true) {
                    lo++;
                    hi--;
                    if (lo >= hi) {
                        break;
                    }
                    short t = a[lo];
                    a[lo] = a[hi];
                    a[hi] = t;
                }
            } else {
                do {
                    k++;
                    if (k > right) {
                        break;
                    }
                } while (a[k - 1] <= a[k]);
            }
            count++;
            if (count == MAX_RUN_COUNT) {
                sort(a, left, right, true);
                return;
            }
            run[count] = k;
        }
        int right2 = right + 1;
        if (run[count] == right) {
            count++;
            run[count] = right2;
        } else if (count == 1) {
            return;
        }
        int odd = 0;
        int n = 1;
        while (true) {
            n <<= 1;
            if (n >= count) {
                break;
            }
            byte odd2 = (byte) (odd2 ^ 1);
        }
        int blen = right2 - left;
        if (work == null || workLen < blen || workBase + blen > work.length) {
            work = new short[blen];
            workBase = 0;
        }
        if (odd2 == 0) {
            System.arraycopy(a, left, work, workBase, blen);
            b = a;
            bo = 0;
            a = work;
            ao = workBase - left;
        } else {
            b = work;
            ao = 0;
            bo = workBase - left;
        }
        while (count > 1) {
            int i;
            int last = 0;
            for (k = 2; k <= count; k += 2) {
                hi = run[k];
                int mi = run[k - 1];
                i = run[k - 2];
                int q = mi;
                int p = i;
                while (i < hi) {
                    int p2;
                    int q2;
                    if (q >= hi || (p < mi && a[p + ao] <= a[q + ao])) {
                        p2 = p + 1;
                        b[i + bo] = a[p + ao];
                        q2 = q;
                    } else {
                        q2 = q + 1;
                        b[i + bo] = a[q + ao];
                        p2 = p;
                    }
                    i++;
                    q = q2;
                    p = p2;
                }
                last++;
                run[last] = hi;
            }
            if ((count & 1) != 0) {
                i = right2;
                lo = run[count - 1];
                while (true) {
                    i--;
                    if (i < lo) {
                        break;
                    }
                    b[i + bo] = a[i + ao];
                }
                last++;
                run[last] = right2;
            }
            short[] t2 = a;
            a = b;
            b = t2;
            int o = ao;
            ao = bo;
            bo = o;
            count = last;
        }
    }

    private static void sort(short[] a, int left, int right, boolean leftmost) {
        int length = (right - left) + 1;
        int k;
        if (length < INSERTION_SORT_THRESHOLD) {
            if (leftmost) {
                int i = left;
                int j = left;
                while (i < right) {
                    short ai = a[i + 1];
                    while (ai < a[j]) {
                        a[j + 1] = a[j];
                        int j2 = j - 1;
                        if (j == left) {
                            j = j2;
                            break;
                        }
                        j = j2;
                    }
                    a[j + 1] = ai;
                    i++;
                    j = i;
                }
            } else {
                while (left < right) {
                    left++;
                    if (a[left] < a[left - 1]) {
                        while (true) {
                            k = left;
                            left++;
                            if (left > right) {
                                break;
                            }
                            short a1 = a[k];
                            short a2 = a[left];
                            if (a1 < a2) {
                                a2 = a1;
                                a1 = a[left];
                            }
                            while (true) {
                                k--;
                                if (a1 >= a[k]) {
                                    break;
                                }
                                a[k + 2] = a[k];
                            }
                            k++;
                            a[k + 1] = a1;
                            while (true) {
                                k--;
                                if (a2 >= a[k]) {
                                    break;
                                }
                                a[k + 1] = a[k];
                            }
                            a[k + 1] = a2;
                            left++;
                        }
                        short last = a[right];
                        while (true) {
                            right--;
                            if (last >= a[right]) {
                                break;
                            }
                            a[right + 1] = a[right];
                        }
                        a[right + 1] = last;
                    }
                }
                return;
            }
            return;
        }
        short t;
        int seventh = ((length >> 3) + (length >> 6)) + 1;
        int e3 = (left + right) >>> 1;
        int e2 = e3 - seventh;
        int e1 = e2 - seventh;
        int e4 = e3 + seventh;
        int e5 = e4 + seventh;
        if (a[e2] < a[e1]) {
            t = a[e2];
            a[e2] = a[e1];
            a[e1] = t;
        }
        if (a[e3] < a[e2]) {
            t = a[e3];
            a[e3] = a[e2];
            a[e2] = t;
            if (t < a[e1]) {
                a[e2] = a[e1];
                a[e1] = t;
            }
        }
        if (a[e4] < a[e3]) {
            t = a[e4];
            a[e4] = a[e3];
            a[e3] = t;
            if (t < a[e2]) {
                a[e3] = a[e2];
                a[e2] = t;
                if (t < a[e1]) {
                    a[e2] = a[e1];
                    a[e1] = t;
                }
            }
        }
        if (a[e5] < a[e4]) {
            t = a[e5];
            a[e5] = a[e4];
            a[e4] = t;
            if (t < a[e3]) {
                a[e4] = a[e3];
                a[e3] = t;
                if (t < a[e2]) {
                    a[e3] = a[e2];
                    a[e2] = t;
                    if (t < a[e1]) {
                        a[e2] = a[e1];
                        a[e1] = t;
                    }
                }
            }
        }
        int less = left;
        int great = right;
        short ak;
        if (a[e1] == a[e2] || a[e2] == a[e3] || a[e3] == a[e4] || a[e4] == a[e5]) {
            short pivot = a[e3];
            for (k = left; k <= great; k++) {
                if (a[k] != pivot) {
                    ak = a[k];
                    if (ak < pivot) {
                        a[k] = a[less];
                        a[less] = ak;
                        less++;
                    } else {
                        while (a[great] > pivot) {
                            great--;
                        }
                        if (a[great] < pivot) {
                            a[k] = a[less];
                            a[less] = a[great];
                            less++;
                        } else {
                            a[k] = pivot;
                        }
                        a[great] = ak;
                        great--;
                    }
                }
            }
            sort(a, left, less - 1, leftmost);
            sort(a, great + 1, right, false);
        } else {
            int great2;
            short pivot1 = a[e2];
            short pivot2 = a[e4];
            a[e2] = a[left];
            a[e4] = a[right];
            do {
                less++;
            } while (a[less] < pivot1);
            do {
                great--;
            } while (a[great] > pivot2);
            k = less - 1;
            loop9:
            while (true) {
                k++;
                if (k > great) {
                    break;
                }
                ak = a[k];
                if (ak < pivot1) {
                    a[k] = a[less];
                    a[less] = ak;
                    less++;
                } else if (ak > pivot2) {
                    while (a[great] > pivot2) {
                        great2 = great - 1;
                        if (great == k) {
                            great = great2;
                            break loop9;
                        }
                        great = great2;
                    }
                    if (a[great] < pivot1) {
                        a[k] = a[less];
                        a[less] = a[great];
                        less++;
                    } else {
                        a[k] = a[great];
                    }
                    a[great] = ak;
                    great--;
                } else {
                    continue;
                }
            }
            a[left] = a[less - 1];
            a[less - 1] = pivot1;
            a[right] = a[great + 1];
            a[great + 1] = pivot2;
            sort(a, left, less - 2, leftmost);
            sort(a, great + 2, right, false);
            if (less < e1 && e5 < great) {
                while (a[less] == pivot1) {
                    less++;
                }
                while (a[great] == pivot2) {
                    great--;
                }
                k = less - 1;
                loop13:
                while (true) {
                    k++;
                    if (k > great) {
                        break;
                    }
                    ak = a[k];
                    if (ak == pivot1) {
                        a[k] = a[less];
                        a[less] = ak;
                        less++;
                    } else if (ak == pivot2) {
                        while (a[great] == pivot2) {
                            great2 = great - 1;
                            if (great == k) {
                                great = great2;
                                break loop13;
                            }
                            great = great2;
                        }
                        if (a[great] == pivot1) {
                            a[k] = a[less];
                            a[less] = pivot1;
                            less++;
                        } else {
                            a[k] = a[great];
                        }
                        a[great] = ak;
                        great--;
                    } else {
                        continue;
                    }
                }
            }
            sort(a, less, great, false);
        }
    }

    static void sort(char[] a, int left, int right, char[] work, int workBase, int workLen) {
        if (right - left > COUNTING_SORT_THRESHOLD_FOR_SHORT_OR_CHAR) {
            int[] count = new int[65536];
            int i = left - 1;
            while (true) {
                i++;
                if (i > right) {
                    break;
                }
                char c = a[i];
                count[c] = count[c] + 1;
            }
            i = 65536;
            int k = right + 1;
            while (k > left) {
                do {
                    i--;
                } while (count[i] == 0);
                char value = (char) i;
                int s = count[i];
                while (true) {
                    k--;
                    a[k] = value;
                    s--;
                    if (s > 0) {
                    }
                }
            }
            return;
        }
        doSort(a, left, right, work, workBase, workLen);
    }

    private static void doSort(char[] a, int left, int right, char[] work, int workBase, int workLen) {
        if (right - left < QUICKSORT_THRESHOLD) {
            sort(a, left, right, true);
            return;
        }
        int lo;
        int hi;
        char[] b;
        int bo;
        int ao;
        int[] run = new int[68];
        int count = 0;
        run[0] = left;
        int k = left;
        while (k < right) {
            if (a[k] >= a[k + 1]) {
                if (a[k] <= a[k + 1]) {
                    int m = MAX_RUN_LENGTH;
                    while (true) {
                        k++;
                        if (k > right || a[k - 1] != a[k]) {
                            break;
                        }
                        m--;
                        if (m == 0) {
                            sort(a, left, right, true);
                            return;
                        }
                    }
                }
                do {
                    k++;
                    if (k > right) {
                        break;
                    }
                } while (a[k - 1] >= a[k]);
                lo = run[count] - 1;
                hi = k;
                while (true) {
                    lo++;
                    hi--;
                    if (lo >= hi) {
                        break;
                    }
                    char t = a[lo];
                    a[lo] = a[hi];
                    a[hi] = t;
                }
            } else {
                do {
                    k++;
                    if (k > right) {
                        break;
                    }
                } while (a[k - 1] <= a[k]);
            }
            count++;
            if (count == MAX_RUN_COUNT) {
                sort(a, left, right, true);
                return;
            }
            run[count] = k;
        }
        int right2 = right + 1;
        if (run[count] == right) {
            count++;
            run[count] = right2;
        } else if (count == 1) {
            return;
        }
        int odd = 0;
        int n = 1;
        while (true) {
            n <<= 1;
            if (n >= count) {
                break;
            }
            byte odd2 = (byte) (odd2 ^ 1);
        }
        int blen = right2 - left;
        if (work == null || workLen < blen || workBase + blen > work.length) {
            work = new char[blen];
            workBase = 0;
        }
        if (odd2 == 0) {
            System.arraycopy(a, left, work, workBase, blen);
            b = a;
            bo = 0;
            a = work;
            ao = workBase - left;
        } else {
            b = work;
            ao = 0;
            bo = workBase - left;
        }
        while (count > 1) {
            int i;
            int last = 0;
            for (k = 2; k <= count; k += 2) {
                hi = run[k];
                int mi = run[k - 1];
                i = run[k - 2];
                int q = mi;
                int p = i;
                while (i < hi) {
                    int p2;
                    int q2;
                    if (q >= hi || (p < mi && a[p + ao] <= a[q + ao])) {
                        p2 = p + 1;
                        b[i + bo] = a[p + ao];
                        q2 = q;
                    } else {
                        q2 = q + 1;
                        b[i + bo] = a[q + ao];
                        p2 = p;
                    }
                    i++;
                    q = q2;
                    p = p2;
                }
                last++;
                run[last] = hi;
            }
            if ((count & 1) != 0) {
                i = right2;
                lo = run[count - 1];
                while (true) {
                    i--;
                    if (i < lo) {
                        break;
                    }
                    b[i + bo] = a[i + ao];
                }
                last++;
                run[last] = right2;
            }
            char[] t2 = a;
            a = b;
            b = t2;
            int o = ao;
            ao = bo;
            bo = o;
            count = last;
        }
    }

    private static void sort(char[] a, int left, int right, boolean leftmost) {
        int length = (right - left) + 1;
        int k;
        if (length < INSERTION_SORT_THRESHOLD) {
            if (leftmost) {
                int i = left;
                int j = left;
                while (i < right) {
                    char ai = a[i + 1];
                    while (ai < a[j]) {
                        a[j + 1] = a[j];
                        int j2 = j - 1;
                        if (j == left) {
                            j = j2;
                            break;
                        }
                        j = j2;
                    }
                    a[j + 1] = ai;
                    i++;
                    j = i;
                }
            } else {
                while (left < right) {
                    left++;
                    if (a[left] < a[left - 1]) {
                        while (true) {
                            k = left;
                            left++;
                            if (left > right) {
                                break;
                            }
                            char a1 = a[k];
                            char a2 = a[left];
                            if (a1 < a2) {
                                a2 = a1;
                                a1 = a[left];
                            }
                            while (true) {
                                k--;
                                if (a1 >= a[k]) {
                                    break;
                                }
                                a[k + 2] = a[k];
                            }
                            k++;
                            a[k + 1] = a1;
                            while (true) {
                                k--;
                                if (a2 >= a[k]) {
                                    break;
                                }
                                a[k + 1] = a[k];
                            }
                            a[k + 1] = a2;
                            left++;
                        }
                        char last = a[right];
                        while (true) {
                            right--;
                            if (last >= a[right]) {
                                break;
                            }
                            a[right + 1] = a[right];
                        }
                        a[right + 1] = last;
                    }
                }
                return;
            }
            return;
        }
        char t;
        int seventh = ((length >> 3) + (length >> 6)) + 1;
        int e3 = (left + right) >>> 1;
        int e2 = e3 - seventh;
        int e1 = e2 - seventh;
        int e4 = e3 + seventh;
        int e5 = e4 + seventh;
        if (a[e2] < a[e1]) {
            t = a[e2];
            a[e2] = a[e1];
            a[e1] = t;
        }
        if (a[e3] < a[e2]) {
            t = a[e3];
            a[e3] = a[e2];
            a[e2] = t;
            if (t < a[e1]) {
                a[e2] = a[e1];
                a[e1] = t;
            }
        }
        if (a[e4] < a[e3]) {
            t = a[e4];
            a[e4] = a[e3];
            a[e3] = t;
            if (t < a[e2]) {
                a[e3] = a[e2];
                a[e2] = t;
                if (t < a[e1]) {
                    a[e2] = a[e1];
                    a[e1] = t;
                }
            }
        }
        if (a[e5] < a[e4]) {
            t = a[e5];
            a[e5] = a[e4];
            a[e4] = t;
            if (t < a[e3]) {
                a[e4] = a[e3];
                a[e3] = t;
                if (t < a[e2]) {
                    a[e3] = a[e2];
                    a[e2] = t;
                    if (t < a[e1]) {
                        a[e2] = a[e1];
                        a[e1] = t;
                    }
                }
            }
        }
        int less = left;
        int great = right;
        char ak;
        if (a[e1] == a[e2] || a[e2] == a[e3] || a[e3] == a[e4] || a[e4] == a[e5]) {
            char pivot = a[e3];
            for (k = left; k <= great; k++) {
                if (a[k] != pivot) {
                    ak = a[k];
                    if (ak < pivot) {
                        a[k] = a[less];
                        a[less] = ak;
                        less++;
                    } else {
                        while (a[great] > pivot) {
                            great--;
                        }
                        if (a[great] < pivot) {
                            a[k] = a[less];
                            a[less] = a[great];
                            less++;
                        } else {
                            a[k] = pivot;
                        }
                        a[great] = ak;
                        great--;
                    }
                }
            }
            sort(a, left, less - 1, leftmost);
            sort(a, great + 1, right, false);
        } else {
            int great2;
            char pivot1 = a[e2];
            char pivot2 = a[e4];
            a[e2] = a[left];
            a[e4] = a[right];
            do {
                less++;
            } while (a[less] < pivot1);
            do {
                great--;
            } while (a[great] > pivot2);
            k = less - 1;
            loop9:
            while (true) {
                k++;
                if (k > great) {
                    break;
                }
                ak = a[k];
                if (ak < pivot1) {
                    a[k] = a[less];
                    a[less] = ak;
                    less++;
                } else if (ak > pivot2) {
                    while (a[great] > pivot2) {
                        great2 = great - 1;
                        if (great == k) {
                            great = great2;
                            break loop9;
                        }
                        great = great2;
                    }
                    if (a[great] < pivot1) {
                        a[k] = a[less];
                        a[less] = a[great];
                        less++;
                    } else {
                        a[k] = a[great];
                    }
                    a[great] = ak;
                    great--;
                } else {
                    continue;
                }
            }
            a[left] = a[less - 1];
            a[less - 1] = pivot1;
            a[right] = a[great + 1];
            a[great + 1] = pivot2;
            sort(a, left, less - 2, leftmost);
            sort(a, great + 2, right, false);
            if (less < e1 && e5 < great) {
                while (a[less] == pivot1) {
                    less++;
                }
                while (a[great] == pivot2) {
                    great--;
                }
                k = less - 1;
                loop13:
                while (true) {
                    k++;
                    if (k > great) {
                        break;
                    }
                    ak = a[k];
                    if (ak == pivot1) {
                        a[k] = a[less];
                        a[less] = ak;
                        less++;
                    } else if (ak == pivot2) {
                        while (a[great] == pivot2) {
                            great2 = great - 1;
                            if (great == k) {
                                great = great2;
                                break loop13;
                            }
                            great = great2;
                        }
                        if (a[great] == pivot1) {
                            a[k] = a[less];
                            a[less] = pivot1;
                            less++;
                        } else {
                            a[k] = a[great];
                        }
                        a[great] = ak;
                        great--;
                    } else {
                        continue;
                    }
                }
            }
            sort(a, less, great, false);
        }
    }

    static void sort(byte[] a, int left, int right) {
        int i;
        if (right - left > COUNTING_SORT_THRESHOLD_FOR_BYTE) {
            int[] count = new int[256];
            i = left - 1;
            while (true) {
                i++;
                if (i > right) {
                    break;
                }
                int i2 = a[i] + 128;
                count[i2] = count[i2] + 1;
            }
            i = 256;
            int k = right + 1;
            while (k > left) {
                do {
                    i--;
                } while (count[i] == 0);
                byte value = (byte) (i - 128);
                int s = count[i];
                while (true) {
                    k--;
                    a[k] = value;
                    s--;
                    if (s > 0) {
                    }
                }
            }
            return;
        }
        i = left;
        int j = left;
        while (i < right) {
            byte ai = a[i + 1];
            while (ai < a[j]) {
                a[j + 1] = a[j];
                int j2 = j - 1;
                if (j == left) {
                    j = j2;
                    break;
                }
                j = j2;
            }
            a[j + 1] = ai;
            i++;
            j = i;
        }
    }

    static void sort(float[] a, int left, int right, float[] work, int workBase, int workLen) {
        float ak;
        while (left <= right && Float.isNaN(a[right])) {
            right--;
        }
        int k = right;
        while (true) {
            k--;
            if (k < left) {
                break;
            }
            ak = a[k];
            if (ak != ak) {
                a[k] = a[right];
                a[right] = ak;
                right--;
            }
        }
        doSort(a, left, right, work, workBase, workLen);
        int hi = right;
        while (left < hi) {
            int middle = (left + hi) >>> 1;
            if (a[middle] < 0.0f) {
                left = middle + 1;
            } else {
                hi = middle;
            }
        }
        while (left <= right && Float.floatToRawIntBits(a[left]) < 0) {
            left++;
        }
        k = left;
        int p = left - 1;
        while (true) {
            k++;
            if (k <= right) {
                ak = a[k];
                if (ak == 0.0f) {
                    if (Float.floatToRawIntBits(ak) < 0) {
                        a[k] = 0.0f;
                        p++;
                        a[p] = -0.0f;
                    }
                } else {
                    return;
                }
            }
            return;
        }
    }

    private static void doSort(float[] a, int left, int right, float[] work, int workBase, int workLen) {
        if (right - left < QUICKSORT_THRESHOLD) {
            sort(a, left, right, true);
            return;
        }
        int lo;
        int hi;
        float[] b;
        int bo;
        int ao;
        int[] run = new int[68];
        int count = 0;
        run[0] = left;
        int k = left;
        while (k < right) {
            if (a[k] >= a[k + 1]) {
                if (a[k] <= a[k + 1]) {
                    int m = MAX_RUN_LENGTH;
                    while (true) {
                        k++;
                        if (k > right || a[k - 1] != a[k]) {
                            break;
                        }
                        m--;
                        if (m == 0) {
                            sort(a, left, right, true);
                            return;
                        }
                    }
                }
                do {
                    k++;
                    if (k > right) {
                        break;
                    }
                } while (a[k - 1] >= a[k]);
                lo = run[count] - 1;
                hi = k;
                while (true) {
                    lo++;
                    hi--;
                    if (lo >= hi) {
                        break;
                    }
                    float t = a[lo];
                    a[lo] = a[hi];
                    a[hi] = t;
                }
            } else {
                do {
                    k++;
                    if (k > right) {
                        break;
                    }
                } while (a[k - 1] <= a[k]);
            }
            count++;
            if (count == MAX_RUN_COUNT) {
                sort(a, left, right, true);
                return;
            }
            run[count] = k;
        }
        int right2 = right + 1;
        if (run[count] == right) {
            count++;
            run[count] = right2;
        } else if (count == 1) {
            return;
        }
        int odd = 0;
        int n = 1;
        while (true) {
            n <<= 1;
            if (n >= count) {
                break;
            }
            byte odd2 = (byte) (odd2 ^ 1);
        }
        int blen = right2 - left;
        if (work == null || workLen < blen || workBase + blen > work.length) {
            work = new float[blen];
            workBase = 0;
        }
        if (odd2 == 0) {
            System.arraycopy(a, left, work, workBase, blen);
            b = a;
            bo = 0;
            a = work;
            ao = workBase - left;
        } else {
            b = work;
            ao = 0;
            bo = workBase - left;
        }
        while (count > 1) {
            int i;
            int last = 0;
            for (k = 2; k <= count; k += 2) {
                hi = run[k];
                int mi = run[k - 1];
                i = run[k - 2];
                int q = mi;
                int p = i;
                while (i < hi) {
                    int p2;
                    int q2;
                    if (q >= hi || (p < mi && a[p + ao] <= a[q + ao])) {
                        p2 = p + 1;
                        b[i + bo] = a[p + ao];
                        q2 = q;
                    } else {
                        q2 = q + 1;
                        b[i + bo] = a[q + ao];
                        p2 = p;
                    }
                    i++;
                    q = q2;
                    p = p2;
                }
                last++;
                run[last] = hi;
            }
            if ((count & 1) != 0) {
                i = right2;
                lo = run[count - 1];
                while (true) {
                    i--;
                    if (i < lo) {
                        break;
                    }
                    b[i + bo] = a[i + ao];
                }
                last++;
                run[last] = right2;
            }
            float[] t2 = a;
            a = b;
            b = t2;
            int o = ao;
            ao = bo;
            bo = o;
            count = last;
        }
    }

    private static void sort(float[] a, int left, int right, boolean leftmost) {
        int length = (right - left) + 1;
        int k;
        if (length < INSERTION_SORT_THRESHOLD) {
            if (leftmost) {
                int i = left;
                int j = left;
                while (i < right) {
                    float ai = a[i + 1];
                    while (ai < a[j]) {
                        a[j + 1] = a[j];
                        int j2 = j - 1;
                        if (j == left) {
                            j = j2;
                            break;
                        }
                        j = j2;
                    }
                    a[j + 1] = ai;
                    i++;
                    j = i;
                }
            } else {
                while (left < right) {
                    left++;
                    if (a[left] < a[left - 1]) {
                        while (true) {
                            k = left;
                            left++;
                            if (left > right) {
                                break;
                            }
                            float a1 = a[k];
                            float a2 = a[left];
                            if (a1 < a2) {
                                a2 = a1;
                                a1 = a[left];
                            }
                            while (true) {
                                k--;
                                if (a1 >= a[k]) {
                                    break;
                                }
                                a[k + 2] = a[k];
                            }
                            k++;
                            a[k + 1] = a1;
                            while (true) {
                                k--;
                                if (a2 >= a[k]) {
                                    break;
                                }
                                a[k + 1] = a[k];
                            }
                            a[k + 1] = a2;
                            left++;
                        }
                        float last = a[right];
                        while (true) {
                            right--;
                            if (last >= a[right]) {
                                break;
                            }
                            a[right + 1] = a[right];
                        }
                        a[right + 1] = last;
                    }
                }
                return;
            }
            return;
        }
        float t;
        int seventh = ((length >> 3) + (length >> 6)) + 1;
        int e3 = (left + right) >>> 1;
        int e2 = e3 - seventh;
        int e1 = e2 - seventh;
        int e4 = e3 + seventh;
        int e5 = e4 + seventh;
        if (a[e2] < a[e1]) {
            t = a[e2];
            a[e2] = a[e1];
            a[e1] = t;
        }
        if (a[e3] < a[e2]) {
            t = a[e3];
            a[e3] = a[e2];
            a[e2] = t;
            if (t < a[e1]) {
                a[e2] = a[e1];
                a[e1] = t;
            }
        }
        if (a[e4] < a[e3]) {
            t = a[e4];
            a[e4] = a[e3];
            a[e3] = t;
            if (t < a[e2]) {
                a[e3] = a[e2];
                a[e2] = t;
                if (t < a[e1]) {
                    a[e2] = a[e1];
                    a[e1] = t;
                }
            }
        }
        if (a[e5] < a[e4]) {
            t = a[e5];
            a[e5] = a[e4];
            a[e4] = t;
            if (t < a[e3]) {
                a[e4] = a[e3];
                a[e3] = t;
                if (t < a[e2]) {
                    a[e3] = a[e2];
                    a[e2] = t;
                    if (t < a[e1]) {
                        a[e2] = a[e1];
                        a[e1] = t;
                    }
                }
            }
        }
        int less = left;
        int great = right;
        float ak;
        if (a[e1] == a[e2] || a[e2] == a[e3] || a[e3] == a[e4] || a[e4] == a[e5]) {
            float pivot = a[e3];
            for (k = left; k <= great; k++) {
                if (a[k] != pivot) {
                    ak = a[k];
                    if (ak < pivot) {
                        a[k] = a[less];
                        a[less] = ak;
                        less++;
                    } else {
                        while (a[great] > pivot) {
                            great--;
                        }
                        if (a[great] < pivot) {
                            a[k] = a[less];
                            a[less] = a[great];
                            less++;
                        } else {
                            a[k] = a[great];
                        }
                        a[great] = ak;
                        great--;
                    }
                }
            }
            sort(a, left, less - 1, leftmost);
            sort(a, great + 1, right, false);
        } else {
            int great2;
            float pivot1 = a[e2];
            float pivot2 = a[e4];
            a[e2] = a[left];
            a[e4] = a[right];
            do {
                less++;
            } while (a[less] < pivot1);
            do {
                great--;
            } while (a[great] > pivot2);
            k = less - 1;
            loop9:
            while (true) {
                k++;
                if (k > great) {
                    break;
                }
                ak = a[k];
                if (ak < pivot1) {
                    a[k] = a[less];
                    a[less] = ak;
                    less++;
                } else if (ak > pivot2) {
                    while (a[great] > pivot2) {
                        great2 = great - 1;
                        if (great == k) {
                            great = great2;
                            break loop9;
                        }
                        great = great2;
                    }
                    if (a[great] < pivot1) {
                        a[k] = a[less];
                        a[less] = a[great];
                        less++;
                    } else {
                        a[k] = a[great];
                    }
                    a[great] = ak;
                    great--;
                } else {
                    continue;
                }
            }
            a[left] = a[less - 1];
            a[less - 1] = pivot1;
            a[right] = a[great + 1];
            a[great + 1] = pivot2;
            sort(a, left, less - 2, leftmost);
            sort(a, great + 2, right, false);
            if (less < e1 && e5 < great) {
                while (a[less] == pivot1) {
                    less++;
                }
                while (a[great] == pivot2) {
                    great--;
                }
                k = less - 1;
                loop13:
                while (true) {
                    k++;
                    if (k > great) {
                        break;
                    }
                    ak = a[k];
                    if (ak == pivot1) {
                        a[k] = a[less];
                        a[less] = ak;
                        less++;
                    } else if (ak == pivot2) {
                        while (a[great] == pivot2) {
                            great2 = great - 1;
                            if (great == k) {
                                great = great2;
                                break loop13;
                            }
                            great = great2;
                        }
                        if (a[great] == pivot1) {
                            a[k] = a[less];
                            a[less] = a[great];
                            less++;
                        } else {
                            a[k] = a[great];
                        }
                        a[great] = ak;
                        great--;
                    } else {
                        continue;
                    }
                }
            }
            sort(a, less, great, false);
        }
    }

    static void sort(double[] a, int left, int right, double[] work, int workBase, int workLen) {
        double ak;
        while (left <= right && Double.isNaN(a[right])) {
            right--;
        }
        int k = right;
        while (true) {
            k--;
            if (k < left) {
                break;
            }
            ak = a[k];
            if (ak != ak) {
                a[k] = a[right];
                a[right] = ak;
                right--;
            }
        }
        doSort(a, left, right, work, workBase, workLen);
        int hi = right;
        while (left < hi) {
            int middle = (left + hi) >>> 1;
            if (a[middle] < 0.0d) {
                left = middle + 1;
            } else {
                hi = middle;
            }
        }
        while (left <= right && Double.doubleToRawLongBits(a[left]) < 0) {
            left++;
        }
        k = left;
        int p = left - 1;
        while (true) {
            k++;
            if (k <= right) {
                ak = a[k];
                if (ak == 0.0d) {
                    if (Double.doubleToRawLongBits(ak) < 0) {
                        a[k] = 0.0d;
                        p++;
                        a[p] = -0.0d;
                    }
                } else {
                    return;
                }
            }
            return;
        }
    }

    private static void doSort(double[] a, int left, int right, double[] work, int workBase, int workLen) {
        if (right - left < QUICKSORT_THRESHOLD) {
            sort(a, left, right, true);
            return;
        }
        int lo;
        int hi;
        double[] b;
        int bo;
        int ao;
        int[] run = new int[68];
        int count = 0;
        run[0] = left;
        int k = left;
        while (k < right) {
            if (a[k] >= a[k + 1]) {
                if (a[k] <= a[k + 1]) {
                    int m = MAX_RUN_LENGTH;
                    while (true) {
                        k++;
                        if (k > right || a[k - 1] != a[k]) {
                            break;
                        }
                        m--;
                        if (m == 0) {
                            sort(a, left, right, true);
                            return;
                        }
                    }
                }
                do {
                    k++;
                    if (k > right) {
                        break;
                    }
                } while (a[k - 1] >= a[k]);
                lo = run[count] - 1;
                hi = k;
                while (true) {
                    lo++;
                    hi--;
                    if (lo >= hi) {
                        break;
                    }
                    double t = a[lo];
                    a[lo] = a[hi];
                    a[hi] = t;
                }
            } else {
                do {
                    k++;
                    if (k > right) {
                        break;
                    }
                } while (a[k - 1] <= a[k]);
            }
            count++;
            if (count == MAX_RUN_COUNT) {
                sort(a, left, right, true);
                return;
            }
            run[count] = k;
        }
        int right2 = right + 1;
        if (run[count] == right) {
            count++;
            run[count] = right2;
        } else if (count == 1) {
            return;
        }
        int odd = 0;
        int n = 1;
        while (true) {
            n <<= 1;
            if (n >= count) {
                break;
            }
            byte odd2 = (byte) (odd2 ^ 1);
        }
        int blen = right2 - left;
        if (work == null || workLen < blen || workBase + blen > work.length) {
            work = new double[blen];
            workBase = 0;
        }
        if (odd2 == 0) {
            System.arraycopy(a, left, work, workBase, blen);
            b = a;
            bo = 0;
            a = work;
            ao = workBase - left;
        } else {
            b = work;
            ao = 0;
            bo = workBase - left;
        }
        while (count > 1) {
            int i;
            int last = 0;
            for (k = 2; k <= count; k += 2) {
                hi = run[k];
                int mi = run[k - 1];
                i = run[k - 2];
                int q = mi;
                int p = i;
                while (i < hi) {
                    int p2;
                    int q2;
                    if (q >= hi || (p < mi && a[p + ao] <= a[q + ao])) {
                        p2 = p + 1;
                        b[i + bo] = a[p + ao];
                        q2 = q;
                    } else {
                        q2 = q + 1;
                        b[i + bo] = a[q + ao];
                        p2 = p;
                    }
                    i++;
                    q = q2;
                    p = p2;
                }
                last++;
                run[last] = hi;
            }
            if ((count & 1) != 0) {
                i = right2;
                lo = run[count - 1];
                while (true) {
                    i--;
                    if (i < lo) {
                        break;
                    }
                    b[i + bo] = a[i + ao];
                }
                last++;
                run[last] = right2;
            }
            double[] t2 = a;
            a = b;
            b = t2;
            int o = ao;
            ao = bo;
            bo = o;
            count = last;
        }
    }

    private static void sort(double[] a, int left, int right, boolean leftmost) {
        int length = (right - left) + 1;
        int k;
        if (length < INSERTION_SORT_THRESHOLD) {
            if (leftmost) {
                int i = left;
                int j = left;
                while (i < right) {
                    double ai = a[i + 1];
                    while (ai < a[j]) {
                        a[j + 1] = a[j];
                        int j2 = j - 1;
                        if (j == left) {
                            j = j2;
                            break;
                        }
                        j = j2;
                    }
                    a[j + 1] = ai;
                    i++;
                    j = i;
                }
            } else {
                while (left < right) {
                    left++;
                    if (a[left] < a[left - 1]) {
                        while (true) {
                            k = left;
                            left++;
                            if (left > right) {
                                break;
                            }
                            double a1 = a[k];
                            double a2 = a[left];
                            if (a1 < a2) {
                                a2 = a1;
                                a1 = a[left];
                            }
                            while (true) {
                                k--;
                                if (a1 >= a[k]) {
                                    break;
                                }
                                a[k + 2] = a[k];
                            }
                            k++;
                            a[k + 1] = a1;
                            while (true) {
                                k--;
                                if (a2 >= a[k]) {
                                    break;
                                }
                                a[k + 1] = a[k];
                            }
                            a[k + 1] = a2;
                            left++;
                        }
                        double last = a[right];
                        while (true) {
                            right--;
                            if (last >= a[right]) {
                                break;
                            }
                            a[right + 1] = a[right];
                        }
                        a[right + 1] = last;
                    }
                }
                return;
            }
            return;
        }
        double t;
        int seventh = ((length >> 3) + (length >> 6)) + 1;
        int e3 = (left + right) >>> 1;
        int e2 = e3 - seventh;
        int e1 = e2 - seventh;
        int e4 = e3 + seventh;
        int e5 = e4 + seventh;
        if (a[e2] < a[e1]) {
            t = a[e2];
            a[e2] = a[e1];
            a[e1] = t;
        }
        if (a[e3] < a[e2]) {
            t = a[e3];
            a[e3] = a[e2];
            a[e2] = t;
            if (t < a[e1]) {
                a[e2] = a[e1];
                a[e1] = t;
            }
        }
        if (a[e4] < a[e3]) {
            t = a[e4];
            a[e4] = a[e3];
            a[e3] = t;
            if (t < a[e2]) {
                a[e3] = a[e2];
                a[e2] = t;
                if (t < a[e1]) {
                    a[e2] = a[e1];
                    a[e1] = t;
                }
            }
        }
        if (a[e5] < a[e4]) {
            t = a[e5];
            a[e5] = a[e4];
            a[e4] = t;
            if (t < a[e3]) {
                a[e4] = a[e3];
                a[e3] = t;
                if (t < a[e2]) {
                    a[e3] = a[e2];
                    a[e2] = t;
                    if (t < a[e1]) {
                        a[e2] = a[e1];
                        a[e1] = t;
                    }
                }
            }
        }
        int less = left;
        int great = right;
        double ak;
        if (a[e1] == a[e2] || a[e2] == a[e3] || a[e3] == a[e4] || a[e4] == a[e5]) {
            double pivot = a[e3];
            for (k = left; k <= great; k++) {
                if (a[k] != pivot) {
                    ak = a[k];
                    if (ak < pivot) {
                        a[k] = a[less];
                        a[less] = ak;
                        less++;
                    } else {
                        while (a[great] > pivot) {
                            great--;
                        }
                        if (a[great] < pivot) {
                            a[k] = a[less];
                            a[less] = a[great];
                            less++;
                        } else {
                            a[k] = a[great];
                        }
                        a[great] = ak;
                        great--;
                    }
                }
            }
            sort(a, left, less - 1, leftmost);
            sort(a, great + 1, right, false);
        } else {
            int great2;
            double pivot1 = a[e2];
            double pivot2 = a[e4];
            a[e2] = a[left];
            a[e4] = a[right];
            do {
                less++;
            } while (a[less] < pivot1);
            do {
                great--;
            } while (a[great] > pivot2);
            k = less - 1;
            loop9:
            while (true) {
                k++;
                if (k > great) {
                    break;
                }
                ak = a[k];
                if (ak < pivot1) {
                    a[k] = a[less];
                    a[less] = ak;
                    less++;
                } else if (ak > pivot2) {
                    while (a[great] > pivot2) {
                        great2 = great - 1;
                        if (great == k) {
                            great = great2;
                            break loop9;
                        }
                        great = great2;
                    }
                    if (a[great] < pivot1) {
                        a[k] = a[less];
                        a[less] = a[great];
                        less++;
                    } else {
                        a[k] = a[great];
                    }
                    a[great] = ak;
                    great--;
                } else {
                    continue;
                }
            }
            a[left] = a[less - 1];
            a[less - 1] = pivot1;
            a[right] = a[great + 1];
            a[great + 1] = pivot2;
            sort(a, left, less - 2, leftmost);
            sort(a, great + 2, right, false);
            if (less < e1 && e5 < great) {
                while (a[less] == pivot1) {
                    less++;
                }
                while (a[great] == pivot2) {
                    great--;
                }
                k = less - 1;
                loop13:
                while (true) {
                    k++;
                    if (k > great) {
                        break;
                    }
                    ak = a[k];
                    if (ak == pivot1) {
                        a[k] = a[less];
                        a[less] = ak;
                        less++;
                    } else if (ak == pivot2) {
                        while (a[great] == pivot2) {
                            great2 = great - 1;
                            if (great == k) {
                                great = great2;
                                break loop13;
                            }
                            great = great2;
                        }
                        if (a[great] == pivot1) {
                            a[k] = a[less];
                            a[less] = a[great];
                            less++;
                        } else {
                            a[k] = a[great];
                        }
                        a[great] = ak;
                        great--;
                    } else {
                        continue;
                    }
                }
            }
            sort(a, less, great, false);
        }
    }
}
