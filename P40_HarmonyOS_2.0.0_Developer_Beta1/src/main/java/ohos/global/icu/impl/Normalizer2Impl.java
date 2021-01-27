package ohos.global.icu.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import ohos.global.icu.impl.ICUBinary;
import ohos.global.icu.text.UTF16;
import ohos.global.icu.text.UnicodeSet;
import ohos.global.icu.util.CodePointMap;
import ohos.global.icu.util.CodePointTrie;
import ohos.global.icu.util.ICUUncheckedIOException;
import ohos.global.icu.util.MutableCodePointTrie;
import ohos.global.icu.util.VersionInfo;

public final class Normalizer2Impl {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int CANON_HAS_COMPOSITIONS = 1073741824;
    private static final int CANON_HAS_SET = 2097152;
    private static final int CANON_NOT_SEGMENT_STARTER = Integer.MIN_VALUE;
    private static final int CANON_VALUE_MASK = 2097151;
    public static final int COMP_1_LAST_TUPLE = 32768;
    public static final int COMP_1_TRAIL_LIMIT = 13312;
    public static final int COMP_1_TRAIL_MASK = 32766;
    public static final int COMP_1_TRAIL_SHIFT = 9;
    public static final int COMP_1_TRIPLE = 1;
    public static final int COMP_2_TRAIL_MASK = 65472;
    public static final int COMP_2_TRAIL_SHIFT = 6;
    private static final int DATA_FORMAT = 1316121906;
    public static final int DELTA_SHIFT = 3;
    public static final int DELTA_TCCC_0 = 0;
    public static final int DELTA_TCCC_1 = 2;
    public static final int DELTA_TCCC_GT_1 = 4;
    public static final int DELTA_TCCC_MASK = 6;
    public static final int HAS_COMP_BOUNDARY_AFTER = 1;
    public static final int INERT = 1;
    private static final IsAcceptable IS_ACCEPTABLE = new IsAcceptable();
    public static final int IX_COUNT = 20;
    public static final int IX_EXTRA_DATA_OFFSET = 1;
    public static final int IX_LIMIT_NO_NO = 12;
    public static final int IX_MIN_COMP_NO_MAYBE_CP = 9;
    public static final int IX_MIN_DECOMP_NO_CP = 8;
    public static final int IX_MIN_LCCC_CP = 18;
    public static final int IX_MIN_MAYBE_YES = 13;
    public static final int IX_MIN_NO_NO = 11;
    public static final int IX_MIN_NO_NO_COMP_BOUNDARY_BEFORE = 15;
    public static final int IX_MIN_NO_NO_COMP_NO_MAYBE_CC = 16;
    public static final int IX_MIN_NO_NO_EMPTY = 17;
    public static final int IX_MIN_YES_NO = 10;
    public static final int IX_MIN_YES_NO_MAPPINGS_ONLY = 14;
    public static final int IX_NORM_TRIE_OFFSET = 0;
    public static final int IX_RESERVED3_OFFSET = 3;
    public static final int IX_SMALL_FCD_OFFSET = 2;
    public static final int IX_TOTAL_SIZE = 7;
    public static final int JAMO_L = 2;
    public static final int JAMO_VT = 65024;
    public static final int MAPPING_HAS_CCC_LCCC_WORD = 128;
    public static final int MAPPING_HAS_RAW_MAPPING = 64;
    public static final int MAPPING_LENGTH_MASK = 31;
    public static final int MAX_DELTA = 64;
    public static final int MIN_NORMAL_MAYBE_YES = 64512;
    public static final int MIN_YES_YES_WITH_CC = 65026;
    public static final int OFFSET_SHIFT = 1;
    private static final CodePointMap.ValueFilter segmentStarterMapper = new CodePointMap.ValueFilter() {
        /* class ohos.global.icu.impl.Normalizer2Impl.AnonymousClass1 */

        public int apply(int i) {
            return Integer.MIN_VALUE & i;
        }
    };
    private CodePointTrie canonIterData;
    private ArrayList<UnicodeSet> canonStartSets;
    private int centerNoNoDelta;
    private VersionInfo dataVersion;
    private String extraData;
    private int limitNoNo;
    private String maybeYesCompositions;
    private int minCompNoMaybeCP;
    private int minDecompNoCP;
    private int minLcccCP;
    private int minMaybeYes;
    private int minNoNo;
    private int minNoNoCompBoundaryBefore;
    private int minNoNoCompNoMaybeCC;
    private int minNoNoEmpty;
    private int minYesNo;
    private int minYesNoMappingsOnly;
    private CodePointTrie.Fast16 normTrie;
    private byte[] smallFCD;

    public static int getCCFromNormalYesOrMaybe(int i) {
        return (i >> 1) & 255;
    }

    private static boolean isInert(int i) {
        return i == 1;
    }

    private static boolean isJamoL(int i) {
        return i == 2;
    }

    private static boolean isJamoVT(int i) {
        return i == 65024;
    }

    public static final class Hangul {
        public static final int HANGUL_BASE = 44032;
        public static final int HANGUL_COUNT = 11172;
        public static final int HANGUL_END = 55203;
        public static final int HANGUL_LIMIT = 55204;
        public static final int JAMO_L_BASE = 4352;
        public static final int JAMO_L_COUNT = 19;
        public static final int JAMO_L_END = 4370;
        public static final int JAMO_L_LIMIT = 4371;
        public static final int JAMO_T_BASE = 4519;
        public static final int JAMO_T_COUNT = 28;
        public static final int JAMO_T_END = 4546;
        public static final int JAMO_VT_COUNT = 588;
        public static final int JAMO_V_BASE = 4449;
        public static final int JAMO_V_COUNT = 21;
        public static final int JAMO_V_END = 4469;
        public static final int JAMO_V_LIMIT = 4470;

        public static boolean isHangul(int i) {
            return 44032 <= i && i < 55204;
        }

        public static boolean isJamo(int i) {
            return 4352 <= i && i <= 4546 && (i <= 4370 || ((4449 <= i && i <= 4469) || 4519 < i));
        }

        public static boolean isJamoL(int i) {
            return 4352 <= i && i < 4371;
        }

        public static boolean isJamoT(int i) {
            int i2 = i - 4519;
            return i2 > 0 && i2 < 28;
        }

        public static boolean isJamoV(int i) {
            return 4449 <= i && i < 4470;
        }

        public static boolean isHangulLV(int i) {
            int i2 = i - HANGUL_BASE;
            return i2 >= 0 && i2 < 11172 && i2 % 28 == 0;
        }

        public static int decompose(int i, Appendable appendable) {
            int i2 = i - HANGUL_BASE;
            try {
                int i3 = i2 % 28;
                int i4 = i2 / 28;
                appendable.append((char) ((i4 / 21) + JAMO_L_BASE));
                appendable.append((char) ((i4 % 21) + JAMO_V_BASE));
                if (i3 == 0) {
                    return 2;
                }
                appendable.append((char) (i3 + JAMO_T_BASE));
                return 3;
            } catch (IOException e) {
                throw new ICUUncheckedIOException(e);
            }
        }

        public static void getRawDecomposition(int i, Appendable appendable) {
            int i2 = i - HANGUL_BASE;
            try {
                int i3 = i2 % 28;
                if (i3 == 0) {
                    int i4 = i2 / 28;
                    appendable.append((char) ((i4 / 21) + JAMO_L_BASE));
                    appendable.append((char) ((i4 % 21) + JAMO_V_BASE));
                    return;
                }
                appendable.append((char) (i - i3));
                appendable.append((char) (i3 + JAMO_T_BASE));
            } catch (IOException e) {
                throw new ICUUncheckedIOException(e);
            }
        }
    }

    public static final class ReorderingBuffer implements Appendable {
        private final Appendable app;
        private final boolean appIsStringBuilder;
        private int codePointLimit;
        private int codePointStart;
        private final Normalizer2Impl impl;
        private int lastCC;
        private int reorderStart;
        private final StringBuilder str;

        public ReorderingBuffer(Normalizer2Impl normalizer2Impl, Appendable appendable, int i) {
            this.impl = normalizer2Impl;
            this.app = appendable;
            if (this.app instanceof StringBuilder) {
                this.appIsStringBuilder = true;
                this.str = (StringBuilder) appendable;
                this.str.ensureCapacity(i);
                this.reorderStart = 0;
                if (this.str.length() == 0) {
                    this.lastCC = 0;
                    return;
                }
                setIterator();
                this.lastCC = previousCC();
                if (this.lastCC > 1) {
                    do {
                    } while (previousCC() > 1);
                }
                this.reorderStart = this.codePointLimit;
                return;
            }
            this.appIsStringBuilder = false;
            this.str = new StringBuilder();
            this.reorderStart = 0;
            this.lastCC = 0;
        }

        public boolean isEmpty() {
            return this.str.length() == 0;
        }

        public int length() {
            return this.str.length();
        }

        public int getLastCC() {
            return this.lastCC;
        }

        public StringBuilder getStringBuilder() {
            return this.str;
        }

        public boolean equals(CharSequence charSequence, int i, int i2) {
            StringBuilder sb = this.str;
            return UTF16Plus.equal(sb, 0, sb.length(), charSequence, i, i2);
        }

        public void append(int i, int i2) {
            if (this.lastCC <= i2 || i2 == 0) {
                this.str.appendCodePoint(i);
                this.lastCC = i2;
                if (i2 <= 1) {
                    this.reorderStart = this.str.length();
                    return;
                }
                return;
            }
            insert(i, i2);
        }

        public void append(CharSequence charSequence, int i, int i2, boolean z, int i3, int i4) {
            int i5;
            if (i != i2) {
                if (this.lastCC <= i3 || i3 == 0) {
                    if (i4 <= 1) {
                        this.reorderStart = this.str.length() + (i2 - i);
                    } else if (i3 <= 1) {
                        this.reorderStart = this.str.length() + 1;
                    }
                    this.str.append(charSequence, i, i2);
                    this.lastCC = i4;
                    return;
                }
                int codePointAt = Character.codePointAt(charSequence, i);
                int charCount = i + Character.charCount(codePointAt);
                insert(codePointAt, i3);
                while (charCount < i2) {
                    int codePointAt2 = Character.codePointAt(charSequence, charCount);
                    charCount += Character.charCount(codePointAt2);
                    if (charCount >= i2) {
                        i5 = i4;
                    } else if (z) {
                        i5 = Normalizer2Impl.getCCFromYesOrMaybe(this.impl.getNorm16(codePointAt2));
                    } else {
                        Normalizer2Impl normalizer2Impl = this.impl;
                        i5 = normalizer2Impl.getCC(normalizer2Impl.getNorm16(codePointAt2));
                    }
                    append(codePointAt2, i5);
                }
            }
        }

        @Override // java.lang.Appendable
        public ReorderingBuffer append(char c) {
            this.str.append(c);
            this.lastCC = 0;
            this.reorderStart = this.str.length();
            return this;
        }

        public void appendZeroCC(int i) {
            this.str.appendCodePoint(i);
            this.lastCC = 0;
            this.reorderStart = this.str.length();
        }

        @Override // java.lang.Appendable
        public ReorderingBuffer append(CharSequence charSequence) {
            if (charSequence.length() != 0) {
                this.str.append(charSequence);
                this.lastCC = 0;
                this.reorderStart = this.str.length();
            }
            return this;
        }

        @Override // java.lang.Appendable
        public ReorderingBuffer append(CharSequence charSequence, int i, int i2) {
            if (i != i2) {
                this.str.append(charSequence, i, i2);
                this.lastCC = 0;
                this.reorderStart = this.str.length();
            }
            return this;
        }

        public void flush() {
            if (this.appIsStringBuilder) {
                this.reorderStart = this.str.length();
            } else {
                try {
                    this.app.append(this.str);
                    this.str.setLength(0);
                    this.reorderStart = 0;
                } catch (IOException e) {
                    throw new ICUUncheckedIOException(e);
                }
            }
            this.lastCC = 0;
        }

        public ReorderingBuffer flushAndAppendZeroCC(CharSequence charSequence, int i, int i2) {
            if (this.appIsStringBuilder) {
                this.str.append(charSequence, i, i2);
                this.reorderStart = this.str.length();
            } else {
                try {
                    this.app.append(this.str).append(charSequence, i, i2);
                    this.str.setLength(0);
                    this.reorderStart = 0;
                } catch (IOException e) {
                    throw new ICUUncheckedIOException(e);
                }
            }
            this.lastCC = 0;
            return this;
        }

        public void remove() {
            this.str.setLength(0);
            this.lastCC = 0;
            this.reorderStart = 0;
        }

        public void removeSuffix(int i) {
            int length = this.str.length();
            this.str.delete(length - i, length);
            this.lastCC = 0;
            this.reorderStart = this.str.length();
        }

        private void insert(int i, int i2) {
            setIterator();
            skipPrevious();
            do {
            } while (previousCC() > i2);
            if (i <= 65535) {
                this.str.insert(this.codePointLimit, (char) i);
                if (i2 <= 1) {
                    this.reorderStart = this.codePointLimit + 1;
                    return;
                }
                return;
            }
            this.str.insert(this.codePointLimit, Character.toChars(i));
            if (i2 <= 1) {
                this.reorderStart = this.codePointLimit + 2;
            }
        }

        private void setIterator() {
            this.codePointStart = this.str.length();
        }

        private void skipPrevious() {
            int i = this.codePointStart;
            this.codePointLimit = i;
            this.codePointStart = this.str.offsetByCodePoints(i, -1);
        }

        private int previousCC() {
            int i = this.codePointStart;
            this.codePointLimit = i;
            if (this.reorderStart >= i) {
                return 0;
            }
            int codePointBefore = this.str.codePointBefore(i);
            this.codePointStart -= Character.charCount(codePointBefore);
            return this.impl.getCCFromYesOrMaybeCP(codePointBefore);
        }
    }

    public static final class UTF16Plus {
        public static boolean isLeadSurrogate(int i) {
            return (i & -1024) == 55296;
        }

        public static boolean isSurrogate(int i) {
            return (i & -2048) == 55296;
        }

        public static boolean isSurrogateLead(int i) {
            return (i & 1024) == 0;
        }

        public static boolean isTrailSurrogate(int i) {
            return (i & -1024) == 56320;
        }

        public static boolean equal(CharSequence charSequence, CharSequence charSequence2) {
            if (charSequence == charSequence2) {
                return true;
            }
            int length = charSequence.length();
            if (length != charSequence2.length()) {
                return false;
            }
            for (int i = 0; i < length; i++) {
                if (charSequence.charAt(i) != charSequence2.charAt(i)) {
                    return false;
                }
            }
            return true;
        }

        public static boolean equal(CharSequence charSequence, int i, int i2, CharSequence charSequence2, int i3, int i4) {
            if (i2 - i != i4 - i3) {
                return false;
            }
            if (charSequence == charSequence2 && i == i3) {
                return true;
            }
            while (i < i2) {
                int i5 = i + 1;
                int i6 = i3 + 1;
                if (charSequence.charAt(i) != charSequence2.charAt(i3)) {
                    return false;
                }
                i = i5;
                i3 = i6;
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static final class IsAcceptable implements ICUBinary.Authenticate {
        private IsAcceptable() {
        }

        @Override // ohos.global.icu.impl.ICUBinary.Authenticate
        public boolean isDataVersionAcceptable(byte[] bArr) {
            return bArr[0] == 4;
        }
    }

    public Normalizer2Impl load(ByteBuffer byteBuffer) {
        try {
            this.dataVersion = ICUBinary.readHeaderAndDataVersion(byteBuffer, DATA_FORMAT, IS_ACCEPTABLE);
            int i = byteBuffer.getInt() / 4;
            if (i > 18) {
                int[] iArr = new int[i];
                iArr[0] = i * 4;
                for (int i2 = 1; i2 < i; i2++) {
                    iArr[i2] = byteBuffer.getInt();
                }
                this.minDecompNoCP = iArr[8];
                this.minCompNoMaybeCP = iArr[9];
                this.minLcccCP = iArr[18];
                this.minYesNo = iArr[10];
                this.minYesNoMappingsOnly = iArr[14];
                this.minNoNo = iArr[11];
                this.minNoNoCompBoundaryBefore = iArr[15];
                this.minNoNoCompNoMaybeCC = iArr[16];
                this.minNoNoEmpty = iArr[17];
                this.limitNoNo = iArr[12];
                this.minMaybeYes = iArr[13];
                this.centerNoNoDelta = ((this.minMaybeYes >> 3) - 64) - 1;
                int i3 = iArr[0];
                int i4 = iArr[1];
                int position = byteBuffer.position();
                this.normTrie = CodePointTrie.Fast16.fromBinary(byteBuffer);
                int position2 = byteBuffer.position() - position;
                int i5 = i4 - i3;
                if (position2 <= i5) {
                    ICUBinary.skipBytes(byteBuffer, i5 - position2);
                    int i6 = (iArr[2] - i4) / 2;
                    if (i6 != 0) {
                        this.maybeYesCompositions = ICUBinary.getString(byteBuffer, i6, 0);
                        this.extraData = this.maybeYesCompositions.substring((MIN_NORMAL_MAYBE_YES - this.minMaybeYes) >> 1);
                    }
                    this.smallFCD = new byte[256];
                    byteBuffer.get(this.smallFCD);
                    return this;
                }
                throw new ICUUncheckedIOException("Normalizer2 data: not enough bytes for normTrie");
            }
            throw new ICUUncheckedIOException("Normalizer2 data: not enough indexes");
        } catch (IOException e) {
            throw new ICUUncheckedIOException(e);
        }
    }

    public Normalizer2Impl load(String str) {
        return load(ICUBinary.getRequiredData(str));
    }

    public void addLcccChars(UnicodeSet unicodeSet) {
        CodePointMap.Range range = new CodePointMap.Range();
        int i = 0;
        while (this.normTrie.getRange(i, CodePointMap.RangeOption.FIXED_LEAD_SURROGATES, 1, (CodePointMap.ValueFilter) null, range)) {
            int end = range.getEnd();
            int value = range.getValue();
            if (value > 64512 && value != 65024) {
                unicodeSet.add(i, end);
            } else if (this.minNoNoCompNoMaybeCC <= value && value < this.limitNoNo && getFCD16(i) > 255) {
                unicodeSet.add(i, end);
            }
            i = end + 1;
        }
    }

    public void addPropertyStarts(UnicodeSet unicodeSet) {
        CodePointMap.Range range = new CodePointMap.Range();
        int i = 0;
        while (this.normTrie.getRange(i, CodePointMap.RangeOption.FIXED_LEAD_SURROGATES, 1, (CodePointMap.ValueFilter) null, range)) {
            int end = range.getEnd();
            int value = range.getValue();
            unicodeSet.add(i);
            if (i != end && isAlgorithmicNoNo(value) && (value & 6) > 2) {
                int fcd16 = getFCD16(i);
                while (true) {
                    i++;
                    if (i > end) {
                        break;
                    }
                    int fcd162 = getFCD16(i);
                    if (fcd162 != fcd16) {
                        unicodeSet.add(i);
                        fcd16 = fcd162;
                    }
                }
            }
            i = end + 1;
        }
        for (int i2 = Hangul.HANGUL_BASE; i2 < 55204; i2 += 28) {
            unicodeSet.add(i2);
            unicodeSet.add(i2 + 1);
        }
        unicodeSet.add((int) Hangul.HANGUL_LIMIT);
    }

    public void addCanonIterPropertyStarts(UnicodeSet unicodeSet) {
        ensureCanonIterData();
        CodePointMap.Range range = new CodePointMap.Range();
        for (int i = 0; this.canonIterData.getRange(i, segmentStarterMapper, range); i = range.getEnd() + 1) {
            unicodeSet.add(i);
        }
    }

    public synchronized Normalizer2Impl ensureCanonIterData() {
        int i;
        int i2;
        int i3;
        if (this.canonIterData == null) {
            int i4 = 0;
            MutableCodePointTrie mutableCodePointTrie = new MutableCodePointTrie(0, 0);
            this.canonStartSets = new ArrayList<>();
            CodePointMap.Range range = new CodePointMap.Range();
            while (this.normTrie.getRange(i4, CodePointMap.RangeOption.FIXED_LEAD_SURROGATES, 1, (CodePointMap.ValueFilter) null, range)) {
                int end = range.getEnd();
                int value = range.getValue();
                if (!isInert(value)) {
                    if (this.minYesNo > value || value >= this.minNoNo) {
                        while (i4 <= end) {
                            int i5 = mutableCodePointTrie.get(i4);
                            if (isMaybeOrNonZeroCC(value)) {
                                i = i5 | Integer.MIN_VALUE;
                                if (value < 64512) {
                                    i |= 1073741824;
                                }
                            } else if (value < this.minYesNo) {
                                i = i5 | 1073741824;
                            } else {
                                if (isDecompNoAlgorithmic(value)) {
                                    i3 = mapAlgorithmic(i4, value);
                                    i2 = getRawNorm16(i3);
                                } else {
                                    i3 = i4;
                                    i2 = value;
                                }
                                if (i2 > this.minYesNo) {
                                    int i6 = i2 >> 1;
                                    char charAt = this.extraData.charAt(i6);
                                    int i7 = charAt & 31;
                                    i = ((charAt & 128) == 0 || i4 != i3 || (this.extraData.charAt(i6 + -1) & 255) == 0) ? i5 : i5 | Integer.MIN_VALUE;
                                    if (i7 != 0) {
                                        int i8 = i6 + 1;
                                        int i9 = i7 + i8;
                                        int codePointAt = this.extraData.codePointAt(i8);
                                        addToStartSet(mutableCodePointTrie, i4, codePointAt);
                                        if (i2 >= this.minNoNo) {
                                            while (true) {
                                                i8 += Character.charCount(codePointAt);
                                                if (i8 >= i9) {
                                                    break;
                                                }
                                                codePointAt = this.extraData.codePointAt(i8);
                                                int i10 = mutableCodePointTrie.get(codePointAt);
                                                if ((i10 & Integer.MIN_VALUE) == 0) {
                                                    mutableCodePointTrie.set(codePointAt, i10 | Integer.MIN_VALUE);
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    addToStartSet(mutableCodePointTrie, i4, i3);
                                    i = i5;
                                }
                            }
                            if (i != i5) {
                                mutableCodePointTrie.set(i4, i);
                            }
                            i4++;
                        }
                    }
                }
                i4 = end + 1;
            }
            this.canonIterData = mutableCodePointTrie.buildImmutable(CodePointTrie.Type.SMALL, CodePointTrie.ValueWidth.BITS_32);
        }
        return this;
    }

    public int getNorm16(int i) {
        if (UTF16Plus.isLeadSurrogate(i)) {
            return 1;
        }
        return this.normTrie.get(i);
    }

    public int getRawNorm16(int i) {
        return this.normTrie.get(i);
    }

    public int getCompQuickCheck(int i) {
        if (i < this.minNoNo || 65026 <= i) {
            return 1;
        }
        return this.minMaybeYes <= i ? 2 : 0;
    }

    public boolean isAlgorithmicNoNo(int i) {
        return this.limitNoNo <= i && i < this.minMaybeYes;
    }

    public boolean isCompNo(int i) {
        return this.minNoNo <= i && i < this.minMaybeYes;
    }

    public boolean isDecompYes(int i) {
        return i < this.minYesNo || this.minMaybeYes <= i;
    }

    public int getCC(int i) {
        if (i >= 64512) {
            return getCCFromNormalYesOrMaybe(i);
        }
        if (i < this.minNoNo || this.limitNoNo <= i) {
            return 0;
        }
        return getCCFromNoNo(i);
    }

    public static int getCCFromYesOrMaybe(int i) {
        if (i >= 64512) {
            return getCCFromNormalYesOrMaybe(i);
        }
        return 0;
    }

    public int getCCFromYesOrMaybeCP(int i) {
        if (i < this.minCompNoMaybeCP) {
            return 0;
        }
        return getCCFromYesOrMaybe(getNorm16(i));
    }

    public int getFCD16(int i) {
        if (i < this.minDecompNoCP) {
            return 0;
        }
        if (i > 65535 || singleLeadMightHaveNonZeroFCD16(i)) {
            return getFCD16FromNormData(i);
        }
        return 0;
    }

    public boolean singleLeadMightHaveNonZeroFCD16(int i) {
        byte b = this.smallFCD[i >> 8];
        if (b == 0) {
            return false;
        }
        return ((b >> ((i >> 5) & 7)) & 1) != 0;
    }

    public int getFCD16FromNormData(int i) {
        int norm16 = getNorm16(i);
        if (norm16 >= this.limitNoNo) {
            if (norm16 >= 64512) {
                int cCFromNormalYesOrMaybe = getCCFromNormalYesOrMaybe(norm16);
                return cCFromNormalYesOrMaybe | (cCFromNormalYesOrMaybe << 8);
            } else if (norm16 >= this.minMaybeYes) {
                return 0;
            } else {
                int i2 = norm16 & 6;
                if (i2 <= 2) {
                    return i2 >> 1;
                }
                norm16 = getRawNorm16(mapAlgorithmic(i, norm16));
            }
        }
        if (norm16 <= this.minYesNo || isHangulLVT(norm16)) {
            return 0;
        }
        int i3 = norm16 >> 1;
        char charAt = this.extraData.charAt(i3);
        int i4 = charAt >> '\b';
        return (charAt & 128) != 0 ? i4 | (this.extraData.charAt(i3 - 1) & 65280) : i4;
    }

    public String getDecomposition(int i) {
        int i2;
        int i3;
        if (i >= this.minDecompNoCP) {
            int norm16 = getNorm16(i);
            if (!isMaybeOrNonZeroCC(norm16)) {
                if (isDecompNoAlgorithmic(norm16)) {
                    i2 = mapAlgorithmic(i, norm16);
                    norm16 = getRawNorm16(i2);
                    i3 = i2;
                } else {
                    i3 = i;
                    i2 = -1;
                }
                if (norm16 < this.minYesNo) {
                    if (i2 < 0) {
                        return null;
                    }
                    return UTF16.valueOf(i2);
                } else if (isHangulLV(norm16) || isHangulLVT(norm16)) {
                    StringBuilder sb = new StringBuilder();
                    Hangul.decompose(i3, sb);
                    return sb.toString();
                } else {
                    int i4 = norm16 >> 1;
                    int i5 = i4 + 1;
                    return this.extraData.substring(i5, (this.extraData.charAt(i4) & 31) + i5);
                }
            }
        }
        return null;
    }

    public String getRawDecomposition(int i) {
        if (i < this.minDecompNoCP) {
            return null;
        }
        int norm16 = getNorm16(i);
        if (isDecompYes(norm16)) {
            return null;
        }
        if (isHangulLV(norm16) || isHangulLVT(norm16)) {
            StringBuilder sb = new StringBuilder();
            Hangul.getRawDecomposition(i, sb);
            return sb.toString();
        } else if (isDecompNoAlgorithmic(norm16)) {
            return UTF16.valueOf(mapAlgorithmic(i, norm16));
        } else {
            int i2 = norm16 >> 1;
            char charAt = this.extraData.charAt(i2);
            int i3 = charAt & 31;
            if ((charAt & '@') != 0) {
                int i4 = (i2 - ((charAt >> 7) & 1)) - 1;
                char charAt2 = this.extraData.charAt(i4);
                if (charAt2 <= 31) {
                    return this.extraData.substring(i4 - charAt2, i4);
                }
                StringBuilder sb2 = new StringBuilder(i3 - 1);
                sb2.append(charAt2);
                int i5 = i2 + 3;
                sb2.append((CharSequence) this.extraData, i5, (i3 + i5) - 2);
                return sb2.toString();
            }
            int i6 = i2 + 1;
            return this.extraData.substring(i6, i3 + i6);
        }
    }

    public boolean isCanonSegmentStarter(int i) {
        return this.canonIterData.get(i) >= 0;
    }

    public boolean getCanonStartSet(int i, UnicodeSet unicodeSet) {
        int i2 = this.canonIterData.get(i) & Integer.MAX_VALUE;
        if (i2 == 0) {
            return false;
        }
        unicodeSet.clear();
        int i3 = CANON_VALUE_MASK & i2;
        if ((2097152 & i2) != 0) {
            unicodeSet.addAll(this.canonStartSets.get(i3));
        } else if (i3 != 0) {
            unicodeSet.add(i3);
        }
        if ((i2 & 1073741824) != 0) {
            int rawNorm16 = getRawNorm16(i);
            if (rawNorm16 == 2) {
                int i4 = ((i - 4352) * Hangul.JAMO_VT_COUNT) + Hangul.HANGUL_BASE;
                unicodeSet.add(i4, (i4 + Hangul.JAMO_VT_COUNT) - 1);
            } else {
                addComposites(getCompositionsList(rawNorm16), unicodeSet);
            }
        }
        return true;
    }

    public Appendable decompose(CharSequence charSequence, StringBuilder sb) {
        decompose(charSequence, 0, charSequence.length(), sb, charSequence.length());
        return sb;
    }

    public void decompose(CharSequence charSequence, int i, int i2, StringBuilder sb, int i3) {
        if (i3 < 0) {
            i3 = i2 - i;
        }
        sb.setLength(0);
        decompose(charSequence, i, i2, new ReorderingBuffer(this, sb, i3));
    }

    public int decompose(CharSequence charSequence, int i, int i2, ReorderingBuffer reorderingBuffer) {
        int cCFromYesOrMaybe;
        int i3 = this.minDecompNoCP;
        int i4 = i;
        int i5 = 0;
        int i6 = 0;
        int i7 = 0;
        while (true) {
            int i8 = i6;
            int i9 = i5;
            int i10 = i;
            while (i10 != i2) {
                i9 = charSequence.charAt(i10);
                if (i9 >= i3) {
                    i8 = this.normTrie.bmpGet(i9);
                    if (!isMostDecompYesAndZeroCC(i8)) {
                        if (!UTF16Plus.isLeadSurrogate(i9)) {
                            break;
                        }
                        int i11 = i10 + 1;
                        if (i11 != i2) {
                            char charAt = charSequence.charAt(i11);
                            if (Character.isLowSurrogate(charAt)) {
                                i9 = Character.toCodePoint((char) i9, charAt);
                                i8 = this.normTrie.suppGet(i9);
                                if (!isMostDecompYesAndZeroCC(i8)) {
                                    break;
                                }
                                i10 += 2;
                            }
                        }
                        i10 = i11;
                    }
                }
                i10++;
            }
            if (i10 != i) {
                if (reorderingBuffer != null) {
                    reorderingBuffer.flushAndAppendZeroCC(charSequence, i, i10);
                } else {
                    i7 = 0;
                    i4 = i10;
                }
            }
            if (i10 == i2) {
                return i10;
            }
            i = Character.charCount(i9) + i10;
            if (reorderingBuffer != null) {
                decompose(i9, i8, reorderingBuffer);
            } else if (!isDecompYes(i8) || (i7 > (cCFromYesOrMaybe = getCCFromYesOrMaybe(i8)) && cCFromYesOrMaybe != 0)) {
                break;
            } else {
                if (cCFromYesOrMaybe <= 1) {
                    i4 = i;
                }
                i7 = cCFromYesOrMaybe;
            }
            i5 = i9;
            i6 = i8;
        }
        return i4;
    }

    public void decomposeAndAppend(CharSequence charSequence, boolean z, ReorderingBuffer reorderingBuffer) {
        int i;
        int length = charSequence.length();
        if (length != 0) {
            int i2 = 0;
            if (z) {
                decompose(charSequence, 0, length, reorderingBuffer);
                return;
            }
            int codePointAt = Character.codePointAt(charSequence, 0);
            int cc = getCC(getNorm16(codePointAt));
            int i3 = codePointAt;
            int i4 = cc;
            int i5 = i4;
            while (true) {
                if (i4 == 0) {
                    i = i5;
                    break;
                }
                i2 += Character.charCount(i3);
                if (i2 >= length) {
                    i = i4;
                    break;
                }
                i3 = Character.codePointAt(charSequence, i2);
                i5 = i4;
                i4 = getCC(getNorm16(i3));
            }
            reorderingBuffer.append(charSequence, 0, i2, false, cc, i);
            reorderingBuffer.append(charSequence, i2, length);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0054, code lost:
        if (isCompYesAndZeroCC(r4) == false) goto L_0x0056;
     */
    /* JADX WARNING: Removed duplicated region for block: B:113:0x0191  */
    /* JADX WARNING: Removed duplicated region for block: B:120:0x01be  */
    public boolean compose(CharSequence charSequence, int i, int i2, boolean z, boolean z2, ReorderingBuffer reorderingBuffer) {
        int codePointBefore;
        int i3;
        int i4 = this.minCompNoMaybeCP;
        int i5 = i;
        while (true) {
            int i6 = i5;
            while (i5 != i2) {
                int charAt = charSequence.charAt(i5);
                if (charAt >= i4) {
                    int bmpGet = this.normTrie.bmpGet(charAt);
                    if (!isCompYesAndZeroCC(bmpGet)) {
                        int i7 = i5 + 1;
                        if (UTF16Plus.isLeadSurrogate(charAt)) {
                            if (i7 != i2) {
                                char charAt2 = charSequence.charAt(i7);
                                if (Character.isLowSurrogate(charAt2)) {
                                    i7++;
                                    charAt = Character.toCodePoint((char) charAt, charAt2);
                                    bmpGet = this.normTrie.suppGet(charAt);
                                }
                            }
                            i5 = i7;
                        }
                        if (!isMaybeOrNonZeroCC(bmpGet)) {
                            if (!z2) {
                                return false;
                            }
                            if (isDecompNoAlgorithmic(bmpGet)) {
                                if (norm16HasCompBoundaryAfter(bmpGet, z) || hasCompBoundaryBefore(charSequence, i7, i2)) {
                                    if (i6 != i5) {
                                        reorderingBuffer.append(charSequence, i6, i5);
                                    }
                                    reorderingBuffer.append(mapAlgorithmic(charAt, bmpGet), 0);
                                }
                                if (i6 != i5 && !norm16HasCompBoundaryBefore(bmpGet)) {
                                    codePointBefore = Character.codePointBefore(charSequence, i5);
                                    if (!norm16HasCompBoundaryAfter(this.normTrie.get(codePointBefore), z)) {
                                        i5 -= Character.charCount(codePointBefore);
                                    }
                                }
                                if (z2 && i6 != i5) {
                                    reorderingBuffer.append(charSequence, i6, i5);
                                }
                                int length = reorderingBuffer.length();
                                decomposeShort(charSequence, i5, i7, false, z, reorderingBuffer);
                                i6 = decomposeShort(charSequence, i7, i2, true, z, reorderingBuffer);
                                recompose(reorderingBuffer, length, z);
                                if (!z2) {
                                    if (!reorderingBuffer.equals(charSequence, i5, i6)) {
                                        return false;
                                    }
                                    reorderingBuffer.remove();
                                }
                                i5 = i6;
                            } else if (bmpGet < this.minNoNoCompBoundaryBefore) {
                                if (norm16HasCompBoundaryAfter(bmpGet, z) || hasCompBoundaryBefore(charSequence, i7, i2)) {
                                    if (i6 != i5) {
                                        reorderingBuffer.append(charSequence, i6, i5);
                                    }
                                    int i8 = bmpGet >> 1;
                                    int i9 = i8 + 1;
                                    reorderingBuffer.append((CharSequence) this.extraData, i9, (this.extraData.charAt(i8) & 31) + i9);
                                }
                                codePointBefore = Character.codePointBefore(charSequence, i5);
                                if (!norm16HasCompBoundaryAfter(this.normTrie.get(codePointBefore), z)) {
                                }
                                reorderingBuffer.append(charSequence, i6, i5);
                                int length2 = reorderingBuffer.length();
                                decomposeShort(charSequence, i5, i7, false, z, reorderingBuffer);
                                i6 = decomposeShort(charSequence, i7, i2, true, z, reorderingBuffer);
                                recompose(reorderingBuffer, length2, z);
                                if (!z2) {
                                }
                                i5 = i6;
                            } else {
                                if (bmpGet >= this.minNoNoEmpty && (hasCompBoundaryBefore(charSequence, i7, i2) || hasCompBoundaryAfter(charSequence, i6, i5, z))) {
                                    if (i6 != i5) {
                                        reorderingBuffer.append(charSequence, i6, i5);
                                    }
                                }
                                codePointBefore = Character.codePointBefore(charSequence, i5);
                                if (!norm16HasCompBoundaryAfter(this.normTrie.get(codePointBefore), z)) {
                                }
                                reorderingBuffer.append(charSequence, i6, i5);
                                int length22 = reorderingBuffer.length();
                                decomposeShort(charSequence, i5, i7, false, z, reorderingBuffer);
                                i6 = decomposeShort(charSequence, i7, i2, true, z, reorderingBuffer);
                                recompose(reorderingBuffer, length22, z);
                                if (!z2) {
                                }
                                i5 = i6;
                            }
                        } else if (!isJamoVT(bmpGet) || i6 == i5) {
                            if (bmpGet > 65024) {
                                int cCFromNormalYesOrMaybe = getCCFromNormalYesOrMaybe(bmpGet);
                                if (!z || getPreviousTrailCC(charSequence, i6, i5) <= cCFromNormalYesOrMaybe) {
                                    while (i7 != i2) {
                                        int codePointAt = Character.codePointAt(charSequence, i7);
                                        int i10 = this.normTrie.get(codePointAt);
                                        if (i10 >= 65026) {
                                            int cCFromNormalYesOrMaybe2 = getCCFromNormalYesOrMaybe(i10);
                                            if (cCFromNormalYesOrMaybe <= cCFromNormalYesOrMaybe2) {
                                                i7 += Character.charCount(codePointAt);
                                                cCFromNormalYesOrMaybe = cCFromNormalYesOrMaybe2;
                                            } else if (!z2) {
                                                return false;
                                            }
                                        }
                                        if (norm16HasCompBoundaryBefore(i10)) {
                                            if (isCompYesAndZeroCC(i10)) {
                                                i5 = Character.charCount(codePointAt) + i7;
                                            }
                                            i5 = i7;
                                        }
                                    }
                                    if (z2) {
                                        reorderingBuffer.append(charSequence, i6, i2);
                                    }
                                    return true;
                                } else if (!z2) {
                                    return false;
                                }
                            }
                            codePointBefore = Character.codePointBefore(charSequence, i5);
                            if (!norm16HasCompBoundaryAfter(this.normTrie.get(codePointBefore), z)) {
                            }
                            reorderingBuffer.append(charSequence, i6, i5);
                            int length222 = reorderingBuffer.length();
                            decomposeShort(charSequence, i5, i7, false, z, reorderingBuffer);
                            i6 = decomposeShort(charSequence, i7, i2, true, z, reorderingBuffer);
                            recompose(reorderingBuffer, length222, z);
                            if (!z2) {
                            }
                            i5 = i6;
                        } else {
                            char charAt3 = charSequence.charAt(i5 - 1);
                            if (charAt < 4519) {
                                char c = (char) (charAt3 - 4352);
                                if (c < 19) {
                                    if (!z2) {
                                        return false;
                                    }
                                    if (i7 == i2 || charSequence.charAt(i7) - 4519 <= 0 || i3 >= 28) {
                                        i3 = hasCompBoundaryBefore(charSequence, i7, i2) ? 0 : -1;
                                    } else {
                                        i7++;
                                    }
                                    if (i3 >= 0) {
                                        int i11 = (((c * 21) + (charAt - 4449)) * 28) + Hangul.HANGUL_BASE + i3;
                                        int i12 = i5 - 1;
                                        if (i6 != i12) {
                                            reorderingBuffer.append(charSequence, i6, i12);
                                        }
                                        reorderingBuffer.append((char) i11);
                                    }
                                }
                                codePointBefore = Character.codePointBefore(charSequence, i5);
                                if (!norm16HasCompBoundaryAfter(this.normTrie.get(codePointBefore), z)) {
                                }
                                reorderingBuffer.append(charSequence, i6, i5);
                                int length2222 = reorderingBuffer.length();
                                decomposeShort(charSequence, i5, i7, false, z, reorderingBuffer);
                                i6 = decomposeShort(charSequence, i7, i2, true, z, reorderingBuffer);
                                recompose(reorderingBuffer, length2222, z);
                                if (!z2) {
                                }
                                i5 = i6;
                            } else {
                                if (Hangul.isHangulLV(charAt3)) {
                                    if (!z2) {
                                        return false;
                                    }
                                    int i13 = (charAt3 + charAt) - Hangul.JAMO_T_BASE;
                                    int i14 = i5 - 1;
                                    if (i6 != i14) {
                                        reorderingBuffer.append(charSequence, i6, i14);
                                    }
                                    reorderingBuffer.append((char) i13);
                                }
                                codePointBefore = Character.codePointBefore(charSequence, i5);
                                if (!norm16HasCompBoundaryAfter(this.normTrie.get(codePointBefore), z)) {
                                }
                                reorderingBuffer.append(charSequence, i6, i5);
                                int length22222 = reorderingBuffer.length();
                                decomposeShort(charSequence, i5, i7, false, z, reorderingBuffer);
                                i6 = decomposeShort(charSequence, i7, i2, true, z, reorderingBuffer);
                                recompose(reorderingBuffer, length22222, z);
                                if (!z2) {
                                }
                                i5 = i6;
                            }
                        }
                        i5 = i7;
                    }
                }
                i5++;
            }
            if (i6 != i2 && z2) {
                reorderingBuffer.append(charSequence, i6, i2);
            }
            return true;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0045, code lost:
        if (isCompYesAndZeroCC(r5) == false) goto L_0x0047;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:5:0x000a, code lost:
        return r8 | r1;
     */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x006c  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x00b9 A[EDGE_INSN: B:57:0x00b9->B:49:0x00b9 ?: BREAK  , SYNTHETIC] */
    public int composeQuickCheck(CharSequence charSequence, int i, int i2, boolean z, boolean z2) {
        int i3;
        int i4;
        int codePointAt;
        int cCFromYesOrMaybe;
        int i5 = this.minCompNoMaybeCP;
        int i6 = 0;
        int i7 = i;
        loop0:
        while (true) {
            if (i == i2) {
                i3 = i << 1;
                break;
            }
            char charAt = charSequence.charAt(i);
            if (charAt >= i5) {
                int bmpGet = this.normTrie.bmpGet(charAt);
                if (!isCompYesAndZeroCC(bmpGet)) {
                    int i8 = i + 1;
                    if (UTF16Plus.isLeadSurrogate(charAt)) {
                        if (i8 != i2) {
                            char charAt2 = charSequence.charAt(i8);
                            if (Character.isLowSurrogate(charAt2)) {
                                i8++;
                                bmpGet = this.normTrie.suppGet(Character.toCodePoint((char) charAt, charAt2));
                            }
                        }
                        i = i8;
                    }
                    if (i7 != i) {
                        if (!norm16HasCompBoundaryBefore(bmpGet)) {
                            int codePointBefore = Character.codePointBefore(charSequence, i);
                            i4 = getNorm16(codePointBefore);
                            if (!norm16HasCompBoundaryAfter(i4, z)) {
                                i7 = i - Character.charCount(codePointBefore);
                                if (isMaybeOrNonZeroCC(bmpGet)) {
                                    break;
                                }
                                int cCFromYesOrMaybe2 = getCCFromYesOrMaybe(bmpGet);
                                if (z && cCFromYesOrMaybe2 != 0 && getTrailCCFromCompYesAndZeroCC(i4) > cCFromYesOrMaybe2) {
                                    break;
                                }
                                while (true) {
                                    if (bmpGet < 65026) {
                                        if (z2) {
                                            return i7 << 1;
                                        }
                                        i6 = 1;
                                    }
                                    if (i8 == i2) {
                                        i3 = i8 << 1;
                                        break loop0;
                                    }
                                    codePointAt = Character.codePointAt(charSequence, i8);
                                    bmpGet = getNorm16(codePointAt);
                                    if (!isMaybeOrNonZeroCC(bmpGet) || (cCFromYesOrMaybe2 > (cCFromYesOrMaybe = getCCFromYesOrMaybe(bmpGet)) && cCFromYesOrMaybe != 0)) {
                                        break;
                                    }
                                    i8 += Character.charCount(codePointAt);
                                    cCFromYesOrMaybe2 = cCFromYesOrMaybe;
                                }
                                if (!isCompYesAndZeroCC(bmpGet)) {
                                    break;
                                }
                                i = Character.charCount(codePointAt) + i8;
                                i7 = i8;
                            }
                        }
                        i7 = i;
                    }
                    i4 = 1;
                    if (isMaybeOrNonZeroCC(bmpGet)) {
                    }
                }
            }
            i++;
        }
        return i7 << 1;
    }

    public void composeAndAppend(CharSequence charSequence, boolean z, boolean z2, ReorderingBuffer reorderingBuffer) {
        int i;
        int findNextCompBoundary;
        int length = charSequence.length();
        if (reorderingBuffer.isEmpty() || (findNextCompBoundary = findNextCompBoundary(charSequence, 0, length, z2)) == 0) {
            i = 0;
        } else {
            int findPreviousCompBoundary = findPreviousCompBoundary(reorderingBuffer.getStringBuilder(), reorderingBuffer.length(), z2);
            StringBuilder sb = new StringBuilder((reorderingBuffer.length() - findPreviousCompBoundary) + findNextCompBoundary + 16);
            sb.append((CharSequence) reorderingBuffer.getStringBuilder(), findPreviousCompBoundary, reorderingBuffer.length());
            reorderingBuffer.removeSuffix(reorderingBuffer.length() - findPreviousCompBoundary);
            sb.append(charSequence, 0, findNextCompBoundary);
            compose(sb, 0, sb.length(), z2, true, reorderingBuffer);
            i = findNextCompBoundary;
        }
        if (z) {
            compose(charSequence, i, length, z2, true, reorderingBuffer);
        } else {
            reorderingBuffer.append(charSequence, i, length);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:49:0x00c1  */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x00d1  */
    public int makeFCD(CharSequence charSequence, int i, int i2, ReorderingBuffer reorderingBuffer) {
        int i3;
        int i4;
        int i5;
        int i6;
        int i7 = i;
        int i8 = i7;
        int i9 = 0;
        int i10 = 0;
        int i11 = 0;
        while (true) {
            int i12 = i11;
            int i13 = i10;
            int i14 = i9;
            i3 = i7;
            while (i3 != i2) {
                i13 = charSequence.charAt(i3);
                if (i13 >= this.minLcccCP) {
                    if (singleLeadMightHaveNonZeroFCD16(i13)) {
                        if (UTF16Plus.isLeadSurrogate(i13) && (i6 = i3 + 1) != i2) {
                            char charAt = charSequence.charAt(i6);
                            if (Character.isLowSurrogate(charAt)) {
                                i13 = Character.toCodePoint((char) i13, charAt);
                            }
                        }
                        i12 = getFCD16FromNormData(i13);
                        if (i12 > 255) {
                            break;
                        }
                        i3 += Character.charCount(i13);
                        i14 = i12;
                    } else {
                        i3++;
                        i14 = 0;
                    }
                } else {
                    i14 = ~i13;
                    i3++;
                }
            }
            if (i3 == i7) {
                if (i3 == i2) {
                    break;
                }
                int charCount = i3 + Character.charCount(i13);
                if ((i14 & 255) > (i12 >> 8)) {
                }
            } else if (i3 != i2) {
                if (i14 < 0) {
                    int i15 = ~i14;
                    if (i15 < this.minDecompNoCP) {
                        i4 = i3;
                        i5 = 0;
                    } else {
                        i5 = getFCD16FromNormData(i15);
                        i4 = i5 > 1 ? i3 - 1 : i3;
                    }
                    i14 = i5;
                    i8 = i4;
                } else {
                    i8 = i3 - 1;
                    if (Character.isLowSurrogate(charSequence.charAt(i8)) && i7 < i8 && Character.isHighSurrogate(charSequence.charAt(i8 - 1))) {
                        i8--;
                        i14 = getFCD16FromNormData(Character.toCodePoint(charSequence.charAt(i8), charSequence.charAt(i8 + 1)));
                    }
                    if (i14 <= 1) {
                        i8 = i3;
                    }
                }
                if (reorderingBuffer != null) {
                    reorderingBuffer.flushAndAppendZeroCC(charSequence, i7, i8);
                    reorderingBuffer.append(charSequence, i8, i3);
                }
                i7 = i3;
                int charCount2 = i3 + Character.charCount(i13);
                if ((i14 & 255) > (i12 >> 8)) {
                    if ((i12 & 255) <= 1) {
                        i8 = charCount2;
                    }
                    if (reorderingBuffer != null) {
                        reorderingBuffer.appendZeroCC(i13);
                    }
                    i7 = charCount2;
                    i10 = i13;
                    i9 = i12;
                    i11 = i9;
                } else if (reorderingBuffer == null) {
                    return i8;
                } else {
                    reorderingBuffer.removeSuffix(i7 - i8);
                    int findNextFCDBoundary = findNextFCDBoundary(charSequence, charCount2, i2);
                    decomposeShort(charSequence, i8, findNextFCDBoundary, false, false, reorderingBuffer);
                    i9 = 0;
                    i10 = i13;
                    i11 = i12;
                    i7 = findNextFCDBoundary;
                    i8 = i7;
                }
            } else if (reorderingBuffer != null) {
                reorderingBuffer.flushAndAppendZeroCC(charSequence, i7, i3);
            }
        }
        return i3;
    }

    public void makeFCDAndAppend(CharSequence charSequence, boolean z, ReorderingBuffer reorderingBuffer) {
        int i;
        int length = charSequence.length();
        if (reorderingBuffer.isEmpty() || (i = findNextFCDBoundary(charSequence, 0, length)) == 0) {
            i = 0;
        } else {
            int findPreviousFCDBoundary = findPreviousFCDBoundary(reorderingBuffer.getStringBuilder(), reorderingBuffer.length());
            StringBuilder sb = new StringBuilder((reorderingBuffer.length() - findPreviousFCDBoundary) + i + 16);
            sb.append((CharSequence) reorderingBuffer.getStringBuilder(), findPreviousFCDBoundary, reorderingBuffer.length());
            reorderingBuffer.removeSuffix(reorderingBuffer.length() - findPreviousFCDBoundary);
            sb.append(charSequence, 0, i);
            makeFCD(sb, 0, sb.length(), reorderingBuffer);
        }
        if (z) {
            makeFCD(charSequence, i, length, reorderingBuffer);
        } else {
            reorderingBuffer.append(charSequence, i, length);
        }
    }

    public boolean hasDecompBoundaryBefore(int i) {
        return i < this.minLcccCP || (i <= 65535 && !singleLeadMightHaveNonZeroFCD16(i)) || norm16HasDecompBoundaryBefore(getNorm16(i));
    }

    public boolean norm16HasDecompBoundaryBefore(int i) {
        if (i < this.minNoNoCompNoMaybeCC) {
            return true;
        }
        if (i < this.limitNoNo) {
            int i2 = i >> 1;
            if ((this.extraData.charAt(i2) & 128) == 0 || (this.extraData.charAt(i2 - 1) & 65280) == 0) {
                return true;
            }
            return false;
        } else if (i <= 64512 || i == 65024) {
            return true;
        } else {
            return false;
        }
    }

    public boolean hasDecompBoundaryAfter(int i) {
        if (i < this.minDecompNoCP) {
            return true;
        }
        if (i > 65535 || singleLeadMightHaveNonZeroFCD16(i)) {
            return norm16HasDecompBoundaryAfter(getNorm16(i));
        }
        return true;
    }

    public boolean norm16HasDecompBoundaryAfter(int i) {
        if (i <= this.minYesNo || isHangulLVT(i)) {
            return true;
        }
        if (i < this.limitNoNo) {
            int i2 = i >> 1;
            char charAt = this.extraData.charAt(i2);
            if (charAt > 511) {
                return false;
            }
            if (charAt <= 255 || (charAt & 128) == 0 || (this.extraData.charAt(i2 - 1) & 65280) == 0) {
                return true;
            }
            return false;
        } else if (isMaybeOrNonZeroCC(i)) {
            if (i <= 64512 || i == 65024) {
                return true;
            }
            return false;
        } else if ((i & 6) <= 2) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isDecompInert(int i) {
        return isDecompYesAndZeroCC(getNorm16(i));
    }

    public boolean hasCompBoundaryBefore(int i) {
        return i < this.minCompNoMaybeCP || norm16HasCompBoundaryBefore(getNorm16(i));
    }

    public boolean hasCompBoundaryAfter(int i, boolean z) {
        return norm16HasCompBoundaryAfter(getNorm16(i), z);
    }

    public boolean isCompInert(int i, boolean z) {
        int norm16 = getNorm16(i);
        if (!isCompYesAndZeroCC(norm16) || (norm16 & 1) == 0 || (z && !isInert(norm16) && this.extraData.charAt(norm16 >> 1) > 511)) {
            return false;
        }
        return true;
    }

    public boolean hasFCDBoundaryBefore(int i) {
        return hasDecompBoundaryBefore(i);
    }

    public boolean hasFCDBoundaryAfter(int i) {
        return hasDecompBoundaryAfter(i);
    }

    public boolean isFCDInert(int i) {
        return getFCD16(i) <= 1;
    }

    private boolean isMaybe(int i) {
        return this.minMaybeYes <= i && i <= 65024;
    }

    private boolean isMaybeOrNonZeroCC(int i) {
        return i >= this.minMaybeYes;
    }

    private int hangulLVT() {
        return this.minYesNoMappingsOnly | 1;
    }

    private boolean isHangulLV(int i) {
        return i == this.minYesNo;
    }

    private boolean isHangulLVT(int i) {
        return i == hangulLVT();
    }

    private boolean isCompYesAndZeroCC(int i) {
        return i < this.minNoNo;
    }

    private boolean isDecompYesAndZeroCC(int i) {
        return i < this.minYesNo || i == 65024 || (this.minMaybeYes <= i && i <= 64512);
    }

    private boolean isMostDecompYesAndZeroCC(int i) {
        return i < this.minYesNo || i == 64512 || i == 65024;
    }

    private boolean isDecompNoAlgorithmic(int i) {
        return i >= this.limitNoNo;
    }

    private int getCCFromNoNo(int i) {
        int i2 = i >> 1;
        if ((this.extraData.charAt(i2) & 128) != 0) {
            return this.extraData.charAt(i2 - 1) & 255;
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int getTrailCCFromCompYesAndZeroCC(int i) {
        if (i <= this.minYesNo) {
            return 0;
        }
        return this.extraData.charAt(i >> 1) >> '\b';
    }

    private int mapAlgorithmic(int i, int i2) {
        return (i + (i2 >> 3)) - this.centerNoNoDelta;
    }

    private int getCompositionsListForDecompYes(int i) {
        if (i < 2 || 64512 <= i) {
            return -1;
        }
        int i2 = i - this.minMaybeYes;
        if (i2 < 0) {
            i2 += MIN_NORMAL_MAYBE_YES;
        }
        return i2 >> 1;
    }

    private int getCompositionsListForComposite(int i) {
        int i2 = ((MIN_NORMAL_MAYBE_YES - this.minMaybeYes) + i) >> 1;
        return i2 + 1 + (this.maybeYesCompositions.charAt(i2) & 31);
    }

    private int getCompositionsListForMaybe(int i) {
        return (i - this.minMaybeYes) >> 1;
    }

    private int getCompositionsList(int i) {
        if (isDecompYes(i)) {
            return getCompositionsListForDecompYes(i);
        }
        return getCompositionsListForComposite(i);
    }

    private int decomposeShort(CharSequence charSequence, int i, int i2, boolean z, boolean z2, ReorderingBuffer reorderingBuffer) {
        while (i < i2) {
            int codePointAt = Character.codePointAt(charSequence, i);
            if (z && codePointAt < this.minCompNoMaybeCP) {
                return i;
            }
            int norm16 = getNorm16(codePointAt);
            if (!z || !norm16HasCompBoundaryBefore(norm16)) {
                i += Character.charCount(codePointAt);
                decompose(codePointAt, norm16, reorderingBuffer);
                if (z && norm16HasCompBoundaryAfter(norm16, z2)) {
                    break;
                }
            } else {
                return i;
            }
        }
        return i;
    }

    private void decompose(int i, int i2, ReorderingBuffer reorderingBuffer) {
        if (i2 >= this.limitNoNo) {
            if (isMaybeOrNonZeroCC(i2)) {
                reorderingBuffer.append(i, getCCFromYesOrMaybe(i2));
                return;
            } else {
                i = mapAlgorithmic(i, i2);
                i2 = getRawNorm16(i);
            }
        }
        int i3 = 0;
        if (i2 < this.minYesNo) {
            reorderingBuffer.append(i, 0);
        } else if (isHangulLV(i2) || isHangulLVT(i2)) {
            Hangul.decompose(i, reorderingBuffer);
        } else {
            int i4 = i2 >> 1;
            char charAt = this.extraData.charAt(i4);
            int i5 = charAt & 31;
            int i6 = charAt >> '\b';
            if ((charAt & 128) != 0) {
                i3 = this.extraData.charAt(i4 - 1) >> '\b';
            }
            int i7 = i4 + 1;
            reorderingBuffer.append(this.extraData, i7, i7 + i5, true, i3, i6);
        }
    }

    private static int combine(String str, int i, int i2) {
        char charAt;
        if (i2 < 13312) {
            int i3 = i2 << 1;
            while (true) {
                charAt = str.charAt(i);
                if (i3 <= charAt) {
                    break;
                }
                i += (charAt & 1) + 2;
            }
            if (i3 != (charAt & 32766)) {
                return -1;
            }
            if ((charAt & 1) == 0) {
                return str.charAt(i + 1);
            }
            return str.charAt(i + 2) | (str.charAt(i + 1) << 16);
        }
        int i4 = ((i2 >> 9) & -2) + COMP_1_TRAIL_LIMIT;
        int i5 = (i2 << 6) & 65535;
        while (true) {
            char charAt2 = str.charAt(i);
            if (i4 > charAt2) {
                i += (charAt2 & 1) + 2;
            } else if (i4 != (charAt2 & 32766)) {
                return -1;
            } else {
                char charAt3 = str.charAt(i + 1);
                if (i5 > charAt3) {
                    if ((charAt2 & 32768) != 0) {
                        return -1;
                    }
                    i += 3;
                } else if (i5 != (65472 & charAt3)) {
                    return -1;
                } else {
                    return str.charAt(i + 2) | (('?' & charAt3) << 16);
                }
            }
        }
    }

    private void addComposites(int i, UnicodeSet unicodeSet) {
        char charAt;
        int i2;
        do {
            charAt = this.maybeYesCompositions.charAt(i);
            if ((charAt & 1) == 0) {
                i2 = this.maybeYesCompositions.charAt(i + 1);
                i += 2;
            } else {
                i2 = ((this.maybeYesCompositions.charAt(i + 1) & '?') << 16) | this.maybeYesCompositions.charAt(i + 2);
                i += 3;
            }
            int i3 = i2 >> 1;
            if ((i2 & 1) != 0) {
                addComposites(getCompositionsListForComposite(getRawNorm16(i3)), unicodeSet);
            }
            unicodeSet.add(i3);
        } while ((charAt & 32768) == 0);
    }

    private void recompose(ReorderingBuffer reorderingBuffer, int i, boolean z) {
        char charAt;
        char charAt2;
        StringBuilder stringBuilder = reorderingBuffer.getStringBuilder();
        int i2 = i;
        if (i2 != stringBuilder.length()) {
            int i3 = 0;
            boolean z2 = false;
            int i4 = -1;
            int i5 = -1;
            while (true) {
                int codePointAt = stringBuilder.codePointAt(i2);
                i2 += Character.charCount(codePointAt);
                int norm16 = getNorm16(codePointAt);
                int cCFromYesOrMaybe = getCCFromYesOrMaybe(norm16);
                if (isMaybe(norm16) && i4 >= 0 && (i3 < cCFromYesOrMaybe || i3 == 0)) {
                    if (isJamoVT(norm16)) {
                        if (codePointAt < 4519 && (charAt = (char) (stringBuilder.charAt(i5) - 4352)) < 19) {
                            int i6 = i2 - 1;
                            char c = (char) ((((charAt * 21) + (codePointAt - 4449)) * 28) + Hangul.HANGUL_BASE);
                            if (i2 != stringBuilder.length() && (charAt2 = (char) (stringBuilder.charAt(i2) - Hangul.JAMO_T_BASE)) < 28) {
                                i2++;
                                c = (char) (c + charAt2);
                            }
                            stringBuilder.setCharAt(i5, c);
                            stringBuilder.delete(i6, i2);
                            i2 = i6;
                        }
                        if (i2 == stringBuilder.length()) {
                            break;
                        }
                    } else {
                        int combine = combine(this.maybeYesCompositions, i4, codePointAt);
                        if (combine >= 0) {
                            int i7 = combine >> 1;
                            int charCount = i2 - Character.charCount(codePointAt);
                            stringBuilder.delete(charCount, i2);
                            if (z2) {
                                if (i7 > 65535) {
                                    stringBuilder.setCharAt(i5, UTF16.getLeadSurrogate(i7));
                                    stringBuilder.setCharAt(i5 + 1, UTF16.getTrailSurrogate(i7));
                                } else {
                                    stringBuilder.setCharAt(i5, (char) codePointAt);
                                    stringBuilder.deleteCharAt(i5 + 1);
                                    charCount--;
                                    z2 = false;
                                }
                            } else if (i7 > 65535) {
                                stringBuilder.setCharAt(i5, UTF16.getLeadSurrogate(i7));
                                stringBuilder.insert(i5 + 1, UTF16.getTrailSurrogate(i7));
                                charCount++;
                                z2 = true;
                            } else {
                                stringBuilder.setCharAt(i5, (char) i7);
                            }
                            i2 = charCount;
                            if (i2 == stringBuilder.length()) {
                                break;
                            } else if ((combine & 1) != 0) {
                                i4 = getCompositionsListForComposite(getRawNorm16(i7));
                            }
                        }
                    }
                    i4 = -1;
                }
                if (i2 == stringBuilder.length()) {
                    break;
                }
                if (cCFromYesOrMaybe == 0) {
                    i4 = getCompositionsListForDecompYes(norm16);
                    if (i4 >= 0) {
                        if (codePointAt <= 65535) {
                            i5 = i2 - 1;
                            z2 = false;
                        } else {
                            i5 = i2 - 2;
                            z2 = true;
                        }
                    }
                } else if (z) {
                    i4 = -1;
                }
                i3 = cCFromYesOrMaybe;
            }
            reorderingBuffer.flush();
        }
    }

    public int composePair(int i, int i2) {
        int i3;
        int norm16 = getNorm16(i);
        if (isInert(norm16)) {
            return -1;
        }
        if (norm16 >= this.minYesNoMappingsOnly) {
            if (norm16 >= this.minMaybeYes && 64512 > norm16) {
                i3 = getCompositionsListForMaybe(norm16);
            }
            return -1;
        } else if (isJamoL(norm16)) {
            int i4 = i2 - 4449;
            if (i4 < 0 || i4 >= 21) {
                return -1;
            }
            return ((((i - 4352) * 21) + i4) * 28) + Hangul.HANGUL_BASE;
        } else if (isHangulLV(norm16)) {
            int i5 = i2 - 4519;
            if (i5 <= 0 || i5 >= 28) {
                return -1;
            }
            return i + i5;
        } else {
            i3 = ((MIN_NORMAL_MAYBE_YES - this.minMaybeYes) + norm16) >> 1;
            if (norm16 > this.minYesNo) {
                i3 += (this.maybeYesCompositions.charAt(i3) & 31) + 1;
            }
        }
        if (i2 >= 0 && 1114111 >= i2) {
            return combine(this.maybeYesCompositions, i3, i2) >> 1;
        }
        return -1;
    }

    private boolean hasCompBoundaryBefore(int i, int i2) {
        return i < this.minCompNoMaybeCP || norm16HasCompBoundaryBefore(i2);
    }

    private boolean norm16HasCompBoundaryBefore(int i) {
        return i < this.minNoNoCompNoMaybeCC || isAlgorithmicNoNo(i);
    }

    private boolean hasCompBoundaryBefore(CharSequence charSequence, int i, int i2) {
        return i == i2 || hasCompBoundaryBefore(Character.codePointAt(charSequence, i));
    }

    private boolean norm16HasCompBoundaryAfter(int i, boolean z) {
        return (i & 1) != 0 && (!z || isTrailCC01ForCompBoundaryAfter(i));
    }

    private boolean hasCompBoundaryAfter(CharSequence charSequence, int i, int i2, boolean z) {
        return i == i2 || hasCompBoundaryAfter(Character.codePointBefore(charSequence, i2), z);
    }

    private boolean isTrailCC01ForCompBoundaryAfter(int i) {
        if (isInert(i)) {
            return true;
        }
        if (isDecompNoAlgorithmic(i)) {
            if ((i & 6) <= 2) {
                return true;
            }
        } else if (this.extraData.charAt(i >> 1) <= 511) {
            return true;
        }
        return false;
    }

    private int findPreviousCompBoundary(CharSequence charSequence, int i, boolean z) {
        while (i > 0) {
            int codePointBefore = Character.codePointBefore(charSequence, i);
            int norm16 = getNorm16(codePointBefore);
            if (!norm16HasCompBoundaryAfter(norm16, z)) {
                i -= Character.charCount(codePointBefore);
                if (hasCompBoundaryBefore(codePointBefore, norm16)) {
                    break;
                }
            } else {
                break;
            }
        }
        return i;
    }

    private int findNextCompBoundary(CharSequence charSequence, int i, int i2, boolean z) {
        while (i < i2) {
            int codePointAt = Character.codePointAt(charSequence, i);
            int i3 = this.normTrie.get(codePointAt);
            if (!hasCompBoundaryBefore(codePointAt, i3)) {
                i += Character.charCount(codePointAt);
                if (norm16HasCompBoundaryAfter(i3, z)) {
                    break;
                }
            } else {
                break;
            }
        }
        return i;
    }

    private int findPreviousFCDBoundary(CharSequence charSequence, int i) {
        while (i > 0) {
            int codePointBefore = Character.codePointBefore(charSequence, i);
            if (codePointBefore < this.minDecompNoCP) {
                break;
            }
            int norm16 = getNorm16(codePointBefore);
            if (!norm16HasDecompBoundaryAfter(norm16)) {
                i -= Character.charCount(codePointBefore);
                if (norm16HasDecompBoundaryBefore(norm16)) {
                    break;
                }
            } else {
                break;
            }
        }
        return i;
    }

    private int findNextFCDBoundary(CharSequence charSequence, int i, int i2) {
        while (i < i2) {
            int codePointAt = Character.codePointAt(charSequence, i);
            if (codePointAt < this.minLcccCP) {
                break;
            }
            int norm16 = getNorm16(codePointAt);
            if (!norm16HasDecompBoundaryBefore(norm16)) {
                i += Character.charCount(codePointAt);
                if (norm16HasDecompBoundaryAfter(norm16)) {
                    break;
                }
            } else {
                break;
            }
        }
        return i;
    }

    private int getPreviousTrailCC(CharSequence charSequence, int i, int i2) {
        if (i == i2) {
            return 0;
        }
        return getFCD16(Character.codePointBefore(charSequence, i2));
    }

    private void addToStartSet(MutableCodePointTrie mutableCodePointTrie, int i, int i2) {
        UnicodeSet unicodeSet;
        int i3 = mutableCodePointTrie.get(i2);
        if ((4194303 & i3) != 0 || i == 0) {
            if ((i3 & 2097152) == 0) {
                int i4 = i3 & CANON_VALUE_MASK;
                mutableCodePointTrie.set(i2, (i3 & -2097152) | 2097152 | this.canonStartSets.size());
                ArrayList<UnicodeSet> arrayList = this.canonStartSets;
                unicodeSet = new UnicodeSet();
                arrayList.add(unicodeSet);
                if (i4 != 0) {
                    unicodeSet.add(i4);
                }
            } else {
                unicodeSet = this.canonStartSets.get(i3 & CANON_VALUE_MASK);
            }
            unicodeSet.add(i);
            return;
        }
        mutableCodePointTrie.set(i2, i3 | i);
    }
}
