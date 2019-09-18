package android.icu.impl.coll;

import android.icu.text.DateTimePatternGenerator;
import java.util.Arrays;

public final class CollationSettings extends SharedObject {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    static final int ALTERNATE_MASK = 12;
    public static final int BACKWARD_SECONDARY = 2048;
    public static final int CASE_FIRST = 512;
    public static final int CASE_FIRST_AND_UPPER_MASK = 768;
    public static final int CASE_LEVEL = 1024;
    public static final int CHECK_FCD = 1;
    private static final int[] EMPTY_INT_ARRAY = new int[0];
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
    public int fastLatinOptions = -1;
    public char[] fastLatinPrimaries = new char[CollationFastLatin.LATIN_LIMIT];
    long minHighNoReorder;
    public int options = 8208;
    public int[] reorderCodes = EMPTY_INT_ARRAY;
    long[] reorderRanges;
    public byte[] reorderTable;
    public long variableTop;

    CollationSettings() {
    }

    public CollationSettings clone() {
        CollationSettings newSettings = (CollationSettings) super.clone();
        newSettings.fastLatinPrimaries = (char[]) this.fastLatinPrimaries.clone();
        return newSettings;
    }

    public boolean equals(Object other) {
        if (other == null || !getClass().equals(other.getClass())) {
            return false;
        }
        CollationSettings o = (CollationSettings) other;
        if (this.options != o.options) {
            return false;
        }
        if (((this.options & 12) == 0 || this.variableTop == o.variableTop) && Arrays.equals(this.reorderCodes, o.reorderCodes)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int h = this.options << 8;
        if ((this.options & 12) != 0) {
            h = (int) (((long) h) ^ this.variableTop);
        }
        int h2 = h ^ this.reorderCodes.length;
        for (int i = 0; i < this.reorderCodes.length; i++) {
            h2 ^= this.reorderCodes[i] << i;
        }
        return h2;
    }

    public void resetReordering() {
        this.reorderTable = null;
        this.minHighNoReorder = 0;
        this.reorderRanges = null;
        this.reorderCodes = EMPTY_INT_ARRAY;
    }

    /* access modifiers changed from: package-private */
    public void aliasReordering(CollationData data, int[] codesAndRanges, int codesLength, byte[] table) {
        int[] codes;
        if (codesLength == codesAndRanges.length) {
            codes = codesAndRanges;
        } else {
            codes = new int[codesLength];
            System.arraycopy(codesAndRanges, 0, codes, 0, codesLength);
        }
        int rangesStart = codesLength;
        int rangesLimit = codesAndRanges.length;
        int rangesLength = rangesLimit - rangesStart;
        if (table == null || (rangesLength != 0 ? rangesLength < 2 || (codesAndRanges[rangesStart] & DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH) != 0 || (codesAndRanges[rangesLimit - 1] & DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH) == 0 : reorderTableHasSplitBytes(table))) {
            setReordering(data, codes);
            return;
        }
        this.reorderTable = table;
        this.reorderCodes = codes;
        int firstSplitByteRangeIndex = rangesStart;
        while (firstSplitByteRangeIndex < rangesLimit && (codesAndRanges[firstSplitByteRangeIndex] & 16711680) == 0) {
            firstSplitByteRangeIndex++;
        }
        if (firstSplitByteRangeIndex == rangesLimit) {
            this.minHighNoReorder = 0;
            this.reorderRanges = null;
        } else {
            this.minHighNoReorder = ((long) codesAndRanges[rangesLimit - 1]) & Collation.MAX_PRIMARY;
            setReorderRanges(codesAndRanges, firstSplitByteRangeIndex, rangesLimit - firstSplitByteRangeIndex);
        }
    }

    public void setReordering(CollationData data, int[] codes) {
        int rangesLength;
        if (codes.length != 0) {
            int rangesStart = 0;
            if (!(codes.length == 1 && codes[0] == 103)) {
                UVector32 rangesList = new UVector32();
                data.makeReorderRanges(codes, rangesList);
                int rangesLength2 = rangesList.size();
                if (rangesLength2 == 0) {
                    resetReordering();
                    return;
                }
                int[] ranges = rangesList.getBuffer();
                this.minHighNoReorder = ((long) ranges[rangesLength2 - 1]) & Collation.MAX_PRIMARY;
                byte[] table = new byte[256];
                int firstSplitByteRangeIndex = -1;
                int b = 0;
                for (int i = 0; i < rangesLength2; i++) {
                    int pair = ranges[i];
                    int limit1 = pair >>> 24;
                    while (b < limit1) {
                        table[b] = (byte) (b + pair);
                        b++;
                    }
                    if ((16711680 & pair) != 0) {
                        table[limit1] = 0;
                        b = limit1 + 1;
                        if (firstSplitByteRangeIndex < 0) {
                            firstSplitByteRangeIndex = i;
                        }
                    }
                }
                for (int b2 = b; b2 <= 255; b2++) {
                    table[b2] = (byte) b2;
                }
                if (firstSplitByteRangeIndex < 0) {
                    rangesLength = 0;
                } else {
                    rangesStart = firstSplitByteRangeIndex;
                    rangesLength = rangesLength2 - firstSplitByteRangeIndex;
                }
                setReorderArrays(codes, ranges, rangesStart, rangesLength, table);
                return;
            }
        }
        resetReordering();
    }

    private void setReorderArrays(int[] codes, int[] ranges, int rangesStart, int rangesLength, byte[] table) {
        if (codes == null) {
            codes = EMPTY_INT_ARRAY;
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
        int i = 0;
        while (true) {
            int i2 = i + 1;
            int rangesStart2 = rangesStart + 1;
            this.reorderRanges[i] = ((long) ranges[rangesStart]) & 4294967295L;
            if (i2 >= rangesLength) {
                int i3 = rangesStart2;
                return;
            } else {
                i = i2;
                rangesStart = rangesStart2;
            }
        }
    }

    public void copyReorderingFrom(CollationSettings other) {
        if (!other.hasReordering()) {
            resetReordering();
            return;
        }
        this.minHighNoReorder = other.minHighNoReorder;
        this.reorderTable = other.reorderTable;
        this.reorderRanges = other.reorderRanges;
        this.reorderCodes = other.reorderCodes;
    }

    public boolean hasReordering() {
        return this.reorderTable != null;
    }

    private static boolean reorderTableHasSplitBytes(byte[] table) {
        for (int i = 1; i < 256; i++) {
            if (table[i] == 0) {
                return true;
            }
        }
        return false;
    }

    public long reorder(long p) {
        byte b = this.reorderTable[((int) p) >>> 24];
        if (b != 0 || p <= 1) {
            return ((((long) b) & 255) << 24) | (16777215 & p);
        }
        return reorderEx(p);
    }

    private long reorderEx(long p) {
        if (p >= this.minHighNoReorder) {
            return p;
        }
        long q = 65535 | p;
        int i = 0;
        while (true) {
            long j = this.reorderRanges[i];
            long r = j;
            if (q < j) {
                return (((long) ((short) ((int) r))) << 24) + p;
            }
            i++;
        }
    }

    public void setStrength(int value) {
        int noStrength = this.options & -61441;
        if (value != 15) {
            switch (value) {
                case 0:
                case 1:
                case 2:
                case 3:
                    break;
                default:
                    throw new IllegalArgumentException("illegal strength value " + value);
            }
        }
        this.options = (value << 12) | noStrength;
    }

    public void setStrengthDefault(int defaultOptions) {
        this.options = (STRENGTH_MASK & defaultOptions) | (this.options & -61441);
    }

    static int getStrength(int options2) {
        return options2 >> 12;
    }

    public int getStrength() {
        return getStrength(this.options);
    }

    public void setFlag(int bit, boolean value) {
        if (value) {
            this.options |= bit;
        } else {
            this.options &= ~bit;
        }
    }

    public void setFlagDefault(int bit, int defaultOptions) {
        this.options = (this.options & (~bit)) | (defaultOptions & bit);
    }

    public boolean getFlag(int bit) {
        return (this.options & bit) != 0;
    }

    public void setCaseFirst(int value) {
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
            this.options = noAlternate | 4;
        } else {
            this.options = noAlternate;
        }
    }

    public void setAlternateHandlingDefault(int defaultOptions) {
        this.options = (defaultOptions & 12) | (this.options & -13);
    }

    public boolean getAlternateHandling() {
        return (this.options & 12) != 0;
    }

    public void setMaxVariable(int value, int defaultOptions) {
        int noMax = this.options & -113;
        switch (value) {
            case -1:
                this.options = (defaultOptions & 112) | noMax;
                return;
            case 0:
            case 1:
            case 2:
            case 3:
                this.options = (value << 4) | noMax;
                return;
            default:
                throw new IllegalArgumentException("illegal maxVariable value " + value);
        }
    }

    public int getMaxVariable() {
        return (this.options & 112) >> 4;
    }

    static boolean isTertiaryWithCaseBits(int options2) {
        return (options2 & 1536) == 512;
    }

    static int getTertiaryMask(int options2) {
        if (isTertiaryWithCaseBits(options2)) {
            return 65343;
        }
        return Collation.ONLY_TERTIARY_MASK;
    }

    static boolean sortsTertiaryUpperCaseFirst(int options2) {
        return (options2 & 1792) == 768;
    }

    public boolean dontCheckFCD() {
        return (this.options & 1) == 0;
    }

    /* access modifiers changed from: package-private */
    public boolean hasBackwardSecondary() {
        return (this.options & 2048) != 0;
    }

    public boolean isNumeric() {
        return (this.options & 2) != 0;
    }
}
