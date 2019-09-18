package java.util;

class ComparableTimSort {
    static final /* synthetic */ boolean $assertionsDisabled = false;
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

    private ComparableTimSort(Object[] a2, Object[] work, int workBase, int workLen) {
        int stackLen;
        this.a = a2;
        int len = a2.length;
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

    static void sort(Object[] a2, int lo, int hi, Object[] work, int workBase, int workLen) {
        int nRemaining = hi - lo;
        if (nRemaining >= 2) {
            if (nRemaining < 32) {
                binarySort(a2, lo, hi, lo + countRunAndMakeAscending(a2, lo, hi));
                return;
            }
            ComparableTimSort ts = new ComparableTimSort(a2, work, workBase, workLen);
            int minRun = minRunLength(nRemaining);
            do {
                int runLen2 = countRunAndMakeAscending(a2, lo, hi);
                if (runLen2 < minRun) {
                    int force = nRemaining <= minRun ? nRemaining : minRun;
                    binarySort(a2, lo, lo + force, lo + runLen2);
                    runLen2 = force;
                }
                ts.pushRun(lo, runLen2);
                ts.mergeCollapse();
                lo += runLen2;
                nRemaining -= runLen2;
            } while (nRemaining != 0);
            ts.mergeForceCollapse();
        }
    }

    private static void binarySort(Object[] a2, int lo, int hi, int start) {
        if (start == lo) {
            start++;
        }
        while (start < hi) {
            Comparable pivot = (Comparable) a2[start];
            int left = lo;
            int right = start;
            while (left < right) {
                int mid = (left + right) >>> 1;
                if (pivot.compareTo(a2[mid]) < 0) {
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

    private static int countRunAndMakeAscending(Object[] a2, int lo, int hi) {
        int runHi = lo + 1;
        if (runHi == hi) {
            return 1;
        }
        int runHi2 = runHi + 1;
        if (a2[runHi].compareTo(a2[lo]) < 0) {
            while (runHi2 < hi && a2[runHi2].compareTo(a2[runHi2 - 1]) < 0) {
                runHi2++;
            }
            reverseRange(a2, lo, runHi2);
        } else {
            while (runHi2 < hi && a2[runHi2].compareTo(a2[runHi2 - 1]) >= 0) {
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
        int base1 = this.runBase[i];
        int len1 = this.runLen[i];
        int base2 = this.runBase[i + 1];
        int len2 = this.runLen[i + 1];
        this.runLen[i] = len1 + len2;
        if (i == this.stackSize - 3) {
            this.runBase[i + 1] = this.runBase[i + 2];
            this.runLen[i + 1] = this.runLen[i + 2];
        }
        this.stackSize--;
        int k = gallopRight((Comparable) this.a[base2], this.a, base1, len1, 0);
        int base12 = base1 + k;
        int len12 = len1 - k;
        if (len12 != 0) {
            int len22 = gallopLeft((Comparable) this.a[(base12 + len12) - 1], this.a, base2, len2, len2 - 1);
            if (len22 != 0) {
                if (len12 <= len22) {
                    mergeLo(base12, len12, base2, len22);
                } else {
                    mergeHi(base12, len12, base2, len22);
                }
            }
        }
    }

    private static int gallopLeft(Comparable<Object> key, Object[] a2, int base, int len, int hint) {
        int ofs;
        int lastOfs;
        int lastOfs2 = 0;
        int ofs2 = 1;
        if (key.compareTo(a2[base + hint]) > 0) {
            int maxOfs = len - hint;
            while (ofs2 < maxOfs && key.compareTo(a2[base + hint + ofs2]) > 0) {
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
            while (ofs2 < maxOfs2 && key.compareTo(a2[(base + hint) - ofs2]) <= 0) {
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
            if (key.compareTo(a2[base + m]) > 0) {
                lastOfs3 = m + 1;
            } else {
                ofs = m;
            }
        }
        return ofs;
    }

    private static int gallopRight(Comparable<Object> key, Object[] a2, int base, int len, int hint) {
        int lastOfs;
        int ofs;
        int ofs2 = 1;
        int lastOfs2 = 0;
        if (key.compareTo(a2[base + hint]) < 0) {
            int maxOfs = hint + 1;
            while (ofs2 < maxOfs && key.compareTo(a2[(base + hint) - ofs2]) < 0) {
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
            while (ofs2 < maxOfs2 && key.compareTo(a2[base + hint + ofs2]) >= 0) {
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
            if (key.compareTo(a2[base + m]) < 0) {
                ofs = m;
            } else {
                lastOfs3 = m + 1;
            }
        }
        return ofs;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0059, code lost:
        r9 = r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0078, code lost:
        r13 = gallopRight((java.lang.Comparable) r2[r9], r3, r8, r4, 0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0081, code lost:
        if (r13 == 0) goto L_0x008e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0083, code lost:
        java.lang.System.arraycopy((java.lang.Object) r3, r8, (java.lang.Object) r2, r11, r13);
        r14 = r11 + r13;
        r8 = r8 + r13;
        r4 = r4 - r13;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x008a, code lost:
        if (r4 > 1) goto L_0x008d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x008d, code lost:
        r11 = r14;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x008e, code lost:
        r14 = r11 + 1;
        r15 = r9 + 1;
        r2[r11] = r2[r9];
        r5 = r5 - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0098, code lost:
        if (r5 != 0) goto L_0x009b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x009b, code lost:
        r1 = gallopLeft((java.lang.Comparable) r3[r8], r2, r15, r5, 0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00a3, code lost:
        if (r1 == 0) goto L_0x00af;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00a5, code lost:
        java.lang.System.arraycopy((java.lang.Object) r2, r15, (java.lang.Object) r2, r14, r1);
        r14 = r14 + r1;
        r9 = r15 + r1;
        r5 = r5 - r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00ac, code lost:
        if (r5 != 0) goto L_0x00b0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00af, code lost:
        r9 = r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00b0, code lost:
        r11 = r14 + 1;
        r15 = r8 + 1;
        r2[r14] = r3[r8];
        r4 = r4 - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00ba, code lost:
        if (r4 != 1) goto L_0x00e3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00bc, code lost:
        r14 = r11;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00e3, code lost:
        r10 = r10 - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00e6, code lost:
        if (r13 < 7) goto L_0x00ea;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00e8, code lost:
        r14 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00ea, code lost:
        r14 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00eb, code lost:
        if (r1 < 7) goto L_0x00ef;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00ed, code lost:
        r8 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00ef, code lost:
        r8 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00f1, code lost:
        if ((r8 | r14) != false) goto L_0x00fe;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00f3, code lost:
        if (r10 >= 0) goto L_0x00f6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00f5, code lost:
        r10 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00fe, code lost:
        r8 = r15;
     */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x0078 A[EDGE_INSN: B:69:0x0078->B:22:0x0078 ?: BREAK  , SYNTHETIC] */
    private void mergeLo(int base1, int len1, int base2, int len2) {
        int cursor1;
        int len12;
        int dest;
        int cursor12;
        int len13 = len1;
        Object[] a2 = this.a;
        Object[] tmp2 = ensureCapacity(len13);
        int len14 = this.tmpBase;
        int cursor2 = base2;
        int dest2 = base1;
        System.arraycopy((Object) a2, base1, (Object) tmp2, len14, len13);
        int dest3 = dest2 + 1;
        int count1 = cursor2 + 1;
        a2[dest2] = a2[cursor2];
        int len22 = len2 - 1;
        if (len22 == 0) {
            System.arraycopy((Object) tmp2, len14, (Object) a2, dest3, len13);
        } else if (len13 == 1) {
            System.arraycopy((Object) a2, count1, (Object) a2, dest3, len22);
            a2[dest3 + len22] = tmp2[len14];
        } else {
            int minGallop2 = this.minGallop;
            loop0:
            while (true) {
                int count12 = 0;
                int dest4 = dest3;
                cursor1 = len14;
                len12 = len13;
                int count2 = 0;
                while (true) {
                    if (((Comparable) a2[count1]).compareTo(tmp2[cursor1]) < 0) {
                        dest = dest4 + 1;
                        int cursor22 = count1 + 1;
                        a2[dest4] = a2[count1];
                        count2++;
                        len22--;
                        if (len22 != 0) {
                            count12 = 0;
                            dest4 = dest;
                            count1 = cursor22;
                            if ((count12 | count2) >= minGallop2) {
                                break;
                            }
                        } else {
                            break loop0;
                        }
                    } else {
                        dest = dest4 + 1;
                        cursor12 = cursor1 + 1;
                        a2[dest4] = tmp2[cursor1];
                        count12++;
                        count2 = 0;
                        len12--;
                        if (len12 == 1) {
                            break loop0;
                        }
                        dest4 = dest;
                        cursor1 = cursor12;
                        if ((count12 | count2) >= minGallop2) {
                        }
                    }
                }
                minGallop2 += 2;
                len13 = len12;
                dest3 = dest4;
                len14 = cursor12;
            }
            cursor1 = cursor12;
            this.minGallop = minGallop2 < 1 ? 1 : minGallop2;
            if (len12 == 1) {
                System.arraycopy((Object) a2, count1, (Object) a2, dest, len22);
                a2[dest + len22] = tmp2[cursor1];
            } else if (len12 != 0) {
                System.arraycopy((Object) tmp2, cursor1, (Object) a2, dest, len12);
            } else {
                throw new IllegalArgumentException("Comparison method violates its general contract!");
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x008b, code lost:
        r15 = r4 - gallopRight((java.lang.Comparable) r6[r11], r5, r1, r4, r4 - 1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0098, code lost:
        if (r15 == 0) goto L_0x00aa;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x009a, code lost:
        r9 = r12 - r15;
        r13 = r13 - r15;
        r4 = r4 - r15;
        java.lang.System.arraycopy((java.lang.Object) r5, r13 + 1, (java.lang.Object) r5, r9 + 1, r15);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x00a5, code lost:
        if (r4 != 0) goto L_0x00ab;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00a8, code lost:
        r12 = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00aa, code lost:
        r9 = r12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x00ab, code lost:
        r12 = r9 - 1;
        r14 = r11 - 1;
        r5[r9] = r6[r11];
        r10 = r10 - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00b6, code lost:
        if (r10 != 1) goto L_0x00bc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00b8, code lost:
        r9 = r8;
        r11 = r14;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00bc, code lost:
        r2 = r10 - gallopLeft((java.lang.Comparable) r5[r13], r6, r7, r10, r10 - 1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00c8, code lost:
        if (r2 == 0) goto L_0x00da;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00ca, code lost:
        r9 = r12 - r2;
        r11 = r14 - r2;
        r10 = r10 - r2;
        java.lang.System.arraycopy((java.lang.Object) r6, r11 + 1, (java.lang.Object) r5, r9 + 1, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00d7, code lost:
        if (r10 > 1) goto L_0x00dc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00da, code lost:
        r9 = r12;
        r11 = r14;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00dc, code lost:
        r12 = r9 - 1;
        r14 = r13 - 1;
        r5[r9] = r5[r13];
        r4 = r4 - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00e6, code lost:
        if (r4 != 0) goto L_0x0118;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00e8, code lost:
        r9 = r8;
        r13 = r14;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x0118, code lost:
        r8 = r8 - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x011c, code lost:
        if (r15 < 7) goto L_0x0121;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x011e, code lost:
        r16 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x0121, code lost:
        r16 = $assertionsDisabled;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0123, code lost:
        if (r2 < 7) goto L_0x0127;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x0125, code lost:
        r13 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x0127, code lost:
        r13 = $assertionsDisabled;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x012a, code lost:
        if ((r16 | r13) != false) goto L_0x0138;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x012c, code lost:
        if (r8 >= 0) goto L_0x012f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x012e, code lost:
        r8 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0138, code lost:
        r13 = r14;
     */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x013b A[LOOP:1: B:9:0x0049->B:64:0x013b, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x008b A[EDGE_INSN: B:73:0x008b->B:22:0x008b ?: BREAK  , SYNTHETIC] */
    private void mergeHi(int base1, int len1, int base2, int len2) {
        int cursor2;
        int len22;
        int len12;
        int cursor1;
        int dest;
        int i = base1;
        int i2 = base2;
        int len13 = len2;
        Object[] a2 = this.a;
        Object[] tmp2 = ensureCapacity(len13);
        int tmpBase2 = this.tmpBase;
        System.arraycopy((Object) a2, i2, (Object) tmp2, tmpBase2, len13);
        int cursor12 = (i + len1) - 1;
        int len23 = (tmpBase2 + len13) - 1;
        int dest2 = (i2 + len13) - 1;
        int dest3 = dest2 - 1;
        int cursor13 = cursor12 - 1;
        a2[dest2] = a2[cursor12];
        int len14 = len1 - 1;
        if (len14 == 0) {
            System.arraycopy((Object) tmp2, tmpBase2, (Object) a2, dest3 - (len13 - 1), len13);
        } else if (len13 == 1) {
            int dest4 = dest3 - len14;
            System.arraycopy((Object) a2, (cursor13 - len14) + 1, (Object) a2, dest4 + 1, len14);
            a2[dest4] = tmp2[len23];
        } else {
            int minGallop2 = this.minGallop;
            loop0:
            while (true) {
                int count1 = 0;
                cursor2 = len23;
                len22 = len13;
                len12 = len14;
                int count2 = 0;
                while (true) {
                    if (((Comparable) tmp2[cursor2]).compareTo(a2[cursor13]) < 0) {
                        int dest5 = dest3 - 1;
                        int cursor14 = cursor13 - 1;
                        a2[dest3] = a2[cursor13];
                        count1++;
                        count2 = 0;
                        len12--;
                        if (len12 == 0) {
                            dest = dest5;
                            cursor13 = cursor14;
                            break loop0;
                        }
                        dest3 = dest5;
                        cursor13 = cursor14;
                        if ((count1 | count2) < minGallop2) {
                            break;
                        }
                    } else {
                        int dest6 = dest3 - 1;
                        int cursor22 = cursor2 - 1;
                        a2[dest3] = tmp2[cursor2];
                        count2++;
                        len22--;
                        if (len22 == 1) {
                            dest = dest6;
                            cursor2 = cursor22;
                            break loop0;
                        }
                        dest3 = dest6;
                        count1 = 0;
                        cursor2 = cursor22;
                        if ((count1 | count2) < minGallop2) {
                        }
                    }
                }
                minGallop2 += 2;
                len14 = len12;
                len13 = len22;
                len23 = cursor2;
                cursor13 = cursor1;
            }
            int minGallop3 = minGallop2;
            this.minGallop = minGallop3 < 1 ? 1 : minGallop3;
            if (len22 == 1) {
                int dest7 = dest3 - len12;
                System.arraycopy((Object) a2, (cursor13 - len12) + 1, (Object) a2, dest7 + 1, len12);
                a2[dest7] = tmp2[cursor2];
            } else if (len22 != 0) {
                System.arraycopy((Object) tmp2, tmpBase2, (Object) a2, dest3 - (len22 - 1), len22);
            } else {
                throw new IllegalArgumentException("Comparison method violates its general contract!");
            }
        }
    }

    private Object[] ensureCapacity(int minCapacity) {
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
            this.tmp = new Object[newSize];
            this.tmpLen = newSize;
            this.tmpBase = 0;
        }
        return this.tmp;
    }
}
