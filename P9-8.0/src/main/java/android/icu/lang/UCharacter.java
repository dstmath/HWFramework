package android.icu.lang;

import android.icu.impl.CaseMapImpl;
import android.icu.impl.IllegalIcuArgumentException;
import android.icu.impl.Trie2.Range;
import android.icu.impl.Trie2.ValueMapper;
import android.icu.impl.UBiDiProps;
import android.icu.impl.UCaseProps;
import android.icu.impl.UCharacterName;
import android.icu.impl.UCharacterProperty;
import android.icu.impl.UCharacterUtility;
import android.icu.impl.UPropertyAliases;
import android.icu.lang.UCharacterEnums.ECharacterCategory;
import android.icu.lang.UCharacterEnums.ECharacterDirection;
import android.icu.text.BreakIterator;
import android.icu.text.Edits;
import android.icu.text.Normalizer2;
import android.icu.util.RangeValueIterator;
import android.icu.util.ULocale;
import android.icu.util.ValueIterator;
import android.icu.util.ValueIterator.Element;
import android.icu.util.VersionInfo;
import dalvik.system.VMRuntime;
import java.lang.Character.Subset;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

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
    public static final char MAX_HIGH_SURROGATE = '?';
    public static final char MAX_LOW_SURROGATE = '?';
    public static final int MAX_RADIX = 36;
    public static final char MAX_SURROGATE = '?';
    public static final int MAX_VALUE = 1114111;
    public static final int MIN_CODE_POINT = 0;
    public static final char MIN_HIGH_SURROGATE = '?';
    public static final char MIN_LOW_SURROGATE = '?';
    public static final int MIN_RADIX = 2;
    public static final int MIN_SUPPLEMENTARY_CODE_POINT = 65536;
    public static final char MIN_SURROGATE = '?';
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
        @Deprecated
        public static final int COUNT = 3;
        public static final int NONE = 0;
        public static final int OPEN = 1;
    }

    public interface DecompositionType {
        public static final int CANONICAL = 1;
        public static final int CIRCLE = 3;
        public static final int COMPAT = 2;
        @Deprecated
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
        /* synthetic */ DummyValueIterator(DummyValueIterator -this0) {
            this();
        }

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
        @Deprecated
        public static final int COUNT = 6;
        public static final int FULLWIDTH = 3;
        public static final int HALFWIDTH = 2;
        public static final int NARROW = 4;
        public static final int NEUTRAL = 0;
        public static final int WIDE = 5;
    }

    public interface GraphemeClusterBreak {
        public static final int CONTROL = 1;
        @Deprecated
        public static final int COUNT = 18;
        public static final int CR = 2;
        public static final int EXTEND = 3;
        public static final int E_BASE = 13;
        public static final int E_BASE_GAZ = 14;
        public static final int E_MODIFIER = 15;
        public static final int GLUE_AFTER_ZWJ = 16;
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
        public static final int ZWJ = 17;
    }

    public interface HangulSyllableType {
        @Deprecated
        public static final int COUNT = 6;
        public static final int LEADING_JAMO = 1;
        public static final int LVT_SYLLABLE = 5;
        public static final int LV_SYLLABLE = 4;
        public static final int NOT_APPLICABLE = 0;
        public static final int TRAILING_JAMO = 3;
        public static final int VOWEL_JAMO = 2;
    }

    public interface JoiningGroup {
        public static final int AFRICAN_FEH = 86;
        public static final int AFRICAN_NOON = 87;
        public static final int AFRICAN_QAF = 88;
        public static final int AIN = 1;
        public static final int ALAPH = 2;
        public static final int ALEF = 3;
        public static final int BEH = 4;
        public static final int BETH = 5;
        public static final int BURUSHASKI_YEH_BARREE = 54;
        @Deprecated
        public static final int COUNT = 89;
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
        @Deprecated
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
        @Deprecated
        public static final int COUNT = 43;
        public static final int EXCLAMATION = 11;
        public static final int E_BASE = 40;
        public static final int E_MODIFIER = 41;
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
        public static final int ZWJ = 42;
        public static final int ZWSPACE = 28;
    }

    public interface NumericType {
        @Deprecated
        public static final int COUNT = 4;
        public static final int DECIMAL = 1;
        public static final int DIGIT = 2;
        public static final int NONE = 0;
        public static final int NUMERIC = 3;
    }

    public interface SentenceBreak {
        public static final int ATERM = 1;
        public static final int CLOSE = 2;
        @Deprecated
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

    private static final class UCharacterTypeIterator implements RangeValueIterator {
        private static final MaskType MASK_TYPE = new MaskType();
        private Range range;
        private Iterator<Range> trieIterator;

        private static final class MaskType implements ValueMapper {
            /* synthetic */ MaskType(MaskType -this0) {
                this();
            }

            private MaskType() {
            }

            public int map(int value) {
                return value & 31;
            }
        }

        UCharacterTypeIterator() {
            reset();
        }

        public boolean next(RangeValueIterator.Element element) {
            if (this.trieIterator.hasNext()) {
                Range range = (Range) this.trieIterator.next();
                this.range = range;
                if ((range.leadSurrogate ^ 1) != 0) {
                    element.start = this.range.startCodePoint;
                    element.limit = this.range.endCodePoint + 1;
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
        public static final UnicodeBlock ADLAM = new UnicodeBlock("ADLAM", ADLAM_ID);
        public static final int ADLAM_ID = 263;
        public static final UnicodeBlock AEGEAN_NUMBERS = new UnicodeBlock("AEGEAN_NUMBERS", 119);
        public static final int AEGEAN_NUMBERS_ID = 119;
        public static final UnicodeBlock AHOM = new UnicodeBlock("AHOM", 253);
        public static final int AHOM_ID = 253;
        public static final UnicodeBlock ALCHEMICAL_SYMBOLS = new UnicodeBlock("ALCHEMICAL_SYMBOLS", 208);
        public static final int ALCHEMICAL_SYMBOLS_ID = 208;
        public static final UnicodeBlock ALPHABETIC_PRESENTATION_FORMS = new UnicodeBlock("ALPHABETIC_PRESENTATION_FORMS", 80);
        public static final int ALPHABETIC_PRESENTATION_FORMS_ID = 80;
        public static final UnicodeBlock ANATOLIAN_HIEROGLYPHS = new UnicodeBlock("ANATOLIAN_HIEROGLYPHS", 254);
        public static final int ANATOLIAN_HIEROGLYPHS_ID = 254;
        public static final UnicodeBlock ANCIENT_GREEK_MUSICAL_NOTATION = new UnicodeBlock("ANCIENT_GREEK_MUSICAL_NOTATION", 126);
        public static final int ANCIENT_GREEK_MUSICAL_NOTATION_ID = 126;
        public static final UnicodeBlock ANCIENT_GREEK_NUMBERS = new UnicodeBlock("ANCIENT_GREEK_NUMBERS", 127);
        public static final int ANCIENT_GREEK_NUMBERS_ID = 127;
        public static final UnicodeBlock ANCIENT_SYMBOLS = new UnicodeBlock("ANCIENT_SYMBOLS", 165);
        public static final int ANCIENT_SYMBOLS_ID = 165;
        public static final UnicodeBlock ARABIC = new UnicodeBlock("ARABIC", 12);
        public static final UnicodeBlock ARABIC_EXTENDED_A = new UnicodeBlock("ARABIC_EXTENDED_A", 210);
        public static final int ARABIC_EXTENDED_A_ID = 210;
        public static final int ARABIC_ID = 12;
        public static final UnicodeBlock ARABIC_MATHEMATICAL_ALPHABETIC_SYMBOLS = new UnicodeBlock("ARABIC_MATHEMATICAL_ALPHABETIC_SYMBOLS", 211);
        public static final int ARABIC_MATHEMATICAL_ALPHABETIC_SYMBOLS_ID = 211;
        public static final UnicodeBlock ARABIC_PRESENTATION_FORMS_A = new UnicodeBlock("ARABIC_PRESENTATION_FORMS_A", 81);
        public static final int ARABIC_PRESENTATION_FORMS_A_ID = 81;
        public static final UnicodeBlock ARABIC_PRESENTATION_FORMS_B = new UnicodeBlock("ARABIC_PRESENTATION_FORMS_B", 85);
        public static final int ARABIC_PRESENTATION_FORMS_B_ID = 85;
        public static final UnicodeBlock ARABIC_SUPPLEMENT = new UnicodeBlock("ARABIC_SUPPLEMENT", 128);
        public static final int ARABIC_SUPPLEMENT_ID = 128;
        public static final UnicodeBlock ARMENIAN = new UnicodeBlock("ARMENIAN", 10);
        public static final int ARMENIAN_ID = 10;
        public static final UnicodeBlock ARROWS = new UnicodeBlock("ARROWS", 46);
        public static final int ARROWS_ID = 46;
        public static final UnicodeBlock AVESTAN = new UnicodeBlock("AVESTAN", 188);
        public static final int AVESTAN_ID = 188;
        public static final UnicodeBlock BALINESE = new UnicodeBlock("BALINESE", 147);
        public static final int BALINESE_ID = 147;
        public static final UnicodeBlock BAMUM = new UnicodeBlock("BAMUM", 177);
        public static final int BAMUM_ID = 177;
        public static final UnicodeBlock BAMUM_SUPPLEMENT = new UnicodeBlock("BAMUM_SUPPLEMENT", 202);
        public static final int BAMUM_SUPPLEMENT_ID = 202;
        public static final UnicodeBlock BASIC_LATIN = new UnicodeBlock("BASIC_LATIN", 1);
        public static final int BASIC_LATIN_ID = 1;
        public static final UnicodeBlock BASSA_VAH = new UnicodeBlock("BASSA_VAH", 221);
        public static final int BASSA_VAH_ID = 221;
        public static final UnicodeBlock BATAK = new UnicodeBlock("BATAK", 199);
        public static final int BATAK_ID = 199;
        public static final UnicodeBlock BENGALI = new UnicodeBlock("BENGALI", 16);
        public static final int BENGALI_ID = 16;
        public static final UnicodeBlock BHAIKSUKI = new UnicodeBlock("BHAIKSUKI", BHAIKSUKI_ID);
        public static final int BHAIKSUKI_ID = 264;
        private static final UnicodeBlock[] BLOCKS_ = new UnicodeBlock[COUNT];
        public static final UnicodeBlock BLOCK_ELEMENTS = new UnicodeBlock("BLOCK_ELEMENTS", 53);
        public static final int BLOCK_ELEMENTS_ID = 53;
        public static final UnicodeBlock BOPOMOFO = new UnicodeBlock("BOPOMOFO", 64);
        public static final UnicodeBlock BOPOMOFO_EXTENDED = new UnicodeBlock("BOPOMOFO_EXTENDED", 67);
        public static final int BOPOMOFO_EXTENDED_ID = 67;
        public static final int BOPOMOFO_ID = 64;
        public static final UnicodeBlock BOX_DRAWING = new UnicodeBlock("BOX_DRAWING", 52);
        public static final int BOX_DRAWING_ID = 52;
        public static final UnicodeBlock BRAHMI = new UnicodeBlock("BRAHMI", 201);
        public static final int BRAHMI_ID = 201;
        public static final UnicodeBlock BRAILLE_PATTERNS = new UnicodeBlock("BRAILLE_PATTERNS", 57);
        public static final int BRAILLE_PATTERNS_ID = 57;
        public static final UnicodeBlock BUGINESE = new UnicodeBlock("BUGINESE", 129);
        public static final int BUGINESE_ID = 129;
        public static final UnicodeBlock BUHID = new UnicodeBlock("BUHID", 100);
        public static final int BUHID_ID = 100;
        public static final UnicodeBlock BYZANTINE_MUSICAL_SYMBOLS = new UnicodeBlock("BYZANTINE_MUSICAL_SYMBOLS", 91);
        public static final int BYZANTINE_MUSICAL_SYMBOLS_ID = 91;
        public static final UnicodeBlock CARIAN = new UnicodeBlock("CARIAN", 168);
        public static final int CARIAN_ID = 168;
        public static final UnicodeBlock CAUCASIAN_ALBANIAN = new UnicodeBlock("CAUCASIAN_ALBANIAN", 222);
        public static final int CAUCASIAN_ALBANIAN_ID = 222;
        public static final UnicodeBlock CHAKMA = new UnicodeBlock("CHAKMA", 212);
        public static final int CHAKMA_ID = 212;
        public static final UnicodeBlock CHAM = new UnicodeBlock("CHAM", 164);
        public static final int CHAM_ID = 164;
        public static final UnicodeBlock CHEROKEE = new UnicodeBlock("CHEROKEE", 32);
        public static final int CHEROKEE_ID = 32;
        public static final UnicodeBlock CHEROKEE_SUPPLEMENT = new UnicodeBlock("CHEROKEE_SUPPLEMENT", 255);
        public static final int CHEROKEE_SUPPLEMENT_ID = 255;
        public static final UnicodeBlock CJK_COMPATIBILITY = new UnicodeBlock("CJK_COMPATIBILITY", 69);
        public static final UnicodeBlock CJK_COMPATIBILITY_FORMS = new UnicodeBlock("CJK_COMPATIBILITY_FORMS", 83);
        public static final int CJK_COMPATIBILITY_FORMS_ID = 83;
        public static final int CJK_COMPATIBILITY_ID = 69;
        public static final UnicodeBlock CJK_COMPATIBILITY_IDEOGRAPHS = new UnicodeBlock("CJK_COMPATIBILITY_IDEOGRAPHS", 79);
        public static final int CJK_COMPATIBILITY_IDEOGRAPHS_ID = 79;
        public static final UnicodeBlock CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT = new UnicodeBlock("CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT", 95);
        public static final int CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT_ID = 95;
        public static final UnicodeBlock CJK_RADICALS_SUPPLEMENT = new UnicodeBlock("CJK_RADICALS_SUPPLEMENT", 58);
        public static final int CJK_RADICALS_SUPPLEMENT_ID = 58;
        public static final UnicodeBlock CJK_STROKES = new UnicodeBlock("CJK_STROKES", 130);
        public static final int CJK_STROKES_ID = 130;
        public static final UnicodeBlock CJK_SYMBOLS_AND_PUNCTUATION = new UnicodeBlock("CJK_SYMBOLS_AND_PUNCTUATION", 61);
        public static final int CJK_SYMBOLS_AND_PUNCTUATION_ID = 61;
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS = new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS", 71);
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A = new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A", 70);
        public static final int CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A_ID = 70;
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B = new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B", 94);
        public static final int CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B_ID = 94;
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C = new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C", 197);
        public static final int CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C_ID = 197;
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D = new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D", 209);
        public static final int CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D_ID = 209;
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_E = new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_E", 256);
        public static final int CJK_UNIFIED_IDEOGRAPHS_EXTENSION_E_ID = 256;
        public static final int CJK_UNIFIED_IDEOGRAPHS_ID = 71;
        public static final UnicodeBlock COMBINING_DIACRITICAL_MARKS = new UnicodeBlock("COMBINING_DIACRITICAL_MARKS", 7);
        public static final UnicodeBlock COMBINING_DIACRITICAL_MARKS_EXTENDED = new UnicodeBlock("COMBINING_DIACRITICAL_MARKS_EXTENDED", 224);
        public static final int COMBINING_DIACRITICAL_MARKS_EXTENDED_ID = 224;
        public static final int COMBINING_DIACRITICAL_MARKS_ID = 7;
        public static final UnicodeBlock COMBINING_DIACRITICAL_MARKS_SUPPLEMENT = new UnicodeBlock("COMBINING_DIACRITICAL_MARKS_SUPPLEMENT", 131);
        public static final int COMBINING_DIACRITICAL_MARKS_SUPPLEMENT_ID = 131;
        public static final UnicodeBlock COMBINING_HALF_MARKS = new UnicodeBlock("COMBINING_HALF_MARKS", 82);
        public static final int COMBINING_HALF_MARKS_ID = 82;
        public static final UnicodeBlock COMBINING_MARKS_FOR_SYMBOLS = new UnicodeBlock("COMBINING_MARKS_FOR_SYMBOLS", 43);
        public static final int COMBINING_MARKS_FOR_SYMBOLS_ID = 43;
        public static final UnicodeBlock COMMON_INDIC_NUMBER_FORMS = new UnicodeBlock("COMMON_INDIC_NUMBER_FORMS", 178);
        public static final int COMMON_INDIC_NUMBER_FORMS_ID = 178;
        public static final UnicodeBlock CONTROL_PICTURES = new UnicodeBlock("CONTROL_PICTURES", 49);
        public static final int CONTROL_PICTURES_ID = 49;
        public static final UnicodeBlock COPTIC = new UnicodeBlock("COPTIC", 132);
        public static final UnicodeBlock COPTIC_EPACT_NUMBERS = new UnicodeBlock("COPTIC_EPACT_NUMBERS", 223);
        public static final int COPTIC_EPACT_NUMBERS_ID = 223;
        public static final int COPTIC_ID = 132;
        @Deprecated
        public static final int COUNT = 274;
        public static final UnicodeBlock COUNTING_ROD_NUMERALS = new UnicodeBlock("COUNTING_ROD_NUMERALS", 154);
        public static final int COUNTING_ROD_NUMERALS_ID = 154;
        public static final UnicodeBlock CUNEIFORM = new UnicodeBlock("CUNEIFORM", 152);
        public static final int CUNEIFORM_ID = 152;
        public static final UnicodeBlock CUNEIFORM_NUMBERS_AND_PUNCTUATION = new UnicodeBlock("CUNEIFORM_NUMBERS_AND_PUNCTUATION", 153);
        public static final int CUNEIFORM_NUMBERS_AND_PUNCTUATION_ID = 153;
        public static final UnicodeBlock CURRENCY_SYMBOLS = new UnicodeBlock("CURRENCY_SYMBOLS", 42);
        public static final int CURRENCY_SYMBOLS_ID = 42;
        public static final UnicodeBlock CYPRIOT_SYLLABARY = new UnicodeBlock("CYPRIOT_SYLLABARY", 123);
        public static final int CYPRIOT_SYLLABARY_ID = 123;
        public static final UnicodeBlock CYRILLIC = new UnicodeBlock("CYRILLIC", 9);
        public static final UnicodeBlock CYRILLIC_EXTENDED_A = new UnicodeBlock("CYRILLIC_EXTENDED_A", 158);
        public static final int CYRILLIC_EXTENDED_A_ID = 158;
        public static final UnicodeBlock CYRILLIC_EXTENDED_B = new UnicodeBlock("CYRILLIC_EXTENDED_B", 160);
        public static final int CYRILLIC_EXTENDED_B_ID = 160;
        public static final UnicodeBlock CYRILLIC_EXTENDED_C = new UnicodeBlock("CYRILLIC_EXTENDED_C", CYRILLIC_EXTENDED_C_ID);
        public static final int CYRILLIC_EXTENDED_C_ID = 265;
        public static final int CYRILLIC_ID = 9;
        public static final UnicodeBlock CYRILLIC_SUPPLEMENT = new UnicodeBlock("CYRILLIC_SUPPLEMENT", 97);
        public static final UnicodeBlock CYRILLIC_SUPPLEMENTARY = new UnicodeBlock("CYRILLIC_SUPPLEMENTARY", 97);
        public static final int CYRILLIC_SUPPLEMENTARY_ID = 97;
        public static final int CYRILLIC_SUPPLEMENT_ID = 97;
        public static final UnicodeBlock DESERET = new UnicodeBlock("DESERET", 90);
        public static final int DESERET_ID = 90;
        public static final UnicodeBlock DEVANAGARI = new UnicodeBlock("DEVANAGARI", 15);
        public static final UnicodeBlock DEVANAGARI_EXTENDED = new UnicodeBlock("DEVANAGARI_EXTENDED", 179);
        public static final int DEVANAGARI_EXTENDED_ID = 179;
        public static final int DEVANAGARI_ID = 15;
        public static final UnicodeBlock DINGBATS = new UnicodeBlock("DINGBATS", 56);
        public static final int DINGBATS_ID = 56;
        public static final UnicodeBlock DOMINO_TILES = new UnicodeBlock("DOMINO_TILES", 171);
        public static final int DOMINO_TILES_ID = 171;
        public static final UnicodeBlock DUPLOYAN = new UnicodeBlock("DUPLOYAN", 225);
        public static final int DUPLOYAN_ID = 225;
        public static final UnicodeBlock EARLY_DYNASTIC_CUNEIFORM = new UnicodeBlock("EARLY_DYNASTIC_CUNEIFORM", EARLY_DYNASTIC_CUNEIFORM_ID);
        public static final int EARLY_DYNASTIC_CUNEIFORM_ID = 257;
        public static final UnicodeBlock EGYPTIAN_HIEROGLYPHS = new UnicodeBlock("EGYPTIAN_HIEROGLYPHS", 194);
        public static final int EGYPTIAN_HIEROGLYPHS_ID = 194;
        public static final UnicodeBlock ELBASAN = new UnicodeBlock("ELBASAN", 226);
        public static final int ELBASAN_ID = 226;
        public static final UnicodeBlock EMOTICONS = new UnicodeBlock("EMOTICONS", 206);
        public static final int EMOTICONS_ID = 206;
        public static final UnicodeBlock ENCLOSED_ALPHANUMERICS = new UnicodeBlock("ENCLOSED_ALPHANUMERICS", 51);
        public static final int ENCLOSED_ALPHANUMERICS_ID = 51;
        public static final UnicodeBlock ENCLOSED_ALPHANUMERIC_SUPPLEMENT = new UnicodeBlock("ENCLOSED_ALPHANUMERIC_SUPPLEMENT", 195);
        public static final int ENCLOSED_ALPHANUMERIC_SUPPLEMENT_ID = 195;
        public static final UnicodeBlock ENCLOSED_CJK_LETTERS_AND_MONTHS = new UnicodeBlock("ENCLOSED_CJK_LETTERS_AND_MONTHS", 68);
        public static final int ENCLOSED_CJK_LETTERS_AND_MONTHS_ID = 68;
        public static final UnicodeBlock ENCLOSED_IDEOGRAPHIC_SUPPLEMENT = new UnicodeBlock("ENCLOSED_IDEOGRAPHIC_SUPPLEMENT", 196);
        public static final int ENCLOSED_IDEOGRAPHIC_SUPPLEMENT_ID = 196;
        public static final UnicodeBlock ETHIOPIC = new UnicodeBlock("ETHIOPIC", 31);
        public static final UnicodeBlock ETHIOPIC_EXTENDED = new UnicodeBlock("ETHIOPIC_EXTENDED", 133);
        public static final UnicodeBlock ETHIOPIC_EXTENDED_A = new UnicodeBlock("ETHIOPIC_EXTENDED_A", 200);
        public static final int ETHIOPIC_EXTENDED_A_ID = 200;
        public static final int ETHIOPIC_EXTENDED_ID = 133;
        public static final int ETHIOPIC_ID = 31;
        public static final UnicodeBlock ETHIOPIC_SUPPLEMENT = new UnicodeBlock("ETHIOPIC_SUPPLEMENT", 134);
        public static final int ETHIOPIC_SUPPLEMENT_ID = 134;
        public static final UnicodeBlock GENERAL_PUNCTUATION = new UnicodeBlock("GENERAL_PUNCTUATION", 40);
        public static final int GENERAL_PUNCTUATION_ID = 40;
        public static final UnicodeBlock GEOMETRIC_SHAPES = new UnicodeBlock("GEOMETRIC_SHAPES", 54);
        public static final UnicodeBlock GEOMETRIC_SHAPES_EXTENDED = new UnicodeBlock("GEOMETRIC_SHAPES_EXTENDED", 227);
        public static final int GEOMETRIC_SHAPES_EXTENDED_ID = 227;
        public static final int GEOMETRIC_SHAPES_ID = 54;
        public static final UnicodeBlock GEORGIAN = new UnicodeBlock("GEORGIAN", 29);
        public static final int GEORGIAN_ID = 29;
        public static final UnicodeBlock GEORGIAN_SUPPLEMENT = new UnicodeBlock("GEORGIAN_SUPPLEMENT", 135);
        public static final int GEORGIAN_SUPPLEMENT_ID = 135;
        public static final UnicodeBlock GLAGOLITIC = new UnicodeBlock("GLAGOLITIC", 136);
        public static final int GLAGOLITIC_ID = 136;
        public static final UnicodeBlock GLAGOLITIC_SUPPLEMENT = new UnicodeBlock("GLAGOLITIC_SUPPLEMENT", GLAGOLITIC_SUPPLEMENT_ID);
        public static final int GLAGOLITIC_SUPPLEMENT_ID = 266;
        public static final UnicodeBlock GOTHIC = new UnicodeBlock("GOTHIC", 89);
        public static final int GOTHIC_ID = 89;
        public static final UnicodeBlock GRANTHA = new UnicodeBlock("GRANTHA", 228);
        public static final int GRANTHA_ID = 228;
        public static final UnicodeBlock GREEK = new UnicodeBlock("GREEK", 8);
        public static final UnicodeBlock GREEK_EXTENDED = new UnicodeBlock("GREEK_EXTENDED", 39);
        public static final int GREEK_EXTENDED_ID = 39;
        public static final int GREEK_ID = 8;
        public static final UnicodeBlock GUJARATI = new UnicodeBlock("GUJARATI", 18);
        public static final int GUJARATI_ID = 18;
        public static final UnicodeBlock GURMUKHI = new UnicodeBlock("GURMUKHI", 17);
        public static final int GURMUKHI_ID = 17;
        public static final UnicodeBlock HALFWIDTH_AND_FULLWIDTH_FORMS = new UnicodeBlock("HALFWIDTH_AND_FULLWIDTH_FORMS", 87);
        public static final int HALFWIDTH_AND_FULLWIDTH_FORMS_ID = 87;
        public static final UnicodeBlock HANGUL_COMPATIBILITY_JAMO = new UnicodeBlock("HANGUL_COMPATIBILITY_JAMO", 65);
        public static final int HANGUL_COMPATIBILITY_JAMO_ID = 65;
        public static final UnicodeBlock HANGUL_JAMO = new UnicodeBlock("HANGUL_JAMO", 30);
        public static final UnicodeBlock HANGUL_JAMO_EXTENDED_A = new UnicodeBlock("HANGUL_JAMO_EXTENDED_A", 180);
        public static final int HANGUL_JAMO_EXTENDED_A_ID = 180;
        public static final UnicodeBlock HANGUL_JAMO_EXTENDED_B = new UnicodeBlock("HANGUL_JAMO_EXTENDED_B", 185);
        public static final int HANGUL_JAMO_EXTENDED_B_ID = 185;
        public static final int HANGUL_JAMO_ID = 30;
        public static final UnicodeBlock HANGUL_SYLLABLES = new UnicodeBlock("HANGUL_SYLLABLES", 74);
        public static final int HANGUL_SYLLABLES_ID = 74;
        public static final UnicodeBlock HANUNOO = new UnicodeBlock("HANUNOO", 99);
        public static final int HANUNOO_ID = 99;
        public static final UnicodeBlock HATRAN = new UnicodeBlock("HATRAN", HATRAN_ID);
        public static final int HATRAN_ID = 258;
        public static final UnicodeBlock HEBREW = new UnicodeBlock("HEBREW", 11);
        public static final int HEBREW_ID = 11;
        public static final UnicodeBlock HIGH_PRIVATE_USE_SURROGATES = new UnicodeBlock("HIGH_PRIVATE_USE_SURROGATES", 76);
        public static final int HIGH_PRIVATE_USE_SURROGATES_ID = 76;
        public static final UnicodeBlock HIGH_SURROGATES = new UnicodeBlock("HIGH_SURROGATES", 75);
        public static final int HIGH_SURROGATES_ID = 75;
        public static final UnicodeBlock HIRAGANA = new UnicodeBlock("HIRAGANA", 62);
        public static final int HIRAGANA_ID = 62;
        public static final UnicodeBlock IDEOGRAPHIC_DESCRIPTION_CHARACTERS = new UnicodeBlock("IDEOGRAPHIC_DESCRIPTION_CHARACTERS", 60);
        public static final int IDEOGRAPHIC_DESCRIPTION_CHARACTERS_ID = 60;
        public static final UnicodeBlock IDEOGRAPHIC_SYMBOLS_AND_PUNCTUATION = new UnicodeBlock("IDEOGRAPHIC_SYMBOLS_AND_PUNCTUATION", IDEOGRAPHIC_SYMBOLS_AND_PUNCTUATION_ID);
        public static final int IDEOGRAPHIC_SYMBOLS_AND_PUNCTUATION_ID = 267;
        public static final UnicodeBlock IMPERIAL_ARAMAIC = new UnicodeBlock("IMPERIAL_ARAMAIC", 186);
        public static final int IMPERIAL_ARAMAIC_ID = 186;
        public static final UnicodeBlock INSCRIPTIONAL_PAHLAVI = new UnicodeBlock("INSCRIPTIONAL_PAHLAVI", 190);
        public static final int INSCRIPTIONAL_PAHLAVI_ID = 190;
        public static final UnicodeBlock INSCRIPTIONAL_PARTHIAN = new UnicodeBlock("INSCRIPTIONAL_PARTHIAN", 189);
        public static final int INSCRIPTIONAL_PARTHIAN_ID = 189;
        public static final UnicodeBlock INVALID_CODE = new UnicodeBlock("INVALID_CODE", -1);
        public static final int INVALID_CODE_ID = -1;
        public static final UnicodeBlock IPA_EXTENSIONS = new UnicodeBlock("IPA_EXTENSIONS", 5);
        public static final int IPA_EXTENSIONS_ID = 5;
        public static final UnicodeBlock JAVANESE = new UnicodeBlock("JAVANESE", 181);
        public static final int JAVANESE_ID = 181;
        public static final UnicodeBlock KAITHI = new UnicodeBlock("KAITHI", 193);
        public static final int KAITHI_ID = 193;
        public static final UnicodeBlock KANA_SUPPLEMENT = new UnicodeBlock("KANA_SUPPLEMENT", 203);
        public static final int KANA_SUPPLEMENT_ID = 203;
        public static final UnicodeBlock KANBUN = new UnicodeBlock("KANBUN", 66);
        public static final int KANBUN_ID = 66;
        public static final UnicodeBlock KANGXI_RADICALS = new UnicodeBlock("KANGXI_RADICALS", 59);
        public static final int KANGXI_RADICALS_ID = 59;
        public static final UnicodeBlock KANNADA = new UnicodeBlock("KANNADA", 22);
        public static final int KANNADA_ID = 22;
        public static final UnicodeBlock KATAKANA = new UnicodeBlock("KATAKANA", 63);
        public static final int KATAKANA_ID = 63;
        public static final UnicodeBlock KATAKANA_PHONETIC_EXTENSIONS = new UnicodeBlock("KATAKANA_PHONETIC_EXTENSIONS", 107);
        public static final int KATAKANA_PHONETIC_EXTENSIONS_ID = 107;
        public static final UnicodeBlock KAYAH_LI = new UnicodeBlock("KAYAH_LI", 162);
        public static final int KAYAH_LI_ID = 162;
        public static final UnicodeBlock KHAROSHTHI = new UnicodeBlock("KHAROSHTHI", 137);
        public static final int KHAROSHTHI_ID = 137;
        public static final UnicodeBlock KHMER = new UnicodeBlock("KHMER", 36);
        public static final int KHMER_ID = 36;
        public static final UnicodeBlock KHMER_SYMBOLS = new UnicodeBlock("KHMER_SYMBOLS", 113);
        public static final int KHMER_SYMBOLS_ID = 113;
        public static final UnicodeBlock KHOJKI = new UnicodeBlock("KHOJKI", 229);
        public static final int KHOJKI_ID = 229;
        public static final UnicodeBlock KHUDAWADI = new UnicodeBlock("KHUDAWADI", 230);
        public static final int KHUDAWADI_ID = 230;
        public static final UnicodeBlock LAO = new UnicodeBlock("LAO", 26);
        public static final int LAO_ID = 26;
        public static final UnicodeBlock LATIN_1_SUPPLEMENT = new UnicodeBlock("LATIN_1_SUPPLEMENT", 2);
        public static final int LATIN_1_SUPPLEMENT_ID = 2;
        public static final UnicodeBlock LATIN_EXTENDED_A = new UnicodeBlock("LATIN_EXTENDED_A", 3);
        public static final UnicodeBlock LATIN_EXTENDED_ADDITIONAL = new UnicodeBlock("LATIN_EXTENDED_ADDITIONAL", 38);
        public static final int LATIN_EXTENDED_ADDITIONAL_ID = 38;
        public static final int LATIN_EXTENDED_A_ID = 3;
        public static final UnicodeBlock LATIN_EXTENDED_B = new UnicodeBlock("LATIN_EXTENDED_B", 4);
        public static final int LATIN_EXTENDED_B_ID = 4;
        public static final UnicodeBlock LATIN_EXTENDED_C = new UnicodeBlock("LATIN_EXTENDED_C", 148);
        public static final int LATIN_EXTENDED_C_ID = 148;
        public static final UnicodeBlock LATIN_EXTENDED_D = new UnicodeBlock("LATIN_EXTENDED_D", 149);
        public static final int LATIN_EXTENDED_D_ID = 149;
        public static final UnicodeBlock LATIN_EXTENDED_E = new UnicodeBlock("LATIN_EXTENDED_E", 231);
        public static final int LATIN_EXTENDED_E_ID = 231;
        public static final UnicodeBlock LEPCHA = new UnicodeBlock("LEPCHA", 156);
        public static final int LEPCHA_ID = 156;
        public static final UnicodeBlock LETTERLIKE_SYMBOLS = new UnicodeBlock("LETTERLIKE_SYMBOLS", 44);
        public static final int LETTERLIKE_SYMBOLS_ID = 44;
        public static final UnicodeBlock LIMBU = new UnicodeBlock("LIMBU", 111);
        public static final int LIMBU_ID = 111;
        public static final UnicodeBlock LINEAR_A = new UnicodeBlock("LINEAR_A", 232);
        public static final int LINEAR_A_ID = 232;
        public static final UnicodeBlock LINEAR_B_IDEOGRAMS = new UnicodeBlock("LINEAR_B_IDEOGRAMS", 118);
        public static final int LINEAR_B_IDEOGRAMS_ID = 118;
        public static final UnicodeBlock LINEAR_B_SYLLABARY = new UnicodeBlock("LINEAR_B_SYLLABARY", 117);
        public static final int LINEAR_B_SYLLABARY_ID = 117;
        public static final UnicodeBlock LISU = new UnicodeBlock("LISU", 176);
        public static final int LISU_ID = 176;
        public static final UnicodeBlock LOW_SURROGATES = new UnicodeBlock("LOW_SURROGATES", 77);
        public static final int LOW_SURROGATES_ID = 77;
        public static final UnicodeBlock LYCIAN = new UnicodeBlock("LYCIAN", 167);
        public static final int LYCIAN_ID = 167;
        public static final UnicodeBlock LYDIAN = new UnicodeBlock("LYDIAN", 169);
        public static final int LYDIAN_ID = 169;
        public static final UnicodeBlock MAHAJANI = new UnicodeBlock("MAHAJANI", 233);
        public static final int MAHAJANI_ID = 233;
        public static final UnicodeBlock MAHJONG_TILES = new UnicodeBlock("MAHJONG_TILES", 170);
        public static final int MAHJONG_TILES_ID = 170;
        public static final UnicodeBlock MALAYALAM = new UnicodeBlock("MALAYALAM", 23);
        public static final int MALAYALAM_ID = 23;
        public static final UnicodeBlock MANDAIC = new UnicodeBlock("MANDAIC", 198);
        public static final int MANDAIC_ID = 198;
        public static final UnicodeBlock MANICHAEAN = new UnicodeBlock("MANICHAEAN", 234);
        public static final int MANICHAEAN_ID = 234;
        public static final UnicodeBlock MARCHEN = new UnicodeBlock("MARCHEN", MARCHEN_ID);
        public static final int MARCHEN_ID = 268;
        public static final UnicodeBlock MATHEMATICAL_ALPHANUMERIC_SYMBOLS = new UnicodeBlock("MATHEMATICAL_ALPHANUMERIC_SYMBOLS", 93);
        public static final int MATHEMATICAL_ALPHANUMERIC_SYMBOLS_ID = 93;
        public static final UnicodeBlock MATHEMATICAL_OPERATORS = new UnicodeBlock("MATHEMATICAL_OPERATORS", 47);
        public static final int MATHEMATICAL_OPERATORS_ID = 47;
        public static final UnicodeBlock MEETEI_MAYEK = new UnicodeBlock("MEETEI_MAYEK", 184);
        public static final UnicodeBlock MEETEI_MAYEK_EXTENSIONS = new UnicodeBlock("MEETEI_MAYEK_EXTENSIONS", 213);
        public static final int MEETEI_MAYEK_EXTENSIONS_ID = 213;
        public static final int MEETEI_MAYEK_ID = 184;
        public static final UnicodeBlock MENDE_KIKAKUI = new UnicodeBlock("MENDE_KIKAKUI", 235);
        public static final int MENDE_KIKAKUI_ID = 235;
        public static final UnicodeBlock MEROITIC_CURSIVE = new UnicodeBlock("MEROITIC_CURSIVE", 214);
        public static final int MEROITIC_CURSIVE_ID = 214;
        public static final UnicodeBlock MEROITIC_HIEROGLYPHS = new UnicodeBlock("MEROITIC_HIEROGLYPHS", 215);
        public static final int MEROITIC_HIEROGLYPHS_ID = 215;
        public static final UnicodeBlock MIAO = new UnicodeBlock("MIAO", 216);
        public static final int MIAO_ID = 216;
        public static final UnicodeBlock MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A = new UnicodeBlock("MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A", 102);
        public static final int MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A_ID = 102;
        public static final UnicodeBlock MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B = new UnicodeBlock("MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B", 105);
        public static final int MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B_ID = 105;
        public static final UnicodeBlock MISCELLANEOUS_SYMBOLS = new UnicodeBlock("MISCELLANEOUS_SYMBOLS", 55);
        public static final UnicodeBlock MISCELLANEOUS_SYMBOLS_AND_ARROWS = new UnicodeBlock("MISCELLANEOUS_SYMBOLS_AND_ARROWS", 115);
        public static final int MISCELLANEOUS_SYMBOLS_AND_ARROWS_ID = 115;
        public static final UnicodeBlock MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS = new UnicodeBlock("MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS", 205);
        public static final int MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS_ID = 205;
        public static final int MISCELLANEOUS_SYMBOLS_ID = 55;
        public static final UnicodeBlock MISCELLANEOUS_TECHNICAL = new UnicodeBlock("MISCELLANEOUS_TECHNICAL", 48);
        public static final int MISCELLANEOUS_TECHNICAL_ID = 48;
        public static final UnicodeBlock MODI = new UnicodeBlock("MODI", 236);
        public static final UnicodeBlock MODIFIER_TONE_LETTERS = new UnicodeBlock("MODIFIER_TONE_LETTERS", 138);
        public static final int MODIFIER_TONE_LETTERS_ID = 138;
        public static final int MODI_ID = 236;
        public static final UnicodeBlock MONGOLIAN = new UnicodeBlock("MONGOLIAN", 37);
        public static final int MONGOLIAN_ID = 37;
        public static final UnicodeBlock MONGOLIAN_SUPPLEMENT = new UnicodeBlock("MONGOLIAN_SUPPLEMENT", MONGOLIAN_SUPPLEMENT_ID);
        public static final int MONGOLIAN_SUPPLEMENT_ID = 269;
        public static final UnicodeBlock MRO = new UnicodeBlock("MRO", 237);
        public static final int MRO_ID = 237;
        public static final UnicodeBlock MULTANI = new UnicodeBlock("MULTANI", MULTANI_ID);
        public static final int MULTANI_ID = 259;
        public static final UnicodeBlock MUSICAL_SYMBOLS = new UnicodeBlock("MUSICAL_SYMBOLS", 92);
        public static final int MUSICAL_SYMBOLS_ID = 92;
        public static final UnicodeBlock MYANMAR = new UnicodeBlock("MYANMAR", 28);
        public static final UnicodeBlock MYANMAR_EXTENDED_A = new UnicodeBlock("MYANMAR_EXTENDED_A", 182);
        public static final int MYANMAR_EXTENDED_A_ID = 182;
        public static final UnicodeBlock MYANMAR_EXTENDED_B = new UnicodeBlock("MYANMAR_EXTENDED_B", 238);
        public static final int MYANMAR_EXTENDED_B_ID = 238;
        public static final int MYANMAR_ID = 28;
        public static final UnicodeBlock NABATAEAN = new UnicodeBlock("NABATAEAN", 239);
        public static final int NABATAEAN_ID = 239;
        public static final UnicodeBlock NEWA = new UnicodeBlock("NEWA", NEWA_ID);
        public static final int NEWA_ID = 270;
        public static final UnicodeBlock NEW_TAI_LUE = new UnicodeBlock("NEW_TAI_LUE", 139);
        public static final int NEW_TAI_LUE_ID = 139;
        public static final UnicodeBlock NKO = new UnicodeBlock("NKO", 146);
        public static final int NKO_ID = 146;
        public static final UnicodeBlock NO_BLOCK = new UnicodeBlock("NO_BLOCK", 0);
        public static final UnicodeBlock NUMBER_FORMS = new UnicodeBlock("NUMBER_FORMS", 45);
        public static final int NUMBER_FORMS_ID = 45;
        public static final UnicodeBlock OGHAM = new UnicodeBlock("OGHAM", 34);
        public static final int OGHAM_ID = 34;
        public static final UnicodeBlock OLD_HUNGARIAN = new UnicodeBlock("OLD_HUNGARIAN", OLD_HUNGARIAN_ID);
        public static final int OLD_HUNGARIAN_ID = 260;
        public static final UnicodeBlock OLD_ITALIC = new UnicodeBlock("OLD_ITALIC", 88);
        public static final int OLD_ITALIC_ID = 88;
        public static final UnicodeBlock OLD_NORTH_ARABIAN = new UnicodeBlock("OLD_NORTH_ARABIAN", 240);
        public static final int OLD_NORTH_ARABIAN_ID = 240;
        public static final UnicodeBlock OLD_PERMIC = new UnicodeBlock("OLD_PERMIC", 241);
        public static final int OLD_PERMIC_ID = 241;
        public static final UnicodeBlock OLD_PERSIAN = new UnicodeBlock("OLD_PERSIAN", 140);
        public static final int OLD_PERSIAN_ID = 140;
        public static final UnicodeBlock OLD_SOUTH_ARABIAN = new UnicodeBlock("OLD_SOUTH_ARABIAN", 187);
        public static final int OLD_SOUTH_ARABIAN_ID = 187;
        public static final UnicodeBlock OLD_TURKIC = new UnicodeBlock("OLD_TURKIC", 191);
        public static final int OLD_TURKIC_ID = 191;
        public static final UnicodeBlock OL_CHIKI = new UnicodeBlock("OL_CHIKI", 157);
        public static final int OL_CHIKI_ID = 157;
        public static final UnicodeBlock OPTICAL_CHARACTER_RECOGNITION = new UnicodeBlock("OPTICAL_CHARACTER_RECOGNITION", 50);
        public static final int OPTICAL_CHARACTER_RECOGNITION_ID = 50;
        public static final UnicodeBlock ORIYA = new UnicodeBlock("ORIYA", 19);
        public static final int ORIYA_ID = 19;
        public static final UnicodeBlock ORNAMENTAL_DINGBATS = new UnicodeBlock("ORNAMENTAL_DINGBATS", 242);
        public static final int ORNAMENTAL_DINGBATS_ID = 242;
        public static final UnicodeBlock OSAGE = new UnicodeBlock("OSAGE", OSAGE_ID);
        public static final int OSAGE_ID = 271;
        public static final UnicodeBlock OSMANYA = new UnicodeBlock("OSMANYA", 122);
        public static final int OSMANYA_ID = 122;
        public static final UnicodeBlock PAHAWH_HMONG = new UnicodeBlock("PAHAWH_HMONG", 243);
        public static final int PAHAWH_HMONG_ID = 243;
        public static final UnicodeBlock PALMYRENE = new UnicodeBlock("PALMYRENE", 244);
        public static final int PALMYRENE_ID = 244;
        public static final UnicodeBlock PAU_CIN_HAU = new UnicodeBlock("PAU_CIN_HAU", 245);
        public static final int PAU_CIN_HAU_ID = 245;
        public static final UnicodeBlock PHAGS_PA = new UnicodeBlock("PHAGS_PA", 150);
        public static final int PHAGS_PA_ID = 150;
        public static final UnicodeBlock PHAISTOS_DISC = new UnicodeBlock("PHAISTOS_DISC", 166);
        public static final int PHAISTOS_DISC_ID = 166;
        public static final UnicodeBlock PHOENICIAN = new UnicodeBlock("PHOENICIAN", 151);
        public static final int PHOENICIAN_ID = 151;
        public static final UnicodeBlock PHONETIC_EXTENSIONS = new UnicodeBlock("PHONETIC_EXTENSIONS", 114);
        public static final int PHONETIC_EXTENSIONS_ID = 114;
        public static final UnicodeBlock PHONETIC_EXTENSIONS_SUPPLEMENT = new UnicodeBlock("PHONETIC_EXTENSIONS_SUPPLEMENT", 141);
        public static final int PHONETIC_EXTENSIONS_SUPPLEMENT_ID = 141;
        public static final UnicodeBlock PLAYING_CARDS = new UnicodeBlock("PLAYING_CARDS", 204);
        public static final int PLAYING_CARDS_ID = 204;
        public static final UnicodeBlock PRIVATE_USE = PRIVATE_USE_AREA;
        public static final UnicodeBlock PRIVATE_USE_AREA = new UnicodeBlock("PRIVATE_USE_AREA", 78);
        public static final int PRIVATE_USE_AREA_ID = 78;
        public static final int PRIVATE_USE_ID = 78;
        public static final UnicodeBlock PSALTER_PAHLAVI = new UnicodeBlock("PSALTER_PAHLAVI", 246);
        public static final int PSALTER_PAHLAVI_ID = 246;
        public static final UnicodeBlock REJANG = new UnicodeBlock("REJANG", 163);
        public static final int REJANG_ID = 163;
        public static final UnicodeBlock RUMI_NUMERAL_SYMBOLS = new UnicodeBlock("RUMI_NUMERAL_SYMBOLS", 192);
        public static final int RUMI_NUMERAL_SYMBOLS_ID = 192;
        public static final UnicodeBlock RUNIC = new UnicodeBlock("RUNIC", 35);
        public static final int RUNIC_ID = 35;
        public static final UnicodeBlock SAMARITAN = new UnicodeBlock("SAMARITAN", 172);
        public static final int SAMARITAN_ID = 172;
        public static final UnicodeBlock SAURASHTRA = new UnicodeBlock("SAURASHTRA", 161);
        public static final int SAURASHTRA_ID = 161;
        public static final UnicodeBlock SHARADA = new UnicodeBlock("SHARADA", 217);
        public static final int SHARADA_ID = 217;
        public static final UnicodeBlock SHAVIAN = new UnicodeBlock("SHAVIAN", 121);
        public static final int SHAVIAN_ID = 121;
        public static final UnicodeBlock SHORTHAND_FORMAT_CONTROLS = new UnicodeBlock("SHORTHAND_FORMAT_CONTROLS", 247);
        public static final int SHORTHAND_FORMAT_CONTROLS_ID = 247;
        public static final UnicodeBlock SIDDHAM = new UnicodeBlock("SIDDHAM", 248);
        public static final int SIDDHAM_ID = 248;
        public static final UnicodeBlock SINHALA = new UnicodeBlock("SINHALA", 24);
        public static final UnicodeBlock SINHALA_ARCHAIC_NUMBERS = new UnicodeBlock("SINHALA_ARCHAIC_NUMBERS", 249);
        public static final int SINHALA_ARCHAIC_NUMBERS_ID = 249;
        public static final int SINHALA_ID = 24;
        public static final UnicodeBlock SMALL_FORM_VARIANTS = new UnicodeBlock("SMALL_FORM_VARIANTS", 84);
        public static final int SMALL_FORM_VARIANTS_ID = 84;
        public static final UnicodeBlock SORA_SOMPENG = new UnicodeBlock("SORA_SOMPENG", 218);
        public static final int SORA_SOMPENG_ID = 218;
        public static final UnicodeBlock SPACING_MODIFIER_LETTERS = new UnicodeBlock("SPACING_MODIFIER_LETTERS", 6);
        public static final int SPACING_MODIFIER_LETTERS_ID = 6;
        public static final UnicodeBlock SPECIALS = new UnicodeBlock("SPECIALS", 86);
        public static final int SPECIALS_ID = 86;
        public static final UnicodeBlock SUNDANESE = new UnicodeBlock("SUNDANESE", 155);
        public static final int SUNDANESE_ID = 155;
        public static final UnicodeBlock SUNDANESE_SUPPLEMENT = new UnicodeBlock("SUNDANESE_SUPPLEMENT", 219);
        public static final int SUNDANESE_SUPPLEMENT_ID = 219;
        public static final UnicodeBlock SUPERSCRIPTS_AND_SUBSCRIPTS = new UnicodeBlock("SUPERSCRIPTS_AND_SUBSCRIPTS", 41);
        public static final int SUPERSCRIPTS_AND_SUBSCRIPTS_ID = 41;
        public static final UnicodeBlock SUPPLEMENTAL_ARROWS_A = new UnicodeBlock("SUPPLEMENTAL_ARROWS_A", 103);
        public static final int SUPPLEMENTAL_ARROWS_A_ID = 103;
        public static final UnicodeBlock SUPPLEMENTAL_ARROWS_B = new UnicodeBlock("SUPPLEMENTAL_ARROWS_B", 104);
        public static final int SUPPLEMENTAL_ARROWS_B_ID = 104;
        public static final UnicodeBlock SUPPLEMENTAL_ARROWS_C = new UnicodeBlock("SUPPLEMENTAL_ARROWS_C", 250);
        public static final int SUPPLEMENTAL_ARROWS_C_ID = 250;
        public static final UnicodeBlock SUPPLEMENTAL_MATHEMATICAL_OPERATORS = new UnicodeBlock("SUPPLEMENTAL_MATHEMATICAL_OPERATORS", 106);
        public static final int SUPPLEMENTAL_MATHEMATICAL_OPERATORS_ID = 106;
        public static final UnicodeBlock SUPPLEMENTAL_PUNCTUATION = new UnicodeBlock("SUPPLEMENTAL_PUNCTUATION", 142);
        public static final int SUPPLEMENTAL_PUNCTUATION_ID = 142;
        public static final UnicodeBlock SUPPLEMENTAL_SYMBOLS_AND_PICTOGRAPHS = new UnicodeBlock("SUPPLEMENTAL_SYMBOLS_AND_PICTOGRAPHS", SUPPLEMENTAL_SYMBOLS_AND_PICTOGRAPHS_ID);
        public static final int SUPPLEMENTAL_SYMBOLS_AND_PICTOGRAPHS_ID = 261;
        public static final UnicodeBlock SUPPLEMENTARY_PRIVATE_USE_AREA_A = new UnicodeBlock("SUPPLEMENTARY_PRIVATE_USE_AREA_A", 109);
        public static final int SUPPLEMENTARY_PRIVATE_USE_AREA_A_ID = 109;
        public static final UnicodeBlock SUPPLEMENTARY_PRIVATE_USE_AREA_B = new UnicodeBlock("SUPPLEMENTARY_PRIVATE_USE_AREA_B", 110);
        public static final int SUPPLEMENTARY_PRIVATE_USE_AREA_B_ID = 110;
        public static final UnicodeBlock SUTTON_SIGNWRITING = new UnicodeBlock("SUTTON_SIGNWRITING", SUTTON_SIGNWRITING_ID);
        public static final int SUTTON_SIGNWRITING_ID = 262;
        public static final UnicodeBlock SYLOTI_NAGRI = new UnicodeBlock("SYLOTI_NAGRI", 143);
        public static final int SYLOTI_NAGRI_ID = 143;
        public static final UnicodeBlock SYRIAC = new UnicodeBlock("SYRIAC", 13);
        public static final int SYRIAC_ID = 13;
        public static final UnicodeBlock TAGALOG = new UnicodeBlock("TAGALOG", 98);
        public static final int TAGALOG_ID = 98;
        public static final UnicodeBlock TAGBANWA = new UnicodeBlock("TAGBANWA", 101);
        public static final int TAGBANWA_ID = 101;
        public static final UnicodeBlock TAGS = new UnicodeBlock("TAGS", 96);
        public static final int TAGS_ID = 96;
        public static final UnicodeBlock TAI_LE = new UnicodeBlock("TAI_LE", 112);
        public static final int TAI_LE_ID = 112;
        public static final UnicodeBlock TAI_THAM = new UnicodeBlock("TAI_THAM", 174);
        public static final int TAI_THAM_ID = 174;
        public static final UnicodeBlock TAI_VIET = new UnicodeBlock("TAI_VIET", 183);
        public static final int TAI_VIET_ID = 183;
        public static final UnicodeBlock TAI_XUAN_JING_SYMBOLS = new UnicodeBlock("TAI_XUAN_JING_SYMBOLS", 124);
        public static final int TAI_XUAN_JING_SYMBOLS_ID = 124;
        public static final UnicodeBlock TAKRI = new UnicodeBlock("TAKRI", 220);
        public static final int TAKRI_ID = 220;
        public static final UnicodeBlock TAMIL = new UnicodeBlock("TAMIL", 20);
        public static final int TAMIL_ID = 20;
        public static final UnicodeBlock TANGUT = new UnicodeBlock("TANGUT", TANGUT_ID);
        public static final UnicodeBlock TANGUT_COMPONENTS = new UnicodeBlock("TANGUT_COMPONENTS", TANGUT_COMPONENTS_ID);
        public static final int TANGUT_COMPONENTS_ID = 273;
        public static final int TANGUT_ID = 272;
        public static final UnicodeBlock TELUGU = new UnicodeBlock("TELUGU", 21);
        public static final int TELUGU_ID = 21;
        public static final UnicodeBlock THAANA = new UnicodeBlock("THAANA", 14);
        public static final int THAANA_ID = 14;
        public static final UnicodeBlock THAI = new UnicodeBlock("THAI", 25);
        public static final int THAI_ID = 25;
        public static final UnicodeBlock TIBETAN = new UnicodeBlock("TIBETAN", 27);
        public static final int TIBETAN_ID = 27;
        public static final UnicodeBlock TIFINAGH = new UnicodeBlock("TIFINAGH", 144);
        public static final int TIFINAGH_ID = 144;
        public static final UnicodeBlock TIRHUTA = new UnicodeBlock("TIRHUTA", 251);
        public static final int TIRHUTA_ID = 251;
        public static final UnicodeBlock TRANSPORT_AND_MAP_SYMBOLS = new UnicodeBlock("TRANSPORT_AND_MAP_SYMBOLS", 207);
        public static final int TRANSPORT_AND_MAP_SYMBOLS_ID = 207;
        public static final UnicodeBlock UGARITIC = new UnicodeBlock("UGARITIC", 120);
        public static final int UGARITIC_ID = 120;
        public static final UnicodeBlock UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS = new UnicodeBlock("UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS", 33);
        public static final UnicodeBlock UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_EXTENDED = new UnicodeBlock("UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_EXTENDED", 173);
        public static final int UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_EXTENDED_ID = 173;
        public static final int UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_ID = 33;
        public static final UnicodeBlock VAI = new UnicodeBlock("VAI", 159);
        public static final int VAI_ID = 159;
        public static final UnicodeBlock VARIATION_SELECTORS = new UnicodeBlock("VARIATION_SELECTORS", 108);
        public static final int VARIATION_SELECTORS_ID = 108;
        public static final UnicodeBlock VARIATION_SELECTORS_SUPPLEMENT = new UnicodeBlock("VARIATION_SELECTORS_SUPPLEMENT", 125);
        public static final int VARIATION_SELECTORS_SUPPLEMENT_ID = 125;
        public static final UnicodeBlock VEDIC_EXTENSIONS = new UnicodeBlock("VEDIC_EXTENSIONS", 175);
        public static final int VEDIC_EXTENSIONS_ID = 175;
        public static final UnicodeBlock VERTICAL_FORMS = new UnicodeBlock("VERTICAL_FORMS", 145);
        public static final int VERTICAL_FORMS_ID = 145;
        public static final UnicodeBlock WARANG_CITI = new UnicodeBlock("WARANG_CITI", 252);
        public static final int WARANG_CITI_ID = 252;
        public static final UnicodeBlock YIJING_HEXAGRAM_SYMBOLS = new UnicodeBlock("YIJING_HEXAGRAM_SYMBOLS", 116);
        public static final int YIJING_HEXAGRAM_SYMBOLS_ID = 116;
        public static final UnicodeBlock YI_RADICALS = new UnicodeBlock("YI_RADICALS", 73);
        public static final int YI_RADICALS_ID = 73;
        public static final UnicodeBlock YI_SYLLABLES = new UnicodeBlock("YI_SYLLABLES", 72);
        public static final int YI_SYLLABLES_ID = 72;
        private static SoftReference<Map<String, UnicodeBlock>> mref;
        private int m_id_;

        static {
            for (int blockId = 0; blockId < COUNT; blockId++) {
                if (BLOCKS_[blockId] == null) {
                    throw new IllegalStateException("UnicodeBlock.BLOCKS_[" + blockId + "] not initialized");
                }
            }
        }

        public static UnicodeBlock getInstance(int id) {
            if (id < 0 || id >= BLOCKS_.length) {
                return INVALID_CODE;
            }
            return BLOCKS_[id];
        }

        public static UnicodeBlock of(int ch) {
            if (ch > 1114111) {
                return INVALID_CODE;
            }
            return getInstance(UCharacterProperty.INSTANCE.getIntPropertyValue(ch, 4097));
        }

        public static final UnicodeBlock forName(String blockName) {
            Map m = null;
            if (mref != null) {
                m = (Map) mref.get();
            }
            if (m == null) {
                m = new HashMap(BLOCKS_.length);
                for (UnicodeBlock b : BLOCKS_) {
                    m.put(trimBlockName(UCharacter.getPropertyValueName(4097, b.getID(), 1)), b);
                }
                mref = new SoftReference(m);
            }
            UnicodeBlock b2 = (UnicodeBlock) m.get(trimBlockName(blockName));
            if (b2 != null) {
                return b2;
            }
            throw new IllegalArgumentException();
        }

        private static String trimBlockName(String name) {
            String upper = name.toUpperCase(Locale.ENGLISH);
            StringBuilder result = new StringBuilder(upper.length());
            for (int i = 0; i < upper.length(); i++) {
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
        @Deprecated
        public static final int COUNT = 22;
        public static final int CR = 8;
        public static final int DOUBLE_QUOTE = 16;
        public static final int EXTEND = 9;
        public static final int EXTENDNUMLET = 7;
        public static final int E_BASE = 17;
        public static final int E_BASE_GAZ = 18;
        public static final int E_MODIFIER = 19;
        public static final int FORMAT = 2;
        public static final int GLUE_AFTER_ZWJ = 20;
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
        public static final int ZWJ = 21;
    }

    public static int digit(int ch, int radix) {
        if (2 > radix || radix > 36) {
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
        if (ch < 0 || ch > 159) {
            return false;
        }
        return ch <= 31 || ch >= 127;
    }

    public static boolean isLetter(int ch) {
        return ((1 << getType(ch)) & 62) != 0;
    }

    public static boolean isLetterOrDigit(int ch) {
        return ((1 << getType(ch)) & 574) != 0;
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
        return getType(ch) == 2;
    }

    public static boolean isWhitespace(int ch) {
        if (((1 << getType(ch)) & 28672) != 0 && ch != 160 && ch != FIGURE_SPACE_ && ch != NARROW_NO_BREAK_SPACE_) {
            return true;
        }
        if (ch >= 9 && ch <= 13) {
            return true;
        }
        if (ch < 28 || ch > 31) {
            return false;
        }
        return true;
    }

    public static boolean isSpaceChar(int ch) {
        return ((1 << getType(ch)) & 28672) != 0;
    }

    public static boolean isTitleCase(int ch) {
        return getType(ch) == 3;
    }

    public static boolean isUnicodeIdentifierPart(int ch) {
        if (((1 << getType(ch)) & 4196222) == 0) {
            return isIdentifierIgnorable(ch);
        }
        return true;
    }

    public static boolean isUnicodeIdentifierStart(int ch) {
        return ((1 << getType(ch)) & 1086) != 0;
    }

    public static boolean isIdentifierIgnorable(int ch) {
        boolean z = true;
        boolean z2 = false;
        if (ch <= 159) {
            if (isISOControl(ch) && (ch < 9 || ch > 13)) {
                if (ch >= 28 && ch <= 31) {
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
        return getType(ch) == 1;
    }

    public static int toLowerCase(int ch) {
        return UCaseProps.INSTANCE.tolower(ch);
    }

    public static String toString(int ch) {
        if (ch < 0 || ch > 1114111) {
            return null;
        }
        if (ch < 65536) {
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
        if (ch < 65536 || ch > 1114111) {
            return false;
        }
        return true;
    }

    public static boolean isBMP(int ch) {
        return ch >= 0 && ch <= 65535;
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
        if (cat == 9 || cat == 11 || cat == 10 || cat == 1 || cat == 2 || cat == 3 || cat == 4 || cat == 5 || cat == 6 || cat == 7 || cat == 8) {
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
        if (ch < 55296) {
            return true;
        }
        if (ch <= 57343 || UCharacterUtility.isNonCharacter(ch)) {
            return false;
        }
        if (ch > 1114111) {
            z = false;
        }
        return z;
    }

    public static boolean isLegal(String str) {
        int size = str.length();
        int i = 0;
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
        return UCharacterName.INSTANCE.getName(ch, 0);
    }

    public static String getName(String s, String separator) {
        if (s.length() == 1) {
            return getName(s.charAt(0));
        }
        StringBuilder sb = new StringBuilder();
        int i = 0;
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
        return UCharacterName.INSTANCE.getName(ch, 2);
    }

    public static String getNameAlias(int ch) {
        return UCharacterName.INSTANCE.getName(ch, 3);
    }

    @Deprecated
    public static String getISOComment(int ch) {
        return null;
    }

    public static int getCharFromName(String name) {
        return UCharacterName.INSTANCE.getCharFromName(0, name);
    }

    @Deprecated
    public static int getCharFromName1_0(String name) {
        return -1;
    }

    public static int getCharFromExtendedName(String name) {
        return UCharacterName.INSTANCE.getCharFromName(2, name);
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
        if ((property != 4098 && property != UProperty.LEAD_CANONICAL_COMBINING_CLASS && property != UProperty.TRAIL_CANONICAL_COMBINING_CLASS) || value < getIntPropertyMinValue(4098) || value > getIntPropertyMaxValue(4098) || nameChoice < 0 || nameChoice >= 2) {
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
        return toUpperCase(getDefaultCaseLocale(), str);
    }

    public static String toLowerCase(String str) {
        return toLowerCase(getDefaultCaseLocale(), str);
    }

    public static String toTitleCase(String str, BreakIterator breakiter) {
        return toTitleCase(Locale.getDefault(), str, breakiter, 0);
    }

    private static int getDefaultCaseLocale() {
        return UCaseProps.getCaseLocale(Locale.getDefault());
    }

    private static int getCaseLocale(Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        return UCaseProps.getCaseLocale(locale);
    }

    private static int getCaseLocale(ULocale locale) {
        if (locale == null) {
            locale = ULocale.getDefault();
        }
        return UCaseProps.getCaseLocale(locale);
    }

    private static String toLowerCase(int caseLocale, String str) {
        if (str.length() > 100) {
            return ((StringBuilder) CaseMapImpl.toLower(caseLocale, 0, str, new StringBuilder(str.length()), null)).toString();
        }
        if (str.isEmpty()) {
            return str;
        }
        Edits edits = new Edits();
        return applyEdits(str, (StringBuilder) CaseMapImpl.toLower(caseLocale, 16384, str, new StringBuilder(), edits), edits);
    }

    private static String toUpperCase(int caseLocale, String str) {
        if (str.length() > 100) {
            return ((StringBuilder) CaseMapImpl.toUpper(caseLocale, 0, str, new StringBuilder(str.length()), null)).toString();
        }
        if (str.isEmpty()) {
            return str;
        }
        Edits edits = new Edits();
        return applyEdits(str, (StringBuilder) CaseMapImpl.toUpper(caseLocale, 16384, str, new StringBuilder(), edits), edits);
    }

    private static String toTitleCase(int caseLocale, int options, BreakIterator titleIter, String str) {
        if (str.length() > 100) {
            return ((StringBuilder) CaseMapImpl.toTitle(caseLocale, options, titleIter, str, new StringBuilder(str.length()), null)).toString();
        } else if (str.isEmpty()) {
            return str;
        } else {
            Edits edits = new Edits();
            return applyEdits(str, (StringBuilder) CaseMapImpl.toTitle(caseLocale, options | 16384, titleIter, str, new StringBuilder(), edits), edits);
        }
    }

    private static String applyEdits(String str, StringBuilder replacementChars, Edits edits) {
        if (!edits.hasChanges()) {
            return str;
        }
        StringBuilder result = new StringBuilder(str.length() + edits.lengthDelta());
        Edits.Iterator ei = edits.getCoarseIterator();
        while (ei.next()) {
            int i;
            if (ei.hasChange()) {
                i = ei.replacementIndex();
                result.append(replacementChars, i, ei.newLength() + i);
            } else {
                i = ei.sourceIndex();
                result.append(str, i, ei.oldLength() + i);
            }
        }
        return result.toString();
    }

    public static String toUpperCase(Locale locale, String str) {
        return toUpperCase(getCaseLocale(locale), str);
    }

    public static String toUpperCase(ULocale locale, String str) {
        return toUpperCase(getCaseLocale(locale), str);
    }

    public static String toLowerCase(Locale locale, String str) {
        return toLowerCase(getCaseLocale(locale), str);
    }

    public static String toLowerCase(ULocale locale, String str) {
        return toLowerCase(getCaseLocale(locale), str);
    }

    public static String toTitleCase(Locale locale, String str, BreakIterator breakiter) {
        return toTitleCase(locale, str, breakiter, 0);
    }

    public static String toTitleCase(ULocale locale, String str, BreakIterator titleIter) {
        return toTitleCase(locale, str, titleIter, 0);
    }

    public static String toTitleCase(ULocale locale, String str, BreakIterator titleIter, int options) {
        if (titleIter == null) {
            if (locale == null) {
                locale = ULocale.getDefault();
            }
            titleIter = BreakIterator.getWordInstance(locale);
        }
        titleIter.setText(str);
        return toTitleCase(getCaseLocale(locale), options, titleIter, str);
    }

    @Deprecated
    public static String toTitleFirst(ULocale locale, String str) {
        int i = 0;
        while (i < str.length()) {
            int c = codePointAt((CharSequence) str, i);
            if ((getIntPropertyValue(c, 8192) & BREAK_MASK) != 0) {
                break;
            } else if (UCaseProps.INSTANCE.getType(c) == 0) {
                i += charCount(c);
            } else {
                String titled = toTitleCase(locale, str.substring(i, charCount(c) + i), BreakIterator.getSentenceInstance(locale), 0);
                if (titled.codePointAt(0) != c) {
                    int startOfSuffix;
                    StringBuilder result = new StringBuilder(str.length()).append(str, 0, i);
                    if (c == 105 && locale.getLanguage().equals("nl") && i < str.length() && str.charAt(i + 1) == 'j') {
                        result.append("IJ");
                        startOfSuffix = 2;
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
        if (titleIter == null) {
            titleIter = BreakIterator.getWordInstance(locale);
        }
        titleIter.setText(str);
        return toTitleCase(getCaseLocale(locale), options, titleIter, str);
    }

    public static int foldCase(int ch, boolean defaultmapping) {
        return foldCase(ch, defaultmapping ? 0 : 1);
    }

    public static String foldCase(String str, boolean defaultmapping) {
        return foldCase(str, defaultmapping ? 0 : 1);
    }

    public static int foldCase(int ch, int options) {
        return UCaseProps.INSTANCE.fold(ch, options);
    }

    public static final String foldCase(String str, int options) {
        if (str.length() > 100) {
            return ((StringBuilder) CaseMapImpl.fold(options, str, new StringBuilder(str.length()), null)).toString();
        }
        if (str.isEmpty()) {
            return str;
        }
        Edits edits = new Edits();
        return applyEdits(str, (StringBuilder) CaseMapImpl.fold(options | 16384, str, new StringBuilder(), edits), edits);
    }

    public static int getHanNumericValue(int ch) {
        switch (ch) {
            case IDEOGRAPHIC_NUMBER_ZERO_ /*12295*/:
            case CJK_IDEOGRAPH_COMPLEX_ZERO_ /*38646*/:
                return 0;
            case CJK_IDEOGRAPH_FIRST_ /*19968*/:
            case CJK_IDEOGRAPH_COMPLEX_ONE_ /*22777*/:
                return 1;
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
                return 2;
            case CJK_IDEOGRAPH_FIFTH_ /*20116*/:
            case CJK_IDEOGRAPH_COMPLEX_FIVE_ /*20237*/:
                return 5;
            case CJK_IDEOGRAPH_COMPLEX_THOUSAND_ /*20191*/:
            case CJK_IDEOGRAPH_THOUSAND_ /*21315*/:
                return 1000;
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
                return VMRuntime.SDK_VERSION_CUR_DEVELOPMENT;
            default:
                return -1;
        }
    }

    public static RangeValueIterator getTypeIterator() {
        return new UCharacterTypeIterator();
    }

    public static ValueIterator getNameIterator() {
        return new UCharacterNameIterator(UCharacterName.INSTANCE, 0);
    }

    @Deprecated
    public static ValueIterator getName1_0Iterator() {
        return new DummyValueIterator();
    }

    public static ValueIterator getExtendedNameIterator() {
        return new UCharacterNameIterator(UCharacterName.INSTANCE, 2);
    }

    public static VersionInfo getAge(int ch) {
        if (ch >= 0 && ch <= 1114111) {
            return UCharacterProperty.INSTANCE.getAge(ch);
        }
        throw new IllegalArgumentException("Codepoint out of bounds");
    }

    public static boolean hasBinaryProperty(int ch, int property) {
        return UCharacterProperty.INSTANCE.hasBinaryProperty(ch, property);
    }

    public static boolean isUAlphabetic(int ch) {
        return hasBinaryProperty(ch, 0);
    }

    public static boolean isULowercase(int ch) {
        return hasBinaryProperty(ch, 22);
    }

    public static boolean isUUppercase(int ch) {
        return hasBinaryProperty(ch, 30);
    }

    public static boolean isUWhiteSpace(int ch) {
        return hasBinaryProperty(ch, 31);
    }

    public static int getIntPropertyValue(int ch, int type) {
        return UCharacterProperty.INSTANCE.getIntPropertyValue(ch, type);
    }

    @Deprecated
    public static String getStringPropertyValue(int propertyEnum, int codepoint, int nameChoice) {
        if ((propertyEnum >= 0 && propertyEnum < 61) || (propertyEnum >= 4096 && propertyEnum < UProperty.INT_LIMIT)) {
            return getPropertyValueName(propertyEnum, getIntPropertyValue(codepoint, propertyEnum), nameChoice);
        }
        if (propertyEnum == 12288) {
            return String.valueOf(getUnicodeNumericValue(codepoint));
        }
        switch (propertyEnum) {
            case 16384:
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
        return 0;
    }

    public static int getIntPropertyMaxValue(int type) {
        return UCharacterProperty.INSTANCE.getIntPropertyMaxValue(type);
    }

    public static char forDigit(int digit, int radix) {
        return Character.forDigit(digit, radix);
    }

    public static final boolean isValidCodePoint(int cp) {
        return cp >= 0 && cp <= 1114111;
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
        int index2 = index + 1;
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
        int index2 = index + 1;
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
        int index2 = index + 1;
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
            while (ch >= MIN_LOW_SURROGATE && ch <= 57343 && limit > start) {
                limit--;
                ch = text.charAt(limit);
                if (ch >= 55296 && ch <= MAX_HIGH_SURROGATE) {
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
            while (ch >= MIN_LOW_SURROGATE && ch <= 57343 && limit > start) {
                limit--;
                ch = text[limit];
                if (ch >= 55296 && ch <= MAX_HIGH_SURROGATE) {
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
        if (codePointOffset >= 0) {
            int limit = text.length();
            int index2 = index;
            while (true) {
                codePointOffset--;
                if (codePointOffset < 0) {
                    index = index2;
                    break;
                }
                index = index2 + 1;
                ch = text.charAt(index2);
                index2 = index;
                while (ch >= 55296 && ch <= MAX_HIGH_SURROGATE && index2 < limit) {
                    index = index2 + 1;
                    ch = text.charAt(index2);
                    if (ch < MIN_LOW_SURROGATE || ch > 57343) {
                        codePointOffset--;
                        if (codePointOffset < 0) {
                            return index - 1;
                        }
                    }
                    index2 = index;
                }
            }
        } else {
            while (true) {
                codePointOffset++;
                if (codePointOffset > 0) {
                    break;
                }
                index--;
                ch = text.charAt(index);
                while (ch >= MIN_LOW_SURROGATE && ch <= 57343 && index > 0) {
                    index--;
                    ch = text.charAt(index);
                    if (ch < 55296 || ch > MAX_HIGH_SURROGATE) {
                        codePointOffset++;
                        if (codePointOffset > 0) {
                            return index + 1;
                        }
                    }
                }
            }
        }
        return index;
    }

    public static int offsetByCodePoints(char[] text, int start, int count, int index, int codePointOffset) {
        int limit = start + count;
        if (start < 0 || limit < start || limit > text.length || index < start || index > limit) {
            throw new IndexOutOfBoundsException("index ( " + index + ") out of range " + start + ", " + limit + " in array 0, " + text.length);
        }
        char ch;
        if (codePointOffset >= 0) {
            int index2 = index;
            while (true) {
                codePointOffset--;
                if (codePointOffset < 0) {
                    index = index2;
                    break;
                }
                index = index2 + 1;
                ch = text[index2];
                if (index > limit) {
                    throw new IndexOutOfBoundsException("index ( " + index + ") > limit (" + limit + ")");
                }
                while (true) {
                    index2 = index;
                    if (ch >= 55296 && ch <= MAX_HIGH_SURROGATE && index2 < limit) {
                        index = index2 + 1;
                        ch = text[index2];
                        if (ch < MIN_LOW_SURROGATE || ch > 57343) {
                            codePointOffset--;
                            if (codePointOffset < 0) {
                                return index - 1;
                            }
                        }
                    }
                }
            }
        } else {
            while (true) {
                codePointOffset++;
                if (codePointOffset > 0) {
                    break;
                }
                index--;
                ch = text[index];
                if (index < start) {
                    throw new IndexOutOfBoundsException("index ( " + index + ") < start (" + start + ")");
                }
                while (ch >= MIN_LOW_SURROGATE && ch <= 57343 && index > start) {
                    index--;
                    ch = text[index];
                    if (ch < 55296 || ch > MAX_HIGH_SURROGATE) {
                        codePointOffset++;
                        if (codePointOffset > 0) {
                            return index + 1;
                        }
                    }
                }
            }
        }
        return index;
    }

    private UCharacter() {
    }
}
