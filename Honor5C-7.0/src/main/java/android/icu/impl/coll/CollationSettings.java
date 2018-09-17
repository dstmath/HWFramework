package android.icu.impl.coll;

import com.android.dex.DexFormat;
import dalvik.bytecode.Opcodes;
import java.util.Arrays;
import libcore.icu.ICU;
import org.w3c.dom.traversal.NodeFilter;

public final class CollationSettings extends SharedObject {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    static final int ALTERNATE_MASK = 12;
    public static final int BACKWARD_SECONDARY = 2048;
    public static final int CASE_FIRST = 512;
    public static final int CASE_FIRST_AND_UPPER_MASK = 768;
    public static final int CASE_LEVEL = 1024;
    public static final int CHECK_FCD = 1;
    private static final int[] EMPTY_INT_ARRAY = null;
    static final int MAX_VARIABLE_MASK = 112;
    static final int MAX_VARIABLE_SHIFT = 4;
    static final int MAX_VAR_CURRENCY = 3;
    static final int MAX_VAR_PUNCT = 1;
    static final int MAX_VAR_SPACE = 0;
    static final int MAX_VAR_SYMBOL = 2;
    public static final int NUMERIC = 2;
    static final int SHIFTED = 4;
    static final int STRENGTH_MASK = 61440;
    static final int STRENGTH_SHIFT = 12;
    static final int UPPER_FIRST = 256;
    public int fastLatinOptions;
    public char[] fastLatinPrimaries;
    long minHighNoReorder;
    public int options;
    public int[] reorderCodes;
    long[] reorderRanges;
    public byte[] reorderTable;
    public long variableTop;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.coll.CollationSettings.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.coll.CollationSettings.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationSettings.<clinit>():void");
    }

    public void setFlag(int r1, boolean r2) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.coll.CollationSettings.setFlag(int, boolean):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationSettings.setFlag(int, boolean):void");
    }

    public void setFlagDefault(int r1, int r2) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.coll.CollationSettings.setFlagDefault(int, int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationSettings.setFlagDefault(int, int):void");
    }

    CollationSettings() {
        this.options = 8208;
        this.reorderCodes = EMPTY_INT_ARRAY;
        this.fastLatinOptions = -1;
        this.fastLatinPrimaries = new char[CollationFastLatin.LATIN_LIMIT];
    }

    public CollationSettings clone() {
        CollationSettings newSettings = (CollationSettings) super.clone();
        newSettings.fastLatinPrimaries = (char[]) this.fastLatinPrimaries.clone();
        return newSettings;
    }

    public boolean equals(Object other) {
        if (other == null || !getClass().equals(other.getClass())) {
            return -assertionsDisabled;
        }
        CollationSettings o = (CollationSettings) other;
        if (this.options != o.options) {
            return -assertionsDisabled;
        }
        if (((this.options & STRENGTH_SHIFT) == 0 || this.variableTop == o.variableTop) && Arrays.equals(this.reorderCodes, o.reorderCodes)) {
            return true;
        }
        return -assertionsDisabled;
    }

    public int hashCode() {
        int h = this.options << 8;
        if ((this.options & STRENGTH_SHIFT) != 0) {
            h = (int) (((long) h) ^ this.variableTop);
        }
        h ^= this.reorderCodes.length;
        for (int i = MAX_VAR_SPACE; i < this.reorderCodes.length; i += MAX_VAR_PUNCT) {
            h ^= this.reorderCodes[i] << i;
        }
        return h;
    }

    public void resetReordering() {
        this.reorderTable = null;
        this.minHighNoReorder = 0;
        this.reorderRanges = null;
        this.reorderCodes = EMPTY_INT_ARRAY;
    }

    void aliasReordering(CollationData data, int[] codesAndRanges, int codesLength, byte[] table) {
        int[] codes;
        int i = MAX_VAR_PUNCT;
        int i2 = MAX_VAR_SPACE;
        if (codesLength == codesAndRanges.length) {
            codes = codesAndRanges;
        } else {
            codes = new int[codesLength];
            System.arraycopy(codesAndRanges, MAX_VAR_SPACE, codes, MAX_VAR_SPACE, codesLength);
        }
        int rangesStart = codesLength;
        int rangesLimit = codesAndRanges.length;
        int rangesLength = rangesLimit - codesLength;
        if (table == null || (rangesLength != 0 ? rangesLength < NUMERIC || (codesAndRanges[codesLength] & DexFormat.MAX_TYPE_IDX) != 0 || (codesAndRanges[rangesLimit - 1] & DexFormat.MAX_TYPE_IDX) == 0 : reorderTableHasSplitBytes(table))) {
            setReordering(data, codes);
            return;
        }
        this.reorderTable = table;
        this.reorderCodes = codes;
        int firstSplitByteRangeIndex = codesLength;
        while (firstSplitByteRangeIndex < rangesLimit && (codesAndRanges[firstSplitByteRangeIndex] & 16711680) == 0) {
            firstSplitByteRangeIndex += MAX_VAR_PUNCT;
        }
        if (firstSplitByteRangeIndex == rangesLimit) {
            if (!-assertionsDisabled) {
                if (!reorderTableHasSplitBytes(table)) {
                    i2 = MAX_VAR_PUNCT;
                }
                if (i2 == 0) {
                    throw new AssertionError();
                }
            }
            this.minHighNoReorder = 0;
            this.reorderRanges = null;
        } else {
            if (!-assertionsDisabled) {
                if (table[codesAndRanges[firstSplitByteRangeIndex] >>> 24] != null) {
                    i = MAX_VAR_SPACE;
                }
                if (i == 0) {
                    throw new AssertionError();
                }
            }
            this.minHighNoReorder = ((long) codesAndRanges[rangesLimit - 1]) & Collation.MAX_PRIMARY;
            setReorderRanges(codesAndRanges, firstSplitByteRangeIndex, rangesLimit - firstSplitByteRangeIndex);
        }
    }

    public void setReordering(CollationData data, int[] codes) {
        if (codes.length == 0 || (codes.length == MAX_VAR_PUNCT && codes[MAX_VAR_SPACE] == Opcodes.OP_SPUT)) {
            resetReordering();
            return;
        }
        UVector32 rangesList = new UVector32();
        data.makeReorderRanges(codes, rangesList);
        int rangesLength = rangesList.size();
        if (rangesLength == 0) {
            resetReordering();
            return;
        }
        int rangesStart;
        int[] ranges = rangesList.getBuffer();
        if (!-assertionsDisabled) {
            if ((rangesLength >= NUMERIC ? MAX_VAR_PUNCT : null) == null) {
                throw new AssertionError();
            }
        }
        if (!-assertionsDisabled) {
            Object obj = ((ranges[MAX_VAR_SPACE] & DexFormat.MAX_TYPE_IDX) != 0 || (ranges[rangesLength - 1] & DexFormat.MAX_TYPE_IDX) == 0) ? null : MAX_VAR_PUNCT;
            if (obj == null) {
                throw new AssertionError();
            }
        }
        this.minHighNoReorder = ((long) ranges[rangesLength - 1]) & Collation.MAX_PRIMARY;
        byte[] table = new byte[UPPER_FIRST];
        int b = MAX_VAR_SPACE;
        int firstSplitByteRangeIndex = -1;
        for (int i = MAX_VAR_SPACE; i < rangesLength; i += MAX_VAR_PUNCT) {
            int pair = ranges[i];
            int limit1 = pair >>> 24;
            while (b < limit1) {
                table[b] = (byte) (b + pair);
                b += MAX_VAR_PUNCT;
            }
            if ((16711680 & pair) != 0) {
                table[limit1] = (byte) 0;
                b = limit1 + MAX_VAR_PUNCT;
                if (firstSplitByteRangeIndex < 0) {
                    firstSplitByteRangeIndex = i;
                }
            }
        }
        while (b <= Opcodes.OP_CONST_CLASS_JUMBO) {
            table[b] = (byte) b;
            b += MAX_VAR_PUNCT;
        }
        if (firstSplitByteRangeIndex < 0) {
            rangesLength = MAX_VAR_SPACE;
            rangesStart = MAX_VAR_SPACE;
        } else {
            rangesStart = firstSplitByteRangeIndex;
            rangesLength -= firstSplitByteRangeIndex;
        }
        setReorderArrays(codes, ranges, rangesStart, rangesLength, table);
    }

    private void setReorderArrays(int[] codes, int[] ranges, int rangesStart, int rangesLength, byte[] table) {
        Object obj = MAX_VAR_PUNCT;
        if (codes == null) {
            codes = EMPTY_INT_ARRAY;
        }
        if (!-assertionsDisabled) {
            if ((codes.length == 0 ? MAX_VAR_PUNCT : MAX_VAR_SPACE) != (table == null ? MAX_VAR_PUNCT : MAX_VAR_SPACE)) {
                obj = MAX_VAR_SPACE;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        this.reorderTable = table;
        this.reorderCodes = codes;
        setReorderRanges(ranges, rangesStart, rangesLength);
    }

    private void setReorderRanges(int[] ranges, int rangesStart, int rangesLength) {
        if (rangesLength == 0) {
            this.reorderRanges = null;
            return;
        }
        this.reorderRanges = new long[rangesLength];
        int i = MAX_VAR_SPACE;
        while (true) {
            int i2 = i + MAX_VAR_PUNCT;
            int rangesStart2 = rangesStart + MAX_VAR_PUNCT;
            this.reorderRanges[i] = ((long) ranges[rangesStart]) & 4294967295L;
            if (i2 < rangesLength) {
                i = i2;
                rangesStart = rangesStart2;
            } else {
                return;
            }
        }
    }

    public void copyReorderingFrom(CollationSettings other) {
        if (other.hasReordering()) {
            this.minHighNoReorder = other.minHighNoReorder;
            this.reorderTable = other.reorderTable;
            this.reorderRanges = other.reorderRanges;
            this.reorderCodes = other.reorderCodes;
            return;
        }
        resetReordering();
    }

    public boolean hasReordering() {
        return this.reorderTable != null ? true : -assertionsDisabled;
    }

    private static boolean reorderTableHasSplitBytes(byte[] table) {
        if (!-assertionsDisabled) {
            if (!(table[MAX_VAR_SPACE] == null ? true : -assertionsDisabled)) {
                throw new AssertionError();
            }
        }
        for (int i = MAX_VAR_PUNCT; i < UPPER_FIRST; i += MAX_VAR_PUNCT) {
            if (table[i] == null) {
                return true;
            }
        }
        return -assertionsDisabled;
    }

    public long reorder(long p) {
        byte b = this.reorderTable[((int) p) >>> 24];
        if (b != null || p <= 1) {
            return ((((long) b) & 255) << 24) | (16777215 & p);
        }
        return reorderEx(p);
    }

    private long reorderEx(long p) {
        if (!-assertionsDisabled) {
            if ((this.minHighNoReorder > 0 ? MAX_VAR_PUNCT : null) == null) {
                throw new AssertionError();
            }
        }
        if (p >= this.minHighNoReorder) {
            return p;
        }
        long q = p | 65535;
        int i = MAX_VAR_SPACE;
        while (true) {
            long r = this.reorderRanges[i];
            if (q < r) {
                return (((long) ((short) ((int) r))) << 24) + p;
            }
            i += MAX_VAR_PUNCT;
        }
    }

    public void setStrength(int value) {
        int noStrength = this.options & -61441;
        switch (value) {
            case MAX_VAR_SPACE /*0*/:
            case MAX_VAR_PUNCT /*1*/:
            case NUMERIC /*2*/:
            case MAX_VAR_CURRENCY /*3*/:
            case ICU.U_BUFFER_OVERFLOW_ERROR /*15*/:
                this.options = (value << STRENGTH_SHIFT) | noStrength;
            default:
                throw new IllegalArgumentException("illegal strength value " + value);
        }
    }

    public void setStrengthDefault(int defaultOptions) {
        this.options = (STRENGTH_MASK & defaultOptions) | (this.options & -61441);
    }

    static int getStrength(int options) {
        return options >> STRENGTH_SHIFT;
    }

    public int getStrength() {
        return getStrength(this.options);
    }

    public boolean getFlag(int bit) {
        return (this.options & bit) != 0 ? true : -assertionsDisabled;
    }

    public void setCaseFirst(int value) {
        Object obj = MAX_VAR_PUNCT;
        if (!-assertionsDisabled) {
            if (!(value == 0 || value == CASE_FIRST || value == CASE_FIRST_AND_UPPER_MASK)) {
                obj = MAX_VAR_SPACE;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        this.options = (this.options & -769) | value;
    }

    public void setCaseFirstDefault(int defaultOptions) {
        this.options = (defaultOptions & CASE_FIRST_AND_UPPER_MASK) | (this.options & -769);
    }

    public int getCaseFirst() {
        return this.options & CASE_FIRST_AND_UPPER_MASK;
    }

    public void setAlternateHandlingShifted(boolean value) {
        int noAlternate = this.options & -13;
        if (value) {
            this.options = noAlternate | SHIFTED;
        } else {
            this.options = noAlternate;
        }
    }

    public void setAlternateHandlingDefault(int defaultOptions) {
        this.options = (defaultOptions & STRENGTH_SHIFT) | (this.options & -13);
    }

    public boolean getAlternateHandling() {
        return (this.options & STRENGTH_SHIFT) != 0 ? true : -assertionsDisabled;
    }

    public void setMaxVariable(int value, int defaultOptions) {
        int noMax = this.options & -113;
        switch (value) {
            case NodeFilter.SHOW_ALL /*-1*/:
                this.options = (defaultOptions & MAX_VARIABLE_MASK) | noMax;
            case MAX_VAR_SPACE /*0*/:
            case MAX_VAR_PUNCT /*1*/:
            case NUMERIC /*2*/:
            case MAX_VAR_CURRENCY /*3*/:
                this.options = (value << SHIFTED) | noMax;
            default:
                throw new IllegalArgumentException("illegal maxVariable value " + value);
        }
    }

    public int getMaxVariable() {
        return (this.options & MAX_VARIABLE_MASK) >> SHIFTED;
    }

    static boolean isTertiaryWithCaseBits(int options) {
        return (options & 1536) == CASE_FIRST ? true : -assertionsDisabled;
    }

    static int getTertiaryMask(int options) {
        return isTertiaryWithCaseBits(options) ? 65343 : Collation.ONLY_TERTIARY_MASK;
    }

    static boolean sortsTertiaryUpperCaseFirst(int options) {
        return (options & 1792) == CASE_FIRST_AND_UPPER_MASK ? true : -assertionsDisabled;
    }

    public boolean dontCheckFCD() {
        return (this.options & MAX_VAR_PUNCT) == 0 ? true : -assertionsDisabled;
    }

    boolean hasBackwardSecondary() {
        return (this.options & BACKWARD_SECONDARY) != 0 ? true : -assertionsDisabled;
    }

    public boolean isNumeric() {
        return (this.options & NUMERIC) != 0 ? true : -assertionsDisabled;
    }
}
