package java.util;

class ComparableTimSort {
    static final /* synthetic */ boolean -assertionsDisabled = (ComparableTimSort.class.desiredAssertionStatus() ^ 1);
    private static final int INITIAL_TMP_STORAGE_LENGTH = 256;
    private static final int MIN_GALLOP = 7;
    private static final int MIN_MERGE = 32;
    private final Object[] a;
    private int minGallop = 7;
    private final int[] runBase;
    private final int[] runLen;
    private int stackSize = 0;
    private Object[] tmp;
    private int tmpBase;
    private int tmpLen;

    private ComparableTimSort(Object[] a, Object[] work, int workBase, int workLen) {
        this.a = a;
        int len = a.length;
        int tlen = len < 512 ? len >>> 1 : 256;
        if (work == null || workLen < tlen || workBase + tlen > work.length) {
            this.tmp = new Object[tlen];
            this.tmpBase = 0;
            this.tmpLen = tlen;
        } else {
            this.tmp = work;
            this.tmpBase = workBase;
            this.tmpLen = workLen;
        }
        int stackLen = len < 120 ? 5 : len < 1542 ? 10 : len < 119151 ? 24 : 49;
        this.runBase = new int[stackLen];
        this.runLen = new int[stackLen];
    }

    static void sort(Object[] a, int lo, int hi, Object[] work, int workBase, int workLen) {
        if (-assertionsDisabled || (a != null && lo >= 0 && lo <= hi && hi <= a.length)) {
            int nRemaining = hi - lo;
            if (nRemaining >= 2) {
                if (nRemaining < 32) {
                    binarySort(a, lo, hi, lo + countRunAndMakeAscending(a, lo, hi));
                    return;
                }
                ComparableTimSort ts = new ComparableTimSort(a, work, workBase, workLen);
                int minRun = minRunLength(nRemaining);
                do {
                    int runLen = countRunAndMakeAscending(a, lo, hi);
                    if (runLen < minRun) {
                        int force = nRemaining <= minRun ? nRemaining : minRun;
                        binarySort(a, lo, lo + force, lo + runLen);
                        runLen = force;
                    }
                    ts.pushRun(lo, runLen);
                    ts.mergeCollapse();
                    lo += runLen;
                    nRemaining -= runLen;
                } while (nRemaining != 0);
                if (-assertionsDisabled || lo == hi) {
                    ts.mergeForceCollapse();
                    if (!-assertionsDisabled && ts.stackSize != 1) {
                        throw new AssertionError();
                    }
                    return;
                }
                throw new AssertionError();
            }
            return;
        }
        throw new AssertionError();
    }

    private static void binarySort(Object[] a, int lo, int hi, int start) {
        if (-assertionsDisabled || (lo <= start && start <= hi)) {
            if (start == lo) {
                start++;
            }
            while (start < hi) {
                Comparable pivot = a[start];
                int left = lo;
                int right = start;
                if (-assertionsDisabled || lo <= right) {
                    while (left < right) {
                        int mid = (left + right) >>> 1;
                        if (pivot.compareTo(a[mid]) < 0) {
                            right = mid;
                        } else {
                            left = mid + 1;
                        }
                    }
                    if (-assertionsDisabled || left == right) {
                        int n = start - left;
                        switch (n) {
                            case 1:
                                break;
                            case 2:
                                a[left + 2] = a[left + 1];
                                break;
                            default:
                                System.arraycopy((Object) a, left, (Object) a, left + 1, n);
                                continue;
                        }
                        a[left + 1] = a[left];
                        a[left] = pivot;
                        start++;
                    } else {
                        throw new AssertionError();
                    }
                }
                throw new AssertionError();
            }
            return;
        }
        throw new AssertionError();
    }

    private static int countRunAndMakeAscending(Object[] a, int lo, int hi) {
        if (-assertionsDisabled || lo < hi) {
            int runHi = lo + 1;
            if (runHi == hi) {
                return 1;
            }
            int runHi2 = runHi + 1;
            if (((Comparable) a[runHi]).compareTo(a[lo]) < 0) {
                runHi = runHi2;
                while (runHi < hi && ((Comparable) a[runHi]).compareTo(a[runHi - 1]) < 0) {
                    runHi++;
                }
                reverseRange(a, lo, runHi);
            } else {
                runHi = runHi2;
                while (runHi < hi && ((Comparable) a[runHi]).compareTo(a[runHi - 1]) >= 0) {
                    runHi++;
                }
            }
            return runHi - lo;
        }
        throw new AssertionError();
    }

    private static void reverseRange(Object[] a, int lo, int hi) {
        int hi2 = hi - 1;
        int i = lo;
        while (i < hi2) {
            Object t = a[i];
            lo = i + 1;
            a[i] = a[hi2];
            hi = hi2 - 1;
            a[hi2] = t;
            hi2 = hi;
            i = lo;
        }
    }

    private static int minRunLength(int n) {
        if (-assertionsDisabled || n >= 0) {
            int r = 0;
            while (n >= 32) {
                r |= n & 1;
                n >>= 1;
            }
            return n + r;
        }
        throw new AssertionError();
    }

    private void pushRun(int runBase, int runLen) {
        this.runBase[this.stackSize] = runBase;
        this.runLen[this.stackSize] = runLen;
        this.stackSize++;
    }

    private void mergeCollapse() {
        while (this.stackSize > 1) {
            int n = this.stackSize - 2;
            if (n > 0 && this.runLen[n - 1] <= this.runLen[n] + this.runLen[n + 1]) {
                if (this.runLen[n - 1] < this.runLen[n + 1]) {
                    n--;
                }
                mergeAt(n);
            } else if (this.runLen[n] <= this.runLen[n + 1]) {
                mergeAt(n);
            } else {
                return;
            }
        }
    }

    private void mergeForceCollapse() {
        while (this.stackSize > 1) {
            int n = this.stackSize - 2;
            if (n > 0 && this.runLen[n - 1] < this.runLen[n + 1]) {
                n--;
            }
            mergeAt(n);
        }
    }

    private void mergeAt(int i) {
        if (!-assertionsDisabled && this.stackSize < 2) {
            throw new AssertionError();
        } else if (!-assertionsDisabled && i < 0) {
            throw new AssertionError();
        } else if (-assertionsDisabled || i == this.stackSize - 2 || i == this.stackSize - 3) {
            int base1 = this.runBase[i];
            int len1 = this.runLen[i];
            int base2 = this.runBase[i + 1];
            int len2 = this.runLen[i + 1];
            if (!-assertionsDisabled && (len1 <= 0 || len2 <= 0)) {
                throw new AssertionError();
            } else if (-assertionsDisabled || base1 + len1 == base2) {
                this.runLen[i] = len1 + len2;
                if (i == this.stackSize - 3) {
                    this.runBase[i + 1] = this.runBase[i + 2];
                    this.runLen[i + 1] = this.runLen[i + 2];
                }
                this.stackSize--;
                int k = gallopRight((Comparable) this.a[base2], this.a, base1, len1, 0);
                if (-assertionsDisabled || k >= 0) {
                    base1 += k;
                    len1 -= k;
                    if (len1 != 0) {
                        len2 = gallopLeft((Comparable) this.a[(base1 + len1) - 1], this.a, base2, len2, len2 - 1);
                        if (!-assertionsDisabled && len2 < 0) {
                            throw new AssertionError();
                        } else if (len2 != 0) {
                            if (len1 <= len2) {
                                mergeLo(base1, len1, base2, len2);
                            } else {
                                mergeHi(base1, len1, base2, len2);
                            }
                            return;
                        } else {
                            return;
                        }
                    }
                    return;
                }
                throw new AssertionError();
            } else {
                throw new AssertionError();
            }
        } else {
            throw new AssertionError();
        }
    }

    private static int gallopLeft(Comparable<Object> key, Object[] a, int base, int len, int hint) {
        if (-assertionsDisabled || (len > 0 && hint >= 0 && hint < len)) {
            int lastOfs = 0;
            int ofs = 1;
            int maxOfs;
            if (key.compareTo(a[base + hint]) > 0) {
                maxOfs = len - hint;
                while (ofs < maxOfs && key.compareTo(a[(base + hint) + ofs]) > 0) {
                    lastOfs = ofs;
                    ofs = (ofs << 1) + 1;
                    if (ofs <= 0) {
                        ofs = maxOfs;
                    }
                }
                if (ofs > maxOfs) {
                    ofs = maxOfs;
                }
                lastOfs += hint;
                ofs += hint;
            } else {
                maxOfs = hint + 1;
                while (ofs < maxOfs && key.compareTo(a[(base + hint) - ofs]) <= 0) {
                    lastOfs = ofs;
                    ofs = (ofs << 1) + 1;
                    if (ofs <= 0) {
                        ofs = maxOfs;
                    }
                }
                if (ofs > maxOfs) {
                    ofs = maxOfs;
                }
                int tmp = lastOfs;
                lastOfs = hint - ofs;
                ofs = hint - tmp;
            }
            if (-assertionsDisabled || (-1 <= lastOfs && lastOfs < ofs && ofs <= len)) {
                lastOfs++;
                while (lastOfs < ofs) {
                    int m = lastOfs + ((ofs - lastOfs) >>> 1);
                    if (key.compareTo(a[base + m]) > 0) {
                        lastOfs = m + 1;
                    } else {
                        ofs = m;
                    }
                }
                if (-assertionsDisabled || lastOfs == ofs) {
                    return ofs;
                }
                throw new AssertionError();
            }
            throw new AssertionError();
        }
        throw new AssertionError();
    }

    private static int gallopRight(Comparable<Object> key, Object[] a, int base, int len, int hint) {
        if (-assertionsDisabled || (len > 0 && hint >= 0 && hint < len)) {
            int ofs = 1;
            int lastOfs = 0;
            int maxOfs;
            if (key.compareTo(a[base + hint]) < 0) {
                maxOfs = hint + 1;
                while (ofs < maxOfs && key.compareTo(a[(base + hint) - ofs]) < 0) {
                    lastOfs = ofs;
                    ofs = (ofs << 1) + 1;
                    if (ofs <= 0) {
                        ofs = maxOfs;
                    }
                }
                if (ofs > maxOfs) {
                    ofs = maxOfs;
                }
                int tmp = lastOfs;
                lastOfs = hint - ofs;
                ofs = hint - tmp;
            } else {
                maxOfs = len - hint;
                while (ofs < maxOfs && key.compareTo(a[(base + hint) + ofs]) >= 0) {
                    lastOfs = ofs;
                    ofs = (ofs << 1) + 1;
                    if (ofs <= 0) {
                        ofs = maxOfs;
                    }
                }
                if (ofs > maxOfs) {
                    ofs = maxOfs;
                }
                lastOfs += hint;
                ofs += hint;
            }
            if (-assertionsDisabled || (-1 <= lastOfs && lastOfs < ofs && ofs <= len)) {
                lastOfs++;
                while (lastOfs < ofs) {
                    int m = lastOfs + ((ofs - lastOfs) >>> 1);
                    if (key.compareTo(a[base + m]) < 0) {
                        ofs = m;
                    } else {
                        lastOfs = m + 1;
                    }
                }
                if (-assertionsDisabled || lastOfs == ofs) {
                    return ofs;
                }
                throw new AssertionError();
            }
            throw new AssertionError();
        }
        throw new AssertionError();
    }

    /* JADX WARNING: Missing block: B:24:0x005d, code:
            throw new java.lang.AssertionError();
     */
    /* JADX WARNING: Missing block: B:52:0x00bd, code:
            throw new java.lang.AssertionError();
     */
    /* JADX WARNING: Missing block: B:78:0x0127, code:
            if (r10 >= 0) goto L_0x012a;
     */
    /* JADX WARNING: Missing block: B:79:0x0129, code:
            r10 = 0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void mergeLo(int base1, int len1, int base2, int len2) {
        if (-assertionsDisabled || (len1 > 0 && len2 > 0 && base1 + len1 == base2)) {
            Object a = this.a;
            Object tmp = ensureCapacity(len1);
            int cursor1 = this.tmpBase;
            int cursor2 = base2;
            int dest = base1;
            System.arraycopy(a, base1, tmp, cursor1, len1);
            dest = base1 + 1;
            cursor2 = base2 + 1;
            a[base1] = a[base2];
            len2--;
            if (len2 == 0) {
                System.arraycopy(tmp, cursor1, a, dest, len1);
                return;
            } else if (len1 == 1) {
                System.arraycopy(a, cursor2, a, dest, len2);
                a[dest + len2] = tmp[cursor1];
                return;
            } else {
                int minGallop = this.minGallop;
                loop0:
                while (true) {
                    int dest2;
                    int cursor22;
                    int cursor12;
                    int count1 = 0;
                    int count2 = 0;
                    do {
                        if (-assertionsDisabled || (len1 > 1 && len2 > 0)) {
                            if (((Comparable) a[cursor2]).compareTo(tmp[cursor1]) < 0) {
                                dest2 = dest + 1;
                                cursor22 = cursor2 + 1;
                                a[dest] = a[cursor2];
                                count2++;
                                count1 = 0;
                                len2--;
                                if (len2 == 0) {
                                    dest = dest2;
                                    cursor2 = cursor22;
                                    break loop0;
                                }
                                dest = dest2;
                                cursor2 = cursor22;
                            } else {
                                dest2 = dest + 1;
                                cursor12 = cursor1 + 1;
                                a[dest] = tmp[cursor1];
                                count1++;
                                count2 = 0;
                                len1--;
                                if (len1 == 1) {
                                    dest = dest2;
                                    cursor1 = cursor12;
                                    break loop0;
                                }
                                dest = dest2;
                                cursor1 = cursor12;
                            }
                        }
                    } while ((count1 | count2) < minGallop);
                    while (true) {
                        if (-assertionsDisabled || (len1 > 1 && len2 > 0)) {
                            count1 = gallopRight((Comparable) a[cursor2], tmp, cursor1, len1, 0);
                            if (count1 != 0) {
                                System.arraycopy(tmp, cursor1, a, dest, count1);
                                dest += count1;
                                cursor1 += count1;
                                len1 -= count1;
                                if (len1 <= 1) {
                                    break loop0;
                                }
                            }
                            dest2 = dest + 1;
                            cursor22 = cursor2 + 1;
                            a[dest] = a[cursor2];
                            len2--;
                            if (len2 != 0) {
                                count2 = gallopLeft((Comparable) tmp[cursor1], a, cursor22, len2, 0);
                                if (count2 != 0) {
                                    System.arraycopy(a, cursor22, a, dest2, count2);
                                    dest = dest2 + count2;
                                    cursor2 = cursor22 + count2;
                                    len2 -= count2;
                                    if (len2 == 0) {
                                        break loop0;
                                    }
                                }
                                dest = dest2;
                                cursor2 = cursor22;
                                dest2 = dest + 1;
                                cursor12 = cursor1 + 1;
                                a[dest] = tmp[cursor1];
                                len1--;
                                if (len1 != 1) {
                                    minGallop--;
                                    if (((count2 >= 7 ? 1 : 0) | (count1 >= 7 ? 1 : 0)) == 0) {
                                        break;
                                    }
                                    dest = dest2;
                                    cursor1 = cursor12;
                                } else {
                                    dest = dest2;
                                    cursor1 = cursor12;
                                    break loop0;
                                }
                            }
                            dest = dest2;
                            cursor2 = cursor22;
                            break loop0;
                        }
                    }
                    minGallop += 2;
                    dest = dest2;
                    cursor1 = cursor12;
                }
                if (minGallop < 1) {
                    minGallop = 1;
                }
                this.minGallop = minGallop;
                if (len1 == 1) {
                    if (-assertionsDisabled || len2 > 0) {
                        System.arraycopy(a, cursor2, a, dest, len2);
                        a[dest + len2] = tmp[cursor1];
                    } else {
                        throw new AssertionError();
                    }
                } else if (len1 == 0) {
                    throw new IllegalArgumentException("Comparison method violates its general contract!");
                } else if (!-assertionsDisabled && len2 != 0) {
                    throw new AssertionError();
                } else if (-assertionsDisabled || len1 > 1) {
                    System.arraycopy(tmp, cursor1, a, dest, len1);
                } else {
                    throw new AssertionError();
                }
                return;
            }
        }
        throw new AssertionError();
    }

    /* JADX WARNING: Missing block: B:24:0x007c, code:
            throw new java.lang.AssertionError();
     */
    /* JADX WARNING: Missing block: B:52:0x00de, code:
            throw new java.lang.AssertionError();
     */
    /* JADX WARNING: Missing block: B:78:0x0159, code:
            if (r11 >= 0) goto L_0x015c;
     */
    /* JADX WARNING: Missing block: B:79:0x015b, code:
            r11 = 0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void mergeHi(int base1, int len1, int base2, int len2) {
        if (-assertionsDisabled || (len1 > 0 && len2 > 0 && base1 + len1 == base2)) {
            Object a = this.a;
            Object tmp = ensureCapacity(len2);
            int tmpBase = this.tmpBase;
            System.arraycopy(a, base2, tmp, tmpBase, len2);
            int cursor1 = (base1 + len1) - 1;
            int cursor2 = (tmpBase + len2) - 1;
            int i = (base2 + len2) - 1;
            int dest = i - 1;
            int cursor12 = cursor1 - 1;
            a[i] = a[cursor1];
            len1--;
            if (len1 == 0) {
                System.arraycopy(tmp, tmpBase, a, dest - (len2 - 1), len2);
                return;
            } else if (len2 == 1) {
                i = dest - len1;
                System.arraycopy(a, (cursor12 - len1) + 1, a, i + 1, len1);
                a[i] = tmp[cursor2];
                return;
            } else {
                int minGallop = this.minGallop;
                loop0:
                while (true) {
                    int cursor22;
                    i = dest;
                    cursor1 = cursor12;
                    int count1 = 0;
                    int count2 = 0;
                    do {
                        if (-assertionsDisabled || (len1 > 0 && len2 > 1)) {
                            if (((Comparable) tmp[cursor2]).compareTo(a[cursor1]) < 0) {
                                dest = i - 1;
                                cursor12 = cursor1 - 1;
                                a[i] = a[cursor1];
                                count1++;
                                count2 = 0;
                                len1--;
                                if (len1 == 0) {
                                    i = dest;
                                    cursor1 = cursor12;
                                    break loop0;
                                }
                                i = dest;
                                cursor1 = cursor12;
                            } else {
                                dest = i - 1;
                                cursor22 = cursor2 - 1;
                                a[i] = tmp[cursor2];
                                count2++;
                                count1 = 0;
                                len2--;
                                if (len2 == 1) {
                                    i = dest;
                                    cursor2 = cursor22;
                                    break loop0;
                                }
                                i = dest;
                                cursor2 = cursor22;
                            }
                        }
                    } while ((count1 | count2) < minGallop);
                    while (true) {
                        if (-assertionsDisabled || (len1 > 0 && len2 > 1)) {
                            count1 = len1 - gallopRight((Comparable) tmp[cursor2], a, base1, len1, len1 - 1);
                            if (count1 != 0) {
                                i -= count1;
                                cursor1 -= count1;
                                len1 -= count1;
                                System.arraycopy(a, cursor1 + 1, a, i + 1, count1);
                                if (len1 == 0) {
                                    break loop0;
                                }
                            }
                            dest = i - 1;
                            cursor22 = cursor2 - 1;
                            a[i] = tmp[cursor2];
                            len2--;
                            if (len2 != 1) {
                                count2 = len2 - gallopLeft((Comparable) a[cursor1], tmp, tmpBase, len2, len2 - 1);
                                if (count2 != 0) {
                                    i = dest - count2;
                                    cursor2 = cursor22 - count2;
                                    len2 -= count2;
                                    System.arraycopy(tmp, cursor2 + 1, a, i + 1, count2);
                                    if (len2 <= 1) {
                                        break loop0;
                                    }
                                }
                                i = dest;
                                cursor2 = cursor22;
                                dest = i - 1;
                                cursor12 = cursor1 - 1;
                                a[i] = a[cursor1];
                                len1--;
                                if (len1 != 0) {
                                    minGallop--;
                                    if (((count2 >= 7 ? 1 : 0) | (count1 >= 7 ? 1 : 0)) == 0) {
                                        break;
                                    }
                                    i = dest;
                                    cursor1 = cursor12;
                                } else {
                                    i = dest;
                                    cursor1 = cursor12;
                                    break loop0;
                                }
                            }
                            i = dest;
                            cursor2 = cursor22;
                            break loop0;
                        }
                    }
                    minGallop += 2;
                }
                if (minGallop < 1) {
                    minGallop = 1;
                }
                this.minGallop = minGallop;
                if (len2 == 1) {
                    if (-assertionsDisabled || len1 > 0) {
                        i -= len1;
                        System.arraycopy(a, (cursor1 - len1) + 1, a, i + 1, len1);
                        a[i] = tmp[cursor2];
                    } else {
                        throw new AssertionError();
                    }
                } else if (len2 == 0) {
                    throw new IllegalArgumentException("Comparison method violates its general contract!");
                } else if (!-assertionsDisabled && len1 != 0) {
                    throw new AssertionError();
                } else if (-assertionsDisabled || len2 > 0) {
                    System.arraycopy(tmp, tmpBase, a, i - (len2 - 1), len2);
                } else {
                    throw new AssertionError();
                }
                return;
            }
        }
        throw new AssertionError();
    }

    private Object[] ensureCapacity(int minCapacity) {
        if (this.tmpLen < minCapacity) {
            int newSize = minCapacity;
            newSize = minCapacity | (minCapacity >> 1);
            newSize |= newSize >> 2;
            newSize |= newSize >> 4;
            newSize |= newSize >> 8;
            newSize = (newSize | (newSize >> 16)) + 1;
            if (newSize < 0) {
                newSize = minCapacity;
            } else {
                newSize = Math.min(newSize, this.a.length >>> 1);
            }
            this.tmp = new Object[newSize];
            this.tmpLen = newSize;
            this.tmpBase = 0;
        }
        return this.tmp;
    }
}
