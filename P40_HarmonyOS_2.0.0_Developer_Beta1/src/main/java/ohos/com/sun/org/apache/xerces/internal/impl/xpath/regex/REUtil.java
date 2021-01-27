package ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex;

import java.text.CharacterIterator;
import ohos.global.icu.impl.Normalizer2Impl;
import ohos.global.icu.impl.PatternTokenizer;
import ohos.global.icu.impl.UCharacterProperty;
import ohos.global.icu.impl.locale.UnicodeLocaleExtension;

public final class REUtil {
    static final int CACHESIZE = 20;
    static final RegularExpression[] regexCache = new RegularExpression[20];

    static final int composeFromSurrogates(int i, int i2) {
        return ((((i - 55296) << 10) + 65536) + i2) - 56320;
    }

    static final int getOptionValue(int i) {
        if (i == 44) {
            return 1024;
        }
        if (i == 70) {
            return 256;
        }
        if (i == 72) {
            return 128;
        }
        if (i == 88) {
            return 512;
        }
        if (i == 105) {
            return 2;
        }
        if (i == 109) {
            return 8;
        }
        if (i == 115) {
            return 4;
        }
        if (i == 117) {
            return 32;
        }
        if (i != 119) {
            return i != 120 ? 0 : 16;
        }
        return 64;
    }

    static final boolean isHighSurrogate(int i) {
        return (i & Normalizer2Impl.MIN_NORMAL_MAYBE_YES) == 55296;
    }

    static final boolean isLowSurrogate(int i) {
        return (i & Normalizer2Impl.MIN_NORMAL_MAYBE_YES) == 56320;
    }

    private REUtil() {
    }

    static final String decomposeToSurrogates(int i) {
        int i2 = i - 65536;
        return new String(new char[]{(char) ((i2 >> 10) + 55296), (char) ((i2 & UCharacterProperty.MAX_SCRIPT) + 56320)});
    }

    static final String substring(CharacterIterator characterIterator, int i, int i2) {
        char[] cArr = new char[(i2 - i)];
        for (int i3 = 0; i3 < cArr.length; i3++) {
            cArr[i3] = characterIterator.setIndex(i3 + i);
        }
        return new String(cArr);
    }

    static final int parseOptions(String str) throws ParseException {
        if (str == null) {
            return 0;
        }
        int i = 0;
        for (int i2 = 0; i2 < str.length(); i2++) {
            int optionValue = getOptionValue(str.charAt(i2));
            if (optionValue != 0) {
                i |= optionValue;
            } else {
                throw new ParseException("Unknown Option: " + str.substring(i2), -1);
            }
        }
        return i;
    }

    static final String createOptionString(int i) {
        StringBuffer stringBuffer = new StringBuffer(9);
        if ((i & 256) != 0) {
            stringBuffer.append('F');
        }
        if ((i & 128) != 0) {
            stringBuffer.append('H');
        }
        if ((i & 512) != 0) {
            stringBuffer.append('X');
        }
        if ((i & 2) != 0) {
            stringBuffer.append(UCharacterProperty.LATIN_SMALL_LETTER_I_);
        }
        if ((i & 8) != 0) {
            stringBuffer.append('m');
        }
        if ((i & 4) != 0) {
            stringBuffer.append('s');
        }
        if ((i & 32) != 0) {
            stringBuffer.append(UnicodeLocaleExtension.SINGLETON);
        }
        if ((i & 64) != 0) {
            stringBuffer.append('w');
        }
        if ((i & 16) != 0) {
            stringBuffer.append('x');
        }
        if ((i & 1024) != 0) {
            stringBuffer.append(',');
        }
        return stringBuffer.toString().intern();
    }

    static String stripExtendedComment(String str) {
        int length = str.length();
        StringBuffer stringBuffer = new StringBuffer(length);
        int i = 0;
        while (i < length) {
            int i2 = i + 1;
            char charAt = str.charAt(i);
            if (charAt != '\t' && charAt != '\n' && charAt != '\f' && charAt != '\r' && charAt != ' ') {
                if (charAt == '#') {
                    while (true) {
                        i = i2;
                        if (i >= length) {
                            break;
                        }
                        i2 = i + 1;
                        char charAt2 = str.charAt(i);
                        if (charAt2 != '\r') {
                            if (charAt2 == '\n') {
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                } else if (charAt != '\\' || i2 >= length) {
                    stringBuffer.append((char) charAt);
                } else {
                    char charAt3 = str.charAt(i2);
                    if (charAt3 == '#' || charAt3 == '\t' || charAt3 == '\n' || charAt3 == '\f' || charAt3 == '\r' || charAt3 == ' ') {
                        stringBuffer.append((char) charAt3);
                    } else {
                        stringBuffer.append(PatternTokenizer.BACK_SLASH);
                        stringBuffer.append((char) charAt3);
                    }
                    i2++;
                }
            }
            i = i2;
        }
        return stringBuffer.toString();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:50:0x01bb, code lost:
        r10 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x01bd, code lost:
        r10 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x01be, code lost:
        r10.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x01c6, code lost:
        r10.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x01ca, code lost:
        java.lang.System.err.println("com.sun.org.apache.xerces.internal.utils.regex.ParseException: " + r10.getMessage());
        java.lang.System.err.println("        " + r4);
        r10 = r10.getLocation();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x01fe, code lost:
        if (r10 >= 0) goto L_0x0200;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x0200, code lost:
        java.lang.System.err.print("        ");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x0205, code lost:
        if (r3 < r10) goto L_0x0207;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x0207, code lost:
        java.lang.System.err.print(ohos.global.icu.impl.locale.LanguageTag.SEP);
        r3 = r3 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x0211, code lost:
        java.lang.System.err.println("^");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:?, code lost:
        return;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x01bd A[ExcHandler: Exception (r10v5 'e' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:1:0x0008] */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x01c6  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x01ca  */
    public static void main(String[] strArr) {
        String str;
        int i = 0;
        try {
            if (strArr.length == 0) {
                System.out.println("Error:Usage: java REUtil -i|-m|-s|-u|-w|-X regularExpression String");
                System.exit(0);
            }
            String str2 = null;
            String str3 = "";
            str = null;
            for (int i2 = 0; i2 < strArr.length; i2++) {
                if (strArr[i2].length() != 0) {
                    if (strArr[i2].charAt(0) == '-') {
                        if (strArr[i2].equals("-i")) {
                            str3 = str3 + "i";
                        } else if (strArr[i2].equals("-m")) {
                            str3 = str3 + "m";
                        } else if (strArr[i2].equals("-s")) {
                            str3 = str3 + "s";
                        } else if (strArr[i2].equals("-u")) {
                            str3 = str3 + "u";
                        } else if (strArr[i2].equals("-w")) {
                            str3 = str3 + "w";
                        } else if (strArr[i2].equals("-X")) {
                            str3 = str3 + "X";
                        } else {
                            System.err.println("Unknown option: " + strArr[i2]);
                        }
                    }
                }
                if (str == null) {
                    str = strArr[i2];
                } else if (str2 == null) {
                    str2 = strArr[i2];
                } else {
                    System.err.println("Unnecessary: " + strArr[i2]);
                }
            }
            RegularExpression regularExpression = new RegularExpression(str, str3);
            System.out.println("RegularExpression: " + regularExpression);
            Match match = new Match();
            regularExpression.matches(str2, match);
            for (int i3 = 0; i3 < match.getNumberOfGroups(); i3++) {
                if (i3 == 0) {
                    System.out.print("Matched range for the whole pattern: ");
                } else {
                    System.out.print("[" + i3 + "]: ");
                }
                if (match.getBeginning(i3) < 0) {
                    System.out.println("-1");
                } else {
                    System.out.print(match.getBeginning(i3) + ", " + match.getEnd(i3) + ", ");
                    System.out.println("\"" + match.getCapturedText(i3) + "\"");
                }
            }
        } catch (ParseException e) {
            ParseException e2 = e;
            str = null;
            if (str != null) {
            }
        } catch (Exception e3) {
        }
    }

    public static RegularExpression createRegex(String str, String str2) throws ParseException {
        RegularExpression regularExpression;
        int parseOptions = parseOptions(str2);
        synchronized (regexCache) {
            int i = 0;
            while (true) {
                regularExpression = null;
                if (i >= 20) {
                    break;
                }
                try {
                    RegularExpression regularExpression2 = regexCache[i];
                    if (regularExpression2 == null) {
                        i = -1;
                        break;
                    } else if (regularExpression2.equals(str, parseOptions)) {
                        regularExpression = regularExpression2;
                        break;
                    } else {
                        i++;
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
            if (regularExpression == null) {
                regularExpression = new RegularExpression(str, str2);
                System.arraycopy(regexCache, 0, regexCache, 1, 19);
                regexCache[0] = regularExpression;
            } else if (i != 0) {
                System.arraycopy(regexCache, 0, regexCache, 1, i);
                regexCache[0] = regularExpression;
            }
        }
        return regularExpression;
    }

    public static boolean matches(String str, String str2) throws ParseException {
        return createRegex(str, null).matches(str2);
    }

    public static boolean matches(String str, String str2, String str3) throws ParseException {
        return createRegex(str, str2).matches(str3);
    }

    public static String quoteMeta(String str) {
        int length = str.length();
        StringBuffer stringBuffer = null;
        for (int i = 0; i < length; i++) {
            char charAt = str.charAt(i);
            if (".*+?{[()|\\^$".indexOf(charAt) >= 0) {
                if (stringBuffer == null) {
                    stringBuffer = new StringBuffer(((length - i) * 2) + i);
                    if (i > 0) {
                        stringBuffer.append(str.substring(0, i));
                    }
                }
                stringBuffer.append(PatternTokenizer.BACK_SLASH);
                stringBuffer.append((char) charAt);
            } else if (stringBuffer != null) {
                stringBuffer.append((char) charAt);
            }
        }
        return stringBuffer != null ? stringBuffer.toString() : str;
    }

    static void dumpString(String str) {
        for (int i = 0; i < str.length(); i++) {
            System.out.print(Integer.toHexString(str.charAt(i)));
            System.out.print(" ");
        }
        System.out.println();
    }
}
