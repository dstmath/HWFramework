package android.icu.impl;

import android.icu.impl.ICUBinary.Authenticate;
import android.icu.impl.Normalizer2Impl.ReorderingBuffer;
import android.icu.impl.Normalizer2Impl.UTF16Plus;
import android.icu.impl.Trie2.Range;
import android.icu.lang.UCharacter;
import android.icu.lang.UProperty;
import android.icu.lang.UScript;
import android.icu.text.ArabicShaping;
import android.icu.text.MessagePattern;
import android.icu.text.Normalizer2;
import android.icu.text.UTF16;
import android.icu.text.UnicodeSet;
import android.icu.util.ICUException;
import android.icu.util.VersionInfo;
import dalvik.bytecode.Opcodes;
import dalvik.system.VMDebug;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import libcore.icu.DateUtilsBridge;
import org.w3c.dom.traversal.NodeFilter;

public final class UCharacterProperty {
    static final /* synthetic */ boolean -assertionsDisabled = false;
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
    private static final int GC_CC_MASK = 0;
    private static final int GC_CN_MASK = 0;
    private static final int GC_CS_MASK = 0;
    private static final int GC_ZL_MASK = 0;
    private static final int GC_ZP_MASK = 0;
    private static final int GC_ZS_MASK = 0;
    private static final int GC_Z_MASK = 0;
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
    public static final UCharacterProperty INSTANCE = null;
    private static final int LAST_NIBBLE_MASK_ = 15;
    public static final char LATIN_CAPITAL_LETTER_I_WITH_DOT_ABOVE_ = '\u0130';
    public static final char LATIN_SMALL_LETTER_DOTLESS_I_ = '\u0131';
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
    private static final int NTV_FRACTION_START_ = 176;
    private static final int NTV_LARGE_START_ = 480;
    private static final int NTV_NONE_ = 0;
    private static final int NTV_NUMERIC_START_ = 21;
    private static final int NTV_RESERVED_START_ = 804;
    private static final int NUMERIC_TYPE_VALUE_SHIFT_ = 6;
    private static final int PATTERN_SYNTAX = 29;
    private static final int PATTERN_WHITE_SPACE = 30;
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
    private static final int[] gcbToHst = null;
    BinaryProperty[] binProps;
    IntProperty[] intProps;
    int m_additionalColumnsCount_;
    Trie2_16 m_additionalTrie_;
    int[] m_additionalVectors_;
    int m_maxBlockScriptValue_;
    int m_maxJTGValue_;
    public char[] m_scriptExtensions_;
    public Trie2_16 m_trie_;
    public VersionInfo m_unicodeVersion_;

    private class BinaryProperty {
        int column;
        int mask;

        BinaryProperty(int column, int mask) {
            this.column = column;
            this.mask = mask;
        }

        BinaryProperty(int source) {
            this.column = source;
            this.mask = UCharacterProperty.WHITE_SPACE_PROPERTY_;
        }

        final int getSource() {
            return this.mask == 0 ? this.column : UCharacterProperty.SRC_PROPSVEC;
        }

        boolean contains(int c) {
            return (UCharacterProperty.this.getAdditional(c, this.column) & this.mask) != 0 ? true : UCharacterProperty.-assertionsDisabled;
        }
    }

    /* renamed from: android.icu.impl.UCharacterProperty.10 */
    class AnonymousClass10 extends BinaryProperty {
        final /* synthetic */ UCharacterProperty this$0;

        AnonymousClass10(UCharacterProperty this$0, UCharacterProperty this$0_1, int $anonymous0) {
            this.this$0 = this$0_1;
            super($anonymous0);
        }

        boolean contains(int c) {
            boolean z = true;
            if ((c <= UCharacterProperty.U_f && c >= UCharacterProperty.U_A && (c <= UCharacterProperty.U_F || c >= UCharacterProperty.U_a)) || (c >= UCharacterProperty.U_FW_A && c <= UCharacterProperty.U_FW_f && (c <= UCharacterProperty.U_FW_F || c >= UCharacterProperty.U_FW_a))) {
                return true;
            }
            if (UCharacter.getType(c) != UCharacterProperty.TAB) {
                z = UCharacterProperty.-assertionsDisabled;
            }
            return z;
        }
    }

    /* renamed from: android.icu.impl.UCharacterProperty.11 */
    class AnonymousClass11 extends BinaryProperty {
        final /* synthetic */ UCharacterProperty this$0;

        AnonymousClass11(UCharacterProperty this$0, UCharacterProperty this$0_1, int $anonymous0) {
            this.this$0 = this$0_1;
            super($anonymous0);
        }

        boolean contains(int c) {
            boolean z = true;
            boolean z2 = UCharacterProperty.-assertionsDisabled;
            String nfd = Norm2AllModes.getNFCInstance().impl.getDecomposition(c);
            if (nfd != null) {
                c = nfd.codePointAt(UCharacterProperty.WHITE_SPACE_PROPERTY_);
                if (Character.charCount(c) != nfd.length()) {
                    c = -1;
                }
            } else if (c < 0) {
                return UCharacterProperty.-assertionsDisabled;
            }
            if (c >= 0) {
                UCaseProps csp = UCaseProps.INSTANCE;
                UCaseProps.dummyStringBuilder.setLength(UCharacterProperty.WHITE_SPACE_PROPERTY_);
                if (csp.toFullFolding(c, UCaseProps.dummyStringBuilder, UCharacterProperty.WHITE_SPACE_PROPERTY_) < 0) {
                    z = UCharacterProperty.-assertionsDisabled;
                }
                return z;
            }
            if (!UCharacter.foldCase(nfd, true).equals(nfd)) {
                z2 = true;
            }
            return z2;
        }
    }

    /* renamed from: android.icu.impl.UCharacterProperty.12 */
    class AnonymousClass12 extends BinaryProperty {
        final /* synthetic */ UCharacterProperty this$0;

        AnonymousClass12(UCharacterProperty this$0, UCharacterProperty this$0_1, int $anonymous0) {
            this.this$0 = this$0_1;
            super($anonymous0);
        }

        boolean contains(int c) {
            Normalizer2Impl kcf = Norm2AllModes.getNFKC_CFInstance().impl;
            String src = UTF16.valueOf(c);
            StringBuilder dest = new StringBuilder();
            kcf.compose(src, UCharacterProperty.WHITE_SPACE_PROPERTY_, src.length(), UCharacterProperty.-assertionsDisabled, true, new ReorderingBuffer(kcf, dest, UCharacterProperty.SRC_BIDI));
            if (UTF16Plus.equal(dest, src)) {
                return UCharacterProperty.-assertionsDisabled;
            }
            return true;
        }
    }

    private class IntProperty {
        int column;
        int mask;
        int shift;

        IntProperty(int column, int mask, int shift) {
            this.column = column;
            this.mask = mask;
            this.shift = shift;
        }

        IntProperty(int source) {
            this.column = source;
            this.mask = UCharacterProperty.WHITE_SPACE_PROPERTY_;
        }

        final int getSource() {
            return this.mask == 0 ? this.column : UCharacterProperty.SRC_PROPSVEC;
        }

        int getValue(int c) {
            return (UCharacterProperty.this.getAdditional(c, this.column) & this.mask) >>> this.shift;
        }

        int getMaxValue(int which) {
            return (UCharacterProperty.this.getMaxValues(this.column) & this.mask) >>> this.shift;
        }
    }

    private class BiDiIntProperty extends IntProperty {
        BiDiIntProperty() {
            super(UCharacterProperty.SRC_BIDI);
        }

        int getMaxValue(int which) {
            return UBiDiProps.INSTANCE.getMaxValue(which);
        }
    }

    /* renamed from: android.icu.impl.UCharacterProperty.13 */
    class AnonymousClass13 extends BiDiIntProperty {
        final /* synthetic */ UCharacterProperty this$0;

        AnonymousClass13(UCharacterProperty this$0, UCharacterProperty this$0_1) {
            this.this$0 = this$0_1;
            super();
        }

        int getValue(int c) {
            return UBiDiProps.INSTANCE.getClass(c);
        }
    }

    private class CombiningClassIntProperty extends IntProperty {
        CombiningClassIntProperty(int source) {
            super(source);
        }

        int getMaxValue(int which) {
            return UCharacterProperty.SCRIPT_MASK_;
        }
    }

    /* renamed from: android.icu.impl.UCharacterProperty.14 */
    class AnonymousClass14 extends CombiningClassIntProperty {
        final /* synthetic */ UCharacterProperty this$0;

        AnonymousClass14(UCharacterProperty this$0, UCharacterProperty this$0_1, int $anonymous0) {
            this.this$0 = this$0_1;
            super($anonymous0);
        }

        int getValue(int c) {
            return Normalizer2.getNFDInstance().getCombiningClass(c);
        }
    }

    /* renamed from: android.icu.impl.UCharacterProperty.15 */
    class AnonymousClass15 extends IntProperty {
        final /* synthetic */ UCharacterProperty this$0;

        AnonymousClass15(UCharacterProperty this$0, UCharacterProperty this$0_1, int $anonymous0) {
            this.this$0 = this$0_1;
            super($anonymous0);
        }

        int getValue(int c) {
            return this.this$0.getType(c);
        }

        int getMaxValue(int which) {
            return UCharacterProperty.PATTERN_SYNTAX;
        }
    }

    /* renamed from: android.icu.impl.UCharacterProperty.16 */
    class AnonymousClass16 extends BiDiIntProperty {
        final /* synthetic */ UCharacterProperty this$0;

        AnonymousClass16(UCharacterProperty this$0, UCharacterProperty this$0_1) {
            this.this$0 = this$0_1;
            super();
        }

        int getValue(int c) {
            return UBiDiProps.INSTANCE.getJoiningGroup(c);
        }
    }

    /* renamed from: android.icu.impl.UCharacterProperty.17 */
    class AnonymousClass17 extends BiDiIntProperty {
        final /* synthetic */ UCharacterProperty this$0;

        AnonymousClass17(UCharacterProperty this$0, UCharacterProperty this$0_1) {
            this.this$0 = this$0_1;
            super();
        }

        int getValue(int c) {
            return UBiDiProps.INSTANCE.getJoiningType(c);
        }
    }

    /* renamed from: android.icu.impl.UCharacterProperty.18 */
    class AnonymousClass18 extends IntProperty {
        final /* synthetic */ UCharacterProperty this$0;

        AnonymousClass18(UCharacterProperty this$0, UCharacterProperty this$0_1, int $anonymous0) {
            this.this$0 = this$0_1;
            super($anonymous0);
        }

        int getValue(int c) {
            return UCharacterProperty.ntvGetType(UCharacterProperty.getNumericTypeValue(this.this$0.getProperty(c)));
        }

        int getMaxValue(int which) {
            return UCharacterProperty.SRC_NAMES;
        }
    }

    /* renamed from: android.icu.impl.UCharacterProperty.19 */
    class AnonymousClass19 extends IntProperty {
        final /* synthetic */ UCharacterProperty this$0;

        AnonymousClass19(UCharacterProperty this$0, UCharacterProperty this$0_1, int $anonymous0, int $anonymous1, int $anonymous2) {
            this.this$0 = this$0_1;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        int getValue(int c) {
            return UScript.getScript(c);
        }
    }

    /* renamed from: android.icu.impl.UCharacterProperty.1 */
    class AnonymousClass1 extends BinaryProperty {
        final /* synthetic */ UCharacterProperty this$0;

        AnonymousClass1(UCharacterProperty this$0, UCharacterProperty this$0_1, int $anonymous0) {
            this.this$0 = this$0_1;
            super($anonymous0);
        }

        boolean contains(int c) {
            return UBiDiProps.INSTANCE.isBidiControl(c);
        }
    }

    /* renamed from: android.icu.impl.UCharacterProperty.20 */
    class AnonymousClass20 extends IntProperty {
        final /* synthetic */ UCharacterProperty this$0;

        AnonymousClass20(UCharacterProperty this$0, UCharacterProperty this$0_1, int $anonymous0) {
            this.this$0 = this$0_1;
            super($anonymous0);
        }

        int getValue(int c) {
            int gcb = (this.this$0.getAdditional(c, UCharacterProperty.SRC_PROPSVEC) & UCharacterProperty.GCB_MASK) >>> UCharacterProperty.SRC_BIDI;
            if (gcb < UCharacterProperty.gcbToHst.length) {
                return UCharacterProperty.gcbToHst[gcb];
            }
            return UCharacterProperty.WHITE_SPACE_PROPERTY_;
        }

        int getMaxValue(int which) {
            return UCharacterProperty.SRC_BIDI;
        }
    }

    /* renamed from: android.icu.impl.UCharacterProperty.21 */
    class AnonymousClass21 extends CombiningClassIntProperty {
        final /* synthetic */ UCharacterProperty this$0;

        AnonymousClass21(UCharacterProperty this$0, UCharacterProperty this$0_1, int $anonymous0) {
            this.this$0 = this$0_1;
            super($anonymous0);
        }

        int getValue(int c) {
            return Norm2AllModes.getNFCInstance().impl.getFCD16(c) >> UCharacterProperty.SRC_NFC;
        }
    }

    /* renamed from: android.icu.impl.UCharacterProperty.22 */
    class AnonymousClass22 extends CombiningClassIntProperty {
        final /* synthetic */ UCharacterProperty this$0;

        AnonymousClass22(UCharacterProperty this$0, UCharacterProperty this$0_1, int $anonymous0) {
            this.this$0 = this$0_1;
            super($anonymous0);
        }

        int getValue(int c) {
            return Norm2AllModes.getNFCInstance().impl.getFCD16(c) & UCharacterProperty.SCRIPT_MASK_;
        }
    }

    /* renamed from: android.icu.impl.UCharacterProperty.23 */
    class AnonymousClass23 extends BiDiIntProperty {
        final /* synthetic */ UCharacterProperty this$0;

        AnonymousClass23(UCharacterProperty this$0, UCharacterProperty this$0_1) {
            this.this$0 = this$0_1;
            super();
        }

        int getValue(int c) {
            return UBiDiProps.INSTANCE.getPairedBracketType(c);
        }
    }

    /* renamed from: android.icu.impl.UCharacterProperty.2 */
    class AnonymousClass2 extends BinaryProperty {
        final /* synthetic */ UCharacterProperty this$0;

        AnonymousClass2(UCharacterProperty this$0, UCharacterProperty this$0_1, int $anonymous0) {
            this.this$0 = this$0_1;
            super($anonymous0);
        }

        boolean contains(int c) {
            return UBiDiProps.INSTANCE.isMirrored(c);
        }
    }

    /* renamed from: android.icu.impl.UCharacterProperty.3 */
    class AnonymousClass3 extends BinaryProperty {
        final /* synthetic */ UCharacterProperty this$0;

        AnonymousClass3(UCharacterProperty this$0, UCharacterProperty this$0_1, int $anonymous0) {
            this.this$0 = this$0_1;
            super($anonymous0);
        }

        boolean contains(int c) {
            Normalizer2Impl impl = Norm2AllModes.getNFCInstance().impl;
            return impl.isCompNo(impl.getNorm16(c));
        }
    }

    /* renamed from: android.icu.impl.UCharacterProperty.4 */
    class AnonymousClass4 extends BinaryProperty {
        final /* synthetic */ UCharacterProperty this$0;

        AnonymousClass4(UCharacterProperty this$0, UCharacterProperty this$0_1, int $anonymous0) {
            this.this$0 = this$0_1;
            super($anonymous0);
        }

        boolean contains(int c) {
            return UBiDiProps.INSTANCE.isJoinControl(c);
        }
    }

    /* renamed from: android.icu.impl.UCharacterProperty.5 */
    class AnonymousClass5 extends BinaryProperty {
        final /* synthetic */ UCharacterProperty this$0;

        AnonymousClass5(UCharacterProperty this$0, UCharacterProperty this$0_1, int $anonymous0) {
            this.this$0 = this$0_1;
            super($anonymous0);
        }

        boolean contains(int c) {
            return Norm2AllModes.getNFCInstance().impl.ensureCanonIterData().isCanonSegmentStarter(c);
        }
    }

    /* renamed from: android.icu.impl.UCharacterProperty.6 */
    class AnonymousClass6 extends BinaryProperty {
        final /* synthetic */ UCharacterProperty this$0;

        AnonymousClass6(UCharacterProperty this$0, UCharacterProperty this$0_1, int $anonymous0) {
            this.this$0 = this$0_1;
            super($anonymous0);
        }

        boolean contains(int c) {
            return !UCharacter.isUAlphabetic(c) ? UCharacter.isDigit(c) : true;
        }
    }

    /* renamed from: android.icu.impl.UCharacterProperty.7 */
    class AnonymousClass7 extends BinaryProperty {
        final /* synthetic */ UCharacterProperty this$0;

        AnonymousClass7(UCharacterProperty this$0, UCharacterProperty this$0_1, int $anonymous0) {
            this.this$0 = this$0_1;
            super($anonymous0);
        }

        boolean contains(int c) {
            boolean z = true;
            if (c <= Opcodes.OP_REM_LONG) {
                if (!(c == UCharacterProperty.TAB || c == 32)) {
                    z = UCharacterProperty.-assertionsDisabled;
                }
                return z;
            }
            if (UCharacter.getType(c) != UCharacterProperty.SRC_COUNT) {
                z = UCharacterProperty.-assertionsDisabled;
            }
            return z;
        }
    }

    /* renamed from: android.icu.impl.UCharacterProperty.8 */
    class AnonymousClass8 extends BinaryProperty {
        final /* synthetic */ UCharacterProperty this$0;

        AnonymousClass8(UCharacterProperty this$0, UCharacterProperty this$0_1, int $anonymous0) {
            this.this$0 = this$0_1;
            super($anonymous0);
        }

        boolean contains(int c) {
            return UCharacterProperty.isgraphPOSIX(c);
        }
    }

    /* renamed from: android.icu.impl.UCharacterProperty.9 */
    class AnonymousClass9 extends BinaryProperty {
        final /* synthetic */ UCharacterProperty this$0;

        AnonymousClass9(UCharacterProperty this$0, UCharacterProperty this$0_1, int $anonymous0) {
            this.this$0 = this$0_1;
            super($anonymous0);
        }

        boolean contains(int c) {
            return UCharacter.getType(c) != UCharacterProperty.SRC_COUNT ? UCharacterProperty.isgraphPOSIX(c) : true;
        }
    }

    private class CaseBinaryProperty extends BinaryProperty {
        int which;

        CaseBinaryProperty(int which) {
            super(UCharacterProperty.TERMINAL_PUNCTUATION_PROPERTY_);
            this.which = which;
        }

        boolean contains(int c) {
            return UCaseProps.INSTANCE.hasBinaryProperty(c, this.which);
        }
    }

    private static final class IsAcceptable implements Authenticate {
        private IsAcceptable() {
        }

        public boolean isDataVersionAcceptable(byte[] version) {
            return version[UCharacterProperty.WHITE_SPACE_PROPERTY_] == UCharacterProperty.SRC_CASE_AND_NORM ? true : UCharacterProperty.-assertionsDisabled;
        }
    }

    private class NormInertBinaryProperty extends BinaryProperty {
        int which;

        NormInertBinaryProperty(int source, int which) {
            super(source);
            this.which = which;
        }

        boolean contains(int c) {
            return Norm2AllModes.getN2WithImpl(this.which - 37).isInert(c);
        }
    }

    private class NormQuickCheckIntProperty extends IntProperty {
        int max;
        int which;

        NormQuickCheckIntProperty(int source, int which, int max) {
            super(source);
            this.which = which;
            this.max = max;
        }

        int getValue(int c) {
            return Norm2AllModes.getN2WithImpl(this.which - 4108).getQuickCheck(c);
        }

        int getMaxValue(int which) {
            return this.max;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.UCharacterProperty.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.UCharacterProperty.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.UCharacterProperty.<clinit>():void");
    }

    public final int getProperty(int ch) {
        return this.m_trie_.get(ch);
    }

    public int getAdditional(int codepoint, int column) {
        if (!-assertionsDisabled) {
            if ((column >= 0 ? SRC_CHAR : WHITE_SPACE_PROPERTY_) == 0) {
                throw new AssertionError();
            }
        }
        if (column >= this.m_additionalColumnsCount_) {
            return WHITE_SPACE_PROPERTY_;
        }
        return this.m_additionalVectors_[this.m_additionalTrie_.get(codepoint) + column];
    }

    public VersionInfo getAge(int codepoint) {
        int version = getAdditional(codepoint, WHITE_SPACE_PROPERTY_) >> ID_START_PROPERTY_;
        return VersionInfo.getInstance((version >> TERMINAL_PUNCTUATION_PROPERTY_) & SB_SHIFT, version & SB_SHIFT, WHITE_SPACE_PROPERTY_, WHITE_SPACE_PROPERTY_);
    }

    private static final boolean isgraphPOSIX(int c) {
        return (getMask(UCharacter.getType(c)) & (((GC_CC_MASK | GC_CS_MASK) | GC_CN_MASK) | GC_Z_MASK)) == 0 ? true : -assertionsDisabled;
    }

    public boolean hasBinaryProperty(int c, int which) {
        if (which < 0 || 57 <= which) {
            return -assertionsDisabled;
        }
        return this.binProps[which].contains(c);
    }

    public int getType(int c) {
        return getProperty(c) & TYPE_MASK;
    }

    public int getIntPropertyValue(int c, int which) {
        int i = WHITE_SPACE_PROPERTY_;
        if (which < VMDebug.KIND_GLOBAL_EXT_ALLOCATED_OBJECTS) {
            if (which >= 0 && which < 57) {
                if (this.binProps[which].contains(c)) {
                    i = SRC_CHAR;
                }
                return i;
            }
        } else if (which < UProperty.INT_LIMIT) {
            return this.intProps[which - 4096].getValue(c);
        } else {
            if (which == DateUtilsBridge.FORMAT_UTC) {
                return getMask(getType(c));
            }
        }
        return WHITE_SPACE_PROPERTY_;
    }

    public int getIntPropertyMaxValue(int which) {
        if (which < VMDebug.KIND_GLOBAL_EXT_ALLOCATED_OBJECTS) {
            if (which >= 0 && which < 57) {
                return SRC_CHAR;
            }
        } else if (which < UProperty.INT_LIMIT) {
            return this.intProps[which - 4096].getMaxValue(which);
        }
        return -1;
    }

    public final int getSource(int which) {
        if (which < 0) {
            return WHITE_SPACE_PROPERTY_;
        }
        if (which < 57) {
            return this.binProps[which].getSource();
        }
        if (which < VMDebug.KIND_GLOBAL_EXT_ALLOCATED_OBJECTS) {
            return WHITE_SPACE_PROPERTY_;
        }
        if (which < UProperty.INT_LIMIT) {
            return this.intProps[which - 4096].getSource();
        }
        if (which < DateUtilsBridge.FORMAT_ABBREV_TIME) {
            switch (which) {
                case DateUtilsBridge.FORMAT_UTC /*8192*/:
                case UProperty.NUMERIC_VALUE /*12288*/:
                    return SRC_CHAR;
                default:
                    return WHITE_SPACE_PROPERTY_;
            }
        } else if (which < UProperty.STRING_LIMIT) {
            switch (which) {
                case DateUtilsBridge.FORMAT_ABBREV_TIME /*16384*/:
                    return SRC_PROPSVEC;
                case UProperty.BIDI_MIRRORING_GLYPH /*16385*/:
                    return SRC_BIDI;
                case UProperty.CASE_FOLDING /*16386*/:
                case UProperty.LOWERCASE_MAPPING /*16388*/:
                case UProperty.SIMPLE_CASE_FOLDING /*16390*/:
                case UProperty.SIMPLE_LOWERCASE_MAPPING /*16391*/:
                case UProperty.SIMPLE_TITLECASE_MAPPING /*16392*/:
                case UProperty.SIMPLE_UPPERCASE_MAPPING /*16393*/:
                case UProperty.TITLECASE_MAPPING /*16394*/:
                case UProperty.UPPERCASE_MAPPING /*16396*/:
                    return TERMINAL_PUNCTUATION_PROPERTY_;
                case UProperty.ISO_COMMENT /*16387*/:
                case UProperty.NAME /*16389*/:
                case UProperty.UNICODE_1_NAME /*16395*/:
                    return SRC_NAMES;
                default:
                    return WHITE_SPACE_PROPERTY_;
            }
        } else {
            switch (which) {
                case UProperty.SCRIPT_EXTENSIONS /*28672*/:
                    return SRC_PROPSVEC;
                default:
                    return WHITE_SPACE_PROPERTY_;
            }
        }
    }

    public int getMaxValues(int column) {
        switch (column) {
            case WHITE_SPACE_PROPERTY_ /*0*/:
                return this.m_maxBlockScriptValue_;
            case SRC_PROPSVEC /*2*/:
                return this.m_maxJTGValue_;
            default:
                return WHITE_SPACE_PROPERTY_;
        }
    }

    public static final int getMask(int type) {
        return SRC_CHAR << type;
    }

    public static int getEuropeanDigit(int ch) {
        int i = U_A;
        if ((ch > U_z && ch < U_FW_A) || ch < U_A || ((ch > U_Z && ch < U_a) || ch > U_FW_z || (ch > U_FW_Z && ch < U_FW_a))) {
            return -1;
        }
        if (ch <= U_z) {
            int i2 = ch + WB_SHIFT;
            if (ch > U_Z) {
                i = U_a;
            }
            return i2 - i;
        } else if (ch <= U_FW_Z) {
            return (ch + WB_SHIFT) - U_FW_A;
        } else {
            return (ch + WB_SHIFT) - U_FW_a;
        }
    }

    public int digit(int c) {
        int value = getNumericTypeValue(getProperty(c)) - 1;
        if (value <= TAB) {
            return value;
        }
        return -1;
    }

    public int getNumericValue(int c) {
        int ntv = getNumericTypeValue(getProperty(c));
        if (ntv == 0) {
            return getEuropeanDigit(c);
        }
        if (ntv < SRC_NFC_CANON_ITER) {
            return ntv - 1;
        }
        if (ntv < NTV_NUMERIC_START_) {
            return ntv - 11;
        }
        if (ntv < NTV_FRACTION_START_) {
            return ntv - 21;
        }
        if (ntv < NTV_LARGE_START_) {
            return -2;
        }
        int numValue;
        if (ntv < NTV_BASE60_START_) {
            int mant = (ntv >> SRC_BIDI) - 14;
            int exp = (ntv & TYPE_MASK) + SRC_PROPSVEC;
            if (exp >= TAB && (exp != TAB || mant > SRC_PROPSVEC)) {
                return -2;
            }
            numValue = mant;
            do {
                numValue *= WB_SHIFT;
                exp--;
            } while (exp > 0);
            return numValue;
        } else if (ntv >= NTV_RESERVED_START_) {
            return -2;
        } else {
            numValue = (ntv >> SRC_PROPSVEC) - 191;
            switch ((ntv & SRC_NAMES) + SRC_CHAR) {
                case SRC_CHAR /*1*/:
                    numValue *= 60;
                    break;
                case SRC_PROPSVEC /*2*/:
                    numValue *= 3600;
                    break;
                case SRC_NAMES /*3*/:
                    numValue *= 216000;
                    break;
                case TERMINAL_PUNCTUATION_PROPERTY_ /*4*/:
                    numValue *= 12960000;
                    break;
            }
            return numValue;
        }
    }

    public double getUnicodeNumericValue(int c) {
        int ntv = getNumericTypeValue(getProperty(c));
        if (ntv == 0) {
            return MessagePattern.NO_NUMERIC_VALUE;
        }
        if (ntv < SRC_NFC_CANON_ITER) {
            return (double) (ntv - 1);
        }
        if (ntv < NTV_NUMERIC_START_) {
            return (double) (ntv - 11);
        }
        if (ntv < NTV_FRACTION_START_) {
            return (double) (ntv - 21);
        }
        if (ntv < NTV_LARGE_START_) {
            return ((double) ((ntv >> TERMINAL_PUNCTUATION_PROPERTY_) - 12)) / ((double) ((ntv & SB_SHIFT) + SRC_CHAR));
        } else if (ntv < NTV_BASE60_START_) {
            int exp = (ntv & TYPE_MASK) + SRC_PROPSVEC;
            double numValue = (double) ((ntv >> SRC_BIDI) - 14);
            while (exp >= TERMINAL_PUNCTUATION_PROPERTY_) {
                numValue *= 10000.0d;
                exp -= 4;
            }
            switch (exp) {
                case SRC_CHAR /*1*/:
                    numValue *= 10.0d;
                    break;
                case SRC_PROPSVEC /*2*/:
                    numValue *= 100.0d;
                    break;
                case SRC_NAMES /*3*/:
                    numValue *= 1000.0d;
                    break;
            }
            return numValue;
        } else if (ntv >= NTV_RESERVED_START_) {
            return MessagePattern.NO_NUMERIC_VALUE;
        } else {
            int numValue2 = (ntv >> SRC_PROPSVEC) - 191;
            switch ((ntv & SRC_NAMES) + SRC_CHAR) {
                case SRC_CHAR /*1*/:
                    numValue2 *= 60;
                    break;
                case SRC_PROPSVEC /*2*/:
                    numValue2 *= 3600;
                    break;
                case SRC_NAMES /*3*/:
                    numValue2 *= 216000;
                    break;
                case TERMINAL_PUNCTUATION_PROPERTY_ /*4*/:
                    numValue2 *= 12960000;
                    break;
            }
            return (double) numValue2;
        }
    }

    private static final int getNumericTypeValue(int props) {
        return props >> SRC_CHAR_AND_PROPSVEC;
    }

    private static final int ntvGetType(int ntv) {
        if (ntv == 0) {
            return WHITE_SPACE_PROPERTY_;
        }
        if (ntv < SRC_NFC_CANON_ITER) {
            return SRC_CHAR;
        }
        if (ntv < NTV_NUMERIC_START_) {
            return SRC_PROPSVEC;
        }
        return SRC_NAMES;
    }

    private UCharacterProperty() throws IOException {
        this.binProps = new BinaryProperty[]{new BinaryProperty(SRC_CHAR, NodeFilter.SHOW_DOCUMENT), new BinaryProperty(SRC_CHAR, NodeFilter.SHOW_COMMENT), new AnonymousClass1(this, this, SRC_BIDI), new AnonymousClass2(this, this, SRC_BIDI), new BinaryProperty(SRC_CHAR, SRC_PROPSVEC), new BinaryProperty(SRC_CHAR, DateUtilsBridge.FORMAT_ABBREV_ALL), new BinaryProperty(SRC_CHAR, VMDebug.KIND_THREAD_GC_INVOCATIONS), new BinaryProperty(SRC_CHAR, NodeFilter.SHOW_DOCUMENT_FRAGMENT), new BinaryProperty(SRC_CHAR, NodeFilter.SHOW_NOTATION), new AnonymousClass3(this, this, SRC_NFC), new BinaryProperty(SRC_CHAR, ArabicShaping.SPACES_RELATIVE_TO_TEXT_MASK), new BinaryProperty(SRC_CHAR, DateUtilsBridge.FORMAT_UTC), new BinaryProperty(SRC_CHAR, DateUtilsBridge.FORMAT_ABBREV_TIME), new BinaryProperty(SRC_CHAR, 64), new BinaryProperty(SRC_CHAR, TERMINAL_PUNCTUATION_PROPERTY_), new BinaryProperty(SRC_CHAR, 33554432), new BinaryProperty(SRC_CHAR, DictionaryData.TRANSFORM_TYPE_OFFSET), new BinaryProperty(SRC_CHAR, NodeFilter.SHOW_DOCUMENT_TYPE), new BinaryProperty(SRC_CHAR, DateUtilsBridge.FORMAT_ABBREV_WEEKDAY), new BinaryProperty(SRC_CHAR, DateUtilsBridge.FORMAT_ABBREV_MONTH), new AnonymousClass4(this, this, SRC_BIDI), new BinaryProperty(SRC_CHAR, VMDebug.KIND_THREAD_CLASS_INIT_COUNT), new CaseBinaryProperty(XID_START_PROPERTY_), new BinaryProperty(SRC_CHAR, 32), new BinaryProperty(SRC_CHAR, VMDebug.KIND_GLOBAL_EXT_ALLOCATED_OBJECTS), new BinaryProperty(SRC_CHAR, SRC_NFC), new BinaryProperty(SRC_CHAR, DateUtilsBridge.FORMAT_NUMERIC_DATE), new CaseBinaryProperty(S_TERM_PROPERTY_), new BinaryProperty(SRC_CHAR, IDS_TRINARY_OPERATOR_PROPERTY_), new BinaryProperty(SRC_CHAR, DateUtilsBridge.FORMAT_ABBREV_RELATIVE), new CaseBinaryProperty(PATTERN_WHITE_SPACE), new BinaryProperty(SRC_CHAR, SRC_CHAR), new BinaryProperty(SRC_CHAR, SCRIPT_X_WITH_INHERITED), new BinaryProperty(SRC_CHAR, SCRIPT_X_WITH_COMMON), new CaseBinaryProperty(34), new BinaryProperty(SRC_CHAR, ArabicShaping.SHAPE_TAIL_TYPE_MASK), new BinaryProperty(SRC_CHAR, VMDebug.KIND_THREAD_EXT_ALLOCATED_OBJECTS), new NormInertBinaryProperty(SRC_NFC, 37), new NormInertBinaryProperty(TAB, 38), new NormInertBinaryProperty(SRC_NFC, 39), new NormInertBinaryProperty(TAB, 40), new AnonymousClass5(this, this, SRC_NFC_CANON_ITER), new BinaryProperty(SRC_CHAR, VMDebug.KIND_THREAD_EXT_ALLOCATED_BYTES), new BinaryProperty(SRC_CHAR, VMDebug.KIND_THREAD_EXT_FREED_OBJECTS), new AnonymousClass6(this, this, SRC_CHAR_AND_PROPSVEC), new AnonymousClass7(this, this, SRC_CHAR), new AnonymousClass8(this, this, SRC_CHAR), new AnonymousClass9(this, this, SRC_CHAR), new AnonymousClass10(this, this, SRC_CHAR), new CaseBinaryProperty(49), new CaseBinaryProperty(50), new CaseBinaryProperty(51), new CaseBinaryProperty(52), new CaseBinaryProperty(53), new AnonymousClass11(this, this, SRC_CASE_AND_NORM), new CaseBinaryProperty(55), new AnonymousClass12(this, this, WB_SHIFT)};
        IntProperty[] intPropertyArr = new IntProperty[XID_START_PROPERTY_];
        intPropertyArr[WHITE_SPACE_PROPERTY_] = new AnonymousClass13(this, this);
        intPropertyArr[SRC_CHAR] = new IntProperty(WHITE_SPACE_PROPERTY_, BLOCK_MASK_, SRC_NFC);
        intPropertyArr[SRC_PROPSVEC] = new AnonymousClass14(this, this, SRC_NFC);
        intPropertyArr[SRC_NAMES] = new IntProperty(SRC_PROPSVEC, TYPE_MASK, WHITE_SPACE_PROPERTY_);
        intPropertyArr[TERMINAL_PUNCTUATION_PROPERTY_] = new IntProperty(WHITE_SPACE_PROPERTY_, EAST_ASIAN_MASK_, RADICAL_PROPERTY_);
        intPropertyArr[SRC_BIDI] = new AnonymousClass15(this, this, SRC_CHAR);
        intPropertyArr[SRC_CHAR_AND_PROPSVEC] = new AnonymousClass16(this, this);
        intPropertyArr[SRC_CASE_AND_NORM] = new AnonymousClass17(this, this);
        intPropertyArr[SRC_NFC] = new IntProperty(SRC_PROPSVEC, LB_MASK, LB_SHIFT);
        intPropertyArr[TAB] = new AnonymousClass18(this, this, SRC_CHAR);
        intPropertyArr[WB_SHIFT] = new AnonymousClass19(this, this, WHITE_SPACE_PROPERTY_, SCRIPT_MASK_, WHITE_SPACE_PROPERTY_);
        intPropertyArr[SRC_NFC_CANON_ITER] = new AnonymousClass20(this, this, SRC_PROPSVEC);
        intPropertyArr[SRC_COUNT] = new NormQuickCheckIntProperty(SRC_NFC, UProperty.NFD_QUICK_CHECK, SRC_CHAR);
        intPropertyArr[GRAPHEME_EXTEND_PROPERTY_] = new NormQuickCheckIntProperty(TAB, UProperty.NFKD_QUICK_CHECK, SRC_CHAR);
        intPropertyArr[GRAPHEME_LINK_PROPERTY_] = new NormQuickCheckIntProperty(SRC_NFC, UProperty.NFC_QUICK_CHECK, SRC_PROPSVEC);
        intPropertyArr[SB_SHIFT] = new NormQuickCheckIntProperty(TAB, UProperty.NFKC_QUICK_CHECK, SRC_PROPSVEC);
        intPropertyArr[IDS_TRINARY_OPERATOR_PROPERTY_] = new AnonymousClass21(this, this, SRC_NFC);
        intPropertyArr[RADICAL_PROPERTY_] = new AnonymousClass22(this, this, SRC_NFC);
        intPropertyArr[UNIFIED_IDEOGRAPH_PROPERTY_] = new IntProperty(SRC_PROPSVEC, GCB_MASK, SRC_BIDI);
        intPropertyArr[DEFAULT_IGNORABLE_CODE_POINT_PROPERTY_] = new IntProperty(SRC_PROPSVEC, SB_MASK, SB_SHIFT);
        intPropertyArr[LB_SHIFT] = new IntProperty(SRC_PROPSVEC, WB_MASK, WB_SHIFT);
        intPropertyArr[NTV_NUMERIC_START_] = new AnonymousClass23(this, this);
        this.intProps = intPropertyArr;
        if (this.binProps.length != 57) {
            throw new ICUException("binProps.length!=UProperty.BINARY_LIMIT");
        } else if (this.intProps.length != XID_START_PROPERTY_) {
            throw new ICUException("intProps.length!=(UProperty.INT_LIMIT-UProperty.INT_START)");
        } else {
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
            ICUBinary.skipBytes(bytes, IDS_TRINARY_OPERATOR_PROPERTY_);
            this.m_trie_ = Trie2_16.createFromSerialized(bytes);
            int expectedTrieLength = (propertyOffset - 16) * TERMINAL_PUNCTUATION_PROPERTY_;
            int trieLength = this.m_trie_.getSerializedLength();
            if (trieLength > expectedTrieLength) {
                throw new IOException("uprops.icu: not enough bytes for main trie");
            }
            ICUBinary.skipBytes(bytes, expectedTrieLength - trieLength);
            ICUBinary.skipBytes(bytes, (additionalOffset - propertyOffset) * TERMINAL_PUNCTUATION_PROPERTY_);
            if (this.m_additionalColumnsCount_ > 0) {
                this.m_additionalTrie_ = Trie2_16.createFromSerialized(bytes);
                expectedTrieLength = (additionalVectorsOffset - additionalOffset) * TERMINAL_PUNCTUATION_PROPERTY_;
                trieLength = this.m_additionalTrie_.getSerializedLength();
                if (trieLength > expectedTrieLength) {
                    throw new IOException("uprops.icu: not enough bytes for additional-properties trie");
                }
                ICUBinary.skipBytes(bytes, expectedTrieLength - trieLength);
                this.m_additionalVectors_ = ICUBinary.getInts(bytes, scriptExtensionsOffset - additionalVectorsOffset, WHITE_SPACE_PROPERTY_);
            }
            int numChars = (reservedOffset7 - scriptExtensionsOffset) * SRC_PROPSVEC;
            if (numChars > 0) {
                this.m_scriptExtensions_ = ICUBinary.getChars(bytes, numChars, WHITE_SPACE_PROPERTY_);
            }
        }
    }

    public UnicodeSet addPropertyStarts(UnicodeSet set) {
        Iterator<Range> trieIterator = this.m_trie_.iterator();
        while (trieIterator.hasNext()) {
            Range range = (Range) trieIterator.next();
            if (range.leadSurrogate) {
                break;
            }
            set.add(range.startCodePoint);
        }
        set.add((int) TAB);
        set.add((int) WB_SHIFT);
        set.add((int) GRAPHEME_LINK_PROPERTY_);
        set.add((int) VARIATION_SELECTOR_PROPERTY_);
        set.add(32);
        set.add((int) NL);
        set.add((int) Opcodes.OP_LONG_TO_DOUBLE);
        set.add((int) DEL);
        set.add((int) HAIRSP);
        set.add(8208);
        set.add((int) INHSWAP);
        set.add(8304);
        set.add((int) ZWNBSP);
        set.add((int) Normalizer2Impl.JAMO_VT);
        set.add((int) NBSP);
        set.add((int) Opcodes.OP_OR_LONG);
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
        set.add((int) U_a);
        set.add((int) Opcodes.OP_NEG_INT);
        set.add((int) U_A);
        set.add(91);
        set.add((int) U_FW_a);
        set.add(65371);
        set.add((int) U_FW_A);
        set.add(65339);
        set.add((int) Opcodes.OP_SPUT);
        set.add(71);
        set.add(65351);
        set.add(65319);
        set.add((int) WJ);
        set.add(65520);
        set.add(65532);
        set.add((int) EAST_ASIAN_MASK_);
        set.add(921600);
        set.add((int) CGJ);
        set.add(848);
        return set;
    }

    public void upropsvec_addPropertyStarts(UnicodeSet set) {
        if (this.m_additionalColumnsCount_ > 0) {
            Iterator<Range> trieIterator = this.m_additionalTrie_.iterator();
            while (trieIterator.hasNext()) {
                Range range = (Range) trieIterator.next();
                if (!range.leadSurrogate) {
                    set.add(range.startCodePoint);
                } else {
                    return;
                }
            }
        }
    }
}
