package ohos.global.icu.impl.coll;

public final class CollationRootElements {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    static final int IX_COMMON_SEC_AND_TER_CE = 3;
    static final int IX_COUNT = 5;
    static final int IX_FIRST_PRIMARY_INDEX = 2;
    static final int IX_FIRST_SECONDARY_INDEX = 1;
    public static final int IX_FIRST_TERTIARY_INDEX = 0;
    static final int IX_SEC_TER_BOUNDARIES = 4;
    public static final long PRIMARY_SENTINEL = 4294967040L;
    public static final int PRIMARY_STEP_MASK = 127;
    public static final int SEC_TER_DELTA_FLAG = 128;
    private long[] elements;

    private static boolean isEndOfPrimaryRange(long j) {
        return (128 & j) == 0 && (j & 127) != 0;
    }

    public CollationRootElements(long[] jArr) {
        this.elements = jArr;
    }

    public int getTertiaryBoundary() {
        return (((int) this.elements[4]) << 8) & 65280;
    }

    /* access modifiers changed from: package-private */
    public long getFirstTertiaryCE() {
        long[] jArr = this.elements;
        return jArr[(int) jArr[0]] & -129;
    }

    /* access modifiers changed from: package-private */
    public long getLastTertiaryCE() {
        long[] jArr = this.elements;
        return jArr[((int) jArr[1]) - 1] & -129;
    }

    public int getLastCommonSecondary() {
        return (((int) this.elements[4]) >> 16) & 65280;
    }

    public int getSecondaryBoundary() {
        return (((int) this.elements[4]) >> 8) & 65280;
    }

    /* access modifiers changed from: package-private */
    public long getFirstSecondaryCE() {
        long[] jArr = this.elements;
        return jArr[(int) jArr[1]] & -129;
    }

    /* access modifiers changed from: package-private */
    public long getLastSecondaryCE() {
        long[] jArr = this.elements;
        return jArr[((int) jArr[2]) - 1] & -129;
    }

    /* access modifiers changed from: package-private */
    public long getFirstPrimary() {
        long[] jArr = this.elements;
        return jArr[(int) jArr[2]];
    }

    /* access modifiers changed from: package-private */
    public long getFirstPrimaryCE() {
        return Collation.makeCE(getFirstPrimary());
    }

    /* access modifiers changed from: package-private */
    public long lastCEWithPrimaryBefore(long j) {
        long j2;
        if (j == 0) {
            return 0;
        }
        int findP = findP(j);
        long[] jArr = this.elements;
        long j3 = jArr[findP] & PRIMARY_SENTINEL;
        long j4 = 83887360;
        if (j == j3) {
            long j5 = jArr[findP - 1];
            if ((j5 & 128) == 0) {
                j3 = j5 & PRIMARY_SENTINEL;
            } else {
                int i = findP - 2;
                while (true) {
                    j2 = this.elements[i];
                    if ((j2 & 128) == 0) {
                        break;
                    }
                    i--;
                }
                j3 = j2 & PRIMARY_SENTINEL;
                j4 = j5;
            }
        } else {
            while (true) {
                findP++;
                long j6 = this.elements[findP];
                if ((j6 & 128) == 0) {
                    break;
                }
                j4 = j6;
            }
        }
        return (j3 << 32) | (-129 & j4);
    }

    /* access modifiers changed from: package-private */
    public long firstCEWithPrimaryAtLeast(long j) {
        if (j == 0) {
            return 0;
        }
        int findP = findP(j);
        if (j != (this.elements[findP] & PRIMARY_SENTINEL)) {
            do {
                findP++;
                j = this.elements[findP];
            } while ((128 & j) != 0);
        }
        return (j << 32) | 83887360;
    }

    /* access modifiers changed from: package-private */
    public long getPrimaryBefore(long j, boolean z) {
        int i;
        long j2;
        int findPrimary = findPrimary(j);
        long[] jArr = this.elements;
        long j3 = jArr[findPrimary];
        if (j == (j3 & PRIMARY_SENTINEL)) {
            i = ((int) j3) & 127;
            if (i == 0) {
                do {
                    findPrimary--;
                    j2 = this.elements[findPrimary];
                } while ((128 & j2) != 0);
                return j2 & PRIMARY_SENTINEL;
            }
        } else {
            i = ((int) jArr[findPrimary + 1]) & 127;
        }
        if ((65535 & j) == 0) {
            return Collation.decTwoBytePrimaryByOneStep(j, z, i);
        }
        return Collation.decThreeBytePrimaryByOneStep(j, z, i);
    }

    /* access modifiers changed from: package-private */
    public int getSecondaryBefore(long j, int i) {
        int i2;
        int i3;
        int i4;
        if (j == 0) {
            long[] jArr = this.elements;
            i2 = (int) jArr[1];
            i4 = 0;
            i3 = (int) (jArr[i2] >> 16);
        } else {
            i2 = findPrimary(j) + 1;
            i4 = 256;
            i3 = ((int) getFirstSecTerForPrimary(i2)) >>> 16;
        }
        while (true) {
            i4 = i3;
            if (i <= i4) {
                return i4;
            }
            i3 = (int) (this.elements[i2] >> 16);
            i2++;
        }
    }

    /* access modifiers changed from: package-private */
    public int getTertiaryBefore(long j, int i, int i2) {
        int i3;
        long j2;
        int i4 = 256;
        if (j == 0) {
            if (i == 0) {
                i3 = (int) this.elements[0];
                i4 = 0;
            } else {
                i3 = (int) this.elements[1];
            }
            j2 = this.elements[i3] & -129;
        } else {
            i3 = findPrimary(j) + 1;
            j2 = getFirstSecTerForPrimary(i3);
        }
        long j3 = (((long) i) << 16) | ((long) i2);
        while (j3 > j2) {
            if (((int) (j2 >> 16)) == i) {
                i4 = (int) j2;
            }
            j2 = this.elements[i3] & -129;
            i3++;
        }
        return 65535 & i4;
    }

    /* access modifiers changed from: package-private */
    public int findPrimary(long j) {
        return findP(j);
    }

    /* access modifiers changed from: package-private */
    public long getPrimaryAfter(long j, int i, boolean z) {
        int i2;
        int i3 = i + 1;
        long j2 = this.elements[i3];
        if ((j2 & 128) != 0 || (i2 = ((int) j2) & 127) == 0) {
            while ((j2 & 128) != 0) {
                i3++;
                j2 = this.elements[i3];
            }
            return j2;
        } else if ((65535 & j) == 0) {
            return Collation.incTwoBytePrimaryByOffset(j, z, i2);
        } else {
            return Collation.incThreeBytePrimaryByOffset(j, z, i2);
        }
    }

    /* access modifiers changed from: package-private */
    public int getSecondaryAfter(int i, int i2) {
        long j;
        int i3;
        if (i == 0) {
            long[] jArr = this.elements;
            int i4 = (int) jArr[1];
            j = jArr[i4];
            i3 = 65536;
            i = i4;
        } else {
            j = getFirstSecTerForPrimary(i + 1);
            i3 = getSecondaryBoundary();
        }
        do {
            int i5 = (int) (j >> 16);
            if (i5 > i2) {
                return i5;
            }
            i++;
            j = this.elements[i];
        } while ((128 & j) != 0);
        return i3;
    }

    /* access modifiers changed from: package-private */
    public int getTertiaryAfter(int i, int i2, int i3) {
        long j;
        int i4;
        int i5;
        if (i == 0) {
            if (i2 == 0) {
                i5 = (int) this.elements[0];
                i4 = 16384;
            } else {
                i5 = (int) this.elements[1];
                i4 = getTertiaryBoundary();
            }
            j = this.elements[i5] & -129;
        } else {
            j = getFirstSecTerForPrimary(i + 1);
            i4 = getTertiaryBoundary();
            i5 = i;
        }
        long j2 = (long) i2;
        long j3 = ((4294967295L & j2) << 16) | ((long) i3);
        while (j <= j3) {
            i5++;
            long j4 = this.elements[i5];
            if ((128 & j4) == 0 || (j4 >> 16) > j2) {
                return i4;
            }
            j = j4 & -129;
        }
        return ((int) j) & 65535;
    }

    private long getFirstSecTerForPrimary(int i) {
        long j = this.elements[i];
        if ((128 & j) == 0) {
            return 83887360;
        }
        long j2 = j & -129;
        if (j2 > 83887360) {
            return 83887360;
        }
        return j2;
    }

    private int findP(long j) {
        long[] jArr = this.elements;
        int i = (int) jArr[2];
        int length = jArr.length - 1;
        while (i + 1 < length) {
            int i2 = (int) ((((long) i) + ((long) length)) / 2);
            long j2 = this.elements[i2];
            if ((j2 & 128) != 0) {
                int i3 = i2 + 1;
                while (true) {
                    if (i3 == length) {
                        break;
                    }
                    j2 = this.elements[i3];
                    if ((j2 & 128) == 0) {
                        i2 = i3;
                        break;
                    }
                    i3++;
                }
                if ((j2 & 128) != 0) {
                    int i4 = i2 - 1;
                    while (true) {
                        if (i4 == i) {
                            break;
                        }
                        j2 = this.elements[i4];
                        if ((j2 & 128) == 0) {
                            i2 = i4;
                            break;
                        }
                        i4--;
                    }
                    if ((128 & j2) != 0) {
                        break;
                    }
                }
            }
            if (j < (j2 & PRIMARY_SENTINEL)) {
                length = i2;
            } else {
                i = i2;
            }
        }
        return i;
    }
}
