package java.awt.font;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Set;
import sun.util.logging.PlatformLogger;

public final class NumericShaper implements Serializable {
    public static final int ALL_RANGES = 524287;
    public static final int ARABIC = 2;
    private static final int ARABIC_KEY = 1;
    public static final int BENGALI = 16;
    private static final int BENGALI_KEY = 4;
    private static final int BSEARCH_THRESHOLD = 3;
    private static final int CONTEXTUAL_MASK = Integer.MIN_VALUE;
    public static final int DEVANAGARI = 8;
    private static final int DEVANAGARI_KEY = 3;
    public static final int EASTERN_ARABIC = 4;
    private static final int EASTERN_ARABIC_KEY = 2;
    public static final int ETHIOPIC = 65536;
    private static final int ETHIOPIC_KEY = 16;
    public static final int EUROPEAN = 1;
    private static final int EUROPEAN_KEY = 0;
    public static final int GUJARATI = 64;
    private static final int GUJARATI_KEY = 6;
    public static final int GURMUKHI = 32;
    private static final int GURMUKHI_KEY = 5;
    public static final int KANNADA = 1024;
    private static final int KANNADA_KEY = 10;
    public static final int KHMER = 131072;
    private static final int KHMER_KEY = 17;
    public static final int LAO = 8192;
    private static final int LAO_KEY = 13;
    public static final int MALAYALAM = 2048;
    private static final int MALAYALAM_KEY = 11;
    public static final int MONGOLIAN = 262144;
    private static final int MONGOLIAN_KEY = 18;
    public static final int MYANMAR = 32768;
    private static final int MYANMAR_KEY = 15;
    private static final int NUM_KEYS = 19;
    public static final int ORIYA = 128;
    private static final int ORIYA_KEY = 7;
    public static final int TAMIL = 256;
    private static final int TAMIL_KEY = 8;
    public static final int TELUGU = 512;
    private static final int TELUGU_KEY = 9;
    public static final int THAI = 4096;
    private static final int THAI_KEY = 12;
    public static final int TIBETAN = 16384;
    private static final int TIBETAN_KEY = 14;
    private static final char[] bases = null;
    private static final char[] contexts = null;
    private static int ctCache = 0;
    private static int ctCacheLimit = 0;
    private static final long serialVersionUID = -8022764705923730308L;
    private static int[] strongTable;
    private volatile transient Range currentRange;
    private int key;
    private int mask;
    private transient Range[] rangeArray;
    private transient Set<Range> rangeSet;
    private Range shapingRange;
    private volatile transient int stCache;

    public enum Range {
        ;
        
        private final int base;
        private final int end;
        private final int start;

        /* renamed from: java.awt.font.NumericShaper.Range.1 */
        enum AnonymousClass1 extends Range {
            AnonymousClass1(String str, int i, int $anonymous0, int $anonymous1, int $anonymous2) {
                super(i, $anonymous0, $anonymous1, $anonymous2, null);
            }

            char getNumericBase() {
                return '\u0001';
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.awt.font.NumericShaper.Range.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.awt.font.NumericShaper.Range.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.awt.font.NumericShaper.Range.<clinit>():void");
        }

        private static int toRangeIndex(Range script) {
            int index = script.ordinal();
            return index < NumericShaper.NUM_KEYS ? index : -1;
        }

        private static Range indexToRange(int index) {
            return index < NumericShaper.NUM_KEYS ? values()[index] : null;
        }

        private static int toRangeMask(Set<Range> ranges) {
            int m = NumericShaper.EUROPEAN_KEY;
            for (Range range : ranges) {
                int index = range.ordinal();
                if (index < NumericShaper.NUM_KEYS) {
                    m |= NumericShaper.EUROPEAN << index;
                }
            }
            return m;
        }

        private static Set<Range> maskToRangeSet(int mask) {
            Set<Range> set = EnumSet.noneOf(Range.class);
            Range[] a = values();
            for (int i = NumericShaper.EUROPEAN_KEY; i < NumericShaper.NUM_KEYS; i += NumericShaper.EUROPEAN) {
                if (((NumericShaper.EUROPEAN << i) & mask) != 0) {
                    set.add(a[i]);
                }
            }
            return set;
        }

        private Range(int base, int start, int end) {
            this.base = base - (getNumericBase() + 48);
            this.start = start;
            this.end = end;
        }

        private int getDigitBase() {
            return this.base;
        }

        char getNumericBase() {
            return '\u0000';
        }

        private boolean inRange(int c) {
            return this.start <= c && c < this.end;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.awt.font.NumericShaper.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.awt.font.NumericShaper.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.awt.font.NumericShaper.<clinit>():void");
    }

    private static int getKeyFromMask(int r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.awt.font.NumericShaper.getKeyFromMask(int):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.awt.font.NumericShaper.getKeyFromMask(int):int");
    }

    private static int getContextKey(char c) {
        if (c < contexts[ctCache]) {
            while (ctCache > 0 && c < contexts[ctCache]) {
                ctCache--;
            }
        } else if (c >= contexts[ctCache + EUROPEAN]) {
            while (ctCache < ctCacheLimit && c >= contexts[ctCache + EUROPEAN]) {
                ctCache += EUROPEAN;
            }
        }
        if ((ctCache & EUROPEAN) == 0) {
            return ctCache / EASTERN_ARABIC_KEY;
        }
        return EUROPEAN_KEY;
    }

    private Range rangeForCodePoint(int codepoint) {
        if (this.currentRange.inRange(codepoint)) {
            return this.currentRange;
        }
        Range[] ranges = this.rangeArray;
        if (ranges.length > DEVANAGARI_KEY) {
            int lo = EUROPEAN_KEY;
            int hi = ranges.length - 1;
            while (lo <= hi) {
                int mid = (lo + hi) / EASTERN_ARABIC_KEY;
                Range range = ranges[mid];
                if (codepoint < range.start) {
                    hi = mid - 1;
                } else if (codepoint >= range.end) {
                    lo = mid + EUROPEAN;
                } else {
                    this.currentRange = range;
                    return range;
                }
            }
        }
        for (int i = EUROPEAN_KEY; i < ranges.length; i += EUROPEAN) {
            if (ranges[i].inRange(codepoint)) {
                return ranges[i];
            }
        }
        return Range.EUROPEAN;
    }

    private boolean isStrongDirectional(char c) {
        int cachedIndex = this.stCache;
        if (c < strongTable[cachedIndex]) {
            cachedIndex = search(c, strongTable, EUROPEAN_KEY, cachedIndex);
        } else if (c >= strongTable[cachedIndex + EUROPEAN]) {
            cachedIndex = search(c, strongTable, cachedIndex + EUROPEAN, (strongTable.length - cachedIndex) - 1);
        }
        boolean val = (cachedIndex & EUROPEAN) == EUROPEAN;
        this.stCache = cachedIndex;
        return val;
    }

    public static NumericShaper getShaper(int singleRange) {
        return new NumericShaper(getKeyFromMask(singleRange), singleRange);
    }

    public static NumericShaper getShaper(Range singleRange) {
        return new NumericShaper(singleRange, EnumSet.of(singleRange));
    }

    public static NumericShaper getContextualShaper(int ranges) {
        return new NumericShaper((int) EUROPEAN_KEY, ranges | CONTEXTUAL_MASK);
    }

    public static NumericShaper getContextualShaper(Set<Range> ranges) {
        NumericShaper shaper = new NumericShaper(Range.EUROPEAN, (Set) ranges);
        shaper.mask = CONTEXTUAL_MASK;
        return shaper;
    }

    public static NumericShaper getContextualShaper(int ranges, int defaultContext) {
        return new NumericShaper(getKeyFromMask(defaultContext), ranges | CONTEXTUAL_MASK);
    }

    public static NumericShaper getContextualShaper(Set<Range> ranges, Range defaultContext) {
        if (defaultContext == null) {
            throw new NullPointerException();
        }
        NumericShaper shaper = new NumericShaper(defaultContext, (Set) ranges);
        shaper.mask = CONTEXTUAL_MASK;
        return shaper;
    }

    private NumericShaper(int key, int mask) {
        this.currentRange = Range.EUROPEAN;
        this.stCache = EUROPEAN_KEY;
        this.key = key;
        this.mask = mask;
    }

    private NumericShaper(Range defaultContext, Set<Range> ranges) {
        this.currentRange = Range.EUROPEAN;
        this.stCache = EUROPEAN_KEY;
        this.shapingRange = defaultContext;
        this.rangeSet = EnumSet.copyOf((Collection) ranges);
        if (this.rangeSet.contains(Range.EASTERN_ARABIC) && this.rangeSet.contains(Range.ARABIC)) {
            this.rangeSet.remove(Range.ARABIC);
        }
        if (this.rangeSet.contains(Range.TAI_THAM_THAM) && this.rangeSet.contains(Range.TAI_THAM_HORA)) {
            this.rangeSet.remove(Range.TAI_THAM_HORA);
        }
        this.rangeArray = (Range[]) this.rangeSet.toArray(new Range[this.rangeSet.size()]);
        if (this.rangeArray.length > DEVANAGARI_KEY) {
            Arrays.sort(this.rangeArray, new Comparator<Range>() {
                public int compare(Range s1, Range s2) {
                    if (s1.base > s2.base) {
                        return NumericShaper.EUROPEAN;
                    }
                    return s1.base == s2.base ? NumericShaper.EUROPEAN_KEY : -1;
                }
            });
        }
    }

    public void shape(char[] text, int start, int count) {
        checkParams(text, start, count);
        if (!isContextual()) {
            shapeNonContextually(text, start, count);
        } else if (this.rangeSet == null) {
            shapeContextually(text, start, count, this.key);
        } else {
            shapeContextually(text, start, count, this.shapingRange);
        }
    }

    public void shape(char[] text, int start, int count, int context) {
        checkParams(text, start, count);
        if (isContextual()) {
            int ctxKey = getKeyFromMask(context);
            if (this.rangeSet == null) {
                shapeContextually(text, start, count, ctxKey);
                return;
            } else {
                shapeContextually(text, start, count, Range.values()[ctxKey]);
                return;
            }
        }
        shapeNonContextually(text, start, count);
    }

    public void shape(char[] text, int start, int count, Range context) {
        checkParams(text, start, count);
        if (context == null) {
            throw new NullPointerException("context is null");
        } else if (!isContextual()) {
            shapeNonContextually(text, start, count);
        } else if (this.rangeSet != null) {
            shapeContextually(text, start, count, context);
        } else {
            int key = Range.toRangeIndex(context);
            if (key >= 0) {
                shapeContextually(text, start, count, key);
            } else {
                shapeContextually(text, start, count, this.shapingRange);
            }
        }
    }

    private void checkParams(char[] text, int start, int count) {
        if (text == null) {
            throw new NullPointerException("text is null");
        } else if (start < 0 || start > text.length || start + count < 0 || start + count > text.length) {
            throw new IndexOutOfBoundsException("bad start or count for text of length " + text.length);
        }
    }

    public boolean isContextual() {
        return (this.mask & CONTEXTUAL_MASK) != 0;
    }

    public int getRanges() {
        return this.mask & PlatformLogger.OFF;
    }

    public Set<Range> getRangeSet() {
        if (this.rangeSet != null) {
            return EnumSet.copyOf(this.rangeSet);
        }
        return Range.maskToRangeSet(this.mask);
    }

    private void shapeNonContextually(char[] text, int start, int count) {
        int base;
        char minDigit = '0';
        if (this.shapingRange != null) {
            base = this.shapingRange.getDigitBase();
            minDigit = (char) (this.shapingRange.getNumericBase() + 48);
        } else {
            base = bases[this.key];
            if (this.key == ETHIOPIC_KEY) {
                minDigit = (char) 49;
            }
        }
        int e = start + count;
        for (int i = start; i < e; i += EUROPEAN) {
            char c = text[i];
            if (c >= minDigit && c <= '9') {
                text[i] = (char) (c + base);
            }
        }
    }

    private synchronized void shapeContextually(char[] text, int start, int count, int ctxKey) {
        if ((this.mask & (EUROPEAN << ctxKey)) == 0) {
            ctxKey = EUROPEAN_KEY;
        }
        int lastkey = ctxKey;
        int base = bases[ctxKey];
        char minDigit = ctxKey == ETHIOPIC_KEY ? '1' : '0';
        synchronized (NumericShaper.class) {
            int e = start + count;
            for (int i = start; i < e; i += EUROPEAN) {
                char c = text[i];
                if (c >= minDigit && c <= '9') {
                    text[i] = (char) (c + base);
                }
                if (isStrongDirectional(c)) {
                    int newkey = getContextKey(c);
                    if (newkey != lastkey) {
                        lastkey = newkey;
                        ctxKey = newkey;
                        if ((this.mask & EASTERN_ARABIC) != 0 && (newkey == EUROPEAN || newkey == EASTERN_ARABIC_KEY)) {
                            ctxKey = EASTERN_ARABIC_KEY;
                        } else if ((this.mask & EASTERN_ARABIC_KEY) != 0 && (newkey == EUROPEAN || newkey == EASTERN_ARABIC_KEY)) {
                            ctxKey = EUROPEAN;
                        } else if ((this.mask & (EUROPEAN << newkey)) == 0) {
                            ctxKey = EUROPEAN_KEY;
                        }
                        base = bases[ctxKey];
                        if (ctxKey == ETHIOPIC_KEY) {
                            minDigit = '1';
                        } else {
                            minDigit = '0';
                        }
                    }
                }
            }
        }
    }

    private void shapeContextually(char[] text, int start, int count, Range ctxKey) {
        if (ctxKey == null || !this.rangeSet.contains(ctxKey)) {
            ctxKey = Range.EUROPEAN;
        }
        Range lastKey = ctxKey;
        int base = ctxKey.getDigitBase();
        char minDigit = (char) (ctxKey.getNumericBase() + 48);
        int end = start + count;
        for (int i = start; i < end; i += EUROPEAN) {
            char c = text[i];
            if (c >= minDigit && c <= '9') {
                text[i] = (char) (c + base);
            } else if (isStrongDirectional(c)) {
                ctxKey = rangeForCodePoint(c);
                if (ctxKey != lastKey) {
                    lastKey = ctxKey;
                    base = ctxKey.getDigitBase();
                    minDigit = (char) (ctxKey.getNumericBase() + 48);
                }
            }
        }
    }

    public int hashCode() {
        int hash = this.mask;
        if (this.rangeSet != null) {
            return (hash & CONTEXTUAL_MASK) ^ this.rangeSet.hashCode();
        }
        return hash;
    }

    public boolean equals(Object o) {
        boolean z = true;
        boolean z2 = false;
        if (o != null) {
            try {
                NumericShaper rhs = (NumericShaper) o;
                if (this.rangeSet != null) {
                    if (rhs.rangeSet != null) {
                        if (isContextual() != rhs.isContextual() || !this.rangeSet.equals(rhs.rangeSet)) {
                            z = false;
                        } else if (this.shapingRange != rhs.shapingRange) {
                            z = false;
                        }
                        return z;
                    }
                    if (isContextual() == rhs.isContextual() && this.rangeSet.equals(Range.maskToRangeSet(rhs.mask)) && this.shapingRange == Range.indexToRange(rhs.key)) {
                        z2 = true;
                    }
                    return z2;
                } else if (rhs.rangeSet != null) {
                    Set<Range> rset = Range.maskToRangeSet(this.mask);
                    Range srange = Range.indexToRange(this.key);
                    if (isContextual() == rhs.isContextual() && rset.equals(rhs.rangeSet) && srange == rhs.shapingRange) {
                        z2 = true;
                    }
                    return z2;
                } else {
                    if (rhs.mask == this.mask && rhs.key == this.key) {
                        z2 = true;
                    }
                    return z2;
                }
            } catch (ClassCastException e) {
            }
        }
        return false;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder(super.toString());
        buf.append("[contextual:").append(isContextual());
        if (isContextual()) {
            buf.append(", context:");
            buf.append(this.shapingRange == null ? Range.values()[this.key] : this.shapingRange);
        }
        if (this.rangeSet == null) {
            buf.append(", range(s): ");
            boolean first = true;
            for (int i = EUROPEAN_KEY; i < NUM_KEYS; i += EUROPEAN) {
                if ((this.mask & (EUROPEAN << i)) != 0) {
                    if (first) {
                        first = false;
                    } else {
                        buf.append(", ");
                    }
                    buf.append(Range.values()[i]);
                }
            }
        } else {
            buf.append(", range set: ").append(this.rangeSet);
        }
        buf.append(']');
        return buf.toString();
    }

    private static int getHighBit(int value) {
        if (value <= 0) {
            return -32;
        }
        int bit = EUROPEAN_KEY;
        if (value >= ETHIOPIC) {
            value >>= ETHIOPIC_KEY;
            bit = ETHIOPIC_KEY;
        }
        if (value >= TAMIL) {
            value >>= TAMIL_KEY;
            bit += TAMIL_KEY;
        }
        if (value >= ETHIOPIC_KEY) {
            value >>= EASTERN_ARABIC;
            bit += EASTERN_ARABIC;
        }
        if (value >= EASTERN_ARABIC) {
            value >>= EASTERN_ARABIC_KEY;
            bit += EASTERN_ARABIC_KEY;
        }
        if (value >= EASTERN_ARABIC_KEY) {
            bit += EUROPEAN;
        }
        return bit;
    }

    private static int search(int value, int[] array, int start, int length) {
        int power = EUROPEAN << getHighBit(length);
        int extra = length - power;
        int probe = power;
        int index = start;
        if (value >= array[start + extra]) {
            index = start + extra;
        }
        while (probe > EUROPEAN) {
            probe >>= EUROPEAN;
            if (value >= array[index + probe]) {
                index += probe;
            }
        }
        return index;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        if (this.shapingRange != null) {
            int index = Range.toRangeIndex(this.shapingRange);
            if (index >= 0) {
                this.key = index;
            }
        }
        if (this.rangeSet != null) {
            this.mask |= Range.toRangeMask(this.rangeSet);
        }
        stream.defaultWriteObject();
    }
}
