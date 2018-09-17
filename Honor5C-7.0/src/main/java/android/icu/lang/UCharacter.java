package android.icu.lang;

import android.icu.impl.Grego;
import android.icu.impl.IllegalIcuArgumentException;
import android.icu.impl.Trie2.Range;
import android.icu.impl.Trie2.ValueMapper;
import android.icu.impl.UBiDiProps;
import android.icu.impl.UCaseProps;
import android.icu.impl.UCaseProps.ContextIterator;
import android.icu.impl.UCharacterName;
import android.icu.impl.UCharacterProperty;
import android.icu.impl.UCharacterUtility;
import android.icu.impl.UPropertyAliases;
import android.icu.lang.UCharacterEnums.ECharacterCategory;
import android.icu.lang.UCharacterEnums.ECharacterDirection;
import android.icu.text.BreakIterator;
import android.icu.text.Collator.ReorderCodes;
import android.icu.text.Normalizer2;
import android.icu.text.UTF16;
import android.icu.util.RangeValueIterator;
import android.icu.util.ULocale;
import android.icu.util.ValueIterator;
import android.icu.util.ValueIterator.Element;
import android.icu.util.VersionInfo;
import dalvik.bytecode.Opcodes;
import dalvik.system.VMDebug;
import java.lang.Character.Subset;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import libcore.icu.DateUtilsBridge;

public final class UCharacter implements ECharacterCategory, ECharacterDirection {
    private static final int APPLICATION_PROGRAM_COMMAND_ = 159;
    private static final int BREAK_MASK = 560;
    private static final int CJK_IDEOGRAPH_COMPLEX_EIGHT_ = 25420;
    private static final int CJK_IDEOGRAPH_COMPLEX_FIVE_ = 20237;
    private static final int CJK_IDEOGRAPH_COMPLEX_FOUR_ = 32902;
    private static final int CJK_IDEOGRAPH_COMPLEX_HUNDRED_ = 20336;
    private static final int CJK_IDEOGRAPH_COMPLEX_NINE_ = 29590;
    private static final int CJK_IDEOGRAPH_COMPLEX_ONE_ = 22777;
    private static final int CJK_IDEOGRAPH_COMPLEX_SEVEN_ = 26578;
    private static final int CJK_IDEOGRAPH_COMPLEX_SIX_ = 38520;
    private static final int CJK_IDEOGRAPH_COMPLEX_TEN_ = 25342;
    private static final int CJK_IDEOGRAPH_COMPLEX_THOUSAND_ = 20191;
    private static final int CJK_IDEOGRAPH_COMPLEX_THREE_ = 21443;
    private static final int CJK_IDEOGRAPH_COMPLEX_TWO_ = 36019;
    private static final int CJK_IDEOGRAPH_COMPLEX_ZERO_ = 38646;
    private static final int CJK_IDEOGRAPH_EIGHTH_ = 20843;
    private static final int CJK_IDEOGRAPH_FIFTH_ = 20116;
    private static final int CJK_IDEOGRAPH_FIRST_ = 19968;
    private static final int CJK_IDEOGRAPH_FOURTH_ = 22235;
    private static final int CJK_IDEOGRAPH_HUNDRED_ = 30334;
    private static final int CJK_IDEOGRAPH_HUNDRED_MILLION_ = 20740;
    private static final int CJK_IDEOGRAPH_NINETH_ = 20061;
    private static final int CJK_IDEOGRAPH_SECOND_ = 20108;
    private static final int CJK_IDEOGRAPH_SEVENTH_ = 19971;
    private static final int CJK_IDEOGRAPH_SIXTH_ = 20845;
    private static final int CJK_IDEOGRAPH_TEN_ = 21313;
    private static final int CJK_IDEOGRAPH_TEN_THOUSAND_ = 33356;
    private static final int CJK_IDEOGRAPH_THIRD_ = 19977;
    private static final int CJK_IDEOGRAPH_THOUSAND_ = 21315;
    private static final int DELETE_ = 127;
    private static final int FIGURE_SPACE_ = 8199;
    public static final int FOLD_CASE_DEFAULT = 0;
    public static final int FOLD_CASE_EXCLUDE_SPECIAL_I = 1;
    private static final int IDEOGRAPHIC_NUMBER_ZERO_ = 12295;
    private static final int LAST_CHAR_MASK_ = 65535;
    public static final int MAX_CODE_POINT = 1114111;
    public static final char MAX_HIGH_SURROGATE = '\udbff';
    public static final char MAX_LOW_SURROGATE = '\udfff';
    public static final int MAX_RADIX = 36;
    public static final char MAX_SURROGATE = '\udfff';
    public static final int MAX_VALUE = 1114111;
    public static final int MIN_CODE_POINT = 0;
    public static final char MIN_HIGH_SURROGATE = '\ud800';
    public static final char MIN_LOW_SURROGATE = '\udc00';
    public static final int MIN_RADIX = 2;
    public static final int MIN_SUPPLEMENTARY_CODE_POINT = 65536;
    public static final char MIN_SURROGATE = '\ud800';
    public static final int MIN_VALUE = 0;
    private static final int NARROW_NO_BREAK_SPACE_ = 8239;
    private static final int NO_BREAK_SPACE_ = 160;
    public static final double NO_NUMERIC_VALUE = -1.23456789E8d;
    public static final int REPLACEMENT_CHAR = 65533;
    public static final int SUPPLEMENTARY_MIN_VALUE = 65536;
    public static final int TITLECASE_NO_BREAK_ADJUSTMENT = 512;
    public static final int TITLECASE_NO_LOWERCASE = 256;
    private static final int UNIT_SEPARATOR_ = 31;

    public interface BidiPairedBracketType {
        public static final int CLOSE = 2;
        public static final int COUNT = 3;
        public static final int NONE = 0;
        public static final int OPEN = 1;
    }

    public interface DecompositionType {
        public static final int CANONICAL = 1;
        public static final int CIRCLE = 3;
        public static final int COMPAT = 2;
        public static final int COUNT = 18;
        public static final int FINAL = 4;
        public static final int FONT = 5;
        public static final int FRACTION = 6;
        public static final int INITIAL = 7;
        public static final int ISOLATED = 8;
        public static final int MEDIAL = 9;
        public static final int NARROW = 10;
        public static final int NOBREAK = 11;
        public static final int NONE = 0;
        public static final int SMALL = 12;
        public static final int SQUARE = 13;
        public static final int SUB = 14;
        public static final int SUPER = 15;
        public static final int VERTICAL = 16;
        public static final int WIDE = 17;
    }

    private static final class DummyValueIterator implements ValueIterator {
        private DummyValueIterator() {
        }

        public boolean next(Element element) {
            return false;
        }

        public void reset() {
        }

        public void setRange(int start, int limit) {
        }
    }

    public interface EastAsianWidth {
        public static final int AMBIGUOUS = 1;
        public static final int COUNT = 6;
        public static final int FULLWIDTH = 3;
        public static final int HALFWIDTH = 2;
        public static final int NARROW = 4;
        public static final int NEUTRAL = 0;
        public static final int WIDE = 5;
    }

    public interface GraphemeClusterBreak {
        public static final int CONTROL = 1;
        public static final int COUNT = 13;
        public static final int CR = 2;
        public static final int EXTEND = 3;
        public static final int L = 4;
        public static final int LF = 5;
        public static final int LV = 6;
        public static final int LVT = 7;
        public static final int OTHER = 0;
        public static final int PREPEND = 11;
        public static final int REGIONAL_INDICATOR = 12;
        public static final int SPACING_MARK = 10;
        public static final int T = 8;
        public static final int V = 9;
    }

    public interface HangulSyllableType {
        public static final int COUNT = 6;
        public static final int LEADING_JAMO = 1;
        public static final int LVT_SYLLABLE = 5;
        public static final int LV_SYLLABLE = 4;
        public static final int NOT_APPLICABLE = 0;
        public static final int TRAILING_JAMO = 3;
        public static final int VOWEL_JAMO = 2;
    }

    public interface JoiningGroup {
        public static final int AIN = 1;
        public static final int ALAPH = 2;
        public static final int ALEF = 3;
        public static final int BEH = 4;
        public static final int BETH = 5;
        public static final int BURUSHASKI_YEH_BARREE = 54;
        public static final int COUNT = 86;
        public static final int DAL = 6;
        public static final int DALATH_RISH = 7;
        public static final int E = 8;
        public static final int FARSI_YEH = 55;
        public static final int FE = 51;
        public static final int FEH = 9;
        public static final int FINAL_SEMKATH = 10;
        public static final int GAF = 11;
        public static final int GAMAL = 12;
        public static final int HAH = 13;
        public static final int HAMZA_ON_HEH_GOAL = 14;
        public static final int HE = 15;
        public static final int HEH = 16;
        public static final int HEH_GOAL = 17;
        public static final int HETH = 18;
        public static final int KAF = 19;
        public static final int KAPH = 20;
        public static final int KHAPH = 52;
        public static final int KNOTTED_HEH = 21;
        public static final int LAM = 22;
        public static final int LAMADH = 23;
        public static final int MANICHAEAN_ALEPH = 58;
        public static final int MANICHAEAN_AYIN = 59;
        public static final int MANICHAEAN_BETH = 60;
        public static final int MANICHAEAN_DALETH = 61;
        public static final int MANICHAEAN_DHAMEDH = 62;
        public static final int MANICHAEAN_FIVE = 63;
        public static final int MANICHAEAN_GIMEL = 64;
        public static final int MANICHAEAN_HETH = 65;
        public static final int MANICHAEAN_HUNDRED = 66;
        public static final int MANICHAEAN_KAPH = 67;
        public static final int MANICHAEAN_LAMEDH = 68;
        public static final int MANICHAEAN_MEM = 69;
        public static final int MANICHAEAN_NUN = 70;
        public static final int MANICHAEAN_ONE = 71;
        public static final int MANICHAEAN_PE = 72;
        public static final int MANICHAEAN_QOPH = 73;
        public static final int MANICHAEAN_RESH = 74;
        public static final int MANICHAEAN_SADHE = 75;
        public static final int MANICHAEAN_SAMEKH = 76;
        public static final int MANICHAEAN_TAW = 77;
        public static final int MANICHAEAN_TEN = 78;
        public static final int MANICHAEAN_TETH = 79;
        public static final int MANICHAEAN_THAMEDH = 80;
        public static final int MANICHAEAN_TWENTY = 81;
        public static final int MANICHAEAN_WAW = 82;
        public static final int MANICHAEAN_YODH = 83;
        public static final int MANICHAEAN_ZAYIN = 84;
        public static final int MEEM = 24;
        public static final int MIM = 25;
        public static final int NOON = 26;
        public static final int NO_JOINING_GROUP = 0;
        public static final int NUN = 27;
        public static final int NYA = 56;
        public static final int PE = 28;
        public static final int QAF = 29;
        public static final int QAPH = 30;
        public static final int REH = 31;
        public static final int REVERSED_PE = 32;
        public static final int ROHINGYA_YEH = 57;
        public static final int SAD = 33;
        public static final int SADHE = 34;
        public static final int SEEN = 35;
        public static final int SEMKATH = 36;
        public static final int SHIN = 37;
        public static final int STRAIGHT_WAW = 85;
        public static final int SWASH_KAF = 38;
        public static final int SYRIAC_WAW = 39;
        public static final int TAH = 40;
        public static final int TAW = 41;
        public static final int TEH_MARBUTA = 42;
        public static final int TEH_MARBUTA_GOAL = 14;
        public static final int TETH = 43;
        public static final int WAW = 44;
        public static final int YEH = 45;
        public static final int YEH_BARREE = 46;
        public static final int YEH_WITH_TAIL = 47;
        public static final int YUDH = 48;
        public static final int YUDH_HE = 49;
        public static final int ZAIN = 50;
        public static final int ZHAIN = 53;
    }

    public interface JoiningType {
        public static final int COUNT = 6;
        public static final int DUAL_JOINING = 2;
        public static final int JOIN_CAUSING = 1;
        public static final int LEFT_JOINING = 3;
        public static final int NON_JOINING = 0;
        public static final int RIGHT_JOINING = 4;
        public static final int TRANSPARENT = 5;
    }

    public interface LineBreak {
        public static final int ALPHABETIC = 2;
        public static final int AMBIGUOUS = 1;
        public static final int BREAK_AFTER = 4;
        public static final int BREAK_BEFORE = 5;
        public static final int BREAK_BOTH = 3;
        public static final int BREAK_SYMBOLS = 27;
        public static final int CARRIAGE_RETURN = 10;
        public static final int CLOSE_PARENTHESIS = 36;
        public static final int CLOSE_PUNCTUATION = 8;
        public static final int COMBINING_MARK = 9;
        public static final int COMPLEX_CONTEXT = 24;
        public static final int CONDITIONAL_JAPANESE_STARTER = 37;
        public static final int CONTINGENT_BREAK = 7;
        public static final int COUNT = 40;
        public static final int EXCLAMATION = 11;
        public static final int GLUE = 12;
        public static final int H2 = 31;
        public static final int H3 = 32;
        public static final int HEBREW_LETTER = 38;
        public static final int HYPHEN = 13;
        public static final int IDEOGRAPHIC = 14;
        public static final int INFIX_NUMERIC = 16;
        public static final int INSEPARABLE = 15;
        public static final int INSEPERABLE = 15;
        public static final int JL = 33;
        public static final int JT = 34;
        public static final int JV = 35;
        public static final int LINE_FEED = 17;
        public static final int MANDATORY_BREAK = 6;
        public static final int NEXT_LINE = 29;
        public static final int NONSTARTER = 18;
        public static final int NUMERIC = 19;
        public static final int OPEN_PUNCTUATION = 20;
        public static final int POSTFIX_NUMERIC = 21;
        public static final int PREFIX_NUMERIC = 22;
        public static final int QUOTATION = 23;
        public static final int REGIONAL_INDICATOR = 39;
        public static final int SPACE = 26;
        public static final int SURROGATE = 25;
        public static final int UNKNOWN = 0;
        public static final int WORD_JOINER = 30;
        public static final int ZWSPACE = 28;
    }

    public interface NumericType {
        public static final int COUNT = 4;
        public static final int DECIMAL = 1;
        public static final int DIGIT = 2;
        public static final int NONE = 0;
        public static final int NUMERIC = 3;
    }

    public interface SentenceBreak {
        public static final int ATERM = 1;
        public static final int CLOSE = 2;
        public static final int COUNT = 15;
        public static final int CR = 11;
        public static final int EXTEND = 12;
        public static final int FORMAT = 3;
        public static final int LF = 13;
        public static final int LOWER = 4;
        public static final int NUMERIC = 5;
        public static final int OLETTER = 6;
        public static final int OTHER = 0;
        public static final int SCONTINUE = 14;
        public static final int SEP = 7;
        public static final int SP = 8;
        public static final int STERM = 9;
        public static final int UPPER = 10;
    }

    private static class StringContextIterator implements ContextIterator {
        protected int cpLimit;
        protected int cpStart;
        protected int dir;
        protected int index;
        protected int limit;
        protected String s;

        StringContextIterator(String s) {
            this.s = s;
            this.limit = s.length();
            this.index = UCharacter.MIN_VALUE;
            this.cpLimit = UCharacter.MIN_VALUE;
            this.cpStart = UCharacter.MIN_VALUE;
            this.dir = UCharacter.MIN_VALUE;
        }

        public void setLimit(int lim) {
            if (lim < 0 || lim > this.s.length()) {
                this.limit = this.s.length();
            } else {
                this.limit = lim;
            }
        }

        public void moveToLimit() {
            int i = this.limit;
            this.cpLimit = i;
            this.cpStart = i;
        }

        public int nextCaseMapCP() {
            this.cpStart = this.cpLimit;
            if (this.cpLimit >= this.limit) {
                return -1;
            }
            int c = this.s.codePointAt(this.cpLimit);
            this.cpLimit += Character.charCount(c);
            return c;
        }

        public int getCPStart() {
            return this.cpStart;
        }

        public int getCPLimit() {
            return this.cpLimit;
        }

        public void reset(int direction) {
            if (direction > 0) {
                this.dir = UCharacter.FOLD_CASE_EXCLUDE_SPECIAL_I;
                this.index = this.cpLimit;
            } else if (direction < 0) {
                this.dir = -1;
                this.index = this.cpStart;
            } else {
                this.dir = UCharacter.MIN_VALUE;
                this.index = UCharacter.MIN_VALUE;
            }
        }

        public int next() {
            int c;
            if (this.dir > 0 && this.index < this.s.length()) {
                c = this.s.codePointAt(this.index);
                this.index += Character.charCount(c);
                return c;
            } else if (this.dir >= 0 || this.index <= 0) {
                return -1;
            } else {
                c = this.s.codePointBefore(this.index);
                this.index -= Character.charCount(c);
                return c;
            }
        }
    }

    private static final class UCharacterTypeIterator implements RangeValueIterator {
        private static final MaskType MASK_TYPE = null;
        private Range range;
        private Iterator<Range> trieIterator;

        private static final class MaskType implements ValueMapper {
            private MaskType() {
            }

            public int map(int value) {
                return value & UCharacter.UNIT_SEPARATOR_;
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.lang.UCharacter.UCharacterTypeIterator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.lang.UCharacter.UCharacterTypeIterator.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.lang.UCharacter.UCharacterTypeIterator.<clinit>():void");
        }

        UCharacterTypeIterator() {
            reset();
        }

        public boolean next(RangeValueIterator.Element element) {
            if (this.trieIterator.hasNext()) {
                Range range = (Range) this.trieIterator.next();
                this.range = range;
                if (!range.leadSurrogate) {
                    element.start = this.range.startCodePoint;
                    element.limit = this.range.endCodePoint + UCharacter.FOLD_CASE_EXCLUDE_SPECIAL_I;
                    element.value = this.range.value;
                    return true;
                }
            }
            return false;
        }

        public void reset() {
            this.trieIterator = UCharacterProperty.INSTANCE.m_trie_.iterator(MASK_TYPE);
        }
    }

    public static final class UnicodeBlock extends Subset {
        public static final UnicodeBlock AEGEAN_NUMBERS = null;
        public static final int AEGEAN_NUMBERS_ID = 119;
        public static final UnicodeBlock AHOM = null;
        public static final int AHOM_ID = 253;
        public static final UnicodeBlock ALCHEMICAL_SYMBOLS = null;
        public static final int ALCHEMICAL_SYMBOLS_ID = 208;
        public static final UnicodeBlock ALPHABETIC_PRESENTATION_FORMS = null;
        public static final int ALPHABETIC_PRESENTATION_FORMS_ID = 80;
        public static final UnicodeBlock ANATOLIAN_HIEROGLYPHS = null;
        public static final int ANATOLIAN_HIEROGLYPHS_ID = 254;
        public static final UnicodeBlock ANCIENT_GREEK_MUSICAL_NOTATION = null;
        public static final int ANCIENT_GREEK_MUSICAL_NOTATION_ID = 126;
        public static final UnicodeBlock ANCIENT_GREEK_NUMBERS = null;
        public static final int ANCIENT_GREEK_NUMBERS_ID = 127;
        public static final UnicodeBlock ANCIENT_SYMBOLS = null;
        public static final int ANCIENT_SYMBOLS_ID = 165;
        public static final UnicodeBlock ARABIC = null;
        public static final UnicodeBlock ARABIC_EXTENDED_A = null;
        public static final int ARABIC_EXTENDED_A_ID = 210;
        public static final int ARABIC_ID = 12;
        public static final UnicodeBlock ARABIC_MATHEMATICAL_ALPHABETIC_SYMBOLS = null;
        public static final int ARABIC_MATHEMATICAL_ALPHABETIC_SYMBOLS_ID = 211;
        public static final UnicodeBlock ARABIC_PRESENTATION_FORMS_A = null;
        public static final int ARABIC_PRESENTATION_FORMS_A_ID = 81;
        public static final UnicodeBlock ARABIC_PRESENTATION_FORMS_B = null;
        public static final int ARABIC_PRESENTATION_FORMS_B_ID = 85;
        public static final UnicodeBlock ARABIC_SUPPLEMENT = null;
        public static final int ARABIC_SUPPLEMENT_ID = 128;
        public static final UnicodeBlock ARMENIAN = null;
        public static final int ARMENIAN_ID = 10;
        public static final UnicodeBlock ARROWS = null;
        public static final int ARROWS_ID = 46;
        public static final UnicodeBlock AVESTAN = null;
        public static final int AVESTAN_ID = 188;
        public static final UnicodeBlock BALINESE = null;
        public static final int BALINESE_ID = 147;
        public static final UnicodeBlock BAMUM = null;
        public static final int BAMUM_ID = 177;
        public static final UnicodeBlock BAMUM_SUPPLEMENT = null;
        public static final int BAMUM_SUPPLEMENT_ID = 202;
        public static final UnicodeBlock BASIC_LATIN = null;
        public static final int BASIC_LATIN_ID = 1;
        public static final UnicodeBlock BASSA_VAH = null;
        public static final int BASSA_VAH_ID = 221;
        public static final UnicodeBlock BATAK = null;
        public static final int BATAK_ID = 199;
        public static final UnicodeBlock BENGALI = null;
        public static final int BENGALI_ID = 16;
        private static final UnicodeBlock[] BLOCKS_ = null;
        public static final UnicodeBlock BLOCK_ELEMENTS = null;
        public static final int BLOCK_ELEMENTS_ID = 53;
        public static final UnicodeBlock BOPOMOFO = null;
        public static final UnicodeBlock BOPOMOFO_EXTENDED = null;
        public static final int BOPOMOFO_EXTENDED_ID = 67;
        public static final int BOPOMOFO_ID = 64;
        public static final UnicodeBlock BOX_DRAWING = null;
        public static final int BOX_DRAWING_ID = 52;
        public static final UnicodeBlock BRAHMI = null;
        public static final int BRAHMI_ID = 201;
        public static final UnicodeBlock BRAILLE_PATTERNS = null;
        public static final int BRAILLE_PATTERNS_ID = 57;
        public static final UnicodeBlock BUGINESE = null;
        public static final int BUGINESE_ID = 129;
        public static final UnicodeBlock BUHID = null;
        public static final int BUHID_ID = 100;
        public static final UnicodeBlock BYZANTINE_MUSICAL_SYMBOLS = null;
        public static final int BYZANTINE_MUSICAL_SYMBOLS_ID = 91;
        public static final UnicodeBlock CARIAN = null;
        public static final int CARIAN_ID = 168;
        public static final UnicodeBlock CAUCASIAN_ALBANIAN = null;
        public static final int CAUCASIAN_ALBANIAN_ID = 222;
        public static final UnicodeBlock CHAKMA = null;
        public static final int CHAKMA_ID = 212;
        public static final UnicodeBlock CHAM = null;
        public static final int CHAM_ID = 164;
        public static final UnicodeBlock CHEROKEE = null;
        public static final int CHEROKEE_ID = 32;
        public static final UnicodeBlock CHEROKEE_SUPPLEMENT = null;
        public static final int CHEROKEE_SUPPLEMENT_ID = 255;
        public static final UnicodeBlock CJK_COMPATIBILITY = null;
        public static final UnicodeBlock CJK_COMPATIBILITY_FORMS = null;
        public static final int CJK_COMPATIBILITY_FORMS_ID = 83;
        public static final int CJK_COMPATIBILITY_ID = 69;
        public static final UnicodeBlock CJK_COMPATIBILITY_IDEOGRAPHS = null;
        public static final int CJK_COMPATIBILITY_IDEOGRAPHS_ID = 79;
        public static final UnicodeBlock CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT = null;
        public static final int CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT_ID = 95;
        public static final UnicodeBlock CJK_RADICALS_SUPPLEMENT = null;
        public static final int CJK_RADICALS_SUPPLEMENT_ID = 58;
        public static final UnicodeBlock CJK_STROKES = null;
        public static final int CJK_STROKES_ID = 130;
        public static final UnicodeBlock CJK_SYMBOLS_AND_PUNCTUATION = null;
        public static final int CJK_SYMBOLS_AND_PUNCTUATION_ID = 61;
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS = null;
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A = null;
        public static final int CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A_ID = 70;
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B = null;
        public static final int CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B_ID = 94;
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C = null;
        public static final int CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C_ID = 197;
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D = null;
        public static final int CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D_ID = 209;
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_E = null;
        public static final int CJK_UNIFIED_IDEOGRAPHS_EXTENSION_E_ID = 256;
        public static final int CJK_UNIFIED_IDEOGRAPHS_ID = 71;
        public static final UnicodeBlock COMBINING_DIACRITICAL_MARKS = null;
        public static final UnicodeBlock COMBINING_DIACRITICAL_MARKS_EXTENDED = null;
        public static final int COMBINING_DIACRITICAL_MARKS_EXTENDED_ID = 224;
        public static final int COMBINING_DIACRITICAL_MARKS_ID = 7;
        public static final UnicodeBlock COMBINING_DIACRITICAL_MARKS_SUPPLEMENT = null;
        public static final int COMBINING_DIACRITICAL_MARKS_SUPPLEMENT_ID = 131;
        public static final UnicodeBlock COMBINING_HALF_MARKS = null;
        public static final int COMBINING_HALF_MARKS_ID = 82;
        public static final UnicodeBlock COMBINING_MARKS_FOR_SYMBOLS = null;
        public static final int COMBINING_MARKS_FOR_SYMBOLS_ID = 43;
        public static final UnicodeBlock COMMON_INDIC_NUMBER_FORMS = null;
        public static final int COMMON_INDIC_NUMBER_FORMS_ID = 178;
        public static final UnicodeBlock CONTROL_PICTURES = null;
        public static final int CONTROL_PICTURES_ID = 49;
        public static final UnicodeBlock COPTIC = null;
        public static final UnicodeBlock COPTIC_EPACT_NUMBERS = null;
        public static final int COPTIC_EPACT_NUMBERS_ID = 223;
        public static final int COPTIC_ID = 132;
        public static final int COUNT = 263;
        public static final UnicodeBlock COUNTING_ROD_NUMERALS = null;
        public static final int COUNTING_ROD_NUMERALS_ID = 154;
        public static final UnicodeBlock CUNEIFORM = null;
        public static final int CUNEIFORM_ID = 152;
        public static final UnicodeBlock CUNEIFORM_NUMBERS_AND_PUNCTUATION = null;
        public static final int CUNEIFORM_NUMBERS_AND_PUNCTUATION_ID = 153;
        public static final UnicodeBlock CURRENCY_SYMBOLS = null;
        public static final int CURRENCY_SYMBOLS_ID = 42;
        public static final UnicodeBlock CYPRIOT_SYLLABARY = null;
        public static final int CYPRIOT_SYLLABARY_ID = 123;
        public static final UnicodeBlock CYRILLIC = null;
        public static final UnicodeBlock CYRILLIC_EXTENDED_A = null;
        public static final int CYRILLIC_EXTENDED_A_ID = 158;
        public static final UnicodeBlock CYRILLIC_EXTENDED_B = null;
        public static final int CYRILLIC_EXTENDED_B_ID = 160;
        public static final int CYRILLIC_ID = 9;
        public static final UnicodeBlock CYRILLIC_SUPPLEMENT = null;
        public static final UnicodeBlock CYRILLIC_SUPPLEMENTARY = null;
        public static final int CYRILLIC_SUPPLEMENTARY_ID = 97;
        public static final int CYRILLIC_SUPPLEMENT_ID = 97;
        public static final UnicodeBlock DESERET = null;
        public static final int DESERET_ID = 90;
        public static final UnicodeBlock DEVANAGARI = null;
        public static final UnicodeBlock DEVANAGARI_EXTENDED = null;
        public static final int DEVANAGARI_EXTENDED_ID = 179;
        public static final int DEVANAGARI_ID = 15;
        public static final UnicodeBlock DINGBATS = null;
        public static final int DINGBATS_ID = 56;
        public static final UnicodeBlock DOMINO_TILES = null;
        public static final int DOMINO_TILES_ID = 171;
        public static final UnicodeBlock DUPLOYAN = null;
        public static final int DUPLOYAN_ID = 225;
        public static final UnicodeBlock EARLY_DYNASTIC_CUNEIFORM = null;
        public static final int EARLY_DYNASTIC_CUNEIFORM_ID = 257;
        public static final UnicodeBlock EGYPTIAN_HIEROGLYPHS = null;
        public static final int EGYPTIAN_HIEROGLYPHS_ID = 194;
        public static final UnicodeBlock ELBASAN = null;
        public static final int ELBASAN_ID = 226;
        public static final UnicodeBlock EMOTICONS = null;
        public static final int EMOTICONS_ID = 206;
        public static final UnicodeBlock ENCLOSED_ALPHANUMERICS = null;
        public static final int ENCLOSED_ALPHANUMERICS_ID = 51;
        public static final UnicodeBlock ENCLOSED_ALPHANUMERIC_SUPPLEMENT = null;
        public static final int ENCLOSED_ALPHANUMERIC_SUPPLEMENT_ID = 195;
        public static final UnicodeBlock ENCLOSED_CJK_LETTERS_AND_MONTHS = null;
        public static final int ENCLOSED_CJK_LETTERS_AND_MONTHS_ID = 68;
        public static final UnicodeBlock ENCLOSED_IDEOGRAPHIC_SUPPLEMENT = null;
        public static final int ENCLOSED_IDEOGRAPHIC_SUPPLEMENT_ID = 196;
        public static final UnicodeBlock ETHIOPIC = null;
        public static final UnicodeBlock ETHIOPIC_EXTENDED = null;
        public static final UnicodeBlock ETHIOPIC_EXTENDED_A = null;
        public static final int ETHIOPIC_EXTENDED_A_ID = 200;
        public static final int ETHIOPIC_EXTENDED_ID = 133;
        public static final int ETHIOPIC_ID = 31;
        public static final UnicodeBlock ETHIOPIC_SUPPLEMENT = null;
        public static final int ETHIOPIC_SUPPLEMENT_ID = 134;
        public static final UnicodeBlock GENERAL_PUNCTUATION = null;
        public static final int GENERAL_PUNCTUATION_ID = 40;
        public static final UnicodeBlock GEOMETRIC_SHAPES = null;
        public static final UnicodeBlock GEOMETRIC_SHAPES_EXTENDED = null;
        public static final int GEOMETRIC_SHAPES_EXTENDED_ID = 227;
        public static final int GEOMETRIC_SHAPES_ID = 54;
        public static final UnicodeBlock GEORGIAN = null;
        public static final int GEORGIAN_ID = 29;
        public static final UnicodeBlock GEORGIAN_SUPPLEMENT = null;
        public static final int GEORGIAN_SUPPLEMENT_ID = 135;
        public static final UnicodeBlock GLAGOLITIC = null;
        public static final int GLAGOLITIC_ID = 136;
        public static final UnicodeBlock GOTHIC = null;
        public static final int GOTHIC_ID = 89;
        public static final UnicodeBlock GRANTHA = null;
        public static final int GRANTHA_ID = 228;
        public static final UnicodeBlock GREEK = null;
        public static final UnicodeBlock GREEK_EXTENDED = null;
        public static final int GREEK_EXTENDED_ID = 39;
        public static final int GREEK_ID = 8;
        public static final UnicodeBlock GUJARATI = null;
        public static final int GUJARATI_ID = 18;
        public static final UnicodeBlock GURMUKHI = null;
        public static final int GURMUKHI_ID = 17;
        public static final UnicodeBlock HALFWIDTH_AND_FULLWIDTH_FORMS = null;
        public static final int HALFWIDTH_AND_FULLWIDTH_FORMS_ID = 87;
        public static final UnicodeBlock HANGUL_COMPATIBILITY_JAMO = null;
        public static final int HANGUL_COMPATIBILITY_JAMO_ID = 65;
        public static final UnicodeBlock HANGUL_JAMO = null;
        public static final UnicodeBlock HANGUL_JAMO_EXTENDED_A = null;
        public static final int HANGUL_JAMO_EXTENDED_A_ID = 180;
        public static final UnicodeBlock HANGUL_JAMO_EXTENDED_B = null;
        public static final int HANGUL_JAMO_EXTENDED_B_ID = 185;
        public static final int HANGUL_JAMO_ID = 30;
        public static final UnicodeBlock HANGUL_SYLLABLES = null;
        public static final int HANGUL_SYLLABLES_ID = 74;
        public static final UnicodeBlock HANUNOO = null;
        public static final int HANUNOO_ID = 99;
        public static final UnicodeBlock HATRAN = null;
        public static final int HATRAN_ID = 258;
        public static final UnicodeBlock HEBREW = null;
        public static final int HEBREW_ID = 11;
        public static final UnicodeBlock HIGH_PRIVATE_USE_SURROGATES = null;
        public static final int HIGH_PRIVATE_USE_SURROGATES_ID = 76;
        public static final UnicodeBlock HIGH_SURROGATES = null;
        public static final int HIGH_SURROGATES_ID = 75;
        public static final UnicodeBlock HIRAGANA = null;
        public static final int HIRAGANA_ID = 62;
        public static final UnicodeBlock IDEOGRAPHIC_DESCRIPTION_CHARACTERS = null;
        public static final int IDEOGRAPHIC_DESCRIPTION_CHARACTERS_ID = 60;
        public static final UnicodeBlock IMPERIAL_ARAMAIC = null;
        public static final int IMPERIAL_ARAMAIC_ID = 186;
        public static final UnicodeBlock INSCRIPTIONAL_PAHLAVI = null;
        public static final int INSCRIPTIONAL_PAHLAVI_ID = 190;
        public static final UnicodeBlock INSCRIPTIONAL_PARTHIAN = null;
        public static final int INSCRIPTIONAL_PARTHIAN_ID = 189;
        public static final UnicodeBlock INVALID_CODE = null;
        public static final int INVALID_CODE_ID = -1;
        public static final UnicodeBlock IPA_EXTENSIONS = null;
        public static final int IPA_EXTENSIONS_ID = 5;
        public static final UnicodeBlock JAVANESE = null;
        public static final int JAVANESE_ID = 181;
        public static final UnicodeBlock KAITHI = null;
        public static final int KAITHI_ID = 193;
        public static final UnicodeBlock KANA_SUPPLEMENT = null;
        public static final int KANA_SUPPLEMENT_ID = 203;
        public static final UnicodeBlock KANBUN = null;
        public static final int KANBUN_ID = 66;
        public static final UnicodeBlock KANGXI_RADICALS = null;
        public static final int KANGXI_RADICALS_ID = 59;
        public static final UnicodeBlock KANNADA = null;
        public static final int KANNADA_ID = 22;
        public static final UnicodeBlock KATAKANA = null;
        public static final int KATAKANA_ID = 63;
        public static final UnicodeBlock KATAKANA_PHONETIC_EXTENSIONS = null;
        public static final int KATAKANA_PHONETIC_EXTENSIONS_ID = 107;
        public static final UnicodeBlock KAYAH_LI = null;
        public static final int KAYAH_LI_ID = 162;
        public static final UnicodeBlock KHAROSHTHI = null;
        public static final int KHAROSHTHI_ID = 137;
        public static final UnicodeBlock KHMER = null;
        public static final int KHMER_ID = 36;
        public static final UnicodeBlock KHMER_SYMBOLS = null;
        public static final int KHMER_SYMBOLS_ID = 113;
        public static final UnicodeBlock KHOJKI = null;
        public static final int KHOJKI_ID = 229;
        public static final UnicodeBlock KHUDAWADI = null;
        public static final int KHUDAWADI_ID = 230;
        public static final UnicodeBlock LAO = null;
        public static final int LAO_ID = 26;
        public static final UnicodeBlock LATIN_1_SUPPLEMENT = null;
        public static final int LATIN_1_SUPPLEMENT_ID = 2;
        public static final UnicodeBlock LATIN_EXTENDED_A = null;
        public static final UnicodeBlock LATIN_EXTENDED_ADDITIONAL = null;
        public static final int LATIN_EXTENDED_ADDITIONAL_ID = 38;
        public static final int LATIN_EXTENDED_A_ID = 3;
        public static final UnicodeBlock LATIN_EXTENDED_B = null;
        public static final int LATIN_EXTENDED_B_ID = 4;
        public static final UnicodeBlock LATIN_EXTENDED_C = null;
        public static final int LATIN_EXTENDED_C_ID = 148;
        public static final UnicodeBlock LATIN_EXTENDED_D = null;
        public static final int LATIN_EXTENDED_D_ID = 149;
        public static final UnicodeBlock LATIN_EXTENDED_E = null;
        public static final int LATIN_EXTENDED_E_ID = 231;
        public static final UnicodeBlock LEPCHA = null;
        public static final int LEPCHA_ID = 156;
        public static final UnicodeBlock LETTERLIKE_SYMBOLS = null;
        public static final int LETTERLIKE_SYMBOLS_ID = 44;
        public static final UnicodeBlock LIMBU = null;
        public static final int LIMBU_ID = 111;
        public static final UnicodeBlock LINEAR_A = null;
        public static final int LINEAR_A_ID = 232;
        public static final UnicodeBlock LINEAR_B_IDEOGRAMS = null;
        public static final int LINEAR_B_IDEOGRAMS_ID = 118;
        public static final UnicodeBlock LINEAR_B_SYLLABARY = null;
        public static final int LINEAR_B_SYLLABARY_ID = 117;
        public static final UnicodeBlock LISU = null;
        public static final int LISU_ID = 176;
        public static final UnicodeBlock LOW_SURROGATES = null;
        public static final int LOW_SURROGATES_ID = 77;
        public static final UnicodeBlock LYCIAN = null;
        public static final int LYCIAN_ID = 167;
        public static final UnicodeBlock LYDIAN = null;
        public static final int LYDIAN_ID = 169;
        public static final UnicodeBlock MAHAJANI = null;
        public static final int MAHAJANI_ID = 233;
        public static final UnicodeBlock MAHJONG_TILES = null;
        public static final int MAHJONG_TILES_ID = 170;
        public static final UnicodeBlock MALAYALAM = null;
        public static final int MALAYALAM_ID = 23;
        public static final UnicodeBlock MANDAIC = null;
        public static final int MANDAIC_ID = 198;
        public static final UnicodeBlock MANICHAEAN = null;
        public static final int MANICHAEAN_ID = 234;
        public static final UnicodeBlock MATHEMATICAL_ALPHANUMERIC_SYMBOLS = null;
        public static final int MATHEMATICAL_ALPHANUMERIC_SYMBOLS_ID = 93;
        public static final UnicodeBlock MATHEMATICAL_OPERATORS = null;
        public static final int MATHEMATICAL_OPERATORS_ID = 47;
        public static final UnicodeBlock MEETEI_MAYEK = null;
        public static final UnicodeBlock MEETEI_MAYEK_EXTENSIONS = null;
        public static final int MEETEI_MAYEK_EXTENSIONS_ID = 213;
        public static final int MEETEI_MAYEK_ID = 184;
        public static final UnicodeBlock MENDE_KIKAKUI = null;
        public static final int MENDE_KIKAKUI_ID = 235;
        public static final UnicodeBlock MEROITIC_CURSIVE = null;
        public static final int MEROITIC_CURSIVE_ID = 214;
        public static final UnicodeBlock MEROITIC_HIEROGLYPHS = null;
        public static final int MEROITIC_HIEROGLYPHS_ID = 215;
        public static final UnicodeBlock MIAO = null;
        public static final int MIAO_ID = 216;
        public static final UnicodeBlock MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A = null;
        public static final int MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A_ID = 102;
        public static final UnicodeBlock MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B = null;
        public static final int MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B_ID = 105;
        public static final UnicodeBlock MISCELLANEOUS_SYMBOLS = null;
        public static final UnicodeBlock MISCELLANEOUS_SYMBOLS_AND_ARROWS = null;
        public static final int MISCELLANEOUS_SYMBOLS_AND_ARROWS_ID = 115;
        public static final UnicodeBlock MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS = null;
        public static final int MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS_ID = 205;
        public static final int MISCELLANEOUS_SYMBOLS_ID = 55;
        public static final UnicodeBlock MISCELLANEOUS_TECHNICAL = null;
        public static final int MISCELLANEOUS_TECHNICAL_ID = 48;
        public static final UnicodeBlock MODI = null;
        public static final UnicodeBlock MODIFIER_TONE_LETTERS = null;
        public static final int MODIFIER_TONE_LETTERS_ID = 138;
        public static final int MODI_ID = 236;
        public static final UnicodeBlock MONGOLIAN = null;
        public static final int MONGOLIAN_ID = 37;
        public static final UnicodeBlock MRO = null;
        public static final int MRO_ID = 237;
        public static final UnicodeBlock MULTANI = null;
        public static final int MULTANI_ID = 259;
        public static final UnicodeBlock MUSICAL_SYMBOLS = null;
        public static final int MUSICAL_SYMBOLS_ID = 92;
        public static final UnicodeBlock MYANMAR = null;
        public static final UnicodeBlock MYANMAR_EXTENDED_A = null;
        public static final int MYANMAR_EXTENDED_A_ID = 182;
        public static final UnicodeBlock MYANMAR_EXTENDED_B = null;
        public static final int MYANMAR_EXTENDED_B_ID = 238;
        public static final int MYANMAR_ID = 28;
        public static final UnicodeBlock NABATAEAN = null;
        public static final int NABATAEAN_ID = 239;
        public static final UnicodeBlock NEW_TAI_LUE = null;
        public static final int NEW_TAI_LUE_ID = 139;
        public static final UnicodeBlock NKO = null;
        public static final int NKO_ID = 146;
        public static final UnicodeBlock NO_BLOCK = null;
        public static final UnicodeBlock NUMBER_FORMS = null;
        public static final int NUMBER_FORMS_ID = 45;
        public static final UnicodeBlock OGHAM = null;
        public static final int OGHAM_ID = 34;
        public static final UnicodeBlock OLD_HUNGARIAN = null;
        public static final int OLD_HUNGARIAN_ID = 260;
        public static final UnicodeBlock OLD_ITALIC = null;
        public static final int OLD_ITALIC_ID = 88;
        public static final UnicodeBlock OLD_NORTH_ARABIAN = null;
        public static final int OLD_NORTH_ARABIAN_ID = 240;
        public static final UnicodeBlock OLD_PERMIC = null;
        public static final int OLD_PERMIC_ID = 241;
        public static final UnicodeBlock OLD_PERSIAN = null;
        public static final int OLD_PERSIAN_ID = 140;
        public static final UnicodeBlock OLD_SOUTH_ARABIAN = null;
        public static final int OLD_SOUTH_ARABIAN_ID = 187;
        public static final UnicodeBlock OLD_TURKIC = null;
        public static final int OLD_TURKIC_ID = 191;
        public static final UnicodeBlock OL_CHIKI = null;
        public static final int OL_CHIKI_ID = 157;
        public static final UnicodeBlock OPTICAL_CHARACTER_RECOGNITION = null;
        public static final int OPTICAL_CHARACTER_RECOGNITION_ID = 50;
        public static final UnicodeBlock ORIYA = null;
        public static final int ORIYA_ID = 19;
        public static final UnicodeBlock ORNAMENTAL_DINGBATS = null;
        public static final int ORNAMENTAL_DINGBATS_ID = 242;
        public static final UnicodeBlock OSMANYA = null;
        public static final int OSMANYA_ID = 122;
        public static final UnicodeBlock PAHAWH_HMONG = null;
        public static final int PAHAWH_HMONG_ID = 243;
        public static final UnicodeBlock PALMYRENE = null;
        public static final int PALMYRENE_ID = 244;
        public static final UnicodeBlock PAU_CIN_HAU = null;
        public static final int PAU_CIN_HAU_ID = 245;
        public static final UnicodeBlock PHAGS_PA = null;
        public static final int PHAGS_PA_ID = 150;
        public static final UnicodeBlock PHAISTOS_DISC = null;
        public static final int PHAISTOS_DISC_ID = 166;
        public static final UnicodeBlock PHOENICIAN = null;
        public static final int PHOENICIAN_ID = 151;
        public static final UnicodeBlock PHONETIC_EXTENSIONS = null;
        public static final int PHONETIC_EXTENSIONS_ID = 114;
        public static final UnicodeBlock PHONETIC_EXTENSIONS_SUPPLEMENT = null;
        public static final int PHONETIC_EXTENSIONS_SUPPLEMENT_ID = 141;
        public static final UnicodeBlock PLAYING_CARDS = null;
        public static final int PLAYING_CARDS_ID = 204;
        public static final UnicodeBlock PRIVATE_USE = null;
        public static final UnicodeBlock PRIVATE_USE_AREA = null;
        public static final int PRIVATE_USE_AREA_ID = 78;
        public static final int PRIVATE_USE_ID = 78;
        public static final UnicodeBlock PSALTER_PAHLAVI = null;
        public static final int PSALTER_PAHLAVI_ID = 246;
        public static final UnicodeBlock REJANG = null;
        public static final int REJANG_ID = 163;
        public static final UnicodeBlock RUMI_NUMERAL_SYMBOLS = null;
        public static final int RUMI_NUMERAL_SYMBOLS_ID = 192;
        public static final UnicodeBlock RUNIC = null;
        public static final int RUNIC_ID = 35;
        public static final UnicodeBlock SAMARITAN = null;
        public static final int SAMARITAN_ID = 172;
        public static final UnicodeBlock SAURASHTRA = null;
        public static final int SAURASHTRA_ID = 161;
        public static final UnicodeBlock SHARADA = null;
        public static final int SHARADA_ID = 217;
        public static final UnicodeBlock SHAVIAN = null;
        public static final int SHAVIAN_ID = 121;
        public static final UnicodeBlock SHORTHAND_FORMAT_CONTROLS = null;
        public static final int SHORTHAND_FORMAT_CONTROLS_ID = 247;
        public static final UnicodeBlock SIDDHAM = null;
        public static final int SIDDHAM_ID = 248;
        public static final UnicodeBlock SINHALA = null;
        public static final UnicodeBlock SINHALA_ARCHAIC_NUMBERS = null;
        public static final int SINHALA_ARCHAIC_NUMBERS_ID = 249;
        public static final int SINHALA_ID = 24;
        public static final UnicodeBlock SMALL_FORM_VARIANTS = null;
        public static final int SMALL_FORM_VARIANTS_ID = 84;
        public static final UnicodeBlock SORA_SOMPENG = null;
        public static final int SORA_SOMPENG_ID = 218;
        public static final UnicodeBlock SPACING_MODIFIER_LETTERS = null;
        public static final int SPACING_MODIFIER_LETTERS_ID = 6;
        public static final UnicodeBlock SPECIALS = null;
        public static final int SPECIALS_ID = 86;
        public static final UnicodeBlock SUNDANESE = null;
        public static final int SUNDANESE_ID = 155;
        public static final UnicodeBlock SUNDANESE_SUPPLEMENT = null;
        public static final int SUNDANESE_SUPPLEMENT_ID = 219;
        public static final UnicodeBlock SUPERSCRIPTS_AND_SUBSCRIPTS = null;
        public static final int SUPERSCRIPTS_AND_SUBSCRIPTS_ID = 41;
        public static final UnicodeBlock SUPPLEMENTAL_ARROWS_A = null;
        public static final int SUPPLEMENTAL_ARROWS_A_ID = 103;
        public static final UnicodeBlock SUPPLEMENTAL_ARROWS_B = null;
        public static final int SUPPLEMENTAL_ARROWS_B_ID = 104;
        public static final UnicodeBlock SUPPLEMENTAL_ARROWS_C = null;
        public static final int SUPPLEMENTAL_ARROWS_C_ID = 250;
        public static final UnicodeBlock SUPPLEMENTAL_MATHEMATICAL_OPERATORS = null;
        public static final int SUPPLEMENTAL_MATHEMATICAL_OPERATORS_ID = 106;
        public static final UnicodeBlock SUPPLEMENTAL_PUNCTUATION = null;
        public static final int SUPPLEMENTAL_PUNCTUATION_ID = 142;
        public static final UnicodeBlock SUPPLEMENTAL_SYMBOLS_AND_PICTOGRAPHS = null;
        public static final int SUPPLEMENTAL_SYMBOLS_AND_PICTOGRAPHS_ID = 261;
        public static final UnicodeBlock SUPPLEMENTARY_PRIVATE_USE_AREA_A = null;
        public static final int SUPPLEMENTARY_PRIVATE_USE_AREA_A_ID = 109;
        public static final UnicodeBlock SUPPLEMENTARY_PRIVATE_USE_AREA_B = null;
        public static final int SUPPLEMENTARY_PRIVATE_USE_AREA_B_ID = 110;
        public static final UnicodeBlock SUTTON_SIGNWRITING = null;
        public static final int SUTTON_SIGNWRITING_ID = 262;
        public static final UnicodeBlock SYLOTI_NAGRI = null;
        public static final int SYLOTI_NAGRI_ID = 143;
        public static final UnicodeBlock SYRIAC = null;
        public static final int SYRIAC_ID = 13;
        public static final UnicodeBlock TAGALOG = null;
        public static final int TAGALOG_ID = 98;
        public static final UnicodeBlock TAGBANWA = null;
        public static final int TAGBANWA_ID = 101;
        public static final UnicodeBlock TAGS = null;
        public static final int TAGS_ID = 96;
        public static final UnicodeBlock TAI_LE = null;
        public static final int TAI_LE_ID = 112;
        public static final UnicodeBlock TAI_THAM = null;
        public static final int TAI_THAM_ID = 174;
        public static final UnicodeBlock TAI_VIET = null;
        public static final int TAI_VIET_ID = 183;
        public static final UnicodeBlock TAI_XUAN_JING_SYMBOLS = null;
        public static final int TAI_XUAN_JING_SYMBOLS_ID = 124;
        public static final UnicodeBlock TAKRI = null;
        public static final int TAKRI_ID = 220;
        public static final UnicodeBlock TAMIL = null;
        public static final int TAMIL_ID = 20;
        public static final UnicodeBlock TELUGU = null;
        public static final int TELUGU_ID = 21;
        public static final UnicodeBlock THAANA = null;
        public static final int THAANA_ID = 14;
        public static final UnicodeBlock THAI = null;
        public static final int THAI_ID = 25;
        public static final UnicodeBlock TIBETAN = null;
        public static final int TIBETAN_ID = 27;
        public static final UnicodeBlock TIFINAGH = null;
        public static final int TIFINAGH_ID = 144;
        public static final UnicodeBlock TIRHUTA = null;
        public static final int TIRHUTA_ID = 251;
        public static final UnicodeBlock TRANSPORT_AND_MAP_SYMBOLS = null;
        public static final int TRANSPORT_AND_MAP_SYMBOLS_ID = 207;
        public static final UnicodeBlock UGARITIC = null;
        public static final int UGARITIC_ID = 120;
        public static final UnicodeBlock UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS = null;
        public static final UnicodeBlock UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_EXTENDED = null;
        public static final int UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_EXTENDED_ID = 173;
        public static final int UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_ID = 33;
        public static final UnicodeBlock VAI = null;
        public static final int VAI_ID = 159;
        public static final UnicodeBlock VARIATION_SELECTORS = null;
        public static final int VARIATION_SELECTORS_ID = 108;
        public static final UnicodeBlock VARIATION_SELECTORS_SUPPLEMENT = null;
        public static final int VARIATION_SELECTORS_SUPPLEMENT_ID = 125;
        public static final UnicodeBlock VEDIC_EXTENSIONS = null;
        public static final int VEDIC_EXTENSIONS_ID = 175;
        public static final UnicodeBlock VERTICAL_FORMS = null;
        public static final int VERTICAL_FORMS_ID = 145;
        public static final UnicodeBlock WARANG_CITI = null;
        public static final int WARANG_CITI_ID = 252;
        public static final UnicodeBlock YIJING_HEXAGRAM_SYMBOLS = null;
        public static final int YIJING_HEXAGRAM_SYMBOLS_ID = 116;
        public static final UnicodeBlock YI_RADICALS = null;
        public static final int YI_RADICALS_ID = 73;
        public static final UnicodeBlock YI_SYLLABLES = null;
        public static final int YI_SYLLABLES_ID = 72;
        private static SoftReference<Map<String, UnicodeBlock>> mref;
        private int m_id_;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.lang.UCharacter.UnicodeBlock.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.lang.UCharacter.UnicodeBlock.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.lang.UCharacter.UnicodeBlock.<clinit>():void");
        }

        public static UnicodeBlock getInstance(int id) {
            if (id < 0 || id >= BLOCKS_.length) {
                return INVALID_CODE;
            }
            return BLOCKS_[id];
        }

        public static UnicodeBlock of(int ch) {
            if (ch > UCharacter.MAX_VALUE) {
                return INVALID_CODE;
            }
            return getInstance(UCharacterProperty.INSTANCE.getIntPropertyValue(ch, ReorderCodes.PUNCTUATION));
        }

        public static final UnicodeBlock forName(String blockName) {
            UnicodeBlock b;
            Map map = null;
            if (mref != null) {
                map = (Map) mref.get();
            }
            if (map == null) {
                map = new HashMap(BLOCKS_.length);
                for (int i = UCharacter.MIN_VALUE; i < BLOCKS_.length; i += BASIC_LATIN_ID) {
                    b = BLOCKS_[i];
                    map.put(trimBlockName(UCharacter.getPropertyValueName(ReorderCodes.PUNCTUATION, b.getID(), BASIC_LATIN_ID)), b);
                }
                mref = new SoftReference(map);
            }
            b = (UnicodeBlock) map.get(trimBlockName(blockName));
            if (b != null) {
                return b;
            }
            throw new IllegalArgumentException();
        }

        private static String trimBlockName(String name) {
            String upper = name.toUpperCase(Locale.ENGLISH);
            StringBuilder result = new StringBuilder(upper.length());
            for (int i = UCharacter.MIN_VALUE; i < upper.length(); i += BASIC_LATIN_ID) {
                char c = upper.charAt(i);
                if (!(c == ' ' || c == '_' || c == '-')) {
                    result.append(c);
                }
            }
            return result.toString();
        }

        public int getID() {
            return this.m_id_;
        }

        private UnicodeBlock(String name, int id) {
            super(name);
            this.m_id_ = id;
            if (id >= 0) {
                BLOCKS_[id] = this;
            }
        }
    }

    public interface WordBreak {
        public static final int ALETTER = 1;
        public static final int COUNT = 17;
        public static final int CR = 8;
        public static final int DOUBLE_QUOTE = 16;
        public static final int EXTEND = 9;
        public static final int EXTENDNUMLET = 7;
        public static final int FORMAT = 2;
        public static final int HEBREW_LETTER = 14;
        public static final int KATAKANA = 3;
        public static final int LF = 10;
        public static final int MIDLETTER = 4;
        public static final int MIDNUM = 5;
        public static final int MIDNUMLET = 11;
        public static final int NEWLINE = 12;
        public static final int NUMERIC = 6;
        public static final int OTHER = 0;
        public static final int REGIONAL_INDICATOR = 13;
        public static final int SINGLE_QUOTE = 15;
    }

    public static final java.lang.String foldCase(java.lang.String r1, int r2) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.lang.UCharacter.foldCase(java.lang.String, int):java.lang.String
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.lang.UCharacter.foldCase(java.lang.String, int):java.lang.String");
    }

    public static java.lang.String toLowerCase(android.icu.util.ULocale r1, java.lang.String r2) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.lang.UCharacter.toLowerCase(android.icu.util.ULocale, java.lang.String):java.lang.String
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.lang.UCharacter.toLowerCase(android.icu.util.ULocale, java.lang.String):java.lang.String");
    }

    public static java.lang.String toTitleCase(android.icu.util.ULocale r1, java.lang.String r2, android.icu.text.BreakIterator r3, int r4) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.lang.UCharacter.toTitleCase(android.icu.util.ULocale, java.lang.String, android.icu.text.BreakIterator, int):java.lang.String
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.lang.UCharacter.toTitleCase(android.icu.util.ULocale, java.lang.String, android.icu.text.BreakIterator, int):java.lang.String");
    }

    public static java.lang.String toUpperCase(android.icu.util.ULocale r1, java.lang.String r2) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.lang.UCharacter.toUpperCase(android.icu.util.ULocale, java.lang.String):java.lang.String
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.lang.UCharacter.toUpperCase(android.icu.util.ULocale, java.lang.String):java.lang.String");
    }

    public static int digit(int ch, int radix) {
        if (MIN_RADIX > radix || radix > MAX_RADIX) {
            return -1;
        }
        int value = digit(ch);
        if (value < 0) {
            value = UCharacterProperty.getEuropeanDigit(ch);
        }
        if (value >= radix) {
            value = -1;
        }
        return value;
    }

    public static int digit(int ch) {
        return UCharacterProperty.INSTANCE.digit(ch);
    }

    public static int getNumericValue(int ch) {
        return UCharacterProperty.INSTANCE.getNumericValue(ch);
    }

    public static double getUnicodeNumericValue(int ch) {
        return UCharacterProperty.INSTANCE.getUnicodeNumericValue(ch);
    }

    @Deprecated
    public static boolean isSpace(int ch) {
        if (ch > 32) {
            return false;
        }
        if (ch == 32 || ch == 9 || ch == 10 || ch == 12 || ch == 13) {
            return true;
        }
        return false;
    }

    public static int getType(int ch) {
        return UCharacterProperty.INSTANCE.getType(ch);
    }

    public static boolean isDefined(int ch) {
        return getType(ch) != 0;
    }

    public static boolean isDigit(int ch) {
        return getType(ch) == 9;
    }

    public static boolean isISOControl(int ch) {
        if (ch < 0 || ch > APPLICATION_PROGRAM_COMMAND_) {
            return false;
        }
        return ch <= UNIT_SEPARATOR_ || ch >= DELETE_;
    }

    public static boolean isLetter(int ch) {
        return ((FOLD_CASE_EXCLUDE_SPECIAL_I << getType(ch)) & 62) != 0;
    }

    public static boolean isLetterOrDigit(int ch) {
        return ((FOLD_CASE_EXCLUDE_SPECIAL_I << getType(ch)) & 574) != 0;
    }

    @Deprecated
    public static boolean isJavaLetter(int cp) {
        return isJavaIdentifierStart(cp);
    }

    @Deprecated
    public static boolean isJavaLetterOrDigit(int cp) {
        return isJavaIdentifierPart(cp);
    }

    public static boolean isJavaIdentifierStart(int cp) {
        return Character.isJavaIdentifierStart((char) cp);
    }

    public static boolean isJavaIdentifierPart(int cp) {
        return Character.isJavaIdentifierPart((char) cp);
    }

    public static boolean isLowerCase(int ch) {
        return getType(ch) == MIN_RADIX;
    }

    public static boolean isWhitespace(int ch) {
        if (((FOLD_CASE_EXCLUDE_SPECIAL_I << getType(ch)) & UProperty.SCRIPT_EXTENSIONS) != 0 && ch != NO_BREAK_SPACE_ && ch != FIGURE_SPACE_ && ch != NARROW_NO_BREAK_SPACE_) {
            return true;
        }
        if (ch >= 9 && ch <= 13) {
            return true;
        }
        if (ch < 28 || ch > UNIT_SEPARATOR_) {
            return false;
        }
        return true;
    }

    public static boolean isSpaceChar(int ch) {
        return ((FOLD_CASE_EXCLUDE_SPECIAL_I << getType(ch)) & UProperty.SCRIPT_EXTENSIONS) != 0;
    }

    public static boolean isTitleCase(int ch) {
        return getType(ch) == 3;
    }

    public static boolean isUnicodeIdentifierPart(int ch) {
        if (((FOLD_CASE_EXCLUDE_SPECIAL_I << getType(ch)) & 4196222) == 0) {
            return isIdentifierIgnorable(ch);
        }
        return true;
    }

    public static boolean isUnicodeIdentifierStart(int ch) {
        return ((FOLD_CASE_EXCLUDE_SPECIAL_I << getType(ch)) & 1086) != 0;
    }

    public static boolean isIdentifierIgnorable(int ch) {
        boolean z = true;
        boolean z2 = false;
        if (ch <= APPLICATION_PROGRAM_COMMAND_) {
            if (isISOControl(ch) && (ch < 9 || ch > 13)) {
                if (ch >= 28 && ch <= UNIT_SEPARATOR_) {
                    z = false;
                }
                z2 = z;
            }
            return z2;
        }
        if (getType(ch) != 16) {
            z = false;
        }
        return z;
    }

    public static boolean isUpperCase(int ch) {
        return getType(ch) == FOLD_CASE_EXCLUDE_SPECIAL_I;
    }

    public static int toLowerCase(int ch) {
        return UCaseProps.INSTANCE.tolower(ch);
    }

    public static String toString(int ch) {
        if (ch < 0 || ch > MAX_VALUE) {
            return null;
        }
        if (ch < SUPPLEMENTARY_MIN_VALUE) {
            return String.valueOf((char) ch);
        }
        return new String(Character.toChars(ch));
    }

    public static int toTitleCase(int ch) {
        return UCaseProps.INSTANCE.totitle(ch);
    }

    public static int toUpperCase(int ch) {
        return UCaseProps.INSTANCE.toupper(ch);
    }

    public static boolean isSupplementary(int ch) {
        if (ch < SUPPLEMENTARY_MIN_VALUE || ch > MAX_VALUE) {
            return false;
        }
        return true;
    }

    public static boolean isBMP(int ch) {
        return ch >= 0 && ch <= LAST_CHAR_MASK_;
    }

    public static boolean isPrintable(int ch) {
        int cat = getType(ch);
        if (cat == 0 || cat == 15 || cat == 16 || cat == 17 || cat == 18 || cat == 0) {
            return false;
        }
        return true;
    }

    public static boolean isBaseForm(int ch) {
        int cat = getType(ch);
        if (cat == 9 || cat == 11 || cat == 10 || cat == FOLD_CASE_EXCLUDE_SPECIAL_I || cat == MIN_RADIX || cat == 3 || cat == 4 || cat == 5 || cat == 6 || cat == 7 || cat == 8) {
            return true;
        }
        return false;
    }

    public static int getDirection(int ch) {
        return UBiDiProps.INSTANCE.getClass(ch);
    }

    public static boolean isMirrored(int ch) {
        return UBiDiProps.INSTANCE.isMirrored(ch);
    }

    public static int getMirror(int ch) {
        return UBiDiProps.INSTANCE.getMirror(ch);
    }

    public static int getBidiPairedBracket(int c) {
        return UBiDiProps.INSTANCE.getPairedBracket(c);
    }

    public static int getCombiningClass(int ch) {
        return Normalizer2.getNFDInstance().getCombiningClass(ch);
    }

    public static boolean isLegal(int ch) {
        boolean z = true;
        if (ch < 0) {
            return false;
        }
        if (ch < UTF16.SURROGATE_MIN_VALUE) {
            return true;
        }
        if (ch <= UTF16.TRAIL_SURROGATE_MAX_VALUE || UCharacterUtility.isNonCharacter(ch)) {
            return false;
        }
        if (ch > MAX_VALUE) {
            z = false;
        }
        return z;
    }

    public static boolean isLegal(String str) {
        int size = str.length();
        int i = MIN_VALUE;
        while (i < size) {
            int codepoint = str.codePointAt(i);
            if (!isLegal(codepoint)) {
                return false;
            }
            i += Character.charCount(codepoint);
        }
        return true;
    }

    public static VersionInfo getUnicodeVersion() {
        return UCharacterProperty.INSTANCE.m_unicodeVersion_;
    }

    public static String getName(int ch) {
        return UCharacterName.INSTANCE.getName(ch, MIN_VALUE);
    }

    public static String getName(String s, String separator) {
        if (s.length() == FOLD_CASE_EXCLUDE_SPECIAL_I) {
            return getName(s.charAt(MIN_VALUE));
        }
        StringBuilder sb = new StringBuilder();
        int i = MIN_VALUE;
        while (i < s.length()) {
            int cp = s.codePointAt(i);
            if (i != 0) {
                sb.append(separator);
            }
            sb.append(getName(cp));
            i += Character.charCount(cp);
        }
        return sb.toString();
    }

    @Deprecated
    public static String getName1_0(int ch) {
        return null;
    }

    public static String getExtendedName(int ch) {
        return UCharacterName.INSTANCE.getName(ch, MIN_RADIX);
    }

    public static String getNameAlias(int ch) {
        return UCharacterName.INSTANCE.getName(ch, 3);
    }

    @Deprecated
    public static String getISOComment(int ch) {
        return null;
    }

    public static int getCharFromName(String name) {
        return UCharacterName.INSTANCE.getCharFromName(MIN_VALUE, name);
    }

    @Deprecated
    public static int getCharFromName1_0(String name) {
        return -1;
    }

    public static int getCharFromExtendedName(String name) {
        return UCharacterName.INSTANCE.getCharFromName(MIN_RADIX, name);
    }

    public static int getCharFromNameAlias(String name) {
        return UCharacterName.INSTANCE.getCharFromName(3, name);
    }

    public static String getPropertyName(int property, int nameChoice) {
        return UPropertyAliases.INSTANCE.getPropertyName(property, nameChoice);
    }

    public static int getPropertyEnum(CharSequence propertyAlias) {
        int propEnum = UPropertyAliases.INSTANCE.getPropertyEnum(propertyAlias);
        if (propEnum != -1) {
            return propEnum;
        }
        throw new IllegalIcuArgumentException("Invalid name: " + propertyAlias);
    }

    public static String getPropertyValueName(int property, int value, int nameChoice) {
        if ((property != ReorderCodes.SYMBOL && property != UProperty.LEAD_CANONICAL_COMBINING_CLASS && property != UProperty.TRAIL_CANONICAL_COMBINING_CLASS) || value < getIntPropertyMinValue(ReorderCodes.SYMBOL) || value > getIntPropertyMaxValue(ReorderCodes.SYMBOL) || nameChoice < 0 || nameChoice >= MIN_RADIX) {
            return UPropertyAliases.INSTANCE.getPropertyValueName(property, value, nameChoice);
        }
        try {
            return UPropertyAliases.INSTANCE.getPropertyValueName(property, value, nameChoice);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static int getPropertyValueEnum(int property, CharSequence valueAlias) {
        int propEnum = UPropertyAliases.INSTANCE.getPropertyValueEnum(property, valueAlias);
        if (propEnum != -1) {
            return propEnum;
        }
        throw new IllegalIcuArgumentException("Invalid name: " + valueAlias);
    }

    @Deprecated
    public static int getPropertyValueEnumNoThrow(int property, CharSequence valueAlias) {
        return UPropertyAliases.INSTANCE.getPropertyValueEnumNoThrow(property, valueAlias);
    }

    public static int getCodePoint(char lead, char trail) {
        if (Character.isSurrogatePair(lead, trail)) {
            return Character.toCodePoint(lead, trail);
        }
        throw new IllegalArgumentException("Illegal surrogate characters");
    }

    public static int getCodePoint(char char16) {
        if (isLegal((int) char16)) {
            return char16;
        }
        throw new IllegalArgumentException("Illegal codepoint");
    }

    public static String toUpperCase(String str) {
        return toUpperCase(ULocale.getDefault(), str);
    }

    public static String toLowerCase(String str) {
        return toLowerCase(ULocale.getDefault(), str);
    }

    public static String toTitleCase(String str, BreakIterator breakiter) {
        return toTitleCase(ULocale.getDefault(), str, breakiter);
    }

    public static String toUpperCase(Locale locale, String str) {
        return toUpperCase(ULocale.forLocale(locale), str);
    }

    public static String toLowerCase(Locale locale, String str) {
        return toLowerCase(ULocale.forLocale(locale), str);
    }

    public static String toTitleCase(Locale locale, String str, BreakIterator breakiter) {
        return toTitleCase(ULocale.forLocale(locale), str, breakiter);
    }

    public static String toTitleCase(ULocale locale, String str, BreakIterator titleIter) {
        return toTitleCase(locale, str, titleIter, (int) MIN_VALUE);
    }

    @Deprecated
    public static String toTitleFirst(ULocale locale, String str) {
        int i = MIN_VALUE;
        while (i < str.length()) {
            int c = codePointAt((CharSequence) str, i);
            if ((getIntPropertyValue(c, DateUtilsBridge.FORMAT_UTC) & BREAK_MASK) != 0) {
                break;
            } else if (UCaseProps.INSTANCE.getType(c) == 0) {
                i += charCount(c);
            } else {
                String titled = toTitleCase(locale, str.substring(i, charCount(c) + i), BreakIterator.getSentenceInstance(locale), (int) MIN_VALUE);
                if (titled.codePointAt(MIN_VALUE) != c) {
                    int startOfSuffix;
                    StringBuilder result = new StringBuilder(str.length()).append(str, MIN_VALUE, i);
                    if (c == Opcodes.OP_SPUT_OBJECT && locale.getLanguage().equals("nl") && i < str.length() && str.charAt(i + FOLD_CASE_EXCLUDE_SPECIAL_I) == 'j') {
                        result.append("IJ");
                        startOfSuffix = MIN_RADIX;
                    } else {
                        result.append(titled);
                        startOfSuffix = i + charCount(c);
                    }
                    return result.append(str, startOfSuffix, str.length()).toString();
                }
            }
        }
        return str;
    }

    public static String toTitleCase(Locale locale, String str, BreakIterator titleIter, int options) {
        return toTitleCase(ULocale.forLocale(locale), str, titleIter, options);
    }

    public static int foldCase(int ch, boolean defaultmapping) {
        return foldCase(ch, defaultmapping ? MIN_VALUE : FOLD_CASE_EXCLUDE_SPECIAL_I);
    }

    public static String foldCase(String str, boolean defaultmapping) {
        return foldCase(str, defaultmapping ? MIN_VALUE : FOLD_CASE_EXCLUDE_SPECIAL_I);
    }

    public static int foldCase(int ch, int options) {
        return UCaseProps.INSTANCE.fold(ch, options);
    }

    public static int getHanNumericValue(int ch) {
        switch (ch) {
            case IDEOGRAPHIC_NUMBER_ZERO_ /*12295*/:
            case CJK_IDEOGRAPH_COMPLEX_ZERO_ /*38646*/:
                return MIN_VALUE;
            case CJK_IDEOGRAPH_FIRST_ /*19968*/:
            case CJK_IDEOGRAPH_COMPLEX_ONE_ /*22777*/:
                return FOLD_CASE_EXCLUDE_SPECIAL_I;
            case CJK_IDEOGRAPH_SEVENTH_ /*19971*/:
            case CJK_IDEOGRAPH_COMPLEX_SEVEN_ /*26578*/:
                return 7;
            case CJK_IDEOGRAPH_THIRD_ /*19977*/:
            case CJK_IDEOGRAPH_COMPLEX_THREE_ /*21443*/:
                return 3;
            case CJK_IDEOGRAPH_NINETH_ /*20061*/:
            case CJK_IDEOGRAPH_COMPLEX_NINE_ /*29590*/:
                return 9;
            case CJK_IDEOGRAPH_SECOND_ /*20108*/:
            case CJK_IDEOGRAPH_COMPLEX_TWO_ /*36019*/:
                return MIN_RADIX;
            case CJK_IDEOGRAPH_FIFTH_ /*20116*/:
            case CJK_IDEOGRAPH_COMPLEX_FIVE_ /*20237*/:
                return 5;
            case CJK_IDEOGRAPH_COMPLEX_THOUSAND_ /*20191*/:
            case CJK_IDEOGRAPH_THOUSAND_ /*21315*/:
                return Grego.MILLIS_PER_SECOND;
            case CJK_IDEOGRAPH_COMPLEX_HUNDRED_ /*20336*/:
            case CJK_IDEOGRAPH_HUNDRED_ /*30334*/:
                return 100;
            case CJK_IDEOGRAPH_HUNDRED_MILLION_ /*20740*/:
                return 100000000;
            case CJK_IDEOGRAPH_EIGHTH_ /*20843*/:
            case CJK_IDEOGRAPH_COMPLEX_EIGHT_ /*25420*/:
                return 8;
            case CJK_IDEOGRAPH_SIXTH_ /*20845*/:
            case CJK_IDEOGRAPH_COMPLEX_SIX_ /*38520*/:
                return 6;
            case CJK_IDEOGRAPH_TEN_ /*21313*/:
            case CJK_IDEOGRAPH_COMPLEX_TEN_ /*25342*/:
                return 10;
            case CJK_IDEOGRAPH_FOURTH_ /*22235*/:
            case CJK_IDEOGRAPH_COMPLEX_FOUR_ /*32902*/:
                return 4;
            case CJK_IDEOGRAPH_TEN_THOUSAND_ /*33356*/:
                return 10000;
            default:
                return -1;
        }
    }

    public static RangeValueIterator getTypeIterator() {
        return new UCharacterTypeIterator();
    }

    public static ValueIterator getNameIterator() {
        return new UCharacterNameIterator(UCharacterName.INSTANCE, MIN_VALUE);
    }

    @Deprecated
    public static ValueIterator getName1_0Iterator() {
        return new DummyValueIterator();
    }

    public static ValueIterator getExtendedNameIterator() {
        return new UCharacterNameIterator(UCharacterName.INSTANCE, MIN_RADIX);
    }

    public static VersionInfo getAge(int ch) {
        if (ch >= 0 && ch <= MAX_VALUE) {
            return UCharacterProperty.INSTANCE.getAge(ch);
        }
        throw new IllegalArgumentException("Codepoint out of bounds");
    }

    public static boolean hasBinaryProperty(int ch, int property) {
        return UCharacterProperty.INSTANCE.hasBinaryProperty(ch, property);
    }

    public static boolean isUAlphabetic(int ch) {
        return hasBinaryProperty(ch, MIN_VALUE);
    }

    public static boolean isULowercase(int ch) {
        return hasBinaryProperty(ch, 22);
    }

    public static boolean isUUppercase(int ch) {
        return hasBinaryProperty(ch, 30);
    }

    public static boolean isUWhiteSpace(int ch) {
        return hasBinaryProperty(ch, UNIT_SEPARATOR_);
    }

    public static int getIntPropertyValue(int ch, int type) {
        return UCharacterProperty.INSTANCE.getIntPropertyValue(ch, type);
    }

    @Deprecated
    public static String getStringPropertyValue(int propertyEnum, int codepoint, int nameChoice) {
        if ((propertyEnum >= 0 && propertyEnum < 57) || (propertyEnum >= VMDebug.KIND_GLOBAL_EXT_ALLOCATED_OBJECTS && propertyEnum < UProperty.INT_LIMIT)) {
            return getPropertyValueName(propertyEnum, getIntPropertyValue(codepoint, propertyEnum), nameChoice);
        }
        if (propertyEnum == UProperty.NUMERIC_VALUE) {
            return String.valueOf(getUnicodeNumericValue(codepoint));
        }
        switch (propertyEnum) {
            case DateUtilsBridge.FORMAT_ABBREV_TIME /*16384*/:
                return getAge(codepoint).toString();
            case UProperty.BIDI_MIRRORING_GLYPH /*16385*/:
                return toString(getMirror(codepoint));
            case UProperty.CASE_FOLDING /*16386*/:
                return toString(foldCase(codepoint, true));
            case UProperty.ISO_COMMENT /*16387*/:
                return getISOComment(codepoint);
            case UProperty.LOWERCASE_MAPPING /*16388*/:
                return toString(toLowerCase(codepoint));
            case UProperty.NAME /*16389*/:
                return getName(codepoint);
            case UProperty.SIMPLE_CASE_FOLDING /*16390*/:
                return toString(foldCase(codepoint, true));
            case UProperty.SIMPLE_LOWERCASE_MAPPING /*16391*/:
                return toString(toLowerCase(codepoint));
            case UProperty.SIMPLE_TITLECASE_MAPPING /*16392*/:
                return toString(toTitleCase(codepoint));
            case UProperty.SIMPLE_UPPERCASE_MAPPING /*16393*/:
                return toString(toUpperCase(codepoint));
            case UProperty.TITLECASE_MAPPING /*16394*/:
                return toString(toTitleCase(codepoint));
            case UProperty.UNICODE_1_NAME /*16395*/:
                return getName1_0(codepoint);
            case UProperty.UPPERCASE_MAPPING /*16396*/:
                return toString(toUpperCase(codepoint));
            default:
                throw new IllegalArgumentException("Illegal Property Enum");
        }
    }

    public static int getIntPropertyMinValue(int type) {
        return MIN_VALUE;
    }

    public static int getIntPropertyMaxValue(int type) {
        return UCharacterProperty.INSTANCE.getIntPropertyMaxValue(type);
    }

    public static char forDigit(int digit, int radix) {
        return Character.forDigit(digit, radix);
    }

    public static final boolean isValidCodePoint(int cp) {
        return cp >= 0 && cp <= MAX_VALUE;
    }

    public static final boolean isSupplementaryCodePoint(int cp) {
        return Character.isSupplementaryCodePoint(cp);
    }

    public static boolean isHighSurrogate(char ch) {
        return Character.isHighSurrogate(ch);
    }

    public static boolean isLowSurrogate(char ch) {
        return Character.isLowSurrogate(ch);
    }

    public static final boolean isSurrogatePair(char high, char low) {
        return Character.isSurrogatePair(high, low);
    }

    public static int charCount(int cp) {
        return Character.charCount(cp);
    }

    public static final int toCodePoint(char high, char low) {
        return Character.toCodePoint(high, low);
    }

    public static final int codePointAt(CharSequence seq, int index) {
        int index2 = index + FOLD_CASE_EXCLUDE_SPECIAL_I;
        char c1 = seq.charAt(index);
        if (isHighSurrogate(c1) && index2 < seq.length()) {
            char c2 = seq.charAt(index2);
            if (isLowSurrogate(c2)) {
                return toCodePoint(c1, c2);
            }
        }
        return c1;
    }

    public static final int codePointAt(char[] text, int index) {
        int index2 = index + FOLD_CASE_EXCLUDE_SPECIAL_I;
        char c1 = text[index];
        if (isHighSurrogate(c1) && index2 < text.length) {
            char c2 = text[index2];
            if (isLowSurrogate(c2)) {
                return toCodePoint(c1, c2);
            }
        }
        return c1;
    }

    public static final int codePointAt(char[] text, int index, int limit) {
        if (index >= limit || limit > text.length) {
            throw new IndexOutOfBoundsException();
        }
        int index2 = index + FOLD_CASE_EXCLUDE_SPECIAL_I;
        char c1 = text[index];
        if (isHighSurrogate(c1) && index2 < limit) {
            char c2 = text[index2];
            if (isLowSurrogate(c2)) {
                return toCodePoint(c1, c2);
            }
        }
        return c1;
    }

    public static final int codePointBefore(CharSequence seq, int index) {
        index--;
        char c2 = seq.charAt(index);
        if (isLowSurrogate(c2) && index > 0) {
            char c1 = seq.charAt(index - 1);
            if (isHighSurrogate(c1)) {
                return toCodePoint(c1, c2);
            }
        }
        return c2;
    }

    public static final int codePointBefore(char[] text, int index) {
        index--;
        char c2 = text[index];
        if (isLowSurrogate(c2) && index > 0) {
            char c1 = text[index - 1];
            if (isHighSurrogate(c1)) {
                return toCodePoint(c1, c2);
            }
        }
        return c2;
    }

    public static final int codePointBefore(char[] text, int index, int limit) {
        if (index <= limit || limit < 0) {
            throw new IndexOutOfBoundsException();
        }
        index--;
        char c2 = text[index];
        if (isLowSurrogate(c2) && index > limit) {
            char c1 = text[index - 1];
            if (isHighSurrogate(c1)) {
                return toCodePoint(c1, c2);
            }
        }
        return c2;
    }

    public static final int toChars(int cp, char[] dst, int dstIndex) {
        return Character.toChars(cp, dst, dstIndex);
    }

    public static final char[] toChars(int cp) {
        return Character.toChars(cp);
    }

    public static byte getDirectionality(int cp) {
        return (byte) getDirection(cp);
    }

    public static int codePointCount(CharSequence text, int start, int limit) {
        if (start < 0 || limit < start || limit > text.length()) {
            throw new IndexOutOfBoundsException("start (" + start + ") or limit (" + limit + ") invalid or out of range 0, " + text.length());
        }
        int len = limit - start;
        while (limit > start) {
            limit--;
            char ch = text.charAt(limit);
            while (ch >= MIN_LOW_SURROGATE && ch <= MAX_SURROGATE && limit > start) {
                limit--;
                ch = text.charAt(limit);
                if (ch >= MIN_SURROGATE && ch <= MAX_HIGH_SURROGATE) {
                    len--;
                    break;
                }
            }
        }
        return len;
    }

    public static int codePointCount(char[] text, int start, int limit) {
        if (start < 0 || limit < start || limit > text.length) {
            throw new IndexOutOfBoundsException("start (" + start + ") or limit (" + limit + ") invalid or out of range 0, " + text.length);
        }
        int len = limit - start;
        while (limit > start) {
            limit--;
            char ch = text[limit];
            while (ch >= MIN_LOW_SURROGATE && ch <= MAX_SURROGATE && limit > start) {
                limit--;
                ch = text[limit];
                if (ch >= MIN_SURROGATE && ch <= MAX_HIGH_SURROGATE) {
                    len--;
                    break;
                }
            }
        }
        return len;
    }

    public static int offsetByCodePoints(CharSequence text, int index, int codePointOffset) {
        if (index < 0 || index > text.length()) {
            throw new IndexOutOfBoundsException("index ( " + index + ") out of range 0, " + text.length());
        }
        char ch;
        if (codePointOffset < 0) {
            while (true) {
                codePointOffset += FOLD_CASE_EXCLUDE_SPECIAL_I;
                if (codePointOffset > 0) {
                    break;
                }
                index--;
                ch = text.charAt(index);
                while (ch >= MIN_LOW_SURROGATE && ch <= MAX_SURROGATE && index > 0) {
                    index--;
                    ch = text.charAt(index);
                    if (ch < MIN_SURROGATE || ch > MAX_HIGH_SURROGATE) {
                        codePointOffset += FOLD_CASE_EXCLUDE_SPECIAL_I;
                        if (codePointOffset > 0) {
                            return index + FOLD_CASE_EXCLUDE_SPECIAL_I;
                        }
                    }
                }
            }
        } else {
            int limit = text.length();
            int index2 = index;
            while (true) {
                codePointOffset--;
                if (codePointOffset < 0) {
                    break;
                }
                index = index2 + FOLD_CASE_EXCLUDE_SPECIAL_I;
                ch = text.charAt(index2);
                index2 = index;
                while (ch >= MIN_SURROGATE && ch <= MAX_HIGH_SURROGATE && index2 < limit) {
                    index = index2 + FOLD_CASE_EXCLUDE_SPECIAL_I;
                    ch = text.charAt(index2);
                    if (ch < MIN_LOW_SURROGATE || ch > MAX_SURROGATE) {
                        codePointOffset--;
                        if (codePointOffset < 0) {
                            return index - 1;
                        }
                    }
                    index2 = index;
                }
            }
            index = index2;
        }
        return index;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int offsetByCodePoints(char[] text, int start, int count, int index, int codePointOffset) {
        int limit = start + count;
        if (start < 0 || limit < start || limit > text.length || index < start || index > limit) {
            throw new IndexOutOfBoundsException("index ( " + index + ") out of range " + start + ", " + limit + " in array 0, " + text.length);
        }
        char ch;
        if (codePointOffset < 0) {
            while (true) {
                codePointOffset += FOLD_CASE_EXCLUDE_SPECIAL_I;
                if (codePointOffset > 0) {
                    break;
                }
                index--;
                ch = text[index];
                if (index < start) {
                    break;
                }
                while (ch >= MIN_LOW_SURROGATE && ch <= MAX_SURROGATE && index > start) {
                    index--;
                    ch = text[index];
                    if (ch < MIN_SURROGATE || ch > MAX_HIGH_SURROGATE) {
                        codePointOffset += FOLD_CASE_EXCLUDE_SPECIAL_I;
                        if (codePointOffset > 0) {
                            return index + FOLD_CASE_EXCLUDE_SPECIAL_I;
                        }
                    }
                }
            }
            throw new IndexOutOfBoundsException("index ( " + index + ") < start (" + start + ")");
        }
        int index2 = index;
        while (true) {
            codePointOffset--;
            if (codePointOffset < 0) {
                break;
            }
            index = index2 + FOLD_CASE_EXCLUDE_SPECIAL_I;
            ch = text[index2];
            if (index > limit) {
                break;
            }
            while (true) {
                index2 = index;
                if (ch >= MIN_SURROGATE && ch <= MAX_HIGH_SURROGATE && index2 < limit) {
                    index = index2 + FOLD_CASE_EXCLUDE_SPECIAL_I;
                    ch = text[index2];
                    if (ch < MIN_LOW_SURROGATE || ch > MAX_SURROGATE) {
                        codePointOffset--;
                        if (codePointOffset < 0) {
                            return index - 1;
                        }
                    }
                }
            }
        }
        throw new IndexOutOfBoundsException("index ( " + index + ") > limit (" + limit + ")");
        return index;
    }

    private UCharacter() {
    }
}
