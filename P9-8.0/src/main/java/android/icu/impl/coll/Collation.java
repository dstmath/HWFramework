package android.icu.impl.coll;

import android.icu.impl.Normalizer2Impl;

public final class Collation {
    static final /* synthetic */ boolean -assertionsDisabled = (Collation.class.desiredAssertionStatus() ^ 1);
    static final int BEFORE_WEIGHT16 = 256;
    static final int BUILDER_DATA_TAG = 7;
    public static final int CASE_AND_QUATERNARY_MASK = 49344;
    static final int CASE_AND_TERTIARY_MASK = 65343;
    public static final int CASE_LEVEL = 3;
    static final int CASE_LEVEL_FLAG = 8;
    public static final int CASE_MASK = 49152;
    static final int COMMON_BYTE = 5;
    static final int COMMON_SECONDARY_CE = 83886080;
    public static final int COMMON_SEC_AND_TER_CE = 83887360;
    static final int COMMON_TERTIARY_CE = 1280;
    public static final int COMMON_WEIGHT16 = 1280;
    static final int CONTRACTION_TAG = 9;
    static final int CONTRACT_NEXT_CCC = 512;
    static final int CONTRACT_SINGLE_CP_NO_MATCH = 256;
    static final int CONTRACT_TRAILING_CCC = 1024;
    static final int DIGIT_TAG = 10;
    public static final int EQUAL = 0;
    static final int EXPANSION32_TAG = 5;
    static final int EXPANSION_TAG = 6;
    static final int FALLBACK_CE32 = 192;
    static final int FALLBACK_TAG = 0;
    static final int FFFD_CE32 = -195323;
    public static final long FFFD_PRIMARY = 4294770688L;
    static final long FIRST_TRAILING_PRIMARY = 4278321664L;
    static final long FIRST_UNASSIGNED_PRIMARY = 4261675520L;
    public static final int GREATER = 1;
    static final int HANGUL_NO_SPECIAL_JAMO = 256;
    static final int HANGUL_TAG = 12;
    public static final int IDENTICAL_LEVEL = 6;
    static final int IDENTICAL_LEVEL_FLAG = 64;
    static final int IMPLICIT_TAG = 15;
    static final int LATIN_EXPANSION_TAG = 4;
    static final int LEAD_ALL_FALLBACK = 256;
    static final int LEAD_ALL_UNASSIGNED = 0;
    static final int LEAD_MIXED = 512;
    static final int LEAD_SURROGATE_TAG = 13;
    static final int LEAD_TYPE_MASK = 768;
    public static final int LESS = -1;
    public static final int LEVEL_SEPARATOR_BYTE = 1;
    static final int LONG_PRIMARY_CE32_LOW_BYTE = 193;
    static final int LONG_PRIMARY_TAG = 1;
    static final int LONG_SECONDARY_TAG = 2;
    static final int MAX_EXPANSION_LENGTH = 31;
    static final int MAX_INDEX = 524287;
    public static final long MAX_PRIMARY = 4294901760L;
    static final int MAX_REGULAR_CE32 = -64251;
    public static final int MERGE_SEPARATOR_BYTE = 2;
    static final int MERGE_SEPARATOR_CE32 = 33555717;
    public static final long MERGE_SEPARATOR_PRIMARY = 33554432;
    public static final long NO_CE = 4311744768L;
    static final int NO_CE32 = 1;
    static final long NO_CE_PRIMARY = 1;
    static final int NO_CE_WEIGHT16 = 256;
    public static final int NO_LEVEL = 0;
    static final int NO_LEVEL_FLAG = 1;
    static final int OFFSET_TAG = 14;
    static final int ONLY_SEC_TER_MASK = -49345;
    public static final int ONLY_TERTIARY_MASK = 16191;
    static final int PREFIX_TAG = 8;
    public static final int PRIMARY_COMPRESSION_HIGH_BYTE = 255;
    public static final int PRIMARY_COMPRESSION_LOW_BYTE = 3;
    public static final int PRIMARY_LEVEL = 1;
    static final int PRIMARY_LEVEL_FLAG = 2;
    public static final int QUATERNARY_LEVEL = 5;
    static final int QUATERNARY_LEVEL_FLAG = 32;
    public static final int QUATERNARY_MASK = 192;
    static final int RESERVED_TAG_3 = 3;
    static final int SECONDARY_AND_CASE_MASK = -16384;
    public static final int SECONDARY_LEVEL = 2;
    static final int SECONDARY_LEVEL_FLAG = 4;
    static final int SECONDARY_MASK = -65536;
    public static final int SENTINEL_CP = -1;
    static final int SPECIAL_CE32_LOW_BYTE = 192;
    public static final int TERMINATOR_BYTE = 0;
    public static final int TERTIARY_LEVEL = 4;
    static final int TERTIARY_LEVEL_FLAG = 16;
    static final int TRAIL_WEIGHT_BYTE = 255;
    static final int U0000_TAG = 11;
    static final int UNASSIGNED_CE32 = -1;
    static final int UNASSIGNED_IMPLICIT_BYTE = 254;
    public static final int ZERO_LEVEL = 7;
    static final int ZERO_LEVEL_FLAG = 128;

    static boolean isAssignedCE32(int ce32) {
        return (ce32 == 192 || ce32 == -1) ? false : true;
    }

    static int makeLongPrimaryCE32(long p) {
        return (int) (193 | p);
    }

    static long primaryFromLongPrimaryCE32(int ce32) {
        return ((long) ce32) & CollationRootElements.PRIMARY_SENTINEL;
    }

    static long ceFromLongPrimaryCE32(int ce32) {
        return (((long) (ce32 & -256)) << 32) | 83887360;
    }

    static int makeLongSecondaryCE32(int lower32) {
        return (lower32 | 192) | 2;
    }

    static long ceFromLongSecondaryCE32(int ce32) {
        return ((long) ce32) & CollationRootElements.PRIMARY_SENTINEL;
    }

    static int makeCE32FromTagIndexAndLength(int tag, int index, int length) {
        return (((index << 13) | (length << 8)) | 192) | tag;
    }

    static int makeCE32FromTagAndIndex(int tag, int index) {
        return ((index << 13) | 192) | tag;
    }

    static boolean isSpecialCE32(int ce32) {
        return (ce32 & 255) >= 192;
    }

    static int tagFromCE32(int ce32) {
        return ce32 & 15;
    }

    static boolean hasCE32Tag(int ce32, int tag) {
        return isSpecialCE32(ce32) && tagFromCE32(ce32) == tag;
    }

    static boolean isLongPrimaryCE32(int ce32) {
        return hasCE32Tag(ce32, 1);
    }

    static boolean isSimpleOrLongCE32(int ce32) {
        if (!isSpecialCE32(ce32) || tagFromCE32(ce32) == 1 || tagFromCE32(ce32) == 2) {
            return true;
        }
        return false;
    }

    static boolean isSelfContainedCE32(int ce32) {
        if (!isSpecialCE32(ce32) || tagFromCE32(ce32) == 1 || tagFromCE32(ce32) == 2 || tagFromCE32(ce32) == 4) {
            return true;
        }
        return false;
    }

    static boolean isPrefixCE32(int ce32) {
        return hasCE32Tag(ce32, 8);
    }

    static boolean isContractionCE32(int ce32) {
        return hasCE32Tag(ce32, 9);
    }

    static boolean ce32HasContext(int ce32) {
        if (isSpecialCE32(ce32)) {
            return tagFromCE32(ce32) == 8 || tagFromCE32(ce32) == 9;
        } else {
            return false;
        }
    }

    static long latinCE0FromCE32(int ce32) {
        return ((((long) (-16777216 & ce32)) << 32) | 83886080) | ((long) ((16711680 & ce32) >> 8));
    }

    static long latinCE1FromCE32(int ce32) {
        return ((((long) ce32) & 65280) << 16) | 1280;
    }

    static int indexFromCE32(int ce32) {
        return ce32 >>> 13;
    }

    static int lengthFromCE32(int ce32) {
        return (ce32 >> 8) & 31;
    }

    static char digitFromCE32(int ce32) {
        return (char) ((ce32 >> 8) & 15);
    }

    static long ceFromSimpleCE32(int ce32) {
        if (-assertionsDisabled || (ce32 & 255) < 192) {
            return ((((long) (SECONDARY_MASK & ce32)) << 32) | (((long) (Normalizer2Impl.JAMO_VT & ce32)) << 16)) | ((long) ((ce32 & 255) << 8));
        }
        throw new AssertionError();
    }

    static long ceFromCE32(int ce32) {
        int tertiary = ce32 & 255;
        if (tertiary < 192) {
            return ((((long) (SECONDARY_MASK & ce32)) << 32) | (((long) (Normalizer2Impl.JAMO_VT & ce32)) << 16)) | ((long) (tertiary << 8));
        }
        ce32 -= tertiary;
        if ((tertiary & 15) == 1) {
            return (((long) ce32) << 32) | 83887360;
        }
        if (-assertionsDisabled || (tertiary & 15) == 2) {
            return ((long) ce32) & 4294967295L;
        }
        throw new AssertionError();
    }

    public static long makeCE(long p) {
        return (p << 32) | 83887360;
    }

    static long makeCE(long p, int s, int t, int q) {
        return (((p << 32) | (((long) s) << 16)) | ((long) t)) | ((long) (q << 6));
    }

    public static long incTwoBytePrimaryByOffset(long basePrimary, boolean isCompressible, int offset) {
        long primary;
        if (isCompressible) {
            offset += (((int) (basePrimary >> 16)) & 255) - 4;
            primary = (long) (((offset % 251) + 4) << 16);
            offset /= 251;
        } else {
            offset += (((int) (basePrimary >> 16)) & 255) - 2;
            primary = (long) (((offset % 254) + 2) << 16);
            offset /= 254;
        }
        return ((4278190080L & basePrimary) + (((long) offset) << 24)) | primary;
    }

    public static long incThreeBytePrimaryByOffset(long basePrimary, boolean isCompressible, int offset) {
        offset += (((int) (basePrimary >> 8)) & 255) - 2;
        long primary = (long) (((offset % 254) + 2) << 8);
        offset /= 254;
        if (isCompressible) {
            offset += (((int) (basePrimary >> 16)) & 255) - 4;
            primary |= (long) (((offset % 251) + 4) << 16);
            offset /= 251;
        } else {
            offset += (((int) (basePrimary >> 16)) & 255) - 2;
            primary |= (long) (((offset % 254) + 2) << 16);
            offset /= 254;
        }
        return ((4278190080L & basePrimary) + (((long) offset) << 24)) | primary;
    }

    static long decTwoBytePrimaryByOneStep(long basePrimary, boolean isCompressible, int step) {
        if (-assertionsDisabled || (step > 0 && step <= 127)) {
            int byte2 = (((int) (basePrimary >> 16)) & 255) - step;
            if (isCompressible) {
                if (byte2 < 4) {
                    byte2 += 251;
                    basePrimary -= 16777216;
                }
            } else if (byte2 < 2) {
                byte2 += 254;
                basePrimary -= 16777216;
            }
            return (4278190080L & basePrimary) | ((long) (byte2 << 16));
        }
        throw new AssertionError();
    }

    static long decThreeBytePrimaryByOneStep(long basePrimary, boolean isCompressible, int step) {
        if (-assertionsDisabled || (step > 0 && step <= 127)) {
            int byte3 = (((int) (basePrimary >> 8)) & 255) - step;
            if (byte3 >= 2) {
                return (MAX_PRIMARY & basePrimary) | ((long) (byte3 << 8));
            }
            byte3 += 254;
            int byte2 = (((int) (basePrimary >> 16)) & 255) - 1;
            if (isCompressible) {
                if (byte2 < 4) {
                    byte2 = 254;
                    basePrimary -= 16777216;
                }
            } else if (byte2 < 2) {
                byte2 = 255;
                basePrimary -= 16777216;
            }
            return ((4278190080L & basePrimary) | ((long) (byte2 << 16))) | ((long) (byte3 << 8));
        }
        throw new AssertionError();
    }

    static long getThreeBytePrimaryForOffsetData(int c, long dataCE) {
        int lower32 = (int) dataCE;
        return incThreeBytePrimaryByOffset(dataCE >>> 32, (lower32 & 128) != 0, (c - (lower32 >> 8)) * (lower32 & 127));
    }

    static long unassignedPrimaryFromCodePoint(int c) {
        c++;
        c /= 18;
        return 4261412864L | ((((long) (((c % 18) * 14) + 2)) | ((long) (((c % 254) + 2) << 8))) | ((long) ((((c / 254) % 251) + 4) << 16)));
    }

    static long unassignedCEFromCodePoint(int c) {
        return makeCE(unassignedPrimaryFromCodePoint(c));
    }
}
