package android.icu.impl.coll;

import android.icu.impl.Normalizer2Impl;
import dalvik.bytecode.Opcodes;

public final class Collation {
    static final /* synthetic */ boolean -assertionsDisabled = false;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.coll.Collation.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.coll.Collation.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.Collation.<clinit>():void");
    }

    static boolean isAssignedCE32(int ce32) {
        return (ce32 == SPECIAL_CE32_LOW_BYTE || ce32 == UNASSIGNED_CE32) ? -assertionsDisabled : true;
    }

    static int makeLongPrimaryCE32(long p) {
        return (int) (193 | p);
    }

    static long primaryFromLongPrimaryCE32(int ce32) {
        return ((long) ce32) & CollationRootElements.PRIMARY_SENTINEL;
    }

    static long ceFromLongPrimaryCE32(int ce32) {
        return (((long) (ce32 & -256)) << QUATERNARY_LEVEL_FLAG) | 83887360;
    }

    static int makeLongSecondaryCE32(int lower32) {
        return (lower32 | SPECIAL_CE32_LOW_BYTE) | SECONDARY_LEVEL;
    }

    static long ceFromLongSecondaryCE32(int ce32) {
        return ((long) ce32) & CollationRootElements.PRIMARY_SENTINEL;
    }

    static int makeCE32FromTagIndexAndLength(int tag, int index, int length) {
        return (((index << LEAD_SURROGATE_TAG) | (length << PREFIX_TAG)) | SPECIAL_CE32_LOW_BYTE) | tag;
    }

    static int makeCE32FromTagAndIndex(int tag, int index) {
        return ((index << LEAD_SURROGATE_TAG) | SPECIAL_CE32_LOW_BYTE) | tag;
    }

    static boolean isSpecialCE32(int ce32) {
        return (ce32 & TRAIL_WEIGHT_BYTE) >= SPECIAL_CE32_LOW_BYTE ? true : -assertionsDisabled;
    }

    static int tagFromCE32(int ce32) {
        return ce32 & IMPLICIT_TAG;
    }

    static boolean hasCE32Tag(int ce32, int tag) {
        return (isSpecialCE32(ce32) && tagFromCE32(ce32) == tag) ? true : -assertionsDisabled;
    }

    static boolean isLongPrimaryCE32(int ce32) {
        return hasCE32Tag(ce32, PRIMARY_LEVEL);
    }

    static boolean isSimpleOrLongCE32(int ce32) {
        if (!isSpecialCE32(ce32) || tagFromCE32(ce32) == PRIMARY_LEVEL || tagFromCE32(ce32) == SECONDARY_LEVEL) {
            return true;
        }
        return -assertionsDisabled;
    }

    static boolean isSelfContainedCE32(int ce32) {
        if (!isSpecialCE32(ce32) || tagFromCE32(ce32) == PRIMARY_LEVEL || tagFromCE32(ce32) == SECONDARY_LEVEL || tagFromCE32(ce32) == TERTIARY_LEVEL) {
            return true;
        }
        return -assertionsDisabled;
    }

    static boolean isPrefixCE32(int ce32) {
        return hasCE32Tag(ce32, PREFIX_TAG);
    }

    static boolean isContractionCE32(int ce32) {
        return hasCE32Tag(ce32, CONTRACTION_TAG);
    }

    static boolean ce32HasContext(int ce32) {
        if (isSpecialCE32(ce32)) {
            return (tagFromCE32(ce32) == PREFIX_TAG || tagFromCE32(ce32) == CONTRACTION_TAG) ? true : -assertionsDisabled;
        } else {
            return -assertionsDisabled;
        }
    }

    static long latinCE0FromCE32(int ce32) {
        return ((((long) (-16777216 & ce32)) << QUATERNARY_LEVEL_FLAG) | 83886080) | ((long) ((16711680 & ce32) >> PREFIX_TAG));
    }

    static long latinCE1FromCE32(int ce32) {
        return ((((long) ce32) & 65280) << TERTIARY_LEVEL_FLAG) | 1280;
    }

    static int indexFromCE32(int ce32) {
        return ce32 >>> LEAD_SURROGATE_TAG;
    }

    static int lengthFromCE32(int ce32) {
        return (ce32 >> PREFIX_TAG) & MAX_EXPANSION_LENGTH;
    }

    static char digitFromCE32(int ce32) {
        return (char) ((ce32 >> PREFIX_TAG) & IMPLICIT_TAG);
    }

    static long ceFromSimpleCE32(int ce32) {
        if (!-assertionsDisabled) {
            if (((ce32 & TRAIL_WEIGHT_BYTE) < SPECIAL_CE32_LOW_BYTE ? PRIMARY_LEVEL : null) == null) {
                throw new AssertionError();
            }
        }
        return ((((long) (SECONDARY_MASK & ce32)) << QUATERNARY_LEVEL_FLAG) | (((long) (Normalizer2Impl.JAMO_VT & ce32)) << TERTIARY_LEVEL_FLAG)) | ((long) ((ce32 & TRAIL_WEIGHT_BYTE) << PREFIX_TAG));
    }

    static long ceFromCE32(int ce32) {
        Object obj = PRIMARY_LEVEL;
        int tertiary = ce32 & TRAIL_WEIGHT_BYTE;
        if (tertiary < SPECIAL_CE32_LOW_BYTE) {
            return ((((long) (SECONDARY_MASK & ce32)) << 32) | (((long) (Normalizer2Impl.JAMO_VT & ce32)) << TERTIARY_LEVEL_FLAG)) | ((long) (tertiary << PREFIX_TAG));
        }
        ce32 -= tertiary;
        if ((tertiary & IMPLICIT_TAG) == PRIMARY_LEVEL) {
            return (((long) ce32) << 32) | 83887360;
        }
        if (!-assertionsDisabled) {
            if ((tertiary & IMPLICIT_TAG) != SECONDARY_LEVEL) {
                obj = null;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        return ((long) ce32) & 4294967295L;
    }

    public static long makeCE(long p) {
        return (p << QUATERNARY_LEVEL_FLAG) | 83887360;
    }

    static long makeCE(long p, int s, int t, int q) {
        return (((p << QUATERNARY_LEVEL_FLAG) | (((long) s) << TERTIARY_LEVEL_FLAG)) | ((long) t)) | ((long) (q << IDENTICAL_LEVEL));
    }

    public static long incTwoBytePrimaryByOffset(long basePrimary, boolean isCompressible, int offset) {
        long primary;
        if (isCompressible) {
            offset += (((int) (basePrimary >> 16)) & TRAIL_WEIGHT_BYTE) - 4;
            primary = (long) (((offset % Opcodes.OP_INVOKE_SUPER_QUICK_RANGE) + TERTIARY_LEVEL) << TERTIARY_LEVEL_FLAG);
            offset /= Opcodes.OP_INVOKE_SUPER_QUICK_RANGE;
        } else {
            offset += (((int) (basePrimary >> 16)) & TRAIL_WEIGHT_BYTE) - 2;
            primary = (long) (((offset % UNASSIGNED_IMPLICIT_BYTE) + SECONDARY_LEVEL) << TERTIARY_LEVEL_FLAG);
            offset /= UNASSIGNED_IMPLICIT_BYTE;
        }
        return ((4278190080L & basePrimary) + (((long) offset) << 24)) | primary;
    }

    public static long incThreeBytePrimaryByOffset(long basePrimary, boolean isCompressible, int offset) {
        offset += (((int) (basePrimary >> PREFIX_TAG)) & TRAIL_WEIGHT_BYTE) - 2;
        long primary = (long) (((offset % UNASSIGNED_IMPLICIT_BYTE) + SECONDARY_LEVEL) << PREFIX_TAG);
        offset /= UNASSIGNED_IMPLICIT_BYTE;
        if (isCompressible) {
            offset += (((int) (basePrimary >> 16)) & TRAIL_WEIGHT_BYTE) - 4;
            primary |= (long) (((offset % Opcodes.OP_INVOKE_SUPER_QUICK_RANGE) + TERTIARY_LEVEL) << TERTIARY_LEVEL_FLAG);
            offset /= Opcodes.OP_INVOKE_SUPER_QUICK_RANGE;
        } else {
            offset += (((int) (basePrimary >> 16)) & TRAIL_WEIGHT_BYTE) - 2;
            primary |= (long) (((offset % UNASSIGNED_IMPLICIT_BYTE) + SECONDARY_LEVEL) << TERTIARY_LEVEL_FLAG);
            offset /= UNASSIGNED_IMPLICIT_BYTE;
        }
        return ((4278190080L & basePrimary) + (((long) offset) << 24)) | primary;
    }

    static long decTwoBytePrimaryByOneStep(long basePrimary, boolean isCompressible, int step) {
        Object obj = null;
        if (!-assertionsDisabled) {
            if (step > 0 && step <= Opcodes.OP_NEG_FLOAT) {
                obj = PRIMARY_LEVEL;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        int byte2 = (((int) (basePrimary >> TERTIARY_LEVEL_FLAG)) & TRAIL_WEIGHT_BYTE) - step;
        if (isCompressible) {
            if (byte2 < TERTIARY_LEVEL) {
                byte2 += Opcodes.OP_INVOKE_SUPER_QUICK_RANGE;
                basePrimary -= 16777216;
            }
        } else if (byte2 < SECONDARY_LEVEL) {
            byte2 += UNASSIGNED_IMPLICIT_BYTE;
            basePrimary -= 16777216;
        }
        return (4278190080L & basePrimary) | ((long) (byte2 << TERTIARY_LEVEL_FLAG));
    }

    static long decThreeBytePrimaryByOneStep(long basePrimary, boolean isCompressible, int step) {
        Object obj = null;
        if (!-assertionsDisabled) {
            if (step > 0 && step <= Opcodes.OP_NEG_FLOAT) {
                obj = PRIMARY_LEVEL;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        int byte3 = (((int) (basePrimary >> PREFIX_TAG)) & TRAIL_WEIGHT_BYTE) - step;
        if (byte3 >= SECONDARY_LEVEL) {
            return (MAX_PRIMARY & basePrimary) | ((long) (byte3 << PREFIX_TAG));
        }
        byte3 += UNASSIGNED_IMPLICIT_BYTE;
        int byte2 = (((int) (basePrimary >> TERTIARY_LEVEL_FLAG)) & TRAIL_WEIGHT_BYTE) + UNASSIGNED_CE32;
        if (isCompressible) {
            if (byte2 < TERTIARY_LEVEL) {
                byte2 = UNASSIGNED_IMPLICIT_BYTE;
                basePrimary -= 16777216;
            }
        } else if (byte2 < SECONDARY_LEVEL) {
            byte2 = TRAIL_WEIGHT_BYTE;
            basePrimary -= 16777216;
        }
        return ((4278190080L & basePrimary) | ((long) (byte2 << TERTIARY_LEVEL_FLAG))) | ((long) (byte3 << PREFIX_TAG));
    }

    static long getThreeBytePrimaryForOffsetData(int c, long dataCE) {
        boolean isCompressible = -assertionsDisabled;
        long p = dataCE >>> QUATERNARY_LEVEL_FLAG;
        int lower32 = (int) dataCE;
        int offset = (c - (lower32 >> PREFIX_TAG)) * (lower32 & Opcodes.OP_NEG_FLOAT);
        if ((lower32 & ZERO_LEVEL_FLAG) != 0) {
            isCompressible = true;
        }
        return incThreeBytePrimaryByOffset(p, isCompressible, offset);
    }

    static long unassignedPrimaryFromCodePoint(int c) {
        c += PRIMARY_LEVEL;
        c /= 18;
        return 4261412864L | ((((long) (((c % 18) * OFFSET_TAG) + SECONDARY_LEVEL)) | ((long) (((c % UNASSIGNED_IMPLICIT_BYTE) + SECONDARY_LEVEL) << PREFIX_TAG))) | ((long) ((((c / UNASSIGNED_IMPLICIT_BYTE) % Opcodes.OP_INVOKE_SUPER_QUICK_RANGE) + TERTIARY_LEVEL) << TERTIARY_LEVEL_FLAG)));
    }

    static long unassignedCEFromCodePoint(int c) {
        return makeCE(unassignedPrimaryFromCodePoint(c));
    }
}
