package android.icu.impl;

import android.icu.impl.ICUBinary.Authenticate;
import android.icu.impl.Trie2.Range;
import android.icu.impl.Trie2.ValueMapper;
import android.icu.impl.coll.CollationFastLatin;
import android.icu.text.UTF16;
import android.icu.text.UnicodeSet;
import android.icu.util.AnnualTimeZoneRule;
import android.icu.util.ICUUncheckedIOException;
import android.icu.util.VersionInfo;
import com.android.dex.DexFormat;
import dalvik.bytecode.Opcodes;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import org.w3c.dom.traversal.NodeFilter;

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
    private static final IsAcceptable IS_ACCEPTABLE = null;
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
    private static final ValueMapper segmentStarterMapper = null;
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
            if (c >= '\u2ba4' || c % JAMO_T_COUNT != 0) {
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
                int c2 = c % JAMO_T_COUNT;
                c /= JAMO_T_COUNT;
                buffer.append((char) ((c / JAMO_V_COUNT) + JAMO_L_BASE));
                buffer.append((char) ((c % JAMO_V_COUNT) + JAMO_V_BASE));
                if (c2 == 0) {
                    return Normalizer2Impl.IX_SMALL_FCD_OFFSET;
                }
                buffer.append((char) (c2 + JAMO_T_BASE));
                return Normalizer2Impl.IX_RESERVED3_OFFSET;
            } catch (Throwable e) {
                throw new ICUUncheckedIOException(e);
            }
        }

        public static void getRawDecomposition(int c, Appendable buffer) {
            int orig = c;
            c -= HANGUL_BASE;
            try {
                int c2 = c % JAMO_T_COUNT;
                if (c2 == 0) {
                    c /= JAMO_T_COUNT;
                    buffer.append((char) ((c / JAMO_V_COUNT) + JAMO_L_BASE));
                    buffer.append((char) ((c % JAMO_V_COUNT) + JAMO_V_BASE));
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
        private IsAcceptable() {
        }

        public boolean isDataVersionAcceptable(byte[] version) {
            return version[Normalizer2Impl.IX_NORM_TRIE_OFFSET] == Normalizer2Impl.IX_SMALL_FCD_OFFSET;
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
                this.reorderStart = Normalizer2Impl.IX_NORM_TRIE_OFFSET;
                if (this.str.length() == 0) {
                    this.lastCC = Normalizer2Impl.IX_NORM_TRIE_OFFSET;
                    return;
                }
                setIterator();
                this.lastCC = previousCC();
                if (this.lastCC > Normalizer2Impl.JAMO_L) {
                    do {
                    } while (previousCC() > Normalizer2Impl.JAMO_L);
                }
                this.reorderStart = this.codePointLimit;
                return;
            }
            this.appIsStringBuilder = false;
            this.str = new StringBuilder();
            this.reorderStart = Normalizer2Impl.IX_NORM_TRIE_OFFSET;
            this.lastCC = Normalizer2Impl.IX_NORM_TRIE_OFFSET;
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
            return UTF16Plus.equal(this.str, Normalizer2Impl.IX_NORM_TRIE_OFFSET, this.str.length(), s, start, limit);
        }

        public void setLastChar(char c) {
            this.str.setCharAt(this.str.length() - 1, c);
        }

        public void append(int c, int cc) {
            if (this.lastCC <= cc || cc == 0) {
                this.str.appendCodePoint(c);
                this.lastCC = cc;
                if (cc <= Normalizer2Impl.JAMO_L) {
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
                    if (trailCC <= Normalizer2Impl.JAMO_L) {
                        this.reorderStart = this.str.length() + (limit - start);
                    } else if (leadCC <= Normalizer2Impl.JAMO_L) {
                        this.reorderStart = this.str.length() + Normalizer2Impl.JAMO_L;
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
            this.lastCC = Normalizer2Impl.IX_NORM_TRIE_OFFSET;
            this.reorderStart = this.str.length();
            return this;
        }

        public void appendZeroCC(int c) {
            this.str.appendCodePoint(c);
            this.lastCC = Normalizer2Impl.IX_NORM_TRIE_OFFSET;
            this.reorderStart = this.str.length();
        }

        public ReorderingBuffer append(CharSequence s) {
            if (s.length() != 0) {
                this.str.append(s);
                this.lastCC = Normalizer2Impl.IX_NORM_TRIE_OFFSET;
                this.reorderStart = this.str.length();
            }
            return this;
        }

        public ReorderingBuffer append(CharSequence s, int start, int limit) {
            if (start != limit) {
                this.str.append(s, start, limit);
                this.lastCC = Normalizer2Impl.IX_NORM_TRIE_OFFSET;
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
                    this.str.setLength(Normalizer2Impl.IX_NORM_TRIE_OFFSET);
                    this.reorderStart = Normalizer2Impl.IX_NORM_TRIE_OFFSET;
                } catch (Throwable e) {
                    throw new ICUUncheckedIOException(e);
                }
            }
            this.lastCC = Normalizer2Impl.IX_NORM_TRIE_OFFSET;
        }

        public ReorderingBuffer flushAndAppendZeroCC(CharSequence s, int start, int limit) {
            if (this.appIsStringBuilder) {
                this.str.append(s, start, limit);
                this.reorderStart = this.str.length();
            } else {
                try {
                    this.app.append(this.str).append(s, start, limit);
                    this.str.setLength(Normalizer2Impl.IX_NORM_TRIE_OFFSET);
                    this.reorderStart = Normalizer2Impl.IX_NORM_TRIE_OFFSET;
                } catch (Throwable e) {
                    throw new ICUUncheckedIOException(e);
                }
            }
            this.lastCC = Normalizer2Impl.IX_NORM_TRIE_OFFSET;
            return this;
        }

        public void remove() {
            this.str.setLength(Normalizer2Impl.IX_NORM_TRIE_OFFSET);
            this.lastCC = Normalizer2Impl.IX_NORM_TRIE_OFFSET;
            this.reorderStart = Normalizer2Impl.IX_NORM_TRIE_OFFSET;
        }

        public void removeSuffix(int suffixLength) {
            int oldLength = this.str.length();
            this.str.delete(oldLength - suffixLength, oldLength);
            this.lastCC = Normalizer2Impl.IX_NORM_TRIE_OFFSET;
            this.reorderStart = this.str.length();
        }

        private void insert(int c, int cc) {
            setIterator();
            skipPrevious();
            do {
            } while (previousCC() > cc);
            if (c <= DexFormat.MAX_TYPE_IDX) {
                this.str.insert(this.codePointLimit, (char) c);
                if (cc <= Normalizer2Impl.JAMO_L) {
                    this.reorderStart = this.codePointLimit + Normalizer2Impl.JAMO_L;
                    return;
                }
                return;
            }
            this.str.insert(this.codePointLimit, Character.toChars(c));
            if (cc <= Normalizer2Impl.JAMO_L) {
                this.reorderStart = this.codePointLimit + Normalizer2Impl.IX_SMALL_FCD_OFFSET;
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
                return Normalizer2Impl.IX_NORM_TRIE_OFFSET;
            }
            int c = this.str.codePointBefore(this.codePointStart);
            this.codePointStart -= Character.charCount(c);
            if (c < Normalizer2Impl.MIN_CCC_LCCC_CP) {
                return Normalizer2Impl.IX_NORM_TRIE_OFFSET;
            }
            return Normalizer2Impl.getCCFromYesOrMaybe(this.impl.getNorm16(c));
        }
    }

    public static final class UTF16Plus {
        public static boolean isSurrogateLead(int c) {
            return (c & NodeFilter.SHOW_DOCUMENT_FRAGMENT) == 0;
        }

        public static boolean equal(CharSequence s1, CharSequence s2) {
            if (s1 == s2) {
                return true;
            }
            int length = s1.length();
            if (length != s2.length()) {
                return false;
            }
            for (int i = Normalizer2Impl.IX_NORM_TRIE_OFFSET; i < length; i += Normalizer2Impl.JAMO_L) {
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
                start1 = start12 + Normalizer2Impl.JAMO_L;
                start2 = start22 + Normalizer2Impl.JAMO_L;
                if (s1.charAt(start12) != s2.charAt(start22)) {
                    return false;
                }
                start22 = start2;
                start12 = start1;
            }
            return true;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.Normalizer2Impl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.Normalizer2Impl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.Normalizer2Impl.<clinit>():void");
    }

    public int makeFCD(java.lang.CharSequence r1, int r2, int r3, android.icu.impl.Normalizer2Impl.ReorderingBuffer r4) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.Normalizer2Impl.makeFCD(java.lang.CharSequence, int, int, android.icu.impl.Normalizer2Impl$ReorderingBuffer):int
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.Normalizer2Impl.makeFCD(java.lang.CharSequence, int, int, android.icu.impl.Normalizer2Impl$ReorderingBuffer):int");
    }

    public Normalizer2Impl load(ByteBuffer bytes) {
        try {
            this.dataVersion = ICUBinary.readHeaderAndDataVersion(bytes, DATA_FORMAT, IS_ACCEPTABLE);
            int indexesLength = bytes.getInt() / 4;
            if (indexesLength <= IX_MIN_MAYBE_YES) {
                throw new ICUUncheckedIOException("Normalizer2 data: not enough indexes");
            }
            int i;
            int[] inIndexes = new int[indexesLength];
            inIndexes[IX_NORM_TRIE_OFFSET] = indexesLength * 4;
            for (i = JAMO_L; i < indexesLength; i += JAMO_L) {
                inIndexes[i] = bytes.getInt();
            }
            this.minDecompNoCP = inIndexes[IX_MIN_DECOMP_NO_CP];
            this.minCompNoMaybeCP = inIndexes[IX_MIN_COMP_NO_MAYBE_CP];
            this.minYesNo = inIndexes[IX_MIN_YES_NO];
            this.minYesNoMappingsOnly = inIndexes[IX_MIN_YES_NO_MAPPINGS_ONLY];
            this.minNoNo = inIndexes[IX_MIN_NO_NO];
            this.limitNoNo = inIndexes[IX_LIMIT_NO_NO];
            this.minMaybeYes = inIndexes[IX_MIN_MAYBE_YES];
            int offset = inIndexes[IX_NORM_TRIE_OFFSET];
            int nextOffset = inIndexes[JAMO_L];
            this.normTrie = Trie2_16.createFromSerialized(bytes);
            int trieLength = this.normTrie.getSerializedLength();
            if (trieLength > nextOffset - offset) {
                throw new ICUUncheckedIOException("Normalizer2 data: not enough bytes for normTrie");
            }
            ICUBinary.skipBytes(bytes, (nextOffset - offset) - trieLength);
            offset = nextOffset;
            nextOffset = inIndexes[IX_SMALL_FCD_OFFSET];
            int numChars = (nextOffset - offset) / IX_SMALL_FCD_OFFSET;
            if (numChars != 0) {
                this.maybeYesCompositions = ICUBinary.getString(bytes, numChars, IX_NORM_TRIE_OFFSET);
                this.extraData = this.maybeYesCompositions.substring(MIN_NORMAL_MAYBE_YES - this.minMaybeYes);
            }
            offset = nextOffset;
            this.smallFCD = new byte[NodeFilter.SHOW_DOCUMENT];
            bytes.get(this.smallFCD);
            this.tccc180 = new int[CollationFastLatin.LATIN_LIMIT];
            int bits = IX_NORM_TRIE_OFFSET;
            int c = IX_NORM_TRIE_OFFSET;
            while (c < CollationFastLatin.LATIN_LIMIT) {
                if ((c & Opcodes.OP_CONST_CLASS_JUMBO) == 0) {
                    bits = this.smallFCD[c >> IX_MIN_DECOMP_NO_CP];
                }
                if ((bits & JAMO_L) != 0) {
                    i = IX_NORM_TRIE_OFFSET;
                    while (i < MAPPING_NO_COMP_BOUNDARY_AFTER) {
                        this.tccc180[c] = getFCD16FromNormData(c) & Opcodes.OP_CONST_CLASS_JUMBO;
                        i += JAMO_L;
                        c += JAMO_L;
                    }
                } else {
                    c += MAPPING_NO_COMP_BOUNDARY_AFTER;
                }
                bits >>= JAMO_L;
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
                if (getFCD16(start) > Opcodes.OP_CONST_CLASS_JUMBO) {
                    set.add(start);
                }
                start += JAMO_L;
            } while (start <= end);
        } else if (getFCD16(start) > Opcodes.OP_CONST_CLASS_JUMBO) {
            set.add(start, end);
        }
    }

    private void enumNorm16PropertyStartsRange(int start, int end, int value, UnicodeSet set) {
        set.add(start);
        if (start != end && isAlgorithmicNoNo(value)) {
            int prevFCD16 = getFCD16(start);
            while (true) {
                start += JAMO_L;
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
            if (!range.leadSurrogate) {
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
            if (range.leadSurrogate) {
                break;
            }
            enumNorm16PropertyStartsRange(range.startCodePoint, range.endCodePoint, range.value, set);
        }
        for (int c = Hangul.HANGUL_BASE; c < Hangul.HANGUL_LIMIT; c += 28) {
            set.add(c);
            set.add(c + JAMO_L);
        }
        set.add((int) Hangul.HANGUL_LIMIT);
    }

    public void addCanonIterPropertyStarts(UnicodeSet set) {
        ensureCanonIterData();
        Iterator<Range> trieIterator = this.canonIterData.iterator(segmentStarterMapper);
        while (trieIterator.hasNext()) {
            Range range = (Range) trieIterator.next();
            if (!range.leadSurrogate) {
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
            Trie2Writable newData = new Trie2Writable(IX_NORM_TRIE_OFFSET, IX_NORM_TRIE_OFFSET);
            this.canonStartSets = new ArrayList();
            Iterator<Range> trieIterator = this.normTrie.iterator();
            while (trieIterator.hasNext()) {
                Range range = (Range) trieIterator.next();
                if (range.leadSurrogate) {
                    break;
                }
                int norm16 = range.value;
                if (norm16 != 0 && (this.minYesNo > norm16 || norm16 >= this.minNoNo)) {
                    int c = range.startCodePoint;
                    while (c <= range.endCodePoint) {
                        int oldValue = newData.get(c);
                        int newValue = oldValue;
                        if (norm16 < this.minMaybeYes) {
                            if (norm16 >= this.minYesNo) {
                                int c2 = c;
                                int norm16_2 = norm16;
                                while (this.limitNoNo <= norm16_2 && norm16_2 < this.minMaybeYes) {
                                    c2 = mapAlgorithmic(c2, norm16_2);
                                    norm16_2 = getNorm16(c2);
                                }
                                if (this.minYesNo <= norm16_2 && norm16_2 < this.limitNoNo) {
                                    int firstUnit = this.extraData.charAt(norm16_2);
                                    int length = firstUnit & MAPPING_LENGTH_MASK;
                                    if (!((firstUnit & MAPPING_HAS_CCC_LCCC_WORD) == 0 || c != c2 || (this.extraData.charAt(norm16_2 - 1) & Opcodes.OP_CONST_CLASS_JUMBO) == 0)) {
                                        newValue = oldValue | CANON_NOT_SEGMENT_STARTER;
                                    }
                                    if (length != 0) {
                                        norm16_2 += JAMO_L;
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
                                                if ((CANON_NOT_SEGMENT_STARTER & c2Value) == 0) {
                                                    newData.set(c2, CANON_NOT_SEGMENT_STARTER | c2Value);
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    addToStartSet(newData, c, c2);
                                }
                            } else {
                                newValue = oldValue | CANON_HAS_COMPOSITIONS;
                            }
                        } else {
                            newValue = oldValue | CANON_NOT_SEGMENT_STARTER;
                            if (norm16 < MIN_NORMAL_MAYBE_YES) {
                                newValue |= CANON_HAS_COMPOSITIONS;
                            }
                        }
                        if (newValue != oldValue) {
                            newData.set(c, newValue);
                        }
                        c += JAMO_L;
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
            return JAMO_L;
        }
        if (this.minMaybeYes <= norm16) {
            return IX_SMALL_FCD_OFFSET;
        }
        return IX_NORM_TRIE_OFFSET;
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
            return norm16 & Opcodes.OP_CONST_CLASS_JUMBO;
        }
        if (norm16 < this.minNoNo || this.limitNoNo <= norm16) {
            return IX_NORM_TRIE_OFFSET;
        }
        return getCCFromNoNo(norm16);
    }

    public static int getCCFromYesOrMaybe(int norm16) {
        return norm16 >= MIN_NORMAL_MAYBE_YES ? norm16 & Opcodes.OP_CONST_CLASS_JUMBO : IX_NORM_TRIE_OFFSET;
    }

    public int getFCD16(int c) {
        if (c < 0) {
            return IX_NORM_TRIE_OFFSET;
        }
        if (c < CollationFastLatin.LATIN_LIMIT) {
            return this.tccc180[c];
        }
        if (c > DexFormat.MAX_TYPE_IDX || singleLeadMightHaveNonZeroFCD16(c)) {
            return getFCD16FromNormData(c);
        }
        return IX_NORM_TRIE_OFFSET;
    }

    public int getFCD16FromBelow180(int c) {
        return this.tccc180[c];
    }

    public boolean singleLeadMightHaveNonZeroFCD16(int lead) {
        boolean z = false;
        byte bits = this.smallFCD[lead >> IX_MIN_DECOMP_NO_CP];
        if (bits == null) {
            return false;
        }
        if (((bits >> ((lead >> 5) & IX_TOTAL_SIZE)) & JAMO_L) != 0) {
            z = true;
        }
        return z;
    }

    public int getFCD16FromNormData(int c) {
        int norm16;
        while (true) {
            norm16 = getNorm16(c);
            if (norm16 > this.minYesNo) {
                if (norm16 < MIN_NORMAL_MAYBE_YES) {
                    if (norm16 < this.minMaybeYes) {
                        if (!isDecompNoAlgorithmic(norm16)) {
                            break;
                        }
                        c = mapAlgorithmic(c, norm16);
                    } else {
                        return IX_NORM_TRIE_OFFSET;
                    }
                }
                norm16 &= Opcodes.OP_CONST_CLASS_JUMBO;
                return (norm16 << IX_MIN_DECOMP_NO_CP) | norm16;
            }
            return IX_NORM_TRIE_OFFSET;
        }
        int firstUnit = this.extraData.charAt(norm16);
        if ((firstUnit & MAPPING_LENGTH_MASK) == 0) {
            return Opcodes.OP_CHECK_CAST_JUMBO;
        }
        int fcd16 = firstUnit >> IX_MIN_DECOMP_NO_CP;
        if ((firstUnit & MAPPING_HAS_CCC_LCCC_WORD) != 0) {
            fcd16 |= this.extraData.charAt(norm16 - 1) & JAMO_VT;
        }
        return fcd16;
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
                int norm162 = norm16 + JAMO_L;
                return this.extraData.substring(norm162, norm162 + (this.extraData.charAt(norm16) & MAPPING_LENGTH_MASK));
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
                    int mLength = firstUnit & MAPPING_LENGTH_MASK;
                    if ((firstUnit & MAX_DELTA) != 0) {
                        int rawMapping = (norm16 - ((firstUnit >> IX_TOTAL_SIZE) & JAMO_L)) - 1;
                        char rm0 = this.extraData.charAt(rawMapping);
                        if (rm0 <= '\u001f') {
                            return this.extraData.substring(rawMapping - rm0, rawMapping);
                        }
                        norm16 += IX_RESERVED3_OFFSET;
                        return new StringBuilder(mLength - 1).append(rm0).append(this.extraData, norm16, (norm16 + mLength) - 2).toString();
                    }
                    norm16 += JAMO_L;
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
        int canonValue = this.canonIterData.get(c) & AnnualTimeZoneRule.MAX_YEAR;
        if (canonValue == 0) {
            return false;
        }
        set.clear();
        int value = canonValue & CANON_VALUE_MASK;
        if ((CANON_HAS_SET & canonValue) != 0) {
            set.addAll((UnicodeSet) this.canonStartSets.get(value));
        } else if (value != 0) {
            set.add(value);
        }
        if ((CANON_HAS_COMPOSITIONS & canonValue) != 0) {
            int norm16 = getNorm16(c);
            if (norm16 == JAMO_L) {
                int syllable = Hangul.HANGUL_BASE + ((c - 4352) * Hangul.JAMO_VT_COUNT);
                set.add(syllable, (syllable + Hangul.JAMO_VT_COUNT) - 1);
            } else {
                addComposites(getCompositionsList(norm16), set);
            }
        }
        return true;
    }

    public Appendable decompose(CharSequence s, StringBuilder dest) {
        decompose(s, IX_NORM_TRIE_OFFSET, s.length(), dest, s.length());
        return dest;
    }

    public void decompose(CharSequence s, int src, int limit, StringBuilder dest, int destLengthEstimate) {
        if (destLengthEstimate < 0) {
            destLengthEstimate = limit - src;
        }
        dest.setLength(IX_NORM_TRIE_OFFSET);
        decompose(s, src, limit, new ReorderingBuffer(this, dest, destLengthEstimate));
    }

    public int decompose(CharSequence s, int src, int limit, ReorderingBuffer buffer) {
        int minNoCP = this.minDecompNoCP;
        int c = IX_NORM_TRIE_OFFSET;
        int norm16 = IX_NORM_TRIE_OFFSET;
        int prevBoundary = src;
        int prevCC = IX_NORM_TRIE_OFFSET;
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
                            if (src + JAMO_L != limit) {
                                c2 = s.charAt(src + JAMO_L);
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
                src += JAMO_L;
            }
            if (src != prevSrc) {
                if (buffer != null) {
                    buffer.flushAndAppendZeroCC(s, prevSrc, src);
                } else {
                    prevCC = IX_NORM_TRIE_OFFSET;
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
                    if (cc <= JAMO_L) {
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
                decompose(s, IX_NORM_TRIE_OFFSET, limit, buffer);
                return;
            }
            int c = Character.codePointAt(s, IX_NORM_TRIE_OFFSET);
            int src = IX_NORM_TRIE_OFFSET;
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
            buffer.append(s, IX_NORM_TRIE_OFFSET, src, cc, prevCC);
            buffer.append(s, src, limit);
        }
    }

    public boolean compose(CharSequence s, int src, int limit, boolean onlyContiguous, boolean doCompose, ReorderingBuffer buffer) {
        int minNoMaybeCP = this.minCompNoMaybeCP;
        int prevBoundary = src;
        int c = IX_NORM_TRIE_OFFSET;
        int norm16 = IX_NORM_TRIE_OFFSET;
        int prevCC = IX_NORM_TRIE_OFFSET;
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
                            if (src + JAMO_L != limit) {
                                c2 = s.charAt(src + JAMO_L);
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
                src += JAMO_L;
            }
            char prev;
            boolean needToDecompose;
            char syllable;
            char t;
            int cc;
            int recomposeStartIndex;
            if (src == prevSrc) {
                if (src == limit) {
                    break;
                }
                src += Character.charCount(c);
                prev = s.charAt(prevSrc - 1);
                needToDecompose = false;
                if (c < 4519) {
                    prev = (char) (prev - 4352);
                    if (prev < '\u0013') {
                        if (!doCompose) {
                            return false;
                        }
                        syllable = (char) ((((prev * 21) + (c - 4449)) * 28) + Hangul.HANGUL_BASE);
                        if (src != limit) {
                            t = (char) (s.charAt(src) - 4519);
                            if (t < '\u001c') {
                                src += JAMO_L;
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
                if (!needToDecompose) {
                    if (doCompose) {
                        buffer.append((char) c);
                    } else {
                        prevCC = IX_NORM_TRIE_OFFSET;
                    }
                }
                if (norm16 < MIN_YES_YES_WITH_CC) {
                    return false;
                }
                cc = norm16 & Opcodes.OP_CONST_CLASS_JUMBO;
                if (onlyContiguous) {
                    if (doCompose) {
                    }
                    if (!doCompose) {
                        return false;
                    }
                }
                if (doCompose) {
                    buffer.append(c, cc);
                } else if (prevCC <= cc) {
                    return false;
                } else {
                    prevCC = cc;
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
                if (!doCompose) {
                    if (!buffer.equals(s, prevBoundary, src)) {
                        return false;
                    }
                    buffer.remove();
                    prevCC = IX_NORM_TRIE_OFFSET;
                }
                prevBoundary = src;
            } else if (src == limit) {
                break;
            } else {
                prevBoundary = src - 1;
                if (Character.isLowSurrogate(s.charAt(prevBoundary)) && prevSrc < prevBoundary) {
                    if (Character.isHighSurrogate(s.charAt(prevBoundary - 1))) {
                        prevBoundary--;
                    }
                }
                if (doCompose) {
                    buffer.flushAndAppendZeroCC(s, prevSrc, prevBoundary);
                    buffer.append(s, prevBoundary, src);
                } else {
                    prevCC = IX_NORM_TRIE_OFFSET;
                }
                prevSrc = src;
                src += Character.charCount(c);
                if (isJamoVT(norm16) && prevBoundary != prevSrc) {
                    prev = s.charAt(prevSrc - 1);
                    needToDecompose = false;
                    if (c < 4519) {
                        prev = (char) (prev - 4352);
                        if (prev < '\u0013') {
                            if (!doCompose) {
                                return false;
                            }
                            syllable = (char) ((((prev * 21) + (c - 4449)) * 28) + Hangul.HANGUL_BASE);
                            if (src != limit) {
                                t = (char) (s.charAt(src) - 4519);
                                if (t < '\u001c') {
                                    src += JAMO_L;
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
                            prevCC = IX_NORM_TRIE_OFFSET;
                        }
                    }
                }
                if (norm16 < MIN_YES_YES_WITH_CC) {
                    cc = norm16 & Opcodes.OP_CONST_CLASS_JUMBO;
                    if (onlyContiguous) {
                        if ((doCompose ? buffer.getLastCC() : prevCC) == 0 && prevBoundary < prevSrc && getTrailCCFromCompYesAndZeroCC(s, prevBoundary, prevSrc) > cc) {
                            if (doCompose) {
                                return false;
                            }
                        }
                    }
                    if (doCompose) {
                        buffer.append(c, cc);
                    } else if (prevCC <= cc) {
                        return false;
                    } else {
                        prevCC = cc;
                    }
                } else if (!(doCompose || isMaybeOrNonZeroCC(norm16))) {
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
                    prevCC = IX_NORM_TRIE_OFFSET;
                }
                prevBoundary = src;
            }
            return true;
        }
        if (doCompose) {
            buffer.flushAndAppendZeroCC(s, prevSrc, src);
        }
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int composeQuickCheck(CharSequence s, int src, int limit, boolean onlyContiguous, boolean doSpan) {
        int qcResult = IX_NORM_TRIE_OFFSET;
        int minNoMaybeCP = this.minCompNoMaybeCP;
        int prevBoundary = src;
        int prevCC = IX_NORM_TRIE_OFFSET;
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
                                if (src + JAMO_L != limit) {
                                    c2 = s.charAt(src + JAMO_L);
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
                            prevCC = IX_NORM_TRIE_OFFSET;
                            prevSrc = src;
                        }
                        src += Character.charCount(c);
                        if (!isMaybeOrNonZeroCC(norm16)) {
                            break;
                        }
                        int cc = getCCFromYesOrMaybe(norm16);
                        if ((!onlyContiguous || cc == 0 || prevCC != 0 || prevBoundary >= prevSrc || getTrailCCFromCompYesAndZeroCC(s, prevBoundary, prevSrc) <= cc) && (prevCC <= cc || cc == 0)) {
                            prevCC = cc;
                            if (norm16 < MIN_YES_YES_WITH_CC) {
                                if (doSpan) {
                                    return prevBoundary << JAMO_L;
                                }
                                qcResult = JAMO_L;
                            }
                        }
                    }
                }
                src += JAMO_L;
            }
            return (src << JAMO_L) | qcResult;
        }
        return prevBoundary << JAMO_L;
    }

    public void composeAndAppend(CharSequence s, boolean doCompose, boolean onlyContiguous, ReorderingBuffer buffer) {
        int src = IX_NORM_TRIE_OFFSET;
        int limit = s.length();
        if (!buffer.isEmpty()) {
            int firstStarterInSrc = findNextCompBoundary(s, IX_NORM_TRIE_OFFSET, limit);
            if (firstStarterInSrc != 0) {
                int lastStarterInDest = findPreviousCompBoundary(buffer.getStringBuilder(), buffer.length());
                StringBuilder middle = new StringBuilder(((buffer.length() - lastStarterInDest) + firstStarterInSrc) + IX_COUNT);
                middle.append(buffer.getStringBuilder(), lastStarterInDest, buffer.length());
                buffer.removeSuffix(buffer.length() - lastStarterInDest);
                middle.append(s, IX_NORM_TRIE_OFFSET, firstStarterInSrc);
                compose(middle, IX_NORM_TRIE_OFFSET, middle.length(), onlyContiguous, true, buffer);
                src = firstStarterInSrc;
            }
        }
        if (doCompose) {
            compose(s, src, limit, onlyContiguous, true, buffer);
        } else {
            buffer.append(s, src, limit);
        }
    }

    public void makeFCDAndAppend(CharSequence s, boolean doMakeFCD, ReorderingBuffer buffer) {
        int src = IX_NORM_TRIE_OFFSET;
        int limit = s.length();
        if (!buffer.isEmpty()) {
            int firstBoundaryInSrc = findNextFCDBoundary(s, IX_NORM_TRIE_OFFSET, limit);
            if (firstBoundaryInSrc != 0) {
                int lastBoundaryInDest = findPreviousFCDBoundary(buffer.getStringBuilder(), buffer.length());
                StringBuilder middle = new StringBuilder(((buffer.length() - lastBoundaryInDest) + firstBoundaryInSrc) + IX_COUNT);
                middle.append(buffer.getStringBuilder(), lastBoundaryInDest, buffer.length());
                buffer.removeSuffix(buffer.length() - lastBoundaryInDest);
                middle.append(s, IX_NORM_TRIE_OFFSET, firstBoundaryInSrc);
                makeFCD(middle, IX_NORM_TRIE_OFFSET, middle.length(), buffer);
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
                if ((firstUnit & MAPPING_LENGTH_MASK) == 0) {
                    return false;
                }
                if (!before) {
                    if (firstUnit > Opcodes.OP_CHECK_CAST_JUMBO) {
                        return false;
                    }
                    if (firstUnit <= Opcodes.OP_CONST_CLASS_JUMBO) {
                        return true;
                    }
                }
                if (!((firstUnit & MAPPING_HAS_CCC_LCCC_WORD) == 0 || (this.extraData.charAt(norm16 - 1) & JAMO_VT) == 0)) {
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean hasCompBoundaryAfter(int c, boolean onlyContiguous, boolean testInert) {
        boolean z = true;
        boolean z2 = false;
        while (true) {
            int norm16 = getNorm16(c);
            if (!isInert(norm16)) {
                if (norm16 > this.minYesNo) {
                    if (norm16 < (testInert ? this.minNoNo : this.minMaybeYes)) {
                        if (!isDecompNoAlgorithmic(norm16)) {
                            break;
                        }
                        c = mapAlgorithmic(c, norm16);
                    } else {
                        return false;
                    }
                }
                break;
            }
            return true;
        }
        if (isHangul(norm16) && !Hangul.isHangulWithoutJamoT((char) c)) {
            z2 = true;
        }
        return z2;
    }

    public boolean hasFCDBoundaryBefore(int c) {
        return c < MIN_CCC_LCCC_CP || getFCD16(c) <= Opcodes.OP_CONST_CLASS_JUMBO;
    }

    public boolean hasFCDBoundaryAfter(int c) {
        int fcd16 = getFCD16(c);
        if (fcd16 <= JAMO_L || (fcd16 & Opcodes.OP_CONST_CLASS_JUMBO) == 0) {
            return true;
        }
        return false;
    }

    public boolean isFCDInert(int c) {
        return getFCD16(c) <= JAMO_L;
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
        return norm16 == JAMO_L;
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
        if ((this.extraData.charAt(norm16) & MAPPING_HAS_CCC_LCCC_WORD) != 0) {
            return this.extraData.charAt(norm16 - 1) & Opcodes.OP_CONST_CLASS_JUMBO;
        }
        return IX_NORM_TRIE_OFFSET;
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
            return IX_NORM_TRIE_OFFSET;
        }
        return this.extraData.charAt(prevNorm16) >> IX_MIN_DECOMP_NO_CP;
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
        return (((MIN_NORMAL_MAYBE_YES - this.minMaybeYes) + norm16) + JAMO_L) + (this.extraData.charAt(norm16) & MAPPING_LENGTH_MASK);
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
                int length = firstUnit & MAPPING_LENGTH_MASK;
                int trailCC = firstUnit >> IX_MIN_DECOMP_NO_CP;
                if ((firstUnit & MAPPING_HAS_CCC_LCCC_WORD) != 0) {
                    leadCC = this.extraData.charAt(norm16 - 1) >> IX_MIN_DECOMP_NO_CP;
                } else {
                    leadCC = IX_NORM_TRIE_OFFSET;
                }
                norm16 += JAMO_L;
                buffer.append(this.extraData, norm16, norm16 + length, leadCC, trailCC);
                return;
            }
        }
        buffer.append(c, getCCFromYesOrMaybe(norm16));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static int combine(String compositions, int list, int trail) {
        int key1;
        int firstUnit;
        if (trail >= COMP_1_TRAIL_LIMIT) {
            key1 = ((trail >> IX_MIN_COMP_NO_MAYBE_CP) & -2) + COMP_1_TRAIL_LIMIT;
            int key2 = (trail << COMP_2_TRAIL_SHIFT) & DexFormat.MAX_TYPE_IDX;
            while (true) {
                firstUnit = compositions.charAt(list);
                if (key1 <= firstUnit) {
                    if (key1 != (firstUnit & COMP_1_TRAIL_MASK)) {
                        break;
                    }
                    int secondUnit = compositions.charAt(list + JAMO_L);
                    if (key2 <= secondUnit) {
                        break;
                    } else if ((COMP_1_LAST_TUPLE & firstUnit) != 0) {
                        break;
                    } else {
                        list += IX_RESERVED3_OFFSET;
                    }
                } else {
                    list += (firstUnit & JAMO_L) + IX_SMALL_FCD_OFFSET;
                }
            }
        } else {
            key1 = trail << JAMO_L;
            while (true) {
                firstUnit = compositions.charAt(list);
                if (key1 <= firstUnit) {
                    break;
                }
                list += (firstUnit & JAMO_L) + IX_SMALL_FCD_OFFSET;
            }
            if (key1 == (firstUnit & COMP_1_TRAIL_MASK)) {
                if ((firstUnit & JAMO_L) != 0) {
                    return (compositions.charAt(list + JAMO_L) << IX_COUNT) | compositions.charAt(list + IX_SMALL_FCD_OFFSET);
                }
                return compositions.charAt(list + JAMO_L);
            }
        }
        return -1;
    }

    private void addComposites(int list, UnicodeSet set) {
        int firstUnit;
        do {
            int compositeAndFwd;
            firstUnit = this.maybeYesCompositions.charAt(list);
            if ((firstUnit & JAMO_L) == 0) {
                compositeAndFwd = this.maybeYesCompositions.charAt(list + JAMO_L);
                list += IX_SMALL_FCD_OFFSET;
            } else {
                compositeAndFwd = ((this.maybeYesCompositions.charAt(list + JAMO_L) & -65473) << IX_COUNT) | this.maybeYesCompositions.charAt(list + IX_SMALL_FCD_OFFSET);
                list += IX_RESERVED3_OFFSET;
            }
            int composite = compositeAndFwd >> JAMO_L;
            if ((compositeAndFwd & JAMO_L) != 0) {
                addComposites(getCompositionsListForComposite(getNorm16(composite)), set);
            }
            set.add(composite);
        } while ((COMP_1_LAST_TUPLE & firstUnit) == 0);
    }

    private void recompose(ReorderingBuffer buffer, int recomposeStartIndex, boolean onlyContiguous) {
        StringBuilder sb = buffer.getStringBuilder();
        int p = recomposeStartIndex;
        if (recomposeStartIndex != sb.length()) {
            int compositionsList = -1;
            int starter = -1;
            boolean starterIsSupplementary = false;
            int prevCC = IX_NORM_TRIE_OFFSET;
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
                            int composite = compositeAndFwd >> JAMO_L;
                            pRemove = p - Character.charCount(c);
                            sb.delete(pRemove, p);
                            p = pRemove;
                            if (starterIsSupplementary) {
                                if (composite > 65535) {
                                    sb.setCharAt(starter, UTF16.getLeadSurrogate(composite));
                                    sb.setCharAt(starter + JAMO_L, UTF16.getTrailSurrogate(composite));
                                } else {
                                    sb.setCharAt(starter, (char) c);
                                    sb.deleteCharAt(starter + JAMO_L);
                                    starterIsSupplementary = false;
                                    p = pRemove - 1;
                                }
                            } else if (composite > 65535) {
                                starterIsSupplementary = true;
                                sb.setCharAt(starter, UTF16.getLeadSurrogate(composite));
                                sb.insert(starter + JAMO_L, UTF16.getTrailSurrogate(composite));
                                p = pRemove + JAMO_L;
                            } else {
                                sb.setCharAt(starter, (char) composite);
                            }
                            if (p == sb.length()) {
                                break;
                            } else if ((compositeAndFwd & JAMO_L) != 0) {
                                compositionsList = getCompositionsListForComposite(getNorm16(composite));
                            } else {
                                compositionsList = -1;
                            }
                        }
                    } else {
                        if (c < 4519) {
                            char prev = (char) (sb.charAt(starter) - 4352);
                            if (prev < '\u0013') {
                                pRemove = p - 1;
                                char syllable = (char) ((((prev * 21) + (c - 4449)) * 28) + Hangul.HANGUL_BASE);
                                if (p != sb.length()) {
                                    char t = (char) (sb.charAt(p) - 4519);
                                    if (t < '\u001c') {
                                        p += JAMO_L;
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
                    list = norm16 + ((this.extraData.charAt(norm16) & MAPPING_LENGTH_MASK) + JAMO_L);
                }
                list += MIN_NORMAL_MAYBE_YES - this.minMaybeYes;
            }
        } else if (norm16 < this.minMaybeYes || MIN_NORMAL_MAYBE_YES <= norm16) {
            return -1;
        } else {
            list = norm16 - this.minMaybeYes;
        }
        if (b < 0 || UnicodeSet.MAX_VALUE < b) {
            return -1;
        }
        return combine(this.maybeYesCompositions, list, b) >> JAMO_L;
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
                if ((firstUnit & MAPPING_LENGTH_MASK) == 0) {
                    return false;
                }
                if ((firstUnit & MAPPING_HAS_CCC_LCCC_WORD) == 0 || (this.extraData.charAt(norm16 - 1) & JAMO_VT) == 0) {
                    return isCompYesAndZeroCC(getNorm16(Character.codePointAt(this.extraData, norm16 + JAMO_L)));
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
            if (c >= MIN_CCC_LCCC_CP) {
                if (getFCD16(c) <= Opcodes.OP_CONST_CLASS_JUMBO) {
                    break;
                }
            }
            break;
        }
        return p;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int findNextFCDBoundary(CharSequence s, int p, int limit) {
        while (p < limit) {
            int c = Character.codePointAt(s, p);
            if (c >= MIN_CCC_LCCC_CP && getFCD16(c) > Opcodes.OP_CONST_CLASS_JUMBO) {
                p += Character.charCount(c);
            }
        }
        return p;
    }

    private void addToStartSet(Trie2Writable newData, int origin, int decompLead) {
        int canonValue = newData.get(decompLead);
        if ((4194303 & canonValue) != 0 || origin == 0) {
            UnicodeSet set;
            if ((canonValue & CANON_HAS_SET) == 0) {
                int firstOrigin = canonValue & CANON_VALUE_MASK;
                newData.set(decompLead, ((-2097152 & canonValue) | CANON_HAS_SET) | this.canonStartSets.size());
                ArrayList arrayList = this.canonStartSets;
                set = new UnicodeSet();
                arrayList.add(set);
                if (firstOrigin != 0) {
                    set.add(firstOrigin);
                }
            } else {
                set = (UnicodeSet) this.canonStartSets.get(CANON_VALUE_MASK & canonValue);
            }
            set.add(origin);
            return;
        }
        newData.set(decompLead, canonValue | origin);
    }
}
