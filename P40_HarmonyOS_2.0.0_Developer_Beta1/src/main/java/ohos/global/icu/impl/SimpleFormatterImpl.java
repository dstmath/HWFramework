package ohos.global.icu.impl;

public final class SimpleFormatterImpl {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int ARG_NUM_LIMIT = 256;
    private static final String[][] COMMON_PATTERNS = {new String[]{"{0} {1}", "\u0002\u0000ā \u0001"}, new String[]{"{0} ({1})", "\u0002\u0000Ă (\u0001ā)"}, new String[]{"{0}, {1}", "\u0002\u0000Ă, \u0001"}, new String[]{"{0} – {1}", "\u0002\u0000ă – \u0001"}};
    private static final char LEN1_CHAR = 257;
    private static final char LEN2_CHAR = 258;
    private static final char LEN3_CHAR = 259;
    private static final int MAX_SEGMENT_LENGTH = 65279;
    private static final char SEGMENT_LENGTH_ARGUMENT_CHAR = 65535;

    private SimpleFormatterImpl() {
    }

    public static String compileToStringMinMaxArguments(CharSequence charSequence, StringBuilder sb, int i, int i2) {
        int i3;
        int i4;
        int i5;
        int i6;
        char charAt;
        int i7 = 0;
        int i8 = 1;
        if (i <= 2 && 2 <= i2) {
            String[][] strArr = COMMON_PATTERNS;
            for (String[] strArr2 : strArr) {
                if (strArr2[0].contentEquals(charSequence)) {
                    return strArr2[1];
                }
            }
        }
        int length = charSequence.length();
        sb.ensureCapacity(length);
        sb.setLength(1);
        int i9 = 0;
        int i10 = 0;
        int i11 = 0;
        int i12 = -1;
        while (i9 < length) {
            int i13 = i9 + 1;
            char charAt2 = charSequence.charAt(i9);
            if (charAt2 == '\'') {
                if (i13 < length && (charAt2 = charSequence.charAt(i13)) == '\'') {
                    i13++;
                } else if (i11 != 0) {
                    i11 = i7;
                    i9 = i13;
                } else if (charAt2 == '{' || charAt2 == '}') {
                    i13++;
                    i11 = i8;
                } else {
                    charAt2 = '\'';
                }
            } else if (i11 == 0 && charAt2 == '{') {
                if (i10 > 0) {
                    sb.setCharAt((sb.length() - i10) - i8, (char) (i10 + 256));
                    i10 = i7;
                }
                int i14 = i13 + 1;
                if (i14 >= length || charSequence.charAt(i13) - '0' < 0 || i4 > 9 || charSequence.charAt(i14) != '}') {
                    int i15 = i13 - 1;
                    if (i13 < length) {
                        charAt2 = charSequence.charAt(i13);
                        if ('1' > charAt2 || charAt2 > '9') {
                            i13 = i14;
                        } else {
                            i13 = i14;
                            i5 = charAt2 - 48;
                            while (true) {
                                if (i13 >= length) {
                                    break;
                                }
                                i6 = i13 + 1;
                                charAt = charSequence.charAt(i13);
                                if ('0' > charAt || charAt > '9' || (i5 = (i5 * 10) + (charAt - '0')) >= 256) {
                                    break;
                                }
                                i13 = i6;
                                charAt2 = charAt;
                            }
                            i13 = i6;
                            charAt2 = charAt;
                            if (i5 >= 0 || charAt2 != '}') {
                                throw new IllegalArgumentException("Argument syntax error in pattern \"" + ((Object) charSequence) + "\" at index " + i15 + ": " + ((Object) charSequence.subSequence(i15, i13)));
                            }
                            i4 = i5;
                        }
                    }
                    i5 = -1;
                    if (i5 >= 0) {
                    }
                    throw new IllegalArgumentException("Argument syntax error in pattern \"" + ((Object) charSequence) + "\" at index " + i15 + ": " + ((Object) charSequence.subSequence(i15, i13)));
                }
                i13 += 2;
                i9 = i13;
                if (i4 > i12) {
                    i12 = i4;
                }
                sb.append((char) i4);
                i7 = 0;
                i8 = 1;
            }
            if (i10 == 0) {
                sb.append(SEGMENT_LENGTH_ARGUMENT_CHAR);
            }
            sb.append(charAt2);
            i10++;
            if (i10 == MAX_SEGMENT_LENGTH) {
                i10 = 0;
            }
            i9 = i13;
            i7 = 0;
            i8 = 1;
        }
        if (i10 > 0) {
            i3 = 1;
            sb.setCharAt((sb.length() - i10) - 1, (char) (i10 + 256));
        } else {
            i3 = 1;
        }
        int i16 = i12 + i3;
        if (i16 < i) {
            throw new IllegalArgumentException("Fewer than minimum " + i + " arguments in pattern \"" + ((Object) charSequence) + "\"");
        } else if (i16 <= i2) {
            sb.setCharAt(0, (char) i16);
            return sb.toString();
        } else {
            throw new IllegalArgumentException("More than maximum " + i2 + " arguments in pattern \"" + ((Object) charSequence) + "\"");
        }
    }

    public static int getArgumentLimit(String str) {
        return str.charAt(0);
    }

    public static String formatCompiledPattern(String str, CharSequence... charSequenceArr) {
        return formatAndAppend(str, new StringBuilder(), null, charSequenceArr).toString();
    }

    public static String formatRawPattern(String str, int i, int i2, CharSequence... charSequenceArr) {
        StringBuilder sb = new StringBuilder();
        String compileToStringMinMaxArguments = compileToStringMinMaxArguments(str, sb, i, i2);
        sb.setLength(0);
        return formatAndAppend(compileToStringMinMaxArguments, sb, null, charSequenceArr).toString();
    }

    public static StringBuilder formatAndAppend(String str, StringBuilder sb, int[] iArr, CharSequence... charSequenceArr) {
        if ((charSequenceArr != null ? charSequenceArr.length : 0) >= getArgumentLimit(str)) {
            return format(str, charSequenceArr, sb, null, true, iArr);
        }
        throw new IllegalArgumentException("Too few values.");
    }

    public static StringBuilder formatAndReplace(String str, StringBuilder sb, int[] iArr, CharSequence... charSequenceArr) {
        if ((charSequenceArr != null ? charSequenceArr.length : 0) >= getArgumentLimit(str)) {
            char c = SEGMENT_LENGTH_ARGUMENT_CHAR;
            String str2 = null;
            if (getArgumentLimit(str) > 0) {
                int i = 1;
                while (i < str.length()) {
                    int i2 = i + 1;
                    char charAt = str.charAt(i);
                    if (charAt >= 256) {
                        i2 += charAt - 256;
                    } else if (charSequenceArr[charAt] == sb) {
                        if (i2 == 2) {
                            c = charAt;
                        } else if (str2 == null) {
                            str2 = sb.toString();
                        }
                    }
                    i = i2;
                }
            }
            if (c < 0) {
                sb.setLength(0);
            }
            return format(str, charSequenceArr, sb, str2, false, iArr);
        }
        throw new IllegalArgumentException("Too few values.");
    }

    public static String getTextWithNoArguments(String str) {
        int i = 1;
        StringBuilder sb = new StringBuilder((str.length() - 1) - getArgumentLimit(str));
        while (i < str.length()) {
            int i2 = i + 1;
            int charAt = str.charAt(i) - 256;
            if (charAt > 0) {
                i = charAt + i2;
                sb.append((CharSequence) str, i2, i);
            } else {
                i = i2;
            }
        }
        return sb.toString();
    }

    public static class Int64Iterator {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        public static final long DONE = -1;

        public static int getArgIndex(long j) {
            return (int) j;
        }

        public static long step(CharSequence charSequence, long j, StringBuffer stringBuffer) {
            int i = ((int) (j >>> 32)) + 1;
            while (i < charSequence.length() && charSequence.charAt(i) > 256) {
                int charAt = ((charSequence.charAt(i) + i) + 1) - 256;
                stringBuffer.append(charSequence, i + 1, charAt);
                i = charAt;
            }
            if (i == charSequence.length()) {
                return -1;
            }
            return ((long) charSequence.charAt(i)) | (((long) i) << 32);
        }
    }

    private static StringBuilder format(String str, CharSequence[] charSequenceArr, StringBuilder sb, String str2, boolean z, int[] iArr) {
        int i;
        if (iArr == null) {
            i = 0;
        } else {
            i = iArr.length;
            for (int i2 = 0; i2 < i; i2++) {
                iArr[i2] = -1;
            }
        }
        int i3 = 1;
        while (i3 < str.length()) {
            int i4 = i3 + 1;
            char charAt = str.charAt(i3);
            if (charAt < 256) {
                CharSequence charSequence = charSequenceArr[charAt];
                if (charSequence != sb) {
                    if (charAt < i) {
                        iArr[charAt] = sb.length();
                    }
                    sb.append(charSequence);
                } else if (z) {
                    throw new IllegalArgumentException("Value must not be same object as result");
                } else if (i4 != 2) {
                    if (charAt < i) {
                        iArr[charAt] = sb.length();
                    }
                    sb.append(str2);
                } else if (charAt < i) {
                    iArr[charAt] = 0;
                }
                i3 = i4;
            } else {
                i3 = (charAt - 256) + i4;
                sb.append((CharSequence) str, i4, i3);
            }
        }
        return sb;
    }
}
