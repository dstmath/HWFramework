package ohos.global.icu.lang;

import java.lang.Character;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import ohos.global.icu.impl.CaseMapImpl;
import ohos.global.icu.impl.IllegalIcuArgumentException;
import ohos.global.icu.impl.Trie2;
import ohos.global.icu.impl.UBiDiProps;
import ohos.global.icu.impl.UCaseProps;
import ohos.global.icu.impl.UCharacterName;
import ohos.global.icu.impl.UCharacterProperty;
import ohos.global.icu.impl.UCharacterUtility;
import ohos.global.icu.impl.UPropertyAliases;
import ohos.global.icu.lang.UCharacterEnums;
import ohos.global.icu.text.BreakIterator;
import ohos.global.icu.text.Normalizer2;
import ohos.global.icu.util.RangeValueIterator;
import ohos.global.icu.util.ULocale;
import ohos.global.icu.util.ValueIterator;
import ohos.global.icu.util.VersionInfo;

public final class UCharacter implements UCharacterEnums.ECharacterCategory, UCharacterEnums.ECharacterDirection {
    private static final int APPLICATION_PROGRAM_COMMAND_ = 159;
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
    public static final char MAX_HIGH_SURROGATE = 56319;
    public static final char MAX_LOW_SURROGATE = 57343;
    public static final int MAX_RADIX = 36;
    public static final char MAX_SURROGATE = 57343;
    public static final int MAX_VALUE = 1114111;
    public static final int MIN_CODE_POINT = 0;
    public static final char MIN_HIGH_SURROGATE = 55296;
    public static final char MIN_LOW_SURROGATE = 56320;
    public static final int MIN_RADIX = 2;
    public static final int MIN_SUPPLEMENTARY_CODE_POINT = 65536;
    public static final char MIN_SURROGATE = 55296;
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

    public interface IndicPositionalCategory {
        public static final int BOTTOM = 1;
        public static final int BOTTOM_AND_LEFT = 2;
        public static final int BOTTOM_AND_RIGHT = 3;
        public static final int LEFT = 4;
        public static final int LEFT_AND_RIGHT = 5;
        public static final int NA = 0;
        public static final int OVERSTRUCK = 6;
        public static final int RIGHT = 7;
        public static final int TOP = 8;
        public static final int TOP_AND_BOTTOM = 9;
        public static final int TOP_AND_BOTTOM_AND_LEFT = 15;
        public static final int TOP_AND_BOTTOM_AND_RIGHT = 10;
        public static final int TOP_AND_LEFT = 11;
        public static final int TOP_AND_LEFT_AND_RIGHT = 12;
        public static final int TOP_AND_RIGHT = 13;
        public static final int VISUAL_ORDER_LEFT = 14;
    }

    public interface IndicSyllabicCategory {
        public static final int AVAGRAHA = 1;
        public static final int BINDU = 2;
        public static final int BRAHMI_JOINING_NUMBER = 3;
        public static final int CANTILLATION_MARK = 4;
        public static final int CONSONANT = 5;
        public static final int CONSONANT_DEAD = 6;
        public static final int CONSONANT_FINAL = 7;
        public static final int CONSONANT_HEAD_LETTER = 8;
        public static final int CONSONANT_INITIAL_POSTFIXED = 9;
        public static final int CONSONANT_KILLER = 10;
        public static final int CONSONANT_MEDIAL = 11;
        public static final int CONSONANT_PLACEHOLDER = 12;
        public static final int CONSONANT_PRECEDING_REPHA = 13;
        public static final int CONSONANT_PREFIXED = 14;
        public static final int CONSONANT_SUBJOINED = 15;
        public static final int CONSONANT_SUCCEEDING_REPHA = 16;
        public static final int CONSONANT_WITH_STACKER = 17;
        public static final int GEMINATION_MARK = 18;
        public static final int INVISIBLE_STACKER = 19;
        public static final int JOINER = 20;
        public static final int MODIFYING_LETTER = 21;
        public static final int NON_JOINER = 22;
        public static final int NUKTA = 23;
        public static final int NUMBER = 24;
        public static final int NUMBER_JOINER = 25;
        public static final int OTHER = 0;
        public static final int PURE_KILLER = 26;
        public static final int REGISTER_SHIFTER = 27;
        public static final int SYLLABLE_MODIFIER = 28;
        public static final int TONE_LETTER = 29;
        public static final int TONE_MARK = 30;
        public static final int VIRAMA = 31;
        public static final int VISARGA = 32;
        public static final int VOWEL = 33;
        public static final int VOWEL_DEPENDENT = 34;
        public static final int VOWEL_INDEPENDENT = 35;
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
        public static final int COUNT = 102;
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
        public static final int HANIFI_ROHINGYA_KINNA_YA = 100;
        public static final int HANIFI_ROHINGYA_PA = 101;
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
        public static final int MALAYALAM_BHA = 89;
        public static final int MALAYALAM_JA = 90;
        public static final int MALAYALAM_LLA = 91;
        public static final int MALAYALAM_LLLA = 92;
        public static final int MALAYALAM_NGA = 93;
        public static final int MALAYALAM_NNA = 94;
        public static final int MALAYALAM_NNNA = 95;
        public static final int MALAYALAM_NYA = 96;
        public static final int MALAYALAM_RA = 97;
        public static final int MALAYALAM_SSA = 98;
        public static final int MALAYALAM_TTA = 99;
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

    public interface VerticalOrientation {
        public static final int ROTATED = 0;
        public static final int TRANSFORMED_ROTATED = 1;
        public static final int TRANSFORMED_UPRIGHT = 2;
        public static final int UPRIGHT = 3;
    }

    public interface WordBreak {
        public static final int ALETTER = 1;
        @Deprecated
        public static final int COUNT = 23;
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
        public static final int WSEGSPACE = 22;
        public static final int ZWJ = 21;
    }

    @Deprecated
    public static int getCharFromName1_0(String str) {
        return -1;
    }

    public static int getHanNumericValue(int i) {
        switch (i) {
            case IDEOGRAPHIC_NUMBER_ZERO_ /* 12295 */:
            case CJK_IDEOGRAPH_COMPLEX_ZERO_ /* 38646 */:
                return 0;
            case CJK_IDEOGRAPH_FIRST_ /* 19968 */:
            case CJK_IDEOGRAPH_COMPLEX_ONE_ /* 22777 */:
                return 1;
            case CJK_IDEOGRAPH_SEVENTH_ /* 19971 */:
            case CJK_IDEOGRAPH_COMPLEX_SEVEN_ /* 26578 */:
                return 7;
            case CJK_IDEOGRAPH_THIRD_ /* 19977 */:
            case CJK_IDEOGRAPH_COMPLEX_THREE_ /* 21443 */:
                return 3;
            case CJK_IDEOGRAPH_NINETH_ /* 20061 */:
            case CJK_IDEOGRAPH_COMPLEX_NINE_ /* 29590 */:
                return 9;
            case CJK_IDEOGRAPH_SECOND_ /* 20108 */:
            case CJK_IDEOGRAPH_COMPLEX_TWO_ /* 36019 */:
                return 2;
            case CJK_IDEOGRAPH_FIFTH_ /* 20116 */:
            case CJK_IDEOGRAPH_COMPLEX_FIVE_ /* 20237 */:
                return 5;
            case CJK_IDEOGRAPH_COMPLEX_THOUSAND_ /* 20191 */:
            case CJK_IDEOGRAPH_THOUSAND_ /* 21315 */:
                return 1000;
            case CJK_IDEOGRAPH_COMPLEX_HUNDRED_ /* 20336 */:
            case CJK_IDEOGRAPH_HUNDRED_ /* 30334 */:
                return 100;
            case CJK_IDEOGRAPH_HUNDRED_MILLION_ /* 20740 */:
                return 100000000;
            case CJK_IDEOGRAPH_EIGHTH_ /* 20843 */:
            case CJK_IDEOGRAPH_COMPLEX_EIGHT_ /* 25420 */:
                return 8;
            case CJK_IDEOGRAPH_SIXTH_ /* 20845 */:
            case CJK_IDEOGRAPH_COMPLEX_SIX_ /* 38520 */:
                return 6;
            case CJK_IDEOGRAPH_TEN_ /* 21313 */:
            case CJK_IDEOGRAPH_COMPLEX_TEN_ /* 25342 */:
                return 10;
            case CJK_IDEOGRAPH_FOURTH_ /* 22235 */:
            case CJK_IDEOGRAPH_COMPLEX_FOUR_ /* 32902 */:
                return 4;
            case CJK_IDEOGRAPH_TEN_THOUSAND_ /* 33356 */:
                return 10000;
            default:
                return -1;
        }
    }

    @Deprecated
    public static String getISOComment(int i) {
        return null;
    }

    public static int getIntPropertyMinValue(int i) {
        return 0;
    }

    @Deprecated
    public static String getName1_0(int i) {
        return null;
    }

    public static boolean isBMP(int i) {
        return i >= 0 && i <= 65535;
    }

    public static boolean isISOControl(int i) {
        return i >= 0 && i <= 159 && (i <= 31 || i >= 127);
    }

    @Deprecated
    public static boolean isSpace(int i) {
        return i <= 32 && (i == 32 || i == 9 || i == 10 || i == 12 || i == 13);
    }

    public static boolean isSupplementary(int i) {
        return i >= 65536 && i <= 1114111;
    }

    public static final boolean isValidCodePoint(int i) {
        return i >= 0 && i <= 1114111;
    }

    public static final class UnicodeBlock extends Character.Subset {
        public static final UnicodeBlock ADLAM = new UnicodeBlock("ADLAM", 263);
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
        public static final UnicodeBlock BHAIKSUKI = new UnicodeBlock("BHAIKSUKI", 264);
        public static final int BHAIKSUKI_ID = 264;
        private static final UnicodeBlock[] BLOCKS_ = new UnicodeBlock[309];
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
        public static final UnicodeBlock CHESS_SYMBOLS = new UnicodeBlock("CHESS_SYMBOLS", 281);
        public static final int CHESS_SYMBOLS_ID = 281;
        public static final UnicodeBlock CHORASMIAN = new UnicodeBlock("CHORASMIAN", 301);
        public static final int CHORASMIAN_ID = 301;
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
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_F = new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_F", 274);
        public static final int CJK_UNIFIED_IDEOGRAPHS_EXTENSION_F_ID = 274;
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_G = new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_G", 302);
        public static final int CJK_UNIFIED_IDEOGRAPHS_EXTENSION_G_ID = 302;
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
        public static final int COUNT = 309;
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
        public static final UnicodeBlock CYRILLIC_EXTENDED_C = new UnicodeBlock("CYRILLIC_EXTENDED_C", 265);
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
        public static final UnicodeBlock DIVES_AKURU = new UnicodeBlock("DIVES_AKURU", 303);
        public static final int DIVES_AKURU_ID = 303;
        public static final UnicodeBlock DOGRA = new UnicodeBlock("DOGRA", 282);
        public static final int DOGRA_ID = 282;
        public static final UnicodeBlock DOMINO_TILES = new UnicodeBlock("DOMINO_TILES", 171);
        public static final int DOMINO_TILES_ID = 171;
        public static final UnicodeBlock DUPLOYAN = new UnicodeBlock("DUPLOYAN", 225);
        public static final int DUPLOYAN_ID = 225;
        public static final UnicodeBlock EARLY_DYNASTIC_CUNEIFORM = new UnicodeBlock("EARLY_DYNASTIC_CUNEIFORM", 257);
        public static final int EARLY_DYNASTIC_CUNEIFORM_ID = 257;
        public static final UnicodeBlock EGYPTIAN_HIEROGLYPHS = new UnicodeBlock("EGYPTIAN_HIEROGLYPHS", 194);
        public static final int EGYPTIAN_HIEROGLYPHS_ID = 194;
        public static final UnicodeBlock EGYPTIAN_HIEROGLYPH_FORMAT_CONTROLS = new UnicodeBlock("EGYPTIAN_HIEROGLYPH_FORMAT_CONTROLS", 292);
        public static final int EGYPTIAN_HIEROGLYPH_FORMAT_CONTROLS_ID = 292;
        public static final UnicodeBlock ELBASAN = new UnicodeBlock("ELBASAN", 226);
        public static final int ELBASAN_ID = 226;
        public static final UnicodeBlock ELYMAIC = new UnicodeBlock("ELYMAIC", 293);
        public static final int ELYMAIC_ID = 293;
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
        public static final UnicodeBlock GEORGIAN_EXTENDED = new UnicodeBlock("GEORGIAN_EXTENDED", 283);
        public static final int GEORGIAN_EXTENDED_ID = 283;
        public static final int GEORGIAN_ID = 29;
        public static final UnicodeBlock GEORGIAN_SUPPLEMENT = new UnicodeBlock("GEORGIAN_SUPPLEMENT", 135);
        public static final int GEORGIAN_SUPPLEMENT_ID = 135;
        public static final UnicodeBlock GLAGOLITIC = new UnicodeBlock("GLAGOLITIC", 136);
        public static final int GLAGOLITIC_ID = 136;
        public static final UnicodeBlock GLAGOLITIC_SUPPLEMENT = new UnicodeBlock("GLAGOLITIC_SUPPLEMENT", 266);
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
        public static final UnicodeBlock GUNJALA_GONDI = new UnicodeBlock("GUNJALA_GONDI", 284);
        public static final int GUNJALA_GONDI_ID = 284;
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
        public static final UnicodeBlock HANIFI_ROHINGYA = new UnicodeBlock("HANIFI_ROHINGYA", 285);
        public static final int HANIFI_ROHINGYA_ID = 285;
        public static final UnicodeBlock HANUNOO = new UnicodeBlock("HANUNOO", 99);
        public static final int HANUNOO_ID = 99;
        public static final UnicodeBlock HATRAN = new UnicodeBlock("HATRAN", 258);
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
        public static final UnicodeBlock IDEOGRAPHIC_SYMBOLS_AND_PUNCTUATION = new UnicodeBlock("IDEOGRAPHIC_SYMBOLS_AND_PUNCTUATION", 267);
        public static final int IDEOGRAPHIC_SYMBOLS_AND_PUNCTUATION_ID = 267;
        public static final UnicodeBlock IMPERIAL_ARAMAIC = new UnicodeBlock("IMPERIAL_ARAMAIC", 186);
        public static final int IMPERIAL_ARAMAIC_ID = 186;
        public static final UnicodeBlock INDIC_SIYAQ_NUMBERS = new UnicodeBlock("INDIC_SIYAQ_NUMBERS", 286);
        public static final int INDIC_SIYAQ_NUMBERS_ID = 286;
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
        public static final UnicodeBlock KANA_EXTENDED_A = new UnicodeBlock("KANA_EXTENDED_A", 275);
        public static final int KANA_EXTENDED_A_ID = 275;
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
        public static final UnicodeBlock KHITAN_SMALL_SCRIPT = new UnicodeBlock("KHITAN_SMALL_SCRIPT", 304);
        public static final int KHITAN_SMALL_SCRIPT_ID = 304;
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
        public static final UnicodeBlock LISU_SUPPLEMENT = new UnicodeBlock("LISU_SUPPLEMENT", 305);
        public static final int LISU_SUPPLEMENT_ID = 305;
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
        public static final UnicodeBlock MAKASAR = new UnicodeBlock("MAKASAR", 287);
        public static final int MAKASAR_ID = 287;
        public static final UnicodeBlock MALAYALAM = new UnicodeBlock("MALAYALAM", 23);
        public static final int MALAYALAM_ID = 23;
        public static final UnicodeBlock MANDAIC = new UnicodeBlock("MANDAIC", 198);
        public static final int MANDAIC_ID = 198;
        public static final UnicodeBlock MANICHAEAN = new UnicodeBlock("MANICHAEAN", 234);
        public static final int MANICHAEAN_ID = 234;
        public static final UnicodeBlock MARCHEN = new UnicodeBlock("MARCHEN", 268);
        public static final int MARCHEN_ID = 268;
        public static final UnicodeBlock MASARAM_GONDI = new UnicodeBlock("MASARAM_GONDI", 276);
        public static final int MASARAM_GONDI_ID = 276;
        public static final UnicodeBlock MATHEMATICAL_ALPHANUMERIC_SYMBOLS = new UnicodeBlock("MATHEMATICAL_ALPHANUMERIC_SYMBOLS", 93);
        public static final int MATHEMATICAL_ALPHANUMERIC_SYMBOLS_ID = 93;
        public static final UnicodeBlock MATHEMATICAL_OPERATORS = new UnicodeBlock("MATHEMATICAL_OPERATORS", 47);
        public static final int MATHEMATICAL_OPERATORS_ID = 47;
        public static final UnicodeBlock MAYAN_NUMERALS = new UnicodeBlock("MAYAN_NUMERALS", 288);
        public static final int MAYAN_NUMERALS_ID = 288;
        public static final UnicodeBlock MEDEFAIDRIN = new UnicodeBlock("MEDEFAIDRIN", 289);
        public static final int MEDEFAIDRIN_ID = 289;
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
        public static final UnicodeBlock MONGOLIAN_SUPPLEMENT = new UnicodeBlock("MONGOLIAN_SUPPLEMENT", 269);
        public static final int MONGOLIAN_SUPPLEMENT_ID = 269;
        public static final UnicodeBlock MRO = new UnicodeBlock("MRO", 237);
        public static final int MRO_ID = 237;
        public static final UnicodeBlock MULTANI = new UnicodeBlock("MULTANI", 259);
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
        public static final UnicodeBlock NANDINAGARI = new UnicodeBlock("NANDINAGARI", 294);
        public static final int NANDINAGARI_ID = 294;
        public static final UnicodeBlock NEWA = new UnicodeBlock("NEWA", 270);
        public static final int NEWA_ID = 270;
        public static final UnicodeBlock NEW_TAI_LUE = new UnicodeBlock("NEW_TAI_LUE", 139);
        public static final int NEW_TAI_LUE_ID = 139;
        public static final UnicodeBlock NKO = new UnicodeBlock("NKO", 146);
        public static final int NKO_ID = 146;
        public static final UnicodeBlock NO_BLOCK = new UnicodeBlock("NO_BLOCK", 0);
        public static final UnicodeBlock NUMBER_FORMS = new UnicodeBlock("NUMBER_FORMS", 45);
        public static final int NUMBER_FORMS_ID = 45;
        public static final UnicodeBlock NUSHU = new UnicodeBlock("NUSHU", 277);
        public static final int NUSHU_ID = 277;
        public static final UnicodeBlock NYIAKENG_PUACHUE_HMONG = new UnicodeBlock("NYIAKENG_PUACHUE_HMONG", 295);
        public static final int NYIAKENG_PUACHUE_HMONG_ID = 295;
        public static final UnicodeBlock OGHAM = new UnicodeBlock("OGHAM", 34);
        public static final int OGHAM_ID = 34;
        public static final UnicodeBlock OLD_HUNGARIAN = new UnicodeBlock("OLD_HUNGARIAN", 260);
        public static final int OLD_HUNGARIAN_ID = 260;
        public static final UnicodeBlock OLD_ITALIC = new UnicodeBlock("OLD_ITALIC", 88);
        public static final int OLD_ITALIC_ID = 88;
        public static final UnicodeBlock OLD_NORTH_ARABIAN = new UnicodeBlock("OLD_NORTH_ARABIAN", 240);
        public static final int OLD_NORTH_ARABIAN_ID = 240;
        public static final UnicodeBlock OLD_PERMIC = new UnicodeBlock("OLD_PERMIC", 241);
        public static final int OLD_PERMIC_ID = 241;
        public static final UnicodeBlock OLD_PERSIAN = new UnicodeBlock("OLD_PERSIAN", 140);
        public static final int OLD_PERSIAN_ID = 140;
        public static final UnicodeBlock OLD_SOGDIAN = new UnicodeBlock("OLD_SOGDIAN", 290);
        public static final int OLD_SOGDIAN_ID = 290;
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
        public static final UnicodeBlock OSAGE = new UnicodeBlock("OSAGE", 271);
        public static final int OSAGE_ID = 271;
        public static final UnicodeBlock OSMANYA = new UnicodeBlock("OSMANYA", 122);
        public static final int OSMANYA_ID = 122;
        public static final UnicodeBlock OTTOMAN_SIYAQ_NUMBERS = new UnicodeBlock("OTTOMAN_SIYAQ_NUMBERS", 296);
        public static final int OTTOMAN_SIYAQ_NUMBERS_ID = 296;
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
        public static final UnicodeBlock SMALL_KANA_EXTENSION = new UnicodeBlock("SMALL_KANA_EXTENSION", 297);
        public static final int SMALL_KANA_EXTENSION_ID = 297;
        public static final UnicodeBlock SOGDIAN = new UnicodeBlock("SOGDIAN", 291);
        public static final int SOGDIAN_ID = 291;
        public static final UnicodeBlock SORA_SOMPENG = new UnicodeBlock("SORA_SOMPENG", 218);
        public static final int SORA_SOMPENG_ID = 218;
        public static final UnicodeBlock SOYOMBO = new UnicodeBlock("SOYOMBO", 278);
        public static final int SOYOMBO_ID = 278;
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
        public static final UnicodeBlock SUPPLEMENTAL_SYMBOLS_AND_PICTOGRAPHS = new UnicodeBlock("SUPPLEMENTAL_SYMBOLS_AND_PICTOGRAPHS", 261);
        public static final int SUPPLEMENTAL_SYMBOLS_AND_PICTOGRAPHS_ID = 261;
        public static final UnicodeBlock SUPPLEMENTARY_PRIVATE_USE_AREA_A = new UnicodeBlock("SUPPLEMENTARY_PRIVATE_USE_AREA_A", 109);
        public static final int SUPPLEMENTARY_PRIVATE_USE_AREA_A_ID = 109;
        public static final UnicodeBlock SUPPLEMENTARY_PRIVATE_USE_AREA_B = new UnicodeBlock("SUPPLEMENTARY_PRIVATE_USE_AREA_B", 110);
        public static final int SUPPLEMENTARY_PRIVATE_USE_AREA_B_ID = 110;
        public static final UnicodeBlock SUTTON_SIGNWRITING = new UnicodeBlock("SUTTON_SIGNWRITING", 262);
        public static final int SUTTON_SIGNWRITING_ID = 262;
        public static final UnicodeBlock SYLOTI_NAGRI = new UnicodeBlock("SYLOTI_NAGRI", 143);
        public static final int SYLOTI_NAGRI_ID = 143;
        public static final UnicodeBlock SYMBOLS_AND_PICTOGRAPHS_EXTENDED_A = new UnicodeBlock("SYMBOLS_AND_PICTOGRAPHS_EXTENDED_A", 298);
        public static final int SYMBOLS_AND_PICTOGRAPHS_EXTENDED_A_ID = 298;
        public static final UnicodeBlock SYMBOLS_FOR_LEGACY_COMPUTING = new UnicodeBlock("SYMBOLS_FOR_LEGACY_COMPUTING", 306);
        public static final int SYMBOLS_FOR_LEGACY_COMPUTING_ID = 306;
        public static final UnicodeBlock SYRIAC = new UnicodeBlock("SYRIAC", 13);
        public static final int SYRIAC_ID = 13;
        public static final UnicodeBlock SYRIAC_SUPPLEMENT = new UnicodeBlock("SYRIAC_SUPPLEMENT", 279);
        public static final int SYRIAC_SUPPLEMENT_ID = 279;
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
        public static final UnicodeBlock TAMIL_SUPPLEMENT = new UnicodeBlock("TAMIL_SUPPLEMENT", 299);
        public static final int TAMIL_SUPPLEMENT_ID = 299;
        public static final UnicodeBlock TANGUT = new UnicodeBlock("TANGUT", 272);
        public static final UnicodeBlock TANGUT_COMPONENTS = new UnicodeBlock("TANGUT_COMPONENTS", 273);
        public static final int TANGUT_COMPONENTS_ID = 273;
        public static final int TANGUT_ID = 272;
        public static final UnicodeBlock TANGUT_SUPPLEMENT = new UnicodeBlock("TANGUT_SUPPLEMENT", 307);
        public static final int TANGUT_SUPPLEMENT_ID = 307;
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
        public static final UnicodeBlock WANCHO = new UnicodeBlock("WANCHO", 300);
        public static final int WANCHO_ID = 300;
        public static final UnicodeBlock WARANG_CITI = new UnicodeBlock("WARANG_CITI", 252);
        public static final int WARANG_CITI_ID = 252;
        public static final UnicodeBlock YEZIDI = new UnicodeBlock("YEZIDI", 308);
        public static final int YEZIDI_ID = 308;
        public static final UnicodeBlock YIJING_HEXAGRAM_SYMBOLS = new UnicodeBlock("YIJING_HEXAGRAM_SYMBOLS", 116);
        public static final int YIJING_HEXAGRAM_SYMBOLS_ID = 116;
        public static final UnicodeBlock YI_RADICALS = new UnicodeBlock("YI_RADICALS", 73);
        public static final int YI_RADICALS_ID = 73;
        public static final UnicodeBlock YI_SYLLABLES = new UnicodeBlock("YI_SYLLABLES", 72);
        public static final int YI_SYLLABLES_ID = 72;
        public static final UnicodeBlock ZANABAZAR_SQUARE = new UnicodeBlock("ZANABAZAR_SQUARE", 280);
        public static final int ZANABAZAR_SQUARE_ID = 280;
        private static SoftReference<Map<String, UnicodeBlock>> mref;
        private int m_id_;

        static {
            for (int i = 0; i < 309; i++) {
                if (BLOCKS_[i] == null) {
                    throw new IllegalStateException("UnicodeBlock.BLOCKS_[" + i + "] not initialized");
                }
            }
        }

        public static UnicodeBlock getInstance(int i) {
            if (i >= 0) {
                UnicodeBlock[] unicodeBlockArr = BLOCKS_;
                if (i < unicodeBlockArr.length) {
                    return unicodeBlockArr[i];
                }
            }
            return INVALID_CODE;
        }

        public static UnicodeBlock of(int i) {
            if (i > 1114111) {
                return INVALID_CODE;
            }
            return getInstance(UCharacterProperty.INSTANCE.getIntPropertyValue(i, 4097));
        }

        public static final UnicodeBlock forName(String str) {
            SoftReference<Map<String, UnicodeBlock>> softReference = mref;
            Map<String, UnicodeBlock> map = softReference != null ? softReference.get() : null;
            if (map == null) {
                map = new HashMap<>(BLOCKS_.length);
                int i = 0;
                while (true) {
                    UnicodeBlock[] unicodeBlockArr = BLOCKS_;
                    if (i >= unicodeBlockArr.length) {
                        break;
                    }
                    UnicodeBlock unicodeBlock = unicodeBlockArr[i];
                    map.put(trimBlockName(UCharacter.getPropertyValueName(4097, unicodeBlock.getID(), 1)), unicodeBlock);
                    i++;
                }
                mref = new SoftReference<>(map);
            }
            UnicodeBlock unicodeBlock2 = map.get(trimBlockName(str));
            if (unicodeBlock2 != null) {
                return unicodeBlock2;
            }
            throw new IllegalArgumentException();
        }

        private static String trimBlockName(String str) {
            String upperCase = str.toUpperCase(Locale.ENGLISH);
            StringBuilder sb = new StringBuilder(upperCase.length());
            for (int i = 0; i < upperCase.length(); i++) {
                char charAt = upperCase.charAt(i);
                if (!(charAt == ' ' || charAt == '_' || charAt == '-')) {
                    sb.append(charAt);
                }
            }
            return sb.toString();
        }

        public int getID() {
            return this.m_id_;
        }

        private UnicodeBlock(String str, int i) {
            super(str);
            this.m_id_ = i;
            if (i >= 0) {
                BLOCKS_[i] = this;
            }
        }
    }

    public static int digit(int i, int i2) {
        if (2 > i2 || i2 > 36) {
            return -1;
        }
        int digit = digit(i);
        int europeanDigit = digit < 0 ? UCharacterProperty.getEuropeanDigit(i) : digit;
        if (europeanDigit < i2) {
            return europeanDigit;
        }
        return -1;
    }

    public static int digit(int i) {
        return UCharacterProperty.INSTANCE.digit(i);
    }

    public static int getNumericValue(int i) {
        return UCharacterProperty.INSTANCE.getNumericValue(i);
    }

    public static double getUnicodeNumericValue(int i) {
        return UCharacterProperty.INSTANCE.getUnicodeNumericValue(i);
    }

    public static int getType(int i) {
        return UCharacterProperty.INSTANCE.getType(i);
    }

    public static boolean isDefined(int i) {
        return getType(i) != 0;
    }

    public static boolean isDigit(int i) {
        return getType(i) == 9;
    }

    public static boolean isLetter(int i) {
        return ((1 << getType(i)) & 62) != 0;
    }

    public static boolean isLetterOrDigit(int i) {
        return ((1 << getType(i)) & 574) != 0;
    }

    @Deprecated
    public static boolean isJavaLetter(int i) {
        return isJavaIdentifierStart(i);
    }

    @Deprecated
    public static boolean isJavaLetterOrDigit(int i) {
        return isJavaIdentifierPart(i);
    }

    public static boolean isJavaIdentifierStart(int i) {
        return Character.isJavaIdentifierStart((char) i);
    }

    public static boolean isJavaIdentifierPart(int i) {
        return Character.isJavaIdentifierPart((char) i);
    }

    public static boolean isLowerCase(int i) {
        return getType(i) == 2;
    }

    public static boolean isWhitespace(int i) {
        if (((1 << getType(i)) & 28672) != 0 && i != 160 && i != FIGURE_SPACE_ && i != NARROW_NO_BREAK_SPACE_) {
            return true;
        }
        if (i < 9 || i > 13) {
            return i >= 28 && i <= 31;
        }
        return true;
    }

    public static boolean isSpaceChar(int i) {
        return ((1 << getType(i)) & 28672) != 0;
    }

    public static boolean isTitleCase(int i) {
        return getType(i) == 3;
    }

    public static boolean isUnicodeIdentifierPart(int i) {
        return ((1 << getType(i)) & 4196222) != 0 || isIdentifierIgnorable(i);
    }

    public static boolean isUnicodeIdentifierStart(int i) {
        return ((1 << getType(i)) & 1086) != 0;
    }

    public static boolean isIdentifierIgnorable(int i) {
        return i <= 159 ? isISOControl(i) && (i < 9 || i > 13) && (i < 28 || i > 31) : getType(i) == 16;
    }

    public static boolean isUpperCase(int i) {
        return getType(i) == 1;
    }

    public static int toLowerCase(int i) {
        return UCaseProps.INSTANCE.tolower(i);
    }

    public static String toString(int i) {
        if (i < 0 || i > 1114111) {
            return null;
        }
        if (i < 65536) {
            return String.valueOf((char) i);
        }
        return new String(Character.toChars(i));
    }

    public static int toTitleCase(int i) {
        return UCaseProps.INSTANCE.totitle(i);
    }

    public static int toUpperCase(int i) {
        return UCaseProps.INSTANCE.toupper(i);
    }

    public static boolean isPrintable(int i) {
        int type = getType(i);
        return (type == 0 || type == 15 || type == 16 || type == 17 || type == 18 || type == 0) ? false : true;
    }

    public static boolean isBaseForm(int i) {
        int type = getType(i);
        return type == 9 || type == 11 || type == 10 || type == 1 || type == 2 || type == 3 || type == 4 || type == 5 || type == 6 || type == 7 || type == 8;
    }

    public static int getDirection(int i) {
        return UBiDiProps.INSTANCE.getClass(i);
    }

    public static boolean isMirrored(int i) {
        return UBiDiProps.INSTANCE.isMirrored(i);
    }

    public static int getMirror(int i) {
        return UBiDiProps.INSTANCE.getMirror(i);
    }

    public static int getBidiPairedBracket(int i) {
        return UBiDiProps.INSTANCE.getPairedBracket(i);
    }

    public static int getCombiningClass(int i) {
        return Normalizer2.getNFDInstance().getCombiningClass(i);
    }

    public static boolean isLegal(int i) {
        if (i < 0) {
            return false;
        }
        if (i < 55296) {
            return true;
        }
        return i > 57343 && !UCharacterUtility.isNonCharacter(i) && i <= 1114111;
    }

    public static boolean isLegal(String str) {
        int length = str.length();
        int i = 0;
        while (i < length) {
            int codePointAt = str.codePointAt(i);
            if (!isLegal(codePointAt)) {
                return false;
            }
            i += Character.charCount(codePointAt);
        }
        return true;
    }

    public static VersionInfo getUnicodeVersion() {
        return UCharacterProperty.INSTANCE.m_unicodeVersion_;
    }

    public static String getName(int i) {
        return UCharacterName.INSTANCE.getName(i, 0);
    }

    public static String getName(String str, String str2) {
        int i = 0;
        if (str.length() == 1) {
            return getName(str.charAt(0));
        }
        StringBuilder sb = new StringBuilder();
        while (i < str.length()) {
            int codePointAt = str.codePointAt(i);
            if (i != 0) {
                sb.append(str2);
            }
            sb.append(getName(codePointAt));
            i += Character.charCount(codePointAt);
        }
        return sb.toString();
    }

    public static String getExtendedName(int i) {
        return UCharacterName.INSTANCE.getName(i, 2);
    }

    public static String getNameAlias(int i) {
        return UCharacterName.INSTANCE.getName(i, 3);
    }

    public static int getCharFromName(String str) {
        return UCharacterName.INSTANCE.getCharFromName(0, str);
    }

    public static int getCharFromExtendedName(String str) {
        return UCharacterName.INSTANCE.getCharFromName(2, str);
    }

    public static int getCharFromNameAlias(String str) {
        return UCharacterName.INSTANCE.getCharFromName(3, str);
    }

    public static String getPropertyName(int i, int i2) {
        return UPropertyAliases.INSTANCE.getPropertyName(i, i2);
    }

    public static int getPropertyEnum(CharSequence charSequence) {
        int propertyEnum = UPropertyAliases.INSTANCE.getPropertyEnum(charSequence);
        if (propertyEnum != -1) {
            return propertyEnum;
        }
        throw new IllegalIcuArgumentException("Invalid name: " + ((Object) charSequence));
    }

    public static String getPropertyValueName(int i, int i2, int i3) {
        if ((i != 4098 && i != 4112 && i != 4113) || i2 < getIntPropertyMinValue(4098) || i2 > getIntPropertyMaxValue(4098) || i3 < 0 || i3 >= 2) {
            return UPropertyAliases.INSTANCE.getPropertyValueName(i, i2, i3);
        }
        try {
            return UPropertyAliases.INSTANCE.getPropertyValueName(i, i2, i3);
        } catch (IllegalArgumentException unused) {
            return null;
        }
    }

    public static int getPropertyValueEnum(int i, CharSequence charSequence) {
        int propertyValueEnum = UPropertyAliases.INSTANCE.getPropertyValueEnum(i, charSequence);
        if (propertyValueEnum != -1) {
            return propertyValueEnum;
        }
        throw new IllegalIcuArgumentException("Invalid name: " + ((Object) charSequence));
    }

    @Deprecated
    public static int getPropertyValueEnumNoThrow(int i, CharSequence charSequence) {
        return UPropertyAliases.INSTANCE.getPropertyValueEnumNoThrow(i, charSequence);
    }

    public static int getCodePoint(char c, char c2) {
        if (Character.isSurrogatePair(c, c2)) {
            return Character.toCodePoint(c, c2);
        }
        throw new IllegalArgumentException("Illegal surrogate characters");
    }

    public static int getCodePoint(char c) {
        if (isLegal(c)) {
            return c;
        }
        throw new IllegalArgumentException("Illegal codepoint");
    }

    public static String toUpperCase(String str) {
        return CaseMapImpl.toUpper(getDefaultCaseLocale(), 0, str);
    }

    public static String toLowerCase(String str) {
        return CaseMapImpl.toLower(getDefaultCaseLocale(), 0, str);
    }

    public static String toTitleCase(String str, BreakIterator breakIterator) {
        return toTitleCase(Locale.getDefault(), str, breakIterator, 0);
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

    private static int getCaseLocale(ULocale uLocale) {
        if (uLocale == null) {
            uLocale = ULocale.getDefault();
        }
        return UCaseProps.getCaseLocale(uLocale);
    }

    public static String toUpperCase(Locale locale, String str) {
        return CaseMapImpl.toUpper(getCaseLocale(locale), 0, str);
    }

    public static String toUpperCase(ULocale uLocale, String str) {
        return CaseMapImpl.toUpper(getCaseLocale(uLocale), 0, str);
    }

    public static String toLowerCase(Locale locale, String str) {
        return CaseMapImpl.toLower(getCaseLocale(locale), 0, str);
    }

    public static String toLowerCase(ULocale uLocale, String str) {
        return CaseMapImpl.toLower(getCaseLocale(uLocale), 0, str);
    }

    public static String toTitleCase(Locale locale, String str, BreakIterator breakIterator) {
        return toTitleCase(locale, str, breakIterator, 0);
    }

    public static String toTitleCase(ULocale uLocale, String str, BreakIterator breakIterator) {
        return toTitleCase(uLocale, str, breakIterator, 0);
    }

    public static String toTitleCase(ULocale uLocale, String str, BreakIterator breakIterator, int i) {
        if (breakIterator == null && uLocale == null) {
            uLocale = ULocale.getDefault();
        }
        BreakIterator titleBreakIterator = CaseMapImpl.getTitleBreakIterator(uLocale, i, breakIterator);
        titleBreakIterator.setText(str);
        return CaseMapImpl.toTitle(getCaseLocale(uLocale), i, titleBreakIterator, str);
    }

    public static String toTitleCase(Locale locale, String str, BreakIterator breakIterator, int i) {
        if (breakIterator == null && locale == null) {
            locale = Locale.getDefault();
        }
        BreakIterator titleBreakIterator = CaseMapImpl.getTitleBreakIterator(locale, i, breakIterator);
        titleBreakIterator.setText(str);
        return CaseMapImpl.toTitle(getCaseLocale(locale), i, titleBreakIterator, str);
    }

    public static int foldCase(int i, boolean z) {
        return foldCase(i, !z ? 1 : 0);
    }

    public static String foldCase(String str, boolean z) {
        return foldCase(str, !z ? 1 : 0);
    }

    public static int foldCase(int i, int i2) {
        return UCaseProps.INSTANCE.fold(i, i2);
    }

    public static final String foldCase(String str, int i) {
        return CaseMapImpl.fold(i, str);
    }

    public static RangeValueIterator getTypeIterator() {
        return new UCharacterTypeIterator();
    }

    private static final class UCharacterTypeIterator implements RangeValueIterator {
        private static final MaskType MASK_TYPE = new MaskType();
        private Trie2.Range range;
        private Iterator<Trie2.Range> trieIterator;

        UCharacterTypeIterator() {
            reset();
        }

        public boolean next(RangeValueIterator.Element element) {
            if (!this.trieIterator.hasNext()) {
                return false;
            }
            Trie2.Range next = this.trieIterator.next();
            this.range = next;
            if (next.leadSurrogate) {
                return false;
            }
            element.start = this.range.startCodePoint;
            element.limit = this.range.endCodePoint + 1;
            element.value = this.range.value;
            return true;
        }

        public void reset() {
            this.trieIterator = UCharacterProperty.INSTANCE.m_trie_.iterator(MASK_TYPE);
        }

        /* access modifiers changed from: private */
        public static final class MaskType implements Trie2.ValueMapper {
            @Override // ohos.global.icu.impl.Trie2.ValueMapper
            public int map(int i) {
                return i & 31;
            }

            private MaskType() {
            }
        }
    }

    public static ValueIterator getNameIterator() {
        return new UCharacterNameIterator(UCharacterName.INSTANCE, 0);
    }

    @Deprecated
    public static ValueIterator getName1_0Iterator() {
        return new DummyValueIterator();
    }

    private static final class DummyValueIterator implements ValueIterator {
        public boolean next(ValueIterator.Element element) {
            return false;
        }

        public void reset() {
        }

        public void setRange(int i, int i2) {
        }

        private DummyValueIterator() {
        }
    }

    public static ValueIterator getExtendedNameIterator() {
        return new UCharacterNameIterator(UCharacterName.INSTANCE, 2);
    }

    public static VersionInfo getAge(int i) {
        if (i >= 0 && i <= 1114111) {
            return UCharacterProperty.INSTANCE.getAge(i);
        }
        throw new IllegalArgumentException("Codepoint out of bounds");
    }

    public static boolean hasBinaryProperty(int i, int i2) {
        return UCharacterProperty.INSTANCE.hasBinaryProperty(i, i2);
    }

    public static boolean isUAlphabetic(int i) {
        return hasBinaryProperty(i, 0);
    }

    public static boolean isULowercase(int i) {
        return hasBinaryProperty(i, 22);
    }

    public static boolean isUUppercase(int i) {
        return hasBinaryProperty(i, 30);
    }

    public static boolean isUWhiteSpace(int i) {
        return hasBinaryProperty(i, 31);
    }

    public static int getIntPropertyValue(int i, int i2) {
        return UCharacterProperty.INSTANCE.getIntPropertyValue(i, i2);
    }

    @Deprecated
    public static String getStringPropertyValue(int i, int i2, int i3) {
        if ((i >= 0 && i < 65) || (i >= 4096 && i < 4121)) {
            return getPropertyValueName(i, getIntPropertyValue(i2, i), i3);
        }
        if (i == 12288) {
            return String.valueOf(getUnicodeNumericValue(i2));
        }
        switch (i) {
            case 16384:
                return getAge(i2).toString();
            case UProperty.BIDI_MIRRORING_GLYPH /* 16385 */:
                return toString(getMirror(i2));
            case UProperty.CASE_FOLDING /* 16386 */:
                return toString(foldCase(i2, true));
            case UProperty.ISO_COMMENT /* 16387 */:
                return getISOComment(i2);
            case UProperty.LOWERCASE_MAPPING /* 16388 */:
                return toString(toLowerCase(i2));
            case UProperty.NAME /* 16389 */:
                return getName(i2);
            case UProperty.SIMPLE_CASE_FOLDING /* 16390 */:
                return toString(foldCase(i2, true));
            case UProperty.SIMPLE_LOWERCASE_MAPPING /* 16391 */:
                return toString(toLowerCase(i2));
            case UProperty.SIMPLE_TITLECASE_MAPPING /* 16392 */:
                return toString(toTitleCase(i2));
            case UProperty.SIMPLE_UPPERCASE_MAPPING /* 16393 */:
                return toString(toUpperCase(i2));
            case UProperty.TITLECASE_MAPPING /* 16394 */:
                return toString(toTitleCase(i2));
            case UProperty.UNICODE_1_NAME /* 16395 */:
                return getName1_0(i2);
            case UProperty.UPPERCASE_MAPPING /* 16396 */:
                return toString(toUpperCase(i2));
            default:
                throw new IllegalArgumentException("Illegal Property Enum");
        }
    }

    public static int getIntPropertyMaxValue(int i) {
        return UCharacterProperty.INSTANCE.getIntPropertyMaxValue(i);
    }

    public static char forDigit(int i, int i2) {
        return Character.forDigit(i, i2);
    }

    public static final boolean isSupplementaryCodePoint(int i) {
        return Character.isSupplementaryCodePoint(i);
    }

    public static boolean isHighSurrogate(char c) {
        return Character.isHighSurrogate(c);
    }

    public static boolean isLowSurrogate(char c) {
        return Character.isLowSurrogate(c);
    }

    public static final boolean isSurrogatePair(char c, char c2) {
        return Character.isSurrogatePair(c, c2);
    }

    public static int charCount(int i) {
        return Character.charCount(i);
    }

    public static final int toCodePoint(char c, char c2) {
        return Character.toCodePoint(c, c2);
    }

    public static final int codePointAt(CharSequence charSequence, int i) {
        int i2 = i + 1;
        char charAt = charSequence.charAt(i);
        if (isHighSurrogate(charAt) && i2 < charSequence.length()) {
            char charAt2 = charSequence.charAt(i2);
            if (isLowSurrogate(charAt2)) {
                return toCodePoint(charAt, charAt2);
            }
        }
        return charAt;
    }

    public static final int codePointAt(char[] cArr, int i) {
        int i2 = i + 1;
        char c = cArr[i];
        if (isHighSurrogate(c) && i2 < cArr.length) {
            char c2 = cArr[i2];
            if (isLowSurrogate(c2)) {
                return toCodePoint(c, c2);
            }
        }
        return c;
    }

    public static final int codePointAt(char[] cArr, int i, int i2) {
        if (i >= i2 || i2 > cArr.length) {
            throw new IndexOutOfBoundsException();
        }
        int i3 = i + 1;
        char c = cArr[i];
        if (isHighSurrogate(c) && i3 < i2) {
            char c2 = cArr[i3];
            if (isLowSurrogate(c2)) {
                return toCodePoint(c, c2);
            }
        }
        return c;
    }

    public static final int codePointBefore(CharSequence charSequence, int i) {
        int i2 = i - 1;
        char charAt = charSequence.charAt(i2);
        if (isLowSurrogate(charAt) && i2 > 0) {
            char charAt2 = charSequence.charAt(i2 - 1);
            if (isHighSurrogate(charAt2)) {
                return toCodePoint(charAt2, charAt);
            }
        }
        return charAt;
    }

    public static final int codePointBefore(char[] cArr, int i) {
        int i2 = i - 1;
        char c = cArr[i2];
        if (isLowSurrogate(c) && i2 > 0) {
            char c2 = cArr[i2 - 1];
            if (isHighSurrogate(c2)) {
                return toCodePoint(c2, c);
            }
        }
        return c;
    }

    public static final int codePointBefore(char[] cArr, int i, int i2) {
        if (i <= i2 || i2 < 0) {
            throw new IndexOutOfBoundsException();
        }
        int i3 = i - 1;
        char c = cArr[i3];
        if (isLowSurrogate(c) && i3 > i2) {
            char c2 = cArr[i3 - 1];
            if (isHighSurrogate(c2)) {
                return toCodePoint(c2, c);
            }
        }
        return c;
    }

    public static final int toChars(int i, char[] cArr, int i2) {
        return Character.toChars(i, cArr, i2);
    }

    public static final char[] toChars(int i) {
        return Character.toChars(i);
    }

    public static byte getDirectionality(int i) {
        return (byte) getDirection(i);
    }

    public static int codePointCount(CharSequence charSequence, int i, int i2) {
        if (i < 0 || i2 < i || i2 > charSequence.length()) {
            throw new IndexOutOfBoundsException("start (" + i + ") or limit (" + i2 + ") invalid or out of range 0, " + charSequence.length());
        }
        int i3 = i2 - i;
        while (i2 > i) {
            i2--;
            char charAt = charSequence.charAt(i2);
            while (true) {
                if (charAt < 56320 || charAt > 57343 || i2 <= i) {
                    break;
                }
                i2--;
                charAt = charSequence.charAt(i2);
                if (charAt >= 55296 && charAt <= 56319) {
                    i3--;
                    break;
                }
            }
        }
        return i3;
    }

    public static int codePointCount(char[] cArr, int i, int i2) {
        if (i < 0 || i2 < i || i2 > cArr.length) {
            throw new IndexOutOfBoundsException("start (" + i + ") or limit (" + i2 + ") invalid or out of range 0, " + cArr.length);
        }
        int i3 = i2 - i;
        while (i2 > i) {
            i2--;
            char c = cArr[i2];
            while (true) {
                if (c < 56320 || c > 57343 || i2 <= i) {
                    break;
                }
                i2--;
                c = cArr[i2];
                if (c >= 55296 && c <= 56319) {
                    i3--;
                    break;
                }
            }
        }
        return i3;
    }

    public static int offsetByCodePoints(CharSequence charSequence, int i, int i2) {
        if (i < 0 || i > charSequence.length()) {
            throw new IndexOutOfBoundsException("index ( " + i + ") out of range 0, " + charSequence.length());
        }
        if (i2 >= 0) {
            int length = charSequence.length();
            while (true) {
                i2--;
                if (i2 < 0) {
                    break;
                }
                int i3 = i + 1;
                char charAt = charSequence.charAt(i);
                while (charAt >= 55296 && charAt <= 56319 && i3 < length) {
                    int i4 = i3 + 1;
                    char charAt2 = charSequence.charAt(i3);
                    if ((charAt2 < 56320 || charAt2 > 57343) && i2 - 1 < 0) {
                        return i4 - 1;
                    }
                    i3 = i4;
                    charAt = charAt2;
                }
                i = i3;
            }
        } else {
            while (true) {
                i2++;
                if (i2 > 0) {
                    break;
                }
                i--;
                char charAt3 = charSequence.charAt(i);
                while (charAt3 >= 56320 && charAt3 <= 57343 && i > 0) {
                    i--;
                    charAt3 = charSequence.charAt(i);
                    if ((charAt3 < 55296 || charAt3 > 56319) && (i2 = i2 + 1) > 0) {
                        return i + 1;
                    }
                }
            }
        }
        return i;
    }

    public static int offsetByCodePoints(char[] cArr, int i, int i2, int i3, int i4) {
        int i5 = i2 + i;
        if (i < 0 || i5 < i || i5 > cArr.length || i3 < i || i3 > i5) {
            throw new IndexOutOfBoundsException("index ( " + i3 + ") out of range " + i + ", " + i5 + " in array 0, " + cArr.length);
        }
        if (i4 >= 0) {
            while (true) {
                i4--;
                if (i4 < 0) {
                    break;
                }
                int i6 = i3 + 1;
                char c = cArr[i3];
                if (i6 <= i5) {
                    while (c >= 55296 && c <= 56319 && i6 < i5) {
                        int i7 = i6 + 1;
                        char c2 = cArr[i6];
                        if ((c2 < 56320 || c2 > 57343) && i4 - 1 < 0) {
                            return i7 - 1;
                        }
                        c = c2;
                        i6 = i7;
                    }
                    i3 = i6;
                } else {
                    throw new IndexOutOfBoundsException("index ( " + i6 + ") > limit (" + i5 + ")");
                }
            }
        } else {
            while (true) {
                i4++;
                if (i4 > 0) {
                    break;
                }
                i3--;
                char c3 = cArr[i3];
                if (i3 >= i) {
                    while (c3 >= 56320 && c3 <= 57343 && i3 > i) {
                        i3--;
                        c3 = cArr[i3];
                        if ((c3 < 55296 || c3 > 56319) && (i4 = i4 + 1) > 0) {
                            return i3 + 1;
                        }
                    }
                } else {
                    throw new IndexOutOfBoundsException("index ( " + i3 + ") < start (" + i + ")");
                }
            }
        }
        return i3;
    }

    private UCharacter() {
    }
}
