package android.icu.impl;

import android.icu.lang.UCharacter;
import android.icu.lang.UScript;
import android.icu.text.IDNA;
import android.icu.text.IDNA.Error;
import android.icu.text.IDNA.Info;
import android.icu.text.Normalizer2;
import android.icu.text.StringPrepParseException;
import android.icu.util.AnnualTimeZoneRule;
import android.icu.util.ICUException;
import android.icu.util.ULocale;
import com.android.dex.DexFormat;
import dalvik.bytecode.Opcodes;
import java.util.EnumSet;

public final class UTS46 extends IDNA {
    private static final int EN_AN_MASK = 0;
    private static final int ES_CS_ET_ON_BN_NSM_MASK = 0;
    private static final int L_EN_ES_CS_ET_ON_BN_NSM_MASK = 0;
    private static final int L_EN_MASK = 0;
    private static final int L_MASK = 0;
    private static final int L_R_AL_MASK = 0;
    private static final int R_AL_AN_EN_ES_CS_ET_ON_BN_NSM_MASK = 0;
    private static final int R_AL_AN_MASK = 0;
    private static final int R_AL_EN_AN_MASK = 0;
    private static final int R_AL_MASK = 0;
    private static int U_GC_M_MASK;
    private static final byte[] asciiData = null;
    private static final EnumSet<Error> severeErrors = null;
    private static final Normalizer2 uts46Norm2 = null;
    final int options;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.UTS46.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.UTS46.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.UTS46.<clinit>():void");
    }

    private void checkLabelBiDi(java.lang.CharSequence r1, int r2, int r3, android.icu.text.IDNA.Info r4) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.UTS46.checkLabelBiDi(java.lang.CharSequence, int, int, android.icu.text.IDNA$Info):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.UTS46.checkLabelBiDi(java.lang.CharSequence, int, int, android.icu.text.IDNA$Info):void");
    }

    public UTS46(int options) {
        this.options = options;
    }

    public StringBuilder labelToASCII(CharSequence label, StringBuilder dest, Info info) {
        return process(label, true, true, dest, info);
    }

    public StringBuilder labelToUnicode(CharSequence label, StringBuilder dest, Info info) {
        return process(label, true, false, dest, info);
    }

    public StringBuilder nameToASCII(CharSequence name, StringBuilder dest, Info info) {
        process(name, false, true, dest, info);
        if (dest.length() >= SCSU.KATAKANAINDEX && !info.getErrors().contains(Error.DOMAIN_NAME_TOO_LONG) && isASCIIString(dest) && (dest.length() > SCSU.KATAKANAINDEX || dest.charAt(SCSU.HIRAGANAINDEX) != '.')) {
            IDNA.addError(info, Error.DOMAIN_NAME_TOO_LONG);
        }
        return dest;
    }

    public StringBuilder nameToUnicode(CharSequence name, StringBuilder dest, Info info) {
        return process(name, false, false, dest, info);
    }

    private static boolean isASCIIString(CharSequence dest) {
        int length = dest.length();
        for (int i = 0; i < length; i++) {
            if (dest.charAt(i) > '\u007f') {
                return false;
            }
        }
        return true;
    }

    private StringBuilder process(CharSequence src, boolean isLabel, boolean toASCII, StringBuilder dest, Info info) {
        if (dest == src) {
            throw new IllegalArgumentException();
        }
        dest.delete(0, AnnualTimeZoneRule.MAX_YEAR);
        IDNA.resetInfo(info);
        int srcLength = src.length();
        if (srcLength == 0) {
            IDNA.addError(info, Error.EMPTY_LABEL);
            return dest;
        }
        boolean disallowNonLDHDot = (this.options & 2) != 0;
        int labelStart = 0;
        int i = 0;
        while (i != srcLength) {
            char c = src.charAt(i);
            if (c <= '\u007f') {
                int cData = asciiData[c];
                if (cData > 0) {
                    dest.append((char) (c + 32));
                } else if (cData >= 0 || !disallowNonLDHDot) {
                    dest.append(c);
                    if (c == '-') {
                        if (i == labelStart + 3 && src.charAt(i - 1) == '-') {
                            i++;
                        } else {
                            if (i == labelStart) {
                                IDNA.addLabelError(info, Error.LEADING_HYPHEN);
                            }
                            if (i + 1 == srcLength || src.charAt(i + 1) == '.') {
                                IDNA.addLabelError(info, Error.TRAILING_HYPHEN);
                            }
                        }
                    } else if (c != '.') {
                        continue;
                    } else if (isLabel) {
                        i++;
                    } else {
                        if (i == labelStart) {
                            IDNA.addLabelError(info, Error.EMPTY_LABEL);
                        }
                        if (toASCII && i - labelStart > 63) {
                            IDNA.addLabelError(info, Error.LABEL_TOO_LONG);
                        }
                        IDNA.promoteAndResetLabelErrors(info);
                        labelStart = i + 1;
                    }
                }
                i++;
            }
            IDNA.promoteAndResetLabelErrors(info);
            processUnicode(src, labelStart, i, isLabel, toASCII, dest, info);
            if (IDNA.isBiDi(info)) {
                if (!IDNA.hasCertainErrors(info, severeErrors) && (!IDNA.isOkBiDi(info) || (labelStart > 0 && !isASCIIOkBiDi(dest, labelStart)))) {
                    IDNA.addError(info, Error.BIDI);
                }
            }
            return dest;
        }
        if (toASCII) {
            if (i - labelStart > 63) {
                IDNA.addLabelError(info, Error.LABEL_TOO_LONG);
            }
            if (!isLabel && i >= SCSU.KATAKANAINDEX && (i > SCSU.KATAKANAINDEX || labelStart < i)) {
                IDNA.addError(info, Error.DOMAIN_NAME_TOO_LONG);
            }
        }
        IDNA.promoteAndResetLabelErrors(info);
        return dest;
    }

    private StringBuilder processUnicode(CharSequence src, int labelStart, int mappingStart, boolean isLabel, boolean toASCII, StringBuilder dest, Info info) {
        if (mappingStart == 0) {
            uts46Norm2.normalize(src, dest);
        } else {
            uts46Norm2.normalizeSecondAndAppend(dest, src.subSequence(mappingStart, src.length()));
        }
        boolean doMapDevChars = toASCII ? (this.options & 16) == 0 : (this.options & 32) == 0;
        int destLength = dest.length();
        int labelLimit = labelStart;
        while (labelLimit < destLength) {
            char c = dest.charAt(labelLimit);
            if (c == '.' && !isLabel) {
                int labelLength = labelLimit - labelStart;
                int newLength = processLabel(dest, labelStart, labelLength, toASCII, info);
                IDNA.promoteAndResetLabelErrors(info);
                destLength += newLength - labelLength;
                labelStart += newLength + 1;
                labelLimit = labelStart;
            } else if ('\u00df' > c || c > '\u200d' || !(c == '\u00df' || c == '\u03c2' || c >= '\u200c')) {
                labelLimit++;
            } else {
                IDNA.setTransitionalDifferent(info);
                if (doMapDevChars) {
                    destLength = mapDevChars(dest, labelStart, labelLimit);
                    doMapDevChars = false;
                } else {
                    labelLimit++;
                }
            }
        }
        if (labelStart == 0 || labelStart < labelLimit) {
            processLabel(dest, labelStart, labelLimit - labelStart, toASCII, info);
            IDNA.promoteAndResetLabelErrors(info);
        }
        return dest;
    }

    private int mapDevChars(StringBuilder dest, int labelStart, int mappingStart) {
        int length = dest.length();
        boolean didMapDevChars = false;
        int i = mappingStart;
        while (i < length) {
            int i2;
            switch (dest.charAt(i)) {
                case Opcodes.OP_XOR_INT_LIT8 /*223*/:
                    didMapDevChars = true;
                    i2 = i + 1;
                    dest.setCharAt(i, 's');
                    i = i2 + 1;
                    dest.insert(i2, 's');
                    length++;
                    i2 = i;
                    break;
                case '\u03c2':
                    didMapDevChars = true;
                    i2 = i + 1;
                    dest.setCharAt(i, '\u03c3');
                    break;
                case '\u200c':
                case '\u200d':
                    didMapDevChars = true;
                    dest.delete(i, i + 1);
                    length--;
                    i2 = i;
                    break;
                default:
                    i2 = i + 1;
                    break;
            }
            i = i2;
        }
        if (!didMapDevChars) {
            return length;
        }
        dest.replace(labelStart, AnnualTimeZoneRule.MAX_YEAR, uts46Norm2.normalize(dest.subSequence(labelStart, dest.length())));
        return dest.length();
    }

    private static boolean isNonASCIIDisallowedSTD3Valid(int c) {
        return c == 8800 || c == 8814 || c == 8815;
    }

    private static int replaceLabel(StringBuilder dest, int destLabelStart, int destLabelLength, CharSequence label, int labelLength) {
        if (label != dest) {
            dest.delete(destLabelStart, destLabelStart + destLabelLength).insert(destLabelStart, label);
        }
        return labelLength;
    }

    private int processLabel(StringBuilder dest, int labelStart, int labelLength, boolean toASCII, Info info) {
        boolean wasPunycode;
        StringBuilder labelString;
        int i;
        int limit;
        int oredChars;
        boolean disallowNonLDHDot;
        char c;
        int c2;
        int destLabelStart = labelStart;
        int destLabelLength = labelLength;
        if (labelLength >= 4 && dest.charAt(labelStart) == ULocale.PRIVATE_USE_EXTENSION) {
            if (dest.charAt(labelStart + 1) == 'n') {
                if (dest.charAt(labelStart + 2) == '-') {
                    if (dest.charAt(labelStart + 3) == '-') {
                        wasPunycode = true;
                        try {
                            StringBuilder fromPunycode = Punycode.decode(dest.subSequence(labelStart + 4, labelStart + labelLength), null);
                            if (uts46Norm2.isNormalized(fromPunycode)) {
                                labelString = fromPunycode;
                                labelStart = 0;
                                labelLength = fromPunycode.length();
                                if (labelLength != 0) {
                                    IDNA.addLabelError(info, Error.EMPTY_LABEL);
                                    return replaceLabel(dest, destLabelStart, destLabelLength, labelString, labelLength);
                                }
                                if (labelLength >= 4) {
                                    if (labelString.charAt(labelStart + 2) == '-') {
                                        if (labelString.charAt(labelStart + 3) == '-') {
                                            IDNA.addLabelError(info, Error.HYPHEN_3_4);
                                        }
                                    }
                                }
                                if (labelString.charAt(labelStart) == '-') {
                                    IDNA.addLabelError(info, Error.LEADING_HYPHEN);
                                }
                                if (labelString.charAt((labelStart + labelLength) - 1) == '-') {
                                    IDNA.addLabelError(info, Error.TRAILING_HYPHEN);
                                }
                                i = labelStart;
                                limit = labelStart + labelLength;
                                oredChars = 0;
                                disallowNonLDHDot = (this.options & 2) == 0;
                                do {
                                    c = labelString.charAt(i);
                                    if (c <= '\u007f') {
                                        char oredChars2 = (char) (oredChars | c);
                                        if (!disallowNonLDHDot && isNonASCIIDisallowedSTD3Valid(c)) {
                                            IDNA.addLabelError(info, Error.DISALLOWED);
                                            labelString.setCharAt(i, '\ufffd');
                                        } else if (c == '\ufffd') {
                                            IDNA.addLabelError(info, Error.DISALLOWED);
                                        }
                                    } else if (c != '.') {
                                        IDNA.addLabelError(info, Error.LABEL_HAS_DOT);
                                        labelString.setCharAt(i, '\ufffd');
                                    } else if (disallowNonLDHDot && asciiData[c] < null) {
                                        IDNA.addLabelError(info, Error.DISALLOWED);
                                        labelString.setCharAt(i, '\ufffd');
                                    }
                                    i++;
                                } while (i < limit);
                                c2 = labelString.codePointAt(labelStart);
                                if ((U_GET_GC_MASK(c2) & U_GC_M_MASK) != 0) {
                                    IDNA.addLabelError(info, Error.LEADING_COMBINING_MARK);
                                    labelString.setCharAt(labelStart, '\ufffd');
                                    if (c2 > DexFormat.MAX_TYPE_IDX) {
                                        labelString.deleteCharAt(labelStart + 1);
                                        labelLength--;
                                        if (labelString == dest) {
                                            destLabelLength--;
                                        }
                                    }
                                }
                                if (!IDNA.hasCertainLabelErrors(info, severeErrors)) {
                                    if ((this.options & 4) != 0 && (!IDNA.isBiDi(info) || IDNA.isOkBiDi(info))) {
                                        checkLabelBiDi(labelString, labelStart, labelLength, info);
                                    }
                                    if (!((this.options & 8) == 0 || (oredChars & 8204) != 8204 || isLabelOkContextJ(labelString, labelStart, labelLength))) {
                                        IDNA.addLabelError(info, Error.CONTEXTJ);
                                    }
                                    if ((this.options & 64) != 0 && oredChars >= 183) {
                                        checkLabelContextO(labelString, labelStart, labelLength, info);
                                    }
                                    if (toASCII) {
                                        if (wasPunycode) {
                                            if (destLabelLength > 63) {
                                                IDNA.addLabelError(info, Error.LABEL_TOO_LONG);
                                            }
                                            return destLabelLength;
                                        } else if (oredChars >= 128) {
                                            try {
                                                StringBuilder punycode;
                                                punycode = Punycode.encode(labelString.subSequence(labelStart, labelStart + labelLength), null);
                                                punycode.insert(0, "xn--");
                                                if (punycode.length() > 63) {
                                                    IDNA.addLabelError(info, Error.LABEL_TOO_LONG);
                                                }
                                                return replaceLabel(dest, destLabelStart, destLabelLength, punycode, punycode.length());
                                            } catch (Throwable e) {
                                                throw new ICUException(e);
                                            }
                                        } else if (labelLength > 63) {
                                            IDNA.addLabelError(info, Error.LABEL_TOO_LONG);
                                        }
                                    }
                                } else if (wasPunycode) {
                                    IDNA.addLabelError(info, Error.INVALID_ACE_LABEL);
                                    return markBadACELabel(dest, destLabelStart, destLabelLength, toASCII, info);
                                }
                                return replaceLabel(dest, destLabelStart, destLabelLength, labelString, labelLength);
                            }
                            IDNA.addLabelError(info, Error.INVALID_ACE_LABEL);
                            return markBadACELabel(dest, labelStart, labelLength, toASCII, info);
                        } catch (StringPrepParseException e2) {
                            IDNA.addLabelError(info, Error.PUNYCODE);
                            return markBadACELabel(dest, labelStart, labelLength, toASCII, info);
                        }
                    }
                }
            }
        }
        wasPunycode = false;
        labelString = dest;
        if (labelLength != 0) {
            if (labelLength >= 4) {
                if (labelString.charAt(labelStart + 2) == '-') {
                    if (labelString.charAt(labelStart + 3) == '-') {
                        IDNA.addLabelError(info, Error.HYPHEN_3_4);
                    }
                }
            }
            if (labelString.charAt(labelStart) == '-') {
                IDNA.addLabelError(info, Error.LEADING_HYPHEN);
            }
            if (labelString.charAt((labelStart + labelLength) - 1) == '-') {
                IDNA.addLabelError(info, Error.TRAILING_HYPHEN);
            }
            i = labelStart;
            limit = labelStart + labelLength;
            oredChars = 0;
            if ((this.options & 2) == 0) {
            }
            do {
                c = labelString.charAt(i);
                if (c <= '\u007f') {
                    char oredChars22 = (char) (oredChars | c);
                    if (!disallowNonLDHDot) {
                    }
                    if (c == '\ufffd') {
                        IDNA.addLabelError(info, Error.DISALLOWED);
                    }
                } else if (c != '.') {
                    IDNA.addLabelError(info, Error.DISALLOWED);
                    labelString.setCharAt(i, '\ufffd');
                } else {
                    IDNA.addLabelError(info, Error.LABEL_HAS_DOT);
                    labelString.setCharAt(i, '\ufffd');
                }
                i++;
            } while (i < limit);
            c2 = labelString.codePointAt(labelStart);
            if ((U_GET_GC_MASK(c2) & U_GC_M_MASK) != 0) {
                IDNA.addLabelError(info, Error.LEADING_COMBINING_MARK);
                labelString.setCharAt(labelStart, '\ufffd');
                if (c2 > DexFormat.MAX_TYPE_IDX) {
                    labelString.deleteCharAt(labelStart + 1);
                    labelLength--;
                    if (labelString == dest) {
                        destLabelLength--;
                    }
                }
            }
            if (!IDNA.hasCertainLabelErrors(info, severeErrors)) {
                checkLabelBiDi(labelString, labelStart, labelLength, info);
                IDNA.addLabelError(info, Error.CONTEXTJ);
                checkLabelContextO(labelString, labelStart, labelLength, info);
                if (toASCII) {
                    if (wasPunycode) {
                        if (destLabelLength > 63) {
                            IDNA.addLabelError(info, Error.LABEL_TOO_LONG);
                        }
                        return destLabelLength;
                    } else if (oredChars >= 128) {
                        punycode = Punycode.encode(labelString.subSequence(labelStart, labelStart + labelLength), null);
                        punycode.insert(0, "xn--");
                        if (punycode.length() > 63) {
                            IDNA.addLabelError(info, Error.LABEL_TOO_LONG);
                        }
                        return replaceLabel(dest, destLabelStart, destLabelLength, punycode, punycode.length());
                    } else if (labelLength > 63) {
                        IDNA.addLabelError(info, Error.LABEL_TOO_LONG);
                    }
                }
            } else if (wasPunycode) {
                IDNA.addLabelError(info, Error.INVALID_ACE_LABEL);
                return markBadACELabel(dest, destLabelStart, destLabelLength, toASCII, info);
            }
            return replaceLabel(dest, destLabelStart, destLabelLength, labelString, labelLength);
        }
        IDNA.addLabelError(info, Error.EMPTY_LABEL);
        return replaceLabel(dest, destLabelStart, destLabelLength, labelString, labelLength);
    }

    private int markBadACELabel(StringBuilder dest, int labelStart, int labelLength, boolean toASCII, Info info) {
        boolean disallowNonLDHDot = (this.options & 2) != 0;
        boolean isASCII = true;
        boolean onlyLDH = true;
        int i = labelStart + 4;
        int limit = labelStart + labelLength;
        do {
            char c = dest.charAt(i);
            if (c > '\u007f') {
                onlyLDH = false;
                isASCII = false;
            } else if (c == '.') {
                IDNA.addLabelError(info, Error.LABEL_HAS_DOT);
                dest.setCharAt(i, '\ufffd');
                onlyLDH = false;
                isASCII = false;
            } else if (asciiData[c] < null) {
                onlyLDH = false;
                if (disallowNonLDHDot) {
                    dest.setCharAt(i, '\ufffd');
                    isASCII = false;
                }
            }
            i++;
        } while (i < limit);
        if (onlyLDH) {
            dest.insert(labelStart + labelLength, '\ufffd');
            return labelLength + 1;
        } else if (!toASCII || !isASCII || labelLength <= 63) {
            return labelLength;
        } else {
            IDNA.addLabelError(info, Error.LABEL_TOO_LONG);
            return labelLength;
        }
    }

    private static boolean isASCIIOkBiDi(CharSequence s, int length) {
        int labelStart = 0;
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            if (c == '.') {
                if (i > labelStart) {
                    c = s.charAt(i - 1);
                    if (('a' > c || c > 'z') && ('0' > c || c > '9')) {
                        return false;
                    }
                }
                labelStart = i + 1;
            } else if (i == labelStart) {
                if ('a' > c || c > 'z') {
                    return false;
                }
            } else if (c <= ' ' && (c >= '\u001c' || ('\t' <= c && c <= '\r'))) {
                return false;
            }
        }
        return true;
    }

    private boolean isLabelOkContextJ(CharSequence label, int labelStart, int labelLength) {
        int labelLimit = labelStart + labelLength;
        for (int i = labelStart; i < labelLimit; i++) {
            if (label.charAt(i) == '\u200c') {
                if (i == labelStart) {
                    return false;
                }
                int j = i;
                int c = Character.codePointBefore(label, i);
                j -= Character.charCount(c);
                if (uts46Norm2.getCombiningClass(c) == 9) {
                    continue;
                } else {
                    int type;
                    while (true) {
                        type = UBiDiProps.INSTANCE.getJoiningType(c);
                        if (type != 5) {
                            break;
                        } else if (j == 0) {
                            return false;
                        } else {
                            c = Character.codePointBefore(label, j);
                            j -= Character.charCount(c);
                        }
                    }
                    if (type != 3 && type != 2) {
                        return false;
                    }
                    j = i + 1;
                    while (j != labelLimit) {
                        c = Character.codePointAt(label, j);
                        j += Character.charCount(c);
                        type = UBiDiProps.INSTANCE.getJoiningType(c);
                        if (type != 5) {
                            if (!(type == 4 || type == 2)) {
                                return false;
                            }
                        }
                    }
                    return false;
                }
            } else if (label.charAt(i) != '\u200d') {
                continue;
            } else if (i == labelStart) {
                return false;
            } else {
                if (uts46Norm2.getCombiningClass(Character.codePointBefore(label, i)) != 9) {
                    return false;
                }
            }
        }
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void checkLabelContextO(CharSequence label, int labelStart, int labelLength, Info info) {
        int labelEnd = (labelStart + labelLength) - 1;
        int arabicDigits = 0;
        int i = labelStart;
        while (i <= labelEnd) {
            int c = label.charAt(i);
            if (c >= Opcodes.OP_XOR_INT_2ADDR) {
                if (c <= 1785) {
                    if (c == Opcodes.OP_XOR_INT_2ADDR) {
                        if (labelStart < i && label.charAt(i - 1) == 'l' && i < labelEnd) {
                            if (label.charAt(i + 1) != 'l') {
                            }
                        }
                        IDNA.addLabelError(info, Error.CONTEXTO_PUNCTUATION);
                    } else if (c == 885) {
                        if (i >= labelEnd || 14 != UScript.getScript(Character.codePointAt(label, i + 1))) {
                            IDNA.addLabelError(info, Error.CONTEXTO_PUNCTUATION);
                        }
                    } else if (c == 1523 || c == 1524) {
                        if (labelStart >= i || 19 != UScript.getScript(Character.codePointBefore(label, i))) {
                            IDNA.addLabelError(info, Error.CONTEXTO_PUNCTUATION);
                        }
                    } else if (1632 <= c) {
                        if (c <= 1641) {
                            if (arabicDigits > 0) {
                                IDNA.addLabelError(info, Error.CONTEXTO_DIGITS);
                            }
                            arabicDigits = -1;
                        } else if (1776 <= c) {
                            if (arabicDigits < 0) {
                                IDNA.addLabelError(info, Error.CONTEXTO_DIGITS);
                            }
                            arabicDigits = 1;
                        }
                    }
                } else if (c == 12539) {
                    int j = labelStart;
                    while (j <= labelEnd) {
                        c = Character.codePointAt(label, j);
                        int script = UScript.getScript(c);
                        if (!(script == 20 || script == 22 || script == 17)) {
                            j += Character.charCount(c);
                        }
                    }
                    IDNA.addLabelError(info, Error.CONTEXTO_PUNCTUATION);
                }
            }
            i++;
        }
    }

    private static int U_MASK(int x) {
        return 1 << x;
    }

    private static int U_GET_GC_MASK(int c) {
        return 1 << UCharacter.getType(c);
    }
}
