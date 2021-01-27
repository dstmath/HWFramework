package ohos.global.icu.impl;

import ohos.com.sun.org.apache.xml.internal.utils.LocaleUtility;
import ohos.global.icu.text.StringPrep;
import ohos.global.icu.text.StringPrepParseException;
import ohos.global.icu.text.UCharacterIterator;

public final class IDNA2003 {
    private static char[] ACE_PREFIX = {'x', 'n', LocaleUtility.IETF_SEPARATOR, LocaleUtility.IETF_SEPARATOR};
    private static final int CAPITAL_A = 65;
    private static final int CAPITAL_Z = 90;
    private static final int FULL_STOP = 46;
    private static final int HYPHEN = 45;
    private static final int LOWER_CASE_DELTA = 32;
    private static final int MAX_DOMAIN_NAME_LENGTH = 255;
    private static final int MAX_LABEL_LENGTH = 63;
    private static final StringPrep namePrep = StringPrep.getInstance(0);

    private static boolean isLDHChar(int i) {
        if (i > 122) {
            return false;
        }
        if (i == 45) {
            return true;
        }
        if (48 <= i && i <= 57) {
            return true;
        }
        if (65 > i || i > 90) {
            return 97 <= i && i <= 122;
        }
        return true;
    }

    private static boolean isLabelSeparator(int i) {
        return i == 46 || i == 12290 || i == 65294 || i == 65377;
    }

    private static char toASCIILower(char c) {
        return ('A' > c || c > 'Z') ? c : (char) (c + ' ');
    }

    private static boolean startsWithPrefix(StringBuffer stringBuffer) {
        if (stringBuffer.length() < ACE_PREFIX.length) {
            return false;
        }
        for (int i = 0; i < ACE_PREFIX.length; i++) {
            if (toASCIILower(stringBuffer.charAt(i)) != ACE_PREFIX[i]) {
                return false;
            }
        }
        return true;
    }

    private static StringBuffer toASCIILower(CharSequence charSequence) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < charSequence.length(); i++) {
            stringBuffer.append(toASCIILower(charSequence.charAt(i)));
        }
        return stringBuffer;
    }

    private static int compareCaseInsensitiveASCII(StringBuffer stringBuffer, StringBuffer stringBuffer2) {
        int aSCIILower;
        for (int i = 0; i != stringBuffer.length(); i++) {
            char charAt = stringBuffer.charAt(i);
            char charAt2 = stringBuffer2.charAt(i);
            if (!(charAt == charAt2 || (aSCIILower = toASCIILower(charAt) - toASCIILower(charAt2)) == 0)) {
                return aSCIILower;
            }
        }
        return 0;
    }

    private static int getSeparatorIndex(char[] cArr, int i, int i2) {
        while (i < i2 && !isLabelSeparator(cArr[i])) {
            i++;
        }
        return i;
    }

    public static StringBuffer convertToASCII(UCharacterIterator uCharacterIterator, int i) throws StringPrepParseException {
        boolean z;
        StringBuffer stringBuffer;
        boolean z2 = (i & 2) != 0;
        while (true) {
            int next = uCharacterIterator.next();
            if (next != -1) {
                if (next > 127) {
                    z = false;
                    break;
                }
            } else {
                z = true;
                break;
            }
        }
        uCharacterIterator.setToStart();
        if (!z) {
            stringBuffer = namePrep.prepare(uCharacterIterator, i);
        } else {
            stringBuffer = new StringBuffer(uCharacterIterator.getText());
        }
        int length = stringBuffer.length();
        if (length != 0) {
            StringBuffer stringBuffer2 = new StringBuffer();
            boolean z3 = true;
            boolean z4 = true;
            int i2 = -1;
            for (int i3 = 0; i3 < length; i3++) {
                char charAt = stringBuffer.charAt(i3);
                if (charAt > 127) {
                    z4 = false;
                } else if (!isLDHChar(charAt)) {
                    z3 = false;
                    i2 = i3;
                }
            }
            if (!z2 || (z3 && stringBuffer.charAt(0) != '-' && stringBuffer.charAt(stringBuffer.length() - 1) != '-')) {
                if (!z4) {
                    if (!startsWithPrefix(stringBuffer)) {
                        StringBuffer aSCIILower = toASCIILower(Punycode.encode(stringBuffer, new boolean[length]));
                        char[] cArr = ACE_PREFIX;
                        stringBuffer2.append(cArr, 0, cArr.length);
                        stringBuffer2.append(aSCIILower);
                        stringBuffer = stringBuffer2;
                    } else {
                        throw new StringPrepParseException("The input does not start with the ACE Prefix.", 6, stringBuffer.toString(), 0);
                    }
                }
                if (stringBuffer.length() <= 63) {
                    return stringBuffer;
                }
                throw new StringPrepParseException("The labels in the input are too long. Length > 63.", 8, stringBuffer.toString(), 0);
            } else if (!z3) {
                String stringBuffer3 = stringBuffer.toString();
                if (i2 > 0) {
                    i2--;
                }
                throw new StringPrepParseException("The input does not conform to the STD 3 ASCII rules", 5, stringBuffer3, i2);
            } else if (stringBuffer.charAt(0) != '-') {
                String stringBuffer4 = stringBuffer.toString();
                if (length > 0) {
                    length--;
                }
                throw new StringPrepParseException("The input does not conform to the STD 3 ASCII rules", 5, stringBuffer4, length);
            } else {
                throw new StringPrepParseException("The input does not conform to the STD 3 ASCII rules", 5, stringBuffer.toString(), 0);
            }
        } else {
            throw new StringPrepParseException("Found zero length lable after NamePrep.", 10);
        }
    }

    public static StringBuffer convertIDNToASCII(String str, int i) throws StringPrepParseException {
        char[] charArray = str.toCharArray();
        StringBuffer stringBuffer = new StringBuffer();
        int i2 = 0;
        int i3 = 0;
        while (true) {
            int separatorIndex = getSeparatorIndex(charArray, i2, charArray.length);
            String str2 = new String(charArray, i3, separatorIndex - i3);
            if (!(str2.length() == 0 && separatorIndex == charArray.length)) {
                stringBuffer.append(convertToASCII(UCharacterIterator.getInstance(str2), i));
            }
            if (separatorIndex == charArray.length) {
                break;
            }
            i3 = separatorIndex + 1;
            stringBuffer.append('.');
            i2 = i3;
        }
        if (stringBuffer.length() <= 255) {
            return stringBuffer;
        }
        throw new StringPrepParseException("The output exceed the max allowed length.", 11);
    }

    public static StringBuffer convertToUnicode(UCharacterIterator uCharacterIterator, int i) throws StringPrepParseException {
        StringBuffer stringBuffer;
        StringBuffer stringBuffer2;
        int index = uCharacterIterator.getIndex();
        boolean z = true;
        while (true) {
            int next = uCharacterIterator.next();
            if (next == -1) {
                break;
            } else if (next > 127) {
                z = false;
            }
        }
        if (!z) {
            try {
                uCharacterIterator.setIndex(index);
                stringBuffer = namePrep.prepare(uCharacterIterator, i);
            } catch (StringPrepParseException unused) {
                return new StringBuffer(uCharacterIterator.getText());
            }
        } else {
            stringBuffer = new StringBuffer(uCharacterIterator.getText());
        }
        if (startsWithPrefix(stringBuffer)) {
            StringBuffer stringBuffer3 = null;
            try {
                stringBuffer2 = new StringBuffer(Punycode.decode(stringBuffer.substring(ACE_PREFIX.length, stringBuffer.length()), null));
            } catch (StringPrepParseException unused2) {
                stringBuffer2 = null;
            }
            if (stringBuffer2 == null || compareCaseInsensitiveASCII(stringBuffer, convertToASCII(UCharacterIterator.getInstance(stringBuffer2), i)) == 0) {
                stringBuffer3 = stringBuffer2;
            }
            if (stringBuffer3 != null) {
                return stringBuffer3;
            }
        }
        return new StringBuffer(uCharacterIterator.getText());
    }

    public static StringBuffer convertIDNToUnicode(String str, int i) throws StringPrepParseException {
        char[] charArray = str.toCharArray();
        StringBuffer stringBuffer = new StringBuffer();
        int i2 = 0;
        int i3 = 0;
        while (true) {
            int separatorIndex = getSeparatorIndex(charArray, i2, charArray.length);
            String str2 = new String(charArray, i3, separatorIndex - i3);
            if (str2.length() != 0 || separatorIndex == charArray.length) {
                stringBuffer.append(convertToUnicode(UCharacterIterator.getInstance(str2), i));
                if (separatorIndex != charArray.length) {
                    stringBuffer.append(charArray[separatorIndex]);
                    i3 = separatorIndex + 1;
                    i2 = i3;
                } else if (stringBuffer.length() <= 255) {
                    return stringBuffer;
                } else {
                    throw new StringPrepParseException("The output exceed the max allowed length.", 11);
                }
            } else {
                throw new StringPrepParseException("Found zero length lable after NamePrep.", 10);
            }
        }
    }

    public static int compare(String str, String str2, int i) throws StringPrepParseException {
        return compareCaseInsensitiveASCII(convertIDNToASCII(str, i), convertIDNToASCII(str2, i));
    }
}
