package android.icu.impl;

import android.icu.impl.ICUBinary.Authenticate;
import android.icu.impl.Trie2.Range;
import android.icu.impl.Trie2.ValueMapper;
import android.icu.impl.coll.CollationFastLatin;
import android.icu.text.DateTimePatternGenerator;
import android.icu.text.UTF16;
import android.icu.text.UnicodeSet;
import android.icu.util.ICUUncheckedIOException;
import android.icu.util.VersionInfo;
import dalvik.bytecode.Opcodes;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;

public final class Normalizer2Impl {
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
    private static final IsAcceptable IS_ACCEPTABLE = new IsAcceptable();
    public static final int IX_COUNT = 16;
    public static final int IX_EXTRA_DATA_OFFSET = 1;
    public static final int IX_LIMIT_NO_NO = 12;
    public static final int IX_MIN_COMP_NO_MAYBE_CP = 9;
    public static final int IX_MIN_DECOMP_NO_CP = 8;
    public static final int IX_MIN_MAYBE_YES = 13;
    public static final int IX_MIN_NO_NO = 11;
    public static final int IX_MIN_YES_NO = 10;
    public static final int IX_MIN_YES_NO_MAPPINGS_ONLY = 14;
    public static final int IX_NORM_TRIE_OFFSET = 0;
    public static final int IX_RESERVED3_OFFSET = 3;
    public static final int IX_SMALL_FCD_OFFSET = 2;
    public static final int IX_TOTAL_SIZE = 7;
    public static final int JAMO_L = 1;
    public static final int JAMO_VT = 65280;
    public static final int MAPPING_HAS_CCC_LCCC_WORD = 128;
    public static final int MAPPING_HAS_RAW_MAPPING = 64;
    public static final int MAPPING_LENGTH_MASK = 31;
    public static final int MAPPING_NO_COMP_BOUNDARY_AFTER = 32;
    public static final int MAX_DELTA = 64;
    public static final int MIN_CCC_LCCC_CP = 768;
    public static final int MIN_NORMAL_MAYBE_YES = 65024;
    public static final int MIN_YES_YES_WITH_CC = 65281;
    private static final ValueMapper segmentStarterMapper = new ValueMapper() {
        public int map(int in) {
            return Integer.MIN_VALUE & in;
        }
    };
    private Trie2_32 canonIterData;
    private ArrayList<UnicodeSet> canonStartSets;
    private VersionInfo dataVersion;
    private String extraData;
    private int limitNoNo;
    private String maybeYesCompositions;
    private int minCompNoMaybeCP;
    private int minDecompNoCP;
    private int minMaybeYes;
    private int minNoNo;
    private int minYesNo;
    private int minYesNoMappingsOnly;
    private Trie2_16 normTrie;
    private byte[] smallFCD;
    private int[] tccc180;

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

        public static boolean isHangul(int c) {
            return HANGUL_BASE <= c && c < HANGUL_LIMIT;
        }

        public static boolean isHangulWithoutJamoT(char c) {
            c = (char) (c - HANGUL_BASE);
            if (c >= 11172 || c % 28 != 0) {
                return false;
            }
            return true;
        }

        public static boolean isJamoL(int c) {
            return JAMO_L_BASE <= c && c < JAMO_L_LIMIT;
        }

        public static boolean isJamoV(int c) {
            return JAMO_V_BASE <= c && c < JAMO_V_LIMIT;
        }

        public static int decompose(int c, Appendable buffer) {
            c -= HANGUL_BASE;
            try {
                int c2 = c % 28;
                c /= 28;
                buffer.append((char) ((c / 21) + JAMO_L_BASE));
                buffer.append((char) ((c % 21) + JAMO_V_BASE));
                if (c2 == 0) {
                    return 2;
                }
                buffer.append((char) (c2 + JAMO_T_BASE));
                return 3;
            } catch (Throwable e) {
                throw new ICUUncheckedIOException(e);
            }
        }

        public static void getRawDecomposition(int c, Appendable buffer) {
            int orig = c;
            c -= HANGUL_BASE;
            try {
                int c2 = c % 28;
                if (c2 == 0) {
                    c /= 28;
                    buffer.append((char) ((c / 21) + JAMO_L_BASE));
                    buffer.append((char) ((c % 21) + JAMO_V_BASE));
                    return;
                }
                buffer.append((char) (orig - c2));
                buffer.append((char) (c2 + JAMO_T_BASE));
            } catch (Throwable e) {
                throw new ICUUncheckedIOException(e);
            }
        }
    }

    private static final class IsAcceptable implements Authenticate {
        /* synthetic */ IsAcceptable(IsAcceptable -this0) {
            this();
        }

        private IsAcceptable() {
        }

        public boolean isDataVersionAcceptable(byte[] version) {
            return version[0] == (byte) 2;
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

        public ReorderingBuffer(Normalizer2Impl ni, Appendable dest, int destCapacity) {
            this.impl = ni;
            this.app = dest;
            if (this.app instanceof StringBuilder) {
                this.appIsStringBuilder = true;
                this.str = (StringBuilder) dest;
                this.str.ensureCapacity(destCapacity);
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

        public boolean equals(CharSequence s, int start, int limit) {
            return UTF16Plus.equal(this.str, 0, this.str.length(), s, start, limit);
        }

        public void setLastChar(char c) {
            this.str.setCharAt(this.str.length() - 1, c);
        }

        public void append(int c, int cc) {
            if (this.lastCC <= cc || cc == 0) {
                this.str.appendCodePoint(c);
                this.lastCC = cc;
                if (cc <= 1) {
                    this.reorderStart = this.str.length();
                    return;
                }
                return;
            }
            insert(c, cc);
        }

        public void append(CharSequence s, int start, int limit, int leadCC, int trailCC) {
            if (start != limit) {
                if (this.lastCC <= leadCC || leadCC == 0) {
                    if (trailCC <= 1) {
                        this.reorderStart = this.str.length() + (limit - start);
                    } else if (leadCC <= 1) {
                        this.reorderStart = this.str.length() + 1;
                    }
                    this.str.append(s, start, limit);
                    this.lastCC = trailCC;
                } else {
                    int c = Character.codePointAt(s, start);
                    start += Character.charCount(c);
                    insert(c, leadCC);
                    while (start < limit) {
                        c = Character.codePointAt(s, start);
                        start += Character.charCount(c);
                        if (start < limit) {
                            leadCC = Normalizer2Impl.getCCFromYesOrMaybe(this.impl.getNorm16(c));
                        } else {
                            leadCC = trailCC;
                        }
                        append(c, leadCC);
                    }
                }
            }
        }

        public ReorderingBuffer append(char c) {
            this.str.append(c);
            this.lastCC = 0;
            this.reorderStart = this.str.length();
            return this;
        }

        public void appendZeroCC(int c) {
            this.str.appendCodePoint(c);
            this.lastCC = 0;
            this.reorderStart = this.str.length();
        }

        public ReorderingBuffer append(CharSequence s) {
            if (s.length() != 0) {
                this.str.append(s);
                this.lastCC = 0;
                this.reorderStart = this.str.length();
            }
            return this;
        }

        public ReorderingBuffer append(CharSequence s, int start, int limit) {
            if (start != limit) {
                this.str.append(s, start, limit);
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
                } catch (Throwable e) {
                    throw new ICUUncheckedIOException(e);
                }
            }
            this.lastCC = 0;
        }

        public ReorderingBuffer flushAndAppendZeroCC(CharSequence s, int start, int limit) {
            if (this.appIsStringBuilder) {
                this.str.append(s, start, limit);
                this.reorderStart = this.str.length();
            } else {
                try {
                    this.app.append(this.str).append(s, start, limit);
                    this.str.setLength(0);
                    this.reorderStart = 0;
                } catch (Throwable e) {
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

        public void removeSuffix(int suffixLength) {
            int oldLength = this.str.length();
            this.str.delete(oldLength - suffixLength, oldLength);
            this.lastCC = 0;
            this.reorderStart = this.str.length();
        }

        private void insert(int c, int cc) {
            setIterator();
            skipPrevious();
            do {
            } while (previousCC() > cc);
            if (c <= DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH) {
                this.str.insert(this.codePointLimit, (char) c);
                if (cc <= 1) {
                    this.reorderStart = this.codePointLimit + 1;
                    return;
                }
                return;
            }
            this.str.insert(this.codePointLimit, Character.toChars(c));
            if (cc <= 1) {
                this.reorderStart = this.codePointLimit + 2;
            }
        }

        private void setIterator() {
            this.codePointStart = this.str.length();
        }

        private void skipPrevious() {
            this.codePointLimit = this.codePointStart;
            this.codePointStart = this.str.offsetByCodePoints(this.codePointStart, -1);
        }

        private int previousCC() {
            this.codePointLimit = this.codePointStart;
            if (this.reorderStart >= this.codePointStart) {
                return 0;
            }
            int c = this.str.codePointBefore(this.codePointStart);
            this.codePointStart -= Character.charCount(c);
            if (c < 768) {
                return 0;
            }
            return Normalizer2Impl.getCCFromYesOrMaybe(this.impl.getNorm16(c));
        }
    }

    public static final class UTF16Plus {
        public static boolean isSurrogateLead(int c) {
            return (c & 1024) == 0;
        }

        public static boolean equal(CharSequence s1, CharSequence s2) {
            if (s1 == s2) {
                return true;
            }
            int length = s1.length();
            if (length != s2.length()) {
                return false;
            }
            for (int i = 0; i < length; i++) {
                if (s1.charAt(i) != s2.charAt(i)) {
                    return false;
                }
            }
            return true;
        }

        public static boolean equal(CharSequence s1, int start1, int limit1, CharSequence s2, int start2, int limit2) {
            if (limit1 - start1 != limit2 - start2) {
                return false;
            }
            if (s1 == s2 && start1 == start2) {
                return true;
            }
            int start22 = start2;
            int start12 = start1;
            while (start12 < limit1) {
                start1 = start12 + 1;
                start2 = start22 + 1;
                if (s1.charAt(start12) != s2.charAt(start22)) {
                    return false;
                }
                start22 = start2;
                start12 = start1;
            }
            return true;
        }
    }

    public Normalizer2Impl load(ByteBuffer bytes) {
        try {
            this.dataVersion = ICUBinary.readHeaderAndDataVersion(bytes, DATA_FORMAT, IS_ACCEPTABLE);
            int indexesLength = bytes.getInt() / 4;
            if (indexesLength <= 13) {
                throw new ICUUncheckedIOException("Normalizer2 data: not enough indexes");
            }
            int i;
            int[] inIndexes = new int[indexesLength];
            inIndexes[0] = indexesLength * 4;
            for (i = 1; i < indexesLength; i++) {
                inIndexes[i] = bytes.getInt();
            }
            this.minDecompNoCP = inIndexes[8];
            this.minCompNoMaybeCP = inIndexes[9];
            this.minYesNo = inIndexes[10];
            this.minYesNoMappingsOnly = inIndexes[14];
            this.minNoNo = inIndexes[11];
            this.limitNoNo = inIndexes[12];
            this.minMaybeYes = inIndexes[13];
            int offset = inIndexes[0];
            int nextOffset = inIndexes[1];
            this.normTrie = Trie2_16.createFromSerialized(bytes);
            int trieLength = this.normTrie.getSerializedLength();
            if (trieLength > nextOffset - offset) {
                throw new ICUUncheckedIOException("Normalizer2 data: not enough bytes for normTrie");
            }
            ICUBinary.skipBytes(bytes, (nextOffset - offset) - trieLength);
            offset = nextOffset;
            nextOffset = inIndexes[2];
            int numChars = (nextOffset - offset) / 2;
            if (numChars != 0) {
                this.maybeYesCompositions = ICUBinary.getString(bytes, numChars, 0);
                this.extraData = this.maybeYesCompositions.substring(MIN_NORMAL_MAYBE_YES - this.minMaybeYes);
            }
            offset = nextOffset;
            this.smallFCD = new byte[256];
            bytes.get(this.smallFCD);
            this.tccc180 = new int[CollationFastLatin.LATIN_LIMIT];
            int bits = 0;
            int c = 0;
            while (c < CollationFastLatin.LATIN_LIMIT) {
                if ((c & 255) == 0) {
                    bits = this.smallFCD[c >> 8];
                }
                if ((bits & 1) != 0) {
                    i = 0;
                    while (i < 32) {
                        this.tccc180[c] = getFCD16FromNormData(c) & 255;
                        i++;
                        c++;
                    }
                } else {
                    c += 32;
                }
                bits >>= 1;
            }
            return this;
        } catch (Throwable e) {
            throw new ICUUncheckedIOException(e);
        }
    }

    public Normalizer2Impl load(String name) {
        return load(ICUBinary.getRequiredData(name));
    }

    private void enumLcccRange(int start, int end, int norm16, UnicodeSet set) {
        if (isAlgorithmicNoNo(norm16)) {
            do {
                if (getFCD16(start) > 255) {
                    set.add(start);
                }
                start++;
            } while (start <= end);
        } else if (getFCD16(start) > 255) {
            set.add(start, end);
        }
    }

    private void enumNorm16PropertyStartsRange(int start, int end, int value, UnicodeSet set) {
        set.add(start);
        if (start != end && isAlgorithmicNoNo(value)) {
            int prevFCD16 = getFCD16(start);
            while (true) {
                start++;
                if (start <= end) {
                    int fcd16 = getFCD16(start);
                    if (fcd16 != prevFCD16) {
                        set.add(start);
                        prevFCD16 = fcd16;
                    }
                } else {
                    return;
                }
            }
        }
    }

    public void addLcccChars(UnicodeSet set) {
        Iterator<Range> trieIterator = this.normTrie.iterator();
        while (trieIterator.hasNext()) {
            Range range = (Range) trieIterator.next();
            if ((range.leadSurrogate ^ 1) != 0) {
                enumLcccRange(range.startCodePoint, range.endCodePoint, range.value, set);
            } else {
                return;
            }
        }
    }

    public void addPropertyStarts(UnicodeSet set) {
        Iterator<Range> trieIterator = this.normTrie.iterator();
        while (trieIterator.hasNext()) {
            Range range = (Range) trieIterator.next();
            if ((range.leadSurrogate ^ 1) == 0) {
                break;
            }
            enumNorm16PropertyStartsRange(range.startCodePoint, range.endCodePoint, range.value, set);
        }
        for (int c = Hangul.HANGUL_BASE; c < Hangul.HANGUL_LIMIT; c += 28) {
            set.add(c);
            set.add(c + 1);
        }
        set.add((int) Hangul.HANGUL_LIMIT);
    }

    public void addCanonIterPropertyStarts(UnicodeSet set) {
        ensureCanonIterData();
        Iterator<Range> trieIterator = this.canonIterData.iterator(segmentStarterMapper);
        while (trieIterator.hasNext()) {
            Range range = (Range) trieIterator.next();
            if ((range.leadSurrogate ^ 1) != 0) {
                set.add(range.startCodePoint);
            } else {
                return;
            }
        }
    }

    public Trie2_16 getNormTrie() {
        return this.normTrie;
    }

    public synchronized Normalizer2Impl ensureCanonIterData() {
        if (this.canonIterData == null) {
            Trie2Writable newData = new Trie2Writable(0, 0);
            this.canonStartSets = new ArrayList();
            Iterator<Range> trieIterator = this.normTrie.iterator();
            while (trieIterator.hasNext()) {
                Range range = (Range) trieIterator.next();
                if ((range.leadSurrogate ^ 1) == 0) {
                    break;
                }
                int norm16 = range.value;
                if (norm16 != 0 && (this.minYesNo > norm16 || norm16 >= this.minNoNo)) {
                    int c = range.startCodePoint;
                    while (c <= range.endCodePoint) {
                        int oldValue = newData.get(c);
                        int newValue = oldValue;
                        if (norm16 >= this.minMaybeYes) {
                            newValue = oldValue | Integer.MIN_VALUE;
                            if (norm16 < MIN_NORMAL_MAYBE_YES) {
                                newValue |= 1073741824;
                            }
                        } else if (norm16 < this.minYesNo) {
                            newValue = oldValue | 1073741824;
                        } else {
                            int c2 = c;
                            int norm16_2 = norm16;
                            while (this.limitNoNo <= norm16_2 && norm16_2 < this.minMaybeYes) {
                                c2 = mapAlgorithmic(c2, norm16_2);
                                norm16_2 = getNorm16(c2);
                            }
                            if (this.minYesNo > norm16_2 || norm16_2 >= this.limitNoNo) {
                                addToStartSet(newData, c, c2);
                            } else {
                                int firstUnit = this.extraData.charAt(norm16_2);
                                int length = firstUnit & 31;
                                if (!((firstUnit & 128) == 0 || c != c2 || (this.extraData.charAt(norm16_2 - 1) & 255) == 0)) {
                                    newValue = oldValue | Integer.MIN_VALUE;
                                }
                                if (length != 0) {
                                    norm16_2++;
                                    int limit = norm16_2 + length;
                                    c2 = this.extraData.codePointAt(norm16_2);
                                    addToStartSet(newData, c, c2);
                                    if (norm16_2 >= this.minNoNo) {
                                        while (true) {
                                            norm16_2 += Character.charCount(c2);
                                            if (norm16_2 >= limit) {
                                                break;
                                            }
                                            c2 = this.extraData.codePointAt(norm16_2);
                                            int c2Value = newData.get(c2);
                                            if ((Integer.MIN_VALUE & c2Value) == 0) {
                                                newData.set(c2, Integer.MIN_VALUE | c2Value);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (newValue != oldValue) {
                            newData.set(c, newValue);
                        }
                        c++;
                    }
                    continue;
                }
            }
            this.canonIterData = newData.toTrie2_32();
        }
        return this;
    }

    public int getNorm16(int c) {
        return this.normTrie.get(c);
    }

    public int getCompQuickCheck(int norm16) {
        if (norm16 < this.minNoNo || MIN_YES_YES_WITH_CC <= norm16) {
            return 1;
        }
        if (this.minMaybeYes <= norm16) {
            return 2;
        }
        return 0;
    }

    public boolean isAlgorithmicNoNo(int norm16) {
        return this.limitNoNo <= norm16 && norm16 < this.minMaybeYes;
    }

    public boolean isCompNo(int norm16) {
        return this.minNoNo <= norm16 && norm16 < this.minMaybeYes;
    }

    public boolean isDecompYes(int norm16) {
        return norm16 < this.minYesNo || this.minMaybeYes <= norm16;
    }

    public int getCC(int norm16) {
        if (norm16 >= MIN_NORMAL_MAYBE_YES) {
            return norm16 & 255;
        }
        if (norm16 < this.minNoNo || this.limitNoNo <= norm16) {
            return 0;
        }
        return getCCFromNoNo(norm16);
    }

    public static int getCCFromYesOrMaybe(int norm16) {
        return norm16 >= MIN_NORMAL_MAYBE_YES ? norm16 & 255 : 0;
    }

    public int getFCD16(int c) {
        if (c < 0) {
            return 0;
        }
        if (c < CollationFastLatin.LATIN_LIMIT) {
            return this.tccc180[c];
        }
        if (c > DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH || singleLeadMightHaveNonZeroFCD16(c)) {
            return getFCD16FromNormData(c);
        }
        return 0;
    }

    public int getFCD16FromBelow180(int c) {
        return this.tccc180[c];
    }

    public boolean singleLeadMightHaveNonZeroFCD16(int lead) {
        boolean z = false;
        byte bits = this.smallFCD[lead >> 8];
        if (bits == (byte) 0) {
            return false;
        }
        if (((bits >> ((lead >> 5) & 7)) & 1) != 0) {
            z = true;
        }
        return z;
    }

    public int getFCD16FromNormData(int c) {
        while (true) {
            int norm16 = getNorm16(c);
            if (norm16 <= this.minYesNo) {
                return 0;
            }
            if (norm16 >= MIN_NORMAL_MAYBE_YES) {
                norm16 &= 255;
                return (norm16 << 8) | norm16;
            } else if (norm16 >= this.minMaybeYes) {
                return 0;
            } else {
                if (isDecompNoAlgorithmic(norm16)) {
                    c = mapAlgorithmic(c, norm16);
                } else {
                    int firstUnit = this.extraData.charAt(norm16);
                    if ((firstUnit & 31) == 0) {
                        return Opcodes.OP_CHECK_CAST_JUMBO;
                    }
                    int fcd16 = firstUnit >> 8;
                    if ((firstUnit & 128) != 0) {
                        fcd16 |= this.extraData.charAt(norm16 - 1) & JAMO_VT;
                    }
                    return fcd16;
                }
            }
        }
    }

    public String getDecomposition(int c) {
        int decomp = -1;
        while (c >= this.minDecompNoCP) {
            int norm16 = getNorm16(c);
            if (isDecompYes(norm16)) {
                break;
            } else if (isHangul(norm16)) {
                StringBuilder buffer = new StringBuilder();
                Hangul.decompose(c, buffer);
                return buffer.toString();
            } else if (isDecompNoAlgorithmic(norm16)) {
                c = mapAlgorithmic(c, norm16);
                decomp = c;
            } else {
                int norm162 = norm16 + 1;
                return this.extraData.substring(norm162, norm162 + (this.extraData.charAt(norm16) & 31));
            }
        }
        if (decomp < 0) {
            return null;
        }
        return UTF16.valueOf(decomp);
    }

    public String getRawDecomposition(int c) {
        if (c >= this.minDecompNoCP) {
            int norm16 = getNorm16(c);
            if (!isDecompYes(norm16)) {
                if (isHangul(norm16)) {
                    StringBuilder buffer = new StringBuilder();
                    Hangul.getRawDecomposition(c, buffer);
                    return buffer.toString();
                } else if (isDecompNoAlgorithmic(norm16)) {
                    return UTF16.valueOf(mapAlgorithmic(c, norm16));
                } else {
                    int firstUnit = this.extraData.charAt(norm16);
                    int mLength = firstUnit & 31;
                    if ((firstUnit & 64) != 0) {
                        int rawMapping = (norm16 - ((firstUnit >> 7) & 1)) - 1;
                        char rm0 = this.extraData.charAt(rawMapping);
                        if (rm0 <= 31) {
                            return this.extraData.substring(rawMapping - rm0, rawMapping);
                        }
                        norm16 += 3;
                        return new StringBuilder(mLength - 1).append(rm0).append(this.extraData, norm16, (norm16 + mLength) - 2).toString();
                    }
                    norm16++;
                    return this.extraData.substring(norm16, norm16 + mLength);
                }
            }
        }
        return null;
    }

    public boolean isCanonSegmentStarter(int c) {
        return this.canonIterData.get(c) >= 0;
    }

    public boolean getCanonStartSet(int c, UnicodeSet set) {
        int canonValue = this.canonIterData.get(c) & Integer.MAX_VALUE;
        if (canonValue == 0) {
            return false;
        }
        set.clear();
        int value = canonValue & 2097151;
        if ((2097152 & canonValue) != 0) {
            set.addAll((UnicodeSet) this.canonStartSets.get(value));
        } else if (value != 0) {
            set.add(value);
        }
        if ((1073741824 & canonValue) != 0) {
            int norm16 = getNorm16(c);
            if (norm16 == 1) {
                int syllable = Hangul.HANGUL_BASE + ((c - 4352) * Hangul.JAMO_VT_COUNT);
                set.add(syllable, (syllable + Hangul.JAMO_VT_COUNT) - 1);
            } else {
                addComposites(getCompositionsList(norm16), set);
            }
        }
        return true;
    }

    public Appendable decompose(CharSequence s, StringBuilder dest) {
        decompose(s, 0, s.length(), dest, s.length());
        return dest;
    }

    public void decompose(CharSequence s, int src, int limit, StringBuilder dest, int destLengthEstimate) {
        if (destLengthEstimate < 0) {
            destLengthEstimate = limit - src;
        }
        dest.setLength(0);
        decompose(s, src, limit, new ReorderingBuffer(this, dest, destLengthEstimate));
    }

    public int decompose(CharSequence s, int src, int limit, ReorderingBuffer buffer) {
        int minNoCP = this.minDecompNoCP;
        int c = 0;
        int norm16 = 0;
        int prevBoundary = src;
        int prevCC = 0;
        while (true) {
            int prevSrc = src;
            while (src != limit) {
                c = s.charAt(src);
                if (c >= minNoCP) {
                    norm16 = this.normTrie.getFromU16SingleLead((char) c);
                    if (!isMostDecompYesAndZeroCC(norm16)) {
                        if (!UTF16.isSurrogate((char) c)) {
                            break;
                        }
                        char c2;
                        if (UTF16Plus.isSurrogateLead(c)) {
                            if (src + 1 != limit) {
                                c2 = s.charAt(src + 1);
                                if (Character.isLowSurrogate(c2)) {
                                    c = Character.toCodePoint((char) c, c2);
                                }
                            }
                        } else if (prevSrc < src) {
                            c2 = s.charAt(src - 1);
                            if (Character.isHighSurrogate(c2)) {
                                src--;
                                c = Character.toCodePoint(c2, (char) c);
                            }
                        }
                        norm16 = getNorm16(c);
                        if (!isMostDecompYesAndZeroCC(norm16)) {
                            break;
                        }
                        src += Character.charCount(c);
                    }
                }
                src++;
            }
            if (src != prevSrc) {
                if (buffer != null) {
                    buffer.flushAndAppendZeroCC(s, prevSrc, src);
                } else {
                    prevCC = 0;
                    prevBoundary = src;
                }
            }
            if (src != limit) {
                src += Character.charCount(c);
                if (buffer == null) {
                    if (!isDecompYes(norm16)) {
                        break;
                    }
                    int cc = getCCFromYesOrMaybe(norm16);
                    if (prevCC > cc && cc != 0) {
                        break;
                    }
                    prevCC = cc;
                    if (cc <= 1) {
                        prevBoundary = src;
                    }
                } else {
                    decompose(c, norm16, buffer);
                }
            } else {
                return src;
            }
        }
        return prevBoundary;
    }

    public void decomposeAndAppend(CharSequence s, boolean doDecompose, ReorderingBuffer buffer) {
        int limit = s.length();
        if (limit != 0) {
            if (doDecompose) {
                decompose(s, 0, limit, buffer);
                return;
            }
            int c = Character.codePointAt(s, 0);
            int src = 0;
            int cc = getCC(getNorm16(c));
            int prevCC = cc;
            int firstCC = cc;
            int cc2 = cc;
            while (cc2 != 0) {
                prevCC = cc2;
                src += Character.charCount(c);
                if (src >= limit) {
                    break;
                }
                c = Character.codePointAt(s, src);
                cc2 = getCC(getNorm16(c));
            }
            buffer.append(s, 0, src, cc, prevCC);
            buffer.append(s, src, limit);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:65:0x016d  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0101  */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x015f  */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x01cd A:{SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x019a  */
    /* JADX WARNING: Removed duplicated region for block: B:106:0x0212  */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x01e4  */
    /* JADX WARNING: Removed duplicated region for block: B:102:0x0203  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean compose(CharSequence s, int src, int limit, boolean onlyContiguous, boolean doCompose, ReorderingBuffer buffer) {
        int minNoMaybeCP = this.minCompNoMaybeCP;
        int prevBoundary = src;
        int c = 0;
        int norm16 = 0;
        int prevCC = 0;
        while (true) {
            int prevSrc = src;
            while (src != limit) {
                c = s.charAt(src);
                if (c >= minNoMaybeCP) {
                    norm16 = this.normTrie.getFromU16SingleLead((char) c);
                    if (!isCompYesAndZeroCC(norm16)) {
                        if (!UTF16.isSurrogate((char) c)) {
                            break;
                        }
                        char c2;
                        if (UTF16Plus.isSurrogateLead(c)) {
                            if (src + 1 != limit) {
                                c2 = s.charAt(src + 1);
                                if (Character.isLowSurrogate(c2)) {
                                    c = Character.toCodePoint((char) c, c2);
                                }
                            }
                        } else if (prevSrc < src) {
                            c2 = s.charAt(src - 1);
                            if (Character.isHighSurrogate(c2)) {
                                src--;
                                c = Character.toCodePoint(c2, (char) c);
                            }
                        }
                        norm16 = getNorm16(c);
                        if (!isCompYesAndZeroCC(norm16)) {
                            break;
                        }
                        src += Character.charCount(c);
                    }
                }
                src++;
            }
            char prev;
            boolean needToDecompose;
            int recomposeStartIndex;
            if (src == prevSrc) {
                if (src == limit) {
                    break;
                }
                src += Character.charCount(c);
                prev = s.charAt(prevSrc - 1);
                needToDecompose = false;
                if (c >= 4519) {
                }
                if (!needToDecompose) {
                }
                if (norm16 < 65281) {
                }
                if (hasCompBoundaryBefore(c, norm16)) {
                }
                src = findNextCompBoundary(s, src, limit);
                recomposeStartIndex = buffer.length();
                decomposeShort(s, prevBoundary, src, buffer);
                recompose(buffer, recomposeStartIndex, onlyContiguous);
                if (!doCompose) {
                }
                prevBoundary = src;
            } else if (src != limit) {
                prevBoundary = src - 1;
                if (Character.isLowSurrogate(s.charAt(prevBoundary)) && prevSrc < prevBoundary && Character.isHighSurrogate(s.charAt(prevBoundary - 1))) {
                    prevBoundary--;
                }
                if (doCompose) {
                    buffer.flushAndAppendZeroCC(s, prevSrc, prevBoundary);
                    buffer.append(s, prevBoundary, src);
                } else {
                    prevCC = 0;
                }
                prevSrc = src;
                src += Character.charCount(c);
                if (isJamoVT(norm16) && prevBoundary != prevSrc) {
                    prev = s.charAt(prevSrc - 1);
                    needToDecompose = false;
                    if (c >= 4519) {
                        prev = (char) (prev - 4352);
                        if (prev < 19) {
                            if (!doCompose) {
                                return false;
                            }
                            char syllable = (char) ((((prev * 21) + (c - 4449)) * 28) + Hangul.HANGUL_BASE);
                            if (src != limit) {
                                char t = (char) (s.charAt(src) - 4519);
                                if (t < 28) {
                                    src++;
                                    prevBoundary = src;
                                    buffer.setLastChar((char) (syllable + t));
                                }
                            }
                            needToDecompose = true;
                        }
                    } else if (Hangul.isHangulWithoutJamoT(prev)) {
                        if (!doCompose) {
                            return false;
                        }
                        buffer.setLastChar((char) ((prev + c) - 4519));
                        prevBoundary = src;
                    }
                    if (needToDecompose) {
                        if (doCompose) {
                            buffer.append((char) c);
                        } else {
                            prevCC = 0;
                        }
                    }
                }
                if (norm16 < 65281) {
                    int cc = norm16 & 255;
                    if (onlyContiguous) {
                        if ((doCompose ? buffer.getLastCC() : prevCC) == 0 && prevBoundary < prevSrc && getTrailCCFromCompYesAndZeroCC(s, prevBoundary, prevSrc) > cc) {
                            if (!doCompose) {
                                return false;
                            }
                        }
                    }
                    if (doCompose) {
                        buffer.append(c, cc);
                    } else if (prevCC > cc) {
                        return false;
                    } else {
                        prevCC = cc;
                    }
                } else if (!(doCompose || (isMaybeOrNonZeroCC(norm16) ^ 1) == 0)) {
                    return false;
                }
                if (hasCompBoundaryBefore(c, norm16)) {
                    prevBoundary = prevSrc;
                } else if (doCompose) {
                    buffer.removeSuffix(prevSrc - prevBoundary);
                }
                src = findNextCompBoundary(s, src, limit);
                recomposeStartIndex = buffer.length();
                decomposeShort(s, prevBoundary, src, buffer);
                recompose(buffer, recomposeStartIndex, onlyContiguous);
                if (doCompose) {
                    if (!buffer.equals(s, prevBoundary, src)) {
                        return false;
                    }
                    buffer.remove();
                    prevCC = 0;
                }
                prevBoundary = src;
            } else if (doCompose) {
                buffer.flushAndAppendZeroCC(s, prevSrc, src);
            }
        }
        return true;
    }

    public int composeQuickCheck(CharSequence s, int src, int limit, boolean onlyContiguous, boolean doSpan) {
        int qcResult = 0;
        int minNoMaybeCP = this.minCompNoMaybeCP;
        int prevBoundary = src;
        int prevCC = 0;
        while (true) {
            int prevSrc = src;
            while (src != limit) {
                int c = s.charAt(src);
                if (c >= minNoMaybeCP) {
                    int norm16 = this.normTrie.getFromU16SingleLead((char) c);
                    if (!isCompYesAndZeroCC(norm16)) {
                        if (UTF16.isSurrogate((char) c)) {
                            char c2;
                            if (UTF16Plus.isSurrogateLead(c)) {
                                if (src + 1 != limit) {
                                    c2 = s.charAt(src + 1);
                                    if (Character.isLowSurrogate(c2)) {
                                        c = Character.toCodePoint((char) c, c2);
                                    }
                                }
                            } else if (prevSrc < src) {
                                c2 = s.charAt(src - 1);
                                if (Character.isHighSurrogate(c2)) {
                                    src--;
                                    c = Character.toCodePoint(c2, (char) c);
                                }
                            }
                            norm16 = getNorm16(c);
                            if (isCompYesAndZeroCC(norm16)) {
                                src += Character.charCount(c);
                            }
                        }
                        if (src != prevSrc) {
                            prevBoundary = src - 1;
                            if (Character.isLowSurrogate(s.charAt(prevBoundary)) && prevSrc < prevBoundary && Character.isHighSurrogate(s.charAt(prevBoundary - 1))) {
                                prevBoundary--;
                            }
                            prevCC = 0;
                            prevSrc = src;
                        }
                        src += Character.charCount(c);
                        if (!isMaybeOrNonZeroCC(norm16)) {
                            break;
                        }
                        int cc = getCCFromYesOrMaybe(norm16);
                        if ((onlyContiguous && cc != 0 && prevCC == 0 && prevBoundary < prevSrc && getTrailCCFromCompYesAndZeroCC(s, prevBoundary, prevSrc) > cc) || (prevCC > cc && cc != 0)) {
                            break;
                        }
                        prevCC = cc;
                        if (norm16 < MIN_YES_YES_WITH_CC) {
                            if (doSpan) {
                                return prevBoundary << 1;
                            }
                            qcResult = 1;
                        }
                    }
                }
                src++;
            }
            return (src << 1) | qcResult;
        }
        return prevBoundary << 1;
    }

    public void composeAndAppend(CharSequence s, boolean doCompose, boolean onlyContiguous, ReorderingBuffer buffer) {
        int src = 0;
        int limit = s.length();
        if (!buffer.isEmpty()) {
            int firstStarterInSrc = findNextCompBoundary(s, 0, limit);
            if (firstStarterInSrc != 0) {
                int lastStarterInDest = findPreviousCompBoundary(buffer.getStringBuilder(), buffer.length());
                StringBuilder middle = new StringBuilder(((buffer.length() - lastStarterInDest) + firstStarterInSrc) + 16);
                middle.append(buffer.getStringBuilder(), lastStarterInDest, buffer.length());
                buffer.removeSuffix(buffer.length() - lastStarterInDest);
                middle.append(s, 0, firstStarterInSrc);
                compose(middle, 0, middle.length(), onlyContiguous, true, buffer);
                src = firstStarterInSrc;
            }
        }
        if (doCompose) {
            compose(s, src, limit, onlyContiguous, true, buffer);
        } else {
            buffer.append(s, src, limit);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:58:0x00da  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0093  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int makeFCD(CharSequence s, int src, int limit, ReorderingBuffer buffer) {
        int prevBoundary = src;
        int c = 0;
        int prevFCD16 = 0;
        int fcd16 = 0;
        while (true) {
            int prevSrc = src;
            while (src != limit) {
                c = s.charAt(src);
                if (c >= 768) {
                    if (singleLeadMightHaveNonZeroFCD16(c)) {
                        if (UTF16.isSurrogate((char) c)) {
                            char c2;
                            if (UTF16Plus.isSurrogateLead(c)) {
                                if (src + 1 != limit) {
                                    c2 = s.charAt(src + 1);
                                    if (Character.isLowSurrogate(c2)) {
                                        c = Character.toCodePoint((char) c, c2);
                                    }
                                }
                            } else if (prevSrc < src) {
                                c2 = s.charAt(src - 1);
                                if (Character.isHighSurrogate(c2)) {
                                    src--;
                                    c = Character.toCodePoint(c2, (char) c);
                                }
                            }
                        }
                        fcd16 = getFCD16FromNormData(c);
                        if (fcd16 > 255) {
                            break;
                        }
                        prevFCD16 = fcd16;
                        src += Character.charCount(c);
                    } else {
                        prevFCD16 = 0;
                        src++;
                    }
                } else {
                    prevFCD16 = ~c;
                    src++;
                }
            }
            if (src == prevSrc) {
                if (src == limit) {
                    break;
                }
                src += Character.charCount(c);
                if ((prevFCD16 & 255) > (fcd16 >> 8)) {
                }
            } else if (src != limit) {
                prevBoundary = src;
                if (prevFCD16 < 0) {
                    int prev = ~prevFCD16;
                    prevFCD16 = prev < CollationFastLatin.LATIN_LIMIT ? this.tccc180[prev] : getFCD16FromNormData(prev);
                    if (prevFCD16 > 1) {
                        prevBoundary--;
                    }
                } else {
                    int p = src - 1;
                    if (Character.isLowSurrogate(s.charAt(p)) && prevSrc < p && Character.isHighSurrogate(s.charAt(p - 1))) {
                        p--;
                        prevFCD16 = getFCD16FromNormData(Character.toCodePoint(s.charAt(p), s.charAt(p + 1)));
                    }
                    if (prevFCD16 > 1) {
                        prevBoundary = p;
                    }
                }
                if (buffer != null) {
                    buffer.flushAndAppendZeroCC(s, prevSrc, prevBoundary);
                    buffer.append(s, prevBoundary, src);
                }
                prevSrc = src;
                src += Character.charCount(c);
                if ((prevFCD16 & 255) > (fcd16 >> 8)) {
                    if ((fcd16 & 255) <= 1) {
                        prevBoundary = src;
                    }
                    if (buffer != null) {
                        buffer.appendZeroCC(c);
                    }
                    prevFCD16 = fcd16;
                } else if (buffer == null) {
                    return prevBoundary;
                } else {
                    buffer.removeSuffix(prevSrc - prevBoundary);
                    src = findNextFCDBoundary(s, src, limit);
                    decomposeShort(s, prevBoundary, src, buffer);
                    prevBoundary = src;
                    prevFCD16 = 0;
                }
            } else if (buffer != null) {
                buffer.flushAndAppendZeroCC(s, prevSrc, src);
            }
        }
        return src;
    }

    public void makeFCDAndAppend(CharSequence s, boolean doMakeFCD, ReorderingBuffer buffer) {
        int src = 0;
        int limit = s.length();
        if (!buffer.isEmpty()) {
            int firstBoundaryInSrc = findNextFCDBoundary(s, 0, limit);
            if (firstBoundaryInSrc != 0) {
                int lastBoundaryInDest = findPreviousFCDBoundary(buffer.getStringBuilder(), buffer.length());
                StringBuilder middle = new StringBuilder(((buffer.length() - lastBoundaryInDest) + firstBoundaryInSrc) + 16);
                middle.append(buffer.getStringBuilder(), lastBoundaryInDest, buffer.length());
                buffer.removeSuffix(buffer.length() - lastBoundaryInDest);
                middle.append(s, 0, firstBoundaryInSrc);
                makeFCD(middle, 0, middle.length(), buffer);
                src = firstBoundaryInSrc;
            }
        }
        if (doMakeFCD) {
            makeFCD(s, src, limit, buffer);
        } else {
            buffer.append(s, src, limit);
        }
    }

    public boolean hasDecompBoundary(int c, boolean before) {
        boolean z = true;
        while (c >= this.minDecompNoCP) {
            int norm16 = getNorm16(c);
            if (isHangul(norm16) || isDecompYesAndZeroCC(norm16)) {
                return true;
            }
            if (norm16 > MIN_NORMAL_MAYBE_YES) {
                return false;
            }
            if (isDecompNoAlgorithmic(norm16)) {
                c = mapAlgorithmic(c, norm16);
            } else {
                int firstUnit = this.extraData.charAt(norm16);
                if ((firstUnit & 31) == 0) {
                    return false;
                }
                if (!before) {
                    if (firstUnit > Opcodes.OP_CHECK_CAST_JUMBO) {
                        return false;
                    }
                    if (firstUnit <= 255) {
                        return true;
                    }
                }
                if (!((firstUnit & 128) == 0 || (this.extraData.charAt(norm16 - 1) & JAMO_VT) == 0)) {
                    z = false;
                }
                return z;
            }
        }
        return true;
    }

    public boolean isDecompInert(int c) {
        return isDecompYesAndZeroCC(getNorm16(c));
    }

    public boolean hasCompBoundaryBefore(int c) {
        return c >= this.minCompNoMaybeCP ? hasCompBoundaryBefore(c, getNorm16(c)) : true;
    }

    public boolean hasCompBoundaryAfter(int c, boolean onlyContiguous, boolean testInert) {
        boolean z = false;
        while (true) {
            int norm16 = getNorm16(c);
            if (isInert(norm16)) {
                return true;
            }
            if (norm16 <= this.minYesNo) {
                if (isHangul(norm16)) {
                    z = Hangul.isHangulWithoutJamoT((char) c) ^ 1;
                }
                return z;
            }
            if (norm16 >= (testInert ? this.minNoNo : this.minMaybeYes)) {
                return false;
            }
            if (isDecompNoAlgorithmic(norm16)) {
                c = mapAlgorithmic(c, norm16);
            } else {
                int firstUnit = this.extraData.charAt(norm16);
                if ((firstUnit & 32) == 0 && (!onlyContiguous || firstUnit <= Opcodes.OP_CHECK_CAST_JUMBO)) {
                    z = true;
                }
                return z;
            }
        }
    }

    public boolean hasFCDBoundaryBefore(int c) {
        return c < 768 || getFCD16(c) <= 255;
    }

    public boolean hasFCDBoundaryAfter(int c) {
        int fcd16 = getFCD16(c);
        if (fcd16 <= 1 || (fcd16 & 255) == 0) {
            return true;
        }
        return false;
    }

    public boolean isFCDInert(int c) {
        return getFCD16(c) <= 1;
    }

    private boolean isMaybe(int norm16) {
        return this.minMaybeYes <= norm16 && norm16 <= JAMO_VT;
    }

    private boolean isMaybeOrNonZeroCC(int norm16) {
        return norm16 >= this.minMaybeYes;
    }

    private static boolean isInert(int norm16) {
        return norm16 == 0;
    }

    private static boolean isJamoL(int norm16) {
        return norm16 == 1;
    }

    private static boolean isJamoVT(int norm16) {
        return norm16 == JAMO_VT;
    }

    private boolean isHangul(int norm16) {
        return norm16 == this.minYesNo;
    }

    private boolean isCompYesAndZeroCC(int norm16) {
        return norm16 < this.minNoNo;
    }

    private boolean isDecompYesAndZeroCC(int norm16) {
        if (norm16 < this.minYesNo || norm16 == JAMO_VT) {
            return true;
        }
        if (this.minMaybeYes > norm16 || norm16 > MIN_NORMAL_MAYBE_YES) {
            return false;
        }
        return true;
    }

    private boolean isMostDecompYesAndZeroCC(int norm16) {
        return norm16 < this.minYesNo || norm16 == MIN_NORMAL_MAYBE_YES || norm16 == JAMO_VT;
    }

    private boolean isDecompNoAlgorithmic(int norm16) {
        return norm16 >= this.limitNoNo;
    }

    private int getCCFromNoNo(int norm16) {
        if ((this.extraData.charAt(norm16) & 128) != 0) {
            return this.extraData.charAt(norm16 - 1) & 255;
        }
        return 0;
    }

    int getTrailCCFromCompYesAndZeroCC(CharSequence s, int cpStart, int cpLimit) {
        int c;
        if (cpStart == cpLimit - 1) {
            c = s.charAt(cpStart);
        } else {
            c = Character.codePointAt(s, cpStart);
        }
        int prevNorm16 = getNorm16(c);
        if (prevNorm16 <= this.minYesNo) {
            return 0;
        }
        return this.extraData.charAt(prevNorm16) >> 8;
    }

    private int mapAlgorithmic(int c, int norm16) {
        return (c + norm16) - ((this.minMaybeYes - 64) - 1);
    }

    private int getCompositionsListForDecompYes(int norm16) {
        if (norm16 == 0 || MIN_NORMAL_MAYBE_YES <= norm16) {
            return -1;
        }
        norm16 -= this.minMaybeYes;
        if (norm16 < 0) {
            norm16 += MIN_NORMAL_MAYBE_YES;
        }
        return norm16;
    }

    private int getCompositionsListForComposite(int norm16) {
        return (((MIN_NORMAL_MAYBE_YES - this.minMaybeYes) + norm16) + 1) + (this.extraData.charAt(norm16) & 31);
    }

    private int getCompositionsList(int norm16) {
        if (isDecompYes(norm16)) {
            return getCompositionsListForDecompYes(norm16);
        }
        return getCompositionsListForComposite(norm16);
    }

    public void decomposeShort(CharSequence s, int src, int limit, ReorderingBuffer buffer) {
        while (src < limit) {
            int c = Character.codePointAt(s, src);
            src += Character.charCount(c);
            decompose(c, getNorm16(c), buffer);
        }
    }

    private void decompose(int c, int norm16, ReorderingBuffer buffer) {
        while (!isDecompYes(norm16)) {
            if (isHangul(norm16)) {
                Hangul.decompose(c, buffer);
                return;
            } else if (isDecompNoAlgorithmic(norm16)) {
                c = mapAlgorithmic(c, norm16);
                norm16 = getNorm16(c);
            } else {
                int leadCC;
                int firstUnit = this.extraData.charAt(norm16);
                int length = firstUnit & 31;
                int trailCC = firstUnit >> 8;
                if ((firstUnit & 128) != 0) {
                    leadCC = this.extraData.charAt(norm16 - 1) >> 8;
                } else {
                    leadCC = 0;
                }
                norm16++;
                buffer.append(this.extraData, norm16, norm16 + length, leadCC, trailCC);
                return;
            }
        }
        buffer.append(c, getCCFromYesOrMaybe(norm16));
    }

    private static int combine(String compositions, int list, int trail) {
        int key1;
        int firstUnit;
        if (trail >= COMP_1_TRAIL_LIMIT) {
            key1 = ((trail >> 9) & -2) + COMP_1_TRAIL_LIMIT;
            int key2 = (trail << 6) & DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH;
            while (true) {
                firstUnit = compositions.charAt(list);
                if (key1 <= firstUnit) {
                    if (key1 != (firstUnit & COMP_1_TRAIL_MASK)) {
                        break;
                    }
                    int secondUnit = compositions.charAt(list + 1);
                    if (key2 > secondUnit) {
                        if ((32768 & firstUnit) != 0) {
                            break;
                        }
                        list += 3;
                    } else if (key2 == (COMP_2_TRAIL_MASK & secondUnit)) {
                        return ((-65473 & secondUnit) << 16) | compositions.charAt(list + 2);
                    }
                } else {
                    list += (firstUnit & 1) + 2;
                }
            }
        } else {
            key1 = trail << 1;
            while (true) {
                firstUnit = compositions.charAt(list);
                if (key1 <= firstUnit) {
                    break;
                }
                list += (firstUnit & 1) + 2;
            }
            if (key1 == (firstUnit & COMP_1_TRAIL_MASK)) {
                if ((firstUnit & 1) != 0) {
                    return (compositions.charAt(list + 1) << 16) | compositions.charAt(list + 2);
                }
                return compositions.charAt(list + 1);
            }
        }
        return -1;
    }

    private void addComposites(int list, UnicodeSet set) {
        int firstUnit;
        do {
            int compositeAndFwd;
            firstUnit = this.maybeYesCompositions.charAt(list);
            if ((firstUnit & 1) == 0) {
                compositeAndFwd = this.maybeYesCompositions.charAt(list + 1);
                list += 2;
            } else {
                compositeAndFwd = ((this.maybeYesCompositions.charAt(list + 1) & -65473) << 16) | this.maybeYesCompositions.charAt(list + 2);
                list += 3;
            }
            int composite = compositeAndFwd >> 1;
            if ((compositeAndFwd & 1) != 0) {
                addComposites(getCompositionsListForComposite(getNorm16(composite)), set);
            }
            set.add(composite);
        } while ((32768 & firstUnit) == 0);
    }

    private void recompose(ReorderingBuffer buffer, int recomposeStartIndex, boolean onlyContiguous) {
        StringBuilder sb = buffer.getStringBuilder();
        int p = recomposeStartIndex;
        if (recomposeStartIndex != sb.length()) {
            int compositionsList = -1;
            int starter = -1;
            boolean starterIsSupplementary = false;
            int prevCC = 0;
            while (true) {
                int c = sb.codePointAt(p);
                p += Character.charCount(c);
                int norm16 = getNorm16(c);
                int cc = getCCFromYesOrMaybe(norm16);
                if (isMaybe(norm16) && compositionsList >= 0 && (prevCC < cc || prevCC == 0)) {
                    int pRemove;
                    if (!isJamoVT(norm16)) {
                        int compositeAndFwd = combine(this.maybeYesCompositions, compositionsList, c);
                        if (compositeAndFwd >= 0) {
                            int composite = compositeAndFwd >> 1;
                            pRemove = p - Character.charCount(c);
                            sb.delete(pRemove, p);
                            p = pRemove;
                            if (starterIsSupplementary) {
                                if (composite > 65535) {
                                    sb.setCharAt(starter, UTF16.getLeadSurrogate(composite));
                                    sb.setCharAt(starter + 1, UTF16.getTrailSurrogate(composite));
                                } else {
                                    sb.setCharAt(starter, (char) c);
                                    sb.deleteCharAt(starter + 1);
                                    starterIsSupplementary = false;
                                    p = pRemove - 1;
                                }
                            } else if (composite > 65535) {
                                starterIsSupplementary = true;
                                sb.setCharAt(starter, UTF16.getLeadSurrogate(composite));
                                sb.insert(starter + 1, UTF16.getTrailSurrogate(composite));
                                p = pRemove + 1;
                            } else {
                                sb.setCharAt(starter, (char) composite);
                            }
                            if (p == sb.length()) {
                                break;
                            } else if ((compositeAndFwd & 1) != 0) {
                                compositionsList = getCompositionsListForComposite(getNorm16(composite));
                            } else {
                                compositionsList = -1;
                            }
                        }
                    } else {
                        if (c < 4519) {
                            char prev = (char) (sb.charAt(starter) - 4352);
                            if (prev < 19) {
                                pRemove = p - 1;
                                char syllable = (char) ((((prev * 21) + (c - 4449)) * 28) + Hangul.HANGUL_BASE);
                                if (p != sb.length()) {
                                    char t = (char) (sb.charAt(p) - 4519);
                                    if (t < 28) {
                                        p++;
                                        syllable = (char) (syllable + t);
                                    }
                                }
                                sb.setCharAt(starter, syllable);
                                sb.delete(pRemove, p);
                                p = pRemove;
                            }
                        }
                        if (p == sb.length()) {
                            break;
                        }
                        compositionsList = -1;
                    }
                }
                prevCC = cc;
                if (p == sb.length()) {
                    break;
                } else if (cc == 0) {
                    compositionsList = getCompositionsListForDecompYes(norm16);
                    if (compositionsList >= 0) {
                        if (c <= 65535) {
                            starterIsSupplementary = false;
                            starter = p - 1;
                        } else {
                            starterIsSupplementary = true;
                            starter = p - 2;
                        }
                    }
                } else if (onlyContiguous) {
                    compositionsList = -1;
                }
            }
            buffer.flush();
        }
    }

    public int composePair(int a, int b) {
        int norm16 = getNorm16(a);
        if (isInert(norm16)) {
            return -1;
        }
        int list;
        if (norm16 < this.minYesNoMappingsOnly) {
            if (isJamoL(norm16)) {
                b -= 4449;
                if (b < 0 || b >= 21) {
                    return -1;
                }
                return ((((a - 4352) * 21) + b) * 28) + Hangul.HANGUL_BASE;
            } else if (isHangul(norm16)) {
                b -= 4519;
                if (!Hangul.isHangulWithoutJamoT((char) a) || b <= 0 || b >= 28) {
                    return -1;
                }
                return a + b;
            } else {
                list = norm16;
                if (norm16 > this.minYesNo) {
                    list = norm16 + ((this.extraData.charAt(norm16) & 31) + 1);
                }
                list += MIN_NORMAL_MAYBE_YES - this.minMaybeYes;
            }
        } else if (norm16 < this.minMaybeYes || MIN_NORMAL_MAYBE_YES <= norm16) {
            return -1;
        } else {
            list = norm16 - this.minMaybeYes;
        }
        if (b < 0 || 1114111 < b) {
            return -1;
        }
        return combine(this.maybeYesCompositions, list, b) >> 1;
    }

    private boolean hasCompBoundaryBefore(int c, int norm16) {
        while (!isCompYesAndZeroCC(norm16)) {
            if (isMaybeOrNonZeroCC(norm16)) {
                return false;
            }
            if (isDecompNoAlgorithmic(norm16)) {
                c = mapAlgorithmic(c, norm16);
                norm16 = getNorm16(c);
            } else {
                int firstUnit = this.extraData.charAt(norm16);
                if ((firstUnit & 31) == 0) {
                    return false;
                }
                if ((firstUnit & 128) == 0 || (this.extraData.charAt(norm16 - 1) & JAMO_VT) == 0) {
                    return isCompYesAndZeroCC(getNorm16(Character.codePointAt(this.extraData, norm16 + 1)));
                }
                return false;
            }
        }
        return true;
    }

    private int findPreviousCompBoundary(CharSequence s, int p) {
        while (p > 0) {
            int c = Character.codePointBefore(s, p);
            p -= Character.charCount(c);
            if (hasCompBoundaryBefore(c)) {
                break;
            }
        }
        return p;
    }

    private int findNextCompBoundary(CharSequence s, int p, int limit) {
        while (p < limit) {
            int c = Character.codePointAt(s, p);
            if (hasCompBoundaryBefore(c, this.normTrie.get(c))) {
                break;
            }
            p += Character.charCount(c);
        }
        return p;
    }

    private int findPreviousFCDBoundary(CharSequence s, int p) {
        while (p > 0) {
            int c = Character.codePointBefore(s, p);
            p -= Character.charCount(c);
            if (c >= 768) {
                if (getFCD16(c) <= 255) {
                    break;
                }
            }
            break;
        }
        return p;
    }

    private int findNextFCDBoundary(CharSequence s, int p, int limit) {
        while (p < limit) {
            int c = Character.codePointAt(s, p);
            if (c < 768 || getFCD16(c) <= 255) {
                break;
            }
            p += Character.charCount(c);
        }
        return p;
    }

    private void addToStartSet(Trie2Writable newData, int origin, int decompLead) {
        int canonValue = newData.get(decompLead);
        if ((4194303 & canonValue) != 0 || origin == 0) {
            UnicodeSet set;
            if ((canonValue & 2097152) == 0) {
                int firstOrigin = canonValue & 2097151;
                newData.set(decompLead, ((-2097152 & canonValue) | 2097152) | this.canonStartSets.size());
                ArrayList arrayList = this.canonStartSets;
                set = new UnicodeSet();
                arrayList.add(set);
                if (firstOrigin != 0) {
                    set.add(firstOrigin);
                }
            } else {
                set = (UnicodeSet) this.canonStartSets.get(2097151 & canonValue);
            }
            set.add(origin);
            return;
        }
        newData.set(decompLead, canonValue | origin);
    }
}
