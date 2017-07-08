package android.icu.impl.coll;

import android.icu.impl.Normalizer2Impl;
import com.android.dex.DexFormat;
import libcore.icu.DateUtilsBridge;
import org.w3c.dom.traversal.NodeFilter;

public final class CollationRootElements {
    static final /* synthetic */ boolean -assertionsDisabled = false;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.coll.CollationRootElements.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.coll.CollationRootElements.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationRootElements.<clinit>():void");
    }

    public CollationRootElements(long[] rootElements) {
        this.elements = rootElements;
    }

    public int getTertiaryBoundary() {
        return (((int) this.elements[IX_SEC_TER_BOUNDARIES]) << 8) & Normalizer2Impl.JAMO_VT;
    }

    long getFirstTertiaryCE() {
        return this.elements[(int) this.elements[IX_FIRST_TERTIARY_INDEX]] & -129;
    }

    long getLastTertiaryCE() {
        return this.elements[((int) this.elements[IX_FIRST_SECONDARY_INDEX]) - 1] & -129;
    }

    public int getLastCommonSecondary() {
        return (((int) this.elements[IX_SEC_TER_BOUNDARIES]) >> 16) & Normalizer2Impl.JAMO_VT;
    }

    public int getSecondaryBoundary() {
        return (((int) this.elements[IX_SEC_TER_BOUNDARIES]) >> 8) & Normalizer2Impl.JAMO_VT;
    }

    long getFirstSecondaryCE() {
        return this.elements[(int) this.elements[IX_FIRST_SECONDARY_INDEX]] & -129;
    }

    long getLastSecondaryCE() {
        return this.elements[((int) this.elements[IX_FIRST_PRIMARY_INDEX]) - 1] & -129;
    }

    long getFirstPrimary() {
        return this.elements[(int) this.elements[IX_FIRST_PRIMARY_INDEX]];
    }

    long getFirstPrimaryCE() {
        return Collation.makeCE(getFirstPrimary());
    }

    long lastCEWithPrimaryBefore(long p) {
        if (p == 0) {
            return 0;
        }
        long secTer;
        if (!-assertionsDisabled) {
            if ((p > this.elements[(int) this.elements[IX_FIRST_PRIMARY_INDEX]] ? IX_FIRST_SECONDARY_INDEX : null) == null) {
                throw new AssertionError();
            }
        }
        int index = findP(p);
        long q = this.elements[index];
        if (p == (PRIMARY_SENTINEL & q)) {
            if (!-assertionsDisabled) {
                if (((127 & q) == 0 ? IX_FIRST_SECONDARY_INDEX : null) == null) {
                    throw new AssertionError();
                }
            }
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
            p = q & PRIMARY_SENTINEL;
            secTer = 83887360;
            while (true) {
                index += IX_FIRST_SECONDARY_INDEX;
                q = this.elements[index];
                if ((128 & q) == 0) {
                    break;
                }
                secTer = q;
            }
            if (!-assertionsDisabled) {
                if (((127 & q) == 0 ? IX_FIRST_SECONDARY_INDEX : null) == null) {
                    throw new AssertionError();
                }
            }
        }
        return (p << 32) | (-129 & secTer);
    }

    long firstCEWithPrimaryAtLeast(long p) {
        if (p == 0) {
            return 0;
        }
        int index = findP(p);
        if (p != (this.elements[index] & PRIMARY_SENTINEL)) {
            do {
                index += IX_FIRST_SECONDARY_INDEX;
                p = this.elements[index];
            } while ((128 & p) != 0);
            if (!-assertionsDisabled) {
                if (((127 & p) == 0 ? IX_FIRST_SECONDARY_INDEX : null) == null) {
                    throw new AssertionError();
                }
            }
        }
        return (p << 32) | 83887360;
    }

    long getPrimaryBefore(long p, boolean isCompressible) {
        int step;
        int index = findPrimary(p);
        long q = this.elements[index];
        if (p == (q & PRIMARY_SENTINEL)) {
            step = ((int) q) & PRIMARY_STEP_MASK;
            if (step == 0) {
                do {
                    index--;
                    p = this.elements[index];
                } while ((128 & p) != 0);
                return p & PRIMARY_SENTINEL;
            }
        }
        long nextElement = this.elements[index + IX_FIRST_SECONDARY_INDEX];
        if (-assertionsDisabled || isEndOfPrimaryRange(nextElement)) {
            step = ((int) nextElement) & PRIMARY_STEP_MASK;
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
            index = (int) this.elements[IX_FIRST_SECONDARY_INDEX];
            previousSec = IX_FIRST_TERTIARY_INDEX;
            sec = (int) (this.elements[index] >> 16);
        } else {
            index = findPrimary(p) + IX_FIRST_SECONDARY_INDEX;
            previousSec = NodeFilter.SHOW_DOCUMENT;
            sec = ((int) getFirstSecTerForPrimary(index)) >>> 16;
        }
        if (!-assertionsDisabled) {
            if ((s >= sec ? IX_FIRST_SECONDARY_INDEX : null) == null) {
                throw new AssertionError();
            }
        }
        int index2 = index;
        while (s > sec) {
            previousSec = sec;
            if (!-assertionsDisabled) {
                if (((this.elements[index2] & 128) != 0 ? IX_FIRST_SECONDARY_INDEX : null) == null) {
                    throw new AssertionError();
                }
            }
            sec = (int) (this.elements[index2] >> 16);
            index2 += IX_FIRST_SECONDARY_INDEX;
        }
        if (!-assertionsDisabled) {
            if ((sec == s ? IX_FIRST_SECONDARY_INDEX : null) == null) {
                throw new AssertionError();
            }
        }
        return previousSec;
    }

    int getTertiaryBefore(long p, int s, int t) {
        int index;
        int previousTer;
        long secTer;
        if (!-assertionsDisabled) {
            if (((t & -16192) == 0 ? IX_FIRST_SECONDARY_INDEX : null) == null) {
                throw new AssertionError();
            }
        }
        if (p == 0) {
            if (s == 0) {
                index = (int) this.elements[IX_FIRST_TERTIARY_INDEX];
                previousTer = IX_FIRST_TERTIARY_INDEX;
            } else {
                index = (int) this.elements[IX_FIRST_SECONDARY_INDEX];
                previousTer = NodeFilter.SHOW_DOCUMENT;
            }
            secTer = this.elements[index] & -129;
        } else {
            index = findPrimary(p) + IX_FIRST_SECONDARY_INDEX;
            previousTer = NodeFilter.SHOW_DOCUMENT;
            secTer = getFirstSecTerForPrimary(index);
        }
        long st = (((long) s) << 16) | ((long) t);
        int index2 = index;
        while (st > secTer) {
            if (((int) (secTer >> 16)) == s) {
                previousTer = (int) secTer;
            }
            if (!-assertionsDisabled) {
                Object obj;
                if ((this.elements[index2] & 128) != 0) {
                    obj = IX_FIRST_SECONDARY_INDEX;
                } else {
                    obj = null;
                }
                if (obj == null) {
                    throw new AssertionError();
                }
            }
            secTer = this.elements[index2] & -129;
            index2 += IX_FIRST_SECONDARY_INDEX;
        }
        if (!-assertionsDisabled) {
            if ((secTer == st ? IX_FIRST_SECONDARY_INDEX : null) == null) {
                throw new AssertionError();
            }
        }
        return DexFormat.MAX_TYPE_IDX & previousTer;
    }

    int findPrimary(long p) {
        Object obj = IX_FIRST_SECONDARY_INDEX;
        if (!-assertionsDisabled) {
            if (((255 & p) == 0 ? IX_FIRST_SECONDARY_INDEX : IX_FIRST_TERTIARY_INDEX) == null) {
                throw new AssertionError();
            }
        }
        int index = findP(p);
        if (!-assertionsDisabled) {
            if (!(isEndOfPrimaryRange(this.elements[index + IX_FIRST_SECONDARY_INDEX]) || p == (this.elements[index] & PRIMARY_SENTINEL))) {
                obj = IX_FIRST_TERTIARY_INDEX;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        return index;
    }

    long getPrimaryAfter(long p, int index, boolean isCompressible) {
        if (!-assertionsDisabled) {
            if (!(p != (this.elements[index] & PRIMARY_SENTINEL) ? isEndOfPrimaryRange(this.elements[index + IX_FIRST_SECONDARY_INDEX]) : true)) {
                throw new AssertionError();
            }
        }
        index += IX_FIRST_SECONDARY_INDEX;
        long q = this.elements[index];
        if ((128 & q) == 0) {
            int step = ((int) q) & PRIMARY_STEP_MASK;
            if (step != 0) {
                if ((65535 & p) == 0) {
                    return Collation.incTwoBytePrimaryByOffset(p, isCompressible, step);
                }
                return Collation.incThreeBytePrimaryByOffset(p, isCompressible, step);
            }
        }
        while ((128 & q) != 0) {
            index += IX_FIRST_SECONDARY_INDEX;
            q = this.elements[index];
        }
        if (!-assertionsDisabled) {
            if (((127 & q) == 0 ? IX_FIRST_SECONDARY_INDEX : null) == null) {
                throw new AssertionError();
            }
        }
        return q;
    }

    int getSecondaryAfter(int index, int s) {
        long secTer;
        int secLimit;
        int i = IX_FIRST_SECONDARY_INDEX;
        int i2 = IX_FIRST_TERTIARY_INDEX;
        if (index == 0) {
            if (!-assertionsDisabled) {
                if (s != 0) {
                    i2 = IX_FIRST_SECONDARY_INDEX;
                }
                if (i2 == 0) {
                    throw new AssertionError();
                }
            }
            index = (int) this.elements[IX_FIRST_SECONDARY_INDEX];
            secTer = this.elements[index];
            secLimit = DateUtilsBridge.FORMAT_ABBREV_MONTH;
        } else {
            if (!-assertionsDisabled) {
                if (index < ((int) this.elements[IX_FIRST_PRIMARY_INDEX])) {
                    i = IX_FIRST_TERTIARY_INDEX;
                }
                if (i == 0) {
                    throw new AssertionError();
                }
            }
            secTer = getFirstSecTerForPrimary(index + IX_FIRST_SECONDARY_INDEX);
            secLimit = getSecondaryBoundary();
        }
        do {
            int sec = (int) (secTer >> 16);
            if (sec > s) {
                return sec;
            }
            index += IX_FIRST_SECONDARY_INDEX;
            secTer = this.elements[index];
        } while ((128 & secTer) != 0);
        return secLimit;
    }

    int getTertiaryAfter(int index, int s, int t) {
        int terLimit;
        long secTer;
        if (index == 0) {
            if (s == 0) {
                if (!-assertionsDisabled) {
                    if ((t != 0 ? IX_FIRST_SECONDARY_INDEX : null) == null) {
                        throw new AssertionError();
                    }
                }
                index = (int) this.elements[IX_FIRST_TERTIARY_INDEX];
                terLimit = DateUtilsBridge.FORMAT_ABBREV_TIME;
            } else {
                index = (int) this.elements[IX_FIRST_SECONDARY_INDEX];
                terLimit = getTertiaryBoundary();
            }
            secTer = this.elements[index] & -129;
        } else {
            if (!-assertionsDisabled) {
                if ((index >= ((int) this.elements[IX_FIRST_PRIMARY_INDEX]) ? IX_FIRST_SECONDARY_INDEX : null) == null) {
                    throw new AssertionError();
                }
            }
            secTer = getFirstSecTerForPrimary(index + IX_FIRST_SECONDARY_INDEX);
            terLimit = getTertiaryBoundary();
        }
        long st = ((((long) s) & 4294967295L) << 16) | ((long) t);
        while (secTer <= st) {
            index += IX_FIRST_SECONDARY_INDEX;
            secTer = this.elements[index];
            if ((128 & secTer) == 0 || (secTer >> 16) > ((long) s)) {
                return terLimit;
            }
            secTer &= -129;
        }
        if (!-assertionsDisabled) {
            Object obj;
            if ((secTer >> 16) == ((long) s)) {
                obj = IX_FIRST_SECONDARY_INDEX;
            } else {
                obj = null;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        return ((int) secTer) & DexFormat.MAX_TYPE_IDX;
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
        if (!-assertionsDisabled) {
            if (((p >> 24) != 254 ? IX_FIRST_SECONDARY_INDEX : null) == null) {
                throw new AssertionError();
            }
        }
        int start = (int) this.elements[IX_FIRST_PRIMARY_INDEX];
        if (!-assertionsDisabled) {
            if ((p >= this.elements[start] ? IX_FIRST_SECONDARY_INDEX : null) == null) {
                throw new AssertionError();
            }
        }
        int limit = this.elements.length - 1;
        if (!-assertionsDisabled) {
            if ((this.elements[limit] >= PRIMARY_SENTINEL ? IX_FIRST_SECONDARY_INDEX : null) == null) {
                throw new AssertionError();
            }
        }
        if (!-assertionsDisabled) {
            if ((p < this.elements[limit] ? IX_FIRST_SECONDARY_INDEX : null) == null) {
                throw new AssertionError();
            }
        }
        while (start + IX_FIRST_SECONDARY_INDEX < limit) {
            int i = (start + limit) / IX_FIRST_PRIMARY_INDEX;
            long q = this.elements[i];
            if ((128 & q) != 0) {
                int j;
                for (j = i + IX_FIRST_SECONDARY_INDEX; j != limit; j += IX_FIRST_SECONDARY_INDEX) {
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
    }

    private static boolean isEndOfPrimaryRange(long q) {
        return ((128 & q) != 0 || (127 & q) == 0) ? -assertionsDisabled : true;
    }
}
