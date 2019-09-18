package android.icu.impl;

import android.icu.impl.ICUBinary;
import android.icu.impl.Normalizer2Impl;
import android.icu.impl.Trie2;
import android.icu.lang.UCharacter;
import android.icu.lang.UProperty;
import android.icu.lang.UScript;
import android.icu.text.Normalizer2;
import android.icu.text.UTF16;
import android.icu.text.UnicodeSet;
import android.icu.util.ICUException;
import android.icu.util.VersionInfo;
import dalvik.system.VMDebug;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.MissingResourceException;

public final class UCharacterProperty {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int AGE_SHIFT_ = 24;
    private static final int ALPHABETIC_PROPERTY_ = 8;
    private static final int ASCII_HEX_DIGIT_PROPERTY_ = 7;
    private static final int BLOCK_MASK_ = 130816;
    private static final int BLOCK_SHIFT_ = 8;
    private static final int CGJ = 847;
    private static final int CR = 13;
    private static final int DASH_PROPERTY_ = 1;
    private static final String DATA_FILE_NAME_ = "uprops.icu";
    private static final int DATA_FORMAT = 1431335535;
    private static final int DECOMPOSITION_TYPE_MASK_ = 31;
    private static final int DEFAULT_IGNORABLE_CODE_POINT_PROPERTY_ = 19;
    private static final int DEL = 127;
    private static final int DEPRECATED_PROPERTY_ = 20;
    private static final int DIACRITIC_PROPERTY_ = 10;
    private static final int EAST_ASIAN_MASK_ = 917504;
    private static final int EAST_ASIAN_SHIFT_ = 17;
    private static final int EXTENDER_PROPERTY_ = 11;
    private static final int FIGURESP = 8199;
    private static final int FIRST_NIBBLE_SHIFT_ = 4;
    private static final int GCB_MASK = 992;
    private static final int GCB_SHIFT = 5;
    private static final int GC_CC_MASK = getMask(15);
    private static final int GC_CN_MASK = getMask(0);
    private static final int GC_CS_MASK = getMask(18);
    private static final int GC_ZL_MASK = getMask(13);
    private static final int GC_ZP_MASK = getMask(14);
    private static final int GC_ZS_MASK = getMask(12);
    private static final int GC_Z_MASK = ((GC_ZS_MASK | GC_ZL_MASK) | GC_ZP_MASK);
    private static final int GRAPHEME_BASE_PROPERTY_ = 26;
    private static final int GRAPHEME_EXTEND_PROPERTY_ = 13;
    private static final int GRAPHEME_LINK_PROPERTY_ = 14;
    private static final int HAIRSP = 8202;
    private static final int HEX_DIGIT_PROPERTY_ = 6;
    private static final int HYPHEN_PROPERTY_ = 2;
    private static final int IDEOGRAPHIC_PROPERTY_ = 9;
    private static final int IDS_BINARY_OPERATOR_PROPERTY_ = 15;
    private static final int IDS_TRINARY_OPERATOR_PROPERTY_ = 16;
    private static final int ID_CONTINUE_PROPERTY_ = 25;
    private static final int ID_START_PROPERTY_ = 24;
    private static final int INHSWAP = 8298;
    public static final UCharacterProperty INSTANCE;
    private static final int LAST_NIBBLE_MASK_ = 15;
    public static final char LATIN_CAPITAL_LETTER_I_WITH_DOT_ABOVE_ = 'İ';
    public static final char LATIN_SMALL_LETTER_DOTLESS_I_ = 'ı';
    public static final char LATIN_SMALL_LETTER_I_ = 'i';
    private static final int LB_MASK = 66060288;
    private static final int LB_SHIFT = 20;
    private static final int LOGICAL_ORDER_EXCEPTION_PROPERTY_ = 21;
    private static final int MATH_PROPERTY_ = 5;
    static final int MY_MASK = 30;
    private static final int NBSP = 160;
    private static final int NL = 133;
    private static final int NNBSP = 8239;
    private static final int NOMDIG = 8303;
    private static final int NONCHARACTER_CODE_POINT_PROPERTY_ = 12;
    private static final int NTV_BASE60_START_ = 768;
    private static final int NTV_DECIMAL_START_ = 1;
    private static final int NTV_DIGIT_START_ = 11;
    private static final int NTV_FRACTION20_START_ = 804;
    private static final int NTV_FRACTION_START_ = 176;
    private static final int NTV_LARGE_START_ = 480;
    private static final int NTV_NONE_ = 0;
    private static final int NTV_NUMERIC_START_ = 21;
    private static final int NTV_RESERVED_START_ = 828;
    private static final int NUMERIC_TYPE_VALUE_SHIFT_ = 6;
    private static final int PATTERN_SYNTAX = 29;
    private static final int PATTERN_WHITE_SPACE = 30;
    private static final int PREPENDED_CONCATENATION_MARK = 31;
    private static final int PROPS_2_EMOJI = 28;
    private static final int PROPS_2_EMOJI_COMPONENT = 27;
    private static final int PROPS_2_EMOJI_MODIFIER = 30;
    private static final int PROPS_2_EMOJI_MODIFIER_BASE = 31;
    private static final int PROPS_2_EMOJI_PRESENTATION = 29;
    private static final int QUOTATION_MARK_PROPERTY_ = 3;
    private static final int RADICAL_PROPERTY_ = 17;
    private static final int RLM = 8207;
    private static final int SB_MASK = 1015808;
    private static final int SB_SHIFT = 15;
    public static final int SCRIPT_MASK_ = 255;
    public static final int SCRIPT_X_MASK = 12583167;
    public static final int SCRIPT_X_WITH_COMMON = 4194304;
    public static final int SCRIPT_X_WITH_INHERITED = 8388608;
    public static final int SCRIPT_X_WITH_OTHER = 12582912;
    public static final int SRC_BIDI = 5;
    public static final int SRC_CASE = 4;
    public static final int SRC_CASE_AND_NORM = 7;
    public static final int SRC_CHAR = 1;
    public static final int SRC_CHAR_AND_PROPSVEC = 6;
    public static final int SRC_COUNT = 12;
    public static final int SRC_NAMES = 3;
    public static final int SRC_NFC = 8;
    public static final int SRC_NFC_CANON_ITER = 11;
    public static final int SRC_NFKC = 9;
    public static final int SRC_NFKC_CF = 10;
    public static final int SRC_NONE = 0;
    public static final int SRC_PROPSVEC = 2;
    private static final int S_TERM_PROPERTY_ = 27;
    private static final int TAB = 9;
    private static final int TERMINAL_PUNCTUATION_PROPERTY_ = 4;
    public static final int TYPE_MASK = 31;
    private static final int UNIFIED_IDEOGRAPH_PROPERTY_ = 18;
    private static final int U_A = 65;
    private static final int U_F = 70;
    private static final int U_FW_A = 65313;
    private static final int U_FW_F = 65318;
    private static final int U_FW_Z = 65338;
    private static final int U_FW_a = 65345;
    private static final int U_FW_f = 65350;
    private static final int U_FW_z = 65370;
    private static final int U_Z = 90;
    private static final int U_a = 97;
    private static final int U_f = 102;
    private static final int U_z = 122;
    private static final int VARIATION_SELECTOR_PROPERTY_ = 28;
    private static final int WB_MASK = 31744;
    private static final int WB_SHIFT = 10;
    private static final int WHITE_SPACE_PROPERTY_ = 0;
    private static final int WJ = 8288;
    private static final int XID_CONTINUE_PROPERTY_ = 23;
    private static final int XID_START_PROPERTY_ = 22;
    private static final int ZWNBSP = 65279;
    /* access modifiers changed from: private */
    public static final int[] gcbToHst = {0, 0, 0, 0, 1, 0, 4, 5, 3, 2};
    BinaryProperty[] binProps = {new BinaryProperty(1, 256), new BinaryProperty(1, 128), new BinaryProperty(5) {
        /* access modifiers changed from: package-private */
        public boolean contains(int c) {
            return UBiDiProps.INSTANCE.isBidiControl(c);
        }
    }, new BinaryProperty(5) {
        /* access modifiers changed from: package-private */
        public boolean contains(int c) {
            return UBiDiProps.INSTANCE.isMirrored(c);
        }
    }, new BinaryProperty(1, 2), new BinaryProperty(1, 524288), new BinaryProperty(1, VMDebug.KIND_THREAD_GC_INVOCATIONS), new BinaryProperty(1, 1024), new BinaryProperty(1, 2048), new BinaryProperty(8) {
        /* access modifiers changed from: package-private */
        public boolean contains(int c) {
            Normalizer2Impl impl = Norm2AllModes.getNFCInstance().impl;
            return impl.isCompNo(impl.getNorm16(c));
        }
    }, new BinaryProperty(1, 67108864), new BinaryProperty(1, 8192), new BinaryProperty(1, 16384), new BinaryProperty(1, 64), new BinaryProperty(1, 4), new BinaryProperty(1, 33554432), new BinaryProperty(1, 16777216), new BinaryProperty(1, 512), new BinaryProperty(1, 32768), new BinaryProperty(1, 65536), new BinaryProperty(5) {
        /* access modifiers changed from: package-private */
        public boolean contains(int c) {
            return UBiDiProps.INSTANCE.isJoinControl(c);
        }
    }, new BinaryProperty(1, 2097152), new CaseBinaryProperty(22), new BinaryProperty(1, 32), new BinaryProperty(1, 4096), new BinaryProperty(1, 8), new BinaryProperty(1, 131072), new CaseBinaryProperty(27), new BinaryProperty(1, 16), new BinaryProperty(1, 262144), new CaseBinaryProperty(30), new BinaryProperty(1, 1), new BinaryProperty(1, SCRIPT_X_WITH_INHERITED), new BinaryProperty(1, 4194304), new CaseBinaryProperty(34), new BinaryProperty(1, 134217728), new BinaryProperty(1, VMDebug.KIND_THREAD_EXT_ALLOCATED_OBJECTS), new NormInertBinaryProperty(8, 37), new NormInertBinaryProperty(9, 38), new NormInertBinaryProperty(8, 39), new NormInertBinaryProperty(9, 40), new BinaryProperty(11) {
        /* access modifiers changed from: package-private */
        public boolean contains(int c) {
            return Norm2AllModes.getNFCInstance().impl.ensureCanonIterData().isCanonSegmentStarter(c);
        }
    }, new BinaryProperty(1, VMDebug.KIND_THREAD_EXT_ALLOCATED_BYTES), new BinaryProperty(1, VMDebug.KIND_THREAD_EXT_FREED_OBJECTS), new BinaryProperty(6) {
        /* access modifiers changed from: package-private */
        public boolean contains(int c) {
            return UCharacter.isUAlphabetic(c) || UCharacter.isDigit(c);
        }
    }, new BinaryProperty(1) {
        /* access modifiers changed from: package-private */
        public boolean contains(int c) {
            boolean z = true;
            if (c <= 159) {
                if (!(c == 9 || c == 32)) {
                    z = false;
                }
                return z;
            }
            if (UCharacter.getType(c) != 12) {
                z = false;
            }
            return z;
        }
    }, new BinaryProperty(1) {
        /* access modifiers changed from: package-private */
        public boolean contains(int c) {
            return UCharacterProperty.isgraphPOSIX(c);
        }
    }, new BinaryProperty(1) {
        /* access modifiers changed from: package-private */
        public boolean contains(int c) {
            return UCharacter.getType(c) == 12 || UCharacterProperty.isgraphPOSIX(c);
        }
    }, new BinaryProperty(1) {
        /* access modifiers changed from: package-private */
        public boolean contains(int c) {
            boolean z = true;
            if ((c <= 102 && c >= 65 && (c <= 70 || c >= 97)) || (c >= UCharacterProperty.U_FW_A && c <= UCharacterProperty.U_FW_f && (c <= UCharacterProperty.U_FW_F || c >= UCharacterProperty.U_FW_a))) {
                return true;
            }
            if (UCharacter.getType(c) != 9) {
                z = false;
            }
            return z;
        }
    }, new CaseBinaryProperty(49), new CaseBinaryProperty(50), new CaseBinaryProperty(51), new CaseBinaryProperty(52), new CaseBinaryProperty(53), new BinaryProperty(7) {
        /* access modifiers changed from: package-private */
        public boolean contains(int c) {
            String nfd = Norm2AllModes.getNFCInstance().impl.getDecomposition(c);
            boolean z = false;
            if (nfd != null) {
                c = nfd.codePointAt(0);
                if (Character.charCount(c) != nfd.length()) {
                    c = -1;
                }
            } else if (c < 0) {
                return false;
            }
            if (c < 0) {
                return true ^ UCharacter.foldCase(nfd, true).equals(nfd);
            }
            UCaseProps csp = UCaseProps.INSTANCE;
            UCaseProps.dummyStringBuilder.setLength(0);
            if (csp.toFullFolding(c, UCaseProps.dummyStringBuilder, 0) >= 0) {
                z = true;
            }
            return z;
        }
    }, new CaseBinaryProperty(55), new BinaryProperty(10) {
        /* access modifiers changed from: package-private */
        public boolean contains(int c) {
            Normalizer2Impl kcf = Norm2AllModes.getNFKC_CFInstance().impl;
            String src = UTF16.valueOf(c);
            StringBuilder dest = new StringBuilder();
            Normalizer2Impl normalizer2Impl = kcf;
            String str = src;
            normalizer2Impl.compose(str, 0, src.length(), false, true, new Normalizer2Impl.ReorderingBuffer(kcf, dest, 5));
            return !Normalizer2Impl.UTF16Plus.equal(dest, src);
        }
    }, new BinaryProperty(2, VMDebug.KIND_THREAD_EXT_ALLOCATED_OBJECTS), new BinaryProperty(2, VMDebug.KIND_THREAD_EXT_ALLOCATED_BYTES), new BinaryProperty(2, VMDebug.KIND_THREAD_EXT_FREED_OBJECTS), new BinaryProperty(2, Integer.MIN_VALUE), new BinaryProperty(2, 134217728), new BinaryProperty(2) {
        /* access modifiers changed from: package-private */
        public boolean contains(int c) {
            return 127462 <= c && c <= 127487;
        }
    }, new BinaryProperty(1, Integer.MIN_VALUE)};
    IntProperty[] intProps = {new BiDiIntProperty() {
        /* access modifiers changed from: package-private */
        public int getValue(int c) {
            return UBiDiProps.INSTANCE.getClass(c);
        }
    }, new IntProperty(0, BLOCK_MASK_, 8), new CombiningClassIntProperty(8) {
        /* access modifiers changed from: package-private */
        public int getValue(int c) {
            return Normalizer2.getNFDInstance().getCombiningClass(c);
        }
    }, new IntProperty(2, 31, 0), new IntProperty(0, 917504, 17), new IntProperty(1) {
        /* access modifiers changed from: package-private */
        public int getValue(int c) {
            return UCharacterProperty.this.getType(c);
        }

        /* access modifiers changed from: package-private */
        public int getMaxValue(int which) {
            return 29;
        }
    }, new BiDiIntProperty() {
        /* access modifiers changed from: package-private */
        public int getValue(int c) {
            return UBiDiProps.INSTANCE.getJoiningGroup(c);
        }
    }, new BiDiIntProperty() {
        /* access modifiers changed from: package-private */
        public int getValue(int c) {
            return UBiDiProps.INSTANCE.getJoiningType(c);
        }
    }, new IntProperty(2, LB_MASK, 20), new IntProperty(1) {
        /* access modifiers changed from: package-private */
        public int getValue(int c) {
            return UCharacterProperty.ntvGetType(UCharacterProperty.getNumericTypeValue(UCharacterProperty.this.getProperty(c)));
        }

        /* access modifiers changed from: package-private */
        public int getMaxValue(int which) {
            return 3;
        }
    }, new IntProperty(0, 255, 0) {
        /* access modifiers changed from: package-private */
        public int getValue(int c) {
            return UScript.getScript(c);
        }
    }, new IntProperty(2) {
        /* access modifiers changed from: package-private */
        public int getValue(int c) {
            int gcb = (UCharacterProperty.this.getAdditional(c, 2) & UCharacterProperty.GCB_MASK) >>> 5;
            if (gcb < UCharacterProperty.gcbToHst.length) {
                return UCharacterProperty.gcbToHst[gcb];
            }
            return 0;
        }

        /* access modifiers changed from: package-private */
        public int getMaxValue(int which) {
            return 5;
        }
    }, new NormQuickCheckIntProperty(8, UProperty.NFD_QUICK_CHECK, 1), new NormQuickCheckIntProperty(9, UProperty.NFKD_QUICK_CHECK, 1), new NormQuickCheckIntProperty(8, UProperty.NFC_QUICK_CHECK, 2), new NormQuickCheckIntProperty(9, UProperty.NFKC_QUICK_CHECK, 2), new CombiningClassIntProperty(8) {
        /* access modifiers changed from: package-private */
        public int getValue(int c) {
            return Norm2AllModes.getNFCInstance().impl.getFCD16(c) >> 8;
        }
    }, new CombiningClassIntProperty(8) {
        /* access modifiers changed from: package-private */
        public int getValue(int c) {
            return Norm2AllModes.getNFCInstance().impl.getFCD16(c) & 255;
        }
    }, new IntProperty(2, GCB_MASK, 5), new IntProperty(2, SB_MASK, 15), new IntProperty(2, WB_MASK, 10), new BiDiIntProperty() {
        /* access modifiers changed from: package-private */
        public int getValue(int c) {
            return UBiDiProps.INSTANCE.getPairedBracketType(c);
        }
    }};
    int m_additionalColumnsCount_;
    Trie2_16 m_additionalTrie_;
    int[] m_additionalVectors_;
    int m_maxBlockScriptValue_;
    int m_maxJTGValue_;
    public char[] m_scriptExtensions_;
    public Trie2_16 m_trie_;
    public VersionInfo m_unicodeVersion_;

    private class BiDiIntProperty extends IntProperty {
        BiDiIntProperty() {
            super(5);
        }

        /* access modifiers changed from: package-private */
        public int getMaxValue(int which) {
            return UBiDiProps.INSTANCE.getMaxValue(which);
        }
    }

    private class BinaryProperty {
        int column;
        int mask;

        BinaryProperty(int column2, int mask2) {
            this.column = column2;
            this.mask = mask2;
        }

        BinaryProperty(int source) {
            this.column = source;
            this.mask = 0;
        }

        /* access modifiers changed from: package-private */
        public final int getSource() {
            if (this.mask == 0) {
                return this.column;
            }
            return 2;
        }

        /* access modifiers changed from: package-private */
        public boolean contains(int c) {
            return (UCharacterProperty.this.getAdditional(c, this.column) & this.mask) != 0;
        }
    }

    private class CaseBinaryProperty extends BinaryProperty {
        int which;

        CaseBinaryProperty(int which2) {
            super(4);
            this.which = which2;
        }

        /* access modifiers changed from: package-private */
        public boolean contains(int c) {
            return UCaseProps.INSTANCE.hasBinaryProperty(c, this.which);
        }
    }

    private class CombiningClassIntProperty extends IntProperty {
        CombiningClassIntProperty(int source) {
            super(source);
        }

        /* access modifiers changed from: package-private */
        public int getMaxValue(int which) {
            return 255;
        }
    }

    private class IntProperty {
        int column;
        int mask;
        int shift;

        IntProperty(int column2, int mask2, int shift2) {
            this.column = column2;
            this.mask = mask2;
            this.shift = shift2;
        }

        IntProperty(int source) {
            this.column = source;
            this.mask = 0;
        }

        /* access modifiers changed from: package-private */
        public final int getSource() {
            if (this.mask == 0) {
                return this.column;
            }
            return 2;
        }

        /* access modifiers changed from: package-private */
        public int getValue(int c) {
            return (UCharacterProperty.this.getAdditional(c, this.column) & this.mask) >>> this.shift;
        }

        /* access modifiers changed from: package-private */
        public int getMaxValue(int which) {
            return (UCharacterProperty.this.getMaxValues(this.column) & this.mask) >>> this.shift;
        }
    }

    private static final class IsAcceptable implements ICUBinary.Authenticate {
        private IsAcceptable() {
        }

        public boolean isDataVersionAcceptable(byte[] version) {
            return version[0] == 7;
        }
    }

    private class NormInertBinaryProperty extends BinaryProperty {
        int which;

        NormInertBinaryProperty(int source, int which2) {
            super(source);
            this.which = which2;
        }

        /* access modifiers changed from: package-private */
        public boolean contains(int c) {
            return Norm2AllModes.getN2WithImpl(this.which - 37).isInert(c);
        }
    }

    private class NormQuickCheckIntProperty extends IntProperty {
        int max;
        int which;

        NormQuickCheckIntProperty(int source, int which2, int max2) {
            super(source);
            this.which = which2;
            this.max = max2;
        }

        /* access modifiers changed from: package-private */
        public int getValue(int c) {
            return Norm2AllModes.getN2WithImpl(this.which - 4108).getQuickCheck(c);
        }

        /* access modifiers changed from: package-private */
        public int getMaxValue(int which2) {
            return this.max;
        }
    }

    static {
        try {
            INSTANCE = new UCharacterProperty();
        } catch (IOException e) {
            throw new MissingResourceException(e.getMessage(), "", "");
        }
    }

    public final int getProperty(int ch) {
        return this.m_trie_.get(ch);
    }

    public int getAdditional(int codepoint, int column) {
        if (column >= this.m_additionalColumnsCount_) {
            return 0;
        }
        return this.m_additionalVectors_[this.m_additionalTrie_.get(codepoint) + column];
    }

    public VersionInfo getAge(int codepoint) {
        int version = getAdditional(codepoint, 0) >> 24;
        return VersionInfo.getInstance((version >> 4) & 15, version & 15, 0, 0);
    }

    /* access modifiers changed from: private */
    public static final boolean isgraphPOSIX(int c) {
        return (getMask(UCharacter.getType(c)) & (((GC_CC_MASK | GC_CS_MASK) | GC_CN_MASK) | GC_Z_MASK)) == 0;
    }

    public boolean hasBinaryProperty(int c, int which) {
        if (which < 0 || 64 <= which) {
            return false;
        }
        return this.binProps[which].contains(c);
    }

    public int getType(int c) {
        return getProperty(c) & 31;
    }

    public int getIntPropertyValue(int c, int which) {
        if (which < 4096) {
            if (which >= 0 && which < 64) {
                return this.binProps[which].contains(c) ? 1 : 0;
            }
        } else if (which < 4118) {
            return this.intProps[which - 4096].getValue(c);
        } else {
            if (which == 8192) {
                return getMask(getType(c));
            }
        }
        return 0;
    }

    public int getIntPropertyMaxValue(int which) {
        if (which < 4096) {
            if (which >= 0 && which < 64) {
                return 1;
            }
        } else if (which < 4118) {
            return this.intProps[which - 4096].getMaxValue(which);
        }
        return -1;
    }

    public final int getSource(int which) {
        if (which < 0) {
            return 0;
        }
        if (which < 64) {
            return this.binProps[which].getSource();
        }
        if (which < 4096) {
            return 0;
        }
        if (which < 4118) {
            return this.intProps[which - 4096].getSource();
        }
        if (which < 16384) {
            if (which == 8192 || which == 12288) {
                return 1;
            }
            return 0;
        } else if (which < 16398) {
            switch (which) {
                case 16384:
                    return 2;
                case UProperty.BIDI_MIRRORING_GLYPH /*16385*/:
                    return 5;
                case UProperty.CASE_FOLDING /*16386*/:
                case UProperty.LOWERCASE_MAPPING /*16388*/:
                case UProperty.SIMPLE_CASE_FOLDING /*16390*/:
                case UProperty.SIMPLE_LOWERCASE_MAPPING /*16391*/:
                case UProperty.SIMPLE_TITLECASE_MAPPING /*16392*/:
                case UProperty.SIMPLE_UPPERCASE_MAPPING /*16393*/:
                case UProperty.TITLECASE_MAPPING /*16394*/:
                case UProperty.UPPERCASE_MAPPING /*16396*/:
                    return 4;
                case UProperty.ISO_COMMENT /*16387*/:
                case UProperty.NAME /*16389*/:
                case UProperty.UNICODE_1_NAME /*16395*/:
                    return 3;
                default:
                    return 0;
            }
        } else if (which != 28672) {
            return 0;
        } else {
            return 2;
        }
    }

    public int getMaxValues(int column) {
        if (column == 0) {
            return this.m_maxBlockScriptValue_;
        }
        if (column != 2) {
            return 0;
        }
        return this.m_maxJTGValue_;
    }

    public static final int getMask(int type) {
        return 1 << type;
    }

    public static int getEuropeanDigit(int ch) {
        if (ch <= 122 || ch >= U_FW_A) {
            int i = 65;
            if (ch >= 65 && ((ch <= 90 || ch >= 97) && ch <= U_FW_z && (ch <= U_FW_Z || ch >= U_FW_a))) {
                if (ch <= 122) {
                    int i2 = ch + 10;
                    if (ch > 90) {
                        i = 97;
                    }
                    return i2 - i;
                } else if (ch <= U_FW_Z) {
                    return (ch + 10) - U_FW_A;
                } else {
                    return (ch + 10) - U_FW_a;
                }
            }
        }
        return -1;
    }

    public int digit(int c) {
        int value = getNumericTypeValue(getProperty(c)) - 1;
        if (value <= 9) {
            return value;
        }
        return -1;
    }

    public int getNumericValue(int c) {
        int ntv = getNumericTypeValue(getProperty(c));
        if (ntv == 0) {
            return getEuropeanDigit(c);
        }
        if (ntv < 11) {
            return ntv - 1;
        }
        if (ntv < 21) {
            return ntv - 11;
        }
        if (ntv < 176) {
            return ntv - 21;
        }
        if (ntv < NTV_LARGE_START_) {
            return -2;
        }
        if (ntv < 768) {
            int mant = (ntv >> 5) - 14;
            int exp = (ntv & 31) + 2;
            if (exp >= 9 && (exp != 9 || mant > 2)) {
                return -2;
            }
            int numValue = mant;
            do {
                numValue *= 10;
                exp--;
            } while (exp > 0);
            return numValue;
        } else if (ntv >= NTV_FRACTION20_START_) {
            return ntv < NTV_RESERVED_START_ ? -2 : -2;
        } else {
            int numValue2 = (ntv >> 2) - 191;
            switch ((ntv & 3) + 1) {
                case 1:
                    numValue2 *= 60;
                    break;
                case 2:
                    numValue2 *= 3600;
                    break;
                case 3:
                    numValue2 *= 216000;
                    break;
                case 4:
                    numValue2 *= 12960000;
                    break;
            }
            return numValue2;
        }
    }

    public double getUnicodeNumericValue(int c) {
        int ntv = getNumericTypeValue(getProperty(c));
        if (ntv == 0) {
            return -1.23456789E8d;
        }
        if (ntv < 11) {
            return (double) (ntv - 1);
        }
        if (ntv < 21) {
            return (double) (ntv - 11);
        }
        if (ntv < 176) {
            return (double) (ntv - 21);
        }
        if (ntv < NTV_LARGE_START_) {
            return ((double) ((ntv >> 4) - 12)) / ((double) ((ntv & 15) + 1));
        }
        if (ntv < 768) {
            int exp = (ntv & 31) + 2;
            double numValue = (double) ((ntv >> 5) - 14);
            while (exp >= 4) {
                numValue *= 10000.0d;
                exp -= 4;
            }
            switch (exp) {
                case 1:
                    numValue *= 10.0d;
                    break;
                case 2:
                    numValue *= 100.0d;
                    break;
                case 3:
                    numValue *= 1000.0d;
                    break;
            }
            return numValue;
        } else if (ntv < NTV_FRACTION20_START_) {
            int numValue2 = (ntv >> 2) - 191;
            switch ((ntv & 3) + 1) {
                case 1:
                    numValue2 *= 60;
                    break;
                case 2:
                    numValue2 *= 3600;
                    break;
                case 3:
                    numValue2 *= 216000;
                    break;
                case 4:
                    numValue2 *= 12960000;
                    break;
            }
            return (double) numValue2;
        } else if (ntv >= NTV_RESERVED_START_) {
            return -1.23456789E8d;
        } else {
            int frac20 = ntv - 804;
            return ((double) ((2 * (frac20 & 3)) + 1)) / ((double) (20 << (frac20 >> 2)));
        }
    }

    /* access modifiers changed from: private */
    public static final int getNumericTypeValue(int props) {
        return props >> 6;
    }

    /* access modifiers changed from: private */
    public static final int ntvGetType(int ntv) {
        if (ntv == 0) {
            return 0;
        }
        if (ntv < 11) {
            return 1;
        }
        if (ntv < 21) {
            return 2;
        }
        return 3;
    }

    private UCharacterProperty() throws IOException {
        if (this.binProps.length != 64) {
            throw new ICUException("binProps.length!=UProperty.BINARY_LIMIT");
        } else if (this.intProps.length == 22) {
            ByteBuffer bytes = ICUBinary.getRequiredData(DATA_FILE_NAME_);
            this.m_unicodeVersion_ = ICUBinary.readHeaderAndDataVersion(bytes, DATA_FORMAT, new IsAcceptable());
            int propertyOffset = bytes.getInt();
            bytes.getInt();
            bytes.getInt();
            int additionalOffset = bytes.getInt();
            int additionalVectorsOffset = bytes.getInt();
            this.m_additionalColumnsCount_ = bytes.getInt();
            int scriptExtensionsOffset = bytes.getInt();
            int reservedOffset7 = bytes.getInt();
            bytes.getInt();
            bytes.getInt();
            this.m_maxBlockScriptValue_ = bytes.getInt();
            this.m_maxJTGValue_ = bytes.getInt();
            ICUBinary.skipBytes(bytes, 16);
            this.m_trie_ = Trie2_16.createFromSerialized(bytes);
            int expectedTrieLength = (propertyOffset - 16) * 4;
            int trieLength = this.m_trie_.getSerializedLength();
            if (trieLength <= expectedTrieLength) {
                ICUBinary.skipBytes(bytes, expectedTrieLength - trieLength);
                ICUBinary.skipBytes(bytes, (additionalOffset - propertyOffset) * 4);
                if (this.m_additionalColumnsCount_ > 0) {
                    this.m_additionalTrie_ = Trie2_16.createFromSerialized(bytes);
                    int expectedTrieLength2 = (additionalVectorsOffset - additionalOffset) * 4;
                    int trieLength2 = this.m_additionalTrie_.getSerializedLength();
                    if (trieLength2 <= expectedTrieLength2) {
                        ICUBinary.skipBytes(bytes, expectedTrieLength2 - trieLength2);
                        this.m_additionalVectors_ = ICUBinary.getInts(bytes, scriptExtensionsOffset - additionalVectorsOffset, 0);
                    } else {
                        throw new IOException("uprops.icu: not enough bytes for additional-properties trie");
                    }
                }
                int numChars = (reservedOffset7 - scriptExtensionsOffset) * 2;
                if (numChars > 0) {
                    this.m_scriptExtensions_ = ICUBinary.getChars(bytes, numChars, 0);
                    return;
                }
                return;
            }
            throw new IOException("uprops.icu: not enough bytes for main trie");
        } else {
            throw new ICUException("intProps.length!=(UProperty.INT_LIMIT-UProperty.INT_START)");
        }
    }

    public UnicodeSet addPropertyStarts(UnicodeSet set) {
        Iterator<Trie2.Range> trieIterator = this.m_trie_.iterator();
        while (trieIterator.hasNext()) {
            Trie2.Range next = trieIterator.next();
            Trie2.Range range = next;
            if (next.leadSurrogate) {
                break;
            }
            set.add(range.startCodePoint);
        }
        set.add(9);
        set.add(10);
        set.add(14);
        set.add(28);
        set.add(32);
        set.add(133);
        set.add(134);
        set.add(127);
        set.add((int) HAIRSP);
        set.add(8208);
        set.add((int) INHSWAP);
        set.add(8304);
        set.add((int) ZWNBSP);
        set.add(65280);
        set.add(160);
        set.add(161);
        set.add((int) FIGURESP);
        set.add(8200);
        set.add((int) NNBSP);
        set.add(8240);
        set.add(12295);
        set.add(12296);
        set.add(19968);
        set.add(19969);
        set.add(20108);
        set.add(20109);
        set.add(19977);
        set.add(19978);
        set.add(22235);
        set.add(22236);
        set.add(20116);
        set.add(20117);
        set.add(20845);
        set.add(20846);
        set.add(19971);
        set.add(19972);
        set.add(20843);
        set.add(20844);
        set.add(20061);
        set.add(20062);
        set.add(97);
        set.add(123);
        set.add(65);
        set.add(91);
        set.add((int) U_FW_a);
        set.add(65371);
        set.add((int) U_FW_A);
        set.add(65339);
        set.add(103);
        set.add(71);
        set.add(65351);
        set.add(65319);
        set.add((int) WJ);
        set.add(65520);
        set.add(65532);
        set.add(917504);
        set.add(921600);
        set.add((int) CGJ);
        set.add(848);
        return set;
    }

    public void upropsvec_addPropertyStarts(UnicodeSet set) {
        if (this.m_additionalColumnsCount_ > 0) {
            Iterator<Trie2.Range> trieIterator = this.m_additionalTrie_.iterator();
            while (trieIterator.hasNext()) {
                Trie2.Range next = trieIterator.next();
                Trie2.Range range = next;
                if (!next.leadSurrogate) {
                    set.add(range.startCodePoint);
                } else {
                    return;
                }
            }
        }
    }
}
