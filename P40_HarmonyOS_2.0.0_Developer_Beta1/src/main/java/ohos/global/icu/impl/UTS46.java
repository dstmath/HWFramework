package ohos.global.icu.impl;

import java.io.InputStream;
import java.util.EnumSet;
import ohos.global.icu.impl.Normalizer2Impl;
import ohos.global.icu.lang.UCharacter;
import ohos.global.icu.lang.UScript;
import ohos.global.icu.text.IDNA;
import ohos.global.icu.text.Normalizer2;
import ohos.global.icu.text.StringPrepParseException;
import ohos.global.icu.util.ICUException;

public final class UTS46 extends IDNA {
    private static final int EN_AN_MASK = (U_MASK(5) | U_MASK(2));
    private static final int ES_CS_ET_ON_BN_NSM_MASK = (((((U_MASK(3) | U_MASK(6)) | U_MASK(4)) | U_MASK(10)) | U_MASK(18)) | U_MASK(17));
    private static final int L_EN_ES_CS_ET_ON_BN_NSM_MASK;
    private static final int L_EN_MASK = (L_MASK | U_MASK(2));
    private static final int L_MASK = U_MASK(0);
    private static final int L_R_AL_MASK;
    private static final int R_AL_AN_EN_ES_CS_ET_ON_BN_NSM_MASK;
    private static final int R_AL_AN_MASK;
    private static final int R_AL_EN_AN_MASK = (R_AL_MASK | EN_AN_MASK);
    private static final int R_AL_MASK = (U_MASK(1) | U_MASK(13));
    private static int U_GC_M_MASK = ((U_MASK(6) | U_MASK(7)) | U_MASK(8));
    private static final byte[] asciiData = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1};
    private static final EnumSet<IDNA.Error> severeErrors = EnumSet.of(IDNA.Error.LEADING_COMBINING_MARK, IDNA.Error.DISALLOWED, IDNA.Error.PUNYCODE, IDNA.Error.LABEL_HAS_DOT, IDNA.Error.INVALID_ACE_LABEL);
    private static final Normalizer2 uts46Norm2 = Normalizer2.getInstance((InputStream) null, "uts46", Normalizer2.Mode.COMPOSE);
    final int options;

    private static int U_MASK(int i) {
        return 1 << i;
    }

    private static boolean isNonASCIIDisallowedSTD3Valid(int i) {
        return i == 8800 || i == 8814 || i == 8815;
    }

    public UTS46(int i) {
        this.options = i;
    }

    public StringBuilder labelToASCII(CharSequence charSequence, StringBuilder sb, IDNA.Info info) {
        return process(charSequence, true, true, sb, info);
    }

    public StringBuilder labelToUnicode(CharSequence charSequence, StringBuilder sb, IDNA.Info info) {
        return process(charSequence, true, false, sb, info);
    }

    public StringBuilder nameToASCII(CharSequence charSequence, StringBuilder sb, IDNA.Info info) {
        process(charSequence, false, true, sb, info);
        if (sb.length() >= 254 && !info.getErrors().contains(IDNA.Error.DOMAIN_NAME_TOO_LONG) && isASCIIString(sb) && (sb.length() > 254 || sb.charAt(253) != '.')) {
            addError(info, IDNA.Error.DOMAIN_NAME_TOO_LONG);
        }
        return sb;
    }

    public StringBuilder nameToUnicode(CharSequence charSequence, StringBuilder sb, IDNA.Info info) {
        return process(charSequence, false, false, sb, info);
    }

    static {
        int i = L_MASK;
        int i2 = R_AL_MASK;
        L_R_AL_MASK = i | i2;
        R_AL_AN_MASK = i2 | U_MASK(5);
        int i3 = L_EN_MASK;
        int i4 = ES_CS_ET_ON_BN_NSM_MASK;
        L_EN_ES_CS_ET_ON_BN_NSM_MASK = i3 | i4;
        R_AL_AN_EN_ES_CS_ET_ON_BN_NSM_MASK = R_AL_MASK | EN_AN_MASK | i4;
    }

    private static boolean isASCIIString(CharSequence charSequence) {
        int length = charSequence.length();
        for (int i = 0; i < length; i++) {
            if (charSequence.charAt(i) > 127) {
                return false;
            }
        }
        return true;
    }

    private StringBuilder process(CharSequence charSequence, boolean z, boolean z2, StringBuilder sb, IDNA.Info info) {
        if (sb != charSequence) {
            int i = 0;
            sb.delete(0, Integer.MAX_VALUE);
            resetInfo(info);
            int length = charSequence.length();
            if (length == 0) {
                addError(info, IDNA.Error.EMPTY_LABEL);
                return sb;
            }
            boolean z3 = (this.options & 2) != 0;
            int i2 = 0;
            while (i != length) {
                char charAt = charSequence.charAt(i);
                if (charAt <= 127) {
                    byte b = asciiData[charAt];
                    if (b > 0) {
                        sb.append((char) (charAt + ' '));
                    } else if (b >= 0 || !z3) {
                        sb.append(charAt);
                        if (charAt == '-') {
                            if (i != i2 + 3 || charSequence.charAt(i - 1) != '-') {
                                if (i == i2) {
                                    addLabelError(info, IDNA.Error.LEADING_HYPHEN);
                                }
                                int i3 = i + 1;
                                if (i3 == length || charSequence.charAt(i3) == '.') {
                                    addLabelError(info, IDNA.Error.TRAILING_HYPHEN);
                                }
                            }
                        } else if (charAt != '.') {
                            continue;
                        } else if (!z) {
                            if (i == i2) {
                                addLabelError(info, IDNA.Error.EMPTY_LABEL);
                            }
                            if (z2 && i - i2 > 63) {
                                addLabelError(info, IDNA.Error.LABEL_TOO_LONG);
                            }
                            promoteAndResetLabelErrors(info);
                            i2 = i + 1;
                        }
                        i++;
                    }
                    i++;
                }
                promoteAndResetLabelErrors(info);
                processUnicode(charSequence, i2, i, z, z2, sb, info);
                if (isBiDi(info) && !hasCertainErrors(info, severeErrors) && (!isOkBiDi(info) || (i2 > 0 && !isASCIIOkBiDi(sb, i2)))) {
                    addError(info, IDNA.Error.BIDI);
                }
                return sb;
            }
            if (z2) {
                if (i - i2 > 63) {
                    addLabelError(info, IDNA.Error.LABEL_TOO_LONG);
                }
                if (!z && i >= 254 && (i > 254 || i2 < i)) {
                    addError(info, IDNA.Error.DOMAIN_NAME_TOO_LONG);
                }
            }
            promoteAndResetLabelErrors(info);
            return sb;
        }
        throw new IllegalArgumentException();
    }

    private StringBuilder processUnicode(CharSequence charSequence, int i, int i2, boolean z, boolean z2, StringBuilder sb, IDNA.Info info) {
        int i3;
        int i4;
        if (i2 == 0) {
            uts46Norm2.normalize(charSequence, sb);
        } else {
            uts46Norm2.normalizeSecondAndAppend(sb, charSequence.subSequence(i2, charSequence.length()));
        }
        int i5 = i;
        boolean z3 = !z2 ? (this.options & 32) == 0 : (this.options & 16) == 0;
        int length = sb.length();
        loop0:
        while (true) {
            i3 = i5;
            while (i3 < length) {
                char charAt = sb.charAt(i3);
                if (charAt != '.' || z) {
                    if (charAt >= 223) {
                        if (charAt <= 8205 && (charAt == 223 || charAt == 962 || charAt >= 8204)) {
                            setTransitionalDifferent(info);
                            if (z3) {
                                length = mapDevChars(sb, i5, i3);
                                z3 = false;
                            }
                        } else if (Character.isSurrogate(charAt) && (!Normalizer2Impl.UTF16Plus.isSurrogateLead(charAt) ? i3 == i5 || !Character.isHighSurrogate(sb.charAt(i3 - 1)) : (i4 = i3 + 1) == length || !Character.isLowSurrogate(sb.charAt(i4)))) {
                            addLabelError(info, IDNA.Error.DISALLOWED);
                            sb.setCharAt(i3, 65533);
                            i3++;
                        }
                    }
                    i3++;
                } else {
                    int i6 = i3 - i5;
                    int processLabel = processLabel(sb, i5, i6, z2, info);
                    promoteAndResetLabelErrors(info);
                    length += processLabel - i6;
                    i5 += processLabel + 1;
                }
            }
            break loop0;
        }
        if (i5 == 0 || i5 < i3) {
            processLabel(sb, i5, i3 - i5, z2, info);
            promoteAndResetLabelErrors(info);
        }
        return sb;
    }

    private int mapDevChars(StringBuilder sb, int i, int i2) {
        int length = sb.length();
        boolean z = false;
        while (i2 < length) {
            char charAt = sb.charAt(i2);
            if (charAt == 223) {
                int i3 = i2 + 1;
                sb.setCharAt(i2, 's');
                i2 = i3 + 1;
                sb.insert(i3, 's');
                length++;
            } else if (charAt == 962) {
                sb.setCharAt(i2, 963);
                i2++;
            } else if (charAt == 8204 || charAt == 8205) {
                sb.delete(i2, i2 + 1);
                length--;
            } else {
                i2++;
            }
            z = true;
        }
        if (!z) {
            return length;
        }
        sb.replace(i, Integer.MAX_VALUE, uts46Norm2.normalize(sb.subSequence(i, sb.length())));
        return sb.length();
    }

    private static int replaceLabel(StringBuilder sb, int i, int i2, CharSequence charSequence, int i3) {
        if (charSequence != sb) {
            sb.delete(i, i2 + i).insert(i, charSequence);
        }
        return i3;
    }

    private int processLabel(StringBuilder sb, int i, int i2, boolean z, IDNA.Info info) {
        boolean z2;
        int i3;
        int i4;
        StringBuilder sb2;
        int i5 = i2;
        boolean z3 = true;
        if (i5 >= 4 && sb.charAt(i) == 'x' && sb.charAt(i + 1) == 'n' && sb.charAt(i + 2) == '-' && sb.charAt(i + 3) == '-') {
            try {
                sb2 = Punycode.decode(sb.subSequence(i + 4, i + i5), null);
                if (!uts46Norm2.isNormalized(sb2)) {
                    addLabelError(info, IDNA.Error.INVALID_ACE_LABEL);
                    return markBadACELabel(sb, i, i2, z, info);
                }
                i4 = sb2.length();
                z2 = true;
                i3 = 0;
            } catch (StringPrepParseException unused) {
                addLabelError(info, IDNA.Error.PUNYCODE);
                return markBadACELabel(sb, i, i2, z, info);
            }
        } else {
            i4 = i5;
            sb2 = sb;
            i3 = i;
            z2 = false;
        }
        if (i4 == 0) {
            addLabelError(info, IDNA.Error.EMPTY_LABEL);
            return replaceLabel(sb, i, i5, sb2, i4);
        }
        if (i4 >= 4 && sb2.charAt(i3 + 2) == '-' && sb2.charAt(i3 + 3) == '-') {
            addLabelError(info, IDNA.Error.HYPHEN_3_4);
        }
        if (sb2.charAt(i3) == '-') {
            addLabelError(info, IDNA.Error.LEADING_HYPHEN);
        }
        int i6 = i3 + i4;
        if (sb2.charAt(i6 - 1) == '-') {
            addLabelError(info, IDNA.Error.TRAILING_HYPHEN);
        }
        if ((this.options & 2) == 0) {
            z3 = false;
        }
        int i7 = i3;
        char c = 0;
        while (true) {
            char charAt = sb2.charAt(i7);
            if (charAt > 127) {
                char c2 = (char) (c | charAt);
                if (z3 && isNonASCIIDisallowedSTD3Valid(charAt)) {
                    addLabelError(info, IDNA.Error.DISALLOWED);
                    sb2.setCharAt(i7, 65533);
                } else if (charAt == 65533) {
                    addLabelError(info, IDNA.Error.DISALLOWED);
                }
                c = c2;
            } else if (charAt == '.') {
                addLabelError(info, IDNA.Error.LABEL_HAS_DOT);
                sb2.setCharAt(i7, 65533);
            } else if (z3 && asciiData[charAt] < 0) {
                addLabelError(info, IDNA.Error.DISALLOWED);
                sb2.setCharAt(i7, 65533);
            }
            i7++;
            if (i7 >= i6) {
                break;
            }
        }
        int codePointAt = sb2.codePointAt(i3);
        if ((U_GET_GC_MASK(codePointAt) & U_GC_M_MASK) != 0) {
            addLabelError(info, IDNA.Error.LEADING_COMBINING_MARK);
            sb2.setCharAt(i3, 65533);
            if (codePointAt > 65535) {
                sb2.deleteCharAt(i3 + 1);
                i4--;
                if (sb2 == sb) {
                    i5--;
                }
            }
        }
        if (!hasCertainLabelErrors(info, severeErrors)) {
            if ((this.options & 4) != 0 && (!isBiDi(info) || isOkBiDi(info))) {
                checkLabelBiDi(sb2, i3, i4, info);
            }
            if ((this.options & 8) != 0 && (c & 8204) == 8204 && !isLabelOkContextJ(sb2, i3, i4)) {
                addLabelError(info, IDNA.Error.CONTEXTJ);
            }
            if ((this.options & 64) != 0 && c >= 183) {
                checkLabelContextO(sb2, i3, i4, info);
            }
            if (z) {
                if (z2) {
                    if (i5 > 63) {
                        addLabelError(info, IDNA.Error.LABEL_TOO_LONG);
                    }
                    return i5;
                } else if (c >= 128) {
                    try {
                        StringBuilder encode = Punycode.encode(sb2.subSequence(i3, i4 + i3), null);
                        encode.insert(0, "xn--");
                        if (encode.length() > 63) {
                            addLabelError(info, IDNA.Error.LABEL_TOO_LONG);
                        }
                        return replaceLabel(sb, i, i5, encode, encode.length());
                    } catch (StringPrepParseException e) {
                        throw new ICUException(e);
                    }
                } else if (i4 > 63) {
                    addLabelError(info, IDNA.Error.LABEL_TOO_LONG);
                }
            }
        } else if (z2) {
            addLabelError(info, IDNA.Error.INVALID_ACE_LABEL);
            return markBadACELabel(sb, i, i5, z, info);
        }
        return replaceLabel(sb, i, i5, sb2, i4);
    }

    private int markBadACELabel(StringBuilder sb, int i, int i2, boolean z, IDNA.Info info) {
        boolean z2 = true;
        boolean z3 = (this.options & 2) != 0;
        int i3 = i + 4;
        int i4 = i + i2;
        boolean z4 = true;
        do {
            char charAt = sb.charAt(i3);
            if (charAt <= 127) {
                if (charAt == '.') {
                    addLabelError(info, IDNA.Error.LABEL_HAS_DOT);
                    sb.setCharAt(i3, 65533);
                } else {
                    if (asciiData[charAt] < 0) {
                        if (z3) {
                            sb.setCharAt(i3, 65533);
                        } else {
                            z2 = false;
                        }
                    }
                    i3++;
                }
            }
            z2 = false;
            z4 = false;
            i3++;
        } while (i3 < i4);
        if (z2) {
            sb.insert(i4, (char) 65533);
            return i2 + 1;
        } else if (!z || !z4 || i2 <= 63) {
            return i2;
        } else {
            addLabelError(info, IDNA.Error.LABEL_TOO_LONG);
            return i2;
        }
    }

    private void checkLabelBiDi(CharSequence charSequence, int i, int i2, IDNA.Info info) {
        int i3;
        int codePointAt = Character.codePointAt(charSequence, i);
        int charCount = Character.charCount(codePointAt) + i;
        int U_MASK = U_MASK(UBiDiProps.INSTANCE.getClass(codePointAt));
        if (((~L_R_AL_MASK) & U_MASK) != 0) {
            setNotOkBiDi(info);
        }
        int i4 = i + i2;
        while (true) {
            if (charCount < i4) {
                int codePointBefore = Character.codePointBefore(charSequence, i4);
                i4 -= Character.charCount(codePointBefore);
                int i5 = UBiDiProps.INSTANCE.getClass(codePointBefore);
                if (i5 != 17) {
                    i3 = U_MASK(i5);
                    break;
                }
            } else {
                i3 = U_MASK;
                break;
            }
        }
        if ((L_MASK & U_MASK) == 0 ? ((~R_AL_EN_AN_MASK) & i3) != 0 : ((~L_EN_MASK) & i3) != 0) {
            setNotOkBiDi(info);
        }
        int i6 = i3 | U_MASK;
        while (charCount < i4) {
            int codePointAt2 = Character.codePointAt(charSequence, charCount);
            charCount += Character.charCount(codePointAt2);
            i6 |= U_MASK(UBiDiProps.INSTANCE.getClass(codePointAt2));
        }
        if ((U_MASK & L_MASK) == 0) {
            if (((~R_AL_AN_EN_ES_CS_ET_ON_BN_NSM_MASK) & i6) != 0) {
                setNotOkBiDi(info);
            }
            int i7 = EN_AN_MASK;
            if ((i6 & i7) == i7) {
                setNotOkBiDi(info);
            }
        } else if (((~L_EN_ES_CS_ET_ON_BN_NSM_MASK) & i6) != 0) {
            setNotOkBiDi(info);
        }
        if ((R_AL_AN_MASK & i6) != 0) {
            setBiDi(info);
        }
    }

    private static boolean isASCIIOkBiDi(CharSequence charSequence, int i) {
        char charAt;
        int i2 = 0;
        for (int i3 = 0; i3 < i; i3++) {
            char charAt2 = charSequence.charAt(i3);
            if (charAt2 == '.') {
                if (i3 > i2 && (('a' > (charAt = charSequence.charAt(i3 - 1)) || charAt > 'z') && ('0' > charAt || charAt > '9'))) {
                    return false;
                }
                i2 = i3 + 1;
            } else if (i3 == i2) {
                if ('a' > charAt2 || charAt2 > 'z') {
                    return false;
                }
            } else if (charAt2 <= ' ' && (charAt2 >= 28 || ('\t' <= charAt2 && charAt2 <= '\r'))) {
                return false;
            }
        }
        return true;
    }

    private boolean isLabelOkContextJ(CharSequence charSequence, int i, int i2) {
        int i3 = i2 + i;
        for (int i4 = i; i4 < i3; i4++) {
            if (charSequence.charAt(i4) == 8204) {
                if (i4 == i) {
                    return false;
                }
                int codePointBefore = Character.codePointBefore(charSequence, i4);
                int charCount = i4 - Character.charCount(codePointBefore);
                if (uts46Norm2.getCombiningClass(codePointBefore) == 9) {
                    continue;
                } else {
                    while (true) {
                        int joiningType = UBiDiProps.INSTANCE.getJoiningType(codePointBefore);
                        if (joiningType == 5) {
                            if (charCount == 0) {
                                return false;
                            }
                            codePointBefore = Character.codePointBefore(charSequence, charCount);
                            charCount -= Character.charCount(codePointBefore);
                        } else if (joiningType != 3 && joiningType != 2) {
                            return false;
                        } else {
                            int i5 = i4 + 1;
                            while (i5 != i3) {
                                int codePointAt = Character.codePointAt(charSequence, i5);
                                i5 += Character.charCount(codePointAt);
                                int joiningType2 = UBiDiProps.INSTANCE.getJoiningType(codePointAt);
                                if (joiningType2 != 5) {
                                    if (!(joiningType2 == 4 || joiningType2 == 2)) {
                                        return false;
                                    }
                                }
                            }
                            return false;
                        }
                    }
                }
            } else if (charSequence.charAt(i4) != 8205) {
                continue;
            } else if (i4 == i) {
                return false;
            } else {
                if (uts46Norm2.getCombiningClass(Character.codePointBefore(charSequence, i4)) != 9) {
                    return false;
                }
            }
        }
        return true;
    }

    private void checkLabelContextO(CharSequence charSequence, int i, int i2, IDNA.Info info) {
        int i3 = (i2 + i) - 1;
        char c = 0;
        for (int i4 = i; i4 <= i3; i4++) {
            char charAt = charSequence.charAt(i4);
            if (charAt >= 183) {
                if (charAt <= 1785) {
                    if (charAt == 183) {
                        if (i >= i4 || charSequence.charAt(i4 - 1) != 'l' || i4 >= i3 || charSequence.charAt(i4 + 1) != 'l') {
                            addLabelError(info, IDNA.Error.CONTEXTO_PUNCTUATION);
                        }
                    } else if (charAt == 885) {
                        if (i4 >= i3 || 14 != UScript.getScript(Character.codePointAt(charSequence, i4 + 1))) {
                            addLabelError(info, IDNA.Error.CONTEXTO_PUNCTUATION);
                        }
                    } else if (charAt == 1523 || charAt == 1524) {
                        if (i >= i4 || 19 != UScript.getScript(Character.codePointBefore(charSequence, i4))) {
                            addLabelError(info, IDNA.Error.CONTEXTO_PUNCTUATION);
                        }
                    } else if (1632 <= charAt) {
                        if (charAt <= 1641) {
                            if (c > 0) {
                                addLabelError(info, IDNA.Error.CONTEXTO_DIGITS);
                            }
                            c = 65535;
                        } else if (1776 <= charAt) {
                            if (c < 0) {
                                addLabelError(info, IDNA.Error.CONTEXTO_DIGITS);
                            }
                            c = 1;
                        }
                    }
                } else if (charAt == 12539) {
                    int i5 = i;
                    while (true) {
                        if (i5 > i3) {
                            addLabelError(info, IDNA.Error.CONTEXTO_PUNCTUATION);
                            break;
                        }
                        int codePointAt = Character.codePointAt(charSequence, i5);
                        int script = UScript.getScript(codePointAt);
                        if (script == 20 || script == 22 || script == 17) {
                            break;
                        }
                        i5 += Character.charCount(codePointAt);
                    }
                }
            }
        }
    }

    private static int U_GET_GC_MASK(int i) {
        return 1 << UCharacter.getType(i);
    }
}
