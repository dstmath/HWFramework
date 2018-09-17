package android.icu.lang;

import android.icu.impl.UCharacterProperty;
import android.icu.text.ArabicShaping;
import android.icu.text.UnicodeSet;
import android.icu.util.ULocale;
import dalvik.bytecode.Opcodes;
import dalvik.system.VMDebug;
import java.util.BitSet;
import java.util.Locale;
import libcore.icu.DateUtilsBridge;
import org.xmlpull.v1.XmlPullParser;

public final class UScript {
    public static final int AFAKA = 147;
    public static final int AHOM = 161;
    public static final int ANATOLIAN_HIEROGLYPHS = 156;
    public static final int ARABIC = 2;
    public static final int ARMENIAN = 3;
    public static final int AVESTAN = 117;
    public static final int BALINESE = 62;
    public static final int BAMUM = 130;
    public static final int BASSA_VAH = 134;
    public static final int BATAK = 63;
    public static final int BENGALI = 4;
    public static final int BLISSYMBOLS = 64;
    public static final int BOOK_PAHLAVI = 124;
    public static final int BOPOMOFO = 5;
    public static final int BRAHMI = 65;
    public static final int BRAILLE = 46;
    public static final int BUGINESE = 55;
    public static final int BUHID = 44;
    public static final int CANADIAN_ABORIGINAL = 40;
    public static final int CARIAN = 104;
    public static final int CAUCASIAN_ALBANIAN = 159;
    public static final int CHAKMA = 118;
    public static final int CHAM = 66;
    public static final int CHEROKEE = 6;
    public static final int CIRTH = 67;
    public static final int CODE_LIMIT = 167;
    public static final int COMMON = 0;
    public static final int COPTIC = 7;
    public static final int CUNEIFORM = 101;
    public static final int CYPRIOT = 47;
    public static final int CYRILLIC = 8;
    public static final int DEMOTIC_EGYPTIAN = 69;
    public static final int DESERET = 9;
    public static final int DEVANAGARI = 10;
    public static final int DUPLOYAN = 135;
    @Deprecated
    public static final int DUPLOYAN_SHORTAND = 135;
    public static final int EASTERN_SYRIAC = 97;
    public static final int EGYPTIAN_HIEROGLYPHS = 71;
    public static final int ELBASAN = 136;
    public static final int ESTRANGELO_SYRIAC = 95;
    public static final int ETHIOPIC = 11;
    public static final int GEORGIAN = 12;
    public static final int GLAGOLITIC = 56;
    public static final int GOTHIC = 13;
    public static final int GRANTHA = 137;
    public static final int GREEK = 14;
    public static final int GUJARATI = 15;
    public static final int GURMUKHI = 16;
    public static final int HAN = 17;
    public static final int HANGUL = 18;
    public static final int HANUNOO = 43;
    public static final int HARAPPAN_INDUS = 77;
    public static final int HATRAN = 162;
    public static final int HEBREW = 19;
    public static final int HIERATIC_EGYPTIAN = 70;
    public static final int HIRAGANA = 20;
    public static final int IMPERIAL_ARAMAIC = 116;
    public static final int INHERITED = 1;
    public static final int INSCRIPTIONAL_PAHLAVI = 122;
    public static final int INSCRIPTIONAL_PARTHIAN = 125;
    public static final int INVALID_CODE = -1;
    public static final int JAPANESE = 105;
    public static final int JAVANESE = 78;
    public static final int JURCHEN = 148;
    public static final int KAITHI = 120;
    public static final int KANNADA = 21;
    public static final int KATAKANA = 22;
    public static final int KATAKANA_OR_HIRAGANA = 54;
    public static final int KAYAH_LI = 79;
    public static final int KHAROSHTHI = 57;
    public static final int KHMER = 23;
    public static final int KHOJKI = 157;
    public static final int KHUDAWADI = 145;
    public static final int KHUTSURI = 72;
    public static final int KOREAN = 119;
    public static final int KPELLE = 138;
    public static final int LANNA = 106;
    public static final int LAO = 24;
    public static final int LATIN = 25;
    public static final int LATIN_FRAKTUR = 80;
    public static final int LATIN_GAELIC = 81;
    public static final int LEPCHA = 82;
    public static final int LIMBU = 48;
    public static final int LINEAR_A = 83;
    public static final int LINEAR_B = 49;
    public static final int LISU = 131;
    public static final int LOMA = 139;
    public static final int LYCIAN = 107;
    public static final int LYDIAN = 108;
    public static final int MAHAJANI = 160;
    public static final int MALAYALAM = 26;
    public static final int MANDAEAN = 84;
    public static final int MANDAIC = 84;
    public static final int MANICHAEAN = 121;
    public static final int MATHEMATICAL_NOTATION = 128;
    public static final int MAYAN_HIEROGLYPHS = 85;
    public static final int MEITEI_MAYEK = 115;
    public static final int MENDE = 140;
    public static final int MEROITIC = 86;
    public static final int MEROITIC_CURSIVE = 141;
    public static final int MEROITIC_HIEROGLYPHS = 86;
    public static final int MIAO = 92;
    public static final int MODI = 163;
    public static final int MONGOLIAN = 27;
    public static final int MOON = 114;
    public static final int MRO = 149;
    public static final int MULTANI = 164;
    public static final int MYANMAR = 28;
    public static final int NABATAEAN = 143;
    public static final int NAKHI_GEBA = 132;
    public static final int NEW_TAI_LUE = 59;
    public static final int NKO = 87;
    public static final int NUSHU = 150;
    public static final int OGHAM = 29;
    public static final int OLD_CHURCH_SLAVONIC_CYRILLIC = 68;
    public static final int OLD_HUNGARIAN = 76;
    public static final int OLD_ITALIC = 30;
    public static final int OLD_NORTH_ARABIAN = 142;
    public static final int OLD_PERMIC = 89;
    public static final int OLD_PERSIAN = 61;
    public static final int OLD_SOUTH_ARABIAN = 133;
    public static final int OL_CHIKI = 109;
    public static final int ORIYA = 31;
    public static final int ORKHON = 88;
    public static final int OSMANYA = 50;
    public static final int PAHAWH_HMONG = 75;
    public static final int PALMYRENE = 144;
    public static final int PAU_CIN_HAU = 165;
    public static final int PHAGS_PA = 90;
    public static final int PHOENICIAN = 91;
    public static final int PHONETIC_POLLARD = 92;
    public static final int PSALTER_PAHLAVI = 123;
    public static final int REJANG = 110;
    public static final int RONGORONGO = 93;
    public static final int RUNIC = 32;
    public static final int SAMARITAN = 126;
    public static final int SARATI = 94;
    public static final int SAURASHTRA = 111;
    public static final int SHARADA = 151;
    public static final int SHAVIAN = 51;
    public static final int SIDDHAM = 166;
    public static final int SIGN_WRITING = 112;
    public static final int SIMPLIFIED_HAN = 73;
    public static final int SINDHI = 145;
    public static final int SINHALA = 33;
    public static final int SORA_SOMPENG = 152;
    public static final int SUNDANESE = 113;
    public static final int SYLOTI_NAGRI = 58;
    public static final int SYMBOLS = 129;
    public static final int SYRIAC = 34;
    public static final int TAGALOG = 42;
    public static final int TAGBANWA = 45;
    public static final int TAI_LE = 52;
    public static final int TAI_VIET = 127;
    public static final int TAKRI = 153;
    public static final int TAMIL = 35;
    public static final int TANGUT = 154;
    public static final int TELUGU = 36;
    public static final int TENGWAR = 98;
    public static final int THAANA = 37;
    public static final int THAI = 38;
    public static final int TIBETAN = 39;
    public static final int TIFINAGH = 60;
    public static final int TIRHUTA = 158;
    public static final int TRADITIONAL_HAN = 74;
    public static final int UCAS = 40;
    public static final int UGARITIC = 53;
    public static final int UNKNOWN = 103;
    public static final int UNWRITTEN_LANGUAGES = 102;
    public static final int VAI = 99;
    public static final int VISIBLE_SPEECH = 100;
    public static final int WARANG_CITI = 146;
    public static final int WESTERN_SYRIAC = 96;
    public static final int WOLEAI = 155;
    public static final int YI = 41;
    private static final ScriptUsage[] usageValues = null;

    private static final class ScriptMetadata {
        private static final int ASPIRATIONAL = 8388608;
        private static final int CASED = 67108864;
        private static final int EXCLUSION = 4194304;
        private static final int LB_LETTERS = 33554432;
        private static final int LIMITED_USE = 6291456;
        private static final int RECOMMENDED = 10485760;
        private static final int RTL = 16777216;
        private static final int[] SCRIPT_PROPS = null;
        private static final int UNKNOWN = 2097152;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.lang.UScript.ScriptMetadata.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.lang.UScript.ScriptMetadata.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.lang.UScript.ScriptMetadata.<clinit>():void");
        }

        private ScriptMetadata() {
        }

        private static final int getScriptProps(int script) {
            if (script < 0 || script >= SCRIPT_PROPS.length) {
                return UScript.COMMON;
            }
            return SCRIPT_PROPS[script];
        }
    }

    public enum ScriptUsage {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.lang.UScript.ScriptUsage.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.lang.UScript.ScriptUsage.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.lang.UScript.ScriptUsage.<clinit>():void");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.lang.UScript.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.lang.UScript.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.lang.UScript.<clinit>():void");
    }

    private static int[] getCodesFromLocale(ULocale locale) {
        String lang = locale.getLanguage();
        if (lang.equals("ja")) {
            return new int[]{KATAKANA, HIRAGANA, HAN};
        }
        if (lang.equals("ko")) {
            return new int[]{HANGUL, HAN};
        }
        String script = locale.getScript();
        if (lang.equals("zh") && script.equals("Hant")) {
            return new int[]{HAN, BOPOMOFO};
        }
        if (script.length() != 0) {
            int scriptCode = getCodeFromName(script);
            if (scriptCode != INVALID_CODE) {
                if (scriptCode == SIMPLIFIED_HAN || scriptCode == TRADITIONAL_HAN) {
                    scriptCode = HAN;
                }
                int[] iArr = new int[INHERITED];
                iArr[COMMON] = scriptCode;
                return iArr;
            }
        }
        return null;
    }

    private static int[] findCodeFromLocale(ULocale locale) {
        int[] result = getCodesFromLocale(locale);
        if (result != null) {
            return result;
        }
        return getCodesFromLocale(ULocale.addLikelySubtags(locale));
    }

    public static final int[] getCode(Locale locale) {
        return findCodeFromLocale(ULocale.forLocale(locale));
    }

    public static final int[] getCode(ULocale locale) {
        return findCodeFromLocale(locale);
    }

    public static final int[] getCode(String nameOrAbbrOrLocale) {
        int propNum;
        boolean triedCode = false;
        if (nameOrAbbrOrLocale.indexOf(ESTRANGELO_SYRIAC) < 0 && nameOrAbbrOrLocale.indexOf(TAGBANWA) < 0) {
            propNum = UCharacter.getPropertyValueEnumNoThrow(UProperty.SCRIPT, nameOrAbbrOrLocale);
            if (propNum != INVALID_CODE) {
                int[] iArr = new int[INHERITED];
                iArr[COMMON] = propNum;
                return iArr;
            }
            triedCode = true;
        }
        int[] scripts = findCodeFromLocale(new ULocale(nameOrAbbrOrLocale));
        if (scripts != null) {
            return scripts;
        }
        if (!triedCode) {
            propNum = UCharacter.getPropertyValueEnumNoThrow(UProperty.SCRIPT, nameOrAbbrOrLocale);
            if (propNum != INVALID_CODE) {
                iArr = new int[INHERITED];
                iArr[COMMON] = propNum;
                return iArr;
            }
        }
        return null;
    }

    public static final int getCodeFromName(String nameOrAbbr) {
        int propNum = UCharacter.getPropertyValueEnumNoThrow(UProperty.SCRIPT, nameOrAbbr);
        return propNum == INVALID_CODE ? INVALID_CODE : propNum;
    }

    public static final int getScript(int codepoint) {
        int i;
        if (codepoint >= 0) {
            i = INHERITED;
        } else {
            i = COMMON;
        }
        if (((codepoint <= UnicodeSet.MAX_VALUE ? INHERITED : COMMON) & i) != 0) {
            int scriptX = UCharacterProperty.INSTANCE.getAdditional(codepoint, COMMON) & UCharacterProperty.SCRIPT_X_MASK;
            if (scriptX < VMDebug.KIND_THREAD_CLASS_INIT_TIME) {
                return scriptX;
            }
            if (scriptX < UCharacterProperty.SCRIPT_X_WITH_INHERITED) {
                return COMMON;
            }
            if (scriptX < UCharacterProperty.SCRIPT_X_WITH_OTHER) {
                return INHERITED;
            }
            return UCharacterProperty.INSTANCE.m_scriptExtensions_[scriptX & Opcodes.OP_CONST_CLASS_JUMBO];
        }
        throw new IllegalArgumentException(Integer.toString(codepoint));
    }

    public static final boolean hasScript(int c, int sc) {
        boolean z = true;
        int scriptX = UCharacterProperty.INSTANCE.getAdditional(c, COMMON) & UCharacterProperty.SCRIPT_X_MASK;
        if (scriptX < VMDebug.KIND_THREAD_CLASS_INIT_TIME) {
            if (sc != scriptX) {
                z = false;
            }
            return z;
        }
        char[] scriptExtensions = UCharacterProperty.INSTANCE.m_scriptExtensions_;
        int scx = scriptX & Opcodes.OP_CONST_CLASS_JUMBO;
        if (scriptX >= UCharacterProperty.SCRIPT_X_WITH_OTHER) {
            scx = scriptExtensions[scx + INHERITED];
        }
        if (sc > 32767) {
            return false;
        }
        while (sc > scriptExtensions[scx]) {
            scx += INHERITED;
        }
        if (sc != (scriptExtensions[scx] & 32767)) {
            z = false;
        }
        return z;
    }

    public static final int getScriptExtensions(int c, BitSet set) {
        set.clear();
        int scriptX = UCharacterProperty.INSTANCE.getAdditional(c, COMMON) & UCharacterProperty.SCRIPT_X_MASK;
        if (scriptX < VMDebug.KIND_THREAD_CLASS_INIT_TIME) {
            set.set(scriptX);
            return scriptX;
        }
        char[] scriptExtensions = UCharacterProperty.INSTANCE.m_scriptExtensions_;
        int scx = scriptX & Opcodes.OP_CONST_CLASS_JUMBO;
        if (scriptX >= UCharacterProperty.SCRIPT_X_WITH_OTHER) {
            scx = scriptExtensions[scx + INHERITED];
        }
        int length = COMMON;
        while (true) {
            int scx2 = scx + INHERITED;
            int sx = scriptExtensions[scx];
            set.set(sx & 32767);
            length += INHERITED;
            if (sx >= DateUtilsBridge.FORMAT_ABBREV_WEEKDAY) {
                return -length;
            }
            scx = scx2;
        }
    }

    public static final String getName(int scriptCode) {
        return UCharacter.getPropertyValueName(UProperty.SCRIPT, scriptCode, INHERITED);
    }

    public static final String getShortName(int scriptCode) {
        return UCharacter.getPropertyValueName(UProperty.SCRIPT, scriptCode, COMMON);
    }

    public static final String getSampleString(int script) {
        int sampleChar = ScriptMetadata.getScriptProps(script) & DictionaryData.TRANSFORM_OFFSET_MASK;
        if (sampleChar != 0) {
            return sampleChar;
        }
        return XmlPullParser.NO_NAMESPACE;
    }

    public static final ScriptUsage getUsage(int script) {
        return usageValues[(ScriptMetadata.getScriptProps(script) >> KANNADA) & COPTIC];
    }

    public static final boolean isRightToLeft(int script) {
        return (ScriptMetadata.getScriptProps(script) & DictionaryData.TRANSFORM_TYPE_OFFSET) != 0;
    }

    public static final boolean breaksBetweenLetters(int script) {
        return (ScriptMetadata.getScriptProps(script) & 33554432) != 0;
    }

    public static final boolean isCased(int script) {
        return (ScriptMetadata.getScriptProps(script) & ArabicShaping.SPACES_RELATIVE_TO_TEXT_MASK) != 0;
    }

    private UScript() {
    }
}
