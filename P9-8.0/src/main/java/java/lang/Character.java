package java.lang;

import java.awt.font.NumericShaper;
import java.io.Serializable;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import sun.util.logging.PlatformLogger;

public final class Character implements Serializable, Comparable<Character> {
    public static final int BYTES = 2;
    public static final byte COMBINING_SPACING_MARK = (byte) 8;
    public static final byte CONNECTOR_PUNCTUATION = (byte) 23;
    public static final byte CONTROL = (byte) 15;
    public static final byte CURRENCY_SYMBOL = (byte) 26;
    public static final byte DASH_PUNCTUATION = (byte) 20;
    public static final byte DECIMAL_DIGIT_NUMBER = (byte) 9;
    private static final byte[] DIRECTIONALITY = new byte[]{(byte) 0, (byte) 1, (byte) 3, (byte) 4, (byte) 5, (byte) 6, (byte) 7, (byte) 10, (byte) 11, (byte) 12, (byte) 13, (byte) 14, (byte) 15, (byte) 2, (byte) 16, DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE, (byte) 18, (byte) 8, (byte) 9};
    public static final byte DIRECTIONALITY_ARABIC_NUMBER = (byte) 6;
    public static final byte DIRECTIONALITY_BOUNDARY_NEUTRAL = (byte) 9;
    public static final byte DIRECTIONALITY_COMMON_NUMBER_SEPARATOR = (byte) 7;
    public static final byte DIRECTIONALITY_EUROPEAN_NUMBER = (byte) 3;
    public static final byte DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR = (byte) 4;
    public static final byte DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR = (byte) 5;
    public static final byte DIRECTIONALITY_LEFT_TO_RIGHT = (byte) 0;
    public static final byte DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING = (byte) 14;
    public static final byte DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE = (byte) 15;
    public static final byte DIRECTIONALITY_NONSPACING_MARK = (byte) 8;
    public static final byte DIRECTIONALITY_OTHER_NEUTRALS = (byte) 13;
    public static final byte DIRECTIONALITY_PARAGRAPH_SEPARATOR = (byte) 10;
    public static final byte DIRECTIONALITY_POP_DIRECTIONAL_FORMAT = (byte) 18;
    public static final byte DIRECTIONALITY_RIGHT_TO_LEFT = (byte) 1;
    public static final byte DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC = (byte) 2;
    public static final byte DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING = (byte) 16;
    public static final byte DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE = (byte) 17;
    public static final byte DIRECTIONALITY_SEGMENT_SEPARATOR = (byte) 11;
    public static final byte DIRECTIONALITY_UNDEFINED = (byte) -1;
    public static final byte DIRECTIONALITY_WHITESPACE = (byte) 12;
    public static final byte ENCLOSING_MARK = (byte) 7;
    public static final byte END_PUNCTUATION = (byte) 22;
    static final int ERROR = -1;
    public static final byte FINAL_QUOTE_PUNCTUATION = (byte) 30;
    public static final byte FORMAT = (byte) 16;
    public static final byte INITIAL_QUOTE_PUNCTUATION = (byte) 29;
    public static final byte LETTER_NUMBER = (byte) 10;
    public static final byte LINE_SEPARATOR = (byte) 13;
    public static final byte LOWERCASE_LETTER = (byte) 2;
    public static final byte MATH_SYMBOL = (byte) 25;
    public static final int MAX_CODE_POINT = 1114111;
    public static final char MAX_HIGH_SURROGATE = '?';
    public static final char MAX_LOW_SURROGATE = '?';
    public static final int MAX_RADIX = 36;
    public static final char MAX_SURROGATE = '?';
    public static final char MAX_VALUE = '￿';
    public static final int MIN_CODE_POINT = 0;
    public static final char MIN_HIGH_SURROGATE = '?';
    public static final char MIN_LOW_SURROGATE = '?';
    public static final int MIN_RADIX = 2;
    public static final int MIN_SUPPLEMENTARY_CODE_POINT = 65536;
    public static final char MIN_SURROGATE = '?';
    public static final char MIN_VALUE = '\u0000';
    public static final byte MODIFIER_LETTER = (byte) 4;
    public static final byte MODIFIER_SYMBOL = (byte) 27;
    public static final byte NON_SPACING_MARK = (byte) 6;
    public static final byte OTHER_LETTER = (byte) 5;
    public static final byte OTHER_NUMBER = (byte) 11;
    public static final byte OTHER_PUNCTUATION = (byte) 24;
    public static final byte OTHER_SYMBOL = (byte) 28;
    public static final byte PARAGRAPH_SEPARATOR = (byte) 14;
    public static final byte PRIVATE_USE = (byte) 18;
    public static final int SIZE = 16;
    public static final byte SPACE_SEPARATOR = (byte) 12;
    public static final byte START_PUNCTUATION = (byte) 21;
    public static final byte SURROGATE = (byte) 19;
    public static final byte TITLECASE_LETTER = (byte) 3;
    public static final Class<Character> TYPE = char[].class.getComponentType();
    public static final byte UNASSIGNED = (byte) 0;
    public static final byte UPPERCASE_LETTER = (byte) 1;
    private static final long serialVersionUID = 3786198910865385080L;
    private final char value;

    private static class CharacterCache {
        static final Character[] cache = new Character[128];

        private CharacterCache() {
        }

        static {
            for (int i = 0; i < cache.length; i++) {
                cache[i] = new Character((char) i);
            }
        }
    }

    public static class Subset {
        private String name;

        protected Subset(String name) {
            if (name == null) {
                throw new NullPointerException("name");
            }
            this.name = name;
        }

        public final boolean equals(Object obj) {
            return this == obj;
        }

        public final int hashCode() {
            return super.hashCode();
        }

        public final String toString() {
            return this.name;
        }
    }

    public static final class UnicodeBlock extends Subset {
        public static final UnicodeBlock AEGEAN_NUMBERS = new UnicodeBlock("AEGEAN_NUMBERS", "AEGEAN NUMBERS", "AEGEANNUMBERS");
        public static final UnicodeBlock ALCHEMICAL_SYMBOLS = new UnicodeBlock("ALCHEMICAL_SYMBOLS", "ALCHEMICAL SYMBOLS", "ALCHEMICALSYMBOLS");
        public static final UnicodeBlock ALPHABETIC_PRESENTATION_FORMS = new UnicodeBlock("ALPHABETIC_PRESENTATION_FORMS", "ALPHABETIC PRESENTATION FORMS", "ALPHABETICPRESENTATIONFORMS");
        public static final UnicodeBlock ANCIENT_GREEK_MUSICAL_NOTATION = new UnicodeBlock("ANCIENT_GREEK_MUSICAL_NOTATION", "ANCIENT GREEK MUSICAL NOTATION", "ANCIENTGREEKMUSICALNOTATION");
        public static final UnicodeBlock ANCIENT_GREEK_NUMBERS = new UnicodeBlock("ANCIENT_GREEK_NUMBERS", "ANCIENT GREEK NUMBERS", "ANCIENTGREEKNUMBERS");
        public static final UnicodeBlock ANCIENT_SYMBOLS = new UnicodeBlock("ANCIENT_SYMBOLS", "ANCIENT SYMBOLS", "ANCIENTSYMBOLS");
        public static final UnicodeBlock ARABIC = new UnicodeBlock("ARABIC");
        public static final UnicodeBlock ARABIC_EXTENDED_A = new UnicodeBlock("ARABIC_EXTENDED_A", "ARABIC EXTENDED-A", "ARABICEXTENDED-A");
        public static final UnicodeBlock ARABIC_MATHEMATICAL_ALPHABETIC_SYMBOLS = new UnicodeBlock("ARABIC_MATHEMATICAL_ALPHABETIC_SYMBOLS", "ARABIC MATHEMATICAL ALPHABETIC SYMBOLS", "ARABICMATHEMATICALALPHABETICSYMBOLS");
        public static final UnicodeBlock ARABIC_PRESENTATION_FORMS_A = new UnicodeBlock("ARABIC_PRESENTATION_FORMS_A", "ARABIC PRESENTATION FORMS-A", "ARABICPRESENTATIONFORMS-A");
        public static final UnicodeBlock ARABIC_PRESENTATION_FORMS_B = new UnicodeBlock("ARABIC_PRESENTATION_FORMS_B", "ARABIC PRESENTATION FORMS-B", "ARABICPRESENTATIONFORMS-B");
        public static final UnicodeBlock ARABIC_SUPPLEMENT = new UnicodeBlock("ARABIC_SUPPLEMENT", "ARABIC SUPPLEMENT", "ARABICSUPPLEMENT");
        public static final UnicodeBlock ARMENIAN = new UnicodeBlock("ARMENIAN");
        public static final UnicodeBlock ARROWS = new UnicodeBlock("ARROWS");
        public static final UnicodeBlock AVESTAN = new UnicodeBlock("AVESTAN");
        public static final UnicodeBlock BALINESE = new UnicodeBlock("BALINESE");
        public static final UnicodeBlock BAMUM = new UnicodeBlock("BAMUM");
        public static final UnicodeBlock BAMUM_SUPPLEMENT = new UnicodeBlock("BAMUM_SUPPLEMENT", "BAMUM SUPPLEMENT", "BAMUMSUPPLEMENT");
        public static final UnicodeBlock BASIC_LATIN = new UnicodeBlock("BASIC_LATIN", "BASIC LATIN", "BASICLATIN");
        public static final UnicodeBlock BATAK = new UnicodeBlock("BATAK");
        public static final UnicodeBlock BENGALI = new UnicodeBlock("BENGALI");
        public static final UnicodeBlock BLOCK_ELEMENTS = new UnicodeBlock("BLOCK_ELEMENTS", "BLOCK ELEMENTS", "BLOCKELEMENTS");
        public static final UnicodeBlock BOPOMOFO = new UnicodeBlock("BOPOMOFO");
        public static final UnicodeBlock BOPOMOFO_EXTENDED = new UnicodeBlock("BOPOMOFO_EXTENDED", "BOPOMOFO EXTENDED", "BOPOMOFOEXTENDED");
        public static final UnicodeBlock BOX_DRAWING = new UnicodeBlock("BOX_DRAWING", "BOX DRAWING", "BOXDRAWING");
        public static final UnicodeBlock BRAHMI = new UnicodeBlock("BRAHMI");
        public static final UnicodeBlock BRAILLE_PATTERNS = new UnicodeBlock("BRAILLE_PATTERNS", "BRAILLE PATTERNS", "BRAILLEPATTERNS");
        public static final UnicodeBlock BUGINESE = new UnicodeBlock("BUGINESE");
        public static final UnicodeBlock BUHID = new UnicodeBlock("BUHID");
        public static final UnicodeBlock BYZANTINE_MUSICAL_SYMBOLS = new UnicodeBlock("BYZANTINE_MUSICAL_SYMBOLS", "BYZANTINE MUSICAL SYMBOLS", "BYZANTINEMUSICALSYMBOLS");
        public static final UnicodeBlock CARIAN = new UnicodeBlock("CARIAN");
        public static final UnicodeBlock CHAKMA = new UnicodeBlock("CHAKMA");
        public static final UnicodeBlock CHAM = new UnicodeBlock("CHAM");
        public static final UnicodeBlock CHEROKEE = new UnicodeBlock("CHEROKEE");
        public static final UnicodeBlock CJK_COMPATIBILITY = new UnicodeBlock("CJK_COMPATIBILITY", "CJK COMPATIBILITY", "CJKCOMPATIBILITY");
        public static final UnicodeBlock CJK_COMPATIBILITY_FORMS = new UnicodeBlock("CJK_COMPATIBILITY_FORMS", "CJK COMPATIBILITY FORMS", "CJKCOMPATIBILITYFORMS");
        public static final UnicodeBlock CJK_COMPATIBILITY_IDEOGRAPHS = new UnicodeBlock("CJK_COMPATIBILITY_IDEOGRAPHS", "CJK COMPATIBILITY IDEOGRAPHS", "CJKCOMPATIBILITYIDEOGRAPHS");
        public static final UnicodeBlock CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT = new UnicodeBlock("CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT", "CJK COMPATIBILITY IDEOGRAPHS SUPPLEMENT", "CJKCOMPATIBILITYIDEOGRAPHSSUPPLEMENT");
        public static final UnicodeBlock CJK_RADICALS_SUPPLEMENT = new UnicodeBlock("CJK_RADICALS_SUPPLEMENT", "CJK RADICALS SUPPLEMENT", "CJKRADICALSSUPPLEMENT");
        public static final UnicodeBlock CJK_STROKES = new UnicodeBlock("CJK_STROKES", "CJK STROKES", "CJKSTROKES");
        public static final UnicodeBlock CJK_SYMBOLS_AND_PUNCTUATION = new UnicodeBlock("CJK_SYMBOLS_AND_PUNCTUATION", "CJK SYMBOLS AND PUNCTUATION", "CJKSYMBOLSANDPUNCTUATION");
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS = new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS", "CJK UNIFIED IDEOGRAPHS", "CJKUNIFIEDIDEOGRAPHS");
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A = new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A", "CJK UNIFIED IDEOGRAPHS EXTENSION A", "CJKUNIFIEDIDEOGRAPHSEXTENSIONA");
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B = new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B", "CJK UNIFIED IDEOGRAPHS EXTENSION B", "CJKUNIFIEDIDEOGRAPHSEXTENSIONB");
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C = new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C", "CJK UNIFIED IDEOGRAPHS EXTENSION C", "CJKUNIFIEDIDEOGRAPHSEXTENSIONC");
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D = new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D", "CJK UNIFIED IDEOGRAPHS EXTENSION D", "CJKUNIFIEDIDEOGRAPHSEXTENSIOND");
        public static final UnicodeBlock COMBINING_DIACRITICAL_MARKS = new UnicodeBlock("COMBINING_DIACRITICAL_MARKS", "COMBINING DIACRITICAL MARKS", "COMBININGDIACRITICALMARKS");
        public static final UnicodeBlock COMBINING_DIACRITICAL_MARKS_SUPPLEMENT = new UnicodeBlock("COMBINING_DIACRITICAL_MARKS_SUPPLEMENT", "COMBINING DIACRITICAL MARKS SUPPLEMENT", "COMBININGDIACRITICALMARKSSUPPLEMENT");
        public static final UnicodeBlock COMBINING_HALF_MARKS = new UnicodeBlock("COMBINING_HALF_MARKS", "COMBINING HALF MARKS", "COMBININGHALFMARKS");
        public static final UnicodeBlock COMBINING_MARKS_FOR_SYMBOLS = new UnicodeBlock("COMBINING_MARKS_FOR_SYMBOLS", "COMBINING DIACRITICAL MARKS FOR SYMBOLS", "COMBININGDIACRITICALMARKSFORSYMBOLS", "COMBINING MARKS FOR SYMBOLS", "COMBININGMARKSFORSYMBOLS");
        public static final UnicodeBlock COMMON_INDIC_NUMBER_FORMS = new UnicodeBlock("COMMON_INDIC_NUMBER_FORMS", "COMMON INDIC NUMBER FORMS", "COMMONINDICNUMBERFORMS");
        public static final UnicodeBlock CONTROL_PICTURES = new UnicodeBlock("CONTROL_PICTURES", "CONTROL PICTURES", "CONTROLPICTURES");
        public static final UnicodeBlock COPTIC = new UnicodeBlock("COPTIC");
        public static final UnicodeBlock COUNTING_ROD_NUMERALS = new UnicodeBlock("COUNTING_ROD_NUMERALS", "COUNTING ROD NUMERALS", "COUNTINGRODNUMERALS");
        public static final UnicodeBlock CUNEIFORM = new UnicodeBlock("CUNEIFORM");
        public static final UnicodeBlock CUNEIFORM_NUMBERS_AND_PUNCTUATION = new UnicodeBlock("CUNEIFORM_NUMBERS_AND_PUNCTUATION", "CUNEIFORM NUMBERS AND PUNCTUATION", "CUNEIFORMNUMBERSANDPUNCTUATION");
        public static final UnicodeBlock CURRENCY_SYMBOLS = new UnicodeBlock("CURRENCY_SYMBOLS", "CURRENCY SYMBOLS", "CURRENCYSYMBOLS");
        public static final UnicodeBlock CYPRIOT_SYLLABARY = new UnicodeBlock("CYPRIOT_SYLLABARY", "CYPRIOT SYLLABARY", "CYPRIOTSYLLABARY");
        public static final UnicodeBlock CYRILLIC = new UnicodeBlock("CYRILLIC");
        public static final UnicodeBlock CYRILLIC_EXTENDED_A = new UnicodeBlock("CYRILLIC_EXTENDED_A", "CYRILLIC EXTENDED-A", "CYRILLICEXTENDED-A");
        public static final UnicodeBlock CYRILLIC_EXTENDED_B = new UnicodeBlock("CYRILLIC_EXTENDED_B", "CYRILLIC EXTENDED-B", "CYRILLICEXTENDED-B");
        public static final UnicodeBlock CYRILLIC_SUPPLEMENTARY = new UnicodeBlock("CYRILLIC_SUPPLEMENTARY", "CYRILLIC SUPPLEMENTARY", "CYRILLICSUPPLEMENTARY", "CYRILLIC SUPPLEMENT", "CYRILLICSUPPLEMENT");
        public static final UnicodeBlock DESERET = new UnicodeBlock("DESERET");
        public static final UnicodeBlock DEVANAGARI = new UnicodeBlock("DEVANAGARI");
        public static final UnicodeBlock DEVANAGARI_EXTENDED = new UnicodeBlock("DEVANAGARI_EXTENDED", "DEVANAGARI EXTENDED", "DEVANAGARIEXTENDED");
        public static final UnicodeBlock DINGBATS = new UnicodeBlock("DINGBATS");
        public static final UnicodeBlock DOMINO_TILES = new UnicodeBlock("DOMINO_TILES", "DOMINO TILES", "DOMINOTILES");
        public static final UnicodeBlock EGYPTIAN_HIEROGLYPHS = new UnicodeBlock("EGYPTIAN_HIEROGLYPHS", "EGYPTIAN HIEROGLYPHS", "EGYPTIANHIEROGLYPHS");
        public static final UnicodeBlock EMOTICONS = new UnicodeBlock("EMOTICONS");
        public static final UnicodeBlock ENCLOSED_ALPHANUMERICS = new UnicodeBlock("ENCLOSED_ALPHANUMERICS", "ENCLOSED ALPHANUMERICS", "ENCLOSEDALPHANUMERICS");
        public static final UnicodeBlock ENCLOSED_ALPHANUMERIC_SUPPLEMENT = new UnicodeBlock("ENCLOSED_ALPHANUMERIC_SUPPLEMENT", "ENCLOSED ALPHANUMERIC SUPPLEMENT", "ENCLOSEDALPHANUMERICSUPPLEMENT");
        public static final UnicodeBlock ENCLOSED_CJK_LETTERS_AND_MONTHS = new UnicodeBlock("ENCLOSED_CJK_LETTERS_AND_MONTHS", "ENCLOSED CJK LETTERS AND MONTHS", "ENCLOSEDCJKLETTERSANDMONTHS");
        public static final UnicodeBlock ENCLOSED_IDEOGRAPHIC_SUPPLEMENT = new UnicodeBlock("ENCLOSED_IDEOGRAPHIC_SUPPLEMENT", "ENCLOSED IDEOGRAPHIC SUPPLEMENT", "ENCLOSEDIDEOGRAPHICSUPPLEMENT");
        public static final UnicodeBlock ETHIOPIC = new UnicodeBlock("ETHIOPIC");
        public static final UnicodeBlock ETHIOPIC_EXTENDED = new UnicodeBlock("ETHIOPIC_EXTENDED", "ETHIOPIC EXTENDED", "ETHIOPICEXTENDED");
        public static final UnicodeBlock ETHIOPIC_EXTENDED_A = new UnicodeBlock("ETHIOPIC_EXTENDED_A", "ETHIOPIC EXTENDED-A", "ETHIOPICEXTENDED-A");
        public static final UnicodeBlock ETHIOPIC_SUPPLEMENT = new UnicodeBlock("ETHIOPIC_SUPPLEMENT", "ETHIOPIC SUPPLEMENT", "ETHIOPICSUPPLEMENT");
        public static final UnicodeBlock GENERAL_PUNCTUATION = new UnicodeBlock("GENERAL_PUNCTUATION", "GENERAL PUNCTUATION", "GENERALPUNCTUATION");
        public static final UnicodeBlock GEOMETRIC_SHAPES = new UnicodeBlock("GEOMETRIC_SHAPES", "GEOMETRIC SHAPES", "GEOMETRICSHAPES");
        public static final UnicodeBlock GEORGIAN = new UnicodeBlock("GEORGIAN");
        public static final UnicodeBlock GEORGIAN_SUPPLEMENT = new UnicodeBlock("GEORGIAN_SUPPLEMENT", "GEORGIAN SUPPLEMENT", "GEORGIANSUPPLEMENT");
        public static final UnicodeBlock GLAGOLITIC = new UnicodeBlock("GLAGOLITIC");
        public static final UnicodeBlock GOTHIC = new UnicodeBlock("GOTHIC");
        public static final UnicodeBlock GREEK = new UnicodeBlock("GREEK", "GREEK AND COPTIC", "GREEKANDCOPTIC");
        public static final UnicodeBlock GREEK_EXTENDED = new UnicodeBlock("GREEK_EXTENDED", "GREEK EXTENDED", "GREEKEXTENDED");
        public static final UnicodeBlock GUJARATI = new UnicodeBlock("GUJARATI");
        public static final UnicodeBlock GURMUKHI = new UnicodeBlock("GURMUKHI");
        public static final UnicodeBlock HALFWIDTH_AND_FULLWIDTH_FORMS = new UnicodeBlock("HALFWIDTH_AND_FULLWIDTH_FORMS", "HALFWIDTH AND FULLWIDTH FORMS", "HALFWIDTHANDFULLWIDTHFORMS");
        public static final UnicodeBlock HANGUL_COMPATIBILITY_JAMO = new UnicodeBlock("HANGUL_COMPATIBILITY_JAMO", "HANGUL COMPATIBILITY JAMO", "HANGULCOMPATIBILITYJAMO");
        public static final UnicodeBlock HANGUL_JAMO = new UnicodeBlock("HANGUL_JAMO", "HANGUL JAMO", "HANGULJAMO");
        public static final UnicodeBlock HANGUL_JAMO_EXTENDED_A = new UnicodeBlock("HANGUL_JAMO_EXTENDED_A", "HANGUL JAMO EXTENDED-A", "HANGULJAMOEXTENDED-A");
        public static final UnicodeBlock HANGUL_JAMO_EXTENDED_B = new UnicodeBlock("HANGUL_JAMO_EXTENDED_B", "HANGUL JAMO EXTENDED-B", "HANGULJAMOEXTENDED-B");
        public static final UnicodeBlock HANGUL_SYLLABLES = new UnicodeBlock("HANGUL_SYLLABLES", "HANGUL SYLLABLES", "HANGULSYLLABLES");
        public static final UnicodeBlock HANUNOO = new UnicodeBlock("HANUNOO");
        public static final UnicodeBlock HEBREW = new UnicodeBlock("HEBREW");
        public static final UnicodeBlock HIGH_PRIVATE_USE_SURROGATES = new UnicodeBlock("HIGH_PRIVATE_USE_SURROGATES", "HIGH PRIVATE USE SURROGATES", "HIGHPRIVATEUSESURROGATES");
        public static final UnicodeBlock HIGH_SURROGATES = new UnicodeBlock("HIGH_SURROGATES", "HIGH SURROGATES", "HIGHSURROGATES");
        public static final UnicodeBlock HIRAGANA = new UnicodeBlock("HIRAGANA");
        public static final UnicodeBlock IDEOGRAPHIC_DESCRIPTION_CHARACTERS = new UnicodeBlock("IDEOGRAPHIC_DESCRIPTION_CHARACTERS", "IDEOGRAPHIC DESCRIPTION CHARACTERS", "IDEOGRAPHICDESCRIPTIONCHARACTERS");
        public static final UnicodeBlock IMPERIAL_ARAMAIC = new UnicodeBlock("IMPERIAL_ARAMAIC", "IMPERIAL ARAMAIC", "IMPERIALARAMAIC");
        public static final UnicodeBlock INSCRIPTIONAL_PAHLAVI = new UnicodeBlock("INSCRIPTIONAL_PAHLAVI", "INSCRIPTIONAL PAHLAVI", "INSCRIPTIONALPAHLAVI");
        public static final UnicodeBlock INSCRIPTIONAL_PARTHIAN = new UnicodeBlock("INSCRIPTIONAL_PARTHIAN", "INSCRIPTIONAL PARTHIAN", "INSCRIPTIONALPARTHIAN");
        public static final UnicodeBlock IPA_EXTENSIONS = new UnicodeBlock("IPA_EXTENSIONS", "IPA EXTENSIONS", "IPAEXTENSIONS");
        public static final UnicodeBlock JAVANESE = new UnicodeBlock("JAVANESE");
        public static final UnicodeBlock KAITHI = new UnicodeBlock("KAITHI");
        public static final UnicodeBlock KANA_SUPPLEMENT = new UnicodeBlock("KANA_SUPPLEMENT", "KANA SUPPLEMENT", "KANASUPPLEMENT");
        public static final UnicodeBlock KANBUN = new UnicodeBlock("KANBUN");
        public static final UnicodeBlock KANGXI_RADICALS = new UnicodeBlock("KANGXI_RADICALS", "KANGXI RADICALS", "KANGXIRADICALS");
        public static final UnicodeBlock KANNADA = new UnicodeBlock("KANNADA");
        public static final UnicodeBlock KATAKANA = new UnicodeBlock("KATAKANA");
        public static final UnicodeBlock KATAKANA_PHONETIC_EXTENSIONS = new UnicodeBlock("KATAKANA_PHONETIC_EXTENSIONS", "KATAKANA PHONETIC EXTENSIONS", "KATAKANAPHONETICEXTENSIONS");
        public static final UnicodeBlock KAYAH_LI = new UnicodeBlock("KAYAH_LI", "KAYAH LI", "KAYAHLI");
        public static final UnicodeBlock KHAROSHTHI = new UnicodeBlock("KHAROSHTHI");
        public static final UnicodeBlock KHMER = new UnicodeBlock("KHMER");
        public static final UnicodeBlock KHMER_SYMBOLS = new UnicodeBlock("KHMER_SYMBOLS", "KHMER SYMBOLS", "KHMERSYMBOLS");
        public static final UnicodeBlock LAO = new UnicodeBlock("LAO");
        public static final UnicodeBlock LATIN_1_SUPPLEMENT = new UnicodeBlock("LATIN_1_SUPPLEMENT", "LATIN-1 SUPPLEMENT", "LATIN-1SUPPLEMENT");
        public static final UnicodeBlock LATIN_EXTENDED_A = new UnicodeBlock("LATIN_EXTENDED_A", "LATIN EXTENDED-A", "LATINEXTENDED-A");
        public static final UnicodeBlock LATIN_EXTENDED_ADDITIONAL = new UnicodeBlock("LATIN_EXTENDED_ADDITIONAL", "LATIN EXTENDED ADDITIONAL", "LATINEXTENDEDADDITIONAL");
        public static final UnicodeBlock LATIN_EXTENDED_B = new UnicodeBlock("LATIN_EXTENDED_B", "LATIN EXTENDED-B", "LATINEXTENDED-B");
        public static final UnicodeBlock LATIN_EXTENDED_C = new UnicodeBlock("LATIN_EXTENDED_C", "LATIN EXTENDED-C", "LATINEXTENDED-C");
        public static final UnicodeBlock LATIN_EXTENDED_D = new UnicodeBlock("LATIN_EXTENDED_D", "LATIN EXTENDED-D", "LATINEXTENDED-D");
        public static final UnicodeBlock LEPCHA = new UnicodeBlock("LEPCHA");
        public static final UnicodeBlock LETTERLIKE_SYMBOLS = new UnicodeBlock("LETTERLIKE_SYMBOLS", "LETTERLIKE SYMBOLS", "LETTERLIKESYMBOLS");
        public static final UnicodeBlock LIMBU = new UnicodeBlock("LIMBU");
        public static final UnicodeBlock LINEAR_B_IDEOGRAMS = new UnicodeBlock("LINEAR_B_IDEOGRAMS", "LINEAR B IDEOGRAMS", "LINEARBIDEOGRAMS");
        public static final UnicodeBlock LINEAR_B_SYLLABARY = new UnicodeBlock("LINEAR_B_SYLLABARY", "LINEAR B SYLLABARY", "LINEARBSYLLABARY");
        public static final UnicodeBlock LISU = new UnicodeBlock("LISU");
        public static final UnicodeBlock LOW_SURROGATES = new UnicodeBlock("LOW_SURROGATES", "LOW SURROGATES", "LOWSURROGATES");
        public static final UnicodeBlock LYCIAN = new UnicodeBlock("LYCIAN");
        public static final UnicodeBlock LYDIAN = new UnicodeBlock("LYDIAN");
        public static final UnicodeBlock MAHJONG_TILES = new UnicodeBlock("MAHJONG_TILES", "MAHJONG TILES", "MAHJONGTILES");
        public static final UnicodeBlock MALAYALAM = new UnicodeBlock("MALAYALAM");
        public static final UnicodeBlock MANDAIC = new UnicodeBlock("MANDAIC");
        public static final UnicodeBlock MATHEMATICAL_ALPHANUMERIC_SYMBOLS = new UnicodeBlock("MATHEMATICAL_ALPHANUMERIC_SYMBOLS", "MATHEMATICAL ALPHANUMERIC SYMBOLS", "MATHEMATICALALPHANUMERICSYMBOLS");
        public static final UnicodeBlock MATHEMATICAL_OPERATORS = new UnicodeBlock("MATHEMATICAL_OPERATORS", "MATHEMATICAL OPERATORS", "MATHEMATICALOPERATORS");
        public static final UnicodeBlock MEETEI_MAYEK = new UnicodeBlock("MEETEI_MAYEK", "MEETEI MAYEK", "MEETEIMAYEK");
        public static final UnicodeBlock MEETEI_MAYEK_EXTENSIONS = new UnicodeBlock("MEETEI_MAYEK_EXTENSIONS", "MEETEI MAYEK EXTENSIONS", "MEETEIMAYEKEXTENSIONS");
        public static final UnicodeBlock MEROITIC_CURSIVE = new UnicodeBlock("MEROITIC_CURSIVE", "MEROITIC CURSIVE", "MEROITICCURSIVE");
        public static final UnicodeBlock MEROITIC_HIEROGLYPHS = new UnicodeBlock("MEROITIC_HIEROGLYPHS", "MEROITIC HIEROGLYPHS", "MEROITICHIEROGLYPHS");
        public static final UnicodeBlock MIAO = new UnicodeBlock("MIAO");
        public static final UnicodeBlock MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A = new UnicodeBlock("MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A", "MISCELLANEOUS MATHEMATICAL SYMBOLS-A", "MISCELLANEOUSMATHEMATICALSYMBOLS-A");
        public static final UnicodeBlock MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B = new UnicodeBlock("MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B", "MISCELLANEOUS MATHEMATICAL SYMBOLS-B", "MISCELLANEOUSMATHEMATICALSYMBOLS-B");
        public static final UnicodeBlock MISCELLANEOUS_SYMBOLS = new UnicodeBlock("MISCELLANEOUS_SYMBOLS", "MISCELLANEOUS SYMBOLS", "MISCELLANEOUSSYMBOLS");
        public static final UnicodeBlock MISCELLANEOUS_SYMBOLS_AND_ARROWS = new UnicodeBlock("MISCELLANEOUS_SYMBOLS_AND_ARROWS", "MISCELLANEOUS SYMBOLS AND ARROWS", "MISCELLANEOUSSYMBOLSANDARROWS");
        public static final UnicodeBlock MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS = new UnicodeBlock("MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS", "MISCELLANEOUS SYMBOLS AND PICTOGRAPHS", "MISCELLANEOUSSYMBOLSANDPICTOGRAPHS");
        public static final UnicodeBlock MISCELLANEOUS_TECHNICAL = new UnicodeBlock("MISCELLANEOUS_TECHNICAL", "MISCELLANEOUS TECHNICAL", "MISCELLANEOUSTECHNICAL");
        public static final UnicodeBlock MODIFIER_TONE_LETTERS = new UnicodeBlock("MODIFIER_TONE_LETTERS", "MODIFIER TONE LETTERS", "MODIFIERTONELETTERS");
        public static final UnicodeBlock MONGOLIAN = new UnicodeBlock("MONGOLIAN");
        public static final UnicodeBlock MUSICAL_SYMBOLS = new UnicodeBlock("MUSICAL_SYMBOLS", "MUSICAL SYMBOLS", "MUSICALSYMBOLS");
        public static final UnicodeBlock MYANMAR = new UnicodeBlock("MYANMAR");
        public static final UnicodeBlock MYANMAR_EXTENDED_A = new UnicodeBlock("MYANMAR_EXTENDED_A", "MYANMAR EXTENDED-A", "MYANMAREXTENDED-A");
        public static final UnicodeBlock NEW_TAI_LUE = new UnicodeBlock("NEW_TAI_LUE", "NEW TAI LUE", "NEWTAILUE");
        public static final UnicodeBlock NKO = new UnicodeBlock("NKO");
        public static final UnicodeBlock NUMBER_FORMS = new UnicodeBlock("NUMBER_FORMS", "NUMBER FORMS", "NUMBERFORMS");
        public static final UnicodeBlock OGHAM = new UnicodeBlock("OGHAM");
        public static final UnicodeBlock OLD_ITALIC = new UnicodeBlock("OLD_ITALIC", "OLD ITALIC", "OLDITALIC");
        public static final UnicodeBlock OLD_PERSIAN = new UnicodeBlock("OLD_PERSIAN", "OLD PERSIAN", "OLDPERSIAN");
        public static final UnicodeBlock OLD_SOUTH_ARABIAN = new UnicodeBlock("OLD_SOUTH_ARABIAN", "OLD SOUTH ARABIAN", "OLDSOUTHARABIAN");
        public static final UnicodeBlock OLD_TURKIC = new UnicodeBlock("OLD_TURKIC", "OLD TURKIC", "OLDTURKIC");
        public static final UnicodeBlock OL_CHIKI = new UnicodeBlock("OL_CHIKI", "OL CHIKI", "OLCHIKI");
        public static final UnicodeBlock OPTICAL_CHARACTER_RECOGNITION = new UnicodeBlock("OPTICAL_CHARACTER_RECOGNITION", "OPTICAL CHARACTER RECOGNITION", "OPTICALCHARACTERRECOGNITION");
        public static final UnicodeBlock ORIYA = new UnicodeBlock("ORIYA");
        public static final UnicodeBlock OSMANYA = new UnicodeBlock("OSMANYA");
        public static final UnicodeBlock PHAGS_PA = new UnicodeBlock("PHAGS_PA", "PHAGS-PA");
        public static final UnicodeBlock PHAISTOS_DISC = new UnicodeBlock("PHAISTOS_DISC", "PHAISTOS DISC", "PHAISTOSDISC");
        public static final UnicodeBlock PHOENICIAN = new UnicodeBlock("PHOENICIAN");
        public static final UnicodeBlock PHONETIC_EXTENSIONS = new UnicodeBlock("PHONETIC_EXTENSIONS", "PHONETIC EXTENSIONS", "PHONETICEXTENSIONS");
        public static final UnicodeBlock PHONETIC_EXTENSIONS_SUPPLEMENT = new UnicodeBlock("PHONETIC_EXTENSIONS_SUPPLEMENT", "PHONETIC EXTENSIONS SUPPLEMENT", "PHONETICEXTENSIONSSUPPLEMENT");
        public static final UnicodeBlock PLAYING_CARDS = new UnicodeBlock("PLAYING_CARDS", "PLAYING CARDS", "PLAYINGCARDS");
        public static final UnicodeBlock PRIVATE_USE_AREA = new UnicodeBlock("PRIVATE_USE_AREA", "PRIVATE USE AREA", "PRIVATEUSEAREA");
        public static final UnicodeBlock REJANG = new UnicodeBlock("REJANG");
        public static final UnicodeBlock RUMI_NUMERAL_SYMBOLS = new UnicodeBlock("RUMI_NUMERAL_SYMBOLS", "RUMI NUMERAL SYMBOLS", "RUMINUMERALSYMBOLS");
        public static final UnicodeBlock RUNIC = new UnicodeBlock("RUNIC");
        public static final UnicodeBlock SAMARITAN = new UnicodeBlock("SAMARITAN");
        public static final UnicodeBlock SAURASHTRA = new UnicodeBlock("SAURASHTRA");
        public static final UnicodeBlock SHARADA = new UnicodeBlock("SHARADA");
        public static final UnicodeBlock SHAVIAN = new UnicodeBlock("SHAVIAN");
        public static final UnicodeBlock SINHALA = new UnicodeBlock("SINHALA");
        public static final UnicodeBlock SMALL_FORM_VARIANTS = new UnicodeBlock("SMALL_FORM_VARIANTS", "SMALL FORM VARIANTS", "SMALLFORMVARIANTS");
        public static final UnicodeBlock SORA_SOMPENG = new UnicodeBlock("SORA_SOMPENG", "SORA SOMPENG", "SORASOMPENG");
        public static final UnicodeBlock SPACING_MODIFIER_LETTERS = new UnicodeBlock("SPACING_MODIFIER_LETTERS", "SPACING MODIFIER LETTERS", "SPACINGMODIFIERLETTERS");
        public static final UnicodeBlock SPECIALS = new UnicodeBlock("SPECIALS");
        public static final UnicodeBlock SUNDANESE = new UnicodeBlock("SUNDANESE");
        public static final UnicodeBlock SUNDANESE_SUPPLEMENT = new UnicodeBlock("SUNDANESE_SUPPLEMENT", "SUNDANESE SUPPLEMENT", "SUNDANESESUPPLEMENT");
        public static final UnicodeBlock SUPERSCRIPTS_AND_SUBSCRIPTS = new UnicodeBlock("SUPERSCRIPTS_AND_SUBSCRIPTS", "SUPERSCRIPTS AND SUBSCRIPTS", "SUPERSCRIPTSANDSUBSCRIPTS");
        public static final UnicodeBlock SUPPLEMENTAL_ARROWS_A = new UnicodeBlock("SUPPLEMENTAL_ARROWS_A", "SUPPLEMENTAL ARROWS-A", "SUPPLEMENTALARROWS-A");
        public static final UnicodeBlock SUPPLEMENTAL_ARROWS_B = new UnicodeBlock("SUPPLEMENTAL_ARROWS_B", "SUPPLEMENTAL ARROWS-B", "SUPPLEMENTALARROWS-B");
        public static final UnicodeBlock SUPPLEMENTAL_MATHEMATICAL_OPERATORS = new UnicodeBlock("SUPPLEMENTAL_MATHEMATICAL_OPERATORS", "SUPPLEMENTAL MATHEMATICAL OPERATORS", "SUPPLEMENTALMATHEMATICALOPERATORS");
        public static final UnicodeBlock SUPPLEMENTAL_PUNCTUATION = new UnicodeBlock("SUPPLEMENTAL_PUNCTUATION", "SUPPLEMENTAL PUNCTUATION", "SUPPLEMENTALPUNCTUATION");
        public static final UnicodeBlock SUPPLEMENTARY_PRIVATE_USE_AREA_A = new UnicodeBlock("SUPPLEMENTARY_PRIVATE_USE_AREA_A", "SUPPLEMENTARY PRIVATE USE AREA-A", "SUPPLEMENTARYPRIVATEUSEAREA-A");
        public static final UnicodeBlock SUPPLEMENTARY_PRIVATE_USE_AREA_B = new UnicodeBlock("SUPPLEMENTARY_PRIVATE_USE_AREA_B", "SUPPLEMENTARY PRIVATE USE AREA-B", "SUPPLEMENTARYPRIVATEUSEAREA-B");
        @Deprecated
        public static final UnicodeBlock SURROGATES_AREA = new UnicodeBlock("SURROGATES_AREA", false);
        public static final UnicodeBlock SYLOTI_NAGRI = new UnicodeBlock("SYLOTI_NAGRI", "SYLOTI NAGRI", "SYLOTINAGRI");
        public static final UnicodeBlock SYRIAC = new UnicodeBlock("SYRIAC");
        public static final UnicodeBlock TAGALOG = new UnicodeBlock("TAGALOG");
        public static final UnicodeBlock TAGBANWA = new UnicodeBlock("TAGBANWA");
        public static final UnicodeBlock TAGS = new UnicodeBlock("TAGS");
        public static final UnicodeBlock TAI_LE = new UnicodeBlock("TAI_LE", "TAI LE", "TAILE");
        public static final UnicodeBlock TAI_THAM = new UnicodeBlock("TAI_THAM", "TAI THAM", "TAITHAM");
        public static final UnicodeBlock TAI_VIET = new UnicodeBlock("TAI_VIET", "TAI VIET", "TAIVIET");
        public static final UnicodeBlock TAI_XUAN_JING_SYMBOLS = new UnicodeBlock("TAI_XUAN_JING_SYMBOLS", "TAI XUAN JING SYMBOLS", "TAIXUANJINGSYMBOLS");
        public static final UnicodeBlock TAKRI = new UnicodeBlock("TAKRI");
        public static final UnicodeBlock TAMIL = new UnicodeBlock("TAMIL");
        public static final UnicodeBlock TELUGU = new UnicodeBlock("TELUGU");
        public static final UnicodeBlock THAANA = new UnicodeBlock("THAANA");
        public static final UnicodeBlock THAI = new UnicodeBlock("THAI");
        public static final UnicodeBlock TIBETAN = new UnicodeBlock("TIBETAN");
        public static final UnicodeBlock TIFINAGH = new UnicodeBlock("TIFINAGH");
        public static final UnicodeBlock TRANSPORT_AND_MAP_SYMBOLS = new UnicodeBlock("TRANSPORT_AND_MAP_SYMBOLS", "TRANSPORT AND MAP SYMBOLS", "TRANSPORTANDMAPSYMBOLS");
        public static final UnicodeBlock UGARITIC = new UnicodeBlock("UGARITIC");
        public static final UnicodeBlock UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS = new UnicodeBlock("UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS", "UNIFIED CANADIAN ABORIGINAL SYLLABICS", "UNIFIEDCANADIANABORIGINALSYLLABICS");
        public static final UnicodeBlock UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_EXTENDED = new UnicodeBlock("UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_EXTENDED", "UNIFIED CANADIAN ABORIGINAL SYLLABICS EXTENDED", "UNIFIEDCANADIANABORIGINALSYLLABICSEXTENDED");
        public static final UnicodeBlock VAI = new UnicodeBlock("VAI");
        public static final UnicodeBlock VARIATION_SELECTORS = new UnicodeBlock("VARIATION_SELECTORS", "VARIATION SELECTORS", "VARIATIONSELECTORS");
        public static final UnicodeBlock VARIATION_SELECTORS_SUPPLEMENT = new UnicodeBlock("VARIATION_SELECTORS_SUPPLEMENT", "VARIATION SELECTORS SUPPLEMENT", "VARIATIONSELECTORSSUPPLEMENT");
        public static final UnicodeBlock VEDIC_EXTENSIONS = new UnicodeBlock("VEDIC_EXTENSIONS", "VEDIC EXTENSIONS", "VEDICEXTENSIONS");
        public static final UnicodeBlock VERTICAL_FORMS = new UnicodeBlock("VERTICAL_FORMS", "VERTICAL FORMS", "VERTICALFORMS");
        public static final UnicodeBlock YIJING_HEXAGRAM_SYMBOLS = new UnicodeBlock("YIJING_HEXAGRAM_SYMBOLS", "YIJING HEXAGRAM SYMBOLS", "YIJINGHEXAGRAMSYMBOLS");
        public static final UnicodeBlock YI_RADICALS = new UnicodeBlock("YI_RADICALS", "YI RADICALS", "YIRADICALS");
        public static final UnicodeBlock YI_SYLLABLES = new UnicodeBlock("YI_SYLLABLES", "YI SYLLABLES", "YISYLLABLES");
        private static final int[] blockStarts = new int[]{0, 128, 256, 384, 592, 688, 768, 880, 1024, 1280, 1328, 1424, 1536, 1792, 1872, 1920, 1984, 2048, 2112, 2144, 2208, 2304, 2432, 2560, 2688, 2816, 2944, 3072, 3200, 3328, 3456, 3584, 3712, 3840, 4096, 4256, 4352, 4608, 4992, 5024, 5120, 5760, 5792, 5888, 5920, 5952, 5984, 6016, 6144, 6320, 6400, 6480, 6528, 6624, 6656, 6688, 6832, 6912, 7040, 7104, 7168, 7248, 7296, 7360, 7376, 7424, 7552, 7616, 7680, 7936, 8192, 8304, 8352, 8400, 8448, 8528, 8592, 8704, 8960, 9216, 9280, 9312, 9472, 9600, 9632, 9728, 9984, 10176, 10224, 10240, 10496, 10624, 10752, 11008, 11264, 11360, 11392, 11520, 11568, 11648, 11744, 11776, 11904, 12032, 12256, 12272, 12288, 12352, 12448, 12544, 12592, 12688, 12704, 12736, 12784, 12800, 13056, 13312, 19904, 19968, 40960, 42128, 42192, 42240, 42560, 42656, 42752, 42784, 43008, 43056, 43072, 43136, 43232, 43264, 43312, 43360, 43392, 43488, 43520, 43616, 43648, 43744, 43776, 43824, 43968, 44032, 55216, 55296, 56192, 56320, 57344, 63744, 64256, 64336, 65024, 65040, 65056, 65072, 65104, 65136, 65280, 65520, 65536, 65664, 65792, 65856, 65936, 66000, 66048, 66176, 66208, 66272, 66304, 66352, 66384, 66432, 66464, 66528, 66560, 66640, 66688, 66736, 67584, 67648, 67680, 67840, 67872, 67904, 67968, 68000, 68096, 68192, 68224, 68352, 68416, 68448, 68480, 68608, 68688, 69216, 69248, 69632, 69760, 69840, 69888, 69968, 70016, 70112, 71296, 71376, 73728, 74752, 74880, 77824, 78896, 92160, 92736, 93952, 94112, 110592, 110848, 118784, 119040, 119296, 119376, 119552, 119648, 119680, 119808, 120832, 126464, 126720, 126976, 127024, 127136, 127232, 127488, 127744, 128512, 128592, 128640, 128768, 128896, NumericShaper.KHMER, 173792, 173824, 177984, 178208, 194560, 195104, 917504, 917632, 917760, 918000, 983040, 1048576};
        private static final UnicodeBlock[] blocks = new UnicodeBlock[]{BASIC_LATIN, LATIN_1_SUPPLEMENT, LATIN_EXTENDED_A, LATIN_EXTENDED_B, IPA_EXTENSIONS, SPACING_MODIFIER_LETTERS, COMBINING_DIACRITICAL_MARKS, GREEK, CYRILLIC, CYRILLIC_SUPPLEMENTARY, ARMENIAN, HEBREW, ARABIC, SYRIAC, ARABIC_SUPPLEMENT, THAANA, NKO, SAMARITAN, MANDAIC, null, ARABIC_EXTENDED_A, DEVANAGARI, BENGALI, GURMUKHI, GUJARATI, ORIYA, TAMIL, TELUGU, KANNADA, MALAYALAM, SINHALA, THAI, LAO, TIBETAN, MYANMAR, GEORGIAN, HANGUL_JAMO, ETHIOPIC, ETHIOPIC_SUPPLEMENT, CHEROKEE, UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS, OGHAM, RUNIC, TAGALOG, HANUNOO, BUHID, TAGBANWA, KHMER, MONGOLIAN, UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_EXTENDED, LIMBU, TAI_LE, NEW_TAI_LUE, KHMER_SYMBOLS, BUGINESE, TAI_THAM, null, BALINESE, SUNDANESE, BATAK, LEPCHA, OL_CHIKI, null, SUNDANESE_SUPPLEMENT, VEDIC_EXTENSIONS, PHONETIC_EXTENSIONS, PHONETIC_EXTENSIONS_SUPPLEMENT, COMBINING_DIACRITICAL_MARKS_SUPPLEMENT, LATIN_EXTENDED_ADDITIONAL, GREEK_EXTENDED, GENERAL_PUNCTUATION, SUPERSCRIPTS_AND_SUBSCRIPTS, CURRENCY_SYMBOLS, COMBINING_MARKS_FOR_SYMBOLS, LETTERLIKE_SYMBOLS, NUMBER_FORMS, ARROWS, MATHEMATICAL_OPERATORS, MISCELLANEOUS_TECHNICAL, CONTROL_PICTURES, OPTICAL_CHARACTER_RECOGNITION, ENCLOSED_ALPHANUMERICS, BOX_DRAWING, BLOCK_ELEMENTS, GEOMETRIC_SHAPES, MISCELLANEOUS_SYMBOLS, DINGBATS, MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A, SUPPLEMENTAL_ARROWS_A, BRAILLE_PATTERNS, SUPPLEMENTAL_ARROWS_B, MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B, SUPPLEMENTAL_MATHEMATICAL_OPERATORS, MISCELLANEOUS_SYMBOLS_AND_ARROWS, GLAGOLITIC, LATIN_EXTENDED_C, COPTIC, GEORGIAN_SUPPLEMENT, TIFINAGH, ETHIOPIC_EXTENDED, CYRILLIC_EXTENDED_A, SUPPLEMENTAL_PUNCTUATION, CJK_RADICALS_SUPPLEMENT, KANGXI_RADICALS, null, IDEOGRAPHIC_DESCRIPTION_CHARACTERS, CJK_SYMBOLS_AND_PUNCTUATION, HIRAGANA, KATAKANA, BOPOMOFO, HANGUL_COMPATIBILITY_JAMO, KANBUN, BOPOMOFO_EXTENDED, CJK_STROKES, KATAKANA_PHONETIC_EXTENSIONS, ENCLOSED_CJK_LETTERS_AND_MONTHS, CJK_COMPATIBILITY, CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A, YIJING_HEXAGRAM_SYMBOLS, CJK_UNIFIED_IDEOGRAPHS, YI_SYLLABLES, YI_RADICALS, LISU, VAI, CYRILLIC_EXTENDED_B, BAMUM, MODIFIER_TONE_LETTERS, LATIN_EXTENDED_D, SYLOTI_NAGRI, COMMON_INDIC_NUMBER_FORMS, PHAGS_PA, SAURASHTRA, DEVANAGARI_EXTENDED, KAYAH_LI, REJANG, HANGUL_JAMO_EXTENDED_A, JAVANESE, null, CHAM, MYANMAR_EXTENDED_A, TAI_VIET, MEETEI_MAYEK_EXTENSIONS, ETHIOPIC_EXTENDED_A, null, MEETEI_MAYEK, HANGUL_SYLLABLES, HANGUL_JAMO_EXTENDED_B, HIGH_SURROGATES, HIGH_PRIVATE_USE_SURROGATES, LOW_SURROGATES, PRIVATE_USE_AREA, CJK_COMPATIBILITY_IDEOGRAPHS, ALPHABETIC_PRESENTATION_FORMS, ARABIC_PRESENTATION_FORMS_A, VARIATION_SELECTORS, VERTICAL_FORMS, COMBINING_HALF_MARKS, CJK_COMPATIBILITY_FORMS, SMALL_FORM_VARIANTS, ARABIC_PRESENTATION_FORMS_B, HALFWIDTH_AND_FULLWIDTH_FORMS, SPECIALS, LINEAR_B_SYLLABARY, LINEAR_B_IDEOGRAMS, AEGEAN_NUMBERS, ANCIENT_GREEK_NUMBERS, ANCIENT_SYMBOLS, PHAISTOS_DISC, null, LYCIAN, CARIAN, null, OLD_ITALIC, GOTHIC, null, UGARITIC, OLD_PERSIAN, null, DESERET, SHAVIAN, OSMANYA, null, CYPRIOT_SYLLABARY, IMPERIAL_ARAMAIC, null, PHOENICIAN, LYDIAN, null, MEROITIC_HIEROGLYPHS, MEROITIC_CURSIVE, KHAROSHTHI, OLD_SOUTH_ARABIAN, null, AVESTAN, INSCRIPTIONAL_PARTHIAN, INSCRIPTIONAL_PAHLAVI, null, OLD_TURKIC, null, RUMI_NUMERAL_SYMBOLS, null, BRAHMI, KAITHI, SORA_SOMPENG, CHAKMA, null, SHARADA, null, TAKRI, null, CUNEIFORM, CUNEIFORM_NUMBERS_AND_PUNCTUATION, null, EGYPTIAN_HIEROGLYPHS, null, BAMUM_SUPPLEMENT, null, MIAO, null, KANA_SUPPLEMENT, null, BYZANTINE_MUSICAL_SYMBOLS, MUSICAL_SYMBOLS, ANCIENT_GREEK_MUSICAL_NOTATION, null, TAI_XUAN_JING_SYMBOLS, COUNTING_ROD_NUMERALS, null, MATHEMATICAL_ALPHANUMERIC_SYMBOLS, null, ARABIC_MATHEMATICAL_ALPHABETIC_SYMBOLS, null, MAHJONG_TILES, DOMINO_TILES, PLAYING_CARDS, ENCLOSED_ALPHANUMERIC_SUPPLEMENT, ENCLOSED_IDEOGRAPHIC_SUPPLEMENT, MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS, EMOTICONS, null, TRANSPORT_AND_MAP_SYMBOLS, ALCHEMICAL_SYMBOLS, null, CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B, null, CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C, CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D, null, CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT, null, TAGS, null, VARIATION_SELECTORS_SUPPLEMENT, null, SUPPLEMENTARY_PRIVATE_USE_AREA_A, SUPPLEMENTARY_PRIVATE_USE_AREA_B};
        private static Map<String, UnicodeBlock> map = new HashMap(256);

        private UnicodeBlock(String idName) {
            this(idName, true);
        }

        private UnicodeBlock(String idName, boolean isMap) {
            super(idName);
            if (isMap) {
                map.put(idName, this);
            }
        }

        private UnicodeBlock(String idName, String alias) {
            this(idName);
            map.put(alias, this);
        }

        private UnicodeBlock(String idName, String... aliases) {
            this(idName);
            for (String alias : aliases) {
                map.put(alias, this);
            }
        }

        public static UnicodeBlock of(char c) {
            return of((int) c);
        }

        public static UnicodeBlock of(int codePoint) {
            if (Character.isValidCodePoint(codePoint)) {
                int bottom = 0;
                int top = blockStarts.length;
                int current = top / 2;
                while (top - bottom > 1) {
                    if (codePoint >= blockStarts[current]) {
                    }
                    bottom = current;
                    current = (top + bottom) / 2;
                }
                return blocks[current];
            }
            throw new IllegalArgumentException();
        }

        public static final UnicodeBlock forName(String blockName) {
            UnicodeBlock block = (UnicodeBlock) map.get(blockName.toUpperCase(Locale.US));
            if (block != null) {
                return block;
            }
            throw new IllegalArgumentException();
        }
    }

    public enum UnicodeScript {
        COMMON,
        LATIN,
        GREEK,
        CYRILLIC,
        ARMENIAN,
        HEBREW,
        ARABIC,
        SYRIAC,
        THAANA,
        DEVANAGARI,
        BENGALI,
        GURMUKHI,
        GUJARATI,
        ORIYA,
        TAMIL,
        TELUGU,
        KANNADA,
        MALAYALAM,
        SINHALA,
        THAI,
        LAO,
        TIBETAN,
        MYANMAR,
        GEORGIAN,
        HANGUL,
        ETHIOPIC,
        CHEROKEE,
        CANADIAN_ABORIGINAL,
        OGHAM,
        RUNIC,
        KHMER,
        MONGOLIAN,
        HIRAGANA,
        KATAKANA,
        BOPOMOFO,
        HAN,
        YI,
        OLD_ITALIC,
        GOTHIC,
        DESERET,
        INHERITED,
        TAGALOG,
        HANUNOO,
        BUHID,
        TAGBANWA,
        LIMBU,
        TAI_LE,
        LINEAR_B,
        UGARITIC,
        SHAVIAN,
        OSMANYA,
        CYPRIOT,
        BRAILLE,
        BUGINESE,
        COPTIC,
        NEW_TAI_LUE,
        GLAGOLITIC,
        TIFINAGH,
        SYLOTI_NAGRI,
        OLD_PERSIAN,
        KHAROSHTHI,
        BALINESE,
        CUNEIFORM,
        PHOENICIAN,
        PHAGS_PA,
        NKO,
        SUNDANESE,
        BATAK,
        LEPCHA,
        OL_CHIKI,
        VAI,
        SAURASHTRA,
        KAYAH_LI,
        REJANG,
        LYCIAN,
        CARIAN,
        LYDIAN,
        CHAM,
        TAI_THAM,
        TAI_VIET,
        AVESTAN,
        EGYPTIAN_HIEROGLYPHS,
        SAMARITAN,
        MANDAIC,
        LISU,
        BAMUM,
        JAVANESE,
        MEETEI_MAYEK,
        IMPERIAL_ARAMAIC,
        OLD_SOUTH_ARABIAN,
        INSCRIPTIONAL_PARTHIAN,
        INSCRIPTIONAL_PAHLAVI,
        OLD_TURKIC,
        BRAHMI,
        KAITHI,
        MEROITIC_HIEROGLYPHS,
        MEROITIC_CURSIVE,
        SORA_SOMPENG,
        CHAKMA,
        SHARADA,
        TAKRI,
        MIAO,
        UNKNOWN;
        
        private static HashMap<String, UnicodeScript> aliases;
        private static final int[] scriptStarts = null;
        private static final UnicodeScript[] scripts = null;

        static {
            scriptStarts = new int[]{0, 65, 91, 97, 123, 170, 171, 186, 187, 192, 215, 216, 247, 248, 697, 736, 741, 746, 748, 768, 880, 884, 885, 894, PlatformLogger.WARNING, 901, 902, 903, 904, 994, ResultSet.CONCUR_UPDATABLE, 1024, 1157, 1159, 1329, 1417, 1418, 1425, 1536, 1548, 1549, 1563, 1566, 1567, 1568, 1600, 1601, 1611, 1622, 1632, 1642, 1648, 1649, 1757, 1758, 1792, 1872, 1920, 1984, 2048, 2112, 2208, 2304, 2385, 2387, 2404, 2406, 2433, 2561, 2689, 2817, 2946, 3073, 3202, 3330, 3458, 3585, 3647, 3648, 3713, 3840, 4053, 4057, 4096, 4256, 4347, 4348, 4352, 4608, 5024, 5120, 5760, 5792, 5867, 5870, 5888, 5920, 5941, 5952, 5984, 6016, 6144, 6146, 6148, 6149, 6150, 6320, 6400, 6480, 6528, 6624, 6656, 6688, 6912, 7040, 7104, 7168, 7248, 7360, 7376, 7379, 7380, 7393, 7394, 7401, 7405, 7406, 7412, 7413, 7424, 7462, 7467, 7468, 7517, 7522, 7526, 7531, 7544, 7545, 7615, 7616, 7680, 7936, 8192, 8204, 8206, 8305, 8308, 8319, 8320, 8336, 8352, 8400, 8448, 8486, 8487, 8490, 8492, 8498, 8499, 8526, 8527, 8544, 8585, 10240, 10496, 11264, 11360, 11392, 11520, 11568, 11648, 11744, 11776, 11904, 12272, 12293, 12294, 12295, 12296, 12321, 12330, 12334, 12336, 12344, 12348, 12353, 12441, 12443, 12445, 12448, 12449, 12539, 12541, 12549, 12593, 12688, 12704, 12736, 12784, 12800, 12832, 12896, 12927, 13008, 13144, 13312, 19904, 19968, 40960, 42192, 42240, 42560, 42656, 42752, 42786, 42888, 42891, 43008, 43056, 43072, 43136, 43232, 43264, 43312, 43360, 43392, 43520, 43616, 43648, 43744, 43777, 43968, 44032, 55292, 63744, 64256, 64275, 64285, 64336, 64830, 64848, 65021, 65024, 65040, 65056, 65072, 65136, 65279, 65313, 65339, 65345, 65371, 65382, 65392, 65393, 65438, 65440, 65504, 65536, 65792, 65856, 65936, 66045, 66176, 66208, 66304, 66352, 66432, 66464, 66560, 66640, 66688, 67584, 67648, 67840, 67872, 67968, 68000, 68096, 68192, 68352, 68416, 68448, 68608, 69216, 69632, 69760, 69840, 69888, 70016, 71296, 73728, 77824, 92160, 93952, 110592, 110593, 118784, 119143, 119146, 119163, 119171, 119173, 119180, 119210, 119214, 119296, 119552, 126464, 126976, 127488, 127489, NumericShaper.KHMER, 917505, 917760, 918000};
            scripts = new UnicodeScript[]{COMMON, LATIN, COMMON, LATIN, COMMON, LATIN, COMMON, LATIN, COMMON, LATIN, COMMON, LATIN, COMMON, LATIN, COMMON, LATIN, COMMON, BOPOMOFO, COMMON, INHERITED, GREEK, COMMON, GREEK, COMMON, GREEK, COMMON, GREEK, COMMON, GREEK, COPTIC, GREEK, CYRILLIC, INHERITED, CYRILLIC, ARMENIAN, COMMON, ARMENIAN, HEBREW, ARABIC, COMMON, ARABIC, COMMON, ARABIC, COMMON, ARABIC, COMMON, ARABIC, INHERITED, ARABIC, COMMON, ARABIC, INHERITED, ARABIC, COMMON, ARABIC, SYRIAC, ARABIC, THAANA, NKO, SAMARITAN, MANDAIC, ARABIC, DEVANAGARI, INHERITED, DEVANAGARI, COMMON, DEVANAGARI, BENGALI, GURMUKHI, GUJARATI, ORIYA, TAMIL, TELUGU, KANNADA, MALAYALAM, SINHALA, THAI, COMMON, THAI, LAO, TIBETAN, COMMON, TIBETAN, MYANMAR, GEORGIAN, COMMON, GEORGIAN, HANGUL, ETHIOPIC, CHEROKEE, CANADIAN_ABORIGINAL, OGHAM, RUNIC, COMMON, RUNIC, TAGALOG, HANUNOO, COMMON, BUHID, TAGBANWA, KHMER, MONGOLIAN, COMMON, MONGOLIAN, COMMON, MONGOLIAN, CANADIAN_ABORIGINAL, LIMBU, TAI_LE, NEW_TAI_LUE, KHMER, BUGINESE, TAI_THAM, BALINESE, SUNDANESE, BATAK, LEPCHA, OL_CHIKI, SUNDANESE, INHERITED, COMMON, INHERITED, COMMON, INHERITED, COMMON, INHERITED, COMMON, INHERITED, COMMON, LATIN, GREEK, CYRILLIC, LATIN, GREEK, LATIN, GREEK, LATIN, CYRILLIC, LATIN, GREEK, INHERITED, LATIN, GREEK, COMMON, INHERITED, COMMON, LATIN, COMMON, LATIN, COMMON, LATIN, COMMON, INHERITED, COMMON, GREEK, COMMON, LATIN, COMMON, LATIN, COMMON, LATIN, COMMON, LATIN, COMMON, BRAILLE, COMMON, GLAGOLITIC, LATIN, COPTIC, GEORGIAN, TIFINAGH, ETHIOPIC, CYRILLIC, COMMON, HAN, COMMON, HAN, COMMON, HAN, COMMON, HAN, INHERITED, HANGUL, COMMON, HAN, COMMON, HIRAGANA, INHERITED, COMMON, HIRAGANA, COMMON, KATAKANA, COMMON, KATAKANA, BOPOMOFO, HANGUL, COMMON, BOPOMOFO, COMMON, KATAKANA, HANGUL, COMMON, HANGUL, COMMON, KATAKANA, COMMON, HAN, COMMON, HAN, YI, LISU, VAI, CYRILLIC, BAMUM, COMMON, LATIN, COMMON, LATIN, SYLOTI_NAGRI, COMMON, PHAGS_PA, SAURASHTRA, DEVANAGARI, KAYAH_LI, REJANG, HANGUL, JAVANESE, CHAM, MYANMAR, TAI_VIET, MEETEI_MAYEK, ETHIOPIC, MEETEI_MAYEK, HANGUL, UNKNOWN, HAN, LATIN, ARMENIAN, HEBREW, ARABIC, COMMON, ARABIC, COMMON, INHERITED, COMMON, INHERITED, COMMON, ARABIC, COMMON, LATIN, COMMON, LATIN, COMMON, KATAKANA, COMMON, KATAKANA, COMMON, HANGUL, COMMON, LINEAR_B, COMMON, GREEK, COMMON, INHERITED, LYCIAN, CARIAN, OLD_ITALIC, GOTHIC, UGARITIC, OLD_PERSIAN, DESERET, SHAVIAN, OSMANYA, CYPRIOT, IMPERIAL_ARAMAIC, PHOENICIAN, LYDIAN, MEROITIC_HIEROGLYPHS, MEROITIC_CURSIVE, KHAROSHTHI, OLD_SOUTH_ARABIAN, AVESTAN, INSCRIPTIONAL_PARTHIAN, INSCRIPTIONAL_PAHLAVI, OLD_TURKIC, ARABIC, BRAHMI, KAITHI, SORA_SOMPENG, CHAKMA, SHARADA, TAKRI, CUNEIFORM, EGYPTIAN_HIEROGLYPHS, BAMUM, MIAO, KATAKANA, HIRAGANA, COMMON, INHERITED, COMMON, INHERITED, COMMON, INHERITED, COMMON, INHERITED, COMMON, GREEK, COMMON, ARABIC, COMMON, HIRAGANA, COMMON, HAN, COMMON, INHERITED, UNKNOWN};
            aliases = new HashMap(128);
            aliases.put("ARAB", ARABIC);
            aliases.put("ARMI", IMPERIAL_ARAMAIC);
            aliases.put("ARMN", ARMENIAN);
            aliases.put("AVST", AVESTAN);
            aliases.put("BALI", BALINESE);
            aliases.put("BAMU", BAMUM);
            aliases.put("BATK", BATAK);
            aliases.put("BENG", BENGALI);
            aliases.put("BOPO", BOPOMOFO);
            aliases.put("BRAI", BRAILLE);
            aliases.put("BRAH", BRAHMI);
            aliases.put("BUGI", BUGINESE);
            aliases.put("BUHD", BUHID);
            aliases.put("CAKM", CHAKMA);
            aliases.put("CANS", CANADIAN_ABORIGINAL);
            aliases.put("CARI", CARIAN);
            aliases.put("CHAM", CHAM);
            aliases.put("CHER", CHEROKEE);
            aliases.put("COPT", COPTIC);
            aliases.put("CPRT", CYPRIOT);
            aliases.put("CYRL", CYRILLIC);
            aliases.put("DEVA", DEVANAGARI);
            aliases.put("DSRT", DESERET);
            aliases.put("EGYP", EGYPTIAN_HIEROGLYPHS);
            aliases.put("ETHI", ETHIOPIC);
            aliases.put("GEOR", GEORGIAN);
            aliases.put("GLAG", GLAGOLITIC);
            aliases.put("GOTH", GOTHIC);
            aliases.put("GREK", GREEK);
            aliases.put("GUJR", GUJARATI);
            aliases.put("GURU", GURMUKHI);
            aliases.put("HANG", HANGUL);
            aliases.put("HANI", HAN);
            aliases.put("HANO", HANUNOO);
            aliases.put("HEBR", HEBREW);
            aliases.put("HIRA", HIRAGANA);
            aliases.put("ITAL", OLD_ITALIC);
            aliases.put("JAVA", JAVANESE);
            aliases.put("KALI", KAYAH_LI);
            aliases.put("KANA", KATAKANA);
            aliases.put("KHAR", KHAROSHTHI);
            aliases.put("KHMR", KHMER);
            aliases.put("KNDA", KANNADA);
            aliases.put("KTHI", KAITHI);
            aliases.put("LANA", TAI_THAM);
            aliases.put("LAOO", LAO);
            aliases.put("LATN", LATIN);
            aliases.put("LEPC", LEPCHA);
            aliases.put("LIMB", LIMBU);
            aliases.put("LINB", LINEAR_B);
            aliases.put("LISU", LISU);
            aliases.put("LYCI", LYCIAN);
            aliases.put("LYDI", LYDIAN);
            aliases.put("MAND", MANDAIC);
            aliases.put("MERC", MEROITIC_CURSIVE);
            aliases.put("MERO", MEROITIC_HIEROGLYPHS);
            aliases.put("MLYM", MALAYALAM);
            aliases.put("MONG", MONGOLIAN);
            aliases.put("MTEI", MEETEI_MAYEK);
            aliases.put("MYMR", MYANMAR);
            aliases.put("NKOO", NKO);
            aliases.put("OGAM", OGHAM);
            aliases.put("OLCK", OL_CHIKI);
            aliases.put("ORKH", OLD_TURKIC);
            aliases.put("ORYA", ORIYA);
            aliases.put("OSMA", OSMANYA);
            aliases.put("PHAG", PHAGS_PA);
            aliases.put("PLRD", MIAO);
            aliases.put("PHLI", INSCRIPTIONAL_PAHLAVI);
            aliases.put("PHNX", PHOENICIAN);
            aliases.put("PRTI", INSCRIPTIONAL_PARTHIAN);
            aliases.put("RJNG", REJANG);
            aliases.put("RUNR", RUNIC);
            aliases.put("SAMR", SAMARITAN);
            aliases.put("SARB", OLD_SOUTH_ARABIAN);
            aliases.put("SAUR", SAURASHTRA);
            aliases.put("SHAW", SHAVIAN);
            aliases.put("SHRD", SHARADA);
            aliases.put("SINH", SINHALA);
            aliases.put("SORA", SORA_SOMPENG);
            aliases.put("SUND", SUNDANESE);
            aliases.put("SYLO", SYLOTI_NAGRI);
            aliases.put("SYRC", SYRIAC);
            aliases.put("TAGB", TAGBANWA);
            aliases.put("TALE", TAI_LE);
            aliases.put("TAKR", TAKRI);
            aliases.put("TALU", NEW_TAI_LUE);
            aliases.put("TAML", TAMIL);
            aliases.put("TAVT", TAI_VIET);
            aliases.put("TELU", TELUGU);
            aliases.put("TFNG", TIFINAGH);
            aliases.put("TGLG", TAGALOG);
            aliases.put("THAA", THAANA);
            aliases.put("THAI", THAI);
            aliases.put("TIBT", TIBETAN);
            aliases.put("UGAR", UGARITIC);
            aliases.put("VAII", VAI);
            aliases.put("XPEO", OLD_PERSIAN);
            aliases.put("XSUX", CUNEIFORM);
            aliases.put("YIII", YI);
            aliases.put("ZINH", INHERITED);
            aliases.put("ZYYY", COMMON);
            aliases.put("ZZZZ", UNKNOWN);
        }

        public static UnicodeScript of(int codePoint) {
            if (!Character.isValidCodePoint(codePoint)) {
                throw new IllegalArgumentException();
            } else if (Character.getType(codePoint) == 0) {
                return UNKNOWN;
            } else {
                int index = Arrays.binarySearch(scriptStarts, codePoint);
                if (index < 0) {
                    index = (-index) - 2;
                }
                return scripts[index];
            }
        }

        public static final UnicodeScript forName(String scriptName) {
            scriptName = scriptName.toUpperCase(Locale.ENGLISH);
            UnicodeScript sc = (UnicodeScript) aliases.get(scriptName);
            if (sc != null) {
                return sc;
            }
            return valueOf(scriptName);
        }
    }

    static native int digitImpl(int i, int i2);

    static native byte getDirectionalityImpl(int i);

    private static native String getNameImpl(int i);

    static native int getNumericValueImpl(int i);

    static native int getTypeImpl(int i);

    static native boolean isAlphabeticImpl(int i);

    static native boolean isDefinedImpl(int i);

    static native boolean isDigitImpl(int i);

    static native boolean isIdentifierIgnorableImpl(int i);

    static native boolean isIdeographicImpl(int i);

    static native boolean isLetterImpl(int i);

    static native boolean isLetterOrDigitImpl(int i);

    static native boolean isLowerCaseImpl(int i);

    static native boolean isMirroredImpl(int i);

    static native boolean isSpaceCharImpl(int i);

    static native boolean isTitleCaseImpl(int i);

    static native boolean isUnicodeIdentifierPartImpl(int i);

    static native boolean isUnicodeIdentifierStartImpl(int i);

    static native boolean isUpperCaseImpl(int i);

    static native boolean isWhitespaceImpl(int i);

    static native int toLowerCaseImpl(int i);

    static native int toTitleCaseImpl(int i);

    static native int toUpperCaseImpl(int i);

    public Character(char value) {
        this.value = value;
    }

    public static Character valueOf(char c) {
        if (c <= 127) {
            return CharacterCache.cache[c];
        }
        return new Character(c);
    }

    public char charValue() {
        return this.value;
    }

    public int hashCode() {
        return hashCode(this.value);
    }

    public static int hashCode(char value) {
        return value;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof Character)) {
            return false;
        }
        if (this.value == ((Character) obj).charValue()) {
            z = true;
        }
        return z;
    }

    public String toString() {
        return String.valueOf(new char[]{this.value});
    }

    public static String toString(char c) {
        return String.valueOf(c);
    }

    public static boolean isValidCodePoint(int codePoint) {
        return (codePoint >>> 16) < 17;
    }

    public static boolean isBmpCodePoint(int codePoint) {
        return (codePoint >>> 16) == 0;
    }

    public static boolean isSupplementaryCodePoint(int codePoint) {
        if (codePoint < 65536 || codePoint >= 1114112) {
            return false;
        }
        return true;
    }

    public static boolean isHighSurrogate(char ch) {
        return ch >= 55296 && ch < MIN_LOW_SURROGATE;
    }

    public static boolean isLowSurrogate(char ch) {
        return ch >= MIN_LOW_SURROGATE && ch < 57344;
    }

    public static boolean isSurrogate(char ch) {
        return ch >= 55296 && ch < 57344;
    }

    public static boolean isSurrogatePair(char high, char low) {
        return isHighSurrogate(high) ? isLowSurrogate(low) : false;
    }

    public static int charCount(int codePoint) {
        return codePoint >= 65536 ? 2 : 1;
    }

    public static int toCodePoint(char high, char low) {
        return ((high << 10) + low) - 56613888;
    }

    public static int codePointAt(CharSequence seq, int index) {
        char c1 = seq.charAt(index);
        if (isHighSurrogate(c1)) {
            index++;
            if (index < seq.length()) {
                char c2 = seq.charAt(index);
                if (isLowSurrogate(c2)) {
                    return toCodePoint(c1, c2);
                }
            }
        }
        return c1;
    }

    public static int codePointAt(char[] a, int index) {
        return codePointAtImpl(a, index, a.length);
    }

    public static int codePointAt(char[] a, int index, int limit) {
        if (index < limit && limit >= 0 && limit <= a.length) {
            return codePointAtImpl(a, index, limit);
        }
        throw new IndexOutOfBoundsException();
    }

    static int codePointAtImpl(char[] a, int index, int limit) {
        char c1 = a[index];
        if (isHighSurrogate(c1)) {
            index++;
            if (index < limit) {
                char c2 = a[index];
                if (isLowSurrogate(c2)) {
                    return toCodePoint(c1, c2);
                }
            }
        }
        return c1;
    }

    public static int codePointBefore(CharSequence seq, int index) {
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

    public static int codePointBefore(char[] a, int index) {
        return codePointBeforeImpl(a, index, 0);
    }

    public static int codePointBefore(char[] a, int index, int start) {
        if (index > start && start >= 0 && start < a.length) {
            return codePointBeforeImpl(a, index, start);
        }
        throw new IndexOutOfBoundsException();
    }

    static int codePointBeforeImpl(char[] a, int index, int start) {
        index--;
        char c2 = a[index];
        if (isLowSurrogate(c2) && index > start) {
            char c1 = a[index - 1];
            if (isHighSurrogate(c1)) {
                return toCodePoint(c1, c2);
            }
        }
        return c2;
    }

    public static char highSurrogate(int codePoint) {
        return (char) ((codePoint >>> 10) + 55232);
    }

    public static char lowSurrogate(int codePoint) {
        return (char) ((codePoint & 1023) + 56320);
    }

    public static int toChars(int codePoint, char[] dst, int dstIndex) {
        if (isBmpCodePoint(codePoint)) {
            dst[dstIndex] = (char) codePoint;
            return 1;
        } else if (isValidCodePoint(codePoint)) {
            toSurrogates(codePoint, dst, dstIndex);
            return 2;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static char[] toChars(int codePoint) {
        if (isBmpCodePoint(codePoint)) {
            return new char[]{(char) codePoint};
        } else if (isValidCodePoint(codePoint)) {
            char[] result = new char[2];
            toSurrogates(codePoint, result, 0);
            return result;
        } else {
            throw new IllegalArgumentException();
        }
    }

    static void toSurrogates(int codePoint, char[] dst, int index) {
        dst[index + 1] = lowSurrogate(codePoint);
        dst[index] = highSurrogate(codePoint);
    }

    public static int codePointCount(CharSequence seq, int beginIndex, int endIndex) {
        int length = seq.length();
        if (beginIndex < 0 || endIndex > length || beginIndex > endIndex) {
            throw new IndexOutOfBoundsException();
        }
        int n = endIndex - beginIndex;
        int i = beginIndex;
        while (i < endIndex) {
            int i2 = i + 1;
            if (isHighSurrogate(seq.charAt(i)) && i2 < endIndex && isLowSurrogate(seq.charAt(i2))) {
                n--;
                i2++;
            }
            i = i2;
        }
        return n;
    }

    public static int codePointCount(char[] a, int offset, int count) {
        if (count <= a.length - offset && offset >= 0 && count >= 0) {
            return codePointCountImpl(a, offset, count);
        }
        throw new IndexOutOfBoundsException();
    }

    static int codePointCountImpl(char[] a, int offset, int count) {
        int endIndex = offset + count;
        int n = count;
        int i = offset;
        while (i < endIndex) {
            int i2 = i + 1;
            if (isHighSurrogate(a[i]) && i2 < endIndex && isLowSurrogate(a[i2])) {
                n--;
                i2++;
            }
            i = i2;
        }
        return n;
    }

    public static int offsetByCodePoints(CharSequence seq, int index, int codePointOffset) {
        int length = seq.length();
        if (index < 0 || index > length) {
            throw new IndexOutOfBoundsException();
        }
        int x = index;
        int i;
        if (codePointOffset >= 0) {
            i = 0;
            int x2 = x;
            while (x2 < length && i < codePointOffset) {
                x = x2 + 1;
                if (isHighSurrogate(seq.charAt(x2)) && x < length && isLowSurrogate(seq.charAt(x))) {
                    x++;
                }
                i++;
                x2 = x;
            }
            if (i >= codePointOffset) {
                return x2;
            }
            throw new IndexOutOfBoundsException();
        }
        i = codePointOffset;
        while (x > 0 && i < 0) {
            x--;
            if (isLowSurrogate(seq.charAt(x)) && x > 0 && isHighSurrogate(seq.charAt(x - 1))) {
                x--;
            }
            i++;
        }
        if (i >= 0) {
            return x;
        }
        throw new IndexOutOfBoundsException();
    }

    public static int offsetByCodePoints(char[] a, int start, int count, int index, int codePointOffset) {
        if (count <= a.length - start && start >= 0 && count >= 0 && index >= start && index <= start + count) {
            return offsetByCodePointsImpl(a, start, count, index, codePointOffset);
        }
        throw new IndexOutOfBoundsException();
    }

    static int offsetByCodePointsImpl(char[] a, int start, int count, int index, int codePointOffset) {
        int x = index;
        int i;
        if (codePointOffset >= 0) {
            int limit = start + count;
            i = 0;
            int x2 = x;
            while (x2 < limit && i < codePointOffset) {
                x = x2 + 1;
                if (isHighSurrogate(a[x2]) && x < limit && isLowSurrogate(a[x])) {
                    x++;
                }
                i++;
                x2 = x;
            }
            if (i >= codePointOffset) {
                return x2;
            }
            throw new IndexOutOfBoundsException();
        }
        i = codePointOffset;
        while (x > start && i < 0) {
            x--;
            if (isLowSurrogate(a[x]) && x > start && isHighSurrogate(a[x - 1])) {
                x--;
            }
            i++;
        }
        if (i >= 0) {
            return x;
        }
        throw new IndexOutOfBoundsException();
    }

    public static boolean isLowerCase(char ch) {
        return isLowerCase((int) ch);
    }

    public static boolean isLowerCase(int codePoint) {
        return isLowerCaseImpl(codePoint);
    }

    public static boolean isUpperCase(char ch) {
        return isUpperCase((int) ch);
    }

    public static boolean isUpperCase(int codePoint) {
        return isUpperCaseImpl(codePoint);
    }

    public static boolean isTitleCase(char ch) {
        return isTitleCase((int) ch);
    }

    public static boolean isTitleCase(int codePoint) {
        return isTitleCaseImpl(codePoint);
    }

    public static boolean isDigit(char ch) {
        return isDigit((int) ch);
    }

    public static boolean isDigit(int codePoint) {
        return isDigitImpl(codePoint);
    }

    public static boolean isDefined(char ch) {
        return isDefined((int) ch);
    }

    public static boolean isDefined(int codePoint) {
        return isDefinedImpl(codePoint);
    }

    public static boolean isLetter(char ch) {
        return isLetter((int) ch);
    }

    public static boolean isLetter(int codePoint) {
        return isLetterImpl(codePoint);
    }

    public static boolean isLetterOrDigit(char ch) {
        return isLetterOrDigit((int) ch);
    }

    public static boolean isLetterOrDigit(int codePoint) {
        return isLetterOrDigitImpl(codePoint);
    }

    @Deprecated
    public static boolean isJavaLetter(char ch) {
        return isJavaIdentifierStart(ch);
    }

    @Deprecated
    public static boolean isJavaLetterOrDigit(char ch) {
        return isJavaIdentifierPart(ch);
    }

    public static boolean isAlphabetic(int codePoint) {
        return isAlphabeticImpl(codePoint);
    }

    public static boolean isIdeographic(int codePoint) {
        return isIdeographicImpl(codePoint);
    }

    public static boolean isJavaIdentifierStart(char ch) {
        return isJavaIdentifierStart((int) ch);
    }

    public static boolean isJavaIdentifierStart(int codePoint) {
        boolean z = true;
        if (codePoint < 64) {
            if (codePoint != 36) {
                z = false;
            }
            return z;
        } else if (codePoint < 128) {
            if (((1 << (codePoint - 64)) & 576460745995190270L) == 0) {
                z = false;
            }
            return z;
        } else {
            if (((1 << getType(codePoint)) & 75498558) == 0) {
                z = false;
            }
            return z;
        }
    }

    public static boolean isJavaIdentifierPart(char ch) {
        return isJavaIdentifierPart((int) ch);
    }

    public static boolean isJavaIdentifierPart(int codePoint) {
        boolean z = true;
        if (codePoint < 64) {
            if (((1 << codePoint) & 287948970162897407L) == 0) {
                z = false;
            }
            return z;
        } else if (codePoint < 128) {
            if (((1 << (codePoint - 64)) & -8646911290859585538L) == 0) {
                z = false;
            }
            return z;
        } else {
            if (((1 << getType(codePoint)) & 75564926) == 0 && ((codePoint < 0 || codePoint > 8) && ((codePoint < 14 || codePoint > 27) && (codePoint < 127 || codePoint > 159)))) {
                z = false;
            }
            return z;
        }
    }

    public static boolean isUnicodeIdentifierStart(char ch) {
        return isUnicodeIdentifierStart((int) ch);
    }

    public static boolean isUnicodeIdentifierStart(int codePoint) {
        return isUnicodeIdentifierStartImpl(codePoint);
    }

    public static boolean isUnicodeIdentifierPart(char ch) {
        return isUnicodeIdentifierPart((int) ch);
    }

    public static boolean isUnicodeIdentifierPart(int codePoint) {
        return isUnicodeIdentifierPartImpl(codePoint);
    }

    public static boolean isIdentifierIgnorable(char ch) {
        return isIdentifierIgnorable((int) ch);
    }

    public static boolean isIdentifierIgnorable(int codePoint) {
        return isIdentifierIgnorableImpl(codePoint);
    }

    public static char toLowerCase(char ch) {
        return (char) toLowerCase((int) ch);
    }

    public static int toLowerCase(int codePoint) {
        if (codePoint >= 65 && codePoint <= 90) {
            return codePoint + 32;
        }
        if (codePoint < 128) {
            return codePoint;
        }
        return toLowerCaseImpl(codePoint);
    }

    public static char toUpperCase(char ch) {
        return (char) toUpperCase((int) ch);
    }

    public static int toUpperCase(int codePoint) {
        if (codePoint >= 97 && codePoint <= 122) {
            return codePoint - 32;
        }
        if (codePoint < 128) {
            return codePoint;
        }
        return toUpperCaseImpl(codePoint);
    }

    public static char toTitleCase(char ch) {
        return (char) toTitleCase((int) ch);
    }

    public static int toTitleCase(int codePoint) {
        return toTitleCaseImpl(codePoint);
    }

    public static int digit(char ch, int radix) {
        return digit((int) ch, radix);
    }

    public static int digit(int codePoint, int radix) {
        if (radix < 2 || radix > 36) {
            return -1;
        }
        if (codePoint >= 128) {
            return digitImpl(codePoint, radix);
        }
        int result = -1;
        if (48 <= codePoint && codePoint <= 57) {
            result = codePoint - 48;
        } else if (97 <= codePoint && codePoint <= 122) {
            result = (codePoint - 97) + 10;
        } else if (65 <= codePoint && codePoint <= 90) {
            result = (codePoint - 65) + 10;
        }
        if (result >= radix) {
            result = -1;
        }
        return result;
    }

    public static int getNumericValue(char ch) {
        return getNumericValue((int) ch);
    }

    public static int getNumericValue(int codePoint) {
        if (codePoint < 128) {
            if (codePoint >= 48 && codePoint <= 57) {
                return codePoint - 48;
            }
            if (codePoint >= 97 && codePoint <= 122) {
                return codePoint - 87;
            }
            if (codePoint < 65 || codePoint > 90) {
                return -1;
            }
            return codePoint - 55;
        } else if (codePoint >= 65313 && codePoint <= 65338) {
            return codePoint - 65303;
        } else {
            if (codePoint < 65345 || codePoint > 65370) {
                return getNumericValueImpl(codePoint);
            }
            return codePoint - 65335;
        }
    }

    @Deprecated
    public static boolean isSpace(char ch) {
        if (ch > ' ' || ((4294981120L >> ch) & 1) == 0) {
            return false;
        }
        return true;
    }

    public static boolean isSpaceChar(char ch) {
        return isSpaceChar((int) ch);
    }

    public static boolean isSpaceChar(int codePoint) {
        boolean z = true;
        if (codePoint == 32 || codePoint == 160) {
            return true;
        }
        if (codePoint < 4096) {
            return false;
        }
        if (codePoint == 5760 || codePoint == 6158) {
            return true;
        }
        if (codePoint < 8192) {
            return false;
        }
        if (codePoint > 65535) {
            return isSpaceCharImpl(codePoint);
        }
        if (!(codePoint <= 8202 || codePoint == 8232 || codePoint == 8233 || codePoint == 8239 || codePoint == 8287 || codePoint == 12288)) {
            z = false;
        }
        return z;
    }

    public static boolean isWhitespace(char ch) {
        return isWhitespace((int) ch);
    }

    public static boolean isWhitespace(int codePoint) {
        boolean z = true;
        if ((codePoint >= 28 && codePoint <= 32) || (codePoint >= 9 && codePoint <= 13)) {
            return true;
        }
        if (codePoint < 4096) {
            return false;
        }
        if (codePoint == 5760 || codePoint == 6158) {
            return true;
        }
        if (codePoint < 8192 || codePoint == 8199 || codePoint == 8239) {
            return false;
        }
        if (codePoint > 65535) {
            return isWhitespaceImpl(codePoint);
        }
        if (!(codePoint <= 8202 || codePoint == 8232 || codePoint == 8233 || codePoint == 8287 || codePoint == 12288)) {
            z = false;
        }
        return z;
    }

    public static boolean isISOControl(char ch) {
        return isISOControl((int) ch);
    }

    public static boolean isISOControl(int codePoint) {
        if (codePoint <= 159) {
            return codePoint >= 127 || (codePoint >>> 5) == 0;
        } else {
            return false;
        }
    }

    public static int getType(char ch) {
        return getType((int) ch);
    }

    public static int getType(int codePoint) {
        int type = getTypeImpl(codePoint);
        if (type <= 16) {
            return type;
        }
        return type + 1;
    }

    /* JADX WARNING: Missing block: B:3:0x0005, code:
            return 0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static char forDigit(int digit, int radix) {
        if (digit >= radix || digit < 0 || radix < 2 || radix > 36) {
            return 0;
        }
        if (digit < 10) {
            return (char) (digit + 48);
        }
        return (char) (digit + 87);
    }

    public static byte getDirectionality(char ch) {
        return getDirectionality((int) ch);
    }

    public static byte getDirectionality(int codePoint) {
        if (getType(codePoint) == 0) {
            return (byte) -1;
        }
        byte directionality = getDirectionalityImpl(codePoint);
        if (directionality < (byte) 0 || directionality >= DIRECTIONALITY.length) {
            return (byte) -1;
        }
        return DIRECTIONALITY[directionality];
    }

    public static boolean isMirrored(char ch) {
        return isMirrored((int) ch);
    }

    public static boolean isMirrored(int codePoint) {
        return isMirroredImpl(codePoint);
    }

    public int compareTo(Character anotherCharacter) {
        return compare(this.value, anotherCharacter.value);
    }

    public static int compare(char x, char y) {
        return x - y;
    }

    public static char reverseBytes(char ch) {
        return (char) (((65280 & ch) >> 8) | (ch << 8));
    }

    public static String getName(int codePoint) {
        if (isValidCodePoint(codePoint)) {
            String name = getNameImpl(codePoint);
            if (name != null) {
                return name;
            }
            if (getType(codePoint) == 0) {
                return null;
            }
            UnicodeBlock block = UnicodeBlock.of(codePoint);
            if (block != null) {
                return block.toString().replace('_', ' ') + " " + Integer.toHexString(codePoint).toUpperCase(Locale.ENGLISH);
            }
            return Integer.toHexString(codePoint).toUpperCase(Locale.ENGLISH);
        }
        throw new IllegalArgumentException();
    }
}
