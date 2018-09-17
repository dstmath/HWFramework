package java.awt.font;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Set;

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
    private static final char[] bases = new char[]{0, 1584, 1728, 2358, 2486, 2614, 2742, 2870, 2998, 3126, 3254, 3382, 3616, 3744, 3824, 4112, 4920, 6064, 6112};
    private static final char[] contexts = new char[]{0, 768, 1536, 1920, 1536, 1920, 2304, 2432, 2432, 2560, 2560, 2688, 2688, 2816, 2816, 2944, 2944, 3072, 3072, 3200, 3200, 3328, 3328, 3456, 3584, 3712, 3712, 3840, 3840, 4096, 4096, 4224, 4608, 4992, 6016, 6144, 6144, 6400, 65535};
    private static int ctCache = 0;
    private static int ctCacheLimit = (contexts.length - 2);
    private static final long serialVersionUID = -8022764705923730308L;
    private static int[] strongTable = new int[]{0, 65, 91, 97, 123, 170, 171, 181, 182, 186, 187, 192, 215, 216, 247, 248, 697, 699, 706, 720, 722, 736, 741, 750, 751, 880, 884, 886, 894, 902, 903, 904, 1014, 1015, 1155, 1162, 1418, 1470, 1471, 1472, 1473, 1475, 1476, 1478, 1479, 1488, 1536, 1544, 1545, 1547, 1548, 1549, 1550, 1563, 1611, 1645, 1648, 1649, 1750, 1765, 1767, 1774, 1776, 1786, 1809, 1810, 1840, 1869, 1958, 1969, 2027, 2036, 2038, 2042, 2070, 2074, 2075, 2084, 2085, 2088, 2089, 2096, 2137, 2142, 2276, 2307, 2362, 2363, 2364, 2365, 2369, 2377, 2381, 2382, 2385, 2392, 2402, 2404, 2433, 2434, 2492, 2493, 2497, 2503, 2509, 2510, 2530, 2534, 2546, 2548, 2555, 2563, 2620, 2622, 2625, 2649, 2672, 2674, 2677, 2691, 2748, 2749, 2753, 2761, 2765, 2768, 2786, 2790, 2801, 2818, 2876, 2877, 2879, 2880, 2881, 2887, 2893, 2903, 2914, 2918, 2946, 2947, 3008, 3009, 3021, 3024, 3059, 3073, 3134, 3137, 3142, 3160, 3170, 3174, 3192, 3199, 3260, 3261, 3276, 3285, 3298, 3302, 3393, 3398, 3405, 3406, 3426, 3430, 3530, 3535, 3538, 3544, 3633, 3634, 3636, 3648, 3655, 3663, 3761, 3762, 3764, 3773, 3784, 3792, 3864, 3866, 3893, 3894, 3895, 3896, 3897, 3902, 3953, 3967, 3968, 3973, 3974, 3976, 3981, 4030, 4038, 4039, 4141, 4145, 4146, 4152, 4153, 4155, 4157, 4159, 4184, 4186, 4190, 4193, 4209, 4213, 4226, 4227, 4229, 4231, 4237, 4238, 4253, 4254, 4957, 4960, 5008, 5024, 5120, 5121, 5760, 5761, 5787, 5792, 5906, 5920, 5938, 5941, 5970, 5984, 6002, 6016, 6068, 6070, 6071, 6078, 6086, 6087, 6089, 6100, 6107, 6108, 6109, 6112, 6128, 6160, 6313, 6314, 6432, 6435, 6439, 6441, 6450, 6451, 6457, 6470, 6622, 6656, 6679, 6681, 6742, 6743, 6744, 6753, 6754, 6755, 6757, 6765, 6771, 6784, 6912, 6916, 6964, 6965, 6966, 6971, 6972, 6973, 6978, 6979, 7019, 7028, 7040, 7042, 7074, 7078, 7080, 7082, 7083, 7084, 7142, 7143, 7144, 7146, 7149, 7150, 7151, 7154, 7212, 7220, 7222, 7227, 7376, 7379, 7380, 7393, 7394, 7401, 7405, 7406, 7412, 7413, 7616, 7680, 8125, 8126, 8127, 8130, 8141, 8144, 8157, 8160, 8173, 8178, 8189, 8206, 8208, 8305, 8308, 8319, 8320, 8336, 8352, 8450, 8451, 8455, 8456, 8458, 8468, 8469, 8470, 8473, 8478, 8484, 8485, 8486, 8487, 8488, 8489, 8490, 8494, 8495, 8506, 8508, 8512, 8517, 8522, 8526, 8528, 8544, 8585, 9014, 9083, 9109, 9110, 9372, 9450, 9900, 9901, 10240, 10496, 11264, 11493, 11499, 11503, 11506, 11513, 11520, 11647, 11648, 11744, 12293, 12296, 12321, 12330, 12337, 12342, 12344, 12349, 12353, 12441, 12445, 12448, 12449, 12539, 12540, 12736, 12784, 12829, 12832, 12880, 12896, 12924, 12927, 12977, 12992, 13004, 13008, 13175, 13179, 13278, 13280, 13311, 13312, 19904, 19968, 42128, 42192, 42509, 42512, 42607, 42624, 42655, 42656, 42736, 42738, 42752, 42786, 42888, 42889, 43010, 43011, 43014, 43015, 43019, 43020, 43045, 43047, 43048, 43056, 43064, 43072, 43124, 43136, 43204, 43214, 43232, 43250, 43302, 43310, 43335, 43346, 43392, 43395, 43443, 43444, 43446, 43450, 43452, 43453, 43561, 43567, 43569, 43571, 43573, 43584, 43587, 43588, 43596, 43597, 43696, 43697, 43698, 43701, 43703, 43705, 43710, 43712, 43713, 43714, 43756, 43758, 43766, 43777, 44005, 44006, 44008, 44009, 44013, 44016, 64286, 64287, 64297, 64298, 64830, 64848, 65021, 65136, 65279, 65313, 65339, 65345, 65371, 65382, 65504, 65536, 65793, 65794, 65856, 66000, 66045, 66176, 67871, 67872, 68097, 68112, 68152, 68160, 68409, 68416, 69216, 69632, 69633, 69634, 69688, 69703, 69714, 69734, 69760, 69762, 69811, 69815, 69817, 69819, 69888, 69891, 69927, 69932, 69933, 69942, 70016, 70018, 70070, 70079, 71339, 71340, 71341, 71342, 71344, 71350, 71351, 71360, 94095, 94099, 119143, 119146, 119155, 119171, 119173, 119180, 119210, 119214, 119296, 119648, 120539, 120540, 120597, 120598, 120655, 120656, 120713, 120714, 120771, 120772, 120782, 126464, 126704, 127248, 127338, 127344, 127744, 128140, 128141, 128292, 128293, KHMER, 917505, 983040, 1114110, Character.MAX_CODE_POINT};
    private volatile transient Range currentRange = Range.EUROPEAN;
    private int key;
    private int mask;
    private transient Range[] rangeArray;
    private transient Set<Range> rangeSet;
    private Range shapingRange;
    private volatile transient int stCache = 0;

    public enum Range {
        EUROPEAN(48, 0, 768),
        ARABIC(1632, 1536, 1920),
        EASTERN_ARABIC(1776, 1536, 1920),
        DEVANAGARI(2406, 2304, 2432),
        BENGALI(2534, 2432, 2560),
        GURMUKHI(2662, 2560, 2688),
        GUJARATI(2790, 2816, 2944),
        ORIYA(2918, 2816, 2944),
        TAMIL(3046, 2944, 3072),
        TELUGU(3174, 3072, 3200),
        KANNADA(3302, 3200, 3328),
        MALAYALAM(3430, 3328, 3456),
        THAI(3664, 3584, 3712),
        LAO(3792, 3712, 3840),
        TIBETAN(3872, 3840, 4096),
        MYANMAR(4160, 4096, 4224),
        ETHIOPIC(4969, 4608, 4992) {
            char getNumericBase() {
                return 1;
            }
        },
        KHMER(6112, 6016, 6144),
        MONGOLIAN(6160, 6144, 6400),
        NKO(1984, 1984, 2048),
        MYANMAR_SHAN(4240, 4096, 4256),
        LIMBU(6470, 6400, 6480),
        NEW_TAI_LUE(6608, 6528, 6624),
        BALINESE(6992, 6912, 7040),
        SUNDANESE(7088, 7040, 7104),
        LEPCHA(7232, 7168, 7248),
        OL_CHIKI(7248, 7248, 7296),
        VAI(42528, 42240, 42560),
        SAURASHTRA(43216, 43136, 43232),
        KAYAH_LI(43264, 43264, 43312),
        CHAM(43600, 43520, 43616),
        TAI_THAM_HORA(6784, 6688, 6832),
        TAI_THAM_THAM(6800, 6688, 6832),
        JAVANESE(43472, 43392, 43488),
        MEETEI_MAYEK(44016, 43968, 44032);
        
        private final int base;
        private final int end;
        private final int start;

        private static int toRangeIndex(Range script) {
            int index = script.ordinal();
            return index < NumericShaper.NUM_KEYS ? index : -1;
        }

        private static Range indexToRange(int index) {
            return index < NumericShaper.NUM_KEYS ? values()[index] : null;
        }

        private static int toRangeMask(Set<Range> ranges) {
            int m = 0;
            for (Range range : ranges) {
                int index = range.ordinal();
                if (index < NumericShaper.NUM_KEYS) {
                    m |= 1 << index;
                }
            }
            return m;
        }

        private static Set<Range> maskToRangeSet(int mask) {
            Set<Range> set = EnumSet.noneOf(Range.class);
            Range[] a = values();
            for (int i = 0; i < NumericShaper.NUM_KEYS; i++) {
                if (((1 << i) & mask) != 0) {
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
            return 0;
        }

        private boolean inRange(int c) {
            return this.start <= c && c < this.end;
        }
    }

    private static int getContextKey(char c) {
        if (c < contexts[ctCache]) {
            while (ctCache > 0 && c < contexts[ctCache]) {
                ctCache--;
            }
        } else if (c >= contexts[ctCache + 1]) {
            while (ctCache < ctCacheLimit && c >= contexts[ctCache + 1]) {
                ctCache++;
            }
        }
        if ((ctCache & 1) == 0) {
            return ctCache / 2;
        }
        return 0;
    }

    private Range rangeForCodePoint(int codepoint) {
        if (this.currentRange.inRange(codepoint)) {
            return this.currentRange;
        }
        Range[] ranges = this.rangeArray;
        if (ranges.length > 3) {
            int lo = 0;
            int hi = ranges.length - 1;
            while (lo <= hi) {
                int mid = (lo + hi) / 2;
                Range range = ranges[mid];
                if (codepoint < range.start) {
                    hi = mid - 1;
                } else if (codepoint >= range.end) {
                    lo = mid + 1;
                } else {
                    this.currentRange = range;
                    return range;
                }
            }
        }
        for (int i = 0; i < ranges.length; i++) {
            if (ranges[i].inRange(codepoint)) {
                return ranges[i];
            }
        }
        return Range.EUROPEAN;
    }

    private boolean isStrongDirectional(char c) {
        int cachedIndex = this.stCache;
        if (c < strongTable[cachedIndex]) {
            cachedIndex = search(c, strongTable, 0, cachedIndex);
        } else if (c >= strongTable[cachedIndex + 1]) {
            cachedIndex = search(c, strongTable, cachedIndex + 1, (strongTable.length - cachedIndex) - 1);
        }
        boolean val = (cachedIndex & 1) == 1;
        this.stCache = cachedIndex;
        return val;
    }

    private static int getKeyFromMask(int mask) {
        int key = 0;
        while (key < NUM_KEYS && ((1 << key) & mask) == 0) {
            key++;
        }
        if (key != NUM_KEYS && ((~(1 << key)) & mask) == 0) {
            return key;
        }
        throw new IllegalArgumentException("invalid shaper: " + Integer.toHexString(mask));
    }

    public static NumericShaper getShaper(int singleRange) {
        return new NumericShaper(getKeyFromMask(singleRange), singleRange);
    }

    public static NumericShaper getShaper(Range singleRange) {
        return new NumericShaper(singleRange, EnumSet.of(singleRange));
    }

    public static NumericShaper getContextualShaper(int ranges) {
        return new NumericShaper(0, ranges | Integer.MIN_VALUE);
    }

    public static NumericShaper getContextualShaper(Set<Range> ranges) {
        NumericShaper shaper = new NumericShaper(Range.EUROPEAN, (Set) ranges);
        shaper.mask = Integer.MIN_VALUE;
        return shaper;
    }

    public static NumericShaper getContextualShaper(int ranges, int defaultContext) {
        return new NumericShaper(getKeyFromMask(defaultContext), ranges | Integer.MIN_VALUE);
    }

    public static NumericShaper getContextualShaper(Set<Range> ranges, Range defaultContext) {
        if (defaultContext == null) {
            throw new NullPointerException();
        }
        NumericShaper shaper = new NumericShaper(defaultContext, (Set) ranges);
        shaper.mask = Integer.MIN_VALUE;
        return shaper;
    }

    private NumericShaper(int key, int mask) {
        this.key = key;
        this.mask = mask;
    }

    private NumericShaper(Range defaultContext, Set<Range> ranges) {
        this.shapingRange = defaultContext;
        this.rangeSet = EnumSet.copyOf((Collection) ranges);
        if (this.rangeSet.contains(Range.EASTERN_ARABIC) && this.rangeSet.contains(Range.ARABIC)) {
            this.rangeSet.remove(Range.ARABIC);
        }
        if (this.rangeSet.contains(Range.TAI_THAM_THAM) && this.rangeSet.contains(Range.TAI_THAM_HORA)) {
            this.rangeSet.remove(Range.TAI_THAM_HORA);
        }
        this.rangeArray = (Range[]) this.rangeSet.toArray(new Range[this.rangeSet.size()]);
        if (this.rangeArray.length > 3) {
            Arrays.sort(this.rangeArray, new Comparator<Range>() {
                public int compare(Range s1, Range s2) {
                    if (s1.base > s2.base) {
                        return 1;
                    }
                    return s1.base == s2.base ? 0 : -1;
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
        return (this.mask & Integer.MIN_VALUE) != 0;
    }

    public int getRanges() {
        return this.mask & Integer.MAX_VALUE;
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
            if (this.key == 16) {
                minDigit = (char) 49;
            }
        }
        int e = start + count;
        for (int i = start; i < e; i++) {
            char c = text[i];
            if (c >= minDigit && c <= '9') {
                text[i] = (char) (c + base);
            }
        }
    }

    private synchronized void shapeContextually(char[] text, int start, int count, int ctxKey) {
        if ((this.mask & (1 << ctxKey)) == 0) {
            ctxKey = 0;
        }
        int lastkey = ctxKey;
        int base = bases[ctxKey];
        char minDigit = ctxKey == 16 ? '1' : '0';
        synchronized (NumericShaper.class) {
            int e = start + count;
            for (int i = start; i < e; i++) {
                char c = text[i];
                if (c >= minDigit && c <= '9') {
                    text[i] = (char) (c + base);
                }
                if (isStrongDirectional(c)) {
                    int newkey = getContextKey(c);
                    if (newkey != lastkey) {
                        lastkey = newkey;
                        ctxKey = newkey;
                        if ((this.mask & 4) != 0 && (newkey == 1 || newkey == 2)) {
                            ctxKey = 2;
                        } else if ((this.mask & 2) != 0 && (newkey == 1 || newkey == 2)) {
                            ctxKey = 1;
                        } else if ((this.mask & (1 << newkey)) == 0) {
                            ctxKey = 0;
                        }
                        base = bases[ctxKey];
                        if (ctxKey == 16) {
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
        if (ctxKey == null || (this.rangeSet.contains(ctxKey) ^ 1) != 0) {
            ctxKey = Range.EUROPEAN;
        }
        Range lastKey = ctxKey;
        int base = ctxKey.getDigitBase();
        char minDigit = (char) (ctxKey.getNumericBase() + 48);
        int end = start + count;
        for (int i = start; i < end; i++) {
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
            return (hash & Integer.MIN_VALUE) ^ this.rangeSet.hashCode();
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
            for (int i = 0; i < NUM_KEYS; i++) {
                if ((this.mask & (1 << i)) != 0) {
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
        int bit = 0;
        if (value >= 65536) {
            value >>= 16;
            bit = 16;
        }
        if (value >= 256) {
            value >>= 8;
            bit += 8;
        }
        if (value >= 16) {
            value >>= 4;
            bit += 4;
        }
        if (value >= 4) {
            value >>= 2;
            bit += 2;
        }
        if (value >= 2) {
            bit++;
        }
        return bit;
    }

    private static int search(int value, int[] array, int start, int length) {
        int power = 1 << getHighBit(length);
        int extra = length - power;
        int probe = power;
        int index = start;
        if (value >= array[start + extra]) {
            index = start + extra;
        }
        while (probe > 1) {
            probe >>= 1;
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
