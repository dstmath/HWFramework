package android.icu.impl;

import android.icu.lang.UCharacter;
import android.icu.lang.UScript;
import android.icu.text.IDNA;
import android.icu.text.Normalizer2;
import android.icu.text.StringPrepParseException;
import android.icu.util.ICUException;
import java.util.EnumSet;

public final class UTS46 extends IDNA {
    private static final int EN_AN_MASK = (U_MASK(5) | U_MASK(2));
    private static final int ES_CS_ET_ON_BN_NSM_MASK = (((((U_MASK(3) | U_MASK(6)) | U_MASK(4)) | U_MASK(10)) | U_MASK(18)) | U_MASK(17));
    private static final int L_EN_ES_CS_ET_ON_BN_NSM_MASK = (L_EN_MASK | ES_CS_ET_ON_BN_NSM_MASK);
    private static final int L_EN_MASK = (U_MASK(2) | L_MASK);
    private static final int L_MASK = U_MASK(0);
    private static final int L_R_AL_MASK = (L_MASK | R_AL_MASK);
    private static final int R_AL_AN_EN_ES_CS_ET_ON_BN_NSM_MASK = ((R_AL_MASK | EN_AN_MASK) | ES_CS_ET_ON_BN_NSM_MASK);
    private static final int R_AL_AN_MASK = (R_AL_MASK | U_MASK(5));
    private static final int R_AL_EN_AN_MASK = (R_AL_MASK | EN_AN_MASK);
    private static final int R_AL_MASK = (U_MASK(1) | U_MASK(13));
    private static int U_GC_M_MASK = ((U_MASK(6) | U_MASK(7)) | U_MASK(8));
    private static final byte[] asciiData = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1};
    private static final EnumSet<IDNA.Error> severeErrors = EnumSet.of(IDNA.Error.LEADING_COMBINING_MARK, IDNA.Error.DISALLOWED, IDNA.Error.PUNYCODE, IDNA.Error.LABEL_HAS_DOT, IDNA.Error.INVALID_ACE_LABEL);
    private static final Normalizer2 uts46Norm2 = Normalizer2.getInstance(null, "uts46", Normalizer2.Mode.COMPOSE);
    final int options;

    public UTS46(int options2) {
        this.options = options2;
    }

    public StringBuilder labelToASCII(CharSequence label, StringBuilder dest, IDNA.Info info) {
        return process(label, true, true, dest, info);
    }

    public StringBuilder labelToUnicode(CharSequence label, StringBuilder dest, IDNA.Info info) {
        return process(label, true, false, dest, info);
    }

    public StringBuilder nameToASCII(CharSequence name, StringBuilder dest, IDNA.Info info) {
        process(name, false, true, dest, info);
        if (dest.length() >= 254 && !info.getErrors().contains(IDNA.Error.DOMAIN_NAME_TOO_LONG) && isASCIIString(dest) && (dest.length() > 254 || dest.charAt(253) != '.')) {
            addError(info, IDNA.Error.DOMAIN_NAME_TOO_LONG);
        }
        return dest;
    }

    public StringBuilder nameToUnicode(CharSequence name, StringBuilder dest, IDNA.Info info) {
        return process(name, false, false, dest, info);
    }

    private static boolean isASCIIString(CharSequence dest) {
        int length = dest.length();
        for (int i = 0; i < length; i++) {
            if (dest.charAt(i) > 127) {
                return false;
            }
        }
        return true;
    }

    private StringBuilder process(CharSequence src, boolean isLabel, boolean toASCII, StringBuilder dest, IDNA.Info info) {
        int i;
        int i2;
        CharSequence charSequence = src;
        StringBuilder sb = dest;
        IDNA.Info info2 = info;
        if (sb != charSequence) {
            int i3 = 0;
            sb.delete(0, Integer.MAX_VALUE);
            resetInfo(info);
            int srcLength = src.length();
            if (srcLength == 0) {
                addError(info2, IDNA.Error.EMPTY_LABEL);
                return sb;
            }
            boolean disallowNonLDHDot = (this.options & 2) != 0;
            int labelStart = 0;
            while (true) {
                i = i3;
                if (i == srcLength) {
                    if (toASCII) {
                        if (i - labelStart > 63) {
                            addLabelError(info2, IDNA.Error.LABEL_TOO_LONG);
                        }
                        if (!isLabel && i >= 254 && (i > 254 || labelStart < i)) {
                            addError(info2, IDNA.Error.DOMAIN_NAME_TOO_LONG);
                        }
                    }
                    promoteAndResetLabelErrors(info);
                    return sb;
                }
                char c = charSequence.charAt(i);
                if (c > 127) {
                    break;
                }
                byte cData = asciiData[c];
                if (cData <= 0) {
                    if (cData < 0 && disallowNonLDHDot) {
                        break;
                    }
                    sb.append(c);
                    if (c == '-') {
                        if (i == labelStart + 3 && charSequence.charAt(i - 1) == '-') {
                            i++;
                            break;
                        }
                        if (i == labelStart) {
                            addLabelError(info2, IDNA.Error.LEADING_HYPHEN);
                        }
                        if (i + 1 == srcLength || charSequence.charAt(i + 1) == '.') {
                            addLabelError(info2, IDNA.Error.TRAILING_HYPHEN);
                        }
                    } else if (c != '.') {
                        i2 = labelStart;
                        labelStart = i2;
                        i3 = i + 1;
                        charSequence = src;
                    } else if (isLabel) {
                        i++;
                        break;
                    } else {
                        int labelStart2 = labelStart;
                        if (i == labelStart2) {
                            addLabelError(info2, IDNA.Error.EMPTY_LABEL);
                        }
                        if (toASCII && i - labelStart2 > 63) {
                            addLabelError(info2, IDNA.Error.LABEL_TOO_LONG);
                        }
                        promoteAndResetLabelErrors(info);
                        labelStart = i + 1;
                        i3 = i + 1;
                        charSequence = src;
                    }
                } else {
                    sb.append((char) (c + ' '));
                }
                i2 = labelStart;
                labelStart = i2;
                i3 = i + 1;
                charSequence = src;
            }
            int i4 = i;
            promoteAndResetLabelErrors(info);
            int labelStart3 = labelStart;
            processUnicode(charSequence, labelStart, i4, isLabel, toASCII, sb, info2);
            if (isBiDi(info) && !hasCertainErrors(info2, severeErrors) && (!isOkBiDi(info) || (labelStart3 > 0 && !isASCIIOkBiDi(sb, labelStart3)))) {
                addError(info2, IDNA.Error.BIDI);
            }
            return sb;
        }
        throw new IllegalArgumentException();
    }

    private StringBuilder processUnicode(CharSequence src, int labelStart, int mappingStart, boolean isLabel, boolean toASCII, StringBuilder dest, IDNA.Info info) {
        int labelLimit;
        CharSequence charSequence = src;
        int i = mappingStart;
        StringBuilder sb = dest;
        if (i == 0) {
            uts46Norm2.normalize(charSequence, sb);
        } else {
            uts46Norm2.normalizeSecondAndAppend(sb, charSequence.subSequence(i, src.length()));
        }
        boolean doMapDevChars = false;
        if (!toASCII ? (this.options & 32) == 0 : (this.options & 16) == 0) {
            doMapDevChars = true;
        }
        int labelStart2 = labelStart;
        boolean doMapDevChars2 = doMapDevChars;
        int destLength = dest.length();
        int newLength = labelStart2;
        while (true) {
            labelLimit = newLength;
            if (labelLimit >= destLength) {
                break;
            }
            char c = sb.charAt(labelLimit);
            if (c != '.' || isLabel) {
                if (223 > c || c > 8205 || !(c == 223 || c == 962 || c >= 8204)) {
                    labelLimit++;
                } else {
                    setTransitionalDifferent(info);
                    if (doMapDevChars2) {
                        destLength = mapDevChars(sb, labelStart2, labelLimit);
                        doMapDevChars2 = false;
                    } else {
                        labelLimit++;
                    }
                }
                newLength = labelLimit;
            } else {
                int labelLength = labelLimit - labelStart2;
                int newLength2 = processLabel(sb, labelStart2, labelLength, toASCII, info);
                promoteAndResetLabelErrors(info);
                destLength += newLength2 - labelLength;
                int labelStart3 = newLength2 + 1 + labelStart2;
                newLength = labelStart3;
                labelStart2 = labelStart3;
            }
        }
        if (labelStart2 == 0 || labelStart2 < labelLimit) {
            processLabel(sb, labelStart2, labelLimit - labelStart2, toASCII, info);
            promoteAndResetLabelErrors(info);
        }
        return sb;
    }

    private int mapDevChars(StringBuilder dest, int labelStart, int mappingStart) {
        boolean didMapDevChars = false;
        int length = dest.length();
        int i = mappingStart;
        while (i < length) {
            char c = dest.charAt(i);
            if (c == 223) {
                didMapDevChars = true;
                int i2 = i + 1;
                dest.setCharAt(i, 's');
                i = i2 + 1;
                dest.insert(i2, 's');
                length++;
            } else if (c != 962) {
                switch (c) {
                    case 8204:
                    case 8205:
                        didMapDevChars = true;
                        dest.delete(i, i + 1);
                        length--;
                        break;
                    default:
                        i++;
                        break;
                }
            } else {
                didMapDevChars = true;
                dest.setCharAt(i, 963);
                i++;
            }
        }
        if (!didMapDevChars) {
            return length;
        }
        dest.replace(labelStart, Integer.MAX_VALUE, uts46Norm2.normalize(dest.subSequence(labelStart, dest.length())));
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

    private int processLabel(StringBuilder dest, int labelStart, int labelLength, boolean toASCII, IDNA.Info info) {
        StringBuilder labelString;
        StringBuilder fromPunycode;
        int labelStart2;
        char oredChars;
        int destLabelLength;
        int labelLength2;
        StringBuilder sb = dest;
        int labelLength3 = labelLength;
        IDNA.Info info2 = info;
        int destLabelStart = labelStart;
        int destLabelLength2 = labelLength3;
        if (labelLength3 >= 4 && dest.charAt(labelStart) == 'x' && sb.charAt(labelStart + 1) == 'n' && sb.charAt(labelStart + 2) == '-' && sb.charAt(labelStart + 3) == '-') {
            try {
                StringBuilder fromPunycode2 = Punycode.decode(sb.subSequence(labelStart + 4, labelStart + labelLength3), null);
                if (!uts46Norm2.isNormalized(fromPunycode2)) {
                    addLabelError(info2, IDNA.Error.INVALID_ACE_LABEL);
                    return markBadACELabel(dest, labelStart, labelLength, toASCII, info);
                }
                labelString = fromPunycode2;
                labelLength3 = fromPunycode2.length();
                fromPunycode = 1;
                labelStart2 = 0;
            } catch (StringPrepParseException e) {
                addLabelError(info2, IDNA.Error.PUNYCODE);
                return markBadACELabel(dest, labelStart, labelLength, toASCII, info);
            }
        } else {
            labelString = sb;
            fromPunycode = null;
            labelStart2 = labelStart;
        }
        StringBuilder labelString2 = labelString;
        if (labelLength3 == 0) {
            addLabelError(info2, IDNA.Error.EMPTY_LABEL);
            return replaceLabel(sb, destLabelStart, destLabelLength2, labelString2, labelLength3);
        }
        if (labelLength3 >= 4 && labelString2.charAt(labelStart2 + 2) == '-' && labelString2.charAt(labelStart2 + 3) == '-') {
            addLabelError(info2, IDNA.Error.HYPHEN_3_4);
        }
        if (labelString2.charAt(labelStart2) == '-') {
            addLabelError(info2, IDNA.Error.LEADING_HYPHEN);
        }
        if (labelString2.charAt((labelStart2 + labelLength3) - 1) == '-') {
            addLabelError(info2, IDNA.Error.TRAILING_HYPHEN);
        }
        int i = labelStart2;
        int limit = labelStart2 + labelLength3;
        char oredChars2 = 0;
        boolean disallowNonLDHDot = (this.options & 2) != 0;
        while (true) {
            char c = labelString2.charAt(i);
            if (c > 127) {
                oredChars2 = (char) (oredChars2 | c);
                if (disallowNonLDHDot && isNonASCIIDisallowedSTD3Valid(c)) {
                    addLabelError(info2, IDNA.Error.DISALLOWED);
                    labelString2.setCharAt(i, 65533);
                } else if (c == 65533) {
                    addLabelError(info2, IDNA.Error.DISALLOWED);
                }
            } else if (c == '.') {
                addLabelError(info2, IDNA.Error.LABEL_HAS_DOT);
                labelString2.setCharAt(i, 65533);
            } else if (disallowNonLDHDot && asciiData[c] < 0) {
                addLabelError(info2, IDNA.Error.DISALLOWED);
                labelString2.setCharAt(i, 65533);
            }
            oredChars = oredChars2;
            int i2 = i + 1;
            if (i2 >= limit) {
                break;
            }
            i = i2;
            oredChars2 = oredChars;
        }
        int c2 = labelString2.codePointAt(labelStart2);
        if ((U_GET_GC_MASK(c2) & U_GC_M_MASK) != 0) {
            addLabelError(info2, IDNA.Error.LEADING_COMBINING_MARK);
            labelString2.setCharAt(labelStart2, 65533);
            if (c2 > 65535) {
                labelString2.deleteCharAt(labelStart2 + 1);
                labelLength3--;
                if (labelString2 == sb) {
                    destLabelLength2--;
                }
            }
        }
        if (!hasCertainLabelErrors(info2, severeErrors)) {
            if ((this.options & 4) != 0 && (!isBiDi(info) || isOkBiDi(info))) {
                checkLabelBiDi(labelString2, labelStart2, labelLength3, info2);
            }
            if ((this.options & 8) != 0 && (oredChars & 8204) == 8204 && !isLabelOkContextJ(labelString2, labelStart2, labelLength3)) {
                addLabelError(info2, IDNA.Error.CONTEXTJ);
            }
            if ((this.options & 64) != 0 && oredChars >= 183) {
                checkLabelContextO(labelString2, labelStart2, labelLength3, info2);
            }
            if (toASCII) {
                if (fromPunycode != null) {
                    if (destLabelLength2 > 63) {
                        addLabelError(info2, IDNA.Error.LABEL_TOO_LONG);
                    }
                    return destLabelLength2;
                } else if (oredChars >= 128) {
                    try {
                        StringBuilder punycode = Punycode.encode(labelString2.subSequence(labelStart2, labelStart2 + labelLength3), null);
                        punycode.insert(0, "xn--");
                        if (punycode.length() > 63) {
                            addLabelError(info2, IDNA.Error.LABEL_TOO_LONG);
                        }
                        return replaceLabel(sb, destLabelStart, destLabelLength2, punycode, punycode.length());
                    } catch (StringPrepParseException e2) {
                        throw new ICUException((Throwable) e2);
                    }
                } else if (labelLength3 > 63) {
                    addLabelError(info2, IDNA.Error.LABEL_TOO_LONG);
                }
            }
            labelLength2 = labelLength3;
            destLabelLength = destLabelLength2;
        } else if (fromPunycode != null) {
            addLabelError(info2, IDNA.Error.INVALID_ACE_LABEL);
            int i3 = labelLength3;
            int i4 = c2;
            char c3 = oredChars;
            return markBadACELabel(sb, destLabelStart, destLabelLength2, toASCII, info2);
        } else {
            labelLength2 = labelLength3;
            destLabelLength = destLabelLength2;
            int i5 = c2;
            char c4 = oredChars;
        }
        return replaceLabel(sb, destLabelStart, destLabelLength, labelString2, labelLength2);
    }

    private int markBadACELabel(StringBuilder dest, int labelStart, int labelLength, boolean toASCII, IDNA.Info info) {
        boolean disallowNonLDHDot = (this.options & 2) != 0;
        boolean isASCII = true;
        boolean onlyLDH = true;
        int i = labelStart + 4;
        int limit = labelStart + labelLength;
        do {
            char c = dest.charAt(i);
            if (c > 127) {
                onlyLDH = false;
                isASCII = false;
            } else if (c == '.') {
                addLabelError(info, IDNA.Error.LABEL_HAS_DOT);
                dest.setCharAt(i, 65533);
                onlyLDH = false;
                isASCII = false;
            } else if (asciiData[c] < 0) {
                onlyLDH = false;
                if (disallowNonLDHDot) {
                    dest.setCharAt(i, 65533);
                    isASCII = false;
                }
            }
            i++;
        } while (i < limit);
        if (onlyLDH) {
            dest.insert(labelStart + labelLength, 65533);
            return labelLength + 1;
        } else if (!toASCII || !isASCII || labelLength <= 63) {
            return labelLength;
        } else {
            addLabelError(info, IDNA.Error.LABEL_TOO_LONG);
            return labelLength;
        }
    }

    private void checkLabelBiDi(CharSequence label, int labelStart, int labelLength, IDNA.Info info) {
        int dir;
        int i = labelStart;
        int c = Character.codePointAt(label, i);
        int i2 = i + Character.charCount(c);
        int firstMask = U_MASK(UBiDiProps.INSTANCE.getClass(c));
        if (((~L_R_AL_MASK) & firstMask) != 0) {
            setNotOkBiDi(info);
        }
        int labelLimit = labelStart + labelLength;
        while (true) {
            if (i2 < labelLimit) {
                int c2 = Character.codePointBefore(label, labelLimit);
                labelLimit -= Character.charCount(c2);
                int dir2 = UBiDiProps.INSTANCE.getClass(c2);
                if (dir2 != 17) {
                    dir = U_MASK(dir2);
                    break;
                }
            } else {
                dir = firstMask;
                break;
            }
        }
        if ((L_MASK & firstMask) == 0 ? ((~R_AL_EN_AN_MASK) & dir) != 0 : ((~L_EN_MASK) & dir) != 0) {
            setNotOkBiDi(info);
        }
        int mask = firstMask | dir;
        while (i2 < labelLimit) {
            int c3 = Character.codePointAt(label, i2);
            i2 += Character.charCount(c3);
            mask |= U_MASK(UBiDiProps.INSTANCE.getClass(c3));
        }
        if ((L_MASK & firstMask) == 0) {
            if (((~R_AL_AN_EN_ES_CS_ET_ON_BN_NSM_MASK) & mask) != 0) {
                setNotOkBiDi(info);
            }
            if ((EN_AN_MASK & mask) == EN_AN_MASK) {
                setNotOkBiDi(info);
            }
        } else if (((~L_EN_ES_CS_ET_ON_BN_NSM_MASK) & mask) != 0) {
            setNotOkBiDi(info);
        }
        if ((R_AL_AN_MASK & mask) != 0) {
            setBiDi(info);
        }
    }

    private static boolean isASCIIOkBiDi(CharSequence s, int length) {
        int labelStart = 0;
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            if (c == '.') {
                if (i > labelStart) {
                    char c2 = s.charAt(i - 1);
                    if (('a' > c2 || c2 > 'z') && ('0' > c2 || c2 > '9')) {
                        return false;
                    }
                }
                labelStart = i + 1;
            } else if (i == labelStart) {
                if ('a' > c || c > 'z') {
                    return false;
                }
            } else if (c <= ' ' && (c >= 28 || (9 <= c && c <= 13))) {
                return false;
            }
        }
        return true;
    }

    private boolean isLabelOkContextJ(CharSequence label, int labelStart, int labelLength) {
        int labelLimit = labelStart + labelLength;
        for (int i = labelStart; i < labelLimit; i++) {
            if (label.charAt(i) == 8204) {
                if (i == labelStart) {
                    return false;
                }
                int j = i;
                int c = Character.codePointBefore(label, j);
                int j2 = j - Character.charCount(c);
                if (uts46Norm2.getCombiningClass(c) == 9) {
                    continue;
                } else {
                    while (true) {
                        int type = UBiDiProps.INSTANCE.getJoiningType(c);
                        if (type == 5) {
                            if (j2 == 0) {
                                return false;
                            }
                            c = Character.codePointBefore(label, j2);
                            j2 -= Character.charCount(c);
                        } else if (type != 3 && type != 2) {
                            return false;
                        } else {
                            int j3 = i + 1;
                            while (j3 != labelLimit) {
                                int c2 = Character.codePointAt(label, j3);
                                j3 += Character.charCount(c2);
                                int type2 = UBiDiProps.INSTANCE.getJoiningType(c2);
                                if (type2 != 5) {
                                    if (!(type2 == 4 || type2 == 2)) {
                                        return false;
                                    }
                                }
                            }
                            return false;
                        }
                    }
                }
            } else if (label.charAt(i) != 8205) {
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

    private void checkLabelContextO(CharSequence label, int labelStart, int labelLength, IDNA.Info info) {
        int labelEnd = (labelStart + labelLength) - 1;
        int arabicDigits = 0;
        for (int i = labelStart; i <= labelEnd; i++) {
            char charAt = label.charAt(i);
            if (charAt >= 183) {
                if (charAt <= 1785) {
                    if (charAt == 183) {
                        if (labelStart >= i || label.charAt(i - 1) != 'l' || i >= labelEnd || label.charAt(i + 1) != 'l') {
                            addLabelError(info, IDNA.Error.CONTEXTO_PUNCTUATION);
                        }
                    } else if (charAt == 885) {
                        if (i >= labelEnd || 14 != UScript.getScript(Character.codePointAt(label, i + 1))) {
                            addLabelError(info, IDNA.Error.CONTEXTO_PUNCTUATION);
                        }
                    } else if (charAt == 1523 || charAt == 1524) {
                        if (labelStart >= i || 19 != UScript.getScript(Character.codePointBefore(label, i))) {
                            addLabelError(info, IDNA.Error.CONTEXTO_PUNCTUATION);
                        }
                    } else if (1632 <= charAt) {
                        if (charAt <= 1641) {
                            if (arabicDigits > 0) {
                                addLabelError(info, IDNA.Error.CONTEXTO_DIGITS);
                            }
                            arabicDigits = -1;
                        } else if (1776 <= charAt) {
                            if (arabicDigits < 0) {
                                addLabelError(info, IDNA.Error.CONTEXTO_DIGITS);
                            }
                            arabicDigits = 1;
                        }
                    }
                } else if (charAt == 12539) {
                    char c = charAt;
                    int j = labelStart;
                    while (true) {
                        if (j <= labelEnd) {
                            int c2 = Character.codePointAt(label, j);
                            int script = UScript.getScript(c2);
                            if (script == 20 || script == 22 || script == 17) {
                                break;
                            }
                            j += Character.charCount(c2);
                        } else {
                            addLabelError(info, IDNA.Error.CONTEXTO_PUNCTUATION);
                            break;
                        }
                    }
                }
            }
        }
    }

    private static int U_MASK(int x) {
        return 1 << x;
    }

    private static int U_GET_GC_MASK(int c) {
        return 1 << UCharacter.getType(c);
    }
}
