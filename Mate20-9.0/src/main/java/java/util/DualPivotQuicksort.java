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

    /* JADX WARNING: Code restructure failed: missing block: B:68:0x00f2, code lost:
        if (r0[r3 + r14] <= r0[r6 + r14]) goto L_0x0105;
     */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x00ae  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00b7  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x00bd  */
    static void sort(int[] a, int left, int right, int[] work, int workBase, int workLen) {
        int workBase2;
        int ao;
        int bo;
        int[] b;
        int mi;
        int[] a2 = a;
        int i = left;
        int i2 = right;
        int[] work2 = work;
        int i3 = 1;
        if (i2 - i < QUICKSORT_THRESHOLD) {
            sort(a2, i, i2, true);
            return;
        }
        int[] run = new int[68];
        int i4 = 0;
        run[0] = i;
        int count = 0;
        int k = i;
        while (k < i2) {
            if (a2[k] >= a2[k + 1]) {
                if (a2[k] <= a2[k + 1]) {
                    int m = MAX_RUN_LENGTH;
                    while (true) {
                        k++;
                        if (k > i2 || a2[k - 1] != a2[k]) {
                            break;
                        }
                        m--;
                        if (m == 0) {
                            sort(a2, i, i2, true);
                            return;
                        }
                    }
                } else {
                    do {
                        k++;
                        if (k > i2) {
                            break;
                        }
                    } while (a2[k - 1] >= a2[k]);
                    int lo = run[count] - 1;
                    int hi = k;
                    while (true) {
                        lo++;
                        hi--;
                        if (lo >= hi) {
                            break;
                        }
                        int t = a2[lo];
                        a2[lo] = a2[hi];
                        a2[hi] = t;
                    }
                }
            } else {
                do {
                    k++;
                    if (k > i2) {
                        break;
                    }
                } while (a2[k - 1] <= a2[k]);
            }
            count++;
            if (count == MAX_RUN_COUNT) {
                sort(a2, i, i2, true);
                return;
            }
            run[count] = k;
        }
        int right2 = i2 + 1;
        if (run[count] == i2) {
            count++;
            run[count] = right2;
        } else if (count == 1) {
            return;
        }
        byte odd = 0;
        int n = 1;
        while (true) {
            int i5 = n << 1;
            n = i5;
            if (i5 >= count) {
                break;
            }
            odd = (byte) (odd ^ 1);
        }
        int blen = right2 - i;
        if (work2 == null) {
            int i6 = workLen;
        } else if (workLen >= blen && workBase + blen <= work2.length) {
            workBase2 = workBase;
            if (odd != 0) {
                System.arraycopy((Object) a2, i, (Object) work2, workBase2, blen);
                b = a2;
                bo = 0;
                a2 = work2;
                ao = workBase2 - i;
            } else {
                b = work2;
                ao = 0;
                bo = workBase2 - i;
            }
            while (count > i3) {
                int last = i4;
                int k2 = 2;
                while (true) {
                    int k3 = k2;
                    if (k3 > count) {
                        break;
                    }
                    int hi2 = run[k3];
                    int mi2 = run[k3 - 1];
                    int i7 = run[k3 - 2];
                    int blen2 = blen;
                    int[] work3 = work2;
                    int i8 = i7;
                    int p = i7;
                    int q = mi2;
                    while (true) {
                        int q2 = q;
                        if (i8 >= hi2) {
                            break;
                        }
                        byte odd2 = odd;
                        int q3 = q2;
                        if (q3 < hi2) {
                            if (p < mi2) {
                                mi = mi2;
                            } else {
                                mi = mi2;
                            }
                            b[i8 + bo] = a2[q3 + ao];
                            q = q3 + 1;
                            i8++;
                            odd = odd2;
                            mi2 = mi;
                            int i9 = workLen;
                        } else {
                            mi = mi2;
                        }
                        b[i8 + bo] = a2[p + ao];
                        q = q3;
                        p++;
                        i8++;
                        odd = odd2;
                        mi2 = mi;
                        int i92 = workLen;
                    }
                    byte b2 = odd;
                    last++;
                    run[last] = hi2;
                    k2 = k3 + 2;
                    blen = blen2;
                    work2 = work3;
                    int i10 = left;
                    int i11 = workLen;
                }
                int blen3 = blen;
                int[] work4 = work2;
                byte odd3 = odd;
                if ((count & 1) != 0) {
                    int i12 = right2;
                    int lo2 = run[count - 1];
                    while (true) {
                        i12--;
                        if (i12 < lo2) {
                            break;
                        }
                        b[i12 + bo] = a2[i12 + ao];
                    }
                    last++;
                    run[last] = right2;
                }
                int[] t2 = a2;
                a2 = b;
                b = t2;
                int o = ao;
                ao = bo;
                bo = o;
                count = last;
                blen = blen3;
                work2 = work4;
                odd = odd3;
                int i13 = left;
                i3 = 1;
                i4 = 0;
                int i14 = workLen;
            }
            int[] iArr = work2;
            byte b3 = odd;
        }
        work2 = new int[blen];
        workBase2 = 0;
        if (odd != 0) {
        }
        while (count > i3) {
        }
        int[] iArr2 = work2;
        byte b32 = odd;
    }

    private static void sort(int[] a, int left, int right, boolean leftmost) {
        int seventh;
        int left2;
        int[] iArr = a;
        int k = left;
        int right2 = right;
        boolean z = leftmost;
        int length = (right2 - k) + 1;
        if (length < INSERTION_SORT_THRESHOLD) {
            if (z) {
                int j = k;
                int i = j;
                while (i < right2) {
                    int ai = iArr[i + 1];
                    while (true) {
                        if (ai >= iArr[j]) {
                            break;
                        }
                        iArr[j + 1] = iArr[j];
                        int j2 = j - 1;
                        if (j == k) {
                            j = j2;
                            break;
                        }
                        j = j2;
                    }
                    iArr[j + 1] = ai;
                    i++;
                    j = i;
                }
            } else {
                while (k < right2) {
                    k++;
                    if (iArr[k] < iArr[k - 1]) {
                        int left3 = k;
                        while (true) {
                            left2 = left3 + 1;
                            if (left2 > right2) {
                                break;
                            }
                            int a1 = iArr[k];
                            int a2 = iArr[left2];
                            if (a1 < a2) {
                                a2 = a1;
                                a1 = iArr[left2];
                            }
                            while (true) {
                                k--;
                                if (a1 >= iArr[k]) {
                                    break;
                                }
                                iArr[k + 2] = iArr[k];
                            }
                            int k2 = k + 1;
                            iArr[k2 + 1] = a1;
                            while (true) {
                                k2--;
                                if (a2 >= iArr[k2]) {
                                    break;
                                }
                                iArr[k2 + 1] = iArr[k2];
                            }
                            iArr[k2 + 1] = a2;
                            left3 = left2 + 1;
                            k = left3;
                        }
                        int last = iArr[right2];
                        while (true) {
                            right2--;
                            if (last >= iArr[right2]) {
                                break;
                            }
                            iArr[right2 + 1] = iArr[right2];
                        }
                        iArr[right2 + 1] = last;
                        int last2 = left2;
                    }
                }
                return;
            }
            return;
        }
        int great = (length >> 3) + (length >> 6) + 1;
        int e3 = (k + right2) >>> 1;
        int e2 = e3 - great;
        int e1 = e2 - great;
        int e4 = e3 + great;
        int e5 = e4 + great;
        if (iArr[e2] < iArr[e1]) {
            int t = iArr[e2];
            iArr[e2] = iArr[e1];
            iArr[e1] = t;
        }
        if (iArr[e3] < iArr[e2]) {
            int t2 = iArr[e3];
            iArr[e3] = iArr[e2];
            iArr[e2] = t2;
            if (t2 < iArr[e1]) {
                iArr[e2] = iArr[e1];
                iArr[e1] = t2;
            }
        }
        if (iArr[e4] < iArr[e3]) {
            int t3 = iArr[e4];
            iArr[e4] = iArr[e3];
            iArr[e3] = t3;
            if (t3 < iArr[e2]) {
                iArr[e3] = iArr[e2];
                iArr[e2] = t3;
                if (t3 < iArr[e1]) {
                    iArr[e2] = iArr[e1];
                    iArr[e1] = t3;
                }
            }
        }
        if (iArr[e5] < iArr[e4]) {
            int t4 = iArr[e5];
            iArr[e5] = iArr[e4];
            iArr[e4] = t4;
            if (t4 < iArr[e3]) {
                iArr[e4] = iArr[e3];
                iArr[e3] = t4;
                if (t4 < iArr[e2]) {
                    iArr[e3] = iArr[e2];
                    iArr[e2] = t4;
                    if (t4 < iArr[e1]) {
                        iArr[e2] = iArr[e1];
                        iArr[e1] = t4;
                    }
                }
            }
        }
        int less = k;
        int great2 = right2;
        if (iArr[e1] == iArr[e2] || iArr[e2] == iArr[e3] || iArr[e3] == iArr[e4] || iArr[e4] == iArr[e5]) {
            int i2 = great;
            int length2 = iArr[e3];
            int less2 = less;
            while (less <= great2) {
                if (iArr[less] != length2) {
                    int ak = iArr[less];
                    if (ak < length2) {
                        iArr[less] = iArr[less2];
                        iArr[less2] = ak;
                        less2++;
                    } else {
                        while (iArr[great2] > length2) {
                            great2--;
                        }
                        if (iArr[great2] < length2) {
                            iArr[less] = iArr[less2];
                            iArr[less2] = iArr[great2];
                            less2++;
                        } else {
                            iArr[less] = length2;
                        }
                        iArr[great2] = ak;
                        great2--;
                    }
                }
                less++;
            }
            sort(iArr, k, less2 - 1, z);
            sort(iArr, great2 + 1, right2, false);
            int i3 = less2;
        } else {
            int pivot1 = iArr[e2];
            int pivot2 = iArr[e4];
            iArr[e2] = iArr[k];
            iArr[e4] = iArr[right2];
            do {
                less++;
            } while (iArr[less] < pivot1);
            do {
                great2--;
            } while (iArr[great2] > pivot2);
            int k3 = less - 1;
            loop9:
            while (true) {
                k3++;
                if (k3 > great2) {
                    int i4 = great;
                    break;
                }
                int length3 = length;
                int length4 = iArr[k3];
                if (length4 < pivot1) {
                    iArr[k3] = iArr[less];
                    iArr[less] = length4;
                    less++;
                    seventh = great;
                } else if (length4 > pivot2) {
                    while (true) {
                        seventh = great;
                        if (iArr[great2] > pivot2) {
                            int great3 = great2 - 1;
                            if (great2 == k3) {
                                great2 = great3;
                                break loop9;
                            } else {
                                great2 = great3;
                                great = seventh;
                            }
                        } else {
                            if (iArr[great2] < pivot1) {
                                iArr[k3] = iArr[less];
                                iArr[less] = iArr[great2];
                                less++;
                            } else {
                                iArr[k3] = iArr[great2];
                            }
                            iArr[great2] = length4;
                            great2--;
                        }
                    }
                } else {
                    seventh = great;
                }
                length = length3;
                great = seventh;
            }
            iArr[k] = iArr[less - 1];
            iArr[less - 1] = pivot1;
            iArr[right2] = iArr[great2 + 1];
            iArr[great2 + 1] = pivot2;
            sort(iArr, k, less - 2, z);
            sort(iArr, great2 + 2, right2, false);
            if (less < e1 && e5 < great2) {
                while (iArr[less] == pivot1) {
                    less++;
                }
                while (iArr[great2] == pivot2) {
                    great2--;
                }
                int k4 = less - 1;
                loop13:
                while (true) {
                    k4++;
                    if (k4 > great2) {
                        break;
                    }
                    int ak2 = iArr[k4];
                    if (ak2 == pivot1) {
                        iArr[k4] = iArr[less];
                        iArr[less] = ak2;
                        less++;
                    } else if (ak2 == pivot2) {
                        while (iArr[great2] == pivot2) {
                            int great4 = great2 - 1;
                            if (great2 == k4) {
                                great2 = great4;
                                break loop13;
                            }
                            great2 = great4;
                        }
                        if (iArr[great2] == pivot1) {
                            iArr[k4] = iArr[less];
                            iArr[less] = pivot1;
                            less++;
                        } else {
                            iArr[k4] = iArr[great2];
                        }
                        iArr[great2] = ak2;
                        great2--;
                    } else {
                        continue;
                    }
                }
            }
            sort(iArr, less, great2, false);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:55:0x00b8  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00c1  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x00c7  */
    static void sort(long[] a, int left, int right, long[] work, int workBase, int workLen) {
        int workBase2;
        int ao;
        int bo;
        long[] b;
        long[] a2 = a;
        int i = left;
        int i2 = right;
        long[] work2 = work;
        int i3 = 1;
        if (i2 - i < QUICKSORT_THRESHOLD) {
            sort(a2, i, i2, true);
            return;
        }
        int[] run = new int[68];
        int i4 = 0;
        run[0] = i;
        int count = 0;
        int k = i;
        while (k < i2) {
            if (a2[k] >= a2[k + 1]) {
                if (a2[k] <= a2[k + 1]) {
                    int m = MAX_RUN_LENGTH;
                    while (true) {
                        k++;
                        if (k > i2 || a2[k - 1] != a2[k]) {
                            break;
                        }
                        m--;
                        if (m == 0) {
                            sort(a2, i, i2, true);
                            return;
                        }
                    }
                } else {
                    do {
                        k++;
                        if (k > i2) {
                            break;
                        }
                    } while (a2[k - 1] >= a2[k]);
                    int lo = run[count] - 1;
                    int hi = k;
                    while (true) {
                        lo++;
                        hi--;
                        if (lo >= hi) {
                            break;
                        }
                        long t = a2[lo];
                        a2[lo] = a2[hi];
                        a2[hi] = t;
                    }
                }
            } else {
                do {
                    k++;
                    if (k > i2) {
                        break;
                    }
                } while (a2[k - 1] <= a2[k]);
            }
            count++;
            if (count == MAX_RUN_COUNT) {
                sort(a2, i, i2, true);
                return;
            }
            run[count] = k;
        }
        int right2 = i2 + 1;
        if (run[count] == i2) {
            count++;
            run[count] = right2;
        } else if (count == 1) {
            return;
        }
        byte odd = 0;
        int n = 1;
        while (true) {
            int i5 = n << 1;
            n = i5;
            if (i5 >= count) {
                break;
            }
            odd = (byte) (odd ^ 1);
        }
        int blen = right2 - i;
        if (work2 == null) {
            int i6 = workLen;
        } else if (workLen >= blen && workBase + blen <= work2.length) {
            workBase2 = workBase;
            if (odd != 0) {
                System.arraycopy((Object) a2, i, (Object) work2, workBase2, blen);
                b = a2;
                bo = 0;
                a2 = work2;
                ao = workBase2 - i;
            } else {
                b = work2;
                ao = 0;
                bo = workBase2 - i;
            }
            while (count > i3) {
                int last = i4;
                int k2 = 2;
                while (true) {
                    int k3 = k2;
                    if (k3 > count) {
                        break;
                    }
                    int hi2 = run[k3];
                    int mi = run[k3 - 1];
                    int i7 = run[k3 - 2];
                    int blen2 = blen;
                    long[] work3 = work2;
                    int i8 = i7;
                    int p = i7;
                    int q = mi;
                    while (true) {
                        int q2 = q;
                        if (i8 >= hi2) {
                            break;
                        }
                        byte odd2 = odd;
                        int q3 = q2;
                        if (q3 >= hi2 || (p < mi && a2[p + ao] <= a2[q3 + ao])) {
                            b[i8 + bo] = a2[p + ao];
                            q = q3;
                            p++;
                        } else {
                            b[i8 + bo] = a2[q3 + ao];
                            q = q3 + 1;
                        }
                        i8++;
                        odd = odd2;
                    }
                    last++;
                    run[last] = hi2;
                    k2 = k3 + 2;
                    blen = blen2;
                    work2 = work3;
                    int i9 = left;
                }
                int blen3 = blen;
                long[] work4 = work2;
                byte odd3 = odd;
                if ((count & 1) != 0) {
                    int i10 = right2;
                    int lo2 = run[count - 1];
                    while (true) {
                        i10--;
                        if (i10 < lo2) {
                            break;
                        }
                        b[i10 + bo] = a2[i10 + ao];
                    }
                    last++;
                    run[last] = right2;
                }
                long[] t2 = a2;
                a2 = b;
                b = t2;
                int o = ao;
                ao = bo;
                bo = o;
                count = last;
                blen = blen3;
                work2 = work4;
                odd = odd3;
                int i11 = left;
                i3 = 1;
                i4 = 0;
            }
            long[] jArr = work2;
            byte b2 = odd;
        }
        work2 = new long[blen];
        workBase2 = 0;
        if (odd != 0) {
        }
        while (count > i3) {
        }
        long[] jArr2 = work2;
        byte b22 = odd;
    }

    private static void sort(long[] a, int left, int right, boolean leftmost) {
        int left2;
        long[] jArr = a;
        int k = left;
        int right2 = right;
        boolean z = leftmost;
        int length = (right2 - k) + 1;
        if (length < INSERTION_SORT_THRESHOLD) {
            if (z) {
                int j = k;
                int i = j;
                while (i < right2) {
                    long ai = jArr[i + 1];
                    while (true) {
                        if (ai >= jArr[j]) {
                            break;
                        }
                        jArr[j + 1] = jArr[j];
                        int j2 = j - 1;
                        if (j == k) {
                            j = j2;
                            break;
                        }
                        j = j2;
                    }
                    jArr[j + 1] = ai;
                    i++;
                    j = i;
                }
            } else {
                while (k < right2) {
                    k++;
                    if (jArr[k] < jArr[k - 1]) {
                        int left3 = k;
                        while (true) {
                            left2 = left3 + 1;
                            if (left2 > right2) {
                                break;
                            }
                            long a1 = jArr[k];
                            long a2 = jArr[left2];
                            if (a1 < a2) {
                                a2 = a1;
                                a1 = jArr[left2];
                            }
                            while (true) {
                                k--;
                                if (a1 >= jArr[k]) {
                                    break;
                                }
                                jArr[k + 2] = jArr[k];
                            }
                            int k2 = k + 1;
                            jArr[k2 + 1] = a1;
                            while (true) {
                                k2--;
                                if (a2 >= jArr[k2]) {
                                    break;
                                }
                                jArr[k2 + 1] = jArr[k2];
                            }
                            jArr[k2 + 1] = a2;
                            left3 = left2 + 1;
                            k = left3;
                        }
                        long last = jArr[right2];
                        while (true) {
                            right2--;
                            if (last >= jArr[right2]) {
                                break;
                            }
                            jArr[right2 + 1] = jArr[right2];
                        }
                        jArr[right2 + 1] = last;
                        int i2 = left2;
                    }
                }
                return;
            }
            return;
        }
        int seventh = (length >> 3) + (length >> 6) + 1;
        int e3 = (k + right2) >>> 1;
        int e2 = e3 - seventh;
        int e1 = e2 - seventh;
        int e4 = e3 + seventh;
        int e5 = e4 + seventh;
        if (jArr[e2] < jArr[e1]) {
            long t = jArr[e2];
            jArr[e2] = jArr[e1];
            jArr[e1] = t;
        }
        if (jArr[e3] < jArr[e2]) {
            long t2 = jArr[e3];
            jArr[e3] = jArr[e2];
            jArr[e2] = t2;
            if (t2 < jArr[e1]) {
                jArr[e2] = jArr[e1];
                jArr[e1] = t2;
            }
        }
        if (jArr[e4] < jArr[e3]) {
            long t3 = jArr[e4];
            jArr[e4] = jArr[e3];
            jArr[e3] = t3;
            if (t3 < jArr[e2]) {
                jArr[e3] = jArr[e2];
                jArr[e2] = t3;
                if (t3 < jArr[e1]) {
                    jArr[e2] = jArr[e1];
                    jArr[e1] = t3;
                }
            }
        }
        if (jArr[e5] < jArr[e4]) {
            long t4 = jArr[e5];
            jArr[e5] = jArr[e4];
            jArr[e4] = t4;
            if (t4 < jArr[e3]) {
                jArr[e4] = jArr[e3];
                jArr[e3] = t4;
                if (t4 < jArr[e2]) {
                    jArr[e3] = jArr[e2];
                    jArr[e2] = t4;
                    if (t4 < jArr[e1]) {
                        jArr[e2] = jArr[e1];
                        jArr[e1] = t4;
                    }
                }
            }
        }
        int less = k;
        int great = right2;
        if (jArr[e1] == jArr[e2] || jArr[e2] == jArr[e3] || jArr[e3] == jArr[e4] || jArr[e4] == jArr[e5]) {
            long pivot = jArr[e3];
            int less2 = less;
            while (less <= great) {
                if (jArr[less] != pivot) {
                    long ak = jArr[less];
                    if (ak < pivot) {
                        jArr[less] = jArr[less2];
                        jArr[less2] = ak;
                        less2++;
                    } else {
                        while (jArr[great] > pivot) {
                            great--;
                        }
                        if (jArr[great] < pivot) {
                            jArr[less] = jArr[less2];
                            jArr[less2] = jArr[great];
                            less2++;
                        } else {
                            jArr[less] = pivot;
                        }
                        jArr[great] = ak;
                        great--;
                    }
                }
                less++;
            }
            sort(jArr, k, less2 - 1, z);
            sort(jArr, great + 1, right2, false);
            int i3 = less2;
        } else {
            long pivot1 = jArr[e2];
            long pivot2 = jArr[e4];
            jArr[e2] = jArr[k];
            jArr[e4] = jArr[right2];
            do {
                less++;
            } while (jArr[less] < pivot1);
            do {
                great--;
            } while (jArr[great] > pivot2);
            int k3 = less - 1;
            loop9:
            while (true) {
                k3++;
                if (k3 > great) {
                    break;
                }
                long ak2 = jArr[k3];
                if (ak2 < pivot1) {
                    jArr[k3] = jArr[less];
                    jArr[less] = ak2;
                    less++;
                } else if (ak2 > pivot2) {
                    while (jArr[great] > pivot2) {
                        int great2 = great - 1;
                        if (great == k3) {
                            great = great2;
                            break loop9;
                        }
                        great = great2;
                    }
                    if (jArr[great] < pivot1) {
                        jArr[k3] = jArr[less];
                        jArr[less] = jArr[great];
                        less++;
                    } else {
                        jArr[k3] = jArr[great];
                    }
                    jArr[great] = ak2;
                    great--;
                } else {
                    continue;
                }
            }
            jArr[k] = jArr[less - 1];
            jArr[less - 1] = pivot1;
            jArr[right2] = jArr[great + 1];
            jArr[great + 1] = pivot2;
            sort(jArr, k, less - 2, z);
            sort(jArr, great + 2, right2, false);
            if (less < e1 && e5 < great) {
                while (jArr[less] == pivot1) {
                    less++;
                }
                while (jArr[great] == pivot2) {
                    great--;
                }
                int k4 = less - 1;
                loop13:
                while (true) {
                    k4++;
                    if (k4 > great) {
                        break;
                    }
                    long ak3 = jArr[k4];
                    if (ak3 == pivot1) {
                        jArr[k4] = jArr[less];
                        jArr[less] = ak3;
                        less++;
                    } else if (ak3 == pivot2) {
                        while (jArr[great] == pivot2) {
                            int great3 = great - 1;
                            if (great == k4) {
                                great = great3;
                                break loop13;
                            }
                            great = great3;
                        }
                        if (jArr[great] == pivot1) {
                            jArr[k4] = jArr[less];
                            jArr[less] = pivot1;
                            less++;
                        } else {
                            jArr[k4] = jArr[great];
                        }
                        jArr[great] = ak3;
                        great--;
                    } else {
                        continue;
                    }
                }
            }
            sort(jArr, less, great, false);
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
                int i2 = a[i] - Short.MIN_VALUE;
                count[i2] = count[i2] + 1;
            }
            int i3 = 65536;
            int k = right + 1;
            while (k > left) {
                do {
                    i3--;
                } while (count[i3] == 0);
                short value = (short) (i3 - 32768);
                int s = count[i3];
                do {
                    k--;
                    a[k] = value;
                    s--;
                } while (s > 0);
            }
            return;
        }
        doSort(a, left, right, work, workBase, workLen);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:68:0x00f2, code lost:
        if (r0[r3 + r14] <= r0[r6 + r14]) goto L_0x0105;
     */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x00ae  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00b7  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x00bd  */
    private static void doSort(short[] a, int left, int right, short[] work, int workBase, int workLen) {
        int workBase2;
        int ao;
        int bo;
        short[] b;
        int mi;
        short[] a2 = a;
        int i = left;
        int i2 = right;
        short[] work2 = work;
        int i3 = 1;
        if (i2 - i < QUICKSORT_THRESHOLD) {
            sort(a2, i, i2, true);
            return;
        }
        int[] run = new int[68];
        int i4 = 0;
        run[0] = i;
        int count = 0;
        int k = i;
        while (k < i2) {
            if (a2[k] >= a2[k + 1]) {
                if (a2[k] <= a2[k + 1]) {
                    int m = MAX_RUN_LENGTH;
                    while (true) {
                        k++;
                        if (k > i2 || a2[k - 1] != a2[k]) {
                            break;
                        }
                        m--;
                        if (m == 0) {
                            sort(a2, i, i2, true);
                            return;
                        }
                    }
                } else {
                    do {
                        k++;
                        if (k > i2) {
                            break;
                        }
                    } while (a2[k - 1] >= a2[k]);
                    int lo = run[count] - 1;
                    int hi = k;
                    while (true) {
                        lo++;
                        hi--;
                        if (lo >= hi) {
                            break;
                        }
                        short t = a2[lo];
                        a2[lo] = a2[hi];
                        a2[hi] = t;
                    }
                }
            } else {
                do {
                    k++;
                    if (k > i2) {
                        break;
                    }
                } while (a2[k - 1] <= a2[k]);
            }
            count++;
            if (count == MAX_RUN_COUNT) {
                sort(a2, i, i2, true);
                return;
            }
            run[count] = k;
        }
        int right2 = i2 + 1;
        if (run[count] == i2) {
            count++;
            run[count] = right2;
        } else if (count == 1) {
            return;
        }
        byte odd = 0;
        int n = 1;
        while (true) {
            int i5 = n << 1;
            n = i5;
            if (i5 >= count) {
                break;
            }
            odd = (byte) (odd ^ 1);
        }
        int blen = right2 - i;
        if (work2 == null) {
            int i6 = workLen;
        } else if (workLen >= blen && workBase + blen <= work2.length) {
            workBase2 = workBase;
            if (odd != 0) {
                System.arraycopy((Object) a2, i, (Object) work2, workBase2, blen);
                b = a2;
                bo = 0;
                a2 = work2;
                ao = workBase2 - i;
            } else {
                b = work2;
                ao = 0;
                bo = workBase2 - i;
            }
            while (count > i3) {
                int last = i4;
                int k2 = 2;
                while (true) {
                    int k3 = k2;
                    if (k3 > count) {
                        break;
                    }
                    int hi2 = run[k3];
                    int mi2 = run[k3 - 1];
                    int i7 = run[k3 - 2];
                    int blen2 = blen;
                    short[] work3 = work2;
                    int i8 = i7;
                    int p = i7;
                    int q = mi2;
                    while (true) {
                        int q2 = q;
                        if (i8 >= hi2) {
                            break;
                        }
                        byte odd2 = odd;
                        int q3 = q2;
                        if (q3 < hi2) {
                            if (p < mi2) {
                                mi = mi2;
                            } else {
                                mi = mi2;
                            }
                            b[i8 + bo] = a2[q3 + ao];
                            q = q3 + 1;
                            i8++;
                            odd = odd2;
                            mi2 = mi;
                            int i9 = workLen;
                        } else {
                            mi = mi2;
                        }
                        b[i8 + bo] = a2[p + ao];
                        q = q3;
                        p++;
                        i8++;
                        odd = odd2;
                        mi2 = mi;
                        int i92 = workLen;
                    }
                    byte b2 = odd;
                    last++;
                    run[last] = hi2;
                    k2 = k3 + 2;
                    blen = blen2;
                    work2 = work3;
                    int i10 = left;
                    int i11 = workLen;
                }
                int blen3 = blen;
                short[] work4 = work2;
                byte odd3 = odd;
                if ((count & 1) != 0) {
                    int i12 = right2;
                    int lo2 = run[count - 1];
                    while (true) {
                        i12--;
                        if (i12 < lo2) {
                            break;
                        }
                        b[i12 + bo] = a2[i12 + ao];
                    }
                    last++;
                    run[last] = right2;
                }
                short[] t2 = a2;
                a2 = b;
                b = t2;
                int o = ao;
                ao = bo;
                bo = o;
                count = last;
                blen = blen3;
                work2 = work4;
                odd = odd3;
                int i13 = left;
                i3 = 1;
                i4 = 0;
                int i14 = workLen;
            }
            short[] sArr = work2;
            byte b3 = odd;
        }
        work2 = new short[blen];
        workBase2 = 0;
        if (odd != 0) {
        }
        while (count > i3) {
        }
        short[] sArr2 = work2;
        byte b32 = odd;
    }

    private static void sort(short[] a, int left, int right, boolean leftmost) {
        int seventh;
        int left2;
        short[] sArr = a;
        int k = left;
        int right2 = right;
        boolean z = leftmost;
        int length = (right2 - k) + 1;
        if (length < INSERTION_SORT_THRESHOLD) {
            if (z) {
                int j = k;
                int i = j;
                while (i < right2) {
                    short ai = sArr[i + 1];
                    while (true) {
                        if (ai >= sArr[j]) {
                            break;
                        }
                        sArr[j + 1] = sArr[j];
                        int j2 = j - 1;
                        if (j == k) {
                            j = j2;
                            break;
                        }
                        j = j2;
                    }
                    sArr[j + 1] = ai;
                    i++;
                    j = i;
                }
            } else {
                while (k < right2) {
                    k++;
                    if (sArr[k] < sArr[k - 1]) {
                        int left3 = k;
                        while (true) {
                            left2 = left3 + 1;
                            if (left2 > right2) {
                                break;
                            }
                            short a1 = sArr[k];
                            short a2 = sArr[left2];
                            if (a1 < a2) {
                                a2 = a1;
                                a1 = sArr[left2];
                            }
                            while (true) {
                                k--;
                                if (a1 >= sArr[k]) {
                                    break;
                                }
                                sArr[k + 2] = sArr[k];
                            }
                            int k2 = k + 1;
                            sArr[k2 + 1] = a1;
                            while (true) {
                                k2--;
                                if (a2 >= sArr[k2]) {
                                    break;
                                }
                                sArr[k2 + 1] = sArr[k2];
                            }
                            sArr[k2 + 1] = a2;
                            left3 = left2 + 1;
                            k = left3;
                        }
                        short k3 = sArr[right2];
                        while (true) {
                            right2--;
                            if (k3 >= sArr[right2]) {
                                break;
                            }
                            sArr[right2 + 1] = sArr[right2];
                        }
                        sArr[right2 + 1] = k3;
                        int i2 = left2;
                    }
                }
                return;
            }
            return;
        }
        int great = (length >> 3) + (length >> 6) + 1;
        int e3 = (k + right2) >>> 1;
        int e2 = e3 - great;
        int e1 = e2 - great;
        int e4 = e3 + great;
        int e5 = e4 + great;
        if (sArr[e2] < sArr[e1]) {
            short t = sArr[e2];
            sArr[e2] = sArr[e1];
            sArr[e1] = t;
        }
        if (sArr[e3] < sArr[e2]) {
            short t2 = sArr[e3];
            sArr[e3] = sArr[e2];
            sArr[e2] = t2;
            if (t2 < sArr[e1]) {
                sArr[e2] = sArr[e1];
                sArr[e1] = t2;
            }
        }
        if (sArr[e4] < sArr[e3]) {
            short t3 = sArr[e4];
            sArr[e4] = sArr[e3];
            sArr[e3] = t3;
            if (t3 < sArr[e2]) {
                sArr[e3] = sArr[e2];
                sArr[e2] = t3;
                if (t3 < sArr[e1]) {
                    sArr[e2] = sArr[e1];
                    sArr[e1] = t3;
                }
            }
        }
        if (sArr[e5] < sArr[e4]) {
            short t4 = sArr[e5];
            sArr[e5] = sArr[e4];
            sArr[e4] = t4;
            if (t4 < sArr[e3]) {
                sArr[e4] = sArr[e3];
                sArr[e3] = t4;
                if (t4 < sArr[e2]) {
                    sArr[e3] = sArr[e2];
                    sArr[e2] = t4;
                    if (t4 < sArr[e1]) {
                        sArr[e2] = sArr[e1];
                        sArr[e1] = t4;
                    }
                }
            }
        }
        int less = k;
        int great2 = right2;
        if (sArr[e1] == sArr[e2] || sArr[e2] == sArr[e3] || sArr[e3] == sArr[e4] || sArr[e4] == sArr[e5]) {
            int i3 = great;
            short length2 = sArr[e3];
            int less2 = less;
            while (less <= great2) {
                if (sArr[less] != length2) {
                    short ak = sArr[less];
                    if (ak < length2) {
                        sArr[less] = sArr[less2];
                        sArr[less2] = ak;
                        less2++;
                    } else {
                        while (sArr[great2] > length2) {
                            great2--;
                        }
                        if (sArr[great2] < length2) {
                            sArr[less] = sArr[less2];
                            sArr[less2] = sArr[great2];
                            less2++;
                        } else {
                            sArr[less] = length2;
                        }
                        sArr[great2] = ak;
                        great2--;
                    }
                }
                less++;
            }
            sort(sArr, k, less2 - 1, z);
            sort(sArr, great2 + 1, right2, false);
            int i4 = less2;
        } else {
            short pivot1 = sArr[e2];
            short pivot2 = sArr[e4];
            sArr[e2] = sArr[k];
            sArr[e4] = sArr[right2];
            do {
                less++;
            } while (sArr[less] < pivot1);
            do {
                great2--;
            } while (sArr[great2] > pivot2);
            int k4 = less - 1;
            loop9:
            while (true) {
                k4++;
                if (k4 > great2) {
                    int i5 = great;
                    break;
                }
                int length3 = length;
                short length4 = sArr[k4];
                if (length4 < pivot1) {
                    sArr[k4] = sArr[less];
                    sArr[less] = length4;
                    less++;
                    seventh = great;
                } else if (length4 > pivot2) {
                    while (true) {
                        seventh = great;
                        if (sArr[great2] > pivot2) {
                            int great3 = great2 - 1;
                            if (great2 == k4) {
                                great2 = great3;
                                break loop9;
                            } else {
                                great2 = great3;
                                great = seventh;
                            }
                        } else {
                            if (sArr[great2] < pivot1) {
                                sArr[k4] = sArr[less];
                                sArr[less] = sArr[great2];
                                less++;
                            } else {
                                sArr[k4] = sArr[great2];
                            }
                            sArr[great2] = length4;
                            great2--;
                        }
                    }
                } else {
                    seventh = great;
                }
                length = length3;
                great = seventh;
            }
            sArr[k] = sArr[less - 1];
            sArr[less - 1] = pivot1;
            sArr[right2] = sArr[great2 + 1];
            sArr[great2 + 1] = pivot2;
            sort(sArr, k, less - 2, z);
            sort(sArr, great2 + 2, right2, false);
            if (less < e1 && e5 < great2) {
                while (sArr[less] == pivot1) {
                    less++;
                }
                while (sArr[great2] == pivot2) {
                    great2--;
                }
                int k5 = less - 1;
                loop13:
                while (true) {
                    k5++;
                    if (k5 > great2) {
                        break;
                    }
                    short ak2 = sArr[k5];
                    if (ak2 == pivot1) {
                        sArr[k5] = sArr[less];
                        sArr[less] = ak2;
                        less++;
                    } else if (ak2 == pivot2) {
                        while (sArr[great2] == pivot2) {
                            int great4 = great2 - 1;
                            if (great2 == k5) {
                                great2 = great4;
                                break loop13;
                            }
                            great2 = great4;
                        }
                        if (sArr[great2] == pivot1) {
                            sArr[k5] = sArr[less];
                            sArr[less] = pivot1;
                            less++;
                        } else {
                            sArr[k5] = sArr[great2];
                        }
                        sArr[great2] = ak2;
                        great2--;
                    } else {
                        continue;
                    }
                }
            }
            sort(sArr, less, great2, false);
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
            int i2 = 65536;
            int k = right + 1;
            while (k > left) {
                do {
                    i2--;
                } while (count[i2] == 0);
                char value = (char) i2;
                int s = count[i2];
                do {
                    k--;
                    a[k] = value;
                    s--;
                } while (s > 0);
            }
            return;
        }
        doSort(a, left, right, work, workBase, workLen);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:68:0x00f2, code lost:
        if (r0[r3 + r14] <= r0[r6 + r14]) goto L_0x0105;
     */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x00ae  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00b7  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x00bd  */
    private static void doSort(char[] a, int left, int right, char[] work, int workBase, int workLen) {
        int workBase2;
        int ao;
        int bo;
        char[] b;
        int mi;
        char[] a2 = a;
        int i = left;
        int i2 = right;
        char[] work2 = work;
        int i3 = 1;
        if (i2 - i < QUICKSORT_THRESHOLD) {
            sort(a2, i, i2, true);
            return;
        }
        int[] run = new int[68];
        int i4 = 0;
        run[0] = i;
        int count = 0;
        int k = i;
        while (k < i2) {
            if (a2[k] >= a2[k + 1]) {
                if (a2[k] <= a2[k + 1]) {
                    int m = MAX_RUN_LENGTH;
                    while (true) {
                        k++;
                        if (k > i2 || a2[k - 1] != a2[k]) {
                            break;
                        }
                        m--;
                        if (m == 0) {
                            sort(a2, i, i2, true);
                            return;
                        }
                    }
                } else {
                    do {
                        k++;
                        if (k > i2) {
                            break;
                        }
                    } while (a2[k - 1] >= a2[k]);
                    int lo = run[count] - 1;
                    int hi = k;
                    while (true) {
                        lo++;
                        hi--;
                        if (lo >= hi) {
                            break;
                        }
                        char t = a2[lo];
                        a2[lo] = a2[hi];
                        a2[hi] = t;
                    }
                }
            } else {
                do {
                    k++;
                    if (k > i2) {
                        break;
                    }
                } while (a2[k - 1] <= a2[k]);
            }
            count++;
            if (count == MAX_RUN_COUNT) {
                sort(a2, i, i2, true);
                return;
            }
            run[count] = k;
        }
        int right2 = i2 + 1;
        if (run[count] == i2) {
            count++;
            run[count] = right2;
        } else if (count == 1) {
            return;
        }
        byte odd = 0;
        int n = 1;
        while (true) {
            int i5 = n << 1;
            n = i5;
            if (i5 >= count) {
                break;
            }
            odd = (byte) (odd ^ 1);
        }
        int blen = right2 - i;
        if (work2 == null) {
            int i6 = workLen;
        } else if (workLen >= blen && workBase + blen <= work2.length) {
            workBase2 = workBase;
            if (odd != 0) {
                System.arraycopy((Object) a2, i, (Object) work2, workBase2, blen);
                b = a2;
                bo = 0;
                a2 = work2;
                ao = workBase2 - i;
            } else {
                b = work2;
                ao = 0;
                bo = workBase2 - i;
            }
            while (count > i3) {
                int last = i4;
                int k2 = 2;
                while (true) {
                    int k3 = k2;
                    if (k3 > count) {
                        break;
                    }
                    int hi2 = run[k3];
                    int mi2 = run[k3 - 1];
                    int i7 = run[k3 - 2];
                    int blen2 = blen;
                    char[] work3 = work2;
                    int i8 = i7;
                    int p = i7;
                    int q = mi2;
                    while (true) {
                        int q2 = q;
                        if (i8 >= hi2) {
                            break;
                        }
                        byte odd2 = odd;
                        int q3 = q2;
                        if (q3 < hi2) {
                            if (p < mi2) {
                                mi = mi2;
                            } else {
                                mi = mi2;
                            }
                            b[i8 + bo] = a2[q3 + ao];
                            q = q3 + 1;
                            i8++;
                            odd = odd2;
                            mi2 = mi;
                            int i9 = workLen;
                        } else {
                            mi = mi2;
                        }
                        b[i8 + bo] = a2[p + ao];
                        q = q3;
                        p++;
                        i8++;
                        odd = odd2;
                        mi2 = mi;
                        int i92 = workLen;
                    }
                    byte b2 = odd;
                    last++;
                    run[last] = hi2;
                    k2 = k3 + 2;
                    blen = blen2;
                    work2 = work3;
                    int i10 = left;
                    int i11 = workLen;
                }
                int blen3 = blen;
                char[] work4 = work2;
                byte odd3 = odd;
                if ((count & 1) != 0) {
                    int i12 = right2;
                    int lo2 = run[count - 1];
                    while (true) {
                        i12--;
                        if (i12 < lo2) {
                            break;
                        }
                        b[i12 + bo] = a2[i12 + ao];
                    }
                    last++;
                    run[last] = right2;
                }
                char[] t2 = a2;
                a2 = b;
                b = t2;
                int o = ao;
                ao = bo;
                bo = o;
                count = last;
                blen = blen3;
                work2 = work4;
                odd = odd3;
                int i13 = left;
                i3 = 1;
                i4 = 0;
                int i14 = workLen;
            }
            char[] cArr = work2;
            byte b3 = odd;
        }
        work2 = new char[blen];
        workBase2 = 0;
        if (odd != 0) {
        }
        while (count > i3) {
        }
        char[] cArr2 = work2;
        byte b32 = odd;
    }

    private static void sort(char[] a, int left, int right, boolean leftmost) {
        int seventh;
        int left2;
        char[] cArr = a;
        int k = left;
        int right2 = right;
        boolean z = leftmost;
        int length = (right2 - k) + 1;
        if (length < INSERTION_SORT_THRESHOLD) {
            if (z) {
                int j = k;
                int i = j;
                while (i < right2) {
                    char ai = cArr[i + 1];
                    while (true) {
                        if (ai >= cArr[j]) {
                            break;
                        }
                        cArr[j + 1] = cArr[j];
                        int j2 = j - 1;
                        if (j == k) {
                            j = j2;
                            break;
                        }
                        j = j2;
                    }
                    cArr[j + 1] = ai;
                    i++;
                    j = i;
                }
            } else {
                while (k < right2) {
                    k++;
                    if (cArr[k] < cArr[k - 1]) {
                        int left3 = k;
                        while (true) {
                            left2 = left3 + 1;
                            if (left2 > right2) {
                                break;
                            }
                            char a1 = cArr[k];
                            char a2 = cArr[left2];
                            if (a1 < a2) {
                                a2 = a1;
                                a1 = cArr[left2];
                            }
                            while (true) {
                                k--;
                                if (a1 >= cArr[k]) {
                                    break;
                                }
                                cArr[k + 2] = cArr[k];
                            }
                            int k2 = k + 1;
                            cArr[k2 + 1] = a1;
                            while (true) {
                                k2--;
                                if (a2 >= cArr[k2]) {
                                    break;
                                }
                                cArr[k2 + 1] = cArr[k2];
                            }
                            cArr[k2 + 1] = a2;
                            left3 = left2 + 1;
                            k = left3;
                        }
                        char last = cArr[right2];
                        while (true) {
                            right2--;
                            if (last >= cArr[right2]) {
                                break;
                            }
                            cArr[right2 + 1] = cArr[right2];
                        }
                        cArr[right2 + 1] = last;
                        int i2 = left2;
                    }
                }
                return;
            }
            return;
        }
        int great = (length >> 3) + (length >> 6) + 1;
        int e3 = (k + right2) >>> 1;
        int e2 = e3 - great;
        int e1 = e2 - great;
        int e4 = e3 + great;
        int e5 = e4 + great;
        if (cArr[e2] < cArr[e1]) {
            char t = cArr[e2];
            cArr[e2] = cArr[e1];
            cArr[e1] = t;
        }
        if (cArr[e3] < cArr[e2]) {
            char t2 = cArr[e3];
            cArr[e3] = cArr[e2];
            cArr[e2] = t2;
            if (t2 < cArr[e1]) {
                cArr[e2] = cArr[e1];
                cArr[e1] = t2;
            }
        }
        if (cArr[e4] < cArr[e3]) {
            char t3 = cArr[e4];
            cArr[e4] = cArr[e3];
            cArr[e3] = t3;
            if (t3 < cArr[e2]) {
                cArr[e3] = cArr[e2];
                cArr[e2] = t3;
                if (t3 < cArr[e1]) {
                    cArr[e2] = cArr[e1];
                    cArr[e1] = t3;
                }
            }
        }
        if (cArr[e5] < cArr[e4]) {
            char t4 = cArr[e5];
            cArr[e5] = cArr[e4];
            cArr[e4] = t4;
            if (t4 < cArr[e3]) {
                cArr[e4] = cArr[e3];
                cArr[e3] = t4;
                if (t4 < cArr[e2]) {
                    cArr[e3] = cArr[e2];
                    cArr[e2] = t4;
                    if (t4 < cArr[e1]) {
                        cArr[e2] = cArr[e1];
                        cArr[e1] = t4;
                    }
                }
            }
        }
        int less = k;
        int great2 = right2;
        if (cArr[e1] == cArr[e2] || cArr[e2] == cArr[e3] || cArr[e3] == cArr[e4] || cArr[e4] == cArr[e5]) {
            int i3 = great;
            char pivot = cArr[e3];
            int less2 = less;
            while (less <= great2) {
                if (cArr[less] != pivot) {
                    char ak = cArr[less];
                    if (ak < pivot) {
                        cArr[less] = cArr[less2];
                        cArr[less2] = ak;
                        less2++;
                    } else {
                        while (cArr[great2] > pivot) {
                            great2--;
                        }
                        if (cArr[great2] < pivot) {
                            cArr[less] = cArr[less2];
                            cArr[less2] = cArr[great2];
                            less2++;
                        } else {
                            cArr[less] = pivot;
                        }
                        cArr[great2] = ak;
                        great2--;
                    }
                }
                less++;
            }
            sort(cArr, k, less2 - 1, z);
            sort(cArr, great2 + 1, right2, false);
            int i4 = less2;
        } else {
            char pivot1 = cArr[e2];
            char pivot2 = cArr[e4];
            cArr[e2] = cArr[k];
            cArr[e4] = cArr[right2];
            do {
                less++;
            } while (cArr[less] < pivot1);
            do {
                great2--;
            } while (cArr[great2] > pivot2);
            int k3 = less - 1;
            loop9:
            while (true) {
                k3++;
                if (k3 > great2) {
                    int i5 = great;
                    break;
                }
                int length2 = length;
                char length3 = cArr[k3];
                if (length3 < pivot1) {
                    cArr[k3] = cArr[less];
                    cArr[less] = length3;
                    less++;
                    seventh = great;
                } else if (length3 > pivot2) {
                    while (true) {
                        seventh = great;
                        if (cArr[great2] > pivot2) {
                            int great3 = great2 - 1;
                            if (great2 == k3) {
                                great2 = great3;
                                break loop9;
                            } else {
                                great2 = great3;
                                great = seventh;
                            }
                        } else {
                            if (cArr[great2] < pivot1) {
                                cArr[k3] = cArr[less];
                                cArr[less] = cArr[great2];
                                less++;
                            } else {
                                cArr[k3] = cArr[great2];
                            }
                            cArr[great2] = length3;
                            great2--;
                        }
                    }
                } else {
                    seventh = great;
                }
                length = length2;
                great = seventh;
            }
            cArr[k] = cArr[less - 1];
            cArr[less - 1] = pivot1;
            cArr[right2] = cArr[great2 + 1];
            cArr[great2 + 1] = pivot2;
            sort(cArr, k, less - 2, z);
            sort(cArr, great2 + 2, right2, false);
            if (less < e1 && e5 < great2) {
                while (cArr[less] == pivot1) {
                    less++;
                }
                while (cArr[great2] == pivot2) {
                    great2--;
                }
                int k4 = less - 1;
                loop13:
                while (true) {
                    k4++;
                    if (k4 > great2) {
                        break;
                    }
                    char ak2 = cArr[k4];
                    if (ak2 == pivot1) {
                        cArr[k4] = cArr[less];
                        cArr[less] = ak2;
                        less++;
                    } else if (ak2 == pivot2) {
                        while (cArr[great2] == pivot2) {
                            int great4 = great2 - 1;
                            if (great2 == k4) {
                                great2 = great4;
                                break loop13;
                            }
                            great2 = great4;
                        }
                        if (cArr[great2] == pivot1) {
                            cArr[k4] = cArr[less];
                            cArr[less] = pivot1;
                            less++;
                        } else {
                            cArr[k4] = cArr[great2];
                        }
                        cArr[great2] = ak2;
                        great2--;
                    } else {
                        continue;
                    }
                }
            }
            sort(cArr, less, great2, false);
        }
    }

    static void sort(byte[] a, int left, int right) {
        if (right - left > COUNTING_SORT_THRESHOLD_FOR_BYTE) {
            int[] count = new int[256];
            int i = left - 1;
            while (true) {
                i++;
                if (i > right) {
                    break;
                }
                int i2 = a[i] + 128;
                count[i2] = count[i2] + 1;
            }
            int i3 = 256;
            int k = right + 1;
            while (k > left) {
                do {
                    i3--;
                } while (count[i3] == 0);
                byte value = (byte) (i3 - 128);
                int s = count[i3];
                do {
                    k--;
                    a[k] = value;
                    s--;
                } while (s > 0);
            }
            return;
        }
        int j = left;
        int i4 = j;
        while (i4 < right) {
            byte ai = a[i4 + 1];
            while (true) {
                if (ai >= a[j]) {
                    break;
                }
                a[j + 1] = a[j];
                int j2 = j - 1;
                if (j == left) {
                    j = j2;
                    break;
                }
                j = j2;
            }
            a[j + 1] = ai;
            i4++;
            j = i4;
        }
    }

    static void sort(float[] a, int left, int right, float[] work, int workBase, int workLen) {
        while (left <= right && Float.isNaN(a[right])) {
            right--;
        }
        int right2 = right;
        while (true) {
            right--;
            if (right < left) {
                break;
            }
            float ak = a[right];
            if (ak != ak) {
                a[right] = a[right2];
                a[right2] = ak;
                right2--;
            }
        }
        doSort(a, left, right2, work, workBase, workLen);
        int left2 = left;
        int hi = right2;
        while (left2 < hi) {
            int middle = (left2 + hi) >>> 1;
            if (a[middle] < 0.0f) {
                left2 = middle + 1;
            } else {
                hi = middle;
            }
        }
        while (left2 <= right2 && Float.floatToRawIntBits(a[left2]) < 0) {
            left2++;
        }
        int k = left2;
        int p = left2 - 1;
        while (true) {
            k++;
            if (k <= right2) {
                float ak2 = a[k];
                if (ak2 == 0.0f) {
                    if (Float.floatToRawIntBits(ak2) < 0) {
                        a[k] = 0.0f;
                        p++;
                        a[p] = -0.0f;
                    }
                } else {
                    return;
                }
            } else {
                return;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:55:0x00b8  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00c1  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x00c7  */
    private static void doSort(float[] a, int left, int right, float[] work, int workBase, int workLen) {
        int workBase2;
        int ao;
        int bo;
        float[] b;
        float[] a2 = a;
        int i = left;
        int i2 = right;
        float[] work2 = work;
        int i3 = 1;
        if (i2 - i < QUICKSORT_THRESHOLD) {
            sort(a2, i, i2, true);
            return;
        }
        int[] run = new int[68];
        int i4 = 0;
        run[0] = i;
        int count = 0;
        int k = i;
        while (k < i2) {
            if (a2[k] >= a2[k + 1]) {
                if (a2[k] <= a2[k + 1]) {
                    int m = MAX_RUN_LENGTH;
                    while (true) {
                        k++;
                        if (k > i2 || a2[k - 1] != a2[k]) {
                            break;
                        }
                        m--;
                        if (m == 0) {
                            sort(a2, i, i2, true);
                            return;
                        }
                    }
                } else {
                    do {
                        k++;
                        if (k > i2) {
                            break;
                        }
                    } while (a2[k - 1] >= a2[k]);
                    int lo = run[count] - 1;
                    int hi = k;
                    while (true) {
                        lo++;
                        hi--;
                        if (lo >= hi) {
                            break;
                        }
                        float t = a2[lo];
                        a2[lo] = a2[hi];
                        a2[hi] = t;
                    }
                }
            } else {
                do {
                    k++;
                    if (k > i2) {
                        break;
                    }
                } while (a2[k - 1] <= a2[k]);
            }
            count++;
            if (count == MAX_RUN_COUNT) {
                sort(a2, i, i2, true);
                return;
            }
            run[count] = k;
        }
        int right2 = i2 + 1;
        if (run[count] == i2) {
            count++;
            run[count] = right2;
        } else if (count == 1) {
            return;
        }
        byte odd = 0;
        int n = 1;
        while (true) {
            int i5 = n << 1;
            n = i5;
            if (i5 >= count) {
                break;
            }
            odd = (byte) (odd ^ 1);
        }
        int blen = right2 - i;
        if (work2 == null) {
            int i6 = workLen;
        } else if (workLen >= blen && workBase + blen <= work2.length) {
            workBase2 = workBase;
            if (odd != 0) {
                System.arraycopy((Object) a2, i, (Object) work2, workBase2, blen);
                b = a2;
                bo = 0;
                a2 = work2;
                ao = workBase2 - i;
            } else {
                b = work2;
                ao = 0;
                bo = workBase2 - i;
            }
            while (count > i3) {
                int last = i4;
                int k2 = 2;
                while (true) {
                    int k3 = k2;
                    if (k3 > count) {
                        break;
                    }
                    int hi2 = run[k3];
                    int mi = run[k3 - 1];
                    int i7 = run[k3 - 2];
                    int blen2 = blen;
                    float[] work3 = work2;
                    int i8 = i7;
                    int p = i7;
                    int q = mi;
                    while (true) {
                        int q2 = q;
                        if (i8 >= hi2) {
                            break;
                        }
                        byte odd2 = odd;
                        int q3 = q2;
                        if (q3 >= hi2 || (p < mi && a2[p + ao] <= a2[q3 + ao])) {
                            b[i8 + bo] = a2[p + ao];
                            q = q3;
                            p++;
                        } else {
                            b[i8 + bo] = a2[q3 + ao];
                            q = q3 + 1;
                        }
                        i8++;
                        odd = odd2;
                    }
                    last++;
                    run[last] = hi2;
                    k2 = k3 + 2;
                    blen = blen2;
                    work2 = work3;
                    int i9 = left;
                }
                int blen3 = blen;
                float[] work4 = work2;
                byte odd3 = odd;
                if ((count & 1) != 0) {
                    int i10 = right2;
                    int lo2 = run[count - 1];
                    while (true) {
                        i10--;
                        if (i10 < lo2) {
                            break;
                        }
                        b[i10 + bo] = a2[i10 + ao];
                    }
                    last++;
                    run[last] = right2;
                }
                float[] t2 = a2;
                a2 = b;
                b = t2;
                int o = ao;
                ao = bo;
                bo = o;
                count = last;
                blen = blen3;
                work2 = work4;
                odd = odd3;
                int i11 = left;
                i3 = 1;
                i4 = 0;
            }
            float[] fArr = work2;
            byte b2 = odd;
        }
        work2 = new float[blen];
        workBase2 = 0;
        if (odd != 0) {
        }
        while (count > i3) {
        }
        float[] fArr2 = work2;
        byte b22 = odd;
    }

    private static void sort(float[] a, int left, int right, boolean leftmost) {
        int left2;
        float[] fArr = a;
        int k = left;
        int right2 = right;
        boolean z = leftmost;
        int length = (right2 - k) + 1;
        if (length < INSERTION_SORT_THRESHOLD) {
            if (z) {
                int j = k;
                int i = j;
                while (i < right2) {
                    float ai = fArr[i + 1];
                    while (true) {
                        if (ai >= fArr[j]) {
                            break;
                        }
                        fArr[j + 1] = fArr[j];
                        int j2 = j - 1;
                        if (j == k) {
                            j = j2;
                            break;
                        }
                        j = j2;
                    }
                    fArr[j + 1] = ai;
                    i++;
                    j = i;
                }
            } else {
                while (k < right2) {
                    k++;
                    if (fArr[k] < fArr[k - 1]) {
                        int left3 = k;
                        while (true) {
                            left2 = left3 + 1;
                            if (left2 > right2) {
                                break;
                            }
                            float a1 = fArr[k];
                            float a2 = fArr[left2];
                            if (a1 < a2) {
                                a2 = a1;
                                a1 = fArr[left2];
                            }
                            while (true) {
                                k--;
                                if (a1 >= fArr[k]) {
                                    break;
                                }
                                fArr[k + 2] = fArr[k];
                            }
                            int k2 = k + 1;
                            fArr[k2 + 1] = a1;
                            while (true) {
                                k2--;
                                if (a2 >= fArr[k2]) {
                                    break;
                                }
                                fArr[k2 + 1] = fArr[k2];
                            }
                            fArr[k2 + 1] = a2;
                            left3 = left2 + 1;
                            k = left3;
                        }
                        float last = fArr[right2];
                        while (true) {
                            right2--;
                            if (last >= fArr[right2]) {
                                break;
                            }
                            fArr[right2 + 1] = fArr[right2];
                        }
                        fArr[right2 + 1] = last;
                        float last2 = left2;
                    }
                }
                return;
            }
            return;
        }
        int seventh = (length >> 3) + (length >> 6) + 1;
        int e3 = (k + right2) >>> 1;
        int e2 = e3 - seventh;
        int e1 = e2 - seventh;
        int e4 = e3 + seventh;
        int e5 = e4 + seventh;
        if (fArr[e2] < fArr[e1]) {
            float t = fArr[e2];
            fArr[e2] = fArr[e1];
            fArr[e1] = t;
        }
        if (fArr[e3] < fArr[e2]) {
            float t2 = fArr[e3];
            fArr[e3] = fArr[e2];
            fArr[e2] = t2;
            if (t2 < fArr[e1]) {
                fArr[e2] = fArr[e1];
                fArr[e1] = t2;
            }
        }
        if (fArr[e4] < fArr[e3]) {
            float t3 = fArr[e4];
            fArr[e4] = fArr[e3];
            fArr[e3] = t3;
            if (t3 < fArr[e2]) {
                fArr[e3] = fArr[e2];
                fArr[e2] = t3;
                if (t3 < fArr[e1]) {
                    fArr[e2] = fArr[e1];
                    fArr[e1] = t3;
                }
            }
        }
        if (fArr[e5] < fArr[e4]) {
            float t4 = fArr[e5];
            fArr[e5] = fArr[e4];
            fArr[e4] = t4;
            if (t4 < fArr[e3]) {
                fArr[e4] = fArr[e3];
                fArr[e3] = t4;
                if (t4 < fArr[e2]) {
                    fArr[e3] = fArr[e2];
                    fArr[e2] = t4;
                    if (t4 < fArr[e1]) {
                        fArr[e2] = fArr[e1];
                        fArr[e1] = t4;
                    }
                }
            }
        }
        int less = k;
        int great = right2;
        if (fArr[e1] == fArr[e2] || fArr[e2] == fArr[e3] || fArr[e3] == fArr[e4] || fArr[e4] == fArr[e5]) {
            float pivot = fArr[e3];
            int less2 = less;
            while (less <= great) {
                if (fArr[less] != pivot) {
                    float ak = fArr[less];
                    if (ak < pivot) {
                        fArr[less] = fArr[less2];
                        fArr[less2] = ak;
                        less2++;
                    } else {
                        while (fArr[great] > pivot) {
                            great--;
                        }
                        if (fArr[great] < pivot) {
                            fArr[less] = fArr[less2];
                            fArr[less2] = fArr[great];
                            less2++;
                        } else {
                            fArr[less] = fArr[great];
                        }
                        fArr[great] = ak;
                        great--;
                    }
                }
                less++;
            }
            sort(fArr, k, less2 - 1, z);
            sort(fArr, great + 1, right2, false);
            int i2 = less2;
        } else {
            float pivot1 = fArr[e2];
            float pivot2 = fArr[e4];
            fArr[e2] = fArr[k];
            fArr[e4] = fArr[right2];
            do {
                less++;
            } while (fArr[less] < pivot1);
            do {
                great--;
            } while (fArr[great] > pivot2);
            int k3 = less - 1;
            loop9:
            while (true) {
                int k4 = k3 + 1;
                if (k4 > great) {
                    break;
                }
                float ak2 = fArr[k4];
                if (ak2 < pivot1) {
                    fArr[k4] = fArr[less];
                    fArr[less] = ak2;
                    less++;
                } else if (ak2 > pivot2) {
                    while (fArr[great] > pivot2) {
                        int great2 = great - 1;
                        if (great == k4) {
                            great = great2;
                            break loop9;
                        }
                        great = great2;
                    }
                    if (fArr[great] < pivot1) {
                        fArr[k4] = fArr[less];
                        fArr[less] = fArr[great];
                        less++;
                    } else {
                        fArr[k4] = fArr[great];
                    }
                    fArr[great] = ak2;
                    great--;
                } else {
                    continue;
                }
                k3 = k4;
            }
            fArr[k] = fArr[less - 1];
            fArr[less - 1] = pivot1;
            fArr[right2] = fArr[great + 1];
            fArr[great + 1] = pivot2;
            sort(fArr, k, less - 2, z);
            int i3 = length;
            sort(fArr, great + 2, right2, false);
            if (less < e1 && e5 < great) {
                while (fArr[less] == pivot1) {
                    less++;
                }
                while (fArr[great] == pivot2) {
                    great--;
                }
                int k5 = less - 1;
                loop13:
                while (true) {
                    k5++;
                    if (k5 > great) {
                        break;
                    }
                    float ak3 = fArr[k5];
                    if (ak3 == pivot1) {
                        fArr[k5] = fArr[less];
                        fArr[less] = ak3;
                        less++;
                    } else if (ak3 == pivot2) {
                        while (fArr[great] == pivot2) {
                            int great3 = great - 1;
                            if (great == k5) {
                                great = great3;
                                break loop13;
                            }
                            great = great3;
                        }
                        if (fArr[great] == pivot1) {
                            fArr[k5] = fArr[less];
                            fArr[less] = fArr[great];
                            less++;
                        } else {
                            fArr[k5] = fArr[great];
                        }
                        fArr[great] = ak3;
                        great--;
                    } else {
                        continue;
                    }
                }
            }
            sort(fArr, less, great, false);
        }
    }

    static void sort(double[] a, int left, int right, double[] work, int workBase, int workLen) {
        int left2 = left;
        int right2 = right;
        while (left2 <= right2 && Double.isNaN(a[right2])) {
            right2--;
        }
        int right3 = right2;
        while (true) {
            right2--;
            if (right2 < left2) {
                break;
            }
            double ak = a[right2];
            if (ak != ak) {
                a[right2] = a[right3];
                a[right3] = ak;
                right3--;
            }
        }
        doSort(a, left2, right3, work, workBase, workLen);
        int hi = right3;
        while (left2 < hi) {
            int middle = (left2 + hi) >>> 1;
            if (a[middle] < 0.0d) {
                left2 = middle + 1;
            } else {
                hi = middle;
            }
        }
        while (left2 <= right3 && Double.doubleToRawLongBits(a[left2]) < 0) {
            left2++;
        }
        int k = left2;
        int p = left2 - 1;
        while (true) {
            k++;
            if (k <= right3) {
                double ak2 = a[k];
                if (ak2 == 0.0d) {
                    if (Double.doubleToRawLongBits(ak2) < 0) {
                        a[k] = 0.0d;
                        p++;
                        a[p] = -0.0d;
                    }
                } else {
                    return;
                }
            } else {
                return;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:55:0x00b8  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00c1  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x00c7  */
    private static void doSort(double[] a, int left, int right, double[] work, int workBase, int workLen) {
        int workBase2;
        int ao;
        int bo;
        double[] b;
        double[] a2 = a;
        int i = left;
        int i2 = right;
        double[] work2 = work;
        int i3 = 1;
        if (i2 - i < QUICKSORT_THRESHOLD) {
            sort(a2, i, i2, true);
            return;
        }
        int[] run = new int[68];
        int i4 = 0;
        run[0] = i;
        int count = 0;
        int k = i;
        while (k < i2) {
            if (a2[k] >= a2[k + 1]) {
                if (a2[k] <= a2[k + 1]) {
                    int m = MAX_RUN_LENGTH;
                    while (true) {
                        k++;
                        if (k > i2 || a2[k - 1] != a2[k]) {
                            break;
                        }
                        m--;
                        if (m == 0) {
                            sort(a2, i, i2, true);
                            return;
                        }
                    }
                } else {
                    do {
                        k++;
                        if (k > i2) {
                            break;
                        }
                    } while (a2[k - 1] >= a2[k]);
                    int lo = run[count] - 1;
                    int hi = k;
                    while (true) {
                        lo++;
                        hi--;
                        if (lo >= hi) {
                            break;
                        }
                        double t = a2[lo];
                        a2[lo] = a2[hi];
                        a2[hi] = t;
                    }
                }
            } else {
                do {
                    k++;
                    if (k > i2) {
                        break;
                    }
                } while (a2[k - 1] <= a2[k]);
            }
            count++;
            if (count == MAX_RUN_COUNT) {
                sort(a2, i, i2, true);
                return;
            }
            run[count] = k;
        }
        int right2 = i2 + 1;
        if (run[count] == i2) {
            count++;
            run[count] = right2;
        } else if (count == 1) {
            return;
        }
        byte odd = 0;
        int n = 1;
        while (true) {
            int i5 = n << 1;
            n = i5;
            if (i5 >= count) {
                break;
            }
            odd = (byte) (odd ^ 1);
        }
        int blen = right2 - i;
        if (work2 == null) {
            int i6 = workLen;
        } else if (workLen >= blen && workBase + blen <= work2.length) {
            workBase2 = workBase;
            if (odd != 0) {
                System.arraycopy((Object) a2, i, (Object) work2, workBase2, blen);
                b = a2;
                bo = 0;
                a2 = work2;
                ao = workBase2 - i;
            } else {
                b = work2;
                ao = 0;
                bo = workBase2 - i;
            }
            while (count > i3) {
                int last = i4;
                int k2 = 2;
                while (true) {
                    int k3 = k2;
                    if (k3 > count) {
                        break;
                    }
                    int hi2 = run[k3];
                    int mi = run[k3 - 1];
                    int i7 = run[k3 - 2];
                    int blen2 = blen;
                    double[] work3 = work2;
                    int i8 = i7;
                    int p = i7;
                    int q = mi;
                    while (true) {
                        int q2 = q;
                        if (i8 >= hi2) {
                            break;
                        }
                        byte odd2 = odd;
                        int q3 = q2;
                        if (q3 >= hi2 || (p < mi && a2[p + ao] <= a2[q3 + ao])) {
                            b[i8 + bo] = a2[p + ao];
                            q = q3;
                            p++;
                        } else {
                            b[i8 + bo] = a2[q3 + ao];
                            q = q3 + 1;
                        }
                        i8++;
                        odd = odd2;
                    }
                    last++;
                    run[last] = hi2;
                    k2 = k3 + 2;
                    blen = blen2;
                    work2 = work3;
                    int i9 = left;
                }
                int blen3 = blen;
                double[] work4 = work2;
                byte odd3 = odd;
                if ((count & 1) != 0) {
                    int i10 = right2;
                    int lo2 = run[count - 1];
                    while (true) {
                        i10--;
                        if (i10 < lo2) {
                            break;
                        }
                        b[i10 + bo] = a2[i10 + ao];
                    }
                    last++;
                    run[last] = right2;
                }
                double[] t2 = a2;
                a2 = b;
                b = t2;
                int o = ao;
                ao = bo;
                bo = o;
                count = last;
                blen = blen3;
                work2 = work4;
                odd = odd3;
                int i11 = left;
                i3 = 1;
                i4 = 0;
            }
            double[] dArr = work2;
            byte b2 = odd;
        }
        work2 = new double[blen];
        workBase2 = 0;
        if (odd != 0) {
        }
        while (count > i3) {
        }
        double[] dArr2 = work2;
        byte b22 = odd;
    }

    private static void sort(double[] a, int left, int right, boolean leftmost) {
        int left2;
        double[] dArr = a;
        int k = left;
        int right2 = right;
        boolean z = leftmost;
        int length = (right2 - k) + 1;
        if (length < INSERTION_SORT_THRESHOLD) {
            if (z) {
                int j = k;
                int i = j;
                while (i < right2) {
                    double ai = dArr[i + 1];
                    while (true) {
                        if (ai >= dArr[j]) {
                            break;
                        }
                        dArr[j + 1] = dArr[j];
                        int j2 = j - 1;
                        if (j == k) {
                            j = j2;
                            break;
                        }
                        j = j2;
                    }
                    dArr[j + 1] = ai;
                    i++;
                    j = i;
                }
            } else {
                while (k < right2) {
                    k++;
                    if (dArr[k] < dArr[k - 1]) {
                        int left3 = k;
                        while (true) {
                            left2 = left3 + 1;
                            if (left2 > right2) {
                                break;
                            }
                            double a1 = dArr[k];
                            double a2 = dArr[left2];
                            if (a1 < a2) {
                                a2 = a1;
                                a1 = dArr[left2];
                            }
                            while (true) {
                                k--;
                                if (a1 >= dArr[k]) {
                                    break;
                                }
                                dArr[k + 2] = dArr[k];
                            }
                            int k2 = k + 1;
                            dArr[k2 + 1] = a1;
                            while (true) {
                                k2--;
                                if (a2 >= dArr[k2]) {
                                    break;
                                }
                                dArr[k2 + 1] = dArr[k2];
                            }
                            dArr[k2 + 1] = a2;
                            left3 = left2 + 1;
                            k = left3;
                        }
                        double last = dArr[right2];
                        while (true) {
                            right2--;
                            if (last >= dArr[right2]) {
                                break;
                            }
                            dArr[right2 + 1] = dArr[right2];
                        }
                        dArr[right2 + 1] = last;
                        int i2 = left2;
                    }
                }
                return;
            }
            return;
        }
        int seventh = (length >> 3) + (length >> 6) + 1;
        int e3 = (k + right2) >>> 1;
        int e2 = e3 - seventh;
        int e1 = e2 - seventh;
        int e4 = e3 + seventh;
        int e5 = e4 + seventh;
        if (dArr[e2] < dArr[e1]) {
            double t = dArr[e2];
            dArr[e2] = dArr[e1];
            dArr[e1] = t;
        }
        if (dArr[e3] < dArr[e2]) {
            double t2 = dArr[e3];
            dArr[e3] = dArr[e2];
            dArr[e2] = t2;
            if (t2 < dArr[e1]) {
                dArr[e2] = dArr[e1];
                dArr[e1] = t2;
            }
        }
        if (dArr[e4] < dArr[e3]) {
            double t3 = dArr[e4];
            dArr[e4] = dArr[e3];
            dArr[e3] = t3;
            if (t3 < dArr[e2]) {
                dArr[e3] = dArr[e2];
                dArr[e2] = t3;
                if (t3 < dArr[e1]) {
                    dArr[e2] = dArr[e1];
                    dArr[e1] = t3;
                }
            }
        }
        if (dArr[e5] < dArr[e4]) {
            double t4 = dArr[e5];
            dArr[e5] = dArr[e4];
            dArr[e4] = t4;
            if (t4 < dArr[e3]) {
                dArr[e4] = dArr[e3];
                dArr[e3] = t4;
                if (t4 < dArr[e2]) {
                    dArr[e3] = dArr[e2];
                    dArr[e2] = t4;
                    if (t4 < dArr[e1]) {
                        dArr[e2] = dArr[e1];
                        dArr[e1] = t4;
                    }
                }
            }
        }
        int less = k;
        int great = right2;
        if (dArr[e1] == dArr[e2] || dArr[e2] == dArr[e3] || dArr[e3] == dArr[e4] || dArr[e4] == dArr[e5]) {
            double pivot = dArr[e3];
            int less2 = less;
            while (less <= great) {
                if (dArr[less] != pivot) {
                    double ak = dArr[less];
                    if (ak < pivot) {
                        dArr[less] = dArr[less2];
                        dArr[less2] = ak;
                        less2++;
                    } else {
                        while (dArr[great] > pivot) {
                            great--;
                        }
                        if (dArr[great] < pivot) {
                            dArr[less] = dArr[less2];
                            dArr[less2] = dArr[great];
                            less2++;
                        } else {
                            dArr[less] = dArr[great];
                        }
                        dArr[great] = ak;
                        great--;
                    }
                }
                less++;
            }
            sort(dArr, k, less2 - 1, z);
            sort(dArr, great + 1, right2, false);
            int i3 = less2;
        } else {
            double pivot1 = dArr[e2];
            double pivot2 = dArr[e4];
            dArr[e2] = dArr[k];
            dArr[e4] = dArr[right2];
            do {
                less++;
            } while (dArr[less] < pivot1);
            do {
                great--;
            } while (dArr[great] > pivot2);
            int k3 = less - 1;
            loop9:
            while (true) {
                k3++;
                if (k3 > great) {
                    break;
                }
                double ak2 = dArr[k3];
                if (ak2 < pivot1) {
                    dArr[k3] = dArr[less];
                    dArr[less] = ak2;
                    less++;
                } else if (ak2 > pivot2) {
                    while (dArr[great] > pivot2) {
                        int great2 = great - 1;
                        if (great == k3) {
                            great = great2;
                            break loop9;
                        }
                        great = great2;
                    }
                    if (dArr[great] < pivot1) {
                        dArr[k3] = dArr[less];
                        dArr[less] = dArr[great];
                        less++;
                    } else {
                        dArr[k3] = dArr[great];
                    }
                    dArr[great] = ak2;
                    great--;
                } else {
                    continue;
                }
            }
            dArr[k] = dArr[less - 1];
            dArr[less - 1] = pivot1;
            dArr[right2] = dArr[great + 1];
            dArr[great + 1] = pivot2;
            sort(dArr, k, less - 2, z);
            sort(dArr, great + 2, right2, false);
            if (less < e1 && e5 < great) {
                while (dArr[less] == pivot1) {
                    less++;
                }
                while (dArr[great] == pivot2) {
                    great--;
                }
                int k4 = less - 1;
                loop13:
                while (true) {
                    k4++;
                    if (k4 > great) {
                        break;
                    }
                    double ak3 = dArr[k4];
                    if (ak3 == pivot1) {
                        dArr[k4] = dArr[less];
                        dArr[less] = ak3;
                        less++;
                    } else if (ak3 == pivot2) {
                        while (dArr[great] == pivot2) {
                            int great3 = great - 1;
                            if (great == k4) {
                                great = great3;
                                break loop13;
                            }
                            great = great3;
                        }
                        if (dArr[great] == pivot1) {
                            dArr[k4] = dArr[less];
                            dArr[less] = dArr[great];
                            less++;
                        } else {
                            dArr[k4] = dArr[great];
                        }
                        dArr[great] = ak3;
                        great--;
                    } else {
                        continue;
                    }
                }
            }
            sort(dArr, less, great, false);
        }
    }
}
