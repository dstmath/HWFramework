package android.icu.text;

import android.icu.impl.Normalizer2Impl;
import android.icu.impl.UBiDiProps;
import android.icu.util.AnnualTimeZoneRule;
import dalvik.bytecode.Opcodes;

public final class ArabicShaping {
    private static final int ALEFTYPE = 32;
    private static final int DESHAPE_MODE = 1;
    public static final int DIGITS_AN2EN = 64;
    public static final int DIGITS_EN2AN = 32;
    public static final int DIGITS_EN2AN_INIT_AL = 128;
    public static final int DIGITS_EN2AN_INIT_LR = 96;
    public static final int DIGITS_MASK = 224;
    public static final int DIGITS_NOOP = 0;
    public static final int DIGIT_TYPE_AN = 0;
    public static final int DIGIT_TYPE_AN_EXTENDED = 256;
    public static final int DIGIT_TYPE_MASK = 256;
    private static final char HAMZA06_CHAR = '\u0621';
    private static final char HAMZAFE_CHAR = '\ufe80';
    private static final int IRRELEVANT = 4;
    public static final int LAMALEF_AUTO = 65536;
    public static final int LAMALEF_BEGIN = 3;
    public static final int LAMALEF_END = 2;
    public static final int LAMALEF_MASK = 65539;
    public static final int LAMALEF_NEAR = 1;
    public static final int LAMALEF_RESIZE = 0;
    private static final char LAMALEF_SPACE_SUB = '\uffff';
    private static final int LAMTYPE = 16;
    private static final char LAM_CHAR = '\u0644';
    public static final int LENGTH_FIXED_SPACES_AT_BEGINNING = 3;
    public static final int LENGTH_FIXED_SPACES_AT_END = 2;
    public static final int LENGTH_FIXED_SPACES_NEAR = 1;
    public static final int LENGTH_GROW_SHRINK = 0;
    public static final int LENGTH_MASK = 65539;
    public static final int LETTERS_MASK = 24;
    public static final int LETTERS_NOOP = 0;
    public static final int LETTERS_SHAPE = 8;
    public static final int LETTERS_SHAPE_TASHKEEL_ISOLATED = 24;
    public static final int LETTERS_UNSHAPE = 16;
    private static final int LINKL = 2;
    private static final int LINKR = 1;
    private static final int LINK_MASK = 3;
    private static final char NEW_TAIL_CHAR = '\ufe73';
    private static final char OLD_TAIL_CHAR = '\u200b';
    public static final int SEEN_MASK = 7340032;
    public static final int SEEN_TWOCELL_NEAR = 2097152;
    private static final char SHADDA06_CHAR = '\u0651';
    private static final char SHADDA_CHAR = '\ufe7c';
    private static final char SHADDA_TATWEEL_CHAR = '\ufe7d';
    private static final int SHAPE_MODE = 0;
    public static final int SHAPE_TAIL_NEW_UNICODE = 134217728;
    public static final int SHAPE_TAIL_TYPE_MASK = 134217728;
    public static final int SPACES_RELATIVE_TO_TEXT_BEGIN_END = 67108864;
    public static final int SPACES_RELATIVE_TO_TEXT_MASK = 67108864;
    private static final char SPACE_CHAR = ' ';
    public static final int TASHKEEL_BEGIN = 262144;
    public static final int TASHKEEL_END = 393216;
    public static final int TASHKEEL_MASK = 917504;
    public static final int TASHKEEL_REPLACE_BY_TATWEEL = 786432;
    public static final int TASHKEEL_RESIZE = 524288;
    private static final char TASHKEEL_SPACE_SUB = '\ufffe';
    private static final char TATWEEL_CHAR = '\u0640';
    public static final int TEXT_DIRECTION_LOGICAL = 0;
    public static final int TEXT_DIRECTION_MASK = 4;
    public static final int TEXT_DIRECTION_VISUAL_LTR = 4;
    public static final int TEXT_DIRECTION_VISUAL_RTL = 0;
    public static final int YEHHAMZA_MASK = 58720256;
    public static final int YEHHAMZA_TWOCELL_NEAR = 16777216;
    private static final char YEH_HAMZAFE_CHAR = '\ufe89';
    private static final char YEH_HAMZA_CHAR = '\u0626';
    private static final int[] araLink = null;
    private static int[] convertFEto06;
    private static final char[] convertNormalizedLamAlef = null;
    private static final int[] irrelevantPos = null;
    private static final int[] presLink = null;
    private static final int[][][] shapeTable = null;
    private static final int[] tailFamilyIsolatedFinal = null;
    private static final int[] tashkeelMedial = null;
    private static final char[] yehHamzaToYeh = null;
    private boolean isLogical;
    private final int options;
    private boolean spacesRelativeToTextBeginEnd;
    private char tailChar;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.ArabicShaping.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.ArabicShaping.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.ArabicShaping.<clinit>():void");
    }

    public int shape(char[] source, int sourceStart, int sourceLength, char[] dest, int destStart, int destSize) throws ArabicShapingException {
        if (source == null) {
            throw new IllegalArgumentException("source can not be null");
        } else if (sourceStart < 0 || sourceLength < 0 || sourceStart + sourceLength > source.length) {
            throw new IllegalArgumentException("bad source start (" + sourceStart + ") or length (" + sourceLength + ") for buffer of length " + source.length);
        } else if (dest == null && destSize != 0) {
            throw new IllegalArgumentException("null dest requires destSize == 0");
        } else if (destSize != 0 && (destStart < 0 || destSize < 0 || destStart + destSize > dest.length)) {
            throw new IllegalArgumentException("bad dest start (" + destStart + ") or size (" + destSize + ") for buffer of length " + dest.length);
        } else if ((this.options & TASHKEEL_MASK) > 0 && (this.options & TASHKEEL_MASK) != TASHKEEL_BEGIN && (this.options & TASHKEEL_MASK) != TASHKEEL_END && (this.options & TASHKEEL_MASK) != TASHKEEL_RESIZE && (this.options & TASHKEEL_MASK) != TASHKEEL_REPLACE_BY_TATWEEL) {
            throw new IllegalArgumentException("Wrong Tashkeel argument");
        } else if ((this.options & LENGTH_MASK) > 0 && (this.options & LENGTH_MASK) != LINK_MASK && (this.options & LENGTH_MASK) != LINKL && (this.options & LENGTH_MASK) != 0 && (this.options & LENGTH_MASK) != LAMALEF_AUTO && (this.options & LENGTH_MASK) != LINKR) {
            throw new IllegalArgumentException("Wrong Lam Alef argument");
        } else if ((this.options & TASHKEEL_MASK) <= 0 || (this.options & LETTERS_SHAPE_TASHKEEL_ISOLATED) != LETTERS_UNSHAPE) {
            return internalShape(source, sourceStart, sourceLength, dest, destStart, destSize);
        } else {
            throw new IllegalArgumentException("Tashkeel replacement should not be enabled in deshaping mode ");
        }
    }

    public void shape(char[] source, int start, int length) throws ArabicShapingException {
        if ((this.options & LENGTH_MASK) == 0) {
            throw new ArabicShapingException("Cannot shape in place with length option resize.");
        }
        shape(source, start, length, source, start, length);
    }

    public String shape(String text) throws ArabicShapingException {
        char[] src = text.toCharArray();
        char[] dest = src;
        if ((this.options & LENGTH_MASK) == 0 && (this.options & LETTERS_SHAPE_TASHKEEL_ISOLATED) == LETTERS_UNSHAPE) {
            dest = new char[(src.length * LINKL)];
        }
        return new String(dest, TEXT_DIRECTION_VISUAL_RTL, shape(src, TEXT_DIRECTION_VISUAL_RTL, src.length, dest, TEXT_DIRECTION_VISUAL_RTL, dest.length));
    }

    public ArabicShaping(int options) {
        boolean z = true;
        this.options = options;
        if ((options & DIGITS_MASK) > DIGITS_EN2AN_INIT_AL) {
            throw new IllegalArgumentException("bad DIGITS options");
        }
        boolean z2;
        if ((options & TEXT_DIRECTION_VISUAL_LTR) == 0) {
            z2 = true;
        } else {
            z2 = false;
        }
        this.isLogical = z2;
        if ((options & SPACES_RELATIVE_TO_TEXT_MASK) != SPACES_RELATIVE_TO_TEXT_MASK) {
            z = false;
        }
        this.spacesRelativeToTextBeginEnd = z;
        if ((options & SHAPE_TAIL_TYPE_MASK) == SHAPE_TAIL_TYPE_MASK) {
            this.tailChar = NEW_TAIL_CHAR;
        } else {
            this.tailChar = OLD_TAIL_CHAR;
        }
    }

    public boolean equals(Object rhs) {
        if (rhs != null && rhs.getClass() == ArabicShaping.class && this.options == ((ArabicShaping) rhs).options) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return this.options;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder(super.toString());
        buf.append('[');
        switch (this.options & LENGTH_MASK) {
            case TEXT_DIRECTION_VISUAL_RTL /*0*/:
                buf.append("LamAlef resize");
                break;
            case LINKR /*1*/:
                buf.append("LamAlef spaces at near");
                break;
            case LINKL /*2*/:
                buf.append("LamAlef spaces at end");
                break;
            case LINK_MASK /*3*/:
                buf.append("LamAlef spaces at begin");
                break;
            case LAMALEF_AUTO /*65536*/:
                buf.append("lamAlef auto");
                break;
        }
        switch (this.options & TEXT_DIRECTION_VISUAL_LTR) {
            case TEXT_DIRECTION_VISUAL_RTL /*0*/:
                buf.append(", logical");
                break;
            case TEXT_DIRECTION_VISUAL_LTR /*4*/:
                buf.append(", visual");
                break;
        }
        switch (this.options & LETTERS_SHAPE_TASHKEEL_ISOLATED) {
            case TEXT_DIRECTION_VISUAL_RTL /*0*/:
                buf.append(", no letter shaping");
                break;
            case LETTERS_SHAPE /*8*/:
                buf.append(", shape letters");
                break;
            case LETTERS_UNSHAPE /*16*/:
                buf.append(", unshape letters");
                break;
            case LETTERS_SHAPE_TASHKEEL_ISOLATED /*24*/:
                buf.append(", shape letters tashkeel isolated");
                break;
        }
        switch (this.options & SEEN_MASK) {
            case SEEN_TWOCELL_NEAR /*2097152*/:
                buf.append(", Seen at near");
                break;
        }
        switch (this.options & YEHHAMZA_MASK) {
            case YEHHAMZA_TWOCELL_NEAR /*16777216*/:
                buf.append(", Yeh Hamza at near");
                break;
        }
        switch (this.options & TASHKEEL_MASK) {
            case TASHKEEL_BEGIN /*262144*/:
                buf.append(", Tashkeel at begin");
                break;
            case TASHKEEL_END /*393216*/:
                buf.append(", Tashkeel at end");
                break;
            case TASHKEEL_RESIZE /*524288*/:
                buf.append(", Tashkeel resize");
                break;
            case TASHKEEL_REPLACE_BY_TATWEEL /*786432*/:
                buf.append(", Tashkeel replace with tatweel");
                break;
        }
        switch (this.options & DIGITS_MASK) {
            case TEXT_DIRECTION_VISUAL_RTL /*0*/:
                buf.append(", no digit shaping");
                break;
            case DIGITS_EN2AN /*32*/:
                buf.append(", shape digits to AN");
                break;
            case DIGITS_AN2EN /*64*/:
                buf.append(", shape digits to EN");
                break;
            case DIGITS_EN2AN_INIT_LR /*96*/:
                buf.append(", shape digits to AN contextually: default EN");
                break;
            case DIGITS_EN2AN_INIT_AL /*128*/:
                buf.append(", shape digits to AN contextually: default AL");
                break;
        }
        switch (this.options & DIGIT_TYPE_MASK) {
            case TEXT_DIRECTION_VISUAL_RTL /*0*/:
                buf.append(", standard Arabic-Indic digits");
                break;
            case DIGIT_TYPE_MASK /*256*/:
                buf.append(", extended Arabic-Indic digits");
                break;
        }
        buf.append("]");
        return buf.toString();
    }

    private void shapeToArabicDigitsWithContext(char[] dest, int start, int length, char digitBase, boolean lastStrongWasAL) {
        UBiDiProps bdp = UBiDiProps.INSTANCE;
        digitBase = (char) (digitBase - 48);
        int i = start + length;
        while (true) {
            i--;
            if (i >= start) {
                char ch = dest[i];
                switch (bdp.getClass(ch)) {
                    case TEXT_DIRECTION_VISUAL_RTL /*0*/:
                    case LINKR /*1*/:
                        lastStrongWasAL = false;
                        break;
                    case LINKL /*2*/:
                        if (lastStrongWasAL && ch <= '9') {
                            dest[i] = (char) (ch + digitBase);
                            break;
                        }
                    case Opcodes.OP_MOVE_EXCEPTION /*13*/:
                        lastStrongWasAL = true;
                        break;
                    default:
                        break;
                }
            }
            return;
        }
    }

    private static void invertBuffer(char[] buffer, int start, int length) {
        int i = start;
        for (int j = (start + length) - 1; i < j; j--) {
            char temp = buffer[i];
            buffer[i] = buffer[j];
            buffer[j] = temp;
            i += LINKR;
        }
    }

    private static char changeLamAlef(char ch) {
        switch (ch) {
            case '\u0622':
                return '\u065c';
            case '\u0623':
                return '\u065d';
            case '\u0625':
                return '\u065e';
            case '\u0627':
                return '\u065f';
            default:
                return '\u0000';
        }
    }

    private static int specialChar(char ch) {
        if ((ch > HAMZA06_CHAR && ch < YEH_HAMZA_CHAR) || ch == '\u0627' || ((ch > '\u062e' && ch < '\u0633') || ((ch > '\u0647' && ch < '\u064a') || ch == '\u0629'))) {
            return LINKR;
        }
        if (ch >= '\u064b' && ch <= '\u0652') {
            return LINKL;
        }
        if ((ch < '\u0653' || ch > '\u0655') && ch != '\u0670' && (ch < '\ufe70' || ch > '\ufe7f')) {
            return TEXT_DIRECTION_VISUAL_RTL;
        }
        return LINK_MASK;
    }

    private static int getLink(char ch) {
        if (ch >= '\u0622' && ch <= '\u06d3') {
            return araLink[ch - 1570];
        }
        if (ch == '\u200d') {
            return LINK_MASK;
        }
        if (ch >= '\u206d' && ch <= '\u206f') {
            return TEXT_DIRECTION_VISUAL_LTR;
        }
        if (ch < '\ufe70' || ch > '\ufefc') {
            return TEXT_DIRECTION_VISUAL_RTL;
        }
        return presLink[ch - 65136];
    }

    private static int countSpacesLeft(char[] dest, int start, int count) {
        int e = start + count;
        for (int i = start; i < e; i += LINKR) {
            if (dest[i] != SPACE_CHAR) {
                return i - start;
            }
        }
        return count;
    }

    private static int countSpacesRight(char[] dest, int start, int count) {
        int i = start + count;
        do {
            i--;
            if (i < start) {
                return count;
            }
        } while (dest[i] == SPACE_CHAR);
        return ((start + count) - 1) - i;
    }

    private static boolean isTashkeelChar(char ch) {
        return ch >= '\u064b' && ch <= '\u0652';
    }

    private static int isSeenTailFamilyChar(char ch) {
        if (ch < '\ufeb1' || ch >= '\ufebf') {
            return TEXT_DIRECTION_VISUAL_RTL;
        }
        return tailFamilyIsolatedFinal[ch - 65201];
    }

    private static int isSeenFamilyChar(char ch) {
        if (ch < '\u0633' || ch > '\u0636') {
            return TEXT_DIRECTION_VISUAL_RTL;
        }
        return LINKR;
    }

    private static boolean isTailChar(char ch) {
        if (ch == OLD_TAIL_CHAR || ch == NEW_TAIL_CHAR) {
            return true;
        }
        return false;
    }

    private static boolean isAlefMaksouraChar(char ch) {
        return ch == '\ufeef' || ch == '\ufef0' || ch == '\u0649';
    }

    private static boolean isYehHamzaChar(char ch) {
        if (ch == YEH_HAMZAFE_CHAR || ch == '\ufe8a') {
            return true;
        }
        return false;
    }

    private static boolean isTashkeelCharFE(char ch) {
        return ch != '\ufe75' && ch >= '\ufe70' && ch <= '\ufe7f';
    }

    private static int isTashkeelOnTatweelChar(char ch) {
        if (ch >= '\ufe70' && ch <= '\ufe7f' && ch != NEW_TAIL_CHAR && ch != '\ufe75' && ch != SHADDA_TATWEEL_CHAR) {
            return tashkeelMedial[ch - 65136];
        }
        if ((ch < '\ufcf2' || ch > '\ufcf4') && ch != SHADDA_TATWEEL_CHAR) {
            return TEXT_DIRECTION_VISUAL_RTL;
        }
        return LINKL;
    }

    private static int isIsolatedTashkeelChar(char ch) {
        if (ch >= '\ufe70' && ch <= '\ufe7f' && ch != NEW_TAIL_CHAR && ch != '\ufe75') {
            return 1 - tashkeelMedial[ch - 65136];
        }
        if (ch < '\ufc5e' || ch > '\ufc63') {
            return TEXT_DIRECTION_VISUAL_RTL;
        }
        return LINKR;
    }

    private static boolean isAlefChar(char ch) {
        return ch == '\u0622' || ch == '\u0623' || ch == '\u0625' || ch == '\u0627';
    }

    private static boolean isLamAlefChar(char ch) {
        return ch >= '\ufef5' && ch <= '\ufefc';
    }

    private static boolean isNormalizedLamAlefChar(char ch) {
        return ch >= '\u065c' && ch <= '\u065f';
    }

    private int calculateSize(char[] source, int sourceStart, int sourceLength) {
        int destSize = sourceLength;
        int i;
        int e;
        switch (this.options & LETTERS_SHAPE_TASHKEEL_ISOLATED) {
            case LETTERS_SHAPE /*8*/:
            case LETTERS_SHAPE_TASHKEEL_ISOLATED /*24*/:
                if (!this.isLogical) {
                    i = sourceStart + LINKR;
                    e = sourceStart + sourceLength;
                    while (i < e) {
                        if ((source[i] == LAM_CHAR && isAlefChar(source[i - 1])) || isTashkeelCharFE(source[i])) {
                            destSize--;
                        }
                        i += LINKR;
                    }
                    break;
                }
                i = sourceStart;
                e = (sourceStart + sourceLength) - 1;
                while (i < e) {
                    if ((source[i] == LAM_CHAR && isAlefChar(source[i + LINKR])) || isTashkeelCharFE(source[i])) {
                        destSize--;
                    }
                    i += LINKR;
                }
                break;
            case LETTERS_UNSHAPE /*16*/:
                e = sourceStart + sourceLength;
                for (i = sourceStart; i < e; i += LINKR) {
                    if (isLamAlefChar(source[i])) {
                        destSize += LINKR;
                    }
                }
                break;
        }
        return destSize;
    }

    private static int countSpaceSub(char[] dest, int length, char subChar) {
        int count = TEXT_DIRECTION_VISUAL_RTL;
        for (int i = TEXT_DIRECTION_VISUAL_RTL; i < length; i += LINKR) {
            if (dest[i] == subChar) {
                count += LINKR;
            }
        }
        return count;
    }

    private static void shiftArray(char[] dest, int start, int e, char subChar) {
        int w = e;
        int r = e;
        while (true) {
            r--;
            if (r >= start) {
                char ch = dest[r];
                if (ch != subChar) {
                    w--;
                    if (w != r) {
                        dest[w] = ch;
                    }
                }
            } else {
                return;
            }
        }
    }

    private static int flipArray(char[] dest, int start, int e, int w) {
        if (w <= start) {
            return e;
        }
        int r = w;
        int w2 = start;
        while (r < e) {
            w = w2 + LINKR;
            int r2 = r + LINKR;
            dest[w2] = dest[r];
            r = r2;
            w2 = w;
        }
        return w2;
    }

    private static int handleTashkeelWithTatweel(char[] dest, int sourceLength) {
        int i = TEXT_DIRECTION_VISUAL_RTL;
        while (i < sourceLength) {
            if (isTashkeelOnTatweelChar(dest[i]) == LINKR) {
                dest[i] = TATWEEL_CHAR;
            } else if (isTashkeelOnTatweelChar(dest[i]) == LINKL) {
                dest[i] = SHADDA_TATWEEL_CHAR;
            } else if (isIsolatedTashkeelChar(dest[i]) == LINKR && dest[i] != SHADDA_CHAR) {
                dest[i] = SPACE_CHAR;
            }
            i += LINKR;
        }
        return sourceLength;
    }

    private int handleGeneratedSpaces(char[] dest, int start, int length) {
        int lenOptionsLamAlef = this.options & LENGTH_MASK;
        int lenOptionsTashkeel = this.options & TASHKEEL_MASK;
        boolean lamAlefOn = false;
        boolean tashkeelOn = false;
        if (((this.isLogical ? TEXT_DIRECTION_VISUAL_RTL : LINKR) & (this.spacesRelativeToTextBeginEnd ? TEXT_DIRECTION_VISUAL_RTL : LINKR)) != 0) {
            switch (lenOptionsLamAlef) {
                case LINKL /*2*/:
                    lenOptionsLamAlef = LINK_MASK;
                    break;
                case LINK_MASK /*3*/:
                    lenOptionsLamAlef = LINKL;
                    break;
            }
            switch (lenOptionsTashkeel) {
                case TASHKEEL_BEGIN /*262144*/:
                    lenOptionsTashkeel = TASHKEEL_END;
                    break;
                case TASHKEEL_END /*393216*/:
                    lenOptionsTashkeel = TASHKEEL_BEGIN;
                    break;
            }
        }
        int e;
        if (lenOptionsLamAlef == LINKR) {
            e = start + length;
            for (int i = start; i < e; i += LINKR) {
                if (dest[i] == LAMALEF_SPACE_SUB) {
                    dest[i] = SPACE_CHAR;
                }
            }
        } else {
            e = start + length;
            int wL = countSpaceSub(dest, length, LAMALEF_SPACE_SUB);
            int wT = countSpaceSub(dest, length, TASHKEEL_SPACE_SUB);
            if (lenOptionsLamAlef == LINKL) {
                lamAlefOn = true;
            }
            if (lenOptionsTashkeel == TASHKEEL_END) {
                tashkeelOn = true;
            }
            if (lamAlefOn && lenOptionsLamAlef == LINKL) {
                shiftArray(dest, start, e, LAMALEF_SPACE_SUB);
                while (wL > start) {
                    wL--;
                    dest[wL] = SPACE_CHAR;
                }
            }
            if (tashkeelOn && lenOptionsTashkeel == TASHKEEL_END) {
                shiftArray(dest, start, e, TASHKEEL_SPACE_SUB);
                while (wT > start) {
                    wT--;
                    dest[wT] = SPACE_CHAR;
                }
            }
            lamAlefOn = false;
            tashkeelOn = false;
            if (lenOptionsLamAlef == 0) {
                lamAlefOn = true;
            }
            if (lenOptionsTashkeel == TASHKEEL_RESIZE) {
                tashkeelOn = true;
            }
            if (lamAlefOn && lenOptionsLamAlef == 0) {
                shiftArray(dest, start, e, LAMALEF_SPACE_SUB);
                wL = flipArray(dest, start, e, wL);
                length = wL - start;
            }
            if (tashkeelOn && lenOptionsTashkeel == TASHKEEL_RESIZE) {
                shiftArray(dest, start, e, TASHKEEL_SPACE_SUB);
                wT = flipArray(dest, start, e, wT);
                length = wT - start;
            }
            lamAlefOn = false;
            tashkeelOn = false;
            if (lenOptionsLamAlef == LINK_MASK || lenOptionsLamAlef == LAMALEF_AUTO) {
                lamAlefOn = true;
            }
            if (lenOptionsTashkeel == TASHKEEL_BEGIN) {
                tashkeelOn = true;
            }
            if (lamAlefOn && (lenOptionsLamAlef == LINK_MASK || lenOptionsLamAlef == LAMALEF_AUTO)) {
                shiftArray(dest, start, e, LAMALEF_SPACE_SUB);
                int wL2 = flipArray(dest, start, e, wL);
                while (wL2 < e) {
                    wL = wL2 + LINKR;
                    dest[wL2] = SPACE_CHAR;
                    wL2 = wL;
                }
                wL = wL2;
            }
            if (tashkeelOn && lenOptionsTashkeel == TASHKEEL_BEGIN) {
                shiftArray(dest, start, e, TASHKEEL_SPACE_SUB);
                int flipArray = flipArray(dest, start, e, wT);
                while (flipArray < e) {
                    wT = flipArray + LINKR;
                    dest[flipArray] = SPACE_CHAR;
                    flipArray = wT;
                }
            }
        }
        return length;
    }

    private boolean expandCompositCharAtBegin(char[] dest, int start, int length, int lacount) {
        if (lacount > countSpacesRight(dest, start, length)) {
            return true;
        }
        int r = (start + length) - lacount;
        int w = start + length;
        while (true) {
            r--;
            if (r < start) {
                return false;
            }
            char ch = dest[r];
            if (isNormalizedLamAlefChar(ch)) {
                w--;
                dest[w] = LAM_CHAR;
                w--;
                dest[w] = convertNormalizedLamAlef[ch - 1628];
            } else {
                w--;
                dest[w] = ch;
            }
        }
    }

    private boolean expandCompositCharAtEnd(char[] dest, int start, int length, int lacount) {
        if (lacount > countSpacesLeft(dest, start, length)) {
            return true;
        }
        int r = start + lacount;
        int e = start + length;
        int w = start;
        while (r < e) {
            int i;
            char ch = dest[r];
            if (isNormalizedLamAlefChar(ch)) {
                i = w + LINKR;
                dest[w] = convertNormalizedLamAlef[ch - 1628];
                w = i + LINKR;
                dest[i] = LAM_CHAR;
                i = w;
            } else {
                i = w + LINKR;
                dest[w] = ch;
            }
            r += LINKR;
            w = i;
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean expandCompositCharAtNear(char[] dest, int start, int length, int yehHamzaOption, int seenTailOption, int lamAlefOption) {
        if (isNormalizedLamAlefChar(dest[start])) {
            return true;
        }
        int i = start + length;
        while (true) {
            i--;
            if (i < start) {
                return false;
            }
            char ch = dest[i];
            if (lamAlefOption == LINKR && isNormalizedLamAlefChar(ch)) {
                if (i > start && dest[i - 1] == SPACE_CHAR) {
                    dest[i] = LAM_CHAR;
                    i--;
                    dest[i] = convertNormalizedLamAlef[ch - 1628];
                }
            } else if (seenTailOption == LINKR && isSeenTailFamilyChar(ch) == LINKR) {
                if (i > start && dest[i - 1] == SPACE_CHAR) {
                    dest[i - 1] = this.tailChar;
                }
            } else if (yehHamzaOption == LINKR && isYehHamzaChar(ch)) {
                if (i > start && dest[i - 1] == SPACE_CHAR) {
                    dest[i] = yehHamzaToYeh[ch - 65161];
                    dest[i - 1] = HAMZAFE_CHAR;
                }
            }
        }
        return true;
    }

    private int expandCompositChar(char[] dest, int start, int length, int lacount, int shapingMode) throws ArabicShapingException {
        int lenOptionsLamAlef = this.options & LENGTH_MASK;
        int lenOptionsSeen = this.options & SEEN_MASK;
        int lenOptionsYehHamza = this.options & YEHHAMZA_MASK;
        if (!(this.isLogical || this.spacesRelativeToTextBeginEnd)) {
            switch (lenOptionsLamAlef) {
                case LINKL /*2*/:
                    lenOptionsLamAlef = LINK_MASK;
                    break;
                case LINK_MASK /*3*/:
                    lenOptionsLamAlef = LINKL;
                    break;
                default:
                    break;
            }
        }
        if (shapingMode == LINKR) {
            if (lenOptionsLamAlef == LAMALEF_AUTO) {
                boolean spaceNotFound;
                if (this.isLogical) {
                    spaceNotFound = expandCompositCharAtEnd(dest, start, length, lacount);
                    if (spaceNotFound) {
                        spaceNotFound = expandCompositCharAtBegin(dest, start, length, lacount);
                    }
                    if (spaceNotFound) {
                        spaceNotFound = expandCompositCharAtNear(dest, start, length, TEXT_DIRECTION_VISUAL_RTL, TEXT_DIRECTION_VISUAL_RTL, LINKR);
                    }
                    if (!spaceNotFound) {
                        return length;
                    }
                    throw new ArabicShapingException("No spacefor lamalef");
                }
                spaceNotFound = expandCompositCharAtBegin(dest, start, length, lacount);
                if (spaceNotFound) {
                    spaceNotFound = expandCompositCharAtEnd(dest, start, length, lacount);
                }
                if (spaceNotFound) {
                    spaceNotFound = expandCompositCharAtNear(dest, start, length, TEXT_DIRECTION_VISUAL_RTL, TEXT_DIRECTION_VISUAL_RTL, LINKR);
                }
                if (!spaceNotFound) {
                    return length;
                }
                throw new ArabicShapingException("No spacefor lamalef");
            } else if (lenOptionsLamAlef == LINKL) {
                if (!expandCompositCharAtEnd(dest, start, length, lacount)) {
                    return length;
                }
                throw new ArabicShapingException("No spacefor lamalef");
            } else if (lenOptionsLamAlef == LINK_MASK) {
                if (!expandCompositCharAtBegin(dest, start, length, lacount)) {
                    return length;
                }
                throw new ArabicShapingException("No spacefor lamalef");
            } else if (lenOptionsLamAlef == LINKR) {
                if (!expandCompositCharAtNear(dest, start, length, TEXT_DIRECTION_VISUAL_RTL, TEXT_DIRECTION_VISUAL_RTL, LINKR)) {
                    return length;
                }
                throw new ArabicShapingException("No spacefor lamalef");
            } else if (lenOptionsLamAlef != 0) {
                return length;
            } else {
                int r = start + length;
                int w = r + lacount;
                while (true) {
                    r--;
                    if (r < start) {
                        return length + lacount;
                    }
                    char ch = dest[r];
                    if (isNormalizedLamAlefChar(ch)) {
                        w--;
                        dest[w] = LAM_CHAR;
                        w--;
                        dest[w] = convertNormalizedLamAlef[ch - 1628];
                    } else {
                        w--;
                        dest[w] = ch;
                    }
                }
            }
        } else if (lenOptionsSeen == SEEN_TWOCELL_NEAR && expandCompositCharAtNear(dest, start, length, TEXT_DIRECTION_VISUAL_RTL, LINKR, TEXT_DIRECTION_VISUAL_RTL)) {
            throw new ArabicShapingException("No space for Seen tail expansion");
        } else if (lenOptionsYehHamza != YEHHAMZA_TWOCELL_NEAR || !expandCompositCharAtNear(dest, start, length, LINKR, TEXT_DIRECTION_VISUAL_RTL, TEXT_DIRECTION_VISUAL_RTL)) {
            return length;
        } else {
            throw new ArabicShapingException("No space for YehHamza expansion");
        }
    }

    private int normalize(char[] dest, int start, int length) {
        int lacount = TEXT_DIRECTION_VISUAL_RTL;
        int e = start + length;
        for (int i = start; i < e; i += LINKR) {
            char ch = dest[i];
            if (ch >= '\ufe70' && ch <= '\ufefc') {
                if (isLamAlefChar(ch)) {
                    lacount += LINKR;
                }
                dest[i] = (char) convertFEto06[ch - 65136];
            }
        }
        return lacount;
    }

    private int deshapeNormalize(char[] dest, int start, int length) {
        int lacount = TEXT_DIRECTION_VISUAL_RTL;
        int yehHamzaComposeEnabled = (this.options & YEHHAMZA_MASK) == YEHHAMZA_TWOCELL_NEAR ? LINKR : TEXT_DIRECTION_VISUAL_RTL;
        int seenComposeEnabled = (this.options & SEEN_MASK) == SEEN_TWOCELL_NEAR ? LINKR : TEXT_DIRECTION_VISUAL_RTL;
        int i = start;
        int e = start + length;
        while (i < e) {
            char ch = dest[i];
            if (yehHamzaComposeEnabled == LINKR && ((ch == HAMZA06_CHAR || ch == HAMZAFE_CHAR) && i < length - 1 && isAlefMaksouraChar(dest[i + LINKR]))) {
                dest[i] = SPACE_CHAR;
                dest[i + LINKR] = YEH_HAMZA_CHAR;
            } else if (seenComposeEnabled == LINKR && isTailChar(ch) && i < length - 1 && isSeenTailFamilyChar(dest[i + LINKR]) == LINKR) {
                dest[i] = SPACE_CHAR;
            } else if (ch >= '\ufe70' && ch <= '\ufefc') {
                if (isLamAlefChar(ch)) {
                    lacount += LINKR;
                }
                dest[i] = (char) convertFEto06[ch - 65136];
            }
            i += LINKR;
        }
        return lacount;
    }

    private int shapeUnicode(char[] dest, int start, int length, int destSize, int tashkeelFlag) throws ArabicShapingException {
        int lamalef_count = normalize(dest, start, length);
        boolean lamalef_found = false;
        boolean seenfam_found = false;
        boolean yehhamza_found = false;
        boolean tashkeel_found = false;
        int i = (start + length) - 1;
        int currLink = getLink(dest[i]);
        int nextLink = TEXT_DIRECTION_VISUAL_RTL;
        int prevLink = TEXT_DIRECTION_VISUAL_RTL;
        int lastLink = TEXT_DIRECTION_VISUAL_RTL;
        int lastPos = i;
        int nx = -2;
        while (i >= 0) {
            if ((Normalizer2Impl.JAMO_VT & currLink) > 0 || isTashkeelChar(dest[i])) {
                int nw = i - 1;
                nx = -2;
                while (nx < 0) {
                    if (nw == -1) {
                        nextLink = TEXT_DIRECTION_VISUAL_RTL;
                        nx = AnnualTimeZoneRule.MAX_YEAR;
                    } else {
                        nextLink = getLink(dest[nw]);
                        if ((nextLink & TEXT_DIRECTION_VISUAL_LTR) == 0) {
                            nx = nw;
                        } else {
                            nw--;
                        }
                    }
                }
                if ((currLink & DIGITS_EN2AN) > 0 && (lastLink & LETTERS_UNSHAPE) > 0) {
                    lamalef_found = true;
                    char wLamalef = changeLamAlef(dest[i]);
                    if (wLamalef != '\u0000') {
                        dest[i] = LAMALEF_SPACE_SUB;
                        dest[lastPos] = wLamalef;
                        i = lastPos;
                    }
                    lastLink = prevLink;
                    currLink = getLink(wLamalef);
                }
                if (i <= 0 || dest[i - 1] != SPACE_CHAR) {
                    if (i == 0) {
                        if (isSeenFamilyChar(dest[i]) == LINKR) {
                            seenfam_found = true;
                        } else if (dest[i] == YEH_HAMZA_CHAR) {
                            yehhamza_found = true;
                        }
                    }
                } else if (isSeenFamilyChar(dest[i]) == LINKR) {
                    seenfam_found = true;
                } else if (dest[i] == YEH_HAMZA_CHAR) {
                    yehhamza_found = true;
                }
                int flag = specialChar(dest[i]);
                int shape = shapeTable[nextLink & LINK_MASK][lastLink & LINK_MASK][currLink & LINK_MASK];
                if (flag == LINKR) {
                    shape &= LINKR;
                } else if (flag == LINKL) {
                    if (tashkeelFlag == 0 && (lastLink & LINKL) != 0 && (nextLink & LINKR) != 0 && dest[i] != '\u064c' && dest[i] != '\u064d' && ((nextLink & DIGITS_EN2AN) != DIGITS_EN2AN || (lastLink & LETTERS_UNSHAPE) != LETTERS_UNSHAPE)) {
                        shape = LINKR;
                    } else if (tashkeelFlag == LINKL && dest[i] == SHADDA06_CHAR) {
                        shape = LINKR;
                    } else {
                        shape = TEXT_DIRECTION_VISUAL_RTL;
                    }
                }
                if (flag != LINKL) {
                    dest[i] = (char) (((currLink >> LETTERS_SHAPE) + 65136) + shape);
                } else if (tashkeelFlag != LINKL || dest[i] == SHADDA06_CHAR) {
                    dest[i] = (char) ((irrelevantPos[dest[i] - 1611] + 65136) + shape);
                } else {
                    dest[i] = TASHKEEL_SPACE_SUB;
                    tashkeel_found = true;
                }
            }
            if ((currLink & TEXT_DIRECTION_VISUAL_LTR) == 0) {
                prevLink = lastLink;
                lastLink = currLink;
                lastPos = i;
            }
            i--;
            if (i == nx) {
                currLink = nextLink;
                nx = -2;
            } else if (i != -1) {
                currLink = getLink(dest[i]);
            }
        }
        destSize = length;
        if (lamalef_found || tashkeel_found) {
            destSize = handleGeneratedSpaces(dest, start, length);
        }
        if (seenfam_found || yehhamza_found) {
            return expandCompositChar(dest, start, destSize, lamalef_count, TEXT_DIRECTION_VISUAL_RTL);
        }
        return destSize;
    }

    private int deShapeUnicode(char[] dest, int start, int length, int destSize) throws ArabicShapingException {
        int lamalef_count = deshapeNormalize(dest, start, length);
        if (lamalef_count != 0) {
            return expandCompositChar(dest, start, length, lamalef_count, LINKR);
        }
        return length;
    }

    private int internalShape(char[] source, int sourceStart, int sourceLength, char[] dest, int destStart, int destSize) throws ArabicShapingException {
        if (sourceLength == 0) {
            return TEXT_DIRECTION_VISUAL_RTL;
        }
        if (destSize != 0) {
            char[] temp = new char[(sourceLength * LINKL)];
            System.arraycopy(source, sourceStart, temp, TEXT_DIRECTION_VISUAL_RTL, sourceLength);
            if (this.isLogical) {
                invertBuffer(temp, TEXT_DIRECTION_VISUAL_RTL, sourceLength);
            }
            int outputSize = sourceLength;
            switch (this.options & LETTERS_SHAPE_TASHKEEL_ISOLATED) {
                case LETTERS_SHAPE /*8*/:
                    if ((this.options & TASHKEEL_MASK) > 0 && (this.options & TASHKEEL_MASK) != TASHKEEL_REPLACE_BY_TATWEEL) {
                        outputSize = shapeUnicode(temp, TEXT_DIRECTION_VISUAL_RTL, sourceLength, destSize, LINKL);
                        break;
                    }
                    outputSize = shapeUnicode(temp, TEXT_DIRECTION_VISUAL_RTL, sourceLength, destSize, TEXT_DIRECTION_VISUAL_RTL);
                    if ((this.options & TASHKEEL_MASK) == TASHKEEL_REPLACE_BY_TATWEEL) {
                        outputSize = handleTashkeelWithTatweel(temp, sourceLength);
                        break;
                    }
                    break;
                case LETTERS_UNSHAPE /*16*/:
                    outputSize = deShapeUnicode(temp, TEXT_DIRECTION_VISUAL_RTL, sourceLength, destSize);
                    break;
                case LETTERS_SHAPE_TASHKEEL_ISOLATED /*24*/:
                    outputSize = shapeUnicode(temp, TEXT_DIRECTION_VISUAL_RTL, sourceLength, destSize, LINKR);
                    break;
            }
            if (outputSize > destSize) {
                throw new ArabicShapingException("not enough room for result data");
            }
            if ((this.options & DIGITS_MASK) != 0) {
                char digitBase = '0';
                switch (this.options & DIGIT_TYPE_MASK) {
                    case TEXT_DIRECTION_VISUAL_RTL /*0*/:
                        digitBase = '\u0660';
                        break;
                    case DIGIT_TYPE_MASK /*256*/:
                        digitBase = '\u06f0';
                        break;
                }
                int digitDelta;
                int i;
                char ch;
                switch (this.options & DIGITS_MASK) {
                    case DIGITS_EN2AN /*32*/:
                        digitDelta = digitBase - 48;
                        for (i = TEXT_DIRECTION_VISUAL_RTL; i < outputSize; i += LINKR) {
                            ch = temp[i];
                            if (ch <= '9' && ch >= '0') {
                                temp[i] = (char) (temp[i] + digitDelta);
                            }
                        }
                        break;
                    case DIGITS_AN2EN /*64*/:
                        char digitTop = (char) (digitBase + 9);
                        digitDelta = 48 - digitBase;
                        for (i = TEXT_DIRECTION_VISUAL_RTL; i < outputSize; i += LINKR) {
                            ch = temp[i];
                            if (ch <= digitTop && ch >= digitBase) {
                                temp[i] = (char) (temp[i] + digitDelta);
                            }
                        }
                        break;
                    case DIGITS_EN2AN_INIT_LR /*96*/:
                        shapeToArabicDigitsWithContext(temp, TEXT_DIRECTION_VISUAL_RTL, outputSize, digitBase, false);
                        break;
                    case DIGITS_EN2AN_INIT_AL /*128*/:
                        shapeToArabicDigitsWithContext(temp, TEXT_DIRECTION_VISUAL_RTL, outputSize, digitBase, true);
                        break;
                }
            }
            if (this.isLogical) {
                invertBuffer(temp, TEXT_DIRECTION_VISUAL_RTL, outputSize);
            }
            System.arraycopy(temp, TEXT_DIRECTION_VISUAL_RTL, dest, destStart, outputSize);
            return outputSize;
        } else if ((this.options & LETTERS_SHAPE_TASHKEEL_ISOLATED) == 0 || (this.options & LENGTH_MASK) != 0) {
            return sourceLength;
        } else {
            return calculateSize(source, sourceStart, sourceLength);
        }
    }
}
