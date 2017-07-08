package java.lang;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Spliterator;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import sun.misc.DoubleConsts;
import sun.misc.FloatConsts;

public final class Character implements Serializable, Comparable<Character> {
    public static final int BYTES = 2;
    public static final byte COMBINING_SPACING_MARK = (byte) 8;
    public static final byte CONNECTOR_PUNCTUATION = (byte) 23;
    public static final byte CONTROL = (byte) 15;
    public static final byte CURRENCY_SYMBOL = (byte) 26;
    public static final byte DASH_PUNCTUATION = (byte) 20;
    public static final byte DECIMAL_DIGIT_NUMBER = (byte) 9;
    private static final byte[] DIRECTIONALITY = null;
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
    public static final char MAX_HIGH_SURROGATE = '\udbff';
    public static final char MAX_LOW_SURROGATE = '\udfff';
    public static final int MAX_RADIX = 36;
    public static final char MAX_SURROGATE = '\udfff';
    public static final char MAX_VALUE = '\uffff';
    public static final int MIN_CODE_POINT = 0;
    public static final char MIN_HIGH_SURROGATE = '\ud800';
    public static final char MIN_LOW_SURROGATE = '\udc00';
    public static final int MIN_RADIX = 2;
    public static final int MIN_SUPPLEMENTARY_CODE_POINT = 65536;
    public static final char MIN_SURROGATE = '\ud800';
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
    public static final Class<Character> TYPE = null;
    public static final byte UNASSIGNED = (byte) 0;
    public static final byte UPPERCASE_LETTER = (byte) 1;
    private static final long serialVersionUID = 3786198910865385080L;
    private final char value;

    private static class CharacterCache {
        static final Character[] cache = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.lang.Character.CharacterCache.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.lang.Character.CharacterCache.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: java.lang.Character.CharacterCache.<clinit>():void");
        }

        private CharacterCache() {
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
        public static final UnicodeBlock AEGEAN_NUMBERS = null;
        public static final UnicodeBlock ALCHEMICAL_SYMBOLS = null;
        public static final UnicodeBlock ALPHABETIC_PRESENTATION_FORMS = null;
        public static final UnicodeBlock ANCIENT_GREEK_MUSICAL_NOTATION = null;
        public static final UnicodeBlock ANCIENT_GREEK_NUMBERS = null;
        public static final UnicodeBlock ANCIENT_SYMBOLS = null;
        public static final UnicodeBlock ARABIC = null;
        public static final UnicodeBlock ARABIC_PRESENTATION_FORMS_A = null;
        public static final UnicodeBlock ARABIC_PRESENTATION_FORMS_B = null;
        public static final UnicodeBlock ARABIC_SUPPLEMENT = null;
        public static final UnicodeBlock ARMENIAN = null;
        public static final UnicodeBlock ARROWS = null;
        public static final UnicodeBlock AVESTAN = null;
        public static final UnicodeBlock BALINESE = null;
        public static final UnicodeBlock BAMUM = null;
        public static final UnicodeBlock BAMUM_SUPPLEMENT = null;
        public static final UnicodeBlock BASIC_LATIN = null;
        public static final UnicodeBlock BATAK = null;
        public static final UnicodeBlock BENGALI = null;
        public static final UnicodeBlock BLOCK_ELEMENTS = null;
        public static final UnicodeBlock BOPOMOFO = null;
        public static final UnicodeBlock BOPOMOFO_EXTENDED = null;
        public static final UnicodeBlock BOX_DRAWING = null;
        public static final UnicodeBlock BRAHMI = null;
        public static final UnicodeBlock BRAILLE_PATTERNS = null;
        public static final UnicodeBlock BUGINESE = null;
        public static final UnicodeBlock BUHID = null;
        public static final UnicodeBlock BYZANTINE_MUSICAL_SYMBOLS = null;
        public static final UnicodeBlock CARIAN = null;
        public static final UnicodeBlock CHAM = null;
        public static final UnicodeBlock CHEROKEE = null;
        public static final UnicodeBlock CJK_COMPATIBILITY = null;
        public static final UnicodeBlock CJK_COMPATIBILITY_FORMS = null;
        public static final UnicodeBlock CJK_COMPATIBILITY_IDEOGRAPHS = null;
        public static final UnicodeBlock CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT = null;
        public static final UnicodeBlock CJK_RADICALS_SUPPLEMENT = null;
        public static final UnicodeBlock CJK_STROKES = null;
        public static final UnicodeBlock CJK_SYMBOLS_AND_PUNCTUATION = null;
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS = null;
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A = null;
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B = null;
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C = null;
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D = null;
        public static final UnicodeBlock COMBINING_DIACRITICAL_MARKS = null;
        public static final UnicodeBlock COMBINING_DIACRITICAL_MARKS_SUPPLEMENT = null;
        public static final UnicodeBlock COMBINING_HALF_MARKS = null;
        public static final UnicodeBlock COMBINING_MARKS_FOR_SYMBOLS = null;
        public static final UnicodeBlock COMMON_INDIC_NUMBER_FORMS = null;
        public static final UnicodeBlock CONTROL_PICTURES = null;
        public static final UnicodeBlock COPTIC = null;
        public static final UnicodeBlock COUNTING_ROD_NUMERALS = null;
        public static final UnicodeBlock CUNEIFORM = null;
        public static final UnicodeBlock CUNEIFORM_NUMBERS_AND_PUNCTUATION = null;
        public static final UnicodeBlock CURRENCY_SYMBOLS = null;
        public static final UnicodeBlock CYPRIOT_SYLLABARY = null;
        public static final UnicodeBlock CYRILLIC = null;
        public static final UnicodeBlock CYRILLIC_EXTENDED_A = null;
        public static final UnicodeBlock CYRILLIC_EXTENDED_B = null;
        public static final UnicodeBlock CYRILLIC_SUPPLEMENTARY = null;
        public static final UnicodeBlock DESERET = null;
        public static final UnicodeBlock DEVANAGARI = null;
        public static final UnicodeBlock DEVANAGARI_EXTENDED = null;
        public static final UnicodeBlock DINGBATS = null;
        public static final UnicodeBlock DOMINO_TILES = null;
        public static final UnicodeBlock EGYPTIAN_HIEROGLYPHS = null;
        public static final UnicodeBlock EMOTICONS = null;
        public static final UnicodeBlock ENCLOSED_ALPHANUMERICS = null;
        public static final UnicodeBlock ENCLOSED_ALPHANUMERIC_SUPPLEMENT = null;
        public static final UnicodeBlock ENCLOSED_CJK_LETTERS_AND_MONTHS = null;
        public static final UnicodeBlock ENCLOSED_IDEOGRAPHIC_SUPPLEMENT = null;
        public static final UnicodeBlock ETHIOPIC = null;
        public static final UnicodeBlock ETHIOPIC_EXTENDED = null;
        public static final UnicodeBlock ETHIOPIC_EXTENDED_A = null;
        public static final UnicodeBlock ETHIOPIC_SUPPLEMENT = null;
        public static final UnicodeBlock GENERAL_PUNCTUATION = null;
        public static final UnicodeBlock GEOMETRIC_SHAPES = null;
        public static final UnicodeBlock GEORGIAN = null;
        public static final UnicodeBlock GEORGIAN_SUPPLEMENT = null;
        public static final UnicodeBlock GLAGOLITIC = null;
        public static final UnicodeBlock GOTHIC = null;
        public static final UnicodeBlock GREEK = null;
        public static final UnicodeBlock GREEK_EXTENDED = null;
        public static final UnicodeBlock GUJARATI = null;
        public static final UnicodeBlock GURMUKHI = null;
        public static final UnicodeBlock HALFWIDTH_AND_FULLWIDTH_FORMS = null;
        public static final UnicodeBlock HANGUL_COMPATIBILITY_JAMO = null;
        public static final UnicodeBlock HANGUL_JAMO = null;
        public static final UnicodeBlock HANGUL_JAMO_EXTENDED_A = null;
        public static final UnicodeBlock HANGUL_JAMO_EXTENDED_B = null;
        public static final UnicodeBlock HANGUL_SYLLABLES = null;
        public static final UnicodeBlock HANUNOO = null;
        public static final UnicodeBlock HEBREW = null;
        public static final UnicodeBlock HIGH_PRIVATE_USE_SURROGATES = null;
        public static final UnicodeBlock HIGH_SURROGATES = null;
        public static final UnicodeBlock HIRAGANA = null;
        public static final UnicodeBlock IDEOGRAPHIC_DESCRIPTION_CHARACTERS = null;
        public static final UnicodeBlock IMPERIAL_ARAMAIC = null;
        public static final UnicodeBlock INSCRIPTIONAL_PAHLAVI = null;
        public static final UnicodeBlock INSCRIPTIONAL_PARTHIAN = null;
        public static final UnicodeBlock IPA_EXTENSIONS = null;
        public static final UnicodeBlock JAVANESE = null;
        public static final UnicodeBlock KAITHI = null;
        public static final UnicodeBlock KANA_SUPPLEMENT = null;
        public static final UnicodeBlock KANBUN = null;
        public static final UnicodeBlock KANGXI_RADICALS = null;
        public static final UnicodeBlock KANNADA = null;
        public static final UnicodeBlock KATAKANA = null;
        public static final UnicodeBlock KATAKANA_PHONETIC_EXTENSIONS = null;
        public static final UnicodeBlock KAYAH_LI = null;
        public static final UnicodeBlock KHAROSHTHI = null;
        public static final UnicodeBlock KHMER = null;
        public static final UnicodeBlock KHMER_SYMBOLS = null;
        public static final UnicodeBlock LAO = null;
        public static final UnicodeBlock LATIN_1_SUPPLEMENT = null;
        public static final UnicodeBlock LATIN_EXTENDED_A = null;
        public static final UnicodeBlock LATIN_EXTENDED_ADDITIONAL = null;
        public static final UnicodeBlock LATIN_EXTENDED_B = null;
        public static final UnicodeBlock LATIN_EXTENDED_C = null;
        public static final UnicodeBlock LATIN_EXTENDED_D = null;
        public static final UnicodeBlock LEPCHA = null;
        public static final UnicodeBlock LETTERLIKE_SYMBOLS = null;
        public static final UnicodeBlock LIMBU = null;
        public static final UnicodeBlock LINEAR_B_IDEOGRAMS = null;
        public static final UnicodeBlock LINEAR_B_SYLLABARY = null;
        public static final UnicodeBlock LISU = null;
        public static final UnicodeBlock LOW_SURROGATES = null;
        public static final UnicodeBlock LYCIAN = null;
        public static final UnicodeBlock LYDIAN = null;
        public static final UnicodeBlock MAHJONG_TILES = null;
        public static final UnicodeBlock MALAYALAM = null;
        public static final UnicodeBlock MANDAIC = null;
        public static final UnicodeBlock MATHEMATICAL_ALPHANUMERIC_SYMBOLS = null;
        public static final UnicodeBlock MATHEMATICAL_OPERATORS = null;
        public static final UnicodeBlock MEETEI_MAYEK = null;
        public static final UnicodeBlock MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A = null;
        public static final UnicodeBlock MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B = null;
        public static final UnicodeBlock MISCELLANEOUS_SYMBOLS = null;
        public static final UnicodeBlock MISCELLANEOUS_SYMBOLS_AND_ARROWS = null;
        public static final UnicodeBlock MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS = null;
        public static final UnicodeBlock MISCELLANEOUS_TECHNICAL = null;
        public static final UnicodeBlock MODIFIER_TONE_LETTERS = null;
        public static final UnicodeBlock MONGOLIAN = null;
        public static final UnicodeBlock MUSICAL_SYMBOLS = null;
        public static final UnicodeBlock MYANMAR = null;
        public static final UnicodeBlock MYANMAR_EXTENDED_A = null;
        public static final UnicodeBlock NEW_TAI_LUE = null;
        public static final UnicodeBlock NKO = null;
        public static final UnicodeBlock NUMBER_FORMS = null;
        public static final UnicodeBlock OGHAM = null;
        public static final UnicodeBlock OLD_ITALIC = null;
        public static final UnicodeBlock OLD_PERSIAN = null;
        public static final UnicodeBlock OLD_SOUTH_ARABIAN = null;
        public static final UnicodeBlock OLD_TURKIC = null;
        public static final UnicodeBlock OL_CHIKI = null;
        public static final UnicodeBlock OPTICAL_CHARACTER_RECOGNITION = null;
        public static final UnicodeBlock ORIYA = null;
        public static final UnicodeBlock OSMANYA = null;
        public static final UnicodeBlock PHAGS_PA = null;
        public static final UnicodeBlock PHAISTOS_DISC = null;
        public static final UnicodeBlock PHOENICIAN = null;
        public static final UnicodeBlock PHONETIC_EXTENSIONS = null;
        public static final UnicodeBlock PHONETIC_EXTENSIONS_SUPPLEMENT = null;
        public static final UnicodeBlock PLAYING_CARDS = null;
        public static final UnicodeBlock PRIVATE_USE_AREA = null;
        public static final UnicodeBlock REJANG = null;
        public static final UnicodeBlock RUMI_NUMERAL_SYMBOLS = null;
        public static final UnicodeBlock RUNIC = null;
        public static final UnicodeBlock SAMARITAN = null;
        public static final UnicodeBlock SAURASHTRA = null;
        public static final UnicodeBlock SHAVIAN = null;
        public static final UnicodeBlock SINHALA = null;
        public static final UnicodeBlock SMALL_FORM_VARIANTS = null;
        public static final UnicodeBlock SPACING_MODIFIER_LETTERS = null;
        public static final UnicodeBlock SPECIALS = null;
        public static final UnicodeBlock SUNDANESE = null;
        public static final UnicodeBlock SUPERSCRIPTS_AND_SUBSCRIPTS = null;
        public static final UnicodeBlock SUPPLEMENTAL_ARROWS_A = null;
        public static final UnicodeBlock SUPPLEMENTAL_ARROWS_B = null;
        public static final UnicodeBlock SUPPLEMENTAL_MATHEMATICAL_OPERATORS = null;
        public static final UnicodeBlock SUPPLEMENTAL_PUNCTUATION = null;
        public static final UnicodeBlock SUPPLEMENTARY_PRIVATE_USE_AREA_A = null;
        public static final UnicodeBlock SUPPLEMENTARY_PRIVATE_USE_AREA_B = null;
        @Deprecated
        public static final UnicodeBlock SURROGATES_AREA = null;
        public static final UnicodeBlock SYLOTI_NAGRI = null;
        public static final UnicodeBlock SYRIAC = null;
        public static final UnicodeBlock TAGALOG = null;
        public static final UnicodeBlock TAGBANWA = null;
        public static final UnicodeBlock TAGS = null;
        public static final UnicodeBlock TAI_LE = null;
        public static final UnicodeBlock TAI_THAM = null;
        public static final UnicodeBlock TAI_VIET = null;
        public static final UnicodeBlock TAI_XUAN_JING_SYMBOLS = null;
        public static final UnicodeBlock TAMIL = null;
        public static final UnicodeBlock TELUGU = null;
        public static final UnicodeBlock THAANA = null;
        public static final UnicodeBlock THAI = null;
        public static final UnicodeBlock TIBETAN = null;
        public static final UnicodeBlock TIFINAGH = null;
        public static final UnicodeBlock TRANSPORT_AND_MAP_SYMBOLS = null;
        public static final UnicodeBlock UGARITIC = null;
        public static final UnicodeBlock UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS = null;
        public static final UnicodeBlock UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_EXTENDED = null;
        public static final UnicodeBlock VAI = null;
        public static final UnicodeBlock VARIATION_SELECTORS = null;
        public static final UnicodeBlock VARIATION_SELECTORS_SUPPLEMENT = null;
        public static final UnicodeBlock VEDIC_EXTENSIONS = null;
        public static final UnicodeBlock VERTICAL_FORMS = null;
        public static final UnicodeBlock YIJING_HEXAGRAM_SYMBOLS = null;
        public static final UnicodeBlock YI_RADICALS = null;
        public static final UnicodeBlock YI_SYLLABLES = null;
        private static final int[] blockStarts = null;
        private static final UnicodeBlock[] blocks = null;
        private static Map<String, UnicodeBlock> map;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.lang.Character.UnicodeBlock.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.lang.Character.UnicodeBlock.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: java.lang.Character.UnicodeBlock.<clinit>():void");
        }

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
            this(idName, true);
            map.put(alias, this);
        }

        private UnicodeBlock(String idName, String... aliases) {
            this(idName, true);
            int length = aliases.length;
            for (int i = Character.MIN_CODE_POINT; i < length; i++) {
                map.put(aliases[i], this);
            }
        }

        public static UnicodeBlock of(char c) {
            return of((int) c);
        }

        public static UnicodeBlock of(int codePoint) {
            if (Character.isValidCodePoint(codePoint)) {
                int bottom = Character.MIN_CODE_POINT;
                int top = blockStarts.length;
                int current = top / Character.MIN_RADIX;
                while (top - bottom > 1) {
                    if (codePoint >= blockStarts[current]) {
                        bottom = current;
                    } else {
                        top = current;
                    }
                    current = (top + bottom) / Character.MIN_RADIX;
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
        ;
        
        private static HashMap<String, UnicodeScript> aliases;
        private static final int[] scriptStarts = null;
        private static final UnicodeScript[] scripts = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.lang.Character.UnicodeScript.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.lang.Character.UnicodeScript.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: java.lang.Character.UnicodeScript.<clinit>():void");
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.lang.Character.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.lang.Character.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.lang.Character.<clinit>():void");
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
        if (c <= '\u007f') {
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
        return (codePoint >>> SIZE) < 17;
    }

    public static boolean isBmpCodePoint(int codePoint) {
        return (codePoint >>> SIZE) == 0;
    }

    public static boolean isSupplementaryCodePoint(int codePoint) {
        if (codePoint < MIN_SUPPLEMENTARY_CODE_POINT || codePoint >= 1114112) {
            return false;
        }
        return true;
    }

    public static boolean isHighSurrogate(char ch) {
        return ch >= MIN_SURROGATE && ch < MIN_LOW_SURROGATE;
    }

    public static boolean isLowSurrogate(char ch) {
        return ch >= MIN_LOW_SURROGATE && ch < '\ue000';
    }

    public static boolean isSurrogate(char ch) {
        return ch >= MIN_SURROGATE && ch < '\ue000';
    }

    public static boolean isSurrogatePair(char high, char low) {
        return isHighSurrogate(high) ? isLowSurrogate(low) : false;
    }

    public static int charCount(int codePoint) {
        return codePoint >= MIN_SUPPLEMENTARY_CODE_POINT ? MIN_RADIX : 1;
    }

    public static int toCodePoint(char high, char low) {
        return ((high << 10) + low) - 56613888;
    }

    public static int codePointAt(CharSequence seq, int index) {
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
        int index2 = index + 1;
        char c1 = a[index];
        if (isHighSurrogate(c1) && index2 < limit) {
            char c2 = a[index2];
            if (isLowSurrogate(c2)) {
                return toCodePoint(c1, c2);
            }
        }
        return c1;
    }

    public static int codePointBefore(CharSequence seq, int index) {
        index += ERROR;
        char c2 = seq.charAt(index);
        if (isLowSurrogate(c2) && index > 0) {
            char c1 = seq.charAt(index + ERROR);
            if (isHighSurrogate(c1)) {
                return toCodePoint(c1, c2);
            }
        }
        return c2;
    }

    public static int codePointBefore(char[] a, int index) {
        return codePointBeforeImpl(a, index, MIN_CODE_POINT);
    }

    public static int codePointBefore(char[] a, int index, int start) {
        if (index > start && start >= 0 && start < a.length) {
            return codePointBeforeImpl(a, index, start);
        }
        throw new IndexOutOfBoundsException();
    }

    static int codePointBeforeImpl(char[] a, int index, int start) {
        index += ERROR;
        char c2 = a[index];
        if (isLowSurrogate(c2) && index > start) {
            char c1 = a[index + ERROR];
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
        return (char) ((codePoint & DoubleConsts.MAX_EXPONENT) + 56320);
    }

    public static int toChars(int codePoint, char[] dst, int dstIndex) {
        if (isBmpCodePoint(codePoint)) {
            dst[dstIndex] = (char) codePoint;
            return 1;
        } else if (isValidCodePoint(codePoint)) {
            toSurrogates(codePoint, dst, dstIndex);
            return MIN_RADIX;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static char[] toChars(int codePoint) {
        if (isBmpCodePoint(codePoint)) {
            return new char[]{(char) codePoint};
        } else if (isValidCodePoint(codePoint)) {
            char[] result = new char[MIN_RADIX];
            toSurrogates(codePoint, result, MIN_CODE_POINT);
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
                n += ERROR;
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
                n += ERROR;
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
            i = MIN_CODE_POINT;
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
            x += ERROR;
            if (isLowSurrogate(seq.charAt(x)) && x > 0 && isHighSurrogate(seq.charAt(x + ERROR))) {
                x += ERROR;
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
            i = MIN_CODE_POINT;
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
            x += ERROR;
            if (isLowSurrogate(a[x]) && x > start && isHighSurrogate(a[x + ERROR])) {
                x += ERROR;
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
            if (codePoint != MAX_RADIX) {
                z = false;
            }
            return z;
        } else if (codePoint < Pattern.CANON_EQ) {
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
        } else if (codePoint < Pattern.CANON_EQ) {
            if (((1 << (codePoint - 64)) & -8646911290859585538L) == 0) {
                z = false;
            }
            return z;
        } else {
            if (((1 << getType(codePoint)) & 75564926) == 0 && ((codePoint < 0 || codePoint > 8) && ((codePoint < 14 || codePoint > 27) && (codePoint < FloatConsts.MAX_EXPONENT || codePoint > 159)))) {
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
        return toLowerCaseImpl(codePoint);
    }

    public static char toUpperCase(char ch) {
        return (char) toUpperCase((int) ch);
    }

    public static int toUpperCase(int codePoint) {
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
        if (radix < MIN_RADIX || radix > MAX_RADIX) {
            return ERROR;
        }
        if (codePoint >= Pattern.CANON_EQ) {
            return digitImpl(codePoint, radix);
        }
        int result = ERROR;
        if (48 <= codePoint && codePoint <= 57) {
            result = codePoint - 48;
        } else if (97 <= codePoint && codePoint <= 122) {
            result = (codePoint - 97) + 10;
        } else if (65 <= codePoint && codePoint <= 90) {
            result = (codePoint - 65) + 10;
        }
        if (result >= radix) {
            result = ERROR;
        }
        return result;
    }

    public static int getNumericValue(char ch) {
        return getNumericValue((int) ch);
    }

    public static int getNumericValue(int codePoint) {
        if (codePoint < Pattern.CANON_EQ) {
            if (codePoint >= 48 && codePoint <= 57) {
                return codePoint - 48;
            }
            if (codePoint >= 97 && codePoint <= 122) {
                return codePoint - 87;
            }
            if (codePoint < 65 || codePoint > 90) {
                return ERROR;
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
        if (codePoint < Spliterator.CONCURRENT) {
            return false;
        }
        if (codePoint == 5760 || codePoint == 6158) {
            return true;
        }
        if (codePoint < Preferences.MAX_VALUE_LENGTH) {
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
        if (codePoint < Spliterator.CONCURRENT) {
            return false;
        }
        if (codePoint == 5760 || codePoint == 6158) {
            return true;
        }
        if (codePoint < Preferences.MAX_VALUE_LENGTH || codePoint == 8199 || codePoint == 8239) {
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
            return codePoint >= FloatConsts.MAX_EXPONENT || (codePoint >>> 5) == 0;
        } else {
            return false;
        }
    }

    public static int getType(char ch) {
        return getType((int) ch);
    }

    public static int getType(int codePoint) {
        int type = getTypeImpl(codePoint);
        if (type <= SIZE) {
            return type;
        }
        return type + 1;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static char forDigit(int digit, int radix) {
        if (digit >= radix || digit < 0 || radix < MIN_RADIX || radix > MAX_RADIX) {
            return MIN_VALUE;
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
            return DIRECTIONALITY_UNDEFINED;
        }
        byte directionality = getDirectionalityImpl(codePoint);
        if (directionality < null || directionality >= DIRECTIONALITY.length) {
            return DIRECTIONALITY_UNDEFINED;
        }
        return DIRECTIONALITY[directionality];
    }

    public static boolean isMirrored(char ch) {
        return isMirrored((int) ch);
    }

    public static boolean isMirrored(int codePoint) {
        return isMirroredImpl(codePoint);
    }

    public /* bridge */ /* synthetic */ int compareTo(Object anotherCharacter) {
        return compareTo((Character) anotherCharacter);
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
