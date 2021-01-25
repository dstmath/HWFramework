package ohos.agp.utils;

import java.util.Locale;

public class TextTool {
    private TextTool() {
    }

    public static boolean isNullOrEmpty(CharSequence charSequence) {
        return charSequence == null || charSequence.length() <= 0;
    }

    public static boolean isEqual(CharSequence charSequence, CharSequence charSequence2) {
        int length;
        if (charSequence == charSequence2) {
            return true;
        }
        if (charSequence == null || charSequence2 == null || (length = charSequence.length()) != charSequence2.length()) {
            return false;
        }
        if ((charSequence instanceof String) && (charSequence2 instanceof String) && charSequence.equals(charSequence2)) {
            return true;
        }
        for (int i = 0; i < length; i++) {
            if (charSequence.charAt(i) != charSequence2.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isLayoutRightToLeft(Locale locale) {
        return (locale != null && !locale.equals(Locale.ROOT) && TextToolHelper.isRightToLeft(locale)) || TextToolHelper.sysGetBooleanRTL();
    }

    public static int findChar(CharSequence charSequence, char c, int i, int i2) {
        if (i < 0) {
            throw new StringIndexOutOfBoundsException(i);
        } else if (i <= i2) {
            int length = charSequence.length();
            if (i2 > length) {
                i2 = length;
            }
            while (i < i2) {
                if (charSequence.charAt(i) == c) {
                    return i;
                }
                i++;
            }
            return -1;
        } else {
            throw new StringIndexOutOfBoundsException(i2 - i);
        }
    }

    public static String getSubstring(CharSequence charSequence, int i, int i2) {
        if (i < 0) {
            throw new StringIndexOutOfBoundsException(i);
        } else if (i2 < charSequence.length()) {
            int i3 = i2 - i;
            if (i3 < 0) {
                throw new StringIndexOutOfBoundsException(i3);
            } else if (charSequence instanceof String) {
                return ((String) charSequence).substring(i, i2);
            } else {
                if (charSequence instanceof StringBuilder) {
                    return ((StringBuilder) charSequence).substring(i, i2);
                }
                if (charSequence instanceof StringBuffer) {
                    return ((StringBuffer) charSequence).substring(i, i2);
                }
                return new StringBuffer(charSequence).substring(i, i2);
            }
        } else {
            throw new StringIndexOutOfBoundsException(i2);
        }
    }

    public static char[] subCharArray(CharSequence charSequence, int i, int i2) {
        if (i < 0) {
            throw new StringIndexOutOfBoundsException(i);
        } else if (i2 >= charSequence.length()) {
            throw new StringIndexOutOfBoundsException(i2);
        } else if (i <= i2) {
            char[] cArr = new char[(i2 - i)];
            subCharArray(charSequence, i, i2, cArr, 0);
            return cArr;
        } else {
            throw new StringIndexOutOfBoundsException(i2 - i);
        }
    }

    public static void subCharArray(CharSequence charSequence, int i, int i2, char[] cArr, int i3) {
        if (i < 0) {
            throw new StringIndexOutOfBoundsException(i);
        } else if (i2 >= charSequence.length()) {
            throw new StringIndexOutOfBoundsException(i2);
        } else if (i > i2) {
            throw new StringIndexOutOfBoundsException(i2 - i);
        } else if (charSequence instanceof String) {
            ((String) charSequence).getChars(i, i2, cArr, i3);
        } else if (charSequence instanceof StringBuilder) {
            ((StringBuilder) charSequence).getChars(i, i2, cArr, i3);
        } else if (charSequence instanceof StringBuffer) {
            ((StringBuffer) charSequence).getChars(i, i2, cArr, i3);
        } else {
            int length = cArr.length;
            if (length < i2 - i) {
                throw new StringIndexOutOfBoundsException(length);
            } else if (i3 < cArr.length) {
                while (i < i2) {
                    cArr[i3] = charSequence.charAt(i);
                    i3++;
                    i++;
                }
            }
        }
    }
}
