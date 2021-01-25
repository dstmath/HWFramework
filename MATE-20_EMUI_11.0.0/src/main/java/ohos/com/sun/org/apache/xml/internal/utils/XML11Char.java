package ohos.com.sun.org.apache.xml.internal.utils;

import java.util.Arrays;
import ohos.global.icu.lang.UProperty;
import ohos.global.icu.text.Bidi;

public class XML11Char {
    public static final int MASK_XML11_CONTENT = 32;
    public static final int MASK_XML11_CONTENT_INTERNAL = 48;
    public static final int MASK_XML11_CONTROL = 16;
    public static final int MASK_XML11_NAME = 8;
    public static final int MASK_XML11_NAME_START = 4;
    public static final int MASK_XML11_NCNAME = 128;
    public static final int MASK_XML11_NCNAME_START = 64;
    public static final int MASK_XML11_SPACE = 2;
    public static final int MASK_XML11_VALID = 1;
    private static final byte[] XML11CHARS = new byte[65536];

    public static boolean isXML11NameHighSurrogate(int i) {
        return 55296 <= i && i <= 56191;
    }

    static {
        Arrays.fill(XML11CHARS, 1, 9, (byte) 17);
        byte[] bArr = XML11CHARS;
        bArr[9] = 35;
        bArr[10] = 3;
        Arrays.fill(bArr, 11, 13, (byte) 17);
        byte[] bArr2 = XML11CHARS;
        bArr2[13] = 3;
        Arrays.fill(bArr2, 14, 32, (byte) 17);
        byte[] bArr3 = XML11CHARS;
        bArr3[32] = 35;
        Arrays.fill(bArr3, 33, 38, (byte) 33);
        byte[] bArr4 = XML11CHARS;
        bArr4[38] = 1;
        Arrays.fill(bArr4, 39, 45, (byte) 33);
        Arrays.fill(XML11CHARS, 45, 47, (byte) -87);
        byte[] bArr5 = XML11CHARS;
        bArr5[47] = 33;
        Arrays.fill(bArr5, 48, 58, (byte) -87);
        byte[] bArr6 = XML11CHARS;
        bArr6[58] = 45;
        bArr6[59] = 33;
        bArr6[60] = 1;
        Arrays.fill(bArr6, 61, 65, (byte) 33);
        Arrays.fill(XML11CHARS, 65, 91, (byte) -19);
        Arrays.fill(XML11CHARS, 91, 93, (byte) 33);
        byte[] bArr7 = XML11CHARS;
        bArr7[93] = 1;
        bArr7[94] = 33;
        bArr7[95] = -19;
        bArr7[96] = 33;
        Arrays.fill(bArr7, 97, 123, (byte) -19);
        Arrays.fill(XML11CHARS, 123, 127, (byte) 33);
        Arrays.fill(XML11CHARS, 127, 133, (byte) 17);
        byte[] bArr8 = XML11CHARS;
        bArr8[133] = 35;
        Arrays.fill(bArr8, 134, 160, (byte) 17);
        Arrays.fill(XML11CHARS, 160, 183, (byte) 33);
        byte[] bArr9 = XML11CHARS;
        bArr9[183] = -87;
        Arrays.fill(bArr9, 184, 192, (byte) 33);
        Arrays.fill(XML11CHARS, 192, 215, (byte) -19);
        byte[] bArr10 = XML11CHARS;
        bArr10[215] = 33;
        Arrays.fill(bArr10, 216, 247, (byte) -19);
        byte[] bArr11 = XML11CHARS;
        bArr11[247] = 33;
        Arrays.fill(bArr11, 248, 768, (byte) -19);
        Arrays.fill(XML11CHARS, 768, 880, (byte) -87);
        Arrays.fill(XML11CHARS, 880, 894, (byte) -19);
        byte[] bArr12 = XML11CHARS;
        bArr12[894] = 33;
        Arrays.fill(bArr12, 895, 8192, (byte) -19);
        Arrays.fill(XML11CHARS, 8192, 8204, (byte) 33);
        Arrays.fill(XML11CHARS, 8204, 8206, (byte) -19);
        Arrays.fill(XML11CHARS, 8206, 8232, (byte) 33);
        byte[] bArr13 = XML11CHARS;
        bArr13[8232] = 35;
        Arrays.fill(bArr13, 8233, 8255, (byte) 33);
        Arrays.fill(XML11CHARS, 8255, 8257, (byte) -87);
        Arrays.fill(XML11CHARS, 8257, 8304, (byte) 33);
        Arrays.fill(XML11CHARS, 8304, 8592, (byte) -19);
        Arrays.fill(XML11CHARS, 8592, 11264, (byte) 33);
        Arrays.fill(XML11CHARS, 11264, 12272, (byte) -19);
        Arrays.fill(XML11CHARS, 12272, (int) UProperty.DOUBLE_LIMIT, (byte) 33);
        Arrays.fill(XML11CHARS, (int) UProperty.DOUBLE_LIMIT, 55296, (byte) -19);
        Arrays.fill(XML11CHARS, 57344, 63744, (byte) 33);
        Arrays.fill(XML11CHARS, 63744, 64976, (byte) -19);
        Arrays.fill(XML11CHARS, 64976, 65008, (byte) 33);
        Arrays.fill(XML11CHARS, 65008, 65534, (byte) -19);
    }

    public static boolean isXML11Space(int i) {
        return i < 65536 && (XML11CHARS[i] & 2) != 0;
    }

    public static boolean isXML11Valid(int i) {
        if (i >= 65536 || (XML11CHARS[i] & 1) == 0) {
            return 65536 <= i && i <= 1114111;
        }
        return true;
    }

    public static boolean isXML11Invalid(int i) {
        return !isXML11Valid(i);
    }

    public static boolean isXML11ValidLiteral(int i) {
        if (i < 65536) {
            byte[] bArr = XML11CHARS;
            if ((bArr[i] & 1) != 0 && (bArr[i] & 16) == 0) {
                return true;
            }
        }
        return 65536 <= i && i <= 1114111;
    }

    public static boolean isXML11Content(int i) {
        return (i < 65536 && (XML11CHARS[i] & 32) != 0) || (65536 <= i && i <= 1114111);
    }

    public static boolean isXML11InternalEntityContent(int i) {
        return (i < 65536 && (XML11CHARS[i] & 48) != 0) || (65536 <= i && i <= 1114111);
    }

    public static boolean isXML11NameStart(int i) {
        return (i < 65536 && (XML11CHARS[i] & 4) != 0) || (65536 <= i && i < 983040);
    }

    public static boolean isXML11Name(int i) {
        return (i < 65536 && (XML11CHARS[i] & 8) != 0) || (i >= 65536 && i < 983040);
    }

    public static boolean isXML11NCNameStart(int i) {
        return (i < 65536 && (XML11CHARS[i] & 64) != 0) || (65536 <= i && i < 983040);
    }

    public static boolean isXML11NCName(int i) {
        return (i < 65536 && (XML11CHARS[i] & Bidi.LEVEL_OVERRIDE) != 0) || (65536 <= i && i < 983040);
    }

    public static boolean isXML11ValidName(String str) {
        int i;
        int length = str.length();
        if (length == 0) {
            return false;
        }
        char charAt = str.charAt(0);
        if (!isXML11NameStart(charAt)) {
            if (length > 1 && isXML11NameHighSurrogate(charAt)) {
                char charAt2 = str.charAt(1);
                if (XMLChar.isLowSurrogate(charAt2) && isXML11NameStart(XMLChar.supplemental(charAt, charAt2))) {
                    i = 2;
                }
            }
            return false;
        }
        i = 1;
        while (i < length) {
            char charAt3 = str.charAt(i);
            if (!isXML11Name(charAt3)) {
                i++;
                if (i < length && isXML11NameHighSurrogate(charAt3)) {
                    char charAt4 = str.charAt(i);
                    if (XMLChar.isLowSurrogate(charAt4) && isXML11Name(XMLChar.supplemental(charAt3, charAt4))) {
                    }
                }
                return false;
            }
            i++;
        }
        return true;
    }

    public static boolean isXML11ValidNCName(String str) {
        int i;
        int length = str.length();
        if (length == 0) {
            return false;
        }
        char charAt = str.charAt(0);
        if (!isXML11NCNameStart(charAt)) {
            if (length > 1 && isXML11NameHighSurrogate(charAt)) {
                char charAt2 = str.charAt(1);
                if (XMLChar.isLowSurrogate(charAt2) && isXML11NCNameStart(XMLChar.supplemental(charAt, charAt2))) {
                    i = 2;
                }
            }
            return false;
        }
        i = 1;
        while (i < length) {
            char charAt3 = str.charAt(i);
            if (!isXML11NCName(charAt3)) {
                i++;
                if (i < length && isXML11NameHighSurrogate(charAt3)) {
                    char charAt4 = str.charAt(i);
                    if (XMLChar.isLowSurrogate(charAt4) && isXML11NCName(XMLChar.supplemental(charAt3, charAt4))) {
                    }
                }
                return false;
            }
            i++;
        }
        return true;
    }

    public static boolean isXML11ValidNmtoken(String str) {
        int length = str.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        while (i < length) {
            char charAt = str.charAt(i);
            if (!isXML11Name(charAt)) {
                i++;
                if (i < length && isXML11NameHighSurrogate(charAt)) {
                    char charAt2 = str.charAt(i);
                    if (XMLChar.isLowSurrogate(charAt2) && isXML11Name(XMLChar.supplemental(charAt, charAt2))) {
                    }
                }
                return false;
            }
            i++;
        }
        return true;
    }

    public static boolean isXML11ValidQName(String str) {
        int indexOf = str.indexOf(58);
        if (indexOf == 0 || indexOf == str.length() - 1) {
            return false;
        }
        if (indexOf <= 0) {
            return isXML11ValidNCName(str);
        }
        String substring = str.substring(0, indexOf);
        String substring2 = str.substring(indexOf + 1);
        if (!isXML11ValidNCName(substring) || !isXML11ValidNCName(substring2)) {
            return false;
        }
        return true;
    }
}
