package android.icu.impl.coll;

import android.icu.impl.Normalizer2Impl;
import android.icu.text.DateTimePatternGenerator;

public final class CollationRootElements {
    static final /* synthetic */ boolean -assertionsDisabled = (CollationRootElements.class.desiredAssertionStatus() ^ 1);
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

    public CollationRootElements(long[] rootElements) {
        this.elements = rootElements;
    }

    public int getTertiaryBoundary() {
        return (((int) this.elements[4]) << 8) & Normalizer2Impl.JAMO_VT;
    }

    long getFirstTertiaryCE() {
        return this.elements[(int) this.elements[0]] & -129;
    }

    long getLastTertiaryCE() {
        return this.elements[((int) this.elements[1]) - 1] & -129;
    }

    public int getLastCommonSecondary() {
        return (((int) this.elements[4]) >> 16) & Normalizer2Impl.JAMO_VT;
    }

    public int getSecondaryBoundary() {
        return (((int) this.elements[4]) >> 8) & Normalizer2Impl.JAMO_VT;
    }

    long getFirstSecondaryCE() {
        return this.elements[(int) this.elements[1]] & -129;
    }

    long getLastSecondaryCE() {
        return this.elements[((int) this.elements[2]) - 1] & -129;
    }

    long getFirstPrimary() {
        return this.elements[(int) this.elements[2]];
    }

    long getFirstPrimaryCE() {
        return Collation.makeCE(getFirstPrimary());
    }

    long lastCEWithPrimaryBefore(long p) {
        if (p == 0) {
            return 0;
        }
        if (-assertionsDisabled || p > this.elements[(int) this.elements[2]]) {
            long secTer;
            int index = findP(p);
            long q = this.elements[index];
            if (p != (PRIMARY_SENTINEL & q)) {
                p = q & PRIMARY_SENTINEL;
                secTer = 83887360;
                while (true) {
                    index++;
                    q = this.elements[index];
                    if ((128 & q) == 0) {
                        break;
                    }
                    secTer = q;
                }
                if (!(-assertionsDisabled || (127 & q) == 0)) {
                    throw new AssertionError();
                }
            } else if (-assertionsDisabled || (127 & q) == 0) {
                secTer = this.elements[index - 1];
                if ((128 & secTer) == 0) {
                    p = secTer & PRIMARY_SENTINEL;
                    secTer = 83887360;
                } else {
                    index -= 2;
                    while (true) {
                        p = this.elements[index];
                        if ((128 & p) == 0) {
                            break;
                        }
                        index--;
                    }
                    p &= PRIMARY_SENTINEL;
                }
            } else {
                throw new AssertionError();
            }
            return (p << 32) | (-129 & secTer);
        }
        throw new AssertionError();
    }

    long firstCEWithPrimaryAtLeast(long p) {
        if (p == 0) {
            return 0;
        }
        int index = findP(p);
        if (p != (this.elements[index] & PRIMARY_SENTINEL)) {
            do {
                index++;
                p = this.elements[index];
            } while ((128 & p) != 0);
            if (!(-assertionsDisabled || (127 & p) == 0)) {
                throw new AssertionError();
            }
        }
        return (p << 32) | 83887360;
    }

    long getPrimaryBefore(long p, boolean isCompressible) {
        int step;
        int index = findPrimary(p);
        long q = this.elements[index];
        if (p == (q & PRIMARY_SENTINEL)) {
            step = ((int) q) & 127;
            if (step == 0) {
                do {
                    index--;
                    p = this.elements[index];
                } while ((128 & p) != 0);
                return p & PRIMARY_SENTINEL;
            }
        }
        long nextElement = this.elements[index + 1];
        if (-assertionsDisabled || isEndOfPrimaryRange(nextElement)) {
            step = ((int) nextElement) & 127;
        } else {
            throw new AssertionError();
        }
        if ((65535 & p) == 0) {
            return Collation.decTwoBytePrimaryByOneStep(p, isCompressible, step);
        }
        return Collation.decThreeBytePrimaryByOneStep(p, isCompressible, step);
    }

    int getSecondaryBefore(long p, int s) {
        int index;
        int previousSec;
        int sec;
        if (p == 0) {
            index = (int) this.elements[1];
            previousSec = 0;
            sec = (int) (this.elements[index] >> 16);
        } else {
            index = findPrimary(p) + 1;
            previousSec = 256;
            sec = ((int) getFirstSecTerForPrimary(index)) >>> 16;
        }
        if (-assertionsDisabled || s >= sec) {
            while (true) {
                int index2 = index;
                if (s > sec) {
                    previousSec = sec;
                    if (-assertionsDisabled || (this.elements[index2] & 128) != 0) {
                        index = index2 + 1;
                        sec = (int) (this.elements[index2] >> 16);
                    } else {
                        throw new AssertionError();
                    }
                } else if (-assertionsDisabled || sec == s) {
                    return previousSec;
                } else {
                    throw new AssertionError();
                }
            }
        }
        throw new AssertionError();
    }

    int getTertiaryBefore(long p, int s, int t) {
        if (-assertionsDisabled || (t & -16192) == 0) {
            int index;
            int previousTer;
            long secTer;
            if (p == 0) {
                if (s == 0) {
                    index = (int) this.elements[0];
                    previousTer = 0;
                } else {
                    index = (int) this.elements[1];
                    previousTer = 256;
                }
                secTer = this.elements[index] & -129;
            } else {
                index = findPrimary(p) + 1;
                previousTer = 256;
                secTer = getFirstSecTerForPrimary(index);
            }
            long st = (((long) s) << 16) | ((long) t);
            while (true) {
                int index2 = index;
                if (st > secTer) {
                    if (((int) (secTer >> 16)) == s) {
                        previousTer = (int) secTer;
                    }
                    if (-assertionsDisabled || (this.elements[index2] & 128) != 0) {
                        index = index2 + 1;
                        secTer = this.elements[index2] & -129;
                    } else {
                        throw new AssertionError();
                    }
                } else if (-assertionsDisabled || secTer == st) {
                    return DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH & previousTer;
                } else {
                    throw new AssertionError();
                }
            }
        }
        throw new AssertionError();
    }

    int findPrimary(long p) {
        Object obj = 1;
        if (-assertionsDisabled || (255 & p) == 0) {
            int index = findP(p);
            if (!-assertionsDisabled) {
                if (!(isEndOfPrimaryRange(this.elements[index + 1]) || p == (this.elements[index] & PRIMARY_SENTINEL))) {
                    obj = null;
                }
                if (obj == null) {
                    throw new AssertionError();
                }
            }
            return index;
        }
        throw new AssertionError();
    }

    long getPrimaryAfter(long p, int index, boolean isCompressible) {
        if (!-assertionsDisabled) {
            if (!(p != (this.elements[index] & PRIMARY_SENTINEL) ? isEndOfPrimaryRange(this.elements[index + 1]) : true)) {
                throw new AssertionError();
            }
        }
        index++;
        long q = this.elements[index];
        if ((128 & q) == 0) {
            int step = ((int) q) & 127;
            if (step != 0) {
                if ((65535 & p) == 0) {
                    return Collation.incTwoBytePrimaryByOffset(p, isCompressible, step);
                }
                return Collation.incThreeBytePrimaryByOffset(p, isCompressible, step);
            }
        }
        while ((128 & q) != 0) {
            index++;
            q = this.elements[index];
        }
        if (-assertionsDisabled || (127 & q) == 0) {
            return q;
        }
        throw new AssertionError();
    }

    int getSecondaryAfter(int index, int s) {
        long secTer;
        int secLimit;
        if (index == 0) {
            if (-assertionsDisabled || s != 0) {
                index = (int) this.elements[1];
                secTer = this.elements[index];
                secLimit = 65536;
            } else {
                throw new AssertionError();
            }
        } else if (-assertionsDisabled || index >= ((int) this.elements[2])) {
            secTer = getFirstSecTerForPrimary(index + 1);
            secLimit = getSecondaryBoundary();
        } else {
            throw new AssertionError();
        }
        do {
            int sec = (int) (secTer >> 16);
            if (sec > s) {
                return sec;
            }
            index++;
            secTer = this.elements[index];
        } while ((128 & secTer) != 0);
        return secLimit;
    }

    int getTertiaryAfter(int index, int s, int t) {
        int terLimit;
        long secTer;
        if (index == 0) {
            if (s != 0) {
                index = (int) this.elements[1];
                terLimit = getTertiaryBoundary();
            } else if (-assertionsDisabled || t != 0) {
                index = (int) this.elements[0];
                terLimit = 16384;
            } else {
                throw new AssertionError();
            }
            secTer = this.elements[index] & -129;
        } else if (-assertionsDisabled || index >= ((int) this.elements[2])) {
            secTer = getFirstSecTerForPrimary(index + 1);
            terLimit = getTertiaryBoundary();
        } else {
            throw new AssertionError();
        }
        long st = ((((long) s) & 4294967295L) << 16) | ((long) t);
        while (secTer <= st) {
            index++;
            secTer = this.elements[index];
            if ((128 & secTer) == 0 || (secTer >> 16) > ((long) s)) {
                return terLimit;
            }
            secTer &= -129;
        }
        if (-assertionsDisabled || (secTer >> 16) == ((long) s)) {
            return ((int) secTer) & DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH;
        }
        throw new AssertionError();
    }

    private long getFirstSecTerForPrimary(int index) {
        long secTer = this.elements[index];
        if ((128 & secTer) == 0) {
            return 83887360;
        }
        secTer &= -129;
        if (secTer > 83887360) {
            return 83887360;
        }
        return secTer;
    }

    private int findP(long p) {
        if (-assertionsDisabled || (p >> 24) != 254) {
            int start = (int) this.elements[2];
            if (-assertionsDisabled || p >= this.elements[start]) {
                int limit = this.elements.length - 1;
                if (!-assertionsDisabled && this.elements[limit] < PRIMARY_SENTINEL) {
                    throw new AssertionError();
                } else if (-assertionsDisabled || p < this.elements[limit]) {
                    while (start + 1 < limit) {
                        int i = (int) ((((long) start) + ((long) limit)) / 2);
                        long q = this.elements[i];
                        if ((128 & q) != 0) {
                            int j;
                            for (j = i + 1; j != limit; j++) {
                                q = this.elements[j];
                                if ((128 & q) == 0) {
                                    i = j;
                                    break;
                                }
                            }
                            if ((128 & q) != 0) {
                                for (j = i - 1; j != start; j--) {
                                    q = this.elements[j];
                                    if ((128 & q) == 0) {
                                        i = j;
                                        break;
                                    }
                                }
                                if ((128 & q) != 0) {
                                    break;
                                }
                            }
                        }
                        if (p < (PRIMARY_SENTINEL & q)) {
                            limit = i;
                        } else {
                            start = i;
                        }
                    }
                    return start;
                } else {
                    throw new AssertionError();
                }
            }
            throw new AssertionError();
        }
        throw new AssertionError();
    }

    private static boolean isEndOfPrimaryRange(long q) {
        return (128 & q) == 0 && (127 & q) != 0;
    }
}
