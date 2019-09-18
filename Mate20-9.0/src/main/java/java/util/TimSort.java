package java.util;

import java.lang.reflect.Array;

class TimSort<T> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int INITIAL_TMP_STORAGE_LENGTH = 256;
    private static final int MIN_GALLOP = 7;
    private static final int MIN_MERGE = 32;
    private final T[] a;
    private final Comparator<? super T> c;
    private int minGallop = 7;
    private final int[] runBase;
    private final int[] runLen;
    private int stackSize = 0;
    private T[] tmp;
    private int tmpBase;
    private int tmpLen;

    private TimSort(T[] a2, Comparator<? super T> c2, T[] work, int workBase, int workLen) {
        int stackLen;
        this.a = a2;
        this.c = c2;
        int len = a2.length;
        int tlen = len < 512 ? len >>> 1 : 256;
        if (work == null || workLen < tlen || workBase + tlen > work.length) {
            this.tmp = (Object[]) Array.newInstance(a2.getClass().getComponentType(), tlen);
            this.tmpBase = 0;
            this.tmpLen = tlen;
        } else {
            this.tmp = work;
            this.tmpBase = workBase;
            this.tmpLen = workLen;
        }
        if (len < 120) {
            stackLen = 5;
        } else if (len < 1542) {
            stackLen = 10;
        } else {
            stackLen = len < 119151 ? 24 : 49;
        }
        this.runBase = new int[stackLen];
        this.runLen = new int[stackLen];
    }

    static <T> void sort(T[] a2, int lo, int hi, Comparator<? super T> c2, T[] work, int workBase, int workLen) {
        int nRemaining = hi - lo;
        if (nRemaining >= 2) {
            if (nRemaining < 32) {
                binarySort(a2, lo, hi, lo + countRunAndMakeAscending(a2, lo, hi, c2), c2);
                return;
            }
            TimSort timSort = new TimSort(a2, c2, work, workBase, workLen);
            int minRun = minRunLength(nRemaining);
            do {
                int runLen2 = countRunAndMakeAscending(a2, lo, hi, c2);
                if (runLen2 < minRun) {
                    int force = nRemaining <= minRun ? nRemaining : minRun;
                    binarySort(a2, lo, lo + force, lo + runLen2, c2);
                    runLen2 = force;
                }
                timSort.pushRun(lo, runLen2);
                timSort.mergeCollapse();
                lo += runLen2;
                nRemaining -= runLen2;
            } while (nRemaining != 0);
            timSort.mergeForceCollapse();
        }
    }

    private static <T> void binarySort(T[] a2, int lo, int hi, int start, Comparator<? super T> c2) {
        if (start == lo) {
            start++;
        }
        while (start < hi) {
            T pivot = a2[start];
            int left = lo;
            int right = start;
            while (left < right) {
                int mid = (left + right) >>> 1;
                if (c2.compare(pivot, a2[mid]) < 0) {
                    right = mid;
                } else {
                    left = mid + 1;
                }
            }
            int n = start - left;
            switch (n) {
                case 1:
                    break;
                case 2:
                    a2[left + 2] = a2[left + 1];
                    break;
                default:
                    System.arraycopy((Object) a2, left, (Object) a2, left + 1, n);
                    continue;
            }
            a2[left + 1] = a2[left];
            a2[left] = pivot;
            start++;
        }
    }

    private static <T> int countRunAndMakeAscending(T[] a2, int lo, int hi, Comparator<? super T> c2) {
        int runHi = lo + 1;
        if (runHi == hi) {
            return 1;
        }
        int runHi2 = runHi + 1;
        if (c2.compare(a2[runHi], a2[lo]) < 0) {
            while (runHi2 < hi && c2.compare(a2[runHi2], a2[runHi2 - 1]) < 0) {
                runHi2++;
            }
            reverseRange(a2, lo, runHi2);
        } else {
            while (runHi2 < hi && c2.compare(a2[runHi2], a2[runHi2 - 1]) >= 0) {
                runHi2++;
            }
        }
        return runHi2 - lo;
    }

    private static void reverseRange(Object[] a2, int hi, int hi2) {
        int hi3 = hi2 - 1;
        while (hi < hi3) {
            Object t = a2[hi];
            a2[hi] = a2[hi3];
            a2[hi3] = t;
            hi3--;
            hi++;
        }
    }

    private static int minRunLength(int n) {
        int r = 0;
        while (n >= 32) {
            r |= n & 1;
            n >>= 1;
        }
        return n + r;
    }

    private void pushRun(int runBase2, int runLen2) {
        this.runBase[this.stackSize] = runBase2;
        this.runLen[this.stackSize] = runLen2;
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
        int i2 = i;
        int base1 = this.runBase[i2];
        int len1 = this.runLen[i2];
        int base2 = this.runBase[i2 + 1];
        int len2 = this.runLen[i2 + 1];
        this.runLen[i2] = len1 + len2;
        if (i2 == this.stackSize - 3) {
            this.runBase[i2 + 1] = this.runBase[i2 + 2];
            this.runLen[i2 + 1] = this.runLen[i2 + 2];
        }
        this.stackSize--;
        int k = gallopRight(this.a[base2], this.a, base1, len1, 0, this.c);
        int base12 = base1 + k;
        int len12 = len1 - k;
        if (len12 != 0) {
            int base22 = base2;
            int len22 = gallopLeft(this.a[(base12 + len12) - 1], this.a, base2, len2, len2 - 1, this.c);
            if (len22 != 0) {
                if (len12 <= len22) {
                    mergeLo(base12, len12, base22, len22);
                } else {
                    mergeHi(base12, len12, base22, len22);
                }
            }
        }
    }

    private static <T> int gallopLeft(T key, T[] a2, int base, int len, int hint, Comparator<? super T> c2) {
        int ofs;
        int lastOfs;
        int lastOfs2 = 0;
        int ofs2 = 1;
        if (c2.compare(key, a2[base + hint]) > 0) {
            int maxOfs = len - hint;
            while (ofs2 < maxOfs && c2.compare(key, a2[base + hint + ofs2]) > 0) {
                lastOfs2 = ofs2;
                ofs2 = (ofs2 << 1) + 1;
                if (ofs2 <= 0) {
                    ofs2 = maxOfs;
                }
            }
            if (ofs2 > maxOfs) {
                ofs2 = maxOfs;
            }
            lastOfs = lastOfs2 + hint;
            ofs = ofs2 + hint;
        } else {
            int maxOfs2 = hint + 1;
            while (ofs2 < maxOfs2 && c2.compare(key, a2[(base + hint) - ofs2]) <= 0) {
                lastOfs2 = ofs2;
                int ofs3 = (ofs2 << 1) + 1;
                if (ofs3 <= 0) {
                    ofs3 = maxOfs2;
                }
            }
            if (ofs2 > maxOfs2) {
                ofs2 = maxOfs2;
            }
            int tmp2 = lastOfs2;
            lastOfs = hint - ofs2;
            ofs = hint - tmp2;
        }
        int lastOfs3 = lastOfs + 1;
        while (lastOfs3 < ofs) {
            int m = ((ofs - lastOfs3) >>> 1) + lastOfs3;
            if (c2.compare(key, a2[base + m]) > 0) {
                lastOfs3 = m + 1;
            } else {
                ofs = m;
            }
        }
        return ofs;
    }

    private static <T> int gallopRight(T key, T[] a2, int base, int len, int hint, Comparator<? super T> c2) {
        int lastOfs;
        int ofs;
        int ofs2 = 1;
        int lastOfs2 = 0;
        if (c2.compare(key, a2[base + hint]) < 0) {
            int maxOfs = hint + 1;
            while (ofs2 < maxOfs && c2.compare(key, a2[(base + hint) - ofs2]) < 0) {
                lastOfs2 = ofs2;
                ofs2 = (ofs2 << 1) + 1;
                if (ofs2 <= 0) {
                    ofs2 = maxOfs;
                }
            }
            if (ofs2 > maxOfs) {
                ofs2 = maxOfs;
            }
            int tmp2 = lastOfs2;
            lastOfs = hint - ofs2;
            ofs = hint - tmp2;
        } else {
            int maxOfs2 = len - hint;
            while (ofs2 < maxOfs2 && c2.compare(key, a2[base + hint + ofs2]) >= 0) {
                lastOfs2 = ofs2;
                int ofs3 = (ofs2 << 1) + 1;
                if (ofs3 <= 0) {
                    ofs3 = maxOfs2;
                }
            }
            if (ofs2 > maxOfs2) {
                ofs2 = maxOfs2;
            }
            lastOfs = lastOfs2 + hint;
            ofs = ofs2 + hint;
        }
        int lastOfs3 = lastOfs + 1;
        while (lastOfs3 < ofs) {
            int m = ((ofs - lastOfs3) >>> 1) + lastOfs3;
            if (c2.compare(key, a2[base + m]) < 0) {
                ofs = m;
            } else {
                lastOfs3 = m + 1;
            }
        }
        return ofs;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x007f, code lost:
        r18 = r1;
        r13 = r3;
        r17 = r4;
        r15 = r6;
        r6 = r12;
        r16 = r14;
        r12 = r2;
        r14 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x008a, code lost:
        r10 = r6;
        r6 = gallopRight(r7[r15], r8, r14, r12, 0, r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0096, code lost:
        if (r6 == 0) goto L_0x00b1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0098, code lost:
        java.lang.System.arraycopy((java.lang.Object) r8, r14, (java.lang.Object) r7, r10, r6);
        r1 = r10 + r6;
        r5 = r14 + r6;
        r2 = r12 - r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00a2, code lost:
        if (r2 > 1) goto L_0x00ae;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00a4, code lost:
        r12 = r2;
        r14 = r5;
        r3 = r13;
        r10 = r17;
        r9 = 1;
        r13 = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x00ae, code lost:
        r10 = r1;
        r12 = r2;
        r14 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00b1, code lost:
        r5 = r10 + 1;
        r4 = r15 + 1;
        r7[r10] = r7[r15];
        r10 = r13 - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00bb, code lost:
        if (r10 != 0) goto L_0x00c5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00bd, code lost:
        r15 = r4;
        r13 = r5;
        r3 = r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00c5, code lost:
        r15 = r4;
        r9 = r5;
        r13 = r6;
        r1 = gallopLeft(r8[r14], r7, r4, r10, 0, r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00d4, code lost:
        if (r1 == 0) goto L_0x00e8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00d6, code lost:
        java.lang.System.arraycopy((java.lang.Object) r7, r15, (java.lang.Object) r7, r9, r1);
        r2 = r9 + r1;
        r6 = r15 + r1;
        r3 = r10 - r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00df, code lost:
        if (r3 != 0) goto L_0x00e5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00e1, code lost:
        r13 = r2;
        r15 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00e5, code lost:
        r9 = r2;
        r10 = r3;
        r15 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00e8, code lost:
        r6 = r9 + 1;
        r2 = r14 + 1;
        r7[r9] = r8[r14];
        r12 = r12 - 1;
        r9 = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00f3, code lost:
        if (r12 != 1) goto L_0x011f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00f5, code lost:
        r14 = r2;
        r13 = r6;
        r3 = r10;
        r10 = r17;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x011f, code lost:
        r17 = r17 - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x0122, code lost:
        if (r13 < 7) goto L_0x0126;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0124, code lost:
        r4 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x0126, code lost:
        r4 = $assertionsDisabled;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x0127, code lost:
        if (r1 < 7) goto L_0x012b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x0129, code lost:
        r3 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x012b, code lost:
        r3 = $assertionsDisabled;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x012d, code lost:
        if ((r3 | r4) != false) goto L_0x013f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x012f, code lost:
        if (r17 >= 0) goto L_0x0133;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0131, code lost:
        r17 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x013f, code lost:
        r18 = r1;
        r14 = r2;
        r16 = r13;
        r13 = r10;
        r9 = r21;
     */
    private void mergeLo(int base1, int len1, int base2, int len2) {
        int len22;
        int cursor2;
        int cursor1;
        int dest;
        int len12;
        int len23;
        int i;
        int minGallop2;
        int dest2;
        int len24;
        int len13 = len1;
        T[] a2 = this.a;
        T[] tmp2 = ensureCapacity(len13);
        int cursor12 = this.tmpBase;
        int cursor22 = base2;
        int dest3 = base1;
        System.arraycopy((Object) a2, base1, (Object) tmp2, cursor12, len13);
        int dest4 = dest3 + 1;
        int count1 = cursor22 + 1;
        a2[dest3] = a2[cursor22];
        int len25 = len2 - 1;
        if (len25 == 0) {
            System.arraycopy((Object) tmp2, cursor12, (Object) a2, dest4, len13);
            return;
        }
        int len26 = 1;
        if (len13 == 1) {
            System.arraycopy((Object) a2, count1, (Object) a2, dest4, len25);
            a2[dest4 + len25] = tmp2[cursor12];
            return;
        }
        Comparator<? super T> c2 = this.c;
        int minGallop3 = this.minGallop;
        loop0:
        while (true) {
            int count12 = 0;
            int dest5 = dest4;
            int cursor13 = cursor12;
            int len14 = len13;
            int count2 = 0;
            while (true) {
                if (c2.compare(a2[count1], tmp2[cursor13]) < 0) {
                    dest = dest5 + 1;
                    cursor2 = count1 + 1;
                    a2[dest5] = a2[count1];
                    count2++;
                    len22--;
                    if (len22 == 0) {
                        len12 = len14;
                        cursor1 = cursor13;
                        i = len26;
                        len23 = minGallop3;
                        break loop0;
                    }
                    count12 = 0;
                    dest5 = dest;
                    count1 = cursor2;
                } else {
                    dest = dest5 + 1;
                    int cursor14 = cursor13 + 1;
                    a2[dest5] = tmp2[cursor13];
                    count12++;
                    count2 = 0;
                    len14--;
                    if (len14 == len26) {
                        len12 = len14;
                        i = len26;
                        cursor1 = cursor14;
                        len23 = minGallop3;
                        cursor2 = count1;
                        break loop0;
                    }
                    dest5 = dest;
                    cursor13 = cursor14;
                }
                if ((count12 | count2) >= minGallop3) {
                    break;
                }
                int i2 = base1;
            }
            minGallop3 = minGallop2 + 2;
            dest4 = dest2;
            len25 = len24;
            len13 = len12;
            count1 = cursor2;
            len26 = 1;
            int i3 = base1;
        }
        len23 = minGallop2;
        i = 1;
        this.minGallop = len23 < i ? i : len23;
        if (len12 == i) {
            System.arraycopy((Object) a2, cursor2, (Object) a2, dest, len22);
            a2[dest + len22] = tmp2[cursor1];
        } else if (len12 != 0) {
            System.arraycopy((Object) tmp2, cursor1, (Object) a2, dest, len12);
        } else {
            throw new IllegalArgumentException("Comparison method violates its general contract!");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0068, code lost:
        r20 = r12;
        r12 = r4;
        r4 = r20;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x008e, code lost:
        r19 = r1;
        r1 = r3;
        r18 = r4;
        r12 = r5;
        r14 = r6;
        r17 = r7;
        r16 = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0099, code lost:
        r8 = r1 - gallopRight(r10[r14], r9, r22, r1, r1 - 1, r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x00a8, code lost:
        if (r8 == 0) goto L_0x00c3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x00aa, code lost:
        r3 = r17 - r8;
        r4 = r16 - r8;
        r1 = r1 - r8;
        java.lang.System.arraycopy((java.lang.Object) r9, r4 + 1, (java.lang.Object) r9, r3 + 1, r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00b6, code lost:
        if (r1 != 0) goto L_0x00c0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00b8, code lost:
        r16 = r4;
        r5 = r12;
        r12 = r18;
        r4 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x00c0, code lost:
        r16 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00c3, code lost:
        r3 = r17;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00c5, code lost:
        r15 = r3 - 1;
        r17 = r14 - 1;
        r9[r3] = r10[r14];
        r12 = r12 - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00d0, code lost:
        if (r12 != 1) goto L_0x00da;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00d2, code lost:
        r5 = r12;
        r4 = r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00d5, code lost:
        r14 = r17;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00da, code lost:
        r14 = r8;
        r3 = r12 - gallopLeft(r9[r16], r10, r11, r12, r12 - 1, r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00e9, code lost:
        if (r3 == 0) goto L_0x0102;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00eb, code lost:
        r4 = r15 - r3;
        r6 = r17 - r3;
        r5 = r12 - r3;
        java.lang.System.arraycopy((java.lang.Object) r10, r6 + 1, (java.lang.Object) r9, r4 + 1, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00f9, code lost:
        if (r5 > 1) goto L_0x00fe;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00fb, code lost:
        r14 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00fe, code lost:
        r15 = r4;
        r12 = r5;
        r17 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0102, code lost:
        r4 = r15 - 1;
        r5 = r16 - 1;
        r9[r15] = r9[r16];
        r1 = r1 - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x010c, code lost:
        if (r1 != 0) goto L_0x0141;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x010e, code lost:
        r16 = r5;
        r5 = r12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x0141, code lost:
        r18 = r18 - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x0145, code lost:
        if (r14 < 7) goto L_0x0149;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x0147, code lost:
        r8 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x0149, code lost:
        r8 = $assertionsDisabled;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x014a, code lost:
        if (r3 < 7) goto L_0x014e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x014c, code lost:
        r7 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x014e, code lost:
        r7 = $assertionsDisabled;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x0150, code lost:
        if ((r7 | r8) != false) goto L_0x0162;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x0152, code lost:
        if (r18 >= 0) goto L_0x0156;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x0154, code lost:
        r18 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0162, code lost:
        r19 = r3;
        r16 = r5;
        r15 = r14;
        r14 = r17;
        r17 = r4;
     */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x016d A[LOOP:1: B:9:0x0049->B:69:0x016d, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x008e A[EDGE_INSN: B:78:0x008e->B:22:0x008e ?: BREAK  , SYNTHETIC] */
    private void mergeHi(int base1, int len1, int base2, int len2) {
        int dest;
        int cursor1;
        int count1;
        int len22;
        int dest2;
        int minGallop2;
        int len23;
        int cursor2;
        int cursor12;
        int dest3;
        int i = base2;
        int minGallop3 = len2;
        T[] a2 = this.a;
        T[] tmp2 = ensureCapacity(minGallop3);
        int tmpBase2 = this.tmpBase;
        System.arraycopy((Object) a2, i, (Object) tmp2, tmpBase2, minGallop3);
        int cursor13 = (base1 + len1) - 1;
        int cursor14 = (tmpBase2 + minGallop3) - 1;
        int dest4 = (i + minGallop3) - 1;
        int dest5 = dest4 - 1;
        int cursor15 = cursor13 - 1;
        a2[dest4] = a2[cursor13];
        int count2 = len1 - 1;
        if (count2 == 0) {
            System.arraycopy((Object) tmp2, tmpBase2, (Object) a2, dest5 - (minGallop3 - 1), minGallop3);
        } else if (minGallop3 == 1) {
            int dest6 = dest5 - count2;
            System.arraycopy((Object) a2, (cursor15 - count2) + 1, (Object) a2, dest6 + 1, count2);
            a2[dest6] = tmp2[cursor14];
        } else {
            Comparator<? super T> c2 = this.c;
            int dest7 = this.minGallop;
            loop0:
            while (true) {
                int count12 = 0;
                int count13 = cursor14;
                int len24 = minGallop3;
                int len12 = count2;
                int count22 = 0;
                while (true) {
                    if (c2.compare(tmp2[count13], a2[cursor15]) < 0) {
                        dest3 = dest - 1;
                        int cursor16 = cursor15 - 1;
                        a2[dest] = a2[cursor15];
                        count12++;
                        count22 = 0;
                        len12--;
                        if (len12 == 0) {
                            count2 = len12;
                            cursor1 = cursor16;
                            count1 = count13;
                            break loop0;
                        }
                        dest = dest3;
                        cursor15 = cursor16;
                        if ((count12 | count22) < dest7) {
                            break;
                        }
                    } else {
                        dest3 = dest - 1;
                        count1 = count13 - 1;
                        a2[dest] = tmp2[count13];
                        count22++;
                        len24--;
                        if (len24 == 1) {
                            count2 = len12;
                            cursor1 = cursor15;
                            break loop0;
                        }
                        count12 = 0;
                        dest = dest3;
                        count13 = count1;
                        if ((count12 | count22) < dest7) {
                        }
                    }
                }
                dest5 = dest2;
                cursor15 = cursor12;
                cursor14 = cursor2;
                dest7 = minGallop2 + 2;
                minGallop3 = len23;
            }
            int minGallop4 = minGallop2;
            this.minGallop = minGallop4 < 1 ? 1 : minGallop4;
            if (len22 == 1) {
                int dest8 = dest2 - count2;
                System.arraycopy((Object) a2, (cursor1 - count2) + 1, (Object) a2, dest8 + 1, count2);
                a2[dest8] = tmp2[count1];
            } else if (len22 != 0) {
                System.arraycopy((Object) tmp2, tmpBase2, (Object) a2, dest2 - (len22 - 1), len22);
            } else {
                throw new IllegalArgumentException("Comparison method violates its general contract!");
            }
        }
    }

    private T[] ensureCapacity(int minCapacity) {
        int newSize;
        if (this.tmpLen < minCapacity) {
            int newSize2 = minCapacity;
            int newSize3 = newSize2 | (newSize2 >> 1);
            int newSize4 = newSize3 | (newSize3 >> 2);
            int newSize5 = newSize4 | (newSize4 >> 4);
            int newSize6 = newSize5 | (newSize5 >> 8);
            int newSize7 = (newSize6 | (newSize6 >> 16)) + 1;
            if (newSize7 < 0) {
                newSize = minCapacity;
            } else {
                newSize = Math.min(newSize7, this.a.length >>> 1);
            }
            this.tmp = (Object[]) Array.newInstance(this.a.getClass().getComponentType(), newSize);
            this.tmpLen = newSize;
            this.tmpBase = 0;
        }
        return this.tmp;
    }
}
